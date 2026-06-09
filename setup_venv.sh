#!/bin/bash
# 配置项目虚拟Python环境

set -e

echo "============================================================"
echo "配置ArtPlus项目虚拟Python环境"
echo "============================================================"
echo ""

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# 检查虚拟环境
if [ ! -d ".venv" ]; then
    echo "创建虚拟环境..."
    python3 -m venv .venv
    echo "✓ 虚拟环境已创建"
else
    echo "✓ 虚拟环境已存在"
fi

# 激活虚拟环境
echo ""
echo "激活虚拟环境..."
source .venv/bin/activate

# 升级pip
echo ""
echo "升级pip..."
python -m pip install --upgrade pip

# 安装依赖
echo ""
echo "安装项目依赖..."
echo "这可能需要几分钟，请耐心等待..."
python -m pip install -r requirements.txt

# 验证安装
echo ""
echo "============================================================"
echo "验证安装..."
echo "============================================================"

python -c "
import sys
print(f'Python版本: {sys.version}')

try:
    import cv2
    print('✓ opencv-python 已安装')
except ImportError:
    print('✗ opencv-python 未安装')

try:
    import numpy
    print(f'✓ numpy 已安装 (版本: {numpy.__version__})')
except ImportError:
    print('✗ numpy 未安装')

try:
    from PIL import Image
    print('✓ Pillow 已安装')
except ImportError:
    print('✗ Pillow 未安装')

try:
    from rembg import remove, new_session
    print('✓ rembg 已安装')
    # 测试创建session
    try:
        session = new_session('u2net')
        print('✓ rembg session 创建成功')
    except Exception as e:
        print(f'⚠ rembg session 创建失败: {e}')
except ImportError:
    print('✗ rembg 未安装')

try:
    from sklearn.cluster import KMeans
    print('✓ scikit-learn 已安装')
except ImportError:
    print('✗ scikit-learn 未安装')

try:
    import scipy
    print('✓ scipy 已安装')
except ImportError:
    print('✗ scipy 未安装')
"

echo ""
echo "============================================================"
echo "环境配置完成！"
echo "============================================================"
echo ""
echo "使用方法："
echo "  激活虚拟环境: source .venv/bin/activate"
echo "  运行测试脚本: python cli/test_all_separation_methods.py"
echo "  退出虚拟环境: deactivate"
echo ""

