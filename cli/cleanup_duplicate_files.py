#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
清理outputs/new_artplus目录中的重复文件
删除带序号后缀的重复文件（如 file_1.png, file_2.png等）
"""

import sys
import io
from pathlib import Path
import hashlib

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


def get_file_hash(file_path: Path) -> str:
    """计算文件的MD5哈希值"""
    hash_md5 = hashlib.md5()
    try:
        with open(file_path, "rb") as f:
            for chunk in iter(lambda: f.read(4096), b""):
                hash_md5.update(chunk)
        return hash_md5.hexdigest()
    except Exception:
        return None


def is_duplicate_filename(filename: str) -> bool:
    """检查文件名是否是带序号后缀的重复文件"""
    # 匹配 pattern: name_数字.扩展名
    import re
    pattern = r'^(.+)_(\d+)\.([^.]+)$'
    return bool(re.match(pattern, filename))


def cleanup_duplicate_files():
    """清理重复文件"""
    print("="*60)
    print("清理outputs/new_artplus目录中的重复文件")
    print("="*60)
    
    base_dir = get_path("outputs/new_artplus")
    if not base_dir.exists():
        print(f"错误: 目录不存在: {base_dir}")
        return
    
    deleted_count = 0
    skipped_count = 0
    total_size_freed = 0
    
    # 遍历所有应用目录
    app_dirs = [d for d in base_dir.iterdir() if d.is_dir()]
    
    print(f"\n扫描 {len(app_dirs)} 个应用目录...\n")
    
    for app_dir in app_dirs:
        package_name = app_dir.name
        print(f"处理: {package_name}")
        
        # 获取所有文件（排除src目录）
        all_files = [f for f in app_dir.iterdir() if f.is_file()]
        
        # 按文件名分组
        file_groups = {}
        for file_path in all_files:
            filename = file_path.name
            
            # 检查是否是重复文件名
            if is_duplicate_filename(filename):
                # 提取基础文件名（去掉_数字后缀）
                import re
                match = re.match(r'^(.+)_(\d+)\.([^.]+)$', filename)
                if match:
                    base_name = match.group(1)
                    ext = match.group(3)
                    base_filename = f"{base_name}.{ext}"
                    
                    if base_filename not in file_groups:
                        file_groups[base_filename] = []
                    file_groups[base_filename].append(file_path)
            else:
                # 普通文件，检查是否有重复
                if filename not in file_groups:
                    file_groups[filename] = []
                file_groups[filename].append(file_path)
        
        # 处理每个文件组
        for base_filename, files in file_groups.items():
            if len(files) <= 1:
                continue
            
            # 找到原始文件（不带序号后缀的）
            original_file = None
            duplicate_files = []
            
            for file_path in files:
                if file_path.name == base_filename:
                    original_file = file_path
                else:
                    duplicate_files.append(file_path)
            
            # 如果没有原始文件，保留第一个，删除其他的
            if not original_file:
                # 按序号排序，保留第一个
                duplicate_files.sort(key=lambda x: x.name)
                original_file = duplicate_files[0]
                duplicate_files = duplicate_files[1:]
            
            # 检查重复文件是否与原始文件内容相同
            if original_file:
                original_hash = get_file_hash(original_file)
                
                for dup_file in duplicate_files:
                    dup_hash = get_file_hash(dup_file)
                    
                    # 如果哈希相同，删除重复文件
                    if original_hash and dup_hash and original_hash == dup_hash:
                        try:
                            file_size = dup_file.stat().st_size
                            dup_file.unlink()
                            deleted_count += 1
                            total_size_freed += file_size
                            print(f"  删除重复文件: {dup_file.name} (与 {original_file.name} 相同)")
                        except Exception as e:
                            print(f"  警告: 删除失败 {dup_file.name}: {e}")
                    else:
                        # 哈希不同，可能是不同的文件，保留
                        skipped_count += 1
                        print(f"  保留: {dup_file.name} (内容不同)")
    
    print("\n" + "="*60)
    print("清理完成！")
    print(f"  删除重复文件: {deleted_count} 个")
    print(f"  保留文件: {skipped_count} 个")
    print(f"  释放空间: {total_size_freed / 1024 / 1024:.2f} MB")
    print("="*60)


if __name__ == "__main__":
    cleanup_duplicate_files()
