package dev.artplus.mobile

import android.content.pm.PackageManager
import android.graphics.Bitmap
import java.io.File
import android.graphics.Color as AndroidColor

/**
 * 生成管线（P4 拆分）：本地会话构建/重建 + GPT 图层生成 + 批量预览快照读取。
 *
 * 从 MainActivity 迁移而来：原 `private fun`，现 `internal`。
 * - `IconLayers`/`BatchPreviewItem`/`BatchPreviewResult`/`PreviewAssets` 整类提升
 *  （原 private data class，自包含；`PreviewAssets.missingMessage` 随带，
 *   `preparedForDraw` 纯 helper 随带；UI/管线引用同包零改动）。
 * - 读 Activity 状态者改为显式参数：`buildLocalSessionForPreview`/`rebuildLocalSession`
 *   收 pm + tuning 快照（原直读 packageManager/currentLocalPipelineConfig；
 *   pipeline 及 imaging 显式标量经 LocalPipelineConfig.from/fromValue 从快照派生，
 *   与 P1 wrapper 传参逐值一致，调用方传 currentTuningParams()）；
 *   `generateGptLayers` 收 customPrompt + preset + subjectPercent + mode + modelId
 *   + baseUrl + apiKey + isDebugBuild，`status()` 改 onStatus 回调；
 *   `loadBatchPreviewSnapshot` 收 filesDir。
 * - `refreshActivePreviewOutputs`/`writeActivePreviewOutputs`/`startBatchPreview`/
 *   `openBatchPreviewForPreset`/`generateMemoryPreviewAssetsForApp`/
 *   `applyPresetToSelectedApps`/`executeApplyPresetToSelectedApps`/
 *   `applyCurrentPresetBatch`/`executeApplyCurrentBatch` 的线程/协程/对话框/进度编排
 *   搬不动（或拖带未定域 helper），留 Activity 做瘦壳。
 * 调用点改显式传参（同名同参 wrapper 禁止，§5.8）。
 */

internal data class IconLayers(
    val recfg: Bitmap,
    val recbg: Bitmap,
)

internal data class BatchPreviewItem(
    val packageName: String,
    val label: String,
    val assets: PreviewAssets,
)

internal data class BatchPreviewResult(
    val preset: TuningPreset,
    val items: List<BatchPreviewItem>,
)

internal data class PreviewAssets(
    val recbg: Bitmap?,
    val recfg: Bitmap?,
    val recNight: Bitmap?,
    val monochromeLight: Bitmap?,
    val monochromeDark: Bitmap?,
) {
    fun missingMessage(mode: PreviewMode): String? =
        when (mode) {
            PreviewMode.NormalLight -> if (recbg == null || recfg == null) "缺少 recbg/recfg" else null
            PreviewMode.NormalDark -> if (recNight == null) "缺少 rec_night" else null
            PreviewMode.MonochromeLight -> if (monochromeLight == null) "缺少 monochrome" else null
            PreviewMode.MonochromeDark -> if (monochromeDark == null) "缺少 monochrome" else null
        }
}

internal fun PreviewAssets.preparedForDraw(): PreviewAssets {
    recbg?.prepareToDraw()
    recfg?.prepareToDraw()
    recNight?.prepareToDraw()
    monochromeLight?.prepareToDraw()
    monochromeDark?.prepareToDraw()
    return this
}

internal fun buildLocalSessionForPreview(
    app: AppEntry,
    outDir: File,
    pm: PackageManager,
    tuning: TuningParams,
): GenerationSession {
    val pipeline = LocalPipelineConfig.from(tuning)
    val icon = app.applicationInfo.loadIcon(pm)
    val localSourceIcon = drawLocalCandidateSourceIcon(icon, SIZE_1X1, SIZE_1X1)
    val gptSourceIcon = drawDrawable(icon, GPT_SOURCE_SIZE, GPT_SOURCE_SIZE, transparent = false)
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
    return GenerationSession(
        packageName = app.packageName,
        outDir = outDir,
        sourceIcon = gptSourceIcon,
        baseRecfg = localSource.recfg,
        baseRecbg = localSource.recbg,
        monochromeRaw = localSource.monochrome,
        candidates = localCandidateSet.candidates,
        autoLocalChoice = localCandidateSet.autoChoice,
    )
}

internal fun rebuildLocalSession(
    session: GenerationSession,
    app: AppEntry,
    pm: PackageManager,
    tuning: TuningParams,
): GenerationSession {
    val pipeline = LocalPipelineConfig.from(tuning)
    val icon = app.applicationInfo.loadIcon(pm)
    val localSourceIcon = drawLocalCandidateSourceIcon(icon, SIZE_1X1, SIZE_1X1)
    val gptSourceIcon = drawDrawable(icon, GPT_SOURCE_SIZE, GPT_SOURCE_SIZE, transparent = false)
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
    val retainedCandidates = buildMap {
        session.candidates[PreviewChoice.Gpt]?.let { put(PreviewChoice.Gpt, it) }
        session.candidates[PreviewChoice.Rmbg]?.let { put(PreviewChoice.Rmbg, it) }
    }
    val candidates = localCandidates + retainedCandidates
    return session.copy(
        sourceIcon = gptSourceIcon,
        baseRecfg = localSource.recfg,
        baseRecbg = localSource.recbg,
        monochromeRaw = localSource.monochrome,
        candidates = candidates,
        customForegrounds = session.customForegrounds,
        customBackgrounds = session.customBackgrounds,
        autoLocalChoice = localCandidateSet.autoChoice,
    )
}

internal fun generateGptLayers(
    sourceIcon: Bitmap,
    localRecfg: Bitmap,
    localRecbg: Bitmap,
    customPrompt: String,
    preset: GptPromptPreset,
    subjectPercent: Int,
    mode: GptImageMode,
    modelId: String,
    baseUrl: String,
    apiKey: String,
    isDebugBuild: Boolean,
    onStatus: (String) -> Unit,
): IconLayers {
    val chromaKey = chooseChromaKey(sourceIcon)
    val chromaHex = "#%02x%02x%02x".format(
        AndroidColor.red(chromaKey),
        AndroidColor.green(chromaKey),
        AndroidColor.blue(chromaKey),
    )
    val transparentForegroundPrompt = buildTransparentForegroundPrompt(customPrompt, preset, subjectPercent)
    val chromaForegroundPrompt = buildChromaForegroundPrompt(chromaHex, customPrompt, preset, subjectPercent)
    val backgroundPrompt = buildBackgroundPrompt()

    onStatus("AI生成前景...")
    var usedChromaForeground = false
    val rawForeground = try {
        val transparentForeground = gptEditImage(sourceIcon, transparentForegroundPrompt, "transparent", mode, modelId, baseUrl, apiKey, isDebugBuild)
        if (hasRealAlpha(transparentForeground)) {
            transparentForeground
        } else {
            usedChromaForeground = true
            onStatus("AI未返回透明前景，改用纯色抠底兜底")
            gptEditImage(sourceIcon, chromaForegroundPrompt, "opaque", mode, modelId, baseUrl, apiKey, isDebugBuild)
        }
    } catch (error: Exception) {
        usedChromaForeground = true
        onStatus("AI透明前景失败，改用纯色抠底兜底: ${error.message ?: error.javaClass.simpleName}")
        gptEditImage(sourceIcon, chromaForegroundPrompt, "opaque", mode, modelId, baseUrl, apiKey, isDebugBuild)
    }
    onStatus("AI生成背景...")
    val rawBackground = gptEditImage(sourceIcon, backgroundPrompt, "opaque", mode, modelId, baseUrl, apiKey, isDebugBuild)

    val recbg = Bitmap.createScaledBitmap(rawBackground, SIZE_1X1, SIZE_1X1, true)
    val recfg = when {
        hasRealAlpha(rawForeground) -> {
            Bitmap.createScaledBitmap(rawForeground, SIZE_1X1, SIZE_1X1, true)
        }
        usedChromaForeground -> {
            val keyed = removeChromaKeyBackground(rawForeground, chromaKey)
            if (alphaCoverage(keyed) in 0.002..0.95) {
                Bitmap.createScaledBitmap(keyed, SIZE_1X1, SIZE_1X1, true)
            } else {
                localRecfg
            }
        }
        else -> localRecfg
    }
    return IconLayers(recfg, recbg)
}

internal fun loadBatchPreviewSnapshot(filesDir: File, preset: TuningPreset): BatchPreviewResult? {
    val datas = BatchPreviewStore.loadSnapshot(filesDir, preset) ?: return null
    val items = datas.map { data ->
        BatchPreviewItem(
            packageName = data.packageName,
            label = data.label,
            assets = PreviewAssets(
                recbg = data.recbg,
                recfg = data.recfg,
                recNight = data.recNight,
                monochromeLight = data.monochromeLight,
                monochromeDark = data.monochromeDark,
            ).preparedForDraw(),
        )
    }
    return BatchPreviewResult(preset = preset, items = items)
}
