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
    val pageBackground = if (isSystemInDarkTheme()) {
        Color.Black
    } else {
        Color(0xFFF7F7F7)
    }
    val selectedApp by remember {
        derivedStateOf { apps.firstOrNull { it.packageName == selectedPackageName } }
    }
    val generatedCount by remember {
        derivedStateOf { apps.count { it.packageName in generatedPackageNames && AppVisibility.shouldShowInPicker(it.applicationInfo, it.launchable, showSystemApps, packageName) } }
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
    val sharedPreviewSession = activeGenerationSession?.takeIf {
        it.packageName == previewPackageName && it.outDir.absolutePath == previewDirPath
    }
    val sharedPreviewTuning = currentTuningParams()

    LaunchedEffect(
        previewPackageName,
        previewDirPath,
        sharedPreviewSession,
        PreviewSelections.fromNames(tuningState.previewNormalLight, tuningState.previewNormalDark, tuningState.previewMonochromeLight, tuningState.previewMonochromeDark),
        previewVersion,
        sharedPreviewTuning,
    ) {
        val dirPath = previewDirPath
        val packageName = previewPackageName
        if (dirPath.isNullOrBlank() || packageName.isNullOrBlank()) {
            sharedPreviewAssets = null
            isPreviewAssetsRefreshing = false
            return@LaunchedEffect
        }
        isPreviewAssetsRefreshing = true
        try {
            delay(PREVIEW_LIVE_ASSET_DEBOUNCE_MS)
            val diskAssets = withContext(Dispatchers.IO) {
                loadPreviewAssets(File(dirPath)).preparedForDraw()
            }
            val liveAssets = sharedPreviewSession?.let { session ->
                try {
                    withContext(previewWorkerDispatcher) {
                        previewAssetsForSelections(session, PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark)).preparedForDraw()
                    }
                } catch (_: CancellationException) {
                    throw CancellationException()
                } catch (_: Throwable) {
                    null
                }
            }
            sharedPreviewAssets = liveAssets ?: diskAssets
        } catch (_: CancellationException) {
            throw CancellationException()
        } catch (_: Throwable) {
            sharedPreviewAssets = null
        } finally {
            isPreviewAssetsRefreshing = false
        }
    }

    fun completeBackFrom(progress: Float) {
        if (isCompletingBackGesture) {
            return
        }
        val start = progress.coerceIn(0f, 1f)
        isCompletingBackGesture = true
        skipNextHomeReturnAnimation = true
        screenScope.launch {
            completingBackProgress.snapTo(start)
            cancellingBackProgress.snapTo(0f)
            completingBackProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = ((1f - start) * 220f).roundToInt().coerceIn(90, 220)),
            )
            currentPage = AppPage.Home
            delay(40)
            systemBackProgress = 0f
            completingBackProgress.snapTo(0f)
            cancellingBackProgress.snapTo(0f)
            isCompletingBackGesture = false
            skipNextHomeReturnAnimation = false
        }
    }

    LaunchedEffect(currentPage, skipNextHomeReturnAnimation, isCompletingBackGesture) {
        if (currentPage != AppPage.Home) {
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
            skipNextHomeReturnAnimation = false
        } else if (skipNextHomeReturnAnimation || isCompletingBackGesture) {
            delay(90)
            if (!isCompletingBackGesture) {
                systemBackProgress = 0f
                completingBackProgress.snapTo(0f)
                cancellingBackProgress.snapTo(0f)
                skipNextHomeReturnAnimation = false
            }
        } else {
            systemBackProgress = 0f
            completingBackProgress.snapTo(0f)
            cancellingBackProgress.snapTo(0f)
            childEnterProgress.snapTo(1f)
        }
    }

    PredictiveBackHandler(enabled = currentPage != AppPage.Home && !isCompletingBackGesture) { backEvents ->
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
            if (start > 0f && currentPage != AppPage.Home && !isCompletingBackGesture) {
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
        val overlayPage = currentPage.takeIf { it != AppPage.Home }
        val dimReveal = if (skipNextHomeReturnAnimation || isCompletingBackGesture || backProgress > 0f) {
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
            val isCompletingBack = skipNextHomeReturnAnimation || isCompletingBackGesture
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
                            derivedStateOf { apps.filter { entry -> AppVisibility.shouldShowInPicker(entry.applicationInfo, entry.launchable, showSystemApps, packageName) } }
                        }
                        val ungeneratedCount by remember {
                            derivedStateOf { scopedApps.size - generatedCount }
                        }
                        val filteredApps by remember {
                            derivedStateOf {
                                val query = queryText.trim().lowercase(Locale.ROOT)
                                val stateScopedApps = when (generatedFilter) {
                                    GeneratedFilter.All -> scopedApps
                                    GeneratedFilter.Generated -> scopedApps.filter { it.packageName in generatedPackageNames }
                                    GeneratedFilter.Ungenerated -> scopedApps.filter { it.packageName !in generatedPackageNames }
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
                                when (generatedFilter) {
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

        batchApplyProgress?.let { progress ->
            BatchApplyProgressDialog(progress)
        }
        if (singleExportSheetVisible && exportProgress != null) {
            val p = exportProgress!!
            BackupProgressBottomSheet(
                progress = p,
                onStop = { cancelSingleExport() },
                onBackground = {
                    // 单包导出后台：仅隐藏弹窗，任务继续（不显示设置页动效）
                    singleExportSheetVisible = false
                },
            )
        }
        if (backupSheetVisible && backupProgress != null) {
            val p = backupProgress!!
            BackupProgressBottomSheet(
                progress = p,
                onStop = { cancelBackup() },
                onBackground = {
                    backupSheetVisible = false
                    backupInBackground = true
                    startBackupDotAnimation()
                },
            )
        } else if (!backupSheetVisible && backupInBackground && backupProgress != null) {
            // 后台态不显示底部弹窗，但保留状态供设置页“备份中...”展示
        }

        pendingServiceConfirm?.let { request ->
            ServiceConfirmDialog(
                request = request,
                onConfirm = { dismissServiceConfirm(confirmed = true) },
                onDismiss = { dismissServiceConfirm(confirmed = false) },
            )
        }
        RootWriteConfirmDialog()
        RefreshConfirmDialog()
        PresetPageDialogs()
        OnboardingDialog()
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
