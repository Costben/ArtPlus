#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$ROOT_DIR"

echo "ART+图标缺失检测工具"
echo "===================================="
echo ""
read -r -p "按回车开始..." _

python3 cli/check_artplus_icons_enhanced.py

echo ""
read -r -p "执行完成，按回车退出..." _

