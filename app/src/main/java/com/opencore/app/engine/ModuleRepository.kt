package com.opencore.app.engine

import android.content.Context
import com.opencore.app.utils.LogHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class RemoteModule(
    val id: String,
    val name: String,
    val description: String,
    val downloadUrl: String,
    val version: String
)

object ModuleRepository {
    // 国内可访问的模块源（可替换为实际有效链接）
    val availableModules = listOf(
        RemoteModule(
            id = "magisk_props",
            name = "MagiskHide Props Config",
            description = "设备指纹修改模块",
            downloadUrl = "https://ghproxy.com/https://github.com/Magisk-Modules-Repo/MagiskHidePropsConf/releases/latest/download/MagiskHidePropsConf.zip",
            version = "6.1.2"
        ),
        RemoteModule(
            id = "systemless_hosts",
            name = "Systemless Hosts",
            description = "广告屏蔽 hosts 模块",
            downloadUrl = "https://ghproxy.com/https://github.com/Magisk-Modules-Repo/hosts/releases/latest/download/systemless-hosts.zip",
            version = "1.0"
        )
    )

    suspend fun downloadModule(url: String, destFile: File, onProgress: (Int) -> Unit): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.requestMethod = "GET"
                val fileLength = connection.contentLength
                val input = connection.inputStream
                val output = destFile.outputStream()
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalRead = 0L
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    if (fileLength > 0) {
                        onProgress((totalRead * 100 / fileLength).toInt())
                    }
                }
                output.close()
                input.close()
                connection.disconnect()
                true
            } catch (e: Exception) {
                LogHelper.addLog("ModuleRepo", "下载失败: ${e.message}")
                false
            }
        }
    }
}
