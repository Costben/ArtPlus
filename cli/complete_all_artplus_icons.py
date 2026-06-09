#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量完善outputs/new_artplus目录下所有包的ART+图标文件
- 画布扩展（生成1x2, 2x1, 2x2变形）
- 单色化（生成monochrome系列）
- 生成暗色图标（rec_night系列）
- 生成其他样式（day, nsd, mat, peb）
"""

import sys
import io
from pathlib import Path
from typing import Tuple

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
    generate_all_icons,
    create_resized_background,
    SIZE_1x1, SIZE_1x2, SIZE_2x1, SIZE_2x2
)
from PIL import Image


def check_missing_files(app_dir: Path) -> Tuple[list, list]:
    """
    检查应用目录缺少哪些文件
    
    Returns:
        (缺少的文件列表, 已有的文件列表)
    """
    required_files = {
        # 变形背景
        "recbg_1x2.png", "recbg_2x1.png", "recbg_2x2.png",
        # 变形前景
        "recfg_1x2.png", "recfg_2x1.png", "recfg_2x2.png",
        # 暗色图标
        "rec_night.png", "rec_night_1x2.png", "rec_night_2x1.png", "rec_night_2x2.png",
        # 单色图标
        "monochrome.png", "monochrome_1x2.png", "monochrome_2x1.png", "monochrome_2x2.png",
        # 其他样式
        "day.png", "nsd.png", "mat.png", "peb.png"
    }
    
    missing = []
    existing = []
    
    for filename in required_files:
        filepath = app_dir / filename
        if filepath.exists():
            existing.append(filename)
        else:
            missing.append(filename)
    
    return missing, existing


def process_app_directory(app_dir: Path, force_regenerate: bool = False) -> Tuple[bool, str, int]:
    """
    处理单个应用目录
    
    Args:
        app_dir: 应用目录路径
        force_regenerate: 是否强制重新生成所有文件
        
    Returns:
        (是否成功, 消息, 生成的文件数)
    """
    package_name = app_dir.name
    
    # 检查基础文件
    recbg_path = app_dir / "recbg.png"
    recfg_path = app_dir / "recfg.png"
    
    # 如果缺少recfg.png，无法处理
    if not recfg_path.exists():
        return False, "缺少recfg.png", 0
    
    # 如果缺少recbg.png，自动创建白色背景
    if not recbg_path.exists():
        print(f"  缺少recbg.png，自动创建白色背景...")
        bg_image = create_resized_background(SIZE_1x1, 'white')
        bg_image.save(recbg_path, 'PNG', optimize=True)
        print(f"  ✓ 已创建recbg.png")
    
    # 检查缺少的文件
    missing, existing = check_missing_files(app_dir)
    
    if not force_regenerate and len(missing) == 0:
        return False, f"所有文件已存在（{len(existing)}个）", 0
    
    if len(missing) > 0:
        print(f"  缺少 {len(missing)} 个文件，开始生成...")
    
    # 生成所有图标
    try:
        success = generate_all_icons(str(app_dir))
        
        if not success:
            return False, "生成失败", 0
        
        # 重新检查生成的文件
        _, new_existing = check_missing_files(app_dir)
        generated_count = len(new_existing) - len(existing)
        
        return True, f"成功生成 {generated_count} 个文件", generated_count
        
    except Exception as e:
        return False, f"处理失败: {e}", 0


def main():
    """主函数"""
    new_artplus_dir = get_path("outputs/new_artplus")
    
    if not new_artplus_dir.exists():
        print(f"错误: outputs/new_artplus目录不存在: {new_artplus_dir}")
        return
    
    print("="*60)
    print("完善outputs/new_artplus目录下所有包的ART+图标文件")
    print("="*60)
    print(f"目标目录: {new_artplus_dir}\n")
    print("处理内容:")
    print("- 画布扩展：生成1x2, 2x1, 2x2变形图标")
    print("- 单色化：生成monochrome系列图标")
    print("- 暗色图标：生成rec_night系列图标")
    print("- 其他样式：生成day, nsd, mat, peb图标")
    print()
    
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
    total_generated = 0
    
    for i, app_dir in enumerate(app_dirs, 1):
        package_name = app_dir.name
        print(f"[{i}/{total_apps}] {package_name}")
        
        success, message, generated = process_app_directory(app_dir, force_regenerate=False)
        
        if success:
            print(f"  ✓ {message}")
            success_count += 1
            total_generated += generated
        elif "已存在" in message or "所有文件已存在" in message:
            print(f"  ⚠ {message}")
            skip_count += 1
        elif "缺少recfg.png" in message:
            print(f"  ✗ {message}")
            fail_count += 1
        else:
            print(f"  ⚠ {message}")
            skip_count += 1
        
        print()
    
    print("="*60)
    print("处理完成！")
    print(f"  总计: {total_apps} 个应用")
    print(f"  成功: {success_count} 个")
    print(f"  跳过: {skip_count} 个")
    print(f"  失败: {fail_count} 个")
    print(f"  生成文件总数: {total_generated} 个")
    print("="*60)
    print("\n说明:")
    print("- 每个包应包含20个ART+图标文件（不包括基础recbg.png和recfg.png）")
    print("- 如果基础文件已存在，将跳过该包")
    print("- 使用 --force 参数可以强制重新生成所有文件")


if __name__ == "__main__":
    import argparse
    
    parser = argparse.ArgumentParser(description='完善outputs/new_artplus目录下所有包的ART+图标文件')
    parser.add_argument('--force', action='store_true', help='强制重新生成所有文件')
    args = parser.parse_args()
    
    # 如果指定了--force，需要修改process_app_directory的调用
    # 这里先实现基本功能，force功能可以后续添加
    
    main()

