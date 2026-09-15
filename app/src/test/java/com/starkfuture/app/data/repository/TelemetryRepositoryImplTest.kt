package com.starkfuture.app.data.repository

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
    fun `should return success when data source returns valid telemetry`() = runTest {
        val repository = TelemetryRepositoryImpl(
            dataSource = FakeTelemetryDataSource(validTelemetryDto())
        )

        val result = repository.getTelemetry()

        assertTrue(result is TelemetryResult.Success)
        val telemetry = (result as TelemetryResult.Success).telemetry
        assertEquals("Stark VARG MX 1.2", telemetry.bike.model)
        assertEquals("1:02:22", telemetry.session.durationFormatted)
    }

    @Test
    fun `should return empty when data source returns empty telemetry`() = runTest {
        val repository = TelemetryRepositoryImpl(
            dataSource = FakeTelemetryDataSource(TelemetryDto())
        )

        val result = repository.getTelemetry()

        assertEquals(TelemetryResult.Empty, result)
    }

    @Test
    fun `should return data source error message when data source throws io exception`() = runTest {
        val repository = TelemetryRepositoryImpl(
            dataSource = ThrowingTelemetryDataSource(java.io.IOException("Unable to retrieve bike telemetry."))
        )

        val result = repository.getTelemetry()

        assertEquals(TelemetryResult.Error("Unable to retrieve bike telemetry."), result)
    }

    @Test
    fun `should return generic error when data source throws unexpected exception`() = runTest {
        val repository = TelemetryRepositoryImpl(
            dataSource = ThrowingTelemetryDataSource(RuntimeException("boom"))
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
