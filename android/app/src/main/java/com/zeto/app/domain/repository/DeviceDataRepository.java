package com.zeto.app.domain.repository;

import com.zeto.app.domain.model.DeviceStatus;

/**
 * Repository abstraction for sending device status data to the backend.
 * Implementations handle the actual transport (e.g. HTTP via Retrofit).
 */
public interface DeviceDataRepository {

    /**
     * Transmits the given device status snapshot to the remote backend.
     * The call is performed asynchronously; failures are logged internally.
     *
     * @param deviceStatus the status snapshot to send
     */
    void sendDeviceStatus(DeviceStatus deviceStatus);
}
