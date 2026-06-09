#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
使用U2Net对outputs/new_artplus目录内的非官方命名PNG进行前后景分离
并按照ColorOS官方结构命名（recbg.png, recfg.png）
"""

import sys
import io
import shutil
from pathlib import Path
from typing import Optional, Tuple

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

try:
    from rembg import new_session
    HAS_REMBG = True
except ImportError:
    HAS_REMBG = False
    print("错误: 需要安装rembg库")
    print("请运行: pip install 'rembg[cpu]'")
    sys.exit(1)

# 导入分离函数
from separate_icons_with_rembg import separate_icon_with_rembg, OFFICIAL_ICON_NAMES, has_official_icons, is_official_icon_name


def get_image_resolution(image_path: Path) -> Tuple[int, int]:
    """获取图片分辨率（宽, 高）"""
    try:
        with Image.open(image_path) as img:
            return img.size  # (width, height)
    except Exception:
        return (0, 0)


def get_highest_resolution_png(app_dir: Path) -> Optional[Path]:
    """
    获取应用目录中分辨率最高的非官方命名PNG文件
    
    Args:
        app_dir: 应用目录路径
        
    Returns:
        分辨率最高的PNG文件路径，如果没有则返回None
    """
    # 获取所有非官方命名的PNG文件（排除src目录）
    png_files = []
    for png_file in app_dir.iterdir():
        if (png_file.is_file() and 
            png_file.suffix.lower() == '.png' and
            not is_official_icon_name(png_file.name)):
            png_files.append(png_file)
    
    if not png_files:
        return None
    
    # 找到分辨率最高的文件（按像素总数）
    highest_res = None
    highest_pixels = 0
    
    for png_file in png_files:
        width, height = get_image_resolution(png_file)
        pixels = width * height
        
        if pixels > highest_pixels:
            highest_pixels = pixels
            highest_res = png_file
    
    return highest_res


def resize_to_artplus_size(image_path: Path, target_size: int = 240) -> Image.Image:
    """
    将图片调整到ART+标准尺寸（240x240）
    
    Args:
        image_path: 图片路径
        target_size: 目标尺寸（默认240）
        
    Returns:
        调整后的PIL Image对象
    """
    img = Image.open(image_path).convert('RGBA')
    
    # 如果图片已经是目标尺寸，直接返回
    if img.size == (target_size, target_size):
        return img
    
    # 调整尺寸，保持宽高比，然后居中裁剪
    img.thumbnail((target_size, target_size), Image.Resampling.LANCZOS)
    
    # 创建正方形画布
    canvas = Image.new('RGBA', (target_size, target_size), (255, 255, 255, 0))
    
    # 居中放置
    x = (target_size - img.width) // 2
    y = (target_size - img.height) // 2
    canvas.paste(img, (x, y), img if img.mode == 'RGBA' else None)
    
    return canvas


def process_app_directory(app_dir: Path, session) -> Tuple[bool, str]:
    """
    处理单个应用目录
    
    Args:
        app_dir: 应用目录路径
        session: rembg session对象
        
    Returns:
        (是否成功, 消息)
    """
    package_name = app_dir.name
    
    # 获取分辨率最高的非官方命名PNG
    source_png = get_highest_resolution_png(app_dir)
    
    if not source_png:
        return False, "未找到非官方命名的PNG文件"
    
    print(f"  源文件: {source_png.name} ({get_image_resolution(source_png)[0]}x{get_image_resolution(source_png)[1]})")
    
    # 创建src目录（如果不存在）
    src_dir = app_dir / "src"
    src_dir.mkdir(exist_ok=True)
    
    # 使用U2Net分离前后景
    try:
        result = separate_icon_with_rembg(source_png, output_dir=app_dir, session=session)
        
        if not result:
            return False, "U2Net分离失败"
        
        foreground_path, background_path = result
        
        # 调整到ART+标准尺寸（240x240）
        print(f"  调整到ART+标准尺寸（240x240）...")
        foreground_resized = resize_to_artplus_size(foreground_path, 240)
        background_resized = resize_to_artplus_size(background_path, 240)
        
        # 保存为官方命名
        recfg_path = app_dir / "recfg.png"
        recbg_path = app_dir / "recbg.png"
        
        foreground_resized.save(recfg_path, 'PNG')
        background_resized.save(recbg_path, 'PNG')
        
        print(f"  ✓ 已生成: recfg.png, recbg.png")
        
        # 删除临时文件（u2net_xxx_foreground.png, u2net_xxx_background.png）
        try:
            foreground_path.unlink()
            background_path.unlink()
        except:
            pass
        
        # 将源文件移到src目录
        src_target = src_dir / source_png.name
        if not src_target.exists():
            shutil.move(str(source_png), str(src_target))
            print(f"  ✓ 已移动源文件到src目录")
        
        return True, "成功"
        
    except Exception as e:
        return False, f"处理失败: {e}"


def main():
    """主函数"""
    new_artplus_dir = get_path("outputs/new_artplus")
    
    if not new_artplus_dir.exists():
        print(f"错误: outputs/new_artplus目录不存在: {new_artplus_dir}")
        return
    
    print("="*60)
    print("使用U2Net处理outputs/new_artplus目录")
    print("="*60)
    print(f"目标目录: {new_artplus_dir}\n")
    print("处理规则:")
    print("- 处理所有有非官方命名PNG文件的包")
    print("- 选择分辨率最高的非官方命名PNG文件")
    print("- 使用U2Net分离前后景")
    print("- 调整到ART+标准尺寸（240x240）")
    print("- 保存为recbg.png和recfg.png（会覆盖已存在的文件）")
    print("- 源文件移到src目录\n")
    
    # 创建rembg session
    print("创建rembg session...")
    try:
        session = new_session('u2net')
        print("✓ Session创建成功\n")
    except Exception as e:
        print(f"✗ Session创建失败: {e}")
        return
    
    # 获取所有应用目录
    app_dirs = [d for d in new_artplus_dir.iterdir() if d.is_dir() and not d.name.startswith('.')]
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
        print(f"[{i}/{total_apps}] {package_name}")
        
        success, message = process_app_directory(app_dir, session)
        
        if success:
            print(f"  ✓ {message}")
            success_count += 1
        elif "已有官方图标" in message or "未找到" in message:
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
    print("\n说明:")
    print("- 前景文件: recfg.png (240x240)")
    print("- 背景文件: recbg.png (240x240)")
    print("- 源文件已移动到src目录")


if __name__ == "__main__":
    main()

