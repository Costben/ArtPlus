package dev.artplus.enhancer

import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * 判断某个包的图标是不是 ArtPlus 写进去的（而不是系统自己生成的）。
 *
 * ArtPlus 用 root 把整组 PNG 铺进 `/data/oplus/uxicons/<pkg>/`，并在首次写入时留下
 * `.artplusmodified` 标记。系统生成的 uxicon 目录不会有这个文件，所以判定只认它：
 * `recfg.png` 之类的素材系统目录里也有一整套，拿它当依据会把系统图标误判成 ArtPlus 的。
 *
 * 结果带 2 秒 TTL：ArtPlus 写完图标后桌面会被主题重载拉起来，不需要插件自己盯着文件变化。
 */
object ArtPlusAssets {

    private const val ROOT = "/data/oplus/uxicons"
    private const val MARKER = ".artplusmodified"
    private const val TTL_MS = 2_000L

    private class Entry(val present: Boolean, val at: Long)

    private val cache = ConcurrentHashMap<String, Entry>()

    fun has(pkg: String?): Boolean {
        if (pkg.isNullOrEmpty()) return false
        val now = System.currentTimeMillis()
        cache[pkg]?.let { if (now - it.at < TTL_MS) return it.present }

        val dir = File(ROOT, pkg)
        val present = File(dir, MARKER).isFile
        cache[pkg] = Entry(present, now)
        return present
    }

    fun invalidate() {
        cache.clear()
    }
}
