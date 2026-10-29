package dev.artplus.enhancer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * 判断某个包的图标是不是 ArtPlus 写进去的（而不是系统自己生成的），并提供素材读取。
 *
 * ArtPlus 用 root 把整组 PNG 直接铺进 `/data/oplus/uxicons/<pkg>/`，其中
 * `monochrome_light.png` / `monochrome_dark.png` 只有它会写：系统生成的 uxicon 目录里
 * 是 recfg / recbg / rec_night / monochrome 再加 span 后缀、以及 day、mat、nsd、peb 这一套，
 * 没有 light/dark 后缀
 * （设备上 2870 个目录里带这两个文件的 89 个，全部 root:root 且带 ArtPlus 的 2x2 素材）。
 *
 * 结果带 2 秒 TTL：ArtPlus 写完图标后桌面会被主题重载拉起来，不需要插件自己盯着文件变化。
 */
object ArtPlusAssets {

    private const val ROOT = "/data/oplus/uxicons"
    private const val TTL_MS = 2_000L

    private class Entry(val present: Boolean, val at: Long)

    private val cache = ConcurrentHashMap<String, Entry>()
    private val bitmapCache = ConcurrentHashMap<String, Bitmap>()

    fun has(pkg: String?): Boolean {
        if (pkg.isNullOrEmpty()) return false
        val now = System.currentTimeMillis()
        cache[pkg]?.let { if (now - it.at < TTL_MS) return it.present }

        val dir = File(ROOT, pkg)
        val present = File(dir, "monochrome_light.png").isFile ||
            File(dir, "monochrome_dark.png").isFile
        cache[pkg] = Entry(present, now)
        return present
    }

    /**
     * 读取 ArtPlus 为该包写入的 1x1 图标位图。
     * 优先读取 day.png（完整合成图）或 monochrome_light.png，作为锁定图标的真源。
     */
    fun loadBitmap(pkg: String): Bitmap? {
        bitmapCache[pkg]?.let { if (!it.isRecycled) return it }
        val dir = File(ROOT, pkg)
        if (!dir.isDirectory) return null

        val candidates = arrayOf(
            File(dir, "day.png"),
            File(dir, "monochrome_light.png"),
            File(dir, "monochrome_dark.png"),
            File(dir, "recbg.png"),
        )
        for (f in candidates) {
            if (f.isFile && f.canRead()) {
                val bmp = runCatching { BitmapFactory.decodeFile(f.absolutePath) }.getOrNull()
                if (bmp != null) {
                    bitmapCache[pkg] = bmp
                    return bmp
                }
            }
        }
        return null
    }

    fun invalidate() {
        cache.clear()
        bitmapCache.clear()
    }
}
