#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from __future__ import annotations

import subprocess
import sys
import tempfile
import unittest
import zipfile
from argparse import Namespace
from base64 import b64encode
from io import BytesIO
from pathlib import Path
from unittest.mock import patch


PROJECT_ROOT = Path(__file__).resolve().parents[1]
SRC_DIR = PROJECT_ROOT / "src"
if str(SRC_DIR) not in sys.path:
    sys.path.insert(0, str(SRC_DIR))
CLI_DIR = PROJECT_ROOT / "cli"
if str(CLI_DIR) not in sys.path:
    sys.path.insert(0, str(CLI_DIR))


try:
    from PIL import Image, ImageDraw
except ImportError:  # pragma: no cover - dependency install is verified separately
    Image = None
    ImageDraw = None

from run_apk_zip_to_oppo_uxicons import (  # noqa: E402
    FOREGROUND_SUBJECT_MAX_SIDE_RATIO,
    direct_gpt_image_edit,
    foreground_subject_max_side_ratio,
    generate_missing_artplus_icons,
    normalize_images_edit_url,
    normalize_foreground_subject_size,
)
from generate_all_artplus_icons import create_monochrome_icon  # noqa: E402


@unittest.skipIf(Image is None, "Pillow not installed")
class WebUiWorkerSmokeTests(unittest.TestCase):
    def test_normalizes_direct_image_edit_url(self) -> None:
        self.assertEqual(
            normalize_images_edit_url("https://api.example.test/v1"),
            "https://api.example.test/v1/images/edits",
        )
        self.assertEqual(
            normalize_images_edit_url("https://api.example.test"),
            "https://api.example.test/v1/images/edits",
        )
        self.assertEqual(
            normalize_images_edit_url("https://api.example.test/v1/images/edits"),
            "https://api.example.test/v1/images/edits",
        )

    def test_direct_gpt_image_edit_uses_base_url_and_key(self) -> None:
        class FakeResponse:
            def __init__(self, body: bytes) -> None:
                self.body = body

            def __enter__(self):
                return self

            def __exit__(self, exc_type, exc, tb):
                return False

            def read(self) -> bytes:
                return self.body

        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = Path(tmp)
            source = tmp_path / "source.png"
            output = tmp_path / "output.png"
            image = Image.new("RGBA", (16, 16), (12, 34, 56, 255))
            image.save(source)
            buffer = BytesIO()
            Image.new("RGBA", (16, 16), (200, 210, 220, 255)).save(buffer, "PNG")
            response_body = (
                b'{"data":[{"b64_json":"'
                + b64encode(buffer.getvalue())
                + b'"}]}'
            )
            captured = {}

            def fake_urlopen(request, timeout=0, **kwargs):
                captured["url"] = request.full_url
                captured["headers"] = dict(request.header_items())
                captured["body"] = request.data
                captured["timeout"] = timeout
                captured["context"] = kwargs.get("context")
                return FakeResponse(response_body)

            args = Namespace(
                gpt_image_api_key="test-key",
                gpt_image_base_url="https://api.example.test/v1",
                gpt_image_model="gpt-image-2",
                gpt_image_size="1024x1024",
                gpt_image_quality="low",
                gpt_image_timeout_seconds=30,
                gpt_image_insecure_skip_tls_verify=False,
            )
            with patch("run_apk_zip_to_oppo_uxicons.urlopen", fake_urlopen):
                direct_gpt_image_edit(source, "prompt", "opaque", output, args)

            self.assertEqual(captured["url"], "https://api.example.test/v1/images/edits")
            self.assertEqual(captured["headers"]["Authorization"], "Bearer test-key")
            self.assertIn(b'name="model"', captured["body"])
            self.assertIn(b'gpt-image-2', captured["body"])
            self.assertIn(b'name="image"; filename="artplus_source_icon.png"', captured["body"])
            self.assertTrue(output.is_file())

    def test_foreground_subject_is_normalized_to_70_percent(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            recfg_path = Path(tmp) / "recfg.png"
            image = Image.new("RGBA", (240, 240), (0, 0, 0, 0))
            draw = ImageDraw.Draw(image)
            draw.rectangle((100, 96, 139, 143), fill=(255, 255, 255, 255))
            image.save(recfg_path)

            changed = normalize_foreground_subject_size(recfg_path)

            self.assertTrue(changed)
            with Image.open(recfg_path) as normalized:
                ratio = foreground_subject_max_side_ratio(normalized)
            self.assertAlmostEqual(ratio, FOREGROUND_SUBJECT_MAX_SIDE_RATIO, delta=0.01)

    def test_monochrome_uses_luminance_as_alpha_mask(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            recfg_path = Path(tmp) / "recfg.png"
            image = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
            draw = ImageDraw.Draw(image)
            draw.rectangle((8, 8, 27, 55), fill=(235, 235, 235, 255))
            draw.rectangle((36, 8, 55, 55), fill=(45, 45, 45, 255))
            image.save(recfg_path)

            result = create_monochrome_icon(recfg_path, 64)

            self.assertEqual(result.getpixel((0, 0))[3], 0)
            self.assertEqual(result.getpixel((16, 24))[:3], (255, 255, 255))
            self.assertGreater(result.getpixel((16, 24))[3], result.getpixel((44, 24))[3] + 120)

    def test_flat_dark_monochrome_subject_stays_visible(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            recfg_path = Path(tmp) / "recfg.png"
            image = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
            draw = ImageDraw.Draw(image)
            draw.rectangle((16, 16, 47, 47), fill=(0, 0, 0, 255))
            image.save(recfg_path)

            result = create_monochrome_icon(recfg_path, 64)

            self.assertGreater(result.getpixel((32, 32))[3], 200)
            self.assertEqual(result.getpixel((0, 0))[3], 0)

    def test_pipeline_overwrites_existing_monochrome_with_alpha_mask(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            output_dir = Path(tmp)
            recfg_path = output_dir / "recfg.png"
            image = Image.new("RGBA", (240, 240), (0, 0, 0, 0))
            draw = ImageDraw.Draw(image)
            draw.rectangle((42, 44, 105, 196), fill=(238, 238, 238, 255))
            draw.rectangle((136, 44, 198, 196), fill=(40, 40, 40, 255))
            image.save(recfg_path)
            Image.new("RGBA", (240, 240), (0, 0, 0, 255)).save(output_dir / "monochrome.png")

            generated = generate_missing_artplus_icons(output_dir)

            self.assertIn("monochrome.png", generated)
            with Image.open(output_dir / "monochrome.png") as mono:
                rgba = mono.convert("RGBA")
                self.assertEqual(rgba.getpixel((60, 80))[:3], (255, 255, 255))
                self.assertGreater(rgba.getpixel((60, 80))[3], rgba.getpixel((160, 80))[3] + 120)

    def test_local_worker_generates_package_zip(self) -> None:
        package_name = "com.example.worker"
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = Path(tmp)
            icon_path = tmp_path / "icon.png"
            image = Image.new("RGBA", (240, 240), (230, 240, 255, 255))
            draw = ImageDraw.Draw(image)
            draw.ellipse((58, 58, 182, 182), fill=(30, 103, 210, 255))
            image.save(icon_path)

            upload_zip = tmp_path / f"{package_name}.zip"
            with zipfile.ZipFile(upload_zip, "w") as zip_ref:
                zip_ref.write(icon_path, "icon.png")

            job_dir = tmp_path / "job"
            command = [
                sys.executable,
                str(PROJECT_ROOT / "cli" / "webui_worker.py"),
                "--job-dir",
                str(job_dir),
                "--upload-zip",
                str(upload_zip),
                "--package-name",
                package_name,
                "--variant",
                "local",
            ]
            completed = subprocess.run(
                command,
                cwd=str(PROJECT_ROOT),
                text=True,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                timeout=90,
            )
            self.assertEqual(completed.returncode, 0, completed.stdout)

            download_zip = job_dir / "downloads" / f"{package_name}-local.zip"
            self.assertTrue(download_zip.is_file())
            with zipfile.ZipFile(download_zip, "r") as zip_ref:
                names = set(zip_ref.namelist())
            self.assertIn(f"{package_name}/recfg.png", names)
            self.assertIn(f"{package_name}/recbg.png", names)
            self.assertIn(f"{package_name}/rec_night.png", names)


if __name__ == "__main__":
    unittest.main()
