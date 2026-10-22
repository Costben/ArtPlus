package dev.artplus.mobile.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.github.suqi8.coui.kmp.blur.LocalBackdrop
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.blur.rememberLayerBackdrop
import dev.artplus.mobile.MainActivity
import dev.artplus.mobile.data.AppEntry
import dev.artplus.mobile.ui.pages.tabs.HomeTabPage
import dev.artplus.mobile.ui.pages.tabs.ParamsTabPage
import dev.artplus.mobile.ui.pages.tabs.PresetTabPage

@Composable
internal fun MainActivity.HomePage(pageBackground: Color, selectedApp: AppEntry?, launcherCount: Int, totalCount: Int, generatedCount: Int) {
    // O1 修复：订阅 presetUi（裸读 .value 不触发重组，存/改/删后列表 stale，需重进才刷）。
    // 只加订阅、读取经此订阅走；数据逻辑/持久化顺序/符号名一律不动。
    val presetUiState by mainViewModel.presetUi.collectAsState()
    // 热修复：主页生成链渲染位订阅 picker/shell/previewSession（StateFlow .value 裸读不触发重组，
    // 生成按钮 isBusy/权限门/预览条 stale，选中 magisk 点生成也无产物/进度）。
    // 回调内事件时读 .value 仍合法，此处只修组合期渲染读。
    val pickerState by mainViewModel.picker.collectAsState()
    val shellState by mainViewModel.shell.collectAsState()
    val previewSessionState by mainViewModel.previewSession.collectAsState()
    // 热修复2：四宫格预览链订阅 batchPreviewConfig（wallpaperKey 裸读不触发重组，壁纸切换后预览 stale）。
    val batchConfigState by mainViewModel.batchPreviewConfig.collectAsState()
    // S3.5：调参快照在页面顶层订阅一次，取代 11 处内联 `collectAsState()`（每处都是一次独立订阅 + 一次重组）。
    val paramsState by mainViewModel.params.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val scope = rememberCoroutineScope()
    // 与 COUI demo（Coui17Root）一致：整个 pager 只录一次 barBackdrop（先铺页面底色再画内容），
    // 顶栏/底栏作为录制 Box 的兄弟节点叠在上面，不参与采样。LayerBackdrop 只有一块 graphicsLayer，
    // 挂在每个 pager 页上会被屏外邻页覆盖，底栏就会糊错页面。
    val pageColor = pageBackground
    val barBackdrop = rememberLayerBackdrop {
        drawRect(pageColor)
        drawContent()
    }
    // 顶栏按页各一根、随页面左右移动，滚动行为由各页自己持有（见 PagerShellPage）。
    val host = remember { PagerShellHost() }
    var beyondViewportCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        delay(200)
        beyondViewportCount = 1
        delay(350)
        beyondViewportCount = 3
    }
    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(pageColor)
                .layerBackdrop(barBackdrop),
        ) {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = beyondViewportCount,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                CompositionLocalProvider(
                    LocalPagerShellHost provides host,
                    LocalPagerShellPageIndex provides page,
                ) {
                    when (page) {
                        0 -> HomeTabPage(pageBackground, selectedApp, launcherCount, totalCount, generatedCount)
                        1 -> ParamsTabPage(pageBackground, selectedApp, launcherCount, totalCount, generatedCount)
                        2 -> PresetTabPage(pageBackground, selectedApp, launcherCount, totalCount, generatedCount)
                    }
                }
            }
        }

        // 顶栏/底栏的玻璃材质都采样 barBackdrop（页面内容），自身不在录制范围内。
        CompositionLocalProvider(LocalBackdrop provides barBackdrop) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val pageWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
                PagerShellSlidingTopBars(
                    host = host,
                    pagerState = pagerState,
                    pageWidthPx = pageWidthPx,
                )
                PagerShellBottomBar(
                    pagerState = pagerState,
                    onSelectPage = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

/** 主页面顶栏的紧凑 1×4 预览；设置页和应用选择页不会组合此组件。 */
