package com.starkfuture.app.data.repository

import com.starkfuture.app.data.mapper.TelemetryMapper
import com.starkfuture.app.data.remote.api.TelemetryApi
import com.starkfuture.app.domain.repository.TelemetryRepository
import com.starkfuture.app.domain.repository.TelemetryResult
import retrofit2.HttpException
import java.io.IOException

class TelemetryRepositoryImpl(
    private val api: TelemetryApi
) : TelemetryRepository {

    override suspend fun getTelemetry(): TelemetryResult {
        return try {
            val dto = api.getTelemetry()
            if (dto.isEmpty() || dto.bike == null || dto.battery == null || dto.motor == null || dto.rideSettings == null || dto.session == null) {
                TelemetryResult.Empty
            } else {
                TelemetryResult.Success(TelemetryMapper.toDomain(dto))
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
}
