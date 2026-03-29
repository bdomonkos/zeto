package com.zeto.app.di;

import com.zeto.app.data.remote.DeviceMonitorApiService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Hilt module wiring up the singleton network stack:
 * {@link OkHttpClient}, {@link Retrofit}, and {@link DeviceMonitorApiService}.
 */
@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    private static final String BASE_URL = "http://127.0.0.1:8080/";

    /**
     * @return {@link OkHttpClient} with body-level logging routed through Timber
     */
    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message ->
                timber.log.Timber.tag("OkHttp").d(message)
        );
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
    }

    /**
     * @param okHttpClient the HTTP client to use
     * @return {@link Retrofit} pointed at {@value BASE_URL} with Gson converter
     */
    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * @param retrofit the configured Retrofit instance
     * @return the generated {@link DeviceMonitorApiService} implementation
     */
    @Provides
    @Singleton
    public DeviceMonitorApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(DeviceMonitorApiService.class);
    }
}
