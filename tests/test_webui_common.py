#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from __future__ import annotations

import sys
import tempfile
import unittest
import zipfile
from io import BytesIO
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parents[1]
SRC_DIR = PROJECT_ROOT / "src"
if str(SRC_DIR) not in sys.path:
    sys.path.insert(0, str(SRC_DIR))

from artplus_webui_common import (  # noqa: E402
    MAX_UPLOAD_BYTES,
    UploadValidationError,
    ensure_upload_size_allowed,
    extract_package_name_from_zip,
    extract_package_name,
    extract_original_icon_preview,
    parse_arsc_package_names,
    resolve_package_name,
    validate_upload_zip,
)
from artplus_apk_icon_zip import (  # noqa: E402
    extract_apk_icon_zip,
    package_name_from_apk,
    parse_manifest_icon_refs,
    parse_resource_path_map,
)

try:
    from PIL import Image
except ImportError:  # pragma: no cover - dependency install is verified separately
    Image = None


def png_bytes(size: tuple[int, int], color: tuple[int, int, int, int]) -> bytes:
    if Image is None:
        raise unittest.SkipTest("Pillow not installed")
    buffer = BytesIO()
    Image.new("RGBA", size, color).save(buffer, "PNG")
    return buffer.getvalue()


class PackageNameTests(unittest.TestCase):
    def test_extracts_strict_package_zip(self) -> None:
        self.assertEqual(extract_package_name("com.example.app.zip"), "com.example.app")

    def test_extracts_appicon_suffix(self) -> None:
        self.assertEqual(extract_package_name("com.example.app.appicon.zip"), "com.example.app")

    def test_extracts_underscore_icon_suffix(self) -> None:
        self.assertEqual(extract_package_name("com.example.app_icon.zip"), "com.example.app")

    def test_extracts_apk_icon_suffix_with_chinese_prefix(self) -> None:
        self.assertEqual(extract_package_name("应用名_com.example.app.apk_icon.zip"), "com.example.app")

    def test_rejects_filename_without_package(self) -> None:
        with self.assertRaises(UploadValidationError):
            extract_package_name("应用图标.zip")

    def test_parses_package_name_from_real_resources_arsc(self) -> None:
        apk_path = PROJECT_ROOT / "tests" / "com.catchingnow.np.apk"
        with zipfile.ZipFile(apk_path, "r") as apk_ref:
            names = parse_arsc_package_names(apk_ref.read("resources.arsc"))
        self.assertIn("com.catchingnow.np", names)

    def test_extracts_package_name_from_zip_resource_arsc(self) -> None:
        apk_path = PROJECT_ROOT / "tests" / "com.catchingnow.np.apk"
        with zipfile.ZipFile(apk_path, "r") as apk_ref:
            arsc = apk_ref.read("resources.arsc")
        with tempfile.TemporaryDirectory() as tmp:
            zip_path = Path(tmp) / "无包名.zip"
            with zipfile.ZipFile(zip_path, "w") as zip_ref:
                zip_ref.writestr("payload/resource.arsc", arsc)
                zip_ref.writestr("icon.png", b"fake")
            self.assertEqual(extract_package_name_from_zip(zip_path), "com.catchingnow.np")
            self.assertEqual(resolve_package_name(zip_path.name, zip_path), "com.catchingnow.np")

    def test_accepts_top_level_icon_and_resources_arsc(self) -> None:
        apk_path = PROJECT_ROOT / "tests" / "com.catchingnow.np.apk"
        with zipfile.ZipFile(apk_path, "r") as apk_ref:
            arsc = apk_ref.read("resources.arsc")
        with tempfile.TemporaryDirectory() as tmp:
            zip_path = Path(tmp) / "上传图标.zip"
            with zipfile.ZipFile(zip_path, "w") as zip_ref:
                zip_ref.writestr("resources.arsc", arsc)
                zip_ref.writestr("icon.png", b"fake")
            validate_upload_zip(zip_path)
            self.assertEqual(resolve_package_name(zip_path.name, zip_path), "com.catchingnow.np")

    def test_extracts_package_name_from_nested_apk(self) -> None:
        apk_path = PROJECT_ROOT / "tests" / "com.catchingnow.np.apk"
        with tempfile.TemporaryDirectory() as tmp:
            zip_path = Path(tmp) / "图标.zip"
            with zipfile.ZipFile(zip_path, "w") as zip_ref:
                zip_ref.write(apk_path, "nested/app.apk")
                zip_ref.writestr("icon.png", b"fake")
            self.assertEqual(resolve_package_name(zip_path.name, zip_path), "com.catchingnow.np")


class UploadValidationTests(unittest.TestCase):
    def test_upload_size_limit(self) -> None:
        ensure_upload_size_allowed(MAX_UPLOAD_BYTES)
        with self.assertRaises(UploadValidationError):
            ensure_upload_size_allowed(MAX_UPLOAD_BYTES + 1)

    def test_rejects_zip_slip_member(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            zip_path = Path(tmp) / "com.example.app.zip"
            with zipfile.ZipFile(zip_path, "w") as zip_ref:
                zip_ref.writestr("../bad.png", b"bad")
            with self.assertRaises(UploadValidationError):
                validate_upload_zip(zip_path)

    def test_rejects_multiple_package_directories(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            zip_path = Path(tmp) / "com.example.app.zip"
            with zipfile.ZipFile(zip_path, "w") as zip_ref:
                zip_ref.writestr("com.example.one/icon.png", b"one")
                zip_ref.writestr("com.example.two/icon.png", b"two")
            with self.assertRaises(UploadValidationError):
                validate_upload_zip(zip_path)

    def test_accepts_single_app_zip(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            zip_path = Path(tmp) / "com.example.app.zip"
            with zipfile.ZipFile(zip_path, "w") as zip_ref:
                zip_ref.writestr("icon.png", b"not validated as image here")
            validate_upload_zip(zip_path)


class ApkIconZipExtractionTests(unittest.TestCase):
    def test_extracts_package_name_from_real_apk(self) -> None:
        apk_path = PROJECT_ROOT / "tests" / "com.catchingnow.np.apk"

        self.assertEqual(package_name_from_apk(apk_path), "com.catchingnow.np")

    def test_parses_manifest_icon_refs_and_resource_paths(self) -> None:
        apk_path = PROJECT_ROOT / "tests" / "com.catchingnow.np.apk"
        with zipfile.ZipFile(apk_path, "r") as apk_ref:
            package_name, icon_refs = parse_manifest_icon_refs(apk_ref.read("AndroidManifest.xml"))
            path_map = parse_resource_path_map(apk_ref.read("resources.arsc"))

        self.assertEqual(package_name, "com.catchingnow.np")
        self.assertIn(0x7F0F0003, icon_refs)
        self.assertIn("res/mipmap-xxxhdpi-v4/ic_launcher.webp", path_map[0x7F0F0003])

    def test_converts_apk_to_icon_zip_elements(self) -> None:
        apk_path = PROJECT_ROOT / "tests" / "com.catchingnow.np.apk"
        with tempfile.TemporaryDirectory() as tmp:
            output_zip = Path(tmp) / "icon.zip"

            result = extract_apk_icon_zip(apk_path, output_zip)

            self.assertEqual(result.package_name, "com.catchingnow.np")
            self.assertEqual(result.selected_icon_path, "res/mipmap-xxxhdpi-v4/ic_launcher.webp")
            with zipfile.ZipFile(output_zip, "r") as zip_ref:
                names = set(zip_ref.namelist())
            self.assertIn("icon.webp", names)
            self.assertIn("AndroidManifest.xml", names)
            self.assertIn("resources.arsc", names)
            self.assertIn("res/mipmap-anydpi-v26/ic_launcher.xml", names)
            self.assertIn("res/mipmap-xxxhdpi-v4/ic_launcher.webp", names)
            self.assertIn("res/mipmap-xxxhdpi-v4/ic_foreground.webp", names)
            self.assertIn("res/mipmap-xxxhdpi-v4/ic_background.webp", names)


@unittest.skipIf(Image is None, "Pillow not installed")
class OriginalIconPreviewTests(unittest.TestCase):
    def test_extracts_top_level_original_icon_for_preview(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = Path(tmp)
            zip_path = tmp_path / "com.example.app.zip"
            output_path = tmp_path / "preview" / "original_icon.png"
            with zipfile.ZipFile(zip_path, "w") as zip_ref:
                zip_ref.writestr("res/large.webp", png_bytes((512, 512), (255, 0, 0, 255)))
                zip_ref.writestr("icon.png", png_bytes((64, 64), (0, 0, 0, 255)))

            result = extract_original_icon_preview(zip_path, output_path)
            self.assertEqual(result, output_path)
            self.assertTrue(output_path.is_file())
            image = Image.open(output_path).convert("RGBA")
            self.assertEqual(image.size, (240, 240))
            self.assertEqual(image.getpixel((120, 120)), (0, 0, 0, 255))

    def test_skips_invalid_image_members(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = Path(tmp)
            zip_path = tmp_path / "com.example.app.zip"
            output_path = tmp_path / "preview" / "original_icon.png"
            with zipfile.ZipFile(zip_path, "w") as zip_ref:
                zip_ref.writestr("icon.png", b"not a real image")

            self.assertIsNone(extract_original_icon_preview(zip_path, output_path))
            self.assertFalse(output_path.exists())


if __name__ == "__main__":
    unittest.main()
