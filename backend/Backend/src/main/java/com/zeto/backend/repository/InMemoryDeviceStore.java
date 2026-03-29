package com.zeto.backend.repository;

import com.zeto.backend.model.DeviceStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory {@link DeviceStore} backed by a {@link ConcurrentHashMap}.
 * Each {@code deviceId} maps to its latest status; saves overwrite previous entries.
 */
@Repository
public class InMemoryDeviceStore implements DeviceStore {

    private final Map<String, DeviceStatus> devices = new ConcurrentHashMap<>();

    /**
     * @param deviceId device to look up
     * @return stored status, or {@code null} if not found
     */
    @Override
    public DeviceStatus findById(String deviceId) {
        return devices.get(deviceId);
    }

    /**
     * @param status device status to store (overwrites any existing entry for the same ID)
     */
    @Override
    public void save(DeviceStatus status) {
        devices.put(status.getDeviceId(), status);
    }

    /**
     * @return snapshot copy of all stored device statuses
     */
    @Override
    public List<DeviceStatus> findAll() {
        return new ArrayList<>(devices.values());
    }
}