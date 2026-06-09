#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ADB工具路径辅助模块
统一管理项目内的adb路径
"""

import os
import sys
from pathlib import Path


def get_adb_path() -> str:
    """
    获取项目内的adb路径
    
    Returns:
        adb可执行文件的路径
    """
    # 获取脚本所在目录（src目录）
    script_dir = Path(__file__).parent.absolute()
    
    # 获取项目根目录（scripts的父目录）
    project_root = script_dir.parent.absolute()
    
    # 构建platform_tools目录路径（在项目根目录下）
    platform_tools_dir = project_root / "platform_tools"
    
    # 根据操作系统选择adb可执行文件
    if sys.platform == "win32":
        adb_exe = platform_tools_dir / "adb.exe"
    else:
        adb_exe = platform_tools_dir / "adb"
    
    # 检查文件是否存在
    if adb_exe.exists():
        return str(adb_exe)
    
    # 如果项目内不存在，回退到系统PATH中的adb
    return "adb"


def get_platform_tools_dir() -> Path:
    """
    获取platform_tools目录路径
    
    Returns:
        platform_tools目录的Path对象
    """
    script_dir = Path(__file__).parent.absolute()
    project_root = script_dir.parent.absolute()
    return project_root / "platform_tools"


def get_aapt_path() -> Path | None:
    """
    获取aapt工具路径（跨平台支持）
    
    Returns:
        aapt可执行文件的Path对象，如果不存在则返回None
    """
    import shutil
    
    platform_tools_dir = get_platform_tools_dir()
    
    # 根据操作系统选择aapt可执行文件
    if sys.platform == "win32":
        aapt_exe = platform_tools_dir / "aapt.exe"
    else:
        aapt_exe = platform_tools_dir / "aapt"
    
    # 检查文件是否存在
    if aapt_exe.exists():
        return aapt_exe
    
    # 如果项目内不存在，尝试从系统PATH查找
    aapt_system = shutil.which("aapt")
    if aapt_system:
        return Path(aapt_system)
    
    # Windows下也尝试查找aapt.exe
    if sys.platform == "win32":
        aapt_system = shutil.which("aapt.exe")
        if aapt_system:
            return Path(aapt_system)
    
    return None


if __name__ == "__main__":
    # 测试
    print(f"ADB路径: {get_adb_path()}")
    print(f"Platform Tools目录: {get_platform_tools_dir()}")
    aapt_path = get_aapt_path()
    if aapt_path:
        print(f"AAPT路径: {aapt_path}")
    else:
        print("AAPT路径: 未找到")
