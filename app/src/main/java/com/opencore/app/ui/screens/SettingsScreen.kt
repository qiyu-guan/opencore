package com.opencore.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.opencore.app.R
import com.opencore.app.ui.theme.PresetColors
import com.opencore.app.ui.theme.TechBlue
import com.opencore.app.ui.theme.ThemeViewModel

@Composable
fun SettingsScreen(themeViewModel: ThemeViewModel, navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("全局设置", fontSize = 22.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }

        // 深色主题
        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (themeViewModel.isDarkTheme.value) Icons.Default.DarkMode else Icons.Default.LightMode, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("深色主题", fontSize = 16.sp)
                            Text(if (themeViewModel.isDarkTheme.value) "已启用" else "已禁用", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Switch(
                        checked = themeViewModel.isDarkTheme.value,
                        onCheckedChange = { themeViewModel.toggleTheme() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TechBlue)
                    )
                }
            }
        }

        // 主色选择
        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("主题主色", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PresetColors.forEach { (name, color) ->
                            val isSelected = color == themeViewModel.primaryColor.value
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(50),
                                color = color,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, TechBlue) else null,
                                onClick = {
                                    themeViewModel.setPrimaryColor(color)
                                }
                            ) {
                                if (isSelected) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Root 授权管理
        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = TechBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Root 权限管理", fontSize = 16.sp)
                            Text("管理应用的 Root 授权", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Button(
                        onClick = { navController.navigate("root_auth") },
                        colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("管理", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        // 字库备份
        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SdStorage, contentDescription = null, tint = TechBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("字库备份", fontSize = 16.sp)
                            Text("备份 eMMC/UFS 分区", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Button(
                        onClick = { navController.navigate("backup") },
                        colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("进入", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        // 关于（含工作室信息）
        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Image(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_studio),
                        contentDescription = "工作室图标",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("根隙弥合", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text("创始人: qiyu", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("版本 22.1", fontSize = 14.sp, color = TechBlue)
                    Text("构建日期: 2026-06-20", fontSize = 12.sp, color = Color.Gray)
                    Text("53项核心特性", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}
