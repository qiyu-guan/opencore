package com.opencore.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun OpenCoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    primaryColor: Color = DefaultPrimary,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = DefaultOnPrimary,
            background = DarkBg,
            surface = DarkSurface,
            onBackground = TextPrimary,
            onSurface = TextPrimary
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = DefaultOnPrimary,
            background = LightBg,
            surface = LightSurface,
            onBackground = Color(0xFF1E293B),
            onSurface = Color(0xFF1E293B)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as androidx.activity.ComponentActivity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
