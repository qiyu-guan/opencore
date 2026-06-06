package com.opencore.app.utils

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

object LogHelper {
    private const val PREFS_NAME = "opencore_logs"
    private const val KEY_LOG = "log_list"
    private const val MAX_LOG_SIZE = 200
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun getPrefs(): SharedPreferences? {
        val ctx = appContext ?: return null
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun addLog(tag: String, message: String) {
        val prefs = getPrefs() ?: return
        val timestamp = dateFormat.format(Date())
        val logEntry = "[$timestamp] $tag: $message"
        val currentLogs = getLogsFromPrefs(prefs).toMutableList()
        currentLogs.add(logEntry)
        if (currentLogs.size > MAX_LOG_SIZE) currentLogs.removeAt(0)
        prefs.edit().putStringSet(KEY_LOG, currentLogs.toSet()).apply()

        // 自动清除旧日志（7天）
        val autoClear = appContext?.getSharedPreferences("opencore_prefs", Context.MODE_PRIVATE)
            ?.getBoolean("auto_clear_log", true) ?: true
        if (autoClear) {
            trimOldLogs(prefs)
        }
    }

    private fun trimOldLogs(prefs: SharedPreferences) {
        val logs = getLogsFromPrefs(prefs).toMutableList()
        val cutoff = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L
        val newLogs = logs.filter {
            try {
                val dateStr = it.substring(1, 20)
                val date = dateFormat.parse(dateStr)
                date.time > cutoff
            } catch (e: Exception) {
                true
            }
        }
        if (newLogs.size != logs.size) {
            prefs.edit().putStringSet(KEY_LOG, newLogs.toSet()).apply()
        }
    }

    private fun getLogsFromPrefs(prefs: SharedPreferences): List<String> {
        return prefs.getStringSet(KEY_LOG, emptySet())?.toList() ?: emptyList()
    }

    fun getLogs(): List<String> {
        val prefs = getPrefs() ?: return emptyList()
        return getLogsFromPrefs(prefs)
    }

    fun clearLog() {
        val prefs = getPrefs() ?: return
        prefs.edit().remove(KEY_LOG).apply()
        addLog("LogHelper", "日志已手动清除")
    }
}
