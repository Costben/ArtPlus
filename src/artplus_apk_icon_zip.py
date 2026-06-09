#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Fast APK launcher icon extraction for ArtPlus uploads.

The extractor treats an APK as a ZIP and copies only manifest, resources.arsc,
and launcher-icon-related resources. It intentionally avoids apktool-style full
decode/decompile so it stays close to the speed profile of Android file managers.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from io import BytesIO
import re
import struct
import zipfile
from pathlib import Path
from typing import Iterable

from PIL import Image, ImageOps, UnidentifiedImageError

from artplus_webui_common import (
    IMAGE_MEMBER_SUFFIXES,
    UploadValidationError,
    is_safe_zip_member,
    is_valid_package_name,
    parse_arsc_package_names,
)


RESOURCE_VALUE_STRING = 0x03
RESOURCE_VALUE_REFERENCE = 0x01
RESOURCE_NO_ENTRY = 0xFFFFFFFF
XML_START_ELEMENT = 0x0102
RES_STRING_POOL_TYPE = 0x0001
RES_TABLE_PACKAGE_TYPE = 0x0200
RES_TABLE_TYPE_TYPE = 0x0201

APK_ICON_MEMBER_SUFFIXES = IMAGE_MEMBER_SUFFIXES | {".xml"}
ANDROID_ICON_ATTRS = {"icon", "roundIcon"}
ADAPTIVE_ICON_ATTRS = {"drawable"}


class ApkIconExtractionError(ValueError):
    """Raised when an APK cannot be converted into an icon ZIP."""


@dataclass(frozen=True)
class BinaryXmlAttr:
    name: str
    value: str | None
    data_type: int
    data: int


@dataclass(frozen=True)
class BinaryXmlTag:
    name: str
    attrs: tuple[BinaryXmlAttr, ...]


@dataclass
class ApkIconExtractionResult:
    apk_path: Path
    output_zip: Path
    package_name: str
    selected_icon_path: str | None
    extracted_members: list[str] = field(default_factory=list)
    generated_members: list[str] = field(default_factory=list)


def _chunk_header(data: bytes, offset: int) -> tuple[int, int, int]:
    if offset + 8 > len(data):
        raise ApkIconExtractionError("Android resource chunk is truncated")
    chunk_type, header_size, chunk_size = struct.unpack_from("<HHI", data, offset)
    if header_size < 8 or chunk_size < header_size or offset + chunk_size > len(data):
        raise ApkIconExtractionError("Android resource chunk header is invalid")
    return chunk_type, header_size, chunk_size


def _read_utf8_length(data: bytes, offset: int) -> tuple[int, int]:
    first = data[offset]
    if first & 0x80:
        return ((first & 0x7F) << 8) | data[offset + 1], offset + 2
    return first, offset + 1


def _read_utf16_length(data: bytes, offset: int) -> tuple[int, int]:
    first = struct.unpack_from("<H", data, offset)[0]
    if first & 0x8000:
        second = struct.unpack_from("<H", data, offset + 2)[0]
        return ((first & 0x7FFF) << 16) | second, offset + 4
    return first, offset + 2


def _parse_string_pool(data: bytes, offset: int) -> tuple[list[str], int]:
    chunk_type, header_size, chunk_size = _chunk_header(data, offset)
    if chunk_type != RES_STRING_POOL_TYPE or header_size < 28:
        raise ApkIconExtractionError("Expected Android string pool")
    string_count, _style_count, flags, strings_start, _styles_start = struct.unpack_from("<IIIII", data, offset + 8)
    offsets_start = offset + header_size
    strings_base = offset + strings_start
    is_utf8 = bool(flags & 0x00000100)
    strings: list[str] = []

    for index in range(string_count):
        string_offset = struct.unpack_from("<I", data, offsets_start + index * 4)[0]
        cursor = strings_base + string_offset
        try:
            if is_utf8:
                _utf16_len, cursor = _read_utf8_length(data, cursor)
                byte_len, cursor = _read_utf8_length(data, cursor)
                raw = data[cursor : cursor + byte_len]
                strings.append(raw.decode("utf-8", errors="replace"))
            else:
                char_len, cursor = _read_utf16_length(data, cursor)
                raw = data[cursor : cursor + char_len * 2]
                strings.append(raw.decode("utf-16le", errors="replace"))
        except Exception:
            strings.append("")

    return strings, offset + chunk_size


def _string_at(strings: list[str], index: int) -> str | None:
    if 0 <= index < len(strings):
        return strings[index]
    return None


def parse_binary_xml(data: bytes) -> list[BinaryXmlTag]:
    """Parse enough binary XML to read start tags and attributes."""
    chunk_type, header_size, chunk_size = _chunk_header(data, 0)
    if chunk_type != 0x0003:
        raise ApkIconExtractionError("Expected Android binary XML")

    strings: list[str] = []
    tags: list[BinaryXmlTag] = []
    offset = header_size
    end = min(chunk_size, len(data))
    while offset + 8 <= end:
        current_type, current_header_size, current_size = _chunk_header(data, offset)
        current_end = offset + current_size
        if current_type == RES_STRING_POOL_TYPE:
            strings, _ = _parse_string_pool(data, offset)
        elif current_type == XML_START_ELEMENT and current_header_size >= 16 and offset + 36 <= current_end:
            ext_offset = offset + current_header_size
            _ns_idx, name_idx = struct.unpack_from("<II", data, ext_offset)
            tag_name = _string_at(strings, name_idx) or ""
            attr_start, attr_size, attr_count, _id_idx, _class_idx, _style_idx = struct.unpack_from(
                "<HHHHHH", data, ext_offset + 8
            )
            attr_offset = ext_offset + attr_start
            attrs: list[BinaryXmlAttr] = []
            for attr_index in range(attr_count):
                item_offset = attr_offset + attr_index * attr_size
                if item_offset + 20 > current_end:
                    break
                _attr_ns, attr_name_idx, raw_value_idx = struct.unpack_from("<III", data, item_offset)
                _value_size, _value_res0, data_type, value_data = struct.unpack_from("<HBBI", data, item_offset + 12)
                attr_name = _string_at(strings, attr_name_idx) or ""
                raw_value = _string_at(strings, raw_value_idx) if raw_value_idx != RESOURCE_NO_ENTRY else None
                if raw_value is None and data_type == RESOURCE_VALUE_STRING:
                    raw_value = _string_at(strings, value_data)
                elif raw_value is None and data_type == RESOURCE_VALUE_REFERENCE:
                    raw_value = f"@{value_data:08X}"
                attrs.append(BinaryXmlAttr(attr_name, raw_value, data_type, value_data))
            tags.append(BinaryXmlTag(tag_name, tuple(attrs)))
        offset = current_end
    return tags


def _decode_utf16le_name(data: bytes) -> str:
    return data.decode("utf-16le", errors="ignore").split("\x00", 1)[0].strip()


def parse_resource_path_map(resources_arsc: bytes) -> dict[int, list[str]]:
    """Map Android resource IDs to ZIP member paths such as res/mipmap-.../icon.webp."""
    mapping: dict[int, list[str]] = {}
    try:
        table_type, table_header_size, table_size = _chunk_header(resources_arsc, 0)
    except ApkIconExtractionError:
        return mapping
    if table_type != 0x0002:
        return mapping

    offset = table_header_size
    global_strings: list[str] = []
    end = min(table_size, len(resources_arsc))
    while offset + 8 <= end:
        try:
            chunk_type, header_size, chunk_size = _chunk_header(resources_arsc, offset)
        except ApkIconExtractionError:
            break
        if chunk_type == RES_STRING_POOL_TYPE and not global_strings:
            try:
                global_strings, _ = _parse_string_pool(resources_arsc, offset)
            except ApkIconExtractionError:
                pass
        elif chunk_type == RES_TABLE_PACKAGE_TYPE:
            _parse_package_chunk(resources_arsc, offset, header_size, chunk_size, global_strings, mapping)
        offset += chunk_size
    return mapping


def _parse_package_chunk(
    data: bytes,
    offset: int,
    header_size: int,
    chunk_size: int,
    global_strings: list[str],
    mapping: dict[int, list[str]],
) -> None:
    if header_size < 288 or offset + header_size > len(data):
        return
    package_id = struct.unpack_from("<I", data, offset + 8)[0]
    if package_id == 0:
        package_id = 0x7F
    type_strings_offset = struct.unpack_from("<I", data, offset + 268)[0]
    key_strings_offset = struct.unpack_from("<I", data, offset + 276)[0]
    type_id_offset = 0
    if header_size >= 288 and offset + 288 <= len(data):
        type_id_offset = struct.unpack_from("<I", data, offset + 284)[0]

    try:
        type_strings, _ = _parse_string_pool(data, offset + type_strings_offset)
        key_strings, _ = _parse_string_pool(data, offset + key_strings_offset)
    except ApkIconExtractionError:
        return

    child_offset = offset + header_size
    package_end = offset + chunk_size
    while child_offset + 8 <= package_end:
        try:
            child_type, child_header_size, child_size = _chunk_header(data, child_offset)
        except ApkIconExtractionError:
            break
        if child_type == RES_TABLE_TYPE_TYPE:
            _parse_type_chunk(
                data,
                child_offset,
                child_header_size,
                child_size,
                package_id,
                type_id_offset,
                type_strings,
                key_strings,
                global_strings,
                mapping,
            )
        child_offset += child_size


def _parse_type_chunk(
    data: bytes,
    offset: int,
    header_size: int,
    chunk_size: int,
    package_id: int,
    type_id_offset: int,
    type_strings: list[str],
    key_strings: list[str],
    global_strings: list[str],
    mapping: dict[int, list[str]],
) -> None:
    if header_size < 20 or offset + header_size > len(data):
        return
    type_id = data[offset + 8]
    if type_id <= 0:
        return
    entry_count, entries_start = struct.unpack_from("<II", data, offset + 12)
    offsets_start = offset + header_size
    entries_base = offset + entries_start
    chunk_end = offset + chunk_size
    if offsets_start + entry_count * 4 > chunk_end:
        return

    # Touch the type/key string pools to validate indexes while keeping the ID mapping authoritative.
    _ = _string_at(type_strings, type_id - 1 + type_id_offset)
    for entry_index in range(entry_count):
        entry_offset = struct.unpack_from("<I", data, offsets_start + entry_index * 4)[0]
        if entry_offset == RESOURCE_NO_ENTRY:
            continue
        entry_pos = entries_base + entry_offset
        if entry_pos + 8 > chunk_end:
            continue
        entry_size, entry_flags, key_index = struct.unpack_from("<HHI", data, entry_pos)
        if not _string_at(key_strings, key_index):
            continue
        if entry_flags & 0x0001:
            continue
        value_pos = entry_pos + entry_size
        if value_pos + 8 > chunk_end:
            continue
        _value_size, _value_res0, value_type, value_data = struct.unpack_from("<HBBI", data, value_pos)
        if value_type != RESOURCE_VALUE_STRING:
            continue
        path = _string_at(global_strings, value_data)
        if not path or not path.startswith("res/"):
            continue
        resource_id = (package_id << 24) | (type_id << 16) | entry_index
        mapping.setdefault(resource_id, [])
        if path not in mapping[resource_id]:
            mapping[resource_id].append(path)


def _resource_refs_from_attrs(attrs: Iterable[BinaryXmlAttr], attr_names: set[str]) -> list[int]:
    refs: list[int] = []
    for attr in attrs:
        if attr.name not in attr_names:
            continue
        if attr.data_type == RESOURCE_VALUE_REFERENCE and attr.data:
            refs.append(attr.data)
            continue
        if attr.value:
            match = re.fullmatch(r"@(?:0x)?([0-9A-Fa-f]{8})", attr.value)
            if match:
                refs.append(int(match.group(1), 16))
    return refs


def parse_manifest_icon_refs(manifest_data: bytes) -> tuple[str | None, list[int]]:
    package_name: str | None = None
    icon_refs: list[int] = []
    try:
        tags = parse_binary_xml(manifest_data)
    except ApkIconExtractionError:
        return None, []
    for tag in tags:
        if tag.name == "manifest":
            for attr in tag.attrs:
                if attr.name == "package" and attr.value and is_valid_package_name(attr.value):
                    package_name = attr.value
        elif tag.name == "application":
            icon_refs.extend(_resource_refs_from_attrs(tag.attrs, ANDROID_ICON_ATTRS))
            break
    return package_name, _dedupe_ints(icon_refs)


def parse_drawable_refs_from_binary_xml(data: bytes) -> list[int]:
    refs: list[int] = []
    try:
        tags = parse_binary_xml(data)
    except ApkIconExtractionError:
        return refs
    for tag in tags:
        refs.extend(_resource_refs_from_attrs(tag.attrs, ADAPTIVE_ICON_ATTRS))
    return _dedupe_ints(refs)


def _dedupe_ints(values: Iterable[int]) -> list[int]:
    result: list[int] = []
    seen: set[int] = set()
    for value in values:
        if value not in seen:
            result.append(value)
            seen.add(value)
    return result


def _dedupe_strings(values: Iterable[str]) -> list[str]:
    result: list[str] = []
    seen: set[str] = set()
    for value in values:
        if value not in seen:
            result.append(value)
            seen.add(value)
    return result


def package_name_from_apk(apk_path: Path) -> str:
    with zipfile.ZipFile(apk_path, "r") as apk_ref:
        manifest_name, _icon_refs = parse_manifest_icon_refs(apk_ref.read("AndroidManifest.xml"))
        if manifest_name:
            return manifest_name
        try:
            arsc_names = parse_arsc_package_names(apk_ref.read("resources.arsc"))
        except KeyError:
            arsc_names = []
    if len(arsc_names) == 1:
        return arsc_names[0]
    if len(arsc_names) > 1:
        raise ApkIconExtractionError(f"APK 中发现多个 resources.arsc 包名: {', '.join(arsc_names)}")
    filename_match = re.search(r"[A-Za-z][A-Za-z0-9_]*(?:\.[A-Za-z][A-Za-z0-9_]*)+", apk_path.name)
    if filename_match and is_valid_package_name(filename_match.group(0)):
        return filename_match.group(0)
    raise ApkIconExtractionError("无法从 APK 推断包名")


def _resolve_resource_refs(
    apk_ref: zipfile.ZipFile,
    resource_map: dict[int, list[str]],
    refs: Iterable[int],
    max_depth: int = 4,
) -> list[str]:
    selected: list[str] = []
    queue: list[tuple[int, int]] = [(ref, 0) for ref in refs]
    seen_refs: set[int] = set()
    while queue:
        ref, depth = queue.pop(0)
        if ref in seen_refs or depth > max_depth:
            continue
        seen_refs.add(ref)
        for path in resource_map.get(ref, []):
            selected.append(path)
            if Path(path).suffix.lower() != ".xml" or path not in apk_ref.namelist():
                continue
            try:
                nested_refs = parse_drawable_refs_from_binary_xml(apk_ref.read(path))
            except Exception:
                nested_refs = []
            queue.extend((nested_ref, depth + 1) for nested_ref in nested_refs)
    return _dedupe_strings(selected)


def _looks_like_launcher_member(name: str) -> bool:
    path = Path(name)
    if len(path.parts) < 3 or path.parts[0] != "res":
        return False
    if path.suffix.lower() not in APK_ICON_MEMBER_SUFFIXES:
        return False
    folder = path.parts[1].lower()
    if not (folder.startswith("mipmap") or folder.startswith("drawable")):
        return False
    stem = path.stem.lower()
    exact = {
        "ic_launcher",
        "ic_launcher_round",
        "ic_launcher_foreground",
        "ic_launcher_background",
        "ic_foreground",
        "ic_background",
        "ic_app_icon",
        "app_icon",
        "appicon",
        "icon",
        "logo",
    }
    if stem in exact:
        return True
    return "launcher" in stem or stem.endswith("_launcher") or stem.endswith("_app_icon")


def _density_rank(folder: str) -> int:
    folder = folder.lower()
    if "xxxhdpi" in folder:
        return 600
    if "xxhdpi" in folder:
        return 500
    if "xhdpi" in folder:
        return 400
    if "hdpi" in folder:
        return 300
    if "mdpi" in folder:
        return 200
    if "nodpi" in folder:
        return 150
    if "anydpi" in folder:
        return 100
    return 0


def _launcher_member_score(name: str, apk_ref: zipfile.ZipFile) -> tuple[int, int, int, int, str]:
    path = Path(name)
    stem = path.stem.lower()
    folder = path.parts[1].lower() if len(path.parts) > 1 else ""
    suffix = path.suffix.lower()
    priority = 0
    if stem == "ic_launcher":
        priority += 500
    if "launcher" in stem:
        priority += 400
    if stem in {"icon", "app_icon", "appicon"}:
        priority += 180
    if "round" in stem:
        priority -= 25
    if "foreground" in stem:
        priority -= 120
    if "background" in stem:
        priority -= 160
    if "monochrome" in stem or "mono" in stem:
        priority -= 240
    if folder.startswith("mipmap"):
        priority += 90
    if "nodpi" in folder:
        priority += 45
    if suffix == ".xml":
        priority -= 80

    try:
        file_size = apk_ref.getinfo(name).file_size
    except KeyError:
        file_size = 0
    return priority, _density_rank(folder), file_size, -len(path.parts), name


def _select_icon_source(paths: Iterable[str], apk_ref: zipfile.ZipFile) -> str | None:
    image_paths = [path for path in paths if Path(path).suffix.lower() in IMAGE_MEMBER_SUFFIXES and path in apk_ref.namelist()]
    if not image_paths:
        return None
    image_paths.sort(key=lambda path: _launcher_member_score(path, apk_ref), reverse=True)
    return image_paths[0]


def _make_icon_png(apk_ref: zipfile.ZipFile, source_path: str) -> bytes:
    try:
        with Image.open(BytesIO(apk_ref.read(source_path))) as img:
            image = ImageOps.exif_transpose(img).convert("RGBA")
            image = ImageOps.fit(image, (512, 512), method=Image.Resampling.LANCZOS, centering=(0.5, 0.5))
            out = BytesIO()
            image.save(out, "PNG", optimize=True)
            return out.getvalue()
    except (OSError, UnidentifiedImageError) as exc:
        raise ApkIconExtractionError(f"无法读取 launcher 图标图片: {source_path}") from exc


def _top_level_icon_member(source_path: str, normalize_icon_png: bool) -> str:
    if normalize_icon_png:
        return "icon.png"
    suffix = Path(source_path).suffix.lower()
    if suffix in IMAGE_MEMBER_SUFFIXES:
        return f"icon{suffix}"
    return "icon.png"


def _top_level_icon_bytes(apk_ref: zipfile.ZipFile, source_path: str, normalize_icon_png: bool) -> bytes:
    if normalize_icon_png:
        return _make_icon_png(apk_ref, source_path)
    return apk_ref.read(source_path)


def extract_apk_icon_zip(
    apk_path: Path,
    output_zip: Path | None = None,
    package_name: str | None = None,
    normalize_icon_png: bool = False,
    compress_output: bool = False,
) -> ApkIconExtractionResult:
    apk_path = Path(apk_path).expanduser().resolve()
    if not apk_path.is_file() or apk_path.suffix.lower() != ".apk":
        raise ApkIconExtractionError(f"APK 不存在或后缀不是 .apk: {apk_path}")

    output_zip = output_zip or apk_path.with_suffix(".apk_icon.zip")
    output_zip = Path(output_zip).expanduser().resolve()

    try:
        with zipfile.ZipFile(apk_path, "r") as apk_ref:
            names = set(apk_ref.namelist())
            if "AndroidManifest.xml" not in names:
                raise ApkIconExtractionError("APK 缺少 AndroidManifest.xml")
            if "resources.arsc" not in names:
                raise ApkIconExtractionError("APK 缺少 resources.arsc")

            manifest_package, icon_refs = parse_manifest_icon_refs(apk_ref.read("AndroidManifest.xml"))
            arsc_data = apk_ref.read("resources.arsc")
            resource_map = parse_resource_path_map(arsc_data)
            resolved_package = package_name or manifest_package
            if not resolved_package:
                arsc_names = parse_arsc_package_names(arsc_data)
                if len(arsc_names) == 1:
                    resolved_package = arsc_names[0]
                elif len(arsc_names) > 1:
                    raise ApkIconExtractionError(f"APK 中发现多个 resources.arsc 包名: {', '.join(arsc_names)}")
            if not resolved_package or not is_valid_package_name(resolved_package):
                raise ApkIconExtractionError("无法从 APK 推断包名")

            selected_members = _resolve_resource_refs(apk_ref, resource_map, icon_refs)
            heuristic_members = [name for name in names if _looks_like_launcher_member(name)]
            all_members = _dedupe_strings([*selected_members, *heuristic_members])
            all_members = [
                name
                for name in all_members
                if name in names and is_safe_zip_member(name) and Path(name).suffix.lower() in APK_ICON_MEMBER_SUFFIXES
            ]
            if not all_members:
                raise ApkIconExtractionError("APK 中没有找到 launcher 图标资源")

            selected_icon_path = _select_icon_source(selected_members, apk_ref) or _select_icon_source(all_members, apk_ref)
            if not selected_icon_path:
                raise ApkIconExtractionError("APK 中没有找到可直接读取的 launcher 图片")
            icon_member = _top_level_icon_member(selected_icon_path, normalize_icon_png)
            icon_data = _top_level_icon_bytes(apk_ref, selected_icon_path, normalize_icon_png)

            output_zip.parent.mkdir(parents=True, exist_ok=True)
            compression = zipfile.ZIP_DEFLATED if compress_output else zipfile.ZIP_STORED
            with zipfile.ZipFile(output_zip, "w", compression) as zip_out:
                zip_out.writestr(icon_member, icon_data)
                zip_out.writestr("AndroidManifest.xml", apk_ref.read("AndroidManifest.xml"))
                zip_out.writestr("resources.arsc", arsc_data)
                for member in sorted(all_members):
                    zip_out.writestr(member, apk_ref.read(member))

            return ApkIconExtractionResult(
                apk_path=apk_path,
                output_zip=output_zip,
                package_name=resolved_package,
                selected_icon_path=selected_icon_path,
                extracted_members=sorted(all_members),
                generated_members=[icon_member],
            )
    except zipfile.BadZipFile as exc:
        raise ApkIconExtractionError("APK 文件损坏或不可读取") from exc


def validate_apk_upload(apk_path: Path) -> None:
    try:
        with zipfile.ZipFile(apk_path, "r") as apk_ref:
            names = set(apk_ref.namelist())
    except zipfile.BadZipFile as exc:
        raise UploadValidationError("APK 文件损坏或不可读取") from exc
    if "AndroidManifest.xml" not in names or "resources.arsc" not in names:
        raise UploadValidationError("APK 缺少 AndroidManifest.xml 或 resources.arsc")
