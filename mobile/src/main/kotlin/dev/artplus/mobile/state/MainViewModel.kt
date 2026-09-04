package dev.artplus.mobile

import androidx.lifecycle.ViewModel
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * P2 ViewModel 地基：快照/历史/预设域（单源）。
 * Slice 3.1 收敛：非调参 UI 状态收敛为 10 组具名 StateFlow（state/UiState.kt），
 * 调参子集为单一 MutableStateFlow<TuningParams>（params），Activity 侧 collectAsState()，
 * draft*Text 草稿态留 UI 层不进 VM。单源：同一状态不得同时存在于 Activity 与 VM，
 * Activity 侧仅留薄 wrapper 委托（标注“重构期间保留”），load/save 语义不变。
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

    // Slice 3.1：非调参 UI 状态分组单源（默认值与 MainActivity 基线一致，见 state/UiState.kt）。
    private val _picker = MutableStateFlow(PickerState())
    internal val picker: StateFlow<PickerState> = _picker.asStateFlow()

    private val _shell = MutableStateFlow(ShellState())
    internal val shell: StateFlow<ShellState> = _shell.asStateFlow()

    private val _gptRmbgSettings = MutableStateFlow(GptRmbgSettingsState())
    internal val gptRmbgSettings: StateFlow<GptRmbgSettingsState> = _gptRmbgSettings.asStateFlow()

    private val _glassBar = MutableStateFlow(GlassBarState())
    internal val glassBar: StateFlow<GlassBarState> = _glassBar.asStateFlow()

    private val _presetUi = MutableStateFlow(PresetUiState())
    internal val presetUi: StateFlow<PresetUiState> = _presetUi.asStateFlow()

    private val _batchPreviewConfig = MutableStateFlow(BatchPreviewConfigState())
    internal val batchPreviewConfig: StateFlow<BatchPreviewConfigState> = _batchPreviewConfig.asStateFlow()

    private val _confirm = MutableStateFlow(ConfirmUiState())
    internal val confirm: StateFlow<ConfirmUiState> = _confirm.asStateFlow()

    private val _transfer = MutableStateFlow(TransferState())
    internal val transfer: StateFlow<TransferState> = _transfer.asStateFlow()

    private val _previewSession = MutableStateFlow(PreviewSessionState())
    internal val previewSession: StateFlow<PreviewSessionState> = _previewSession.asStateFlow()

    private val _updateUi = MutableStateFlow(UpdateUiState())
    internal val updateUi: StateFlow<UpdateUiState> = _updateUi.asStateFlow()

    /** Slice 3.1 分组写漏斗：不记历史，与原各直接写 var 一致，同步更新对应流触发重组。 */
    internal fun updatePicker(transform: (PickerState) -> PickerState) {
        _picker.value = transform(_picker.value)
    }

    internal fun updateShell(transform: (ShellState) -> ShellState) {
        _shell.value = transform(_shell.value)
    }

    internal fun updateGptRmbgSettings(transform: (GptRmbgSettingsState) -> GptRmbgSettingsState) {
        _gptRmbgSettings.value = transform(_gptRmbgSettings.value)
    }

    internal fun updateGlassBar(transform: (GlassBarState) -> GlassBarState) {
        _glassBar.value = transform(_glassBar.value)
    }

    internal fun updatePresetUi(transform: (PresetUiState) -> PresetUiState) {
        _presetUi.value = transform(_presetUi.value)
    }

    internal fun updateBatchPreviewConfig(transform: (BatchPreviewConfigState) -> BatchPreviewConfigState) {
        _batchPreviewConfig.value = transform(_batchPreviewConfig.value)
    }

    internal fun updateConfirm(transform: (ConfirmUiState) -> ConfirmUiState) {
        _confirm.value = transform(_confirm.value)
    }

    internal fun updateTransfer(transform: (TransferState) -> TransferState) {
        _transfer.value = transform(_transfer.value)
    }

    internal fun updatePreviewSession(transform: (PreviewSessionState) -> PreviewSessionState) {
        _previewSession.value = transform(_previewSession.value)
    }

    internal fun updateUpdateUi(transform: (UpdateUiState) -> UpdateUiState) {
        _updateUi.value = transform(_updateUi.value)
    }

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

    /**
     * live 调参写漏斗（P5 状态收敛）：不记历史（与原各 update* 直接写 var 一致，
     * 撤销只经 applyTuningParams/captureUndo 路径），同步更新 params 流触发重组。
     */
    fun updateLive(transform: (TuningParams) -> TuningParams) {
        _params.value = transform(_params.value)
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
