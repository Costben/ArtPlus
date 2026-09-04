package dev.artplus.mobile

import android.graphics.drawable.Drawable
import androidx.lifecycle.viewModelScope
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Slice 3.2：预设业务操作与批量应用调度下沉至 MainViewModel。
 *
 * 从 MainActivity 原样搬迁（只搬名单 12 项）：
 * startBatchPreview / openBatchPreviewForPreset / generateMemoryPreviewAssetsForApp /
 * applyPresetToSelectedApps / executeApplyPresetToSelectedApps / applyCurrentPresetBatch /
 * executeApplyCurrentBatch / updateBatchApplyProgress / addLiquidGlassToSelectedGenerated /
 * toggleMultiSelectedPackage / addLiquidGlassToMultiSelectedGenerated /
 * executeAddLiquidGlassToMultiSelectedGenerated。
 * 内联 helper 扫描结论：名单函数引用的 helper（updateGeneratedPackageCache /
 * markPackageGenerated / existingGeneratedPackageDir / buildGeneratedPackageSession /
 * applyLiquidGlassToGeneratedPackage / installLiquidGlassFilesWithRoot / installWithRoot /
 * exportToTree / saveUiState / status / generateArtPlusPackage / requestServiceConfirm /
 * applyTuningParams / currentTuningParams / defaultPreviewChoiceForMode /
 * previewAssetsForSelections）均为跨簇共享（名单外另有引用），唯一独占引用
 * loadBatchPreviewSnapshot 属 pipeline/ 既有函数（禁动，原位调用）——无同簇独占
 * 内联 helper 需另搬，随本体迁移的只有 inline 映射/文案分支。
 *
 * 约定（沿用 Slice 3.1 分组槽位，同包直接用，不改默认值与更新顺序）：
 * - 进度/对话框只写 PresetUiState（batchPreview*）与 TransferState（batchApplyProgress），
 *   经 updatePresetUi / updateTransfer 漏斗；statusText 经 updateShell。
 * - 调度一律 viewModelScope 协程（Dispatchers.Default/IO 做后台段），替代 Activity 的
 *   mainScope/Thread 直调；Activity 侧 runOnUiThread 不再需要（StateFlow 写线程安全）。
 * - Root 写入与文件 IO（BatchPreviewStore.saveSnapshot / generateArtPlusPackage /
 *   existingGeneratedPackageDir + applyLiquidGlass + installLiquidGlassFilesWithRoot /
 *   updateGeneratedPackageCache / markPackageGenerated）只做调用点迁移：Context 绑定
 *   的部分经参数/回调由 Activity 注入，顺序与路径不变。原 execute* 内
 *   `if (false && outputUri != null)` 死分支恒不执行，迁移时省略，语义不变。
 * - 同名同参 wrapper 禁止：VM 侧方法一律扩参（显式依赖），Activity 留同名薄 wrapper。
 */

// ---------- 纯 helper（JVM 可测，无 Android 调用） ----------

/** 多选集合翻转（原 toggleMultiSelectedPackage 本体逻辑）。 */
internal fun toggleMultiSelection(current: Set<String>, packageName: String): Set<String> =
    if (packageName in current) current - packageName else current + packageName

/**
 * 预设批量包名解析（原 applyPresetToSelectedApps 头部逻辑）：
 * 多选非空取多选排序，否则取当前会话包单例，否则空。
 */
internal fun resolvePresetBatchPackages(
    multiSelected: Set<String>,
    sessionPackage: String?,
): List<String> =
    if (multiSelected.isNotEmpty()) {
        multiSelected.toList().sorted()
    } else {
        listOfNotNull(sessionPackage)
    }

/** 批量起始进度（原 execute* begin 块：raw 值，不做 coerce，与原构造一致）。 */
internal fun buildBatchApplyProgress(
    title: String,
    completed: Int,
    total: Int,
    currentLabel: String,
    failures: Int,
): BatchApplyProgress =
    BatchApplyProgress(
        title = title,
        completed = completed,
        total = total,
        currentLabel = currentLabel,
        failures = failures,
    )

/** 进度推进体（原 updateBatchApplyProgress 本体：coerce 语义逐字保留）。 */
internal fun coercedBatchApplyProgress(
    completed: Int,
    total: Int,
    currentLabel: String,
    failures: Int,
): BatchApplyProgress =
    BatchApplyProgress(
        title = "全部应用",
        completed = completed.coerceIn(0, total.coerceAtLeast(0)),
        total = total,
        currentLabel = currentLabel,
        failures = failures,
    )

/** 原 updateBatchApplyProgress 的 statusText 文案分支。 */
internal fun batchApplyProcessingStatus(completed: Int, total: Int): String =
    "全部应用处理中: ${completed.coerceAtMost(total)}/$total"

/** 原 executeApplyPresetToSelectedApps 收尾 statusText 文案分支。 */
internal fun presetBatchFinishStatus(
    presetName: String,
    successes: Int,
    total: Int,
    firstFailure: String?,
): String = when {
    firstFailure == null -> "预设「$presetName」批量完成: $successes/$total"
    successes == 0 -> "预设批量失败: ${firstFailure.orEmpty()}"
    else -> "预设批量完成 $successes 个，失败 ${total - successes} 个：${firstFailure.orEmpty()}"
}

/** 原 executeApplyCurrentBatch 收尾 statusText 文案分支。 */
internal fun currentBatchFinishStatus(
    successes: Int,
    total: Int,
    firstFailure: String?,
): String = when {
    firstFailure == null -> "按当前调参批量完成: $successes/$total"
    successes == 0 -> "批量失败: ${firstFailure.orEmpty()}"
    else -> "批量完成 $successes 个，失败 ${total - successes} 个：${firstFailure.orEmpty()}"
}

/** 原 executeAddLiquidGlassToMultiSelectedGenerated 收尾 statusText 文案分支。 */
internal fun liquidGlassBatchFinishStatus(
    successes: Int,
    failures: Int,
    firstFailure: String?,
): String = when {
    failures == 0 -> "已批量添加光影 $successes 个，未刷新，请手动点首页左上角刷新图标"
    successes == 0 -> "批量添加光影失败: ${firstFailure.orEmpty()}"
    else -> "已添加光影 $successes 个，失败 $failures 个: ${firstFailure.orEmpty()}"
}

// ---------- ViewModel 编排（viewModelScope 调度，对外暴露回调/suspend） ----------

/**
 * 多选翻转（原 toggleMultiSelectedPackage 本体下沉；扩参 current 显式传入）。
 * 纯状态写，无需协程。
 */
internal fun MainViewModel.toggleMultiSelectedPackage(packageName: String, current: Set<String>) {
    updatePicker { it.copy(multiSelectedPackageNames = toggleMultiSelection(current, packageName)) }
}

/**
 * 批量进度推进（原 updateBatchApplyProgress 本体下沉；扩参 title 显式传入，
 * 调用方传 "全部应用"；原 runOnUiThread 包裹省略，StateFlow 写线程安全）。
 */
internal fun MainViewModel.updateBatchApplyProgress(
    completed: Int,
    total: Int,
    currentLabel: String,
    failures: Int,
    title: String,
) {
    updateTransfer {
        it.copy(
            batchApplyProgress = coercedBatchApplyProgress(
                completed = completed,
                total = total,
                currentLabel = currentLabel,
                failures = failures,
            ).copy(title = title),
        )
    }
    updateShell { it.copy(statusText = batchApplyProcessingStatus(completed, total)) }
}

/**
 * 批量预览快照打开（原 openBatchPreviewForPreset 本体下沉；扩参 filesDir）。
 * 同步小 IO（manifest 读），无需协程；调用方在主线程。
 */
internal fun MainViewModel.openBatchPreviewForPreset(preset: TuningPreset, filesDir: File) {
    updatePresetUi { it.copy(activeBatchPreviewPreset = preset) }
    if (BatchPreviewStore.hasSnapshot(filesDir, preset.id)) {
        // P4 交界：快照读取收敛进 pipeline/，显式传 filesDir（原位调用，不搬）。
        val cached = loadBatchPreviewSnapshot(filesDir, preset)
        if (cached != null) {
            updatePresetUi { it.copy(batchPreviewResult = cached) }
            updateShell {
                it.copy(
                    currentPage = AppPage.BatchPreview,
                    statusText = "已加载预设「${preset.name}」批量预览快照",
                )
            }
            return
        }
    }
    updatePresetUi { it.copy(presetBatchPreviewConfirmTarget = preset) }
}

/**
 * 内存预览资源生成（原 generateMemoryPreviewAssetsForApp 本体下沉；suspend + 扩参）。
 * 调参快照/图标加载/默认选项映射/资源合成一律显式注入，VM 不读 Activity 字段；
 * imaging/ 同包函数原位直接调用，顺序与参数不变。
 */
internal suspend fun MainViewModel.generateMemoryPreviewAssetsForApp(
    app: AppEntry,
    pipeline: LocalPipelineConfig,
    tuning: TuningParams,
    cacheDir: File,
    loadIcon: (AppEntry) -> Drawable,
    defaultChoiceForMode: (LocalSeparationMode, PreviewChoice) -> PreviewChoice,
    composeAssets: (GenerationSession, PreviewSelections) -> PreviewAssets,
): PreviewAssets = withContext(Dispatchers.Default) {
    val icon = loadIcon(app)
    val localSourceIcon = drawLocalCandidateSourceIcon(icon, SIZE_1X1, SIZE_1X1)
    val localSource = buildLocalIconLayers(
        icon,
        pipeline,
        tuning.backgroundSeparationPercent,
        AdaptiveForegroundMode.fromValue(tuning.adaptiveForegroundMode),
        tuning.adaptiveDirectMaxCoveragePercent,
        tuning.adaptiveDirectMaxCoverageIncreasePercent,
        tuning.adaptiveMaskEdgeCoveragePercent,
        tuning.adaptiveMaskMinCoveragePercent,
        tuning.adaptiveCenterEpsilonPercent,
    )
    val localCandidateSet = buildLocalCandidates(
        localSource,
        localSourceIcon,
        pipeline,
        OriginalForegroundCleanupMode.fromValue(tuning.originalForegroundCleanupMode),
        tuning.plateRemovalPercent,
        tuning.shadowRemovalPercent,
        tuning.backgroundSeparationPercent,
    )
    val localCandidates = localCandidateSet.candidates
    val defaultChoice = defaultChoiceForMode(
        LocalSeparationMode.fromValue(tuning.localSeparationMode),
        localCandidateSet.autoChoice,
    )
        .takeIf { localCandidates.containsKey(it) }
        ?: localCandidateSet.autoChoice.takeIf { localCandidates.containsKey(it) }
        ?: PreviewChoice.Original
    val selections = PreviewSelections.default(defaultChoice)
    val dummyOutDir = File(cacheDir, "preview_tmp")
    val session = GenerationSession(
        packageName = app.packageName,
        outDir = dummyOutDir,
        sourceIcon = localSourceIcon,
        baseRecfg = localSource.recfg,
        baseRecbg = localSource.recbg,
        monochromeRaw = localSource.monochrome,
        candidates = localCandidates,
        autoLocalChoice = localCandidateSet.autoChoice,
    )
    composeAssets(session, selections).preparedForDraw()
}

/**
 * 批量预览启动（原 startBatchPreview 本体下沉；viewModelScope 调度）。
 * 管线由已应用的 merged 快照派生（LocalPipelineConfig.from(merged)，与原
 * applyTuningParams(merged) 后 currentLocalPipelineConfig() 等价）；采样/渲染/
 * 快照落盘顺序不变，快照 IO 经 onSaveSnapshot（Activity 绑定 filesDir）。
 */
internal fun MainViewModel.startBatchPreview(
    preset: TuningPreset,
    apps: List<AppEntry>,
    selfPackageName: String,
    batchPreviewCount: Int,
    generatedPackageNames: Set<String>,
    originalParams: TuningParams,
    cacheDir: File,
    loadIcon: (AppEntry) -> Drawable,
    defaultChoiceForMode: (LocalSeparationMode, PreviewChoice) -> PreviewChoice,
    composeAssets: (GenerationSession, PreviewSelections) -> PreviewAssets,
    onApplyMerged: (TuningParams) -> Unit,
    onRestoreOriginal: (TuningParams) -> Unit,
    onSaveSnapshot: (TuningPreset, List<BatchPreviewItemData>) -> Unit,
) {
    if (shell.value.isBusy || previewSession.value.isGeneratingGptCandidate ||
        previewSession.value.isGeneratingRmbgCandidate || presetUi.value.isGeneratingBatchPreview
    ) {
        updateShell { it.copy(statusText = "当前有任务在运行，请等待") }
        return
    }
    val sampledApps = BatchPreviewSampler.sample(
        candidates = apps,
        generatedPackageNames = generatedPackageNames,
        count = batchPreviewCount,
        selfPackageName = selfPackageName,
    )
    if (sampledApps.isEmpty()) {
        updateShell { it.copy(statusText = "未找到可供预览的启动器应用") }
        return
    }

    updateShell { it.copy(isBusy = true) }
    updatePresetUi { it.copy(isGeneratingBatchPreview = true, batchPreviewCancelled = false) }
    val merged = TuningParams.fromParamMap(preset.params.toParamMap(), originalParams)
    onApplyMerged(merged)
    val pipeline = LocalPipelineConfig.from(merged)
    updatePresetUi {
        it.copy(
            batchPreviewProgress = BatchPreviewProgress(
                presetName = preset.name,
                completed = 0,
                total = sampledApps.size,
                currentLabel = "准备渲染 ${sampledApps.size} 个应用",
            ),
        )
    }
    updateShell { it.copy(statusText = "预设「${preset.name}」批量预览渲染中...") }

    viewModelScope.launch {
        val items = mutableListOf<BatchPreviewItem>()
        var wasCancelled = false
        try {
            for ((index, app) in sampledApps.withIndex()) {
                if (presetUi.value.batchPreviewCancelled) {
                    wasCancelled = true
                    break
                }
                updatePresetUi {
                    it.copy(
                        batchPreviewProgress = BatchPreviewProgress(
                            presetName = preset.name,
                            completed = index,
                            total = sampledApps.size,
                            currentLabel = "渲染中: ${app.label}",
                        ),
                    )
                }
                try {
                    val assets = generateMemoryPreviewAssetsForApp(
                        app = app,
                        pipeline = pipeline,
                        tuning = merged,
                        cacheDir = cacheDir,
                        loadIcon = loadIcon,
                        defaultChoiceForMode = defaultChoiceForMode,
                        composeAssets = composeAssets,
                    )
                    items.add(BatchPreviewItem(packageName = app.packageName, label = app.label, assets = assets))
                } catch (_skipped: Throwable) {
                    // 跳过单应用渲染异常
                }
                updatePresetUi {
                    it.copy(
                        batchPreviewProgress = BatchPreviewProgress(
                            presetName = preset.name,
                            completed = index + 1,
                            total = sampledApps.size,
                            currentLabel = "已完成: ${app.label}",
                        ),
                    )
                }
            }
            if (!wasCancelled && items.isNotEmpty()) {
                val result = BatchPreviewResult(preset = preset, items = items)
                updatePresetUi { it.copy(batchPreviewResult = result, activeBatchPreviewPreset = preset) }

                // 持久化保存快照到磁盘
                val dataList = items.map { item ->
                    BatchPreviewItemData(
                        packageName = item.packageName,
                        label = item.label,
                        recbg = item.assets.recbg,
                        recfg = item.assets.recfg,
                        recNight = item.assets.recNight,
                        monochromeLight = item.assets.monochromeLight,
                        monochromeDark = item.assets.monochromeDark,
                    )
                }
                withContext(Dispatchers.IO) { onSaveSnapshot(preset, dataList) }

                updateShell {
                    it.copy(
                        currentPage = AppPage.BatchPreview,
                        statusText = "已生成预设「${preset.name}」批量预览并保存快照 (${items.size} 个应用)",
                    )
                }
            } else if (wasCancelled) {
                updateShell { it.copy(statusText = "已取消批量预览") }
            } else {
                updateShell { it.copy(statusText = "批量预览生成失败") }
            }
        } finally {
            onRestoreOriginal(originalParams)
            updateShell { it.copy(isBusy = false) }
            updatePresetUi {
                it.copy(
                    isGeneratingBatchPreview = false,
                    batchPreviewProgress = null,
                    batchPreviewCancelled = false,
                )
            }
        }
    }
}

/**
 * 预设套用确认（原 applyPresetToSelectedApps 本体下沉）。
 * 包名解析收敛为 resolvePresetBatchPackages；对话框经 onRequestConfirm，
 * 确认后经 onExecute 回到 Activity wrapper 再进 execute（调用点零改动）。
 */
internal fun MainViewModel.applyPresetToSelectedApps(
    preset: TuningPreset,
    onRequestConfirm: (title: String, message: String, confirmLabel: String, onConfirm: () -> Unit) -> Unit,
    onExecute: (TuningPreset, List<String>) -> Unit,
) {
    val batchPackageNames = resolvePresetBatchPackages(
        picker.value.multiSelectedPackageNames,
        previewSession.value.activeGenerationSession?.packageName,
    )
    if (batchPackageNames.isEmpty()) {
        updateShell { it.copy(statusText = "先在应用页多选或选中一个应用") }
        return
    }
    if (shell.value.isBusy || previewSession.value.isGeneratingGptCandidate ||
        previewSession.value.isGeneratingRmbgCandidate
    ) {
        updateShell { it.copy(statusText = "当前有任务在运行，请等待") }
        return
    }
    onRequestConfirm(
        "确认套用预设",
        "将按预设「${preset.name}」批量处理 ${batchPackageNames.size} 个应用，会覆盖现有图标并写入对应分区，确认继续？",
        "确认套用",
    ) {
        onExecute(preset, batchPackageNames)
    }
    return
}

/**
 * 预设批量执行（原 executeApplyPresetToSelectedApps 本体下沉；viewModelScope 调度）。
 * 循环内进度推进沿用"全部应用"标题（与原 updateBatchApplyProgress 硬编码一致）；
 * 生成 IO 经 generatePackage（后台段），缓存持久化经 persistGenerated，saveUiState 经回调。
 */
internal fun MainViewModel.executeApplyPresetToSelectedApps(
    preset: TuningPreset,
    batchPackageNames: List<String>,
    beforeParams: TuningParams,
    store: PresetStore,
    selectedAtStart: String?,
    apps: List<AppEntry>,
    onApplyPresetParams: (TuningParams) -> Unit,
    generatePackage: (AppEntry) -> GenerationResult,
    persistGenerated: (Set<String>) -> Set<String>,
    onSaveUiState: () -> Unit,
) {
    if (shell.value.isBusy || previewSession.value.isGeneratingGptCandidate ||
        previewSession.value.isGeneratingRmbgCandidate
    ) {
        updateShell { it.copy(statusText = "当前有任务在运行，请等待") }
        return
    }
    updateShell { it.copy(isBusy = true) }
    updatePreviewSession { it.copy(previewChoiceMode = null) }
    updateTransfer {
        it.copy(
            batchApplyProgress = buildBatchApplyProgress(
                title = "预设批量应用",
                completed = 0,
                total = batchPackageNames.size,
                currentLabel = "准备处理 ${batchPackageNames.size} 个 APK",
                failures = 0,
            ),
        )
    }
    updateShell { it.copy(statusText = "预设「${preset.name}」批量处理中: 0/${batchPackageNames.size}") }
    onApplyPresetParams(TuningParams.fromParamMap(preset.params.toParamMap(), beforeParams))
    store.activePresetId = preset.id
    updatePresetUi { it.copy(activePresetId = preset.id) }
    viewModelScope.launch {
        val successes = mutableListOf<String>()
        val failures = mutableListOf<String>()
        var selectedResult: GenerationResult? = null
        try {
            withContext(Dispatchers.Default) {
                batchPackageNames.forEachIndexed { index, packageName ->
                    val app = apps.firstOrNull { it.packageName == packageName }
                    if (app == null) {
                        failures += "$packageName: 应用不存在"
                        updateBatchApplyProgress(
                            completed = index + 1,
                            total = batchPackageNames.size,
                            currentLabel = "跳过: $packageName",
                            failures = failures.size,
                            title = "全部应用",
                        )
                        return@forEachIndexed
                    }
                    updateBatchApplyProgress(
                        completed = index,
                        total = batchPackageNames.size,
                        currentLabel = "处理中: ${app.label} ($packageName)",
                        failures = failures.size,
                        title = "全部应用",
                    )
                    try {
                        val result = generatePackage(app)
                        successes += packageName
                        if (packageName == selectedAtStart) {
                            selectedResult = result
                        }
                    } catch (error: Throwable) {
                        failures += "$packageName: ${error.message ?: error.javaClass.simpleName}"
                    }
                    updateBatchApplyProgress(
                        completed = index + 1,
                        total = batchPackageNames.size,
                        currentLabel = "已完成: ${app.label} ($packageName)",
                        failures = failures.size,
                        title = "全部应用",
                    )
                }
            }
            if (successes.isNotEmpty()) {
                val persisted = withContext(Dispatchers.IO) {
                    persistGenerated(picker.value.generatedPackageNames + successes)
                }
                updatePicker {
                    it.copy(
                        generatedPackageNames = persisted,
                        multiSelectedPackageNames = it.multiSelectedPackageNames - successes.toSet(),
                    )
                }
            }
            val result = selectedResult
            if (result != null && picker.value.selectedPackageName == selectedAtStart) {
                updatePreviewSession { it.copy(activeGenerationSession = result.session) }
                updateLive { p ->
                    p.copy(
                        previewNormalLight = (result.selections).normalLight.name,
                        previewNormalDark = (result.selections).normalDark.name,
                        previewMonochromeLight = (result.selections).monochromeLight.name,
                        previewMonochromeDark = (result.selections).monochromeDark.name,
                    )
                }
                updatePreviewSession {
                    it.copy(
                        previewChoiceMode = null,
                        previewPackageName = result.session.packageName,
                        previewDirPath = result.outDir.absolutePath,
                        previewVersion = it.previewVersion + 1,
                    )
                }
                onSaveUiState()
            }
            updateShell {
                it.copy(
                    statusText = presetBatchFinishStatus(
                        presetName = preset.name,
                        successes = successes.size,
                        total = batchPackageNames.size,
                        firstFailure = failures.firstOrNull(),
                    ),
                )
            }
        } finally {
            updateShell { it.copy(isBusy = false) }
            updatePreviewSession {
                it.copy(
                    isGptPreviewLoading = false,
                    isGeneratingGptCandidate = false,
                    isGeneratingRmbgCandidate = false,
                    rmbgCandidatePackageName = null,
                    rmbgCandidateMode = null,
                    rmbgCandidateStatusText = "",
                )
            }
            updateTransfer { it.copy(batchApplyProgress = null) }
        }
    }
}

/**
 * 当前调参批量入口（原 applyCurrentPresetBatch 本体下沉）。
 * 有激活预设走预设分支（经 onExecutePreset），否则走当前调参分支（确认后 onExecuteCurrent）。
 */
internal fun MainViewModel.applyCurrentPresetBatch(
    store: PresetStore,
    onRequestConfirm: (title: String, message: String, confirmLabel: String, onConfirm: () -> Unit) -> Unit,
    onExecutePreset: (TuningPreset, List<String>) -> Unit,
    onExecuteCurrent: (List<String>) -> Unit,
) {
    val preset = presetUi.value.activePresetId?.let { store.get(it) }
    if (preset != null) {
        applyPresetToSelectedApps(
            preset = preset,
            onRequestConfirm = onRequestConfirm,
            onExecute = onExecutePreset,
        )
        return
    }
    // 未选择任何预设：直接按当前调参批量生成
    val batchPackageNames = picker.value.multiSelectedPackageNames.toList().sorted()
    if (batchPackageNames.isEmpty()) {
        updateShell { it.copy(statusText = "先在应用页多选要批量处理的应用") }
        return
    }
    if (shell.value.isBusy || previewSession.value.isGeneratingGptCandidate ||
        previewSession.value.isGeneratingRmbgCandidate
    ) {
        updateShell { it.copy(statusText = "当前有任务在运行，请等待") }
        return
    }
    onRequestConfirm(
        "确认套用预设",
        "将按当前调参批量处理 ${batchPackageNames.size} 个应用，会覆盖现有图标并写入对应分区，确认继续？",
        "确认套用",
    ) {
        onExecuteCurrent(batchPackageNames)
    }
    return
}

/**
 * 当前调参批量执行（原 executeApplyCurrentBatch 本体下沉；viewModelScope 调度）。
 * 与预设批量同构，标题为"批量生成"，无预设激活写。
 */
internal fun MainViewModel.executeApplyCurrentBatch(
    batchPackageNames: List<String>,
    selectedAtStart: String?,
    apps: List<AppEntry>,
    generatePackage: (AppEntry) -> GenerationResult,
    persistGenerated: (Set<String>) -> Set<String>,
    onSaveUiState: () -> Unit,
) {
    if (shell.value.isBusy || previewSession.value.isGeneratingGptCandidate ||
        previewSession.value.isGeneratingRmbgCandidate
    ) {
        updateShell { it.copy(statusText = "当前有任务在运行，请等待") }
        return
    }
    updateShell { it.copy(isBusy = true) }
    updatePreviewSession { it.copy(previewChoiceMode = null) }
    updateTransfer {
        it.copy(
            batchApplyProgress = buildBatchApplyProgress(
                title = "批量生成",
                completed = 0,
                total = batchPackageNames.size,
                currentLabel = "准备处理 ${batchPackageNames.size} 个 APK",
                failures = 0,
            ),
        )
    }
    updateShell { it.copy(statusText = "按当前调参批量处理中: 0/${batchPackageNames.size}") }
    viewModelScope.launch {
        val successes = mutableListOf<String>()
        val failures = mutableListOf<String>()
        var selectedResult: GenerationResult? = null
        try {
            withContext(Dispatchers.Default) {
                batchPackageNames.forEachIndexed { index, packageName ->
                    val app = apps.firstOrNull { it.packageName == packageName }
                    if (app == null) {
                        failures += "$packageName: 应用不存在"
                        updateBatchApplyProgress(
                            completed = index + 1,
                            total = batchPackageNames.size,
                            currentLabel = "跳过: $packageName",
                            failures = failures.size,
                            title = "全部应用",
                        )
                        return@forEachIndexed
                    }
                    updateBatchApplyProgress(
                        completed = index,
                        total = batchPackageNames.size,
                        currentLabel = "处理中: ${app.label} ($packageName)",
                        failures = failures.size,
                        title = "全部应用",
                    )
                    try {
                        val result = generatePackage(app)
                        successes += packageName
                        if (packageName == selectedAtStart) {
                            selectedResult = result
                        }
                    } catch (error: Throwable) {
                        failures += "$packageName: ${error.message ?: error.javaClass.simpleName}"
                    }
                    updateBatchApplyProgress(
                        completed = index + 1,
                        total = batchPackageNames.size,
                        currentLabel = "已完成: ${app.label} ($packageName)",
                        failures = failures.size,
                        title = "全部应用",
                    )
                }
            }
            if (successes.isNotEmpty()) {
                val persisted = withContext(Dispatchers.IO) {
                    persistGenerated(picker.value.generatedPackageNames + successes)
                }
                updatePicker {
                    it.copy(
                        generatedPackageNames = persisted,
                        multiSelectedPackageNames = it.multiSelectedPackageNames - successes.toSet(),
                    )
                }
            }
            val result = selectedResult
            if (result != null && picker.value.selectedPackageName == selectedAtStart) {
                updatePreviewSession { it.copy(activeGenerationSession = result.session) }
                updateLive { p ->
                    p.copy(
                        previewNormalLight = (result.selections).normalLight.name,
                        previewNormalDark = (result.selections).normalDark.name,
                        previewMonochromeLight = (result.selections).monochromeLight.name,
                        previewMonochromeDark = (result.selections).monochromeDark.name,
                    )
                }
                updatePreviewSession {
                    it.copy(
                        previewChoiceMode = null,
                        previewPackageName = result.session.packageName,
                        previewDirPath = result.outDir.absolutePath,
                        previewVersion = it.previewVersion + 1,
                    )
                }
                onSaveUiState()
            }
            updateShell {
                it.copy(
                    statusText = currentBatchFinishStatus(
                        successes = successes.size,
                        total = batchPackageNames.size,
                        firstFailure = failures.firstOrNull(),
                    ),
                )
            }
        } finally {
            updateShell { it.copy(isBusy = false) }
            updatePreviewSession {
                it.copy(
                    isGptPreviewLoading = false,
                    isGeneratingGptCandidate = false,
                    isGeneratingRmbgCandidate = false,
                    rmbgCandidatePackageName = null,
                    rmbgCandidateMode = null,
                    rmbgCandidateStatusText = "",
                )
            }
            updateTransfer { it.copy(batchApplyProgress = null) }
        }
    }
}

/**
 * 单选光影追加（原 addLiquidGlassToSelectedGenerated 本体下沉；viewModelScope 调度）。
 * Root 写入三段（定位/渲染/安装）经回调注入，顺序与路径不变。
 */
internal fun MainViewModel.addLiquidGlassToSelectedGenerated(
    apps: List<AppEntry>,
    resolvePackageDir: (String) -> File,
    applyGlass: (File) -> Unit,
    installGlass: (File, String) -> Unit,
    buildSession: (String, File) -> GenerationSession,
    persistOne: (Set<String>, String) -> Set<String>,
    onSaveUiState: () -> Unit,
) {
    val entry = apps.firstOrNull { it.packageName == picker.value.selectedPackageName }
    if (entry == null) {
        updateShell { it.copy(statusText = "先选择一个应用") }
        return
    }
    if (shell.value.isBusy) {
        return
    }
    updateShell { it.copy(isBusy = true, statusText = "正在添加光影: ${entry.packageName}") }
    viewModelScope.launch {
        try {
            val built = withContext(Dispatchers.IO) {
                val packageDir = resolvePackageDir(entry.packageName)
                applyGlass(packageDir)
                installGlass(packageDir, entry.packageName)
                buildSession(entry.packageName, packageDir) to packageDir
            }
            val (session, packageDir) = built
            updatePicker {
                it.copy(generatedPackageNames = persistOne(it.generatedPackageNames, entry.packageName))
            }
            updatePreviewSession { it.copy(activeGenerationSession = session) }
            updateLive { p ->
                p.copy(
                    previewNormalLight = (PreviewSelections.default(PreviewChoice.Original)).normalLight.name,
                    previewNormalDark = (PreviewSelections.default(PreviewChoice.Original)).normalDark.name,
                    previewMonochromeLight = (PreviewSelections.default(PreviewChoice.Original)).monochromeLight.name,
                    previewMonochromeDark = (PreviewSelections.default(PreviewChoice.Original)).monochromeDark.name,
                )
            }
            updatePreviewSession {
                it.copy(
                    previewChoiceMode = null,
                    previewPackageName = entry.packageName,
                    previewDirPath = packageDir.absolutePath,
                    previewVersion = it.previewVersion + 1,
                )
            }
            onSaveUiState()
            updateShell { it.copy(statusText = "已添加光影，未刷新，请手动点首页左上角刷新图标: ${entry.packageName}") }
        } catch (error: Exception) {
            updateShell { it.copy(statusText = "添加光影失败: ${error.message ?: error.javaClass.simpleName}") }
        } finally {
            updateShell { it.copy(isBusy = false) }
        }
    }
}

/**
 * 多选光影追加确认（原 addLiquidGlassToMultiSelectedGenerated 本体下沉）。
 */
internal fun MainViewModel.addLiquidGlassToMultiSelectedGenerated(
    onRequestConfirm: (title: String, message: String, confirmLabel: String, onConfirm: () -> Unit) -> Unit,
    onExecute: (List<String>) -> Unit,
) {
    val packageNames = picker.value.multiSelectedPackageNames.toList().sorted()
    if (packageNames.isEmpty()) {
        updateShell { it.copy(statusText = "先选择要添加光影的应用") }
        return
    }
    if (shell.value.isBusy) {
        return
    }

    onRequestConfirm(
        "确认添加光影",
        "将为 ${packageNames.size} 个已选项添加光影并写入 data 分区，耗时较长且会覆盖现有图标，确认继续？",
        "确认添加",
    ) {
        onExecute(packageNames)
    }
    return
}

/**
 * 多选光影批量执行（原 executeAddLiquidGlassToMultiSelectedGenerated 本体下沉；
 * viewModelScope 调度；后台 status 直写 statusText，与原 status() 落点一致）。
 */
internal fun MainViewModel.executeAddLiquidGlassToMultiSelectedGenerated(
    packageNames: List<String>,
    selectedAtStart: String?,
    resolvePackageDir: (String) -> File,
    applyGlass: (File) -> Unit,
    installGlass: (File, String) -> Unit,
    buildSession: (String, File) -> GenerationSession,
    persistMany: (Set<String>) -> Set<String>,
    onSaveUiState: () -> Unit,
) {
    if (shell.value.isBusy) {
        return
    }
    updateShell { it.copy(isBusy = true, statusText = "正在批量添加光影: ${packageNames.size} 个") }
    viewModelScope.launch {
        val successes = mutableListOf<String>()
        val failures = mutableListOf<String>()
        var selectedSession: GenerationSession? = null
        var selectedDirPath: String? = null

        withContext(Dispatchers.IO) {
            packageNames.forEachIndexed { index, packageName ->
                updateShell { it.copy(statusText = "添加光影中 ${index + 1}/${packageNames.size}: $packageName") }
                try {
                    val packageDir = resolvePackageDir(packageName)
                    applyGlass(packageDir)
                    installGlass(packageDir, packageName)
                    successes += packageName
                    if (packageName == selectedAtStart) {
                        selectedSession = buildSession(packageName, packageDir)
                        selectedDirPath = packageDir.absolutePath
                    }
                } catch (error: Exception) {
                    failures += "$packageName: ${error.message ?: error.javaClass.simpleName}"
                }
            }
        }

        if (successes.isNotEmpty()) {
            val persisted = withContext(Dispatchers.IO) {
                persistMany(picker.value.generatedPackageNames + successes)
            }
            updatePicker {
                it.copy(
                    generatedPackageNames = persisted,
                    multiSelectedPackageNames = it.multiSelectedPackageNames - successes.toSet(),
                )
            }
        }
        if (selectedAtStart != null &&
            picker.value.selectedPackageName == selectedAtStart &&
            selectedSession != null &&
            selectedDirPath != null
        ) {
            val session = selectedSession
            val dirPath = selectedDirPath
            updatePreviewSession { it.copy(activeGenerationSession = session) }
            updateLive { p ->
                p.copy(
                    previewNormalLight = (PreviewSelections.default(PreviewChoice.Original)).normalLight.name,
                    previewNormalDark = (PreviewSelections.default(PreviewChoice.Original)).normalDark.name,
                    previewMonochromeLight = (PreviewSelections.default(PreviewChoice.Original)).monochromeLight.name,
                    previewMonochromeDark = (PreviewSelections.default(PreviewChoice.Original)).monochromeDark.name,
                )
            }
            updatePreviewSession {
                it.copy(
                    previewChoiceMode = null,
                    previewPackageName = selectedAtStart,
                    previewDirPath = dirPath,
                    previewVersion = it.previewVersion + 1,
                )
            }
            onSaveUiState()
        }
        updateShell {
            it.copy(
                statusText = liquidGlassBatchFinishStatus(
                    successes = successes.size,
                    failures = failures.size,
                    firstFailure = failures.firstOrNull(),
                ),
            )
        }
        updateShell { it.copy(isBusy = false) }
    }
}
