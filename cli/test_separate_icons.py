#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试脚本：在test目录中测试多种方法分离图标前景和背景
支持方法：GrabCut, U2Net (rembg), SAM2
"""

import sys
import io
from pathlib import Path
from typing import Tuple

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

try:
    from PIL import Image
    HAS_PIL = True
except ImportError:
    HAS_PIL = False
    print("错误: 需要安装Pillow库")
    sys.exit(1)

try:
    import numpy as np
    HAS_NUMPY = True
except ImportError:
    HAS_NUMPY = False

try:
    import cv2
    HAS_OPENCV = True
except ImportError:
    HAS_OPENCV = False

try:
    from rembg import remove, new_session
    HAS_REMBG = True
except ImportError:
    HAS_REMBG = False

try:
    from sam2 import SAM2Processor
    HAS_SAM2 = True
except ImportError:
    HAS_SAM2 = False

# 导入separate_icons_with_rembg中的函数
from separate_icons_with_rembg import separate_icon_with_rembg

def separate_icon_with_grabcut(image_path: Path, output_dir: Path = None) -> Tuple[Path, Path] | None:
    """使用GrabCut分离图标"""
    if not HAS_OPENCV:
        return None
    
    if output_dir is None:
        output_dir = image_path.parent
    
    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    
    try:
        img = cv2.imread(str(image_path))
        if img is None:
            return None
        
        h, w = img.shape[:2]
        mask = np.zeros((h, w), np.uint8)
        bgd_model = np.zeros((1, 65), np.float64)
        fgd_model = np.zeros((1, 65), np.float64)
        
        margin = 0.2
        rect = (int(w * margin), int(h * margin), int(w * (1 - 2 * margin)), int(h * (1 - 2 * margin)))
        cv2.grabCut(img, mask, rect, bgd_model, fgd_model, 10, cv2.GC_INIT_WITH_RECT)
        
        mask2 = np.where((mask == 1) | (mask == 3), 1, 0).astype('uint8')
        
        original = Image.open(image_path).convert('RGBA')
        original_array = np.array(original)
        original_rgb = original.convert('RGB')
        original_rgb_array = np.array(original_rgb)
        
        # 转换到HSV颜色空间以更好地检测黄色-橙色背景
        img_hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
        
        # 定义黄色-橙色的HSV范围（更宽泛以捕获更多黄色背景）
        # 扩大范围以捕获更多黄色变体
        lower_yellow = np.array([8, 60, 60])   # 更低的阈值，捕获更多黄色
        upper_yellow = np.array([40, 255, 255])  # 更宽的范围
        
        # 创建黄色-橙色掩码
        yellow_orange_mask = cv2.inRange(img_hsv, lower_yellow, upper_yellow)
        
        # 在前景mask区域内，移除黄色-橙色像素
        yellow_in_foreground = (yellow_orange_mask > 0) & (mask2 > 0)
        mask2[yellow_in_foreground] = 0
        
        # 额外的RGB空间检测：直接检测黄色像素（R和G都高，B较低）
        original_rgb_array = np.array(original_rgb)
        r, g, b = original_rgb_array[:, :, 0], original_rgb_array[:, :, 1], original_rgb_array[:, :, 2]
        # 黄色：R和G都>150，B < R和G的平均值
        yellow_rgb_mask = (r > 150) & (g > 150) & (b < (r + g) / 2) & (mask2 > 0)
        mask2[yellow_rgb_mask] = 0
        
        # 连通组件分析：保留所有较大的连通区域（基于绝对面积，而非相对面积）
        mask_binary = (mask2 * 255).astype(np.uint8)
        num_labels, labels, stats, centroids = cv2.connectedComponentsWithStats(mask_binary, connectivity=8)
        
        if num_labels > 1:
            # 找到所有连通区域
            areas = [stats[i, cv2.CC_STAT_AREA] for i in range(1, num_labels)]
            if areas:
                # 使用绝对面积阈值，而不是相对面积
                # 基于图像总面积的百分比，保留所有大于0.5%图像面积的区域
                image_area = h * w
                threshold = image_area * 0.005  # 保留大于0.5%图像面积的区域
                
                # 同时，如果最大区域很大，也保留所有大于最大区域5%的区域（作为备选）
                max_area = max(areas)
                relative_threshold = max_area * 0.05  # 5%最大面积
                
                # 使用两个阈值中较小的，确保保留更多区域
                final_threshold = min(threshold, relative_threshold)
                
                valid_labels = [i + 1 for i, area in enumerate(areas) if area >= final_threshold]
                
                # 合并所有有效区域
                final_mask = np.zeros_like(mask2)
                for label in valid_labels:
                    final_mask[labels == label] = 1
                mask2 = final_mask
        
        foreground_array = original_array.copy()
        foreground_array[mask2 == 0] = [0, 0, 0, 0]
        foreground = Image.fromarray(foreground_array)
        foreground_alpha = (mask2 * 255).astype('uint8')
        foreground.putalpha(Image.fromarray(foreground_alpha))
        
        # 提取背景颜色
        bg_mask = mask2 == 0
        if bg_mask.any() and HAS_NUMPY:
            bg_pixels = original_array[bg_mask][:, :3]
            try:
                from sklearn.cluster import KMeans
                n_clusters = min(5, len(bg_pixels))
                if n_clusters > 0:
                    kmeans = KMeans(n_clusters=n_clusters, random_state=42, n_init=10)
                    kmeans.fit(bg_pixels)
                    colors = kmeans.cluster_centers_.astype(int)
                    labels = kmeans.labels_
                    color_info = []
                    for i, color in enumerate(colors):
                        count = np.sum(labels == i)
                        ratio = count / len(bg_pixels)
                        r, g, b = color
                        luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                        color_info.append({'color': tuple(color), 'ratio': ratio, 'luminance': luminance})
                    valid_colors = [c for c in color_info if c['ratio'] > 0.1]
                    if valid_colors:
                        valid_colors.sort(key=lambda x: x['ratio'] * 0.5 + x['luminance'] * 0.5, reverse=True)
                        bg_color = valid_colors[0]['color']
                    else:
                        color_info.sort(key=lambda x: x['luminance'], reverse=True)
                        bg_color = color_info[0]['color']
                else:
                    bg_color = tuple(int(c) for c in bg_pixels.mean(axis=0))
            except:
                bg_color = tuple(int(c) for c in bg_pixels.mean(axis=0))
        else:
            bg_color = (255, 255, 255)
        
        r, g, b = bg_color
        luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
        is_white = luminance > 0.9
        is_black = luminance < 0.1
        
        if is_white or is_black:
            background = Image.new('RGBA', (w, h), bg_color + (255,))
        else:
            if HAS_NUMPY:
                y_coords, x_coords = np.mgrid[0:h, 0:w]
                max_dist = np.sqrt(w**2 + h**2)
                dist = np.sqrt(x_coords**2 + y_coords**2) / max_dist
                brightness = 1.2 - dist * 0.35
                bg_array = np.zeros((h, w, 3), dtype=np.uint8)
                bg_array[:, :, 0] = np.clip(bg_color[0] * brightness, 0, 255).astype(np.uint8)
                bg_array[:, :, 1] = np.clip(bg_color[1] * brightness, 0, 255).astype(np.uint8)
                bg_array[:, :, 2] = np.clip(bg_color[2] * brightness, 0, 255).astype(np.uint8)
                bg_alpha = np.ones((h, w), dtype=np.uint8) * 255
                bg_rgba = np.zeros((h, w, 4), dtype=np.uint8)
                bg_rgba[:, :, :3] = bg_array
                bg_rgba[:, :, 3] = bg_alpha
                background = Image.fromarray(bg_rgba, 'RGBA')
            else:
                background = Image.new('RGBA', (w, h), bg_color + (255,))
        
        stem = image_path.stem
        method_name = "grabcut"
        foreground_path = output_dir / f"{method_name}_{stem}_foreground.png"
        background_path = output_dir / f"{method_name}_{stem}_background.png"
        
        foreground.save(foreground_path, 'PNG')
        background.save(background_path, 'PNG')
        
        return foreground_path, background_path
    except Exception as e:
        print(f"      错误: GrabCut分离失败: {e}")
        return None


def separate_icon_with_sam2(image_path: Path, output_dir: Path = None) -> Tuple[Path, Path] | None:
    """使用SAM2分离图标（如果SAM2未安装，使用GrabCut作为fallback）"""
    # 即使SAM2未安装，也使用GrabCut作为fallback
    if not HAS_OPENCV:
        return None
    
    if output_dir is None:
        output_dir = image_path.parent
    
    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    
    try:
        # 读取图像
        original = Image.open(image_path).convert('RGB')
        w, h = original.size
        
        # SAM2需要提示点，我们使用图像中心点作为前景提示
        # 注意：SAM2的使用需要模型文件，这里提供一个基本实现框架
        print(f"      注意: SAM2需要模型文件和提示，当前使用中心点作为提示")
        
        # 由于SAM2实现较复杂，这里先使用GrabCut作为fallback
        # 如果用户安装了SAM2并配置了模型，可以在这里实现真正的SAM2调用
        if HAS_OPENCV:
            # 使用GrabCut作为临时实现
            img = cv2.imread(str(image_path))
            if img is None:
                return None
            
            # 使用中心区域作为前景提示
            mask = np.zeros(img.shape[:2], np.uint8)
            bgd_model = np.zeros((1, 65), np.float64)
            fgd_model = np.zeros((1, 65), np.float64)
            
            # 中心区域（占图像的60%）
            margin = 0.2
            rect = (
                int(w * margin),
                int(h * margin),
                int(w * (1 - 2 * margin)),
                int(h * (1 - 2 * margin))
            )
            
            cv2.grabCut(img, mask, rect, bgd_model, fgd_model, 5, cv2.GC_INIT_WITH_RECT)
            mask2 = np.where((mask == 1) | (mask == 3), 255, 0).astype(np.uint8)
            
            # 转换到HSV颜色空间以更好地检测黄色-橙色背景
            img_hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
            
            # 定义黄色-橙色的HSV范围（更宽泛以捕获更多黄色背景）
            lower_yellow = np.array([8, 60, 60])   # 更低的阈值
            upper_yellow = np.array([40, 255, 255])  # 更宽的范围
            
            # 创建黄色-橙色掩码
            yellow_orange_mask = cv2.inRange(img_hsv, lower_yellow, upper_yellow)
            
            # 在前景mask区域内，移除黄色-橙色像素
            yellow_in_foreground = (yellow_orange_mask > 0) & (mask2 > 0)
            mask2[yellow_in_foreground] = 0
            
            # 额外的RGB空间检测：直接检测黄色像素
            original_array = np.array(original.convert('RGB'))
            r, g, b = original_array[:, :, 0], original_array[:, :, 1], original_array[:, :, 2]
            # 黄色：R和G都>150，B < R和G的平均值
            yellow_rgb_mask = (r > 150) & (g > 150) & (b < (r + g) / 2) & (mask2 > 0)
            mask2[yellow_rgb_mask] = 0
            
            # 连通组件分析：保留所有较大的连通区域（基于绝对面积）
            num_labels, labels, stats, centroids = cv2.connectedComponentsWithStats(mask2, connectivity=8)
            
            if num_labels > 1:
                # 找到所有连通区域
                areas = [stats[i, cv2.CC_STAT_AREA] for i in range(1, num_labels)]
                if areas:
                    # 使用绝对面积阈值，而不是相对面积
                    # 基于图像总面积的百分比，保留所有大于0.5%图像面积的区域
                    image_area = h * w
                    threshold = image_area * 0.005  # 保留大于0.5%图像面积的区域
                    
                    # 同时，如果最大区域很大，也保留所有大于最大区域5%的区域（作为备选）
                    max_area = max(areas)
                    relative_threshold = max_area * 0.05  # 5%最大面积
                    
                    # 使用两个阈值中较小的，确保保留更多区域
                    final_threshold = min(threshold, relative_threshold)
                    
                    valid_labels = [i + 1 for i, area in enumerate(areas) if area >= final_threshold]
                    
                    # 合并所有有效区域
                    final_mask = np.zeros_like(mask2)
                    for label in valid_labels:
                        final_mask[labels == label] = 255
                    mask2 = final_mask
            
            # 创建前景
            foreground_array = np.array(original.convert('RGBA'))
            foreground_array[:, :, 3] = mask2
            foreground = Image.fromarray(foreground_array, 'RGBA')
            
            # 提取背景颜色（优先从图像边缘提取）
            if HAS_NUMPY:
                original_array = np.array(original)
                h, w = original_array.shape[:2]
                
                # 优先从图像边缘和四个角提取背景颜色
                edge_pixels = []
                edge_width = min(5, w // 10, h // 10)
                edge_pixels.extend(original_array[0:edge_width, :, :].reshape(-1, 3))  # 上边缘
                edge_pixels.extend(original_array[-edge_width:, :, :].reshape(-1, 3))  # 下边缘
                edge_pixels.extend(original_array[:, 0:edge_width, :].reshape(-1, 3))  # 左边缘
                edge_pixels.extend(original_array[:, -edge_width:, :].reshape(-1, 3))  # 右边缘
                
                # 四个角
                corner_size = min(10, w // 4, h // 4)
                edge_pixels.extend(original_array[0:corner_size, 0:corner_size, :].reshape(-1, 3))  # 左上角
                edge_pixels.extend(original_array[0:corner_size, -corner_size:, :].reshape(-1, 3))  # 右上角
                edge_pixels.extend(original_array[-corner_size:, 0:corner_size, :].reshape(-1, 3))  # 左下角
                edge_pixels.extend(original_array[-corner_size:, -corner_size:, :].reshape(-1, 3))  # 右下角
                
                # 同时从GrabCut分离的背景区域提取（作为补充）
                bg_mask = mask2 < 128
                if bg_mask.any():
                    bg_pixels_from_mask = original_array[bg_mask][:, :3]
                    edge_pixels.extend(bg_pixels_from_mask.tolist())
                
                if edge_pixels:
                    # 使用K-means找到主要颜色
                    try:
                        from sklearn.cluster import KMeans
                        edge_pixels_array = np.array(edge_pixels)
                        n_clusters = min(5, len(edge_pixels_array))
                        if n_clusters > 0:
                            kmeans = KMeans(n_clusters=n_clusters, random_state=42, n_init=10)
                            kmeans.fit(edge_pixels_array)
                            colors = kmeans.cluster_centers_.astype(int)
                            labels = kmeans.labels_
                            
                            color_info = []
                            for i, color in enumerate(colors):
                                count = np.sum(labels == i)
                                ratio = count / len(edge_pixels_array)
                                r, g, b = color
                                luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                                color_info.append({
                                    'color': tuple(color),
                                    'ratio': ratio,
                                    'luminance': luminance
                                })
                            
                            valid_colors = [c for c in color_info if c['ratio'] > 0.1]
                            if valid_colors:
                                valid_colors.sort(key=lambda x: x['ratio'] * 0.7 + x['luminance'] * 0.3, reverse=True)
                                bg_color = valid_colors[0]['color']
                            else:
                                color_info.sort(key=lambda x: x['luminance'], reverse=True)
                                bg_color = color_info[0]['color']
                        else:
                            bg_color = tuple(int(c) for c in np.array(edge_pixels).mean(axis=0))
                    except:
                        bg_color = tuple(int(c) for c in np.array(edge_pixels).mean(axis=0))
                else:
                    bg_color = (255, 255, 255)
            else:
                bg_color = (255, 255, 255)
            
            # 创建背景
            r, g, b = bg_color
            luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
            is_white = luminance > 0.9
            is_black = luminance < 0.1
            
            if is_white or is_black:
                background = Image.new('RGBA', (w, h), bg_color + (255,))
            else:
                if HAS_NUMPY:
                    y_coords, x_coords = np.mgrid[0:h, 0:w]
                    max_dist = np.sqrt(w**2 + h**2)
                    dist = np.sqrt(x_coords**2 + y_coords**2) / max_dist
                    brightness = 1.2 - dist * 0.35
                    bg_array = np.zeros((h, w, 3), dtype=np.uint8)
                    bg_array[:, :, 0] = np.clip(bg_color[0] * brightness, 0, 255).astype(np.uint8)
                    bg_array[:, :, 1] = np.clip(bg_color[1] * brightness, 0, 255).astype(np.uint8)
                    bg_array[:, :, 2] = np.clip(bg_color[2] * brightness, 0, 255).astype(np.uint8)
                    bg_alpha = np.ones((h, w), dtype=np.uint8) * 255
                    bg_rgba = np.zeros((h, w, 4), dtype=np.uint8)
                    bg_rgba[:, :, :3] = bg_array
                    bg_rgba[:, :, 3] = bg_alpha
                    background = Image.fromarray(bg_rgba, 'RGBA')
                else:
                    background = Image.new('RGBA', (w, h), bg_color + (255,))
            
            stem = image_path.stem
            method_name = "sam2"
            foreground_path = output_dir / f"{method_name}_{stem}_foreground.png"
            background_path = output_dir / f"{method_name}_{stem}_background.png"
            
            foreground.save(foreground_path, 'PNG')
            background.save(background_path, 'PNG')
            
            return foreground_path, background_path
        else:
            print(f"      错误: 需要OpenCV支持")
            return None
            
    except Exception as e:
        print(f"      错误: SAM2分离失败: {e}")
        return None


def extract_icon_from_apk(apk_path: Path, output_dir: Path) -> Path | None:
    """从APK中提取图标"""
    try:
        import zipfile
        import shutil
        import subprocess
        import re
        
        from adb_helper import get_aapt_path
        aapt_path = get_aapt_path()
        
        icon_patterns = [
            r'res/mipmap-.*/ic_launcher.*\.png',
            r'res/drawable-.*/ic_launcher.*\.png',
            r'res/drawable-nodpi.*/ic_launcher.*\.png',
        ]
        
        with zipfile.ZipFile(apk_path, 'r') as zip_ref:
            all_files = zip_ref.namelist()
            matched_files = []
            for file_path in all_files:
                for pattern in icon_patterns:
                    if re.match(pattern, file_path) and file_path.endswith('.png'):
                        matched_files.append((file_path, zip_ref.getinfo(file_path).file_size))
                        break
            
            if not matched_files:
                return None
            
            # 选择最大的PNG文件
            largest_file = max(matched_files, key=lambda x: x[1])[0]
            
            output_dir.mkdir(parents=True, exist_ok=True)
            output_filename = largest_file.split('/')[-1]
            output_path = output_dir / output_filename
            
            with zip_ref.open(largest_file) as source, open(output_path, 'wb') as target:
                shutil.copyfileobj(source, target)
            
            return output_path
    except Exception as e:
        print(f"      错误: 提取图标失败: {e}")
        return None


def test_separate_icons():
    """在test目录中测试分离功能"""
    test_dir = get_path("test")
    temp_dir = test_dir / "temp"
    
    if not test_dir.exists():
        print(f"错误: test目录不存在: {test_dir}")
        return
    
    # 清理temp目录
    import shutil
    if temp_dir.exists():
        shutil.rmtree(temp_dir)
    temp_dir.mkdir(parents=True, exist_ok=True)
    
    print("="*60)
    print("测试：从APK提取图标并分离前景和背景")
    print("="*60)
    print(f"测试目录: {test_dir}")
    print(f"临时目录: {temp_dir}\n")
    
    # 查找test目录中的APK文件
    apk_files = list(test_dir.rglob("*.apk"))
    
    if not apk_files:
        print("未找到APK文件")
        if temp_dir.exists():
            shutil.rmtree(temp_dir)
        return
    
    print(f"找到 {len(apk_files)} 个APK文件\n")
    
    # 提取图标
    extracted_icons = []
    for i, apk_file in enumerate(apk_files, 1):
        print(f"[提取 {i}/{len(apk_files)}] {apk_file.name}")
        icon_path = extract_icon_from_apk(apk_file, temp_dir)
        if icon_path:
            extracted_icons.append(icon_path)
            print(f"  ✓ 已提取: {icon_path.name}")
        else:
            print(f"  ✗ 提取失败")
    
    print(f"\n成功提取 {len(extracted_icons)} 个图标\n")
    print("="*60)
    print("开始分离前景和背景")
    print("="*60)
    print()
    
    # 创建rembg session
    session = None
    if HAS_REMBG:
        try:
            session = new_session('u2net')
            print("✓ 使用rembg u2net模型\n")
        except:
            try:
                session = new_session('u2netp')
                print("✓ 使用rembg u2netp模型\n")
            except:
                pass
    
    # 显示可用方法
    available_methods = ["GrabCut"]
    if HAS_REMBG:
        available_methods.append("U2Net (rembg)")
    if HAS_SAM2:
        available_methods.append("SAM2")
    
    print(f"可用方法: {', '.join(available_methods)}\n")
    
    success_count = 0
    fail_count = 0
    
    for i, icon_file in enumerate(extracted_icons, 1):
        print(f"[{i}/{len(extracted_icons)}] {icon_file.name}")
        
        apk_name = apk_files[i-1].stem
        output_dir = test_dir / apk_name
        output_dir.mkdir(parents=True, exist_ok=True)
        
        methods_results = {}
        
        # 方法1: GrabCut
        print("  使用GrabCut算法...")
        try:
            result = separate_icon_with_grabcut(icon_file, output_dir=output_dir)
            if result:
                methods_results['grabcut'] = result
                print(f"    ✓ 完成")
            else:
                print(f"    ✗ 失败")
        except Exception as e:
            print(f"    ✗ 错误: {e}")
        
        # 方法2: rembg (U2Net)
        if HAS_REMBG:
            print("  使用rembg (U2Net)算法...")
            try:
                result = separate_icon_with_rembg(icon_file, output_dir=output_dir, session=session)
                if result:
                    methods_results['u2net'] = result
                    print(f"    ✓ 完成")
                else:
                    print(f"    ✗ 失败")
            except Exception as e:
                print(f"    ✗ 错误: {e}")
        
        # 方法3: SAM2 (使用GrabCut作为fallback)
        if HAS_OPENCV:
            print("  使用SAM2算法（GrabCut fallback）...")
            try:
                result = separate_icon_with_sam2(icon_file, output_dir=output_dir)
                if result:
                    methods_results['sam2'] = result
                    print(f"    ✓ 完成")
                else:
                    print(f"    ✗ 失败")
            except Exception as e:
                print(f"    ✗ 错误: {e}")
        
        if methods_results:
            success_count += 1
            print(f"  ✓ 成功使用 {len(methods_results)} 种方法: {', '.join(methods_results.keys())}")
        else:
            fail_count += 1
            print(f"  ✗ 所有方法都失败")
        print()
    
    # 清理temp目录
    if temp_dir.exists():
        shutil.rmtree(temp_dir)
        print("✓ 已清理临时文件\n")
    
    print("="*60)
    print("测试完成！")
    print(f"  总计: {len(extracted_icons)} 个图标")
    print(f"  成功: {success_count} 个")
    print(f"  失败: {fail_count} 个")
    print("="*60)


if __name__ == "__main__":
    test_separate_icons()
