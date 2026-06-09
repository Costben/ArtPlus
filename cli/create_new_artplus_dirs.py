#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
为缺失ART+图标的应用创建目录结构
根据 missing_artplus_icons.json 创建 outputs/new_artplus 文件夹及对应的应用目录
"""

import json
import sys
from pathlib import Path
from typing import List, Dict

# 添加src目录到路径
script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))

from project_helper import get_report_path, get_new_artplus_dir, ensure_dir


def load_missing_apps(json_file: str = None) -> List[Dict]:
    """
    加载缺失应用列表
    
    Args:
        json_file: JSON文件路径
        
    Returns:
        缺失应用列表
    """
    if json_file is None:
        json_path = get_report_path("missing_artplus_icons.json")
    else:
        json_path = Path(json_file)
    if not json_path.exists():
        print(f"错误: 文件不存在: {json_path}")
        return []
    
    try:
        with open(json_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
            return data.get('missing_apps', [])
    except Exception as e:
        print(f"错误: 读取文件时发生异常: {e}")
        return []


def create_app_directories(missing_apps: List[Dict], base_dir: str = None):
    """
    为缺失应用创建目录结构
    
    Args:
        missing_apps: 缺失应用列表
        base_dir: 基础目录名称
    """
    if base_dir is None:
        base_path = get_new_artplus_dir()
    else:
        base_path = Path(base_dir)
    
    # 创建基础目录
    ensure_dir(base_path)
    print(f"创建基础目录: {base_path.absolute()}")
    
    # 为每个应用创建目录
    created_count = 0
    skipped_count = 0
    
    for app in missing_apps:
        package_name = app.get('package', '')
        app_name = app.get('name', package_name)
        
        if not package_name:
            continue
        
        app_dir = base_path / package_name
        
        if app_dir.exists():
            print(f"跳过（已存在）: {package_name}")
            skipped_count += 1
            continue
        
        try:
            app_dir.mkdir(parents=True, exist_ok=True)
            print(f"创建目录: {package_name} ({app_name})")
            created_count += 1
            
            # 创建一个说明文件
            readme_file = app_dir / "README.txt"
            with open(readme_file, 'w', encoding='utf-8') as f:
                f.write(f"应用包名: {package_name}\n")
                f.write(f"应用名称: {app_name}\n\n")
                f.write("此目录用于存放该应用的ART+图标资源。\n\n")
                f.write("需要的图标文件:\n")
                f.write("- recbg.png, recbg_1x2.png, recbg_2x1.png, recbg_2x2.png (经典亮色背景)\n")
                f.write("- recfg.png, recfg_1x2.png, recfg_2x1.png, recfg_2x2.png (经典亮色前景)\n")
                f.write("- rec_night.png, rec_night_1x2.png, rec_night_2x1.png, rec_night_2x2.png (经典暗色)\n")
                f.write("- monochrome.png, monochrome_1x2.png, monochrome_2x1.png, monochrome_2x2.png (灵感样式)\n")
                f.write("- day.png (彩昼)\n")
                f.write("- nsd.png (夜影)\n")
                f.write("- mat.png (材料)\n")
                f.write("- peb.png (鹅卵石)\n")
                f.write("- outline.png (图标描边，可选)\n")
                f.write("- art_off.png (可选)\n")
                f.write("- game_app.png (可选)\n\n")
                f.write("图标规格:\n")
                f.write("- 常规尺寸: 240x240\n")
                f.write("- 变形尺寸: 240x820, 820x240, 704x704\n")
                f.write("- 快捷功能图标: 162x162\n")
                
        except Exception as e:
            print(f"错误: 创建目录失败 {package_name}: {e}")
    
    print("\n" + "="*60)
    print(f"完成！")
    print(f"  创建目录: {created_count} 个")
    print(f"  跳过目录: {skipped_count} 个")
    print(f"  总计: {len(missing_apps)} 个应用")
    print("="*60)


def create_summary_file(missing_apps: List[Dict], base_dir: str = None):
    """
    创建汇总文件
    
    Args:
        missing_apps: 缺失应用列表
        base_dir: 基础目录名称
    """
    if base_dir is None:
        base_path = get_new_artplus_dir()
    else:
        base_path = Path(base_dir)
    summary_file = base_path / "应用列表.txt"
    
    with open(summary_file, 'w', encoding='utf-8') as f:
        f.write("缺失ART+图标的应用列表\n")
        f.write("="*60 + "\n\n")
        f.write(f"总计: {len(missing_apps)} 个应用\n\n")
        
        for i, app in enumerate(missing_apps, 1):
            package_name = app.get('package', '')
            app_name = app.get('name', package_name)
            f.write(f"{i}. {app_name}\n")
            f.write(f"   包名: {package_name}\n")
            f.write(f"   目录: {package_name}/\n\n")
    
    print(f"\n汇总文件已创建: {summary_file}")


def main():
    """主函数"""
    print("ART+图标目录创建工具")
    print("="*60)
    
    # 加载缺失应用列表
    missing_apps = load_missing_apps()
    
    if not missing_apps:
        print("未找到缺失应用列表，退出")
        return
    
    print(f"\n找到 {len(missing_apps)} 个缺失ART+图标的应用")
    
    # 创建目录结构
    create_app_directories(missing_apps)
    
    # 创建汇总文件
    create_summary_file(missing_apps)
    
    print("\n所有目录已创建完成！")
    print(f"现在可以在 {get_new_artplus_dir()} 中为每个应用添加对应的图标文件。")


if __name__ == "__main__":
    main()
