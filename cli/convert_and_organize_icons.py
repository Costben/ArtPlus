#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
整理和转换提取的应用图标
- 将原始文件移动到src文件夹
- 转换为240x240的PNG图标（ART+图标标准尺寸）
"""

import shutil
import re
from pathlib import Path
from typing import List, Optional, Tuple
try:
    from PIL import Image, ImageDraw
    HAS_PIL = True
except ImportError:
    HAS_PIL = False
    print("警告: 未安装Pillow库，将无法处理图片。请运行: pip install Pillow")

# 添加src目录到路径
import sys
script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))

from project_helper import get_path


def get_best_png_icon(icon_files: List[Path]) -> Optional[Path]:
    """
    从列表中选择最佳的PNG图标
    优先顺序：ic_launcher_foreground > ic_launcher_round > ic_launcher > 其他PNG
    在同等优先级下，选择分辨率最高的
    """
    if not icon_files:
        return None
    
    # 定义图标名称的优先级模式
    priority_patterns = [
        (r'ic_launcher_foreground\.png', 3),  # 自适应图标前景（最高优先级）
        (r'ic_launcher_round\.png', 2),       # 圆形启动器图标
        (r'ic_launcher\.png', 1),             # 标准启动器图标
    ]
    
    best_icon: Optional[Path] = None
    best_priority = -1
    best_resolution = 0
    
    for icon_file in icon_files:
        if not icon_file.suffix.lower() == '.png':
            continue
        
        # 确定优先级
        priority = 0
        for pattern, prio in priority_patterns:
            if re.search(pattern, icon_file.name, re.IGNORECASE):
                priority = prio
                break
        
        # 获取分辨率
        try:
            if HAS_PIL:
                with Image.open(icon_file) as img:
                    resolution = img.width * img.height
            else:
                # 如果没有PIL，使用文件名推断（不准确，但总比没有好）
                resolution = 0
                if 'xxxhdpi' in icon_file.name:
                    resolution = 192 * 192
                elif 'xxhdpi' in icon_file.name:
                    resolution = 144 * 144
                elif 'xhdpi' in icon_file.name:
                    resolution = 96 * 96
                elif 'hdpi' in icon_file.name:
                    resolution = 72 * 72
                elif 'mdpi' in icon_file.name:
                    resolution = 48 * 48
        except Exception:
            resolution = 0
        
        # 选择最佳图标
        if priority > best_priority or (priority == best_priority and resolution > best_resolution):
            best_priority = priority
            best_resolution = resolution
            best_icon = icon_file
    
    return best_icon


def resize_icon_to_artplus(icon_path: Path, output_path: Path, target_size: int = 240) -> bool:
    """
    将图标调整为ART+图标标准尺寸（240x240）
    
    Args:
        icon_path: 输入图标路径
        output_path: 输出路径
        target_size: 目标尺寸（默认240）
        
    Returns:
        是否成功
    """
    if not HAS_PIL:
        # 如果没有PIL，直接复制文件
        try:
            shutil.copy(icon_path, output_path)
            return True
        except Exception as e:
            print(f"    错误: 复制图标失败: {e}")
            return False
    
    try:
        with Image.open(icon_path) as img:
            # 转换为RGBA模式（支持透明）
            if img.mode != 'RGBA':
                img = img.convert('RGBA')
            
            # 如果图片是正方形，直接缩放
            if img.width == img.height:
                resized = img.resize((target_size, target_size), Image.Resampling.LANCZOS)
            else:
                # 如果不是正方形，先裁剪为正方形（居中裁剪）
                size = min(img.width, img.height)
                left = (img.width - size) // 2
                top = (img.height - size) // 2
                cropped = img.crop((left, top, left + size, top + size))
                resized = cropped.resize((target_size, target_size), Image.Resampling.LANCZOS)
            
            # 保存
            resized.save(output_path, 'PNG', optimize=True)
            return True
    except Exception as e:
        print(f"    错误: 调整图标尺寸失败: {e}")
        return False


def process_app_icons(app_dir: Path) -> Tuple[bool, int]:
    """
    处理单个应用的图标
    
    Returns:
        (是否成功, 处理的文件数)
    """
    package_name = app_dir.name
    
    # 创建src目录
    src_dir = app_dir / "src"
    src_dir.mkdir(exist_ok=True)
    
    # 获取所有原始文件（排除README.txt和可能已存在的输出文件）
    exclude_files = {"README.txt", "recfg.png", "display_icon.png"}
    all_files = [f for f in app_dir.iterdir() 
                 if f.is_file() and f.name not in exclude_files]
    
    if not all_files:
        return False, 0
    
    # 分类文件
    png_files = [f for f in all_files if f.suffix.lower() == '.png']
    xml_files = [f for f in all_files if f.suffix.lower() == '.xml']
    other_files = [f for f in all_files if f.suffix.lower() not in ['.png', '.xml']]
    
    # 选择最佳PNG图标
    best_png = get_best_png_icon(png_files)
    
    success = False
    if best_png:
        # 转换为240x240的PNG
        output_icon = app_dir / "recfg.png"
        if resize_icon_to_artplus(best_png, output_icon):
            print(f"  [OK] 已生成 recfg.png (来源: {best_png.name})")
            success = True
        else:
            print(f"  [FAIL] 生成 recfg.png 失败")
    else:
        if xml_files:
            print(f"  [WARN] 只有XML图标，无法自动转换（需要Android SDK工具）")
        else:
            print(f"  [FAIL] 未找到可用的PNG图标")
    
    # 移动所有原始文件到src目录
    moved_count = 0
    for f in all_files:
        try:
            dest = src_dir / f.name
            if dest.exists():
                # 如果目标文件已存在，添加序号
                base_name = f.stem
                ext = f.suffix
                counter = 1
                while dest.exists():
                    dest = src_dir / f"{base_name}_{counter}{ext}"
                    counter += 1
            shutil.move(f, dest)
            moved_count += 1
        except Exception as e:
            print(f"  警告: 移动文件 '{f.name}' 失败: {e}")
    
    return success, moved_count


def main():
    """主函数"""
    if not HAS_PIL:
        print("="*60)
        print("警告: 未安装Pillow库")
        print("请运行以下命令安装: pip install Pillow")
        print("="*60)
        print("\n是否继续（将无法调整图片尺寸）? (y/n): ", end='')
        choice = input().strip().lower()
        if choice != 'y':
            return
    
    base_dir = get_path("outputs/new_artplus")
    if not base_dir.is_dir():
        print(f"错误: 目录 '{base_dir}' 不存在")
        return
    
    app_dirs = [d for d in base_dir.iterdir() if d.is_dir() and d.name != "src"]
    total_apps = len(app_dirs)
    
    if total_apps == 0:
        print("未找到应用目录")
        return
    
    print("="*60)
    print("开始整理和转换应用图标")
    print("="*60)
    print(f"找到 {total_apps} 个应用目录\n")
    
    success_count = 0
    fail_count = 0
    total_moved = 0
    
    for i, app_dir in enumerate(app_dirs, 1):
        print(f"[{i}/{total_apps}] {app_dir.name}")
        success, moved = process_app_icons(app_dir)
        
        if success:
            success_count += 1
        else:
            fail_count += 1
        
        total_moved += moved
    
    print("\n" + "="*60)
    print("处理完成！")
    print(f"  总计: {total_apps} 个应用")
    print(f"  成功生成图标: {success_count} 个")
    print(f"  失败/跳过: {fail_count} 个")
    print(f"  移动原始文件: {total_moved} 个")
    print("="*60)
    print("\n说明:")
    print("- 原始图标文件已移动到各应用的 src/ 目录")
    print("- 成功转换的图标已保存为 recfg.png (240x240)")
    print("- 对于只有XML图标的应用，需要手动处理或使用Android SDK工具")


if __name__ == "__main__":
    main()
