#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
根据官方ART+图标格式，补全所有PNG文件
生成完整的ART+图标套件
"""

from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter, ImageEnhance
import sys


# ART+图标尺寸定义
SIZE_1x1 = 240  # 常规尺寸
SIZE_1x2 = (240, 820)  # 1x2变形
SIZE_2x1 = (820, 240)  # 2x1变形
SIZE_2x2 = 704  # 2x2变形
MONOCHROME_ALPHA_MIN = 40
MONOCHROME_ALPHA_MAX = 230
MONOCHROME_ALPHA_GAMMA = 0.85


def create_resized_background(size, color='white'):
    """
    创建指定尺寸的背景
    
    Args:
        size: 尺寸元组或整数（如果是整数，创建正方形）
        color: 背景颜色
    """
    if isinstance(size, int):
        size = (size, size)
    return Image.new('RGB', size, color=color)


def create_resized_foreground(foreground_path, target_size):
    """
    将前景图标放置在目标尺寸的画布上（居中，不拉伸）
    
    Args:
        foreground_path: 前景图标路径
        target_size: 目标尺寸元组
    """
    fg = Image.open(foreground_path).convert('RGBA')
    
    if isinstance(target_size, int):
        target_size = (target_size, target_size)
    
    # 如果尺寸相同，直接返回
    if fg.size == target_size:
        return fg
    
    # 创建透明画布
    canvas = Image.new('RGBA', target_size, (0, 0, 0, 0))
    
    # 计算居中位置
    x_offset = (target_size[0] - fg.width) // 2
    y_offset = (target_size[1] - fg.height) // 2
    
    # 将前景图标居中粘贴到画布上（不拉伸）
    canvas.paste(fg, (x_offset, y_offset), fg)
    
    return canvas


def create_night_icon(foreground_path, size):
    """
    创建暗色图标（rec_night）
    将前景图标转换为适合暗色背景的版本
    
    Args:
        foreground_path: 前景图标路径
        size: 尺寸（元组或整数）
    """
    fg_original = Image.open(foreground_path).convert('RGBA')
    
    if isinstance(size, int):
        size = (size, size)
    
    # 先处理颜色效果（在原始尺寸上）
    fg_rgb = Image.new('RGB', fg_original.size, (255, 255, 255))
    fg_rgb.paste(fg_original, mask=fg_original.split()[3])
    
    # 降低亮度
    enhancer = ImageEnhance.Brightness(fg_rgb)
    fg_dark = enhancer.enhance(0.7)
    
    # 增加对比度
    enhancer = ImageEnhance.Contrast(fg_dark)
    fg_dark = enhancer.enhance(1.2)
    
    # 转换回RGBA
    fg_dark_rgba = fg_dark.convert('RGBA')
    fg_dark_rgba.putalpha(fg_original.split()[3])
    
    # 如果目标尺寸与原始尺寸相同，直接返回
    if fg_dark_rgba.size == size:
        return fg_dark_rgba
    
    # 创建透明画布，居中放置
    canvas = Image.new('RGBA', size, (0, 0, 0, 0))
    x_offset = (size[0] - fg_dark_rgba.width) // 2
    y_offset = (size[1] - fg_dark_rgba.height) // 2
    canvas.paste(fg_dark_rgba, (x_offset, y_offset), fg_dark_rgba)
    
    return canvas


def create_monochrome_icon(foreground_path, size):
    """
    创建单色图标（monochrome）
    将主体亮度写入 alpha，RGB 固定为白色，便于系统色调模式按透明度套色。
    
    Args:
        foreground_path: 前景图标路径
        size: 尺寸（元组或整数）
    """
    fg_original = Image.open(foreground_path).convert('RGBA')
    
    if isinstance(size, int):
        size = (size, size)
    
    fg_mono = create_luminance_alpha_mask(fg_original)
    
    # 如果目标尺寸与原始尺寸相同，直接返回
    if fg_mono.size == size:
        return fg_mono
    
    # 创建透明画布，居中放置
    canvas = Image.new('RGBA', size, (0, 0, 0, 0))
    x_offset = (size[0] - fg_mono.width) // 2
    y_offset = (size[1] - fg_mono.height) // 2
    canvas.paste(fg_mono, (x_offset, y_offset), fg_mono)
    
    return canvas


def percentile(values, ratio):
    if not values:
        return 0
    values = sorted(values)
    index = min(len(values) - 1, max(0, int(round((len(values) - 1) * ratio))))
    return values[index]


def image_data(image):
    getter = getattr(image, "get_flattened_data", None)
    if getter:
        return getter()
    return image.getdata()


def create_luminance_alpha_mask(image):
    """
    将前景图转成系统可套色的 alpha mask。
    透明区域保持透明；主体内部的亮暗差异通过 alpha 保留。
    """
    rgba = image.convert('RGBA')
    gray = rgba.convert('L')
    alpha = rgba.getchannel('A')
    gray_values = list(image_data(gray))
    alpha_values = list(image_data(alpha))
    visible_luma = [luma for luma, a in zip(gray_values, alpha_values) if a > 8]

    if not visible_luma:
        return Image.new('RGBA', rgba.size, (255, 255, 255, 0))

    low = percentile(visible_luma, 0.02)
    high = percentile(visible_luma, 0.98)
    has_real_luma_range = high - low >= 12

    out_alpha = []
    for luma, a in zip(gray_values, alpha_values):
        if a <= 0:
            out_alpha.append(0)
            continue
        if has_real_luma_range:
            normalized = (luma - low) / (high - low)
            normalized = max(0.0, min(1.0, normalized))
            normalized = normalized ** MONOCHROME_ALPHA_GAMMA
            mask_alpha = MONOCHROME_ALPHA_MIN + normalized * (MONOCHROME_ALPHA_MAX - MONOCHROME_ALPHA_MIN)
        else:
            mask_alpha = MONOCHROME_ALPHA_MAX
        out_alpha.append(int(round((a / 255.0) * mask_alpha)))

    result = Image.new('RGBA', rgba.size, (255, 255, 255, 0))
    result.putalpha(Image.frombytes('L', rgba.size, bytes(out_alpha)))
    return result


def create_style_icon(foreground_path, style, size):
    """
    创建不同样式的图标
    
    Args:
        foreground_path: 前景图标路径
        style: 样式名称 ('day', 'nsd', 'mat', 'peb')
        size: 尺寸（元组或整数）
    """
    fg_original = Image.open(foreground_path).convert('RGBA')
    
    if isinstance(size, int):
        size = (size, size)
    
    # 根据样式应用不同的效果（在原始尺寸上）
    fg_rgb = Image.new('RGB', fg_original.size, (255, 255, 255))
    fg_rgb.paste(fg_original, mask=fg_original.split()[3])
    
    if style == 'day':  # 彩昼 - 增强饱和度
        enhancer = ImageEnhance.Color(fg_rgb)
        fg_enhanced = enhancer.enhance(1.3)
        result = fg_enhanced.convert('RGBA')
        result.putalpha(fg_original.split()[3])
    
    elif style == 'nsd':  # 夜影 - 降低亮度，增加对比度
        enhancer = ImageEnhance.Brightness(fg_rgb)
        fg_dark = enhancer.enhance(0.8)
        enhancer = ImageEnhance.Contrast(fg_dark)
        fg_enhanced = enhancer.enhance(1.3)
        result = fg_enhanced.convert('RGBA')
        result.putalpha(fg_original.split()[3])
    
    elif style == 'mat':  # 材料 - 轻微降低饱和度，增加对比度
        enhancer = ImageEnhance.Color(fg_rgb)
        fg_desat = enhancer.enhance(0.9)
        enhancer = ImageEnhance.Contrast(fg_desat)
        fg_enhanced = enhancer.enhance(1.1)
        result = fg_enhanced.convert('RGBA')
        result.putalpha(fg_original.split()[3])
    
    elif style == 'peb':  # 鹅卵石 - 降低饱和度，柔和效果
        enhancer = ImageEnhance.Color(fg_rgb)
        fg_desat = enhancer.enhance(0.7)
        enhancer = ImageEnhance.Contrast(fg_desat)
        fg_soft = enhancer.enhance(0.9)
        # 应用轻微模糊
        fg_soft = fg_soft.filter(ImageFilter.GaussianBlur(radius=0.5))
        result = fg_soft.convert('RGBA')
        result.putalpha(fg_original.split()[3])
    
    else:
        # 默认返回原图
        result = fg_original
    
    # 如果目标尺寸与原始尺寸相同，直接返回
    if result.size == size:
        return result
    
    # 创建透明画布，居中放置
    canvas = Image.new('RGBA', size, (0, 0, 0, 0))
    x_offset = (size[0] - result.width) // 2
    y_offset = (size[1] - result.height) // 2
    canvas.paste(result, (x_offset, y_offset), result)
    
    return canvas


def generate_all_icons(app_dir: str):
    """
    为指定应用生成所有ART+图标文件
    
    Args:
        app_dir: 应用目录路径
    """
    app_path = Path(app_dir)
    
    if not app_path.exists():
        print(f"错误: 目录不存在: {app_dir}")
        return False
    
    recbg_path = app_path / "recbg.png"
    recfg_path = app_path / "recfg.png"
    
    if not recbg_path.exists():
        print(f"错误: 未找到 recbg.png")
        return False
    
    if not recfg_path.exists():
        print(f"错误: 未找到 recfg.png")
        return False
    
    print(f"开始为 {app_path.name} 生成ART+图标套件...")
    
    # 1. 生成变形背景 (recbg_1x2, recbg_2x1, recbg_2x2)
    print("  生成变形背景...")
    bg_1x2 = create_resized_background(SIZE_1x2, 'white')
    bg_1x2.save(app_path / "recbg_1x2.png", 'PNG', optimize=True)
    
    bg_2x1 = create_resized_background(SIZE_2x1, 'white')
    bg_2x1.save(app_path / "recbg_2x1.png", 'PNG', optimize=True)
    
    bg_2x2 = create_resized_background(SIZE_2x2, 'white')
    bg_2x2.save(app_path / "recbg_2x2.png", 'PNG', optimize=True)
    
    # 2. 生成变形前景 (recfg_1x2, recfg_2x1, recfg_2x2)
    print("  生成变形前景...")
    fg_1x2 = create_resized_foreground(recfg_path, SIZE_1x2)
    fg_1x2.save(app_path / "recfg_1x2.png", 'PNG', optimize=True)
    
    fg_2x1 = create_resized_foreground(recfg_path, SIZE_2x1)
    fg_2x1.save(app_path / "recfg_2x1.png", 'PNG', optimize=True)
    
    fg_2x2 = create_resized_foreground(recfg_path, SIZE_2x2)
    fg_2x2.save(app_path / "recfg_2x2.png", 'PNG', optimize=True)
    
    # 3. 生成暗色图标 (rec_night系列)
    print("  生成暗色图标...")
    night_1x1 = create_night_icon(recfg_path, SIZE_1x1)
    night_1x1.save(app_path / "rec_night.png", 'PNG', optimize=True)
    
    night_1x2 = create_night_icon(recfg_path, SIZE_1x2)
    night_1x2.save(app_path / "rec_night_1x2.png", 'PNG', optimize=True)
    
    night_2x1 = create_night_icon(recfg_path, SIZE_2x1)
    night_2x1.save(app_path / "rec_night_2x1.png", 'PNG', optimize=True)
    
    night_2x2 = create_night_icon(recfg_path, SIZE_2x2)
    night_2x2.save(app_path / "rec_night_2x2.png", 'PNG', optimize=True)
    
    # 4. 生成单色图标 (monochrome系列，灵感样式)
    print("  生成单色图标（灵感样式）...")
    mono_1x1 = create_monochrome_icon(recfg_path, SIZE_1x1)
    mono_1x1.save(app_path / "monochrome.png", 'PNG', optimize=True)
    
    mono_1x2 = create_monochrome_icon(recfg_path, SIZE_1x2)
    mono_1x2.save(app_path / "monochrome_1x2.png", 'PNG', optimize=True)
    
    mono_2x1 = create_monochrome_icon(recfg_path, SIZE_2x1)
    mono_2x1.save(app_path / "monochrome_2x1.png", 'PNG', optimize=True)
    
    mono_2x2 = create_monochrome_icon(recfg_path, SIZE_2x2)
    mono_2x2.save(app_path / "monochrome_2x2.png", 'PNG', optimize=True)
    
    # 5. 生成其他样式图标
    print("  生成其他样式图标...")
    day_icon = create_style_icon(recfg_path, 'day', SIZE_1x1)
    day_icon.save(app_path / "day.png", 'PNG', optimize=True)
    
    nsd_icon = create_style_icon(recfg_path, 'nsd', SIZE_1x1)
    nsd_icon.save(app_path / "nsd.png", 'PNG', optimize=True)
    
    mat_icon = create_style_icon(recfg_path, 'mat', SIZE_1x1)
    mat_icon.save(app_path / "mat.png", 'PNG', optimize=True)
    
    peb_icon = create_style_icon(recfg_path, 'peb', SIZE_1x1)
    peb_icon.save(app_path / "peb.png", 'PNG', optimize=True)
    
    print(f"\n完成！已生成所有ART+图标文件")
    
    # 统计生成的文件
    generated_files = [
        "recbg_1x2.png", "recbg_2x1.png", "recbg_2x2.png",
        "recfg_1x2.png", "recfg_2x1.png", "recfg_2x2.png",
        "rec_night.png", "rec_night_1x2.png", "rec_night_2x1.png", "rec_night_2x2.png",
        "monochrome.png", "monochrome_1x2.png", "monochrome_2x1.png", "monochrome_2x2.png",
        "day.png", "nsd.png", "mat.png", "peb.png"
    ]
    
    existing = sum(1 for f in generated_files if (app_path / f).exists())
    print(f"生成文件数: {existing}/{len(generated_files)}")
    
    return True


def main():
    """主函数"""
    if len(sys.argv) < 2:
        print("用法: python generate_all_artplus_icons.py <应用目录路径>")
        print("示例: python generate_all_artplus_icons.py outputs/new_artplus/app.alextran.immich")
        return
    
    app_dir = sys.argv[1]
    generate_all_icons(app_dir)


if __name__ == "__main__":
    main()
