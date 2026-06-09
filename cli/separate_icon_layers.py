#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
分离图标的前景和背景为两张PNG图片
提供多种轻量级方法
"""

import sys
import io
from pathlib import Path
try:
    import numpy as np
    HAS_NUMPY = True
except ImportError:
    HAS_NUMPY = False
    print("警告: 未安装numpy，某些功能可能不可用")

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
    import cv2
    HAS_OPENCV = True
except ImportError:
    HAS_OPENCV = False
    print("警告: 未安装OpenCV，将无法使用GrabCut方法")

try:
    from rembg import remove
    HAS_REMBG = True
except ImportError:
    HAS_REMBG = False
    print("提示: 未安装rembg，可以使用 'pip install rembg' 安装以获得更好的效果")


def separate_by_alpha(image_path: Path) -> tuple[Image.Image, Image.Image] | None:
    """
    方法1: 基于透明通道分离（最简单，性能最好）
    如果PNG已经有透明通道，直接使用
    """
    try:
        img = Image.open(image_path)
        
        # 转换为RGBA模式
        if img.mode != 'RGBA':
            return None
        
        # 提取alpha通道作为掩码
        alpha = img.split()[3]
        
        # 创建前景（保留原图，背景透明）
        foreground = img.copy()
        
        # 创建背景（白色背景，前景透明）
        background = Image.new('RGBA', img.size, (255, 255, 255, 255))
        # 将前景区域设为透明
        background.putalpha(alpha)
        
        return foreground, background
    except Exception as e:
        print(f"    警告: 透明通道方法失败: {e}")
        return None


def separate_by_grabcut(image_path: Path) -> tuple[Image.Image, Image.Image] | None:
    """
    方法2: 使用OpenCV GrabCut算法（轻量级，不需要模型）
    适合没有透明通道的图标
    """
    if not HAS_OPENCV:
        return None
    
    try:
        # 读取图像
        img = cv2.imread(str(image_path))
        if img is None:
            return None
        
        # 初始化掩码
        mask = np.zeros(img.shape[:2], np.uint8)
        
        # 初始化背景和前景模型
        bgd_model = np.zeros((1, 65), np.float64)
        fgd_model = np.zeros((1, 65), np.float64)
        
        # 设置初始矩形框（假设前景在中心区域，占图像的70%）
        h, w = img.shape[:2]
        margin = 0.15
        rect = (
            int(w * margin),
            int(h * margin),
            int(w * (1 - 2 * margin)),
            int(h * (1 - 2 * margin))
        )
        
        # 应用GrabCut算法
        cv2.grabCut(img, mask, rect, bgd_model, fgd_model, 5, cv2.GC_INIT_WITH_RECT)
        
        # 创建前景和背景掩码
        mask2 = np.where((mask == 2) | (mask == 0), 0, 1).astype('uint8')
        
        # 提取前景
        foreground_img = img * mask2[:, :, np.newaxis]
        
        # 提取背景
        background_img = img * (1 - mask2[:, :, np.newaxis])
        
        # 转换为PIL Image
        foreground = Image.fromarray(cv2.cvtColor(foreground_img, cv2.COLOR_BGR2RGBA))
        # 添加alpha通道
        foreground_alpha = np.ones((h, w), dtype=np.uint8) * 255
        foreground_alpha = foreground_alpha * mask2
        foreground.putalpha(Image.fromarray(foreground_alpha))
        
        # 背景设为白色
        background = Image.new('RGBA', (w, h), (255, 255, 255, 255))
        background_alpha = np.ones((h, w), dtype=np.uint8) * 255
        background_alpha = background_alpha * (1 - mask2)
        background.putalpha(Image.fromarray(background_alpha))
        
        return foreground, background
    except Exception as e:
        print(f"    警告: GrabCut方法失败: {e}")
        return None


def separate_by_rembg(image_path: Path) -> tuple[Image.Image, Image.Image] | None:
    """
    方法3: 使用rembg库（需要下载模型，但效果最好）
    使用轻量级模型 u2netp
    """
    if not HAS_REMBG:
        return None
    
    try:
        # 读取图像
        with open(image_path, 'rb') as f:
            input_data = f.read()
        
        # 使用rembg移除背景（使用轻量级模型）
        # 默认使用u2net，也可以指定u2netp（更轻量）
        output_data = remove(input_data, model_name='u2netp')
        
        # 转换为PIL Image
        foreground = Image.open(io.BytesIO(output_data))
        
        # 获取原始图像
        original = Image.open(image_path)
        
        # 创建背景（白色）
        background = Image.new('RGBA', original.size, (255, 255, 255, 255))
        
        # 如果前景有alpha通道，提取alpha
        if foreground.mode == 'RGBA':
            alpha = foreground.split()[3]
            # 背景的alpha应该是前景alpha的反转
            background.putalpha(Image.fromarray(255 - np.array(alpha)))
        
        return foreground, background
    except Exception as e:
        print(f"    警告: rembg方法失败: {e}")
        return None


def separate_icon_layers(image_path: Path, output_dir: Path = None, method: str = 'auto') -> bool:
    """
    分离图标的前景和背景
    
    Args:
        image_path: 输入图像路径
        output_dir: 输出目录（默认为图像所在目录）
        method: 方法选择 ('auto', 'alpha', 'grabcut', 'rembg')
    
    Returns:
        是否成功
    """
    if not image_path.exists():
        print(f"错误: 文件不存在: {image_path}")
        return False
    
    if output_dir is None:
        output_dir = image_path.parent
    
    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # 生成输出文件名
    stem = image_path.stem
    foreground_path = output_dir / f"{stem}_foreground.png"
    background_path = output_dir / f"{stem}_background.png"
    
    foreground = None
    background = None
    
    # 根据方法选择处理方式
    if method == 'auto':
        # 自动选择：先尝试透明通道，再尝试GrabCut，最后尝试rembg
        print(f"  尝试方法1: 透明通道...")
        result = separate_by_alpha(image_path)
        if result:
            foreground, background = result
            print(f"    ✓ 使用透明通道方法成功")
        else:
            if HAS_OPENCV:
                print(f"  尝试方法2: GrabCut...")
                result = separate_by_grabcut(image_path)
                if result:
                    foreground, background = result
                    print(f"    ✓ 使用GrabCut方法成功")
                elif HAS_REMBG:
                    print(f"  尝试方法3: rembg...")
                    result = separate_by_rembg(image_path)
                    if result:
                        foreground, background = result
                        print(f"    ✓ 使用rembg方法成功")
            elif HAS_REMBG:
                print(f"  尝试方法3: rembg...")
                result = separate_by_rembg(image_path)
                if result:
                    foreground, background = result
                    print(f"    ✓ 使用rembg方法成功")
    elif method == 'alpha':
        result = separate_by_alpha(image_path)
        if result:
            foreground, background = result
    elif method == 'grabcut':
        if not HAS_OPENCV:
            print("错误: OpenCV未安装，无法使用GrabCut方法")
            return False
        result = separate_by_grabcut(image_path)
        if result:
            foreground, background = result
    elif method == 'rembg':
        if not HAS_REMBG:
            print("错误: rembg未安装，无法使用rembg方法")
            print("请运行: pip install rembg")
            return False
        result = separate_by_rembg(image_path)
        if result:
            foreground, background = result
    
    if foreground is None or background is None:
        print(f"    ✗ 所有方法都失败")
        return False
    
    # 保存结果
    foreground.save(foreground_path, 'PNG')
    background.save(background_path, 'PNG')
    
    print(f"    ✓ 前景保存: {foreground_path.name}")
    print(f"    ✓ 背景保存: {background_path.name}")
    
    return True


def process_directory(directory: Path, method: str = 'auto'):
    """批量处理目录中的所有PNG文件"""
    png_files = list(directory.glob("*.png"))
    
    if not png_files:
        print(f"未找到PNG文件")
        return
    
    print(f"找到 {len(png_files)} 个PNG文件\n")
    
    success_count = 0
    for i, png_file in enumerate(png_files, 1):
        print(f"[{i}/{len(png_files)}] {png_file.name}")
        if separate_icon_layers(png_file, method=method):
            success_count += 1
        print()
    
    print("="*60)
    print(f"完成！成功: {success_count}/{len(png_files)}")
    print("="*60)


if __name__ == "__main__":
    import argparse
    
    parser = argparse.ArgumentParser(description='分离图标的前景和背景')
    parser.add_argument('input', type=str, help='输入图像路径或目录')
    parser.add_argument('-o', '--output', type=str, help='输出目录（仅对单个文件有效）')
    parser.add_argument('-m', '--method', type=str, choices=['auto', 'alpha', 'grabcut', 'rembg'],
                       default='auto', help='分离方法 (默认: auto)')
    
    args = parser.parse_args()
    
    input_path = Path(args.input)
    
    if input_path.is_file():
        # 处理单个文件
        separate_icon_layers(input_path, args.output, args.method)
    elif input_path.is_dir():
        # 处理目录
        process_directory(input_path, args.method)
    else:
        print(f"错误: 路径不存在: {input_path}")
