package com.opencore.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencore.app.R
import com.opencore.app.utils.LogHelper

data class ModuleItem(
    val id: Int,
    val name: String,
    val desc: String,
    val group: String,
    var enabled: Boolean,
    val iconRes: Int
)

@Composable
fun ModulesScreen() {
    var modules by remember {
        mutableStateOf(
            listOf(
                ModuleItem(0, "设备伪装", "修改系统设备参数绕过检测", "device", false, R.drawable.ic_settings),
                ModuleItem(1, "内核管控", "kprobe注入与内核权限认证", "kernel", true, R.drawable.ic_modules),
                ModuleItem(2, "SELinux规则", "动态放行/拦截SELinux权限", "selinux", false, R.drawable.ic_settings),
                ModuleItem(3, "SU权限体系", "自研Root权限管理", "selinux", true, R.drawable.ic_modules),
                ModuleItem(4, "虚拟机隔离", "应用运行环境隔离防检测", "selinux", false, R.drawable.ic_settings),
                ModuleItem(5, "日志管控", "屏蔽系统日志输出", "system", true, R.drawable.ic_modules),
                ModuleItem(6, "分区挂载引擎", "自定义分区挂载与卸载", "system", false, R.drawable.ic_settings),
                ModuleItem(7, "OTA拦截", "屏蔽系统OTA升级", "system", true, R.drawable.ic_modules),
                ModuleItem(8, "缓存清理", "自动清理系统缓存", "system", false, R.drawable.ic_settings),
                ModuleItem(9, "分区修复", "自动修复分区异常", "system", false, R.drawable.ic_settings)
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groupedModules.forEach { (group, groupModules) ->
            item {
                Text(
                    text = groupTitles[group] ?: group,
                    fontSize = 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
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
                        LogHelper.addLog("Modules", "${module.name} ${if (enabled) "启用" else "禁用"}")
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
    val borderColor by animateColorAsState(
        targetValue = if (checked) TechBlue else Color.Transparent,
        animationSpec = tween(200)
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = module.iconRes),
                    contentDescription = null,
                    tint = if (checked) TechBlue else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = module.name,
                        fontSize = 15.sp,
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

private val TechBlue = Color(0xFF2563EB)
