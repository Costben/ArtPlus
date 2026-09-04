package dev.artplus.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Slice 2.3：液态玻璃设置卡片（原 MainActivity 本体原样搬迁）。
 * 只做纯移动：文案/布局/参数范围一律不变。
 * Activity 状态（tuningState/isBusy/draft 文本）经参数注入，调参存取（update*，Slice 2.4）
 * 经 onSave/onCheckedChange 回调注入；MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动。
 */

@Composable
internal fun LiquidGlassToggleCard(
    enabled: Boolean,
    isBusy: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SectionCard(rowsFullBleed = true) {
        LibrarySettingRow(
            title = "液态玻璃风格",
            summary = "开启后按当前液态玻璃参数重绘背景和前景光影",
            icon = SettingsIconKind.Glass,
            showSwitch = true,
            checked = enabled,
            enabled = !isBusy,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
internal fun LiquidGlassSurfaceCard(
    tuningState: TuningParams,
    isBusy: Boolean,
    draftRadiusText: String,
    onDraftRadiusChange: (String) -> Unit,
    onSaveRadius: (Int) -> Unit,
    draftOuterWidthText: String,
    onDraftOuterWidthChange: (String) -> Unit,
    onSaveOuterWidth: (Int) -> Unit,
    draftTopAlphaText: String,
    onDraftTopAlphaChange: (String) -> Unit,
    onSaveTopAlpha: (Int) -> Unit,
    draftBottomAlphaText: String,
    onDraftBottomAlphaChange: (String) -> Unit,
    onSaveBottomAlpha: (Int) -> Unit,
    draftBackgroundMistAlphaText: String,
    onDraftBackgroundMistAlphaChange: (String) -> Unit,
    onSaveBackgroundMistAlpha: (Int) -> Unit,
    draftBottomDarkAlphaText: String,
    onDraftBottomDarkAlphaChange: (String) -> Unit,
    onSaveBottomDarkAlpha: (Int) -> Unit,
) {
    SectionCard(rowsFullBleed = true) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NumberParameterControl(
                busy = isBusy,
                title = "玻璃圆角",
                summary = "控制玻璃遮罩圆角，背景与主体按同一轮廓裁剪",
                value = tuningState.liquidGlassRadius,
                draftText = draftRadiusText,
                min = MIN_LIQUID_GLASS_RADIUS,
                max = MAX_LIQUID_GLASS_RADIUS,
                onDraftChange = onDraftRadiusChange,
                onSave = onSaveRadius,
                icon = SettingsIconKind.Radius,
            )
            NumberParameterControl(
                busy = isBusy,
                title = "外框高度",
                summary = "控制玻璃外缘高光的厚度",
                value = tuningState.liquidGlassOuterWidth,
                draftText = draftOuterWidthText,
                min = MIN_LIQUID_GLASS_OUTER_WIDTH,
                max = MAX_LIQUID_GLASS_OUTER_WIDTH,
                step = 1,
                onDraftChange = onDraftOuterWidthChange,
                onSave = onSaveOuterWidth,
                icon = SettingsIconKind.Glass,
            )
            NumberParameterControl(
                busy = isBusy,
                title = "顶部强度",
                summary = "控制顶边贴边高光的亮度",
                value = tuningState.liquidGlassTopAlpha,
                draftText = draftTopAlphaText,
                min = MIN_LIQUID_GLASS_ALPHA,
                max = MAX_LIQUID_GLASS_ALPHA,
                step = 1,
                onDraftChange = onDraftTopAlphaChange,
                onSave = onSaveTopAlpha,
                icon = SettingsIconKind.Spark,
            )
            NumberParameterControl(
                busy = isBusy,
                title = "底边强度",
                summary = "控制底边贴边高光的亮度",
                value = tuningState.liquidGlassBottomAlpha,
                draftText = draftBottomAlphaText,
                min = MIN_LIQUID_GLASS_ALPHA,
                max = MAX_LIQUID_GLASS_ALPHA,
                step = 1,
                onDraftChange = onDraftBottomAlphaChange,
                onSave = onSaveBottomAlpha,
                icon = SettingsIconKind.Spark,
            )
            NumberParameterControl(
                busy = isBusy,
                title = "背景灰雾",
                summary = "给图标背景叠加均匀暗雾，降低整体亮度",
                value = tuningState.liquidGlassBackgroundMistAlpha,
                draftText = draftBackgroundMistAlphaText,
                min = MIN_LIQUID_GLASS_MIST_ALPHA,
                max = MAX_LIQUID_GLASS_MIST_ALPHA,
                step = 1,
                onDraftChange = onDraftBackgroundMistAlphaChange,
                onSave = onSaveBackgroundMistAlpha,
                icon = SettingsIconKind.Shadow,
            )
            NumberParameterControl(
                busy = isBusy,
                title = "底部灰雾",
                summary = "给底部叠加暗雾渐变，压住底边亮度",
                value = tuningState.liquidGlassBottomDarkAlpha,
                draftText = draftBottomDarkAlphaText,
                min = MIN_LIQUID_GLASS_BOTTOM_DARK_ALPHA,
                max = MAX_LIQUID_GLASS_BOTTOM_DARK_ALPHA,
                step = 1,
                onDraftChange = onDraftBottomDarkAlphaChange,
                onSave = onSaveBottomDarkAlpha,
                icon = SettingsIconKind.Shadow,
            )
        }
    }
}

@Composable
internal fun LiquidGlassSubjectCard(
    tuningState: TuningParams,
    isBusy: Boolean,
    draftSubjectScaleText: String,
    onDraftSubjectScaleChange: (String) -> Unit,
    onSaveSubjectScale: (Int) -> Unit,
    draftSubjectOutlineWidthText: String,
    onDraftSubjectOutlineWidthChange: (String) -> Unit,
    onSaveSubjectOutlineWidth: (Int) -> Unit,
    draftSubjectInnerOutlineWidthText: String,
    onDraftSubjectInnerOutlineWidthChange: (String) -> Unit,
    onSaveSubjectInnerOutlineWidth: (Int) -> Unit,
    draftSubjectShadowAlphaText: String,
    onDraftSubjectShadowAlphaChange: (String) -> Unit,
    onSaveSubjectShadowAlpha: (Int) -> Unit,
    draftSubjectOpacityText: String,
    onDraftSubjectOpacityChange: (String) -> Unit,
    onSaveSubjectOpacity: (Int) -> Unit,
) {
    SectionCard(rowsFullBleed = true) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NumberParameterControl(
                busy = isBusy,
                title = "主体比例",
                summary = "调整主体在玻璃层中的缩放比例",
                value = tuningState.liquidGlassSubjectScalePercent,
                draftText = draftSubjectScaleText,
                min = MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT,
                max = MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT,
                step = 1,
                onDraftChange = onDraftSubjectScaleChange,
                onSave = onSaveSubjectScale,
                icon = SettingsIconKind.Scale,
            )
            NumberParameterControl(
                busy = isBusy,
                title = "主体外框宽度",
                summary = "沿主体外侧透明边界添加高光描边",
                value = tuningState.liquidGlassSubjectOutlineWidth,
                draftText = draftSubjectOutlineWidthText,
                min = MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                max = MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                step = 1,
                onDraftChange = onDraftSubjectOutlineWidthChange,
                onSave = onSaveSubjectOutlineWidth,
                icon = SettingsIconKind.Spark,
            )
            NumberParameterControl(
                busy = isBusy,
                title = "主体内框宽度",
                summary = "沿主体内侧透明边界添加高光描边",
                value = tuningState.liquidGlassSubjectInnerOutlineWidth,
                draftText = draftSubjectInnerOutlineWidthText,
                min = MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                max = MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                step = 1,
                onDraftChange = onDraftSubjectInnerOutlineWidthChange,
                onSave = onSaveSubjectInnerOutlineWidth,
                icon = SettingsIconKind.Spark,
            )
            NumberParameterControl(
                busy = isBusy,
                title = "主体阴影",
                summary = "控制主体投影透明度，增强层次",
                value = tuningState.liquidGlassSubjectShadowAlpha,
                draftText = draftSubjectShadowAlphaText,
                min = MIN_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA,
                max = MAX_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA,
                step = 1,
                onDraftChange = onDraftSubjectShadowAlphaChange,
                onSave = onSaveSubjectShadowAlpha,
                icon = SettingsIconKind.Shadow,
            )
            NumberParameterControl(
                busy = isBusy,
                title = "主体透明度",
                summary = "归一化主体后再控制整体不透明度",
                value = tuningState.liquidGlassSubjectOpacityPercent,
                draftText = draftSubjectOpacityText,
                min = MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
                max = MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
                step = 1,
                onDraftChange = onDraftSubjectOpacityChange,
                onSave = onSaveSubjectOpacity,
                icon = SettingsIconKind.Glass,
            )
        }
    }
}

@Composable
internal fun LiquidGlassToggleRow(
    enabled: Boolean,
    isBusy: Boolean,
    onToggle: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val bleedPx = with(LocalDensity.current) { CHOICE_ROW_HORIZONTAL_BLEED_DP.dp.roundToPx() }
    val bridge = LocalSectionCardPressBridge.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .trackSectionPress(bridge, pressed)
            .cardRowBleed(bleedPx)
            .background(cardRowPressedColor(pressed))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isBusy,
                onClick = onToggle,
            )
            .padding(horizontal = CHOICE_ROW_HORIZONTAL_BLEED_DP.dp)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsLineIcon(kind = SettingsIconKind.Glass)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = "液态玻璃风格",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "开启后按当前液态玻璃参数重绘背景和前景光影",
                modifier = Modifier.basicMarquee(),
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                softWrap = false,
            )
        }
        LiquidGlassSwitch(checked = enabled, enabled = !isBusy)
    }
}
