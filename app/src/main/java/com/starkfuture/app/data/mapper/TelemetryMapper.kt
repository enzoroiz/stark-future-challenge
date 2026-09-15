package com.starkfuture.app.data.mapper

import com.starkfuture.app.data.remote.model.TelemetryDto
import com.starkfuture.app.data.remote.model.WarningDto
import com.starkfuture.app.domain.model.Battery
import com.starkfuture.app.domain.model.Bike
import com.starkfuture.app.domain.model.Motor
import com.starkfuture.app.domain.model.RideSettings
import com.starkfuture.app.domain.model.Session
import com.starkfuture.app.domain.model.Telemetry
import com.starkfuture.app.domain.model.Warning
import kotlin.requireNotNull

internal object TelemetryMapper {
    fun toDomain(dto: TelemetryDto): Telemetry {
        val bikeDto = requireNotNull(dto.bike)
        val batteryDto = requireNotNull(dto.battery)
        val motorDto = requireNotNull(dto.motor)
        val rideSettingsDto = requireNotNull(dto.rideSettings)
        val sessionDto = requireNotNull(dto.session)

        return Telemetry(
            bike = Bike(
                model = bikeDto.model,
                variant = bikeDto.variant,
                firmwareVersion = bikeDto.firmwareVersion,
                imageUrl = bikeDto.imageUrl
            ),
            timestamp = dto.timestamp.orEmpty(),
            battery = Battery(
                stateOfChargePct = batteryDto.stateOfChargePct,
                estimatedRangeKm = batteryDto.estimatedRangeKm,
                temperatureC = batteryDto.temperatureC,
                chargingState = batteryDto.chargingState
            ),
            motor = Motor(
                powerHp = motorDto.powerHp,
                temperatureC = motorDto.temperatureC
            ),
            rideSettings = RideSettings(
                powerMap = rideSettingsDto.powerMap,
                maxPowerHp = rideSettingsDto.maxPowerHp,
                engineBrakingPct = rideSettingsDto.engineBrakingPct,
                regenPct = rideSettingsDto.regenPct
            ),
            session = Session(
                durationS = sessionDto.durationS,
                durationFormatted = formatDuration(sessionDto.durationS),
                distanceKm = sessionDto.distanceKm,
                maxSpeedKmh = sessionDto.maxSpeedKmh
            ),
            warnings = dto.diagnostics.warnings.map(::toDomain)
        )
    }

    private fun toDomain(dto: WarningDto): Warning = Warning(
        code = dto.code,
        message = dto.message,
        severity = dto.severity
    )

    private fun formatDuration(durationSeconds: Int): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60
        return "%d:%02d:%02d".format(hours, minutes, seconds)
    }
}
