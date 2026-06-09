# 测试说明

## 当前状态

✅ APK已成功拉取到tests文件夹：
- `com.catchingnow.np.apk` (9.81 MB)

✅ aapt工具已配置：
- `platform_tools\aapt.exe`

✅ 图标提取测试成功：
- 已从APK中提取12个图标文件到 `tests/com.catchingnow.np/` 目录

## 测试功能

### 1. 提取应用名称

使用aapt工具从APK中提取应用的中文显示名称：

```bash
python cli/test_extract_app_name.py com.catchingnow.np
```

### 2. 提取APK图标

使用aapt工具从APK中提取所有图标资源：

```bash
python cli/test_extract_icons_from_apk.py
```

脚本会：
1. 自动查找tests目录中的所有APK文件
2. 使用aapt工具识别图标资源名称
3. 从APK中提取所有匹配的图标文件（PNG、XML、WEBP等）
4. 保存到 `tests/<包名>/` 目录

## 提取的图标类型

脚本会提取以下类型的图标：
- `ic_launcher.png/xml` - 标准启动图标
- `ic_launcher_foreground.png/xml` - 前景图标
- `ic_launcher_background.png/xml` - 背景图标
- `ic_launcher_round.png/xml` - 圆形图标
- `ic_launcher_monochrome.png/xml` - 单色图标
- 其他自定义图标名称

## 测试其他APK

将APK文件放到tests目录，然后运行：

```bash
python cli/test_extract_icons_from_apk.py
```

脚本会自动处理tests目录中的所有APK文件。
