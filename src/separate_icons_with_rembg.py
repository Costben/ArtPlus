#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
使用rembg批量分离new_artplus中非官方命名的PNG图标的前景和背景
跳过已经官方命名的包
"""

import sys
import io
from pathlib import Path
from typing import List, Tuple

# 设置输出编码
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# 添加src目录到路径
script_dir = Path(__file__).parent.absolute()
if str(script_dir) not in sys.path:
    sys.path.insert(0, str(script_dir))

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
    print("警告: 未安装numpy，某些功能可能不可用")

try:
    from rembg import remove, new_session
    HAS_REMBG = True
except ImportError as e:
    HAS_REMBG = False
    print("错误: 无法导入rembg库")
    error_msg = str(e)
    if "onnxruntime" in error_msg.lower():
        print("缺少依赖库: onnxruntime")
        print("请运行: pip install onnxruntime")
    elif "rembg" in error_msg.lower():
        print("请运行: pip install rembg")
    else:
        print(f"导入错误: {error_msg}")
        print("请运行: pip install rembg onnxruntime")
    sys.exit(1)


# 官方ART图标命名规则
OFFICIAL_ICON_NAMES = {
    # 经典样式（亮色）
    'recbg.png', 'recbg_1x2.png', 'recbg_2x1.png', 'recbg_2x2.png',
    'recfg.png', 'recfg_1x2.png', 'recfg_2x1.png', 'recfg_2x2.png',
    # 经典样式（暗色）
    'rec_night.png', 'rec_night_1x2.png', 'rec_night_2x1.png', 'rec_night_2x2.png',
    # 灵感样式
    'monochrome.png', 'monochrome_1x2.png', 'monochrome_2x1.png', 'monochrome_2x2.png',
    # 其他样式
    'day.png',  # 彩昼
    'nsd.png',  # 夜影
    'mat.png',  # 材料
    'peb.png',  # 鹅卵石
    'outline.png',  # 图标描边
    'art_off.png',  # 用途暂不明确
    'game_app.png',  # 游戏图标描边
}


def has_official_icons(app_dir: Path) -> bool:
    """检查应用目录是否已有官方ART图标"""
    for png_file in app_dir.iterdir():
        if png_file.is_file() and png_file.name.lower() in OFFICIAL_ICON_NAMES:
            return True
    return False


def is_official_icon_name(filename: str) -> bool:
    """检查文件名是否符合官方ART图标命名规则"""
    return filename.lower() in OFFICIAL_ICON_NAMES


def separate_icon_with_rembg(image_path: Path, output_dir: Path = None, session=None) -> Tuple[Path, Path] | None:
    """
    使用rembg分离图标的前景和背景
    
    Args:
        image_path: 输入图像路径
        output_dir: 输出目录
        session: rembg session对象（如果为None，会使用默认模型）
    
    Returns:
        (foreground_path, background_path) 或 None
    """
    if output_dir is None:
        output_dir = image_path.parent
    
    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    
    try:
        # 读取图像
        with open(image_path, 'rb') as f:
            input_data = f.read()
        
        # 使用rembg移除背景
        # 如果提供了session，使用它；否则使用默认模型
        if session is not None:
            output_data = remove(input_data, session=session)
        else:
            output_data = remove(input_data)
        
        # 转换为PIL Image
        from io import BytesIO
        foreground_raw = Image.open(BytesIO(output_data))
        
        # 获取原始图像
        original = Image.open(image_path)
        
        # 确保都是RGBA模式
        if foreground_raw.mode != 'RGBA':
            foreground_raw = foreground_raw.convert('RGBA')
        if original.mode != 'RGBA':
            original = original.convert('RGBA')
        
        # 提取前景的alpha通道作为掩码
        if foreground_raw.mode == 'RGBA' and HAS_NUMPY:
            # 获取alpha通道
            alpha = foreground_raw.split()[3]
            alpha_array = np.array(alpha)
            
            # 策略：直接使用GrabCut分离主体，不依赖rembg的输出
            # rembg可能把整个图标都当作前景，所以我们需要用GrabCut重新分离
            if HAS_OPENCV:
                original_rgb = original.convert('RGB')
                original_array = np.array(original_rgb)
                h, w = original_array.shape[:2]
                
                # 使用更大的中心区域作为前景提示（保留更多主体）
                # 创建一个矩形区域，占图像的70%，位于中心
                margin = 0.15
                rect = (
                    int(w * margin),
                    int(h * margin),
                    int(w * (1 - 2 * margin)),
                    int(h * (1 - 2 * margin))
                )
                
                # 转换为BGR格式
                img_bgr = cv2.cvtColor(original_array, cv2.COLOR_RGB2BGR)
                
                # 初始化GrabCut
                mask = np.zeros((h, w), dtype=np.uint8)
                bgd_model = np.zeros((1, 65), np.float64)
                fgd_model = np.zeros((1, 65), np.float64)
                
                # 运行GrabCut（使用矩形初始化，迭代10次以获得更好的结果）
                cv2.grabCut(img_bgr, mask, rect, bgd_model, fgd_model, 10, cv2.GC_INIT_WITH_RECT)
                
                # 提取前景mask（确定前景+可能前景）
                final_mask = np.where((mask == 1) | (mask == 3), 255, 0).astype(np.uint8)
                
                # 连通组件分析：保留所有较大的连通区域（基于绝对面积，不只看最大的）
                num_labels, labels, stats, centroids = cv2.connectedComponentsWithStats(final_mask, connectivity=8)
                if num_labels > 1:
                    areas = [stats[i, cv2.CC_STAT_AREA] for i in range(1, num_labels)]
                    if areas:
                        # 使用非常低的绝对面积阈值
                        image_area = h * w
                        threshold = image_area * 0.0005  # 降低到0.05%图像面积
                        
                        # 使用更小的相对阈值
                        max_area = max(areas)
                        relative_threshold = max_area * 0.01  # 降低到1%最大面积
                        
                        # 至少保留最大的5个区域
                        sorted_areas = sorted(areas, reverse=True)
                        if len(sorted_areas) >= 5:
                            threshold_by_rank = sorted_areas[4] * 0.5
                        elif len(sorted_areas) >= 3:
                            threshold_by_rank = sorted_areas[2] * 0.5
                        else:
                            threshold_by_rank = min(areas) * 0.5
                        
                        # 使用所有阈值中最小的
                        final_threshold = min(threshold, relative_threshold, threshold_by_rank)
                        
                        valid_labels = [i + 1 for i, area in enumerate(areas) if area >= final_threshold]
                        
                        # 合并所有有效区域
                        final_mask_new = np.zeros_like(final_mask)
                        for label in valid_labels:
                            final_mask_new[labels == label] = 255
                        final_mask = final_mask_new
                
                # 移除黄色-橙色背景：使用颜色相似度检测
                # 从图像边缘和四个角提取背景颜色（包括黄色-橙色）
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
                
                if edge_pixels:
                    # 使用K-means找到主要背景颜色
                    try:
                        from sklearn.cluster import KMeans
                        edge_pixels_array = np.array(edge_pixels)
                        n_clusters = min(5, len(edge_pixels_array))
                        if n_clusters > 0:
                            kmeans = KMeans(n_clusters=n_clusters, random_state=42, n_init=10)
                            kmeans.fit(edge_pixels_array)
                            bg_colors = kmeans.cluster_centers_.astype(int)
                            
                            # 在前景mask区域内，移除与背景颜色相似的像素
                            for bg_color in bg_colors:
                                # 计算背景颜色的亮度
                                bg_luminance = (0.299 * bg_color[0] + 0.587 * bg_color[1] + 0.114 * bg_color[2]) / 255.0
                                
                                # 只处理较亮的背景颜色（黄色-橙色）
                                if bg_luminance > 0.4:  # 排除黑色等暗色
                                    # 计算颜色差异（使用安全的计算方式）
                                    color_diff = original_array.astype(np.float32) - bg_color.astype(np.float32)
                                    color_distance = np.sqrt(np.sum(color_diff ** 2, axis=2))
                                    
                                    # 如果像素颜色与背景颜色相似（距离 < 100，更宽松以捕获更多背景），且在前景mask内，则移除
                                    similar_to_bg = (color_distance < 100) & (final_mask > 128)
                                    final_mask[similar_to_bg] = 0
                    except:
                        pass
                
                # 使用HSV颜色空间直接检测黄色-橙色像素并移除
                # 转换到HSV颜色空间
                img_hsv = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2HSV)
                
                # 定义黄色-橙色的HSV范围（扩大范围）
                lower_yellow = np.array([8, 60, 60])   # 更低的阈值
                upper_yellow = np.array([40, 255, 255])  # 更宽的范围
                
                # 创建黄色-橙色掩码
                yellow_orange_mask = cv2.inRange(img_hsv, lower_yellow, upper_yellow)
                
                # 在前景mask区域内，移除黄色-橙色像素
                yellow_in_foreground = (yellow_orange_mask > 0) & (final_mask > 128)
                final_mask[yellow_in_foreground] = 0
                
                # 额外的RGB空间检测：直接检测黄色像素
                r, g, b = original_array[:, :, 0], original_array[:, :, 1], original_array[:, :, 2]
                # 黄色：R和G都>150，B < R和G的平均值
                yellow_rgb_mask = (r > 150) & (g > 150) & (b < (r + g) / 2) & (final_mask > 128)
                final_mask[yellow_rgb_mask] = 0
                
                # 再次连通组件分析：保留所有较大的连通区域（更激进的策略）
                num_labels, labels, stats, centroids = cv2.connectedComponentsWithStats(final_mask, connectivity=8)
                if num_labels > 1:
                    areas = [stats[i, cv2.CC_STAT_AREA] for i in range(1, num_labels)]
                    if areas:
                        # 策略1: 使用非常低的绝对面积阈值
                        image_area = h * w
                        threshold = image_area * 0.0005  # 降低到0.05%图像面积
                        
                        # 策略2: 使用更小的相对阈值
                        max_area = max(areas)
                        relative_threshold = max_area * 0.01  # 降低到1%最大面积
                        
                        # 策略3: 至少保留最大的5个区域（确保下半部分被保留）
                        sorted_areas = sorted(areas, reverse=True)
                        if len(sorted_areas) >= 5:
                            # 确保阈值不超过第5大区域的面积
                            fifth_largest = sorted_areas[4]
                            threshold_by_rank = fifth_largest * 0.5  # 第5大区域的50%
                        elif len(sorted_areas) >= 3:
                            # 如果区域少于5个，至少保留前3个
                            third_largest = sorted_areas[2]
                            threshold_by_rank = third_largest * 0.5
                        else:
                            threshold_by_rank = min(areas) * 0.5  # 所有区域的50%
                        
                        # 使用所有阈值中最小的
                        final_threshold = min(threshold, relative_threshold, threshold_by_rank)
                        
                        valid_labels = [i + 1 for i, area in enumerate(areas) if area >= final_threshold]
                        
                        # 如果有效标签太少，至少保留最大的几个
                        if len(valid_labels) < 3 and len(areas) >= 3:
                            # 至少保留最大的3个区域
                            sorted_indices = sorted(range(len(areas)), key=lambda i: areas[i], reverse=True)
                            valid_labels = [sorted_indices[i] + 1 for i in range(min(3, len(sorted_indices)))]
                        
                        # 合并所有有效区域
                        final_mask_new = np.zeros_like(final_mask)
                        for label in valid_labels:
                            final_mask_new[labels == label] = 255
                        final_mask = final_mask_new
                
                # 形态学操作清理边缘
                kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (3, 3))
                final_mask = cv2.morphologyEx(final_mask, cv2.MORPH_OPEN, kernel, iterations=1)
                final_mask = cv2.morphologyEx(final_mask, cv2.MORPH_CLOSE, kernel, iterations=1)
                
                # 高斯模糊平滑边缘
                final_mask = cv2.GaussianBlur(final_mask, (3, 3), 0)
                final_mask = np.where(final_mask > 128, 255, 0).astype(np.uint8)
                
                alpha_array = final_mask
            else:
                # 如果没有OpenCV，使用原始rembg输出，但降低阈值
                # 连通组件分析：保留所有较大的连通区域
                binary_mask = ((alpha_array > 20) * 255).astype(np.uint8)
                if HAS_OPENCV:
                    num_labels, labels, stats, centroids = cv2.connectedComponentsWithStats(binary_mask, connectivity=8)
                    if num_labels > 1:
                        areas = [stats[i, cv2.CC_STAT_AREA] for i in range(1, num_labels)]
                    if areas:
                        # 使用非常低的绝对面积阈值
                        h, w = alpha_array.shape
                        image_area = h * w
                        threshold = image_area * 0.0005  # 降低到0.05%图像面积
                        
                        max_area = max(areas)
                        relative_threshold = max_area * 0.01  # 降低到1%最大面积
                        
                        # 至少保留最大的5个区域
                        sorted_areas = sorted(areas, reverse=True)
                        if len(sorted_areas) >= 5:
                            threshold_by_rank = sorted_areas[4] * 0.5
                        elif len(sorted_areas) >= 3:
                            threshold_by_rank = sorted_areas[2] * 0.5
                        else:
                            threshold_by_rank = min(areas) * 0.5
                        
                        final_threshold = min(threshold, relative_threshold, threshold_by_rank)
                        
                        valid_labels = [i + 1 for i, area in enumerate(areas) if area >= final_threshold]
                        
                        # 如果有效标签太少，至少保留最大的几个
                        if len(valid_labels) < 3 and len(areas) >= 3:
                            sorted_indices = sorted(range(len(areas)), key=lambda i: areas[i], reverse=True)
                            valid_labels = [sorted_indices[i] + 1 for i in range(min(3, len(sorted_indices)))]
                        
                        # 合并所有有效区域
                        valid_mask = np.zeros_like(alpha_array)
                        for label in valid_labels:
                            valid_mask[labels == label] = 1
                        alpha_array = np.where(valid_mask > 0, alpha_array, 0)
                
                # 使用更低的阈值
                alpha_array = np.where(alpha_array < 30, 0, 
                              np.where(alpha_array > 100, 255, alpha_array)).astype(np.uint8)
            
            # 创建处理后的前景图像（只包含前景，背景区域设为透明）
            original_rgb = original.convert('RGB')
            original_array = np.array(original_rgb)
            h, w = original_array.shape[:2]
            foreground_array = np.zeros((h, w, 4), dtype=np.uint8)
            foreground_array[:, :, :3] = original_array  # RGB通道
            foreground_array[:, :, 3] = alpha_array  # Alpha通道（前景区域不透明，背景区域透明）
            foreground = Image.fromarray(foreground_array, 'RGBA')
        else:
            # 如果没有numpy，直接使用原始输出
            foreground = foreground_raw
        
        # 提取前景的alpha通道作为掩码（用于背景颜色提取）
        if foreground.mode == 'RGBA':
            alpha = foreground.split()[3]
            alpha_array = np.array(alpha) if HAS_NUMPY else None
            
            # 计算背景区域的主要颜色
            if HAS_NUMPY:
                # 将原图转换为numpy数组
                original_array = np.array(original.convert('RGB'))
                
                # 优先从图像边缘和四个角提取背景颜色（更可靠）
                h, w = original_array.shape[:2]
                edge_pixels = []
                
                # 提取边缘像素（上下左右各5像素）
                edge_width = min(5, w // 10, h // 10)
                edge_pixels.extend(original_array[0:edge_width, :, :].reshape(-1, 3))  # 上边缘
                edge_pixels.extend(original_array[-edge_width:, :, :].reshape(-1, 3))  # 下边缘
                edge_pixels.extend(original_array[:, 0:edge_width, :].reshape(-1, 3))  # 左边缘
                edge_pixels.extend(original_array[:, -edge_width:, :].reshape(-1, 3))  # 右边缘
                
                # 提取四个角的像素
                corner_size = min(10, w // 4, h // 4)
                edge_pixels.extend(original_array[0:corner_size, 0:corner_size, :].reshape(-1, 3))  # 左上角
                edge_pixels.extend(original_array[0:corner_size, -corner_size:, :].reshape(-1, 3))  # 右上角
                edge_pixels.extend(original_array[-corner_size:, 0:corner_size, :].reshape(-1, 3))  # 左下角
                edge_pixels.extend(original_array[-corner_size:, -corner_size:, :].reshape(-1, 3))  # 右下角
                
                # 同时从rembg分离的背景区域提取（作为补充）
                bg_mask = alpha_array < 50
                if bg_mask.any():
                    bg_pixels_from_mask = original_array[bg_mask][:, :3]
                    edge_pixels.extend(bg_pixels_from_mask.tolist())
                
                if edge_pixels:
                    bg_pixels = np.array(edge_pixels)
                    
                    # 使用K-means聚类找到主要颜色（最多5个聚类）
                    try:
                        from sklearn.cluster import KMeans
                        n_clusters = min(5, len(bg_pixels))
                        if n_clusters > 0:
                            kmeans = KMeans(n_clusters=n_clusters, random_state=42, n_init=10)
                            kmeans.fit(bg_pixels)
                            
                            # 获取每个聚类的中心颜色和像素数量
                            colors = kmeans.cluster_centers_.astype(int)
                            labels = kmeans.labels_
                            
                            # 计算每个颜色的占比和亮度
                            color_info = []
                            for i, color in enumerate(colors):
                                count = np.sum(labels == i)
                                ratio = count / len(bg_pixels)  # 占比
                                # 计算亮度（相对亮度公式）
                                r, g, b = color
                                luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                                color_info.append({
                                    'color': tuple(color),
                                    'ratio': ratio,
                                    'luminance': luminance
                                })
                            
                            # 选择既占据较大区域（ratio > 0.1）又较亮（luminance较高）的颜色
                            # 优先考虑占比，然后考虑亮度
                            valid_colors = [c for c in color_info if c['ratio'] > 0.1]
                            if valid_colors:
                                # 按占比和亮度的加权分数排序
                                valid_colors.sort(key=lambda x: x['ratio'] * 0.7 + x['luminance'] * 0.3, reverse=True)
                                bg_color = valid_colors[0]['color']
                            else:
                                # 如果没有占比>0.1的颜色，选择最亮的
                                color_info.sort(key=lambda x: x['luminance'], reverse=True)
                                bg_color = color_info[0]['color']
                        else:
                            # 如果像素太少，使用平均颜色
                            bg_color = tuple(int(c) for c in bg_pixels.mean(axis=0))
                    except ImportError:
                        # 如果没有sklearn，使用简单的平均颜色
                        bg_color = tuple(int(c) for c in bg_pixels.mean(axis=0))
                    except Exception:
                        # 如果K-means失败，使用平均颜色
                        bg_color = tuple(int(c) for c in bg_pixels.mean(axis=0))
                else:
                    # 如果edge_pixels为空，使用图像边缘的颜色
                    h, w = original_array.shape[:2]
                    edge_pixels_fallback = np.concatenate([
                        original_array[0, :, :].reshape(-1, 3),  # 上边缘
                        original_array[-1, :, :].reshape(-1, 3),  # 下边缘
                        original_array[:, 0, :].reshape(-1, 3),  # 左边缘
                        original_array[:, -1, :].reshape(-1, 3),  # 右边缘
                    ])
                    bg_color = tuple(int(c) for c in edge_pixels_fallback.mean(axis=0))
            else:
                # 不使用numpy的简单方法：使用图像边缘的平均颜色
                original_array = list(original.getdata())
                w, h = original.size
                edge_pixels = []
                # 提取边缘像素
                for y in [0, h-1]:
                    for x in range(w):
                        r, g, b, a = original_array[y * w + x]
                        edge_pixels.append((r, g, b))
                for x in [0, w-1]:
                    for y in range(1, h-1):
                        r, g, b, a = original_array[y * w + x]
                        edge_pixels.append((r, g, b))
                
                if edge_pixels:
                    bg_color = tuple(int(sum(c) / len(edge_pixels)) for c in zip(*edge_pixels))
                else:
                    bg_color = (255, 255, 255)  # 默认白色
            
            # 判断背景是否为白色或黑色（或接近）
            # 计算颜色的亮度（使用相对亮度公式）
            r, g, b = bg_color
            luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
            
            # 判断是否为白色（亮度 > 0.9）或黑色（亮度 < 0.1）
            is_white = luminance > 0.9
            is_black = luminance < 0.1
            
            # 创建背景图像
            w, h = original.size
            
            if is_white or is_black:
                # 白色或黑色背景：直接填充，不做渐变，完全填充（不透明）
                background = Image.new('RGBA', (w, h), bg_color + (255,))
            else:
                # 有颜色的背景：添加明度渐变
                # 左上角亮度因子为1.2（可以超出1，更亮），右下角亮度因子为0.85
                if HAS_NUMPY:
                    # 使用numpy创建渐变背景
                    y_coords, x_coords = np.mgrid[0:h, 0:w]
                    # 计算每个像素到左上角的距离，归一化到[0, 1]
                    max_dist = np.sqrt(w**2 + h**2)
                    dist = np.sqrt(x_coords**2 + y_coords**2) / max_dist
                    # 亮度因子：左上角1.2（超出原始亮度），右下角0.85
                    brightness = 1.2 - dist * 0.35  # 从1.2渐变到0.85
                    
                    # 应用渐变到背景颜色
                    bg_array = np.zeros((h, w, 3), dtype=np.uint8)
                    bg_array[:, :, 0] = np.clip(bg_color[0] * brightness, 0, 255).astype(np.uint8)
                    bg_array[:, :, 1] = np.clip(bg_color[1] * brightness, 0, 255).astype(np.uint8)
                    bg_array[:, :, 2] = np.clip(bg_color[2] * brightness, 0, 255).astype(np.uint8)
                    
                    # 创建alpha通道：完全填充，全部不透明
                    bg_alpha = np.ones((h, w), dtype=np.uint8) * 255
                    
                    # 合并RGB和alpha
                    bg_rgba = np.zeros((h, w, 4), dtype=np.uint8)
                    bg_rgba[:, :, :3] = bg_array
                    bg_rgba[:, :, 3] = bg_alpha
                    
                    background = Image.fromarray(bg_rgba, 'RGBA')
                else:
                    # 不使用numpy的方法：创建渐变背景
                    background = Image.new('RGBA', (w, h))
                    bg_pixels = []
                    max_dist = (w**2 + h**2)**0.5
                    
                    for y in range(h):
                        for x in range(w):
                            # 计算到左上角的距离
                            dist = (x**2 + y**2)**0.5 / max_dist
                            # 亮度因子：左上角1.2（超出原始亮度），右下角0.85
                            brightness = 1.2 - dist * 0.35
                            
                            # 应用渐变
                            r = max(0, min(255, int(bg_color[0] * brightness)))
                            g = max(0, min(255, int(bg_color[1] * brightness)))
                            b = max(0, min(255, int(bg_color[2] * brightness)))
                            
                            # 设置alpha：完全填充，全部不透明
                            bg_pixels.append((r, g, b, 255))
                    
                    background.putdata(bg_pixels)
        else:
            # 如果没有alpha通道，使用默认白色背景
            background = Image.new('RGBA', original.size, (255, 255, 255, 255))
        
        # 生成输出文件名（添加方法前缀）
        stem = image_path.stem
        method_name = "u2net"
        foreground_path = output_dir / f"{method_name}_{stem}_foreground.png"
        background_path = output_dir / f"{method_name}_{stem}_background.png"
        
        # 保存结果
        foreground.save(foreground_path, 'PNG')
        background.save(background_path, 'PNG')
        
        return foreground_path, background_path
        
    except Exception as e:
        print(f"      错误: 分离失败: {e}")
        import traceback
        traceback.print_exc()
        return None


def process_app_directory(app_dir: Path, session=None) -> Tuple[int, int]:
    """
    处理单个应用目录
    
    Args:
        app_dir: 应用目录路径
        session: rembg session对象（共享使用以提高性能）
    
    Returns:
        (处理的文件数, 成功的文件数)
    """
    processed_count = 0
    success_count = 0
    
    # 获取所有非官方命名的PNG文件（排除src目录中的文件）
    all_png_files = [f for f in app_dir.iterdir() 
                    if f.is_file() and f.suffix.lower() == '.png']
    
    # 过滤出非官方命名的文件
    non_official_files = [f for f in all_png_files 
                          if not is_official_icon_name(f.name)]
    
    if not non_official_files:
        return 0, 0
    
    for png_file in non_official_files:
        processed_count += 1
        print(f"    处理: {png_file.name}")
        
        result = separate_icon_with_rembg(png_file, session=session)
        if result:
            foreground_path, background_path = result
            print(f"      ✓ 前景: {foreground_path.name}")
            print(f"      ✓ 背景: {background_path.name}")
            success_count += 1
        else:
            print(f"      ✗ 失败")
    
    return processed_count, success_count


def process_all_apps():
    """批量处理所有应用目录"""
    base_dir = get_path("new_artplus")
    
    if not base_dir.exists():
        print(f"错误: 目录不存在: {base_dir}")
        return
    
    print("="*60)
    print("使用rembg批量分离图标前景和背景")
    print("="*60)
    print(f"目标目录: {base_dir}\n")
    print("规则:")
    print("- 跳过已有官方ART图标的包")
    print("- 只处理非官方命名的PNG文件")
    print("- 使用rembg分离前景和背景")
    print("- 生成 {原文件名}_foreground.png 和 {原文件名}_background.png\n")
    
    # 创建共享的session以提高性能
    # 对于图标这种简单场景，尝试不同的模型
    session = None
    models_to_try = ['silueta', 'u2net', 'u2netp', 'isnet-general-use']
    
    for model_name in models_to_try:
        try:
            session = new_session(model_name)
            print(f"✓ 使用{model_name}模型\n")
            break
        except Exception as e:
            continue
    
    if session is None:
        print("⚠ 无法创建任何session，将使用默认模型\n")
    
    app_dirs = [d for d in base_dir.iterdir() if d.is_dir()]
    total_apps = len(app_dirs)
    
    if total_apps == 0:
        print("未找到应用目录")
        return
    
    skipped_count = 0
    processed_apps = 0
    total_processed_files = 0
    total_success_files = 0
    
    for i, app_dir in enumerate(app_dirs, 1):
        package_name = app_dir.name
        
        # 检查是否已有官方图标
        if has_official_icons(app_dir):
            print(f"[{i}/{total_apps}] {package_name} - 跳过（已有官方图标）")
            skipped_count += 1
            continue
        
        print(f"[{i}/{total_apps}] {package_name}")
        
        processed, success = process_app_directory(app_dir, session=session)
        
        if processed > 0:
            processed_apps += 1
            total_processed_files += processed
            total_success_files += success
            print(f"  处理: {processed} 个文件, 成功: {success}")
        else:
            print(f"  无需处理")
    
    print("\n" + "="*60)
    print("处理完成！")
    print(f"  总计应用: {total_apps}")
    print(f"  跳过应用: {skipped_count} (已有官方图标)")
    print(f"  处理应用: {processed_apps}")
    print(f"  处理文件: {total_processed_files}")
    print(f"  成功文件: {total_success_files}")
    print("="*60)
    print("\n说明:")
    print("- 前景文件: {原文件名}_foreground.png (背景透明)")
    print("- 背景文件: {原文件名}_background.png (白色背景，前景区域透明)")


if __name__ == "__main__":
    process_all_apps()
