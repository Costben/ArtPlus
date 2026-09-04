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

class MainActivity : ComponentActivity() {
    internal val rmbgGenerationGate = AtomicBoolean(false)
    internal val previewWorkerDispatcher = Executors.newSingleThreadExecutor { task ->
        Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            task.run()
        }, "ArtPlusPreviewWorker").apply {
            priority = Thread.MIN_PRIORITY
        }
    }.asCoroutineDispatcher()
    internal val previewWorkerScope = CoroutineScope(SupervisorJob() + previewWorkerDispatcher)
    internal val apps = mutableStateListOf<AppEntry>()
    internal var didRequestAppLoad = false
    internal var draftForegroundSubjectPercentText by mutableStateOf(DEFAULT_FOREGROUND_SUBJECT_PERCENT.toString())
    internal var draftForegroundShadowLevelText by mutableStateOf(DEFAULT_FOREGROUND_SHADOW_LEVEL.toString())
    internal var draftMonochromeThemeScaleText by mutableStateOf((DEFAULT_MONOCHROME_THEME_SCALE * 100).roundToInt().toString())
    internal var draftBackgroundSeparationText by mutableStateOf(DEFAULT_BACKGROUND_SEPARATION_PERCENT.toString())
    internal var draftPlateRemovalText by mutableStateOf(DEFAULT_PLATE_REMOVAL_PERCENT.toString())
    internal var draftShadowRemovalText by mutableStateOf(DEFAULT_SHADOW_REMOVAL_PERCENT.toString())
    internal var draftEdgePolishText by mutableStateOf(DEFAULT_EDGE_POLISH_PERCENT.toString())
    internal var draftRmbgAlphaStrengthText by mutableStateOf(DEFAULT_RMBG_ALPHA_STRENGTH_PERCENT.toString())
    internal var draftRmbgEdgeFeatherText by mutableStateOf(DEFAULT_RMBG_EDGE_FEATHER_PERCENT.toString())
    internal var draftRmbgEdgeAdjustText by mutableStateOf(DEFAULT_RMBG_EDGE_ADJUST_PERCENT.toString())
    internal var draftRmbgWeakAlphaKeepText by mutableStateOf(DEFAULT_RMBG_WEAK_ALPHA_KEEP_PERCENT.toString())
    internal var draftLiquidGlassRadiusText by mutableStateOf(DEFAULT_LIQUID_GLASS_RADIUS.toString())
    internal var draftLiquidGlassOuterWidthText by mutableStateOf(DEFAULT_LIQUID_GLASS_OUTER_WIDTH.toString())
    internal var draftLiquidGlassTopAlphaText by mutableStateOf(DEFAULT_LIQUID_GLASS_TOP_ALPHA.toString())
    internal var draftLiquidGlassBottomAlphaText by mutableStateOf(DEFAULT_LIQUID_GLASS_BOTTOM_ALPHA.toString())
    internal var draftLiquidGlassBackgroundMistAlphaText by mutableStateOf(DEFAULT_LIQUID_GLASS_BACKGROUND_MIST_ALPHA.toString())
    internal var draftLiquidGlassBottomDarkAlphaText by mutableStateOf(DEFAULT_LIQUID_GLASS_BOTTOM_DARK_ALPHA.toString())
    internal var draftLiquidGlassSubjectScaleText by mutableStateOf(DEFAULT_LIQUID_GLASS_SUBJECT_SCALE_PERCENT.toString())
    internal var draftLiquidGlassSubjectOutlineWidthText by mutableStateOf(DEFAULT_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH.toString())
    internal var draftLiquidGlassSubjectInnerOutlineWidthText by mutableStateOf(DEFAULT_LIQUID_GLASS_SUBJECT_INNER_OUTLINE_WIDTH.toString())
    internal var draftLiquidGlassSubjectShadowAlphaText by mutableStateOf(DEFAULT_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA.toString())
    internal var draftLiquidGlassSubjectOpacityText by mutableStateOf(DEFAULT_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT.toString())
    // P2 交界：历史单源已收敛进 MainViewModel（state/），Activity 不再持有 tuningHistory 栈；
    // 186 live vars 与 currentTuningParams() 不动（P5 重写），同步一律走快照显式调用。
    internal val mainViewModel: MainViewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }
    internal var draftBatchPreviewCountText by mutableStateOf(BatchPreviewSampler.DEFAULT_BATCH_PREVIEW_COUNT.toString())
    internal var draftBatchPreviewColumnsText by mutableStateOf("4")
    internal var draftBatchPreviewIconSizeDpText by mutableStateOf("54")
    internal var draftBatchPreviewCornerRadiusDpText by mutableStateOf("20")
    internal var draftJsonParamsText by mutableStateOf("")
    internal val presetStore by lazy { PresetStore(getSharedPreferences(PREFS_NAME, MODE_PRIVATE)) }
    // 底部备份/导出弹窗与后台态
    internal var backupJob: Job? = null
    internal var singleExportJob: Job? = null
    internal var backupDotJob: Job? = null
    internal var draftPreviewCornerRadiusDpText by mutableStateOf(DEFAULT_PREVIEW_CORNER_RADIUS_DP.toString())
    internal var draftPreviewIconSizeDpText by mutableStateOf(DEFAULT_PREVIEW_ICON_SIZE_DP.toString())
    internal var previewOutputJob: Job? = null
    internal var previewOutputRevision = 0
    internal var generatedPreviewRestoreRevision = 0
    internal var debugHttpServer: DebugHttpServer? = null
    internal var rmbgRuntime: DynamicRmbgRuntime? = null
    internal val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)






    internal val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            refreshPermissionState()
            loadApps()
        }

    internal val chooseTreeLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri == null) {
                toastStatus("未选择输出目录")
                return@registerForActivityResult
            }
            mainViewModel.updateShell { it -> it.copy(outputTreeUri = (uri)) }
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }
            // 自动在根目录创建 .nomedia，避免出现在相册
            runCatching { ensureNomediaAtTreeRoot(contentResolver, mainViewModel.shell.value.outputTreeUri) }
            toastStatus("已选择输出目录")
            saveUiState()
            // 若来自首次引导，自动执行全量备份
            if (mainViewModel.shell.value.onboardingVisible) {
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putBoolean(PREF_HAS_COMPLETED_ONBOARDING, true).apply()
                mainViewModel.updateShell { it -> it.copy(onboardingVisible = (false)) }
                backupAllToExternal(isFromOnboarding = true)
            }
        }

    internal val chooseRmbgComponentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                mainViewModel.updateShell { it -> it.copy(statusText = ("未选择 RMBG 组件")) }
                return@registerForActivityResult
            }
            installRmbgComponent(
                uri = uri,
                filesDir = filesDir,
                isBusy = mainViewModel.shell.value.isBusy,
                isGeneratingRmbgCandidate = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
                isInstallingRmbgComponent = mainViewModel.previewSession.value.isInstallingRmbgComponent,
                openInput = { contentResolver.openInputStream(it) },
                getRuntime = { rmbgRuntime },
                setRuntime = { rmbgRuntime = it },
                setInstalling = { mainViewModel.updatePreviewSession { v -> v.copy(isInstallingRmbgComponent = (it)) } },
                setStage = { mainViewModel.updatePreviewSession { v -> v.copy(rmbgInstallStage = (it)) } },
                setProgress = { mainViewModel.updatePreviewSession { v -> v.copy(rmbgInstallProgress = (it)) } },
                setStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                setComponentStatus = { mainViewModel.updateGptRmbgSettings { v -> v.copy(rmbgComponentStatus = (it)) } },
                setLastError = { mainViewModel.updatePreviewSession { v -> v.copy(lastRmbgCandidateError = (it)) } },
                runOnUi = { runOnUiThread(it) },
            )

        }

    internal val chooseCustomImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            val mode = mainViewModel.previewSession.value.pendingCustomImageMode
            val kind = mainViewModel.previewSession.value.pendingCustomImageKind
            mainViewModel.updatePreviewSession { it -> it.copy(pendingCustomImageMode = (null)) }
            mainViewModel.updatePreviewSession { it -> it.copy(pendingCustomImageKind = (null)) }
            if (uri == null) {
                mainViewModel.updateShell { it -> it.copy(statusText = ("未选择自定义图片")) }
                return@registerForActivityResult
            }
            if (mode == null || kind == null) {
                mainViewModel.updateShell { it -> it.copy(statusText = ("自定义槽位已失效")) }
                return@registerForActivityResult
            }
            importCustomPreviewImage(mode, kind, uri)
        }

    internal val chooseWallpaperLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                mainViewModel.updateShell { it -> it.copy(statusText = ("未选择壁纸")) }
                return@registerForActivityResult
            }
            importCustomWallpaper(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "ArtPlus Mobile"
        // Slice 3.3：初始化链下沉至 state/AppLoadOps.kt，顺序与条件不变；Activity 只留装配。
        mainViewModel.onCreatePreContent(
            onLoadGptSettings = ::loadGptSettings,
            onLoadTuningParams = ::loadTuningParams,
            onInitTuningHistory = ::initTuningHistory,
            onLoadRmbgSettings = ::loadRmbgSettings,
            onLoadGeneratedCache = {
                mainViewModel.updatePicker { it -> it.copy(generatedPackageNames = (loadGeneratedPackageCache(getSharedPreferences(PREFS_NAME, MODE_PRIVATE)))) }
                mainViewModel.updatePicker { it -> it.copy(generatedScanFailed = (false)) }
                mainViewModel.updatePicker { it -> it.copy(isScanningGeneratedPackages = (false)) }
            },
            onLoadUiState = ::loadUiState,
            onLoadPresetState = ::loadPresetState,
            onStartDebugServer = ::startDebugHttpServerIfNeeded,
            onRefreshPermissions = ::refreshPermissionState,
        )

        setContent {
            val darkMode = isSystemInDarkTheme()

            DisposableEffect(darkMode) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        AndroidColor.TRANSPARENT,
                        AndroidColor.TRANSPARENT,
                    ) { darkMode },
                    navigationBarStyle = SystemBarStyle.auto(
                        AndroidColor.TRANSPARENT,
                        AndroidColor.TRANSPARENT,
                    ) { darkMode },
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
                onDispose {}
            }

            MiuixTheme(
                colors = if (darkMode) darkColorScheme() else lightColorScheme(),
            ) {
                ArtPlusScreen()
            }
        }

        mainViewModel.onCreatePostContent(
            isDebugIntent = isDebugGenerateIntent(intent),
            onRequestDeclaredPermissions = ::requestDeclaredPermissions,
            onRequestSpecialPermissionsOnce = ::requestSpecialPermissionsOnce,
            onLoadApps = { loadApps() },
            onHandleDebugIntent = { handleDebugGenerateIntent(intent) },
        )
    }

    override fun onDestroy() {
        // Slice 3.3：清理顺序下沉至 state/AppLoadOps.kt，super.onDestroy 留装配。
        mainViewModel.onDestroyCleanup(
            onCancelPreviewJob = { previewOutputJob?.cancel() },
            onCancelWorkerScope = { previewWorkerScope.cancel() },
            onCloseWorkerDispatcher = { previewWorkerDispatcher.close() },
            onStopDebugServer = {
                debugHttpServer?.stop()
                debugHttpServer = null
            },
            onCloseRmbgRuntime = {
                runCatching { rmbgRuntime?.close() }
                rmbgRuntime = null
            },
        )
        super.onDestroy()
    }

    override fun onPause() {
        // Slice 3.3：持久化下沉至 state/AppLoadOps.kt，super 留装配。
        mainViewModel.onPausePersist(::saveUiState)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        // Slice 3.3：权限刷新与条件重载下沉至 state/AppLoadOps.kt，顺序与条件不变。
        val previousPackageListPermission = mainViewModel.picker.value.packageListPermissionGranted
        val previousUsageAccess = mainViewModel.picker.value.usageAccessGranted
        mainViewModel.onResumeRefresh(
            didRequestAppLoad = didRequestAppLoad,
            appsEmpty = apps.isEmpty(),
            previousQueryGranted = previousPackageListPermission,
            previousUsageGranted = previousUsageAccess,
            onRefreshPermissions = ::refreshPermissionState,
            currentQueryGranted = { mainViewModel.picker.value.packageListPermissionGranted },
            currentUsageGranted = { mainViewModel.picker.value.usageAccessGranted },
            onLoadApps = { loadApps() },
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Slice 3.3：debug 入口下沉至 state/AppLoadOps.kt，顺序不变。
        mainViewModel.onNewIntentDebug(
            onSetIntent = { setIntent(intent) },
            onHandleDebugIntent = { handleDebugGenerateIntent(intent) },
        )
    }

    // 重构期间保留：委托到 state/AppLoadOps.kt MainViewModel viewModelScope 版本，调用点零改动。
    internal fun startUiFriendlyThread(name: String, block: () -> Unit) {
        mainViewModel.launchUiFriendly(name, block)
    }


    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun OnboardingDialog() {
        val shellState by mainViewModel.shell.collectAsState()
        // Slice 3.1: Activity侧collect读VM单源；写经薄wrapper（重构期间保留）。
        OnboardingDialog(
            visible = shellState.onboardingVisible,
            isBusy = shellState.isBusy,
            onSkip = {
                // 允许通过外部点击关闭视为跳过
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putBoolean(PREF_HAS_COMPLETED_ONBOARDING, true).apply()
                mainViewModel.updateShell { it -> it.copy(onboardingVisible = (false)) }
                toastStatus("已跳过，可在设置-导出引导中重新进入")
            },
            onChooseDir = {
                // 不在此关闭，等待 chooseTreeLauncher 回调中关闭
                chooseTreeLauncher.launch(null)
            },
        )
    }



    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun RefreshConfirmDialog() {
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
                    saveUiState()
                }
                refreshArtPlusIcons()
            },
        )
    }








    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PermissionCard() {
        val pickerState by mainViewModel.picker.collectAsState()
        val shellState by mainViewModel.shell.collectAsState()
        // Slice 3.1: Activity侧collect读VM单源。
        PermissionCard(
            packageListGranted = pickerState.packageListPermissionGranted,
            usageGranted = pickerState.usageAccessGranted,
            isBusy = shellState.isBusy,
            onOpenAppSettings = { openAppPermissionSettings() },
            onOpenUsageSettings = { openUsageAccessSettings() },
        )
    }




    // 重构期间保留：委托到 ui/pages/home/HomePreviewCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun GeneratedPreviewCard() =
        GeneratedPreviewCard(
            dirPath = mainViewModel.previewSession.value.previewDirPath,
            packageName = mainViewModel.previewSession.value.previewPackageName,
            session = mainViewModel.previewSession.value.activeGenerationSession?.takeIf {
                it.packageName == mainViewModel.previewSession.value.previewPackageName && it.outDir.absolutePath == mainViewModel.previewSession.value.previewDirPath
            },
            displayAssets = mainViewModel.previewSession.value.sharedPreviewAssets,
            previewLoading = mainViewModel.previewSession.value.isGptPreviewLoading || mainViewModel.previewSession.value.isPreviewAssetsRefreshing || mainViewModel.previewSession.value.isPreviewOutputRefreshing,
            desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
            iconSizeDp = mainViewModel.previewSession.value.previewIconSizeDp,
            cornerRadiusDp = mainViewModel.previewSession.value.previewCornerRadiusDp,
            wallpaperInitial = cachedCustomWallpaper ?: cachedSystemWallpaper ?: cachedBundledWallpaper,
            wallpaperKey = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
            loadWallpaper = {
                withContext(Dispatchers.IO) {
                    loadCustomWallpaperBitmap() ?: loadPreviewWallpaperBitmap() ?: loadBundledPreviewWallpaperBitmap()
                }
            },
            materialColorProvider = ::systemMaterialColor,
            previewChoiceMode = mainViewModel.previewSession.value.previewChoiceMode,
            tuningState = mainViewModel.params.collectAsState().value,
            isBusy = mainViewModel.shell.value.isBusy,
            isGeneratingGptCandidate = mainViewModel.previewSession.value.isGeneratingGptCandidate,
            isGeneratingRmbgCandidate = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
            draftForegroundSubjectPercentText = draftForegroundSubjectPercentText,
            isDark = isSystemInDarkTheme(),
            nightSubjectLightBackgroundEnabled = mainViewModel.params.collectAsState().value.nightSubjectLightBackgroundEnabled,
            rmbgCandidatePackageName = mainViewModel.previewSession.value.rmbgCandidatePackageName,
            rmbgCandidateMode = mainViewModel.previewSession.value.rmbgCandidateMode,
            rmbgCandidateFailurePackageName = mainViewModel.previewSession.value.rmbgCandidateFailurePackageName,
            rmbgCandidateFailureMode = mainViewModel.previewSession.value.rmbgCandidateFailureMode,
            lastRmbgCandidateError = mainViewModel.previewSession.value.lastRmbgCandidateError,
            rmbgCandidateStatusText = mainViewModel.previewSession.value.rmbgCandidateStatusText,
            gptBaseUrl = mainViewModel.gptRmbgSettings.value.gptBaseUrl,
            gptApiKey = mainViewModel.gptRmbgSettings.value.gptApiKey,
            hasRmbgComponent = findRmbgComponent() != null,
            loadCandidateAssets = { candidate, mode ->
                withContext(previewWorkerDispatcher) {
                    previewAssetsForCandidate(candidate, mode).preparedForDraw()
                }
            },
            onChoiceClick = { mainViewModel.updatePreviewSession { v -> v.copy(previewChoiceMode = (it)) } },
            onNightFill = { updateNightSubjectLightBackgroundEnabled(it) },
            onDraftForegroundSubjectPercent = { draftForegroundSubjectPercentText = it },
            onSaveForegroundSubjectPercent = { updateForegroundSubjectPercent(it) },
            onGenerateGpt = { generateGptCandidateForMode(it) },
            onGenerateRmbg = { generateRmbgCandidateForMode(it) },
            onChooseCustom = { mode, kind -> chooseCustomImageForMode(mode, kind) },
            onApplyPreviewChoice = { mode, choice -> applyPreviewChoice(mode, choice) },
            onApplyPreviewChoiceToAll = { applyPreviewChoiceToAll(it) },
            onDismissChoice = { mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) } },
            onDismissChoiceFinished = { },
        )


    // 重构期间保留：委托到 ui/pages/home/HomePreviewTiles.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun TopPreviewStripTile(
        assets: PreviewAssets?,
        mode: PreviewMode,
        loading: Boolean,
        desktopBackground: PreviewDesktopBackground,
        iconSizeDp: Int,
        cornerRadiusDp: Int,
        modifier: Modifier = Modifier,
    ) =
        TopPreviewStripTile(
            assets = assets,
            mode = mode,
            loading = loading,
            desktopBackground = desktopBackground,
            iconSizeDp = iconSizeDp,
            cornerRadiusDp = cornerRadiusDp,
            wallpaperInitial = cachedCustomWallpaper ?: cachedSystemWallpaper ?: cachedBundledWallpaper,
            wallpaperKey = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
            loadWallpaper = {
                withContext(Dispatchers.IO) {
                    loadCustomWallpaperBitmap() ?: loadPreviewWallpaperBitmap() ?: loadBundledPreviewWallpaperBitmap()
                }
            },
            materialColorProvider = ::systemMaterialColor,
            modifier = modifier,
        )

    /** 调试图层卡片：前景 / 背景 / alpha 蒙版小图。 */
    // 重构期间保留：委托到 ui/pages/home/HomePreviewCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun LayerDebugCard() =
        LayerDebugCard(
            dirPath = mainViewModel.previewSession.value.previewDirPath,
            packageName = mainViewModel.previewSession.value.previewPackageName,
            session = mainViewModel.previewSession.value.activeGenerationSession?.takeIf {
                it.packageName == mainViewModel.previewSession.value.previewPackageName && it.outDir.absolutePath == mainViewModel.previewSession.value.previewDirPath
            },
            assets = mainViewModel.previewSession.value.sharedPreviewAssets,
            tuningState = mainViewModel.params.collectAsState().value,
        )


    // 重构期间保留：委托到 ui/pages/home/HomePreviewCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PreviewControlCard() =
        PreviewControlCard(
            tuningState = mainViewModel.params.collectAsState().value,
            isBusy = mainViewModel.shell.value.isBusy,
            previewCornerRadiusDp = mainViewModel.previewSession.value.previewCornerRadiusDp,
            draftPreviewCornerRadiusDpText = draftPreviewCornerRadiusDpText,
            previewIconSizeDp = mainViewModel.previewSession.value.previewIconSizeDp,
            draftPreviewIconSizeDpText = draftPreviewIconSizeDpText,
            draftForegroundSubjectPercentText = draftForegroundSubjectPercentText,
            previewStripEnabled = mainViewModel.previewSession.value.previewStripEnabled,
            previewDesktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
            wallpaperInitial = cachedCustomWallpaper ?: cachedSystemWallpaper ?: cachedBundledWallpaper,
            wallpaperKey = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
            loadWallpaper = {
                withContext(Dispatchers.IO) {
                    loadCustomWallpaperBitmap() ?: loadPreviewWallpaperBitmap() ?: loadBundledPreviewWallpaperBitmap()
                }
            },
            onDraftForegroundSubjectPercent = { draftForegroundSubjectPercentText = it },
            onSaveForegroundSubjectPercent = { updateForegroundSubjectPercent(it) },
            onDraftPreviewCornerRadiusDp = { draftPreviewCornerRadiusDpText = it },
            onSavePreviewCornerRadiusDp = { updatePreviewCornerRadiusDp(it) },
            onDraftPreviewIconSizeDp = { draftPreviewIconSizeDpText = it },
            onSavePreviewIconSizeDp = { updatePreviewIconSizeDp(it) },
            onPreviewStripEnabled = { updatePreviewStripEnabled(it) },
            onPreviewDesktopBackground = { updatePreviewDesktopBackground(it) },
        )

    // 重构期间保留：委托到 ui/pages/home/HomePreviewTiles.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PreviewBackgroundOption(
        option: PreviewDesktopBackground,
        selected: Boolean,
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
    ) =
        PreviewBackgroundOption(
            option = option,
            selected = selected,
            isBusy = mainViewModel.shell.value.isBusy,
            wallpaperInitial = cachedCustomWallpaper ?: cachedSystemWallpaper ?: cachedBundledWallpaper,
            wallpaperKey = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
            loadWallpaper = {
                withContext(Dispatchers.IO) {
                    loadCustomWallpaperBitmap() ?: loadPreviewWallpaperBitmap() ?: loadBundledPreviewWallpaperBitmap()
                }
            },
            modifier = modifier,
            onClick = onClick,
        )




    // 重构期间保留：委托到 ui/pages/home/HomePreviewTiles.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PreviewDesktopBackgroundSurface(
        option: PreviewDesktopBackground,
        modifier: Modifier = Modifier,
    ) =
        PreviewDesktopBackgroundSurface(
            option = option,
            modifier = modifier,
            wallpaperInitial = cachedCustomWallpaper ?: cachedSystemWallpaper ?: cachedBundledWallpaper,
            wallpaperKey = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
            loadWallpaper = {
                withContext(Dispatchers.IO) {
                    loadCustomWallpaperBitmap() ?: loadPreviewWallpaperBitmap() ?: loadBundledPreviewWallpaperBitmap()
                }
            },
        )

    // 重构期间保留：委托到 ui/pages/home/HomePreviewTiles.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun GeneratedIconPreview(
        assets: PreviewAssets?,
        mode: PreviewMode,
        modifier: Modifier = Modifier.size(72.dp),
        cornerRadiusDp: Int = mainViewModel.previewSession.value.previewCornerRadiusDp,
    ) =
        GeneratedIconPreview(
            assets = assets,
            mode = mode,
            modifier = modifier,
            cornerRadiusDp = cornerRadiusDp,
            materialColorProvider = ::systemMaterialColor,
        )

    // 重构期间保留：委托到 ui/pages/home/HomePreviewTiles.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun MissingIconPreview(
        modifier: Modifier = Modifier.size(72.dp),
        mode: PreviewMode? = null,
        compact: Boolean = false,
        cornerRadiusDp: Int = mainViewModel.previewSession.value.previewCornerRadiusDp,
    ) =
        MissingIconPreview(
            modifier = modifier,
            mode = mode,
            compact = compact,
            cornerRadiusDp = cornerRadiusDp,
            materialColorProvider = ::systemMaterialColor,
        )


    // 重构期间保留：委托到 ui/pages/home/HomePreviewCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PreviewNightFillBackgroundRow(
        checked: Boolean = mainViewModel.params.collectAsState().value.nightSubjectLightBackgroundEnabled,
        onCheckedChange: (Boolean) -> Unit = { updateNightSubjectLightBackgroundEnabled(it) },
    ) =
        PreviewNightFillBackgroundRow(
            checked = checked,
            isBusy = mainViewModel.shell.value.isBusy,
            onCheckedChange = onCheckedChange,
        )

    // 重构期间保留：委托到 ui/pages/home/HomePreviewChoice.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PreviewChoiceBottomSheet(
        show: Boolean,
        mode: PreviewMode,
        session: GenerationSession,
        onDismissRequest: () -> Unit,
        onDismissFinished: () -> Unit,
    ) =
        PreviewChoiceBottomSheet(
            show = show,
            mode = mode,
            session = session,
            tuningState = mainViewModel.params.collectAsState().value,
            isBusy = mainViewModel.shell.value.isBusy,
            isGeneratingGptCandidate = mainViewModel.previewSession.value.isGeneratingGptCandidate,
            isGeneratingRmbgCandidate = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
            draftForegroundSubjectPercentText = draftForegroundSubjectPercentText,
            isDark = isSystemInDarkTheme(),
            nightSubjectLightBackgroundEnabled = mainViewModel.params.collectAsState().value.nightSubjectLightBackgroundEnabled,
            rmbgCandidatePackageName = mainViewModel.previewSession.value.rmbgCandidatePackageName,
            rmbgCandidateMode = mainViewModel.previewSession.value.rmbgCandidateMode,
            rmbgCandidateFailurePackageName = mainViewModel.previewSession.value.rmbgCandidateFailurePackageName,
            rmbgCandidateFailureMode = mainViewModel.previewSession.value.rmbgCandidateFailureMode,
            lastRmbgCandidateError = mainViewModel.previewSession.value.lastRmbgCandidateError,
            rmbgCandidateStatusText = mainViewModel.previewSession.value.rmbgCandidateStatusText,
            gptBaseUrl = mainViewModel.gptRmbgSettings.value.gptBaseUrl,
            gptApiKey = mainViewModel.gptRmbgSettings.value.gptApiKey,
            hasRmbgComponent = findRmbgComponent() != null,
            cornerRadiusDp = mainViewModel.previewSession.value.previewCornerRadiusDp,
            materialColorProvider = ::systemMaterialColor,
            loadCandidateAssets = { candidate, m ->
                withContext(previewWorkerDispatcher) {
                    previewAssetsForCandidate(candidate, m).preparedForDraw()
                }
            },
            onNightFill = { updateNightSubjectLightBackgroundEnabled(it) },
            onDraftForegroundSubjectPercent = { draftForegroundSubjectPercentText = it },
            onSaveForegroundSubjectPercent = { updateForegroundSubjectPercent(it) },
            onGenerateGpt = { generateGptCandidateForMode(it) },
            onGenerateRmbg = { generateRmbgCandidateForMode(it) },
            onChooseCustom = { m, kind -> chooseCustomImageForMode(m, kind) },
            onApply = { m, choice -> applyPreviewChoice(m, choice) },
            onApplyAll = { applyPreviewChoiceToAll(it) },
            onDismissRequest = onDismissRequest,
            onDismissFinished = onDismissFinished,
        )

    // Slice 2.2 已搬入 ui/pages/home/HomePreviewChoice.kt：shouldShowPreviewChoiceRow（纯函数，同包直接用）。
    // 重构期间保留：委托到 ui/pages/home/HomePreviewChoice.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun MoreRulesGroupRow(
        selectedRule: PreviewChoice?,
        expanded: Boolean,
        onToggle: () -> Unit,
    ) =
        MoreRulesGroupRow(
            selectedRule = selectedRule,
            expanded = expanded,
            isBusy = mainViewModel.shell.value.isBusy,
            isGeneratingGptCandidate = mainViewModel.previewSession.value.isGeneratingGptCandidate,
            isGeneratingRmbgCandidate = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
            onToggle = onToggle,
        )

    // 重构期间保留：委托到 ui/pages/home/HomePreviewChoice.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PreviewChoiceRow(mode: PreviewMode, choice: PreviewChoice, session: GenerationSession) =
        PreviewChoiceRow(
            mode = mode,
            choice = choice,
            session = session,
            tuningState = mainViewModel.params.collectAsState().value,
            isBusy = mainViewModel.shell.value.isBusy,
            isGeneratingGptCandidate = mainViewModel.previewSession.value.isGeneratingGptCandidate,
            isGeneratingRmbgCandidate = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
            rmbgCandidatePackageName = mainViewModel.previewSession.value.rmbgCandidatePackageName,
            rmbgCandidateMode = mainViewModel.previewSession.value.rmbgCandidateMode,
            rmbgCandidateFailurePackageName = mainViewModel.previewSession.value.rmbgCandidateFailurePackageName,
            rmbgCandidateFailureMode = mainViewModel.previewSession.value.rmbgCandidateFailureMode,
            lastRmbgCandidateError = mainViewModel.previewSession.value.lastRmbgCandidateError,
            rmbgCandidateStatusText = mainViewModel.previewSession.value.rmbgCandidateStatusText,
            gptBaseUrl = mainViewModel.gptRmbgSettings.value.gptBaseUrl,
            gptApiKey = mainViewModel.gptRmbgSettings.value.gptApiKey,
            hasRmbgComponent = findRmbgComponent() != null,
            cornerRadiusDp = mainViewModel.previewSession.value.previewCornerRadiusDp,
            materialColorProvider = ::systemMaterialColor,
            loadCandidateAssets = { candidate, m ->
                withContext(previewWorkerDispatcher) {
                    previewAssetsForCandidate(candidate, m).preparedForDraw()
                }
            },
            onGenerateGpt = { generateGptCandidateForMode(it) },
            onGenerateRmbg = { generateRmbgCandidateForMode(it) },
            onChooseCustom = { m, kind -> chooseCustomImageForMode(m, kind) },
            onApply = { m, c -> applyPreviewChoice(m, c) },
            onApplyAll = { applyPreviewChoiceToAll(it) },
        )


    // 重构期间保留：委托到 ui/pages/home/HomePreviewChoice.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun CandidateIconPreview(candidate: IconCandidate, mode: PreviewMode) =
        CandidateIconPreview(
            candidate = candidate,
            mode = mode,
            tuningState = mainViewModel.params.collectAsState().value,
            cornerRadiusDp = mainViewModel.previewSession.value.previewCornerRadiusDp,
            materialColorProvider = ::systemMaterialColor,
            loadAssets = { c, m ->
                withContext(previewWorkerDispatcher) {
                    previewAssetsForCandidate(c, m).preparedForDraw()
                }
            },
        )
    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    internal fun systemMaterialColor(resourceName: String, fallback: Color): Color =
        pickerSystemMaterialColor(
            resources = resources,
            getColor = ::getColor,
            resourceName = resourceName,
            fallback = fallback,
        )

    // 重构期间保留：委托到 ui/pages/home/HomeStatusCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun StatusCard(
        selectedApp: AppEntry?,
        launcherCount: Int,
        totalCount: Int,
        generatedCount: Int,
    ) {
        val shellState by mainViewModel.shell.collectAsState()
        // Slice 3.1: Activity侧collect读VM单源；写经薄wrapper（重构期间保留）。
        StatusCard(
            selectedApp = selectedApp,
            launcherCount = launcherCount,
            totalCount = totalCount,
            generatedCount = generatedCount,
            isBusy = shellState.isBusy,
            hasApps = apps.isNotEmpty(),
            statusText = shellState.statusText,
            onOpenPicker = { mainViewModel.updateShell { it -> it.copy(currentPage = (AppPage.AppPicker)) } },
            appIcon = { entry -> AppIcon(entry, 48.dp) },
        )
    }

    // 重构期间保留：委托到 ui/pages/home/HomeStatusCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun EmptyAppListCard() {
        val pickerState by mainViewModel.picker.collectAsState()
        val shellState by mainViewModel.shell.collectAsState()
        // Slice 3.1: Activity侧collect读VM单源；写经薄wrapper（重构期间保留）。
        EmptyAppListCard(
            queryText = pickerState.queryText,
            showSystemApps = pickerState.showSystemApps,
            hasHiddenSystemApps = apps.any { AppVisibility.isSystemAppFlags(it.applicationInfo.flags) && it.packageName != packageName },
            isBusy = shellState.isBusy,
            onShowSystemApps = {
                mainViewModel.updatePicker { it -> it.copy(showSystemApps = (true)) }
                saveUiState()
            },
            onRefresh = { loadApps() },
        )
    }

    // 重构期间保留：委托到 ui/pages/settings/SettingsTuningCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun LocalSeparationModeControl() =
        LocalSeparationModeControl(
            tuningState = mainViewModel.params.collectAsState().value,
            isBusy = mainViewModel.shell.value.isBusy,
            onSelect = { updateLocalSeparationMode(it) },
        )

    /** 第二层级「生成设置」：顶部「滑块 / JSON」切换 + 保存成预设 + 滑块分类导航。 */
    // 重构期间保留：委托到 ui/pages/home/HomeStatusCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun GenerationNavCard() {
        val shellState by mainViewModel.shell.collectAsState()
        // Slice 3.1: Activity侧collect读VM单源；写经薄wrapper（重构期间保留）。
        GenerationNavCard(
            isBusy = shellState.isBusy,
            advancedSettingsTab = shellState.advancedSettingsTab,
            advancedSettingsCategory = shellState.advancedSettingsCategory,
            onTabSelected = {
                mainViewModel.updateShell { v -> v.copy(advancedSettingsTab = (it)) }
                saveUiState()
            },
            onRequestSavePreset = {
                mainViewModel.updatePresetUi { it -> it.copy(presetSaveName = ("")) }
                mainViewModel.updatePresetUi { it -> it.copy(presetSaveDialogVisible = (true)) }
            },
            onCategorySelected = {
                mainViewModel.updateShell { v -> v.copy(advancedSettingsCategory = (it)) }
                saveUiState()
            },
        )
    }


    // 重构期间保留：委托到 ui/pages/settings/SettingsGlassCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun LiquidGlassToggleCard() =
        LiquidGlassToggleCard(
            enabled = mainViewModel.params.collectAsState().value.liquidGlassEnabled,
            isBusy = mainViewModel.shell.value.isBusy,
            onCheckedChange = { updateLiquidGlassEnabled(it) },
        )

    // 重构期间保留：委托到 ui/pages/settings/SettingsGlassCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun LiquidGlassSurfaceCard() =
        LiquidGlassSurfaceCard(
            tuningState = mainViewModel.params.collectAsState().value,
            isBusy = mainViewModel.shell.value.isBusy,
            draftRadiusText = draftLiquidGlassRadiusText,
            onDraftRadiusChange = { draftLiquidGlassRadiusText = it },
            onSaveRadius = { updateLiquidGlassRadius(it) },
            draftOuterWidthText = draftLiquidGlassOuterWidthText,
            onDraftOuterWidthChange = { draftLiquidGlassOuterWidthText = it },
            onSaveOuterWidth = { updateLiquidGlassOuterWidth(it) },
            draftTopAlphaText = draftLiquidGlassTopAlphaText,
            onDraftTopAlphaChange = { draftLiquidGlassTopAlphaText = it },
            onSaveTopAlpha = { updateLiquidGlassTopAlpha(it) },
            draftBottomAlphaText = draftLiquidGlassBottomAlphaText,
            onDraftBottomAlphaChange = { draftLiquidGlassBottomAlphaText = it },
            onSaveBottomAlpha = { updateLiquidGlassBottomAlpha(it) },
            draftBackgroundMistAlphaText = draftLiquidGlassBackgroundMistAlphaText,
            onDraftBackgroundMistAlphaChange = { draftLiquidGlassBackgroundMistAlphaText = it },
            onSaveBackgroundMistAlpha = { updateLiquidGlassBackgroundMistAlpha(it) },
            draftBottomDarkAlphaText = draftLiquidGlassBottomDarkAlphaText,
            onDraftBottomDarkAlphaChange = { draftLiquidGlassBottomDarkAlphaText = it },
            onSaveBottomDarkAlpha = { updateLiquidGlassBottomDarkAlpha(it) },
        )

    // 重构期间保留：委托到 ui/pages/settings/SettingsGlassCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun LiquidGlassSubjectCard() =
        LiquidGlassSubjectCard(
            tuningState = mainViewModel.params.collectAsState().value,
            isBusy = mainViewModel.shell.value.isBusy,
            draftSubjectScaleText = draftLiquidGlassSubjectScaleText,
            onDraftSubjectScaleChange = { draftLiquidGlassSubjectScaleText = it },
            onSaveSubjectScale = { updateLiquidGlassSubjectScalePercent(it) },
            draftSubjectOutlineWidthText = draftLiquidGlassSubjectOutlineWidthText,
            onDraftSubjectOutlineWidthChange = { draftLiquidGlassSubjectOutlineWidthText = it },
            onSaveSubjectOutlineWidth = { updateLiquidGlassSubjectOutlineWidth(it) },
            draftSubjectInnerOutlineWidthText = draftLiquidGlassSubjectInnerOutlineWidthText,
            onDraftSubjectInnerOutlineWidthChange = { draftLiquidGlassSubjectInnerOutlineWidthText = it },
            onSaveSubjectInnerOutlineWidth = { updateLiquidGlassSubjectInnerOutlineWidth(it) },
            draftSubjectShadowAlphaText = draftLiquidGlassSubjectShadowAlphaText,
            onDraftSubjectShadowAlphaChange = { draftLiquidGlassSubjectShadowAlphaText = it },
            onSaveSubjectShadowAlpha = { updateLiquidGlassSubjectShadowAlpha(it) },
            draftSubjectOpacityText = draftLiquidGlassSubjectOpacityText,
            onDraftSubjectOpacityChange = { draftLiquidGlassSubjectOpacityText = it },
            onSaveSubjectOpacity = { updateLiquidGlassSubjectOpacityPercent(it) },
        )

    // 重构期间保留：委托到 ui/pages/settings/SettingsTuningCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun LocalRuleTuningCard() =
        LocalRuleTuningCard(
            tuningState = mainViewModel.params.collectAsState().value,
            isBusy = mainViewModel.shell.value.isBusy,
            draftBackgroundSeparationText = draftBackgroundSeparationText,
            onDraftBackgroundSeparationChange = { draftBackgroundSeparationText = it },
            onSaveBackgroundSeparation = { updateBackgroundSeparationPercent(it) },
            draftPlateRemovalText = draftPlateRemovalText,
            onDraftPlateRemovalChange = { draftPlateRemovalText = it },
            onSavePlateRemoval = { updatePlateRemovalPercent(it) },
            draftShadowRemovalText = draftShadowRemovalText,
            onDraftShadowRemovalChange = { draftShadowRemovalText = it },
            onSaveShadowRemoval = { updateShadowRemovalPercent(it) },
            draftEdgePolishText = draftEdgePolishText,
            onDraftEdgePolishChange = { draftEdgePolishText = it },
            onSaveEdgePolish = { updateEdgePolishPercent(it) },
        )

    // 重构期间保留：委托到 ui/pages/settings/SettingsTuningCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun LocalWorkflowPipelineCard() =
        LocalWorkflowPipelineCard(
            tuningState = mainViewModel.params.collectAsState().value,
            isBusy = mainViewModel.shell.value.isBusy,
            onToggle = { key, enabled -> updateLocalWorkflowToggle(key, enabled) },
        )

    // 重构期间保留：委托到 ui/pages/settings/SettingsTuningCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun LocalWorkflowToggleRow(
        title: String,
        summary: String,
        checked: Boolean,
        key: String,
    ) = LocalWorkflowToggleRow(
        title = title,
        summary = summary,
        checked = checked,
        key = key,
        isBusy = mainViewModel.shell.value.isBusy,
        onCheckedChange = { k, v -> updateLocalWorkflowToggle(k, v) },
    )

    // 重构期间保留：委托到 ui/pages/settings/SettingsTuningCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun RmbgTuningCard() =
        RmbgTuningCard(
            tuningState = mainViewModel.params.collectAsState().value,
            isBusy = mainViewModel.shell.value.isBusy,
            draftAlphaStrengthText = draftRmbgAlphaStrengthText,
            onDraftAlphaStrengthChange = { draftRmbgAlphaStrengthText = it },
            onSaveAlphaStrength = { updateRmbgAlphaStrengthPercent(it) },
            draftEdgeFeatherText = draftRmbgEdgeFeatherText,
            onDraftEdgeFeatherChange = { draftRmbgEdgeFeatherText = it },
            onSaveEdgeFeather = { updateRmbgEdgeFeatherPercent(it) },
            draftEdgeAdjustText = draftRmbgEdgeAdjustText,
            onDraftEdgeAdjustChange = { draftRmbgEdgeAdjustText = it },
            onSaveEdgeAdjust = { updateRmbgEdgeAdjustPercent(it) },
            draftWeakAlphaKeepText = draftRmbgWeakAlphaKeepText,
            onDraftWeakAlphaKeepChange = { draftRmbgWeakAlphaKeepText = it },
            onSaveWeakAlphaKeep = { updateRmbgWeakAlphaKeepPercent(it) },
        )

    // 重构期间保留：委托到 ui/pages/settings/SettingsJsonCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun JsonSettingsEditorCard() =
        JsonSettingsEditorCard(
            currentParams = currentTuningParams(),
            draftText = draftJsonParamsText,
            onDraftChange = { draftJsonParamsText = it },
            onSave = { saveJsonParamsFromText(it) },
            onRestore = {
                draftJsonParamsText = currentTuningParams().toJson().toString(4)
                mainViewModel.updateShell { it -> it.copy(statusText = ("已恢复为当前参数 JSON")) }
            },
        )

    // ---------- 预设：保存 / 应用 / 批量 / 导入导出 ----------

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun refreshPresets() =
        refreshPresets(
            store = presetStore,
            onBumpVersion = { mainViewModel.updatePresetUi { it -> it.copy(presetListVersion = it.presetListVersion + (1)) } },
            onRefreshed = { id, base ->
                mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (id)) }
                mainViewModel.updatePresetUi { it -> it.copy(activePresetBaseParams = (base)) }
            },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun loadPresetState() =
        loadPresetState(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            batchOutputModeKey = PREF_BATCH_OUTPUT_MODE,
            batchOutputModeFallbackName = BatchOutputMode.Root.name,
            gptRunCountKey = PREF_GPT_RUN_COUNT,
            rmbgRunCountKey = PREF_RMBG_RUN_COUNT,
            onRefreshPresets = { refreshPresets() },
            onLoaded = { mode, gpt, rmbg ->
                mainViewModel.updatePresetUi { it -> it.copy(batchOutputMode = (mode)) }
                mainViewModel.updatePresetUi { it -> it.copy(gptRunCount = (gpt)) }
                mainViewModel.updatePresetUi { it -> it.copy(rmbgRunCount = (rmbg)) }
            },
        )

    internal fun incrementGptRunCount() {
        mainViewModel.updatePresetUi { it -> it.copy(gptRunCount = it.gptRunCount + (1)) }
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putInt(PREF_GPT_RUN_COUNT, mainViewModel.presetUi.value.gptRunCount)
            .apply()
    }

    internal fun incrementRmbgRunCount() {
        mainViewModel.updatePresetUi { it -> it.copy(rmbgRunCount = it.rmbgRunCount + (1)) }
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putInt(PREF_RMBG_RUN_COUNT, mainViewModel.presetUi.value.rmbgRunCount)
            .apply()
    }

    /**
     * 本地优先：调用 AI/RMBG 前弹二次确认。
     * 入口函数用 [confirmed] 参数重入，避免再次弹窗。
     */
    internal fun requestServiceConfirm(
        title: String,
        message: String,
        confirmLabel: String,
        onConfirm: () -> Unit,
    ) {
        mainViewModel.updateConfirm { it -> it.copy(pendingServiceConfirm = (ServiceConfirmRequest(
            title = title,
            message = message,
            confirmLabel = confirmLabel,
            onConfirm = onConfirm,
        ))) }
    }

    internal fun dismissServiceConfirm(confirmed: Boolean) {
        val request = mainViewModel.confirm.value.pendingServiceConfirm ?: return
        mainViewModel.updateConfirm { it -> it.copy(pendingServiceConfirm = (null)) }
        if (confirmed) {
            request.onConfirm()
        }
    }

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun saveCurrentAsPreset(rawName: String) =
        saveCurrentAsPreset(
            rawName = rawName,
            store = presetStore,
            current = currentTuningParams(),
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

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun overwritePreset(preset: TuningPreset) =
        overwritePreset(
            preset = preset,
            store = presetStore,
            current = currentTuningParams(),
            viewModel = mainViewModel,
            onOverwritten = { p, cur, msg ->
                mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (p.id)) }
                mainViewModel.updatePresetUi { it -> it.copy(activePresetBaseParams = (cur)) }
                mainViewModel.updatePresetUi { it -> it.copy(presetListVersion = it.presetListVersion + (1)) }
                mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
            },
            onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun resetToPreset(preset: TuningPreset) =
        resetToPreset(
            preset = preset,
            isBusy = mainViewModel.shell.value.isBusy,
            isGeneratingGptCandidate = mainViewModel.previewSession.value.isGeneratingGptCandidate,
            isGeneratingRmbgCandidate = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
            before = currentTuningParams(),
            viewModel = mainViewModel,
            onReset = { p, merged, msg ->
                applyTuningParams(merged, rebuildCandidates = true)
                presetStore.activePresetId = p.id
                mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (p.id)) }
                mainViewModel.updatePresetUi { it -> it.copy(activePresetBaseParams = (p.params)) }
                mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
            },
            onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun applyPreset(preset: TuningPreset) =
        applyPreset(
            preset = preset,
            isBusy = mainViewModel.shell.value.isBusy,
            isGeneratingGptCandidate = mainViewModel.previewSession.value.isGeneratingGptCandidate,
            isGeneratingRmbgCandidate = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
            before = currentTuningParams(),
            viewModel = mainViewModel,
            onApplied = { p, merged, msg ->
                applyTuningParams(merged, rebuildCandidates = true)
                presetStore.activePresetId = p.id
                mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (p.id)) }
                mainViewModel.updatePresetUi { it -> it.copy(activePresetBaseParams = (p.params)) }
                mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
            },
            onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun deletePreset(id: String) =
        deletePreset(
            id = id,
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

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun renamePreset(id: String, rawName: String) =
        renamePreset(
            id = id,
            rawName = rawName,
            store = presetStore,
            viewModel = mainViewModel,
            onRenamed = { _, msg ->
                mainViewModel.updatePresetUi { it -> it.copy(presetListVersion = it.presetListVersion + (1)) }
                mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
            },
            onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun exportPresetsToClipboard() =
        exportPresetsToClipboard(
            store = presetStore,
            clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager,
            onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun exportSinglePresetToClipboard(preset: TuningPreset) =
        exportSinglePresetToClipboard(
            preset = preset,
            store = presetStore,
            clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager,
            onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun importPresetsFromText(text: String) =
        importPresetsFromText(
            text = text,
            store = presetStore,
            onApplied = { msg ->
                mainViewModel.updatePresetUi { it -> it.copy(presetImportDialogVisible = (false)) }
                mainViewModel.updatePresetUi { it -> it.copy(presetImportText = ("")) }
                mainViewModel.updatePresetUi { it -> it.copy(presetListVersion = it.presetListVersion + (1)) }
                mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
            },
        )

    /** JSON 编辑器：解析文本为 TuningParams 并应用（缺失键保持当前值）。 */
    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun saveJsonParamsFromText(text: String) =
        saveJsonParamsFromText(
            text = text,
            current = currentTuningParams(),
            onApplyParams = { applyTuningParams(it, rebuildCandidates = true) },
            onStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
        )

    // 重构期间保留：委托到 state/PresetBatchOps.kt MainViewModel 显式参数版本，调用点零改动。
    internal fun startBatchPreview(preset: TuningPreset) =
        mainViewModel.startBatchPreview(
            preset = preset,
            apps = apps.toList(),
            selfPackageName = packageName,
            batchPreviewCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
            generatedPackageNames = mainViewModel.picker.value.generatedPackageNames,
            originalParams = currentTuningParams(),
            cacheDir = cacheDir,
            loadIcon = { app -> app.applicationInfo.loadIcon(packageManager) },
            defaultChoiceForMode = { mode, auto -> defaultPreviewChoiceForMode(mode, auto) },
            composeAssets = { session, selections -> previewAssetsForSelections(session, selections) },
            onApplyMerged = { merged ->
                applyTuningParams(merged, rebuildCandidates = false, persist = false, captureUndo = false, refreshPreview = false)
            },
            onRestoreOriginal = { original ->
                applyTuningParams(original, rebuildCandidates = true, persist = false, captureUndo = false, refreshPreview = true)
            },
            onSaveSnapshot = { p, dataList -> BatchPreviewStore.saveSnapshot(filesDir, p, dataList) },
        )

    // 重构期间保留：委托到 state/PresetBatchOps.kt MainViewModel 显式参数版本，调用点零改动。
    internal fun openBatchPreviewForPreset(preset: TuningPreset) =
        mainViewModel.openBatchPreviewForPreset(preset = preset, filesDir = filesDir)

    // 重构期间保留：委托到 state/PresetBatchOps.kt MainViewModel suspend 显式参数版本，调用点零改动（唯一调用方为批量预览渲染协程）。
    internal suspend fun generateMemoryPreviewAssetsForApp(
        app: AppEntry,
        pipeline: LocalPipelineConfig,
    ): PreviewAssets =
        mainViewModel.generateMemoryPreviewAssetsForApp(
            app = app,
            pipeline = pipeline,
            tuning = mainViewModel.params.value,
            cacheDir = cacheDir,
            loadIcon = { a -> a.applicationInfo.loadIcon(packageManager) },
            defaultChoiceForMode = { mode, auto -> defaultPreviewChoiceForMode(mode, auto) },
            composeAssets = { session, selections -> previewAssetsForSelections(session, selections) },
        )

    // 重构期间保留：委托到 state/PresetBatchOps.kt MainViewModel 显式参数版本，调用点零改动。
    internal fun applyPresetToSelectedApps(preset: TuningPreset) =
        mainViewModel.applyPresetToSelectedApps(
            preset = preset,
            onRequestConfirm = { title, message, confirmLabel, onConfirm ->
                requestServiceConfirm(title, message, confirmLabel, onConfirm)
            },
            onExecute = { p, pkgs -> executeApplyPresetToSelectedApps(p, pkgs) },
        )

    // 重构期间保留：委托到 state/PresetBatchOps.kt MainViewModel 显式参数版本，调用点零改动。
    internal fun executeApplyPresetToSelectedApps(preset: TuningPreset, batchPackageNames: List<String>) =
        mainViewModel.executeApplyPresetToSelectedApps(
            preset = preset,
            batchPackageNames = batchPackageNames,
            beforeParams = currentTuningParams(),
            store = presetStore,
            selectedAtStart = mainViewModel.picker.value.selectedPackageName,
            apps = apps.toList(),
            onApplyPresetParams = { merged -> applyTuningParams(merged, rebuildCandidates = false) },
            generatePackage = { app -> generateArtPlusPackage(app, useGpt = false) },
            persistGenerated = { combined ->
                updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), combined)
            },
            onSaveUiState = ::saveUiState,
        )

    /** 在 APK 选择页多选态下套用当前预设/当前调参批量生成（本地）。 */
    // 重构期间保留：委托到 state/PresetBatchOps.kt MainViewModel 显式参数版本，调用点零改动。
    internal fun applyCurrentPresetBatch() =
        mainViewModel.applyCurrentPresetBatch(
            store = presetStore,
            onRequestConfirm = { title, message, confirmLabel, onConfirm ->
                requestServiceConfirm(title, message, confirmLabel, onConfirm)
            },
            onExecutePreset = { p, pkgs -> executeApplyPresetToSelectedApps(p, pkgs) },
            onExecuteCurrent = { pkgs -> executeApplyCurrentBatch(pkgs) },
        )

    // 重构期间保留：委托到 state/PresetBatchOps.kt MainViewModel 显式参数版本，调用点零改动。
    internal fun executeApplyCurrentBatch(batchPackageNames: List<String>) =
        mainViewModel.executeApplyCurrentBatch(
            batchPackageNames = batchPackageNames,
            selectedAtStart = mainViewModel.picker.value.selectedPackageName,
            apps = apps.toList(),
            generatePackage = { app -> generateArtPlusPackage(app, useGpt = false) },
            persistGenerated = { combined ->
                updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), combined)
            },
            onSaveUiState = ::saveUiState,
        )


    // 重构期间保留：委托到 ui/pages/presets/PresetCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PresetStatusCard() =
        PresetStatusCard(
            presets = remember(mainViewModel.presetUi.value.presetListVersion) { presetStore.all() },
            activePresetId = mainViewModel.presetUi.value.activePresetId,
            activePresetBaseParams = mainViewModel.presetUi.value.activePresetBaseParams,
            currentParams = currentTuningParams(),
            isBusy = mainViewModel.shell.value.isBusy,
            onOverwrite = { overwritePreset(it) },
            onRequestSavePreset = {
                mainViewModel.updatePresetUi { v -> v.copy(presetSaveName = (it)) }
                mainViewModel.updatePresetUi { it -> it.copy(presetSaveDialogVisible = (true)) }
            },
            onResetToPreset = { resetToPreset(it) },
            onResetToDefaults = { resetToDefaults() },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PresetLibraryCard() =
        PresetLibraryCard(
            presets = remember(mainViewModel.presetUi.value.presetListVersion) { presetStore.all() },
            activePresetId = mainViewModel.presetUi.value.activePresetId,
            activePresetBaseParams = mainViewModel.presetUi.value.activePresetBaseParams,
            currentParams = currentTuningParams(),
            searchQuery = mainViewModel.presetUi.value.presetSearchQuery,
            onSearchChange = { mainViewModel.updatePresetUi { v -> v.copy(presetSearchQuery = (it)) } },
            listExpanded = mainViewModel.presetUi.value.presetListExpanded,
            onToggleExpanded = { mainViewModel.updatePresetUi { it -> it.copy(presetListExpanded = (!mainViewModel.presetUi.value.presetListExpanded)) } },
            isBusy = mainViewModel.shell.value.isBusy,
            onApply = { applyPreset(it) },
            onPreview = { openBatchPreviewForPreset(it) },
            onMore = { mainViewModel.updatePresetUi { v -> v.copy(presetActionMenuTarget = (it)) } },
        )

    // 重构期间保留：委托到 ui/pages/settings/SettingsAppCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun WallpaperSettingsCard() =
        WallpaperSettingsCard(
            hasCustom = mainViewModel.batchPreviewConfig.value.customWallpaperPath != null,
            customInfo = mainViewModel.batchPreviewConfig.value.customWallpaperInfo,
            isBusy = mainViewModel.shell.value.isBusy,
            onPickWallpaper = {
                chooseWallpaperLauncher.launch(arrayOf("image/jpeg", "image/png", "image/webp"))
            },
            onClearWallpaper = { clearCustomWallpaper() },
        )

    // 重构期间保留：委托到 ui/pages/settings/SettingsAppCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun BatchPreviewSettingsCard() =
        BatchPreviewSettingsCard(
            value = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
            draftText = draftBatchPreviewCountText,
            isBusy = mainViewModel.shell.value.isBusy,
            onDraftChange = { draftBatchPreviewCountText = it },
            onSave = { updateBatchPreviewCount(it) },
        )


    // 重构期间保留：委托到 ui/pages/presets/PresetDialogs.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PresetActionMenuDialog(
        target: TuningPreset,
        onDismiss: () -> Unit,
    ) =
        PresetActionMenuDialog(
            target = target,
            isBusy = mainViewModel.shell.value.isBusy,
            onDismiss = onDismiss,
            onApply = { applyPreset(it) },
            onPreview = { openBatchPreviewForPreset(it) },
            onOverwrite = { overwritePreset(it) },
            onRename = { mainViewModel.updatePresetUi { v -> v.copy(presetRenameTarget = (it)) } },
            onExportSingle = { exportSinglePresetToClipboard(it) },
            onDelete = { mainViewModel.updatePresetUi { v -> v.copy(presetDeleteConfirmTarget = (it)) } },
        )


    // 重构期间保留：委托到 ui/pages/presets/PresetDialogs.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PresetDeleteConfirmDialog(
        target: TuningPreset,
        onDismiss: () -> Unit,
    ) =
        PresetDeleteConfirmDialog(
            target = target,
            onDismiss = onDismiss,
            onConfirmDelete = { deletePreset(it) },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetDialogs.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PresetPageDialogs() =
        PresetPageDialogs(
            saveDialogVisible = mainViewModel.presetUi.value.presetSaveDialogVisible,
            saveInitialName = mainViewModel.presetUi.value.presetSaveName,
            onSaveConfirm = { name ->
                mainViewModel.updatePresetUi { it -> it.copy(presetSaveDialogVisible = (false)) }
                saveCurrentAsPreset(name)
            },
            onSaveDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetSaveDialogVisible = (false)) } },
            renameTarget = mainViewModel.presetUi.value.presetRenameTarget,
            onRenameConfirm = { id, name ->
                mainViewModel.updatePresetUi { it -> it.copy(presetRenameTarget = (null)) }
                renamePreset(id, name)
            },
            onRenameDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetRenameTarget = (null)) } },
            actionMenuTarget = mainViewModel.presetUi.value.presetActionMenuTarget,
            actionMenuBusy = mainViewModel.shell.value.isBusy,
            onActionMenuDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetActionMenuTarget = (null)) } },
            onActionApply = { applyPreset(it) },
            onActionPreview = { openBatchPreviewForPreset(it) },
            onActionOverwrite = { overwritePreset(it) },
            onActionRename = { mainViewModel.updatePresetUi { v -> v.copy(presetRenameTarget = (it)) } },
            onActionExportSingle = { exportSinglePresetToClipboard(it) },
            onActionDelete = { mainViewModel.updatePresetUi { v -> v.copy(presetDeleteConfirmTarget = (it)) } },
            deleteConfirmTarget = mainViewModel.presetUi.value.presetDeleteConfirmTarget,
            onDeleteDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetDeleteConfirmTarget = (null)) } },
            onDeleteConfirm = { deletePreset(it) },
            importDialogVisible = mainViewModel.presetUi.value.presetImportDialogVisible,
            onImportConfirm = { text -> importPresetsFromText(text) },
            onImportDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetImportDialogVisible = (false)) } },
            batchPreviewConfirmTarget = mainViewModel.presetUi.value.presetBatchPreviewConfirmTarget,
            onBatchPreviewConfirm = {
                mainViewModel.updatePresetUi { it -> it.copy(presetBatchPreviewConfirmTarget = (null)) }
                startBatchPreview(it)
            },
            onBatchPreviewConfirmDismiss = { mainViewModel.updatePresetUi { it -> it.copy(presetBatchPreviewConfirmTarget = (null)) } },
            batchPreviewProgress = mainViewModel.presetUi.value.batchPreviewProgress,
            onCancelBatchPreview = { mainViewModel.updatePresetUi { it -> it.copy(batchPreviewCancelled = (true)) } },
            showRefreshConfirm = mainViewModel.presetUi.value.showBatchPreviewRefreshConfirm,
            refreshConfirmPreset = mainViewModel.presetUi.value.activeBatchPreviewPreset ?: mainViewModel.presetUi.value.batchPreviewResult?.preset,
            batchPreviewCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
            onRefreshConfirm = {
                mainViewModel.updatePresetUi { it -> it.copy(showBatchPreviewRefreshConfirm = (false)) }
                startBatchPreview(it)
            },
            onRefreshDismiss = { mainViewModel.updatePresetUi { it -> it.copy(showBatchPreviewRefreshConfirm = (false)) } },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetDialogs.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PresetBatchPreviewConfirmDialog(
        preset: TuningPreset,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
    ) =
        PresetBatchPreviewConfirmDialog(
            preset = preset,
            batchPreviewCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetDialogs.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PresetBatchPreviewProgressDialog() =
        PresetBatchPreviewProgressDialog(
            progress = mainViewModel.presetUi.value.batchPreviewProgress,
            onCancel = { mainViewModel.updatePresetUi { it -> it.copy(batchPreviewCancelled = (true)) } },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetDialogs.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun BatchPreviewRefreshConfirmDialog(
        preset: TuningPreset,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
    ) =
        BatchPreviewRefreshConfirmDialog(
            preset = preset,
            batchPreviewCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )



    /**
     * JSON 参数编辑器：全部调参参数以类型化 JSON 呈现。
     * 左侧滑块修改后此处自动刷新；也可以直接编辑文本，点「保存并应用」生效。
     */
    // 重构期间保留：委托到 ui/pages/settings/SettingsJsonCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun JsonSettingsEditor() =
        JsonSettingsEditor(
            currentParams = currentTuningParams(),
            draftText = draftJsonParamsText,
            onDraftChange = { draftJsonParamsText = it },
            onSave = { saveJsonParamsFromText(it) },
            onRestore = {
                draftJsonParamsText = currentTuningParams().toJson().toString(4)
                mainViewModel.updateShell { it -> it.copy(statusText = ("已恢复为当前参数 JSON")) }
            },
        )


    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun RootWriteConfirmDialog() =
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
                    saveUiState()
                }
                onConfirm()
            },
        )






    // 重构期间保留：委托到 ui/pages/home/HomeStatusCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun GenerationActionCard(selectedApp: AppEntry?) =
        GenerationActionCard(
            selectedApp = selectedApp,
            isBusy = mainViewModel.shell.value.isBusy,
            onLocalGenerate = { generateSelected(installWithRoot = false, useGpt = false) },
            onLocalExport = { exportSelectedToExternal() },
            onWriteAll = { writeSelectedWithRoot(rootWriteMode = RootWriteMode.All) },
            onWriteStandard = { writeSelectedWithRoot(rootWriteMode = RootWriteMode.StandardOnly) },
            onWriteMono = { writeSelectedWithRoot(rootWriteMode = RootWriteMode.MonochromeOnly) },
        )








    // 重构期间保留：委托到 ui/pages/settings/SettingsAppCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun GptSettingsCard() =
        GptSettingsCard(
            tuningState = mainViewModel.params.collectAsState().value,
            isBusy = mainViewModel.shell.value.isBusy,
            gptModelId = mainViewModel.gptRmbgSettings.value.gptModelId,
            gptBaseUrl = mainViewModel.gptRmbgSettings.value.gptBaseUrl,
            gptApiKey = mainViewModel.gptRmbgSettings.value.gptApiKey,
            gptRunCount = mainViewModel.presetUi.value.gptRunCount,
            onGptImageModeChange = { mode ->
                mainViewModel.updateLive { p -> p.copy(gptImageMode = (mode).value) }
                mainViewModel.updateGptRmbgSettings { it -> it.copy(gptSettingsSaveStatus = ("")) }
            },
            onGptPromptPresetChange = { preset ->
                mainViewModel.updateLive { p -> p.copy(gptPromptPreset = (preset).value) }
                mainViewModel.updateGptRmbgSettings { it -> it.copy(gptSettingsSaveStatus = ("")) }
            },
            onGptCustomPromptChange = {
                mainViewModel.updateLive { p -> p.copy(gptCustomPrompt = it) }
                mainViewModel.updateGptRmbgSettings { it -> it.copy(gptSettingsSaveStatus = ("")) }
            },
            onGptModelIdChange = {
                mainViewModel.updateGptRmbgSettings { v -> v.copy(gptModelId = (it)) }
                mainViewModel.updateGptRmbgSettings { it -> it.copy(gptSettingsSaveStatus = ("")) }
            },
            onGptBaseUrlChange = {
                mainViewModel.updateGptRmbgSettings { v -> v.copy(gptBaseUrl = (it)) }
                mainViewModel.updateGptRmbgSettings { it -> it.copy(gptSettingsSaveStatus = ("")) }
            },
            onGptApiKeyChange = {
                mainViewModel.updateGptRmbgSettings { v -> v.copy(gptApiKey = (it)) }
                mainViewModel.updateGptRmbgSettings { it -> it.copy(gptSettingsSaveStatus = ("")) }
            },
        )

    // 重构期间保留：委托到 ui/pages/settings/SettingsAppCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun RmbgComponentCard() =
        RmbgComponentCard(
            component = remember(mainViewModel.gptRmbgSettings.value.rmbgComponentStatus) { findRmbgComponent() },
            rmbgRunCount = mainViewModel.presetUi.value.rmbgRunCount,
            currentPreset = currentRmbgModelPreset(),
            allPresets = RMBG_MODEL_PRESETS,
            lastError = mainViewModel.previewSession.value.lastRmbgCandidateError,
            componentUrl = mainViewModel.gptRmbgSettings.value.rmbgComponentUrl,
            isBusy = mainViewModel.shell.value.isBusy,
            isGenerating = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
            isInstalling = mainViewModel.previewSession.value.isInstallingRmbgComponent,
            installStage = mainViewModel.previewSession.value.rmbgInstallStage,
            installProgress = mainViewModel.previewSession.value.rmbgInstallProgress,
            dialogVisible = mainViewModel.previewSession.value.rmbgDialogVisible,
            onPresetSelected = { updateRmbgModelPreset(it) },
            onComponentUrlChange = {
                mainViewModel.updateGptRmbgSettings { v -> v.copy(rmbgComponentUrl = (it)) }
                mainViewModel.updateGptRmbgSettings { it -> it.copy(rmbgComponentSaveStatus = ("")) }
            },
            onDialogVisibleChange = { mainViewModel.updatePreviewSession { v -> v.copy(rmbgDialogVisible = (it)) } },
            onPickZip = {
                chooseRmbgComponentLauncher.launch(
                    arrayOf("application/zip", "application/octet-stream", "*/*"),
                )
            },
            onInstallFromUrl = { installRmbgComponentFromUrl() },
            onClearInstalled = { clearInstalledRmbgComponent() },
        )


    // Slice 1.5 已搬入 system/ExportManager.kt：formatTreeUriDisplay（纯函数，同包直接用）。

    // 重构期间保留：委托到 ui/pages/settings/SettingsAppCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun OutputCard() =
        OutputCard(
            autoConfirmRootWrite = mainViewModel.confirm.value.autoConfirmRootWrite,
            autoConfirmRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
            isBusy = mainViewModel.shell.value.isBusy,
            outputTreeUri = mainViewModel.shell.value.outputTreeUri,
            treeDisplay = remember(mainViewModel.shell.value.outputTreeUri) { formatTreeUriDisplay(mainViewModel.shell.value.outputTreeUri) },
            backupActive = backupJob?.isActive == true && mainViewModel.transfer.value.backupProgress != null,
            backupInBackground = mainViewModel.transfer.value.backupInBackground,
            backupDots = mainViewModel.transfer.value.backupBackgroundDots,
            exportDialogVisible = mainViewModel.previewSession.value.exportDialogVisible,
            onAutoConfirmRootWriteChange = {
                mainViewModel.updateConfirm { v -> v.copy(autoConfirmRootWrite = (it)) }
                saveUiState()
                mainViewModel.updateShell { it -> it.copy(statusText = (if (mainViewModel.confirm.value.autoConfirmRootWrite) "已开启自动确认写入" else "已关闭自动确认写入")) }
            },
            onAutoConfirmRefreshChange = {
                mainViewModel.updateConfirm { v -> v.copy(autoConfirmRefresh = (it)) }
                saveUiState()
                mainViewModel.updateShell { it -> it.copy(statusText = (if (mainViewModel.confirm.value.autoConfirmRefresh) "已开启自动确认刷新" else "已关闭自动确认刷新")) }
            },
            onBackupRowClick = {
                val active = backupJob?.isActive == true && mainViewModel.transfer.value.backupProgress != null
                val inBg = mainViewModel.transfer.value.backupInBackground && active
                if (inBg || (active && mainViewModel.transfer.value.backupSheetVisible.not())) {
                    mainViewModel.updateTransfer { it -> it.copy(backupInBackground = (false)) }
                    mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (true)) }
                    stopBackupDotAnimation()
                } else if (active) {
                    mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (true)) }
                } else {
                    mainViewModel.updatePreviewSession { it -> it.copy(exportDialogVisible = (true)) }
                }
            },
            onBackupBackgroundActiveChanged = { inBg ->
                if (inBg) startBackupDotAnimation() else stopBackupDotAnimation()
            },
            onExportDialogDismiss = { mainViewModel.updatePreviewSession { it -> it.copy(exportDialogVisible = (false)) } },
            onChooseTree = { chooseTreeLauncher.launch(null) },
            onBackupAll = { backupAllToExternal() },
        )

    // 重构期间保留：委托到 system/ExportManager.kt 显式参数版本，调用点零改动。
    internal fun exportCurrentToExternal() =
        exportCurrentToExternal(onBackupAll = { backupAllToExternal() })

    // 重构期间保留：委托到 ui/pages/settings/SettingsAppCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PreviewStripSettingsCard() =
        PreviewStripSettingsCard(
            enabled = mainViewModel.previewSession.value.previewStripEnabled,
            isBusy = mainViewModel.shell.value.isBusy,
            onCheckedChange = { updatePreviewStripEnabled(it) },
        )

    // 重构期间保留：委托到 ui/pages/settings/SettingsGlassCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun LiquidGlassToggleRow() =
        LiquidGlassToggleRow(
            enabled = mainViewModel.params.collectAsState().value.liquidGlassEnabled,
            isBusy = mainViewModel.shell.value.isBusy,
            onToggle = { updateLiquidGlassEnabled(!mainViewModel.params.value.liquidGlassEnabled) },
        )



    // Slice 2.3 已搬入 ui/pages/settings/SettingsAppCards.kt：InputSettingsCard（纯 UI，直接搬迁，不留 wrapper）。
    // 重构期间保留：委托到 ui/pages/settings/SettingsAppCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun ShowSystemAppsToggleRow() =
        ShowSystemAppsToggleRow(
            checked = mainViewModel.picker.value.showSystemApps,
            isBusy = mainViewModel.shell.value.isBusy,
            onToggle = {
                mainViewModel.updatePicker { it -> it.copy(showSystemApps = (!mainViewModel.picker.value.showSystemApps)) }
                saveUiState()
            },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun AppPickerStatusCard(
        filteredCount: Int,
        totalCount: Int,
        generatedCount: Int,
        ungeneratedCount: Int,
    ) =
        AppPickerStatusCard(
            filteredCount = filteredCount,
            totalCount = totalCount,
            generatedCount = generatedCount,
            ungeneratedCount = ungeneratedCount,
            multiCount = mainViewModel.picker.value.multiSelectedPackageNames.size,
            isScanning = mainViewModel.picker.value.isScanningGeneratedPackages,
            scanFailed = mainViewModel.picker.value.generatedScanFailed,
            isBusy = mainViewModel.shell.value.isBusy,
            hasApps = apps.isNotEmpty(),
            onRefreshGenerated = { refreshGeneratedPackages() },
            onReloadApps = { loadApps() },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun AppPickerFilterCard() =
        AppPickerFilterCard(
            showSystemApps = mainViewModel.picker.value.showSystemApps,
            generatedFilter = mainViewModel.picker.value.generatedFilter,
            isBusy = mainViewModel.shell.value.isBusy,
            onToggleSystemApps = {
                mainViewModel.updatePicker { it -> it.copy(showSystemApps = (!mainViewModel.picker.value.showSystemApps)) }
                saveUiState()
            },
            onFilterSelected = {
                mainViewModel.updatePicker { v -> v.copy(generatedFilter = (it)) }
                mainViewModel.updatePicker { it -> it.copy(queryText = ("")) }
                saveUiState()
            },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun AppPickerSearchCard(filteredApps: List<AppEntry>) =
        AppPickerSearchCard(
            queryText = mainViewModel.picker.value.queryText,
            isBusy = mainViewModel.shell.value.isBusy,
            onQueryChange = {
                mainViewModel.updatePicker { v -> v.copy(queryText = (it)) }
                saveUiState()
            },
            onClearQuery = {
                mainViewModel.updatePicker { it -> it.copy(queryText = ("")) }
                saveUiState()
            },
            multiSelectContent = { AppMultiSelectActions(filteredApps) },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun AppPickerControlsCard(
        filteredCount: Int,
        totalCount: Int,
        generatedCount: Int,
        ungeneratedCount: Int,
        filteredApps: List<AppEntry>,
    ) =
        AppPickerControlsCard(
            filteredCount = filteredCount,
            totalCount = totalCount,
            generatedCount = generatedCount,
            ungeneratedCount = ungeneratedCount,
            multiCount = mainViewModel.picker.value.multiSelectedPackageNames.size,
            isScanning = mainViewModel.picker.value.isScanningGeneratedPackages,
            scanFailed = mainViewModel.picker.value.generatedScanFailed,
            isBusy = mainViewModel.shell.value.isBusy,
            hasApps = apps.isNotEmpty(),
            showSystemApps = mainViewModel.picker.value.showSystemApps,
            generatedFilter = mainViewModel.picker.value.generatedFilter,
            queryText = mainViewModel.picker.value.queryText,
            onRefreshGenerated = { refreshGeneratedPackages() },
            onReloadApps = { loadApps() },
            onToggleSystemApps = {
                mainViewModel.updatePicker { it -> it.copy(showSystemApps = (!mainViewModel.picker.value.showSystemApps)) }
                saveUiState()
            },
            onFilterSelected = {
                mainViewModel.updatePicker { v -> v.copy(generatedFilter = (it)) }
                mainViewModel.updatePicker { it -> it.copy(queryText = ("")) }
                saveUiState()
            },
            onQueryChange = {
                mainViewModel.updatePicker { v -> v.copy(queryText = (it)) }
                saveUiState()
            },
            onClearQuery = {
                mainViewModel.updatePicker { it -> it.copy(queryText = ("")) }
                saveUiState()
            },
            multiSelectContent = { AppMultiSelectActions(filteredApps) },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun AppMultiSelectActions(filteredApps: List<AppEntry>) {
        val filteredPackageNames = remember(filteredApps) { filteredApps.map { it.packageName }.toSet() }
        AppMultiSelectActions(
            selectedCount = mainViewModel.picker.value.multiSelectedPackageNames.size,
            hasFiltered = filteredPackageNames.isNotEmpty(),
            allFilteredSelected = pickerAllFilteredSelected(filteredPackageNames, mainViewModel.picker.value.multiSelectedPackageNames),
            isBusy = mainViewModel.shell.value.isBusy,
            onToggleFiltered = {
                val allSelected = pickerAllFilteredSelected(filteredPackageNames, mainViewModel.picker.value.multiSelectedPackageNames)
                mainViewModel.updatePicker { it -> it.copy(multiSelectedPackageNames = (if (allSelected) {
                    mainViewModel.picker.value.multiSelectedPackageNames - filteredPackageNames
                } else {
                    mainViewModel.picker.value.multiSelectedPackageNames + filteredPackageNames
                })) }
            },
            onClear = { mainViewModel.updatePicker { it -> it.copy(multiSelectedPackageNames = (emptySet())) } },
            onAddGlass = { addLiquidGlassToMultiSelectedGenerated() },
            onApplyPreset = { applyCurrentPresetBatch() },
        )
    }

    // 重构期间保留：委托到 ui/pages/picker/PickerCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun AppRow(
        entry: AppEntry,
        selected: Boolean,
        multiSelected: Boolean,
        generated: Boolean,
        onClick: () -> Unit,
        onToggleMultiSelect: () -> Unit,
    ) =
        AppRow(
            entry = entry,
            selected = selected,
            multiSelected = multiSelected,
            generated = generated,
            isBusy = mainViewModel.shell.value.isBusy,
            onClick = onClick,
            onToggleMultiSelect = onToggleMultiSelect,
            icon = {
                AppIcon(
                    entry = entry,
                    size = 48.dp,
                )
            },
        )
















    // 重构期间保留：委托到 ui/pages/picker/PickerCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun AppIcon(entry: AppEntry, size: Dp) =
        AppIcon(
            entry = entry,
            size = size,
            getCached = { key -> getCachedAppIcon(appIconCache, key) },
            loadIcon = { loadCachedAppIcon(entry) },
        )


    // 重构期间保留：委托到 state/AppLoadOps.kt MainViewModel 显式参数版本，调用点零改动。
    internal suspend fun loadCachedAppIcon(entry: AppEntry): Bitmap? =
        mainViewModel.loadCachedAppIconOp(
            entry = entry,
            iconCache = appIconCache,
            pm = packageManager,
            cacheSize = ICON_CACHE_SIZE,
        )


    @Volatile
    private var cachedCustomWallpaper: Bitmap? = null
    @Volatile
    private var cachedCustomWallpaperPath: String? = null
    @Volatile
    private var cachedSystemWallpaper: Bitmap? = null
    @Volatile
    private var cachedBundledWallpaper: Bitmap? = null

    /**
     * 读取当前设备桌面壁纸并保留原始宽高比（短边缩放到 480 左右）。
     * 静态壁纸经 ImageWallpaper 暴露为 BitmapDrawable，直接取位图，无需任何权限；
     * 失败返回 null，调用方走内置图兜底。
     */
    // 重构期间保留：委托到 ui/pages/picker/PickerIo.kt 显式参数版本，调用点零改动。
    internal fun loadPreviewWallpaperBitmap(): Bitmap? =
        pickerLoadPreviewWallpaperBitmap(
            getCached = { cachedSystemWallpaper },
            setCached = { cachedSystemWallpaper = it },
            loadDrawable = { pickerSystemWallpaperDrawable(WallpaperManager.getInstance(this)) },
            shortEdge = PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE,
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerIo.kt 显式参数版本，调用点零改动。
    internal fun loadBundledPreviewWallpaperBitmap(): Bitmap? =
        pickerLoadBundledPreviewWallpaperBitmap(
            getCached = { cachedBundledWallpaper },
            setCached = { cachedBundledWallpaper = it },
            resources = resources,
            resId = R.drawable.preview_wallpaper,
            shortEdge = PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE,
        )

    /**
     * 导入用户上传的壁纸：只居中裁剪为 16:9，不做任何缩放压缩，避免变形；
     * 以 PNG 无损存档到私有目录，「桌面」背景优先使用。
     */
    // 重构期间保留：委托到 ui/pages/picker/PickerIo.kt 显式参数版本，调用点零改动。
    internal fun importCustomWallpaper(uri: Uri) =
        pickerImportCustomWallpaper(
            uri = uri,
            isBusy = mainViewModel.shell.value.isBusy,
            onStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onLaunch = { name, block -> startUiFriendlyThread(name, block) },
            openInputBytes = { u -> contentResolver.openInputStream(u)?.use { it.readBytes() } },
            filesDir = filesDir,
            fileName = CUSTOM_WALLPAPER_FILE,
            onSuccess = { path, info ->
                runOnUiThread {
                    cachedCustomWallpaper = null
                    cachedCustomWallpaperPath = null
                    mainViewModel.updateBatchPreviewConfig { it -> it.copy(customWallpaperPath = (path)) }
                    mainViewModel.updateBatchPreviewConfig { it -> it.copy(customWallpaperInfo = (info)) }
                    mainViewModel.updateShell { it -> it.copy(statusText = ("已导入自定义壁纸（$info），「桌面」背景优先使用此图")) }
                    saveUiState()
                }
            },
            onError = ::status,
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerIo.kt 显式参数版本，调用点零改动。
    internal fun clearCustomWallpaper() =
        pickerClearCustomWallpaper(
            filesDir = filesDir,
            customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
            fileName = CUSTOM_WALLPAPER_FILE,
            onCleared = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onSave = ::saveUiState,
            clearCache = {
                cachedCustomWallpaper = null
                cachedCustomWallpaperPath = null
            },
            setPath = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(customWallpaperPath = (it)) } },
            setInfo = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(customWallpaperInfo = (it)) } },
        )

    // 纯函数 centerCropToSixteenNine 已直接搬迁至 ui/pages/picker/PickerIo.kt，同包直接引用，不留 wrapper。

    // 重构期间保留：委托到 ui/pages/picker/PickerIo.kt 显式参数版本，调用点零改动。
    internal fun loadCustomWallpaperBitmap(): Bitmap? =
        pickerLoadCustomWallpaperBitmap(
            path = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
            cachedPath = cachedCustomWallpaperPath,
            getCached = { cachedCustomWallpaper },
            setCached = { path, bitmap ->
                cachedCustomWallpaper = bitmap
                cachedCustomWallpaperPath = path
            },
            shortEdge = PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE,
        )




    // 重构期间保留：委托到 state/AppLoadOps.kt MainViewModel 显式参数版本，调用点零改动。
    internal fun loadApps(refreshGenerated: Boolean = false) =
        mainViewModel.requestAppLoad(
            refreshGenerated = refreshGenerated,
            pm = packageManager,
            iconCache = appIconCache,
            cacheSize = ICON_CACHE_SIZE,
            preloadCount = PRELOAD_ICON_COUNT,
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            markLoad = { didRequestAppLoad = true },
            onRefreshPermissions = { refreshPermissionState() },
            applyEntries = { loaded ->
                androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
                    apps.clear()
                    apps.addAll(loaded)
                }
            },
        )

    // 重构期间保留：委托到 state/AppLoadOps.kt MainViewModel 显式参数版本，调用点零改动。
    internal fun refreshGeneratedPackages(entries: List<AppEntry> = apps.toList()) =
        mainViewModel.refreshGeneratedPackagesAsync(
            entries = entries,
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
        )




    // 重构期间保留：委托到 ui/pages/picker/PickerAppOps.kt 显式参数版本，调用点零改动。
    internal fun loadUiState() =
        pickerLoadUiState(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            persistedReadWriteUri = contentResolver.persistedUriPermissions.firstOrNull { it.isReadPermission && it.isWritePermission }?.uri,
            setSelectedPackage = { mainViewModel.updatePicker { v -> v.copy(selectedPackageName = (it)) } },
            setGeneratedFilter = { mainViewModel.updatePicker { v -> v.copy(generatedFilter = (it)) } },
            setShowSystemApps = { mainViewModel.updatePicker { v -> v.copy(showSystemApps = (it)) } },
            setQueryText = { mainViewModel.updatePicker { v -> v.copy(queryText = (it)) } },
            setAdvancedCategory = { mainViewModel.updateShell { v -> v.copy(advancedSettingsCategory = (it)) } },
            setAdvancedTab = { mainViewModel.updateShell { v -> v.copy(advancedSettingsTab = (it)) } },
            setPreviewPackage = { mainViewModel.updatePreviewSession { v -> v.copy(previewPackageName = (it)) } },
            setPreviewDir = { mainViewModel.updatePreviewSession { v -> v.copy(previewDirPath = (it)) } },
            setPreviewStrip = { mainViewModel.updatePreviewSession { v -> v.copy(previewStripEnabled = (it)) } },
            updateLiveSelections = { selections ->
                mainViewModel.updateLive { p ->
                    p.copy(
                        previewNormalLight = selections.normalLight.name,
                        previewNormalDark = selections.normalDark.name,
                        previewMonochromeLight = selections.monochromeLight.name,
                        previewMonochromeDark = selections.monochromeDark.name,
                    )
                }
            },
            setDesktopBackground = { mainViewModel.updatePreviewSession { v -> v.copy(previewDesktopBackground = (it)) } },
            setIconSize = { mainViewModel.updatePreviewSession { v -> v.copy(previewIconSizeDp = (it)) } },
            setDraftIconSizeText = { draftPreviewIconSizeDpText = it },
            setCornerRadius = { mainViewModel.updatePreviewSession { v -> v.copy(previewCornerRadiusDp = (it)) } },
            setDraftCornerRadiusText = { draftPreviewCornerRadiusDpText = it },
            setBatchCount = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(batchPreviewCount = (it)) } },
            setDraftBatchCountText = { draftBatchPreviewCountText = it },
            setBatchColumns = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(batchPreviewColumns = (it)) } },
            setDraftBatchColumnsText = { draftBatchPreviewColumnsText = it },
            setBatchIconSize = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(batchPreviewIconSizeDp = (it)) } },
            setDraftBatchIconSizeText = { draftBatchPreviewIconSizeDpText = it },
            setBatchCorner = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(batchPreviewCornerRadiusDp = (it)) } },
            setDraftBatchCornerText = { draftBatchPreviewCornerRadiusDpText = it },
            setBatchDesktopBg = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(batchPreviewDesktopBackground = (it)) } },
            setCustomPath = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(customWallpaperPath = (it)) } },
            setCustomInfo = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(customWallpaperInfo = (it)) } },
            setAutoRoot = { mainViewModel.updateConfirm { v -> v.copy(autoConfirmRootWrite = (it)) } },
            setAutoRefresh = { mainViewModel.updateConfirm { v -> v.copy(autoConfirmRefresh = (it)) } },
            setOutputUri = { mainViewModel.updateShell { v -> v.copy(outputTreeUri = (it)) } },
            setOnboardingVisible = { mainViewModel.updateShell { v -> v.copy(onboardingVisible = (it)) } },
            parseUri = { runCatching { Uri.parse(it) }.getOrNull() },
            isFile = ::pickerIsCustomWallpaperFile,
            decodeBounds = ::pickerDecodeWallpaperBounds,
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerAppOps.kt 显式参数版本，调用点零改动。
    internal fun saveUiState() =
        pickerSaveUiState(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
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

    // 重构期间保留：委托到 state/AppLoadOps.kt MainViewModel 显式参数版本，调用点零改动。
    internal fun refreshArtPlusIcons() =
        mainViewModel.refreshArtPlusIconsAsync(
            contentResolver = contentResolver,
            apkPath = applicationInfo.sourceDir,
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerAppOps.kt 显式参数版本，调用点零改动。
    internal fun refreshPermissionState() =
        pickerRefreshPermissionState(
            checkQueryPermission = { pickerCheckQueryPermission(packageManager, packageName) },
            hasUsage = ::hasUsageAccess,
            onResult = { queryGranted, usageGranted ->
                mainViewModel.updatePicker { it -> it.copy(packageListPermissionGranted = (queryGranted)) }
                mainViewModel.updatePicker { it -> it.copy(usageAccessGranted = (usageGranted)) }
            },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerAppOps.kt 显式参数版本，调用点零改动。
    internal fun requestDeclaredPermissions() =
        pickerRequestDeclaredPermissions(
            needsQuery = pickerNeedsQueryPermission(packageManager, packageName),
            launcher = { permissionLauncher.launch(it) },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerAppOps.kt 显式参数版本，调用点零改动。
    internal fun requestSpecialPermissionsOnce() =
        pickerRequestSpecialPermissionsOnce(
            usageGranted = mainViewModel.picker.value.usageAccessGranted,
            prompted = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(PREF_USAGE_PERMISSION_PROMPTED, false),
            markPrompted = {
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit().putBoolean(PREF_USAGE_PERMISSION_PROMPTED, true).apply()
            },
            postOnDecor = { window.decorView.post(it) },
            hasUsage = ::hasUsageAccess,
            openSettings = ::openUsageAccessSettings,
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    internal fun openAppPermissionSettings() =
        pickerOpenAppPermissionSettings(
            start = ::startActivity,
            packageName = packageName,
            onError = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    internal fun openUsageAccessSettings() =
        pickerOpenUsageAccessSettings(
            start = ::startActivity,
            onError = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    internal fun openExternalLink(url: String) =
        pickerOpenExternalLink(
            start = ::startActivity,
            url = url,
            onError = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    @Suppress("DEPRECATION")
    internal fun currentVersionName(): String =
        pickerCurrentVersionName(
            getVersionName = { packageManager.getPackageInfo(packageName, 0).versionName },
        )

    // 纯函数 isNewerVersion 已直接搬迁至 ui/pages/picker/PickerCommon.kt，同包直接引用，不留 wrapper。

    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    internal fun checkForUpdate() =
        pickerCheckForUpdate(
            isChecking = mainViewModel.updateUi.value.isCheckingUpdate,
            onCheckingChange = { mainViewModel.updateUpdateUi { v -> v.copy(isCheckingUpdate = (it)) } },
            onStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            scope = mainScope,
            resolveUrl = { pickerResolveUpdateUrl(it, "检查更新", isDebugBuild()) },
            fetchLatest = ::pickerFetchUpdateBody,
            currentVersion = currentVersionName(),
            onUpdateAvailable = { info, text ->
                mainViewModel.updateUpdateUi { it -> it.copy(updateAvailableInfo = (info)) }
                mainViewModel.updateShell { it -> it.copy(statusText = (text)) }
            },
            onUpToDate = {
                mainViewModel.updateUpdateUi { it -> it.copy(updateUpToDateDialogVisible = (true)) }
                mainViewModel.updateShell { v -> v.copy(statusText = (it)) }
            },
            onFailed = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    internal fun hasUsageAccess(): Boolean =
        pickerHasUsageAccess(
            appOps = getSystemService(AppOpsManager::class.java),
            uid = Process.myUid(),
            packageName = packageName,
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    internal fun getApplicationInfoCompat(packageName: String): ApplicationInfo =
        pickerGetApplicationInfoCompat(
            pm = packageManager,
            packageName = packageName,
        )

    // 重构期间保留：委托到 system/DebugServer.kt 显式参数版本，调用点零改动。
    internal fun isDebugGenerateIntent(intent: Intent?): Boolean =
        isDebugGenerateIntent(intent, ::isDebugTokenValid)

    // 重构期间保留：委托到 system/DebugServer.kt 显式参数版本，调用点零改动。
    internal fun handleDebugGenerateIntent(intent: Intent?) =
        handleDebugGenerateIntent(intent, ::isDebugBuild, ::isDebugTokenValid, ::startDebugGeneration)

    // 重构期间保留：委托到 system/DebugServer.kt 显式参数版本，调用点零改动。
    internal fun startDebugGeneration(
        packageName: String,
        useGpt: Boolean,
        installWithRoot: Boolean,
        debugMode: LocalSeparationMode,
        rootWriteMode: RootWriteMode,
    ): Boolean =
        startDebugGeneration(
            packageName = packageName,
            useGpt = useGpt,
            installWithRoot = installWithRoot,
            debugMode = debugMode,
            rootWriteMode = rootWriteMode,
            runOnMainSync = ::runOnMainSync,
            isBusyGet = { mainViewModel.shell.value.isBusy },
            setBusy = { mainViewModel.updateShell { v -> v.copy(isBusy = (it)) } },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onStatus = ::status,
            getAppInfo = ::getApplicationInfoCompat,
            packageManager = packageManager,
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            getGeneratedNames = { mainViewModel.picker.value.generatedPackageNames },
            setGeneratedNames = { mainViewModel.updatePicker { v -> v.copy(generatedPackageNames = (it)) } },
            generatePackage = ::generateArtPlusPackage,
            setActiveSession = { mainViewModel.updatePreviewSession { v -> v.copy(activeGenerationSession = (it)) } },
            updateSelections = { selections ->
                mainViewModel.updateLive { p ->
                    p.copy(
                        previewNormalLight = selections.normalLight.name,
                        previewNormalDark = selections.normalDark.name,
                        previewMonochromeLight = selections.monochromeLight.name,
                        previewMonochromeDark = selections.monochromeDark.name,
                    )
                }
            },
            setPreviewChoiceMode = { mainViewModel.updatePreviewSession { v -> v.copy(previewChoiceMode = (it)) } },
            setPreviewPackage = { mainViewModel.updatePreviewSession { v -> v.copy(previewPackageName = (it)) } },
            setPreviewDir = { mainViewModel.updatePreviewSession { v -> v.copy(previewDirPath = (it)) } },
            bumpPreviewVersion = { mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) } },
            onSaveUiState = ::saveUiState,
        )

    // 重构期间保留：委托到 system/DebugServer.kt 显式参数版本，调用点零改动。
    internal fun debugInspectPackage(packageName: String, includeRmbg: Boolean): JSONObject =
        debugInspectPackage(
            packageName = packageName,
            includeRmbg = includeRmbg,
            getAppInfo = ::getApplicationInfoCompat,
            packageManager = packageManager,
            externalLabDir = getExternalFilesDir("ArtPlusLab"),
            filesDir = filesDir,
            tuning = currentTuningParams(),
            runOnMainSync = ::runOnMainSync,
            setLastReport = { mainViewModel.updatePreviewSession { v -> v.copy(lastRmbgInferenceReport = (it)) } },
            setLastError = { mainViewModel.updatePreviewSession { v -> v.copy(lastRmbgCandidateError = (it)) } },
            buildRmbgDebug = ::buildRmbgDebugCandidate,
            describeFailure = ::describeRmbgFailure,
            renderForeground = ::renderCandidateForeground,
            monochromeFor = ::monochromeForCandidate,
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsGptRmbg.kt 显式参数版本，调用点零改动。
    internal fun loadGptSettings(): Unit =
        paramsLoadGptSettings(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            updateLive = mainViewModel::updateLive,
            setGptModelId = { mainViewModel.updateGptRmbgSettings { v -> v.copy(gptModelId = (it)) } },
            setGptBaseUrl = { mainViewModel.updateGptRmbgSettings { v -> v.copy(gptBaseUrl = (it)) } },
            setGptApiKey = { mainViewModel.updateGptRmbgSettings { v -> v.copy(gptApiKey = (it)) } },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsGptRmbg.kt 显式参数版本，调用点零改动。
    internal fun saveGptSettings(): Boolean =
        paramsSaveGptSettings(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            getParams = { mainViewModel.params.value },
            getGptApiKey = { mainViewModel.gptRmbgSettings.value.gptApiKey },
            getGptModelId = { mainViewModel.gptRmbgSettings.value.gptModelId },
            getGptBaseUrl = { mainViewModel.gptRmbgSettings.value.gptBaseUrl },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsGptRmbg.kt 显式参数版本，调用点零改动。
    internal fun saveSettingsPage(): Unit =
        paramsSaveSettingsPage(
            saveGpt = { saveGptSettings() },
            saveRmbg = { saveRmbgSettings() },
            saveLocalSeparation = { saveLocalSeparationSettings() },
            saveImageTuning = { saveImageTuningSettings() },
            saveLiquidGlass = { saveLiquidGlassSettings() },
            saveUi = { saveUiState() },
            setGptSaveStatus = { mainViewModel.updateGptRmbgSettings { v -> v.copy(gptSettingsSaveStatus = (it)) } },
            setRmbgSaveStatus = { mainViewModel.updateGptRmbgSettings { v -> v.copy(rmbgComponentSaveStatus = (it)) } },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsCrypto.kt 显式参数版本，调用点零改动。
    internal fun loadGptApiKey(prefs: android.content.SharedPreferences): String =
        paramsLoadGptApiKey(prefs)

    // 重构期间保留：委托到 ui/pages/params/ParamsCrypto.kt 显式参数版本，调用点零改动。
    internal fun encryptSecret(value: String): String =
        paramsEncryptSecret(value)

    // 重构期间保留：委托到 ui/pages/params/ParamsCrypto.kt 显式参数版本，调用点零改动。
    internal fun decryptSecret(value: String): String =
        paramsDecryptSecret(value)

    // 重构期间保留：委托到 ui/pages/params/ParamsCrypto.kt 显式参数版本，调用点零改动。
    internal fun gptSecretKey(): SecretKey =
        paramsGptSecretKey()

    // 重构期间保留：委托到 ui/pages/params/ParamsGptRmbg.kt 显式参数版本，调用点零改动。
    internal fun loadRmbgSettings(): Unit =
        paramsLoadRmbgSettings(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            setComponentUrl = { mainViewModel.updateGptRmbgSettings { v -> v.copy(rmbgComponentUrl = (it)) } },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsGptRmbg.kt 显式参数版本，调用点零改动。
    internal fun saveRmbgSettings(): Boolean =
        paramsSaveRmbgSettings(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            getComponentUrl = { mainViewModel.gptRmbgSettings.value.rmbgComponentUrl },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLocalSeparation.kt 显式参数版本，调用点零改动。
    internal fun loadLocalSeparationSettings(): Unit =
        paramsLoadLocalSeparationSettings(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            updateLive = mainViewModel::updateLive,
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLocalSeparation.kt 显式参数版本，调用点零改动。
    internal fun saveLocalSeparationSettings(): Unit =
        paramsSaveLocalSeparationSettings(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            getParams = { mainViewModel.params.value },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLocalSeparation.kt 显式参数版本，调用点零改动。
    internal fun updateLocalSeparationMode(mode: LocalSeparationMode): Unit =
        paramsUpdateLocalSeparationMode(
            mode = mode,
            getParams = { mainViewModel.params.value },
            updateLive = mainViewModel::updateLive,
            getSession = { mainViewModel.previewSession.value.activeGenerationSession },
            defaultPreviewChoiceForMode = ::defaultPreviewChoiceForMode,
            onSave = { saveLocalSeparationSettings() },
            onRefresh = { rebuild, retarget -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild, retargetFrom = retarget) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLocalSeparation.kt 显式参数版本，调用点零改动。
    internal fun updateLocalWorkflowToggle(name: String, enabled: Boolean): Unit =
        paramsUpdateLocalWorkflowToggle(
            name = name,
            enabled = enabled,
            getParams = { mainViewModel.params.value },
            updateLive = mainViewModel::updateLive,
            onSaveImageTuning = { saveImageTuningSettings() },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsImageTuning.kt 显式参数版本，调用点零改动。
    internal fun loadImageSettings(): Unit =
        paramsLoadImageSettings(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            updateLive = mainViewModel::updateLive,
            getParams = { mainViewModel.params.value },
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
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsImageTuning.kt 显式参数版本，调用点零改动。
    internal fun updateForegroundSubjectPercent(value: Int): Unit =
        paramsUpdateForegroundSubjectPercent(
            value = value,
            updateLive = mainViewModel::updateLive,
            getParams = { mainViewModel.params.value },
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            setDraftText = { draftForegroundSubjectPercentText = it },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsImageTuning.kt 显式参数版本，调用点零改动。
    internal fun updateForegroundShadowLevel(value: Int): Unit =
        paramsUpdateForegroundShadowLevel(
            value = value,
            updateLive = mainViewModel::updateLive,
            getParams = { mainViewModel.params.value },
            setDraftText = { draftForegroundShadowLevelText = it },
            onSave = { saveImageTuningSettings() },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsImageTuning.kt 显式参数版本，调用点零改动。
    internal fun migrateLegacyPercent(value: Int, fallback: Int): Int =
        paramsMigrateLegacyPercent(value = value, fallback = fallback)

    // 重构期间保留：委托到 ui/pages/params/ParamsImageTuning.kt 显式参数版本，调用点零改动。
    internal fun updateMonochromeThemeScalePercent(value: Int): Unit =
        paramsUpdateMonochromeThemeScalePercent(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftMonochromeThemeScaleText = it },
            onSave = { saveImageTuningSettings() },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsImageTuning.kt 显式参数版本，调用点零改动。
    internal fun updateBackgroundSeparationPercent(value: Int): Unit =
        paramsUpdateBackgroundSeparationPercent(
            value = value,
            updateLive = mainViewModel::updateLive,
            getParams = { mainViewModel.params.value },
            setDraftText = { draftBackgroundSeparationText = it },
            onSave = { saveImageTuningSettings() },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsImageTuning.kt 显式参数版本，调用点零改动。
    internal fun updatePlateRemovalPercent(value: Int): Unit =
        paramsUpdatePlateRemovalPercent(
            value = value,
            updateLive = mainViewModel::updateLive,
            getParams = { mainViewModel.params.value },
            setDraftText = { draftPlateRemovalText = it },
            onSave = { saveImageTuningSettings() },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsImageTuning.kt 显式参数版本，调用点零改动。
    internal fun updateShadowRemovalPercent(value: Int): Unit =
        paramsUpdateShadowRemovalPercent(
            value = value,
            updateLive = mainViewModel::updateLive,
            getParams = { mainViewModel.params.value },
            setDraftText = { draftShadowRemovalText = it },
            onSave = { saveImageTuningSettings() },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsImageTuning.kt 显式参数版本，调用点零改动。
    internal fun updateEdgePolishPercent(value: Int): Unit =
        paramsUpdateEdgePolishPercent(
            value = value,
            updateLive = mainViewModel::updateLive,
            getParams = { mainViewModel.params.value },
            setDraftText = { draftEdgePolishText = it },
            onSave = { saveImageTuningSettings() },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsImageTuning.kt 显式参数版本，调用点零改动。
    internal fun updateRmbgAlphaStrengthPercent(value: Int): Unit =
        paramsUpdateRmbgAlphaStrengthPercent(
            value = value,
            updateLive = mainViewModel::updateLive,
            getParams = { mainViewModel.params.value },
            setDraftText = { draftRmbgAlphaStrengthText = it },
            onSave = { saveImageTuningSettings() },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsImageTuning.kt 显式参数版本，调用点零改动。
    internal fun updateRmbgEdgeFeatherPercent(value: Int): Unit =
        paramsUpdateRmbgEdgeFeatherPercent(
            value = value,
            updateLive = mainViewModel::updateLive,
            getParams = { mainViewModel.params.value },
            setDraftText = { draftRmbgEdgeFeatherText = it },
            onSave = { saveImageTuningSettings() },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsImageTuning.kt 显式参数版本，调用点零改动。
    internal fun updateRmbgEdgeAdjustPercent(value: Int): Unit =
        paramsUpdateRmbgEdgeAdjustPercent(
            value = value,
            updateLive = mainViewModel::updateLive,
            getParams = { mainViewModel.params.value },
            setDraftText = { draftRmbgEdgeAdjustText = it },
            onSave = { saveImageTuningSettings() },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsImageTuning.kt 显式参数版本，调用点零改动。
    internal fun updateRmbgWeakAlphaKeepPercent(value: Int): Unit =
        paramsUpdateRmbgWeakAlphaKeepPercent(
            value = value,
            updateLive = mainViewModel::updateLive,
            getParams = { mainViewModel.params.value },
            setDraftText = { draftRmbgWeakAlphaKeepText = it },
            onSave = { saveImageTuningSettings() },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsGptRmbg.kt 显式参数版本，调用点零改动。
    internal fun currentRmbgModelPreset(): RmbgModelPreset =
        paramsCurrentRmbgModelPreset(componentUrl = mainViewModel.gptRmbgSettings.value.rmbgComponentUrl)

    // 重构期间保留：委托到 ui/pages/params/ParamsGptRmbg.kt 显式参数版本，调用点零改动。
    internal fun updateRmbgModelPreset(preset: RmbgModelPreset): Unit =
        paramsUpdateRmbgModelPreset(
            preset = preset,
            setComponentUrl = { mainViewModel.updateGptRmbgSettings { v -> v.copy(rmbgComponentUrl = (it)) } },
            setSaveStatus = { mainViewModel.updateGptRmbgSettings { v -> v.copy(rmbgComponentSaveStatus = (it)) } },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsGptRmbg.kt 显式参数版本，调用点零改动。
    internal fun rmbgInferenceStatusSummary(): String =
        paramsRmbgInferenceStatusSummary(
            isGenerating = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
            candidateStatusText = mainViewModel.previewSession.value.rmbgCandidateStatusText,
            report = mainViewModel.previewSession.value.lastRmbgInferenceReport,
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsImageTuning.kt 显式参数版本，调用点零改动。
    internal fun saveImageTuningSettings(): Unit =
        paramsSaveImageTuningSettings(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            getParams = { mainViewModel.params.value },
        )

    /** 汇总当前全部调参字段为不可变快照（预设保存、撤销、debug 往返共用）。 */
    // 重构期间保留：委托到 ui/pages/params/ParamsImageTuning.kt 显式参数版本，调用点零改动。
    internal fun currentTuningParams(): TuningParams =
        paramsCurrentTuningParams(getParams = { mainViewModel.params.value })

    // 重构期间保留：委托到 ui/pages/params/ParamsImageTuning.kt 显式参数版本，调用点零改动。
    internal fun currentLocalPipelineConfig(): LocalPipelineConfig =
        paramsCurrentLocalPipelineConfig(getParams = { mainViewModel.params.value })

    /**
     * 应用一份参数快照：写全部字段 + draft 文本，可选持久化并刷新预览。
     * 预设、debug HTTP、撤销/批量都走这里。
     */
    // 重构期间保留：委托到 ui/pages/params/ParamsHistory.kt 显式参数版本，调用点零改动。
    internal fun applyTuningParams(
        params: TuningParams,
        rebuildCandidates: Boolean = true,
        persist: Boolean = true,
        captureUndo: Boolean = true,
        refreshPreview: Boolean = true,
    ): Unit =
        paramsApplyTuningParams(
            params = params,
            rebuildCandidates = rebuildCandidates,
            persist = persist,
            captureUndo = captureUndo,
            refreshPreview = refreshPreview,
            getBefore = { currentTuningParams() },
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
            onSaveLocalSeparation = { saveLocalSeparationSettings() },
            onSaveImageTuning = { saveImageTuningSettings() },
            onSaveLiquidGlass = { saveLiquidGlassSettings() },
            onSaveGpt = { saveGptSettings() },
            onSaveUi = { saveUiState() },
            isBusy = { mainViewModel.shell.value.isBusy },
            getSession = { mainViewModel.previewSession.value.activeGenerationSession },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    /** 撤销上一次参数应用（预设/批量前自动捕获快照）。 */
    // 重构期间保留：委托到 ui/pages/params/ParamsHistory.kt 显式参数版本，调用点零改动。
    internal fun restoreLastParams(): Unit =
        paramsRestoreLastParams(
            getSnapshot = { mainViewModel.previewSession.value.lastParamsSnapshot },
            clearSnapshot = { mainViewModel.updatePreviewSession { it -> it.copy(lastParamsSnapshot = (null)) } },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onApply = { applyTuningParams(it, captureUndo = false) },
        )

    /**
     * 恢复默认配置（Issue #4）。
     * 仅重置全部调参到出厂默认值（TuningParams 默认构造），不清除已下载的 RMBG 模型与已生成的图标包。
     * 通过 TuningParams 默认值 + applyTuningParams 统一持久化，保证与各迁移逻辑一致。
     */
    // 重构期间保留：委托到 ui/pages/params/ParamsHistory.kt 显式参数版本，调用点零改动。
    internal fun resetToDefaults(confirmed: Boolean = false): Unit =
        paramsResetToDefaults(
            confirmed = confirmed,
            isBusy = { mainViewModel.shell.value.isBusy },
            isGeneratingGpt = { mainViewModel.previewSession.value.isGeneratingGptCandidate },
            isGeneratingRmbg = { mainViewModel.previewSession.value.isGeneratingRmbgCandidate },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onRequestConfirm = { title, message, confirmLabel, onConfirm ->
                requestServiceConfirm(title = title, message = message, confirmLabel = confirmLabel, onConfirm = onConfirm)
            },
            onApplyDefaults = { applyTuningParams(it, rebuildCandidates = true) },
            onClearPreset = {
                presetStore.activePresetId = null
                mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (null)) }
            },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsHistory.kt 显式参数版本，调用点零改动。
    internal fun initTuningHistory(): Unit =
        paramsInitTuningHistory(
            getParams = { mainViewModel.params.value },
            resetHistory = mainViewModel::resetHistory,
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsHistory.kt 显式参数版本，调用点零改动。
    internal fun undoTuning(): Unit =
        paramsUndoTuning(
            isBusy = { mainViewModel.shell.value.isBusy },
            isGeneratingGpt = { mainViewModel.previewSession.value.isGeneratingGptCandidate },
            isGeneratingRmbg = { mainViewModel.previewSession.value.isGeneratingRmbgCandidate },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onUndo = { mainViewModel.undo() },
            onApply = { applyTuningParams(it, captureUndo = false) },
            onClearPreset = {
                presetStore.activePresetId = null
                mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (null)) }
            },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsHistory.kt 显式参数版本，调用点零改动。
    internal fun redoTuning(): Unit =
        paramsRedoTuning(
            isBusy = { mainViewModel.shell.value.isBusy },
            isGeneratingGpt = { mainViewModel.previewSession.value.isGeneratingGptCandidate },
            isGeneratingRmbg = { mainViewModel.previewSession.value.isGeneratingRmbgCandidate },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onRedo = { mainViewModel.redo() },
            onApply = { applyTuningParams(it, captureUndo = false) },
            onClearPreset = {
                presetStore.activePresetId = null
                mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (null)) }
            },
        )

    /** 启动时统一加载调参相关设置（保留各迁移分支）。 */
    // 重构期间保留：委托到 ui/pages/params/ParamsHistory.kt 显式参数版本，调用点零改动。
    internal fun loadTuningParams(): Unit =
        paramsLoadTuningParams(
            onLoadLocal = { loadLocalSeparationSettings() },
            onLoadImage = { loadImageSettings() },
            onLoadLiquid = { loadLiquidGlassSettings() },
            getParams = { currentTuningParams() },
            setDraftJsonParamsText = { draftJsonParamsText = it },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsImageTuning.kt 显式参数版本，调用点零改动。
    internal fun updateNightSubjectLightBackgroundEnabled(enabled: Boolean): Unit =
        paramsUpdateNightSubjectLightBackgroundEnabled(
            enabled = enabled,
            getParams = { mainViewModel.params.value },
            updateLive = mainViewModel::updateLive,
            onSave = { saveImageTuningSettings() },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updatePreviewCornerRadiusDp(value: Int): Unit =
        paramsUpdatePreviewCornerRadiusDp(
            value = value,
            setValue = { mainViewModel.updatePreviewSession { v -> v.copy(previewCornerRadiusDp = (it)) } },
            setDraftText = { draftPreviewCornerRadiusDpText = it },
            onSave = { saveUiState() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updatePreviewIconSizeDp(value: Int): Unit =
        paramsUpdatePreviewIconSizeDp(
            value = value,
            setValue = { mainViewModel.updatePreviewSession { v -> v.copy(previewIconSizeDp = (it)) } },
            setDraftText = { draftPreviewIconSizeDpText = it },
            onSave = { saveUiState() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updateBatchPreviewCount(value: Int): Unit =
        paramsUpdateBatchPreviewCount(
            value = value,
            setValue = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(batchPreviewCount = (it)) } },
            setDraftText = { draftBatchPreviewCountText = it },
            onSave = { saveUiState() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updateBatchPreviewColumns(value: Int): Unit =
        paramsUpdateBatchPreviewColumns(
            value = value,
            setColumns = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(batchPreviewColumns = (it)) } },
            setDraftColumnsText = { draftBatchPreviewColumnsText = it },
            setIconSize = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(batchPreviewIconSizeDp = (it)) } },
            setDraftIconSizeText = { draftBatchPreviewIconSizeDpText = it },
            onSave = { saveUiState() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updateBatchPreviewIconSizeDp(value: Int): Unit =
        paramsUpdateBatchPreviewIconSizeDp(
            value = value,
            setValue = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(batchPreviewIconSizeDp = (it)) } },
            setDraftText = { draftBatchPreviewIconSizeDpText = it },
            onSave = { saveUiState() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updateBatchPreviewCornerRadiusDp(value: Int): Unit =
        paramsUpdateBatchPreviewCornerRadiusDp(
            value = value,
            setValue = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(batchPreviewCornerRadiusDp = (it)) } },
            setDraftText = { draftBatchPreviewCornerRadiusDpText = it },
            onSave = { saveUiState() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updateBatchPreviewDesktopBackground(option: PreviewDesktopBackground): Unit =
        paramsUpdateBatchPreviewDesktopBackground(
            option = option,
            getValue = { mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground },
            setValue = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(batchPreviewDesktopBackground = (it)) } },
            onSave = { saveUiState() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updatePreviewDesktopBackground(option: PreviewDesktopBackground): Unit =
        paramsUpdatePreviewDesktopBackground(
            option = option,
            getValue = { mainViewModel.previewSession.value.previewDesktopBackground },
            setValue = { mainViewModel.updatePreviewSession { v -> v.copy(previewDesktopBackground = (it)) } },
            onSave = { saveUiState() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updatePreviewStripEnabled(enabled: Boolean): Unit =
        paramsUpdatePreviewStripEnabled(
            enabled = enabled,
            getValue = { mainViewModel.previewSession.value.previewStripEnabled },
            setValue = { mainViewModel.updatePreviewSession { v -> v.copy(previewStripEnabled = (it)) } },
            onSave = { saveUiState() },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun loadLiquidGlassSettings(): Unit =
        paramsLoadLiquidGlassSettings(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            updateLive = mainViewModel::updateLive,
            getParams = { mainViewModel.params.value },
            setDraftRadiusText = { draftLiquidGlassRadiusText = it },
            setDraftOuterWidthText = { draftLiquidGlassOuterWidthText = it },
            setDraftTopAlphaText = { draftLiquidGlassTopAlphaText = it },
            setDraftBottomAlphaText = { draftLiquidGlassBottomAlphaText = it },
            setDraftBackgroundMistAlphaText = { draftLiquidGlassBackgroundMistAlphaText = it },
            setDraftBottomDarkAlphaText = { draftLiquidGlassBottomDarkAlphaText = it },
            setDraftSubjectScaleText = { draftLiquidGlassSubjectScaleText = it },
            setDraftSubjectOutlineWidthText = { draftLiquidGlassSubjectOutlineWidthText = it },
            setDraftSubjectInnerOutlineWidthText = { draftLiquidGlassSubjectInnerOutlineWidthText = it },
            setDraftSubjectShadowAlphaText = { draftLiquidGlassSubjectShadowAlphaText = it },
            setDraftSubjectOpacityText = { draftLiquidGlassSubjectOpacityText = it },
            setBottomBarEnabled = { mainViewModel.updateGlassBar { v -> v.copy(liquidGlassBottomBarEnabled = (it)) } },
            setBottomBarBlurEnabled = { mainViewModel.updateGlassBar { v -> v.copy(liquidGlassBottomBarBlurEnabled = (it)) } },
            getBottomBarEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarEnabled },
            getBottomBarBlurEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarBlurEnabled },
            onSave = { saveLiquidGlassSettings() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun saveLiquidGlassSettings(): Unit =
        paramsSaveLiquidGlassSettings(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            getParams = { mainViewModel.params.value },
            getBottomBarEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarEnabled },
            getBottomBarBlurEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarBlurEnabled },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun SharedPreferences.Editor.putLiquidGlassSettings(): SharedPreferences.Editor =
        paramsPutLiquidGlassSettings(
            params = mainViewModel.params.value,
            bottomBarEnabled = mainViewModel.glassBar.value.liquidGlassBottomBarEnabled,
            bottomBarBlurEnabled = mainViewModel.glassBar.value.liquidGlassBottomBarBlurEnabled,
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassEnabled(enabled: Boolean): Unit =
        paramsUpdateLiquidGlassEnabled(
            enabled = enabled,
            getParams = { mainViewModel.params.value },
            updateLive = mainViewModel::updateLive,
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassRadius(value: Int): Unit =
        paramsUpdateLiquidGlassRadius(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassRadiusText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassOuterWidth(value: Int): Unit =
        paramsUpdateLiquidGlassOuterWidth(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassOuterWidthText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassTopAlpha(value: Int): Unit =
        paramsUpdateLiquidGlassTopAlpha(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassTopAlphaText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassBottomAlpha(value: Int): Unit =
        paramsUpdateLiquidGlassBottomAlpha(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassBottomAlphaText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassBackgroundMistAlpha(value: Int): Unit =
        paramsUpdateLiquidGlassBackgroundMistAlpha(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassBackgroundMistAlphaText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassBottomDarkAlpha(value: Int): Unit =
        paramsUpdateLiquidGlassBottomDarkAlpha(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassBottomDarkAlphaText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassSubjectScalePercent(value: Int): Unit =
        paramsUpdateLiquidGlassSubjectScalePercent(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassSubjectScaleText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassSubjectOutlineWidth(value: Int): Unit =
        paramsUpdateLiquidGlassSubjectOutlineWidth(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassSubjectOutlineWidthText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassSubjectInnerOutlineWidth(value: Int): Unit =
        paramsUpdateLiquidGlassSubjectInnerOutlineWidth(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassSubjectInnerOutlineWidthText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassSubjectShadowAlpha(value: Int): Unit =
        paramsUpdateLiquidGlassSubjectShadowAlpha(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassSubjectShadowAlphaText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassSubjectOpacityPercent(value: Int): Unit =
        paramsUpdateLiquidGlassSubjectOpacityPercent(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassSubjectOpacityText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/home/HomeGenerationOps.kt 显式参数版本，调用点零改动。
    internal fun generateSelected(
        installWithRoot: Boolean,
        useGpt: Boolean,
        rootWriteMode: RootWriteMode = RootWriteMode.All,
        confirmed: Boolean = false,
    ): Unit =
        homeGenerateSelected(
            entry = apps.firstOrNull { it.packageName == mainViewModel.picker.value.selectedPackageName },
            installWithRoot = installWithRoot,
            useGpt = useGpt,
            rootWriteMode = rootWriteMode,
            confirmed = confirmed,
            gptApiKey = mainViewModel.gptRmbgSettings.value.gptApiKey,
            gptBaseUrl = mainViewModel.gptRmbgSettings.value.gptBaseUrl,
            isBusy = mainViewModel.shell.value.isBusy,
            gptRunCount = mainViewModel.presetUi.value.gptRunCount,
            onStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onRequestConfirm = { title, message, confirmLabel, onConfirm ->
                requestServiceConfirm(title = title, message = message, confirmLabel = confirmLabel, onConfirm = onConfirm)
            },
            onBeginBusy = { gpt ->
                mainViewModel.updateShell { it -> it.copy(isBusy = (true)) }
                if (gpt) {
                    mainViewModel.updatePreviewSession { it -> it.copy(isGptPreviewLoading = (true)) }
                    incrementGptRunCount()
                }
            },
            onLaunch = { name, block -> startUiFriendlyThread(name, block) },
            onGenerate = { e, g -> generateArtPlusPackage(e, g) },
            onPostGenerate = { result, e ->
                runOnUiThread {
                    mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (result.session)) }
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (result.selections).normalLight.name, previewNormalDark = (result.selections).normalDark.name, previewMonochromeLight = (result.selections).monochromeLight.name, previewMonochromeDark = (result.selections).monochromeDark.name) }
                    mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(previewPackageName = (e.packageName)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(previewDirPath = (result.outDir.absolutePath)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                    saveUiState()
                }
            },
            onInstall = { outDir, pkg, mode -> installWithRoot(outDir, pkg, mode) },
            onMarkGenerated = { pkg -> mainViewModel.updatePicker { it -> it.copy(generatedPackageNames = (markPackageGenerated(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), mainViewModel.picker.value.generatedPackageNames, pkg))) } },
            onToast = { toastStatus(it) },
            onStatus = { status(it) },
            onFinish = { gpt ->
                runOnUiThread {
                    mainViewModel.updateShell { it -> it.copy(isBusy = (false)) }
                    if (gpt) {
                        mainViewModel.updatePreviewSession { it -> it.copy(isGptPreviewLoading = (false)) }
                    }
                }
            },
            onConfirmedRetry = { root: Boolean, gpt: Boolean, mode: RootWriteMode -> generateSelected(root, gpt, mode, confirmed = true) },
        )

    // 重构期间保留：委托到 ui/pages/home/HomeGenerationOps.kt 显式参数版本（含内嵌 executeWrite），调用点零改动。
    internal fun writeSelectedWithRoot(rootWriteMode: RootWriteMode): Unit =
        homeWriteSelectedWithRoot(
            entry = apps.firstOrNull { it.packageName == mainViewModel.picker.value.selectedPackageName },
            rootWriteMode = rootWriteMode,
            isBusy = mainViewModel.shell.value.isBusy,
            activeSession = mainViewModel.previewSession.value.activeGenerationSession,
            selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
            autoConfirmRootWrite = mainViewModel.confirm.value.autoConfirmRootWrite,
            targetPath = apps.firstOrNull { it.packageName == mainViewModel.picker.value.selectedPackageName }?.let { "$ROOT_UXICONS_DIR/${it.packageName}" },
            onStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onGenerateFallback = { generateSelected(installWithRoot = true, useGpt = false, rootWriteMode = rootWriteMode) },
            onBeginBusy = { msg ->
                mainViewModel.updateShell { it -> it.copy(isBusy = (true)) }
                mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
            },
            onLaunch = { name, block -> startUiFriendlyThread(name, block) },
            onWrite = { session, selections -> writePackageOutputs(session, selections) },
            onInstall = { outDir, pkg, mode -> installWithRoot(outDir, pkg, mode) },
            onPostWrite = { session, selections, e ->
                runOnUiThread {
                    mainViewModel.updatePicker { it -> it.copy(generatedPackageNames = (markPackageGenerated(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), mainViewModel.picker.value.generatedPackageNames, e.packageName))) }
                    mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                    mainViewModel.updatePreviewSession { it -> it.copy(previewPackageName = (e.packageName)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(previewDirPath = (session.outDir.absolutePath)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                    saveUiState()
                }
            },
            onToast = { toastStatus(it) },
            onFinish = { runOnUiThread { mainViewModel.updateShell { it -> it.copy(isBusy = (false)) } } },
            onRequestConfirm = { pkg, targetPath, mode, onConfirm ->
                mainViewModel.updateConfirm { it -> it.copy(rootWriteConfirmRememberSkip = (false)) }
                mainViewModel.updateConfirm { it -> it.copy(pendingRootWriteConfirm = (RootWriteConfirmRequest(
                    packageName = pkg,
                    targetPath = targetPath,
                    rootWriteMode = mode,
                    onConfirm = { onConfirm() },
                ))) }
            },
        )

    // 重构期间保留：委托到 ui/pages/home/HomeGenerationOps.kt 显式参数版本，调用点零改动。
    internal fun selectAppAndRestoreGeneratedPreview(entry: AppEntry) {
        val revision = ++generatedPreviewRestoreRevision
        val localDir = artPlusPackageDir(entry.packageName)
        val known = entry.packageName in mainViewModel.picker.value.generatedPackageNames || hasGeneratedPackageBaseAssets(localDir)
        homeSelectAppAndRestore(
            entry = entry,
            revision = revision,
            isBusy = mainViewModel.shell.value.isBusy,
            knownGenerated = known,
            getSelected = { mainViewModel.picker.value.selectedPackageName },
            getRevision = { generatedPreviewRestoreRevision },
            onResetSelection = { pkg ->
                mainViewModel.updatePicker { it -> it.copy(selectedPackageName = (pkg)) }
                mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (null)) }
                mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) }
                mainViewModel.updatePreviewSession { it -> it.copy(previewPackageName = (null)) }
                mainViewModel.updatePreviewSession { it -> it.copy(previewDirPath = (null)) }
                mainViewModel.updateLive { p -> p.copy(previewNormalLight = (PreviewSelections.default(PreviewChoice.Original)).normalLight.name, previewNormalDark = (PreviewSelections.default(PreviewChoice.Original)).normalDark.name, previewMonochromeLight = (PreviewSelections.default(PreviewChoice.Original)).monochromeLight.name, previewMonochromeDark = (PreviewSelections.default(PreviewChoice.Original)).monochromeDark.name) }
                mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
            },
            onStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onSaveUi = { saveUiState() },
            onClearRmbg = { clearRmbgCandidateUiState() },
            onLaunch = { name, block -> startUiFriendlyThread(name, block) },
            onLoadDir = { existingGeneratedPackageDir(entry.packageName) },
            onUi = { block -> runOnUiThread(block) },
            onMarkGenerated = { pkg -> mainViewModel.updatePicker { it -> it.copy(generatedPackageNames = (markPackageGenerated(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), mainViewModel.picker.value.generatedPackageNames, pkg))) } },
            onBuildSession = { pkg, dir -> buildGeneratedPackageSession(pkg, dir) },
            onCommitSession = { session, dir, e ->
                mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                mainViewModel.updateLive { p -> p.copy(previewNormalLight = (PreviewSelections.default(PreviewChoice.Original)).normalLight.name, previewNormalDark = (PreviewSelections.default(PreviewChoice.Original)).normalDark.name, previewMonochromeLight = (PreviewSelections.default(PreviewChoice.Original)).monochromeLight.name, previewMonochromeDark = (PreviewSelections.default(PreviewChoice.Original)).monochromeDark.name) }
                mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) }
                mainViewModel.updatePreviewSession { it -> it.copy(previewPackageName = (e.packageName)) }
                mainViewModel.updatePreviewSession { it -> it.copy(previewDirPath = (dir.absolutePath)) }
                mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
            },
        )
    }

    // 重构期间保留：委托到 state/PresetBatchOps.kt MainViewModel 显式参数版本，调用点零改动。
    internal fun addLiquidGlassToSelectedGenerated() =
        mainViewModel.addLiquidGlassToSelectedGenerated(
            apps = apps.toList(),
            resolvePackageDir = ::existingGeneratedPackageDir,
            applyGlass = ::applyLiquidGlassToGeneratedPackage,
            installGlass = ::installLiquidGlassFilesWithRoot,
            buildSession = ::buildGeneratedPackageSession,
            persistOne = { current, pkg ->
                markPackageGenerated(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), current, pkg)
            },
            onSaveUiState = ::saveUiState,
        )

    // 重构期间保留：委托到 state/PresetBatchOps.kt MainViewModel 显式参数版本，调用点零改动。
    internal fun toggleMultiSelectedPackage(packageName: String) =
        mainViewModel.toggleMultiSelectedPackage(packageName = packageName, current = mainViewModel.picker.value.multiSelectedPackageNames)

    // 重构期间保留：委托到 state/PresetBatchOps.kt MainViewModel 显式参数版本，调用点零改动。
    internal fun addLiquidGlassToMultiSelectedGenerated() =
        mainViewModel.addLiquidGlassToMultiSelectedGenerated(
            onRequestConfirm = { title, message, confirmLabel, onConfirm ->
                requestServiceConfirm(title, message, confirmLabel, onConfirm)
            },
            onExecute = { pkgs -> executeAddLiquidGlassToMultiSelectedGenerated(pkgs) },
        )

    // 重构期间保留：委托到 state/PresetBatchOps.kt MainViewModel 显式参数版本，调用点零改动。
    internal fun executeAddLiquidGlassToMultiSelectedGenerated(packageNames: List<String>) =
        mainViewModel.executeAddLiquidGlassToMultiSelectedGenerated(
            packageNames = packageNames,
            selectedAtStart = mainViewModel.picker.value.selectedPackageName,
            resolvePackageDir = ::existingGeneratedPackageDir,
            applyGlass = ::applyLiquidGlassToGeneratedPackage,
            installGlass = ::installLiquidGlassFilesWithRoot,
            buildSession = ::buildGeneratedPackageSession,
            persistMany = { combined ->
                updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), combined)
            },
            onSaveUiState = ::saveUiState,
        )

    // 重构期间保留：委托到 system/RootInstaller.kt 显式参数版本，调用点零改动。
    internal fun existingGeneratedPackageDir(packageName: String): File =
        existingGeneratedPackageDir(
            packageName = packageName,
            previewDirPath = mainViewModel.previewSession.value.previewDirPath,
            previewPackageName = mainViewModel.previewSession.value.previewPackageName,
            externalArtPlusDir = getExternalFilesDir("ArtPlus"),
            filesDir = filesDir,
            appUid = applicationInfo.uid,
        )

    // Slice 1.4 已搬入 system/RootInstaller.kt：buildGeneratedPackageSession（纯函数，同包直接用）。

    // 重构期间保留：委托到 system/RootInstaller.kt 显式参数版本，调用点零改动。
    internal fun artPlusPackageDir(packageName: String): File =
        artPlusPackageDir(
            packageName = packageName,
            externalArtPlusDir = getExternalFilesDir("ArtPlus"),
            filesDir = filesDir,
        )

    // 重构期间保留：委托到 system/RootInstaller.kt 显式参数版本，调用点零改动。
    internal fun rootGeneratedPreviewDir(packageName: String): File =
        rootGeneratedPreviewDir(packageName = packageName, filesDir = filesDir)

    // Slice 1.4 已搬入 system/RootInstaller.kt：hasGeneratedPackageBaseAssets（纯函数，同包直接用）。

    // 重构期间保留：委托到 system/RootInstaller.kt 显式参数版本，调用点零改动。
    internal fun copyRootGeneratedPackageToLocal(packageName: String): File =
        copyRootGeneratedPackageToLocal(
            packageName = packageName,
            filesDir = filesDir,
            appUid = applicationInfo.uid,
        )

    // 重构期间保留：委托到 system/RootInstaller.kt 显式参数版本，调用点零改动。
    internal fun applyLiquidGlassToGeneratedPackage(dir: File) {
        val params = mainViewModel.params.value
        applyLiquidGlassToGeneratedPackage(
            dir = dir,
            nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
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
        )
    }

    // 重构期间保留：委托到 system/RootInstaller.kt 显式参数版本，调用点零改动。
    internal fun writeDefaultSubjectMonochromeFiles(
        dir: File,
        baseRecfg: Bitmap,
        overwriteExisting: Boolean,
    ) {
        writeDefaultSubjectMonochromeFiles(
            dir = dir,
            baseRecfg = baseRecfg,
            overwriteExisting = overwriteExisting,
            monochromeThemeScale = mainViewModel.params.value.monochromeThemeScale,
        )
    }

    // 重构期间保留：委托到 system/RootInstaller.kt 显式参数版本，调用点零改动。
    internal fun glassBackgroundForGeneratedPackage(
        dir: File,
        name: String,
        fallback: Bitmap,
        width: Int,
        height: Int,
    ): Bitmap {
        val params = mainViewModel.params.value
        return glassBackgroundForGeneratedPackage(
            dir = dir,
            name = name,
            fallback = fallback,
            width = width,
            height = height,
            liquidGlassEnabled = params.liquidGlassEnabled,
            liquidGlassBackgroundMistAlpha = params.liquidGlassBackgroundMistAlpha,
            liquidGlassTopAlpha = params.liquidGlassTopAlpha,
            liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
            liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
            liquidGlassOuterWidth = params.liquidGlassOuterWidth,
            liquidGlassRadius = params.liquidGlassRadius,
        )
    }

    // Slice 1.4 已搬入 system/RootInstaller.kt：decodeGeneratedBitmap（纯函数，同包直接用）。

    // Slice 1.4 已搬入 system/RootInstaller.kt：installLiquidGlassFilesWithRoot（纯 Root IO，同包直接用）。

    // 重构期间保留：委托到 system/RootInstaller.kt 显式参数版本，调用点零改动。
    internal fun generateArtPlusPackage(
        app: AppEntry,
        useGpt: Boolean,
        localModeOverride: LocalSeparationMode? = null,
    ): GenerationResult {
        val icon = app.applicationInfo.loadIcon(packageManager)
        return generateArtPlusPackage(
            app = app,
            useGpt = useGpt,
            localModeOverride = localModeOverride,
            params = mainViewModel.params.value,
            externalArtPlusDir = getExternalFilesDir("ArtPlus"),
            filesDir = filesDir,
            icon = icon,
            gptModelId = mainViewModel.gptRmbgSettings.value.gptModelId,
            gptBaseUrl = mainViewModel.gptRmbgSettings.value.gptBaseUrl,
            gptApiKey = mainViewModel.gptRmbgSettings.value.gptApiKey,
            isDebug = isDebugBuild(),
            onStatus = ::status,
            defaultChoiceForMode = ::defaultPreviewChoiceForMode,
            rmbgTunedForeground = ::rmbgTunedForegroundRaw,
        )
    }


    // 重构期间保留：委托到 pipeline/RmbgManager.kt 显式参数版本，调用点零改动。
    internal fun buildRmbgCandidate(sourceIcon: Bitmap): CandidateBuildResult? {
        val params = mainViewModel.params.value
        return buildRmbgCandidate(
            sourceIcon = sourceIcon,
            filesDir = filesDir,
            rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
            rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
            rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
            rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
            lock = this,
            getRuntime = { rmbgRuntime },
            setRuntime = { rmbgRuntime = it },
        )
    }

    // Slice 1.3 已搬入 pipeline/RmbgManager.kt：rebuildRmbgBackground（纯函数，同包直接用）。
    // Slice 1.3 已搬入 pipeline/RmbgManager.kt：rmbgValidationWarning（纯函数，同包直接用）。

    // 重构期间保留：委托到 pipeline/RmbgManager.kt 显式参数版本，调用点零改动。
    internal fun buildRmbgDebugCandidate(sourceIcon: Bitmap): RmbgDebugCandidate {
        val params = mainViewModel.params.value
        return buildRmbgDebugCandidate(
            sourceIcon = sourceIcon,
            filesDir = filesDir,
            rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
            rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
            rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
            rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
            lock = this,
            getRuntime = { rmbgRuntime },
            setRuntime = { rmbgRuntime = it },
        )
    }

    // 重构期间保留：委托到 pipeline/RmbgManager.kt 显式参数版本，调用点零改动。
    internal fun rmbgComponentDir(): File = rmbgComponentDir(filesDir)

    // 重构期间保留：委托到 pipeline/RmbgManager.kt 显式参数版本，调用点零改动。
    internal fun findRmbgComponent(): RmbgComponent? = findRmbgComponent(filesDir)

    // 重构期间保留：委托到 pipeline/RmbgManager.kt 显式参数版本，调用点零改动。
    internal fun clearInstalledRmbgComponent() {
        clearInstalledRmbgComponent(
            filesDir = filesDir,
            isBusy = mainViewModel.shell.value.isBusy,
            isGeneratingRmbgCandidate = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
            isInstallingRmbgComponent = mainViewModel.previewSession.value.isInstallingRmbgComponent,
            closeRuntime = {
                runCatching { rmbgRuntime?.close() }
                rmbgRuntime = null
            },
            onClearUiState = { clearRmbgCandidateUiState() },
            onResult = { deleted ->
                mainViewModel.updatePreviewSession { it -> it.copy(lastRmbgInferenceReport = (null)) }
                mainViewModel.updateGptRmbgSettings { it -> it.copy(rmbgComponentStatus = ("${System.currentTimeMillis()}")) }
                mainViewModel.updatePreviewSession { it -> it.copy(rmbgInstallStage = ("")) }
                mainViewModel.updatePreviewSession { it -> it.copy(rmbgInstallProgress = (null)) }
                mainViewModel.updateGptRmbgSettings { it -> it.copy(rmbgComponentSaveStatus = ("")) }
                mainViewModel.updateShell { it -> it.copy(statusText = (if (deleted) "已清除 RMBG" else "没有已安装 RMBG")) }
            },
        )
    }

    // 重构期间保留：委托到 pipeline/RmbgManager.kt 显式参数版本，调用点零改动。
    internal fun installRmbgComponentFromUrl() {
        installRmbgComponentFromUrl(
            urlText = mainViewModel.gptRmbgSettings.value.rmbgComponentUrl,
            filesDir = filesDir,
            cacheDir = cacheDir,
            isBusy = mainViewModel.shell.value.isBusy,
            isGeneratingRmbgCandidate = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
            isInstallingRmbgComponent = mainViewModel.previewSession.value.isInstallingRmbgComponent,
            isDebugBuild = isDebugBuild(),
            onSaveSettings = { saveRmbgSettings() },
            getRuntime = { rmbgRuntime },
            setRuntime = { rmbgRuntime = it },
            setInstalling = { mainViewModel.updatePreviewSession { v -> v.copy(isInstallingRmbgComponent = (it)) } },
            setStage = { mainViewModel.updatePreviewSession { v -> v.copy(rmbgInstallStage = (it)) } },
            setProgress = { mainViewModel.updatePreviewSession { v -> v.copy(rmbgInstallProgress = (it)) } },
            setStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            setComponentStatus = { mainViewModel.updateGptRmbgSettings { v -> v.copy(rmbgComponentStatus = (it)) } },
            setComponentSaveStatus = { mainViewModel.updateGptRmbgSettings { v -> v.copy(rmbgComponentSaveStatus = (it)) } },
            setLastError = { mainViewModel.updatePreviewSession { v -> v.copy(lastRmbgCandidateError = (it)) } },
            runOnUi = { runOnUiThread(it) },
        )
    }

    // 重构期间保留：委托到 pipeline/RmbgManager.kt 显式参数版本，调用点零改动。
    internal fun installRmbgComponentFromModelUrl(modelUrl: String, modelFile: File): RmbgComponent =
        installRmbgComponentFromModelUrl(
            modelUrl = modelUrl,
            modelFile = modelFile,
            filesDir = filesDir,
            isDebugBuild = isDebugBuild(),
            getRuntime = { rmbgRuntime },
            setRuntime = { rmbgRuntime = it },
            onDownloadProgress = { stage, progress, status ->
                runOnUiThread {
                    mainViewModel.updateShell { it -> it.copy(statusText = (status)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgInstallStage = (stage)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgInstallProgress = (progress)) }
                }
            },
            onInstallStage = {
                runOnUiThread {
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgInstallStage = ("安装模型")) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgInstallProgress = (null)) }
                }
            },
        )

    // 重构期间保留：委托到 pipeline/RmbgManager.kt 显式参数版本，调用点零改动。
    internal fun installRmbgComponentFromInput(input: InputStream): RmbgComponent =
        installRmbgComponentFromInput(
            input = input,
            filesDir = filesDir,
            getRuntime = { rmbgRuntime },
            setRuntime = { rmbgRuntime = it },
        )

    // 重构期间保留：委托到 pipeline/RmbgManager.kt 显式参数版本，调用点零改动。
    internal fun downloadRmbgFile(urlText: String, target: File, minBytes: Long, label: String) {
        downloadRmbgFile(
            urlText = urlText,
            target = target,
            minBytes = minBytes,
            label = label,
            isDebugBuild = isDebugBuild(),
        ) { stage, progress, status ->
            runOnUiThread {
                mainViewModel.updateShell { it -> it.copy(statusText = (status)) }
                mainViewModel.updatePreviewSession { it -> it.copy(rmbgInstallStage = (stage)) }
                mainViewModel.updatePreviewSession { it -> it.copy(rmbgInstallProgress = (progress)) }
            }
        }
    }

    // Slice 1.3 已搬入 pipeline/RmbgManager.kt：unzipRmbgComponent（纯函数，同包直接用）。

    // Slice 1.3 已搬入 pipeline/RmbgManager.kt：normalizeRmbgModelFile（纯函数，同包直接用）。

    // Slice 1.3 已搬入 pipeline/RmbgManager.kt：validateRmbgComponentDir（纯函数，同包直接用）。

    // Slice 1.3 已搬入 pipeline/RmbgManager.kt：copyDirectory（纯函数，同包直接用）。

    // Slice 1.3 已搬入 pipeline/RmbgManager.kt：DynamicRmbgRuntime
    //（任务名单称 OnnxSessionWrapper，实为当前本体；原 internal inner class，现顶层 internal class，
    // createSessionPair / configureBaseOptions / run / close 纯移动，初始化/关闭顺序与线程模型不变）。

    // 重构期间保留：委托到 pipeline/RmbgManager.kt 显式参数版本，调用点零改动。
    internal fun runRmbgModel(component: RmbgComponent, input: FloatBuffer, shape: LongArray): RmbgModelOutput =
        runRmbgModel(
            component = component,
            input = input,
            shape = shape,
            lock = this,
            getRuntime = { rmbgRuntime },
            setRuntime = { rmbgRuntime = it },
        )

    // 重构期间保留：委托到 pipeline/RmbgManager.kt 显式参数版本，调用点零改动。
    internal fun runRmbgAlphaMask(sourceIcon: Bitmap, component: RmbgComponent): RmbgMaskResult =
        runRmbgAlphaMask(
            sourceIcon = sourceIcon,
            component = component,
            lock = this,
            getRuntime = { rmbgRuntime },
            setRuntime = { rmbgRuntime = it },
        )

    // 重构期间保留：委托到 pipeline/RmbgManager.kt 显式参数版本，调用点零改动。
    internal fun tuneRmbgAlpha(alpha: IntArray, width: Int, height: Int): IntArray {
        val params = mainViewModel.params.value
        return tuneRmbgAlpha(
            alpha = alpha,
            width = width,
            height = height,
            rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
            rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
            rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
            rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
        )
    }

    // Slice 1.3 已搬入 pipeline/RmbgManager.kt：applyAlphaArrayToSource（纯函数，同包直接用）。

    // Slice 1.3 已搬入 pipeline/RmbgManager.kt：applyMaskToSource（纯函数，同包直接用）。


    internal fun defaultLocalPreviewChoice(autoChoice: PreviewChoice): PreviewChoice =
        defaultPreviewChoiceForMode(LocalSeparationMode.Auto, autoChoice)

    internal fun defaultPreviewChoiceForMode(mode: LocalSeparationMode, autoChoice: PreviewChoice): PreviewChoice =
        when (mode) {
            LocalSeparationMode.Original -> PreviewChoice.Original
            LocalSeparationMode.Plate -> PreviewChoice.Full
            LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
            LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
            LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
            LocalSeparationMode.Auto -> autoChoice
            LocalSeparationMode.Full -> PreviewChoice.Full
        }

    // 重构期间保留：委托到 system/RootInstaller.kt 显式参数版本，调用点零改动。
    internal fun writePackageOutputs(session: GenerationSession, selections: PreviewSelections) {
        val params = mainViewModel.params.value
        writePackageOutputs(
            session = session,
            selections = selections,
            edgePolishPercent = params.edgePolishPercent,
            foregroundSubjectPercent = params.foregroundSubjectPercent,
            rmbgTunedForeground = ::rmbgTunedForegroundRaw,
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
    }

    // 重构期间保留：委托到 imaging/LiquidGlassPipeline.kt 显式参数版本，调用点零改动。
    internal fun liquidGlassBackgroundForSize(
        source: Bitmap,
        width: Int,
        height: Int,
        forceLiquidGlass: Boolean = false,
    ): Bitmap {
        val params = mainViewModel.params.value
        return liquidGlassBackgroundForSize(
            source = source,
            width = width,
            height = height,
            forceLiquidGlass = forceLiquidGlass,
            liquidGlassEnabled = params.liquidGlassEnabled,
            liquidGlassBackgroundMistAlpha = params.liquidGlassBackgroundMistAlpha,
            liquidGlassTopAlpha = params.liquidGlassTopAlpha,
            liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
            liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
            liquidGlassOuterWidth = params.liquidGlassOuterWidth,
            liquidGlassRadius = params.liquidGlassRadius,
        )
    }

    // 重构期间保留：委托到 imaging/LiquidGlassPipeline.kt 显式参数版本，调用点零改动。
    internal fun foregroundForSize(
        source: Bitmap,
        width: Int,
        height: Int,
        forceLiquidGlass: Boolean = false,
    ): Bitmap {
        val params = mainViewModel.params.value
        return foregroundForSize(
            source = source,
            width = width,
            height = height,
            forceLiquidGlass = forceLiquidGlass,
            liquidGlassEnabled = params.liquidGlassEnabled,
            liquidGlassSubjectScalePercent = params.liquidGlassSubjectScalePercent,
            liquidGlassSubjectShadowAlpha = params.liquidGlassSubjectShadowAlpha,
            liquidGlassSubjectOutlineWidth = params.liquidGlassSubjectOutlineWidth,
            liquidGlassSubjectInnerOutlineWidth = params.liquidGlassSubjectInnerOutlineWidth,
            liquidGlassSubjectOpacityPercent = params.liquidGlassSubjectOpacityPercent,
            liquidGlassTopAlpha = params.liquidGlassTopAlpha,
            liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
            liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
            liquidGlassOuterWidth = params.liquidGlassOuterWidth,
            liquidGlassRadius = params.liquidGlassRadius,
            applyShadow = ::applyForegroundShadow,
            renderSubjectShadow = ::subjectShadowBitmap,
        )
    }

    // 重构期间保留：委托到 imaging/LiquidGlassPipeline.kt 显式参数版本，调用点零改动。
    internal fun renderLayeredLiquidGlassBackground(source: Bitmap): Bitmap {
        val params = mainViewModel.params.value
        return renderLayeredLiquidGlassBackground(
            source = source,
            liquidGlassBackgroundMistAlpha = params.liquidGlassBackgroundMistAlpha,
            liquidGlassTopAlpha = params.liquidGlassTopAlpha,
            liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
            liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
            liquidGlassOuterWidth = params.liquidGlassOuterWidth,
            liquidGlassRadius = params.liquidGlassRadius,
        )
    }

    // 重构期间保留：委托到 imaging/LiquidGlassPipeline.kt 显式参数版本，调用点零改动。
    internal fun renderLayeredLiquidGlassForeground(source: Bitmap): Bitmap {
        val params = mainViewModel.params.value
        return renderLayeredLiquidGlassForeground(
            source = source,
            liquidGlassSubjectScalePercent = params.liquidGlassSubjectScalePercent,
            liquidGlassSubjectShadowAlpha = params.liquidGlassSubjectShadowAlpha,
            liquidGlassSubjectOutlineWidth = params.liquidGlassSubjectOutlineWidth,
            liquidGlassSubjectInnerOutlineWidth = params.liquidGlassSubjectInnerOutlineWidth,
            liquidGlassSubjectOpacityPercent = params.liquidGlassSubjectOpacityPercent,
            liquidGlassTopAlpha = params.liquidGlassTopAlpha,
            liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
            liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
            liquidGlassOuterWidth = params.liquidGlassOuterWidth,
            liquidGlassRadius = params.liquidGlassRadius,
            renderSubjectShadow = ::subjectShadowBitmap,
        )
    }

    // 重构期间保留：委托到 imaging/LiquidGlassPipeline.kt 显式参数版本，调用点零改动。
    internal fun drawLayeredLiquidGlassLight(canvas: Canvas, width: Int, height: Int, radius: Float) {
        val params = mainViewModel.params.value
        return drawLayeredLiquidGlassLight(
            canvas = canvas,
            width = width,
            height = height,
            radius = radius,
            liquidGlassTopAlpha = params.liquidGlassTopAlpha,
            liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
            liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
            liquidGlassOuterWidth = params.liquidGlassOuterWidth,
        )
    }

    // 重构期间保留：委托到 imaging/LiquidGlassPipeline.kt 显式参数版本，调用点零改动。
    internal fun liquidGlassRadiusForSize(width: Int, height: Int): Float =
        liquidGlassRadiusForSize(
            width = width,
            height = height,
            liquidGlassRadius = mainViewModel.params.value.liquidGlassRadius,
        )

    // 重构期间保留：委托到 imaging/PreviewComposer.kt 显式参数版本，调用点零改动。
    internal fun effectiveChoiceForPreviewRow(
        mode: PreviewMode,
        rowChoice: PreviewChoice,
        session: GenerationSession,
    ): PreviewChoice {
        val params = mainViewModel.params.value
        return effectiveChoiceForPreviewRow(
            mode = mode,
            rowChoice = rowChoice,
            session = session,
            previewNormalLight = params.previewNormalLight,
            previewNormalDark = params.previewNormalDark,
            previewMonochromeLight = params.previewMonochromeLight,
            previewMonochromeDark = params.previewMonochromeDark,
        )
    }

    // 重构期间保留：委托到 imaging/PreviewComposer.kt 显式参数版本，调用点零改动。
    internal fun monochromeForCandidate(candidate: IconCandidate, invertLuma: Boolean = false): Bitmap {
        val params = mainViewModel.params.value
        return monochromeForCandidate(
            candidate = candidate,
            invertLuma = invertLuma,
            edgePolishPercent = params.edgePolishPercent,
            foregroundSubjectPercent = params.foregroundSubjectPercent,
            rmbgTunedForeground = ::rmbgTunedForegroundRaw,
        )
    }

    // 重构期间保留：委托到 imaging/PreviewComposer.kt 显式参数版本，调用点零改动。
    internal fun scaleMonochromeForTheme(source: Bitmap): Bitmap =
        scaleMonochromeForTheme(
            source = source,
            monochromeThemeScale = mainViewModel.params.value.monochromeThemeScale,
        )



    // 重构期间保留：委托到 imaging/PreviewComposer.kt 显式参数版本，调用点零改动。
    internal fun previewAssetsForSelections(
        session: GenerationSession,
        selections: PreviewSelections,
    ): PreviewAssets {
        val params = mainViewModel.params.value
        return previewAssetsForSelections(
            session = session,
            selections = selections,
            edgePolishPercent = params.edgePolishPercent,
            foregroundSubjectPercent = params.foregroundSubjectPercent,
            rmbgTunedForeground = ::rmbgTunedForegroundRaw,
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
    }

    // 重构期间保留：委托到 imaging/PreviewComposer.kt 显式参数版本，调用点零改动。
    internal fun previewAssetsForCandidate(candidate: IconCandidate, mode: PreviewMode? = null): PreviewAssets {
        val params = mainViewModel.params.value
        return previewAssetsForCandidate(
            candidate = candidate,
            mode = mode,
            edgePolishPercent = params.edgePolishPercent,
            foregroundSubjectPercent = params.foregroundSubjectPercent,
            rmbgTunedForeground = ::rmbgTunedForegroundRaw,
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
    }

    // 重构期间保留：委托到 imaging/PreviewComposer.kt 显式参数版本，调用点零改动。
    internal fun renderCandidateForegroundBase(candidate: IconCandidate): Bitmap =
        renderCandidateForegroundBase(
            candidate = candidate,
            edgePolishPercent = mainViewModel.params.value.edgePolishPercent,
            foregroundSubjectPercent = mainViewModel.params.value.foregroundSubjectPercent,
            rmbgTunedForeground = ::rmbgTunedForegroundRaw,
        )

    // 重构期间保留：委托到 imaging/PreviewComposer.kt 显式参数版本，调用点零改动。
    internal fun renderCandidateForeground(candidate: IconCandidate): Bitmap {
        val params = mainViewModel.params.value
        return renderCandidateForeground(
            candidate = candidate,
            edgePolishPercent = params.edgePolishPercent,
            foregroundSubjectPercent = params.foregroundSubjectPercent,
            rmbgTunedForeground = ::rmbgTunedForegroundRaw,
            liquidGlassEnabled = params.liquidGlassEnabled,
            liquidGlassSubjectScalePercent = params.liquidGlassSubjectScalePercent,
            liquidGlassSubjectShadowAlpha = params.liquidGlassSubjectShadowAlpha,
            liquidGlassSubjectOutlineWidth = params.liquidGlassSubjectOutlineWidth,
            liquidGlassSubjectInnerOutlineWidth = params.liquidGlassSubjectInnerOutlineWidth,
            liquidGlassSubjectOpacityPercent = params.liquidGlassSubjectOpacityPercent,
            liquidGlassTopAlpha = params.liquidGlassTopAlpha,
            liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
            liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
            liquidGlassOuterWidth = params.liquidGlassOuterWidth,
            liquidGlassRadius = params.liquidGlassRadius,
            foregroundShadowLevel = params.foregroundShadowLevel,
        )
    }

    // 重构期间保留：委托到 imaging/PreviewComposer.kt 显式参数版本，调用点零改动。
    internal fun applyForegroundShadow(source: Bitmap): Bitmap =
        applyForegroundShadow(
            source = source,
            foregroundShadowLevel = mainViewModel.params.value.foregroundShadowLevel,
        )

    // 重构期间保留：委托到 pipeline/RmbgManager.kt 显式参数版本，调用点零改动。
    // imaging/PreviewComposer.kt 以 lambda 注入 ::rmbgTunedForegroundRaw，签名不变仍能解析。
    internal fun rmbgTunedForegroundRaw(candidate: IconCandidate): Bitmap? {
        val params = mainViewModel.params.value
        return rmbgTunedForegroundRaw(
            candidate = candidate,
            rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
            rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
            rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
            rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
        )
    }

    // 重构期间保留：委托到 imaging/PreviewComposer.kt 显式参数版本，调用点零改动。
    internal fun renderCandidateBitmap(bitmap: Bitmap): Bitmap =
        renderCandidateBitmap(
            bitmap = bitmap,
            foregroundSubjectPercent = mainViewModel.params.value.foregroundSubjectPercent,
        )

    // 重构期间保留：委托到 ui/pages/home/HomeGenerationOps.kt 显式参数版本，调用点零改动。
    internal fun applyPreviewChoice(mode: PreviewMode, choice: PreviewChoice): Unit =
        homeApplyPreviewChoice(
            mode = mode,
            choice = choice,
            session = mainViewModel.previewSession.value.activeGenerationSession,
            selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
            onChooseCustom = { chooseCustomImageForMode(mode, choice.customKind!!) },
            onGenerateGpt = { generateGptCandidateForMode(mode) },
            onStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onCommitSelections = { selections -> mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) } },
            onSaveUi = { saveUiState() },
            onWrite = { session, selections -> writeActivePreviewOutputs(session, selections, closeDialog = false) },
        )

    // 重构期间保留：委托到 ui/pages/home/HomeGenerationOps.kt 显式参数版本，调用点零改动。
    internal fun applyPreviewChoiceToAll(choice: PreviewChoice): Unit =
        homeApplyPreviewChoiceToAll(
            choice = choice,
            session = mainViewModel.previewSession.value.activeGenerationSession,
            batchPackageNames = mainViewModel.picker.value.multiSelectedPackageNames.toList().sorted(),
            onApplyToSelected = { c, pkgs -> applyPreviewChoiceToSelectedPackages(c, pkgs) },
            onGenerateGptAll = { generateGptCandidateForAll() },
            onGenerateRmbgAll = { generateRmbgCandidateForAll() },
            onStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            candidateAvailable = { s, c -> candidateForChoice(s, c) != null },
            onCommitDefault = { selections -> mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) } },
            onClearChoiceMode = { mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) } },
            onSaveUi = { saveUiState() },
            onWriteClose = { session, selections -> writeActivePreviewOutputs(session, selections, closeDialog = true) },
        )

    // 重构期间保留：委托到 ui/pages/home/HomeGenerationOps.kt 显式参数版本，调用点零改动。
    internal fun applyPreviewChoiceToSelectedPackages(choice: PreviewChoice, packageNames: List<String>): Unit =
        homeApplyPreviewChoiceToSelectedPackages(
            choice = choice,
            packageNames = packageNames,
            gptBaseUrl = mainViewModel.gptRmbgSettings.value.gptBaseUrl,
            gptApiKey = mainViewModel.gptRmbgSettings.value.gptApiKey,
            hasRmbgComponent = findRmbgComponent() != null,
            isBusy = mainViewModel.shell.value.isBusy,
            isGeneratingGpt = mainViewModel.previewSession.value.isGeneratingGptCandidate,
            isGeneratingRmbg = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
            tryAcquireRmbg = { rmbgGenerationGate.compareAndSet(false, true) },
            onStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onBegin = { total ->
                mainViewModel.updateShell { it -> it.copy(isBusy = (true)) }
                mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) }
                mainViewModel.updateTransfer { it -> it.copy(batchApplyProgress = (BatchApplyProgress(
                    title = "全部应用",
                    completed = 0,
                    total = total,
                    currentLabel = "准备处理 $total 个 APK",
                    failures = 0,
                ))) }
            },
            selectedAtStart = mainViewModel.picker.value.selectedPackageName,
            apps = apps,
            onProgress = { completed, total, label, failures -> updateBatchApplyProgress(completed, total, label, failures) },
            onGeneratePackage = { app, c -> generatePackageForPreviewChoice(app, c) },
            onInstall = { outDir, pkg -> installWithRoot(outDir, pkg, RootWriteMode.All) },
            onFinishBatch = { successes, failures, selectedResult, atStart ->
                runOnUiThread {
                    if (successes.isNotEmpty()) {
                        mainViewModel.updatePicker { it -> it.copy(generatedPackageNames = (updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), mainViewModel.picker.value.generatedPackageNames + successes))) }
                        mainViewModel.updatePicker { it -> it.copy(multiSelectedPackageNames = (mainViewModel.picker.value.multiSelectedPackageNames - successes.toSet())) }
                    }
                    if (selectedResult != null && mainViewModel.picker.value.selectedPackageName == atStart) {
                        mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (selectedResult.session)) }
                        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selectedResult.selections).normalLight.name, previewNormalDark = (selectedResult.selections).normalDark.name, previewMonochromeLight = (selectedResult.selections).monochromeLight.name, previewMonochromeDark = (selectedResult.selections).monochromeDark.name) }
                        mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) }
                        mainViewModel.updatePreviewSession { it -> it.copy(previewPackageName = (selectedResult.session.packageName)) }
                        mainViewModel.updatePreviewSession { it -> it.copy(previewDirPath = (selectedResult.outDir.absolutePath)) }
                        mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                        saveUiState()
                    }
                    mainViewModel.updateShell { it -> it.copy(statusText = (when {
                        failures.isEmpty() -> "全部应用完成: ${successes.size}/${packageNames.size}"
                        successes.isEmpty() -> "全部应用失败: ${failures.firstOrNull().orEmpty()}"
                        else -> "全部应用完成 ${successes.size} 个，失败 ${failures.size} 个: ${failures.firstOrNull().orEmpty()}"
                    })) }
                }
            },
            onReleaseRmbg = { rmbgGenerationGate.set(false) },
            onResetBusy = {
                runOnUiThread {
                    mainViewModel.updateShell { it -> it.copy(isBusy = (false)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(isGptPreviewLoading = (false)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(isGeneratingGptCandidate = (false)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(isGeneratingRmbgCandidate = (false)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidatePackageName = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateMode = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateStatusText = ("")) }
                    mainViewModel.updateTransfer { it -> it.copy(batchApplyProgress = (null)) }
                }
            },
            onLaunch = { name, block -> startUiFriendlyThread(name, block) },
        )

    // 重构期间保留：委托到 state/PresetBatchOps.kt MainViewModel 显式参数版本，调用点零改动（标题 "全部应用" 内聚进 wrapper）。
    internal fun updateBatchApplyProgress(
        completed: Int,
        total: Int,
        currentLabel: String,
        failures: Int,
    ) = mainViewModel.updateBatchApplyProgress(
        completed = completed,
        total = total,
        currentLabel = currentLabel,
        failures = failures,
        title = "全部应用",
    )

    // 重构期间保留：委托到 ui/pages/home/HomeGenerationOps.kt 显式参数版本，调用点零改动。
    internal fun generatePackageForPreviewChoice(app: AppEntry, choice: PreviewChoice): GenerationResult =
        homeGeneratePackageForPreviewChoice(
            app = app,
            choice = choice,
            onGenerate = { a, g -> generateArtPlusPackage(a, g) },
            onBuildRmbg = { src -> (buildRmbgCandidate(src) ?: error("未安装 RMBG 组件 ZIP")).candidate ?: error("RMBG候选为空") },
            onResize = { src, w, h -> resizeBitmap(src, w, h) },
            onWrite = { session, selections -> writePackageOutputs(session, selections) },
            defaultLocal = { auto -> defaultLocalPreviewChoice(auto) },
            candidateAvailable = { s, c -> candidateForChoice(s, c) },
        )

    internal fun clearRmbgCandidateUiState() {
        if (mainViewModel.previewSession.value.isGeneratingRmbgCandidate) {
            return
        }
        mainViewModel.updatePreviewSession { it -> it.copy(lastRmbgCandidateError = (null)) }
        mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidatePackageName = (null)) }
        mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateMode = (null)) }
        mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateStatusText = ("")) }
        mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailurePackageName = (null)) }
        mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailureMode = (null)) }
    }

    internal fun chooseCustomImageForMode(mode: PreviewMode, kind: CustomImageKind) {
        if (mainViewModel.shell.value.isBusy || mainViewModel.previewSession.value.isGeneratingGptCandidate || mainViewModel.previewSession.value.isGeneratingRmbgCandidate) {
            return
        }
        mainViewModel.updatePreviewSession { it -> it.copy(pendingCustomImageMode = (mode)) }
        mainViewModel.updatePreviewSession { it -> it.copy(pendingCustomImageKind = (kind)) }
        chooseCustomImageLauncher.launch(
            arrayOf(
                "image/png",
                "image/svg+xml",
            ),
        )
    }

    internal fun importCustomPreviewImage(mode: PreviewMode, kind: CustomImageKind, uri: Uri) {
        val session = mainViewModel.previewSession.value.activeGenerationSession
        if (session == null) {
            mainViewModel.updateShell { it -> it.copy(statusText = ("先生成一次预览后再导入自定义图片")) }
            return
        }
        mainViewModel.updateShell { it -> it.copy(statusText = ("导入${kind.label}: ${mode.label}")) }
        startUiFriendlyThread("ArtPlusCustomImageImport") {
            try {
                val bitmap = loadCustomImageBitmap(contentResolver, uri)
                val updatedSession = session.copy(
                    customForegrounds = if (kind == CustomImageKind.Foreground) {
                        session.customForegrounds + (mode to bitmap)
                    } else {
                        session.customForegrounds
                    },
                    customBackgrounds = if (kind == CustomImageKind.Background) {
                        session.customBackgrounds + (mode to bitmap)
                    } else {
                        session.customBackgrounds
                    },
                )
                val selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark)
                writePackageOutputs(updatedSession, selections)
                if (false && mainViewModel.shell.value.outputTreeUri != null) {
                    exportToTree(contentResolver, mainViewModel.shell.value.outputTreeUri, updatedSession.outDir)
                }
                runOnUiThread {
                    mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (updatedSession)) }
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                    mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                    mainViewModel.updateShell { it -> it.copy(statusText = ("已导入${kind.label}: ${mode.label}")) }
                    saveUiState()
                }
            } catch (error: Exception) {
                status("${kind.label}导入失败: ${error.message ?: error.javaClass.simpleName}")
            }
        }
    }

    internal fun generateGptCandidateForMode(mode: PreviewMode, confirmed: Boolean = false) {
        val session = mainViewModel.previewSession.value.activeGenerationSession ?: return
        if (mainViewModel.gptRmbgSettings.value.gptBaseUrl.trim().isEmpty() || mainViewModel.gptRmbgSettings.value.gptApiKey.trim().isEmpty()) {
            mainViewModel.updateShell { it -> it.copy(statusText = ("请填写AI提供商信息")) }
            return
        }
        if (mainViewModel.previewSession.value.isGeneratingGptCandidate || mainViewModel.shell.value.isBusy) {
            return
        }
        if (!confirmed) {
            requestServiceConfirm(
                title = "使用 AI 生成",
                message = "将调用云端图像接口（已累计 ${mainViewModel.presetUi.value.gptRunCount} 次）。确认继续？",
                confirmLabel = "继续",
            ) {
                generateGptCandidateForMode(mode, confirmed = true)
            }
            return
        }
        mainViewModel.updatePreviewSession { it -> it.copy(isGeneratingGptCandidate = (true)) }
        mainViewModel.updatePreviewSession { it -> it.copy(isGptPreviewLoading = (true)) }
        incrementGptRunCount()
        mainViewModel.updateShell { it -> it.copy(statusText = ("AI候选生成中: ${session.packageName}")) }
        val selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark).withChoice(mode, PreviewChoice.Gpt)
        startUiFriendlyThread("ArtPlusGptCandidate") {
            try {
                // P4 交界：GPT 图层收敛进 pipeline/，显式传调参 + 凭证 + 状态回调。
                val gptLayers = generateGptLayers(session.sourceIcon, session.baseRecfg, session.baseRecbg, mainViewModel.params.value.gptCustomPrompt, GptPromptPreset.fromValue(mainViewModel.params.value.gptPromptPreset), mainViewModel.params.value.foregroundSubjectPercent, GptImageMode.fromValue(mainViewModel.params.value.gptImageMode), mainViewModel.gptRmbgSettings.value.gptModelId, mainViewModel.gptRmbgSettings.value.gptBaseUrl, mainViewModel.gptRmbgSettings.value.gptApiKey, isDebugBuild(), ::status)
                val updatedSession = session.copy(
                    candidates = session.candidates + (
                        PreviewChoice.Gpt to IconCandidate(
                            recfgRaw = gptLayers.recfg,
                            recbg = gptLayers.recbg,
                            monochromeRaw = null,
                            isLocal = false,
                        )
                        ),
                )
                writePackageOutputs(updatedSession, selections)
                if (false && mainViewModel.shell.value.outputTreeUri != null) {
                    exportToTree(contentResolver, mainViewModel.shell.value.outputTreeUri, updatedSession.outDir)
                }
                runOnUiThread {
                    mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (updatedSession)) }
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                    mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                    val msg = "AI候选已生成并应用到 ${mode.label}"
                    mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    saveUiState()
                }
            } catch (error: Exception) {
                toastStatus("AI候选失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                runOnUiThread {
                    mainViewModel.updatePreviewSession { it -> it.copy(isGeneratingGptCandidate = (false)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(isGptPreviewLoading = (false)) }
                }
            }
        }
    }

    internal fun generateGptCandidateForAll(confirmed: Boolean = false) {
        val session = mainViewModel.previewSession.value.activeGenerationSession ?: return
        if (mainViewModel.gptRmbgSettings.value.gptBaseUrl.trim().isEmpty() || mainViewModel.gptRmbgSettings.value.gptApiKey.trim().isEmpty()) {
            mainViewModel.updateShell { it -> it.copy(statusText = ("请填写AI提供商信息")) }
            return
        }
        if (mainViewModel.previewSession.value.isGeneratingGptCandidate || mainViewModel.shell.value.isBusy) {
            return
        }
        if (!confirmed) {
            requestServiceConfirm(
                title = "使用 AI 生成全部",
                message = "将调用云端图像接口（已累计 ${mainViewModel.presetUi.value.gptRunCount} 次）。确认继续？",
                confirmLabel = "继续",
            ) {
                generateGptCandidateForAll(confirmed = true)
            }
            return
        }
        mainViewModel.updatePreviewSession { it -> it.copy(isGeneratingGptCandidate = (true)) }
        mainViewModel.updatePreviewSession { it -> it.copy(isGptPreviewLoading = (true)) }
        incrementGptRunCount()
        mainViewModel.updateShell { it -> it.copy(statusText = ("AI候选生成中: ${session.packageName}")) }
        val selections = PreviewSelections.default(PreviewChoice.Gpt)
        startUiFriendlyThread("ArtPlusGptCandidateAll") {
            try {
                // P4 交界：GPT 图层收敛进 pipeline/，显式传调参 + 凭证 + 状态回调。
                val gptLayers = generateGptLayers(session.sourceIcon, session.baseRecfg, session.baseRecbg, mainViewModel.params.value.gptCustomPrompt, GptPromptPreset.fromValue(mainViewModel.params.value.gptPromptPreset), mainViewModel.params.value.foregroundSubjectPercent, GptImageMode.fromValue(mainViewModel.params.value.gptImageMode), mainViewModel.gptRmbgSettings.value.gptModelId, mainViewModel.gptRmbgSettings.value.gptBaseUrl, mainViewModel.gptRmbgSettings.value.gptApiKey, isDebugBuild(), ::status)
                val updatedSession = session.copy(
                    candidates = session.candidates + (
                        PreviewChoice.Gpt to IconCandidate(
                            recfgRaw = gptLayers.recfg,
                            recbg = gptLayers.recbg,
                            monochromeRaw = null,
                            isLocal = false,
                        )
                        ),
                )
                writePackageOutputs(updatedSession, selections)
                if (false && mainViewModel.shell.value.outputTreeUri != null) {
                    exportToTree(contentResolver, mainViewModel.shell.value.outputTreeUri, updatedSession.outDir)
                }
                runOnUiThread {
                    mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (updatedSession)) }
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                    mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                    val msg = "AI候选已生成并应用到全部"
                    mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    saveUiState()
                }
            } catch (error: Exception) {
                toastStatus("AI候选失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                runOnUiThread {
                    mainViewModel.updatePreviewSession { it -> it.copy(isGeneratingGptCandidate = (false)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(isGptPreviewLoading = (false)) }
                }
            }
        }
    }

    internal fun generateRmbgCandidateForMode(mode: PreviewMode, confirmed: Boolean = false) {
        val session = mainViewModel.previewSession.value.activeGenerationSession ?: return
        if (session.candidates[PreviewChoice.Rmbg] != null) {
            applyPreviewChoice(mode, PreviewChoice.Rmbg)
            mainViewModel.updateShell { it -> it.copy(statusText = ("已使用现有 RMBG 候选")) }
            return
        }
        if (findRmbgComponent() == null) {
            mainViewModel.updatePreviewSession { it -> it.copy(lastRmbgCandidateError = ("未安装 RMBG 组件 ZIP")) }
            mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailurePackageName = (session.packageName)) }
            mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailureMode = (mode)) }
            mainViewModel.updateShell { it -> it.copy(statusText = (mainViewModel.previewSession.value.lastRmbgCandidateError ?: "未安装 RMBG 组件")) }
            return
        }
        if (mainViewModel.previewSession.value.isGeneratingRmbgCandidate || mainViewModel.previewSession.value.isGeneratingGptCandidate || mainViewModel.shell.value.isBusy) {
            mainViewModel.updateShell { it -> it.copy(statusText = ("RMBG正在运行或主任务忙，请等待")) }
            return
        }
        if (!confirmed) {
            requestServiceConfirm(
                title = "使用 RMBG 抠图",
                message = "将运行本地 ONNX 模型抠图（已累计 ${mainViewModel.presetUi.value.rmbgRunCount} 次）。确认继续？",
                confirmLabel = "继续",
            ) {
                generateRmbgCandidateForMode(mode, confirmed = true)
            }
            return
        }
        if (!rmbgGenerationGate.compareAndSet(false, true)) {
            mainViewModel.updateShell { it -> it.copy(statusText = ("RMBG正在运行，请等待")) }
            return
        }
        mainViewModel.updatePreviewSession { it -> it.copy(isGeneratingRmbgCandidate = (true)) }
        incrementRmbgRunCount()
        mainViewModel.updatePreviewSession { it -> it.copy(lastRmbgCandidateError = (null)) }
        mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidatePackageName = (session.packageName)) }
        mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateMode = (mode)) }
        mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateStatusText = ("RMBG运行中(${RmbgInferenceBackend.Cpu.label})，请等待: ${mode.label}")) }
        mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailurePackageName = (null)) }
        mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailureMode = (null)) }
        mainViewModel.updateShell { it -> it.copy(statusText = ("RMBG候选生成中(${RmbgInferenceBackend.Cpu.label}): ${session.packageName}")) }
        val selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark).withChoice(mode, PreviewChoice.Rmbg)
        startUiFriendlyThread("ArtPlusRmbgCandidate") {
            try {
                val source = resizeBitmap(session.sourceIcon, SIZE_1X1, SIZE_1X1)
                val result = buildRmbgCandidate(source)
                    ?: error("未安装 RMBG 组件 ZIP")
                val candidate = result.candidate ?: error("RMBG候选为空")
                val inferenceReport = result.rmbgInference
                val updatedSession = session.copy(
                    candidates = session.candidates + (PreviewChoice.Rmbg to candidate),
                )
                writePackageOutputs(updatedSession, selections)
                if (false && mainViewModel.shell.value.outputTreeUri != null) {
                    exportToTree(contentResolver, mainViewModel.shell.value.outputTreeUri, updatedSession.outDir)
                }
                runOnUiThread {
                    mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (updatedSession)) }
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                    mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(lastRmbgCandidateError = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(lastRmbgInferenceReport = (inferenceReport)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailurePackageName = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailureMode = (null)) }
                    val msg = if (result.validationWarning != null) {
                        "${result.validationWarning}，已应用到 ${mode.label}: ${formatRmbgInferenceReport(inferenceReport)}"
                    } else {
                        "RMBG候选已生成并应用到 ${mode.label}: ${formatRmbgInferenceReport(inferenceReport)}"
                    }
                    mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    saveUiState()
                }
            } catch (error: Throwable) {
                val message = describeRmbgFailure(error)
                runOnUiThread {
                    mainViewModel.updatePreviewSession { it -> it.copy(lastRmbgCandidateError = (message)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailurePackageName = (session.packageName)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailureMode = (mode)) }
                    val msg = "RMBG候选失败(${RmbgInferenceBackend.Cpu.label}): $message"
                    mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } finally {
                rmbgGenerationGate.set(false)
                runOnUiThread {
                    mainViewModel.updatePreviewSession { it -> it.copy(isGeneratingRmbgCandidate = (false)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidatePackageName = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateMode = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateStatusText = ("")) }
                }
            }
        }
    }

    internal fun generateRmbgCandidateForAll(confirmed: Boolean = false) {
        val session = mainViewModel.previewSession.value.activeGenerationSession ?: return
        if (session.candidates[PreviewChoice.Rmbg] != null) {
            applyPreviewChoiceToAll(PreviewChoice.Rmbg)
            mainViewModel.updateShell { it -> it.copy(statusText = ("已使用现有 RMBG 候选")) }
            return
        }
        if (findRmbgComponent() == null) {
            mainViewModel.updatePreviewSession { it -> it.copy(lastRmbgCandidateError = ("未安装 RMBG 组件 ZIP")) }
            mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailurePackageName = (session.packageName)) }
            mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailureMode = (null)) }
            mainViewModel.updateShell { it -> it.copy(statusText = (mainViewModel.previewSession.value.lastRmbgCandidateError ?: "未安装 RMBG 组件")) }
            return
        }
        if (mainViewModel.previewSession.value.isGeneratingRmbgCandidate || mainViewModel.previewSession.value.isGeneratingGptCandidate || mainViewModel.shell.value.isBusy) {
            mainViewModel.updateShell { it -> it.copy(statusText = ("RMBG正在运行或主任务忙，请等待")) }
            return
        }
        if (!confirmed) {
            requestServiceConfirm(
                title = "使用 RMBG 抠图全部",
                message = "将运行本地 ONNX 模型抠图（已累计 ${mainViewModel.presetUi.value.rmbgRunCount} 次）。确认继续？",
                confirmLabel = "继续",
            ) {
                generateRmbgCandidateForAll(confirmed = true)
            }
            return
        }
        if (!rmbgGenerationGate.compareAndSet(false, true)) {
            mainViewModel.updateShell { it -> it.copy(statusText = ("RMBG正在运行，请等待")) }
            return
        }
        mainViewModel.updatePreviewSession { it -> it.copy(isGeneratingRmbgCandidate = (true)) }
        incrementRmbgRunCount()
        mainViewModel.updatePreviewSession { it -> it.copy(lastRmbgCandidateError = (null)) }
        mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidatePackageName = (session.packageName)) }
        mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateMode = (null)) }
        mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateStatusText = ("RMBG运行中(${RmbgInferenceBackend.Cpu.label})，请等待: 全部")) }
        mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailurePackageName = (null)) }
        mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailureMode = (null)) }
        mainViewModel.updateShell { it -> it.copy(statusText = ("RMBG候选生成中(${RmbgInferenceBackend.Cpu.label}): ${session.packageName}")) }
        val selections = PreviewSelections.default(PreviewChoice.Rmbg)
        startUiFriendlyThread("ArtPlusRmbgCandidateAll") {
            try {
                val source = resizeBitmap(session.sourceIcon, SIZE_1X1, SIZE_1X1)
                val result = buildRmbgCandidate(source)
                    ?: error("未安装 RMBG 组件 ZIP")
                val candidate = result.candidate ?: error("RMBG候选为空")
                val inferenceReport = result.rmbgInference
                val updatedSession = session.copy(
                    candidates = session.candidates + (PreviewChoice.Rmbg to candidate),
                )
                writePackageOutputs(updatedSession, selections)
                if (false && mainViewModel.shell.value.outputTreeUri != null) {
                    exportToTree(contentResolver, mainViewModel.shell.value.outputTreeUri, updatedSession.outDir)
                }
                runOnUiThread {
                    mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (updatedSession)) }
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                    mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(lastRmbgCandidateError = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(lastRmbgInferenceReport = (inferenceReport)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailurePackageName = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailureMode = (null)) }
                    val msg = if (result.validationWarning != null) {
                        "${result.validationWarning}，已应用到全部: ${formatRmbgInferenceReport(inferenceReport)}"
                    } else {
                        "RMBG候选已生成并应用到全部: ${formatRmbgInferenceReport(inferenceReport)}"
                    }
                    mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    saveUiState()
                }
            } catch (error: Throwable) {
                val message = describeRmbgFailure(error)
                runOnUiThread {
                    mainViewModel.updatePreviewSession { it -> it.copy(lastRmbgCandidateError = (message)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailurePackageName = (session.packageName)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailureMode = (null)) }
                    val msg = "RMBG候选失败(${RmbgInferenceBackend.Cpu.label}): $message"
                    mainViewModel.updateShell { it -> it.copy(statusText = (msg)) }
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } finally {
                rmbgGenerationGate.set(false)
                runOnUiThread {
                    mainViewModel.updatePreviewSession { it -> it.copy(isGeneratingRmbgCandidate = (false)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidatePackageName = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateMode = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateStatusText = ("")) }
                }
            }
        }
    }

    internal fun describeRmbgFailure(error: Throwable): String {
        val root = unwrapInvocationError(error)
        val raw = root.message ?: root.javaClass.simpleName
        val lower = raw.lowercase()
        return when {
            root is OutOfMemoryError ||
                "outofmemory" in lower ||
                "failed to allocate" in lower ||
                "memory" in lower -> {
                "内存不足或 ONNX 分配失败；已释放会话但 RMBG-2.0 峰值仍较高"
            }
            "未通过校验" in raw -> {
                raw
            }
            "reshape" in lower || "shape" in lower || "invalid dimensions" in lower -> {
                "模型输入尺寸不匹配；当前 RMBG-2.0 ONNX 组件需要 1024 推理分辨率"
            }
            else -> raw
        }
    }

    internal fun formatRmbgInferenceReport(report: RmbgInferenceReport?): String {
        if (report == null) {
            return RmbgInferenceBackend.Cpu.label
        }
        return buildString {
            append(report.actualBackend.label)
            append(" ")
            append(report.elapsedMs)
            append("ms")
        }
    }

    internal fun unwrapInvocationError(error: Throwable): Throwable {
        var current = error
        while (current is InvocationTargetException && current.targetException != null) {
            current = current.targetException
        }
        return current
    }

    // 重构期间保留：委托到 ui/pages/home/HomeGenerationOps.kt 显式参数版本，调用点零改动。
    internal fun refreshActivePreviewOutputs(
        rebuildLocalCandidates: Boolean,
        retargetFrom: PreviewChoice? = null,
    ): Unit =
        homeRefreshActivePreviewOutputs(
            currentSession = mainViewModel.previewSession.value.activeGenerationSession,
            rebuildLocalCandidates = rebuildLocalCandidates,
            retargetFrom = retargetFrom,
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
            tuning = currentTuningParams(),
            onRebuild = { session, app, tuning -> rebuildLocalSession(session, app, packageManager, tuning) },
            defaultLocal = { auto -> defaultLocalPreviewChoice(auto) },
            normalize = { session, selections -> normalizePreviewSelections(session, selections) },
            onWrite = { session, selections -> writePackageOutputs(session, selections) },
            onCommit = { session, selections ->
                mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                saveUiState()
            },
            onStatus = { status(it) },
        )

    // 重构期间保留：委托到 ui/pages/home/HomeGenerationOps.kt 显式参数版本，调用点零改动。
    internal fun writeActivePreviewOutputs(
        session: GenerationSession,
        selections: PreviewSelections,
        closeDialog: Boolean,
    ): Unit =
        homeWriteActivePreviewOutputs(
            session = session,
            selections = selections,
            closeDialog = closeDialog,
            scope = previewWorkerScope,
            getJob = { previewOutputJob },
            setJob = { previewOutputJob = it },
            incRevision = { ++previewOutputRevision },
            getRevision = { previewOutputRevision },
            setRefreshing = { mainViewModel.updatePreviewSession { v -> v.copy(isPreviewOutputRefreshing = (it)) } },
            outputDebounceMs = PREVIEW_OUTPUT_DEBOUNCE_MS,
            onWrite = { s, sel -> writePackageOutputs(s, sel) },
            onCommit = { s, sel, close ->
                mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (s)) }
                mainViewModel.updateLive { p -> p.copy(previewNormalLight = (sel).normalLight.name, previewNormalDark = (sel).normalDark.name, previewMonochromeLight = (sel).monochromeLight.name, previewMonochromeDark = (sel).monochromeDark.name) }
                mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                if (close) {
                    mainViewModel.updatePreviewSession { it -> it.copy(previewChoiceMode = (null)) }
                }
                saveUiState()
            },
            onStatus = { status(it) },
        )



















    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    internal fun showKeyboardFor(editText: EditText) =
        pickerShowKeyboardFor(editText)

    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    internal fun status(message: String) {
        pickerPostStatus(message) { runOnUiThread { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } } }
    }

    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    internal fun toastStatus(message: String) =
        pickerToastStatus(
            message = message,
            postOnUi = { text -> runOnUiThread { mainViewModel.updateShell { it -> it.copy(statusText = (text)) } } },
            showToast = { text ->
                runOnUiThread {
                    Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
                }
            },
        )

    // 重构期间保留：委托到 system/ExportManager.kt 显式参数版本，调用点零改动。
    internal fun cancelBackup() {
        val state = BackupCancelState(
            backupJob = backupJob,
            backupDotJob = backupDotJob,
            sheetVisible = mainViewModel.transfer.value.backupSheetVisible,
            inBackground = mainViewModel.transfer.value.backupInBackground,
            progress = mainViewModel.transfer.value.backupProgress,
            isBusy = mainViewModel.shell.value.isBusy,
        )
        cancelBackup(state, ::toastStatus)
        backupJob = state.backupJob
        backupDotJob = state.backupDotJob
        mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (state.sheetVisible)) }
        mainViewModel.updateTransfer { it -> it.copy(backupInBackground = (state.inBackground)) }
        mainViewModel.updateTransfer { it -> it.copy(backupProgress = (state.progress)) }
        mainViewModel.updateShell { it -> it.copy(isBusy = (state.isBusy)) }
    }

    // 重构期间保留：委托到 system/ExportManager.kt 显式参数版本，调用点零改动。
    internal fun cancelSingleExport() {
        val state = SingleExportCancelState(
            singleExportJob = singleExportJob,
            sheetVisible = mainViewModel.transfer.value.singleExportSheetVisible,
            progress = mainViewModel.transfer.value.exportProgress,
        )
        cancelSingleExport(state, ::toastStatus)
        singleExportJob = state.singleExportJob
        mainViewModel.updateTransfer { it -> it.copy(singleExportSheetVisible = (state.sheetVisible)) }
        mainViewModel.updateTransfer { it -> it.copy(exportProgress = (state.progress)) }
    }

    internal fun startBackupDotAnimation() {
        backupDotJob?.cancel()
        backupDotJob = mainScope.launch {
            while (isActive) {
                delay(500)
                mainViewModel.updateTransfer { it -> it.copy(backupBackgroundDots = (if (mainViewModel.transfer.value.backupBackgroundDots >= 3) 1 else mainViewModel.transfer.value.backupBackgroundDots + 1)) }
            }
        }
    }

    internal fun stopBackupDotAnimation() {
        backupDotJob?.cancel()
        backupDotJob = null
    }

    internal fun exportSelectedToExternal() {
        if (mainViewModel.shell.value.outputTreeUri == null) {
            toastStatus("还没有设置目录")
            mainViewModel.updatePreviewSession { it -> it.copy(exportDialogVisible = (true)) }
            return
        }
        val dir = mainViewModel.previewSession.value.activeGenerationSession?.outDir
            ?: mainViewModel.previewSession.value.previewDirPath?.let { File(it) }?.takeIf { it.isDirectory && hasGeneratedPackageBaseAssets(it) }
            ?: mainViewModel.picker.value.selectedPackageName?.let { artPlusPackageDir(it) }?.takeIf { hasGeneratedPackageBaseAssets(it) }
        if (dir == null || !hasGeneratedPackageBaseAssets(dir)) {
            toastStatus("没有可导出的图标包")
            return
        }
        if (mainViewModel.shell.value.isBusy) return
        mainViewModel.updateShell { it -> it.copy(isBusy = (true)) }
        mainViewModel.updateTransfer { it -> it.copy(exportProgress = (ExportProgress(
            title = "导出中",
            completed = 0,
            total = 1,
            currentLabel = "正在导出: ${dir.name}",
            isIndeterminate = true,
        ))) }
        mainViewModel.updateTransfer { it -> it.copy(singleExportSheetVisible = (true)) }
        singleExportJob?.cancel()
        singleExportJob = mainScope.launch(Dispatchers.IO) {
            try {
                runCatching { ensureNomediaAtTreeRoot(contentResolver, mainViewModel.shell.value.outputTreeUri) }
                // 优先尝试文件系统直拷（su cp），速度为 SAF 的 10-20 倍，失败再回退 SAF
                val fastOk = runCatching { exportToTreeFast(mainViewModel.shell.value.outputTreeUri, dir) }.getOrDefault(false)
                if (fastOk) {
                    withContext(Dispatchers.Main) { toastStatus("已导出到外部目录: ${dir.name}") }
                } else {
                    withContext(Dispatchers.Main) {
                        runCatching { exportToTree(contentResolver, mainViewModel.shell.value.outputTreeUri, dir) }
                            .onSuccess { toastStatus("已导出到外部目录: ${dir.name}") }
                            .onFailure { error -> toastStatus("导出失败: ${error.message ?: error.javaClass.simpleName}") }
                    }
                }
            } catch (e: CancellationException) {
                withContext(Dispatchers.Main) { /* 已在 cancelSingleExport 中处理 */ }
                throw e
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { toastStatus("导出失败: ${e.message ?: e.javaClass.simpleName}") }
            } finally {
                withContext(Dispatchers.Main) {
                    mainViewModel.updateTransfer { it -> it.copy(exportProgress = (null)) }
                    mainViewModel.updateTransfer { it -> it.copy(singleExportSheetVisible = (false)) }
                    singleExportJob = null
                    mainViewModel.updateShell { it -> it.copy(isBusy = (false)) }
                }
            }
        }
    }

    internal fun backupAllToExternal(isFromOnboarding: Boolean = false) {
        if (mainViewModel.shell.value.outputTreeUri == null) {
            toastStatus("还没有设置目录")
            mainViewModel.updatePreviewSession { it -> it.copy(exportDialogVisible = (true)) }
            return
        }
        if (mainViewModel.shell.value.isBusy) return
        // 若已有备份任务，仅重显弹窗
        if (backupJob?.isActive == true) {
            mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (true)) }
            mainViewModel.updateTransfer { it -> it.copy(backupInBackground = (false)) }
            stopBackupDotAnimation()
            return
        }
        mainViewModel.updateShell { it -> it.copy(isBusy = (true)) }
        mainViewModel.updateTransfer { it -> it.copy(backupInBackground = (false)) }
        mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (true)) }
        mainViewModel.updateTransfer { it -> it.copy(backupBackgroundDots = (1)) }
        mainViewModel.updateTransfer { it -> it.copy(backupProgress = (ExportProgress(
            title = "备份中",
            completed = 0,
            total = 1,
            currentLabel = "正在准备...",
            isIndeterminate = true,
        ))) }
        toastStatus("正在备份...")
        backupJob?.cancel()
        backupDotJob?.cancel()
        backupJob = mainScope.launch(Dispatchers.IO) {
            try {
                runCatching { ensureNomediaAtTreeRoot(contentResolver, mainViewModel.shell.value.outputTreeUri) }
                val pkgs = listRootIconPackages()
                if (pkgs.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        mainViewModel.updateTransfer { it -> it.copy(backupProgress = (null)) }
                        mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (false)) }
                        toastStatus("没有可导出的图标包")
                    }
                    return@launch
                }
                val treeUri = mainViewModel.shell.value.outputTreeUri
                if (treeUri == null) {
                    withContext(Dispatchers.Main) {
                        mainViewModel.updateTransfer { it -> it.copy(backupProgress = (null)) }
                        mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (false)) }
                        toastStatus("还没有设置目录")
                    }
                    return@launch
                }
                withContext(Dispatchers.Main) {
                    mainViewModel.updateTransfer { it -> it.copy(backupProgress = (ExportProgress(
                        title = "备份中",
                        completed = 0,
                        total = pkgs.size,
                        currentLabel = "准备备份 ${pkgs.size} 个图标包",
                        isIndeterminate = false,
                    ))) }
                }
                var successCount = 0
                var failCount = 0
                val destRootFast = resolveTreeUriToFilePath(treeUri)
                // 情况1：可解析为文件系统路径 -> 使用 su 直拷（一次 su per pkg，约 10ms/包），最快
                if (destRootFast != null) {
                    for ((index, pkgName) in pkgs.withIndex()) {
                        ensureActive()
                        withContext(Dispatchers.Main) {
                            mainViewModel.updateTransfer { it -> it.copy(backupProgress = (ExportProgress(
                                title = "备份中",
                                completed = index,
                                total = pkgs.size,
                                currentLabel = "正在备份 ${index + 1}/${pkgs.size}: $pkgName",
                                isIndeterminate = false,
                            ))) }
                            mainViewModel.updateShell { it -> it.copy(statusText = ("正在备份 ${index + 1}/${pkgs.size}: $pkgName")) }
                        }
                        val ok = runCatching { backupPackageFast(pkgName, destRootFast) }.getOrDefault(false)
                        if (ok) successCount++ else failCount++
                        withContext(Dispatchers.Main) {
                            mainViewModel.updateTransfer { it -> it.copy(backupProgress = (ExportProgress(
                                title = "备份中",
                                completed = index + 1,
                                total = pkgs.size,
                                currentLabel = if (ok) "已完成 ${index + 1}/${pkgs.size}: $pkgName" else "失败 $pkgName",
                                isIndeterminate = false,
                            ))) }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        if (!mainViewModel.transfer.value.backupInBackground) {
                            mainViewModel.updateTransfer { it -> it.copy(backupProgress = (null)) }
                            mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (false)) }
                        }
                        if (failCount == 0) toastStatus("已备份 $successCount 个图标包")
                        else toastStatus("已备份 $successCount 个，失败 $failCount 个")
                    }
                } else {
                    // 情况2：无法解析路径（SD卡/特殊 Provider）-> 回退 SAF 中转缓存方案
                    val stagingRoot = File(cacheDir, "backup_staging").also { it.mkdirs() }
                    for ((index, pkgName) in pkgs.withIndex()) {
                        ensureActive()
                        withContext(Dispatchers.Main) {
                            mainViewModel.updateTransfer { it -> it.copy(backupProgress = (ExportProgress(
                                title = "备份中",
                                completed = index,
                                total = pkgs.size,
                                currentLabel = "正在备份 ${index + 1}/${pkgs.size}: $pkgName",
                                isIndeterminate = false,
                            ))) }
                            mainViewModel.updateShell { it -> it.copy(statusText = ("正在备份 ${index + 1}/${pkgs.size}: $pkgName")) }
                        }
                        val stagingDir = File(stagingRoot, pkgName)
                        try {
                            if (stagingDir.exists()) stagingDir.deleteRecursively()
                            stagingDir.mkdirs()
                            val src = "$ROOT_UXICONS_DIR/$pkgName"
                            val cmd = "cp -f ${shQuote(src)}/*.png ${shQuote(stagingDir.absolutePath)}/ 2>/dev/null; echo done"
                            runRootCommand(cmd, timeoutMs = 8000)
                            val files = stagingDir.listFiles { _, name -> name.endsWith(".png") }
                            if (files == null || files.isEmpty()) {
                                withContext(Dispatchers.Main) {
                                    mainViewModel.updateTransfer { it -> it.copy(backupProgress = (ExportProgress(
                                        title = "备份中",
                                        completed = index + 1,
                                        total = pkgs.size,
                                        currentLabel = "已跳过 ${pkgName}（无图标）",
                                        isIndeterminate = false,
                                    ))) }
                                }
                                continue
                            }
                            withContext(Dispatchers.Main) {
                                runCatching { exportToTree(contentResolver, treeUri, stagingDir) }.onSuccess { successCount++ }.onFailure { failCount++ }
                                mainViewModel.updateTransfer { it -> it.copy(backupProgress = (ExportProgress(
                                    title = "备份中",
                                    completed = index + 1,
                                    total = pkgs.size,
                                    currentLabel = "已完成 ${index + 1}/${pkgs.size}: $pkgName",
                                    isIndeterminate = false,
                                ))) }
                            }
                        } catch (_: Exception) {
                            failCount++
                            withContext(Dispatchers.Main) {
                                mainViewModel.updateTransfer { it -> it.copy(backupProgress = (ExportProgress(
                                    title = "备份中",
                                    completed = index + 1,
                                    total = pkgs.size,
                                    currentLabel = "失败 ${pkgName}",
                                    isIndeterminate = false,
                                ))) }
                            }
                        }
                    }
                    runCatching { stagingRoot.deleteRecursively() }
                    withContext(Dispatchers.Main) {
                        if (!mainViewModel.transfer.value.backupInBackground) {
                            mainViewModel.updateTransfer { it -> it.copy(backupProgress = (null)) }
                            mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (false)) }
                        }
                        if (failCount == 0) toastStatus("已备份 $successCount 个图标包")
                        else toastStatus("已备份 $successCount 个，失败 $failCount 个")
                    }
                }
            } catch (e: CancellationException) {
                withContext(Dispatchers.Main) {
                    // 停止时已由 cancelBackup 清理
                }
                throw e
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mainViewModel.updateTransfer { it -> it.copy(backupProgress = (null)) }
                    mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (false)) }
                    mainViewModel.updateTransfer { it -> it.copy(backupInBackground = (false)) }
                    toastStatus("备份失败: ${e.message ?: e.javaClass.simpleName}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    mainViewModel.updateShell { it -> it.copy(isBusy = (false)) }
                    backupJob = null
                    stopBackupDotAnimation()
                    if (!mainViewModel.transfer.value.backupInBackground) {
                        mainViewModel.updateTransfer { it -> it.copy(backupProgress = (null)) }
                        mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (false)) }
                        mainViewModel.updateTransfer { it -> it.copy(backupInBackground = (false)) }
                    }
                    // 若为后台，则保留 backupProgress 供设置页“备份中...”展示
                }
            }
        }
    }

    internal fun startDebugHttpServerIfNeeded() {
        if (!isDebugBuild()) {
            return
        }
        if (debugHttpServer != null) {
            return
        }
        debugToken()
        // P4 交界：server 顶层化进 system/DebugServer，不持 Activity；
        // 经 DebugServerHooks 回调解耦（currentDebugParamsOnMain/applyDebugParams 系留置，
        // 读 186 live vars，P5 再议）。
        debugHttpServer = DebugHttpServer(
            DEBUG_HTTP_PORT,
            object : DebugServerHooks {
                override fun onStatus(message: String) = status(message)
                override fun homeHtml(): String = debugHomeHtml()
                override fun currentParams(): JSONObject = currentDebugParamsOnMain()
                override fun applyParams(params: Map<String, String>): JSONObject = applyDebugParams(params)
                override fun inspectPackage(packageName: String, includeRmbg: Boolean): JSONObject =
                    debugInspectPackage(packageName, includeRmbg)
                override fun startGeneration(
                    packageName: String,
                    useGpt: Boolean,
                    installWithRoot: Boolean,
                    debugMode: LocalSeparationMode,
                    rootWriteMode: RootWriteMode,
                ): Boolean = startDebugGeneration(packageName, useGpt, installWithRoot, debugMode, rootWriteMode)
                override fun isTokenValid(token: String?): Boolean = isDebugTokenValid(token)
            },
        ).also { it.start() }
    }

    internal fun isDebugBuild(): Boolean =
        (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    internal fun debugToken(): String {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val existing = prefs.getString(PREF_DEBUG_TOKEN, null)
            ?.takeIf { it.length >= 32 }
        if (existing != null) {
            return existing
        }
        val created = UUID.randomUUID().toString() + UUID.randomUUID().toString()
        prefs.edit().putString(PREF_DEBUG_TOKEN, created).apply()
        return created
    }

    internal fun isDebugTokenValid(token: String?): Boolean =
        token != null && token == debugToken()

    internal fun runOnMainSync(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
            return
        }
        val latch = CountDownLatch(1)
        var failure: Throwable? = null
        runOnUiThread {
            try {
                action()
            } catch (error: Throwable) {
                failure = error
            } finally {
                latch.countDown()
            }
        }
        if (!latch.await(5, TimeUnit.SECONDS)) {
            error("main thread timeout")
        }
        failure?.let { throw it }
    }

    internal fun currentDebugParamsJson(): JSONObject =
        JSONObject()
            .put("port", DEBUG_HTTP_PORT)
            .put("busy", mainViewModel.shell.value.isBusy)
            .put("status", mainViewModel.shell.value.statusText)
            .put("foreground_subject_percent", mainViewModel.params.value.foregroundSubjectPercent)
            .put("foreground_shadow_level", mainViewModel.params.value.foregroundShadowLevel)
            .put("monochrome_theme_scale", (mainViewModel.params.value.monochromeThemeScale * 100).roundToInt())
            .put("gpt_mode", GptImageMode.fromValue(mainViewModel.params.value.gptImageMode).value)
            .put("gpt_prompt_preset", GptPromptPreset.fromValue(mainViewModel.params.value.gptPromptPreset).value)
            .put("gpt_custom_prompt", mainViewModel.params.value.gptCustomPrompt)
            .put("gpt_base_url", mainViewModel.gptRmbgSettings.value.gptBaseUrl)
            .put("gpt_api_key_set", mainViewModel.gptRmbgSettings.value.gptApiKey.isNotBlank())
            .put("background_separation_percent", mainViewModel.params.value.backgroundSeparationPercent)
            .put("plate_removal_percent", mainViewModel.params.value.plateRemovalPercent)
            .put("shadow_removal_percent", mainViewModel.params.value.shadowRemovalPercent)
            .put("edge_polish_percent", mainViewModel.params.value.edgePolishPercent)
            .put("rmbg_alpha_strength_percent", mainViewModel.params.value.rmbgAlphaStrengthPercent)
            .put("rmbg_edge_feather_percent", mainViewModel.params.value.rmbgEdgeFeatherPercent)
            .put("rmbg_edge_adjust_percent", mainViewModel.params.value.rmbgEdgeAdjustPercent)
            .put("rmbg_weak_alpha_keep_percent", mainViewModel.params.value.rmbgWeakAlphaKeepPercent)
            .put("liquid_glass_enabled", mainViewModel.params.value.liquidGlassEnabled)
            .put("liquid_glass_radius", mainViewModel.params.value.liquidGlassRadius)
            .put("liquid_glass_outer_width", mainViewModel.params.value.liquidGlassOuterWidth)
            .put("liquid_glass_top_alpha", mainViewModel.params.value.liquidGlassTopAlpha)
            .put("liquid_glass_bottom_alpha", mainViewModel.params.value.liquidGlassBottomAlpha)
            .put("liquid_glass_background_mist_alpha", mainViewModel.params.value.liquidGlassBackgroundMistAlpha)
            .put("liquid_glass_bottom_dark_alpha", mainViewModel.params.value.liquidGlassBottomDarkAlpha)
            .put("liquid_glass_subject_scale_percent", mainViewModel.params.value.liquidGlassSubjectScalePercent)
            .put("liquid_glass_subject_outline_width", mainViewModel.params.value.liquidGlassSubjectOutlineWidth)
            .put("liquid_glass_subject_inner_outline_width", mainViewModel.params.value.liquidGlassSubjectInnerOutlineWidth)
            .put("liquid_glass_subject_shadow_alpha", mainViewModel.params.value.liquidGlassSubjectShadowAlpha)
            .put("liquid_glass_subject_opacity_percent", mainViewModel.params.value.liquidGlassSubjectOpacityPercent)
            .put("liquid_glass_param_labels", liquidGlassParamLabelsJson())
            .put("rmbg_model_installed", findRmbgComponent() != null)
            .put("rmbg_component_installed", findRmbgComponent() != null)
            .put("rmbg_component_abi", findRmbgComponent()?.abi ?: "")
            .put("rmbg_model_name", RMBG_MODEL_NAME)
            .put("rmbg_status", rmbgInferenceStatusSummary())
            .put("rmbg_actual_backend", mainViewModel.previewSession.value.lastRmbgInferenceReport?.actualBackend?.value ?: "")
            .put("rmbg_inference_elapsed_ms", mainViewModel.previewSession.value.lastRmbgInferenceReport?.elapsedMs ?: JSONObject.NULL)
            .put("rmbg_last_error", mainViewModel.previewSession.value.lastRmbgCandidateError ?: "")
            .put("adaptive_foreground_mode", AdaptiveForegroundMode.fromValue(mainViewModel.params.value.adaptiveForegroundMode).value)
            .put("adaptive_foreground_modes", JSONArray().also { array ->
                AdaptiveForegroundMode.entries.forEach { mode ->
                    array.put(JSONObject().put("value", mode.value).put("label", mode.label))
                }
            })
            .put("adaptive_direct_max_coverage_percent", mainViewModel.params.value.adaptiveDirectMaxCoveragePercent)
            .put("adaptive_direct_max_coverage_increase_percent", mainViewModel.params.value.adaptiveDirectMaxCoverageIncreasePercent)
            .put("adaptive_mask_edge_coverage_percent", mainViewModel.params.value.adaptiveMaskEdgeCoveragePercent)
            .put("adaptive_mask_min_coverage_percent", mainViewModel.params.value.adaptiveMaskMinCoveragePercent)
            .put("adaptive_center_epsilon_percent", mainViewModel.params.value.adaptiveCenterEpsilonPercent)
            .put("original_foreground_cleanup_mode", OriginalForegroundCleanupMode.fromValue(mainViewModel.params.value.originalForegroundCleanupMode).value)
            .put("local_background_separation_enabled", mainViewModel.params.value.localBackgroundSeparationEnabled)
            .put("local_adaptive_selection_enabled", mainViewModel.params.value.localAdaptiveSelectionEnabled)
            .put("local_corner_mask_cleanup_enabled", mainViewModel.params.value.localCornerMaskCleanupEnabled)
            .put("local_alpha_edge_color_repair_enabled", mainViewModel.params.value.localAlphaEdgeColorRepairEnabled)
            .put("local_plain_background_estimation_enabled", mainViewModel.params.value.localPlainBackgroundEstimationEnabled)
            .put("local_original_cleanup_enabled", mainViewModel.params.value.localOriginalCleanupEnabled)
            .put("local_plate_cleanup_enabled", mainViewModel.params.value.localPlateCleanupEnabled)
            .put("local_plate_edge_repair_enabled", mainViewModel.params.value.localPlateEdgeRepairEnabled)
            .put("local_plate_residue_cleanup_enabled", mainViewModel.params.value.localPlateResidueCleanupEnabled)
            .put("local_shadow_cleanup_enabled", mainViewModel.params.value.localShadowCleanupEnabled)
            .put("local_shadow_edge_repair_enabled", mainViewModel.params.value.localShadowEdgeRepairEnabled)
            .put("local_edge_trim_enabled", mainViewModel.params.value.localEdgeTrimEnabled)
            .put("local_composed_background_enabled", mainViewModel.params.value.localComposedBackgroundEnabled)
            .put("local_two_layer_candidate_enabled", mainViewModel.params.value.localTwoLayerCandidateEnabled)
            .put("local_component_candidates_enabled", mainViewModel.params.value.localComponentCandidatesEnabled)
            .put("local_text_safe_candidate_enabled", mainViewModel.params.value.localTextSafeCandidateEnabled)
            .put("local_auto_selection_enabled", mainViewModel.params.value.localAutoSelectionEnabled)
            .put("local_edge_polish_enabled", mainViewModel.params.value.localEdgePolishEnabled)
            .put("original_foreground_cleanup_modes", JSONArray().also { array ->
                OriginalForegroundCleanupMode.entries.forEach { mode ->
                    array.put(JSONObject().put("value", mode.value).put("label", mode.label))
                }
            })
            .put(
                "ranges",
                JSONObject()
                    .put("foreground_subject_percent", intRangeJson(MIN_FOREGROUND_SUBJECT_PERCENT, MAX_FOREGROUND_SUBJECT_PERCENT))
                    .put("foreground_shadow_level", intRangeJson(MIN_FOREGROUND_SHADOW_LEVEL, MAX_FOREGROUND_SHADOW_LEVEL))
                    .put(
                        "monochrome_theme_scale",
                        intRangeJson(MIN_MONOCHROME_THEME_SCALE_PERCENT, MAX_MONOCHROME_THEME_SCALE_PERCENT),
                    )
                    .put("background_separation_percent", intRangeJson(MIN_BACKGROUND_SEPARATION_PERCENT, MAX_BACKGROUND_SEPARATION_PERCENT))
                    .put("plate_removal_percent", intRangeJson(MIN_PLATE_REMOVAL_PERCENT, MAX_PLATE_REMOVAL_PERCENT))
                    .put("shadow_removal_percent", intRangeJson(MIN_SHADOW_REMOVAL_PERCENT, MAX_SHADOW_REMOVAL_PERCENT))
                    .put("edge_polish_percent", intRangeJson(MIN_EDGE_POLISH_PERCENT, MAX_EDGE_POLISH_PERCENT))
                    .put(
                        "rmbg_alpha_strength_percent",
                        intRangeJson(MIN_RMBG_ALPHA_STRENGTH_PERCENT, MAX_RMBG_ALPHA_STRENGTH_PERCENT),
                    )
                    .put(
                        "rmbg_edge_feather_percent",
                        intRangeJson(MIN_RMBG_EDGE_FEATHER_PERCENT, MAX_RMBG_EDGE_FEATHER_PERCENT),
                    )
                    .put(
                        "rmbg_edge_adjust_percent",
                        intRangeJson(MIN_RMBG_EDGE_ADJUST_PERCENT, MAX_RMBG_EDGE_ADJUST_PERCENT),
                    )
                    .put(
                        "rmbg_weak_alpha_keep_percent",
                        intRangeJson(MIN_RMBG_WEAK_ALPHA_KEEP_PERCENT, MAX_RMBG_WEAK_ALPHA_KEEP_PERCENT),
                    )
                    .put("liquid_glass_radius", intRangeJson(MIN_LIQUID_GLASS_RADIUS, MAX_LIQUID_GLASS_RADIUS))
                    .put("liquid_glass_outer_width", intRangeJson(MIN_LIQUID_GLASS_OUTER_WIDTH, MAX_LIQUID_GLASS_OUTER_WIDTH))
                    .put("liquid_glass_top_alpha", intRangeJson(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA))
                    .put("liquid_glass_bottom_alpha", intRangeJson(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA))
                    .put("liquid_glass_background_mist_alpha", intRangeJson(MIN_LIQUID_GLASS_MIST_ALPHA, MAX_LIQUID_GLASS_MIST_ALPHA))
                    .put(
                        "liquid_glass_bottom_dark_alpha",
                        intRangeJson(MIN_LIQUID_GLASS_BOTTOM_DARK_ALPHA, MAX_LIQUID_GLASS_BOTTOM_DARK_ALPHA),
                    )
                    .put(
                        "liquid_glass_subject_scale_percent",
                        intRangeJson(MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT, MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT),
                    )
                    .put(
                        "liquid_glass_subject_outline_width",
                        intRangeJson(MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH, MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH),
                    )
                    .put(
                        "liquid_glass_subject_inner_outline_width",
                        intRangeJson(MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH, MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH),
                    )
                    .put(
                        "liquid_glass_subject_shadow_alpha",
                        intRangeJson(MIN_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA, MAX_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA),
                    )
                    .put(
                        "liquid_glass_subject_opacity_percent",
                        intRangeJson(MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT, MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT),
                    )
                    .put(
                        "adaptive_direct_max_coverage_percent",
                        intRangeJson(MIN_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT, MAX_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT),
                    )
                    .put(
                        "adaptive_direct_max_coverage_increase_percent",
                        intRangeJson(
                            MIN_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
                            MAX_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
                        ),
                    )
                    .put(
                        "adaptive_mask_edge_coverage_percent",
                        intRangeJson(MIN_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT, MAX_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT),
                    )
                    .put(
                        "adaptive_mask_min_coverage_percent",
                        intRangeJson(MIN_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT, MAX_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT),
                    )
                    .put(
                        "adaptive_center_epsilon_percent",
                        intRangeJson(MIN_ADAPTIVE_CENTER_EPSILON_PERCENT, MAX_ADAPTIVE_CENTER_EPSILON_PERCENT),
                    ),
            )

    internal fun intRangeJson(min: Int, max: Int): JSONObject =
        JSONObject().put("min", min).put("max", max)

    internal fun liquidGlassParamLabelsJson(): JSONObject =
        JSONObject()
            .put("liquid_glass_enabled", "启用液态玻璃")
            .put("liquid_glass_radius", "玻璃圆角")
            .put("liquid_glass_outer_width", "外框高度")
            .put("liquid_glass_top_alpha", "顶部强度")
            .put("liquid_glass_bottom_alpha", "底边强度")
            .put("liquid_glass_background_mist_alpha", "背景灰雾")
            .put("liquid_glass_bottom_dark_alpha", "底部灰雾")
            .put("liquid_glass_subject_scale_percent", "主体比例")
            .put("liquid_glass_subject_outline_width", "主体外框宽度")
            .put("liquid_glass_subject_inner_outline_width", "主体内框宽度")
            .put("liquid_glass_subject_shadow_alpha", "主体阴影")
            .put("liquid_glass_subject_opacity_percent", "主体透明度")

    // P4 注明：读 Activity 调参，留置（system/DebugServer 经 hooks.currentParams() 回调）。
    internal fun currentDebugParamsOnMain(): JSONObject {
        var snapshot: JSONObject? = null
        runOnMainSync {
            snapshot = currentDebugParamsJson()
        }
        return snapshot ?: error("debug params unavailable")
    }

    // P4 注明：写 Activity 调参，留置（system/DebugServer 经 hooks.applyParams() 回调）。
    internal fun applyDebugParams(params: Map<String, String>): JSONObject {
        var snapshot: JSONObject? = null
        runOnMainSync {
            check(!mainViewModel.shell.value.isBusy) { "当前任务正在运行，不能修改参数" }
            // AI 凭据不走 TuningParams（预设不导出密钥），单独处理。
            params["gpt_base_url"]?.let { mainViewModel.updateGptRmbgSettings { v -> v.copy(gptBaseUrl = (it)) } }
            params["gpt_api_key"]?.let { mainViewModel.updateGptRmbgSettings { v -> v.copy(gptApiKey = (it)) } }
            val gptCredentialChanged =
                params.containsKey("gpt_base_url") || params.containsKey("gpt_api_key")
            applyTuningParams(
                TuningParams.fromParamMap(params, currentTuningParams()),
                captureUndo = false,
            )
            if (gptCredentialChanged) {
                saveGptSettings()
            }
            snapshot = currentDebugParamsJson()
        }
        return snapshot ?: error("debug params unavailable")
    }

    internal fun debugHomeHtml(): String = """
        <!doctype html>
        <html>
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <title>ArtPlus Debug</title>
          <style>
            body{font-family:system-ui,-apple-system,BlinkMacSystemFont,sans-serif;margin:24px;background:#f5f6f8;color:#1b1d22}
            main{max-width:760px;margin:auto;background:white;border-radius:18px;padding:20px;box-shadow:0 10px 30px #0001}
            label{display:grid;grid-template-columns:1fr 140px;gap:12px;align-items:center;margin:10px 0}
            input,select,button{font:inherit;padding:9px 11px;border-radius:10px;border:1px solid #ccd1d8}
            button{background:#1d6fff;color:white;border:0;margin-top:12px}
            code,pre{background:#eef1f5;border-radius:10px;padding:10px;display:block;overflow:auto}
          </style>
        </head>
        <body>
        <main>
          <h1>ArtPlus Debug</h1>
          <p>Hidden debug surface. POST /debug/params with JSON to tune without rebuilding.</p>
          <form id="params"></form>
          <button id="save" type="button">Save Params</button>
          <h2>Generate</h2>
          <label>Package <input id="packageName" value="io.github.vvb2060.magisk"></label>
          <label>Mode <select id="mode"><option>original</option><option>auto</option><option>plate</option><option>full</option></select></label>
          <label>Root write <select id="rootWriteMode"><option>all</option><option>default</option><option>monochrome</option></select></label>
          <button id="generate" type="button">Generate</button>
          <h2>Status</h2>
          <pre id="out"></pre>
        </main>
        <script>
        let labels = {};
        const numericKeys = [
          'foreground_subject_percent','foreground_shadow_level',
          'background_separation_percent','plate_removal_percent','shadow_removal_percent','edge_polish_percent',
          'rmbg_alpha_strength_percent','rmbg_edge_feather_percent','rmbg_edge_adjust_percent','rmbg_weak_alpha_keep_percent',
          'liquid_glass_radius','liquid_glass_outer_width','liquid_glass_top_alpha','liquid_glass_bottom_alpha',
          'liquid_glass_background_mist_alpha','liquid_glass_bottom_dark_alpha',
          'liquid_glass_subject_scale_percent','liquid_glass_subject_outline_width',
          'liquid_glass_subject_inner_outline_width','liquid_glass_subject_shadow_alpha','liquid_glass_subject_opacity_percent',
          'adaptive_direct_max_coverage_percent','adaptive_direct_max_coverage_increase_percent',
          'adaptive_mask_edge_coverage_percent','adaptive_mask_min_coverage_percent','adaptive_center_epsilon_percent'
        ];
        const checkboxKeys = [
          'liquid_glass_enabled',
          'local_background_separation_enabled','local_adaptive_selection_enabled',
          'local_corner_mask_cleanup_enabled','local_alpha_edge_color_repair_enabled',
          'local_plain_background_estimation_enabled','local_original_cleanup_enabled',
          'local_plate_cleanup_enabled','local_plate_edge_repair_enabled',
          'local_plate_residue_cleanup_enabled','local_shadow_cleanup_enabled',
          'local_shadow_edge_repair_enabled','local_edge_trim_enabled',
          'local_composed_background_enabled','local_two_layer_candidate_enabled',
          'local_component_candidates_enabled','local_text_safe_candidate_enabled',
          'local_auto_selection_enabled','local_edge_polish_enabled'
        ];
        const colorKeys = [];
        const selectSpecs = [];
        async function load(){
          const data = await fetch('/debug/params').then(r=>r.json());
          labels = data.liquid_glass_param_labels || {};
	          const form = document.getElementById('params');
	          form.innerHTML = '';
	          const select = document.createElement('select');
	          data.adaptive_foreground_modes.forEach(m => {
	            const option = document.createElement('option');
            option.value = m.value; option.textContent = m.value + ' - ' + m.label;
            option.selected = m.value === data.adaptive_foreground_mode;
            select.appendChild(option);
          });
          select.name = 'adaptive_foreground_mode';
          form.appendChild(row('adaptive_foreground_mode', select));
          const originalCleanup = document.createElement('select');
          data.original_foreground_cleanup_modes.forEach(m => {
            const option = document.createElement('option');
            option.value = m.value; option.textContent = m.value + ' - ' + m.label;
            option.selected = m.value === data.original_foreground_cleanup_mode;
            originalCleanup.appendChild(option);
          });
          originalCleanup.name = 'original_foreground_cleanup_mode';
          form.appendChild(row('original_foreground_cleanup_mode', originalCleanup));
          selectSpecs.forEach(([key, optionsKey]) => {
            const input = document.createElement('select');
            (data[optionsKey] || []).forEach(m => {
              const option = document.createElement('option');
              option.value = m.value; option.textContent = m.value + ' - ' + m.label;
              option.selected = m.value === data[key];
              input.appendChild(option);
            });
            input.name = key;
            form.appendChild(row(key, input));
          });
          checkboxKeys.forEach(k => {
            const input = document.createElement('input');
            input.type = 'checkbox'; input.name = k; input.checked = !!data[k];
            form.appendChild(row(k, input));
          });
          colorKeys.forEach(k => {
            const input = document.createElement('input');
            input.type = 'text'; input.name = k; input.value = data[k] || '';
            form.appendChild(row(k, input));
          });
          numericKeys.forEach(k => {
            const input = document.createElement('input');
            input.type = 'number'; input.name = k; input.value = data[k]; input.step = 'any';
            if (data.ranges[k]) { input.min = data.ranges[k].min; input.max = data.ranges[k].max; }
            form.appendChild(row(k, input));
          });
          document.getElementById('out').textContent = JSON.stringify(data, null, 2);
        }
        function row(label, input){ const l=document.createElement('label'); const s=document.createElement('span'); s.textContent=labels[label] || label; l.appendChild(s); l.appendChild(input); return l; }
        document.getElementById('save').onclick = async () => {
          const body = {};
          new FormData(document.getElementById('params')).forEach((v,k)=>body[k]=v);
          checkboxKeys.forEach(k => { const el = document.querySelector('[name="'+k+'"]'); body[k] = el && el.checked ? 'true' : 'false'; });
          document.getElementById('out').textContent = await fetch('/debug/params',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)}).then(r=>r.text());
          load();
        };
        document.getElementById('generate').onclick = async () => {
	          const pkg = encodeURIComponent(document.getElementById('packageName').value);
	          const mode = encodeURIComponent(document.getElementById('mode').value);
	          const rootWriteMode = encodeURIComponent(document.getElementById('rootWriteMode').value);
	          document.getElementById('out').textContent = await fetch('/debug/generate?package='+pkg+'&mode='+mode+'&root_write_mode='+rootWriteMode,{method:'POST'}).then(r=>r.text());
	        };
        load();
        </script>
        </body>
        </html>
    """.trimIndent()

    internal fun loadPreviewAssets(dir: File): PreviewAssets =
        PreviewAssets(
            recbg = decodePreviewBitmap(dir, "recbg.png"),
            recfg = decodePreviewBitmap(dir, "recfg.png"),
            recNight = decodePreviewBitmap(dir, "rec_night.png"),
            monochromeLight = decodePreviewBitmap(dir, "monochrome_light.png")
                ?: decodePreviewBitmap(dir, "monochrome.png"),
            monochromeDark = decodePreviewBitmap(dir, "monochrome_dark.png")
                ?: decodePreviewBitmap(dir, "monochrome.png"),
        )

    internal fun decodePreviewBitmap(dir: File, name: String): Bitmap? =
        BitmapFactory.decodeFile(File(dir, name).absolutePath)?.also { it.prepareToDraw() }











    /** 生成设置内部的「可视化 / JSON」二级切换。 */


    companion object {
        // Slice 2.5 已提升到 TuningParams.kt：PREFS_NAME / PREF_AUTO_CONFIRM_ROOT_WRITE /
        // PREF_AUTO_CONFIRM_REFRESH / PREF_SKIP_ROOT_WRITE_CONFIRM / PREF_USAGE_PERMISSION_PROMPTED /
        // PREF_SELECTED_PACKAGE_NAME / PREF_GENERATED_FILTER / PREF_QUERY_TEXT /
        // PREF_ADVANCED_SETTINGS_CATEGORY / PREF_ADVANCED_SETTINGS_TAB /
        // PREF_PREVIEW_PACKAGE_NAME / PREF_PREVIEW_DIR_PATH / PREF_PREVIEW_STRIP_ENABLED /
        // PREF_BATCH_PREVIEW_COUNT / PREF_BATCH_PREVIEW_COLUMNS /
        // PREF_BATCH_PREVIEW_ICON_SIZE_DP / PREF_BATCH_PREVIEW_CORNER_RADIUS_DP /
        // PREF_BATCH_PREVIEW_DESKTOP_BG / PREF_CUSTOM_WALLPAPER_PATH / CUSTOM_WALLPAPER_FILE /
        // PREF_PREVIEW_DESKTOP_BACKGROUND / PREF_PREVIEW_ICON_SIZE_DP /
        // PREF_PREVIEW_CORNER_RADIUS_DP / PREF_SHOW_SYSTEM_APPS / PREF_OUTPUT_TREE_URI /
        // PREF_HAS_COMPLETED_ONBOARDING / PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE，同包直接引用。
        private const val PREF_BATCH_OUTPUT_MODE = "batch_output_mode"
        private const val PREF_GPT_RUN_COUNT = "gpt_run_count"
        private const val PREF_RMBG_RUN_COUNT = "rmbg_run_count"
        private const val PREF_DEBUG_TOKEN = "debug_token"
        // Slice 1.6 已提升到 TuningParams.kt：EXTRA_DEBUG_GENERATE_*（6 项），同包直接引用。
        // Slice 1.4 已提升到 TuningParams.kt：SIZE_2X2 / SIZE_1X2 / SIZE_2X1 /
        // FOREGROUND_ORIGINAL_BACKUP_NAME，同包直接引用。
        // Slice 1.3 已提升到 TuningParams.kt：RMBG_COMPONENT_DIR / RMBG_MODEL_NAME /
        // DEFAULT_RMBG_INPUT_SIZE / RMBG_MIN_* / RMBG_MAX_* / RMBG_DOWNLOAD_* /
        // RMBG_MODEL_URL_* / DEFAULT_RMBG_COMPONENT_URL / RMBG_MODEL_PRESETS(PRESET_CUSTOM) /
        // RMBG_NORMALIZE_MEAN/STD，同包直接引用。
        // Slice 2.2 已提升到 TuningParams.kt：PREVIEW_OUTPUT_DEBOUNCE_MS /
        // PREVIEW_REBUILD_DEBOUNCE_MS / DEFAULT_PREVIEW_ICON_SIZE_DP /
        // MIN_PREVIEW_ICON_SIZE_DP / MAX_PREVIEW_ICON_SIZE_DP /
        // DEFAULT_PREVIEW_CORNER_RADIUS_DP / MIN_PREVIEW_CORNER_RADIUS_DP /
        // MAX_PREVIEW_CORNER_RADIUS_DP，同包直接引用。
        // Slice 2.3 已提升到 TuningParams.kt：DEFAULT/MIN/MAX_BATCH_PREVIEW_COUNT，同包直接引用。
        private val appIconCache = object : LruCache<String, Bitmap>(
            ((Runtime.getRuntime().maxMemory() / 1024) / 16).toInt().coerceAtLeast(4 * 1024),
        ) {
            override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount / 1024
        }
        // Slice 1.4 已提升到 TuningParams.kt：SIZE_2X2 / SIZE_1X2 / SIZE_2X1 /
        // FOREGROUND_ORIGINAL_BACKUP_NAME，同包直接引用。
        // Slice 1.3 已提升到 TuningParams.kt：RMBG_MIN/MAX_MANUAL/AUTO_COVERAGE /
        // RMBG_EDGE_ADJUST_MAX_RADIUS / RMBG_WEAK_ALPHA_MAX_CUT，同包直接引用。
    }
}

