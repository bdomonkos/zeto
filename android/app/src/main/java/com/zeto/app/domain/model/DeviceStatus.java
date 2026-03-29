package com.zeto.app.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Snapshot of device state collected by the sensors and sent to the backend.
 */
@Getter
@Builder
public class DeviceStatus {

    /** Unique Android device identifier ({@code Settings.Secure.ANDROID_ID}). */
    private String deviceId;

    /** Device manufacturer (e.g. {@code Samsung}). */
    private String manufacturer;

    /** Device model name (e.g. {@code Galaxy S24}). */
    private String model;

    /** Android SDK version as a string (e.g. {@code "34"}). */
    private String osVersion;

    /** Battery percentage (0–100) as a string, or {@code "UNKNOWN"}. */
    private String batteryLevel;

    /** {@code "true"} / {@code "false"} / {@code "UNKNOWN"} charging state. */
    private String charging;

    /** GPS latitude as a string, or a sentinel value if unavailable. */
    private String latitude;

    /** GPS longitude as a string, or a sentinel value if unavailable. */
    private String longitude;

    /** SSID of the connected Wi-Fi network, or a sentinel value if unavailable. */
    private String wifiSsid;

    /** RSSI signal strength as a string, or a sentinel value if unavailable. */
    private String wifiRssi;

    /** Unix epoch timestamp (milliseconds) when this snapshot was taken. */
    private long timestamp;
}
