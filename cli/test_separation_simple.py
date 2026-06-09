#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
简化版测试脚本 - 测试图标分离方法
"""

import sys
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

from project_helper import get_path

def main():
    """主函数"""
    test_dir = get_path("test")
    
    # 查找已有的图标文件
    icon_file = test_dir / "com.catchingnow.np" / "ic_launcher_512.png"
    
    if not icon_file.exists():
        print(f"错误: 图标文件不存在: {icon_file}")
        return
    
    print("="*60)
    print("测试图标分离方法")
    print("="*60)
    print(f"图标文件: {icon_file}\n")
    
    output_dir = icon_file.parent
    
    # 测试GrabCut
    print("[GRABCUT]")
    try:
        from test_separate_icons import separate_icon_with_grabcut
        result = separate_icon_with_grabcut(icon_file, output_dir=output_dir)
        if result:
            print(f"  ✓ 成功: {result[0].name}, {result[1].name}")
        else:
            print(f"  ✗ 失败")
    except Exception as e:
        print(f"  ✗ 错误: {e}")
        import traceback
        traceback.print_exc()
    
    print()
    
    # 测试U2Net
    print("[U2NET]")
    try:
        from separate_icons_with_rembg import separate_icon_with_rembg
        from rembg import new_session
        session = new_session('u2net')
        result = separate_icon_with_rembg(icon_file, output_dir=output_dir, session=session)
        if result:
            print(f"  ✓ 成功: {result[0].name}, {result[1].name}")
        else:
            print(f"  ✗ 失败")
    except Exception as e:
        print(f"  ✗ 错误: {e}")
        import traceback
        traceback.print_exc()
    
    print()
    
    # 测试SAM2
    print("[SAM2]")
    try:
        from test_separate_icons import separate_icon_with_sam2
        result = separate_icon_with_sam2(icon_file, output_dir=output_dir)
        if result:
            print(f"  ✓ 成功: {result[0].name}, {result[1].name}")
        else:
            print(f"  ✗ 失败")
    except Exception as e:
        print(f"  ✗ 错误: {e}")
        import traceback
        traceback.print_exc()
    
    print()
    print("="*60)
    print("测试完成！")
    print("="*60)
    
    # 生成预览图
    print("\n生成预览图...")
    try:
        from test_all_separation_methods import create_preview_image
        
        methods = ['grabcut', 'u2net', 'sam2']
        for method in methods:
            fg_path = output_dir / f"{method}_ic_launcher_512_foreground.png"
            bg_path = output_dir / f"{method}_ic_launcher_512_background.png"
            preview_path = output_dir / f"{method}_preview.png"
            
            if fg_path.exists() and bg_path.exists():
                print(f"  生成 {method} 预览图...")
                create_preview_image(icon_file, fg_path, bg_path, method, preview_path)
                if preview_path.exists():
                    print(f"    ✓ {preview_path.name}")
                else:
                    print(f"    ✗ 失败")
            else:
                print(f"  ⚠ {method} 的前景或背景文件不存在")
    except Exception as e:
        print(f"  预览图生成错误: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()

