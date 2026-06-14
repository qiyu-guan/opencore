package com.opencore.app.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencore.app.utils.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast
import java.io.File
import android.content.pm.PackageManager

data class AppAuthInfo(
    val packageName: String,
    val appName: String,
    val isGranted: Boolean,
    val iconBitmap: Bitmap?
)

fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootAuthScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var appList by remember { mutableStateOf<List<AppAuthInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            val apps = mutableListOf<AppAuthInfo>()
            for (pkg in packages) {
                try {
                    val appInfo = pm.getApplicationInfo(pkg.packageName, 0)
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    val isGranted = checkRootGranted(pkg.packageName)
                    val icon = try {
                        val drawable = pm.getApplicationIcon(appInfo)
                        drawableToBitmap(drawable)
                    } catch (e: Exception) { null }
                    apps.add(AppAuthInfo(pkg.packageName, appName, isGranted, icon))
                } catch (e: Exception) { }
            }
            appList = apps.sortedBy { it.appName.lowercase() }
        }
        isLoading = false
    }

    val filteredApps = if (searchQuery.isEmpty()) appList else appList.filter {
        it.appName.contains(searchQuery, ignoreCase = true) ||
        it.packageName.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Root 权限管理", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2563EB))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜索应用") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("总计", fontSize = 12.sp, color = Color.Gray)
                        Text("${filteredApps.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("已授权", fontSize = 12.sp, color = Color(0xFF10B981))
                        Text("${filteredApps.count { it.isGranted }}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("未授权", fontSize = 12.sp, color = Color(0xFFEF4444))
                        Text("${filteredApps.count { !it.isGranted }}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredApps) { app ->
                        AppAuthCard(
                            app = app,
                            onToggle = { newState ->
                                scope.launch {
                                    if (newState) {
                                        grantRootPermission(app.packageName)
                                        Toast.makeText(context, "${app.appName} 已获得 Root 权限", Toast.LENGTH_SHORT).show()
                                    } else {
                                        revokeRootPermission(app.packageName)
                                        Toast.makeText(context, "${app.appName} 已撤销 Root 权限", Toast.LENGTH_SHORT).show()
                                    }
                                    appList = appList.map {
                                        if (it.packageName == app.packageName) it.copy(isGranted = newState) else it
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppAuthCard(
    app: AppAuthInfo,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (app.isGranted) Color(0xFF2563EB).copy(alpha = 0.15f)
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
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (app.iconBitmap != null) {
                    Image(
                        bitmap = app.iconBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(40.dp))
                }

                Column {
                    Text(
                        text = app.appName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (app.isGranted) Color(0xFF2563EB) else MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = app.packageName,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }

            Switch(
                checked = app.isGranted,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF2563EB)
                )
            )
        }
    }
}

fun checkRootGranted(packageName: String): Boolean {
    return try {
        val suPath = arrayOf(
            "/data/data/com.topjohnwu.magisk/su",
            "/system/bin/su",
            "/system/xbin/su"
        )
        for (path in suPath) {
            if (File(path).exists()) {
                val process = Runtime.getRuntime().exec(arrayOf(path, "-c", "id $packageName"))
                val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
                val output = reader.readText()
                process.waitFor()
                if (output.contains("uid=0")) return true
            }
        }
        false
    } catch (e: Exception) {
        false
    }
}

suspend fun grantRootPermission(packageName: String) {
    withContext(Dispatchers.IO) {
        try {
            RootManager.execRoot("magiskpolicy --live \"allow $packageName * * *\" 2>/dev/null")
            RootManager.execRoot("echo '$packageName' >> /data/adb/magisk/whitelist 2>/dev/null")
            RootManager.execRoot("echo '#!/system/bin/sh\nexec /system/bin/su -c \"\$@\"' > /data/local/tmp/su_$packageName.sh && chmod 755 /data/local/tmp/su_$packageName.sh")
        } catch (e: Exception) { }
    }
}

suspend fun revokeRootPermission(packageName: String) {
    withContext(Dispatchers.IO) {
        try {
            RootManager.execRoot("sed -i '/$packageName/d' /data/adb/magisk/whitelist 2>/dev/null")
            RootManager.execRoot("rm -f /data/local/tmp/su_$packageName.sh")
        } catch (e: Exception) { }
    }
}
