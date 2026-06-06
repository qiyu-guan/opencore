package com.opencore.app.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.opencore.app.R
import com.opencore.app.utils.LogHelper
import kotlinx.coroutines.*

class HomeFragment : Fragment() {

    private lateinit var tvEngineLoad: TextView
    private lateinit var tvServiceStatus: TextView
    private lateinit var tvKprobeStatus: TextView
    private lateinit var tvFeaturesList: TextView
    private lateinit var tvBootStatus: TextView
    private lateinit var tvPatchProgress: TextView
    private lateinit var tvFirmwareInfo: TextView
    private lateinit var tvPatchHistory: TextView
    private lateinit var btnPatchBoot: Button
    private lateinit var tvVersionStatus: TextView
    private lateinit var tvFeaturesCount: TextView
    private lateinit var modeMount: TextView
    private lateinit var modeKernel: TextView
    private lateinit var modeMagisk: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        System.loadLibrary("native-lib")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化视图
        tvEngineLoad = view.findViewById(R.id.tvEngineLoad)
        tvServiceStatus = view.findViewById(R.id.tvServiceStatus)
        tvKprobeStatus = view.findViewById(R.id.tvKprobeStatus)
        tvFeaturesList = view.findViewById(R.id.tvFeaturesList)
        tvBootStatus = view.findViewById(R.id.tvBootStatus)
        tvPatchProgress = view.findViewById(R.id.tvPatchProgress)
        tvFirmwareInfo = view.findViewById(R.id.tvFirmwareInfo)
        tvPatchHistory = view.findViewById(R.id.tvPatchHistory)
        btnPatchBoot = view.findViewById(R.id.btnPatchBoot)
        tvVersionStatus = view.findViewById(R.id.tvVersionStatus)
        tvFeaturesCount = view.findViewById(R.id.tvFeaturesCount)
        modeMount = view.findViewById(R.id.modeMount)
        modeKernel = view.findViewById(R.id.modeKernel)
        modeMagisk = view.findViewById(R.id.modeMagisk)

        // 从Native获取版本信息
        tvVersionStatus.text = getNativeVersionInfo()
        val featuresEnabled = getEnabledFeaturesCount()
        tvFeaturesCount.text = "53项核心特性: 已启用 $featuresEnabled/53"

        // 从Native获取当前运行模式
        updateModeDisplay()

        // Boot修补按钮
        btnPatchBoot.setOnClickListener {
            scope.launch {
                val result = patchBootImage()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                    LogHelper.addLog("HomeFragment", result)
                    updateBootStatus()
                }
            }
        }

        // 启动实时数据更新
        startRealtimeUpdates()
    }

    private fun startRealtimeUpdates() {
        updateRunnable = object : Runnable {
            override fun run() {
                updateRealtimeData()
                handler.postDelayed(this, 2000) // 每2秒更新一次
            }
        }
        handler.post(updateRunnable!!)
    }

    private fun updateRealtimeData() {
        scope.launch {
            val load = getEngineLoad()
            val serviceRunning = isServiceRunning()
            val kprobeActive = isKprobeActive()
            val features = getActiveFeaturesList()

            withContext(Dispatchers.Main) {
                tvEngineLoad.text = "引擎负载: $load%"
                tvServiceStatus.text = "底层服务: ${if (serviceRunning) "运行中" else "已停止"}"
                tvKprobeStatus.text = "内核注入: ${if (kprobeActive) "活跃" else "未激活"}"
                tvFeaturesList.text = "启用功能: $features"
            }
        }
    }

    private fun updateBootStatus() {
        scope.launch {
            val status = getBootImageStatus()
            val progress = getPatchProgress()
            val firmware = getFirmwareInfo()
            val history = getPatchHistory()
            withContext(Dispatchers.Main) {
                tvBootStatus.text = "Boot镜像状态: $status"
                tvPatchProgress.text = "修补进度: $progress%"
                tvFirmwareInfo.text = "固件适配: $firmware"
                tvPatchHistory.text = "修补历史: ${if (history.isEmpty()) "无" else history}"
            }
        }
    }

    private fun updateModeDisplay() {
        scope.launch {
            val mode = getCurrentMode()
            withContext(Dispatchers.Main) {
                // 重置所有样式
                modeMount.setBackgroundResource(R.drawable.bg_mode_inactive)
                modeKernel.setBackgroundResource(R.drawable.bg_mode_inactive)
                modeMagisk.setBackgroundResource(R.drawable.bg_mode_inactive)
                modeMount.setTextColor(resources.getColor(R.color.text_secondary, null))
                modeKernel.setTextColor(resources.getColor(R.color.text_secondary, null))
                modeMagisk.setTextColor(resources.getColor(R.color.text_secondary, null))

                // 高亮当前模式
                when (mode) {
                    0 -> {
                        modeMount.setBackgroundResource(R.drawable.bg_mode_active)
                        modeMount.setTextColor(resources.getColor(android.R.color.white, null))
                    }
                    1 -> {
                        modeKernel.setBackgroundResource(R.drawable.bg_mode_active)
                        modeKernel.setTextColor(resources.getColor(android.R.color.white, null))
                    }
                    2 -> {
                        modeMagisk.setBackgroundResource(R.drawable.bg_mode_active)
                        modeMagisk.setTextColor(resources.getColor(android.R.color.white, null))
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updateRunnable?.let { handler.removeCallbacks(it) }
        scope.cancel()
    }

    // ========== Native 方法声明 ==========
    private external fun getNativeVersionInfo(): String
    private external fun getEnabledFeaturesCount(): Int
    private external fun getEngineLoad(): Int
    private external fun isServiceRunning(): Boolean
    private external fun isKprobeActive(): Boolean
    private external fun getActiveFeaturesList(): String
    private external fun getBootImageStatus(): String
    private external fun getPatchProgress(): Int
    private external fun getFirmwareInfo(): String
    private external fun getPatchHistory(): String
    private external fun patchBootImage(): String
    private external fun getCurrentMode(): Int
}
