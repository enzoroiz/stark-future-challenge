package com.starkfuture.app.presentation.telemetry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.starkfuture.app.data.mock.MockScenario
import com.starkfuture.app.data.mock.MockScenarioStore
import com.starkfuture.app.data.remote.api.TelemetryApi
import com.starkfuture.app.data.remote.model.TelemetryDto
import com.starkfuture.app.data.repository.TelemetryRepositoryImpl
import com.starkfuture.app.domain.model.Telemetry
import com.starkfuture.app.domain.model.Warning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

@Composable
fun TelemetryScreen() {
    val viewModel = viewModel<TelemetryViewModel>(
        factory = TelemetryViewModelFactory(
            LocalTelemetryDependencies.repository,
            LocalTelemetryDependencies.scenarioStore
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val selectedScenario by viewModel.selectedScenario.collectAsState()

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Demo scenario", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MockScenario.entries.forEach { scenario ->
                        FilterChip(
                                    selected = selectedScenario == scenario,
                                    onClick = { viewModel.onScenarioSelected(scenario) },
                            label = { Text(scenario.name.lowercase().replaceFirstChar { it.titlecase() }) }
                        )
                    }
                }
            }

            when (val state = uiState) {
                TelemetryUiState.Loading -> item { Text("Loading telemetry…") }
                is TelemetryUiState.Success -> item { SuccessState(state.telemetry) }
                TelemetryUiState.Empty -> item { EmptyState() }
                is TelemetryUiState.Error -> item { ErrorState(state.message, viewModel::retry) }
            }
        }
    }
}

@Composable
private fun SuccessState(telemetry: Telemetry) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(telemetry.bike.model, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${telemetry.bike.variant} • Firmware ${telemetry.bike.firmwareVersion}")
                if (telemetry.bike.imageUrl.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    AsyncImage(
                        model = telemetry.bike.imageUrl,
                        contentDescription = telemetry.bike.model,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        TelemetryCard("Battery", "${telemetry.battery.stateOfChargePct}%", "Range ${telemetry.battery.estimatedRangeKm} km")
        TelemetryCard("Power", "${telemetry.motor.powerHp} hp", "Motor temp ${telemetry.motor.temperatureC}°C")
        TelemetryCard("Ride settings", telemetry.rideSettings.powerMap, "Max ${telemetry.rideSettings.maxPowerHp} hp")
        TelemetryCard("Session", "${telemetry.session.durationS / 60} min", "${telemetry.session.distanceKm} km")
        WarningsCard(telemetry.warnings)
    }
}

@Composable
private fun TelemetryCard(title: String, primary: String, secondary: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(primary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(secondary)
        }
    }
}

@Composable
private fun WarningsCard(warnings: List<Warning>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Warnings", style = MaterialTheme.typography.titleMedium)
            if (warnings.isEmpty()) {
                Text("No active warnings")
            } else {
                warnings.forEach { warning ->
                    Text("${warning.code} · ${warning.message}")
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("No telemetry available")
        }
    }
}

@Composable
private fun ErrorState(message: String, retry: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Telemetry unavailable")
            Text(message)
            OutlinedButton(onClick = retry) { Text("Retry") }
        }
    }
}

private object LocalTelemetryDependencies {
    val scenarioStore = MockScenarioStore()
    val api = object : TelemetryApi {
        private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
        override suspend fun getTelemetry(): TelemetryDto = withContext(Dispatchers.IO) {
            val scenario = scenarioStore.currentScenarioForRequest()
            val body = when (scenario) {
                MockScenario.SUCCESS -> SUCCESS_JSON
                MockScenario.EMPTY -> EMPTY_JSON
                MockScenario.ERROR -> ERROR_JSON
            }
            if (scenario == MockScenario.ERROR) {
                throw HttpException(Response.error<TelemetryDto>(500, body.toResponseBody()))
            }
            return@withContext json.decodeFromString(TelemetryDto.serializer(), body)
        }
    }
    val repository = TelemetryRepositoryImpl(api)
}

private const val SUCCESS_JSON = """
{
  "bike": {
    "model": "Stark VARG MX 1.2",
    "variant": "Alpha",
    "firmware_version": "3.4.1",
    "image_url": "https://assets.starkfuture.com/frontend-assets/mx-product-images/SMX1_side_stand_red_handbrake_enduro18_nosidestand.webp"
  },
  "timestamp": "2025-05-19T10:32:45Z",
  "battery": {
    "state_of_charge_pct": 73,
    "estimated_range_km": 38,
    "temperature_c": 34.7,
    "charging_state": "discharging"
  },
  "motor": {
    "power_hp": 52.4,
    "temperature_c": 61.2
  },
  "ride_settings": {
    "power_map": "enduro",
    "max_power_hp": 80,
    "engine_braking_pct": 45,
    "regen_pct": 60
  },
  "session": {
    "duration_s": 3742,
    "distance_km": 24.7,
    "max_speed_kmh": 94.1
  },
  "diagnostics": {
    "fault_codes": [],
    "warnings": [
      {
        "code": "W_MOT_TEMP_HIGH",
        "message": "Motor temperature elevated",
        "severity": "warning"
      }
    ]
  }
}
"""

private const val EMPTY_JSON = """
{
  "bike": null,
  "timestamp": null,
  "battery": null,
  "motor": null,
  "ride_settings": null,
  "session": null,
  "diagnostics": {
    "fault_codes": [],
    "warnings": []
  }
}
"""

private const val ERROR_JSON = """
{
  "error": {
    "code": "MOCK_TELEMETRY_ERROR",
    "message": "Unable to retrieve bike telemetry."
  }
}
"""
