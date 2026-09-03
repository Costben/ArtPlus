package dev.artplus.mobile

import android.graphics.Bitmap

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

/**
 * 管线结果模型（P1.2-b 从 MainActivity 提升而来，原 private 嵌套类）。
 */
internal data class ForegroundCleanupResult(
    val bitmap: Bitmap,
    val changed: Boolean,
    val removedRatio: Double,
    val repairedRatio: Double,
)

internal data class ShadowCleanupResult(
    val bitmap: Bitmap,
    val changed: Boolean,
    val removedRatio: Double,
)

internal data class EdgeAnalysis(
    val coverage: Double,
    val color: Int,
)

internal data class MaskComponent(
    val indices: IntArray,
    val touchesEdge: Boolean,
    val bounds: Bounds,
) {
    val size: Int
        get() = indices.size
}
