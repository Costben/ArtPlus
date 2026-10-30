package dev.artplus.mobile.ui.pages.tabs

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Download
import io.github.suqi8.coui.kmp.icon.extended.Share
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dev.artplus.mobile.MainActivity
import dev.artplus.mobile.PREFS_NAME
import dev.artplus.mobile.TuningPreset
import dev.artplus.mobile.data.AppEntry
import dev.artplus.mobile.state.openBatchPreviewForPreset
import dev.artplus.mobile.state.persistUiState
import dev.artplus.mobile.state.setStatus
import dev.artplus.mobile.ui.cards.ServiceConfirmRequest
import dev.artplus.mobile.ui.components.TitleBarIconButton
import dev.artplus.mobile.ui.pages.PagerShellPage
import dev.artplus.mobile.ui.pages.params.paramsApplyTuningParams
import dev.artplus.mobile.ui.pages.params.paramsResetToDefaults
import dev.artplus.mobile.ui.pages.params.paramsSaveGptSettings
import dev.artplus.mobile.ui.pages.params.paramsSaveImageTuningSettings
import dev.artplus.mobile.ui.pages.params.paramsSaveLocalSeparationSettings
import dev.artplus.mobile.ui.pages.params.paramsUpdateBatchPreviewCount
import dev.artplus.mobile.ui.pages.presets.PresetLibraryCard
import dev.artplus.mobile.ui.pages.presets.applyPreset
import dev.artplus.mobile.ui.pages.presets.exportPresetsToClipboard
import dev.artplus.mobile.ui.pages.presets.overwritePreset
import dev.artplus.mobile.ui.pages.presets.resetToPreset
import dev.artplus.mobile.ui.pages.settings.BatchPreviewSettingsCard
import dev.artplus.mobile.ui.components.BarClearance

/**
 * S4.x：从 HomePage 的 `when (page)` 分支原样搬出的 tab 页（纯移动，key/remember 原样保留）。
 * 状态仍由 mainViewModel 单源提供，本页只订阅自己用到的 flow。
 */
@Composable
internal fun MainActivity.PresetTabPage(
    pageBackground: Color,
    selectedApp: AppEntry?,
    launcherCount: Int,
    totalCount: Int,
    generatedCount: Int,
) {
    val presetUiState by mainViewModel.presetUi.collectAsState()
    val shellState by mainViewModel.shell.collectAsState()
    val previewSessionState by mainViewModel.previewSession.collectAsState()
    val batchConfigState by mainViewModel.batchPreviewConfig.collectAsState()
    val paramsState by mainViewModel.params.collectAsState()
    // S3.6/S4.3：预设列表读盘在 effect 里做（组合期无 IO），随 presetListVersion 刷新。
    var presetsSnapshot by remember { mutableStateOf<List<TuningPreset>>(emptyList()) }
    LaunchedEffect(presetUiState.presetListVersion) {
        presetsSnapshot = withContext(Dispatchers.IO) { presetStore.all() }
    }

PagerShellPage(
                    title = "预设",
                    actions = {
                        val presetCount = presetsSnapshot.size
                        TitleBarIconButton(
                            icon = COUIIcons.Download,
                            contentDescription = "导入预设",
                            enabled = !shellState.isBusy,
                            onClick = {
                                mainViewModel.updatePresetUi { it -> it.copy(presetImportText = ("")) }
                                mainViewModel.updatePresetUi { it -> it.copy(presetImportDialogVisible = (true)) }
                            },
                        )
                        TitleBarIconButton(
                            icon = COUIIcons.Share,
                            contentDescription = "导出全部预设",
                            enabled = !shellState.isBusy && presetCount > 0,
                            onClick = { run {
    exportPresetsToClipboard(
                store = presetStore,
                clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager,
                context = this@PresetTabPage,
                onStatus = { setStatus((it)) },
            )
} },
                        )
                    },
                    showPreviewStrip = previewSessionState.previewStripEnabled,
                ) { innerPadding, scrollBehavior ->
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val topPadding = innerPadding.calculateTopPadding()
                        val bottomPadding = innerPadding.calculateBottomPadding()
                        val minContentHeight = (maxHeight - topPadding - bottomPadding - 16.dp - BarClearance + 1.dp).coerceAtLeast(0.dp)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(scrollBehavior.nestedScrollConnection)
                                .imePadding()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(
                                top = innerPadding.calculateTopPadding() + 16.dp,
                                bottom = BarClearance,
                            ),
                        ) {
                            item(key = "preset_cards") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = minContentHeight),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
    PresetLibraryCard(
                presets = presetsSnapshot,
                activePresetId = presetUiState.activePresetId,
                activePresetBaseParams = presetUiState.activePresetBaseParams,
                currentParams = remember(paramsState) { paramsState },
                searchQuery = presetUiState.presetSearchQuery,
                onSearchChange = { mainViewModel.updatePresetUi { v -> v.copy(presetSearchQuery = (it)) } },
                listExpanded = presetUiState.presetListExpanded,
                onToggleExpanded = { mainViewModel.updatePresetUi { it -> it.copy(presetListExpanded = !it.presetListExpanded) } },
                isBusy = shellState.isBusy,
                onApply = { run {
        applyPreset(
                    preset = (it),
                    isBusy = shellState.isBusy,
                    isGeneratingGptCandidate = mainViewModel.previewSession.value.isGeneratingGptCandidate,
                    isGeneratingRmbgCandidate = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
                    before = mainViewModel.params.value,
                    viewModel = mainViewModel,
                    onApplied = { p, merged, msg ->
            paramsApplyTuningParams(
                        params = (merged),
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
        
                        presetStore.activePresetId = p.id
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (p.id)) }
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetBaseParams = (p.params)) }
                        setStatus((msg))
                    },
                    onStatus = { setStatus((it)) },
                )
    } },
                onPreview = { mainViewModel.openBatchPreviewForPreset(preset = (it), filesDir = filesDir) },
                onMore = { mainViewModel.updatePresetUi { v -> v.copy(presetActionMenuTarget = (it)) } },
                onDelete = { mainViewModel.updatePresetUi { v -> v.copy(presetDeleteConfirmTarget = (it)) } },
            )
    BatchPreviewSettingsCard(
                value = batchConfigState.batchPreviewCount,
                draftText = draftBatchPreviewCountText,
                isBusy = shellState.isBusy,
                onDraftChange = { draftBatchPreviewCountText = it },
                onSave = { run {
        paramsUpdateBatchPreviewCount(
                    value = (it),
                    setValue = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(batchPreviewCount = (it)) } },
                    setDraftText = { draftBatchPreviewCountText = it },
                    onSave = { persistUiState() },
                )
    } },
            )
                                }
                            }
                        }
                    }
                }

}
