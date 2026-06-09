#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
重新生成预览图（使用改进的布局和英文标签）
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

try:
    from PIL import Image, ImageDraw, ImageFont
    HAS_PIL = True
except ImportError:
    HAS_PIL = False
    print("错误: 需要安装Pillow库")
    sys.exit(1)

def create_preview_image(
    original_path: Path,
    foreground_path: Path,
    background_path: Path,
    method_name: str,
    output_path: Path,
    preview_size: int = 512
):
    """创建完整的预览图，包含原图、前景、背景和合并图"""
    try:
        # 读取所有图片
        original = Image.open(original_path).convert('RGBA')
        foreground = Image.open(foreground_path).convert('RGBA')
        background = Image.open(background_path).convert('RGBA')
        
        # 调整尺寸 - 铺满画布
        def resize_to_fill(img, size):
            """调整图片尺寸以铺满画布"""
            img.thumbnail((size, size), Image.Resampling.LANCZOS)
            # 创建正方形画布并铺满
            canvas = Image.new('RGBA', (size, size), (255, 255, 255, 0))
            # 如果图片小于画布，放大以铺满
            if img.width < size or img.height < size:
                scale = max(size / img.width, size / img.height)
                new_width = int(img.width * scale)
                new_height = int(img.height * scale)
                img = img.resize((new_width, new_height), Image.Resampling.LANCZOS)
            # 居中裁剪或放置
            x = (size - img.width) // 2
            y = (size - img.height) // 2
            canvas.paste(img, (x, y), img if img.mode == 'RGBA' else None)
            return canvas
        
        preview_size_small = preview_size // 2
        original_resized = resize_to_fill(original, preview_size_small)
        foreground_resized = resize_to_fill(foreground, preview_size_small)
        background_resized = resize_to_fill(background, preview_size_small)
        
        # 创建合并图
        merged = background_resized.copy()
        merged = Image.alpha_composite(merged, foreground_resized)
        merged_resized = merged
        
        # 创建预览画布 (2x2布局，铺满)
        margin = 10
        spacing = 10
        label_height = 25
        title_height = 35
        canvas_width = preview_size_small * 2 + spacing + margin * 2
        canvas_height = preview_size_small * 2 + spacing + label_height * 2 + title_height + margin * 2
        canvas = Image.new('RGB', (canvas_width, canvas_height), (240, 240, 240))
        draw = ImageDraw.Draw(canvas)
        
        # 尝试加载字体
        try:
            if sys.platform == 'darwin':
                font_path = '/System/Library/Fonts/Helvetica.ttc'
            elif sys.platform == 'win32':
                font_path = 'C:/Windows/Fonts/arial.ttf'
            else:
                font_path = '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'
            
            try:
                title_font = ImageFont.truetype(font_path, 20)
                label_font = ImageFont.truetype(font_path, 14)
            except:
                title_font = ImageFont.load_default()
                label_font = ImageFont.load_default()
        except:
            title_font = ImageFont.load_default()
            label_font = ImageFont.load_default()
        
        # 绘制标题 (English)
        title = f"{method_name.upper()}"
        bbox = draw.textbbox((0, 0), title, font=title_font)
        title_width = bbox[2] - bbox[0]
        title_x = (canvas_width - title_width) // 2
        draw.text((title_x, margin), title, fill=(0, 0, 0), font=title_font)
        
        # 计算位置 - 铺满画布
        start_y = title_height + margin
        start_x = margin
        
        # 第一行：原图和前景
        x1 = start_x
        y1 = start_y
        canvas.paste(original_resized, (x1, y1), original_resized if original_resized.mode == 'RGBA' else None)
        draw.text((x1, y1 + preview_size_small + 5), "Original", fill=(0, 0, 0), font=label_font)
        
        x2 = start_x + preview_size_small + spacing
        y2 = start_y
        canvas.paste(foreground_resized, (x2, y2), foreground_resized if foreground_resized.mode == 'RGBA' else None)
        draw.text((x2, y2 + preview_size_small + 5), "Foreground", fill=(0, 0, 0), font=label_font)
        
        # 第二行：背景和合并图
        x3 = start_x
        y3 = start_y + preview_size_small + label_height + spacing
        canvas.paste(background_resized, (x3, y3), background_resized if background_resized.mode == 'RGBA' else None)
        draw.text((x3, y3 + preview_size_small + 5), "Background", fill=(0, 0, 0), font=label_font)
        
        x4 = start_x + preview_size_small + spacing
        y4 = start_y + preview_size_small + label_height + spacing
        canvas.paste(merged_resized, (x4, y4), merged_resized if merged_resized.mode == 'RGBA' else None)
        draw.text((x4, y4 + preview_size_small + 5), "Merged", fill=(0, 0, 0), font=label_font)
        
        # 保存预览图
        canvas.save(output_path, 'PNG', quality=95)
        return output_path
        
    except Exception as e:
        print(f"  ✗ 创建预览图失败: {e}")
        import traceback
        traceback.print_exc()
        return None

def main():
    """主函数"""
    test_dir = get_path("test")
    output_dir = test_dir / "com.catchingnow.np"
    
    if not output_dir.exists():
        print(f"错误: 输出目录不存在: {output_dir}")
        return
    
    icon_file = output_dir / "ic_launcher_512.png"
    
    if not icon_file.exists():
        print(f"错误: 图标文件不存在: {icon_file}")
        return
    
    print("="*60)
    print("重新生成预览图（使用改进的布局和英文标签）")
    print("="*60)
    print(f"输出目录: {output_dir}\n")
    
    methods = ['grabcut', 'u2net', 'sam2']
    
    for method in methods:
        fg_path = output_dir / f"{method}_ic_launcher_512_foreground.png"
        bg_path = output_dir / f"{method}_ic_launcher_512_background.png"
        preview_path = output_dir / f"{method}_preview.png"
        
        if not fg_path.exists() or not bg_path.exists():
            print(f"[{method.upper()}] ⚠ 前景或背景文件不存在，跳过")
            continue
        
        print(f"[{method.upper()}] 生成预览图...")
        result = create_preview_image(icon_file, fg_path, bg_path, method, preview_path)
        
        if result and preview_path.exists():
            print(f"  ✓ 已生成: {preview_path.name}")
        else:
            print(f"  ✗ 生成失败")
        print()
    
    print("="*60)
    print("完成！")
    print("="*60)

if __name__ == "__main__":
    main()

