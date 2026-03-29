package com.zeto.app.di;

import com.zeto.app.domain.repository.DeviceDataRepositoryImpl;
import com.zeto.app.domain.repository.DeviceDataRepository;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;

/**
 * Hilt module binding {@link DeviceDataRepository} to {@link DeviceDataRepositoryImpl}.
 */
@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {

    /**
     * @param impl the implementation to bind
     * @return the {@link DeviceDataRepository} interface backed by the impl
     */
    @Binds
    @Singleton
    public abstract DeviceDataRepository bindDeviceDataRepository(
            DeviceDataRepositoryImpl impl
    );
}
