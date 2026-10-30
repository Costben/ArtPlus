package dev.artplus.mobile.ui.pages.presets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Delete
import io.github.suqi8.coui.kmp.icon.extended.Folder
import io.github.suqi8.coui.kmp.icon.extended.More
import io.github.suqi8.coui.kmp.icon.extended.Search
import io.github.suqi8.coui.kmp.icon.extended.Show
import io.github.suqi8.coui.kmp.basic.EmptyState
import io.github.suqi8.coui.kmp.basic.NotificationSwipeAction
import io.github.suqi8.coui.kmp.basic.NotificationSwipeBox
import io.github.suqi8.coui.kmp.basic.NotificationSwipeDefaults
import io.github.suqi8.coui.kmp.basic.rememberNotificationSwipeState
import io.github.suqi8.coui.kmp.core.ExperimentalCOUIApi
import io.github.suqi8.coui.kmp.basic.TagView
import io.github.suqi8.coui.kmp.basic.TagViewDefaults
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TextField
import io.github.suqi8.coui.kmp.basic.TextFieldMode
import io.github.suqi8.coui.kmp.basic.TextButton
import io.github.suqi8.coui.kmp.theme.COUITheme
import dev.artplus.mobile.TuningParams
import dev.artplus.mobile.TuningPreset
import dev.artplus.mobile.ui.cards.CompactPresetRow
import dev.artplus.mobile.ui.cards.formatPresetDate
import dev.artplus.mobile.ui.components.AppTagCornerRadius
import dev.artplus.mobile.ui.components.SectionCard
import dev.artplus.mobile.ui.components.SectionHeader

/**
 * Slice 2.1：预设页卡片 UI（原 MainActivity.PresetStatusCard / PresetLibraryCard 原样搬迁）。
 * Composable 只收 state + onEvent；Activity 状态经参数/回调注入，行为与 UI 100% 等价。
 */
@Composable
internal fun PresetStatusCard(
    presets: List<TuningPreset>,
    activePresetId: String?,
    activePresetBaseParams: TuningParams?,
    currentParams: TuningParams,
    isBusy: Boolean,
    onOverwrite: (TuningPreset) -> Unit,
    onRequestSavePreset: (String) -> Unit,
    onResetToPreset: (TuningPreset) -> Unit,
    onResetToDefaults: () -> Unit,
) {
    val activePreset = presets.firstOrNull { it.id == activePresetId }
    val isPresetModified = activePreset != null && (activePresetBaseParams == null || !currentParams.sameAs(activePresetBaseParams ?: activePreset.params))

    SectionCard(title = "活动预设") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (activePreset != null) {
                                if (isPresetModified) COUITheme.colorScheme.primaryVariant.copy(alpha = 0.6f) else COUITheme.colorScheme.primaryVariant
                            } else {
                                COUITheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.4f)
                            }
                        ),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = when {
                                activePreset != null -> "当前生效：${activePreset.name}"
                                else -> "当前生效：自定义调参"
                            },
                            style = COUITheme.textStyles.title4.copy(fontWeight = FontWeight.Bold),
                            color = COUITheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (activePreset != null && isPresetModified) {
                            // 状态标签：COUI TagView，tertiaryContainer 蓝色淡底
                            TagView(
                                text = "已修改",
                                cornerRadius = AppTagCornerRadius,
                                colors = TagViewDefaults.tagViewColors(
                                    containerColor = COUITheme.colorScheme.tertiaryContainer,
                                    contentColor = COUITheme.colorScheme.onTertiaryContainer,
                                ),
                            )
                        }
                    }
                    Text(
                        text = when {
                            activePreset != null && isPresetModified -> "与快照有参数差异 · ${currentParams.diffSummary(activePreset.params)}"
                            activePreset != null -> "与快照保持一致 · 更新于 ${formatPresetDate(activePreset.updatedAt)}"
                            else -> "未绑定预设快照 · 可保存为独立快照"
                        },
                        style = COUITheme.textStyles.footnote1,
                        color = COUITheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (activePreset != null && isPresetModified) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TextButton(
                            text = "覆盖更新",
                            onClick = { onOverwrite(activePreset) },
                            enabled = !isBusy,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = "另存为",
                            onClick = {
                                onRequestSavePreset("${activePreset.name} (副本)")
                            },
                            enabled = !isBusy,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    TextButton(
                        text = "重置到快照",
                        onClick = { onResetToPreset(activePreset) },
                        enabled = !isBusy,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else if (activePreset != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        text = "另存为",
                        onClick = {
                            onRequestSavePreset("${activePreset.name} (副本)")
                        },
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        text = "恢复出厂",
                        onClick = { onResetToDefaults() },
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        text = "保存为预设",
                        onClick = {
                            onRequestSavePreset("")
                        },
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        text = "恢复出厂",
                        onClick = { onResetToDefaults() },
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
internal fun PresetLibraryCard(
    presets: List<TuningPreset>,
    activePresetId: String?,
    activePresetBaseParams: TuningParams?,
    currentParams: TuningParams,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    listExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    isBusy: Boolean,
    onApply: (TuningPreset) -> Unit,
    onPreview: (TuningPreset) -> Unit,
    onMore: (TuningPreset) -> Unit,
    onDelete: (TuningPreset) -> Unit = {},
) {
    val activePreset = presets.firstOrNull { it.id == activePresetId }

    val filtered = remember(presets, searchQuery) {
        if (searchQuery.isBlank()) presets
        else presets.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }

    val displayList = remember(filtered, activePresetId, listExpanded, searchQuery) {
        if (searchQuery.isNotBlank() || listExpanded || filtered.size <= 5) {
            filtered
        } else {
            val result = mutableListOf<TuningPreset>()
            val active = filtered.firstOrNull { it.id == activePresetId }
            if (active != null) {
                result.add(active)
            }
            filtered.forEach { p ->
                if (p.id != activePresetId && result.size < 5) {
                    result.add(p)
                }
            }
            result
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionHeader("预设列表")

        if (presets.size >= 8) {
            // 搜索框：surface 圆角底 + COUI TextField（无自带底，带清除按钮）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(COUITheme.colorScheme.surface),
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { onSearchChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    backgroundMode = TextFieldMode.None,
                    label = "搜索预设名称...",
                    showClearButton = true,
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = COUIIcons.Search,
                            contentDescription = "搜索预设",
                            modifier = Modifier.size(16.dp),
                            tint = COUITheme.colorScheme.onSurfaceVariantSummary,
                        )
                    },
                )
            }
        }

        if (presets.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(vertical = 24.dp, horizontal = 16.dp),
            ) {
                EmptyState(
                    title = "暂无预设快照",
                    subtitle = "在「生成参数」微调好效果后，点击上方「保存为预设」即可创建",
                    modifier = Modifier.fillMaxWidth(),
                    illustration = {
                        Icon(
                            imageVector = COUIIcons.Folder,
                            contentDescription = "暂无预设",
                            modifier = Modifier.size(36.dp),
                            tint = COUITheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.4f),
                        )
                    },
                )
            }
        } else if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "未找到包含「$searchQuery」的预设",
                    style = COUITheme.textStyles.footnote1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        } else {
            displayList.forEach { preset ->
                val isActive = preset.id == activePresetId
                val isModified = isActive && (activePresetBaseParams == null || !currentParams.sameAs(activePresetBaseParams ?: preset.params))
                NotificationPresetCard(
                    preset = preset,
                    isActive = isActive,
                    isModified = isModified,
                    busy = isBusy,
                    onApply = { onApply(preset) },
                    onPreview = { onPreview(preset) },
                    onMore = { onMore(preset) },
                    onDelete = { onDelete(preset) },
                )
            }

            if (presets.size > 5 && searchQuery.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleExpanded() }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (listExpanded) "收起 ▴" else "展开查看全部 (共 ${presets.size} 个) ▾",
                        style = COUITheme.textStyles.footnote1.copy(fontWeight = FontWeight.Medium),
                        color = COUITheme.colorScheme.primaryVariant,
                    )
                }
            }
        }
    }
}

/**
 * 基于 COUI17 NotificationSwipeBox 的通知式滑动盒预设项：
 * - 采用 COUI 原生通知卡片液态融合（MetaBallSurface）、边缘光照与弹簧手感；
 * - 正常态为标准组件底色（surface）；
 * - 生效态描蓝色边框（border），名称与描述均以蓝色文字高亮呈现；
 * - 左滑拉出 2 个操作按钮：更多 (More) 在左、删除 (Delete) 在右；向右滑动或点击收回。
 */
@OptIn(ExperimentalCOUIApi::class)
@Composable
internal fun NotificationPresetCard(
    preset: TuningPreset,
    isActive: Boolean,
    isModified: Boolean,
    busy: Boolean,
    onApply: () -> Unit,
    onPreview: () -> Unit,
    onMore: () -> Unit,
    onDelete: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val swipeState = rememberNotificationSwipeState()

    val deleteColor = COUITheme.colorScheme.error
    val actions = remember(onMore, onDelete, deleteColor) {
        listOf(
            NotificationSwipeAction(
                icon = COUIIcons.More,
                description = "更多",
                onClick = onMore,
            ),
            NotificationSwipeAction(
                icon = null,
                description = "删除",
                onClick = onDelete,
                color = deleteColor,
            ),
        )
    }

    val cardShape = RoundedCornerShape(NotificationSwipeDefaults.CardCornerRadius)

    NotificationSwipeBox(
        actions = actions,
        modifier = Modifier.fillMaxWidth(),
        state = swipeState,
        enabled = !busy,
        minHeight = 0.dp,
        containerColor = COUITheme.colorScheme.surfaceContainer,
        actionColor = COUITheme.colorScheme.onSurface,
        rimColor = Color.Transparent,
        dismissOnFullSwipe = true,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isActive) {
                        Modifier.border(1.5.dp, COUITheme.colorScheme.primary, cardShape)
                    } else {
                        Modifier
                    }
                )
                .clickable(enabled = !busy) {
                    if (swipeState.isRevealed) {
                        coroutineScope.launch { swipeState.close() }
                    } else {
                        onApply()
                    }
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) {
                            COUITheme.colorScheme.primary
                        } else {
                            Color.Transparent
                        }
                    ),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = preset.name,
                    style = COUITheme.textStyles.body1.copy(
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    ),
                    color = if (isActive) COUITheme.colorScheme.primary else COUITheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = formatPresetDate(preset.updatedAt) + if (isActive) (if (isModified) " · 已套用 (已修改)" else " · 已套用") else "",
                    style = COUITheme.textStyles.footnote2,
                    color = if (isActive) COUITheme.colorScheme.primary.copy(alpha = 0.8f) else COUITheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
