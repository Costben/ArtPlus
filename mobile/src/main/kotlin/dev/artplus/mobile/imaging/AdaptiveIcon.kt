package dev.artplus.mobile

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import kotlin.math.roundToInt

/**
 * 自适应图标管线：Drawable→图层桥 + 前景优选 + 组件候选。
 *
 * 从 MainActivity 迁移而来（P1.2-b b3-slice2b）。
 * 显式收 `pipeline` + 调参（百分比/enum），不读 Activity 状态。
 * 仅内部调用的 buildAdaptiveComponentCandidates 无 wrapper。
 */

internal fun buildLocalIconLayers(
    icon: Drawable,
    pipeline: LocalPipelineConfig,
    backgroundSeparationPercent: Int,
    adaptiveForegroundMode: AdaptiveForegroundMode,
    adaptiveDirectMaxCoveragePercent: Int,
    adaptiveDirectMaxCoverageIncreasePercent: Int,
    adaptiveMaskEdgeCoveragePercent: Int,
    adaptiveMaskMinCoveragePercent: Int,
    adaptiveCenterEpsilonPercent: Int,
): LocalIconLayers {
    val renderSize = SIZE_1X1 * LOCAL_ICON_RENDER_SCALE
    if (icon is AdaptiveIconDrawable) {
        val background = drawDrawable(
            icon.background ?: ColorDrawable(AndroidColor.WHITE),
            renderSize,
            renderSize,
            transparent = false,
        )
        val directForeground = drawDrawable(icon.foreground, renderSize, renderSize, transparent = true)
        val composed = drawDrawable(icon, renderSize, renderSize, transparent = true)
        val foreground = if (pipeline.backgroundSeparationEnabled) {
            subtractBackground(composed, background, pipeline = pipeline, backgroundSeparationPercent = backgroundSeparationPercent)
        } else {
            composed
        }
        val recbg = resizeBitmap(background, SIZE_1X1, SIZE_1X1)
        val foregroundSelection = chooseBetterAdaptiveForeground(
            foreground,
            directForeground,
            background,
            pipeline,
            adaptiveForegroundMode,
            adaptiveDirectMaxCoveragePercent,
            adaptiveDirectMaxCoverageIncreasePercent,
            adaptiveMaskEdgeCoveragePercent,
            adaptiveMaskMinCoveragePercent,
            adaptiveCenterEpsilonPercent,
        )
        val monochrome = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            icon.monochrome?.let {
                resizeBitmap(drawDrawable(it, renderSize, renderSize, transparent = true), SIZE_1X1, SIZE_1X1)
            }
        } else {
            null
        }?.takeIf { alphaCoverage(it) >= MONOCHROME_MIN_COVERAGE }
        val selectedForeground = resizeBitmap(
            foregroundSelection.bitmap,
            SIZE_1X1,
            SIZE_1X1,
        )
        val recfg = if (pipeline.cornerMaskCleanupEnabled) {
            removeCornerMaskResidue(selectedForeground, recbg, pipeline = pipeline)
        } else {
            selectedForeground
        }
        val textSafeRecfg = if (!pipeline.textSafeCandidateEnabled) {
            null
        } else if (pipeline.cornerMaskCleanupEnabled) {
            removeCornerMaskResidue(
                source = selectedForeground,
                background = recbg,
                removeNearWhite = false,
                pipeline = pipeline,
            )
        } else {
            selectedForeground
        }
        val componentCandidates = if (pipeline.componentCandidatesEnabled) {
            buildAdaptiveComponentCandidates(
                background = background,
                composed = composed,
                directForeground = directForeground,
                monochrome = monochrome,
                pipeline = pipeline,
                backgroundSeparationPercent = backgroundSeparationPercent,
                adaptiveDirectMaxCoveragePercent = adaptiveDirectMaxCoveragePercent,
                adaptiveDirectMaxCoverageIncreasePercent = adaptiveDirectMaxCoverageIncreasePercent,
            )
        } else {
            ComponentCandidates(subject = null, background = null)
        }
        return LocalIconLayers(
            recfg = recfg,
            recbg = recbg,
            monochrome = monochrome,
            monochromeIsNative = monochrome != null,
            preserveGeometry = foregroundSelection.preserveGeometry,
            textSafe = textSafeRecfg?.let { textSafeForeground ->
                IconCandidate(
                    recfgRaw = textSafeForeground,
                    recbg = recbg,
                    monochromeRaw = monochrome,
                    monochromeIsNative = monochrome != null,
                    preserveGeometry = foregroundSelection.preserveGeometry,
                    applyLocalEdgePolish = pipeline.edgePolishEnabled,
                )
            },
            componentSubject = componentCandidates.subject,
            componentBackground = componentCandidates.background,
        )
    }

    val source = resizeBitmap(drawDrawable(icon, renderSize, renderSize, transparent = true), SIZE_1X1, SIZE_1X1)
    val recbg = solidBitmap(
        SIZE_1X1,
        SIZE_1X1,
        if (pipeline.plainBackgroundEstimationEnabled) {
            estimatePlainIconBackground(source)
        } else {
            AndroidColor.TRANSPARENT
        },
    )
    return LocalIconLayers(
        recfg = if (pipeline.backgroundSeparationEnabled && pipeline.plainBackgroundEstimationEnabled) {
            subtractPlainIconBackground(source, recbg, pipeline, backgroundSeparationPercent)
        } else {
            source
        },
        recbg = recbg,
        monochrome = null,
        monochromeIsNative = false,
        preserveGeometry = false,
        textSafe = null,
        componentSubject = null,
        componentBackground = null,
    )
}

internal fun buildAdaptiveComponentCandidates(
    background: Bitmap,
    composed: Bitmap,
    directForeground: Bitmap,
    monochrome: Bitmap?,
    pipeline: LocalPipelineConfig,
    backgroundSeparationPercent: Int,
    adaptiveDirectMaxCoveragePercent: Int,
    adaptiveDirectMaxCoverageIncreasePercent: Int,
): ComponentCandidates {
    if (
        !hasDetailedAdaptiveBackground(background) ||
        !isUsableDirectAdaptiveForeground(
            source = directForeground,
            composedCoverage = alphaCoverage(
                if (pipeline.backgroundSeparationEnabled) {
                    subtractBackground(composed, background, pipeline = pipeline, backgroundSeparationPercent = backgroundSeparationPercent)
                } else {
                    composed
                },
            ),
            adaptiveDirectMaxCoveragePercent = adaptiveDirectMaxCoveragePercent,
            adaptiveDirectMaxCoverageIncreasePercent = adaptiveDirectMaxCoverageIncreasePercent,
        )
    ) {
        return ComponentCandidates(subject = null, background = null)
    }
    val cleanBackground = estimateAdaptiveCleanBackground(background)
    val subjectForeground = if (pipeline.backgroundSeparationEnabled) {
        subtractPlainIconBackground(composed, cleanBackground, pipeline, backgroundSeparationPercent)
    } else {
        composed
    }
    val subjectCandidate = IconCandidate(
        recfgRaw = resizeBitmap(subjectForeground, SIZE_1X1, SIZE_1X1),
        recbg = resizeBitmap(cleanBackground, SIZE_1X1, SIZE_1X1),
        monochromeRaw = null,
        preserveGeometry = true,
        applyLocalEdgePolish = pipeline.edgePolishEnabled,
    )
    val backgroundCandidate = IconCandidate(
        recfgRaw = resizeBitmap(repairLocalTransparentEdgeColors(directForeground, pipeline), SIZE_1X1, SIZE_1X1),
        recbg = resizeBitmap(background, SIZE_1X1, SIZE_1X1),
        monochromeRaw = monochrome,
        monochromeIsNative = monochrome != null,
        preserveGeometry = true,
        applyLocalEdgePolish = pipeline.edgePolishEnabled,
    )
    return ComponentCandidates(subject = subjectCandidate, background = backgroundCandidate)
}

internal fun chooseBetterAdaptiveForeground(
    fromComposed: Bitmap,
    directForeground: Bitmap,
    background: Bitmap,
    pipeline: LocalPipelineConfig,
    adaptiveForegroundMode: AdaptiveForegroundMode,
    adaptiveDirectMaxCoveragePercent: Int,
    adaptiveDirectMaxCoverageIncreasePercent: Int,
    adaptiveMaskEdgeCoveragePercent: Int,
    adaptiveMaskMinCoveragePercent: Int,
    adaptiveCenterEpsilonPercent: Int,
): AdaptiveForegroundSelection {
    val composedBounds = alphaBounds(fromComposed, LOCAL_ALPHA_VISIBLE_THRESHOLD)
    val directBounds = alphaBounds(directForeground, LOCAL_ALPHA_VISIBLE_THRESHOLD)
    if (composedBounds == null) {
        return AdaptiveForegroundSelection(repairLocalTransparentEdgeColors(directForeground, pipeline), preserveGeometry = true)
    }
    if (directBounds == null) {
        return AdaptiveForegroundSelection(fromComposed, preserveGeometry = false)
    }
    if (!pipeline.adaptiveSelectionEnabled) {
        return AdaptiveForegroundSelection(fromComposed, preserveGeometry = false)
    }
    if (adaptiveForegroundMode == AdaptiveForegroundMode.Composed) {
        return AdaptiveForegroundSelection(fromComposed, preserveGeometry = false)
    }
    if (adaptiveForegroundMode == AdaptiveForegroundMode.Direct) {
        return AdaptiveForegroundSelection(repairLocalTransparentEdgeColors(directForeground, pipeline), preserveGeometry = true)
    }
    if (
        hasDetailedAdaptiveBackground(background) &&
        isUsableDirectAdaptiveForeground(
            source = directForeground,
            composedCoverage = alphaCoverage(fromComposed),
            adaptiveDirectMaxCoveragePercent = adaptiveDirectMaxCoveragePercent,
            adaptiveDirectMaxCoverageIncreasePercent = adaptiveDirectMaxCoverageIncreasePercent,
        )
    ) {
        return AdaptiveForegroundSelection(repairLocalTransparentEdgeColors(directForeground, pipeline), preserveGeometry = true)
    }
    if (shouldPreferDirectAdaptiveForeground(fromComposed, directForeground, background)) {
        return AdaptiveForegroundSelection(repairLocalTransparentEdgeColors(directForeground, pipeline), preserveGeometry = true)
    }
    val composedCenterOffset = centerOffsetRatio(composedBounds, fromComposed.width, fromComposed.height)
    val directCenterOffset = centerOffsetRatio(directBounds, directForeground.width, directForeground.height)
    if (
        hasAdaptiveMaskArtifact(
            fromComposed,
            adaptiveMaskEdgeCoveragePercent,
            adaptiveMaskMinCoveragePercent,
        ) &&
        isUsableDirectAdaptiveForeground(
            source = directForeground,
            composedCoverage = alphaCoverage(fromComposed),
            adaptiveDirectMaxCoveragePercent = adaptiveDirectMaxCoveragePercent,
            adaptiveDirectMaxCoverageIncreasePercent = adaptiveDirectMaxCoverageIncreasePercent,
        )
    ) {
        return AdaptiveForegroundSelection(repairLocalTransparentEdgeColors(directForeground, pipeline), preserveGeometry = true)
    }
    if (alphaCoverage(directForeground) > ratioPercent(adaptiveDirectMaxCoveragePercent)) {
        return AdaptiveForegroundSelection(fromComposed, preserveGeometry = false)
    }
    return if (composedCenterOffset <= directCenterOffset + ratioPercent(adaptiveCenterEpsilonPercent)) {
        AdaptiveForegroundSelection(fromComposed, preserveGeometry = false)
    } else {
        AdaptiveForegroundSelection(repairLocalTransparentEdgeColors(directForeground, pipeline), preserveGeometry = true)
    }
}

internal fun hasDetailedAdaptiveBackground(background: Bitmap): Boolean {
    val width = background.width
    val height = background.height
    if (width <= 0 || height <= 0) {
        return false
    }
    val pixels = IntArray(width * height)
    background.getPixels(pixels, 0, width, 0, 0, width, height)
    val baseColor = medianVisibleColor(background)
    var visible = 0
    var detail = 0
    for (pixel in pixels) {
        if (AndroidColor.alpha(pixel) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
            continue
        }
        visible++
        if (colorDistance(pixel, baseColor) >= ADAPTIVE_BACKGROUND_DETAIL_DISTANCE) {
            detail++
        }
    }
    if (visible == 0) {
        return false
    }
    val detailRatio = detail.toDouble() / visible.toDouble()
    return detailRatio in ADAPTIVE_BACKGROUND_DETAIL_MIN_RATIO..ADAPTIVE_BACKGROUND_DETAIL_MAX_RATIO
}

internal fun estimateAdaptiveCleanBackground(background: Bitmap): Bitmap {
    val width = background.width
    val height = background.height
    if (width <= 0 || height <= 0) {
        return background
    }
    val cornerSize = maxOf(4, (minOf(width, height) * ADAPTIVE_CLEAN_CORNER_RATIO + 0.5f).toInt())
    val topLeft = medianVisibleColorInRect(background, 0, 0, cornerSize, cornerSize)
    val topRight = medianVisibleColorInRect(background, width - cornerSize, 0, width, cornerSize)
    val bottomLeft = medianVisibleColorInRect(background, 0, height - cornerSize, cornerSize, height)
    val bottomRight = medianVisibleColorInRect(
        background,
        width - cornerSize,
        height - cornerSize,
        width,
        height,
    )
    val colors = intArrayOf(topLeft, topRight, bottomLeft, bottomRight)
    var maxDistance = 0.0
    for (i in colors.indices) {
        for (j in i + 1 until colors.size) {
            maxDistance = maxOf(maxDistance, colorDistance(colors[i], colors[j]))
        }
    }
    if (maxDistance <= ADAPTIVE_CLEAN_SOLID_DISTANCE) {
        return solidBitmap(width, height, medianColor(colors))
    }

    val outPixels = IntArray(width * height)
    for (y in 0 until height) {
        val fy = if (height <= 1) 0.0 else y.toDouble() / (height - 1).toDouble()
        for (x in 0 until width) {
            val fx = if (width <= 1) 0.0 else x.toDouble() / (width - 1).toDouble()
            val top = lerpColor(topLeft, topRight, fx)
            val bottom = lerpColor(bottomLeft, bottomRight, fx)
            outPixels[y * width + x] = lerpColor(top, bottom, fy)
        }
    }
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return out
}

internal fun shouldPreferDirectAdaptiveForeground(
    fromComposed: Bitmap,
    directForeground: Bitmap,
    background: Bitmap,
): Boolean {
    val directCoverage = alphaCoverage(directForeground)
    val composedCoverage = alphaCoverage(fromComposed)
    if (directCoverage < ADAPTIVE_DIRECT_FULL_PLATE_COVERAGE) {
        return false
    }
    if (composedCoverage >= directCoverage - ADAPTIVE_DIRECT_MIN_LOST_COVERAGE) {
        return false
    }
    val directBounds = alphaBounds(directForeground, LOCAL_ALPHA_VISIBLE_THRESHOLD) ?: return false
    if (!hasAutoCropRisk(directBounds, directForeground.width, directForeground.height)) {
        return false
    }
    val backgroundColor = medianVisibleColor(background)
    return hasLayeredAdaptiveForegroundPlate(directForeground, backgroundColor)
}

internal fun hasLayeredAdaptiveForegroundPlate(source: Bitmap, backgroundColor: Int): Boolean {
    val width = source.width
    val height = source.height
    val pixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    var visible = 0
    var plateLike = 0
    var detailLike = 0
    for (pixel in pixels) {
        if (AndroidColor.alpha(pixel) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
            continue
        }
        visible++
        val saturation = saturation(pixel)
        val luma = luma(pixel)
        if (
            luma >= ADAPTIVE_DIRECT_PLATE_MIN_LUMA &&
            saturation <= ADAPTIVE_DIRECT_PLATE_MAX_SATURATION &&
            colorDistance(pixel, backgroundColor) >= ADAPTIVE_DIRECT_PLATE_BACKGROUND_DISTANCE
        ) {
            plateLike++
        } else if (colorDistance(pixel, backgroundColor) >= ADAPTIVE_DIRECT_DETAIL_BACKGROUND_DISTANCE) {
            detailLike++
        }
    }
    if (visible == 0) {
        return false
    }
    val plateRatio = plateLike.toDouble() / visible.toDouble()
    val detailRatio = detailLike.toDouble() / visible.toDouble()
    return plateRatio >= ADAPTIVE_DIRECT_PLATE_MIN_RATIO &&
        detailRatio >= ADAPTIVE_DIRECT_DETAIL_MIN_RATIO
}

internal fun hasAdaptiveMaskArtifact(
    source: Bitmap,
    adaptiveMaskEdgeCoveragePercent: Int,
    adaptiveMaskMinCoveragePercent: Int,
): Boolean {
    val bounds = alphaBounds(source, LOCAL_ALPHA_VISIBLE_THRESHOLD) ?: return false
    if (!hasAutoCropRisk(bounds, source.width, source.height)) {
        return false
    }
    val pixels = IntArray(source.width * source.height)
    source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
    val edge = dominantEdgeColor(pixels, source.width, bounds)
    return edge.coverage >= ratioPercent(adaptiveMaskEdgeCoveragePercent) &&
        alphaCoverage(source) >= ratioPercent(adaptiveMaskMinCoveragePercent)
}

internal fun isUsableDirectAdaptiveForeground(
    source: Bitmap,
    composedCoverage: Double,
    adaptiveDirectMaxCoveragePercent: Int,
    adaptiveDirectMaxCoverageIncreasePercent: Int,
): Boolean {
    val coverage = alphaCoverage(source)
    if (coverage !in ADAPTIVE_DIRECT_MIN_COVERAGE..ratioPercent(adaptiveDirectMaxCoveragePercent)) {
        return false
    }
    if (coverage > composedCoverage + ratioPercent(adaptiveDirectMaxCoverageIncreasePercent)) {
        return false
    }
    val bounds = alphaBounds(source, LOCAL_ALPHA_VISIBLE_THRESHOLD) ?: return false
    return !hasAutoCropRisk(bounds, source.width, source.height)
}

internal fun medianVisibleColor(source: Bitmap): Int {
    val pixels = IntArray(source.width * source.height)
    source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
    val reds = mutableListOf<Int>()
    val greens = mutableListOf<Int>()
    val blues = mutableListOf<Int>()
    for (pixel in pixels) {
        if (AndroidColor.alpha(pixel) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
            continue
        }
        reds += AndroidColor.red(pixel)
        greens += AndroidColor.green(pixel)
        blues += AndroidColor.blue(pixel)
    }
    if (reds.isEmpty()) {
        return AndroidColor.WHITE
    }
    return AndroidColor.rgb(median(reds), median(greens), median(blues))
}

internal fun medianVisibleColorInRect(source: Bitmap, left: Int, top: Int, right: Int, bottom: Int): Int {
    val reds = mutableListOf<Int>()
    val greens = mutableListOf<Int>()
    val blues = mutableListOf<Int>()
    val safeLeft = left.coerceIn(0, source.width)
    val safeTop = top.coerceIn(0, source.height)
    val safeRight = right.coerceIn(safeLeft, source.width)
    val safeBottom = bottom.coerceIn(safeTop, source.height)
    for (y in safeTop until safeBottom) {
        for (x in safeLeft until safeRight) {
            val pixel = source.getPixel(x, y)
            if (AndroidColor.alpha(pixel) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                continue
            }
            reds += AndroidColor.red(pixel)
            greens += AndroidColor.green(pixel)
            blues += AndroidColor.blue(pixel)
        }
    }
    if (reds.isEmpty()) {
        return medianVisibleColor(source)
    }
    return AndroidColor.rgb(median(reds), median(greens), median(blues))
}

internal fun medianColor(colors: IntArray): Int {
    val reds = mutableListOf<Int>()
    val greens = mutableListOf<Int>()
    val blues = mutableListOf<Int>()
    colors.forEach { color ->
        reds += AndroidColor.red(color)
        greens += AndroidColor.green(color)
        blues += AndroidColor.blue(color)
    }
    return AndroidColor.rgb(median(reds), median(greens), median(blues))
}

internal fun lerpColor(start: Int, end: Int, amount: Double): Int {
    val t = amount.coerceIn(0.0, 1.0)
    fun channel(a: Int, b: Int): Int =
        (a + (b - a) * t).roundToInt().coerceIn(0, 255)
    return AndroidColor.rgb(
        channel(AndroidColor.red(start), AndroidColor.red(end)),
        channel(AndroidColor.green(start), AndroidColor.green(end)),
        channel(AndroidColor.blue(start), AndroidColor.blue(end)),
    )
}
