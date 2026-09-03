package dev.artplus.mobile

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

/**
 * P4 管线守卫：RMBG alpha 形态学 helper 必须与原 Activity 内联逻辑一致。
 * 全部 JVM 直跑（纯 IntArray 数学）。
 */
class RmbgRunnerTest {

    @Test
    fun morphExpand_dilatesBrightPixel() {
        val alpha = intArrayOf(0, 0, 0, 0, 255, 0, 0, 0, 0)
        val out = morphRmbgAlpha(alpha, 3, 3, expand = true, radius = 1)
        assertContentEquals(IntArray(9) { 255 }, out)
    }

    @Test
    fun morphErode_shrinksBrightPixel() {
        val alpha = intArrayOf(0, 0, 0, 0, 255, 0, 0, 0, 0)
        val out = morphRmbgAlpha(alpha, 3, 3, expand = false, radius = 1)
        assertContentEquals(IntArray(9) { 0 }, out)
    }

    @Test
    fun morphExpand_solidStays() {
        val alpha = IntArray(16) { 200 }
        assertContentEquals(alpha, morphRmbgAlpha(alpha, 4, 4, expand = true, radius = 1))
    }

    @Test
    fun featherZeroStrength_identity() {
        val alpha = intArrayOf(0, 10, 200, 255)
        assertContentEquals(alpha, featherRmbgAlphaEdges(alpha, 2, 2, strength = 0.0, radius = 1))
    }

    @Test
    fun featherUniform2x2_outOfBoundsCountsAsZero() {
        // 2x2 全 200：角点邻域 4 个 200 + 5 个越界 0，均值 800/9=88.89
        val alpha = IntArray(4) { 200 }
        assertContentEquals(IntArray(4) { 89 }, featherRmbgAlphaEdges(alpha, 2, 2, strength = 1.0, radius = 1))
    }

    @Test
    fun featherEdge_blendsTowardAverage() {
        // 3x3：[0,0,0, 0,255,255, 255,255,255]，strength=1 全取邻域均值
        // (越界按 0 计入均值，与原逻辑一致)
        val alpha = intArrayOf(0, 0, 0, 0, 255, 255, 255, 255, 255)
        val out = featherRmbgAlphaEdges(alpha, 3, 3, strength = 1.0, radius = 1)
        assertEquals(28, out[0]) // 255/9=28.33
        assertEquals(142, out[4]) // 1275/9=141.67
        assertEquals(113, out[8]) // 1020/9=113.33
    }
}
