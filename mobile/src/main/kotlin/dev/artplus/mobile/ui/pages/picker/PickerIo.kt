package dev.artplus.mobile

import android.app.WallpaperManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Slice 2.5：应用选择器图标/壁纸 IO（原 MainActivity 残留本体原样搬迁）。
 * 只做物理搬迁+显式参数化：Activity 状态（cache/路径/文案）经参数/回调注入；
 * 线程调度不变（import 仍经调用方线程发射，load 系 suspend IO）。
 * MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动；
 * 纯函数 centerCropToSixteenNine 直接搬迁、不留 wrapper（避免同名同参）。
 * 名单内联 helper 扫描结论：本簇无内联 helper（sample/draw 收敛进 data/IconCache，只做调用）。
 */

internal suspend fun pickerLoadCachedAppIcon(
    entry: AppEntry,
    getCached: (String) -> Bitmap?,
    putCached: (String, Bitmap) -> Unit,
    loadBitmap: (AppEntry) -> Bitmap?,
): Bitmap? =
    withContext(Dispatchers.IO) {
        val cached = getCached(entry.iconKey)
        if (cached != null) {
            return@withContext cached
        }
        val bitmap = loadBitmap(entry) ?: return@withContext null
        putCached(entry.iconKey, bitmap)
        bitmap
    }

/**
 * 读取当前设备桌面壁纸并保留原始宽高比（短边缩放到 480 左右）。
 * 静态壁纸经 ImageWallpaper 暴露为 BitmapDrawable，直接取位图，无需任何权限；
 * 失败返回 null，调用方走内置图兜底。
 */
internal fun pickerLoadPreviewWallpaperBitmap(
    getCached: () -> Bitmap?,
    setCached: (Bitmap) -> Unit,
    loadDrawable: () -> Drawable?,
    shortEdge: Int,
): Bitmap? {
    val cached = getCached()
    if (cached != null && !cached.isRecycled) {
        return cached
    }
    val loaded = runCatching {
        val drawable = loadDrawable() ?: return null
        val sampled = if (drawable is BitmapDrawable) {
            drawable.bitmap?.let { sampleBitmapShortEdge(it, shortEdge) }
        } else {
            val intrinsicW = drawable.intrinsicWidth.takeIf { it > 0 }
            val intrinsicH = drawable.intrinsicHeight.takeIf { it > 0 }
            if (intrinsicW != null && intrinsicH != null) {
                val scale = shortEdge.toFloat() /
                    minOf(intrinsicW, intrinsicH).toFloat()
                drawDrawableCover(
                    drawable = drawable,
                    width = (intrinsicW * scale).roundToInt().coerceAtLeast(1),
                    height = (intrinsicH * scale).roundToInt().coerceAtLeast(1),
                )
            } else {
                // 无内在尺寸（如纯色壁纸）：按常见竖屏比例渲染
                drawDrawableCover(drawable, shortEdge, 854)
            }
        }
        sampled?.also { it.prepareToDraw() }
    }.getOrNull()
    if (loaded != null) {
        setCached(loaded)
    }
    return loaded
}

internal fun pickerLoadBundledPreviewWallpaperBitmap(
    getCached: () -> Bitmap?,
    setCached: (Bitmap) -> Unit,
    resources: Resources,
    resId: Int,
    shortEdge: Int,
): Bitmap? {
    val cached = getCached()
    if (cached != null && !cached.isRecycled) {
        return cached
    }
    val loaded = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(resources, resId, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null
        }
        val shortEdgeOfSrc = minOf(bounds.outWidth, bounds.outHeight)
        var sampleSize = 1
        while (shortEdgeOfSrc / (sampleSize * 2) >= shortEdge) {
            sampleSize *= 2
        }
        val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        BitmapFactory.decodeResource(resources, resId, opts)
            ?.also { it.prepareToDraw() }
    }.getOrNull()
    if (loaded != null) {
        setCached(loaded)
    }
    return loaded
}

/**
 * 导入用户上传的壁纸：只居中裁剪为 16:9，不做任何缩放压缩，避免变形；
 * 以 PNG 无损存档到私有目录，「桌面」背景优先使用。
 */
internal fun pickerImportCustomWallpaper(
    uri: Uri,
    isBusy: Boolean,
    onStatusText: (String) -> Unit,
    onLaunch: (String, () -> Unit) -> Unit,
    openInputBytes: (Uri) -> ByteArray?,
    filesDir: File,
    fileName: String,
    onSuccess: (path: String, info: String) -> Unit,
    onError: (String) -> Unit,
) {
    if (isBusy) {
        return
    }
    onStatusText("正在导入壁纸…")
    onLaunch("ArtPlusWallpaperImport") {
        try {
            val bytes = openInputBytes(uri)
                ?: error("无法打开图片")
            val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: error("图片无法解码；请选择 JPG/PNG/WEBP")
            val cropped = centerCropToSixteenNine(decoded)
            if (cropped !== decoded && !decoded.isRecycled) {
                decoded.recycle()
            }
            val outFile = File(filesDir, fileName)
            FileOutputStream(outFile).use { fos ->
                if (!cropped.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
                    error("壁纸保存失败")
                }
            }
            val info = "${cropped.width} × ${cropped.height}"
            onSuccess(outFile.absolutePath, info)
        } catch (error: Exception) {
            onError("壁纸导入失败: ${error.message ?: error.javaClass.simpleName}")
        }
    }
}

internal fun pickerClearCustomWallpaper(
    filesDir: File,
    customPath: String?,
    fileName: String,
    onCleared: (status: String) -> Unit,
    onSave: () -> Unit,
    clearCache: () -> Unit,
    setPath: (String?) -> Unit,
    setInfo: (String) -> Unit,
) {
    runCatching {
        customPath?.let { File(it).delete() }
        File(filesDir, fileName).delete()
    }
    clearCache()
    setPath(null)
    setInfo("")
    onCleared("已清除自定义壁纸，「桌面」背景恢复系统壁纸/内置壁纸")
    onSave()
}

/** 居中裁剪为 16:9（竖屏），只裁剪不缩放；已符合比例则原图返回。 */
internal fun centerCropToSixteenNine(source: Bitmap): Bitmap {
    val w = source.width
    val h = source.height
    if (w <= 0 || h <= 0) {
        return source
    }
    val targetRatio = 9f / 16f
    val currentRatio = w.toFloat() / h.toFloat()
    if (abs(currentRatio - targetRatio) / targetRatio < 0.005f) {
        return source
    }
    return if (currentRatio > targetRatio) {
        val cropW = (h * targetRatio).roundToInt().coerceIn(1, w)
        Bitmap.createBitmap(source, (w - cropW) / 2, 0, cropW, h)
    } else {
        val cropH = (w / targetRatio).roundToInt().coerceIn(1, h)
        Bitmap.createBitmap(source, 0, (h - cropH) / 2, w, cropH)
    }
}

internal fun pickerLoadCustomWallpaperBitmap(
    path: String?,
    cachedPath: String?,
    getCached: () -> Bitmap?,
    setCached: (path: String, bitmap: Bitmap) -> Unit,
    shortEdge: Int,
): Bitmap? {
    val current = path ?: return null
    val cached = getCached()
    if (cachedPath == current && cached != null && !cached.isRecycled) {
        return cached
    }
    val loaded = runCatching {
        val file = File(current)
        if (!file.isFile) {
            return null
        }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null
        sampleBitmapShortEdge(bitmap, shortEdge)
            .also { it.prepareToDraw() }
    }.getOrNull()
    if (loaded != null) {
        setCached(current, loaded)
    }
    return loaded
}

/** WallpaperManager 便捷取 drawable（供 wrapper 注入 loadDrawable 用）。 */
internal fun pickerSystemWallpaperDrawable(manager: WallpaperManager): Drawable? =
    runCatching { manager.drawable }.getOrNull()
