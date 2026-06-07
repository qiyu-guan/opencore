package com.opencore.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencore.app.ui.theme.TechBlue
import com.opencore.app.ui.theme.ThemeViewModel

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
    val progressValue = enabledFeatures.toFloat() / totalFeatures.toFloat()
    val engineProgress = engineLoad.toFloat() / 100f
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 标题区域
        Text(
            text = "OpenCore 控制中心",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "OpenCore v2.0 Native Engine | 运行中 | 53项特性",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "已启用 $enabledFeatures/$totalFeatures",
            fontSize = 12.sp,
            color = TechBlue
        )
        
        LinearProgressIndicator(
            progress = { progressValue },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = TechBlue,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // 状态卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "实时工作状态",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "引擎负载: $engineLoad%",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                
                LinearProgressIndicator(
                    progress = { engineProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = TechBlue,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val serviceText = if (isServiceRunning) "运行中" else "已停止"
                val serviceColor = if (isServiceRunning) SuccessGreen else ErrorRed
                Text(
                    text = "底层服务: $serviceText",
                    fontSize = 14.sp,
                    color = serviceColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val kprobeText = if (isKprobeActive) "活跃" else "未激活"
                val kprobeColor = if (isKprobeActive) SuccessGreen else ErrorRed
                Text(
                    text = "内核注入: $kprobeText",
                    fontSize = 14.sp,
                    color = kprobeColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "启用功能: 设备伪装 • SELinux • SU权限 • 虚拟机隔离",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        // Boot 卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Boot镜像管理",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "当前模式: $bootMode",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val bootColor = if (bootStatus == "已修补") SuccessGreen else Color.Gray
                Text(
                    text = "镜像状态: $bootStatus",
                    fontSize = 14.sp,
                    color = bootColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                if (patchProgress > 0 && patchProgress < 100) {
                    val patchProgressValue = patchProgress.toFloat() / 100f
                    LinearProgressIndicator(
                        progress = { patchProgressValue },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = TechBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Button(
                    onClick = {
                        patchProgress = 100
                        bootStatus = "已修补"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TechBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "一键修补 Boot 镜像",
                        color = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "OpenCore v2.0.0 | Build 2026.06",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// 定义颜色常量（避免重复导入）
private val SuccessGreen = Color(0xFF10B981)
private val ErrorRed = Color(0xFFEF4444)
