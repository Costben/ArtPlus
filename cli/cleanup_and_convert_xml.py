#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
整理outputs/new_artplus目录，只保留PNG文件
删除所有非PNG文件（包括XML文件）
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


def cleanup_app_directory(app_dir: Path) -> tuple[int, int]:
    """
    整理单个应用目录，只保留PNG文件
    
    Returns:
        (保留的PNG数, 删除的文件数)
    """
    kept_pngs = 0
    deleted_files = 0
    
    # 获取所有文件（排除src目录）
    all_files = [f for f in app_dir.iterdir() if f.is_file()]
    
    # 分类文件
    png_files = [f for f in all_files if f.suffix.lower() == '.png']
    non_png_files = [f for f in all_files if f.suffix.lower() != '.png']
    
    # 保留所有PNG文件
    kept_pngs = len(png_files)
    
    # 删除所有非PNG文件
    for file in non_png_files:
        print(f"    删除: {file.name}")
        try:
            file.unlink()
            deleted_files += 1
        except Exception as e:
            print(f"      警告: 删除失败: {e}")
    
    return kept_pngs, deleted_files


def cleanup_all_apps():
    """整理所有应用目录，只保留PNG文件"""
    base_dir = get_path("outputs/new_artplus")
    
    if not base_dir.exists():
        print(f"错误: 目录不存在: {base_dir}")
        return
    
    print("="*60)
    print("整理outputs/new_artplus目录（只保留PNG文件）")
    print("="*60)
    print(f"目标目录: {base_dir}\n")
    print("说明: 将删除所有非PNG文件（包括XML、ARSC等）")
    print("      src目录中的文件保持不变\n")
    
    app_dirs = [d for d in base_dir.iterdir() if d.is_dir()]
    total_apps = len(app_dirs)
    
    if total_apps == 0:
        print("未找到应用目录")
        return
    
    total_kept_pngs = 0
    total_deleted = 0
    
    for i, app_dir in enumerate(app_dirs, 1):
        package_name = app_dir.name
        print(f"[{i}/{total_apps}] {package_name}")
        
        kept, deleted = cleanup_app_directory(app_dir)
        
        total_kept_pngs += kept
        total_deleted += deleted
        
        if kept > 0 or deleted > 0:
            print(f"  保留PNG: {kept}, 删除文件: {deleted}")
        else:
            print(f"  无需整理")
    
    print("\n" + "="*60)
    print("整理完成！")
    print(f"  总计应用: {total_apps}")
    print(f"  保留PNG: {total_kept_pngs}")
    print(f"  删除文件: {total_deleted}")
    print("="*60)
    print("\n说明:")
    print("- 所有PNG文件已保留")
    print("- 所有非PNG文件已删除（XML、ARSC等）")
    print("- src目录中的文件保持不变")


if __name__ == "__main__":
    cleanup_all_apps()
