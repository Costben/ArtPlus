package dev.artplus.mobile

import kotlin.math.roundToInt

/**
 * 管线调参映射：把 1–100 的滑杆百分比映射为算法实际阈值。
 *
 * 从 MainActivity 迁移而来（P1.2 拆分）：原 `private fun effective*()` 直接读
 * Activity 的 `mutableStateOf` 字段，新版本显式收百分比参数，不读任何 Activity 状态。
 * Activity 内保留同名无参 wrapper 委托到这里，原有调用点零改动。
 */

internal fun effectiveBackgroundSeparationDistance(backgroundSeparationPercent: Int): Double =
    lerpDouble(LEGACY_BACKGROUND_SEPARATION_MIN, LEGACY_BACKGROUND_SEPARATION_MAX, ratioPercent(backgroundSeparationPercent))

internal fun effectivePlateRemovalDistance(plateRemovalPercent: Int): Double =
    lerpDouble(LEGACY_PLATE_REMOVAL_MIN, LEGACY_PLATE_REMOVAL_MAX, ratioPercent(plateRemovalPercent))

internal fun effectiveShadowRemovalAlpha(shadowRemovalPercent: Int): Int =
    lerpDouble(LEGACY_SHADOW_REMOVAL_MIN, LEGACY_SHADOW_REMOVAL_MAX, ratioPercent(shadowRemovalPercent))
        .toInt()
        .coerceIn(0, 255)

internal fun effectiveShadowMaxSaturation(shadowRemovalPercent: Int): Double =
    lerpDouble(SHADOW_MAX_SATURATION_MIN, SHADOW_MAX_SATURATION_MAX, ratioPercent(shadowRemovalPercent))

internal fun effectiveShadowMaxLuminance(shadowRemovalPercent: Int): Int =
    lerpDouble(SHADOW_MAX_LUMINANCE_MIN, SHADOW_MAX_LUMINANCE_MAX, ratioPercent(shadowRemovalPercent))
        .roundToInt()
        .coerceIn(0, 255)

internal fun effectiveShadowMinVisibleRatio(shadowRemovalPercent: Int): Double =
    lerpDouble(SHADOW_MIN_VISIBLE_RATIO_MAX, SHADOW_MIN_VISIBLE_RATIO_MIN, ratioPercent(shadowRemovalPercent))

internal fun effectiveShadowMinOffset(shadowRemovalPercent: Int): Double =
    lerpDouble(SHADOW_MIN_OFFSET_MAX, SHADOW_MIN_OFFSET_MIN, ratioPercent(shadowRemovalPercent))

internal fun effectiveShadowMinDownOffset(shadowRemovalPercent: Int): Double =
    lerpDouble(SHADOW_MIN_DOWN_OFFSET_MAX, SHADOW_MIN_DOWN_OFFSET_MIN, ratioPercent(shadowRemovalPercent))

internal fun effectiveShadowMinLumaDrop(shadowRemovalPercent: Int): Int =
    lerpDouble(SHADOW_MIN_LUMA_DROP_MAX, SHADOW_MIN_LUMA_DROP_MIN, ratioPercent(shadowRemovalPercent))
        .roundToInt()
        .coerceAtLeast(0)

internal fun residueDistanceThreshold(plateRemovalPercent: Int): Double =
    (effectivePlateRemovalDistance(plateRemovalPercent) * RESIDUE_DISTANCE_SCALE)
        .coerceIn(RESIDUE_MIN_DISTANCE, RESIDUE_MAX_DISTANCE)

internal fun edgeConnectedResidueDistanceThreshold(plateRemovalPercent: Int): Double =
    (effectivePlateRemovalDistance(plateRemovalPercent) * RESIDUE_CONNECTED_DISTANCE_SCALE)
        .coerceIn(RESIDUE_CONNECTED_MIN_DISTANCE, RESIDUE_CONNECTED_MAX_DISTANCE)

internal fun foregroundEdgePolishStrength(edgePolishPercent: Int): Double =
    EDGE_POLISH_FOREGROUND_MIN_STRENGTH +
        ratioPercent(edgePolishPercent) * (EDGE_POLISH_FOREGROUND_MAX_STRENGTH - EDGE_POLISH_FOREGROUND_MIN_STRENGTH)

internal fun monochromeEdgePolishStrength(edgePolishPercent: Int): Double =
    EDGE_POLISH_MONO_MIN_STRENGTH +
        ratioPercent(edgePolishPercent) * (EDGE_POLISH_MONO_MAX_STRENGTH - EDGE_POLISH_MONO_MIN_STRENGTH)
