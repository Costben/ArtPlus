# OpenList 服务器使用说明

## 服务器状态

✅ **OpenList 服务器已启动并运行在 5244 端口**

## 访问信息

- **Web访问地址**: http://localhost:5244/
- **WebDAV访问地址**: http://localhost:5244/dav/
- **管理员用户名**: admin
- **管理员密码**: admin123

### 重要提示

⚠️ **手机应用访问时请注意**：
- 如果使用WebDAV协议，必须使用 `/dav/` 路径，而不是根路径 `/`
- 正确的WebDAV地址：`http://192.168.31.247:5244/dav/`
- 错误的地址（会导致405错误）：`http://192.168.31.247:5244/`

## 快速开始

### 启动服务器

```bash
cd /Users/rinshibuya/Downloads/ArtPlus
./openlist server
```

或使用启动脚本：

```bash
bash cli/start_openlist.sh
```

### 停止服务器

按 `Ctrl+C` 停止服务器，或查找进程并终止：

```bash
# 查找进程
lsof -i :5244

# 终止进程（替换PID为实际进程ID）
kill [PID]
```

### 后台运行

```bash
cd /Users/rinshibuya/Downloads/ArtPlus
nohup ./openlist server > openlist.log 2>&1 &
```

## 管理命令

### 重置管理员密码

```bash
# 设置新密码
./openlist admin set 新密码

# 生成随机密码
./openlist admin random
```

### 查看管理员信息

```bash
./openlist admin
```

## 配置文件

OpenList的配置文件位于：`data/config.json`

首次启动时会自动创建默认配置文件。您可以编辑此文件来配置：
- 服务器端口
- 存储驱动
- 其他高级设置

## 功能特性

根据 [OpenList GitHub仓库](https://github.com/OpenListTeam/OpenList/)，OpenList支持：

- ✅ 多种存储驱动（本地、阿里云盘、OneDrive、Google Drive等）
- ✅ 文件预览（PDF、Markdown、代码、图片、视频等）
- ✅ 文件上传、下载、删除、重命名等操作
- ✅ WebDAV支持（路径：`/dav/`）
- ✅ 暗色模式
- ✅ 多语言支持
- ✅ 密码保护和认证

## WebDAV配置

### 手机文件管理器配置

如果您的手机应用使用WebDAV协议访问，请使用以下配置：

- **服务器地址**: `http://192.168.31.247:5244/dav/` （注意末尾的 `/dav/`）
- **用户名**: `admin`
- **密码**: `admin123`
- **端口**: `5244`

⚠️ **常见错误**：
- ❌ 使用 `http://192.168.31.247:5244/` 会导致 405 Method Not Allowed 错误
- ✅ 正确使用 `http://192.168.31.247:5244/dav/` 才能正常访问

## 默认配置

- **数据目录**: `data/`
- **配置文件**: `data/config.json`
- **默认端口**: 5244
- **管理员用户名**: admin

## 注意事项

1. **首次启动**: OpenList会自动创建默认配置文件和admin用户
2. **密码安全**: 初始密码已设置为 `admin123`，建议在生产环境中修改
3. **数据目录**: 所有数据存储在 `data/` 目录下
4. **端口冲突**: 如果5244端口被占用，可以在配置文件中修改端口

## 故障排除

### 端口被占用

如果5244端口被占用，可以：
1. 编辑 `data/config.json`
2. 修改 `port` 字段
3. 重启服务器

### 无法访问

1. 检查服务器是否运行：`lsof -i :5244`
2. 检查防火墙设置
3. 检查配置文件中的 `host` 设置

### 忘记密码

```bash
# 生成新的随机密码
./openlist admin random

# 或设置新密码
./openlist admin set 新密码
```

## 相关链接

- [OpenList GitHub](https://github.com/OpenListTeam/OpenList/)
- [OpenList 文档](https://doc.oplist.org/)

