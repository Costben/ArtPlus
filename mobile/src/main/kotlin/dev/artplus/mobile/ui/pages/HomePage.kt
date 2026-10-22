package dev.artplus.mobile.ui.pages

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.blur.rememberLayerBackdrop
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.github.suqi8.coui.kmp.basic.BottomFloatingNavigationBarItem
import io.github.suqi8.coui.kmp.basic.BottomFloatingNavigationBar
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.GridView
import io.github.suqi8.coui.kmp.icon.extended.Layers
import io.github.suqi8.coui.kmp.icon.extended.Settings
import io.github.suqi8.coui.kmp.icon.extended.Tune
import dev.artplus.mobile.MainActivity
import dev.artplus.mobile.data.AppEntry
import dev.artplus.mobile.ui.pages.tabs.HomeTabPage
import dev.artplus.mobile.ui.pages.tabs.ParamsTabPage
import dev.artplus.mobile.ui.pages.tabs.PresetTabPage
import dev.artplus.mobile.ui.pages.tabs.SettingsTabPage

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
    // 底栏订阅：glassBar .value 裸读不触发重组，设置页开关翻转后底栏显隐/模糊 stale。
    // 回调内事件时读 .value 仍合法，此处只修组合期渲染读。
    val glassBar by mainViewModel.glassBar.collectAsState()
    // S3.5：调参快照在页面顶层订阅一次，取代 11 处内联 `collectAsState()`（每处都是一次独立订阅 + 一次重组）。
    val paramsState by mainViewModel.params.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
    val scope = rememberCoroutineScope()
    val isBlurEnabled = glassBar.liquidGlassBottomBarEnabled && glassBar.liquidGlassBottomBarBlurEnabled
    var beyondViewportCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        delay(200)
        beyondViewportCount = 1
        delay(350)
        beyondViewportCount = 3
    }
    val backdrop = rememberLayerBackdrop {
        drawRect(pageBackground)
        drawContent()
    }
    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = beyondViewportCount,
            modifier = Modifier
                .fillMaxSize()
                .background(pageBackground),
        ) { page ->
            // S6.1：只有当前页 ±1 挂 backdrop，远页不参与采样（beyondViewportCount 仍为 3，
            // 保证滚动时页面不重建，但 4 层 backdrop 采样降为最多 3 层）。
            val near = kotlin.math.abs(page - pagerState.currentPage) <= 1
            Box(
                modifier = if (isBlurEnabled && near) Modifier.layerBackdrop(backdrop) else Modifier,
            ) {
            when (page) {
                0 -> HomeTabPage(pageBackground, selectedApp, launcherCount, totalCount, generatedCount)
                1 -> ParamsTabPage(pageBackground, selectedApp, launcherCount, totalCount, generatedCount)
                2 -> PresetTabPage(pageBackground, selectedApp, launcherCount, totalCount, generatedCount)
                3 -> SettingsTabPage(pageBackground, selectedApp, launcherCount, totalCount, generatedCount)
            }
            }
        }

        // ColorOS 17 floating capsule bar: COUI BottomFloatingNavigationBar over the page backdrop.
        // The bar forwards its modifier into the capsule row, so alignment and the gesture-bar inset
        // ride on this wrapper instead of the bar itself.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            BottomFloatingNavigationBar(
                backdrop = if (isBlurEnabled) backdrop else null,
            ) {
                listOf(
                    Triple(COUIIcons.GridView, "主页", 0),
                    Triple(COUIIcons.Tune, "生成参数", 1),
                    Triple(COUIIcons.Layers, "预设", 2),
                    Triple(COUIIcons.Settings, "设置", 3),
                ).forEach { (icon, label, index) ->
                    BottomFloatingNavigationBarItem(
                        selected = pagerState.targetPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        icon = icon,
                        label = label,
                    )
                }
            }
        }
    }
}

/** 主页面顶栏的紧凑 1×4 预览；设置页和应用选择页不会组合此组件。 */
