package dev.artplus.mobile.ui.pages.tabs

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import dev.artplus.mobile.MainActivity
import dev.artplus.mobile.PREFS_NAME
import dev.artplus.mobile.data.AppEntry
import dev.artplus.mobile.state.persistUiState
import dev.artplus.mobile.state.setStatus
import dev.artplus.mobile.ui.cards.AdvancedSettingsCategory
import dev.artplus.mobile.ui.cards.AdvancedSettingsTab
import dev.artplus.mobile.ui.cards.SegmentedControl
import dev.artplus.mobile.ui.pages.PagerShellPage
import dev.artplus.mobile.ui.components.sectionItem
import dev.artplus.mobile.ui.pages.params.paramsApplyTuningParams
import dev.artplus.mobile.ui.pages.params.paramsSaveGptSettings
import dev.artplus.mobile.ui.pages.params.paramsSaveImageTuningSettings
import dev.artplus.mobile.ui.pages.params.paramsSaveLocalSeparationSettings
import dev.artplus.mobile.ui.pages.params.paramsUpdateBackgroundSeparationPercent
import dev.artplus.mobile.ui.pages.params.paramsUpdateEdgePolishPercent
import dev.artplus.mobile.ui.pages.params.paramsUpdateLocalWorkflowToggle
import dev.artplus.mobile.ui.pages.params.paramsUpdatePlateRemovalPercent
import dev.artplus.mobile.ui.pages.params.paramsUpdateRmbgAlphaStrengthPercent
import dev.artplus.mobile.ui.pages.params.paramsUpdateRmbgEdgeAdjustPercent
import dev.artplus.mobile.ui.pages.params.paramsUpdateRmbgEdgeFeatherPercent
import dev.artplus.mobile.ui.pages.params.paramsUpdateRmbgWeakAlphaKeepPercent
import dev.artplus.mobile.ui.pages.params.paramsUpdateShadowRemovalPercent
import dev.artplus.mobile.ui.pages.presets.saveJsonParamsFromText
import dev.artplus.mobile.ui.pages.settings.JsonSettingsEditorCard
import dev.artplus.mobile.ui.pages.settings.LocalRuleTuningCard
import dev.artplus.mobile.ui.pages.settings.LocalWorkflowPipelineCard
import dev.artplus.mobile.ui.pages.settings.RmbgTuningCard
import dev.artplus.mobile.ui.components.BarClearance

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
                    Box(modifier = Modifier.fillMaxSize().imePadding()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(scrollBehavior.nestedScrollConnection)
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(
                                top = innerPadding.calculateTopPadding() + 16.dp,
                                bottom = BarClearance,
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            // 「稳定规则 / RMBG」就是这一页的标签栏，沿用原来「可视化 / JSON」的轮廓分段样式。
                            // JSON 模式只有一个编辑器、没有可切换的分类，所以只在滑块模式出现。
                            if (genContentShell.advancedSettingsTab == AdvancedSettingsTab.Sliders) {
                                item(key = "category_tabs") {
                                    SegmentedControl(
                                        enabled = !shellState.isBusy,
                                        labels = AdvancedSettingsCategory.entries.map { it.label },
                                        selectedIndex = AdvancedSettingsCategory.entries
                                            .indexOf(genContentShell.advancedSettingsCategory)
                                            .coerceAtLeast(0),
                                        onSelected = { index ->
                                            mainViewModel.updateShell { v ->
                                                v.copy(advancedSettingsCategory = AdvancedSettingsCategory.entries[index])
                                            }
                                            persistUiState()
                                        },
                                    )
                                }
                            }
                            when (genContentShell.advancedSettingsTab) {
                                AdvancedSettingsTab.Sliders -> when (genContentShell.advancedSettingsCategory) {
                                    AdvancedSettingsCategory.Local -> {
                                        sectionItem(key = "local_rule", title = "本地分离") { run {
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
                                        sectionItem(key = "local_pipeline", title = "处理流程") { run {
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
                                        sectionItem(key = "rmbg_tuning", title = "RMBG 抠图") { run {
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
                                    sectionItem(key = "json_editor", title = "JSON 参数") {
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
