package dev.artplus.mobile

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import kotlin.math.pow

/**
 * 单色（monochrome）管线：色调映射 alpha + 原生单色判定/清理/锐化/边缘修剪/抛光。
 *
 * 从 MainActivity 迁移而来（P1.2-c 拆分）：原 `private fun`，现 `internal`，
 * 签名仅 `polishMonochromeEdges` 增加显式 `edgePolishPercent` 参数
 * （原经 `monochromeEdgePolishStrength()` 直读 Activity 状态），其余签名未变。
 * `simpleMonochromeAlphaFromDefaultSubject` 与 `polishForegroundEdges` 不在
 * P1.2-c 范围内，暂留 MainActivity。
 */

/**
 * 前景边缘抛光：Phase 2 漏搬，Phase 3 slice0 fix-forward。
 * 原 `private fun` 经 `foregroundEdgePolishStrength()` 直读 Activity 状态
 * `edgePolishPercent`，新版本显式收参。
 */
internal fun polishForegroundEdges(source: Bitmap, edgePolishPercent: Int): Bitmap {
    val width = source.width
    val height = source.height
    if (width <= 2 || height <= 2) {
        return source
    }
    val pixels = IntArray(width * height)
    val outPixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    val repairedPixels = repairTransparentEdgeColors(pixels, width, height)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val index = y * width + x
            val pixel = repairedPixels[index]
            val alpha = AndroidColor.alpha(pixel)
            if (alpha <= 0) {
                outPixels[index] = AndroidColor.TRANSPARENT
                continue
            }
            val edge = hasTransparentNeighbor(pixels, width, height, x, y, FOREGROUND_EDGE_POLISH_RADIUS)
            if (!edge) {
                outPixels[index] = pixel
                continue
            }
            val coverage = visibleNeighborCoverage(
                pixels = pixels,
                width = width,
                height = height,
                x = x,
                y = y,
                radius = FOREGROUND_EDGE_POLISH_RADIUS,
                threshold = LOCAL_ALPHA_VISIBLE_THRESHOLD,
            )
            val targetAlpha = (coverage * 255.0).toInt().coerceIn(0, 255)
            val strength = foregroundEdgePolishStrength(edgePolishPercent)
            val smoothedAlpha = (alpha * (1.0 - strength) + targetAlpha * strength)
                .toInt()
                .coerceIn(0, 255)
            outPixels[index] = if (smoothedAlpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                AndroidColor.TRANSPARENT
            } else {
                AndroidColor.argb(
                    smoothedAlpha,
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

internal fun monochromeAlpha(source: Bitmap, invertLuma: Boolean): Bitmap {
    val width = source.width
    val height = source.height
    val sourcePixels = IntArray(width * height)
    source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
    val repairedPixels = repairTransparentEdgeColors(sourcePixels, width, height)
    val lumas = sourcePixels
        .indices
        .filter { AndroidColor.alpha(sourcePixels[it]) > MONO_EDGE_ALPHA_IGNORE_THRESHOLD }
        .map { luma(repairedPixels[it]) }
        .toMutableList()
    val low = percentile(lumas, 0.02)
    val high = percentile(lumas, 0.98)
    val hasRange = high - low >= MONO_TONAL_RANGE_THRESHOLD
    val outPixels = IntArray(sourcePixels.size)

    for (i in sourcePixels.indices) {
        val pixel = sourcePixels[i]
        val alpha = AndroidColor.alpha(pixel)
        if (alpha <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
            outPixels[i] = AndroidColor.TRANSPARENT
            continue
        }
        val maskAlpha = if (hasRange) {
            val normalized = ((luma(repairedPixels[i]) - low).toDouble() / (high - low).toDouble())
                .coerceIn(0.0, 1.0)
            val tonal = if (invertLuma) 1.0 - normalized else normalized
            MONO_ALPHA_MIN + tonal.pow(MONO_ALPHA_GAMMA) * (MONO_ALPHA_MAX - MONO_ALPHA_MIN)
        } else {
            MONO_ALPHA_MAX.toDouble()
        }
        val edgeCoverage = ((alpha - MONO_EDGE_ALPHA_DROP_THRESHOLD).toDouble() /
            (255 - MONO_EDGE_ALPHA_DROP_THRESHOLD).toDouble())
            .coerceIn(0.0, 1.0)
        val outAlpha = (edgeCoverage * maskAlpha).toInt().coerceIn(0, 255)
        outPixels[i] = (outAlpha shl 24) or 0x00ffffff
    }

    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return out
}

internal fun hasForegroundTonalRange(source: Bitmap): Boolean {
    val width = source.width
    val height = source.height
    val pixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    val repaired = repairTransparentEdgeColors(pixels, width, height)
    val lumas = mutableListOf<Int>()
    for (i in pixels.indices) {
        if (AndroidColor.alpha(pixels[i]) > MONO_EDGE_ALPHA_IGNORE_THRESHOLD) {
            lumas += luma(repaired[i])
        }
    }
    if (lumas.size < MONO_TONAL_MIN_VISIBLE_PIXELS) {
        return false
    }
    return percentile(lumas, 0.98) - percentile(lumas, 0.02) >= MONO_TONAL_RANGE_THRESHOLD
}

internal fun isUsableNativeMonochrome(source: Bitmap, foreground: Bitmap): Boolean {
    val nativeCoverage = meaningfulAlphaCoverage(source)
    if (nativeCoverage <= 0.0) {
        return false
    }
    if (nativeCoverage >= MONO_NATIVE_MAX_TILE_COVERAGE) {
        return false
    }
    val foregroundCoverage = meaningfulAlphaCoverage(foreground)
    if (foregroundCoverage > 0.0 && nativeCoverage > foregroundCoverage + MONO_NATIVE_MAX_COVERAGE_EXTRA) {
        return false
    }
    return true
}

internal fun monochromeAlphaFromMask(source: Bitmap): Bitmap {
    val width = source.width
    val height = source.height
    val sourcePixels = IntArray(width * height)
    val outPixels = IntArray(sourcePixels.size)
    source.getPixels(sourcePixels, 0, width, 0, 0, width, height)

    for (i in sourcePixels.indices) {
        val pixel = sourcePixels[i]
        val sourceAlpha = AndroidColor.alpha(pixel)
        if (sourceAlpha <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
            outPixels[i] = AndroidColor.TRANSPARENT
            continue
        }
        val outAlpha = sourceAlpha.coerceIn(0, MONO_ALPHA_MAX)
        outPixels[i] = (outAlpha shl 24) or 0x00ffffff
    }

    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return out
}

internal fun cleanNativeMonochrome(source: Bitmap): Bitmap {
    val width = source.width
    val height = source.height
    val sourcePixels = IntArray(width * height)
    val outPixels = IntArray(sourcePixels.size)
    source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
    for (i in sourcePixels.indices) {
        val alpha = AndroidColor.alpha(sourcePixels[i])
        outPixels[i] = if (alpha <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
            AndroidColor.TRANSPARENT
        } else {
            (alpha.coerceIn(0, MONO_ALPHA_MAX) shl 24) or 0x00ffffff
        }
    }
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return out
}

internal fun sharpenMonochromeAlpha(source: Bitmap, nativeSource: Boolean = false): Bitmap {
    val width = source.width
    val height = source.height
    val pixels = IntArray(width * height)
    val outPixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    val lowCut = if (nativeSource) MONO_NATIVE_EDGE_LOW_CUT else MONO_EDGE_SHARPEN_LOW_CUT
    val highCut = if (nativeSource) MONO_NATIVE_EDGE_HIGH_CUT else MONO_EDGE_SHARPEN_HIGH_CUT
    for (i in pixels.indices) {
        val alpha = AndroidColor.alpha(pixels[i])
        val outAlpha = when {
            alpha <= lowCut -> 0
            alpha >= highCut -> alpha.coerceAtMost(MONO_ALPHA_MAX)
            else -> {
                val t = (alpha - lowCut).toDouble() / (highCut - lowCut).toDouble()
                val eased = t * t * (3.0 - 2.0 * t)
                (eased * MONO_ALPHA_MAX).toInt().coerceIn(0, MONO_ALPHA_MAX)
            }
        }
        outPixels[i] = if (outAlpha <= 0) {
            AndroidColor.TRANSPARENT
        } else {
            (outAlpha shl 24) or 0x00ffffff
        }
    }
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return out
}

internal fun trimMonochromeEdge(source: Bitmap): Bitmap {
    val width = source.width
    val height = source.height
    val pixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    val visible = BooleanArray(pixels.size)
    for (i in pixels.indices) {
        visible[i] = AndroidColor.alpha(pixels[i]) > MONO_EDGE_ALPHA_DROP_THRESHOLD
    }

    val outside = BooleanArray(pixels.size)
    val queue = ArrayDeque<Int>()
    fun markOutside(index: Int) {
        if (!visible[index] && !outside[index]) {
            outside[index] = true
            queue.add(index)
        }
    }

    for (x in 0 until width) {
        markOutside(x)
        markOutside((height - 1) * width + x)
    }
    for (y in 0 until height) {
        markOutside(y * width)
        markOutside(y * width + width - 1)
    }

    while (!queue.isEmpty()) {
        val index = queue.removeFirst()
        val x = index % width
        val y = index / width
        for (dy in -1..1) {
            for (dx in -1..1) {
                if (dx == 0 && dy == 0) {
                    continue
                }
                val nx = x + dx
                val ny = y + dy
                if (nx !in 0 until width || ny !in 0 until height) {
                    continue
                }
                markOutside(ny * width + nx)
            }
        }
    }

    val outPixels = pixels.copyOf()
    for (y in 0 until height) {
        for (x in 0 until width) {
            val index = y * width + x
            if (!visible[index]) {
                outPixels[index] = AndroidColor.TRANSPARENT
                continue
            }
            var touchesOutside = false
            for (dy in -1..1) {
                for (dx in -1..1) {
                    if (dx == 0 && dy == 0) {
                        continue
                    }
                    val nx = x + dx
                    val ny = y + dy
                    if (nx !in 0 until width || ny !in 0 until height || outside[ny * width + nx]) {
                        touchesOutside = true
                        break
                    }
                }
                if (touchesOutside) {
                    break
                }
            }
            if (touchesOutside) {
                val alpha = AndroidColor.alpha(outPixels[index])
                val softenedAlpha = (alpha * MONO_EDGE_TRIM_FEATHER_SCALE)
                    .toInt()
                    .coerceIn(0, 255)
                outPixels[index] = if (softenedAlpha <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
                    AndroidColor.TRANSPARENT
                } else {
                    AndroidColor.argb(softenedAlpha, 255, 255, 255)
                }
            }
        }
    }

    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return out
}

internal fun polishMonochromeEdges(source: Bitmap, edgePolishPercent: Int): Bitmap {
    val width = source.width
    val height = source.height
    if (width <= 2 || height <= 2) {
        return source
    }
    val pixels = IntArray(width * height)
    val outPixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val index = y * width + x
            val alpha = AndroidColor.alpha(pixels[index])
            if (alpha <= 0) {
                outPixels[index] = AndroidColor.TRANSPARENT
                continue
            }
            val edge = hasTransparentNeighbor(pixels, width, height, x, y, MONO_EDGE_POLISH_RADIUS)
            if (!edge) {
                outPixels[index] = (alpha.coerceAtMost(MONO_ALPHA_MAX) shl 24) or 0x00ffffff
                continue
            }
            val coverage = visibleNeighborCoverage(
                pixels = pixels,
                width = width,
                height = height,
                x = x,
                y = y,
                radius = MONO_EDGE_POLISH_RADIUS,
                threshold = MONO_EDGE_ALPHA_DROP_THRESHOLD,
            )
            val targetAlpha = (coverage * MONO_ALPHA_MAX).toInt().coerceIn(0, MONO_ALPHA_MAX)
            val strength = monochromeEdgePolishStrength(edgePolishPercent)
            val smoothedAlpha = (alpha * (1.0 - strength) + targetAlpha * strength)
                .toInt()
                .coerceIn(0, MONO_ALPHA_MAX)
            outPixels[index] = if (smoothedAlpha <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
                AndroidColor.TRANSPARENT
            } else {
                (smoothedAlpha shl 24) or 0x00ffffff
            }
        }
    }

    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return out
}
