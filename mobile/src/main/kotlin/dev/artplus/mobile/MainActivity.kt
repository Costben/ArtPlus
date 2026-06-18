package dev.artplus.mobile

import android.Manifest
import android.app.AppOpsManager
import android.app.WallpaperManager
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
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.LocalServerSocket
import android.net.LocalSocket
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowInsetsControllerCompat
import com.composables.icons.lucide.BadgeCheck
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Cpu
import com.composables.icons.lucide.Eraser
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
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.SlidersHorizontal
import com.composables.icons.lucide.Sparkles
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import android.graphics.Color as AndroidColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.caverock.androidsvg.SVG

class MainActivity : ComponentActivity() {
    private val rmbgGenerationGate = AtomicBoolean(false)
    private val previewWorkerDispatcher = Executors.newSingleThreadExecutor { task ->
        Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            task.run()
        }, "ArtPlusPreviewWorker").apply {
            priority = Thread.MIN_PRIORITY
        }
    }.asCoroutineDispatcher()
    private val previewWorkerScope = CoroutineScope(SupervisorJob() + previewWorkerDispatcher)
    private val apps = mutableStateListOf<AppEntry>()
    private var queryText by mutableStateOf("")
    private var selectedPackageName by mutableStateOf<String?>(null)
    private var statusText by mutableStateOf("加载应用列表中...")
    private var packageListPermissionGranted by mutableStateOf(true)
    private var usageAccessGranted by mutableStateOf(false)
    private var outputTreeUri by mutableStateOf<Uri?>(null)
    private var isBusy by mutableStateOf(false)
    private var didRequestAppLoad = false
    private var gptImageMode by mutableStateOf(GptImageMode.Responses)
    private var gptPromptPreset by mutableStateOf(GptPromptPreset.StableCutout)
    private var gptCustomPrompt by mutableStateOf("")
    private var gptBaseUrl by mutableStateOf("")
    private var gptApiKey by mutableStateOf("")
    private var gptSettingsSaveStatus by mutableStateOf("")
    private var localSeparationMode by mutableStateOf(LocalSeparationMode.Auto)
    private var foregroundSubjectPercent by mutableStateOf(DEFAULT_FOREGROUND_SUBJECT_PERCENT)
    private var foregroundShadowLevel by mutableStateOf(DEFAULT_FOREGROUND_SHADOW_LEVEL)
    private var draftForegroundShadowLevelText by mutableStateOf(DEFAULT_FOREGROUND_SHADOW_LEVEL.toString())
    private var monochromeThemeScale by mutableStateOf(DEFAULT_MONOCHROME_THEME_SCALE)
    private var draftMonochromeThemeScaleText by mutableStateOf((DEFAULT_MONOCHROME_THEME_SCALE * 100).roundToInt().toString())
    private var advancedSettingsCategory by mutableStateOf(AdvancedSettingsCategory.LiquidGlass)
    private var backgroundSeparationPercent by mutableStateOf(DEFAULT_BACKGROUND_SEPARATION_PERCENT)
    private var draftBackgroundSeparationText by mutableStateOf(DEFAULT_BACKGROUND_SEPARATION_PERCENT.toString())
    private var plateRemovalPercent by mutableStateOf(DEFAULT_PLATE_REMOVAL_PERCENT)
    private var draftPlateRemovalText by mutableStateOf(DEFAULT_PLATE_REMOVAL_PERCENT.toString())
    private var shadowRemovalPercent by mutableStateOf(DEFAULT_SHADOW_REMOVAL_PERCENT)
    private var draftShadowRemovalText by mutableStateOf(DEFAULT_SHADOW_REMOVAL_PERCENT.toString())
    private var edgePolishPercent by mutableStateOf(DEFAULT_EDGE_POLISH_PERCENT)
    private var draftEdgePolishText by mutableStateOf(DEFAULT_EDGE_POLISH_PERCENT.toString())
    private var rmbgAlphaStrengthPercent by mutableStateOf(DEFAULT_RMBG_ALPHA_STRENGTH_PERCENT)
    private var draftRmbgAlphaStrengthText by mutableStateOf(DEFAULT_RMBG_ALPHA_STRENGTH_PERCENT.toString())
    private var rmbgEdgeFeatherPercent by mutableStateOf(DEFAULT_RMBG_EDGE_FEATHER_PERCENT)
    private var draftRmbgEdgeFeatherText by mutableStateOf(DEFAULT_RMBG_EDGE_FEATHER_PERCENT.toString())
    private var rmbgEdgeAdjustPercent by mutableStateOf(DEFAULT_RMBG_EDGE_ADJUST_PERCENT)
    private var draftRmbgEdgeAdjustText by mutableStateOf(DEFAULT_RMBG_EDGE_ADJUST_PERCENT.toString())
    private var rmbgWeakAlphaKeepPercent by mutableStateOf(DEFAULT_RMBG_WEAK_ALPHA_KEEP_PERCENT)
    private var draftRmbgWeakAlphaKeepText by mutableStateOf(DEFAULT_RMBG_WEAK_ALPHA_KEEP_PERCENT.toString())
    private var liquidGlassEnabled by mutableStateOf(false)
    private var liquidGlassRadius by mutableStateOf(DEFAULT_LIQUID_GLASS_RADIUS)
    private var draftLiquidGlassRadiusText by mutableStateOf(DEFAULT_LIQUID_GLASS_RADIUS.toString())
    private var liquidGlassOuterWidth by mutableStateOf(DEFAULT_LIQUID_GLASS_OUTER_WIDTH)
    private var draftLiquidGlassOuterWidthText by mutableStateOf(DEFAULT_LIQUID_GLASS_OUTER_WIDTH.toString())
    private var liquidGlassTopAlpha by mutableStateOf(DEFAULT_LIQUID_GLASS_TOP_ALPHA)
    private var draftLiquidGlassTopAlphaText by mutableStateOf(DEFAULT_LIQUID_GLASS_TOP_ALPHA.toString())
    private var liquidGlassBottomAlpha by mutableStateOf(DEFAULT_LIQUID_GLASS_BOTTOM_ALPHA)
    private var draftLiquidGlassBottomAlphaText by mutableStateOf(DEFAULT_LIQUID_GLASS_BOTTOM_ALPHA.toString())
    private var liquidGlassBackgroundMistAlpha by mutableStateOf(DEFAULT_LIQUID_GLASS_BACKGROUND_MIST_ALPHA)
    private var draftLiquidGlassBackgroundMistAlphaText by mutableStateOf(DEFAULT_LIQUID_GLASS_BACKGROUND_MIST_ALPHA.toString())
    private var liquidGlassBottomDarkAlpha by mutableStateOf(DEFAULT_LIQUID_GLASS_BOTTOM_DARK_ALPHA)
    private var draftLiquidGlassBottomDarkAlphaText by mutableStateOf(DEFAULT_LIQUID_GLASS_BOTTOM_DARK_ALPHA.toString())
    private var liquidGlassSubjectScalePercent by mutableStateOf(DEFAULT_LIQUID_GLASS_SUBJECT_SCALE_PERCENT)
    private var draftLiquidGlassSubjectScaleText by mutableStateOf(DEFAULT_LIQUID_GLASS_SUBJECT_SCALE_PERCENT.toString())
    private var liquidGlassSubjectOutlineWidth by mutableStateOf(DEFAULT_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH)
    private var draftLiquidGlassSubjectOutlineWidthText by mutableStateOf(DEFAULT_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH.toString())
    private var liquidGlassSubjectInnerOutlineWidth by mutableStateOf(DEFAULT_LIQUID_GLASS_SUBJECT_INNER_OUTLINE_WIDTH)
    private var draftLiquidGlassSubjectInnerOutlineWidthText by mutableStateOf(DEFAULT_LIQUID_GLASS_SUBJECT_INNER_OUTLINE_WIDTH.toString())
    private var liquidGlassSubjectShadowAlpha by mutableStateOf(DEFAULT_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA)
    private var draftLiquidGlassSubjectShadowAlphaText by mutableStateOf(DEFAULT_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA.toString())
    private var liquidGlassSubjectOpacityPercent by mutableStateOf(DEFAULT_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT)
    private var draftLiquidGlassSubjectOpacityText by mutableStateOf(DEFAULT_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT.toString())
    private var adaptiveForegroundMode by mutableStateOf(AdaptiveForegroundMode.Auto)
    private var adaptiveDirectMaxCoveragePercent by mutableStateOf(DEFAULT_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT)
    private var adaptiveDirectMaxCoverageIncreasePercent by mutableStateOf(DEFAULT_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT)
    private var adaptiveMaskEdgeCoveragePercent by mutableStateOf(DEFAULT_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT)
    private var adaptiveMaskMinCoveragePercent by mutableStateOf(DEFAULT_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT)
    private var adaptiveCenterEpsilonPercent by mutableStateOf(DEFAULT_ADAPTIVE_CENTER_EPSILON_PERCENT)
    private var originalForegroundCleanupMode by mutableStateOf(OriginalForegroundCleanupMode.Auto)
    private var nightSubjectLightBackgroundEnabled by mutableStateOf(false)
    private var currentPage by mutableStateOf(AppPage.Home)
    private var generatedFilter by mutableStateOf(GeneratedFilter.All)
    private var generatedPackageNames by mutableStateOf<Set<String>>(emptySet())
    private var multiSelectedPackageNames by mutableStateOf<Set<String>>(emptySet())
    private var batchApplyProgress by mutableStateOf<BatchApplyProgress?>(null)
    private var isScanningGeneratedPackages by mutableStateOf(false)
    private var generatedScanFailed by mutableStateOf(false)
    private var previewPackageName by mutableStateOf<String?>(null)
    private var previewDirPath by mutableStateOf<String?>(null)
    private var previewVersion by mutableStateOf(0)
    private var activeGenerationSession by mutableStateOf<GenerationSession?>(null)
    private var previewSelections by mutableStateOf(PreviewSelections.default(PreviewChoice.Full))
    private var previewDesktopBackground by mutableStateOf(PreviewDesktopBackground.DarkGray)
    private var previewIconSizeDp by mutableStateOf(DEFAULT_PREVIEW_ICON_SIZE_DP)
    private var previewChoiceMode by mutableStateOf<PreviewMode?>(null)
    private var isGptPreviewLoading by mutableStateOf(false)
    private var isGeneratingGptCandidate by mutableStateOf(false)
    private var isGeneratingRmbgCandidate by mutableStateOf(false)
    private var isRefreshingArtPlusIcons by mutableStateOf(false)
    private var isPreviewAssetsRefreshing by mutableStateOf(false)
    private var isPreviewOutputRefreshing by mutableStateOf(false)
    private var lastRmbgCandidateError by mutableStateOf<String?>(null)
    private var rmbgCandidatePackageName by mutableStateOf<String?>(null)
    private var rmbgCandidateMode by mutableStateOf<PreviewMode?>(null)
    private var rmbgCandidateStatusText by mutableStateOf("")
    private var rmbgCandidateFailurePackageName by mutableStateOf<String?>(null)
    private var rmbgCandidateFailureMode by mutableStateOf<PreviewMode?>(null)
    private var skipNextHomeReturnAnimation by mutableStateOf(false)
    private var pendingCustomImageMode by mutableStateOf<PreviewMode?>(null)
    private var pendingCustomImageKind by mutableStateOf<CustomImageKind?>(null)
    private var isInstallingRmbgComponent by mutableStateOf(false)
    private var rmbgInstallStage by mutableStateOf("")
    private var rmbgInstallProgress by mutableStateOf<Float?>(null)
    private var rmbgComponentUrl by mutableStateOf("")
    private var rmbgComponentSaveStatus by mutableStateOf("")
    private var lastRmbgInferenceReport by mutableStateOf<RmbgInferenceReport?>(null)
    private var choicePopupRequest by mutableStateOf<ChoicePopupRequest?>(null)
    private var choicePopupVisible by mutableStateOf(false)
    private var nextChoicePopupId = 0L
    private var previewOutputJob: Job? = null
    private var previewOutputRevision = 0
    private var generatedPreviewRestoreRevision = 0
    private var debugHttpServer: DebugHttpServer? = null
    private var rmbgRuntime: DynamicRmbgRuntime? = null
    private var rmbgComponentStatus by mutableStateOf("")

    private data class ChoicePopupRequest(
        val id: Long,
        val anchorBounds: Rect?,
        val items: List<ChoicePopupItem>,
    )

    private data class ChoicePopupItem(
        val label: String,
        val summary: String,
        val selected: Boolean,
        val onSelected: () -> Unit,
    )

    private data class BatchApplyProgress(
        val title: String,
        val completed: Int,
        val total: Int,
        val currentLabel: String,
        val failures: Int,
    )

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            refreshPermissionState()
            loadApps()
        }

    private val chooseTreeLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri == null) {
                statusText = "未选择输出目录"
                return@registerForActivityResult
            }
            outputTreeUri = uri
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }
            statusText = "已选择输出目录"
            saveUiState()
        }

    private val chooseRmbgComponentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                statusText = "未选择 RMBG 组件"
                return@registerForActivityResult
            }
            installRmbgComponent(uri)
        }

    private val chooseCustomImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            val mode = pendingCustomImageMode
            val kind = pendingCustomImageKind
            pendingCustomImageMode = null
            pendingCustomImageKind = null
            if (uri == null) {
                statusText = "未选择自定义图片"
                return@registerForActivityResult
            }
            if (mode == null || kind == null) {
                statusText = "自定义槽位已失效"
                return@registerForActivityResult
            }
            importCustomPreviewImage(mode, kind, uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "ArtPlus Mobile"
        loadGptSettings()
        loadLocalSeparationSettings()
        loadImageSettings()
        loadLiquidGlassSettings()
        loadRmbgSettings()
        loadGeneratedPackageCache()
        loadUiState()
        startDebugHttpServerIfNeeded()
        refreshPermissionState()

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

        requestDeclaredPermissions()
        if (!isDebugGenerateIntent(intent)) {
            requestSpecialPermissionsOnce()
        }
        loadApps()
        handleDebugGenerateIntent(intent)
    }

    override fun onDestroy() {
        previewOutputJob?.cancel()
        previewWorkerScope.cancel()
        previewWorkerDispatcher.close()
        debugHttpServer?.stop()
        debugHttpServer = null
        runCatching { rmbgRuntime?.close() }
        rmbgRuntime = null
        super.onDestroy()
    }

    override fun onPause() {
        saveUiState()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        val previousPackageListPermission = packageListPermissionGranted
        val previousUsageAccess = usageAccessGranted
        refreshPermissionState()
        if (
            didRequestAppLoad &&
            (apps.isEmpty() ||
                previousPackageListPermission != packageListPermissionGranted ||
                previousUsageAccess != usageAccessGranted)
        ) {
            loadApps()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDebugGenerateIntent(intent)
    }

    private fun openChoicePopup(anchorBounds: Rect?, items: List<ChoicePopupItem>): Long {
        nextChoicePopupId += 1L
        choicePopupVisible = false
        choicePopupRequest = ChoicePopupRequest(
            id = nextChoicePopupId,
            anchorBounds = anchorBounds,
            items = items,
        )
        return nextChoicePopupId
    }

    private fun closeChoicePopup() {
        if (choicePopupRequest != null) {
            choicePopupVisible = false
        }
    }

    private fun startUiFriendlyThread(name: String, block: () -> Unit) {
        Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            block()
        }, name).apply {
            priority = Thread.MIN_PRIORITY
            start()
        }
    }

    @Composable
    private fun ArtPlusScreen() {
        val pageBackground = if (isSystemInDarkTheme()) {
            Color.Black
        } else {
            Color(0xFFF7F7F7)
        }
        val selectedApp by remember {
            derivedStateOf { apps.firstOrNull { it.packageName == selectedPackageName } }
        }
        val scopedApps by remember {
            derivedStateOf { apps.filter { it.launchable } }
        }
        val generatedCount by remember {
            derivedStateOf { scopedApps.count { it.packageName in generatedPackageNames } }
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
        val launcherCount by remember {
            derivedStateOf { apps.count { it.launchable } }
        }
        val density = LocalDensity.current
        val edgeBackWidthPx = with(density) { BACK_GESTURE_EDGE_WIDTH_DP.dp.toPx() }
        val commitBackDistancePx = with(density) { BACK_GESTURE_COMMIT_DISTANCE_DP.dp.toPx() }
        var systemBackProgress by remember { mutableStateOf(0f) }
        var dragBackProgress by remember { mutableStateOf(0f) }
        val completingBackProgress = remember { Animatable(0f) }
        val cancellingBackProgress = remember { Animatable(0f) }
        val childEnterProgress = remember { Animatable(1f) }
        var isCompletingBackGesture by remember { mutableStateOf(false) }
        val screenScope = rememberCoroutineScope()
        val backProgress = maxOf(
            systemBackProgress,
            dragBackProgress,
            completingBackProgress.value,
            cancellingBackProgress.value,
        )
        val activeChoicePopup = choicePopupRequest

        LaunchedEffect(activeChoicePopup?.id) {
            if (activeChoicePopup != null) {
                delay(16)
                if (choicePopupRequest?.id == activeChoicePopup.id) {
                    choicePopupVisible = true
                }
            }
        }

        LaunchedEffect(activeChoicePopup?.id, choicePopupVisible) {
            if (activeChoicePopup != null && !choicePopupVisible) {
                delay(180)
                if (choicePopupRequest?.id == activeChoicePopup.id && !choicePopupVisible) {
                    choicePopupRequest = null
                }
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
                dragBackProgress = 0f
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
                    dragBackProgress = 0f
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
                    dragBackProgress = 0f
                    completingBackProgress.snapTo(0f)
                    cancellingBackProgress.snapTo(0f)
                    skipNextHomeReturnAnimation = false
                }
            } else {
                systemBackProgress = 0f
                dragBackProgress = 0f
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
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                HomePage(
                    pageBackground = pageBackground,
                    selectedApp = selectedApp,
                    launcherCount = launcherCount,
                    generatedCount = generatedCount,
                )
            }

            val overlayPage = currentPage.takeIf { it != AppPage.Home }
            if (overlayPage != null) {
                val isActivePage = !isCompletingBackGesture
                val isCompletingBack = skipNextHomeReturnAnimation || isCompletingBackGesture
                val reveal = if (isCompletingBack || backProgress > 0f) {
                    backProgress.coerceIn(0f, 1f)
                } else {
                    0f
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val enteringOffset = size.width * childEnterProgress.value
                            val leavingOffset = size.width * reveal * BACK_GESTURE_PAGE_TRANSLATION_RATIO
                            translationX = enteringOffset + leavingOffset
                        }
                        .pointerInput(overlayPage, currentPage, isBusy, edgeBackWidthPx, commitBackDistancePx) {
                            if (!isActivePage) {
                                return@pointerInput
                            }
                            var activeEdge = 0
                            var dragDistance = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    activeEdge = when {
                                        isBusy -> 0
                                        offset.x <= edgeBackWidthPx -> 1
                                        offset.x >= size.width - edgeBackWidthPx -> -1
                                        else -> 0
                                    }
                                    dragDistance = 0f
                                    dragBackProgress = 0f
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    if (activeEdge == 0) {
                                        return@detectHorizontalDragGestures
                                    }
                                    dragDistance = (dragDistance + dragAmount * activeEdge).coerceAtLeast(0f)
                                    dragBackProgress =
                                        (dragDistance / (size.width * BACK_GESTURE_PROGRESS_DISTANCE_RATIO))
                                            .coerceIn(0f, 1f)
                                },
                                onDragCancel = {
                                    activeEdge = 0
                                    dragDistance = 0f
                                    dragBackProgress = 0f
                                },
                                onDragEnd = {
                                    if (
                                        activeEdge != 0 &&
                                        (dragBackProgress >= BACK_GESTURE_COMMIT_PROGRESS ||
                                            dragDistance >= commitBackDistancePx)
                                    ) {
                                        completeBackFrom(dragBackProgress)
                                    }
                                    activeEdge = 0
                                    dragDistance = 0f
                                },
                            )
                        },
                ) {
                    when (overlayPage) {
                        AppPage.Settings -> SettingsPage(
                            pageBackground = pageBackground,
                            launcherCount = launcherCount,
                            totalCount = apps.size,
                            generatedCount = generatedCount,
                        )

                        AppPage.AppPicker -> AppPickerPage(
                            pageBackground = pageBackground,
                            filteredApps = filteredApps,
                            scopeCount = scopeCount,
                            generatedCount = generatedCount,
                            ungeneratedCount = ungeneratedCount,
                        )

                        AppPage.Home -> Unit
                    }
                }
            }

            if (activeChoicePopup != null) {
                ChoicePopupOverlay(
                    request = activeChoicePopup,
                    visible = choicePopupVisible,
                    pageBackground = pageBackground,
                    onDismiss = { closeChoicePopup() },
                )
            }

            batchApplyProgress?.let { progress ->
                BatchApplyProgressDialog(progress)
            }
        }
    }

    @Composable
    private fun HomePage(pageBackground: Color, selectedApp: AppEntry?, launcherCount: Int, generatedCount: Int) {
        val scrollBehavior = MiuixScrollBehavior()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = "ArtPlus",
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 18.dp)
                                .size(46.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = !isBusy && !isRefreshingArtPlusIcons) { refreshArtPlusIcons() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                imageVector = Lucide.RefreshCw,
                                contentDescription = null,
                                modifier = Modifier.size(21.dp),
                                colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurface),
                            )
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 18.dp)
                                .size(46.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = !isBusy) { currentPage = AppPage.Settings },
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                imageVector = Lucide.SlidersHorizontal,
                                contentDescription = null,
                                modifier = Modifier.size(21.dp),
                                colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurface),
                            )
                        }
                    },
                )
            },
            popupHost = {},
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(pageBackground)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(innerPadding)
                    .imePadding()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (!packageListPermissionGranted || !usageAccessGranted) {
                            PermissionCard()
                        }
                        StatusCard(
                            selectedApp = selectedApp,
                            launcherCount = launcherCount,
                            totalCount = apps.size,
                            generatedCount = generatedCount,
                        )
                        GenerationCard(selectedApp)
                    }
                }
            }
        }
    }

    @Composable
    private fun SettingsPage(pageBackground: Color, launcherCount: Int, totalCount: Int, generatedCount: Int) {
        val scrollBehavior = MiuixScrollBehavior()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = "设置",
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 18.dp)
                                .size(46.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = !isBusy) { currentPage = AppPage.Home },
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                imageVector = Lucide.ChevronLeft,
                                contentDescription = null,
                                modifier = Modifier.size(21.dp),
                                colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurface),
                            )
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 18.dp)
                                .size(46.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = !isBusy) { saveSettingsPage() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                imageVector = Lucide.Save,
                                contentDescription = null,
                                modifier = Modifier.size(21.dp),
                                colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurface),
                            )
                        }
                    },
                )
            },
            popupHost = {},
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(pageBackground)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(innerPadding)
                    .imePadding()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        InputSettingsCard(
                            launcherCount = launcherCount,
                            totalCount = totalCount,
                            generatedCount = generatedCount,
                        )
                        OutputCard()
                        GptSettingsCard()
                        RmbgComponentCard()
                    }
                }
            }
        }
    }

    @Composable
    private fun AppPickerPage(
        pageBackground: Color,
        filteredApps: List<AppEntry>,
        scopeCount: Int,
        generatedCount: Int,
        ungeneratedCount: Int,
    ) {
        val scrollBehavior = MiuixScrollBehavior()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = "选择 APK",
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 18.dp)
                                .size(46.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = !isBusy) { currentPage = AppPage.Home },
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                imageVector = Lucide.ChevronLeft,
                                contentDescription = null,
                                modifier = Modifier.size(21.dp),
                                colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurface),
                            )
                        }
                    },
                )
            },
            popupHost = {},
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(pageBackground)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(innerPadding)
                    .imePadding()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
            ) {
                item {
                    Column {
                        AppPickerControlsCard(
                            filteredCount = filteredApps.size,
                            totalCount = scopeCount,
                            generatedCount = generatedCount,
                            ungeneratedCount = ungeneratedCount,
                            filteredApps = filteredApps,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                if (filteredApps.isEmpty()) {
                    item {
                        EmptyAppListCard()
                    }
                } else {
                    items(
                        items = filteredApps,
                        key = { it.packageName },
                        contentType = { "app" },
                    ) { entry ->
                        AppRow(
                            entry = entry,
                            selected = entry.packageName == selectedPackageName,
                            multiSelected = entry.packageName in multiSelectedPackageNames,
                            generated = entry.packageName in generatedPackageNames,
                            onClick = {
                                selectAppAndRestoreGeneratedPreview(entry)
                                currentPage = AppPage.Home
                            },
                            onToggleMultiSelect = { toggleMultiSelectedPackage(entry.packageName) },
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun PermissionCard() {
        SectionCard(title = "权限", summary = "启动时会自动请求普通权限；特殊权限需要进入系统设置确认") {
            SettingLine(
                title = "应用列表",
                summary = if (packageListPermissionGranted) "已声明并可读取已安装应用" else "需要允许读取应用列表",
                value = if (packageListPermissionGranted) "已允许" else "待授权",
            )
            Spacer(modifier = Modifier.height(10.dp))
            SettingLine(
                title = "使用情况访问",
                summary = if (usageAccessGranted) "已允许任务/使用情况访问" else "Android 只能在系统设置中授权",
                value = if (usageAccessGranted) "已允许" else "待授权",
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    text = "应用权限",
                    onClick = { openAppPermissionSettings() },
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = "使用情况访问",
                    onClick = { openUsageAccessSettings() },
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    @Composable
    private fun BatchApplyProgressDialog(progress: BatchApplyProgress) {
        val fraction = if (progress.total <= 0) {
            0f
        } else {
            (progress.completed.toFloat() / progress.total.toFloat()).coerceIn(0f, 1f)
        }
        Dialog(onDismissRequest = {}) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(18.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = progress.title,
                        style = MiuixTheme.textStyles.title4,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = progress.currentLabel,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MiuixTheme.colorScheme.surfaceContainerHigh),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction)
                                .clip(RoundedCornerShape(999.dp))
                                .background(MiuixTheme.colorScheme.primaryVariant),
                        )
                    }
                    Text(
                        text = buildString {
                            append("${progress.completed}/${progress.total}")
                            if (progress.failures > 0) {
                                append(" · 失败 ${progress.failures}")
                            }
                        },
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

    @Composable
    private fun GeneratedPreviewSection() {
        val dirPath = previewDirPath ?: return
        val packageName = previewPackageName ?: return
        var assets by remember(dirPath, previewVersion) {
            mutableStateOf<PreviewAssets?>(null)
        }

        LaunchedEffect(dirPath, previewVersion) {
            assets = withContext(Dispatchers.IO) {
                loadPreviewAssets(File(dirPath))
            }
        }
        val session = activeGenerationSession?.takeIf {
            it.packageName == packageName && it.outDir.absolutePath == dirPath
        }
        var liveAssets by remember(session) { mutableStateOf<PreviewAssets?>(null) }
        var liveAssetsLoading by remember(session) { mutableStateOf(false) }

        LaunchedEffect(
            session,
            previewSelections,
            foregroundSubjectPercent,
            foregroundShadowLevel,
            edgePolishPercent,
            rmbgAlphaStrengthPercent,
            rmbgEdgeFeatherPercent,
            rmbgEdgeAdjustPercent,
            rmbgWeakAlphaKeepPercent,
            liquidGlassEnabled,
            liquidGlassRadius,
            liquidGlassOuterWidth,
            liquidGlassTopAlpha,
            liquidGlassBottomAlpha,
            liquidGlassBackgroundMistAlpha,
            liquidGlassBottomDarkAlpha,
            liquidGlassSubjectScalePercent,
            liquidGlassSubjectOutlineWidth,
            liquidGlassSubjectInnerOutlineWidth,
            liquidGlassSubjectShadowAlpha,
            liquidGlassSubjectOpacityPercent,
            nightSubjectLightBackgroundEnabled,
            previewVersion,
        ) {
            if (session == null) {
                liveAssets = null
                liveAssetsLoading = false
                return@LaunchedEffect
            }
            liveAssetsLoading = true
            isPreviewAssetsRefreshing = true
            try {
                delay(PREVIEW_LIVE_ASSET_DEBOUNCE_MS)
                liveAssets = withContext(previewWorkerDispatcher) {
                    previewAssetsForSelections(session, previewSelections).preparedForDraw()
                }
            } catch (_: CancellationException) {
                throw CancellationException()
            } catch (_: Throwable) {
                liveAssets = null
            } finally {
                liveAssetsLoading = false
                isPreviewAssetsRefreshing = false
            }
        }
        val displayAssets = liveAssets ?: assets
        val previewLoading = isGptPreviewLoading || liveAssetsLoading || isPreviewOutputRefreshing

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "预览",
            style = MiuixTheme.textStyles.title4,
            color = MiuixTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "预览图可点击选择不同抠图规则",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = packageName,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(10.dp))
        PreviewDisplaySettings()
        Spacer(modifier = Modifier.height(10.dp))
        if (displayAssets == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AiIconLoadingPreview(modifier = Modifier.size(42.dp), overlay = true)
                Text(
                    text = "加载预览中",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            return
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PreviewTile(
                label = "正常亮色",
                assets = displayAssets,
                mode = PreviewMode.NormalLight,
                desktopBackground = previewDesktopBackground,
                iconSizeDp = previewIconSizeDp,
                loading = previewLoading,
                choiceEnabled = session != null,
                onClick = { previewChoiceMode = PreviewMode.NormalLight },
                modifier = Modifier.weight(1f),
            )
            PreviewTile(
                label = "正常暗色",
                assets = displayAssets,
                mode = PreviewMode.NormalDark,
                desktopBackground = previewDesktopBackground,
                iconSizeDp = previewIconSizeDp,
                loading = previewLoading,
                choiceEnabled = session != null,
                onClick = { previewChoiceMode = PreviewMode.NormalDark },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PreviewTile(
                label = "单色亮色",
                assets = displayAssets,
                mode = PreviewMode.MonochromeLight,
                desktopBackground = previewDesktopBackground,
                iconSizeDp = previewIconSizeDp,
                loading = previewLoading,
                choiceEnabled = session != null,
                onClick = { previewChoiceMode = PreviewMode.MonochromeLight },
                modifier = Modifier.weight(1f),
            )
            PreviewTile(
                label = "单色暗色",
                assets = displayAssets,
                mode = PreviewMode.MonochromeDark,
                desktopBackground = previewDesktopBackground,
                iconSizeDp = previewIconSizeDp,
                loading = previewLoading,
                choiceEnabled = session != null,
                onClick = { previewChoiceMode = PreviewMode.MonochromeDark },
                modifier = Modifier.weight(1f),
            )
        }
        val chooserMode = previewChoiceMode
        if (chooserMode != null && session != null) {
            PreviewChoiceDialog(mode = chooserMode, session = session)
        }
    }

    @Composable
    private fun PreviewDisplaySettings() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "预览设置",
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "只影响这里的显示，不会改生成结果",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                Text(
                    text = "$previewIconSizeDp%",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            SteppedPercentSlider(
                value = previewIconSizeDp,
                min = MIN_PREVIEW_ICON_SIZE_DP,
                max = MAX_PREVIEW_ICON_SIZE_DP,
                step = 1,
                enabled = !isBusy,
                showDots = false,
                onValueChange = { updatePreviewIconSizeDp(it) },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PreviewDesktopBackground.entries.forEach { option ->
                    PreviewBackgroundOption(
                        option = option,
                        selected = option == previewDesktopBackground,
                        modifier = Modifier.weight(1f),
                        onClick = { updatePreviewDesktopBackground(option) },
                    )
                }
            }
        }
    }

    @Composable
    private fun PreviewBackgroundOption(
        option: PreviewDesktopBackground,
        selected: Boolean,
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

    @Composable
    private fun PreviewTile(
        label: String,
        assets: PreviewAssets?,
        mode: PreviewMode,
        desktopBackground: PreviewDesktopBackground,
        iconSizeDp: Int,
        loading: Boolean,
        choiceEnabled: Boolean,
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
                    ) {
                        GeneratedIconPreview(
                            assets = assets,
                            mode = mode,
                            modifier = Modifier.size(iconSizeDp.dp),
                        )
                    }
                } else {
                    DesktopIconPreview(
                        desktopBackground = desktopBackground,
                        iconSize = iconSizeDp.dp,
                    ) {
                        MissingIconPreview(
                            modifier = Modifier.size(iconSizeDp.dp),
                            mode = mode,
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
    private fun PreviewCornerSwitch(checked: Boolean, enabled: Boolean) {
        val trackColor by animateColorAsState(
            targetValue = when {
                checked && enabled -> MiuixTheme.colorScheme.primaryVariant
                checked -> MiuixTheme.colorScheme.primaryVariant.copy(alpha = 0.46f)
                enabled -> MiuixTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f)
                else -> MiuixTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.52f)
            },
            animationSpec = tween(durationMillis = 180),
            label = "PreviewCornerSwitchTrack",
        )
        val thumbColor by animateColorAsState(
            targetValue = if (enabled) {
                MiuixTheme.colorScheme.onPrimaryVariant
            } else {
                MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.54f)
            },
            animationSpec = tween(durationMillis = 180),
            label = "PreviewCornerSwitchThumb",
        )
        val thumbOffset by animateDpAsState(
            targetValue = if (checked) 14.dp else 0.dp,
            animationSpec = tween(durationMillis = 180),
            label = "PreviewCornerSwitchOffset",
        )
        Box(
            modifier = Modifier
                .width(34.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(trackColor)
                .padding(2.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(16.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(thumbColor),
            )
        }
    }

    @Composable
    private fun DesktopIconPreview(
        desktopBackground: PreviewDesktopBackground,
        iconSize: Dp,
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
    private fun PreviewDesktopBackgroundSurface(
        option: PreviewDesktopBackground,
        modifier: Modifier = Modifier,
    ) {
        val wallpaper = remember(option) {
            if (option == PreviewDesktopBackground.Wallpaper) {
                loadPreviewWallpaperBitmap()
            } else {
                null
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
            } else if (option == PreviewDesktopBackground.Wallpaper) {
                ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF25262E),
                                Color(0xFF4B5968),
                                Color(0xFFB7A68D),
                            ),
                            start = Offset.Zero,
                            end = Offset(w, h),
                        ),
                    )
                    drawCircle(
                        color = Color(0xFFEAE2D3).copy(alpha = 0.28f),
                        radius = maxOf(w, h) * 0.34f,
                        center = Offset(w * 0.22f, h * 0.18f),
                    )
                    drawCircle(
                        color = Color(0xFF6A8FBD).copy(alpha = 0.24f),
                        radius = maxOf(w, h) * 0.32f,
                        center = Offset(w * 0.82f, h * 0.76f),
                    )
                }
            }
        }
    }

    @Composable
    private fun GeneratedIconPreview(
        assets: PreviewAssets?,
        mode: PreviewMode,
        modifier: Modifier = Modifier.size(72.dp),
    ) {
        val iconShape = RoundedCornerShape(20.dp)
        val md3LightBackground = systemMaterialColor("system_accent1_100", Color(0xFFEADDFF))
        val md3LightForeground = systemMaterialColor("system_accent1_700", Color(0xFF21005D))
        val md3DarkBackground = systemMaterialColor("system_accent1_700", Color(0xFF4F378B))
        val md3DarkForeground = systemMaterialColor("system_accent1_100", Color(0xFFEADDFF))
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
    private fun MissingIconPreview(
        modifier: Modifier = Modifier.size(72.dp),
        mode: PreviewMode? = null,
        compact: Boolean = false,
    ) {
        val md3LightBackground = systemMaterialColor("system_accent1_100", Color(0xFFEADDFF))
        val md3DarkBackground = systemMaterialColor("system_accent1_700", Color(0xFF4F378B))
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
        val outerRadius = if (compact) 16.dp else 20.dp
        val innerRadius = if (compact) 10.dp else 14.dp
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
    private fun AiIconLoadingPreview(
        modifier: Modifier = Modifier.size(72.dp),
        overlay: Boolean = false,
    ) {
        val transition = rememberInfiniteTransition(label = "AiIconLoading")
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "AiIconLoadingPhase",
        )
        val pulse by transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "AiIconLoadingPulse",
        )
        val shape = RoundedCornerShape(20.dp)
        Box(
            modifier = modifier
                .clip(shape)
                .background(if (overlay) Color.Transparent else Color(0xFF14131A)),
            contentAlignment = Alignment.Center,
        ) {
            ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF15131D),
                            Color(0xFF1D2136),
                            Color(0xFF171522),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(w, h),
                    ),
                    alpha = if (overlay) 0.22f else 1f,
                )
                val rows = 9
                val cols = 11
                for (row in 0 until rows) {
                    for (col in 0 until cols) {
                        val fx = (col + 0.5f) / cols
                        val fy = (row + 0.5f) / rows
                        val wave = sin((fx * 5.6f + phase * 6.28318f).toDouble()).toFloat()
                        val ribbon = 1f - (abs(fy - (0.52f + wave * 0.18f)) / 0.34f).coerceIn(0f, 1f)
                        val shimmer = 0.55f + 0.45f * sin((phase * 6.28318f + row * 0.74f + col * 0.39f).toDouble()).toFloat()
                        val alphaBase = (0.12f + ribbon * 0.46f + pulse * 0.12f + shimmer * 0.08f).coerceIn(0.12f, 0.72f)
                        val alpha = if (overlay) alphaBase * 0.58f else alphaBase
                        val radius = w * (0.012f + ribbon * 0.012f)
                        val color = when ((row + col) % 5) {
                            0 -> Color(0xFF28F7D2)
                            1 -> Color(0xFF4C83FF)
                            2 -> Color(0xFFE044FF)
                            3 -> Color(0xFFFFC857)
                            else -> Color(0xFFFF4D9D)
                        }
                        val offsetX = sin((phase * 6.28318f + row).toDouble()).toFloat() * w * 0.018f
                        val offsetY = cos((phase * 6.28318f + col).toDouble()).toFloat() * h * 0.014f
                        drawCircle(
                            color = color,
                            radius = radius,
                            center = Offset(w * fx + offsetX, h * fy + offsetY),
                            alpha = alpha,
                        )
                    }
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.18f + pulse * 0.12f), Color.Transparent),
                        center = Offset(w * (0.48f + 0.12f * sin((phase * 6.28318f).toDouble()).toFloat()), h * 0.5f),
                        radius = w * 0.54f,
                    ),
                    radius = w * 0.54f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    alpha = if (overlay) 0.16f else 0.35f,
                )
            }
        }
    }

    @Composable
    private fun PreviewNightFillBackgroundRow() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MiuixTheme.colorScheme.secondaryContainer.copy(alpha = 0.58f))
                .clickable(enabled = !isBusy) {
                    updateNightSubjectLightBackgroundEnabled(!nightSubjectLightBackgroundEnabled)
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "填充背景色",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "将暗色背景颜色填补到主体暗部",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            PreviewCornerSwitch(
                checked = nightSubjectLightBackgroundEnabled,
                enabled = !isBusy,
            )
        }
    }

    @Composable
    private fun PreviewChoiceDialog(mode: PreviewMode, session: GenerationSession) {
        val defaultChoices = listOf(
            PreviewChoice.Original,
            PreviewChoice.ComposedBackground,
            PreviewChoice.Rmbg,
            PreviewChoice.Gpt,
        )
        val moreChoices = listOf(
            PreviewChoice.TextSafe,
            PreviewChoice.Full,
            PreviewChoice.ComponentSubject,
            PreviewChoice.ComponentBackground,
            PreviewChoice.TwoLayer,
            PreviewChoice.CustomForeground,
            PreviewChoice.CustomBackground,
        )
        val selectedMoreRule = previewSelections.choiceFor(mode).let { choice ->
            when {
                choice == PreviewChoice.Plate -> PreviewChoice.Full
                choice in moreChoices -> choice
                else -> null
            }
        }
        var showMoreRules by remember(mode) { mutableStateOf(false) }
        Dialog(onDismissRequest = { previewChoiceMode = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f),
                insideMargin = PaddingValues(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = "${mode.label} 来源",
                        style = MiuixTheme.textStyles.title4,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "每个槽位单独选择",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingLine(
                        title = "主体占比",
                        summary = "复杂游戏图标建议 100%，范围 20% 到 150%",
                        value = "$foregroundSubjectPercent%",
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SteppedPercentSlider(
                        value = foregroundSubjectPercent,
                        min = MIN_FOREGROUND_SUBJECT_PERCENT,
                        max = MAX_FOREGROUND_SUBJECT_PERCENT,
                        step = 1,
                        enabled = !isBusy,
                        showDots = false,
                        onValueChange = { updateForegroundSubjectPercent(it) },
                    )
                    if (mode == PreviewMode.NormalDark) {
                        Spacer(modifier = Modifier.height(12.dp))
                        PreviewNightFillBackgroundRow()
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        defaultChoices.forEach { choice ->
                            PreviewChoiceRow(
                                mode = mode,
                                choice = choice,
                                session = session,
                            )
                        }
                        MoreRulesGroupRow(
                            selectedRule = selectedMoreRule,
                            expanded = showMoreRules,
                            onToggle = { showMoreRules = !showMoreRules },
                        )
                        if (showMoreRules) {
                            moreChoices.forEach { choice ->
                                if (shouldShowPreviewChoiceRow(choice, session)) {
                                    PreviewChoiceRow(
                                        mode = mode,
                                        choice = choice,
                                        session = session,
                                    )
                                }
                            }
                        }
                        if (!showMoreRules && selectedMoreRule != null) {
                            moreChoices
                                .firstOrNull { it == selectedMoreRule && shouldShowPreviewChoiceRow(it, session) }
                                ?.let { choice ->
                                    PreviewChoiceRow(
                                        mode = mode,
                                        choice = choice,
                                        session = session,
                                    )
                                }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        text = "关闭",
                        onClick = { previewChoiceMode = null },
                        enabled = !isGeneratingGptCandidate && !isGeneratingRmbgCandidate,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    private fun shouldShowPreviewChoiceRow(choice: PreviewChoice, session: GenerationSession): Boolean =
        when {
            choice.isCustom -> true
            choice == PreviewChoice.Full -> session.candidates[PreviewChoice.Full] != null ||
                session.candidates[PreviewChoice.Plate] != null
            else -> candidateForChoice(session, choice) != null
        }

    @Composable
    private fun MoreRulesGroupRow(
        selectedRule: PreviewChoice?,
        expanded: Boolean,
        onToggle: () -> Unit,
    ) {
        val selected = selectedRule != null
        val background = if (selected) {
            MiuixTheme.colorScheme.primaryVariant
        } else {
            MiuixTheme.colorScheme.surfaceContainerHigh
        }
        val titleColor = if (selected) {
            MiuixTheme.colorScheme.onPrimaryVariant
        } else {
            MiuixTheme.colorScheme.onSurface
        }
        val summaryColor = if (selected) {
            MiuixTheme.colorScheme.onPrimaryVariant
        } else {
            MiuixTheme.colorScheme.onSurfaceVariantSummary
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(background)
                .clickable(
                    enabled = !isBusy && !isGeneratingGptCandidate && !isGeneratingRmbgCandidate,
                    onClick = onToggle,
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "更多规则",
                    style = MiuixTheme.textStyles.body1,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = selectedRule?.let { "当前使用: ${it.label}" }
                        ?: "字标保全 / 清理 / 底座 / 二层 / 自定义",
                    style = MiuixTheme.textStyles.footnote1,
                    color = summaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            MetricPill(label = if (expanded) "收起" else "展开")
        }
    }

    @Composable
    private fun PreviewChoiceRow(mode: PreviewMode, choice: PreviewChoice, session: GenerationSession) {
        val currentChoice = previewSelections.choiceFor(mode)
        val effectiveChoice = effectiveChoiceForPreviewRow(mode, choice, session)
        val selected = currentChoice == effectiveChoice ||
            (choice == PreviewChoice.ComposedBackground && currentChoice.isComposedBackgroundCombination)
        val customKind = choice.customKind
        val candidate = if (customKind == null) {
            candidateForChoice(session, effectiveChoice)
        } else {
            customCandidateForPreview(mode, customKind, session)
        }
        val gptMissing = effectiveChoice == PreviewChoice.Gpt && candidate == null
        val rmbgMissing = effectiveChoice == PreviewChoice.Rmbg && candidate == null
        val customMissing = customKind != null && candidate == null
        val rmbgRunning = choice == PreviewChoice.Rmbg &&
            isGeneratingRmbgCandidate &&
            rmbgCandidatePackageName == session.packageName &&
            (rmbgCandidateMode == null || rmbgCandidateMode == mode)
        val rmbgFailure = if (
            choice == PreviewChoice.Rmbg &&
            rmbgCandidateFailurePackageName == session.packageName &&
            (rmbgCandidateFailureMode == null || rmbgCandidateFailureMode == mode)
        ) {
            lastRmbgCandidateError
        } else {
            null
        }
        val canGenerateGpt = gptBaseUrl.trim().isNotEmpty() && gptApiKey.trim().isNotEmpty()
        val canGenerateRmbg = rmbgMissing && findRmbgComponent() != null
        val missingLocalCandidate = choice != PreviewChoice.Gpt &&
            customKind == null &&
            candidate == null &&
            !canGenerateRmbg
        val canImportCustom = customMissing
        val missingCandidate = missingLocalCandidate && !canImportCustom
        val enabled = !isBusy && !isGeneratingGptCandidate && !isGeneratingRmbgCandidate && !missingCandidate
        val background = if (selected) {
            MiuixTheme.colorScheme.primaryVariant
        } else {
            MiuixTheme.colorScheme.surfaceContainerHigh
        }
        val titleColor = if (selected) {
            MiuixTheme.colorScheme.onPrimaryVariant
        } else {
            MiuixTheme.colorScheme.onSurface
        }
        val summaryColor = if (selected) {
            MiuixTheme.colorScheme.onPrimaryVariant
        } else {
            MiuixTheme.colorScheme.onSurfaceVariantSummary
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(background)
                .clickable(enabled = enabled) {
                    if (gptMissing) {
                        generateGptCandidateForMode(mode)
                    } else if (rmbgMissing) {
                        generateRmbgCandidateForMode(mode)
                    } else if (customKind != null) {
                        chooseCustomImageForMode(mode, customKind)
                    } else {
                        applyPreviewChoice(mode, effectiveChoice)
                    }
                }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MiuixTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                if (choice == PreviewChoice.Gpt && isGeneratingGptCandidate) {
                    AiIconLoadingPreview(modifier = Modifier.fillMaxSize())
                } else if (rmbgRunning) {
                    AiIconLoadingPreview(modifier = Modifier.fillMaxSize(), overlay = true)
                } else if (candidate != null) {
                    CandidateIconPreview(candidate, mode)
                } else {
                    MissingIconPreview(
                        modifier = Modifier.fillMaxSize(),
                        mode = mode,
                        compact = true,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = choice.label,
                    style = MiuixTheme.textStyles.body1,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = when {
                        selected -> "当前使用"
                        missingCandidate && choice == PreviewChoice.TwoLayer -> "当前图标不符合二层结构"
                        missingCandidate && choice == PreviewChoice.Rmbg -> "未安装组件"
                        rmbgRunning -> rmbgCandidateStatusText.ifBlank { "RMBG运行中" }
                        rmbgMissing && rmbgFailure != null -> rmbgFailure
                        rmbgMissing -> "点击运行"
                        customMissing -> "选择 PNG/SVG"
                        customKind != null -> "已导入"
                        missingCandidate -> "不可用"
                        choice == PreviewChoice.Gpt && isGeneratingGptCandidate -> "正在生成"
                        gptMissing && !canGenerateGpt -> "先填 GPT 设置"
                        gptMissing -> "点击生成"
                        effectiveChoice.isComposedBackgroundCombination -> effectiveChoice.summary
                        else -> choice.summary
                    },
                    style = MiuixTheme.textStyles.footnote1,
                    color = summaryColor,
                    maxLines = if (choice == PreviewChoice.Rmbg && rmbgFailure != null) 4 else 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            PreviewChoiceActions(
                showApplyAll = customKind == null,
                applyEnabled = enabled && customKind == null,
                onApplyAll = { applyPreviewChoiceToAll(effectiveChoice) },
            )
    }
    }

    @Composable
    private fun PreviewChoiceActions(
        showApplyAll: Boolean,
        applyEnabled: Boolean,
        onApplyAll: () -> Unit,
    ) {
        Box(
            modifier = Modifier.width(88.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (showApplyAll) {
                CompactActionButton(
                    text = "全部应用",
                    onClick = onApplyAll,
                    enabled = applyEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    height = 56.dp,
                )
            }
        }
    }

    @Composable
    private fun CandidateIconPreview(candidate: IconCandidate, mode: PreviewMode) {
        var assets by remember(
            candidate,
            mode,
            foregroundSubjectPercent,
            foregroundShadowLevel,
            edgePolishPercent,
            rmbgAlphaStrengthPercent,
            rmbgEdgeFeatherPercent,
            rmbgEdgeAdjustPercent,
            rmbgWeakAlphaKeepPercent,
            liquidGlassEnabled,
            liquidGlassRadius,
            liquidGlassOuterWidth,
            liquidGlassTopAlpha,
            liquidGlassBottomAlpha,
            liquidGlassBackgroundMistAlpha,
            liquidGlassBottomDarkAlpha,
            liquidGlassSubjectScalePercent,
            liquidGlassSubjectOutlineWidth,
            liquidGlassSubjectInnerOutlineWidth,
            liquidGlassSubjectShadowAlpha,
            liquidGlassSubjectOpacityPercent,
            nightSubjectLightBackgroundEnabled,
        ) {
            mutableStateOf<PreviewAssets?>(null)
        }
        LaunchedEffect(
            candidate,
            mode,
            foregroundSubjectPercent,
            foregroundShadowLevel,
            edgePolishPercent,
            rmbgAlphaStrengthPercent,
            rmbgEdgeFeatherPercent,
            rmbgEdgeAdjustPercent,
            rmbgWeakAlphaKeepPercent,
            liquidGlassEnabled,
            liquidGlassRadius,
            liquidGlassOuterWidth,
            liquidGlassTopAlpha,
            liquidGlassBottomAlpha,
            liquidGlassBackgroundMistAlpha,
            liquidGlassBottomDarkAlpha,
            liquidGlassSubjectScalePercent,
            liquidGlassSubjectOutlineWidth,
            liquidGlassSubjectInnerOutlineWidth,
            liquidGlassSubjectShadowAlpha,
            liquidGlassSubjectOpacityPercent,
            nightSubjectLightBackgroundEnabled,
        ) {
            assets = null
            try {
                assets = withContext(previewWorkerDispatcher) {
                    previewAssetsForCandidate(candidate, mode).preparedForDraw()
                }
            } catch (_: CancellationException) {
                throw CancellationException()
            } catch (_: Throwable) {
                assets = null
            }
        }
        val readyAssets = assets
        if (readyAssets == null) {
            AiIconLoadingPreview(modifier = Modifier.fillMaxSize(), overlay = true)
        } else {
            GeneratedIconPreview(readyAssets, mode)
        }
    }
    private fun systemMaterialColor(resourceName: String, fallback: Color): Color {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return fallback
        }
        val colorId = resources.getIdentifier(resourceName, "color", "android")
        if (colorId == 0) {
            return fallback
        }
        return runCatching { Color(getColor(colorId)) }.getOrDefault(fallback)
    }

    @Composable
    private fun StatusCard(
        selectedApp: AppEntry?,
        launcherCount: Int,
        totalCount: Int,
        generatedCount: Int,
    ) {
        val statusLabel = if (isBusy) "运行中" else "就绪"
        val enabled = !isBusy && apps.isNotEmpty()

        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(enabled = enabled) { currentPage = AppPage.AppPicker }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (selectedApp == null) {
                    BrandMark(size = 48.dp, text = "UX")
                } else {
                    AppIcon(selectedApp, 48.dp)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        StatusDot(active = isBusy)
                        Text(
                            text = statusLabel,
                            style = MiuixTheme.textStyles.title4,
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = selectedApp?.label ?: "选择一个应用开始生成",
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = selectedApp?.packageName ?: statusText,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "启动器 $launcherCount 个 / 全部 $totalCount 个 / 已生成 $generatedCount 个",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Image(
                    imageVector = Lucide.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantSummary),
                )
            }
        }
    }

    @Composable
    private fun EmptyAppListCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(16.dp),
        ) {
            Text(
                text = "没有可显示的应用",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "清空搜索词，或在系统设置中允许 ArtPlus 读取应用列表后刷新。",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                text = "刷新应用列表",
                onClick = { loadApps() },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    @Composable
    private fun LocalSeparationModeControl() {
        val modes = LocalSeparationMode.entries.filterNot { it == LocalSeparationMode.Plate }
        val selectedMode = if (localSeparationMode == LocalSeparationMode.Plate) {
            LocalSeparationMode.Full
        } else {
            localSeparationMode
        }
        SegmentedControl(
            labels = modes.map { it.label },
            selectedIndex = modes.indexOf(selectedMode).coerceAtLeast(0),
            scrollable = true,
            onSelected = { index ->
                updateLocalSeparationMode(modes[index])
            }
        )
    }

    @Composable
    private fun AdvancedSeparationSettings() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AdvancedCategoryTabs(
                selected = advancedSettingsCategory,
                onSelected = { category ->
                    advancedSettingsCategory = category
                    saveUiState()
                },
            )
            when (advancedSettingsCategory) {
                AdvancedSettingsCategory.LiquidGlass -> LiquidGlassAdvancedSettings()
                AdvancedSettingsCategory.Local -> LocalRuleAdvancedSettings()
                AdvancedSettingsCategory.Rmbg -> RmbgAdvancedSettings()
            }
        }
    }

    @Composable
    private fun AdvancedCategoryTabs(
        selected: AdvancedSettingsCategory,
        onSelected: (AdvancedSettingsCategory) -> Unit,
    ) {
        val categories = AdvancedSettingsCategory.entries
        val safeSelectedIndex = categories.indexOf(selected).coerceAtLeast(0)
        val density = LocalDensity.current
        var widthPx by remember(categories.size) { mutableStateOf(0) }
        val gap = 12.dp
        val gapPx = with(density) { gap.toPx() }
        val segmentWidthPx = if (widthPx == 0) {
            0f
        } else {
            ((widthPx.toFloat() - gapPx * (categories.size - 1)).coerceAtLeast(1f) / categories.size.toFloat())
        }
        val selectedOffsetPx by animateFloatAsState(
            targetValue = (segmentWidthPx + gapPx) * safeSelectedIndex,
            animationSpec = tween(durationMillis = 240),
            label = "AdvancedCategoryOffset",
        )
        val selectedWidth = with(density) { segmentWidthPx.toDp() }
        val selectedColor = if (isSystemInDarkTheme()) {
            Color(0xFF555555)
        } else {
            Color(0xFFF1F1F1)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .onGloballyPositioned { widthPx = it.size.width },
        ) {
            if (segmentWidthPx > 0f) {
                Box(
                    modifier = Modifier
                        .width(selectedWidth)
                        .fillMaxHeight()
                        .offset { IntOffset(selectedOffsetPx.roundToInt(), 0) }
                        .clip(RoundedCornerShape(18.dp))
                        .background(selectedColor),
                )
            }
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                categories.forEach { category ->
                    val isSelected = category == selected
                    val interactionSource = remember(category) { MutableInteractionSource() }
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MiuixTheme.colorScheme.onSurface
                        } else {
                            MiuixTheme.colorScheme.onSurfaceVariantSummary
                        },
                        animationSpec = tween(durationMillis = 180),
                        label = "AdvancedCategoryText",
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(
                                enabled = !isBusy && !isSelected,
                                interactionSource = interactionSource,
                                indication = null,
                            ) { onSelected(category) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = category.label,
                            style = MiuixTheme.textStyles.title4.copy(
                                fontWeight = FontWeight(700),
                                fontSize = 16.sp,
                            ),
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun LiquidGlassAdvancedSettings() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LiquidGlassToggleRow()
            LiquidGlassSectionTitle("玻璃层")
            NumberParameterControl(
                title = "玻璃圆角",
                summary = "控制玻璃遮罩圆角，背景与主体按同一轮廓裁剪",
                value = liquidGlassRadius,
                draftText = draftLiquidGlassRadiusText,
                min = MIN_LIQUID_GLASS_RADIUS,
                max = MAX_LIQUID_GLASS_RADIUS,
                onDraftChange = { draftLiquidGlassRadiusText = it },
                onSave = { updateLiquidGlassRadius(it) },
                icon = SettingsIconKind.Radius,
            )
            NumberParameterControl(
                title = "外框高度",
                summary = "控制玻璃外缘高光的厚度",
                value = liquidGlassOuterWidth,
                draftText = draftLiquidGlassOuterWidthText,
                min = MIN_LIQUID_GLASS_OUTER_WIDTH,
                max = MAX_LIQUID_GLASS_OUTER_WIDTH,
                step = 1,
                onDraftChange = { draftLiquidGlassOuterWidthText = it },
                onSave = { updateLiquidGlassOuterWidth(it) },
                icon = SettingsIconKind.Glass,
            )
            NumberParameterControl(
                title = "顶部强度",
                summary = "控制顶边贴边高光的亮度",
                value = liquidGlassTopAlpha,
                draftText = draftLiquidGlassTopAlphaText,
                min = MIN_LIQUID_GLASS_ALPHA,
                max = MAX_LIQUID_GLASS_ALPHA,
                step = 1,
                onDraftChange = { draftLiquidGlassTopAlphaText = it },
                onSave = { updateLiquidGlassTopAlpha(it) },
                icon = SettingsIconKind.Spark,
            )
            NumberParameterControl(
                title = "底边强度",
                summary = "控制底边贴边高光的亮度",
                value = liquidGlassBottomAlpha,
                draftText = draftLiquidGlassBottomAlphaText,
                min = MIN_LIQUID_GLASS_ALPHA,
                max = MAX_LIQUID_GLASS_ALPHA,
                step = 1,
                onDraftChange = { draftLiquidGlassBottomAlphaText = it },
                onSave = { updateLiquidGlassBottomAlpha(it) },
                icon = SettingsIconKind.Spark,
            )
            NumberParameterControl(
                title = "背景灰雾",
                summary = "给图标背景叠加均匀暗雾，降低整体亮度",
                value = liquidGlassBackgroundMistAlpha,
                draftText = draftLiquidGlassBackgroundMistAlphaText,
                min = MIN_LIQUID_GLASS_MIST_ALPHA,
                max = MAX_LIQUID_GLASS_MIST_ALPHA,
                step = 1,
                onDraftChange = { draftLiquidGlassBackgroundMistAlphaText = it },
                onSave = { updateLiquidGlassBackgroundMistAlpha(it) },
                icon = SettingsIconKind.Shadow,
            )
            NumberParameterControl(
                title = "底部灰雾",
                summary = "给底部叠加暗雾渐变，压住底边亮度",
                value = liquidGlassBottomDarkAlpha,
                draftText = draftLiquidGlassBottomDarkAlphaText,
                min = MIN_LIQUID_GLASS_BOTTOM_DARK_ALPHA,
                max = MAX_LIQUID_GLASS_BOTTOM_DARK_ALPHA,
                step = 1,
                onDraftChange = { draftLiquidGlassBottomDarkAlphaText = it },
                onSave = { updateLiquidGlassBottomDarkAlpha(it) },
                icon = SettingsIconKind.Shadow,
            )
            LiquidGlassSectionTitle("主体层")
            NumberParameterControl(
                title = "主体比例",
                summary = "调整主体在玻璃层中的缩放比例",
                value = liquidGlassSubjectScalePercent,
                draftText = draftLiquidGlassSubjectScaleText,
                min = MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT,
                max = MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT,
                step = 1,
                onDraftChange = { draftLiquidGlassSubjectScaleText = it },
                onSave = { updateLiquidGlassSubjectScalePercent(it) },
                icon = SettingsIconKind.Scale,
            )
            NumberParameterControl(
                title = "主体外框宽度",
                summary = "沿主体外侧透明边界添加高光描边",
                value = liquidGlassSubjectOutlineWidth,
                draftText = draftLiquidGlassSubjectOutlineWidthText,
                min = MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                max = MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                step = 1,
                onDraftChange = { draftLiquidGlassSubjectOutlineWidthText = it },
                onSave = { updateLiquidGlassSubjectOutlineWidth(it) },
                icon = SettingsIconKind.Spark,
            )
            NumberParameterControl(
                title = "主体内框宽度",
                summary = "沿主体内侧透明边界添加高光描边",
                value = liquidGlassSubjectInnerOutlineWidth,
                draftText = draftLiquidGlassSubjectInnerOutlineWidthText,
                min = MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                max = MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                step = 1,
                onDraftChange = { draftLiquidGlassSubjectInnerOutlineWidthText = it },
                onSave = { updateLiquidGlassSubjectInnerOutlineWidth(it) },
                icon = SettingsIconKind.Spark,
            )
            NumberParameterControl(
                title = "主体阴影",
                summary = "控制主体投影透明度，增强层次",
                value = liquidGlassSubjectShadowAlpha,
                draftText = draftLiquidGlassSubjectShadowAlphaText,
                min = MIN_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA,
                max = MAX_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA,
                step = 1,
                onDraftChange = { draftLiquidGlassSubjectShadowAlphaText = it },
                onSave = { updateLiquidGlassSubjectShadowAlpha(it) },
                icon = SettingsIconKind.Shadow,
            )
            NumberParameterControl(
                title = "主体透明度",
                summary = "控制主体层整体不透明度",
                value = liquidGlassSubjectOpacityPercent,
                draftText = draftLiquidGlassSubjectOpacityText,
                min = MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
                max = MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
                step = 1,
                onDraftChange = { draftLiquidGlassSubjectOpacityText = it },
                onSave = { updateLiquidGlassSubjectOpacityPercent(it) },
                icon = SettingsIconKind.Glass,
            )
        }
    }

    @Composable
    private fun LocalRuleAdvancedSettings() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "这些只影响“本地生成/自动规则”，对已精修 data 包不会重新抠图。",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            NumberParameterControl(
                title = "背景相似度",
                summary = "越高越容易把相近颜色当背景",
                value = backgroundSeparationPercent,
                draftText = draftBackgroundSeparationText,
                min = MIN_BACKGROUND_SEPARATION_PERCENT,
                max = MAX_BACKGROUND_SEPARATION_PERCENT,
                onDraftChange = { draftBackgroundSeparationText = it },
                onSave = { updateBackgroundSeparationPercent(it) },
                icon = SettingsIconKind.Cutout,
            )
            NumberParameterControl(
                title = "底板清理",
                summary = "越高越容易移除纯色底板",
                value = plateRemovalPercent,
                draftText = draftPlateRemovalText,
                min = MIN_PLATE_REMOVAL_PERCENT,
                max = MAX_PLATE_REMOVAL_PERCENT,
                onDraftChange = { draftPlateRemovalText = it },
                onSave = { updatePlateRemovalPercent(it) },
                icon = SettingsIconKind.Plate,
            )
            NumberParameterControl(
                title = "旧阴影清理",
                summary = "清掉原图里的长阴影，不是新增阴影",
                value = shadowRemovalPercent,
                draftText = draftShadowRemovalText,
                min = MIN_SHADOW_REMOVAL_PERCENT,
                max = MAX_SHADOW_REMOVAL_PERCENT,
                onDraftChange = { draftShadowRemovalText = it },
                onSave = { updateShadowRemovalPercent(it) },
                icon = SettingsIconKind.Eraser,
            )
            NumberParameterControl(
                title = "边缘修补",
                summary = "修补抠图毛刺和半透明边",
                value = edgePolishPercent,
                draftText = draftEdgePolishText,
                min = MIN_EDGE_POLISH_PERCENT,
                max = MAX_EDGE_POLISH_PERCENT,
                onDraftChange = { draftEdgePolishText = it },
                onSave = { updateEdgePolishPercent(it) },
                icon = SettingsIconKind.Spark,
            )
        }
    }

    @Composable
    private fun RmbgAdvancedSettings() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "这些只影响 RMBG 候选图，已精修主体不走这组参数。",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            NumberParameterControl(
                title = "Alpha 力度",
                summary = "100 不变，越高越实",
                value = rmbgAlphaStrengthPercent,
                draftText = draftRmbgAlphaStrengthText,
                min = MIN_RMBG_ALPHA_STRENGTH_PERCENT,
                max = MAX_RMBG_ALPHA_STRENGTH_PERCENT,
                onDraftChange = { draftRmbgAlphaStrengthText = it },
                onSave = { updateRmbgAlphaStrengthPercent(it) },
                icon = SettingsIconKind.Cutout,
            )
            NumberParameterControl(
                title = "边缘柔化",
                summary = "越高边缘越软",
                value = rmbgEdgeFeatherPercent,
                draftText = draftRmbgEdgeFeatherText,
                min = MIN_RMBG_EDGE_FEATHER_PERCENT,
                max = MAX_RMBG_EDGE_FEATHER_PERCENT,
                onDraftChange = { draftRmbgEdgeFeatherText = it },
                onSave = { updateRmbgEdgeFeatherPercent(it) },
                icon = SettingsIconKind.Cutout,
            )
            NumberParameterControl(
                title = "边缘扩缩",
                summary = "低收缩，高扩张",
                value = rmbgEdgeAdjustPercent,
                draftText = draftRmbgEdgeAdjustText,
                min = MIN_RMBG_EDGE_ADJUST_PERCENT,
                max = MAX_RMBG_EDGE_ADJUST_PERCENT,
                onDraftChange = { draftRmbgEdgeAdjustText = it },
                onSave = { updateRmbgEdgeAdjustPercent(it) },
                icon = SettingsIconKind.Scale,
            )
            NumberParameterControl(
                title = "弱透明保留",
                summary = "越高越保留半透明细节",
                value = rmbgWeakAlphaKeepPercent,
                draftText = draftRmbgWeakAlphaKeepText,
                min = MIN_RMBG_WEAK_ALPHA_KEEP_PERCENT,
                max = MAX_RMBG_WEAK_ALPHA_KEEP_PERCENT,
                onDraftChange = { draftRmbgWeakAlphaKeepText = it },
                onSave = { updateRmbgWeakAlphaKeepPercent(it) },
                icon = SettingsIconKind.Cutout,
            )
        }
    }

    @Composable
    private fun SteppedPercentSlider(
        value: Int,
        min: Int,
        max: Int,
        step: Int,
        enabled: Boolean,
        showDots: Boolean = true,
        onValueChange: (Int) -> Unit,
    ) {
        val density = LocalDensity.current
        val inactiveColor = MiuixTheme.colorScheme.surfaceContainerHigh
        val nodeColor = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.38f)
        val activeColor = MiuixTheme.colorScheme.primaryVariant
        val thumbColor = MiuixTheme.colorScheme.onPrimaryVariant
        val disabledAlpha = if (enabled) 1f else 0.48f
        val steps = ((max - min) / step).coerceAtLeast(1)
        val safeIndex = (((value.coerceIn(min, max) - min).toFloat() / step.toFloat()).roundToInt())
            .coerceIn(0, steps)
        val currentValue by rememberUpdatedState(value)
        val currentOnValueChange by rememberUpdatedState(onValueChange)
        var widthPx by remember { mutableStateOf(0) }
        val sideInsetPx = with(density) { 28.dp.toPx() }

        fun updateFromX(x: Float) {
            if (!enabled || widthPx <= 0) {
                return
            }
            val trackStart = sideInsetPx
            val trackEnd = (widthPx.toFloat() - sideInsetPx).coerceAtLeast(trackStart + 1f)
            val ratio = ((x.coerceIn(trackStart, trackEnd) - trackStart) / (trackEnd - trackStart))
                .coerceIn(0f, 1f)
            val nextIndex = (ratio * steps).roundToInt().coerceIn(0, steps)
            val nextValue = min + nextIndex * step
            if (nextValue != currentValue) {
                currentOnValueChange(nextValue)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MiuixTheme.colorScheme.secondaryContainer.copy(alpha = 0.58f))
                .onGloballyPositioned { widthPx = it.size.width }
                .pointerInput(enabled, widthPx, min, max, step) {
                    detectTapGestures { offset -> updateFromX(offset.x) }
                }
                .pointerInput(enabled, widthPx, min, max, step) {
                    detectDragGestures(
                        onDragStart = { offset -> updateFromX(offset.x) },
                        onDrag = { change, _ ->
                            updateFromX(change.position.x)
                            change.consume()
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                val trackStart = sideInsetPx.coerceAtMost(size.width / 2f)
                val trackEnd = (size.width - sideInsetPx).coerceAtLeast(trackStart + 1f)
                val centerY = size.height / 2f
                val trackWidth = trackEnd - trackStart
                val selectedX = trackStart + trackWidth * (safeIndex.toFloat() / steps.toFloat())
                val trackHeight = 34.dp.toPx()
                val dotRadius = 3.4.dp.toPx()
                val thumbRadius = 13.dp.toPx()

                drawLine(
                    color = inactiveColor.copy(alpha = disabledAlpha),
                    start = Offset(trackStart, centerY),
                    end = Offset(trackEnd, centerY),
                    strokeWidth = trackHeight,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = activeColor.copy(alpha = disabledAlpha),
                    start = Offset(trackStart, centerY),
                    end = Offset(selectedX, centerY),
                    strokeWidth = trackHeight,
                    cap = StrokeCap.Round,
                )
                if (showDots && steps <= 30) {
                    for (index in 0..steps) {
                        val x = trackStart + trackWidth * (index.toFloat() / steps.toFloat())
                        drawCircle(
                            color = if (index <= safeIndex) {
                                thumbColor.copy(alpha = disabledAlpha * 0.42f)
                            } else {
                                nodeColor
                            },
                            radius = dotRadius,
                            center = Offset(x, centerY),
                        )
                    }
                }
                drawCircle(
                    color = thumbColor.copy(alpha = disabledAlpha),
                    radius = thumbRadius,
                    center = Offset(selectedX, centerY),
                )
            }
        }
    }

    @Composable
    private fun NumberParameterControl(
        title: String,
        summary: String,
        value: Int,
        draftText: String,
        min: Int,
        max: Int,
        step: Int = 1,
        onDraftChange: (String) -> Unit,
        onSave: (Int) -> Unit,
        enabled: Boolean = true,
        icon: SettingsIconKind? = null,
    ) {
        val controlEnabled = enabled && !isBusy
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SettingsLineIcon(kind = icon ?: settingsIconForTitle(title))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = title,
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "$summary · 范围 $min-$max",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NumberInputBox(
                        value = draftText,
                        fallbackValue = value,
                        enabled = controlEnabled,
                        onValueChange = onDraftChange,
                        onDone = { submitted ->
                            submitted.toIntOrNull()
                                ?.coerceIn(min, max)
                                ?.let(onSave)
                        },
                    )
                }
            }
            SteppedPercentSlider(
                value = value,
                min = min,
                max = max,
                step = step,
                enabled = controlEnabled,
                onValueChange = onSave,
            )
        }
    }

    @Composable
    private fun DecimalParameterControl(
        title: String,
        summary: String,
        value: Float,
        draftText: String,
        min: Float,
        max: Float,
        onDraftChange: (String) -> Unit,
        onSave: (Float) -> Unit,
        enabled: Boolean = true,
        icon: SettingsIconKind? = null,
    ) {
        val controlEnabled = enabled && !isBusy
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsLineIcon(kind = icon ?: settingsIconForTitle(title))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "$summary · 范围 ${formatScale(min)}-${formatScale(max)}",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DecimalInputBox(
                    value = draftText,
                    fallbackValue = formatScale(value),
                    enabled = controlEnabled,
                    onValueChange = onDraftChange,
                    onDone = { submitted ->
                        submitted.toFloatOrNull()
                            ?.coerceIn(min, max)
                            ?.let(onSave)
                    },
                )
            }
        }
    }

    @Composable
    private fun DecimalInputBox(
        value: String,
        fallbackValue: String,
        enabled: Boolean,
        onValueChange: (String) -> Unit,
        onDone: (String) -> Unit,
    ) {
        val textColor = MiuixTheme.colorScheme.onSurface.toArgb()
        val cursorColor = MiuixTheme.colorScheme.primaryVariant.toArgb()
        val bringIntoViewRequester = remember { BringIntoViewRequester() }
        var bringIntoViewRequest by remember { mutableStateOf(0) }

        LaunchedEffect(bringIntoViewRequest) {
            if (bringIntoViewRequest > 0) {
                delay(260)
                bringIntoViewRequester.bringIntoView()
            }
        }

        Box(
            modifier = Modifier
                .width(78.dp)
                .height(46.dp)
                .bringIntoViewRequester(bringIntoViewRequester)
                .clip(RoundedCornerShape(14.dp))
                .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    EditText(context).apply {
                        background = null
                        gravity = Gravity.CENTER
                        includeFontPadding = false
                        isSingleLine = true
                        filters = arrayOf(InputFilter.LengthFilter(8))
                        inputType = InputType.TYPE_CLASS_NUMBER or
                            InputType.TYPE_NUMBER_FLAG_DECIMAL or
                            InputType.TYPE_NUMBER_FLAG_SIGNED
                        imeOptions = EditorInfo.IME_ACTION_DONE or
                            EditorInfo.IME_FLAG_NO_EXTRACT_UI or
                            EditorInfo.IME_FLAG_NO_FULLSCREEN
                        setSelectAllOnFocus(true)
                        setPadding(0, 0, 0, 0)
                        setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
                        setOnClickListener {
                            bringIntoViewRequest++
                            requestFocus()
                            post {
                                selectAll()
                                showKeyboardFor(this)
                            }
                        }
                        setOnFocusChangeListener { _, hasFocus ->
                            if (hasFocus) {
                                bringIntoViewRequest++
                                post {
                                    selectAll()
                                    showKeyboardFor(this)
                                }
                            } else if (text.isEmpty()) {
                                setText(fallbackValue)
                                onValueChange(fallbackValue)
                            }
                        }
                        setOnEditorActionListener { _, actionId, _ ->
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                onDone(text.toString())
                                clearFocus()
                                true
                            } else {
                                false
                            }
                        }
                    }
                },
                update = { editText ->
                    editText.isEnabled = enabled
                    editText.setTextColor(textColor)
                    editText.setHintTextColor(textColor)
                    editText.textCursorDrawable?.setTint(cursorColor)
                    editText.imeOptions = EditorInfo.IME_ACTION_DONE or
                        EditorInfo.IME_FLAG_NO_EXTRACT_UI or
                        EditorInfo.IME_FLAG_NO_FULLSCREEN
                    (editText.tag as? TextWatcher)?.let { editText.removeTextChangedListener(it) }
                    if (editText.text.toString() != value) {
                        editText.setText(value)
                        editText.setSelection(editText.text.length)
                    }
                    val watcher = object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            val raw = s?.toString().orEmpty()
                            val filtered = filterDecimalInput(raw)
                            if (filtered != raw) {
                                editText.setText(filtered)
                                editText.setSelection(editText.text.length)
                                return
                            }
                            onValueChange(filtered)
                        }
                        override fun afterTextChanged(s: Editable?) = Unit
                    }
                    editText.tag = watcher
                    editText.addTextChangedListener(watcher)
                }
            )
        }
    }

    @Composable
    private fun NumberInputBox(
        value: String,
        fallbackValue: Int,
        enabled: Boolean,
        onValueChange: (String) -> Unit,
        onDone: (String) -> Unit,
    ) {
        val textColor = MiuixTheme.colorScheme.onSurface.toArgb()
        val cursorColor = MiuixTheme.colorScheme.primaryVariant.toArgb()
        val bringIntoViewRequester = remember { BringIntoViewRequester() }
        var bringIntoViewRequest by remember { mutableStateOf(0) }

        LaunchedEffect(bringIntoViewRequest) {
            if (bringIntoViewRequest > 0) {
                delay(260)
                bringIntoViewRequester.bringIntoView()
            }
        }

        Box(
            modifier = Modifier
                .width(78.dp)
                .height(46.dp)
                .bringIntoViewRequester(bringIntoViewRequester)
                .clip(RoundedCornerShape(14.dp))
                .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    EditText(context).apply {
                        background = null
                        gravity = Gravity.CENTER
                        includeFontPadding = false
                        isSingleLine = true
                        filters = arrayOf(InputFilter.LengthFilter(3))
                        inputType = InputType.TYPE_CLASS_NUMBER
                        imeOptions = EditorInfo.IME_ACTION_DONE or
                            EditorInfo.IME_FLAG_NO_EXTRACT_UI or
                            EditorInfo.IME_FLAG_NO_FULLSCREEN
                        setSelectAllOnFocus(true)
                        setPadding(0, 0, 0, 0)
                        setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
                        setOnClickListener {
                            bringIntoViewRequest++
                            requestFocus()
                            post {
                                selectAll()
                                showKeyboardFor(this)
                            }
                        }
                        setOnFocusChangeListener { _, hasFocus ->
                            if (hasFocus) {
                                bringIntoViewRequest++
                                post {
                                    selectAll()
                                    showKeyboardFor(this)
                                }
                            } else if (text.isEmpty()) {
                                setText(fallbackValue.toString())
                                onValueChange(fallbackValue.toString())
                            }
                        }
                        setOnEditorActionListener { _, actionId, _ ->
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                onDone(text.toString())
                                clearFocus()
                                true
                            } else {
                                false
                            }
                        }
                    }
                },
                update = { editText ->
                    editText.isEnabled = enabled
                    editText.setTextColor(textColor)
                    editText.setHintTextColor(textColor)
                    editText.textCursorDrawable?.setTint(cursorColor)
                    editText.imeOptions = EditorInfo.IME_ACTION_DONE or
                        EditorInfo.IME_FLAG_NO_EXTRACT_UI or
                        EditorInfo.IME_FLAG_NO_FULLSCREEN
                    (editText.tag as? TextWatcher)?.let { editText.removeTextChangedListener(it) }
                    if (editText.text.toString() != value) {
                        editText.setText(value)
                        editText.setSelection(editText.text.length)
                    }
                    val watcher = object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            val digits = s?.filter { it.isDigit() }?.take(3)?.toString() ?: ""
                            if (digits != s.toString()) {
                                editText.setText(digits)
                                editText.setSelection(editText.text.length)
                                return
                            }
                            onValueChange(digits)
                        }
                        override fun afterTextChanged(s: Editable?) = Unit
                    }
                    editText.tag = watcher
                    editText.addTextChangedListener(watcher)
                }
            )
        }
    }

    @Composable
    private fun GenerationCard(selectedApp: AppEntry?) {
        val canRun = selectedApp != null && !isBusy
        SectionCard(title = "生成任务", summary = "生成 ART+ 图标包，Root 写入固定 data 分区") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { generateSelected(installWithRoot = false, useGpt = false) },
                    enabled = canRun,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(
                        text = "本地生成",
                        style = MiuixTheme.textStyles.button,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Button(
                    onClick = { generateSelected(installWithRoot = false, useGpt = true) },
                    enabled = canRun,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "GPT生成",
                        style = MiuixTheme.textStyles.button,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CompactActionButton(
                    text = "写入全部",
                    onClick = {
                        writeSelectedWithRoot(rootWriteMode = RootWriteMode.All)
                    },
                    enabled = canRun,
                    modifier = Modifier.weight(1f),
                )
                CompactActionButton(
                    text = "写入默认",
                    onClick = {
                        writeSelectedWithRoot(rootWriteMode = RootWriteMode.DefaultOnly)
                    },
                    enabled = canRun,
                    modifier = Modifier.weight(1f),
                )
                CompactActionButton(
                    text = "写入单色",
                    onClick = {
                        writeSelectedWithRoot(rootWriteMode = RootWriteMode.MonochromeOnly)
                    },
                    enabled = canRun,
                    modifier = Modifier.weight(1f),
                )
            }
            GeneratedPreviewSection()
            Spacer(modifier = Modifier.height(14.dp))
            SettingLine(
                title = "主体占比",
                summary = "复杂游戏图标建议 100%，范围 20% 到 150%",
                value = "$foregroundSubjectPercent%",
            )
            Spacer(modifier = Modifier.height(12.dp))
            SteppedPercentSlider(
                value = foregroundSubjectPercent,
                min = MIN_FOREGROUND_SUBJECT_PERCENT,
                max = MAX_FOREGROUND_SUBJECT_PERCENT,
                step = 1,
                enabled = !isBusy,
                showDots = false,
                onValueChange = { updateForegroundSubjectPercent(it) },
            )
            Spacer(modifier = Modifier.height(14.dp))
            AdvancedSeparationSettings()
        }
    }

    @Composable
    private fun CompactActionButton(
        text: String,
        onClick: () -> Unit,
        enabled: Boolean,
        modifier: Modifier = Modifier,
        height: Dp = 50.dp,
    ) {
        val darkTheme = isSystemInDarkTheme()
        val background = if (darkTheme) {
            Color(0xFF444444)
        } else {
            Color(0xFFEFEFEF)
        }
        val foreground = if (enabled) {
            MiuixTheme.colorScheme.onSurface
        } else {
            MiuixTheme.colorScheme.onSurfaceVariantSummary
        }
        Box(
            modifier = modifier
                .height(height)
                .clip(RoundedCornerShape(18.dp))
                .background(background)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MiuixTheme.textStyles.button.copy(fontSize = 15.sp),
                color = foreground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }

    @Composable
    private fun GptSettingsCard() {
        SectionCard {
            GptModeChoiceRow()
            Spacer(modifier = Modifier.height(12.dp))
            GptPromptChoiceRow()
            AnimatedVisibility(
                visible = gptPromptPreset == GptPromptPreset.Custom,
                enter = fadeIn(animationSpec = tween(durationMillis = 150)) +
                    expandVertically(animationSpec = tween(durationMillis = 180)),
                exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                    shrinkVertically(animationSpec = tween(durationMillis = 160)),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    InlineInputField(
                        value = gptCustomPrompt,
                        onValueChange = {
                            gptCustomPrompt = it
                            gptSettingsSaveStatus = ""
                        },
                        label = "自定义前景提示词",
                        icon = SettingsIconKind.Prompt,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            InlineInputField(
                value = gptBaseUrl,
                onValueChange = {
                    gptBaseUrl = it
                    gptSettingsSaveStatus = ""
                },
                label = "Base URL",
                icon = SettingsIconKind.Link,
            )
            Spacer(modifier = Modifier.height(8.dp))
            InlineInputField(
                value = gptApiKey,
                onValueChange = {
                    gptApiKey = it
                    gptSettingsSaveStatus = ""
                },
                label = "API key",
                obscure = true,
                icon = SettingsIconKind.Key,
            )
        }
    }

    @Composable
    private fun RmbgComponentCard() {
        val component = remember(rmbgComponentStatus) { findRmbgComponent() }

        SectionCard {
            SettingLine(
                title = "RMBG 状态",
                summary = component?.let { "ABI ${it.abi}" } ?: "未安装",
                value = if (component == null) "未安装" else "已安装",
            )
            Spacer(modifier = Modifier.height(12.dp))
            RmbgModelPresetChoiceRow(
                enabled = !isGeneratingRmbgCandidate && !isInstallingRmbgComponent,
            )
            if (lastRmbgCandidateError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = lastRmbgCandidateError.orEmpty(),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.error,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            InlineInputField(
                value = rmbgComponentUrl,
                onValueChange = {
                    rmbgComponentUrl = it
                    rmbgComponentSaveStatus = ""
                },
                label = "模型或组件 ZIP URL",
                icon = SettingsIconKind.Link,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    text = "选择 ZIP",
                    onClick = {
                        chooseRmbgComponentLauncher.launch(
                            arrayOf("application/zip", "application/octet-stream", "*/*"),
                        )
                    },
                    enabled = !isBusy && !isGeneratingRmbgCandidate && !isInstallingRmbgComponent,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = if (isInstallingRmbgComponent) "安装中" else "一键安装",
                    onClick = { installRmbgComponentFromUrl() },
                    enabled = !isBusy && !isGeneratingRmbgCandidate && !isInstallingRmbgComponent,
                    modifier = Modifier.weight(1f),
                )
            }
            if (isInstallingRmbgComponent || rmbgInstallStage.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                RmbgInstallProgressBar(
                    text = rmbgInstallStage.ifBlank { if (isInstallingRmbgComponent) "安装中" else "" },
                    progress = rmbgInstallProgress,
                    active = isInstallingRmbgComponent,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(
                text = "清除已安装 RMBG",
                onClick = { clearInstalledRmbgComponent() },
                enabled = component != null && !isBusy && !isGeneratingRmbgCandidate && !isInstallingRmbgComponent,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    @Composable
    private fun RmbgInstallProgressBar(text: String, progress: Float?, active: Boolean) {
        val transition = rememberInfiniteTransition(label = "RmbgInstallProgress")
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1300, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "RmbgInstallProgressPhase",
        )
        val fraction = progress?.coerceIn(0f, 1f)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (text.isNotBlank()) {
                Text(
                    text = text,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MiuixTheme.colorScheme.surfaceContainerHigh),
            ) {
                if (fraction != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MiuixTheme.colorScheme.primaryVariant),
                    )
                } else if (active) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.36f)
                            .offset { IntOffset(((phase * 1.64f - 0.36f) * 1000).roundToInt(), 0) }
                            .clip(RoundedCornerShape(999.dp))
                            .background(MiuixTheme.colorScheme.primaryVariant),
                    )
                }
            }
        }
    }

    @Composable
    private fun OutputCard() {
        SectionCard {
            SettingLine(
                title = "Root 目标",
                summary = "/data/oplus/uxicons/{package}",
                value = "data",
            )
            Spacer(modifier = Modifier.height(10.dp))
            SettingNavigationLine(
                title = "外部导出",
                summary = if (outputTreeUri == null) "未选择时仅保存在应用私有目录" else "生成后同步复制到你选择的目录",
                enabled = !isBusy,
                onClick = { chooseTreeLauncher.launch(null) },
            )
        }
    }

    @Composable
    private fun LiquidGlassToggleRow() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(enabled = !isBusy) { updateLiquidGlassEnabled(!liquidGlassEnabled) }
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsLineIcon(kind = SettingsIconKind.Glass)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "液态玻璃风格",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "开启后按当前液态玻璃参数重绘背景和前景光影",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            LiquidGlassSwitch(checked = liquidGlassEnabled, enabled = !isBusy)
        }
    }

    @Composable
    private fun LiquidGlassSwitch(checked: Boolean, enabled: Boolean) {
        val targetTrackColor = when {
            checked && enabled -> MiuixTheme.colorScheme.primaryVariant
            checked -> MiuixTheme.colorScheme.primaryVariant.copy(alpha = 0.46f)
            enabled -> MiuixTheme.colorScheme.surfaceContainerHigh
            else -> MiuixTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.52f)
        }
        val targetThumbColor = if (enabled) {
            MiuixTheme.colorScheme.onPrimaryVariant
        } else {
            MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.54f)
        }
        val trackColor by animateColorAsState(
            targetValue = targetTrackColor,
            animationSpec = tween(durationMillis = 180),
            label = "LiquidGlassSwitchTrack",
        )
        val thumbColor by animateColorAsState(
            targetValue = targetThumbColor,
            animationSpec = tween(durationMillis = 180),
            label = "LiquidGlassSwitchThumb",
        )
        val thumbOffset by animateDpAsState(
            targetValue = if (checked) 24.dp else 0.dp,
            animationSpec = tween(durationMillis = 180),
            label = "LiquidGlassSwitchOffset",
        )
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(trackColor)
                .padding(3.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .offset(x = thumbOffset)
                    .clip(RoundedCornerShape(999.dp))
                    .background(thumbColor),
            )
        }
    }

    @Composable
    private fun LiquidGlassSectionTitle(title: String) {
        Text(
            text = title,
            style = MiuixTheme.textStyles.title4,
            color = MiuixTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

    @Composable
    private fun InputSettingsCard(launcherCount: Int, totalCount: Int, generatedCount: Int) {
        SectionCard {
            SettingLine(
                title = "应用范围",
                summary = "启动器 $launcherCount 个 / 全部 $totalCount 个",
                value = "启动器",
            )
            Spacer(modifier = Modifier.height(10.dp))
            SettingLine(
                title = "已生成",
                summary = "来自本地缓存；手动刷新后才重新读取 data 路径",
                value = "$generatedCount",
                enabled = !isBusy,
                onClick = { loadApps(refreshGenerated = true) },
            )
        }
    }

    @Composable
    private fun AppPickerControlsCard(
        filteredCount: Int,
        totalCount: Int,
        generatedCount: Int,
        ungeneratedCount: Int,
        filteredApps: List<AppEntry>,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "应用列表",
                        style = MiuixTheme.textStyles.title4,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                    TextButton(
                        text = "刷新",
                        onClick = { refreshGeneratedPackages() },
                        enabled = !isBusy && apps.isNotEmpty(),
                    )
                }
                Text(
                    text = buildString {
                        append("$filteredCount/$totalCount")
                        append(" · 已生成 $generatedCount")
                        append(" · 未生成 $ungeneratedCount")
                        if (isScanningGeneratedPackages) {
                            append(" · 扫描中")
                        } else if (generatedScanFailed) {
                            append(" · 无法读取 data 路径")
                        }
                    },
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val filters = GeneratedFilter.entries
                SegmentedControl(
                    labels = filters.map { it.label },
                    selectedIndex = filters.indexOf(generatedFilter),
                    onSelected = { index ->
                        generatedFilter = filters[index]
                        queryText = ""
                        saveUiState()
                    }
                )
                InlineInputField(
                    value = queryText,
                    onValueChange = {
                        queryText = it
                        saveUiState()
                    },
                    label = "搜索应用或包名",
                )
                AppMultiSelectActions(filteredApps)
            }
        }
    }

    @Composable
    private fun AppMultiSelectActions(filteredApps: List<AppEntry>) {
        val filteredPackageNames = remember(filteredApps) { filteredApps.map { it.packageName }.toSet() }
        val selectedCount = multiSelectedPackageNames.size
        val hasFiltered = filteredPackageNames.isNotEmpty()
        val allFilteredSelected = hasFiltered && filteredPackageNames.all { it in multiSelectedPackageNames }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CompactActionButton(
                text = if (allFilteredSelected) "取消当前" else "选择当前",
                onClick = {
                    multiSelectedPackageNames = if (allFilteredSelected) {
                        multiSelectedPackageNames - filteredPackageNames
                    } else {
                        multiSelectedPackageNames + filteredPackageNames
                    }
                },
                enabled = !isBusy && hasFiltered,
                modifier = Modifier.weight(1f),
                height = 48.dp,
            )
            CompactActionButton(
                text = "清空",
                onClick = { multiSelectedPackageNames = emptySet() },
                enabled = !isBusy && selectedCount > 0,
                modifier = Modifier.weight(1f),
                height = 48.dp,
            )
        }
        CompactActionButton(
            text = "添加光影 $selectedCount",
            onClick = { addLiquidGlassToMultiSelectedGenerated() },
            enabled = !isBusy && selectedCount > 0,
            modifier = Modifier.fillMaxWidth(),
            height = 50.dp,
        )
    }

    @Composable
    private fun AppRow(
        entry: AppEntry,
        selected: Boolean,
        multiSelected: Boolean,
        generated: Boolean,
        onClick: () -> Unit,
        onToggleMultiSelect: () -> Unit,
    ) {
        val selectedTagBg = MiuixTheme.colorScheme.primaryVariant
        val selectedTagFg = MiuixTheme.colorScheme.onPrimaryVariant
        val multiSelectedTagBg = MiuixTheme.colorScheme.primaryContainer
        val multiSelectedTagFg = MiuixTheme.colorScheme.onPrimaryContainer
        val generatedTagBg = MiuixTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
        val generatedTagFg = MiuixTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
        val allTagBg = MiuixTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
        val allTagFg = MiuixTheme.colorScheme.onSecondaryContainer
        val tags = remember(
            selected,
            multiSelected,
            generated,
            entry.launchable,
            selectedTagBg,
            selectedTagFg,
            multiSelectedTagBg,
            multiSelectedTagFg,
            generatedTagBg,
            generatedTagFg,
            allTagBg,
            allTagFg,
        ) {
            buildList {
                if (selected) add(AppListTag("已选", selectedTagBg, selectedTagFg))
                if (multiSelected) add(AppListTag("多选", multiSelectedTagBg, multiSelectedTagFg))
                if (generated) add(AppListTag("已生成", generatedTagBg, generatedTagFg))
                if (!entry.launchable) add(AppListTag("全部", allTagBg, allTagFg))
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            insideMargin = PaddingValues(start = 10.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            showIndication = true,
            onClick = onClick,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIcon(
                    entry = entry,
                    size = 48.dp,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = entry.label,
                        modifier = Modifier.basicMarquee(),
                        style = MiuixTheme.textStyles.body1.copy(fontWeight = FontWeight(550)),
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false,
                    )
                    Text(
                        text = entry.packageName,
                        modifier = Modifier.basicMarquee(),
                        style = MiuixTheme.textStyles.footnote1.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight(550),
                        ),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
                if (tags.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(start = 16.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        tags.forEach { tag ->
                            AppStatusTag(tag = tag)
                        }
                    }
                }
                TextButton(
                    text = if (multiSelected) "已选" else "选择",
                    onClick = onToggleMultiSelect,
                    enabled = !isBusy,
                )
                KernelStyleArrow()
            }
        }
    }

    private data class AppListTag(
        val label: String,
        val backgroundColor: Color,
        val contentColor: Color,
    )

    @Composable
    private fun AppStatusTag(tag: AppListTag) {
        Box(
            modifier = Modifier
                .background(
                    color = tag.backgroundColor,
                    shape = RoundedCornerShape(6.dp),
                ),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                text = tag.label,
                fontSize = 9.sp,
                fontWeight = FontWeight(750),
                color = tag.contentColor,
                maxLines = 1,
                softWrap = false,
            )
        }
    }

    @Composable
    private fun KernelStyleArrow(modifier: Modifier = Modifier, expanded: Boolean = false) {
        val layoutDirection = LocalLayoutDirection.current
        val rotation by animateFloatAsState(
            targetValue = if (expanded) 90f else 0f,
            animationSpec = tween(durationMillis = 180),
            label = "kernel-style-arrow-rotation",
        )

        Image(
            modifier = modifier
                .graphicsLayer {
                    rotationZ = rotation
                    if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
                }
                .padding(start = 8.dp)
                .size(width = 10.dp, height = 16.dp),
            imageVector = MiuixIcons.Basic.ArrowRight,
            contentDescription = null,
            colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions),
        )
    }

    @Composable
    private fun SectionCard(
        title: String? = null,
        summary: String? = null,
        content: @Composable () -> Unit,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(16.dp),
        ) {
            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    style = MiuixTheme.textStyles.title4,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!summary.isNullOrBlank()) {
                if (!title.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                }
                Text(
                    text = summary,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!title.isNullOrBlank() || !summary.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
            }
            content()
        }
    }

    @Composable
    private fun GptModeChoiceRow() {
        val modes = GptImageMode.entries
        ChoicePopupRow(
            title = "GPT image two 生成模式",
            summary = gptImageMode.shortSummary(),
            value = gptImageMode.label,
            enabled = !isBusy,
            icon = SettingsIconKind.Spark,
            options = modes,
            selected = gptImageMode,
            optionLabel = { it.label },
            optionSummary = { it.shortSummary() },
            onSelected = { mode ->
                gptImageMode = mode
                gptSettingsSaveStatus = ""
            },
        )
    }

    private fun GptImageMode.shortSummary(): String = when (this) {
        GptImageMode.Responses -> "Codex image gen"
        GptImageMode.Images -> "直连 gpt-image-2"
    }

    @Composable
    private fun GptPromptChoiceRow() {
        val presets = GptPromptPreset.entries
        ChoicePopupRow(
            title = "GPT 提示词",
            summary = gptPromptPreset.summary,
            value = gptPromptPreset.label,
            enabled = !isBusy,
            icon = SettingsIconKind.Prompt,
            options = presets,
            selected = gptPromptPreset,
            optionLabel = { it.label },
            optionSummary = { it.summary },
            onSelected = { preset ->
                gptPromptPreset = preset
                gptSettingsSaveStatus = ""
            },
        )
    }

    @Composable
    private fun RmbgModelPresetChoiceRow(enabled: Boolean) {
        val options = remember { RMBG_MODEL_PRESETS }
        val selected = currentRmbgModelPreset()
        ChoicePopupRow(
            title = "模型版本",
            summary = selected.summary,
            value = selected.label,
            enabled = enabled && !isBusy,
            icon = SettingsIconKind.Layers,
            options = options,
            selected = selected,
            optionLabel = { it.label },
            optionSummary = { it.summary },
            onSelected = { updateRmbgModelPreset(it) },
        )
    }

    @Composable
    private fun <T> ChoicePopupRow(
        title: String,
        summary: String,
        value: String,
        enabled: Boolean,
        icon: SettingsIconKind? = null,
        options: List<T>,
        selected: T,
        optionLabel: (T) -> String,
        optionSummary: (T) -> String,
        onSelected: (T) -> Unit,
    ) {
        var anchorBounds by remember { mutableStateOf<Rect?>(null) }
        val interactionSource = remember { MutableInteractionSource() }
        val pressed by interactionSource.collectIsPressedAsState()
        val rowOverlay = if (pressed) {
            MiuixTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.74f)
        } else {
            Color.Transparent
        }
        val density = LocalDensity.current
        val bleedPx = with(density) { CHOICE_ROW_HORIZONTAL_BLEED_DP.dp.roundToPx() }

        fun openDialog() {
            openChoicePopup(
                anchorBounds = anchorBounds,
                items = options.map { option ->
                    ChoicePopupItem(
                        label = optionLabel(option),
                        summary = optionSummary(option),
                        selected = option == selected,
                        onSelected = { onSelected(option) },
                    )
                },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val expandedWidth = constraints.maxWidth + bleedPx * 2
                    val placeable = measurable.measure(
                        constraints.copy(
                            minWidth = expandedWidth,
                            maxWidth = expandedWidth,
                        ),
                    )
                    layout(constraints.maxWidth, placeable.height) {
                        placeable.place(-bleedPx, 0)
                    }
                }
                .onGloballyPositioned { anchorBounds = it.boundsInWindow() }
                .clip(RoundedCornerShape(16.dp))
                .background(rowOverlay)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = { openDialog() },
                )
                .padding(vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CHOICE_ROW_HORIZONTAL_BLEED_DP.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SettingsLineIcon(kind = icon ?: settingsIconForTitle(title))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = title,
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = summary,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                MetricPill(label = value)
                ChoicePopupChevron()
            }
        }
    }

    @Composable
    private fun ChoicePopupChevron() {
        val color = MiuixTheme.colorScheme.onSurfaceVariantSummary
        ComposeCanvas(
            modifier = Modifier
                .width(20.dp)
                .height(28.dp),
        ) {
            val strokeWidth = 2.4.dp.toPx()
            val left = 3.5.dp.toPx()
            val right = size.width - left
            val centerX = size.width / 2f
            val upperTop = 7.dp.toPx()
            val upperBottom = 12.dp.toPx()
            val lowerTop = 16.dp.toPx()
            val lowerBottom = 21.dp.toPx()

            drawLine(
                color = color,
                start = Offset(left, upperBottom),
                end = Offset(centerX, upperTop),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = color,
                start = Offset(centerX, upperTop),
                end = Offset(right, upperBottom),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = color,
                start = Offset(left, lowerTop),
                end = Offset(centerX, lowerBottom),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = color,
                start = Offset(centerX, lowerBottom),
                end = Offset(right, lowerTop),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }

    @Composable
    private fun ChoicePopupOverlay(
        request: ChoicePopupRequest,
        visible: Boolean,
        pageBackground: Color,
        onDismiss: () -> Unit,
    ) {
        val density = LocalDensity.current
        val popupWidth = 224.dp
        val popupWidthPx = with(density) { popupWidth.roundToPx() }
        val marginPx = with(density) { 16.dp.roundToPx() }
        val overlapPx = with(density) { 32.dp.roundToPx() }
        val estimatedHeightPx = with(density) {
            (request.items.size * 68).dp.roundToPx() + 24.dp.roundToPx()
        }
        val screenWidthPx = resources.displayMetrics.widthPixels
        val screenHeightPx = resources.displayMetrics.heightPixels
        val anchor = request.anchorBounds
        val popupX = if (anchor == null) {
            ((screenWidthPx - popupWidthPx) / 2).coerceAtLeast(marginPx)
        } else {
            (anchor.right.roundToInt() - popupWidthPx)
                .coerceIn(marginPx, screenWidthPx - popupWidthPx - marginPx)
        }
        val preferredY = anchor?.bottom?.roundToInt()?.minus(overlapPx)
            ?: ((screenHeightPx - estimatedHeightPx) / 2)
        val popupY = if (preferredY + estimatedHeightPx > screenHeightPx - marginPx) {
            (anchor?.top?.roundToInt()?.minus(estimatedHeightPx)?.plus(overlapPx) ?: preferredY)
                .coerceAtLeast(marginPx)
        } else {
            preferredY.coerceAtLeast(marginPx)
        }
        val overlayAlpha by animateFloatAsState(
            targetValue = if (visible) 0.22f else 0f,
            animationSpec = tween(durationMillis = 140),
            label = "choice-popup-overlay-alpha",
        )
        val popupAlpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(durationMillis = 160),
            label = "choice-popup-alpha",
        )
        val popupScale by animateFloatAsState(
            targetValue = if (visible) 1f else 0.96f,
            animationSpec = tween(durationMillis = 160),
            label = "choice-popup-scale",
        )
        val popupTranslationY by animateFloatAsState(
            targetValue = if (visible) 0f else 10f,
            animationSpec = tween(durationMillis = 160),
            label = "choice-popup-translation-y",
        )

        ChoicePopupSystemBars(
            overlayAlpha = overlayAlpha,
            pageBackground = pageBackground,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = overlayAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        ) {
            Column(
                modifier = Modifier
                    .offset { IntOffset(popupX, popupY) }
                    .graphicsLayer {
                        alpha = popupAlpha
                        scaleX = popupScale
                        scaleY = popupScale
                        translationY = popupTranslationY
                    }
                    .width(popupWidth)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MiuixTheme.colorScheme.surface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    )
                    .padding(vertical = 12.dp),
            ) {
                request.items.forEach { item ->
                    ChoicePopupOptionRow(
                        item = item,
                        onSelected = {
                            item.onSelected()
                            onDismiss()
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun ChoicePopupSystemBars(overlayAlpha: Float, pageBackground: Color) {
        val darkTheme = isSystemInDarkTheme()
        DisposableEffect(darkTheme) {
            onDispose {
                window.statusBarColor = AndroidColor.TRANSPARENT
                window.navigationBarColor = AndroidColor.TRANSPARENT
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
            }
        }
        SideEffect {
            val barColor = blackScrimOver(pageBackground, overlayAlpha)
            window.statusBarColor = barColor
            window.navigationBarColor = barColor
            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    private fun blackScrimOver(base: Color, alpha: Float): Int {
        val retain = 1f - alpha.coerceIn(0f, 1f)
        return AndroidColor.rgb(
            (base.red * 255f * retain).roundToInt().coerceIn(0, 255),
            (base.green * 255f * retain).roundToInt().coerceIn(0, 255),
            (base.blue * 255f * retain).roundToInt().coerceIn(0, 255),
        )
    }

    @Composable
    private fun ChoicePopupOptionRow(item: ChoicePopupItem, onSelected: () -> Unit) {
        val interactionSource = remember { MutableInteractionSource() }
        val pressed by interactionSource.collectIsPressedAsState()
        val rowBackground = if (pressed) {
            MiuixTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.74f)
        } else {
            Color.Transparent
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(rowBackground)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onSelected,
                )
                .padding(horizontal = 22.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = item.label,
                    style = MiuixTheme.textStyles.body1,
                    color = if (item.selected) {
                        MiuixTheme.colorScheme.primaryVariant
                    } else {
                        MiuixTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.summary.isNotBlank()) {
                    Text(
                        text = item.summary,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (item.selected) {
                Text(
                    text = "✓",
                    style = MiuixTheme.textStyles.title4,
                    color = MiuixTheme.colorScheme.primaryVariant,
                    maxLines = 1,
                )
            }
        }
    }

    @Composable
    private fun SegmentedControl(
        labels: List<String>,
        selectedIndex: Int,
        scrollable: Boolean = false,
        onSelected: (Int) -> Unit,
    ) {
        if (labels.isEmpty()) {
            return
        }
        val shape = RoundedCornerShape(16.dp)
        val safeSelectedIndex = selectedIndex.coerceIn(0, labels.lastIndex)
        val density = LocalDensity.current
        var widthPx by remember(labels.size) { mutableStateOf(0) }
        val gap = 10.dp
        val gapPx = with(density) { gap.toPx() }
        val minSegmentWidth = if (scrollable) 86.dp else 0.dp
        val minSegmentWidthPx = with(density) { minSegmentWidth.toPx() }
        val segmentWidthPx = if (widthPx == 0) {
            0f
        } else if (scrollable) {
            val availableWidth = (widthPx.toFloat() - gapPx * (labels.size - 1)).coerceAtLeast(1f)
            maxOf(minSegmentWidthPx, availableWidth / labels.size.toFloat())
        } else {
            val availableWidth = (widthPx.toFloat() - gapPx * (labels.size - 1)).coerceAtLeast(1f)
            availableWidth / labels.size.toFloat()
        }
        val selectedOffsetPx by animateFloatAsState(
            targetValue = (segmentWidthPx + gapPx) * safeSelectedIndex,
            animationSpec = tween(durationMillis = 220),
            label = "SegmentedControlOffset",
        )
        val selectedWidth = with(density) { segmentWidthPx.toDp() }
        val contentWidth = with(density) {
            (segmentWidthPx * labels.size + gapPx * (labels.size - 1)).toDp()
        }
        val optionBackground = if (isSystemInDarkTheme()) {
            Color(0xFF444444)
        } else {
            Color(0xFFEFEFEF)
        }
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .onGloballyPositioned { coordinates ->
                    widthPx = coordinates.size.width
                },
        ) {
            Box(
                modifier = Modifier
                    .then(
                        if (widthPx > 0) {
                            Modifier.width(contentWidth)
                        } else {
                            Modifier.fillMaxWidth()
                        }
                    )
                    .fillMaxHeight()
                    .then(if (scrollable) Modifier.horizontalScroll(scrollState) else Modifier),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    labels.forEach {
                        Box(
                            modifier = Modifier
                                .then(
                                    if (widthPx > 0) {
                                        Modifier.width(selectedWidth)
                                    } else {
                                        Modifier.weight(1f)
                                    }
                                )
                                .fillMaxHeight()
                                .clip(shape)
                                .background(optionBackground),
                        )
                    }
                }
                if (widthPx > 0) {
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(selectedOffsetPx.roundToInt(), 0) }
                            .width(selectedWidth)
                            .fillMaxHeight()
                            .clip(shape)
                            .background(MiuixTheme.colorScheme.primaryVariant),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    labels.forEachIndexed { index, label ->
                        val selected = index == safeSelectedIndex
                        val interactionSource = remember { MutableInteractionSource() }
                        val foreground = if (selected) {
                            Color.White
                        } else {
                            MiuixTheme.colorScheme.onSurfaceVariantSummary
                        }
                        Box(
                            modifier = Modifier
                                .then(
                                    if (widthPx > 0) {
                                        Modifier.width(selectedWidth)
                                    } else {
                                        Modifier.weight(1f)
                                    }
                                )
                                .fillMaxHeight()
                                .clip(shape)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    enabled = !isBusy && !selected,
                                ) {
                                    onSelected(index)
                                }
                                .padding(horizontal = 2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                style = MiuixTheme.textStyles.button,
                                color = foreground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SegmentOption(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
        val background = if (selected) {
            MiuixTheme.colorScheme.primaryVariant
        } else {
            MiuixTheme.colorScheme.surfaceContainerHigh
        }
        val foreground = if (selected) {
            MiuixTheme.colorScheme.onPrimaryVariant
        } else {
            MiuixTheme.colorScheme.onSurfaceVariantSummary
        }
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(background)
                .clickable(enabled = !isBusy, onClick = onClick)
                .padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MiuixTheme.textStyles.button,
                color = foreground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    @Composable
    private fun InlineInputField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        obscure: Boolean = false,
        icon: SettingsIconKind? = null,
    ) {
        val bringIntoViewRequester = remember { BringIntoViewRequester() }
        var focused by remember { mutableStateOf(false) }

        LaunchedEffect(focused) {
            if (focused) {
                delay(260)
                bringIntoViewRequester.bringIntoView()
            }
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MiuixTheme.textStyles.body1.copy(
                color = MiuixTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MiuixTheme.colorScheme.primaryVariant),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            visualTransformation = if (obscure) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged { focused = it.isFocused },
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        icon?.let {
                            SettingsLineIcon(kind = it)
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = label,
                                    style = MiuixTheme.textStyles.body1,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            },
        )
    }

    @Composable
    private fun SettingLine(
        title: String,
        summary: String,
        value: String,
        enabled: Boolean = true,
        onClick: (() -> Unit)? = null,
    ) {
        val rowModifier = if (onClick == null) {
            Modifier.fillMaxWidth()
        } else {
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(enabled = enabled, onClick = onClick)
                .padding(vertical = 2.dp)
        }
        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsLineIcon(kind = settingsIconForTitle(title))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = summary,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            MetricPill(label = value)
        }
    }

    @Composable
    private fun SettingNavigationLine(
        title: String,
        summary: String,
        enabled: Boolean,
        onClick: () -> Unit,
    ) {
        val arrowColor = if (enabled) {
            MiuixTheme.colorScheme.onSurfaceVariantSummary
        } else {
            MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.52f)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(enabled = enabled, onClick = onClick)
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsLineIcon(kind = settingsIconForTitle(title))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = summary,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Image(
                imageVector = Lucide.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                colorFilter = ColorFilter.tint(arrowColor),
            )
        }
    }

    private fun settingsIconForTitle(title: String): SettingsIconKind =
        when (title) {
            "应用范围" -> SettingsIconKind.Grid
            "应用列表" -> SettingsIconKind.Grid
            "已生成" -> SettingsIconKind.CheckBadge
            "使用情况访问" -> SettingsIconKind.Shield
            "Root 目标" -> SettingsIconKind.Shield
            "外部导出" -> SettingsIconKind.FileUpload
            "RMBG 状态" -> SettingsIconKind.Chip
            "模型版本" -> SettingsIconKind.Layers
            "GPT image two 生成模式" -> SettingsIconKind.Spark
            "GPT 提示词" -> SettingsIconKind.Prompt
            "液态玻璃风格" -> SettingsIconKind.Glass
            "渲染方式" -> SettingsIconKind.Layers
            "圆角半径" -> SettingsIconKind.Radius
            "边缘高光宽度" -> SettingsIconKind.Glass
            "主体占比" -> SettingsIconKind.Scale
            "主体阴影等级" -> SettingsIconKind.Shadow
            "单色主体缩放" -> SettingsIconKind.Scale
            "背景剔除阈值" -> SettingsIconKind.Cutout
            "背景相似度" -> SettingsIconKind.Cutout
            "底板颜色阈值" -> SettingsIconKind.Plate
            "底板清理" -> SettingsIconKind.Plate
            "长阴影清理强度" -> SettingsIconKind.Shadow
            "旧阴影清理" -> SettingsIconKind.Eraser
            "毛刺优化" -> SettingsIconKind.Spark
            "边缘修补" -> SettingsIconKind.Spark
            "RMBG Alpha 强度" -> SettingsIconKind.Cutout
            "Alpha 力度" -> SettingsIconKind.Cutout
            "RMBG 边缘柔化" -> SettingsIconKind.Cutout
            "边缘柔化" -> SettingsIconKind.Cutout
            "RMBG 边缘扩张" -> SettingsIconKind.Scale
            "边缘扩缩" -> SettingsIconKind.Scale
            "RMBG 弱透明保留" -> SettingsIconKind.Cutout
            "弱透明保留" -> SettingsIconKind.Cutout
            "单色缩放" -> SettingsIconKind.Scale
            else -> SettingsIconKind.Dot
        }

    private enum class SettingsIconKind {
        Grid,
        CheckBadge,
        Shield,
        FileUpload,
        Glass,
        Radius,
        Chip,
        Layers,
        Spark,
        Scale,
        Cutout,
        Palette,
        Plate,
        Shadow,
        Eraser,
        Link,
        Key,
        Prompt,
        Dot,
    }

    private fun settingsIconVector(kind: SettingsIconKind): ImageVector = when (kind) {
        SettingsIconKind.Grid -> Lucide.Grid2x2
        SettingsIconKind.CheckBadge -> Lucide.BadgeCheck
        SettingsIconKind.Shield -> Lucide.Shield
        SettingsIconKind.FileUpload -> Lucide.FileUp
        SettingsIconKind.Glass -> Lucide.GlassWater
        SettingsIconKind.Radius -> Lucide.Radius
        SettingsIconKind.Chip -> Lucide.Cpu
        SettingsIconKind.Layers -> Lucide.Layers
        SettingsIconKind.Spark -> Lucide.Sparkles
        SettingsIconKind.Scale -> Lucide.Scale
        SettingsIconKind.Cutout -> Lucide.SlidersHorizontal
        SettingsIconKind.Palette -> Lucide.Palette
        SettingsIconKind.Plate -> Lucide.Palette
        SettingsIconKind.Shadow -> Lucide.Sparkles
        SettingsIconKind.Eraser -> Lucide.Eraser
        SettingsIconKind.Link -> Lucide.Link
        SettingsIconKind.Key -> Lucide.KeyRound
        SettingsIconKind.Prompt -> Lucide.MessageSquareText
        SettingsIconKind.Dot -> Lucide.Settings
    }

    @Composable
    private fun SettingsLineIcon(kind: SettingsIconKind, modifier: Modifier = Modifier) {
        Image(
            imageVector = settingsIconVector(kind),
            contentDescription = null,
            modifier = modifier.size(18.dp),
            colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurface),
        )
    }

    @Composable
    private fun AppIcon(entry: AppEntry, size: Dp) {
        var bitmap by remember(entry.iconKey) {
            mutableStateOf(getCachedAppIcon(entry.iconKey))
        }
        val imageBitmap = remember(bitmap) { bitmap?.asImageBitmap() }

        LaunchedEffect(entry.iconKey) {
            if (bitmap == null) {
                bitmap = loadCachedAppIcon(entry)
            }
        }

        Box(
            modifier = Modifier
                .size(size),
            contentAlignment = Alignment.Center,
        ) {
            if (imageBitmap == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MiuixTheme.colorScheme.secondaryContainer),
                )
            } else {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }

    private fun getCachedAppIcon(key: String): Bitmap? =
        synchronized(appIconCache) { appIconCache.get(key) }

    private suspend fun loadCachedAppIcon(entry: AppEntry): Bitmap? =
        withContext(Dispatchers.IO) {
            val cached = synchronized(appIconCache) {
                appIconCache.get(entry.iconKey)
            }
            if (cached != null) {
                return@withContext cached
            }

            val bitmap = runCatching { loadAppIconBitmap(entry) }.getOrNull() ?: return@withContext null

            synchronized(appIconCache) {
                appIconCache.put(entry.iconKey, bitmap)
            }
            bitmap
        }

    private fun loadAppIconBitmap(entry: AppEntry): Bitmap =
        drawDrawable(entry.applicationInfo.loadIcon(packageManager), ICON_CACHE_SIZE, ICON_CACHE_SIZE, transparent = true)
            .also { it.prepareToDraw() }

    private fun loadPreviewWallpaperBitmap(): Bitmap? =
        runCatching {
            val drawable = WallpaperManager.getInstance(this).drawable ?: return null
            drawDrawable(
                drawable = drawable,
                width = PREVIEW_WALLPAPER_SAMPLE_SIZE,
                height = PREVIEW_WALLPAPER_SAMPLE_SIZE,
                transparent = false,
            ).also { it.prepareToDraw() }
        }.getOrNull()

    @Composable
    private fun BrandMark(size: Dp, text: String) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(16.dp))
                .background(MiuixTheme.colorScheme.primaryVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MiuixTheme.textStyles.title4,
                color = MiuixTheme.colorScheme.onPrimaryVariant,
                maxLines = 1,
            )
        }
    }

    @Composable
    private fun MetricPill(label: String, modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(999.dp))
                .background(MiuixTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    @Composable
    private fun StatusDot(active: Boolean) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    if (active) {
                        MiuixTheme.colorScheme.primaryVariant
                    } else {
                        MiuixTheme.colorScheme.secondaryContainer
                    },
                ),
        )
    }

    private fun loadApps(refreshGenerated: Boolean = false) {
        didRequestAppLoad = true
        Thread {
            val pm = packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
            val launchablePackages = queryLaunchablePackages(pm, intent)
            val installedApps = getInstalledApplications(pm)
            val entries = installedApps.mapNotNull { info ->
                val packageName = info.packageName ?: return@mapNotNull null
                val label = runCatching { info.loadLabel(pm)?.toString() }
                    .getOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?: packageName
                AppEntry(
                    label = label,
                    packageName = packageName,
                    applicationInfo = info,
                    launchable = packageName in launchablePackages,
                    iconKey = "${packageName}:${info.uid}:${info.sourceDir}",
                )
            }
                .sortedWith(
                    compareByDescending<AppEntry> { it.launchable }
                        .thenBy { it.label.lowercase(Locale.ROOT) }
                        .thenBy { it.packageName },
                )
            preloadAppIcons(entries)
            runOnUiThread {
                refreshPermissionState()
                apps.clear()
                apps.addAll(entries)
                statusText = when {
                    entries.isEmpty() -> "没有读取到应用。请确认已允许读取应用列表。"
                    !packageListPermissionGranted -> "读取到 ${apps.size} 个应用，但应用列表权限状态异常。"
                    else -> "共 ${apps.size} 个应用，其中 ${launchablePackages.size} 个有启动器入口。"
                }
                if (refreshGenerated) {
                    refreshGeneratedPackages(entries)
                }
            }
        }.start()
    }

    private fun refreshGeneratedPackages(entries: List<AppEntry> = apps.toList()) {
        if (entries.isEmpty()) {
            generatedScanFailed = false
            isScanningGeneratedPackages = false
            statusText = "应用列表为空，保留已生成缓存"
            return
        }
        isScanningGeneratedPackages = true
        generatedScanFailed = false
        Thread {
            val packageNames = entries.map { it.packageName }.toSet()
            val result = runCatching { scanRootGeneratedPackages(packageNames) }
            runOnUiThread {
                result
                    .onSuccess { generated ->
                        updateGeneratedPackageCache(generated)
                        generatedScanFailed = false
                        statusText = "已刷新生成状态: ${generated.size} 个"
                    }
                    .onFailure {
                        generatedScanFailed = true
                        statusText = "生成状态刷新失败，保留上次缓存: ${it.message ?: it.javaClass.simpleName}"
                    }
                isScanningGeneratedPackages = false
            }
        }.start()
    }

    private fun scanRootGeneratedPackages(packageNames: Set<String>): Set<String> {
        if (packageNames.isEmpty()) {
            return emptySet()
        }
        val command = """
            if [ -d ${shQuote(ROOT_UXICONS_DIR)} ]; then
                while IFS= read -r name; do
                    [ -n "${'$'}name" ] || continue
                    dir=${shQuote(ROOT_UXICONS_DIR)}/"${'$'}name"
                    [ -d "${'$'}dir" ] || continue
                    if [ -f "${'$'}dir/recbg.png" ] ||
                        [ -f "${'$'}dir/recfg.png" ] ||
                        [ -f "${'$'}dir/rec_night.png" ] ||
                        [ -f "${'$'}dir/monochrome.png" ] ||
                        ls "${'$'}dir"/*.png >/dev/null 2>&1; then
                        printf '%s\n' "${'$'}name"
                    fi
                done
            fi
        """.trimIndent()
        val input = packageNames
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .sorted()
            .joinToString(separator = "\n", postfix = "\n")
        val output = runRootCommand(command, ROOT_SCAN_TIMEOUT_MS, input)
        return output
            .lineSequence()
            .map { it.trim() }
            .filter { it in packageNames }
            .toSet()
    }

    private fun loadGeneratedPackageCache() {
        val cached = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getStringSet(PREF_GENERATED_PACKAGE_NAMES, emptySet())
            .orEmpty()
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
        generatedPackageNames = cached
        generatedScanFailed = false
        isScanningGeneratedPackages = false
    }

    private fun updateGeneratedPackageCache(packages: Set<String>) {
        val normalized = packages
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
        generatedPackageNames = normalized
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putStringSet(PREF_GENERATED_PACKAGE_NAMES, normalized)
            .putLong(PREF_GENERATED_PACKAGE_NAMES_UPDATED_AT, System.currentTimeMillis())
            .apply()
    }

    private fun markPackageGenerated(packageName: String) {
        updateGeneratedPackageCache(generatedPackageNames + packageName)
    }

    private fun loadUiState() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        selectedPackageName = prefs.getString(PREF_SELECTED_PACKAGE_NAME, null)
            ?.takeIf { it.isNotBlank() }
        generatedFilter = GeneratedFilter.fromName(prefs.getString(PREF_GENERATED_FILTER, null))
        queryText = prefs.getString(PREF_QUERY_TEXT, "") ?: ""
        advancedSettingsCategory = AdvancedSettingsCategory.fromName(
            prefs.getString(PREF_ADVANCED_SETTINGS_CATEGORY, null),
        )
        previewPackageName = prefs.getString(PREF_PREVIEW_PACKAGE_NAME, null)
            ?.takeIf { it.isNotBlank() }
        previewDirPath = prefs.getString(PREF_PREVIEW_DIR_PATH, null)
            ?.takeIf { it.isNotBlank() }
        previewSelections = PreviewSelections.fromPrefs(prefs)
        previewDesktopBackground = PreviewDesktopBackground.fromName(
            prefs.getString(PREF_PREVIEW_DESKTOP_BACKGROUND, null),
        )
        previewIconSizeDp = prefs.getInt(PREF_PREVIEW_ICON_SIZE_DP, DEFAULT_PREVIEW_ICON_SIZE_DP)
            .coerceIn(MIN_PREVIEW_ICON_SIZE_DP, MAX_PREVIEW_ICON_SIZE_DP)
    }

    private fun saveUiState() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(PREF_SELECTED_PACKAGE_NAME, selectedPackageName)
            .putString(PREF_GENERATED_FILTER, generatedFilter.name)
            .putString(PREF_QUERY_TEXT, queryText)
            .putString(PREF_ADVANCED_SETTINGS_CATEGORY, advancedSettingsCategory.name)
            .putString(PREF_PREVIEW_PACKAGE_NAME, previewPackageName)
            .putString(PREF_PREVIEW_DIR_PATH, previewDirPath)
            .putString(PREF_PREVIEW_SELECTION_NORMAL_LIGHT, previewSelections.normalLight.name)
            .putString(PREF_PREVIEW_SELECTION_NORMAL_DARK, previewSelections.normalDark.name)
            .putString(PREF_PREVIEW_SELECTION_MONOCHROME_LIGHT, previewSelections.monochromeLight.name)
            .putString(PREF_PREVIEW_SELECTION_MONOCHROME_DARK, previewSelections.monochromeDark.name)
            .putString(PREF_PREVIEW_DESKTOP_BACKGROUND, previewDesktopBackground.name)
            .putInt(PREF_PREVIEW_ICON_SIZE_DP, previewIconSizeDp)
            .apply()
    }

    private fun runRootCommand(command: String, timeoutMs: Long, stdin: String? = null): String {
        val process = ProcessBuilder("su", "-c", command)
            .redirectErrorStream(true)
            .start()
        val outputBuilder = StringBuilder()
        val outputReader = Thread {
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    outputBuilder
                        .append(line)
                        .append('\n')
                }
            }
        }.apply {
            isDaemon = true
            start()
        }
        runCatching {
            process.outputStream.bufferedWriter().use { writer ->
                if (stdin != null) {
                    writer.write(stdin)
                }
            }
        }
        val finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)
        if (!finished) {
            process.destroyForcibly()
            outputReader.join(250)
            error("su 超时")
        }
        outputReader.join(1_000)
        val output = outputBuilder.toString()
        val code = process.exitValue()
        if (code != 0) {
            val detail = output.lineSequence()
                .map { it.trim() }
                .firstOrNull { it.isNotEmpty() }
                ?.take(120)
            error(
                buildString {
                    append("su 退出码: ")
                    append(code)
                    if (detail != null) {
                        append(": ")
                        append(detail)
                    }
                },
            )
        }
        return output
    }

    private fun refreshArtPlusIcons() {
        if (isBusy || isRefreshingArtPlusIcons) {
            return
        }
        isRefreshingArtPlusIcons = true
        statusText = "正在刷新 ART+ 图标..."
        Thread {
            val result = runCatching { refreshArtPlusIconsBlocking() }
            runOnUiThread {
                result
                    .onSuccess { summary ->
                        statusText = if (summary.isBlank()) {
                            "已刷新 ART+ 图标"
                        } else {
                            "已刷新 ART+ 图标: $summary"
                        }
                    }
                    .onFailure { error ->
                        statusText = "刷新 ART+ 图标失败: ${error.message ?: error.javaClass.simpleName}"
                    }
                isRefreshingArtPlusIcons = false
            }
        }.start()
    }

    private fun refreshArtPlusIconsBlocking(): String {
        val currentConfig = Settings.System
            .getString(contentResolver, COLOROS_UX_ICON_CONFIG_KEY)
            ?.trim()
            ?.toLongOrNull()
            ?: FALLBACK_ARTPLUS_INSPIRATION_UXICON_CONFIG
        val finalTheme = currentConfig.uxIconTheme()
        val temporaryTheme = when (finalTheme) {
            COLOROS_DEFAULT_ICON_THEME -> COLOROS_INSPIRATION_ICON_THEME
            COLOROS_INSPIRATION_ICON_THEME -> COLOROS_DEFAULT_ICON_THEME
            else -> COLOROS_DEFAULT_ICON_THEME
        }
        val finalConfig = currentConfig
            .withUxIconArtPlusOn(COLOROS_ARTPLUS_ON)
        val temporaryConfig = finalConfig.withUxIconTheme(temporaryTheme)
        val apkPath = applicationInfo.sourceDir
        val command = """
            set -e
            APP_APK=${shQuote(apkPath)}
            apply_uxicon_config() {
                value="${'$'}1"
                app_process -Djava.class.path="${'$'}APP_APK" /system/bin ${UxIconConfigCli::class.java.name} "${'$'}value"
                settings put system ${shQuote(COLOROS_UX_ICON_CONFIG_KEY)} "${'$'}value"
                am broadcast -a oplus.intent.action.SKIN_CHANGED >/dev/null 2>&1 || true
            }
            apply_uxicon_config ${temporaryConfig}
            sleep 1
            apply_uxicon_config ${finalConfig}
            am start -a android.intent.action.MAIN -c android.intent.category.HOME >/dev/null 2>&1 ||
                input keyevent 3 >/dev/null 2>&1 || true
        """.trimIndent()
        return runRootCommand(command, ARTPLUS_ICON_REFRESH_TIMEOUT_MS)
            .lineSequence()
            .map { it.trim() }
            .filter { it.startsWith("mUxIconConfig=") }
            .joinToString(" -> ")
    }

    private fun Long.withUxIconTheme(theme: Int): Long =
        (this and COLOROS_UXICON_THEME_MASK.inv()) or
            (((theme.toLong()) and 0x0fL) shl COLOROS_UXICON_THEME_SHIFT)

    private fun Long.uxIconTheme(): Int =
        ((this and COLOROS_UXICON_THEME_MASK) shr COLOROS_UXICON_THEME_SHIFT).toInt()

    private fun Long.withUxIconArtPlusOn(value: Int): Long =
        (this and COLOROS_UXICON_ARTPLUS_MASK.inv()) or
            (((value.toLong()) and 0x07L) shl COLOROS_UXICON_ARTPLUS_SHIFT)

    private fun preloadAppIcons(entries: List<AppEntry>) {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
        entries.asSequence()
            .filter { it.launchable }
            .take(PRELOAD_ICON_COUNT)
            .forEach { entry ->
                if (getCachedAppIcon(entry.iconKey) != null) {
                    return@forEach
                }
                val bitmap = runCatching { loadAppIconBitmap(entry) }.getOrNull() ?: return@forEach
                synchronized(appIconCache) {
                    if (appIconCache.get(entry.iconKey) == null) {
                        appIconCache.put(entry.iconKey, bitmap)
                    }
                }
            }
    }

    private fun refreshPermissionState() {
        packageListPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            packageManager.checkPermission(Manifest.permission.QUERY_ALL_PACKAGES, packageName) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        usageAccessGranted = hasUsageAccess()
    }

    private fun requestDeclaredPermissions() {
        val permissions = mutableListOf<String>()
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            packageManager.checkPermission(Manifest.permission.QUERY_ALL_PACKAGES, packageName) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissions += Manifest.permission.QUERY_ALL_PACKAGES
        }
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun requestSpecialPermissionsOnce() {
        if (usageAccessGranted) {
            return
        }
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if (prefs.getBoolean(PREF_USAGE_PERMISSION_PROMPTED, false)) {
            return
        }
        prefs.edit().putBoolean(PREF_USAGE_PERMISSION_PROMPTED, true).apply()
        window.decorView.post {
            if (!hasUsageAccess()) {
                openUsageAccessSettings()
            }
        }
    }

    private fun openAppPermissionSettings() {
        runCatching {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", packageName, null)),
            )
        }.onFailure {
            statusText = "无法打开应用权限设置: ${it.message ?: it.javaClass.simpleName}"
        }
    }

    private fun openUsageAccessSettings() {
        runCatching {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }.onFailure {
            statusText = "无法打开使用情况访问设置: ${it.message ?: it.javaClass.simpleName}"
        }
    }

    private fun hasUsageAccess(): Boolean {
        val appOps = getSystemService(AppOpsManager::class.java) ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName,
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName,
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    @Suppress("DEPRECATION")
    private fun queryLaunchablePackages(pm: PackageManager, intent: Intent): Set<String> {
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
            )
        } else {
            pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        }
        return resolveInfos
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    @Suppress("DEPRECATION")
    private fun getInstalledApplications(pm: PackageManager): List<ApplicationInfo> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
            )
        } else {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        }

    private fun getApplicationInfoCompat(packageName: String): ApplicationInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        }

    private fun isDebugGenerateIntent(intent: Intent?): Boolean =
        intent?.getStringExtra(EXTRA_DEBUG_GENERATE_PACKAGE)?.isNotBlank() == true &&
            isDebugTokenValid(intent.getStringExtra(EXTRA_DEBUG_GENERATE_TOKEN))

    private fun handleDebugGenerateIntent(intent: Intent?) {
        if (!isDebugBuild()) {
            return
        }
        if (!isDebugTokenValid(intent?.getStringExtra(EXTRA_DEBUG_GENERATE_TOKEN))) {
            return
        }
        val debugPackageName = intent
            ?.getStringExtra(EXTRA_DEBUG_GENERATE_PACKAGE)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return
        val useGpt = intent.getBooleanExtra(EXTRA_DEBUG_GENERATE_USE_GPT, false)
        val installWithRoot = intent.getBooleanExtra(EXTRA_DEBUG_GENERATE_INSTALL_ROOT, false)
        val debugMode = LocalSeparationMode.fromValue(
            intent.getStringExtra(EXTRA_DEBUG_GENERATE_MODE),
        )
        val rootWriteMode = RootWriteMode.fromValue(
            intent.getStringExtra(EXTRA_DEBUG_GENERATE_ROOT_WRITE_MODE),
        )
        startDebugGeneration(
            packageName = debugPackageName,
            useGpt = useGpt,
            installWithRoot = installWithRoot,
            debugMode = debugMode,
            rootWriteMode = rootWriteMode,
        )
    }

    private fun startDebugGeneration(
        packageName: String,
        useGpt: Boolean,
        installWithRoot: Boolean,
        debugMode: LocalSeparationMode,
        rootWriteMode: RootWriteMode,
    ): Boolean {
        var accepted = false
        runOnMainSync {
            if (isBusy) {
                statusText = "调试生成排队失败，当前正在处理: $packageName"
            } else {
                isBusy = true
                statusText = "调试生成中: $packageName"
                accepted = true
            }
        }
        if (!accepted) {
            return false
        }
        Thread {
            try {
                val info = getApplicationInfoCompat(packageName)
                val label = runCatching { info.loadLabel(packageManager)?.toString() }
                    .getOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?: packageName
                val launchable = packageManager.getLaunchIntentForPackage(packageName) != null
                val entry = AppEntry(
                    label = label,
                    packageName = packageName,
                    applicationInfo = info,
                    launchable = launchable,
                    iconKey = "${packageName}:${info.uid}:${info.sourceDir}",
                )
                val result = generateArtPlusPackage(
                    app = entry,
                    useGpt = useGpt,
                    localModeOverride = debugMode,
                )
                if (installWithRoot) {
                    installWithRoot(result.outDir, packageName, rootWriteMode)
                    runOnMainSync {
                        markPackageGenerated(packageName)
                        statusText = "调试生成完成并${rootWriteMode.label}写入 Root，未刷新，请手动点刷新 ART+ 图标: ${result.outDir.absolutePath}"
                    }
                } else {
                    runOnMainSync {
                        statusText = "调试生成完成: ${result.outDir.absolutePath}"
                    }
                }
                runOnMainSync {
                    activeGenerationSession = result.session
                    previewSelections = result.selections
                    previewChoiceMode = null
                    previewPackageName = packageName
                    previewDirPath = result.outDir.absolutePath
                    previewVersion += 1
                    saveUiState()
                }
            } catch (error: Exception) {
                status("调试生成失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                runOnMainSync { isBusy = false }
            }
        }.start()
        return true
    }

    private fun debugInspectPackage(packageName: String, includeRmbg: Boolean): JSONObject {
        val info = getApplicationInfoCompat(packageName)
        val label = runCatching { info.loadLabel(packageManager)?.toString() }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: packageName
        val icon = info.loadIcon(packageManager)
        val base = getExternalFilesDir("ArtPlusLab") ?: File(filesDir, "ArtPlusLab")
        val outDir = File(base, packageName)
        ensureFreshDir(outDir)

        val metadata = JSONObject()
            .put("ok", true)
            .put("package", packageName)
            .put("label", label)
            .put("output_dir", outDir.absolutePath)
            .put("source_dir", info.sourceDir ?: "")
            .put("public_source_dir", info.publicSourceDir ?: "")
            .put("is_adaptive", icon is AdaptiveIconDrawable)
            .put(
                "settings",
                JSONObject()
                    .put("foreground_subject_percent", foregroundSubjectPercent)
                    .put("background_separation_percent", backgroundSeparationPercent)
                    .put("plate_removal_percent", plateRemovalPercent)
                    .put("shadow_removal_percent", shadowRemovalPercent)
                    .put("edge_polish_percent", edgePolishPercent)
                    .put("adaptive_foreground_mode", adaptiveForegroundMode.value)
                    .put("original_foreground_cleanup_mode", originalForegroundCleanupMode.value),
            )

        fun saveLayer(name: String, bitmap: Bitmap, metrics: JSONObject = metadata): Bitmap {
            savePng(bitmap, File(outDir, "$name.png"))
            metrics.put(name, bitmapStatsJson(bitmap))
            return bitmap
        }

        val source240 = saveLayer("source_icon_240_opaque", drawDrawable(icon, SIZE_1X1, SIZE_1X1, transparent = false))
        val candidateSource240 = saveLayer(
            "source_icon_240_candidate",
            drawLocalCandidateSourceIcon(icon, SIZE_1X1, SIZE_1X1),
        )
        saveLayer("source_icon_240_transparent", drawDrawable(icon, SIZE_1X1, SIZE_1X1, transparent = true))

        if (icon is AdaptiveIconDrawable) {
            val renderSize = SIZE_1X1 * LOCAL_ICON_RENDER_SCALE
            val background = drawDrawable(
                icon.background ?: ColorDrawable(AndroidColor.WHITE),
                renderSize,
                renderSize,
                transparent = false,
            )
            val direct = drawDrawable(icon.foreground, renderSize, renderSize, transparent = true)
            val composed = drawDrawable(icon, renderSize, renderSize, transparent = true)
            val subtracted = subtractBackground(composed, background)
            val selection = chooseBetterAdaptiveForeground(subtracted, direct, background)
            val chosen = selection.bitmap
            val adaptiveJson = JSONObject()
            saveLayer("adaptive_background_240", resizeBitmap(background, SIZE_1X1, SIZE_1X1), adaptiveJson)
            saveLayer("adaptive_composed_240", resizeBitmap(composed, SIZE_1X1, SIZE_1X1), adaptiveJson)
            saveLayer("adaptive_subtracted_foreground_240", resizeBitmap(subtracted, SIZE_1X1, SIZE_1X1), adaptiveJson)
            saveLayer("adaptive_direct_foreground_240", resizeBitmap(direct, SIZE_1X1, SIZE_1X1), adaptiveJson)
            saveLayer("adaptive_chosen_foreground_240", resizeBitmap(chosen, SIZE_1X1, SIZE_1X1), adaptiveJson)
            adaptiveJson
                .put("subtracted_has_mask_artifact", hasAdaptiveMaskArtifact(subtracted))
                .put("direct_usable", isUsableDirectAdaptiveForeground(direct, alphaCoverage(subtracted)))
                .put("subtracted_coverage", alphaCoverage(subtracted))
                .put("direct_coverage", alphaCoverage(direct))
                .put("chosen_preserve_geometry", selection.preserveGeometry)
            metadata.put("adaptive", adaptiveJson)
        }

        val localSource = buildLocalIconLayers(icon)
        val localJson = JSONObject()
        saveLayer("local_base_recbg", localSource.recbg, localJson)
        saveLayer("local_base_recfg", localSource.recfg, localJson)
        localSource.monochrome?.let { saveLayer("local_base_monochrome", it, localJson) }
        val candidateSet = buildLocalCandidates(localSource, candidateSource240)
        localJson.put("auto_choice", candidateSet.autoChoice.name.lowercase(Locale.US))
        metadata.put("local", localJson)

        val candidatesJson = JSONObject()
        candidateSet.candidates.forEach { (choice, candidate) ->
            val key = choice.name.lowercase(Locale.US)
            val candidateJson = JSONObject()
                .put("label", choice.label)
                .put("preserve_geometry", candidate.preserveGeometry)
            saveLayer("candidate_${key}_raw", candidate.recfgRaw, candidateJson)
            val rendered = renderCandidateForeground(candidate)
            saveLayer("candidate_${key}_rendered", rendered, candidateJson)
            saveLayer("candidate_${key}_night", nightForeground(rendered, candidate.recbg), candidateJson)
            saveLayer("candidate_${key}_monochrome_light", monochromeForCandidate(candidate, invertLuma = true), candidateJson)
            saveLayer("candidate_${key}_monochrome_dark", monochromeForCandidate(candidate, invertLuma = false), candidateJson)
            candidatesJson.put(key, candidateJson)
        }

        if (includeRmbg) {
            val rmbgJson = JSONObject()
            try {
                val rmbgDebug = buildRmbgDebugCandidate(source240)
                val rmbgCandidate = rmbgDebug.result?.candidate
                val validationWarning = rmbgDebug.result?.validationWarning
                rmbgJson
                    .put("coverage", rmbgDebug.coverage)
                    .put("manual_usable", rmbgDebug.manualUsable)
                    .put("auto_usable", rmbgDebug.result?.autoUsable ?: false)
                    .put("bounds", rmbgDebug.boundsText)
                    .put("crop_risk", rmbgDebug.cropRisk)
                    .put("backend", rmbgDebug.inference.actualBackend.value)
                    .put("elapsed_ms", rmbgDebug.inference.elapsedMs)
                saveLayer("candidate_rmbg_raw", rmbgDebug.foreground, rmbgJson)
                val rendered = renderCandidateForeground(
                    rmbgCandidate ?: IconCandidate(
                        recfgRaw = rmbgDebug.foreground,
                        recbg = localSource.recbg,
                        monochromeRaw = rmbgDebug.foreground,
                    ),
                )
                saveLayer("candidate_rmbg_rendered", rendered, rmbgJson)
                saveLayer("candidate_rmbg_night", nightForeground(rendered, localSource.recbg), rmbgJson)
                saveLayer("candidate_rmbg_monochrome_light", monochromeForCandidate(
                    rmbgCandidate ?: IconCandidate(rmbgDebug.foreground, localSource.recbg, monochromeRaw = rmbgDebug.foreground),
                    invertLuma = true,
                ), rmbgJson)
                saveLayer("candidate_rmbg_monochrome_dark", monochromeForCandidate(
                    rmbgCandidate ?: IconCandidate(rmbgDebug.foreground, localSource.recbg, monochromeRaw = rmbgDebug.foreground),
                    invertLuma = false,
                ), rmbgJson)
                if (validationWarning != null) {
                    rmbgJson.put("validation_warning", validationWarning)
                }
                rmbgJson.put("ok", true)
                runOnMainSync {
                    lastRmbgInferenceReport = rmbgDebug.inference
                    lastRmbgCandidateError = null
                }
            } catch (error: Throwable) {
                val message = describeRmbgFailure(error)
                rmbgJson
                    .put("ok", false)
                    .put("error", message)
                runOnMainSync {
                    lastRmbgCandidateError = message
                    lastRmbgInferenceReport = null
                }
            }
            candidatesJson.put("rmbg", rmbgJson)
        }

        metadata.put("candidates", candidatesJson)
        FileOutputStream(File(outDir, "metadata.json")).use { output ->
            output.write(metadata.toString(2).toByteArray(Charsets.UTF_8))
        }
        return metadata
    }

    private data class RmbgDebugCandidate(
        val foreground: Bitmap,
        val result: CandidateBuildResult?,
        val coverage: Double,
        val boundsText: String,
        val cropRisk: Boolean,
        val manualUsable: Boolean,
        val inference: RmbgInferenceReport,
    )

    private fun loadGptSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        gptImageMode = GptImageMode.fromValue(prefs.getString(PREF_GPT_MODE, GptImageMode.Responses.value))
        gptPromptPreset = GptPromptPreset.fromValue(
            prefs.getString(PREF_GPT_PROMPT_PRESET, GptPromptPreset.StableCutout.value),
        )
        gptCustomPrompt = prefs.getString(PREF_GPT_CUSTOM_PROMPT, "") ?: ""
        val storedBaseUrl = prefs.getString(PREF_GPT_BASE_URL, "") ?: ""
        gptBaseUrl = if (storedBaseUrl == LEGACY_DEFAULT_GPT_BASE_URL) "" else storedBaseUrl
        gptApiKey = loadGptApiKey(prefs)
        if (storedBaseUrl == LEGACY_DEFAULT_GPT_BASE_URL) {
            prefs.edit().putString(PREF_GPT_BASE_URL, "").apply()
        }
    }

    private fun saveGptSettings(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val encryptedKey = encryptSecret(gptApiKey.trim())
        return prefs
            .edit()
            .putString(PREF_GPT_MODE, gptImageMode.value)
            .putString(PREF_GPT_PROMPT_PRESET, gptPromptPreset.value)
            .putString(PREF_GPT_CUSTOM_PROMPT, gptCustomPrompt.trim())
            .putString(PREF_GPT_BASE_URL, gptBaseUrl.trim())
            .remove(PREF_GPT_API_KEY)
            .apply {
                if (encryptedKey.isBlank()) {
                    remove(PREF_GPT_API_KEY_ENCRYPTED)
                } else {
                    putString(PREF_GPT_API_KEY_ENCRYPTED, encryptedKey)
                }
            }
            .commit()
    }

    private fun saveSettingsPage() {
        val gptSaved = runCatching { saveGptSettings() }.getOrDefault(false)
        val rmbgSaved = runCatching { saveRmbgSettings() }.getOrDefault(false)
        saveLocalSeparationSettings()
        saveImageTuningSettings()
        saveLiquidGlassSettings()
        saveUiState()
        gptSettingsSaveStatus = ""
        rmbgComponentSaveStatus = ""
        statusText = if (gptSaved && rmbgSaved) "设置已保存" else "设置保存失败"
    }

    private fun loadGptApiKey(prefs: android.content.SharedPreferences): String {
        val encrypted = prefs.getString(PREF_GPT_API_KEY_ENCRYPTED, null)
        val decrypted = encrypted
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { decryptSecret(it) }.getOrNull() }
        if (decrypted != null) {
            if (prefs.contains(PREF_GPT_API_KEY)) {
                prefs.edit().remove(PREF_GPT_API_KEY).apply()
            }
            return decrypted
        }
        val legacyPlain = prefs.getString(PREF_GPT_API_KEY, "") ?: ""
        if (legacyPlain.isNotBlank()) {
            val migrated = encryptSecret(legacyPlain)
            prefs.edit()
                .remove(PREF_GPT_API_KEY)
                .putString(PREF_GPT_API_KEY_ENCRYPTED, migrated)
                .apply()
        }
        return legacyPlain
    }

    private fun encryptSecret(value: String): String {
        if (value.isBlank()) {
            return ""
        }
        val cipher = Cipher.getInstance(KEYSTORE_CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, gptSecretKey())
        val encrypted = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        return listOf(cipher.iv, encrypted)
            .joinToString(":") { Base64.encodeToString(it, Base64.NO_WRAP) }
    }

    private fun decryptSecret(value: String): String {
        val parts = value.split(':')
        if (parts.size != 2) {
            error("invalid encrypted secret")
        }
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val encrypted = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(KEYSTORE_CIPHER_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, gptSecretKey(), GCMParameterSpec(KEYSTORE_GCM_TAG_BITS, iv))
        return String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
    }

    private fun gptSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEYSTORE_GPT_KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEYSTORE_GPT_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return keyGenerator.generateKey()
    }

    private fun loadRmbgSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        rmbgComponentUrl = prefs.getString(PREF_RMBG_COMPONENT_URL, DEFAULT_RMBG_COMPONENT_URL)
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_RMBG_COMPONENT_URL
        val storedInputSize = prefs.getInt(PREF_RMBG_INPUT_SIZE, DEFAULT_RMBG_INPUT_SIZE)
        if (
            !prefs.getBoolean(PREF_RMBG_INPUT_SIZE_MIGRATED_TO_1024, false) ||
            storedInputSize != DEFAULT_RMBG_INPUT_SIZE
        ) {
            prefs.edit()
                .putInt(PREF_RMBG_INPUT_SIZE, DEFAULT_RMBG_INPUT_SIZE)
                .putBoolean(PREF_RMBG_INPUT_SIZE_MIGRATED_TO_1024, true)
                .apply()
        }
    }

    private fun saveRmbgSettings(): Boolean =
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(PREF_RMBG_COMPONENT_URL, rmbgComponentUrl.trim())
            .putInt(PREF_RMBG_INPUT_SIZE, DEFAULT_RMBG_INPUT_SIZE)
            .commit()

    private fun loadLocalSeparationSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        localSeparationMode = LocalSeparationMode.fromValue(
            prefs.getString(PREF_LOCAL_SEPARATION_MODE, LocalSeparationMode.Auto.value),
        )
    }

    private fun saveLocalSeparationSettings() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(PREF_LOCAL_SEPARATION_MODE, localSeparationMode.value)
            .apply()
    }

    private fun updateLocalSeparationMode(mode: LocalSeparationMode) {
        if (localSeparationMode == mode) {
            return
        }
        val session = activeGenerationSession
        val previousDefault = session?.let {
            defaultPreviewChoiceForMode(localSeparationMode, it.autoLocalChoice)
        }
        localSeparationMode = mode
        saveLocalSeparationSettings()
        refreshActivePreviewOutputs(
            rebuildLocalCandidates = true,
            retargetFrom = previousDefault,
        )
    }

    private fun loadImageSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val storedValue = prefs.getInt(
            PREF_FOREGROUND_SUBJECT_PERCENT,
            DEFAULT_FOREGROUND_SUBJECT_PERCENT,
        )
        foregroundSubjectPercent = if (storedValue == LEGACY_FOREGROUND_SUBJECT_PERCENT) {
            DEFAULT_FOREGROUND_SUBJECT_PERCENT
        } else {
            storedValue.coerceIn(MIN_FOREGROUND_SUBJECT_PERCENT, MAX_FOREGROUND_SUBJECT_PERCENT)
        }
        foregroundShadowLevel = prefs.getInt(
            PREF_FOREGROUND_SHADOW_LEVEL,
            DEFAULT_FOREGROUND_SHADOW_LEVEL,
        ).coerceIn(MIN_FOREGROUND_SHADOW_LEVEL, MAX_FOREGROUND_SHADOW_LEVEL)
        draftForegroundShadowLevelText = foregroundShadowLevel.toString()
        monochromeThemeScale = prefs.getFloat(
            PREF_MONOCHROME_THEME_SCALE,
            DEFAULT_MONOCHROME_THEME_SCALE,
        ).coerceIn(MIN_MONOCHROME_THEME_SCALE, MAX_MONOCHROME_THEME_SCALE)
        draftMonochromeThemeScaleText = (monochromeThemeScale * 100).roundToInt().toString()
        val tuningVersion = prefs.getInt(PREF_IMAGE_TUNING_VERSION, 1)
        backgroundSeparationPercent = if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
            DEFAULT_BACKGROUND_SEPARATION_PERCENT
        } else {
            prefs.getInt(PREF_BACKGROUND_SEPARATION_PERCENT, DEFAULT_BACKGROUND_SEPARATION_PERCENT)
                .let { migrateLegacyPercent(it, DEFAULT_BACKGROUND_SEPARATION_PERCENT) }
        }.coerceIn(MIN_BACKGROUND_SEPARATION_PERCENT, MAX_BACKGROUND_SEPARATION_PERCENT)
        draftBackgroundSeparationText = backgroundSeparationPercent.toString()
        plateRemovalPercent = if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
            DEFAULT_PLATE_REMOVAL_PERCENT
        } else {
            prefs.getInt(PREF_PLATE_REMOVAL_PERCENT, DEFAULT_PLATE_REMOVAL_PERCENT)
                .let { migrateLegacyPercent(it, DEFAULT_PLATE_REMOVAL_PERCENT) }
        }.coerceIn(MIN_PLATE_REMOVAL_PERCENT, MAX_PLATE_REMOVAL_PERCENT)
        draftPlateRemovalText = plateRemovalPercent.toString()
        shadowRemovalPercent = if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
            DEFAULT_SHADOW_REMOVAL_PERCENT
        } else {
            prefs.getInt(PREF_SHADOW_REMOVAL_PERCENT, DEFAULT_SHADOW_REMOVAL_PERCENT)
                .let { migrateLegacyPercent(it, DEFAULT_SHADOW_REMOVAL_PERCENT) }
        }.coerceIn(MIN_SHADOW_REMOVAL_PERCENT, MAX_SHADOW_REMOVAL_PERCENT)
        draftShadowRemovalText = shadowRemovalPercent.toString()
        edgePolishPercent = if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
            DEFAULT_EDGE_POLISH_PERCENT
        } else {
            prefs.getInt(PREF_EDGE_POLISH_PERCENT, DEFAULT_EDGE_POLISH_PERCENT)
        }.coerceIn(MIN_EDGE_POLISH_PERCENT, MAX_EDGE_POLISH_PERCENT)
        draftEdgePolishText = edgePolishPercent.toString()
        rmbgAlphaStrengthPercent = prefs.getInt(
            PREF_RMBG_ALPHA_STRENGTH_PERCENT,
            DEFAULT_RMBG_ALPHA_STRENGTH_PERCENT,
        ).coerceIn(MIN_RMBG_ALPHA_STRENGTH_PERCENT, MAX_RMBG_ALPHA_STRENGTH_PERCENT)
        draftRmbgAlphaStrengthText = rmbgAlphaStrengthPercent.toString()
        rmbgEdgeFeatherPercent = prefs.getInt(
            PREF_RMBG_EDGE_FEATHER_PERCENT,
            DEFAULT_RMBG_EDGE_FEATHER_PERCENT,
        ).coerceIn(MIN_RMBG_EDGE_FEATHER_PERCENT, MAX_RMBG_EDGE_FEATHER_PERCENT)
        draftRmbgEdgeFeatherText = rmbgEdgeFeatherPercent.toString()
        rmbgEdgeAdjustPercent = prefs.getInt(
            PREF_RMBG_EDGE_ADJUST_PERCENT,
            DEFAULT_RMBG_EDGE_ADJUST_PERCENT,
        ).coerceIn(MIN_RMBG_EDGE_ADJUST_PERCENT, MAX_RMBG_EDGE_ADJUST_PERCENT)
        draftRmbgEdgeAdjustText = rmbgEdgeAdjustPercent.toString()
        rmbgWeakAlphaKeepPercent = prefs.getInt(
            PREF_RMBG_WEAK_ALPHA_KEEP_PERCENT,
            DEFAULT_RMBG_WEAK_ALPHA_KEEP_PERCENT,
        ).coerceIn(MIN_RMBG_WEAK_ALPHA_KEEP_PERCENT, MAX_RMBG_WEAK_ALPHA_KEEP_PERCENT)
        draftRmbgWeakAlphaKeepText = rmbgWeakAlphaKeepPercent.toString()
        adaptiveForegroundMode = if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
            AdaptiveForegroundMode.Auto
        } else {
            AdaptiveForegroundMode.fromValue(
                prefs.getString(PREF_ADAPTIVE_FOREGROUND_MODE, AdaptiveForegroundMode.Auto.value),
            )
        }
        adaptiveDirectMaxCoveragePercent = prefs.getInt(
            PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT,
            DEFAULT_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT,
        ).coerceIn(MIN_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT, MAX_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT)
        adaptiveDirectMaxCoverageIncreasePercent = prefs.getInt(
            PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
            DEFAULT_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
        ).coerceIn(
            MIN_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
            MAX_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
        )
        adaptiveMaskEdgeCoveragePercent = prefs.getInt(
            PREF_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT,
            DEFAULT_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT,
        ).coerceIn(MIN_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT, MAX_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT)
        adaptiveMaskMinCoveragePercent = prefs.getInt(
            PREF_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT,
            DEFAULT_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT,
        ).coerceIn(MIN_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT, MAX_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT)
        adaptiveCenterEpsilonPercent = prefs.getInt(
            PREF_ADAPTIVE_CENTER_EPSILON_PERCENT,
            DEFAULT_ADAPTIVE_CENTER_EPSILON_PERCENT,
        ).coerceIn(MIN_ADAPTIVE_CENTER_EPSILON_PERCENT, MAX_ADAPTIVE_CENTER_EPSILON_PERCENT)
        originalForegroundCleanupMode = if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
            OriginalForegroundCleanupMode.Auto
        } else {
            OriginalForegroundCleanupMode.fromValue(
                prefs.getString(PREF_ORIGINAL_FOREGROUND_CLEANUP_MODE, OriginalForegroundCleanupMode.Auto.value),
            )
        }
        nightSubjectLightBackgroundEnabled = prefs.getBoolean(
            PREF_NIGHT_SUBJECT_LIGHT_BACKGROUND_ENABLED,
            false,
        )
        prefs.edit()
            .putInt(PREF_FOREGROUND_SUBJECT_PERCENT, foregroundSubjectPercent)
            .putInt(PREF_FOREGROUND_SHADOW_LEVEL, foregroundShadowLevel)
            .putFloat(PREF_MONOCHROME_THEME_SCALE, monochromeThemeScale)
            .putInt(PREF_BACKGROUND_SEPARATION_PERCENT, backgroundSeparationPercent)
            .putInt(PREF_PLATE_REMOVAL_PERCENT, plateRemovalPercent)
            .putInt(PREF_SHADOW_REMOVAL_PERCENT, shadowRemovalPercent)
            .putInt(PREF_EDGE_POLISH_PERCENT, edgePolishPercent)
            .putInt(PREF_RMBG_ALPHA_STRENGTH_PERCENT, rmbgAlphaStrengthPercent)
            .putInt(PREF_RMBG_EDGE_FEATHER_PERCENT, rmbgEdgeFeatherPercent)
            .putInt(PREF_RMBG_EDGE_ADJUST_PERCENT, rmbgEdgeAdjustPercent)
            .putInt(PREF_RMBG_WEAK_ALPHA_KEEP_PERCENT, rmbgWeakAlphaKeepPercent)
            .putString(PREF_ADAPTIVE_FOREGROUND_MODE, adaptiveForegroundMode.value)
            .putInt(PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT, adaptiveDirectMaxCoveragePercent)
            .putInt(PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT, adaptiveDirectMaxCoverageIncreasePercent)
            .putInt(PREF_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT, adaptiveMaskEdgeCoveragePercent)
            .putInt(PREF_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT, adaptiveMaskMinCoveragePercent)
            .putInt(PREF_ADAPTIVE_CENTER_EPSILON_PERCENT, adaptiveCenterEpsilonPercent)
            .putString(PREF_ORIGINAL_FOREGROUND_CLEANUP_MODE, originalForegroundCleanupMode.value)
            .putBoolean(PREF_NIGHT_SUBJECT_LIGHT_BACKGROUND_ENABLED, nightSubjectLightBackgroundEnabled)
            .putInt(PREF_IMAGE_TUNING_VERSION, CURRENT_IMAGE_TUNING_VERSION)
            .putBoolean(PREF_FOREGROUND_SUBJECT_PERCENT_MIGRATED, true)
            .apply()
    }

    private fun updateForegroundSubjectPercent(value: Int) {
        foregroundSubjectPercent = value.coerceIn(
            MIN_FOREGROUND_SUBJECT_PERCENT,
            MAX_FOREGROUND_SUBJECT_PERCENT,
        )
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putInt(PREF_FOREGROUND_SUBJECT_PERCENT, foregroundSubjectPercent)
            .apply()
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updateForegroundShadowLevel(value: Int) {
        foregroundShadowLevel = value.coerceIn(
            MIN_FOREGROUND_SHADOW_LEVEL,
            MAX_FOREGROUND_SHADOW_LEVEL,
        )
        draftForegroundShadowLevelText = foregroundShadowLevel.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun migrateLegacyPercent(value: Int, fallback: Int): Int =
        when {
            value in 1..100 -> value
            value <= 0 -> 1
            else -> fallback
        }

    private fun updateMonochromeThemeScalePercent(value: Int) {
        val percent = value.coerceIn(MIN_MONOCHROME_THEME_SCALE_PERCENT, MAX_MONOCHROME_THEME_SCALE_PERCENT)
        monochromeThemeScale = (percent.toFloat() / 100f).coerceIn(
            MIN_MONOCHROME_THEME_SCALE,
            MAX_MONOCHROME_THEME_SCALE,
        )
        draftMonochromeThemeScaleText = percent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updateBackgroundSeparationPercent(value: Int) {
        backgroundSeparationPercent = value.coerceIn(
            MIN_BACKGROUND_SEPARATION_PERCENT,
            MAX_BACKGROUND_SEPARATION_PERCENT,
        )
        draftBackgroundSeparationText = backgroundSeparationPercent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = true)
    }

    private fun updatePlateRemovalPercent(value: Int) {
        plateRemovalPercent = value.coerceIn(
            MIN_PLATE_REMOVAL_PERCENT,
            MAX_PLATE_REMOVAL_PERCENT,
        )
        draftPlateRemovalText = plateRemovalPercent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = true)
    }

    private fun updateShadowRemovalPercent(value: Int) {
        shadowRemovalPercent = value.coerceIn(
            MIN_SHADOW_REMOVAL_PERCENT,
            MAX_SHADOW_REMOVAL_PERCENT,
        )
        draftShadowRemovalText = shadowRemovalPercent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = true)
    }

    private fun updateEdgePolishPercent(value: Int) {
        edgePolishPercent = value.coerceIn(
            MIN_EDGE_POLISH_PERCENT,
            MAX_EDGE_POLISH_PERCENT,
        )
        draftEdgePolishText = edgePolishPercent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updateRmbgAlphaStrengthPercent(value: Int) {
        rmbgAlphaStrengthPercent = value.coerceIn(
            MIN_RMBG_ALPHA_STRENGTH_PERCENT,
            MAX_RMBG_ALPHA_STRENGTH_PERCENT,
        )
        draftRmbgAlphaStrengthText = rmbgAlphaStrengthPercent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updateRmbgEdgeFeatherPercent(value: Int) {
        rmbgEdgeFeatherPercent = value.coerceIn(
            MIN_RMBG_EDGE_FEATHER_PERCENT,
            MAX_RMBG_EDGE_FEATHER_PERCENT,
        )
        draftRmbgEdgeFeatherText = rmbgEdgeFeatherPercent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updateRmbgEdgeAdjustPercent(value: Int) {
        rmbgEdgeAdjustPercent = value.coerceIn(
            MIN_RMBG_EDGE_ADJUST_PERCENT,
            MAX_RMBG_EDGE_ADJUST_PERCENT,
        )
        draftRmbgEdgeAdjustText = rmbgEdgeAdjustPercent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updateRmbgWeakAlphaKeepPercent(value: Int) {
        rmbgWeakAlphaKeepPercent = value.coerceIn(
            MIN_RMBG_WEAK_ALPHA_KEEP_PERCENT,
            MAX_RMBG_WEAK_ALPHA_KEEP_PERCENT,
        )
        draftRmbgWeakAlphaKeepText = rmbgWeakAlphaKeepPercent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun currentRmbgModelPreset(): RmbgModelPreset {
        val url = rmbgComponentUrl.trim()
        return RMBG_MODEL_PRESETS.firstOrNull { preset ->
            preset.url.isNotBlank() && preset.url == url
        } ?: RMBG_MODEL_PRESET_CUSTOM
    }

    private fun updateRmbgModelPreset(preset: RmbgModelPreset) {
        if (preset == RMBG_MODEL_PRESET_CUSTOM) {
            rmbgComponentSaveStatus = ""
            statusText = "RMBG 使用自定义 URL"
            return
        }
        if (preset.url.isBlank()) {
            rmbgComponentSaveStatus = "该预设缺少 URL"
            statusText = "RMBG ${preset.label} 还没有下载地址"
            return
        }
        rmbgComponentUrl = preset.url
        rmbgComponentSaveStatus = ""
        statusText = "RMBG 预设已选择: ${preset.label}"
    }

    private fun rmbgInferenceStatusSummary(): String {
        if (isGeneratingRmbgCandidate) {
            return rmbgCandidateStatusText.ifBlank { "RMBG运行中" }
        }
        val report = lastRmbgInferenceReport
        if (report != null) {
            return "${report.actualBackend.label}，耗时 ${report.elapsedMs}ms"
        }
        return "尚未运行"
    }

    private fun saveImageTuningSettings() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putInt(PREF_FOREGROUND_SUBJECT_PERCENT, foregroundSubjectPercent)
            .putInt(PREF_FOREGROUND_SHADOW_LEVEL, foregroundShadowLevel)
            .putFloat(PREF_MONOCHROME_THEME_SCALE, monochromeThemeScale)
            .putInt(PREF_BACKGROUND_SEPARATION_PERCENT, backgroundSeparationPercent)
            .putInt(PREF_PLATE_REMOVAL_PERCENT, plateRemovalPercent)
            .putInt(PREF_SHADOW_REMOVAL_PERCENT, shadowRemovalPercent)
            .putInt(PREF_EDGE_POLISH_PERCENT, edgePolishPercent)
            .putInt(PREF_RMBG_ALPHA_STRENGTH_PERCENT, rmbgAlphaStrengthPercent)
            .putInt(PREF_RMBG_EDGE_FEATHER_PERCENT, rmbgEdgeFeatherPercent)
            .putInt(PREF_RMBG_EDGE_ADJUST_PERCENT, rmbgEdgeAdjustPercent)
            .putInt(PREF_RMBG_WEAK_ALPHA_KEEP_PERCENT, rmbgWeakAlphaKeepPercent)
            .putString(PREF_ADAPTIVE_FOREGROUND_MODE, adaptiveForegroundMode.value)
            .putInt(PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT, adaptiveDirectMaxCoveragePercent)
            .putInt(PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT, adaptiveDirectMaxCoverageIncreasePercent)
            .putInt(PREF_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT, adaptiveMaskEdgeCoveragePercent)
            .putInt(PREF_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT, adaptiveMaskMinCoveragePercent)
            .putInt(PREF_ADAPTIVE_CENTER_EPSILON_PERCENT, adaptiveCenterEpsilonPercent)
            .putString(PREF_ORIGINAL_FOREGROUND_CLEANUP_MODE, originalForegroundCleanupMode.value)
            .putBoolean(PREF_NIGHT_SUBJECT_LIGHT_BACKGROUND_ENABLED, nightSubjectLightBackgroundEnabled)
            .putInt(PREF_IMAGE_TUNING_VERSION, CURRENT_IMAGE_TUNING_VERSION)
            .apply()
    }

    private fun updateNightSubjectLightBackgroundEnabled(enabled: Boolean) {
        if (nightSubjectLightBackgroundEnabled == enabled) {
            return
        }
        nightSubjectLightBackgroundEnabled = enabled
        saveImageTuningSettings()
        statusText = if (enabled) {
            "正常暗色已开启填充背景色"
        } else {
            "正常暗色已关闭填充背景色"
        }
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updatePreviewIconSizeDp(value: Int) {
        val next = value.coerceIn(MIN_PREVIEW_ICON_SIZE_DP, MAX_PREVIEW_ICON_SIZE_DP)
        previewIconSizeDp = next
        saveUiState()
    }

    private fun updatePreviewDesktopBackground(option: PreviewDesktopBackground) {
        if (previewDesktopBackground == option) {
            return
        }
        previewDesktopBackground = option
        saveUiState()
    }

    private fun loadLiquidGlassSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val migratedToLayered = prefs.getBoolean(PREF_LIQUID_GLASS_LAYERED_MIGRATED, false)
        liquidGlassEnabled = if (migratedToLayered) {
            prefs.getBoolean(PREF_LIQUID_GLASS_ENABLED, true)
        } else {
            true
        }
        liquidGlassRadius = prefs.getInt(
            PREF_LIQUID_GLASS_RADIUS,
            DEFAULT_LIQUID_GLASS_RADIUS,
        ).coerceIn(MIN_LIQUID_GLASS_RADIUS, MAX_LIQUID_GLASS_RADIUS)
        draftLiquidGlassRadiusText = liquidGlassRadius.toString()
        liquidGlassOuterWidth = prefs.getInt(
            PREF_LIQUID_GLASS_OUTER_WIDTH,
            prefs.getInt(PREF_LIQUID_GLASS_BACKGROUND_LEVEL_LEGACY, DEFAULT_LIQUID_GLASS_OUTER_WIDTH),
        ).coerceIn(MIN_LIQUID_GLASS_OUTER_WIDTH, MAX_LIQUID_GLASS_OUTER_WIDTH)
        draftLiquidGlassOuterWidthText = liquidGlassOuterWidth.toString()
        liquidGlassTopAlpha = prefs.getInt(
            PREF_LIQUID_GLASS_TOP_ALPHA,
            DEFAULT_LIQUID_GLASS_TOP_ALPHA,
        ).coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)
        draftLiquidGlassTopAlphaText = liquidGlassTopAlpha.toString()
        liquidGlassBottomAlpha = prefs.getInt(
            PREF_LIQUID_GLASS_BOTTOM_ALPHA,
            DEFAULT_LIQUID_GLASS_BOTTOM_ALPHA,
        ).coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)
        draftLiquidGlassBottomAlphaText = liquidGlassBottomAlpha.toString()
        liquidGlassBackgroundMistAlpha = prefs.getInt(
            PREF_LIQUID_GLASS_BACKGROUND_MIST_ALPHA,
            DEFAULT_LIQUID_GLASS_BACKGROUND_MIST_ALPHA,
        ).coerceIn(MIN_LIQUID_GLASS_MIST_ALPHA, MAX_LIQUID_GLASS_MIST_ALPHA)
        draftLiquidGlassBackgroundMistAlphaText = liquidGlassBackgroundMistAlpha.toString()
        liquidGlassBottomDarkAlpha = prefs.getInt(
            PREF_LIQUID_GLASS_BOTTOM_DARK_ALPHA,
            DEFAULT_LIQUID_GLASS_BOTTOM_DARK_ALPHA,
        ).coerceIn(MIN_LIQUID_GLASS_BOTTOM_DARK_ALPHA, MAX_LIQUID_GLASS_BOTTOM_DARK_ALPHA)
        draftLiquidGlassBottomDarkAlphaText = liquidGlassBottomDarkAlpha.toString()
        liquidGlassSubjectScalePercent = prefs.getInt(
            PREF_LIQUID_GLASS_SUBJECT_SCALE_PERCENT,
            DEFAULT_LIQUID_GLASS_SUBJECT_SCALE_PERCENT,
        ).coerceIn(MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT, MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT)
        draftLiquidGlassSubjectScaleText = liquidGlassSubjectScalePercent.toString()
        liquidGlassSubjectOutlineWidth = prefs.getInt(
            PREF_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
            DEFAULT_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
        ).coerceIn(MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH, MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH)
        draftLiquidGlassSubjectOutlineWidthText = liquidGlassSubjectOutlineWidth.toString()
        liquidGlassSubjectInnerOutlineWidth = prefs.getInt(
            PREF_LIQUID_GLASS_SUBJECT_INNER_OUTLINE_WIDTH,
            DEFAULT_LIQUID_GLASS_SUBJECT_INNER_OUTLINE_WIDTH,
        ).coerceIn(MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH, MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH)
        draftLiquidGlassSubjectInnerOutlineWidthText = liquidGlassSubjectInnerOutlineWidth.toString()
        liquidGlassSubjectShadowAlpha = prefs.getInt(
            PREF_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA,
            DEFAULT_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA,
        ).coerceIn(MIN_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA, MAX_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA)
        draftLiquidGlassSubjectShadowAlphaText = liquidGlassSubjectShadowAlpha.toString()
        liquidGlassSubjectOpacityPercent = prefs.getInt(
            PREF_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
            DEFAULT_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
        ).coerceIn(MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT, MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT)
        draftLiquidGlassSubjectOpacityText = liquidGlassSubjectOpacityPercent.toString()
        if (!migratedToLayered) {
            prefs.edit()
                .putLiquidGlassSettings()
                .apply()
        }
    }

    private fun saveLiquidGlassSettings() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putLiquidGlassSettings()
            .apply()
    }

    private fun SharedPreferences.Editor.putLiquidGlassSettings(): SharedPreferences.Editor =
        putBoolean(PREF_LIQUID_GLASS_LAYERED_MIGRATED, true)
            .putBoolean(PREF_LIQUID_GLASS_ENABLED, liquidGlassEnabled)
            .putInt(PREF_LIQUID_GLASS_RADIUS, liquidGlassRadius)
            .putInt(PREF_LIQUID_GLASS_OUTER_WIDTH, liquidGlassOuterWidth)
            .putInt(PREF_LIQUID_GLASS_TOP_ALPHA, liquidGlassTopAlpha)
            .putInt(PREF_LIQUID_GLASS_BOTTOM_ALPHA, liquidGlassBottomAlpha)
            .putInt(PREF_LIQUID_GLASS_BACKGROUND_MIST_ALPHA, liquidGlassBackgroundMistAlpha)
            .putInt(PREF_LIQUID_GLASS_BOTTOM_DARK_ALPHA, liquidGlassBottomDarkAlpha)
            .putInt(PREF_LIQUID_GLASS_SUBJECT_SCALE_PERCENT, liquidGlassSubjectScalePercent)
            .putInt(PREF_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH, liquidGlassSubjectOutlineWidth)
            .putInt(PREF_LIQUID_GLASS_SUBJECT_INNER_OUTLINE_WIDTH, liquidGlassSubjectInnerOutlineWidth)
            .putInt(PREF_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA, liquidGlassSubjectShadowAlpha)
            .putInt(PREF_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT, liquidGlassSubjectOpacityPercent)

    private fun updateLiquidGlassEnabled(enabled: Boolean) {
        if (liquidGlassEnabled == enabled) {
            return
        }
        liquidGlassEnabled = enabled
        saveLiquidGlassSettings()
        statusText = if (enabled) "液态玻璃风格已开启" else "液态玻璃风格已关闭"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updateLiquidGlassRadius(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_RADIUS, MAX_LIQUID_GLASS_RADIUS)
        liquidGlassRadius = next
        draftLiquidGlassRadiusText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃圆角 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updateLiquidGlassOuterWidth(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_OUTER_WIDTH, MAX_LIQUID_GLASS_OUTER_WIDTH)
        liquidGlassOuterWidth = next
        draftLiquidGlassOuterWidthText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃外框高度 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updateLiquidGlassTopAlpha(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)
        liquidGlassTopAlpha = next
        draftLiquidGlassTopAlphaText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃顶部强度 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updateLiquidGlassBottomAlpha(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)
        liquidGlassBottomAlpha = next
        draftLiquidGlassBottomAlphaText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃底边强度 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updateLiquidGlassBackgroundMistAlpha(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_MIST_ALPHA, MAX_LIQUID_GLASS_MIST_ALPHA)
        liquidGlassBackgroundMistAlpha = next
        draftLiquidGlassBackgroundMistAlphaText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃背景灰雾 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updateLiquidGlassBottomDarkAlpha(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_BOTTOM_DARK_ALPHA, MAX_LIQUID_GLASS_BOTTOM_DARK_ALPHA)
        liquidGlassBottomDarkAlpha = next
        draftLiquidGlassBottomDarkAlphaText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃底部灰雾 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updateLiquidGlassSubjectScalePercent(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT, MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT)
        liquidGlassSubjectScalePercent = next
        draftLiquidGlassSubjectScaleText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃主体比例 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updateLiquidGlassSubjectOutlineWidth(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH, MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH)
        liquidGlassSubjectOutlineWidth = next
        draftLiquidGlassSubjectOutlineWidthText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃主体外框 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updateLiquidGlassSubjectInnerOutlineWidth(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH, MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH)
        liquidGlassSubjectInnerOutlineWidth = next
        draftLiquidGlassSubjectInnerOutlineWidthText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃主体内框 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updateLiquidGlassSubjectShadowAlpha(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA, MAX_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA)
        liquidGlassSubjectShadowAlpha = next
        draftLiquidGlassSubjectShadowAlphaText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃主体阴影 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun updateLiquidGlassSubjectOpacityPercent(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT, MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT)
        liquidGlassSubjectOpacityPercent = next
        draftLiquidGlassSubjectOpacityText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃主体透明度 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    private fun generateSelected(
        installWithRoot: Boolean,
        useGpt: Boolean,
        rootWriteMode: RootWriteMode = RootWriteMode.All,
    ) {
        val entry = apps.firstOrNull { it.packageName == selectedPackageName }
        if (entry == null) {
            statusText = "先选择一个应用"
            return
        }
        if (useGpt && gptApiKey.trim().isEmpty()) {
            statusText = "先填写 GPT API key"
            return
        }
        if (useGpt && gptBaseUrl.trim().isEmpty()) {
            statusText = "先填写 GPT Base URL"
            return
        }
        if (isBusy) {
            return
        }

        isBusy = true
        if (useGpt) {
            isGptPreviewLoading = true
        }
        statusText = if (useGpt) {
            "GPT处理中: ${entry.packageName}"
        } else {
            "本地处理中(自动): ${entry.packageName}"
        }
        startUiFriendlyThread(if (useGpt) "ArtPlusGptGenerate" else "ArtPlusLocalGenerate") {
            try {
                val result = generateArtPlusPackage(entry, useGpt)
                runOnUiThread {
                    activeGenerationSession = result.session
                    previewSelections = result.selections
                    previewChoiceMode = null
                    previewPackageName = entry.packageName
                    previewDirPath = result.outDir.absolutePath
                    previewVersion += 1
                    saveUiState()
                }
                if (outputTreeUri != null) {
                    exportToTree(result.outDir)
                }
                if (installWithRoot) {
                    installWithRoot(result.outDir, entry.packageName, rootWriteMode)
                    runOnUiThread {
                        markPackageGenerated(entry.packageName)
                    }
                    val sourceLabel = if (useGpt) "GPT版" else "本地版"
                    status("已生成${sourceLabel}并${rootWriteMode.label}写入，未刷新，请手动点首页左上角刷新图标: ${entry.packageName}")
                } else {
                    status("已生成${if (useGpt) "GPT版" else "本地版"}: ${result.outDir.absolutePath}")
                }
            } catch (error: Exception) {
                status("失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                runOnUiThread {
                    isBusy = false
                    if (useGpt) {
                        isGptPreviewLoading = false
                    }
                }
            }
        }
    }

    private fun writeSelectedWithRoot(rootWriteMode: RootWriteMode) {
        val entry = apps.firstOrNull { it.packageName == selectedPackageName }
        if (entry == null) {
            statusText = "先选择一个应用"
            return
        }
        if (isBusy) {
            return
        }
        val session = activeGenerationSession?.takeIf { it.packageName == entry.packageName }
        if (session == null) {
            generateSelected(
                installWithRoot = true,
                useGpt = false,
                rootWriteMode = rootWriteMode,
            )
            return
        }

        isBusy = true
        statusText = "按当前预览写入${rootWriteMode.label}: ${entry.packageName}"
        val selections = previewSelections
        startUiFriendlyThread("ArtPlusPreviewRootWrite") {
            try {
                writePackageOutputs(session, selections)
                if (outputTreeUri != null) {
                    exportToTree(session.outDir)
                }
                installWithRoot(session.outDir, entry.packageName, rootWriteMode)
                runOnUiThread {
                    markPackageGenerated(entry.packageName)
                    activeGenerationSession = session
                    previewSelections = selections
                    previewPackageName = entry.packageName
                    previewDirPath = session.outDir.absolutePath
                    previewVersion += 1
                    saveUiState()
                }
                status("已按当前预览${rootWriteMode.label}写入，未刷新，请手动点首页左上角刷新图标: ${entry.packageName}")
            } catch (error: Exception) {
                status("写入失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                runOnUiThread {
                    isBusy = false
                }
            }
        }
    }

    private fun selectAppAndRestoreGeneratedPreview(entry: AppEntry) {
        val packageName = entry.packageName
        val revision = ++generatedPreviewRestoreRevision
        selectedPackageName = packageName
        activeGenerationSession = null
        previewChoiceMode = null
        previewPackageName = null
        previewDirPath = null
        previewSelections = PreviewSelections.default(PreviewChoice.Original)
        previewVersion += 1
        clearRmbgCandidateUiState()
        val localDir = artPlusPackageDir(packageName)
        val knownGenerated = packageName in generatedPackageNames || hasGeneratedPackageBaseAssets(localDir)
        statusText = if (knownGenerated) {
            "正在读取现有图标包: ${entry.label} ($packageName)"
        } else {
            "已选择: ${entry.label} ($packageName)"
        }
        saveUiState()
        if (isBusy) {
            return
        }
        startUiFriendlyThread("ArtPlusRestoreGeneratedPreview") {
            val result = runCatching { existingGeneratedPackageDir(packageName) }
            runOnUiThread {
                if (revision != generatedPreviewRestoreRevision || selectedPackageName != packageName) {
                    return@runOnUiThread
                }
                result
                    .onSuccess { packageDir ->
                        markPackageGenerated(packageName)
                        activeGenerationSession = buildGeneratedPackageSession(packageName, packageDir)
                        previewSelections = PreviewSelections.default(PreviewChoice.Original)
                        previewChoiceMode = null
                        previewPackageName = packageName
                        previewDirPath = packageDir.absolutePath
                        previewVersion += 1
                        statusText = "已读取现有图标包: ${entry.label} ($packageName)"
                        saveUiState()
                    }
                    .onFailure { error ->
                        statusText = "未读取到现有图标包: ${error.message ?: error.javaClass.simpleName}"
                    }
            }
        }
    }

    private fun addLiquidGlassToSelectedGenerated() {
        val entry = apps.firstOrNull { it.packageName == selectedPackageName }
        if (entry == null) {
            statusText = "先选择一个应用"
            return
        }
        if (isBusy) {
            return
        }
        isBusy = true
        statusText = "正在添加光影: ${entry.packageName}"
        startUiFriendlyThread("ArtPlusAddLiquidGlass") {
            try {
                val packageDir = existingGeneratedPackageDir(entry.packageName)
                applyLiquidGlassToGeneratedPackage(packageDir)
                installLiquidGlassFilesWithRoot(packageDir, entry.packageName)
                runOnUiThread {
                    markPackageGenerated(entry.packageName)
                    activeGenerationSession = buildGeneratedPackageSession(entry.packageName, packageDir)
                    previewSelections = PreviewSelections.default(PreviewChoice.Original)
                    previewChoiceMode = null
                    previewPackageName = entry.packageName
                    previewDirPath = packageDir.absolutePath
                    previewVersion += 1
                    saveUiState()
                    statusText = "已添加光影，未刷新，请手动点首页左上角刷新图标: ${entry.packageName}"
                }
            } catch (error: Exception) {
                status("添加光影失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                runOnUiThread {
                    isBusy = false
                }
            }
        }
    }

    private fun toggleMultiSelectedPackage(packageName: String) {
        multiSelectedPackageNames = if (packageName in multiSelectedPackageNames) {
            multiSelectedPackageNames - packageName
        } else {
            multiSelectedPackageNames + packageName
        }
    }

    private fun addLiquidGlassToMultiSelectedGenerated() {
        val packageNames = multiSelectedPackageNames.toList().sorted()
        if (packageNames.isEmpty()) {
            statusText = "先选择要添加光影的应用"
            return
        }
        if (isBusy) {
            return
        }

        isBusy = true
        statusText = "正在批量添加光影: ${packageNames.size} 个"
        val selectedAtStart = selectedPackageName
        startUiFriendlyThread("ArtPlusBatchAddLiquidGlass") {
            val successes = mutableListOf<String>()
            val failures = mutableListOf<String>()
            var selectedSession: GenerationSession? = null
            var selectedDirPath: String? = null

            packageNames.forEachIndexed { index, packageName ->
                status("添加光影中 ${index + 1}/${packageNames.size}: $packageName")
                try {
                    val packageDir = existingGeneratedPackageDir(packageName)
                    applyLiquidGlassToGeneratedPackage(packageDir)
                    installLiquidGlassFilesWithRoot(packageDir, packageName)
                    successes += packageName
                    if (packageName == selectedAtStart) {
                        selectedSession = buildGeneratedPackageSession(packageName, packageDir)
                        selectedDirPath = packageDir.absolutePath
                    }
                } catch (error: Exception) {
                    failures += "$packageName: ${error.message ?: error.javaClass.simpleName}"
                }
            }

            runOnUiThread {
                if (successes.isNotEmpty()) {
                    updateGeneratedPackageCache(generatedPackageNames + successes)
                    multiSelectedPackageNames = multiSelectedPackageNames - successes.toSet()
                }
                if (
                    selectedAtStart != null &&
                    selectedPackageName == selectedAtStart &&
                    selectedSession != null &&
                    selectedDirPath != null
                ) {
                    activeGenerationSession = selectedSession
                    previewSelections = PreviewSelections.default(PreviewChoice.Original)
                    previewChoiceMode = null
                    previewPackageName = selectedAtStart
                    previewDirPath = selectedDirPath
                    previewVersion += 1
                    saveUiState()
                }
                statusText = when {
                    failures.isEmpty() -> "已批量添加光影 ${successes.size} 个，未刷新，请手动点首页左上角刷新图标"
                    successes.isEmpty() -> "批量添加光影失败: ${failures.first()}"
                    else -> "已添加光影 ${successes.size} 个，失败 ${failures.size} 个: ${failures.first()}"
                }
                isBusy = false
            }
        }
    }

    private fun existingGeneratedPackageDir(packageName: String): File {
        val currentPreviewDir = previewDirPath
            ?.takeIf { previewPackageName == packageName }
            ?.let(::File)
            ?.takeIf { hasGeneratedPackageBaseAssets(it) && it != artPlusPackageDir(packageName) }
        if (currentPreviewDir != null) {
            return currentPreviewDir
        }
        runCatching { copyRootGeneratedPackageToLocal(packageName) }
            .onSuccess { return it }
        val localDir = artPlusPackageDir(packageName)
        if (hasGeneratedPackageBaseAssets(localDir)) {
            return localDir
        }
        return copyRootGeneratedPackageToLocal(packageName)
    }

    private fun buildGeneratedPackageSession(packageName: String, packageDir: File): GenerationSession {
        val recfg = decodeGeneratedBitmap(packageDir, FOREGROUND_ORIGINAL_BACKUP_NAME)
            ?: decodeGeneratedBitmap(packageDir, "recfg.png")
            ?: error("现有图标包缺少 recfg.png")
        val recbg = decodeGeneratedBitmap(packageDir, "recbg.png")
            ?: error("现有图标包缺少 recbg.png")
        val normalizedRecfg = resizeBitmap(recfg, SIZE_1X1, SIZE_1X1)
        val normalizedRecbg = resizeBitmap(recbg, SIZE_1X1, SIZE_1X1)
        val monochrome = simpleMonochromeAlphaFromDefaultSubject(normalizedRecfg, invertLuma = false)
        val original = IconCandidate(
            recfgRaw = normalizedRecfg,
            recbg = normalizedRecbg,
            monochromeRaw = null,
            monochromeFromDefaultSubject = true,
            preserveGeometry = true,
        )
        return GenerationSession(
            packageName = packageName,
            outDir = packageDir,
            sourceIcon = centerOnCanvas(normalizedRecfg, GPT_SOURCE_SIZE, GPT_SOURCE_SIZE),
            baseRecfg = normalizedRecfg,
            baseRecbg = normalizedRecbg,
            monochromeRaw = monochrome,
            candidates = mapOf(PreviewChoice.Original to original),
            autoLocalChoice = PreviewChoice.Original,
            canRebuildLocalCandidates = false,
        )
    }

    private fun artPlusPackageDir(packageName: String): File {
        val base = getExternalFilesDir("ArtPlus") ?: File(filesDir, "ArtPlus")
        return File(base, packageName)
    }

    private fun rootGeneratedPreviewDir(packageName: String): File =
        File(File(filesDir, "RootGeneratedPreview"), packageName)

    private fun hasGeneratedPackageBaseAssets(dir: File): Boolean =
        dir.isDirectory &&
            File(dir, "recbg.png").isFile &&
            File(dir, "recfg.png").isFile

    private fun copyRootGeneratedPackageToLocal(packageName: String): File {
        val targetDir = rootGeneratedPreviewDir(packageName)
        ensureFreshDir(targetDir)
        val sourceDir = "$ROOT_UXICONS_DIR/$packageName"
        val appUid = applicationInfo.uid
        val command = """
            set -e
            src=${shQuote(sourceDir)}
            dst=${shQuote(targetDir.absolutePath)}
            [ -d "${'$'}src" ] || { echo "data 中没有图标包"; exit 2; }
            copied=0
            find "${'$'}src" -maxdepth 1 -type f -name '*.png' | while IFS= read -r file; do
                cp -f "${'$'}file" "${'$'}dst"/
                copied=1
            done
            if ! ls "${'$'}dst"/*.png >/dev/null 2>&1; then
                echo "data 图标包没有 PNG"
                exit 3
            fi
            chown -R $appUid:$appUid "${'$'}dst" 2>/dev/null || true
            chmod 0644 "${'$'}dst"/*.png 2>/dev/null || true
        """.trimIndent()
        runRootCommand(command, ROOT_SCAN_TIMEOUT_MS)
        if (!hasGeneratedPackageBaseAssets(targetDir)) {
            error("现有图标包缺少 recbg.png 或 recfg.png")
        }
        return targetDir
    }

    private fun applyLiquidGlassToGeneratedPackage(dir: File) {
        val baseRecbg = decodeGeneratedBitmap(dir, "recbg.png")
            ?: error("现有图标包缺少 recbg.png")
        val originalRecfgFile = File(dir, FOREGROUND_ORIGINAL_BACKUP_NAME)
        val baseRecfg = decodeGeneratedBitmap(dir, FOREGROUND_ORIGINAL_BACKUP_NAME)
            ?: decodeGeneratedBitmap(dir, "recfg.png")
            ?: error("现有图标包缺少 recfg.png")
        if (!originalRecfgFile.isFile) {
            savePng(baseRecfg, originalRecfgFile)
        }

        val recbg = glassBackgroundForGeneratedPackage(dir, "recbg.png", baseRecbg, SIZE_1X1, SIZE_1X1)
        val recbg1x2 = glassBackgroundForGeneratedPackage(dir, "recbg_1x2.png", baseRecbg, SIZE_1X2[0], SIZE_1X2[1])
        val recbg2x1 = glassBackgroundForGeneratedPackage(dir, "recbg_2x1.png", baseRecbg, SIZE_2X1[0], SIZE_2X1[1])
        val recbg2x2 = glassBackgroundForGeneratedPackage(dir, "recbg_2x2.png", baseRecbg, SIZE_2X2, SIZE_2X2)

        savePng(recbg, File(dir, "recbg.png"))
        savePng(recbg1x2, File(dir, "recbg_1x2.png"))
        savePng(recbg2x1, File(dir, "recbg_2x1.png"))
        savePng(recbg2x2, File(dir, "recbg_2x2.png"))

        val outputRecfg = foregroundForSize(baseRecfg, SIZE_1X1, SIZE_1X1, forceLiquidGlass = true)
        val recfg1x2Source = decodeGeneratedBitmap(dir, "recfg_1x2.png")
            ?: centerOnCanvas(baseRecfg, SIZE_1X2[0], SIZE_1X2[1])
        val recfg2x1Source = decodeGeneratedBitmap(dir, "recfg_2x1.png")
            ?: centerOnCanvas(baseRecfg, SIZE_2X1[0], SIZE_2X1[1])
        val recfg2x2Source = decodeGeneratedBitmap(dir, "recfg_2x2.png")
            ?: centerOnCanvas(baseRecfg, SIZE_2X2, SIZE_2X2)
        val recfg1x2 = foregroundForSize(recfg1x2Source, SIZE_1X2[0], SIZE_1X2[1], forceLiquidGlass = true)
        val recfg2x1 = foregroundForSize(recfg2x1Source, SIZE_2X1[0], SIZE_2X1[1], forceLiquidGlass = true)
        val recfg2x2 = foregroundForSize(recfg2x2Source, SIZE_2X2, SIZE_2X2, forceLiquidGlass = true)

        writeDefaultSubjectMonochromeFiles(dir, baseRecfg, overwriteExisting = false)

        savePng(outputRecfg, File(dir, "recfg.png"))
        savePng(recfg1x2, File(dir, "recfg_1x2.png"))
        savePng(recfg2x1, File(dir, "recfg_2x1.png"))
        savePng(recfg2x2, File(dir, "recfg_2x2.png"))

        savePng(normalDarkForeground(outputRecfg, recbg), File(dir, "rec_night.png"))
        savePng(normalDarkForeground(recfg1x2, recbg1x2), File(dir, "rec_night_1x2.png"))
        savePng(normalDarkForeground(recfg2x1, recbg2x1), File(dir, "rec_night_2x1.png"))
        savePng(normalDarkForeground(recfg2x2, recbg2x2), File(dir, "rec_night_2x2.png"))
    }

    private fun writeDefaultSubjectMonochromeFiles(
        dir: File,
        baseRecfg: Bitmap,
        overwriteExisting: Boolean,
    ) {
        val subject = if (baseRecfg.width == SIZE_1X1 && baseRecfg.height == SIZE_1X1) {
            baseRecfg
        } else {
            resizeBitmap(baseRecfg, SIZE_1X1, SIZE_1X1)
        }
        val rawLight = simpleMonochromeAlphaFromDefaultSubject(subject, invertLuma = true)
        val rawDark = simpleMonochromeAlphaFromDefaultSubject(subject, invertLuma = false)
        val outputs = listOf(
            "monochrome_light.png" to scaleMonochromeForTheme(rawLight),
            "monochrome_dark.png" to scaleMonochromeForTheme(rawDark),
            "monochrome.png" to scaleMonochromeForTheme(rawDark),
            "monochrome_1x2.png" to centerOnCanvas(rawDark, SIZE_1X2[0], SIZE_1X2[1]),
            "monochrome_2x1.png" to centerOnCanvas(rawDark, SIZE_2X1[0], SIZE_2X1[1]),
            "monochrome_2x2.png" to centerOnCanvas(rawDark, SIZE_2X2, SIZE_2X2),
        )
        outputs.forEach { (name, bitmap) ->
            val target = File(dir, name)
            if (overwriteExisting || !target.isFile) {
                savePng(bitmap, target)
            }
        }
    }

    private fun glassBackgroundForGeneratedPackage(
        dir: File,
        name: String,
        fallback: Bitmap,
        width: Int,
        height: Int,
    ): Bitmap {
        val source = decodeGeneratedBitmap(dir, name) ?: fallback
        val resized = if (source.width == width && source.height == height) {
            source
        } else {
            resizeBitmap(source, width, height)
        }
        return liquidGlassBackgroundForSize(resized, width, height, forceLiquidGlass = true)
    }

    private fun decodeGeneratedBitmap(dir: File, name: String): Bitmap? =
        BitmapFactory.decodeFile(File(dir, name).absolutePath)

    private fun installLiquidGlassFilesWithRoot(packageDir: File, packageName: String) {
        val target = "$ROOT_UXICONS_DIR/$packageName"
        val source = packageDir.absolutePath
        val names = listOf(
            "recbg.png",
            "recbg_1x2.png",
            "recbg_2x1.png",
            "recbg_2x2.png",
            "recfg.png",
            "recfg_1x2.png",
            "recfg_2x1.png",
            "recfg_2x2.png",
            "rec_night.png",
            "rec_night_1x2.png",
            "rec_night_2x1.png",
            "rec_night_2x2.png",
            "monochrome_light.png",
            "monochrome_dark.png",
            "monochrome.png",
            "monochrome_1x2.png",
            "monochrome_2x1.png",
            "monochrome_2x2.png",
        )
        val copyCommands = names.joinToString(separator = "\n") { name ->
            """
            if [ -f ${shQuote("$source/$name")} ]; then
                cp -f ${shQuote("$source/$name")} ${shQuote("$target/$name")}
                chmod 0644 ${shQuote("$target/$name")}
            fi
            """.trimIndent()
        }
        val command = """
            set -e
            mkdir -p ${shQuote(target)}
            $copyCommands
            restorecon -RF ${shQuote(target)} 2>/dev/null || true
        """.trimIndent()
        runRootCommand(command, ROOT_SCAN_TIMEOUT_MS)
    }

    private fun generateArtPlusPackage(
        app: AppEntry,
        useGpt: Boolean,
        localModeOverride: LocalSeparationMode? = null,
    ): GenerationResult {
        val base = getExternalFilesDir("ArtPlus") ?: File(filesDir, "ArtPlus")
        val outDir = File(base, app.packageName)
        ensureCleanDir(outDir)

        val icon = app.applicationInfo.loadIcon(packageManager)
        val localSourceIcon = drawLocalCandidateSourceIcon(icon, SIZE_1X1, SIZE_1X1)
        val gptSourceIcon = drawDrawable(icon, GPT_SOURCE_SIZE, GPT_SOURCE_SIZE, transparent = false)
        val localSource = buildLocalIconLayers(icon)
        val localCandidateSet = buildLocalCandidates(localSource, localSourceIcon)
        val localCandidates = localCandidateSet.candidates
        val candidates = if (useGpt) {
            val gptLayers = generateGptLayers(gptSourceIcon, localSource.recfg, localSource.recbg)
            localCandidates + (PreviewChoice.Gpt to IconCandidate(gptLayers.recfg, gptLayers.recbg, monochromeRaw = null))
        } else {
            localCandidates
        }
        val selectedLocalMode = localModeOverride ?: LocalSeparationMode.Auto
        val defaultChoice = if (useGpt) {
            PreviewChoice.Gpt
        } else {
            defaultPreviewChoiceForMode(selectedLocalMode, localCandidateSet.autoChoice)
        }
        val selections = PreviewSelections.default(defaultChoice)
        val session = GenerationSession(
            packageName = app.packageName,
            outDir = outDir,
            sourceIcon = gptSourceIcon,
            baseRecfg = localSource.recfg,
            baseRecbg = localSource.recbg,
            monochromeRaw = localSource.monochrome,
            candidates = candidates,
            autoLocalChoice = localCandidateSet.autoChoice,
        )
        writePackageOutputs(session, selections)
        status("本地分离: ${selectedLocalMode.label}/${defaultChoice.label} · 背景 $backgroundSeparationPercent · 底板 $plateRemovalPercent · 阴影 $shadowRemovalPercent · 毛刺 $edgePolishPercent")
        return GenerationResult(outDir = outDir, session = session, selections = selections)
    }

    private fun buildLocalCandidates(localSource: LocalIconLayers, sourceIcon: Bitmap): LocalCandidateSet {
        val originalForeground = prepareOriginalForeground(localSource.recfg)
        val original = IconCandidate(
            recfgRaw = originalForeground,
            recbg = localSource.recbg,
            monochromeRaw = localSource.monochrome,
            monochromeIsNative = localSource.monochromeIsNative,
            preserveGeometry = localSource.preserveGeometry,
        )
        val plateResult = separateLocalForeground(localSource.recfg, localSource.recbg, LocalSeparationMode.Plate)
        val fullResult = separateLocalForeground(localSource.recfg, localSource.recbg, LocalSeparationMode.Full)
        val cleanupResult = chooseMergedCleanupResult(
            original = originalForeground,
            plate = plateResult,
            full = fullResult,
        )
        val cleanup = IconCandidate(
            recfgRaw = cleanupResult.bitmap,
            recbg = localSource.recbg,
            monochromeRaw = localSource.monochrome,
            monochromeIsNative = localSource.monochromeIsNative,
            preserveGeometry = localSource.preserveGeometry,
        )
        val composedBackground = buildComposedBackgroundCandidate(
            source = sourceIcon,
            monochrome = localSource.monochrome,
            monochromeIsNative = localSource.monochromeIsNative,
        )
        val twoLayerResult = buildTwoLayerCandidate(sourceIcon)
        val candidates = linkedMapOf<PreviewChoice, IconCandidate>(
            PreviewChoice.Original to original,
            PreviewChoice.Full to cleanup,
            PreviewChoice.ComposedBackground to composedBackground,
        )
        if (localSource.textSafe != null) {
            candidates[PreviewChoice.TextSafe] = localSource.textSafe
        }
        if (localSource.componentSubject != null) {
            candidates[PreviewChoice.ComponentSubject] = localSource.componentSubject
        }
        if (localSource.componentBackground != null) {
            candidates[PreviewChoice.ComponentBackground] = localSource.componentBackground
        }
        if (twoLayerResult?.candidate != null) {
            candidates[PreviewChoice.TwoLayer] = twoLayerResult.candidate
        }
        val autoChoice = chooseAutoLocalChoice(
            original = originalForeground,
            cleanup = cleanupResult.bitmap,
            twoLayer = twoLayerResult,
            rmbg = null,
        )
        return LocalCandidateSet(candidates = candidates, autoChoice = autoChoice)
    }

    private fun chooseMergedCleanupResult(
        original: Bitmap,
        plate: LocalSeparationResult,
        full: LocalSeparationResult,
    ): LocalSeparationResult {
        val originalCoverage = meaningfulAlphaCoverage(original)
        val plateCoverage = meaningfulAlphaCoverage(plate.bitmap)
        val fullCoverage = meaningfulAlphaCoverage(full.bitmap)
        val plateUsable = isAutoLocalCandidateUsable(
            candidate = plate.bitmap,
            originalCoverage = originalCoverage,
            candidateCoverage = plateCoverage,
        )
        val fullUsable = isAutoLocalCandidateUsable(
            candidate = full.bitmap,
            originalCoverage = originalCoverage,
            candidateCoverage = fullCoverage,
        )
        return when {
            fullUsable && (!plateUsable || fullCoverage <= plateCoverage + AUTO_COVERAGE_CHANGE_THRESHOLD) -> full
            plateUsable -> plate.copy(summary = plate.summary.replace("去底板", "清理"))
            else -> full
        }
    }

    private fun buildComposedBackgroundCandidate(
        source: Bitmap,
        monochrome: Bitmap?,
        monochromeIsNative: Boolean,
    ): IconCandidate {
        val normalizedSource = if (source.width == SIZE_1X1 && source.height == SIZE_1X1) {
            source
        } else {
            resizeBitmap(source, SIZE_1X1, SIZE_1X1)
        }
        val recbg = solidBitmap(
            SIZE_1X1,
            SIZE_1X1,
            estimatePlainIconBackground(normalizedSource),
        )
        val extracted = subtractPlainIconBackground(normalizedSource, recbg)
        val background = rebuildComposedIconBackground(normalizedSource, extracted, recbg)
        val cleaned = separateLocalForeground(
            source = extracted,
            background = background,
            mode = LocalSeparationMode.ComposedBackground,
        ).bitmap
        return IconCandidate(
            recfgRaw = cleaned,
            recbg = background,
            monochromeRaw = monochrome,
            monochromeIsNative = monochromeIsNative,
            preserveGeometry = false,
        )
    }

    private fun rebuildComposedIconBackground(source: Bitmap, extractedForeground: Bitmap, fallbackBackground: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val sourcePixels = IntArray(width * height)
        val fallbackPixels = IntArray(width * height)
        val foregroundPixels = IntArray(width * height)
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
        val fallback = if (fallbackBackground.width == width && fallbackBackground.height == height) {
            fallbackBackground
        } else {
            resizeBitmap(fallbackBackground, width, height)
        }
        fallback.getPixels(fallbackPixels, 0, width, 0, 0, width, height)
        extractedForeground.getPixels(foregroundPixels, 0, width, 0, 0, width, height)

        val subjectMask = BooleanArray(width * height)
        for (i in foregroundPixels.indices) {
            subjectMask[i] = AndroidColor.alpha(foregroundPixels[i]) > COMPOSED_BACKGROUND_SUBJECT_ALPHA_THRESHOLD
        }
        val fillMask = dilateMask(subjectMask, width, height, COMPOSED_BACKGROUND_FILL_RADIUS)
        val outPixels = sourcePixels.copyOf()
        for (i in outPixels.indices) {
            if (fillMask[i] || AndroidColor.alpha(outPixels[i]) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                outPixels[i] = AndroidColor.argb(
                    255,
                    AndroidColor.red(fallbackPixels[i]),
                    AndroidColor.green(fallbackPixels[i]),
                    AndroidColor.blue(fallbackPixels[i]),
                )
            } else if (AndroidColor.alpha(outPixels[i]) < 255) {
                val pixel = outPixels[i]
                outPixels[i] = AndroidColor.argb(
                    255,
                    AndroidColor.red(pixel),
                    AndroidColor.green(pixel),
                    AndroidColor.blue(pixel),
                )
            }
        }
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun buildTwoLayerCandidate(source: Bitmap): CandidateBuildResult? {
        val width = source.width
        val height = source.height
        if (width <= 0 || height <= 0) {
            return null
        }
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val borderColor = medianEdgeColor(pixels, width, height)
        val plateLike = BooleanArray(pixels.size)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            plateLike[i] = AndroidColor.alpha(pixel) > LOCAL_ALPHA_VISIBLE_THRESHOLD &&
                colorDistance(pixel, borderColor) >= TWO_LAYER_PLATE_BACKGROUND_DISTANCE
        }
        val plateMask = largestConnectedMask(plateLike, width, height)
        val platePixels = plateMask.count { it }
        if (platePixels == 0) {
            return null
        }
        val plateCoverage = platePixels.toDouble() / pixels.size.toDouble()
        if (plateCoverage !in TWO_LAYER_MIN_PLATE_COVERAGE..TWO_LAYER_MAX_PLATE_COVERAGE) {
            return null
        }

        val plateDilated = dilateMask(plateMask, width, height, TWO_LAYER_SUBJECT_PLATE_DILATE_RADIUS)
        val subjectLike = BooleanArray(pixels.size)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            subjectLike[i] = plateDilated[i] &&
                AndroidColor.alpha(pixel) > LOCAL_ALPHA_VISIBLE_THRESHOLD &&
                colorDistance(pixel, borderColor) <= TWO_LAYER_SUBJECT_BACKGROUND_DISTANCE
        }

        val subjectMask = BooleanArray(pixels.size)
        val minArea = maxOf(TWO_LAYER_MIN_SUBJECT_PIXELS, (pixels.size * TWO_LAYER_MIN_SUBJECT_COVERAGE).toInt())
        val maxArea = (platePixels * TWO_LAYER_MAX_SUBJECT_TO_PLATE_RATIO).toInt().coerceAtLeast(minArea)
        connectedMaskComponents(subjectLike, width, height).forEach { component ->
            if (component.touchesEdge || component.size < minArea || component.size > maxArea) {
                return@forEach
            }
            val boundsArea = component.bounds.width() * component.bounds.height()
            if (boundsArea <= 0) {
                return@forEach
            }
            val fillRatio = component.size.toDouble() / boundsArea.toDouble()
            if (fillRatio < TWO_LAYER_MIN_SUBJECT_FILL_RATIO) {
                return@forEach
            }
            if (boundsArea > platePixels * TWO_LAYER_MAX_SUBJECT_BOUNDS_TO_PLATE_RATIO) {
                return@forEach
            }
            component.indices.forEach { subjectMask[it] = true }
        }

        val closedSubjectMask = erodeMask(
            dilateMask(subjectMask, width, height, TWO_LAYER_SUBJECT_CLOSE_RADIUS),
            width,
            height,
            TWO_LAYER_SUBJECT_CLOSE_RADIUS,
        )
        val subjectPixels = closedSubjectMask.count { it }
        if (subjectPixels == 0) {
            return null
        }
        val subjectCoverage = subjectPixels.toDouble() / pixels.size.toDouble()
        val plateColor = dominantMaskColor(pixels, plateMask)
        val plateStd = maskColorStd(pixels, plateMask)
        val plateLuma = luma(plateColor).toDouble() / 255.0
        val manualUsable = subjectCoverage >= TWO_LAYER_MIN_MANUAL_SUBJECT_COVERAGE &&
            subjectCoverage <= plateCoverage * TWO_LAYER_MAX_SUBJECT_TO_PLATE_RATIO &&
            plateStd <= TWO_LAYER_MANUAL_MAX_PLATE_STD
        if (!manualUsable) {
            return null
        }

        val foreground = smoothAlphaEdges(
            applyMaskToSolidColor(width, height, closedSubjectMask, borderColor),
            TWO_LAYER_EDGE_SMOOTH_STRENGTH,
            growTransparentEdges = true,
            radius = TWO_LAYER_EDGE_SMOOTH_RADIUS,
            growStrength = TWO_LAYER_EDGE_GROW_STRENGTH,
        )
        val background = fillMaskOnSource(
            pixels = pixels,
            width = width,
            height = height,
            mask = dilateMask(closedSubjectMask, width, height, TWO_LAYER_BACKGROUND_FILL_RADIUS),
            color = plateColor,
        )
        val autoUsable = plateCoverage <= TWO_LAYER_AUTO_MAX_PLATE_COVERAGE &&
            subjectCoverage <= plateCoverage * TWO_LAYER_AUTO_MAX_SUBJECT_TO_PLATE_RATIO &&
            plateStd <= TWO_LAYER_AUTO_MAX_PLATE_STD &&
            plateLuma >= TWO_LAYER_AUTO_MIN_PLATE_LUMA
        return CandidateBuildResult(
            candidate = IconCandidate(
                recfgRaw = foreground,
                recbg = background,
                monochromeRaw = foreground,
                preserveGeometry = true,
            ),
            autoUsable = autoUsable,
            coverage = subjectCoverage,
        )
    }

    private fun buildRmbgCandidate(sourceIcon: Bitmap): CandidateBuildResult? {
        val component = findRmbgComponent() ?: return null
        return runCatching {
            val mask = runRmbgAlphaMask(sourceIcon, component)
            val tunedAlpha = tuneRmbgAlpha(mask.alpha, sourceIcon.width, sourceIcon.height)
            val foreground = applyAlphaArrayToSource(sourceIcon, tunedAlpha)
            val cleanBackground = rebuildRmbgBackground(sourceIcon, foreground)
            val coverage = meaningfulAlphaCoverage(foreground)
            val bounds = meaningfulAlphaBounds(foreground)
            val cropRisk = bounds?.let { hasAutoCropRisk(it, foreground.width, foreground.height) } ?: true
            val manualUsable = coverage in RMBG_MIN_MANUAL_COVERAGE..RMBG_MAX_MANUAL_COVERAGE &&
                bounds != null &&
                !cropRisk
            val validationWarning = if (manualUsable) null else rmbgValidationWarning(coverage, bounds, cropRisk)
            CandidateBuildResult(
                candidate = IconCandidate(
                    recfgRaw = foreground,
                    recbg = cleanBackground,
                    monochromeRaw = foreground,
                    rmbgSourceRaw = sourceIcon,
                    rmbgAlphaRaw = mask.alpha,
                ),
                autoUsable = manualUsable && coverage in RMBG_MIN_AUTO_COVERAGE..RMBG_MAX_AUTO_COVERAGE,
                coverage = coverage,
                rmbgInference = mask.report,
                manualUsable = manualUsable,
                validationWarning = validationWarning,
            )
        }.getOrElse { throw it }
    }

    private fun rebuildRmbgBackground(sourceIcon: Bitmap, foreground: Bitmap): Bitmap {
        val fallback = solidBitmap(
            sourceIcon.width,
            sourceIcon.height,
            estimatePlainIconBackground(sourceIcon),
        )
        return rebuildComposedIconBackground(sourceIcon, foreground, fallback)
    }

    private fun rmbgValidationWarning(coverage: Double, bounds: Bounds?, cropRisk: Boolean): String {
        val coverageText = (coverage * 100.0).roundToInt()
        val boundsText = bounds?.let { "${it.width()}x${it.height()}@${it.left},${it.top}" } ?: "无"
        return "RMBG候选未通过校验，已保留: 覆盖率 ${coverageText}%，边界 $boundsText，贴边风险 ${if (cropRisk) "是" else "否"}"
    }

    private fun buildRmbgDebugCandidate(sourceIcon: Bitmap): RmbgDebugCandidate {
        val component = findRmbgComponent() ?: error("未安装 RMBG 组件 ZIP")
        val mask = runRmbgAlphaMask(sourceIcon, component)
        val tunedAlpha = tuneRmbgAlpha(mask.alpha, sourceIcon.width, sourceIcon.height)
        val foreground = applyAlphaArrayToSource(sourceIcon, tunedAlpha)
        val cleanBackground = rebuildRmbgBackground(sourceIcon, foreground)
        val coverage = meaningfulAlphaCoverage(foreground)
        val bounds = meaningfulAlphaBounds(foreground)
        val cropRisk = bounds?.let { hasAutoCropRisk(it, foreground.width, foreground.height) } ?: true
        val manualUsable = coverage in RMBG_MIN_MANUAL_COVERAGE..RMBG_MAX_MANUAL_COVERAGE &&
            bounds != null &&
            !cropRisk
        val candidate = IconCandidate(
            recfgRaw = foreground,
            recbg = cleanBackground,
            monochromeRaw = foreground,
            rmbgSourceRaw = sourceIcon,
            rmbgAlphaRaw = mask.alpha,
        )
        val result = CandidateBuildResult(
            candidate = candidate,
            autoUsable = manualUsable && coverage in RMBG_MIN_AUTO_COVERAGE..RMBG_MAX_AUTO_COVERAGE,
            coverage = coverage,
            rmbgInference = mask.report,
            manualUsable = manualUsable,
            validationWarning = if (manualUsable) null else rmbgValidationWarning(coverage, bounds, cropRisk),
        )
        return RmbgDebugCandidate(
            foreground = foreground,
            result = result,
            coverage = coverage,
            boundsText = bounds?.let { "${it.width()}x${it.height()}@${it.left},${it.top}" } ?: "无",
            cropRisk = cropRisk,
            manualUsable = manualUsable,
            inference = mask.report,
        )
    }

    private fun rmbgComponentDir(): File = File(filesDir, RMBG_COMPONENT_DIR)

    private fun findRmbgComponent(): RmbgComponent? {
        val dir = rmbgComponentDir()
        val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: return null
        val model = File(dir, RMBG_MODEL_NAME)
        if (!model.isFile || model.length() < RMBG_MIN_MODEL_BYTES) {
            return null
        }
        return RmbgComponent(dir, abi, model)
    }

    private fun clearInstalledRmbgComponent() {
        if (isBusy || isGeneratingRmbgCandidate || isInstallingRmbgComponent) {
            return
        }
        runCatching { rmbgRuntime?.close() }
        rmbgRuntime = null
        val targetDir = rmbgComponentDir()
        val tmpDir = File(filesDir, "$RMBG_COMPONENT_DIR.tmp")
        val deleted = targetDir.exists() && targetDir.deleteRecursively()
        if (tmpDir.exists()) {
            tmpDir.deleteRecursively()
        }
        clearRmbgCandidateUiState()
        lastRmbgInferenceReport = null
        rmbgComponentStatus = "${System.currentTimeMillis()}"
        rmbgInstallStage = ""
        rmbgInstallProgress = null
        rmbgComponentSaveStatus = ""
        statusText = if (deleted) "已清除 RMBG" else "没有已安装 RMBG"
    }

    private fun installRmbgComponent(uri: Uri) {
        if (isBusy || isGeneratingRmbgCandidate || isInstallingRmbgComponent) {
            return
        }
        isInstallingRmbgComponent = true
        rmbgInstallStage = "读取组件"
        rmbgInstallProgress = null
        statusText = "RMBG组件安装中"
        Thread {
            try {
                val component = contentResolver.openInputStream(uri)?.use { input ->
                    installRmbgComponentFromInput(input)
                } ?: error("无法打开组件 ZIP")
                runOnUiThread {
                    rmbgComponentStatus = "${System.currentTimeMillis()}"
                    lastRmbgCandidateError = null
                    rmbgInstallStage = "安装完成"
                    rmbgInstallProgress = 1f
                    statusText = "RMBG已安装: ${component.abi}"
                }
            } catch (error: Exception) {
                runOnUiThread {
                    rmbgComponentStatus = "${System.currentTimeMillis()}"
                    lastRmbgCandidateError = "RMBG安装失败: ${error.message ?: error.javaClass.simpleName}"
                    rmbgInstallStage = "安装失败"
                    rmbgInstallProgress = null
                    statusText = lastRmbgCandidateError ?: "RMBG安装失败"
                }
            } finally {
                runOnUiThread {
                    isInstallingRmbgComponent = false
                    rmbgInstallProgress = null
                }
            }
        }.start()
    }

    private fun installRmbgComponentFromUrl() {
        if (isBusy || isGeneratingRmbgCandidate || isInstallingRmbgComponent) {
            return
        }
        val urlText = rmbgComponentUrl.trim()
        if (urlText.isEmpty()) {
            statusText = "先填 RMBG 组件 URL"
            return
        }
        saveRmbgSettings()
        isInstallingRmbgComponent = true
        rmbgInstallStage = "准备下载"
        rmbgInstallProgress = null
        statusText = "RMBG组件下载中"
        Thread {
            val tmpDownload = File(cacheDir, "rmbg-download-${System.currentTimeMillis()}")
            try {
                val component = if (urlText.endsWith(".zip", ignoreCase = true)) {
                    downloadRmbgFile(urlText, tmpDownload, RMBG_MIN_COMPONENT_ZIP_BYTES, "RMBG组件")
                    FileInputStream(tmpDownload).use { input -> installRmbgComponentFromInput(input) }
                } else {
                    installRmbgComponentFromModelUrl(urlText, tmpDownload)
                }
                runOnUiThread {
                    rmbgComponentStatus = "${System.currentTimeMillis()}"
                    rmbgComponentSaveStatus = "已保存"
                    lastRmbgCandidateError = null
                    rmbgInstallStage = "安装完成"
                    rmbgInstallProgress = 1f
                    statusText = "RMBG已安装: ${component.abi}"
                }
            } catch (error: Exception) {
                runOnUiThread {
                    rmbgComponentStatus = "${System.currentTimeMillis()}"
                    lastRmbgCandidateError = "RMBG安装失败: ${error.message ?: error.javaClass.simpleName}"
                    rmbgInstallStage = "安装失败"
                    rmbgInstallProgress = null
                    statusText = lastRmbgCandidateError ?: "RMBG安装失败"
                }
            } finally {
                tmpDownload.delete()
                runOnUiThread { isInstallingRmbgComponent = false }
            }
        }.start()
    }

    private fun installRmbgComponentFromModelUrl(modelUrl: String, modelFile: File): RmbgComponent {
        val targetDir = rmbgComponentDir()
        val tmpDir = File(filesDir, "$RMBG_COMPONENT_DIR.tmp")
        runCatching { rmbgRuntime?.close() }
        rmbgRuntime = null
        if (tmpDir.exists()) {
            tmpDir.deleteRecursively()
        }
        tmpDir.mkdirs()
        try {
            downloadRmbgFile(modelUrl, modelFile, RMBG_MIN_MODEL_BYTES, "RMBG模型")
            runOnUiThread {
                rmbgInstallStage = "安装模型"
                rmbgInstallProgress = null
            }
            modelFile.copyTo(File(tmpDir, RMBG_MODEL_NAME), overwrite = true)
            validateRmbgComponentDir(tmpDir)
            if (targetDir.exists()) {
                targetDir.deleteRecursively()
            }
            if (!tmpDir.renameTo(targetDir)) {
                copyDirectory(tmpDir, targetDir)
                tmpDir.deleteRecursively()
            }
            return findRmbgComponent()
                ?: error("缺少当前 ABI: ${Build.SUPPORTED_ABIS.joinToString()}")
        } catch (error: Exception) {
            tmpDir.deleteRecursively()
            throw error
        }
    }

    private fun installRmbgComponentFromInput(input: InputStream): RmbgComponent {
        val targetDir = rmbgComponentDir()
        val tmpDir = File(filesDir, "$RMBG_COMPONENT_DIR.tmp")
        runCatching { rmbgRuntime?.close() }
        rmbgRuntime = null
        if (tmpDir.exists()) {
            tmpDir.deleteRecursively()
        }
        tmpDir.mkdirs()
        try {
            unzipRmbgComponent(input, tmpDir)
            normalizeRmbgModelFile(tmpDir)
            validateRmbgComponentDir(tmpDir)
            if (targetDir.exists()) {
                targetDir.deleteRecursively()
            }
            if (!tmpDir.renameTo(targetDir)) {
                copyDirectory(tmpDir, targetDir)
                tmpDir.deleteRecursively()
            }
            return findRmbgComponent()
                ?: error("缺少当前 ABI: ${Build.SUPPORTED_ABIS.joinToString()}")
        } catch (error: Exception) {
            tmpDir.deleteRecursively()
            throw error
        }
    }

    private fun downloadRmbgFile(urlText: String, target: File, minBytes: Long, label: String) {
        val url = validatedRemoteUrl(urlText, label)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = RMBG_DOWNLOAD_CONNECT_TIMEOUT_MS
            readTimeout = RMBG_DOWNLOAD_READ_TIMEOUT_MS
            url.userInfo?.takeIf { it.isNotBlank() }?.let { userInfo ->
                setRequestProperty(
                    "Authorization",
                    "Basic ${Base64.encodeToString(userInfo.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)}",
                )
            }
        }
        try {
            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }
            if (connection.responseCode !in 200..299) {
                val message = stream.bufferedReader().use { it.readText() }.take(160)
                error("HTTP ${connection.responseCode}: $message")
            }
            val totalBytes = connection.contentLengthLong.takeIf { it > 0L }
            if (totalBytes != null && totalBytes > RMBG_MAX_DOWNLOAD_BYTES) {
                error("$label 超过最大下载大小")
            }
            var downloaded = 0L
            var nextReportAt = 0L
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            target.parentFile?.mkdirs()
            runOnUiThread {
                rmbgInstallStage = "$label 下载中"
                rmbgInstallProgress = totalBytes?.let { 0f }
                statusText = "$label 下载中"
            }
            stream.use { input ->
                FileOutputStream(target).use { output ->
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) {
                            break
                        }
                        output.write(buffer, 0, read)
                        downloaded += read.toLong()
                        if (downloaded > RMBG_MAX_DOWNLOAD_BYTES) {
                            error("$label 超过最大下载大小")
                        }
                        if (downloaded >= nextReportAt) {
                            val progress = totalBytes?.let { downloaded.toFloat() / it.toFloat() }
                            val text = totalBytes?.let { total ->
                                val percent = ((progress ?: 0f) * 100f).roundToInt().coerceIn(0, 100)
                                "$label $percent% · ${downloaded / 1024 / 1024}/${total / 1024 / 1024}MB"
                            } ?: "$label ${downloaded / 1024 / 1024}MB"
                            runOnUiThread {
                                statusText = text
                                rmbgInstallStage = text
                                rmbgInstallProgress = progress?.coerceIn(0f, 1f)
                            }
                            nextReportAt = downloaded + 2L * 1024L * 1024L
                        }
                    }
                }
            }
            if (target.length() < minBytes) {
                error("$label 过小")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun unzipRmbgComponent(input: InputStream, targetDir: File) {
        val canonicalTarget = targetDir.canonicalFile
        var totalWritten = 0L
        var fileCount = 0
        ZipInputStream(input).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val entryName = entry.name.replace('\\', '/').trimStart('/')
                if (entryName.isBlank() || entryName.contains("..")) {
                    zip.closeEntry()
                    continue
                }
                fileCount += 1
                if (fileCount > RMBG_MAX_COMPONENT_ZIP_ENTRIES) {
                    error("RMBG组件压缩包文件过多")
                }
                val outFile = File(targetDir, entryName)
                val canonicalOut = outFile.canonicalFile
                if (!canonicalOut.path.startsWith(canonicalTarget.path + File.separator)) {
                    error("RMBG组件压缩包路径非法")
                }
                if (entry.isDirectory) {
                    canonicalOut.mkdirs()
                } else {
                    canonicalOut.parentFile?.mkdirs()
                    FileOutputStream(canonicalOut).use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            val read = zip.read(buffer)
                            if (read < 0) {
                                break
                            }
                            totalWritten += read.toLong()
                            if (totalWritten > RMBG_MAX_COMPONENT_ZIP_UNPACK_BYTES) {
                                error("RMBG组件压缩包过大")
                            }
                            output.write(buffer, 0, read)
                        }
                    }
                }
                zip.closeEntry()
            }
        }
    }

    private fun normalizeRmbgModelFile(dir: File) {
        val target = File(dir, RMBG_MODEL_NAME)
        if (target.isFile) {
            return
        }
        val candidate = listOf(
            File(dir, "onnx/model.onnx"),
            File(dir, "model.onnx"),
        ).firstOrNull { it.isFile && it.length() >= RMBG_MIN_MODEL_BYTES }
        candidate?.copyTo(target, overwrite = true)
    }

    private fun validateRmbgComponentDir(dir: File) {
        val model = File(dir, RMBG_MODEL_NAME)
        if (!model.isFile || model.length() < RMBG_MIN_MODEL_BYTES) {
            error("缺少 $RMBG_MODEL_NAME")
        }
    }

    private fun copyDirectory(source: File, target: File) {
        if (source.isDirectory) {
            target.mkdirs()
            source.listFiles().orEmpty().forEach { child ->
                copyDirectory(child, File(target, child.name))
            }
        } else {
            target.parentFile?.mkdirs()
            source.inputStream().use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        }
    }

    private data class RmbgComponent(
        val dir: File,
        val abi: String,
        val model: File,
    ) {
        val key: String = listOf(
            dir.absolutePath,
            abi,
            model.length(),
            model.lastModified(),
        ).joinToString("|")
    }

    private data class RmbgInferenceReport(
        val actualBackend: RmbgInferenceBackend,
        val elapsedMs: Long,
    )

    private data class RmbgModelOutput(
        val output: FloatArray,
        val report: RmbgInferenceReport,
    )

    private data class RmbgMaskResult(
        val alpha: IntArray,
        val report: RmbgInferenceReport,
    )

    private inner class DynamicRmbgRuntime(
        private val component: RmbgComponent,
    ) : AutoCloseable {
        var activeBackend: RmbgInferenceBackend = RmbgInferenceBackend.Cpu
            private set

        private val environmentClass: Class<*>
        private val environment: Any
        private var sessionOptions: Any? = null
        private var session: Any? = null
        private val tensorClass: Class<*>
        private val onnxTensorClass: Class<*>
        private val closeMethod = AutoCloseable::class.java.getMethod("close")

        init {
            val classLoader = MainActivity::class.java.classLoader ?: ClassLoader.getSystemClassLoader()
            environmentClass = classLoader.loadClass("ai.onnxruntime.OrtEnvironment")
            val sessionOptionsClass = classLoader.loadClass("ai.onnxruntime.OrtSession\$SessionOptions")
            onnxTensorClass = classLoader.loadClass("ai.onnxruntime.OnnxTensor")
            tensorClass = onnxTensorClass
            environment = environmentClass.getMethod("getEnvironment").invoke(null)
                ?: error("无法初始化 ONNX Runtime 环境")
            val created = createSessionPair(sessionOptionsClass)
            sessionOptions = created.first
            session = created.second
            activeBackend = RmbgInferenceBackend.Cpu
        }

        private fun createSessionPair(sessionOptionsClass: Class<*>): Pair<Any, Any> {
            val options = sessionOptionsClass.getConstructor().newInstance()
            try {
                configureBaseOptions(sessionOptionsClass, options)
                val createdSession = environmentClass
                    .getMethod("createSession", String::class.java, sessionOptionsClass)
                    .invoke(environment, component.model.absolutePath, options)
                    ?: error("无法创建 RMBG ONNX 会话")
                return options to createdSession
            } catch (error: InvocationTargetException) {
                runCatching { closeMethod.invoke(options) }
                throw error.targetException ?: error
            } catch (error: Throwable) {
                runCatching { closeMethod.invoke(options) }
                throw error
            }
        }

        private fun configureBaseOptions(sessionOptionsClass: Class<*>, options: Any) {
            runCatching { sessionOptionsClass.getMethod("setMemoryPatternOptimization", Boolean::class.javaPrimitiveType).invoke(options, false) }
            runCatching { sessionOptionsClass.getMethod("setCPUArenaAllocator", Boolean::class.javaPrimitiveType).invoke(options, false) }
            runCatching { sessionOptionsClass.getMethod("setIntraOpNumThreads", Int::class.javaPrimitiveType).invoke(options, 1) }
            runCatching { sessionOptionsClass.getMethod("setInterOpNumThreads", Int::class.javaPrimitiveType).invoke(options, 1) }
        }

        @Suppress("UNCHECKED_CAST")
        fun run(input: FloatBuffer, shape: LongArray): FloatArray {
            val activeSession = session ?: error("RMBG ONNX 会话未初始化")
            val tensor = tensorClass
                .getMethod("createTensor", environmentClass, FloatBuffer::class.java, LongArray::class.java)
                .invoke(null, environment, input, shape)
            try {
                val inputNames = activeSession.javaClass.getMethod("getInputNames").invoke(activeSession) as Set<String>
                val feeds = mapOf(inputNames.first() to tensor)
                val runMethod = activeSession.javaClass.getMethod("run", Map::class.java)
                val result = try {
                    runMethod.invoke(activeSession, feeds)
                } catch (error: InvocationTargetException) {
                    throw error.targetException ?: error
                }
                try {
                    val outputTensor = result.javaClass.getMethod("get", Int::class.javaPrimitiveType).invoke(result, 0)
                    val buffer = onnxTensorClass.getMethod("getFloatBuffer").invoke(outputTensor) as FloatBuffer
                    buffer.rewind()
                    return FloatArray(buffer.remaining()).also { buffer.get(it) }
                } finally {
                    closeMethod.invoke(result)
                }
            } finally {
                closeMethod.invoke(tensor)
            }
        }

        override fun close() {
            runCatching { session?.let { closeMethod.invoke(it) } }
            runCatching { sessionOptions?.let { closeMethod.invoke(it) } }
            session = null
            sessionOptions = null
        }
    }

    private fun runRmbgModel(component: RmbgComponent, input: FloatBuffer, shape: LongArray): RmbgModelOutput =
        synchronized(this) {
            runCatching { rmbgRuntime?.close() }
            rmbgRuntime = null
            val startedAt = System.nanoTime()
            var runtime: DynamicRmbgRuntime? = null
            try {
                input.rewind()
                runtime = DynamicRmbgRuntime(component)
                rmbgRuntime = runtime
                val output = runtime.run(input, shape)
                RmbgModelOutput(
                    output = output,
                    report = RmbgInferenceReport(
                        actualBackend = runtime.activeBackend,
                        elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt),
                    ),
                )
            } catch (error: Throwable) {
                runCatching { runtime?.close() }
                rmbgRuntime = null
                throw error
            } finally {
                runCatching { runtime?.close() }
                rmbgRuntime = null
            }
        }

    private fun runRmbgAlphaMask(sourceIcon: Bitmap, component: RmbgComponent): RmbgMaskResult {
        val inputSize = DEFAULT_RMBG_INPUT_SIZE
        val modelInput = resizeBitmap(sourceIcon, inputSize, inputSize)
        val inputPixels = IntArray(inputSize * inputSize)
        modelInput.getPixels(inputPixels, 0, inputSize, 0, 0, inputSize, inputSize)
        val input = FloatBuffer.allocate(inputSize * inputSize * 3)
        for (channel in 0..2) {
            val mean = RMBG_NORMALIZE_MEAN[channel]
            val std = RMBG_NORMALIZE_STD[channel]
            for (pixel in inputPixels) {
                val value = when (channel) {
                    0 -> AndroidColor.red(pixel)
                    1 -> AndroidColor.green(pixel)
                    else -> AndroidColor.blue(pixel)
                }
                input.put(((value / 255.0f) - mean) / std)
            }
        }
        input.rewind()

        val modelOutput = runRmbgModel(component, input, longArrayOf(1L, 3L, inputSize.toLong(), inputSize.toLong()))
        val output = modelOutput.output
        if (output.isEmpty()) {
            error("RMBG输出为空")
        }
        val outputSide = kotlin.math.sqrt(output.size.toDouble()).roundToInt()
        if (outputSide <= 0 || outputSide * outputSide != output.size) {
            error("RMBG输出尺寸异常: ${output.size}")
        }
        var min = Float.POSITIVE_INFINITY
        var max = Float.NEGATIVE_INFINITY
        output.forEach { value ->
            if (value < min) min = value
            if (value > max) max = value
        }
        val range = max - min
        if (range <= 0.000001f) {
            error("RMBG输出无有效 Alpha 范围")
        }
        val scaledPixels = IntArray(sourceIcon.width * sourceIcon.height)
        val scaleX = outputSide.toFloat() / sourceIcon.width.toFloat()
        val scaleY = outputSide.toFloat() / sourceIcon.height.toFloat()
        for (y in 0 until sourceIcon.height) {
            val sourceY = ((y + 0.5f) * scaleY - 0.5f).coerceIn(0f, (outputSide - 1).toFloat())
            val y0 = sourceY.toInt().coerceIn(0, outputSide - 1)
            val y1 = (y0 + 1).coerceIn(0, outputSide - 1)
            val yRatio = sourceY - y0.toFloat()
            val row0 = y0 * outputSide
            val row1 = y1 * outputSide
            val outOffset = y * sourceIcon.width
            for (x in 0 until sourceIcon.width) {
                val sourceX = ((x + 0.5f) * scaleX - 0.5f).coerceIn(0f, (outputSide - 1).toFloat())
                val x0 = sourceX.toInt().coerceIn(0, outputSide - 1)
                val x1 = (x0 + 1).coerceIn(0, outputSide - 1)
                val xRatio = sourceX - x0.toFloat()
                val top = output[row0 + x0] * (1f - xRatio) + output[row0 + x1] * xRatio
                val bottom = output[row1 + x0] * (1f - xRatio) + output[row1 + x1] * xRatio
                val value = top * (1f - yRatio) + bottom * yRatio
                scaledPixels[outOffset + x] = (((value - min) / range) * 255.0f)
                    .roundToInt()
                    .coerceIn(0, 255)
            }
        }
        return RmbgMaskResult(alpha = scaledPixels, report = modelOutput.report)
    }

    private fun tuneRmbgAlpha(alpha: IntArray, width: Int, height: Int): IntArray {
        if (alpha.size != width * height || width <= 0 || height <= 0) {
            return alpha.copyOf()
        }
        var current = alpha.copyOf()
        val strength = rmbgAlphaStrengthPercent.coerceIn(
            MIN_RMBG_ALPHA_STRENGTH_PERCENT,
            MAX_RMBG_ALPHA_STRENGTH_PERCENT,
        )
        if (strength != DEFAULT_RMBG_ALPHA_STRENGTH_PERCENT) {
            val gamma = DEFAULT_RMBG_ALPHA_STRENGTH_PERCENT.toDouble() / strength.toDouble()
            for (i in current.indices) {
                val normalized = current[i].coerceIn(0, 255).toDouble() / 255.0
                current[i] = (normalized.pow(gamma) * 255.0)
                    .roundToInt()
                    .coerceIn(0, 255)
            }
        }

        val adjust = rmbgEdgeAdjustPercent.coerceIn(
            MIN_RMBG_EDGE_ADJUST_PERCENT,
            MAX_RMBG_EDGE_ADJUST_PERCENT,
        ) - DEFAULT_RMBG_EDGE_ADJUST_PERCENT
        if (adjust != 0) {
            val radius = ((abs(adjust) / 50.0) * RMBG_EDGE_ADJUST_MAX_RADIUS)
                .roundToInt()
                .coerceIn(1, RMBG_EDGE_ADJUST_MAX_RADIUS)
            val morphed = morphRmbgAlpha(current, width, height, expand = adjust > 0, radius = radius)
            val blend = (abs(adjust).toDouble() / DEFAULT_RMBG_EDGE_ADJUST_PERCENT.toDouble())
                .coerceIn(0.0, 1.0)
            for (i in current.indices) {
                current[i] = (current[i] * (1.0 - blend) + morphed[i] * blend)
                    .roundToInt()
                    .coerceIn(0, 255)
            }
        }

        val feather = ratioPercent(rmbgEdgeFeatherPercent.coerceIn(
            MIN_RMBG_EDGE_FEATHER_PERCENT,
            MAX_RMBG_EDGE_FEATHER_PERCENT,
        ))
        if (feather > 0.0) {
            val radius = if (rmbgEdgeFeatherPercent >= 70) 2 else 1
            current = featherRmbgAlphaEdges(current, width, height, strength = feather, radius = radius)
        }

        val weakKeep = ratioPercent(rmbgWeakAlphaKeepPercent.coerceIn(
            MIN_RMBG_WEAK_ALPHA_KEEP_PERCENT,
            MAX_RMBG_WEAK_ALPHA_KEEP_PERCENT,
        ))
        if (weakKeep < 1.0) {
            val lowCut = lerpDouble(RMBG_WEAK_ALPHA_MAX_CUT.toDouble(), 0.0, weakKeep)
                .roundToInt()
                .coerceIn(0, 254)
            if (lowCut > 0) {
                val range = (255 - lowCut).coerceAtLeast(1)
                for (i in current.indices) {
                    val value = current[i].coerceIn(0, 255)
                    current[i] = if (value <= lowCut) {
                        0
                    } else {
                        (((value - lowCut).toDouble() / range.toDouble()) * 255.0)
                            .roundToInt()
                            .coerceIn(0, 255)
                    }
                }
            }
        }
        return current
    }

    private fun morphRmbgAlpha(
        alpha: IntArray,
        width: Int,
        height: Int,
        expand: Boolean,
        radius: Int,
    ): IntArray {
        val out = IntArray(alpha.size)
        val safeRadius = radius.coerceAtLeast(1)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var selected = if (expand) 0 else 255
                for (dy in -safeRadius..safeRadius) {
                    for (dx in -safeRadius..safeRadius) {
                        val nx = x + dx
                        val ny = y + dy
                        val value = if (nx in 0 until width && ny in 0 until height) {
                            alpha[ny * width + nx].coerceIn(0, 255)
                        } else {
                            0
                        }
                        selected = if (expand) {
                            maxOf(selected, value)
                        } else {
                            minOf(selected, value)
                        }
                    }
                }
                out[y * width + x] = selected
            }
        }
        return out
    }

    private fun featherRmbgAlphaEdges(
        alpha: IntArray,
        width: Int,
        height: Int,
        strength: Double,
        radius: Int,
    ): IntArray {
        val out = alpha.copyOf()
        val safeRadius = radius.coerceAtLeast(1)
        val blend = strength.coerceIn(0.0, 1.0)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                var sum = 0
                var count = 0
                var minAlpha = 255
                var maxAlpha = 0
                for (dy in -safeRadius..safeRadius) {
                    for (dx in -safeRadius..safeRadius) {
                        val nx = x + dx
                        val ny = y + dy
                        val value = if (nx in 0 until width && ny in 0 until height) {
                            alpha[ny * width + nx].coerceIn(0, 255)
                        } else {
                            0
                        }
                        sum += value
                        count++
                        minAlpha = minOf(minAlpha, value)
                        maxAlpha = maxOf(maxAlpha, value)
                    }
                }
                if (count <= 0 || maxAlpha - minAlpha < RMBG_EDGE_FEATHER_MIN_ALPHA_DELTA) {
                    continue
                }
                val average = sum.toDouble() / count.toDouble()
                out[index] = (alpha[index] * (1.0 - blend) + average * blend)
                    .roundToInt()
                    .coerceIn(0, 255)
            }
        }
        return out
    }

    private fun applyAlphaArrayToSource(source: Bitmap, alpha: IntArray): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val outPixels = IntArray(pixels.size)
        for (i in pixels.indices) {
            val outAlpha = alpha.getOrElse(i) { 0 }.coerceIn(0, 255)
            outPixels[i] = if (outAlpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                AndroidColor.TRANSPARENT
            } else {
                AndroidColor.argb(
                    outAlpha,
                    AndroidColor.red(pixels[i]),
                    AndroidColor.green(pixels[i]),
                    AndroidColor.blue(pixels[i]),
                )
            }
        }
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return repairTransparentEdgeColors(out)
    }

    private fun medianEdgeColor(pixels: IntArray, width: Int, height: Int): Int {
        val reds = mutableListOf<Int>()
        val greens = mutableListOf<Int>()
        val blues = mutableListOf<Int>()
        fun add(pixel: Int) {
            if (AndroidColor.alpha(pixel) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                return
            }
            reds += AndroidColor.red(pixel)
            greens += AndroidColor.green(pixel)
            blues += AndroidColor.blue(pixel)
        }
        for (x in 0 until width) {
            add(pixels[x])
            add(pixels[(height - 1) * width + x])
        }
        for (y in 0 until height) {
            add(pixels[y * width])
            add(pixels[y * width + width - 1])
        }
        return AndroidColor.rgb(median(reds), median(greens), median(blues))
    }

    private fun dominantMaskColor(pixels: IntArray, mask: BooleanArray): Int {
        val reds = mutableListOf<Int>()
        val greens = mutableListOf<Int>()
        val blues = mutableListOf<Int>()
        for (i in pixels.indices) {
            if (!mask[i]) {
                continue
            }
            val pixel = pixels[i]
            if (saturation(pixel) < TWO_LAYER_DOMINANT_MIN_SATURATION || luma(pixel) > TWO_LAYER_DOMINANT_MAX_LUMA) {
                continue
            }
            reds += AndroidColor.red(pixel)
            greens += AndroidColor.green(pixel)
            blues += AndroidColor.blue(pixel)
        }
        if (reds.isEmpty()) {
            for (i in pixels.indices) {
                if (mask[i]) {
                    val pixel = pixels[i]
                    reds += AndroidColor.red(pixel)
                    greens += AndroidColor.green(pixel)
                    blues += AndroidColor.blue(pixel)
                }
            }
        }
        return AndroidColor.rgb(median(reds), median(greens), median(blues))
    }

    private fun medianVisibleColor(source: Bitmap): Int {
        val pixels = IntArray(source.width * source.height)
        source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
        val reds = mutableListOf<Int>()
        val greens = mutableListOf<Int>()
        val blues = mutableListOf<Int>()
        for (pixel in pixels) {
            if (AndroidColor.alpha(pixel) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                continue
            }
            reds += AndroidColor.red(pixel)
            greens += AndroidColor.green(pixel)
            blues += AndroidColor.blue(pixel)
        }
        if (reds.isEmpty()) {
            return AndroidColor.WHITE
        }
        return AndroidColor.rgb(median(reds), median(greens), median(blues))
    }

    private fun medianVisibleColorInRect(source: Bitmap, left: Int, top: Int, right: Int, bottom: Int): Int {
        val reds = mutableListOf<Int>()
        val greens = mutableListOf<Int>()
        val blues = mutableListOf<Int>()
        val safeLeft = left.coerceIn(0, source.width)
        val safeTop = top.coerceIn(0, source.height)
        val safeRight = right.coerceIn(safeLeft, source.width)
        val safeBottom = bottom.coerceIn(safeTop, source.height)
        for (y in safeTop until safeBottom) {
            for (x in safeLeft until safeRight) {
                val pixel = source.getPixel(x, y)
                if (AndroidColor.alpha(pixel) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                    continue
                }
                reds += AndroidColor.red(pixel)
                greens += AndroidColor.green(pixel)
                blues += AndroidColor.blue(pixel)
            }
        }
        if (reds.isEmpty()) {
            return medianVisibleColor(source)
        }
        return AndroidColor.rgb(median(reds), median(greens), median(blues))
    }

    private fun medianColor(colors: IntArray): Int {
        val reds = mutableListOf<Int>()
        val greens = mutableListOf<Int>()
        val blues = mutableListOf<Int>()
        colors.forEach { color ->
            reds += AndroidColor.red(color)
            greens += AndroidColor.green(color)
            blues += AndroidColor.blue(color)
        }
        return AndroidColor.rgb(median(reds), median(greens), median(blues))
    }

    private fun lerpColor(start: Int, end: Int, amount: Double): Int {
        val t = amount.coerceIn(0.0, 1.0)
        fun channel(a: Int, b: Int): Int =
            (a + (b - a) * t).roundToInt().coerceIn(0, 255)
        return AndroidColor.rgb(
            channel(AndroidColor.red(start), AndroidColor.red(end)),
            channel(AndroidColor.green(start), AndroidColor.green(end)),
            channel(AndroidColor.blue(start), AndroidColor.blue(end)),
        )
    }

    private fun maskColorStd(pixels: IntArray, mask: BooleanArray): Double {
        var count = 0
        var redSum = 0.0
        var greenSum = 0.0
        var blueSum = 0.0
        for (i in pixels.indices) {
            if (!mask[i]) {
                continue
            }
            val pixel = pixels[i]
            redSum += AndroidColor.red(pixel)
            greenSum += AndroidColor.green(pixel)
            blueSum += AndroidColor.blue(pixel)
            count++
        }
        if (count == 0) {
            return Double.MAX_VALUE
        }
        val redMean = redSum / count
        val greenMean = greenSum / count
        val blueMean = blueSum / count
        var redVar = 0.0
        var greenVar = 0.0
        var blueVar = 0.0
        for (i in pixels.indices) {
            if (!mask[i]) {
                continue
            }
            val pixel = pixels[i]
            redVar += (AndroidColor.red(pixel) - redMean) * (AndroidColor.red(pixel) - redMean)
            greenVar += (AndroidColor.green(pixel) - greenMean) * (AndroidColor.green(pixel) - greenMean)
            blueVar += (AndroidColor.blue(pixel) - blueMean) * (AndroidColor.blue(pixel) - blueMean)
        }
        return (kotlin.math.sqrt(redVar / count) +
            kotlin.math.sqrt(greenVar / count) +
            kotlin.math.sqrt(blueVar / count)) / 3.0
    }

    private fun largestConnectedMask(mask: BooleanArray, width: Int, height: Int): BooleanArray {
        val largest = connectedMaskComponents(mask, width, height).maxByOrNull { it.size }
            ?: return BooleanArray(mask.size)
        val out = BooleanArray(mask.size)
        largest.indices.forEach { out[it] = true }
        return out
    }

    private fun connectedMaskComponents(mask: BooleanArray, width: Int, height: Int): List<MaskComponent> {
        val seen = BooleanArray(mask.size)
        val components = mutableListOf<MaskComponent>()
        val queue = ArrayDeque<Int>()
        for (index in mask.indices) {
            if (!mask[index] || seen[index]) {
                continue
            }
            val indices = mutableListOf<Int>()
            var touchesEdge = false
            var left = width
            var top = height
            var right = -1
            var bottom = -1
            seen[index] = true
            queue.add(index)
            while (!queue.isEmpty()) {
                val current = queue.removeFirst()
                indices += current
                val x = current % width
                val y = current / width
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    touchesEdge = true
                }
                if (x < left) left = x
                if (x > right) right = x
                if (y < top) top = y
                if (y > bottom) bottom = y
                fun enqueue(nx: Int, ny: Int) {
                    if (nx !in 0 until width || ny !in 0 until height) {
                        return
                    }
                    val next = ny * width + nx
                    if (mask[next] && !seen[next]) {
                        seen[next] = true
                        queue.add(next)
                    }
                }
                enqueue(x + 1, y)
                enqueue(x - 1, y)
                enqueue(x, y + 1)
                enqueue(x, y - 1)
            }
            components += MaskComponent(
                indices = indices.toIntArray(),
                touchesEdge = touchesEdge,
                bounds = Bounds(left, top, right + 1, bottom + 1),
            )
        }
        return components
    }

    private fun dilateMask(mask: BooleanArray, width: Int, height: Int, radius: Int): BooleanArray {
        if (radius <= 0) {
            return mask.copyOf()
        }
        val out = BooleanArray(mask.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (!mask[y * width + x]) {
                    continue
                }
                for (dy in -radius..radius) {
                    for (dx in -radius..radius) {
                        val nx = x + dx
                        val ny = y + dy
                        if (nx in 0 until width && ny in 0 until height) {
                            out[ny * width + nx] = true
                        }
                    }
                }
            }
        }
        return out
    }

    private fun erodeMask(mask: BooleanArray, width: Int, height: Int, radius: Int): BooleanArray {
        if (radius <= 0) {
            return mask.copyOf()
        }
        val out = BooleanArray(mask.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                if (!mask[index]) {
                    continue
                }
                var keep = true
                for (dy in -radius..radius) {
                    for (dx in -radius..radius) {
                        val nx = x + dx
                        val ny = y + dy
                        if (nx !in 0 until width || ny !in 0 until height || !mask[ny * width + nx]) {
                            keep = false
                            break
                        }
                    }
                    if (!keep) {
                        break
                    }
                }
                out[index] = keep
            }
        }
        return out
    }

    private fun applyMaskToSource(pixels: IntArray, width: Int, height: Int, mask: BooleanArray): Bitmap {
        val outPixels = IntArray(pixels.size)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            outPixels[i] = if (mask[i]) {
                AndroidColor.argb(
                    255,
                    AndroidColor.red(pixel),
                    AndroidColor.green(pixel),
                    AndroidColor.blue(pixel),
                )
            } else {
                AndroidColor.TRANSPARENT
            }
        }
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return repairTransparentEdgeColors(out)
    }

    private fun applyMaskToSolidColor(width: Int, height: Int, mask: BooleanArray, color: Int): Bitmap {
        val outPixels = IntArray(width * height)
        for (i in outPixels.indices) {
            outPixels[i] = if (mask[i]) {
                AndroidColor.argb(
                    255,
                    AndroidColor.red(color),
                    AndroidColor.green(color),
                    AndroidColor.blue(color),
                )
            } else {
                AndroidColor.TRANSPARENT
            }
        }
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return repairTransparentEdgeColors(out)
    }

    private fun fillMaskOnSource(
        pixels: IntArray,
        width: Int,
        height: Int,
        mask: BooleanArray,
        color: Int,
    ): Bitmap {
        val outPixels = pixels.copyOf()
        val fill = AndroidColor.rgb(
            AndroidColor.red(color),
            AndroidColor.green(color),
            AndroidColor.blue(color),
        )
        for (i in outPixels.indices) {
            if (mask[i]) {
                outPixels[i] = AndroidColor.argb(255, AndroidColor.red(fill), AndroidColor.green(fill), AndroidColor.blue(fill))
            } else if (AndroidColor.alpha(outPixels[i]) < 255) {
                outPixels[i] = AndroidColor.argb(
                    255,
                    AndroidColor.red(outPixels[i]),
                    AndroidColor.green(outPixels[i]),
                    AndroidColor.blue(outPixels[i]),
                )
            }
        }
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun prepareOriginalForeground(source: Bitmap): Bitmap =
        when (originalForegroundCleanupMode) {
            OriginalForegroundCleanupMode.Off -> source
            OriginalForegroundCleanupMode.Plate -> removeForegroundPlate(source).bitmap
            OriginalForegroundCleanupMode.Auto -> {
                if (hasLayeredLightForegroundPlate(source)) {
                    source
                } else {
                    val cleaned = removeForegroundPlate(source)
                    if (cleaned.changed && shouldUseOriginalSafetyCleanup(source, cleaned.bitmap)) {
                        cleaned.bitmap
                    } else {
                        source
                    }
                }
            }
        }

    private fun shouldUseOriginalSafetyCleanup(source: Bitmap, cleaned: Bitmap): Boolean {
        val sourceCoverage = alphaCoverage(source)
        val cleanedCoverage = alphaCoverage(cleaned)
        if (cleanedCoverage <= 0.0 || cleanedCoverage >= sourceCoverage - ORIGINAL_CLEANUP_MIN_COVERAGE_DROP) {
            return false
        }
        if (cleanedCoverage < ORIGINAL_CLEANUP_MIN_REMAINING_COVERAGE) {
            return false
        }
        val sourceBounds = alphaBounds(source, LOCAL_ALPHA_VISIBLE_THRESHOLD) ?: return false
        val cleanedBounds = alphaBounds(cleaned, ORIGINAL_CLEANUP_ALPHA_BOUNDS_THRESHOLD)
            ?: alphaBounds(cleaned, LOCAL_ALPHA_VISIBLE_THRESHOLD)
            ?: return false
        val sourceMax = maxOf(sourceBounds.width(), sourceBounds.height()).toDouble()
        val cleanedMax = maxOf(cleanedBounds.width(), cleanedBounds.height()).toDouble()
        if (sourceMax <= 0.0) {
            return false
        }
        return cleanedMax / sourceMax >= ORIGINAL_CLEANUP_MIN_BOUNDS_RATIO
    }

    private fun hasLayeredLightForegroundPlate(source: Bitmap): Boolean {
        if (alphaCoverage(source) < ADAPTIVE_DIRECT_FULL_PLATE_COVERAGE) {
            return false
        }
        val pixels = IntArray(source.width * source.height)
        source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
        var visible = 0
        var lightPlate = 0
        var darkDetail = 0
        for (pixel in pixels) {
            if (AndroidColor.alpha(pixel) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                continue
            }
            visible++
            val saturation = saturation(pixel)
            val luma = luma(pixel)
            if (luma >= ADAPTIVE_DIRECT_PLATE_MIN_LUMA && saturation <= ADAPTIVE_DIRECT_PLATE_MAX_SATURATION) {
                lightPlate++
            } else if (luma <= ADAPTIVE_DIRECT_DETAIL_MAX_LUMA) {
                darkDetail++
            }
        }
        if (visible == 0) {
            return false
        }
        return lightPlate.toDouble() / visible.toDouble() >= ADAPTIVE_DIRECT_PLATE_MIN_RATIO &&
            darkDetail.toDouble() / visible.toDouble() >= ADAPTIVE_DIRECT_DETAIL_MIN_RATIO
    }

    private fun defaultLocalPreviewChoice(autoChoice: PreviewChoice): PreviewChoice =
        defaultPreviewChoiceForMode(LocalSeparationMode.Auto, autoChoice)

    private fun defaultPreviewChoiceForMode(mode: LocalSeparationMode, autoChoice: PreviewChoice): PreviewChoice =
        when (mode) {
            LocalSeparationMode.Original -> PreviewChoice.Original
            LocalSeparationMode.Plate -> PreviewChoice.Full
            LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
            LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
            LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
            LocalSeparationMode.Auto -> autoChoice
            LocalSeparationMode.Full -> PreviewChoice.Full
        }

    private fun chooseAutoLocalChoice(
        original: Bitmap,
        cleanup: Bitmap,
        twoLayer: CandidateBuildResult?,
        rmbg: CandidateBuildResult?,
    ): PreviewChoice {
        val originalCoverage = meaningfulAlphaCoverage(original)
        val cleanupCoverage = meaningfulAlphaCoverage(cleanup)
        val cleanupUsable = isAutoLocalCandidateUsable(
            candidate = cleanup,
            originalCoverage = originalCoverage,
            candidateCoverage = cleanupCoverage,
        )
        if (twoLayer?.autoUsable == true) {
            return PreviewChoice.TwoLayer
        }
        if (
            rmbg?.autoUsable == true &&
            rmbg.coverage < cleanupCoverage - AUTO_COVERAGE_CHANGE_THRESHOLD
        ) {
            return PreviewChoice.Rmbg
        }
        return when {
            cleanupUsable -> PreviewChoice.Full
            else -> PreviewChoice.Original
        }
    }

    private fun isAutoLocalCandidateUsable(
        candidate: Bitmap,
        originalCoverage: Double,
        candidateCoverage: Double,
    ): Boolean {
        if (candidateCoverage >= originalCoverage - AUTO_COVERAGE_CHANGE_THRESHOLD) {
            return false
        }
        val bounds = meaningfulAlphaBounds(candidate) ?: return false
        return !hasAutoCropRisk(bounds, candidate.width, candidate.height)
    }

    private fun hasAutoCropRisk(bounds: Bounds, width: Int, height: Int): Boolean {
        val margin = AUTO_EDGE_TOUCH_MARGIN_PX
        val touchesLeft = bounds.left <= margin
        val touchesTop = bounds.top <= margin
        val touchesRight = bounds.right >= width - margin
        val touchesBottom = bounds.bottom >= height - margin
        val touchCount = listOf(touchesLeft, touchesTop, touchesRight, touchesBottom).count { it }
        return touchCount >= AUTO_EDGE_TOUCH_COUNT_LIMIT ||
            (touchesLeft && touchesRight) ||
            (touchesTop && touchesBottom)
    }

    private fun writePackageOutputs(session: GenerationSession, selections: PreviewSelections) {
        val light = candidateWithCustomOverrides(session, PreviewMode.NormalLight, selections.normalLight)
        val lightBaseRecfg = renderCandidateForegroundBase(light)
        val lightRecfg = foregroundForSize(lightBaseRecfg, SIZE_1X1, SIZE_1X1)
        val lightBaseRecbg = light.recbg
        val lightRecbg = liquidGlassBackgroundForSize(lightBaseRecbg, SIZE_1X1, SIZE_1X1)
        savePng(lightRecbg, File(session.outDir, "recbg.png"))
        savePng(lightRecfg, File(session.outDir, "recfg.png"))
        val recbg1x2 = liquidGlassBackgroundForSize(lightBaseRecbg, SIZE_1X2[0], SIZE_1X2[1])
        val recbg2x1 = liquidGlassBackgroundForSize(lightBaseRecbg, SIZE_2X1[0], SIZE_2X1[1])
        val recbg2x2 = liquidGlassBackgroundForSize(lightBaseRecbg, SIZE_2X2, SIZE_2X2)
        savePng(recbg1x2, File(session.outDir, "recbg_1x2.png"))
        savePng(recbg2x1, File(session.outDir, "recbg_2x1.png"))
        savePng(recbg2x2, File(session.outDir, "recbg_2x2.png"))

        val recfg1x2 = foregroundForSize(lightBaseRecfg, SIZE_1X2[0], SIZE_1X2[1])
        val recfg2x1 = foregroundForSize(lightBaseRecfg, SIZE_2X1[0], SIZE_2X1[1])
        val recfg2x2 = foregroundForSize(lightBaseRecfg, SIZE_2X2, SIZE_2X2)
        savePng(recfg1x2, File(session.outDir, "recfg_1x2.png"))
        savePng(recfg2x1, File(session.outDir, "recfg_2x1.png"))
        savePng(recfg2x2, File(session.outDir, "recfg_2x2.png"))

        val night = candidateWithCustomOverrides(session, PreviewMode.NormalDark, selections.normalDark)
        val nightBaseRecfg = renderCandidateForegroundBase(night)
        val nightRecfg = foregroundForSize(nightBaseRecfg, SIZE_1X1, SIZE_1X1)
        val nightBaseRecbg = night.recbg
        val nightRecbg = liquidGlassBackgroundForSize(nightBaseRecbg, SIZE_1X1, SIZE_1X1)
        val nightRecfg1x2 = foregroundForSize(nightBaseRecfg, SIZE_1X2[0], SIZE_1X2[1])
        val nightRecfg2x1 = foregroundForSize(nightBaseRecfg, SIZE_2X1[0], SIZE_2X1[1])
        val nightRecfg2x2 = foregroundForSize(nightBaseRecfg, SIZE_2X2, SIZE_2X2)
        val nightRecbg1x2 = liquidGlassBackgroundForSize(nightBaseRecbg, SIZE_1X2[0], SIZE_1X2[1])
        val nightRecbg2x1 = liquidGlassBackgroundForSize(nightBaseRecbg, SIZE_2X1[0], SIZE_2X1[1])
        val nightRecbg2x2 = liquidGlassBackgroundForSize(nightBaseRecbg, SIZE_2X2, SIZE_2X2)
        savePng(normalDarkForeground(nightRecfg, nightRecbg), File(session.outDir, "rec_night.png"))
        savePng(
            normalDarkForeground(nightRecfg1x2, nightRecbg1x2),
            File(session.outDir, "rec_night_1x2.png"),
        )
        savePng(
            normalDarkForeground(nightRecfg2x1, nightRecbg2x1),
            File(session.outDir, "rec_night_2x1.png"),
        )
        savePng(
            normalDarkForeground(nightRecfg2x2, nightRecbg2x2),
            File(session.outDir, "rec_night_2x2.png"),
        )

        val rawMonochromeLight = monochromeForCandidate(
            candidateWithCustomOverrides(session, PreviewMode.MonochromeLight, selections.monochromeLight),
            invertLuma = true,
        )
        val rawMonochromeDark = monochromeForCandidate(
            candidateWithCustomOverrides(session, PreviewMode.MonochromeDark, selections.monochromeDark),
            invertLuma = false,
        )
        val monochromeLight = scaleMonochromeForTheme(rawMonochromeLight)
        val monochromeDark = scaleMonochromeForTheme(rawMonochromeDark)
        savePng(monochromeLight, File(session.outDir, "monochrome_light.png"))
        savePng(monochromeDark, File(session.outDir, "monochrome_dark.png"))
        savePng(monochromeDark, File(session.outDir, "monochrome.png"))
        savePng(centerOnCanvas(rawMonochromeDark, SIZE_1X2[0], SIZE_1X2[1]), File(session.outDir, "monochrome_1x2.png"))
        savePng(centerOnCanvas(rawMonochromeDark, SIZE_2X1[0], SIZE_2X1[1]), File(session.outDir, "monochrome_2x1.png"))
        savePng(centerOnCanvas(rawMonochromeDark, SIZE_2X2, SIZE_2X2), File(session.outDir, "monochrome_2x2.png"))

        savePng(adjustColor(lightRecfg, 1.3f, 1.0f), File(session.outDir, "day.png"))
        savePng(adjustColor(lightRecfg, 0.9f, 0.9f), File(session.outDir, "nsd.png"))
        savePng(adjustColor(lightRecfg, 0.9f, 1.05f), File(session.outDir, "mat.png"))
        savePng(adjustColor(lightRecfg, 0.7f, 0.95f), File(session.outDir, "peb.png"))
    }

    private fun liquidGlassBackgroundForSize(
        source: Bitmap,
        width: Int,
        height: Int,
        forceLiquidGlass: Boolean = false,
    ): Bitmap {
        val resized = if (source.width == width && source.height == height) {
            source
        } else {
            resizeBitmap(source, width, height)
        }
        return if (forceLiquidGlass || liquidGlassEnabled) {
            renderLayeredLiquidGlassBackground(resized)
        } else {
            resized
        }
    }

    private fun foregroundForSize(
        source: Bitmap,
        width: Int,
        height: Int,
        forceLiquidGlass: Boolean = false,
    ): Bitmap {
        val sized = if (source.width == width && source.height == height) {
            source
        } else {
            centerOnCanvas(source, width, height)
        }
        return if (forceLiquidGlass || liquidGlassEnabled) {
            renderLayeredLiquidGlassForeground(sized)
        } else {
            applyForegroundShadow(sized)
        }
    }

    private fun renderLayeredLiquidGlassBackground(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val radius = liquidGlassRadiusForSize(width, height)
        val shapeMask = roundedRectMaskAlpha(width, height, radius, feather = liquidGlassMaskFeather(width, height))
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(AndroidColor.TRANSPARENT)
        canvas.drawBitmap(source, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))

        val mistAlpha = liquidGlassBackgroundMistAlpha.coerceIn(MIN_LIQUID_GLASS_MIST_ALPHA, MAX_LIQUID_GLASS_MIST_ALPHA)
        if (mistAlpha > 0) {
            canvas.drawRect(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.argb(mistAlpha, 0, 0, 0)
                },
            )
        }
        drawLayeredLiquidGlassLight(canvas, width, height, radius)
        return applyAlphaMask(out, shapeMask)
    }

    private fun renderLayeredLiquidGlassForeground(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val radius = liquidGlassRadiusForSize(width, height)
        val shapeMask = roundedRectMaskAlpha(width, height, radius, feather = liquidGlassMaskFeather(width, height))
        val subject = scaleBitmapAroundCanvasCenter(
            source,
            liquidGlassSubjectScalePercent
                .coerceIn(MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT, MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT)
                .toFloat() / 100f,
        )
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(AndroidColor.TRANSPARENT)

        val subjectShadowAlpha = liquidGlassSubjectShadowAlpha
            .coerceIn(MIN_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA, MAX_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA)
        if (subjectShadowAlpha > 0) {
            val minSide = minOf(width, height).coerceAtLeast(1)
            val params = ForegroundShadowParams(
                alpha = subjectShadowAlpha,
                blurRadius = minSide * 0.026f,
                offsetX = 0,
                offsetY = (minSide * 0.018f).roundToInt().coerceAtLeast(1),
                spread = 0,
            )
            val shadow = subjectShadowBitmap(subject, params)
            canvas.drawBitmap(shadow, params.offsetX.toFloat(), params.offsetY.toFloat(), Paint(Paint.FILTER_BITMAP_FLAG))
        }

        val outlineWidth = liquidGlassScaledWidth(
            width,
            height,
            liquidGlassSubjectOutlineWidth.coerceIn(
                MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
            ),
        )
        if (outlineWidth > 0) {
            canvas.drawBitmap(
                subjectOutlineLayer(subject, outlineWidth, inner = false, alphaScale = 0.92f),
                0f,
                0f,
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
            )
        }

        val innerOutlineWidth = liquidGlassScaledWidth(
            width,
            height,
            liquidGlassSubjectInnerOutlineWidth.coerceIn(
                MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
            ),
        )
        if (innerOutlineWidth > 0) {
            canvas.drawBitmap(
                subjectOutlineLayer(subject, innerOutlineWidth, inner = true, alphaScale = 0.76f),
                0f,
                0f,
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
            )
        }

        val subjectOpacity = liquidGlassSubjectOpacityPercent
            .coerceIn(MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT, MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT)
        if (subjectOpacity > 0) {
            canvas.drawBitmap(
                subject,
                0f,
                0f,
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    alpha = (subjectOpacity * 255f / 100f).roundToInt().coerceIn(0, 255)
                },
            )
        }
        return applyAlphaMask(out, shapeMask)
    }

    private fun drawLayeredLiquidGlassLight(canvas: Canvas, width: Int, height: Int, radius: Float) {
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val bottom = height.toFloat()
        val topAlpha = liquidGlassTopAlpha.coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)
        val bottomAlpha = liquidGlassBottomAlpha.coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)

        canvas.drawRoundRect(
            rect,
            radius,
            radius,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                shader = LinearGradient(
                    0f,
                    0f,
                    0f,
                    bottom,
                    intArrayOf(
                        whiteWithAlpha(14f),
                        whiteWithAlpha(0f),
                        whiteWithAlpha(0f),
                        whiteWithAlpha(bottomAlpha * 0.16f),
                    ),
                    floatArrayOf(0f, 0.35f, 0.70f, 1f),
                    Shader.TileMode.CLAMP,
                )
            },
        )

        val bottomDarkAlpha = liquidGlassBottomDarkAlpha
            .coerceIn(MIN_LIQUID_GLASS_BOTTOM_DARK_ALPHA, MAX_LIQUID_GLASS_BOTTOM_DARK_ALPHA)
        if (bottomDarkAlpha > 0) {
            canvas.drawRoundRect(
                rect,
                radius,
                radius,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    shader = LinearGradient(
                        0f,
                        bottom - height * 0.38f,
                        0f,
                        bottom,
                        intArrayOf(
                            blackWithAlpha(0f),
                            blackWithAlpha(bottomDarkAlpha * 0.45f),
                            blackWithAlpha(bottomDarkAlpha.toFloat()),
                        ),
                        floatArrayOf(0f, 0.72f, 1f),
                        Shader.TileMode.CLAMP,
                    )
                },
            )
        }

        val outerWidth = liquidGlassOuterWidth
            .coerceIn(MIN_LIQUID_GLASS_OUTER_WIDTH, MAX_LIQUID_GLASS_OUTER_WIDTH)
            .toFloat() * liquidGlassScaleForSize(width, height)
        if (outerWidth <= 0f) {
            return
        }
        canvas.drawRoundRect(
            rect,
            radius,
            radius,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = outerWidth
                shader = LinearGradient(
                    0f,
                    0f,
                    0f,
                    bottom,
                    intArrayOf(
                        whiteWithAlpha(topAlpha.toFloat()),
                        whiteWithAlpha(topAlpha * 0.38f),
                        whiteWithAlpha(0f),
                        whiteWithAlpha(0f),
                        whiteWithAlpha(bottomAlpha * 0.36f),
                        whiteWithAlpha(bottomAlpha.toFloat()),
                    ),
                    floatArrayOf(0f, 0.13f, 0.42f, 0.70f, 0.92f, 1f),
                    Shader.TileMode.CLAMP,
                )
            },
        )
    }

    private fun liquidGlassRadiusForSize(width: Int, height: Int): Float {
        val minSide = minOf(width, height).toFloat().coerceAtLeast(1f)
        return (liquidGlassRadius.coerceIn(MIN_LIQUID_GLASS_RADIUS, MAX_LIQUID_GLASS_RADIUS) * liquidGlassScaleForSize(width, height))
            .coerceIn(0f, minSide / 2f)
    }

    private fun liquidGlassScaleForSize(width: Int, height: Int): Float =
        minOf(width, height).toFloat().coerceAtLeast(1f) / SIZE_1X1.toFloat()

    private fun liquidGlassMaskFeather(width: Int, height: Int): Float =
        maxOf(1f, liquidGlassScaleForSize(width, height))

    private fun liquidGlassScaledWidth(width: Int, height: Int, value: Int): Int =
        (value * liquidGlassScaleForSize(width, height)).roundToInt().coerceAtLeast(0)

    private fun scaleBitmapAroundCanvasCenter(source: Bitmap, scale: Float): Bitmap {
        val safeScale = scale.coerceIn(
            MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT / 100f,
            MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT / 100f,
        )
        if (safeScale in 0.995f..1.005f) {
            return source
        }
        val scaledWidth = (source.width * safeScale).roundToInt().coerceAtLeast(1)
        val scaledHeight = (source.height * safeScale).roundToInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        Canvas(out).apply {
            drawColor(AndroidColor.TRANSPARENT)
            drawBitmap(
                scaled,
                (source.width - scaledWidth) / 2f,
                (source.height - scaledHeight) / 2f,
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
            )
        }
        return out
    }

    private fun subjectOutlineLayer(source: Bitmap, width: Int, inner: Boolean, alphaScale: Float): Bitmap {
        val baseAlpha = bitmapAlphaArray(source)
        val edgeAlpha = if (inner) {
            val eroded = minFilterAlpha(baseAlpha, source.width, source.height, width)
            IntArray(baseAlpha.size) { index -> (baseAlpha[index] - eroded[index]).coerceIn(0, 255) }
        } else {
            val dilated = maxFilterAlpha(baseAlpha, source.width, source.height, width)
            IntArray(baseAlpha.size) { index -> (dilated[index] - baseAlpha[index]).coerceIn(0, 255) }
        }
        return alphaArrayToColorLayer(edgeAlpha, source.width, source.height, AndroidColor.WHITE, alphaScale)
    }

    private fun maxFilterAlpha(alpha: IntArray, width: Int, height: Int, radius: Int): IntArray {
        if (radius <= 0) {
            return alpha.copyOf()
        }
        val horizontal = IntArray(alpha.size)
        val out = IntArray(alpha.size)
        for (y in 0 until height) {
            val row = y * width
            for (x in 0 until width) {
                var maxAlpha = 0
                val left = maxOf(0, x - radius)
                val right = minOf(width - 1, x + radius)
                for (cx in left..right) {
                    maxAlpha = maxOf(maxAlpha, alpha[row + cx])
                }
                horizontal[row + x] = maxAlpha
            }
        }
        for (y in 0 until height) {
            for (x in 0 until width) {
                var maxAlpha = 0
                val top = maxOf(0, y - radius)
                val bottom = minOf(height - 1, y + radius)
                for (cy in top..bottom) {
                    maxAlpha = maxOf(maxAlpha, horizontal[cy * width + x])
                }
                out[y * width + x] = maxAlpha
            }
        }
        return out
    }

    private fun minFilterAlpha(alpha: IntArray, width: Int, height: Int, radius: Int): IntArray {
        if (radius <= 0) {
            return alpha.copyOf()
        }
        val horizontal = IntArray(alpha.size)
        val out = IntArray(alpha.size)
        for (y in 0 until height) {
            val row = y * width
            for (x in 0 until width) {
                if (x - radius < 0 || x + radius >= width) {
                    horizontal[row + x] = 0
                    continue
                }
                var minAlpha = 255
                for (cx in (x - radius)..(x + radius)) {
                    minAlpha = minOf(minAlpha, alpha[row + cx])
                }
                horizontal[row + x] = minAlpha
            }
        }
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (y - radius < 0 || y + radius >= height) {
                    out[y * width + x] = 0
                    continue
                }
                var minAlpha = 255
                for (cy in (y - radius)..(y + radius)) {
                    minAlpha = minOf(minAlpha, horizontal[cy * width + x])
                }
                out[y * width + x] = minAlpha
            }
        }
        return out
    }

    private fun whiteWithAlpha(alpha: Float): Int =
        AndroidColor.argb(alpha.roundToInt().coerceIn(0, 255), 255, 255, 255)

    private fun blackWithAlpha(alpha: Float): Int =
        AndroidColor.argb(alpha.roundToInt().coerceIn(0, 255), 0, 0, 0)

    private fun applyAlphaMask(source: Bitmap, mask: IntArray): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = (AndroidColor.alpha(pixel) * mask[i] / 255f).roundToInt().coerceIn(0, 255)
            pixels[i] = if (alpha <= 0) {
                AndroidColor.TRANSPARENT
            } else {
                AndroidColor.argb(alpha, AndroidColor.red(pixel), AndroidColor.green(pixel), AndroidColor.blue(pixel))
            }
        }
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun roundedRectMaskAlpha(width: Int, height: Int, radius: Float, feather: Float): IntArray {
        val mask = IntArray(width * height)
        val halfWidth = width * 0.5f
        val halfHeight = height * 0.5f
        val safeFeather = feather.coerceAtLeast(0.001f)
        val denom = safeFeather * 2f
        for (y in 0 until height) {
            val centeredY = y + 0.5f - halfHeight
            for (x in 0 until width) {
                val centeredX = x + 0.5f - halfWidth
                val distance = sdRoundedRectCentered(centeredX, centeredY, halfWidth, halfHeight, radius)
                val alpha = when {
                    distance <= -safeFeather -> 255
                    distance >= safeFeather -> 0
                    else -> ((safeFeather - distance) / denom * 255f).roundToInt().coerceIn(0, 255)
                }
                mask[y * width + x] = alpha
            }
        }
        return mask
    }

    private fun sdRoundedRectCentered(
        x: Float,
        y: Float,
        halfWidth: Float,
        halfHeight: Float,
        radius: Float,
    ): Float {
        val qx = abs(x) - (halfWidth - radius)
        val qy = abs(y) - (halfHeight - radius)
        val outside = vectorLength(maxOf(qx, 0f), maxOf(qy, 0f))
        val inside = minOf(maxOf(qx, qy), 0f)
        return outside + inside - radius
    }

    private fun bitmapAlphaArray(source: Bitmap): IntArray {
        val pixels = IntArray(source.width * source.height)
        source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
        for (i in pixels.indices) {
            pixels[i] = AndroidColor.alpha(pixels[i])
        }
        return pixels
    }

    private fun alphaArrayToColorLayer(
        alpha: IntArray,
        width: Int,
        height: Int,
        color: Int,
        alphaScale: Float,
    ): Bitmap {
        val outPixels = IntArray(alpha.size)
        val red = AndroidColor.red(color)
        val green = AndroidColor.green(color)
        val blue = AndroidColor.blue(color)
        for (i in alpha.indices) {
            val scaledAlpha = (alpha[i] * alphaScale).roundToInt().coerceIn(0, 255)
            outPixels[i] = if (scaledAlpha <= 0) {
                AndroidColor.TRANSPARENT
            } else {
                AndroidColor.argb(scaledAlpha, red, green, blue)
            }
        }
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun vectorLength(x: Float, y: Float): Float =
        sqrt((x * x + y * y).toDouble()).toFloat()

    private fun candidateOrFallback(
        session: GenerationSession,
        choice: PreviewChoice,
    ): IconCandidate =
        candidateForChoice(session, choice)
            ?: session.candidates[PreviewChoice.Full]
            ?: session.candidates[PreviewChoice.Plate]
            ?: session.candidates.getValue(PreviewChoice.Original)

    private fun candidateForChoice(session: GenerationSession, choice: PreviewChoice): IconCandidate? =
        when (choice) {
            PreviewChoice.RmbgComposedBackground -> candidateWithComposedBackground(
                session = session,
                foregroundChoice = PreviewChoice.Rmbg,
            )
            PreviewChoice.GptComposedBackground -> candidateWithComposedBackground(
                session = session,
                foregroundChoice = PreviewChoice.Gpt,
            )
            else -> session.candidates[choice]
        }

    private fun candidateWithComposedBackground(
        session: GenerationSession,
        foregroundChoice: PreviewChoice,
    ): IconCandidate? {
        val foreground = session.candidates[foregroundChoice] ?: return null
        val background = session.candidates[PreviewChoice.ComposedBackground]?.recbg ?: return null
        return foreground.copy(
            recbg = background,
            customFinalBitmap = null,
        )
    }

    private fun effectiveChoiceForPreviewRow(
        mode: PreviewMode,
        rowChoice: PreviewChoice,
        session: GenerationSession,
    ): PreviewChoice {
        if (rowChoice != PreviewChoice.ComposedBackground) {
            return rowChoice
        }
        val currentChoice = previewSelections.choiceFor(mode)
        val target = when (currentChoice) {
            PreviewChoice.Rmbg,
            PreviewChoice.RmbgComposedBackground -> PreviewChoice.RmbgComposedBackground
            PreviewChoice.Gpt,
            PreviewChoice.GptComposedBackground -> PreviewChoice.GptComposedBackground
            else -> PreviewChoice.ComposedBackground
        }
        return if (target == PreviewChoice.ComposedBackground || candidateForChoice(session, target) != null) {
            target
        } else {
            PreviewChoice.ComposedBackground
        }
    }

    private fun candidateWithCustomOverrides(
        session: GenerationSession,
        mode: PreviewMode,
        choice: PreviewChoice,
    ): IconCandidate {
        val base = candidateOrFallback(session, choice)
        val customForeground = session.customForegrounds[mode]
        val customBackground = session.customBackgrounds[mode]
        if (customForeground == null && customBackground == null) {
            return base
        }
        return base.copy(
            recfgRaw = customForeground ?: base.recfgRaw,
            recbg = customBackground ?: base.recbg,
            monochromeRaw = when {
                customForeground != null -> customForeground
                else -> base.monochromeRaw
            },
            preserveGeometry = if (customForeground != null) true else base.preserveGeometry,
            customFinalBitmap = null,
            rmbgSourceRaw = if (customForeground != null) null else base.rmbgSourceRaw,
            rmbgAlphaRaw = if (customForeground != null) null else base.rmbgAlphaRaw,
        )
    }

    private fun monochromeForCandidate(candidate: IconCandidate, invertLuma: Boolean = false): Bitmap {
        if (candidate.monochromeFromDefaultSubject) {
            return simpleMonochromeAlphaFromDefaultSubject(
                renderCandidateBitmap(candidate.recfgRaw),
                invertLuma = invertLuma,
            )
        }
        val foreground = renderCandidateForegroundBase(candidate)
        val rmbgSource = rmbgTunedForegroundRaw(candidate)?.let { renderCandidateBitmap(it) }
        val nativeSource = candidate.monochromeRaw?.let { renderCandidateBitmap(it) }
        val monochrome = when {
            rmbgSource != null -> {
                monochromeAlpha(rmbgSource, invertLuma = invertLuma)
            }
            hasForegroundTonalRange(foreground) -> {
                monochromeAlpha(foreground, invertLuma = invertLuma)
            }
            nativeSource != null &&
                candidate.monochromeIsNative &&
                isUsableNativeMonochrome(nativeSource, foreground) -> {
                cleanNativeMonochrome(nativeSource)
            }
            nativeSource != null && hasForegroundTonalRange(nativeSource) -> {
                monochromeAlpha(nativeSource, invertLuma = invertLuma)
            }
            nativeSource != null && !candidate.monochromeIsNative -> {
                monochromeAlpha(nativeSource, invertLuma = invertLuma)
            }
            else -> {
                monochromeAlpha(foreground, invertLuma = invertLuma)
            }
        }
        return trimMonochromeEdge(monochrome)
    }

    private fun scaleMonochromeForTheme(source: Bitmap): Bitmap =
        scaleBitmapAroundAlphaCenter(source, monochromeThemeScale)

    private fun filterDecimalInput(value: String): String {
        val builder = StringBuilder()
        var hasDot = false
        value.forEachIndexed { index, char ->
            when {
                char.isDigit() -> builder.append(char)
                char == '-' && index == 0 && builder.isEmpty() -> builder.append(char)
                char == '.' && !hasDot -> {
                    builder.append(char)
                    hasDot = true
                }
            }
        }
        val filtered = builder.toString()
        val dotIndex = filtered.indexOf('.')
        return if (dotIndex >= 0 && filtered.length > dotIndex + 3) {
            filtered.take(dotIndex + 3)
        } else {
            filtered
        }.take(8)
    }

    private fun formatScale(value: Float): String =
        String.format(Locale.US, "%.2f", value)

    private fun previewAssetsForSelections(
        session: GenerationSession,
        selections: PreviewSelections,
    ): PreviewAssets {
        val light = candidateWithCustomOverrides(session, PreviewMode.NormalLight, selections.normalLight)
        val lightRecfg = renderCandidateForeground(light)
        val lightRecbg = liquidGlassBackgroundForSize(light.recbg, SIZE_1X1, SIZE_1X1)

        val night = candidateWithCustomOverrides(session, PreviewMode.NormalDark, selections.normalDark)
        val nightPreview = run {
            val nightRecfg = renderCandidateForeground(night)
            val nightRecbg = liquidGlassBackgroundForSize(night.recbg, SIZE_1X1, SIZE_1X1)
            normalDarkForeground(nightRecfg, nightRecbg)
        }

        val monochromeLight = monochromeForCandidate(
            candidateWithCustomOverrides(session, PreviewMode.MonochromeLight, selections.monochromeLight),
            invertLuma = true,
        )
        val monochromeDark = monochromeForCandidate(
            candidateWithCustomOverrides(session, PreviewMode.MonochromeDark, selections.monochromeDark),
            invertLuma = false,
        )

        return PreviewAssets(
            recbg = lightRecbg,
            recfg = lightRecfg,
            recNight = nightPreview,
            monochromeLight = monochromeLight,
            monochromeDark = monochromeDark,
        )
    }

    private fun previewAssetsForCandidate(candidate: IconCandidate, mode: PreviewMode? = null): PreviewAssets {
        val customFinal = candidate.customFinalBitmap
        if (customFinal != null) {
            val transparent = solidBitmap(customFinal.width, customFinal.height, AndroidColor.TRANSPARENT)
            return when (mode) {
                PreviewMode.NormalLight -> PreviewAssets(
                    recbg = transparent,
                    recfg = customFinal,
                    recNight = null,
                    monochromeLight = null,
                    monochromeDark = null,
                )
                PreviewMode.NormalDark -> PreviewAssets(
                    recbg = null,
                    recfg = null,
                    recNight = customFinal,
                    monochromeLight = null,
                    monochromeDark = null,
                )
                PreviewMode.MonochromeLight,
                PreviewMode.MonochromeDark,
                null -> PreviewAssets(
                    recbg = null,
                    recfg = null,
                    recNight = null,
                    monochromeLight = monochromeForCandidate(candidate, invertLuma = true),
                    monochromeDark = monochromeForCandidate(candidate, invertLuma = false),
                )
            }
        }
        val recfg = renderCandidateForeground(candidate)
        val recbg = liquidGlassBackgroundForSize(candidate.recbg, SIZE_1X1, SIZE_1X1)
        return PreviewAssets(
            recbg = recbg,
            recfg = recfg,
            recNight = normalDarkForeground(recfg, recbg),
            monochromeLight = monochromeForCandidate(candidate, invertLuma = true),
            monochromeDark = monochromeForCandidate(candidate, invertLuma = false),
        )
    }

    private fun renderCandidateForegroundBase(candidate: IconCandidate): Bitmap =
        polishForegroundEdges(renderCandidateBitmap(rmbgTunedForegroundRaw(candidate) ?: candidate.recfgRaw))

    private fun renderCandidateForeground(candidate: IconCandidate): Bitmap =
        foregroundForSize(renderCandidateForegroundBase(candidate), SIZE_1X1, SIZE_1X1)

    private fun applyForegroundShadow(source: Bitmap): Bitmap {
        val level = foregroundShadowLevel.coerceIn(MIN_FOREGROUND_SHADOW_LEVEL, MAX_FOREGROUND_SHADOW_LEVEL)
        if (level <= 0) {
            return source
        }
        val params = foregroundShadowParams(level, minOf(source.width, source.height))
        val shadow = subjectShadowBitmap(source, params)
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        Canvas(out).apply {
            drawColor(AndroidColor.TRANSPARENT)
            drawBitmap(shadow, params.offsetX.toFloat(), params.offsetY.toFloat(), Paint(Paint.FILTER_BITMAP_FLAG))
            drawBitmap(source, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        }
        return out
    }

    private fun foregroundShadowParams(level: Int, baseSize: Int): ForegroundShadowParams {
        val ratio = (level.toDouble() / MAX_FOREGROUND_SHADOW_LEVEL.toDouble()).coerceIn(0.0, 1.0)
        val scale = baseSize.toDouble() / SIZE_1X1.toDouble()
        return ForegroundShadowParams(
            alpha = (ratio * FOREGROUND_SHADOW_MAX_ALPHA).roundToInt().coerceIn(0, 255),
            blurRadius = (ratio * FOREGROUND_SHADOW_MAX_BLUR * scale).toFloat(),
            offsetX = (ratio * FOREGROUND_SHADOW_MAX_OFFSET_X * scale).roundToInt(),
            offsetY = (ratio * FOREGROUND_SHADOW_MAX_OFFSET_Y * scale).roundToInt(),
            spread = (ratio * FOREGROUND_SHADOW_MAX_SPREAD * scale).roundToInt().coerceAtLeast(0),
        )
    }

    private fun subjectShadowBitmap(source: Bitmap, params: ForegroundShadowParams): Bitmap {
        val width = source.width
        val height = source.height
        val sourcePixels = IntArray(width * height)
        val shadowPixels = IntArray(sourcePixels.size)
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
        for (i in sourcePixels.indices) {
            val alpha = (AndroidColor.alpha(sourcePixels[i]) * params.alpha / 255.0)
                .roundToInt()
                .coerceIn(0, 255)
            shadowPixels[i] = if (alpha <= 0) AndroidColor.TRANSPARENT else AndroidColor.argb(alpha, 0, 0, 0)
        }
        val alphaMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        alphaMask.setPixels(shadowPixels, 0, width, 0, 0, width, height)
        val shadow = if (params.spread > 0) growAlphaMask(alphaMask, params.spread) else alphaMask
        return if (params.blurRadius > 0f) {
            blurAlphaMask(shadow, params.blurRadius)
        } else {
            shadow
        }
    }

    private fun growAlphaMask(source: Bitmap, radius: Int): Bitmap {
        val width = source.width
        val height = source.height
        val sourcePixels = IntArray(width * height)
        val outPixels = IntArray(sourcePixels.size)
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
        val safeRadius = radius.coerceAtLeast(1)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var maxAlpha = 0
                for (dy in -safeRadius..safeRadius) {
                    val ny = y + dy
                    if (ny !in 0 until height) continue
                    for (dx in -safeRadius..safeRadius) {
                        val nx = x + dx
                        if (nx !in 0 until width) continue
                        maxAlpha = maxOf(maxAlpha, AndroidColor.alpha(sourcePixels[ny * width + nx]))
                    }
                }
                outPixels[y * width + x] = if (maxAlpha <= 0) {
                    AndroidColor.TRANSPARENT
                } else {
                    AndroidColor.argb(maxAlpha, 0, 0, 0)
                }
            }
        }
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun blurAlphaMask(source: Bitmap, radius: Float): Bitmap {
        val safeRadius = radius.roundToInt().coerceIn(0, 25)
        if (safeRadius <= 0) {
            return source
        }
        val width = source.width
        val height = source.height
        val sourcePixels = IntArray(width * height)
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
        val horizontal = IntArray(sourcePixels.size)
        val outPixels = IntArray(sourcePixels.size)
        val window = safeRadius * 2 + 1

        for (y in 0 until height) {
            var sum = 0
            for (x in -safeRadius..safeRadius) {
                val cx = x.coerceIn(0, width - 1)
                sum += AndroidColor.alpha(sourcePixels[y * width + cx])
            }
            for (x in 0 until width) {
                horizontal[y * width + x] = sum / window
                val removeX = (x - safeRadius).coerceIn(0, width - 1)
                val addX = (x + safeRadius + 1).coerceIn(0, width - 1)
                sum += AndroidColor.alpha(sourcePixels[y * width + addX])
                sum -= AndroidColor.alpha(sourcePixels[y * width + removeX])
            }
        }

        for (x in 0 until width) {
            var sum = 0
            for (y in -safeRadius..safeRadius) {
                val cy = y.coerceIn(0, height - 1)
                sum += horizontal[cy * width + x]
            }
            for (y in 0 until height) {
                val alpha = (sum / window).coerceIn(0, 255)
                outPixels[y * width + x] = if (alpha <= 0) {
                    AndroidColor.TRANSPARENT
                } else {
                    AndroidColor.argb(alpha, 0, 0, 0)
                }
                val removeY = (y - safeRadius).coerceIn(0, height - 1)
                val addY = (y + safeRadius + 1).coerceIn(0, height - 1)
                sum += horizontal[addY * width + x]
                sum -= horizontal[removeY * width + x]
            }
        }
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun rmbgTunedForegroundRaw(candidate: IconCandidate): Bitmap? {
        val source = candidate.rmbgSourceRaw ?: return null
        val alpha = candidate.rmbgAlphaRaw ?: return null
        if (alpha.size != source.width * source.height) {
            return null
        }
        return applyAlphaArrayToSource(
            source = source,
            alpha = tuneRmbgAlpha(alpha, source.width, source.height),
        )
    }

    private fun renderCandidateBitmap(bitmap: Bitmap): Bitmap =
        normalizeForegroundSubjectSize(bitmap)

    private fun applyPreviewChoice(mode: PreviewMode, choice: PreviewChoice) {
        val session = activeGenerationSession ?: return
        val customKind = choice.customKind
        if (customKind != null) {
            chooseCustomImageForMode(mode, customKind)
            return
        }
        if (choice == PreviewChoice.Gpt && session.candidates[PreviewChoice.Gpt] == null) {
            generateGptCandidateForMode(mode)
            return
        }
        if (choice == PreviewChoice.GptComposedBackground && session.candidates[PreviewChoice.Gpt] == null) {
            statusText = "先生成 GPT 候选，再使用拼合背景"
            return
        }
        if (choice == PreviewChoice.RmbgComposedBackground && session.candidates[PreviewChoice.Rmbg] == null) {
            statusText = "先生成 RMBG 候选，再使用拼合背景"
            return
        }
        val selections = previewSelections.withChoice(mode, choice)
        previewSelections = selections
        saveUiState()
        writeActivePreviewOutputs(session, selections, closeDialog = false)
    }

    private fun applyPreviewChoiceToAll(choice: PreviewChoice) {
        val session = activeGenerationSession ?: return
        val batchPackageNames = multiSelectedPackageNames.toList().sorted()
        if (batchPackageNames.isNotEmpty()) {
            applyPreviewChoiceToSelectedPackages(choice, batchPackageNames)
            return
        }
        if (choice == PreviewChoice.Gpt && session.candidates[PreviewChoice.Gpt] == null) {
            generateGptCandidateForAll()
            return
        }
        if (choice == PreviewChoice.Rmbg && session.candidates[PreviewChoice.Rmbg] == null) {
            generateRmbgCandidateForAll()
            return
        }
        if (choice == PreviewChoice.GptComposedBackground && session.candidates[PreviewChoice.Gpt] == null) {
            statusText = "先生成 GPT 候选，再使用拼合背景"
            return
        }
        if (choice == PreviewChoice.RmbgComposedBackground && session.candidates[PreviewChoice.Rmbg] == null) {
            statusText = "先生成 RMBG 候选，再使用拼合背景"
            return
        }
        if (choice.isCustom) {
            statusText = "自定义图片需要逐个槽位上传"
            return
        }
        if (candidateForChoice(session, choice) == null) {
            statusText = "${choice.label} 当前不可用"
            return
        }
        val selections = PreviewSelections.default(choice)
        previewSelections = selections
        previewChoiceMode = null
        saveUiState()
        writeActivePreviewOutputs(session, selections, closeDialog = true)
    }

    private fun applyPreviewChoiceToSelectedPackages(choice: PreviewChoice, packageNames: List<String>) {
        if (choice.isCustom) {
            statusText = "自定义图片需要逐个槽位上传"
            return
        }
        if (choice == PreviewChoice.Gpt && (gptBaseUrl.trim().isEmpty() || gptApiKey.trim().isEmpty())) {
            statusText = "先填写 GPT 设置"
            return
        }
        if (
            (choice == PreviewChoice.Rmbg || choice == PreviewChoice.RmbgComposedBackground) &&
            findRmbgComponent() == null
        ) {
            statusText = "未安装 RMBG 组件 ZIP"
            return
        }
        if (isBusy || isGeneratingGptCandidate || isGeneratingRmbgCandidate) {
            statusText = "当前有任务在运行，请等待"
            return
        }
        if (
            (choice == PreviewChoice.Rmbg || choice == PreviewChoice.RmbgComposedBackground) &&
            !rmbgGenerationGate.compareAndSet(false, true)
        ) {
            statusText = "RMBG正在运行，请等待"
            return
        }

        isBusy = true
        previewChoiceMode = null
        batchApplyProgress = BatchApplyProgress(
            title = "全部应用",
            completed = 0,
            total = packageNames.size,
            currentLabel = "准备处理 ${packageNames.size} 个 APK",
            failures = 0,
        )
        statusText = "全部应用处理中: 0/${packageNames.size}"
        val outputUri = outputTreeUri
        val selectedAtStart = selectedPackageName
        startUiFriendlyThread("ArtPlusBatchApplyRule") {
            val successes = mutableListOf<String>()
            val failures = mutableListOf<String>()
            var selectedResult: GenerationResult? = null
            try {
                packageNames.forEachIndexed { index, packageName ->
                    val app = apps.firstOrNull { it.packageName == packageName }
                    if (app == null) {
                        failures += "$packageName: 应用不存在"
                        updateBatchApplyProgress(
                            completed = index + 1,
                            total = packageNames.size,
                            currentLabel = "跳过: $packageName",
                            failures = failures.size,
                        )
                        return@forEachIndexed
                    }
                    updateBatchApplyProgress(
                        completed = index,
                        total = packageNames.size,
                        currentLabel = "处理中: ${app.label} (${packageName})",
                        failures = failures.size,
                    )
                    try {
                        val result = generatePackageForPreviewChoice(app, choice)
                        if (outputUri != null) {
                            exportToTree(result.outDir)
                        }
                        installWithRoot(result.outDir, packageName, RootWriteMode.All)
                        successes += packageName
                        if (packageName == selectedAtStart) {
                            selectedResult = result
                        }
                    } catch (error: Throwable) {
                        failures += "$packageName: ${error.message ?: error.javaClass.simpleName}"
                    }
                    updateBatchApplyProgress(
                        completed = index + 1,
                        total = packageNames.size,
                        currentLabel = "已完成: ${app.label} (${packageName})",
                        failures = failures.size,
                    )
                }
                runOnUiThread {
                    if (successes.isNotEmpty()) {
                        updateGeneratedPackageCache(generatedPackageNames + successes)
                        multiSelectedPackageNames = multiSelectedPackageNames - successes.toSet()
                    }
                    val result = selectedResult
                    if (result != null && selectedPackageName == selectedAtStart) {
                        activeGenerationSession = result.session
                        previewSelections = result.selections
                        previewChoiceMode = null
                        previewPackageName = result.session.packageName
                        previewDirPath = result.outDir.absolutePath
                        previewVersion += 1
                        saveUiState()
                    }
                    statusText = when {
                        failures.isEmpty() -> "全部应用完成: ${successes.size}/${packageNames.size}"
                        successes.isEmpty() -> "全部应用失败: ${failures.firstOrNull().orEmpty()}"
                        else -> "全部应用完成 ${successes.size} 个，失败 ${failures.size} 个: ${failures.firstOrNull().orEmpty()}"
                    }
                }
            } finally {
                if (choice == PreviewChoice.Rmbg || choice == PreviewChoice.RmbgComposedBackground) {
                    rmbgGenerationGate.set(false)
                }
                runOnUiThread {
                    isBusy = false
                    isGptPreviewLoading = false
                    isGeneratingGptCandidate = false
                    isGeneratingRmbgCandidate = false
                    rmbgCandidatePackageName = null
                    rmbgCandidateMode = null
                    rmbgCandidateStatusText = ""
                    batchApplyProgress = null
                }
            }
        }
    }

    private fun updateBatchApplyProgress(
        completed: Int,
        total: Int,
        currentLabel: String,
        failures: Int,
    ) {
        runOnUiThread {
            batchApplyProgress = BatchApplyProgress(
                title = "全部应用",
                completed = completed.coerceIn(0, total.coerceAtLeast(0)),
                total = total,
                currentLabel = currentLabel,
                failures = failures,
            )
            statusText = "全部应用处理中: ${completed.coerceAtMost(total)}/$total"
        }
    }

    private fun generatePackageForPreviewChoice(app: AppEntry, choice: PreviewChoice): GenerationResult {
        val useGpt = choice == PreviewChoice.Gpt || choice == PreviewChoice.GptComposedBackground
        val result = generateArtPlusPackage(app, useGpt)
        var session = result.session
        if (choice == PreviewChoice.Rmbg || choice == PreviewChoice.RmbgComposedBackground) {
            val source = resizeBitmap(session.sourceIcon, SIZE_1X1, SIZE_1X1)
            val rmbgResult = buildRmbgCandidate(source)
                ?: error("未安装 RMBG 组件 ZIP")
            val candidate = rmbgResult.candidate ?: error("RMBG候选为空")
            session = session.copy(
                candidates = session.candidates + (PreviewChoice.Rmbg to candidate),
            )
        }
        val effectiveChoice = when {
            choice == PreviewChoice.GptComposedBackground && candidateForChoice(session, PreviewChoice.GptComposedBackground) == null ->
                PreviewChoice.Gpt
            choice == PreviewChoice.RmbgComposedBackground && candidateForChoice(session, PreviewChoice.RmbgComposedBackground) == null ->
                PreviewChoice.Rmbg
            candidateForChoice(session, choice) != null -> choice
            else -> defaultLocalPreviewChoice(session.autoLocalChoice)
        }
        val selections = PreviewSelections.default(effectiveChoice)
        val finalSession = session.copy(outDir = result.outDir)
        writePackageOutputs(finalSession, selections)
        return GenerationResult(
            outDir = result.outDir,
            session = finalSession,
            selections = selections,
        )
    }

    private fun clearRmbgCandidateUiState() {
        if (isGeneratingRmbgCandidate) {
            return
        }
        lastRmbgCandidateError = null
        rmbgCandidatePackageName = null
        rmbgCandidateMode = null
        rmbgCandidateStatusText = ""
        rmbgCandidateFailurePackageName = null
        rmbgCandidateFailureMode = null
    }

    private fun chooseCustomImageForMode(mode: PreviewMode, kind: CustomImageKind) {
        if (isBusy || isGeneratingGptCandidate || isGeneratingRmbgCandidate) {
            return
        }
        pendingCustomImageMode = mode
        pendingCustomImageKind = kind
        chooseCustomImageLauncher.launch(
            arrayOf(
                "image/png",
                "image/svg+xml",
            ),
        )
    }

    private fun importCustomPreviewImage(mode: PreviewMode, kind: CustomImageKind, uri: Uri) {
        val session = activeGenerationSession
        if (session == null) {
            statusText = "先生成一次预览后再导入自定义图片"
            return
        }
        statusText = "导入${kind.label}: ${mode.label}"
        startUiFriendlyThread("ArtPlusCustomImageImport") {
            try {
                val bitmap = loadCustomImageBitmap(uri)
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
                val selections = previewSelections
                writePackageOutputs(updatedSession, selections)
                if (outputTreeUri != null) {
                    exportToTree(updatedSession.outDir)
                }
                runOnUiThread {
                    activeGenerationSession = updatedSession
                    previewSelections = selections
                    previewVersion += 1
                    statusText = "已导入${kind.label}: ${mode.label}"
                    saveUiState()
                }
            } catch (error: Exception) {
                status("${kind.label}导入失败: ${error.message ?: error.javaClass.simpleName}")
            }
        }
    }

    private fun generateGptCandidateForMode(mode: PreviewMode) {
        val session = activeGenerationSession ?: return
        if (gptBaseUrl.trim().isEmpty() || gptApiKey.trim().isEmpty()) {
            statusText = "先填写 GPT Base URL 和 API key"
            return
        }
        if (isGeneratingGptCandidate || isBusy) {
            return
        }
        isGeneratingGptCandidate = true
        isGptPreviewLoading = true
        statusText = "GPT候选生成中: ${session.packageName}"
        val selections = previewSelections.withChoice(mode, PreviewChoice.Gpt)
        startUiFriendlyThread("ArtPlusGptCandidate") {
            try {
                val gptLayers = generateGptLayers(session.sourceIcon, session.baseRecfg, session.baseRecbg)
                val updatedSession = session.copy(
                    candidates = session.candidates + (
                        PreviewChoice.Gpt to IconCandidate(
                            recfgRaw = gptLayers.recfg,
                            recbg = gptLayers.recbg,
                            monochromeRaw = null,
                        )
                        ),
                )
                writePackageOutputs(updatedSession, selections)
                if (outputTreeUri != null) {
                    exportToTree(updatedSession.outDir)
                }
                runOnUiThread {
                    activeGenerationSession = updatedSession
                    previewSelections = selections
                    previewVersion += 1
                    statusText = "GPT候选已生成并应用到 ${mode.label}"
                    saveUiState()
                }
            } catch (error: Exception) {
                status("GPT候选失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                runOnUiThread {
                    isGeneratingGptCandidate = false
                    isGptPreviewLoading = false
                }
            }
        }
    }

    private fun generateGptCandidateForAll() {
        val session = activeGenerationSession ?: return
        if (gptBaseUrl.trim().isEmpty() || gptApiKey.trim().isEmpty()) {
            statusText = "先填写 GPT Base URL 和 API key"
            return
        }
        if (isGeneratingGptCandidate || isBusy) {
            return
        }
        isGeneratingGptCandidate = true
        isGptPreviewLoading = true
        statusText = "GPT候选生成中: ${session.packageName}"
        val selections = PreviewSelections.default(PreviewChoice.Gpt)
        startUiFriendlyThread("ArtPlusGptCandidateAll") {
            try {
                val gptLayers = generateGptLayers(session.sourceIcon, session.baseRecfg, session.baseRecbg)
                val updatedSession = session.copy(
                    candidates = session.candidates + (
                        PreviewChoice.Gpt to IconCandidate(
                            recfgRaw = gptLayers.recfg,
                            recbg = gptLayers.recbg,
                            monochromeRaw = null,
                        )
                        ),
                )
                writePackageOutputs(updatedSession, selections)
                if (outputTreeUri != null) {
                    exportToTree(updatedSession.outDir)
                }
                runOnUiThread {
                    activeGenerationSession = updatedSession
                    previewSelections = selections
                    previewChoiceMode = null
                    previewVersion += 1
                    statusText = "GPT候选已生成并应用到全部"
                    saveUiState()
                }
            } catch (error: Exception) {
                status("GPT候选失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                runOnUiThread {
                    isGeneratingGptCandidate = false
                    isGptPreviewLoading = false
                }
            }
        }
    }

    private fun generateRmbgCandidateForMode(mode: PreviewMode) {
        val session = activeGenerationSession ?: return
        if (session.candidates[PreviewChoice.Rmbg] != null) {
            applyPreviewChoice(mode, PreviewChoice.Rmbg)
            statusText = "已使用现有 RMBG 候选"
            return
        }
        if (findRmbgComponent() == null) {
            lastRmbgCandidateError = "未安装 RMBG 组件 ZIP"
            rmbgCandidateFailurePackageName = session.packageName
            rmbgCandidateFailureMode = mode
            statusText = lastRmbgCandidateError ?: "未安装 RMBG 组件"
            return
        }
        if (isGeneratingRmbgCandidate || isGeneratingGptCandidate || isBusy) {
            statusText = "RMBG正在运行或主任务忙，请等待"
            return
        }
        if (!rmbgGenerationGate.compareAndSet(false, true)) {
            statusText = "RMBG正在运行，请等待"
            return
        }
        isGeneratingRmbgCandidate = true
        lastRmbgCandidateError = null
        rmbgCandidatePackageName = session.packageName
        rmbgCandidateMode = mode
        rmbgCandidateStatusText = "RMBG运行中(${RmbgInferenceBackend.Cpu.label})，请等待: ${mode.label}"
        rmbgCandidateFailurePackageName = null
        rmbgCandidateFailureMode = null
        statusText = "RMBG候选生成中(${RmbgInferenceBackend.Cpu.label}): ${session.packageName}"
        val selections = previewSelections.withChoice(mode, PreviewChoice.Rmbg)
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
                if (outputTreeUri != null) {
                    exportToTree(updatedSession.outDir)
                }
                runOnUiThread {
                    activeGenerationSession = updatedSession
                    previewSelections = selections
                    previewVersion += 1
                    lastRmbgCandidateError = null
                    lastRmbgInferenceReport = inferenceReport
                    rmbgCandidateFailurePackageName = null
                    rmbgCandidateFailureMode = null
                    statusText = if (result.validationWarning != null) {
                        "${result.validationWarning}，已应用到 ${mode.label}: ${formatRmbgInferenceReport(inferenceReport)}"
                    } else {
                        "RMBG候选已生成并应用到 ${mode.label}: ${formatRmbgInferenceReport(inferenceReport)}"
                    }
                    saveUiState()
                }
            } catch (error: Throwable) {
                val message = describeRmbgFailure(error)
                runOnUiThread {
                    lastRmbgCandidateError = message
                    rmbgCandidateFailurePackageName = session.packageName
                    rmbgCandidateFailureMode = mode
                    statusText = "RMBG候选失败(${RmbgInferenceBackend.Cpu.label}): $message"
                }
            } finally {
                rmbgGenerationGate.set(false)
                runOnUiThread {
                    isGeneratingRmbgCandidate = false
                    rmbgCandidatePackageName = null
                    rmbgCandidateMode = null
                    rmbgCandidateStatusText = ""
                }
            }
        }
    }

    private fun generateRmbgCandidateForAll() {
        val session = activeGenerationSession ?: return
        if (session.candidates[PreviewChoice.Rmbg] != null) {
            applyPreviewChoiceToAll(PreviewChoice.Rmbg)
            statusText = "已使用现有 RMBG 候选"
            return
        }
        if (findRmbgComponent() == null) {
            lastRmbgCandidateError = "未安装 RMBG 组件 ZIP"
            rmbgCandidateFailurePackageName = session.packageName
            rmbgCandidateFailureMode = null
            statusText = lastRmbgCandidateError ?: "未安装 RMBG 组件"
            return
        }
        if (isGeneratingRmbgCandidate || isGeneratingGptCandidate || isBusy) {
            statusText = "RMBG正在运行或主任务忙，请等待"
            return
        }
        if (!rmbgGenerationGate.compareAndSet(false, true)) {
            statusText = "RMBG正在运行，请等待"
            return
        }
        isGeneratingRmbgCandidate = true
        lastRmbgCandidateError = null
        rmbgCandidatePackageName = session.packageName
        rmbgCandidateMode = null
        rmbgCandidateStatusText = "RMBG运行中(${RmbgInferenceBackend.Cpu.label})，请等待: 全部"
        rmbgCandidateFailurePackageName = null
        rmbgCandidateFailureMode = null
        statusText = "RMBG候选生成中(${RmbgInferenceBackend.Cpu.label}): ${session.packageName}"
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
                if (outputTreeUri != null) {
                    exportToTree(updatedSession.outDir)
                }
                runOnUiThread {
                    activeGenerationSession = updatedSession
                    previewSelections = selections
                    previewChoiceMode = null
                    previewVersion += 1
                    lastRmbgCandidateError = null
                    lastRmbgInferenceReport = inferenceReport
                    rmbgCandidateFailurePackageName = null
                    rmbgCandidateFailureMode = null
                    statusText = if (result.validationWarning != null) {
                        "${result.validationWarning}，已应用到全部: ${formatRmbgInferenceReport(inferenceReport)}"
                    } else {
                        "RMBG候选已生成并应用到全部: ${formatRmbgInferenceReport(inferenceReport)}"
                    }
                    saveUiState()
                }
            } catch (error: Throwable) {
                val message = describeRmbgFailure(error)
                runOnUiThread {
                    lastRmbgCandidateError = message
                    rmbgCandidateFailurePackageName = session.packageName
                    rmbgCandidateFailureMode = null
                    statusText = "RMBG候选失败(${RmbgInferenceBackend.Cpu.label}): $message"
                }
            } finally {
                rmbgGenerationGate.set(false)
                runOnUiThread {
                    isGeneratingRmbgCandidate = false
                    rmbgCandidatePackageName = null
                    rmbgCandidateMode = null
                    rmbgCandidateStatusText = ""
                }
            }
        }
    }

    private fun describeRmbgFailure(error: Throwable): String {
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

    private fun formatRmbgInferenceReport(report: RmbgInferenceReport?): String {
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

    private fun unwrapInvocationError(error: Throwable): Throwable {
        var current = error
        while (current is InvocationTargetException && current.targetException != null) {
            current = current.targetException
        }
        return current
    }

    private fun refreshActivePreviewOutputs(
        rebuildLocalCandidates: Boolean,
        retargetFrom: PreviewChoice? = null,
    ) {
        val currentSession = activeGenerationSession
        if (currentSession == null) {
            previewOutputJob?.cancel()
            isPreviewOutputRefreshing = false
            return
        }
        val packageName = currentSession.packageName
        val app = apps.firstOrNull { it.packageName == packageName }
        val outDir = currentSession.outDir
        val currentSelections = previewSelections
        val outputUri = outputTreeUri
        val requestRevision = ++previewOutputRevision
        previewOutputJob?.cancel()
        isPreviewOutputRefreshing = true
        previewOutputJob = previewWorkerScope.launch {
            try {
                delay(if (rebuildLocalCandidates) PREVIEW_REBUILD_DEBOUNCE_MS else PREVIEW_OUTPUT_DEBOUNCE_MS)
                val updatedSession = when {
                    rebuildLocalCandidates && app != null && currentSession.canRebuildLocalCandidates ->
                        rebuildLocalSession(currentSession, app)
                    else -> currentSession
                }
                val previousDefault = retargetFrom
                    ?: if (rebuildLocalCandidates && currentSession.canRebuildLocalCandidates) {
                        defaultLocalPreviewChoice(currentSession.autoLocalChoice)
                    } else {
                        null
                    }
                val nextDefault = defaultLocalPreviewChoice(updatedSession.autoLocalChoice)
                val selections = when {
                    previousDefault == null -> currentSelections
                    else -> currentSelections.retarget(previousDefault, nextDefault)
                }
                writePackageOutputs(updatedSession, selections)
                if (outputUri != null) {
                    exportToTree(updatedSession.outDir)
                }
                withContext(Dispatchers.Main) {
                    if (requestRevision == previewOutputRevision) {
                        activeGenerationSession = updatedSession
                        previewSelections = selections
                        previewVersion += 1
                        saveUiState()
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                status("预览刷新失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                withContext(Dispatchers.Main) {
                    if (requestRevision == previewOutputRevision) {
                        isPreviewOutputRefreshing = false
                    }
                }
            }
        }
    }

    private fun buildLocalSessionForPreview(app: AppEntry, outDir: File): GenerationSession {
        val icon = app.applicationInfo.loadIcon(packageManager)
        val localSourceIcon = drawLocalCandidateSourceIcon(icon, SIZE_1X1, SIZE_1X1)
        val gptSourceIcon = drawDrawable(icon, GPT_SOURCE_SIZE, GPT_SOURCE_SIZE, transparent = false)
        val localSource = buildLocalIconLayers(icon)
        val localCandidateSet = buildLocalCandidates(localSource, localSourceIcon)
        return GenerationSession(
            packageName = app.packageName,
            outDir = outDir,
            sourceIcon = gptSourceIcon,
            baseRecfg = localSource.recfg,
            baseRecbg = localSource.recbg,
            monochromeRaw = localSource.monochrome,
            candidates = localCandidateSet.candidates,
            autoLocalChoice = localCandidateSet.autoChoice,
        )
    }

    private fun rebuildLocalSession(session: GenerationSession, app: AppEntry): GenerationSession {
        val icon = app.applicationInfo.loadIcon(packageManager)
        val localSourceIcon = drawLocalCandidateSourceIcon(icon, SIZE_1X1, SIZE_1X1)
        val gptSourceIcon = drawDrawable(icon, GPT_SOURCE_SIZE, GPT_SOURCE_SIZE, transparent = false)
        val localSource = buildLocalIconLayers(icon)
        val localCandidateSet = buildLocalCandidates(localSource, localSourceIcon)
        val localCandidates = localCandidateSet.candidates
        val retainedCandidates = buildMap {
            session.candidates[PreviewChoice.Gpt]?.let { put(PreviewChoice.Gpt, it) }
            session.candidates[PreviewChoice.Rmbg]?.let { put(PreviewChoice.Rmbg, it) }
        }
        val candidates = localCandidates + retainedCandidates
        return session.copy(
            sourceIcon = gptSourceIcon,
            baseRecfg = localSource.recfg,
            baseRecbg = localSource.recbg,
            monochromeRaw = localSource.monochrome,
            candidates = candidates,
            customForegrounds = session.customForegrounds,
            customBackgrounds = session.customBackgrounds,
            autoLocalChoice = localCandidateSet.autoChoice,
        )
    }

    private fun writeActivePreviewOutputs(
        session: GenerationSession,
        selections: PreviewSelections,
        closeDialog: Boolean,
    ) {
        val outputUri = outputTreeUri
        val requestRevision = ++previewOutputRevision
        previewOutputJob?.cancel()
        isPreviewOutputRefreshing = true
        previewOutputJob = previewWorkerScope.launch {
            try {
                delay(PREVIEW_OUTPUT_DEBOUNCE_MS)
                writePackageOutputs(session, selections)
                if (outputUri != null) {
                    exportToTree(session.outDir)
                }
                withContext(Dispatchers.Main) {
                    if (requestRevision == previewOutputRevision) {
                        activeGenerationSession = session
                        previewSelections = selections
                        previewVersion += 1
                        if (closeDialog) {
                            previewChoiceMode = null
                        }
                        saveUiState()
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                status("预览刷新失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                withContext(Dispatchers.Main) {
                    if (requestRevision == previewOutputRevision) {
                        isPreviewOutputRefreshing = false
                    }
                }
            }
        }
    }

    private fun generateGptLayers(sourceIcon: Bitmap, localRecfg: Bitmap, localRecbg: Bitmap): IconLayers {
        val chromaKey = chooseChromaKey(sourceIcon)
        val chromaHex = "#%02x%02x%02x".format(
            AndroidColor.red(chromaKey),
            AndroidColor.green(chromaKey),
            AndroidColor.blue(chromaKey),
        )
        val transparentForegroundPrompt = buildTransparentForegroundPrompt()
        val chromaForegroundPrompt = buildChromaForegroundPrompt(chromaHex)
        val backgroundPrompt = buildBackgroundPrompt()

        status("GPT生成前景...")
        var usedChromaForeground = false
        val rawForeground = try {
            val transparentForeground = gptEditImage(sourceIcon, transparentForegroundPrompt, "transparent")
            if (hasRealAlpha(transparentForeground)) {
                transparentForeground
            } else {
                usedChromaForeground = true
                status("GPT未返回透明前景，改用纯色抠底兜底")
                gptEditImage(sourceIcon, chromaForegroundPrompt, "opaque")
            }
        } catch (error: Exception) {
            usedChromaForeground = true
            status("GPT透明前景失败，改用纯色抠底兜底: ${error.message ?: error.javaClass.simpleName}")
            gptEditImage(sourceIcon, chromaForegroundPrompt, "opaque")
        }
        status("GPT生成背景...")
        val rawBackground = gptEditImage(sourceIcon, backgroundPrompt, "opaque")

        val recbg = Bitmap.createScaledBitmap(rawBackground, SIZE_1X1, SIZE_1X1, true)
        val recfg = when {
            hasRealAlpha(rawForeground) -> {
                Bitmap.createScaledBitmap(rawForeground, SIZE_1X1, SIZE_1X1, true)
            }
            usedChromaForeground -> {
                val keyed = removeChromaKeyBackground(rawForeground, chromaKey)
                if (alphaCoverage(keyed) in 0.002..0.95) {
                    Bitmap.createScaledBitmap(keyed, SIZE_1X1, SIZE_1X1, true)
                } else {
                    localRecfg
                }
            }
            else -> localRecfg
        }
        return IconLayers(recfg, recbg)
    }

    private fun gptEditImage(source: Bitmap, prompt: String, background: String): Bitmap =
        when (gptImageMode) {
            GptImageMode.Responses -> responsesEditImage(source, prompt, background)
            GptImageMode.Images -> imagesEditImage(source, prompt, background)
        }

    private fun responsesEditImage(source: Bitmap, prompt: String, background: String): Bitmap {
        val body = JSONObject()
            .put("model", GPT_RESPONSE_MODEL)
            .put(
                "input",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put(
                            "content",
                            JSONArray()
                                .put(JSONObject().put("type", "input_text").put("text", prompt))
                                .put(
                                    JSONObject()
                                        .put("type", "input_image")
                                        .put("image_url", bitmapToDataUrl(source)),
                                ),
                        ),
                ),
            )
            .put(
                "tools",
                JSONArray().put(
                    JSONObject()
                        .put("type", "image_generation")
                        .put("size", "auto")
                        .put("quality", GPT_IMAGE_QUALITY)
                        .put("background", background)
                        .put("output_format", "png"),
                ),
            )
            .put("tool_choice", JSONObject().put("type", "image_generation"))
            .put("stream", true)

        val response = postJson(normalizeResponsesUrl(gptBaseUrl), body)
        val parsed = if (response.trimStart().startsWith("data:") || response.trimStart().startsWith("event:")) {
            parseResponsesStream(response)
        } else {
            JSONObject(response)
        }
        return decodeBitmap(extractImageBytes(parsed))
    }

    private fun imagesEditImage(source: Bitmap, prompt: String, background: String): Bitmap {
        val boundary = "----ArtPlusMobile${UUID.randomUUID().toString().replace("-", "")}"
        val pngBytes = bitmapToPngBytes(source)
        val body = ByteArrayOutputStream()

        fun field(name: String, value: String) {
            body.writeString("--$boundary\r\n")
            body.writeString("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
            body.writeString(value)
            body.writeString("\r\n")
        }

        field("model", GPT_IMAGE_MODEL)
        field("prompt", prompt)
        field("size", GPT_IMAGE_SIZE)
        field("quality", GPT_IMAGE_QUALITY)
        field("background", background)
        field("output_format", "png")
        body.writeString("--$boundary\r\n")
        body.writeString("Content-Disposition: form-data; name=\"image\"; filename=\"artplus_source_icon.png\"\r\n")
        body.writeString("Content-Type: image/png\r\n\r\n")
        body.write(pngBytes)
        body.writeString("\r\n--$boundary--\r\n")

        val response = postBytes(
            urlText = normalizeImagesEditUrl(gptBaseUrl),
            body = body.toByteArray(),
            contentType = "multipart/form-data; boundary=$boundary",
        )
        return decodeBitmap(extractImageBytes(JSONObject(response)))
    }

    private fun postJson(urlText: String, body: JSONObject): String =
        postBytes(urlText, body.toString().toByteArray(Charsets.UTF_8), "application/json", accept = "text/event-stream, application/json")

    private fun postBytes(
        urlText: String,
        body: ByteArray,
        contentType: String,
        accept: String = "application/json",
    ): String {
        val url = validatedRemoteUrl(urlText, "GPT")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = GPT_CONNECT_TIMEOUT_MS
            readTimeout = GPT_READ_TIMEOUT_MS
            doOutput = true
            setRequestProperty("Accept", accept)
            setRequestProperty("Authorization", "Bearer ${gptApiKey.trim()}")
            setRequestProperty("Content-Type", contentType)
            setRequestProperty("Content-Length", body.size.toString())
        }
        try {
            connection.outputStream.use { it.write(body) }
            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }
            val text = stream.bufferedReader().use { it.readText() }
            if (connection.responseCode !in 200..299) {
                error("GPT HTTP ${connection.responseCode}: ${text.take(300)}")
            }
            return text
        } finally {
            connection.disconnect()
        }
    }

    private fun downloadBytes(urlText: String): ByteArray {
        val url = validatedRemoteUrl(urlText, "GPT图片")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = GPT_CONNECT_TIMEOUT_MS
            readTimeout = GPT_READ_TIMEOUT_MS
            if (gptApiKey.trim().isNotEmpty()) {
                setRequestProperty("Authorization", "Bearer ${gptApiKey.trim()}")
            }
        }
        try {
            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }
            val bytes = stream.use { it.readBytes() }
            if (connection.responseCode !in 200..299) {
                error("下载GPT图片失败 HTTP ${connection.responseCode}: ${String(bytes).take(300)}")
            }
            return bytes
        } finally {
            connection.disconnect()
        }
    }

    private fun validatedRemoteUrl(urlText: String, label: String): URL {
        val url = URL(urlText)
        val protocol = url.protocol.lowercase(Locale.US)
        if (protocol != "http" && protocol != "https") {
            error("$label URL 只支持 HTTP/HTTPS")
        }
        if (protocol == "http" && !isDebugBuild()) {
            error("$label URL 在正式版中必须使用 HTTPS")
        }
        return url
    }

    private fun parseResponsesStream(text: String): JSONObject {
        val output = JSONArray()
        var response: JSONObject? = null
        for (block in text.split("\n\n")) {
            val data = block.lineSequence()
                .map { it.trimEnd() }
                .filter { it.startsWith("data:") }
                .joinToString("\n") { it.removePrefix("data:").trimStart() }
                .trim()
            if (data.isEmpty() || data == "[DONE]") {
                continue
            }
            val event = runCatching { JSONObject(data) }.getOrNull() ?: continue
            event.optJSONObject("response")?.let {
                response = it
                val existing = it.optJSONArray("output")
                if (existing != null) {
                    for (i in 0 until existing.length()) {
                        output.put(existing.get(i))
                    }
                }
            }
            val item = event.optJSONObject("item")
            if (
                item != null &&
                (event.optString("type") == "response.output_item.done" ||
                    event.optString("type") == "response.output_item.added")
            ) {
                output.put(item)
            }
            if (event.optString("type") == "response.image_generation_call.partial_image") {
                val partial = event.optString("partial_image_b64")
                if (partial.isNotBlank()) {
                    output.put(
                        JSONObject()
                            .put("type", "image_generation_call")
                            .put("image_base64", partial),
                    )
                }
            }
        }
        return (response ?: JSONObject()).put("output", output)
    }

    private fun extractImageBytes(json: JSONObject): ByteArray {
        json.optJSONArray("output")?.let { output ->
            findImageBytes(output)?.let { return it }
        }
        json.optJSONArray("data")?.let { data ->
            findImageBytes(data)?.let { return it }
        }
        findImageBytes(JSONArray().put(json))?.let { return it }
        error("GPT响应没有图片数据")
    }

    private fun findImageBytes(items: JSONArray): ByteArray? {
        for (i in 0 until items.length()) {
            val item = items.optJSONObject(i) ?: continue
            decodeImageReference(item.opt("b64_json"))?.let { return it }
            decodeImageReference(item.opt("b64"))?.let { return it }
            decodeImageReference(item.opt("image_base64"))?.let { return it }
            decodeImageReference(item.opt("base64"))?.let { return it }
            decodeImageReference(item.opt("result"))?.let { return it }
            decodeImageReference(item.opt("url"))?.let { return it }
            decodeImageReference(item.opt("imageUrl"))?.let { return it }
            decodeImageReference(item.opt("remoteImageUrl"))?.let { return it }
            val imageUrl = item.optJSONObject("image_url")
            if (imageUrl != null) {
                decodeImageReference(imageUrl.opt("url"))?.let { return it }
            }
        }
        return null
    }

    private fun decodeImageReference(value: Any?): ByteArray? {
        val text = (value as? String)?.trim().orEmpty()
        if (text.isEmpty()) {
            return null
        }
        if (text.startsWith("http://") || text.startsWith("https://")) {
            return downloadBytes(text)
        }
        val b64 = if (text.startsWith("data:image/")) {
            text.substringAfter("base64,", "")
        } else {
            text
        }.replace("\\s".toRegex(), "")
        if (b64.length < 128) {
            return null
        }
        return runCatching { Base64.decode(b64, Base64.DEFAULT) }.getOrNull()
    }

    private fun decodeBitmap(bytes: ByteArray): Bitmap {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        return bitmap ?: error("GPT返回的图片无法解码")
    }

    private fun loadCustomImageBitmap(uri: Uri): Bitmap {
        val mime = contentResolver.getType(uri).orEmpty().lowercase(Locale.US)
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("无法打开图片")
        val bitmap = if (mime.contains("svg") || looksLikeSvg(bytes)) {
            decodeSvgBitmap(bytes)
        } else {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: error("图片无法解码；请选择 PNG 或 SVG")
        }
        return fitBitmapOnTransparentCanvas(bitmap, SIZE_1X1, SIZE_1X1)
    }

    private fun looksLikeSvg(bytes: ByteArray): Boolean {
        val prefix = String(bytes, 0, minOf(bytes.size, 256), Charsets.UTF_8).trimStart()
        return prefix.startsWith("<svg", ignoreCase = true) ||
            prefix.startsWith("<?xml", ignoreCase = true) && "<svg" in prefix.lowercase(Locale.US)
    }

    private fun decodeSvgBitmap(bytes: ByteArray): Bitmap {
        val svg = SVG.getFromInputStream(bytes.inputStream())
        val width = svg.documentWidth.takeIf { it > 0f } ?: SIZE_1X1.toFloat()
        val height = svg.documentHeight.takeIf { it > 0f } ?: SIZE_1X1.toFloat()
        svg.setDocumentWidth(width)
        svg.setDocumentHeight(height)
        val bitmap = Bitmap.createBitmap(width.roundToInt().coerceAtLeast(1), height.roundToInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(AndroidColor.TRANSPARENT)
        svg.renderToCanvas(canvas)
        return bitmap
    }

    private fun fitBitmapOnTransparentCanvas(source: Bitmap, width: Int, height: Int): Bitmap {
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(AndroidColor.TRANSPARENT)
        val scale = minOf(
            width.toFloat() / source.width.toFloat(),
            height.toFloat() / source.height.toFloat(),
        )
        val targetWidth = (source.width * scale).roundToInt().coerceAtLeast(1)
        val targetHeight = (source.height * scale).roundToInt().coerceAtLeast(1)
        val resized = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
        canvas.drawBitmap(resized, (width - targetWidth) / 2f, (height - targetHeight) / 2f, null)
        return out
    }

    private fun customCandidateForPreview(
        mode: PreviewMode,
        kind: CustomImageKind,
        session: GenerationSession,
    ): IconCandidate? {
        val foreground = session.customForegrounds[mode]
        val background = session.customBackgrounds[mode]
        val transparent = solidBitmap(SIZE_1X1, SIZE_1X1, AndroidColor.TRANSPARENT)
        return when (kind) {
            CustomImageKind.Foreground -> foreground?.let {
                IconCandidate(
                    recfgRaw = it,
                    recbg = background ?: session.baseRecbg,
                    monochromeRaw = it,
                    preserveGeometry = true,
                )
            }
            CustomImageKind.Background -> background?.let {
                IconCandidate(
                    recfgRaw = foreground ?: transparent,
                    recbg = it,
                    monochromeRaw = foreground,
                    preserveGeometry = true,
                )
            }
        }
    }

    private fun bitmapToDataUrl(bitmap: Bitmap): String =
        "data:image/png;base64,${Base64.encodeToString(bitmapToPngBytes(bitmap), Base64.NO_WRAP)}"

    private fun bitmapToPngBytes(bitmap: Bitmap): ByteArray {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        return output.toByteArray()
    }

    private fun ByteArrayOutputStream.writeString(value: String) {
        write(value.toByteArray(Charsets.UTF_8))
    }

    private fun normalizeResponsesUrl(baseUrl: String): String {
        val normalized = baseUrl.trim().trimEnd('/')
        return when {
            normalized.endsWith("/responses") -> normalized
            normalized.endsWith("/v1") -> "$normalized/responses"
            "/v1/" in "$normalized/" -> "$normalized/responses"
            else -> "$normalized/v1/responses"
        }
    }

    private fun normalizeImagesEditUrl(baseUrl: String): String {
        val normalized = baseUrl.trim().trimEnd('/')
        return when {
            normalized.endsWith("/images/edits") -> normalized
            normalized.endsWith("/v1") -> "$normalized/images/edits"
            "/v1/" in "$normalized/" -> "$normalized/images/edits"
            else -> "$normalized/v1/images/edits"
        }
    }

    private fun activeGptForegroundPrompt(): String {
        val custom = gptCustomPrompt.trim()
        val base = if (gptPromptPreset == GptPromptPreset.Custom && custom.isNotBlank()) {
            custom
        } else {
            gptPromptPreset.foregroundPrompt.ifBlank { GptPromptPreset.StableCutout.foregroundPrompt }
        }
        return base.trim().trimEnd('.') +
            ". Scale the subject/logo so its visible bounding box is about $foregroundSubjectPercent% of the final square canvas."
    }

    private fun buildTransparentForegroundPrompt(): String =
        activeGptForegroundPrompt() + " Return the extracted subject on a real transparent background with alpha channel."

    private fun buildChromaForegroundPrompt(chromaHex: String): String =
        activeGptForegroundPrompt() +
            " Place the extracted subject on a perfectly flat solid $chromaHex chroma-key background. " +
            "The chroma-key background must be one uniform color, with no checkerboard, no transparency preview pattern, " +
            "no shadows, no gradients, no texture, and no lighting variation. " +
            "Do not use $chromaHex anywhere in the subject/logo."

    private fun buildBackgroundPrompt(): String =
        "Remove the app icon main subject/logo. Rebuild only the clean original background plate. No logo, no text, no symbol."

    private fun drawDrawable(
        drawable: Drawable?,
        width: Int,
        height: Int,
        transparent: Boolean,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(if (transparent) AndroidColor.TRANSPARENT else AndroidColor.WHITE)
        if (drawable != null) {
            val copy = drawable.constantState?.newDrawable()?.mutate() ?: drawable.mutate()
            copy.setBounds(0, 0, width, height)
            copy.draw(canvas)
            if (transparent && copy is AdaptiveIconDrawable) {
                return clearOutsideAdaptiveIconMask(bitmap, copy)
            }
        }
        return bitmap
    }

    private fun clearOutsideAdaptiveIconMask(source: Bitmap, icon: AdaptiveIconDrawable): Bitmap {
        val width = source.width
        val height = source.height
        val mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val maskCanvas = Canvas(mask)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = AndroidColor.WHITE
        maskCanvas.drawPath(icon.iconMask, paint)

        val sourcePixels = IntArray(width * height)
        val maskPixels = IntArray(width * height)
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
        mask.getPixels(maskPixels, 0, width, 0, 0, width, height)
        for (i in sourcePixels.indices) {
            val maskAlpha = AndroidColor.alpha(maskPixels[i])
            if (maskAlpha <= 0) {
                sourcePixels[i] = AndroidColor.TRANSPARENT
                continue
            }
            if (maskAlpha < 255) {
                val pixel = sourcePixels[i]
                val alpha = (AndroidColor.alpha(pixel) * maskAlpha / 255.0)
                    .toInt()
                    .coerceIn(0, 255)
                sourcePixels[i] = AndroidColor.argb(
                    alpha,
                    AndroidColor.red(pixel),
                    AndroidColor.green(pixel),
                    AndroidColor.blue(pixel),
                )
            }
        }
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(sourcePixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun drawLocalCandidateSourceIcon(icon: Drawable, width: Int, height: Int): Bitmap =
        drawDrawable(
            drawable = icon,
            width = width,
            height = height,
            transparent = icon is AdaptiveIconDrawable,
        )

    private fun buildLocalIconLayers(icon: Drawable): LocalIconLayers {
        val renderSize = SIZE_1X1 * LOCAL_ICON_RENDER_SCALE
        if (icon is AdaptiveIconDrawable) {
            val background = drawDrawable(
                icon.background ?: ColorDrawable(AndroidColor.WHITE),
                renderSize,
                renderSize,
                transparent = false,
            )
            val directForeground = drawDrawable(icon.foreground, renderSize, renderSize, transparent = true)
            val composed = drawDrawable(icon, renderSize, renderSize, transparent = true)
            val foreground = subtractBackground(composed, background)
            val recbg = resizeBitmap(background, SIZE_1X1, SIZE_1X1)
            val foregroundSelection = chooseBetterAdaptiveForeground(foreground, directForeground, background)
            val monochrome = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                icon.monochrome?.let {
                    resizeBitmap(drawDrawable(it, renderSize, renderSize, transparent = true), SIZE_1X1, SIZE_1X1)
                }
            } else {
                null
            }?.takeIf { alphaCoverage(it) >= MONOCHROME_MIN_COVERAGE }
            val selectedForeground = resizeBitmap(
                foregroundSelection.bitmap,
                SIZE_1X1,
                SIZE_1X1,
            )
            val recfg = removeCornerMaskResidue(selectedForeground, recbg)
            val textSafeRecfg = removeCornerMaskResidue(
                source = selectedForeground,
                background = recbg,
                removeNearWhite = false,
            )
            val componentCandidates = buildAdaptiveComponentCandidates(
                background = background,
                composed = composed,
                directForeground = directForeground,
                monochrome = monochrome,
            )
            return LocalIconLayers(
                recfg = recfg,
                recbg = recbg,
                monochrome = monochrome,
                monochromeIsNative = monochrome != null,
                preserveGeometry = foregroundSelection.preserveGeometry,
                textSafe = IconCandidate(
                    recfgRaw = textSafeRecfg,
                    recbg = recbg,
                    monochromeRaw = monochrome,
                    monochromeIsNative = monochrome != null,
                    preserveGeometry = foregroundSelection.preserveGeometry,
                ),
                componentSubject = componentCandidates.subject,
                componentBackground = componentCandidates.background,
            )
        }

        val source = resizeBitmap(drawDrawable(icon, renderSize, renderSize, transparent = true), SIZE_1X1, SIZE_1X1)
        val recbg = solidBitmap(SIZE_1X1, SIZE_1X1, estimatePlainIconBackground(source))
        return LocalIconLayers(
            recfg = subtractPlainIconBackground(source, recbg),
            recbg = recbg,
            monochrome = null,
            monochromeIsNative = false,
            preserveGeometry = false,
            textSafe = null,
            componentSubject = null,
            componentBackground = null,
        )
    }

    private fun buildAdaptiveComponentCandidates(
        background: Bitmap,
        composed: Bitmap,
        directForeground: Bitmap,
        monochrome: Bitmap?,
    ): ComponentCandidates {
        if (
            !hasDetailedAdaptiveBackground(background) ||
            !isUsableDirectAdaptiveForeground(
                source = directForeground,
                composedCoverage = alphaCoverage(subtractBackground(composed, background)),
            )
        ) {
            return ComponentCandidates(subject = null, background = null)
        }
        val cleanBackground = estimateAdaptiveCleanBackground(background)
        val subjectForeground = subtractPlainIconBackground(composed, cleanBackground)
        val subjectCandidate = IconCandidate(
            recfgRaw = resizeBitmap(subjectForeground, SIZE_1X1, SIZE_1X1),
            recbg = resizeBitmap(cleanBackground, SIZE_1X1, SIZE_1X1),
            monochromeRaw = null,
            preserveGeometry = true,
        )
        val backgroundCandidate = IconCandidate(
            recfgRaw = resizeBitmap(repairTransparentEdgeColors(directForeground), SIZE_1X1, SIZE_1X1),
            recbg = resizeBitmap(background, SIZE_1X1, SIZE_1X1),
            monochromeRaw = monochrome,
            monochromeIsNative = monochrome != null,
            preserveGeometry = true,
        )
        return ComponentCandidates(subject = subjectCandidate, background = backgroundCandidate)
    }

    private fun chooseBetterAdaptiveForeground(
        fromComposed: Bitmap,
        directForeground: Bitmap,
        background: Bitmap,
    ): AdaptiveForegroundSelection {
        val composedBounds = alphaBounds(fromComposed, LOCAL_ALPHA_VISIBLE_THRESHOLD)
        val directBounds = alphaBounds(directForeground, LOCAL_ALPHA_VISIBLE_THRESHOLD)
        if (composedBounds == null) {
            return AdaptiveForegroundSelection(repairTransparentEdgeColors(directForeground), preserveGeometry = true)
        }
        if (directBounds == null) {
            return AdaptiveForegroundSelection(fromComposed, preserveGeometry = false)
        }
        if (adaptiveForegroundMode == AdaptiveForegroundMode.Composed) {
            return AdaptiveForegroundSelection(fromComposed, preserveGeometry = false)
        }
        if (adaptiveForegroundMode == AdaptiveForegroundMode.Direct) {
            return AdaptiveForegroundSelection(repairTransparentEdgeColors(directForeground), preserveGeometry = true)
        }
        if (
            hasDetailedAdaptiveBackground(background) &&
            isUsableDirectAdaptiveForeground(
                source = directForeground,
                composedCoverage = alphaCoverage(fromComposed),
            )
        ) {
            return AdaptiveForegroundSelection(repairTransparentEdgeColors(directForeground), preserveGeometry = true)
        }
        if (shouldPreferDirectAdaptiveForeground(fromComposed, directForeground, background)) {
            return AdaptiveForegroundSelection(repairTransparentEdgeColors(directForeground), preserveGeometry = true)
        }
        val composedCenterOffset = centerOffsetRatio(composedBounds, fromComposed.width, fromComposed.height)
        val directCenterOffset = centerOffsetRatio(directBounds, directForeground.width, directForeground.height)
        if (
            hasAdaptiveMaskArtifact(fromComposed) &&
            isUsableDirectAdaptiveForeground(
                source = directForeground,
                composedCoverage = alphaCoverage(fromComposed),
            )
        ) {
            return AdaptiveForegroundSelection(repairTransparentEdgeColors(directForeground), preserveGeometry = true)
        }
        if (alphaCoverage(directForeground) > ratioPercent(adaptiveDirectMaxCoveragePercent)) {
            return AdaptiveForegroundSelection(fromComposed, preserveGeometry = false)
        }
        return if (composedCenterOffset <= directCenterOffset + ratioPercent(adaptiveCenterEpsilonPercent)) {
            AdaptiveForegroundSelection(fromComposed, preserveGeometry = false)
        } else {
            AdaptiveForegroundSelection(repairTransparentEdgeColors(directForeground), preserveGeometry = true)
        }
    }

    private fun hasDetailedAdaptiveBackground(background: Bitmap): Boolean {
        val width = background.width
        val height = background.height
        if (width <= 0 || height <= 0) {
            return false
        }
        val pixels = IntArray(width * height)
        background.getPixels(pixels, 0, width, 0, 0, width, height)
        val baseColor = medianVisibleColor(background)
        var visible = 0
        var detail = 0
        for (pixel in pixels) {
            if (AndroidColor.alpha(pixel) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                continue
            }
            visible++
            if (colorDistance(pixel, baseColor) >= ADAPTIVE_BACKGROUND_DETAIL_DISTANCE) {
                detail++
            }
        }
        if (visible == 0) {
            return false
        }
        val detailRatio = detail.toDouble() / visible.toDouble()
        return detailRatio in ADAPTIVE_BACKGROUND_DETAIL_MIN_RATIO..ADAPTIVE_BACKGROUND_DETAIL_MAX_RATIO
    }

    private fun estimateAdaptiveCleanBackground(background: Bitmap): Bitmap {
        val width = background.width
        val height = background.height
        if (width <= 0 || height <= 0) {
            return background
        }
        val cornerSize = maxOf(4, (minOf(width, height) * ADAPTIVE_CLEAN_CORNER_RATIO + 0.5f).toInt())
        val topLeft = medianVisibleColorInRect(background, 0, 0, cornerSize, cornerSize)
        val topRight = medianVisibleColorInRect(background, width - cornerSize, 0, width, cornerSize)
        val bottomLeft = medianVisibleColorInRect(background, 0, height - cornerSize, cornerSize, height)
        val bottomRight = medianVisibleColorInRect(
            background,
            width - cornerSize,
            height - cornerSize,
            width,
            height,
        )
        val colors = intArrayOf(topLeft, topRight, bottomLeft, bottomRight)
        var maxDistance = 0.0
        for (i in colors.indices) {
            for (j in i + 1 until colors.size) {
                maxDistance = maxOf(maxDistance, colorDistance(colors[i], colors[j]))
            }
        }
        if (maxDistance <= ADAPTIVE_CLEAN_SOLID_DISTANCE) {
            return solidBitmap(width, height, medianColor(colors))
        }

        val outPixels = IntArray(width * height)
        for (y in 0 until height) {
            val fy = if (height <= 1) 0.0 else y.toDouble() / (height - 1).toDouble()
            for (x in 0 until width) {
                val fx = if (width <= 1) 0.0 else x.toDouble() / (width - 1).toDouble()
                val top = lerpColor(topLeft, topRight, fx)
                val bottom = lerpColor(bottomLeft, bottomRight, fx)
                outPixels[y * width + x] = lerpColor(top, bottom, fy)
            }
        }
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun shouldPreferDirectAdaptiveForeground(
        fromComposed: Bitmap,
        directForeground: Bitmap,
        background: Bitmap,
    ): Boolean {
        val directCoverage = alphaCoverage(directForeground)
        val composedCoverage = alphaCoverage(fromComposed)
        if (directCoverage < ADAPTIVE_DIRECT_FULL_PLATE_COVERAGE) {
            return false
        }
        if (composedCoverage >= directCoverage - ADAPTIVE_DIRECT_MIN_LOST_COVERAGE) {
            return false
        }
        val directBounds = alphaBounds(directForeground, LOCAL_ALPHA_VISIBLE_THRESHOLD) ?: return false
        if (!hasAutoCropRisk(directBounds, directForeground.width, directForeground.height)) {
            return false
        }
        val backgroundColor = medianVisibleColor(background)
        return hasLayeredAdaptiveForegroundPlate(directForeground, backgroundColor)
    }

    private fun hasLayeredAdaptiveForegroundPlate(source: Bitmap, backgroundColor: Int): Boolean {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        var visible = 0
        var plateLike = 0
        var detailLike = 0
        for (pixel in pixels) {
            if (AndroidColor.alpha(pixel) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                continue
            }
            visible++
            val saturation = saturation(pixel)
            val luma = luma(pixel)
            if (
                luma >= ADAPTIVE_DIRECT_PLATE_MIN_LUMA &&
                saturation <= ADAPTIVE_DIRECT_PLATE_MAX_SATURATION &&
                colorDistance(pixel, backgroundColor) >= ADAPTIVE_DIRECT_PLATE_BACKGROUND_DISTANCE
            ) {
                plateLike++
            } else if (colorDistance(pixel, backgroundColor) >= ADAPTIVE_DIRECT_DETAIL_BACKGROUND_DISTANCE) {
                detailLike++
            }
        }
        if (visible == 0) {
            return false
        }
        val plateRatio = plateLike.toDouble() / visible.toDouble()
        val detailRatio = detailLike.toDouble() / visible.toDouble()
        return plateRatio >= ADAPTIVE_DIRECT_PLATE_MIN_RATIO &&
            detailRatio >= ADAPTIVE_DIRECT_DETAIL_MIN_RATIO
    }

    private fun estimatePlainIconBackground(source: Bitmap): Int {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val bounds = alphaBounds(source, LOCAL_ALPHA_VISIBLE_THRESHOLD)
        if (bounds == null) {
            return AndroidColor.WHITE
        }
        val band = maxOf(2, (minOf(bounds.width(), bounds.height()) * PLAIN_ICON_EDGE_BAND_RATIO + 0.5f).toInt())
        val reds = mutableListOf<Int>()
        val greens = mutableListOf<Int>()
        val blues = mutableListOf<Int>()
        fun addColor(pixel: Int) {
            reds += AndroidColor.red(pixel)
            greens += AndroidColor.green(pixel)
            blues += AndroidColor.blue(pixel)
        }
        for (y in bounds.top until bounds.bottom) {
            for (x in bounds.left until bounds.right) {
                if (!isInEdgeBand(x, y, bounds, band)) {
                    continue
                }
                val pixel = pixels[y * width + x]
                if (AndroidColor.alpha(pixel) > PLAIN_ICON_BACKGROUND_ALPHA_THRESHOLD) {
                    addColor(pixel)
                }
            }
        }
        if (reds.size < PLAIN_ICON_MIN_BACKGROUND_SAMPLES) {
            for (pixel in pixels) {
                if (AndroidColor.alpha(pixel) > PLAIN_ICON_BACKGROUND_ALPHA_THRESHOLD) {
                    addColor(pixel)
                }
            }
        }
        if (reds.isEmpty()) {
            return AndroidColor.WHITE
        }
        return AndroidColor.rgb(median(reds), median(greens), median(blues))
    }

    private fun subtractPlainIconBackground(source: Bitmap, background: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val sourcePixels = IntArray(width * height)
        val backgroundPixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
        val bg = if (background.width == width && background.height == height) {
            background
        } else {
            resizeBitmap(background, width, height)
        }
        bg.getPixels(backgroundPixels, 0, width, 0, 0, width, height)
        val transparentDistance = ADAPTIVE_SUBTRACT_TRANSPARENT_DISTANCE
        val opaqueDistance = effectiveBackgroundSeparationDistance()

        for (i in sourcePixels.indices) {
            val pixel = sourcePixels[i]
            val sourceAlpha = AndroidColor.alpha(pixel)
            if (sourceAlpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                outPixels[i] = AndroidColor.TRANSPARENT
                continue
            }
            val distance = colorDistance(pixel, backgroundPixels[i])
            if (distance <= transparentDistance) {
                outPixels[i] = AndroidColor.TRANSPARENT
                continue
            }
            val alpha = ((distance - transparentDistance) / (opaqueDistance - transparentDistance))
                .coerceIn(0.0, 1.0)
            val outAlpha = (sourceAlpha * alpha).toInt().coerceIn(0, 255)
            outPixels[i] = if (outAlpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                AndroidColor.TRANSPARENT
            } else {
                val restored = restoredForegroundColor(
                    visiblePixel = pixel,
                    backgroundPixel = backgroundPixels[i],
                    foregroundAlpha = alpha,
                )
                AndroidColor.argb(
                    outAlpha,
                    AndroidColor.red(restored),
                    AndroidColor.green(restored),
                    AndroidColor.blue(restored),
                )
            }
        }

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return repairTransparentEdgeColors(out)
    }

    private fun hasAdaptiveMaskArtifact(source: Bitmap): Boolean {
        val bounds = alphaBounds(source, LOCAL_ALPHA_VISIBLE_THRESHOLD) ?: return false
        if (!hasAutoCropRisk(bounds, source.width, source.height)) {
            return false
        }
        val pixels = IntArray(source.width * source.height)
        source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
        val edge = dominantEdgeColor(pixels, source.width, bounds)
        return edge.coverage >= ratioPercent(adaptiveMaskEdgeCoveragePercent) &&
            alphaCoverage(source) >= ratioPercent(adaptiveMaskMinCoveragePercent)
    }

    private fun isUsableDirectAdaptiveForeground(source: Bitmap, composedCoverage: Double): Boolean {
        val coverage = alphaCoverage(source)
        if (coverage !in ADAPTIVE_DIRECT_MIN_COVERAGE..ratioPercent(adaptiveDirectMaxCoveragePercent)) {
            return false
        }
        if (coverage > composedCoverage + ratioPercent(adaptiveDirectMaxCoverageIncreasePercent)) {
            return false
        }
        val bounds = alphaBounds(source, LOCAL_ALPHA_VISIBLE_THRESHOLD) ?: return false
        return !hasAutoCropRisk(bounds, source.width, source.height)
    }

    private fun ratioPercent(percent: Int): Double =
        percent.coerceIn(0, 100).toDouble() / 100.0

    private fun effectiveBackgroundSeparationDistance(): Double =
        lerpDouble(LEGACY_BACKGROUND_SEPARATION_MIN, LEGACY_BACKGROUND_SEPARATION_MAX, ratioPercent(backgroundSeparationPercent))

    private fun effectivePlateRemovalDistance(): Double =
        lerpDouble(LEGACY_PLATE_REMOVAL_MIN, LEGACY_PLATE_REMOVAL_MAX, ratioPercent(plateRemovalPercent))

    private fun effectiveShadowRemovalAlpha(): Int =
        lerpDouble(LEGACY_SHADOW_REMOVAL_MIN, LEGACY_SHADOW_REMOVAL_MAX, ratioPercent(shadowRemovalPercent))
            .toInt()
            .coerceIn(0, 255)

    private fun effectiveShadowMaxSaturation(): Double =
        lerpDouble(SHADOW_MAX_SATURATION_MIN, SHADOW_MAX_SATURATION_MAX, ratioPercent(shadowRemovalPercent))

    private fun effectiveShadowMaxLuminance(): Int =
        lerpDouble(SHADOW_MAX_LUMINANCE_MIN, SHADOW_MAX_LUMINANCE_MAX, ratioPercent(shadowRemovalPercent))
            .roundToInt()
            .coerceIn(0, 255)

    private fun effectiveShadowMinVisibleRatio(): Double =
        lerpDouble(SHADOW_MIN_VISIBLE_RATIO_MAX, SHADOW_MIN_VISIBLE_RATIO_MIN, ratioPercent(shadowRemovalPercent))

    private fun effectiveShadowMinOffset(): Double =
        lerpDouble(SHADOW_MIN_OFFSET_MAX, SHADOW_MIN_OFFSET_MIN, ratioPercent(shadowRemovalPercent))

    private fun effectiveShadowMinDownOffset(): Double =
        lerpDouble(SHADOW_MIN_DOWN_OFFSET_MAX, SHADOW_MIN_DOWN_OFFSET_MIN, ratioPercent(shadowRemovalPercent))

    private fun effectiveShadowMinLumaDrop(): Int =
        lerpDouble(SHADOW_MIN_LUMA_DROP_MAX, SHADOW_MIN_LUMA_DROP_MIN, ratioPercent(shadowRemovalPercent))
            .roundToInt()
            .coerceAtLeast(0)

    private fun lerpDouble(start: Double, end: Double, ratio: Double): Double =
        start + (end - start) * ratio.coerceIn(0.0, 1.0)

    private fun subtractBackground(
        composed: Bitmap,
        background: Bitmap,
        colorSource: Bitmap? = null,
    ): Bitmap {
        val width = composed.width
        val height = composed.height
        val composedPixels = IntArray(width * height)
        val backgroundPixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        composed.getPixels(composedPixels, 0, width, 0, 0, width, height)
        val bg = if (background.width == width && background.height == height) {
            background
        } else {
            resizeBitmap(background, width, height)
        }
        bg.getPixels(backgroundPixels, 0, width, 0, 0, width, height)
        val colorSourcePixels = colorSource?.let { source ->
            val sized = if (source.width == width && source.height == height) {
                source
            } else {
                resizeBitmap(source, width, height)
            }
            IntArray(width * height).also { pixels ->
                sized.getPixels(pixels, 0, width, 0, 0, width, height)
            }
        }
        val transparentDistance = ADAPTIVE_SUBTRACT_TRANSPARENT_DISTANCE
        val opaqueDistance = effectiveBackgroundSeparationDistance()

        for (i in composedPixels.indices) {
            val composedAlpha = AndroidColor.alpha(composedPixels[i])
            if (composedAlpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                outPixels[i] = AndroidColor.TRANSPARENT
                continue
            }
            val distance = colorDistance(composedPixels[i], backgroundPixels[i])
            if (distance <= transparentDistance) {
                outPixels[i] = AndroidColor.TRANSPARENT
                continue
            }
            val alpha = ((distance - transparentDistance) / (opaqueDistance - transparentDistance))
                .coerceIn(0.0, 1.0)
            val outAlpha = (composedAlpha * alpha).toInt().coerceIn(0, 255)
            if (outAlpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                outPixels[i] = AndroidColor.TRANSPARENT
                continue
            }
            val restored = restoredForegroundColor(
                visiblePixel = composedPixels[i],
                backgroundPixel = backgroundPixels[i],
                foregroundAlpha = alpha,
                colorSourcePixel = colorSourcePixels?.get(i),
            )
            outPixels[i] = AndroidColor.argb(
                outAlpha,
                AndroidColor.red(restored),
                AndroidColor.green(restored),
                AndroidColor.blue(restored),
            )
        }

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return repairTransparentEdgeColors(out)
    }

    private fun restoredForegroundColor(
        visiblePixel: Int,
        backgroundPixel: Int,
        foregroundAlpha: Double,
        colorSourcePixel: Int? = null,
    ): Int {
        if (
            colorSourcePixel != null &&
            AndroidColor.alpha(colorSourcePixel) > LOCAL_ALPHA_VISIBLE_THRESHOLD
        ) {
            return AndroidColor.rgb(
                AndroidColor.red(colorSourcePixel),
                AndroidColor.green(colorSourcePixel),
                AndroidColor.blue(colorSourcePixel),
            )
        }
        val alpha = foregroundAlpha.coerceIn(0.001, 1.0)
        return AndroidColor.rgb(
            uncompositeChannel(AndroidColor.red(visiblePixel), AndroidColor.red(backgroundPixel), alpha),
            uncompositeChannel(AndroidColor.green(visiblePixel), AndroidColor.green(backgroundPixel), alpha),
            uncompositeChannel(AndroidColor.blue(visiblePixel), AndroidColor.blue(backgroundPixel), alpha),
        )
    }

    private fun centerOffsetRatio(bounds: Bounds, width: Int, height: Int): Double {
        val dx = bounds.left + bounds.width() / 2.0 - width / 2.0
        val dy = bounds.top + bounds.height() / 2.0 - height / 2.0
        return kotlin.math.sqrt(dx * dx + dy * dy) / maxOf(width, height).toDouble()
    }

    private fun solidBitmap(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawColor(color)
        return bitmap
    }

    private fun resizeBitmap(source: Bitmap, width: Int, height: Int): Bitmap =
        Bitmap.createScaledBitmap(source, width, height, true)

    private fun centerOnCanvas(source: Bitmap, width: Int, height: Int): Bitmap {
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(AndroidColor.TRANSPARENT)
        val x = (width - source.width) / 2f
        val y = (height - source.height) / 2f
        canvas.drawBitmap(source, x, y, null)
        return out
    }

    private fun separateLocalForeground(source: Bitmap, background: Bitmap, mode: LocalSeparationMode): LocalSeparationResult {
        if (
            mode == LocalSeparationMode.Original ||
            mode == LocalSeparationMode.ComposedBackground ||
            mode == LocalSeparationMode.ComponentSubject ||
            mode == LocalSeparationMode.ComponentBackground
        ) {
            if (mode != LocalSeparationMode.ComposedBackground) {
                return LocalSeparationResult(source, "${mode.label}: 不清理")
            }
        }

        var current = source
        val actions = mutableListOf<String>()

        if (
            mode == LocalSeparationMode.Auto ||
            mode == LocalSeparationMode.Plate ||
            mode == LocalSeparationMode.Full ||
            mode == LocalSeparationMode.ComposedBackground
        ) {
            val plate = removeForegroundPlate(current)
            current = plate.bitmap
            if (plate.changed) {
                actions += "去底板 ${percentText(plate.removedRatio)}"
                if (plate.repairedRatio > 0.0) {
                    actions += "修边 ${percentText(plate.repairedRatio)}"
                }
            } else if (mode == LocalSeparationMode.Plate) {
                actions += "未触发底板"
            }
        }

        if (
            mode == LocalSeparationMode.Auto ||
            mode == LocalSeparationMode.Full ||
            mode == LocalSeparationMode.ComposedBackground
        ) {
            val shadow = removeOffsetShadow(current, background)
            current = shadow.bitmap
            if (shadow.changed) {
                actions += "柔化长阴影 ${percentText(shadow.removedRatio)}"
            }
        }

        current = trimForegroundEdge(current)
        actions += "收边"

        val prefix = when (mode) {
            LocalSeparationMode.Auto -> "自动"
            LocalSeparationMode.Original -> "原始"
            LocalSeparationMode.Plate -> "清理"
            LocalSeparationMode.ComposedBackground -> "拼合背景"
            LocalSeparationMode.ComponentSubject -> "组件主体"
            LocalSeparationMode.ComponentBackground -> "组件背景"
            LocalSeparationMode.Full -> "清理"
        }
        return LocalSeparationResult(
            bitmap = current,
            summary = if (actions.isEmpty()) "$prefix: 保持原样" else "$prefix: ${actions.joinToString(" / ")}",
        )
    }

    private fun removeForegroundPlate(source: Bitmap): ForegroundCleanupResult {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val bounds = alphaBounds(source, LOCAL_ALPHA_VISIBLE_THRESHOLD)
            ?: return ForegroundCleanupResult(source, changed = false, removedRatio = 0.0, repairedRatio = 0.0)
        val edge = dominantEdgeColor(pixels, width, bounds)
        val borderCoverageThreshold = PLATE_BORDER_COVERAGE_THRESHOLD
        val colorDistanceThreshold = effectivePlateRemovalDistance()
        val minRemovedRatio = PLATE_MIN_REMOVED_RATIO
        if (edge.coverage < borderCoverageThreshold) {
            return ForegroundCleanupResult(source, changed = false, removedRatio = 0.0, repairedRatio = 0.0)
        }

        val plateLike = BooleanArray(pixels.size)
        var visible = 0
        for (i in pixels.indices) {
            val pixel = pixels[i]
            if (AndroidColor.alpha(pixel) > LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                visible++
                if (colorDistance(pixel, edge.color) <= colorDistanceThreshold) {
                    plateLike[i] = true
                }
            }
        }
        if (visible == 0) {
            return ForegroundCleanupResult(source, changed = false, removedRatio = 0.0, repairedRatio = 0.0)
        }

        val band = maxOf(2, (minOf(bounds.width(), bounds.height()) * EDGE_BAND_RATIO + 0.5f).toInt())
        val plate = floodFillEdgeConnectedMask(plateLike, width, height, bounds, band)
        val removed = plate.count { it }
        val removedRatio = removed.toDouble() / visible.toDouble()
        if (removedRatio < minRemovedRatio) {
            return ForegroundCleanupResult(source, changed = false, removedRatio = removedRatio, repairedRatio = 0.0)
        }

        val cleaned = pixels.copyOf()
        for (i in cleaned.indices) {
            if (plate[i]) {
                cleaned[i] = AndroidColor.TRANSPARENT
            }
        }
        val repaired = repairPlateEdges(pixels, cleaned, plate, width, height, edge.color)

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(cleaned, 0, width, 0, 0, width, height)
        val cleanedBitmap = removeBackgroundColoredResidue(out, edge.color)
        if (isUnsafePlateRemoval(source, cleanedBitmap)) {
            return ForegroundCleanupResult(source, changed = false, removedRatio = removedRatio, repairedRatio = 0.0)
        }
        return ForegroundCleanupResult(
            bitmap = cleanedBitmap,
            changed = true,
            removedRatio = removedRatio,
            repairedRatio = repaired.toDouble() / visible.toDouble(),
        )
    }

    private fun isUnsafePlateRemoval(source: Bitmap, cleaned: Bitmap): Boolean {
        val sourceCoverage = meaningfulAlphaCoverage(source)
        val cleanedCoverage = meaningfulAlphaCoverage(cleaned)
        if (cleanedCoverage <= PLATE_MIN_SAFE_REMAINING_COVERAGE) {
            return true
        }
        if (sourceCoverage <= 0.0) {
            return false
        }
        val keptRatio = cleanedCoverage / sourceCoverage
        if (keptRatio < PLATE_MIN_SAFE_KEEP_RATIO) {
            return true
        }
        val sourceBounds = meaningfulAlphaBounds(source) ?: return false
        val cleanedBounds = meaningfulAlphaBounds(cleaned) ?: return true
        val sourceMax = maxOf(sourceBounds.width(), sourceBounds.height()).toDouble()
        val cleanedMax = maxOf(cleanedBounds.width(), cleanedBounds.height()).toDouble()
        return sourceMax > 0.0 && cleanedMax / sourceMax < PLATE_MIN_SAFE_BOUNDS_RATIO
    }

    private fun removeCornerMaskResidue(
        source: Bitmap,
        background: Bitmap,
        removeNearWhite: Boolean = true,
    ): Bitmap {
        val width = source.width
        val height = source.height
        if (width <= 0 || height <= 0) {
            return source
        }

        val pixels = IntArray(width * height)
        val backgroundPixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val normalizedBackground = if (background.width == width && background.height == height) {
            background
        } else {
            resizeBitmap(background, width, height)
        }
        normalizedBackground.getPixels(backgroundPixels, 0, width, 0, 0, width, height)

        val candidate = BooleanArray(pixels.size)
        for (i in pixels.indices) {
            candidate[i] = isCornerMaskResidueCandidate(
                pixel = pixels[i],
                backgroundPixel = backgroundPixels[i],
                removeNearWhite = removeNearWhite,
            )
        }

        val remove = BooleanArray(pixels.size)
        val queue = ArrayDeque<Int>()
        fun enqueue(index: Int) {
            if (candidate[index] && !remove[index]) {
                remove[index] = true
                queue.add(index)
            }
        }

        val seed = minOf(CORNER_MASK_SEED_SIZE, width / 3, height / 3).coerceAtLeast(1)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val inCorner = (x < seed || x >= width - seed) && (y < seed || y >= height - seed)
                val onBorder = x == 0 || y == 0 || x == width - 1 || y == height - 1
                if (inCorner || onBorder) {
                    enqueue(y * width + x)
                }
            }
        }

        while (!queue.isEmpty()) {
            val index = queue.removeFirst()
            val x = index % width
            val y = index / width
            for (dy in -1..1) {
                for (dx in -1..1) {
                    if (dx == 0 && dy == 0) {
                        continue
                    }
                    val nx = x + dx
                    val ny = y + dy
                    if (nx !in 0 until width || ny !in 0 until height) {
                        continue
                    }
                    if (!isInCornerMaskZone(nx, ny, width, height)) {
                        continue
                    }
                    enqueue(ny * width + nx)
                }
            }
        }

        val removed = remove.count { it }
        if (removed == 0) {
            return source
        }
        val visible = pixels.count { AndroidColor.alpha(it) > LOCAL_ALPHA_VISIBLE_THRESHOLD }
        if (visible == 0 || removed.toDouble() / visible.toDouble() > CORNER_MASK_MAX_REMOVED_RATIO) {
            return source
        }

        val cleaned = pixels.copyOf()
        for (i in cleaned.indices) {
            if (remove[i]) {
                cleaned[i] = AndroidColor.TRANSPARENT
            }
        }

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(cleaned, 0, width, 0, 0, width, height)
        return repairTransparentEdgeColors(out)
    }

    private fun isNearWhite(pixel: Int): Boolean =
        AndroidColor.red(pixel) >= CORNER_MASK_WHITE_THRESHOLD &&
            AndroidColor.green(pixel) >= CORNER_MASK_WHITE_THRESHOLD &&
            AndroidColor.blue(pixel) >= CORNER_MASK_WHITE_THRESHOLD

    private fun isCornerMaskResidueCandidate(
        pixel: Int,
        backgroundPixel: Int,
        removeNearWhite: Boolean,
    ): Boolean {
        val alpha = AndroidColor.alpha(pixel)
        if (alpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
            return false
        }

        val backgroundDistance = colorDistance(pixel, backgroundPixel)
        val subjectLike = alpha >= CORNER_MASK_SUBJECT_ALPHA &&
            backgroundDistance >= CORNER_MASK_SUBJECT_BACKGROUND_DISTANCE
        if (subjectLike) {
            return false
        }

        val nearBackground = backgroundDistance <= CORNER_MASK_BACKGROUND_DISTANCE
        val weakMaskEdge = alpha < CORNER_MASK_OPAQUE_ALPHA && nearBackground
        val nearWhiteMaskEdge = removeNearWhite &&
            isNearWhite(pixel) &&
            (nearBackground || alpha < CORNER_MASK_WHITE_EDGE_ALPHA)
        return nearBackground || weakMaskEdge || nearWhiteMaskEdge
    }

    private fun isInCornerMaskZone(x: Int, y: Int, width: Int, height: Int): Boolean {
        val zone = minOf(CORNER_MASK_ZONE_SIZE, width / 2, height / 2).coerceAtLeast(1)
        return (x < zone || x >= width - zone) && (y < zone || y >= height - zone)
    }

    private fun removeBackgroundColoredResidue(source: Bitmap, backgroundColor: Int): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val canRemoveSaturatedResidue = saturation(backgroundColor) >= RESIDUE_BACKGROUND_MIN_SATURATION
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = AndroidColor.alpha(pixel)
            if (alpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                outPixels[i] = AndroidColor.TRANSPARENT
                continue
            }
            val distance = colorDistance(pixel, backgroundColor)
            if (
                canRemoveSaturatedResidue &&
                alpha <= RESIDUE_MAX_ALPHA &&
                distance <= residueDistanceThreshold()
            ) {
                outPixels[i] = AndroidColor.TRANSPARENT
            } else {
                outPixels[i] = pixel
            }
        }
        val cleanedPixels = if (canRemoveSaturatedResidue) {
            removeEdgeConnectedColorResidue(outPixels, width, height, backgroundColor)
        } else {
            outPixels
        }
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(cleanedPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun residueDistanceThreshold(): Double =
        (effectivePlateRemovalDistance() * RESIDUE_DISTANCE_SCALE)
            .coerceIn(RESIDUE_MIN_DISTANCE, RESIDUE_MAX_DISTANCE)

    private fun edgeConnectedResidueDistanceThreshold(): Double =
        (effectivePlateRemovalDistance() * RESIDUE_CONNECTED_DISTANCE_SCALE)
            .coerceIn(RESIDUE_CONNECTED_MIN_DISTANCE, RESIDUE_CONNECTED_MAX_DISTANCE)

    private fun removeEdgeConnectedColorResidue(
        pixels: IntArray,
        width: Int,
        height: Int,
        backgroundColor: Int,
    ): IntArray {
        val threshold = edgeConnectedResidueDistanceThreshold()
        val candidate = BooleanArray(pixels.size)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = AndroidColor.alpha(pixel)
            candidate[i] = alpha > LOCAL_ALPHA_VISIBLE_THRESHOLD &&
                alpha <= RESIDUE_CONNECTED_MAX_ALPHA &&
                colorDistance(pixel, backgroundColor) <= threshold
        }

        val remove = BooleanArray(pixels.size)
        val queue = ArrayDeque<Int>()
        fun enqueue(index: Int) {
            if (candidate[index] && !remove[index]) {
                remove[index] = true
                queue.add(index)
            }
        }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                if (!candidate[index]) {
                    continue
                }
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1 ||
                    hasNearbyTransparentPixel(pixels, width, height, x, y, RESIDUE_CONNECTED_TRANSPARENT_RADIUS)
                ) {
                    enqueue(index)
                }
            }
        }

        while (!queue.isEmpty()) {
            val index = queue.removeFirst()
            val x = index % width
            val y = index / width
            for (dy in -1..1) {
                for (dx in -1..1) {
                    if (dx == 0 && dy == 0) {
                        continue
                    }
                    val nx = x + dx
                    val ny = y + dy
                    if (nx !in 0 until width || ny !in 0 until height) {
                        continue
                    }
                    enqueue(ny * width + nx)
                }
            }
        }

        val out = pixels.copyOf()
        for (i in out.indices) {
            if (remove[i]) {
                out[i] = AndroidColor.TRANSPARENT
            }
        }
        return out
    }

    private fun dominantEdgeColor(pixels: IntArray, width: Int, bounds: Bounds): EdgeAnalysis {
        val band = maxOf(2, (minOf(bounds.width(), bounds.height()) * EDGE_BAND_RATIO + 0.5f).toInt())
        val reds = mutableListOf<Int>()
        val greens = mutableListOf<Int>()
        val blues = mutableListOf<Int>()
        var ringArea = 0
        var visibleRing = 0
        for (y in bounds.top until bounds.bottom) {
            for (x in bounds.left until bounds.right) {
                if (!isInEdgeBand(x, y, bounds, band)) {
                    continue
                }
                ringArea++
                val pixel = pixels[y * width + x]
                if (AndroidColor.alpha(pixel) > LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                    visibleRing++
                    reds += AndroidColor.red(pixel)
                    greens += AndroidColor.green(pixel)
                    blues += AndroidColor.blue(pixel)
                }
            }
        }
        if (ringArea == 0 || visibleRing == 0) {
            return EdgeAnalysis(coverage = 0.0, color = AndroidColor.BLACK)
        }
        val color = AndroidColor.rgb(median(reds), median(greens), median(blues))
        return EdgeAnalysis(
            coverage = visibleRing.toDouble() / ringArea.toDouble(),
            color = color,
        )
    }

    private fun floodFillEdgeConnectedMask(
        sourceMask: BooleanArray,
        width: Int,
        height: Int,
        bounds: Bounds,
        band: Int,
    ): BooleanArray {
        val out = BooleanArray(sourceMask.size)
        val queue = ArrayDeque<Int>()
        for (y in bounds.top until bounds.bottom) {
            for (x in bounds.left until bounds.right) {
                if (!isInEdgeBand(x, y, bounds, band)) {
                    continue
                }
                val index = y * width + x
                if (sourceMask[index] && !out[index]) {
                    out[index] = true
                    queue.add(index)
                }
            }
        }

        while (!queue.isEmpty()) {
            val index = queue.removeFirst()
            val x = index % width
            val y = index / width
            for (dy in -1..1) {
                for (dx in -1..1) {
                    if (dx == 0 && dy == 0) {
                        continue
                    }
                    val nx = x + dx
                    val ny = y + dy
                    if (nx !in 0 until width || ny !in 0 until height) {
                        continue
                    }
                    val next = ny * width + nx
                    if (sourceMask[next] && !out[next]) {
                        out[next] = true
                        queue.add(next)
                    }
                }
            }
        }
        return out
    }

    private fun repairPlateEdges(
        sourcePixels: IntArray,
        cleanedPixels: IntArray,
        plate: BooleanArray,
        width: Int,
        height: Int,
        plateColor: Int,
    ): Int {
        var repaired = 0
        val plateRed = AndroidColor.red(plateColor)
        val plateGreen = AndroidColor.green(plateColor)
        val plateBlue = AndroidColor.blue(plateColor)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                if (plate[index] || AndroidColor.alpha(sourcePixels[index]) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                    continue
                }
                if (!hasNearbyMaskPixel(plate, width, height, x, y, 2)) {
                    continue
                }
                val pixel = sourcePixels[index]
                val maxDelta = maxOf(
                    kotlin.math.abs(AndroidColor.red(pixel) - plateRed),
                    kotlin.math.abs(AndroidColor.green(pixel) - plateGreen),
                    kotlin.math.abs(AndroidColor.blue(pixel) - plateBlue),
                )
                val inferredAlpha = (maxDelta / 255.0).coerceIn(0.0, 1.0)
                if (inferredAlpha >= 0.985) {
                    continue
                }
                repaired++
                if (inferredAlpha <= 0.035) {
                    cleanedPixels[index] = AndroidColor.TRANSPARENT
                    continue
                }
                val red = uncompositeChannel(AndroidColor.red(pixel), plateRed, inferredAlpha)
                val green = uncompositeChannel(AndroidColor.green(pixel), plateGreen, inferredAlpha)
                val blue = uncompositeChannel(AndroidColor.blue(pixel), plateBlue, inferredAlpha)
                val alpha = (AndroidColor.alpha(pixel) * inferredAlpha).toInt().coerceIn(0, 255)
                cleanedPixels[index] = AndroidColor.argb(alpha, red, green, blue)
            }
        }
        return repaired
    }

    private fun removeOffsetShadow(source: Bitmap, background: Bitmap): ShadowCleanupResult {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        val backgroundPixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val bg = if (background.width == width && background.height == height) {
            background
        } else {
            resizeBitmap(background, width, height)
        }
        bg.getPixels(backgroundPixels, 0, width, 0, 0, width, height)

        var visible = 0
        var highCount = 0
        var highX = 0.0
        var highY = 0.0
        val highAlpha = BooleanArray(pixels.size)
        val shadowCandidate = BooleanArray(pixels.size)
        val alphaMax = effectiveShadowRemovalAlpha()
        val saturationMax = effectiveShadowMaxSaturation()
        val luminanceMax = effectiveShadowMaxLuminance()
        val minVisibleRatio = effectiveShadowMinVisibleRatio()
        val minOffset = effectiveShadowMinOffset()
        val minDownOffset = effectiveShadowMinDownOffset()
        val minLumaDrop = effectiveShadowMinLumaDrop()
        if (alphaMax <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
            return ShadowCleanupResult(source, changed = false, removedRatio = 0.0)
        }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val pixel = pixels[index]
                val alpha = AndroidColor.alpha(pixel)
                if (alpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                    continue
                }
                visible++
                if (alpha > SHADOW_HIGH_ALPHA_THRESHOLD) {
                    highAlpha[index] = true
                    highCount++
                    highX += x.toDouble()
                    highY += y.toDouble()
                }
                if (
                    alpha <= alphaMax &&
                    saturation(pixel) <= saturationMax &&
                    luma(pixel) <= luminanceMax
                ) {
                    shadowCandidate[index] = true
                }
            }
        }

        if (visible == 0 || highCount == 0) {
            return ShadowCleanupResult(source, changed = false, removedRatio = 0.0)
        }
        val highCenterX = highX / highCount.toDouble()
        val highCenterY = highY / highCount.toDouble()

        val selected = BooleanArray(pixels.size)
        connectedMaskComponents(shadowCandidate, width, height).forEach { component ->
            val componentRatio = component.size.toDouble() / visible.toDouble()
            if (componentRatio < minVisibleRatio) {
                return@forEach
            }
            var componentX = 0.0
            var componentY = 0.0
            val lumaDrops = mutableListOf<Int>()
            component.indices.forEach { index ->
                val x = index % width
                val y = index / width
                val sourcePixel = pixels[index]
                val bgPixel = backgroundPixels[index]
                val alpha = AndroidColor.alpha(sourcePixel) / 255.0
                val composedRed = AndroidColor.red(sourcePixel) * alpha + AndroidColor.red(bgPixel) * (1.0 - alpha)
                val composedGreen = AndroidColor.green(sourcePixel) * alpha + AndroidColor.green(bgPixel) * (1.0 - alpha)
                val composedBlue = AndroidColor.blue(sourcePixel) * alpha + AndroidColor.blue(bgPixel) * (1.0 - alpha)
                val drop = luma(bgPixel) - luma(composedRed, composedGreen, composedBlue)
                lumaDrops += drop.toInt()
                componentX += x.toDouble()
                componentY += y.toDouble()
            }
            val componentCenterX = componentX / component.size.toDouble()
            val componentCenterY = componentY / component.size.toDouble()
            val dx = componentCenterX - highCenterX
            val dy = componentCenterY - highCenterY
            val offset = kotlin.math.sqrt(dx * dx + dy * dy)
            val medianDrop = percentile(lumaDrops, 0.50)
            if (
                offset >= minOffset &&
                dy >= minDownOffset &&
                medianDrop >= minLumaDrop
            ) {
                component.indices.forEach { selected[it] = true }
            }
        }

        val cleaned = pixels.copyOf()
        var selectedCount = 0
        for (i in cleaned.indices) {
            if (!selected[i]) {
                continue
            }
            val x = i % width
            val y = i / width
            val distance = distanceToNearbyMaskPixel(
                mask = highAlpha,
                width = width,
                height = height,
                x = x,
                y = y,
                maxRadius = SHADOW_EDGE_ANTIALIAS_RADIUS,
            )
            if (distance != null && distance <= SHADOW_EDGE_ANTIALIAS_RADIUS.toDouble()) {
                nearestOpaqueNeighborColor(pixels, width, height, x, y)?.let { edgeColor ->
                    val alpha = AndroidColor.alpha(cleaned[i])
                        .coerceIn(LOCAL_ALPHA_VISIBLE_THRESHOLD + 1, SHADOW_EDGE_REPAIR_MAX_ALPHA)
                    cleaned[i] = AndroidColor.argb(
                        alpha,
                        AndroidColor.red(edgeColor),
                        AndroidColor.green(edgeColor),
                        AndroidColor.blue(edgeColor),
                    )
                }
                continue
            }
            selectedCount++
            cleaned[i] = AndroidColor.TRANSPARENT
        }
        if (selectedCount == 0) {
            return ShadowCleanupResult(source, changed = false, removedRatio = 0.0)
        }
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(cleaned, 0, width, 0, 0, width, height)
        return ShadowCleanupResult(
            bitmap = repairTransparentEdgeColors(out),
            changed = true,
            removedRatio = selectedCount.toDouble() / visible.toDouble(),
        )
    }

    private fun distanceToNearbyMaskPixel(
        mask: BooleanArray,
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        maxRadius: Int,
    ): Double? {
        var bestSquared = Int.MAX_VALUE
        for (dy in -maxRadius..maxRadius) {
            for (dx in -maxRadius..maxRadius) {
                val nx = x + dx
                val ny = y + dy
                if (nx !in 0 until width || ny !in 0 until height) {
                    continue
                }
                val squared = dx * dx + dy * dy
                if (squared > maxRadius * maxRadius || squared >= bestSquared) {
                    continue
                }
                if (mask[ny * width + nx]) {
                    bestSquared = squared
                }
            }
        }
        return if (bestSquared == Int.MAX_VALUE) null else kotlin.math.sqrt(bestSquared.toDouble())
    }

    private fun trimForegroundEdge(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val visible = BooleanArray(pixels.size)
        for (i in pixels.indices) {
            visible[i] = AndroidColor.alpha(pixels[i]) > LOCAL_ALPHA_VISIBLE_THRESHOLD
        }

        val eroded = BooleanArray(pixels.size)
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val index = y * width + x
                if (!visible[index]) {
                    continue
                }
                var keep = true
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        if (!visible[(y + dy) * width + (x + dx)]) {
                            keep = false
                            break
                        }
                    }
                    if (!keep) {
                        break
                    }
                }
                eroded[index] = keep
            }
        }

        val outPixels = pixels.copyOf()
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                if (!visible[index] || eroded[index]) {
                    continue
                }
                var nearEroded = false
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        val nx = x + dx
                        val ny = y + dy
                        if (nx !in 0 until width || ny !in 0 until height) {
                            continue
                        }
                        if (eroded[ny * width + nx]) {
                            nearEroded = true
                            break
                        }
                    }
                    if (nearEroded) {
                        break
                    }
                }
                val pixel = outPixels[index]
                if (!nearEroded) {
                    outPixels[index] = AndroidColor.TRANSPARENT
                    continue
                }
                val alpha = (AndroidColor.alpha(pixel) * FOREGROUND_EDGE_FEATHER_ALPHA_SCALE)
                    .toInt()
                    .coerceIn(0, 255)
                outPixels[index] = if (alpha <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
                    AndroidColor.TRANSPARENT
                } else {
                    AndroidColor.argb(
                        alpha,
                        AndroidColor.red(pixel),
                        AndroidColor.green(pixel),
                        AndroidColor.blue(pixel),
                    )
                }
            }
        }

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return repairTransparentEdgeColors(out)
    }

    private fun isInEdgeBand(x: Int, y: Int, bounds: Bounds, band: Int): Boolean =
        y < bounds.top + band ||
            y >= bounds.bottom - band ||
            x < bounds.left + band ||
            x >= bounds.right - band

    private fun hasNearbyMaskPixel(mask: BooleanArray, width: Int, height: Int, x: Int, y: Int, radius: Int): Boolean {
        val radiusSquared = radius * radius
        for (dy in -radius..radius) {
            for (dx in -radius..radius) {
                if (dx * dx + dy * dy > radiusSquared) {
                    continue
                }
                val nx = x + dx
                val ny = y + dy
                if (nx in 0 until width && ny in 0 until height && mask[ny * width + nx]) {
                    return true
                }
            }
        }
        return false
    }

    private fun hasNearbyTransparentPixel(
        pixels: IntArray,
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        radius: Int,
    ): Boolean {
        for (dy in -radius..radius) {
            for (dx in -radius..radius) {
                if (dx == 0 && dy == 0) {
                    continue
                }
                val nx = x + dx
                val ny = y + dy
                if (nx !in 0 until width || ny !in 0 until height) {
                    return true
                }
                if (AndroidColor.alpha(pixels[ny * width + nx]) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                    return true
                }
            }
        }
        return false
    }

    private fun uncompositeChannel(value: Int, plateValue: Int, alpha: Double): Int =
        (plateValue + (value - plateValue) / alpha).toInt().coerceIn(0, 255)

    private fun median(values: MutableList<Int>): Int {
        if (values.isEmpty()) {
            return 0
        }
        values.sort()
        return values[values.size / 2]
    }

    private fun percentText(value: Double): String =
        "${(value * 100.0).toInt().coerceIn(0, 100)}%"

    private fun saturation(pixel: Int): Double {
        val max = maxOf(AndroidColor.red(pixel), AndroidColor.green(pixel), AndroidColor.blue(pixel))
        if (max <= 0) {
            return 0.0
        }
        val min = minOf(AndroidColor.red(pixel), AndroidColor.green(pixel), AndroidColor.blue(pixel))
        return (max - min).toDouble() / max.toDouble()
    }

    private fun normalizeForegroundSubjectSize(source: Bitmap): Bitmap {
        val bounds = meaningfulAlphaBounds(source) ?: return source
        val originalCenter = meaningfulAlphaCentroid(source)
        val currentMax = maxOf(bounds.width(), bounds.height()).toFloat()
        if (currentMax <= 0f) {
            return source
        }
        val targetMax = source.width * (foregroundSubjectPercent.toFloat() / 100f)
        val scale = targetMax / currentMax
        if (scale in 0.97f..1.03f) {
            return source
        }
        val scaledWidth = (source.width * scale).toInt().coerceAtLeast(1)
        val scaledHeight = (source.height * scale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
        val scaledCenter = meaningfulAlphaCentroid(scaled) ?: return source
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(AndroidColor.TRANSPARENT)
        val originalCenterX = originalCenter?.first ?: (bounds.left + bounds.width() / 2f)
        val originalCenterY = originalCenter?.second ?: (bounds.top + bounds.height() / 2f)
        val dx = originalCenterX - scaledCenter.first
        val dy = originalCenterY - scaledCenter.second
        canvas.drawBitmap(scaled, dx, dy, null)
        return out
    }

    private fun scaleBitmapAroundAlphaCenter(source: Bitmap, scale: Float): Bitmap {
        val normalizedScale = scale.coerceIn(0.2f, 1.5f)
        if (normalizedScale in 0.97f..1.03f) {
            return source
        }
        val bounds = meaningfulAlphaBounds(source) ?: return source
        val originalCenter = meaningfulAlphaCentroid(source)
        val scaledWidth = (source.width * normalizedScale).toInt().coerceAtLeast(1)
        val scaledHeight = (source.height * normalizedScale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
        val scaledCenter = meaningfulAlphaCentroid(scaled) ?: return source
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(AndroidColor.TRANSPARENT)
        val originalCenterX = originalCenter?.first ?: (bounds.left + bounds.width() / 2f)
        val originalCenterY = originalCenter?.second ?: (bounds.top + bounds.height() / 2f)
        canvas.drawBitmap(scaled, originalCenterX - scaledCenter.first, originalCenterY - scaledCenter.second, null)
        return out
    }

    private fun meaningfulAlphaBounds(source: Bitmap): Bounds? =
        alphaBounds(source, NORMALIZE_ALPHA_BOUNDS_THRESHOLD)
            ?: alphaBounds(source, LOCAL_ALPHA_VISIBLE_THRESHOLD)

    private fun meaningfulAlphaCentroid(source: Bitmap): Pair<Float, Float>? =
        alphaCentroid(source, NORMALIZE_ALPHA_BOUNDS_THRESHOLD)
            ?: alphaCentroid(source, LOCAL_ALPHA_VISIBLE_THRESHOLD)

    private fun alphaCentroid(source: Bitmap, threshold: Int): Pair<Float, Float>? {
        var weight = 0.0
        var xSum = 0.0
        var ySum = 0.0
        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                val alpha = AndroidColor.alpha(source.getPixel(x, y))
                if (alpha <= threshold) {
                    continue
                }
                weight += alpha.toDouble()
                xSum += x * alpha.toDouble()
                ySum += y * alpha.toDouble()
            }
        }
        if (weight <= 0.0) {
            return null
        }
        return Pair((xSum / weight).toFloat(), (ySum / weight).toFloat())
    }

    private fun alphaBounds(source: Bitmap, threshold: Int): Bounds? {
        var left = source.width
        var top = source.height
        var right = -1
        var bottom = -1
        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                if (AndroidColor.alpha(source.getPixel(x, y)) > threshold) {
                    if (x < left) left = x
                    if (x > right) right = x
                    if (y < top) top = y
                    if (y > bottom) bottom = y
                }
            }
        }
        return if (right >= left && bottom >= top) {
            Bounds(left, top, right + 1, bottom + 1)
        } else {
            null
        }
    }

    private fun hasRealAlpha(source: Bitmap): Boolean {
        var transparent = 0
        var samples = 0
        for (y in 0 until source.height step maxOf(1, source.height / 128)) {
            for (x in 0 until source.width step maxOf(1, source.width / 128)) {
                samples++
                if (AndroidColor.alpha(source.getPixel(x, y)) < 8) {
                    transparent++
                }
            }
        }
        return samples > 0 && transparent.toDouble() / samples.toDouble() >= 0.05
    }

    private fun alphaCoverage(source: Bitmap): Double {
        var visible = 0
        val total = source.width * source.height
        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                if (AndroidColor.alpha(source.getPixel(x, y)) > 8) {
                    visible++
                }
            }
        }
        return if (total == 0) 0.0 else visible.toDouble() / total.toDouble()
    }

    private fun alphaCoverage(source: Bitmap, threshold: Int): Double {
        var visible = 0
        val total = source.width * source.height
        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                if (AndroidColor.alpha(source.getPixel(x, y)) > threshold) {
                    visible++
                }
            }
        }
        return if (total == 0) 0.0 else visible.toDouble() / total.toDouble()
    }

    private fun meaningfulAlphaCoverage(source: Bitmap): Double {
        val strongCoverage = alphaCoverage(source, NORMALIZE_ALPHA_BOUNDS_THRESHOLD)
        return if (strongCoverage > 0.0) strongCoverage else alphaCoverage(source)
    }

    private fun removeChromaKeyBackground(source: Bitmap, keyColor: Int): Bitmap {
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                val pixel = source.getPixel(x, y)
                val distance = colorDistance(pixel, keyColor)
                val originalAlpha = AndroidColor.alpha(pixel)
                val alpha = when {
                    distance <= CHROMA_TRANSPARENT_THRESHOLD -> 0
                    distance >= CHROMA_OPAQUE_THRESHOLD -> originalAlpha
                    else -> {
                        val factor = (distance - CHROMA_TRANSPARENT_THRESHOLD) /
                            (CHROMA_OPAQUE_THRESHOLD - CHROMA_TRANSPARENT_THRESHOLD)
                        (factor.coerceIn(0.0, 1.0) * originalAlpha).toInt()
                    }
                }
                out.setPixel(x, y, (alpha shl 24) or (pixel and 0x00ffffff))
            }
        }
        return out
    }

    private fun chooseChromaKey(source: Bitmap): Int {
        var best = CHROMA_KEY_CANDIDATES.first()
        var bestScore = -1.0
        for (candidate in CHROMA_KEY_CANDIDATES) {
            var minDistance = Double.MAX_VALUE
            for (y in 0 until source.height step maxOf(1, source.height / 64)) {
                for (x in 0 until source.width step maxOf(1, source.width / 64)) {
                    val pixel = source.getPixel(x, y)
                    if (AndroidColor.alpha(pixel) >= 64) {
                        minDistance = minOf(minDistance, colorDistance(candidate, pixel))
                    }
                }
            }
            if (minDistance > bestScore) {
                best = candidate
                bestScore = minDistance
            }
        }
        return best
    }

    private fun colorDistance(a: Int, b: Int): Double {
        val dr = AndroidColor.red(a) - AndroidColor.red(b)
        val dg = AndroidColor.green(a) - AndroidColor.green(b)
        val db = AndroidColor.blue(a) - AndroidColor.blue(b)
        return kotlin.math.sqrt((dr * dr + dg * dg + db * db).toDouble())
    }

    private fun normalDarkForeground(source: Bitmap, darkBackground: Bitmap): Bitmap {
        val preserved = nightForeground(
            source = source,
            background = darkBackground,
            preserveSubjectColors = true,
        )
        val boosted = supportNightForegroundWithColor(
            source = source,
            preserved = preserved,
            supportColor = NIGHT_APP_WHITE,
            maxBlend = NIGHT_DEFAULT_BOOST_MAX_BLEND,
        )
        return if (nightSubjectLightBackgroundEnabled) {
            supportNightForegroundWithColor(
                source = source,
                preserved = boosted,
                supportColor = sampleColor(darkBackground),
                maxBlend = NIGHT_FILL_BACKGROUND_MAX_BLEND,
            )
        } else {
            boosted
        }
    }

    private fun supportNightForegroundWithColor(
        source: Bitmap,
        preserved: Bitmap,
        supportColor: Int,
        maxBlend: Double,
    ): Bitmap {
        val width = preserved.width
        val height = preserved.height
        val sourcePixels = IntArray(width * height)
        val preservedPixels = IntArray(width * height)
        val outPixels = IntArray(sourcePixels.size)
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
        preserved.getPixels(preservedPixels, 0, width, 0, 0, width, height)

        for (i in sourcePixels.indices) {
            val sourcePixel = sourcePixels[i]
            val preservedPixel = preservedPixels[i]
            val alpha = AndroidColor.alpha(preservedPixel)
            outPixels[i] = if (alpha <= 0) {
                AndroidColor.TRANSPARENT
            } else {
                val sourceLuma = luma(sourcePixel)
                val sourceSaturation = saturation(sourcePixel)
                val darkness = ((NIGHT_SUPPORT_MAX_LUMA - sourceLuma).toDouble() /
                    (NIGHT_SUPPORT_MAX_LUMA - NIGHT_SUPPORT_MIN_LUMA).toDouble())
                    .coerceIn(0.0, 1.0)
                val neutrality = ((NIGHT_SUPPORT_MAX_SATURATION - sourceSaturation) /
                    NIGHT_SUPPORT_MAX_SATURATION)
                    .coerceIn(0.0, 1.0)
                val blend = (darkness * neutrality * maxBlend)
                    .takeUnless {
                        sourceSaturation >= NIGHT_SUPPORT_PRESERVE_SATURATION ||
                            sourceLuma >= NIGHT_SUPPORT_PRESERVE_LUMA
                    }
                    ?: 0.0
                AndroidColor.argb(
                    alpha,
                    blendChannel(AndroidColor.red(preservedPixel), AndroidColor.red(supportColor), blend),
                    blendChannel(AndroidColor.green(preservedPixel), AndroidColor.green(supportColor), blend),
                    blendChannel(AndroidColor.blue(preservedPixel), AndroidColor.blue(supportColor), blend),
                )
            }
        }

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return smoothAlphaEdges(
            featherVisibleEdges(repairTransparentEdgeColors(out), NIGHT_EDGE_FEATHER_BLEND),
            NIGHT_EDGE_SMOOTH_STRENGTH,
        )
    }

    private fun blendChannel(base: Int, target: Int, blend: Double): Int =
        (base * (1.0 - blend) + target * blend)
            .roundToInt()
            .coerceIn(0, 255)

    private fun nightForeground(
        source: Bitmap,
        background: Bitmap,
        preserveSubjectColors: Boolean = false,
    ): Bitmap {
        val width = source.width
        val height = source.height
        val sourcePixels = IntArray(width * height)
        val outPixels = IntArray(sourcePixels.size)
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height)

        var visibleWeight = 0.0
        var whiteWeight = 0.0
        var darkWeight = 0.0
        var colorWeight = 0.0
        for (i in sourcePixels.indices) {
            val pixel = sourcePixels[i]
            val alpha = AndroidColor.alpha(pixel)
            if (alpha <= 0) {
                outPixels[i] = AndroidColor.TRANSPARENT
                continue
            }
            outPixels[i] = AndroidColor.argb(
                alpha,
                liftDarkChannel(AndroidColor.red(pixel)),
                liftDarkChannel(AndroidColor.green(pixel)),
                liftDarkChannel(AndroidColor.blue(pixel)),
            )
            if (alpha <= NIGHT_VISIBLE_ALPHA_THRESHOLD) {
                continue
            }
            val weight = alpha / 255.0
            visibleWeight += weight
            val pixelLuma = luma(pixel)
            val pixelSaturation = saturation(pixel)
            if (pixelLuma >= NIGHT_WHITE_LUMA_THRESHOLD && pixelSaturation <= NIGHT_WHITE_MAX_SATURATION) {
                whiteWeight += weight
            }
            if (pixelLuma <= NIGHT_DARK_LUMA_THRESHOLD) {
                darkWeight += weight
            }
            if (
                pixelLuma > NIGHT_DARK_COLOR_IGNORE_LUMA_THRESHOLD &&
                pixelSaturation >= NIGHT_COLOR_SATURATION_THRESHOLD
            ) {
                colorWeight += weight
            }
        }

        if (visibleWeight > 0.0) {
            val bgColor = sampleColor(background)
            val bgLuma = luma(bgColor)
            val bgSaturation = saturation(bgColor)
            val nightWhiteTarget = if (bgLuma <= NIGHT_BACKGROUND_DARK_LUMA_THRESHOLD) {
                NIGHT_APP_WHITE
            } else {
                bgColor
            }
            val whiteRatio = whiteWeight / visibleWeight
            val darkRatio = darkWeight / visibleWeight
            val colorRatio = colorWeight / visibleWeight
            val flatLightSubject = isFlatLightNightSubject(
                sourcePixels = sourcePixels,
                whiteRatio = whiteRatio,
                darkRatio = darkRatio,
                colorRatio = colorRatio,
            )
            when {
                flatLightSubject &&
                    whiteRatio >= NIGHT_COLORED_BACKGROUND_WHITE_RATIO_THRESHOLD &&
                    bgSaturation >= NIGHT_COLORED_BACKGROUND_MIN_SATURATION &&
                    bgLuma >= NIGHT_BACKGROUND_COLORED_LUMA_THRESHOLD &&
                    darkRatio <= NIGHT_COLORED_BACKGROUND_DARK_RATIO_MAX &&
                    colorRatio <= NIGHT_COLORED_BACKGROUND_COLOR_RATIO_MAX -> {
                    recolorNightPixels(
                        sourcePixels = sourcePixels,
                        outPixels = outPixels,
                        target = bgColor,
                    ) { pixel ->
                        isNightWhiteSubjectPixel(pixel, includeSoftEdge = true)
                    }
                }
                flatLightSubject &&
                    whiteRatio >= NIGHT_WHITE_RATIO_THRESHOLD &&
                    colorRatio <= NIGHT_COLOR_RATIO_MAX -> {
                    recolorNightPixels(
                        sourcePixels = sourcePixels,
                        outPixels = outPixels,
                        target = nightWhiteTarget,
                    ) { pixel ->
                        isNightWhiteSubjectPixel(pixel, includeSoftEdge = true)
                    }
                }
                !preserveSubjectColors &&
                    darkRatio >= NIGHT_DARK_RATIO_THRESHOLD &&
                    whiteRatio <= NIGHT_DARK_MAX_WHITE_RATIO &&
                    colorRatio <= NIGHT_DARK_COLOR_RATIO_MAX &&
                    bgLuma >= NIGHT_BACKGROUND_COLORED_LUMA_THRESHOLD -> {
                    recolorNightPixels(
                        sourcePixels = sourcePixels,
                        outPixels = outPixels,
                        target = darkSubjectNightTarget(bgColor, bgLuma, bgSaturation),
                    ) { pixel ->
                        AndroidColor.alpha(pixel) > NIGHT_VISIBLE_ALPHA_THRESHOLD
                    }
                }
            }
            if (flatLightSubject) {
                recolorLocalNightEdgePixels(
                    sourcePixels = sourcePixels,
                    outPixels = outPixels,
                    width = width,
                    height = height,
                    target = nightWhiteTarget,
                )
            }
        }

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return smoothAlphaEdges(
            featherVisibleEdges(repairTransparentEdgeColors(out), NIGHT_EDGE_FEATHER_BLEND),
            NIGHT_EDGE_SMOOTH_STRENGTH,
        )
    }

    private fun recolorLocalNightEdgePixels(
        sourcePixels: IntArray,
        outPixels: IntArray,
        width: Int,
        height: Int,
        target: Int,
    ) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val source = sourcePixels[index]
                if (!isNightWhiteSubjectPixel(source)) {
                    continue
                }
                if (!hasNearbyColoredOrTransparentContrast(sourcePixels, width, height, x, y)) {
                    continue
                }
                val alpha = AndroidColor.alpha(source)
                outPixels[index] = AndroidColor.argb(
                    alpha,
                    AndroidColor.red(target),
                    AndroidColor.green(target),
                    AndroidColor.blue(target),
                )
            }
        }
    }

    private fun isFlatLightNightSubject(
        sourcePixels: IntArray,
        whiteRatio: Double,
        darkRatio: Double,
        colorRatio: Double,
    ): Boolean {
        if (
            darkRatio > NIGHT_FLAT_LIGHT_DARK_RATIO_MAX ||
            colorRatio > NIGHT_FLAT_LIGHT_COLOR_RATIO_MAX
        ) {
            return false
        }

        var strongWeight = 0.0
        var lightWeight = 0.0
        var veryLightWeight = 0.0
        var saturatedWeight = 0.0
        val lumas = mutableListOf<Int>()

        for (pixel in sourcePixels) {
            val alpha = AndroidColor.alpha(pixel)
            if (alpha <= NIGHT_FLAT_LIGHT_ALPHA_THRESHOLD) {
                continue
            }
            val weight = alpha / 255.0
            val pixelLuma = luma(pixel)
            val pixelSaturation = saturation(pixel)
            strongWeight += weight
            lumas += pixelLuma
            if (
                pixelLuma >= NIGHT_FLAT_LIGHT_LUMA_THRESHOLD &&
                pixelSaturation <= NIGHT_FLAT_LIGHT_MAX_SATURATION
            ) {
                lightWeight += weight
            }
            if (
                pixelLuma >= NIGHT_WHITE_LUMA_THRESHOLD &&
                pixelSaturation <= NIGHT_WHITE_MAX_SATURATION
            ) {
                veryLightWeight += weight
            }
            if (pixelSaturation >= NIGHT_FLAT_LIGHT_SATURATED_THRESHOLD) {
                saturatedWeight += weight
            }
        }

        if (strongWeight <= 0.0 || lumas.size < NIGHT_FLAT_LIGHT_MIN_PIXELS) {
            return false
        }

        val lightRatio = lightWeight / strongWeight
        val veryLightRatio = veryLightWeight / strongWeight
        val saturatedRatio = saturatedWeight / strongWeight
        val lumaRange = percentile(lumas, 0.90) - percentile(lumas, 0.10)
        val dominantWhiteMark =
            whiteRatio >= NIGHT_WHITE_RATIO_THRESHOLD &&
                lightRatio >= NIGHT_FLAT_LIGHT_RATIO_MIN &&
                veryLightRatio >= NIGHT_FLAT_VERY_LIGHT_RATIO_MIN
        val solidPaleMark =
            lightRatio >= NIGHT_FLAT_PALE_RATIO_MIN &&
                veryLightRatio >= NIGHT_FLAT_PALE_VERY_LIGHT_RATIO_MIN &&
                lumaRange <= NIGHT_FLAT_LIGHT_LUMA_RANGE_MAX

        return saturatedRatio <= NIGHT_FLAT_LIGHT_SATURATED_RATIO_MAX &&
            (dominantWhiteMark || solidPaleMark)
    }

    private fun hasNearbyColoredOrTransparentContrast(
        pixels: IntArray,
        width: Int,
        height: Int,
        x: Int,
        y: Int,
    ): Boolean {
        for (dy in -NIGHT_EDGE_CONTRAST_RADIUS..NIGHT_EDGE_CONTRAST_RADIUS) {
            for (dx in -NIGHT_EDGE_CONTRAST_RADIUS..NIGHT_EDGE_CONTRAST_RADIUS) {
                if (dx == 0 && dy == 0) {
                    continue
                }
                val nx = x + dx
                val ny = y + dy
                if (nx !in 0 until width || ny !in 0 until height) {
                    return true
                }
                val neighbor = pixels[ny * width + nx]
                val alpha = AndroidColor.alpha(neighbor)
                if (alpha <= NIGHT_VISIBLE_ALPHA_THRESHOLD) {
                    return true
                }
                if (saturation(neighbor) >= NIGHT_EDGE_COLORED_NEIGHBOR_SATURATION) {
                    return true
                }
                if (luma(neighbor) <= NIGHT_EDGE_DARK_NEIGHBOR_LUMA) {
                    return true
                }
            }
        }
        return false
    }

    private fun isNightWhiteSubjectPixel(pixel: Int, includeSoftEdge: Boolean = false): Boolean {
        if (AndroidColor.alpha(pixel) <= NIGHT_VISIBLE_ALPHA_THRESHOLD) {
            return false
        }
        val pixelLuma = luma(pixel)
        val pixelSaturation = saturation(pixel)
        return (
            pixelLuma >= NIGHT_WHITE_LUMA_THRESHOLD &&
                pixelSaturation <= NIGHT_WHITE_MAX_SATURATION
            ) || (
            pixelLuma >= NIGHT_EDGE_WHITE_LUMA_THRESHOLD &&
                pixelSaturation <= NIGHT_EDGE_WHITE_MAX_SATURATION
            ) || (
            includeSoftEdge &&
                pixelLuma >= NIGHT_SOFT_EDGE_WHITE_LUMA_THRESHOLD &&
                pixelSaturation <= NIGHT_SOFT_EDGE_WHITE_MAX_SATURATION
            )
    }

    private fun liftDarkChannel(value: Int): Int =
        (48 + value * 0.82f).toInt().coerceIn(0, 255)

    private fun recolorNightPixels(
        sourcePixels: IntArray,
        outPixels: IntArray,
        target: Int,
        shouldRecolor: (Int) -> Boolean,
    ) {
        for (i in sourcePixels.indices) {
            val source = sourcePixels[i]
            val alpha = AndroidColor.alpha(source)
            if (alpha <= NIGHT_VISIBLE_ALPHA_THRESHOLD || !shouldRecolor(source)) {
                continue
            }
            outPixels[i] = AndroidColor.argb(
                alpha,
                AndroidColor.red(target),
                AndroidColor.green(target),
                AndroidColor.blue(target),
            )
        }
    }

    private fun appWhiteFromBackground(bgColor: Int): Int {
        val bgLuma = luma(bgColor)
        val bgSaturation = saturation(bgColor)
        if (
            bgLuma <= NIGHT_BACKGROUND_DARK_LUMA_THRESHOLD ||
            (bgLuma >= NIGHT_DIRECT_WHITE_LUMA_THRESHOLD &&
                bgSaturation <= NIGHT_DIRECT_WHITE_MAX_SATURATION)
        ) {
            return NIGHT_APP_WHITE
        }
        return AndroidColor.rgb(
            blendNightWhiteChannel(AndroidColor.red(bgColor), AndroidColor.red(NIGHT_APP_WHITE)),
            blendNightWhiteChannel(AndroidColor.green(bgColor), AndroidColor.green(NIGHT_APP_WHITE)),
            blendNightWhiteChannel(AndroidColor.blue(bgColor), AndroidColor.blue(NIGHT_APP_WHITE)),
        )
    }

    private fun darkSubjectNightTarget(bgColor: Int, bgLuma: Int, bgSaturation: Double): Int {
        if (bgLuma >= NIGHT_BACKGROUND_LIGHT_LUMA_THRESHOLD &&
            bgSaturation <= NIGHT_BACKGROUND_LIGHT_MAX_SATURATION
        ) {
            return appWhiteFromBackground(bgColor)
        }
        return AndroidColor.rgb(
            AndroidColor.red(bgColor),
            AndroidColor.green(bgColor),
            AndroidColor.blue(bgColor),
        )
    }

    private fun blendNightWhiteChannel(background: Int, appWhite: Int): Int =
        (background * NIGHT_BACKGROUND_WHITE_BLEND + appWhite * (1.0 - NIGHT_BACKGROUND_WHITE_BLEND))
            .toInt()
            .coerceIn(0, 255)

    private fun monochromeAlpha(source: Bitmap, invertLuma: Boolean): Bitmap {
        val width = source.width
        val height = source.height
        val sourcePixels = IntArray(width * height)
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
        val repairedPixels = repairTransparentEdgeColors(sourcePixels, width, height)
        val lumas = sourcePixels
            .indices
            .filter { AndroidColor.alpha(sourcePixels[it]) > MONO_EDGE_ALPHA_IGNORE_THRESHOLD }
            .map { luma(repairedPixels[it]) }
            .toMutableList()
        val low = percentile(lumas, 0.02)
        val high = percentile(lumas, 0.98)
        val hasRange = high - low >= MONO_TONAL_RANGE_THRESHOLD
        val outPixels = IntArray(sourcePixels.size)

        for (i in sourcePixels.indices) {
            val pixel = sourcePixels[i]
            val alpha = AndroidColor.alpha(pixel)
            if (alpha <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
                outPixels[i] = AndroidColor.TRANSPARENT
                continue
            }
            val maskAlpha = if (hasRange) {
                val normalized = ((luma(repairedPixels[i]) - low).toDouble() / (high - low).toDouble())
                    .coerceIn(0.0, 1.0)
                val tonal = if (invertLuma) 1.0 - normalized else normalized
                MONO_ALPHA_MIN + tonal.pow(MONO_ALPHA_GAMMA) * (MONO_ALPHA_MAX - MONO_ALPHA_MIN)
            } else {
                MONO_ALPHA_MAX.toDouble()
            }
            val edgeCoverage = ((alpha - MONO_EDGE_ALPHA_DROP_THRESHOLD).toDouble() /
                (255 - MONO_EDGE_ALPHA_DROP_THRESHOLD).toDouble())
                .coerceIn(0.0, 1.0)
            val outAlpha = (edgeCoverage * maskAlpha).toInt().coerceIn(0, 255)
            outPixels[i] = (outAlpha shl 24) or 0x00ffffff
        }

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun simpleMonochromeAlphaFromDefaultSubject(source: Bitmap, invertLuma: Boolean): Bitmap {
        val width = source.width
        val height = source.height
        val sourcePixels = IntArray(width * height)
        val outPixels = IntArray(sourcePixels.size)
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
        for (i in sourcePixels.indices) {
            val pixel = sourcePixels[i]
            val sourceAlpha = AndroidColor.alpha(pixel)
            if (sourceAlpha <= 0) {
                outPixels[i] = AndroidColor.TRANSPARENT
                continue
            }
            val gray = luma(pixel)
            val tonal = if (invertLuma) 255 - gray else gray
            val outAlpha = (sourceAlpha * tonal / 255.0)
                .roundToInt()
                .coerceIn(0, 255)
            outPixels[i] = if (outAlpha <= 0) {
                AndroidColor.TRANSPARENT
            } else {
                (outAlpha shl 24) or 0x00ffffff
            }
        }
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun hasForegroundTonalRange(source: Bitmap): Boolean {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val repaired = repairTransparentEdgeColors(pixels, width, height)
        val lumas = mutableListOf<Int>()
        for (i in pixels.indices) {
            if (AndroidColor.alpha(pixels[i]) > MONO_EDGE_ALPHA_IGNORE_THRESHOLD) {
                lumas += luma(repaired[i])
            }
        }
        if (lumas.size < MONO_TONAL_MIN_VISIBLE_PIXELS) {
            return false
        }
        return percentile(lumas, 0.98) - percentile(lumas, 0.02) >= MONO_TONAL_RANGE_THRESHOLD
    }

    private fun isUsableNativeMonochrome(source: Bitmap, foreground: Bitmap): Boolean {
        val nativeCoverage = meaningfulAlphaCoverage(source)
        if (nativeCoverage <= 0.0) {
            return false
        }
        if (nativeCoverage >= MONO_NATIVE_MAX_TILE_COVERAGE) {
            return false
        }
        val foregroundCoverage = meaningfulAlphaCoverage(foreground)
        if (foregroundCoverage > 0.0 && nativeCoverage > foregroundCoverage + MONO_NATIVE_MAX_COVERAGE_EXTRA) {
            return false
        }
        return true
    }

    private fun repairTransparentEdgeColors(sourcePixels: IntArray, width: Int, height: Int): IntArray {
        val repaired = sourcePixels.copyOf()
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val pixel = sourcePixels[index]
                val alpha = AndroidColor.alpha(pixel)
                if (alpha >= MONO_EDGE_ALPHA_REPAIR_THRESHOLD || alpha <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
                    continue
                }
                val neighbor = nearestOpaqueNeighborColor(sourcePixels, width, height, x, y)
                if (neighbor != null) {
                    repaired[index] = AndroidColor.argb(
                        alpha,
                        AndroidColor.red(neighbor),
                        AndroidColor.green(neighbor),
                        AndroidColor.blue(neighbor),
                    )
                }
            }
        }
        return repaired
    }

    private fun repairTransparentEdgeColors(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val repaired = repairTransparentEdgeColors(pixels, width, height)
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(repaired, 0, width, 0, 0, width, height)
        return out
    }

    private fun nearestOpaqueNeighborColor(sourcePixels: IntArray, width: Int, height: Int, x: Int, y: Int): Int? {
        for (radius in 1..MONO_EDGE_REPAIR_RADIUS) {
            var red = 0
            var green = 0
            var blue = 0
            var count = 0
            for (dy in -radius..radius) {
                for (dx in -radius..radius) {
                    if (maxOf(abs(dx), abs(dy)) != radius) {
                        continue
                    }
                    val nx = x + dx
                    val ny = y + dy
                    if (nx !in 0 until width || ny !in 0 until height) {
                        continue
                    }
                    val candidate = sourcePixels[ny * width + nx]
                    if (AndroidColor.alpha(candidate) < MONO_EDGE_ALPHA_REPAIR_THRESHOLD) {
                        continue
                    }
                    red += AndroidColor.red(candidate)
                    green += AndroidColor.green(candidate)
                    blue += AndroidColor.blue(candidate)
                    count += 1
                }
            }
            if (count > 0) {
                return AndroidColor.rgb(red / count, green / count, blue / count)
            }
        }
        return null
    }

    private fun featherVisibleEdges(source: Bitmap, blend: Double): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val pixel = pixels[index]
                val alpha = AndroidColor.alpha(pixel)
                if (alpha <= 0) {
                    outPixels[index] = AndroidColor.TRANSPARENT
                    continue
                }
                var transparentNeighbors = 0
                var totalNeighbors = 0
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        if (dx == 0 && dy == 0) {
                            continue
                        }
                        val nx = x + dx
                        val ny = y + dy
                        totalNeighbors += 1
                        if (nx !in 0 until width || ny !in 0 until height) {
                            transparentNeighbors += 1
                            continue
                        }
                        if (AndroidColor.alpha(pixels[ny * width + nx]) <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
                            transparentNeighbors += 1
                        }
                    }
                }
                if (transparentNeighbors == 0 || totalNeighbors == 0) {
                    outPixels[index] = pixel
                    continue
                }
                val edgeRatio = transparentNeighbors.toDouble() / totalNeighbors.toDouble()
                val alphaScale = (1.0 - edgeRatio * blend).coerceIn(0.0, 1.0)
                val outAlpha = (alpha * alphaScale).toInt().coerceIn(0, 255)
                outPixels[index] = if (outAlpha <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
                    AndroidColor.TRANSPARENT
                } else {
                    AndroidColor.argb(
                        outAlpha,
                        AndroidColor.red(pixel),
                        AndroidColor.green(pixel),
                        AndroidColor.blue(pixel),
                    )
                }
            }
        }
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun smoothAlphaEdges(
        source: Bitmap,
        strength: Double,
        growTransparentEdges: Boolean = false,
        radius: Int = 1,
        growStrength: Double = MONO_EDGE_GROW_STRENGTH,
    ): Bitmap {
        if (strength <= 0.0) {
            return source
        }
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        val outPixels = pixels.copyOf()
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val pixel = pixels[index]
                val alpha = AndroidColor.alpha(pixel)
                if (alpha <= 0 && !growTransparentEdges) {
                    outPixels[index] = AndroidColor.TRANSPARENT
                    continue
                }
                var alphaSum = 0
                var count = 0
                var hasTransparentNeighbor = false
                var redSum = 0
                var greenSum = 0
                var blueSum = 0
                var visibleColorCount = 0
                val edgeRadius = radius.coerceAtLeast(1)
                for (dy in -edgeRadius..edgeRadius) {
                    for (dx in -edgeRadius..edgeRadius) {
                        val nx = x + dx
                        val ny = y + dy
                        count += 1
                        if (nx !in 0 until width || ny !in 0 until height) {
                            hasTransparentNeighbor = true
                            continue
                        }
                        val neighborAlpha = AndroidColor.alpha(pixels[ny * width + nx])
                        alphaSum += neighborAlpha
                        if (neighborAlpha <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
                            hasTransparentNeighbor = true
                        } else {
                            val neighbor = pixels[ny * width + nx]
                            redSum += AndroidColor.red(neighbor)
                            greenSum += AndroidColor.green(neighbor)
                            blueSum += AndroidColor.blue(neighbor)
                            visibleColorCount += 1
                        }
                    }
                }
                if (!hasTransparentNeighbor || count <= 0) {
                    outPixels[index] = pixel
                    continue
                }
                val blurredAlpha = alphaSum / count
                val outAlpha = if (alpha <= 0 && growTransparentEdges) {
                    (blurredAlpha * growStrength).toInt()
                } else {
                    (alpha * (1.0 - strength) + blurredAlpha * strength).toInt()
                }
                    .coerceIn(0, 255)
                outPixels[index] = if (outAlpha <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
                    AndroidColor.TRANSPARENT
                } else if (alpha <= 0 && visibleColorCount > 0) {
                    AndroidColor.argb(
                        outAlpha,
                        redSum / visibleColorCount,
                        greenSum / visibleColorCount,
                        blueSum / visibleColorCount,
                    )
                } else {
                    AndroidColor.argb(
                        outAlpha,
                        AndroidColor.red(pixel),
                        AndroidColor.green(pixel),
                        AndroidColor.blue(pixel),
                    )
                }
            }
        }

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun monochromeAlphaFromMask(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val sourcePixels = IntArray(width * height)
        val outPixels = IntArray(sourcePixels.size)
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height)

        for (i in sourcePixels.indices) {
            val pixel = sourcePixels[i]
            val sourceAlpha = AndroidColor.alpha(pixel)
            if (sourceAlpha <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
                outPixels[i] = AndroidColor.TRANSPARENT
                continue
            }
            val outAlpha = sourceAlpha.coerceIn(0, MONO_ALPHA_MAX)
            outPixels[i] = (outAlpha shl 24) or 0x00ffffff
        }

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun cleanNativeMonochrome(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val sourcePixels = IntArray(width * height)
        val outPixels = IntArray(sourcePixels.size)
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
        for (i in sourcePixels.indices) {
            val alpha = AndroidColor.alpha(sourcePixels[i])
            outPixels[i] = if (alpha <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
                AndroidColor.TRANSPARENT
            } else {
                (alpha.coerceIn(0, MONO_ALPHA_MAX) shl 24) or 0x00ffffff
            }
        }
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun sharpenMonochromeAlpha(source: Bitmap, nativeSource: Boolean = false): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val lowCut = if (nativeSource) MONO_NATIVE_EDGE_LOW_CUT else MONO_EDGE_SHARPEN_LOW_CUT
        val highCut = if (nativeSource) MONO_NATIVE_EDGE_HIGH_CUT else MONO_EDGE_SHARPEN_HIGH_CUT
        for (i in pixels.indices) {
            val alpha = AndroidColor.alpha(pixels[i])
            val outAlpha = when {
                alpha <= lowCut -> 0
                alpha >= highCut -> alpha.coerceAtMost(MONO_ALPHA_MAX)
                else -> {
                    val t = (alpha - lowCut).toDouble() / (highCut - lowCut).toDouble()
                    val eased = t * t * (3.0 - 2.0 * t)
                    (eased * MONO_ALPHA_MAX).toInt().coerceIn(0, MONO_ALPHA_MAX)
                }
            }
            outPixels[i] = if (outAlpha <= 0) {
                AndroidColor.TRANSPARENT
            } else {
                (outAlpha shl 24) or 0x00ffffff
            }
        }
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun trimMonochromeEdge(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val visible = BooleanArray(pixels.size)
        for (i in pixels.indices) {
            visible[i] = AndroidColor.alpha(pixels[i]) > MONO_EDGE_ALPHA_DROP_THRESHOLD
        }

        val outside = BooleanArray(pixels.size)
        val queue = ArrayDeque<Int>()
        fun markOutside(index: Int) {
            if (!visible[index] && !outside[index]) {
                outside[index] = true
                queue.add(index)
            }
        }

        for (x in 0 until width) {
            markOutside(x)
            markOutside((height - 1) * width + x)
        }
        for (y in 0 until height) {
            markOutside(y * width)
            markOutside(y * width + width - 1)
        }

        while (!queue.isEmpty()) {
            val index = queue.removeFirst()
            val x = index % width
            val y = index / width
            for (dy in -1..1) {
                for (dx in -1..1) {
                    if (dx == 0 && dy == 0) {
                        continue
                    }
                    val nx = x + dx
                    val ny = y + dy
                    if (nx !in 0 until width || ny !in 0 until height) {
                        continue
                    }
                    markOutside(ny * width + nx)
                }
            }
        }

        val outPixels = pixels.copyOf()
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                if (!visible[index]) {
                    outPixels[index] = AndroidColor.TRANSPARENT
                    continue
                }
                var touchesOutside = false
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        if (dx == 0 && dy == 0) {
                            continue
                        }
                        val nx = x + dx
                        val ny = y + dy
                        if (nx !in 0 until width || ny !in 0 until height || outside[ny * width + nx]) {
                            touchesOutside = true
                            break
                        }
                    }
                    if (touchesOutside) {
                        break
                    }
                }
                if (touchesOutside) {
                    val alpha = AndroidColor.alpha(outPixels[index])
                    val softenedAlpha = (alpha * MONO_EDGE_TRIM_FEATHER_SCALE)
                        .toInt()
                        .coerceIn(0, 255)
                    outPixels[index] = if (softenedAlpha <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
                        AndroidColor.TRANSPARENT
                    } else {
                        AndroidColor.argb(softenedAlpha, 255, 255, 255)
                    }
                }
            }
        }

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun polishForegroundEdges(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        if (width <= 2 || height <= 2) {
            return source
        }
        val pixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val repairedPixels = repairTransparentEdgeColors(pixels, width, height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val pixel = repairedPixels[index]
                val alpha = AndroidColor.alpha(pixel)
                if (alpha <= 0) {
                    outPixels[index] = AndroidColor.TRANSPARENT
                    continue
                }
                val edge = hasTransparentNeighbor(pixels, width, height, x, y, FOREGROUND_EDGE_POLISH_RADIUS)
                if (!edge) {
                    outPixels[index] = pixel
                    continue
                }
                val coverage = visibleNeighborCoverage(
                    pixels = pixels,
                    width = width,
                    height = height,
                    x = x,
                    y = y,
                    radius = FOREGROUND_EDGE_POLISH_RADIUS,
                    threshold = LOCAL_ALPHA_VISIBLE_THRESHOLD,
                )
                val targetAlpha = (coverage * 255.0).toInt().coerceIn(0, 255)
                val strength = foregroundEdgePolishStrength()
                val smoothedAlpha = (alpha * (1.0 - strength) + targetAlpha * strength)
                    .toInt()
                    .coerceIn(0, 255)
                outPixels[index] = if (smoothedAlpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                    AndroidColor.TRANSPARENT
                } else {
                    AndroidColor.argb(
                        smoothedAlpha,
                        AndroidColor.red(pixel),
                        AndroidColor.green(pixel),
                        AndroidColor.blue(pixel),
                    )
                }
            }
        }

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun polishMonochromeEdges(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        if (width <= 2 || height <= 2) {
            return source
        }
        val pixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val alpha = AndroidColor.alpha(pixels[index])
                if (alpha <= 0) {
                    outPixels[index] = AndroidColor.TRANSPARENT
                    continue
                }
                val edge = hasTransparentNeighbor(pixels, width, height, x, y, MONO_EDGE_POLISH_RADIUS)
                if (!edge) {
                    outPixels[index] = (alpha.coerceAtMost(MONO_ALPHA_MAX) shl 24) or 0x00ffffff
                    continue
                }
                val coverage = visibleNeighborCoverage(
                    pixels = pixels,
                    width = width,
                    height = height,
                    x = x,
                    y = y,
                    radius = MONO_EDGE_POLISH_RADIUS,
                    threshold = MONO_EDGE_ALPHA_DROP_THRESHOLD,
                )
                val targetAlpha = (coverage * MONO_ALPHA_MAX).toInt().coerceIn(0, MONO_ALPHA_MAX)
                val strength = monochromeEdgePolishStrength()
                val smoothedAlpha = (alpha * (1.0 - strength) + targetAlpha * strength)
                    .toInt()
                    .coerceIn(0, MONO_ALPHA_MAX)
                outPixels[index] = if (smoothedAlpha <= MONO_EDGE_ALPHA_DROP_THRESHOLD) {
                    AndroidColor.TRANSPARENT
                } else {
                    (smoothedAlpha shl 24) or 0x00ffffff
                }
            }
        }

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun hasTransparentNeighbor(
        pixels: IntArray,
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        radius: Int,
    ): Boolean {
        for (dy in -radius..radius) {
            for (dx in -radius..radius) {
                if (dx == 0 && dy == 0) {
                    continue
                }
                val nx = x + dx
                val ny = y + dy
                if (nx !in 0 until width || ny !in 0 until height) {
                    return true
                }
                if (AndroidColor.alpha(pixels[ny * width + nx]) <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                    return true
                }
            }
        }
        return false
    }

    private fun visibleNeighborCoverage(
        pixels: IntArray,
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        radius: Int,
        threshold: Int,
    ): Double {
        var total = 0
        var visible = 0.0
        for (dy in -radius..radius) {
            for (dx in -radius..radius) {
                val nx = x + dx
                val ny = y + dy
                total += 1
                if (nx !in 0 until width || ny !in 0 until height) {
                    continue
                }
                val alpha = AndroidColor.alpha(pixels[ny * width + nx])
                if (alpha > threshold) {
                    visible += alpha.toDouble() / 255.0
                }
            }
        }
        return if (total == 0) 0.0 else (visible / total.toDouble()).coerceIn(0.0, 1.0)
    }

    private fun foregroundEdgePolishStrength(): Double =
        EDGE_POLISH_FOREGROUND_MIN_STRENGTH +
            ratioPercent(edgePolishPercent) * (EDGE_POLISH_FOREGROUND_MAX_STRENGTH - EDGE_POLISH_FOREGROUND_MIN_STRENGTH)

    private fun monochromeEdgePolishStrength(): Double =
        EDGE_POLISH_MONO_MIN_STRENGTH +
            ratioPercent(edgePolishPercent) * (EDGE_POLISH_MONO_MAX_STRENGTH - EDGE_POLISH_MONO_MIN_STRENGTH)

    private fun percentile(values: MutableList<Int>, ratio: Double): Int {
        if (values.isEmpty()) {
            return 0
        }
        values.sort()
        val index = ((values.size - 1) * ratio)
            .toInt()
            .coerceIn(0, values.size - 1)
        return values[index]
    }

    private fun luma(pixel: Int): Int =
        (AndroidColor.red(pixel) * 0.299 +
            AndroidColor.green(pixel) * 0.587 +
            AndroidColor.blue(pixel) * 0.114).toInt()

    private fun luma(red: Double, green: Double, blue: Double): Double =
        red * 0.299 + green * 0.587 + blue * 0.114

    private fun sampleColor(bitmap: Bitmap): Int {
        val center = bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
        if (
            AndroidColor.alpha(center) > 32 &&
            AndroidColor.red(center) + AndroidColor.green(center) + AndroidColor.blue(center) >= 120
        ) {
            return AndroidColor.rgb(
                AndroidColor.red(center),
                AndroidColor.green(center),
                AndroidColor.blue(center),
            )
        }

        var red = 0L
        var green = 0L
        var blue = 0L
        var count = 0L
        for (y in 0 until bitmap.height step 8) {
            for (x in 0 until bitmap.width step 8) {
                val pixel = bitmap.getPixel(x, y)
                if (AndroidColor.alpha(pixel) >= 128) {
                    red += AndroidColor.red(pixel)
                    green += AndroidColor.green(pixel)
                    blue += AndroidColor.blue(pixel)
                    count++
                }
            }
        }
        if (count == 0L) {
            return AndroidColor.rgb(216, 224, 253)
        }
        return AndroidColor.rgb((red / count).toInt(), (green / count).toInt(), (blue / count).toInt())
    }

    private fun adjustColor(source: Bitmap, saturation: Float, brightness: Float): Bitmap {
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val saturationMatrix = ColorMatrix()
        saturationMatrix.setSaturation(saturation)
        val brightnessMatrix = ColorMatrix(
            floatArrayOf(
                brightness, 0f, 0f, 0f, 0f,
                0f, brightness, 0f, 0f, 0f,
                0f, 0f, brightness, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            ),
        )
        saturationMatrix.postConcat(brightnessMatrix)
        paint.colorFilter = ColorMatrixColorFilter(saturationMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return out
    }

    private fun savePng(bitmap: Bitmap, file: File) {
        val parent = file.parentFile
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            error("无法创建目录: $parent")
        }
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    private fun ensureCleanDir(dir: File) {
        if (!dir.exists()) {
            dir.mkdirs()
            return
        }
        dir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".png") }
            ?.forEach { it.delete() }
    }

    private fun ensureFreshDir(dir: File) {
        if (dir.exists()) {
            dir.deleteRecursively()
        }
        if (!dir.mkdirs()) {
            error("无法创建目录: $dir")
        }
    }

    private fun bitmapStatsJson(bitmap: Bitmap): JSONObject {
        val visibleBounds = alphaBounds(bitmap, LOCAL_ALPHA_VISIBLE_THRESHOLD)
        val meaningfulBounds = meaningfulAlphaBounds(bitmap)
        val centroid = meaningfulAlphaCentroid(bitmap)
        return JSONObject()
            .put("width", bitmap.width)
            .put("height", bitmap.height)
            .put("alpha_coverage", alphaCoverage(bitmap))
            .put("meaningful_alpha_coverage", meaningfulAlphaCoverage(bitmap))
            .put("visible_bounds", boundsJson(visibleBounds))
            .put("meaningful_bounds", boundsJson(meaningfulBounds))
            .put(
                "centroid",
                if (centroid == null) {
                    JSONObject.NULL
                } else {
                    JSONObject()
                        .put("x", centroid.first)
                        .put("y", centroid.second)
                },
            )
            .put("touches_edge", meaningfulBounds?.let { hasAutoCropRisk(it, bitmap.width, bitmap.height) } ?: false)
    }

    private fun boundsJson(bounds: Bounds?): Any =
        if (bounds == null) {
            JSONObject.NULL
        } else {
            JSONObject()
                .put("left", bounds.left)
                .put("top", bounds.top)
                .put("right", bounds.right)
                .put("bottom", bounds.bottom)
                .put("width", bounds.width())
                .put("height", bounds.height())
        }

    private fun exportToTree(packageDir: File) {
        val treeUri = outputTreeUri ?: return
        val rootDoc = DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri),
        )
        var packageDoc = findChild(treeUri, rootDoc, packageDir.name)
        if (packageDoc == null) {
            packageDoc = DocumentsContract.createDocument(
                contentResolver,
                rootDoc,
                DocumentsContract.Document.MIME_TYPE_DIR,
                packageDir.name,
            )
        }
        if (packageDoc == null) {
            error("无法创建输出目录")
        }

        val files = packageDir.listFiles { _, name -> name.endsWith(".png") } ?: return
        for (file in files) {
            findChild(treeUri, packageDoc, file.name)?.let {
                DocumentsContract.deleteDocument(contentResolver, it)
            }
            val doc = DocumentsContract.createDocument(contentResolver, packageDoc, "image/png", file.name)
                ?: error("无法创建文件: ${file.name}")
            FileInputStream(file).use { input ->
                contentResolver.openOutputStream(doc, "w").useRequired { output ->
                    copyStream(input, output)
                }
            }
        }
    }

    private fun findChild(treeUri: Uri, parentDoc: Uri, displayName: String): Uri? {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            DocumentsContract.getDocumentId(parentDoc),
        )
        return try {
            var found: Uri? = null
            contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                ),
                null,
                null,
                null,
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val childName = cursor.getString(1)
                    if (displayName == childName) {
                        val documentId = cursor.getString(0)
                        found = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                        break
                    }
                }
            }
            found
        } catch (_: Exception) {
            null
        }
    }

    private fun installWithRoot(packageDir: File, packageName: String, mode: RootWriteMode) {
        val target = "$ROOT_UXICONS_DIR/$packageName"
        val source = packageDir.absolutePath
        val copyCommand = when (mode) {
            RootWriteMode.All -> """
                find ${shQuote(source)} -maxdepth 1 -type f -name '*.png' -exec cp -f {} ${shQuote(target)}/ \;
            """.trimIndent()
            RootWriteMode.DefaultOnly -> """
                find ${shQuote(target)} -maxdepth 1 -type f -name 'monochrome*.png' -delete
                find ${shQuote(source)} -maxdepth 1 -type f -name '*.png' ! -name 'monochrome*.png' -exec cp -f {} ${shQuote(target)}/ \;
            """.trimIndent()
            RootWriteMode.MonochromeOnly -> """
                find ${shQuote(source)} -maxdepth 1 -type f -name 'monochrome*.png' -exec cp -f {} ${shQuote(target)}/ \;
            """.trimIndent()
        }
        val command = """
            set -e
            mkdir -p ${shQuote(target)}
            $copyCommand
            find ${shQuote(target)} -maxdepth 1 -type f -name '*.png' -exec chmod 0644 {} +
            restorecon -RF ${shQuote(target)} 2>/dev/null || true
        """.trimIndent()
        val process = ProcessBuilder("su", "-c", command)
            .redirectErrorStream(true)
            .start()
        val code = process.waitFor()
        if (code != 0) {
            error("su 退出码: $code")
        }
    }

    private fun shQuote(value: String): String = "'" + value.replace("'", "'\\''") + "'"

    private fun showKeyboardFor(editText: EditText) {
        editText.post {
            editText.requestFocus()
            editText.context
                .getSystemService(InputMethodManager::class.java)
                ?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun status(message: String) {
        runOnUiThread { statusText = message }
    }

    private fun startDebugHttpServerIfNeeded() {
        if (!isDebugBuild()) {
            return
        }
        if (debugHttpServer != null) {
            return
        }
        debugToken()
        debugHttpServer = DebugHttpServer(DEBUG_HTTP_PORT).also { it.start() }
    }

    private fun isDebugBuild(): Boolean =
        (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private fun debugToken(): String {
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

    private fun isDebugTokenValid(token: String?): Boolean =
        token != null && token == debugToken()

    private fun runOnMainSync(action: () -> Unit) {
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

    private fun currentDebugParamsJson(): JSONObject =
        JSONObject()
            .put("port", DEBUG_HTTP_PORT)
            .put("busy", isBusy)
            .put("status", statusText)
            .put("foreground_subject_percent", foregroundSubjectPercent)
            .put("foreground_shadow_level", foregroundShadowLevel)
            .put("monochrome_theme_scale", (monochromeThemeScale * 100).roundToInt())
            .put("gpt_mode", gptImageMode.value)
            .put("gpt_prompt_preset", gptPromptPreset.value)
            .put("gpt_custom_prompt", gptCustomPrompt)
            .put("gpt_base_url", gptBaseUrl)
            .put("gpt_api_key_set", gptApiKey.isNotBlank())
            .put("background_separation_percent", backgroundSeparationPercent)
            .put("plate_removal_percent", plateRemovalPercent)
            .put("shadow_removal_percent", shadowRemovalPercent)
            .put("edge_polish_percent", edgePolishPercent)
            .put("rmbg_alpha_strength_percent", rmbgAlphaStrengthPercent)
            .put("rmbg_edge_feather_percent", rmbgEdgeFeatherPercent)
            .put("rmbg_edge_adjust_percent", rmbgEdgeAdjustPercent)
            .put("rmbg_weak_alpha_keep_percent", rmbgWeakAlphaKeepPercent)
            .put("liquid_glass_enabled", liquidGlassEnabled)
            .put("liquid_glass_radius", liquidGlassRadius)
            .put("liquid_glass_outer_width", liquidGlassOuterWidth)
            .put("liquid_glass_top_alpha", liquidGlassTopAlpha)
            .put("liquid_glass_bottom_alpha", liquidGlassBottomAlpha)
            .put("liquid_glass_background_mist_alpha", liquidGlassBackgroundMistAlpha)
            .put("liquid_glass_bottom_dark_alpha", liquidGlassBottomDarkAlpha)
            .put("liquid_glass_subject_scale_percent", liquidGlassSubjectScalePercent)
            .put("liquid_glass_subject_outline_width", liquidGlassSubjectOutlineWidth)
            .put("liquid_glass_subject_inner_outline_width", liquidGlassSubjectInnerOutlineWidth)
            .put("liquid_glass_subject_shadow_alpha", liquidGlassSubjectShadowAlpha)
            .put("liquid_glass_subject_opacity_percent", liquidGlassSubjectOpacityPercent)
            .put("liquid_glass_param_labels", liquidGlassParamLabelsJson())
            .put("rmbg_model_installed", findRmbgComponent() != null)
            .put("rmbg_component_installed", findRmbgComponent() != null)
            .put("rmbg_component_abi", findRmbgComponent()?.abi ?: "")
            .put("rmbg_model_name", RMBG_MODEL_NAME)
            .put("rmbg_status", rmbgInferenceStatusSummary())
            .put("rmbg_actual_backend", lastRmbgInferenceReport?.actualBackend?.value ?: "")
            .put("rmbg_inference_elapsed_ms", lastRmbgInferenceReport?.elapsedMs ?: JSONObject.NULL)
            .put("rmbg_last_error", lastRmbgCandidateError ?: "")
            .put("adaptive_foreground_mode", adaptiveForegroundMode.value)
            .put("adaptive_foreground_modes", JSONArray().also { array ->
                AdaptiveForegroundMode.entries.forEach { mode ->
                    array.put(JSONObject().put("value", mode.value).put("label", mode.label))
                }
            })
            .put("adaptive_direct_max_coverage_percent", adaptiveDirectMaxCoveragePercent)
            .put("adaptive_direct_max_coverage_increase_percent", adaptiveDirectMaxCoverageIncreasePercent)
            .put("adaptive_mask_edge_coverage_percent", adaptiveMaskEdgeCoveragePercent)
            .put("adaptive_mask_min_coverage_percent", adaptiveMaskMinCoveragePercent)
            .put("adaptive_center_epsilon_percent", adaptiveCenterEpsilonPercent)
            .put("original_foreground_cleanup_mode", originalForegroundCleanupMode.value)
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

    private fun intRangeJson(min: Int, max: Int): JSONObject =
        JSONObject().put("min", min).put("max", max)

    private fun liquidGlassParamLabelsJson(): JSONObject =
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

    private fun currentDebugParamsOnMain(): JSONObject {
        var snapshot: JSONObject? = null
        runOnMainSync {
            snapshot = currentDebugParamsJson()
        }
        return snapshot ?: error("debug params unavailable")
    }

    private fun applyDebugParams(params: Map<String, String>): JSONObject {
        var snapshot: JSONObject? = null
        runOnMainSync {
            fun intParam(vararg keys: String): Int? {
                keys.forEach { key ->
                    params[key]?.toIntOrNull()?.let { return it }
                }
                return null
            }

            params["foreground_subject_percent"]?.toIntOrNull()?.let {
                foregroundSubjectPercent = it.coerceIn(MIN_FOREGROUND_SUBJECT_PERCENT, MAX_FOREGROUND_SUBJECT_PERCENT)
            }
            params["foreground_shadow_level"]?.toIntOrNull()?.let {
                foregroundShadowLevel = it.coerceIn(MIN_FOREGROUND_SHADOW_LEVEL, MAX_FOREGROUND_SHADOW_LEVEL)
                draftForegroundShadowLevelText = foregroundShadowLevel.toString()
            }
            params["monochrome_theme_scale"]?.toFloatOrNull()?.let {
                val percent = if (it <= 2f) (it * 100f).roundToInt() else it.roundToInt()
                monochromeThemeScale = (percent.coerceIn(
                    MIN_MONOCHROME_THEME_SCALE_PERCENT,
                    MAX_MONOCHROME_THEME_SCALE_PERCENT,
                ).toFloat() / 100f).coerceIn(MIN_MONOCHROME_THEME_SCALE, MAX_MONOCHROME_THEME_SCALE)
                draftMonochromeThemeScaleText = (monochromeThemeScale * 100).roundToInt().toString()
            }
            params["background_separation_percent"]?.toIntOrNull()?.let {
                backgroundSeparationPercent = it.coerceIn(MIN_BACKGROUND_SEPARATION_PERCENT, MAX_BACKGROUND_SEPARATION_PERCENT)
                draftBackgroundSeparationText = backgroundSeparationPercent.toString()
            }
            params["plate_removal_percent"]?.toIntOrNull()?.let {
                plateRemovalPercent = it.coerceIn(MIN_PLATE_REMOVAL_PERCENT, MAX_PLATE_REMOVAL_PERCENT)
                draftPlateRemovalText = plateRemovalPercent.toString()
            }
            params["shadow_removal_percent"]?.toIntOrNull()?.let {
                shadowRemovalPercent = it.coerceIn(MIN_SHADOW_REMOVAL_PERCENT, MAX_SHADOW_REMOVAL_PERCENT)
                draftShadowRemovalText = shadowRemovalPercent.toString()
            }
            params["edge_polish_percent"]?.toIntOrNull()?.let {
                edgePolishPercent = it.coerceIn(MIN_EDGE_POLISH_PERCENT, MAX_EDGE_POLISH_PERCENT)
                draftEdgePolishText = edgePolishPercent.toString()
            }
            params["rmbg_alpha_strength_percent"]?.toIntOrNull()?.let {
                rmbgAlphaStrengthPercent = it.coerceIn(
                    MIN_RMBG_ALPHA_STRENGTH_PERCENT,
                    MAX_RMBG_ALPHA_STRENGTH_PERCENT,
                )
                draftRmbgAlphaStrengthText = rmbgAlphaStrengthPercent.toString()
            }
            params["rmbg_edge_feather_percent"]?.toIntOrNull()?.let {
                rmbgEdgeFeatherPercent = it.coerceIn(
                    MIN_RMBG_EDGE_FEATHER_PERCENT,
                    MAX_RMBG_EDGE_FEATHER_PERCENT,
                )
                draftRmbgEdgeFeatherText = rmbgEdgeFeatherPercent.toString()
            }
            params["rmbg_edge_adjust_percent"]?.toIntOrNull()?.let {
                rmbgEdgeAdjustPercent = it.coerceIn(
                    MIN_RMBG_EDGE_ADJUST_PERCENT,
                    MAX_RMBG_EDGE_ADJUST_PERCENT,
                )
                draftRmbgEdgeAdjustText = rmbgEdgeAdjustPercent.toString()
            }
            params["rmbg_weak_alpha_keep_percent"]?.toIntOrNull()?.let {
                rmbgWeakAlphaKeepPercent = it.coerceIn(
                    MIN_RMBG_WEAK_ALPHA_KEEP_PERCENT,
                    MAX_RMBG_WEAK_ALPHA_KEEP_PERCENT,
                )
                draftRmbgWeakAlphaKeepText = rmbgWeakAlphaKeepPercent.toString()
            }
            params["liquid_glass_enabled"]?.toBooleanStrictOrNull()?.let {
                liquidGlassEnabled = it
            }
            intParam("liquid_glass_radius", "radius")?.let {
                liquidGlassRadius = it.coerceIn(MIN_LIQUID_GLASS_RADIUS, MAX_LIQUID_GLASS_RADIUS)
                draftLiquidGlassRadiusText = liquidGlassRadius.toString()
            }
            intParam("liquid_glass_outer_width", "outerWidth", "liquid_glass_background_level")?.let {
                liquidGlassOuterWidth = it.coerceIn(MIN_LIQUID_GLASS_OUTER_WIDTH, MAX_LIQUID_GLASS_OUTER_WIDTH)
                draftLiquidGlassOuterWidthText = liquidGlassOuterWidth.toString()
            }
            intParam("liquid_glass_top_alpha", "topAlpha")?.let {
                liquidGlassTopAlpha = it.coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)
                draftLiquidGlassTopAlphaText = liquidGlassTopAlpha.toString()
            }
            intParam("liquid_glass_bottom_alpha", "bottomAlpha")?.let {
                liquidGlassBottomAlpha = it.coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)
                draftLiquidGlassBottomAlphaText = liquidGlassBottomAlpha.toString()
            }
            intParam("liquid_glass_background_mist_alpha", "backgroundMistAlpha")?.let {
                liquidGlassBackgroundMistAlpha = it.coerceIn(MIN_LIQUID_GLASS_MIST_ALPHA, MAX_LIQUID_GLASS_MIST_ALPHA)
                draftLiquidGlassBackgroundMistAlphaText = liquidGlassBackgroundMistAlpha.toString()
            }
            intParam("liquid_glass_bottom_dark_alpha", "bottomDarkAlpha")?.let {
                liquidGlassBottomDarkAlpha = it.coerceIn(MIN_LIQUID_GLASS_BOTTOM_DARK_ALPHA, MAX_LIQUID_GLASS_BOTTOM_DARK_ALPHA)
                draftLiquidGlassBottomDarkAlphaText = liquidGlassBottomDarkAlpha.toString()
            }
            intParam("liquid_glass_subject_scale_percent", "subjectScale")?.let {
                liquidGlassSubjectScalePercent = it.coerceIn(
                    MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT,
                    MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT,
                )
                draftLiquidGlassSubjectScaleText = liquidGlassSubjectScalePercent.toString()
            }
            intParam("liquid_glass_subject_outline_width", "subjectOutlineWidth")?.let {
                liquidGlassSubjectOutlineWidth = it.coerceIn(
                    MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                    MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                )
                draftLiquidGlassSubjectOutlineWidthText = liquidGlassSubjectOutlineWidth.toString()
            }
            intParam("liquid_glass_subject_inner_outline_width", "subjectInnerOutlineWidth")?.let {
                liquidGlassSubjectInnerOutlineWidth = it.coerceIn(
                    MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                    MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                )
                draftLiquidGlassSubjectInnerOutlineWidthText = liquidGlassSubjectInnerOutlineWidth.toString()
            }
            intParam("liquid_glass_subject_shadow_alpha", "subjectShadowAlpha")?.let {
                liquidGlassSubjectShadowAlpha = it.coerceIn(
                    MIN_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA,
                    MAX_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA,
                )
                draftLiquidGlassSubjectShadowAlphaText = liquidGlassSubjectShadowAlpha.toString()
            }
            intParam("liquid_glass_subject_opacity_percent", "subjectOpacity")?.let {
                liquidGlassSubjectOpacityPercent = it.coerceIn(
                    MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
                    MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
                )
                draftLiquidGlassSubjectOpacityText = liquidGlassSubjectOpacityPercent.toString()
            }
            params["gpt_mode"]?.let {
                gptImageMode = GptImageMode.fromValue(it)
            }
            params["gpt_prompt_preset"]?.let {
                gptPromptPreset = GptPromptPreset.fromValue(it)
            }
            params["gpt_custom_prompt"]?.let {
                gptCustomPrompt = it
            }
            params["gpt_base_url"]?.let {
                gptBaseUrl = it
            }
            params["gpt_api_key"]?.let {
                gptApiKey = it
            }
            if (
                params.containsKey("gpt_mode") ||
                params.containsKey("gpt_prompt_preset") ||
                params.containsKey("gpt_custom_prompt") ||
                params.containsKey("gpt_base_url") ||
                params.containsKey("gpt_api_key")
            ) {
                saveGptSettings()
            }
            params["adaptive_foreground_mode"]?.let {
                adaptiveForegroundMode = AdaptiveForegroundMode.fromValue(it)
            }
            params["original_foreground_cleanup_mode"]?.let {
                originalForegroundCleanupMode = OriginalForegroundCleanupMode.fromValue(it)
            }
            params["adaptive_direct_max_coverage_percent"]?.toIntOrNull()?.let {
                adaptiveDirectMaxCoveragePercent = it.coerceIn(
                    MIN_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT,
                    MAX_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT,
                )
            }
            params["adaptive_direct_max_coverage_increase_percent"]?.toIntOrNull()?.let {
                adaptiveDirectMaxCoverageIncreasePercent = it.coerceIn(
                    MIN_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
                    MAX_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
                )
            }
            params["adaptive_mask_edge_coverage_percent"]?.toIntOrNull()?.let {
                adaptiveMaskEdgeCoveragePercent = it.coerceIn(
                    MIN_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT,
                    MAX_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT,
                )
            }
            params["adaptive_mask_min_coverage_percent"]?.toIntOrNull()?.let {
                adaptiveMaskMinCoveragePercent = it.coerceIn(
                    MIN_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT,
                    MAX_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT,
                )
            }
            params["adaptive_center_epsilon_percent"]?.toIntOrNull()?.let {
                adaptiveCenterEpsilonPercent = it.coerceIn(
                    MIN_ADAPTIVE_CENTER_EPSILON_PERCENT,
                    MAX_ADAPTIVE_CENTER_EPSILON_PERCENT,
                )
            }
            saveLiquidGlassSettings()
            saveImageTuningSettings()
            if (!isBusy && activeGenerationSession != null) {
                refreshActivePreviewOutputs(rebuildLocalCandidates = true)
            }
            snapshot = currentDebugParamsJson()
        }
        return snapshot ?: error("debug params unavailable")
    }

    private fun jsonToParamMap(json: JSONObject): Map<String, String> {
        val params = mutableMapOf<String, String>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            if (!json.isNull(key)) {
                params[key] = json.optString(key)
            }
        }
        return params
    }

    private fun debugHomeHtml(): String = """
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
          'liquid_glass_enabled'
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

    private data class DebugHttpResponse(
        val status: Int,
        val contentType: String,
        val body: String,
    )

    private data class DebugHttpRequest(
        val method: String,
        val target: String,
        val headers: Map<String, String>,
        val body: String,
    )

    private inner class DebugHttpServer(private val port: Int) {
        @Volatile
        private var running = false
        private var serverSocket: ServerSocket? = null
        private var localServerSocket: LocalServerSocket? = null
        private var thread: Thread? = null
        private var localThread: Thread? = null

        fun start() {
            if (running) {
                return
            }
            running = true
            startTcpServer()
            startLocalServer()
        }

        private fun startTcpServer() {
            thread = Thread({
                runCatching {
                    ServerSocket().use { server ->
                        server.reuseAddress = true
                        server.bind(InetSocketAddress(InetAddress.getLoopbackAddress(), port))
                        serverSocket = server
                        while (running) {
                            val socket = runCatching { server.accept() }.getOrNull() ?: break
                            Thread({ handle(socket) }, "ArtPlusDebugHttpClient").also {
                                it.isDaemon = true
                                it.start()
                            }
                        }
                    }
                }.onFailure {
                    if (running) {
                        status("Debug HTTP 启动失败: ${it.message ?: it.javaClass.simpleName}")
                    }
                }
            }, "ArtPlusDebugHttp").also {
                it.isDaemon = true
                it.start()
            }
        }

        private fun startLocalServer() {
            localThread = Thread({
                var server: LocalServerSocket? = null
                try {
                    server = LocalServerSocket(DEBUG_HTTP_ABSTRACT_NAME)
                    localServerSocket = server
                    while (running) {
                        val socket = runCatching { server.accept() }.getOrNull() ?: break
                        Thread({ handle(socket) }, "ArtPlusDebugLocalClient").also {
                            it.isDaemon = true
                            it.start()
                        }
                    }
                } catch (error: Exception) {
                    if (running) {
                        status("Debug local HTTP 启动失败: ${error.message ?: error.javaClass.simpleName}")
                    }
                } finally {
                    runCatching { server?.close() }
                }
            }, "ArtPlusDebugLocalHttp").also {
                it.isDaemon = true
                it.start()
            }
        }

        fun stop() {
            running = false
            runCatching { serverSocket?.close() }
            runCatching { localServerSocket?.close() }
            serverSocket = null
            localServerSocket = null
            thread = null
            localThread = null
        }

        private fun handle(socket: Socket) {
            socket.use { client ->
                try {
                    client.soTimeout = DEBUG_HTTP_READ_TIMEOUT_MS
                    handleStreams(client.getInputStream(), client.getOutputStream())
                } catch (error: Exception) {
                    writeResponseQuietly(client.getOutputStream(), errorResponse(error))
                }
            }
        }

        private fun handle(socket: LocalSocket) {
            try {
                handleStreams(socket.inputStream, socket.outputStream)
            } catch (error: Exception) {
                writeResponseQuietly(socket.outputStream, errorResponse(error))
            } finally {
                runCatching { socket.close() }
            }
        }

        private fun handleStreams(input: InputStream, output: OutputStream) {
            val request = readRequest(input)
            if (request == null) {
                writeResponse(output, DebugHttpResponse(400, "application/json; charset=utf-8", "{\"ok\":false,\"error\":\"bad request\"}"))
                return
            }
            writeResponse(output, route(request))
        }

        private fun readRequest(input: InputStream): DebugHttpRequest? {
            val headerBytes = readHttpHeader(input) ?: return null
            val headerText = String(headerBytes, StandardCharsets.UTF_8)
            val lines = headerLines(headerText)
            val requestLine = lines.firstOrNull() ?: return null
            val parts = requestLine.trim().split(Regex("\\s+"))
            if (parts.size < 2) {
                return null
            }
            val headers = mutableMapOf<String, String>()
            lines.drop(1).forEach { line ->
                val separator = line.indexOf(':')
                if (separator > 0) {
                    headers[line.substring(0, separator).lowercase(Locale.US)] = line.substring(separator + 1).trim()
                }
            }
            val contentLength = headers["content-length"]?.toIntOrNull() ?: 0
            val body = if (contentLength > 0) {
                readExactlyAvailable(input, contentLength.coerceAtMost(DEBUG_HTTP_MAX_BODY_BYTES))
            } else {
                ""
            }
            return DebugHttpRequest(
                method = parts[0].uppercase(Locale.US),
                target = parts[1],
                headers = headers,
                body = body,
            )
        }

        private fun readHttpHeader(input: InputStream): ByteArray? {
            val header = ByteArrayOutputStream()
            while (header.size() < DEBUG_HTTP_MAX_HEADER_BYTES) {
                val next = input.read()
                if (next < 0) {
                    return if (header.size() == 0) null else header.toByteArray()
                }
                header.write(next)
                val bytes = header.toByteArray()
                val size = bytes.size
                if (
                    size >= 4 &&
                    bytes[size - 4] == '\r'.code.toByte() &&
                    bytes[size - 3] == '\n'.code.toByte() &&
                    bytes[size - 2] == '\r'.code.toByte() &&
                    bytes[size - 1] == '\n'.code.toByte()
                ) {
                    return bytes
                }
                if (
                    size >= 2 &&
                    bytes[size - 2] == '\n'.code.toByte() &&
                    bytes[size - 1] == '\n'.code.toByte()
                ) {
                    return bytes
                }
            }
            return null
        }

        private fun readExactlyAvailable(input: InputStream, length: Int): String {
            val bodyBytes = ByteArray(length)
            var offset = 0
            while (offset < length) {
                val read = input.read(bodyBytes, offset, length - offset)
                if (read < 0) {
                    break
                }
                offset += read
            }
            return String(bodyBytes, 0, offset, StandardCharsets.UTF_8)
        }

        private fun headerLines(headerText: String): List<String> =
            headerText
                .replace("\r\n", "\n")
                .split('\n')
                .map { it.trimEnd('\r') }
                .filter { it.isNotBlank() }

        private fun route(request: DebugHttpRequest): DebugHttpResponse {
            val method = request.method
            val target = request.target
            val body = request.body
            val path = target.substringBefore('?')
            val query = parseQuery(target.substringAfter('?', ""))
            return try {
                if (!isAuthorizedDebugRequest(request, query, body)) {
                    return jsonResponse(JSONObject().put("ok", false).put("error", "forbidden"), 403)
                }
                when {
                    method == "GET" && (path == "/" || path == "/debug") ->
                        DebugHttpResponse(200, "text/html; charset=utf-8", debugHomeHtml())
                    method == "GET" && path == "/debug/params" ->
                        jsonResponse(currentDebugParamsOnMain())
                    method == "POST" && path == "/debug/params" -> {
                        val params = query.toMutableMap()
                        params.putAll(parseBodyParams(body))
                        jsonResponse(applyDebugParams(params))
                    }
                    path == "/debug/status" ->
                        jsonResponse(currentDebugParamsOnMain())
                    method == "POST" && path == "/debug/inspect" -> {
                        val params = query.toMutableMap()
                        params.putAll(parseBodyParams(body))
                        val packageName = params["package"]?.trim().orEmpty()
                        if (packageName.isEmpty()) {
                            jsonResponse(JSONObject().put("ok", false).put("error", "missing package"), 400)
                        } else {
                            jsonResponse(
                                debugInspectPackage(
                                    packageName = packageName,
                                    includeRmbg = params["include_rmbg"]?.toBooleanStrictOrNull() ?: false,
                                ),
                            )
                        }
                    }
                    method == "POST" && path == "/debug/generate" -> {
                        val params = query.toMutableMap()
                        params.putAll(parseBodyParams(body))
                        val packageName = params["package"]?.trim().orEmpty()
                        if (packageName.isEmpty()) {
                            jsonResponse(JSONObject().put("ok", false).put("error", "missing package"), 400)
                        } else {
                            val mode = LocalSeparationMode.fromValue(params["mode"])
                            val accepted = startDebugGeneration(
                                packageName = packageName,
                                useGpt = params["use_gpt"]?.toBooleanStrictOrNull() ?: false,
                                installWithRoot = params["install_root"]?.toBooleanStrictOrNull() ?: false,
                                debugMode = mode,
                                rootWriteMode = RootWriteMode.fromValue(params["root_write_mode"]),
                            )
                            val snapshot = currentDebugParamsOnMain()
                            jsonResponse(
                                JSONObject()
                                    .put("ok", accepted)
                                    .put("package", packageName)
                                    .put("mode", mode.value)
                                    .put("rmbg_status", snapshot.optString("rmbg_status"))
                                    .put("status", snapshot.optString("status")),
                                if (accepted) 202 else 409,
                            )
                        }
                    }
                    else -> jsonResponse(JSONObject().put("ok", false).put("error", "not found"), 404)
                }
            } catch (error: Exception) {
                jsonResponse(
                    JSONObject()
                        .put("ok", false)
                        .put("error", error.message ?: error.javaClass.simpleName),
                    500,
                )
            }
        }

        private fun isAuthorizedDebugRequest(
            request: DebugHttpRequest,
            query: Map<String, String>,
            body: String,
        ): Boolean {
            val bodyParams = runCatching { parseBodyParams(body) }.getOrDefault(emptyMap())
            val token = request.headers[DEBUG_HTTP_TOKEN_HEADER.lowercase(Locale.US)]
                ?: query[DEBUG_HTTP_TOKEN_PARAM]
                ?: bodyParams[DEBUG_HTTP_TOKEN_PARAM]
            return isDebugTokenValid(token)
        }

        private fun parseBodyParams(body: String): Map<String, String> {
            val trimmed = body.trim()
            if (trimmed.isEmpty()) {
                return emptyMap()
            }
            return if (trimmed.startsWith("{")) {
                jsonToParamMap(JSONObject(trimmed))
            } else {
                parseQuery(trimmed)
            }
        }

        private fun parseQuery(query: String): Map<String, String> {
            if (query.isBlank()) {
                return emptyMap()
            }
            return query.split('&')
                .filter { it.isNotBlank() }
                .associate { pair ->
                    val key = pair.substringBefore('=')
                    val value = pair.substringAfter('=', "")
                    urlDecode(key) to urlDecode(value)
                }
        }

        private fun urlDecode(value: String): String =
            URLDecoder.decode(value, StandardCharsets.UTF_8.name())

        private fun jsonResponse(json: JSONObject, status: Int = 200): DebugHttpResponse =
            DebugHttpResponse(status, "application/json; charset=utf-8", json.toString(2))

        private fun errorResponse(error: Exception): DebugHttpResponse =
            jsonResponse(
                JSONObject()
                    .put("ok", false)
                    .put("error", error.message ?: error.javaClass.simpleName),
                500,
            )

        private fun writeResponseQuietly(output: OutputStream, response: DebugHttpResponse) {
            runCatching { writeResponse(output, response) }
        }

        private fun writeResponse(output: OutputStream, response: DebugHttpResponse) {
            val bytes = response.body.toByteArray(StandardCharsets.UTF_8)
            val reason = when (response.status) {
                200 -> "OK"
                202 -> "Accepted"
                400 -> "Bad Request"
                403 -> "Forbidden"
                404 -> "Not Found"
                409 -> "Conflict"
                else -> "Error"
            }
            val header = buildString {
                append("HTTP/1.1 ${response.status} $reason\r\n")
                append("Content-Type: ${response.contentType}\r\n")
                append("Content-Length: ${bytes.size}\r\n")
                append("Cache-Control: no-store\r\n")
                append("Connection: close\r\n")
                append("\r\n")
            }
            output.write(header.toByteArray(StandardCharsets.UTF_8))
            output.write(bytes)
            output.flush()
        }
    }

    private fun copyStream(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(64 * 1024)
        while (true) {
            val read = input.read(buffer)
            if (read == -1) {
                return
            }
            output.write(buffer, 0, read)
        }
    }

    private fun OutputStream?.useRequired(block: (OutputStream) -> Unit) {
        val output = this ?: error("无法打开输出流")
        output.use { block(it) }
    }

    private fun loadPreviewAssets(dir: File): PreviewAssets =
        PreviewAssets(
            recbg = decodePreviewBitmap(dir, "recbg.png"),
            recfg = decodePreviewBitmap(dir, "recfg.png"),
            recNight = decodePreviewBitmap(dir, "rec_night.png"),
            monochromeLight = decodePreviewBitmap(dir, "monochrome_light.png")
                ?: decodePreviewBitmap(dir, "monochrome.png"),
            monochromeDark = decodePreviewBitmap(dir, "monochrome_dark.png")
                ?: decodePreviewBitmap(dir, "monochrome.png"),
        )

    private fun decodePreviewBitmap(dir: File, name: String): Bitmap? =
        BitmapFactory.decodeFile(File(dir, name).absolutePath)?.also { it.prepareToDraw() }

    private data class AppEntry(
        val label: String,
        val packageName: String,
        val applicationInfo: ApplicationInfo,
        val launchable: Boolean,
        val iconKey: String,
    )

    private data class IconLayers(
        val recfg: Bitmap,
        val recbg: Bitmap,
    )

    private data class LocalIconLayers(
        val recfg: Bitmap,
        val recbg: Bitmap,
        val monochrome: Bitmap?,
        val monochromeIsNative: Boolean,
        val preserveGeometry: Boolean,
        val textSafe: IconCandidate?,
        val componentSubject: IconCandidate?,
        val componentBackground: IconCandidate?,
    )

    private data class AdaptiveForegroundSelection(
        val bitmap: Bitmap,
        val preserveGeometry: Boolean,
    )

    private data class ComponentCandidates(
        val subject: IconCandidate?,
        val background: IconCandidate?,
    )

    private data class IconCandidate(
        val recfgRaw: Bitmap,
        val recbg: Bitmap,
        val monochromeRaw: Bitmap?,
        val monochromeIsNative: Boolean = false,
        val monochromeFromDefaultSubject: Boolean = false,
        val preserveGeometry: Boolean = false,
        val customFinalBitmap: Bitmap? = null,
        val rmbgSourceRaw: Bitmap? = null,
        val rmbgAlphaRaw: IntArray? = null,
    )

    private data class GenerationSession(
        val packageName: String,
        val outDir: File,
        val sourceIcon: Bitmap,
        val baseRecfg: Bitmap,
        val baseRecbg: Bitmap,
        val monochromeRaw: Bitmap?,
        val candidates: Map<PreviewChoice, IconCandidate>,
        val customForegrounds: Map<PreviewMode, Bitmap> = emptyMap(),
        val customBackgrounds: Map<PreviewMode, Bitmap> = emptyMap(),
        val autoLocalChoice: PreviewChoice,
        val canRebuildLocalCandidates: Boolean = true,
    )

    private data class ForegroundShadowParams(
        val alpha: Int,
        val blurRadius: Float,
        val offsetX: Int,
        val offsetY: Int,
        val spread: Int,
    )

    private data class GenerationResult(
        val outDir: File,
        val session: GenerationSession,
        val selections: PreviewSelections,
    )

    private data class LocalSeparationResult(
        val bitmap: Bitmap,
        val summary: String,
    )

    private data class LocalCandidateSet(
        val candidates: Map<PreviewChoice, IconCandidate>,
        val autoChoice: PreviewChoice,
    )

    private data class CandidateBuildResult(
        val candidate: IconCandidate?,
        val autoUsable: Boolean,
        val coverage: Double,
        val rmbgInference: RmbgInferenceReport? = null,
        val manualUsable: Boolean = true,
        val validationWarning: String? = null,
    )

    private data class MaskComponent(
        val indices: IntArray,
        val touchesEdge: Boolean,
        val bounds: Bounds,
    ) {
        val size: Int
            get() = indices.size
    }

    private data class ForegroundCleanupResult(
        val bitmap: Bitmap,
        val changed: Boolean,
        val removedRatio: Double,
        val repairedRatio: Double,
    )

    private data class ShadowCleanupResult(
        val bitmap: Bitmap,
        val changed: Boolean,
        val removedRatio: Double,
    )

    private data class EdgeAnalysis(
        val coverage: Double,
        val color: Int,
    )

    private data class PreviewAssets(
        val recbg: Bitmap?,
        val recfg: Bitmap?,
        val recNight: Bitmap?,
        val monochromeLight: Bitmap?,
        val monochromeDark: Bitmap?,
    ) {
        fun missingMessage(mode: PreviewMode): String? =
            when (mode) {
                PreviewMode.NormalLight -> if (recbg == null || recfg == null) "缺少 recbg/recfg" else null
                PreviewMode.NormalDark -> if (recNight == null) "缺少 rec_night" else null
                PreviewMode.MonochromeLight -> if (monochromeLight == null) "缺少 monochrome" else null
                PreviewMode.MonochromeDark -> if (monochromeDark == null) "缺少 monochrome" else null
            }
    }

    private fun PreviewAssets.preparedForDraw(): PreviewAssets {
        recbg?.prepareToDraw()
        recfg?.prepareToDraw()
        recNight?.prepareToDraw()
        monochromeLight?.prepareToDraw()
        monochromeDark?.prepareToDraw()
        return this
    }

    private enum class PreviewMode(val label: String) {
        NormalLight("正常亮色"),
        NormalDark("正常暗色"),
        MonochromeLight("单色亮色"),
        MonochromeDark("单色暗色"),
    }

    private enum class PreviewDesktopBackground(val label: String, val fallbackColor: Color) {
        LightGray("浅灰", Color(0xFFE8E8E8)),
        DarkGray("深灰", Color(0xFF4A4A4A)),
        Black("纯黑", Color(0xFF050505)),
        Wallpaper("桌面", Color(0xFF30333A));

        companion object {
            fun fromName(name: String?): PreviewDesktopBackground =
                entries.firstOrNull { it.name == name } ?: DarkGray
        }
    }

    private enum class PreviewChoice(
        val label: String,
        val summary: String,
        val customKind: CustomImageKind? = null,
    ) {
        Original("原始", "保留原层"),
        TextSafe("字标保全", "保护白字"),
        Plate("清理", "兼容旧去底板规则"),
        Full("清理", "合并底板与阴影清理"),
        ComposedBackground("拼合背景", "从完整图标提取背景"),
        ComponentSubject("底座当主体", "保留复杂底座"),
        ComponentBackground("底座当背景", "底座作为背景"),
        TwoLayer("二层", "底板和主体分层"),
        Rmbg("RMBG", "模型抠图"),
        Gpt("GPT", "GPT Image 2"),
        RmbgComposedBackground("拼合背景", "RMBG 主体 + 原图背景"),
        GptComposedBackground("拼合背景", "GPT 主体 + 原图背景"),
        CustomForeground("自定义主体", "导入主体", CustomImageKind.Foreground),
        CustomBackground("自定义背景", "导入背景", CustomImageKind.Background);

        val isCustom: Boolean
            get() = customKind != null
    }

    private val PreviewChoice.isComposedBackgroundCombination: Boolean
        get() = this == PreviewChoice.RmbgComposedBackground ||
            this == PreviewChoice.GptComposedBackground

    private enum class CustomImageKind(val label: String) {
        Foreground("自定义主体"),
        Background("自定义背景"),
    }

    private data class PreviewSelections(
        val normalLight: PreviewChoice,
        val normalDark: PreviewChoice,
        val monochromeLight: PreviewChoice,
        val monochromeDark: PreviewChoice,
    ) {
        fun choiceFor(mode: PreviewMode): PreviewChoice =
            when (mode) {
                PreviewMode.NormalLight -> normalLight
                PreviewMode.NormalDark -> normalDark
                PreviewMode.MonochromeLight -> monochromeLight
                PreviewMode.MonochromeDark -> monochromeDark
            }

        fun withChoice(mode: PreviewMode, choice: PreviewChoice): PreviewSelections =
            when (mode) {
                PreviewMode.NormalLight -> copy(normalLight = choice)
                PreviewMode.NormalDark -> copy(normalDark = choice)
                PreviewMode.MonochromeLight -> copy(monochromeLight = choice)
                PreviewMode.MonochromeDark -> copy(monochromeDark = choice)
            }

        fun retarget(from: PreviewChoice, to: PreviewChoice): PreviewSelections {
            if (from == to) {
                return this
            }
            return PreviewSelections(
                normalLight = replaceDefault(normalLight, from, to),
                normalDark = replaceDefault(normalDark, from, to),
                monochromeLight = replaceDefault(monochromeLight, from, to),
                monochromeDark = replaceDefault(monochromeDark, from, to),
            )
        }

        private fun replaceDefault(
            choice: PreviewChoice,
            from: PreviewChoice,
            to: PreviewChoice,
        ): PreviewChoice =
            if (choice == from) to else choice

        companion object {
            fun default(choice: PreviewChoice): PreviewSelections =
                PreviewSelections(
                    normalLight = choice,
                    normalDark = choice,
                    monochromeLight = choice,
                    monochromeDark = choice,
                )

            fun fromPrefs(prefs: android.content.SharedPreferences): PreviewSelections =
                PreviewSelections(
                    normalLight = previewChoiceFromName(
                        prefs.getString(PREF_PREVIEW_SELECTION_NORMAL_LIGHT, null),
                    ),
                    normalDark = previewChoiceFromName(
                        prefs.getString(PREF_PREVIEW_SELECTION_NORMAL_DARK, null),
                    ),
                    monochromeLight = previewChoiceFromName(
                        prefs.getString(PREF_PREVIEW_SELECTION_MONOCHROME_LIGHT, null),
                    ),
                    monochromeDark = previewChoiceFromName(
                        prefs.getString(PREF_PREVIEW_SELECTION_MONOCHROME_DARK, null),
                    ),
                )

            private fun previewChoiceFromName(name: String?): PreviewChoice =
                PreviewChoice.entries.firstOrNull { it.name == name }
                    ?.let { if (it == PreviewChoice.Plate) PreviewChoice.Full else it }
                    ?.takeUnless { it.isCustom }
                    ?: PreviewChoice.Full
        }
    }

    private enum class AppPage(val order: Int) {
        Home(0),
        Settings(1),
        AppPicker(1),
    }

    private data class Bounds(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int,
    ) {
        fun width(): Int = right - left
        fun height(): Int = bottom - top
    }

    private enum class GptImageMode(val value: String, val label: String) {
        Responses("responses", "响应模式"),
        Images("images", "接口模式");

        companion object {
            fun fromValue(value: String?): GptImageMode =
                entries.firstOrNull { it.value == value } ?: Responses
        }
    }

    private enum class GptPromptPreset(
        val value: String,
        val label: String,
        val summary: String,
        val foregroundPrompt: String,
    ) {
        Default(
            "default",
            "默认",
            "保留主体颜色和细节",
            "Keep only the app icon main subject/logo. Remove the original background. " +
                "Return the remaining subject/logo on a transparent background. " +
                "Do not add any new circle, glow, outline, shadow, halo, plate, or filled backdrop behind the subject. " +
                "Preserve the subject shape, position, colors, face details, highlights, and internal shading.",
        ),
        StableCutout(
            "stable_cutout",
            "镂空稳定",
            "优先保留孔洞和负形",
            "Extract only the visible foreground subject/logo from the app icon with a precise alpha mask. " +
                "Preserve all cutouts, counters, holes, transparent gaps, negative-space shapes, inner openings, and thin strokes exactly as in the source. " +
                "Do not fill enclosed holes or bridge gaps. Do not invent a backing plate, outline, halo, shadow, glow, circle, rounded square, or extra background. " +
                "Keep antialiasing on the true subject edge and preserve the original colors, gradients, highlights, shadows, and internal details of the subject.",
        ),
        CleanLayers(
            "clean_layers",
            "干净分层",
            "主体与背景分离更强",
            "Separate the app icon into a clean foreground subject/logo only. " +
                "Remove every background plate, wallpaper, rounded square, circle, glow, halo, cast shadow, and decorative backdrop. " +
                "Keep the subject/logo centered and preserve its original colors, gradients, highlights, and internal shading without redrawing it.",
        ),
        Custom(
            "custom",
            "自定义",
            "使用下面输入的前景提示词",
            "",
        );

        companion object {
            fun fromValue(value: String?): GptPromptPreset =
                entries.firstOrNull { it.value == value } ?: StableCutout
        }
    }

    private data class RmbgModelPreset(
        val id: String,
        val label: String,
        val summary: String,
        val url: String,
    )

    private enum class RmbgInferenceBackend(
        val value: String,
        val label: String,
    ) {
        Cpu("cpu", "CPU"),
    }

    private enum class LocalSeparationMode(val value: String, val label: String, val summary: String) {
        Auto("auto", "自动", "按图标特征自动选择底板清理、边缘修复或阴影清理"),
        Original("original", "原始", "完全保留系统绘制的前景层"),
        Plate("plate", "清理", "兼容旧去底板规则"),
        Full("full", "清理", "合并底板和长阴影清理"),
        ComposedBackground("composed_background", "拼合背景", "先拼合完整图标，再从拼合图里估算背景并分离主体"),
        ComponentSubject("component_subject", "底座当主体", "把 adaptive background 里的复杂底座合进主体，背景重建为纯色或渐变"),
        ComponentBackground("component_background", "底座当背景", "保留 adaptive background 为背景，只取 foreground 当主体");

        companion object {
            fun fromValue(value: String?): LocalSeparationMode =
                entries.firstOrNull { it.value == value }
                    ?.let { if (it == Plate) Full else it }
                    ?: Auto
        }
    }

    private enum class AdaptiveForegroundMode(val value: String, val label: String) {
        Auto("auto", "自动"),
        Composed("composed", "合成减背景"),
        Direct("direct", "直接前景");

        companion object {
            fun fromValue(value: String?): AdaptiveForegroundMode =
                entries.firstOrNull { it.value == value } ?: Auto
        }
    }

    private enum class OriginalForegroundCleanupMode(val value: String, val label: String) {
        Auto("auto", "自动安全清理"),
        Off("off", "关闭"),
        Plate("plate", "强制去底板");

        companion object {
            fun fromValue(value: String?): OriginalForegroundCleanupMode =
                entries.firstOrNull { it.value == value } ?: Auto
        }
    }

    private enum class GeneratedFilter(val label: String) {
        All("全部"),
        Generated("已生成"),
        Ungenerated("未生成");

        companion object {
            fun fromName(name: String?): GeneratedFilter =
                entries.firstOrNull { it.name == name } ?: All
        }
    }

    private enum class AdvancedSettingsCategory(val label: String) {
        LiquidGlass("液态玻璃"),
        Local("本地规则"),
        Rmbg("RMBG");

        companion object {
            fun fromName(name: String?): AdvancedSettingsCategory =
                entries.firstOrNull { it.name == name } ?: LiquidGlass
        }
    }

    private enum class RootWriteMode(val value: String, val label: String) {
        All("all", "全部"),
        DefaultOnly("default", "默认"),
        MonochromeOnly("monochrome", "单色");

        companion object {
            fun fromValue(value: String?): RootWriteMode =
                entries.firstOrNull { it.value == value } ?: All
        }
    }

    companion object {
        private const val PREFS_NAME = "artplus_mobile"
        private const val PREF_GENERATED_PACKAGE_NAMES = "generated_package_names"
        private const val PREF_GENERATED_PACKAGE_NAMES_UPDATED_AT = "generated_package_names_updated_at"
        private const val PREF_GPT_MODE = "gpt_mode"
        private const val PREF_GPT_PROMPT_PRESET = "gpt_prompt_preset"
        private const val PREF_GPT_CUSTOM_PROMPT = "gpt_custom_prompt"
        private const val PREF_GPT_BASE_URL = "gpt_base_url"
        private const val PREF_GPT_API_KEY = "gpt_api_key"
        private const val PREF_GPT_API_KEY_ENCRYPTED = "gpt_api_key_encrypted"
        private const val PREF_RMBG_COMPONENT_URL = "rmbg_component_url"
        private const val PREF_RMBG_INPUT_SIZE = "rmbg_input_size"
        private const val PREF_RMBG_INPUT_SIZE_MIGRATED_TO_1024 = "rmbg_input_size_migrated_to_1024"
        private const val PREF_LOCAL_SEPARATION_MODE = "local_separation_mode"
        private const val PREF_FOREGROUND_SUBJECT_PERCENT = "foreground_subject_percent"
        private const val PREF_FOREGROUND_SHADOW_LEVEL = "foreground_shadow_level"
        private const val PREF_MONOCHROME_THEME_SCALE = "monochrome_theme_scale"
        private const val PREF_BACKGROUND_SEPARATION_PERCENT = "background_separation_percent"
        private const val PREF_PLATE_REMOVAL_PERCENT = "plate_removal_percent"
        private const val PREF_SHADOW_REMOVAL_PERCENT = "shadow_removal_percent"
        private const val PREF_EDGE_POLISH_PERCENT = "edge_polish_percent"
        private const val PREF_RMBG_ALPHA_STRENGTH_PERCENT = "rmbg_alpha_strength_percent"
        private const val PREF_RMBG_EDGE_FEATHER_PERCENT = "rmbg_edge_feather_percent"
        private const val PREF_RMBG_EDGE_ADJUST_PERCENT = "rmbg_edge_adjust_percent"
        private const val PREF_RMBG_WEAK_ALPHA_KEEP_PERCENT = "rmbg_weak_alpha_keep_percent"
        private const val PREF_LIQUID_GLASS_ENABLED = "liquid_glass_enabled"
        private const val PREF_LIQUID_GLASS_LAYERED_MIGRATED = "liquid_glass_layered_migrated"
        private const val PREF_LIQUID_GLASS_RADIUS = "liquid_glass_radius"
        private const val PREF_LIQUID_GLASS_OUTER_WIDTH = "liquid_glass_outer_width"
        private const val PREF_LIQUID_GLASS_TOP_ALPHA = "liquid_glass_top_alpha"
        private const val PREF_LIQUID_GLASS_BOTTOM_ALPHA = "liquid_glass_bottom_alpha"
        private const val PREF_LIQUID_GLASS_BACKGROUND_MIST_ALPHA = "liquid_glass_background_mist_alpha"
        private const val PREF_LIQUID_GLASS_BOTTOM_DARK_ALPHA = "liquid_glass_bottom_dark_alpha"
        private const val PREF_LIQUID_GLASS_SUBJECT_SCALE_PERCENT = "liquid_glass_subject_scale_percent"
        private const val PREF_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH = "liquid_glass_subject_outline_width"
        private const val PREF_LIQUID_GLASS_SUBJECT_INNER_OUTLINE_WIDTH = "liquid_glass_subject_inner_outline_width"
        private const val PREF_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA = "liquid_glass_subject_shadow_alpha"
        private const val PREF_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT = "liquid_glass_subject_opacity_percent"
        private const val PREF_LIQUID_GLASS_BACKGROUND_LEVEL_LEGACY = "liquid_glass_background_level"
        private const val PREF_ADAPTIVE_FOREGROUND_MODE = "adaptive_foreground_mode"
        private const val PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT = "adaptive_direct_max_coverage_percent"
        private const val PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT = "adaptive_direct_max_coverage_increase_percent"
        private const val PREF_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT = "adaptive_mask_edge_coverage_percent"
        private const val PREF_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT = "adaptive_mask_min_coverage_percent"
        private const val PREF_ADAPTIVE_CENTER_EPSILON_PERCENT = "adaptive_center_epsilon_percent"
        private const val PREF_ORIGINAL_FOREGROUND_CLEANUP_MODE = "original_foreground_cleanup_mode"
        private const val PREF_NIGHT_SUBJECT_LIGHT_BACKGROUND_ENABLED = "night_subject_light_background_enabled"
        private const val PREF_IMAGE_TUNING_VERSION = "image_tuning_version"
        private const val PREF_FOREGROUND_SUBJECT_PERCENT_MIGRATED = "foreground_subject_percent_migrated"
        private const val PREF_USAGE_PERMISSION_PROMPTED = "usage_permission_prompted"
        private const val PREF_DEBUG_TOKEN = "debug_token"
        private const val PREF_SELECTED_PACKAGE_NAME = "selected_package_name"
        private const val PREF_GENERATED_FILTER = "generated_filter"
        private const val PREF_QUERY_TEXT = "query_text"
        private const val PREF_ADVANCED_SETTINGS_CATEGORY = "advanced_settings_category"
        private const val PREF_PREVIEW_PACKAGE_NAME = "preview_package_name"
        private const val PREF_PREVIEW_DIR_PATH = "preview_dir_path"
        private const val PREF_PREVIEW_SELECTION_NORMAL_LIGHT = "preview_selection_normal_light"
        private const val PREF_PREVIEW_SELECTION_NORMAL_DARK = "preview_selection_normal_dark"
        private const val PREF_PREVIEW_SELECTION_MONOCHROME_LIGHT = "preview_selection_monochrome_light"
        private const val PREF_PREVIEW_SELECTION_MONOCHROME_DARK = "preview_selection_monochrome_dark"
        private const val PREF_PREVIEW_DESKTOP_BACKGROUND = "preview_desktop_background"
        private const val PREF_PREVIEW_ICON_SIZE_DP = "preview_icon_size_dp"
        private const val EXTRA_DEBUG_GENERATE_PACKAGE = "dev.artplus.mobile.DEBUG_GENERATE_PACKAGE"
        private const val EXTRA_DEBUG_GENERATE_USE_GPT = "dev.artplus.mobile.DEBUG_GENERATE_USE_GPT"
        private const val EXTRA_DEBUG_GENERATE_INSTALL_ROOT = "dev.artplus.mobile.DEBUG_GENERATE_INSTALL_ROOT"
        private const val EXTRA_DEBUG_GENERATE_MODE = "dev.artplus.mobile.DEBUG_GENERATE_MODE"
        private const val EXTRA_DEBUG_GENERATE_ROOT_WRITE_MODE = "dev.artplus.mobile.DEBUG_GENERATE_ROOT_WRITE_MODE"
        private const val EXTRA_DEBUG_GENERATE_TOKEN = "dev.artplus.mobile.DEBUG_GENERATE_TOKEN"
        private const val CURRENT_IMAGE_TUNING_VERSION = 4
        private const val SIZE_1X1 = 240
        private const val SIZE_2X2 = 704
        private const val LOCAL_ICON_RENDER_SCALE = 3
        private const val GPT_SOURCE_SIZE = 1024
        private const val RMBG_COMPONENT_DIR = "rmbg_component"
        private const val RMBG_MODEL_NAME = "bria-rmbg.onnx"
        private const val DEFAULT_RMBG_INPUT_SIZE = 1024
        private const val RMBG_MIN_MODEL_BYTES = 100_000_000L
        private const val RMBG_MIN_COMPONENT_ZIP_BYTES = 100_000_000L
        private const val RMBG_MAX_DOWNLOAD_BYTES = 2L * 1024L * 1024L * 1024L
        private const val RMBG_MAX_COMPONENT_ZIP_ENTRIES = 128
        private const val RMBG_MAX_COMPONENT_ZIP_UNPACK_BYTES = 800L * 1024L * 1024L
        private const val RMBG_DOWNLOAD_CONNECT_TIMEOUT_MS = 30_000
        private const val RMBG_DOWNLOAD_READ_TIMEOUT_MS = 1_800_000
        private const val RMBG_MODEL_URL_ORIGINAL =
            "https://modelscope.cn/models/AI-ModelScope/RMBG-2.0/resolve/master/onnx/model.onnx"
        private const val RMBG_MODEL_URL_QUANTIZED =
            "https://modelscope.cn/models/AI-ModelScope/RMBG-2.0/resolve/master/onnx/model_quantized.onnx"
        private const val RMBG_MODEL_URL_UINT8 =
            "https://modelscope.cn/models/AI-ModelScope/RMBG-2.0/resolve/master/onnx/model_uint8.onnx"
        private const val RMBG_MODEL_URL_INT8 =
            "https://modelscope.cn/models/AI-ModelScope/RMBG-2.0/resolve/master/onnx/model_int8.onnx"
        private const val RMBG_MODEL_URL_FP16 =
            "https://modelscope.cn/models/AI-ModelScope/RMBG-2.0/resolve/master/onnx/model_fp16.onnx"
        private const val RMBG_MODEL_URL_Q4 =
            "https://modelscope.cn/models/AI-ModelScope/RMBG-2.0/resolve/master/onnx/model_q4.onnx"
        private const val RMBG_MODEL_URL_BNB4 =
            "https://modelscope.cn/models/AI-ModelScope/RMBG-2.0/resolve/master/onnx/model_bnb4.onnx"
        private const val RMBG_MODEL_URL_Q4F16 =
            "https://modelscope.cn/models/AI-ModelScope/RMBG-2.0/resolve/master/onnx/model_q4f16.onnx"
        private val DEFAULT_RMBG_COMPONENT_URL =
            RMBG_MODEL_URL_QUANTIZED.ifBlank { RMBG_MODEL_URL_ORIGINAL }
        private val RMBG_MODEL_PRESET_CUSTOM = RmbgModelPreset(
            id = "custom",
            label = "自定义 URL",
            summary = "手动填写模型或组件 ZIP 地址",
            url = "",
        )
        private val RMBG_MODEL_PRESETS = listOf(
            RmbgModelPreset(
                id = "rmbg20_quantized",
                label = "量化推荐",
                summary = "model_quantized.onnx · 349MB · 默认候选",
                url = RMBG_MODEL_URL_QUANTIZED,
            ),
            RmbgModelPreset(
                id = "rmbg20_uint8",
                label = "UINT8",
                summary = "model_uint8.onnx · 349MB · 备选",
                url = RMBG_MODEL_URL_UINT8,
            ),
            RmbgModelPreset(
                id = "rmbg20_int8",
                label = "INT8",
                summary = "model_int8.onnx · 349MB · 备选",
                url = RMBG_MODEL_URL_INT8,
            ),
            RmbgModelPreset(
                id = "rmbg20_original",
                label = "原版",
                summary = "model.onnx · 官方 ONNX",
                url = RMBG_MODEL_URL_ORIGINAL,
            ),
            RmbgModelPreset(
                id = "rmbg20_fp16",
                label = "FP16",
                summary = "model_fp16.onnx · 490MB · 基线",
                url = RMBG_MODEL_URL_FP16,
            ),
            RmbgModelPreset(
                id = "rmbg20_q4",
                label = "Q4",
                summary = "model_q4.onnx · 350MB",
                url = RMBG_MODEL_URL_Q4,
            ),
            RmbgModelPreset(
                id = "rmbg20_bnb4",
                label = "BNB4",
                summary = "model_bnb4.onnx · 339MB",
                url = RMBG_MODEL_URL_BNB4,
            ),
            RmbgModelPreset(
                id = "rmbg20_q4f16",
                label = "Q4F16",
                summary = "model_q4f16.onnx · 223MB",
                url = RMBG_MODEL_URL_Q4F16,
            ),
            RMBG_MODEL_PRESET_CUSTOM,
        )
        private val RMBG_NORMALIZE_MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)
        private val RMBG_NORMALIZE_STD = floatArrayOf(0.229f, 0.224f, 0.225f)
        private const val LEGACY_DEFAULT_GPT_BASE_URL = "http://192.168.31.179:3002/v1"
        private const val GPT_RESPONSE_MODEL = "gpt-5.4-mini"
        private const val GPT_IMAGE_MODEL = "gpt-image-2"
        private const val GPT_IMAGE_SIZE = "1024x1024"
        private const val GPT_IMAGE_QUALITY = "low"
        private const val GPT_CONNECT_TIMEOUT_MS = 30_000
        private const val GPT_READ_TIMEOUT_MS = 360_000
        private const val DEBUG_HTTP_PORT = 3964
        private const val DEBUG_HTTP_ABSTRACT_NAME = "artplus-debug-http"
        private const val DEBUG_HTTP_READ_TIMEOUT_MS = 4_000
        private const val DEBUG_HTTP_MAX_HEADER_BYTES = 16 * 1024
        private const val DEBUG_HTTP_MAX_BODY_BYTES = 64 * 1024
        private const val DEBUG_HTTP_TOKEN_HEADER = "X-ArtPlus-Debug-Token"
        private const val DEBUG_HTTP_TOKEN_PARAM = "token"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEYSTORE_GPT_KEY_ALIAS = "artplus_gpt_api_key"
        private const val KEYSTORE_CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEYSTORE_GCM_TAG_BITS = 128
        private const val PREVIEW_LIVE_ASSET_DEBOUNCE_MS = 70L
        private const val PREVIEW_OUTPUT_DEBOUNCE_MS = 140L
        private const val PREVIEW_REBUILD_DEBOUNCE_MS = 180L
        private const val DEFAULT_PREVIEW_ICON_SIZE_DP = 70
        private const val MIN_PREVIEW_ICON_SIZE_DP = 42
        private const val MAX_PREVIEW_ICON_SIZE_DP = 96
        private const val PREVIEW_WALLPAPER_SAMPLE_SIZE = 320
        private const val CHOICE_ROW_HORIZONTAL_BLEED_DP = 16
        private const val ICON_CACHE_SIZE = 96
        private const val PRELOAD_ICON_COUNT = 64
        private const val ROOT_UXICONS_DIR = "/data/oplus/uxicons"
        private const val ROOT_SCAN_TIMEOUT_MS = 8_000L
        private const val ARTPLUS_ICON_REFRESH_TIMEOUT_MS = 12_000L
        private const val COLOROS_UX_ICON_CONFIG_KEY = "key_ux_icon_config"
        private const val COLOROS_DEFAULT_ICON_THEME = 2
        private const val COLOROS_INSPIRATION_ICON_THEME = 3
        private const val COLOROS_ARTPLUS_ON = 1
        private const val FOREGROUND_ORIGINAL_BACKUP_NAME = "recfg_original_artplus.png"
        private const val COLOROS_UXICON_THEME_SHIFT = 4
        private const val COLOROS_UXICON_ARTPLUS_SHIFT = 8
        private const val COLOROS_UXICON_THEME_MASK = 0x0fL shl COLOROS_UXICON_THEME_SHIFT
        private const val COLOROS_UXICON_ARTPLUS_MASK = 0x07L shl COLOROS_UXICON_ARTPLUS_SHIFT
        private const val FALLBACK_ARTPLUS_INSPIRATION_UXICON_CONFIG = 2314313028685793584L
        private const val BACK_GESTURE_EDGE_WIDTH_DP = 88
        private const val BACK_GESTURE_COMMIT_DISTANCE_DP = 96
        private const val BACK_GESTURE_COMMIT_PROGRESS = 0.28f
        private const val BACK_GESTURE_PROGRESS_DISTANCE_RATIO = 1.0f
        private const val BACK_GESTURE_PAGE_TRANSLATION_RATIO = 1.0f
        private val appIconCache = object : LruCache<String, Bitmap>(
            ((Runtime.getRuntime().maxMemory() / 1024) / 16).toInt().coerceAtLeast(4 * 1024),
        ) {
            override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount / 1024
        }
        private val SIZE_1X2 = intArrayOf(240, 820)
        private val SIZE_2X1 = intArrayOf(820, 240)
        private val NIGHT_APP_WHITE = AndroidColor.rgb(247, 248, 250)
        private const val NIGHT_VISIBLE_ALPHA_THRESHOLD = 8
        private const val NIGHT_WHITE_LUMA_THRESHOLD = 235
        private const val NIGHT_WHITE_MAX_SATURATION = 0.10
        private const val NIGHT_DARK_LUMA_THRESHOLD = 96
        private const val NIGHT_WHITE_RATIO_THRESHOLD = 0.55
        private const val NIGHT_DARK_RATIO_THRESHOLD = 0.55
        private const val NIGHT_DARK_MAX_WHITE_RATIO = 0.20
        private const val NIGHT_DARK_COLOR_IGNORE_LUMA_THRESHOLD = 112
        private const val NIGHT_COLOR_SATURATION_THRESHOLD = 0.28
        private const val NIGHT_COLOR_RATIO_MAX = 0.06
        private const val NIGHT_DARK_COLOR_RATIO_MAX = 0.16
        private const val NIGHT_COLORED_BACKGROUND_WHITE_RATIO_THRESHOLD = 0.34
        private const val NIGHT_COLORED_BACKGROUND_MIN_SATURATION = 0.18
        private const val NIGHT_COLORED_BACKGROUND_DARK_RATIO_MAX = 0.22
        private const val NIGHT_COLORED_BACKGROUND_COLOR_RATIO_MAX = 0.24
        private const val NIGHT_FLAT_LIGHT_ALPHA_THRESHOLD = 96
        private const val NIGHT_FLAT_LIGHT_MIN_PIXELS = 48
        private const val NIGHT_FLAT_LIGHT_LUMA_THRESHOLD = 220
        private const val NIGHT_FLAT_LIGHT_MAX_SATURATION = 0.16
        private const val NIGHT_FLAT_LIGHT_DARK_RATIO_MAX = 0.10
        private const val NIGHT_FLAT_LIGHT_COLOR_RATIO_MAX = 0.10
        private const val NIGHT_FLAT_LIGHT_RATIO_MIN = 0.72
        private const val NIGHT_FLAT_VERY_LIGHT_RATIO_MIN = 0.36
        private const val NIGHT_FLAT_PALE_RATIO_MIN = 0.88
        private const val NIGHT_FLAT_PALE_VERY_LIGHT_RATIO_MIN = 0.22
        private const val NIGHT_FLAT_LIGHT_SATURATED_THRESHOLD = 0.20
        private const val NIGHT_FLAT_LIGHT_SATURATED_RATIO_MAX = 0.08
        private const val NIGHT_FLAT_LIGHT_LUMA_RANGE_MAX = 36
        private const val NIGHT_BACKGROUND_DARK_LUMA_THRESHOLD = 76
        private const val NIGHT_BACKGROUND_COLORED_LUMA_THRESHOLD = 72
        private const val NIGHT_BACKGROUND_LIGHT_LUMA_THRESHOLD = 210
        private const val NIGHT_BACKGROUND_LIGHT_MAX_SATURATION = 0.16
        private const val NIGHT_DIRECT_WHITE_LUMA_THRESHOLD = 232
        private const val NIGHT_DIRECT_WHITE_MAX_SATURATION = 0.08
        private const val NIGHT_BACKGROUND_WHITE_BLEND = 0.18
        private const val NIGHT_EDGE_WHITE_LUMA_THRESHOLD = 218
        private const val NIGHT_EDGE_WHITE_MAX_SATURATION = 0.18
        private const val NIGHT_SOFT_EDGE_WHITE_LUMA_THRESHOLD = 142
        private const val NIGHT_SOFT_EDGE_WHITE_MAX_SATURATION = 0.22
        private const val NIGHT_EDGE_FEATHER_BLEND = 0.30
        private const val NIGHT_EDGE_SMOOTH_STRENGTH = 0.42
        private const val NIGHT_EDGE_CONTRAST_RADIUS = 2
        private const val NIGHT_EDGE_COLORED_NEIGHBOR_SATURATION = 0.18
        private const val NIGHT_EDGE_DARK_NEIGHBOR_LUMA = 116
        private const val NIGHT_SUPPORT_MIN_LUMA = 24
        private const val NIGHT_SUPPORT_MAX_LUMA = 150
        private const val NIGHT_SUPPORT_MAX_SATURATION = 0.22
        private const val NIGHT_SUPPORT_PRESERVE_SATURATION = 0.18
        private const val NIGHT_SUPPORT_PRESERVE_LUMA = 172
        private const val NIGHT_DEFAULT_BOOST_MAX_BLEND = 0.16
        private const val NIGHT_FILL_BACKGROUND_MAX_BLEND = 0.30
        private const val MONO_ALPHA_MIN = 40
        private const val MONO_ALPHA_MAX = 230
        private const val MONO_ALPHA_GAMMA = 0.85
        private const val MONO_LIGHT_PREVIEW_SCALE = 0.90
        private const val DEFAULT_MONOCHROME_THEME_SCALE = 0.80f
        private const val MIN_MONOCHROME_THEME_SCALE = 0.20f
        private const val MAX_MONOCHROME_THEME_SCALE = 1.50f
        private const val MIN_MONOCHROME_THEME_SCALE_PERCENT = 20
        private const val MAX_MONOCHROME_THEME_SCALE_PERCENT = 150
        private const val MONO_EDGE_ALPHA_DROP_THRESHOLD = 12
        private const val MONO_EDGE_ALPHA_IGNORE_THRESHOLD = 32
        private const val MONO_EDGE_ALPHA_REPAIR_THRESHOLD = 96
        private const val MONO_EDGE_REPAIR_RADIUS = 3
        private const val MONO_TONAL_MIN_VISIBLE_PIXELS = 64
        private const val MONO_TONAL_RANGE_THRESHOLD = 12
        private const val MONO_EDGE_TRIM_FEATHER_SCALE = 0.26
        private const val MONO_NATIVE_MAX_TILE_COVERAGE = 0.70
        private const val MONO_NATIVE_MAX_COVERAGE_EXTRA = 0.18
        private const val MONO_NATIVE_EDGE_LOW_CUT = 6
        private const val MONO_NATIVE_EDGE_HIGH_CUT = 44
        private const val MONO_EDGE_SHARPEN_LOW_CUT = 18
        private const val MONO_EDGE_SHARPEN_HIGH_CUT = 96
        private const val MONO_EDGE_FEATHER_BLEND = 0.58
        private const val MONO_EDGE_SMOOTH_STRENGTH = 0.68
        private const val MONO_EDGE_SMOOTH_RADIUS = 2
        private const val MONO_EDGE_GROW_STRENGTH = 0.42
        private const val MONO_EDGE_POLISH_RADIUS = 1
        private const val MIN_FOREGROUND_SUBJECT_PERCENT = 20
        private const val MAX_FOREGROUND_SUBJECT_PERCENT = 150
        private const val DEFAULT_FOREGROUND_SUBJECT_PERCENT = 100
        private const val LEGACY_FOREGROUND_SUBJECT_PERCENT = 70
        private const val DEFAULT_FOREGROUND_SHADOW_LEVEL = 0
        private const val MIN_FOREGROUND_SHADOW_LEVEL = 0
        private const val MAX_FOREGROUND_SHADOW_LEVEL = 10
        private const val FOREGROUND_SHADOW_MAX_ALPHA = 190
        private const val FOREGROUND_SHADOW_MAX_BLUR = 7.5
        private const val FOREGROUND_SHADOW_MAX_OFFSET_X = 5.0
        private const val FOREGROUND_SHADOW_MAX_OFFSET_Y = 7.0
        private const val FOREGROUND_SHADOW_MAX_SPREAD = 2.0
        private const val DEFAULT_BACKGROUND_SEPARATION_PERCENT = 60
        private const val MIN_BACKGROUND_SEPARATION_PERCENT = 1
        private const val MAX_BACKGROUND_SEPARATION_PERCENT = 100
        private const val DEFAULT_PLATE_REMOVAL_PERCENT = 58
        private const val MIN_PLATE_REMOVAL_PERCENT = 1
        private const val MAX_PLATE_REMOVAL_PERCENT = 100
        private const val DEFAULT_SHADOW_REMOVAL_PERCENT = 60
        private const val MIN_SHADOW_REMOVAL_PERCENT = 1
        private const val MAX_SHADOW_REMOVAL_PERCENT = 100
        private const val DEFAULT_EDGE_POLISH_PERCENT = 60
        private const val MIN_EDGE_POLISH_PERCENT = 1
        private const val MAX_EDGE_POLISH_PERCENT = 100
        private const val DEFAULT_RMBG_ALPHA_STRENGTH_PERCENT = 100
        private const val MIN_RMBG_ALPHA_STRENGTH_PERCENT = 20
        private const val MAX_RMBG_ALPHA_STRENGTH_PERCENT = 220
        private const val DEFAULT_RMBG_EDGE_FEATHER_PERCENT = 0
        private const val MIN_RMBG_EDGE_FEATHER_PERCENT = 0
        private const val MAX_RMBG_EDGE_FEATHER_PERCENT = 100
        private const val DEFAULT_RMBG_EDGE_ADJUST_PERCENT = 50
        private const val MIN_RMBG_EDGE_ADJUST_PERCENT = 0
        private const val MAX_RMBG_EDGE_ADJUST_PERCENT = 100
        private const val DEFAULT_RMBG_WEAK_ALPHA_KEEP_PERCENT = 100
        private const val MIN_RMBG_WEAK_ALPHA_KEEP_PERCENT = 0
        private const val MAX_RMBG_WEAK_ALPHA_KEEP_PERCENT = 100
        private const val DEFAULT_LIQUID_GLASS_RADIUS = 95
        private const val MIN_LIQUID_GLASS_RADIUS = 0
        private const val MAX_LIQUID_GLASS_RADIUS = 240
        private const val DEFAULT_LIQUID_GLASS_OUTER_WIDTH = 2
        private const val MIN_LIQUID_GLASS_OUTER_WIDTH = 0
        private const val MAX_LIQUID_GLASS_OUTER_WIDTH = 70
        private const val DEFAULT_LIQUID_GLASS_TOP_ALPHA = 175
        private const val DEFAULT_LIQUID_GLASS_BOTTOM_ALPHA = 122
        private const val MIN_LIQUID_GLASS_ALPHA = 0
        private const val MAX_LIQUID_GLASS_ALPHA = 255
        private const val DEFAULT_LIQUID_GLASS_BACKGROUND_MIST_ALPHA = 0
        private const val MIN_LIQUID_GLASS_MIST_ALPHA = 0
        private const val MAX_LIQUID_GLASS_MIST_ALPHA = 160
        private const val DEFAULT_LIQUID_GLASS_BOTTOM_DARK_ALPHA = 24
        private const val MIN_LIQUID_GLASS_BOTTOM_DARK_ALPHA = 0
        private const val MAX_LIQUID_GLASS_BOTTOM_DARK_ALPHA = 50
        private const val DEFAULT_LIQUID_GLASS_SUBJECT_SCALE_PERCENT = 100
        private const val MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT = 45
        private const val MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT = 180
        private const val DEFAULT_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH = 0
        private const val DEFAULT_LIQUID_GLASS_SUBJECT_INNER_OUTLINE_WIDTH = 0
        private const val MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH = 0
        private const val MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH = 36
        private const val DEFAULT_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA = 0
        private const val MIN_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA = 0
        private const val MAX_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA = 180
        private const val DEFAULT_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT = 100
        private const val MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT = 0
        private const val MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT = 100
        private const val LEGACY_BACKGROUND_SEPARATION_MIN = 12.0
        private const val LEGACY_BACKGROUND_SEPARATION_MAX = 420.0
        private const val LEGACY_PLATE_REMOVAL_MIN = 0.0
        private const val LEGACY_PLATE_REMOVAL_MAX = 420.0
        private const val LEGACY_SHADOW_REMOVAL_MIN = 0.0
        private const val LEGACY_SHADOW_REMOVAL_MAX = 255.0
        private const val DEFAULT_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT = 68
        private const val MIN_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT = 0
        private const val MAX_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT = 100
        private const val DEFAULT_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT = 8
        private const val MIN_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT = 0
        private const val MAX_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT = 100
        private const val DEFAULT_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT = 34
        private const val MIN_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT = 0
        private const val MAX_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT = 100
        private const val DEFAULT_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT = 45
        private const val MIN_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT = 0
        private const val MAX_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT = 100
        private const val DEFAULT_ADAPTIVE_CENTER_EPSILON_PERCENT = 3
        private const val MIN_ADAPTIVE_CENTER_EPSILON_PERCENT = 0
        private const val MAX_ADAPTIVE_CENTER_EPSILON_PERCENT = 50
        private const val AUTO_COVERAGE_CHANGE_THRESHOLD = 0.012
        private const val ORIGINAL_CLEANUP_MIN_COVERAGE_DROP = 0.025
        private const val ORIGINAL_CLEANUP_MIN_REMAINING_COVERAGE = 0.012
        private const val ORIGINAL_CLEANUP_ALPHA_BOUNDS_THRESHOLD = 64
        private const val ORIGINAL_CLEANUP_MIN_BOUNDS_RATIO = 0.25
        private const val AUTO_EDGE_TOUCH_MARGIN_PX = 1
        private const val AUTO_EDGE_TOUCH_COUNT_LIMIT = 2
        private const val LOCAL_ALPHA_VISIBLE_THRESHOLD = 8
        private const val NORMALIZE_ALPHA_BOUNDS_THRESHOLD = 48
        private const val MONOCHROME_MIN_COVERAGE = 0.004
        private const val ADAPTIVE_DIRECT_MIN_COVERAGE = 0.02
        private const val ADAPTIVE_DIRECT_FULL_PLATE_COVERAGE = 0.72
        private const val ADAPTIVE_DIRECT_MIN_LOST_COVERAGE = 0.18
        private const val ADAPTIVE_DIRECT_PLATE_MIN_LUMA = 220
        private const val ADAPTIVE_DIRECT_PLATE_MAX_SATURATION = 0.16
        private const val ADAPTIVE_DIRECT_PLATE_MIN_RATIO = 0.24
        private const val ADAPTIVE_DIRECT_DETAIL_MAX_LUMA = 112
        private const val ADAPTIVE_DIRECT_DETAIL_MIN_RATIO = 0.01
        private const val ADAPTIVE_DIRECT_PLATE_BACKGROUND_DISTANCE = 36.0
        private const val ADAPTIVE_DIRECT_DETAIL_BACKGROUND_DISTANCE = 42.0
        private const val ADAPTIVE_BACKGROUND_DETAIL_DISTANCE = 52.0
        private const val ADAPTIVE_BACKGROUND_DETAIL_MIN_RATIO = 0.015
        private const val ADAPTIVE_BACKGROUND_DETAIL_MAX_RATIO = 0.55
        private const val ADAPTIVE_CLEAN_CORNER_RATIO = 0.18f
        private const val ADAPTIVE_CLEAN_SOLID_DISTANCE = 24.0
        private const val ADAPTIVE_SUBTRACT_TRANSPARENT_DISTANCE = 10.0
        private const val RMBG_MIN_MANUAL_COVERAGE = 0.02
        private const val RMBG_MAX_MANUAL_COVERAGE = 0.62
        private const val RMBG_MIN_AUTO_COVERAGE = 0.02
        private const val RMBG_MAX_AUTO_COVERAGE = 0.34
        private const val RMBG_EDGE_ADJUST_MAX_RADIUS = 3
        private const val RMBG_EDGE_FEATHER_MIN_ALPHA_DELTA = 12
        private const val RMBG_WEAK_ALPHA_MAX_CUT = 72
        private const val TWO_LAYER_PLATE_BACKGROUND_DISTANCE = 35.0
        private const val TWO_LAYER_SUBJECT_BACKGROUND_DISTANCE = 72.0
        private const val TWO_LAYER_MIN_PLATE_COVERAGE = 0.12
        private const val TWO_LAYER_MAX_PLATE_COVERAGE = 0.62
        private const val TWO_LAYER_SUBJECT_PLATE_DILATE_RADIUS = 7
        private const val TWO_LAYER_MIN_SUBJECT_PIXELS = 20
        private const val TWO_LAYER_MIN_SUBJECT_COVERAGE = 0.008
        private const val TWO_LAYER_MIN_MANUAL_SUBJECT_COVERAGE = 0.010
        private const val TWO_LAYER_MAX_SUBJECT_TO_PLATE_RATIO = 0.45
        private const val TWO_LAYER_MIN_SUBJECT_FILL_RATIO = 0.08
        private const val TWO_LAYER_MAX_SUBJECT_BOUNDS_TO_PLATE_RATIO = 0.70
        private const val TWO_LAYER_SUBJECT_CLOSE_RADIUS = 2
        private const val TWO_LAYER_BACKGROUND_FILL_RADIUS = 1
        private const val COMPOSED_BACKGROUND_SUBJECT_ALPHA_THRESHOLD = 24
        private const val COMPOSED_BACKGROUND_FILL_RADIUS = 2
        private const val TWO_LAYER_EDGE_SMOOTH_STRENGTH = 0.32
        private const val TWO_LAYER_EDGE_SMOOTH_RADIUS = 1
        private const val TWO_LAYER_EDGE_GROW_STRENGTH = 0.22
        private const val TWO_LAYER_DOMINANT_MIN_SATURATION = 0.18
        private const val TWO_LAYER_DOMINANT_MAX_LUMA = 240
        private const val TWO_LAYER_MANUAL_MAX_PLATE_STD = 70.0
        private const val TWO_LAYER_AUTO_MAX_PLATE_STD = 45.0
        private const val TWO_LAYER_AUTO_MIN_PLATE_LUMA = 0.35
        private const val TWO_LAYER_AUTO_MAX_PLATE_COVERAGE = 0.62
        private const val TWO_LAYER_AUTO_MAX_SUBJECT_TO_PLATE_RATIO = 0.35
        private const val EDGE_BAND_RATIO = 0.035f
        private const val PLATE_BORDER_COVERAGE_THRESHOLD = 0.42
        private const val PLATE_MIN_REMOVED_RATIO = 0.05
        private const val PLATE_MIN_SAFE_REMAINING_COVERAGE = 0.01
        private const val PLATE_MIN_SAFE_KEEP_RATIO = 0.18
        private const val PLATE_MIN_SAFE_BOUNDS_RATIO = 0.28
        private const val RESIDUE_MAX_ALPHA = 190
        private const val RESIDUE_BACKGROUND_MIN_SATURATION = 0.18
        private const val RESIDUE_DISTANCE_SCALE = 1.45
        private const val RESIDUE_MIN_DISTANCE = 64.0
        private const val RESIDUE_MAX_DISTANCE = 190.0
        private const val RESIDUE_CONNECTED_MAX_ALPHA = 248
        private const val RESIDUE_CONNECTED_DISTANCE_SCALE = 2.15
        private const val RESIDUE_CONNECTED_MIN_DISTANCE = 96.0
        private const val RESIDUE_CONNECTED_MAX_DISTANCE = 260.0
        private const val RESIDUE_CONNECTED_TRANSPARENT_RADIUS = 2
        private const val PLAIN_ICON_EDGE_BAND_RATIO = 0.06f
        private const val PLAIN_ICON_BACKGROUND_ALPHA_THRESHOLD = 32
        private const val PLAIN_ICON_MIN_BACKGROUND_SAMPLES = 20
        private const val CORNER_MASK_SEED_SIZE = 56
        private const val CORNER_MASK_ZONE_SIZE = 68
        private const val CORNER_MASK_OPAQUE_ALPHA = 250
        private const val CORNER_MASK_BACKGROUND_DISTANCE = 90.0
        private const val CORNER_MASK_SUBJECT_ALPHA = 32
        private const val CORNER_MASK_SUBJECT_BACKGROUND_DISTANCE = 130.0
        private const val CORNER_MASK_WHITE_THRESHOLD = 220
        private const val CORNER_MASK_WHITE_EDGE_ALPHA = 180
        private const val CORNER_MASK_MAX_REMOVED_RATIO = 0.45
        private const val SHADOW_HIGH_ALPHA_THRESHOLD = 160
        private const val SHADOW_MAX_SATURATION_MIN = 0.08
        private const val SHADOW_MAX_SATURATION_MAX = 0.42
        private const val SHADOW_MAX_LUMINANCE_MIN = 120.0
        private const val SHADOW_MAX_LUMINANCE_MAX = 245.0
        private const val SHADOW_MIN_VISIBLE_RATIO_MIN = 0.012
        private const val SHADOW_MIN_VISIBLE_RATIO_MAX = 0.085
        private const val SHADOW_MIN_OFFSET_MIN = 2.0
        private const val SHADOW_MIN_OFFSET_MAX = 16.0
        private const val SHADOW_MIN_DOWN_OFFSET_MIN = -2.0
        private const val SHADOW_MIN_DOWN_OFFSET_MAX = 6.0
        private const val SHADOW_MIN_LUMA_DROP_MIN = 2.0
        private const val SHADOW_MIN_LUMA_DROP_MAX = 18.0
        private const val SHADOW_EDGE_ANTIALIAS_RADIUS = 2
        private const val SHADOW_EDGE_REPAIR_MAX_ALPHA = 96
        private const val SHADOW_PRESERVE_EDGE_RADIUS = 3
        private const val SHADOW_FADE_RADIUS = 13
        private const val FOREGROUND_EDGE_FEATHER_ALPHA_SCALE = 0.18
        private const val FOREGROUND_EDGE_POLISH_RADIUS = 1
        private const val EDGE_POLISH_FOREGROUND_MIN_STRENGTH = 0.12
        private const val EDGE_POLISH_FOREGROUND_MAX_STRENGTH = 0.82
        private const val EDGE_POLISH_MONO_MIN_STRENGTH = 0.16
        private const val EDGE_POLISH_MONO_MAX_STRENGTH = 0.92
        private const val CHROMA_TRANSPARENT_THRESHOLD = 36.0
        private const val CHROMA_OPAQUE_THRESHOLD = 170.0
        private val CHROMA_KEY_CANDIDATES = intArrayOf(
            AndroidColor.rgb(0, 255, 0),
            AndroidColor.rgb(255, 0, 255),
            AndroidColor.rgb(0, 255, 255),
            AndroidColor.rgb(0, 0, 255),
            AndroidColor.rgb(255, 255, 0),
        )
    }
}
