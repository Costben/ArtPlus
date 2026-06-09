#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$ROOT_DIR"

echo "使用rembg批量分离图标前景和背景"
echo "===================================="
echo ""
echo "规则:"
echo "- 跳过已有官方ART图标的包"
echo "- 只处理非官方命名的PNG文件"
echo "- 使用rembg分离前景和背景"
echo ""
echo "注意: 首次运行需要下载模型（约170MB）"
echo ""
echo "依赖要求:"
echo "- rembg库（已安装）"
echo "- onnxruntime库（已安装）"
echo ""
read -r -p "按回车开始..." _

python3 src/separate_icons_with_rembg.py

echo ""
read -r -p "执行完成，按回车退出..." _

