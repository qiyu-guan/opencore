package com.opencore.app.ui.theme

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore("theme_prefs")

class ThemeViewModel(private val context: Context) : ViewModel() {
    val isDarkTheme = mutableStateOf(true)

    init {
        viewModelScope.launch {
            val prefs = context.dataStore.data.first()
            isDarkTheme.value = prefs[PreferencesKeys.DARK_THEME] ?: true
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val newValue = !isDarkTheme.value
            isDarkTheme.value = newValue
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.DARK_THEME] = newValue
            }
        }
    }

    private object PreferencesKeys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }
}

import androidx.lifecycle.ViewModelProvider

class ThemeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    
    companion object {
        fun Factory(context: Context): ViewModelProvider.Factory = ThemeViewModelFactory(context)
    }
}
