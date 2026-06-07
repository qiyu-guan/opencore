package com.opencore.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencore.app.utils.LogHelper
import kotlinx.coroutines.launch

@Composable
fun LogScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var logs by remember { mutableStateOf<List<LogEntry>>(emptyList()) }
    
    fun refreshLogs() {
        logs = LogHelper.getLogs().mapNotNull { line ->
            parseLogEntry(line)
        }.reversed()
    }
    
    LaunchedEffect(Unit) {
        refreshLogs()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "系统日志",
                fontSize = 22.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Row {
                IconButton(
                    onClick = {
                        scope.launch {
                            LogHelper.clearLog()
                            refreshLogs()
                        }
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "清空", tint = TechBlue)
                }
                IconButton(
                    onClick = {
                        // 导出日志逻辑
                    }
                ) {
                    Icon(Icons.Default.Download, contentDescription = "导出", tint = TechBlue)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 日志列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(logs) { log ->
                LogItem(log)
            }
        }
    }
}

@Composable
fun LogItem(log: LogEntry) {
    val logColor = when (log.level) {
        LogLevel.ERROR -> Color(0xFFEF4444)
        LogLevel.WARNING -> Color(0xFFF59E0B)
        LogLevel.INFO -> TechBlue
        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = log.timestamp,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Text(
                    text = log.level.name,
                    fontSize = 10.sp,
                    color = logColor,
                    modifier = Modifier
                        .background(logColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = log.content,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
        }
    }
}

enum class LogLevel { INFO, WARNING, ERROR }
data class LogEntry(val timestamp: String, val level: LogLevel, val content: String)

private fun parseLogEntry(line: String): LogEntry? {
    val regex = Regex("\\[(.*?)\\] (.*?): (.*)")
    val match = regex.find(line)
    return if (match != null) {
        val (timestamp, tag, content) = match.destructured
        val level = when {
            content.contains("ERROR", ignoreCase = true) -> LogLevel.ERROR
            content.contains("WARN", ignoreCase = true) -> LogLevel.WARNING
            else -> LogLevel.INFO
        }
        LogEntry(timestamp, level, "[$tag] $content")
    } else null
}

private val TechBlue = Color(0xFF2563EB)
