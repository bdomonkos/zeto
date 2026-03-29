package com.zeto.app.data.remote;

import com.zeto.app.domain.model.DeviceStatus;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Retrofit interface for the device monitoring REST API.
 * Base URL is provided by {@link com.zeto.app.di.NetworkModule}.
 */
public interface DeviceMonitorApiService {

    /**
     * Posts a device status snapshot to the backend.
     *
     * @param deviceStatus the status snapshot to send
     * @return a Retrofit {@link Call} wrapping the server's response
     */
    @POST("api/status")
    Call<DeviceStatus> sendStatus(@Body DeviceStatus deviceStatus);
}
