package com.starkfuture.app.data.remote

import com.starkfuture.app.data.mock.MockScenario
import com.starkfuture.app.data.mock.MockScenarioStore
import com.starkfuture.app.data.remote.model.ErrorBodyDto
import com.starkfuture.app.data.remote.model.MockTelemetryPayloads
import com.starkfuture.app.data.remote.model.TelemetryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class MockTelemetryDataSource @Inject constructor(
    private val json: Json,
    private val scenarioStore: MockScenarioStore
) : TelemetryDataSource {
    override suspend fun getTelemetry(): TelemetryDto = withContext(Dispatchers.IO) {
        // Simulates a network delay before returning mock telemetry data.
        delay(1.seconds)

        val scenario = scenarioStore.currentScenarioForRequest()
        val body = when (scenario) {
            MockScenario.SUCCESS -> MockTelemetryPayloads.SUCCESS_JSON
            MockScenario.EMPTY -> MockTelemetryPayloads.EMPTY_JSON
            MockScenario.ERROR -> MockTelemetryPayloads.ERROR_JSON
        }

        if (scenario == MockScenario.ERROR) {
            val errorBody = json.decodeFromString(ErrorBodyDto.serializer(), body)
            throw IOException(errorBody.error?.message ?: "Unable to load telemetry.")
        }

        json.decodeFromString(TelemetryDto.serializer(), body)
    }
}
