package com.zeto.app.domain.usecase;

import com.zeto.app.data.sensor.BatteryCollector;
import com.zeto.app.data.sensor.DeviceInfoCollector;
import com.zeto.app.data.sensor.LocationCollector;
import com.zeto.app.data.sensor.WifiCollector;
import com.zeto.app.domain.model.DeviceStatus;
import com.zeto.app.domain.model.PermissionStatus;
import com.zeto.app.domain.repository.DeviceDataRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * Pulls data from all sensor collectors, builds a {@link DeviceStatus} snapshot,
 * and hands it to the repository. Location is async, so the payload goes out
 * inside the location callback once coordinates arrive.
 */
@Singleton
public class CollectDeviceDataUseCase {

    private final DeviceInfoCollector deviceInfoCollector;
    private final BatteryCollector batteryCollector;
    private final WifiCollector wifiCollector;
    private final LocationCollector locationCollector;
    private final DeviceDataRepository repository;

    /**
     * @param deviceInfoCollector hardware/OS info
     * @param batteryCollector    battery level and charging state
     * @param wifiCollector       Wi-Fi SSID and RSSI
     * @param locationCollector   GPS coordinates (async)
     * @param repository          sends the assembled status to the backend
     */
    @Inject
    public CollectDeviceDataUseCase(
            DeviceInfoCollector deviceInfoCollector,
            BatteryCollector batteryCollector,
            WifiCollector wifiCollector,
            LocationCollector locationCollector,
            DeviceDataRepository repository
    ) {
        this.deviceInfoCollector = deviceInfoCollector;
        this.batteryCollector = batteryCollector;
        this.wifiCollector = wifiCollector;
        this.locationCollector = locationCollector;
        this.repository = repository;
    }

    /**
     * Gathers all sensor data and sends it to the backend.
     * Sync reads run first; the payload goes out once the location callback fires.
     *
     * @param permissionStatus current runtime permission state
     */
    public void execute(PermissionStatus permissionStatus) {
        Timber.d("Collecting device data...");

        String deviceId = deviceInfoCollector.getDeviceId();
        String manufacturer = deviceInfoCollector.getManufacturer();
        String model = deviceInfoCollector.getModel();
        String osVersion = deviceInfoCollector.getOsVersion();
        String batteryLevel = batteryCollector.getBatteryLevel();
        String charging = batteryCollector.isCharging();
        String wifiSsid = wifiCollector.getWifiSsid(permissionStatus);
        String wifiRssi = wifiCollector.getWifiRssi(permissionStatus);

        locationCollector.getLocation(permissionStatus, (latitude, longitude) -> {
            DeviceStatus deviceStatus = DeviceStatus.builder()
                    .deviceId(deviceId)
                    .manufacturer(manufacturer)
                    .model(model)
                    .osVersion(osVersion)
                    .batteryLevel(batteryLevel)
                    .charging(charging)
                    .latitude(latitude)
                    .longitude(longitude)
                    .wifiSsid(wifiSsid)
                    .wifiRssi(wifiRssi)
                    .timestamp(System.currentTimeMillis())
                    .build();

            Timber.d("Device data collected, sending to backend...");
            repository.sendDeviceStatus(deviceStatus);
        });
    }
}
