#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Start the ArtPlus WebUI."""

from __future__ import annotations

import os
import sys
from pathlib import Path


script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent.absolute()
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))


def main() -> int:
    try:
        import uvicorn
    except ImportError:
        print("错误: 需要安装 uvicorn。请运行: .venv/bin/python -m pip install -r requirements.txt")
        return 1

    host = os.environ.get("ARTPLUS_WEB_HOST", "0.0.0.0")
    port = int(os.environ.get("ARTPLUS_WEB_PORT", "3963"))
    print("=" * 60)
    print("ArtPlus WebUI")
    print("=" * 60)
    print(f"项目目录: {project_root}")
    print(f"监听地址: http://{host}:{port}/")
    print("局域网无鉴权服务，请只在可信网络使用。")
    print("=" * 60)
    uvicorn.run("artplus_webui:app", host=host, port=port, reload=False)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
