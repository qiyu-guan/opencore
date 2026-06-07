package com.opencore.app.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencore.app.engine.Module
import com.opencore.app.engine.ModuleManager
import com.opencore.app.ui.theme.TechBlue
import com.opencore.app.utils.RootManager
import kotlinx.coroutines.launch

@Composable
fun ModulesScreen() {
    val scope = rememberCoroutineScope()
    val modules = remember { ModuleManager.getAllModules().toMutableStateList() }
    val groupedModules = remember(modules) { ModuleManager.getModulesByCategory() }
    
    val totalModules = modules.size
    val enabledModules = modules.count { it.isEnabled }
    val enabledPercent = if (totalModules > 0) (enabledModules * 100 / totalModules) else 0
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                        Text("模块统计", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TechBlue.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "共 $totalModules 个模块",
                                fontSize = 12.sp,
                                color = TechBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("已启用", fontSize = 14.sp)
                        Text(
                            "$enabledModules / $totalModules",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechBlue
                        )
                    }
                    LinearProgressIndicator(
                        progress = { enabledPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = TechBlue
                    )
                }
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            modules.forEach { module ->
                                if (!module.isEnabled && (!module.needsRoot || RootManager.isRooted())) {
                                    ModuleManager.setModuleEnabled(module.id, true)
                                    val index = modules.indexOfFirst { it.id == module.id }
                                    if (index >= 0) modules[index] = modules[index].copy(isEnabled = true)
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("全部启用", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            modules.forEach { module ->
                                if (module.isEnabled) {
                                    ModuleManager.setModuleEnabled(module.id, false)
                                    val index = modules.indexOfFirst { it.id == module.id }
                                    if (index >= 0) modules[index] = modules[index].copy(isEnabled = false)
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("全部禁用", fontSize = 13.sp)
                }
            }
        }
        
        groupedModules.forEach { (category, categoryModules) ->
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = TechBlue.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(category, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TechBlue)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TechBlue.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "${categoryModules.count { it.isEnabled }}/${categoryModules.size}",
                                fontSize = 11.sp,
                                color = TechBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            items(categoryModules) { module ->
                ModuleCard(
                    module = module,
                    onToggle = { enabled ->
                        scope.launch {
                            val success = ModuleManager.setModuleEnabled(module.id, enabled)
                            if (success) {
                                val index = modules.indexOfFirst { it.id == module.id }
                                if (index >= 0) modules[index] = modules[index].copy(isEnabled = enabled)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ModuleCard(
    module: Module,
    onToggle: (Boolean) -> Unit
) {
    var isEnabled by remember(module.isEnabled) { mutableStateOf(module.isEnabled) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) TechBlue.copy(alpha = 0.12f)
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
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isEnabled) TechBlue.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        getModuleIcon(module.id),
                        contentDescription = null,
                        tint = if (isEnabled) TechBlue else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        module.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isEnabled) TechBlue else MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        module.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }
            
            if (module.needsRoot && !RootManager.isRooted()) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "需要Root",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { newValue ->
                        isEnabled = newValue
                        onToggle(newValue)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = TechBlue
                    )
                )
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
