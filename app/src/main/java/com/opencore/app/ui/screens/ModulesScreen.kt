package com.opencore.app.ui.screens

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
import com.opencore.app.engine.Module
import com.opencore.app.engine.ModuleManager
import com.opencore.app.ui.theme.TechBlue
import com.opencore.app.utils.RootManager
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun ModulesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val modules = remember { ModuleManager.getAllModules().toMutableStateList() }
    val groupedModules = remember(modules) { ModuleManager.getModulesByCategory() }
    
    var showRootRequestDialog by rememberSaveable { mutableStateOf(!RootManager.isRooted()) }
    
    // Root 权限请求启动器
    val rootRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // 权限请求返回后重新检查
        scope.launch {
            kotlinx.coroutines.delay(500)
            if (RootManager.isRooted()) {
                showRootRequestDialog = false
                Toast.makeText(context, "Root 权限已获取", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Root 权限获取失败，请确保已安装 Magisk 并授权", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    // 请求 Root 权限
    fun requestRoot(activity: androidx.activity.ComponentActivity) {
        RootManager.requestRootPermission(activity) { granted ->
            if (granted) {
                showRootRequestDialog = false
                Toast.makeText(context, "Root 权限已授予", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "需要 Root 权限才能导入模块", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // 导入模块的方法
    fun importModule(module: Module) {
        scope.launch {
            if (!RootManager.isRooted()) {
                Toast.makeText(context, "请先授予 Root 权限", Toast.LENGTH_SHORT).show()
                return@launch
            }
            
            // 模拟导入过程
            Toast.makeText(context, "正在导入 ${module.name}...", Toast.LENGTH_SHORT).show()
            kotlinx.coroutines.delay(1000)
            
            val success = ModuleManager.setModuleEnabled(module.id, true)
            if (success) {
                val index = modules.indexOfFirst { it.id == module.id }
                if (index >= 0) modules[index] = modules[index].copy(isEnabled = true)
                Toast.makeText(context, "${module.name} 导入成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "${module.name} 导入失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // 卸载模块
    fun uninstallModule(module: Module) {
        scope.launch {
            if (!RootManager.isRooted()) {
                Toast.makeText(context, "请先授予 Root 权限", Toast.LENGTH_SHORT).show()
                return@launch
            }
            
            val success = ModuleManager.setModuleEnabled(module.id, false)
            if (success) {
                val index = modules.indexOfFirst { it.id == module.id }
                if (index >= 0) modules[index] = modules[index].copy(isEnabled = false)
                Toast.makeText(context, "${module.name} 已卸载", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "${module.name} 卸载失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Root 权限提示卡片
        if (!RootManager.isRooted()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("需要 Root 权限", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("导入模块需要 Root 权限，请点击下方按钮授权", fontSize = 13.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val activity = context as? androidx.activity.ComponentActivity
                                if (activity != null) {
                                    requestRoot(activity)
                                } else {
                                    Toast.makeText(context, "无法获取 Activity", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("授予 Root 权限")
                        }
                    }
                }
            }
        }
        
        // 统计卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("模块库", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Surface(shape = RoundedCornerShape(12.dp), color = TechBlue.copy(alpha = 0.15f)) {
                            Text("共 ${modules.size} 个可用模块", fontSize = 12.sp, color = TechBlue, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("点击「导入」按钮将模块安装到系统中", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
        
        // 按分组显示可导入的模块
        groupedModules.forEach { (category, categoryModules) ->
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = TechBlue.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(category, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TechBlue)
                        Text("${categoryModules.count { it.isEnabled }} 已导入", fontSize = 12.sp, color = TechBlue)
                    }
                }
            }
            
            items(categoryModules) { module ->
                ModuleImportCard(
                    module = module,
                    onImport = { importModule(module) },
                    onUninstall = { uninstallModule(module) }
                )
            }
        }
    }
}

@Composable
fun ModuleImportCard(
    module: Module,
    onImport: () -> Unit,
    onUninstall: () -> Unit
) {
    val isImported = module.isEnabled
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isImported) TechBlue.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 图标
                Card(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isImported) TechBlue.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            getModuleIcon(module.id),
                            contentDescription = null,
                            tint = if (isImported) TechBlue else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Column {
                    Text(
                        module.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isImported) TechBlue else MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        module.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }
            
            // 导入/卸载按钮
            if (isImported) {
                OutlinedButton(
                    onClick = onUninstall,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFEF4444)
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("卸载", fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = onImport,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                    modifier = Modifier.width(80.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("导入", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun getModuleIcon(id: Int) = when (id) {
    0 -> Icons.Default.DeviceHub
    1 -> Icons.Default.Memory
    2 -> Icons.Default.Security
    3 -> Icons.Default.AdminPanelSettings
    4 -> Icons.Default.Storage
    5 -> Icons.Default.ListAlt
    6 -> Icons.Default.SdStorage
    7 -> Icons.Default.Update
    8 -> Icons.Default.CleaningServices
    9 -> Icons.Default.BugReport
    else -> Icons.Default.Apps
}
