package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    secondary = NeonCyan,
    tertiary = NeonGreen,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = SoftWhite,
    onSecondary = SoftWhite,
    onBackground = SoftWhite,
    onSurface = SoftWhite,
    error = AlertRed
)

private val LightColorScheme = lightColorScheme(
    primary = NeonPurple,
    secondary = NeonCyan,
    tertiary = NeonGreen,
    background = SoftWhite,
    surface = SoftWhite,
    onPrimary = DarkBackground,
    onSecondary = DarkBackground,
    onBackground = DarkBackground,
    onSurface = DarkBackground,
    error = AlertRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default as requested
    dynamicColor: Boolean = false, // Disable dynamic colors by default to preserve the gorgeous glassmorphism branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
