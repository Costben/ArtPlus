#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
检查outputs/new_artplus目录中包含PNG文件的包名文件夹
"""

import sys
import io
from pathlib import Path

# 设置输出编码
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# 添加src目录到路径
script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))

from project_helper import get_path


def check_png_directories():
    """检查outputs/new_artplus目录中包含PNG文件的包名文件夹"""
    base_dir = get_path("outputs/new_artplus")
    
    if not base_dir.exists():
        print(f"错误: 目录不存在: {base_dir}")
        return
    
    print("="*60)
    print("检查outputs/new_artplus中包含PNG文件的包名文件夹")
    print("="*60)
    print(f"目标目录: {base_dir}\n")
    
    app_dirs = [d for d in base_dir.iterdir() if d.is_dir()]
    
    if len(app_dirs) == 0:
        print("未找到应用目录")
        return
    
    png_directories = []
    no_png_directories = []
    
    for app_dir in app_dirs:
        package_name = app_dir.name
        
        # 检查是否包含PNG文件（排除src目录）
        png_files = [f for f in app_dir.iterdir() 
                    if f.is_file() and f.suffix.lower() == '.png']
        
        if png_files:
            png_count = len(png_files)
            png_directories.append((package_name, png_count))
        else:
            no_png_directories.append(package_name)
    
    # 按包名排序
    png_directories.sort(key=lambda x: x[0])
    no_png_directories.sort()
    
    print(f"找到 {len(png_directories)} 个包含PNG文件的文件夹")
    print(f"找到 {len(no_png_directories)} 个不包含PNG文件的文件夹\n")
    
    if png_directories:
        print("="*60)
        print("包含PNG文件的文件夹列表:")
        print("="*60)
        for i, (package_name, png_count) in enumerate(png_directories, 1):
            print(f"{i:3d}. {package_name:<50} ({png_count} 个PNG文件)")
    
    if no_png_directories:
        print("\n" + "="*60)
        print("不包含PNG文件的文件夹列表:")
        print("="*60)
        for i, package_name in enumerate(no_png_directories, 1):
            print(f"{i:3d}. {package_name}")
    
    print("\n" + "="*60)
    print(f"总计: {len(png_directories)} 个包含PNG, {len(no_png_directories)} 个不包含PNG")
    print("="*60)


if __name__ == "__main__":
    check_png_directories()
