#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Regenerate ART+ monochrome PNGs as luminance-driven alpha masks."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))

from generate_all_artplus_icons import (  # noqa: E402
    SIZE_1x1,
    SIZE_1x2,
    SIZE_2x1,
    SIZE_2x2,
    create_monochrome_icon,
)
from project_helper import get_path  # noqa: E402


MONOCHROME_TARGETS = {
    "monochrome.png": SIZE_1x1,
    "monochrome_1x2.png": SIZE_1x2,
    "monochrome_2x1.png": SIZE_2x1,
    "monochrome_2x2.png": SIZE_2x2,
}


def find_package_dirs(root: Path, package: str | None) -> list[Path]:
    if package:
        candidate = root / package
        return [candidate] if (candidate / "recfg.png").is_file() else []
    if (root / "recfg.png").is_file():
        return [root]
    return sorted(path.parent for path in root.glob("*/recfg.png") if path.is_file())


def regenerate_package(package_dir: Path, dry_run: bool = False) -> list[Path]:
    recfg_path = package_dir / "recfg.png"
    if not recfg_path.is_file():
        return []
    written: list[Path] = []
    for filename, size in MONOCHROME_TARGETS.items():
        output_path = package_dir / filename
        if not dry_run:
            create_monochrome_icon(recfg_path, size).save(output_path, "PNG", optimize=True)
        written.append(output_path)
    return written


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="重算旧 ART+ 目录里的 monochrome alpha mask")
    parser.add_argument(
        "--root",
        action="append",
        help=(
            "包含包名目录的根目录，可重复；默认处理 outputs/new_artplus 和 outputs/latest/uxicons"
        ),
    )
    parser.add_argument("--package", help="只处理指定包名")
    parser.add_argument("--dry-run", action="store_true", help="只列出将处理的文件，不写入")
    return parser


def main() -> int:
    args = build_parser().parse_args()
    roots = [Path(value).expanduser().resolve() for value in args.root or []]
    if not roots:
        roots = [get_path("outputs/new_artplus"), get_path("outputs/latest/uxicons")]

    total_packages = 0
    total_files = 0
    for root in roots:
        if not root.exists():
            continue
        package_dirs = find_package_dirs(root, args.package)
        for package_dir in package_dirs:
            written = regenerate_package(package_dir, dry_run=args.dry_run)
            if not written:
                continue
            total_packages += 1
            total_files += len(written)
            action = "将重算" if args.dry_run else "已重算"
            print(f"{action}: {package_dir.name} ({len(written)} files)")

    print(f"完成: {total_packages} packages, {total_files} files")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
