package com.zeto.backend.service;

import com.zeto.backend.dto.DeviceStatusRequest;
import com.zeto.backend.model.DeviceStatus;
import com.zeto.backend.repository.DeviceStore;
import com.zeto.backend.websocket.DeviceWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Manages device status updates. Storage is behind a {@link DeviceStore} so
 * the implementation (in-memory, DB, …) can change without touching this class.
 * Incoming updates older than what's already stored are silently dropped.
 */
@Slf4j
@Service
public class DeviceService {

    private final DeviceStore deviceStore;
    private final DeviceWebSocketHandler wsHandler;

    /**
     * @param deviceStore storage abstraction
     * @param wsHandler   WebSocket handler for broadcasting updates
     */
    public DeviceService(DeviceStore deviceStore,
                         DeviceWebSocketHandler wsHandler) {
        this.deviceStore = deviceStore;
        this.wsHandler = wsHandler;
    }

    /**
     * Applies an incoming status update if it's newer than what's stored,
     * then broadcasts it over WebSocket.
     *
     * @param request device status from the phone
     * @return the mapped {@link DeviceStatus} (may be outdated if the update was discarded)
     * @throws IllegalArgumentException if the request fails business-rule validation
     */
    public DeviceStatus update(DeviceStatusRequest request) {
        log.info("Incoming status from deviceId={}", request.getDeviceId());

        validateRequest(request);

        log.debug("Payload: manufacturer={}, model={}, battery={}, charging={}",
                request.getManufacturer(),
                request.getModel(),
                request.getBatteryLevel(),
                request.getCharging());

        if (request.getMissingPermissions() != null && !request.getMissingPermissions().isEmpty()) {
            log.warn("Device {} missing permissions: {}",
                    request.getDeviceId(),
                    request.getMissingPermissions());
        }

        DeviceStatus status = mapToModel(request);
        DeviceStatus existing = deviceStore.findById(status.getDeviceId());

        if (existing == null) {
            log.info("New device registered: {}", status.getDeviceId());
        } else {
            log.debug("Timestamp comparison for device {}: incoming={}, existing={}",
                    status.getDeviceId(),
                    status.getTimestamp(),
                    existing.getTimestamp());
        }

        if (existing == null || status.getTimestamp() > existing.getTimestamp()) {
            status.setLastSeen(System.currentTimeMillis());
            deviceStore.save(status);

            log.info("Device {} updated successfully", status.getDeviceId());
            wsHandler.broadcast(status);
        } else {
            log.warn("Ignored outdated status for device {}", status.getDeviceId());
        }

        log.debug("Total devices in system: {}", deviceStore.findAll().size());
        return status;
    }

    /**
     * All known devices sorted by manufacturer then model (nulls last).
     *
     * @return list of all stored device statuses
     */
    public List<DeviceStatus> getAll() {
        List<DeviceStatus> result = deviceStore.findAll().stream()
                .sorted(Comparator
                        .comparing(DeviceStatus::getManufacturer, Comparator.nullsLast(String::compareTo))
                        .thenComparing(DeviceStatus::getModel, Comparator.nullsLast(String::compareTo))
                )
                .toList();

        log.debug("Fetching all devices, count={}", result.size());
        return result;
    }

    /**
     * @param request the request to validate
     * @throws IllegalArgumentException if {@code deviceId} is blank, {@code batteryLevel}
     *                                  is outside 0–100, or {@code timestamp} is not positive
     */
    private void validateRequest(DeviceStatusRequest request) {
        if (request.getDeviceId() == null || request.getDeviceId().isBlank()) {
            log.warn("Invalid request: missing deviceId");
            throw new IllegalArgumentException("Device ID cannot be empty");
        }

        if (request.getBatteryLevel() != null &&
                (request.getBatteryLevel() < 0 || request.getBatteryLevel() > 100)) {
            log.warn("Invalid battery level from device {}: {}",
                    request.getDeviceId(),
                    request.getBatteryLevel());
            throw new IllegalArgumentException("Battery level must be between 0 and 100");
        }

        if (request.getTimestamp() == null || request.getTimestamp() <= 0) {
            log.warn("Invalid timestamp from device {}", request.getDeviceId());
            throw new IllegalArgumentException("Timestamp must be valid");
        }
    }

    /**
     * @param request source DTO
     * @return new {@link DeviceStatus} with fields copied from the request
     */
    private DeviceStatus mapToModel(DeviceStatusRequest request) {
        DeviceStatus status = new DeviceStatus();
        status.setDeviceId(request.getDeviceId());
        status.setManufacturer(request.getManufacturer());
        status.setModel(request.getModel());
        status.setOsVersion(request.getOsVersion());
        status.setBatteryLevel(request.getBatteryLevel());
        status.setCharging(request.getCharging());
        status.setLatitude(request.getLatitude());
        status.setLongitude(request.getLongitude());
        status.setWifiSsid(request.getWifiSsid());
        status.setWifiRssi(request.getWifiRssi());
        status.setTimestamp(request.getTimestamp());
        status.setMissingPermissions(request.getMissingPermissions());
        return status;
    }
}