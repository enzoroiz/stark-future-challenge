package com.starkfuture.app.presentation.telemetry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Battery5Bar
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlinx.coroutines.delay
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

    Box(modifier = Modifier.fillMaxSize()) {
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
                    TelemetryUiState.Loading -> item { Spacer(Modifier.height(1.dp)) }
                    is TelemetryUiState.Success -> item { SuccessState(state.telemetry) }
                    TelemetryUiState.Empty -> item { EmptyState() }
                    is TelemetryUiState.Error -> item { ErrorState(state.message, viewModel::retry) }
                }
            }
        }

        if (uiState is TelemetryUiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
                Text(
                    text = "Loading",
                    color = Color.White,
                    modifier = Modifier.padding(top = 64.dp)
                )
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
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = telemetry.bike.imageUrl,
                    contentDescription = telemetry.bike.model,
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        }
        TelemetryCard(
            title = "Battery",
            primaryIcon = Icons.Rounded.Battery5Bar,
            primary = "${telemetry.battery.stateOfChargePct}%",
            secondaryIcon = Icons.Rounded.Route,
            secondary = "Range ${telemetry.battery.estimatedRangeKm} km"
        )
        TelemetryCard(
            title = "Power",
            primaryIcon = Icons.Rounded.Bolt,
            primary = "${telemetry.motor.powerHp} hp",
            secondaryIcon = Icons.Rounded.Thermostat,
            secondary = "Motor temp ${telemetry.motor.temperatureC}°C"
        )
        TelemetryCard(
            title = "Ride settings",
            primaryIcon = Icons.Rounded.Tune,
            primary = telemetry.rideSettings.powerMap.uppercase(),
            secondaryIcon = Icons.Rounded.Bolt,
            secondary = "Max ${telemetry.rideSettings.maxPowerHp} hp"
        )
        TelemetryCard(
            title = "Session",
            primaryIcon = Icons.Rounded.Timer,
            primary = formatDuration(telemetry.session.durationS),
            secondaryIcon = Icons.Rounded.Route,
            secondary = "${telemetry.session.distanceKm} km"
        )
        WarningsCard(telemetry.warnings)
    }
}

@Composable
private fun TelemetryCard(
    title: String,
    primaryIcon: ImageVector,
    primary: String,
    secondaryIcon: ImageVector,
    secondary: String
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            IconValueRow(
                icon = primaryIcon,
                value = primary,
                valueStyle = MaterialTheme.typography.headlineSmall,
                valueWeight = FontWeight.Bold
            )
            IconValueRow(icon = secondaryIcon, value = secondary)
        }
    }
}

@Composable
private fun WarningsCard(warnings: List<Warning>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            IconValueRow(icon = Icons.Rounded.MonitorHeart, value = "Diagnostics", valueWeight = FontWeight.SemiBold)
            if (warnings.isEmpty()) {
                IconValueRow(icon = Icons.Rounded.CheckCircle, value = "No faults")
                IconValueRow(icon = Icons.Rounded.Circle, value = "Connected")
            } else {
                warnings.forEach { warning ->
                    IconValueRow(icon = Icons.Rounded.Warning, value = "${warning.code} · ${warning.message}")
                }
            }
        }
    }
}

@Composable
private fun IconValueRow(
    icon: ImageVector,
    value: String,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    valueWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(text = value, style = valueStyle, fontWeight = valueWeight)
    }
}

private fun formatDuration(durationSeconds: Int): String {
    val hours = durationSeconds / 3600
    val minutes = (durationSeconds % 3600) / 60
    val seconds = durationSeconds % 60
    return "%d:%02d:%02d".format(hours, minutes, seconds)
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
            delay(2_000)
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
