package com.zeto.backend.model;

import lombok.Data;

import java.util.Map;

/** Last known status of a device, stored in memory by {@code deviceId}. */
@Data
public class DeviceStatus {

    /** Unique identifier of the device. */
    private String deviceId;

    /** Device manufacturer name. */
    private String manufacturer;

    /** Device model name. */
    private String model;

    /** Android OS version (SDK integer as string). */
    private String osVersion;

    /** Current battery level (0–100), or {@code null} if unavailable. */
    private Integer batteryLevel;

    /** Whether the device is currently charging, or {@code null} if unavailable. */
    private Boolean charging;

    /** GPS latitude as a string, or a sentinel value if unavailable. */
    private String latitude;

    /** GPS longitude as a string, or a sentinel value if unavailable. */
    private String longitude;

    /** SSID of the connected Wi-Fi network, or a sentinel value if unavailable. */
    private String wifiSsid;

    /** RSSI signal strength of the connected Wi-Fi network. */
    private String wifiRssi;

    /** Unix epoch timestamp (milliseconds) from the device when the data was collected. */
    private long timestamp;

    /** Map of permission names to sentinel strings for denied permissions. */
    private Map<String, String> missingPermissions;

    /** Server-side Unix epoch timestamp (milliseconds) when this status was last stored. */
    private long lastSeen;
}
