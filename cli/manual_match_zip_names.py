#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
手动匹配zip文件名和包名
用于无法自动匹配的情况
"""

import json
import sys
from pathlib import Path

# 添加src目录到路径
script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))

from project_helper import get_report_path, get_mapping_path, ensure_dir


def load_missing_apps():
    """加载缺失应用列表"""
    json_file = get_report_path("missing_artplus_icons.json")
    if not json_file.exists():
        return []
    
    try:
        with open(json_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
            return data.get('missing_apps', [])
    except:
        return []


def load_display_names():
    """加载显示名称映射"""
    mapping_file = get_mapping_path("app_display_names.json")
    if not mapping_file.exists():
        return {}
    
    try:
        with open(mapping_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
            if isinstance(data, dict):
                return data
            return {}
    except:
        return {}


def save_display_names(mapping):
    """保存显示名称映射"""
    mapping_file = get_mapping_path("app_display_names.json")
    ensure_dir(mapping_file.parent)
    with open(mapping_file, 'w', encoding='utf-8') as f:
        json.dump(mapping, f, ensure_ascii=False, indent=2)
    print(f"\n已保存到: {mapping_file.name}")


def manual_match():
    """手动匹配zip文件名和包名"""
    print("="*60)
    print("手动匹配zip文件名和包名")
    print("="*60)
    
    # 加载应用列表
    missing_apps = load_missing_apps()
    if not missing_apps:
        print("未找到缺失应用列表")
        return
    
    # 加载现有映射
    display_mapping = load_display_names()
    
    print(f"\n找到 {len(missing_apps)} 个缺失应用")
    print("\n请输入zip文件名（不含_icon.zip后缀）和对应的包名")
    print("格式: zip文件名|包名")
    print("例如: 冰箱|com.catchingnow.icebox")
    print("输入 'done' 完成，输入 'skip' 跳过当前应用\n")
    
    # 显示未匹配的应用
    unmatched_apps = []
    for app in missing_apps:
        package = app.get('package', '')
        if package not in display_mapping or display_mapping[package].get('display_name') == package:
            unmatched_apps.append(app)
    
    if not unmatched_apps:
        print("所有应用都已匹配！")
        return
    
    print(f"还有 {len(unmatched_apps)} 个应用需要匹配\n")
    
    for i, app in enumerate(unmatched_apps, 1):
        package = app.get('package', '')
        current_name = app.get('name', package)
        
        print(f"[{i}/{len(unmatched_apps)}] 包名: {package}")
        print(f"  当前名称: {current_name}")
        
        if package in display_mapping:
            current_display = display_mapping[package].get('display_name', '')
            if current_display and current_display != package:
                print(f"  已有显示名称: {current_display}")
        
        user_input = input("  请输入zip文件名（不含_icon.zip）或'skip'跳过: ").strip()
        
        if user_input.lower() == 'skip':
            continue
        elif user_input.lower() == 'done':
            break
        elif user_input:
            # 更新映射
            display_mapping[package] = {
                "package": package,
                "name": current_name,
                "display_name": user_input
            }
            print(f"  ✓ 已匹配: {user_input} -> {package}")
    
    # 保存映射
    if display_mapping:
        save_display_names(display_mapping)
        print(f"\n已更新 {len(display_mapping)} 个应用映射")


def batch_update_from_file():
    """从文件批量更新映射"""
    print("="*60)
    print("从文件批量更新应用名称映射")
    print("="*60)
    
    mapping_file = get_mapping_path("app_display_names.json")
    display_mapping = load_display_names()
    
    print("\n请创建一个文本文件，每行格式: 包名|显示名称")
    print("例如:")
    print("com.catchingnow.icebox|冰箱")
    print("bin.mt.plus|MT管理器")
    print("\n输入文件路径（或直接输入内容，输入'end'结束）:")
    
    lines = []
    while True:
        line = input().strip()
        if line.lower() == 'end':
            break
        if line:
            lines.append(line)
    
    updated_count = 0
    for line in lines:
        if '|' in line:
            parts = line.split('|', 1)
            if len(parts) == 2:
                package = parts[0].strip()
                display_name = parts[1].strip()
                if package and display_name:
                    # 查找对应的应用
                    missing_apps = load_missing_apps()
                    app_info = None
                    for app in missing_apps:
                        if app.get('package') == package:
                            app_info = app
                            break
                    
                    if app_info:
                        display_mapping[package] = {
                            "package": package,
                            "name": app_info.get('name', package),
                            "display_name": display_name
                        }
                        updated_count += 1
                        print(f"  ✓ {package} -> {display_name}")
    
    if updated_count > 0:
        save_display_names(display_mapping)
        print(f"\n已更新 {updated_count} 个应用映射")
    else:
        print("\n未更新任何映射")


if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "batch":
        batch_update_from_file()
    else:
        manual_match()
