package dev.artplus.mobile

import android.app.WallpaperManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Cpu
import java.lang.reflect.InvocationTargetException
import kotlinx.coroutines.launch
import io.github.suqi8.coui.kmp.basic.Card
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
 * S4.4：从 HomePage.kt 原样搬出的预览条与 6 个生成动作入口（纯移动，逻辑零改动）。
 * 保留在 ui/pages/home/ 下，按目录归属后续 S4.5 统一对齐包名。
 */

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

internal fun MainActivity.generateGptCandidate(confirmed: Boolean = false): Unit {
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
                        val gptLayers = generateGptLayers(session.sourceIcon, session.baseRecfg, session.baseRecbg, mainViewModel.params.value.gptCustomPrompt, GptPromptPreset.fromValue(mainViewModel.params.value.gptPromptPreset), mainViewModel.params.value.foregroundSubjectPercent, GptImageMode.fromValue(mainViewModel.params.value.gptImageMode), mainViewModel.gptRmbgSettings.value.gptModelId, mainViewModel.gptRmbgSettings.value.gptBaseUrl, mainViewModel.gptRmbgSettings.value.gptApiKey, (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0, { __a0: String -> pickerPostStatus(__a0) { runOnUiThread { setStatus((it)) } } })
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
                            persistUiState()
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

internal fun MainActivity.applyPreviewChoiceToAll(choice: PreviewChoice): Unit {
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
                        hasRmbgComponent = findRmbgComponent(filesDir) != null,
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
                        onProgress = { completed, total, label, failures ->
                mainViewModel.updateBatchApplyProgress(
                        completed = (completed),
                        total = (total),
                        currentLabel = (label),
                        failures = (failures),
                        title = "全部应用",
                    )
             },
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
                            defaultLocal = { auto ->
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
                 },
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
                                    persistUiState()
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
                        onLaunch = { name, block -> mainViewModel.launchUiFriendly((name), (block)) },
                    )
        } },
                    onGenerateGptAll = { generateGptCandidate() },
                    onGenerateRmbgAll = { generateRmbgCandidate() },
                    onStatusText = { setStatus((it)) },
                    candidateAvailable = { s, c -> candidateForChoice(s, c) != null },
                    onCommitDefault = { selections -> mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) } },
                    onClearChoiceMode = { mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) } },
                    onSaveUi = { persistUiState() },
                    onWriteClose = { session, selections ->
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
                            persistUiState()
                        },
                        onStatus = { pickerPostStatus((it)) { runOnUiThread { setStatus((it)) } } },
                    )
         },
                )
}

internal fun MainActivity.generateRmbgCandidate(confirmed: Boolean = false): Unit {
    val __actF6 = this
            val session = mainViewModel.previewSession.value.activeGenerationSession ?: return
            if (session.candidates[PreviewChoice.Rmbg] != null) {
                applyPreviewChoiceToAll(PreviewChoice.Rmbg)
                setStatus(("已使用现有 RMBG 候选"))
                return
            }
            if (findRmbgComponent(filesDir) == null) {
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
                            persistUiState()
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

internal fun MainActivity.generateGptCandidateForMode(mode: PreviewMode, confirmed: Boolean = false): Unit {
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
                        val gptLayers = generateGptLayers(session.sourceIcon, session.baseRecfg, session.baseRecbg, mainViewModel.params.value.gptCustomPrompt, GptPromptPreset.fromValue(mainViewModel.params.value.gptPromptPreset), mainViewModel.params.value.foregroundSubjectPercent, GptImageMode.fromValue(mainViewModel.params.value.gptImageMode), mainViewModel.gptRmbgSettings.value.gptModelId, mainViewModel.gptRmbgSettings.value.gptBaseUrl, mainViewModel.gptRmbgSettings.value.gptApiKey, (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0, { __a0: String -> pickerPostStatus(__a0) { runOnUiThread { setStatus((it)) } } })
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
                            persistUiState()
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

internal fun MainActivity.applyRmbgCandidateForMode(mode: PreviewMode, confirmed: Boolean = false): Unit {
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
                    onSaveUi = { persistUiState() },
                    onWrite = tuningActions.write,
                )
    }
                setStatus(("已使用现有 RMBG 候选"))
                return
            }
            if (findRmbgComponent(filesDir) == null) {
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
                            persistUiState()
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

internal fun MainActivity.generateSelectedApp(installWithRoot: Boolean, useGpt: Boolean, rootWriteMode: RootWriteMode = RootWriteMode.All, confirmed: Boolean = false): Unit {
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
                    mainViewModel.updateConfirm { it -> it.copy(pendingServiceConfirm = (ServiceConfirmRequest(
                        title = (title),
                        message = (message),
                        confirmLabel = (confirmLabel),
                        onConfirm = (onConfirm),
                    ))) }
        
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
                    onLaunch = { name, block -> mainViewModel.launchUiFriendly((name), (block)) },
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
                    onPostGenerate = { result, e ->
                        runOnUiThread {
                            mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (result.session)) }
                            mainViewModel.updateLive { p -> p.copy(previewNormalLight = (result.selections).normalLight.name, previewNormalDark = (result.selections).normalDark.name, previewMonochromeLight = (result.selections).monochromeLight.name, previewMonochromeDark = (result.selections).monochromeDark.name) }
                            mainViewModel.updatePreviewSession { v -> v.copy(previewChoiceMode = (null), previewPackageName = (e.packageName), previewDirPath = (result.outDir.absolutePath)) }
                            mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                            persistUiState()
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
                    onStatus = { pickerPostStatus((it)) { runOnUiThread { setStatus((it)) } } },
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
