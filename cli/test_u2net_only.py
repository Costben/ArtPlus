#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
单独测试U2Net分离方法
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
    output_dir = test_dir / "com.catchingnow.np"
    icon_file = output_dir / "ic_launcher_512.png"
    
    if not icon_file.exists():
        print(f"错误: 图标文件不存在: {icon_file}")
        return
    
    print("="*60)
    print("测试U2Net分离方法（应用优化后的代码）")
    print("="*60)
    print(f"图标文件: {icon_file}\n")
    
    # 删除旧的U2Net文件
    print("清理旧文件...")
    fg_file = output_dir / "u2net_ic_launcher_512_foreground.png"
    bg_file = output_dir / "u2net_ic_launcher_512_background.png"
    if fg_file.exists():
        fg_file.unlink()
        print(f"  删除: {fg_file.name}")
    if bg_file.exists():
        bg_file.unlink()
        print(f"  删除: {bg_file.name}")
    print()
    
    # 重新分离 - U2Net
    print("[U2NET] 重新分离...")
    try:
        from separate_icons_with_rembg import separate_icon_with_rembg
        from rembg import new_session
        
        print("  创建rembg session...")
        session = new_session('u2net')
        print("  ✓ Session创建成功")
        
        print("  开始分离...")
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
    
    # 重新生成预览图
    print("重新生成预览图...")
    try:
        from regenerate_previews import create_preview_image
        
        fg_path = output_dir / "u2net_ic_launcher_512_foreground.png"
        bg_path = output_dir / "u2net_ic_launcher_512_background.png"
        preview_path = output_dir / "u2net_preview.png"
        
        if fg_path.exists() and bg_path.exists():
            create_preview_image(icon_file, fg_path, bg_path, "u2net", preview_path)
            if preview_path.exists():
                print(f"  ✓ u2net_preview.png")
        else:
            print(f"  ⚠ 前景或背景文件不存在")
    except Exception as e:
        print(f"  错误: {e}")
        import traceback
        traceback.print_exc()
    
    print()
    print("="*60)
    print("完成！")
    print("="*60)

if __name__ == "__main__":
    main()

