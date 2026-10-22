package dev.artplus.mobile

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.theme.COUITheme
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
                contentDescription = null,
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
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                }
                assets?.recfg?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
            PreviewMode.NormalDark -> {
                assets?.recNight?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
            PreviewMode.MonochromeLight -> {
                assets?.monochromeLight?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
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
                        contentDescription = null,
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
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(COUITheme.colorScheme.surfaceContainerHigh)
            .clickable(enabled = choiceEnabled, onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = COUITheme.textStyles.footnote1,
            color = COUITheme.colorScheme.onSurfaceVariantSummary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(118.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (missingMessage == null) {
                DesktopIconPreview(
                    desktopBackground = desktopBackground,
                    iconSize = iconSizeDp.dp,
                    wallpaperInitial = wallpaperInitial,
                    wallpaperKey = wallpaperKey,
                    loadWallpaper = loadWallpaper,
                ) {
                    GeneratedIconPreview(
                        assets = assets,
                        mode = mode,
                        modifier = Modifier.size(iconSizeDp.dp),
                        cornerRadiusDp = cornerRadiusDp,
                        materialColorProvider = materialColorProvider,
                    )
                }
            } else {
                DesktopIconPreview(
                    desktopBackground = desktopBackground,
                    iconSize = iconSizeDp.dp,
                    wallpaperInitial = wallpaperInitial,
                    wallpaperKey = wallpaperKey,
                    loadWallpaper = loadWallpaper,
                ) {
                    MissingIconPreview(
                        modifier = Modifier.size(iconSizeDp.dp),
                        mode = mode,
                        cornerRadiusDp = cornerRadiusDp,
                        materialColorProvider = materialColorProvider,
                    )
                }
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
            .clip(RoundedCornerShape(12.dp)),
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
    val borderColor = if (selected) {
        COUITheme.colorScheme.primaryVariant
    } else {
        COUITheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.18f)
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(COUITheme.colorScheme.secondaryVariant.copy(alpha = if (selected) 0.82f else 0.52f))
            .clickable(enabled = !isBusy && !selected, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(borderColor.copy(alpha = 0.14f))
                .padding(2.dp),
        ) {
            PreviewDesktopBackgroundSurface(
                option = option,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(6.dp)),
                wallpaperInitial = wallpaperInitial,
                wallpaperKey = wallpaperKey,
                loadWallpaper = loadWallpaper,
            )
        }
        Text(
            text = option.label,
            style = COUITheme.textStyles.footnote1,
            color = if (selected) {
                COUITheme.colorScheme.onSurface
            } else {
                COUITheme.colorScheme.onSurfaceVariantSummary
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
