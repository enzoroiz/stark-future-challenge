package com.starkfuture.app.domain.model

data class Telemetry(
    val bike: Bike,
    val timestamp: String,
    val battery: Battery,
    val motor: Motor,
    val rideSettings: RideSettings,
    val session: Session,
    val warnings: List<Warning>
)

data class Bike(
    val model: String,
    val variant: String,
    val firmwareVersion: String,
    val imageUrl: String
)

data class Battery(
    val stateOfChargePct: Int,
    val estimatedRangeKm: Int,
    val temperatureC: Double,
    val chargingState: String
)

data class Motor(
    val powerHp: Double,
    val temperatureC: Double
)

data class RideSettings(
    val powerMap: String,
    val maxPowerHp: Int,
    val engineBrakingPct: Int,
    val regenPct: Int
)

data class Session(
    val durationS: Int,
    val durationFormatted: String,
    val distanceKm: Double,
    val maxSpeedKmh: Double
)

data class Warning(
    val code: String,
    val message: String,
    val severity: String
)
