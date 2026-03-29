package com.zeto.app;

import android.app.Application;

import androidx.work.Configuration;
import androidx.hilt.work.HiltWorkerFactory;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import timber.log.Timber;

/**
 * Custom {@link Application} class that initialises Hilt dependency injection
 * and configures WorkManager to use the Hilt-provided {@link HiltWorkerFactory}.
 */
@HiltAndroidApp
public class ZetoApplication extends Application implements Configuration.Provider {

    @Inject
    HiltWorkerFactory workerFactory;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }

    /**
     * Provides a WorkManager {@link Configuration} that uses the Hilt worker factory,
     * enabling {@code @HiltWorker} and {@code @AssistedInject} in worker classes.
     *
     * @return the WorkManager configuration
     */
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build();
    }
}
