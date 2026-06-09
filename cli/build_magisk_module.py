#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
构建Magisk模块ZIP包
将theme目录和uxicons目录复制到模块中
"""

import sys
import io
import shutil
import zipfile
from pathlib import Path
from datetime import datetime

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


def build_module():
    """构建Magisk模块ZIP包（仅包含XML文件，不包含图标素材）"""
    project_root = get_path(".")
    module_dir = project_root / "module" / "ARTPlus_Theme_Module"
    theme_dir = project_root / "theme"
    
    if not module_dir.exists():
        print(f"错误: 模块目录不存在: {module_dir}")
        return False
    
    if not theme_dir.exists():
        print(f"错误: theme目录不存在: {theme_dir}")
        return False
    
    print("="*60)
    print("构建Magisk模块")
    print("="*60)
    print()
    
    # 创建临时目录用于打包
    temp_dir = project_root / "module" / "ARTPlus_Theme_Module_temp"
    if temp_dir.exists():
        shutil.rmtree(temp_dir)
    temp_dir.mkdir(parents=True)
    
    print("1. 复制模块文件...")
    # 复制模块基础文件
    for file in module_dir.iterdir():
        if file.is_file():
            shutil.copy2(file, temp_dir / file.name)
        elif file.is_dir() and file.name != "system":
            shutil.copytree(file, temp_dir / file.name, dirs_exist_ok=True)
    
    # 创建theme目录（用于customize.sh复制）
    temp_theme_dir = temp_dir / "theme"
    temp_theme_dir.mkdir()
    
    print("2. 复制theme目录...")
    for file in theme_dir.iterdir():
        if file.is_file() and file.suffix == '.xml':
            shutil.copy2(file, temp_theme_dir / file.name)
    
    print("3. 创建system目录结构...")
    # 创建system/etc/theme目录
    system_theme_dir = temp_dir / "system" / "etc" / "theme"
    system_theme_dir.mkdir(parents=True)
    
    # 复制theme XML文件到system目录
    xml_count = 0
    for xml_file in theme_dir.glob("*.xml"):
        shutil.copy2(xml_file, system_theme_dir / xml_file.name)
        xml_count += 1
    
    print(f"  ✓ 已复制 {xml_count} 个XML文件")
    
    print("4. 跳过图标文件（图标需手动添加到/data/oplus/uxicons/）")
    
    print()
    print("6. 创建ZIP包...")
    # 创建ZIP包
    zip_path = project_root / "module" / "ARTPlus_Theme_Module.zip"
    if zip_path.exists():
        zip_path.unlink()
    
    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for file in temp_dir.rglob('*'):
            if file.is_file():
                arcname = file.relative_to(temp_dir)
                zipf.write(file, arcname)
    
    # 清理临时目录
    shutil.rmtree(temp_dir)
    
    zip_size = zip_path.stat().st_size / (1024 * 1024)  # MB
    print(f"✓ 模块已构建: {zip_path}")
    print(f"  大小: {zip_size:.2f} MB")
    print()
    print("="*60)
    print("构建完成！")
    print("="*60)
    print()
    print("使用方法:")
    print("1. 将 ARTPlus_Theme_Module.zip 传输到手机")
    print("2. 在Magisk Manager中安装该模块")
    print("3. 重启手机")
    print()
    print("模块功能:")
    print("- 更新 /system/etc/theme/allApps.xml")
    print("- 更新 /system/etc/theme/drawableMapping.xml")
    print("- 更新 /system/etc/theme/icon_version.xml")
    print("- 更新 /system/etc/theme/themeInfo.xml")
    print()
    print("注意:")
    print("- 模块仅包含XML配置文件")
    print("- 图标素材需手动添加到 /data/oplus/uxicons/ 目录")
    
    return True


if __name__ == "__main__":
    build_module()

