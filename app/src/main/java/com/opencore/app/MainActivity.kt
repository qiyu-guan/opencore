package com.opencore.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.opencore.app.ui.components.BottomNavigationBar
import com.opencore.app.ui.screens.HomeScreen
import com.opencore.app.ui.screens.LogScreen
import com.opencore.app.ui.screens.ModulesScreen
import com.opencore.app.ui.screens.SettingsScreen
import com.opencore.app.ui.theme.OpenCoreTheme
import com.opencore.app.ui.theme.TechBlue
import com.opencore.app.ui.theme.ThemeViewModel
import com.opencore.app.utils.LogHelper

class MainActivity : ComponentActivity() {
    
    init {
        System.loadLibrary("native-lib")
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogHelper.init(this)
        
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            OpenCoreTheme(darkTheme = themeViewModel.isDarkTheme.value) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
            bottomBar = {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { index ->
                        selectedTab = index
                        when (index) {
                            0 -> navController.navigate("home") { popUpTo("home") { inclusive = true } }
                            1 -> navController.navigate("log") { popUpTo("log") { inclusive = true } }
                            2 -> navController.navigate("modules") { popUpTo("modules") { inclusive = true } }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("settings") },
                    containerColor = TechBlue,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    modifier = Modifier.padding(end = 16.dp, top = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "设置",
                        tint = Color.White
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.EndTop
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") { HomeScreen(themeViewModel) }
                composable("log") { LogScreen() }
                composable("modules") { ModulesScreen() }
                composable("settings") { SettingsScreen(themeViewModel) }
            }
        }
    }
}
