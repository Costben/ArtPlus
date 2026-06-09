#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试从APK提取应用名称
"""

import sys
import subprocess
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

from adb_helper import get_adb_path
from project_helper import get_tests_dir, ensure_dir
from extract_missing_apks import get_app_display_name, get_apk_path


def test_extract_app_name(package_name: str):
    """测试提取应用名称"""
    print("="*60)
    print(f"测试提取应用名称: {package_name}")
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
    
    # 创建tests文件夹
    test_dir = get_tests_dir()
    ensure_dir(test_dir)
    
    # 获取APK路径
    print(f"\n1. 获取APK路径...")
    apk_path = get_apk_path(adb_path, package_name)
    if not apk_path:
        print(f"   错误: 无法获取APK路径")
        return
    print(f"   APK路径: {apk_path}")
    
    # 拉取APK到tests文件夹
    print(f"\n2. 拉取APK到tests文件夹...")
    local_apk = test_dir / f"{package_name}.apk"
    try:
        cmd = [adb_path, "pull", apk_path, str(local_apk)]
        result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8', errors='ignore', timeout=30)
        
        if result.returncode != 0 or not local_apk.exists():
            print(f"   错误: 拉取APK失败")
            if result.stderr:
                print(f"   错误信息: {result.stderr}")
            return
        
        print(f"   ✓ APK已保存到: {local_apk}")
        print(f"   文件大小: {local_apk.stat().st_size / 1024 / 1024:.2f} MB")
    except Exception as e:
        print(f"   错误: {e}")
        return
    
    # 测试提取应用名称
    print(f"\n3. 测试提取应用名称...")
    try:
        app_name = get_app_display_name(adb_path, package_name)
        print(f"   提取结果: {app_name}")
        
        if app_name == package_name:
            print(f"   ⚠ 提取的名称与包名相同，可能提取失败")
        else:
            print(f"   ✓ 成功提取应用名称: {app_name}")
    except Exception as e:
        print(f"   错误: {e}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    # 从命令行参数获取包名，如果没有则使用默认值
    if len(sys.argv) > 1:
        package_name = sys.argv[1]
    else:
        # 默认测试一个应用
        package_name = "com.catchingnow.np"
        print(f"未指定包名，使用默认值: {package_name}")
        print(f"用法: python {sys.argv[0]} <package_name>\n")
    
    test_extract_app_name(package_name)
