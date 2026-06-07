package com.opencore.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencore.app.ui.theme.ThemeViewModel

@Composable
fun SettingsScreen(themeViewModel: ThemeViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "全局配置",
                fontSize = 22.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            SettingsCard(
                title = "外观",
                items = listOf(
                    SettingsItem(
                        icon = if (themeViewModel.isDarkTheme.value) Icons.Default.DarkMode else Icons.Default.LightMode,
                        title = "深色主题",
                        subtitle = if (themeViewModel.isDarkTheme.value) "已启用深色模式" else "已启用浅色模式",
                        action = {
                            Switch(
                                checked = themeViewModel.isDarkTheme.value,
                                onCheckedChange = { themeViewModel.toggleTheme() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = TechBlue
                                )
                            )
                        }
                    )
                )
            )
        }
        
        item {
            SettingsCard(
                title = "关于",
                items = listOf(
                    SettingsItem(
                        icon = null,
                        title = "版本",
                        subtitle = "OpenCore v2.0.0",
                        action = null
                    ),
                    SettingsItem(
                        icon = null,
                        title = "构建日期",
                        subtitle = "2026-06-06",
                        action = null
                    ),
                    SettingsItem(
                        icon = null,
                        title = "53项核心特性",
                        subtitle = "完整版已启用",
                        action = null
                    )
                )
            )
        }
    }
}

data class SettingsItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector?,
    val title: String,
    val subtitle: String,
    val action: (@Composable () -> Unit)?
)

@Composable
fun SettingsCard(
    title: String,
    items: List<SettingsItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (item.icon != null) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                        Column {
                            Text(
                                text = item.title,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = item.subtitle,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                    item.action?.invoke()
                }
                if (items.last() != item) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

private val TechBlue = Color(0xFF2563EB)
