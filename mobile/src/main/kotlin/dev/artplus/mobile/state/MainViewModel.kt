package dev.artplus.mobile

import androidx.lifecycle.ViewModel
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * P2 ViewModel 地基：快照/历史/预设域（单源）。
 *
 * Activity 管 live UI 域（186 个 `mutableStateOf` + `currentTuningParams()` 本 phase 不动，
 * 全量重写留 P5）；交界一律用 TuningParams 快照显式同步，VM 绝不读 Activity 字段。
 * PresetStore 由调用方传入（Activity 的单实例），VM 内不持有，避免双实例分叉。
 * pipeline/effective* wrapper 删除延后到 P5（调用点仍读 UI vars）。
 */
class MainViewModel : ViewModel() {

    companion object {
        const val MAX_HISTORY_SIZE = 50
    }

    private val _params = MutableStateFlow(TuningParams())
    val params: StateFlow<TuningParams> = _params.asStateFlow()

    private val _history = MutableStateFlow<List<TuningParams>>(emptyList())
    val history: StateFlow<List<TuningParams>> = _history.asStateFlow()

    private val _historyIndex = MutableStateFlow(-1)
    val historyIndex: StateFlow<Int> = _historyIndex.asStateFlow()

    fun canUndo(): Boolean = _historyIndex.value > 0 && _history.value.isNotEmpty()

    fun canRedo(): Boolean =
        _historyIndex.value >= 0 && _historyIndex.value < _history.value.size - 1

    /**
     * apply 漏斗统一入口（从 MainActivity.applyTuningParams 原样搬）：
     * captureUndo 时记历史（冷启动基线为起点；相同快照不入栈；redo 尾截断；上限掐头），
     * 始终把 applied 镜像进 params 流。
     */
    fun onParamsApplied(before: TuningParams, applied: TuningParams, captureUndo: Boolean) {
        if (captureUndo) {
            if (_history.value.isEmpty()) {
                _history.value = listOf(before)
                _historyIndex.value = 0
            }
            if (!applied.sameAs(before)) {
                val truncated = if (_historyIndex.value < _history.value.size - 1) {
                    _history.value.take(_historyIndex.value + 1)
                } else {
                    _history.value
                }
                val next = truncated + applied
                _history.value = next
                _historyIndex.value = next.size - 1
                if (_history.value.size > MAX_HISTORY_SIZE) {
                    _history.value = _history.value.drop(1)
                    _historyIndex.value--
                }
            }
        }
        _params.value = applied
    }

    /** 冷启动基线（从 MainActivity.initTuningHistory 原样搬）。 */
    fun resetHistory(current: TuningParams) {
        _history.value = listOf(current)
        _historyIndex.value = 0
        _params.value = current
    }

    /** 后退；无处可退返回 null（调用方显示"已到最早的配置"）。 */
    fun undo(): TuningParams? {
        if (!canUndo()) {
            return null
        }
        _historyIndex.value--
        return _history.value[_historyIndex.value]
    }

    /** 前进；无处可进返回 null（调用方显示"已到最新的配置"）。 */
    fun redo(): TuningParams? {
        if (!canRedo()) {
            return null
        }
        _historyIndex.value++
        return _history.value[_historyIndex.value]
    }

    /** 预设合并（从 resetToPreset/applyPreset 原样搬）：预设键覆盖，缺失键保持当前值。 */
    fun mergedPresetParams(preset: TuningPreset, before: TuningParams): TuningParams =
        TuningParams.fromParamMap(preset.params.toParamMap(), before)

    /** 保存当前为新预设（从 MainActivity.saveCurrentAsPreset 原样搬；UI 文案/状态留 Activity）。 */
    fun savePreset(
        store: PresetStore,
        current: TuningParams,
        rawName: String,
    ): SavePresetOutcome {
        val name = rawName.trim()
        if (name.isBlank()) {
            return SavePresetOutcome.BlankName
        }
        val now = System.currentTimeMillis()
        val preset = TuningPreset(
            id = UUID.randomUUID().toString(),
            name = name,
            params = current,
            createdAt = now,
            updatedAt = now,
        )
        if (!store.save(preset)) {
            return SavePresetOutcome.DuplicateName(name)
        }
        store.activePresetId = preset.id
        return SavePresetOutcome.Saved(preset)
    }

    /** 覆盖更新既有预设（从 MainActivity.overwritePreset 原样搬）。 */
    fun overwritePreset(store: PresetStore, preset: TuningPreset, current: TuningParams): Boolean {
        val updated = preset.copy(
            params = current,
            updatedAt = System.currentTimeMillis(),
        )
        if (!store.save(updated)) {
            return false
        }
        store.activePresetId = updated.id
        return true
    }

    /** 删除预设（store 写；BatchPreview/页面等 UI 状态留 Activity）。 */
    fun deletePreset(store: PresetStore, id: String) {
        store.delete(id)
    }

    /** 重命名预设（从 MainActivity.renamePreset 原样搬；UI 文案/状态留 Activity）。 */
    fun renamePreset(store: PresetStore, id: String, rawName: String): RenamePresetOutcome {
        val name = rawName.trim()
        if (name.isBlank()) {
            return RenamePresetOutcome.BlankName
        }
        if (!store.rename(id, name)) {
            return RenamePresetOutcome.Failed
        }
        return RenamePresetOutcome.Renamed(name)
    }
}

sealed interface SavePresetOutcome {
    data class Saved(val preset: TuningPreset) : SavePresetOutcome
    data object BlankName : SavePresetOutcome
    data class DuplicateName(val name: String) : SavePresetOutcome
}

sealed interface RenamePresetOutcome {
    data class Renamed(val name: String) : RenamePresetOutcome
    data object BlankName : RenamePresetOutcome
    data object Failed : RenamePresetOutcome
}
