package com.opencore.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencore.app.ui.theme.TechBlue

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf("主页", "日志", "模块")
    val icons = listOf(
        Icons.Default.Dashboard,
        Icons.Default.List,
        Icons.Default.ViewModule
    )
    
    NavigationBar(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
        containerColor = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        items.forEachIndexed { index, title ->
            val selected = selectedTab == index
            val animatedColor = animateColorAsState(
                targetValue = if (selected) TechBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                animationSpec = tween(200)
            )
            
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = icons[index],
                        contentDescription = title,
                        tint = animatedColor.value,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        color = animatedColor.value
                    )
                }
            )
        }
    }
}
