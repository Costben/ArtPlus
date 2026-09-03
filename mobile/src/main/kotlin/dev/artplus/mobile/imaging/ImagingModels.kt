package dev.artplus.mobile

/**
 * 图像几何：可见像素包围盒。
 *
 * 从 MainActivity 迁移而来（原 private data class Bounds）。
 * 纯数据结构，不依赖 Activity 状态。
 */
internal data class Bounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    fun width(): Int = right - left
    fun height(): Int = bottom - top
}
