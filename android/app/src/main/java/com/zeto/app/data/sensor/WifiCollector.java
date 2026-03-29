package com.zeto.app.data.sensor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;

import com.zeto.app.domain.model.PermissionStatus;

import dagger.hilt.android.qualifiers.ApplicationContext;
import timber.log.Timber;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Collects Wi-Fi network information (SSID and RSSI) using the
 * {@link ConnectivityManager} transport info API (API 31+).
 * Reading the SSID requires location permission to be granted.
 */
@Singleton
public class WifiCollector {

    private final Context context;

    /**
     * @param context application context
     */
    @Inject
    public WifiCollector(@ApplicationContext Context context) {
        this.context = context;
    }

    /**
     * Returns the SSID of the active Wi-Fi network. Needs location permission.
     *
     * @param permissionStatus current runtime permission state
     * @return SSID string, {@code "WIFI_DISABLED"} if not connected,
     *         {@code "UNKNOWN"} if unreadable,
     *         or {@link PermissionStatus#PERMISSION_DENIED} if permission is missing
     */
    public String getWifiSsid(PermissionStatus permissionStatus) {
        if (!permissionStatus.isLocationGranted()) {
            return PermissionStatus.PERMISSION_DENIED;
        }
        try {
            WifiInfo wifiInfo = getWifiInfo();
            if (wifiInfo == null) return "WIFI_DISABLED";
            String ssid = wifiInfo.getSSID();
            if (ssid == null || ssid.equals("<unknown ssid>")) return "UNKNOWN";
            return ssid.replaceAll("\"", "");
        } catch (Exception e) {
            Timber.e(e, "Failed to get WiFi SSID");
            return "UNKNOWN";
        }
    }

    /**
     * Returns the RSSI (dBm) of the active Wi-Fi network. Needs location permission.
     *
     * @param permissionStatus current runtime permission state
     * @return RSSI string, {@code "WIFI_DISABLED"} if not connected,
     *         {@code "UNKNOWN"} on error,
     *         or {@link PermissionStatus#PERMISSION_DENIED} if permission is missing
     */
    public String getWifiRssi(PermissionStatus permissionStatus) {
        if (!permissionStatus.isLocationGranted()) {
            return PermissionStatus.PERMISSION_DENIED;
        }
        try {
            WifiInfo wifiInfo = getWifiInfo();
            if (wifiInfo == null) return "WIFI_DISABLED";
            return String.valueOf(wifiInfo.getRssi());
        } catch (Exception e) {
            Timber.e(e, "Failed to get WiFi RSSI");
            return "UNKNOWN";
        }
    }

    /**
     * @return {@link WifiInfo} for the active network, or {@code null} if Wi-Fi is off/disconnected
     */
    private WifiInfo getWifiInfo() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return null;
        Network network = cm.getActiveNetwork();
        if (network == null) return null;
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        if (caps == null) return null;
        return (WifiInfo) caps.getTransportInfo();
    }
}
