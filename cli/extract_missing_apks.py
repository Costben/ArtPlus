#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
拉取缺少PNG图标的应用APK并直接提取图标
使用aapt工具识别图标资源并提取，输出到outputs/new_artplus
"""

import json
import subprocess
import re
import tempfile
import zipfile
import shutil
from pathlib import Path
from typing import List, Dict, Set
import sys
import io

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
from project_helper import get_report_path, get_new_artplus_dir


def load_missing_apps(json_file: str = None) -> List[Dict]:
    """加载缺失应用列表"""
    if json_file is None:
        json_file = get_report_path("missing_artplus_icons.json")
    else:
        json_file = Path(json_file)
    
    if not json_file.exists():
        print(f"错误: 文件不存在: {json_file}")
        return []
    
    try:
        with open(json_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
            return data.get('missing_apps', [])
    except Exception as e:
        print(f"错误: 读取文件时发生异常: {e}")
        return []


def check_app_has_png_icon(package_name: str, base_dir: Path) -> bool:
    """
    检查应用是否有PNG图标（至少有一个recfg.png或其他ART+图标）
    
    Args:
        package_name: 应用包名
        base_dir: outputs/new_artplus目录
        
    Returns:
        是否有PNG图标
    """
    app_dir = base_dir / package_name
    if not app_dir.exists():
        return False
    
    # 检查是否有任何PNG图标文件
    png_files = list(app_dir.glob("*.png"))
    
    # 排除src目录中的文件
    png_files = [f for f in png_files if f.parent.name != "src"]
    
    return len(png_files) > 0


def get_aapt_path() -> Path | None:
    """获取aapt工具路径"""
    from adb_helper import get_aapt_path as _get_aapt_path
    return _get_aapt_path()


def get_icon_name_from_aapt(apk_path: Path) -> Set[str]:
    """
    使用aapt获取APK中的图标资源名称
    
    Returns:
        图标资源名称集合
    """
    aapt_path = get_aapt_path()
    if not aapt_path:
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
            icon_pattern = r"application-icon-\d+:\s*['\"]([^'\"]+)['\"]"
            matches = re.findall(icon_pattern, result.stdout)
            for match in matches:
                icon_name = Path(match).name
                icon_names.add(icon_name)
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
        pass
    
    return icon_names


def extract_icons_from_apk(apk_path: Path, output_dir: Path) -> int:
    """
    从APK中提取图标资源
    
    Args:
        apk_path: APK文件路径
        output_dir: 输出目录
        
    Returns:
        提取的文件数量
    """
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # 使用aapt获取图标名称
    icon_names = get_icon_name_from_aapt(apk_path)
    
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
            icon_patterns.append(re.escape(icon_name))
        else:
            icon_patterns.append(rf'res/.*/{re.escape(icon_name)}')
    
    extracted_count = 0
    
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
                return 0
            
            for file_path in matched_files:
                # 生成输出文件名
                path_parts = Path(file_path).parts
                density = None
                filename = None
                
                for part in path_parts:
                    if 'mipmap-' in part or 'drawable-' in part:
                        density = part
                    if part.endswith('.png') or part.endswith('.xml') or part.endswith('.webp'):
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
                    extracted_count += 1
                except Exception:
                    pass
    
    except Exception:
        return 0
    
    return extracted_count


def get_apk_path(adb_path: str, package_name: str) -> str | None:
    """获取应用APK路径"""
    try:
        cmd = [adb_path, "shell", "pm", "path", package_name]
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            encoding='utf-8',
            errors='ignore',
            timeout=10
        )
        
        if result.returncode == 0 and result.stdout:
            # 输出格式: package:/data/app/.../base.apk
            match = re.search(r'package:([^\s]+)', result.stdout)
            if match:
                return match.group(1)
    except Exception:
        pass
    
    return None


def extract_missing_apks():
    """拉取缺少PNG图标的应用APK并提取图标"""
    print("="*60)
    print("拉取APK并提取图标")
    print("="*60)
    
    # 检查adb连接
    adb_path = get_adb_path()
    try:
        result = subprocess.run([adb_path, "devices"], capture_output=True, text=True, encoding='utf-8', errors='ignore', timeout=5)
        if not result.stdout or ("device" not in result.stdout and "no devices" not in result.stdout.lower()):
            print("错误: 未检测到已连接的设备")
            return
    except Exception as e:
        print(f"错误: 无法检查adb连接: {e}")
        return
    
    # 检查aapt工具
    aapt_path = get_aapt_path()
    if aapt_path:
        print(f"✓ 找到aapt工具: {aapt_path}")
    else:
        print("⚠ 未找到aapt工具，将使用文件名模式匹配")
    print()
    
    # 加载缺失应用列表
    missing_apps = load_missing_apps()
    if not missing_apps:
        print("未找到缺失应用列表")
        return
    
    # 检查哪些应用没有PNG图标
    base_dir = get_new_artplus_dir()
    apps_without_png = []
    
    print(f"检查 {len(missing_apps)} 个应用...")
    for app in missing_apps:
        package_name = app.get('package', '')
        if package_name:
            if not check_app_has_png_icon(package_name, base_dir):
                apps_without_png.append(app)
    
    print(f"找到 {len(apps_without_png)} 个缺少PNG图标的应用\n")
    
    if not apps_without_png:
        print("所有应用都已具备PNG图标，无需提取")
        return
    
    # 拉取APK并提取图标
    print("开始拉取APK并提取图标...\n")
    
    success_count = 0
    fail_count = 0
    skip_count = 0
    
    for i, app in enumerate(apps_without_png, 1):
        package_name = app.get('package', '')
        app_name = app.get('name', package_name)
        
        print(f"[{i}/{len(apps_without_png)}] {app_name} ({package_name})")
        
        # 获取APK路径
        apk_path = get_apk_path(adb_path, package_name)
        if not apk_path:
            print(f"  ✗ 无法获取APK路径")
            fail_count += 1
            continue
        
        # 拉取APK到临时文件
        with tempfile.NamedTemporaryFile(suffix='.apk', delete=False) as tmp_apk:
            tmp_apk_path = tmp_apk.name
        
        try:
            # 拉取APK
            cmd = [adb_path, "pull", apk_path, tmp_apk_path]
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                encoding='utf-8',
                errors='ignore',
                timeout=30
            )
            
            if result.returncode != 0 or not Path(tmp_apk_path).exists():
                print(f"  ✗ 拉取APK失败")
                fail_count += 1
                continue
            
            # 提取图标
            output_dir = base_dir / package_name
            extracted_count = extract_icons_from_apk(Path(tmp_apk_path), output_dir)
            
            if extracted_count > 0:
                print(f"  ✓ 成功提取 {extracted_count} 个图标文件")
                success_count += 1
            else:
                print(f"  ⚠ 未找到图标文件")
                skip_count += 1
        
        except Exception as e:
            print(f"  ✗ 处理失败: {e}")
            fail_count += 1
        finally:
            # 清理临时文件
            try:
                Path(tmp_apk_path).unlink()
            except:
                pass
    
    print("\n" + "="*60)
    print("提取完成！")
    print(f"  总计: {len(apps_without_png)} 个应用")
    print(f"  成功: {success_count} 个")
    print(f"  跳过: {skip_count} 个")
    print(f"  失败: {fail_count} 个")
    print("="*60)
    print("\n说明:")
    print(f"- 图标文件已保存到: {base_dir}/<包名>/")
    print("- 如果提取失败，可以手动处理或使用其他方法")


if __name__ == "__main__":
    extract_missing_apks()
