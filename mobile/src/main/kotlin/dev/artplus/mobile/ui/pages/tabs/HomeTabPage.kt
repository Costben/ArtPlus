package dev.artplus.mobile.ui.pages.tabs

import dev.artplus.mobile.R
import android.app.WallpaperManager
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Settings
import io.github.suqi8.coui.kmp.icon.extended.Refresh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dev.artplus.mobile.ICON_CACHE_SIZE
import dev.artplus.mobile.MainActivity
import dev.artplus.mobile.PREFS_NAME
import dev.artplus.mobile.PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE
import dev.artplus.mobile.data.AppEntry
import dev.artplus.mobile.data.getCachedAppIcon
import dev.artplus.mobile.imaging.IconCandidate
import dev.artplus.mobile.imaging.previewAssetsForCandidate
import dev.artplus.mobile.pipeline.findRmbgComponent
import dev.artplus.mobile.pipeline.preparedForDraw
import dev.artplus.mobile.pipeline.rmbgTunedForegroundRaw
import dev.artplus.mobile.state.loadCachedAppIconOp
import dev.artplus.mobile.state.persistUiState
import dev.artplus.mobile.state.refreshArtPlusIconsAsync
import dev.artplus.mobile.state.setStatus
import dev.artplus.mobile.ui.cards.AppPage
import dev.artplus.mobile.ui.cards.PreviewSelections
import dev.artplus.mobile.ui.components.TitleBarIconButton
import dev.artplus.mobile.ui.pages.PagerShellPage
import dev.artplus.mobile.ui.pages.home.GeneratedPreviewCard
import dev.artplus.mobile.ui.pages.home.LayerDebugCard
import dev.artplus.mobile.ui.pages.home.PreviewControlCard
import dev.artplus.mobile.ui.pages.home.StatusCard
import dev.artplus.mobile.ui.pages.home.applyPreviewChoiceToAll
import dev.artplus.mobile.ui.pages.home.applyRmbgCandidateForMode
import dev.artplus.mobile.ui.pages.home.generateGptCandidateForMode
import dev.artplus.mobile.ui.pages.home.homeApplyPreviewChoice
import dev.artplus.mobile.ui.pages.params.paramsSaveImageTuningSettings
import dev.artplus.mobile.ui.pages.params.paramsUpdateForegroundSubjectPercent
import dev.artplus.mobile.ui.pages.params.paramsUpdateNightSubjectLightBackgroundEnabled
import dev.artplus.mobile.ui.pages.params.paramsUpdatePreviewCornerRadiusDp
import dev.artplus.mobile.ui.pages.params.paramsUpdatePreviewDesktopBackground
import dev.artplus.mobile.ui.pages.params.paramsUpdatePreviewIconSizeDp
import dev.artplus.mobile.ui.pages.params.paramsUpdatePreviewStripEnabled
import dev.artplus.mobile.ui.pages.picker.AppIcon
import dev.artplus.mobile.ui.pages.picker.PermissionCard
import dev.artplus.mobile.ui.pages.picker.pickerLoadBundledPreviewWallpaperBitmap
import dev.artplus.mobile.ui.pages.picker.pickerLoadCustomWallpaperBitmap
import dev.artplus.mobile.ui.pages.picker.pickerLoadPreviewWallpaperBitmap
import dev.artplus.mobile.ui.pages.picker.pickerOpenAppPermissionSettings
import dev.artplus.mobile.ui.pages.picker.pickerOpenUsageAccessSettings
import dev.artplus.mobile.ui.pages.picker.pickerSystemMaterialColor
import dev.artplus.mobile.ui.pages.picker.pickerSystemWallpaperDrawable
import dev.artplus.mobile.ui.components.BarClearance
import dev.artplus.mobile.ui.components.sectionItem

/**
 * S4.x：从 HomePage 的 `when (page)` 分支原样搬出的 tab 页（纯移动，key/remember 原样保留）。
 * 状态仍由 mainViewModel 单源提供，本页只订阅自己用到的 flow。
 */
@Composable
internal fun MainActivity.HomeTabPage(
    pageBackground: Color,
    selectedApp: AppEntry?,
    launcherCount: Int,
    totalCount: Int,
    generatedCount: Int,
) {
    val pickerState by mainViewModel.picker.collectAsState()
    val shellState by mainViewModel.shell.collectAsState()
    val previewSessionState by mainViewModel.previewSession.collectAsState()
    val batchConfigState by mainViewModel.batchPreviewConfig.collectAsState()
    val paramsState by mainViewModel.params.collectAsState()
    val gptSettingsState by mainViewModel.gptRmbgSettings.collectAsState()
PagerShellPage(
                    title = "ArtPlus",
                    actions = {
                        TitleBarIconButton(
                            icon = COUIIcons.Refresh,
                            contentDescription = "刷新",
                            enabled = !shellState.isBusy && !previewSessionState.isRefreshingArtPlusIcons,
                            onClick = {
                                if (mainViewModel.confirm.value.autoConfirmRefresh) {
    mainViewModel.refreshArtPlusIconsAsync(
                contentResolver = contentResolver,
                apkPath = applicationInfo.sourceDir,
            )
                                } else {
                                    mainViewModel.updateConfirm { it -> it.copy(refreshConfirmRememberAuto = (false)) }
                                    mainViewModel.updateConfirm { it -> it.copy(refreshConfirmVisible = (true)) }
                                }
                            },
                        )
                        // 设置从底栏移到右上角：点击进入二级设置页（overlay，返回手势可退）。
                        TitleBarIconButton(
                            icon = COUIIcons.Settings,
                            contentDescription = "设置",
                            enabled = true,
                            onClick = { mainViewModel.setPage(AppPage.Settings) },
                        )
                    },
                    showPreviewStrip = previewSessionState.previewStripEnabled,
                ) { innerPadding, scrollBehavior ->
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
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (!pickerState.packageListPermissionGranted || !pickerState.usageAccessGranted) {
                            sectionItem(key = "permission", title = "权限") {
            // 热修复：pickerState/shellState 已提升至 HomePage 顶层，此处复用（删重复订阅）。
            // Slice 3.1: Activity侧collect读VM单源。
            PermissionCard(
                packageListGranted = pickerState.packageListPermissionGranted,
                usageGranted = pickerState.usageAccessGranted,
                isBusy = shellState.isBusy,
                onOpenAppSettings = { run {
        pickerOpenAppPermissionSettings(
                    start = ::startActivity,
                    packageName = packageName,
                    onError = { setStatus((it)) },
                )
    } },
                onOpenUsageSettings = { run {
        pickerOpenUsageAccessSettings(
                    start = ::startActivity,
                    onError = { setStatus((it)) },
                )
    } },
            )
                            }
                        }
                        sectionItem(key = "status", title = "当前应用") {
            // 热修复：shellState 已提升至 HomePage 顶层，此处复用（删重复订阅）。
            // Slice 3.1: Activity侧collect读VM单源；写经薄wrapper（重构期间保留）。
            StatusCard(
                selectedApp = (selectedApp),
                isBusy = shellState.isBusy,
                hasApps = apps.isNotEmpty(),
                statusText = shellState.statusText,
                onOpenPicker = { mainViewModel.updateShell { it -> it.copy(currentPage = (AppPage.AppPicker)) } },
                appIcon = { entry ->
        AppIcon(
                    entry = (entry),
                    size = (48.dp),
                    getCached = { key -> getCachedAppIcon(MainActivity.appIconCache, key) },
                    loadIcon = { run {
            mainViewModel.loadCachedAppIconOp(
                        entry = ((entry)),
                        iconCache = MainActivity.appIconCache,
                        pm = packageManager,
                        cacheSize = ICON_CACHE_SIZE,
                    )
        } },
                )
     },
            )
                        }
                        // 热修复2：预览卡渲染位经已订阅 previewSessionState 读取（裸读 .value 不触发重组，
                        // 切应用后 previewDirPath/previewPackageName/sharedPreviewAssets 变化 UI 无感，四宫格 stale/消失）。
                        if (previewSessionState.previewDirPath != null && previewSessionState.previewPackageName != null) {
                            sectionItem(key = "generated_preview", title = "生成预览") {
                                run {
val __act3 = LocalContext.current
    GeneratedPreviewCard(
                dirPath = previewSessionState.previewDirPath,
                packageName = previewSessionState.previewPackageName,
                session = previewSessionState.activeGenerationSession?.takeIf {
                    it.packageName == previewSessionState.previewPackageName && it.outDir.absolutePath == previewSessionState.previewDirPath
                },
                displayAssets = previewSessionState.sharedPreviewAssets,
                previewLoading = previewSessionState.isGptPreviewLoading || previewSessionState.isPreviewAssetsRefreshing || previewSessionState.isPreviewOutputRefreshing,
                desktopBackground = previewSessionState.previewDesktopBackground,
                iconSizeDp = previewSessionState.previewIconSizeDp,
                cornerRadiusDp = previewSessionState.previewCornerRadiusDp,
                wallpaperInitial = cachedCustomWallpaper ?: cachedSystemWallpaper ?: cachedBundledWallpaper,
                wallpaperKey = batchConfigState.customWallpaperPath,
                loadWallpaper = {
                    withContext(Dispatchers.IO) {
        pickerLoadCustomWallpaperBitmap(
                    path = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                    cachedPath = cachedCustomWallpaperPath,
                    getCached = { cachedCustomWallpaper },
                    setCached = { path, bitmap ->
                        cachedCustomWallpaper = bitmap
                        cachedCustomWallpaperPath = path
                    },
                    shortEdge = PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE,
                )
     ?: run {
        pickerLoadPreviewWallpaperBitmap(
                    getCached = { cachedSystemWallpaper },
                    setCached = { cachedSystemWallpaper = it },
                    loadDrawable = { pickerSystemWallpaperDrawable(WallpaperManager.getInstance(__act3)) },
                    shortEdge = PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE,
                )
    } ?: run {
        pickerLoadBundledPreviewWallpaperBitmap(
                    getCached = { cachedBundledWallpaper },
                    setCached = { cachedBundledWallpaper = it },
                    resources = resources,
                    resId = R.drawable.preview_wallpaper,
                    shortEdge = PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE,
                )
    }
                    }
                },
                materialColorProvider = { __a0: String, __a1: Color ->
        pickerSystemMaterialColor(
                    resources = resources,
                    getColor = ::getColor,
                    resourceName = __a0,
                    fallback = __a1,
                )
     },
                previewChoiceMode = previewSessionState.previewChoiceMode,
                tuningState = paramsState,
                isBusy = shellState.isBusy,
                isGeneratingGptCandidate = previewSessionState.isGeneratingGptCandidate,
                isGeneratingRmbgCandidate = previewSessionState.isGeneratingRmbgCandidate,
                draftForegroundSubjectPercentText = draftForegroundSubjectPercentText,
                isDark = isSystemInDarkTheme(),
                nightSubjectLightBackgroundEnabled = paramsState.nightSubjectLightBackgroundEnabled,
                rmbgCandidatePackageName = previewSessionState.rmbgCandidatePackageName,
                rmbgCandidateMode = previewSessionState.rmbgCandidateMode,
                rmbgCandidateFailurePackageName = previewSessionState.rmbgCandidateFailurePackageName,
                rmbgCandidateFailureMode = previewSessionState.rmbgCandidateFailureMode,
                lastRmbgCandidateError = previewSessionState.lastRmbgCandidateError,
                rmbgCandidateStatusText = previewSessionState.rmbgCandidateStatusText,
                gptBaseUrl = gptSettingsState.gptBaseUrl,
                gptApiKey = gptSettingsState.gptApiKey,
                hasRmbgComponent = findRmbgComponent(filesDir) != null,
                loadCandidateAssets = { candidate, mode ->
                    withContext(previewWorkerDispatcher) {
                        run {

                val params = mainViewModel.params.value
                return@run previewAssetsForCandidate(
                    candidate = (candidate),
                    mode = (mode),
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
                    foregroundShadowLevel = params.foregroundShadowLevel,
                    nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
                )
    }.preparedForDraw()
                    }
                },
                onChoiceClick = { mainViewModel.updatePreviewSession { v -> v.copy(previewChoiceMode = (it)) } },
                onNightFill = { run {
        paramsUpdateNightSubjectLightBackgroundEnabled(
                    enabled = (it),
                    getParams = { mainViewModel.params.value },
                    updateLive = mainViewModel::updateLive,
                    onSave = { run {
            paramsSaveImageTuningSettings(
                        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                        getParams = { mainViewModel.params.value },
                    )
        } },
                    setStatusText = { setStatus((it)) },
                    onRefresh = tuningActions.refresh,
                )
    } },
                onDraftForegroundSubjectPercent = { draftForegroundSubjectPercentText = it },
                onSaveForegroundSubjectPercent = { run {
        paramsUpdateForegroundSubjectPercent(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    getParams = { mainViewModel.params.value },
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    setDraftText = { draftForegroundSubjectPercentText = it },
                    onRefresh = tuningActions.refresh,
                )
    } },
                onGenerateGpt = { generateGptCandidateForMode(it) },
                onGenerateRmbg = { applyRmbgCandidateForMode(it) },
                onChooseCustom = { mode, kind -> run {

                if (mainViewModel.shell.value.isBusy || mainViewModel.previewSession.value.isGeneratingGptCandidate || mainViewModel.previewSession.value.isGeneratingRmbgCandidate) {
                    return@run
                }
                mainViewModel.updatePreviewSession { v -> v.copy(pendingCustomImageMode = ((mode)), pendingCustomImageKind = ((kind))) }
                chooseCustomImageLauncher.launch(
                    arrayOf(
                        "image/png",
                        "image/svg+xml",
                    ),
                )
    } },
                onApplyPreviewChoice = { mode, choice -> run {
        homeApplyPreviewChoice(
                    mode = (mode),
                    choice = (choice),
                    session = mainViewModel.previewSession.value.activeGenerationSession,
                    selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
                    onChooseCustom = { run {

                    if (mainViewModel.shell.value.isBusy || mainViewModel.previewSession.value.isGeneratingGptCandidate || mainViewModel.previewSession.value.isGeneratingRmbgCandidate) {
                        return@run
                    }
                    mainViewModel.updatePreviewSession { v -> v.copy(pendingCustomImageMode = (((mode))), pendingCustomImageKind = (((choice).customKind!!))) }
                    chooseCustomImageLauncher.launch(
                        arrayOf(
                            "image/png",
                            "image/svg+xml",
                        ),
                    )
        } },
                    onGenerateGpt = { generateGptCandidateForMode((mode)) },
                    onStatusText = { setStatus((it)) },
                    onCommitSelections = { selections -> mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) } },
                    onSaveUi = { persistUiState() },
                    onWrite = tuningActions.write,
                )
    } },
                onApplyPreviewChoiceToAll = { applyPreviewChoiceToAll(it) },
                onDismissChoice = { mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) } },
                onDismissChoiceFinished = { },
            )
}
                            }
                        }
                        sectionItem(key = "preview_control", title = "预览设置") {
val __act2 = LocalContext.current
    PreviewControlCard(
                tuningState = paramsState,
                // 热修复2：预览控制卡渲染位经已订阅状态读取（裸读 .value 不触发重组）。
                isBusy = shellState.isBusy,
                previewCornerRadiusDp = previewSessionState.previewCornerRadiusDp,
                draftPreviewCornerRadiusDpText = draftPreviewCornerRadiusDpText,
                previewIconSizeDp = previewSessionState.previewIconSizeDp,
                draftPreviewIconSizeDpText = draftPreviewIconSizeDpText,
                draftForegroundSubjectPercentText = draftForegroundSubjectPercentText,
                previewStripEnabled = previewSessionState.previewStripEnabled,
                previewDesktopBackground = previewSessionState.previewDesktopBackground,
                wallpaperInitial = cachedCustomWallpaper ?: cachedSystemWallpaper ?: cachedBundledWallpaper,
                wallpaperKey = batchConfigState.customWallpaperPath,
                loadWallpaper = {
                    withContext(Dispatchers.IO) {
                        run {
        pickerLoadCustomWallpaperBitmap(
                    path = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                    cachedPath = cachedCustomWallpaperPath,
                    getCached = { cachedCustomWallpaper },
                    setCached = { path, bitmap ->
                        cachedCustomWallpaper = bitmap
                        cachedCustomWallpaperPath = path
                    },
                    shortEdge = PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE,
                )
    } ?: run {
        pickerLoadPreviewWallpaperBitmap(
                    getCached = { cachedSystemWallpaper },
                    setCached = { cachedSystemWallpaper = it },
                    loadDrawable = { pickerSystemWallpaperDrawable(WallpaperManager.getInstance(__act2)) },
                    shortEdge = PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE,
                )
    } ?: run {
        pickerLoadBundledPreviewWallpaperBitmap(
                    getCached = { cachedBundledWallpaper },
                    setCached = { cachedBundledWallpaper = it },
                    resources = resources,
                    resId = R.drawable.preview_wallpaper,
                    shortEdge = PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE,
                )
    }
                    }
                },
                onDraftForegroundSubjectPercent = { draftForegroundSubjectPercentText = it },
                onSaveForegroundSubjectPercent = { run {
        paramsUpdateForegroundSubjectPercent(
                    value = (it),
                    updateLive = mainViewModel::updateLive,
                    getParams = { mainViewModel.params.value },
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    setDraftText = { draftForegroundSubjectPercentText = it },
                    onRefresh = tuningActions.refresh,
                )
    } },
                onDraftPreviewCornerRadiusDp = { draftPreviewCornerRadiusDpText = it },
                onSavePreviewCornerRadiusDp = { run {
        paramsUpdatePreviewCornerRadiusDp(
                    value = (it),
                    setValue = { mainViewModel.updatePreviewSession { v -> v.copy(previewCornerRadiusDp = (it)) } },
                    setDraftText = { draftPreviewCornerRadiusDpText = it },
                    onSave = { persistUiState() },
                )
    } },
                onDraftPreviewIconSizeDp = { draftPreviewIconSizeDpText = it },
                onSavePreviewIconSizeDp = { run {
        paramsUpdatePreviewIconSizeDp(
                    value = (it),
                    setValue = { mainViewModel.updatePreviewSession { v -> v.copy(previewIconSizeDp = (it)) } },
                    setDraftText = { draftPreviewIconSizeDpText = it },
                    onSave = { persistUiState() },
                )
    } },
                onPreviewStripEnabled = { run {
        paramsUpdatePreviewStripEnabled(
                    enabled = (it),
                    getValue = { mainViewModel.previewSession.value.previewStripEnabled },
                    setValue = { mainViewModel.updatePreviewSession { v -> v.copy(previewStripEnabled = (it)) } },
                    onSave = { persistUiState() },
                    setStatusText = { setStatus((it)) },
                )
    } },
                onPreviewDesktopBackground = { run {
        paramsUpdatePreviewDesktopBackground(
                    option = (it),
                    getValue = { mainViewModel.previewSession.value.previewDesktopBackground },
                    setValue = { mainViewModel.updatePreviewSession { v -> v.copy(previewDesktopBackground = (it)) } },
                    onSave = { persistUiState() },
                )
    } },
            )
                        }
                        // 卡片自己在素材不全时什么都不画，这里同条件挡住小标题，免得只剩一个标题。
                        if (
                            previewSessionState.previewDirPath != null &&
                            previewSessionState.activeGenerationSession != null &&
                            previewSessionState.sharedPreviewAssets != null
                        ) {
                            sectionItem(key = "layer_debug", title = "图层调试") {
    LayerDebugCard(
                // 热修复2：调试卡渲染位经已订阅 previewSessionState 读取。
                dirPath = previewSessionState.previewDirPath,
                packageName = previewSessionState.previewPackageName,
                session = previewSessionState.activeGenerationSession?.takeIf {
                    it.packageName == previewSessionState.previewPackageName && it.outDir.absolutePath == previewSessionState.previewDirPath
                },
                assets = previewSessionState.sharedPreviewAssets,
                tuningState = paramsState,
            )
                            }
                        }
                    }
                }
}
