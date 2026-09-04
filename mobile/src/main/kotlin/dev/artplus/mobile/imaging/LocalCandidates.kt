package dev.artplus.mobile

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor

/**
 * 本时候选编排：原始/清理/拼合背景/二层候选的构建与自动选择。
 *
 * 从 MainActivity 迁移而来（P1.2-b b3-slice3，最后一批）。
 * 显式收 `pipeline` + 调参，不读 Activity 状态。
 * 仅内部调用的另两构建函数无 wrapper。
 */

internal fun buildLocalCandidates(
    localSource: LocalIconLayers,
    sourceIcon: Bitmap,
    pipeline: LocalPipelineConfig,
    originalForegroundCleanupMode: OriginalForegroundCleanupMode,
    plateRemovalPercent: Int,
    shadowRemovalPercent: Int,
    backgroundSeparationPercent: Int,
): LocalCandidateSet {
    val originalForeground = prepareOriginalForeground(
        localSource.recfg,
        pipeline,
        originalForegroundCleanupMode,
        plateRemovalPercent,
    )
    val original = IconCandidate(
        recfgRaw = originalForeground,
        recbg = localSource.recbg,
        monochromeRaw = localSource.monochrome,
        monochromeIsNative = localSource.monochromeIsNative,
        preserveGeometry = localSource.preserveGeometry,
        applyLocalEdgePolish = pipeline.edgePolishEnabled,
    )
    val fullResult = separateLocalForeground(
        localSource.recfg,
        localSource.recbg,
        LocalSeparationMode.Full,
        pipeline,
        plateRemovalPercent,
        shadowRemovalPercent,
    )
    val cleanupResult = if (pipeline.autoSelectionEnabled) {
        val plateResult = separateLocalForeground(
            localSource.recfg,
            localSource.recbg,
            LocalSeparationMode.Plate,
            pipeline,
            plateRemovalPercent,
            shadowRemovalPercent,
        )
        chooseMergedCleanupResult(
            original = originalForeground,
            plate = plateResult,
            full = fullResult,
        )
    } else {
        fullResult
    }
    val cleanup = IconCandidate(
        recfgRaw = cleanupResult.bitmap,
        recbg = localSource.recbg,
        monochromeRaw = localSource.monochrome,
        monochromeIsNative = localSource.monochromeIsNative,
        preserveGeometry = localSource.preserveGeometry,
        applyLocalEdgePolish = pipeline.edgePolishEnabled,
    )
    val composedBackground = if (
        pipeline.composedBackgroundEnabled && pipeline.plainBackgroundEstimationEnabled
    ) {
        buildComposedBackgroundCandidate(
            source = sourceIcon,
            monochrome = localSource.monochrome,
            monochromeIsNative = localSource.monochromeIsNative,
            pipeline = pipeline,
            backgroundSeparationPercent = backgroundSeparationPercent,
            plateRemovalPercent = plateRemovalPercent,
            shadowRemovalPercent = shadowRemovalPercent,
        )
    } else {
        null
    }
    val twoLayerResult = if (pipeline.twoLayerCandidateEnabled) buildTwoLayerCandidate(sourceIcon, pipeline) else null
    val candidates = linkedMapOf<PreviewChoice, IconCandidate>(
        PreviewChoice.Original to original,
        PreviewChoice.Full to cleanup,
    )
    if (composedBackground != null) {
        candidates[PreviewChoice.ComposedBackground] = composedBackground
    }
    if (pipeline.textSafeCandidateEnabled && localSource.textSafe != null) {
        candidates[PreviewChoice.TextSafe] = localSource.textSafe
    }
    if (pipeline.componentCandidatesEnabled && localSource.componentSubject != null) {
        candidates[PreviewChoice.ComponentSubject] = localSource.componentSubject
    }
    if (pipeline.componentCandidatesEnabled && localSource.componentBackground != null) {
        candidates[PreviewChoice.ComponentBackground] = localSource.componentBackground
    }
    if (twoLayerResult?.candidate != null) {
        candidates[PreviewChoice.TwoLayer] = twoLayerResult.candidate
    }
    val autoChoice = if (pipeline.autoSelectionEnabled) {
        chooseAutoLocalChoice(
            original = originalForeground,
            cleanup = cleanupResult.bitmap,
            twoLayer = twoLayerResult,
            rmbg = null,
        ).takeIf { candidates.containsKey(it) } ?: PreviewChoice.Original
    } else {
        defaultLocalChoiceFromAvailable(candidates)
    }
    return LocalCandidateSet(candidates = candidates, autoChoice = autoChoice)
}

internal fun defaultLocalChoiceFromAvailable(candidates: Map<PreviewChoice, IconCandidate>): PreviewChoice =
    when {
        candidates.containsKey(PreviewChoice.Full) -> PreviewChoice.Full
        candidates.containsKey(PreviewChoice.Original) -> PreviewChoice.Original
        else -> candidates.keys.firstOrNull() ?: PreviewChoice.Original
    }

internal fun chooseMergedCleanupResult(
    original: Bitmap,
    plate: LocalSeparationResult,
    full: LocalSeparationResult,
): LocalSeparationResult {
    val originalCoverage = meaningfulAlphaCoverage(original)
    val plateCoverage = meaningfulAlphaCoverage(plate.bitmap)
    val fullCoverage = meaningfulAlphaCoverage(full.bitmap)
    val plateUsable = isAutoLocalCandidateUsable(
        candidate = plate.bitmap,
        originalCoverage = originalCoverage,
        candidateCoverage = plateCoverage,
    )
    val fullUsable = isAutoLocalCandidateUsable(
        candidate = full.bitmap,
        originalCoverage = originalCoverage,
        candidateCoverage = fullCoverage,
    )
    return when {
        fullUsable && (!plateUsable || fullCoverage <= plateCoverage + AUTO_COVERAGE_CHANGE_THRESHOLD) -> full
        plateUsable -> plate.copy(summary = plate.summary.replace("去底板", "清理"))
        else -> full
    }
}

internal fun buildComposedBackgroundCandidate(
    source: Bitmap,
    monochrome: Bitmap?,
    monochromeIsNative: Boolean,
    pipeline: LocalPipelineConfig,
    backgroundSeparationPercent: Int,
    plateRemovalPercent: Int,
    shadowRemovalPercent: Int,
): IconCandidate {
    val normalizedSource = if (source.width == SIZE_1X1 && source.height == SIZE_1X1) {
        source
    } else {
        resizeBitmap(source, SIZE_1X1, SIZE_1X1)
    }
    val recbg = solidBitmap(
        SIZE_1X1,
        SIZE_1X1,
        if (pipeline.plainBackgroundEstimationEnabled) {
            estimatePlainIconBackground(normalizedSource)
        } else {
            AndroidColor.TRANSPARENT
        },
    )
    val extracted = if (pipeline.backgroundSeparationEnabled) {
        subtractPlainIconBackground(normalizedSource, recbg, pipeline, backgroundSeparationPercent)
    } else {
        normalizedSource
    }
    val background = rebuildComposedIconBackground(normalizedSource, extracted, recbg)
    val cleaned = separateLocalForeground(
        source = extracted,
        background = background,
        mode = LocalSeparationMode.ComposedBackground,
        pipeline = pipeline,
        plateRemovalPercent = plateRemovalPercent,
        shadowRemovalPercent = shadowRemovalPercent,
    ).bitmap
    return IconCandidate(
        recfgRaw = cleaned,
        recbg = background,
        monochromeRaw = monochrome,
        monochromeIsNative = monochromeIsNative,
        preserveGeometry = false,
        applyLocalEdgePolish = pipeline.edgePolishEnabled,
    )
}

internal fun rebuildComposedIconBackground(source: Bitmap, extractedForeground: Bitmap, fallbackBackground: Bitmap): Bitmap {
    val width = source.width
    val height = source.height
    val sourcePixels = IntArray(width * height)
    val fallbackPixels = IntArray(width * height)
    val foregroundPixels = IntArray(width * height)
    source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
    val fallback = if (fallbackBackground.width == width && fallbackBackground.height == height) {
        fallbackBackground
    } else {
        resizeBitmap(fallbackBackground, width, height)
    }
    fallback.getPixels(fallbackPixels, 0, width, 0, 0, width, height)
    extractedForeground.getPixels(foregroundPixels, 0, width, 0, 0, width, height)

    val subjectMask = BooleanArray(width * height)
    for (i in foregroundPixels.indices) {
        subjectMask[i] = AndroidColor.alpha(foregroundPixels[i]) > COMPOSED_BACKGROUND_SUBJECT_ALPHA_THRESHOLD
    }
    val fillMask = dilateMask(subjectMask, width, height, COMPOSED_BACKGROUND_FILL_RADIUS)
    val outPixels = sourcePixels.copyOf()
    for (i in outPixels.indices) {
        if (fillMask[i] || AndroidColor.alpha(outPixels[i]) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
            outPixels[i] = AndroidColor.argb(
                255,
                AndroidColor.red(fallbackPixels[i]),
                AndroidColor.green(fallbackPixels[i]),
                AndroidColor.blue(fallbackPixels[i]),
            )
        } else if (AndroidColor.alpha(outPixels[i]) < 255) {
            val pixel = outPixels[i]
            outPixels[i] = AndroidColor.argb(
                255,
                AndroidColor.red(pixel),
                AndroidColor.green(pixel),
                AndroidColor.blue(pixel),
            )
        }
    }
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return out
}

internal fun buildTwoLayerCandidate(
    source: Bitmap,
    pipeline: LocalPipelineConfig,
): CandidateBuildResult? {
    val width = source.width
    val height = source.height
    if (width <= 0 || height <= 0) {
        return null
    }
    val pixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    val borderColor = medianEdgeColor(pixels, width, height)
    val plateLike = BooleanArray(pixels.size)
    for (i in pixels.indices) {
        val pixel = pixels[i]
        plateLike[i] = AndroidColor.alpha(pixel) > LOCAL_ALPHA_VISIBLE_THRESHOLD &&
            colorDistance(pixel, borderColor) >= TWO_LAYER_PLATE_BACKGROUND_DISTANCE
    }
    val plateMask = largestConnectedMask(plateLike, width, height)
    val platePixels = plateMask.count { it }
    if (platePixels == 0) {
        return null
    }
    val plateCoverage = platePixels.toDouble() / pixels.size.toDouble()
    if (plateCoverage !in TWO_LAYER_MIN_PLATE_COVERAGE..TWO_LAYER_MAX_PLATE_COVERAGE) {
        return null
    }

    val plateDilated = dilateMask(plateMask, width, height, TWO_LAYER_SUBJECT_PLATE_DILATE_RADIUS)
    val subjectLike = BooleanArray(pixels.size)
    for (i in pixels.indices) {
        val pixel = pixels[i]
        subjectLike[i] = plateDilated[i] &&
            AndroidColor.alpha(pixel) > LOCAL_ALPHA_VISIBLE_THRESHOLD &&
            colorDistance(pixel, borderColor) <= TWO_LAYER_SUBJECT_BACKGROUND_DISTANCE
    }

    val subjectMask = BooleanArray(pixels.size)
    val minArea = maxOf(TWO_LAYER_MIN_SUBJECT_PIXELS, (pixels.size * TWO_LAYER_MIN_SUBJECT_COVERAGE).toInt())
    val maxArea = (platePixels * TWO_LAYER_MAX_SUBJECT_TO_PLATE_RATIO).toInt().coerceAtLeast(minArea)
    connectedMaskComponents(subjectLike, width, height).forEach { component ->
        if (component.touchesEdge || component.size < minArea || component.size > maxArea) {
            return@forEach
        }
        val boundsArea = component.bounds.width() * component.bounds.height()
        if (boundsArea <= 0) {
            return@forEach
        }
        val fillRatio = component.size.toDouble() / boundsArea.toDouble()
        if (fillRatio < TWO_LAYER_MIN_SUBJECT_FILL_RATIO) {
            return@forEach
        }
        if (boundsArea > platePixels * TWO_LAYER_MAX_SUBJECT_BOUNDS_TO_PLATE_RATIO) {
            return@forEach
        }
        component.indices.forEach { subjectMask[it] = true }
    }

    val closedSubjectMask = erodeMask(
        dilateMask(subjectMask, width, height, TWO_LAYER_SUBJECT_CLOSE_RADIUS),
        width,
        height,
        TWO_LAYER_SUBJECT_CLOSE_RADIUS,
    )
    val subjectPixels = closedSubjectMask.count { it }
    if (subjectPixels == 0) {
        return null
    }
    val subjectCoverage = subjectPixels.toDouble() / pixels.size.toDouble()
    val plateColor = dominantMaskColor(pixels, plateMask)
    val plateStd = maskColorStd(pixels, plateMask)
    val plateLuma = luma(plateColor).toDouble() / 255.0
    val manualUsable = subjectCoverage >= TWO_LAYER_MIN_MANUAL_SUBJECT_COVERAGE &&
        subjectCoverage <= plateCoverage * TWO_LAYER_MAX_SUBJECT_TO_PLATE_RATIO &&
        plateStd <= TWO_LAYER_MANUAL_MAX_PLATE_STD
    if (!manualUsable) {
        return null
    }

    val foreground = smoothAlphaEdges(
        applyMaskToSolidColor(
            width,
            height,
            closedSubjectMask,
            borderColor,
            repairEdges = pipeline.alphaEdgeColorRepairEnabled,
        ),
        TWO_LAYER_EDGE_SMOOTH_STRENGTH,
        growTransparentEdges = true,
        radius = TWO_LAYER_EDGE_SMOOTH_RADIUS,
        growStrength = TWO_LAYER_EDGE_GROW_STRENGTH,
    )
    val background = fillMaskOnSource(
        pixels = pixels,
        width = width,
        height = height,
        mask = dilateMask(closedSubjectMask, width, height, TWO_LAYER_BACKGROUND_FILL_RADIUS),
        color = plateColor,
    )
    val autoUsable = plateCoverage <= TWO_LAYER_AUTO_MAX_PLATE_COVERAGE &&
        subjectCoverage <= plateCoverage * TWO_LAYER_AUTO_MAX_SUBJECT_TO_PLATE_RATIO &&
        plateStd <= TWO_LAYER_AUTO_MAX_PLATE_STD &&
        plateLuma >= TWO_LAYER_AUTO_MIN_PLATE_LUMA
    return CandidateBuildResult(
        candidate = IconCandidate(
            recfgRaw = foreground,
            recbg = background,
            monochromeRaw = foreground,
            preserveGeometry = true,
            applyLocalEdgePolish = pipeline.edgePolishEnabled,
        ),
        autoUsable = autoUsable,
        coverage = subjectCoverage,
    )
}

internal fun chooseAutoLocalChoice(
    original: Bitmap,
    cleanup: Bitmap,
    twoLayer: CandidateBuildResult?,
    rmbg: CandidateBuildResult?,
): PreviewChoice {
    val originalCoverage = meaningfulAlphaCoverage(original)
    val cleanupCoverage = meaningfulAlphaCoverage(cleanup)
    val cleanupUsable = isAutoLocalCandidateUsable(
        candidate = cleanup,
        originalCoverage = originalCoverage,
        candidateCoverage = cleanupCoverage,
    )
    if (twoLayer?.autoUsable == true) {
        return PreviewChoice.TwoLayer
    }
    if (
        rmbg?.autoUsable == true &&
        rmbg.coverage < cleanupCoverage - AUTO_COVERAGE_CHANGE_THRESHOLD
    ) {
        return PreviewChoice.Rmbg
    }
    return when {
        cleanupUsable -> PreviewChoice.Full
        else -> PreviewChoice.Original
    }
}

internal fun isAutoLocalCandidateUsable(
    candidate: Bitmap,
    originalCoverage: Double,
    candidateCoverage: Double,
): Boolean {
    if (candidateCoverage >= originalCoverage - AUTO_COVERAGE_CHANGE_THRESHOLD) {
        return false
    }
    val bounds = meaningfulAlphaBounds(candidate) ?: return false
    return !hasAutoCropRisk(bounds, candidate.width, candidate.height)
}

internal fun medianEdgeColor(pixels: IntArray, width: Int, height: Int): Int {
    val reds = mutableListOf<Int>()
    val greens = mutableListOf<Int>()
    val blues = mutableListOf<Int>()
    fun add(pixel: Int) {
        if (AndroidColor.alpha(pixel) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
            return
        }
        reds += AndroidColor.red(pixel)
        greens += AndroidColor.green(pixel)
        blues += AndroidColor.blue(pixel)
    }
    for (x in 0 until width) {
        add(pixels[x])
        add(pixels[(height - 1) * width + x])
    }
    for (y in 0 until height) {
        add(pixels[y * width])
        add(pixels[y * width + width - 1])
    }
    return AndroidColor.rgb(median(reds), median(greens), median(blues))
}

internal fun dominantMaskColor(pixels: IntArray, mask: BooleanArray): Int {
    val reds = mutableListOf<Int>()
    val greens = mutableListOf<Int>()
    val blues = mutableListOf<Int>()
    for (i in pixels.indices) {
        if (!mask[i]) {
            continue
        }
        val pixel = pixels[i]
        if (saturation(pixel) < TWO_LAYER_DOMINANT_MIN_SATURATION || luma(pixel) > TWO_LAYER_DOMINANT_MAX_LUMA) {
            continue
        }
        reds += AndroidColor.red(pixel)
        greens += AndroidColor.green(pixel)
        blues += AndroidColor.blue(pixel)
    }
    if (reds.isEmpty()) {
        for (i in pixels.indices) {
            if (mask[i]) {
                val pixel = pixels[i]
                reds += AndroidColor.red(pixel)
                greens += AndroidColor.green(pixel)
                blues += AndroidColor.blue(pixel)
            }
        }
    }
    return AndroidColor.rgb(median(reds), median(greens), median(blues))
}

internal fun maskColorStd(pixels: IntArray, mask: BooleanArray): Double {
    var count = 0
    var redSum = 0.0
    var greenSum = 0.0
    var blueSum = 0.0
    for (i in pixels.indices) {
        if (!mask[i]) {
            continue
        }
        val pixel = pixels[i]
        redSum += AndroidColor.red(pixel)
        greenSum += AndroidColor.green(pixel)
        blueSum += AndroidColor.blue(pixel)
        count++
    }
    if (count == 0) {
        return Double.MAX_VALUE
    }
    val redMean = redSum / count
    val greenMean = greenSum / count
    val blueMean = blueSum / count
    var redVar = 0.0
    var greenVar = 0.0
    var blueVar = 0.0
    for (i in pixels.indices) {
        if (!mask[i]) {
            continue
        }
        val pixel = pixels[i]
        redVar += (AndroidColor.red(pixel) - redMean) * (AndroidColor.red(pixel) - redMean)
        greenVar += (AndroidColor.green(pixel) - greenMean) * (AndroidColor.green(pixel) - greenMean)
        blueVar += (AndroidColor.blue(pixel) - blueMean) * (AndroidColor.blue(pixel) - blueMean)
    }
    return (kotlin.math.sqrt(redVar / count) +
        kotlin.math.sqrt(greenVar / count) +
        kotlin.math.sqrt(blueVar / count)) / 3.0
}

internal fun largestConnectedMask(mask: BooleanArray, width: Int, height: Int): BooleanArray {
    val largest = connectedMaskComponents(mask, width, height).maxByOrNull { it.size }
        ?: return BooleanArray(mask.size)
    val out = BooleanArray(mask.size)
    largest.indices.forEach { out[it] = true }
    return out
}

internal fun dilateMask(mask: BooleanArray, width: Int, height: Int, radius: Int): BooleanArray {
    if (radius <= 0) {
        return mask.copyOf()
    }
    val out = BooleanArray(mask.size)
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (!mask[y * width + x]) {
                continue
            }
            for (dy in -radius..radius) {
                for (dx in -radius..radius) {
                    val nx = x + dx
                    val ny = y + dy
                    if (nx in 0 until width && ny in 0 until height) {
                        out[ny * width + nx] = true
                    }
                }
            }
        }
    }
    return out
}

internal fun erodeMask(mask: BooleanArray, width: Int, height: Int, radius: Int): BooleanArray {
    if (radius <= 0) {
        return mask.copyOf()
    }
    val out = BooleanArray(mask.size)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val index = y * width + x
            if (!mask[index]) {
                continue
            }
            var keep = true
            for (dy in -radius..radius) {
                for (dx in -radius..radius) {
                    val nx = x + dx
                    val ny = y + dy
                    if (nx !in 0 until width || ny !in 0 until height || !mask[ny * width + nx]) {
                        keep = false
                        break
                    }
                }
                if (!keep) {
                    break
                }
            }
            out[index] = keep
        }
    }
    return out
}

internal fun applyMaskToSolidColor(
    width: Int,
    height: Int,
    mask: BooleanArray,
    color: Int,
    repairEdges: Boolean = true,
): Bitmap {
    val outPixels = IntArray(width * height)
    for (i in outPixels.indices) {
        outPixels[i] = if (mask[i]) {
            AndroidColor.argb(
                255,
                AndroidColor.red(color),
                AndroidColor.green(color),
                AndroidColor.blue(color),
            )
        } else {
            AndroidColor.TRANSPARENT
        }
    }
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return if (repairEdges) repairTransparentEdgeColors(out) else out
}

internal fun fillMaskOnSource(
    pixels: IntArray,
    width: Int,
    height: Int,
    mask: BooleanArray,
    color: Int,
): Bitmap {
    val outPixels = pixels.copyOf()
    val fill = AndroidColor.rgb(
        AndroidColor.red(color),
        AndroidColor.green(color),
        AndroidColor.blue(color),
    )
    for (i in outPixels.indices) {
        if (mask[i]) {
            outPixels[i] = AndroidColor.argb(255, AndroidColor.red(fill), AndroidColor.green(fill), AndroidColor.blue(fill))
        } else if (AndroidColor.alpha(outPixels[i]) < 255) {
            outPixels[i] = AndroidColor.argb(
                255,
                AndroidColor.red(outPixels[i]),
                AndroidColor.green(outPixels[i]),
                AndroidColor.blue(outPixels[i]),
            )
        }
    }
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return out
}
