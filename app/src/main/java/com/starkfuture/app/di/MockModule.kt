package com.starkfuture.app.di

import com.starkfuture.app.data.mock.MockScenarioStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MockModule {
    @Provides
    @Singleton
    fun provideMockScenarioStore(): MockScenarioStore = MockScenarioStore()
}
