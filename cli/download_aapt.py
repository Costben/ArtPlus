#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
下载aapt工具到platform_tools目录
"""

import sys
import io
import urllib.request
import zipfile
import tempfile
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


def download_aapt():
    """下载aapt工具"""
    print("="*60)
    print("下载aapt工具")
    print("="*60)
    
    platform_tools_dir = get_platform_tools_dir()
    
    # 根据操作系统选择aapt文件名
    if sys.platform == "win32":
        aapt_filename = "aapt.exe"
        platform_suffix = "windows"
    elif sys.platform == "darwin":
        aapt_filename = "aapt"
        platform_suffix = "darwin"
    else:
        aapt_filename = "aapt"
        platform_suffix = "linux"
    
    aapt_path = platform_tools_dir / aapt_filename
    
    if aapt_path.exists():
        print(f"\n✓ {aapt_filename}已存在: {aapt_path}")
        response = input("是否重新下载? (y/n): ").strip().lower()
        if response != 'y':
            print("取消下载")
            return
    
    # Android SDK Build Tools下载地址
    # 根据平台选择不同的下载链接
    if sys.platform == "win32":
        build_tools_versions = [
            ("33.0.2", "https://dl.google.com/android/repository/build-tools_r33.0.2-windows.zip"),
            ("33.0.1", "https://dl.google.com/android/repository/build-tools_r33.0.1-windows.zip"),
            ("33.0.0", "https://dl.google.com/android/repository/build-tools_r33.0.0-windows.zip"),
            ("32.0.0", "https://dl.google.com/android/repository/build-tools_r32.0.0-windows.zip"),
        ]
    elif sys.platform == "darwin":
        build_tools_versions = [
            ("33.0.2", "https://dl.google.com/android/repository/build-tools_r33.0.2-macosx.zip"),
            ("33.0.1", "https://dl.google.com/android/repository/build-tools_r33.0.1-macosx.zip"),
            ("33.0.0", "https://dl.google.com/android/repository/build-tools_r33.0.0-macosx.zip"),
            ("32.0.0", "https://dl.google.com/android/repository/build-tools_r32.0.0-macosx.zip"),
        ]
    else:
        build_tools_versions = [
            ("33.0.2", "https://dl.google.com/android/repository/build-tools_r33.0.2-linux.zip"),
            ("33.0.1", "https://dl.google.com/android/repository/build-tools_r33.0.1-linux.zip"),
            ("33.0.0", "https://dl.google.com/android/repository/build-tools_r33.0.0-linux.zip"),
            ("32.0.0", "https://dl.google.com/android/repository/build-tools_r32.0.0-linux.zip"),
        ]
    
    print(f"\n1. 尝试下载Android SDK Build Tools...")
    print(f"   目标目录: {platform_tools_dir}")
    
    # 创建临时目录
    with tempfile.TemporaryDirectory() as temp_dir:
        zip_path = Path(temp_dir) / "build-tools.zip"
        
        # 尝试多个版本
        download_success = False
        for build_tools_version, build_tools_url in build_tools_versions:
            print(f"\n   尝试版本 {build_tools_version}...")
            print(f"   下载地址: {build_tools_url}")
            
            try:
                print(f"\n2. 开始下载...")
                print(f"   这可能需要几分钟，请耐心等待...")
                
                # 下载文件
                def show_progress(block_num, block_size, total_size):
                    downloaded = block_num * block_size
                    percent = min(downloaded * 100 / total_size, 100)
                    print(f"\r   进度: {percent:.1f}% ({downloaded / 1024 / 1024:.1f} MB / {total_size / 1024 / 1024:.1f} MB)", end='', flush=True)
                
                urllib.request.urlretrieve(build_tools_url, zip_path, show_progress)
                print()  # 换行
                download_success = True
                break
            except urllib.error.HTTPError as e:
                if e.code == 404:
                    print(f"   版本 {build_tools_version} 不存在，尝试下一个...")
                    continue
                else:
                    raise
            except Exception as e:
                print(f"   下载失败: {e}")
                continue
        
        if not download_success:
            print(f"\n✗ 所有下载链接都失败")
            print(f"   请手动下载Android SDK Build Tools")
            print(f"   下载地址: https://developer.android.com/studio/releases/build-tools")
            print(f"   或使用Android SDK Manager下载")
            return
        
        try:
            
            print(f"\n3. 解压文件...")
            
            # 解压zip文件
            with zipfile.ZipFile(zip_path, 'r') as zip_ref:
                # 查找aapt文件（根据平台）
                aapt_in_zip = None
                for file_info in zip_ref.namelist():
                    if file_info.endswith(aapt_filename):
                        aapt_in_zip = file_info
                        break
                
                if not aapt_in_zip:
                    print(f"   错误: 在下载的zip文件中未找到{aapt_filename}")
                    return
                
                print(f"   找到{aapt_filename}: {aapt_in_zip}")
                
                # 提取aapt文件
                print(f"   提取到: {aapt_path}")
                with zip_ref.open(aapt_in_zip) as source:
                    with open(aapt_path, 'wb') as target:
                        target.write(source.read())
                
                # Mac/Linux下需要添加执行权限
                if sys.platform != "win32":
                    import os
                    os.chmod(aapt_path, 0o755)
            
            print(f"\n✓ 下载完成!")
            print(f"   {aapt_filename}已保存到: {aapt_path}")
            
            # 验证文件
            if aapt_path.exists():
                file_size = aapt_path.stat().st_size / 1024 / 1024
                print(f"   文件大小: {file_size:.2f} MB")
            else:
                print("   ⚠ 警告: 文件可能未正确保存")
                
        except urllib.error.URLError as e:
            print(f"\n✗ 下载失败: {e}")
            print(f"   请检查网络连接或手动下载")
            print(f"   下载地址: {build_tools_url}")
        except Exception as e:
            print(f"\n✗ 错误: {e}")
            import traceback
            traceback.print_exc()


if __name__ == "__main__":
    download_aapt()
