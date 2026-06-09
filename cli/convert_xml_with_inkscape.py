#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
使用Inkscape命令行工具转换XML中的SVG为PNG
（替代方案，如果cairosvg不可用）
"""

import sys
import io
import subprocess
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
from cleanup_and_convert_xml import check_xml_has_svg, cleanup_app_directory


def find_inkscape() -> Optional[str]:
    """查找Inkscape可执行文件"""
    # 常见的Inkscape安装路径
    common_paths = [
        r"C:\Program Files\Inkscape\bin\inkscape.exe",
        r"C:\Program Files (x86)\Inkscape\bin\inkscape.exe",
        r"C:\Users\{}\AppData\Local\Programs\Inkscape\bin\inkscape.exe".format(
            Path.home().name
        ),
    ]
    
    for path in common_paths:
        if Path(path).exists():
            return path
    
    # 尝试从PATH查找
    inkscape = shutil.which("inkscape")
    if inkscape:
        return inkscape
    
    return None


def convert_xml_with_inkscape(xml_path: Path, output_path: Path, size: int = 240) -> bool:
    """使用Inkscape将XML转换为PNG"""
    inkscape_path = find_inkscape()
    
    if not inkscape_path:
        print(f"    错误: 未找到Inkscape，请安装Inkscape")
        print(f"    下载地址: https://inkscape.org/release/")
        return False
    
    try:
        # Inkscape命令行参数
        # --export-filename: 输出文件
        # --export-width/--export-height: 输出尺寸
        cmd = [
            inkscape_path,
            str(xml_path),
            f"--export-filename={output_path}",
            f"--export-width={size}",
            f"--export-height={size}",
            "--export-type=png"
        ]
        
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=30
        )
        
        if result.returncode == 0 and output_path.exists():
            return True
        else:
            if result.stderr:
                print(f"    错误: {result.stderr[:200]}")
            return False
    except Exception as e:
        print(f"    错误: {e}")
        return False


if __name__ == "__main__":
    print("="*60)
    print("使用Inkscape转换XML中的SVG为PNG")
    print("="*60)
    print("")
    print("注意: 此脚本需要安装Inkscape")
    print("下载地址: https://inkscape.org/release/")
    print("")
    
    inkscape_path = find_inkscape()
    if not inkscape_path:
        print("错误: 未找到Inkscape")
        print("请安装Inkscape后重试")
        sys.exit(1)
    
    print(f"找到Inkscape: {inkscape_path}\n")
    
    # 这里可以添加具体的转换逻辑
    # 目前只是示例
    print("此脚本需要与cleanup_and_convert_xml.py配合使用")
    print("或手动调用convert_xml_with_inkscape函数")
