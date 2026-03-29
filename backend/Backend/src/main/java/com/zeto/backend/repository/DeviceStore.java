package com.zeto.backend.repository;

import com.zeto.backend.model.DeviceStatus;

import java.util.List;

/**
 * Storage abstraction used by {@link com.zeto.backend.service.DeviceService}.
 * Implementations can be in-memory, database-backed, or anything else.
 */
public interface DeviceStore {

    /**
     * @param deviceId device to look up
     * @return the latest {@link DeviceStatus}, or {@code null} if not found
     */
    DeviceStatus findById(String deviceId);

    /**
     * Saves or overwrites the status for this device ID.
     *
     * @param status the device status to store
     */
    void save(DeviceStatus status);

    /**
     * @return all stored device statuses
     */
    List<DeviceStatus> findAll();
}