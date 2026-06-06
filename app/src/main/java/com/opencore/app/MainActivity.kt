package com.opencore.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.opencore.app.utils.LogHelper

class MainActivity : AppCompatActivity() {

    init {
        System.loadLibrary("native-lib")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化日志助手
        LogHelper.init(this)

        val tvNative = findViewById<TextView>(R.id.tvNativeStatus)
        tvNative.text = stringFromJNI()

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<Button>(R.id.btnLogs).setOnClickListener {
            startActivity(Intent(this, LogActivity::class.java))
        }

        val prefs = getSharedPreferences("opencore_prefs", MODE_PRIVATE)
        val colorValue = prefs.getInt("theme_color", getColor(R.color.default_primary))
        window.statusBarColor = colorValue

        LogHelper.addLog("MainActivity", "应用启动，原生层返回: ${stringFromJNI()}")
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("opencore_prefs", MODE_PRIVATE)
        val colorValue = prefs.getInt("theme_color", getColor(R.color.default_primary))
        window.statusBarColor = colorValue
    }

    external fun stringFromJNI(): String
}
