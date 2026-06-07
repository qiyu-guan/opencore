package com.opencore.app.utils

import android.content.Context
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader

object RootManager {
    private var suPath: String? = null
    private var isRootAvailable = false

    fun init(context: Context) {
        suPath = detectSuPath()
        isRootAvailable = suPath != null && verifySu(suPath!!)
        LogHelper.addLog("RootManager", if (isRootAvailable) "Root 权限已获取 ($suPath)" else "设备未 Root")
    }

    private fun detectSuPath(): String? {
        val paths = listOf(
            "/system/bin/su", "/system/xbin/su", "/su/bin/su",
            "/data/local/bin/su", "/data/local/xbin/su"
        )
        for (path in paths) {
            val file = File(path)
            if (file.exists() && file.canExecute()) {
                return path
            }
        }
        return null
    }

    private fun verifySu(path: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("$path --version")
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    fun isRooted(): Boolean = isRootAvailable

    suspend fun execRoot(command: String): RootResult {
        if (!isRootAvailable) return RootResult(false, emptyList(), listOf("No root access"))
        return try {
            val process = Runtime.getRuntime().exec(arrayOf(suPath!!, "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val output = reader.readLines()
            val error = errorReader.readLines()
            reader.close(); errorReader.close()
            process.waitFor()
            RootResult(process.exitValue() == 0, output, error)
        } catch (e: Exception) {
            RootResult(false, emptyList(), listOf(e.message ?: "Execution failed"))
        }
    }

    fun requestRootPermission(activity: android.app.Activity, callback: (Boolean) -> Unit) {
        if (isRootAvailable) {
            callback(true)
            return
        }
        try {
            val process = Runtime.getRuntime().exec(arrayOf(suPath ?: "su", "-c", "id"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            reader.close()
            val result = process.waitFor()
            isRootAvailable = result == 0 && output.contains("uid=0")
            if (isRootAvailable) suPath = suPath ?: "su"
            callback(isRootAvailable)
        } catch (e: Exception) {
            callback(false)
        }
    }

    data class RootResult(val isSuccess: Boolean, val out: List<String>, val err: List<String>)
}
