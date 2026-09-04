package dev.artplus.mobile

import android.Manifest
import android.app.AppOpsManager
import android.app.WallpaperManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.LocalServerSocket
import android.net.LocalSocket
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.os.Process
import android.provider.DocumentsContract
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.LruCache
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.blur
import dev.artplus.mobile.glass.lens
import dev.artplus.mobile.glass.vibrancy
import dev.artplus.mobile.glass.FloatingBottomBar
import dev.artplus.mobile.glass.FloatingBottomBarItem
import com.kyant.shapes.Capsule
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.foundation.shape.CircleShape
import androidx.core.view.WindowInsetsControllerCompat
import com.composables.icons.lucide.BadgeCheck
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Cpu
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.Eraser
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.FileUp
import com.composables.icons.lucide.GlassWater
import com.composables.icons.lucide.Grid2x2
import com.composables.icons.lucide.KeyRound
import com.composables.icons.lucide.Layers
import com.composables.icons.lucide.Link
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageSquareText
import com.composables.icons.lucide.Palette
import com.composables.icons.lucide.Radius
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.Scale
import com.composables.icons.lucide.Save
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.SlidersHorizontal
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Upload
import com.composables.icons.lucide.X
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URL
import java.net.URLDecoder
import java.nio.FloatBuffer
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.lang.reflect.InvocationTargetException
import java.util.ArrayDeque
import java.util.UUID
import java.util.zip.ZipInputStream
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.CheckboxDefaults
import top.yukonga.miuix.kmp.basic.DropdownColors
import top.yukonga.miuix.kmp.basic.DropdownDefaults
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SliderDefaults
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.basic.ArrowUpDown
import top.yukonga.miuix.kmp.popup.WindowDropdownPopup
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.layout.BottomSheetDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import android.graphics.Color as AndroidColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.caverock.androidsvg.SVG
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
        null -> MiuixTheme.colorScheme.surfaceContainerHigh
    }
    val markColor = when (mode) {
        PreviewMode.MonochromeDark -> Color.White
        PreviewMode.NormalDark -> Color(0xFFE7E1E5)
        else -> MiuixTheme.colorScheme.onSurfaceVariantSummary
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
            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
            .clickable(enabled = choiceEnabled, onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
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
                style = MiuixTheme.textStyles.footnote1.copy(fontSize = 10.sp),
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
        MiuixTheme.colorScheme.primaryVariant
    } else {
        MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.18f)
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MiuixTheme.colorScheme.secondaryContainer.copy(alpha = if (selected) 0.82f else 0.52f))
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
            style = MiuixTheme.textStyles.footnote1,
            color = if (selected) {
                MiuixTheme.colorScheme.onSurface
            } else {
                MiuixTheme.colorScheme.onSurfaceVariantSummary
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
