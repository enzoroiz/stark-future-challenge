package com.starkfuture.app.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TelemetryDto(
    val bike: BikeDto? = null,
    val timestamp: String? = null,
    val battery: BatteryDto? = null,
    val motor: MotorDto? = null,
    @SerialName("ride_settings")
    val rideSettings: RideSettingsDto? = null,
    val session: SessionDto? = null,
    val diagnostics: DiagnosticsDto = DiagnosticsDto()
) {
    fun isEmpty(): Boolean =
        bike == null && battery == null && motor == null && rideSettings == null && session == null && diagnostics.warnings.isEmpty()
}

@Serializable
data class BikeDto(
    val model: String = "",
    val variant: String = "",
    @SerialName("firmware_version")
    val firmwareVersion: String = "",
    @SerialName("image_url")
    val imageUrl: String = ""
)

@Serializable
data class BatteryDto(
    @SerialName("state_of_charge_pct")
    val stateOfChargePct: Int = 0,
    @SerialName("estimated_range_km")
    val estimatedRangeKm: Int = 0,
    @SerialName("temperature_c")
    val temperatureC: Double = 0.0,
    @SerialName("charging_state")
    val chargingState: String = ""
)

@Serializable
data class MotorDto(
    @SerialName("power_hp")
    val powerHp: Double = 0.0,
    @SerialName("temperature_c")
    val temperatureC: Double = 0.0
)

@Serializable
data class RideSettingsDto(
    @SerialName("power_map")
    val powerMap: String = "",
    @SerialName("max_power_hp")
    val maxPowerHp: Int = 0,
    @SerialName("engine_braking_pct")
    val engineBrakingPct: Int = 0,
    @SerialName("regen_pct")
    val regenPct: Int = 0
)

@Serializable
data class SessionDto(
    @SerialName("duration_s")
    val durationS: Int = 0,
    @SerialName("distance_km")
    val distanceKm: Double = 0.0,
    @SerialName("max_speed_kmh")
    val maxSpeedKmh: Double = 0.0
)

@Serializable
data class DiagnosticsDto(
    @SerialName("fault_codes")
    val faultCodes: List<String> = emptyList(),
    val warnings: List<WarningDto> = emptyList()
)

@Serializable
data class WarningDto(
    val code: String = "",
    val message: String = "",
    val severity: String = ""
)

@Serializable
data class ErrorBodyDto(
    val error: ErrorPayloadDto? = null
)

@Serializable
data class ErrorPayloadDto(
    val code: String = "",
    val message: String = ""
)
