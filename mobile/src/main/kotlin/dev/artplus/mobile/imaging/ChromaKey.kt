package dev.artplus.mobile

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor

/**
 * 绿幕抠像：按键色选择 + 背景剔除。
 *
 * 从 MainActivity 迁移而来（原 private fun，签名未变）。
 * 纯函数，不读 Activity 状态。
 */

internal fun removeChromaKeyBackground(source: Bitmap, keyColor: Int): Bitmap {
    val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    for (y in 0 until source.height) {
        for (x in 0 until source.width) {
            val pixel = source.getPixel(x, y)
            val distance = colorDistance(pixel, keyColor)
            val originalAlpha = AndroidColor.alpha(pixel)
            val alpha = when {
                distance <= CHROMA_TRANSPARENT_THRESHOLD -> 0
                distance >= CHROMA_OPAQUE_THRESHOLD -> originalAlpha
                else -> {
                    val factor = (distance - CHROMA_TRANSPARENT_THRESHOLD) /
                        (CHROMA_OPAQUE_THRESHOLD - CHROMA_TRANSPARENT_THRESHOLD)
                    (factor.coerceIn(0.0, 1.0) * originalAlpha).toInt()
                }
            }
            out.setPixel(x, y, (alpha shl 24) or (pixel and 0x00ffffff))
        }
    }
    return out
}

internal fun chooseChromaKey(source: Bitmap): Int {
    var best = CHROMA_KEY_CANDIDATES.first()
    var bestScore = -1.0
    for (candidate in CHROMA_KEY_CANDIDATES) {
        var minDistance = Double.MAX_VALUE
        for (y in 0 until source.height step maxOf(1, source.height / 64)) {
            for (x in 0 until source.width step maxOf(1, source.width / 64)) {
                val pixel = source.getPixel(x, y)
                if (AndroidColor.alpha(pixel) >= 64) {
                    minDistance = minOf(minDistance, colorDistance(candidate, pixel))
                }
            }
        }
        if (minDistance > bestScore) {
            best = candidate
            bestScore = minDistance
        }
    }
    return best
}
