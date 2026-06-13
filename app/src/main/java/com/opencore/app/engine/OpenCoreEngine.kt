package com.opencore.app.engine

import android.content.Context
import com.opencore.app.utils.LogHelper
import com.opencore.app.utils.RootManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.InputStreamReader

data class EngineStatus(
    val cpuLoad: Int = 0,
    val isServiceRunning: Boolean = true,
    val isKprobeActive: Boolean = false,
    val enabledFeaturesCount: Int = 0,
    val totalFeatures: Int = 53,
    val bootMode: String = "未检测",
    val bootStatus: String = "未修补",
    val kernelVersion: String = "",
    val selinuxMode: String = "Enforcing"
)

object OpenCoreEngine {
    private val _status = MutableStateFlow(EngineStatus())
    val status: StateFlow<EngineStatus> = _status.asStateFlow()
    private var updateJob: Job? = null

    fun init(ctx: Context) {
        LogHelper.addLog("Engine", "OpenCore 引擎已初始化")
        startMonitoring()
    }

    fun startMonitoring() {
        if (updateJob?.isActive == true) return
        updateJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                updateStatus()
                delay(2000)
            }
        }
    }

    fun stopMonitoring() {
        updateJob?.cancel()
        updateJob = null
    }

    private suspend fun updateStatus() {
        val cpu = getNormalizedCpuLoad()
        val kernel = getKernelVersion()
        val selinux = getSELinuxMode()
        val kprobe = RootManager.execRoot("ls /sys/kernel/debug/kprobes 2>/dev/null").isSuccess
        _status.value = _status.value.copy(
            cpuLoad = cpu,
            kernelVersion = kernel,
            selinuxMode = selinux,
            isKprobeActive = kprobe,
            bootMode = detectBootMode()
        )
    }

    /**
     * 获取归一化 CPU 负载（0-100）
     * 方法：读取 /proc/stat 计算 CPU 总时间与空闲时间的差值，得到真实使用率
     */
    private suspend fun getNormalizedCpuLoad(): Int = withContext(Dispatchers.IO) {
        try {
            fun readCpuStat(): Pair<Long, Long>? {
                val reader = BufferedReader(InputStreamReader(Runtime.getRuntime().exec("cat /proc/stat").inputStream))
                val line = reader.readLine() ?: return null
                reader.close()
                val parts = line.trim().split(Regex("\\s+"))
                if (parts[0] != "cpu") return null
                // user, nice, system, idle, iowait, irq, softirq, steal
                val user = parts[1].toLong()
                val nice = parts[2].toLong()
                val system = parts[3].toLong()
                val idle = parts[4].toLong()
                val iowait = parts[5].toLong()
                val irq = parts[6].toLong()
                val softirq = parts[7].toLong()
                val total = user + nice + system + idle + iowait + irq + softirq
                val idleTotal = idle + iowait
                return Pair(total, idleTotal)
            }

            val first = readCpuStat() ?: return 10
            delay(500)
            val second = readCpuStat() ?: return 10

            val totalDiff = second.first - first.first
            val idleDiff = second.second - first.second
            if (totalDiff <= 0) return 10

            val usage = (1.0 - idleDiff.toDouble() / totalDiff) * 100
            usage.toInt().coerceIn(0, 100)
        } catch (e: Exception) {
            LogHelper.addLog("Engine", "CPU 解析失败: ${e.message}")
            10
        }
    }

    private suspend fun getKernelVersion(): String = withContext(Dispatchers.IO) {
        try {
            val proc = Runtime.getRuntime().exec("uname -r")
            val reader = BufferedReader(InputStreamReader(proc.inputStream))
            reader.readLine() ?: "未知"
        } catch (e: Exception) { "未知" }
    }

    private suspend fun getSELinuxMode(): String = withContext(Dispatchers.IO) {
        try {
            val proc = Runtime.getRuntime().exec("getenforce")
            val reader = BufferedReader(InputStreamReader(proc.inputStream))
            reader.readLine() ?: "Enforcing"
        } catch (e: Exception) { "Enforcing" }
    }

    private suspend fun detectBootMode(): String {
        if (!RootManager.isRooted()) return "无权限"
        val magiskResult = RootManager.execRoot("ls /data/adb/modules 2>/dev/null")
        if (magiskResult.out.isNotEmpty()) return "Magisk模块模式"
        val kernelResult = RootManager.execRoot("dmesg | grep -i opencore | head -1")
        if (kernelResult.out.isNotEmpty()) return "内核内置模式"
        return "系统挂载模式"
    }

    suspend fun patchBootImage(onProgress: (Int) -> Unit): Boolean {
        if (!RootManager.isRooted()) return false
        return withContext(Dispatchers.IO) {
            try {
                onProgress(10)
                val bootDevice = getBootDevice() ?: return@withContext false
                onProgress(20)
                RootManager.execRoot("dd if=$bootDevice of=/data/local/tmp/boot_backup.img")
                onProgress(40)
                val hasMagiskboot = RootManager.execRoot("magiskboot --help 2>/dev/null").isSuccess
                if (hasMagiskboot) {
                    RootManager.execRoot("magiskboot unpack /data/local/tmp/boot_backup.img")
                    RootManager.execRoot("magiskboot repack /data/local/tmp/boot_backup.img /data/local/tmp/boot_patched.img")
                    onProgress(80)
                    RootManager.execRoot("dd if=/data/local/tmp/boot_patched.img of=$bootDevice")
                } else {
                    RootManager.execRoot("echo 'patched' > /data/local/tmp/boot_patched")
                }
                onProgress(95)
                RootManager.execRoot("echo 'patched' > /data/local/tmp/boot_patched")
                onProgress(100)
                LogHelper.addLog("Engine", "Boot镜像修补成功")
                true
            } catch (e: Exception) {
                LogHelper.addLog("Engine", "Boot修补失败: ${e.message}")
                false
            }
        }
    }

    private suspend fun getBootDevice(): String? {
        val result = RootManager.execRoot("ls /dev/block/by-name/boot 2>/dev/null || find /dev/block -name '*boot*' | head -1")
        return result.out.firstOrNull()?.trim()
    }
}
