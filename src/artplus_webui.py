#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""FastAPI WebUI for ArtPlus ZIP processing."""

from __future__ import annotations

import asyncio
import json
import os
import queue
import re
import shutil
import shlex
import sqlite3
import subprocess
import sys
import threading
import time
import uuid
from contextlib import asynccontextmanager
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Any

from fastapi import FastAPI, File, HTTPException, UploadFile
from fastapi.responses import FileResponse, HTMLResponse, JSONResponse, StreamingResponse
from fastapi.staticfiles import StaticFiles

from artplus_apk_icon_zip import ApkIconExtractionError, extract_apk_icon_zip, validate_apk_upload
from artplus_webui_common import (
    MAX_UPLOAD_BYTES,
    ORIGINAL_ICON_PREVIEW_FILENAME,
    REQUIRED_PREVIEW_FILES,
    RETAIN_JOB_COUNT,
    TERMINAL_VARIANT_STATUSES,
    VARIANTS,
    UploadValidationError,
    ensure_upload_size_allowed,
    extract_original_icon_preview,
    resolve_package_name,
    validate_upload_zip,
)
from project_helper import ensure_dir, get_path, get_project_root


PROJECT_ROOT = get_project_root()
JOBS_DIR = get_path("outputs/webui/jobs")
STATIC_DIR = PROJECT_ROOT / "webui_static"
EVENTS_FILENAME = "events.log"
JOB_FILENAME = "job.json"
HISTORY_DB_FILENAME = "history.sqlite3"

STATUS_PENDING = "pending"
STATUS_QUEUED = "queued"
STATUS_RUNNING = "running"
STATUS_SUCCEEDED = "succeeded"
STATUS_FAILED = "failed"
STATUS_PARTIAL = "partial"

JOB_LOCK = threading.RLock()
TASK_QUEUE: queue.Queue["VariantTask"] = queue.Queue()
WORKER_THREAD: threading.Thread | None = None


@dataclass(frozen=True)
class VariantTask:
    job_id: str
    variant: str


def now_iso() -> str:
    return datetime.now().isoformat(timespec="seconds")


def job_dir(job_id: str) -> Path:
    safe_job_id = re.sub(r"[^A-Za-z0-9_.-]", "", job_id)
    if safe_job_id != job_id or not safe_job_id:
        raise HTTPException(status_code=404, detail="任务不存在")
    return JOBS_DIR / safe_job_id


def job_json_path(job_id: str) -> Path:
    return job_dir(job_id) / JOB_FILENAME


def events_path(job_id: str) -> Path:
    return job_dir(job_id) / EVENTS_FILENAME


def history_db_path() -> Path:
    return JOBS_DIR.parent / HISTORY_DB_FILENAME


def connect_history_db() -> sqlite3.Connection:
    ensure_dir(history_db_path().parent)
    conn = sqlite3.connect(str(history_db_path()), timeout=30)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA journal_mode=WAL")
    conn.execute("PRAGMA busy_timeout=30000")
    return conn


def init_history_db() -> None:
    with connect_history_db() as conn:
        conn.execute(
            """
            CREATE TABLE IF NOT EXISTS jobs (
                job_id TEXT PRIMARY KEY,
                package_name TEXT NOT NULL,
                original_filename TEXT NOT NULL,
                status TEXT NOT NULL,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL,
                local_status TEXT,
                gpt_status TEXT,
                local_download_url TEXT,
                gpt_download_url TEXT,
                job_path TEXT NOT NULL
            )
            """
        )
        conn.execute("CREATE INDEX IF NOT EXISTS idx_jobs_updated_at ON jobs(updated_at DESC)")


def read_json_file(path: Path) -> dict[str, Any]:
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    if not isinstance(data, dict):
        raise ValueError(f"JSON object expected: {path}")
    return data


def write_json_file(path: Path, data: dict[str, Any]) -> None:
    ensure_dir(path.parent)
    tmp_path = path.with_suffix(path.suffix + ".tmp")
    with open(tmp_path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    tmp_path.replace(path)


def variant_download_url(job_id: str, variant: str, meta: dict[str, Any]) -> str | None:
    if meta.get("status") == STATUS_SUCCEEDED and meta.get("download_path"):
        return f"/api/jobs/{job_id}/download/{variant}"
    return None


def sync_history_for_job(job: dict[str, Any]) -> None:
    init_history_db()
    job_id = str(job["id"])
    variants = job.get("variants") or {}
    local = variants.get("local") or {}
    gpt = variants.get("gpt") or {}
    with connect_history_db() as conn:
        conn.execute(
            """
            INSERT INTO jobs (
                job_id,
                package_name,
                original_filename,
                status,
                created_at,
                updated_at,
                local_status,
                gpt_status,
                local_download_url,
                gpt_download_url,
                job_path
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(job_id) DO UPDATE SET
                package_name=excluded.package_name,
                original_filename=excluded.original_filename,
                status=excluded.status,
                created_at=excluded.created_at,
                updated_at=excluded.updated_at,
                local_status=excluded.local_status,
                gpt_status=excluded.gpt_status,
                local_download_url=excluded.local_download_url,
                gpt_download_url=excluded.gpt_download_url,
                job_path=excluded.job_path
            """,
            (
                job_id,
                str(job.get("package_name") or ""),
                str(job.get("original_filename") or ""),
                derive_job_status(job),
                str(job.get("created_at") or now_iso()),
                str(job.get("updated_at") or now_iso()),
                local.get("status"),
                gpt.get("status"),
                variant_download_url(job_id, "local", local),
                variant_download_url(job_id, "gpt", gpt),
                str(job_json_path(job_id)),
            ),
        )


def prune_history_index() -> None:
    init_history_db()
    with connect_history_db() as conn:
        rows = conn.execute("SELECT job_id, job_path FROM jobs").fetchall()
        missing = [row["job_id"] for row in rows if not Path(str(row["job_path"])).is_file()]
        conn.executemany("DELETE FROM jobs WHERE job_id = ?", [(job_id,) for job_id in missing])


def rebuild_history_index() -> None:
    init_history_db()
    if JOBS_DIR.is_dir():
        for path in sorted(JOBS_DIR.glob(f"*/{JOB_FILENAME}")):
            try:
                sync_history_for_job(read_json_file(path))
            except Exception:
                continue
    prune_history_index()


def history_row_to_dict(row: sqlite3.Row) -> dict[str, Any]:
    return {
        "id": row["job_id"],
        "package_name": row["package_name"],
        "original_filename": row["original_filename"],
        "status": row["status"],
        "created_at": row["created_at"],
        "updated_at": row["updated_at"],
        "variants": {
            "local": {
                "status": row["local_status"],
                "download_url": row["local_download_url"],
            },
            "gpt": {
                "status": row["gpt_status"],
                "download_url": row["gpt_download_url"],
            },
        },
    }


def list_history(limit: int = 50) -> list[dict[str, Any]]:
    init_history_db()
    safe_limit = max(1, min(limit, 100))
    with connect_history_db() as conn:
        rows = conn.execute(
            """
            SELECT *
            FROM jobs
            ORDER BY updated_at DESC, created_at DESC
            LIMIT ?
            """,
            (safe_limit,),
        ).fetchall()
    return [history_row_to_dict(row) for row in rows]


def load_job(job_id: str) -> dict[str, Any]:
    path = job_json_path(job_id)
    if not path.is_file():
        raise HTTPException(status_code=404, detail="任务不存在")
    return read_json_file(path)


def save_job(job: dict[str, Any]) -> None:
    job["updated_at"] = now_iso()
    job["status"] = derive_job_status(job)
    write_json_file(job_json_path(str(job["id"])), job)
    sync_history_for_job(job)


def append_event(job_id: str, message: str, variant: str | None = None, level: str = "info") -> None:
    event = {
        "ts": now_iso(),
        "level": level,
        "variant": variant,
        "message": message.rstrip("\n"),
    }
    ensure_dir(job_dir(job_id))
    with open(events_path(job_id), "a", encoding="utf-8") as f:
        f.write(json.dumps(event, ensure_ascii=False) + "\n")


def derive_job_status(job: dict[str, Any]) -> str:
    variants = job.get("variants") or {}
    statuses = [str(meta.get("status", STATUS_PENDING)) for meta in variants.values()]
    if not statuses:
        return STATUS_PENDING
    if STATUS_RUNNING in statuses:
        return STATUS_RUNNING
    if STATUS_QUEUED in statuses:
        return STATUS_QUEUED
    if all(status == STATUS_SUCCEEDED for status in statuses):
        return STATUS_SUCCEEDED
    if all(status in TERMINAL_VARIANT_STATUSES for status in statuses):
        return STATUS_PARTIAL if STATUS_SUCCEEDED in statuses else STATUS_FAILED
    return STATUS_PENDING


def new_job_id() -> str:
    stamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    return f"{stamp}-{uuid.uuid4().hex[:8]}"


def create_job_record(job_id: str, package_name: str, original_filename: str, upload_zip: Path) -> dict[str, Any]:
    variants = {}
    for key, variant in VARIANTS.items():
        variants[key] = {
            "key": key,
            "label": variant["label"],
            "status": STATUS_QUEUED,
            "error": None,
            "attempts": 0,
            "output_dir": None,
            "download_path": None,
            "started_at": None,
            "finished_at": None,
            "report": None,
        }
    return {
        "id": job_id,
        "status": STATUS_QUEUED,
        "package_name": package_name,
        "original_filename": original_filename,
        "upload_zip": str(upload_zip),
        "created_at": now_iso(),
        "updated_at": now_iso(),
        "variants": variants,
    }


def report_path_for(job_id: str, variant: str) -> Path:
    return job_dir(job_id) / "variants" / variant / "report.json"


def output_package_dir_for(job: dict[str, Any], variant: str) -> Path:
    meta = job.get("variants", {}).get(variant, {})
    output_dir = meta.get("output_dir")
    if output_dir:
        return Path(output_dir)
    return job_dir(str(job["id"])) / "variants" / variant / "output" / str(job["package_name"])


def original_icon_preview_path(job: dict[str, Any]) -> Path:
    return job_dir(str(job["id"])) / "preview" / ORIGINAL_ICON_PREVIEW_FILENAME


def ensure_original_icon_preview(job: dict[str, Any]) -> Path | None:
    path = original_icon_preview_path(job)
    if path.is_file():
        return path
    upload_zip = Path(str(job.get("upload_zip") or ""))
    if not upload_zip.is_file():
        return None
    try:
        return extract_original_icon_preview(upload_zip, path)
    except Exception:
        return None


def safe_asset_path(job: dict[str, Any], variant: str, filename: str) -> Path:
    if "/" in filename or "\\" in filename or filename.startswith("."):
        raise HTTPException(status_code=404, detail="文件不存在")
    path = output_package_dir_for(job, variant) / filename
    if not path.is_file():
        raise HTTPException(status_code=404, detail="文件不存在")
    return path


def variant_summary(job: dict[str, Any], variant: str, meta: dict[str, Any]) -> dict[str, Any]:
    job_id = str(job["id"])
    output_dir = output_package_dir_for(job, variant)
    assets = {
        name: f"/api/jobs/{job_id}/assets/{variant}/{name}"
        for name in REQUIRED_PREVIEW_FILES
        if (output_dir / name).is_file()
    }
    summary = dict(meta)
    summary["download_url"] = None
    if meta.get("status") == STATUS_SUCCEEDED:
        summary["download_url"] = f"/api/jobs/{job_id}/download/{variant}"
    summary["assets"] = assets
    return summary


def serialize_job(job: dict[str, Any]) -> dict[str, Any]:
    job_id = str(job["id"])
    original_icon = ensure_original_icon_preview(job)
    result = {
        "id": job["id"],
        "status": derive_job_status(job),
        "package_name": job["package_name"],
        "original_filename": job["original_filename"],
        "created_at": job["created_at"],
        "updated_at": job["updated_at"],
        "preview_assets": {},
        "variants": {},
    }
    if original_icon and original_icon.is_file():
        result["preview_assets"]["original_icon"] = f"/api/jobs/{job_id}/preview/{ORIGINAL_ICON_PREVIEW_FILENAME}"
    for key, meta in (job.get("variants") or {}).items():
        result["variants"][key] = variant_summary(job, key, meta)
    return result


def cleanup_old_jobs() -> None:
    if not JOBS_DIR.is_dir():
        return
    job_dirs = [path for path in JOBS_DIR.iterdir() if path.is_dir()]
    job_dirs.sort(key=lambda path: path.stat().st_mtime, reverse=True)
    for stale in job_dirs[RETAIN_JOB_COUNT:]:
        shutil.rmtree(stale, ignore_errors=True)
    prune_history_index()


def enqueue_variant(job_id: str, variant: str) -> None:
    TASK_QUEUE.put(VariantTask(job_id=job_id, variant=variant))


def worker_command(job: dict[str, Any], variant: str) -> list[str]:
    return [
        sys.executable,
        str(PROJECT_ROOT / "cli" / "webui_worker.py"),
        "--job-dir",
        str(job_dir(str(job["id"]))),
        "--upload-zip",
        str(job["upload_zip"]),
        "--package-name",
        str(job["package_name"]),
        "--variant",
        variant,
    ]


def codex_command() -> list[str]:
    return [
        "codex",
        "exec",
        "--ephemeral",
        "--skip-git-repo-check",
        "-C",
        str(PROJECT_ROOT),
        "--dangerously-bypass-approvals-and-sandbox",
        "-",
    ]


def build_codex_prompt(command: list[str]) -> str:
    command_text = shlex.join(command)
    return (
        "You are the constrained worker for an ArtPlus WebUI upload.\n"
        "Run exactly this command from the repository root and do not run any other command:\n\n"
        f"{command_text}\n\n"
        "Do not edit source files. The command writes only the job outputs and reports. "
        "After the command exits, briefly report the exit status."
    )


def run_process(command: list[str], job_id: str, variant: str, stdin_text: str | None = None) -> int:
    process = subprocess.Popen(
        command,
        cwd=str(PROJECT_ROOT),
        stdin=subprocess.PIPE if stdin_text is not None else None,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        bufsize=1,
    )
    if stdin_text is not None and process.stdin is not None:
        process.stdin.write(stdin_text)
        process.stdin.close()
    assert process.stdout is not None
    for line in process.stdout:
        append_event(job_id, line.rstrip("\n"), variant=variant)
    return process.wait()


def run_variant_task(task: VariantTask) -> None:
    with JOB_LOCK:
        job = load_job(task.job_id)
        variant_meta = job["variants"][task.variant]
        variant_meta["status"] = STATUS_RUNNING
        variant_meta["error"] = None
        variant_meta["attempts"] = int(variant_meta.get("attempts") or 0) + 1
        variant_meta["started_at"] = now_iso()
        variant_meta["finished_at"] = None
        save_job(job)

    label = VARIANTS[task.variant]["label"]
    append_event(task.job_id, f"开始处理 {label}", variant=task.variant)
    command = worker_command(job, task.variant)
    direct_worker = os.environ.get("ARTPLUS_WEBUI_DIRECT_WORKER") == "1"

    try:
        if direct_worker:
            returncode = run_process(command, task.job_id, task.variant)
        else:
            prompt = build_codex_prompt(command)
            append_event(task.job_id, "启动 Codex agent", variant=task.variant)
            returncode = run_process(codex_command(), task.job_id, task.variant, stdin_text=prompt)
    except FileNotFoundError as exc:
        returncode = 127
        append_event(task.job_id, f"无法启动进程: {exc}", variant=task.variant, level="error")

    with JOB_LOCK:
        job = load_job(task.job_id)
        variant_meta = job["variants"][task.variant]
        worker_report_path = report_path_for(task.job_id, task.variant)
        worker_report: dict[str, Any] | None = None
        if worker_report_path.is_file():
            try:
                worker_report = read_json_file(worker_report_path)
            except Exception as exc:
                variant_meta["error"] = f"读取 worker 报告失败: {exc}"

        if worker_report and worker_report.get("success"):
            variant_meta["status"] = STATUS_SUCCEEDED
            variant_meta["error"] = None
            variant_meta["output_dir"] = worker_report.get("output_dir")
            variant_meta["download_path"] = worker_report.get("download_path")
            variant_meta["report"] = {
                "image_generation": (worker_report.get("pipeline_report") or {}).get("image_generation"),
                "image_generation_backend": (worker_report.get("pipeline_report") or {}).get("image_generation_backend"),
                "image_generation_base_url": (worker_report.get("pipeline_report") or {}).get("image_generation_base_url"),
                "generated_files": (worker_report.get("pipeline_report") or {}).get("generated_files", []),
                "copied_files": (worker_report.get("pipeline_report") or {}).get("copied_files", []),
            }
            append_event(task.job_id, f"{label} 完成", variant=task.variant)
        else:
            variant_meta["status"] = STATUS_FAILED
            error = None
            if worker_report:
                error = worker_report.get("error")
            variant_meta["error"] = error or f"Codex/worker 退出码: {returncode}"
            append_event(task.job_id, f"{label} 失败: {variant_meta['error']}", variant=task.variant, level="error")

        variant_meta["finished_at"] = now_iso()
        save_job(job)


def task_worker_loop() -> None:
    while True:
        task = TASK_QUEUE.get()
        try:
            run_variant_task(task)
        except Exception as exc:  # pragma: no cover - last-resort guard for daemon stability
            append_event(task.job_id, f"任务执行异常: {exc}", variant=task.variant, level="error")
            try:
                with JOB_LOCK:
                    job = load_job(task.job_id)
                    meta = job["variants"][task.variant]
                    meta["status"] = STATUS_FAILED
                    meta["error"] = str(exc)
                    meta["finished_at"] = now_iso()
                    save_job(job)
            except Exception:
                pass
        finally:
            TASK_QUEUE.task_done()


def ensure_worker_thread() -> None:
    global WORKER_THREAD
    if WORKER_THREAD and WORKER_THREAD.is_alive():
        return
    WORKER_THREAD = threading.Thread(target=task_worker_loop, name="artplus-webui-worker", daemon=True)
    WORKER_THREAD.start()


async def save_upload(upload: UploadFile, target: Path) -> int:
    ensure_dir(target.parent)
    total = 0
    with open(target, "wb") as out:
        while True:
            chunk = await upload.read(1024 * 1024)
            if not chunk:
                break
            total += len(chunk)
            try:
                ensure_upload_size_allowed(total)
            except UploadValidationError:
                out.close()
                target.unlink(missing_ok=True)
                raise
            out.write(chunk)
    return total


def create_app() -> FastAPI:
    @asynccontextmanager
    async def lifespan(_app: FastAPI):
        ensure_dir(JOBS_DIR)
        cleanup_old_jobs()
        rebuild_history_index()
        ensure_worker_thread()
        yield

    app = FastAPI(title="ArtPlus WebUI", lifespan=lifespan)

    if STATIC_DIR.is_dir():
        app.mount("/static", StaticFiles(directory=str(STATIC_DIR)), name="static")

    @app.get("/", response_class=HTMLResponse)
    def index() -> FileResponse:
        index_path = STATIC_DIR / "index.html"
        if not index_path.is_file():
            raise HTTPException(status_code=500, detail="WebUI 静态文件不存在")
        return FileResponse(index_path)

    @app.post("/api/jobs")
    async def create_job(file: UploadFile = File(...)) -> JSONResponse:
        filename = file.filename or ""
        suffix = Path(filename).suffix.lower()
        if suffix not in {".zip", ".apk"}:
            raise HTTPException(status_code=400, detail="只接受 .zip 或 .apk 文件")

        job_id = new_job_id()
        directory = job_dir(job_id)
        upload_dir = ensure_dir(directory / "uploads")
        upload_path = upload_dir / Path(filename).name

        try:
            await save_upload(file, upload_path)
            if suffix == ".apk":
                validate_apk_upload(upload_path)
                converted_zip = upload_dir / f"{Path(filename).stem}.apk_icon.zip"
                extraction = extract_apk_icon_zip(upload_path, converted_zip)
                package_name = extraction.package_name
                final_zip = upload_dir / f"{package_name}.apk_icon.zip"
                if extraction.output_zip != final_zip:
                    extraction.output_zip.replace(final_zip)
                upload_zip = final_zip
                validate_upload_zip(upload_zip)
            else:
                upload_zip = upload_path
                validate_upload_zip(upload_zip)
                package_name = resolve_package_name(filename, upload_zip)
            try:
                extract_original_icon_preview(upload_zip, directory / "preview" / ORIGINAL_ICON_PREVIEW_FILENAME)
            except Exception:
                pass
        except (UploadValidationError, ApkIconExtractionError) as exc:
            shutil.rmtree(directory, ignore_errors=True)
            status = 413 if "上限" in str(exc) else 400
            raise HTTPException(status_code=status, detail=str(exc)) from exc

        with JOB_LOCK:
            job = create_job_record(job_id, package_name, filename, upload_zip)
            save_job(job)
        if suffix == ".apk":
            append_event(job_id, f"已上传 {filename}，已快速提取为图标 ZIP，包名 {package_name}")
        else:
            append_event(job_id, f"已上传 {filename}，推断包名 {package_name}")
        append_event(job_id, "已加入处理队列")
        enqueue_variant(job_id, "local")
        enqueue_variant(job_id, "gpt")
        cleanup_old_jobs()
        return JSONResponse(serialize_job(load_job(job_id)), status_code=201)

    @app.get("/api/jobs")
    def get_history(limit: int = 50) -> dict[str, Any]:
        return {"items": list_history(limit)}

    @app.get("/api/jobs/{job_id}")
    def get_job(job_id: str) -> dict[str, Any]:
        return serialize_job(load_job(job_id))

    @app.post("/api/jobs/{job_id}/retry/{variant}")
    def retry_variant(job_id: str, variant: str) -> dict[str, Any]:
        if variant not in VARIANTS:
            raise HTTPException(status_code=404, detail="版本不存在")
        with JOB_LOCK:
            job = load_job(job_id)
            meta = job["variants"][variant]
            if meta.get("status") not in {STATUS_FAILED, STATUS_SUCCEEDED}:
                raise HTTPException(status_code=409, detail="该版本仍在处理或排队中")
            if meta.get("status") == STATUS_SUCCEEDED:
                raise HTTPException(status_code=409, detail="该版本已经成功，无需重试")
            meta["status"] = STATUS_QUEUED
            meta["error"] = None
            meta["started_at"] = None
            meta["finished_at"] = None
            save_job(job)
        append_event(job_id, f"重新加入队列: {VARIANTS[variant]['label']}", variant=variant)
        enqueue_variant(job_id, variant)
        return serialize_job(load_job(job_id))

    @app.get("/api/jobs/{job_id}/download/{variant}")
    def download_variant(job_id: str, variant: str) -> FileResponse:
        if variant not in VARIANTS:
            raise HTTPException(status_code=404, detail="版本不存在")
        job = load_job(job_id)
        meta = job["variants"][variant]
        download_path = meta.get("download_path")
        if meta.get("status") != STATUS_SUCCEEDED or not download_path:
            raise HTTPException(status_code=404, detail="下载文件不存在")
        path = Path(download_path)
        if not path.is_file():
            raise HTTPException(status_code=404, detail="下载文件不存在")
        return FileResponse(path, filename=path.name, media_type="application/zip")

    @app.get("/api/jobs/{job_id}/assets/{variant}/{filename}")
    def get_asset(job_id: str, variant: str, filename: str) -> FileResponse:
        if variant not in VARIANTS:
            raise HTTPException(status_code=404, detail="版本不存在")
        job = load_job(job_id)
        path = safe_asset_path(job, variant, filename)
        return FileResponse(path)

    @app.get("/api/jobs/{job_id}/preview/{filename}")
    def get_preview_asset(job_id: str, filename: str) -> FileResponse:
        if filename != ORIGINAL_ICON_PREVIEW_FILENAME:
            raise HTTPException(status_code=404, detail="文件不存在")
        job = load_job(job_id)
        path = ensure_original_icon_preview(job)
        if not path or not path.is_file():
            raise HTTPException(status_code=404, detail="原始图标预览不存在")
        return FileResponse(path)

    @app.get("/api/jobs/{job_id}/events")
    async def job_events(job_id: str) -> StreamingResponse:
        load_job(job_id)

        async def stream():
            offset = 0
            while True:
                path = events_path(job_id)
                if path.is_file():
                    with open(path, "r", encoding="utf-8") as f:
                        f.seek(offset)
                        for line in f:
                            yield f"event: log\ndata: {line.rstrip()}\n\n"
                        offset = f.tell()
                try:
                    snapshot = serialize_job(load_job(job_id))
                except HTTPException:
                    break
                yield f"event: snapshot\ndata: {json.dumps(snapshot, ensure_ascii=False)}\n\n"
                if snapshot["status"] in {STATUS_SUCCEEDED, STATUS_FAILED, STATUS_PARTIAL}:
                    await asyncio.sleep(0.5)
                else:
                    await asyncio.sleep(1.0)

        return StreamingResponse(stream(), media_type="text/event-stream")

    return app


app = create_app()
