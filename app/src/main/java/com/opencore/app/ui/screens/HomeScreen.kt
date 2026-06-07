package com.opencore.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencore.app.engine.OpenCoreEngine
import com.opencore.app.ui.theme.TechBlue
import com.opencore.app.ui.theme.ThemeViewModel
import com.opencore.app.utils.RootManager
import kotlinx.coroutines.launch
import android.widget.Toast

@Composable
fun HomeScreen(themeViewModel: ThemeViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val engineStatus by OpenCoreEngine.status.collectAsState()
    var patchProgress by remember { mutableStateOf(0) }
    var isPatching by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        OpenCoreEngine.startMonitoring()
    }

    DisposableEffect(Unit) {
        onDispose {
            OpenCoreEngine.stopMonitoring()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("OpenCore 控制中心", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(4.dp))
        Text("OpenCore v14.0 | 53项特性", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(8.dp))

        if (!RootManager.isRooted()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)), shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("未检测到 Root 权限，部分功能不可用", fontSize = 12.sp, color = Color(0xFFEF4444))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        AnimatedLoadIndicator(engineStatus.cpuLoad)
        Spacer(modifier = Modifier.height(12.dp))

        val progressValue = engineStatus.enabledFeaturesCount.toFloat() / engineStatus.totalFeatures.toFloat()
        Text("已启用 ${engineStatus.enabledFeaturesCount}/${engineStatus.totalFeatures}", fontSize = 12.sp, color = TechBlue)
        LinearProgressIndicator(progress = { progressValue }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = TechBlue, trackColor = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("实时工作状态", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow("引擎负载", "${engineStatus.cpuLoad}%", TechBlue)
                InfoRow("内核注入", if (engineStatus.isKprobeActive) "活跃" else "未激活", if (engineStatus.isKprobeActive) Color(0xFF10B981) else Color(0xFFEF4444))
                InfoRow("内核版本", engineStatus.kernelVersion, Color.Gray)
                InfoRow("SELinux", engineStatus.selinuxMode, Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Boot镜像管理", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow("当前模式", engineStatus.bootMode, TechBlue)
                InfoRow("镜像状态", engineStatus.bootStatus, if (engineStatus.bootStatus == "已修补") Color(0xFF10B981) else Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                if (isPatching) {
                    LinearProgressIndicator(progress = { patchProgress / 100f }, modifier = Modifier.fillMaxWidth().height(4.dp), color = TechBlue)
                    Text("修补进度: $patchProgress%", fontSize = 11.sp, color = TechBlue, modifier = Modifier.padding(top = 4.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(onClick = {
                    if (RootManager.isRooted()) {
                        scope.launch {
                            isPatching = true
                            val success = OpenCoreEngine.patchBootImage { patchProgress = it }
                            isPatching = false
                            if (success) Toast.makeText(context, "修补成功，重启生效", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "需要 Root 权限", Toast.LENGTH_SHORT).show()
                    }
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = TechBlue), shape = RoundedCornerShape(12.dp), enabled = !isPatching) {
                    Text(if (isPatching) "修补中..." else "一键修补 Boot 镜像", color = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("OpenCore v14.0 | 稳定版", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Text(value, fontSize = 14.sp, color = valueColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AnimatedLoadIndicator(load: Int) {
    val animatedProgress by animateFloatAsState(targetValue = load / 100f, animationSpec = tween(500), label = "load")
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Text("引擎负载", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                Text("$load%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TechBlue)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(progress = { animatedProgress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = TechBlue, trackColor = MaterialTheme.colorScheme.surface)
        }
    }
}
