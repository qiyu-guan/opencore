package com.opencore.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.opencore.app.engine.OpenCoreEngine
import com.opencore.app.engine.ModuleManager
import com.opencore.app.ui.screens.HomeScreen
import com.opencore.app.ui.screens.LogScreen
import com.opencore.app.ui.screens.ModulesScreen
import com.opencore.app.ui.screens.SettingsScreen
import com.opencore.app.ui.theme.OpenCoreTheme
import com.opencore.app.ui.theme.TechBlue
import com.opencore.app.ui.theme.ThemeViewModel
import com.opencore.app.ui.theme.ThemeViewModelFactory
import com.opencore.app.utils.LogHelper
import com.opencore.app.utils.RootManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogHelper.init(this)
        RootManager.init(this)
        OpenCoreEngine.init(this)
        ModuleManager.init(this)

        setContent {
            val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(applicationContext))
            OpenCoreTheme(darkTheme = themeViewModel.isDarkTheme.value) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(themeViewModel)
                }
            }
        }
    }

    @Composable
    fun AppNavigation(themeViewModel: ThemeViewModel) {
        val navController = rememberNavController()
        var selectedTab by remember { mutableStateOf(0) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("OpenCore", color = Color.White) },
                    actions = {
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(Icons.Default.Settings, contentDescription = "设置", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = TechBlue)
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    tonalElevation = 0.dp
                ) {
                    val items = listOf("主页", "日志", "模块")
                    val icons = listOf(Icons.Default.Home, Icons.Default.List, Icons.Default.Apps)
                    items.forEachIndexed { index, title ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                when (index) {
                                    0 -> navController.navigate("home") { popUpTo("home") { inclusive = true } }
                                    1 -> navController.navigate("log") { popUpTo("log") { inclusive = true } }
                                    2 -> navController.navigate("modules") { popUpTo("modules") { inclusive = true } }
                                }
                            },
                            icon = { Icon(icons[index], contentDescription = title) },
                            label = { Text(title, fontSize = 12.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TechBlue,
                                selectedTextColor = TechBlue
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(navController = navController, startDestination = "home", modifier = Modifier.padding(innerPadding)) {
                composable("home") { HomeScreen(themeViewModel) }
                composable("log") { LogScreen() }
                composable("modules") { ModulesScreen() }
                composable("settings") { SettingsScreen(themeViewModel) }
            }
        }
    }
}
