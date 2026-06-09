#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
图标 ZIP → OPPO /data/oplus/uxicons 可用目录

默认从 smb://ERILNG-NAS._smb._tcp.local/Download/ArtPlusUpload 对应的
本地挂载目录中，按“文件夹名=包名”的方式读取最新 ZIP。
也支持通过 --input-dir / --zip 指定本地测试目录或文件。
"""

from __future__ import annotations

import argparse
import base64
import json
import os
import re
import shutil
import sys
import ssl
import time
import uuid
import zipfile
from datetime import datetime
from io import BytesIO
from pathlib import Path
from typing import Any, Iterable
from urllib.error import HTTPError, URLError
from urllib.parse import unquote, urlparse
from urllib.request import Request, urlopen

# 添加src目录到路径
script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))

from project_helper import ensure_dir, get_path  # noqa: E402
from generate_all_artplus_icons import (  # noqa: E402
    SIZE_1x1,
    SIZE_1x2,
    SIZE_2x1,
    SIZE_2x2,
    create_monochrome_icon,
    create_night_icon,
    create_style_icon,
)

try:
    from PIL import Image, ImageEnhance
except ImportError as exc:  # pragma: no cover - 启动时给出明确错误
    raise SystemExit("错误: 需要 Pillow。请先运行: .venv/bin/python -m pip install Pillow") from exc


DEFAULT_SMB_URL = "smb://ERILNG-NAS._smb._tcp.local/Download/ArtPlusUpload"
DEFAULT_GPT_IMAGE_SERVICE_URL = "http://192.168.31.179:9714"
DEFAULT_GPT_IMAGE_UPSTREAM_BASE_URL = "http://192.168.31.179:3002/v1"
DEFAULT_GPT_IMAGE_MODEL = "gpt-image-2"
DEFAULT_GPT_IMAGE_SIZE = "1024x1024"
DEFAULT_GPT_IMAGE_QUALITY = "low"
DEFAULT_GPT_IMAGE_TIMEOUT_SECONDS = 360
DEFAULT_GPT_IMAGE_POLL_SECONDS = 5
DEFAULT_GPT_IMAGE_BACKEND = os.environ.get("ARTPLUS_GPT_IMAGE_BACKEND", "service")
DEFAULT_GPT_IMAGE_DIRECT_BASE_URL = os.environ.get("ARTPLUS_GPT_IMAGE_BASE_URL", DEFAULT_GPT_IMAGE_UPSTREAM_BASE_URL)
DEFAULT_GPT_IMAGE_API_KEY = os.environ.get("ARTPLUS_GPT_IMAGE_API_KEY") or os.environ.get("OPENAI_API_KEY", "")
DEFAULT_GPT_IMAGE_INSECURE_SKIP_TLS_VERIFY = os.environ.get(
    "ARTPLUS_GPT_IMAGE_INSECURE_SKIP_TLS_VERIFY", ""
).lower() in {"1", "true", "yes", "on"}
FOREGROUND_SUBJECT_MAX_SIDE_RATIO = 0.70
CHROMA_KEY_CANDIDATES = [
    (0, 255, 0),      # green
    (255, 0, 255),    # magenta
    (0, 255, 255),    # cyan
    (0, 0, 255),      # blue
    (255, 255, 0),    # yellow
]

# OPPO/ColorOS ART+ 常见官方命名
OFFICIAL_ICON_NAMES = {
    "recbg.png", "recbg_1x2.png", "recbg_2x1.png", "recbg_2x2.png",
    "recfg.png", "recfg_1x2.png", "recfg_2x1.png", "recfg_2x2.png",
    "rec_night.png", "rec_night_1x2.png", "rec_night_2x1.png", "rec_night_2x2.png",
    "monochrome.png", "monochrome_1x2.png", "monochrome_2x1.png", "monochrome_2x2.png",
    "day.png", "nsd.png", "mat.png", "peb.png",
    "outline.png", "art_off.png", "game_app.png",
}

REQUIRED_MINIMUM_FILES = [
    "recfg.png",
    "recbg.png",
    "rec_night.png",
    "monochrome.png",
    "day.png",
    "nsd.png",
    "mat.png",
    "peb.png",
]

EXPECTED_SIZES: dict[str, tuple[int, int]] = {
    "recbg.png": (SIZE_1x1, SIZE_1x1),
    "recfg.png": (SIZE_1x1, SIZE_1x1),
    "rec_night.png": (SIZE_1x1, SIZE_1x1),
    "monochrome.png": (SIZE_1x1, SIZE_1x1),
    "day.png": (SIZE_1x1, SIZE_1x1),
    "nsd.png": (SIZE_1x1, SIZE_1x1),
    "mat.png": (SIZE_1x1, SIZE_1x1),
    "peb.png": (SIZE_1x1, SIZE_1x1),
    "outline.png": (SIZE_1x1, SIZE_1x1),
    "art_off.png": (SIZE_1x1, SIZE_1x1),
    "game_app.png": (SIZE_1x1, SIZE_1x1),
    "recbg_1x2.png": SIZE_1x2,
    "recfg_1x2.png": SIZE_1x2,
    "rec_night_1x2.png": SIZE_1x2,
    "monochrome_1x2.png": SIZE_1x2,
    "recbg_2x1.png": SIZE_2x1,
    "recfg_2x1.png": SIZE_2x1,
    "rec_night_2x1.png": SIZE_2x1,
    "monochrome_2x1.png": SIZE_2x1,
    "recbg_2x2.png": (SIZE_2x2, SIZE_2x2),
    "recfg_2x2.png": (SIZE_2x2, SIZE_2x2),
    "rec_night_2x2.png": (SIZE_2x2, SIZE_2x2),
    "monochrome_2x2.png": (SIZE_2x2, SIZE_2x2),
}

IMAGE_SUFFIXES = {".png", ".webp", ".jpg", ".jpeg"}
PACKAGE_RE = re.compile(r"^[A-Za-z][A-Za-z0-9_]*(?:\.[A-Za-z][A-Za-z0-9_]*)+$")


class PipelineError(RuntimeError):
    """可展示给用户的流水线错误。"""


class RunReport(dict):
    """轻量报告字典，集中初始化字段避免失败路径缺字段。"""

    def __init__(self) -> None:
        super().__init__(
            success=False,
            error=None,
            smb_url=DEFAULT_SMB_URL,
            input_source_dir=None,
            zip_file=None,
            package_name=None,
            output_dir=None,
            copied_files=[],
            generated_files=[],
            final_files=[],
            archived_to=None,
            image_service_url=DEFAULT_GPT_IMAGE_SERVICE_URL,
            image_generation_backend=DEFAULT_GPT_IMAGE_BACKEND,
            image_generation_base_url=None,
            image_generation="enabled",
            image_generation_error=None,
            image_generation_jobs=[],
            started_at=datetime.now().isoformat(timespec="seconds"),
            finished_at=None,
        )


def outputs_latest_dir() -> Path:
    return get_path("outputs/latest")


def reports_dir() -> Path:
    return get_path("outputs/reports")


def latest_report_path() -> Path:
    return reports_dir() / "latest_run.json"


def archives_dir() -> Path:
    return get_path("outputs/archives")


def current_run_dir() -> Path:
    return get_path("outputs/tmp/current_run")


def archive_previous_outputs(report: RunReport) -> Path | None:
    """把上一轮 outputs/latest 和 latest_run.json 归档。"""
    latest = outputs_latest_dir()
    previous_report = latest_report_path()

    if not latest.exists() and not previous_report.exists():
        return None

    stamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    archive_root = archives_dir() / stamp
    suffix = 1
    while archive_root.exists():
        archive_root = archives_dir() / f"{stamp}-{suffix}"
        suffix += 1
    ensure_dir(archive_root)

    if latest.exists():
        shutil.move(str(latest), str(archive_root / "latest"))
    if previous_report.exists():
        ensure_dir(archive_root)
        shutil.move(str(previous_report), str(archive_root / "latest_run.json"))

    report["archived_to"] = str(archive_root)
    return archive_root


def clean_current_run_dir() -> None:
    tmp = current_run_dir()
    if tmp.exists():
        shutil.rmtree(tmp)


def remove_junk_files(root: Path) -> None:
    """清理 .DS_Store / __MACOSX 等无关文件。"""
    if not root.exists():
        return
    for path in sorted(root.rglob("*"), reverse=True):
        if path.name == ".DS_Store" or path.name.startswith("._"):
            path.unlink(missing_ok=True)
        elif path.is_dir() and path.name == "__MACOSX":
            shutil.rmtree(path, ignore_errors=True)
    for path in sorted(root.rglob("*"), reverse=True):
        if path.is_dir():
            try:
                path.rmdir()
            except OSError:
                pass


def parse_smb_mount_candidates(smb_url: str) -> list[Path]:
    """把 smb://host/share/path 推断为常见 macOS /Volumes 挂载路径。"""
    parsed = urlparse(smb_url)
    candidates: list[Path] = []

    env_dir = os.environ.get("ARTPLUS_UPLOAD_DIR")
    if env_dir:
        candidates.append(Path(env_dir).expanduser())

    if parsed.scheme == "smb":
        parts = [unquote(p) for p in parsed.path.split("/") if p]
        if parts:
            share = parts[0]
            rel_parts = parts[1:]
            candidates.append(Path("/Volumes") / share / Path(*rel_parts))
            # 有些用户会把子目录单独挂载出来
            if rel_parts:
                candidates.append(Path("/Volumes") / rel_parts[-1])
                candidates.append(Path("/Volumes") / parsed.netloc / share / Path(*rel_parts))
    else:
        candidates.append(Path(unquote(parsed.path or smb_url)).expanduser())

    # 去重，保序
    unique: list[Path] = []
    seen: set[str] = set()
    for candidate in candidates:
        key = str(candidate)
        if key not in seen:
            unique.append(candidate)
            seen.add(key)
    return unique


def resolve_input_dir(smb_url: str, input_dir: str | None) -> Path:
    if input_dir:
        path = Path(input_dir).expanduser().resolve()
        if not path.is_dir():
            raise PipelineError(f"输入目录不存在或不是目录: {path}")
        return path

    candidates = parse_smb_mount_candidates(smb_url)
    for candidate in candidates:
        if candidate.is_dir():
            return candidate.resolve()

    candidate_text = "\n".join(f"  - {p}" for p in candidates) or "  - 无"
    raise PipelineError(
        "未找到 SMB 本地挂载目录。请先在 Finder 打开/挂载:\n"
        f"  {smb_url}\n"
        "或运行时指定本地目录:\n"
        "  .venv/bin/python cli/run_apk_zip_to_oppo_uxicons.py --input-dir /Volumes/Download/ArtPlusUpload\n"
        "也可以设置环境变量 ARTPLUS_UPLOAD_DIR。已尝试路径:\n"
        f"{candidate_text}"
    )


def newest_file(files: Iterable[Path], suffix: str) -> Path:
    candidates = [p for p in files if p.is_file() and p.suffix.lower() == suffix]
    if not candidates:
        raise PipelineError(f"输入目录里没有找到 {suffix} 文件")
    candidates.sort(key=lambda p: (p.stat().st_mtime, p.name), reverse=True)
    return candidates[0]


def newest_zip_in_dir(source_dir: Path, zip_arg: str | None) -> Path:
    if zip_arg:
        zip_path = Path(zip_arg).expanduser().resolve()
        if not zip_path.is_file() or zip_path.suffix.lower() != ".zip":
            raise PipelineError(f"指定 ZIP 不存在或后缀不是 .zip: {zip_path}")
        return zip_path
    return newest_file(source_dir.iterdir(), ".zip")


def resolve_package_dir(source_dir: Path) -> Path:
    """从挂载根目录中找出真正的包名文件夹。"""
    if source_dir.is_dir() and PACKAGE_RE.match(source_dir.name) and any(
        child.is_file() and child.suffix.lower() == ".zip" for child in source_dir.iterdir()
    ):
        return source_dir

    candidates: list[tuple[float, Path]] = []
    for child in source_dir.iterdir():
        if not child.is_dir() or child.name.startswith("."):
            continue
        try:
            zip_path = newest_file(child.iterdir(), ".zip")
        except PipelineError:
            continue
        score = max(child.stat().st_mtime, zip_path.stat().st_mtime)
        candidates.append((score, child))

    if not candidates:
        raise PipelineError(
            f"在目录中没有找到任何包含 ZIP 的包名文件夹: {source_dir}"
        )

    candidates.sort(key=lambda item: (item[0], item[1].name), reverse=True)
    return candidates[0][1]


def copy_zip_to_run_dir(zip_path: Path) -> Path:
    input_dir = ensure_dir(current_run_dir() / "input")
    local_zip = input_dir / zip_path.name

    # SMB 挂载文件在 macOS 上有时不允许复制扩展元数据/ACL，直接复制内容最稳妥。
    shutil.copyfile(zip_path, local_zip)
    return local_zip


def is_safe_zip_member(name: str) -> bool:
    path = Path(name)
    return not path.is_absolute() and ".." not in path.parts


def unpack_icon_zip(zip_path: Path, dest_dir: Path) -> list[Path]:
    ensure_dir(dest_dir)
    extracted: list[Path] = []
    try:
        with zipfile.ZipFile(zip_path, "r") as zip_ref:
            for info in zip_ref.infolist():
                if info.is_dir() or not is_safe_zip_member(info.filename):
                    continue
                filename = Path(info.filename).name
                if not filename or filename == ".DS_Store" or filename.startswith("._"):
                    continue
                if Path(filename).suffix.lower() not in IMAGE_SUFFIXES:
                    continue
                rel_parent = Path(info.filename).parent
                if "__MACOSX" in rel_parent.parts:
                    continue
                target_dir = ensure_dir(dest_dir / rel_parent)
                target = target_dir / filename
                counter = 1
                while target.exists():
                    target = target_dir / f"{Path(filename).stem}_{counter}{Path(filename).suffix}"
                    counter += 1
                with zip_ref.open(info) as source, open(target, "wb") as out:
                    shutil.copyfileobj(source, out)
                extracted.append(target)
    except zipfile.BadZipFile as exc:
        raise PipelineError(f"ZIP 文件损坏或不可读取: {zip_path}") from exc
    return extracted


def image_area(path: Path) -> int:
    try:
        with Image.open(path) as img:
            return img.width * img.height
    except Exception:
        return 0


def image_colorfulness(path: Path) -> float:
    """粗略衡量图片是否是彩色图标；黑/白/灰 monochrome 分数会很低。"""
    try:
        with Image.open(path) as img:
            small = img.convert("RGBA")
            small.thumbnail((64, 64), Image.Resampling.LANCZOS)
            pixels = [(r, g, b) for r, g, b, a in flattened_data(small) if a >= 64]
    except Exception:
        return 0.0
    if not pixels:
        return 0.0
    # 用 RGB 通道差值衡量饱和/彩色程度。
    return sum(max(pixel) - min(pixel) for pixel in pixels) / len(pixels)


def image_darkness(path: Path) -> float:
    """粗略衡量暗色程度，用来降低黑色 monochrome 图标优先级。"""
    try:
        with Image.open(path) as img:
            small = img.convert("RGBA")
            small.thumbnail((64, 64), Image.Resampling.LANCZOS)
            pixels = [(r, g, b) for r, g, b, a in flattened_data(small) if a >= 64]
    except Exception:
        return 0.0
    if not pixels:
        return 0.0
    brightness = sum((r + g + b) / 3 for r, g, b in pixels) / len(pixels)
    return 255 - brightness


def image_opaque_coverage(path: Path, threshold: int = 32) -> float:
    try:
        with Image.open(path) as img:
            alpha = img.convert("RGBA").getchannel("A")
            hist = alpha.histogram()
            total = alpha.width * alpha.height
            return sum(hist[threshold:]) / total if total else 0.0
    except Exception:
        return 0.0


def image_edge_opaque_ratio(path: Path, threshold: int = 32) -> float:
    try:
        with Image.open(path) as img:
            rgba = img.convert("RGBA")
    except Exception:
        return 0.0
    width, height = rgba.size
    if width == 0 or height == 0:
        return 0.0
    samples: list[int] = []
    for x in range(width):
        samples.append(rgba.getpixel((x, 0))[3])
        samples.append(rgba.getpixel((x, height - 1))[3])
    for y in range(1, height - 1):
        samples.append(rgba.getpixel((0, y))[3])
        samples.append(rgba.getpixel((width - 1, y))[3])
    return sum(1 for alpha in samples if alpha >= threshold) / len(samples) if samples else 0.0


def open_image_rgba(path: Path) -> Image.Image:
    with Image.open(path) as img:
        return img.convert("RGBA")


def paste_fit_on_canvas(img: Image.Image, size: tuple[int, int], background: tuple[int, int, int, int]) -> Image.Image:
    canvas = Image.new("RGBA", size, background)
    fitted = img.convert("RGBA")
    fitted.thumbnail(size, Image.Resampling.LANCZOS)
    x = (size[0] - fitted.width) // 2
    y = (size[1] - fitted.height) // 2
    canvas.paste(fitted, (x, y), fitted)
    return canvas


def alpha_bbox(img: Image.Image, threshold: int = 8) -> tuple[int, int, int, int] | None:
    alpha = img.convert("RGBA").getchannel("A")
    mask = alpha.point(lambda value: 255 if value > threshold else 0)
    return mask.getbbox()


def foreground_subject_max_side_ratio(img: Image.Image, threshold: int = 8) -> float:
    bbox = alpha_bbox(img, threshold)
    if not bbox:
        return 0.0
    width, height = img.size
    if width <= 0 or height <= 0:
        return 0.0
    bbox_width = bbox[2] - bbox[0]
    bbox_height = bbox[3] - bbox[1]
    return max(bbox_width / width, bbox_height / height)


def normalize_foreground_subject_size(
    image_path: Path,
    target_ratio: float = FOREGROUND_SUBJECT_MAX_SIDE_RATIO,
    threshold: int = 8,
) -> bool:
    """Scale the visible alpha subject in a foreground layer to the target visual size."""
    with Image.open(image_path) as img:
        rgba = img.convert("RGBA")

    bbox = alpha_bbox(rgba, threshold)
    if not bbox:
        return False

    canvas_width, canvas_height = rgba.size
    target_side = max(1, int(round(min(canvas_width, canvas_height) * target_ratio)))
    bbox_width = bbox[2] - bbox[0]
    bbox_height = bbox[3] - bbox[1]
    max_side = max(bbox_width, bbox_height)
    if max_side <= 0 or max_side == target_side:
        return False

    subject = rgba.crop(bbox)
    scale = target_side / max_side
    new_size = (
        max(1, int(round(subject.width * scale))),
        max(1, int(round(subject.height * scale))),
    )
    subject = subject.resize(new_size, Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", rgba.size, (0, 0, 0, 0))
    x = (canvas_width - subject.width) // 2
    y = (canvas_height - subject.height) // 2
    canvas.paste(subject, (x, y), subject)
    canvas.save(image_path, "PNG", optimize=True)
    return True


def save_normalized_icon(source: Path, dest: Path, icon_name: str) -> None:
    size = EXPECTED_SIZES.get(icon_name, (SIZE_1x1, SIZE_1x1))
    img = open_image_rgba(source)

    if icon_name.startswith("recbg"):
        # 背景允许填满目标尺寸。
        bg = img.convert("RGB").resize(size, Image.Resampling.LANCZOS)
        bg.save(dest, "PNG", optimize=True)
    elif img.size == size:
        img.save(dest, "PNG", optimize=True)
    else:
        paste_fit_on_canvas(img, size, (0, 0, 0, 0)).save(dest, "PNG", optimize=True)


def flattened_data(img: Image.Image):
    """兼容 Pillow 12+，避免 getdata() 的弃用警告。"""
    getter = getattr(img, "get_flattened_data", None)
    if getter:
        return getter()
    return img.getdata()


def rgb_distance(a: tuple[int, int, int], b: tuple[int, int, int]) -> float:
    return ((a[0] - b[0]) ** 2 + (a[1] - b[1]) ** 2 + (a[2] - b[2]) ** 2) ** 0.5


def color_to_hex(color: tuple[int, int, int]) -> str:
    return f"#{color[0]:02x}{color[1]:02x}{color[2]:02x}"


def choose_chroma_key(source: Path) -> tuple[int, int, int]:
    """为 gpt-image-2 前景生成选择一个源图里最不常见的纯色抠图背景。"""
    try:
        with Image.open(source) as img:
            small = img.convert("RGBA")
            small.thumbnail((64, 64), Image.Resampling.LANCZOS)
            pixels = [
                (r, g, b)
                for r, g, b, a in flattened_data(small)
                if a >= 64
            ]
    except Exception:
        pixels = []
    if not pixels:
        return CHROMA_KEY_CANDIDATES[0]

    best = CHROMA_KEY_CANDIDATES[0]
    best_score = -1.0
    # 用“离源图所有采样色的最小距离”做分数，避免 key 色出现在主体里。
    for candidate in CHROMA_KEY_CANDIDATES:
        min_dist = min(rgb_distance(candidate, pixel) for pixel in pixels)
        if min_dist > best_score:
            best = candidate
            best_score = min_dist
    return best


def alpha_coverage(img: Image.Image, threshold: int = 8) -> float:
    alpha = img.convert("RGBA").getchannel("A")
    hist = alpha.histogram()
    total = alpha.width * alpha.height
    visible = sum(hist[threshold + 1:])
    return visible / total if total else 0.0


def image_has_real_alpha(img: Image.Image) -> bool:
    rgba = img.convert("RGBA")
    alpha = rgba.getchannel("A")
    hist = alpha.histogram()
    total = rgba.width * rgba.height
    transparent = sum(hist[:8])
    # 真透明前景应当至少有明显透明区域；棋盘格假透明通常 alpha 全 255。
    return total > 0 and transparent / total >= 0.05


def border_key_ratio(img: Image.Image, key_rgb: tuple[int, int, int], threshold: float = 80.0) -> float:
    rgba = img.convert("RGBA")
    width, height = rgba.size
    if width == 0 or height == 0:
        return 0.0
    samples: list[tuple[int, int, int]] = []
    for x in range(width):
        samples.append(rgba.getpixel((x, 0))[:3])
        samples.append(rgba.getpixel((x, height - 1))[:3])
    for y in range(1, height - 1):
        samples.append(rgba.getpixel((0, y))[:3])
        samples.append(rgba.getpixel((width - 1, y))[:3])
    if not samples:
        return 0.0
    hits = sum(1 for pixel in samples if rgb_distance(pixel, key_rgb) <= threshold)
    return hits / len(samples)


def remove_chroma_key_background(
    image_path: Path,
    output_path: Path,
    key_rgb: tuple[int, int, int],
    transparent_threshold: float = 36.0,
    opaque_threshold: float = 170.0,
) -> bool:
    """把 gpt-image-2 生成的纯色 key 背景转成真实 alpha。"""
    with Image.open(image_path) as img:
        rgba = img.convert("RGBA")
    if border_key_ratio(rgba, key_rgb) < 0.10:
        return False

    pixels: list[tuple[int, int, int, int]] = []
    for r, g, b, a in flattened_data(rgba):
        dist = rgb_distance((r, g, b), key_rgb)
        if dist <= transparent_threshold:
            new_alpha = 0
        elif dist >= opaque_threshold:
            new_alpha = a
        else:
            factor = (dist - transparent_threshold) / (opaque_threshold - transparent_threshold)
            new_alpha = int(max(0.0, min(1.0, factor)) * a)
        pixels.append((r, g, b, new_alpha))

    cleaned = Image.new("RGBA", rgba.size)
    cleaned.putdata(pixels)
    coverage = alpha_coverage(cleaned)
    if not (0.002 <= coverage <= 0.95):
        return False
    cleaned.save(output_path, "PNG", optimize=True)
    return True


def remove_small_alpha_components(img: Image.Image, min_area: int) -> Image.Image:
    """删除背景相减后残留的小噪点，保留主体/logo组件。"""
    rgba = img.convert("RGBA")
    width, height = rgba.size
    alpha = rgba.getchannel("A")
    alpha_pixels = alpha.load()
    visited = bytearray(width * height)
    keep = bytearray(width * height)

    def idx(x: int, y: int) -> int:
        return y * width + x

    for y in range(height):
        for x in range(width):
            start = idx(x, y)
            if visited[start] or alpha_pixels[x, y] <= 16:
                continue
            stack = [(x, y)]
            visited[start] = 1
            component: list[tuple[int, int]] = []
            while stack:
                cx, cy = stack.pop()
                component.append((cx, cy))
                for nx, ny in ((cx + 1, cy), (cx - 1, cy), (cx, cy + 1), (cx, cy - 1)):
                    if nx < 0 or ny < 0 or nx >= width or ny >= height:
                        continue
                    nidx = idx(nx, ny)
                    if visited[nidx] or alpha_pixels[nx, ny] <= 16:
                        continue
                    visited[nidx] = 1
                    stack.append((nx, ny))
            if len(component) >= min_area:
                for cx, cy in component:
                    keep[idx(cx, cy)] = 1

    out_pixels: list[tuple[int, int, int, int]] = []
    for i, (r, g, b, a) in enumerate(flattened_data(rgba)):
        out_pixels.append((r, g, b, a if keep[i] else 0))
    cleaned = Image.new("RGBA", rgba.size)
    cleaned.putdata(out_pixels)
    return cleaned


def derive_foreground_from_source_and_background(source: Path, background: Path, output_path: Path) -> bool:
    """
    当 gpt-image-2 把棋盘格画进 RGB 时，用“原图 - 去主体背景图”重建真实透明前景。
    这比直接信任模型的透明输出更稳。
    """
    size = EXPECTED_SIZES["recfg.png"]
    with Image.open(source) as src_img:
        src = paste_fit_on_canvas(src_img.convert("RGBA"), size, (0, 0, 0, 0))
    with Image.open(background) as bg_img:
        bg = bg_img.convert("RGB").resize(size, Image.Resampling.LANCZOS)

    pixels: list[tuple[int, int, int, int]] = []
    transparent_threshold = 24.0
    opaque_threshold = 92.0
    for (r, g, b, a), (br, bg_g, bb) in zip(flattened_data(src), flattened_data(bg)):
        if a <= 8:
            pixels.append((r, g, b, 0))
            continue
        dist = rgb_distance((r, g, b), (br, bg_g, bb))
        if dist <= transparent_threshold:
            new_alpha = 0
        elif dist >= opaque_threshold:
            new_alpha = a
        else:
            factor = (dist - transparent_threshold) / (opaque_threshold - transparent_threshold)
            new_alpha = int(max(0.0, min(1.0, factor)) * a)
        pixels.append((r, g, b, new_alpha))

    out = Image.new("RGBA", size)
    out.putdata(pixels)
    out = remove_small_alpha_components(out, min_area=max(8, int(size[0] * size[1] * 0.00035)))
    coverage = alpha_coverage(out)
    if not (0.002 <= coverage <= 0.90):
        return False
    out.save(output_path, "PNG", optimize=True)
    return True


def save_true_foreground_icon(
    raw_foreground: Path,
    source: Path,
    raw_background: Path,
    dest: Path,
    chroma_key: tuple[int, int, int],
) -> str:
    """
    保存真正带 alpha 的 recfg.png。
    优先使用模型的真实 alpha，其次使用 chroma-key，最后用源图和背景图相减兜底。
    """
    with Image.open(raw_foreground) as raw_img:
        raw_rgba = raw_img.convert("RGBA")
        if image_has_real_alpha(raw_rgba):
            save_normalized_icon(raw_foreground, dest, "recfg.png")
            return "model-alpha"

    tmp_dir = ensure_dir(current_run_dir() / "foreground_postprocess")
    chroma_clean = tmp_dir / "recfg_chroma_clean.png"
    if remove_chroma_key_background(raw_foreground, chroma_clean, chroma_key):
        save_normalized_icon(chroma_clean, dest, "recfg.png")
        return "chroma-key"

    if derive_foreground_from_source_and_background(source, raw_background, dest):
        return "source-bg-subtract"

    # 最后兜底：至少不要把棋盘格当透明，直接用源图生成前景。
    create_recfg_from_source(source, dest.parent)
    return "source-fallback"


def copy_standard_icons(image_paths: list[Path], output_dir: Path) -> list[str]:
    copied: list[str] = []
    # 同名官方文件重复时，优先使用分辨率最大的。
    grouped: dict[str, list[Path]] = {}
    for image_path in image_paths:
        official_name = image_path.name.lower()
        if official_name in OFFICIAL_ICON_NAMES and image_path.suffix.lower() == ".png":
            grouped.setdefault(official_name, []).append(image_path)

    for official_name, paths in sorted(grouped.items()):
        source = sorted(paths, key=image_area, reverse=True)[0]
        dest = output_dir / official_name
        save_normalized_icon(source, dest, official_name)
        copied.append(official_name)
    return copied


def choose_best_source_icon(image_paths: list[Path]) -> Path | None:
    candidates = []
    for path in image_paths:
        if path.suffix.lower() not in IMAGE_SUFFIXES:
            continue
        name = path.name.lower()
        if name.startswith("recbg") or name in {"bg.png", "background.png"}:
            continue
        priority = 0
        if name == "recfg.png":
            priority += 100
        if "foreground" in name or name in {"fg.png", "front.png"}:
            priority += 80
        if "launcher" in name:
            priority += 60
        if "icon" in name:
            priority += 40
        if "logo" in name:
            priority += 20
        colorfulness = image_colorfulness(path)
        darkness = image_darkness(path)
        if colorfulness >= 35:
            priority += 160
        elif colorfulness < 18:
            priority -= 90
        if darkness >= 155 and colorfulness < 45:
            priority -= 120
        if "monochrome" in name or "mono" in name or "black" in name:
            priority -= 200
        # APK 解包出来的 res/mipmap* 通常比根目录 icon.png 更接近真正 launcher 图标。
        if any(part.startswith("mipmap") for part in path.parts) or "res" in path.parts:
            priority += 25
        candidates.append((priority, colorfulness, image_area(path), -len(path.parts), path))
    if not candidates:
        return None
    candidates.sort(reverse=True)
    return candidates[0][4]


def choose_best_background_source(image_paths: list[Path], foreground_source: Path) -> Path:
    """选择适合生成 recbg 的源图：优先全尺寸、有实底色的完整图标。"""
    candidates = []
    for path in image_paths:
        if path.suffix.lower() not in IMAGE_SUFFIXES:
            continue
        name = path.name.lower()
        coverage = image_opaque_coverage(path)
        edge_ratio = image_edge_opaque_ratio(path)
        area = image_area(path)
        priority = 0
        if name.startswith("recbg") or name in {"bg.png", "background.png"}:
            priority += 300
        if name == "icon.png":
            priority += 120
        if "launcher" in name:
            priority += 60
        if "icon" in name:
            priority += 40
        if coverage >= 0.75:
            priority += 140
        elif coverage < 0.25:
            priority -= 140
        if edge_ratio >= 0.50:
            priority += 120
        elif edge_ratio < 0.10:
            priority -= 100
        if path == foreground_source and coverage < 0.50:
            priority -= 180
        candidates.append((priority, edge_ratio, coverage, area, -len(path.parts), path))
    if not candidates:
        return foreground_source
    candidates.sort(reverse=True)
    return candidates[0][5]


def http_json(url: str, payload: dict[str, Any] | None = None, timeout: int = 60) -> dict[str, Any]:
    data = None
    headers = {"Accept": "application/json"}
    method = "GET"
    if payload is not None:
        data = json.dumps(payload).encode("utf-8")
        headers["Content-Type"] = "application/json"
        method = "POST"
    request = Request(url, data=data, headers=headers, method=method)
    try:
        with urlopen(request, timeout=timeout) as response:
            body = response.read().decode("utf-8", errors="replace")
    except HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        raise PipelineError(f"调用生图服务失败 HTTP {exc.code}: {body[:500]}") from exc
    except URLError as exc:
        raise PipelineError(f"无法连接生图服务: {exc}") from exc
    try:
        parsed = json.loads(body)
    except json.JSONDecodeError as exc:
        raise PipelineError(f"生图服务返回非 JSON: {body[:500]}") from exc
    if not isinstance(parsed, dict):
        raise PipelineError(f"生图服务返回格式异常: {type(parsed).__name__}")
    return parsed


def parse_json_response(response_body: str, source: str) -> dict[str, Any]:
    try:
        parsed = json.loads(response_body)
    except json.JSONDecodeError as exc:
        raise PipelineError(f"{source} 返回非 JSON: {response_body[:500]}") from exc
    if not isinstance(parsed, dict):
        raise PipelineError(f"{source} 返回格式异常: {type(parsed).__name__}")
    return parsed


def build_multipart_body(
    fields: dict[str, str],
    files: list[tuple[str, str, str, bytes]],
) -> tuple[str, bytes]:
    boundary = f"----ArtPlus{uuid.uuid4().hex}"
    chunks: list[bytes] = []
    for name, value in fields.items():
        chunks.extend(
            [
                f"--{boundary}\r\n".encode("utf-8"),
                f'Content-Disposition: form-data; name="{name}"\r\n\r\n'.encode("utf-8"),
                str(value).encode("utf-8"),
                b"\r\n",
            ]
        )
    for field_name, filename, content_type, content in files:
        chunks.extend(
            [
                f"--{boundary}\r\n".encode("utf-8"),
                (
                    f'Content-Disposition: form-data; name="{field_name}"; '
                    f'filename="{filename}"\r\n'
                ).encode("utf-8"),
                f"Content-Type: {content_type}\r\n\r\n".encode("utf-8"),
                content,
                b"\r\n",
            ]
        )
    chunks.append(f"--{boundary}--\r\n".encode("utf-8"))
    return f"multipart/form-data; boundary={boundary}", b"".join(chunks)


def image_to_png_bytes(source: Path) -> tuple[bytes, int, int]:
    with Image.open(source) as img:
        rgba = img.convert("RGBA")
        width, height = rgba.size
        buffer = BytesIO()
        rgba.save(buffer, "PNG")
    return buffer.getvalue(), width, height


def download_binary(url: str, timeout: int = 60, insecure_skip_tls_verify: bool = False) -> bytes:
    context = ssl._create_unverified_context() if insecure_skip_tls_verify else None
    try:
        with urlopen(url, timeout=timeout, context=context) as response:
            return response.read()
    except HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        raise PipelineError(f"下载生图结果失败 HTTP {exc.code}: {body[:500]}") from exc
    except URLError as exc:
        raise PipelineError(f"下载生图结果失败: {exc}") from exc


def source_image_to_data_url(source: Path) -> tuple[str, int, int]:
    """统一转成 PNG data URL 上传给本地 gpt-image-2 页面。"""
    png_bytes, width, height = image_to_png_bytes(source)
    encoded = base64.b64encode(png_bytes).decode("ascii")
    return f"data:image/png;base64,{encoded}", width, height


def normalize_images_edit_url(base_url: str) -> str:
    normalized = base_url.rstrip("/")
    if normalized.endswith("/images/edits"):
        return normalized
    if normalized.endswith("/v1"):
        return f"{normalized}/images/edits"
    if normalized.endswith("/v1/"):
        return f"{normalized}images/edits"
    if "/v1/" in f"{normalized}/":
        return f"{normalized}/images/edits"
    return f"{normalized}/v1/images/edits"


def extract_image_response_bytes(
    response: dict[str, Any],
    timeout: int = 120,
    insecure_skip_tls_verify: bool = False,
) -> bytes:
    data = response.get("data")
    if not isinstance(data, list) or not data:
        raise PipelineError(f"直连生图接口未返回 data: {json.dumps(response, ensure_ascii=False)[:500]}")
    first = data[0]
    if not isinstance(first, dict):
        raise PipelineError(f"直连生图接口 data[0] 格式异常: {type(first).__name__}")

    b64_json = first.get("b64_json") or first.get("b64")
    if b64_json:
        try:
            return base64.b64decode(str(b64_json))
        except Exception as exc:
            raise PipelineError("直连生图接口返回的 base64 图片无效") from exc

    image_url = first.get("url") or first.get("imageUrl") or first.get("remoteImageUrl")
    if image_url:
        return download_binary(str(image_url), timeout=timeout, insecure_skip_tls_verify=insecure_skip_tls_verify)

    raise PipelineError(f"直连生图接口没有返回图片数据: {json.dumps(first, ensure_ascii=False)[:500]}")


def direct_gpt_image_edit(
    source: Path,
    prompt: str,
    background: str,
    output_path: Path,
    args: argparse.Namespace,
) -> None:
    api_key = str(args.gpt_image_api_key or "").strip()
    if not api_key:
        raise PipelineError("直连 gpt-image 需要 API key: 设置 ARTPLUS_GPT_IMAGE_API_KEY 或传 --gpt-image-api-key")

    png_bytes, _, _ = image_to_png_bytes(source)
    fields = {
        "model": args.gpt_image_model,
        "prompt": prompt,
        "size": args.gpt_image_size,
        "quality": args.gpt_image_quality,
        "background": background,
        "output_format": "png",
    }
    content_type, body = build_multipart_body(
        fields,
        [("image", "artplus_source_icon.png", "image/png", png_bytes)],
    )
    request = Request(
        normalize_images_edit_url(args.gpt_image_base_url),
        data=body,
        headers={
            "Accept": "application/json",
            "Authorization": f"Bearer {api_key}",
            "Content-Type": content_type,
        },
        method="POST",
    )
    context = ssl._create_unverified_context() if args.gpt_image_insecure_skip_tls_verify else None
    try:
        with urlopen(request, timeout=args.gpt_image_timeout_seconds, context=context) as response:
            response_body = response.read().decode("utf-8", errors="replace")
    except HTTPError as exc:
        response_body = exc.read().decode("utf-8", errors="replace")
        raise PipelineError(f"直连 gpt-image 失败 HTTP {exc.code}: {response_body[:500]}") from exc
    except URLError as exc:
        raise PipelineError(f"无法连接直连 gpt-image 接口: {exc}") from exc

    parsed = parse_json_response(response_body, "直连 gpt-image")
    output_path.write_bytes(
        extract_image_response_bytes(
            parsed,
            insecure_skip_tls_verify=args.gpt_image_insecure_skip_tls_verify,
        )
    )
    try:
        with Image.open(output_path) as img:
            img.verify()
    except Exception as exc:
        raise PipelineError(f"直连 gpt-image 结果不是有效图片: {output_path}") from exc


def get_image_service_settings(service_url: str) -> dict[str, Any]:
    state = http_json(f"{service_url.rstrip('/')}/api/shared-state", timeout=60)
    settings = ((state.get("state") or {}).get("settings") or {})
    if not isinstance(settings, dict):
        settings = {}
    return settings


def pick_gpt_upstream_slot(settings: dict[str, Any], upstream_base_url: str, model: str) -> dict[str, Any] | None:
    slots = settings.get("upstreamSlots")
    if not isinstance(slots, list):
        return None
    normalized = upstream_base_url.rstrip("/")
    for slot in slots:
        if not isinstance(slot, dict) or not slot.get("enabled", True):
            continue
        if str(slot.get("baseUrl", "")).rstrip("/") == normalized and str(slot.get("model", "")) == model:
            return slot
    return None


def make_gpt_edit_request(
    settings: dict[str, Any],
    source_image_id: str,
    source_width: int,
    source_height: int,
    prompt: str,
    background: str,
    args: argparse.Namespace,
) -> dict[str, Any]:
    """构造与本地最小生图页一致的请求体。"""
    request_body = dict(settings)
    slot = pick_gpt_upstream_slot(settings, args.gpt_image_upstream_base_url, args.gpt_image_model)
    if slot:
        request_body.update(
            {
                "mode": slot.get("mode", "images"),
                "baseUrl": slot.get("baseUrl", args.gpt_image_upstream_base_url),
                "apiKey": slot.get("apiKey", request_body.get("apiKey", "")),
                "upstreamGroup": slot.get("upstreamGroup", ""),
                "upstreamAlias": slot.get("upstreamAlias", ""),
                "upstreamClientProfile": slot.get("upstreamClientProfile", "default"),
                "model": slot.get("model", args.gpt_image_model),
                "concurrencyLimit": slot.get("concurrency", request_body.get("concurrencyLimit", 5)),
                "retryAttempts": slot.get("retryAttempts", 0),
                "overloadCooldownMs": slot.get("overloadCooldownMs", 15000),
                "activeUpstreamSlotId": slot.get("id", ""),
            }
        )
    else:
        request_body.update(
            {
                "mode": "images",
                "baseUrl": args.gpt_image_upstream_base_url,
                "upstreamGroup": "",
                "upstreamAlias": request_body.get("upstreamAlias", "ErlAPI"),
                "upstreamClientProfile": request_body.get("upstreamClientProfile", "default"),
                "model": args.gpt_image_model,
                "concurrencyLimit": request_body.get("concurrencyLimit", 5),
                "retryAttempts": 0,
                "overloadCooldownMs": request_body.get("overloadCooldownMs", 15000),
            }
        )
    request_body.update(
        {
            "requestType": "edit",
            "prompt": prompt,
            "size": args.gpt_image_size,
            "quality": args.gpt_image_quality,
            "background": background,
            "outputFormat": "png",
            "imagesNativeStream": True,
            "partialImages": 2,
            "moderation": "auto",
            "outputCompression": 100,
            "inputImageId": source_image_id,
            "inputImageOriginalWidth": source_width,
            "inputImageOriginalHeight": source_height,
            "inputImageWidth": source_width,
            "inputImageHeight": source_height,
            "inputImageScaleRatio": 1,
        }
    )
    for key in ("imageUrl", "remoteImageUrl", "result", "queue", "events", "updatedAt"):
        request_body.pop(key, None)
    return request_body


def submit_gpt_image_job(
    service_url: str,
    request_body: dict[str, Any],
    timeout: int,
) -> str:
    result = http_json(
        f"{service_url.rstrip('/')}/api/images/generations",
        payload=request_body,
        timeout=timeout,
    )
    queue_item = result.get("queueItem") or {}
    queue_id = queue_item.get("id")
    if not queue_id:
        raise PipelineError(f"生图服务未返回队列 ID: {json.dumps(result, ensure_ascii=False)[:500]}")
    return str(queue_id)


def poll_gpt_image_jobs(service_url: str, queue_ids: list[str], timeout_seconds: int) -> dict[str, dict[str, Any]]:
    deadline = time.time() + timeout_seconds
    last_status = ""
    while time.time() < deadline:
        state = http_json(f"{service_url.rstrip('/')}/api/shared-state", timeout=60)
        queue = ((state.get("state") or {}).get("queue") or [])
        queue_map = {item.get("id"): item for item in queue if isinstance(item, dict) and item.get("id") in queue_ids}
        statuses = []
        done = True
        for queue_id in queue_ids:
            item = queue_map.get(queue_id)
            status = item.get("status") if item else "missing"
            statuses.append(f"{queue_id[:8]}={status}")
            if status not in {"success", "failed", "error", "cancelled"}:
                done = False
        status_text = " ".join(statuses)
        if status_text != last_status:
            print(f"  gpt-image-2: {status_text}")
            last_status = status_text
        if done:
            return queue_map
        time.sleep(DEFAULT_GPT_IMAGE_POLL_SECONDS)
    raise PipelineError(f"等待 gpt-image-2 结果超时: {', '.join(queue_ids)}")


def fetch_gpt_result_image(service_url: str, item: dict[str, Any], output_path: Path) -> None:
    result = item.get("result") or {}
    if item.get("status") != "success" or not result.get("ok"):
        raise PipelineError(
            "gpt-image-2 任务失败: "
            f"status={item.get('status')}, error={result.get('error') or result.get('message') or result.get('details')}"
        )
    image_url = result.get("imageUrl") or result.get("remoteImageUrl")
    if not image_url:
        raise PipelineError("gpt-image-2 成功但没有返回图片 URL")
    if str(image_url).startswith("/"):
        image_url = f"{service_url.rstrip('/')}{image_url}"
    output_path.write_bytes(download_binary(str(image_url), timeout=120))
    try:
        with Image.open(output_path) as img:
            img.verify()
    except Exception as exc:
        raise PipelineError(f"gpt-image-2 结果不是有效图片: {output_path}") from exc


def build_gpt_image_prompts(chroma_hex: str) -> tuple[str, str]:
    foreground_prompt = (
        "Keep only the app icon main subject/logo. Remove the original background. "
        "Scale the subject/logo so its visible bounding box is about 70% of the final square canvas. "
        f"Place the remaining subject on a perfectly flat solid {chroma_hex} chroma-key background. "
        "The chroma-key background must be one uniform color, with no checkerboard, no transparency preview pattern, "
        "no shadows, no gradients, no texture, and no lighting variation. "
        f"Do not use {chroma_hex} anywhere in the subject/logo. Preserve the subject shape and colors."
    )
    background_prompt = (
        "Remove the app icon main subject/logo. Rebuild only the clean original background plate. "
        "No logo, no text, no symbol."
    )
    return foreground_prompt, background_prompt


def gpt_image2_split_source_icon(
    source: Path,
    background_source: Path,
    output_dir: Path,
    args: argparse.Namespace,
    report: RunReport,
) -> list[str]:
    """默认优先使用本地 gpt-image-2 服务生成 recfg/recbg。"""
    service_url = args.gpt_image_service_url.rstrip("/")
    data_url, width, height = source_image_to_data_url(source)
    upload = http_json(
        f"{service_url}/api/source-images",
        payload={"inputImageDataUrl": data_url, "fileName": "artplus_source_icon.png"},
        timeout=60,
    )
    source_image_id = upload.get("sourceImageId")
    if not source_image_id:
        raise PipelineError(f"生图服务上传源图失败: {json.dumps(upload, ensure_ascii=False)[:500]}")
    bg_data_url, bg_width, bg_height = source_image_to_data_url(background_source)
    bg_upload = http_json(
        f"{service_url}/api/source-images",
        payload={"inputImageDataUrl": bg_data_url, "fileName": "artplus_background_source_icon.png"},
        timeout=60,
    )
    background_source_image_id = bg_upload.get("sourceImageId")
    if not background_source_image_id:
        raise PipelineError(f"生图服务上传背景源图失败: {json.dumps(bg_upload, ensure_ascii=False)[:500]}")

    settings = get_image_service_settings(service_url)
    chroma_key = choose_chroma_key(source)
    chroma_hex = color_to_hex(chroma_key)
    foreground_prompt, background_prompt = build_gpt_image_prompts(chroma_hex)
    fg_request = make_gpt_edit_request(
        settings, str(source_image_id), width, height, foreground_prompt, "opaque", args
    )
    bg_request = make_gpt_edit_request(
        settings, str(background_source_image_id), bg_width, bg_height, background_prompt, "opaque", args
    )

    print("  默认使用 gpt-image-2 生成 recfg/recbg...")
    fg_id = submit_gpt_image_job(service_url, fg_request, timeout=120)
    bg_id = submit_gpt_image_job(service_url, bg_request, timeout=120)
    queue_items = poll_gpt_image_jobs(service_url, [fg_id, bg_id], args.gpt_image_timeout_seconds)

    raw_dir = ensure_dir(current_run_dir() / "gpt_image2")
    raw_fg = raw_dir / "recfg_gpt.png"
    raw_bg = raw_dir / "recbg_gpt.png"
    fetch_gpt_result_image(service_url, queue_items[fg_id], raw_fg)
    fetch_gpt_result_image(service_url, queue_items[bg_id], raw_bg)

    generated: list[str] = []
    if not (output_dir / "recfg.png").exists():
        foreground_method = save_true_foreground_icon(raw_fg, source, raw_bg, output_dir / "recfg.png", chroma_key)
        report["foreground_postprocess"] = foreground_method
        report["foreground_chroma_key"] = chroma_hex
        report["foreground_source"] = str(source)
        report["background_source"] = str(background_source)
        generated.append("recfg.png")
    if not (output_dir / "recbg.png").exists():
        save_normalized_icon(raw_bg, output_dir / "recbg.png", "recbg.png")
        generated.append("recbg.png")
    jobs = [
        {
            "type": "foreground",
            "queue_id": fg_id,
            "status": queue_items[fg_id].get("status"),
            "image_url": (queue_items[fg_id].get("result") or {}).get("imageUrl"),
            "target_host": (queue_items[fg_id].get("result") or {}).get("targetHost"),
        },
        {
            "type": "background",
            "queue_id": bg_id,
            "status": queue_items[bg_id].get("status"),
            "image_url": (queue_items[bg_id].get("result") or {}).get("imageUrl"),
            "target_host": (queue_items[bg_id].get("result") or {}).get("targetHost"),
        },
    ]
    report["image_generation"] = "gpt-image-2"
    report["image_generation_backend"] = "service"
    report["image_generation_base_url"] = args.gpt_image_service_url
    report["image_generation_jobs"] = jobs
    return generated


def direct_gpt_image2_split_source_icon(
    source: Path,
    background_source: Path,
    output_dir: Path,
    args: argparse.Namespace,
    report: RunReport,
) -> list[str]:
    """Use an OpenAI-compatible images/edits endpoint directly with base URL + API key."""
    chroma_key = choose_chroma_key(source)
    chroma_hex = color_to_hex(chroma_key)
    foreground_prompt, background_prompt = build_gpt_image_prompts(chroma_hex)

    raw_dir = ensure_dir(current_run_dir() / "gpt_image_direct")
    raw_fg = raw_dir / "recfg_direct.png"
    raw_bg = raw_dir / "recbg_direct.png"

    print("  直连 gpt-image 生成 recfg/recbg...")
    direct_gpt_image_edit(source, foreground_prompt, "opaque", raw_fg, args)
    direct_gpt_image_edit(background_source, background_prompt, "opaque", raw_bg, args)

    generated: list[str] = []
    if not (output_dir / "recfg.png").exists():
        foreground_method = save_true_foreground_icon(raw_fg, source, raw_bg, output_dir / "recfg.png", chroma_key)
        report["foreground_postprocess"] = foreground_method
        report["foreground_chroma_key"] = chroma_hex
        report["foreground_source"] = str(source)
        report["background_source"] = str(background_source)
        generated.append("recfg.png")
    if not (output_dir / "recbg.png").exists():
        save_normalized_icon(raw_bg, output_dir / "recbg.png", "recbg.png")
        generated.append("recbg.png")

    report["image_generation"] = "gpt-image-2"
    report["image_generation_backend"] = "direct"
    report["image_generation_base_url"] = args.gpt_image_base_url
    report["image_generation_jobs"] = [
        {
            "type": "foreground",
            "status": "success",
            "endpoint": normalize_images_edit_url(args.gpt_image_base_url),
        },
        {
            "type": "background",
            "status": "success",
            "endpoint": normalize_images_edit_url(args.gpt_image_base_url),
        },
    ]
    return generated


def sample_background_color(bg_path: Path) -> tuple[int, int, int]:
    with Image.open(bg_path) as img:
        rgba = img.convert("RGBA")
        width, height = rgba.size
        r, g, b, a = rgba.getpixel((width // 2, height // 2))
        if a >= 32 and r + g + b >= 120:
            return r, g, b

        small = rgba.resize((32, 32), Image.Resampling.BOX)
        pixels: list[tuple[int, int, int]] = []
        for y in range(small.height):
            for x in range(small.width):
                rr, gg, bb, aa = small.getpixel((x, y))
                if aa >= 128 and rr + gg + bb >= 120:
                    pixels.append((rr, gg, bb))
        if not pixels:
            return (216, 224, 253)
        return (
            sum(p[0] for p in pixels) // len(pixels),
            sum(p[1] for p in pixels) // len(pixels),
            sum(p[2] for p in pixels) // len(pixels),
        )


def recolor_rec_night_to_background(output_dir: Path) -> list[str]:
    """暗色模式下把主体改成背景浅色，避免暗色主体在暗色背景上看不清。"""
    bg_path = output_dir / "recbg.png"
    if not bg_path.exists():
        return []
    bg_color = sample_background_color(bg_path)
    changed: list[str] = []
    for suffix in ("", "_1x2", "_2x1", "_2x2"):
        fg_path = output_dir / f"recfg{suffix}.png"
        night_path = output_dir / f"rec_night{suffix}.png"
        if not fg_path.exists():
            continue
        with Image.open(fg_path) as fg:
            fg_rgba = fg.convert("RGBA")
            alpha = fg_rgba.getchannel("A")
            night = Image.new("RGBA", fg_rgba.size, bg_color + (0,))
            night.putalpha(alpha)
            night.save(night_path, "PNG", optimize=True)
        changed.append(night_path.name)
    return changed


def create_recfg_from_source(source: Path, output_dir: Path) -> str:
    img = open_image_rgba(source)
    out = paste_fit_on_canvas(img, (SIZE_1x1, SIZE_1x1), (0, 0, 0, 0))
    out.save(output_dir / "recfg.png", "PNG", optimize=True)
    return "recfg.png"


def create_white_recbg(output_dir: Path) -> str:
    Image.new("RGB", (SIZE_1x1, SIZE_1x1), "white").save(output_dir / "recbg.png", "PNG", optimize=True)
    return "recbg.png"


def generate_missing_artplus_icons(output_dir: Path) -> list[str]:
    generated: list[str] = []
    recfg_path = output_dir / "recfg.png"
    if not recfg_path.exists():
        raise PipelineError("无法生成 ART+ 图标：缺少 recfg.png 且 ZIP 中没有可用普通图标")

    recbg_path = output_dir / "recbg.png"
    if not recbg_path.exists():
        generated.append(create_white_recbg(output_dir))

    # 额外生成变形背景/前景，方便直接放入 /data/oplus/uxicons 使用。
    background_sizes = {
        "recbg_1x2.png": SIZE_1x2,
        "recbg_2x1.png": SIZE_2x1,
        "recbg_2x2.png": (SIZE_2x2, SIZE_2x2),
    }
    recbg_img = open_image_rgba(recbg_path).convert("RGB")
    for filename, size in background_sizes.items():
        if not (output_dir / filename).exists():
            recbg_img.resize(size, Image.Resampling.LANCZOS).save(output_dir / filename, "PNG", optimize=True)
            generated.append(filename)

    foreground_sizes = {
        "recfg_1x2.png": SIZE_1x2,
        "recfg_2x1.png": SIZE_2x1,
        "recfg_2x2.png": (SIZE_2x2, SIZE_2x2),
    }
    recfg_img = open_image_rgba(recfg_path)
    for filename, size in foreground_sizes.items():
        if not (output_dir / filename).exists():
            paste_fit_on_canvas(recfg_img, size, (0, 0, 0, 0)).save(output_dir / filename, "PNG", optimize=True)
            generated.append(filename)

    monochrome_generators = {
        "monochrome.png": lambda: create_monochrome_icon(recfg_path, SIZE_1x1),
        "monochrome_1x2.png": lambda: create_monochrome_icon(recfg_path, SIZE_1x2),
        "monochrome_2x1.png": lambda: create_monochrome_icon(recfg_path, SIZE_2x1),
        "monochrome_2x2.png": lambda: create_monochrome_icon(recfg_path, SIZE_2x2),
    }
    for filename, factory in monochrome_generators.items():
        factory().save(output_dir / filename, "PNG", optimize=True)
        generated.append(filename)

    style_generators = {
        "rec_night.png": lambda: create_night_icon(recfg_path, SIZE_1x1),
        "rec_night_1x2.png": lambda: create_night_icon(recfg_path, SIZE_1x2),
        "rec_night_2x1.png": lambda: create_night_icon(recfg_path, SIZE_2x1),
        "rec_night_2x2.png": lambda: create_night_icon(recfg_path, SIZE_2x2),
        "day.png": lambda: create_style_icon(recfg_path, "day", SIZE_1x1),
        "nsd.png": lambda: create_style_icon(recfg_path, "nsd", SIZE_1x1),
        "mat.png": lambda: create_style_icon(recfg_path, "mat", SIZE_1x1),
        "peb.png": lambda: create_style_icon(recfg_path, "peb", SIZE_1x1),
    }
    for filename, factory in style_generators.items():
        if not (output_dir / filename).exists():
            factory().save(output_dir / filename, "PNG", optimize=True)
            generated.append(filename)

    return generated


def validate_output(output_dir: Path) -> None:
    missing = [name for name in REQUIRED_MINIMUM_FILES if not (output_dir / name).exists()]
    if missing:
        raise PipelineError(f"最终目录缺少必要文件: {', '.join(missing)}")
    for name in REQUIRED_MINIMUM_FILES:
        try:
            with Image.open(output_dir / name) as img:
                if img.size != EXPECTED_SIZES[name]:
                    raise PipelineError(f"{name} 尺寸异常: {img.size}, 期望: {EXPECTED_SIZES[name]}")
        except PipelineError:
            raise
        except Exception as exc:
            raise PipelineError(f"{name} 不是有效图片: {exc}") from exc


def process_zip_into_output(zip_path: Path, package_name: str, args: argparse.Namespace, report: RunReport) -> Path:
    output_dir = outputs_latest_dir() / "uxicons" / package_name
    ensure_dir(output_dir)

    unpack_dir = current_run_dir() / "unpacked_zip"
    image_paths = unpack_icon_zip(zip_path, unpack_dir)
    remove_junk_files(unpack_dir)
    if not image_paths:
        raise PipelineError("ZIP 中没有找到可用图片文件（支持 .png/.webp/.jpg/.jpeg）")

    copied = copy_standard_icons(image_paths, output_dir)
    generated: list[str] = []
    source = choose_best_source_icon(image_paths)
    background_source = choose_best_background_source(image_paths, source) if source else None

    need_core_layers = not (output_dir / "recfg.png").exists() or not (output_dir / "recbg.png").exists()
    if need_core_layers and not args.disable_gpt_image2:
        if source and background_source:
            try:
                if args.gpt_image_backend == "direct":
                    generated.extend(direct_gpt_image2_split_source_icon(source, background_source, output_dir, args, report))
                else:
                    generated.extend(gpt_image2_split_source_icon(source, background_source, output_dir, args, report))
            except Exception as exc:
                report["image_generation"] = "local-fallback"
                report["image_generation_error"] = str(exc)
                print(f"  gpt-image-2 生成失败，回退到本地生成: {exc}", file=sys.stderr)
        else:
            report["image_generation"] = "local-fallback"
            report["image_generation_error"] = "no source icon for gpt-image-2"
            print("  未找到可上传给 gpt-image-2 的源图，回退到本地生成。", file=sys.stderr)
    elif need_core_layers and args.disable_gpt_image2:
        report["image_generation"] = "disabled-local"
    else:
        report["image_generation"] = "existing-files"

    if not (output_dir / "recfg.png").exists():
        if not source:
            raise PipelineError("ZIP 中没有 recfg.png，也没有可用于生成 recfg.png 的普通图标")
        generated.append(create_recfg_from_source(source, output_dir))

    if normalize_foreground_subject_size(output_dir / "recfg.png"):
        report["foreground_subject_target_ratio"] = FOREGROUND_SUBJECT_MAX_SIDE_RATIO
        generated.append("recfg.png")

    generated.extend(generate_missing_artplus_icons(output_dir))
    generated.extend(recolor_rec_night_to_background(output_dir))
    remove_junk_files(output_dir)
    validate_output(output_dir)

    final_files = sorted(p.name for p in output_dir.iterdir() if p.is_file())
    report["copied_files"] = sorted(set(copied))
    report["generated_files"] = sorted(set(generated))
    report["final_files"] = final_files
    report["output_dir"] = str(output_dir)
    return output_dir


def write_report(report: RunReport) -> None:
    report["finished_at"] = datetime.now().isoformat(timespec="seconds")
    ensure_dir(reports_dir())
    with open(latest_report_path(), "w", encoding="utf-8") as f:
        json.dump(report, f, ensure_ascii=False, indent=2)


def run(args: argparse.Namespace) -> RunReport:
    report = RunReport()
    report["smb_url"] = args.smb_url
    report["image_service_url"] = args.gpt_image_service_url
    report["image_generation_backend"] = args.gpt_image_backend
    report["image_generation_base_url"] = args.gpt_image_base_url if args.gpt_image_backend == "direct" else args.gpt_image_service_url

    try:
        clean_current_run_dir()
        ensure_dir(current_run_dir())
        archive_previous_outputs(report)

        input_root = resolve_input_dir(args.smb_url, args.input_dir)
        package_dir = resolve_package_dir(input_root) if not args.zip else Path(args.zip).expanduser().resolve().parent
        package_name = package_dir.name
        if not PACKAGE_RE.match(package_name):
            raise PipelineError(f"包名文件夹名不合法: {package_dir}")

        report["input_source_dir"] = str(package_dir)
        zip_path = newest_zip_in_dir(package_dir, args.zip)
        report["zip_file"] = zip_path.name
        report["package_name"] = package_name

        local_zip = copy_zip_to_run_dir(zip_path)
        output_dir = process_zip_into_output(local_zip, package_name, args, report)
        report["success"] = True
        print("\n完成：已生成 OPPO uxicons 目录")
        print(f"  包名: {package_name}")
        print(f"  输出: {output_dir}")
        print(f"  报告: {latest_report_path()}")
    except Exception as exc:
        report["success"] = False
        report["error"] = str(exc)
        print(f"\n错误: {exc}", file=sys.stderr)
    finally:
        clean_current_run_dir()
        write_report(report)
    return report


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="从包名文件夹中的图标 ZIP 生成可直接放入 OPPO /data/oplus/uxicons 的目录"
    )
    parser.add_argument("--smb-url", default=DEFAULT_SMB_URL, help=f"SMB 地址，默认: {DEFAULT_SMB_URL}")
    parser.add_argument(
        "--input-dir",
        help=(
            "已挂载 SMB 后的本地目录；可以是 ArtPlusUpload 根目录，也可以是具体包名文件夹；"
            "不填时自动尝试 /Volumes/Download/ArtPlusUpload 等路径"
        ),
    )
    parser.add_argument("--zip", help="指定图标 ZIP 文件；不填时使用输入目录中修改时间最新的 .zip")
    parser.add_argument(
        "--disable-gpt-image2",
        action="store_true",
        help="禁用默认 gpt-image-2 生图分离，改用本地兜底生成 recfg/recbg",
    )
    parser.add_argument(
        "--gpt-image-service-url",
        default=DEFAULT_GPT_IMAGE_SERVICE_URL,
        help=f"本地生图服务地址，默认: {DEFAULT_GPT_IMAGE_SERVICE_URL}",
    )
    parser.add_argument(
        "--gpt-image-backend",
        choices=("service", "direct"),
        default=DEFAULT_GPT_IMAGE_BACKEND,
        help=(
            "GPT 图片能力后端：service=本地 9714 队列服务；"
            "direct=直接调用 OpenAI 兼容 /images/edits 接口。"
            f"默认: {DEFAULT_GPT_IMAGE_BACKEND}"
        ),
    )
    parser.add_argument(
        "--gpt-image-base-url",
        default=DEFAULT_GPT_IMAGE_DIRECT_BASE_URL,
        help=(
            "direct 后端 Base URL，可填 .../v1 或完整 .../v1/images/edits；"
            f"默认: {DEFAULT_GPT_IMAGE_DIRECT_BASE_URL}"
        ),
    )
    parser.add_argument(
        "--gpt-image-api-key",
        default=DEFAULT_GPT_IMAGE_API_KEY,
        help="direct 后端 API key；也可用 ARTPLUS_GPT_IMAGE_API_KEY 或 OPENAI_API_KEY。",
    )
    parser.add_argument(
        "--gpt-image-insecure-skip-tls-verify",
        action="store_true",
        default=DEFAULT_GPT_IMAGE_INSECURE_SKIP_TLS_VERIFY,
        help="direct 后端跳过 TLS 证书校验；仅用于可信内网或自签名网关。",
    )
    parser.add_argument(
        "--gpt-image-upstream-base-url",
        default=DEFAULT_GPT_IMAGE_UPSTREAM_BASE_URL,
        help=f"gpt-image-2 上游聚合接口 Base URL，默认: {DEFAULT_GPT_IMAGE_UPSTREAM_BASE_URL}",
    )
    parser.add_argument(
        "--gpt-image-model",
        default=DEFAULT_GPT_IMAGE_MODEL,
        help=f"gpt-image 模型名，默认: {DEFAULT_GPT_IMAGE_MODEL}",
    )
    parser.add_argument(
        "--gpt-image-size",
        default=DEFAULT_GPT_IMAGE_SIZE,
        help=f"gpt-image-2 输出尺寸，默认: {DEFAULT_GPT_IMAGE_SIZE}",
    )
    parser.add_argument(
        "--gpt-image-quality",
        default=DEFAULT_GPT_IMAGE_QUALITY,
        help=f"gpt-image-2 输出质量，默认: {DEFAULT_GPT_IMAGE_QUALITY}",
    )
    parser.add_argument(
        "--gpt-image-timeout-seconds",
        type=int,
        default=DEFAULT_GPT_IMAGE_TIMEOUT_SECONDS,
        help=f"等待 gpt-image-2 队列完成的秒数，默认: {DEFAULT_GPT_IMAGE_TIMEOUT_SECONDS}",
    )
    return parser


def main() -> int:
    args = build_parser().parse_args()
    report = run(args)
    return 0 if report.get("success") else 1


if __name__ == "__main__":
    raise SystemExit(main())
