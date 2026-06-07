package com.opencore.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import com.opencore.app.engine.OpenCoreEngine
import com.opencore.app.ui.theme.TechBlue
import com.opencore.app.ui.theme.ThemeViewModel
import com.opencore.app.utils.RootManager
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(themeViewModel: ThemeViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val engineStatus by OpenCoreEngine.status.collectAsState()
    
    var patchProgress by remember { mutableStateOf(0) }
    var isPatching by remember { mutableStateOf(false) }
    var showRootDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        OpenCoreEngine.startMonitoring()
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: androidx.lifecycle.LifecycleOwner) {
                OpenCoreEngine.stopMonitoring()
            }
        })
    }
    
    LaunchedEffect(Unit) {
        if (!RootManager.isRooted()) {
            showRootDialog = true
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("OpenCore 控制中心", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("OpenCore v8.0 Native Engine | 运行中 | 53项特性", fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        
        if (!RootManager.isRooted()) {
            Surface(color = Color(0xFFEF4444).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                Text("⚠️ 未检测到 Root 权限，部分功能不可用", fontSize = 12.sp, color = Color(0xFFEF4444), modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        AnimatedLoadIndicator(engineStatus.cpuLoad)
        Spacer(modifier = Modifier.height(12.dp))
        
        val progressValue = engineStatus.enabledFeaturesCount.toFloat() / engineStatus.totalFeatures.toFloat()
        Text("已启用 ${engineStatus.enabledFeaturesCount}/${engineStatus.totalFeatures}", fontSize = 12.sp, color = TechBlue)
        LinearProgressIndicator(progress = { progressValue }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = TechBlue)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("实时工作状态", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow("引擎负载", "${engineStatus.cpuLoad}%", TechBlue)
                Spacer(modifier = Modifier.height(4.dp))
                InfoRow("底层服务", if (engineStatus.isServiceRunning) "运行中" else "已停止", if (engineStatus.isServiceRunning) Color(0xFF10B981) else Color(0xFFEF4444))
                Spacer(modifier = Modifier.height(4.dp))
                InfoRow("内核注入", if (engineStatus.isKprobeActive) "活跃" else "未激活", if (engineStatus.isKprobeActive) Color(0xFF10B981) else Color(0xFFEF4444))
                Spacer(modifier = Modifier.height(4.dp))
                InfoRow("内核版本", engineStatus.kernelVersion, Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Boot镜像管理", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow("当前模式", engineStatus.bootMode, TechBlue)
                Spacer(modifier = Modifier.height(4.dp))
                InfoRow("镜像状态", engineStatus.bootStatus, if (engineStatus.bootStatus == "已修补") Color(0xFF10B981) else Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                if (isPatching) {
                    LinearProgressIndicator(progress = { patchProgress / 100f }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)), color = TechBlue)
                    Text("修补进度: $patchProgress%", fontSize = 11.sp, color = TechBlue, modifier = Modifier.padding(top = 4.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(onClick = {
                    if (RootManager.isRooted()) {
                        scope.launch {
                            isPatching = true
                            val success = OpenCoreEngine.patchBootImage { progress -> patchProgress = progress }
                            isPatching = false
                            if (success) patchProgress = 100
                        }
                    }
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = TechBlue), shape = RoundedCornerShape(12.dp), enabled = !isPatching && RootManager.isRooted()) {
                    Text(if (isPatching) "修补中..." else "一键修补 Boot 镜像", color = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("OpenCore v8.0.0 | 真实交互", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth())
    }
    
    if (showRootDialog) {
        AlertDialog(onDismissRequest = { showRootDialog = false }, title = { Text("需要 Root 权限") }, text = { Text("OpenCore 需要 Root 权限才能完整运行。") }, confirmButton = { TextButton(onClick = { showRootDialog = false }) { Text("我知道了", color = TechBlue) } })
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
                Text("引擎负载", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("$load%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TechBlue)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(progress = { animatedProgress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = TechBlue)
        }
    }
}
