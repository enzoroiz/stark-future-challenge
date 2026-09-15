package com.starkfuture.app.data.repository

import com.starkfuture.app.data.mapper.TelemetryMapper
import com.starkfuture.app.data.remote.TelemetryDataSource
import com.starkfuture.app.data.mock.MockScenario
import com.starkfuture.app.data.mock.MockScenarioStore
import com.starkfuture.app.domain.repository.TelemetryRepository
import com.starkfuture.app.domain.repository.TelemetryResult
import javax.inject.Inject

class TelemetryRepositoryImpl @Inject constructor(
    private val dataSource: TelemetryDataSource,
    private val scenarioStore: MockScenarioStore
) : TelemetryRepository {

    override suspend fun getTelemetry(): TelemetryResult {
        return try {
            if (scenarioStore.currentScenarioForRequest() == MockScenario.ERROR) {
                return TelemetryResult.Error("Telemetry request failed (500).")
            }
            val dto = dataSource.getTelemetry()
            if (dto.isEmpty() || dto.bike == null || dto.battery == null || dto.motor == null || dto.rideSettings == null || dto.session == null) {
                TelemetryResult.Empty
            } else {
                TelemetryResult.Success(TelemetryMapper.toDomain(dto))
            }
        } catch (e: IllegalArgumentException) {
            TelemetryResult.Error("Unexpected telemetry response.")
        } catch (e: Exception) {
            TelemetryResult.Error("Unable to load telemetry.")
        }
    }
}
