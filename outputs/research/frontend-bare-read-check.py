#!/usr/bin/env python3
"""S3.1：`ui/` 组合期裸读 `.value` 的棘轮检查（只读）。

规则：`ui/**` 里不允许新增 `mainViewModel.<flow>.value` 裸读。
组合期裸读不会订阅 StateFlow，导致界面 stale（历史 FAIL-1/4/5 就是这类读点）。

落地方式（与 lint baseline 同思路）：
- 计数不得超过 `frontend-bare-read-baseline.json` 里的基线；
- 事件回调（onClick / LaunchedEffect / remember 等 lambda）内的读是合法的，但静态无法区分，
  所以用「总量棘轮 + lint 的 StateFlowValueCalledInComposition 精确拦截」两层：
  lint 负责精确判组合期，本脚本负责防止总量回升。

用法:
    python3 outputs/research/frontend-bare-read-check.py            # 检查（超基线 exit 1）
    python3 outputs/research/frontend-bare-read-check.py --update   # 重写基线
"""
from __future__ import annotations

import argparse
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
UI = ROOT / "mobile/src/main/kotlin/dev/artplus/mobile/ui"
BASELINE = pathlib.Path(__file__).with_name("frontend-bare-read-baseline.json")
PAT = re.compile(r"mainViewModel\.\w+\.value")


def scan() -> tuple[int, dict[str, int]]:
    per: dict[str, int] = {}
    for f in sorted(UI.rglob("*.kt")):
        n = len(PAT.findall(f.read_text(encoding="utf-8", errors="replace")))
        if n:
            per[str(f.relative_to(ROOT))] = n
    return sum(per.values()), per


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--update", action="store_true")
    args = ap.parse_args()

    total, per = scan()
    if args.update:
        BASELINE.write_text(json.dumps({"total": total, "files": per}, ensure_ascii=False, indent=2) + "\n")
        print(f"baseline updated: total={total}")
        return 0

    base = json.loads(BASELINE.read_text()) if BASELINE.exists() else {"total": total}
    print(f"bare reads in ui/: {total} (baseline {base['total']})")
    if total > base["total"]:
        print("BARE-READ REGRESSION: 新增组合期裸读", file=sys.stderr)
        for f, n in per.items():
            if n > base.get("files", {}).get(f, 0):
                print(f"  {f}: {base.get('files', {}).get(f, 0)} -> {n}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
