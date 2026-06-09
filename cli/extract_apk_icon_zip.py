#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Convert one APK into the icon ZIP format accepted by the ArtPlus pipeline."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))

from artplus_apk_icon_zip import ApkIconExtractionError, extract_apk_icon_zip  # noqa: E402


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="从 APK 快速提取 launcher 图标资源并打成 ArtPlus 图标 ZIP")
    parser.add_argument("apk", help="输入 APK 文件")
    parser.add_argument("-o", "--output", help="输出 ZIP；默认写到 APK 同目录的 .apk_icon.zip")
    parser.add_argument("--package-name", help="手动指定包名；默认从 manifest/resources.arsc 读取")
    parser.add_argument("--normalize-icon-png", action="store_true", help="额外解码/缩放主图为 512x512 icon.png；默认原样复制更快")
    parser.add_argument("--compress-output", action="store_true", help="对输出 ZIP 做 deflate 压缩；默认不二次压缩以提高速度")
    parser.add_argument("--json", action="store_true", help="输出机器可读 JSON")
    return parser


def main() -> int:
    args = build_parser().parse_args()
    try:
        result = extract_apk_icon_zip(
            Path(args.apk),
            Path(args.output) if args.output else None,
            package_name=args.package_name,
            normalize_icon_png=args.normalize_icon_png,
            compress_output=args.compress_output,
        )
    except ApkIconExtractionError as exc:
        if args.json:
            print(json.dumps({"success": False, "error": str(exc)}, ensure_ascii=False))
        else:
            print(f"错误: {exc}", file=sys.stderr)
        return 1

    payload = {
        "success": True,
        "package_name": result.package_name,
        "output_zip": str(result.output_zip),
        "selected_icon_path": result.selected_icon_path,
        "extracted_members": result.extracted_members,
        "generated_members": result.generated_members,
    }
    if args.json:
        print(json.dumps(payload, ensure_ascii=False, indent=2))
    else:
        print("完成：已生成图标 ZIP")
        print(f"  包名: {result.package_name}")
        print(f"  输出: {result.output_zip}")
        print(f"  主图: {result.selected_icon_path}")
        print(f"  资源数: {len(result.extracted_members)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
