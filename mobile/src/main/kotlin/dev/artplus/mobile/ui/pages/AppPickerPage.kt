package dev.artplus.mobile.ui.pages

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Process
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import java.io.File
import io.github.suqi8.coui.kmp.basic.Scaffold
import io.github.suqi8.coui.kmp.basic.TopAppBarDefaults
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.ChevronBackward
import androidx.compose.runtime.mutableStateOf
import io.github.suqi8.coui.kmp.basic.SmallTopAppBar
import io.github.suqi8.coui.kmp.icon.extended.Delete
import io.github.suqi8.coui.kmp.icon.extended.Refresh
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.utils.COUIPopupUtils
import dev.artplus.mobile.ui.pages.picker.PickerAppListHeader
import dev.artplus.mobile.ui.pages.picker.PickerMoreBall
import dev.artplus.mobile.AppVisibility
import dev.artplus.mobile.ICON_CACHE_SIZE
import dev.artplus.mobile.MainActivity
import dev.artplus.mobile.PREFS_NAME
import dev.artplus.mobile.PRELOAD_ICON_COUNT
import dev.artplus.mobile.data.AppEntry
import dev.artplus.mobile.data.getCachedAppIcon
import dev.artplus.mobile.data.updateGeneratedPackageCache
import dev.artplus.mobile.imaging.IconCandidate
import dev.artplus.mobile.imaging.LocalSeparationMode
import dev.artplus.mobile.imaging.PreviewChoice
import dev.artplus.mobile.pipeline.rmbgTunedForegroundRaw
import dev.artplus.mobile.state.applyCurrentPresetBatch
import dev.artplus.mobile.state.executeApplyCurrentBatch
import dev.artplus.mobile.state.executeApplyPresetToSelectedApps
import dev.artplus.mobile.state.launchUiFriendly
import dev.artplus.mobile.state.loadCachedAppIconOp
import dev.artplus.mobile.state.markPackageWritten
import dev.artplus.mobile.state.persistUiState
import dev.artplus.mobile.state.refreshArtPlusModifiedPackagesAsync
import dev.artplus.mobile.state.requestAppLoad
import dev.artplus.mobile.state.setStatus
import dev.artplus.mobile.state.toggleMultiSelectedPackage
import dev.artplus.mobile.system.artPlusPackageDir
import dev.artplus.mobile.system.buildGeneratedPackageSession
import dev.artplus.mobile.system.existingGeneratedPackageDir
import dev.artplus.mobile.system.generateArtPlusPackage
import dev.artplus.mobile.system.hasGeneratedPackageBaseAssets
import dev.artplus.mobile.ui.cards.AppPage
import dev.artplus.mobile.ui.cards.PreviewSelections
import dev.artplus.mobile.ui.cards.ServiceConfirmRequest
import dev.artplus.mobile.ui.components.TitleBarIconButton
import dev.artplus.mobile.ui.components.rememberLinkedTopAppBarScrollBehavior
import dev.artplus.mobile.ui.pages.home.EmptyAppListCard
import dev.artplus.mobile.ui.pages.home.homeSelectAppAndRestore
import dev.artplus.mobile.ui.pages.params.paramsApplyTuningParams
import dev.artplus.mobile.ui.pages.params.paramsSaveGptSettings
import dev.artplus.mobile.ui.pages.params.paramsSaveImageTuningSettings
import dev.artplus.mobile.ui.pages.params.paramsSaveLocalSeparationSettings
import dev.artplus.mobile.ui.pages.picker.AppIcon
import dev.artplus.mobile.ui.pages.picker.AppPickerControlsCard
import dev.artplus.mobile.ui.pages.picker.AppRow
import dev.artplus.mobile.ui.pages.picker.pickerAllFilteredSelected
import dev.artplus.mobile.ui.pages.picker.pickerCheckQueryPermission
import dev.artplus.mobile.ui.pages.picker.pickerHasUsageAccess
import dev.artplus.mobile.ui.pages.picker.pickerPostStatus
import dev.artplus.mobile.ui.pages.picker.pickerRefreshPermissionState

@Composable
internal fun MainActivity.AppPickerPage(
    pageBackground: Color,
    filteredApps: List<AppEntry>,
    // 「显示系统应用」收口后的全量：概览计数用它，不受搜索词与分段筛选影响。
    scopedApps: List<AppEntry>,
) {
    // 热修复：选择器渲染位订阅 picker/shell（StateFlow .value 裸读不触发重组，
    // 行高亮 AppRow selected/multi/generated/isBusy 全部 stale，点行写成功但不变色）。
    // 回调内事件时读 .value 仍合法，此处只修组合期渲染读。
    val pickerState by mainViewModel.picker.collectAsState()
    val shellState by mainViewModel.shell.collectAsState()
    val scrollBehavior = rememberLinkedTopAppBarScrollBehavior()

    // 进入选择器时静默对齐 AM 标记（一次 su），徽标不依赖冷启动后的手动刷新
    LaunchedEffect(Unit) {
        mainViewModel.refreshArtPlusModifiedPackagesAsync()
    }

    // Backdrop for the ColorOS app bar material: the list only, never the bar itself.
    val appBarBackdrop = rememberLayerBackdrop {
        drawRect(pageBackground)
        drawContent()
    }
    val contentTopInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
        TopAppBarDefaults.SmallTopAppBarCenterHeight

    // 批量动作从卡片里挪到顶栏和右下角的悬浮球：这里先备好回调。
    val filteredPackageNames = remember(filteredApps) { filteredApps.map { it.packageName }.toSet() }
    // 概览计数：和列表同一份口径（先按「显示系统应用」收口），只是不看搜索词和分段筛选。
    val generatedCount = remember(scopedApps, pickerState.generatedPackageNames) {
        scopedApps.count { it.packageName in pickerState.generatedPackageNames }
    }
    val amMarkedCount = remember(scopedApps, pickerState.artPlusModifiedPackageNames) {
        scopedApps.count { it.packageName in pickerState.artPlusModifiedPackageNames }
    }
    var showMoreMenu by remember { mutableStateOf(false) }
    val refreshApps: () -> Unit = { run {
        mainViewModel.requestAppLoad(
                    refreshGenerated = (true),
                    pm = packageManager,
                    iconCache = MainActivity.appIconCache,
                    cacheSize = ICON_CACHE_SIZE,
                    preloadCount = PRELOAD_ICON_COUNT,
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    markLoad = { didRequestAppLoad = true },
                    onRefreshPermissions = { run {
            pickerRefreshPermissionState(
                        checkQueryPermission = { pickerCheckQueryPermission(packageManager, packageName) },
                        hasUsage = {  ->
                pickerHasUsageAccess(
                            appOps = getSystemService(AppOpsManager::class.java),
                            uid = Process.myUid(),
                            packageName = packageName,
                        )
             },
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
    } }
    val selectCurrent: () -> Unit = {
                        val allSelected = pickerAllFilteredSelected(filteredPackageNames, mainViewModel.picker.value.multiSelectedPackageNames)
                        mainViewModel.updatePicker { it -> it.copy(multiSelectedPackageNames = (if (allSelected) {
                            mainViewModel.picker.value.multiSelectedPackageNames - filteredPackageNames
                        } else {
                            mainViewModel.picker.value.multiSelectedPackageNames + filteredPackageNames
                        })) }
                    }
    val applyCurrentPreset: () -> Unit = { run {
            mainViewModel.applyCurrentPresetBatch(
                        store = presetStore,
                        onRequestConfirm = { title, message, confirmLabel, onConfirm ->
                        mainViewModel.updateConfirm { it -> it.copy(pendingServiceConfirm = (ServiceConfirmRequest(
                            title = (title),
                            message = (message),
                            confirmLabel = (confirmLabel),
                            onConfirm = (onConfirm),
                        ))) }
            
                        },
                        onExecutePreset = { p, pkgs -> run {
                mainViewModel.executeApplyPresetToSelectedApps(
                            preset = (p),
                            batchPackageNames = (pkgs),
                            beforeParams = mainViewModel.params.value,
                            store = presetStore,
                            selectedAtStart = mainViewModel.picker.value.selectedPackageName,
                            apps = apps.toList(),
                            onApplyPresetParams = { merged ->
                    paramsApplyTuningParams(
                                params = (merged),
                                rebuildCandidates = (false),
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
                 },
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
                                isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0,
                                onStatus = { __a0: String -> pickerPostStatus(__a0) { runOnUiThread { setStatus((it)) } } },
                                defaultChoiceForMode = { __a0: LocalSeparationMode, __a1: PreviewChoice ->
                        when (__a0) {
                                    LocalSeparationMode.Original -> PreviewChoice.Original
                                    LocalSeparationMode.Plate -> PreviewChoice.Full
                                    LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                                    LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                                    LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                                    LocalSeparationMode.Auto -> __a1
                                    LocalSeparationMode.Full -> PreviewChoice.Full
                                }
                     },
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
                            onSaveUiState = {  -> persistUiState() },
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
                                isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0,
                                onStatus = { __a0: String -> pickerPostStatus(__a0) { runOnUiThread { setStatus((it)) } } },
                                defaultChoiceForMode = { __a0: LocalSeparationMode, __a1: PreviewChoice ->
                        when (__a0) {
                                    LocalSeparationMode.Original -> PreviewChoice.Original
                                    LocalSeparationMode.Plate -> PreviewChoice.Full
                                    LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                                    LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                                    LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                                    LocalSeparationMode.Auto -> __a1
                                    LocalSeparationMode.Full -> PreviewChoice.Full
                                }
                     },
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
                            onSaveUiState = {  -> persistUiState() },
                        )
            } },
                    )
        } }

    Scaffold(
        // 内容从顶栏底下滚过（顶部留白在 contentPadding 里），COUI 才会给顶栏画玻璃材质。
        edgeToEdge = true,
        containerColor = pageBackground,
        // 浮层要画玻璃，得在它声明处拿到页面内容录制的 backdrop。
        popupHost = {
            CompositionLocalProvider(LocalBackdrop provides appBarBackdrop) {
                COUIPopupUtils.COUIPopupHost()
            }
        },
        floatingActionButton = {
            PickerMoreBall(
                show = showMoreMenu,
                canApplyPreset = !shellState.isBusy && pickerState.multiSelectedPackageNames.isNotEmpty(),
                canSelectCurrent = !shellState.isBusy && filteredPackageNames.isNotEmpty(),
                generatedCount = generatedCount,
                ungeneratedCount = scopedApps.size - generatedCount,
                amMarkedCount = amMarkedCount,
                onShow = { showMoreMenu = true },
                onDismissRequest = { showMoreMenu = false },
                onApplyPreset = {
                    showMoreMenu = false
                    applyCurrentPreset()
                },
                onSelectCurrent = {
                    showMoreMenu = false
                    selectCurrent()
                },
            )
        },
        topBar = {
            CompositionLocalProvider(LocalBackdrop provides appBarBackdrop) {
                SmallTopAppBar(
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
                    actions = {
                        // 清空键排在刷新左边：顶栏 actions 是整排右对齐的，插在刷新后面会把它顶向屏幕中间。
                        // 出现/消失走横向展开收起，而不是直接闪现。
                        AnimatedVisibility(
                            visible = pickerState.multiSelectedPackageNames.isNotEmpty(),
                            enter = fadeIn(animationSpec = tween(durationMillis = 150)) +
                                expandHorizontally(
                                    animationSpec = tween(durationMillis = 180),
                                    expandFrom = Alignment.End,
                                ),
                            exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                                shrinkHorizontally(
                                    animationSpec = tween(durationMillis = 160),
                                    shrinkTowards = Alignment.End,
                                ),
                        ) {
                            // 用错误色标出这是破坏性操作。
                            TitleBarIconButton(
                                icon = COUIIcons.Delete,
                                contentDescription = "清空多选",
                                enabled = !shellState.isBusy,
                                tint = COUITheme.colorScheme.error,
                                onClick = {
                                    mainViewModel.updatePicker { it -> it.copy(multiSelectedPackageNames = (emptySet())) }
                                },
                            )
                        }
                        // 刷新：列表与已生成扫描一起重来，原先拆成两个按钮。
                        TitleBarIconButton(
                            icon = COUIIcons.Refresh,
                            contentDescription = "刷新",
                            enabled = !shellState.isBusy,
                            onClick = refreshApps,
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
            // 底部留出悬浮球的净空：球 56dp + 16dp 边距 + 16dp 间隔，最后一列才不会被球压住。
            contentPadding = PaddingValues(top = contentTopInset + 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                run {
    AppPickerControlsCard(
                showSystemApps = pickerState.showSystemApps,
                generatedFilter = pickerState.generatedFilter,
                queryText = pickerState.queryText,
                onToggleSystemApps = {
                    mainViewModel.updatePicker { it -> it.copy(showSystemApps = !it.showSystemApps) }
                    persistUiState()
                },
                onFilterSelected = {
                    mainViewModel.updatePicker { v -> v.copy(generatedFilter = (it)) }
                    mainViewModel.updatePicker { it -> it.copy(queryText = ("")) }
                    persistUiState()
                },
                onQueryChange = {
                    mainViewModel.updatePicker { v -> v.copy(queryText = (it)) }
                    persistUiState()
                },
                onClearQuery = {
                    mainViewModel.updatePicker { it -> it.copy(queryText = ("")) }
                    persistUiState()
                },
                
            )
}
            }
            // 应用行自成一组，说明下面这一串是可比对选择的应用。
            item(key = "apps_header") {
                PickerAppListHeader(pickerState.multiSelectedPackageNames.size)
            }
            if (filteredApps.isEmpty()) {
                item {
            // 热修复：pickerState/shellState 已提升至 AppPickerPage 顶层，此处复用（删重复订阅）。
            // Slice 3.1: Activity侧collect读VM单源；写经薄wrapper（重构期间保留）。
            EmptyAppListCard(
                queryText = pickerState.queryText,
                showSystemApps = pickerState.showSystemApps,
                hasHiddenSystemApps = apps.any { AppVisibility.isSystemAppFlags(it.applicationInfo.flags) && it.packageName != packageName },
                isBusy = shellState.isBusy,
                onShowSystemApps = {
                    mainViewModel.updatePicker { it -> it.copy(showSystemApps = (true)) }
                    persistUiState()
                },
                onRefresh = refreshApps,
            )
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
                amMarked = (entry.packageName in pickerState.artPlusModifiedPackageNames),
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
                        mainViewModel.updatePreviewSession { v -> v.copy(activeGenerationSession = (null), previewChoiceMode = (null), previewPackageName = (null), previewDirPath = (null)) }
                        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (PreviewSelections.default(PreviewChoice.Original)).normalLight.name, previewNormalDark = (PreviewSelections.default(PreviewChoice.Original)).normalDark.name, previewMonochromeLight = (PreviewSelections.default(PreviewChoice.Original)).monochromeLight.name, previewMonochromeDark = (PreviewSelections.default(PreviewChoice.Original)).monochromeDark.name) }
                        mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                    },
                    onStatusText = { setStatus((it)) },
                    onSaveUi = { persistUiState() },
                    onClearRmbg = { run {

                    if (mainViewModel.previewSession.value.isGeneratingRmbgCandidate) {
                        return@run
                    }
                    mainViewModel.updatePreviewSession { v -> v.copy(lastRmbgCandidateError = (null), rmbgCandidatePackageName = (null), rmbgCandidateMode = (null), rmbgCandidateStatusText = (""), rmbgCandidateFailurePackageName = (null), rmbgCandidateFailureMode = (null)) }
        } },
                    onLaunch = { name, block -> mainViewModel.launchUiFriendly((name), (block)) },
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
                    onMarkGenerated = { pkg -> mainViewModel.markPackageWritten(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), pkg) },
                    onBuildSession = { pkg, dir -> buildGeneratedPackageSession(pkg, dir) },
                    onCommitSession = { session, dir, e ->
                        mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (PreviewSelections.default(PreviewChoice.Original)).normalLight.name, previewNormalDark = (PreviewSelections.default(PreviewChoice.Original)).normalDark.name, previewMonochromeLight = (PreviewSelections.default(PreviewChoice.Original)).monochromeLight.name, previewMonochromeDark = (PreviewSelections.default(PreviewChoice.Original)).monochromeDark.name) }
                        mainViewModel.updatePreviewSession { v -> v.copy(previewChoiceMode = (null), previewPackageName = (e.packageName), previewDirPath = (dir.absolutePath)) }
                        mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                    },
                )
    }
                                mainViewModel.updateShell { it -> it.copy(currentPage = (AppPage.Home)) }
                            }),
                onToggleMultiSelect = ({ mainViewModel.toggleMultiSelectedPackage(packageName = (entry.packageName), current = mainViewModel.picker.value.multiSelectedPackageNames) }),
                icon = {
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
    
                },
            )
}
                }
            }
        }
    }
}
