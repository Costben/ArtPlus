package dev.artplus.mobile.ui.components

import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Cpu
import com.composables.icons.lucide.GlassWater
import com.composables.icons.lucide.KeyRound
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Palette
import com.composables.icons.lucide.Radius
import com.composables.icons.lucide.Scale
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.Sparkles
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.DropdownEntry
import io.github.suqi8.coui.kmp.basic.Switch
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TextField
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.basic.ArrowUpDown
import io.github.suqi8.coui.kmp.icon.extended.Clear
import io.github.suqi8.coui.kmp.icon.extended.GridView
import io.github.suqi8.coui.kmp.icon.extended.Import
import io.github.suqi8.coui.kmp.icon.extended.Layers
import io.github.suqi8.coui.kmp.icon.extended.Link
import io.github.suqi8.coui.kmp.icon.extended.Messages
import io.github.suqi8.coui.kmp.icon.extended.Ok
import io.github.suqi8.coui.kmp.icon.extended.Refresh
import io.github.suqi8.coui.kmp.icon.extended.Settings
import io.github.suqi8.coui.kmp.icon.extended.Tune
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.TextFieldMode
import io.github.suqi8.coui.kmp.layout.DialogButtonBar
import io.github.suqi8.coui.kmp.layout.DialogButtonBarAction
import io.github.suqi8.coui.kmp.preference.ArrowPreference
import io.github.suqi8.coui.kmp.preference.SwitchPreference
import io.github.suqi8.coui.kmp.preference.WindowDropdownPreference
import io.github.suqi8.coui.kmp.window.WindowDialog

/** 行右侧的值文本：ColorOS 右对齐次级文本。 */
@Composable
private fun RowValueText(value: String, enabled: Boolean) {
    Text(
        text = value,
        style = COUITheme.textStyles.body2,
        // 值文本与 WindowDropdownPreference 的值一致取 onSurfaceSecondary（8A）；onSurfaceVariantActions 仅 26%/30% 透明度，暗色下对比过低。
        color = if (enabled) {
            COUITheme.colorScheme.onSurfaceSecondary
        } else {
            COUITheme.colorScheme.disabledOnSecondaryVariant
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * 标准设置行：基于 COUI BasicComponent / ArrowPreference / SwitchPreference，
 * 按压反馈由 COUI 指示器提供并被所在 Card 圆角裁切。
 */
@Composable
internal fun LibrarySettingRow(
    title: String,
    summary: String?,
    icon: SettingsIconKind? = null,
    value: String? = null,
    showValue: Boolean = true,
    showArrowUpDown: Boolean = false,
    showArrowRight: Boolean = false,
    showSwitch: Boolean = false,
    checked: Boolean = false,
    enabled: Boolean = true,
    showIcon: Boolean = icon != null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val summaryText = summary?.takeIf { it.isNotBlank() }
    val startAction: (@Composable () -> Unit)? = if (showIcon && icon != null) {
        { SettingsLineIcon(kind = icon) }
    } else {
        null
    }
    val hasValue = value != null && showValue
    // 保持旧语义：存在 onCheckedChange 时整行点击优先切换开关，否则走 onClick。
    val rowClick: (() -> Unit)? = if (onCheckedChange != null) {
        { onCheckedChange(!checked) }
    } else {
        onClick
    }
    when {
        showSwitch && onCheckedChange != null -> SwitchPreference(
            checked = checked,
            onCheckedChange = onCheckedChange,
            title = title,
            summary = summaryText,
            startAction = startAction,
            endActions = {
                if (hasValue) RowValueText(value = value.orEmpty(), enabled = enabled)
            },
            enabled = enabled,
        )
        showArrowRight -> ArrowPreference(
            title = title,
            summary = summaryText,
            startAction = startAction,
            endActions = {
                if (hasValue) RowValueText(value = value.orEmpty(), enabled = enabled)
            },
            onClick = rowClick,
            enabled = enabled,
        )
        else -> BasicComponent(
            title = title,
            summary = summaryText,
            startAction = startAction,
            endActions = if (hasValue || showSwitch || showArrowUpDown) {
                {
                    if (hasValue) RowValueText(value = value.orEmpty(), enabled = enabled)
                    if (showSwitch) {
                        Switch(
                            checked = checked,
                            onCheckedChange = null,
                            enabled = enabled,
                        )
                    }
                    if (showArrowUpDown) {
                        Image(
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(width = 10.dp, height = 16.dp),
                            imageVector = COUIIcons.Basic.ArrowUpDown,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                if (enabled) {
                                    COUITheme.colorScheme.onSurfaceVariantActions
                                } else {
                                    COUITheme.colorScheme.disabledOnSecondaryVariant
                                },
                            ),
                        )
                    }
                }
            } else {
                null
            },
            onClick = rowClick,
            enabled = enabled,
        )
    }
}

/** 标准选择行：WindowDropdownPreference 自带触觉、值文本、箭头与手指定位；窗口级弹窗保证层级在底栏之上。 */
@Composable
internal fun LibraryChoiceRow(
    title: String,
    summary: String?,
    value: String?,
    icon: SettingsIconKind,
    entry: DropdownEntry,
    enabled: Boolean,
    summaryBelow: Boolean = false,
) {
    val summaryText = summary?.takeIf { it.isNotBlank() }
    // value 由 entry 中被选中项的文本决定（WindowDropdownPreference 自行展示），参数仅为保持调用兼容。
    // summaryBelow：摘要较长（如文件名 · 大小 · 标签）时放进 bottomAction，占满整行宽度，
    // 不再与右侧值文本抢中间列宽度而被挤成多行、从文件名中间断开；左侧按图标宽度 18dp + 间距 8dp 对齐标题。
    WindowDropdownPreference(
        entry = entry,
        title = title,
        summary = if (summaryBelow) null else summaryText,
        bottomAction = if (summaryBelow && summaryText != null) {
            {
                Text(
                    text = summaryText,
                    modifier = Modifier.padding(start = 26.dp),
                    style = COUITheme.textStyles.body2,
                    color = if (enabled) {
                        COUITheme.colorScheme.onSurfaceVariantSummary
                    } else {
                        COUITheme.colorScheme.disabledOnSecondaryVariant
                    },
                )
            }
        } else {
            null
        },
        startAction = { SettingsLineIcon(kind = icon) },
        enabled = enabled,
        showValue = true,
    )
}

@Composable
internal fun SettingsTextInputRow(
    title: String,
    value: String,
    label: String,
    inputHint: String,
    icon: SettingsIconKind,
    obscure: Boolean = false,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
) {
    var dialogVisible by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf(value) }
    LaunchedEffect(value) {
        draft = value
    }
    LibrarySettingRow(
        title = title,
        summary = when {
            value.isBlank() -> "$label · 未设置"
            obscure -> "••••••••"
            else -> value
        },
        icon = icon,
        showValue = false,
        showArrowRight = true,
        enabled = enabled,
        onClick = {
            draft = value
            dialogVisible = true
        },
    )
    // WindowDialog 不给内容加内边距：输入框自带 24dp 水平边距，按钮栏贴边铺满。
    WindowDialog(
        show = dialogVisible,
        title = title,
        onDismissRequest = { dialogVisible = false },
        onDismissFinished = { },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                backgroundMode = TextFieldMode.Line,
                label = inputHint,
                singleLine = true,
                showClearButton = true,
                showPasswordToggle = obscure,
            )
            Spacer(modifier = Modifier.height(16.dp))
            DialogButtonBar(
                negative = DialogButtonBarAction(
                    text = "取消",
                    onClick = { dialogVisible = false },
                ),
                positive = DialogButtonBarAction(
                    text = "确定",
                    recommended = true,
                    onClick = {
                        onValueChange(draft)
                        dialogVisible = false
                    },
                ),
            )
        }
    }
}

/** 库标准信息展示行：无交互、右侧可选值标记。 */
@Composable
internal fun SettingsInfoRow(
    title: String,
    summary: String?,
    value: String?,
    icon: SettingsIconKind,
) {
    LibrarySettingRow(
        title = title,
        summary = summary,
        value = value,
        icon = icon,
        enabled = true,
    )
}

@Composable
internal fun SettingLine(
    title: String,
    summary: String,
    value: String = "",
    showIcon: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    BasicComponent(
        title = title,
        summary = summary.takeIf { it.isNotBlank() },
        startAction = if (showIcon) {
            { SettingsLineIcon(kind = settingsIconForTitle(title)) }
        } else {
            null
        },
        endActions = if (value.isNotBlank()) {
            { RowValueText(value = value, enabled = enabled) }
        } else {
            null
        },
        onClick = onClick,
        enabled = enabled,
    )
}

@Composable
internal fun settingsIconForTitle(title: String): SettingsIconKind =
    when (title) {
        "目标应用" -> SettingsIconKind.Grid
        "应用预设" -> SettingsIconKind.Layers
        "应用范围" -> SettingsIconKind.Grid
        "应用列表" -> SettingsIconKind.Grid
        "显示系统应用" -> SettingsIconKind.Shield
        "已生成" -> SettingsIconKind.CheckBadge
        "使用情况访问" -> SettingsIconKind.Shield
        "Root 目标" -> SettingsIconKind.Shield
        "外部导出" -> SettingsIconKind.FileUpload
        "导出到外部目录" -> SettingsIconKind.FileUpload
        "备份到外部目录" -> SettingsIconKind.FileUpload
        "导出引导" -> SettingsIconKind.FileUpload
        "RMBG 状态" -> SettingsIconKind.Chip
        "模型版本" -> SettingsIconKind.Layers
        "AI image two 生成模式" -> SettingsIconKind.Spark
        "AI 提示词" -> SettingsIconKind.Prompt
        "液态玻璃风格" -> SettingsIconKind.Glass
        "渲染方式" -> SettingsIconKind.Layers
        "圆角半径" -> SettingsIconKind.Radius
        "边缘高光宽度" -> SettingsIconKind.Glass
        "主体占比" -> SettingsIconKind.Scale
        "前景主体大小" -> SettingsIconKind.Scale
        "预览圆角" -> SettingsIconKind.Radius
        "预览缩放" -> SettingsIconKind.Grid
        "主体阴影等级" -> SettingsIconKind.Shadow
        "单色主体缩放" -> SettingsIconKind.Scale
        "背景剔除阈值" -> SettingsIconKind.Cutout
        "背景相似度" -> SettingsIconKind.Cutout
        "底板颜色阈值" -> SettingsIconKind.Plate
        "底板清理" -> SettingsIconKind.Plate
        "长阴影清理强度" -> SettingsIconKind.Shadow
        "旧阴影清理" -> SettingsIconKind.Eraser
        "毛刺优化" -> SettingsIconKind.Spark
        "边缘修补" -> SettingsIconKind.Spark
        "RMBG Alpha 强度" -> SettingsIconKind.Cutout
        "Alpha 力度" -> SettingsIconKind.Cutout
        "RMBG 边缘柔化" -> SettingsIconKind.Cutout
        "边缘柔化" -> SettingsIconKind.Cutout
        "RMBG 边缘扩张" -> SettingsIconKind.Scale
        "边缘扩缩" -> SettingsIconKind.Scale
        "RMBG 弱透明保留" -> SettingsIconKind.Cutout
        "弱透明保留" -> SettingsIconKind.Cutout
        "自动确认写入" -> SettingsIconKind.Shield
        "自动确认刷新" -> SettingsIconKind.Refresh
        "单色缩放" -> SettingsIconKind.Scale
        else -> SettingsIconKind.Dot
    }

internal enum class SettingsIconKind {
    Grid,
    CheckBadge,
    Shield,
    FileUpload,
    Glass,
    Radius,
    Chip,
    Layers,
    Spark,
    Scale,
    Cutout,
    Palette,
    Plate,
    Shadow,
    Eraser,
    Link,
    Key,
    Prompt,
    Refresh,
    Dot,
}

internal fun settingsIconVector(kind: SettingsIconKind): ImageVector = when (kind) {
    SettingsIconKind.Grid -> COUIIcons.GridView
    SettingsIconKind.CheckBadge -> COUIIcons.Ok
    // COUI has no equivalent, Lucide retained.
    SettingsIconKind.Shield -> Lucide.Shield
    SettingsIconKind.FileUpload -> COUIIcons.Import
    // COUI has no equivalent, Lucide retained.
    SettingsIconKind.Glass -> Lucide.GlassWater
    // COUI has no equivalent, Lucide retained.
    SettingsIconKind.Radius -> Lucide.Radius
    // COUI has no equivalent, Lucide retained.
    SettingsIconKind.Chip -> Lucide.Cpu
    SettingsIconKind.Layers -> COUIIcons.Layers
    // COUI has no equivalent, Lucide retained.
    SettingsIconKind.Spark -> Lucide.Sparkles
    // COUI has no equivalent, Lucide retained.
    SettingsIconKind.Scale -> Lucide.Scale
    SettingsIconKind.Cutout -> COUIIcons.Tune
    // COUI has no equivalent, Lucide retained.
    SettingsIconKind.Palette -> Lucide.Palette
    // COUI has no equivalent, Lucide retained.
    SettingsIconKind.Plate -> Lucide.Palette
    // COUI has no equivalent, Lucide retained.
    SettingsIconKind.Shadow -> Lucide.Sparkles
    SettingsIconKind.Eraser -> COUIIcons.Clear
    SettingsIconKind.Link -> COUIIcons.Link
    // COUI has no equivalent, Lucide retained.
    SettingsIconKind.Key -> Lucide.KeyRound
    SettingsIconKind.Prompt -> COUIIcons.Messages
    SettingsIconKind.Refresh -> COUIIcons.Refresh
    SettingsIconKind.Dot -> COUIIcons.Settings
}

@Composable
internal fun SettingsLineIcon(kind: SettingsIconKind, modifier: Modifier = Modifier) {
    Icon(
        imageVector = settingsIconVector(kind),
        contentDescription = null,
        modifier = modifier.size(18.dp),
        tint = COUITheme.colorScheme.onSurface,
    )
}

