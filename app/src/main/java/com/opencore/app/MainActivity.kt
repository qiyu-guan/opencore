package com.opencore.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.opencore.app.ui.screens.HomeScreen
import com.opencore.app.ui.screens.LogScreen
import com.opencore.app.ui.screens.ModulesScreen
import com.opencore.app.ui.screens.SettingsScreen
import com.opencore.app.ui.theme.OpenCoreTheme
import com.opencore.app.ui.theme.TechBlue
import com.opencore.app.ui.theme.ThemeViewModel
import com.opencore.app.utils.LogHelper
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    
    init {
        System.loadLibrary("native-lib")
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogHelper.init(this)
        
        setContent {
            OpenCoreApp()
        }
    }
    
    @Composable
    fun OpenCoreApp() {
        val themeViewModel: ThemeViewModel = viewModel(
            factory = ThemeViewModelFactory.Factory(applicationContext)
        )
        
        OpenCoreTheme(darkTheme = themeViewModel.isDarkTheme.value) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = androidx.compose.material3.MaterialTheme.colorScheme.background
            ) {
                AppNavigation(themeViewModel)
            }
        }
    }
    
    @Composable
    fun AppNavigation(themeViewModel: ThemeViewModel) {
        val navController = rememberNavController()
        var selectedTab by remember { mutableStateOf(0) }
        
        Scaffold(
            bottomBar = {
                com.opencore.app.ui.components.BottomNavigationBar(
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
                    modifier = androidx.compose.ui.Modifier
                        .padding(end = 16.dp, top = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "设置",
                        tint = Color.White
                    )
                }
            },
            floatingActionButtonPosition = androidx.compose.material3.FabPosition.EndTop
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
