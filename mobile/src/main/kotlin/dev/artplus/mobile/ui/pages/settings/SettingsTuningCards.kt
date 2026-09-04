package dev.artplus.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Slice 2.3：本地管线/规则/RMBG 调参卡片（原 MainActivity 本体原样搬迁）。
 * 只做纯移动：文案/布局/参数范围一律不变。
 * Activity 状态（tuningState/isBusy/draft 文本）经参数注入，调参存取（update*，Slice 2.4）
 * 经 onSave/onToggle 回调注入；MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动。
 */

@Composable
internal fun LocalSeparationModeControl(
    tuningState: TuningParams,
    isBusy: Boolean,
    onSelect: (LocalSeparationMode) -> Unit,
) {
    val modes = LocalSeparationMode.entries.filterNot { it == LocalSeparationMode.Plate }
    val selectedMode = if (LocalSeparationMode.fromValue(tuningState.localSeparationMode) == LocalSeparationMode.Plate) {
        LocalSeparationMode.Full
    } else {
        LocalSeparationMode.fromValue(tuningState.localSeparationMode)
    }
    SegmentedControl(
        enabled = !isBusy,
        labels = modes.map { it.label },
        selectedIndex = modes.indexOf(selectedMode).coerceAtLeast(0),
        scrollable = true,
        onSelected = { index ->
            onSelect(modes[index])
        },
    )
}

@Composable
internal fun LocalRuleTuningCard(
    tuningState: TuningParams,
    isBusy: Boolean,
    draftBackgroundSeparationText: String,
    onDraftBackgroundSeparationChange: (String) -> Unit,
    onSaveBackgroundSeparation: (Int) -> Unit,
    draftPlateRemovalText: String,
    onDraftPlateRemovalChange: (String) -> Unit,
    onSavePlateRemoval: (Int) -> Unit,
    draftShadowRemovalText: String,
    onDraftShadowRemovalChange: (String) -> Unit,
    onSaveShadowRemoval: (Int) -> Unit,
    draftEdgePolishText: String,
    onDraftEdgePolishChange: (String) -> Unit,
    onSaveEdgePolish: (Int) -> Unit,
) {
    SectionCard(rowsFullBleed = true) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NumberParameterControl(
                busy = isBusy,
                title = "背景相似度",
                summary = "越高越容易把相近颜色当背景",
                value = tuningState.backgroundSeparationPercent,
                draftText = draftBackgroundSeparationText,
                min = MIN_BACKGROUND_SEPARATION_PERCENT,
                max = MAX_BACKGROUND_SEPARATION_PERCENT,
                onDraftChange = onDraftBackgroundSeparationChange,
                onSave = onSaveBackgroundSeparation,
                icon = SettingsIconKind.Cutout,
            )
            NumberParameterControl(
                busy = isBusy,
                title = "底板清理",
                summary = "越高越容易移除纯色底板",
                value = tuningState.plateRemovalPercent,
                draftText = draftPlateRemovalText,
                min = MIN_PLATE_REMOVAL_PERCENT,
                max = MAX_PLATE_REMOVAL_PERCENT,
                onDraftChange = onDraftPlateRemovalChange,
                onSave = onSavePlateRemoval,
                icon = SettingsIconKind.Plate,
            )
            NumberParameterControl(
                busy = isBusy,
                title = "旧阴影清理",
                summary = "清掉原图里的长阴影，不是新增阴影",
                value = tuningState.shadowRemovalPercent,
                draftText = draftShadowRemovalText,
                min = MIN_SHADOW_REMOVAL_PERCENT,
                max = MAX_SHADOW_REMOVAL_PERCENT,
                onDraftChange = onDraftShadowRemovalChange,
                onSave = onSaveShadowRemoval,
                icon = SettingsIconKind.Eraser,
            )
            NumberParameterControl(
                busy = isBusy,
                title = "边缘修补",
                summary = "修补抠图毛刺和半透明边",
                value = tuningState.edgePolishPercent,
                draftText = draftEdgePolishText,
                min = MIN_EDGE_POLISH_PERCENT,
                max = MAX_EDGE_POLISH_PERCENT,
                onDraftChange = onDraftEdgePolishChange,
                onSave = onSaveEdgePolish,
                icon = SettingsIconKind.Spark,
            )
        }
    }
}

@Composable
internal fun LocalWorkflowPipelineCard(
    tuningState: TuningParams,
    isBusy: Boolean,
    onToggle: (key: String, enabled: Boolean) -> Unit,
) {
    SectionCard(rowsFullBleed = true) {
        LocalWorkflowToggleRow("背景估计与相减", "普通图标和 Adaptive 图标的背景分离", tuningState.localBackgroundSeparationEnabled, "background", isBusy, onToggle)
        LocalWorkflowToggleRow("Adaptive 自动选层", "在合成前景与直接前景之间自动判断", tuningState.localAdaptiveSelectionEnabled, "adaptive", isBusy, onToggle)
        LocalWorkflowToggleRow("角落蒙版清理", "清理 Adaptive 四角残留", tuningState.localCornerMaskCleanupEnabled, "corner", isBusy, onToggle)
        LocalWorkflowToggleRow("透明边缘补色", "修复本地抠图透明边的颜色残留", tuningState.localAlphaEdgeColorRepairEnabled, "alpha_edge_repair", isBusy, onToggle)
        LocalWorkflowToggleRow("普通背景估计", "关闭后跳过普通背景相减与拼合", tuningState.localPlainBackgroundEstimationEnabled, "plain_background", isBusy, onToggle)
        LocalWorkflowToggleRow("原始前景清理", "应用原始前景的底板清理规则", tuningState.localOriginalCleanupEnabled, "original", isBusy, onToggle)
        LocalWorkflowToggleRow("底板清理", "检测并移除连接到边缘的底板", tuningState.localPlateCleanupEnabled, "plate", isBusy, onToggle)
        LocalWorkflowToggleRow("底板修边", "修复底板移除后的边缘颜色", tuningState.localPlateEdgeRepairEnabled, "plate_edge", isBusy, onToggle)
        LocalWorkflowToggleRow("彩色残留清理", "清除底板颜色在主体边缘的残留", tuningState.localPlateResidueCleanupEnabled, "plate_residue", isBusy, onToggle)
        LocalWorkflowToggleRow("长阴影清理", "移除原图中偏移的长阴影", tuningState.localShadowCleanupEnabled, "shadow", isBusy, onToggle)
        LocalWorkflowToggleRow("阴影边缘修复", "保留阴影交界处的抗锯齿边缘", tuningState.localShadowEdgeRepairEnabled, "shadow_edge", isBusy, onToggle)
        LocalWorkflowToggleRow("前景收边", "执行局部侵蚀和边缘羽化", tuningState.localEdgeTrimEnabled, "edge_trim", isBusy, onToggle)
        LocalWorkflowToggleRow("拼合背景候选", "生成主体与重建背景的组合候选", tuningState.localComposedBackgroundEnabled, "composed", isBusy, onToggle)
        LocalWorkflowToggleRow("二层候选", "运行底板/主体分层候选算法", tuningState.localTwoLayerCandidateEnabled, "two_layer", isBusy, onToggle)
        LocalWorkflowToggleRow("组件候选", "生成底座作为主体或背景的候选", tuningState.localComponentCandidatesEnabled, "component", isBusy, onToggle)
        LocalWorkflowToggleRow("字标保全候选", "保留更完整文字的安全候选", tuningState.localTextSafeCandidateEnabled, "text_safe", isBusy, onToggle)
        LocalWorkflowToggleRow("自动候选选择", "关闭后固定使用完整清理结果", tuningState.localAutoSelectionEnabled, "auto", isBusy, onToggle)
        LocalWorkflowToggleRow("本地最终边缘润色", "渲染本地候选时执行最后的边缘处理", tuningState.localEdgePolishEnabled, "edge_polish", isBusy, onToggle)
    }
}

@Composable
internal fun LocalWorkflowToggleRow(
    title: String,
    summary: String,
    checked: Boolean,
    key: String,
    isBusy: Boolean,
    onCheckedChange: (key: String, enabled: Boolean) -> Unit,
) {
    LibrarySettingRow(
        title = title,
        summary = summary,
        icon = SettingsIconKind.Cutout,
        showSwitch = true,
        checked = checked,
        enabled = !isBusy,
        onCheckedChange = { onCheckedChange(key, it) },
    )
}

@Composable
internal fun RmbgTuningCard(
    tuningState: TuningParams,
    isBusy: Boolean,
    draftAlphaStrengthText: String,
    onDraftAlphaStrengthChange: (String) -> Unit,
    onSaveAlphaStrength: (Int) -> Unit,
    draftEdgeFeatherText: String,
    onDraftEdgeFeatherChange: (String) -> Unit,
    onSaveEdgeFeather: (Int) -> Unit,
    draftEdgeAdjustText: String,
    onDraftEdgeAdjustChange: (String) -> Unit,
    onSaveEdgeAdjust: (Int) -> Unit,
    draftWeakAlphaKeepText: String,
    onDraftWeakAlphaKeepChange: (String) -> Unit,
    onSaveWeakAlphaKeep: (Int) -> Unit,
) {
    SectionCard(rowsFullBleed = true) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NumberParameterControl(
                busy = isBusy,
                title = "Alpha 力度",
                summary = "100 不变，越高越实",
                value = tuningState.rmbgAlphaStrengthPercent,
                draftText = draftAlphaStrengthText,
                min = MIN_RMBG_ALPHA_STRENGTH_PERCENT,
                max = MAX_RMBG_ALPHA_STRENGTH_PERCENT,
                onDraftChange = onDraftAlphaStrengthChange,
                onSave = onSaveAlphaStrength,
                icon = SettingsIconKind.Cutout,
            )
            NumberParameterControl(
                busy = isBusy,
                title = "边缘柔化",
                summary = "越高边缘越软",
                value = tuningState.rmbgEdgeFeatherPercent,
                draftText = draftEdgeFeatherText,
                min = MIN_RMBG_EDGE_FEATHER_PERCENT,
                max = MAX_RMBG_EDGE_FEATHER_PERCENT,
                onDraftChange = onDraftEdgeFeatherChange,
                onSave = onSaveEdgeFeather,
                icon = SettingsIconKind.Cutout,
            )
            NumberParameterControl(
                busy = isBusy,
                title = "边缘扩缩",
                summary = "低收缩，高扩张",
                value = tuningState.rmbgEdgeAdjustPercent,
                draftText = draftEdgeAdjustText,
                min = MIN_RMBG_EDGE_ADJUST_PERCENT,
                max = MAX_RMBG_EDGE_ADJUST_PERCENT,
                onDraftChange = onDraftEdgeAdjustChange,
                onSave = onSaveEdgeAdjust,
                icon = SettingsIconKind.Scale,
            )
            NumberParameterControl(
                busy = isBusy,
                title = "弱透明保留",
                summary = "越高越保留半透明细节",
                value = tuningState.rmbgWeakAlphaKeepPercent,
                draftText = draftWeakAlphaKeepText,
                min = MIN_RMBG_WEAK_ALPHA_KEEP_PERCENT,
                max = MAX_RMBG_WEAK_ALPHA_KEEP_PERCENT,
                onDraftChange = onDraftWeakAlphaKeepChange,
                onSave = onSaveWeakAlphaKeep,
                icon = SettingsIconKind.Cutout,
            )
        }
    }
}
