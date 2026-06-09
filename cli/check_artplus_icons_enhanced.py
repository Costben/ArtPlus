#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ART+图标缺失检测工具（增强版）
支持从adb获取应用列表，也支持从本地文件读取应用列表
"""

import os
import subprocess
import sys
import json
from pathlib import Path
from typing import List, Set, Tuple, Optional
from datetime import datetime

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
            artplus_path1: 第一个ART+图标目录路径
            artplus_path2: 第二个ART+图标目录路径
            adb_path: adb命令路径（如果为None，使用项目内的adb）
        """
        self.artplus_path1 = Path(artplus_path1)
        self.artplus_path2 = Path(artplus_path2)
        self.adb_path = adb_path if adb_path else get_adb_path()
        
    def get_user_apps_from_adb(self) -> List[Tuple[str, str]]:
        """
        通过adb获取手机上所有用户应用的包名和应用名称
        
        Returns:
            List[Tuple[包名, 应用名称]]
        """
        print("正在通过adb获取手机上的用户应用列表...")
        
        try:
            # 检查adb连接
            check_cmd = [self.adb_path, "devices"]
            check_result = subprocess.run(check_cmd, capture_output=True, text=True, encoding='utf-8', timeout=5)
            
            if "device" not in check_result.stdout or "no devices" in check_result.stdout.lower():
                print("错误: 未检测到已连接的设备")
                return []
            
            # 获取所有用户应用（-3 表示第三方应用）
            cmd = [self.adb_path, "shell", "pm", "list", "packages", "-3"]
            result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8', timeout=10)
            
            if result.returncode != 0:
                print(f"错误: 无法执行adb命令")
                print(f"错误信息: {result.stderr}")
                return []
            
            packages = []
            for line in result.stdout.strip().split('\n'):
                if line.startswith('package:'):
                    package_name = line.replace('package:', '').strip()
                    if package_name:
                        # 获取应用名称
                        app_name = self.get_app_name(package_name)
                        packages.append((package_name, app_name))
            
            print(f"✓ 找到 {len(packages)} 个用户应用")
            return packages
            
        except FileNotFoundError:
            print(f"错误: 找不到adb命令，请确保adb已添加到系统PATH中")
            return []
        except subprocess.TimeoutExpired:
            print("错误: adb命令执行超时")
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
            # 方法1: 使用dumpsys package获取
            cmd = [self.adb_path, "shell", "dumpsys", "package", package_name]
            result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8', timeout=3)
            
            if result.returncode == 0:
                for line in result.stdout.split('\n'):
                    if 'applicationLabel' in line:
                        # 提取应用名称
                        if '=' in line:
                            name = line.split('=')[-1].strip()
                            if name and name != package_name:
                                return name
        except:
            pass
        
        # 如果获取失败，返回包名
        return package_name
    
    def load_apps_from_file(self, file_path: str) -> List[Tuple[str, str]]:
        """
        从本地文件加载应用列表
        支持格式：
        1. 每行一个包名
        2. JSON格式: [{"package": "xxx", "name": "xxx"}, ...]
        3. 每行格式: 包名|应用名称
        
        Args:
            file_path: 文件路径
            
        Returns:
            List[Tuple[包名, 应用名称]]
        """
        file_path = Path(file_path)
        if not file_path.exists():
            print(f"错误: 文件不存在: {file_path}")
            return []
        
        apps = []
        
        try:
            # 尝试JSON格式
            if file_path.suffix == '.json':
                with open(file_path, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                    if isinstance(data, list):
                        for item in data:
                            if isinstance(item, dict):
                                package = item.get('package', '')
                                name = item.get('name', package)
                                if package:
                                    apps.append((package, name))
                        print(f"✓ 从JSON文件加载了 {len(apps)} 个应用")
                        return apps
            
            # 尝试文本格式
            with open(file_path, 'r', encoding='utf-8') as f:
                for line in f:
                    line = line.strip()
                    if not line or line.startswith('#'):
                        continue
                    
                    # 检查是否是 包名|应用名称 格式
                    if '|' in line:
                        parts = line.split('|', 1)
                        package = parts[0].strip()
                        name = parts[1].strip() if len(parts) > 1 else package
                        if package:
                            apps.append((package, name))
                    else:
                        # 纯包名格式
                        if line:
                            apps.append((line, line))
            
            print(f"✓ 从文件加载了 {len(apps)} 个应用")
            return apps
            
        except Exception as e:
            print(f"错误: 读取文件时发生异常: {e}")
            return []
    
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
            count1 = 0
            for item in self.artplus_path1.iterdir():
                if item.is_dir() and not item.name.startswith('icons-'):
                    packages.add(item.name)
                    count1 += 1
            print(f"  找到 {count1} 个应用")
        else:
            print(f"警告: 目录1不存在: {self.artplus_path1}")
        
        # 检查第二个目录
        if self.artplus_path2.exists():
            print(f"正在扫描目录2: {self.artplus_path2}")
            count2 = 0
            for item in self.artplus_path2.iterdir():
                if item.is_dir() and not item.name.startswith('icons-') and item.name not in ['choose', 'temp_u0']:
                    packages.add(item.name)
                    count2 += 1
            print(f"  找到 {count2} 个应用")
        else:
            print(f"警告: 目录2不存在: {self.artplus_path2}")
        
        print(f"✓ 总计找到 {len(packages)} 个已适配ART+图标的应用（去重后）")
        return packages
    
    def check_missing_icons(self, user_apps: Optional[List[Tuple[str, str]]] = None) -> Tuple[List[Tuple[str, str]], dict]:
        """
        检查哪些用户应用缺少ART+图标
        
        Args:
            user_apps: 用户应用列表，如果为None则从adb获取
            
        Returns:
            Tuple[缺失应用列表, 统计信息字典]
        """
        print("\n" + "="*60)
        print("开始检查ART+图标缺失情况...")
        print("="*60 + "\n")
        
        # 获取用户应用列表
        if user_apps is None:
            user_apps = self.get_user_apps_from_adb()
        
        if not user_apps:
            print("未找到用户应用")
            return [], {}
        
        # 获取已适配的包名
        artplus_packages = self.get_artplus_packages()
        
        # 找出缺失的应用和已适配的应用
        missing_apps = []
        matched_apps = []
        for package_name, app_name in user_apps:
            if package_name not in artplus_packages:
                missing_apps.append((package_name, app_name))
            else:
                matched_apps.append((package_name, app_name))
        
        # 统计信息
        stats = {
            "total_user_apps": len(user_apps),
            "matched_apps": len(matched_apps),
            "missing_apps": len(missing_apps),
            "coverage_rate": (len(matched_apps) / len(user_apps) * 100) if user_apps else 0
        }
        
        return missing_apps, stats
    
    def save_results(self, missing_apps: List[Tuple[str, str]], stats: dict, output_file: Optional[str] = None):
        """
        保存结果到文件
        
        Args:
            missing_apps: 缺少ART+图标的应用列表
            stats: 统计信息
            output_file: 输出文件名
        """
        if output_file is None:
            output_path = get_report_path("missing_artplus_icons.txt")
        else:
            output_path = Path(output_file)
            if not output_path.is_absolute() and output_path.parent == Path("."):
                output_path = get_report_path(output_path.name)

        ensure_dir(output_path.parent)
        
        # 保存为文本格式
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write("ART+图标缺失检测报告\n")
            f.write("="*60 + "\n\n")
            f.write("统计信息:\n")
            f.write(f"  用户应用总数: {stats.get('total_user_apps', 0)}\n")
            f.write(f"  已适配应用数: {stats.get('matched_apps', 0)}\n")
            f.write(f"  缺失应用数: {stats.get('missing_apps', 0)}\n")
            f.write(f"  适配覆盖率: {stats.get('coverage_rate', 0):.2f}%\n\n")
            f.write("="*60 + "\n\n")
            f.write("缺少ART+图标的应用列表:\n\n")
            
            for i, (package_name, app_name) in enumerate(missing_apps, 1):
                f.write(f"{i}. {app_name}\n")
                f.write(f"   包名: {package_name}\n\n")
        
        # 同时保存为JSON格式
        json_path = output_path.with_suffix('.json')
        with open(json_path, 'w', encoding='utf-8') as f:
            json.dump({
                "stats": stats,
                "missing_apps": [
                    {"package": pkg, "name": name} 
                    for pkg, name in missing_apps
                ]
            }, f, ensure_ascii=False, indent=2)
        
        # 生成HTML报告
        html_path = output_path.with_suffix('.html')
        self.save_html_report(missing_apps, stats, html_path)
        
        print(f"\n结果已保存到:")
        print(f"  - {output_path}")
        print(f"  - {json_path}")
        print(f"  - {html_path}")
    
    def save_html_report(self, missing_apps: List[Tuple[str, str]], stats: dict, output_path: Path):
        """
        生成HTML格式的报告
        
        Args:
            missing_apps: 缺少ART+图标的应用列表
            stats: 统计信息
            output_path: 输出文件路径
        """
        coverage_rate = stats.get('coverage_rate', 0)
        coverage_color = '#4CAF50' if coverage_rate >= 80 else '#FF9800' if coverage_rate >= 50 else '#F44336'
        
        html_content = f"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ART+图标缺失检测报告</title>
    <style>
        body {{
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
        }}
        .header {{
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            border-radius: 10px;
            margin-bottom: 20px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }}
        .stats {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }}
        .stat-card {{
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }}
        .stat-card h3 {{
            margin: 0 0 10px 0;
            color: #666;
            font-size: 14px;
            font-weight: normal;
        }}
        .stat-card .value {{
            font-size: 32px;
            font-weight: bold;
            color: #333;
        }}
        .coverage {{
            color: {coverage_color};
        }}
        .app-list {{
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }}
        .app-item {{
            padding: 15px;
            border-bottom: 1px solid #eee;
        }}
        .app-item:last-child {{
            border-bottom: none;
        }}
        .app-name {{
            font-size: 16px;
            font-weight: 500;
            color: #333;
            margin-bottom: 5px;
        }}
        .app-package {{
            font-size: 12px;
            color: #999;
            font-family: monospace;
        }}
        .no-missing {{
            text-align: center;
            padding: 40px;
            color: #4CAF50;
            font-size: 18px;
        }}
    </style>
</head>
<body>
    <div class="header">
        <h1>ART+图标缺失检测报告</h1>
        <p>生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</p>
    </div>
    
    <div class="stats">
        <div class="stat-card">
            <h3>用户应用总数</h3>
            <div class="value">{stats.get('total_user_apps', 0)}</div>
        </div>
        <div class="stat-card">
            <h3>已适配应用</h3>
            <div class="value" style="color: #4CAF50;">{stats.get('matched_apps', 0)}</div>
        </div>
        <div class="stat-card">
            <h3>缺失应用</h3>
            <div class="value" style="color: #F44336;">{stats.get('missing_apps', 0)}</div>
        </div>
        <div class="stat-card">
            <h3>适配覆盖率</h3>
            <div class="value coverage">{coverage_rate:.2f}%</div>
        </div>
    </div>
    
    <div class="app-list">
        <h2>缺少ART+图标的应用列表</h2>
        {"<div class='no-missing'>✓ 所有应用都已适配ART+图标！</div>" if not missing_apps else ""}
        {"".join([f'''
        <div class="app-item">
            <div class="app-name">{i}. {app_name}</div>
            <div class="app-package">{package_name}</div>
        </div>
        ''' for i, (package_name, app_name) in enumerate(missing_apps, 1)])}
    </div>
</body>
</html>"""
        
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(html_content)
    
    def print_results(self, missing_apps: List[Tuple[str, str]], stats: dict):
        """
        打印结果到控制台
        
        Args:
            missing_apps: 缺少ART+图标的应用列表
            stats: 统计信息
        """
        print("\n" + "="*60)
        print("检测结果统计")
        print("="*60)
        print(f"用户应用总数: {stats.get('total_user_apps', 0)}")
        print(f"已适配应用数: {stats.get('matched_apps', 0)}")
        print(f"缺失应用数: {stats.get('missing_apps', 0)}")
        print(f"适配覆盖率: {stats.get('coverage_rate', 0):.2f}%")
        
        print("\n" + "="*60)
        print("缺少ART+图标的应用列表")
        print("="*60)
        
        if not missing_apps:
            print("\n✓ 所有用户应用都已适配ART+图标！")
            return
        
        print(f"\n总计: {len(missing_apps)} 个应用\n")
        for i, (package_name, app_name) in enumerate(missing_apps, 1):
            print(f"{i}. {app_name}")
            print(f"   包名: {package_name}\n")


def main():
    """主函数"""
    print("ART+图标缺失检测工具（增强版）")
    print("="*60)
    
    checker = ArtPlusIconChecker()
    
    # 询问用户选择方式
    print("\n请选择获取应用列表的方式:")
    print("1. 通过adb从手机获取（需要连接手机）")
    print("2. 从本地文件读取")
    print("3. 仅检查本地ART+图标目录（不检查应用列表）")
    
    choice = input("\n请输入选项 (1/2/3): ").strip()
    
    user_apps = None
    
    if choice == "1":
        user_apps = checker.get_user_apps_from_adb()
    elif choice == "2":
        file_path = input("请输入应用列表文件路径: ").strip()
        if file_path:
            user_apps = checker.load_apps_from_file(file_path)
    elif choice == "3":
        print("仅显示已适配的ART+图标列表")
        artplus_packages = checker.get_artplus_packages()
        print(f"\n已适配的应用列表（共 {len(artplus_packages)} 个）:")
        for i, pkg in enumerate(sorted(artplus_packages), 1):
            print(f"{i}. {pkg}")
        return
    else:
        print("无效选项")
        return
    
    if not user_apps:
        print("未获取到应用列表，退出")
        return
    
    # 检查缺失的图标
    missing_apps, stats = checker.check_missing_icons(user_apps)
    
    # 打印结果
    checker.print_results(missing_apps, stats)
    
    # 保存结果
    checker.save_results(missing_apps, stats)
    
    print("\n检查完成！")


if __name__ == "__main__":
    main()
