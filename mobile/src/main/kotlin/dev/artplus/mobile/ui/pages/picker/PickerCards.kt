package dev.artplus.mobile.ui.pages.picker

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import io.github.suqi8.coui.kmp.basic.BottomFloatingAccessoryBall
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.CardDefaults
import io.github.suqi8.coui.kmp.basic.Checkbox
import io.github.suqi8.coui.kmp.basic.DropdownEntry
import io.github.suqi8.coui.kmp.basic.DropdownItem
import io.github.suqi8.coui.kmp.basic.InputField
import io.github.suqi8.coui.kmp.basic.PopupPositionProvider
import io.github.suqi8.coui.kmp.basic.SearchBarDefaults
import io.github.suqi8.coui.kmp.basic.TagView
import io.github.suqi8.coui.kmp.basic.TagViewDefaults
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.More
import io.github.suqi8.coui.kmp.overlay.OverlayNavigationPopupMenu
import io.github.suqi8.coui.kmp.theme.COUITheme
import dev.artplus.mobile.data.AppEntry
import dev.artplus.mobile.ui.cards.GeneratedFilter
import dev.artplus.mobile.ui.cards.SegmentedControl
import dev.artplus.mobile.ui.components.AppListTag
import dev.artplus.mobile.ui.components.AppStatusTag
import dev.artplus.mobile.ui.components.AppTagCornerRadius
import dev.artplus.mobile.ui.components.CompactCardCornerRadius
import dev.artplus.mobile.ui.components.SectionHeader

/**
 * Slice 2.5：应用选择器组件与行（原 MainActivity 残留本体原样搬迁）。
 * 只做物理搬迁+显式参数化：Composable 只收 state + onEvent；
 * 名单外动作（applyCurrentPresetBatch）转为回调注入，不移动本体。
 * MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动。
 * 名单内联 helper 扫描结论：本簇无内联 helper（SectionCard/Tag/按钮系收敛进
 * ui/components，只做调用）。
 */

/**
 * 独立搜索行：直接用 COUI 的标准搜索胶囊（`SearchBar` 的 `InputField`），不套卡片。
 *
 * 这一行是就地筛选下面的列表，没有展开式结果面板，所以固定按收起态渲染、把展开回调吞掉；
 * 收起态的胶囊不发光（见 [SearchBarDefaults.causticGlowColor] 的说明），故 `glowColor = null`。
 */
@Composable
internal fun PickerSearchRow(
    queryText: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
) {
    InputField(
        query = queryText,
        onQueryChange = { text ->
            // 清除按钮把文本置空：沿用原 onClearQuery 路径
            if (text.isEmpty() && queryText.isNotEmpty()) onClearQuery() else onQueryChange(text)
        },
        onSearch = {},
        expanded = false,
        onExpandedChange = {},
        modifier = Modifier.fillMaxWidth(),
        label = "搜索应用或包名...",
        colors = SearchBarDefaults.searchBarColors(
            backgroundColor = COUITheme.colorScheme.secondaryVariant,
        ),
        glowColor = null,
    )
}

@Composable
internal fun AppPickerControlsCard(
    generatedFilter: GeneratedFilter,
    queryText: String,
    onFilterSelected: (GeneratedFilter) -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 全部 / 已生成 / 未生成：页面的第一个组件，独立一行，不套卡片背景
        val filters = GeneratedFilter.entries
        SegmentedControl(
            labels = filters.map { it.label },
            selectedIndex = filters.indexOf(generatedFilter),
            onSelected = { index ->
                onFilterSelected(filters[index])
            },
        )
        // 搜索自成一行，与筛选分开
        PickerSearchRow(
            queryText = queryText,
            onQueryChange = onQueryChange,
            onClearQuery = onClearQuery,
        )
    }
}

/** 应用列表的分组标题：多选时把「多选 N」挂在标题行右端，列表头部就够看了。 */
@Composable
internal fun PickerAppListHeader(multiCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionHeader(
            text = "应用列表",
            modifier = Modifier.weight(1f),
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
}

/**
 * 选择器右下角融合球：底栏融合球同款 MetaBall 玻璃材质、边光高光与手势形变物理。
 * 点开是「更多」菜单（概览 / 显示系统应用 / 套用当前预设 / 选择当前）。
 */
@Composable
internal fun PickerMoreBall(
    show: Boolean,
    showSystemApps: Boolean,
    onlyAmApps: Boolean,
    canApplyPreset: Boolean,
    canSelectCurrent: Boolean,
    generatedCount: Int,
    ungeneratedCount: Int,
    amMarkedCount: Int,
    onShow: () -> Unit,
    onDismissRequest: () -> Unit,
    onToggleSystemApps: () -> Unit,
    onToggleOnlyAmApps: () -> Unit,
    onApplyPreset: () -> Unit,
    onSelectCurrent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BottomFloatingAccessoryBall(
        onClick = onShow,
        modifier = modifier,
        icon = COUIIcons.More,
        contentDescription = "更多",
        menu = {
            OverlayNavigationPopupMenu(
                expanded = show,
                entries = listOf(
                    // 概览统计：只读预览行，文字置灰且无法选中与高光
                    DropdownEntry(
                        items = listOf(
                            DropdownItem(
                                text = "已生成 $generatedCount · 未生成 $ungeneratedCount · AM $amMarkedCount",
                                enabled = false,
                            ),
                        ),
                    ),
                    // 筛选设置：显示系统应用与只显示 AM 应用
                    DropdownEntry(
                        items = listOf(
                            DropdownItem(
                                text = "显示系统应用",
                                summary = if (showSystemApps) "已包含系统应用" else "仅显示用户应用",
                                selected = showSystemApps,
                                onClick = onToggleSystemApps,
                            ),
                            DropdownItem(
                                text = "只显示 AM 应用",
                                summary = if (onlyAmApps) "已开启（仅显示带 AM 标记的应用）" else "已关闭（显示全部应用）",
                                selected = onlyAmApps,
                                onClick = onToggleOnlyAmApps,
                            ),
                        ),
                    ),
                    // 批量动作
                    DropdownEntry(
                        items = listOf(
                            DropdownItem(
                                text = "套用当前预设",
                                enabled = canApplyPreset,
                                onClick = onApplyPreset,
                            ),
                            DropdownItem(
                                text = "选择当前",
                                enabled = canSelectCurrent,
                                onClick = onSelectCurrent,
                            ),
                        ),
                    ),
                ),
                onDismissRequest = onDismissRequest,
                onDismissFinished = {},
                alignment = PopupPositionProvider.Align.BottomEnd,
                renderInRootScaffold = false,
                collapseOnSelection = false,
            )
        },
    )
}

@Composable
internal fun AppRow(
    entry: AppEntry,
    selected: Boolean,
    multiSelected: Boolean,
    generated: Boolean,
    amMarked: Boolean,
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
    // AM 标记用实心主色，和「已生成」的中性淡染拉开层次
    val amTagBg = COUITheme.colorScheme.primary
    val amTagFg = COUITheme.colorScheme.onPrimary
    // 每个元素是一行标签：「已生成」与 AM 并排，其余单独成行
    val tagRows = remember(
        selected,
        multiSelected,
        generated,
        amMarked,
        entry.launchable,
        selectedTagBg,
        selectedTagFg,
        multiSelectedTagBg,
        multiSelectedTagFg,
        generatedTagBg,
        generatedTagFg,
        amTagBg,
        amTagFg,
        allTagBg,
        allTagFg,
    ) {
        buildList {
            if (selected) add(listOf(AppListTag("已选", selectedTagBg, selectedTagFg)))
            if (multiSelected) add(listOf(AppListTag("多选", multiSelectedTagBg, multiSelectedTagFg)))
            if (generated) {
                add(
                    listOfNotNull(
                        if (amMarked) AppListTag("AM", amTagBg, amTagFg) else null,
                        AppListTag("已生成", generatedTagBg, generatedTagFg),
                    ),
                )
            }
            if (!entry.launchable) add(listOf(AppListTag("全部", allTagBg, allTagFg)))
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
                    style = COUITheme.textStyles.body1.copy(fontWeight = FontWeight(550)),
                    color = if (selected) COUITheme.colorScheme.primaryVariant else COUITheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = entry.packageName,
                    style = COUITheme.textStyles.footnote2.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight(550),
                    ),
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (tagRows.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(start = 8.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    tagRows.forEach { row ->
                        if (row.size == 1) {
                            AppStatusTag(tag = row.first())
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                row.forEach { tag -> AppStatusTag(tag = tag) }
                            }
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
