#!/usr/bin/env bash
# ArtPlus 前端结构治理 Epic v3 — 固定门禁（S0.2）
#
# 用法:
#   bash outputs/research/frontend-gate.sh              # 完整门禁：编译 + 单测 + 指标
#   bash outputs/research/frontend-gate.sh --fast       # 只跑指标（改文案/注释时用）
#   bash outputs/research/frontend-gate.sh --baseline   # 重写基线 JSON（只在 P0 用）
#
# 退出码: 0 = 全绿；1 = 编译/单测/指标任一红灯。
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT" || exit 1

export JAVA_HOME="${JAVA_HOME:-$HOME/.gradle/jdks/eclipse_adoptium-17-aarch64-os_x.2/jdk-17.0.17+10/Contents/Home}"
export PATH="$JAVA_HOME/bin:$PATH"

AUDIT="outputs/research/frontend-audit.py"
BASELINE="outputs/research/frontend-baseline-20261022.json"
OUT="outputs/tmp"
mkdir -p "$OUT"

MODE="${1:-full}"
FAIL=0

if [[ "$MODE" == "--baseline" ]]; then
  python3 "$AUDIT" --json > "$BASELINE"
  echo "baseline written: $BASELINE"
  exit 0
fi

if [[ "$MODE" != "--fast" ]]; then
  echo "== [1/3] :mobile:assembleDebug =="
  ./gradlew :mobile:assembleDebug --console=plain -q || FAIL=1
  echo "== [2/3] :mobile:testDebugUnitTest =="
  ./gradlew :mobile:testDebugUnitTest --console=plain -q || FAIL=1
else
  echo "== [1/3][2/3] skipped (--fast) =="
fi

echo "== [3/3] frontend-audit =="
python3 "$AUDIT" --json > "$OUT/audit-now.json" || FAIL=1
python3 "$AUDIT" --strict || FAIL=1

# 与基线逐项对比：任何指标劣化即红灯（新增/删除指标忽略）
if [[ -f "$BASELINE" ]]; then
  python3 - "$BASELINE" "$OUT/audit-now.json" <<'PY'
import json, sys
base = json.load(open(sys.argv[1]))
now = json.load(open(sys.argv[2]))
bad = []
for k, v in base.items():
    if k not in now or not isinstance(v, (int, float)) or not isinstance(now[k], (int, float)):
        continue
    if k in ("unit_tests", "android_tests", "coui_icons", "string_resources"):
        if now[k] < v:
            bad.append(f"{k}: {v} -> {now[k]} (只增不减)")
        continue
    if now[k] > v:
        bad.append(f"{k}: {v} -> {now[k]} (劣化)")
if bad:
    print("BASELINE REGRESSION:")
    for b in bad:
        print("  -", b)
    sys.exit(1)
print("baseline diff: OK (无劣化)")
PY
  [[ $? -ne 0 ]] && FAIL=1
fi

if [[ $FAIL -ne 0 ]]; then
  echo "GATE: RED"
  exit 1
fi
echo "GATE: GREEN"
