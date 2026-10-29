package dev.artplus.enhancer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.AppBarBlurOverlayDefaults
import io.github.suqi8.coui.kmp.basic.HighlightedIconButton
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.Scaffold
import io.github.suqi8.coui.kmp.basic.ScrollBehavior
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TopAppBar
import io.github.suqi8.coui.kmp.basic.couiTopAppBarActionItem
import io.github.suqi8.coui.kmp.blur.LocalBackdrop
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.blur.rememberLayerBackdrop
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Back
import io.github.suqi8.coui.kmp.theme.COUITheme

/**
 * ColorOS 17 页面外壳。
 *
 * 采用 ColorOS 17 规范的沉浸式渐变顶栏与统一背景色：
 *  - 页面背景使用 [AppBarBlurOverlayDefaults.containerColor]，彻底消除未设底色时窗口黑底与暗文字混在一起的黑底黑字问题；
 *  - 顶栏启用 [gradientGlass] 实时材质模糊；
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
 * ColorOS 17 设置页分组标题：32dp 高，16sp 中粗 headline2，主文字色。
 * 文字起始边与卡片内文字严格对齐在同一条基准线上。
 */
@Composable
fun GroupTitle(
    text: String,
    modifier: Modifier = Modifier,
    cardInset: Dp = 16.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = cardInset)
            .padding(bottom = 3.5.dp)
            .height(32.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(start = 16.dp),
            style = COUITheme.textStyles.headline2,
            color = COUITheme.colorScheme.onSurface,
        )
    }
}

/** 刷新图标。 */
object EnhancerIcons {
    val Refresh: ImageVector by lazy {
        ImageVector.Builder(
            name = "Refresh",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(17.65f, 6.35f)
                curveTo(16.2f, 4.9f, 14.21f, 4f, 12f, 4f)
                curveTo(7.58f, 4f, 4.01f, 7.58f, 4.01f, 12f)
                curveTo(4.01f, 16.42f, 7.58f, 20f, 12f, 20f)
                curveTo(15.73f, 20f, 18.84f, 17.45f, 19.73f, 14f)
                horizontalLineTo(17.65f)
                curveTo(16.83f, 16.33f, 14.61f, 18f, 12f, 18f)
                curveTo(8.69f, 18f, 6f, 15.31f, 6f, 12f)
                curveTo(6f, 8.69f, 8.69f, 6f, 12f, 6f)
                curveTo(13.66f, 6f, 15.14f, 6.69f, 16.22f, 7.78f)
                lineTo(13f, 11f)
                horizontalLineTo(20f)
                verticalLineTo(4f)
                close()
            }
        }.build()
    }
}
