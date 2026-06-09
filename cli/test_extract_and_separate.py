#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试脚本：从APK提取图标并分离前后景
"""

import sys
import io
import zipfile
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

def extract_png_icon_from_apk(apk_path: Path, output_dir: Path) -> Path | None:
    """从APK中提取PNG图标"""
    try:
        with zipfile.ZipFile(apk_path, 'r') as zip_ref:
            # 查找PNG图标文件
            icon_files = [f for f in zip_ref.namelist() 
                         if 'ic_launcher' in f and f.endswith('.png')]
            
            if not icon_files:
                print("  未找到PNG图标文件")
                return None
            
            # 选择最大的PNG文件
            largest_file = None
            largest_size = 0
            for file_path in icon_files:
                file_info = zip_ref.getinfo(file_path)
                if file_info.file_size > largest_size:
                    largest_size = file_info.file_size
                    largest_file = file_path
            
            if not largest_file:
                return None
            
            # 提取文件
            output_dir.mkdir(parents=True, exist_ok=True)
            output_filename = Path(largest_file).name
            output_path = output_dir / output_filename
            
            with zip_ref.open(largest_file) as source:
                with open(output_path, 'wb') as target:
                    target.write(source.read())
            
            print(f"  ✓ 已提取图标: {output_filename} ({largest_size:,} bytes)")
            return output_path
            
    except Exception as e:
        print(f"  ✗ 提取失败: {e}")
        return None

def test_separate_icon(icon_path: Path, output_dir: Path):
    """测试分离图标前后景"""
    try:
        from separate_icons_with_rembg import separate_icon_with_rembg
        
        print(f"  开始分离图标...")
        result = separate_icon_with_rembg(icon_path, output_dir=output_dir)
        
        if result:
            foreground_path, background_path = result
            print(f"  ✓ 前景: {foreground_path.name}")
            print(f"  ✓ 背景: {background_path.name}")
            return True
        else:
            print(f"  ✗ 分离失败")
            return False
            
    except ImportError as e:
        print(f"  ✗ 缺少依赖: {e}")
        print(f"  请安装: pip install rembg onnxruntime Pillow numpy opencv-python scikit-learn scipy")
        return False
    except Exception as e:
        print(f"  ✗ 分离失败: {e}")
        import traceback
        traceback.print_exc()
        return False

def main():
    """主函数"""
    test_dir = get_path("test")
    
    if not test_dir.exists():
        print(f"错误: test目录不存在: {test_dir}")
        return
    
    # 查找APK文件
    apk_files = list(test_dir.glob("*.apk"))
    
    if not apk_files:
        print(f"错误: test目录中未找到APK文件")
        return
    
    print("="*60)
    print("测试：从APK提取图标并分离前后景")
    print("="*60)
    print()
    
    for apk_file in apk_files:
        print(f"处理APK: {apk_file.name}")
        
        # 创建输出目录
        output_dir = test_dir / apk_file.stem
        output_dir.mkdir(parents=True, exist_ok=True)
        
        # 步骤1: 提取PNG图标
        print("步骤1: 提取PNG图标")
        icon_path = extract_png_icon_from_apk(apk_file, output_dir)
        
        if not icon_path:
            print("  跳过（未找到PNG图标）")
            continue
        
        print()
        
        # 步骤2: 分离前后景
        print("步骤2: 分离图标前后景")
        success = test_separate_icon(icon_path, output_dir)
        
        if success:
            print("  ✓ 测试成功！")
        else:
            print("  ✗ 测试失败")
        
        print()
    
    print("="*60)
    print("测试完成！")
    print("="*60)

if __name__ == "__main__":
    main()

