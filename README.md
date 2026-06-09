# ART+图标缺失检测工具

用于检测ColorOS16系统中哪些已安装的用户应用缺少ART+图标适配。

## 项目结构

```
ArtPlus/
├── cli/                  # CLI入口脚本
├── src/                  # 共享模块与工具函数
├── docs/                 # 文档目录
├── launch/               # 启动脚本（按系统分类）
│   ├── windows/          # Windows 批处理
│   ├── macos/            # macOS Shell
│   └── linux/            # Linux Shell
├── outputs/              # 标准输出目录
│   ├── new_artplus/      # 新生成的ART+图标
│   ├── reports/          # 报告与统计
│   ├── mappings/         # 名称映射
│   ├── tmp/              # 临时文件
│   └── archives/         # 压缩包/归档
├── tests/                # 测试资源与样例
├── platform_tools/       # Android平台工具（ADB等）
├── theme/                # 主题文件
├── uxicons/              # ART+图标目录
└── README.md             # 本文件
```

## 快速开始

按系统运行 `launch/` 目录下的启动脚本（按使用顺序）：

### Android APK 远程构建

仓库包含一个实验性的 `mobile/` Android 客户端，用于在手机本地列出启动器应用、选择应用、生成本地版 ART+ 图标目录，并可在获得 root 后写入 `/data/oplus/uxicons/{package}`。

本地不需要安装 JDK/Gradle 来构建 APK。推送到 GitHub 后，`Android APK` workflow 会在 GitHub Actions 中远程构建 debug APK，并把 `artplus-mobile-debug` 作为 artifact 上传。

当前移动端 MVP：

- 列出 launcher 应用
- 使用系统解析后的 launcher icon 生成 ART+ 本地版 PNG
- 支持 adaptive icon foreground/background 拆分
- 使用 Monochrome Alpha Mask 算法生成 `monochrome*.png`
- 可选择 SAF 输出目录
- 可尝试 root 写入 `/data/oplus/uxicons/{package}`

### 完整工作流程（按执行顺序）

1. **`01-检测缺失ART图标`** - 检测手机中哪些应用缺少ART+图标适配

2. **`02-拉取APK并提取和整理图标`** - 一键完成以下五个步骤：
   - 拉取缺少PNG图标的应用APK并直接提取图标（使用aapt工具）
   - 整理并转换提取的图标，转换为标准格式，原文件移到src目录
   - 清理目录，只保留PNG文件，删除XML等其他文件
   - 整理非标准图标，保留分辨率最高的，其余移到src目录
   - 清理重复图标文件（基于MD5哈希）

3. **`03-使用rembg分离图标前景背景`** - 使用rembg批量分离非官方命名图标的前景和背景（跳过已有官方图标的包）

### 启动方式（按系统）

**Windows**
```
launch\windows\01-检测缺失ART图标.bat
launch\windows\02-拉取APK并提取和整理图标.bat
launch\windows\03-使用rembg分离图标前景背景.bat
```

**macOS**
```
bash launch/macos/01-检测缺失ART图标.sh
bash launch/macos/02-拉取APK并提取和整理图标.sh
bash launch/macos/03-使用rembg分离图标前景背景.sh
```

**Linux**
```
bash launch/linux/01-检测缺失ART图标.sh
bash launch/linux/02-拉取APK并提取和整理图标.sh
bash launch/linux/03-使用rembg分离图标前景背景.sh
```

## 功能特点

- ✅ 通过adb自动获取手机上的所有用户应用包名
- ✅ 扫描本地ART+图标目录，检查已适配的应用
- ✅ 对比分析，找出缺少ART+图标的应用
- ✅ 支持从本地文件读取应用列表
- ✅ 输出详细的结果报告（文本和JSON格式）

## 使用方法

### 前置要求

1. 已安装Python 3.6+
2. 已安装adb并添加到系统PATH
3. 手机已通过USB连接并开启USB调试
4. 已提取ART+图标目录到本地（`theme/uxicons/hdpi/` 和 `uxicons/`）

### 基本使用

#### 方式1: 通过adb获取应用列表（推荐）

```bash
python cli/check_artplus_icons_enhanced.py
```

选择选项 `1`，工具会自动：
1. 通过adb获取手机上的所有用户应用
2. 扫描本地ART+图标目录
3. 对比找出缺失的应用
4. 生成报告文件

#### 方式2: 从本地文件读取应用列表

如果你已经导出了应用列表，可以保存为以下格式之一：

**格式1: 纯包名列表**
```
com.tencent.mm
com.taobao.taobao
com.jingdong.app.mall
```

**格式2: 包名|应用名称**
```
com.tencent.mm|微信
com.taobao.taobao|淘宝
com.jingdong.app.mall|京东
```

**格式3: JSON格式**
```json
[
  {"package": "com.tencent.mm", "name": "微信"},
  {"package": "com.taobao.taobao", "name": "淘宝"}
]
```

然后运行：
```bash
python cli/check_artplus_icons_enhanced.py
```

选择选项 `2`，输入文件路径。

#### 方式3: 仅查看已适配的应用

```bash
python cli/check_artplus_icons_enhanced.py
```

选择选项 `3`，会列出所有已适配ART+图标的应用。

### 导出应用列表（可选）

如果你想先导出应用列表，可以使用以下adb命令：

```bash
# 导出所有用户应用包名
adb shell pm list packages -3 > apps.txt

# 或者导出为JSON格式（需要额外处理）
adb shell pm list packages -3 | sed 's/package://' | jq -R -s 'split("\n") | map(select(. != "")) | map({"package": ., "name": .})' > apps.json
```

## 输出结果

工具会生成三个文件（默认输出到 `outputs/reports/`）：

1. **missing_artplus_icons.txt** - 文本格式的缺失应用列表
2. **missing_artplus_icons.json** - JSON格式的缺失应用列表
3. **missing_artplus_icons.html** - HTML可视化报告

## 目录结构说明

工具会检查以下两个目录：

1. `theme/uxicons/hdpi/` - 对应系统路径 `/my_product/media/theme/uxicons/hdpi/`
2. `uxicons/` - 对应系统路径 `/data/oplus/uxicons/`

每个应用都有一个以包名命名的文件夹，例如：
- `com.tencent.mm/` - 微信
- `com.taobao.taobao/` - 淘宝

## 注意事项

1. 确保ART+图标目录已正确提取到本地
2. 确保adb可以正常连接手机
3. 工具会排除系统应用，只检查用户安装的第三方应用
4. 如果某个应用在两个目录中都存在，会被正确识别为已适配

## 故障排除

### adb连接问题

如果提示"未检测到已连接的设备"：
1. 检查USB连接
2. 确认已开启USB调试
3. 在手机上授权此电脑的调试权限
4. 运行 `adb devices` 确认设备已连接

### 目录不存在

如果提示目录不存在，请检查：
1. ART+图标目录是否正确提取
2. 目录路径是否正确（相对于脚本运行目录）

## 许可证

MIT License
