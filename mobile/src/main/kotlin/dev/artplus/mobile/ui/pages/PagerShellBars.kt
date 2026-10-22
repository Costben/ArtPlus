package dev.artplus.mobile.ui.pages

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import io.github.suqi8.coui.kmp.basic.BottomFloatingAccessoryNavigationBar
import io.github.suqi8.coui.kmp.basic.BottomFloatingNavigationBarItem
import io.github.suqi8.coui.kmp.basic.PinnedTopAppBar
import io.github.suqi8.coui.kmp.basic.PinnedTopAppBarDefaults
import io.github.suqi8.coui.kmp.basic.ScrollBehavior
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.GridView
import io.github.suqi8.coui.kmp.icon.extended.Layers
import io.github.suqi8.coui.kmp.icon.extended.Tune
import io.github.suqi8.coui.kmp.utils.rememberGalleryDensity

/**
 * 单页的顶栏内容。每个 pager 页只持有一份，字段为 State：页面重组时在 SideEffect 里回写，
 * 只有读它的顶栏会重组，pager 本身不受影响。
 */
@Stable
internal class PagerBarSpec(
    title: String,
    navigationIcon: (@Composable () -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
) {
    var title by mutableStateOf(title)
    var navigationIcon by mutableStateOf(navigationIcon)
    var actions by mutableStateOf(actions)
}

/**
 * pager 宿主：与 COUI demo（Coui17Root）一致，整个 pager 只有一根顶栏和一根底栏，
 * 共用同一个 [scrollBehavior]（钉住行为，只提供滚动距离）；各页只通过 [specs] 登记自己的标题和按钮。
 */
@Stable
internal class PagerShellHost(val scrollBehavior: ScrollBehavior) {
    val specs = mutableStateMapOf<Int, PagerBarSpec>()
}

/** 非 null 表示当前页被 [PagerShellHost] 托管：页面不再自己画顶栏。 */
internal val LocalPagerShellHost = staticCompositionLocalOf<PagerShellHost?> { null }

/** 当前 pager 页的下标，页面登记 [PagerBarSpec] 时用。 */
internal val LocalPagerShellPageIndex = compositionLocalOf { -1 }

/**
 * 固定标题顶栏（ColorOS 17 相册式）：标题与按钮同在第一行，不折叠；材质透明度跟页面内容滚出的距离走。
 * 相册的 toolbar 用相册自己的密度绘制（这台 620dpi 的机器上是 565dpi，与底栏一致），
 * 所以整根顶栏放在 gallery 密度下。托管模式和独立模式（设置页 overlay）共用。
 */
@Composable
internal fun PagerShellTopAppBar(
    title: String,
    navigationIcon: (@Composable () -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
    scrollBehavior: ScrollBehavior,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalDensity provides rememberGalleryDensity()) {
        PinnedTopAppBar(
            title = title,
            modifier = modifier,
            scrollBehavior = scrollBehavior,
            titleStyle = PinnedTopAppBarDefaults.titleStyle().copy(fontWeight = FontWeight.Bold),
            navigationIcon = navigationIcon,
            actions = actions,
        )
    }
}

/**
 * 页面内容起点（平台密度的 Dp）= 状态栏高度 + 顶栏高度。
 * 顶栏在 gallery 密度下排版，它的 52dp 要按该密度换成像素；状态栏 inset 本身就是像素。
 * 页面内容按平台密度排版，所以最后再换回平台 Dp。
 */
@Composable
internal fun rememberShellContentTop(): Dp {
    val platformDensity = LocalDensity.current
    val galleryDensity = rememberGalleryDensity()
    val statusBarPx = WindowInsets.statusBars.getTop(platformDensity)
    return remember(platformDensity, galleryDensity, statusBarPx) {
        val barPx = with(galleryDensity) { PinnedTopAppBarDefaults.Height.toPx() }
        with(platformDensity) { (statusBarPx + barPx).toDp() }
    }
}

/**
 * 宿主顶栏：渲染 `pagerState.currentPage` 页登记的 spec。spec 与 currentPage 只在这里读，
 * 标题/按钮变化只重组这一小块，不会带动 pager。
 */
@Composable
internal fun PagerShellHostTopBar(
    host: PagerShellHost,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val spec = host.specs[pagerState.currentPage]
    PagerShellTopAppBar(
        title = spec?.title.orEmpty(),
        navigationIcon = spec?.navigationIcon,
        actions = spec?.actions ?: {},
        scrollBehavior = host.scrollBehavior,
        modifier = modifier,
    )
}

/**
 * 与 demo 的 `Coui17AccessoryBar` 同款：悬浮胶囊底栏，限定在 gallery 密度下渲染。
 * 玻璃采样源取自 [io.github.suqi8.coui.kmp.blur.LocalBackdrop]，由宿主提供。
 *
 * 首尾配件都传空实现并常隐，即系统相册「创作」页的状态：配件槽的占位和胶囊偏移按「已配置」算，
 * 只配一侧会让胶囊偏离居中。PLK110 实测该状态与配件展开时是同一种半透明材质，胶囊亮度随背后内容变化。
 */
@Composable
internal fun PagerShellBottomBar(
    pagerState: PagerState,
    onSelectPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val galleryDensity = rememberGalleryDensity()
    CompositionLocalProvider(LocalDensity provides galleryDensity) {
        BottomFloatingAccessoryNavigationBar(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            startAccessory = { },
            endAccessory = { },
            startAccessoryVisible = false,
            endAccessoryVisible = false,
        ) {
            listOf(
                Triple(COUIIcons.GridView, "主页", 0),
                Triple(COUIIcons.Tune, "生成参数", 1),
                Triple(COUIIcons.Layers, "预设", 2),
            ).forEach { (icon, label, index) ->
                BottomFloatingNavigationBarItem(
                    selected = pagerState.targetPage == index,
                    onClick = { onSelectPage(index) },
                    icon = icon,
                    label = label,
                )
            }
        }
    }
}
