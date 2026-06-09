# 虚拟环境配置说明

## 快速配置

运行以下命令自动配置虚拟环境：

```bash
cd /Users/rinshibuya/Downloads/ArtPlus
bash setup_venv.sh
```

或者手动配置：

## 手动配置步骤

### 1. 激活虚拟环境

```bash
cd /Users/rinshibuya/Downloads/ArtPlus
source .venv/bin/activate
```

### 2. 升级pip

```bash
python -m pip install --upgrade pip
```

### 3. 安装依赖

```bash
python -m pip install -r requirements.txt
```

### 4. 验证安装

```bash
python -c "import cv2; import numpy; from rembg import new_session; print('所有依赖已安装')"
```

## 依赖说明

项目需要以下依赖：

- **Pillow** - 图像处理
- **opencv-python** - GrabCut和SAM2算法
- **rembg[cpu]** - U2Net背景移除（包含CPU支持）
- **numpy<2.0.0** - 数值计算（限制版本以兼容opencv）
- **scikit-learn** - 颜色聚类
- **scipy** - 科学计算

## 常见问题

### 1. NumPy版本冲突

如果遇到NumPy版本冲突（opencv需要numpy<2.0），requirements.txt已限制numpy版本。

### 2. rembg缺少onnxruntime

确保安装 `rembg[cpu]` 而不是 `rembg`：

```bash
pip install "rembg[cpu]"
```

### 3. 权限问题

如果遇到权限问题，尝试：

```bash
python -m pip install --user -r requirements.txt
```

## 使用虚拟环境

### 激活环境

```bash
source .venv/bin/activate
```

### 运行脚本

```bash
# 测试所有分离方法
python cli/test_all_separation_methods.py

# 单独测试U2Net
python cli/test_u2net_only.py

# 重新生成预览图
python cli/regenerate_previews.py
```

### 退出环境

```bash
deactivate
```

## 环境信息

- Python版本: 3.11.4
- 虚拟环境路径: `.venv/`
- 依赖文件: `requirements.txt`

