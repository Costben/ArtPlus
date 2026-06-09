#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Fixed worker invoked by Codex for one WebUI variant."""

from __future__ import annotations

import argparse
import json
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path
from typing import Any


script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent.absolute()
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))

from artplus_webui_common import VARIANTS, validate_upload_zip  # noqa: E402
from project_helper import ensure_dir, get_path  # noqa: E402


class WorkerError(RuntimeError):
    """A user-displayable worker failure."""


def load_json(path: Path) -> dict[str, Any]:
    try:
        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)
    except FileNotFoundError as exc:
        raise WorkerError(f"未找到流水线报告: {path}") from exc
    except json.JSONDecodeError as exc:
        raise WorkerError(f"流水线报告不是有效 JSON: {path}") from exc
    if not isinstance(data, dict):
        raise WorkerError(f"流水线报告格式异常: {path}")
    return data


def write_json(path: Path, payload: dict[str, Any]) -> None:
    ensure_dir(path.parent)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, indent=2)


def copy_upload_to_package_dir(upload_zip: Path, package_name: str, work_dir: Path) -> Path:
    input_dir = work_dir / "input" / package_name
    if input_dir.exists():
        shutil.rmtree(input_dir)
    ensure_dir(input_dir)
    target = input_dir / upload_zip.name
    shutil.copyfile(upload_zip, target)
    return target


def run_pipeline(package_input_dir: Path, upload_zip: Path, variant: str) -> int:
    python_exe = sys.executable
    cmd = [
        python_exe,
        str(project_root / "cli" / "run_apk_zip_to_oppo_uxicons.py"),
        "--input-dir",
        str(package_input_dir),
        "--zip",
        str(upload_zip),
    ]
    if VARIANTS[variant]["disable_gpt_image2"]:
        cmd.append("--disable-gpt-image2")

    print("运行流水线:")
    print("  " + " ".join(cmd))
    completed = subprocess.run(cmd, cwd=str(project_root), text=True)
    return int(completed.returncode)


def create_package_zip(package_dir: Path, package_name: str, zip_path: Path) -> None:
    if zip_path.exists():
        zip_path.unlink()
    ensure_dir(zip_path.parent)
    with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as zip_ref:
        for file_path in sorted(package_dir.rglob("*")):
            if file_path.is_file():
                arcname = Path(package_name) / file_path.relative_to(package_dir)
                zip_ref.write(file_path, arcname.as_posix())


def copy_variant_outputs(job_dir: Path, package_name: str, variant: str) -> tuple[Path, Path]:
    source_dir = get_path("outputs/latest/uxicons") / package_name
    if not source_dir.is_dir():
        raise WorkerError(f"流水线未生成预期输出目录: {source_dir}")

    variant_dir = job_dir / "variants" / variant
    output_dir = variant_dir / "output" / package_name
    if output_dir.exists():
        shutil.rmtree(output_dir)
    ensure_dir(output_dir.parent)
    shutil.copytree(source_dir, output_dir)

    download_path = job_dir / "downloads" / f"{package_name}-{VARIANTS[variant]['download_suffix']}.zip"
    create_package_zip(output_dir, package_name, download_path)
    return output_dir, download_path


def run_worker(args: argparse.Namespace) -> int:
    upload_zip = Path(args.upload_zip).expanduser().resolve()
    job_dir = Path(args.job_dir).expanduser().resolve()
    variant = args.variant
    package_name = args.package_name
    variant_dir = job_dir / "variants" / variant
    work_dir = variant_dir / "work"
    report_path = variant_dir / "report.json"

    report: dict[str, Any] = {
        "success": False,
        "variant": variant,
        "label": VARIANTS[variant]["label"],
        "package_name": package_name,
        "upload_zip": str(upload_zip),
        "output_dir": None,
        "download_path": None,
        "pipeline_report": None,
        "error": None,
    }

    try:
        if not upload_zip.is_file():
            raise WorkerError(f"上传 ZIP 不存在: {upload_zip}")
        validate_upload_zip(upload_zip)

        if work_dir.exists():
            shutil.rmtree(work_dir)
        ensure_dir(work_dir)
        local_zip = copy_upload_to_package_dir(upload_zip, package_name, work_dir)
        returncode = run_pipeline(local_zip.parent, local_zip, variant)

        pipeline_report_path = get_path("outputs/reports/latest_run.json")
        pipeline_report = load_json(pipeline_report_path)
        report["pipeline_report"] = pipeline_report

        if returncode != 0 or not pipeline_report.get("success"):
            error = pipeline_report.get("error") or f"流水线退出码: {returncode}"
            raise WorkerError(str(error))

        if pipeline_report.get("package_name") != package_name:
            raise WorkerError(
                f"流水线包名不匹配: {pipeline_report.get('package_name')} != {package_name}"
            )

        if variant == "gpt" and pipeline_report.get("image_generation") != "gpt-image-2":
            actual = pipeline_report.get("image_generation")
            detail = pipeline_report.get("image_generation_error")
            message = f"GPT版未成功使用 gpt-image-2，实际为 {actual}"
            if detail:
                message += f": {detail}"
            raise WorkerError(message)

        output_dir, download_path = copy_variant_outputs(job_dir, package_name, variant)
        report["success"] = True
        report["output_dir"] = str(output_dir)
        report["download_path"] = str(download_path)
        print(f"完成 {VARIANTS[variant]['label']}: {download_path}")
        return 0
    except Exception as exc:
        report["success"] = False
        report["error"] = str(exc)
        print(f"错误: {exc}", file=sys.stderr)
        return 1
    finally:
        write_json(report_path, report)


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Run one ArtPlus WebUI processing variant")
    parser.add_argument("--job-dir", required=True)
    parser.add_argument("--upload-zip", required=True)
    parser.add_argument("--package-name", required=True)
    parser.add_argument("--variant", required=True, choices=sorted(VARIANTS))
    return parser


def main() -> int:
    return run_worker(build_parser().parse_args())


if __name__ == "__main__":
    raise SystemExit(main())
