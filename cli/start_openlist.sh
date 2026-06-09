#!/bin/bash
# 启动OpenList服务器

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"
OPENLIST_BIN="$PROJECT_ROOT/openlist"

cd "$PROJECT_ROOT"

# 检查可执行文件是否存在
if [ ! -f "$OPENLIST_BIN" ]; then
    echo "错误: 未找到 openlist 可执行文件"
    echo "请确保 openlist 文件在项目根目录下"
    exit 1
fi

# 确保有执行权限
chmod +x "$OPENLIST_BIN"

# 创建data目录（如果不存在）
mkdir -p data

echo "============================================================"
echo "启动 OpenList 服务器"
echo "============================================================"
echo ""
echo "可执行文件: $OPENLIST_BIN"
echo "工作目录: $PROJECT_ROOT"
echo ""
echo "按 Ctrl+C 停止服务器"
echo "============================================================"
echo ""

# 启动OpenList服务器
# OpenList默认端口是5244，可以通过环境变量或配置文件修改
exec "$OPENLIST_BIN" server


