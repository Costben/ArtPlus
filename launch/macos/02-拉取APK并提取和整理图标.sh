#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$ROOT_DIR"

echo "============================================================"
echo "拉取APK、提取图标并整理"
echo "============================================================"
echo ""
echo "本脚本将依次执行以下步骤:"
echo "1. 拉取缺少PNG图标的应用APK并提取图标"
echo "2. 整理并转换提取的图标"
echo "3. 清理目录，只保留PNG文件"
echo "4. 整理非标准图标，保留分辨率最高的"
echo "5. 清理重复图标文件"
echo ""
read -r -p "按回车开始..." _

run_step() {
  local step="$1"
  local title="$2"
  shift 2

  echo ""
  echo "============================================================"
  echo "步骤 ${step}/5: ${title}"
  echo "============================================================"
  echo ""

  if ! "$@"; then
    echo ""
    read -r -p "步骤${step}执行失败，是否继续执行后续步骤？(y/n): " continue
    if [[ "${continue}" != "y" && "${continue}" != "Y" ]]; then
      exit 1
    fi
  fi

  echo ""
  read -r -p "按回车继续..." _
}

run_step 1 "拉取APK并提取图标" python3 cli/extract_missing_apks.py
run_step 2 "整理并转换提取的图标" python3 cli/convert_and_organize_icons.py
run_step 3 "清理目录只保留PNG文件" python3 cli/cleanup_and_convert_xml.py
run_step 4 "整理非标准图标保留最高分辨率" python3 cli/organize_non_standard_icons.py
run_step 5 "清理重复图标文件" python3 cli/cleanup_duplicate_files.py

echo ""
echo "============================================================"
echo "所有步骤执行完成！"
echo "============================================================"
read -r -p "按回车退出..." _

