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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencore.app.engine.ModuleManager
import com.opencore.app.engine.Module
import com.opencore.app.ui.theme.TechBlue
import com.opencore.app.utils.RootManager
import kotlinx.coroutines.launch

@Composable
fun ModulesScreen() {
    val scope = rememberCoroutineScope()
    val modules = remember { ModuleManager.getAllModules().toMutableStateList() }
    val groupedModules = remember(modules) { ModuleManager.getModulesByCategory() }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedModules.forEach { (category, categoryModules) ->
            item {
                Text(
                    text = category,
                    fontSize = 14.sp,
                    color = TechBlue,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            
            items(categoryModules) { module ->
                ModuleCard(
                    module = module,
                    onToggle = { enabled ->
                        scope.launch {
                            val success = ModuleManager.setModuleEnabled(module.id, enabled)
                            if (success) {
                                val index = modules.indexOfFirst { it.id == module.id }
                                if (index >= 0) {
                                    modules[index] = modules[index].copy(isEnabled = enabled)
                                }
                            }
                        }
                    }
                )
            }
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) TechBlue.copy(alpha = 0.15f) 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = getModuleIcon(module.id),
                    contentDescription = null,
                    tint = if (isEnabled) TechBlue else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = module.name,
                        fontSize = 14.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = module.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }
            
            if (module.needsRoot && !RootManager.isRooted()) {
                Icon(Icons.Default.Lock, contentDescription = "需要Root", tint = Color.Gray, modifier = Modifier.size(20.dp))
            } else {
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { 
                        isEnabled = it
                        onToggle(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = TechBlue,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
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
