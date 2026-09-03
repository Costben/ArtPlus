package dev.artplus.mobile

import android.graphics.Color as AndroidColor
import kotlin.math.roundToInt

/**
 * 纯色彩 / 数值数学函数。
 *
 * 从 MainActivity 迁移而来（原 private fun，签名未变）。
 * 全部为纯函数：只依赖参数与顶层常量，不读 Activity 状态。
 */

internal fun ratioPercent(percent: Int): Double =
    percent.coerceIn(0, 100).toDouble() / 100.0

internal fun lerpDouble(start: Double, end: Double, ratio: Double): Double =
    start + (end - start) * ratio.coerceIn(0.0, 1.0)

internal fun percentText(value: Double): String =
    "${(value * 100.0).toInt().coerceIn(0, 100)}%"

internal fun median(values: MutableList<Int>): Int {
    if (values.isEmpty()) {
        return 0
    }
    values.sort()
    return values[values.size / 2]
}

internal fun percentile(values: MutableList<Int>, ratio: Double): Int {
    if (values.isEmpty()) {
        return 0
    }
    values.sort()
    val index = ((values.size - 1) * ratio)
        .toInt()
        .coerceIn(0, values.size - 1)
    return values[index]
}

internal fun saturation(pixel: Int): Double {
    val max = maxOf(AndroidColor.red(pixel), AndroidColor.green(pixel), AndroidColor.blue(pixel))
    if (max <= 0) {
        return 0.0
    }
    val min = minOf(AndroidColor.red(pixel), AndroidColor.green(pixel), AndroidColor.blue(pixel))
    return (max - min).toDouble() / max.toDouble()
}

internal fun colorDistance(a: Int, b: Int): Double {
    val dr = AndroidColor.red(a) - AndroidColor.red(b)
    val dg = AndroidColor.green(a) - AndroidColor.green(b)
    val db = AndroidColor.blue(a) - AndroidColor.blue(b)
    return kotlin.math.sqrt((dr * dr + dg * dg + db * db).toDouble())
}

internal fun blendChannel(base: Int, target: Int, blend: Double): Int =
    (base * (1.0 - blend) + target * blend)
        .roundToInt()
        .coerceIn(0, 255)

internal fun uncompositeChannel(value: Int, plateValue: Int, alpha: Double): Int =
    (plateValue + (value - plateValue) / alpha).toInt().coerceIn(0, 255)

internal fun luma(pixel: Int): Int =
    (AndroidColor.red(pixel) * 0.299 +
        AndroidColor.green(pixel) * 0.587 +
        AndroidColor.blue(pixel) * 0.114).toInt()

internal fun luma(red: Double, green: Double, blue: Double): Double =
    red * 0.299 + green * 0.587 + blue * 0.114
