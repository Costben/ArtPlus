package dev.artplus.mobile.ui.pages

import android.app.WallpaperManager
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import io.github.suqi8.coui.kmp.blur.LocalBackdrop
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.blur.rememberLayerBackdrop
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.Scaffold
import io.github.suqi8.coui.kmp.basic.TabRowWithContour
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TopAppBar
import io.github.suqi8.coui.kmp.basic.TopAppBarDefaults
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.ChevronBackward
import io.github.suqi8.coui.kmp.icon.extended.Refresh
import io.github.suqi8.coui.kmp.theme.COUITheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dev.artplus.mobile.CHOICE_ROW_HORIZONTAL_BLEED_DP
import dev.artplus.mobile.MainActivity
import dev.artplus.mobile.PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE
import dev.artplus.mobile.PREVIEW_LABEL_HEIGHT_DP
import dev.artplus.mobile.pipeline.PreviewMode
import dev.artplus.mobile.state.persistUiState
import dev.artplus.mobile.ui.cards.AppPage
import dev.artplus.mobile.ui.cards.NumberParameterControl
import dev.artplus.mobile.ui.cards.PreviewDesktopBackground
import dev.artplus.mobile.ui.components.SectionCard
import dev.artplus.mobile.ui.components.TitleBarIconButton
import dev.artplus.mobile.ui.components.rememberLinkedTopAppBarScrollBehavior
import dev.artplus.mobile.ui.pages.home.GeneratedIconPreview
import dev.artplus.mobile.ui.pages.home.MissingIconPreview
import dev.artplus.mobile.ui.pages.home.PreviewBackgroundOption
import dev.artplus.mobile.ui.pages.home.PreviewDesktopBackgroundSurface
import dev.artplus.mobile.ui.pages.params.paramsUpdateBatchPreviewColumns
import dev.artplus.mobile.ui.pages.params.paramsUpdateBatchPreviewCornerRadiusDp
import dev.artplus.mobile.ui.pages.params.paramsUpdateBatchPreviewDesktopBackground
import dev.artplus.mobile.ui.pages.params.paramsUpdateBatchPreviewIconSizeDp
import dev.artplus.mobile.ui.pages.picker.pickerLoadBundledPreviewWallpaperBitmap
import dev.artplus.mobile.ui.pages.picker.pickerLoadCustomWallpaperBitmap
import dev.artplus.mobile.ui.pages.picker.pickerLoadPreviewWallpaperBitmap
import dev.artplus.mobile.ui.pages.picker.pickerSystemMaterialColor
import dev.artplus.mobile.ui.pages.picker.pickerSystemWallpaperDrawable
import dev.artplus.mobile.R

@Composable
internal fun MainActivity.BatchPreviewPage(pageBackground: Color) {
    val scrollBehavior = rememberLinkedTopAppBarScrollBehavior()
    // 热修复2：批量预览渲染位订阅 presetUi/shell/batchPreviewConfig（StateFlow .value 裸读不触发重组，
    // 批量结果回填/桌面背景/列数/尺寸/圆角切换后 stale，亮色↔暗色点了不变）。
    // 回调内（onClick/onSave 等事件时）读 .value 仍合法，此处只修组合期渲染读。
    val batchPresetUi by mainViewModel.presetUi.collectAsState()
    val batchShell by mainViewModel.shell.collectAsState()
    val batchConfig by mainViewModel.batchPreviewConfig.collectAsState()
    val result = batchPresetUi.batchPreviewResult
    val preset = result?.preset ?: batchPresetUi.activeBatchPreviewPreset
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })

    val modes = remember {
        listOf(
            PreviewMode.NormalLight,
            PreviewMode.NormalDark,
            PreviewMode.MonochromeLight,
            PreviewMode.MonochromeDark,
        )
    }
    val tabTitles = remember {
        listOf("正常亮色", "正常暗色", "单色亮色", "单色暗色")
    }

    // Backdrop for the ColorOS app bar material: the page content only, never the bar itself.
    val appBarBackdrop = rememberLayerBackdrop {
        drawRect(pageBackground)
        drawContent()
    }
    val contentTopInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
        TopAppBarDefaults.ExpandedHeight
    Scaffold(
        containerColor = pageBackground,
        topBar = {
            CompositionLocalProvider(LocalBackdrop provides appBarBackdrop) {
                TopAppBar(
                    title = preset?.name ?: "批量预览",
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        TitleBarIconButton(
                            icon = COUIIcons.ChevronBackward,
                            contentDescription = "返回",
                            enabled = !batchShell.isBusy,
                            onClick = { mainViewModel.updateShell { it -> it.copy(currentPage = (AppPage.Home)) } },
                        )
                    },
                    actions = {
                        TitleBarIconButton(
                            icon = COUIIcons.Refresh,
                            contentDescription = "重新生成",
                            enabled = !batchShell.isBusy && !batchPresetUi.isGeneratingBatchPreview && preset != null,
                            onClick = { mainViewModel.updatePresetUi { it -> it.copy(showBatchPreviewRefreshConfirm = (true)) } },
                        )
                    },
                    gradientGlass = true,
                    showDivider = false,
                )
            }
        },
    ) { innerPadding ->
        // 预览框高度锁死：内容行数 + 1 行，框内无滚动可抢，所有纵向手势归页面
        val previewContentRows = result?.items?.size?.let { count ->
            if (count <= 0) 0 else (count + batchConfig.batchPreviewColumns - 1) / batchConfig.batchPreviewColumns
        } ?: 0
        val previewDisplayRows = previewContentRows + 1
        val previewRowHeight = batchConfig.batchPreviewIconSizeDp.dp + 6.dp + PREVIEW_LABEL_HEIGHT_DP.dp
        val previewFrameHeight = 18.dp + previewRowHeight * previewDisplayRows +
            18.dp * (previewDisplayRows - 1) + 18.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(appBarBackdrop)
                .background(pageBackground)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(top = contentTopInset + 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. 桌面控制卡：背景一行 + 三个主页化参数行（无图标、默认展开）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                SectionCard(rowsFullBleed = true) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = CHOICE_ROW_HORIZONTAL_BLEED_DP.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PreviewDesktopBackground.entries.forEach { bgOption ->
val __act2 = LocalContext.current
    PreviewBackgroundOption(
                option = (bgOption),
                selected = (batchConfig.batchPreviewDesktopBackground == bgOption),
                isBusy = batchShell.isBusy,
                wallpaperInitial = cachedCustomWallpaper ?: cachedSystemWallpaper ?: cachedBundledWallpaper,
                wallpaperKey = batchConfig.customWallpaperPath,
                loadWallpaper = {
                    withContext(Dispatchers.IO) {
        pickerLoadCustomWallpaperBitmap(
                    path = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                    cachedPath = cachedCustomWallpaperPath,
                    getCached = { cachedCustomWallpaper },
                    setCached = { path, bitmap ->
                        cachedCustomWallpaper = bitmap
                        cachedCustomWallpaperPath = path
                    },
                    shortEdge = PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE,
                )
     ?: run {
        pickerLoadPreviewWallpaperBitmap(
                    getCached = { cachedSystemWallpaper },
                    setCached = { cachedSystemWallpaper = it },
                    loadDrawable = { pickerSystemWallpaperDrawable(WallpaperManager.getInstance(__act2)) },
                    shortEdge = PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE,
                )
    } ?: run {
        pickerLoadBundledPreviewWallpaperBitmap(
                    getCached = { cachedBundledWallpaper },
                    setCached = { cachedBundledWallpaper = it },
                    resources = resources,
                    resId = R.drawable.preview_wallpaper,
                    shortEdge = PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE,
                )
    }
                    }
                },
                modifier = (Modifier.weight(1f)),
                onClick = ({ run {
        paramsUpdateBatchPreviewDesktopBackground(
                    option = (bgOption),
                    getValue = { mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground },
                    setValue = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(batchPreviewDesktopBackground = (it)) } },
                    onSave = { persistUiState() },
                )
    } }),
            )
                        }
                    }

                    NumberParameterControl(
                        busy = batchShell.isBusy,
                        title = "列数",
                        summary = "控制桌面每行图标数量，切换列数会自动适配图标大小",
                        value = batchConfig.batchPreviewColumns,
                        draftText = draftBatchPreviewColumnsText,
                        min = 2,
                        max = 5,
                        step = 1,
                        onDraftChange = { draftBatchPreviewColumnsText = it },
                        onSave = { run {
    paramsUpdateBatchPreviewColumns(
                value = (it),
                setColumns = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(batchPreviewColumns = (it)) } },
                setDraftColumnsText = { draftBatchPreviewColumnsText = it },
                setIconSize = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(batchPreviewIconSizeDp = (it)) } },
                setDraftIconSizeText = { draftBatchPreviewIconSizeDpText = it },
                onSave = { persistUiState() },
            )
} },
                        showIcon = false,
                        initiallyExpanded = false,
                    )

                    NumberParameterControl(
                        busy = batchShell.isBusy,
                        title = "图标大小",
                        summary = "控制预览图标的显示大小",
                        value = batchConfig.batchPreviewIconSizeDp,
                        draftText = draftBatchPreviewIconSizeDpText,
                        min = 40,
                        max = 84,
                        step = 2,
                        onDraftChange = { draftBatchPreviewIconSizeDpText = it },
                        onSave = { run {
    paramsUpdateBatchPreviewIconSizeDp(
                value = (it),
                setValue = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(batchPreviewIconSizeDp = (it)) } },
                setDraftText = { draftBatchPreviewIconSizeDpText = it },
                onSave = { persistUiState() },
            )
} },
                        showIcon = false,
                        initiallyExpanded = false,
                    )

                    NumberParameterControl(
                        busy = batchShell.isBusy,
                        title = "图标圆角",
                        summary = "控制预览图标的圆角大小",
                        value = batchConfig.batchPreviewCornerRadiusDp,
                        draftText = draftBatchPreviewCornerRadiusDpText,
                        min = 0,
                        max = 36,
                        step = 1,
                        onDraftChange = { draftBatchPreviewCornerRadiusDpText = it },
                        onSave = { run {
    paramsUpdateBatchPreviewCornerRadiusDp(
                value = (it),
                setValue = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(batchPreviewCornerRadiusDp = (it)) } },
                setDraftText = { draftBatchPreviewCornerRadiusDpText = it },
                onSave = { persistUiState() },
            )
} },
                        showIcon = false,
                        initiallyExpanded = false,
                    )
                }
            }
            }

            // 2. 4 风格切换栏（控制卡下方、预览框上方）
            TabRowWithContour(
                tabs = tabTitles,
                selectedTabIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                },
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            // 3. 桌面展示框：高度锁死为内容行数 + 1 行，框内不滚动
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Card(
                    cornerRadius = 20.dp,
                    insideMargin = PaddingValues(0.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(previewFrameHeight)
                            .clip(RoundedCornerShape(20.dp)),
                    ) {
val __act1 = LocalContext.current
    PreviewDesktopBackgroundSurface(
                option = (batchConfig.batchPreviewDesktopBackground),
                modifier = (Modifier.fillMaxSize()),
                wallpaperInitial = cachedCustomWallpaper ?: cachedSystemWallpaper ?: cachedBundledWallpaper,
                wallpaperKey = batchConfig.customWallpaperPath,
                loadWallpaper = {
                    withContext(Dispatchers.IO) {
                        run {
        pickerLoadCustomWallpaperBitmap(
                    path = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                    cachedPath = cachedCustomWallpaperPath,
                    getCached = { cachedCustomWallpaper },
                    setCached = { path, bitmap ->
                        cachedCustomWallpaper = bitmap
                        cachedCustomWallpaperPath = path
                    },
                    shortEdge = PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE,
                )
    } ?: run {
        pickerLoadPreviewWallpaperBitmap(
                    getCached = { cachedSystemWallpaper },
                    setCached = { cachedSystemWallpaper = it },
                    loadDrawable = { pickerSystemWallpaperDrawable(WallpaperManager.getInstance(__act1)) },
                    shortEdge = PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE,
                )
    } ?: run {
        pickerLoadBundledPreviewWallpaperBitmap(
                    getCached = { cachedBundledWallpaper },
                    setCached = { cachedBundledWallpaper = it },
                    resources = resources,
                    resId = R.drawable.preview_wallpaper,
                    shortEdge = PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE,
                )
    }
                    }
                },
            )

                        if (result == null || result.items.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "暂无预览数据",
                                    style = COUITheme.textStyles.body2,
                                    color = Color.White.copy(alpha = 0.8f),
                                )
                            }
                        } else {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize(),
                            ) { page ->
                                val currentMode = modes[page]
                                // 热修复2：宫格渲染位经已订阅 batchConfig 读取（裸读列数/尺寸/圆角/背景不触发重组）。
                                val isLightBg = batchConfig.batchPreviewDesktopBackground == PreviewDesktopBackground.LightGray
                                val labelColor = if (isLightBg) Color(0xFF222222) else Color.White
                                val shadowColor = if (isLightBg) Color.White.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.85f)

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(batchConfig.batchPreviewColumns),
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 18.dp),
                                    verticalArrangement = Arrangement.spacedBy(18.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    userScrollEnabled = false,
                                ) {
                                    items(result.items, key = { it.packageName }) { item ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            if (item.assets.missingMessage(currentMode) == null) {
    GeneratedIconPreview(
                assets = (item.assets),
                mode = (currentMode),
                modifier = (Modifier.size(batchConfig.batchPreviewIconSizeDp.dp)),
                cornerRadiusDp = (batchConfig.batchPreviewCornerRadiusDp),
                materialColorProvider = { __a0: String, __a1: Color ->
        pickerSystemMaterialColor(
                    resources = resources,
                    getColor = ::getColor,
                    resourceName = __a0,
                    fallback = __a1,
                )
     },
            )
                                            } else {
    MissingIconPreview(
                modifier = (Modifier.size(batchConfig.batchPreviewIconSizeDp.dp)),
                mode = (currentMode),
                compact = (true),
                cornerRadiusDp = (batchConfig.batchPreviewCornerRadiusDp),
                materialColorProvider = { __a0: String, __a1: Color ->
        pickerSystemMaterialColor(
                    resources = resources,
                    getColor = ::getColor,
                    resourceName = __a0,
                    fallback = __a1,
                )
     },
            )
                                            }
                                            Text(
                                                text = item.label,
                                                style = COUITheme.textStyles.footnote2.copy(
                                                    fontWeight = FontWeight.Medium,
                                                    shadow = Shadow(
                                                        color = shadowColor,
                                                        offset = Offset(0f, 1f),
                                                        blurRadius = 3f,
                                                    ),
                                                ),
                                                color = labelColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 2.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
