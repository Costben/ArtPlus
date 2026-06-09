#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""兼容入口：实际实现位于 src/separate_icons_with_rembg.py。"""

from pathlib import Path
import runpy
import sys

project_root = Path(__file__).resolve().parent.parent
src_dir = project_root / "src"
if str(src_dir) not in sys.path:
    sys.path.insert(0, str(src_dir))

runpy.run_path(str(src_dir / "separate_icons_with_rembg.py"), run_name="__main__")
