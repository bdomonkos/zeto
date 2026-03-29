package com.zeto.app.domain.usecase;

import com.zeto.app.data.sensor.BatteryCollector;
import com.zeto.app.data.sensor.DeviceInfoCollector;
import com.zeto.app.data.sensor.LocationCollector;
import com.zeto.app.data.sensor.WifiCollector;
import com.zeto.app.domain.model.DeviceStatus;
import com.zeto.app.domain.model.PermissionStatus;
import com.zeto.app.domain.repository.DeviceDataRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CollectDeviceDataUseCaseTest {

    @Mock private DeviceInfoCollector deviceInfoCollector;
    @Mock private BatteryCollector batteryCollector;
    @Mock private WifiCollector wifiCollector;
    @Mock private LocationCollector locationCollector;
    @Mock private DeviceDataRepository repository;

    private CollectDeviceDataUseCase useCase;

    @Before
    public void setUp() {
        useCase = new CollectDeviceDataUseCase(
                deviceInfoCollector, batteryCollector, wifiCollector,
                locationCollector, repository
        );
    }

    private void stubSensors(String deviceId, String battery, String charging,
                              String ssid, String rssi) {
        when(deviceInfoCollector.getDeviceId()).thenReturn(deviceId);
        when(deviceInfoCollector.getManufacturer()).thenReturn("Acme");
        when(deviceInfoCollector.getModel()).thenReturn("X1");
        when(deviceInfoCollector.getOsVersion()).thenReturn("34");
        when(batteryCollector.getBatteryLevel()).thenReturn(battery);
        when(batteryCollector.isCharging()).thenReturn(charging);
        when(wifiCollector.getWifiSsid(any())).thenReturn(ssid);
        when(wifiCollector.getWifiRssi(any())).thenReturn(rssi);
    }

    /** Simulates LocationCollector calling the callback synchronously. */
    private void stubLocation(String lat, String lon) {
        doAnswer(invocation -> {
            LocationCollector.LocationCallback cb = invocation.getArgument(1);
            cb.onResult(lat, lon);
            return null;
        }).when(locationCollector).getLocation(any(), any());
    }

    @Test
    public void execute_allPermissionsGranted_sendsCompleteStatus() {
        PermissionStatus permissions = new PermissionStatus(true, true);
        stubSensors("device-1", "85", "true", "HomeNet", "-60");
        stubLocation("47.5", "19.0");

        useCase.execute(permissions);

        ArgumentCaptor<DeviceStatus> captor = ArgumentCaptor.forClass(DeviceStatus.class);
        verify(repository).sendDeviceStatus(captor.capture());

        DeviceStatus sent = captor.getValue();
        assertEquals("device-1", sent.getDeviceId());
        assertEquals("Acme", sent.getManufacturer());
        assertEquals("X1", sent.getModel());
        assertEquals("34", sent.getOsVersion());
        assertEquals("85", sent.getBatteryLevel());
        assertEquals("true", sent.getCharging());
        assertEquals("HomeNet", sent.getWifiSsid());
        assertEquals("-60", sent.getWifiRssi());
        assertEquals("47.5", sent.getLatitude());
        assertEquals("19.0", sent.getLongitude());
        assertTrue(sent.getTimestamp() > 0);
    }

    @Test
    public void execute_locationDenied_sendsPermissionDeniedCoordinates() {
        PermissionStatus permissions = new PermissionStatus(false, false);
        stubSensors("device-2", "50", "false",
                PermissionStatus.PERMISSION_DENIED, PermissionStatus.PERMISSION_DENIED);
        stubLocation(PermissionStatus.PERMISSION_DENIED, PermissionStatus.PERMISSION_DENIED);

        useCase.execute(permissions);

        ArgumentCaptor<DeviceStatus> captor = ArgumentCaptor.forClass(DeviceStatus.class);
        verify(repository).sendDeviceStatus(captor.capture());

        DeviceStatus sent = captor.getValue();
        assertEquals(PermissionStatus.PERMISSION_DENIED, sent.getLatitude());
        assertEquals(PermissionStatus.PERMISSION_DENIED, sent.getLongitude());
    }

    @Test
    public void execute_unknownBattery_sendsUnknownBatteryLevel() {
        PermissionStatus permissions = new PermissionStatus(true, true);
        stubSensors("device-3", "UNKNOWN", "UNKNOWN", "Net", "-70");
        stubLocation("0.0", "0.0");

        useCase.execute(permissions);

        ArgumentCaptor<DeviceStatus> captor = ArgumentCaptor.forClass(DeviceStatus.class);
        verify(repository).sendDeviceStatus(captor.capture());

        assertEquals("UNKNOWN", captor.getValue().getBatteryLevel());
        assertEquals("UNKNOWN", captor.getValue().getCharging());
    }

    @Test
    public void execute_wifiDisabled_sendsWifiDisabledSsid() {
        PermissionStatus permissions = new PermissionStatus(true, true);
        stubSensors("device-4", "70", "false", "WIFI_DISABLED", "WIFI_DISABLED");
        stubLocation("1.0", "2.0");

        useCase.execute(permissions);

        ArgumentCaptor<DeviceStatus> captor = ArgumentCaptor.forClass(DeviceStatus.class);
        verify(repository).sendDeviceStatus(captor.capture());

        assertEquals("WIFI_DISABLED", captor.getValue().getWifiSsid());
    }

    @Test
    public void execute_callsAllCollectors() {
        PermissionStatus permissions = new PermissionStatus(true, true);
        stubSensors("device-5", "60", "true", "Net", "-65");
        stubLocation("10.0", "20.0");

        useCase.execute(permissions);

        verify(deviceInfoCollector).getDeviceId();
        verify(deviceInfoCollector).getManufacturer();
        verify(deviceInfoCollector).getModel();
        verify(deviceInfoCollector).getOsVersion();
        verify(batteryCollector).getBatteryLevel();
        verify(batteryCollector).isCharging();
        verify(wifiCollector).getWifiSsid(permissions);
        verify(wifiCollector).getWifiRssi(permissions);
        verify(locationCollector).getLocation(eq(permissions), any());
    }

    @Test
    public void execute_repositoryCalledExactlyOnce() {
        PermissionStatus permissions = new PermissionStatus(true, true);
        stubSensors("device-6", "90", "true", "Net", "-50");
        stubLocation("5.0", "6.0");

        useCase.execute(permissions);

        verify(repository, times(1)).sendDeviceStatus(any());
    }
}
