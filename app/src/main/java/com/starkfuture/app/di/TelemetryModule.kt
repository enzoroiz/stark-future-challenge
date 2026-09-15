package com.starkfuture.app.di

import com.starkfuture.app.data.mock.MockScenario
import com.starkfuture.app.data.mock.MockScenarioStore
import com.starkfuture.app.data.remote.api.TelemetryApi
import com.starkfuture.app.data.remote.model.TelemetryDto
import com.starkfuture.app.data.repository.TelemetryRepositoryImpl
import com.starkfuture.app.domain.repository.TelemetryRepository
import com.starkfuture.app.presentation.telemetry.TelemetryFixtures
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TelemetryProvidesModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideMockScenarioStore(): MockScenarioStore = MockScenarioStore()

    @Provides
    @Singleton
    fun provideTelemetryApi(
        json: Json,
        scenarioStore: MockScenarioStore
    ): TelemetryApi = object : TelemetryApi {
        override suspend fun getTelemetry(): TelemetryDto = withContext(Dispatchers.IO) {
            delay(2_000)
            val scenario = scenarioStore.currentScenarioForRequest()
            val body = when (scenario) {
                MockScenario.SUCCESS -> TelemetryFixtures.SUCCESS_JSON
                MockScenario.EMPTY -> TelemetryFixtures.EMPTY_JSON
                MockScenario.ERROR -> TelemetryFixtures.ERROR_JSON
            }
            if (scenario == MockScenario.ERROR) {
                throw HttpException(
                    Response.error<TelemetryDto>(
                        500,
                        body.toResponseBody("application/json".toMediaType())
                    )
                )
            }
            json.decodeFromString(TelemetryDto.serializer(), body)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TelemetryBindsModule {
    @Binds
    @Singleton
    abstract fun bindTelemetryRepository(
        repository: TelemetryRepositoryImpl
    ): TelemetryRepository
}
