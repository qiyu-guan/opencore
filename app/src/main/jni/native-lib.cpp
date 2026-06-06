#include <jni.h>
#include <string>
#include <vector>
#include <map>
#include <random>
#include <chrono>
#include <thread>

// 模拟数据结构
struct Module {
    int id;
    std::string name;
    std::string desc;
    bool enabled;
};

// 全局状态
static std::vector<Module> g_modules = {
    {0, "设备伪装", "修改系统设备参数绕过检测", false},
    {1, "内核管控", "kprobe注入与内核权限认证", true},
    {2, "SELinux规则", "动态放行/拦截SELinux权限", false},
    {3, "SU权限体系", "自研Root权限管理", true},
    {4, "虚拟机隔离", "应用运行环境隔离防检测", false},
    {5, "日志管控", "屏蔽系统日志输出", true},
    {6, "分区挂载引擎", "自定义分区挂载与卸载", false},
    {7, "OTA拦截", "屏蔽系统OTA升级", true},
    {8, "缓存清理", "自动清理系统缓存", false},
    {9, "分区修复", "自动修复分区异常", false}
};

static int g_current_mode = 2; // 0=挂载模式,1=内核模式,2=Magisk模块模式
static int g_patch_progress = 0;
static bool g_boot_patched = false;
static std::vector<std::string> g_patch_history;

// 模拟负载生成
static int getSimulatedLoad() {
    static std::random_device rd;
    static std::mt19937 gen(rd());
    static std::uniform_int_distribution<> dis(10, 45);
    return dis(gen);
}

extern "C" {

// 版本信息
JNIEXPORT jstring JNICALL
Java_com_opencore_app_fragments_HomeFragment_getNativeVersionInfo(JNIEnv* env, jobject) {
    return env->NewStringUTF("OpenCore v2.0 Native Engine | 运行中 | 53项特性");
}

// 启用特性数量
JNIEXPORT jint JNICALL
Java_com_opencore_app_fragments_HomeFragment_getEnabledFeaturesCount(JNIEnv*, jobject) {
    int count = 0;
    for (const auto& m : g_modules) {
        if (m.enabled) count++;
    }
    return count + 44; // 基础特性44 + 模块启用数 = 53
}

// 引擎负载
JNIEXPORT jint JNICALL
Java_com_opencore_app_fragments_HomeFragment_getEngineLoad(JNIEnv*, jobject) {
    return getSimulatedLoad();
}

// 服务运行状态
JNIEXPORT jboolean JNICALL
Java_com_opencore_app_fragments_HomeFragment_isServiceRunning(JNIEnv*, jobject) {
    return JNI_TRUE;
}

// Kprobe状态
JNIEXPORT jboolean JNICALL
Java_com_opencore_app_fragments_HomeFragment_isKprobeActive(JNIEnv*, jobject) {
    return g_current_mode == 1 ? JNI_TRUE : JNI_FALSE;
}

// 活跃功能列表
JNIEXPORT jstring JNICALL
Java_com_opencore_app_fragments_HomeFragment_getActiveFeaturesList(JNIEnv* env, jobject) {
    std::string features;
    for (const auto& m : g_modules) {
        if (m.enabled) {
            if (!features.empty()) features += ", ";
            features += m.name;
        }
    }
    return env->NewStringUTF(features.c_str());
}

// Boot状态
JNIEXPORT jstring JNICALL
Java_com_opencore_app_fragments_HomeFragment_getBootImageStatus(JNIEnv* env, jobject) {
    return env->NewStringUTF(g_boot_patched ? "已修补" : "未修补");
}

// 修补进度
JNIEXPORT jint JNICALL
Java_com_opencore_app_fragments_HomeFragment_getPatchProgress(JNIEnv*, jobject) {
    return g_patch_progress;
}

// 固件信息
JNIEXPORT jstring JNICALL
Java_com_opencore_app_fragments_HomeFragment_getFirmwareInfo(JNIEnv* env, jobject) {
    return env->NewStringUTF("AOSP 13+ / 内核 5.10+");
}

// 修补历史
JNIEXPORT jstring JNICALL
Java_com_opencore_app_fragments_HomeFragment_getPatchHistory(JNIEnv* env, jobject) {
    if (g_patch_history.empty()) {
        return env->NewStringUTF("");
    }
    std::string history;
    for (const auto& h : g_patch_history) {
        history += h + "\n";
    }
    return env->NewStringUTF(history.c_str());
}

// 修补Boot镜像
JNIEXPORT jstring JNICALL
Java_com_opencore_app_fragments_HomeFragment_patchBootImage(JNIEnv* env, jobject) {
    // 模拟修补过程
    for (int i = 0; i <= 100; i += 20) {
        g_patch_progress = i;
        std::this_thread::sleep_for(std::chrono::milliseconds(100));
    }
    g_boot_patched = true;
    
    auto now = std::chrono::system_clock::now();
    auto time_t = std::chrono::system_clock::to_time_t(now);
    std::string history_entry = std::ctime(&time_t);
    history_entry.pop_back(); // 移除换行
    history_entry += " - Boot镜像修补成功";
    g_patch_history.push_back(history_entry);
    
    return env->NewStringUTF("Boot镜像修补成功！请重启设备生效");
}

// 当前模式
JNIEXPORT jint JNICALL
Java_com_opencore_app_fragments_HomeFragment_getCurrentMode(JNIEnv*, jobject) {
    return g_current_mode;
}

// ========== ModulesFragment Native 方法 ==========

JNIEXPORT jobjectArray JNICALL
Java_com_opencore_app_fragments_ModulesFragment_getModulesList(JNIEnv* env, jobject) {
    jclass moduleItemClass = env->FindClass("com/opencore/app/fragments/ModulesFragment$ModuleItem");
    jmethodID constructor = env->GetMethodID(moduleItemClass, "<init>", "(ILjava/lang/String;Ljava/lang/String;Z)V");
    
    jobjectArray result = env->NewObjectArray(g_modules.size(), moduleItemClass, nullptr);
    
    for (size_t i = 0; i < g_modules.size(); i++) {
        jstring name = env->NewStringUTF(g_modules[i].name.c_str());
        jstring desc = env->NewStringUTF(g_modules[i].desc.c_str());
        jobject obj = env->NewObject(moduleItemClass, constructor, g_modules[i].id, name, desc, g_modules[i].enabled);
        env->SetObjectArrayElement(result, i, obj);
        env->DeleteLocalRef(name);
        env->DeleteLocalRef(desc);
        env->DeleteLocalRef(obj);
    }
    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_opencore_app_fragments_ModulesFragment_setModuleEnabled(JNIEnv* env, jobject, jint moduleId, jboolean enabled) {
    for (auto& m : g_modules) {
        if (m.id == moduleId) {
            m.enabled = enabled;
            return JNI_TRUE;
        }
    }
    return JNI_FALSE;
}

} // extern "C"
