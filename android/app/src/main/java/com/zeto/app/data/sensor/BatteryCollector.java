package com.zeto.app.data.sensor;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import dagger.hilt.android.qualifiers.ApplicationContext;
import timber.log.Timber;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Collects battery-related information from the Android system by listening
 * to the sticky {@link Intent#ACTION_BATTERY_CHANGED} broadcast.
 */
@Singleton
public class BatteryCollector {

    private final Context context;

    /**
     * @param context application context
     */
    @Inject
    public BatteryCollector(@ApplicationContext Context context) {
        this.context = context;
    }

    /**
     * @return battery percentage as a string (e.g. {@code "85"}), or {@code "UNKNOWN"}
     */
    public String getBatteryLevel() {
        try {
            Intent batteryIntent = context.registerReceiver(
                    null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            );
            if (batteryIntent == null) {
                Timber.w("Battery intent is null");
                return "UNKNOWN";
            }
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level == -1 || scale == -1) {
                return "UNKNOWN";
            }
            int batteryPct = (int) ((level / (float) scale) * 100);
            Timber.d("Battery level: %d%%", batteryPct);
            return String.valueOf(batteryPct);
        } catch (Exception e) {
            Timber.e(e, "Failed to get battery level");
            return "UNKNOWN";
        }
    }

    /**
     * @return {@code "true"} if charging or full, {@code "false"} if discharging,
     *         or {@code "UNKNOWN"} if the state can't be determined
     */
    public String isCharging() {
        try {
            Intent batteryIntent = context.registerReceiver(
                    null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            );
            if (batteryIntent == null) {
                return "UNKNOWN";
            }
            int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean charging = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL;
            Timber.d("Is charging: %b", charging);
            return String.valueOf(charging);
        } catch (Exception e) {
            Timber.e(e, "Failed to get charging status");
            return "UNKNOWN";
        }
    }
}
