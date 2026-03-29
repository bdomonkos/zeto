package com.zeto.app.data.sensor;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import dagger.hilt.android.qualifiers.ApplicationContext;
import timber.log.Timber;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Collects static device hardware and OS information from the Android platform.
 */
@Singleton
public class DeviceInfoCollector {

    private final Context context;

    /**
     * @param context application context
     */
    @Inject
    public DeviceInfoCollector(@ApplicationContext Context context) {
        this.context = context;
    }

    /**
     * @return {@code Settings.Secure.ANDROID_ID}, or {@code "UNKNOWN"} on failure
     */
    public String getDeviceId() {
        try {
            String deviceId = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
            Timber.d("Device ID collected: %s", deviceId);
            return deviceId;
        } catch (Exception e) {
            Timber.e(e, "Failed to get device ID");
            return "UNKNOWN";
        }
    }

    /** @return {@link Build#MANUFACTURER} */
    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    /** @return {@link Build#MODEL} */
    public String getModel() {
        return Build.MODEL;
    }

    /** @return {@link Build.VERSION#SDK_INT} as a string */
    public String getOsVersion() {
        return String.valueOf(Build.VERSION.SDK_INT);
    }
}
