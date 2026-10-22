package dev.artplus.mobile

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Process
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import io.github.suqi8.coui.kmp.blur.LocalBackdrop
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.blur.rememberLayerBackdrop
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import java.io.File
import io.github.suqi8.coui.kmp.basic.Scaffold
import io.github.suqi8.coui.kmp.basic.TopAppBar
import io.github.suqi8.coui.kmp.basic.TopAppBarDefaults
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.ChevronBackward

@Composable
internal fun MainActivity.AppPickerPage(
    pageBackground: Color,
    filteredApps: List<AppEntry>,
    scopeCount: Int,
    generatedCount: Int,
    ungeneratedCount: Int,
) {
    // 热修复：选择器渲染位订阅 picker/shell（StateFlow .value 裸读不触发重组，
    // 行高亮 AppRow selected/multi/generated/isBusy 全部 stale，点行写成功但不变色）。
    // 回调内事件时读 .value 仍合法，此处只修组合期渲染读。
    val pickerState by mainViewModel.picker.collectAsState()
    val shellState by mainViewModel.shell.collectAsState()
    val scrollBehavior = rememberLinkedTopAppBarScrollBehavior()

    // Backdrop for the ColorOS app bar material: the list only, never the bar itself.
    val appBarBackdrop = rememberLayerBackdrop {
        drawRect(pageBackground)
        drawContent()
    }
    val contentTopInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
        TopAppBarDefaults.ExpandedHeight
    Scaffold(
        containerColor = pageBackground,
        topBar = {
            CompositionLocalProvider(LocalBackdrop provides appBarBackdrop) {
                TopAppBar(
                    title = "选择 APK",
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        TitleBarIconButton(
                            icon = COUIIcons.ChevronBackward,
                            contentDescription = "返回",
                            enabled = !shellState.isBusy,
                            onClick = { mainViewModel.updateShell { it -> it.copy(currentPage = (AppPage.Home)) } },
                        )
                    },
                    gradientGlass = true,
                    showDivider = false,
                )
            }
        },

    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(appBarBackdrop)
                .background(pageBackground)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .imePadding()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = contentTopInset + 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                run {
    AppPickerControlsCard(
                filteredCount = ((filteredApps).size),
                totalCount = (scopeCount),
                generatedCount = (generatedCount),
                ungeneratedCount = (ungeneratedCount),
                multiCount = pickerState.multiSelectedPackageNames.size,
                isScanning = pickerState.isScanningGeneratedPackages,
                scanFailed = pickerState.generatedScanFailed,
                isBusy = shellState.isBusy,
                hasApps = apps.isNotEmpty(),
                showSystemApps = pickerState.showSystemApps,
                generatedFilter = pickerState.generatedFilter,
                queryText = pickerState.queryText,
                onRefreshGenerated = { run {
        mainViewModel.refreshGeneratedPackagesAsync(
                    entries = (apps.toList()),
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                )
    } },
                onReloadApps = { run {
        mainViewModel.requestAppLoad(
                    refreshGenerated = (false),
                    pm = packageManager,
                    iconCache = MainActivity.appIconCache,
                    cacheSize = ICON_CACHE_SIZE,
                    preloadCount = PRELOAD_ICON_COUNT,
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    markLoad = { didRequestAppLoad = true },
                    onRefreshPermissions = { run {
            pickerRefreshPermissionState(
                        checkQueryPermission = { pickerCheckQueryPermission(packageManager, packageName) },
                        hasUsage = {  -> run {
                pickerHasUsageAccess(
                            appOps = getSystemService(AppOpsManager::class.java),
                            uid = Process.myUid(),
                            packageName = packageName,
                        )
            } },
                        onResult = { queryGranted, usageGranted ->
                            mainViewModel.updatePicker { it -> it.copy(packageListPermissionGranted = (queryGranted)) }
                            mainViewModel.updatePicker { it -> it.copy(usageAccessGranted = (usageGranted)) }
                        },
                    )
        } },
                    applyEntries = { loaded ->
                        androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
                            apps.clear()
                            apps.addAll(loaded)
                        }
                    },
                )
    } },
                onToggleSystemApps = {
                    mainViewModel.updatePicker { it -> it.copy(showSystemApps = !it.showSystemApps) }
                    run {
        persistUiState()
    }
                },
                onFilterSelected = {
                    mainViewModel.updatePicker { v -> v.copy(generatedFilter = (it)) }
                    mainViewModel.updatePicker { it -> it.copy(queryText = ("")) }
                    run {
        persistUiState()
    }
                },
                onQueryChange = {
                    mainViewModel.updatePicker { v -> v.copy(queryText = (it)) }
                    run {
        persistUiState()
    }
                },
                onClearQuery = {
                    mainViewModel.updatePicker { it -> it.copy(queryText = ("")) }
                    run {
        persistUiState()
    }
                },
                multiSelectContent = { run {

                val filteredPackageNames = remember(((filteredApps))) { ((filteredApps)).map { it.packageName }.toSet() }
                AppMultiSelectActions(
                    selectedCount = pickerState.multiSelectedPackageNames.size,
                    hasFiltered = filteredPackageNames.isNotEmpty(),
                    allFilteredSelected = pickerAllFilteredSelected(filteredPackageNames, pickerState.multiSelectedPackageNames),
                    isBusy = shellState.isBusy,
                    onToggleFiltered = {
                        val allSelected = pickerAllFilteredSelected(filteredPackageNames, mainViewModel.picker.value.multiSelectedPackageNames)
                        mainViewModel.updatePicker { it -> it.copy(multiSelectedPackageNames = (if (allSelected) {
                            mainViewModel.picker.value.multiSelectedPackageNames - filteredPackageNames
                        } else {
                            mainViewModel.picker.value.multiSelectedPackageNames + filteredPackageNames
                        })) }
                    },
                    onClear = { mainViewModel.updatePicker { it -> it.copy(multiSelectedPackageNames = (emptySet())) } },
                    onAddGlass = { run {
            mainViewModel.addLiquidGlassToMultiSelectedGenerated(
                        onRequestConfirm = { title, message, confirmLabel, onConfirm ->
                            run {

                        mainViewModel.updateConfirm { it -> it.copy(pendingServiceConfirm = (ServiceConfirmRequest(
                            title = (title),
                            message = (message),
                            confirmLabel = (confirmLabel),
                            onConfirm = (onConfirm),
                        ))) }
            }
                        },
                        onExecute = { pkgs -> run {
                mainViewModel.executeAddLiquidGlassToMultiSelectedGenerated(
                            packageNames = (pkgs),
                            selectedAtStart = mainViewModel.picker.value.selectedPackageName,
                            resolvePackageDir = { __a0: String -> run {
                    existingGeneratedPackageDir(
                                packageName = __a0,
                                previewDirPath = mainViewModel.previewSession.value.previewDirPath,
                                previewPackageName = mainViewModel.previewSession.value.previewPackageName,
                                externalArtPlusDir = getExternalFilesDir("ArtPlus"),
                                filesDir = filesDir,
                                appUid = applicationInfo.uid,
                            )
                } },
                            applyGlass = { __a0: File -> run {

                            val params = mainViewModel.params.value
                            applyLiquidGlassToGeneratedPackage(
                                dir = __a0,
                                nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
                                liquidGlassEnabled = params.liquidGlassEnabled,
                                liquidGlassBackgroundMistAlpha = params.liquidGlassBackgroundMistAlpha,
                                liquidGlassTopAlpha = params.liquidGlassTopAlpha,
                                liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
                                liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
                                liquidGlassOuterWidth = params.liquidGlassOuterWidth,
                                liquidGlassRadius = params.liquidGlassRadius,
                                liquidGlassSubjectScalePercent = params.liquidGlassSubjectScalePercent,
                                liquidGlassSubjectShadowAlpha = params.liquidGlassSubjectShadowAlpha,
                                liquidGlassSubjectOutlineWidth = params.liquidGlassSubjectOutlineWidth,
                                liquidGlassSubjectInnerOutlineWidth = params.liquidGlassSubjectInnerOutlineWidth,
                                liquidGlassSubjectOpacityPercent = params.liquidGlassSubjectOpacityPercent,
                                foregroundShadowLevel = params.foregroundShadowLevel,
                                monochromeThemeScale = params.monochromeThemeScale,
                            )
                } },
                            installGlass = ::installLiquidGlassFilesWithRoot,
                            buildSession = ::buildGeneratedPackageSession,
                            persistMany = { combined ->
                                updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), combined)
                            },
                            onSaveUiState = {  -> run {
                    persistUiState()
                } },
                        )
            } },
                    )
        } },
                    onApplyPreset = { run {
            mainViewModel.applyCurrentPresetBatch(
                        store = presetStore,
                        onRequestConfirm = { title, message, confirmLabel, onConfirm ->
                            run {

                        mainViewModel.updateConfirm { it -> it.copy(pendingServiceConfirm = (ServiceConfirmRequest(
                            title = (title),
                            message = (message),
                            confirmLabel = (confirmLabel),
                            onConfirm = (onConfirm),
                        ))) }
            }
                        },
                        onExecutePreset = { p, pkgs -> run {
                mainViewModel.executeApplyPresetToSelectedApps(
                            preset = (p),
                            batchPackageNames = (pkgs),
                            beforeParams = run {
                    mainViewModel.params.value
                },
                            store = presetStore,
                            selectedAtStart = mainViewModel.picker.value.selectedPackageName,
                            apps = apps.toList(),
                            onApplyPresetParams = { merged -> run {
                    paramsApplyTuningParams(
                                params = (merged),
                                rebuildCandidates = (false),
                                persist = (true),
                                captureUndo = (true),
                                refreshPreview = (true),
                                getBefore = { run {
                        mainViewModel.params.value
                    } },
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
                                onSaveLiquidGlass = { run {
                        paramsSaveLiquidGlassSettings(
                                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                                    getParams = { mainViewModel.params.value },
                                    getBottomBarEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarEnabled },
                                    getBottomBarBlurEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarBlurEnabled },
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
                                onSaveUi = { run {
                        persistUiState()
                    } },
                                isBusy = { mainViewModel.shell.value.isBusy },
                                getSession = { mainViewModel.previewSession.value.activeGenerationSession },
                                onRefresh = { rebuild -> run {
                        homeRefreshActivePreviewOutputs(
                                    currentSession = mainViewModel.previewSession.value.activeGenerationSession,
                                    rebuildLocalCandidates = (rebuild),
                                    retargetFrom = (null),
                                    app = mainViewModel.previewSession.value.activeGenerationSession?.let { s -> apps.firstOrNull { it.packageName == s.packageName } },
                                    currentSelections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
                                    scope = previewWorkerScope,
                                    getJob = { previewOutputJob },
                                    setJob = { previewOutputJob = it },
                                    incRevision = { ++previewOutputRevision },
                                    getRevision = { previewOutputRevision },
                                    setRefreshing = { mainViewModel.updatePreviewSession { v -> v.copy(isPreviewOutputRefreshing = (it)) } },
                                    rebuildDebounceMs = PREVIEW_REBUILD_DEBOUNCE_MS,
                                    outputDebounceMs = PREVIEW_OUTPUT_DEBOUNCE_MS,
                                    tuning = run {
                            mainViewModel.params.value
                        },
                                    onRebuild = { session, app, tuning -> rebuildLocalSession(session, app, packageManager, tuning) },
                                    defaultLocal = { auto -> run {
                            run {
                                when ((LocalSeparationMode.Auto)) {
                                            LocalSeparationMode.Original -> PreviewChoice.Original
                                            LocalSeparationMode.Plate -> PreviewChoice.Full
                                            LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                                            LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                                            LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                                            LocalSeparationMode.Auto -> ((auto))
                                            LocalSeparationMode.Full -> PreviewChoice.Full
                                        }
                            }
                        } },
                                    normalize = { session, selections -> normalizePreviewSelections(session, selections) },
                                    onWrite = { session, selections -> run {

                                    val params = mainViewModel.params.value
                                    writePackageOutputs(
                                        session = (session),
                                        selections = (selections),
                                        edgePolishPercent = params.edgePolishPercent,
                                        foregroundSubjectPercent = params.foregroundSubjectPercent,
                                        rmbgTunedForeground = { __a0: IconCandidate -> run {

                                        val params = mainViewModel.params.value
                                        return@run rmbgTunedForegroundRaw(
                                            candidate = __a0,
                                            rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
                                            rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
                                            rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
                                            rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
                                        )
                            } },
                                        liquidGlassEnabled = params.liquidGlassEnabled,
                                        liquidGlassBackgroundMistAlpha = params.liquidGlassBackgroundMistAlpha,
                                        liquidGlassTopAlpha = params.liquidGlassTopAlpha,
                                        liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
                                        liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
                                        liquidGlassOuterWidth = params.liquidGlassOuterWidth,
                                        liquidGlassRadius = params.liquidGlassRadius,
                                        liquidGlassSubjectScalePercent = params.liquidGlassSubjectScalePercent,
                                        liquidGlassSubjectShadowAlpha = params.liquidGlassSubjectShadowAlpha,
                                        liquidGlassSubjectOutlineWidth = params.liquidGlassSubjectOutlineWidth,
                                        liquidGlassSubjectInnerOutlineWidth = params.liquidGlassSubjectInnerOutlineWidth,
                                        liquidGlassSubjectOpacityPercent = params.liquidGlassSubjectOpacityPercent,
                                        foregroundShadowLevel = params.foregroundShadowLevel,
                                        monochromeThemeScale = params.monochromeThemeScale,
                                        nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
                                    )
                        } },
                                    onCommit = { session, selections ->
                                        mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                                        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                                        mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                                        run {
                            persistUiState()
                        }
                                    },
                                    onStatus = { run {

                                    pickerPostStatus((it)) { runOnUiThread { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } } }
                        } },
                                )
                    } },
                            )
                } },
                            generatePackage = { app -> run {

                            val icon = (app).applicationInfo.loadIcon(packageManager)
                            return@run generateArtPlusPackage(
                                app = (app),
                                useGpt = (false),
                                localModeOverride = (null),
                                params = mainViewModel.params.value,
                                externalArtPlusDir = getExternalFilesDir("ArtPlus"),
                                filesDir = filesDir,
                                icon = icon,
                                gptModelId = mainViewModel.gptRmbgSettings.value.gptModelId,
                                gptBaseUrl = mainViewModel.gptRmbgSettings.value.gptBaseUrl,
                                gptApiKey = mainViewModel.gptRmbgSettings.value.gptApiKey,
                                isDebug = run {
                        (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
                    },
                                onStatus = { __a0: String -> run {

                                pickerPostStatus(__a0) { runOnUiThread { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } } }
                    } },
                                defaultChoiceForMode = { __a0: LocalSeparationMode, __a1: PreviewChoice -> run {
                        when (__a0) {
                                    LocalSeparationMode.Original -> PreviewChoice.Original
                                    LocalSeparationMode.Plate -> PreviewChoice.Full
                                    LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                                    LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                                    LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                                    LocalSeparationMode.Auto -> __a1
                                    LocalSeparationMode.Full -> PreviewChoice.Full
                                }
                    } },
                                rmbgTunedForeground = { __a0: IconCandidate -> run {

                                val params = mainViewModel.params.value
                                return@run rmbgTunedForegroundRaw(
                                    candidate = __a0,
                                    rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
                                    rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
                                    rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
                                    rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
                                )
                    } },
                            )
                } },
                            persistGenerated = { combined ->
                                updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), combined)
                            },
                            onSaveUiState = {  -> run {
                    persistUiState()
                } },
                        )
            } },
                        onExecuteCurrent = { pkgs -> run {
                mainViewModel.executeApplyCurrentBatch(
                            batchPackageNames = (pkgs),
                            selectedAtStart = mainViewModel.picker.value.selectedPackageName,
                            apps = apps.toList(),
                            generatePackage = { app -> run {

                            val icon = (app).applicationInfo.loadIcon(packageManager)
                            return@run generateArtPlusPackage(
                                app = (app),
                                useGpt = (false),
                                localModeOverride = (null),
                                params = mainViewModel.params.value,
                                externalArtPlusDir = getExternalFilesDir("ArtPlus"),
                                filesDir = filesDir,
                                icon = icon,
                                gptModelId = mainViewModel.gptRmbgSettings.value.gptModelId,
                                gptBaseUrl = mainViewModel.gptRmbgSettings.value.gptBaseUrl,
                                gptApiKey = mainViewModel.gptRmbgSettings.value.gptApiKey,
                                isDebug = run {
                        (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
                    },
                                onStatus = { __a0: String -> run {

                                pickerPostStatus(__a0) { runOnUiThread { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } } }
                    } },
                                defaultChoiceForMode = { __a0: LocalSeparationMode, __a1: PreviewChoice -> run {
                        when (__a0) {
                                    LocalSeparationMode.Original -> PreviewChoice.Original
                                    LocalSeparationMode.Plate -> PreviewChoice.Full
                                    LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                                    LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                                    LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                                    LocalSeparationMode.Auto -> __a1
                                    LocalSeparationMode.Full -> PreviewChoice.Full
                                }
                    } },
                                rmbgTunedForeground = { __a0: IconCandidate -> run {

                                val params = mainViewModel.params.value
                                return@run rmbgTunedForegroundRaw(
                                    candidate = __a0,
                                    rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
                                    rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
                                    rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
                                    rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
                                )
                    } },
                            )
                } },
                            persistGenerated = { combined ->
                                updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), combined)
                            },
                            onSaveUiState = {  -> run {
                    persistUiState()
                } },
                        )
            } },
                    )
        } },
                )
    } },
            )
}
            }
            if (filteredApps.isEmpty()) {
                item {
                    run {

            // 热修复：pickerState/shellState 已提升至 AppPickerPage 顶层，此处复用（删重复订阅）。
            // Slice 3.1: Activity侧collect读VM单源；写经薄wrapper（重构期间保留）。
            EmptyAppListCard(
                queryText = pickerState.queryText,
                showSystemApps = pickerState.showSystemApps,
                hasHiddenSystemApps = apps.any { AppVisibility.isSystemAppFlags(it.applicationInfo.flags) && it.packageName != packageName },
                isBusy = shellState.isBusy,
                onShowSystemApps = {
                    mainViewModel.updatePicker { it -> it.copy(showSystemApps = (true)) }
                    run {
        persistUiState()
    }
                },
                onRefresh = { run {
        mainViewModel.requestAppLoad(
                    refreshGenerated = (false),
                    pm = packageManager,
                    iconCache = MainActivity.appIconCache,
                    cacheSize = ICON_CACHE_SIZE,
                    preloadCount = PRELOAD_ICON_COUNT,
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    markLoad = { didRequestAppLoad = true },
                    onRefreshPermissions = { run {
            pickerRefreshPermissionState(
                        checkQueryPermission = { pickerCheckQueryPermission(packageManager, packageName) },
                        hasUsage = {  -> run {
                pickerHasUsageAccess(
                            appOps = getSystemService(AppOpsManager::class.java),
                            uid = Process.myUid(),
                            packageName = packageName,
                        )
            } },
                        onResult = { queryGranted, usageGranted ->
                            mainViewModel.updatePicker { it -> it.copy(packageListPermissionGranted = (queryGranted)) }
                            mainViewModel.updatePicker { it -> it.copy(usageAccessGranted = (usageGranted)) }
                        },
                    )
        } },
                    applyEntries = { loaded ->
                        androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
                            apps.clear()
                            apps.addAll(loaded)
                        }
                    },
                )
    } },
            )
}
                }
            } else {
                items(
                    items = filteredApps,
                    key = { it.packageName },
                    contentType = { "app" },
                ) { entry ->
                    run {
    AppRow(
                entry = (entry),
                selected = (entry.packageName == pickerState.selectedPackageName),
                multiSelected = (entry.packageName in pickerState.multiSelectedPackageNames),
                generated = (entry.packageName in pickerState.generatedPackageNames),
                isBusy = shellState.isBusy,
                onClick = ({
                                run {

                val revision = ++generatedPreviewRestoreRevision
                val localDir = run {
            artPlusPackageDir(
                        packageName = ((entry).packageName),
                        externalArtPlusDir = getExternalFilesDir("ArtPlus"),
                        filesDir = filesDir,
                    )
        }
                val known = (entry).packageName in mainViewModel.picker.value.generatedPackageNames || hasGeneratedPackageBaseAssets(localDir)
                homeSelectAppAndRestore(
                    entry = (entry),
                    revision = revision,
                    isBusy = mainViewModel.shell.value.isBusy,
                    knownGenerated = known,
                    getSelected = { mainViewModel.picker.value.selectedPackageName },
                    getRevision = { generatedPreviewRestoreRevision },
                    onResetSelection = { pkg ->
                        mainViewModel.updatePicker { it -> it.copy(selectedPackageName = (pkg)) }
                        mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (null)) }
                        mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) }
                        mainViewModel.updatePreviewSession { it -> it.copy(previewPackageName = (null)) }
                        mainViewModel.updatePreviewSession { it -> it.copy(previewDirPath = (null)) }
                        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (PreviewSelections.default(PreviewChoice.Original)).normalLight.name, previewNormalDark = (PreviewSelections.default(PreviewChoice.Original)).normalDark.name, previewMonochromeLight = (PreviewSelections.default(PreviewChoice.Original)).monochromeLight.name, previewMonochromeDark = (PreviewSelections.default(PreviewChoice.Original)).monochromeDark.name) }
                        mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                    },
                    onStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                    onSaveUi = { run {
            persistUiState()
        } },
                    onClearRmbg = { run {

                    if (mainViewModel.previewSession.value.isGeneratingRmbgCandidate) {
                        return@run
                    }
                    mainViewModel.updatePreviewSession { it -> it.copy(lastRmbgCandidateError = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidatePackageName = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateMode = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateStatusText = ("")) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailurePackageName = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailureMode = (null)) }
        } },
                    onLaunch = { name, block -> run {

                    mainViewModel.launchUiFriendly((name), (block))
        } },
                    onLoadDir = { run {
            existingGeneratedPackageDir(
                        packageName = ((entry).packageName),
                        previewDirPath = mainViewModel.previewSession.value.previewDirPath,
                        previewPackageName = mainViewModel.previewSession.value.previewPackageName,
                        externalArtPlusDir = getExternalFilesDir("ArtPlus"),
                        filesDir = filesDir,
                        appUid = applicationInfo.uid,
                    )
        } },
                    onUi = { block -> runOnUiThread(block) },
                    onMarkGenerated = { pkg -> mainViewModel.updatePicker { it -> it.copy(generatedPackageNames = (markPackageGenerated(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), mainViewModel.picker.value.generatedPackageNames, pkg))) } },
                    onBuildSession = { pkg, dir -> buildGeneratedPackageSession(pkg, dir) },
                    onCommitSession = { session, dir, e ->
                        mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (PreviewSelections.default(PreviewChoice.Original)).normalLight.name, previewNormalDark = (PreviewSelections.default(PreviewChoice.Original)).normalDark.name, previewMonochromeLight = (PreviewSelections.default(PreviewChoice.Original)).monochromeLight.name, previewMonochromeDark = (PreviewSelections.default(PreviewChoice.Original)).monochromeDark.name) }
                        mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) }
                        mainViewModel.updatePreviewSession { it -> it.copy(previewPackageName = (e.packageName)) }
                        mainViewModel.updatePreviewSession { it -> it.copy(previewDirPath = (dir.absolutePath)) }
                        mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                    },
                )
    }
                                mainViewModel.updateShell { it -> it.copy(currentPage = (AppPage.Home)) }
                            }),
                onToggleMultiSelect = ({ run {
        mainViewModel.toggleMultiSelectedPackage(packageName = (entry.packageName), current = mainViewModel.picker.value.multiSelectedPackageNames)
    } }),
                icon = {
                    run {
        AppIcon(
                    entry = ((entry)),
                    size = (48.dp),
                    getCached = { key -> getCachedAppIcon(MainActivity.appIconCache, key) },
                    loadIcon = { run {
            mainViewModel.loadCachedAppIconOp(
                        entry = (((entry))),
                        iconCache = MainActivity.appIconCache,
                        pm = packageManager,
                        cacheSize = ICON_CACHE_SIZE,
                    )
        } },
                )
    }
                },
            )
}
                }
            }
        }
    }
}
