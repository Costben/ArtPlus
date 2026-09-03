package dev.artplus.mobile

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor

/**
 * 边缘收尾：透明边缘串色修复 + 可见边缘羽化。
 *
 * 从 MainActivity 迁移而来（原 private fun，签名未变）。
 * `repairLocalTransparentEdgeColors` 的 `pipeline` 参数来自 TuningParams.kt，
 * 调用方显式传入，不读 Activity 状态。
 */

internal fun repairTransparentEdgeColors(sourcePixels: IntArray, width: Int, height: Int): IntArray {
    val repaired = sourcePixels.copyOf()
    for (y in 0 until height) {
        for (x in 0 until width) {
            val index = y * width + x
            val pixel = sourcePixels[index]
            val alpha = AndroidColor.alpha(pixel)
            if (alpha >= MONO_EDGE_ALPHA_REPAIR_THRESHOLD || alpha <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
                continue
            }
            val neighbor = nearestOpaqueNeighborColor(sourcePixels, width, height, x, y)
            if (neighbor != null) {
                repaired[index] = AndroidColor.argb(
                    alpha,
                    AndroidColor.red(neighbor),
                    AndroidColor.green(neighbor),
                    AndroidColor.blue(neighbor),
                )
            }
        }
    }
    return repaired
}

internal fun repairLocalTransparentEdgeColors(
    source: Bitmap,
    pipeline: LocalPipelineConfig,
): Bitmap =
    if (pipeline.alphaEdgeColorRepairEnabled) repairTransparentEdgeColors(source) else source

internal fun repairTransparentEdgeColors(source: Bitmap): Bitmap {
    val width = source.width
    val height = source.height
    val pixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    val repaired = repairTransparentEdgeColors(pixels, width, height)
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(repaired, 0, width, 0, 0, width, height)
    return out
}

internal fun featherVisibleEdges(source: Bitmap, blend: Double): Bitmap {
    val width = source.width
    val height = source.height
    val pixels = IntArray(width * height)
    val outPixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val index = y * width + x
            val pixel = pixels[index]
            val alpha = AndroidColor.alpha(pixel)
            if (alpha <= 0) {
                outPixels[index] = AndroidColor.TRANSPARENT
                continue
            }
            var transparentNeighbors = 0
            var totalNeighbors = 0
            for (dy in -1..1) {
                for (dx in -1..1) {
                    if (dx == 0 && dy == 0) {
                        continue
                    }
                    val nx = x + dx
                    val ny = y + dy
                    totalNeighbors += 1
                    if (nx !in 0 until width || ny !in 0 until height) {
                        transparentNeighbors += 1
                        continue
                    }
                    if (AndroidColor.alpha(pixels[ny * width + nx]) <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
                        transparentNeighbors += 1
                    }
                }
            }
            if (transparentNeighbors == 0 || totalNeighbors == 0) {
                outPixels[index] = pixel
                continue
            }
            val edgeRatio = transparentNeighbors.toDouble() / totalNeighbors.toDouble()
            val alphaScale = (1.0 - edgeRatio * blend).coerceIn(0.0, 1.0)
            val outAlpha = (alpha * alphaScale).toInt().coerceIn(0, 255)
            outPixels[index] = if (outAlpha <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
                AndroidColor.TRANSPARENT
            } else {
                AndroidColor.argb(
                    outAlpha,
                    AndroidColor.red(pixel),
                    AndroidColor.green(pixel),
                    AndroidColor.blue(pixel),
                )
            }
        }
    }
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return out
}
