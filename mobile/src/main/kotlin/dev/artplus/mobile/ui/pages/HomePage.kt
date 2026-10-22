package dev.artplus.mobile

import android.app.WallpaperManager
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.blur.rememberLayerBackdrop
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Cpu
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Save
import java.io.File
import java.lang.reflect.InvocationTargetException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.BottomFloatingNavigationBarItem
import io.github.suqi8.coui.kmp.basic.BottomFloatingNavigationBar
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Download
import io.github.suqi8.coui.kmp.icon.extended.GridView
import io.github.suqi8.coui.kmp.icon.extended.Layers
import io.github.suqi8.coui.kmp.icon.extended.Refresh
import io.github.suqi8.coui.kmp.icon.extended.Settings
import io.github.suqi8.coui.kmp.icon.extended.Tune
import io.github.suqi8.coui.kmp.icon.extended.UploadCloud
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun MainActivity.HomePage(pageBackground: Color, selectedApp: AppEntry?, launcherCount: Int, totalCount: Int, generatedCount: Int) {
    // O1 修复：订阅 presetUi（裸读 .value 不触发重组，存/改/删后列表 stale，需重进才刷）。
    // 只加订阅、读取经此订阅走；数据逻辑/持久化顺序/符号名一律不动。
    val presetUiState by mainViewModel.presetUi.collectAsState()
    // 热修复：主页生成链渲染位订阅 picker/shell/previewSession（StateFlow .value 裸读不触发重组，
    // 生成按钮 isBusy/权限门/预览条 stale，选中 magisk 点生成也无产物/进度）。
    // 回调内事件时读 .value 仍合法，此处只修组合期渲染读。
    val pickerState by mainViewModel.picker.collectAsState()
    val shellState by mainViewModel.shell.collectAsState()
    val previewSessionState by mainViewModel.previewSession.collectAsState()
    // 热修复2：四宫格预览链订阅 batchPreviewConfig（wallpaperKey 裸读不触发重组，壁纸切换后预览 stale）。
    val batchConfigState by mainViewModel.batchPreviewConfig.collectAsState()
    // 底栏订阅：glassBar .value 裸读不触发重组，设置页开关翻转后底栏显隐/模糊 stale。
    // 回调内事件时读 .value 仍合法，此处只修组合期渲染读。
    val glassBar by mainViewModel.glassBar.collectAsState()
    // S3.5：调参快照在页面顶层订阅一次，取代 11 处内联 `collectAsState()`（每处都是一次独立订阅 + 一次重组）。
    val paramsState by mainViewModel.params.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
    val scope = rememberCoroutineScope()
    val isBlurEnabled = glassBar.liquidGlassBottomBarEnabled && glassBar.liquidGlassBottomBarBlurEnabled
    var beyondViewportCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        delay(200)
        beyondViewportCount = 1
        delay(350)
        beyondViewportCount = 3
    }
    val backdrop = rememberLayerBackdrop {
        drawRect(pageBackground)
        drawContent()
    }
    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = beyondViewportCount,
            modifier = Modifier
                .fillMaxSize()
                .background(pageBackground)
                .then(if (isBlurEnabled) Modifier.layerBackdrop(backdrop) else Modifier),
        ) { page ->
            when (page) {
                0 -> PagerShellPage(
                    title = "ArtPlus",
                    actions = {
                        TitleBarIconButton(
                            icon = COUIIcons.Refresh,
                            contentDescription = "刷新",
                            enabled = !shellState.isBusy && !previewSessionState.isRefreshingArtPlusIcons,
                            onClick = {
                                if (mainViewModel.confirm.value.autoConfirmRefresh) {
                                    run {
    mainViewModel.refreshArtPlusIconsAsync(
                contentResolver = contentResolver,
                apkPath = applicationInfo.sourceDir,
            )
}
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
                            bottom = 88.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (!pickerState.packageListPermissionGranted || !pickerState.usageAccessGranted) {
                            item(key = "permission") {
                                run {

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
                        }
                        item(key = "status") {
                            run {

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
                appIcon = { entry -> run {
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
    } },
            )
}
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
                    run {
            pickerToastStatus(
                        message = ("还没有设置目录"),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act4, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        }
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
                    run {
            pickerToastStatus(
                        message = ("没有可导出的图标包"),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act4, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        }
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
                                    .onFailure { error -> run {
            pickerToastStatus(
                        message = ("导出失败: ${error.message ?: error.javaClass.simpleName}"),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act4, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        } }
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
                    onLaunch = { name, block -> run {

                    mainViewModel.launchUiFriendly((name), (block))
        } },
                    onWrite = tuningActions.write,
                    onInstall = { outDir, pkg, mode -> installWithRoot(outDir, pkg, mode) },
                    onPostWrite = { session, selections, e ->
                        runOnUiThread {
                            mainViewModel.updatePicker { it -> it.copy(generatedPackageNames = (markPackageGenerated(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), mainViewModel.picker.value.generatedPackageNames, e.packageName))) }
                            mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                            mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                            mainViewModel.updatePreviewSession { v -> v.copy(previewPackageName = (e.packageName), previewDirPath = (session.outDir.absolutePath)) }
                            mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                            run {
            persistUiState()
        }
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
                    onLaunch = { name, block -> run {

                    mainViewModel.launchUiFriendly((name), (block))
        } },
                    onWrite = tuningActions.write,
                    onInstall = { outDir, pkg, mode -> installWithRoot(outDir, pkg, mode) },
                    onPostWrite = { session, selections, e ->
                        runOnUiThread {
                            mainViewModel.updatePicker { it -> it.copy(generatedPackageNames = (markPackageGenerated(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), mainViewModel.picker.value.generatedPackageNames, e.packageName))) }
                            mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                            mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                            mainViewModel.updatePreviewSession { v -> v.copy(previewPackageName = (e.packageName), previewDirPath = (session.outDir.absolutePath)) }
                            mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                            run {
            persistUiState()
        }
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
                    onLaunch = { name, block -> run {

                    mainViewModel.launchUiFriendly((name), (block))
        } },
                    onWrite = tuningActions.write,
                    onInstall = { outDir, pkg, mode -> installWithRoot(outDir, pkg, mode) },
                    onPostWrite = { session, selections, e ->
                        runOnUiThread {
                            mainViewModel.updatePicker { it -> it.copy(generatedPackageNames = (markPackageGenerated(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), mainViewModel.picker.value.generatedPackageNames, e.packageName))) }
                            mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                            mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                            mainViewModel.updatePreviewSession { v -> v.copy(previewPackageName = (e.packageName), previewDirPath = (session.outDir.absolutePath)) }
                            mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                            run {
            persistUiState()
        }
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
                materialColorProvider = { __a0: String, __a1: Color -> run {
        pickerSystemMaterialColor(
                    resources = resources,
                    getColor = ::getColor,
                    resourceName = __a0,
                    fallback = __a1,
                )
    } },
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
                hasRmbgComponent = run {
        findRmbgComponent(filesDir)
    } != null,
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
                    onSaveUi = { run {
            persistUiState()
        } },
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
                            run {
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
                    onSave = { run {
            persistUiState()
        } },
                )
    } },
                onDraftPreviewIconSizeDp = { draftPreviewIconSizeDpText = it },
                onSavePreviewIconSizeDp = { run {
        paramsUpdatePreviewIconSizeDp(
                    value = (it),
                    setValue = { mainViewModel.updatePreviewSession { v -> v.copy(previewIconSizeDp = (it)) } },
                    setDraftText = { draftPreviewIconSizeDpText = it },
                    onSave = { run {
            persistUiState()
        } },
                )
    } },
                onPreviewStripEnabled = { run {
        paramsUpdatePreviewStripEnabled(
                    enabled = (it),
                    getValue = { mainViewModel.previewSession.value.previewStripEnabled },
                    setValue = { mainViewModel.updatePreviewSession { v -> v.copy(previewStripEnabled = (it)) } },
                    onSave = { run {
            persistUiState()
        } },
                    setStatusText = { setStatus((it)) },
                )
    } },
                onPreviewDesktopBackground = { run {
        paramsUpdatePreviewDesktopBackground(
                    option = (it),
                    getValue = { mainViewModel.previewSession.value.previewDesktopBackground },
                    setValue = { mainViewModel.updatePreviewSession { v -> v.copy(previewDesktopBackground = (it)) } },
                    onSave = { run {
            persistUiState()
        } },
                )
    } },
            )
}
                        }
                        item(key = "layer_debug") {
                            run {
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

                1 -> PagerShellPage(
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
                    run {
        persistUiState()
    }
                },
                onRequestSavePreset = {
                    mainViewModel.updatePresetUi { it -> it.copy(presetSaveName = ("")) }
                    mainViewModel.updatePresetUi { it -> it.copy(presetSaveDialogVisible = (true)) }
                },
                onCategorySelected = {
                    mainViewModel.updateShell { v -> v.copy(advancedSettingsCategory = (it)) }
                    run {
        persistUiState()
    }
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
                    onSave = { run {
            tuningActions.save()
        } },
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
                    onSave = { run {
            tuningActions.save()
        } },
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
                    onSave = { run {
            tuningActions.save()
        } },
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
                    onSave = { run {
            tuningActions.save()
        } },
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
                    onSave = { run {
            tuningActions.save()
        } },
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
                    onSave = { run {
            tuningActions.save()
        } },
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
                    onSave = { run {
            tuningActions.save()
        } },
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
                    onSave = { run {
            tuningActions.save()
        } },
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
                    onSave = { run {
            tuningActions.save()
        } },
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
                    onSave = { run {
            tuningActions.save()
        } },
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
                    onSave = { run {
            tuningActions.save()
        } },
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
                    onSave = { run {
            tuningActions.save()
        } },
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
                onToggle = { key, enabled -> run {
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
    } },
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
                currentParams = run {
        mainViewModel.params.value
    },
                draftText = draftJsonParamsText,
                onDraftChange = { draftJsonParamsText = it },
                onSave = { run {
        saveJsonParamsFromText(
                    text = (it),
                    current = run {
            mainViewModel.params.value
        },
                    onApplyParams = { run {
            paramsApplyTuningParams(
                        params = (it),
                        rebuildCandidates = (true),
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
                tuningActions.save()
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
                        onRefresh = tuningActions.refresh,
                    )
        } },
                    onStatus = { setStatus((it)) },
                )
    } },
                onRestore = {
                    draftJsonParamsText = run {
        mainViewModel.params.value
    }.toJson().toString(4)
                    setStatus(("已恢复为当前参数 JSON"))
                },
            )
}
                                }
                            }
                        }
                    }
                }

                2 -> PagerShellPage(
                    title = "预设",
                    actions = {
                        val presetCount = remember(presetUiState.presetListVersion) { presetStore.all().size }
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
                            icon = COUIIcons.UploadCloud,
                            contentDescription = "导出全部预设",
                            enabled = !shellState.isBusy && presetCount > 0,
                            onClick = { run {
    exportPresetsToClipboard(
                store = presetStore,
                clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager,
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
                        val minContentHeight = (maxHeight - topPadding - bottomPadding - 16.dp - 88.dp + 1.dp).coerceAtLeast(0.dp)
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
                        ) {
                            item(key = "preset_cards") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = minContentHeight),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    run {
    PresetStatusCard(
                presets = remember(presetUiState.presetListVersion) { presetStore.all() },
                activePresetId = presetUiState.activePresetId,
                activePresetBaseParams = presetUiState.activePresetBaseParams,
                currentParams = run {
        mainViewModel.params.value
    },
                isBusy = shellState.isBusy,
                onOverwrite = { run {
        overwritePreset(
                    preset = (it),
                    store = presetStore,
                    current = run {
            mainViewModel.params.value
        },
                    viewModel = mainViewModel,
                    onOverwritten = { p, cur, msg ->
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (p.id)) }
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetBaseParams = (cur)) }
                        mainViewModel.updatePresetUi { it -> it.copy(presetListVersion = it.presetListVersion + (1)) }
                        setStatus((msg))
                    },
                    onStatus = { setStatus((it)) },
                )
    } },
                onRequestSavePreset = {
                    mainViewModel.updatePresetUi { v -> v.copy(presetSaveName = (it)) }
                    mainViewModel.updatePresetUi { it -> it.copy(presetSaveDialogVisible = (true)) }
                },
                onResetToPreset = { run {
        resetToPreset(
                    preset = (it),
                    isBusy = shellState.isBusy,
                    isGeneratingGptCandidate = mainViewModel.previewSession.value.isGeneratingGptCandidate,
                    isGeneratingRmbgCandidate = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
                    before = run {
            mainViewModel.params.value
        },
                    viewModel = mainViewModel,
                    onReset = { p, merged, msg ->
                        run {
            paramsApplyTuningParams(
                        params = (merged),
                        rebuildCandidates = (true),
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
                tuningActions.save()
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
                        onRefresh = tuningActions.refresh,
                    )
        }
                        presetStore.activePresetId = p.id
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (p.id)) }
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetBaseParams = (p.params)) }
                        setStatus((msg))
                    },
                    onStatus = { setStatus((it)) },
                )
    } },
                onResetToDefaults = { run {
        paramsResetToDefaults(
                    confirmed = (false),
                    isBusy = { mainViewModel.shell.value.isBusy },
                    isGeneratingGpt = { mainViewModel.previewSession.value.isGeneratingGptCandidate },
                    isGeneratingRmbg = { mainViewModel.previewSession.value.isGeneratingRmbgCandidate },
                    setStatusText = { setStatus((it)) },
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
                    onApplyDefaults = { run {
            paramsApplyTuningParams(
                        params = (it),
                        rebuildCandidates = (true),
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
                tuningActions.save()
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
                        onRefresh = tuningActions.refresh,
                    )
        } },
                    onClearPreset = {
                        presetStore.activePresetId = null
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (null)) }
                    },
                )
    } },
            )
}
                                    run {
    PresetLibraryCard(
                presets = remember(presetUiState.presetListVersion) { presetStore.all() },
                activePresetId = presetUiState.activePresetId,
                activePresetBaseParams = presetUiState.activePresetBaseParams,
                currentParams = run {
        mainViewModel.params.value
    },
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
                    before = run {
            mainViewModel.params.value
        },
                    viewModel = mainViewModel,
                    onApplied = { p, merged, msg ->
                        run {
            paramsApplyTuningParams(
                        params = (merged),
                        rebuildCandidates = (true),
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
                tuningActions.save()
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
                        onRefresh = tuningActions.refresh,
                    )
        }
                        presetStore.activePresetId = p.id
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (p.id)) }
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetBaseParams = (p.params)) }
                        setStatus((msg))
                    },
                    onStatus = { setStatus((it)) },
                )
    } },
                onPreview = { run {
        mainViewModel.openBatchPreviewForPreset(preset = (it), filesDir = filesDir)
    } },
                onMore = { mainViewModel.updatePresetUi { v -> v.copy(presetActionMenuTarget = (it)) } },
            )
}
                                    run {
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
                    onSave = { run {
            persistUiState()
        } },
                )
    } },
            )
}
                                }
                            }
                        }
                    }
                }

                3 -> PagerShellPage(
                    title = "设置",
                    actions = {
                        TitleBarIconButton(
                            icon = Lucide.Save,
                            contentDescription = "保存",
                            // 热修复2：渲染位经已订阅 shellState 读取。
                            enabled = !shellState.isBusy,
                            onClick = { run {
    paramsSaveSettingsPage(
                saveGpt = { run {
        paramsSaveGptSettings(
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    getParams = { mainViewModel.params.value },
                    getGptApiKey = { mainViewModel.gptRmbgSettings.value.gptApiKey },
                    getGptModelId = { mainViewModel.gptRmbgSettings.value.gptModelId },
                    getGptBaseUrl = { mainViewModel.gptRmbgSettings.value.gptBaseUrl },
                )
    } },
                saveRmbg = { run {
        paramsSaveRmbgSettings(
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    getComponentUrl = { mainViewModel.gptRmbgSettings.value.rmbgComponentUrl },
                )
    } },
                saveLocalSeparation = { run {
        paramsSaveLocalSeparationSettings(
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    getParams = { mainViewModel.params.value },
                )
    } },
                saveImageTuning = { run {
        paramsSaveImageTuningSettings(
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    getParams = { mainViewModel.params.value },
                )
    } },
                saveLiquidGlass = { run {
        tuningActions.save()
    } },
                saveUi = { run {
        persistUiState()
    } },
                setGptSaveStatus = { mainViewModel.updateGptRmbgSettings { v -> v.copy(gptSettingsSaveStatus = (it)) } },
                setRmbgSaveStatus = { mainViewModel.updateGptRmbgSettings { v -> v.copy(rmbgComponentSaveStatus = (it)) } },
                setStatusText = { setStatus((it)) },
            )
} },
                        )
                    },
                ) { innerPadding, scrollBehavior ->
                    SettingsPage(
                        innerPadding = innerPadding,
                        scrollBehavior = scrollBehavior,
                        launcherCount = launcherCount,
                        totalCount = totalCount,
                        generatedCount = generatedCount,
                    )
                }
            }
        }

        // ColorOS 17 floating capsule bar: COUI BottomFloatingNavigationBar over the page backdrop.
        // The bar forwards its modifier into the capsule row, so alignment and the gesture-bar inset
        // ride on this wrapper instead of the bar itself.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            BottomFloatingNavigationBar(
                backdrop = if (isBlurEnabled) backdrop else null,
            ) {
                listOf(
                    Triple(COUIIcons.GridView, "主页", 0),
                    Triple(COUIIcons.Tune, "生成参数", 1),
                    Triple(COUIIcons.Layers, "预设", 2),
                    Triple(COUIIcons.Settings, "设置", 3),
                ).forEach { (icon, label, index) ->
                    BottomFloatingNavigationBarItem(
                        selected = pagerState.targetPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        icon = icon,
                        label = label,
                    )
                }
            }
        }
    }
}

/** 主页面顶栏的紧凑 1×4 预览；设置页和应用选择页不会组合此组件。 */
@Composable
internal fun MainActivity.HomePreviewStrip(
    onHeightMeasured: (androidx.compose.ui.unit.Dp) -> Unit = {},
) {
    val density = LocalDensity.current
    // 热修复2：顶部 1×4 预览条渲染位订阅 previewSession（裸读 .value 不触发重组，切应用后条带 stale）。
    val stripPreviewState by mainViewModel.previewSession.collectAsState()
    val stripBatchConfig by mainViewModel.batchPreviewConfig.collectAsState()
    val assets = stripPreviewState.sharedPreviewAssets
    val loading = stripPreviewState.isPreviewAssetsRefreshing || stripPreviewState.isPreviewOutputRefreshing || stripPreviewState.isGptPreviewLoading
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp)
            .onGloballyPositioned { coords ->
                val h = with(density) { coords.size.height.toDp() }
                if (h > 0.dp) onHeightMeasured(h)
            },
        insideMargin = PaddingValues(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PreviewMode.entries.forEach { mode ->
                run {
val __act1 = LocalContext.current
    TopPreviewStripTile(
                assets = (assets),
                mode = (mode),
                loading = (loading),
                desktopBackground = (stripPreviewState.previewDesktopBackground),
                iconSizeDp = (stripPreviewState.previewIconSizeDp),
                cornerRadiusDp = (stripPreviewState.previewCornerRadiusDp),
                wallpaperInitial = cachedCustomWallpaper ?: cachedSystemWallpaper ?: cachedBundledWallpaper,
                wallpaperKey = stripBatchConfig.customWallpaperPath,
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
                    loadDrawable = { pickerSystemWallpaperDrawable(WallpaperManager.getInstance(__act1)) },
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
                materialColorProvider = { __a0: String, __a1: Color -> run {
        pickerSystemMaterialColor(
                    resources = resources,
                    getColor = ::getColor,
                    resourceName = __a0,
                    fallback = __a1,
                )
    } },
                modifier = (Modifier.weight(1f)),
            )
}
            }
        }
    }
}

private fun MainActivity.generateGptCandidate(confirmed: Boolean = false): Unit {
    val __actF4 = this
            val session = mainViewModel.previewSession.value.activeGenerationSession ?: return
            if (mainViewModel.gptRmbgSettings.value.gptBaseUrl.trim().isEmpty() || mainViewModel.gptRmbgSettings.value.gptApiKey.trim().isEmpty()) {
                setStatus(("请填写AI提供商信息"))
                return
            }
            if (mainViewModel.previewSession.value.isGeneratingGptCandidate || mainViewModel.shell.value.isBusy) {
                return
            }
            if (!confirmed) {
                run {

                mainViewModel.updateConfirm { it -> it.copy(pendingServiceConfirm = (ServiceConfirmRequest(
                    title = ("使用 AI 生成全部"),
                    message = ("将调用云端图像接口（已累计 ${mainViewModel.presetUi.value.gptRunCount} 次）。确认继续？"),
                    confirmLabel = ("继续"),
                    onConfirm = ({
                        generateGptCandidate(confirmed = true)
                    }),
                ))) }
    }
                return
            }
            mainViewModel.updatePreviewSession { v -> v.copy(isGeneratingGptCandidate = (true), isGptPreviewLoading = (true)) }
            run {

                mainViewModel.updatePresetUi { it -> it.copy(gptRunCount = it.gptRunCount + (1)) }
                getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putInt(MainActivity.PREF_GPT_RUN_COUNT, mainViewModel.presetUi.value.gptRunCount)
                    .apply()
    }
            setStatus(("AI候选生成中: ${session.packageName}"))
            val selections = PreviewSelections.default(PreviewChoice.Gpt)
            run {

                mainViewModel.launchUiFriendly(("ArtPlusGptCandidateAll"), ({
                    try {
                        // P4 交界：GPT 图层收敛进 pipeline/，显式传调参 + 凭证 + 状态回调。
                        val gptLayers = generateGptLayers(session.sourceIcon, session.baseRecfg, session.baseRecbg, mainViewModel.params.value.gptCustomPrompt, GptPromptPreset.fromValue(mainViewModel.params.value.gptPromptPreset), mainViewModel.params.value.foregroundSubjectPercent, GptImageMode.fromValue(mainViewModel.params.value.gptImageMode), mainViewModel.gptRmbgSettings.value.gptModelId, mainViewModel.gptRmbgSettings.value.gptBaseUrl, mainViewModel.gptRmbgSettings.value.gptApiKey, run {
            (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        }, { __a0: String -> run {

                    pickerPostStatus(__a0) { runOnUiThread { setStatus((it)) } }
        } })
                        val updatedSession = session.copy(
                            candidates = session.candidates + (
                                PreviewChoice.Gpt to IconCandidate(
                                    recfgRaw = gptLayers.recfg,
                                    recbg = gptLayers.recbg,
                                    monochromeRaw = null,
                                    isLocal = false,
                                )
                                ),
                        )
                        run {

                    val params = mainViewModel.params.value
                    writePackageOutputs(
                        session = (updatedSession),
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
        }
                        if (false && mainViewModel.shell.value.outputTreeUri != null) {
                            exportToTree(contentResolver, mainViewModel.shell.value.outputTreeUri, updatedSession.outDir)
                        }
                        runOnUiThread {
                            mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (updatedSession)) }
                            mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                            mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) }
                            mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                            val msg = "AI候选已生成并应用到全部"
                            setStatus((msg))
                            Toast.makeText(__actF4, msg, Toast.LENGTH_SHORT).show()
                            run {
            persistUiState()
        }
                        }
                    } catch (error: Exception) {
                        run {
            pickerToastStatus(
                        message = ("AI候选失败: ${error.message ?: error.javaClass.simpleName}"),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__actF4, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        }
                    } finally {
                        runOnUiThread {
                            mainViewModel.updatePreviewSession { v -> v.copy(isGeneratingGptCandidate = (false), isGptPreviewLoading = (false)) }
                        }
                    }
                }))
    }
}

private fun MainActivity.applyPreviewChoiceToAll(choice: PreviewChoice): Unit {
    val __actF5 = this
    return homeApplyPreviewChoiceToAll(
                    choice = choice,
                    session = mainViewModel.previewSession.value.activeGenerationSession,
                    batchPackageNames = mainViewModel.picker.value.multiSelectedPackageNames.toList().sorted(),
                    onApplyToSelected = { c, pkgs -> run {
            homeApplyPreviewChoiceToSelectedPackages(
                        choice = (c),
                        packageNames = (pkgs),
                        gptBaseUrl = mainViewModel.gptRmbgSettings.value.gptBaseUrl,
                        gptApiKey = mainViewModel.gptRmbgSettings.value.gptApiKey,
                        hasRmbgComponent = run {
                findRmbgComponent(filesDir)
            } != null,
                        isBusy = mainViewModel.shell.value.isBusy,
                        isGeneratingGpt = mainViewModel.previewSession.value.isGeneratingGptCandidate,
                        isGeneratingRmbg = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
                        tryAcquireRmbg = { rmbgGenerationGate.compareAndSet(false, true) },
                        onStatusText = { setStatus((it)) },
                        onBegin = { total ->
                            mainViewModel.updateShell { it -> it.copy(isBusy = (true)) }
                            mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) }
                            mainViewModel.updateTransfer { it -> it.copy(batchApplyProgress = (BatchApplyProgress(
                                title = "全部应用",
                                completed = 0,
                                total = total,
                                currentLabel = "准备处理 $total 个 APK",
                                failures = 0,
                            ))) }
                        },
                        selectedAtStart = mainViewModel.picker.value.selectedPackageName,
                        apps = apps,
                        onProgress = { completed, total, label, failures -> run {
                mainViewModel.updateBatchApplyProgress(
                        completed = (completed),
                        total = (total),
                        currentLabel = (label),
                        failures = (failures),
                        title = "全部应用",
                    )
            } },
                        onGeneratePackage = { app, c -> run {
                homeGeneratePackageForPreviewChoice(
                            app = (app),
                            choice = (c),
                            onGenerate = { a, g -> run {

                            val icon = (a).applicationInfo.loadIcon(packageManager)
                            return@run generateArtPlusPackage(
                                app = (a),
                                useGpt = (g),
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

                                pickerPostStatus(__a0) { runOnUiThread { setStatus((it)) } }
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
                            onBuildRmbg = { src -> (run {

                            val params = mainViewModel.params.value
                            return@run buildRmbgCandidate(
                                sourceIcon = (src),
                                filesDir = filesDir,
                                rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
                                rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
                                rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
                                rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
                                lock = __actF5,
                                getRuntime = { rmbgRuntime },
                                setRuntime = { rmbgRuntime = it },
                            )
                } ?: error("未安装 RMBG 组件 ZIP")).candidate ?: error("RMBG候选为空") },
                            onResize = { src, w, h -> resizeBitmap(src, w, h) },
                            onWrite = tuningActions.write,
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
                            candidateAvailable = { s, c -> candidateForChoice(s, c) },
                        )
            } },
                        onInstall = { outDir, pkg -> installWithRoot(outDir, pkg, RootWriteMode.All) },
                        onFinishBatch = { successes, failures, selectedResult, atStart ->
                            runOnUiThread {
                                if (successes.isNotEmpty()) {
                                    mainViewModel.updatePicker { it -> it.copy(generatedPackageNames = (updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), mainViewModel.picker.value.generatedPackageNames + successes))) }
                                    mainViewModel.updatePicker { it -> it.copy(multiSelectedPackageNames = (mainViewModel.picker.value.multiSelectedPackageNames - successes.toSet())) }
                                }
                                if (selectedResult != null && mainViewModel.picker.value.selectedPackageName == atStart) {
                                    mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (selectedResult.session)) }
                                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selectedResult.selections).normalLight.name, previewNormalDark = (selectedResult.selections).normalDark.name, previewMonochromeLight = (selectedResult.selections).monochromeLight.name, previewMonochromeDark = (selectedResult.selections).monochromeDark.name) }
                                    mainViewModel.updatePreviewSession { v -> v.copy(previewChoiceMode = (null), previewPackageName = (selectedResult.session.packageName), previewDirPath = (selectedResult.outDir.absolutePath)) }
                                    mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                                    run {
                persistUiState()
            }
                                }
                                mainViewModel.updateShell { it -> it.copy(statusText = (when {
                                    failures.isEmpty() -> "全部应用完成: ${successes.size}/${(pkgs).size}"
                                    successes.isEmpty() -> "全部应用失败: ${failures.firstOrNull().orEmpty()}"
                                    else -> "全部应用完成 ${successes.size} 个，失败 ${failures.size} 个: ${failures.firstOrNull().orEmpty()}"
                                })) }
                            }
                        },
                        onReleaseRmbg = { rmbgGenerationGate.set(false) },
                        onResetBusy = {
                            runOnUiThread {
                                mainViewModel.updateShell { it -> it.copy(isBusy = (false)) }
                                mainViewModel.updatePreviewSession { v -> v.copy(isGptPreviewLoading = (false), isGeneratingGptCandidate = (false), isGeneratingRmbgCandidate = (false), rmbgCandidatePackageName = (null), rmbgCandidateMode = (null), rmbgCandidateStatusText = ("")) }
                                mainViewModel.updateTransfer { it -> it.copy(batchApplyProgress = (null)) }
                            }
                        },
                        onLaunch = { name, block -> run {

                        mainViewModel.launchUiFriendly((name), (block))
            } },
                    )
        } },
                    onGenerateGptAll = { generateGptCandidate() },
                    onGenerateRmbgAll = { generateRmbgCandidate() },
                    onStatusText = { setStatus((it)) },
                    candidateAvailable = { s, c -> candidateForChoice(s, c) != null },
                    onCommitDefault = { selections -> mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) } },
                    onClearChoiceMode = { mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) } },
                    onSaveUi = { run {
            persistUiState()
        } },
                    onWriteClose = { session, selections -> run {
            homeWriteActivePreviewOutputs(
                        session = (session),
                        selections = (selections),
                        closeDialog = (true),
                        scope = previewWorkerScope,
                        getJob = { previewOutputJob },
                        setJob = { previewOutputJob = it },
                        incRevision = { ++previewOutputRevision },
                        getRevision = { previewOutputRevision },
                        setRefreshing = { mainViewModel.updatePreviewSession { v -> v.copy(isPreviewOutputRefreshing = (it)) } },
                        outputDebounceMs = PREVIEW_OUTPUT_DEBOUNCE_MS,
                        onWrite = tuningActions.write,
                        onCommit = { s, sel, close ->
                            mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (s)) }
                            mainViewModel.updateLive { p -> p.copy(previewNormalLight = (sel).normalLight.name, previewNormalDark = (sel).normalDark.name, previewMonochromeLight = (sel).monochromeLight.name, previewMonochromeDark = (sel).monochromeDark.name) }
                            mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                            if (close) {
                                mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) }
                            }
                            run {
                persistUiState()
            }
                        },
                        onStatus = { run {

                        pickerPostStatus((it)) { runOnUiThread { setStatus((it)) } }
            } },
                    )
        } },
                )
}

private fun MainActivity.generateRmbgCandidate(confirmed: Boolean = false): Unit {
    val __actF6 = this
            val session = mainViewModel.previewSession.value.activeGenerationSession ?: return
            if (session.candidates[PreviewChoice.Rmbg] != null) {
                applyPreviewChoiceToAll(PreviewChoice.Rmbg)
                setStatus(("已使用现有 RMBG 候选"))
                return
            }
            if (run {
        findRmbgComponent(filesDir)
    } == null) {
                mainViewModel.updatePreviewSession { v -> v.copy(lastRmbgCandidateError = ("未安装 RMBG 组件 ZIP"), rmbgCandidateFailurePackageName = (session.packageName), rmbgCandidateFailureMode = (null)) }
                setStatus((mainViewModel.previewSession.value.lastRmbgCandidateError ?: "未安装 RMBG 组件"))
                return
            }
            if (mainViewModel.previewSession.value.isGeneratingRmbgCandidate || mainViewModel.previewSession.value.isGeneratingGptCandidate || mainViewModel.shell.value.isBusy) {
                setStatus(("RMBG正在运行或主任务忙，请等待"))
                return
            }
            if (!confirmed) {
                run {

                mainViewModel.updateConfirm { it -> it.copy(pendingServiceConfirm = (ServiceConfirmRequest(
                    title = ("使用 RMBG 抠图全部"),
                    message = ("将运行本地 ONNX 模型抠图（已累计 ${mainViewModel.presetUi.value.rmbgRunCount} 次）。确认继续？"),
                    confirmLabel = ("继续"),
                    onConfirm = ({
                        generateRmbgCandidate(confirmed = true)
                    }),
                ))) }
    }
                return
            }
            if (!rmbgGenerationGate.compareAndSet(false, true)) {
                setStatus(("RMBG正在运行，请等待"))
                return
            }
            mainViewModel.updatePreviewSession { it -> it.copy(isGeneratingRmbgCandidate = (true)) }
            run {

                mainViewModel.updatePresetUi { it -> it.copy(rmbgRunCount = it.rmbgRunCount + (1)) }
                getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putInt(MainActivity.PREF_RMBG_RUN_COUNT, mainViewModel.presetUi.value.rmbgRunCount)
                    .apply()
    }
            mainViewModel.updatePreviewSession { v -> v.copy(lastRmbgCandidateError = (null), rmbgCandidatePackageName = (session.packageName), rmbgCandidateMode = (null), rmbgCandidateStatusText = ("RMBG运行中(${RmbgInferenceBackend.Cpu.label})，请等待: 全部"), rmbgCandidateFailurePackageName = (null), rmbgCandidateFailureMode = (null)) }
            setStatus(("RMBG候选生成中(${RmbgInferenceBackend.Cpu.label}): ${session.packageName}"))
            val selections = PreviewSelections.default(PreviewChoice.Rmbg)
            run {

                mainViewModel.launchUiFriendly(("ArtPlusRmbgCandidateAll"), ({
                    try {
                        val source = resizeBitmap(session.sourceIcon, SIZE_1X1, SIZE_1X1)
                        val result = run {

                    val params = mainViewModel.params.value
                    return@run buildRmbgCandidate(
                        sourceIcon = (source),
                        filesDir = filesDir,
                        rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
                        rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
                        rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
                        rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
                        lock = __actF6,
                        getRuntime = { rmbgRuntime },
                        setRuntime = { rmbgRuntime = it },
                    )
        }
                            ?: error("未安装 RMBG 组件 ZIP")
                        val candidate = result.candidate ?: error("RMBG候选为空")
                        val inferenceReport = result.rmbgInference
                        val updatedSession = session.copy(
                            candidates = session.candidates + (PreviewChoice.Rmbg to candidate),
                        )
                        run {

                    val params = mainViewModel.params.value
                    writePackageOutputs(
                        session = (updatedSession),
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
        }
                        if (false && mainViewModel.shell.value.outputTreeUri != null) {
                            exportToTree(contentResolver, mainViewModel.shell.value.outputTreeUri, updatedSession.outDir)
                        }
                        runOnUiThread {
                            mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (updatedSession)) }
                            mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                            mainViewModel.updatePreviewSession { v -> v.copy(previewChoiceMode = (null), previewVersion = v.previewVersion + (1), lastRmbgCandidateError = (null), lastRmbgInferenceReport = (inferenceReport), rmbgCandidateFailurePackageName = (null), rmbgCandidateFailureMode = (null)) }
                            val msg = if (result.validationWarning != null) {
                                "${result.validationWarning}，已应用到全部: ${run {

                    if ((inferenceReport) == null) {
                        return@run RmbgInferenceBackend.Cpu.label
                    }
                    return@run buildString {
                        append((inferenceReport).actualBackend.label)
                        append(" ")
                        append((inferenceReport).elapsedMs)
                        append("ms")
                    }
        }}"
                            } else {
                                "RMBG候选已生成并应用到全部: ${run {

                    if ((inferenceReport) == null) {
                        return@run RmbgInferenceBackend.Cpu.label
                    }
                    return@run buildString {
                        append((inferenceReport).actualBackend.label)
                        append(" ")
                        append((inferenceReport).elapsedMs)
                        append("ms")
                    }
        }}"
                            }
                            setStatus((msg))
                            Toast.makeText(__actF6, msg, Toast.LENGTH_SHORT).show()
                            run {
            persistUiState()
        }
                        }
                    } catch (error: Throwable) {
                        val message = run {

                    val root = run {

                        var current = ((error))
                        while (current is InvocationTargetException && current.targetException != null) {
                            current = current.targetException
                        }
                        return@run current
            }
                    val raw = root.message ?: root.javaClass.simpleName
                    val lower = raw.lowercase()
                    return@run when {
                        root is OutOfMemoryError ||
                            "outofmemory" in lower ||
                            "failed to allocate" in lower ||
                            "memory" in lower -> {
                            "内存不足或 ONNX 分配失败；已释放会话但 RMBG-2.0 峰值仍较高"
                        }
                        "未通过校验" in raw -> {
                            raw
                        }
                        "reshape" in lower || "shape" in lower || "invalid dimensions" in lower -> {
                            "模型输入尺寸不匹配；当前 RMBG-2.0 ONNX 组件需要 1024 推理分辨率"
                        }
                        else -> raw
                    }
        }
                        runOnUiThread {
                            mainViewModel.updatePreviewSession { v -> v.copy(lastRmbgCandidateError = (message), rmbgCandidateFailurePackageName = (session.packageName), rmbgCandidateFailureMode = (null)) }
                            val msg = "RMBG候选失败(${RmbgInferenceBackend.Cpu.label}): $message"
                            setStatus((msg))
                            Toast.makeText(__actF6, msg, Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        rmbgGenerationGate.set(false)
                        runOnUiThread {
                            mainViewModel.updatePreviewSession { v -> v.copy(isGeneratingRmbgCandidate = (false), rmbgCandidatePackageName = (null), rmbgCandidateMode = (null), rmbgCandidateStatusText = ("")) }
                        }
                    }
                }))
    }
}

private fun MainActivity.generateGptCandidateForMode(mode: PreviewMode, confirmed: Boolean = false): Unit {
    val __actF8 = this
            val session = mainViewModel.previewSession.value.activeGenerationSession ?: return
            if (mainViewModel.gptRmbgSettings.value.gptBaseUrl.trim().isEmpty() || mainViewModel.gptRmbgSettings.value.gptApiKey.trim().isEmpty()) {
                setStatus(("请填写AI提供商信息"))
                return
            }
            if (mainViewModel.previewSession.value.isGeneratingGptCandidate || mainViewModel.shell.value.isBusy) {
                return
            }
            if (!confirmed) {
                run {

                mainViewModel.updateConfirm { it -> it.copy(pendingServiceConfirm = (ServiceConfirmRequest(
                    title = ("使用 AI 生成"),
                    message = ("将调用云端图像接口（已累计 ${mainViewModel.presetUi.value.gptRunCount} 次）。确认继续？"),
                    confirmLabel = ("继续"),
                    onConfirm = ({
                        generateGptCandidateForMode(mode, confirmed = true)
                    }),
                ))) }
    }
                return
            }
            mainViewModel.updatePreviewSession { v -> v.copy(isGeneratingGptCandidate = (true), isGptPreviewLoading = (true)) }
            run {

                mainViewModel.updatePresetUi { it -> it.copy(gptRunCount = it.gptRunCount + (1)) }
                getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putInt(MainActivity.PREF_GPT_RUN_COUNT, mainViewModel.presetUi.value.gptRunCount)
                    .apply()
    }
            setStatus(("AI候选生成中: ${session.packageName}"))
            val selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark).withChoice(mode, PreviewChoice.Gpt)
            run {

                mainViewModel.launchUiFriendly(("ArtPlusGptCandidate"), ({
                    try {
                        // P4 交界：GPT 图层收敛进 pipeline/，显式传调参 + 凭证 + 状态回调。
                        val gptLayers = generateGptLayers(session.sourceIcon, session.baseRecfg, session.baseRecbg, mainViewModel.params.value.gptCustomPrompt, GptPromptPreset.fromValue(mainViewModel.params.value.gptPromptPreset), mainViewModel.params.value.foregroundSubjectPercent, GptImageMode.fromValue(mainViewModel.params.value.gptImageMode), mainViewModel.gptRmbgSettings.value.gptModelId, mainViewModel.gptRmbgSettings.value.gptBaseUrl, mainViewModel.gptRmbgSettings.value.gptApiKey, run {
            (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        }, { __a0: String -> run {

                    pickerPostStatus(__a0) { runOnUiThread { setStatus((it)) } }
        } })
                        val updatedSession = session.copy(
                            candidates = session.candidates + (
                                PreviewChoice.Gpt to IconCandidate(
                                    recfgRaw = gptLayers.recfg,
                                    recbg = gptLayers.recbg,
                                    monochromeRaw = null,
                                    isLocal = false,
                                )
                                ),
                        )
                        run {

                    val params = mainViewModel.params.value
                    writePackageOutputs(
                        session = (updatedSession),
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
        }
                        if (false && mainViewModel.shell.value.outputTreeUri != null) {
                            exportToTree(contentResolver, mainViewModel.shell.value.outputTreeUri, updatedSession.outDir)
                        }
                        runOnUiThread {
                            mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (updatedSession)) }
                            mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                            mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                            val msg = "AI候选已生成并应用到 ${mode.label}"
                            setStatus((msg))
                            Toast.makeText(__actF8, msg, Toast.LENGTH_SHORT).show()
                            run {
            persistUiState()
        }
                        }
                    } catch (error: Exception) {
                        run {
            pickerToastStatus(
                        message = ("AI候选失败: ${error.message ?: error.javaClass.simpleName}"),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__actF8, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        }
                    } finally {
                        runOnUiThread {
                            mainViewModel.updatePreviewSession { v -> v.copy(isGeneratingGptCandidate = (false), isGptPreviewLoading = (false)) }
                        }
                    }
                }))
    }
}

private fun MainActivity.applyRmbgCandidateForMode(mode: PreviewMode, confirmed: Boolean = false): Unit {
    val __actF10 = this
            val session = mainViewModel.previewSession.value.activeGenerationSession ?: return
            if (session.candidates[PreviewChoice.Rmbg] != null) {
                run {
        homeApplyPreviewChoice(
                    mode = (mode),
                    choice = (PreviewChoice.Rmbg),
                    session = mainViewModel.previewSession.value.activeGenerationSession,
                    selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
                    onChooseCustom = { run {

                    if (mainViewModel.shell.value.isBusy || mainViewModel.previewSession.value.isGeneratingGptCandidate || mainViewModel.previewSession.value.isGeneratingRmbgCandidate) {
                        return@run
                    }
                    mainViewModel.updatePreviewSession { v -> v.copy(pendingCustomImageMode = (((mode))), pendingCustomImageKind = (((PreviewChoice.Rmbg).customKind!!))) }
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
                    onSaveUi = { run {
            persistUiState()
        } },
                    onWrite = tuningActions.write,
                )
    }
                setStatus(("已使用现有 RMBG 候选"))
                return
            }
            if (run {
        findRmbgComponent(filesDir)
    } == null) {
                mainViewModel.updatePreviewSession { v -> v.copy(lastRmbgCandidateError = ("未安装 RMBG 组件 ZIP"), rmbgCandidateFailurePackageName = (session.packageName), rmbgCandidateFailureMode = (mode)) }
                setStatus((mainViewModel.previewSession.value.lastRmbgCandidateError ?: "未安装 RMBG 组件"))
                return
            }
            if (mainViewModel.previewSession.value.isGeneratingRmbgCandidate || mainViewModel.previewSession.value.isGeneratingGptCandidate || mainViewModel.shell.value.isBusy) {
                setStatus(("RMBG正在运行或主任务忙，请等待"))
                return
            }
            if (!confirmed) {
                run {

                mainViewModel.updateConfirm { it -> it.copy(pendingServiceConfirm = (ServiceConfirmRequest(
                    title = ("使用 RMBG 抠图"),
                    message = ("将运行本地 ONNX 模型抠图（已累计 ${mainViewModel.presetUi.value.rmbgRunCount} 次）。确认继续？"),
                    confirmLabel = ("继续"),
                    onConfirm = ({
                        applyRmbgCandidateForMode(mode, confirmed = true)
                    }),
                ))) }
    }
                return
            }
            if (!rmbgGenerationGate.compareAndSet(false, true)) {
                setStatus(("RMBG正在运行，请等待"))
                return
            }
            mainViewModel.updatePreviewSession { it -> it.copy(isGeneratingRmbgCandidate = (true)) }
            run {

                mainViewModel.updatePresetUi { it -> it.copy(rmbgRunCount = it.rmbgRunCount + (1)) }
                getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putInt(MainActivity.PREF_RMBG_RUN_COUNT, mainViewModel.presetUi.value.rmbgRunCount)
                    .apply()
    }
            mainViewModel.updatePreviewSession { v -> v.copy(lastRmbgCandidateError = (null), rmbgCandidatePackageName = (session.packageName), rmbgCandidateMode = (mode), rmbgCandidateStatusText = ("RMBG运行中(${RmbgInferenceBackend.Cpu.label})，请等待: ${mode.label}"), rmbgCandidateFailurePackageName = (null), rmbgCandidateFailureMode = (null)) }
            setStatus(("RMBG候选生成中(${RmbgInferenceBackend.Cpu.label}): ${session.packageName}"))
            val selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark).withChoice(mode, PreviewChoice.Rmbg)
            run {

                mainViewModel.launchUiFriendly(("ArtPlusRmbgCandidate"), ({
                    try {
                        val source = resizeBitmap(session.sourceIcon, SIZE_1X1, SIZE_1X1)
                        val result = run {

                    val params = mainViewModel.params.value
                    return@run buildRmbgCandidate(
                        sourceIcon = (source),
                        filesDir = filesDir,
                        rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
                        rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
                        rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
                        rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
                        lock = __actF10,
                        getRuntime = { rmbgRuntime },
                        setRuntime = { rmbgRuntime = it },
                    )
        }
                            ?: error("未安装 RMBG 组件 ZIP")
                        val candidate = result.candidate ?: error("RMBG候选为空")
                        val inferenceReport = result.rmbgInference
                        val updatedSession = session.copy(
                            candidates = session.candidates + (PreviewChoice.Rmbg to candidate),
                        )
                        run {

                    val params = mainViewModel.params.value
                    writePackageOutputs(
                        session = (updatedSession),
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
        }
                        if (false && mainViewModel.shell.value.outputTreeUri != null) {
                            exportToTree(contentResolver, mainViewModel.shell.value.outputTreeUri, updatedSession.outDir)
                        }
                        runOnUiThread {
                            mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (updatedSession)) }
                            mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                            mainViewModel.updatePreviewSession { v -> v.copy(previewVersion = v.previewVersion + (1), lastRmbgCandidateError = (null), lastRmbgInferenceReport = (inferenceReport), rmbgCandidateFailurePackageName = (null), rmbgCandidateFailureMode = (null)) }
                            val msg = if (result.validationWarning != null) {
                                "${result.validationWarning}，已应用到 ${mode.label}: ${run {

                    if ((inferenceReport) == null) {
                        return@run RmbgInferenceBackend.Cpu.label
                    }
                    return@run buildString {
                        append((inferenceReport).actualBackend.label)
                        append(" ")
                        append((inferenceReport).elapsedMs)
                        append("ms")
                    }
        }}"
                            } else {
                                "RMBG候选已生成并应用到 ${mode.label}: ${run {

                    if ((inferenceReport) == null) {
                        return@run RmbgInferenceBackend.Cpu.label
                    }
                    return@run buildString {
                        append((inferenceReport).actualBackend.label)
                        append(" ")
                        append((inferenceReport).elapsedMs)
                        append("ms")
                    }
        }}"
                            }
                            setStatus((msg))
                            Toast.makeText(__actF10, msg, Toast.LENGTH_SHORT).show()
                            run {
            persistUiState()
        }
                        }
                    } catch (error: Throwable) {
                        val message = run {

                    val root = run {

                        var current = ((error))
                        while (current is InvocationTargetException && current.targetException != null) {
                            current = current.targetException
                        }
                        return@run current
            }
                    val raw = root.message ?: root.javaClass.simpleName
                    val lower = raw.lowercase()
                    return@run when {
                        root is OutOfMemoryError ||
                            "outofmemory" in lower ||
                            "failed to allocate" in lower ||
                            "memory" in lower -> {
                            "内存不足或 ONNX 分配失败；已释放会话但 RMBG-2.0 峰值仍较高"
                        }
                        "未通过校验" in raw -> {
                            raw
                        }
                        "reshape" in lower || "shape" in lower || "invalid dimensions" in lower -> {
                            "模型输入尺寸不匹配；当前 RMBG-2.0 ONNX 组件需要 1024 推理分辨率"
                        }
                        else -> raw
                    }
        }
                        runOnUiThread {
                            mainViewModel.updatePreviewSession { v -> v.copy(lastRmbgCandidateError = (message), rmbgCandidateFailurePackageName = (session.packageName), rmbgCandidateFailureMode = (mode)) }
                            val msg = "RMBG候选失败(${RmbgInferenceBackend.Cpu.label}): $message"
                            setStatus((msg))
                            Toast.makeText(__actF10, msg, Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        rmbgGenerationGate.set(false)
                        runOnUiThread {
                            mainViewModel.updatePreviewSession { v -> v.copy(isGeneratingRmbgCandidate = (false), rmbgCandidatePackageName = (null), rmbgCandidateMode = (null), rmbgCandidateStatusText = ("")) }
                        }
                    }
                }))
    }
}

private fun MainActivity.generateSelectedApp(installWithRoot: Boolean, useGpt: Boolean, rootWriteMode: RootWriteMode = RootWriteMode.All, confirmed: Boolean = false): Unit {
    val __actF12 = this
    return homeGenerateSelected(
                    entry = apps.firstOrNull { it.packageName == mainViewModel.picker.value.selectedPackageName },
                    installWithRoot = installWithRoot,
                    useGpt = useGpt,
                    rootWriteMode = rootWriteMode,
                    confirmed = confirmed,
                    gptApiKey = mainViewModel.gptRmbgSettings.value.gptApiKey,
                    gptBaseUrl = mainViewModel.gptRmbgSettings.value.gptBaseUrl,
                    isBusy = mainViewModel.shell.value.isBusy,
                    gptRunCount = mainViewModel.presetUi.value.gptRunCount,
                    onStatusText = { setStatus((it)) },
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
                    onBeginBusy = { gpt ->
                        mainViewModel.updateShell { it -> it.copy(isBusy = (true)) }
                        if (gpt) {
                            mainViewModel.updatePreviewSession { it -> it.copy(isGptPreviewLoading = (true)) }
                            run {

                    mainViewModel.updatePresetUi { it -> it.copy(gptRunCount = it.gptRunCount + (1)) }
                    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit()
                        .putInt(MainActivity.PREF_GPT_RUN_COUNT, mainViewModel.presetUi.value.gptRunCount)
                        .apply()
        }
                        }
                    },
                    onLaunch = { name, block -> run {

                    mainViewModel.launchUiFriendly((name), (block))
        } },
                    onGenerate = { e, g -> run {

                    val icon = (e).applicationInfo.loadIcon(packageManager)
                    return@run generateArtPlusPackage(
                        app = (e),
                        useGpt = (g),
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

                        pickerPostStatus(__a0) { runOnUiThread { setStatus((it)) } }
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
                    onPostGenerate = { result, e ->
                        runOnUiThread {
                            mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (result.session)) }
                            mainViewModel.updateLive { p -> p.copy(previewNormalLight = (result.selections).normalLight.name, previewNormalDark = (result.selections).normalDark.name, previewMonochromeLight = (result.selections).monochromeLight.name, previewMonochromeDark = (result.selections).monochromeDark.name) }
                            mainViewModel.updatePreviewSession { v -> v.copy(previewChoiceMode = (null), previewPackageName = (e.packageName), previewDirPath = (result.outDir.absolutePath)) }
                            mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                            run {
            persistUiState()
        }
                        }
                    },
                    onInstall = { outDir, pkg, mode -> installWithRoot(outDir, pkg, mode) },
                    onMarkGenerated = { pkg -> mainViewModel.updatePicker { it -> it.copy(generatedPackageNames = (markPackageGenerated(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), mainViewModel.picker.value.generatedPackageNames, pkg))) } },
                    onToast = { run {
            pickerToastStatus(
                        message = (it),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__actF12, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        } },
                    onStatus = { run {

                    pickerPostStatus((it)) { runOnUiThread { setStatus((it)) } }
        } },
                    onFinish = { gpt ->
                        runOnUiThread {
                            mainViewModel.updateShell { it -> it.copy(isBusy = (false)) }
                            if (gpt) {
                                mainViewModel.updatePreviewSession { it -> it.copy(isGptPreviewLoading = (false)) }
                            }
                        }
                    },
                    onConfirmedRetry = { root: Boolean, gpt: Boolean, mode: RootWriteMode -> generateSelectedApp(root, gpt, mode, confirmed = true) },
                )
}
