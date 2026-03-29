package com.zeto.backend.controller;

import com.zeto.backend.model.DeviceStatus;
import com.zeto.backend.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceService deviceService;

    private static final String VALID_REQUEST = """
            {
              "deviceId": "abc123",
              "manufacturer": "Acme",
              "model": "X1",
              "timestamp": 1000
            }
            """;

    @Test
    void postStatus_validRequest_returns200() throws Exception {
        DeviceStatus status = new DeviceStatus();
        status.setDeviceId("abc123");
        when(deviceService.update(any())).thenReturn(status);

        mockMvc.perform(post("/api/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("abc123"));
    }

    @Test
    void postStatus_missingDeviceId_returns400() throws Exception {
        String body = """
                {
                  "manufacturer": "Acme",
                  "model": "X1",
                  "timestamp": 1000
                }
                """;

        mockMvc.perform(post("/api/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postStatus_missingManufacturer_returns400() throws Exception {
        String body = """
                {
                  "deviceId": "abc123",
                  "model": "X1",
                  "timestamp": 1000
                }
                """;

        mockMvc.perform(post("/api/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postStatus_missingTimestamp_returns400() throws Exception {
        String body = """
                {
                  "deviceId": "abc123",
                  "manufacturer": "Acme",
                  "model": "X1"
                }
                """;

        mockMvc.perform(post("/api/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postStatus_serviceThrowsIllegalArgument_returns400() throws Exception {
        when(deviceService.update(any()))
                .thenThrow(new IllegalArgumentException("Battery level must be between 0 and 100"));

        mockMvc.perform(post("/api/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Battery level must be between 0 and 100"));
    }

    @Test
    void getDevices_returnsListFromService() throws Exception {
        DeviceStatus s1 = new DeviceStatus();
        s1.setDeviceId("d1");
        s1.setManufacturer("Alpha");
        DeviceStatus s2 = new DeviceStatus();
        s2.setDeviceId("d2");
        s2.setManufacturer("Beta");

        when(deviceService.getAll()).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].deviceId").value("d1"))
                .andExpect(jsonPath("$[1].deviceId").value("d2"));
    }

    @Test
    void getDevices_emptyList_returns200WithEmptyArray() throws Exception {
        when(deviceService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void postStatus_validationError_responseContainsFieldErrors() throws Exception {
        String body = """
                {
                  "manufacturer": "Acme",
                  "model": "X1",
                  "timestamp": 1000
                }
                """;

        mockMvc.perform(post("/api/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.deviceId").exists());
    }
}
