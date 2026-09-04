package dev.artplus.mobile

import android.content.SharedPreferences

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
