#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
为应用创建白色背景（recbg.png）
根据ColorOS ART+图标格式创建240x240的白色背景
"""

from pathlib import Path
from PIL import Image, ImageDraw
import sys


def create_white_background(app_dir: str, size: int = 240) -> bool:
    """
    为指定应用创建白色背景recbg.png
    
    Args:
        app_dir: 应用目录路径
        size: 图标尺寸（默认240）
        
    Returns:
        是否成功
    """
    app_path = Path(app_dir)
    
    if not app_path.exists():
        print(f"错误: 目录不存在: {app_dir}")
        return False
    
    # 检查recfg.png是否存在
    recfg_path = app_path / "recfg.png"
    if not recfg_path.exists():
        print(f"警告: 未找到 recfg.png，将创建纯白色背景")
    
    # 创建白色背景
    # 使用RGB模式，白色背景
    bg_image = Image.new('RGB', (size, size), color='white')
    
    # 保存为recbg.png
    output_path = app_path / "recbg.png"
    bg_image.save(output_path, 'PNG', optimize=True)
    
    print(f"成功创建白色背景: {output_path}")
    print(f"尺寸: {size}x{size}")
    
    return True


def main():
    """主函数"""
    if len(sys.argv) < 2:
        print("用法: python create_white_background.py <应用目录路径>")
        print("示例: python create_white_background.py outputs/new_artplus/app.alextran.immich")
        return
    
    app_dir = sys.argv[1]
    create_white_background(app_dir)


if __name__ == "__main__":
    main()
