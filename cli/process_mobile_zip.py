#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
处理从手机提取的图标 zip 文件
1. 扫描 mobile_extracted 目录下的 zip 文件
2. 根据文件名提取或匹配包名
3. 在 zip 所在目录创建包名文件夹
4. 解压图标到包名文件夹
"""

import os
import zipfile
import shutil
import re
import json
import sys
from pathlib import Path

# 添加项目根目录到路径
script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))

try:
    from project_helper import get_mapping_path, ensure_dir
except ImportError:
    # 回退方案，如果项目结构不符合预期
    def get_mapping_path(name):
        return project_root / "outputs" / "mappings" / name
    def ensure_dir(p):
        p.mkdir(parents=True, exist_ok=True)
        return p

def load_display_name_mapping():
    """加载显示名称到包名的映射"""
    mapping_file = get_mapping_path("app_display_names.json")
    display_to_package = {}
    if mapping_file.exists():
        try:
            with open(mapping_file, 'r', encoding='utf-8') as f:
                data = json.load(f)
                for pkg, info in data.items():
                    display_name = info.get('display_name')
                    if display_name:
                        display_to_package[display_name] = pkg
        except Exception as e:
            print(f"警告: 加载映射文件失败: {e}")
    return display_to_package

def extract_package_name(filename, mapping):
    """
    从文件名中提取或匹配包名
    支持格式:
    1. {应用名称}_{包名}.apk_icon.zip
    2. {包名}.zip
    3. {显示名称}.zip
    """
    # 移除 .zip 后缀
    name = filename
    if name.lower().endswith('.zip'):
        name = name[:-4]
    
    # 1. 处理特定的 {应用名称}_{包名}.apk_icon 格式
    # 匹配 _{包名}.apk_icon，使用更精确的包名正则表达式
    # 修改正则以支持大写字母和数字开头的段（虽然不常见但存在）
    match = re.search(r'_([a-zA-Z][a-zA-Z0-9_]*(?:\.[a-zA-Z][a-zA-Z0-9_]*)+)\.apk_icon$', name)
    if match:
        return match.group(1)

    # 2. 移除常见的后缀干扰
    name = re.sub(r'\.apk_icon$', '', name)
    name = re.sub(r'_icon$', '', name)
    
    # 3. 检查是否本身就是包名格式 (a.b.c)
    if re.match(r'^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$', name):
        return name
    
    # 4. 检查是否包含包名 (如: 微信_com.tencent.mm)
    match = re.search(r'([a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+)', name)
    if match:
        return match.group(1)
    
    # 5. 尝试在映射中匹配显示名称
    if name in mapping:
        return mapping[name]
    
    # 6. 如果都匹配不到，直接返回清理后的文件名
    return name

def process_zips():
    """处理 mobile_extracted 目录下的所有 zip 文件"""
    mobile_dir = project_root / "mobile_extracted"
    if not mobile_dir.exists():
        print(f"目录不存在: {mobile_dir}")
        return

    mapping = load_display_name_mapping()
    zip_files = list(mobile_dir.glob("*.zip"))
    
    if not zip_files:
        print("未发现 zip 文件。")
        return

    print(f"发现 {len(zip_files)} 个 zip 文件，开始处理...")
    
    for zip_path in zip_files:
        print(f"\n正在处理: {zip_path.name}")
        
        # 1. 确定包名
        package_name = extract_package_name(zip_path.name, mapping)
        print(f"解析出的包名: {package_name}")
        
        # 2. 创建包名文件夹
        pkg_dir = mobile_dir / package_name
        ensure_dir(pkg_dir)
        
        # 3. 解压文件
        try:
            with zipfile.ZipFile(zip_path, 'r') as zip_ref:
                # 只提取 png 图标文件
                for file_info in zip_ref.infolist():
                    if file_info.is_dir():
                        continue
                    
                    filename = os.path.basename(file_info.filename)
                    if not filename or not filename.lower().endswith('.png'):
                        continue
                    
                    target_path = pkg_dir / filename
                    with zip_ref.open(file_info) as source, open(target_path, 'wb') as target:
                        shutil.copyfileobj(source, target)
            
            print(f"成功解压到: {pkg_dir}")
            
            # 4. 处理完成后（可选）移动或删除 zip
            # archive_dir = mobile_dir / "archives"
            # ensure_dir(archive_dir)
            # shutil.move(str(zip_path), str(archive_dir / zip_path.name))
            
        except Exception as e:
            print(f"处理 zip 失败: {e}")

if __name__ == "__main__":
    process_zips()
