#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from __future__ import annotations

import io
import os
import sys
import tempfile
import unittest
import zipfile
from io import BytesIO
from pathlib import Path
from unittest.mock import patch


PROJECT_ROOT = Path(__file__).resolve().parents[1]
SRC_DIR = PROJECT_ROOT / "src"
if str(SRC_DIR) not in sys.path:
    sys.path.insert(0, str(SRC_DIR))


try:
    from fastapi.testclient import TestClient
except ImportError:  # pragma: no cover - dependency install is verified separately
    TestClient = None

try:
    from PIL import Image
except ImportError:  # pragma: no cover - dependency install is verified separately
    Image = None


def make_png_bytes(color: tuple[int, int, int, int] = (0, 0, 0, 255)) -> bytes:
    if Image is None:
        raise unittest.SkipTest("Pillow not installed")
    buffer = BytesIO()
    Image.new("RGBA", (80, 80), color).save(buffer, "PNG")
    return buffer.getvalue()


def make_zip_bytes() -> bytes:
    buffer = io.BytesIO()
    with zipfile.ZipFile(buffer, "w") as zip_ref:
        zip_ref.writestr("icon.png", b"fake-png")
    return buffer.getvalue()


def make_resource_arsc_zip_bytes() -> bytes:
    apk_path = PROJECT_ROOT / "tests" / "com.catchingnow.np.apk"
    with zipfile.ZipFile(apk_path, "r") as apk_ref:
        arsc = apk_ref.read("resources.arsc")
    buffer = io.BytesIO()
    with zipfile.ZipFile(buffer, "w") as zip_ref:
        zip_ref.writestr("payload/resource.arsc", arsc)
        zip_ref.writestr("icon.png", b"fake-png")
    return buffer.getvalue()


@unittest.skipIf(TestClient is None, "fastapi/httpx not installed")
class WebUiApiTests(unittest.TestCase):
    def setUp(self) -> None:
        self.tmp = tempfile.TemporaryDirectory()
        self.jobs_dir = Path(self.tmp.name) / "jobs"

        import artplus_webui

        self.module = artplus_webui
        self.patches = [
            patch.object(artplus_webui, "JOBS_DIR", self.jobs_dir),
            patch.object(artplus_webui, "ensure_worker_thread", lambda: None),
            patch.object(artplus_webui, "cleanup_old_jobs", lambda: None),
            patch.object(artplus_webui, "enqueue_variant", lambda job_id, variant: None),
        ]
        for patcher in self.patches:
            patcher.start()
        self.client = TestClient(artplus_webui.create_app())

    def tearDown(self) -> None:
        for patcher in reversed(self.patches):
            patcher.stop()
        self.tmp.cleanup()

    def test_create_job_returns_status_shape(self) -> None:
        response = self.client.post(
            "/api/jobs",
            files={"file": ("com.example.app.zip", make_zip_bytes(), "application/zip")},
        )
        self.assertEqual(response.status_code, 201, response.text)
        payload = response.json()
        self.assertEqual(payload["package_name"], "com.example.app")
        self.assertIn("local", payload["variants"])
        self.assertIn("gpt", payload["variants"])
        self.assertEqual(payload["variants"]["local"]["status"], "queued")

    def test_create_job_is_listed_in_sqlite_history(self) -> None:
        created = self.client.post(
            "/api/jobs",
            files={"file": ("com.example.app.zip", make_zip_bytes(), "application/zip")},
        ).json()

        response = self.client.get("/api/jobs")
        self.assertEqual(response.status_code, 200, response.text)
        payload = response.json()
        self.assertEqual(payload["items"][0]["id"], created["id"])
        self.assertEqual(payload["items"][0]["package_name"], "com.example.app")
        self.assertEqual(payload["items"][0]["status"], "queued")

    def test_history_rebuild_scans_existing_job_json(self) -> None:
        job_id = "20260609-120000-existing"
        upload_zip = self.jobs_dir / job_id / "uploads" / "com.example.existing.zip"
        upload_zip.parent.mkdir(parents=True)
        upload_zip.write_bytes(make_zip_bytes())
        job = self.module.create_job_record(job_id, "com.example.existing", "com.example.existing.zip", upload_zip)
        self.module.write_json_file(self.jobs_dir / job_id / "job.json", job)

        self.module.rebuild_history_index()

        response = self.client.get("/api/jobs")
        self.assertEqual(response.status_code, 200, response.text)
        items = response.json()["items"]
        self.assertTrue(any(item["id"] == job_id for item in items))

    def test_create_job_uses_resource_arsc_when_filename_has_no_package(self) -> None:
        response = self.client.post(
            "/api/jobs",
            files={"file": ("上传图标.zip", make_resource_arsc_zip_bytes(), "application/zip")},
        )
        self.assertEqual(response.status_code, 201, response.text)
        payload = response.json()
        self.assertEqual(payload["package_name"], "com.catchingnow.np")

    def test_create_job_accepts_apk_and_converts_to_icon_zip(self) -> None:
        apk_path = PROJECT_ROOT / "tests" / "com.catchingnow.np.apk"
        response = self.client.post(
            "/api/jobs",
            files={
                "file": (
                    "FilterBox.apk",
                    apk_path.read_bytes(),
                    "application/vnd.android.package-archive",
                )
            },
        )

        self.assertEqual(response.status_code, 201, response.text)
        payload = response.json()
        self.assertEqual(payload["package_name"], "com.catchingnow.np")
        job = self.module.read_json_file(self.jobs_dir / payload["id"] / "job.json")
        upload_zip = Path(job["upload_zip"])
        self.assertEqual(upload_zip.name, "com.catchingnow.np.apk_icon.zip")
        with zipfile.ZipFile(upload_zip, "r") as zip_ref:
            names = set(zip_ref.namelist())
        self.assertIn("icon.webp", names)
        self.assertIn("resources.arsc", names)
        self.assertIn("res/mipmap-xxxhdpi-v4/ic_launcher.webp", names)

    @unittest.skipIf(Image is None, "Pillow not installed")
    def test_create_job_exposes_original_icon_preview_asset(self) -> None:
        buffer = io.BytesIO()
        with zipfile.ZipFile(buffer, "w") as zip_ref:
            zip_ref.writestr("icon.png", make_png_bytes())
        response = self.client.post(
            "/api/jobs",
            files={"file": ("com.example.app.zip", buffer.getvalue(), "application/zip")},
        )
        self.assertEqual(response.status_code, 201, response.text)
        payload = response.json()
        preview_url = payload["preview_assets"]["original_icon"]
        self.assertEqual(preview_url, f"/api/jobs/{payload['id']}/preview/original_icon.png")

        asset_response = self.client.get(preview_url)
        self.assertEqual(asset_response.status_code, 200, asset_response.text)
        self.assertEqual(asset_response.headers["content-type"], "image/png")

    def test_rejects_non_zip_upload(self) -> None:
        response = self.client.post(
            "/api/jobs",
            files={"file": ("com.example.app.txt", b"no", "text/plain")},
        )
        self.assertEqual(response.status_code, 400)

    def test_retry_failed_variant(self) -> None:
        created = self.client.post(
            "/api/jobs",
            files={"file": ("com.example.app.zip", make_zip_bytes(), "application/zip")},
        ).json()
        job_path = self.jobs_dir / created["id"] / "job.json"
        job = self.module.read_json_file(job_path)
        job["variants"]["gpt"]["status"] = "failed"
        job["variants"]["gpt"]["error"] = "boom"
        self.module.write_json_file(job_path, job)

        response = self.client.post(f"/api/jobs/{created['id']}/retry/gpt")
        self.assertEqual(response.status_code, 200, response.text)
        payload = response.json()
        self.assertEqual(payload["variants"]["gpt"]["status"], "queued")
        self.assertIsNone(payload["variants"]["gpt"]["error"])

    def test_download_available_for_successful_variant(self) -> None:
        created = self.client.post(
            "/api/jobs",
            files={"file": ("com.example.app.zip", make_zip_bytes(), "application/zip")},
        ).json()
        download_dir = self.jobs_dir / created["id"] / "downloads"
        download_dir.mkdir(parents=True)
        download_path = download_dir / "com.example.app-local.zip"
        download_path.write_bytes(make_zip_bytes())

        job_path = self.jobs_dir / created["id"] / "job.json"
        job = self.module.read_json_file(job_path)
        job["variants"]["local"]["status"] = "succeeded"
        job["variants"]["local"]["download_path"] = str(download_path)
        self.module.write_json_file(job_path, job)

        response = self.client.get(f"/api/jobs/{created['id']}/download/local")
        self.assertEqual(response.status_code, 200, response.text)
        self.assertEqual(response.headers["content-type"], "application/zip")

    def test_codex_command_uses_current_noninteractive_flags(self) -> None:
        command = self.module.codex_command()
        self.assertIn("--dangerously-bypass-approvals-and-sandbox", command)
        self.assertNotIn("--ask-for-approval", command)


if __name__ == "__main__":
    unittest.main()
