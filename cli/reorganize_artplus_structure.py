#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
整理outputs/new_artplus目录结构，使其与官方ART+图标目录结构一致
- 将src目录中的ART+图标文件移动到包名文件夹根目录
- 删除README.txt和preview.png等非官方文件
- 确保文件列表与官方格式一致
"""

import shutil
from pathlib import Path
from typing import List, Set

# 官方ART+图标文件列表（标准文件）
OFFICIAL_ART_PLUS_FILES = {
    # 经典样式（亮色）
    "recbg.png", "recbg_1x2.png", "recbg_2x1.png", "recbg_2x2.png",
    "recfg.png", "recfg_1x2.png", "recfg_2x1.png", "recfg_2x2.png",
    # 经典样式（暗色）
    "rec_night.png", "rec_night_1x2.png", "rec_night_2x1.png", "rec_night_2x2.png",
    # 灵感样式（单色）
    "monochrome.png", "monochrome_1x2.png", "monochrome_2x1.png", "monochrome_2x2.png",
    # 其他样式
    "day.png", "nsd.png", "mat.png", "peb.png",
}

# 需要删除的非官方文件
NON_OFFICIAL_FILES = {
    "README.txt",
    "preview.png",
}


def reorganize_app_directory(app_dir: Path) -> tuple[int, int, int]:
    """
    整理单个应用目录，使其结构与官方一致
    
    Returns:
        (移动的文件数, 删除的文件数, 保留的文件数)
    """
    moved_count = 0
    deleted_count = 0
    kept_count = 0
    
    src_dir = app_dir / "src"
    
    # 如果src目录存在，移动ART+图标文件到根目录
    if src_dir.exists() and src_dir.is_dir():
        for file in src_dir.iterdir():
            if file.is_file() and file.name in OFFICIAL_ART_PLUS_FILES:
                # 检查目标文件是否已存在
                dest_file = app_dir / file.name
                if not dest_file.exists():
                    # 移动文件到根目录
                    shutil.move(str(file), str(dest_file))
                    moved_count += 1
                else:
                    # 如果已存在，删除src中的副本
                    file.unlink()
                    moved_count += 1
    
    # 删除非官方文件
    for non_official in NON_OFFICIAL_FILES:
        file_path = app_dir / non_official
        if file_path.exists():
            file_path.unlink()
            deleted_count += 1
    
    # 统计保留的官方文件
    for official_file in OFFICIAL_ART_PLUS_FILES:
        if (app_dir / official_file).exists():
            kept_count += 1
    
    # 清理src目录中的非必要文件
    if src_dir.exists():
        # 删除src目录中的preview.png等非原始文件
        for file in src_dir.iterdir():
            if file.is_file() and file.name in NON_OFFICIAL_FILES:
                file.unlink()
                deleted_count += 1
        
        remaining_files = list(src_dir.iterdir())
        if not remaining_files:
            # src目录为空，删除
            try:
                src_dir.rmdir()
            except:
                pass
        # 如果src目录中还有文件（原始提取的mipmap文件等），保留src目录
    
    return moved_count, deleted_count, kept_count


def cleanup_and_convert_xml():
    """整理并转换XML中的SVG为PNG"""
    import sys
    script_dir = Path(__file__).parent.absolute()
    src_dir = script_dir.parent / "src"
    if str(src_dir) not in sys.path:
        sys.path.insert(0, str(src_dir))
    
    from cleanup_and_convert_xml import cleanup_all_apps
    cleanup_all_apps()


def reorganize_all_apps(base_dir: Path = None):
    """
    整理所有应用目录
    
    Args:
        base_dir: outputs/new_artplus目录路径，如果为None则使用默认路径
    """
    # 添加src目录到路径
    import sys
    script_dir = Path(__file__).parent.absolute()
    src_dir = script_dir.parent / "src"
    if str(src_dir) not in sys.path:
        sys.path.insert(0, str(src_dir))
    
    from project_helper import get_path
    
    if base_dir is None:
        base_dir = get_path("outputs/new_artplus")
    
    if not base_dir.exists():
        print(f"错误: 目录不存在: {base_dir}")
        return
    
    print("="*60)
    print("整理outputs/new_artplus目录结构")
    print("="*60)
    print(f"目标目录: {base_dir}\n")
    
    app_dirs = [d for d in base_dir.iterdir() if d.is_dir()]
    total_apps = len(app_dirs)
    
    if total_apps == 0:
        print("未找到应用目录")
        return
    
    total_moved = 0
    total_deleted = 0
    total_kept = 0
    
    for i, app_dir in enumerate(app_dirs, 1):
        package_name = app_dir.name
        print(f"[{i}/{total_apps}] {package_name}")
        
        moved, deleted, kept = reorganize_app_directory(app_dir)
        
        total_moved += moved
        total_deleted += deleted
        total_kept += kept
        
        if moved > 0 or deleted > 0:
            print(f"  移动文件: {moved}, 删除文件: {deleted}, 保留文件: {kept}")
        else:
            print(f"  无需整理（已符合官方结构）")
    
    print("\n" + "="*60)
    print("整理完成！")
    print(f"  总计应用: {total_apps}")
    print(f"  移动文件: {total_moved}")
    print(f"  删除文件: {total_deleted}")
    print(f"  保留文件: {total_kept}")
    print("="*60)
    print("\n说明:")
    print("- 所有ART+图标文件已移动到包名文件夹根目录")
    print("- 非官方文件（README.txt、preview.png）已删除")
    print("- src目录保留，用于存放原始提取的文件")
    print("- 目录结构现在与官方ART+图标格式一致")


def main():
    """主函数"""
    import sys
    
    if len(sys.argv) > 1:
        base_dir = Path(sys.argv[1])
    else:
        base_dir = None
    
    reorganize_all_apps(base_dir)


if __name__ == "__main__":
    main()
