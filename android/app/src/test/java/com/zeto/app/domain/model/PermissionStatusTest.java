package com.zeto.app.domain.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class PermissionStatusTest {

    @Test
    public void bothGranted_returnsCorrectFlags() {
        PermissionStatus status = new PermissionStatus(true, true);

        assertTrue(status.isLocationGranted());
        assertTrue(status.isNotificationGranted());
    }

    @Test
    public void noneGranted_returnsCorrectFlags() {
        PermissionStatus status = new PermissionStatus(false, false);

        assertFalse(status.isLocationGranted());
        assertFalse(status.isNotificationGranted());
    }

    @Test
    public void onlyLocationGranted_notificationFalse() {
        PermissionStatus status = new PermissionStatus(true, false);

        assertTrue(status.isLocationGranted());
        assertFalse(status.isNotificationGranted());
    }

    @Test
    public void onlyNotificationGranted_locationFalse() {
        PermissionStatus status = new PermissionStatus(false, true);

        assertFalse(status.isLocationGranted());
        assertTrue(status.isNotificationGranted());
    }

    @Test
    public void permissionDeniedConstant_hasExpectedValue() {
        assertEquals("PERMISSION_DENIED", PermissionStatus.PERMISSION_DENIED);
    }
}
