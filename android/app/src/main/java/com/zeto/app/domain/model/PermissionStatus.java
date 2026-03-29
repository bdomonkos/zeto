package com.zeto.app.domain.model;

import lombok.Getter;

/**
 * Tracks which runtime permissions the user has granted.
 * Immutable – replace the whole object when permissions change.
 */
@Getter
public class PermissionStatus {

    /** Placeholder sent to the backend when a required permission is denied. */
    public static final String PERMISSION_DENIED = "PERMISSION_DENIED";

    /** {@code true} if {@code ACCESS_FINE_LOCATION} has been granted. */
    private final boolean locationGranted;

    /** {@code true} if {@code POST_NOTIFICATIONS} has been granted (or Android < 13). */
    private final boolean notificationGranted;

    /**
     * @param locationGranted     {@code ACCESS_FINE_LOCATION} granted
     * @param notificationGranted {@code POST_NOTIFICATIONS} granted
     */
    public PermissionStatus(boolean locationGranted, boolean notificationGranted) {
        this.locationGranted = locationGranted;
        this.notificationGranted = notificationGranted;
    }
}
