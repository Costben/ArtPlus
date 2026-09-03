package dev.artplus.mobile

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * P1.2 拆分守卫：显式传参版本必须与原 Activity 内联公式逐值一致。
 * 全部纯数学，可 JVM 直跑。
 */
class PipelineTuningTest {

    private fun assertNear(expected: Double, actual: Double, tolerance: Double = 1e-9) {
        assertTrue(
            abs(expected - actual) <= tolerance,
            "expected=$expected actual=$actual",
        )
    }

    @Test
    fun backgroundSeparation_mapsPercentToLegacyRange() {
        assertNear(12.0 + 0.01 * (420.0 - 12.0), effectiveBackgroundSeparationDistance(1))
        assertNear(420.0, effectiveBackgroundSeparationDistance(100))
        assertNear(12.0 + 0.60 * (420.0 - 12.0), effectiveBackgroundSeparationDistance(60))
    }

    @Test
    fun plateRemoval_mapsPercentToLegacyRange() {
        assertNear(4.2, effectivePlateRemovalDistance(1))
        assertNear(420.0, effectivePlateRemovalDistance(100))
    }

    @Test
    fun shadowRemoval_alphaAndThresholds() {
        assertEquals(2, effectiveShadowRemovalAlpha(1))
        assertEquals(255, effectiveShadowRemovalAlpha(100))
        assertNear(0.08 + 0.01 * (0.42 - 0.08), effectiveShadowMaxSaturation(1))
        assertNear(0.42, effectiveShadowMaxSaturation(100))
        // 反向映射：百分比越大，可见比例/偏移下限越小
        assertTrue(effectiveShadowMinVisibleRatio(100) < effectiveShadowMinVisibleRatio(1))
        assertNear(0.012, effectiveShadowMinVisibleRatio(100))
        assertTrue(effectiveShadowMinOffset(100) < effectiveShadowMinOffset(1))
        assertTrue(effectiveShadowMinLumaDrop(100) < effectiveShadowMinLumaDrop(1))
    }

    @Test
    fun residueThresholds_clampToDocumentedBounds() {
        // 极小百分比钳制到下界
        assertNear(64.0, residueDistanceThreshold(1))
        assertNear(96.0, edgeConnectedResidueDistanceThreshold(1))
        // 极大百分比钳制到上界
        assertNear(190.0, residueDistanceThreshold(100))
        assertNear(260.0, edgeConnectedResidueDistanceThreshold(100))
        // 中段未钳制：11% -> 46.2 * 1.45 = 66.99
        assertNear(46.2 * 1.45, residueDistanceThreshold(11))
    }

    @Test
    fun edgePolishStrength_mapsPercentToStrengthRange() {
        assertNear(0.12 + 0.01 * 0.70, foregroundEdgePolishStrength(1))
        assertNear(0.82, foregroundEdgePolishStrength(100))
        assertNear(0.16 + 0.01 * 0.76, monochromeEdgePolishStrength(1))
        assertNear(0.92, monochromeEdgePolishStrength(100))
    }
}
