package com.opencore.app.engine

import android.content.Context
import com.opencore.app.utils.LogHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Module(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    var isEnabled: Boolean,
    val needsRoot: Boolean = true
)

object ModuleManager {
    private lateinit var context: Context
    
    private val availableModules = listOf(
        Module(0, "设备伪装", "修改系统设备参数，绕过应用检测", "设备伪装组", false, true),
        Module(1, "内核管控", "kprobe 动态注入，内核级权限管理", "内核管控组", true, true),
        Module(2, "SELinux 规则", "动态放行/拦截 SELinux 权限策略", "SELinux与权限组", false, true),
        Module(3, "SU 权限体系", "自研 Root 权限管理，独立于 Magisk", "SELinux与权限组", true, true),
        Module(4, "虚拟机隔离", "应用运行环境隔离，防止检测", "SELinux与权限组", false, true),
        Module(5, "日志管控", "屏蔽系统日志输出，保护隐私", "系统维护组", true, true),
        Module(6, "分区挂载引擎", "自定义分区挂载与卸载管理", "系统维护组", false, true),
        Module(7, "OTA 拦截", "屏蔽系统 OTA 升级通知", "系统维护组", true, false),
        Module(8, "缓存自动清理", "定期清理系统缓存，释放空间", "系统维护组", false, false),
        Module(9, "分区自动修复", "检测并修复分区异常问题", "系统维护组", false, true)
    )
    
    fun init(ctx: Context) {
        context = ctx.applicationContext
        loadSavedStates()
    }
    
    private fun loadSavedStates() {
        val prefs = context.getSharedPreferences("opencore_prefs", Context.MODE_PRIVATE)
        availableModules.forEach { module ->
            module.isEnabled = prefs.getBoolean("module_${module.id}", module.isEnabled)
        }
    }
    
    fun getAllModules(): List<Module> = availableModules
    
    fun getModulesByCategory(): Map<String, List<Module>> = availableModules.groupBy { it.category }
    
    suspend fun setModuleEnabled(moduleId: Int, enabled: Boolean): Boolean {
        val module = availableModules.find { it.id == moduleId } ?: return false
        
        module.isEnabled = enabled
        saveModuleState(moduleId, enabled)
        
        val prefs = context.getSharedPreferences("opencore_prefs", Context.MODE_PRIVATE)
        val currentCount = prefs.getInt("enabled_modules_count", 35)
        val newCount = if (enabled) currentCount + 1 else currentCount - 1
        prefs.edit().putInt("enabled_modules_count", newCount).apply()
        
        LogHelper.addLog("ModuleManager", "模块 ${module.name} ${if (enabled) "启用" else "禁用"}")
        return true
    }
    
    private fun saveModuleState(moduleId: Int, enabled: Boolean) {
        val prefs = context.getSharedPreferences("opencore_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("module_$moduleId", enabled).apply()
    }
}
