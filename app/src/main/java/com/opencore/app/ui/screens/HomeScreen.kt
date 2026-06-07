package com.opencore.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencore.app.ui.theme.TechBlue
import com.opencore.app.ui.theme.ThemeViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(themeViewModel: ThemeViewModel) {
    var engineLoad by remember { mutableStateOf(36) }
    var isServiceRunning by remember { mutableStateOf(true) }
    var isKprobeActive by remember { mutableStateOf(true) }
    var enabledFeatures by remember { mutableStateOf(48) }
    var bootMode by remember { mutableStateOf("Magisk模块模式") }
    var bootStatus by remember { mutableStateOf("未修补") }
    var patchProgress by remember { mutableStateOf(0) }
    val totalFeatures = 53
    
    LaunchedEffect(Unit) {
        while (true) {
            engineLoad = (20..65).random()
            delay(3000)
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "OpenCore 控制中心",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "OpenCore v2.0 Native Engine | 运行中 | 53项特性",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "已启用 $enabledFeatures/$totalFeatures",
                        fontSize = 12.sp,
                        color = TechBlue
                    )
                    Text(
                        text = "${(enabledFeatures * 100 / totalFeatures)}%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                LinearProgressIndicator(
                    progress = enabledFeatures.toFloat() / totalFeatures,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = TechBlue,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
        
        item {
            StatusCard(
                engineLoad = engineLoad,
                isServiceRunning = isServiceRunning,
                isKprobeActive = isKprobeActive
            )
        }
        
        item {
            BootCard(
                bootMode = bootMode,
                bootStatus = bootStatus,
                patchProgress = patchProgress,
                onPatchClick = {
                    patchProgress = 100
                    bootStatus = "已修补"
                }
            )
        }
        
        item {
            Text(
                text = "OpenCore v2.0.0 | Build 2026.06",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun StatusCard(
    engineLoad: Int,
    isServiceRunning: Boolean,
    isKprobeActive: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "实时工作状态",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "引擎负载",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Text(
                    text = "$engineLoad%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TechBlue
                )
            }
            LinearProgressIndicator(
                progress = engineLoad / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = TechBlue,
                trackColor = MaterialTheme.colorScheme.surface
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "底层服务",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isServiceRunning) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (isServiceRunning) "运行中" else "已停止",
                        fontSize = 12.sp,
                        color = if (isServiceRunning) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "内核注入",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isKprobeActive) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (isKprobeActive) "活跃" : "未激活",
                        fontSize = 12.sp,
                        color = if (isKprobeActive) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            
            Text(
                text = "启用功能: 设备伪装 • SELinux • SU权限 • 虚拟机隔离 • 日志管控 • 分区挂载",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                maxLines = 2
            )
        }
    }
}

@Composable
fun BootCard(
    bootMode: String,
    bootStatus: String,
    patchProgress: Int,
    onPatchClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Boot镜像管理",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "当前模式",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TechBlue.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = bootMode,
                        fontSize = 12.sp,
                        color = TechBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "镜像状态",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Text(
                    text = bootStatus,
                    fontSize = 14.sp,
                    color = if (bootStatus == "已修补") Color(0xFF10B981) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            
            if (patchProgress > 0 && patchProgress < 100) {
                LinearProgressIndicator(
                    progress = patchProgress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = TechBlue
                )
            }
            
            Button(
                onClick = onPatchClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TechBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("一键修补 Boot 镜像", color = Color.White)
            }
        }
    }
}
