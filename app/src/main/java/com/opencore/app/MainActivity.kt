package com.opencore.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.opencore.app.utils.LogHelper

class MainActivity : AppCompatActivity() {

    init {
        System.loadLibrary("native-lib")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 加载保存的主题
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvNative = findViewById<TextView>(R.id.tvNativeStatus)
        tvNative.text = stringFromJNI()

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<Button>(R.id.btnLogs).setOnClickListener {
            startActivity(Intent(this, LogActivity::class.java))
        }

        LogHelper.addLog("MainActivity", "应用启动，原生层返回: ${stringFromJNI()}")
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("opencore_prefs", MODE_PRIVATE)
        val colorValue = prefs.getInt("theme_color", getColor(R.color.default_primary))
        // 动态修改主题需要在 setContentView 之前调用，但MaterialComponents 推荐使用自定义主题覆盖
        // 这里使用最简单的方式：重新创建Activity？不，我们只改变主界面的状态栏颜色。
        window.statusBarColor = colorValue
        findViewById<androidx.appcompat.widget.Toolbar?>(null)?.setBackgroundColor(colorValue)
        // 更完整的主题切换需要重启 Activity，为简化操作，我们只修改状态栏
    }

    override fun onResume() {
        super.onResume()
        // 每次返回主界面刷新状态栏颜色
        val prefs = getSharedPreferences("opencore_prefs", MODE_PRIVATE)
        val colorValue = prefs.getInt("theme_color", getColor(R.color.default_primary))
        window.statusBarColor = colorValue
    }

    external fun stringFromJNI(): String
}
