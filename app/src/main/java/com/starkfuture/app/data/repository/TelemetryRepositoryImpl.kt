package com.starkfuture.app.data.repository

import com.starkfuture.app.data.mapper.TelemetryMapper
import com.starkfuture.app.data.remote.TelemetryDataSource
import com.starkfuture.app.domain.repository.TelemetryRepository
import com.starkfuture.app.domain.repository.TelemetryResult
import java.io.IOException
import javax.inject.Inject

class TelemetryRepositoryImpl @Inject constructor(
    private val dataSource: TelemetryDataSource
) : TelemetryRepository {

    override suspend fun getTelemetry(): TelemetryResult {
        return try {
            val dto = dataSource.getTelemetry()
            if (dto.isEmpty() || dto.bike == null || dto.battery == null || dto.motor == null || dto.rideSettings == null || dto.session == null) {
                TelemetryResult.Empty
            } else {
                TelemetryResult.Success(TelemetryMapper.toDomain(dto))
            }
        } catch (e: IOException) {
            TelemetryResult.Error(e.message ?: "Unable to load telemetry.")
        } catch (e: IllegalArgumentException) {
            TelemetryResult.Error("Unexpected telemetry response.")
        } catch (e: Exception) {
            TelemetryResult.Error("Unable to load telemetry.")
        }
    }
}
