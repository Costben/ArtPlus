package dev.artplus.mobile

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

/**
 * S4.x：从 HomePage 的 `when (page)` 分支原样搬出的 tab 页（纯移动，key/remember 原样保留）。
 * 状态仍由 mainViewModel 单源提供，本页只订阅自己用到的 flow。
 */
@Composable
internal fun MainActivity.ParamsTabPage(
    pageBackground: Color,
    selectedApp: AppEntry?,
    launcherCount: Int,
    totalCount: Int,
    generatedCount: Int,
) {
    val shellState by mainViewModel.shell.collectAsState()
    val previewSessionState by mainViewModel.previewSession.collectAsState()
    val paramsState by mainViewModel.params.collectAsState()

PagerShellPage(
                    title = "生成参数",
                    // 热修复2：翻页壳渲染位经已订阅 previewSessionState 读取。
                    showPreviewStrip = previewSessionState.previewStripEnabled,
                ) { innerPadding, scrollBehavior ->
                    // Phase 5 回归修复（FAIL-1）：内容分支须读已订阅的 shellState，
                    // 裸读 shell.value 不触发重组（高亮动内容不动）。
                    val genContentShell by mainViewModel.shell.collectAsState()
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .imePadding()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding() + 16.dp,
                            bottom = 88.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item(key = "gen_nav") {
                            run {

            val shellState by mainViewModel.shell.collectAsState()
            // Slice 3.1: Activity侧collect读VM单源；写经薄wrapper（重构期间保留）。
            GenerationNavCard(
                isBusy = shellState.isBusy,
                advancedSettingsTab = shellState.advancedSettingsTab,
                advancedSettingsCategory = shellState.advancedSettingsCategory,
                onTabSelected = {
                    mainViewModel.updateShell { v -> v.copy(advancedSettingsTab = (it)) }
                    persistUiState()
                },
                onRequestSavePreset = {
                    mainViewModel.updatePresetUi { it -> it.copy(presetSaveName = ("")) }
                    mainViewModel.updatePresetUi { it -> it.copy(presetSaveDialogVisible = (true)) }
                },
                onCategorySelected = {
                    mainViewModel.updateShell { v -> v.copy(advancedSettingsCategory = (it)) }
                    persistUiState()
                },
            )
}
                        }
                        when (genContentShell.advancedSettingsTab) {
                            AdvancedSettingsTab.Sliders -> when (genContentShell.advancedSettingsCategory) {
                                AdvancedSettingsCategory.LiquidGlass -> {
                                    item(key = "glass_toggle") { run {
    LiquidGlassToggleCard(
                enabled = paramsState.liquidGlassEnabled,
                isBusy = shellState.isBusy,
                onCheckedChange = { run {
        paramsUpdateLiquidGlassEnabled(
                    enabled = (it),
                    getParams = { mainViewModel.params.value },
                    updateLive = mainViewModel::updateLive,
                    onSave = { tuningActions.save() },
                    setStatusText = { setStatus((it)) },
                    onRefresh = tuningActions.refresh,
                )
    } },
            )
} }
                                    item(key = "glass_surface") { run {
    LiquidGlassSurfaceCard(
                tuningState = paramsState,
                isBusy = shellState.isBusy,
                draftRadiusText = draftLiquidGlassRadiusText,
                onDraftRadiusChange = { draftLiquidGlassRadiusText = it },
                onSaveRadius = { run {
        paramsUpdateLiquidGlassRadius(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    setDraftText = { draftLiquidGlassRadiusText = it },
                    onSave = { tuningActions.save() },
                    setStatusText = { setStatus((it)) },
                    onRefresh = tuningActions.refresh,
                )
    } },
                draftOuterWidthText = draftLiquidGlassOuterWidthText,
                onDraftOuterWidthChange = { draftLiquidGlassOuterWidthText = it },
                onSaveOuterWidth = { run {
        paramsUpdateLiquidGlassOuterWidth(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    setDraftText = { draftLiquidGlassOuterWidthText = it },
                    onSave = { tuningActions.save() },
                    setStatusText = { setStatus((it)) },
                    onRefresh = tuningActions.refresh,
                )
    } },
                draftTopAlphaText = draftLiquidGlassTopAlphaText,
                onDraftTopAlphaChange = { draftLiquidGlassTopAlphaText = it },
                onSaveTopAlpha = { run {
        paramsUpdateLiquidGlassTopAlpha(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    setDraftText = { draftLiquidGlassTopAlphaText = it },
                    onSave = { tuningActions.save() },
                    setStatusText = { setStatus((it)) },
                    onRefresh = tuningActions.refresh,
                )
    } },
                draftBottomAlphaText = draftLiquidGlassBottomAlphaText,
                onDraftBottomAlphaChange = { draftLiquidGlassBottomAlphaText = it },
                onSaveBottomAlpha = { run {
        paramsUpdateLiquidGlassBottomAlpha(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    setDraftText = { draftLiquidGlassBottomAlphaText = it },
                    onSave = { tuningActions.save() },
                    setStatusText = { setStatus((it)) },
                    onRefresh = tuningActions.refresh,
                )
    } },
                draftBackgroundMistAlphaText = draftLiquidGlassBackgroundMistAlphaText,
                onDraftBackgroundMistAlphaChange = { draftLiquidGlassBackgroundMistAlphaText = it },
                onSaveBackgroundMistAlpha = { run {
        paramsUpdateLiquidGlassBackgroundMistAlpha(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    setDraftText = { draftLiquidGlassBackgroundMistAlphaText = it },
                    onSave = { tuningActions.save() },
                    setStatusText = { setStatus((it)) },
                    onRefresh = tuningActions.refresh,
                )
    } },
                draftBottomDarkAlphaText = draftLiquidGlassBottomDarkAlphaText,
                onDraftBottomDarkAlphaChange = { draftLiquidGlassBottomDarkAlphaText = it },
                onSaveBottomDarkAlpha = { run {
        paramsUpdateLiquidGlassBottomDarkAlpha(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    setDraftText = { draftLiquidGlassBottomDarkAlphaText = it },
                    onSave = { tuningActions.save() },
                    setStatusText = { setStatus((it)) },
                    onRefresh = tuningActions.refresh,
                )
    } },
            )
} }
                                    item(key = "glass_subject") { run {
    LiquidGlassSubjectCard(
                tuningState = paramsState,
                isBusy = shellState.isBusy,
                draftSubjectScaleText = draftLiquidGlassSubjectScaleText,
                onDraftSubjectScaleChange = { draftLiquidGlassSubjectScaleText = it },
                onSaveSubjectScale = { run {
        paramsUpdateLiquidGlassSubjectScalePercent(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    setDraftText = { draftLiquidGlassSubjectScaleText = it },
                    onSave = { tuningActions.save() },
                    setStatusText = { setStatus((it)) },
                    onRefresh = tuningActions.refresh,
                )
    } },
                draftSubjectOutlineWidthText = draftLiquidGlassSubjectOutlineWidthText,
                onDraftSubjectOutlineWidthChange = { draftLiquidGlassSubjectOutlineWidthText = it },
                onSaveSubjectOutlineWidth = { run {
        paramsUpdateLiquidGlassSubjectOutlineWidth(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    setDraftText = { draftLiquidGlassSubjectOutlineWidthText = it },
                    onSave = { tuningActions.save() },
                    setStatusText = { setStatus((it)) },
                    onRefresh = tuningActions.refresh,
                )
    } },
                draftSubjectInnerOutlineWidthText = draftLiquidGlassSubjectInnerOutlineWidthText,
                onDraftSubjectInnerOutlineWidthChange = { draftLiquidGlassSubjectInnerOutlineWidthText = it },
                onSaveSubjectInnerOutlineWidth = { run {
        paramsUpdateLiquidGlassSubjectInnerOutlineWidth(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    setDraftText = { draftLiquidGlassSubjectInnerOutlineWidthText = it },
                    onSave = { tuningActions.save() },
                    setStatusText = { setStatus((it)) },
                    onRefresh = tuningActions.refresh,
                )
    } },
                draftSubjectShadowAlphaText = draftLiquidGlassSubjectShadowAlphaText,
                onDraftSubjectShadowAlphaChange = { draftLiquidGlassSubjectShadowAlphaText = it },
                onSaveSubjectShadowAlpha = { run {
        paramsUpdateLiquidGlassSubjectShadowAlpha(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    setDraftText = { draftLiquidGlassSubjectShadowAlphaText = it },
                    onSave = { tuningActions.save() },
                    setStatusText = { setStatus((it)) },
                    onRefresh = tuningActions.refresh,
                )
    } },
                draftSubjectOpacityText = draftLiquidGlassSubjectOpacityText,
                onDraftSubjectOpacityChange = { draftLiquidGlassSubjectOpacityText = it },
                onSaveSubjectOpacity = { run {
        paramsUpdateLiquidGlassSubjectOpacityPercent(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    setDraftText = { draftLiquidGlassSubjectOpacityText = it },
                    onSave = { tuningActions.save() },
                    setStatusText = { setStatus((it)) },
                    onRefresh = tuningActions.refresh,
                )
    } },
            )
} }
                                }
                                AdvancedSettingsCategory.Local -> {
                                    item(key = "local_rule") { run {
    LocalRuleTuningCard(
                tuningState = paramsState,
                isBusy = shellState.isBusy,
                draftBackgroundSeparationText = draftBackgroundSeparationText,
                onDraftBackgroundSeparationChange = { draftBackgroundSeparationText = it },
                onSaveBackgroundSeparation = { run {
        paramsUpdateBackgroundSeparationPercent(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    getParams = { mainViewModel.params.value },
                    setDraftText = { draftBackgroundSeparationText = it },
                    onSave = { run {
            paramsSaveImageTuningSettings(
                        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                        getParams = { mainViewModel.params.value },
                    )
        } },
                    onRefresh = tuningActions.refresh,
                )
    } },
                draftPlateRemovalText = draftPlateRemovalText,
                onDraftPlateRemovalChange = { draftPlateRemovalText = it },
                onSavePlateRemoval = { run {
        paramsUpdatePlateRemovalPercent(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    getParams = { mainViewModel.params.value },
                    setDraftText = { draftPlateRemovalText = it },
                    onSave = { run {
            paramsSaveImageTuningSettings(
                        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                        getParams = { mainViewModel.params.value },
                    )
        } },
                    onRefresh = tuningActions.refresh,
                )
    } },
                draftShadowRemovalText = draftShadowRemovalText,
                onDraftShadowRemovalChange = { draftShadowRemovalText = it },
                onSaveShadowRemoval = { run {
        paramsUpdateShadowRemovalPercent(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    getParams = { mainViewModel.params.value },
                    setDraftText = { draftShadowRemovalText = it },
                    onSave = { run {
            paramsSaveImageTuningSettings(
                        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                        getParams = { mainViewModel.params.value },
                    )
        } },
                    onRefresh = tuningActions.refresh,
                )
    } },
                draftEdgePolishText = draftEdgePolishText,
                onDraftEdgePolishChange = { draftEdgePolishText = it },
                onSaveEdgePolish = { run {
        paramsUpdateEdgePolishPercent(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    getParams = { mainViewModel.params.value },
                    setDraftText = { draftEdgePolishText = it },
                    onSave = { run {
            paramsSaveImageTuningSettings(
                        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                        getParams = { mainViewModel.params.value },
                    )
        } },
                    onRefresh = tuningActions.refresh,
                )
    } },
            )
} }
                                    item(key = "local_pipeline") { run {
    LocalWorkflowPipelineCard(
                tuningState = paramsState,
                isBusy = shellState.isBusy,
                onToggle = { key, enabled ->
        paramsUpdateLocalWorkflowToggle(
                    name = (key),
                    enabled = (enabled),
                    getParams = { mainViewModel.params.value },
                    updateLive = mainViewModel::updateLive,
                    onSaveImageTuning = { run {
            paramsSaveImageTuningSettings(
                        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                        getParams = { mainViewModel.params.value },
                    )
        } },
                    setStatusText = { setStatus((it)) },
                    onRefresh = tuningActions.refresh,
                )
     },
            )
} }
                                }
                                AdvancedSettingsCategory.Rmbg -> {
                                    item(key = "rmbg_tuning") { run {
    RmbgTuningCard(
                tuningState = paramsState,
                isBusy = shellState.isBusy,
                draftAlphaStrengthText = draftRmbgAlphaStrengthText,
                onDraftAlphaStrengthChange = { draftRmbgAlphaStrengthText = it },
                onSaveAlphaStrength = { run {
        paramsUpdateRmbgAlphaStrengthPercent(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    getParams = { mainViewModel.params.value },
                    setDraftText = { draftRmbgAlphaStrengthText = it },
                    onSave = { run {
            paramsSaveImageTuningSettings(
                        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                        getParams = { mainViewModel.params.value },
                    )
        } },
                    onRefresh = tuningActions.refresh,
                )
    } },
                draftEdgeFeatherText = draftRmbgEdgeFeatherText,
                onDraftEdgeFeatherChange = { draftRmbgEdgeFeatherText = it },
                onSaveEdgeFeather = { run {
        paramsUpdateRmbgEdgeFeatherPercent(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    getParams = { mainViewModel.params.value },
                    setDraftText = { draftRmbgEdgeFeatherText = it },
                    onSave = { run {
            paramsSaveImageTuningSettings(
                        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                        getParams = { mainViewModel.params.value },
                    )
        } },
                    onRefresh = tuningActions.refresh,
                )
    } },
                draftEdgeAdjustText = draftRmbgEdgeAdjustText,
                onDraftEdgeAdjustChange = { draftRmbgEdgeAdjustText = it },
                onSaveEdgeAdjust = { run {
        paramsUpdateRmbgEdgeAdjustPercent(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    getParams = { mainViewModel.params.value },
                    setDraftText = { draftRmbgEdgeAdjustText = it },
                    onSave = { run {
            paramsSaveImageTuningSettings(
                        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                        getParams = { mainViewModel.params.value },
                    )
        } },
                    onRefresh = tuningActions.refresh,
                )
    } },
                draftWeakAlphaKeepText = draftRmbgWeakAlphaKeepText,
                onDraftWeakAlphaKeepChange = { draftRmbgWeakAlphaKeepText = it },
                onSaveWeakAlphaKeep = { run {
        paramsUpdateRmbgWeakAlphaKeepPercent(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    getParams = { mainViewModel.params.value },
                    setDraftText = { draftRmbgWeakAlphaKeepText = it },
                    onSave = { run {
            paramsSaveImageTuningSettings(
                        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                        getParams = { mainViewModel.params.value },
                    )
        } },
                    onRefresh = tuningActions.refresh,
                )
    } },
            )
} }
                                }
                            }
                            AdvancedSettingsTab.Json -> {
                                item(key = "json_editor") {
                                    run {
    JsonSettingsEditorCard(
                currentParams = remember(paramsState) { paramsState },
                draftText = draftJsonParamsText,
                onDraftChange = { draftJsonParamsText = it },
                onSave = { run {
        saveJsonParamsFromText(
                    text = (it),
                    current = mainViewModel.params.value,
                    onApplyParams = { run {
            paramsApplyTuningParams(
                        params = (it),
                        rebuildCandidates = (true),
                        persist = (true),
                        captureUndo = (true),
                        refreshPreview = (true),
                        getBefore = { mainViewModel.params.value },
                        onCaptureUndo = { mainViewModel.updatePreviewSession { v -> v.copy(lastParamsSnapshot = (it)) } },
                        onParamsApplied = { before, applied, capture -> mainViewModel.onParamsApplied(before = before, applied = applied, captureUndo = capture) },
                        setDraftForegroundSubjectPercentText = { draftForegroundSubjectPercentText = it },
                        setDraftForegroundShadowLevelText = { draftForegroundShadowLevelText = it },
                        setDraftMonochromeThemeScaleText = { draftMonochromeThemeScaleText = it },
                        setDraftBackgroundSeparationText = { draftBackgroundSeparationText = it },
                        setDraftPlateRemovalText = { draftPlateRemovalText = it },
                        setDraftShadowRemovalText = { draftShadowRemovalText = it },
                        setDraftEdgePolishText = { draftEdgePolishText = it },
                        setDraftRmbgAlphaStrengthText = { draftRmbgAlphaStrengthText = it },
                        setDraftRmbgEdgeFeatherText = { draftRmbgEdgeFeatherText = it },
                        setDraftRmbgEdgeAdjustText = { draftRmbgEdgeAdjustText = it },
                        setDraftRmbgWeakAlphaKeepText = { draftRmbgWeakAlphaKeepText = it },
                        setDraftLiquidGlassRadiusText = { draftLiquidGlassRadiusText = it },
                        setDraftLiquidGlassOuterWidthText = { draftLiquidGlassOuterWidthText = it },
                        setDraftLiquidGlassTopAlphaText = { draftLiquidGlassTopAlphaText = it },
                        setDraftLiquidGlassBottomAlphaText = { draftLiquidGlassBottomAlphaText = it },
                        setDraftLiquidGlassBackgroundMistAlphaText = { draftLiquidGlassBackgroundMistAlphaText = it },
                        setDraftLiquidGlassBottomDarkAlphaText = { draftLiquidGlassBottomDarkAlphaText = it },
                        setDraftLiquidGlassSubjectScaleText = { draftLiquidGlassSubjectScaleText = it },
                        setDraftLiquidGlassSubjectOutlineWidthText = { draftLiquidGlassSubjectOutlineWidthText = it },
                        setDraftLiquidGlassSubjectInnerOutlineWidthText = { draftLiquidGlassSubjectInnerOutlineWidthText = it },
                        setDraftLiquidGlassSubjectShadowAlphaText = { draftLiquidGlassSubjectShadowAlphaText = it },
                        setDraftLiquidGlassSubjectOpacityText = { draftLiquidGlassSubjectOpacityText = it },
                        setDraftJsonParamsText = { draftJsonParamsText = it },
                        onSaveLocalSeparation = { run {
                paramsSaveLocalSeparationSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                        )
            } },
                        onSaveImageTuning = { run {
                paramsSaveImageTuningSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                        )
            } },
                        onSaveLiquidGlass = { tuningActions.save() },
                        onSaveGpt = { run {
                paramsSaveGptSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                            getGptApiKey = { mainViewModel.gptRmbgSettings.value.gptApiKey },
                            getGptModelId = { mainViewModel.gptRmbgSettings.value.gptModelId },
                            getGptBaseUrl = { mainViewModel.gptRmbgSettings.value.gptBaseUrl },
                        )
            } },
                        onSaveUi = { persistUiState() },
                        isBusy = { mainViewModel.shell.value.isBusy },
                        getSession = { mainViewModel.previewSession.value.activeGenerationSession },
                        onRefresh = tuningActions.refresh,
                    )
        } },
                    onStatus = { setStatus((it)) },
                )
    } },
                onRestore = {
                    draftJsonParamsText = mainViewModel.params.value.toJson().toString(4)
                    setStatus(("已恢复为当前参数 JSON"))
                },
            )
}
                                }
                            }
                        }
                    }
                }

}
