package com.zeto.app.data.sensor;

import android.content.Context;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.zeto.app.domain.model.PermissionStatus;

import dagger.hilt.android.qualifiers.ApplicationContext;
import timber.log.Timber;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Fetches the last known GPS location via the Fused Location Provider.
 * The result comes back asynchronously through {@link LocationCallback}.
 */
@Singleton
public class LocationCollector {

    private final FusedLocationProviderClient fusedLocationClient;

    /**
     * @param context application context
     */
    @Inject
    public LocationCollector(@ApplicationContext Context context) {
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Looks up the last known location and delivers it to {@code callback}.
     * If permission is missing, the callback fires immediately with
     * {@link PermissionStatus#PERMISSION_DENIED} for both coordinates.
     *
     * @param permissionStatus current runtime permission state
     * @param callback         called with latitude and longitude strings once the result is ready
     */
    public void getLocation(PermissionStatus permissionStatus, LocationCallback callback) {
        if (!permissionStatus.isLocationGranted()) {
            Timber.w("Location permission denied");
            callback.onResult(PermissionStatus.PERMISSION_DENIED, PermissionStatus.PERMISSION_DENIED);
            return;
        }
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Timber.d("Location: %f, %f", location.getLatitude(), location.getLongitude());
                            callback.onResult(
                                    String.valueOf(location.getLatitude()),
                                    String.valueOf(location.getLongitude())
                            );
                        } else {
                            Timber.w("Location is null");
                            callback.onResult("UNKNOWN", "UNKNOWN");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Timber.e(e, "Failed to get location");
                        callback.onResult("UNKNOWN", "UNKNOWN");
                    });
        } catch (SecurityException e) {
            Timber.e(e, "Security exception getting location");
            callback.onResult(PermissionStatus.PERMISSION_DENIED, PermissionStatus.PERMISSION_DENIED);
        }
    }

    /** Receives the async location result. */
    public interface LocationCallback {

        /**
         * @param latitude  GPS latitude string, or a sentinel value on failure
         * @param longitude GPS longitude string, or a sentinel value on failure
         */
        void onResult(String latitude, String longitude);
    }
}
