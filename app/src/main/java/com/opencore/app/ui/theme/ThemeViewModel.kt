package com.opencore.app.ui.theme

import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ThemeViewModel(private val context: Context) : ViewModel() {
    val isDarkTheme = mutableStateOf(true)
    val primaryColor = mutableStateOf(DefaultPrimary)

    init {
        val prefs = context.getSharedPreferences("opencore_prefs", Context.MODE_PRIVATE)
        isDarkTheme.value = prefs.getBoolean("dark_theme", true)
        val colorHex = prefs.getString("primary_color", "#FFB6C1") ?: "#FFB6C1"
        primaryColor.value = ComposeColor(Color.parseColor(colorHex))
    }

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
        context.getSharedPreferences("opencore_prefs", Context.MODE_PRIVATE).edit()
            .putBoolean("dark_theme", isDarkTheme.value).apply()
    }

    fun setPrimaryColor(color: ComposeColor) {
        primaryColor.value = color
        val hex = String.format("#%06X", (0xFFFFFF and color.toArgb()))
        context.getSharedPreferences("opencore_prefs", Context.MODE_PRIVATE).edit()
            .putString("primary_color", hex).apply()
    }
}

class ThemeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
