package com.opencore.app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import android.content.pm.PackageManager
import java.io.BufferedReader
import java.io.InputStreamReader

object RootManager {
    private var isRootAvailable = false
    private var hasRequested = false
    
    fun init(context: Context) {
        isRootAvailable = checkRoot()
        LogHelper.addLog("RootManager", if (isRootAvailable) "Root 权限已获取" else "设备未 Root")
    }
    
    private fun checkRoot(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c exit")
            val result = process.waitFor()
            result == 0
        } catch (e: Exception) {
            false
        }
    }
    
    fun isRooted(): Boolean = isRootAvailable
    
    /**
     * 主动请求 Root 权限（通过 su 交互）
     */
    fun requestRootPermission(activity: Activity, onResult: (Boolean) -> Unit) {
        if (isRootAvailable) {
            onResult(true)
            return
        }
        
        try {
            // 弹出 su 授权窗口
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            reader.close()
            val result = process.waitFor()
            
            isRootAvailable = result == 0 && output.contains("uid=0")
            onResult(isRootAvailable)
            LogHelper.addLog("RootManager", "Root 权限请求结果: $isRootAvailable")
        } catch (e: Exception) {
            // 如果没有 su，引导用户安装 Magisk
            showMagiskGuide(activity)
            onResult(false)
        }
    }
    
    private fun showMagiskGuide(activity: Activity) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/topjohnwu/Magisk/releases"))
        activity.startActivity(intent)
    }
    
    suspend fun execRoot(command: String): ShellResult {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val output = reader.readLines()
            val error = errorReader.readLines()
            reader.close()
            errorReader.close()
            process.waitFor()
            ShellResult(process.exitValue() == 0, output, error)
        } catch (e: Exception) {
            ShellResult(false, emptyList(), listOf(e.message ?: "执行失败"))
        }
    }
    
    data class ShellResult(val isSuccess: Boolean, val out: List<String>, val err: List<String>)
}
