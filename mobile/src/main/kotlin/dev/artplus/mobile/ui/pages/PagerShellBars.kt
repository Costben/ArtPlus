package dev.artplus.mobile.ui.pages

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.AppBarBlurOverlay
import io.github.suqi8.coui.kmp.basic.AppBarBlurOverlayDefaults
import io.github.suqi8.coui.kmp.basic.BottomFloatingAccessoryNavigationBar
import io.github.suqi8.coui.kmp.basic.BottomFloatingNavigationBarItem
import io.github.suqi8.coui.kmp.basic.ScrollBehavior
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TopAppBarDefaults
import io.github.suqi8.coui.kmp.basic.TopAppBarState
import io.github.suqi8.coui.kmp.basic.rememberTopAppBarState
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.GridView
import io.github.suqi8.coui.kmp.icon.extended.Layers
import io.github.suqi8.coui.kmp.icon.extended.Tune
import io.github.suqi8.coui.kmp.theme.COUITheme
import kotlin.math.roundToInt

/**
 * 单页的顶栏内容。每个 pager 页只持有一份，字段为 State：页面重组时在 SideEffect 里回写，
 * 只有读它的顶栏会重组，pager 本身不受影响。
 */
@Stable
internal class PagerBarSpec(
    title: String,
    navigationIcon: @Composable () -> Unit,
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

/** 顶栏单行高度（不含状态栏）。内容起点 = 状态栏 + 此值，顶栏不折叠所以是常量。 */
internal val ShellBarHeight = 56.dp

/** 返回键等导航图标与标题之间的间距；没有导航图标时不占位。 */
private val NavigationIconGap = 8.dp

/**
 * 固定标题顶栏（ColorOS 17 相册式）：标题与按钮同在第一行，不折叠、不缩放、不下沉到第二行。
 * 结构照 demo 的 `Coui17CompactToolbar`：AppBarBlurOverlay 材质从屏幕顶部起，状态栏高度内留空。
 * 材质透明度跟页面内容滚出的距离走（[ScrollBehavior.state] 的 contentOffset），在绘制阶段读取，
 * 滚动不会重组 pager。托管模式和独立模式（设置页 overlay）共用。
 */
@Composable
internal fun PagerShellTopAppBar(
    title: String,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    scrollBehavior: ScrollBehavior,
    modifier: Modifier = Modifier,
) {
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val density = LocalDensity.current
    AppBarBlurOverlay(
        scrollFraction = {
            AppBarBlurOverlayDefaults.scrollAlpha(-scrollBehavior.state.contentOffset, density)
        },
        modifier = modifier
            .fillMaxWidth()
            .height(statusBar + ShellBarHeight),
        extraHeight = AppBarBlurOverlayDefaults.ExtraHeight,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(statusBar))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ShellBarHeight)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavigationIconSlot(navigationIcon)
                Text(
                    text = title,
                    style = COUITheme.textStyles.title1,
                    fontWeight = FontWeight.Bold,
                    color = COUITheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(TopAppBarDefaults.ActionMenuItemSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    actions()
                }
            }
        }
    }
}

/** 导航图标槽：有内容才在其后补 [NavigationIconGap]，空 lambda 不占位（标题贴起始边）。 */
@Composable
private fun NavigationIconSlot(navigationIcon: @Composable () -> Unit) {
    Layout(content = { navigationIcon() }) { measurables, constraints ->
        val placeables = measurables.map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }
        val width = placeables.maxOfOrNull { it.width } ?: 0
        val height = placeables.maxOfOrNull { it.height } ?: 0
        val gap = if (width > 0) NavigationIconGap.roundToPx() else 0
        layout(width + gap, height) {
            placeables.forEach { it.placeRelative(0, (height - it.height) / 2) }
        }
    }
}

/**
 * 钉住的滚动行为：顶栏不折叠，只记录页面内容滚出的距离给材质透明度用。
 * 与 COUIScrollBehavior 的区别是从不改 heightOffset，也不拦截 pre-scroll，无 snap/fling。
 * contentOffset 随内容上滚为负值，约定与 COUI 一致。
 */
@Composable
internal fun rememberPinnedShellScrollBehavior(
    state: TopAppBarState = rememberTopAppBarState(),
): ScrollBehavior = remember(state) { PinnedShellScrollBehavior(state) }

private class PinnedShellScrollBehavior(override val state: TopAppBarState) : ScrollBehavior {
    override val isPinned: Boolean = true
    override val snapAnimationSpec: AnimationSpec<Float>? = null
    override val flingAnimationSpec: DecayAnimationSpec<Float>? = null
    override val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            if (consumed.y != 0f) {
                state.contentOffset = (state.contentOffset + consumed.y).coerceAtMost(0f)
            }
            return Offset.Zero
        }
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
        navigationIcon = spec?.navigationIcon ?: {},
        actions = spec?.actions ?: {},
        scrollBehavior = host.scrollBehavior,
        modifier = modifier,
    )
}

/** demo `Coui17Dimens.galleryDensity`：以 360dp 短边换算出的密度。 */
private fun galleryDensity(shortSidePixels: Int): Float =
    (shortSidePixels / 360f * 160f).roundToInt().coerceAtLeast(160) / 160f

/**
 * 与 demo 的 `Coui17AccessoryBar` 同款：悬浮胶囊底栏，限定在 gallery 密度下渲染。
 * 玻璃采样源取自 [io.github.suqi8.coui.kmp.blur.LocalBackdrop]，由宿主提供。
 *
 * 首尾配件都传空实现并常隐：配件槽的占位和胶囊偏移按「已配置」算，只配一侧会让胶囊偏离居中；
 * 两侧对称配置（demo `配色` 页的状态）胶囊才居中，同时走 MetaBallSurface 那条玻璃路径。
 */
@Composable
internal fun PagerShellBottomBar(
    pagerState: PagerState,
    onSelectPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val systemDensity = LocalDensity.current
    val windowSize = LocalWindowInfo.current.containerSize
    val galleryDensity = remember(systemDensity, windowSize) {
        // 360dp 短边只描述手机；大屏会把密度越推越大，所以封顶在系统密度，字体缩放保持系统值。
        val density = minOf(
            galleryDensity(minOf(windowSize.width, windowSize.height)),
            systemDensity.density,
        )
        Density(density = density, fontScale = systemDensity.fontScale)
    }
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
