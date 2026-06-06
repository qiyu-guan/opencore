package com.opencore.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.opencore.app.R
import com.opencore.app.utils.LogHelper
import kotlinx.coroutines.*

class ModulesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        System.loadLibrary("native-lib")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_modules, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.modulesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        scope.launch {
            // Native 返回 Array，转换为 List
            val modulesArray = getModulesList()
            val modules = modulesArray.toList()
            withContext(Dispatchers.Main) {
                recyclerView.adapter = ModulesAdapter(modules)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }

    data class ModuleItem(
        val id: Int,
        val name: String,
        val desc: String,
        var enabled: Boolean
    )

    inner class ModulesAdapter(private val items: List<ModuleItem>) :
        RecyclerView.Adapter<ModulesAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_module, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.name.text = item.name
            holder.desc.text = item.desc
            holder.switch.isChecked = item.enabled

            holder.switch.setOnCheckedChangeListener { _, isChecked ->
                scope.launch {
                    val success = setModuleEnabled(item.id, isChecked)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            item.enabled = isChecked
                            val status = if (isChecked) "启用" else "禁用"
                            Toast.makeText(context, "${item.name} 已$status", Toast.LENGTH_SHORT).show()
                            LogHelper.addLog("Modules", "${item.name} $status")
                        } else {
                            holder.switch.isChecked = !isChecked
                            Toast.makeText(context, "${item.name} 操作失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.moduleName)
            val desc: TextView = view.findViewById(R.id.moduleDesc)
            val switch: SwitchMaterial = view.findViewById(R.id.moduleSwitch)
        }
    }

    // ========== Native 方法声明 ==========
    private external fun getModulesList(): Array<ModuleItem>
    private external fun setModuleEnabled(moduleId: Int, enabled: Boolean): Boolean
}
