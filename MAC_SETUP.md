# Mac平台部署说明

## 已完成的工作

### 1. 跨平台支持修复 ✅
- ✅ 在 `src/adb_helper.py` 中添加了 `get_aapt_path()` 函数，支持跨平台获取aapt工具路径
- ✅ 修复了以下脚本中的硬编码 `aapt.exe` 路径：
  - `cli/test_extract_icons_from_apk.py`
  - `cli/test_separate_icons.py`
  - `cli/extract_missing_apks.py`
  - `cli/debug_aapt_output.py`
  - `cli/download_aapt.py` (已支持Mac平台下载)

### 2. 测试脚本 ✅
- ✅ 已成功从APK中提取PNG图标到 `tests/com.catchingnow.np/ic_launcher_512.png`

## 需要手动完成的工作

### 1. 安装Python依赖

由于pip可能存在权限问题，请手动安装依赖：

```bash
cd /Users/rinshibuya/Downloads/ArtPlus

# 方法1: 使用pip安装（推荐）
pip3 install -r requirements.txt

# 方法2: 如果方法1失败，使用--user标志
pip3 install --user -r requirements.txt

# 方法3: 使用虚拟环境（最推荐）
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

### 2. 下载Mac版本的aapt工具

当前 `platform_tools` 目录中只有Windows版本的 `aapt.exe`，需要下载Mac版本：

**方法1: 使用脚本下载（推荐）**
```bash
cd /Users/rinshibuya/Downloads/ArtPlus
python3 cli/download_aapt.py
```

**方法2: 手动下载**
1. 访问 https://developer.android.com/studio/releases/build-tools
2. 下载Mac版本的Android SDK Build Tools
3. 解压后找到 `aapt` 文件（无.exe后缀）
4. 复制到 `platform_tools` 目录
5. 添加执行权限：`chmod +x platform_tools/aapt`

### 3. 测试功能

安装完依赖和aapt工具后，可以运行测试：

```bash
cd /Users/rinshibuya/Downloads/ArtPlus

# 测试1: 从APK提取图标
python3 cli/test_extract_icons_from_apk.py

# 测试2: 提取并分离图标前后景
python3 cli/test_extract_and_separate.py

# 测试3: 使用多种方法分离图标
python3 cli/test_separate_icons.py
```

## 项目结构说明

- `cli/` - CLI入口脚本
- `src/` - 共享模块
- `tests/` - 测试目录，包含测试APK和提取的图标
- `platform_tools/` - Android平台工具目录（需要Mac版本的aapt）
- `requirements.txt` - Python依赖列表

## 依赖说明

主要依赖包括：
- `Pillow` - 图像处理
- `rembg` - 背景移除（需要 `onnxruntime`）
- `opencv-python` - 图像处理（GrabCut算法）
- `numpy` - 数值计算
- `scikit-learn` - 颜色聚类
- `scipy` - 科学计算

## 注意事项

1. **aapt工具**: Mac版本没有 `.exe` 后缀，脚本已自动处理
2. **权限问题**: 如果pip安装失败，尝试使用虚拟环境或 `--user` 标志
3. **依赖冲突**: 如果遇到依赖冲突，建议使用虚拟环境隔离

## 故障排除

### pip安装失败
- 检查Python版本：`python3 --version` (需要3.6+)
- 尝试使用虚拟环境
- 检查网络连接

### aapt工具无法运行
- 确认已下载Mac版本（不是Windows的.exe文件）
- 检查执行权限：`chmod +x platform_tools/aapt`
- 测试运行：`./platform_tools/aapt version`

### 图标分离失败
- 确认所有依赖已安装
- 检查图标文件是否存在且为有效PNG格式
- 查看错误信息，可能需要安装额外的系统库

