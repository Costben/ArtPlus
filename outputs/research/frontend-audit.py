#!/usr/bin/env python3
"""ArtPlus 前端结构审计（只读）。

用途：给 Epic v3 每个 Slice 的"门禁"提供可复现的量化指标。
不修改任何文件；输出 markdown 表格，可直接贴进进度看板。

用法:
    python3 outputs/research/frontend-audit.py            # 表格
    python3 outputs/research/frontend-audit.py --json     # 机器可读
    python3 outputs/research/frontend-audit.py --strict   # 任一指标劣化则 exit 1（CI 用）

基线（2026-10-22，HEAD e036048 / feat/coui17）见 frontend-repair-plan.md §1。
"""
from __future__ import annotations

import argparse
import json
import pathlib
import re
import sys
from collections import Counter

ROOT = pathlib.Path(__file__).resolve().parents[2]
SRC = ROOT / "mobile/src/main/kotlin/dev/artplus/mobile"
UI = SRC / "ui"
RES = ROOT / "mobile/src/main/res/values/strings.xml"

# 目标值（Epic v3 终局）。None = 不参与 strict 判定。
TARGETS = {
    "ui_lines": 16000,
    "homepage_lines": 1200,
    "homepage_longest_fun": 300,
    "unused_imports": 0,
    "import_line_ratio_pct": 3.0,
    "dead_glass_lines": 0,
    "params_current_wrapper": 0,
    "picker_save_sites": 1,
    "onwrite_blocks": 0,
    "inline_collect_as_state": 0,
    "hardcoded_cjk": 0,
    "content_description_null": 5,
    "files_in_dev_artplus_mobile": 20,
}


def read(p: pathlib.Path) -> str:
    return p.read_text(encoding="utf-8", errors="replace")


# Kotlin 委托/操作符符号：由 `by` / 运算符语法隐式使用，正文里不会出现字面名。
OPERATOR_SYMBOLS = {
    "getValue", "setValue", "provideDelegate", "invoke", "get", "set", "contains",
    "iterator", "next", "hasNext", "compareTo", "rangeTo", "rangeUntil",
    "plus", "minus", "times", "div", "rem", "unaryPlus", "unaryMinus", "not",
    "inc", "dec", "plusAssign", "minusAssign", "timesAssign", "divAssign",
    "remAssign", "component1", "component2", "component3", "component4", "component5",
}


def strip_comments(text: str) -> str:
    """去掉 // 与 /* */ 注释，但不误伤字符串字面量里的 `/*`（如 `"$dir/*.png"`）。

    逐字符扫描：双引号内不识别注释；`/* */` 支持嵌套式配对（Kotlin 允许嵌套块注释）。
    """
    out: list[str] = []
    i, n = 0, len(text)
    in_str = False
    while i < n:
        c = text[i]
        if in_str:
            out.append(c)
            if c == "\\" and i + 1 < n:
                out.append(text[i + 1])
                i += 2
                continue
            if c == '"':
                in_str = False
            i += 1
            continue
        if c == '"':
            in_str = True
            out.append(c)
            i += 1
            continue
        if c == "/" and i + 1 < n and text[i + 1] == "/":
            while i < n and text[i] != "\n":
                i += 1
            continue
        if c == "/" and i + 1 < n and text[i + 1] == "*":
            depth, i = 1, i + 2
            while i < n and depth:
                if text.startswith("/*", i):
                    depth, i = depth + 1, i + 2
                elif text.startswith("*/", i):
                    depth, i = depth - 1, i + 2
                else:
                    i += 1
            continue
        out.append(c)
        i += 1
    return "".join(out)


def kt_files(base: pathlib.Path) -> list[pathlib.Path]:
    return sorted(base.rglob("*.kt"))


def longest_function_span(path: pathlib.Path) -> tuple[str, int]:
    """粗略：从顶层 `fun X(` 到下一个顶层 `}` / 下一个顶层声明，取最长跨度。"""
    lines = read(path).splitlines()
    starts: list[tuple[int, str]] = []
    for i, ln in enumerate(lines):
        m = re.match(r"(?:internal |private |public )?(?:suspend )?fun (?:[A-Za-z0-9_.]+\.)?(\w+)\(", ln)
        if m:
            starts.append((i, m.group(1)))
    best = ("", 0)
    for idx, (start, name) in enumerate(starts):
        end = starts[idx + 1][0] if idx + 1 < len(starts) else len(lines)
        span = end - start
        if span > best[1]:
            best = (name, span)
    return best


def audit() -> dict:
    files = kt_files(SRC)
    ui_files = kt_files(UI)
    all_text = {f: read(f) for f in files}

    ui_lines = sum(len(t.splitlines()) for f, t in all_text.items() if UI in f.parents or f == UI)
    ui_import_lines = sum(
        1 for f, t in all_text.items() if (UI in f.parents or f == UI) for ln in t.splitlines() if ln.startswith("import ")
    )

    # 未使用 import：符号名在去掉 import 行与注释后的正文中不出现
    total_imports = unused_imports = 0
    for f, text in all_text.items():
        imports = [ln[len("import "):].strip() for ln in text.splitlines() if ln.startswith("import ")]
        body = strip_comments("\n".join(ln for ln in text.splitlines() if not ln.startswith("import ")))
        for imp in imports:
            total_imports += 1
            name = imp.split(" as ")[-1].strip() if " as " in imp else imp.split(".")[-1]
            if name == "*" or name in OPERATOR_SYMBOLS:
                continue
            if not re.search(r"\b" + re.escape(name) + r"\b", body):
                unused_imports += 1

    packages = Counter(re.search(r"^package\s+([\w.]+)", t, re.M).group(1) for t in all_text.values() if re.search(r"^package\s+([\w.]+)", t, re.M))

    # HomePage
    homepage = SRC / "ui/pages/HomePage.kt"
    hp_text = read(homepage)
    hp_name, hp_span = longest_function_span(homepage)

    # 重复片段
    def count_in_ui(pattern: str) -> int:
        return sum(len(re.findall(pattern, t)) for f, t in all_text.items() if UI in f.parents or f == UI)

    def count_all(pattern: str) -> int:
        return sum(len(re.findall(pattern, t)) for t in all_text.values())

    # glass 死代码
    glass_files = kt_files(SRC / "glass")
    glass_lines = sum(len(read(f).splitlines()) for f in glass_files)
    glass_symbols: set[str] = set()
    for f in glass_files:
        for ln in read(f).splitlines():
            m = re.match(r"(?:internal |private |public )?(?:@Composable\s*)?fun (?:[A-Za-z0-9_.]+\.)?(\w+)\(", ln)
            if m:
                glass_symbols.add(m.group(1))
            m = re.match(r"(?:internal |private |public )?(?:data )?class (\w+)", ln)
            if m:
                glass_symbols.add(m.group(1))
    glass_refs = 0
    for f, t in all_text.items():
        if SRC / "glass" in f.parents:
            continue
        body = strip_comments("\n".join(ln for ln in t.splitlines() if not ln.startswith("import ")))
        glass_refs += sum(1 for s in glass_symbols if re.search(r"\b" + re.escape(s) + r"\b", body))

    # 文案 / 无障碍 / 图标
    hardcoded_cjk = 0
    for f, t in all_text.items():
        if not (UI in f.parents or f == UI):
            continue
        body = strip_comments(t)
        hardcoded_cjk += len(re.findall(r'"[^"\n]*[\u4e00-\u9fff][^"\n]*"', body))

    strings_xml = read(RES) if RES.exists() else ""
    string_entries = len(re.findall(r"<string\b", strings_xml))
    cd_null = count_in_ui(r"contentDescription\s*=\s*null")
    coui_icons = count_in_ui(r"COUIIcons\.")
    lucide_icons = count_in_ui(r"Lucide\.")

    # 测试
    results = ROOT / "mobile/build/test-results/testDebugUnitTest"
    tests = 0
    if results.exists():
        for xml in results.glob("*.xml"):
            m = re.search(r'tests="(\d+)"', read(xml))
            if m:
                tests += int(m.group(1))

    return {
        "kt_files": len(files),
        "ui_files": len(ui_files),
        "ui_lines": ui_lines,
        "import_lines": ui_import_lines,
        "import_line_ratio_pct": round(ui_import_lines / ui_lines * 100, 1) if ui_lines else 0,
        "imports_total": total_imports,
        "unused_imports": unused_imports,
        "unused_imports_pct": round(unused_imports / total_imports * 100, 1) if total_imports else 0,
        "packages": len(packages),
        "files_in_dev_artplus_mobile": packages.get("dev.artplus.mobile", 0),
        "homepage_lines": len(hp_text.splitlines()),
        "homepage_longest_fun": hp_span,
        "homepage_longest_fun_name": hp_name,
        "homepage_run_blocks": len(re.findall(r"run\s*\{", hp_text)),
        "onwrite_blocks": count_all(r"onWrite = \{ session, selections ->"),
        "picker_save_sites": count_all(r"pickerSaveUiState\("),
        "write_package_sites": count_all(r"writePackageOutputs\("),
        "liquid_glass_save_sites": count_all(r"paramsSaveLiquidGlassSettings\("),
        "params_current_wrapper": count_all(r"paramsCurrentTuningParams\("),
        "update_shell_calls": count_all(r"updateShell \{"),
        "update_preview_calls": count_all(r"updatePreviewSession \{"),
        "inline_collect_as_state": sum(
            1 for t in all_text.values() for ln in t.splitlines()
            if ".collectAsState()" in ln and not re.match(r"\s*val \w+ by ", ln)
        ),
        "value_reads": count_in_ui(r"\.value\b"),
        "dead_glass_lines": glass_lines,
        "dead_glass_symbols": len(glass_symbols),
        "dead_glass_refs_outside": glass_refs,
        "hardcoded_cjk": hardcoded_cjk,
        "string_resources": string_entries,
        "content_description_null": cd_null,
        "coui_icons": coui_icons,
        "lucide_icons": lucide_icons,
        "unit_tests": tests,
        "android_tests": len(list((ROOT / "mobile/src/androidTest").rglob("*.kt"))) if (ROOT / "mobile/src/androidTest").exists() else 0,
    }


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--json", action="store_true")
    ap.add_argument("--strict", action="store_true")
    args = ap.parse_args()

    m = audit()
    if args.json:
        print(json.dumps(m, ensure_ascii=False, indent=2))
    else:
        print("| 指标 | 当前 | 目标 |")
        print("|---|---|---|")
        for k, target in TARGETS.items():
            cur = m.get(k)
            flag = ""
            if isinstance(target, (int, float)) and isinstance(cur, (int, float)):
                flag = " ✅" if cur <= target else " ❌"
            print(f"| `{k}` | {cur}{flag} | ≤ {target} |")
        print()
        print("| 补充 | 值 |")
        print("|---|---|")
        for k in ("imports_total", "unused_imports_pct", "files_in_dev_artplus_mobile",
                  "homepage_longest_fun_name", "homepage_run_blocks", "dead_glass_refs_outside",
                  "update_shell_calls", "update_preview_calls", "value_reads",
                  "string_resources", "coui_icons", "lucide_icons", "unit_tests", "android_tests"):
            print(f"| `{k}` | {m[k]} |")

    if args.strict:
        bad = [k for k, t in TARGETS.items() if isinstance(m.get(k), (int, float)) and m[k] > t]
        if bad:
            print("\nSTRICT FAIL: " + ", ".join(bad), file=sys.stderr)
            return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
