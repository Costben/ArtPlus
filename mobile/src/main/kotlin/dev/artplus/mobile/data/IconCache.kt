package dev.artplus.mobile

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.LruCache
import kotlin.math.roundToInt
import android.graphics.Color as AndroidColor

/**
 * 图标缓存层（P3 拆分）：内存缓存读写 + 图标加载/预热 + 位图缩放/绘制。
 *
 * 从 MainActivity 迁移而来：原 `private fun`，现 `internal`。
 * - 读 Activity 状态者改为显式参数：`getCachedAppIcon` 收 cache、
 *   `loadAppIconBitmap` 收 entry + pm + size（原直读 packageManager/ICON_CACHE_SIZE）、
 *   `preloadAppIcons` 收 cache + pm + entries + size + count
 *   （原直读 appIconCache/PRELOAD_ICON_COUNT；线程归属不变，仍由调用方线程执行）。
 * - `sampleBitmapShortEdge`/`drawDrawableCover` 纯移动（壁纸系调用方暂留 Activity，P5 再说）。
 * Activity 内保留 arity 变化的同名 wrapper 委托（重构期间保留，P5 后删除），调用点零改动。
 */

internal fun getCachedAppIcon(cache: LruCache<String, Bitmap>, key: String): Bitmap? =
    synchronized(cache) { cache.get(key) }

internal fun loadAppIconBitmap(entry: AppEntry, pm: PackageManager, size: Int): Bitmap =
    drawDrawable(entry.applicationInfo.loadIcon(pm), size, size, transparent = true)
        .also { it.prepareToDraw() }

internal fun preloadAppIcons(
    cache: LruCache<String, Bitmap>,
    pm: PackageManager,
    entries: List<AppEntry>,
    cacheSize: Int,
    count: Int,
) {
    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
    entries.asSequence()
        .filter { it.launchable }
        .take(count)
        .forEach { entry ->
            if (getCachedAppIcon(cache, entry.iconKey) != null) {
                return@forEach
            }
            val bitmap = runCatching { loadAppIconBitmap(entry, pm, cacheSize) }.getOrNull() ?: return@forEach
            synchronized(cache) {
                if (cache.get(entry.iconKey) == null) {
                    cache.put(entry.iconKey, bitmap)
                }
            }
        }
}

/** 等比缩放：短边对齐目标尺寸（已达标则原图返回，共享位图不回收）。 */
internal fun sampleBitmapShortEdge(source: Bitmap, shortEdge: Int): Bitmap {
    val srcW = source.width
    val srcH = source.height
    if (srcW <= 0 || srcH <= 0 || minOf(srcW, srcH) == shortEdge) {
        return source
    }
    val scale = shortEdge.toFloat() / minOf(srcW, srcH).toFloat()
    return Bitmap.createScaledBitmap(
        source,
        (srcW * scale).roundToInt().coerceAtLeast(1),
        (srcH * scale).roundToInt().coerceAtLeast(1),
        true,
    )
}

/** Cover 模式绘制：等比放大铺满目标并居中裁切，不拉伸变形。 */
internal fun drawDrawableCover(drawable: Drawable, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(AndroidColor.BLACK)
    val copy = drawable.constantState?.newDrawable()?.mutate() ?: drawable.mutate()
    val srcW = copy.intrinsicWidth.takeIf { it > 0 } ?: width
    val srcH = copy.intrinsicHeight.takeIf { it > 0 } ?: height
    val scale = maxOf(width.toFloat() / srcW, height.toFloat() / srcH)
    val dstW = (srcW * scale).roundToInt()
    val dstH = (srcH * scale).roundToInt()
    val left = (width - dstW) / 2
    val top = (height - dstH) / 2
    copy.setBounds(left, top, left + dstW, top + dstH)
    copy.draw(canvas)
    return bitmap
}
