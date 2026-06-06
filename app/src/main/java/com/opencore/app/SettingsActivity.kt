package com.opencore.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.opencore.app.utils.LogHelper

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("opencore_prefs", MODE_PRIVATE)

        val colorList = listOf(
            "红" to R.color.theme_red,
            "粉红" to R.color.theme_pink,
            "紫" to R.color.theme_purple,
            "深紫" to R.color.theme_deep_purple,
            "靛蓝" to R.color.theme_indigo,
            "蓝" to R.color.theme_blue,
            "浅蓝" to R.color.theme_light_blue,
            "青" to R.color.theme_cyan,
            "蓝绿" to R.color.theme_teal,
            "绿" to R.color.theme_green,
            "浅绿" to R.color.theme_light_green,
            "黄绿" to R.color.theme_lime,
            "黄" to R.color.theme_yellow,
            "琥珀" to R.color.theme_amber,
            "橙" to R.color.theme_orange,
            "深橙" to R.color.theme_deep_orange,
            "棕" to R.color.theme_brown,
            "灰" to R.color.theme_grey,
            "蓝灰" to R.color.theme_blue_grey
        )

        val currentColor = prefs.getInt("theme_color", getColor(R.color.default_primary))

        findViewById<RecyclerView>(R.id.colorRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@SettingsActivity)
            adapter = ColorAdapter(colorList, currentColor) { selectedColorRes ->
                val newColor = getColor(selectedColorRes)
                prefs.edit().putInt("theme_color", newColor).apply()
                LogHelper.addLog("Settings", "主题颜色已更改")
                recreate()
            }
        }

        val chkAutoClear = findViewById<android.widget.CheckBox>(R.id.chkAutoClearLog)
        chkAutoClear.isChecked = prefs.getBoolean("auto_clear_log", true)
        chkAutoClear.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_clear_log", isChecked).apply()
            LogHelper.addLog("Settings", "自动清除日志 = $isChecked")
        }
    }

    inner class ColorAdapter(
        private val colors: List<Pair<String, Int>>,
        private val currentColor: Int,
        private val onSelect: (Int) -> Unit
    ) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_color, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val (name, colorRes) = colors[position]
            val colorValue = holder.itemView.context.getColor(colorRes)
            holder.colorView.setBackgroundColor(colorValue)
            holder.colorName.text = name
            holder.radioSelect.isChecked = (currentColor == colorValue)
            holder.itemView.setOnClickListener {
                onSelect(colorRes)
                notifyDataSetChanged()
            }
        }

        override fun getItemCount() = colors.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val colorView: View = itemView.findViewById(R.id.colorView)
            val colorName: TextView = itemView.findViewById(R.id.colorName)
            val radioSelect: RadioButton = itemView.findViewById(R.id.radioSelect)
        }
    }
}
