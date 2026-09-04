package dev.artplus.mobile

import android.content.SharedPreferences

/**
 * Slice 2.4：液态玻璃存取族（原 MainActivity 本体原样搬迁）。
 * 只做物理搬迁+显式参数化：prefs/快照经参数注入，draft 文本/开关/刷新经回调注入；
 * 分层迁移分支/钳制范围/存储顺序一律不变。
 * MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动。
 */

internal fun paramsLoadLiquidGlassSettings(
    prefs: SharedPreferences,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    getParams: () -> TuningParams,
    setDraftRadiusText: (String) -> Unit,
    setDraftOuterWidthText: (String) -> Unit,
    setDraftTopAlphaText: (String) -> Unit,
    setDraftBottomAlphaText: (String) -> Unit,
    setDraftBackgroundMistAlphaText: (String) -> Unit,
    setDraftBottomDarkAlphaText: (String) -> Unit,
    setDraftSubjectScaleText: (String) -> Unit,
    setDraftSubjectOutlineWidthText: (String) -> Unit,
    setDraftSubjectInnerOutlineWidthText: (String) -> Unit,
    setDraftSubjectShadowAlphaText: (String) -> Unit,
    setDraftSubjectOpacityText: (String) -> Unit,
    setBottomBarEnabled: (Boolean) -> Unit,
    setBottomBarBlurEnabled: (Boolean) -> Unit,
    getBottomBarEnabled: () -> Boolean,
    getBottomBarBlurEnabled: () -> Boolean,
    onSave: () -> Unit,
) {
    val migratedToLayered = prefs.getBoolean(PREF_LIQUID_GLASS_LAYERED_MIGRATED, false)
    updateLive { p -> p.copy(liquidGlassEnabled = if (migratedToLayered) {
        prefs.getBoolean(PREF_LIQUID_GLASS_ENABLED, true)
    } else {
        true
    }) }
    updateLive { p -> p.copy(liquidGlassRadius = prefs.getInt(
        PREF_LIQUID_GLASS_RADIUS,
        DEFAULT_LIQUID_GLASS_RADIUS,
    ).coerceIn(MIN_LIQUID_GLASS_RADIUS, MAX_LIQUID_GLASS_RADIUS)) }
    setDraftRadiusText(getParams().liquidGlassRadius.toString())
    updateLive { p -> p.copy(liquidGlassOuterWidth = prefs.getInt(
        PREF_LIQUID_GLASS_OUTER_WIDTH,
        prefs.getInt(PREF_LIQUID_GLASS_BACKGROUND_LEVEL_LEGACY, DEFAULT_LIQUID_GLASS_OUTER_WIDTH),
    ).coerceIn(MIN_LIQUID_GLASS_OUTER_WIDTH, MAX_LIQUID_GLASS_OUTER_WIDTH)) }
    setDraftOuterWidthText(getParams().liquidGlassOuterWidth.toString())
    updateLive { p -> p.copy(liquidGlassTopAlpha = prefs.getInt(
        PREF_LIQUID_GLASS_TOP_ALPHA,
        DEFAULT_LIQUID_GLASS_TOP_ALPHA,
    ).coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)) }
    setDraftTopAlphaText(getParams().liquidGlassTopAlpha.toString())
    updateLive { p -> p.copy(liquidGlassBottomAlpha = prefs.getInt(
        PREF_LIQUID_GLASS_BOTTOM_ALPHA,
        DEFAULT_LIQUID_GLASS_BOTTOM_ALPHA,
    ).coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)) }
    setDraftBottomAlphaText(getParams().liquidGlassBottomAlpha.toString())
    updateLive { p -> p.copy(liquidGlassBackgroundMistAlpha = prefs.getInt(
        PREF_LIQUID_GLASS_BACKGROUND_MIST_ALPHA,
        DEFAULT_LIQUID_GLASS_BACKGROUND_MIST_ALPHA,
    ).coerceIn(MIN_LIQUID_GLASS_MIST_ALPHA, MAX_LIQUID_GLASS_MIST_ALPHA)) }
    setDraftBackgroundMistAlphaText(getParams().liquidGlassBackgroundMistAlpha.toString())
    updateLive { p -> p.copy(liquidGlassBottomDarkAlpha = prefs.getInt(
        PREF_LIQUID_GLASS_BOTTOM_DARK_ALPHA,
        DEFAULT_LIQUID_GLASS_BOTTOM_DARK_ALPHA,
    ).coerceIn(MIN_LIQUID_GLASS_BOTTOM_DARK_ALPHA, MAX_LIQUID_GLASS_BOTTOM_DARK_ALPHA)) }
    setDraftBottomDarkAlphaText(getParams().liquidGlassBottomDarkAlpha.toString())
    updateLive { p -> p.copy(liquidGlassSubjectScalePercent = prefs.getInt(
        PREF_LIQUID_GLASS_SUBJECT_SCALE_PERCENT,
        DEFAULT_LIQUID_GLASS_SUBJECT_SCALE_PERCENT,
    ).coerceIn(MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT, MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT)) }
    setDraftSubjectScaleText(getParams().liquidGlassSubjectScalePercent.toString())
    updateLive { p -> p.copy(liquidGlassSubjectOutlineWidth = prefs.getInt(
        PREF_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
        DEFAULT_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
    ).coerceIn(MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH, MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH)) }
    setDraftSubjectOutlineWidthText(getParams().liquidGlassSubjectOutlineWidth.toString())
    updateLive { p -> p.copy(liquidGlassSubjectInnerOutlineWidth = prefs.getInt(
        PREF_LIQUID_GLASS_SUBJECT_INNER_OUTLINE_WIDTH,
        DEFAULT_LIQUID_GLASS_SUBJECT_INNER_OUTLINE_WIDTH,
    ).coerceIn(MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH, MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH)) }
    setDraftSubjectInnerOutlineWidthText(getParams().liquidGlassSubjectInnerOutlineWidth.toString())
    updateLive { p -> p.copy(liquidGlassSubjectShadowAlpha = prefs.getInt(
        PREF_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA,
        DEFAULT_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA,
    ).coerceIn(MIN_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA, MAX_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA)) }
    setDraftSubjectShadowAlphaText(getParams().liquidGlassSubjectShadowAlpha.toString())
    updateLive { p -> p.copy(liquidGlassSubjectOpacityPercent = prefs.getInt(
        PREF_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
        DEFAULT_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
    ).coerceIn(MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT, MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT)) }
    setDraftSubjectOpacityText(getParams().liquidGlassSubjectOpacityPercent.toString())
    setBottomBarEnabled(prefs.getBoolean(PREF_LIQUID_GLASS_BOTTOM_BAR_ENABLED, true))
    setBottomBarBlurEnabled(prefs.getBoolean(PREF_LIQUID_GLASS_BOTTOM_BAR_BLUR_ENABLED, true))
    if (!migratedToLayered) {
        onSave()
    }
}

internal fun paramsSaveLiquidGlassSettings(
    prefs: SharedPreferences,
    getParams: () -> TuningParams,
    getBottomBarEnabled: () -> Boolean,
    getBottomBarBlurEnabled: () -> Boolean,
) {
    prefs
        .edit()
        .paramsPutLiquidGlassSettings(
            params = getParams(),
            bottomBarEnabled = getBottomBarEnabled(),
            bottomBarBlurEnabled = getBottomBarBlurEnabled(),
        )
        .apply()
}

internal fun SharedPreferences.Editor.paramsPutLiquidGlassSettings(
    params: TuningParams,
    bottomBarEnabled: Boolean,
    bottomBarBlurEnabled: Boolean,
): SharedPreferences.Editor =
    putBoolean(PREF_LIQUID_GLASS_LAYERED_MIGRATED, true)
        .putBoolean(PREF_LIQUID_GLASS_ENABLED, params.liquidGlassEnabled)
        .putBoolean(PREF_LIQUID_GLASS_BOTTOM_BAR_ENABLED, bottomBarEnabled)
        .putBoolean(PREF_LIQUID_GLASS_BOTTOM_BAR_BLUR_ENABLED, bottomBarBlurEnabled)
        .putInt(PREF_LIQUID_GLASS_RADIUS, params.liquidGlassRadius)
        .putInt(PREF_LIQUID_GLASS_OUTER_WIDTH, params.liquidGlassOuterWidth)
        .putInt(PREF_LIQUID_GLASS_TOP_ALPHA, params.liquidGlassTopAlpha)
        .putInt(PREF_LIQUID_GLASS_BOTTOM_ALPHA, params.liquidGlassBottomAlpha)
        .putInt(PREF_LIQUID_GLASS_BACKGROUND_MIST_ALPHA, params.liquidGlassBackgroundMistAlpha)
        .putInt(PREF_LIQUID_GLASS_BOTTOM_DARK_ALPHA, params.liquidGlassBottomDarkAlpha)
        .putInt(PREF_LIQUID_GLASS_SUBJECT_SCALE_PERCENT, params.liquidGlassSubjectScalePercent)
        .putInt(PREF_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH, params.liquidGlassSubjectOutlineWidth)
        .putInt(PREF_LIQUID_GLASS_SUBJECT_INNER_OUTLINE_WIDTH, params.liquidGlassSubjectInnerOutlineWidth)
        .putInt(PREF_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA, params.liquidGlassSubjectShadowAlpha)
        .putInt(PREF_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT, params.liquidGlassSubjectOpacityPercent)

internal fun paramsUpdateLiquidGlassEnabled(
    enabled: Boolean,
    getParams: () -> TuningParams,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    onSave: () -> Unit,
    setStatusText: (String) -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    if (getParams().liquidGlassEnabled == enabled) {
        return
    }
    updateLive { p -> p.copy(liquidGlassEnabled = enabled) }
    onSave()
    setStatusText(if (enabled) "液态玻璃风格已开启" else "液态玻璃风格已关闭")
    onRefresh(false)
}

internal fun paramsUpdateLiquidGlassRadius(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    setStatusText: (String) -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    val next = value.coerceIn(MIN_LIQUID_GLASS_RADIUS, MAX_LIQUID_GLASS_RADIUS)
    updateLive { p -> p.copy(liquidGlassRadius = next) }
    setDraftText(next.toString())
    onSave()
    setStatusText("液态玻璃圆角 $next")
    onRefresh(false)
}

internal fun paramsUpdateLiquidGlassOuterWidth(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    setStatusText: (String) -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    val next = value.coerceIn(MIN_LIQUID_GLASS_OUTER_WIDTH, MAX_LIQUID_GLASS_OUTER_WIDTH)
    updateLive { p -> p.copy(liquidGlassOuterWidth = next) }
    setDraftText(next.toString())
    onSave()
    setStatusText("液态玻璃外框高度 $next")
    onRefresh(false)
}

internal fun paramsUpdateLiquidGlassTopAlpha(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    setStatusText: (String) -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    val next = value.coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)
    updateLive { p -> p.copy(liquidGlassTopAlpha = next) }
    setDraftText(next.toString())
    onSave()
    setStatusText("液态玻璃顶部强度 $next")
    onRefresh(false)
}

internal fun paramsUpdateLiquidGlassBottomAlpha(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    setStatusText: (String) -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    val next = value.coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)
    updateLive { p -> p.copy(liquidGlassBottomAlpha = next) }
    setDraftText(next.toString())
    onSave()
    setStatusText("液态玻璃底边强度 $next")
    onRefresh(false)
}

internal fun paramsUpdateLiquidGlassBackgroundMistAlpha(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    setStatusText: (String) -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    val next = value.coerceIn(MIN_LIQUID_GLASS_MIST_ALPHA, MAX_LIQUID_GLASS_MIST_ALPHA)
    updateLive { p -> p.copy(liquidGlassBackgroundMistAlpha = next) }
    setDraftText(next.toString())
    onSave()
    setStatusText("液态玻璃背景灰雾 $next")
    onRefresh(false)
}

internal fun paramsUpdateLiquidGlassBottomDarkAlpha(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    setStatusText: (String) -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    val next = value.coerceIn(MIN_LIQUID_GLASS_BOTTOM_DARK_ALPHA, MAX_LIQUID_GLASS_BOTTOM_DARK_ALPHA)
    updateLive { p -> p.copy(liquidGlassBottomDarkAlpha = next) }
    setDraftText(next.toString())
    onSave()
    setStatusText("液态玻璃底部灰雾 $next")
    onRefresh(false)
}

internal fun paramsUpdateLiquidGlassSubjectScalePercent(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    setStatusText: (String) -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    val next = value.coerceIn(MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT, MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT)
    updateLive { p -> p.copy(liquidGlassSubjectScalePercent = next) }
    setDraftText(next.toString())
    onSave()
    setStatusText("液态玻璃主体比例 $next")
    onRefresh(false)
}

internal fun paramsUpdateLiquidGlassSubjectOutlineWidth(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    setStatusText: (String) -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    val next = value.coerceIn(MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH, MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH)
    updateLive { p -> p.copy(liquidGlassSubjectOutlineWidth = next) }
    setDraftText(next.toString())
    onSave()
    setStatusText("液态玻璃主体外框 $next")
    onRefresh(false)
}

internal fun paramsUpdateLiquidGlassSubjectInnerOutlineWidth(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    setStatusText: (String) -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    val next = value.coerceIn(MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH, MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH)
    updateLive { p -> p.copy(liquidGlassSubjectInnerOutlineWidth = next) }
    setDraftText(next.toString())
    onSave()
    setStatusText("液态玻璃主体内框 $next")
    onRefresh(false)
}

internal fun paramsUpdateLiquidGlassSubjectShadowAlpha(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    setStatusText: (String) -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    val next = value.coerceIn(MIN_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA, MAX_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA)
    updateLive { p -> p.copy(liquidGlassSubjectShadowAlpha = next) }
    setDraftText(next.toString())
    onSave()
    setStatusText("液态玻璃主体阴影 $next")
    onRefresh(false)
}

internal fun paramsUpdateLiquidGlassSubjectOpacityPercent(
    value: Int,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
    setStatusText: (String) -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    val next = value.coerceIn(MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT, MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT)
    updateLive { p -> p.copy(liquidGlassSubjectOpacityPercent = next) }
    setDraftText(next.toString())
    onSave()
    setStatusText("液态玻璃主体透明度 $next")
    onRefresh(false)
}
