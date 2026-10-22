package dev.artplus.mobile

import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.blur.LocalBackdrop
import io.github.suqi8.coui.kmp.blur.rememberLayerBackdrop
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import io.github.suqi8.coui.kmp.basic.ScrollBehavior
import io.github.suqi8.coui.kmp.basic.Scaffold
import io.github.suqi8.coui.kmp.basic.TopAppBar
import io.github.suqi8.coui.kmp.basic.TopAppBarDefaults
import io.github.suqi8.coui.kmp.theme.COUITheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun MainActivity.ArtPlusScreen() {
    val tuningState = mainViewModel.params.collectAsState().value
    // Phase 5 回归修复（FAIL-4/5）：presetUi/confirm 经 collectAsState 订阅，
    // 渲染位禁止裸读 .value（裸读不触发重组，对话框显隐/消除全部假死）。
    val presetUiState by mainViewModel.presetUi.collectAsState()
    val confirmState by mainViewModel.confirm.collectAsState()
    // 热修复：picker/shell/previewSession 渲染位必须订阅（StateFlow .value 裸读不触发重组，
    // 选择器高亮/selectedApp/生成按钮全部假死，仅 magisk 初始态可见）。
    // 回调内（onClick/onConfirm 等事件时）读 .value 仍合法，此处只修组合期渲染读。
    val pickerState by mainViewModel.picker.collectAsState()
    val shellState by mainViewModel.shell.collectAsState()
    val previewSessionState by mainViewModel.previewSession.collectAsState()
    // Slice 3.4b：非组合回调（onStop 等）内的 Toast 共用此前捕获的 Context。
    val __actCancel = LocalContext.current
    // ColorOS 17 页面底色即 surface（浅色 F0F1F2 / 深色 000000），与 TopAppBar、Scaffold 默认色一致。
    val pageBackground = COUITheme.colorScheme.surface
    // 热修复：selectedApp/generatedCount 直接经已订阅 pickerState 计算。
    // 原 remember{derivedStateOf{...picker.value...}} 对 StateFlow 不可观察，永不重算，
    // selectedApp 恒定初始值（magisk），点别的行写成功但 UI 不刷、生成页 stale。
    val selectedApp = apps.firstOrNull { it.packageName == pickerState.selectedPackageName }
    val generatedCount = apps.count { it.packageName in pickerState.generatedPackageNames && AppVisibility.shouldShowInPicker(it.applicationInfo, it.launchable, pickerState.showSystemApps, packageName) }
    val launcherCount = apps.count { it.launchable }
    var systemBackProgress by remember { mutableStateOf(0f) }
    val completingBackProgress = remember { Animatable(0f) }
    val cancellingBackProgress = remember { Animatable(0f) }
    val childEnterProgress = remember { Animatable(1f) }
    var isCompletingBackGesture by remember { mutableStateOf(false) }
    val screenScope = rememberCoroutineScope()
    val backProgress = maxOf(
        systemBackProgress,
        completingBackProgress.value,
        cancellingBackProgress.value,
    )
    val sharedPreviewSession = previewSessionState.activeGenerationSession?.takeIf {
        it.packageName == previewSessionState.previewPackageName && it.outDir.absolutePath == previewSessionState.previewDirPath
    }
    val sharedPreviewTuning = run {
    mainViewModel.params.value
}

    LaunchedEffect(
        previewSessionState.previewPackageName,
        previewSessionState.previewDirPath,
        sharedPreviewSession,
        PreviewSelections.fromNames(tuningState.previewNormalLight, tuningState.previewNormalDark, tuningState.previewMonochromeLight, tuningState.previewMonochromeDark),
        previewSessionState.previewVersion,
        sharedPreviewTuning,
    ) {
        val dirPath = previewSessionState.previewDirPath
        val packageName = previewSessionState.previewPackageName
        if (dirPath.isNullOrBlank() || packageName.isNullOrBlank()) {
            mainViewModel.updatePreviewSession { it -> it.copy(sharedPreviewAssets = (null)) }
            mainViewModel.updatePreviewSession { it -> it.copy(isPreviewAssetsRefreshing = (false)) }
            return@LaunchedEffect
        }
        mainViewModel.updatePreviewSession { it -> it.copy(isPreviewAssetsRefreshing = (true)) }
        try {
            delay(PREVIEW_LIVE_ASSET_DEBOUNCE_MS)
            val diskAssets = withContext(Dispatchers.IO) {
                run {
    PreviewAssets(
                recbg = run {
        BitmapFactory.decodeFile(File(((File(dirPath))), ("recbg.png")).absolutePath)?.also { it.prepareToDraw() }
    },
                recfg = run {
        BitmapFactory.decodeFile(File(((File(dirPath))), ("recfg.png")).absolutePath)?.also { it.prepareToDraw() }
    },
                recNight = run {
        BitmapFactory.decodeFile(File(((File(dirPath))), ("rec_night.png")).absolutePath)?.also { it.prepareToDraw() }
    },
                monochromeLight = run {
        BitmapFactory.decodeFile(File(((File(dirPath))), ("monochrome_light.png")).absolutePath)?.also { it.prepareToDraw() }
    }
                    ?: run {
        BitmapFactory.decodeFile(File(((File(dirPath))), ("monochrome.png")).absolutePath)?.also { it.prepareToDraw() }
    },
                monochromeDark = run {
        BitmapFactory.decodeFile(File(((File(dirPath))), ("monochrome_dark.png")).absolutePath)?.also { it.prepareToDraw() }
    }
                    ?: run {
        BitmapFactory.decodeFile(File(((File(dirPath))), ("monochrome.png")).absolutePath)?.also { it.prepareToDraw() }
    },
            )
}.preparedForDraw()
            }
            val liveAssets = sharedPreviewSession?.let { session ->
                try {
                    withContext(previewWorkerDispatcher) {
                        run {

            val params = mainViewModel.params.value
            return@run previewAssetsForSelections(
                session = (session),
                selections = (PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark)),
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
                } catch (_: CancellationException) {
                    throw CancellationException()
                } catch (_: Throwable) {
                    null
                }
            }
            mainViewModel.updatePreviewSession { it -> it.copy(sharedPreviewAssets = (liveAssets ?: diskAssets)) }
        } catch (_: CancellationException) {
            throw CancellationException()
        } catch (_: Throwable) {
            mainViewModel.updatePreviewSession { it -> it.copy(sharedPreviewAssets = (null)) }
        } finally {
            mainViewModel.updatePreviewSession { it -> it.copy(isPreviewAssetsRefreshing = (false)) }
        }
    }

    fun completeBackFrom(progress: Float) {
        if (isCompletingBackGesture) {
            return
        }
        val start = progress.coerceIn(0f, 1f)
        isCompletingBackGesture = true
        mainViewModel.updatePreviewSession { it -> it.copy(skipNextHomeReturnAnimation = (true)) }
        screenScope.launch {
            completingBackProgress.snapTo(start)
            cancellingBackProgress.snapTo(0f)
            completingBackProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = ((1f - start) * 220f).roundToInt().coerceIn(90, 220)),
            )
            mainViewModel.updateShell { it -> it.copy(currentPage = (AppPage.Home)) }
            delay(40)
            systemBackProgress = 0f
            completingBackProgress.snapTo(0f)
            cancellingBackProgress.snapTo(0f)
            isCompletingBackGesture = false
            mainViewModel.updatePreviewSession { it -> it.copy(skipNextHomeReturnAnimation = (false)) }
        }
    }

    LaunchedEffect(shellState.currentPage, previewSessionState.skipNextHomeReturnAnimation, isCompletingBackGesture) {
        if (shellState.currentPage != AppPage.Home) {
            if (!isCompletingBackGesture) {
                systemBackProgress = 0f
                completingBackProgress.snapTo(0f)
                cancellingBackProgress.snapTo(0f)
                childEnterProgress.snapTo(1f)
                childEnterProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 300),
                )
            }
            mainViewModel.updatePreviewSession { it -> it.copy(skipNextHomeReturnAnimation = (false)) }
        } else if (previewSessionState.skipNextHomeReturnAnimation || isCompletingBackGesture) {
            delay(90)
            if (!isCompletingBackGesture) {
                systemBackProgress = 0f
                completingBackProgress.snapTo(0f)
                cancellingBackProgress.snapTo(0f)
                mainViewModel.updatePreviewSession { it -> it.copy(skipNextHomeReturnAnimation = (false)) }
            }
        } else {
            systemBackProgress = 0f
            completingBackProgress.snapTo(0f)
            cancellingBackProgress.snapTo(0f)
            childEnterProgress.snapTo(1f)
        }
    }

    PredictiveBackHandler(enabled = shellState.currentPage != AppPage.Home && !isCompletingBackGesture) { backEvents ->
        var latestProgress = systemBackProgress
        try {
            backEvents.collect { backEvent ->
                latestProgress = backEvent.progress.coerceIn(0f, 1f)
                cancellingBackProgress.snapTo(0f)
                systemBackProgress = latestProgress
            }
            completeBackFrom(maxOf(latestProgress, systemBackProgress, BACK_GESTURE_COMMIT_PROGRESS))
        } catch (_: CancellationException) {
            val start = maxOf(latestProgress, systemBackProgress).coerceIn(0f, 1f)
            systemBackProgress = 0f
            if (start > 0f && shellState.currentPage != AppPage.Home && !isCompletingBackGesture) {
                cancellingBackProgress.snapTo(start)
                cancellingBackProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = (start * 180f).roundToInt().coerceIn(70, 180)),
                )
            } else {
                cancellingBackProgress.snapTo(0f)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val overlayPage = shellState.currentPage.takeIf { it != AppPage.Home }
        val dimReveal = if (previewSessionState.skipNextHomeReturnAnimation || isCompletingBackGesture || backProgress > 0f) {
            backProgress.coerceIn(0f, 1f)
        } else {
            0f
        }
        val overlayDimTarget = ((1f - childEnterProgress.value) * (1f - dimReveal)).coerceIn(0f, 1f)
        // 返回主页时平滑回弹（修主页抖动）；进入动画与返回手势跟手时保持实时
        val overlayDimSpec: FiniteAnimationSpec<Float> =
            if (overlayPage == null || (backProgress == 0f && !isCompletingBackGesture && childEnterProgress.value == 0f)) {
                tween(durationMillis = 280)
            } else {
                snap()
            }
        val overlayDim by animateFloatAsState(
            targetValue = overlayDimTarget,
            animationSpec = overlayDimSpec,
            label = "OverlayDim",
        )
        val overlayShadowPx = with(LocalDensity.current) { 24.dp.toPx() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val homeScale = 1f - 0.04f * overlayDim
                    scaleX = homeScale
                    scaleY = homeScale
                },
        ) {
            HomePage(
                pageBackground = pageBackground,
                selectedApp = selectedApp,
                launcherCount = launcherCount,
                totalCount = apps.size,
                generatedCount = generatedCount,
            )
        }
        if (overlayPage != null && overlayDim > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        COUITheme.colorScheme.windowDimming.let { it.copy(alpha = it.alpha * overlayDim) },
                    ),
            )
        }

        if (overlayPage != null) {
            val isCompletingBack = previewSessionState.skipNextHomeReturnAnimation || isCompletingBackGesture
            val reveal = if (isCompletingBack || backProgress > 0f) {
                backProgress.coerceIn(0f, 1f)
            } else {
                0f
            }
            val overlayCover = ((1f - childEnterProgress.value) * (1f - reveal)).coerceIn(0f, 1f)
            val overlayCorner = (40f * overlayCover).dp
            val overlayShape = RoundedCornerShape(topStart = overlayCorner, bottomStart = overlayCorner)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val enteringOffset = size.width * childEnterProgress.value
                        val leavingOffset = size.width * reveal * BACK_GESTURE_PAGE_TRANSLATION_RATIO
                        translationX = enteringOffset + leavingOffset
                        shadowElevation = overlayShadowPx * overlayCover
                        shape = overlayShape
                        clip = true
                    },
            ) {
                when (overlayPage) {
                    AppPage.AppPicker -> {
                        // 热修复：经已订阅 pickerState 推导（原 remember{derivedStateOf{...picker.value...}}
                        // 对 StateFlow 不可观察，query/过滤/系统开关永不重算）。
                        val scopedApps = remember(apps.toList(), pickerState.showSystemApps) {
                            apps.filter { entry -> AppVisibility.shouldShowInPicker(entry.applicationInfo, entry.launchable, pickerState.showSystemApps, packageName) }
                        }
                        val ungeneratedCount = scopedApps.size - generatedCount
                        val filteredApps = remember(scopedApps, pickerState.queryText, pickerState.generatedFilter, pickerState.generatedPackageNames) {
                            val query = pickerState.queryText.trim().lowercase(Locale.ROOT)
                            val stateScopedApps = when (pickerState.generatedFilter) {
                                GeneratedFilter.All -> scopedApps
                                GeneratedFilter.Generated -> scopedApps.filter { it.packageName in pickerState.generatedPackageNames }
                                GeneratedFilter.Ungenerated -> scopedApps.filter { it.packageName !in pickerState.generatedPackageNames }
                            }
                            if (query.isEmpty()) {
                                stateScopedApps
                            } else {
                                stateScopedApps.filter { entry ->
                                    entry.label.lowercase(Locale.ROOT).contains(query) ||
                                        entry.packageName.lowercase(Locale.ROOT).contains(query)
                                }
                            }
                        }
                        val scopeCount = when (pickerState.generatedFilter) {
                            GeneratedFilter.All -> scopedApps.size
                            GeneratedFilter.Generated -> generatedCount
                            GeneratedFilter.Ungenerated -> ungeneratedCount
                        }
                        AppPickerPage(
                            pageBackground = pageBackground,
                            filteredApps = filteredApps,
                            scopeCount = scopeCount,
                            generatedCount = generatedCount,
                            ungeneratedCount = ungeneratedCount,
                        )
                    }

                    AppPage.About -> AboutPage(pageBackground = pageBackground)

                    AppPage.BatchPreview -> BatchPreviewPage(pageBackground = pageBackground)

                    AppPage.Home -> Unit
                }
            }
        }

        mainViewModel.transfer.value.batchApplyProgress?.let { progress ->
            BatchApplyProgressDialog(progress)
        }
        if (mainViewModel.transfer.value.singleExportSheetVisible && mainViewModel.transfer.value.exportProgress != null) {
            val p = mainViewModel.transfer.value.exportProgress!!
            BackupProgressBottomSheet(
                progress = p,
                onStop = { run {

            val state = SingleExportCancelState(
                singleExportJob = singleExportJob,
                sheetVisible = mainViewModel.transfer.value.singleExportSheetVisible,
                progress = mainViewModel.transfer.value.exportProgress,
            )
            cancelSingleExport(state, { __a0: String -> run {
        pickerToastStatus(
                    message = __a0,
                    postOnUi = { text -> runOnUiThread { mainViewModel.updateShell { it -> it.copy(statusText = (text)) } } },
                    showToast = { text ->
                        runOnUiThread {
                            Toast.makeText(__actCancel, text, Toast.LENGTH_SHORT).show()
                        }
                    },
                )
    } })
            singleExportJob = state.singleExportJob
            mainViewModel.updateTransfer { it -> it.copy(singleExportSheetVisible = (state.sheetVisible)) }
            mainViewModel.updateTransfer { it -> it.copy(exportProgress = (state.progress)) }
} },
                onBackground = {
                    // 单包导出后台：仅隐藏弹窗，任务继续（不显示设置页动效）
                    mainViewModel.updateTransfer { it -> it.copy(singleExportSheetVisible = (false)) }
                },
            )
        }
        if (mainViewModel.transfer.value.backupSheetVisible && mainViewModel.transfer.value.backupProgress != null) {
            val p = mainViewModel.transfer.value.backupProgress!!
            BackupProgressBottomSheet(
                progress = p,
                onStop = { run {

            val state = BackupCancelState(
                backupJob = backupJob,
                backupDotJob = backupDotJob,
                sheetVisible = mainViewModel.transfer.value.backupSheetVisible,
                inBackground = mainViewModel.transfer.value.backupInBackground,
                progress = mainViewModel.transfer.value.backupProgress,
                isBusy = mainViewModel.shell.value.isBusy,
            )
            cancelBackup(state, { __a0: String -> run {
        pickerToastStatus(
                    message = __a0,
                    postOnUi = { text -> runOnUiThread { mainViewModel.updateShell { it -> it.copy(statusText = (text)) } } },
                    showToast = { text ->
                        runOnUiThread {
                            Toast.makeText(__actCancel, text, Toast.LENGTH_SHORT).show()
                        }
                    },
                )
    } })
            backupJob = state.backupJob
            backupDotJob = state.backupDotJob
            mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (state.sheetVisible)) }
            mainViewModel.updateTransfer { it -> it.copy(backupInBackground = (state.inBackground)) }
            mainViewModel.updateTransfer { it -> it.copy(backupProgress = (state.progress)) }
            mainViewModel.updateShell { it -> it.copy(isBusy = (state.isBusy)) }
} },
                onBackground = {
                    mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (false)) }
                    mainViewModel.updateTransfer { it -> it.copy(backupInBackground = (true)) }
                    run {

            backupDotJob?.cancel()
            backupDotJob = mainScope.launch {
                while (isActive) {
                    delay(500)
                    mainViewModel.updateTransfer { it -> it.copy(backupBackgroundDots = (if (mainViewModel.transfer.value.backupBackgroundDots >= 3) 1 else mainViewModel.transfer.value.backupBackgroundDots + 1)) }
                }
            }
}
                },
            )
        } else if (!mainViewModel.transfer.value.backupSheetVisible && mainViewModel.transfer.value.backupInBackground && mainViewModel.transfer.value.backupProgress != null) {
            // 后台态不显示底部弹窗，但保留状态供设置页“备份中...”展示
        }

        confirmState.pendingServiceConfirm?.let { request ->
            ServiceConfirmDialog(
                request = request,
                onConfirm = { run {

            val request = mainViewModel.confirm.value.pendingServiceConfirm ?: return@run
            mainViewModel.updateConfirm { it -> it.copy(pendingServiceConfirm = (null)) }
            if ((true)) {
                request.onConfirm()
            }
} },
                onDismiss = { run {

            val request = mainViewModel.confirm.value.pendingServiceConfirm ?: return@run
            mainViewModel.updateConfirm { it -> it.copy(pendingServiceConfirm = (null)) }
            if ((false)) {
                request.onConfirm()
            }
} },
            )
        }
        run {
    RootWriteConfirmDialog(
                request = confirmState.pendingRootWriteConfirm,
                rememberSkip = confirmState.rootWriteConfirmRememberSkip,
                onDismiss = { mainViewModel.updateConfirm { it -> it.copy(pendingRootWriteConfirm = (null)) } },
                onToggleSkip = { mainViewModel.updateConfirm { it -> it.copy(rootWriteConfirmRememberSkip = (!mainViewModel.confirm.value.rootWriteConfirmRememberSkip)) } },
                onConfirm = { request, shouldSkip ->
                    val onConfirm = request.onConfirm
                    mainViewModel.updateConfirm { it -> it.copy(pendingRootWriteConfirm = (null)) }
                    if (shouldSkip) {
                        mainViewModel.updateConfirm { it -> it.copy(autoConfirmRootWrite = (true)) }
                        run {
        persistUiState()
    }
                    }
                    onConfirm()
                },
            )
}
        run {

            // Slice 3.1: Activity侧collect读VM单源；写经薄wrapper（重构期间保留）。
            // Phase 5：confirmState 已提升至 ArtPlusScreen 顶层，此处复用（删重复订阅）。
            RefreshConfirmDialog(
                visible = confirmState.refreshConfirmVisible,
                rememberAuto = confirmState.refreshConfirmRememberAuto,
                onDismiss = { mainViewModel.updateConfirm { it -> it.copy(refreshConfirmVisible = (false)) } },
                onToggleRemember = { mainViewModel.updateConfirm { it -> it.copy(refreshConfirmRememberAuto = (!mainViewModel.confirm.value.refreshConfirmRememberAuto)) } },
                onConfirm = { shouldAuto ->
                    mainViewModel.updateConfirm { it -> it.copy(refreshConfirmVisible = (false)) }
                    if (shouldAuto) {
                        mainViewModel.updateConfirm { it -> it.copy(autoConfirmRefresh = (true)) }
                        run {
        persistUiState()
    }
                    }
                    run {
        mainViewModel.refreshArtPlusIconsAsync(
                    contentResolver = contentResolver,
                    apkPath = applicationInfo.sourceDir,
                )
    }
                },
            )
}
        run {
    PresetPageDialogs(
                saveDialogVisible = presetUiState.presetSaveDialogVisible,
                saveInitialName = presetUiState.presetSaveName,
                onSaveConfirm = { name ->
                    mainViewModel.updatePresetUi { it -> it.copy(presetSaveDialogVisible = (false)) }
                    run {
        saveCurrentAsPreset(
                    rawName = (name),
                    store = presetStore,
                    current = run {
            mainViewModel.params.value
        },
                    viewModel = mainViewModel,
                    onSaved = { preset, msg ->
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (preset.id)) }
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetBaseParams = (preset.params)) }
                        mainViewModel.updatePresetUi { it -> it.copy(presetListVersion = it.presetListVersion + (1)) }
                        mainViewModel.updatePresetUi { it -> it.copy(presetSaveDialogVisible = (false)) }
                        mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
                    },
                    onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                )
    }
                },
                onSaveDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetSaveDialogVisible = (false)) } },
                renameTarget = presetUiState.presetRenameTarget,
                onRenameConfirm = { id, name ->
                    mainViewModel.updatePresetUi { it -> it.copy(presetRenameTarget = (null)) }
                    run {
        renamePreset(
                    id = (id),
                    rawName = (name),
                    store = presetStore,
                    viewModel = mainViewModel,
                    onRenamed = { _, msg ->
                        mainViewModel.updatePresetUi { it -> it.copy(presetListVersion = it.presetListVersion + (1)) }
                        mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
                    },
                    onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                )
    }
                },
                onRenameDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetRenameTarget = (null)) } },
                actionMenuTarget = presetUiState.presetActionMenuTarget,
                actionMenuBusy = mainViewModel.shell.value.isBusy,
                onActionMenuDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetActionMenuTarget = (null)) } },
                onActionApply = { run {
        applyPreset(
                    preset = (it),
                    isBusy = mainViewModel.shell.value.isBusy,
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
                        mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
                    },
                    onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                )
    } },
                onActionPreview = { run {
        mainViewModel.openBatchPreviewForPreset(preset = (it), filesDir = filesDir)
    } },
                onActionOverwrite = { run {
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
                        mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
                    },
                    onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                )
    } },
                onActionRename = { mainViewModel.updatePresetUi { v -> v.copy(presetRenameTarget = (it)) } },
                onActionExportSingle = { run {
        exportSinglePresetToClipboard(
                    preset = (it),
                    store = presetStore,
                    clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager,
                    onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                )
    } },
                onActionDelete = { mainViewModel.updatePresetUi { v -> v.copy(presetDeleteConfirmTarget = (it)) } },
                deleteConfirmTarget = presetUiState.presetDeleteConfirmTarget,
                onDeleteDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetDeleteConfirmTarget = (null)) } },
                onDeleteConfirm = { run {
        deletePreset(
                    id = (it),
                    filesDir = filesDir,
                    store = presetStore,
                    viewModel = mainViewModel,
                    activeBatchPreviewPresetId = mainViewModel.presetUi.value.activeBatchPreviewPreset?.id,
                    batchPreviewResultPresetId = mainViewModel.presetUi.value.batchPreviewResult?.preset?.id,
                    currentPage = mainViewModel.shell.value.currentPage,
                    activePresetId = mainViewModel.presetUi.value.activePresetId,
                    onBatchPreviewReset = {
                        mainViewModel.updatePresetUi { it -> it.copy(activeBatchPreviewPreset = (null)) }
                        mainViewModel.updatePresetUi { it -> it.copy(batchPreviewResult = (null)) }
                    },
                    onNavigateHome = { mainViewModel.updateShell { it -> it.copy(currentPage = (AppPage.Home)) } },
                    onActiveCleared = {
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (null)) }
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetBaseParams = (null)) }
                    },
                    onBumpVersion = { mainViewModel.updatePresetUi { it -> it.copy(presetListVersion = it.presetListVersion + (1)) } },
                    onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                )
    } },
                importDialogVisible = presetUiState.presetImportDialogVisible,
                onImportConfirm = { text -> run {
        importPresetsFromText(
                    text = (text),
                    store = presetStore,
                    onApplied = { msg ->
                        mainViewModel.updatePresetUi { it -> it.copy(presetImportDialogVisible = (false)) }
                        mainViewModel.updatePresetUi { it -> it.copy(presetImportText = ("")) }
                        mainViewModel.updatePresetUi { it -> it.copy(presetListVersion = it.presetListVersion + (1)) }
                        mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
                    },
                )
    } },
                onImportDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetImportDialogVisible = (false)) } },
                batchPreviewConfirmTarget = presetUiState.presetBatchPreviewConfirmTarget,
                onBatchPreviewConfirm = {
                    mainViewModel.updatePresetUi { it -> it.copy(presetBatchPreviewConfirmTarget = (null)) }
                    run {
        mainViewModel.startBatchPreview(
                    preset = (it),
                    apps = apps.toList(),
                    selfPackageName = packageName,
                    batchPreviewCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                    generatedPackageNames = mainViewModel.picker.value.generatedPackageNames,
                    originalParams = run {
            mainViewModel.params.value
        },
                    cacheDir = cacheDir,
                    loadIcon = { app -> app.applicationInfo.loadIcon(packageManager) },
                    defaultChoiceForMode = { mode, auto -> run {
            when ((mode)) {
                        LocalSeparationMode.Original -> PreviewChoice.Original
                        LocalSeparationMode.Plate -> PreviewChoice.Full
                        LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                        LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                        LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                        LocalSeparationMode.Auto -> (auto)
                        LocalSeparationMode.Full -> PreviewChoice.Full
                    }
        } },
                    composeAssets = { session, selections -> run {

                    val params = mainViewModel.params.value
                    return@run previewAssetsForSelections(
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
                        nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
                    )
        } },
                    onApplyMerged = { merged ->
                        run {
            paramsApplyTuningParams(
                        params = (merged),
                        rebuildCandidates = (false),
                        persist = (false),
                        captureUndo = (false),
                        refreshPreview = (false),
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
                    },
                    onRestoreOriginal = { original ->
                        run {
            paramsApplyTuningParams(
                        params = (original),
                        rebuildCandidates = (true),
                        persist = (false),
                        captureUndo = (false),
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
                    },
                    onSaveSnapshot = { p, dataList -> BatchPreviewStore.saveSnapshot(filesDir, p, dataList) },
                )
    }
                },
                onBatchPreviewConfirmDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetBatchPreviewConfirmTarget = (null)) } },
                batchPreviewProgress = presetUiState.batchPreviewProgress,
                onCancelBatchPreview = { mainViewModel.updatePresetUi { it -> it.copy(batchPreviewCancelled = (true)) } },
                showRefreshConfirm = presetUiState.showBatchPreviewRefreshConfirm,
                refreshConfirmPreset = presetUiState.activeBatchPreviewPreset ?: presetUiState.batchPreviewResult?.preset,
                batchPreviewCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                onRefreshConfirm = {
                    mainViewModel.updatePresetUi { it -> it.copy(showBatchPreviewRefreshConfirm = (false)) }
                    run {
        mainViewModel.startBatchPreview(
                    preset = (it),
                    apps = apps.toList(),
                    selfPackageName = packageName,
                    batchPreviewCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                    generatedPackageNames = mainViewModel.picker.value.generatedPackageNames,
                    originalParams = run {
            mainViewModel.params.value
        },
                    cacheDir = cacheDir,
                    loadIcon = { app -> app.applicationInfo.loadIcon(packageManager) },
                    defaultChoiceForMode = { mode, auto -> run {
            when ((mode)) {
                        LocalSeparationMode.Original -> PreviewChoice.Original
                        LocalSeparationMode.Plate -> PreviewChoice.Full
                        LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                        LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                        LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                        LocalSeparationMode.Auto -> (auto)
                        LocalSeparationMode.Full -> PreviewChoice.Full
                    }
        } },
                    composeAssets = { session, selections -> run {

                    val params = mainViewModel.params.value
                    return@run previewAssetsForSelections(
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
                        nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
                    )
        } },
                    onApplyMerged = { merged ->
                        run {
            paramsApplyTuningParams(
                        params = (merged),
                        rebuildCandidates = (false),
                        persist = (false),
                        captureUndo = (false),
                        refreshPreview = (false),
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
                    },
                    onRestoreOriginal = { original ->
                        run {
            paramsApplyTuningParams(
                        params = (original),
                        rebuildCandidates = (true),
                        persist = (false),
                        captureUndo = (false),
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
                    },
                    onSaveSnapshot = { p, dataList -> BatchPreviewStore.saveSnapshot(filesDir, p, dataList) },
                )
    }
                },
                onRefreshDismiss = { mainViewModel.updatePresetUi { it -> it.copy(showBatchPreviewRefreshConfirm = (false)) } },
            )
}
        run {
val __act1 = LocalContext.current

            // 热修复：shellState 已提升至 ArtPlusScreen 顶层，此处复用（删重复订阅）。
            // Slice 3.1: Activity侧collect读VM单源；写经薄wrapper（重构期间保留）。
            OnboardingDialog(
                visible = shellState.onboardingVisible,
                isBusy = shellState.isBusy,
                onSkip = {
                    // 允许通过外部点击关闭视为跳过
                    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                        .putBoolean(PREF_HAS_COMPLETED_ONBOARDING, true).apply()
                    mainViewModel.updateShell { it -> it.copy(onboardingVisible = (false)) }
                    run {
        pickerToastStatus(
                    message = ("已跳过，可在设置-导出引导中重新进入"),
                    postOnUi = { text -> runOnUiThread { mainViewModel.updateShell { it -> it.copy(statusText = (text)) } } },
                    showToast = { text ->
                        runOnUiThread {
                            Toast.makeText(__act1, text, Toast.LENGTH_SHORT).show()
                        }
                    },
                )
    }
                },
                onChooseDir = {
                    // 不在此关闭，等待 chooseTreeLauncher 回调中关闭
                    chooseTreeLauncher.launch(null)
                },
            )
}
    }
}

/**
 * KernelSU 式的分页壳：每页拥有独立的 TopAppBar（标题+按钮随本页左右滑动），
 * 页面滚动命中该页自身的 scrollBehavior 折叠顶栏。
 */
@Composable
internal fun MainActivity.PagerShellPage(
    title: String,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    showPreviewStrip: Boolean = false,
    content: @Composable (PaddingValues, ScrollBehavior) -> Unit,
) {
    val pageScrollBehavior = rememberLinkedTopAppBarScrollBehavior()
    var stripHeight by remember { mutableStateOf(0.dp) }
    val isDark = isSystemInDarkTheme()
    val targetExtra = if (showPreviewStrip) {
        if (stripHeight > 0.dp) stripHeight else 84.dp
    } else 0.dp
    val animatedExtra by animateDpAsState(
        targetValue = targetExtra,
        animationSpec = tween(durationMillis = 180),
        label = "previewStripExtra",
    )
    // Backdrop for the ColorOS app bar material: the page content only, so the bar never samples
    // its own pixels. Content scrolls under the bar through the lists' contentPadding.
    val pageSurface = COUITheme.colorScheme.surface
    val appBarBackdrop = rememberLayerBackdrop {
        drawRect(pageSurface)
        drawContent()
    }
    // Constant top inset (status bar + expanded bar) instead of the measured bar height: the bar
    // shrinks while collapsing, and a moving contentPadding would drag the list under the finger.
    val contentTopInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
        TopAppBarDefaults.ExpandedHeight
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CompositionLocalProvider(LocalBackdrop provides appBarBackdrop) {
                TopAppBar(
                    title = title,
                    scrollBehavior = pageScrollBehavior,
                    navigationIcon = navigationIcon,
                    actions = actions,
                    gradientGlass = true,
                    showDivider = false,
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(appBarBackdrop),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = animatedExtra),
            ) {
                content(PaddingValues(top = contentTopInset), pageScrollBehavior)
            }
            AnimatedVisibility(
                visible = showPreviewStrip,
                enter = fadeIn(animationSpec = tween(durationMillis = 150)) +
                    expandVertically(animationSpec = tween(durationMillis = 180)),
                exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                    shrinkVertically(animationSpec = tween(durationMillis = 160)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = innerPadding.calculateTopPadding())
                    .zIndex(1f),
            ) {
                HomePreviewStrip(
                    onHeightMeasured = { h -> if (h > 0.dp) stripHeight = h },
                )
            }
        }
    }
}
