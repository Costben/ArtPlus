package dev.artplus.mobile.ui.pages

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.BottomFloatingAccessoryNavigationBar
import io.github.suqi8.coui.kmp.basic.BottomFloatingNavigationBarItem
import io.github.suqi8.coui.kmp.basic.LocalEdgeToEdge
import io.github.suqi8.coui.kmp.basic.PinnedTopAppBar
import io.github.suqi8.coui.kmp.basic.PinnedTopAppBarDefaults
import io.github.suqi8.coui.kmp.basic.ScrollBehavior
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Folder
import io.github.suqi8.coui.kmp.icon.extended.FolderFill
import io.github.suqi8.coui.kmp.icon.extended.GridView
import io.github.suqi8.coui.kmp.icon.extended.Tune
import io.github.suqi8.coui.kmp.utils.rememberGalleryDensity
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 单页的顶栏内容。每个 pager 页只持有一份，字段为 State：页面重组时在 SideEffect 里回写，
 * 只有读它的顶栏会重组，pager 本身不受影响。
 *
 * [scrollBehavior] 也是每页一份：顶栏随页面左右移动，材质透明度必须跟本页自己的滚动距离走，
 * 否则切到另一页时会沿用上一页的滚动量。
 */
@Stable
internal class PagerBarSpec(
    title: String,
    navigationIcon: (@Composable () -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
    val scrollBehavior: ScrollBehavior,
) {
    var title by mutableStateOf(title)
    var navigationIcon by mutableStateOf(navigationIcon)
    var actions by mutableStateOf(actions)
}

/**
 * pager 宿主：底栏全局只有一根，顶栏按页各一根。各页通过 [specs] 登记自己的标题、按钮和滚动行为，
 * 宿主据此把每页的顶栏画在该页正上方，随 pager 一起左右移动。
 */
@Stable
internal class PagerShellHost {
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
 *
 * 标题：一级页沿用相册的 32dp 大标题；带返回键的二级页（设置）按 ColorOS 设置的紧凑标题，
 * 即 COUI 折叠态的 24dp Medium（coui_appbar_title_collapsed_text_size）。
 *
 * 两种模式的页面内容都铺满窗口、从顶栏底下滚过（顶部留白走 contentPadding），满足 COUI
 * 顶栏材质的前提，所以这里放行 [LocalEdgeToEdge]；不放行时 COUI 不画材质，顶栏整块透明。
 */
@Composable
internal fun PagerShellTopAppBar(
    title: String,
    navigationIcon: (@Composable () -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
    scrollBehavior: ScrollBehavior,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(
        LocalDensity provides rememberGalleryDensity(),
        LocalEdgeToEdge provides true,
    ) {
        val titleStyle = if (navigationIcon != null) {
            PinnedTopAppBarDefaults.titleStyle().copy(
                fontSize = with(LocalDensity.current) { SecondaryTitleSize.toSp() },
                fontWeight = FontWeight.Medium,
            )
        } else {
            PinnedTopAppBarDefaults.titleStyle().copy(fontWeight = FontWeight.Bold)
        }
        PinnedTopAppBar(
            title = title,
            modifier = modifier,
            scrollBehavior = scrollBehavior,
            titleStyle = titleStyle,
            navigationIcon = navigationIcon,
            actions = actions,
        )
    }
}

/** COUI coui_appbar_title_collapsed_text_size：dp 而非 sp，与系统设置一样不随字体缩放。 */
private val SecondaryTitleSize = 24.dp

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
 * 每页各一根顶栏，横向偏移跟随 pager：第 page 页的顶栏始终压在该页内容的正上方，
 * 拖动时和内容同速左右移动。
 *
 * 偏移在 layout 阶段读 pager 状态，滑动过程不触发重组；只画当前页及左右各一页，
 * 更远的页整根都在屏幕外。
 */
@Composable
internal fun BoxScope.PagerShellSlidingTopBars(
    host: PagerShellHost,
    pagerState: PagerState,
    pageWidthPx: Float,
) {
    val currentPage = pagerState.currentPage
    val pageCount = pagerState.pageCount
    for (page in (currentPage - 1)..(currentPage + 1)) {
        if (page < 0 || page >= pageCount) continue
        val spec = host.specs[page] ?: continue
        PagerShellTopAppBar(
            title = spec.title,
            navigationIcon = spec.navigationIcon,
            actions = spec.actions,
            scrollBehavior = spec.scrollBehavior,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset {
                    val delta = page - pagerState.currentPage - pagerState.currentPageOffsetFraction
                    IntOffset((delta * pageWidthPx).roundToInt(), 0)
                },
        )
    }
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
                PagerShellTab(COUIIcons.GridView, GridViewFill, "主页"),
                PagerShellTab(COUIIcons.Tune, TuneFill, "生成参数"),
                PagerShellTab(COUIIcons.Folder, COUIIcons.FolderFill, "预设"),
            ).forEachIndexed { index, tab ->
                BottomFloatingNavigationBarItem(
                    selected = pagerState.targetPage == index,
                    onClick = { onSelectPage(index) },
                    icon = tab.icon,
                    selectedIcon = tab.selectedIcon,
                    label = tab.label,
                )
            }
        }
    }
}

/** COUI 17 底栏：未选中用线性图标，选中换成同一轮廓的面性图标。 */
private class PagerShellTab(val icon: ImageVector, val selectedIcon: ImageVector, val label: String)

// COUI 只给 Folder 配了面性版本；宫格和调节的面性版本由线性图标去掉内轮廓得到，几何与线性版完全一致。
private val GridViewFill by lazy { COUIIcons.GridView.filledTwin() }
private val TuneFill by lazy { COUIIcons.Tune.filledTwin() }

/**
 * COUI 的线性图标没有用描边，而是「外轮廓 + 反向绕行的内轮廓」拼出的填充：内轮廓在 NonZero 下抵消成镂空。
 * 每条路径里与最大轮廓绕行方向相反的就是这些内轮廓，去掉后宫格成为实心方块、调节的旋钮成为实心圆。
 */
private fun ImageVector.filledTwin(): ImageVector =
    ImageVector.Builder(
        name = "$name.Fill",
        defaultWidth = defaultWidth,
        defaultHeight = defaultHeight,
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
        tintColor = tintColor,
        tintBlendMode = tintBlendMode,
        autoMirror = autoMirror,
    ).apply { addFilled(root) }.build()

private fun ImageVector.Builder.addFilled(group: VectorGroup) {
    group.forEach { node ->
        when (node) {
            is VectorGroup -> {
                addGroup(
                    name = node.name,
                    rotate = node.rotation,
                    pivotX = node.pivotX,
                    pivotY = node.pivotY,
                    scaleX = node.scaleX,
                    scaleY = node.scaleY,
                    translationX = node.translationX,
                    translationY = node.translationY,
                    clipPathData = node.clipPathData,
                )
                addFilled(node)
                clearGroup()
            }
            is VectorPath -> addPath(
                pathData = node.pathData.withoutHoles(),
                pathFillType = node.pathFillType,
                name = node.name,
                fill = node.fill,
                fillAlpha = node.fillAlpha,
                stroke = node.stroke,
                strokeAlpha = node.strokeAlpha,
                strokeLineWidth = node.strokeLineWidth,
                strokeLineCap = node.strokeLineCap,
                strokeLineJoin = node.strokeLineJoin,
                strokeLineMiter = node.strokeLineMiter,
            )
        }
    }
}

private fun List<PathNode>.withoutHoles(): List<PathNode> {
    val contours = mutableListOf<MutableList<PathNode>>()
    forEach { node ->
        if (node is PathNode.MoveTo || contours.isEmpty()) contours += mutableListOf<PathNode>()
        contours.last() += node
    }
    val areas = contours.map { it.signedArea() }
    val outer = areas.maxByOrNull { abs(it) } ?: return this
    return contours.filterIndexed { i, _ -> areas[i] * outer > 0f }.flatten()
}

/** 按各段终点算的鞋带面积，只用来判断绕行方向；COUI 图标只用绝对坐标指令。 */
private fun List<PathNode>.signedArea(): Float {
    var area = 0f
    var startX = 0f
    var startY = 0f
    var x = 0f
    var y = 0f
    fun lineTo(nx: Float, ny: Float) {
        area += x * ny - nx * y
        x = nx
        y = ny
    }
    forEach { node ->
        when (node) {
            is PathNode.MoveTo -> {
                startX = node.x
                startY = node.y
                x = node.x
                y = node.y
            }
            is PathNode.LineTo -> lineTo(node.x, node.y)
            is PathNode.HorizontalTo -> lineTo(node.x, y)
            is PathNode.VerticalTo -> lineTo(x, node.y)
            is PathNode.QuadTo -> lineTo(node.x2, node.y2)
            is PathNode.CurveTo -> lineTo(node.x3, node.y3)
            else -> Unit
        }
    }
    lineTo(startX, startY)
    return area / 2f
}
