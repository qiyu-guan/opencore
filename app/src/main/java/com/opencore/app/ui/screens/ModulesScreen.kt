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
import com.opencore.app.ui.theme.TechBlue

data class ModuleItem(
    val id: Int,
    val name: String,
    val desc: String,
    val group: String,
    var enabled: Boolean,
    val icon: ImageVector
)

@Composable
fun ModulesScreen() {
    var modules by remember {
        mutableStateOf(
            listOf(
                ModuleItem(0, "设备伪装", "修改系统设备参数绕过检测", "device", false, Icons.Default.DeviceHub),
                ModuleItem(1, "内核管控", "kprobe注入与内核权限认证", "kernel", true, Icons.Default.Memory),
                ModuleItem(2, "SELinux规则", "动态放行/拦截SELinux权限", "selinux", false, Icons.Default.Security),
                ModuleItem(3, "SU权限体系", "自研Root权限管理", "selinux", true, Icons.Default.AdminPanelSettings),
                ModuleItem(4, "虚拟机隔离", "应用运行环境隔离防检测", "selinux", false, Icons.Default.Storage),
                ModuleItem(5, "日志管控", "屏蔽系统日志输出", "system", true, Icons.Default.ListAlt),
                ModuleItem(6, "分区挂载引擎", "自定义分区挂载与卸载", "system", false, Icons.Default.SdStorage),
                ModuleItem(7, "OTA拦截", "屏蔽系统OTA升级", "system", true, Icons.Default.Update),
                ModuleItem(8, "缓存清理", "自动清理系统缓存", "system", false, Icons.Default.CleaningServices),
                ModuleItem(9, "分区修复", "自动修复分区异常", "system", false, Icons.Default.BugReport)
            )
        )
    }
    
    val groupedModules = modules.groupBy { it.group }
    val groupTitles = mapOf(
        "device" to "设备伪装组",
        "kernel" to "内核管控组",
        "selinux" to "SELinux与权限组",
        "system" to "系统维护组"
    )
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        groupedModules.forEach { (group, groupModules) ->
            item {
                Text(
                    text = groupTitles[group] ?: group,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            
            items(groupModules) { module ->
                ModuleCard(
                    module = module,
                    onToggle = { enabled ->
                        modules = modules.map {
                            if (it.id == module.id) it.copy(enabled = enabled) else it
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ModuleCard(
    module: ModuleItem,
    onToggle: (Boolean) -> Unit
) {
    var checked by remember(module.enabled) { mutableStateOf(module.enabled) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = module.icon,
                    contentDescription = null,
                    tint = if (checked) TechBlue else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
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
                        text = module.desc,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
            
            Switch(
                checked = checked,
                onCheckedChange = {
                    checked = it
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
