package dev.artplus.mobile

import kotlin.math.roundToInt

/**
 * RMBG 推理 Runner（P4 拆分）：无状态 alpha 形态学 helper。
 *
 * 从 MainActivity 迁移而来：原 `private fun`，现 `internal`，纯移动。
 * `DynamicRmbgRuntime` 内部类持 Context/ONNX 会话不动（P5 再议）；
 * `tuneRmbgAlpha` 读 4 个调参状态不动（P5 与 186 vars 一起收敛），仍调本文件函数。
 * RMBG_EDGE_FEATHER_MIN_ALPHA_DELTA 已提升进 TuningParams.kt。
 */

internal fun morphRmbgAlpha(
    alpha: IntArray,
    width: Int,
    height: Int,
    expand: Boolean,
    radius: Int,
): IntArray {
    val out = IntArray(alpha.size)
    val safeRadius = radius.coerceAtLeast(1)
    for (y in 0 until height) {
        for (x in 0 until width) {
            var selected = if (expand) 0 else 255
            for (dy in -safeRadius..safeRadius) {
                for (dx in -safeRadius..safeRadius) {
                    val nx = x + dx
                    val ny = y + dy
                    val value = if (nx in 0 until width && ny in 0 until height) {
                        alpha[ny * width + nx].coerceIn(0, 255)
                    } else {
                        0
                    }
                    selected = if (expand) {
                        maxOf(selected, value)
                    } else {
                        minOf(selected, value)
                    }
                }
            }
            out[y * width + x] = selected
        }
    }
    return out
}

internal fun featherRmbgAlphaEdges(
    alpha: IntArray,
    width: Int,
    height: Int,
    strength: Double,
    radius: Int,
): IntArray {
    val out = alpha.copyOf()
    val safeRadius = radius.coerceAtLeast(1)
    val blend = strength.coerceIn(0.0, 1.0)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val index = y * width + x
            var sum = 0
            var count = 0
            var minAlpha = 255
            var maxAlpha = 0
            for (dy in -safeRadius..safeRadius) {
                for (dx in -safeRadius..safeRadius) {
                    val nx = x + dx
                    val ny = y + dy
                    val value = if (nx in 0 until width && ny in 0 until height) {
                        alpha[ny * width + nx].coerceIn(0, 255)
                    } else {
                        0
                    }
                    sum += value
                    count++
                    minAlpha = minOf(minAlpha, value)
                    maxAlpha = maxOf(maxAlpha, value)
                }
            }
            if (count <= 0 || maxAlpha - minAlpha < RMBG_EDGE_FEATHER_MIN_ALPHA_DELTA) {
                continue
            }
            val average = sum.toDouble() / count.toDouble()
            out[index] = (alpha[index] * (1.0 - blend) + average * blend)
                .roundToInt()
                .coerceIn(0, 255)
        }
    }
    return out
}
