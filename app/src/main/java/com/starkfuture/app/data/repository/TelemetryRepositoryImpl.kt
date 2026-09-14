package com.starkfuture.app.data.repository

import com.starkfuture.app.data.remote.api.TelemetryApi
import com.starkfuture.app.data.remote.model.TelemetryDto
import com.starkfuture.app.data.remote.model.WarningDto
import com.starkfuture.app.domain.model.Battery
import com.starkfuture.app.domain.model.Bike
import com.starkfuture.app.domain.model.Motor
import com.starkfuture.app.domain.model.RideSettings
import com.starkfuture.app.domain.model.Session
import com.starkfuture.app.domain.model.Telemetry
import com.starkfuture.app.domain.model.Warning
import com.starkfuture.app.domain.repository.TelemetryRepository
import com.starkfuture.app.domain.repository.TelemetryResult
import retrofit2.HttpException
import java.io.IOException
import kotlin.requireNotNull

class TelemetryRepositoryImpl(
    private val api: TelemetryApi
) : TelemetryRepository {

    override suspend fun getTelemetry(): TelemetryResult {
        return try {
            val dto = api.getTelemetry()
            if (dto.isEmpty() || dto.bike == null || dto.battery == null || dto.motor == null || dto.rideSettings == null || dto.session == null) {
                TelemetryResult.Empty
            } else {
                TelemetryResult.Success(dto.toDomain())
            }
        } catch (e: HttpException) {
            val message = e.message ?: "Telemetry request failed (${e.code()})."
            TelemetryResult.Error(message)
        } catch (e: IOException) {
            TelemetryResult.Error("Unable to reach the telemetry service.")
        } catch (e: IllegalArgumentException) {
            TelemetryResult.Error("Unexpected telemetry response.")
        } catch (e: Exception) {
            TelemetryResult.Error("Unable to load telemetry.")
        }
    }

    private fun TelemetryDto.toDomain(): Telemetry {
        val bikeDto = requireNotNull(bike)
        val batteryDto = requireNotNull(battery)
        val motorDto = requireNotNull(motor)
        val rideSettingsDto = requireNotNull(rideSettings)
        val sessionDto = requireNotNull(session)
        return Telemetry(
            bike = Bike(
                model = bikeDto.model,
                variant = bikeDto.variant,
                firmwareVersion = bikeDto.firmwareVersion,
                imageUrl = bikeDto.imageUrl
            ),
            timestamp = timestamp.orEmpty(),
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
                distanceKm = sessionDto.distanceKm,
                maxSpeedKmh = sessionDto.maxSpeedKmh
            ),
            warnings = diagnostics.warnings.map { it.toDomain() }
        )
    }

    private fun WarningDto.toDomain(): Warning = Warning(
        code = code,
        message = message,
        severity = severity
    )
}
