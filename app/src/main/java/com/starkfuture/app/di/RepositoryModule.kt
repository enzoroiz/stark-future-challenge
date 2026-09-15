package com.starkfuture.app.di

import com.starkfuture.app.data.repository.TelemetryRepositoryImpl
import com.starkfuture.app.domain.repository.TelemetryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTelemetryRepository(
        repository: TelemetryRepositoryImpl
    ): TelemetryRepository
}
