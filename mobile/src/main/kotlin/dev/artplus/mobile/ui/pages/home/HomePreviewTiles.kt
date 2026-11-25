package dev.artplus.mobile.ui.pages.home

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import io.github.suqi8.coui.kmp.basic.CardDefaults
import io.github.suqi8.coui.kmp.basic.CircularProgressIndicator
import io.github.suqi8.coui.kmp.basic.ProgressIndicatorDefaults
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.couiGlassSurface
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.blur.rememberLayerBackdrop
import io.github.suqi8.coui.kmp.squircle.SquircleShape
import io.github.suqi8.coui.kmp.theme.COUITheme
import dev.artplus.mobile.DEFAULT_PREVIEW_CORNER_RADIUS_DP
import dev.artplus.mobile.DEFAULT_PREVIEW_ICON_SIZE_DP
import dev.artplus.mobile.pipeline.PreviewAssets
import dev.artplus.mobile.pipeline.PreviewMode
import dev.artplus.mobile.ui.cards.AiIconLoadingPreview
import dev.artplus.mobile.ui.cards.PreviewDesktopBackground
import dev.artplus.mobile.ui.components.GroupCardShape
/**
 * Slice 2.2：主页预览基础 Tiles（原 MainActivity 残留本体原样搬迁）。
 * Composable 只收 state + onEvent；Activity 状态经参数/回调注入，行为与 UI 100% 等价。
 * - PreviewDesktopBackgroundSurface：壁纸经 wallpaperInitial/wallpaperKey/loadWallpaper 注入。
 * - GeneratedIconPreview：MD3 取色经 materialColorProvider 注入；corner 显式化。
 * - MissingIconPreview：纯 UI 直接搬迁，默认圆角改用 TuningParams 常量（调用方均已显式传参）。
 * - DesktopIconPreview/PreviewTile/TopPreviewStripTile/PreviewBackgroundOption：state 显式透传。
 */

@Composable
internal fun PreviewDesktopBackgroundSurface(
    option: PreviewDesktopBackground,
    modifier: Modifier = Modifier,
    wallpaperInitial: Bitmap?,
    wallpaperKey: String?,
    loadWallpaper: suspend () -> Bitmap?,
) {
    val wallpaper by produceState<Bitmap?>(
        initialValue = if (option == PreviewDesktopBackground.Wallpaper) {
            wallpaperInitial
        } else null,
        key1 = option,
        key2 = wallpaperKey,
    ) {
        if (option == PreviewDesktopBackground.Wallpaper) {
            if (value == null) {
                value = loadWallpaper()
            }
        } else {
            value = null
        }
    }
    val wallpaperImage = remember(wallpaper) { wallpaper?.asImageBitmap() }
    Box(modifier = modifier.background(option.fallbackColor)) {
        if (wallpaperImage != null) {
            Image(
                bitmap = wallpaperImage,
                contentDescription = "壁纸预览",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
internal fun GeneratedIconPreview(
    assets: PreviewAssets?,
    mode: PreviewMode,
    modifier: Modifier = Modifier.size(72.dp),
    cornerRadiusDp: Int = DEFAULT_PREVIEW_CORNER_RADIUS_DP,
    materialColorProvider: (String, Color) -> Color,
) {
    val iconShape = RoundedCornerShape(cornerRadiusDp.dp)
    val md3LightBackground = materialColorProvider("system_accent1_100", Color(0xFFEADDFF))
    val md3LightForeground = materialColorProvider("system_accent1_700", Color(0xFF21005D))
    val md3DarkBackground = materialColorProvider("system_accent1_700", Color(0xFF4F378B))
    val md3DarkForeground = materialColorProvider("system_accent1_100", Color(0xFFEADDFF))
    val background = when (mode) {
        PreviewMode.NormalLight -> Color.White
        PreviewMode.NormalDark -> Color(0xFF1C1B1F)
        PreviewMode.MonochromeLight -> md3LightBackground
        PreviewMode.MonochromeDark -> md3DarkBackground
    }

    Box(
        modifier = modifier
            .clip(iconShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        when (mode) {
            PreviewMode.NormalLight -> {
                assets?.recbg?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "图标背景预览",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                }
                assets?.recfg?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "图标前景预览",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
            PreviewMode.NormalDark -> {
                assets?.recNight?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "深色图标背景预览",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
            PreviewMode.MonochromeLight -> {
                assets?.monochromeLight?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "单色图标预览",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(md3LightForeground),
                    )
                }
            }
            PreviewMode.MonochromeDark -> {
                assets?.monochromeDark?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "单色深色图标预览",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(md3DarkForeground),
                    )
                }
            }
        }
    }
}

@Composable
internal fun MissingIconPreview(
    modifier: Modifier = Modifier.size(72.dp),
    mode: PreviewMode? = null,
    compact: Boolean = false,
    cornerRadiusDp: Int = DEFAULT_PREVIEW_CORNER_RADIUS_DP,
    materialColorProvider: (String, Color) -> Color,
) {
    // 原 MainActivity.systemMaterialColor 经 Activity resources 解析；此处经 provider 注入保持 100% 等价。
    // provider 为空时回退到原 fallback（非 S 设备 / 预览场景与原 fallback 一致）。
    val md3LightBackground = materialColorProvider("system_accent1_100", Color(0xFFEADDFF))
    val md3DarkBackground = materialColorProvider("system_accent1_700", Color(0xFF4F378B))
    val iconBackground = when (mode) {
        PreviewMode.NormalDark -> Color(0xFF1C1B1F)
        PreviewMode.MonochromeLight -> md3LightBackground
        PreviewMode.MonochromeDark -> md3DarkBackground
        PreviewMode.NormalLight,
        null -> COUITheme.colorScheme.surfaceContainerHigh
    }
    val markColor = when (mode) {
        PreviewMode.MonochromeDark -> Color.White
        PreviewMode.NormalDark -> Color(0xFFE7E1E5)
        else -> COUITheme.colorScheme.onSurfaceVariantSummary
    }
    val outerRadius = cornerRadiusDp.dp
    val innerRadius = (cornerRadiusDp * 0.7f).dp
    val innerPadding = if (compact) 11.dp else 14.dp

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(outerRadius))
            .background(iconBackground),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clip(RoundedCornerShape(innerRadius))
                .background(markColor.copy(alpha = 0.10f)),
        )
        Box(
            modifier = Modifier
                .fillMaxSize(if (compact) 0.38f else 0.40f)
                .clip(RoundedCornerShape(if (compact) 7.dp else 9.dp))
                .background(markColor.copy(alpha = 0.18f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(if (compact) 9.dp else 11.dp)
                .size(if (compact) 9.dp else 11.dp)
                .clip(RoundedCornerShape(50))
                .background(markColor.copy(alpha = 0.28f)),
        )
    }
}

@Composable
internal fun DesktopIconPreview(
    desktopBackground: PreviewDesktopBackground,
    iconSize: Dp,
    wallpaperInitial: Bitmap?,
    wallpaperKey: String?,
    loadWallpaper: suspend () -> Bitmap?,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .clip(RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center,
    ) {
        PreviewDesktopBackgroundSurface(
            option = desktopBackground,
            modifier = Modifier.fillMaxSize(),
            wallpaperInitial = wallpaperInitial,
            wallpaperKey = wallpaperKey,
            loadWallpaper = loadWallpaper,
        )
        Box(
            modifier = Modifier.size(iconSize),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
internal fun PreviewTile(
    label: String,
    assets: PreviewAssets?,
    mode: PreviewMode,
    desktopBackground: PreviewDesktopBackground,
    iconSizeDp: Int,
    loading: Boolean,
    // 模型正在跑这个变体：标题后面挂一枚和文字同高的小圆环，转着表示还没出结果
    labelLoading: Boolean = false,
    choiceEnabled: Boolean,
    cornerRadiusDp: Int,
    wallpaperInitial: Bitmap?,
    wallpaperKey: String?,
    loadWallpaper: suspend () -> Bitmap?,
    materialColorProvider: (String, Color) -> Color,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val missingMessage = assets?.missingMessage(mode)
    val loadingAlpha by animateFloatAsState(
        targetValue = if (loading) 1f else 0f,
        animationSpec = tween(durationMillis = if (loading) 260 else 360),
        label = "PreviewLoadingAlpha",
    )
    val isDark = COUITheme.colorScheme.background.luminance() < 0.5f
    val glassContainerColor = if (isDark) Color(0xFF1F1F23) else Color.White
    val glassAlpha = if (isDark) 0.72f else 0.78f
    val glassFallbackColor = if (isDark) Color(0xFF202024).copy(alpha = 0.84f) else Color.White.copy(alpha = 0.86f)
    val glassTopBorder = if (isDark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.45f)
    val tileBackdrop = rememberLayerBackdrop {
        drawRect(desktopBackground.fallbackColor)
        drawContent()
    }

    Box(
        modifier = modifier
            .height(152.dp)
            .clip(GroupCardShape)
            .border(0.5.dp, COUITheme.colorScheme.dividerLine, GroupCardShape)
            .clickable(enabled = choiceEnabled, onClick = onClick),
    ) {
        PreviewDesktopBackgroundSurface(
            option = desktopBackground,
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(tileBackdrop),
            wallpaperInitial = wallpaperInitial,
            wallpaperKey = wallpaperKey,
            loadWallpaper = loadWallpaper,
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (missingMessage == null) {
                    GeneratedIconPreview(
                        assets = assets,
                        mode = mode,
                        modifier = Modifier.size(iconSizeDp.dp),
                        cornerRadiusDp = cornerRadiusDp,
                        materialColorProvider = materialColorProvider,
                    )
                } else {
                    MissingIconPreview(
                        modifier = Modifier.size(iconSizeDp.dp),
                        mode = mode,
                        cornerRadiusDp = cornerRadiusDp,
                        materialColorProvider = materialColorProvider,
                    )
                }
                if (loadingAlpha > 0.01f) {
                    AiIconLoadingPreview(
                        modifier = Modifier
                            .size(72.dp)
                            .graphicsLayer { alpha = loadingAlpha },
                        overlay = true,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .couiGlassSurface(
                        backdrop = tileBackdrop,
                        shape = RectangleShape,
                        containerColor = glassContainerColor,
                        fallback = Modifier.background(glassFallbackColor),
                        containerAlpha = glassAlpha,
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(glassTopBorder),
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = label,
                        style = COUITheme.textStyles.footnote1.copy(fontWeight = FontWeight.Medium),
                        color = COUITheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (labelLoading) {
                        // 12dp 与 footnote1 行高齐平；底色淡染一圈，转动的是主色弧
                        CircularProgressIndicator(
                            progress = null,
                            size = 12.dp,
                            strokeWidth = 1.5.dp,
                            colors = ProgressIndicatorDefaults.circularProgressIndicatorColors(
                                foregroundColor = COUITheme.colorScheme.primary,
                                backgroundColor = COUITheme.colorScheme.primary.copy(alpha = 0.2f),
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun TopPreviewStripTile(
    assets: PreviewAssets?,
    mode: PreviewMode,
    loading: Boolean,
    desktopBackground: PreviewDesktopBackground,
    iconSizeDp: Int,
    cornerRadiusDp: Int,
    wallpaperInitial: Bitmap?,
    wallpaperKey: String?,
    loadWallpaper: suspend () -> Bitmap?,
    materialColorProvider: (String, Color) -> Color,
    modifier: Modifier = Modifier,
) {
    val ready = assets != null && assets.missingMessage(mode) == null
    val scaleRatio = (iconSizeDp.toFloat() / DEFAULT_PREVIEW_ICON_SIZE_DP.toFloat()).coerceIn(0.6f, 1.35f)
    val scaledCornerDp = (cornerRadiusDp.toFloat() * (scaleRatio * 0.72f)).roundToInt().coerceAtLeast(0)
    val iconFraction = (0.76f * scaleRatio).coerceIn(0.42f, 0.95f)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(SquircleShape(18.dp, weight = CardDefaults.GroupWeight)),
        contentAlignment = Alignment.Center,
    ) {
        PreviewDesktopBackgroundSurface(
            option = desktopBackground,
            modifier = Modifier.fillMaxSize(),
            wallpaperInitial = wallpaperInitial,
            wallpaperKey = wallpaperKey,
            loadWallpaper = loadWallpaper,
        )
        if (ready) {
            GeneratedIconPreview(
                assets = assets,
                mode = mode,
                modifier = Modifier.fillMaxSize(iconFraction),
                cornerRadiusDp = scaledCornerDp,
                materialColorProvider = materialColorProvider,
            )
        } else {
            MissingIconPreview(
                modifier = Modifier.fillMaxSize(iconFraction),
                mode = mode,
                compact = true,
                cornerRadiusDp = scaledCornerDp,
                materialColorProvider = materialColorProvider,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.42f))
                .padding(horizontal = 3.dp, vertical = 1.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = mode.label,
                style = COUITheme.textStyles.footnote1.copy(fontSize = 10.sp),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
        if (loading) {
            AiIconLoadingPreview(
                modifier = Modifier.size(34.dp),
                overlay = true,
            )
        }
    }
}

@Composable
internal fun PreviewBackgroundOption(
    option: PreviewDesktopBackground,
    selected: Boolean,
    isBusy: Boolean,
    wallpaperInitial: Bitmap?,
    wallpaperKey: String?,
    loadWallpaper: suspend () -> Bitmap?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    // 卡片本体：未选中 surfaceContainer（浅色为白）+ dividerLine 细描边；
    // 选中 tertiaryContainer（COUI 的 15% 蓝浅底，不是 primaryContainer 的实心 #0080FF）
    // + 1.5dp primaryVariant 描边，文字用 onSurface —— 浅底深字，不会出现「选中变黑/变蓝、
    // 字看不清」。
    val containerColor = if (selected) {
        COUITheme.colorScheme.tertiaryContainer
    } else {
        COUITheme.colorScheme.surfaceContainer
    }
    val outlineColor = if (selected) {
        COUITheme.colorScheme.primaryVariant
    } else {
        COUITheme.colorScheme.dividerLine
    }
    val labelColor = if (selected) {
        COUITheme.colorScheme.onSurface
    } else {
        COUITheme.colorScheme.onSurfaceVariantSummary
    }
    val optionShape = SquircleShape(14.dp, weight = CardDefaults.GroupWeight)
    Column(
        modifier = modifier
            .clip(optionShape)
            .background(containerColor)
            .border(if (selected) 1.5.dp else 1.dp, outlineColor, optionShape)
            .clickable(enabled = !isBusy && !selected, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .clip(SquircleShape(8.dp, weight = CardDefaults.GroupWeight))
                .background(outlineColor.copy(alpha = 0.18f))
                .padding(2.dp),
        ) {
            PreviewDesktopBackgroundSurface(
                option = option,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(SquircleShape(6.dp, weight = CardDefaults.GroupWeight)),
                wallpaperInitial = wallpaperInitial,
                wallpaperKey = wallpaperKey,
                loadWallpaper = loadWallpaper,
            )
        }
        Text(
            text = option.label,
            style = COUITheme.textStyles.footnote1,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
