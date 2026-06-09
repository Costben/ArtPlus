#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量提取图标zip文件到outputs/new_artplus对应目录
从手机Download/Apks目录读取zip文件，根据应用名称匹配包名，解压到对应目录
"""

import zipfile
import shutil
import re
import subprocess
import json
from pathlib import Path
from typing import Dict, Optional
import sys

# 添加src目录到路径
script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))

from adb_helper import get_adb_path
from project_helper import get_mapping_path, get_report_path, get_new_artplus_dir


def check_app_has_png_icon(package_name: str, base_dir: Path) -> bool:
    """
    检查应用是否有PNG图标（至少有一个recfg.png或其他ART+图标）
    
    Args:
        package_name: 应用包名
        base_dir: outputs/new_artplus目录
        
    Returns:
        是否有PNG图标
    """
    app_dir = base_dir / package_name
    if not app_dir.exists():
        return False
    
    # 检查是否有任何PNG图标文件
    png_files = list(app_dir.glob("*.png"))
    
    # 排除src目录中的文件
    png_files = [f for f in png_files if f.parent.name != "src"]
    
    return len(png_files) > 0


def load_app_mapping(json_file: str = None):
    """
    加载应用名称到包名的映射
    优先使用app_display_names.json（包含中文显示名称）
    
    Returns:
        (mapping字典, all_apps列表)
    """
    # 首先尝试加载app_display_names.json（包含中文显示名称）
    display_names_file = get_mapping_path("app_display_names.json")
    display_mapping = {}
    
    if display_names_file.exists():
        try:
            with open(display_names_file, 'r', encoding='utf-8') as f:
                display_data = json.load(f)
                if isinstance(display_data, dict):
                    # 如果是字典，键是包名
                    display_mapping = display_data
                elif isinstance(display_data, list):
                    # 如果是列表，转换为字典
                    for item in display_data:
                        if isinstance(item, dict):
                            package = item.get('package', '')
                            if package:
                                display_mapping[package] = item
        except Exception as e:
            print(f"警告: 加载显示名称映射失败: {e}")
    
    # 加载missing_artplus_icons.json作为基础
    if json_file is None:
        json_file = get_report_path("missing_artplus_icons.json")
    else:
        json_file = Path(json_file)
    
    missing_apps = []
    if json_file.exists():
        try:
            with open(json_file, 'r', encoding='utf-8') as f:
                data = json.load(f)
                missing_apps = data.get('missing_apps', [])
        except Exception as e:
            print(f"警告: 加载缺失应用列表失败: {e}")
    
    # 创建映射：应用名称 -> 包名
    mapping = {}
    
    # 优先使用display_names中的显示名称
    for app in missing_apps:
        package_name = app.get('package', '')
        if not package_name:
            continue
        
        # 如果有显示名称映射，使用显示名称
        if package_name in display_mapping:
            display_info = display_mapping[package_name]
            if isinstance(display_info, dict):
                display_name = display_info.get('display_name', '') or display_info.get('name', '')
            else:
                display_name = str(display_info)
            
            if display_name:
                # 使用显示名称作为key（保留原始大小写，用于精确匹配）
                mapping[display_name] = package_name
                # 也添加小写版本
                clean_name = display_name.lower().strip()
                mapping[clean_name] = package_name
                # 也添加去除空格、下划线、连字符、加号的版本（用于模糊匹配）
                clean_name_no_space = clean_name.replace(' ', '').replace('_', '').replace('-', '').replace('+', '')
                if clean_name_no_space != clean_name:
                    mapping[clean_name_no_space] = package_name
        
        # 也使用原始名称
        app_name = app.get('name', package_name)
        clean_name = app_name.lower().strip()
        if clean_name not in mapping:
            mapping[clean_name] = package_name
        
        # 也添加包名作为key
        mapping[package_name.lower()] = package_name
    
    return mapping, missing_apps


def extract_zip_name(zip_filename: str) -> str:
    """
    从zip文件名提取应用名称
    格式: 应用名称_icon.zip
    
    Args:
        zip_filename: zip文件名（如 gkd_icon.zip 或 闲鱼_icon.zip）
        
    Returns:
        应用名称（如 gkd 或 闲鱼）
    """
    # 移除扩展名
    name = Path(zip_filename).stem
    
    # 移除 _icon 后缀
    if name.endswith('_icon'):
        name = name[:-5]  # 移除 '_icon'
    
    # 保留原始大小写和空格（用于中文匹配）
    return name.strip()


def find_package_by_name(app_name: str, mapping: Dict[str, str], all_apps: list = None) -> Optional[str]:
    """
    根据应用名称查找包名
    支持中文名称匹配
    
    Args:
        app_name: 应用名称（从zip文件名提取，可能包含中文）
        mapping: 应用名称到包名的映射
        all_apps: 所有应用的完整列表（包含name和package）
        
    Returns:
        包名，如果找不到返回None
    """
    # 直接匹配（保留原始大小写和空格，用于中文精确匹配）
    if app_name in mapping:
        return mapping[app_name]
    
    # 直接匹配（小写，用于英文匹配）
    app_name_lower = app_name.lower().strip()
    if app_name_lower in mapping:
        return mapping[app_name_lower]
    
    # 直接匹配（去除空格，用于"甲壳虫ADB助手"匹配"甲壳虫 ADB 助手"）
    app_name_no_space = app_name.replace(' ', '').replace('_', '').replace('-', '')
    if app_name_no_space in mapping:
        return mapping[app_name_no_space]
    
    app_name_no_space_lower = app_name_no_space.lower()
    if app_name_no_space_lower in mapping:
        return mapping[app_name_no_space_lower]
    
    # 模糊匹配：检查是否包含（忽略大小写、空格、下划线、连字符、加号）
    app_name_clean = app_name_lower.replace(' ', '').replace('_', '').replace('-', '').replace('+', '')
    
    for mapped_name, package in mapping.items():
        mapped_clean = mapped_name.lower().strip().replace(' ', '').replace('_', '').replace('-', '').replace('+', '')
        if app_name_clean == mapped_clean:
            return package
        if app_name_clean in mapped_clean or mapped_clean in app_name_clean:
            return package
    
    # 如果提供了all_apps，尝试更精确的匹配
    if all_apps:
        for app in all_apps:
            app_display_name = app.get('name', '')
            package = app.get('package', '')
            
            # 清理名称进行比较
            display_clean = app_display_name.lower().strip().replace(' ', '').replace('_', '').replace('-', '').replace('+', '')
            
            if app_name_clean == display_clean or app_name_clean in display_clean or display_clean in app_name_clean:
                return package
    
    # 尝试从包名中查找（如果应用名称是包名的一部分）
    for mapped_name, package in mapping.items():
        package_clean = package.lower().strip().replace('.', '').replace('_', '').replace('-', '').replace('+', '')
        if app_name_clean in package_clean:
            return package
    
    return None


def extract_zip_to_app_dir(zip_path: Path, app_dir: Path, package_name: str):
    """
    解压zip文件到应用目录
    
    Args:
        zip_path: zip文件路径
        app_dir: 应用目录路径
        package_name: 包名（用于日志）
        
    Returns:
        是否成功
    """
    try:
        # 确保应用目录存在
        app_dir.mkdir(parents=True, exist_ok=True)
        
        # 解压zip文件
        with zipfile.ZipFile(zip_path, 'r') as zip_ref:
            # 获取所有文件列表
            file_list = zip_ref.namelist()
            
            extracted_count = 0
            for file_info in file_list:
                # 跳过目录
                if file_info.endswith('/'):
                    continue
                
                # 提取文件名
                filename = Path(file_info).name
                
                # 跳过空文件名
                if not filename:
                    continue
                
                # 目标文件路径
                dest_file = app_dir / filename
                
                # 如果文件已存在，添加序号
                if dest_file.exists():
                    base_name = dest_file.stem
                    ext = dest_file.suffix
                    counter = 1
                    while dest_file.exists():
                        dest_file = app_dir / f"{base_name}_{counter}{ext}"
                        counter += 1
                
                # 提取文件
                try:
                    with zip_ref.open(file_info) as source:
                        with open(dest_file, 'wb') as target:
                            shutil.copyfileobj(source, target)
                    extracted_count += 1
                except Exception as e:
                    print(f"    警告: 提取文件失败 {file_info}: {e}")
            
            print(f"  ✓ 解压完成: {extracted_count} 个文件")
            return True
            
    except Exception as e:
        print(f"  ✗ 解压失败: {e}")
        return False


def extract_icon_zips_from_phone():
    """从手机Download/Apks目录提取图标zip文件"""
    print("="*60)
    print("批量提取图标zip文件")
    print("="*60)
    
    # 检查adb连接
    adb_path = get_adb_path()
    try:
        result = subprocess.run([adb_path, "devices"], capture_output=True, text=True, encoding='utf-8', errors='ignore', timeout=5)
        if not result.stdout:
            print("错误: 无法获取adb设备列表")
            return
        # 检查是否有已连接的设备（排除"List of devices attached"和空行）
        device_lines = [line for line in result.stdout.split('\n') if line.strip() and 'device' in line and 'List of devices' not in line]
        if not device_lines:
            print("错误: 未检测到已连接的设备")
            return
    except Exception as e:
        print(f"错误: 无法检查adb连接: {e}")
        return
    
    # 加载应用映射
    print("\n加载应用映射...")
    mapping, all_apps = load_app_mapping()
    print(f"已加载 {len(mapping)} 个应用映射")
    
    # 从手机拉取zip文件列表
    print("\n从手机获取zip文件列表...")
    phone_zip_dir = "/sdcard/Download/Apks"
    
    try:
        cmd = [adb_path, "shell", "ls", phone_zip_dir]
        result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8', errors='ignore', timeout=10)
        
        if result.returncode != 0:
            print(f"错误: 无法访问手机目录: {phone_zip_dir}")
            if result.stderr:
                print(f"错误信息: {result.stderr}")
            return
        
        # 解析文件列表
        zip_files = []
        if result.stdout:
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
    
    # 创建临时目录
    import tempfile
    temp_dir = Path(tempfile.mkdtemp())
    
    # 拉取并处理每个zip文件
    base_dir = get_new_artplus_dir()
    success_count = 0
    fail_count = 0
    unmatched_count = 0
    
    for i, zip_filename in enumerate(zip_files, 1):
        print(f"[{i}/{len(zip_files)}] {zip_filename}")
        
        # 提取应用名称（保留原始大小写和空格，用于中文匹配）
        app_name = extract_zip_name(zip_filename)
        print(f"  应用名称: {app_name}")
        
        # 查找包名
        package_name = find_package_by_name(app_name, mapping, all_apps)
        
        # 如果找不到，尝试使用原始大小写匹配
        if not package_name:
            app_name_original = app_name  # 保留原始
            package_name = find_package_by_name(app_name_original, mapping, all_apps)
        
        if not package_name:
            print(f"  ⚠ 无法匹配包名，跳过")
            unmatched_count += 1
            continue
        
        print(f"  包名: {package_name}")
        
        # 检查是否已有PNG图标，如果有则跳过
        if check_app_has_png_icon(package_name, base_dir):
            print(f"  ⚠ 已存在PNG图标，跳过")
            unmatched_count += 1
            continue
        
        # 拉取zip文件到临时目录
        phone_zip_path = f"{phone_zip_dir}/{zip_filename}"
        local_zip_path = temp_dir / zip_filename
        
        try:
            cmd = [adb_path, "pull", phone_zip_path, str(local_zip_path)]
            result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8', errors='ignore', timeout=30)
            
            if result.returncode != 0 or not local_zip_path.exists():
                print(f"  ✗ 拉取文件失败")
                if result.stderr:
                    print(f"    错误: {result.stderr[:100]}")
                fail_count += 1
                continue
        except Exception as e:
            print(f"  ✗ 拉取文件失败: {e}")
            fail_count += 1
            continue
        
        # 解压到应用目录
        app_dir = base_dir / package_name
        if extract_zip_to_app_dir(local_zip_path, app_dir, package_name):
            success_count += 1
        else:
            fail_count += 1
        
        # 删除临时文件
        try:
            local_zip_path.unlink()
        except:
            pass
    
    # 清理临时目录
    try:
        shutil.rmtree(temp_dir)
    except:
        pass
    
    print("\n" + "="*60)
    print("提取完成！")
    print(f"  总计: {len(zip_files)} 个zip文件")
    print(f"  成功: {success_count} 个")
    print(f"  失败: {fail_count} 个")
    print(f"  无法匹配: {unmatched_count} 个")
    print("="*60)


def extract_icon_zips_from_local(local_dir: str = None):
    """
    从本地目录提取图标zip文件
    
    Args:
        local_dir: 本地zip文件目录，如果为None则从手机拉取
    """
    if local_dir:
        # 从本地目录处理
        local_path = Path(local_dir)
        if not local_path.exists():
            print(f"错误: 目录不存在: {local_dir}")
            return
        
        # 加载应用映射
        print("\n加载应用映射...")
        mapping, all_apps = load_app_mapping()
        print(f"已加载 {len(mapping)} 个应用映射\n")
        
        # 查找所有zip文件
        zip_files = list(local_path.glob("*_icon.zip"))
        
        if not zip_files:
            print("未找到zip文件")
            return
        
        print(f"找到 {len(zip_files)} 个zip文件\n")
        
        base_dir = get_new_artplus_dir()
        success_count = 0
        fail_count = 0
        unmatched_count = 0
        
        for i, zip_path in enumerate(zip_files, 1):
            zip_filename = zip_path.name
            print(f"[{i}/{len(zip_files)}] {zip_filename}")
            
            # 提取应用名称
            app_name = extract_zip_name(zip_filename)
            print(f"  应用名称: {app_name}")
            
            # 查找包名
            package_name = find_package_by_name(app_name, mapping, all_apps)
            
            if not package_name:
                print(f"  ⚠ 无法匹配包名，跳过")
                unmatched_count += 1
                continue
            
            print(f"  包名: {package_name}")
            
            # 检查是否已有PNG图标，如果有则跳过
            if check_app_has_png_icon(package_name, base_dir):
                print(f"  ⚠ 已存在PNG图标，跳过")
                unmatched_count += 1
                continue
            
            # 解压到应用目录
            app_dir = base_dir / package_name
            if extract_zip_to_app_dir(zip_path, app_dir, package_name):
                success_count += 1
            else:
                fail_count += 1
        
        print("\n" + "="*60)
        print("提取完成！")
        print(f"  总计: {len(zip_files)} 个zip文件")
        print(f"  成功: {success_count} 个")
        print(f"  失败: {fail_count} 个")
        print(f"  无法匹配: {unmatched_count} 个")
        print("="*60)
    else:
        # 从手机拉取
        extract_icon_zips_from_phone()


if __name__ == "__main__":
    if len(sys.argv) > 1:
        # 从本地目录提取
        local_dir = sys.argv[1]
        extract_icon_zips_from_local(local_dir)
    else:
        # 从手机拉取
        extract_icon_zips_from_phone()
