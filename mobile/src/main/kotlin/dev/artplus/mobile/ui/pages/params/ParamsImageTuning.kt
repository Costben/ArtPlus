package dev.artplus.mobile

import android.content.SharedPreferences
import kotlin.math.roundToInt

/**
 * Slice 2.4：图像调参存取族（原 MainActivity 本体原样搬迁）。
 * 只做物理搬迁+显式参数化：prefs/快照经参数注入，draft 文本/刷新经回调注入；
 * 迁移分支/钳制范围/存储顺序一律不变。
 * MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动。
 */

internal fun paramsMigrateLegacyPercent(value: Int, fallback: Int): Int =
    when {
        value in 1..100 -> value
        value <= 0 -> 1
        else -> fallback
    }

internal fun paramsLoadImageSettings(
    prefs: SharedPreferences,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    getParams: () -> TuningParams,
    setDraftForegroundSubjectPercentText: (String) -> Unit,
    setDraftForegroundShadowLevelText: (String) -> Unit,
    setDraftMonochromeThemeScaleText: (String) -> Unit,
    setDraftBackgroundSeparationText: (String) -> Unit,
    setDraftPlateRemovalText: (String) -> Unit,
    setDraftShadowRemovalText: (String) -> Unit,
    setDraftEdgePolishText: (String) -> Unit,
    setDraftRmbgAlphaStrengthText: (String) -> Unit,
    setDraftRmbgEdgeFeatherText: (String) -> Unit,
    setDraftRmbgEdgeAdjustText: (String) -> Unit,
    setDraftRmbgWeakAlphaKeepText: (String) -> Unit,
) {
    val storedValue = prefs.getInt(
        PREF_FOREGROUND_SUBJECT_PERCENT,
        DEFAULT_FOREGROUND_SUBJECT_PERCENT,
    )
    updateLive { p -> p.copy(foregroundSubjectPercent = if (storedValue == LEGACY_FOREGROUND_SUBJECT_PERCENT) {
        DEFAULT_FOREGROUND_SUBJECT_PERCENT
    } else {
        storedValue.coerceIn(MIN_FOREGROUND_SUBJECT_PERCENT, MAX_FOREGROUND_SUBJECT_PERCENT)
    }) }
    setDraftForegroundSubjectPercentText(getParams().foregroundSubjectPercent.toString())
    updateLive { p -> p.copy(foregroundShadowLevel = prefs.getInt(
        PREF_FOREGROUND_SHADOW_LEVEL,
        DEFAULT_FOREGROUND_SHADOW_LEVEL,
    ).coerceIn(MIN_FOREGROUND_SHADOW_LEVEL, MAX_FOREGROUND_SHADOW_LEVEL)) }
    setDraftForegroundShadowLevelText(getParams().foregroundShadowLevel.toString())
    updateLive { p -> p.copy(monochromeThemeScale = prefs.getFloat(
        PREF_MONOCHROME_THEME_SCALE,
        DEFAULT_MONOCHROME_THEME_SCALE,
    ).coerceIn(MIN_MONOCHROME_THEME_SCALE, MAX_MONOCHROME_THEME_SCALE)) }
    setDraftMonochromeThemeScaleText((getParams().monochromeThemeScale * 100).roundToInt().toString())
    val tuningVersion = prefs.getInt(PREF_IMAGE_TUNING_VERSION, 1)
    updateLive { p -> p.copy(backgroundSeparationPercent = if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
        DEFAULT_BACKGROUND_SEPARATION_PERCENT
    } else {
        prefs.getInt(PREF_BACKGROUND_SEPARATION_PERCENT, DEFAULT_BACKGROUND_SEPARATION_PERCENT)
            .let { paramsMigrateLegacyPercent(it, DEFAULT_BACKGROUND_SEPARATION_PERCENT) }
    }.coerceIn(MIN_BACKGROUND_SEPARATION_PERCENT, MAX_BACKGROUND_SEPARATION_PERCENT)) }
    setDraftBackgroundSeparationText(getParams().backgroundSeparationPercent.toString())
    updateLive { p -> p.copy(plateRemovalPercent = if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
        DEFAULT_PLATE_REMOVAL_PERCENT
    } else {
        prefs.getInt(PREF_PLATE_REMOVAL_PERCENT, DEFAULT_PLATE_REMOVAL_PERCENT)
            .let { paramsMigrateLegacyPercent(it, DEFAULT_PLATE_REMOVAL_PERCENT) }
    }.coerceIn(MIN_PLATE_REMOVAL_PERCENT, MAX_PLATE_REMOVAL_PERCENT)) }
    setDraftPlateRemovalText(getParams().plateRemovalPercent.toString())
    updateLive { p -> p.copy(shadowRemovalPercent = if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
        DEFAULT_SHADOW_REMOVAL_PERCENT
    } else {
        prefs.getInt(PREF_SHADOW_REMOVAL_PERCENT, DEFAULT_SHADOW_REMOVAL_PERCENT)
            .let { paramsMigrateLegacyPercent(it, DEFAULT_SHADOW_REMOVAL_PERCENT) }
    }.coerceIn(MIN_SHADOW_REMOVAL_PERCENT, MAX_SHADOW_REMOVAL_PERCENT)) }
    setDraftShadowRemovalText(getParams().shadowRemovalPercent.toString())
    updateLive { p -> p.copy(edgePolishPercent = if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
        DEFAULT_EDGE_POLISH_PERCENT
    } else {
        prefs.getInt(PREF_EDGE_POLISH_PERCENT, DEFAULT_EDGE_POLISH_PERCENT)
    }.coerceIn(MIN_EDGE_POLISH_PERCENT, MAX_EDGE_POLISH_PERCENT)) }
    setDraftEdgePolishText(getParams().edgePolishPercent.toString())
    updateLive { p -> p.copy(rmbgAlphaStrengthPercent = prefs.getInt(
        PREF_RMBG_ALPHA_STRENGTH_PERCENT,
        DEFAULT_RMBG_ALPHA_STRENGTH_PERCENT,
    ).coerceIn(MIN_RMBG_ALPHA_STRENGTH_PERCENT, MAX_RMBG_ALPHA_STRENGTH_PERCENT)) }
    setDraftRmbgAlphaStrengthText(getParams().rmbgAlphaStrengthPercent.toString())
    updateLive { p -> p.copy(rmbgEdgeFeatherPercent = prefs.getInt(
        PREF_RMBG_EDGE_FEATHER_PERCENT,
        DEFAULT_RMBG_EDGE_FEATHER_PERCENT,
    ).coerceIn(MIN_RMBG_EDGE_FEATHER_PERCENT, MAX_RMBG_EDGE_FEATHER_PERCENT)) }
    setDraftRmbgEdgeFeatherText(getParams().rmbgEdgeFeatherPercent.toString())
    updateLive { p -> p.copy(rmbgEdgeAdjustPercent = prefs.getInt(
        PREF_RMBG_EDGE_ADJUST_PERCENT,
        DEFAULT_RMBG_EDGE_ADJUST_PERCENT,
    ).coerceIn(MIN_RMBG_EDGE_ADJUST_PERCENT, MAX_RMBG_EDGE_ADJUST_PERCENT)) }
    setDraftRmbgEdgeAdjustText(getParams().rmbgEdgeAdjustPercent.toString())
    updateLive { p -> p.copy(rmbgWeakAlphaKeepPercent = prefs.getInt(
        PREF_RMBG_WEAK_ALPHA_KEEP_PERCENT,
        DEFAULT_RMBG_WEAK_ALPHA_KEEP_PERCENT,
    ).coerceIn(MIN_RMBG_WEAK_ALPHA_KEEP_PERCENT, MAX_RMBG_WEAK_ALPHA_KEEP_PERCENT)) }
    setDraftRmbgWeakAlphaKeepText(getParams().rmbgWeakAlphaKeepPercent.toString())
    updateLive { p -> p.copy(adaptiveForegroundMode = (if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
        AdaptiveForegroundMode.Auto
    } else {
        AdaptiveForegroundMode.fromValue(
            prefs.getString(PREF_ADAPTIVE_FOREGROUND_MODE, AdaptiveForegroundMode.Auto.value),
        )
    }).value) }
    updateLive { p -> p.copy(adaptiveDirectMaxCoveragePercent = prefs.getInt(
        PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT,
        DEFAULT_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT,
    ).coerceIn(MIN_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT, MAX_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT)) }
    updateLive { p -> p.copy(adaptiveDirectMaxCoverageIncreasePercent = prefs.getInt(
        PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
        DEFAULT_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
    ).coerceIn(
        MIN_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
        MAX_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
    )) }
    updateLive { p -> p.copy(adaptiveMaskEdgeCoveragePercent = prefs.getInt(
        PREF_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT,
        DEFAULT_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT,
    ).coerceIn(MIN_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT, MAX_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT)) }
    updateLive { p -> p.copy(adaptiveMaskMinCoveragePercent = prefs.getInt(
        PREF_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT,
        DEFAULT_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT,
    ).coerceIn(MIN_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT, MAX_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT)) }
    updateLive { p -> p.copy(adaptiveCenterEpsilonPercent = prefs.getInt(
        PREF_ADAPTIVE_CENTER_EPSILON_PERCENT,
        DEFAULT_ADAPTIVE_CENTER_EPSILON_PERCENT,
    ).coerceIn(MIN_ADAPTIVE_CENTER_EPSILON_PERCENT, MAX_ADAPTIVE_CENTER_EPSILON_PERCENT)) }
    updateLive { p -> p.copy(originalForegroundCleanupMode = (if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
        OriginalForegroundCleanupMode.Auto
    } else {
        OriginalForegroundCleanupMode.fromValue(
            prefs.getString(PREF_ORIGINAL_FOREGROUND_CLEANUP_MODE, OriginalForegroundCleanupMode.Auto.value),
        )
    }).value) }
    updateLive { p -> p.copy(localBackgroundSeparationEnabled = prefs.getBoolean(PREF_LOCAL_BACKGROUND_SEPARATION_ENABLED, true)) }
    updateLive { p -> p.copy(localAdaptiveSelectionEnabled = prefs.getBoolean(PREF_LOCAL_ADAPTIVE_SELECTION_ENABLED, true)) }
    updateLive { p -> p.copy(localCornerMaskCleanupEnabled = prefs.getBoolean(PREF_LOCAL_CORNER_MASK_CLEANUP_ENABLED, true)) }
    updateLive { p -> p.copy(localAlphaEdgeColorRepairEnabled = prefs.getBoolean(PREF_LOCAL_ALPHA_EDGE_COLOR_REPAIR_ENABLED, true)) }
    updateLive { p -> p.copy(localPlainBackgroundEstimationEnabled = prefs.getBoolean(PREF_LOCAL_PLAIN_BACKGROUND_ESTIMATION_ENABLED, true)) }
    updateLive { p -> p.copy(localOriginalCleanupEnabled = prefs.getBoolean(PREF_LOCAL_ORIGINAL_CLEANUP_ENABLED, true)) }
    updateLive { p -> p.copy(localPlateCleanupEnabled = prefs.getBoolean(PREF_LOCAL_PLATE_CLEANUP_ENABLED, true)) }
    updateLive { p -> p.copy(localPlateEdgeRepairEnabled = prefs.getBoolean(PREF_LOCAL_PLATE_EDGE_REPAIR_ENABLED, true)) }
    updateLive { p -> p.copy(localPlateResidueCleanupEnabled = prefs.getBoolean(PREF_LOCAL_PLATE_RESIDUE_CLEANUP_ENABLED, true)) }
    updateLive { p -> p.copy(localShadowCleanupEnabled = prefs.getBoolean(PREF_LOCAL_SHADOW_CLEANUP_ENABLED, true)) }
    updateLive { p -> p.copy(localShadowEdgeRepairEnabled = prefs.getBoolean(PREF_LOCAL_SHADOW_EDGE_REPAIR_ENABLED, true)) }
    updateLive { p -> p.copy(localEdgeTrimEnabled = prefs.getBoolean(PREF_LOCAL_EDGE_TRIM_ENABLED, true)) }
    updateLive { p -> p.copy(localComposedBackgroundEnabled = prefs.getBoolean(PREF_LOCAL_COMPOSED_BACKGROUND_ENABLED, true)) }
    updateLive { p -> p.copy(localTwoLayerCandidateEnabled = prefs.getBoolean(PREF_LOCAL_TWO_LAYER_CANDIDATE_ENABLED, true)) }
    updateLive { p -> p.copy(localComponentCandidatesEnabled = prefs.getBoolean(PREF_LOCAL_COMPONENT_CANDIDATES_ENABLED, true)) }
    updateLive { p -> p.copy(localTextSafeCandidateEnabled = prefs.getBoolean(PREF_LOCAL_TEXT_SAFE_CANDIDATE_ENABLED, true)) }
    updateLive { p -> p.copy(localAutoSelectionEnabled = prefs.getBoolean(PREF_LOCAL_AUTO_SELECTION_ENABLED, true)) }
    updateLive { p -> p.copy(localEdgePolishEnabled = prefs.getBoolean(PREF_LOCAL_EDGE_POLISH_ENABLED, true)) }
    updateLive { p -> p.copy(nightSubjectLightBackgroundEnabled = prefs.getBoolean(
        PREF_NIGHT_SUBJECT_LIGHT_BACKGROUND_ENABLED,
        false,
    )) }
    prefs.edit()
        .putInt(PREF_FOREGROUND_SUBJECT_PERCENT, getParams().foregroundSubjectPercent)
        .putInt(PREF_FOREGROUND_SHADOW_LEVEL, getParams().foregroundShadowLevel)
        .putFloat(PREF_MONOCHROME_THEME_SCALE, getParams().monochromeThemeScale)
        .putInt(PREF_BACKGROUND_SEPARATION_PERCENT, getParams().backgroundSeparationPercent)
        .putInt(PREF_PLATE_REMOVAL_PERCENT, getParams().plateRemovalPercent)
        .putInt(PREF_SHADOW_REMOVAL_PERCENT, getParams().shadowRemovalPercent)
        .putInt(PREF_EDGE_POLISH_PERCENT, getParams().edgePolishPercent)
        .putInt(PREF_RMBG_ALPHA_STRENGTH_PERCENT, getParams().rmbgAlphaStrengthPercent)
        .putInt(PREF_RMBG_EDGE_FEATHER_PERCENT, getParams().rmbgEdgeFeatherPercent)
        .putInt(PREF_RMBG_EDGE_ADJUST_PERCENT, getParams().rmbgEdgeAdjustPercent)
        .putInt(PREF_RMBG_WEAK_ALPHA_KEEP_PERCENT, getParams().rmbgWeakAlphaKeepPercent)
        .putString(PREF_ADAPTIVE_FOREGROUND_MODE, AdaptiveForegroundMode.fromValue(getParams().adaptiveForegroundMode).value)
        .putInt(PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT, getParams().adaptiveDirectMaxCoveragePercent)
        .putInt(PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT, getParams().adaptiveDirectMaxCoverageIncreasePercent)
        .putInt(PREF_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT, getParams().adaptiveMaskEdgeCoveragePercent)
        .putInt(PREF_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT, getParams().adaptiveMaskMinCoveragePercent)
        .putInt(PREF_ADAPTIVE_CENTER_EPSILON_PERCENT, getParams().adaptiveCenterEpsilonPercent)
        .putString(PREF_ORIGINAL_FOREGROUND_CLEANUP_MODE, OriginalForegroundCleanupMode.fromValue(getParams().originalForegroundCleanupMode).value)
        .putBoolean(PREF_LOCAL_BACKGROUND_SEPARATION_ENABLED, getParams().localBackgroundSeparationEnabled)
        .putBoolean(PREF_LOCAL_ADAPTIVE_SELECTION_ENABLED, getParams().localAdaptiveSelectionEnabled)
        .putBoolean(PREF_LOCAL_CORNER_MASK_CLEANUP_ENABLED, getParams().localCornerMaskCleanupEnabled)
        .putBoolean(PREF_LOCAL_ALPHA_EDGE_COLOR_REPAIR_ENABLED, getParams().localAlphaEdgeColorRepairEnabled)
        .putBoolean(PREF_LOCAL_PLAIN_BACKGROUND_ESTIMATION_ENABLED, getParams().localPlainBackgroundEstimationEnabled)
        .putBoolean(PREF_LOCAL_ORIGINAL_CLEANUP_ENABLED, getParams().localOriginalCleanupEnabled)
        .putBoolean(PREF_LOCAL_PLATE_CLEANUP_ENABLED, getParams().localPlateCleanupEnabled)
        .putBoolean(PREF_LOCAL_PLATE_EDGE_REPAIR_ENABLED, getParams().localPlateEdgeRepairEnabled)
        .putBoolean(PREF_LOCAL_PLATE_RESIDUE_CLEANUP_ENABLED, getParams().localPlateResidueCleanupEnabled)
        .putBoolean(PREF_LOCAL_SHADOW_CLEANUP_ENABLED, getParams().localShadowCleanupEnabled)
        .putBoolean(PREF_LOCAL_SHADOW_EDGE_REPAIR_ENABLED, getParams().localShadowEdgeRepairEnabled)
        .putBoolean(PREF_LOCAL_EDGE_TRIM_ENABLED, getParams().localEdgeTrimEnabled)
        .putBoolean(PREF_LOCAL_COMPOSED_BACKGROUND_ENABLED, getParams().localComposedBackgroundEnabled)
        .putBoolean(PREF_LOCAL_TWO_LAYER_CANDIDATE_ENABLED, getParams().localTwoLayerCandidateEnabled)
        .putBoolean(PREF_LOCAL_COMPONENT_CANDIDATES_ENABLED, getParams().localComponentCandidatesEnabled)
        .putBoolean(PREF_LOCAL_TEXT_SAFE_CANDIDATE_ENABLED, getParams().localTextSafeCandidateEnabled)
        .putBoolean(PREF_LOCAL_AUTO_SELECTION_ENABLED, getParams().localAutoSelectionEnabled)
        .putBoolean(PREF_LOCAL_EDGE_POLISH_ENABLED, getParams().localEdgePolishEnabled)
        .putBoolean(PREF_NIGHT_SUBJECT_LIGHT_BACKGROUND_ENABLED, getParams().nightSubjectLightBackgroundEnabled)
        .putInt(PREF_IMAGE_TUNING_VERSION, CURRENT_IMAGE_TUNING_VERSION)
        .putBoolean(PREF_FOREGROUND_SUBJECT_PERCENT_MIGRATED, true)
        .apply()
}

internal fun paramsUpdateForegroundSubjectPercent(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    getParams: () -> TuningParams,
    prefs: SharedPreferences,
    setDraftText: (String) -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    updateLive { p -> p.copy(foregroundSubjectPercent = value.coerceIn(
        MIN_FOREGROUND_SUBJECT_PERCENT,
        MAX_FOREGROUND_SUBJECT_PERCENT,
    )) }
    setDraftText(getParams().foregroundSubjectPercent.toString())
    prefs
        .edit()
        .putInt(PREF_FOREGROUND_SUBJECT_PERCENT, getParams().foregroundSubjectPercent)
        .apply()
    onRefresh(false)
}

internal fun paramsUpdateForegroundShadowLevel(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    getParams: () -> TuningParams,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    updateLive { p -> p.copy(foregroundShadowLevel = value.coerceIn(
        MIN_FOREGROUND_SHADOW_LEVEL,
        MAX_FOREGROUND_SHADOW_LEVEL,
    )) }
    setDraftText(getParams().foregroundShadowLevel.toString())
    onSave()
    onRefresh(false)
}

internal fun paramsUpdateMonochromeThemeScalePercent(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    val percent = value.coerceIn(MIN_MONOCHROME_THEME_SCALE_PERCENT, MAX_MONOCHROME_THEME_SCALE_PERCENT)
    updateLive { p -> p.copy(monochromeThemeScale = (percent.toFloat() / 100f).coerceIn(
        MIN_MONOCHROME_THEME_SCALE,
        MAX_MONOCHROME_THEME_SCALE,
    )) }
    setDraftText(percent.toString())
    onSave()
    onRefresh(false)
}

internal fun paramsUpdateBackgroundSeparationPercent(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    getParams: () -> TuningParams,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    updateLive { p -> p.copy(backgroundSeparationPercent = value.coerceIn(
        MIN_BACKGROUND_SEPARATION_PERCENT,
        MAX_BACKGROUND_SEPARATION_PERCENT,
    )) }
    setDraftText(getParams().backgroundSeparationPercent.toString())
    onSave()
    onRefresh(true)
}

internal fun paramsUpdatePlateRemovalPercent(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    getParams: () -> TuningParams,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    updateLive { p -> p.copy(plateRemovalPercent = value.coerceIn(
        MIN_PLATE_REMOVAL_PERCENT,
        MAX_PLATE_REMOVAL_PERCENT,
    )) }
    setDraftText(getParams().plateRemovalPercent.toString())
    onSave()
    onRefresh(true)
}

internal fun paramsUpdateShadowRemovalPercent(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    getParams: () -> TuningParams,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    updateLive { p -> p.copy(shadowRemovalPercent = value.coerceIn(
        MIN_SHADOW_REMOVAL_PERCENT,
        MAX_SHADOW_REMOVAL_PERCENT,
    )) }
    setDraftText(getParams().shadowRemovalPercent.toString())
    onSave()
    onRefresh(true)
}

internal fun paramsUpdateEdgePolishPercent(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    getParams: () -> TuningParams,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    updateLive { p -> p.copy(edgePolishPercent = value.coerceIn(
        MIN_EDGE_POLISH_PERCENT,
        MAX_EDGE_POLISH_PERCENT,
    )) }
    setDraftText(getParams().edgePolishPercent.toString())
    onSave()
    onRefresh(false)
}

internal fun paramsUpdateRmbgAlphaStrengthPercent(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    getParams: () -> TuningParams,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    updateLive { p -> p.copy(rmbgAlphaStrengthPercent = value.coerceIn(
        MIN_RMBG_ALPHA_STRENGTH_PERCENT,
        MAX_RMBG_ALPHA_STRENGTH_PERCENT,
    )) }
    setDraftText(getParams().rmbgAlphaStrengthPercent.toString())
    onSave()
    onRefresh(false)
}

internal fun paramsUpdateRmbgEdgeFeatherPercent(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    getParams: () -> TuningParams,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    updateLive { p -> p.copy(rmbgEdgeFeatherPercent = value.coerceIn(
        MIN_RMBG_EDGE_FEATHER_PERCENT,
        MAX_RMBG_EDGE_FEATHER_PERCENT,
    )) }
    setDraftText(getParams().rmbgEdgeFeatherPercent.toString())
    onSave()
    onRefresh(false)
}

internal fun paramsUpdateRmbgEdgeAdjustPercent(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    getParams: () -> TuningParams,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    updateLive { p -> p.copy(rmbgEdgeAdjustPercent = value.coerceIn(
        MIN_RMBG_EDGE_ADJUST_PERCENT,
        MAX_RMBG_EDGE_ADJUST_PERCENT,
    )) }
    setDraftText(getParams().rmbgEdgeAdjustPercent.toString())
    onSave()
    onRefresh(false)
}

internal fun paramsUpdateRmbgWeakAlphaKeepPercent(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    getParams: () -> TuningParams,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    updateLive { p -> p.copy(rmbgWeakAlphaKeepPercent = value.coerceIn(
        MIN_RMBG_WEAK_ALPHA_KEEP_PERCENT,
        MAX_RMBG_WEAK_ALPHA_KEEP_PERCENT,
    )) }
    setDraftText(getParams().rmbgWeakAlphaKeepPercent.toString())
    onSave()
    onRefresh(false)
}

internal fun paramsSaveImageTuningSettings(
    prefs: SharedPreferences,
    getParams: () -> TuningParams,
) {
    prefs
        .edit()
        .putInt(PREF_FOREGROUND_SUBJECT_PERCENT, getParams().foregroundSubjectPercent)
        .putInt(PREF_FOREGROUND_SHADOW_LEVEL, getParams().foregroundShadowLevel)
        .putFloat(PREF_MONOCHROME_THEME_SCALE, getParams().monochromeThemeScale)
        .putInt(PREF_BACKGROUND_SEPARATION_PERCENT, getParams().backgroundSeparationPercent)
        .putInt(PREF_PLATE_REMOVAL_PERCENT, getParams().plateRemovalPercent)
        .putInt(PREF_SHADOW_REMOVAL_PERCENT, getParams().shadowRemovalPercent)
        .putInt(PREF_EDGE_POLISH_PERCENT, getParams().edgePolishPercent)
        .putInt(PREF_RMBG_ALPHA_STRENGTH_PERCENT, getParams().rmbgAlphaStrengthPercent)
        .putInt(PREF_RMBG_EDGE_FEATHER_PERCENT, getParams().rmbgEdgeFeatherPercent)
        .putInt(PREF_RMBG_EDGE_ADJUST_PERCENT, getParams().rmbgEdgeAdjustPercent)
        .putInt(PREF_RMBG_WEAK_ALPHA_KEEP_PERCENT, getParams().rmbgWeakAlphaKeepPercent)
        .putString(PREF_ADAPTIVE_FOREGROUND_MODE, AdaptiveForegroundMode.fromValue(getParams().adaptiveForegroundMode).value)
        .putInt(PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT, getParams().adaptiveDirectMaxCoveragePercent)
        .putInt(PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT, getParams().adaptiveDirectMaxCoverageIncreasePercent)
        .putInt(PREF_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT, getParams().adaptiveMaskEdgeCoveragePercent)
        .putInt(PREF_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT, getParams().adaptiveMaskMinCoveragePercent)
        .putInt(PREF_ADAPTIVE_CENTER_EPSILON_PERCENT, getParams().adaptiveCenterEpsilonPercent)
        .putString(PREF_ORIGINAL_FOREGROUND_CLEANUP_MODE, OriginalForegroundCleanupMode.fromValue(getParams().originalForegroundCleanupMode).value)
        .putBoolean(PREF_LOCAL_BACKGROUND_SEPARATION_ENABLED, getParams().localBackgroundSeparationEnabled)
        .putBoolean(PREF_LOCAL_ADAPTIVE_SELECTION_ENABLED, getParams().localAdaptiveSelectionEnabled)
        .putBoolean(PREF_LOCAL_CORNER_MASK_CLEANUP_ENABLED, getParams().localCornerMaskCleanupEnabled)
        .putBoolean(PREF_LOCAL_ALPHA_EDGE_COLOR_REPAIR_ENABLED, getParams().localAlphaEdgeColorRepairEnabled)
        .putBoolean(PREF_LOCAL_PLAIN_BACKGROUND_ESTIMATION_ENABLED, getParams().localPlainBackgroundEstimationEnabled)
        .putBoolean(PREF_LOCAL_ORIGINAL_CLEANUP_ENABLED, getParams().localOriginalCleanupEnabled)
        .putBoolean(PREF_LOCAL_PLATE_CLEANUP_ENABLED, getParams().localPlateCleanupEnabled)
        .putBoolean(PREF_LOCAL_PLATE_EDGE_REPAIR_ENABLED, getParams().localPlateEdgeRepairEnabled)
        .putBoolean(PREF_LOCAL_PLATE_RESIDUE_CLEANUP_ENABLED, getParams().localPlateResidueCleanupEnabled)
        .putBoolean(PREF_LOCAL_SHADOW_CLEANUP_ENABLED, getParams().localShadowCleanupEnabled)
        .putBoolean(PREF_LOCAL_SHADOW_EDGE_REPAIR_ENABLED, getParams().localShadowEdgeRepairEnabled)
        .putBoolean(PREF_LOCAL_EDGE_TRIM_ENABLED, getParams().localEdgeTrimEnabled)
        .putBoolean(PREF_LOCAL_COMPOSED_BACKGROUND_ENABLED, getParams().localComposedBackgroundEnabled)
        .putBoolean(PREF_LOCAL_TWO_LAYER_CANDIDATE_ENABLED, getParams().localTwoLayerCandidateEnabled)
        .putBoolean(PREF_LOCAL_COMPONENT_CANDIDATES_ENABLED, getParams().localComponentCandidatesEnabled)
        .putBoolean(PREF_LOCAL_TEXT_SAFE_CANDIDATE_ENABLED, getParams().localTextSafeCandidateEnabled)
        .putBoolean(PREF_LOCAL_AUTO_SELECTION_ENABLED, getParams().localAutoSelectionEnabled)
        .putBoolean(PREF_LOCAL_EDGE_POLISH_ENABLED, getParams().localEdgePolishEnabled)
        .putBoolean(PREF_NIGHT_SUBJECT_LIGHT_BACKGROUND_ENABLED, getParams().nightSubjectLightBackgroundEnabled)
        .putInt(PREF_IMAGE_TUNING_VERSION, CURRENT_IMAGE_TUNING_VERSION)
        .apply()
}

/** 汇总当前全部调参字段为不可变快照（预设保存、撤销、debug 往返共用）。 */
internal fun paramsCurrentTuningParams(getParams: () -> TuningParams): TuningParams =
    getParams()

internal fun paramsCurrentLocalPipelineConfig(getParams: () -> TuningParams): LocalPipelineConfig =
    LocalPipelineConfig.from(paramsCurrentTuningParams(getParams))

internal fun paramsUpdateNightSubjectLightBackgroundEnabled(
    enabled: Boolean,
    getParams: () -> TuningParams,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    onSave: () -> Unit,
    setStatusText: (String) -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    if (getParams().nightSubjectLightBackgroundEnabled == enabled) {
        return
    }
    updateLive { p -> p.copy(nightSubjectLightBackgroundEnabled = enabled) }
    onSave()
    setStatusText(if (enabled) {
        "标准暗色已开启填充背景色"
    } else {
        "标准暗色已关闭填充背景色"
    })
    onRefresh(false)
}
