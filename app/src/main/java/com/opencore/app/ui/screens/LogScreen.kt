package com.opencore.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LogScreen() {
    val logs = remember { listOf("日志示例1", "日志示例2") }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("系统日志", fontSize = 22.sp)
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn {
            items(logs) { log ->
                Text(log, modifier = Modifier.padding(8.dp))
            }
        }
    }
}
