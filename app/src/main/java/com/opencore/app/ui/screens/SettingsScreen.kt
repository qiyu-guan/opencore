package com.opencore.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencore.app.ui.theme.TechBlue
import com.opencore.app.ui.theme.ThemeViewModel

@Composable
fun SettingsScreen(themeViewModel: ThemeViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("全局设置", fontSize = 22.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (themeViewModel.isDarkTheme.value) Icons.Default.DarkMode else Icons.Default.LightMode, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("深色主题", fontSize = 16.sp)
                            Text(if (themeViewModel.isDarkTheme.value) "已启用" else "已禁用", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Switch(checked = themeViewModel.isDarkTheme.value, onCheckedChange = { themeViewModel.toggleTheme() }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TechBlue))
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("关于", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("应用 Root 授权")
                    Button(onClick = { /* 导航到 RootGrantScreen */ }) { Text("管理") }
                }
            }
        }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("OpenCore v13.0")
                    Text("Build: 2026-06-07")
                    Text("53项核心特性")
                }
            }
        }
    }
}
