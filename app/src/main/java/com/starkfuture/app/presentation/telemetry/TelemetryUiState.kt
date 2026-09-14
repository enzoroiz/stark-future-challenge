package com.starkfuture.app.presentation.telemetry

import com.starkfuture.app.domain.model.Telemetry

sealed interface TelemetryUiState {
    data object Loading : TelemetryUiState
    data class Success(val telemetry: Telemetry) : TelemetryUiState
    data object Empty : TelemetryUiState
    data class Error(val message: String) : TelemetryUiState
}
