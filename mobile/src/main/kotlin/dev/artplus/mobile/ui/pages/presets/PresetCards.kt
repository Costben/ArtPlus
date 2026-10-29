package dev.artplus.mobile.ui.pages.presets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Folder
import io.github.suqi8.coui.kmp.icon.extended.Search
import io.github.suqi8.coui.kmp.basic.EmptyState
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

    SectionCard {
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

    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "预设快照库 (${presets.size})",
                    style = COUITheme.textStyles.title4.copy(fontWeight = FontWeight.Bold),
                    color = COUITheme.colorScheme.onSurface,
                )
                if (presets.isNotEmpty()) {
                    Text(
                        text = "轻按条目套用",
                        style = COUITheme.textStyles.footnote2,
                        color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }

            if (presets.size >= 8) {
                // 搜索框：secondaryVariant 圆角底 + COUI TextField（无自带底，带清除按钮）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(COUITheme.colorScheme.secondaryVariant),
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
                EmptyState(
                    title = "暂无预设快照",
                    subtitle = "在「生成参数」微调好效果后，点击上方「保存为预设」即可创建",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    illustration = {
                        Icon(
                            imageVector = COUIIcons.Folder,
                            contentDescription = "暂无预设",
                            modifier = Modifier.size(36.dp),
                            tint = COUITheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.4f),
                        )
                    },
                )
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    displayList.forEach { preset ->
                        val isActive = preset.id == activePresetId
                        val isModified = isActive && (activePresetBaseParams == null || !currentParams.sameAs(activePresetBaseParams ?: preset.params))
                        CompactPresetRow(
                            busy = isBusy,
                            preset = preset,
                            isActive = isActive,
                            isModified = isModified,
                            onApply = { onApply(preset) },
                            onPreview = { onPreview(preset) },
                            onMore = { onMore(preset) },
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
    }
}
