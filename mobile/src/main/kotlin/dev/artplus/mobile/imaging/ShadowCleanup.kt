package dev.artplus.mobile

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor

/**
 * 长阴影清理：偏移阴影检测 + 连通域筛选 + 边缘修复。
 *
 * 从 MainActivity 迁移而来（P1.2-b，自底向上第一批）。
 * 显式收 `pipeline` + `shadowRemovalPercent`，不读 Activity 状态。
 */

internal fun removeOffsetShadow(
    source: Bitmap,
    background: Bitmap,
    pipeline: LocalPipelineConfig,
    shadowRemovalPercent: Int,
): ShadowCleanupResult {
    val width = source.width
    val height = source.height
    val pixels = IntArray(width * height)
    val backgroundPixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    val bg = if (background.width == width && background.height == height) {
        background
    } else {
        resizeBitmap(background, width, height)
    }
    bg.getPixels(backgroundPixels, 0, width, 0, 0, width, height)

    var visible = 0
    var highCount = 0
    var highX = 0.0
    var highY = 0.0
    val highAlpha = BooleanArray(pixels.size)
    val shadowCandidate = BooleanArray(pixels.size)
    val alphaMax = effectiveShadowRemovalAlpha(shadowRemovalPercent)
    val saturationMax = effectiveShadowMaxSaturation(shadowRemovalPercent)
    val luminanceMax = effectiveShadowMaxLuminance(shadowRemovalPercent)
    val minVisibleRatio = effectiveShadowMinVisibleRatio(shadowRemovalPercent)
    val minOffset = effectiveShadowMinOffset(shadowRemovalPercent)
    val minDownOffset = effectiveShadowMinDownOffset(shadowRemovalPercent)
    val minLumaDrop = effectiveShadowMinLumaDrop(shadowRemovalPercent)
    if (alphaMax <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
        return ShadowCleanupResult(source, changed = false, removedRatio = 0.0)
    }

    for (y in 0 until height) {
        for (x in 0 until width) {
            val index = y * width + x
            val pixel = pixels[index]
            val alpha = AndroidColor.alpha(pixel)
            if (alpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                continue
            }
            visible++
            if (alpha > SHADOW_HIGH_ALPHA_THRESHOLD) {
                highAlpha[index] = true
                highCount++
                highX += x.toDouble()
                highY += y.toDouble()
            }
            if (
                alpha <= alphaMax &&
                saturation(pixel) <= saturationMax &&
                luma(pixel) <= luminanceMax
            ) {
                shadowCandidate[index] = true
            }
        }
    }

    if (visible == 0 || highCount == 0) {
        return ShadowCleanupResult(source, changed = false, removedRatio = 0.0)
    }
    val highCenterX = highX / highCount.toDouble()
    val highCenterY = highY / highCount.toDouble()

    val selected = BooleanArray(pixels.size)
    connectedMaskComponents(shadowCandidate, width, height).forEach { component ->
        val componentRatio = component.size.toDouble() / visible.toDouble()
        if (componentRatio < minVisibleRatio) {
            return@forEach
        }
        var componentX = 0.0
        var componentY = 0.0
        val lumaDrops = mutableListOf<Int>()
        component.indices.forEach { index ->
            val x = index % width
            val y = index / width
            val sourcePixel = pixels[index]
            val bgPixel = backgroundPixels[index]
            val alpha = AndroidColor.alpha(sourcePixel) / 255.0
            val composedRed = AndroidColor.red(sourcePixel) * alpha + AndroidColor.red(bgPixel) * (1.0 - alpha)
            val composedGreen = AndroidColor.green(sourcePixel) * alpha + AndroidColor.green(bgPixel) * (1.0 - alpha)
            val composedBlue = AndroidColor.blue(sourcePixel) * alpha + AndroidColor.blue(bgPixel) * (1.0 - alpha)
            val drop = luma(bgPixel) - luma(composedRed, composedGreen, composedBlue)
            lumaDrops += drop.toInt()
            componentX += x.toDouble()
            componentY += y.toDouble()
        }
        val componentCenterX = componentX / component.size.toDouble()
        val componentCenterY = componentY / component.size.toDouble()
        val dx = componentCenterX - highCenterX
        val dy = componentCenterY - highCenterY
        val offset = kotlin.math.sqrt(dx * dx + dy * dy)
        val medianDrop = percentile(lumaDrops, 0.50)
        if (
            offset >= minOffset &&
            dy >= minDownOffset &&
            medianDrop >= minLumaDrop
        ) {
            component.indices.forEach { selected[it] = true }
        }
    }

    val cleaned = pixels.copyOf()
    var selectedCount = 0
    for (i in cleaned.indices) {
        if (!selected[i]) {
            continue
        }
        val x = i % width
        val y = i / width
        val distance = distanceToNearbyMaskPixel(
            mask = highAlpha,
            width = width,
            height = height,
            x = x,
            y = y,
            maxRadius = SHADOW_EDGE_ANTIALIAS_RADIUS,
        )
        if (pipeline.shadowEdgeRepairEnabled && distance != null && distance <= SHADOW_EDGE_ANTIALIAS_RADIUS.toDouble()) {
            nearestOpaqueNeighborColor(pixels, width, height, x, y)?.let { edgeColor ->
                val alpha = AndroidColor.alpha(cleaned[i])
                    .coerceIn(LOCAL_ALPHA_VISIBLE_THRESHOLD + 1, SHADOW_EDGE_REPAIR_MAX_ALPHA)
                cleaned[i] = AndroidColor.argb(
                    alpha,
                    AndroidColor.red(edgeColor),
                    AndroidColor.green(edgeColor),
                    AndroidColor.blue(edgeColor),
                )
            }
            continue
        }
        selectedCount++
        cleaned[i] = AndroidColor.TRANSPARENT
    }
    if (selectedCount == 0) {
        return ShadowCleanupResult(source, changed = false, removedRatio = 0.0)
    }
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(cleaned, 0, width, 0, 0, width, height)
    return ShadowCleanupResult(
        bitmap = if (pipeline.shadowEdgeRepairEnabled && pipeline.alphaEdgeColorRepairEnabled) {
            repairTransparentEdgeColors(out)
        } else {
            out
        },
        changed = true,
        removedRatio = selectedCount.toDouble() / visible.toDouble(),
    )
}

internal fun connectedMaskComponents(mask: BooleanArray, width: Int, height: Int): List<MaskComponent> {
    val seen = BooleanArray(mask.size)
    val components = mutableListOf<MaskComponent>()
    val queue = ArrayDeque<Int>()
    for (index in mask.indices) {
        if (!mask[index] || seen[index]) {
            continue
        }
        val indices = mutableListOf<Int>()
        var touchesEdge = false
        var left = width
        var top = height
        var right = -1
        var bottom = -1
        seen[index] = true
        queue.add(index)
        while (!queue.isEmpty()) {
            val current = queue.removeFirst()
            indices += current
            val x = current % width
            val y = current / width
            if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                touchesEdge = true
            }
            if (x < left) left = x
            if (x > right) right = x
            if (y < top) top = y
            if (y > bottom) bottom = y
            fun enqueue(nx: Int, ny: Int) {
                if (nx !in 0 until width || ny !in 0 until height) {
                    return
                }
                val next = ny * width + nx
                if (mask[next] && !seen[next]) {
                    seen[next] = true
                    queue.add(next)
                }
            }
            enqueue(x + 1, y)
            enqueue(x - 1, y)
            enqueue(x, y + 1)
            enqueue(x, y - 1)
        }
        components += MaskComponent(
            indices = indices.toIntArray(),
            touchesEdge = touchesEdge,
            bounds = Bounds(left, top, right + 1, bottom + 1),
        )
    }
    return components
}
