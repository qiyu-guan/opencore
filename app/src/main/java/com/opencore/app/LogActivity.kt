package com.opencore.app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.opencore.app.utils.LogHelper

class LogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        val tvLog = findViewById<TextView>(R.id.tvLogContent)
        refreshLog(tvLog)

        findViewById<Button>(R.id.btnClearLog).setOnClickListener {
            LogHelper.clearLog()
            refreshLog(tvLog)
        }
    }

    private fun refreshLog(tvLog: TextView) {
        val logs = LogHelper.getLogs()
        tvLog.text = if (logs.isEmpty()) getString(R.string.log_empty) else logs.joinToString("\n")
    }
}
