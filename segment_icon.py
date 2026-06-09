#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import os
import argparse
from pathlib import Path
import numpy as np
import cv2
from PIL import Image
from rembg import remove

def separate_icon(image_path, output_dir=None):
    """
    Separates an icon into foreground (recfg.png) and background (recbg.png).
    """
    image_path = Path(image_path).absolute()
    if not image_path.exists():
        print(f"Error: File not found: {image_path}")
        return

    if output_dir is None:
        output_dir = image_path.parent
    else:
        output_dir = Path(output_dir).absolute()
        output_dir.mkdir(parents=True, exist_ok=True)

    print(f"Processing: {image_path}")
    
    # 1. Generate Foreground (recfg.png) using rembg
    print("Generating foreground (recfg.png)...")
    try:
        original_img = Image.open(image_path).convert("RGBA")
        
        # Use rembg to remove background
        foreground = remove(original_img)
        
        fg_path = output_dir / "recfg.png"
        foreground.save(fg_path)
        print(f"Saved foreground to: {fg_path}")
    except Exception as e:
        print(f"Error generating foreground: {e}")
        return

    # 2. Generate Background (recbg.png) using inpainting
    print("Generating background (recbg.png)...")
    try:
        # Load image with OpenCV
        img_cv = cv2.imread(str(image_path))
        if img_cv is None:
            print("Error: OpenCV could not read the image.")
            return

        # Create a mask from the alpha channel of the foreground we just generated
        fg_cv = cv2.imread(str(fg_path), cv2.IMREAD_UNCHANGED)
        if fg_cv is None or fg_cv.shape[2] != 4:
            print("Error: Could not read generated foreground alpha channel.")
            return

        alpha_channel = fg_cv[:, :, 3]
        
        # Create mask: where alpha > 0 (foreground), we want to inpaint
        # We dilate the mask slightly to ensure we cover the edges
        mask = (alpha_channel > 10).astype(np.uint8) * 255
        kernel = np.ones((5, 5), np.uint8)
        mask = cv2.dilate(mask, kernel, iterations=2)

        # Inpaint
        # Radius 3, flags: NS (Navier-Stokes) or TELEA
        bg_inpainted = cv2.inpaint(img_cv, mask, 3, cv2.INPAINT_TELEA)

        bg_path = output_dir / "recbg.png"
        cv2.imwrite(str(bg_path), bg_inpainted)
        print(f"Saved background to: {bg_path}")
        
    except Exception as e:
        print(f"Error generating background: {e}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Separate icon into foreground and background.")
    parser.add_argument("image_path", help="Path to the input icon image")
    parser.add_argument("--output", "-o", help="Output directory (default: same as input)", default=None)
    
    args = parser.parse_args()
    separate_icon(args.image_path, args.output)
