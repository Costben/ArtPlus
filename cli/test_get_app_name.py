#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""测试获取应用显示名称"""

import subprocess
import sys
from pathlib import Path

# 添加src目录到路径
script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))

from adb_helper import get_adb_path

def test_get_app_name(package_name: str):
    """测试获取应用名称的多种方法"""
    adb_path = get_adb_path()
    
    print(f"测试包名: {package_name}\n")
    
    # 方法1: pm dump
    print("方法1: pm dump")
    try:
        cmd = [adb_path, "shell", "pm", "dump", package_name]
        result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8', errors='ignore', timeout=5)
        if result.returncode == 0 and result.stdout:
            for line in result.stdout.split('\n'):
                if 'label' in line.lower() or 'name' in line.lower():
                    print(f"  {line[:100]}")
    except Exception as e:
        print(f"  错误: {e}")
    
    print("\n方法2: dumpsys package")
    try:
        cmd = [adb_path, "shell", "dumpsys", "package", package_name]
        result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8', errors='ignore', timeout=5)
        if result.returncode == 0 and result.stdout:
            for line in result.stdout.split('\n'):
                if 'label' in line.lower() or 'applicationLabel' in line.lower():
                    print(f"  {line[:100]}")
    except Exception as e:
        print(f"  错误: {e}")
    
    print("\n方法3: pm list packages -3")
    try:
        cmd = [adb_path, "shell", "pm", "list", "packages", "-3", "-f"]
        result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8', errors='ignore', timeout=5)
        if result.returncode == 0 and result.stdout:
            for line in result.stdout.split('\n'):
                if package_name in line:
                    print(f"  {line[:100]}")
    except Exception as e:
        print(f"  错误: {e}")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        test_get_app_name(sys.argv[1])
    else:
        # 测试几个常见的应用
        test_apps = ["com.catchingnow.icebox", "bin.mt.plus", "com.didjdk.adbhelper"]
        for app in test_apps:
            test_get_app_name(app)
            print("\n" + "="*60 + "\n")
