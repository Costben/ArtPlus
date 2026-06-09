#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""验证生成的图标文件"""

from pathlib import Path
import sys
from PIL import Image

# 添加src目录到路径
script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))

from project_helper import get_new_artplus_dir

app_dir = get_new_artplus_dir() / "app.alextran.immich"

# 预期的文件尺寸
expected_sizes = {
    "recbg.png": (240, 240),
    "recfg.png": (240, 240),
    "recbg_1x2.png": (240, 820),
    "recbg_2x1.png": (820, 240),
    "recbg_2x2.png": (704, 704),
    "recfg_1x2.png": (240, 820),
    "recfg_2x1.png": (820, 240),
    "recfg_2x2.png": (704, 704),
    "rec_night.png": (240, 240),
    "rec_night_1x2.png": (240, 820),
    "rec_night_2x1.png": (820, 240),
    "rec_night_2x2.png": (704, 704),
    "monochrome.png": (240, 240),
    "monochrome_1x2.png": (240, 820),
    "monochrome_2x1.png": (820, 240),
    "monochrome_2x2.png": (704, 704),
    "day.png": (240, 240),
    "nsd.png": (240, 240),
    "mat.png": (240, 240),
    "peb.png": (240, 240),
}

print("验证图标文件尺寸:")
print("="*60)

all_ok = True
for filename, expected_size in expected_sizes.items():
    file_path = app_dir / filename
    if file_path.exists():
        img = Image.open(file_path)
        actual_size = img.size
        if actual_size == expected_size:
            print(f"[OK] {filename:25s} {actual_size}")
        else:
            print(f"[FAIL] {filename:25s} {actual_size} (期望: {expected_size})")
            all_ok = False
    else:
        print(f"[MISS] {filename:25s} 文件不存在")
        all_ok = False

print("="*60)
if all_ok:
    print("所有文件验证通过！")
else:
    print("部分文件验证失败")
