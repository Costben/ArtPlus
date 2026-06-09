# 图标zip提取说明

## 工作流程

### 步骤1: 提取缺失PNG图标的应用APK

运行 `python cli/extract_missing_apks.py`，脚本会：
1. 检查哪些应用缺少PNG图标
2. 通过adb提取这些应用的APK到手机 `Download/Apks` 目录
3. APK文件以包名命名（如 `com.example.app.apk`）

### 步骤2: 在手机上提取图标

使用手机上的图标提取工具（如MT管理器等）：
1. 打开 `Download/Apks` 目录
2. 对每个APK提取图标
3. 提取后的zip文件命名格式：`应用桌面显示名_icon.zip`
   - 例如：`gkd_icon.zip`、`闲鱼_icon.zip`

### 步骤3: 批量提取zip到outputs/new_artplus

运行 `python cli/extract_icon_zips.py`，脚本会：
1. 从手机 `Download/Apks` 目录读取所有 `*_icon.zip` 文件
2. 根据zip文件名中的应用名称匹配包名
3. 解压zip文件到 `outputs/new_artplus/包名/` 目录

## 使用方法

### 方式1: 从手机拉取（推荐）

1. 运行 `python cli/extract_icon_zips.py`
2. 选择选项 `1`（从手机Download/Apks目录拉取）
3. 脚本会自动：
   - 连接手机
   - 列出所有zip文件
   - 匹配包名
   - 解压到对应目录

### 方式2: 从本地目录提取

如果已经将zip文件复制到电脑：

1. 运行 `python cli/extract_icon_zips.py`
2. 选择选项 `2`（从本地目录提取）
3. 输入本地zip文件目录路径
4. 脚本会自动解压到对应目录

### 命令行方式

```bash
# 从手机拉取
python cli/extract_icon_zips.py

# 从本地目录提取
python cli/extract_icon_zips.py "C:\path\to\zip\files"
```

## 匹配逻辑

脚本使用以下逻辑匹配应用名称到包名：

1. **精确匹配** - 直接匹配应用名称
2. **模糊匹配** - 忽略大小写、空格、下划线、连字符进行匹配
3. **包含匹配** - 检查应用名称是否包含在包名中，或反之
4. **包名匹配** - 如果应用名称是包名的一部分，也会匹配

## 注意事项

1. **zip文件命名** - 必须遵循 `应用名称_icon.zip` 格式
2. **应用名称匹配** - 如果无法匹配包名，脚本会跳过该文件
3. **文件覆盖** - 如果目标目录已存在同名文件，会自动添加序号
4. **adb连接** - 从手机拉取时需要保持adb连接

## 输出结果

提取完成后，图标文件会解压到：
```
outputs/new_artplus/
├── com.example.app1/
│   ├── icon1.png
│   ├── icon2.png
│   └── ...
└── com.example.app2/
    └── ...
```

## 故障排除

### 问题1: 无法匹配包名

**原因：**
- zip文件名中的应用名称与JSON中的名称不一致
- 应用名称包含特殊字符

**解决方案：**
- 检查zip文件名是否正确
- 手动修改zip文件名，使其更接近应用的实际名称
- 或者手动解压到对应目录

### 问题2: 无法从手机拉取

**原因：**
- adb未连接
- 手机目录不存在

**解决方案：**
- 检查adb连接：`adb devices`
- 确认手机上有 `Download/Apks` 目录
- 使用方式2从本地目录提取

### 问题3: 解压失败

**原因：**
- zip文件损坏
- 权限问题

**解决方案：**
- 检查zip文件是否完整
- 确认有写入权限
- 手动解压zip文件
