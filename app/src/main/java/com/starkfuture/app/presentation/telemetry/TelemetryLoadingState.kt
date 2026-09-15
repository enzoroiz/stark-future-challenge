package com.starkfuture.app.presentation.telemetry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.starkfuture.app.ui.theme.DarkOverlay
import com.starkfuture.app.ui.theme.DarkTextPrimary

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkOverlay),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = DarkTextPrimary)
        Text(
            text = "Loading",
            color = DarkTextPrimary,
            modifier = Modifier.padding(top = 64.dp)
        )
    }
}
