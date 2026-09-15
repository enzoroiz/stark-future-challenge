package com.starkfuture.app.data.mapper

import com.starkfuture.app.data.remote.model.BatteryDto
import com.starkfuture.app.data.remote.model.BikeDto
import com.starkfuture.app.data.remote.model.DiagnosticsDto
import com.starkfuture.app.data.remote.model.MotorDto
import com.starkfuture.app.data.remote.model.RideSettingsDto
import com.starkfuture.app.data.remote.model.SessionDto
import com.starkfuture.app.data.remote.model.TelemetryDto
import com.starkfuture.app.data.remote.model.WarningDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TelemetryMapperTest {
    @Test
    fun toDomain_mapsTelemetryDtoToDomainModel() {
        val dto = TelemetryDto(
            bike = BikeDto("Stark VARG MX 1.2", "Alpha", "3.4.1", "https://example.com/bike.webp"),
            timestamp = "2025-05-19T10:32:45Z",
            battery = BatteryDto(73, 38, 34.7, "discharging"),
            motor = MotorDto(52.4, 61.2),
            rideSettings = RideSettingsDto("enduro", 80, 45, 60),
            session = SessionDto(3742, 24.7, 94.1),
            diagnostics = DiagnosticsDto(
                warnings = listOf(WarningDto("W_MOT_TEMP_HIGH", "Motor temperature elevated", "warning"))
            )
        )

        val result = TelemetryMapper.toDomain(dto)

        assertEquals("Stark VARG MX 1.2", result.bike.model)
        assertEquals("Alpha", result.bike.variant)
        assertEquals("3.4.1", result.bike.firmwareVersion)
        assertEquals("https://example.com/bike.webp", result.bike.imageUrl)
        assertEquals("2025-05-19T10:32:45Z", result.timestamp)
        assertEquals(73, result.battery.stateOfChargePct)
        assertEquals(38, result.battery.estimatedRangeKm)
        assertEquals(34.7, result.battery.temperatureC, 0.0)
        assertEquals(52.4, result.motor.powerHp, 0.0)
        assertEquals("enduro", result.rideSettings.powerMap)
        assertEquals(3742, result.session.durationS)
        assertEquals("1:02:22", result.session.durationFormatted)
        assertEquals(24.7, result.session.distanceKm, 0.0)
        assertEquals(1, result.warnings.size)
        assertEquals("W_MOT_TEMP_HIGH", result.warnings.first().code)
    }

    @Test
    fun toDomain_throwsWhenRequiredSectionsAreMissing() {
        val dto = TelemetryDto()

        assertThrows(IllegalArgumentException::class.java) {
            TelemetryMapper.toDomain(dto)
        }
    }
}
