package com.opencore.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencore.app.ui.theme.TechBlue
import com.opencore.app.utils.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var partitions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedPartitions by remember { mutableStateOf<Set<String>>(emptySet()) }
    var backupProgress by remember { mutableStateOf(0) }
    var isBackingUp by remember { mutableStateOf(false) }
    var outputDir by remember { mutableStateOf("/sdcard/Download/backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}") }

    LaunchedEffect(Unit) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val result = RootManager.execRoot("ls /dev/block/by-name/ 2>/dev/null")
            if (result.isSuccess) {
                partitions = result.out.filter { it.isNotBlank() }
            }
            if (partitions.isEmpty()) {
                val result2 = RootManager.execRoot("ls /dev/block/platform/*/by-name/ 2>/dev/null")
                if (result2.isSuccess) {
                    partitions = result2.out.filter { it.isNotBlank() }
                }
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("字库备份", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2563EB)),
                navigationIcon = {
                    IconButton(onClick = { (context as? androidx.activity.ComponentActivity)?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("选择要备份的分区", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("备份文件将保存到: $outputDir", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (partitions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("未找到任何分区，请确保已 Root 并拥有 /dev/block 访问权限", fontSize = 14.sp, color = Color.Gray)
                }
            } else {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("共 ${partitions.size} 个分区", fontSize = 12.sp, color = Color.Gray)
                    TextButton(onClick = {
                        selectedPartitions = if (selectedPartitions.size == partitions.size) emptySet() else partitions.toSet()
                    }) {
                        Text(if (selectedPartitions.size == partitions.size) "取消全选" else "全选", color = TechBlue)
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(partitions) { partition ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedPartitions.contains(partition)) TechBlue.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(partition, fontSize = 14.sp)
                                Checkbox(
                                    checked = selectedPartitions.contains(partition),
                                    onCheckedChange = { isChecked ->
                                        selectedPartitions = if (isChecked) {
                                            selectedPartitions + partition
                                        } else {
                                            selectedPartitions - partition
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = TechBlue)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isBackingUp) {
                    LinearProgressIndicator(progress = { backupProgress.toFloat() / 100f }, modifier = Modifier.fillMaxWidth().height(6.dp), color = TechBlue)
                    Text("备份进度: $backupProgress%", fontSize = 12.sp, color = TechBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        if (selectedPartitions.isEmpty()) {
                            Toast.makeText(context, "请至少选择一个分区", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!RootManager.isRooted()) {
                            Toast.makeText(context, "需要 Root 权限", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch {
                            isBackingUp = true
                            backupProgress = 0
                            val total = selectedPartitions.size
                            var current = 0
                            val dir = File(outputDir)
                            if (!dir.exists()) dir.mkdirs()
                            for (partition in selectedPartitions) {
                                val result = RootManager.execRoot("dd if=/dev/block/by-name/$partition of=$outputDir/${partition}.img")
                                current++
                                backupProgress = (current * 100 / total)
                                if (result.isSuccess) {
                                    Toast.makeText(context, "${partition} 备份完成", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "${partition} 备份失败: ${result.err.joinToString()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            isBackingUp = false
                            Toast.makeText(context, "全部备份完成，文件保存在 $outputDir", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isBackingUp && selectedPartitions.isNotEmpty()
                ) {
                    Text(if (isBackingUp) "备份中..." else "开始备份", color = Color.White)
                }
            }
        }
    }
}
