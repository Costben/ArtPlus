package dev.artplus.mobile.ui.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.Lucide
import java.util.Locale
import kotlinx.coroutines.isActive
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.IconButton
import io.github.suqi8.coui.kmp.basic.CardDefaults
import io.github.suqi8.coui.kmp.basic.TabRowDefaults
import io.github.suqi8.coui.kmp.basic.TabRowWithContour
import io.github.suqi8.coui.kmp.basic.TagView
import io.github.suqi8.coui.kmp.basic.Button
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.TabRow
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.More
import io.github.suqi8.coui.kmp.theme.COUITheme
import dev.artplus.mobile.TuningPreset
import dev.artplus.mobile.ui.components.AppTagCornerRadius
import dev.artplus.mobile.ui.components.CompactCardCornerRadius
import dev.artplus.mobile.ui.components.KernelStyleArrow
import dev.artplus.mobile.ui.components.NumberInputBox
import dev.artplus.mobile.ui.components.SettingsIconKind
import dev.artplus.mobile.ui.components.SettingsLineIcon
import dev.artplus.mobile.ui.components.SteppedPercentSlider
import dev.artplus.mobile.ui.components.settingsIconForTitle

internal fun formatScale(value: Float): String =
    String.format(Locale.US, "%.2f", value)

/**
 * 分段控件的禁用态包装：COUI TabRow 系列没有 enabled 参数，
 * 禁用时降低透明度并用透明层吞掉触摸，行为与原先 enabled=false 等价。
 */
@Composable
private fun SegmentEnabledScope(
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f),
    ) {
        content()
        if (!enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            )
        }
    }
}

@Composable
internal fun SegmentedControl(
    labels: List<String>,
    selectedIndex: Int,
    scrollable: Boolean = false,
    enabled: Boolean = true,
    onSelected: (Int) -> Unit,
) {
    if (labels.isEmpty()) {
        return
    }
    val safeSelectedIndex = selectedIndex.coerceIn(0, labels.lastIndex)
    SegmentEnabledScope(enabled = enabled) {
        // COUI 分段按钮：自带滑动轮廓动画；scrollable 时每段至少 86dp，超出可横向滚动。
        TabRowWithContour(
            tabs = labels,
            selectedTabIndex = safeSelectedIndex,
            onTabSelected = { index ->
                if (enabled && index != safeSelectedIndex) {
                    onSelected(index)
                }
            },
            minWidth = if (scrollable) 86.dp else TabRowDefaults.TabRowWithContourMinWidth,
        )
    }
}

@Composable
internal fun NumberParameterControl(
    title: String,
    summary: String,
    value: Int,
    draftText: String,
    min: Int,
    max: Int,
    step: Int = 1,
    onDraftChange: (String) -> Unit,
    onSave: (Int) -> Unit,
    enabled: Boolean = true,
    busy: Boolean = false,
    icon: SettingsIconKind? = null,
    showIcon: Boolean = true,
    initiallyExpanded: Boolean = false,
    inputBackgroundColor: Color? = null,
    standaloneCard: Boolean = false,
    cardHeight: Dp = 78.dp,
) {
    val controlEnabled = enabled && !busy
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val body: @Composable ColumnScope.() -> Unit = {
        // 头部行：COUI BasicComponent，按压反馈由 COUI 指示器提供
        BasicComponent(
            modifier = (if (standaloneCard) Modifier.heightIn(min = cardHeight) else Modifier)
                .semantics {
                    stateDescription = if (expanded) "已展开拖动条" else "已收起拖动条"
                },
            title = title,
            summary = summary,
            startAction = if (showIcon) {
                { SettingsLineIcon(kind = icon ?: settingsIconForTitle(title)) }
            } else {
                null
            },
            endActions = {
                // 输入框区域消费点击，避免误触切换折叠
                Box(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = controlEnabled,
                        onClick = {},
                    ),
                ) {
                    NumberInputBox(
                        value = draftText,
                        fallbackValue = value,
                        min = min,
                        max = max,
                        enabled = controlEnabled,
                        onValueChange = onDraftChange,
                        onDone = { submitted ->
                            submitted.toIntOrNull()
                                ?.coerceIn(min, max)
                                ?.let(onSave)
                        },
                        backgroundColor = inputBackgroundColor,
                    )
                }
                KernelStyleArrow(expanded = expanded)
            },
            onClick = { expanded = !expanded },
            role = Role.Button,
            enabled = controlEnabled,
        )
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(durationMillis = 150)) +
                expandVertically(animationSpec = tween(durationMillis = 180)),
            exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                shrinkVertically(animationSpec = tween(durationMillis = 160)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // 全出血行展开后用最小高度（52dp）提供呼吸感；standaloneCard 分支自带底部 12dp。
                    .heightIn(min = if (standaloneCard) 0.dp else 52.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = if (standaloneCard) 12.dp else 0.dp),
                contentAlignment = Alignment.Center,
            ) {
                SteppedPercentSlider(
                    value = value,
                    min = min,
                    max = max,
                    step = step,
                    enabled = controlEnabled,
                    onValueChange = onSave,
                )
            }
        }
    }
    if (standaloneCard) {
        // 底部弹层内的独立卡片：中性 secondaryVariant 填充，避免与面板同色
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(0.dp),
            colors = CardDefaults.defaultColors(color = COUITheme.colorScheme.secondaryVariant),
            content = body,
        )
    } else {
        Column(modifier = Modifier.fillMaxWidth(), content = body)
    }
}

@Composable
internal fun CompactPresetRow(
    preset: TuningPreset,
    isActive: Boolean,
    isModified: Boolean,
    onApply: () -> Unit,
    onPreview: () -> Unit,
    onMore: () -> Unit,
    busy: Boolean = false,
) {
    // 选中态用 COUI 蓝色淡底卡片（tertiaryContainer），未选中用中性 secondaryVariant
    val containerColor = if (isActive) {
        COUITheme.colorScheme.tertiaryContainer
    } else {
        COUITheme.colorScheme.secondaryVariant
    }
    val activeTextColor = COUITheme.colorScheme.onTertiaryContainer
    val tags = remember(preset.params) { preset.featureTags() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = CompactCardCornerRadius,
        // 3dp vertical margin keeps the 54dp card height while the icon buttons take 48dp.
        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 3.dp),
        colors = CardDefaults.defaultColors(color = containerColor),
        onClick = if (busy) null else onApply,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) {
                            if (isModified) COUITheme.colorScheme.primaryVariant.copy(alpha = 0.6f) else COUITheme.colorScheme.primaryVariant
                        } else {
                            Color.Transparent
                        }
                    ),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = preset.name,
                        style = COUITheme.textStyles.body1.copy(
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (isActive) activeTextColor else COUITheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    tags.forEach { tag ->
                        TagView(text = tag, cornerRadius = AppTagCornerRadius)
                    }
                }

                Text(
                    text = formatPresetDate(preset.updatedAt) + if (isActive) (if (isModified) " · 已套用 (已修改)" else " · 已套用") else "",
                    style = COUITheme.textStyles.footnote2,
                    color = if (isActive) activeTextColor.copy(alpha = 0.8f) else COUITheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(
                onClick = onPreview,
                enabled = !busy,
                modifier = Modifier.size(48.dp),
                backgroundColor = Color.Transparent,
                cornerRadius = 8.dp,
            ) {
                Icon(
                    imageVector = Lucide.Eye,
                    contentDescription = "批量预览",
                    modifier = Modifier.size(16.dp),
                    tint = COUITheme.colorScheme.onSurfaceVariantSummary,
                )
            }

            IconButton(
                onClick = onMore,
                enabled = !busy,
                modifier = Modifier.size(48.dp),
                backgroundColor = Color.Transparent,
                cornerRadius = 8.dp,
            ) {
                Icon(
                    imageVector = COUIIcons.More,
                    contentDescription = "更多操作",
                    modifier = Modifier.size(16.dp),
                    tint = COUITheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

internal fun formatPresetDate(timestamp: Long): String {
    val calendar = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
    return String.format(
        Locale.getDefault(),
        "%04d-%02d-%02d %02d:%02d",
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH) + 1,
        calendar.get(java.util.Calendar.DAY_OF_MONTH),
        calendar.get(java.util.Calendar.HOUR_OF_DAY),
        calendar.get(java.util.Calendar.MINUTE),
    )
}

@Composable
internal fun PresetMenuItem(
    title: String,
    summary: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    // COUI 行：按压反馈、标题/摘要配色与禁用态均由 BasicComponent 提供
    BasicComponent(
        title = title,
        summary = summary,
        onClick = onClick,
        enabled = enabled,
    )
}

@Composable
internal fun AdvancedCategoryTabs(
    selected: AdvancedSettingsCategory,
    onSelected: (AdvancedSettingsCategory) -> Unit,
    enabled: Boolean = true,
) {
    val categories = AdvancedSettingsCategory.entries
    val safeSelectedIndex = categories.indexOf(selected).coerceAtLeast(0)
    SegmentEnabledScope(enabled = enabled) {
        // 分类导航用无轮廓的 COUI TabRow，与上方轮廓分段按钮形成层级
        TabRow(
            tabs = categories.map { it.label },
            selectedTabIndex = safeSelectedIndex,
            onTabSelected = { index ->
                if (enabled && index != safeSelectedIndex) {
                    onSelected(categories[index])
                }
            },
        )
    }
}
