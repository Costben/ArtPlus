#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""检查官方图标的布局方式"""

from PIL import Image
import numpy as np

# 加载官方图标
fg_original = Image.open('uxicons/air.tv.douyu.android/recfg.png')
fg_1x2 = Image.open('uxicons/air.tv.douyu.android/recfg_1x2.png')
bg_1x2 = Image.open('uxicons/air.tv.douyu.android/recbg_1x2.png')

print("原始前景尺寸:", fg_original.size)
print("1x2前景尺寸:", fg_1x2.size)
print("1x2背景尺寸:", bg_1x2.size)

# 检查1x2前景的中心区域是否与原始图标匹配
# 1x2是240x820，原始是240x240
# 如果居中放置，应该在y坐标 (820-240)/2 = 290 的位置
center_y = (fg_1x2.height - fg_original.height) // 2
center_region = fg_1x2.crop((0, center_y, fg_original.width, center_y + fg_original.height))

print(f"\n1x2前景中心区域 (y={center_y}):", center_region.size)

# 比较中心区域和原始图标
if center_region.size == fg_original.size:
    # 转换为numpy数组比较
    original_array = np.array(fg_original.convert('RGBA'))
    center_array = np.array(center_region.convert('RGBA'))
    
    # 检查是否匹配（允许一些误差）
    diff = np.abs(original_array.astype(int) - center_array.astype(int))
    match_ratio = np.sum(diff < 10) / diff.size
    
    print(f"中心区域与原始图标匹配度: {match_ratio*100:.2f}%")
    
    if match_ratio > 0.9:
        print("结论: 1x2图标是画布扩展，前景图标居中放置")
    else:
        print("结论: 1x2图标可能是拉伸的")
else:
    print("结论: 尺寸不匹配，可能是拉伸的")
