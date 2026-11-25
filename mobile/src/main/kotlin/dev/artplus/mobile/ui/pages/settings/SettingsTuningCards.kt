package dev.artplus.mobile.ui.pages.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.CardListPosition
import io.github.suqi8.coui.kmp.basic.Chip
import io.github.suqi8.coui.kmp.basic.ExpandArrow
import io.github.suqi8.coui.kmp.basic.SmallTitle
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.theme.COUITheme
import dev.artplus.mobile.MAX_BACKGROUND_SEPARATION_PERCENT
import dev.artplus.mobile.MAX_EDGE_POLISH_PERCENT
import dev.artplus.mobile.MAX_PLATE_REMOVAL_PERCENT
import dev.artplus.mobile.MAX_RMBG_ALPHA_STRENGTH_PERCENT
import dev.artplus.mobile.MAX_RMBG_EDGE_ADJUST_PERCENT
import dev.artplus.mobile.MAX_RMBG_EDGE_FEATHER_PERCENT
import dev.artplus.mobile.MAX_RMBG_WEAK_ALPHA_KEEP_PERCENT
import dev.artplus.mobile.MAX_SHADOW_REMOVAL_PERCENT
import dev.artplus.mobile.MIN_BACKGROUND_SEPARATION_PERCENT
import dev.artplus.mobile.MIN_EDGE_POLISH_PERCENT
import dev.artplus.mobile.MIN_PLATE_REMOVAL_PERCENT
import dev.artplus.mobile.MIN_RMBG_ALPHA_STRENGTH_PERCENT
import dev.artplus.mobile.MIN_RMBG_EDGE_ADJUST_PERCENT
import dev.artplus.mobile.MIN_RMBG_EDGE_FEATHER_PERCENT
import dev.artplus.mobile.MIN_RMBG_WEAK_ALPHA_KEEP_PERCENT
import dev.artplus.mobile.MIN_SHADOW_REMOVAL_PERCENT
import dev.artplus.mobile.TuningParams
import dev.artplus.mobile.imaging.LocalSeparationMode
import dev.artplus.mobile.ui.cards.NumberParameterControl
import dev.artplus.mobile.ui.cards.SegmentedControl
import dev.artplus.mobile.ui.cards.ToggleNumberParameterControl
import dev.artplus.mobile.ui.components.LibrarySettingRow
import dev.artplus.mobile.ui.components.SectionCard
import dev.artplus.mobile.ui.components.SectionDivider
import dev.artplus.mobile.ui.components.SettingsIconKind
import dev.artplus.mobile.ui.pages.params.LocalTuningGroup
import dev.artplus.mobile.ui.pages.params.LocalTuningStylePreset
import dev.artplus.mobile.ui.pages.params.countEnabledLocalWorkflowSteps
import dev.artplus.mobile.ui.pages.params.matchingLocalStylePreset

/**
 * 本地管线/规则/RMBG 调参卡片。
 * 稳定规则按「快捷风格 → 核心处理（开关+强度一体）→ 候选策略 → 默认收起的底层细项」四层组织。
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

/** 顶部快捷风格：4 颗 COUI Chip 一键套用常用组合，并展示当前风格的简短说明。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LocalStylePresetsCard(
    tuningState: TuningParams,
    isBusy: Boolean,
    onSelectStyle: (LocalTuningStylePreset) -> Unit,
) {
    val activeStyle = remember(tuningState) { matchingLocalStylePreset(tuningState) }
    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LocalTuningStylePreset.entries.forEach { preset ->
                    Chip(
                        selected = preset == activeStyle,
                        onClick = { onSelectStyle(preset) },
                        label = preset.label,
                        enabled = !isBusy,
                    )
                }
            }
            Text(
                text = activeStyle?.summary ?: "当前为自定义参数，点上方风格可一键恢复对应档位",
                style = COUITheme.textStyles.footnote1,
                color = COUITheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

/** 核心处理：把原来的 4 个滑杆与对应主/子开关合体为 4 个直观调节项。 */
@Composable
internal fun LocalCoreTuningCard(
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
    onToggleGroup: (LocalTuningGroup, Boolean) -> Unit,
) {
    SectionCard(rowsFullBleed = true) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ToggleNumberParameterControl(
                title = "去除背景",
                summary = "把图标周围与背景相近的颜色扣掉",
                checked = tuningState.localBackgroundSeparationEnabled,
                onCheckedChange = { onToggleGroup(LocalTuningGroup.Background, it) },
                value = tuningState.backgroundSeparationPercent,
                draftText = draftBackgroundSeparationText,
                min = MIN_BACKGROUND_SEPARATION_PERCENT,
                max = MAX_BACKGROUND_SEPARATION_PERCENT,
                onDraftChange = onDraftBackgroundSeparationChange,
                onSave = onSaveBackgroundSeparation,
                busy = isBusy,
                icon = SettingsIconKind.Cutout,
                cardListPosition = CardListPosition.Head,
            )
            ToggleNumberParameterControl(
                title = "清理底板",
                summary = "移除图标自带的纯色圆角或圆形底板",
                checked = tuningState.localPlateCleanupEnabled,
                onCheckedChange = { onToggleGroup(LocalTuningGroup.Plate, it) },
                value = tuningState.plateRemovalPercent,
                draftText = draftPlateRemovalText,
                min = MIN_PLATE_REMOVAL_PERCENT,
                max = MAX_PLATE_REMOVAL_PERCENT,
                onDraftChange = onDraftPlateRemovalChange,
                onSave = onSavePlateRemoval,
                busy = isBusy,
                icon = SettingsIconKind.Plate,
                cardListPosition = CardListPosition.Middle,
            )
            ToggleNumberParameterControl(
                title = "清理旧阴影",
                summary = "清掉原图标自带的长投影，不新增阴影",
                checked = tuningState.localShadowCleanupEnabled,
                onCheckedChange = { onToggleGroup(LocalTuningGroup.Shadow, it) },
                value = tuningState.shadowRemovalPercent,
                draftText = draftShadowRemovalText,
                min = MIN_SHADOW_REMOVAL_PERCENT,
                max = MAX_SHADOW_REMOVAL_PERCENT,
                onDraftChange = onDraftShadowRemovalChange,
                onSave = onSaveShadowRemoval,
                busy = isBusy,
                icon = SettingsIconKind.Eraser,
                cardListPosition = CardListPosition.Middle,
            )
            ToggleNumberParameterControl(
                title = "边缘润色",
                summary = "修补抠图毛刺、黑边与透明边残留",
                checked = tuningState.localEdgePolishEnabled,
                onCheckedChange = { onToggleGroup(LocalTuningGroup.EdgePolish, it) },
                value = tuningState.edgePolishPercent,
                draftText = draftEdgePolishText,
                min = MIN_EDGE_POLISH_PERCENT,
                max = MAX_EDGE_POLISH_PERCENT,
                onDraftChange = onDraftEdgePolishChange,
                onSave = onSaveEdgePolish,
                busy = isBusy,
                icon = SettingsIconKind.Spark,
                cardListPosition = CardListPosition.Tail,
            )
        }
    }
}

/** 候选策略：把原来晦涩的 5 个候选/选优开关收口成 2 个直观开关。 */
@Composable
internal fun LocalStrategyCard(
    tuningState: TuningParams,
    isBusy: Boolean,
    onToggleAutoSelection: (Boolean) -> Unit,
    onToggleMultiCandidates: (Boolean) -> Unit,
) {
    val multiCandidatesEnabled = tuningState.localComposedBackgroundEnabled ||
        tuningState.localTwoLayerCandidateEnabled ||
        tuningState.localComponentCandidatesEnabled ||
        tuningState.localTextSafeCandidateEnabled
    SectionCard(rowsFullBleed = true) {
        LibrarySettingRow(
            title = "自动挑选最佳方案",
            summary = "按图标结构自动选用更合适的结果；关闭则固定用完整清理",
            icon = SettingsIconKind.Spark,
            showSwitch = true,
            checked = tuningState.localAutoSelectionEnabled,
            enabled = !isBusy,
            cardListPosition = CardListPosition.Head,
            onCheckedChange = onToggleAutoSelection,
        )
        LibrarySettingRow(
            title = "生成多套备选方案",
            summary = "开启后在首页点预览图可切换拼合、二层、字标保全等备选",
            icon = SettingsIconKind.Layers,
            showSwitch = true,
            checked = multiCandidatesEnabled,
            enabled = !isBusy,
            cardListPosition = CardListPosition.Tail,
            onCheckedChange = onToggleMultiCandidates,
        )
    }
}

/**
 * 底层算法细项（默认收起）：完整保留 18 个底层开关并按处理阶段归类，
 * 平时不打扰主界面，排查单个图标异常时可展开逐项控制。
 */
@Composable
internal fun LocalAdvancedPipelineCard(
    tuningState: TuningParams,
    isBusy: Boolean,
    onToggle: (key: String, enabled: Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val enabledCount = remember(tuningState) { countEnabledLocalWorkflowSteps(tuningState) }

    SectionCard(rowsFullBleed = true) {
        BasicComponent(
            modifier = Modifier.semantics {
                stateDescription = if (expanded) "已展开底层算法细项" else "已收起底层算法细项"
            },
            title = "底层算法细项",
            summary = "已开启 $enabledCount / 18 项 · 仅在排查单个图标异常时调整",
            startAction = {
                dev.artplus.mobile.ui.components.SettingsLineIcon(kind = SettingsIconKind.Dot)
            },
            endActions = {
                ExpandArrow(expanded = expanded)
            },
            cardListPosition = if (expanded) CardListPosition.Head else CardListPosition.Full,
            showDivider = expanded,
            onClick = { expanded = !expanded },
            role = Role.Button,
            enabled = !isBusy,
        )
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(durationMillis = 150)) +
                expandVertically(animationSpec = tween(durationMillis = 180)),
            exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                shrinkVertically(animationSpec = tween(durationMillis = 160)),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                AdvancedGroupLabel("背景与选层")
                LocalWorkflowToggleRow("背景估计与相减", "普通图标和 Adaptive 图标的背景分离", tuningState.localBackgroundSeparationEnabled, "background", isBusy, CardListPosition.Middle, onToggle)
                LocalWorkflowToggleRow("普通背景估计", "关闭后跳过普通背景相减与拼合", tuningState.localPlainBackgroundEstimationEnabled, "plain_background", isBusy, CardListPosition.Middle, onToggle)
                LocalWorkflowToggleRow("Adaptive 自动选层", "在合成前景与直接前景之间自动判断", tuningState.localAdaptiveSelectionEnabled, "adaptive", isBusy, CardListPosition.Middle, onToggle)
                LocalWorkflowToggleRow("角落蒙版清理", "清理 Adaptive 四角残留", tuningState.localCornerMaskCleanupEnabled, "corner", isBusy, CardListPosition.None, onToggle)
                SectionDivider()

                AdvancedGroupLabel("底板清理")
                LocalWorkflowToggleRow("底板清理", "检测并移除连接到边缘的底板", tuningState.localPlateCleanupEnabled, "plate", isBusy, CardListPosition.Middle, onToggle)
                LocalWorkflowToggleRow("底板修边", "修复底板移除后的边缘颜色", tuningState.localPlateEdgeRepairEnabled, "plate_edge", isBusy, CardListPosition.Middle, onToggle)
                LocalWorkflowToggleRow("彩色残留清理", "清除底板颜色在主体边缘的残留", tuningState.localPlateResidueCleanupEnabled, "plate_residue", isBusy, CardListPosition.Middle, onToggle)
                LocalWorkflowToggleRow("原始前景清理", "应用原始前景的底板清理规则", tuningState.localOriginalCleanupEnabled, "original", isBusy, CardListPosition.None, onToggle)
                SectionDivider()

                AdvancedGroupLabel("阴影与边缘")
                LocalWorkflowToggleRow("长阴影清理", "移除原图中偏移的长阴影", tuningState.localShadowCleanupEnabled, "shadow", isBusy, CardListPosition.Middle, onToggle)
                LocalWorkflowToggleRow("阴影边缘修复", "保留阴影交界处的抗锯齿边缘", tuningState.localShadowEdgeRepairEnabled, "shadow_edge", isBusy, CardListPosition.Middle, onToggle)
                LocalWorkflowToggleRow("透明边缘补色", "修复本地抠图透明边的颜色残留", tuningState.localAlphaEdgeColorRepairEnabled, "alpha_edge_repair", isBusy, CardListPosition.Middle, onToggle)
                LocalWorkflowToggleRow("前景收边", "执行局部侵蚀和边缘羽化", tuningState.localEdgeTrimEnabled, "edge_trim", isBusy, CardListPosition.Middle, onToggle)
                LocalWorkflowToggleRow("本地最终边缘润色", "渲染本地候选时执行最后的边缘处理", tuningState.localEdgePolishEnabled, "edge_polish", isBusy, CardListPosition.None, onToggle)
                SectionDivider()

                AdvancedGroupLabel("候选与选优")
                LocalWorkflowToggleRow("自动候选选择", "关闭后固定使用完整清理结果", tuningState.localAutoSelectionEnabled, "auto", isBusy, CardListPosition.Middle, onToggle)
                LocalWorkflowToggleRow("拼合背景候选", "生成主体与重建背景的组合候选", tuningState.localComposedBackgroundEnabled, "composed", isBusy, CardListPosition.Middle, onToggle)
                LocalWorkflowToggleRow("二层候选", "运行底板/主体分层候选算法", tuningState.localTwoLayerCandidateEnabled, "two_layer", isBusy, CardListPosition.Middle, onToggle)
                LocalWorkflowToggleRow("组件候选", "生成底座作为主体或背景的候选", tuningState.localComponentCandidatesEnabled, "component", isBusy, CardListPosition.Middle, onToggle)
                LocalWorkflowToggleRow("字标保全候选", "保留更完整文字的安全候选", tuningState.localTextSafeCandidateEnabled, "text_safe", isBusy, CardListPosition.Tail, onToggle)
            }
        }
    }
}

@Composable
private fun AdvancedGroupLabel(text: String) {
    SmallTitle(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        insideMargin = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp),
    )
}

@Composable
internal fun LocalWorkflowToggleRow(
    title: String,
    summary: String,
    checked: Boolean,
    key: String,
    isBusy: Boolean,
    cardListPosition: CardListPosition = CardListPosition.None,
    onCheckedChange: (key: String, enabled: Boolean) -> Unit,
) {
    LibrarySettingRow(
        title = title,
        summary = summary,
        icon = SettingsIconKind.Cutout,
        showSwitch = true,
        checked = checked,
        enabled = !isBusy,
        cardListPosition = cardListPosition,
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
                cardListPosition = CardListPosition.Head,
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
                cardListPosition = CardListPosition.Middle,
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
                cardListPosition = CardListPosition.Middle,
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
                cardListPosition = CardListPosition.Tail,
            )
        }
    }
}
