package com.zeto.backend.service;

import com.zeto.backend.dto.DeviceStatusRequest;
import com.zeto.backend.model.DeviceStatus;
import com.zeto.backend.repository.DeviceStore;
import com.zeto.backend.repository.InMemoryDeviceStore;
import com.zeto.backend.websocket.DeviceWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceWebSocketHandler wsHandler;

    private DeviceStore deviceStore;

    private DeviceService service;

    @BeforeEach
    void setUp() {
        deviceStore = new InMemoryDeviceStore();
        service = new DeviceService(deviceStore,wsHandler);
    }

    private DeviceStatusRequest validRequest(String deviceId, long timestamp) {
        DeviceStatusRequest req = new DeviceStatusRequest();
        req.setDeviceId(deviceId);
        req.setManufacturer("Acme");
        req.setModel("X1");
        req.setTimestamp(timestamp);
        return req;
    }

    @Test
    void update_validRequest_storesStatusAndBroadcasts() {
        DeviceStatusRequest req = validRequest("device-1", 1000L);

        DeviceStatus result = service.update(req);

        assertThat(result.getDeviceId()).isEqualTo("device-1");
        assertThat(result.getManufacturer()).isEqualTo("Acme");
        verify(wsHandler).broadcast(any(DeviceStatus.class));
    }

    @Test
    void update_newerTimestamp_replacesExistingEntry() {
        service.update(validRequest("device-1", 1000L));
        service.update(validRequest("device-1", 2000L));

        List<DeviceStatus> all = service.getAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getTimestamp()).isEqualTo(2000L);
        verify(wsHandler, times(2)).broadcast(any());
    }

    @Test
    void update_outdatedTimestamp_discardsUpdate() {
        service.update(validRequest("device-1", 2000L));
        service.update(validRequest("device-1", 1000L));

        List<DeviceStatus> all = service.getAll();
        assertThat(all.get(0).getTimestamp()).isEqualTo(2000L);
        verify(wsHandler, times(1)).broadcast(any());
    }

    @Test
    void update_sameTimestamp_discardsUpdate() {
        service.update(validRequest("device-1", 1000L));
        service.update(validRequest("device-1", 1000L));

        verify(wsHandler, times(1)).broadcast(any());
    }

    @Test
    void update_multipleDevices_storesAll() {
        service.update(validRequest("device-1", 1000L));
        service.update(validRequest("device-2", 1000L));

        assertThat(service.getAll()).hasSize(2);
    }

    @Test
    void update_nullDeviceId_throwsIllegalArgument() {
        DeviceStatusRequest req = validRequest(null, 1000L);

        assertThatThrownBy(() -> service.update(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Device ID");
    }

    @Test
    void update_blankDeviceId_throwsIllegalArgument() {
        DeviceStatusRequest req = validRequest("   ", 1000L);

        assertThatThrownBy(() -> service.update(req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_batteryLevelTooHigh_throwsIllegalArgument() {
        DeviceStatusRequest req = validRequest("device-1", 1000L);
        req.setBatteryLevel(101);

        assertThatThrownBy(() -> service.update(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Battery level");
    }

    @Test
    void update_batteryLevelNegative_throwsIllegalArgument() {
        DeviceStatusRequest req = validRequest("device-1", 1000L);
        req.setBatteryLevel(-1);

        assertThatThrownBy(() -> service.update(req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_batteryLevelBoundary_accepted() {
        DeviceStatusRequest req0 = validRequest("device-0", 1000L);
        req0.setBatteryLevel(0);
        DeviceStatusRequest req100 = validRequest("device-100", 1000L);
        req100.setBatteryLevel(100);

        assertThatNoException().isThrownBy(() -> service.update(req0));
        assertThatNoException().isThrownBy(() -> service.update(req100));
    }

    @Test
    void update_nullTimestamp_throwsIllegalArgument() {
        DeviceStatusRequest req = validRequest("device-1", 1000L);
        req.setTimestamp(null);

        assertThatThrownBy(() -> service.update(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Timestamp");
    }

    @Test
    void update_zeroTimestamp_throwsIllegalArgument() {
        DeviceStatusRequest req = validRequest("device-1", 0L);

        assertThatThrownBy(() -> service.update(req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_setsLastSeen() {
        long before = System.currentTimeMillis();
        DeviceStatus result = service.update(validRequest("device-1", 1000L));
        long after = System.currentTimeMillis();

        assertThat(result.getLastSeen()).isBetween(before, after);
    }

    @Test
    void getAll_sortedByManufacturerThenModel() {
        DeviceStatusRequest b = validRequest("d1", 1000L);
        b.setManufacturer("Beta");
        b.setModel("Z");
        DeviceStatusRequest a = validRequest("d2", 1000L);
        a.setManufacturer("Alpha");
        a.setModel("M");
        DeviceStatusRequest aFirst = validRequest("d3", 1000L);
        aFirst.setManufacturer("Alpha");
        aFirst.setModel("A");

        service.update(b);
        service.update(a);
        service.update(aFirst);

        List<DeviceStatus> all = service.getAll();
        assertThat(all).extracting(DeviceStatus::getManufacturer)
                .containsExactly("Alpha", "Alpha", "Beta");
        assertThat(all.get(0).getModel()).isEqualTo("A");
        assertThat(all.get(1).getModel()).isEqualTo("M");
    }

    @Test
    void getAll_emptyStore_returnsEmptyList() {
        assertThat(service.getAll()).isEmpty();
    }

    @Test
    void update_mapsAllFieldsToModel() {
        DeviceStatusRequest req = validRequest("device-1", 5000L);
        req.setOsVersion("34");
        req.setBatteryLevel(80);
        req.setCharging(true);
        req.setLatitude("47.5");
        req.setLongitude("19.0");
        req.setWifiSsid("HomeNet");
        req.setWifiRssi("-55");

        DeviceStatus result = service.update(req);

        assertThat(result.getOsVersion()).isEqualTo("34");
        assertThat(result.getBatteryLevel()).isEqualTo(80);
        assertThat(result.getCharging()).isTrue();
        assertThat(result.getLatitude()).isEqualTo("47.5");
        assertThat(result.getLongitude()).isEqualTo("19.0");
        assertThat(result.getWifiSsid()).isEqualTo("HomeNet");
        assertThat(result.getWifiRssi()).isEqualTo("-55");
        assertThat(result.getTimestamp()).isEqualTo(5000L);
    }
}
