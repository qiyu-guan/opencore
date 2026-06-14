package com.opencore.app.engine

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.opencore.app.utils.LogHelper
import com.opencore.app.utils.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile

object ModuleInstaller {
    suspend fun installFromZip(context: Context, zipUri: Uri, onProgress: (String) -> Unit): Boolean {
        if (!RootManager.isRooted()) {
            onProgress("需要 Root 权限")
            return false
        }
        return withContext(Dispatchers.IO) {
            try {
                onProgress("复制模块文件...")
                val cachedFile = File(context.cacheDir, "temp_module_${System.currentTimeMillis()}.zip")
                context.contentResolver.openInputStream(zipUri)?.use { input ->
                    cachedFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: throw Exception("无法读取 zip 文件")

                onProgress("解压模块...")
                val tempDir = "/data/local/tmp/module_install"
                RootManager.execRoot("rm -rf $tempDir; mkdir -p $tempDir")
                val unzipResult = RootManager.execRoot("unzip ${cachedFile.absolutePath} -d $tempDir")
                if (!unzipResult.isSuccess) throw Exception("解压失败: ${unzipResult.err.joinToString()}")

                onProgress("读取模块信息...")
                val moduleId = parseModuleId(tempDir) ?: throw Exception("缺少 module.prop 或 id 字段")
                val moduleDest = "/data/adb/modules/$moduleId"
                RootManager.execRoot("rm -rf $moduleDest; mkdir -p $moduleDest")
                onProgress("安装到 $moduleDest")

                val copyResult = RootManager.execRoot("cp -rf $tempDir/* $moduleDest/")
                if (!copyResult.isSuccess) throw Exception("复制失败: ${copyResult.err.joinToString()}")

                RootManager.execRoot("chmod 755 $moduleDest/customize.sh 2>/dev/null")
                RootManager.execRoot("touch $moduleDest/update")
                RootManager.execRoot("rm -rf $tempDir")
                cachedFile.delete()

                onProgress("安装成功！重启后生效")
                LogHelper.addLog("ModuleInstaller", "模块 $moduleId 安装成功")
                true
            } catch (e: Exception) {
                onProgress("安装失败: ${e.message}")
                LogHelper.addLog("ModuleInstaller", "安装失败: ${e.message}")
                false
            }
        }
    }

    private suspend fun parseModuleId(tempDir: String): String? {
        val propPath = "$tempDir/module.prop"
        val result = RootManager.execRoot("cat $propPath 2>/dev/null")
        if (!result.isSuccess) return null
        val lines = result.out
        for (line in lines) {
            if (line.startsWith("id=")) {
                return line.substring(3).trim()
            }
        }
        return null
    }

    suspend fun uninstallModule(moduleId: String, onProgress: (String) -> Unit): Boolean {
        if (!RootManager.isRooted()) {
            onProgress("需要 Root 权限")
            return false
        }
        return withContext(Dispatchers.IO) {
            val modulePath = "/data/adb/modules/$moduleId"
            val result = RootManager.execRoot("rm -rf $modulePath")
            if (result.isSuccess) {
                onProgress("卸载成功，重启后生效")
                true
            } else {
                onProgress("卸载失败: ${result.err.joinToString()}")
                false
            }
        }
    }

    suspend fun rebootDevice(onProgress: (String) -> Unit): Boolean {
        if (!RootManager.isRooted()) {
            onProgress("需要 Root 权限才能重启")
            return false
        }
        return withContext(Dispatchers.IO) {
            try {
                onProgress("正在重启设备...")
                delay(1000)
                val result = RootManager.execRoot("reboot")
                if (result.isSuccess) {
                    onProgress("设备正在重启")
                    true
                } else {
                    onProgress("重启失败: ${result.err.joinToString()}")
                    false
                }
            } catch (e: Exception) {
                onProgress("重启异常: ${e.message}")
                false
            }
        }
    }

    suspend fun softReboot(onProgress: (String) -> Unit): Boolean {
        if (!RootManager.isRooted()) {
            onProgress("需要 Root 权限")
            return false
        }
        return withContext(Dispatchers.IO) {
            try {
                onProgress("正在软重启...")
                val result = RootManager.execRoot("pkill -f com.android.systemui")
                if (result.isSuccess) {
                    onProgress("SystemUI 已重启")
                    true
                } else {
                    onProgress("软重启失败，尝试硬重启")
                    rebootDevice(onProgress)
                }
            } catch (e: Exception) {
                onProgress("软重启异常: ${e.message}")
                false
            }
        }
    }
}
