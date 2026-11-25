package dev.artplus.enhancer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.AppBarBlurOverlayDefaults
import io.github.suqi8.coui.kmp.basic.HighlightedIconButton
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.Scaffold
import io.github.suqi8.coui.kmp.basic.ScrollBehavior
import io.github.suqi8.coui.kmp.basic.SmallTitle
import io.github.suqi8.coui.kmp.basic.TopAppBar
import io.github.suqi8.coui.kmp.basic.couiTopAppBarActionItem
import io.github.suqi8.coui.kmp.blur.LocalBackdrop
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.blur.rememberLayerBackdrop
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Back
import io.github.suqi8.coui.kmp.utils.COUIPopupUtils

/**
 * ColorOS 17 页面外壳。
 *
 * 采用 ColorOS 17 规范的沉浸式渐变顶栏、统一背景色与根部弹层宿主：
 *  - 页面背景使用 [AppBarBlurOverlayDefaults.containerColor]，彻底消除未设底色时窗口黑底与暗文字混在一起的黑底黑字问题；
 *  - 顶栏启用 [gradientGlass] 实时材质模糊；
 *  - `popupHost` 接入 `LocalBackdrop`，保证 Overlay 下拉菜单 / 对话框采样页面实时玻璃材质；
 *  - 顶栏操作项使用统一材质的 [HighlightedIconButton]。
 */
@Composable
fun Coui17Scaffold(
    topBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val pageColor = AppBarBlurOverlayDefaults.containerColor()
    val backdrop = rememberLayerBackdrop {
        drawRect(pageColor)
        drawContent()
    }
    Scaffold(
        containerColor = pageColor,
        edgeToEdge = true,
        topBar = { CompositionLocalProvider(LocalBackdrop provides backdrop) { topBar() } },
        popupHost = {
            CompositionLocalProvider(LocalBackdrop provides backdrop) {
                COUIPopupUtils.COUIPopupHost()
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().layerBackdrop(backdrop)) {
            content(innerPadding)
        }
    }
}

/** 把顶栏与手势条距离并入列表内边距，内容滑过顶栏下方。 */
@Composable
fun PaddingValues.listPadding(
    top: Dp,
    bottom: Dp,
    start: Dp = 0.dp,
    end: Dp = 0.dp,
): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection) + start,
        top = calculateTopPadding() + top,
        end = calculateEndPadding(layoutDirection) + end,
        bottom = calculateBottomPadding() + bottom,
    )
}

/** ColorOS 17 顶栏：大标题渐变玻璃，滑动收起，无分割线。 */
@Composable
fun Coui17TopAppBar(
    title: String,
    scrollBehavior: ScrollBehavior,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = title,
        color = AppBarBlurOverlayDefaults.containerColor(),
        scrollBehavior = scrollBehavior,
        gradientGlass = true,
        showDivider = false,
        navigationIcon = {
            if (onBack != null) {
                BarIconButton(COUIIcons.Back, "返回", onBack)
            }
        },
        actions = actions,
    )
}

/** 顶栏圆形按钮：40dp 圆形，带材质边框与光晕。 */
@Composable
fun BarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    HighlightedIconButton(
        onClick = onClick,
        modifier = Modifier.couiTopAppBarActionItem(),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

/**
 * ColorOS 17 设置页分组标题：直接使用 COUI [SmallTitle]，
 * 文字起始边与卡片内文字严格对齐在同一条基准线上。
 */
@Composable
fun GroupTitle(
    text: String,
    modifier: Modifier = Modifier,
    cardInset: Dp = 16.dp,
) {
    SmallTitle(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = cardInset),
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    )
}
