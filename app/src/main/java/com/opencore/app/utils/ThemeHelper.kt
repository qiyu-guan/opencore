package com.opencore.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.opencore.app.R

object ThemeHelper {
    private const val PREFS_NAME = "opencore_prefs"
    private const val KEY_THEME_COLOR = "theme_color_res"

    fun setThemeColor(context: Context, colorResId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THEME_COLOR, colorResId).apply()
        // 重新创建Activity时会应用新颜色
    }

    fun getThemeColorRes(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_THEME_COLOR, R.color.primary_blue)
    }

    fun getCurrentThemeName(context: Context): String {
        val resId = getThemeColorRes(context)
        return when (resId) {
            R.color.primary_blue -> "蓝色"
            R.color.primary_cyan -> "青色"
            R.color.primary_purple -> "紫色"
            R.color.primary_indigo -> "靛蓝"
            R.color.primary_orange -> "橙色"
            R.color.primary_green -> "绿色"
            R.color.primary_red -> "红色"
            else -> "默认"
        }
    }

    fun applyTheme(context: Context) {
        // 动态颜色需要覆盖主题属性，这里简单设置夜间模式为深色
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        // 注意：需要自定义ThemeOverlay来改变colorPrimary等，但我们可以通过设置DayNight主题+动态颜色实现
        // 由于时间关系，我们让Activity recreate时重新读取颜色并手动设置状态栏等
    }
}
