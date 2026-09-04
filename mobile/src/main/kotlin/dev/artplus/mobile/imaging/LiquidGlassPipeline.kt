package dev.artplus.mobile

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Color as AndroidColor
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * 液态玻璃纯算法管线：背景/前景分层渲染 + 主体不透明度 + 光照描边 + 几何遮罩。
 *
 * 从 MainActivity 迁移而来（Epic v2 Phase 1 Slice 1.1 拆分）：原 `internal fun`，现顶层 `internal`。
 * 读 Activity 调参状态的 6 个函数（liquidGlassBackgroundForSize / foregroundForSize /
 * renderLayeredLiquidGlassBackground / renderLayeredLiquidGlassForeground /
 * drawLayeredLiquidGlassLight / liquidGlassRadiusForSize）改为显式收参，不读任何 Activity 状态；
 * 其余 19 个纯函数签名未变。Activity 内保留原签名 wrapper 委托，原有调用点零改动。
 * 同包直接复用 resizeBitmap / centerOnCanvas / solidBitmap / percentile / ForegroundShadowParams。
 * 非玻璃分支的阴影（applyForegroundShadow）与主体阴影（subjectShadowBitmap）仍在 Activity 内，
 * 以函数参数注入，保持行为等价。
 */

internal fun liquidGlassBackgroundForSize(
    source: Bitmap,
    width: Int,
    height: Int,
    forceLiquidGlass: Boolean = false,
    liquidGlassEnabled: Boolean,
    liquidGlassBackgroundMistAlpha: Int,
    liquidGlassTopAlpha: Int,
    liquidGlassBottomAlpha: Int,
    liquidGlassBottomDarkAlpha: Int,
    liquidGlassOuterWidth: Int,
    liquidGlassRadius: Int,
): Bitmap {
    val resized = if (source.width == width && source.height == height) {
        source
    } else {
        resizeBitmap(source, width, height)
    }
    return if (forceLiquidGlass || liquidGlassEnabled) {
        renderLayeredLiquidGlassBackground(
            resized,
            liquidGlassBackgroundMistAlpha,
            liquidGlassTopAlpha,
            liquidGlassBottomAlpha,
            liquidGlassBottomDarkAlpha,
            liquidGlassOuterWidth,
            liquidGlassRadius,
        )
    } else {
        resized
    }
}

internal fun foregroundForSize(
    source: Bitmap,
    width: Int,
    height: Int,
    forceLiquidGlass: Boolean = false,
    liquidGlassEnabled: Boolean,
    liquidGlassSubjectScalePercent: Int,
    liquidGlassSubjectShadowAlpha: Int,
    liquidGlassSubjectOutlineWidth: Int,
    liquidGlassSubjectInnerOutlineWidth: Int,
    liquidGlassSubjectOpacityPercent: Int,
    liquidGlassTopAlpha: Int,
    liquidGlassBottomAlpha: Int,
    liquidGlassBottomDarkAlpha: Int,
    liquidGlassOuterWidth: Int,
    liquidGlassRadius: Int,
    applyShadow: (Bitmap) -> Bitmap,
    renderSubjectShadow: (Bitmap, ForegroundShadowParams) -> Bitmap,
): Bitmap {
    val sized = if (source.width == width && source.height == height) {
        source
    } else {
        centerOnCanvas(source, width, height)
    }
    return if (forceLiquidGlass || liquidGlassEnabled) {
        renderLayeredLiquidGlassForeground(
            sized,
            liquidGlassSubjectScalePercent,
            liquidGlassSubjectShadowAlpha,
            liquidGlassSubjectOutlineWidth,
            liquidGlassSubjectInnerOutlineWidth,
            liquidGlassSubjectOpacityPercent,
            liquidGlassTopAlpha,
            liquidGlassBottomAlpha,
            liquidGlassBottomDarkAlpha,
            liquidGlassOuterWidth,
            liquidGlassRadius,
            renderSubjectShadow,
        )
    } else {
        applyShadow(sized)
    }
}

internal fun renderLayeredLiquidGlassBackground(
    source: Bitmap,
    liquidGlassBackgroundMistAlpha: Int,
    liquidGlassTopAlpha: Int,
    liquidGlassBottomAlpha: Int,
    liquidGlassBottomDarkAlpha: Int,
    liquidGlassOuterWidth: Int,
    liquidGlassRadius: Int,
): Bitmap {
    val width = source.width
    val height = source.height
    val radius = liquidGlassRadiusForSize(width, height, liquidGlassRadius)
    val shapeMask = roundedRectMaskAlpha(width, height, radius, feather = liquidGlassMaskFeather(width, height))
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(out)
    canvas.drawColor(AndroidColor.TRANSPARENT)
    canvas.drawBitmap(source, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))

    val mistAlpha = liquidGlassBackgroundMistAlpha.coerceIn(MIN_LIQUID_GLASS_MIST_ALPHA, MAX_LIQUID_GLASS_MIST_ALPHA)
    if (mistAlpha > 0) {
        canvas.drawRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.argb(mistAlpha, 0, 0, 0)
            },
        )
    }
    drawLayeredLiquidGlassLight(
        canvas,
        width,
        height,
        radius,
        liquidGlassTopAlpha,
        liquidGlassBottomAlpha,
        liquidGlassBottomDarkAlpha,
        liquidGlassOuterWidth,
    )
    return applyAlphaMask(out, shapeMask)
}

internal fun renderLayeredLiquidGlassForeground(
    source: Bitmap,
    liquidGlassSubjectScalePercent: Int,
    liquidGlassSubjectShadowAlpha: Int,
    liquidGlassSubjectOutlineWidth: Int,
    liquidGlassSubjectInnerOutlineWidth: Int,
    liquidGlassSubjectOpacityPercent: Int,
    liquidGlassTopAlpha: Int,
    liquidGlassBottomAlpha: Int,
    liquidGlassBottomDarkAlpha: Int,
    liquidGlassOuterWidth: Int,
    liquidGlassRadius: Int,
    renderSubjectShadow: (Bitmap, ForegroundShadowParams) -> Bitmap,
): Bitmap {
    val width = source.width
    val height = source.height
    val radius = liquidGlassRadiusForSize(width, height, liquidGlassRadius)
    val shapeMask = roundedRectMaskAlpha(width, height, radius, feather = liquidGlassMaskFeather(width, height))
    val subject = scaleBitmapAroundCanvasCenter(
        source,
        liquidGlassSubjectScalePercent
            .coerceIn(MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT, MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT)
            .toFloat() / 100f,
    )
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(out)
    canvas.drawColor(AndroidColor.TRANSPARENT)

    val subjectShadowAlpha = liquidGlassSubjectShadowAlpha
        .coerceIn(MIN_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA, MAX_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA)
    if (subjectShadowAlpha > 0) {
        val minSide = minOf(width, height).coerceAtLeast(1)
        val params = ForegroundShadowParams(
            alpha = subjectShadowAlpha,
            blurRadius = minSide * 0.026f,
            offsetX = 0,
            offsetY = (minSide * 0.018f).roundToInt().coerceAtLeast(1),
            spread = 0,
        )
        val shadow = renderSubjectShadow(subject, params)
        canvas.drawBitmap(shadow, params.offsetX.toFloat(), params.offsetY.toFloat(), Paint(Paint.FILTER_BITMAP_FLAG))
    }

    val outlineWidth = liquidGlassScaledWidth(
        width,
        height,
        liquidGlassSubjectOutlineWidth.coerceIn(
            MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
            MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
        ),
    )
    if (outlineWidth > 0) {
        canvas.drawBitmap(
            subjectOutlineLayer(subject, outlineWidth, inner = false, alphaScale = 0.92f),
            0f,
            0f,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
        )
    }

    val innerOutlineWidth = liquidGlassScaledWidth(
        width,
        height,
        liquidGlassSubjectInnerOutlineWidth.coerceIn(
            MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
            MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
        ),
    )
    if (innerOutlineWidth > 0) {
        canvas.drawBitmap(
            subjectOutlineLayer(subject, innerOutlineWidth, inner = true, alphaScale = 0.76f),
            0f,
            0f,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
        )
    }

    val subjectOpacity = liquidGlassSubjectOpacityPercent
        .coerceIn(MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT, MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT)
    if (subjectOpacity > 0) {
        canvas.drawBitmap(
            applyLiquidGlassSubjectOpacity(subject, subjectOpacity),
            0f,
            0f,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
        )
    }
    return applyAlphaMask(out, shapeMask)
}

internal fun applyLiquidGlassSubjectOpacity(source: Bitmap, opacityPercent: Int): Bitmap {
    val targetAlpha = (opacityPercent.coerceIn(
        MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
        MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
    ) * 255f / 100f).roundToInt().coerceIn(0, 255)
    if (targetAlpha <= 0) {
        return solidBitmap(source.width, source.height, AndroidColor.TRANSPARENT)
    }

    val pixels = IntArray(source.width * source.height)
    source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
    if (targetAlpha >= 255) {
        val outPixels = IntArray(pixels.size)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = AndroidColor.alpha(pixel)
            outPixels[i] = if (alpha <= 0) {
                AndroidColor.TRANSPARENT
            } else {
                val red = unpremultiplyChannel(AndroidColor.red(pixel), alpha)
                val green = unpremultiplyChannel(AndroidColor.green(pixel), alpha)
                val blue = unpremultiplyChannel(AndroidColor.blue(pixel), alpha)
                AndroidColor.argb(255, red, green, blue)
            }
        }
        return Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888).apply {
            setPixels(outPixels, 0, source.width, 0, 0, source.width, source.height)
        }
    }
    val alphaScaleBase = liquidGlassSubjectAlphaScaleBase(pixels)
    if (alphaScaleBase <= 0) {
        return source
    }
    val solidAlphaCutoff = (alphaScaleBase * LIQUID_GLASS_SUBJECT_SOLID_ALPHA_RATIO)
        .roundToInt()
        .coerceIn(LOCAL_ALPHA_VISIBLE_THRESHOLD + 1, 255)

    val outPixels = IntArray(pixels.size)
    for (i in pixels.indices) {
        val pixel = pixels[i]
        val alpha = AndroidColor.alpha(pixel)
        outPixels[i] = if (alpha <= 0) {
            AndroidColor.TRANSPARENT
        } else {
            val red = unpremultiplyChannel(AndroidColor.red(pixel), alpha)
            val green = unpremultiplyChannel(AndroidColor.green(pixel), alpha)
            val blue = unpremultiplyChannel(AndroidColor.blue(pixel), alpha)
            val normalizedAlpha = liquidGlassSubjectNormalizedAlpha(alpha, solidAlphaCutoff)
            val outAlpha = (normalizedAlpha * targetAlpha).roundToInt().coerceIn(0, 255)
            if (outAlpha <= 0) {
                AndroidColor.TRANSPARENT
            } else {
                AndroidColor.argb(outAlpha, red, green, blue)
            }
        }
    }
    val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, source.width, 0, 0, source.width, source.height)
    return out
}

internal fun unpremultiplyChannel(channel: Int, alpha: Int): Int {
    if (alpha <= 0) {
        return 0
    }
    if (alpha >= 255) {
        return channel.coerceIn(0, 255)
    }
    return ((channel * 255f) / alpha.toFloat()).roundToInt().coerceIn(0, 255)
}

internal fun liquidGlassSubjectNormalizedAlpha(alpha: Int, solidAlphaCutoff: Int): Float {
    if (alpha >= solidAlphaCutoff) {
        return 1f
    }
    val range = (solidAlphaCutoff - LOCAL_ALPHA_VISIBLE_THRESHOLD)
        .coerceAtLeast(1)
        .toFloat()
    val t = ((alpha - LOCAL_ALPHA_VISIBLE_THRESHOLD) / range).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

internal fun liquidGlassSubjectAlphaScaleBase(pixels: IntArray): Int {
    val visibleAlpha = pixels
        .asSequence()
        .map { AndroidColor.alpha(it) }
        .filter { it > LOCAL_ALPHA_VISIBLE_THRESHOLD }
        .toMutableList()
    if (visibleAlpha.isEmpty()) {
        return 0
    }
    val highAlpha = percentile(visibleAlpha, LIQUID_GLASS_SUBJECT_ALPHA_NORMALIZE_PERCENTILE)
    val bodyAlpha = percentile(visibleAlpha, LIQUID_GLASS_SUBJECT_ALPHA_BODY_PERCENTILE)
    return minOf(
        highAlpha,
        (bodyAlpha * LIQUID_GLASS_SUBJECT_ALPHA_OUTLIER_CAP).roundToInt(),
    ).coerceIn(1, 255)
}

internal fun drawLayeredLiquidGlassLight(
    canvas: Canvas,
    width: Int,
    height: Int,
    radius: Float,
    liquidGlassTopAlpha: Int,
    liquidGlassBottomAlpha: Int,
    liquidGlassBottomDarkAlpha: Int,
    liquidGlassOuterWidth: Int,
) {
    val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
    val bottom = height.toFloat()
    val topAlpha = liquidGlassTopAlpha.coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)
    val bottomAlpha = liquidGlassBottomAlpha.coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)

    canvas.drawRoundRect(
        rect,
        radius,
        radius,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = LinearGradient(
                0f,
                0f,
                0f,
                bottom,
                intArrayOf(
                    whiteWithAlpha(14f),
                    whiteWithAlpha(0f),
                    whiteWithAlpha(0f),
                    whiteWithAlpha(bottomAlpha * 0.16f),
                ),
                floatArrayOf(0f, 0.35f, 0.70f, 1f),
                Shader.TileMode.CLAMP,
            )
        },
    )

    val bottomDarkAlpha = liquidGlassBottomDarkAlpha
        .coerceIn(MIN_LIQUID_GLASS_BOTTOM_DARK_ALPHA, MAX_LIQUID_GLASS_BOTTOM_DARK_ALPHA)
    if (bottomDarkAlpha > 0) {
        canvas.drawRoundRect(
            rect,
            radius,
            radius,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                shader = LinearGradient(
                    0f,
                    bottom - height * 0.38f,
                    0f,
                    bottom,
                    intArrayOf(
                        blackWithAlpha(0f),
                        blackWithAlpha(bottomDarkAlpha * 0.45f),
                        blackWithAlpha(bottomDarkAlpha.toFloat()),
                    ),
                    floatArrayOf(0f, 0.72f, 1f),
                    Shader.TileMode.CLAMP,
                )
            },
        )
    }

    val outerWidth = liquidGlassOuterWidth
        .coerceIn(MIN_LIQUID_GLASS_OUTER_WIDTH, MAX_LIQUID_GLASS_OUTER_WIDTH)
        .toFloat() * liquidGlassScaleForSize(width, height)
    if (outerWidth <= 0f) {
        return
    }
    canvas.drawRoundRect(
        rect,
        radius,
        radius,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = outerWidth
            shader = LinearGradient(
                0f,
                0f,
                0f,
                bottom,
                intArrayOf(
                    whiteWithAlpha(topAlpha.toFloat()),
                    whiteWithAlpha(topAlpha * 0.38f),
                    whiteWithAlpha(0f),
                    whiteWithAlpha(0f),
                    whiteWithAlpha(bottomAlpha * 0.36f),
                    whiteWithAlpha(bottomAlpha.toFloat()),
                ),
                floatArrayOf(0f, 0.13f, 0.42f, 0.70f, 0.92f, 1f),
                Shader.TileMode.CLAMP,
            )
        },
    )
}

internal fun liquidGlassRadiusForSize(width: Int, height: Int, liquidGlassRadius: Int): Float {
    val minSide = minOf(width, height).toFloat().coerceAtLeast(1f)
    return (liquidGlassRadius.coerceIn(MIN_LIQUID_GLASS_RADIUS, MAX_LIQUID_GLASS_RADIUS) * liquidGlassScaleForSize(width, height))
        .coerceIn(0f, minSide / 2f)
}

internal fun liquidGlassScaleForSize(width: Int, height: Int): Float =
    minOf(width, height).toFloat().coerceAtLeast(1f) / SIZE_1X1.toFloat()

internal fun liquidGlassMaskFeather(width: Int, height: Int): Float =
    maxOf(1f, liquidGlassScaleForSize(width, height))

internal fun liquidGlassScaledWidth(width: Int, height: Int, value: Int): Int =
    (value * liquidGlassScaleForSize(width, height)).roundToInt().coerceAtLeast(0)

internal fun scaleBitmapAroundCanvasCenter(source: Bitmap, scale: Float): Bitmap {
    val safeScale = scale.coerceIn(
        MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT / 100f,
        MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT / 100f,
    )
    if (safeScale in 0.995f..1.005f) {
        return source
    }
    val scaledWidth = (source.width * safeScale).roundToInt().coerceAtLeast(1)
    val scaledHeight = (source.height * safeScale).roundToInt().coerceAtLeast(1)
    val scaled = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
    val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    Canvas(out).apply {
        drawColor(AndroidColor.TRANSPARENT)
        drawBitmap(
            scaled,
            (source.width - scaledWidth) / 2f,
            (source.height - scaledHeight) / 2f,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
        )
    }
    return out
}

internal fun subjectOutlineLayer(source: Bitmap, width: Int, inner: Boolean, alphaScale: Float): Bitmap {
    val baseAlpha = bitmapAlphaArray(source)
    val edgeAlpha = if (inner) {
        val eroded = minFilterAlpha(baseAlpha, source.width, source.height, width)
        IntArray(baseAlpha.size) { index -> (baseAlpha[index] - eroded[index]).coerceIn(0, 255) }
    } else {
        val dilated = maxFilterAlpha(baseAlpha, source.width, source.height, width)
        IntArray(baseAlpha.size) { index -> (dilated[index] - baseAlpha[index]).coerceIn(0, 255) }
    }
    return alphaArrayToColorLayer(edgeAlpha, source.width, source.height, AndroidColor.WHITE, alphaScale)
}

internal fun maxFilterAlpha(alpha: IntArray, width: Int, height: Int, radius: Int): IntArray {
    if (radius <= 0) {
        return alpha.copyOf()
    }
    val horizontal = IntArray(alpha.size)
    val out = IntArray(alpha.size)
    for (y in 0 until height) {
        val row = y * width
        for (x in 0 until width) {
            var maxAlpha = 0
            val left = maxOf(0, x - radius)
            val right = minOf(width - 1, x + radius)
            for (cx in left..right) {
                maxAlpha = maxOf(maxAlpha, alpha[row + cx])
            }
            horizontal[row + x] = maxAlpha
        }
    }
    for (y in 0 until height) {
        for (x in 0 until width) {
            var maxAlpha = 0
            val top = maxOf(0, y - radius)
            val bottom = minOf(height - 1, y + radius)
            for (cy in top..bottom) {
                maxAlpha = maxOf(maxAlpha, horizontal[cy * width + x])
            }
            out[y * width + x] = maxAlpha
        }
    }
    return out
}

internal fun minFilterAlpha(alpha: IntArray, width: Int, height: Int, radius: Int): IntArray {
    if (radius <= 0) {
        return alpha.copyOf()
    }
    val horizontal = IntArray(alpha.size)
    val out = IntArray(alpha.size)
    for (y in 0 until height) {
        val row = y * width
        for (x in 0 until width) {
            if (x - radius < 0 || x + radius >= width) {
                horizontal[row + x] = 0
                continue
            }
            var minAlpha = 255
            for (cx in (x - radius)..(x + radius)) {
                minAlpha = minOf(minAlpha, alpha[row + cx])
            }
            horizontal[row + x] = minAlpha
        }
    }
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (y - radius < 0 || y + radius >= height) {
                out[y * width + x] = 0
                continue
            }
            var minAlpha = 255
            for (cy in (y - radius)..(y + radius)) {
                minAlpha = minOf(minAlpha, horizontal[cy * width + x])
            }
            out[y * width + x] = minAlpha
        }
    }
    return out
}

internal fun whiteWithAlpha(alpha: Float): Int =
    AndroidColor.argb(alpha.roundToInt().coerceIn(0, 255), 255, 255, 255)

internal fun blackWithAlpha(alpha: Float): Int =
    AndroidColor.argb(alpha.roundToInt().coerceIn(0, 255), 0, 0, 0)

internal fun applyAlphaMask(source: Bitmap, mask: IntArray): Bitmap {
    val width = source.width
    val height = source.height
    val pixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    for (i in pixels.indices) {
        val pixel = pixels[i]
        val alpha = (AndroidColor.alpha(pixel) * mask[i] / 255f).roundToInt().coerceIn(0, 255)
        pixels[i] = if (alpha <= 0) {
            AndroidColor.TRANSPARENT
        } else {
            AndroidColor.argb(alpha, AndroidColor.red(pixel), AndroidColor.green(pixel), AndroidColor.blue(pixel))
        }
    }
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(pixels, 0, width, 0, 0, width, height)
    return out
}

internal fun roundedRectMaskAlpha(width: Int, height: Int, radius: Float, feather: Float): IntArray {
    val mask = IntArray(width * height)
    val halfWidth = width * 0.5f
    val halfHeight = height * 0.5f
    val safeFeather = feather.coerceAtLeast(0.001f)
    val denom = safeFeather * 2f
    for (y in 0 until height) {
        val centeredY = y + 0.5f - halfHeight
        for (x in 0 until width) {
            val centeredX = x + 0.5f - halfWidth
            val distance = sdRoundedRectCentered(centeredX, centeredY, halfWidth, halfHeight, radius)
            val alpha = when {
                distance <= -safeFeather -> 255
                distance >= safeFeather -> 0
                else -> ((safeFeather - distance) / denom * 255f).roundToInt().coerceIn(0, 255)
            }
            mask[y * width + x] = alpha
        }
    }
    return mask
}

internal fun sdRoundedRectCentered(
    x: Float,
    y: Float,
    halfWidth: Float,
    halfHeight: Float,
    radius: Float,
): Float {
    val qx = abs(x) - (halfWidth - radius)
    val qy = abs(y) - (halfHeight - radius)
    val outside = vectorLength(maxOf(qx, 0f), maxOf(qy, 0f))
    val inside = minOf(maxOf(qx, qy), 0f)
    return outside + inside - radius
}

internal fun bitmapAlphaArray(source: Bitmap): IntArray {
    val pixels = IntArray(source.width * source.height)
    source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
    for (i in pixels.indices) {
        pixels[i] = AndroidColor.alpha(pixels[i])
    }
    return pixels
}

internal fun alphaArrayToColorLayer(
    alpha: IntArray,
    width: Int,
    height: Int,
    color: Int,
    alphaScale: Float,
): Bitmap {
    val outPixels = IntArray(alpha.size)
    val red = AndroidColor.red(color)
    val green = AndroidColor.green(color)
    val blue = AndroidColor.blue(color)
    for (i in alpha.indices) {
        val scaledAlpha = (alpha[i] * alphaScale).roundToInt().coerceIn(0, 255)
        outPixels[i] = if (scaledAlpha <= 0) {
            AndroidColor.TRANSPARENT
        } else {
            AndroidColor.argb(scaledAlpha, red, green, blue)
        }
    }
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return out
}

internal fun vectorLength(x: Float, y: Float): Float =
    sqrt((x * x + y * y).toDouble()).toFloat()
