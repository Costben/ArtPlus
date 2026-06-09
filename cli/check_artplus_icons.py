#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ART+图标缺失检测工具
用于检测手机上已安装的用户应用中哪些缺少ART+图标适配
"""

import os
import subprocess
import sys
from pathlib import Path
from typing import List, Set, Tuple, Optional

# 添加src目录到路径，以便导入adb_helper
script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))

from adb_helper import get_adb_path
from project_helper import get_report_path, ensure_dir


class ArtPlusIconChecker:
    """ART+图标检查器"""
    
    def __init__(self, 
                 artplus_path1: str = "theme/uxicons/hdpi",
                 artplus_path2: str = "uxicons",
                 adb_path: Optional[str] = None):
        """
        初始化检查器
        
        Args:
            artplus_path1: 第一个ART+图标目录路径（/my_product/media/theme/uxicons/hdpi/）
            artplus_path2: 第二个ART+图标目录路径（/data/oplus/uxicons/）
            adb_path: adb命令路径（如果为None，使用项目内的adb）
        """
        self.artplus_path1 = Path(artplus_path1)
        self.artplus_path2 = Path(artplus_path2)
        self.adb_path = adb_path if adb_path else get_adb_path()
        
    def get_user_apps(self) -> List[Tuple[str, str]]:
        """
        通过adb获取手机上所有用户应用的包名和应用名称
        
        Returns:
            List[Tuple[包名, 应用名称]]
        """
        print("正在获取手机上的用户应用列表...")
        
        try:
            # 使用adb命令获取所有用户应用（排除系统应用）
            # -3 表示只显示第三方应用
            cmd = [self.adb_path, "shell", "pm", "list", "packages", "-3"]
            result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8')
            
            if result.returncode != 0:
                print(f"错误: 无法执行adb命令")
                print(f"错误信息: {result.stderr}")
                return []
            
            packages = []
            for line in result.stdout.strip().split('\n'):
                if line.startswith('package:'):
                    package_name = line.replace('package:', '').strip()
                    if package_name:
                        # 尝试获取应用名称
                        app_name = self.get_app_name(package_name)
                        packages.append((package_name, app_name))
            
            print(f"找到 {len(packages)} 个用户应用")
            return packages
            
        except FileNotFoundError:
            print(f"错误: 找不到adb命令，请确保adb已添加到系统PATH中")
            return []
        except Exception as e:
            print(f"错误: 获取应用列表时发生异常: {e}")
            return []
    
    def get_app_name(self, package_name: str) -> str:
        """
        获取应用的显示名称
        
        Args:
            package_name: 应用包名
            
        Returns:
            应用名称，如果获取失败则返回包名
        """
        try:
            cmd = [self.adb_path, "shell", "pm", "dump", package_name]
            result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8', timeout=5)
            
            if result.returncode == 0:
                for line in result.stdout.split('\n'):
                    if 'applicationLabel' in line or 'label=' in line:
                        # 尝试提取应用名称
                        parts = line.split('=')
                        if len(parts) > 1:
                            name = parts[-1].strip()
                            if name and name != package_name:
                                return name
        except:
            pass
        
        return package_name
    
    def get_artplus_packages(self) -> Set[str]:
        """
        获取两个ART+图标目录中所有已适配的包名
        
        Returns:
            已适配的包名集合
        """
        packages = set()
        
        # 检查第一个目录
        if self.artplus_path1.exists():
            print(f"正在扫描目录1: {self.artplus_path1}")
            for item in self.artplus_path1.iterdir():
                if item.is_dir() and not item.name.startswith('icons-'):
                    packages.add(item.name)
        
        # 检查第二个目录
        if self.artplus_path2.exists():
            print(f"正在扫描目录2: {self.artplus_path2}")
            for item in self.artplus_path2.iterdir():
                if item.is_dir() and not item.name.startswith('icons-') and item.name not in ['choose', 'temp_u0']:
                    packages.add(item.name)
        
        print(f"找到 {len(packages)} 个已适配ART+图标的应用")
        return packages
    
    def check_missing_icons(self) -> List[Tuple[str, str]]:
        """
        检查哪些用户应用缺少ART+图标
        
        Returns:
            List[Tuple[包名, 应用名称]] - 缺少ART+图标的应用列表
        """
        print("\n" + "="*60)
        print("开始检查ART+图标缺失情况...")
        print("="*60 + "\n")
        
        # 获取用户应用列表
        user_apps = self.get_user_apps()
        if not user_apps:
            print("未找到用户应用，请检查adb连接")
            return []
        
        # 获取已适配的包名
        artplus_packages = self.get_artplus_packages()
        
        # 找出缺失的应用
        missing_apps = []
        for package_name, app_name in user_apps:
            if package_name not in artplus_packages:
                missing_apps.append((package_name, app_name))
        
        return missing_apps
    
    def save_results(self, missing_apps: List[Tuple[str, str]], output_file: Optional[str] = None):
        """
        保存结果到文件
        
        Args:
            missing_apps: 缺少ART+图标的应用列表
            output_file: 输出文件名
        """
        if output_file is None:
            output_path = get_report_path("missing_artplus_icons.txt")
        else:
            output_path = Path(output_file)
            if not output_path.is_absolute() and output_path.parent == Path("."):
                output_path = get_report_path(output_path.name)

        ensure_dir(output_path.parent)

        with open(output_path, 'w', encoding='utf-8') as f:
            f.write("缺少ART+图标的应用列表\n")
            f.write("="*60 + "\n")
            f.write(f"总计: {len(missing_apps)} 个应用\n\n")
            
            for i, (package_name, app_name) in enumerate(missing_apps, 1):
                f.write(f"{i}. {app_name}\n")
                f.write(f"   包名: {package_name}\n\n")
        
        print(f"\n结果已保存到: {output_path}")
    
    def print_results(self, missing_apps: List[Tuple[str, str]]):
        """
        打印结果到控制台
        
        Args:
            missing_apps: 缺少ART+图标的应用列表
        """
        print("\n" + "="*60)
        print("缺少ART+图标的应用列表")
        print("="*60)
        print(f"总计: {len(missing_apps)} 个应用\n")
        
        if not missing_apps:
            print("✓ 所有用户应用都已适配ART+图标！")
            return
        
        for i, (package_name, app_name) in enumerate(missing_apps, 1):
            print(f"{i}. {app_name}")
            print(f"   包名: {package_name}\n")


def main():
    """主函数"""
    print("ART+图标缺失检测工具")
    print("="*60)
    
    # 检查adb连接
    try:
        adb_path = get_adb_path()
        result = subprocess.run([adb_path, "devices"], capture_output=True, text=True, timeout=5)
        if "device" not in result.stdout or "no devices" in result.stdout.lower():
            print("警告: 未检测到已连接的设备，请确保:")
            print("1. 手机已通过USB连接到电脑")
            print("2. 已开启USB调试")
            print("3. 已授权此电脑的调试权限")
            print("\n是否继续使用本地目录检查? (y/n): ", end='')
            choice = input().strip().lower()
            if choice != 'y':
                return
    except:
        print("警告: 无法检测adb连接，将仅使用本地目录检查")
    
    # 创建检查器
    checker = ArtPlusIconChecker()
    
    # 检查缺失的图标
    missing_apps = checker.check_missing_icons()
    
    # 打印结果
    checker.print_results(missing_apps)
    
    # 保存结果
    if missing_apps:
        checker.save_results(missing_apps)
    
    print("\n检查完成！")


if __name__ == "__main__":
    main()
