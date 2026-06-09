# 文件共享服务器使用说明

## 服务器状态

✅ **服务器已启动并运行在 5244 端口**

## 配置信息

- **端口**: 5244
- **用户名**: admin
- **密码**: admin123
- **共享目录**: `/Users/rinshibuya/Downloads/ArtPlus` (项目根目录)

## 访问方式

### 本地访问
```
http://localhost:5244/
```

### 局域网访问
```
http://[您的IP地址]:5244/
```

查看本机IP地址：
```bash
ifconfig | grep "inet " | grep -v 127.0.0.1
```

## 使用方法

### 1. 在浏览器中访问

1. 打开浏览器
2. 访问 `http://localhost:5244/`
3. 输入用户名：`admin`
4. 输入密码：`admin123`
5. 即可浏览和下载项目文件

### 2. 使用curl访问

```bash
# 下载文件
curl -u admin:admin123 http://localhost:5244/文件名

# 列出目录
curl -u admin:admin123 http://localhost:5244/目录名/
```

### 3. 使用wget访问

```bash
wget --user=admin --password=admin123 http://localhost:5244/文件名
```

## 管理服务器

### 启动服务器

```bash
cd /Users/rinshibuya/Downloads/ArtPlus
python3 cli/start_file_server.py
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
nohup python3 cli/start_file_server.py > server.log 2>&1 &
```

## 注意事项

1. **安全性**: 此服务器使用基本认证，密码以明文传输，仅适用于内网环境
2. **防火墙**: 确保5244端口未被防火墙阻止
3. **权限**: 服务器共享的是整个项目根目录，请确保敏感文件已添加到 `.gitignore`
4. **性能**: 此服务器适合小规模文件共享，不适合高并发访问

## 故障排除

### 端口被占用

如果5244端口被占用，可以修改 `cli/start_file_server.py` 中的 `PORT` 变量。

### 无法访问

1. 检查服务器是否运行：`lsof -i :5244`
2. 检查防火墙设置
3. 检查IP地址是否正确

### 认证失败

确保使用正确的用户名和密码：
- 用户名：`admin`
- 密码：`admin123`

