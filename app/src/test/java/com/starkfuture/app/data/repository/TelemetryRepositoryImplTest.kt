package com.starkfuture.app.data.repository

import com.starkfuture.app.data.mock.MockScenario
import com.starkfuture.app.data.mock.MockScenarioStore
import com.starkfuture.app.data.remote.TelemetryDataSource
import com.starkfuture.app.data.remote.model.BatteryDto
import com.starkfuture.app.data.remote.model.BikeDto
import com.starkfuture.app.data.remote.model.DiagnosticsDto
import com.starkfuture.app.data.remote.model.MotorDto
import com.starkfuture.app.data.remote.model.RideSettingsDto
import com.starkfuture.app.data.remote.model.SessionDto
import com.starkfuture.app.data.remote.model.TelemetryDto
import com.starkfuture.app.domain.repository.TelemetryResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetryRepositoryImplTest {
    @Test
    fun getTelemetry_returnsSuccessWhenDataSourceReturnsValidTelemetry() = runTest {
        val scenarioStore = MockScenarioStore()
        val repository = TelemetryRepositoryImpl(
            dataSource = FakeTelemetryDataSource(validTelemetryDto()),
            scenarioStore = scenarioStore
        )

        val result = repository.getTelemetry()

        assertTrue(result is TelemetryResult.Success)
        val telemetry = (result as TelemetryResult.Success).telemetry
        assertEquals("Stark VARG MX 1.2", telemetry.bike.model)
        assertEquals("1:02:22", telemetry.session.durationFormatted)
    }

    @Test
    fun getTelemetry_returnsEmptyWhenDataSourceReturnsEmptyTelemetry() = runTest {
        val repository = TelemetryRepositoryImpl(
            dataSource = FakeTelemetryDataSource(TelemetryDto()),
            scenarioStore = MockScenarioStore()
        )

        val result = repository.getTelemetry()

        assertEquals(TelemetryResult.Empty, result)
    }

    @Test
    fun getTelemetry_returnsScenarioErrorWithoutCallingDataSource() = runTest {
        val scenarioStore = MockScenarioStore().apply { setScenario(MockScenario.ERROR) }
        val dataSource = RecordingTelemetryDataSource(validTelemetryDto())
        val repository = TelemetryRepositoryImpl(dataSource, scenarioStore)

        val result = repository.getTelemetry()

        assertEquals(TelemetryResult.Error("Telemetry request failed (500)."), result)
        assertEquals(0, dataSource.callCount)
    }

    @Test
    fun getTelemetry_returnsGenericErrorWhenDataSourceFails() = runTest {
        val repository = TelemetryRepositoryImpl(
            dataSource = ThrowingTelemetryDataSource(RuntimeException("boom")),
            scenarioStore = MockScenarioStore()
        )

        val result = repository.getTelemetry()

        assertEquals(TelemetryResult.Error("Unable to load telemetry."), result)
    }

    private fun validTelemetryDto() = TelemetryDto(
        bike = BikeDto("Stark VARG MX 1.2", "Alpha", "3.4.1", "https://example.com/bike.webp"),
        timestamp = "2025-05-19T10:32:45Z",
        battery = BatteryDto(73, 38, 34.7, "discharging"),
        motor = MotorDto(52.4, 61.2),
        rideSettings = RideSettingsDto("enduro", 80, 45, 60),
        session = SessionDto(3742, 24.7, 94.1),
        diagnostics = DiagnosticsDto()
    )
}

private class FakeTelemetryDataSource(
    private val dto: TelemetryDto
) : TelemetryDataSource {
    override suspend fun getTelemetry(): TelemetryDto = dto
}

private class ThrowingTelemetryDataSource(
    private val error: Throwable
) : TelemetryDataSource {
    override suspend fun getTelemetry(): TelemetryDto = throw error
}

private class RecordingTelemetryDataSource(
    private val dto: TelemetryDto
) : TelemetryDataSource {
    var callCount: Int = 0
        private set

    override suspend fun getTelemetry(): TelemetryDto {
        callCount += 1
        return dto
    }
}
