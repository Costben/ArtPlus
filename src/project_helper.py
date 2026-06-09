#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
项目路径辅助模块
统一管理项目路径
"""

from pathlib import Path


_LEGACY_PATH_MAP = {
    # 旧输出路径 -> 新标准输出路径
    "new_artplus": "outputs/new_artplus",
    "missing_artplus_icons.txt": "outputs/reports/missing_artplus_icons.txt",
    "missing_artplus_icons.json": "outputs/reports/missing_artplus_icons.json",
    "missing_artplus_icons.html": "outputs/reports/missing_artplus_icons.html",
    "app_display_names.json": "outputs/mappings/app_display_names.json",
    "new_artplus.zip": "outputs/archives/new_artplus.zip",
    "temp_apk": "outputs/tmp/apk",
    # 旧测试目录 -> 标准tests目录
    "test": "tests",
}


def get_project_root() -> Path:
    """
    获取项目根目录路径
    
    Returns:
        项目根目录的Path对象
    """
    # 获取当前脚本所在目录（src目录）
    script_dir = Path(__file__).parent.absolute()
    
    # 项目根目录是scripts的父目录
    project_root = script_dir.parent.absolute()
    
    return project_root


def _resolve_relative_path(relative_path: str) -> str:
    """解析旧路径映射到新的标准路径"""
    return _LEGACY_PATH_MAP.get(relative_path, relative_path)


def get_path(relative_path: str) -> Path:
    """
    获取相对于项目根目录的路径
    
    Args:
        relative_path: 相对路径字符串
        
    Returns:
        绝对路径的Path对象
    """
    project_root = get_project_root()
    return project_root / _resolve_relative_path(relative_path)


def ensure_dir(path: Path) -> Path:
    """确保目录存在，返回目录路径"""
    path.mkdir(parents=True, exist_ok=True)
    return path


def get_outputs_dir() -> Path:
    """获取输出根目录"""
    return get_path("outputs")


def get_reports_dir() -> Path:
    """获取报告输出目录"""
    return get_path("outputs/reports")


def get_mappings_dir() -> Path:
    """获取映射文件目录"""
    return get_path("outputs/mappings")


def get_new_artplus_dir() -> Path:
    """获取新生成图标输出目录"""
    return get_path("outputs/new_artplus")


def get_temp_dir() -> Path:
    """获取临时目录"""
    return get_path("outputs/tmp")


def get_tests_dir() -> Path:
    """获取测试目录"""
    return get_path("tests")


def get_report_path(filename: str = "missing_artplus_icons.txt") -> Path:
    """获取报告文件路径（默认缺失检测报告）"""
    return get_reports_dir() / filename


def get_mapping_path(filename: str = "app_display_names.json") -> Path:
    """获取映射文件路径"""
    return get_mappings_dir() / filename
