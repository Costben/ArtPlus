package dev.artplus.mobile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.SharedPreferences
import java.io.File
import org.json.JSONObject

/**
 * Slice 2.1：预设业务逻辑（原 MainActivity 预设 CRUD / 导入导出 / JSON 直写原样搬迁）。
 * 只做纯移动：SharedPreferences key、剪贴板标签、序列化格式与读写顺序一律不变。
 * Activity 状态经参数/回调注入；MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动。
 */

// ---------- 预设：保存 / 应用 / 批量 / 导入导出 ----------

internal fun refreshPresets(
    store: PresetStore,
    onBumpVersion: () -> Unit,
    onRefreshed: (activeId: String?, base: TuningParams?) -> Unit,
) {
    onBumpVersion()
    val stored = store.activePresetId
    val preset = if (stored != null) store.get(stored) else null
    onRefreshed(preset?.id, preset?.params)
}

internal fun loadPresetState(
    prefs: SharedPreferences,
    batchOutputModeKey: String,
    batchOutputModeFallbackName: String,
    gptRunCountKey: String,
    rmbgRunCountKey: String,
    onRefreshPresets: () -> Unit,
    onLoaded: (BatchOutputMode, Int, Int) -> Unit,
) {
    onRefreshPresets()
    val mode = runCatching {
        BatchOutputMode.valueOf(prefs.getString(batchOutputModeKey, batchOutputModeFallbackName) ?: batchOutputModeFallbackName)
    }.getOrDefault(BatchOutputMode.Root)
    val gptCount = prefs.getInt(gptRunCountKey, 0)
    val rmbgCount = prefs.getInt(rmbgRunCountKey, 0)
    onLoaded(mode, gptCount, rmbgCount)
}

internal fun saveCurrentAsPreset(
    rawName: String,
    store: PresetStore,
    current: TuningParams,
    viewModel: MainViewModel,
    onSaved: (TuningPreset, String) -> Unit,
    onStatus: (String) -> Unit,
) {
    // P2 交界：预设域收敛进 MainViewModel，这里只做 UI 状态（文案/镜像/版本/弹窗）。
    when (val outcome = viewModel.savePreset(store, current, rawName)) {
        SavePresetOutcome.BlankName -> {
            onStatus("预设名称不能为空")
            return
        }
        is SavePresetOutcome.DuplicateName -> {
            onStatus("预设「${outcome.name}」已存在，请换一个名称")
            return
        }
        is SavePresetOutcome.Saved -> {
            val preset = outcome.preset
            onSaved(preset, "已保存预设「${preset.name}」（${preset.params.toParamMap().size} 项参数）")
        }
    }
}

internal fun overwritePreset(
    preset: TuningPreset,
    store: PresetStore,
    current: TuningParams,
    viewModel: MainViewModel,
    onOverwritten: (TuningPreset, TuningParams, String) -> Unit,
    onStatus: (String) -> Unit,
) {
    // P2 交界：预设域收敛进 MainViewModel，这里只做 UI 状态。
    if (!viewModel.overwritePreset(store, preset, current)) {
        onStatus("更新预设失败")
        return
    }
    onOverwritten(preset, current, "已覆盖更新预设「${preset.name}」")
}

internal fun resetToPreset(
    preset: TuningPreset,
    isBusy: Boolean,
    isGeneratingGptCandidate: Boolean,
    isGeneratingRmbgCandidate: Boolean,
    before: TuningParams,
    viewModel: MainViewModel,
    onReset: (TuningPreset, TuningParams, String) -> Unit,
    onStatus: (String) -> Unit,
) {
    if (isBusy || isGeneratingGptCandidate || isGeneratingRmbgCandidate) {
        onStatus("当前有任务在运行，请等待")
        return
    }
    // P2 交界：预设合并收敛进 MainViewModel。
    val merged = viewModel.mergedPresetParams(preset, before)
    onReset(preset, merged, "已重置回预设「${preset.name}」初始参数")
}

internal fun applyPreset(
    preset: TuningPreset,
    isBusy: Boolean,
    isGeneratingGptCandidate: Boolean,
    isGeneratingRmbgCandidate: Boolean,
    before: TuningParams,
    viewModel: MainViewModel,
    onApplied: (TuningPreset, TuningParams, String) -> Unit,
    onStatus: (String) -> Unit,
) {
    if (isBusy || isGeneratingGptCandidate || isGeneratingRmbgCandidate) {
        onStatus("当前有任务在运行，请等待")
        return
    }
    // P2 交界：预设合并收敛进 MainViewModel。
    val merged = viewModel.mergedPresetParams(preset, before)
    onApplied(preset, merged, "已应用预设「${preset.name}」，${before.diffSummary(merged)}")
}

internal fun deletePreset(
    id: String,
    filesDir: File,
    store: PresetStore,
    viewModel: MainViewModel,
    activeBatchPreviewPresetId: String?,
    batchPreviewResultPresetId: String?,
    currentPage: AppPage,
    activePresetId: String?,
    onBatchPreviewReset: () -> Unit,
    onNavigateHome: () -> Unit,
    onActiveCleared: () -> Unit,
    onBumpVersion: () -> Unit,
    onStatus: (String) -> Unit,
) {
    BatchPreviewStore.deleteSnapshot(filesDir, id)
    if (activeBatchPreviewPresetId == id || batchPreviewResultPresetId == id) {
        onBatchPreviewReset()
        if (currentPage == AppPage.BatchPreview) {
            onNavigateHome()
        }
    }
    // P2 交界：store 删除收敛进 MainViewModel；BatchPreview/页面/UI 状态留 Activity。
    viewModel.deletePreset(store, id)
    if (activePresetId == id) {
        onActiveCleared()
    }
    onBumpVersion()
    onStatus("已删除预设")
}

internal fun renamePreset(
    id: String,
    rawName: String,
    store: PresetStore,
    viewModel: MainViewModel,
    onRenamed: (String, String) -> Unit,
    onStatus: (String) -> Unit,
) {
    // P2 交界：预设域收敛进 MainViewModel，这里只做 UI 状态。
    when (val outcome = viewModel.renamePreset(store, id, rawName)) {
        RenamePresetOutcome.BlankName -> {
            onStatus("预设名称不能为空")
            return
        }
        RenamePresetOutcome.Failed -> {
            onStatus("重命名失败：名称与现有预设重复")
            return
        }
        is RenamePresetOutcome.Renamed -> {
            onRenamed(outcome.name, "已重命名为「${outcome.name}」")
        }
    }
}

internal fun exportPresetsToClipboard(
    store: PresetStore,
    clipboard: ClipboardManager?,
    onStatus: (String) -> Unit,
) {
    val json = store.exportJson()
    if (clipboard == null) {
        onStatus("剪贴板不可用")
        return
    }
    clipboard.setPrimaryClip(ClipData.newPlainText("ArtPlus预设", json))
    onStatus("已复制 ${store.all().size} 条预设 JSON 到剪贴板")
}

internal fun exportSinglePresetToClipboard(
    preset: TuningPreset,
    store: PresetStore,
    clipboard: ClipboardManager?,
    onStatus: (String) -> Unit,
) {
    val json = store.exportSingleJson(preset)
    if (clipboard == null) {
        onStatus("剪贴板不可用")
        return
    }
    clipboard.setPrimaryClip(ClipData.newPlainText("ArtPlus预设-${preset.name}", json))
    onStatus("已复制预设「${preset.name}」JSON 到剪贴板")
}

internal fun importPresetsFromText(
    text: String,
    store: PresetStore,
    onApplied: (String) -> Unit,
) {
    val result = store.importJson(text)
    val status = if (result.errors.isEmpty()) {
        "已导入 ${result.imported} 条预设"
    } else {
        "已导入 ${result.imported} 条，失败 ${result.errors.size} 条：${result.errors.firstOrNull().orEmpty()}"
    }
    onApplied(status)
}

/** JSON 编辑器：解析文本为 TuningParams 并应用（缺失键保持当前值）。 */
internal fun saveJsonParamsFromText(
    text: String,
    current: TuningParams,
    onApplyParams: (TuningParams) -> Unit,
    onStatus: (String) -> Unit,
) {
    val json = runCatching { JSONObject(text) }.getOrElse { error ->
        onStatus("JSON 解析失败：${error.message ?: error.javaClass.simpleName}")
        return
    }
    val params = TuningParams.fromJson(json, current)
    if (params == null) {
        onStatus("JSON 参数解析失败")
        return
    }
    onApplyParams(params)
    onStatus("已应用 JSON 参数")
}
