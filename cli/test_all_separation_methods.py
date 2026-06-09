#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试所有图标分离方法并生成完整预览图
每种方法都会生成一个预览图，文件名格式：{方法名}_preview.png
"""

import sys
import io
import zipfile
import shutil
from pathlib import Path
from typing import Tuple, Optional

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
    print("请运行: pip install Pillow")
    sys.exit(1)

try:
    import numpy as np
    HAS_NUMPY = True
except ImportError:
    HAS_NUMPY = False

try:
    import cv2
    HAS_OPENCV = True
except ImportError:
    HAS_OPENCV = False

try:
    from rembg import remove, new_session
    HAS_REMBG = True
except ImportError:
    HAS_REMBG = False

# 导入分离方法（延迟导入，避免依赖问题）
def get_separation_functions():
    """获取分离函数"""
    functions = {}
    
    # GrabCut
    if HAS_OPENCV:
        try:
            from test_separate_icons import separate_icon_with_grabcut
            functions['grabcut'] = separate_icon_with_grabcut
        except:
            pass
    
    # SAM2
    if HAS_OPENCV:
        try:
            from test_separate_icons import separate_icon_with_sam2
            functions['sam2'] = separate_icon_with_sam2
        except:
            pass
    
    # U2Net (rembg)
    if HAS_REMBG:
        try:
            from separate_icons_with_rembg import separate_icon_with_rembg
            functions['u2net'] = separate_icon_with_rembg
        except:
            pass
    
    return functions


def extract_png_icon_from_apk(apk_path: Path, output_dir: Path) -> Optional[Path]:
    """从APK中提取PNG图标"""
    try:
        with zipfile.ZipFile(apk_path, 'r') as zip_ref:
            # 查找PNG图标文件
            icon_files = [f for f in zip_ref.namelist() 
                         if 'ic_launcher' in f and f.endswith('.png')]
            
            if not icon_files:
                return None
            
            # 选择最大的PNG文件
            largest_file = None
            largest_size = 0
            for file_path in icon_files:
                file_info = zip_ref.getinfo(file_path)
                if file_info.file_size > largest_size:
                    largest_size = file_info.file_size
                    largest_file = file_path
            
            if not largest_file:
                return None
            
            # 提取文件
            output_dir.mkdir(parents=True, exist_ok=True)
            output_filename = Path(largest_file).name
            output_path = output_dir / output_filename
            
            with zip_ref.open(largest_file) as source:
                with open(output_path, 'wb') as target:
                    target.write(source.read())
            
            return output_path
            
    except Exception as e:
        print(f"  ✗ 提取失败: {e}")
        return None


def create_preview_image(
    original_path: Path,
    foreground_path: Path,
    background_path: Path,
    method_name: str,
    output_path: Path,
    preview_size: int = 512
):
    """
    创建完整的预览图，包含原图、前景、背景和合并图
    
    Args:
        original_path: 原始图标路径
        foreground_path: 前景图片路径
        background_path: 背景图片路径
        method_name: 方法名称
        output_path: 输出预览图路径
        preview_size: 预览图尺寸
    """
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
            # 尝试使用系统字体
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
        # 原图
        x1 = start_x
        y1 = start_y
        canvas.paste(original_resized, (x1, y1), original_resized if original_resized.mode == 'RGBA' else None)
        draw.text((x1, y1 + preview_size_small + 5), "Original", fill=(0, 0, 0), font=label_font)
        
        # 前景
        x2 = start_x + preview_size_small + spacing
        y2 = start_y
        canvas.paste(foreground_resized, (x2, y2), foreground_resized if foreground_resized.mode == 'RGBA' else None)
        draw.text((x2, y2 + preview_size_small + 5), "Foreground", fill=(0, 0, 0), font=label_font)
        
        # 第二行：背景和合并图
        # 背景
        x3 = start_x
        y3 = start_y + preview_size_small + label_height + spacing
        canvas.paste(background_resized, (x3, y3), background_resized if background_resized.mode == 'RGBA' else None)
        draw.text((x3, y3 + preview_size_small + 5), "Background", fill=(0, 0, 0), font=label_font)
        
        # 合并图
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


def test_separation_method(
    icon_path: Path,
    method_name: str,
    output_dir: Path,
    separation_functions: dict,
    session=None
) -> Tuple[bool, Optional[Path], Optional[Path], Optional[Path]]:
    """
    测试单个分离方法
    
    Args:
        separation_functions: 分离函数字典
    
    Returns:
        (success, foreground_path, background_path, preview_path)
    """
    print(f"  测试 {method_name} 方法...")
    
    try:
        # 获取对应的分离函数
        if method_name not in separation_functions:
            print(f"    ✗ 方法不可用: {method_name}")
            return False, None, None, None
        
        separate_func = separation_functions[method_name]
        
        # 调用分离函数
        if method_name == "u2net":
            result = separate_func(icon_path, output_dir=output_dir, session=session)
        else:
            result = separate_func(icon_path, output_dir=output_dir)
        
        if not result:
            print(f"    ✗ 分离失败")
            return False, None, None, None
        
        foreground_path, background_path = result
        print(f"    ✓ 分离成功")
        print(f"      前景: {foreground_path.name}")
        print(f"      背景: {background_path.name}")
        
        # 创建预览图
        preview_path = output_dir / f"{method_name}_preview.png"
        preview_result = create_preview_image(
            icon_path,
            foreground_path,
            background_path,
            method_name,
            preview_path
        )
        
        if preview_result:
            print(f"    ✓ 预览图: {preview_path.name}")
            return True, foreground_path, background_path, preview_path
        else:
            print(f"    ⚠ 预览图生成失败，但分离成功")
            return True, foreground_path, background_path, None
            
    except Exception as e:
        print(f"    ✗ 错误: {e}")
        import traceback
        traceback.print_exc()
        return False, None, None, None


def main():
    """主函数"""
    test_dir = get_path("test")
    
    if not test_dir.exists():
        print(f"错误: test目录不存在: {test_dir}")
        return
    
    # 查找APK文件
    apk_files = list(test_dir.glob("*.apk"))
    
    if not apk_files:
        print(f"错误: test目录中未找到APK文件")
        return
    
    print("="*60)
    print("测试所有图标分离方法并生成预览图")
    print("="*60)
    print(f"测试目录: {test_dir}\n")
    
    # 获取可用的分离方法
    separation_functions = get_separation_functions()
    available_methods = list(separation_functions.keys())
    
    if not available_methods:
        print("错误: 没有可用的分离方法")
        print("请安装以下依赖之一:")
        print("  - opencv-python (用于GrabCut和SAM2)")
        print("  - rembg (用于U2Net)")
        return
    
    print(f"可用方法: {', '.join(available_methods)}\n")
    
    # 创建rembg session（如果可用）
    session = None
    if HAS_REMBG and 'u2net' in available_methods:
        try:
            session = new_session('u2net')
            print("✓ 已创建rembg session (u2net)\n")
        except:
            try:
                session = new_session('u2netp')
                print("✓ 已创建rembg session (u2netp)\n")
            except:
                print("⚠ 无法创建rembg session，将使用默认模型\n")
    
    # 处理每个APK
    for apk_file in apk_files:
        print("="*60)
        print(f"处理APK: {apk_file.name}")
        print("="*60)
        
        # 创建输出目录
        output_dir = test_dir / apk_file.stem
        output_dir.mkdir(parents=True, exist_ok=True)
        
        # 步骤1: 提取PNG图标
        print("\n步骤1: 提取PNG图标")
        icon_path = extract_png_icon_from_apk(apk_file, output_dir)
        
        if not icon_path:
            print("  ✗ 未找到PNG图标，跳过")
            continue
        
        print(f"  ✓ 已提取: {icon_path.name}\n")
        
        # 步骤2: 测试各种分离方法
        print("步骤2: 测试分离方法\n")
        
        results = {}
        for method_name in available_methods:
            print(f"[{method_name.upper()}]")
            success, fg_path, bg_path, preview_path = test_separation_method(
                icon_path,
                method_name,
                output_dir,
                separation_functions,
                session=session if method_name == "u2net" else None
            )
            results[method_name] = {
                'success': success,
                'foreground': fg_path,
                'background': bg_path,
                'preview': preview_path
            }
            print()
        
        # 总结
        print("="*60)
        print("测试结果总结")
        print("="*60)
        success_count = sum(1 for r in results.values() if r['success'])
        print(f"成功方法: {success_count}/{len(available_methods)}")
        
        for method_name, result in results.items():
            status = "✓" if result['success'] else "✗"
            preview_status = "✓" if result['preview'] else "✗"
            print(f"  {status} {method_name.upper()}: 分离{'成功' if result['success'] else '失败'}, 预览图{preview_status}")
        print()
    
    print("="*60)
    print("所有测试完成！")
    print("="*60)
    print("\n预览图文件命名格式: {方法名}_preview.png")
    print("预览图包含: 原图、前景、背景、合并效果")


if __name__ == "__main__":
    main()

