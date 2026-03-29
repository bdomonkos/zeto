package com.zeto.app.domain.repository;

import com.zeto.app.data.remote.DeviceMonitorApiService;
import com.zeto.app.domain.model.DeviceStatus;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Sends device status snapshots to the backend over HTTP via Retrofit.
 * Calls are async; failures are logged but not retried.
 */
@Singleton
public class DeviceDataRepositoryImpl implements DeviceDataRepository {

    private final DeviceMonitorApiService apiService;

    /**
     * @param apiService Retrofit service for the backend API
     */
    @Inject
    public DeviceDataRepositoryImpl(DeviceMonitorApiService apiService) {
        this.apiService = apiService;
    }

    /**
     * {@inheritDoc}
     * The HTTP call is enqueued asynchronously on Retrofit's background thread.
     */
    @Override
    public void sendDeviceStatus(DeviceStatus deviceStatus) {
        Timber.d("Sending device status to backend: %s", deviceStatus.getDeviceId());
        apiService.sendStatus(deviceStatus).enqueue(new Callback<DeviceStatus>() {
            @Override
            public void onResponse(Call<DeviceStatus> call, Response<DeviceStatus> response) {
                if (response.isSuccessful()) {
                    Timber.d("Device status sent successfully");
                } else {
                    Timber.w("Failed to send device status, response code: %d", response.code());
                }
            }

            @Override
            public void onFailure(Call<DeviceStatus> call, Throwable t) {
                Timber.e(t, "Network error sending device status");
            }
        });
    }
}
