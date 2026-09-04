package dev.artplus.mobile

import kotlin.math.roundToInt

/**
 * Slice 2.4：历史/应用/加载族（原 MainActivity 本体原样搬迁）。
 * 只做物理搬迁+显式参数化：快照/历史经 MainViewModel 显式同步，draft 文本/状态/刷新经回调注入。
 * MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动。
 */

internal fun paramsApplyTuningParams(
    params: TuningParams,
    rebuildCandidates: Boolean = true,
    persist: Boolean = true,
    captureUndo: Boolean = true,
    refreshPreview: Boolean = true,
    getBefore: () -> TuningParams,
    onCaptureUndo: (TuningParams) -> Unit,
    onParamsApplied: (before: TuningParams, applied: TuningParams, captureUndo: Boolean) -> Unit,
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
    setDraftLiquidGlassRadiusText: (String) -> Unit,
    setDraftLiquidGlassOuterWidthText: (String) -> Unit,
    setDraftLiquidGlassTopAlphaText: (String) -> Unit,
    setDraftLiquidGlassBottomAlphaText: (String) -> Unit,
    setDraftLiquidGlassBackgroundMistAlphaText: (String) -> Unit,
    setDraftLiquidGlassBottomDarkAlphaText: (String) -> Unit,
    setDraftLiquidGlassSubjectScaleText: (String) -> Unit,
    setDraftLiquidGlassSubjectOutlineWidthText: (String) -> Unit,
    setDraftLiquidGlassSubjectInnerOutlineWidthText: (String) -> Unit,
    setDraftLiquidGlassSubjectShadowAlphaText: (String) -> Unit,
    setDraftLiquidGlassSubjectOpacityText: (String) -> Unit,
    setDraftJsonParamsText: (String) -> Unit,
    onSaveLocalSeparation: () -> Unit,
    onSaveImageTuning: () -> Unit,
    onSaveLiquidGlass: () -> Unit,
    onSaveGpt: () -> Unit,
    onSaveUi: () -> Unit,
    isBusy: () -> Boolean,
    getSession: () -> GenerationSession?,
    onRefresh: (rebuildLocalCandidates: Boolean) -> Unit,
) {
    val before = getBefore()
    if (captureUndo) {
        onCaptureUndo(before)
    }
    // P2 交界：历史/快照单源在 MainViewModel，用快照显式同步（VM 不读 Activity 字段）；
    // 186 live vars 仍是 UI 真源（P5 重写），applied 传本函数收到的快照参数。
    onParamsApplied(before, params, captureUndo)
    setDraftForegroundSubjectPercentText(params.foregroundSubjectPercent.toString())
    setDraftForegroundShadowLevelText(params.foregroundShadowLevel.toString())
    setDraftMonochromeThemeScaleText((params.monochromeThemeScale * 100).roundToInt().toString())
    setDraftBackgroundSeparationText(params.backgroundSeparationPercent.toString())
    setDraftPlateRemovalText(params.plateRemovalPercent.toString())
    setDraftShadowRemovalText(params.shadowRemovalPercent.toString())
    setDraftEdgePolishText(params.edgePolishPercent.toString())
    setDraftRmbgAlphaStrengthText(params.rmbgAlphaStrengthPercent.toString())
    setDraftRmbgEdgeFeatherText(params.rmbgEdgeFeatherPercent.toString())
    setDraftRmbgEdgeAdjustText(params.rmbgEdgeAdjustPercent.toString())
    setDraftRmbgWeakAlphaKeepText(params.rmbgWeakAlphaKeepPercent.toString())
    setDraftLiquidGlassRadiusText(params.liquidGlassRadius.toString())
    setDraftLiquidGlassOuterWidthText(params.liquidGlassOuterWidth.toString())
    setDraftLiquidGlassTopAlphaText(params.liquidGlassTopAlpha.toString())
    setDraftLiquidGlassBottomAlphaText(params.liquidGlassBottomAlpha.toString())
    setDraftLiquidGlassBackgroundMistAlphaText(params.liquidGlassBackgroundMistAlpha.toString())
    setDraftLiquidGlassBottomDarkAlphaText(params.liquidGlassBottomDarkAlpha.toString())
    setDraftLiquidGlassSubjectScaleText(params.liquidGlassSubjectScalePercent.toString())
    setDraftLiquidGlassSubjectOutlineWidthText(params.liquidGlassSubjectOutlineWidth.toString())
    setDraftLiquidGlassSubjectInnerOutlineWidthText(params.liquidGlassSubjectInnerOutlineWidth.toString())
    setDraftLiquidGlassSubjectShadowAlphaText(params.liquidGlassSubjectShadowAlpha.toString())
    setDraftLiquidGlassSubjectOpacityText(params.liquidGlassSubjectOpacityPercent.toString())
    setDraftJsonParamsText(params.toJson().toString(4))
    if (persist) {
        onSaveLocalSeparation()
        onSaveImageTuning()
        onSaveLiquidGlass()
        if (
            before.gptImageMode != params.gptImageMode ||
            before.gptPromptPreset != params.gptPromptPreset ||
            before.gptCustomPrompt != params.gptCustomPrompt
        ) {
            onSaveGpt()
        }
        onSaveUi()
    }
    if (refreshPreview && !isBusy() && getSession() != null) {
        onRefresh(rebuildCandidates)
    }
}

/** 撤销上一次参数应用（预设/批量前自动捕获快照）。 */
internal fun paramsRestoreLastParams(
    getSnapshot: () -> TuningParams?,
    clearSnapshot: () -> Unit,
    setStatusText: (String) -> Unit,
    onApply: (TuningParams) -> Unit,
) {
    val snapshot = getSnapshot() ?: run {
        setStatusText("没有可还原的参数")
        return
    }
    clearSnapshot()
    onApply(snapshot)
    setStatusText("已还原上一个参数")
}

/**
 * 恢复默认配置（Issue #4）。
 * 仅重置全部调参到出厂默认值（TuningParams 默认构造），不清除已下载的 RMBG 模型与已生成的图标包。
 * 通过 TuningParams 默认值 + applyTuningParams 统一持久化，保证与各迁移逻辑一致。
 */
internal fun paramsResetToDefaults(
    confirmed: Boolean = false,
    isBusy: () -> Boolean,
    isGeneratingGpt: () -> Boolean,
    isGeneratingRmbg: () -> Boolean,
    setStatusText: (String) -> Unit,
    onRequestConfirm: (title: String, message: String, confirmLabel: String, onConfirm: () -> Unit) -> Unit,
    onApplyDefaults: (TuningParams) -> Unit,
    onClearPreset: () -> Unit,
) {
    if (isBusy() || isGeneratingGpt() || isGeneratingRmbg()) {
        setStatusText("当前有任务在运行，请等待")
        return
    }
    if (!confirmed) {
        onRequestConfirm(
            "恢复默认配置",
            "将把全部调参恢复为出厂默认值（不影响已下载的 RMBG 模型与已生成的图标包），可通过「还原上一步」撤销。确认继续？",
            "恢复默认",
            { paramsResetToDefaultsInner(isBusy, isGeneratingGpt, isGeneratingRmbg, setStatusText, onApplyDefaults, onClearPreset) },
        )
        return
    }
    paramsResetToDefaultsInner(isBusy, isGeneratingGpt, isGeneratingRmbg, setStatusText, onApplyDefaults, onClearPreset)
}

private fun paramsResetToDefaultsInner(
    isBusy: () -> Boolean,
    isGeneratingGpt: () -> Boolean,
    isGeneratingRmbg: () -> Boolean,
    setStatusText: (String) -> Unit,
    onApplyDefaults: (TuningParams) -> Unit,
    onClearPreset: () -> Unit,
) {
    if (isBusy() || isGeneratingGpt() || isGeneratingRmbg()) {
        setStatusText("当前有任务在运行，请等待")
        return
    }
    val defaults = TuningParams()
    onApplyDefaults(defaults)
    onClearPreset()
    setStatusText("已恢复默认配置")
}

internal fun paramsInitTuningHistory(
    getParams: () -> TuningParams,
    resetHistory: (TuningParams) -> Unit,
) {
    // P2 交界：历史基线进 MainViewModel（冷启动时快照显式同步一次）。
    resetHistory(getParams())
}

internal fun paramsUndoTuning(
    isBusy: () -> Boolean,
    isGeneratingGpt: () -> Boolean,
    isGeneratingRmbg: () -> Boolean,
    setStatusText: (String) -> Unit,
    onUndo: () -> TuningParams?,
    onApply: (TuningParams) -> Unit,
    onClearPreset: () -> Unit,
) {
    if (isBusy() || isGeneratingGpt() || isGeneratingRmbg()) {
        setStatusText("当前有任务在运行，请等待")
        return
    }
    // P2 交界：取栈顶目标由 MainViewModel 判定（null 即已到最早），UI 状态留 Activity。
    val target = onUndo()
    if (target == null) {
        setStatusText("已到最早的配置")
        return
    }
    onApply(target)
    onClearPreset()
    setStatusText("已后退到上一个配置")
}

internal fun paramsRedoTuning(
    isBusy: () -> Boolean,
    isGeneratingGpt: () -> Boolean,
    isGeneratingRmbg: () -> Boolean,
    setStatusText: (String) -> Unit,
    onRedo: () -> TuningParams?,
    onApply: (TuningParams) -> Unit,
    onClearPreset: () -> Unit,
) {
    if (isBusy() || isGeneratingGpt() || isGeneratingRmbg()) {
        setStatusText("当前有任务在运行，请等待")
        return
    }
    // P2 交界：取栈顶目标由 MainViewModel 判定（null 即已到最新），UI 状态留 Activity。
    val target = onRedo()
    if (target == null) {
        setStatusText("已到最新的配置")
        return
    }
    onApply(target)
    onClearPreset()
    setStatusText("已前进到下一个配置")
}

/** 启动时统一加载调参相关设置（保留各迁移分支）。 */
internal fun paramsLoadTuningParams(
    onLoadLocal: () -> Unit,
    onLoadImage: () -> Unit,
    onLoadLiquid: () -> Unit,
    getParams: () -> TuningParams,
    setDraftJsonParamsText: (String) -> Unit,
) {
    onLoadLocal()
    onLoadImage()
    onLoadLiquid()
    setDraftJsonParamsText(getParams().toJson().toString(4))
}
