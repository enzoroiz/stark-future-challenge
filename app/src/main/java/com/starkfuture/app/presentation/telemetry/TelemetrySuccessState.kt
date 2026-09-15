package com.starkfuture.app.presentation.telemetry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Battery5Bar
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.starkfuture.app.domain.model.Telemetry
import com.starkfuture.app.ui.theme.DarkSurface
import com.starkfuture.app.ui.theme.DarkTextPrimary

@Composable
fun SuccessState(telemetry: Telemetry) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = DarkSurface,
                contentColor = DarkTextPrimary
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    telemetry.bike.model,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text("${telemetry.bike.variant} • Firmware ${telemetry.bike.firmwareVersion}")
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = telemetry.bike.imageUrl,
                    contentDescription = telemetry.bike.model,
                    modifier = Modifier.fillMaxWidth(),
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
            primary = telemetry.session.durationFormatted,
            secondaryIcon = Icons.Rounded.Route,
            secondary = "${telemetry.session.distanceKm} km"
        )
        WarningsCard(telemetry.warnings)
    }
}
