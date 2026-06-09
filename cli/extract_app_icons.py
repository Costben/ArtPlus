#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
通过adb批量提取应用的原始图标
从APK中提取图标资源（PNG、XML/SVG等）并保存到outputs/new_artplus对应目录
"""

import json
import subprocess
import zipfile
import shutil
import re
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import List, Dict, Optional, Set, Union
import tempfile
import os
import sys

# 添加src目录到路径，以便导入adb_helper
script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))

from adb_helper import get_adb_path
from project_helper import get_new_artplus_dir, get_temp_dir, get_report_path, ensure_dir


class AppIconExtractor:
    """应用图标提取器"""
    
    def __init__(
        self,
        adb_path: Optional[str] = None,
        output_base: Optional[Union[str, Path]] = None,
        temp_dir: Optional[Union[str, Path]] = None,
    ):
        """
        初始化提取器
        
        Args:
            adb_path: adb命令路径（如果为None，使用项目内的adb）
            output_base: 输出基础目录
            temp_dir: 临时APK存储目录
        """
        self.adb_path = adb_path if adb_path else get_adb_path()
        self.output_base = Path(output_base) if output_base else get_new_artplus_dir()
        temp_root = Path(temp_dir) if temp_dir else (get_temp_dir() / "apk")
        self.temp_dir = temp_root
        ensure_dir(self.temp_dir)
        
    def check_adb_connection(self) -> bool:
        """检查adb连接"""
        try:
            result = subprocess.run(
                [self.adb_path, "devices"],
                capture_output=True,
                text=True,
                timeout=5
            )
            return "device" in result.stdout and "no devices" not in result.stdout.lower()
        except:
            return False
    
    def get_apk_path(self, package_name: str) -> Optional[str]:
        """
        获取应用的APK路径
        
        Args:
            package_name: 应用包名
            
        Returns:
            APK路径，如果获取失败返回None
        """
        try:
            # 获取APK路径
            cmd = [self.adb_path, "shell", "pm", "path", package_name]
            result = subprocess.run(cmd, capture_output=True, text=True, timeout=10)
            
            if result.returncode == 0:
                for line in result.stdout.strip().split('\n'):
                    if line.startswith('package:'):
                        apk_path = line.replace('package:', '').strip()
                        return apk_path
        except Exception as e:
            print(f"  错误: 获取APK路径失败: {e}")
        
        return None
    
    def pull_apk(self, apk_path: str, package_name: str) -> Optional[Path]:
        """
        从手机拉取APK文件到本地
        
        Args:
            apk_path: 手机上的APK路径
            package_name: 应用包名
            
        Returns:
            本地APK文件路径，如果失败返回None
        """
        local_apk = self.temp_dir / f"{package_name}.apk"
        
        try:
            cmd = [self.adb_path, "pull", apk_path, str(local_apk)]
            result = subprocess.run(cmd, capture_output=True, text=True, timeout=30)
            
            if result.returncode == 0 and local_apk.exists():
                return local_apk
        except Exception as e:
            print(f"  错误: 拉取APK失败: {e}")
        
        return None
    
    
    def get_icon_name_from_aapt(self, apk_path: Path) -> Set[str]:
        """
        使用aapt工具从AndroidManifest.xml中获取图标资源名称
        
        Args:
            apk_path: APK文件路径
            
        Returns:
            图标资源名称集合
        """
        icon_names = set()
        
        try:
            # 尝试使用aapt工具
            cmd = ["aapt", "dump", "badging", str(apk_path)]
            result = subprocess.run(cmd, capture_output=True, text=True, timeout=10)
            
            if result.returncode == 0:
                # 解析aapt输出
                for line in result.stdout.split('\n'):
                    if 'application-icon' in line.lower() or 'icon=' in line.lower():
                        # 提取图标资源名称
                        match = re.search(r'icon=\'([^\']+)\'', line)
                        if match:
                            icon_name = match.group(1)
                            # 提取资源名称（去掉包名前缀）
                            if ':' in icon_name:
                                icon_name = icon_name.split(':')[-1]
                            icon_names.add(icon_name)
        except FileNotFoundError:
            # aapt工具不可用，跳过
            pass
        except Exception as e:
            # 忽略错误
            pass
        
        return icon_names
    
    def extract_icons_from_apk(self, apk_path: Path, package_name: str, output_dir: Path) -> List[str]:
        """
        从APK中提取图标资源
        
        Args:
            apk_path: APK文件路径
            package_name: 应用包名
            output_dir: 输出目录
            
        Returns:
            提取的文件列表
        """
        extracted_files = []
        
        # 尝试使用aapt获取图标名称
        icon_names = self.get_icon_name_from_aapt(apk_path)
        
        try:
            with zipfile.ZipFile(apk_path, 'r') as zip_ref:
                # 查找所有可能的图标文件
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
                    r'res/mipmap-.*/ic_launcher_adaptive_foreground\.png',
                    r'res/mipmap-.*/ic_launcher_adaptive_background\.png',
                    # 其他常见名称
                    r'res/drawable-.*/app_icon\.png',
                    r'res/drawable-.*/icon\.png',
                    r'res/drawable-.*/logo\.png',
                    r'res/mipmap-.*/app_icon\.png',
                    r'res/mipmap-.*/icon\.png',
                ]
                
                # 添加从aapt中获取的图标名称
                for icon_name in icon_names:
                    icon_patterns.extend([
                        rf'res/mipmap-.*/{re.escape(icon_name)}\.png',
                        rf'res/mipmap-.*/{re.escape(icon_name)}\.xml',
                        rf'res/drawable-.*/{re.escape(icon_name)}\.png',
                        rf'res/drawable-.*/{re.escape(icon_name)}\.xml',
                    ])
                
                # 匹配并提取文件
                matched_files = set()
                for file_info in zip_ref.namelist():
                    # 跳过非资源文件
                    if not file_info.startswith('res/'):
                        continue
                    
                    # 检查是否匹配图标模式
                    for pattern in icon_patterns:
                        if re.search(pattern, file_info, re.IGNORECASE):
                            matched_files.add(file_info)
                            break
                
                # 如果没有找到，尝试查找所有drawable和mipmap目录下的文件
                if not matched_files:
                    for file_info in zip_ref.namelist():
                        if not file_info.startswith('res/'):
                            continue
                        
                        # 检查是否在mipmap或drawable目录中
                        if ('mipmap' in file_info or 'drawable' in file_info):
                            # 检查文件扩展名
                            if file_info.endswith(('.png', '.xml', '.svg', '.webp')):
                                filename = os.path.basename(file_info)
                                # 检查文件名是否像图标
                                filename_lower = filename.lower()
                                if any(keyword in filename_lower for keyword in ['icon', 'launcher', 'logo', 'app', 'ic_']):
                                    matched_files.add(file_info)
                
                # 提取文件，按目录结构组织
                for file_path in sorted(matched_files):
                    try:
                        # 保持目录结构，但简化路径
                        # 例如: res/mipmap-hdpi/ic_launcher.png -> mipmap-hdpi_ic_launcher.png
                        path_parts = file_path.split('/')
                        if len(path_parts) >= 3:
                            # res/mipmap-xxx/icon.png -> mipmap-xxx_icon.png
                            dir_name = path_parts[1]  # mipmap-hdpi
                            file_name = path_parts[-1]  # ic_launcher.png
                            output_name = f"{dir_name}_{file_name}"
                        else:
                            output_name = os.path.basename(file_path)
                        
                        output_file = output_dir / output_name
                        
                        # 如果文件已存在，添加序号
                        if output_file.exists():
                            base_name = output_file.stem
                            ext = output_file.suffix
                            counter = 1
                            while output_file.exists():
                                output_file = output_dir / f"{base_name}_{counter}{ext}"
                                counter += 1
                        
                        # 提取文件
                        with zip_ref.open(file_path) as source:
                            with open(output_file, 'wb') as target:
                                shutil.copyfileobj(source, target)
                        
                        extracted_files.append(str(output_file.relative_to(self.output_base)))
                        print(f"    提取: {output_name}")
                    except Exception as e:
                        print(f"    警告: 提取文件失败 {file_path}: {e}")
        
        except Exception as e:
            print(f"  错误: 解压APK失败: {e}")
        
        return extracted_files
    
    def extract_app_icon(self, package_name: str, app_name: str) -> bool:
        """
        提取单个应用的图标
        
        Args:
            package_name: 应用包名
            app_name: 应用名称
            
        Returns:
            是否成功
        """
        print(f"\n处理: {app_name} ({package_name})")
        
        # 获取APK路径
        apk_path = self.get_apk_path(package_name)
        if not apk_path:
            print(f"  ✗ 无法获取APK路径")
            return False
        
        # 拉取APK
        local_apk = self.pull_apk(apk_path, package_name)
        if not local_apk:
            print(f"  ✗ 无法拉取APK")
            return False
        
        # 创建输出目录
        output_dir = self.output_base / package_name
        output_dir.mkdir(parents=True, exist_ok=True)
        
        # 提取图标
        extracted_files = self.extract_icons_from_apk(local_apk, package_name, output_dir)
        
        # 清理临时APK文件
        try:
            local_apk.unlink()
        except:
            pass
        
        if extracted_files:
            print(f"  ✓ 成功提取 {len(extracted_files)} 个图标文件")
            return True
        else:
            print(f"  ✗ 未找到图标文件")
            return False
    
    def batch_extract(self, missing_apps: List[Dict], skip_existing: bool = True):
        """
        批量提取应用图标
        
        Args:
            missing_apps: 缺失应用列表
            skip_existing: 是否跳过已有图标的目录
        """
        print("="*60)
        print("开始批量提取应用图标")
        print("="*60)
        
        if not self.check_adb_connection():
            print("错误: 未检测到adb连接，请检查设备连接")
            return
        
        total = len(missing_apps)
        success_count = 0
        skip_count = 0
        fail_count = 0
        
        for i, app in enumerate(missing_apps, 1):
            package_name = app.get('package', '')
            app_name = app.get('name', package_name)
            
            if not package_name:
                continue
            
            output_dir = self.output_base / package_name
            
            # 检查是否已有图标文件
            if skip_existing and output_dir.exists():
                existing_files = list(output_dir.glob('*.png')) + list(output_dir.glob('*.xml')) + list(output_dir.glob('*.svg'))
                if existing_files and any(f.name != 'README.txt' for f in existing_files):
                    print(f"\n[{i}/{total}] 跳过（已有图标）: {app_name}")
                    skip_count += 1
                    continue
            
            print(f"\n[{i}/{total}] ", end='')
            try:
                if self.extract_app_icon(package_name, app_name):
                    success_count += 1
                else:
                    fail_count += 1
            except KeyboardInterrupt:
                print("\n\n用户中断操作")
                break
            except Exception as e:
                print(f"  错误: 处理应用时发生异常: {e}")
                fail_count += 1
        
        # 清理临时目录
        try:
            if self.temp_dir.exists():
                shutil.rmtree(self.temp_dir)
        except:
            pass
        
        print("\n" + "="*60)
        print("提取完成！")
        print(f"  总计: {total} 个应用")
        print(f"  成功: {success_count} 个")
        print(f"  跳过: {skip_count} 个")
        print(f"  失败: {fail_count} 个")
        print("="*60)


def load_missing_apps(json_file: Optional[str] = None) -> List[Dict]:
    """加载缺失应用列表"""
    if json_file is None:
        json_path = get_report_path("missing_artplus_icons.json")
    else:
        json_path = Path(json_file)
    if not json_path.exists():
        print(f"错误: 文件不存在: {json_path}")
        return []
    
    try:
        with open(json_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
            return data.get('missing_apps', [])
    except Exception as e:
        print(f"错误: 读取文件时发生异常: {e}")
        return []


def main():
    """主函数"""
    print("应用图标批量提取工具")
    print("="*60)
    
    # 加载缺失应用列表
    missing_apps = load_missing_apps()
    
    if not missing_apps:
        print("未找到缺失应用列表，退出")
        return
    
    print(f"\n找到 {len(missing_apps)} 个缺失ART+图标的应用")
    
    # 创建提取器
    extractor = AppIconExtractor()
    
    # 询问是否跳过已有图标的目录
    print("\n是否跳过已有图标的目录？(y/n，默认y): ", end='')
    choice = input().strip().lower()
    skip_existing = choice != 'n'
    
    # 开始批量提取
    extractor.batch_extract(missing_apps, skip_existing=skip_existing)
    
    print(f"\n提取完成！图标文件已保存到 {extractor.output_base} 对应目录中。")


if __name__ == "__main__":
    main()
