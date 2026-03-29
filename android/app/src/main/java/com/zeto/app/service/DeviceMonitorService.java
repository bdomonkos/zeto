package com.zeto.app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.zeto.app.domain.model.PermissionStatus;
import com.zeto.app.domain.usecase.CollectDeviceDataUseCase;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

/**
 * Foreground service that monitors device state and sends updates to the backend.
 * Sensor events are debounced ({@value DEBOUNCE_DELAY_MS} ms) to avoid flooding
 * when several things change at once (e.g. battery + Wi-Fi on charger plug-in).
 */
@AndroidEntryPoint
public class DeviceMonitorService extends Service {

    private static final String CHANNEL_ID = "device_monitor_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final long DEBOUNCE_DELAY_MS = 5000;

    @Inject
    CollectDeviceDataUseCase collectDeviceDataUseCase;

    private PermissionStatus permissionStatus = new PermissionStatus(false, false);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable sendDataRunnable = this::collectAndSend;

    private ConnectivityManager connectivityManager;
    private FusedLocationProviderClient fusedLocationClient;

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scheduleDataCollection();
        }
    };

    private final ConnectivityManager.NetworkCallback wifiNetworkCallback =
            new ConnectivityManager.NetworkCallback() {
                @Override
                public void onCapabilitiesChanged(@NonNull Network network,
                                                  @NonNull NetworkCapabilities caps) {
                    scheduleDataCollection();
                }

                @Override
                public void onLost(@NonNull Network network) {
                    scheduleDataCollection();
                }
            };

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult result) {
            scheduleDataCollection();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();
        registerReceivers();
    }

    /**
     * Reads permission flags from the intent, goes foreground, and kicks off the first data send.
     *
     * @param intent  start intent; may be {@code null} on system restart
     * @param flags   additional start flags
     * @param startId unique ID for this start request
     * @return {@link #START_STICKY} – the system will restart the service if killed
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            boolean hasLocation = intent.getBooleanExtra("hasLocation", false);
            boolean hasNotification = intent.getBooleanExtra("hasNotification", false);
            permissionStatus = new PermissionStatus(hasLocation, hasNotification);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, buildNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(NOTIFICATION_ID, buildNotification());
        }
        registerLocationCallback();
        collectAndSend();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(sendDataRunnable);
        fusedLocationClient.removeLocationUpdates(locationCallback);
        unregisterReceivers();
    }

    /** Cancels any queued run and schedules a fresh one after {@value DEBOUNCE_DELAY_MS} ms. */
    private void scheduleDataCollection() {
        handler.removeCallbacks(sendDataRunnable);
        handler.postDelayed(sendDataRunnable, DEBOUNCE_DELAY_MS);
    }

    /** Runs the collect-and-send cycle via the use case. */
    private void collectAndSend() {
        collectDeviceDataUseCase.execute(permissionStatus);
    }

    /** Starts location updates (5 s interval, 10 m threshold). No-op if permission is missing. */
    private void registerLocationCallback() {
        if (!permissionStatus.isLocationGranted()) return;
        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, 5000)
                .setMinUpdateDistanceMeters(10f)
                .build();
        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            Timber.e(e, "Location permission missing");
        }
    }

    /**
     * Registers the battery broadcast receiver and the Wi-Fi network callback.
     */
    private void registerReceivers() {
        registerReceiver(batteryReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                Context.RECEIVER_NOT_EXPORTED);

        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();
        connectivityManager.registerNetworkCallback(request, wifiNetworkCallback);
    }

    /** Tears down battery receiver and Wi-Fi callback; swallows exceptions if they were never registered. */
    private void unregisterReceivers() {
        try {
            unregisterReceiver(batteryReceiver);
            connectivityManager.unregisterNetworkCallback(wifiNetworkCallback);
        } catch (Exception e) {
            Timber.e(e, "Error unregistering receivers");
        }
    }

    /** Sets up the notification channel needed for the foreground service (API 26+). */
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Device Monitor",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Monitors device status in background");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    /**
     * Builds the ongoing notification shown while the service is running.
     *
     * @return the ready-to-use {@link Notification}
     */
    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Device Monitor")
                .setContentText("Monitoring device status...")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setOngoing(true)
                .build();
    }
}
