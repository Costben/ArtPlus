package dev.artplus.mobile

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import java.io.File
import java.io.FileOutputStream
import android.graphics.Color as AndroidColor

/**
 * 基础位图操作：创建 / 缩放 / 居中 / 取色 / 调色 / 落盘。
 *
 * 从 MainActivity 迁移而来（原 private fun，签名未变）。
 * 全部为纯函数（IO 函数除外），不读 Activity 状态。
 */

internal fun solidBitmap(width: Int, height: Int, color: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    Canvas(bitmap).drawColor(color)
    return bitmap
}

internal fun resizeBitmap(source: Bitmap, width: Int, height: Int): Bitmap =
    Bitmap.createScaledBitmap(source, width, height, true)

internal fun centerOnCanvas(source: Bitmap, width: Int, height: Int): Bitmap {
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(out)
    canvas.drawColor(AndroidColor.TRANSPARENT)
    val x = (width - source.width) / 2f
    val y = (height - source.height) / 2f
    canvas.drawBitmap(source, x, y, null)
    return out
}

internal fun sampleColor(bitmap: Bitmap): Int {
    val center = bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
    if (
        AndroidColor.alpha(center) > 32 &&
        AndroidColor.red(center) + AndroidColor.green(center) + AndroidColor.blue(center) >= 120
    ) {
        return AndroidColor.rgb(
            AndroidColor.red(center),
            AndroidColor.green(center),
            AndroidColor.blue(center),
        )
    }

    var red = 0L
    var green = 0L
    var blue = 0L
    var count = 0L
    for (y in 0 until bitmap.height step 8) {
        for (x in 0 until bitmap.width step 8) {
            val pixel = bitmap.getPixel(x, y)
            if (AndroidColor.alpha(pixel) >= 128) {
                red += AndroidColor.red(pixel)
                green += AndroidColor.green(pixel)
                blue += AndroidColor.blue(pixel)
                count++
            }
        }
    }
    if (count == 0L) {
        return AndroidColor.rgb(216, 224, 253)
    }
    return AndroidColor.rgb((red / count).toInt(), (green / count).toInt(), (blue / count).toInt())
}

internal fun adjustColor(source: Bitmap, saturation: Float, brightness: Float): Bitmap {
    val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(out)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    val saturationMatrix = ColorMatrix()
    saturationMatrix.setSaturation(saturation)
    val brightnessMatrix = ColorMatrix(
        floatArrayOf(
            brightness, 0f, 0f, 0f, 0f,
            0f, brightness, 0f, 0f, 0f,
            0f, 0f, brightness, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        ),
    )
    saturationMatrix.postConcat(brightnessMatrix)
    paint.colorFilter = ColorMatrixColorFilter(saturationMatrix)
    canvas.drawBitmap(source, 0f, 0f, paint)
    return out
}

internal fun savePng(bitmap: Bitmap, file: File) {
    val parent = file.parentFile
    if (parent != null && !parent.exists() && !parent.mkdirs()) {
        error("无法创建目录: $parent")
    }
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
}

internal fun ensureCleanDir(dir: File) {
    if (!dir.exists()) {
        dir.mkdirs()
        return
    }
    dir.listFiles()
        ?.filter { it.isFile && it.name.endsWith(".png") }
        ?.forEach { it.delete() }
}

internal fun ensureFreshDir(dir: File) {
    if (dir.exists()) {
        dir.deleteRecursively()
    }
    if (!dir.mkdirs()) {
        error("无法创建目录: $dir")
    }
}
