package dev.artplus.mobile

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor

/**
 * 前景分离：纯色背景估计/减背景/去底板/残留清理。
 *
 * 从 MainActivity 迁移而来（P1.2-b，自底向上第一批）。
 * 显式收 `pipeline` + 百分比参数，不读 Activity 状态；
 * Activity 内保留同名 wrapper 委托（重构期间保留，P2 后删除），调用点零改动。
 */

internal fun estimatePlainIconBackground(source: Bitmap): Int {
    val width = source.width
    val height = source.height
    val pixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    val bounds = alphaBounds(source, LOCAL_ALPHA_VISIBLE_THRESHOLD)
    if (bounds == null) {
        return AndroidColor.WHITE
    }
    val band = maxOf(2, (minOf(bounds.width(), bounds.height()) * PLAIN_ICON_EDGE_BAND_RATIO + 0.5f).toInt())
    val reds = mutableListOf<Int>()
    val greens = mutableListOf<Int>()
    val blues = mutableListOf<Int>()
    fun addColor(pixel: Int) {
        reds += AndroidColor.red(pixel)
        greens += AndroidColor.green(pixel)
        blues += AndroidColor.blue(pixel)
    }
    for (y in bounds.top until bounds.bottom) {
        for (x in bounds.left until bounds.right) {
            if (!isInEdgeBand(x, y, bounds, band)) {
                continue
            }
            val pixel = pixels[y * width + x]
            if (AndroidColor.alpha(pixel) > PLAIN_ICON_BACKGROUND_ALPHA_THRESHOLD) {
                addColor(pixel)
            }
        }
    }
    if (reds.size < PLAIN_ICON_MIN_BACKGROUND_SAMPLES) {
        for (pixel in pixels) {
            if (AndroidColor.alpha(pixel) > PLAIN_ICON_BACKGROUND_ALPHA_THRESHOLD) {
                addColor(pixel)
            }
        }
    }
    if (reds.isEmpty()) {
        return AndroidColor.WHITE
    }
    return AndroidColor.rgb(median(reds), median(greens), median(blues))
}

internal fun subtractPlainIconBackground(
    source: Bitmap,
    background: Bitmap,
    pipeline: LocalPipelineConfig,
    backgroundSeparationPercent: Int,
): Bitmap {
    val width = source.width
    val height = source.height
    val sourcePixels = IntArray(width * height)
    val backgroundPixels = IntArray(width * height)
    val outPixels = IntArray(width * height)
    source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
    val bg = if (background.width == width && background.height == height) {
        background
    } else {
        resizeBitmap(background, width, height)
    }
    bg.getPixels(backgroundPixels, 0, width, 0, 0, width, height)
    val transparentDistance = ADAPTIVE_SUBTRACT_TRANSPARENT_DISTANCE
    val opaqueDistance = effectiveBackgroundSeparationDistance(backgroundSeparationPercent)

    for (i in sourcePixels.indices) {
        val pixel = sourcePixels[i]
        val sourceAlpha = AndroidColor.alpha(pixel)
        if (sourceAlpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
            outPixels[i] = AndroidColor.TRANSPARENT
            continue
        }
        val distance = colorDistance(pixel, backgroundPixels[i])
        if (distance <= transparentDistance) {
            outPixels[i] = AndroidColor.TRANSPARENT
            continue
        }
        val alpha = ((distance - transparentDistance) / (opaqueDistance - transparentDistance))
            .coerceIn(0.0, 1.0)
        val outAlpha = (sourceAlpha * alpha).toInt().coerceIn(0, 255)
        outPixels[i] = if (outAlpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
            AndroidColor.TRANSPARENT
        } else {
            val restored = restoredForegroundColor(
                visiblePixel = pixel,
                backgroundPixel = backgroundPixels[i],
                foregroundAlpha = alpha,
            )
            AndroidColor.argb(
                outAlpha,
                AndroidColor.red(restored),
                AndroidColor.green(restored),
                AndroidColor.blue(restored),
            )
        }
    }

    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return repairLocalTransparentEdgeColors(out, pipeline)
}

internal fun subtractBackground(
    composed: Bitmap,
    background: Bitmap,
    colorSource: Bitmap? = null,
    pipeline: LocalPipelineConfig,
    backgroundSeparationPercent: Int,
): Bitmap {
    val width = composed.width
    val height = composed.height
    val composedPixels = IntArray(width * height)
    val backgroundPixels = IntArray(width * height)
    val outPixels = IntArray(width * height)
    composed.getPixels(composedPixels, 0, width, 0, 0, width, height)
    val bg = if (background.width == width && background.height == height) {
        background
    } else {
        resizeBitmap(background, width, height)
    }
    bg.getPixels(backgroundPixels, 0, width, 0, 0, width, height)
    val colorSourcePixels = colorSource?.let { source ->
        val sized = if (source.width == width && source.height == height) {
            source
        } else {
            resizeBitmap(source, width, height)
        }
        IntArray(width * height).also { pixels ->
            sized.getPixels(pixels, 0, width, 0, 0, width, height)
        }
    }
    val transparentDistance = ADAPTIVE_SUBTRACT_TRANSPARENT_DISTANCE
    val opaqueDistance = effectiveBackgroundSeparationDistance(backgroundSeparationPercent)

    for (i in composedPixels.indices) {
        val composedAlpha = AndroidColor.alpha(composedPixels[i])
        if (composedAlpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
            outPixels[i] = AndroidColor.TRANSPARENT
            continue
        }
        val distance = colorDistance(composedPixels[i], backgroundPixels[i])
        if (distance <= transparentDistance) {
            outPixels[i] = AndroidColor.TRANSPARENT
            continue
        }
        val alpha = ((distance - transparentDistance) / (opaqueDistance - transparentDistance))
            .coerceIn(0.0, 1.0)
        val outAlpha = (composedAlpha * alpha).toInt().coerceIn(0, 255)
        if (outAlpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
            outPixels[i] = AndroidColor.TRANSPARENT
            continue
        }
        val restored = restoredForegroundColor(
            visiblePixel = composedPixels[i],
            backgroundPixel = backgroundPixels[i],
            foregroundAlpha = alpha,
            colorSourcePixel = colorSourcePixels?.get(i),
        )
        outPixels[i] = AndroidColor.argb(
            outAlpha,
            AndroidColor.red(restored),
            AndroidColor.green(restored),
            AndroidColor.blue(restored),
        )
    }

    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return repairLocalTransparentEdgeColors(out, pipeline)
}

internal fun restoredForegroundColor(
    visiblePixel: Int,
    backgroundPixel: Int,
    foregroundAlpha: Double,
    colorSourcePixel: Int? = null,
): Int {
    if (
        colorSourcePixel != null &&
        AndroidColor.alpha(colorSourcePixel) > LOCAL_ALPHA_VISIBLE_THRESHOLD
    ) {
        return AndroidColor.rgb(
            AndroidColor.red(colorSourcePixel),
            AndroidColor.green(colorSourcePixel),
            AndroidColor.blue(colorSourcePixel),
        )
    }
    val alpha = foregroundAlpha.coerceIn(0.001, 1.0)
    return AndroidColor.rgb(
        uncompositeChannel(AndroidColor.red(visiblePixel), AndroidColor.red(backgroundPixel), alpha),
        uncompositeChannel(AndroidColor.green(visiblePixel), AndroidColor.green(backgroundPixel), alpha),
        uncompositeChannel(AndroidColor.blue(visiblePixel), AndroidColor.blue(backgroundPixel), alpha),
    )
}

internal fun removeForegroundPlate(
    source: Bitmap,
    pipeline: LocalPipelineConfig,
    plateRemovalPercent: Int,
): ForegroundCleanupResult {
    val width = source.width
    val height = source.height
    val pixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    val bounds = alphaBounds(source, LOCAL_ALPHA_VISIBLE_THRESHOLD)
        ?: return ForegroundCleanupResult(source, changed = false, removedRatio = 0.0, repairedRatio = 0.0)
    val edge = dominantEdgeColor(pixels, width, bounds)
    val borderCoverageThreshold = PLATE_BORDER_COVERAGE_THRESHOLD
    val colorDistanceThreshold = effectivePlateRemovalDistance(plateRemovalPercent)
    val minRemovedRatio = PLATE_MIN_REMOVED_RATIO
    if (edge.coverage < borderCoverageThreshold) {
        return ForegroundCleanupResult(source, changed = false, removedRatio = 0.0, repairedRatio = 0.0)
    }

    val plateLike = BooleanArray(pixels.size)
    var visible = 0
    for (i in pixels.indices) {
        val pixel = pixels[i]
        if (AndroidColor.alpha(pixel) > LOCAL_ALPHA_VISIBLE_THRESHOLD) {
            visible++
            if (colorDistance(pixel, edge.color) <= colorDistanceThreshold) {
                plateLike[i] = true
            }
        }
    }
    if (visible == 0) {
        return ForegroundCleanupResult(source, changed = false, removedRatio = 0.0, repairedRatio = 0.0)
    }

    val band = maxOf(2, (minOf(bounds.width(), bounds.height()) * EDGE_BAND_RATIO + 0.5f).toInt())
    val plate = floodFillEdgeConnectedMask(plateLike, width, height, bounds, band)
    val removed = plate.count { it }
    val removedRatio = removed.toDouble() / visible.toDouble()
    if (removedRatio < minRemovedRatio) {
        return ForegroundCleanupResult(source, changed = false, removedRatio = removedRatio, repairedRatio = 0.0)
    }

    val cleaned = pixels.copyOf()
    for (i in cleaned.indices) {
        if (plate[i]) {
            cleaned[i] = AndroidColor.TRANSPARENT
        }
    }
    val repaired = if (pipeline.plateEdgeRepairEnabled) {
        repairPlateEdges(pixels, cleaned, plate, width, height, edge.color)
    } else {
        0
    }

    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(cleaned, 0, width, 0, 0, width, height)
    val cleanedBitmap = if (pipeline.plateResidueCleanupEnabled) {
        removeBackgroundColoredResidue(out, edge.color, plateRemovalPercent)
    } else {
        out
    }
    if (isUnsafePlateRemoval(source, cleanedBitmap)) {
        return ForegroundCleanupResult(source, changed = false, removedRatio = removedRatio, repairedRatio = 0.0)
    }
    return ForegroundCleanupResult(
        bitmap = cleanedBitmap,
        changed = true,
        removedRatio = removedRatio,
        repairedRatio = repaired.toDouble() / visible.toDouble(),
    )
}

internal fun isUnsafePlateRemoval(source: Bitmap, cleaned: Bitmap): Boolean {
    val sourceCoverage = meaningfulAlphaCoverage(source)
    val cleanedCoverage = meaningfulAlphaCoverage(cleaned)
    if (cleanedCoverage <= PLATE_MIN_SAFE_REMAINING_COVERAGE) {
        return true
    }
    if (sourceCoverage <= 0.0) {
        return false
    }
    val keptRatio = cleanedCoverage / sourceCoverage
    if (keptRatio < PLATE_MIN_SAFE_KEEP_RATIO) {
        return true
    }
    val sourceBounds = meaningfulAlphaBounds(source) ?: return false
    val cleanedBounds = meaningfulAlphaBounds(cleaned) ?: return true
    val sourceMax = maxOf(sourceBounds.width(), sourceBounds.height()).toDouble()
    val cleanedMax = maxOf(cleanedBounds.width(), cleanedBounds.height()).toDouble()
    return sourceMax > 0.0 && cleanedMax / sourceMax < PLATE_MIN_SAFE_BOUNDS_RATIO
}

internal fun removeCornerMaskResidue(
    source: Bitmap,
    background: Bitmap,
    removeNearWhite: Boolean = true,
    pipeline: LocalPipelineConfig,
): Bitmap {
    val width = source.width
    val height = source.height
    if (width <= 0 || height <= 0) {
        return source
    }

    val pixels = IntArray(width * height)
    val backgroundPixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    val normalizedBackground = if (background.width == width && background.height == height) {
        background
    } else {
        resizeBitmap(background, width, height)
    }
    normalizedBackground.getPixels(backgroundPixels, 0, width, 0, 0, width, height)

    val candidate = BooleanArray(pixels.size)
    for (i in pixels.indices) {
        candidate[i] = isCornerMaskResidueCandidate(
            pixel = pixels[i],
            backgroundPixel = backgroundPixels[i],
            removeNearWhite = removeNearWhite,
        )
    }

    val remove = BooleanArray(pixels.size)
    val queue = ArrayDeque<Int>()
    fun enqueue(index: Int) {
        if (candidate[index] && !remove[index]) {
            remove[index] = true
            queue.add(index)
        }
    }

    val seed = minOf(CORNER_MASK_SEED_SIZE, width / 3, height / 3).coerceAtLeast(1)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val inCorner = (x < seed || x >= width - seed) && (y < seed || y >= height - seed)
            val onBorder = x == 0 || y == 0 || x == width - 1 || y == height - 1
            if (inCorner || onBorder) {
                enqueue(y * width + x)
            }
        }
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
                if (!isInCornerMaskZone(nx, ny, width, height)) {
                    continue
                }
                enqueue(ny * width + nx)
            }
        }
    }

    val removed = remove.count { it }
    if (removed == 0) {
        return source
    }
    val visible = pixels.count { AndroidColor.alpha(it) > LOCAL_ALPHA_VISIBLE_THRESHOLD }
    if (visible == 0 || removed.toDouble() / visible.toDouble() > CORNER_MASK_MAX_REMOVED_RATIO) {
        return source
    }

    val cleaned = pixels.copyOf()
    for (i in cleaned.indices) {
        if (remove[i]) {
            cleaned[i] = AndroidColor.TRANSPARENT
        }
    }

    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(cleaned, 0, width, 0, 0, width, height)
    return repairLocalTransparentEdgeColors(out, pipeline)
}

internal fun removeBackgroundColoredResidue(
    source: Bitmap,
    backgroundColor: Int,
    plateRemovalPercent: Int,
): Bitmap {
    val width = source.width
    val height = source.height
    val pixels = IntArray(width * height)
    val outPixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    val canRemoveSaturatedResidue = saturation(backgroundColor) >= RESIDUE_BACKGROUND_MIN_SATURATION
    for (i in pixels.indices) {
        val pixel = pixels[i]
        val alpha = AndroidColor.alpha(pixel)
        if (alpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
            outPixels[i] = AndroidColor.TRANSPARENT
            continue
        }
        val distance = colorDistance(pixel, backgroundColor)
        if (
            canRemoveSaturatedResidue &&
            alpha <= RESIDUE_MAX_ALPHA &&
            distance <= residueDistanceThreshold(plateRemovalPercent)
        ) {
            outPixels[i] = AndroidColor.TRANSPARENT
        } else {
            outPixels[i] = pixel
        }
    }
    val cleanedPixels = if (canRemoveSaturatedResidue) {
        removeEdgeConnectedColorResidue(outPixels, width, height, backgroundColor, plateRemovalPercent)
    } else {
        outPixels
    }
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(cleanedPixels, 0, width, 0, 0, width, height)
    return out
}

internal fun removeEdgeConnectedColorResidue(
    pixels: IntArray,
    width: Int,
    height: Int,
    backgroundColor: Int,
    plateRemovalPercent: Int,
): IntArray {
    val threshold = edgeConnectedResidueDistanceThreshold(plateRemovalPercent)
    val candidate = BooleanArray(pixels.size)
    for (i in pixels.indices) {
        val pixel = pixels[i]
        val alpha = AndroidColor.alpha(pixel)
        candidate[i] = alpha > LOCAL_ALPHA_VISIBLE_THRESHOLD &&
            alpha <= RESIDUE_CONNECTED_MAX_ALPHA &&
            colorDistance(pixel, backgroundColor) <= threshold
    }

    val remove = BooleanArray(pixels.size)
    val queue = ArrayDeque<Int>()
    fun enqueue(index: Int) {
        if (candidate[index] && !remove[index]) {
            remove[index] = true
            queue.add(index)
        }
    }

    for (y in 0 until height) {
        for (x in 0 until width) {
            val index = y * width + x
            if (!candidate[index]) {
                continue
            }
            if (x == 0 || y == 0 || x == width - 1 || y == height - 1 ||
                hasNearbyTransparentPixel(pixels, width, height, x, y, RESIDUE_CONNECTED_TRANSPARENT_RADIUS)
            ) {
                enqueue(index)
            }
        }
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
                enqueue(ny * width + nx)
            }
        }
    }

    val out = pixels.copyOf()
    for (i in out.indices) {
        if (remove[i]) {
            out[i] = AndroidColor.TRANSPARENT
        }
    }
    return out
}

internal fun dominantEdgeColor(pixels: IntArray, width: Int, bounds: Bounds): EdgeAnalysis {
    val band = maxOf(2, (minOf(bounds.width(), bounds.height()) * EDGE_BAND_RATIO + 0.5f).toInt())
    val reds = mutableListOf<Int>()
    val greens = mutableListOf<Int>()
    val blues = mutableListOf<Int>()
    var ringArea = 0
    var visibleRing = 0
    for (y in bounds.top until bounds.bottom) {
        for (x in bounds.left until bounds.right) {
            if (!isInEdgeBand(x, y, bounds, band)) {
                continue
            }
            ringArea++
            val pixel = pixels[y * width + x]
            if (AndroidColor.alpha(pixel) > LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                visibleRing++
                reds += AndroidColor.red(pixel)
                greens += AndroidColor.green(pixel)
                blues += AndroidColor.blue(pixel)
            }
        }
    }
    if (ringArea == 0 || visibleRing == 0) {
        return EdgeAnalysis(coverage = 0.0, color = AndroidColor.BLACK)
    }
    val color = AndroidColor.rgb(median(reds), median(greens), median(blues))
    return EdgeAnalysis(
        coverage = visibleRing.toDouble() / ringArea.toDouble(),
        color = color,
    )
}

internal fun floodFillEdgeConnectedMask(
    sourceMask: BooleanArray,
    width: Int,
    height: Int,
    bounds: Bounds,
    band: Int,
): BooleanArray {
    val out = BooleanArray(sourceMask.size)
    val queue = ArrayDeque<Int>()
    for (y in bounds.top until bounds.bottom) {
        for (x in bounds.left until bounds.right) {
            if (!isInEdgeBand(x, y, bounds, band)) {
                continue
            }
            val index = y * width + x
            if (sourceMask[index] && !out[index]) {
                out[index] = true
                queue.add(index)
            }
        }
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
                val next = ny * width + nx
                if (sourceMask[next] && !out[next]) {
                    out[next] = true
                    queue.add(next)
                }
            }
        }
    }
    return out
}

internal fun repairPlateEdges(
    sourcePixels: IntArray,
    cleanedPixels: IntArray,
    plate: BooleanArray,
    width: Int,
    height: Int,
    plateColor: Int,
): Int {
    var repaired = 0
    val plateRed = AndroidColor.red(plateColor)
    val plateGreen = AndroidColor.green(plateColor)
    val plateBlue = AndroidColor.blue(plateColor)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val index = y * width + x
            if (plate[index] || AndroidColor.alpha(sourcePixels[index]) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                continue
            }
            if (!hasNearbyMaskPixel(plate, width, height, x, y, 2)) {
                continue
            }
            val pixel = sourcePixels[index]
            val maxDelta = maxOf(
                kotlin.math.abs(AndroidColor.red(pixel) - plateRed),
                kotlin.math.abs(AndroidColor.green(pixel) - plateGreen),
                kotlin.math.abs(AndroidColor.blue(pixel) - plateBlue),
            )
            val inferredAlpha = (maxDelta / 255.0).coerceIn(0.0, 1.0)
            if (inferredAlpha >= 0.985) {
                continue
            }
            repaired++
            if (inferredAlpha <= 0.035) {
                cleanedPixels[index] = AndroidColor.TRANSPARENT
                continue
            }
            val red = uncompositeChannel(AndroidColor.red(pixel), plateRed, inferredAlpha)
            val green = uncompositeChannel(AndroidColor.green(pixel), plateGreen, inferredAlpha)
            val blue = uncompositeChannel(AndroidColor.blue(pixel), plateBlue, inferredAlpha)
            val alpha = (AndroidColor.alpha(pixel) * inferredAlpha).toInt().coerceIn(0, 255)
            cleanedPixels[index] = AndroidColor.argb(alpha, red, green, blue)
        }
    }
    return repaired
}
