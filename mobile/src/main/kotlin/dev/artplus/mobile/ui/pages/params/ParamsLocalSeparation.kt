package dev.artplus.mobile.ui.pages.params

import android.content.SharedPreferences
import dev.artplus.mobile.DEFAULT_BACKGROUND_SEPARATION_PERCENT
import dev.artplus.mobile.DEFAULT_EDGE_POLISH_PERCENT
import dev.artplus.mobile.DEFAULT_PLATE_REMOVAL_PERCENT
import dev.artplus.mobile.DEFAULT_SHADOW_REMOVAL_PERCENT
import dev.artplus.mobile.PREF_LOCAL_SEPARATION_MODE
import dev.artplus.mobile.TuningParams
import dev.artplus.mobile.imaging.LocalSeparationMode
import dev.artplus.mobile.imaging.PreviewChoice
import dev.artplus.mobile.pipeline.GenerationSession

/**
 * 稳定规则的快捷风格档位：把 4 项强度与 18 个底层开关收口为 4 种一眼能懂的风格。
 * 只改本地分离相关字段，不覆盖用户的 AI / RMBG / 主体大小等其他设置。
 */
internal enum class LocalTuningStylePreset(
    val label: String,
    val summary: String,
    val backgroundEnabled: Boolean,
    val backgroundPercent: Int,
    val plateEnabled: Boolean,
    val platePercent: Int,
    val shadowEnabled: Boolean,
    val shadowPercent: Int,
    val edgePolishEnabled: Boolean,
    val edgePolishPercent: Int,
) {
    Standard(
        label = "标准",
        summary = "默认均衡方案，适合绝大多数应用图标",
        backgroundEnabled = true,
        backgroundPercent = DEFAULT_BACKGROUND_SEPARATION_PERCENT,
        plateEnabled = true,
        platePercent = DEFAULT_PLATE_REMOVAL_PERCENT,
        shadowEnabled = true,
        shadowPercent = DEFAULT_SHADOW_REMOVAL_PERCENT,
        edgePolishEnabled = true,
        edgePolishPercent = DEFAULT_EDGE_POLISH_PERCENT,
    ),
    Gentle(
        label = "温和保留",
        summary = "降低清理力度，减少浅色图标与文字被误删",
        backgroundEnabled = true,
        backgroundPercent = 38,
        plateEnabled = true,
        platePercent = 35,
        shadowEnabled = true,
        shadowPercent = 40,
        edgePolishEnabled = true,
        edgePolishPercent = 45,
    ),
    Strong(
        label = "强力去底",
        summary = "加大背景与底板清理力度，适合顽固白底或方底图标",
        backgroundEnabled = true,
        backgroundPercent = 78,
        plateEnabled = true,
        platePercent = 80,
        shadowEnabled = true,
        shadowPercent = 78,
        edgePolishEnabled = true,
        edgePolishPercent = 72,
    ),
    LightTouch(
        label = "纯净轻修",
        summary = "保留图标自带底板与投影，只分离外围背景并润色边缘",
        backgroundEnabled = true,
        backgroundPercent = 55,
        plateEnabled = false,
        platePercent = DEFAULT_PLATE_REMOVAL_PERCENT,
        shadowEnabled = false,
        shadowPercent = DEFAULT_SHADOW_REMOVAL_PERCENT,
        edgePolishEnabled = true,
        edgePolishPercent = 55,
    ),
    ;

    fun applyTo(current: TuningParams): TuningParams = current.copy(
        backgroundSeparationPercent = backgroundPercent,
        plateRemovalPercent = platePercent,
        shadowRemovalPercent = shadowPercent,
        edgePolishPercent = edgePolishPercent,
        localBackgroundSeparationEnabled = backgroundEnabled,
        localPlainBackgroundEstimationEnabled = backgroundEnabled,
        localPlateCleanupEnabled = plateEnabled,
        localPlateEdgeRepairEnabled = plateEnabled,
        localPlateResidueCleanupEnabled = plateEnabled,
        localOriginalCleanupEnabled = plateEnabled,
        localShadowCleanupEnabled = shadowEnabled,
        localShadowEdgeRepairEnabled = shadowEnabled,
        localEdgePolishEnabled = edgePolishEnabled,
        localAlphaEdgeColorRepairEnabled = edgePolishEnabled,
        localEdgeTrimEnabled = edgePolishEnabled,
        localCornerMaskCleanupEnabled = edgePolishEnabled,
        localAdaptiveSelectionEnabled = true,
        localComposedBackgroundEnabled = true,
        localTwoLayerCandidateEnabled = true,
        localComponentCandidatesEnabled = true,
        localTextSafeCandidateEnabled = true,
        localAutoSelectionEnabled = true,
    )

    fun matches(params: TuningParams): Boolean =
        params.localBackgroundSeparationEnabled == backgroundEnabled &&
            params.localPlainBackgroundEstimationEnabled == backgroundEnabled &&
            (!backgroundEnabled || params.backgroundSeparationPercent == backgroundPercent) &&
            params.localPlateCleanupEnabled == plateEnabled &&
            params.localPlateEdgeRepairEnabled == plateEnabled &&
            params.localPlateResidueCleanupEnabled == plateEnabled &&
            params.localOriginalCleanupEnabled == plateEnabled &&
            (!plateEnabled || params.plateRemovalPercent == platePercent) &&
            params.localShadowCleanupEnabled == shadowEnabled &&
            params.localShadowEdgeRepairEnabled == shadowEnabled &&
            (!shadowEnabled || params.shadowRemovalPercent == shadowPercent) &&
            params.localEdgePolishEnabled == edgePolishEnabled &&
            params.localAlphaEdgeColorRepairEnabled == edgePolishEnabled &&
            params.localEdgeTrimEnabled == edgePolishEnabled &&
            params.localCornerMaskCleanupEnabled == edgePolishEnabled &&
            (!edgePolishEnabled || params.edgePolishPercent == edgePolishPercent) &&
            params.localAutoSelectionEnabled &&
            params.localComposedBackgroundEnabled &&
            params.localTwoLayerCandidateEnabled &&
            params.localComponentCandidatesEnabled &&
            params.localTextSafeCandidateEnabled
}

internal fun matchingLocalStylePreset(params: TuningParams): LocalTuningStylePreset? =
    LocalTuningStylePreset.entries.firstOrNull { it.matches(params) }

/** 核心处理 4 个聚合开关 + 候选策略聚合开关，联动底层 18 个布尔位以保持行为一致。 */
internal enum class LocalTuningGroup {
    Background,
    Plate,
    Shadow,
    EdgePolish,
    MultiCandidates,
}

internal fun applyLocalTuningGroupToggle(
    params: TuningParams,
    group: LocalTuningGroup,
    enabled: Boolean,
): TuningParams = when (group) {
    LocalTuningGroup.Background -> params.copy(
        localBackgroundSeparationEnabled = enabled,
        localPlainBackgroundEstimationEnabled = enabled,
    )
    LocalTuningGroup.Plate -> params.copy(
        localPlateCleanupEnabled = enabled,
        localPlateEdgeRepairEnabled = enabled,
        localPlateResidueCleanupEnabled = enabled,
        localOriginalCleanupEnabled = enabled,
    )
    LocalTuningGroup.Shadow -> params.copy(
        localShadowCleanupEnabled = enabled,
        localShadowEdgeRepairEnabled = enabled,
    )
    LocalTuningGroup.EdgePolish -> params.copy(
        localEdgePolishEnabled = enabled,
        localAlphaEdgeColorRepairEnabled = enabled,
        localEdgeTrimEnabled = enabled,
        localCornerMaskCleanupEnabled = enabled,
    )
    LocalTuningGroup.MultiCandidates -> params.copy(
        localComposedBackgroundEnabled = enabled,
        localTwoLayerCandidateEnabled = enabled,
        localComponentCandidatesEnabled = enabled,
        localTextSafeCandidateEnabled = enabled,
        localAdaptiveSelectionEnabled = enabled,
    )
}

internal fun countEnabledLocalWorkflowSteps(params: TuningParams): Int = listOf(
    params.localBackgroundSeparationEnabled,
    params.localAdaptiveSelectionEnabled,
    params.localCornerMaskCleanupEnabled,
    params.localAlphaEdgeColorRepairEnabled,
    params.localPlainBackgroundEstimationEnabled,
    params.localOriginalCleanupEnabled,
    params.localPlateCleanupEnabled,
    params.localPlateEdgeRepairEnabled,
    params.localPlateResidueCleanupEnabled,
    params.localShadowCleanupEnabled,
    params.localShadowEdgeRepairEnabled,
    params.localEdgeTrimEnabled,
    params.localComposedBackgroundEnabled,
    params.localTwoLayerCandidateEnabled,
    params.localComponentCandidatesEnabled,
    params.localTextSafeCandidateEnabled,
    params.localAutoSelectionEnabled,
    params.localEdgePolishEnabled,
).count { it }

internal fun paramsApplyLocalTuningBatch(
    transform: (TuningParams) -> TuningParams,
    statusMessage: String,
    getParams: () -> TuningParams,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    syncLocalDrafts: (TuningParams) -> Unit,
    onSaveImageTuning: () -> Unit,
    setStatusText: (String) -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    val before = getParams()
    val after = transform(before)
    if (after == before) {
        return
    }
    updateLive { transform(it) }
    syncLocalDrafts(getParams())
    onSaveImageTuning()
    setStatusText(statusMessage)
    onRefresh(true)
}

/**
 * Slice 2.4：本地分离存取族（原 MainActivity 本体原样搬迁）。
 * 只做物理搬迁+显式参数化：prefs/快照经参数注入，预览刷新等经回调注入。
 * MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动。
 */

internal fun paramsLoadLocalSeparationSettings(
    prefs: SharedPreferences,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
) {
    updateLive { p -> p.copy(localSeparationMode = (LocalSeparationMode.fromValue(
        prefs.getString(PREF_LOCAL_SEPARATION_MODE, LocalSeparationMode.Auto.value),
    )).value) }
}

internal fun paramsSaveLocalSeparationSettings(
    prefs: SharedPreferences,
    getParams: () -> TuningParams,
) {
    prefs
        .edit()
        .putString(PREF_LOCAL_SEPARATION_MODE, LocalSeparationMode.fromValue(getParams().localSeparationMode).value)
        .apply()
}

internal fun paramsUpdateLocalSeparationMode(
    mode: LocalSeparationMode,
    getParams: () -> TuningParams,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    getSession: () -> GenerationSession?,
    defaultPreviewChoiceForMode: (LocalSeparationMode, PreviewChoice) -> PreviewChoice,
    onSave: () -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean, retargetFrom: PreviewChoice?) -> Unit,
) {
    if (LocalSeparationMode.fromValue(getParams().localSeparationMode) == mode) {
        return
    }
    val session = getSession()
    val previousDefault = session?.let {
        defaultPreviewChoiceForMode(LocalSeparationMode.fromValue(getParams().localSeparationMode), it.autoLocalChoice)
    }
    updateLive { p -> p.copy(localSeparationMode = (mode).value) }
    onSave()
    onRefresh(true, previousDefault)
}

internal fun paramsUpdateLocalWorkflowToggle(
    name: String,
    enabled: Boolean,
    getParams: () -> TuningParams,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    onSaveImageTuning: () -> Unit,
    setStatusText: (String) -> Unit,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    val previous = when (name) {
        "background" -> getParams().localBackgroundSeparationEnabled
        "adaptive" -> getParams().localAdaptiveSelectionEnabled
        "corner" -> getParams().localCornerMaskCleanupEnabled
        "alpha_edge_repair" -> getParams().localAlphaEdgeColorRepairEnabled
        "plain_background" -> getParams().localPlainBackgroundEstimationEnabled
        "original" -> getParams().localOriginalCleanupEnabled
        "plate" -> getParams().localPlateCleanupEnabled
        "plate_edge" -> getParams().localPlateEdgeRepairEnabled
        "plate_residue" -> getParams().localPlateResidueCleanupEnabled
        "shadow" -> getParams().localShadowCleanupEnabled
        "shadow_edge" -> getParams().localShadowEdgeRepairEnabled
        "edge_trim" -> getParams().localEdgeTrimEnabled
        "composed" -> getParams().localComposedBackgroundEnabled
        "two_layer" -> getParams().localTwoLayerCandidateEnabled
        "component" -> getParams().localComponentCandidatesEnabled
        "text_safe" -> getParams().localTextSafeCandidateEnabled
        "auto" -> getParams().localAutoSelectionEnabled
        "edge_polish" -> getParams().localEdgePolishEnabled
        else -> return
    }
    if (previous == enabled) {
        return
    }
    when (name) {
        "background" -> updateLive { p -> p.copy(localBackgroundSeparationEnabled = enabled) }
        "adaptive" -> updateLive { p -> p.copy(localAdaptiveSelectionEnabled = enabled) }
        "corner" -> updateLive { p -> p.copy(localCornerMaskCleanupEnabled = enabled) }
        "alpha_edge_repair" -> updateLive { p -> p.copy(localAlphaEdgeColorRepairEnabled = enabled) }
        "plain_background" -> updateLive { p -> p.copy(localPlainBackgroundEstimationEnabled = enabled) }
        "original" -> updateLive { p -> p.copy(localOriginalCleanupEnabled = enabled) }
        "plate" -> updateLive { p -> p.copy(localPlateCleanupEnabled = enabled) }
        "plate_edge" -> updateLive { p -> p.copy(localPlateEdgeRepairEnabled = enabled) }
        "plate_residue" -> updateLive { p -> p.copy(localPlateResidueCleanupEnabled = enabled) }
        "shadow" -> updateLive { p -> p.copy(localShadowCleanupEnabled = enabled) }
        "shadow_edge" -> updateLive { p -> p.copy(localShadowEdgeRepairEnabled = enabled) }
        "edge_trim" -> updateLive { p -> p.copy(localEdgeTrimEnabled = enabled) }
        "composed" -> updateLive { p -> p.copy(localComposedBackgroundEnabled = enabled) }
        "two_layer" -> updateLive { p -> p.copy(localTwoLayerCandidateEnabled = enabled) }
        "component" -> updateLive { p -> p.copy(localComponentCandidatesEnabled = enabled) }
        "text_safe" -> updateLive { p -> p.copy(localTextSafeCandidateEnabled = enabled) }
        "auto" -> updateLive { p -> p.copy(localAutoSelectionEnabled = enabled) }
        "edge_polish" -> updateLive { p -> p.copy(localEdgePolishEnabled = enabled) }
    }
    onSaveImageTuning()
    setStatusText(if (enabled) "已启用本地步骤" else "已关闭本地步骤")
    onRefresh(true)
}
