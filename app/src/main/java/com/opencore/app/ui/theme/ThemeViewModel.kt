package com.opencore.app.ui.theme

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("theme_prefs")

class ThemeViewModel(private val context: Context) : ViewModel() {
    val isDarkTheme = mutableStateOf(true)

    init {
        viewModelScope.launch {
            try {
                val prefs = context.dataStore.data.first()
                isDarkTheme.value = prefs[PreferencesKeys.DARK_THEME] ?: true
            } catch (e: Exception) {
                isDarkTheme.value = true
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val newValue = !isDarkTheme.value
            isDarkTheme.value = newValue
            try {
                context.dataStore.edit { prefs ->
                    prefs[PreferencesKeys.DARK_THEME] = newValue
                }
            } catch (e: Exception) {
                // 忽略错误
            }
        }
    }

    private object PreferencesKeys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
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
