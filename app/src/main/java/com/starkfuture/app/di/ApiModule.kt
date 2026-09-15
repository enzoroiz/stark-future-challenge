package com.starkfuture.app.di

import com.starkfuture.app.data.remote.MockTelemetryDataSource
import com.starkfuture.app.data.remote.TelemetryDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ApiModule {
    @Binds
    @Singleton
    abstract fun bindTelemetryDataSource(
        dataSource: MockTelemetryDataSource
    ): TelemetryDataSource
}
