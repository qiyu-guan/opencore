package com.opencore.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencore.app.engine.ModuleManager
import com.opencore.app.engine.Module
import com.opencore.app.ui.theme.TechBlue
import com.opencore.app.utils.RootManager
import com.opencore.app.utils.LogHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulesScreen() {
    val scope = rememberCoroutineScope()
    val modules = remember { ModuleManager.getAllModules().toMutableStateList() }
    val groupedModules = remember(modules) { ModuleManager.getModulesByCategory() }
    
    // 统计信息
    val totalModules = modules.size
    val enabledModules = modules.count { it.isEnabled }
    val enabledPercent = if (totalModules > 0) (enabledModules * 100 / totalModules) else 0
    
    // 分类折叠状态
    val expandedStates = remember { mutableStateMapOf<String, Boolean>().apply {
        groupedModules.keys.forEach { put(it, true) }
    } }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 顶部统计卡片
        item {
            StatsCard(totalModules, enabledModules, enabledPercent)
        }
        
        // 快速操作按钮
        item {
            QuickActionsCard(
                onEnableAll = {
                    scope.launch {
                        modules.forEach { module ->
                            if (!module.isEnabled && (!module.needsRoot || RootManager.isRooted())) {
                                ModuleManager.setModuleEnabled(module.id, true)
                                val index = modules.indexOfFirst { it.id == module.id }
                                if (index >= 0) modules[index] = modules[index].copy(isEnabled = true)
                                delay(50)
                            }
                        }
                    }
                },
                onDisableAll = {
                    scope.launch {
                        modules.forEach { module ->
                            if (module.isEnabled) {
                                ModuleManager.setModuleEnabled(module.id, false)
                                val index = modules.indexOfFirst { it.id == module.id }
                                if (index >= 0) modules[index] = modules[index].copy(isEnabled = false)
                                delay(50)
                            }
                        }
                    }
                }
            )
        }
        
        // 按分组显示模块
        groupedModules.forEach { (category, categoryModules) ->
            item {
                CategoryHeader(
                    category = category,
                    categoryModules = categoryModules,
                    isExpanded = expandedStates[category] ?: true,
                    onToggle = { expandedStates[category] = !(expandedStates[category] ?: true) }
                )
            }
            
            if (expandedStates[category] == true) {
                items(categoryModules) { module ->
                    AnimatedModuleCard(
                        module = module,
                        onToggle = { enabled ->
                            scope.launch {
                                val success = ModuleManager.setModuleEnabled(module.id, enabled)
                                if (success) {
                                    val index = modules.indexOfFirst { it.id == module.id }
                                    if (index >= 0) {
                                        modules[index] = modules[index].copy(isEnabled = enabled)
                                    }
                                    LogHelper.addLog("Modules", "${module.name} ${if (enabled) "启用" else "禁用"}")
                                }
                            }
                        }
                    )
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun StatsCard(total: Int, enabled: Int, percent: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "模块统计",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TechBlue.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "共 $total 个模块",
                        fontSize = 12.sp,
                        color = TechBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("已启用", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Text("$enabled / $total", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TechBlue)
            }
            
            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = TechBlue,
                trackColor = MaterialTheme.colorScheme.surface
            )
            
            Text(
                text = "启用率 $percent%",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun QuickActionsCard(onEnableAll: () -> Unit, onDisableAll: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onEnableAll,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TechBlue
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("全部启用", fontSize = 13.sp)
            }
            
            OutlinedButton(
                onClick = onDisableAll,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("全部禁用", fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun CategoryHeader(
    category: String,
    categoryModules: List<Module>,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val enabledInCategory = categoryModules.count { it.isEnabled }
    val totalInCategory = categoryModules.size
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TechBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = category,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = TechBlue.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "$enabledInCategory/$totalInCategory",
                    fontSize = 11.sp,
                    color = TechBlue,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun AnimatedModuleCard(
    module: Module,
    onToggle: (Boolean) -> Unit
) {
    var isEnabled by remember(module.isEnabled) { mutableStateOf(module.isEnabled) }
    val scope = rememberCoroutineScope()
    
    // 动画效果
    val animatedBorder by animateColorAsState(
        targetValue = if (isEnabled) TechBlue else Color.Transparent,
        animationSpec = tween(300),
        label = "border"
    )
    
    val animatedElevation by animateDpAsState(
        targetValue = if (isEnabled) 4.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "elevation"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(animatedElevation, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) TechBlue.copy(alpha = 0.12f) 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                // 模块图标带动画
                AnimatedModuleIcon(module.id, isEnabled)
                
                Column {
                    Text(
                        text = module.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isEnabled) TechBlue else MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = module.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            
            // 需要 Root 但无权限时显示锁图标
            if (module.needsRoot && !RootManager.isRooted()) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "需要Root权限",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                // 带动画的开关
                AnimatedSwitch(
                    checked = isEnabled,
                    onCheckedChange = { newValue ->
                        isEnabled = newValue
                        onToggle(newValue)
                    }
                )
            }
        }
    }
}

@Composable
fun AnimatedModuleIcon(moduleId: Int, isEnabled: Boolean) {
    val icon = getModuleIcon(moduleId)
    val animatedScale by animateFloatAsState(
        targetValue = if (isEnabled) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isEnabled) TechBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isEnabled) TechBlue else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun AnimatedSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = if (checked) TechBlue else Color(0xFF888888),
        animationSpec = tween(200),
        label = "switchColor"
    )
    
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = animatedColor,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFF444444)
        )
    )
}

@Composable
fun getModuleIcon(id: Int): ImageVector {
    return when (id) {
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
        10 -> Icons.Default.Code
        11 -> Icons.Default.VisibilityOff
        12 -> Icons.Default.Wifi
        13 -> Icons.Default.Extension
        14 -> Icons.Default.Plugin
        else -> Icons.Default.Apps
    }
}
