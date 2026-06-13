# ArtPlus 移动端

ArtPlus 是一个用于制作和预览 ColorOS ART+ 图标资源的 Android 工具。它可以读取手机中的应用列表，基于应用图标生成 ART+ 所需的多层图标文件，并在手机上直接预览普通、暗色和单色效果。

## 主要功能

- 读取已安装应用，并按应用选择需要处理的图标。
- 生成 `recbg`、`recfg`、`rec_night`、`monochrome` 等 ART+ 图标资源。
- 支持本地图标分层、背景清理、前景修整和单色图标生成。
- 可选接入图像生成接口，用于生成候选图标效果。
- 支持导出图标资源，Root 环境下也可以写入系统 ART+ 图标目录。
- 仓库内包含部分图标资源、主题模板和构建脚本，方便继续补全图标包。

## 项目结构

- `mobile/`：Android 移动端应用源码。
- `uxicons/`：已整理的 ART+ 图标资源。
- `theme/`：主题相关资源模板。
- `outputs/`：生成结果、预览图和构建产物。

## 构建

需要本机已安装 Java、Android SDK 和 Gradle。

```bash
gradle :mobile:assembleDebug
gradle :mobile:assembleRelease
```

构建完成后，安装包会生成在：

```text
mobile/build/outputs/apk/debug/mobile-debug.apk
mobile/build/outputs/apk/release/mobile-release.apk
```

## 发布

仓库中的 GitHub Actions 会在手动触发或推送版本标签时自动构建 release 安装包，并上传到发布页面。当前 release 包按项目要求使用默认 Android debug 证书签名，但不会启用 debuggable 或调试 HTTP 服务。
