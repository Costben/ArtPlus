package dev.artplus.mobile.ui.pages.tabs

import dev.artplus.mobile.R
import android.app.WallpaperManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Refresh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dev.artplus.mobile.ICON_CACHE_SIZE
import dev.artplus.mobile.MainActivity
import dev.artplus.mobile.PREFS_NAME
import dev.artplus.mobile.PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE
import dev.artplus.mobile.ROOT_UXICONS_DIR
import dev.artplus.mobile.data.AppEntry
import dev.artplus.mobile.data.getCachedAppIcon
import dev.artplus.mobile.data.markPackageGenerated
import dev.artplus.mobile.imaging.IconCandidate
import dev.artplus.mobile.imaging.previewAssetsForCandidate
import dev.artplus.mobile.pipeline.findRmbgComponent
import dev.artplus.mobile.pipeline.preparedForDraw
import dev.artplus.mobile.pipeline.rmbgTunedForegroundRaw
import dev.artplus.mobile.state.launchUiFriendly
import dev.artplus.mobile.state.loadCachedAppIconOp
import dev.artplus.mobile.state.persistUiState
import dev.artplus.mobile.state.refreshArtPlusIconsAsync
import dev.artplus.mobile.state.setStatus
import dev.artplus.mobile.system.RootWriteMode
import dev.artplus.mobile.system.artPlusPackageDir
import dev.artplus.mobile.system.ensureNomediaAtTreeRoot
import dev.artplus.mobile.system.exportToTree
import dev.artplus.mobile.system.exportToTreeFast
import dev.artplus.mobile.system.hasGeneratedPackageBaseAssets
import dev.artplus.mobile.system.installWithRoot
import dev.artplus.mobile.ui.cards.AppPage
import dev.artplus.mobile.ui.cards.ExportProgress
import dev.artplus.mobile.ui.cards.PreviewSelections
import dev.artplus.mobile.ui.cards.RootWriteConfirmRequest
import dev.artplus.mobile.ui.components.TitleBarIconButton
import dev.artplus.mobile.ui.pages.PagerShellPage
import dev.artplus.mobile.ui.pages.home.GeneratedPreviewCard
import dev.artplus.mobile.ui.pages.home.GenerationActionCard
import dev.artplus.mobile.ui.pages.home.LayerDebugCard
import dev.artplus.mobile.ui.pages.home.PreviewControlCard
import dev.artplus.mobile.ui.pages.home.StatusCard
import dev.artplus.mobile.ui.pages.home.applyPreviewChoiceToAll
import dev.artplus.mobile.ui.pages.home.applyRmbgCandidateForMode
import dev.artplus.mobile.ui.pages.home.generateGptCandidateForMode
import dev.artplus.mobile.ui.pages.home.generateSelectedApp
import dev.artplus.mobile.ui.pages.home.homeApplyPreviewChoice
import dev.artplus.mobile.ui.pages.home.homeWriteSelectedWithRoot
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
import dev.artplus.mobile.ui.pages.picker.pickerToastStatus
import dev.artplus.mobile.ui.components.BarClearance

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
                            item(key = "permission") {
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
                        item(key = "status") {
            // 热修复：shellState 已提升至 HomePage 顶层，此处复用（删重复订阅）。
            // Slice 3.1: Activity侧collect读VM单源；写经薄wrapper（重构期间保留）。
            StatusCard(
                selectedApp = (selectedApp),
                launcherCount = (launcherCount),
                totalCount = (apps.size),
                generatedCount = (generatedCount),
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
                        item(key = "generation_action") {
                            run {
val __act4 = LocalContext.current
    GenerationActionCard(
                selectedApp = (selectedApp),
                isBusy = shellState.isBusy,
                onLocalGenerate = { generateSelectedApp(installWithRoot = false, useGpt = false) },
                onLocalExport = { run {

                if (mainViewModel.shell.value.outputTreeUri == null) {
            pickerToastStatus(
                        message = ("还没有设置目录"),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act4, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        
                    mainViewModel.updatePreviewSession { it -> it.copy(exportDialogVisible = (true)) }
                    return@run
                }
                val dir = mainViewModel.previewSession.value.activeGenerationSession?.outDir
                    ?: mainViewModel.previewSession.value.previewDirPath?.let { File(it) }?.takeIf { it.isDirectory && hasGeneratedPackageBaseAssets(it) }
                    ?: mainViewModel.picker.value.selectedPackageName?.let { run {
            artPlusPackageDir(
                        packageName = (it),
                        externalArtPlusDir = getExternalFilesDir("ArtPlus"),
                        filesDir = filesDir,
                    )
        } }?.takeIf { hasGeneratedPackageBaseAssets(it) }
                if (dir == null || !hasGeneratedPackageBaseAssets(dir)) {
            pickerToastStatus(
                        message = ("没有可导出的图标包"),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act4, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        
                    return@run
                }
                if (mainViewModel.shell.value.isBusy) return@run
                mainViewModel.updateShell { it -> it.copy(isBusy = (true)) }
                mainViewModel.updateTransfer { it -> it.copy(exportProgress = (ExportProgress(
                    title = "导出中",
                    completed = 0,
                    total = 1,
                    currentLabel = "正在导出: ${dir.name}",
                    isIndeterminate = true,
                ))) }
                mainViewModel.updateTransfer { it -> it.copy(singleExportSheetVisible = (true)) }
                singleExportJob?.cancel()
                singleExportJob = mainScope.launch(Dispatchers.IO) {
                    try {
                        runCatching { ensureNomediaAtTreeRoot(contentResolver, mainViewModel.shell.value.outputTreeUri) }
                        // 优先尝试文件系统直拷（su cp），速度为 SAF 的 10-20 倍，失败再回退 SAF
                        val fastOk = runCatching { exportToTreeFast(mainViewModel.shell.value.outputTreeUri, dir) }.getOrDefault(false)
                        if (fastOk) {
                            withContext(Dispatchers.Main) { run {
            pickerToastStatus(
                        message = ("已导出到外部目录: ${dir.name}"),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act4, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        } }
                        } else {
                            withContext(Dispatchers.Main) {
                                runCatching { exportToTree(contentResolver, mainViewModel.shell.value.outputTreeUri, dir) }
                                    .onSuccess { run {
            pickerToastStatus(
                        message = ("已导出到外部目录: ${dir.name}"),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act4, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        } }
                                    .onFailure { error ->
            pickerToastStatus(
                        message = ("导出失败: ${error.message ?: error.javaClass.simpleName}"),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act4, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
         }
                            }
                        }
                    } catch (e: CancellationException) {
                        withContext(Dispatchers.Main) { /* 已在 cancelSingleExport 中处理 */ }
                        throw e
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) { run {
            pickerToastStatus(
                        message = ("导出失败: ${e.message ?: e.javaClass.simpleName}"),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act4, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        } }
                    } finally {
                        withContext(Dispatchers.Main) {
                            mainViewModel.updateTransfer { it -> it.copy(exportProgress = (null)) }
                            mainViewModel.updateTransfer { it -> it.copy(singleExportSheetVisible = (false)) }
                            singleExportJob = null
                            mainViewModel.updateShell { it -> it.copy(isBusy = (false)) }
                        }
                    }
                }
    } },
                onWriteAll = { run {
        homeWriteSelectedWithRoot(
                    entry = apps.firstOrNull { it.packageName == mainViewModel.picker.value.selectedPackageName },
                    rootWriteMode = (RootWriteMode.All),
                    isBusy = shellState.isBusy,
                    activeSession = mainViewModel.previewSession.value.activeGenerationSession,
                    selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
                    autoConfirmRootWrite = mainViewModel.confirm.value.autoConfirmRootWrite,
                    targetPath = apps.firstOrNull { it.packageName == mainViewModel.picker.value.selectedPackageName }?.let { "$ROOT_UXICONS_DIR/${it.packageName}" },
                    onStatusText = { setStatus((it)) },
                    onGenerateFallback = { generateSelectedApp(installWithRoot = true, useGpt = false, rootWriteMode = (RootWriteMode.All)) },
                    onBeginBusy = { msg ->
                        mainViewModel.updateShell { it -> it.copy(isBusy = true, statusText = msg) }
                    },
                    onLaunch = { name, block -> mainViewModel.launchUiFriendly((name), (block)) },
                    onWrite = tuningActions.write,
                    onInstall = { outDir, pkg, mode -> installWithRoot(outDir, pkg, mode) },
                    onPostWrite = { session, selections, e ->
                        runOnUiThread {
                            mainViewModel.updatePicker { it -> it.copy(generatedPackageNames = (markPackageGenerated(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), mainViewModel.picker.value.generatedPackageNames, e.packageName))) }
                            mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                            mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                            mainViewModel.updatePreviewSession { v -> v.copy(previewPackageName = (e.packageName), previewDirPath = (session.outDir.absolutePath)) }
                            mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                            persistUiState()
                        }
                    },
                    onToast = { run {
            pickerToastStatus(
                        message = (it),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act4, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        } },
                    onFinish = { runOnUiThread { mainViewModel.updateShell { it -> it.copy(isBusy = (false)) } } },
                    onRequestConfirm = { pkg, targetPath, mode, onConfirm ->
                        mainViewModel.updateConfirm { it -> it.copy(rootWriteConfirmRememberSkip = (false)) }
                        mainViewModel.updateConfirm { it -> it.copy(pendingRootWriteConfirm = (RootWriteConfirmRequest(
                            packageName = pkg,
                            targetPath = targetPath,
                            rootWriteMode = mode,
                            onConfirm = { onConfirm() },
                        ))) }
                    },
                )
    } },
                onWriteStandard = { run {
        homeWriteSelectedWithRoot(
                    entry = apps.firstOrNull { it.packageName == mainViewModel.picker.value.selectedPackageName },
                    rootWriteMode = (RootWriteMode.StandardOnly),
                    isBusy = shellState.isBusy,
                    activeSession = mainViewModel.previewSession.value.activeGenerationSession,
                    selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
                    autoConfirmRootWrite = mainViewModel.confirm.value.autoConfirmRootWrite,
                    targetPath = apps.firstOrNull { it.packageName == mainViewModel.picker.value.selectedPackageName }?.let { "$ROOT_UXICONS_DIR/${it.packageName}" },
                    onStatusText = { setStatus((it)) },
                    onGenerateFallback = { generateSelectedApp(installWithRoot = true, useGpt = false, rootWriteMode = (RootWriteMode.StandardOnly)) },
                    onBeginBusy = { msg ->
                        mainViewModel.updateShell { it -> it.copy(isBusy = true, statusText = msg) }
                    },
                    onLaunch = { name, block -> mainViewModel.launchUiFriendly((name), (block)) },
                    onWrite = tuningActions.write,
                    onInstall = { outDir, pkg, mode -> installWithRoot(outDir, pkg, mode) },
                    onPostWrite = { session, selections, e ->
                        runOnUiThread {
                            mainViewModel.updatePicker { it -> it.copy(generatedPackageNames = (markPackageGenerated(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), mainViewModel.picker.value.generatedPackageNames, e.packageName))) }
                            mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                            mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                            mainViewModel.updatePreviewSession { v -> v.copy(previewPackageName = (e.packageName), previewDirPath = (session.outDir.absolutePath)) }
                            mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                            persistUiState()
                        }
                    },
                    onToast = { run {
            pickerToastStatus(
                        message = (it),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act4, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        } },
                    onFinish = { runOnUiThread { mainViewModel.updateShell { it -> it.copy(isBusy = (false)) } } },
                    onRequestConfirm = { pkg, targetPath, mode, onConfirm ->
                        mainViewModel.updateConfirm { it -> it.copy(rootWriteConfirmRememberSkip = (false)) }
                        mainViewModel.updateConfirm { it -> it.copy(pendingRootWriteConfirm = (RootWriteConfirmRequest(
                            packageName = pkg,
                            targetPath = targetPath,
                            rootWriteMode = mode,
                            onConfirm = { onConfirm() },
                        ))) }
                    },
                )
    } },
                onWriteMono = { run {
        homeWriteSelectedWithRoot(
                    entry = apps.firstOrNull { it.packageName == mainViewModel.picker.value.selectedPackageName },
                    rootWriteMode = (RootWriteMode.MonochromeOnly),
                    isBusy = shellState.isBusy,
                    activeSession = mainViewModel.previewSession.value.activeGenerationSession,
                    selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
                    autoConfirmRootWrite = mainViewModel.confirm.value.autoConfirmRootWrite,
                    targetPath = apps.firstOrNull { it.packageName == mainViewModel.picker.value.selectedPackageName }?.let { "$ROOT_UXICONS_DIR/${it.packageName}" },
                    onStatusText = { setStatus((it)) },
                    onGenerateFallback = { generateSelectedApp(installWithRoot = true, useGpt = false, rootWriteMode = (RootWriteMode.MonochromeOnly)) },
                    onBeginBusy = { msg ->
                        mainViewModel.updateShell { it -> it.copy(isBusy = true, statusText = msg) }
                    },
                    onLaunch = { name, block -> mainViewModel.launchUiFriendly((name), (block)) },
                    onWrite = tuningActions.write,
                    onInstall = { outDir, pkg, mode -> installWithRoot(outDir, pkg, mode) },
                    onPostWrite = { session, selections, e ->
                        runOnUiThread {
                            mainViewModel.updatePicker { it -> it.copy(generatedPackageNames = (markPackageGenerated(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), mainViewModel.picker.value.generatedPackageNames, e.packageName))) }
                            mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                            mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                            mainViewModel.updatePreviewSession { v -> v.copy(previewPackageName = (e.packageName), previewDirPath = (session.outDir.absolutePath)) }
                            mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                            persistUiState()
                        }
                    },
                    onToast = { run {
            pickerToastStatus(
                        message = (it),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act4, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        } },
                    onFinish = { runOnUiThread { mainViewModel.updateShell { it -> it.copy(isBusy = (false)) } } },
                    onRequestConfirm = { pkg, targetPath, mode, onConfirm ->
                        mainViewModel.updateConfirm { it -> it.copy(rootWriteConfirmRememberSkip = (false)) }
                        mainViewModel.updateConfirm { it -> it.copy(pendingRootWriteConfirm = (RootWriteConfirmRequest(
                            packageName = pkg,
                            targetPath = targetPath,
                            rootWriteMode = mode,
                            onConfirm = { onConfirm() },
                        ))) }
                    },
                )
    } },
            )
}
                        }
                        // 热修复2：预览卡渲染位经已订阅 previewSessionState 读取（裸读 .value 不触发重组，
                        // 切应用后 previewDirPath/previewPackageName/sharedPreviewAssets 变化 UI 无感，四宫格 stale/消失）。
                        if (previewSessionState.previewDirPath != null && previewSessionState.previewPackageName != null) {
                            item(key = "generated_preview") {
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
                gptBaseUrl = mainViewModel.gptRmbgSettings.value.gptBaseUrl,
                gptApiKey = mainViewModel.gptRmbgSettings.value.gptApiKey,
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
                        item(key = "preview_control") {
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
                        item(key = "layer_debug") {
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
