package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = Color(0xFF041C10),
    primaryContainer = Color(0xFF0D2E1D),
    onPrimaryContainer = NeonGreen,
    secondary = NeonCyan,
    onSecondary = Color(0xFF002229),
    secondaryContainer = Color(0xFF003844),
    onSecondaryContainer = NeonCyan,
    tertiary = NeonYellow,
    background = GamingBackground,
    onBackground = TextPrimary,
    surface = GamingSurface,
    onSurface = TextPrimary,
    surfaceVariant = GamingSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = NeonGreen.copy(alpha = 0.4f),
    outlineVariant = Color(0xFF1E3A2C)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force futuristic dark gaming theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
