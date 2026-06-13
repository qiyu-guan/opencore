# OpenCore

**OpenCore** 是一个安卓系统增强框架，提供 Root 权限管理、Magisk 模块安装、实时系统监控、Boot 镜像修补等功能。完全开源，支持通过 GitHub Actions 自动构建 APK。

---

## 🚀 主要功能

| 功能模块 | 描述 |
|---------|------|
| **Root 权限检测** | 独创二进制扫描（`/system/bin/su` 等路径），主动请求 Root 权限 |
| **Magisk 模块管理** | 支持导入本地 `.zip` 模块，或从在线仓库下载安装，一键卸载 |
| **实时系统监控** | 显示 CPU 负载、内核版本、SELinux 状态、Kprobe 状态 |
| **Boot 镜像修补** | 通过 `dd` 备份和 `magiskboot` 打包，一键修补 boot 分区 |
| **深色科技风 UI** | 纯 Jetpack Compose 实现，支持深色/浅色主题切换 |
| **日志管理** | 自动记录操作日志，支持一键清空，WorkManager 定期清理 |

---

## 📦 下载与安装

### 稳定版 APK
从 [Releases](https://github.com/qiyu-guan/opencore/releases) 页面下载最新 `app-release.apk`，直接安装即可。

### 要求
- Android 8.0 (API 26) 或更高版本
- **推荐**：已 Root 设备（Magisk 或 SuperSU）

---

## 🛠️ 使用说明

### 首次使用
1. 安装 APK 并打开。
2. 如果设备已 Root，点击“授予 Root 权限”按钮，允许 Superuser 请求。
3. 主页将实时显示引擎负载和系统状态。

### 模块管理
- **导入本地模块**：点击右下角 ➕ 按钮，选择 Magisk 模块 ZIP 文件，自动安装到 `/data/adb/modules/`。
- **卸载模块**：在“已安装”标签页中点击“卸载”按钮。
- **在线模块**：切换到“在线库”标签，选择推荐模块一键下载安装。

### Boot 镜像修补
- 在主页点击“一键修补 Boot 镜像”，会备份原始 boot 并尝试使用 `magiskboot` 打包修补（需要 Root）。

---

## 🔧 开发者指南

### 项目结构
```

opencore/
├── app/                           # 主应用模块
│   ├── src/main/java/com/opencore/app/
│   │   ├── engine/                # 核心引擎（RootManager, ModuleInstaller, OpenCoreEngine）
│   │   ├── ui/theme/              # Compose 主题和颜色
│   │   ├── ui/screens/            # 四个主页面（主页、日志、模块、设置）
│   │   └── utils/                 # 辅助工具（LogHelper）
│   └── build.gradle               # 依赖配置
├── magisk_module/                 # Magisk 模块模板（示例）
└── .github/workflows/build.yml    # CI 自动构建脚本

```

### 本地构建
```bash
# 克隆仓库
git clone https://github.com/qiyu-guan/opencore.git
cd opencore

# 使用 Gradle 8.2 构建
./gradlew assembleRelease
```

贡献代码

欢迎提交 Issue 和 Pull Request。建议先阅读现有代码风格（Kotlin + Compose）。

---

⚠️ 免责声明

· 本工具涉及系统底层操作（Root、分区读写），可能导致设备变砖或失去保修，请自行承担风险。
· 仅限合法用途，请勿用于绕过安全机制或侵犯他人隐私。

---

📄 许可证

本项目采用 MIT 许可证，详情见 LICENSE 文件。

---

📧 联系方式

· 作者：qiyu-guan
· 邮箱：3839071822@qq.com
· 项目地址：https://github.com/qiyu-guan/opencore

```
