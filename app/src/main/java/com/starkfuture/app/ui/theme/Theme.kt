package com.starkfuture.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF2F2F2),
    secondary = Color(0xFFBDBDBD),
    tertiary = Color(0xFF8A8A8A),
    background = Color(0xFF050505),
    surface = Color(0xFF101010),
    surfaceVariant = Color(0xFF181818),
    onPrimary = Color(0xFF050505),
    onSecondary = Color(0xFF050505),
    onTertiary = Color(0xFF050505),
    onBackground = Color(0xFFF2F2F2),
    onSurface = Color(0xFFF2F2F2),
    onSurfaceVariant = Color(0xFFD0D0D0)
)

@Composable
fun StarkFutureTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}