package dev.artplus.mobile

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.X
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.CheckboxDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
                            if (multiCount > 0) MiuixTheme.colorScheme.primaryVariant
                            else MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.4f),
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
                            style = MiuixTheme.textStyles.title4.copy(fontWeight = FontWeight.Bold),
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (multiCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = "多选 $multiCount",
                                    style = MiuixTheme.textStyles.footnote2.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                    ),
                                    color = MiuixTheme.colorScheme.primaryVariant,
                                )
                            }
                        }
                    }
                    Text(
                        text = statusText,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
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
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
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
            // 标准搜索条：复刻 PresetLibraryCard 的搜索实现，高度与按钮对齐 48dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    imageVector = Lucide.Search,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantSummary),
                )
                BasicTextField(
                    value = queryText,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MiuixTheme.textStyles.body2.copy(
                        color = MiuixTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(MiuixTheme.colorScheme.primaryVariant),
                    decorationBox = { innerTextField ->
                        if (queryText.isEmpty()) {
                            Text(
                                text = "搜索应用或包名...",
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.6f),
                            )
                        }
                        innerTextField()
                    },
                )
                if (queryText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .clickable { onClearQuery() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            imageVector = Lucide.X,
                            contentDescription = "清除",
                            modifier = Modifier.size(14.dp),
                            colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantSummary),
                        )
                    }
                }
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
    val selectedTagBg = MiuixTheme.colorScheme.primaryVariant
    val selectedTagFg = MiuixTheme.colorScheme.onPrimaryVariant
    val multiSelectedTagBg = MiuixTheme.colorScheme.primaryContainer
    val multiSelectedTagFg = MiuixTheme.colorScheme.onPrimaryContainer
    val generatedTagBg = MiuixTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
    val generatedTagFg = MiuixTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
    val allTagBg = MiuixTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
    val allTagFg = MiuixTheme.colorScheme.onSecondaryContainer
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

    val containerBg = when {
        selected -> MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        multiSelected -> MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        else -> MiuixTheme.colorScheme.surfaceContainerHigh
    }
    // Card 本身无 colors 参数（miuix 0.9.1），用外层背景 + clip 模拟 CompactPresetRow 的选中态
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(containerBg)
            .clickable(onClick = onClick),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            showIndication = false,
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
                colors = CheckboxDefaults.checkboxColors(
                    checkedBackgroundColor = MiuixTheme.colorScheme.primaryVariant,
                    checkedForegroundColor = MiuixTheme.colorScheme.onPrimaryVariant,
                    uncheckedBackgroundColor = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.35f),
                    uncheckedForegroundColor = Color.Transparent,
                ),
            )
            icon()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = entry.label,
                    modifier = Modifier.basicMarquee(),
                    style = MiuixTheme.textStyles.body1.copy(fontWeight = FontWeight(550)),
                    color = if (selected) MiuixTheme.colorScheme.primaryVariant else MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                )
                Text(
                    text = entry.packageName,
                    modifier = Modifier.basicMarquee(),
                    style = MiuixTheme.textStyles.footnote1.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight(550),
                    ),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
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
                    .background(MiuixTheme.colorScheme.secondaryContainer),
            )
        } else {
            Image(
                bitmap = current,
                contentDescription = null,
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
