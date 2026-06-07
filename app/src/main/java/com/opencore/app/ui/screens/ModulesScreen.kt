package com.opencore.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.opencore.app.engine.ModuleInstaller
import com.opencore.app.engine.ModuleManager
import com.opencore.app.engine.ModuleRepository
import com.opencore.app.ui.theme.TechBlue
import com.opencore.app.utils.RootManager
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("已安装", "在线库")

    var installedModules by remember { mutableStateOf(listOf<com.opencore.app.engine.InstalledModule>()) }
    var isLoading by remember { mutableStateOf(false) }
    var installing by remember { mutableStateOf(false) }
    var installProgress by remember { mutableStateOf("") }

    // 刷新模块列表
    fun refreshModules() {
        scope.launch {
            isLoading = true
            ModuleManager.loadInstalledModules()
            installedModules = ModuleManager.getInstalledModules()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refreshModules() }

    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                installing = true
                installProgress = "正在复制..."
                val success = ModuleInstaller.installFromZip(context, uri) { msg -> installProgress = msg }
                if (success) {
                    refreshModules()
                    Toast.makeText(context, "安装成功，重启后生效", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "安装失败: $installProgress", Toast.LENGTH_SHORT).show()
                }
                installing = false
                installProgress = ""
            }
        }
    }

    fun downloadAndInstall(remote: com.opencore.app.engine.RemoteModule) {
        scope.launch {
            if (!RootManager.isRooted()) {
                Toast.makeText(context, "需要 Root 权限", Toast.LENGTH_SHORT).show()
                return@launch
            }
            installing = true
            installProgress = "下载中 0%"
            val tempFile = java.io.File(context.cacheDir, "${remote.id}.zip")
            val success = ModuleRepository.downloadModule(remote.downloadUrl, tempFile) { progress ->
                installProgress = "下载中 $progress%"
            }
            if (success) {
                installProgress = "安装中..."
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )
                val installOk = ModuleInstaller.installFromZip(context, uri) { msg -> installProgress = msg }
                if (installOk) {
                    refreshModules()
                    Toast.makeText(context, "安装成功，重启生效", Toast.LENGTH_LONG).show()
                }
                tempFile.delete()
            } else {
                Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show()
            }
            installing = false
            installProgress = ""
        }
    }

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0 && !installing) {
                FloatingActionButton(
                    onClick = { zipPickerLauncher.launch("application/zip") },
                    containerColor = TechBlue,
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "导入本地模块")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (installing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(installProgress)
                    }
                }
            } else {
                when (selectedTab) {
                    0 -> InstalledModulesTab(installedModules, isLoading, refreshModules)
                    1 -> OnlineModulesTab(::downloadAndInstall)
                }
            }
        }
    }
}

@Composable
fun InstalledModulesTab(modules: List<com.opencore.app.engine.InstalledModule>, isLoading: Boolean, onRefresh: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else if (modules.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("没有已安装的 Magisk 模块", fontSize = 14.sp, color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(modules) { module ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(module.name, fontWeight = FontWeight.Medium)
                            Text(module.id, fontSize = 11.sp, color = Color.Gray)
                            Text(module.version, fontSize = 10.sp, color = Color.Gray)
                        }
                        OutlinedButton(onClick = {
                            scope.launch {
                                val success = ModuleInstaller.uninstallModule(module.id) { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                                if (success) onRefresh()
                            }
                        }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("卸载", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnlineModulesTab(onInstall: (com.opencore.app.engine.RemoteModule) -> Unit) {
    val remoteModules = ModuleRepository.availableModules
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(remoteModules) { module ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(module.name, fontWeight = FontWeight.Medium)
                        Text(module.description, fontSize = 12.sp, color = Color.Gray)
                        Text("版本: ${module.version}", fontSize = 10.sp)
                    }
                    Button(onClick = { onInstall(module) }, colors = ButtonDefaults.buttonColors(containerColor = TechBlue), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Download, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("安装", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
