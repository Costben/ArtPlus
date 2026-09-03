package dev.artplus.mobile

import android.graphics.Bitmap
import android.graphics.Canvas
import kotlin.math.abs
import android.graphics.Color as AndroidColor

/**
 * Alpha 几何：包围盒 / 质心 / 覆盖率 / 邻域判定 / 角落残留判定。
 *
 * 从 MainActivity 迁移而来（原 private fun，签名未变）。
 * 全部为纯函数：只依赖参数与顶层阈值常量，不读 Activity 状态。
 */

internal fun centerOffsetRatio(bounds: Bounds, width: Int, height: Int): Double {
    val dx = bounds.left + bounds.width() / 2.0 - width / 2.0
    val dy = bounds.top + bounds.height() / 2.0 - height / 2.0
    return kotlin.math.sqrt(dx * dx + dy * dy) / maxOf(width, height).toDouble()
}

internal fun isInEdgeBand(x: Int, y: Int, bounds: Bounds, band: Int): Boolean =
    y < bounds.top + band ||
        y >= bounds.bottom - band ||
        x < bounds.left + band ||
        x >= bounds.right - band

internal fun hasNearbyMaskPixel(mask: BooleanArray, width: Int, height: Int, x: Int, y: Int, radius: Int): Boolean {
    val radiusSquared = radius * radius
    for (dy in -radius..radius) {
        for (dx in -radius..radius) {
            if (dx * dx + dy * dy > radiusSquared) {
                continue
            }
            val nx = x + dx
            val ny = y + dy
            if (nx in 0 until width && ny in 0 until height && mask[ny * width + nx]) {
                return true
            }
        }
    }
    return false
}

internal fun hasNearbyTransparentPixel(
    pixels: IntArray,
    width: Int,
    height: Int,
    x: Int,
    y: Int,
    radius: Int,
): Boolean {
    for (dy in -radius..radius) {
        for (dx in -radius..radius) {
            if (dx == 0 && dy == 0) {
                continue
            }
            val nx = x + dx
            val ny = y + dy
            if (nx !in 0 until width || ny !in 0 until height) {
                return true
            }
            if (AndroidColor.alpha(pixels[ny * width + nx]) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                return true
            }
        }
    }
    return false
}

internal fun scaleBitmapAroundAlphaCenter(source: Bitmap, scale: Float): Bitmap {
    val normalizedScale = scale.coerceIn(0.2f, 1.5f)
    if (normalizedScale in 0.97f..1.03f) {
        return source
    }
    val bounds = meaningfulAlphaBounds(source) ?: return source
    val originalCenter = meaningfulAlphaCentroid(source)
    val scaledWidth = (source.width * normalizedScale).toInt().coerceAtLeast(1)
    val scaledHeight = (source.height * normalizedScale).toInt().coerceAtLeast(1)
    val scaled = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
    val scaledCenter = meaningfulAlphaCentroid(scaled) ?: return source
    val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(out)
    canvas.drawColor(AndroidColor.TRANSPARENT)
    val originalCenterX = originalCenter?.first ?: (bounds.left + bounds.width() / 2f)
    val originalCenterY = originalCenter?.second ?: (bounds.top + bounds.height() / 2f)
    canvas.drawBitmap(scaled, originalCenterX - scaledCenter.first, originalCenterY - scaledCenter.second, null)
    return out
}

internal fun meaningfulAlphaBounds(source: Bitmap): Bounds? =
    alphaBounds(source, NORMALIZE_ALPHA_BOUNDS_THRESHOLD)
        ?: alphaBounds(source, LOCAL_ALPHA_VISIBLE_THRESHOLD)

internal fun meaningfulAlphaCentroid(source: Bitmap): Pair<Float, Float>? =
    alphaCentroid(source, NORMALIZE_ALPHA_BOUNDS_THRESHOLD)
        ?: alphaCentroid(source, LOCAL_ALPHA_VISIBLE_THRESHOLD)

internal fun alphaCentroid(source: Bitmap, threshold: Int): Pair<Float, Float>? {
    var weight = 0.0
    var xSum = 0.0
    var ySum = 0.0
    for (y in 0 until source.height) {
        for (x in 0 until source.width) {
            val alpha = AndroidColor.alpha(source.getPixel(x, y))
            if (alpha <= threshold) {
                continue
            }
            weight += alpha.toDouble()
            xSum += x * alpha.toDouble()
            ySum += y * alpha.toDouble()
        }
    }
    if (weight <= 0.0) {
        return null
    }
    return Pair((xSum / weight).toFloat(), (ySum / weight).toFloat())
}

internal fun alphaBounds(source: Bitmap, threshold: Int): Bounds? {
    var left = source.width
    var top = source.height
    var right = -1
    var bottom = -1
    for (y in 0 until source.height) {
        for (x in 0 until source.width) {
            if (AndroidColor.alpha(source.getPixel(x, y)) > threshold) {
                if (x < left) left = x
                if (x > right) right = x
                if (y < top) top = y
                if (y > bottom) bottom = y
            }
        }
    }
    return if (right >= left && bottom >= top) {
        Bounds(left, top, right + 1, bottom + 1)
    } else {
        null
    }
}

internal fun hasRealAlpha(source: Bitmap): Boolean {
    var transparent = 0
    var samples = 0
    for (y in 0 until source.height step maxOf(1, source.height / 128)) {
        for (x in 0 until source.width step maxOf(1, source.width / 128)) {
            samples++
            if (AndroidColor.alpha(source.getPixel(x, y)) < 8) {
                transparent++
            }
        }
    }
    return samples > 0 && transparent.toDouble() / samples.toDouble() >= 0.05
}

internal fun alphaCoverage(source: Bitmap): Double {
    var visible = 0
    val total = source.width * source.height
    for (y in 0 until source.height) {
        for (x in 0 until source.width) {
            if (AndroidColor.alpha(source.getPixel(x, y)) > 8) {
                visible++
            }
        }
    }
    return if (total == 0) 0.0 else visible.toDouble() / total.toDouble()
}

internal fun alphaCoverage(source: Bitmap, threshold: Int): Double {
    var visible = 0
    val total = source.width * source.height
    for (y in 0 until source.height) {
        for (x in 0 until source.width) {
            if (AndroidColor.alpha(source.getPixel(x, y)) > threshold) {
                visible++
            }
        }
    }
    return if (total == 0) 0.0 else visible.toDouble() / total.toDouble()
}

internal fun meaningfulAlphaCoverage(source: Bitmap): Double {
    val strongCoverage = alphaCoverage(source, NORMALIZE_ALPHA_BOUNDS_THRESHOLD)
    return if (strongCoverage > 0.0) strongCoverage else alphaCoverage(source)
}

internal fun distanceToNearbyMaskPixel(
    mask: BooleanArray,
    width: Int,
    height: Int,
    x: Int,
    y: Int,
    maxRadius: Int,
): Double? {
    var bestSquared = Int.MAX_VALUE
    for (dy in -maxRadius..maxRadius) {
        for (dx in -maxRadius..maxRadius) {
            val nx = x + dx
            val ny = y + dy
            if (nx !in 0 until width || ny !in 0 until height) {
                continue
            }
            val squared = dx * dx + dy * dy
            if (squared > maxRadius * maxRadius || squared >= bestSquared) {
                continue
            }
            if (mask[ny * width + nx]) {
                bestSquared = squared
            }
        }
    }
    return if (bestSquared == Int.MAX_VALUE) null else kotlin.math.sqrt(bestSquared.toDouble())
}

internal fun hasTransparentNeighbor(
    pixels: IntArray,
    width: Int,
    height: Int,
    x: Int,
    y: Int,
    radius: Int,
): Boolean {
    for (dy in -radius..radius) {
        for (dx in -radius..radius) {
            if (dx == 0 && dy == 0) {
                continue
            }
            val nx = x + dx
            val ny = y + dy
            if (nx !in 0 until width || ny !in 0 until height) {
                return true
            }
            if (AndroidColor.alpha(pixels[ny * width + nx]) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                return true
            }
        }
    }
    return false
}

internal fun visibleNeighborCoverage(
    pixels: IntArray,
    width: Int,
    height: Int,
    x: Int,
    y: Int,
    radius: Int,
    threshold: Int,
): Double {
    var total = 0
    var visible = 0.0
    for (dy in -radius..radius) {
        for (dx in -radius..radius) {
            val nx = x + dx
            val ny = y + dy
            total += 1
            if (nx !in 0 until width || ny !in 0 until height) {
                continue
            }
            val alpha = AndroidColor.alpha(pixels[ny * width + nx])
            if (alpha > threshold) {
                visible += alpha.toDouble() / 255.0
            }
        }
    }
    return if (total == 0) 0.0 else (visible / total.toDouble()).coerceIn(0.0, 1.0)
}

internal fun nearestOpaqueNeighborColor(sourcePixels: IntArray, width: Int, height: Int, x: Int, y: Int): Int? {
    for (radius in 1..MONO_EDGE_REPAIR_RADIUS) {
        var red = 0
        var green = 0
        var blue = 0
        var count = 0
        for (dy in -radius..radius) {
            for (dx in -radius..radius) {
                if (maxOf(abs(dx), abs(dy)) != radius) {
                    continue
                }
                val nx = x + dx
                val ny = y + dy
                if (nx !in 0 until width || ny !in 0 until height) {
                    continue
                }
                val candidate = sourcePixels[ny * width + nx]
                if (AndroidColor.alpha(candidate) < MONO_EDGE_ALPHA_REPAIR_THRESHOLD) {
                    continue
                }
                red += AndroidColor.red(candidate)
                green += AndroidColor.green(candidate)
                blue += AndroidColor.blue(candidate)
                count += 1
            }
        }
        if (count > 0) {
            return AndroidColor.rgb(red / count, green / count, blue / count)
        }
    }
    return null
}

internal fun isNearWhite(pixel: Int): Boolean =
    AndroidColor.red(pixel) >= CORNER_MASK_WHITE_THRESHOLD &&
        AndroidColor.green(pixel) >= CORNER_MASK_WHITE_THRESHOLD &&
        AndroidColor.blue(pixel) >= CORNER_MASK_WHITE_THRESHOLD

internal fun isCornerMaskResidueCandidate(
    pixel: Int,
    backgroundPixel: Int,
    removeNearWhite: Boolean,
): Boolean {
    val alpha = AndroidColor.alpha(pixel)
    if (alpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
        return false
    }

    val backgroundDistance = colorDistance(pixel, backgroundPixel)
    val subjectLike = alpha >= CORNER_MASK_SUBJECT_ALPHA &&
        backgroundDistance >= CORNER_MASK_SUBJECT_BACKGROUND_DISTANCE
    if (subjectLike) {
        return false
    }

    val nearBackground = backgroundDistance <= CORNER_MASK_BACKGROUND_DISTANCE
    val weakMaskEdge = alpha < CORNER_MASK_OPAQUE_ALPHA && nearBackground
    val nearWhiteMaskEdge = removeNearWhite &&
        isNearWhite(pixel) &&
        (nearBackground || alpha < CORNER_MASK_WHITE_EDGE_ALPHA)
    return nearBackground || weakMaskEdge || nearWhiteMaskEdge
}

internal fun isInCornerMaskZone(x: Int, y: Int, width: Int, height: Int): Boolean {
    val zone = minOf(CORNER_MASK_ZONE_SIZE, width / 2, height / 2).coerceAtLeast(1)
    return (x < zone || x >= width - zone) && (y < zone || y >= height - zone)
}
