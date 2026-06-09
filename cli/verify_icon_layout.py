#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""验证图标布局是否正确（前景居中，不拉伸）"""

from PIL import Image
import numpy as np
from pathlib import Path
import sys

# 添加src目录到路径
script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))

from project_helper import get_new_artplus_dir

app_dir = get_new_artplus_dir() / "app.alextran.immich"

# 加载原始前景
fg_original = Image.open(app_dir / "recfg.png")

# 检查各个变形图标
test_files = [
    ("recfg_1x2.png", (240, 820)),
    ("recfg_2x1.png", (820, 240)),
    ("recfg_2x2.png", (704, 704)),
]

print("验证前景图标布局:")
print("="*60)

all_ok = True
for filename, expected_size in test_files:
    file_path = app_dir / filename
    fg_deformed = Image.open(file_path)
    
    # 检查画布尺寸
    if fg_deformed.size != expected_size:
        print(f"[FAIL] {filename}: 画布尺寸错误 {fg_deformed.size} (期望: {expected_size})")
        all_ok = False
        continue
    
    # 检查中心区域是否与原始图标匹配
    center_x = (fg_deformed.width - fg_original.width) // 2
    center_y = (fg_deformed.height - fg_original.height) // 2
    
    center_region = fg_deformed.crop((
        center_x, 
        center_y, 
        center_x + fg_original.width, 
        center_y + fg_original.height
    ))
    
    # 转换为数组比较
    orig_array = np.array(fg_original.convert('RGBA'))
    center_array = np.array(center_region.convert('RGBA'))
    
    # 计算匹配度（允许一些压缩误差）
    diff = np.abs(orig_array.astype(int) - center_array.astype(int))
    match_ratio = np.sum(diff < 15) / diff.size
    
    if match_ratio > 0.85:
        print(f"[OK] {filename:20s} 画布: {fg_deformed.size}, 中心区域匹配度: {match_ratio*100:.1f}%")
    else:
        print(f"[WARN] {filename:20s} 中心区域匹配度较低: {match_ratio*100:.1f}%")
        all_ok = False

print("="*60)
if all_ok:
    print("所有图标布局验证通过！前景图标居中放置，未拉伸。")
else:
    print("部分图标可能需要检查。")
