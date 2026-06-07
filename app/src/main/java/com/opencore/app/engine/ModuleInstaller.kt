package com.opencore.app.engine

import android.content.Context
import com.opencore.app.utils.RootManager
import com.opencore.app.utils.LogHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile

object ModuleInstaller {
    
    suspend fun installFromZip(context: Context, zipPath: String, onProgress: (String) -> Unit): Boolean {
        if (!RootManager.isRooted()) {
            onProgress("需要 Root 权限")
            return false
        }
        
        return withContext(Dispatchers.IO) {
            try {
                onProgress("解析 zip 文件...")
                val zipFile = ZipFile(zipPath)
                
                // 检查 module.prop 是否存在
                val moduleProp = zipFile.getEntry("module.prop")
                if (moduleProp == null) {
                    onProgress("无效的 Magisk 模块：缺少 module.prop")
                    zipFile.close()
                    return@withContext false
                }
                
                // 读取模块 ID
                val propContent = zipFile.getInputStream(moduleProp).bufferedReader().readText()
                val moduleId = Regex("id=(.*)").find(propContent)?.groupValues?.get(1)?.trim() ?: "opencore_module"
                
                onProgress("安装模块: $moduleId")
                
                // 创建模块目录
                val moduleDir = "/data/adb/modules/$moduleId"
                RootManager.execRoot("mkdir -p $moduleDir")
                
                // 解压文件到临时目录
                val tempDir = "/data/local/tmp/module_install"
                RootManager.execRoot("rm -rf $tempDir && mkdir -p $tempDir")
                
                // 解压 zip（使用 busybox unzip 或系统 unzip）
                val unzipCmd = "unzip $zipPath -d $tempDir"
                val unzipResult = RootManager.execRoot(unzipCmd)
                if (!unzipResult.isSuccess) {
                    onProgress("解压失败: ${unzipResult.err.joinToString()}")
                    return@withContext false
                }
                
                onProgress("复制文件到模块目录...")
                // 复制所有文件（保留权限）
                RootManager.execRoot("cp -rf $tempDir/* $moduleDir/")
                
                // 如果存在 customize.sh，赋予执行权限
                RootManager.execRoot("chmod 755 $moduleDir/customize.sh 2>/dev/null")
                
                // 更新模块状态
                RootManager.execRoot("touch $moduleDir/update")
                
                // 清理临时文件
                RootManager.execRoot("rm -rf $tempDir")
                zipFile.close()
                
                onProgress("模块安装成功，请重启设备生效")
                LogHelper.addLog("ModuleInstaller", "模块 $moduleId 安装成功")
                true
            } catch (e: Exception) {
                onProgress("安装失败: ${e.message}")
                LogHelper.addLog("ModuleInstaller", "安装失败: ${e.message}")
                false
            }
        }
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
                onProgress("模块已卸载，重启后生效")
                true
            } else {
                onProgress("卸载失败: ${result.err.joinToString()}")
                false
            }
        }
    }
}
