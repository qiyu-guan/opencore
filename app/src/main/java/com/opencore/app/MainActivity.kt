package com.opencore.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.opencore.app.databinding.ActivityMainBinding
import com.opencore.app.fragments.HomeFragment
import com.opencore.app.fragments.ModulesFragment
import com.opencore.app.utils.LogHelper
import com.opencore.app.utils.RootChecker
import com.opencore.app.utils.ThemeHelper
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var logDialog: android.app.Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LogHelper.init(this)
        checkRootPermission()

        // 设置右上角设置按钮
        binding.fabSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // 底部导航
        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    showHomeFragment()
                    true
                }
                R.id.navigation_log -> {
                    showLogDialog()
                    true
                }
                R.id.navigation_modules -> {
                    showModulesFragment()
                    true
                }
                else -> false
            }
        }

        // 默认显示主页
        showHomeFragment()
    }

    private fun showHomeFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.homeFragmentContainer, HomeFragment())
            .commit()
        binding.middleArea.visibility = View.GONE
    }

    private fun showModulesFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.modulesFragmentContainer, ModulesFragment())
            .commit()
        binding.middleArea.visibility = View.GONE
    }

    private fun showLogDialog() {
        if (logDialog == null) {
            logDialog = MaterialAlertDialogBuilder(this)
                .setView(R.layout.dialog_log)
                .setCancelable(true)
                .create()
            logDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            logDialog?.show()
            val tvLog = logDialog?.findViewById<android.widget.TextView>(R.id.tvLogContent)
            val btnClear = logDialog?.findViewById<android.widget.Button>(R.id.btnClearLog)
            tvLog?.text = LogHelper.getLogs().joinToString("\n")
            btnClear?.setOnClickListener {
                LogHelper.clearLog()
                tvLog?.text = LogHelper.getLogs().joinToString("\n")
                Toast.makeText(this, "日志已清除", Toast.LENGTH_SHORT).show()
            }
        } else {
            logDialog?.show()
            val tvLog = logDialog?.findViewById<android.widget.TextView>(R.id.tvLogContent)
            tvLog?.text = LogHelper.getLogs().joinToString("\n")
        }
        binding.middleArea.visibility = View.VISIBLE
    }

    private fun checkRootPermission() {
        lifecycleScope.launch {
            val hasRoot = RootChecker.isRooted()
            if (!hasRoot) {
                Toast.makeText(this@MainActivity, "未检测到Root权限，部分功能不可用", Toast.LENGTH_LONG).show()
                LogHelper.addLog("MainActivity", "设备未Root")
            } else {
                LogHelper.addLog("MainActivity", "Root权限已获取")
            }
        }
    }
}
