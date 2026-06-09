#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
更新theme目录中的XML文件，添加outputs/new_artplus中的所有包名
"""

import sys
import io
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Set

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


def get_package_names_from_new_artplus() -> Set[str]:
    """从outputs/new_artplus目录获取所有包名"""
    new_artplus_dir = get_path("outputs/new_artplus")
    packages = set()
    
    if not new_artplus_dir.exists():
        return packages
    
    for app_dir in new_artplus_dir.iterdir():
        if app_dir.is_dir() and not app_dir.name.startswith('.'):
            # 检查是否有recfg.png（确保是有效的包）
            if (app_dir / "recfg.png").exists():
                packages.add(app_dir.name)
    
    return packages


def update_allapps_xml():
    """更新allApps.xml，添加outputs/new_artplus中的包名"""
    theme_dir = get_path("theme")
    allapps_xml = theme_dir / "allApps.xml"
    
    if not allapps_xml.exists():
        print(f"错误: {allapps_xml} 不存在")
        return False
    
    # 获取outputs/new_artplus中的所有包名
    new_packages = get_package_names_from_new_artplus()
    print(f"找到 {len(new_packages)} 个包")
    
    # 解析现有XML
    tree = ET.parse(allapps_xml)
    root = tree.getroot()
    
    # 获取现有的包名
    existing_packages = set()
    for icon in root.findall('icon'):
        package = icon.get('package')
        if package:
            existing_packages.add(package)
    
    print(f"现有XML中有 {len(existing_packages)} 个包")
    
    # 找出需要添加的包
    packages_to_add = new_packages - existing_packages
    
    if not packages_to_add:
        print("所有包都已存在于XML中")
        return True
    
    print(f"需要添加 {len(packages_to_add)} 个新包")
    
    # 添加新包
    # 注意：对于ART+图标，系统会从/system/etc/uxicons/{package}/目录自动查找
    # 但为了兼容性，我们仍然在allApps.xml中添加条目
    for package in sorted(packages_to_add):
        # 创建icon元素
        icon_elem = ET.Element('icon')
        # 对于ART+图标，使用包名作为图标名称（系统会忽略此名称，直接从uxicons查找）
        # 但为了格式正确，我们使用包名
        icon_elem.set('name', package)
        icon_elem.set('package', package)
        root.append(icon_elem)
    
    # 保存XML（保持格式）
    tree.write(allapps_xml, encoding='UTF-8', xml_declaration=True)
    
    print(f"✓ 已更新 {allapps_xml}")
    print(f"  添加了 {len(packages_to_add)} 个新包")
    
    return True


def update_drawablemapping_xml():
    """更新drawableMapping.xml，添加outputs/new_artplus中的包名"""
    theme_dir = get_path("theme")
    drawable_xml = theme_dir / "drawableMapping.xml"
    
    if not drawable_xml.exists():
        print(f"警告: {drawable_xml} 不存在，跳过")
        return False
    
    # 获取outputs/new_artplus中的所有包名
    new_packages = get_package_names_from_new_artplus()
    
    # 解析现有XML
    tree = ET.parse(drawable_xml)
    root = tree.getroot()
    
    # 获取现有的包名
    existing_packages = set()
    for item in root.findall('item'):
        package = item.get('package')
        if package:
            existing_packages.add(package)
    
    # 找出需要添加的包
    packages_to_add = new_packages - existing_packages
    
    if not packages_to_add:
        print("drawableMapping.xml中所有包都已存在")
        return True
    
    # 添加新包
    for package in sorted(packages_to_add):
        item_elem = ET.Element('item')
        item_elem.set('package', package)
        root.append(item_elem)
    
    # 保存XML
    tree.write(drawable_xml, encoding='UTF-8', xml_declaration=True)
    
    print(f"✓ 已更新 {drawable_xml}")
    print(f"  添加了 {len(packages_to_add)} 个新包")
    
    return True


def main():
    """主函数"""
    print("="*60)
    print("更新theme目录中的XML文件")
    print("="*60)
    print()
    
    # 更新allApps.xml
    print("更新 allApps.xml...")
    update_allapps_xml()
    print()
    
    # 更新drawableMapping.xml
    print("更新 drawableMapping.xml...")
    update_drawablemapping_xml()
    print()
    
    print("="*60)
    print("完成！")
    print("="*60)


if __name__ == "__main__":
    main()

