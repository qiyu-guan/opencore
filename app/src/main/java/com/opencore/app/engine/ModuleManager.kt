package com.opencore.app.engine

import android.content.Context
import com.opencore.app.utils.LogHelper
import com.opencore.app.utils.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

data class InstalledModule(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val isEnabled: Boolean = true
)

object ModuleManager {
    private lateinit var context: Context
    private var cachedModules: List<InstalledModule> = emptyList()

    fun init(ctx: Context) {
        context = ctx.applicationContext
        // 使用 runBlocking 在初始化时同步加载
        runBlocking {
            loadInstalledModules()
        }
    }

    suspend fun loadInstalledModules() {
        if (!RootManager.isRooted()) return
        cachedModules = withContext(Dispatchers.IO) {
            val result = RootManager.execRoot("ls /data/adb/modules/ 2>/dev/null")
            val dirs = result.out.filter { it.isNotBlank() && it != "lost+found" }
            val modules = mutableListOf<InstalledModule>()
            for (dir in dirs) {
                val moduleId = dir.trim()
                val propResult = RootManager.execRoot("cat /data/adb/modules/$moduleId/module.prop 2>/dev/null")
                if (propResult.isSuccess) {
                    var name = moduleId
                    var version = "未知"
                    var description = ""
                    for (line in propResult.out) {
                        when {
                            line.startsWith("name=") -> name = line.substring(5).trim()
                            line.startsWith("version=") -> version = line.substring(8).trim()
                            line.startsWith("description=") -> description = line.substring(12).trim()
                        }
                    }
                    modules.add(InstalledModule(moduleId, name, version, description, true))
                }
            }
            modules
        }
        LogHelper.addLog("ModuleManager", "已加载 ${cachedModules.size} 个模块")
    }

    fun getInstalledModules(): List<InstalledModule> = cachedModules
}
