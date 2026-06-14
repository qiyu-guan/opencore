package com.opencore.app.utils

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RootGrantHelper {
    
    /**
     * 获取已安装的应用列表
     */
    fun getInstalledApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        return packages.mapNotNull { pkg ->
            try {
                val appInfo = pm.getApplicationInfo(pkg.packageName, 0)
                AppInfo(
                    packageName = pkg.packageName,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    isSystem = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.appName }
    }
    
    /**
     * 为指定应用授予 Root 权限（通过 su 白名单）
     */
    suspend fun grantRootAccess(packageName: String, onProgress: (String) -> Unit): Boolean {
        if (!RootManager.isRooted()) {
            onProgress("需要 Root 权限")
            return false
        }
        
        return withContext(Dispatchers.IO) {
            try {
                onProgress("正在配置权限...")
                
                // 方法1：通过 magiskpolicy 添加规则（如果存在）
                val magiskResult = RootManager.execRoot("magiskpolicy --live \"allow $packageName * * *\" 2>/dev/null")
                if (magiskResult.isSuccess) {
                    onProgress("已通过 Magisk 授权")
                    return@withContext true
                }
                
                // 方法2：将应用加入 su 允许列表（SuperSU 方式）
                val addToList = RootManager.execRoot("echo '$packageName' >> /data/data/com.koushikdutta.superuser/files/whitelist.txt")
                if (addToList.isSuccess) {
                    onProgress("已加入 SuperSU 白名单")
                    return@withContext true
                }
                
                // 方法3：创建独立 su 包装脚本
                val script = "/data/local/tmp/su_$packageName.sh"
                val createScript = RootManager.execRoot("echo '#!/system/bin/sh\\nexec /system/bin/su -c \"\\$@\"' > $script && chmod 755 $script")
                if (createScript.isSuccess) {
                    onProgress("已创建独立 su 脚本")
                    return@withContext true
                }
                
                onProgress("无法配置 Root 权限，请手动在 Magisk/SuperSU 中授权")
                false
            } catch (e: Exception) {
                onProgress("授权失败: ${e.message}")
                false
            }
        }
    }
    
    /**
     * 检查应用是否已有 Root 权限
     */
    suspend fun checkRootAccess(packageName: String): Boolean = withContext(Dispatchers.IO) {
        val result = RootManager.execRoot("su -c 'id $packageName' 2>/dev/null")
        result.out.any { it.contains("uid=0") }
    }
    
    data class AppInfo(
        val packageName: String,
        val appName: String,
        val isSystem: Boolean
    )
}
