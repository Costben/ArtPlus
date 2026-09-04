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
import androidx.compose.ui.platform.LocalContext
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
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import android.graphics.Color as AndroidColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.caverock.androidsvg.SVG

@Composable
internal fun MainActivity.ArtPlusScreen() {
    val tuningState = mainViewModel.params.collectAsState().value
    // Slice 3.4b：非组合回调（onStop 等）内的 Toast 共用此前捕获的 Context。
    val __actCancel = LocalContext.current
    val pageBackground = if (isSystemInDarkTheme()) {
        Color.Black
    } else {
        Color(0xFFF7F7F7)
    }
    val selectedApp by remember {
        derivedStateOf { apps.firstOrNull { it.packageName == mainViewModel.picker.value.selectedPackageName } }
    }
    val generatedCount by remember {
        derivedStateOf { apps.count { it.packageName in mainViewModel.picker.value.generatedPackageNames && AppVisibility.shouldShowInPicker(it.applicationInfo, it.launchable, mainViewModel.picker.value.showSystemApps, packageName) } }
    }
    val launcherCount by remember {
        derivedStateOf { apps.count { it.launchable } }
    }
    var systemBackProgress by remember { mutableStateOf(0f) }
    val completingBackProgress = remember { Animatable(0f) }
    val cancellingBackProgress = remember { Animatable(0f) }
    val childEnterProgress = remember { Animatable(1f) }
    var isCompletingBackGesture by remember { mutableStateOf(false) }
    val screenScope = rememberCoroutineScope()
    val backProgress = maxOf(
        systemBackProgress,
        completingBackProgress.value,
        cancellingBackProgress.value,
    )
    val sharedPreviewSession = mainViewModel.previewSession.value.activeGenerationSession?.takeIf {
        it.packageName == mainViewModel.previewSession.value.previewPackageName && it.outDir.absolutePath == mainViewModel.previewSession.value.previewDirPath
    }
    val sharedPreviewTuning = run {
    paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
}

    LaunchedEffect(
        mainViewModel.previewSession.value.previewPackageName,
        mainViewModel.previewSession.value.previewDirPath,
        sharedPreviewSession,
        PreviewSelections.fromNames(tuningState.previewNormalLight, tuningState.previewNormalDark, tuningState.previewMonochromeLight, tuningState.previewMonochromeDark),
        mainViewModel.previewSession.value.previewVersion,
        sharedPreviewTuning,
    ) {
        val dirPath = mainViewModel.previewSession.value.previewDirPath
        val packageName = mainViewModel.previewSession.value.previewPackageName
        if (dirPath.isNullOrBlank() || packageName.isNullOrBlank()) {
            mainViewModel.updatePreviewSession { it -> it.copy(sharedPreviewAssets = (null)) }
            mainViewModel.updatePreviewSession { it -> it.copy(isPreviewAssetsRefreshing = (false)) }
            return@LaunchedEffect
        }
        mainViewModel.updatePreviewSession { it -> it.copy(isPreviewAssetsRefreshing = (true)) }
        try {
            delay(PREVIEW_LIVE_ASSET_DEBOUNCE_MS)
            val diskAssets = withContext(Dispatchers.IO) {
                run {
    PreviewAssets(
                recbg = run {
        BitmapFactory.decodeFile(File(((File(dirPath))), ("recbg.png")).absolutePath)?.also { it.prepareToDraw() }
    },
                recfg = run {
        BitmapFactory.decodeFile(File(((File(dirPath))), ("recfg.png")).absolutePath)?.also { it.prepareToDraw() }
    },
                recNight = run {
        BitmapFactory.decodeFile(File(((File(dirPath))), ("rec_night.png")).absolutePath)?.also { it.prepareToDraw() }
    },
                monochromeLight = run {
        BitmapFactory.decodeFile(File(((File(dirPath))), ("monochrome_light.png")).absolutePath)?.also { it.prepareToDraw() }
    }
                    ?: run {
        BitmapFactory.decodeFile(File(((File(dirPath))), ("monochrome.png")).absolutePath)?.also { it.prepareToDraw() }
    },
                monochromeDark = run {
        BitmapFactory.decodeFile(File(((File(dirPath))), ("monochrome_dark.png")).absolutePath)?.also { it.prepareToDraw() }
    }
                    ?: run {
        BitmapFactory.decodeFile(File(((File(dirPath))), ("monochrome.png")).absolutePath)?.also { it.prepareToDraw() }
    },
            )
}.preparedForDraw()
            }
            val liveAssets = sharedPreviewSession?.let { session ->
                try {
                    withContext(previewWorkerDispatcher) {
                        run {

            val params = mainViewModel.params.value
            return@run previewAssetsForSelections(
                session = (session),
                selections = (PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark)),
                edgePolishPercent = params.edgePolishPercent,
                foregroundSubjectPercent = params.foregroundSubjectPercent,
                rmbgTunedForeground = { __a0: IconCandidate -> run {

                val params = mainViewModel.params.value
                return@run rmbgTunedForegroundRaw(
                    candidate = __a0,
                    rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
                    rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
                    rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
                    rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
                )
    } },
                liquidGlassEnabled = params.liquidGlassEnabled,
                liquidGlassBackgroundMistAlpha = params.liquidGlassBackgroundMistAlpha,
                liquidGlassTopAlpha = params.liquidGlassTopAlpha,
                liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
                liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
                liquidGlassOuterWidth = params.liquidGlassOuterWidth,
                liquidGlassRadius = params.liquidGlassRadius,
                liquidGlassSubjectScalePercent = params.liquidGlassSubjectScalePercent,
                liquidGlassSubjectShadowAlpha = params.liquidGlassSubjectShadowAlpha,
                liquidGlassSubjectOutlineWidth = params.liquidGlassSubjectOutlineWidth,
                liquidGlassSubjectInnerOutlineWidth = params.liquidGlassSubjectInnerOutlineWidth,
                liquidGlassSubjectOpacityPercent = params.liquidGlassSubjectOpacityPercent,
                foregroundShadowLevel = params.foregroundShadowLevel,
                nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
            )
}.preparedForDraw()
                    }
                } catch (_: CancellationException) {
                    throw CancellationException()
                } catch (_: Throwable) {
                    null
                }
            }
            mainViewModel.updatePreviewSession { it -> it.copy(sharedPreviewAssets = (liveAssets ?: diskAssets)) }
        } catch (_: CancellationException) {
            throw CancellationException()
        } catch (_: Throwable) {
            mainViewModel.updatePreviewSession { it -> it.copy(sharedPreviewAssets = (null)) }
        } finally {
            mainViewModel.updatePreviewSession { it -> it.copy(isPreviewAssetsRefreshing = (false)) }
        }
    }

    fun completeBackFrom(progress: Float) {
        if (isCompletingBackGesture) {
            return
        }
        val start = progress.coerceIn(0f, 1f)
        isCompletingBackGesture = true
        mainViewModel.updatePreviewSession { it -> it.copy(skipNextHomeReturnAnimation = (true)) }
        screenScope.launch {
            completingBackProgress.snapTo(start)
            cancellingBackProgress.snapTo(0f)
            completingBackProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = ((1f - start) * 220f).roundToInt().coerceIn(90, 220)),
            )
            mainViewModel.updateShell { it -> it.copy(currentPage = (AppPage.Home)) }
            delay(40)
            systemBackProgress = 0f
            completingBackProgress.snapTo(0f)
            cancellingBackProgress.snapTo(0f)
            isCompletingBackGesture = false
            mainViewModel.updatePreviewSession { it -> it.copy(skipNextHomeReturnAnimation = (false)) }
        }
    }

    LaunchedEffect(mainViewModel.shell.value.currentPage, mainViewModel.previewSession.value.skipNextHomeReturnAnimation, isCompletingBackGesture) {
        if (mainViewModel.shell.value.currentPage != AppPage.Home) {
            if (!isCompletingBackGesture) {
                systemBackProgress = 0f
                completingBackProgress.snapTo(0f)
                cancellingBackProgress.snapTo(0f)
                childEnterProgress.snapTo(1f)
                childEnterProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 300),
                )
            }
            mainViewModel.updatePreviewSession { it -> it.copy(skipNextHomeReturnAnimation = (false)) }
        } else if (mainViewModel.previewSession.value.skipNextHomeReturnAnimation || isCompletingBackGesture) {
            delay(90)
            if (!isCompletingBackGesture) {
                systemBackProgress = 0f
                completingBackProgress.snapTo(0f)
                cancellingBackProgress.snapTo(0f)
                mainViewModel.updatePreviewSession { it -> it.copy(skipNextHomeReturnAnimation = (false)) }
            }
        } else {
            systemBackProgress = 0f
            completingBackProgress.snapTo(0f)
            cancellingBackProgress.snapTo(0f)
            childEnterProgress.snapTo(1f)
        }
    }

    PredictiveBackHandler(enabled = mainViewModel.shell.value.currentPage != AppPage.Home && !isCompletingBackGesture) { backEvents ->
        var latestProgress = systemBackProgress
        try {
            backEvents.collect { backEvent ->
                latestProgress = backEvent.progress.coerceIn(0f, 1f)
                cancellingBackProgress.snapTo(0f)
                systemBackProgress = latestProgress
            }
            completeBackFrom(maxOf(latestProgress, systemBackProgress, BACK_GESTURE_COMMIT_PROGRESS))
        } catch (_: CancellationException) {
            val start = maxOf(latestProgress, systemBackProgress).coerceIn(0f, 1f)
            systemBackProgress = 0f
            if (start > 0f && mainViewModel.shell.value.currentPage != AppPage.Home && !isCompletingBackGesture) {
                cancellingBackProgress.snapTo(start)
                cancellingBackProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = (start * 180f).roundToInt().coerceIn(70, 180)),
                )
            } else {
                cancellingBackProgress.snapTo(0f)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val overlayPage = mainViewModel.shell.value.currentPage.takeIf { it != AppPage.Home }
        val dimReveal = if (mainViewModel.previewSession.value.skipNextHomeReturnAnimation || isCompletingBackGesture || backProgress > 0f) {
            backProgress.coerceIn(0f, 1f)
        } else {
            0f
        }
        val overlayDimTarget = ((1f - childEnterProgress.value) * (1f - dimReveal)).coerceIn(0f, 1f)
        // 返回主页时平滑回弹（修主页抖动）；进入动画与返回手势跟手时保持实时
        val overlayDimSpec: FiniteAnimationSpec<Float> =
            if (overlayPage == null || (backProgress == 0f && !isCompletingBackGesture && childEnterProgress.value == 0f)) {
                tween(durationMillis = 280)
            } else {
                snap()
            }
        val overlayDim by animateFloatAsState(
            targetValue = overlayDimTarget,
            animationSpec = overlayDimSpec,
            label = "OverlayDim",
        )
        val overlayShadowPx = with(LocalDensity.current) { 24.dp.toPx() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val homeScale = 1f - 0.04f * overlayDim
                    scaleX = homeScale
                    scaleY = homeScale
                },
        ) {
            HomePage(
                pageBackground = pageBackground,
                selectedApp = selectedApp,
                launcherCount = launcherCount,
                totalCount = apps.size,
                generatedCount = generatedCount,
            )
        }
        if (overlayPage != null && overlayDim > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f * overlayDim)),
            )
        }

        if (overlayPage != null) {
            val isCompletingBack = mainViewModel.previewSession.value.skipNextHomeReturnAnimation || isCompletingBackGesture
            val reveal = if (isCompletingBack || backProgress > 0f) {
                backProgress.coerceIn(0f, 1f)
            } else {
                0f
            }
            val overlayCover = ((1f - childEnterProgress.value) * (1f - reveal)).coerceIn(0f, 1f)
            val overlayCorner = (40f * overlayCover).dp
            val overlayShape = RoundedCornerShape(topStart = overlayCorner, bottomStart = overlayCorner)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val enteringOffset = size.width * childEnterProgress.value
                        val leavingOffset = size.width * reveal * BACK_GESTURE_PAGE_TRANSLATION_RATIO
                        translationX = enteringOffset + leavingOffset
                        shadowElevation = overlayShadowPx * overlayCover
                        shape = overlayShape
                        clip = true
                    },
            ) {
                when (overlayPage) {
                    AppPage.AppPicker -> {
                        val scopedApps by remember {
                            derivedStateOf { apps.filter { entry -> AppVisibility.shouldShowInPicker(entry.applicationInfo, entry.launchable, mainViewModel.picker.value.showSystemApps, packageName) } }
                        }
                        val ungeneratedCount by remember {
                            derivedStateOf { scopedApps.size - generatedCount }
                        }
                        val filteredApps by remember {
                            derivedStateOf {
                                val query = mainViewModel.picker.value.queryText.trim().lowercase(Locale.ROOT)
                                val stateScopedApps = when (mainViewModel.picker.value.generatedFilter) {
                                    GeneratedFilter.All -> scopedApps
                                    GeneratedFilter.Generated -> scopedApps.filter { it.packageName in mainViewModel.picker.value.generatedPackageNames }
                                    GeneratedFilter.Ungenerated -> scopedApps.filter { it.packageName !in mainViewModel.picker.value.generatedPackageNames }
                                }
                                if (query.isEmpty()) {
                                    stateScopedApps
                                } else {
                                    stateScopedApps.filter { entry ->
                                        entry.label.lowercase(Locale.ROOT).contains(query) ||
                                            entry.packageName.lowercase(Locale.ROOT).contains(query)
                                    }
                                }
                            }
                        }
                        val scopeCount by remember {
                            derivedStateOf {
                                when (mainViewModel.picker.value.generatedFilter) {
                                    GeneratedFilter.All -> scopedApps.size
                                    GeneratedFilter.Generated -> generatedCount
                                    GeneratedFilter.Ungenerated -> ungeneratedCount
                                }
                            }
                        }
                        AppPickerPage(
                            pageBackground = pageBackground,
                            filteredApps = filteredApps,
                            scopeCount = scopeCount,
                            generatedCount = generatedCount,
                            ungeneratedCount = ungeneratedCount,
                        )
                    }

                    AppPage.About -> AboutPage(pageBackground = pageBackground)

                    AppPage.BatchPreview -> BatchPreviewPage(pageBackground = pageBackground)

                    AppPage.Home -> Unit
                }
            }
        }

        mainViewModel.transfer.value.batchApplyProgress?.let { progress ->
            BatchApplyProgressDialog(progress)
        }
        if (mainViewModel.transfer.value.singleExportSheetVisible && mainViewModel.transfer.value.exportProgress != null) {
            val p = mainViewModel.transfer.value.exportProgress!!
            BackupProgressBottomSheet(
                progress = p,
                onStop = { run {

            val state = SingleExportCancelState(
                singleExportJob = singleExportJob,
                sheetVisible = mainViewModel.transfer.value.singleExportSheetVisible,
                progress = mainViewModel.transfer.value.exportProgress,
            )
            cancelSingleExport(state, { __a0: String -> run {
        pickerToastStatus(
                    message = __a0,
                    postOnUi = { text -> runOnUiThread { mainViewModel.updateShell { it -> it.copy(statusText = (text)) } } },
                    showToast = { text ->
                        runOnUiThread {
                            Toast.makeText(__actCancel, text, Toast.LENGTH_SHORT).show()
                        }
                    },
                )
    } })
            singleExportJob = state.singleExportJob
            mainViewModel.updateTransfer { it -> it.copy(singleExportSheetVisible = (state.sheetVisible)) }
            mainViewModel.updateTransfer { it -> it.copy(exportProgress = (state.progress)) }
} },
                onBackground = {
                    // 单包导出后台：仅隐藏弹窗，任务继续（不显示设置页动效）
                    mainViewModel.updateTransfer { it -> it.copy(singleExportSheetVisible = (false)) }
                },
            )
        }
        if (mainViewModel.transfer.value.backupSheetVisible && mainViewModel.transfer.value.backupProgress != null) {
            val p = mainViewModel.transfer.value.backupProgress!!
            BackupProgressBottomSheet(
                progress = p,
                onStop = { run {

            val state = BackupCancelState(
                backupJob = backupJob,
                backupDotJob = backupDotJob,
                sheetVisible = mainViewModel.transfer.value.backupSheetVisible,
                inBackground = mainViewModel.transfer.value.backupInBackground,
                progress = mainViewModel.transfer.value.backupProgress,
                isBusy = mainViewModel.shell.value.isBusy,
            )
            cancelBackup(state, { __a0: String -> run {
        pickerToastStatus(
                    message = __a0,
                    postOnUi = { text -> runOnUiThread { mainViewModel.updateShell { it -> it.copy(statusText = (text)) } } },
                    showToast = { text ->
                        runOnUiThread {
                            Toast.makeText(__actCancel, text, Toast.LENGTH_SHORT).show()
                        }
                    },
                )
    } })
            backupJob = state.backupJob
            backupDotJob = state.backupDotJob
            mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (state.sheetVisible)) }
            mainViewModel.updateTransfer { it -> it.copy(backupInBackground = (state.inBackground)) }
            mainViewModel.updateTransfer { it -> it.copy(backupProgress = (state.progress)) }
            mainViewModel.updateShell { it -> it.copy(isBusy = (state.isBusy)) }
} },
                onBackground = {
                    mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (false)) }
                    mainViewModel.updateTransfer { it -> it.copy(backupInBackground = (true)) }
                    run {

            backupDotJob?.cancel()
            backupDotJob = mainScope.launch {
                while (isActive) {
                    delay(500)
                    mainViewModel.updateTransfer { it -> it.copy(backupBackgroundDots = (if (mainViewModel.transfer.value.backupBackgroundDots >= 3) 1 else mainViewModel.transfer.value.backupBackgroundDots + 1)) }
                }
            }
}
                },
            )
        } else if (!mainViewModel.transfer.value.backupSheetVisible && mainViewModel.transfer.value.backupInBackground && mainViewModel.transfer.value.backupProgress != null) {
            // 后台态不显示底部弹窗，但保留状态供设置页“备份中...”展示
        }

        mainViewModel.confirm.value.pendingServiceConfirm?.let { request ->
            ServiceConfirmDialog(
                request = request,
                onConfirm = { run {

            val request = mainViewModel.confirm.value.pendingServiceConfirm ?: return@run
            mainViewModel.updateConfirm { it -> it.copy(pendingServiceConfirm = (null)) }
            if ((true)) {
                request.onConfirm()
            }
} },
                onDismiss = { run {

            val request = mainViewModel.confirm.value.pendingServiceConfirm ?: return@run
            mainViewModel.updateConfirm { it -> it.copy(pendingServiceConfirm = (null)) }
            if ((false)) {
                request.onConfirm()
            }
} },
            )
        }
        run {
    RootWriteConfirmDialog(
                request = mainViewModel.confirm.value.pendingRootWriteConfirm,
                rememberSkip = mainViewModel.confirm.value.rootWriteConfirmRememberSkip,
                onDismiss = { mainViewModel.updateConfirm { it -> it.copy(pendingRootWriteConfirm = (null)) } },
                onToggleSkip = { mainViewModel.updateConfirm { it -> it.copy(rootWriteConfirmRememberSkip = (!mainViewModel.confirm.value.rootWriteConfirmRememberSkip)) } },
                onConfirm = { request, shouldSkip ->
                    val onConfirm = request.onConfirm
                    mainViewModel.updateConfirm { it -> it.copy(pendingRootWriteConfirm = (null)) }
                    if (shouldSkip) {
                        mainViewModel.updateConfirm { it -> it.copy(autoConfirmRootWrite = (true)) }
                        run {
        pickerSaveUiState(
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    selectedPackage = mainViewModel.picker.value.selectedPackageName,
                    generatedFilter = mainViewModel.picker.value.generatedFilter,
                    showSystemApps = mainViewModel.picker.value.showSystemApps,
                    queryText = mainViewModel.picker.value.queryText,
                    advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                    advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                    previewPackage = mainViewModel.previewSession.value.previewPackageName,
                    previewDir = mainViewModel.previewSession.value.previewDirPath,
                    previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                    previewNormalLight = mainViewModel.params.value.previewNormalLight,
                    previewNormalDark = mainViewModel.params.value.previewNormalDark,
                    previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                    previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                    desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                    iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                    cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                    batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                    batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                    batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                    batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                    batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                    customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                    autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                    autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                    outputUri = mainViewModel.shell.value.outputTreeUri,
                )
    }
                    }
                    onConfirm()
                },
            )
}
        run {

            val confirmState by mainViewModel.confirm.collectAsState()
            // Slice 3.1: Activity侧collect读VM单源；写经薄wrapper（重构期间保留）。
            RefreshConfirmDialog(
                visible = confirmState.refreshConfirmVisible,
                rememberAuto = confirmState.refreshConfirmRememberAuto,
                onDismiss = { mainViewModel.updateConfirm { it -> it.copy(refreshConfirmVisible = (false)) } },
                onToggleRemember = { mainViewModel.updateConfirm { it -> it.copy(refreshConfirmRememberAuto = (!mainViewModel.confirm.value.refreshConfirmRememberAuto)) } },
                onConfirm = { shouldAuto ->
                    mainViewModel.updateConfirm { it -> it.copy(refreshConfirmVisible = (false)) }
                    if (shouldAuto) {
                        mainViewModel.updateConfirm { it -> it.copy(autoConfirmRefresh = (true)) }
                        run {
        pickerSaveUiState(
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    selectedPackage = mainViewModel.picker.value.selectedPackageName,
                    generatedFilter = mainViewModel.picker.value.generatedFilter,
                    showSystemApps = mainViewModel.picker.value.showSystemApps,
                    queryText = mainViewModel.picker.value.queryText,
                    advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                    advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                    previewPackage = mainViewModel.previewSession.value.previewPackageName,
                    previewDir = mainViewModel.previewSession.value.previewDirPath,
                    previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                    previewNormalLight = mainViewModel.params.value.previewNormalLight,
                    previewNormalDark = mainViewModel.params.value.previewNormalDark,
                    previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                    previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                    desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                    iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                    cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                    batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                    batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                    batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                    batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                    batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                    customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                    autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                    autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                    outputUri = mainViewModel.shell.value.outputTreeUri,
                )
    }
                    }
                    run {
        mainViewModel.refreshArtPlusIconsAsync(
                    contentResolver = contentResolver,
                    apkPath = applicationInfo.sourceDir,
                )
    }
                },
            )
}
        run {
    PresetPageDialogs(
                saveDialogVisible = mainViewModel.presetUi.value.presetSaveDialogVisible,
                saveInitialName = mainViewModel.presetUi.value.presetSaveName,
                onSaveConfirm = { name ->
                    mainViewModel.updatePresetUi { it -> it.copy(presetSaveDialogVisible = (false)) }
                    run {
        saveCurrentAsPreset(
                    rawName = (name),
                    store = presetStore,
                    current = run {
            paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
        },
                    viewModel = mainViewModel,
                    onSaved = { preset, msg ->
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (preset.id)) }
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetBaseParams = (preset.params)) }
                        mainViewModel.updatePresetUi { it -> it.copy(presetListVersion = it.presetListVersion + (1)) }
                        mainViewModel.updatePresetUi { it -> it.copy(presetSaveDialogVisible = (false)) }
                        mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
                    },
                    onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                )
    }
                },
                onSaveDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetSaveDialogVisible = (false)) } },
                renameTarget = mainViewModel.presetUi.value.presetRenameTarget,
                onRenameConfirm = { id, name ->
                    mainViewModel.updatePresetUi { it -> it.copy(presetRenameTarget = (null)) }
                    run {
        renamePreset(
                    id = (id),
                    rawName = (name),
                    store = presetStore,
                    viewModel = mainViewModel,
                    onRenamed = { _, msg ->
                        mainViewModel.updatePresetUi { it -> it.copy(presetListVersion = it.presetListVersion + (1)) }
                        mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
                    },
                    onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                )
    }
                },
                onRenameDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetRenameTarget = (null)) } },
                actionMenuTarget = mainViewModel.presetUi.value.presetActionMenuTarget,
                actionMenuBusy = mainViewModel.shell.value.isBusy,
                onActionMenuDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetActionMenuTarget = (null)) } },
                onActionApply = { run {
        applyPreset(
                    preset = (it),
                    isBusy = mainViewModel.shell.value.isBusy,
                    isGeneratingGptCandidate = mainViewModel.previewSession.value.isGeneratingGptCandidate,
                    isGeneratingRmbgCandidate = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
                    before = run {
            paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
        },
                    viewModel = mainViewModel,
                    onApplied = { p, merged, msg ->
                        run {
            paramsApplyTuningParams(
                        params = (merged),
                        rebuildCandidates = (true),
                        persist = (true),
                        captureUndo = (true),
                        refreshPreview = (true),
                        getBefore = { run {
                paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
            } },
                        onCaptureUndo = { mainViewModel.updatePreviewSession { v -> v.copy(lastParamsSnapshot = (it)) } },
                        onParamsApplied = { before, applied, capture -> mainViewModel.onParamsApplied(before = before, applied = applied, captureUndo = capture) },
                        setDraftForegroundSubjectPercentText = { draftForegroundSubjectPercentText = it },
                        setDraftForegroundShadowLevelText = { draftForegroundShadowLevelText = it },
                        setDraftMonochromeThemeScaleText = { draftMonochromeThemeScaleText = it },
                        setDraftBackgroundSeparationText = { draftBackgroundSeparationText = it },
                        setDraftPlateRemovalText = { draftPlateRemovalText = it },
                        setDraftShadowRemovalText = { draftShadowRemovalText = it },
                        setDraftEdgePolishText = { draftEdgePolishText = it },
                        setDraftRmbgAlphaStrengthText = { draftRmbgAlphaStrengthText = it },
                        setDraftRmbgEdgeFeatherText = { draftRmbgEdgeFeatherText = it },
                        setDraftRmbgEdgeAdjustText = { draftRmbgEdgeAdjustText = it },
                        setDraftRmbgWeakAlphaKeepText = { draftRmbgWeakAlphaKeepText = it },
                        setDraftLiquidGlassRadiusText = { draftLiquidGlassRadiusText = it },
                        setDraftLiquidGlassOuterWidthText = { draftLiquidGlassOuterWidthText = it },
                        setDraftLiquidGlassTopAlphaText = { draftLiquidGlassTopAlphaText = it },
                        setDraftLiquidGlassBottomAlphaText = { draftLiquidGlassBottomAlphaText = it },
                        setDraftLiquidGlassBackgroundMistAlphaText = { draftLiquidGlassBackgroundMistAlphaText = it },
                        setDraftLiquidGlassBottomDarkAlphaText = { draftLiquidGlassBottomDarkAlphaText = it },
                        setDraftLiquidGlassSubjectScaleText = { draftLiquidGlassSubjectScaleText = it },
                        setDraftLiquidGlassSubjectOutlineWidthText = { draftLiquidGlassSubjectOutlineWidthText = it },
                        setDraftLiquidGlassSubjectInnerOutlineWidthText = { draftLiquidGlassSubjectInnerOutlineWidthText = it },
                        setDraftLiquidGlassSubjectShadowAlphaText = { draftLiquidGlassSubjectShadowAlphaText = it },
                        setDraftLiquidGlassSubjectOpacityText = { draftLiquidGlassSubjectOpacityText = it },
                        setDraftJsonParamsText = { draftJsonParamsText = it },
                        onSaveLocalSeparation = { run {
                paramsSaveLocalSeparationSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                        )
            } },
                        onSaveImageTuning = { run {
                paramsSaveImageTuningSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                        )
            } },
                        onSaveLiquidGlass = { run {
                paramsSaveLiquidGlassSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                            getBottomBarEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarEnabled },
                            getBottomBarBlurEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarBlurEnabled },
                        )
            } },
                        onSaveGpt = { run {
                paramsSaveGptSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                            getGptApiKey = { mainViewModel.gptRmbgSettings.value.gptApiKey },
                            getGptModelId = { mainViewModel.gptRmbgSettings.value.gptModelId },
                            getGptBaseUrl = { mainViewModel.gptRmbgSettings.value.gptBaseUrl },
                        )
            } },
                        onSaveUi = { run {
                pickerSaveUiState(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            selectedPackage = mainViewModel.picker.value.selectedPackageName,
                            generatedFilter = mainViewModel.picker.value.generatedFilter,
                            showSystemApps = mainViewModel.picker.value.showSystemApps,
                            queryText = mainViewModel.picker.value.queryText,
                            advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                            advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                            previewPackage = mainViewModel.previewSession.value.previewPackageName,
                            previewDir = mainViewModel.previewSession.value.previewDirPath,
                            previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                            previewNormalLight = mainViewModel.params.value.previewNormalLight,
                            previewNormalDark = mainViewModel.params.value.previewNormalDark,
                            previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                            previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                            desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                            iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                            cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                            batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                            batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                            batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                            batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                            batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                            customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                            autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                            autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                            outputUri = mainViewModel.shell.value.outputTreeUri,
                        )
            } },
                        isBusy = { mainViewModel.shell.value.isBusy },
                        getSession = { mainViewModel.previewSession.value.activeGenerationSession },
                        onRefresh = { rebuild -> run {
                homeRefreshActivePreviewOutputs(
                            currentSession = mainViewModel.previewSession.value.activeGenerationSession,
                            rebuildLocalCandidates = (rebuild),
                            retargetFrom = (null),
                            app = mainViewModel.previewSession.value.activeGenerationSession?.let { s -> apps.firstOrNull { it.packageName == s.packageName } },
                            currentSelections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
                            scope = previewWorkerScope,
                            getJob = { previewOutputJob },
                            setJob = { previewOutputJob = it },
                            incRevision = { ++previewOutputRevision },
                            getRevision = { previewOutputRevision },
                            setRefreshing = { mainViewModel.updatePreviewSession { v -> v.copy(isPreviewOutputRefreshing = (it)) } },
                            rebuildDebounceMs = PREVIEW_REBUILD_DEBOUNCE_MS,
                            outputDebounceMs = PREVIEW_OUTPUT_DEBOUNCE_MS,
                            tuning = run {
                    paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
                },
                            onRebuild = { session, app, tuning -> rebuildLocalSession(session, app, packageManager, tuning) },
                            defaultLocal = { auto -> run {
                    run {
                        when ((LocalSeparationMode.Auto)) {
                                    LocalSeparationMode.Original -> PreviewChoice.Original
                                    LocalSeparationMode.Plate -> PreviewChoice.Full
                                    LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                                    LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                                    LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                                    LocalSeparationMode.Auto -> ((auto))
                                    LocalSeparationMode.Full -> PreviewChoice.Full
                                }
                    }
                } },
                            normalize = { session, selections -> normalizePreviewSelections(session, selections) },
                            onWrite = { session, selections -> run {

                            val params = mainViewModel.params.value
                            writePackageOutputs(
                                session = (session),
                                selections = (selections),
                                edgePolishPercent = params.edgePolishPercent,
                                foregroundSubjectPercent = params.foregroundSubjectPercent,
                                rmbgTunedForeground = { __a0: IconCandidate -> run {

                                val params = mainViewModel.params.value
                                return@run rmbgTunedForegroundRaw(
                                    candidate = __a0,
                                    rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
                                    rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
                                    rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
                                    rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
                                )
                    } },
                                liquidGlassEnabled = params.liquidGlassEnabled,
                                liquidGlassBackgroundMistAlpha = params.liquidGlassBackgroundMistAlpha,
                                liquidGlassTopAlpha = params.liquidGlassTopAlpha,
                                liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
                                liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
                                liquidGlassOuterWidth = params.liquidGlassOuterWidth,
                                liquidGlassRadius = params.liquidGlassRadius,
                                liquidGlassSubjectScalePercent = params.liquidGlassSubjectScalePercent,
                                liquidGlassSubjectShadowAlpha = params.liquidGlassSubjectShadowAlpha,
                                liquidGlassSubjectOutlineWidth = params.liquidGlassSubjectOutlineWidth,
                                liquidGlassSubjectInnerOutlineWidth = params.liquidGlassSubjectInnerOutlineWidth,
                                liquidGlassSubjectOpacityPercent = params.liquidGlassSubjectOpacityPercent,
                                foregroundShadowLevel = params.foregroundShadowLevel,
                                monochromeThemeScale = params.monochromeThemeScale,
                                nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
                            )
                } },
                            onCommit = { session, selections ->
                                mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                                mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                                mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                                run {
                    pickerSaveUiState(
                                prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                                selectedPackage = mainViewModel.picker.value.selectedPackageName,
                                generatedFilter = mainViewModel.picker.value.generatedFilter,
                                showSystemApps = mainViewModel.picker.value.showSystemApps,
                                queryText = mainViewModel.picker.value.queryText,
                                advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                                advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                                previewPackage = mainViewModel.previewSession.value.previewPackageName,
                                previewDir = mainViewModel.previewSession.value.previewDirPath,
                                previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                                previewNormalLight = mainViewModel.params.value.previewNormalLight,
                                previewNormalDark = mainViewModel.params.value.previewNormalDark,
                                previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                                previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                                desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                                iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                                cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                                batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                                batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                                batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                                batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                                batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                                customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                                autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                                autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                                outputUri = mainViewModel.shell.value.outputTreeUri,
                            )
                }
                            },
                            onStatus = { run {

                            pickerPostStatus((it)) { runOnUiThread { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } } }
                } },
                        )
            } },
                    )
        }
                        presetStore.activePresetId = p.id
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (p.id)) }
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetBaseParams = (p.params)) }
                        mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
                    },
                    onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                )
    } },
                onActionPreview = { run {
        mainViewModel.openBatchPreviewForPreset(preset = (it), filesDir = filesDir)
    } },
                onActionOverwrite = { run {
        overwritePreset(
                    preset = (it),
                    store = presetStore,
                    current = run {
            paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
        },
                    viewModel = mainViewModel,
                    onOverwritten = { p, cur, msg ->
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (p.id)) }
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetBaseParams = (cur)) }
                        mainViewModel.updatePresetUi { it -> it.copy(presetListVersion = it.presetListVersion + (1)) }
                        mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
                    },
                    onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                )
    } },
                onActionRename = { mainViewModel.updatePresetUi { v -> v.copy(presetRenameTarget = (it)) } },
                onActionExportSingle = { run {
        exportSinglePresetToClipboard(
                    preset = (it),
                    store = presetStore,
                    clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager,
                    onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                )
    } },
                onActionDelete = { mainViewModel.updatePresetUi { v -> v.copy(presetDeleteConfirmTarget = (it)) } },
                deleteConfirmTarget = mainViewModel.presetUi.value.presetDeleteConfirmTarget,
                onDeleteDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetDeleteConfirmTarget = (null)) } },
                onDeleteConfirm = { run {
        deletePreset(
                    id = (it),
                    filesDir = filesDir,
                    store = presetStore,
                    viewModel = mainViewModel,
                    activeBatchPreviewPresetId = mainViewModel.presetUi.value.activeBatchPreviewPreset?.id,
                    batchPreviewResultPresetId = mainViewModel.presetUi.value.batchPreviewResult?.preset?.id,
                    currentPage = mainViewModel.shell.value.currentPage,
                    activePresetId = mainViewModel.presetUi.value.activePresetId,
                    onBatchPreviewReset = {
                        mainViewModel.updatePresetUi { it -> it.copy(activeBatchPreviewPreset = (null)) }
                        mainViewModel.updatePresetUi { it -> it.copy(batchPreviewResult = (null)) }
                    },
                    onNavigateHome = { mainViewModel.updateShell { it -> it.copy(currentPage = (AppPage.Home)) } },
                    onActiveCleared = {
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (null)) }
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetBaseParams = (null)) }
                    },
                    onBumpVersion = { mainViewModel.updatePresetUi { it -> it.copy(presetListVersion = it.presetListVersion + (1)) } },
                    onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                )
    } },
                importDialogVisible = mainViewModel.presetUi.value.presetImportDialogVisible,
                onImportConfirm = { text -> run {
        importPresetsFromText(
                    text = (text),
                    store = presetStore,
                    onApplied = { msg ->
                        mainViewModel.updatePresetUi { it -> it.copy(presetImportDialogVisible = (false)) }
                        mainViewModel.updatePresetUi { it -> it.copy(presetImportText = ("")) }
                        mainViewModel.updatePresetUi { it -> it.copy(presetListVersion = it.presetListVersion + (1)) }
                        mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
                    },
                )
    } },
                onImportDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetImportDialogVisible = (false)) } },
                batchPreviewConfirmTarget = mainViewModel.presetUi.value.presetBatchPreviewConfirmTarget,
                onBatchPreviewConfirm = {
                    mainViewModel.updatePresetUi { it -> it.copy(presetBatchPreviewConfirmTarget = (null)) }
                    run {
        mainViewModel.startBatchPreview(
                    preset = (it),
                    apps = apps.toList(),
                    selfPackageName = packageName,
                    batchPreviewCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                    generatedPackageNames = mainViewModel.picker.value.generatedPackageNames,
                    originalParams = run {
            paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
        },
                    cacheDir = cacheDir,
                    loadIcon = { app -> app.applicationInfo.loadIcon(packageManager) },
                    defaultChoiceForMode = { mode, auto -> run {
            when ((mode)) {
                        LocalSeparationMode.Original -> PreviewChoice.Original
                        LocalSeparationMode.Plate -> PreviewChoice.Full
                        LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                        LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                        LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                        LocalSeparationMode.Auto -> (auto)
                        LocalSeparationMode.Full -> PreviewChoice.Full
                    }
        } },
                    composeAssets = { session, selections -> run {

                    val params = mainViewModel.params.value
                    return@run previewAssetsForSelections(
                        session = (session),
                        selections = (selections),
                        edgePolishPercent = params.edgePolishPercent,
                        foregroundSubjectPercent = params.foregroundSubjectPercent,
                        rmbgTunedForeground = { __a0: IconCandidate -> run {

                        val params = mainViewModel.params.value
                        return@run rmbgTunedForegroundRaw(
                            candidate = __a0,
                            rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
                            rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
                            rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
                            rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
                        )
            } },
                        liquidGlassEnabled = params.liquidGlassEnabled,
                        liquidGlassBackgroundMistAlpha = params.liquidGlassBackgroundMistAlpha,
                        liquidGlassTopAlpha = params.liquidGlassTopAlpha,
                        liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
                        liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
                        liquidGlassOuterWidth = params.liquidGlassOuterWidth,
                        liquidGlassRadius = params.liquidGlassRadius,
                        liquidGlassSubjectScalePercent = params.liquidGlassSubjectScalePercent,
                        liquidGlassSubjectShadowAlpha = params.liquidGlassSubjectShadowAlpha,
                        liquidGlassSubjectOutlineWidth = params.liquidGlassSubjectOutlineWidth,
                        liquidGlassSubjectInnerOutlineWidth = params.liquidGlassSubjectInnerOutlineWidth,
                        liquidGlassSubjectOpacityPercent = params.liquidGlassSubjectOpacityPercent,
                        foregroundShadowLevel = params.foregroundShadowLevel,
                        nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
                    )
        } },
                    onApplyMerged = { merged ->
                        run {
            paramsApplyTuningParams(
                        params = (merged),
                        rebuildCandidates = (false),
                        persist = (false),
                        captureUndo = (false),
                        refreshPreview = (false),
                        getBefore = { run {
                paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
            } },
                        onCaptureUndo = { mainViewModel.updatePreviewSession { v -> v.copy(lastParamsSnapshot = (it)) } },
                        onParamsApplied = { before, applied, capture -> mainViewModel.onParamsApplied(before = before, applied = applied, captureUndo = capture) },
                        setDraftForegroundSubjectPercentText = { draftForegroundSubjectPercentText = it },
                        setDraftForegroundShadowLevelText = { draftForegroundShadowLevelText = it },
                        setDraftMonochromeThemeScaleText = { draftMonochromeThemeScaleText = it },
                        setDraftBackgroundSeparationText = { draftBackgroundSeparationText = it },
                        setDraftPlateRemovalText = { draftPlateRemovalText = it },
                        setDraftShadowRemovalText = { draftShadowRemovalText = it },
                        setDraftEdgePolishText = { draftEdgePolishText = it },
                        setDraftRmbgAlphaStrengthText = { draftRmbgAlphaStrengthText = it },
                        setDraftRmbgEdgeFeatherText = { draftRmbgEdgeFeatherText = it },
                        setDraftRmbgEdgeAdjustText = { draftRmbgEdgeAdjustText = it },
                        setDraftRmbgWeakAlphaKeepText = { draftRmbgWeakAlphaKeepText = it },
                        setDraftLiquidGlassRadiusText = { draftLiquidGlassRadiusText = it },
                        setDraftLiquidGlassOuterWidthText = { draftLiquidGlassOuterWidthText = it },
                        setDraftLiquidGlassTopAlphaText = { draftLiquidGlassTopAlphaText = it },
                        setDraftLiquidGlassBottomAlphaText = { draftLiquidGlassBottomAlphaText = it },
                        setDraftLiquidGlassBackgroundMistAlphaText = { draftLiquidGlassBackgroundMistAlphaText = it },
                        setDraftLiquidGlassBottomDarkAlphaText = { draftLiquidGlassBottomDarkAlphaText = it },
                        setDraftLiquidGlassSubjectScaleText = { draftLiquidGlassSubjectScaleText = it },
                        setDraftLiquidGlassSubjectOutlineWidthText = { draftLiquidGlassSubjectOutlineWidthText = it },
                        setDraftLiquidGlassSubjectInnerOutlineWidthText = { draftLiquidGlassSubjectInnerOutlineWidthText = it },
                        setDraftLiquidGlassSubjectShadowAlphaText = { draftLiquidGlassSubjectShadowAlphaText = it },
                        setDraftLiquidGlassSubjectOpacityText = { draftLiquidGlassSubjectOpacityText = it },
                        setDraftJsonParamsText = { draftJsonParamsText = it },
                        onSaveLocalSeparation = { run {
                paramsSaveLocalSeparationSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                        )
            } },
                        onSaveImageTuning = { run {
                paramsSaveImageTuningSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                        )
            } },
                        onSaveLiquidGlass = { run {
                paramsSaveLiquidGlassSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                            getBottomBarEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarEnabled },
                            getBottomBarBlurEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarBlurEnabled },
                        )
            } },
                        onSaveGpt = { run {
                paramsSaveGptSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                            getGptApiKey = { mainViewModel.gptRmbgSettings.value.gptApiKey },
                            getGptModelId = { mainViewModel.gptRmbgSettings.value.gptModelId },
                            getGptBaseUrl = { mainViewModel.gptRmbgSettings.value.gptBaseUrl },
                        )
            } },
                        onSaveUi = { run {
                pickerSaveUiState(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            selectedPackage = mainViewModel.picker.value.selectedPackageName,
                            generatedFilter = mainViewModel.picker.value.generatedFilter,
                            showSystemApps = mainViewModel.picker.value.showSystemApps,
                            queryText = mainViewModel.picker.value.queryText,
                            advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                            advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                            previewPackage = mainViewModel.previewSession.value.previewPackageName,
                            previewDir = mainViewModel.previewSession.value.previewDirPath,
                            previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                            previewNormalLight = mainViewModel.params.value.previewNormalLight,
                            previewNormalDark = mainViewModel.params.value.previewNormalDark,
                            previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                            previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                            desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                            iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                            cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                            batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                            batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                            batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                            batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                            batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                            customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                            autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                            autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                            outputUri = mainViewModel.shell.value.outputTreeUri,
                        )
            } },
                        isBusy = { mainViewModel.shell.value.isBusy },
                        getSession = { mainViewModel.previewSession.value.activeGenerationSession },
                        onRefresh = { rebuild -> run {
                homeRefreshActivePreviewOutputs(
                            currentSession = mainViewModel.previewSession.value.activeGenerationSession,
                            rebuildLocalCandidates = (rebuild),
                            retargetFrom = (null),
                            app = mainViewModel.previewSession.value.activeGenerationSession?.let { s -> apps.firstOrNull { it.packageName == s.packageName } },
                            currentSelections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
                            scope = previewWorkerScope,
                            getJob = { previewOutputJob },
                            setJob = { previewOutputJob = it },
                            incRevision = { ++previewOutputRevision },
                            getRevision = { previewOutputRevision },
                            setRefreshing = { mainViewModel.updatePreviewSession { v -> v.copy(isPreviewOutputRefreshing = (it)) } },
                            rebuildDebounceMs = PREVIEW_REBUILD_DEBOUNCE_MS,
                            outputDebounceMs = PREVIEW_OUTPUT_DEBOUNCE_MS,
                            tuning = run {
                    paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
                },
                            onRebuild = { session, app, tuning -> rebuildLocalSession(session, app, packageManager, tuning) },
                            defaultLocal = { auto -> run {
                    run {
                        when ((LocalSeparationMode.Auto)) {
                                    LocalSeparationMode.Original -> PreviewChoice.Original
                                    LocalSeparationMode.Plate -> PreviewChoice.Full
                                    LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                                    LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                                    LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                                    LocalSeparationMode.Auto -> ((auto))
                                    LocalSeparationMode.Full -> PreviewChoice.Full
                                }
                    }
                } },
                            normalize = { session, selections -> normalizePreviewSelections(session, selections) },
                            onWrite = { session, selections -> run {

                            val params = mainViewModel.params.value
                            writePackageOutputs(
                                session = (session),
                                selections = (selections),
                                edgePolishPercent = params.edgePolishPercent,
                                foregroundSubjectPercent = params.foregroundSubjectPercent,
                                rmbgTunedForeground = { __a0: IconCandidate -> run {

                                val params = mainViewModel.params.value
                                return@run rmbgTunedForegroundRaw(
                                    candidate = __a0,
                                    rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
                                    rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
                                    rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
                                    rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
                                )
                    } },
                                liquidGlassEnabled = params.liquidGlassEnabled,
                                liquidGlassBackgroundMistAlpha = params.liquidGlassBackgroundMistAlpha,
                                liquidGlassTopAlpha = params.liquidGlassTopAlpha,
                                liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
                                liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
                                liquidGlassOuterWidth = params.liquidGlassOuterWidth,
                                liquidGlassRadius = params.liquidGlassRadius,
                                liquidGlassSubjectScalePercent = params.liquidGlassSubjectScalePercent,
                                liquidGlassSubjectShadowAlpha = params.liquidGlassSubjectShadowAlpha,
                                liquidGlassSubjectOutlineWidth = params.liquidGlassSubjectOutlineWidth,
                                liquidGlassSubjectInnerOutlineWidth = params.liquidGlassSubjectInnerOutlineWidth,
                                liquidGlassSubjectOpacityPercent = params.liquidGlassSubjectOpacityPercent,
                                foregroundShadowLevel = params.foregroundShadowLevel,
                                monochromeThemeScale = params.monochromeThemeScale,
                                nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
                            )
                } },
                            onCommit = { session, selections ->
                                mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                                mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                                mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                                run {
                    pickerSaveUiState(
                                prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                                selectedPackage = mainViewModel.picker.value.selectedPackageName,
                                generatedFilter = mainViewModel.picker.value.generatedFilter,
                                showSystemApps = mainViewModel.picker.value.showSystemApps,
                                queryText = mainViewModel.picker.value.queryText,
                                advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                                advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                                previewPackage = mainViewModel.previewSession.value.previewPackageName,
                                previewDir = mainViewModel.previewSession.value.previewDirPath,
                                previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                                previewNormalLight = mainViewModel.params.value.previewNormalLight,
                                previewNormalDark = mainViewModel.params.value.previewNormalDark,
                                previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                                previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                                desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                                iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                                cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                                batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                                batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                                batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                                batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                                batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                                customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                                autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                                autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                                outputUri = mainViewModel.shell.value.outputTreeUri,
                            )
                }
                            },
                            onStatus = { run {

                            pickerPostStatus((it)) { runOnUiThread { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } } }
                } },
                        )
            } },
                    )
        }
                    },
                    onRestoreOriginal = { original ->
                        run {
            paramsApplyTuningParams(
                        params = (original),
                        rebuildCandidates = (true),
                        persist = (false),
                        captureUndo = (false),
                        refreshPreview = (true),
                        getBefore = { run {
                paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
            } },
                        onCaptureUndo = { mainViewModel.updatePreviewSession { v -> v.copy(lastParamsSnapshot = (it)) } },
                        onParamsApplied = { before, applied, capture -> mainViewModel.onParamsApplied(before = before, applied = applied, captureUndo = capture) },
                        setDraftForegroundSubjectPercentText = { draftForegroundSubjectPercentText = it },
                        setDraftForegroundShadowLevelText = { draftForegroundShadowLevelText = it },
                        setDraftMonochromeThemeScaleText = { draftMonochromeThemeScaleText = it },
                        setDraftBackgroundSeparationText = { draftBackgroundSeparationText = it },
                        setDraftPlateRemovalText = { draftPlateRemovalText = it },
                        setDraftShadowRemovalText = { draftShadowRemovalText = it },
                        setDraftEdgePolishText = { draftEdgePolishText = it },
                        setDraftRmbgAlphaStrengthText = { draftRmbgAlphaStrengthText = it },
                        setDraftRmbgEdgeFeatherText = { draftRmbgEdgeFeatherText = it },
                        setDraftRmbgEdgeAdjustText = { draftRmbgEdgeAdjustText = it },
                        setDraftRmbgWeakAlphaKeepText = { draftRmbgWeakAlphaKeepText = it },
                        setDraftLiquidGlassRadiusText = { draftLiquidGlassRadiusText = it },
                        setDraftLiquidGlassOuterWidthText = { draftLiquidGlassOuterWidthText = it },
                        setDraftLiquidGlassTopAlphaText = { draftLiquidGlassTopAlphaText = it },
                        setDraftLiquidGlassBottomAlphaText = { draftLiquidGlassBottomAlphaText = it },
                        setDraftLiquidGlassBackgroundMistAlphaText = { draftLiquidGlassBackgroundMistAlphaText = it },
                        setDraftLiquidGlassBottomDarkAlphaText = { draftLiquidGlassBottomDarkAlphaText = it },
                        setDraftLiquidGlassSubjectScaleText = { draftLiquidGlassSubjectScaleText = it },
                        setDraftLiquidGlassSubjectOutlineWidthText = { draftLiquidGlassSubjectOutlineWidthText = it },
                        setDraftLiquidGlassSubjectInnerOutlineWidthText = { draftLiquidGlassSubjectInnerOutlineWidthText = it },
                        setDraftLiquidGlassSubjectShadowAlphaText = { draftLiquidGlassSubjectShadowAlphaText = it },
                        setDraftLiquidGlassSubjectOpacityText = { draftLiquidGlassSubjectOpacityText = it },
                        setDraftJsonParamsText = { draftJsonParamsText = it },
                        onSaveLocalSeparation = { run {
                paramsSaveLocalSeparationSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                        )
            } },
                        onSaveImageTuning = { run {
                paramsSaveImageTuningSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                        )
            } },
                        onSaveLiquidGlass = { run {
                paramsSaveLiquidGlassSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                            getBottomBarEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarEnabled },
                            getBottomBarBlurEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarBlurEnabled },
                        )
            } },
                        onSaveGpt = { run {
                paramsSaveGptSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                            getGptApiKey = { mainViewModel.gptRmbgSettings.value.gptApiKey },
                            getGptModelId = { mainViewModel.gptRmbgSettings.value.gptModelId },
                            getGptBaseUrl = { mainViewModel.gptRmbgSettings.value.gptBaseUrl },
                        )
            } },
                        onSaveUi = { run {
                pickerSaveUiState(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            selectedPackage = mainViewModel.picker.value.selectedPackageName,
                            generatedFilter = mainViewModel.picker.value.generatedFilter,
                            showSystemApps = mainViewModel.picker.value.showSystemApps,
                            queryText = mainViewModel.picker.value.queryText,
                            advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                            advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                            previewPackage = mainViewModel.previewSession.value.previewPackageName,
                            previewDir = mainViewModel.previewSession.value.previewDirPath,
                            previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                            previewNormalLight = mainViewModel.params.value.previewNormalLight,
                            previewNormalDark = mainViewModel.params.value.previewNormalDark,
                            previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                            previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                            desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                            iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                            cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                            batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                            batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                            batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                            batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                            batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                            customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                            autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                            autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                            outputUri = mainViewModel.shell.value.outputTreeUri,
                        )
            } },
                        isBusy = { mainViewModel.shell.value.isBusy },
                        getSession = { mainViewModel.previewSession.value.activeGenerationSession },
                        onRefresh = { rebuild -> run {
                homeRefreshActivePreviewOutputs(
                            currentSession = mainViewModel.previewSession.value.activeGenerationSession,
                            rebuildLocalCandidates = (rebuild),
                            retargetFrom = (null),
                            app = mainViewModel.previewSession.value.activeGenerationSession?.let { s -> apps.firstOrNull { it.packageName == s.packageName } },
                            currentSelections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
                            scope = previewWorkerScope,
                            getJob = { previewOutputJob },
                            setJob = { previewOutputJob = it },
                            incRevision = { ++previewOutputRevision },
                            getRevision = { previewOutputRevision },
                            setRefreshing = { mainViewModel.updatePreviewSession { v -> v.copy(isPreviewOutputRefreshing = (it)) } },
                            rebuildDebounceMs = PREVIEW_REBUILD_DEBOUNCE_MS,
                            outputDebounceMs = PREVIEW_OUTPUT_DEBOUNCE_MS,
                            tuning = run {
                    paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
                },
                            onRebuild = { session, app, tuning -> rebuildLocalSession(session, app, packageManager, tuning) },
                            defaultLocal = { auto -> run {
                    run {
                        when ((LocalSeparationMode.Auto)) {
                                    LocalSeparationMode.Original -> PreviewChoice.Original
                                    LocalSeparationMode.Plate -> PreviewChoice.Full
                                    LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                                    LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                                    LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                                    LocalSeparationMode.Auto -> ((auto))
                                    LocalSeparationMode.Full -> PreviewChoice.Full
                                }
                    }
                } },
                            normalize = { session, selections -> normalizePreviewSelections(session, selections) },
                            onWrite = { session, selections -> run {

                            val params = mainViewModel.params.value
                            writePackageOutputs(
                                session = (session),
                                selections = (selections),
                                edgePolishPercent = params.edgePolishPercent,
                                foregroundSubjectPercent = params.foregroundSubjectPercent,
                                rmbgTunedForeground = { __a0: IconCandidate -> run {

                                val params = mainViewModel.params.value
                                return@run rmbgTunedForegroundRaw(
                                    candidate = __a0,
                                    rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
                                    rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
                                    rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
                                    rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
                                )
                    } },
                                liquidGlassEnabled = params.liquidGlassEnabled,
                                liquidGlassBackgroundMistAlpha = params.liquidGlassBackgroundMistAlpha,
                                liquidGlassTopAlpha = params.liquidGlassTopAlpha,
                                liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
                                liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
                                liquidGlassOuterWidth = params.liquidGlassOuterWidth,
                                liquidGlassRadius = params.liquidGlassRadius,
                                liquidGlassSubjectScalePercent = params.liquidGlassSubjectScalePercent,
                                liquidGlassSubjectShadowAlpha = params.liquidGlassSubjectShadowAlpha,
                                liquidGlassSubjectOutlineWidth = params.liquidGlassSubjectOutlineWidth,
                                liquidGlassSubjectInnerOutlineWidth = params.liquidGlassSubjectInnerOutlineWidth,
                                liquidGlassSubjectOpacityPercent = params.liquidGlassSubjectOpacityPercent,
                                foregroundShadowLevel = params.foregroundShadowLevel,
                                monochromeThemeScale = params.monochromeThemeScale,
                                nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
                            )
                } },
                            onCommit = { session, selections ->
                                mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                                mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                                mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                                run {
                    pickerSaveUiState(
                                prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                                selectedPackage = mainViewModel.picker.value.selectedPackageName,
                                generatedFilter = mainViewModel.picker.value.generatedFilter,
                                showSystemApps = mainViewModel.picker.value.showSystemApps,
                                queryText = mainViewModel.picker.value.queryText,
                                advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                                advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                                previewPackage = mainViewModel.previewSession.value.previewPackageName,
                                previewDir = mainViewModel.previewSession.value.previewDirPath,
                                previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                                previewNormalLight = mainViewModel.params.value.previewNormalLight,
                                previewNormalDark = mainViewModel.params.value.previewNormalDark,
                                previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                                previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                                desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                                iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                                cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                                batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                                batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                                batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                                batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                                batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                                customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                                autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                                autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                                outputUri = mainViewModel.shell.value.outputTreeUri,
                            )
                }
                            },
                            onStatus = { run {

                            pickerPostStatus((it)) { runOnUiThread { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } } }
                } },
                        )
            } },
                    )
        }
                    },
                    onSaveSnapshot = { p, dataList -> BatchPreviewStore.saveSnapshot(filesDir, p, dataList) },
                )
    }
                },
                onBatchPreviewConfirmDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetBatchPreviewConfirmTarget = (null)) } },
                batchPreviewProgress = mainViewModel.presetUi.value.batchPreviewProgress,
                onCancelBatchPreview = { mainViewModel.updatePresetUi { it -> it.copy(batchPreviewCancelled = (true)) } },
                showRefreshConfirm = mainViewModel.presetUi.value.showBatchPreviewRefreshConfirm,
                refreshConfirmPreset = mainViewModel.presetUi.value.activeBatchPreviewPreset ?: mainViewModel.presetUi.value.batchPreviewResult?.preset,
                batchPreviewCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                onRefreshConfirm = {
                    mainViewModel.updatePresetUi { it -> it.copy(showBatchPreviewRefreshConfirm = (false)) }
                    run {
        mainViewModel.startBatchPreview(
                    preset = (it),
                    apps = apps.toList(),
                    selfPackageName = packageName,
                    batchPreviewCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                    generatedPackageNames = mainViewModel.picker.value.generatedPackageNames,
                    originalParams = run {
            paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
        },
                    cacheDir = cacheDir,
                    loadIcon = { app -> app.applicationInfo.loadIcon(packageManager) },
                    defaultChoiceForMode = { mode, auto -> run {
            when ((mode)) {
                        LocalSeparationMode.Original -> PreviewChoice.Original
                        LocalSeparationMode.Plate -> PreviewChoice.Full
                        LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                        LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                        LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                        LocalSeparationMode.Auto -> (auto)
                        LocalSeparationMode.Full -> PreviewChoice.Full
                    }
        } },
                    composeAssets = { session, selections -> run {

                    val params = mainViewModel.params.value
                    return@run previewAssetsForSelections(
                        session = (session),
                        selections = (selections),
                        edgePolishPercent = params.edgePolishPercent,
                        foregroundSubjectPercent = params.foregroundSubjectPercent,
                        rmbgTunedForeground = { __a0: IconCandidate -> run {

                        val params = mainViewModel.params.value
                        return@run rmbgTunedForegroundRaw(
                            candidate = __a0,
                            rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
                            rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
                            rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
                            rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
                        )
            } },
                        liquidGlassEnabled = params.liquidGlassEnabled,
                        liquidGlassBackgroundMistAlpha = params.liquidGlassBackgroundMistAlpha,
                        liquidGlassTopAlpha = params.liquidGlassTopAlpha,
                        liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
                        liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
                        liquidGlassOuterWidth = params.liquidGlassOuterWidth,
                        liquidGlassRadius = params.liquidGlassRadius,
                        liquidGlassSubjectScalePercent = params.liquidGlassSubjectScalePercent,
                        liquidGlassSubjectShadowAlpha = params.liquidGlassSubjectShadowAlpha,
                        liquidGlassSubjectOutlineWidth = params.liquidGlassSubjectOutlineWidth,
                        liquidGlassSubjectInnerOutlineWidth = params.liquidGlassSubjectInnerOutlineWidth,
                        liquidGlassSubjectOpacityPercent = params.liquidGlassSubjectOpacityPercent,
                        foregroundShadowLevel = params.foregroundShadowLevel,
                        nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
                    )
        } },
                    onApplyMerged = { merged ->
                        run {
            paramsApplyTuningParams(
                        params = (merged),
                        rebuildCandidates = (false),
                        persist = (false),
                        captureUndo = (false),
                        refreshPreview = (false),
                        getBefore = { run {
                paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
            } },
                        onCaptureUndo = { mainViewModel.updatePreviewSession { v -> v.copy(lastParamsSnapshot = (it)) } },
                        onParamsApplied = { before, applied, capture -> mainViewModel.onParamsApplied(before = before, applied = applied, captureUndo = capture) },
                        setDraftForegroundSubjectPercentText = { draftForegroundSubjectPercentText = it },
                        setDraftForegroundShadowLevelText = { draftForegroundShadowLevelText = it },
                        setDraftMonochromeThemeScaleText = { draftMonochromeThemeScaleText = it },
                        setDraftBackgroundSeparationText = { draftBackgroundSeparationText = it },
                        setDraftPlateRemovalText = { draftPlateRemovalText = it },
                        setDraftShadowRemovalText = { draftShadowRemovalText = it },
                        setDraftEdgePolishText = { draftEdgePolishText = it },
                        setDraftRmbgAlphaStrengthText = { draftRmbgAlphaStrengthText = it },
                        setDraftRmbgEdgeFeatherText = { draftRmbgEdgeFeatherText = it },
                        setDraftRmbgEdgeAdjustText = { draftRmbgEdgeAdjustText = it },
                        setDraftRmbgWeakAlphaKeepText = { draftRmbgWeakAlphaKeepText = it },
                        setDraftLiquidGlassRadiusText = { draftLiquidGlassRadiusText = it },
                        setDraftLiquidGlassOuterWidthText = { draftLiquidGlassOuterWidthText = it },
                        setDraftLiquidGlassTopAlphaText = { draftLiquidGlassTopAlphaText = it },
                        setDraftLiquidGlassBottomAlphaText = { draftLiquidGlassBottomAlphaText = it },
                        setDraftLiquidGlassBackgroundMistAlphaText = { draftLiquidGlassBackgroundMistAlphaText = it },
                        setDraftLiquidGlassBottomDarkAlphaText = { draftLiquidGlassBottomDarkAlphaText = it },
                        setDraftLiquidGlassSubjectScaleText = { draftLiquidGlassSubjectScaleText = it },
                        setDraftLiquidGlassSubjectOutlineWidthText = { draftLiquidGlassSubjectOutlineWidthText = it },
                        setDraftLiquidGlassSubjectInnerOutlineWidthText = { draftLiquidGlassSubjectInnerOutlineWidthText = it },
                        setDraftLiquidGlassSubjectShadowAlphaText = { draftLiquidGlassSubjectShadowAlphaText = it },
                        setDraftLiquidGlassSubjectOpacityText = { draftLiquidGlassSubjectOpacityText = it },
                        setDraftJsonParamsText = { draftJsonParamsText = it },
                        onSaveLocalSeparation = { run {
                paramsSaveLocalSeparationSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                        )
            } },
                        onSaveImageTuning = { run {
                paramsSaveImageTuningSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                        )
            } },
                        onSaveLiquidGlass = { run {
                paramsSaveLiquidGlassSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                            getBottomBarEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarEnabled },
                            getBottomBarBlurEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarBlurEnabled },
                        )
            } },
                        onSaveGpt = { run {
                paramsSaveGptSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                            getGptApiKey = { mainViewModel.gptRmbgSettings.value.gptApiKey },
                            getGptModelId = { mainViewModel.gptRmbgSettings.value.gptModelId },
                            getGptBaseUrl = { mainViewModel.gptRmbgSettings.value.gptBaseUrl },
                        )
            } },
                        onSaveUi = { run {
                pickerSaveUiState(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            selectedPackage = mainViewModel.picker.value.selectedPackageName,
                            generatedFilter = mainViewModel.picker.value.generatedFilter,
                            showSystemApps = mainViewModel.picker.value.showSystemApps,
                            queryText = mainViewModel.picker.value.queryText,
                            advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                            advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                            previewPackage = mainViewModel.previewSession.value.previewPackageName,
                            previewDir = mainViewModel.previewSession.value.previewDirPath,
                            previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                            previewNormalLight = mainViewModel.params.value.previewNormalLight,
                            previewNormalDark = mainViewModel.params.value.previewNormalDark,
                            previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                            previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                            desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                            iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                            cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                            batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                            batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                            batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                            batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                            batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                            customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                            autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                            autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                            outputUri = mainViewModel.shell.value.outputTreeUri,
                        )
            } },
                        isBusy = { mainViewModel.shell.value.isBusy },
                        getSession = { mainViewModel.previewSession.value.activeGenerationSession },
                        onRefresh = { rebuild -> run {
                homeRefreshActivePreviewOutputs(
                            currentSession = mainViewModel.previewSession.value.activeGenerationSession,
                            rebuildLocalCandidates = (rebuild),
                            retargetFrom = (null),
                            app = mainViewModel.previewSession.value.activeGenerationSession?.let { s -> apps.firstOrNull { it.packageName == s.packageName } },
                            currentSelections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
                            scope = previewWorkerScope,
                            getJob = { previewOutputJob },
                            setJob = { previewOutputJob = it },
                            incRevision = { ++previewOutputRevision },
                            getRevision = { previewOutputRevision },
                            setRefreshing = { mainViewModel.updatePreviewSession { v -> v.copy(isPreviewOutputRefreshing = (it)) } },
                            rebuildDebounceMs = PREVIEW_REBUILD_DEBOUNCE_MS,
                            outputDebounceMs = PREVIEW_OUTPUT_DEBOUNCE_MS,
                            tuning = run {
                    paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
                },
                            onRebuild = { session, app, tuning -> rebuildLocalSession(session, app, packageManager, tuning) },
                            defaultLocal = { auto -> run {
                    run {
                        when ((LocalSeparationMode.Auto)) {
                                    LocalSeparationMode.Original -> PreviewChoice.Original
                                    LocalSeparationMode.Plate -> PreviewChoice.Full
                                    LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                                    LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                                    LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                                    LocalSeparationMode.Auto -> ((auto))
                                    LocalSeparationMode.Full -> PreviewChoice.Full
                                }
                    }
                } },
                            normalize = { session, selections -> normalizePreviewSelections(session, selections) },
                            onWrite = { session, selections -> run {

                            val params = mainViewModel.params.value
                            writePackageOutputs(
                                session = (session),
                                selections = (selections),
                                edgePolishPercent = params.edgePolishPercent,
                                foregroundSubjectPercent = params.foregroundSubjectPercent,
                                rmbgTunedForeground = { __a0: IconCandidate -> run {

                                val params = mainViewModel.params.value
                                return@run rmbgTunedForegroundRaw(
                                    candidate = __a0,
                                    rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
                                    rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
                                    rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
                                    rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
                                )
                    } },
                                liquidGlassEnabled = params.liquidGlassEnabled,
                                liquidGlassBackgroundMistAlpha = params.liquidGlassBackgroundMistAlpha,
                                liquidGlassTopAlpha = params.liquidGlassTopAlpha,
                                liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
                                liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
                                liquidGlassOuterWidth = params.liquidGlassOuterWidth,
                                liquidGlassRadius = params.liquidGlassRadius,
                                liquidGlassSubjectScalePercent = params.liquidGlassSubjectScalePercent,
                                liquidGlassSubjectShadowAlpha = params.liquidGlassSubjectShadowAlpha,
                                liquidGlassSubjectOutlineWidth = params.liquidGlassSubjectOutlineWidth,
                                liquidGlassSubjectInnerOutlineWidth = params.liquidGlassSubjectInnerOutlineWidth,
                                liquidGlassSubjectOpacityPercent = params.liquidGlassSubjectOpacityPercent,
                                foregroundShadowLevel = params.foregroundShadowLevel,
                                monochromeThemeScale = params.monochromeThemeScale,
                                nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
                            )
                } },
                            onCommit = { session, selections ->
                                mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                                mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                                mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                                run {
                    pickerSaveUiState(
                                prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                                selectedPackage = mainViewModel.picker.value.selectedPackageName,
                                generatedFilter = mainViewModel.picker.value.generatedFilter,
                                showSystemApps = mainViewModel.picker.value.showSystemApps,
                                queryText = mainViewModel.picker.value.queryText,
                                advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                                advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                                previewPackage = mainViewModel.previewSession.value.previewPackageName,
                                previewDir = mainViewModel.previewSession.value.previewDirPath,
                                previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                                previewNormalLight = mainViewModel.params.value.previewNormalLight,
                                previewNormalDark = mainViewModel.params.value.previewNormalDark,
                                previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                                previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                                desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                                iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                                cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                                batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                                batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                                batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                                batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                                batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                                customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                                autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                                autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                                outputUri = mainViewModel.shell.value.outputTreeUri,
                            )
                }
                            },
                            onStatus = { run {

                            pickerPostStatus((it)) { runOnUiThread { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } } }
                } },
                        )
            } },
                    )
        }
                    },
                    onRestoreOriginal = { original ->
                        run {
            paramsApplyTuningParams(
                        params = (original),
                        rebuildCandidates = (true),
                        persist = (false),
                        captureUndo = (false),
                        refreshPreview = (true),
                        getBefore = { run {
                paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
            } },
                        onCaptureUndo = { mainViewModel.updatePreviewSession { v -> v.copy(lastParamsSnapshot = (it)) } },
                        onParamsApplied = { before, applied, capture -> mainViewModel.onParamsApplied(before = before, applied = applied, captureUndo = capture) },
                        setDraftForegroundSubjectPercentText = { draftForegroundSubjectPercentText = it },
                        setDraftForegroundShadowLevelText = { draftForegroundShadowLevelText = it },
                        setDraftMonochromeThemeScaleText = { draftMonochromeThemeScaleText = it },
                        setDraftBackgroundSeparationText = { draftBackgroundSeparationText = it },
                        setDraftPlateRemovalText = { draftPlateRemovalText = it },
                        setDraftShadowRemovalText = { draftShadowRemovalText = it },
                        setDraftEdgePolishText = { draftEdgePolishText = it },
                        setDraftRmbgAlphaStrengthText = { draftRmbgAlphaStrengthText = it },
                        setDraftRmbgEdgeFeatherText = { draftRmbgEdgeFeatherText = it },
                        setDraftRmbgEdgeAdjustText = { draftRmbgEdgeAdjustText = it },
                        setDraftRmbgWeakAlphaKeepText = { draftRmbgWeakAlphaKeepText = it },
                        setDraftLiquidGlassRadiusText = { draftLiquidGlassRadiusText = it },
                        setDraftLiquidGlassOuterWidthText = { draftLiquidGlassOuterWidthText = it },
                        setDraftLiquidGlassTopAlphaText = { draftLiquidGlassTopAlphaText = it },
                        setDraftLiquidGlassBottomAlphaText = { draftLiquidGlassBottomAlphaText = it },
                        setDraftLiquidGlassBackgroundMistAlphaText = { draftLiquidGlassBackgroundMistAlphaText = it },
                        setDraftLiquidGlassBottomDarkAlphaText = { draftLiquidGlassBottomDarkAlphaText = it },
                        setDraftLiquidGlassSubjectScaleText = { draftLiquidGlassSubjectScaleText = it },
                        setDraftLiquidGlassSubjectOutlineWidthText = { draftLiquidGlassSubjectOutlineWidthText = it },
                        setDraftLiquidGlassSubjectInnerOutlineWidthText = { draftLiquidGlassSubjectInnerOutlineWidthText = it },
                        setDraftLiquidGlassSubjectShadowAlphaText = { draftLiquidGlassSubjectShadowAlphaText = it },
                        setDraftLiquidGlassSubjectOpacityText = { draftLiquidGlassSubjectOpacityText = it },
                        setDraftJsonParamsText = { draftJsonParamsText = it },
                        onSaveLocalSeparation = { run {
                paramsSaveLocalSeparationSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                        )
            } },
                        onSaveImageTuning = { run {
                paramsSaveImageTuningSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                        )
            } },
                        onSaveLiquidGlass = { run {
                paramsSaveLiquidGlassSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                            getBottomBarEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarEnabled },
                            getBottomBarBlurEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarBlurEnabled },
                        )
            } },
                        onSaveGpt = { run {
                paramsSaveGptSettings(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            getParams = { mainViewModel.params.value },
                            getGptApiKey = { mainViewModel.gptRmbgSettings.value.gptApiKey },
                            getGptModelId = { mainViewModel.gptRmbgSettings.value.gptModelId },
                            getGptBaseUrl = { mainViewModel.gptRmbgSettings.value.gptBaseUrl },
                        )
            } },
                        onSaveUi = { run {
                pickerSaveUiState(
                            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                            selectedPackage = mainViewModel.picker.value.selectedPackageName,
                            generatedFilter = mainViewModel.picker.value.generatedFilter,
                            showSystemApps = mainViewModel.picker.value.showSystemApps,
                            queryText = mainViewModel.picker.value.queryText,
                            advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                            advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                            previewPackage = mainViewModel.previewSession.value.previewPackageName,
                            previewDir = mainViewModel.previewSession.value.previewDirPath,
                            previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                            previewNormalLight = mainViewModel.params.value.previewNormalLight,
                            previewNormalDark = mainViewModel.params.value.previewNormalDark,
                            previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                            previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                            desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                            iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                            cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                            batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                            batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                            batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                            batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                            batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                            customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                            autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                            autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                            outputUri = mainViewModel.shell.value.outputTreeUri,
                        )
            } },
                        isBusy = { mainViewModel.shell.value.isBusy },
                        getSession = { mainViewModel.previewSession.value.activeGenerationSession },
                        onRefresh = { rebuild -> run {
                homeRefreshActivePreviewOutputs(
                            currentSession = mainViewModel.previewSession.value.activeGenerationSession,
                            rebuildLocalCandidates = (rebuild),
                            retargetFrom = (null),
                            app = mainViewModel.previewSession.value.activeGenerationSession?.let { s -> apps.firstOrNull { it.packageName == s.packageName } },
                            currentSelections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
                            scope = previewWorkerScope,
                            getJob = { previewOutputJob },
                            setJob = { previewOutputJob = it },
                            incRevision = { ++previewOutputRevision },
                            getRevision = { previewOutputRevision },
                            setRefreshing = { mainViewModel.updatePreviewSession { v -> v.copy(isPreviewOutputRefreshing = (it)) } },
                            rebuildDebounceMs = PREVIEW_REBUILD_DEBOUNCE_MS,
                            outputDebounceMs = PREVIEW_OUTPUT_DEBOUNCE_MS,
                            tuning = run {
                    paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
                },
                            onRebuild = { session, app, tuning -> rebuildLocalSession(session, app, packageManager, tuning) },
                            defaultLocal = { auto -> run {
                    run {
                        when ((LocalSeparationMode.Auto)) {
                                    LocalSeparationMode.Original -> PreviewChoice.Original
                                    LocalSeparationMode.Plate -> PreviewChoice.Full
                                    LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                                    LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                                    LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                                    LocalSeparationMode.Auto -> ((auto))
                                    LocalSeparationMode.Full -> PreviewChoice.Full
                                }
                    }
                } },
                            normalize = { session, selections -> normalizePreviewSelections(session, selections) },
                            onWrite = { session, selections -> run {

                            val params = mainViewModel.params.value
                            writePackageOutputs(
                                session = (session),
                                selections = (selections),
                                edgePolishPercent = params.edgePolishPercent,
                                foregroundSubjectPercent = params.foregroundSubjectPercent,
                                rmbgTunedForeground = { __a0: IconCandidate -> run {

                                val params = mainViewModel.params.value
                                return@run rmbgTunedForegroundRaw(
                                    candidate = __a0,
                                    rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
                                    rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
                                    rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
                                    rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
                                )
                    } },
                                liquidGlassEnabled = params.liquidGlassEnabled,
                                liquidGlassBackgroundMistAlpha = params.liquidGlassBackgroundMistAlpha,
                                liquidGlassTopAlpha = params.liquidGlassTopAlpha,
                                liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
                                liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
                                liquidGlassOuterWidth = params.liquidGlassOuterWidth,
                                liquidGlassRadius = params.liquidGlassRadius,
                                liquidGlassSubjectScalePercent = params.liquidGlassSubjectScalePercent,
                                liquidGlassSubjectShadowAlpha = params.liquidGlassSubjectShadowAlpha,
                                liquidGlassSubjectOutlineWidth = params.liquidGlassSubjectOutlineWidth,
                                liquidGlassSubjectInnerOutlineWidth = params.liquidGlassSubjectInnerOutlineWidth,
                                liquidGlassSubjectOpacityPercent = params.liquidGlassSubjectOpacityPercent,
                                foregroundShadowLevel = params.foregroundShadowLevel,
                                monochromeThemeScale = params.monochromeThemeScale,
                                nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
                            )
                } },
                            onCommit = { session, selections ->
                                mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                                mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                                mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                                run {
                    pickerSaveUiState(
                                prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                                selectedPackage = mainViewModel.picker.value.selectedPackageName,
                                generatedFilter = mainViewModel.picker.value.generatedFilter,
                                showSystemApps = mainViewModel.picker.value.showSystemApps,
                                queryText = mainViewModel.picker.value.queryText,
                                advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                                advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                                previewPackage = mainViewModel.previewSession.value.previewPackageName,
                                previewDir = mainViewModel.previewSession.value.previewDirPath,
                                previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                                previewNormalLight = mainViewModel.params.value.previewNormalLight,
                                previewNormalDark = mainViewModel.params.value.previewNormalDark,
                                previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                                previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                                desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                                iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                                cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                                batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                                batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                                batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                                batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                                batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                                customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                                autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                                autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                                outputUri = mainViewModel.shell.value.outputTreeUri,
                            )
                }
                            },
                            onStatus = { run {

                            pickerPostStatus((it)) { runOnUiThread { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } } }
                } },
                        )
            } },
                    )
        }
                    },
                    onSaveSnapshot = { p, dataList -> BatchPreviewStore.saveSnapshot(filesDir, p, dataList) },
                )
    }
                },
                onRefreshDismiss = { mainViewModel.updatePresetUi { it -> it.copy(showBatchPreviewRefreshConfirm = (false)) } },
            )
}
        run {
val __act1 = LocalContext.current

            val shellState by mainViewModel.shell.collectAsState()
            // Slice 3.1: Activity侧collect读VM单源；写经薄wrapper（重构期间保留）。
            OnboardingDialog(
                visible = shellState.onboardingVisible,
                isBusy = shellState.isBusy,
                onSkip = {
                    // 允许通过外部点击关闭视为跳过
                    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                        .putBoolean(PREF_HAS_COMPLETED_ONBOARDING, true).apply()
                    mainViewModel.updateShell { it -> it.copy(onboardingVisible = (false)) }
                    run {
        pickerToastStatus(
                    message = ("已跳过，可在设置-导出引导中重新进入"),
                    postOnUi = { text -> runOnUiThread { mainViewModel.updateShell { it -> it.copy(statusText = (text)) } } },
                    showToast = { text ->
                        runOnUiThread {
                            Toast.makeText(__act1, text, Toast.LENGTH_SHORT).show()
                        }
                    },
                )
    }
                },
                onChooseDir = {
                    // 不在此关闭，等待 chooseTreeLauncher 回调中关闭
                    chooseTreeLauncher.launch(null)
                },
            )
}
    }
}

/**
 * KernelSU 式的分页壳：每页拥有独立的 TopAppBar（标题+按钮随本页左右滑动），
 * 页面滚动命中该页自身的 scrollBehavior 折叠顶栏。
 */
@Composable
internal fun MainActivity.PagerShellPage(
    title: String,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    showPreviewStrip: Boolean = false,
    content: @Composable (PaddingValues, ScrollBehavior) -> Unit,
) {
    val pageScrollBehavior = MiuixScrollBehavior()
    var stripHeight by remember { mutableStateOf(0.dp) }
    val isDark = isSystemInDarkTheme()
    val targetExtra = if (showPreviewStrip) {
        if (stripHeight > 0.dp) stripHeight else 84.dp
    } else 0.dp
    val animatedExtra by animateDpAsState(
        targetValue = targetExtra,
        animationSpec = tween(durationMillis = 180),
        label = "previewStripExtra",
    )
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = title,
                scrollBehavior = pageScrollBehavior,
                navigationIconPadding = 0.dp,
                actionIconPadding = 0.dp,
                navigationIcon = navigationIcon,
                actions = actions,
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = animatedExtra),
            ) {
                content(innerPadding, pageScrollBehavior)
            }
            AnimatedVisibility(
                visible = showPreviewStrip,
                enter = fadeIn(animationSpec = tween(durationMillis = 150)) +
                    expandVertically(animationSpec = tween(durationMillis = 180)),
                exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                    shrinkVertically(animationSpec = tween(durationMillis = 160)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = innerPadding.calculateTopPadding())
                    .zIndex(1f),
            ) {
                HomePreviewStrip(
                    onHeightMeasured = { h -> if (h > 0.dp) stripHeight = h },
                )
            }
        }
    }
}
