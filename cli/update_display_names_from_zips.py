#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
从zip文件名更新应用显示名称映射
扫描手机Download/Apks目录中的zip文件，手动匹配包名
"""

import json
import subprocess
import sys
from pathlib import Path
from typing import Dict, List

# 添加src目录到路径
script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))

from adb_helper import get_adb_path
from project_helper import get_report_path, get_mapping_path, ensure_dir


def load_missing_apps() -> List[Dict]:
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


def load_display_names() -> Dict:
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


def save_display_names(mapping: Dict):
    """保存显示名称映射"""
    mapping_file = get_mapping_path("app_display_names.json")
    ensure_dir(mapping_file.parent)
    with open(mapping_file, 'w', encoding='utf-8') as f:
        json.dump(mapping, f, ensure_ascii=False, indent=2)
    print(f"\n已保存到: {mapping_file.name}")


def extract_zip_name(zip_filename: str) -> str:
    """从zip文件名提取应用名称"""
    name = Path(zip_filename).stem
    if name.endswith('_icon'):
        name = name[:-5]
    return name.strip()


def update_from_phone_zips():
    """从手机上的zip文件更新映射"""
    print("="*60)
    print("从手机zip文件更新应用显示名称")
    print("="*60)
    
    # 检查adb连接
    adb_path = get_adb_path()
    try:
        result = subprocess.run([adb_path, "devices"], capture_output=True, text=True, encoding='utf-8', errors='ignore', timeout=5)
        device_lines = [line for line in result.stdout.split('\n') if line.strip() and 'device' in line and 'List of devices' not in line]
        if not device_lines:
            print("错误: 未检测到已连接的设备")
            return
    except Exception as e:
        print(f"错误: 无法检查adb连接: {e}")
        return
    
    # 从手机获取zip文件列表
    phone_zip_dir = "/sdcard/Download/Apks"
    print(f"\n从手机获取zip文件列表: {phone_zip_dir}")
    
    try:
        cmd = [adb_path, "shell", "ls", phone_zip_dir]
        result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8', errors='ignore', timeout=10)
        
        if result.returncode != 0 or not result.stdout:
            print(f"错误: 无法访问手机目录或目录为空")
            return
        
        zip_files = []
        for line in result.stdout.strip().split('\n'):
            line = line.strip()
            if line and (line.endswith('_icon.zip') or line.endswith('.zip')):
                zip_files.append(line)
        
        if not zip_files:
            print("未找到zip文件")
            return
        
        print(f"找到 {len(zip_files)} 个zip文件\n")
        
    except Exception as e:
        print(f"错误: 获取文件列表失败: {e}")
        return
    
    # 加载应用列表和现有映射
    missing_apps = load_missing_apps()
    display_mapping = load_display_names()
    
    # 创建包名到应用的映射
    app_by_package = {app.get('package'): app for app in missing_apps}
    
    print("开始匹配zip文件和应用包名...\n")
    
    updated_count = 0
    unmatched_zips = []
    
    for zip_filename in zip_files:
        app_name = extract_zip_name(zip_filename)
        print(f"Zip文件: {zip_filename}")
        print(f"  提取的应用名称: {app_name}")
        
        # 尝试自动匹配
        matched = False
        for package, app in app_by_package.items():
            # 简单的匹配逻辑
            package_clean = package.lower().replace('.', '').replace('_', '').replace('-', '')
            name_clean = app_name.lower().replace(' ', '').replace('_', '').replace('-', '').replace('+', '')
            
            # 检查包名是否包含应用名称的一部分，或应用名称是否包含包名的关键部分
            if name_clean in package_clean or any(part in name_clean for part in package_clean.split('.') if len(part) > 3):
                # 找到可能的匹配，询问用户确认
                print(f"  可能的匹配: {package}")
                confirm = input(f"  是否匹配? (y/n，直接回车跳过): ").strip().lower()
                if confirm == 'y':
                    display_mapping[package] = {
                        "package": package,
                        "name": app.get('name', package),
                        "display_name": app_name
                    }
                    updated_count += 1
                    print(f"  ✓ 已匹配: {app_name} -> {package}")
                    matched = True
                    break
        
        if not matched:
            # 无法自动匹配，询问用户
            print(f"  无法自动匹配，请输入包名（或'skip'跳过）:")
            user_input = input("  ").strip()
            
            if user_input.lower() == 'skip':
                unmatched_zips.append((zip_filename, app_name))
            elif user_input:
                package = user_input
                if package in app_by_package:
                    app = app_by_package[package]
                    display_mapping[package] = {
                        "package": package,
                        "name": app.get('name', package),
                        "display_name": app_name
                    }
                    updated_count += 1
                    print(f"  ✓ 已匹配: {app_name} -> {package}")
                else:
                    print(f"  ⚠ 包名不在缺失应用列表中")
        
        print()
    
    # 保存映射
    if updated_count > 0:
        save_display_names(display_mapping)
        print(f"已更新 {updated_count} 个应用映射")
    
    if unmatched_zips:
        print(f"\n未匹配的zip文件 ({len(unmatched_zips)} 个):")
        for zip_file, app_name in unmatched_zips:
            print(f"  {zip_file} ({app_name})")


if __name__ == "__main__":
    update_from_phone_zips()
