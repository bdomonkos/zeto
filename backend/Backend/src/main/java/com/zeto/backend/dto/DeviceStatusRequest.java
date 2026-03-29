package com.zeto.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/** Device status payload sent by the phone. {@code @NotBlank}/{@code @NotNull} fields are required. */
@Data
public class DeviceStatusRequest {

    /** Unique identifier of the device. */
    @NotBlank
    private String deviceId;

    /** Device manufacturer name. */
    @NotBlank
    private String manufacturer;

    /** Device model name. */
    @NotBlank
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

    /** Unix epoch timestamp (milliseconds) when the status was collected. */
    @NotNull
    private Long timestamp;

    /** Map of permission names to sentinel strings for denied permissions. */
    private Map<String, String> missingPermissions;
}
