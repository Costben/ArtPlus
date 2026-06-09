#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试脚本：使用aapt从APK中提取图标
在test文件夹中测试提取图标功能
"""

import sys
import io
import subprocess
import zipfile
import re
import shutil
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
from adb_helper import get_aapt_path


def get_icon_name_from_aapt(apk_path: Path) -> set:
    """
    使用aapt获取APK中的图标资源名称
    
    Returns:
        图标资源名称集合
    """
    aapt_path = get_aapt_path()
    if not aapt_path:
        print("警告: 未找到aapt工具，将使用文件名模式匹配")
        return set()
    
    icon_names = set()
    
    try:
        # 使用aapt dump badging获取应用信息
        cmd = [str(aapt_path), "dump", "badging", str(apk_path)]
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            encoding='utf-8',
            errors='ignore',
            timeout=10
        )
        
        if result.returncode == 0 and result.stdout:
            # 查找application-icon标签
            # 格式: application-icon-120:'res/mipmap-hdpi/ic_launcher.png'
            icon_pattern = r"application-icon-\d+:\s*['\"]([^'\"]+)['\"]"
            matches = re.findall(icon_pattern, result.stdout)
            for match in matches:
                # 提取文件名（不含路径）
                icon_name = Path(match).name
                icon_names.add(icon_name)
                # 也添加完整路径
                icon_names.add(match)
        
        # 使用aapt dump resources获取所有资源
        cmd = [str(aapt_path), "dump", "resources", str(apk_path)]
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            encoding='utf-8',
            errors='ignore',
            timeout=10
        )
        
        if result.returncode == 0 and result.stdout:
            # 查找mipmap资源中的图标
            # 格式: resource 0x7f0a0000 mipmap/ic_launcher: ...
            icon_patterns = [
                r"mipmap/(ic_launcher[^:\s]*)",
                r"mipmap/(ic_launcher_[^:\s]*)",
                r"drawable/(ic_launcher[^:\s]*)",
                r"drawable/(ic_launcher_[^:\s]*)",
            ]
            
            for pattern in icon_patterns:
                matches = re.findall(pattern, result.stdout)
                for match in matches:
                    icon_names.add(match)
    
    except Exception as e:
        print(f"警告: aapt执行失败: {e}")
    
    return icon_names


def extract_icons_from_apk(apk_path: Path, output_dir: Path):
    """
    从APK中提取图标资源
    
    Args:
        apk_path: APK文件路径
        output_dir: 输出目录
    """
    output_dir.mkdir(parents=True, exist_ok=True)
    
    print(f"处理APK: {apk_path.name}")
    print(f"输出目录: {output_dir}\n")
    
    # 使用aapt获取图标名称
    icon_names = get_icon_name_from_aapt(apk_path)
    
    if icon_names:
        print(f"使用aapt找到 {len(icon_names)} 个图标资源:")
        for name in sorted(icon_names):
            print(f"  - {name}")
        print()
    
    # 图标文件模式
    icon_patterns = [
        # 标准图标
        r'res/mipmap-.*/ic_launcher\.png',
        r'res/mipmap-.*/ic_launcher\.xml',
        r'res/drawable-.*/ic_launcher\.png',
        r'res/drawable-.*/ic_launcher\.xml',
        # 分层图标
        r'res/mipmap-.*/ic_launcher_foreground\.png',
        r'res/mipmap-.*/ic_launcher_background\.png',
        r'res/mipmap-.*/ic_launcher_foreground\.xml',
        r'res/mipmap-.*/ic_launcher_background\.xml',
        # 其他变体
        r'res/mipmap-.*/ic_launcher_round\.png',
        r'res/mipmap-.*/ic_launcher_round\.xml',
        r'res/mipmap-.*/ic_launcher_monochrome\.png',
        r'res/mipmap-.*/ic_launcher_monochrome\.xml',
        # 其他常见名称
        r'res/drawable-.*/app_icon\.png',
        r'res/drawable-.*/icon\.png',
        r'res/mipmap-.*/app_icon\.png',
        r'res/mipmap-.*/icon\.png',
    ]
    
    # 添加从aapt获取的图标路径
    for icon_name in icon_names:
        if '/' in icon_name:
            # 完整路径，直接使用
            icon_patterns.append(re.escape(icon_name))
        else:
            # 只有文件名，添加到模式中
            icon_patterns.append(rf'res/.*/{re.escape(icon_name)}')
    
    extracted_files = []
    
    try:
        with zipfile.ZipFile(apk_path, 'r') as zip_ref:
            all_files = zip_ref.namelist()
            
            # 查找匹配的图标文件
            matched_files = []
            for file_path in all_files:
                for pattern in icon_patterns:
                    if re.match(pattern, file_path):
                        matched_files.append(file_path)
                        break
            
            if not matched_files:
                print("未找到匹配的图标文件")
                return
            
            print(f"找到 {len(matched_files)} 个图标文件:\n")
            
            for file_path in matched_files:
                # 生成输出文件名
                path_parts = Path(file_path).parts
                # 提取密度和文件名
                density = None
                filename = None
                
                for part in path_parts:
                    if 'mipmap-' in part or 'drawable-' in part:
                        density = part
                    if part.endswith('.png') or part.endswith('.xml'):
                        filename = part
                
                if density and filename:
                    output_name = f"{density}_{filename}"
                else:
                    output_name = Path(file_path).name
                
                # 如果文件已存在，添加序号
                output_path = output_dir / output_name
                counter = 1
                while output_path.exists():
                    stem = output_path.stem
                    suffix = output_path.suffix
                    output_path = output_dir / f"{stem}_{counter}{suffix}"
                    counter += 1
                
                # 提取文件
                try:
                    with zip_ref.open(file_path) as source:
                        with open(output_path, 'wb') as target:
                            target.write(source.read())
                    
                    file_size = output_path.stat().st_size
                    print(f"  ✓ 提取: {output_name} ({file_size:,} bytes)")
                    extracted_files.append(str(output_path))
                except Exception as e:
                    print(f"  ✗ 失败: {file_path}: {e}")
    
    except Exception as e:
        print(f"错误: 无法打开APK文件: {e}")
        return
    
    print(f"\n总计提取: {len(extracted_files)} 个文件")
    return extracted_files


def main():
    """主函数"""
    test_dir = get_path("test")
    
    if not test_dir.exists():
        print(f"错误: test目录不存在: {test_dir}")
        return
    
    # 查找所有APK文件
    apk_files = list(test_dir.glob("*.apk"))
    
    if not apk_files:
        print(f"错误: test目录中未找到APK文件")
        print(f"请将APK文件放到: {test_dir}")
        return
    
    print("="*60)
    print("测试：从APK提取图标")
    print("="*60)
    print()
    
    # 检查aapt工具
    aapt_path = get_aapt_path()
    if aapt_path:
        print(f"✓ 找到aapt工具: {aapt_path}")
    else:
        print("⚠ 未找到aapt工具，将使用文件名模式匹配")
    print()
    
    for apk_file in apk_files:
        # 创建输出目录（使用APK文件名）
        output_dir = test_dir / apk_file.stem
        extract_icons_from_apk(apk_file, output_dir)
        print()
    
    print("="*60)
    print("测试完成！")
    print("="*60)


if __name__ == "__main__":
    main()
