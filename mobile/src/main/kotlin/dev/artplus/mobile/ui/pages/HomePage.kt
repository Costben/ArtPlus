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
import androidx.compose.runtime.mutableStateOf
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
import dev.artplus.mobile.pipeline.findRmbgComponent
import dev.artplus.mobile.state.persistUiState
import dev.artplus.mobile.ui.cards.AdvancedSettingsTab
import dev.artplus.mobile.ui.components.SaveFloppyIcon
import dev.artplus.mobile.ui.pages.home.GenerateActionMenu
import dev.artplus.mobile.ui.pages.home.ParamsModeMenu
import dev.artplus.mobile.ui.pages.home.WriteActionMenu
import dev.artplus.mobile.ui.pages.home.exportCurrentPackage
import dev.artplus.mobile.ui.pages.home.generateSelectedApp
import dev.artplus.mobile.ui.pages.home.generateSelectedAppWithRmbg
import dev.artplus.mobile.ui.pages.home.writeSelectedWithRoot
import dev.artplus.mobile.ui.pages.tabs.HomeTabPage
import dev.artplus.mobile.ui.pages.tabs.ParamsTabPage
import dev.artplus.mobile.ui.pages.tabs.PresetTabPage
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Create
import io.github.suqi8.coui.kmp.icon.extended.Download
import io.github.suqi8.coui.kmp.icon.extended.Edit
import io.github.suqi8.coui.kmp.icon.extended.Notes
import io.github.suqi8.coui.kmp.icon.extended.Show

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
    val gptSettingsState by mainViewModel.gptRmbgSettings.collectAsState()
    // 底栏两个融合球的菜单：状态放页面级，菜单本体声明在球内部（锚点取球的实测矩形），
    // 由 ArtPlusScreen 根部 Scaffold 的 COUIPopupHost 统一渲染，切换 tab 也不会被裁掉。
    var showGenerateMenu by remember { mutableStateOf(false) }
    var showWriteMenu by remember { mutableStateOf(false) }
    var showParamsModeMenu by remember { mutableStateOf(false) }
    val hasRmbgComponent = remember(gptSettingsState.rmbgComponentStatus) { findRmbgComponent(filesDir) != null }
    val hasGptCredentials = gptSettingsState.gptApiKey.isNotBlank() && gptSettingsState.gptBaseUrl.isNotBlank()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    // 融合球的内容随页切换，离开某页时把那一页已经点开的菜单一起收起来。
    LaunchedEffect(pagerState.targetPage) {
        if (pagerState.targetPage != 0) {
            showGenerateMenu = false
            showWriteMenu = false
        }
        if (pagerState.targetPage != 1) {
            showParamsModeMenu = false
        }
    }
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
                        1 -> ParamsTabPage(
                            pageBackground = pageBackground,
                            selectedApp = selectedApp,
                            launcherCount = launcherCount,
                            totalCount = totalCount,
                            generatedCount = generatedCount,
                        )
                        2 -> PresetTabPage(pageBackground, selectedApp, launcherCount, totalCount, generatedCount)
                    }
                }
            }
        }

        // 顶栏/底栏的玻璃材质都采样 barBackdrop（页面内容），自身不在录制范围内。
        // 弹层不在这里渲染：根部 Scaffold 的 COUIPopupHost 压在整屏之上，采样整屏快照。
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
                    // 首尾两颗融合球是「同一个球换内容」：主页是生成/写入，生成参数页是生成设置/保存成预设。
                    startAccessory = { onParamsPage ->
                        if (onParamsPage) {
                            PagerShellActionBall(
                                // 没选过模式时是笔，选过之后换成当前模式的图标（可视化＝眼睛，JSON＝稿纸）。
                                icon = when {
                                    !shellState.advancedSettingsTabChosen -> COUIIcons.Edit
                                    shellState.advancedSettingsTab == AdvancedSettingsTab.Json -> COUIIcons.Notes
                                    else -> COUIIcons.Show
                                },
                                contentDescription = "生成设置",
                                emphasized = false,
                                onClick = { showParamsModeMenu = true },
                                menu = {
                                    ParamsModeMenu(
                                        show = showParamsModeMenu,
                                        current = shellState.advancedSettingsTab,
                                        onSelect = { tab ->
                                            showParamsModeMenu = false
                                            mainViewModel.updateShell { v ->
                                                v.copy(advancedSettingsTab = tab, advancedSettingsTabChosen = true)
                                            }
                                            persistUiState()
                                        },
                                        onDismissRequest = { showParamsModeMenu = false },
                                    )
                                },
                            )
                        } else {
                            PagerShellActionBall(
                                icon = COUIIcons.Create,
                                contentDescription = "生成",
                                emphasized = false,
                                onClick = { showGenerateMenu = true },
                                menu = {
                                    GenerateActionMenu(
                                        show = showGenerateMenu,
                                        canGenerate = selectedApp != null && !shellState.isBusy,
                                        canExport = !shellState.isBusy,
                                        canRmbg = selectedApp != null && !shellState.isBusy && hasRmbgComponent,
                                        canGpt = selectedApp != null && !shellState.isBusy && hasGptCredentials,
                                        onLocalGenerate = {
                                            showGenerateMenu = false
                                            generateSelectedApp(installWithRoot = false, useGpt = false)
                                        },
                                        onLocalExport = {
                                            showGenerateMenu = false
                                            exportCurrentPackage()
                                        },
                                        onRmbgGenerate = {
                                            showGenerateMenu = false
                                            generateSelectedAppWithRmbg()
                                        },
                                        onGptGenerate = {
                                            showGenerateMenu = false
                                            generateSelectedApp(installWithRoot = false, useGpt = true)
                                        },
                                        onDismissRequest = { showGenerateMenu = false },
                                        onDismissFinished = {},
                                    )
                                },
                            )
                        }
                    },
                    endAccessory = { onParamsPage ->
                        if (onParamsPage) {
                            PagerShellActionBall(
                                icon = SaveFloppyIcon,
                                contentDescription = "保存成预设",
                                // 参数页这颗球不铺强调色：只有主页的「写入」才是主操作。
                                emphasized = false,
                                onClick = {
                                    mainViewModel.updatePresetUi { it -> it.copy(presetSaveName = ("")) }
                                    mainViewModel.updatePresetUi { it -> it.copy(presetSaveDialogVisible = (true)) }
                                },
                                menu = {},
                            )
                        } else {
                            PagerShellActionBall(
                                icon = COUIIcons.Download,
                                contentDescription = "写入",
                                emphasized = true,
                                onClick = { showWriteMenu = true },
                                menu = {
                                    WriteActionMenu(
                                        show = showWriteMenu,
                                        enabled = !shellState.isBusy,
                                        onWrite = { mode ->
                                            showWriteMenu = false
                                            writeSelectedWithRoot(mode)
                                        },
                                        onDismissRequest = { showWriteMenu = false },
                                        onDismissFinished = {},
                                    )
                                },
                            )
                        }
                    },
                )
            }
        }
    }
}

/** 主页面顶栏的紧凑 1×4 预览；设置页和应用选择页不会组合此组件。 */
