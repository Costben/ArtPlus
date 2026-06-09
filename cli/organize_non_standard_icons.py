#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
整理outputs/new_artplus目录
对于不符合官方ART图标命名规则的PNG图标，只保留分辨率最高的，其余移到src目录
"""

import sys
import io
from pathlib import Path
from typing import List, Tuple

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

try:
    from PIL import Image
    HAS_PIL = True
except ImportError:
    HAS_PIL = False
    print("错误: 需要安装Pillow库")
    sys.exit(1)


# 官方ART图标命名规则
OFFICIAL_ICON_NAMES = {
    # 经典样式（亮色）
    'recbg.png', 'recbg_1x2.png', 'recbg_2x1.png', 'recbg_2x2.png',
    'recfg.png', 'recfg_1x2.png', 'recfg_2x1.png', 'recfg_2x2.png',
    # 经典样式（暗色）
    'rec_night.png', 'rec_night_1x2.png', 'rec_night_2x1.png', 'rec_night_2x2.png',
    # 灵感样式
    'monochrome.png', 'monochrome_1x2.png', 'monochrome_2x1.png', 'monochrome_2x2.png',
    # 其他样式
    'day.png',  # 彩昼
    'nsd.png',  # 夜影
    'mat.png',  # 材料
    'peb.png',  # 鹅卵石
    'outline.png',  # 图标描边
    'art_off.png',  # 用途暂不明确
    'game_app.png',  # 游戏图标描边
}


def is_official_icon_name(filename: str) -> bool:
    """检查文件名是否符合官方ART图标命名规则"""
    return filename.lower() in OFFICIAL_ICON_NAMES


def get_image_resolution(image_path: Path) -> Tuple[int, int] | None:
    """获取图片分辨率（宽, 高）"""
    try:
        with Image.open(image_path) as img:
            return img.size  # (width, height)
    except Exception as e:
        print(f"      警告: 无法读取图片分辨率 {image_path.name}: {e}")
        return None


def organize_app_directory(app_dir: Path) -> Tuple[int, int]:
    """
    整理单个应用目录
    
    Returns:
        (保留的文件数, 移动的文件数)
    """
    kept_count = 0
    moved_count = 0
    
    # 确保src目录存在
    src_dir = app_dir / 'src'
    src_dir.mkdir(exist_ok=True)
    
    # 获取所有PNG文件（排除src目录中的文件）
    all_png_files = [f for f in app_dir.iterdir() 
                    if f.is_file() and f.suffix.lower() == '.png']
    
    # 分类：官方命名 vs 非官方命名
    official_files = []
    non_official_files = []
    
    for png_file in all_png_files:
        if is_official_icon_name(png_file.name):
            official_files.append(png_file)
        else:
            non_official_files.append(png_file)
    
    # 保留所有官方命名的文件
    kept_count = len(official_files)
    
    # 处理非官方命名的文件
    if non_official_files:
        # 按分辨率排序（宽 * 高）
        files_with_resolution = []
        for png_file in non_official_files:
            resolution = get_image_resolution(png_file)
            if resolution:
                width, height = resolution
                files_with_resolution.append((width * height, width, height, png_file))
            else:
                # 无法读取分辨率，放到src中
                print(f"    移动: {png_file.name} (无法读取分辨率)")
                try:
                    png_file.rename(src_dir / png_file.name)
                    moved_count += 1
                except Exception as e:
                    print(f"      错误: 移动失败: {e}")
        
        if files_with_resolution:
            # 按分辨率降序排序
            files_with_resolution.sort(key=lambda x: x[0], reverse=True)
            
            # 保留分辨率最高的
            highest_res = files_with_resolution[0]
            print(f"    保留: {highest_res[3].name} ({highest_res[1]}x{highest_res[2]})")
            kept_count += 1
            
            # 移动其余文件到src
            for _, width, height, png_file in files_with_resolution[1:]:
                print(f"    移动: {png_file.name} ({width}x{height}) -> src/")
                try:
                    # 如果src中已存在同名文件，添加序号
                    dest_path = src_dir / png_file.name
                    if dest_path.exists():
                        stem = png_file.stem
                        suffix = png_file.suffix
                        counter = 1
                        while dest_path.exists():
                            dest_path = src_dir / f"{stem}_{counter}{suffix}"
                            counter += 1
                    
                    png_file.rename(dest_path)
                    moved_count += 1
                except Exception as e:
                    print(f"      错误: 移动失败: {e}")
    
    return kept_count, moved_count


def organize_all_apps():
    """整理所有应用目录"""
    base_dir = get_path("outputs/new_artplus")
    
    if not base_dir.exists():
        print(f"错误: 目录不存在: {base_dir}")
        return
    
    print("="*60)
    print("整理outputs/new_artplus目录（非标准图标）")
    print("="*60)
    print(f"目标目录: {base_dir}\n")
    print("规则:")
    print("- 保留所有符合官方ART图标命名规则的PNG文件")
    print("- 对于不符合规则的PNG文件，只保留分辨率最高的")
    print("- 其余非标准图标移动到src目录\n")
    
    app_dirs = [d for d in base_dir.iterdir() if d.is_dir()]
    total_apps = len(app_dirs)
    
    if total_apps == 0:
        print("未找到应用目录")
        return
    
    total_kept = 0
    total_moved = 0
    
    for i, app_dir in enumerate(app_dirs, 1):
        package_name = app_dir.name
        print(f"[{i}/{total_apps}] {package_name}")
        
        kept, moved = organize_app_directory(app_dir)
        
        total_kept += kept
        total_moved += moved
        
        if moved > 0:
            print(f"  保留: {kept}, 移动: {moved}")
        else:
            print(f"  无需整理")
    
    print("\n" + "="*60)
    print("整理完成！")
    print(f"  总计应用: {total_apps}")
    print(f"  保留文件: {total_kept}")
    print(f"  移动文件: {total_moved}")
    print("="*60)
    print("\n说明:")
    print("- 所有符合官方命名规则的PNG文件已保留")
    print("- 非标准图标中分辨率最高的已保留")
    print("- 其余非标准图标已移动到src目录")


if __name__ == "__main__":
    organize_all_apps()
