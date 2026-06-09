#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
调试aapt输出，查看APK中的字符串资源
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

from adb_helper import get_platform_tools_dir


def debug_aapt_output(apk_path: Path):
    """调试aapt输出"""
    print("="*60)
    print(f"调试aapt输出: {apk_path.name}")
    print("="*60)
    
    if not apk_path.exists():
        print(f"错误: APK文件不存在: {apk_path}")
        return
    
    # 获取aapt路径
    from adb_helper import get_aapt_path
    aapt_path = get_aapt_path()
    
    # 如果本地没有aapt，提示用户
    if not aapt_path:
        platform_tools_dir = get_platform_tools_dir()
        print(f"错误: aapt工具不存在")
        print(f"  本地路径: {platform_tools_dir / ('aapt.exe' if sys.platform == 'win32' else 'aapt')}")
        print(f"  系统PATH: 未找到")
        print(f"\n请下载aapt工具并放到platform_tools目录，或添加到系统PATH")
        print(f"下载地址: https://developer.android.com/studio/releases/build-tools")
        return
    
    print(f"\n1. 使用 aapt dump strings...")
    try:
        cmd = [str(aapt_path), "dump", "strings", str(apk_path)]
        result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8', errors='ignore', timeout=10)
        
        if result.returncode == 0:
            output = result.stdout
            print(f"   输出长度: {len(output)} 字符")
            
            # 查找包含app_name的行
            print(f"\n2. 查找包含'app_name'的内容...")
            lines = output.split('\n')
            found_app_name = False
            
            for i, line in enumerate(lines):
                if 'app_name' in line.lower():
                    found_app_name = True
                    print(f"\n   找到app_name相关行 {i}:")
                    print(f"   {line}")
                    
                    # 显示后续几行
                    for j in range(i+1, min(i+10, len(lines))):
                        next_line = lines[j]
                        if next_line.strip():
                            print(f"   {next_line}")
                            # 如果遇到下一个String定义，停止
                            if next_line.strip().startswith('String #') and 'app_name' not in next_line.lower():
                                break
            
            if not found_app_name:
                print("   ⚠ 未找到app_name")
                
                # 显示前100行，看看输出格式
                print(f"\n3. 输出前100行（查看格式）...")
                for i, line in enumerate(lines[:100]):
                    if line.strip():
                        print(f"   {i}: {line[:80]}")
        else:
            print(f"   错误: aapt dump strings失败")
            print(f"   返回码: {result.returncode}")
            if result.stderr:
                print(f"   错误信息: {result.stderr}")
    except Exception as e:
        print(f"   错误: {e}")
        import traceback
        traceback.print_exc()
    
    print(f"\n4. 使用 aapt dump resources...")
    try:
        cmd = [str(aapt_path), "dump", "resources", str(apk_path)]
        result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8', errors='ignore', timeout=10)
        
        if result.returncode == 0:
            output = result.stdout
            print(f"   输出长度: {len(output)} 字符")
            
            # 查找包含app_name的行
            print(f"\n5. 查找包含'app_name'的内容...")
            lines = output.split('\n')
            found_app_name = False
            
            for i, line in enumerate(lines):
                if 'app_name' in line.lower():
                    found_app_name = True
                    print(f"\n   找到app_name相关行 {i}:")
                    print(f"   {line}")
                    
                    # 显示后续几行
                    for j in range(i+1, min(i+10, len(lines))):
                        next_line = lines[j]
                        if next_line.strip():
                            print(f"   {next_line}")
                            # 如果遇到下一个resource定义，停止
                            if next_line.strip().startswith('resource ') and 'app_name' not in next_line.lower():
                                break
            
            if not found_app_name:
                print("   ⚠ 未找到app_name")
        else:
            print(f"   错误: aapt dump resources失败")
            print(f"   返回码: {result.returncode}")
            if result.stderr:
                print(f"   错误信息: {result.stderr}")
    except Exception as e:
        print(f"   错误: {e}")


if __name__ == "__main__":
    # 从命令行参数获取APK路径，如果没有则使用test文件夹中的APK
    if len(sys.argv) > 1:
        apk_path = Path(sys.argv[1])
    else:
        # 使用tests文件夹中的APK
        from project_helper import get_tests_dir
        test_dir = get_tests_dir()
        apk_files = list(test_dir.glob("*.apk"))
        if apk_files:
            apk_path = apk_files[0]
            print(f"使用test文件夹中的APK: {apk_path.name}\n")
        else:
            print("错误: test文件夹中没有APK文件")
            print(f"用法: python {sys.argv[0]} <apk_path>")
            sys.exit(1)
    
    debug_aapt_output(apk_path)
