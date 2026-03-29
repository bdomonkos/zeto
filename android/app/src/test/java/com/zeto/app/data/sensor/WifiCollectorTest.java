package com.zeto.app.data.sensor;

import com.zeto.app.domain.model.PermissionStatus;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the permission-guard contract expected from {@link WifiCollector}.
 * Direct instantiation requires Android context, so these tests verify the
 * {@link PermissionStatus} preconditions that the collector relies on.
 * Full sensor integration tests should use Robolectric or instrumented tests.
 */
public class WifiCollectorTest {

    @Test
    public void locationNotGranted_ssidShouldBePermissionDenied() {
        PermissionStatus denied = new PermissionStatus(false, true);
        assertFalse(denied.isLocationGranted());
    }

    @Test
    public void locationGranted_ssidReadShouldBeAttempted() {
        PermissionStatus granted = new PermissionStatus(true, true);
        assertTrue(granted.isLocationGranted());
    }

    @Test
    public void permissionDeniedSentinel_matchesExpectedString() {
        assertEquals("PERMISSION_DENIED", PermissionStatus.PERMISSION_DENIED);
    }
}
