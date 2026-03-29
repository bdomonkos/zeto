package com.zeto.backend.controller;

import com.zeto.backend.dto.DeviceStatusRequest;
import com.zeto.backend.model.DeviceStatus;
import com.zeto.backend.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for device status management.
 * Base path: {@code /api}
 */
@RestController
@RequestMapping("/api")
public class DeviceController {

    private final DeviceService service;

    /**
     * @param service device business logic
     */
    public DeviceController(DeviceService service) {
        this.service = service;
    }

    /**
     * Accepts a status update from a device.
     *
     * @param request device status payload
     * @return the stored {@link DeviceStatus} with HTTP 200
     */
    @PostMapping("/status")
    public ResponseEntity<DeviceStatus> updateStatus(@Valid @RequestBody DeviceStatusRequest request) {
        DeviceStatus updated = service.update(request);
        return ResponseEntity.ok(updated);
    }

    /**
     * @return all known devices sorted by manufacturer and model, HTTP 200
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/devices")
    public ResponseEntity<List<DeviceStatus>> getDevices() {
        return ResponseEntity.ok(service.getAll());
    }
}
