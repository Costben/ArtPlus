package dev.artplus.mobile.imaging

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable

/**
 * Drawable 渲染胶水：Drawable→Bitmap 绘制 + 自适应图标 mask 裁剪。
 *
 * 从 MainActivity 迁移而来（P1.2-b b3-slice2，决策选 b：纯移动，签名不变）。
 * 与算法文件隔离；8+5 处暂留调用点（应用列表/GPT/debug/G1）同包零改动。
 * P3 可整体再搬 bridge + glue，无调用点成本。
 */

/**
 * 自适应图标的安全区比例：108dp 画布里只有中间 72dp 会出现在遮罩内，
 * 每边留 18dp 给视差与形变效果。
 */
private const val ADAPTIVE_ICON_SAFE_ZONE_SCALE = 108f / 72f

/**
 * 渲染自适应图标的单个图层（背景或前景）。
 *
 * `AdaptiveIconDrawable` 的两层都画在 108dp 画布上，只有中间 72dp 会出现在遮罩内。
 * 直接按目标尺寸 `setBounds` 只会把内容画成 2/3，位置对但整块缩了一档 ——
 * 前景和底图各自缩一点，叠起来就跟 `AdaptiveIconDrawable` 自己合成的结果对不上
 * （Shizuku 那种底板上带六边形的图标最明显）。
 * 按 108/72 放大后再裁回中央，内容大小才和系统合成一致。
 */
internal fun drawAdaptiveLayer(drawable: Drawable?, size: Int, transparent: Boolean = true): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(if (transparent) AndroidColor.TRANSPARENT else AndroidColor.WHITE)
    if (drawable == null) return bitmap
    val oversize = (size * ADAPTIVE_ICON_SAFE_ZONE_SCALE).toInt()
    val copy = drawable.constantState?.newDrawable()?.mutate() ?: drawable.mutate()
    canvas.translate((size - oversize) / 2f, (size - oversize) / 2f)
    copy.setBounds(0, 0, oversize, oversize)
    copy.draw(canvas)
    return bitmap
}

internal fun drawDrawable(
    drawable: Drawable?,
    width: Int,
    height: Int,
    transparent: Boolean,
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(if (transparent) AndroidColor.TRANSPARENT else AndroidColor.WHITE)
    if (drawable != null) {
        val copy = drawable.constantState?.newDrawable()?.mutate() ?: drawable.mutate()
        copy.setBounds(0, 0, width, height)
        copy.draw(canvas)
        if (transparent && copy is AdaptiveIconDrawable) {
            return clearOutsideAdaptiveIconMask(bitmap, copy)
        }
    }
    return bitmap
}

internal fun clearOutsideAdaptiveIconMask(source: Bitmap, icon: AdaptiveIconDrawable): Bitmap {
    val width = source.width
    val height = source.height
    val mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val maskCanvas = Canvas(mask)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = AndroidColor.WHITE
    maskCanvas.drawPath(icon.iconMask, paint)

    val sourcePixels = IntArray(width * height)
    val maskPixels = IntArray(width * height)
    source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
    mask.getPixels(maskPixels, 0, width, 0, 0, width, height)
    for (i in sourcePixels.indices) {
        val maskAlpha = AndroidColor.alpha(maskPixels[i])
        if (maskAlpha <= 0) {
            sourcePixels[i] = AndroidColor.TRANSPARENT
            continue
        }
        if (maskAlpha < 255) {
            val pixel = sourcePixels[i]
            val alpha = (AndroidColor.alpha(pixel) * maskAlpha / 255.0)
                .toInt()
                .coerceIn(0, 255)
            sourcePixels[i] = AndroidColor.argb(
                alpha,
                AndroidColor.red(pixel),
                AndroidColor.green(pixel),
                AndroidColor.blue(pixel),
            )
        }
    }
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(sourcePixels, 0, width, 0, 0, width, height)
    return out
}

internal fun drawLocalCandidateSourceIcon(icon: Drawable, width: Int, height: Int): Bitmap =
    drawDrawable(
        drawable = icon,
        width = width,
        height = height,
        transparent = icon is AdaptiveIconDrawable,
    )
