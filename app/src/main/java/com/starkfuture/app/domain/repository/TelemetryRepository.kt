package com.starkfuture.app.domain.repository

import com.starkfuture.app.domain.model.Telemetry

interface TelemetryRepository {
    suspend fun getTelemetry(): TelemetryResult
}

sealed interface TelemetryResult {
    data class Success(val telemetry: Telemetry) : TelemetryResult
    data object Empty : TelemetryResult
    data class Error(val message: String) : TelemetryResult
}
