package com.starkfuture.app.data.remote

import com.starkfuture.app.data.mock.MockScenario
import com.starkfuture.app.data.mock.MockScenarioStore
import com.starkfuture.app.data.remote.model.MockTelemetryPayloads
import com.starkfuture.app.data.remote.model.TelemetryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class MockTelemetryDataSource @Inject constructor(
    private val json: Json,
    private val scenarioStore: MockScenarioStore
) : TelemetryDataSource {
    override suspend fun getTelemetry(): TelemetryDto = withContext(Dispatchers.IO) {
        delay(2_000)
        val body = when (scenarioStore.currentScenarioForRequest()) {
            MockScenario.SUCCESS -> MockTelemetryPayloads.SUCCESS_JSON
            MockScenario.EMPTY -> MockTelemetryPayloads.EMPTY_JSON
            MockScenario.ERROR -> MockTelemetryPayloads.ERROR_JSON
        }
        json.decodeFromString(TelemetryDto.serializer(), body)
    }
}
