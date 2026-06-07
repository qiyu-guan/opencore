package com.opencore.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TechBlue,
    onPrimary = Color.White,
    primaryContainer = TechBlueDark,
    secondary = TechBlueLight,
    onSecondary = Color.White,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = TechBlue,
    onPrimary = Color.White,
    primaryContainer = TechBlueLight,
    secondary = TechBlue,
    onSecondary = Color.White,
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightCard,
    onBackground = TextLightPrimary,
    onSurface = TextLightPrimary,
    error = ErrorRed
)

@Composable
fun OpenCoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as androidx.activity.ComponentActivity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
