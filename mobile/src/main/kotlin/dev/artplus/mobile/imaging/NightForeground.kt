package dev.artplus.mobile

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor

/**
 * 夜间前景（night）管线：暗色主体提亮 + 白色主体重着色 + 暗背景适配。
 *
 * 从 MainActivity 迁移而来（P1.2-c 拆分）：原 `private fun`，现 `internal`，
 * 签名仅 `normalDarkForeground` 增加显式 `nightSubjectLightBackgroundEnabled` 参数
 * （原直读 Activity 状态），其余签名未变。
 * 随带 5 个纯 helper（recolorLocalNightEdgePixels / hasNearbyColoredOrTransparentContrast /
 * appWhiteFromBackground / darkSubjectNightTarget / blendNightWhiteChannel）。
 */

internal fun normalDarkForeground(
    source: Bitmap,
    darkBackground: Bitmap,
    nightSubjectLightBackgroundEnabled: Boolean,
): Bitmap {
    val preserved = nightForeground(
        source = source,
        background = darkBackground,
        preserveSubjectColors = true,
    )
    val boosted = supportNightForegroundWithColor(
        source = source,
        preserved = preserved,
        supportColor = NIGHT_APP_WHITE,
        maxBlend = NIGHT_DEFAULT_BOOST_MAX_BLEND,
    )
    return if (nightSubjectLightBackgroundEnabled) {
        supportNightForegroundWithColor(
            source = source,
            preserved = boosted,
            supportColor = sampleColor(darkBackground),
            maxBlend = NIGHT_FILL_BACKGROUND_MAX_BLEND,
        )
    } else {
        boosted
    }
}

internal fun supportNightForegroundWithColor(
    source: Bitmap,
    preserved: Bitmap,
    supportColor: Int,
    maxBlend: Double,
): Bitmap {
    val width = preserved.width
    val height = preserved.height
    val sourcePixels = IntArray(width * height)
    val preservedPixels = IntArray(width * height)
    val outPixels = IntArray(sourcePixels.size)
    source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
    preserved.getPixels(preservedPixels, 0, width, 0, 0, width, height)

    for (i in sourcePixels.indices) {
        val sourcePixel = sourcePixels[i]
        val preservedPixel = preservedPixels[i]
        val alpha = AndroidColor.alpha(preservedPixel)
        outPixels[i] = if (alpha <= 0) {
            AndroidColor.TRANSPARENT
        } else {
            val sourceLuma = luma(sourcePixel)
            val sourceSaturation = saturation(sourcePixel)
            val darkness = ((NIGHT_SUPPORT_MAX_LUMA - sourceLuma).toDouble() /
                (NIGHT_SUPPORT_MAX_LUMA - NIGHT_SUPPORT_MIN_LUMA).toDouble())
                .coerceIn(0.0, 1.0)
            val neutrality = ((NIGHT_SUPPORT_MAX_SATURATION - sourceSaturation) /
                NIGHT_SUPPORT_MAX_SATURATION)
                .coerceIn(0.0, 1.0)
            val blend = (darkness * neutrality * maxBlend)
                .takeUnless {
                    sourceSaturation >= NIGHT_SUPPORT_PRESERVE_SATURATION ||
                        sourceLuma >= NIGHT_SUPPORT_PRESERVE_LUMA
                }
                ?: 0.0
            AndroidColor.argb(
                alpha,
                blendChannel(AndroidColor.red(preservedPixel), AndroidColor.red(supportColor), blend),
                blendChannel(AndroidColor.green(preservedPixel), AndroidColor.green(supportColor), blend),
                blendChannel(AndroidColor.blue(preservedPixel), AndroidColor.blue(supportColor), blend),
            )
        }
    }

    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return smoothAlphaEdges(
        featherVisibleEdges(repairTransparentEdgeColors(out), NIGHT_EDGE_FEATHER_BLEND),
        NIGHT_EDGE_SMOOTH_STRENGTH,
    )
}

internal fun nightForeground(
    source: Bitmap,
    background: Bitmap,
    preserveSubjectColors: Boolean = false,
): Bitmap {
    val width = source.width
    val height = source.height
    val sourcePixels = IntArray(width * height)
    val outPixels = IntArray(sourcePixels.size)
    source.getPixels(sourcePixels, 0, width, 0, 0, width, height)

    var visibleWeight = 0.0
    var whiteWeight = 0.0
    var darkWeight = 0.0
    var colorWeight = 0.0
    for (i in sourcePixels.indices) {
        val pixel = sourcePixels[i]
        val alpha = AndroidColor.alpha(pixel)
        if (alpha <= 0) {
            outPixels[i] = AndroidColor.TRANSPARENT
            continue
        }
        outPixels[i] = AndroidColor.argb(
            alpha,
            liftDarkChannel(AndroidColor.red(pixel)),
            liftDarkChannel(AndroidColor.green(pixel)),
            liftDarkChannel(AndroidColor.blue(pixel)),
        )
        if (alpha <= NIGHT_VISIBLE_ALPHA_THRESHOLD) {
            continue
        }
        val weight = alpha / 255.0
        visibleWeight += weight
        val pixelLuma = luma(pixel)
        val pixelSaturation = saturation(pixel)
        if (pixelLuma >= NIGHT_WHITE_LUMA_THRESHOLD && pixelSaturation <= NIGHT_WHITE_MAX_SATURATION) {
            whiteWeight += weight
        }
        if (pixelLuma <= NIGHT_DARK_LUMA_THRESHOLD) {
            darkWeight += weight
        }
        if (
            pixelLuma > NIGHT_DARK_COLOR_IGNORE_LUMA_THRESHOLD &&
            pixelSaturation >= NIGHT_COLOR_SATURATION_THRESHOLD
        ) {
            colorWeight += weight
        }
    }

    if (visibleWeight > 0.0) {
        val bgColor = sampleColor(background)
        val bgLuma = luma(bgColor)
        val bgSaturation = saturation(bgColor)
        val nightWhiteTarget = if (bgLuma <= NIGHT_BACKGROUND_DARK_LUMA_THRESHOLD) {
            NIGHT_APP_WHITE
        } else {
            bgColor
        }
        val whiteRatio = whiteWeight / visibleWeight
        val darkRatio = darkWeight / visibleWeight
        val colorRatio = colorWeight / visibleWeight
        val flatLightSubject = isFlatLightNightSubject(
            sourcePixels = sourcePixels,
            whiteRatio = whiteRatio,
            darkRatio = darkRatio,
            colorRatio = colorRatio,
        )
        when {
            flatLightSubject &&
                whiteRatio >= NIGHT_COLORED_BACKGROUND_WHITE_RATIO_THRESHOLD &&
                bgSaturation >= NIGHT_COLORED_BACKGROUND_MIN_SATURATION &&
                bgLuma >= NIGHT_BACKGROUND_COLORED_LUMA_THRESHOLD &&
                darkRatio <= NIGHT_COLORED_BACKGROUND_DARK_RATIO_MAX &&
                colorRatio <= NIGHT_COLORED_BACKGROUND_COLOR_RATIO_MAX -> {
                recolorNightPixels(
                    sourcePixels = sourcePixels,
                    outPixels = outPixels,
                    target = bgColor,
                ) { pixel ->
                    isNightWhiteSubjectPixel(pixel, includeSoftEdge = true)
                }
            }
            flatLightSubject &&
                whiteRatio >= NIGHT_WHITE_RATIO_THRESHOLD &&
                colorRatio <= NIGHT_COLOR_RATIO_MAX -> {
                recolorNightPixels(
                    sourcePixels = sourcePixels,
                    outPixels = outPixels,
                    target = nightWhiteTarget,
                ) { pixel ->
                    isNightWhiteSubjectPixel(pixel, includeSoftEdge = true)
                }
            }
            !preserveSubjectColors &&
                darkRatio >= NIGHT_DARK_RATIO_THRESHOLD &&
                whiteRatio <= NIGHT_DARK_MAX_WHITE_RATIO &&
                colorRatio <= NIGHT_DARK_COLOR_RATIO_MAX &&
                bgLuma >= NIGHT_BACKGROUND_COLORED_LUMA_THRESHOLD -> {
                recolorNightPixels(
                    sourcePixels = sourcePixels,
                    outPixels = outPixels,
                    target = darkSubjectNightTarget(bgColor, bgLuma, bgSaturation),
                ) { pixel ->
                    AndroidColor.alpha(pixel) > NIGHT_VISIBLE_ALPHA_THRESHOLD
                }
            }
        }
        if (flatLightSubject) {
            recolorLocalNightEdgePixels(
                sourcePixels = sourcePixels,
                outPixels = outPixels,
                width = width,
                height = height,
                target = nightWhiteTarget,
            )
        }
    }

    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return smoothAlphaEdges(
        featherVisibleEdges(repairTransparentEdgeColors(out), NIGHT_EDGE_FEATHER_BLEND),
        NIGHT_EDGE_SMOOTH_STRENGTH,
    )
}

internal fun recolorLocalNightEdgePixels(
    sourcePixels: IntArray,
    outPixels: IntArray,
    width: Int,
    height: Int,
    target: Int,
) {
    for (y in 0 until height) {
        for (x in 0 until width) {
            val index = y * width + x
            val source = sourcePixels[index]
            if (!isNightWhiteSubjectPixel(source)) {
                continue
            }
            if (!hasNearbyColoredOrTransparentContrast(sourcePixels, width, height, x, y)) {
                continue
            }
            val alpha = AndroidColor.alpha(source)
            outPixels[index] = AndroidColor.argb(
                alpha,
                AndroidColor.red(target),
                AndroidColor.green(target),
                AndroidColor.blue(target),
            )
        }
    }
}

internal fun isFlatLightNightSubject(
    sourcePixels: IntArray,
    whiteRatio: Double,
    darkRatio: Double,
    colorRatio: Double,
): Boolean {
    if (
        darkRatio > NIGHT_FLAT_LIGHT_DARK_RATIO_MAX ||
        colorRatio > NIGHT_FLAT_LIGHT_COLOR_RATIO_MAX
    ) {
        return false
    }

    var strongWeight = 0.0
    var lightWeight = 0.0
    var veryLightWeight = 0.0
    var saturatedWeight = 0.0
    val lumas = mutableListOf<Int>()

    for (pixel in sourcePixels) {
        val alpha = AndroidColor.alpha(pixel)
        if (alpha <= NIGHT_FLAT_LIGHT_ALPHA_THRESHOLD) {
            continue
        }
        val weight = alpha / 255.0
        val pixelLuma = luma(pixel)
        val pixelSaturation = saturation(pixel)
        strongWeight += weight
        lumas += pixelLuma
        if (
            pixelLuma >= NIGHT_FLAT_LIGHT_LUMA_THRESHOLD &&
            pixelSaturation <= NIGHT_FLAT_LIGHT_MAX_SATURATION
        ) {
            lightWeight += weight
        }
        if (
            pixelLuma >= NIGHT_WHITE_LUMA_THRESHOLD &&
            pixelSaturation <= NIGHT_WHITE_MAX_SATURATION
        ) {
            veryLightWeight += weight
        }
        if (pixelSaturation >= NIGHT_FLAT_LIGHT_SATURATED_THRESHOLD) {
            saturatedWeight += weight
        }
    }

    if (strongWeight <= 0.0 || lumas.size < NIGHT_FLAT_LIGHT_MIN_PIXELS) {
        return false
    }

    val lightRatio = lightWeight / strongWeight
    val veryLightRatio = veryLightWeight / strongWeight
    val saturatedRatio = saturatedWeight / strongWeight
    val lumaRange = percentile(lumas, 0.90) - percentile(lumas, 0.10)
    val dominantWhiteMark =
        whiteRatio >= NIGHT_WHITE_RATIO_THRESHOLD &&
            lightRatio >= NIGHT_FLAT_LIGHT_RATIO_MIN &&
            veryLightRatio >= NIGHT_FLAT_VERY_LIGHT_RATIO_MIN
    val solidPaleMark =
        lightRatio >= NIGHT_FLAT_PALE_RATIO_MIN &&
            veryLightRatio >= NIGHT_FLAT_PALE_VERY_LIGHT_RATIO_MIN &&
            lumaRange <= NIGHT_FLAT_LIGHT_LUMA_RANGE_MAX

    return saturatedRatio <= NIGHT_FLAT_LIGHT_SATURATED_RATIO_MAX &&
        (dominantWhiteMark || solidPaleMark)
}

internal fun hasNearbyColoredOrTransparentContrast(
    pixels: IntArray,
    width: Int,
    height: Int,
    x: Int,
    y: Int,
): Boolean {
    for (dy in -NIGHT_EDGE_CONTRAST_RADIUS..NIGHT_EDGE_CONTRAST_RADIUS) {
        for (dx in -NIGHT_EDGE_CONTRAST_RADIUS..NIGHT_EDGE_CONTRAST_RADIUS) {
            if (dx == 0 && dy == 0) {
                continue
            }
            val nx = x + dx
            val ny = y + dy
            if (nx !in 0 until width || ny !in 0 until height) {
                return true
            }
            val neighbor = pixels[ny * width + nx]
            val alpha = AndroidColor.alpha(neighbor)
            if (alpha <= NIGHT_VISIBLE_ALPHA_THRESHOLD) {
                return true
            }
            if (saturation(neighbor) >= NIGHT_EDGE_COLORED_NEIGHBOR_SATURATION) {
                return true
            }
            if (luma(neighbor) <= NIGHT_EDGE_DARK_NEIGHBOR_LUMA) {
                return true
            }
        }
    }
    return false
}

internal fun isNightWhiteSubjectPixel(pixel: Int, includeSoftEdge: Boolean = false): Boolean {
    if (AndroidColor.alpha(pixel) <= NIGHT_VISIBLE_ALPHA_THRESHOLD) {
        return false
    }
    val pixelLuma = luma(pixel)
    val pixelSaturation = saturation(pixel)
    return (
        pixelLuma >= NIGHT_WHITE_LUMA_THRESHOLD &&
            pixelSaturation <= NIGHT_WHITE_MAX_SATURATION
        ) || (
        pixelLuma >= NIGHT_EDGE_WHITE_LUMA_THRESHOLD &&
            pixelSaturation <= NIGHT_EDGE_WHITE_MAX_SATURATION
        ) || (
        includeSoftEdge &&
            pixelLuma >= NIGHT_SOFT_EDGE_WHITE_LUMA_THRESHOLD &&
            pixelSaturation <= NIGHT_SOFT_EDGE_WHITE_MAX_SATURATION
        )
}

internal fun liftDarkChannel(value: Int): Int =
    (48 + value * 0.82f).toInt().coerceIn(0, 255)

internal fun recolorNightPixels(
    sourcePixels: IntArray,
    outPixels: IntArray,
    target: Int,
    shouldRecolor: (Int) -> Boolean,
) {
    for (i in sourcePixels.indices) {
        val source = sourcePixels[i]
        val alpha = AndroidColor.alpha(source)
        if (alpha <= NIGHT_VISIBLE_ALPHA_THRESHOLD || !shouldRecolor(source)) {
            continue
        }
        outPixels[i] = AndroidColor.argb(
            alpha,
            AndroidColor.red(target),
            AndroidColor.green(target),
            AndroidColor.blue(target),
        )
    }
}

internal fun appWhiteFromBackground(bgColor: Int): Int {
    val bgLuma = luma(bgColor)
    val bgSaturation = saturation(bgColor)
    if (
        bgLuma <= NIGHT_BACKGROUND_DARK_LUMA_THRESHOLD ||
        (bgLuma >= NIGHT_DIRECT_WHITE_LUMA_THRESHOLD &&
            bgSaturation <= NIGHT_DIRECT_WHITE_MAX_SATURATION)
    ) {
        return NIGHT_APP_WHITE
    }
    return AndroidColor.rgb(
        blendNightWhiteChannel(AndroidColor.red(bgColor), AndroidColor.red(NIGHT_APP_WHITE)),
        blendNightWhiteChannel(AndroidColor.green(bgColor), AndroidColor.green(NIGHT_APP_WHITE)),
        blendNightWhiteChannel(AndroidColor.blue(bgColor), AndroidColor.blue(NIGHT_APP_WHITE)),
    )
}

internal fun darkSubjectNightTarget(bgColor: Int, bgLuma: Int, bgSaturation: Double): Int {
    if (bgLuma >= NIGHT_BACKGROUND_LIGHT_LUMA_THRESHOLD &&
        bgSaturation <= NIGHT_BACKGROUND_LIGHT_MAX_SATURATION
    ) {
        return appWhiteFromBackground(bgColor)
    }
    return AndroidColor.rgb(
        AndroidColor.red(bgColor),
        AndroidColor.green(bgColor),
        AndroidColor.blue(bgColor),
    )
}

internal fun blendNightWhiteChannel(background: Int, appWhite: Int): Int =
    (background * NIGHT_BACKGROUND_WHITE_BLEND + appWhite * (1.0 - NIGHT_BACKGROUND_WHITE_BLEND))
        .toInt()
        .coerceIn(0, 255)
