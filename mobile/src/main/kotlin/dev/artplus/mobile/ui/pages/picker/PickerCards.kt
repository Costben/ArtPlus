package dev.artplus.mobile.ui.pages.picker

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Search
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.Checkbox
import io.github.suqi8.coui.kmp.basic.CardDefaults
import io.github.suqi8.coui.kmp.basic.TagView
import io.github.suqi8.coui.kmp.basic.TagViewDefaults
import io.github.suqi8.coui.kmp.basic.TextField
import io.github.suqi8.coui.kmp.basic.TextFieldMode
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TextButton
import io.github.suqi8.coui.kmp.theme.COUITheme
import dev.artplus.mobile.data.AppEntry
import dev.artplus.mobile.ui.cards.GeneratedFilter
import dev.artplus.mobile.ui.cards.SegmentedControl
import dev.artplus.mobile.ui.components.AppListTag
import dev.artplus.mobile.ui.components.AppStatusTag
import dev.artplus.mobile.ui.components.AppTagCornerRadius
import dev.artplus.mobile.ui.components.CompactActionButton
import dev.artplus.mobile.ui.components.CompactCardCornerRadius
import dev.artplus.mobile.ui.components.LibrarySettingRow
import dev.artplus.mobile.ui.components.SectionCard
import dev.artplus.mobile.ui.components.SectionDivider
import dev.artplus.mobile.ui.components.SettingsIconKind

/**
 * Slice 2.5：应用选择器组件与行（原 MainActivity 残留本体原样搬迁）。
 * 只做物理搬迁+显式参数化：Composable 只收 state + onEvent；
 * 名单外动作（addLiquidGlass/applyCurrentPresetBatch）转为回调注入，不移动本体。
 * MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动。
 * 名单内联 helper 扫描结论：本簇无内联 helper（SectionCard/Tag/按钮系收敛进
 * ui/components，只做调用）。
 */

@Composable
internal fun AppPickerStatusCard(
    filteredCount: Int,
    totalCount: Int,
    generatedCount: Int,
    ungeneratedCount: Int,
    multiCount: Int,
    isScanning: Boolean,
    scanFailed: Boolean,
    isBusy: Boolean,
    hasApps: Boolean,
    onRefreshGenerated: () -> Unit,
    onReloadApps: () -> Unit,
) {
    val statusText = buildString {
        append("$filteredCount/$totalCount")
        append(" · 已生成 $generatedCount")
        append(" · 未生成 $ungeneratedCount")
        if (isScanning) {
            append(" · 扫描中")
        } else if (scanFailed) {
            append(" · 无法读取 data 路径")
        }
        if (multiCount > 0) {
            append(" · 多选 $multiCount")
        }
    }
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
                            if (multiCount > 0) COUITheme.colorScheme.primaryVariant
                            else COUITheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.4f),
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
                            text = "应用列表 $filteredCount/$totalCount",
                            style = COUITheme.textStyles.title4.copy(fontWeight = FontWeight.Bold),
                            color = COUITheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (multiCount > 0) {
                            TagView(
                                text = "多选 $multiCount",
                                cornerRadius = AppTagCornerRadius,
                                colors = TagViewDefaults.tagViewColors(
                                    containerColor = COUITheme.colorScheme.tertiaryContainer,
                                    contentColor = COUITheme.colorScheme.onTertiaryContainer,
                                ),
                            )
                        }
                    }
                    Text(
                        text = statusText,
                        style = COUITheme.textStyles.footnote1,
                        color = COUITheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    text = "刷新生成",
                    onClick = onRefreshGenerated,
                    enabled = !isBusy && hasApps,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = "更新列表",
                    onClick = onReloadApps,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
internal fun AppPickerFilterCard(
    showSystemApps: Boolean,
    generatedFilter: GeneratedFilter,
    isBusy: Boolean,
    onToggleSystemApps: () -> Unit,
    onFilterSelected: (GeneratedFilter) -> Unit,
) {
    SectionCard(rowsFullBleed = true) {
        LibrarySettingRow(
            title = "显示系统应用",
            summary = if (showSystemApps) "已包含系统应用，可搜索和批量选择" else "仅显示用户应用；系统应用已隐藏",
            icon = SettingsIconKind.Shield,
            showSwitch = true,
            checked = showSystemApps,
            enabled = !isBusy,
            onCheckedChange = { onToggleSystemApps() },
        )
        SectionDivider()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 12.dp),
        ) {
            val filters = GeneratedFilter.entries
            SegmentedControl(
                enabled = !isBusy,
                labels = filters.map { it.label },
                selectedIndex = filters.indexOf(generatedFilter),
                onSelected = { index ->
                    onFilterSelected(filters[index])
                },
            )
        }
    }
}

@Composable
internal fun AppPickerSearchCard(
    queryText: String,
    isBusy: Boolean,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    multiSelectContent: @Composable () -> Unit,
) {
    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 搜索条：COUI TextField（无底线模式）置于中性胶囊底板内，高度与按钮对齐 48dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(COUITheme.colorScheme.secondaryVariant)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                TextField(
                    value = queryText,
                    onValueChange = { text ->
                        // 清除按钮把文本置空：沿用原 onClearQuery 路径
                        if (text.isEmpty() && queryText.isNotEmpty()) onClearQuery() else onQueryChange(text)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    backgroundMode = TextFieldMode.None,
                    label = "搜索应用或包名...",
                    singleLine = true,
                    showClearButton = true,
                    leadingIcon = {
                        Icon(
                            imageVector = COUIIcons.Search,
                            contentDescription = "搜索",
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(16.dp),
                            tint = COUITheme.colorScheme.onSurfaceVariantSummary,
                        )
                    },
                )
            }
            multiSelectContent()
        }
    }
}

@Composable
internal fun AppPickerControlsCard(
    filteredCount: Int,
    totalCount: Int,
    generatedCount: Int,
    ungeneratedCount: Int,
    multiCount: Int,
    isScanning: Boolean,
    scanFailed: Boolean,
    isBusy: Boolean,
    hasApps: Boolean,
    showSystemApps: Boolean,
    generatedFilter: GeneratedFilter,
    queryText: String,
    onRefreshGenerated: () -> Unit,
    onReloadApps: () -> Unit,
    onToggleSystemApps: () -> Unit,
    onFilterSelected: (GeneratedFilter) -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    multiSelectContent: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppPickerStatusCard(
            filteredCount = filteredCount,
            totalCount = totalCount,
            generatedCount = generatedCount,
            ungeneratedCount = ungeneratedCount,
            multiCount = multiCount,
            isScanning = isScanning,
            scanFailed = scanFailed,
            isBusy = isBusy,
            hasApps = hasApps,
            onRefreshGenerated = onRefreshGenerated,
            onReloadApps = onReloadApps,
        )
        AppPickerFilterCard(
            showSystemApps = showSystemApps,
            generatedFilter = generatedFilter,
            isBusy = isBusy,
            onToggleSystemApps = onToggleSystemApps,
            onFilterSelected = onFilterSelected,
        )
        AppPickerSearchCard(
            queryText = queryText,
            isBusy = isBusy,
            onQueryChange = onQueryChange,
            onClearQuery = onClearQuery,
            multiSelectContent = multiSelectContent,
        )
    }
}

@Composable
internal fun AppMultiSelectActions(
    selectedCount: Int,
    hasFiltered: Boolean,
    allFilteredSelected: Boolean,
    isBusy: Boolean,
    onToggleFiltered: () -> Unit,
    onClear: () -> Unit,
    onAddGlass: () -> Unit,
    onApplyPreset: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CompactActionButton(
            text = if (allFilteredSelected) "取消当前" else "选择当前",
            onClick = onToggleFiltered,
            enabled = !isBusy && hasFiltered,
            modifier = Modifier.weight(1f),
            height = 48.dp,
        )
        CompactActionButton(
            text = "清空",
            onClick = onClear,
            enabled = !isBusy && selectedCount > 0,
            modifier = Modifier.weight(1f),
            height = 48.dp,
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CompactActionButton(
            text = "添加光影 $selectedCount",
            onClick = onAddGlass,
            enabled = !isBusy && selectedCount > 0,
            modifier = Modifier.weight(1f),
            height = 48.dp,
        )
        CompactActionButton(
            text = "套用当前预设",
            onClick = onApplyPreset,
            enabled = !isBusy && selectedCount > 0,
            modifier = Modifier.weight(1f),
            height = 48.dp,
        )
    }
}

@Composable
internal fun AppRow(
    entry: AppEntry,
    selected: Boolean,
    multiSelected: Boolean,
    generated: Boolean,
    isBusy: Boolean,
    onClick: () -> Unit,
    onToggleMultiSelect: () -> Unit,
    icon: @Composable () -> Unit,
) {
    val selectedTagBg = COUITheme.colorScheme.primaryVariant
    val selectedTagFg = COUITheme.colorScheme.onPrimary
    val multiSelectedTagBg = COUITheme.colorScheme.tertiaryContainer
    val multiSelectedTagFg = COUITheme.colorScheme.onTertiaryContainer
    // tertiaryContainer 在 COUI 中是蓝色淡染，「已生成」改用中性色，避免与「已选 / 多选」同色相
    val generatedTagBg = COUITheme.colorScheme.secondaryContainer
    val generatedTagFg = COUITheme.colorScheme.onSecondaryContainer
    val allTagBg = COUITheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
    val allTagFg = COUITheme.colorScheme.onSecondaryContainer
    val tags = remember(
        selected,
        multiSelected,
        generated,
        entry.launchable,
        selectedTagBg,
        selectedTagFg,
        multiSelectedTagBg,
        multiSelectedTagFg,
        generatedTagBg,
        generatedTagFg,
        allTagBg,
        allTagFg,
    ) {
        buildList {
            if (selected) add(AppListTag("已选", selectedTagBg, selectedTagFg))
            if (multiSelected) add(AppListTag("多选", multiSelectedTagBg, multiSelectedTagFg))
            if (generated) add(AppListTag("已生成", generatedTagBg, generatedTagFg))
            if (!entry.launchable) add(AppListTag("全部", allTagBg, allTagFg))
        }
    }

    // 选中态：蓝色淡染叠在卡片底色上（tertiaryContainer 为半透明，需先合成才不会透出页面底色）
    val cardBase = COUITheme.colorScheme.surfaceContainer
    val tint = COUITheme.colorScheme.tertiaryContainer
    val containerBg = when {
        selected -> tint.compositeOver(cardBase)
        multiSelected -> tint.copy(alpha = tint.alpha * 0.6f).compositeOver(cardBase)
        else -> cardBase
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = CompactCardCornerRadius,
        insideMargin = PaddingValues(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        colors = CardDefaults.defaultColors(color = containerBg),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // 复选框：多选清单语义，去掉旧的"选择/已选"文字按钮与箭头
            Checkbox(
                state = ToggleableState(multiSelected),
                onClick = onToggleMultiSelect,
                enabled = !isBusy,
            )
            icon()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = entry.label,
                    modifier = Modifier.basicMarquee(),
                    style = COUITheme.textStyles.body1.copy(fontWeight = FontWeight(550)),
                    color = if (selected) COUITheme.colorScheme.primaryVariant else COUITheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                )
                Text(
                    text = entry.packageName,
                    modifier = Modifier.basicMarquee(),
                    style = COUITheme.textStyles.footnote2.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight(550),
                    ),
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    softWrap = false,
                )
            }
            if (tags.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(start = 8.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    tags.forEach { tag ->
                        AppStatusTag(tag = tag)
                    }
                }
            }
        }
    }
}

@Composable
internal fun AppIcon(
    entry: AppEntry,
    size: Dp,
    getCached: (String) -> Bitmap?,
    loadIcon: suspend () -> Bitmap?,
) {
    var bitmap by remember(entry.iconKey) {
        mutableStateOf(getCached(entry.iconKey))
    }
    val imageBitmap = remember(bitmap) { bitmap?.asImageBitmap() }

    LaunchedEffect(entry.iconKey) {
        if (bitmap == null) {
            bitmap = loadIcon()
        }
    }

    val current: ImageBitmap? = imageBitmap
    Box(
        modifier = Modifier
            .size(size),
        contentAlignment = Alignment.Center,
    ) {
        if (current == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(COUITheme.colorScheme.secondaryContainer),
            )
        } else {
            Image(
                bitmap = current,
                contentDescription = "应用图标",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

/** 供非重构调用点复用的纯选中态计算（保持原分支语义）。 */
internal fun pickerAllFilteredSelected(
    filteredPackageNames: Set<String>,
    multiSelected: Set<String>,
): Boolean =
    filteredPackageNames.isNotEmpty() && filteredPackageNames.all { it in multiSelected }
