#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$ROOT_DIR"

if [[ -x "$ROOT_DIR/.venv/bin/python" ]]; then
  PYTHON="$ROOT_DIR/.venv/bin/python"
else
  PYTHON="python3"
fi

echo "APK + 图标ZIP → OPPO /data/oplus/uxicons 可用目录"
echo "============================================================"
echo "默认读取 SMB: smb://ERILNG-NAS._smb._tcp.local/Download/ArtPlusUpload"
echo "输出目录: outputs/latest/uxicons/<package_name>/"
echo ""

"$PYTHON" cli/run_apk_zip_to_oppo_uxicons.py
