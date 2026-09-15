package com.starkfuture.app.data.remote

import com.starkfuture.app.data.remote.model.TelemetryDto

interface TelemetryDataSource {
    suspend fun getTelemetry(): TelemetryDto
}
