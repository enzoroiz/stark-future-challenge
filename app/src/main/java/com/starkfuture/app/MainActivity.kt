package com.starkfuture.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.starkfuture.app.presentation.telemetry.TelemetryScreen
import com.starkfuture.app.ui.theme.StarkFutureTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StarkFutureTheme {
                TelemetryScreen()
            }
        }
    }
}