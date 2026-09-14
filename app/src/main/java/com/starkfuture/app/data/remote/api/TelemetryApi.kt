package com.starkfuture.app.data.remote.api

import com.starkfuture.app.data.remote.model.TelemetryDto
import retrofit2.http.GET

interface TelemetryApi {
    @GET("telemetry")
    suspend fun getTelemetry(): TelemetryDto
}
