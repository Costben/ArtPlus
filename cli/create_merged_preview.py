#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
创建前后景融合的预览图片
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
    from PIL import Image
    HAS_PIL = True
except ImportError:
    HAS_PIL = False
    print("错误: 需要安装Pillow库")
    sys.exit(1)


def merge_foreground_background(foreground_path: Path, background_path: Path, output_path: Path = None):
    """
    合并前景和背景图片
    
    Args:
        foreground_path: 前景图片路径（带透明通道）
        background_path: 背景图片路径
        output_path: 输出路径，默认为前景图片同目录下的 merged.png
    """
    if output_path is None:
        output_path = foreground_path.parent / "merged.png"
    
    try:
        # 读取前景和背景
        foreground = Image.open(foreground_path).convert('RGBA')
        background = Image.open(background_path).convert('RGBA')
        
        # 确保尺寸一致
        if foreground.size != background.size:
            # 如果尺寸不一致，调整背景尺寸
            background = background.resize(foreground.size, Image.Resampling.LANCZOS)
        
        # 创建画布，先放置背景
        merged = background.copy()
        
        # 将前景叠加在背景上
        merged = Image.alpha_composite(merged, foreground)
        
        # 保存结果
        merged.save(output_path, 'PNG')
        return output_path
        
    except Exception as e:
        print(f"错误: 合并失败: {e}")
        import traceback
        traceback.print_exc()
        return None


def main():
    """在test目录中查找并合并前后景图片"""
    test_dir = get_path("test")
    
    if not test_dir.exists():
        print(f"错误: test目录不存在: {test_dir}")
        return
    
    print("="*60)
    print("创建前后景融合预览图")
    print("="*60)
    print(f"测试目录: {test_dir}\n")
    
    # 查找所有前景和背景图片对
    foreground_files = list(test_dir.rglob("*_foreground.png"))
    
    if not foreground_files:
        print("未找到前景图片（*_foreground.png）")
        return
    
    print(f"找到 {len(foreground_files)} 个前景图片\n")
    
    success_count = 0
    fail_count = 0
    
    for i, fg_path in enumerate(foreground_files, 1):
        # 查找对应的背景图片
        bg_path = fg_path.parent / fg_path.name.replace("_foreground.png", "_background.png")
        
        if not bg_path.exists():
            print(f"[{i}/{len(foreground_files)}] {fg_path.relative_to(test_dir)}")
            print(f"  ⚠ 未找到对应的背景图片: {bg_path.name}")
            fail_count += 1
            continue
        
        print(f"[{i}/{len(foreground_files)}] {fg_path.relative_to(test_dir)}")
        
        result = merge_foreground_background(fg_path, bg_path)
        if result:
            print(f"  ✓ 已生成融合图片: {result.name}")
            success_count += 1
        else:
            print(f"  ✗ 失败")
            fail_count += 1
        print()
    
    print("="*60)
    print("完成！")
    print(f"  总计: {len(foreground_files)} 个文件")
    print(f"  成功: {success_count} 个")
    print(f"  失败: {fail_count} 个")
    print("="*60)


if __name__ == "__main__":
    main()
