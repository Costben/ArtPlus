#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
根据手动绘制的fg.png和bg.png自动生成完整的ART+图标套件
支持单个包或批量处理outputs/new_artplus目录下的所有包
"""

import sys
import io
from pathlib import Path
from typing import Optional

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

# 导入生成函数
from generate_all_artplus_icons import (
    create_resized_background,
    create_resized_foreground,
    create_night_icon,
    create_monochrome_icon,
    create_style_icon,
    SIZE_1x1, SIZE_1x2, SIZE_2x1, SIZE_2x2
)


def generate_from_manual_icons(app_dir: Path, force: bool = False) -> tuple[bool, str]:
    """
    从手动绘制的fg.png和bg.png生成完整的ART+图标套件
    
    Args:
        app_dir: 应用目录路径
        force: 是否强制重新生成所有文件
        
    Returns:
        (是否成功, 消息)
    """
    fg_path = app_dir / "fg.png"
    bg_path = app_dir / "bg.png"
    
    # 检查基础文件
    if not fg_path.exists():
        return False, "缺少fg.png（前景文件）"
    
    if not bg_path.exists():
        return False, "缺少bg.png（背景文件）"
    
    package_name = app_dir.name
    print(f"处理 {package_name}...")
    
    # 检查是否已有完整文件（如果不需要强制重新生成）
    if not force:
        recfg_path = app_dir / "recfg.png"
        recbg_path = app_dir / "recbg.png"
        if recfg_path.exists() and recbg_path.exists():
            # 检查是否所有文件都已存在
            required_files = [
                "recbg_1x2.png", "recbg_2x1.png", "recbg_2x2.png",
                "recfg_1x2.png", "recfg_2x1.png", "recfg_2x2.png",
                "rec_night.png", "rec_night_1x2.png", "rec_night_2x1.png", "rec_night_2x2.png",
                "monochrome.png", "monochrome_1x2.png", "monochrome_2x1.png", "monochrome_2x2.png",
                "day.png", "nsd.png", "mat.png", "peb.png"
            ]
            all_exist = all((app_dir / f).exists() for f in required_files)
            if all_exist:
                return False, "所有文件已存在，跳过（使用--force强制重新生成）"
    
    try:
        print("  1. 生成基础recbg和recfg...")
        # 生成基础recbg和recfg（240x240）
        from PIL import Image
        # 拉伸bg.png到240x240
        bg_img = Image.open(bg_path).convert('RGB')
        if bg_img.size != (SIZE_1x1, SIZE_1x1):
            bg_img = bg_img.resize((SIZE_1x1, SIZE_1x1), Image.Resampling.LANCZOS)
        bg_base = bg_img
        
        # fg.png先等比例缩放到240x240作为基础图
        fg_original = Image.open(fg_path).convert('RGBA')
        # 创建240x240的透明画布
        fg_base = Image.new('RGBA', (SIZE_1x1, SIZE_1x1), (0, 0, 0, 0))
        # 等比例缩放fg.png（保持宽高比）
        fg_resized = fg_original.copy()
        fg_resized.thumbnail((SIZE_1x1, SIZE_1x1), Image.Resampling.LANCZOS)
        # 居中放置在240x240画布上
        x_offset = (SIZE_1x1 - fg_resized.width) // 2
        y_offset = (SIZE_1x1 - fg_resized.height) // 2
        fg_base.paste(fg_resized, (x_offset, y_offset), fg_resized)
        
        recbg_path = app_dir / "recbg.png"
        recfg_path = app_dir / "recfg.png"
        bg_base.save(recbg_path, 'PNG', optimize=True)
        fg_base.save(recfg_path, 'PNG', optimize=True)
        print("    ✓ recbg.png, recfg.png")
        
        print("  2. 生成变形背景...")
        # 生成变形背景（拉伸bg.png到目标尺寸）
        bg_original = Image.open(bg_path).convert('RGB')
        
        # 1x2变形背景（240x820）- 直接拉伸
        bg_1x2 = bg_original.resize(SIZE_1x2, Image.Resampling.LANCZOS)
        
        # 2x1变形背景（820x240）- 直接拉伸
        bg_2x1 = bg_original.resize(SIZE_2x1, Image.Resampling.LANCZOS)
        
        # 2x2变形背景（704x704）- 直接拉伸
        bg_2x2 = bg_original.resize((SIZE_2x2, SIZE_2x2), Image.Resampling.LANCZOS)
        
        bg_1x2.save(app_dir / "recbg_1x2.png", 'PNG', optimize=True)
        bg_2x1.save(app_dir / "recbg_2x1.png", 'PNG', optimize=True)
        bg_2x2.save(app_dir / "recbg_2x2.png", 'PNG', optimize=True)
        print("    ✓ recbg_1x2.png, recbg_2x1.png, recbg_2x2.png")
        
        print("  3. 生成变形前景...")
        # 基于240x240的基础fg图生成变形前景
        # 1x2变形前景（240x820）- 基于240x240的基础图，等比例缩放，居中放置
        fg_1x2_canvas = Image.new('RGBA', SIZE_1x2, (0, 0, 0, 0))
        fg_1x2_resized = fg_base.copy()
        fg_1x2_resized.thumbnail(SIZE_1x2, Image.Resampling.LANCZOS)
        x_offset = (SIZE_1x2[0] - fg_1x2_resized.width) // 2
        y_offset = (SIZE_1x2[1] - fg_1x2_resized.height) // 2
        fg_1x2_canvas.paste(fg_1x2_resized, (x_offset, y_offset), fg_1x2_resized)
        fg_1x2 = fg_1x2_canvas
        
        # 2x1变形前景（820x240）- 基于240x240的基础图，等比例缩放，居中放置
        fg_2x1_canvas = Image.new('RGBA', SIZE_2x1, (0, 0, 0, 0))
        fg_2x1_resized = fg_base.copy()
        fg_2x1_resized.thumbnail(SIZE_2x1, Image.Resampling.LANCZOS)
        x_offset = (SIZE_2x1[0] - fg_2x1_resized.width) // 2
        y_offset = (SIZE_2x1[1] - fg_2x1_resized.height) // 2
        fg_2x1_canvas.paste(fg_2x1_resized, (x_offset, y_offset), fg_2x1_resized)
        fg_2x1 = fg_2x1_canvas
        
        # 2x2变形前景（704x704）- 基于240x240的基础图，等比例缩放，居中放置
        fg_2x2_canvas = Image.new('RGBA', (SIZE_2x2, SIZE_2x2), (0, 0, 0, 0))
        fg_2x2_resized = fg_base.copy()
        fg_2x2_resized.thumbnail((SIZE_2x2, SIZE_2x2), Image.Resampling.LANCZOS)
        x_offset = (SIZE_2x2 - fg_2x2_resized.width) // 2
        y_offset = (SIZE_2x2 - fg_2x2_resized.height) // 2
        fg_2x2_canvas.paste(fg_2x2_resized, (x_offset, y_offset), fg_2x2_resized)
        fg_2x2 = fg_2x2_canvas
        
        fg_1x2.save(app_dir / "recfg_1x2.png", 'PNG', optimize=True)
        fg_2x1.save(app_dir / "recfg_2x1.png", 'PNG', optimize=True)
        fg_2x2.save(app_dir / "recfg_2x2.png", 'PNG', optimize=True)
        print("    ✓ recfg_1x2.png, recfg_2x1.png, recfg_2x2.png")
        
        print("  4. 生成暗色图标...")
        # 生成暗色图标（基于240x240的基础fg图）
        # 创建临时fg文件供样式生成函数使用（只创建一次）
        import tempfile
        import os
        temp_fg = tempfile.NamedTemporaryFile(suffix='.png', delete=False)
        fg_base.save(temp_fg.name, 'PNG')
        temp_fg.close()
        
        try:
            night_1x1 = create_night_icon(temp_fg.name, SIZE_1x1)
            night_1x2 = create_night_icon(temp_fg.name, SIZE_1x2)
            night_2x1 = create_night_icon(temp_fg.name, SIZE_2x1)
            night_2x2 = create_night_icon(temp_fg.name, SIZE_2x2)
            
            night_1x1.save(app_dir / "rec_night.png", 'PNG', optimize=True)
            night_1x2.save(app_dir / "rec_night_1x2.png", 'PNG', optimize=True)
            night_2x1.save(app_dir / "rec_night_2x1.png", 'PNG', optimize=True)
            night_2x2.save(app_dir / "rec_night_2x2.png", 'PNG', optimize=True)
            print("    ✓ rec_night.png, rec_night_1x2.png, rec_night_2x1.png, rec_night_2x2.png")
            
            print("  5. 生成单色图标...")
            # 生成单色图标（基于240x240的基础fg图）
            mono_1x1 = create_monochrome_icon(temp_fg.name, SIZE_1x1)
            mono_1x2 = create_monochrome_icon(temp_fg.name, SIZE_1x2)
            mono_2x1 = create_monochrome_icon(temp_fg.name, SIZE_2x1)
            mono_2x2 = create_monochrome_icon(temp_fg.name, SIZE_2x2)
            
            mono_1x1.save(app_dir / "monochrome.png", 'PNG', optimize=True)
            mono_1x2.save(app_dir / "monochrome_1x2.png", 'PNG', optimize=True)
            mono_2x1.save(app_dir / "monochrome_2x1.png", 'PNG', optimize=True)
            mono_2x2.save(app_dir / "monochrome_2x2.png", 'PNG', optimize=True)
            print("    ✓ monochrome.png, monochrome_1x2.png, monochrome_2x1.png, monochrome_2x2.png")
            
            print("  6. 生成其他样式图标...")
            # 生成其他样式图标（基于240x240的基础fg图）
            day_icon = create_style_icon(temp_fg.name, 'day', SIZE_1x1)
            nsd_icon = create_style_icon(temp_fg.name, 'nsd', SIZE_1x1)
            mat_icon = create_style_icon(temp_fg.name, 'mat', SIZE_1x1)
            peb_icon = create_style_icon(temp_fg.name, 'peb', SIZE_1x1)
        finally:
            # 清理临时文件
            os.unlink(temp_fg.name)
        
        day_icon.save(app_dir / "day.png", 'PNG', optimize=True)
        nsd_icon.save(app_dir / "nsd.png", 'PNG', optimize=True)
        mat_icon.save(app_dir / "mat.png", 'PNG', optimize=True)
        peb_icon.save(app_dir / "peb.png", 'PNG', optimize=True)
        print("    ✓ day.png, nsd.png, mat.png, peb.png")
        
        print(f"  ✓ 完成！已生成所有ART+图标文件")
        return True, "成功"
        
    except Exception as e:
        return False, f"生成失败: {e}"


def process_single_package(package_path: str, force: bool = False):
    """处理单个包"""
    app_dir = Path(package_path)
    
    if not app_dir.exists():
        print(f"错误: 目录不存在: {app_dir}")
        return
    
    if not app_dir.is_dir():
        print(f"错误: 不是目录: {app_dir}")
        return
    
    success, message = generate_from_manual_icons(app_dir, force)
    
    if success:
        print(f"✓ {message}")
    else:
        print(f"✗ {message}")


def process_all_packages(force: bool = False):
    """批量处理outputs/new_artplus目录下的所有包"""
    new_artplus_dir = get_path("outputs/new_artplus")
    
    if not new_artplus_dir.exists():
        print(f"错误: outputs/new_artplus目录不存在: {new_artplus_dir}")
        return
    
    print("="*60)
    print("批量处理outputs/new_artplus目录下的所有包")
    print("="*60)
    print()
    
    # 获取所有应用目录
    app_dirs = [d for d in new_artplus_dir.iterdir() 
                if d.is_dir() and not d.name.startswith('.')]
    
    total_apps = len(app_dirs)
    if total_apps == 0:
        print("未找到应用目录")
        return
    
    print(f"找到 {total_apps} 个应用目录\n")
    print("="*60)
    
    success_count = 0
    skip_count = 0
    fail_count = 0
    
    for i, app_dir in enumerate(app_dirs, 1):
        package_name = app_dir.name
        
        # 检查是否有fg.png和bg.png
        fg_path = app_dir / "fg.png"
        bg_path = app_dir / "bg.png"
        
        if not fg_path.exists() or not bg_path.exists():
            print(f"[{i}/{total_apps}] {package_name}")
            print(f"  ⚠ 缺少fg.png或bg.png，跳过")
            skip_count += 1
            print()
            continue
        
        print(f"[{i}/{total_apps}] {package_name}")
        success, message = generate_from_manual_icons(app_dir, force)
        
        if success:
            print(f"  ✓ {message}")
            success_count += 1
        elif "已存在" in message or "跳过" in message:
            print(f"  ⚠ {message}")
            skip_count += 1
        else:
            print(f"  ✗ {message}")
            fail_count += 1
        
        print()
    
    print("="*60)
    print("处理完成！")
    print(f"  总计: {total_apps} 个应用")
    print(f"  成功: {success_count} 个")
    print(f"  跳过: {skip_count} 个")
    print(f"  失败: {fail_count} 个")
    print("="*60)


def main():
    """主函数"""
    import argparse
    
    parser = argparse.ArgumentParser(
        description='根据手动绘制的fg.png和bg.png生成完整的ART+图标套件',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
使用示例:
  # 处理单个包
  python generate_from_manual_icons.py outputs/new_artplus/com.example.app
  
  # 批量处理所有包
  python generate_from_manual_icons.py --all
  
  # 强制重新生成（即使文件已存在）
  python generate_from_manual_icons.py --all --force
        """
    )
    
    parser.add_argument('package', nargs='?', help='单个包的路径（如：outputs/new_artplus/com.example.app）')
    parser.add_argument('--all', action='store_true', help='批量处理outputs/new_artplus目录下的所有包')
    parser.add_argument('--force', action='store_true', help='强制重新生成所有文件（即使已存在）')
    
    args = parser.parse_args()
    
    if args.all:
        process_all_packages(force=args.force)
    elif args.package:
        process_single_package(args.package, force=args.force)
    else:
        parser.print_help()


if __name__ == "__main__":
    main()

