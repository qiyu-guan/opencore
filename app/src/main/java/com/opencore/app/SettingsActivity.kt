package com.opencore.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.opencore.app.utils.LogHelper
import com.opencore.app.utils.RootChecker
import com.opencore.app.utils.ThemeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "全局设置"

        val listView = findViewById<ListView>(R.id.settingsListView)
        val settingsItems = listOf(
            "主题颜色 (当前: ${ThemeHelper.getCurrentThemeName(this)})",
            "Root权限状态: ${if (RootChecker.isRootedSync()) "已获取" else "未获取"}",
            "关于 OpenCore"
        )
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, settingsItems)
        listView.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> showThemeDialog()
                1 -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val rooted = RootChecker.isRooted()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SettingsActivity, if (rooted) "设备已Root" else "未检测到Root权限", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                2 -> Toast.makeText(this, "OpenCore v1.0 | 53项核心特性", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showThemeDialog() {
        val themes = arrayOf("蓝色", "青色", "紫色", "靛蓝", "橙色", "绿色", "红色")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("选择主题色")
        builder.setItems(themes) { _, which ->
            val colorRes = when (which) {
                0 -> R.color.primary_blue
                1 -> R.color.primary_cyan
                2 -> R.color.primary_purple
                3 -> R.color.primary_indigo
                4 -> R.color.primary_orange
                5 -> R.color.primary_green
                6 -> R.color.primary_red
                else -> R.color.primary_blue
            }
            ThemeHelper.setThemeColor(this, colorRes)
            recreate()
        }
        builder.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
