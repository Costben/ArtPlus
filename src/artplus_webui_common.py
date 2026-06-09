#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Shared rules for the ArtPlus WebUI."""

from __future__ import annotations

from io import BytesIO
import re
import struct
import zipfile
from pathlib import Path

from PIL import Image, ImageOps, UnidentifiedImageError


MAX_UPLOAD_BYTES = 200 * 1024 * 1024
RETAIN_JOB_COUNT = 50

PACKAGE_FIND_RE = re.compile(r"[A-Za-z][A-Za-z0-9_]*(?:\.[A-Za-z][A-Za-z0-9_]*)+")
PACKAGE_RE = re.compile(r"^[A-Za-z][A-Za-z0-9_]*(?:\.[A-Za-z][A-Za-z0-9_]*)+$")

KNOWN_FILENAME_SUFFIXES = (
    ".apk_icon",
    "_apk_icon",
    ".appicon",
    "_appicon",
    ".icon",
    "_icon",
)

VARIANTS = {
    "local": {
        "key": "local",
        "label": "本地版",
        "download_suffix": "local",
        "disable_gpt_image2": True,
    },
    "gpt": {
        "key": "gpt",
        "label": "GPT版",
        "download_suffix": "gpt",
        "disable_gpt_image2": False,
    },
}

REQUIRED_PREVIEW_FILES = ("recbg.png", "recfg.png", "rec_night.png")
ORIGINAL_ICON_PREVIEW_FILENAME = "original_icon.png"
IMAGE_MEMBER_SUFFIXES = {".png", ".webp", ".jpg", ".jpeg"}
TERMINAL_VARIANT_STATUSES = {"succeeded", "failed"}


class UploadValidationError(ValueError):
    """Raised when an uploaded ZIP does not meet WebUI constraints."""


def strip_known_suffix(value: str) -> str:
    current = value
    while True:
        lower = current.lower()
        for suffix in KNOWN_FILENAME_SUFFIXES:
            if lower.endswith(suffix):
                current = current[: -len(suffix)]
                break
        else:
            return current.strip("._- ")


def is_valid_package_name(value: str) -> bool:
    return bool(PACKAGE_RE.fullmatch(value))


def extract_package_name(filename: str) -> str:
    """Extract an Android-style package name from an uploaded ZIP filename."""
    name = Path(filename).name
    stem = name[:-4] if name.lower().endswith(".zip") else name

    candidates: list[str] = []
    current = stem.strip()
    for _ in range(6):
        if current and current not in candidates:
            candidates.append(current)
        stripped = strip_known_suffix(current)
        if stripped == current:
            break
        current = stripped

    for candidate in candidates:
        for match in PACKAGE_FIND_RE.finditer(candidate):
            package_name = strip_known_suffix(match.group(0))
            if is_valid_package_name(package_name):
                return package_name

    raise UploadValidationError(f"无法从文件名推断包名: {filename}")


def parse_arsc_package_names(data: bytes) -> list[str]:
    """Parse package names from an Android resources.arsc payload."""
    names: list[str] = []
    seen: set[str] = set()

    def add_name(raw_name: bytes) -> None:
        try:
            decoded = raw_name.decode("utf-16le", errors="ignore").split("\x00", 1)[0].strip()
        except Exception:
            return
        if decoded and is_valid_package_name(decoded) and decoded not in seen:
            names.append(decoded)
            seen.add(decoded)

    def walk_chunks(start: int, end: int, depth: int = 0) -> None:
        if depth > 8:
            return
        offset = start
        while offset + 8 <= end and offset + 8 <= len(data):
            try:
                chunk_type, header_size, chunk_size = struct.unpack_from("<HHI", data, offset)
            except struct.error:
                return
            if header_size < 8 or chunk_size < header_size:
                return
            chunk_end = offset + chunk_size
            if chunk_end > end or chunk_end > len(data):
                return

            # RES_TABLE_PACKAGE_TYPE. Name starts after ResChunk_header + package id.
            if chunk_type == 0x0200 and header_size >= 268 and offset + 268 <= len(data):
                add_name(data[offset + 12 : offset + 268])

            child_start = offset + header_size
            if child_start + 8 <= chunk_end:
                walk_chunks(child_start, chunk_end, depth + 1)
            offset = chunk_end

    walk_chunks(0, len(data))
    return names


def extract_package_name_from_zip(zip_path: Path) -> str:
    """Extract a package name from resources.arsc files inside an uploaded ZIP."""
    candidates: list[str] = []

    def add_candidates(names: list[str]) -> None:
        for name in names:
            if name not in candidates:
                candidates.append(name)

    try:
        with zipfile.ZipFile(zip_path, "r") as zip_ref:
            infos = [info for info in zip_ref.infolist() if not info.is_dir() and is_safe_zip_member(info.filename)]

            for info in infos:
                name = Path(info.filename).name.lower()
                if name in {"resources.arsc", "resource.arsc"}:
                    add_candidates(parse_arsc_package_names(zip_ref.read(info)))

            for info in infos:
                if Path(info.filename).suffix.lower() != ".apk":
                    continue
                try:
                    with zipfile.ZipFile(BytesIO(zip_ref.read(info)), "r") as apk_ref:
                        try:
                            add_candidates(parse_arsc_package_names(apk_ref.read("resources.arsc")))
                        except KeyError:
                            continue
                except zipfile.BadZipFile:
                    continue
    except zipfile.BadZipFile as exc:
        raise UploadValidationError("ZIP 文件损坏或不可读取") from exc

    if len(candidates) == 1:
        return candidates[0]
    if len(candidates) > 1:
        raise UploadValidationError(f"ZIP 中发现多个 resources.arsc 包名: {', '.join(candidates)}")
    raise UploadValidationError("无法从 ZIP 中的 resources.arsc 推断包名")


def resolve_package_name(filename: str, zip_path: Path) -> str:
    """Resolve package name from filename first, then resources.arsc content."""
    try:
        return extract_package_name(filename)
    except UploadValidationError:
        return extract_package_name_from_zip(zip_path)


def ensure_upload_size_allowed(size: int) -> None:
    if size > MAX_UPLOAD_BYTES:
        limit_mb = MAX_UPLOAD_BYTES // (1024 * 1024)
        raise UploadValidationError(f"上传文件超过 {limit_mb}MB 上限")


def is_safe_zip_member(name: str) -> bool:
    path = Path(name)
    return not path.is_absolute() and ".." not in path.parts


def validate_upload_zip(zip_path: Path) -> None:
    """Validate the uploaded ZIP without extracting it."""
    package_dirs: set[str] = set()
    try:
        with zipfile.ZipFile(zip_path, "r") as zip_ref:
            for info in zip_ref.infolist():
                if not is_safe_zip_member(info.filename):
                    raise UploadValidationError(f"ZIP 包含不安全路径: {info.filename}")
                parts = [part for part in Path(info.filename).parts if part not in {"", "."}]
                if not parts or parts[0] == "__MACOSX":
                    continue
                if len(parts) == 1:
                    continue
                top_level = strip_known_suffix(parts[0])
                if is_valid_package_name(top_level):
                    package_dirs.add(top_level)
    except zipfile.BadZipFile as exc:
        raise UploadValidationError("ZIP 文件损坏或不可读取") from exc

    if len(package_dirs) > 1:
        names = ", ".join(sorted(package_dirs))
        raise UploadValidationError(f"暂不支持一个 ZIP 包含多个应用目录: {names}")


def _icon_member_rank(member_name: str) -> int:
    parts = [part for part in Path(member_name).parts if part not in {"", "."}]
    basename = Path(member_name).name.lower()
    stem = Path(member_name).stem.lower()
    depth = len(parts)

    if depth == 1 and basename == "icon.png":
        return 0
    if depth <= 2 and basename == "icon.png":
        return 1
    if depth <= 2 and stem in {"ic_launcher", "launcher", "icon"}:
        return 2
    if "launcher" in stem or "icon" in stem:
        return 3
    return 4


def extract_original_icon_preview(zip_path: Path, output_path: Path, size: int = 240) -> Path | None:
    """Extract the original app icon from an upload ZIP as a normalized preview PNG.

    This is best-effort: invalid image members are skipped because upload validation
    only checks ZIP structure, not image bytes.
    """
    best: tuple[tuple[int, int, int, str], Image.Image] | None = None

    with zipfile.ZipFile(zip_path, "r") as zip_ref:
        for info in zip_ref.infolist():
            if info.is_dir() or not is_safe_zip_member(info.filename):
                continue
            if Path(info.filename).parts[:1] == ("__MACOSX",):
                continue
            if Path(info.filename).suffix.lower() not in IMAGE_MEMBER_SUFFIXES:
                continue
            try:
                data = zip_ref.read(info)
                image = Image.open(BytesIO(data))
                image.load()
            except (OSError, UnidentifiedImageError, zipfile.BadZipFile):
                continue

            image = ImageOps.exif_transpose(image).convert("RGBA")
            width, height = image.size
            if width <= 0 or height <= 0:
                continue

            rank = _icon_member_rank(info.filename)
            depth = len([part for part in Path(info.filename).parts if part not in {"", "."}])
            score = (rank, -(width * height), depth, info.filename.lower())
            if best is None or score < best[0]:
                best = (score, image)

    if best is None:
        return None

    preview = ImageOps.fit(best[1], (size, size), method=Image.Resampling.LANCZOS, centering=(0.5, 0.5))
    output_path.parent.mkdir(parents=True, exist_ok=True)
    preview.save(output_path, "PNG", optimize=True)
    return output_path
