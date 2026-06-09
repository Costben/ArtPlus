#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
预览ART+图标效果
将前景和背景组合显示
"""

from pathlib import Path
from PIL import Image
import sys


def preview_icon(app_dir: str):
    """
    预览图标效果
    
    Args:
        app_dir: 应用目录路径
    """
    app_path = Path(app_dir)
    
    recbg_path = app_path / "recbg.png"
    recfg_path = app_path / "recfg.png"
    
    if not recbg_path.exists():
        print(f"错误: 未找到 recbg.png")
        return
    
    if not recfg_path.exists():
        print(f"错误: 未找到 recfg.png")
        return
    
    # 加载背景和前景
    bg = Image.open(recbg_path).convert('RGB')
    fg = Image.open(recfg_path).convert('RGBA')
    
    # 确保尺寸一致
    if bg.size != fg.size:
        print(f"警告: 背景和前景尺寸不一致 ({bg.size} vs {fg.size})")
        fg = fg.resize(bg.size, Image.Resampling.LANCZOS)
    
    # 合并前景和背景
    combined = Image.alpha_composite(
        bg.convert('RGBA'),
        fg
    ).convert('RGB')
    
    # 保存预览
    preview_path = app_path / "preview.png"
    combined.save(preview_path, 'PNG', optimize=True)
    
    print(f"预览已保存: {preview_path}")
    print(f"尺寸: {combined.size}")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("用法: python preview_icon.py <应用目录路径>")
        print("示例: python preview_icon.py outputs/new_artplus/app.alextran.immich")
    else:
        preview_icon(sys.argv[1])
