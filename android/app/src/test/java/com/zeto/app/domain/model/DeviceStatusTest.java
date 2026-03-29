package com.zeto.app.domain.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the {@link DeviceStatus} Lombok builder to ensure all fields
 * are correctly populated and accessible via generated getters.
 */
public class DeviceStatusTest {

    @Test
    public void builder_allFields_setCorrectly() {
        DeviceStatus status = DeviceStatus.builder()
                .deviceId("device-1")
                .manufacturer("Acme")
                .model("X1")
                .osVersion("34")
                .batteryLevel("85")
                .charging("true")
                .latitude("47.5")
                .longitude("19.0")
                .wifiSsid("HomeNet")
                .wifiRssi("-60")
                .timestamp(1000L)
                .build();

        assertEquals("device-1", status.getDeviceId());
        assertEquals("Acme", status.getManufacturer());
        assertEquals("X1", status.getModel());
        assertEquals("34", status.getOsVersion());
        assertEquals("85", status.getBatteryLevel());
        assertEquals("true", status.getCharging());
        assertEquals("47.5", status.getLatitude());
        assertEquals("19.0", status.getLongitude());
        assertEquals("HomeNet", status.getWifiSsid());
        assertEquals("-60", status.getWifiRssi());
        assertEquals(1000L, status.getTimestamp());
    }

    @Test
    public void builder_defaultTimestamp_isZero() {
        DeviceStatus status = DeviceStatus.builder().deviceId("d").build();
        assertEquals(0L, status.getTimestamp());
    }

    @Test
    public void builder_nullableFieldsDefaultToNull() {
        DeviceStatus status = DeviceStatus.builder().build();

        assertNull(status.getDeviceId());
        assertNull(status.getManufacturer());
        assertNull(status.getModel());
        assertNull(status.getLatitude());
        assertNull(status.getLongitude());
        assertNull(status.getWifiSsid());
        assertNull(status.getBatteryLevel());
        assertNull(status.getCharging());
    }
}
