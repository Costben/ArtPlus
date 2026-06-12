package dev.artplus.mobile

import android.Manifest
import android.app.AppOpsManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import android.util.Base64
import dalvik.system.DexClassLoader
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.Socket
import java.net.URL
import java.net.URLDecoder
import java.nio.FloatBuffer
import java.nio.charset.StandardCharsets
import java.util.ArrayDeque
import java.util.UUID
import java.util.zip.ZipInputStream
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.pow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import org.json.JSONArray
import org.json.JSONObject
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import android.graphics.Color as AndroidColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.caverock.androidsvg.SVG

class MainActivity : ComponentActivity() {
    private val apps = mutableStateListOf<AppEntry>()
    private var queryText by mutableStateOf("")
    private var showAllApps by mutableStateOf(false)
    private var selectedPackageName by mutableStateOf<String?>(null)
    private var statusText by mutableStateOf("加载应用列表中...")
    private var packageListPermissionGranted by mutableStateOf(true)
    private var usageAccessGranted by mutableStateOf(false)
    private var outputTreeUri by mutableStateOf<Uri?>(null)
    private var isBusy by mutableStateOf(false)
    private var didRequestAppLoad = false
    private var gptImageMode by mutableStateOf(GptImageMode.Responses)
    private var gptBaseUrl by mutableStateOf("")
    private var gptApiKey by mutableStateOf("")
    private var gptSettingsSaveStatus by mutableStateOf("")
    private var localSeparationMode by mutableStateOf(LocalSeparationMode.Auto)
    private var foregroundSubjectPercent by mutableStateOf(DEFAULT_FOREGROUND_SUBJECT_PERCENT)
    private var monochromeThemeScale by mutableStateOf(DEFAULT_MONOCHROME_THEME_SCALE)
    private var draftMonochromeThemeScaleText by mutableStateOf(formatScale(DEFAULT_MONOCHROME_THEME_SCALE))
    private var showAdvancedSeparationSettings by mutableStateOf(false)
    private var backgroundSeparationPercent by mutableStateOf(DEFAULT_BACKGROUND_SEPARATION_PERCENT)
    private var draftBackgroundSeparationText by mutableStateOf(DEFAULT_BACKGROUND_SEPARATION_PERCENT.toString())
    private var plateRemovalPercent by mutableStateOf(DEFAULT_PLATE_REMOVAL_PERCENT)
    private var draftPlateRemovalText by mutableStateOf(DEFAULT_PLATE_REMOVAL_PERCENT.toString())
    private var shadowRemovalPercent by mutableStateOf(DEFAULT_SHADOW_REMOVAL_PERCENT)
    private var draftShadowRemovalText by mutableStateOf(DEFAULT_SHADOW_REMOVAL_PERCENT.toString())
    private var adaptiveForegroundMode by mutableStateOf(AdaptiveForegroundMode.Auto)
    private var adaptiveDirectMaxCoveragePercent by mutableStateOf(DEFAULT_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT)
    private var adaptiveDirectMaxCoverageIncreasePercent by mutableStateOf(DEFAULT_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT)
    private var adaptiveMaskEdgeCoveragePercent by mutableStateOf(DEFAULT_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT)
    private var adaptiveMaskMinCoveragePercent by mutableStateOf(DEFAULT_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT)
    private var adaptiveCenterEpsilonPercent by mutableStateOf(DEFAULT_ADAPTIVE_CENTER_EPSILON_PERCENT)
    private var originalForegroundCleanupMode by mutableStateOf(OriginalForegroundCleanupMode.Auto)
    private var currentPage by mutableStateOf(AppPage.Home)
    private var generatedFilter by mutableStateOf(GeneratedFilter.All)
    private var generatedPackageNames by mutableStateOf<Set<String>>(emptySet())
    private var isScanningGeneratedPackages by mutableStateOf(false)
    private var generatedScanFailed by mutableStateOf(false)
    private var previewPackageName by mutableStateOf<String?>(null)
    private var previewDirPath by mutableStateOf<String?>(null)
    private var previewVersion by mutableStateOf(0)
    private var activeGenerationSession by mutableStateOf<GenerationSession?>(null)
    private var previewSelections by mutableStateOf(PreviewSelections.default(PreviewChoice.Full))
    private var previewChoiceMode by mutableStateOf<PreviewMode?>(null)
    private var isGeneratingGptCandidate by mutableStateOf(false)
    private var isGeneratingRmbgCandidate by mutableStateOf(false)
    private var isRefreshingArtPlusIcons by mutableStateOf(false)
    private var lastRmbgCandidateError by mutableStateOf<String?>(null)
    private var pendingCustomImageMode by mutableStateOf<PreviewMode?>(null)
    private var debugHttpServer: DebugHttpServer? = null
    private var rmbgRuntime: DynamicRmbgRuntime? = null
    private var rmbgComponentStatus by mutableStateOf("")

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
            pendingCustomImageMode = null
            if (uri == null) {
                statusText = "未选择自定义图片"
                return@registerForActivityResult
            }
            if (mode == null) {
                statusText = "自定义图片槽位已失效"
                return@registerForActivityResult
            }
            importCustomPreviewImage(mode, uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "ArtPlus Mobile"
        loadGptSettings()
        loadLocalSeparationSettings()
        loadImageSettings()
        loadGeneratedPackageCache()
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
        debugHttpServer?.stop()
        debugHttpServer = null
        runCatching { rmbgRuntime?.close() }
        rmbgRuntime = null
        super.onDestroy()
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

    @Composable
    private fun ArtPlusScreen() {
        val pageBackground = if (isSystemInDarkTheme()) {
            MiuixTheme.colorScheme.background
        } else {
            Color(0xFFF7F7F7)
        }
        val selectedApp by remember {
            derivedStateOf { apps.firstOrNull { it.packageName == selectedPackageName } }
        }
        val scopedApps by remember {
            derivedStateOf {
                if (showAllApps) {
                    apps.toList()
                } else {
                    apps.filter { it.launchable }
                }
            }
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
        val backProgress = maxOf(systemBackProgress, dragBackProgress)

        LaunchedEffect(currentPage) {
            systemBackProgress = 0f
            dragBackProgress = 0f
        }

        PredictiveBackHandler(enabled = currentPage != AppPage.Home) { progress ->
            try {
                progress.collect { backEvent ->
                    systemBackProgress = backEvent.progress
                }
                currentPage = AppPage.Home
            } catch (_: CancellationException) {
                systemBackProgress = 0f
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (currentPage != AppPage.Home) {
                            val reveal = backProgress.coerceIn(0f, 1f)
                            val scale = BACK_GESTURE_HOME_REST_SCALE +
                                (1f - BACK_GESTURE_HOME_REST_SCALE) * reveal
                            scaleX = scale
                            scaleY = scale
                            alpha = BACK_GESTURE_HOME_REST_ALPHA +
                                (1f - BACK_GESTURE_HOME_REST_ALPHA) * reveal
                        }
                    },
            ) {
                HomePage(
                    pageBackground = pageBackground,
                    selectedApp = selectedApp,
                    launcherCount = launcherCount,
                    generatedCount = generatedCount,
                )
            }

            AnimatedContent(
                targetState = currentPage,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    val enteringChild = initialState == AppPage.Home && targetState != AppPage.Home
                    val leavingChild = initialState != AppPage.Home && targetState == AppPage.Home
                    val direction = if (targetState.order >= initialState.order) 1 else -1
                    val enter = when {
                        enteringChild -> slideInHorizontally(animationSpec = tween(300)) { fullWidth ->
                            fullWidth
                        } + fadeIn(animationSpec = tween(180))

                        leavingChild -> fadeIn(animationSpec = tween(90))

                        else -> slideInHorizontally(animationSpec = tween(260)) { fullWidth ->
                            fullWidth * direction / 4
                        } + fadeIn(animationSpec = tween(180))
                    }
                    val exit = when {
                        enteringChild -> fadeOut(animationSpec = tween(90))

                        leavingChild -> slideOutHorizontally(animationSpec = tween(260)) { fullWidth ->
                            fullWidth
                        } + fadeOut(animationSpec = tween(160))

                        else -> slideOutHorizontally(animationSpec = tween(240)) { fullWidth ->
                            -fullWidth * direction / 5
                        } + fadeOut(animationSpec = tween(160))
                    }
                    enter.togetherWith(exit)
                },
                label = "ArtPlusChildPage",
            ) { page ->
                if (page != AppPage.Home) {
                    val isActivePage = page == currentPage
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                if (isActivePage && backProgress > 0f) {
                                    val reveal = backProgress.coerceIn(0f, 1f)
                                    translationX = size.width * reveal * BACK_GESTURE_PAGE_TRANSLATION_RATIO
                                    alpha = 1f - reveal * BACK_GESTURE_PAGE_ALPHA_DROP
                                    val scale = 1f - reveal * BACK_GESTURE_PAGE_SCALE_DROP
                                    scaleX = scale
                                    scaleY = scale
                                    shape = RoundedCornerShape((BACK_GESTURE_PAGE_CORNER_DP * reveal).dp)
                                    clip = true
                                }
                            }
                            .pointerInput(page, currentPage, isBusy, edgeBackWidthPx, commitBackDistancePx) {
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
                                            currentPage = AppPage.Home
                                        }
                                        activeEdge = 0
                                        dragDistance = 0f
                                        dragBackProgress = 0f
                                    },
                                )
                            },
                    ) {
                        when (page) {
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
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 18.dp)
                                .size(46.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = !isBusy) { currentPage = AppPage.Settings },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "⚙",
                                style = MiuixTheme.textStyles.title4.copy(fontSize = 28.sp),
                                color = MiuixTheme.colorScheme.onSurface,
                                maxLines = 1,
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
                        if (!packageListPermissionGranted || !usageAccessGranted) {
                            PermissionCard()
                        }
                        StatusCard(
                            selectedApp = selectedApp,
                        )
                        LocalSeparationSettingsCard()
                        GenerationCard(selectedApp)
                        AppPickerSummaryCard(
                            selectedApp = selectedApp,
                            launcherCount = launcherCount,
                            totalCount = apps.size,
                            generatedCount = generatedCount,
                        )
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
                    AppPickerControlsCard(
                        filteredCount = filteredApps.size,
                        totalCount = scopeCount,
                        generatedCount = generatedCount,
                        ungeneratedCount = ungeneratedCount,
                    )
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
                            generated = entry.packageName in generatedPackageNames,
                            onClick = {
                                selectedPackageName = entry.packageName
                                statusText = "已选择: ${entry.label} (${entry.packageName})"
                                currentPage = AppPage.Home
                            },
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
        val liveAssets = remember(session, previewSelections, foregroundSubjectPercent, previewVersion) {
            session?.let {
                previewAssetsForSelections(it, previewSelections).also { previewAssets ->
                    previewAssets.recbg?.prepareToDraw()
                    previewAssets.recfg?.prepareToDraw()
                    previewAssets.recNight?.prepareToDraw()
                    previewAssets.monochromeLight?.prepareToDraw()
                    previewAssets.monochromeDark?.prepareToDraw()
                }
            }
        }
        val displayAssets = liveAssets ?: assets

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
            text = packageName,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (displayAssets == null) {
            Text(
                text = "加载预览中",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
                choiceEnabled = session != null,
                onClick = { previewChoiceMode = PreviewMode.NormalLight },
                modifier = Modifier.weight(1f),
            )
            PreviewTile(
                label = "正常暗色",
                assets = displayAssets,
                mode = PreviewMode.NormalDark,
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
                choiceEnabled = session != null,
                onClick = { previewChoiceMode = PreviewMode.MonochromeLight },
                modifier = Modifier.weight(1f),
            )
            PreviewTile(
                label = "单色暗色",
                assets = displayAssets,
                mode = PreviewMode.MonochromeDark,
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
    private fun PreviewTile(
        label: String,
        assets: PreviewAssets?,
        mode: PreviewMode,
        choiceEnabled: Boolean,
        onClick: () -> Unit,
        modifier: Modifier,
    ) {
        val missingMessage = assets?.missingMessage(mode)
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
                    .height(84.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (missingMessage == null) {
                    GeneratedIconPreview(assets, mode)
                } else {
                    Text(
                        text = missingMessage,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

    @Composable
    private fun GeneratedIconPreview(assets: PreviewAssets?, mode: PreviewMode) {
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
            modifier = Modifier
                .size(72.dp)
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
    private fun PreviewChoiceDialog(mode: PreviewMode, session: GenerationSession) {
        val ruleChoices = listOf(
            PreviewChoice.Original,
            PreviewChoice.TextSafe,
            PreviewChoice.Plate,
            PreviewChoice.Full,
            PreviewChoice.ComponentSubject,
            PreviewChoice.ComponentBackground,
            PreviewChoice.TwoLayer,
        )
        val selectedRule = previewSelections.choiceFor(mode).takeIf { it in ruleChoices }
        var showRuleChoices by remember(mode) { mutableStateOf(true) }
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
                        text = "每个预览槽位可以独立选择算法；主体占比会实时重写当前输出",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SubjectRatioControl()
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RuleCleanupGroupRow(
                            selectedRule = selectedRule,
                            expanded = showRuleChoices,
                            onToggle = { showRuleChoices = !showRuleChoices },
                        )
                        if (showRuleChoices) {
                            ruleChoices.forEach { choice ->
                                PreviewChoiceRow(
                                    mode = mode,
                                    choice = choice,
                                    session = session,
                                )
                            }
                        }
                        listOf(PreviewChoice.Rmbg, PreviewChoice.Gpt, PreviewChoice.Custom).forEach { choice ->
                            PreviewChoiceRow(
                                mode = mode,
                                choice = choice,
                                session = session,
                            )
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

    @Composable
    private fun RuleCleanupGroupRow(
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
                    text = "规则清理",
                    style = MiuixTheme.textStyles.body1,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = selectedRule?.let { "当前使用: ${it.label}" }
                        ?: "原始 / 字标保全 / 去底板 / 全清理 / 底座当主体 / 底座当背景 / 二层",
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
        val selected = previewSelections.choiceFor(mode) == choice
        val candidate = if (choice == PreviewChoice.Custom) {
            session.customCandidates[mode]
        } else {
            session.candidates[choice]
        }
        val gptMissing = choice == PreviewChoice.Gpt && candidate == null
        val rmbgMissing = choice == PreviewChoice.Rmbg && candidate == null
        val customMissing = choice == PreviewChoice.Custom && candidate == null
        val rmbgFailure = if (choice == PreviewChoice.Rmbg) lastRmbgCandidateError else null
        val canGenerateGpt = gptBaseUrl.trim().isNotEmpty() && gptApiKey.trim().isNotEmpty()
        val canGenerateRmbg = rmbgMissing && findRmbgComponent() != null
        val missingLocalCandidate = choice != PreviewChoice.Gpt &&
            choice != PreviewChoice.Custom &&
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
                    } else if (customMissing) {
                        chooseCustomImageForMode(mode)
                    } else {
                        applyPreviewChoice(mode, choice)
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
                if (candidate != null) {
                    CandidateIconPreview(candidate, mode)
                } else {
                    Text(
                        text = when {
                            choice == PreviewChoice.Gpt && isGeneratingGptCandidate -> "..."
                            choice == PreviewChoice.Rmbg && isGeneratingRmbgCandidate -> "..."
                            choice == PreviewChoice.Gpt -> "GPT"
                            choice == PreviewChoice.Custom -> "导入"
                            else -> choice.label
                        },
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
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
                        selected -> "当前使用 · ${choice.summary}"
                        missingCandidate && choice == PreviewChoice.TwoLayer -> "当前图标不符合二层结构"
                        missingCandidate && choice == PreviewChoice.Rmbg -> "未安装 RMBG 组件 ZIP"
                        rmbgMissing && rmbgFailure != null -> rmbgFailure
                        rmbgMissing -> "点击运行 RMBG-2.0；大模型，可能因内存失败"
                        customMissing -> "点击选择 PNG 或 SVG；只应用到当前槽位"
                        missingCandidate -> "当前不可用 · ${choice.summary}"
                        gptMissing && !canGenerateGpt -> "填写 Base URL 和 API key 后可生成"
                        gptMissing -> "点击从 GPT Image 2 生成"
                        else -> choice.summary
                    },
                    style = MiuixTheme.textStyles.footnote1,
                    color = summaryColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            PreviewChoiceActions(
                selected = selected,
                applyEnabled = enabled && choice != PreviewChoice.Custom,
                onApplyAll = { applyPreviewChoiceToAll(choice) },
            )
        }
    }

    @Composable
    private fun PreviewChoiceActions(
        selected: Boolean,
        applyEnabled: Boolean,
        onApplyAll: () -> Unit,
    ) {
        Column(
            modifier = Modifier.width(88.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (selected) {
                SelectionBadge(modifier = Modifier.fillMaxWidth())
            } else {
                Spacer(modifier = Modifier.height(27.dp))
            }
            TextButton(
                text = "全部应用",
                onClick = onApplyAll,
                enabled = applyEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    @Composable
    private fun SelectionBadge(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .height(27.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MiuixTheme.colorScheme.primaryVariant)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "已选",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onPrimaryVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    @Composable
    private fun CandidateIconPreview(candidate: IconCandidate, mode: PreviewMode) {
        val assets = remember(candidate, mode, foregroundSubjectPercent) {
            previewAssetsForCandidate(candidate, mode).also {
                it.recbg?.prepareToDraw()
                it.recfg?.prepareToDraw()
                it.recNight?.prepareToDraw()
                it.monochromeLight?.prepareToDraw()
                it.monochromeDark?.prepareToDraw()
            }
        }
        GeneratedIconPreview(assets, mode)
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
    private fun StatusCard(selectedApp: AppEntry?) {
        val statusLabel = if (isBusy) "运行中" else "就绪"

        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                }
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
    private fun LocalSeparationSettingsCard() {
        SectionCard(title = "背景分离", summary = "生成时自动匹配规则；手动覆盖在预览弹窗里完成") {
            SubjectRatioControl()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "当前: 自动 · 主体 $foregroundSubjectPercent% · 规则可在预览里单独选择或全部应用",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                text = if (showAdvancedSeparationSettings) "收起高级设置" else "展开高级设置",
                onClick = { showAdvancedSeparationSettings = !showAdvancedSeparationSettings },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
            )
            if (showAdvancedSeparationSettings) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "高级设置",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(8.dp))
                AdvancedSeparationSettings()
            }
        }
    }

    @Composable
    private fun LocalSeparationModeControl() {
        val modes = LocalSeparationMode.entries
        SegmentedControl(
            labels = modes.map { it.label },
            selectedIndex = modes.indexOf(localSeparationMode),
            scrollable = true,
            onSelected = { index ->
                updateLocalSeparationMode(modes[index])
            }
        )
    }

    @Composable
    private fun SubjectRatioControl() {
        SettingLine(
            title = "主体占比",
            summary = "复杂游戏图标建议 100%，范围 20% 到 150%",
            value = "$foregroundSubjectPercent%",
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                text = "-10%",
                onClick = { updateForegroundSubjectPercent(foregroundSubjectPercent - 10) },
                enabled = !isBusy && foregroundSubjectPercent > MIN_FOREGROUND_SUBJECT_PERCENT,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                text = "100%",
                onClick = { updateForegroundSubjectPercent(100) },
                enabled = !isBusy,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                text = "+10%",
                onClick = { updateForegroundSubjectPercent(foregroundSubjectPercent + 10) },
                enabled = !isBusy && foregroundSubjectPercent < MAX_FOREGROUND_SUBJECT_PERCENT,
                modifier = Modifier.weight(1f),
            )
        }
    }

    @Composable
    private fun AdvancedSeparationSettings() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            NumberParameterControl(
                title = "背景剔除阈值",
                summary = "实际背景减法不透明距离，默认 $DEFAULT_BACKGROUND_SEPARATION_PERCENT",
                value = backgroundSeparationPercent,
                draftText = draftBackgroundSeparationText,
                min = MIN_BACKGROUND_SEPARATION_PERCENT,
                max = MAX_BACKGROUND_SEPARATION_PERCENT,
                onDraftChange = { draftBackgroundSeparationText = it },
                onSave = { updateBackgroundSeparationPercent(it) },
            )
            NumberParameterControl(
                title = "底板颜色阈值",
                summary = "实际边缘底板颜色距离，默认 $DEFAULT_PLATE_REMOVAL_PERCENT",
                value = plateRemovalPercent,
                draftText = draftPlateRemovalText,
                min = MIN_PLATE_REMOVAL_PERCENT,
                max = MAX_PLATE_REMOVAL_PERCENT,
                onDraftChange = { draftPlateRemovalText = it },
                onSave = { updatePlateRemovalPercent(it) },
            )
            NumberParameterControl(
                title = "长阴影清理强度",
                summary = "实际阴影候选最大 Alpha；仅清理明显拖尾，默认 $DEFAULT_SHADOW_REMOVAL_PERCENT",
                value = shadowRemovalPercent,
                draftText = draftShadowRemovalText,
                min = MIN_SHADOW_REMOVAL_PERCENT,
                max = MAX_SHADOW_REMOVAL_PERCENT,
                onDraftChange = { draftShadowRemovalText = it },
                onSave = { updateShadowRemovalPercent(it) },
            )
            DecimalParameterControl(
                title = "单色缩放",
                summary = "只影响 monochrome 1x1；多格单色保持原始大小，默认 ${formatScale(DEFAULT_MONOCHROME_THEME_SCALE)}",
                value = monochromeThemeScale,
                draftText = draftMonochromeThemeScaleText,
                min = MIN_MONOCHROME_THEME_SCALE,
                max = MAX_MONOCHROME_THEME_SCALE,
                onDraftChange = { draftMonochromeThemeScaleText = it },
                onSave = { updateMonochromeThemeScale(it) },
            )
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
        onDraftChange: (String) -> Unit,
        onSave: (Int) -> Unit,
    ) {
        val parsedValue = draftText.toIntOrNull()?.coerceIn(min, max)
        val canSave = !isBusy && parsedValue != null && parsedValue != value
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NumberInputBox(
                    value = draftText,
                    fallbackValue = value,
                    onValueChange = onDraftChange,
                    onDone = { submitted ->
                        submitted.toIntOrNull()
                            ?.coerceIn(min, max)
                            ?.let(onSave)
                    },
                )
                TextButton(
                    text = "保存",
                    onClick = { parsedValue?.let(onSave) },
                    enabled = canSave,
                    modifier = Modifier.width(58.dp),
                )
            }
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
    ) {
        val parsedValue = draftText.toFloatOrNull()?.coerceIn(min, max)
        val canSave = !isBusy && parsedValue != null && abs(parsedValue - value) > 0.0005f
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DecimalInputBox(
                    value = draftText,
                    fallbackValue = formatScale(value),
                    onValueChange = onDraftChange,
                    onDone = { submitted ->
                        submitted.toFloatOrNull()
                            ?.coerceIn(min, max)
                            ?.let(onSave)
                    },
                )
                TextButton(
                    text = "保存",
                    onClick = { parsedValue?.let(onSave) },
                    enabled = canSave,
                    modifier = Modifier.width(58.dp),
                )
            }
        }
    }

    @Composable
    private fun DecimalInputBox(
        value: String,
        fallbackValue: String,
        onValueChange: (String) -> Unit,
        onDone: (String) -> Unit,
    ) {
        val textColor = MiuixTheme.colorScheme.onSurface.toArgb()
        val cursorColor = MiuixTheme.colorScheme.primaryVariant.toArgb()
        val enabled = !isBusy
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
                        filters = arrayOf(InputFilter.LengthFilter(4))
                        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
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
        onValueChange: (String) -> Unit,
        onDone: (String) -> Unit,
    ) {
        val textColor = MiuixTheme.colorScheme.onSurface.toArgb()
        val cursorColor = MiuixTheme.colorScheme.primaryVariant.toArgb()
        val enabled = !isBusy
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
                TextButton(
                    text = "全部写入",
                    onClick = {
                        generateSelected(
                            installWithRoot = true,
                            useGpt = false,
                            rootWriteMode = RootWriteMode.All,
                        )
                    },
                    enabled = canRun,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = "写入默认",
                    onClick = {
                        generateSelected(
                            installWithRoot = true,
                            useGpt = false,
                            rootWriteMode = RootWriteMode.DefaultOnly,
                        )
                    },
                    enabled = canRun,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = "只写单色",
                    onClick = {
                        generateSelected(
                            installWithRoot = true,
                            useGpt = false,
                            rootWriteMode = RootWriteMode.MonochromeOnly,
                        )
                    },
                    enabled = canRun,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(
                text = if (isRefreshingArtPlusIcons) "正在刷新 ART+ 图标" else "刷新 ART+ 图标",
                onClick = { refreshArtPlusIcons() },
                enabled = !isBusy && !isRefreshingArtPlusIcons,
                modifier = Modifier.fillMaxWidth(),
            )
            GeneratedPreviewSection()
        }
    }

    @Composable
    private fun GptSettingsCard() {
        SectionCard(title = "GPT Image 2", summary = "响应模式走 Codex 能力；接口模式直接调用 Base URL + API key") {
            SegmentedModeControl()
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "当前: ${gptImageMode.label}",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(12.dp))
            InlineInputField(
                value = gptBaseUrl,
                onValueChange = {
                    gptBaseUrl = it
                    gptSettingsSaveStatus = ""
                },
                label = "Base URL",
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
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                text = "保存 GPT 设置",
                onClick = {
                    val message = if (saveGptSettings()) {
                        "GPT 设置已保存"
                    } else {
                        "GPT 设置保存失败"
                    }
                    gptSettingsSaveStatus = if (message.contains("已保存")) "已保存" else "保存失败"
                    statusText = message
                },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
            )
            if (gptSettingsSaveStatus.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = gptSettingsSaveStatus,
                    style = MiuixTheme.textStyles.footnote1,
                    color = if (gptSettingsSaveStatus == "已保存") {
                        MiuixTheme.colorScheme.primaryVariant
                    } else {
                        MiuixTheme.colorScheme.error
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    @Composable
    private fun RmbgComponentCard() {
        val component = remember(rmbgComponentStatus) { findRmbgComponent() }
        SectionCard(
            title = "RMBG 组件",
            summary = "RMBG-2.0 不内置在 APK 中；安装组件 ZIP 后才启用",
        ) {
            SettingLine(
                title = "组件状态",
                summary = component?.let { "ABI ${it.abi} · 模型和 ONNX Runtime 已安装" }
                    ?: "未安装组件；主安装包不包含 RMBG 模型和 ONNX Runtime",
                value = if (component == null) "未安装" else "已安装",
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                text = if (component == null) "安装 RMBG 组件 ZIP" else "重新安装组件 ZIP",
                onClick = {
                    chooseRmbgComponentLauncher.launch(
                        arrayOf("application/zip", "application/octet-stream", "*/*"),
                    )
                },
                enabled = !isBusy && !isGeneratingRmbgCandidate,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    @Composable
    private fun OutputCard() {
        SectionCard(title = "输出与写入", summary = "普通导出使用系统目录选择器，Root 写入只使用 data 分区路径") {
            SettingLine(
                title = "Root 目标",
                summary = "/data/oplus/uxicons/{package}",
                value = "data",
            )
            Spacer(modifier = Modifier.height(10.dp))
            SettingLine(
                title = "外部导出",
                summary = if (outputTreeUri == null) "未选择时仅保存在应用私有目录" else "生成后同步复制到你选择的目录",
                value = if (outputTreeUri == null) "未选择" else "已启用",
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                text = "选择输出目录",
                onClick = { chooseTreeLauncher.launch(null) },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    @Composable
    private fun InputSettingsCard(launcherCount: Int, totalCount: Int, generatedCount: Int) {
        SectionCard(title = "输入", summary = "读取手机已安装应用并按生成状态筛选") {
            SettingLine(
                title = "应用范围",
                summary = "启动器 $launcherCount 个 / 全部 $totalCount 个",
                value = if (showAllApps) "全部" else "启动器",
            )
            Spacer(modifier = Modifier.height(10.dp))
            SettingLine(
                title = "已生成",
                summary = "来自本地缓存；手动刷新后才重新读取 data 路径",
                value = "$generatedCount",
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                text = "刷新应用与生成状态",
                onClick = { loadApps(refreshGenerated = true) },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    @Composable
    private fun AppPickerSummaryCard(
        selectedApp: AppEntry?,
        launcherCount: Int,
        totalCount: Int,
        generatedCount: Int,
    ) {
        SectionCard(
            title = "APK",
            summary = selectedApp?.packageName ?: "从手机已安装应用中选择一个 APK",
        ) {
            if (selectedApp != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppIcon(selectedApp, 40.dp)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = selectedApp.label,
                            style = MiuixTheme.textStyles.body1,
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = selectedApp.packageName,
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    MetricPill(label = "已选")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            SettingLine(
                title = "应用列表",
                summary = "启动器 $launcherCount 个 / 全部 $totalCount 个 / 已生成 $generatedCount 个",
                value = if (apps.isEmpty()) "加载中" else "可选择",
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { currentPage = AppPage.AppPicker },
                enabled = !isBusy && apps.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColorsPrimary(),
            ) {
                Text(
                    text = if (selectedApp == null) "选择 APK" else "更换 APK",
                    style = MiuixTheme.textStyles.button,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    @Composable
    private fun AppPickerControlsCard(
        filteredCount: Int,
        totalCount: Int,
        generatedCount: Int,
        ungeneratedCount: Int,
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            text = "刷新",
                            onClick = { refreshGeneratedPackages() },
                            enabled = !isBusy && apps.isNotEmpty(),
                        )
                        TextButton(
                            text = "返回",
                            onClick = { currentPage = AppPage.Home },
                            enabled = !isBusy,
                        )
                    }
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
                SegmentedControl(
                    labels = listOf("启动器", "全部"),
                    selectedIndex = if (showAllApps) 1 else 0,
                    onSelected = { index ->
                        showAllApps = index == 1
                        queryText = ""
                    }
                )
                val filters = GeneratedFilter.entries
                SegmentedControl(
                    labels = filters.map { it.label },
                    selectedIndex = filters.indexOf(generatedFilter),
                    onSelected = { index ->
                        generatedFilter = filters[index]
                        queryText = ""
                    }
                )
                InlineInputField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    label = "搜索应用或包名",
                )
            }
        }
    }

    @Composable
    private fun AppRow(entry: AppEntry, selected: Boolean, generated: Boolean, onClick: () -> Unit) {
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

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .padding(start = 2.dp)
                    .width(6.dp)
                    .height(24.dp)
                    .align(Alignment.CenterVertically)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (selected) {
                            MiuixTheme.colorScheme.primaryVariant
                        } else if (generated) {
                            MiuixTheme.colorScheme.primaryVariant
                        } else {
                            MiuixTheme.colorScheme.secondaryContainer
                        },
                    ),
            )
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp, bottom = 6.dp),
                insideMargin = PaddingValues(start = 10.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                colors = CardDefaults.defaultColors(
                    color = if (selected) {
                        MiuixTheme.colorScheme.primaryVariant
                    } else {
                        MiuixTheme.colorScheme.surfaceContainer
                    },
                ),
                showIndication = true,
                onClick = onClick,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppIcon(entry, 40.dp)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = entry.label,
                            style = MiuixTheme.textStyles.body1,
                            color = titleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = entry.packageName,
                            style = MiuixTheme.textStyles.footnote1,
                            color = summaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (selected || generated || !entry.launchable) {
                        Text(
                            text = when {
                                selected -> "已选"
                                generated -> "已生成"
                                else -> "应用"
                            },
                            style = MiuixTheme.textStyles.footnote1,
                            color = summaryColor,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SectionCard(title: String, summary: String, content: @Composable () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(16.dp),
        ) {
            Text(
                text = title,
                style = MiuixTheme.textStyles.title4,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = summary,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }

    @Composable
    private fun SegmentedModeControl() {
        val modes = GptImageMode.entries
        SegmentedControl(
            labels = modes.map { it.label },
            selectedIndex = modes.indexOf(gptImageMode),
            onSelected = { index ->
                gptImageMode = modes[index]
                gptSettingsSaveStatus = ""
            }
        )
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
        val minSegmentWidth = if (scrollable) 86.dp else 0.dp
        val minSegmentWidthPx = with(density) { minSegmentWidth.toPx() }
        val segmentWidthPx = if (widthPx == 0) {
            0f
        } else if (scrollable) {
            maxOf(minSegmentWidthPx, widthPx.toFloat() / labels.size.toFloat())
        } else {
            widthPx.toFloat() / labels.size.toFloat()
        }
        val selectedOffsetPx by animateFloatAsState(
            targetValue = segmentWidthPx * safeSelectedIndex,
            animationSpec = tween(durationMillis = 220),
            label = "SegmentedControlOffset",
        )
        val selectedWidth = with(density) { segmentWidthPx.toDp() }
        val contentWidth = with(density) { (segmentWidthPx * labels.size).toDp() }
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(shape)
                .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                .onGloballyPositioned { coordinates ->
                    widthPx = coordinates.size.width
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (scrollable) Modifier.horizontalScroll(scrollState) else Modifier),
            ) {
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
                    modifier = if (scrollable) {
                        Modifier
                            .width(contentWidth)
                            .fillMaxHeight()
                    } else {
                        Modifier.fillMaxSize()
                    },
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
                                .then(if (scrollable) Modifier.width(selectedWidth) else Modifier.weight(1f))
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
            },
        )
    }

    @Composable
    private fun SettingLine(title: String, summary: String, value: String) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
            Spacer(modifier = Modifier.width(12.dp))
            MetricPill(label = value)
        }
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
                .size(size)
                .clip(RoundedCornerShape(12.dp))
                .background(MiuixTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            if (imageBitmap == null) {
                Text(
                    text = entry.label.firstOrNull()?.uppercaseChar()?.toString() ?: "#",
                    style = MiuixTheme.textStyles.title4,
                    color = MiuixTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                )
            } else {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    modifier = Modifier.size(size),
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
        val inspirationConfig = currentConfig
            .withUxIconTheme(COLOROS_INSPIRATION_ICON_THEME)
            .withUxIconArtPlusOn(COLOROS_ARTPLUS_ON)
        val defaultConfig = inspirationConfig.withUxIconTheme(COLOROS_DEFAULT_ICON_THEME)
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
            apply_uxicon_config ${defaultConfig}
            sleep 1
            apply_uxicon_config ${inspirationConfig}
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
                PackageManager.ApplicationInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
            )
        } else {
            pm.getInstalledApplications(PackageManager.MATCH_ALL)
        }

    private fun getApplicationInfoCompat(packageName: String): ApplicationInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.ApplicationInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getApplicationInfo(packageName, PackageManager.MATCH_ALL)
        }

    private fun isDebugGenerateIntent(intent: Intent?): Boolean =
        intent?.getStringExtra(EXTRA_DEBUG_GENERATE_PACKAGE)?.isNotBlank() == true

    private fun handleDebugGenerateIntent(intent: Intent?) {
        if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) == 0) {
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
        startDebugGeneration(
            packageName = debugPackageName,
            useGpt = useGpt,
            installWithRoot = installWithRoot,
            debugMode = debugMode,
        )
    }

    private fun startDebugGeneration(
        packageName: String,
        useGpt: Boolean,
        installWithRoot: Boolean,
        debugMode: LocalSeparationMode,
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
                    installWithRoot(result.outDir, packageName, RootWriteMode.All)
                    runOnMainSync {
                        markPackageGenerated(packageName)
                        statusText = "调试生成完成并写入 Root，未刷新，请手动点刷新 ART+ 图标: ${result.outDir.absolutePath}"
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
            val composed = drawDrawable(icon, renderSize, renderSize, transparent = true)
            val subtracted = subtractBackground(composed, background)
            val direct = drawDrawable(icon.foreground, renderSize, renderSize, transparent = true)
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
                val rmbgResult = buildRmbgCandidate(source240, localSource.recbg)
                val rmbgCandidate = rmbgResult?.candidate
                if (rmbgResult == null || rmbgCandidate == null) {
                    rmbgJson.put("ok", false).put("error", "RMBG候选未通过校验")
                } else {
                    rmbgJson
                        .put("ok", true)
                        .put("auto_usable", rmbgResult.autoUsable)
                        .put("coverage", rmbgResult.coverage)
                    saveLayer("candidate_rmbg_raw", rmbgCandidate.recfgRaw, rmbgJson)
                    val rendered = renderCandidateForeground(rmbgCandidate)
                    saveLayer("candidate_rmbg_rendered", rendered, rmbgJson)
                    saveLayer("candidate_rmbg_night", nightForeground(rendered, rmbgCandidate.recbg), rmbgJson)
                    saveLayer("candidate_rmbg_monochrome_light", monochromeForCandidate(rmbgCandidate, invertLuma = true), rmbgJson)
                    saveLayer("candidate_rmbg_monochrome_dark", monochromeForCandidate(rmbgCandidate, invertLuma = false), rmbgJson)
                }
            } catch (error: Throwable) {
                rmbgJson
                    .put("ok", false)
                    .put("error", describeRmbgFailure(error))
            }
            candidatesJson.put("rmbg", rmbgJson)
        }

        metadata.put("candidates", candidatesJson)
        FileOutputStream(File(outDir, "metadata.json")).use { output ->
            output.write(metadata.toString(2).toByteArray(Charsets.UTF_8))
        }
        return metadata
    }

    private fun loadGptSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        gptImageMode = GptImageMode.fromValue(prefs.getString(PREF_GPT_MODE, GptImageMode.Responses.value))
        val storedBaseUrl = prefs.getString(PREF_GPT_BASE_URL, "") ?: ""
        gptBaseUrl = if (storedBaseUrl == LEGACY_DEFAULT_GPT_BASE_URL) "" else storedBaseUrl
        gptApiKey = prefs.getString(PREF_GPT_API_KEY, "") ?: ""
        if (storedBaseUrl == LEGACY_DEFAULT_GPT_BASE_URL) {
            prefs.edit().putString(PREF_GPT_BASE_URL, "").commit()
        }
    }

    private fun saveGptSettings(): Boolean =
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(PREF_GPT_MODE, gptImageMode.value)
            .putString(PREF_GPT_BASE_URL, gptBaseUrl)
            .putString(PREF_GPT_API_KEY, gptApiKey)
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
        monochromeThemeScale = prefs.getFloat(
            PREF_MONOCHROME_THEME_SCALE,
            DEFAULT_MONOCHROME_THEME_SCALE,
        ).coerceIn(MIN_MONOCHROME_THEME_SCALE, MAX_MONOCHROME_THEME_SCALE)
        draftMonochromeThemeScaleText = formatScale(monochromeThemeScale)
        val tuningVersion = prefs.getInt(PREF_IMAGE_TUNING_VERSION, 1)
        backgroundSeparationPercent = if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
            DEFAULT_BACKGROUND_SEPARATION_PERCENT
        } else {
            prefs.getInt(PREF_BACKGROUND_SEPARATION_PERCENT, DEFAULT_BACKGROUND_SEPARATION_PERCENT)
        }.coerceIn(MIN_BACKGROUND_SEPARATION_PERCENT, MAX_BACKGROUND_SEPARATION_PERCENT)
        draftBackgroundSeparationText = backgroundSeparationPercent.toString()
        plateRemovalPercent = if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
            DEFAULT_PLATE_REMOVAL_PERCENT
        } else {
            prefs.getInt(PREF_PLATE_REMOVAL_PERCENT, DEFAULT_PLATE_REMOVAL_PERCENT)
        }.coerceIn(MIN_PLATE_REMOVAL_PERCENT, MAX_PLATE_REMOVAL_PERCENT)
        draftPlateRemovalText = plateRemovalPercent.toString()
        shadowRemovalPercent = if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
            DEFAULT_SHADOW_REMOVAL_PERCENT
        } else {
            prefs.getInt(PREF_SHADOW_REMOVAL_PERCENT, DEFAULT_SHADOW_REMOVAL_PERCENT)
        }.coerceIn(MIN_SHADOW_REMOVAL_PERCENT, MAX_SHADOW_REMOVAL_PERCENT)
        draftShadowRemovalText = shadowRemovalPercent.toString()
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
        prefs.edit()
            .putInt(PREF_FOREGROUND_SUBJECT_PERCENT, foregroundSubjectPercent)
            .putFloat(PREF_MONOCHROME_THEME_SCALE, monochromeThemeScale)
            .putInt(PREF_BACKGROUND_SEPARATION_PERCENT, backgroundSeparationPercent)
            .putInt(PREF_PLATE_REMOVAL_PERCENT, plateRemovalPercent)
            .putInt(PREF_SHADOW_REMOVAL_PERCENT, shadowRemovalPercent)
            .putString(PREF_ADAPTIVE_FOREGROUND_MODE, adaptiveForegroundMode.value)
            .putInt(PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT, adaptiveDirectMaxCoveragePercent)
            .putInt(PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT, adaptiveDirectMaxCoverageIncreasePercent)
            .putInt(PREF_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT, adaptiveMaskEdgeCoveragePercent)
            .putInt(PREF_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT, adaptiveMaskMinCoveragePercent)
            .putInt(PREF_ADAPTIVE_CENTER_EPSILON_PERCENT, adaptiveCenterEpsilonPercent)
            .putString(PREF_ORIGINAL_FOREGROUND_CLEANUP_MODE, originalForegroundCleanupMode.value)
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

    private fun updateMonochromeThemeScale(value: Float) {
        monochromeThemeScale = value.coerceIn(
            MIN_MONOCHROME_THEME_SCALE,
            MAX_MONOCHROME_THEME_SCALE,
        )
        draftMonochromeThemeScaleText = formatScale(monochromeThemeScale)
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

    private fun saveImageTuningSettings() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putInt(PREF_FOREGROUND_SUBJECT_PERCENT, foregroundSubjectPercent)
            .putFloat(PREF_MONOCHROME_THEME_SCALE, monochromeThemeScale)
            .putInt(PREF_BACKGROUND_SEPARATION_PERCENT, backgroundSeparationPercent)
            .putInt(PREF_PLATE_REMOVAL_PERCENT, plateRemovalPercent)
            .putInt(PREF_SHADOW_REMOVAL_PERCENT, shadowRemovalPercent)
            .putString(PREF_ADAPTIVE_FOREGROUND_MODE, adaptiveForegroundMode.value)
            .putInt(PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT, adaptiveDirectMaxCoveragePercent)
            .putInt(PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT, adaptiveDirectMaxCoverageIncreasePercent)
            .putInt(PREF_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT, adaptiveMaskEdgeCoveragePercent)
            .putInt(PREF_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT, adaptiveMaskMinCoveragePercent)
            .putInt(PREF_ADAPTIVE_CENTER_EPSILON_PERCENT, adaptiveCenterEpsilonPercent)
            .putString(PREF_ORIGINAL_FOREGROUND_CLEANUP_MODE, originalForegroundCleanupMode.value)
            .putInt(PREF_IMAGE_TUNING_VERSION, CURRENT_IMAGE_TUNING_VERSION)
            .apply()
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
        statusText = if (useGpt) {
            "GPT处理中: ${entry.packageName}"
        } else {
            "本地处理中(自动): ${entry.packageName}"
        }
        Thread {
            try {
                val result = generateArtPlusPackage(entry, useGpt)
                runOnUiThread {
                    activeGenerationSession = result.session
                    previewSelections = result.selections
                    previewChoiceMode = null
                    previewPackageName = entry.packageName
                    previewDirPath = result.outDir.absolutePath
                    previewVersion += 1
                }
                if (outputTreeUri != null) {
                    exportToTree(result.outDir)
                }
                if (installWithRoot) {
                    installWithRoot(result.outDir, entry.packageName, rootWriteMode)
                    val refreshSummary = when (rootWriteMode) {
                        RootWriteMode.All -> "，未刷新，请手动点刷新 ART+ 图标"
                        RootWriteMode.DefaultOnly -> runCatching { refreshArtPlusIconsBlocking() }
                            .fold(
                                onSuccess = { "并刷新 ART+ 图标" },
                                onFailure = { "，但刷新失败: ${it.message ?: it.javaClass.simpleName}" },
                            )
                        RootWriteMode.MonochromeOnly -> "，未刷新，请手动点刷新 ART+ 图标"
                    }
                    runOnUiThread {
                        markPackageGenerated(entry.packageName)
                    }
                    val sourceLabel = if (useGpt) "GPT版" else "本地版"
                    status("已生成${sourceLabel}并${rootWriteMode.label}写入$refreshSummary: ${entry.packageName}")
                } else {
                    status("已生成${if (useGpt) "GPT版" else "本地版"}: ${result.outDir.absolutePath}")
                }
            } catch (error: Exception) {
                status("失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                runOnUiThread { isBusy = false }
            }
        }.start()
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
        status("本地分离: ${selectedLocalMode.label}/${defaultChoice.label} · 背景阈值 $backgroundSeparationPercent · 底板阈值 $plateRemovalPercent · 长阴影强度 $shadowRemovalPercent")
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
        val plate = IconCandidate(
            recfgRaw = plateResult.bitmap,
            recbg = localSource.recbg,
            monochromeRaw = localSource.monochrome,
            monochromeIsNative = localSource.monochromeIsNative,
            preserveGeometry = localSource.preserveGeometry,
        )
        val fullResult = separateLocalForeground(localSource.recfg, localSource.recbg, LocalSeparationMode.Full)
        val full = IconCandidate(
            recfgRaw = fullResult.bitmap,
            recbg = localSource.recbg,
            monochromeRaw = localSource.monochrome,
            monochromeIsNative = localSource.monochromeIsNative,
            preserveGeometry = localSource.preserveGeometry,
        )
        val twoLayerResult = buildTwoLayerCandidate(sourceIcon)
        val candidates = linkedMapOf<PreviewChoice, IconCandidate>(
            PreviewChoice.Original to original,
            PreviewChoice.Plate to plate,
            PreviewChoice.Full to full,
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
            plate = plateResult.bitmap,
            full = fullResult.bitmap,
            twoLayer = twoLayerResult,
            rmbg = null,
        )
        return LocalCandidateSet(candidates = candidates, autoChoice = autoChoice)
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

    private fun buildRmbgCandidate(sourceIcon: Bitmap, recbg: Bitmap): CandidateBuildResult? {
        val component = findRmbgComponent() ?: return null
        return runCatching {
            val alpha = runRmbgAlphaMask(sourceIcon, component)
            val foreground = applyAlphaArrayToSource(sourceIcon, alpha)
            val coverage = meaningfulAlphaCoverage(foreground)
            val bounds = meaningfulAlphaBounds(foreground)
            val manualUsable = coverage in RMBG_MIN_MANUAL_COVERAGE..RMBG_MAX_MANUAL_COVERAGE &&
                bounds != null &&
                !hasAutoCropRisk(bounds, foreground.width, foreground.height)
            if (!manualUsable) {
                return@runCatching null
            }
            CandidateBuildResult(
                candidate = IconCandidate(
                    recfgRaw = foreground,
                    recbg = recbg,
                    monochromeRaw = foreground,
                ),
                autoUsable = coverage in RMBG_MIN_AUTO_COVERAGE..RMBG_MAX_AUTO_COVERAGE,
                coverage = coverage,
            )
        }.getOrElse { throw it }
    }

    private fun rmbgComponentDir(): File = File(filesDir, RMBG_COMPONENT_DIR)

    private fun findRmbgComponent(): RmbgComponent? {
        val dir = rmbgComponentDir()
        val abi = Build.SUPPORTED_ABIS.firstOrNull { abi ->
            File(dir, "lib/$abi/$RMBG_ONNXRUNTIME_LIB").isFile &&
                File(dir, "lib/$abi/$RMBG_ONNXRUNTIME_JNI_LIB").isFile
        } ?: return null
        val model = File(dir, RMBG_MODEL_NAME)
        val classesJar = File(dir, RMBG_ONNXRUNTIME_CLASSES_JAR)
        val runtimeLib = File(dir, "lib/$abi/$RMBG_ONNXRUNTIME_LIB")
        val jniLib = File(dir, "lib/$abi/$RMBG_ONNXRUNTIME_JNI_LIB")
        if (!model.isFile || model.length() < RMBG_MIN_MODEL_BYTES || !classesJar.isFile) {
            return null
        }
        classesJar.setWritable(false, false)
        return RmbgComponent(dir, abi, model, classesJar, runtimeLib, jniLib)
    }

    private fun installRmbgComponent(uri: Uri) {
        if (isBusy || isGeneratingRmbgCandidate) {
            return
        }
        statusText = "RMBG 组件安装中..."
        Thread {
            val targetDir = rmbgComponentDir()
            val tmpDir = File(filesDir, "$RMBG_COMPONENT_DIR.tmp")
            try {
                runCatching { rmbgRuntime?.close() }
                rmbgRuntime = null
                if (tmpDir.exists()) {
                    tmpDir.deleteRecursively()
                }
                tmpDir.mkdirs()
                contentResolver.openInputStream(uri)?.use { input ->
                    unzipRmbgComponent(input, tmpDir)
                } ?: error("无法打开组件 ZIP")
                validateRmbgComponentDir(tmpDir)
                File(tmpDir, RMBG_ONNXRUNTIME_CLASSES_JAR).setWritable(false, false)
                if (targetDir.exists()) {
                    targetDir.deleteRecursively()
                }
                if (!tmpDir.renameTo(targetDir)) {
                    copyDirectory(tmpDir, targetDir)
                    tmpDir.deleteRecursively()
                }
                val component = findRmbgComponent()
                    ?: error("组件缺少当前 ABI: ${Build.SUPPORTED_ABIS.joinToString()}")
                runOnUiThread {
                    rmbgComponentStatus = "${System.currentTimeMillis()}"
                    lastRmbgCandidateError = null
                    statusText = "RMBG 组件已安装: ${component.abi}"
                }
            } catch (error: Exception) {
                runOnUiThread {
                    rmbgComponentStatus = "${System.currentTimeMillis()}"
                    lastRmbgCandidateError = "RMBG 组件安装失败: ${error.message ?: error.javaClass.simpleName}"
                    statusText = lastRmbgCandidateError ?: "RMBG 组件安装失败"
                }
                tmpDir.deleteRecursively()
            }
        }.start()
    }

    private fun unzipRmbgComponent(input: InputStream, targetDir: File) {
        ZipInputStream(input).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val entryName = entry.name.replace('\\', '/').trimStart('/')
                if (entryName.isBlank() || entryName.contains("..")) {
                    zip.closeEntry()
                    continue
                }
                val outFile = File(targetDir, entryName)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { output -> zip.copyTo(output) }
                }
                zip.closeEntry()
            }
        }
    }

    private fun validateRmbgComponentDir(dir: File) {
        val model = File(dir, RMBG_MODEL_NAME)
        val classesJar = File(dir, RMBG_ONNXRUNTIME_CLASSES_JAR)
        if (!model.isFile || model.length() < RMBG_MIN_MODEL_BYTES) {
            error("缺少 $RMBG_MODEL_NAME")
        }
        if (!classesJar.isFile) {
            error("缺少 $RMBG_ONNXRUNTIME_CLASSES_JAR")
        }
        val supported = Build.SUPPORTED_ABIS.firstOrNull { abi ->
            File(dir, "lib/$abi/$RMBG_ONNXRUNTIME_LIB").isFile &&
                File(dir, "lib/$abi/$RMBG_ONNXRUNTIME_JNI_LIB").isFile
        }
        if (supported == null) {
            error("缺少当前设备 ABI 的 ONNX Runtime: ${Build.SUPPORTED_ABIS.joinToString()}")
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
        val classesJar: File,
        val runtimeLib: File,
        val jniLib: File,
    ) {
        val key: String = listOf(
            dir.absolutePath,
            abi,
            model.length(),
            model.lastModified(),
            classesJar.length(),
            runtimeLib.length(),
            jniLib.length(),
        ).joinToString("|")
    }

    private class DynamicRmbgRuntime(private val component: RmbgComponent) : AutoCloseable {
        val componentKey: String = component.key

        private val classLoader: ClassLoader
        private val environmentClass: Class<*>
        private val environment: Any
        private val sessionOptions: Any
        private val session: Any
        private val tensorClass: Class<*>
        private val onnxTensorClass: Class<*>
        private val closeMethod = AutoCloseable::class.java.getMethod("close")

        init {
            System.load(component.runtimeLib.absolutePath)
            val optimizedDir = File(component.dir, "dex").also { it.mkdirs() }
            component.classesJar.setWritable(false, false)
            classLoader = DexClassLoader(
                component.classesJar.absolutePath,
                optimizedDir.absolutePath,
                component.jniLib.parentFile?.absolutePath,
                MainActivity::class.java.classLoader,
            )
            environmentClass = classLoader.loadClass("ai.onnxruntime.OrtEnvironment")
            val sessionOptionsClass = classLoader.loadClass("ai.onnxruntime.OrtSession\$SessionOptions")
            onnxTensorClass = classLoader.loadClass("ai.onnxruntime.OnnxTensor")
            tensorClass = onnxTensorClass
            environment = environmentClass.getMethod("getEnvironment").invoke(null)
                ?: error("无法初始化 ONNX Runtime 环境")
            sessionOptions = sessionOptionsClass.getConstructor().newInstance()
            session = environmentClass
                .getMethod("createSession", String::class.java, sessionOptionsClass)
                .invoke(environment, component.model.absolutePath, sessionOptions)
                ?: error("无法创建 RMBG ONNX 会话")
        }

        @Suppress("UNCHECKED_CAST")
        fun run(input: FloatBuffer, shape: LongArray): FloatArray {
            val tensor = tensorClass
                .getMethod("createTensor", environmentClass, FloatBuffer::class.java, LongArray::class.java)
                .invoke(null, environment, input, shape)
            try {
                val inputNames = session.javaClass.getMethod("getInputNames").invoke(session) as Set<String>
                val feeds = mapOf(inputNames.first() to tensor)
                val runMethod = session.javaClass.getMethod("run", Map::class.java)
                val result = runMethod.invoke(session, feeds)
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
            runCatching { closeMethod.invoke(session) }
            runCatching { closeMethod.invoke(sessionOptions) }
        }
    }

    private fun getRmbgRuntime(component: RmbgComponent): DynamicRmbgRuntime =
        synchronized(this) {
            val existing = rmbgRuntime
            if (existing != null && existing.componentKey == component.key) {
                return@synchronized existing
            }
            runCatching { existing?.close() }
            DynamicRmbgRuntime(component).also { runtime ->
                rmbgRuntime = runtime
            }
        }

    private fun runRmbgAlphaMask(sourceIcon: Bitmap, component: RmbgComponent): IntArray {
        val modelInput = resizeBitmap(sourceIcon, RMBG_INPUT_SIZE, RMBG_INPUT_SIZE)
        val inputPixels = IntArray(RMBG_INPUT_SIZE * RMBG_INPUT_SIZE)
        modelInput.getPixels(inputPixels, 0, RMBG_INPUT_SIZE, 0, 0, RMBG_INPUT_SIZE, RMBG_INPUT_SIZE)
        val input = FloatBuffer.allocate(RMBG_INPUT_SIZE * RMBG_INPUT_SIZE * 3)
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

        val output = getRmbgRuntime(component).run(input, longArrayOf(1L, 3L, RMBG_INPUT_SIZE.toLong(), RMBG_INPUT_SIZE.toLong()))
        if (output.isEmpty()) {
            error("RMBG输出为空")
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
        val maskPixels = IntArray(output.size)
        for (i in output.indices) {
            val alpha = (((output[i] - min) / range) * 255.0f)
                .roundToInt()
                .coerceIn(0, 255)
            maskPixels[i] = AndroidColor.rgb(alpha, alpha, alpha)
        }
        val mask = Bitmap.createBitmap(RMBG_INPUT_SIZE, RMBG_INPUT_SIZE, Bitmap.Config.ARGB_8888)
        mask.setPixels(maskPixels, 0, RMBG_INPUT_SIZE, 0, 0, RMBG_INPUT_SIZE, RMBG_INPUT_SIZE)
        val scaledMask = resizeBitmap(mask, sourceIcon.width, sourceIcon.height)
        val scaledPixels = IntArray(sourceIcon.width * sourceIcon.height)
        scaledMask.getPixels(scaledPixels, 0, sourceIcon.width, 0, 0, sourceIcon.width, sourceIcon.height)
        return IntArray(scaledPixels.size) { index -> AndroidColor.red(scaledPixels[index]) }
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
            LocalSeparationMode.Plate -> PreviewChoice.Plate
            LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
            LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
            LocalSeparationMode.Auto -> autoChoice
            LocalSeparationMode.Full -> PreviewChoice.Full
        }

    private fun chooseAutoLocalChoice(
        original: Bitmap,
        plate: Bitmap,
        full: Bitmap,
        twoLayer: CandidateBuildResult?,
        rmbg: CandidateBuildResult?,
    ): PreviewChoice {
        val originalCoverage = meaningfulAlphaCoverage(original)
        val plateCoverage = meaningfulAlphaCoverage(plate)
        val fullCoverage = meaningfulAlphaCoverage(full)
        val plateUsable = isAutoLocalCandidateUsable(
            candidate = plate,
            originalCoverage = originalCoverage,
            candidateCoverage = plateCoverage,
        )
        val fullUsable = isAutoLocalCandidateUsable(
            candidate = full,
            originalCoverage = originalCoverage,
            candidateCoverage = fullCoverage,
        )
        if (twoLayer?.autoUsable == true) {
            return PreviewChoice.TwoLayer
        }
        if (
            rmbg?.autoUsable == true &&
            rmbg.coverage < minOf(plateCoverage, fullCoverage) - AUTO_COVERAGE_CHANGE_THRESHOLD
        ) {
            return PreviewChoice.Rmbg
        }
        return when {
            fullUsable &&
                (!plateUsable || fullCoverage < plateCoverage - AUTO_COVERAGE_CHANGE_THRESHOLD) -> PreviewChoice.Full
            plateUsable -> PreviewChoice.Plate
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
        val light = candidateOrFallback(session, PreviewMode.NormalLight, selections.normalLight)
        val lightFinal = light.customFinalBitmap.takeIf { selections.normalLight == PreviewChoice.Custom }
        val lightRecfg = lightFinal ?: renderCandidateForeground(light)
        val lightRecbg = if (lightFinal == null) {
            light.recbg
        } else {
            solidBitmap(SIZE_1X1, SIZE_1X1, AndroidColor.TRANSPARENT)
        }
        savePng(lightRecbg, File(session.outDir, "recbg.png"))
        savePng(lightRecfg, File(session.outDir, "recfg.png"))
        val recbg1x2 = resizeBitmap(lightRecbg, SIZE_1X2[0], SIZE_1X2[1])
        val recbg2x1 = resizeBitmap(lightRecbg, SIZE_2X1[0], SIZE_2X1[1])
        val recbg2x2 = resizeBitmap(lightRecbg, SIZE_2X2, SIZE_2X2)
        savePng(recbg1x2, File(session.outDir, "recbg_1x2.png"))
        savePng(recbg2x1, File(session.outDir, "recbg_2x1.png"))
        savePng(recbg2x2, File(session.outDir, "recbg_2x2.png"))

        val recfg1x2 = centerOnCanvas(lightRecfg, SIZE_1X2[0], SIZE_1X2[1])
        val recfg2x1 = centerOnCanvas(lightRecfg, SIZE_2X1[0], SIZE_2X1[1])
        val recfg2x2 = centerOnCanvas(lightRecfg, SIZE_2X2, SIZE_2X2)
        savePng(recfg1x2, File(session.outDir, "recfg_1x2.png"))
        savePng(recfg2x1, File(session.outDir, "recfg_2x1.png"))
        savePng(recfg2x2, File(session.outDir, "recfg_2x2.png"))

        val night = candidateOrFallback(session, PreviewMode.NormalDark, selections.normalDark)
        val nightFinal = night.customFinalBitmap.takeIf { selections.normalDark == PreviewChoice.Custom }
        if (nightFinal != null) {
            savePng(nightFinal, File(session.outDir, "rec_night.png"))
            savePng(centerOnCanvas(nightFinal, SIZE_1X2[0], SIZE_1X2[1]), File(session.outDir, "rec_night_1x2.png"))
            savePng(centerOnCanvas(nightFinal, SIZE_2X1[0], SIZE_2X1[1]), File(session.outDir, "rec_night_2x1.png"))
            savePng(centerOnCanvas(nightFinal, SIZE_2X2, SIZE_2X2), File(session.outDir, "rec_night_2x2.png"))
        } else {
            val nightRecfg = renderCandidateForeground(night)
            val nightRecbg = night.recbg
            val nightRecfg1x2 = centerOnCanvas(nightRecfg, SIZE_1X2[0], SIZE_1X2[1])
            val nightRecfg2x1 = centerOnCanvas(nightRecfg, SIZE_2X1[0], SIZE_2X1[1])
            val nightRecfg2x2 = centerOnCanvas(nightRecfg, SIZE_2X2, SIZE_2X2)
            savePng(nightForeground(nightRecfg, nightRecbg), File(session.outDir, "rec_night.png"))
            savePng(
                nightForeground(nightRecfg1x2, resizeBitmap(nightRecbg, SIZE_1X2[0], SIZE_1X2[1])),
                File(session.outDir, "rec_night_1x2.png"),
            )
            savePng(
                nightForeground(nightRecfg2x1, resizeBitmap(nightRecbg, SIZE_2X1[0], SIZE_2X1[1])),
                File(session.outDir, "rec_night_2x1.png"),
            )
            savePng(
                nightForeground(nightRecfg2x2, resizeBitmap(nightRecbg, SIZE_2X2, SIZE_2X2)),
                File(session.outDir, "rec_night_2x2.png"),
            )
        }

        val rawMonochromeLight = monochromeForCandidate(
            candidateOrFallback(session, PreviewMode.MonochromeLight, selections.monochromeLight),
            invertLuma = true,
        )
        val rawMonochromeDark = monochromeForCandidate(
            candidateOrFallback(session, PreviewMode.MonochromeDark, selections.monochromeDark),
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

    private fun candidateOrFallback(
        session: GenerationSession,
        mode: PreviewMode,
        choice: PreviewChoice,
    ): IconCandidate =
        if (choice == PreviewChoice.Custom) {
            session.customCandidates[mode]
        } else {
            session.candidates[choice]
        }
            ?: session.candidates[PreviewChoice.Full]
            ?: session.candidates[PreviewChoice.Plate]
            ?: session.candidates.getValue(PreviewChoice.Original)

    private fun monochromeForCandidate(candidate: IconCandidate, invertLuma: Boolean = false): Bitmap {
        val foreground = renderCandidateForeground(candidate)
        val source = candidate.monochromeRaw?.let { renderCandidateBitmap(candidate, it) }
        val monochrome = when {
            source != null && candidate.monochromeIsNative -> {
                cleanNativeMonochrome(source)
            }
            source == null || hasForegroundTonalRange(foreground) -> {
                monochromeAlpha(foreground, invertLuma = invertLuma)
            }
            else -> {
                monochromeAlphaFromMask(source)
            }
        }
        return if (candidate.monochromeIsNative) {
            sharpenMonochromeAlpha(monochrome, nativeSource = true)
        } else {
            trimMonochromeEdge(monochrome)
        }
    }

    private fun scaleMonochromeForTheme(source: Bitmap): Bitmap =
        scaleBitmapAroundAlphaCenter(source, monochromeThemeScale)

    private fun filterDecimalInput(value: String): String {
        val builder = StringBuilder()
        var hasDot = false
        value.forEach { char ->
            when {
                char.isDigit() -> builder.append(char)
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
        }.take(4)
    }

    private fun formatScale(value: Float): String =
        String.format(Locale.US, "%.2f", value)

    private fun previewAssetsForSelections(
        session: GenerationSession,
        selections: PreviewSelections,
    ): PreviewAssets {
        val light = candidateOrFallback(session, PreviewMode.NormalLight, selections.normalLight)
        val lightFinal = light.customFinalBitmap.takeIf { selections.normalLight == PreviewChoice.Custom }
        val lightRecfg = lightFinal ?: renderCandidateForeground(light)
        val lightRecbg = if (lightFinal == null) {
            light.recbg
        } else {
            solidBitmap(SIZE_1X1, SIZE_1X1, AndroidColor.TRANSPARENT)
        }

        val night = candidateOrFallback(session, PreviewMode.NormalDark, selections.normalDark)
        val nightFinal = night.customFinalBitmap.takeIf { selections.normalDark == PreviewChoice.Custom }
        val nightPreview = nightFinal ?: run {
            val nightRecfg = renderCandidateForeground(night)
            nightForeground(nightRecfg, night.recbg)
        }

        val monochromeLight = monochromeForCandidate(
            candidateOrFallback(session, PreviewMode.MonochromeLight, selections.monochromeLight),
            invertLuma = true,
        )
        val monochromeDark = monochromeForCandidate(
            candidateOrFallback(session, PreviewMode.MonochromeDark, selections.monochromeDark),
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
        return PreviewAssets(
            recbg = candidate.recbg,
            recfg = recfg,
            recNight = nightForeground(recfg, candidate.recbg),
            monochromeLight = monochromeForCandidate(candidate, invertLuma = true),
            monochromeDark = monochromeForCandidate(candidate, invertLuma = false),
        )
    }

    private fun renderCandidateForeground(candidate: IconCandidate): Bitmap =
        renderCandidateBitmap(candidate, candidate.recfgRaw)

    private fun renderCandidateBitmap(candidate: IconCandidate, bitmap: Bitmap): Bitmap =
        if (candidate.preserveGeometry) bitmap else normalizeForegroundSubjectSize(bitmap)

    private fun applyPreviewChoice(mode: PreviewMode, choice: PreviewChoice) {
        val session = activeGenerationSession ?: return
        if (choice == PreviewChoice.Gpt && session.candidates[PreviewChoice.Gpt] == null) {
            generateGptCandidateForMode(mode)
            return
        }
        val selections = previewSelections.withChoice(mode, choice)
        previewSelections = selections
        writeActivePreviewOutputs(session, selections, closeDialog = false)
    }

    private fun applyPreviewChoiceToAll(choice: PreviewChoice) {
        val session = activeGenerationSession ?: return
        if (choice == PreviewChoice.Gpt && session.candidates[PreviewChoice.Gpt] == null) {
            generateGptCandidateForAll()
            return
        }
        if (choice == PreviewChoice.Rmbg && session.candidates[PreviewChoice.Rmbg] == null) {
            generateRmbgCandidateForAll()
            return
        }
        if (choice == PreviewChoice.Custom) {
            statusText = "自定义图片需要逐个槽位上传"
            return
        }
        if (session.candidates[choice] == null) {
            statusText = "${choice.label} 当前不可用"
            return
        }
        val selections = PreviewSelections.default(choice)
        previewSelections = selections
        previewChoiceMode = null
        writeActivePreviewOutputs(session, selections, closeDialog = true)
    }

    private fun chooseCustomImageForMode(mode: PreviewMode) {
        if (isBusy || isGeneratingGptCandidate || isGeneratingRmbgCandidate) {
            return
        }
        pendingCustomImageMode = mode
        chooseCustomImageLauncher.launch(
            arrayOf(
                "image/png",
                "image/svg+xml",
            ),
        )
    }

    private fun importCustomPreviewImage(mode: PreviewMode, uri: Uri) {
        val session = activeGenerationSession
        if (session == null) {
            statusText = "先生成一次预览后再导入自定义图片"
            return
        }
        statusText = "导入自定义图片: ${mode.label}"
        Thread {
            try {
                val bitmap = loadCustomImageBitmap(uri)
                val candidate = customCandidateForMode(mode, bitmap, session)
                val updatedSession = session.copy(
                    customCandidates = session.customCandidates + (mode to candidate),
                )
                val selections = previewSelections.withChoice(mode, PreviewChoice.Custom)
                writePackageOutputs(updatedSession, selections)
                if (outputTreeUri != null) {
                    exportToTree(updatedSession.outDir)
                }
                runOnUiThread {
                    activeGenerationSession = updatedSession
                    previewSelections = selections
                    previewVersion += 1
                    statusText = "已导入自定义图片并应用到 ${mode.label}"
                }
            } catch (error: Exception) {
                status("自定义图片导入失败: ${error.message ?: error.javaClass.simpleName}")
            }
        }.start()
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
        statusText = "GPT候选生成中: ${session.packageName}"
        val selections = previewSelections.withChoice(mode, PreviewChoice.Gpt)
        Thread {
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
                }
            } catch (error: Exception) {
                status("GPT候选失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                runOnUiThread { isGeneratingGptCandidate = false }
            }
        }.start()
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
        statusText = "GPT候选生成中: ${session.packageName}"
        val selections = PreviewSelections.default(PreviewChoice.Gpt)
        Thread {
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
                }
            } catch (error: Exception) {
                status("GPT候选失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                runOnUiThread { isGeneratingGptCandidate = false }
            }
        }.start()
    }

    private fun generateRmbgCandidateForMode(mode: PreviewMode) {
        val session = activeGenerationSession ?: return
        if (findRmbgComponent() == null) {
            lastRmbgCandidateError = "未安装 RMBG 组件 ZIP"
            statusText = lastRmbgCandidateError ?: "未安装 RMBG 组件"
            return
        }
        if (isGeneratingRmbgCandidate || isGeneratingGptCandidate || isBusy) {
            return
        }
        isGeneratingRmbgCandidate = true
        lastRmbgCandidateError = null
        statusText = "RMBG候选生成中: ${session.packageName}"
        val selections = previewSelections.withChoice(mode, PreviewChoice.Rmbg)
        Thread {
            try {
                val source = resizeBitmap(session.sourceIcon, SIZE_1X1, SIZE_1X1)
                val result = buildRmbgCandidate(source, session.baseRecbg)
                    ?: error("RMBG候选未通过校验")
                val candidate = result.candidate ?: error("RMBG候选为空")
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
                    statusText = "RMBG候选已生成并应用到 ${mode.label}"
                }
            } catch (error: Throwable) {
                val message = describeRmbgFailure(error)
                runOnUiThread {
                    lastRmbgCandidateError = message
                    statusText = "RMBG候选失败: $message"
                }
            } finally {
                runOnUiThread { isGeneratingRmbgCandidate = false }
            }
        }.start()
    }

    private fun generateRmbgCandidateForAll() {
        val session = activeGenerationSession ?: return
        if (findRmbgComponent() == null) {
            lastRmbgCandidateError = "未安装 RMBG 组件 ZIP"
            statusText = lastRmbgCandidateError ?: "未安装 RMBG 组件"
            return
        }
        if (isGeneratingRmbgCandidate || isGeneratingGptCandidate || isBusy) {
            return
        }
        isGeneratingRmbgCandidate = true
        lastRmbgCandidateError = null
        statusText = "RMBG候选生成中: ${session.packageName}"
        val selections = PreviewSelections.default(PreviewChoice.Rmbg)
        Thread {
            try {
                val source = resizeBitmap(session.sourceIcon, SIZE_1X1, SIZE_1X1)
                val result = buildRmbgCandidate(source, session.baseRecbg)
                    ?: error("RMBG候选未通过校验")
                val candidate = result.candidate ?: error("RMBG候选为空")
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
                    statusText = "RMBG候选已生成并应用到全部"
                }
            } catch (error: Throwable) {
                val message = describeRmbgFailure(error)
                runOnUiThread {
                    lastRmbgCandidateError = message
                    statusText = "RMBG候选失败: $message"
                }
            } finally {
                runOnUiThread { isGeneratingRmbgCandidate = false }
            }
        }.start()
    }

    private fun describeRmbgFailure(error: Throwable): String {
        val raw = error.message ?: error.javaClass.simpleName
        val lower = raw.lowercase()
        return when {
            error is OutOfMemoryError ||
                "outofmemory" in lower ||
                "failed to allocate" in lower ||
                "memory" in lower -> {
                "内存不足或 ONNX 分配失败；RMBG-2.0 需要 1024 输入"
            }
            "未通过校验" in raw -> {
                "RMBG 已运行，但结果未通过覆盖率/边界校验"
            }
            "reshape" in lower || "shape" in lower -> {
                "模型输入尺寸不匹配；RMBG-2.0 当前必须使用 1024 输入"
            }
            else -> raw
        }
    }

    private fun refreshActivePreviewOutputs(
        rebuildLocalCandidates: Boolean,
        retargetFrom: PreviewChoice? = null,
    ) {
        val session = activeGenerationSession ?: return
        val app = if (rebuildLocalCandidates) {
            apps.firstOrNull { it.packageName == session.packageName }
        } else {
            null
        }
        val updatedSession = if (app == null) {
            session
        } else {
            rebuildLocalSession(session, app)
        }
        val previousDefault = retargetFrom
            ?: if (rebuildLocalCandidates) defaultLocalPreviewChoice(session.autoLocalChoice) else null
        val nextDefault = defaultLocalPreviewChoice(updatedSession.autoLocalChoice)
        val selections = if (previousDefault == null) {
            previewSelections
        } else {
            previewSelections.retarget(previousDefault, nextDefault)
        }
        activeGenerationSession = updatedSession
        writeActivePreviewOutputs(updatedSession, selections, closeDialog = false)
    }

    private fun rebuildLocalSession(session: GenerationSession, app: AppEntry): GenerationSession {
        val icon = app.applicationInfo.loadIcon(packageManager)
        val localSourceIcon = drawLocalCandidateSourceIcon(icon, SIZE_1X1, SIZE_1X1)
        val gptSourceIcon = drawDrawable(icon, GPT_SOURCE_SIZE, GPT_SOURCE_SIZE, transparent = false)
        val localSource = buildLocalIconLayers(icon)
        val localCandidateSet = buildLocalCandidates(localSource, localSourceIcon)
        val localCandidates = localCandidateSet.candidates
        val gptCandidate = session.candidates[PreviewChoice.Gpt]
        val candidates = if (gptCandidate == null) {
            localCandidates
        } else {
            localCandidates + (PreviewChoice.Gpt to gptCandidate)
        }
        return session.copy(
            sourceIcon = gptSourceIcon,
            baseRecfg = localSource.recfg,
            baseRecbg = localSource.recbg,
            monochromeRaw = localSource.monochrome,
            candidates = candidates,
            customCandidates = session.customCandidates,
            autoLocalChoice = localCandidateSet.autoChoice,
        )
    }

    private fun writeActivePreviewOutputs(
        session: GenerationSession,
        selections: PreviewSelections,
        closeDialog: Boolean,
    ) {
        Thread {
            try {
                writePackageOutputs(session, selections)
                if (outputTreeUri != null) {
                    exportToTree(session.outDir)
                }
                runOnUiThread {
                    activeGenerationSession = session
                    previewSelections = selections
                    previewVersion += 1
                    if (closeDialog) {
                        previewChoiceMode = null
                    }
                }
            } catch (error: Exception) {
                status("预览刷新失败: ${error.message ?: error.javaClass.simpleName}")
            }
        }.start()
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
        val connection = (URL(urlText).openConnection() as HttpURLConnection).apply {
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
        val connection = (URL(urlText).openConnection() as HttpURLConnection).apply {
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

    private fun customCandidateForMode(
        mode: PreviewMode,
        bitmap: Bitmap,
        session: GenerationSession,
    ): IconCandidate =
        when (mode) {
            PreviewMode.NormalLight -> IconCandidate(
                recfgRaw = bitmap,
                recbg = session.baseRecbg,
                monochromeRaw = null,
                preserveGeometry = true,
                customFinalBitmap = bitmap,
            )
            PreviewMode.NormalDark -> IconCandidate(
                recfgRaw = bitmap,
                recbg = session.baseRecbg,
                monochromeRaw = null,
                preserveGeometry = true,
                customFinalBitmap = bitmap,
            )
            PreviewMode.MonochromeLight,
            PreviewMode.MonochromeDark -> IconCandidate(
                recfgRaw = bitmap,
                recbg = session.baseRecbg,
                monochromeRaw = bitmap,
                preserveGeometry = true,
            )
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

    private fun buildTransparentForegroundPrompt(): String =
        "Keep only the app icon main subject/logo. Remove the original background. " +
            "Return the remaining subject/logo on a transparent background. " +
            "Do not add any new circle, glow, outline, shadow, halo, plate, or filled backdrop behind the subject. " +
            "Scale the subject/logo so its visible bounding box is about $foregroundSubjectPercent% of the final square canvas. " +
            "Preserve the subject shape, position, colors, face details, highlights, and internal shading."

    private fun buildChromaForegroundPrompt(chromaHex: String): String =
        "Keep only the app icon main subject/logo. Remove the original background. " +
            "Scale the subject/logo so its visible bounding box is about $foregroundSubjectPercent% of the final square canvas. " +
            "Place the remaining subject on a perfectly flat solid $chromaHex chroma-key background. " +
            "The chroma-key background must be one uniform color, with no checkerboard, no transparency preview pattern, " +
            "no shadows, no gradients, no texture, and no lighting variation. " +
            "Do not use $chromaHex anywhere in the subject/logo. Preserve the subject shape and colors."

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
            val composed = drawDrawable(icon, renderSize, renderSize, transparent = true)
            val foreground = subtractBackground(composed, background)
            val directForeground = drawDrawable(icon.foreground, renderSize, renderSize, transparent = true)
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
        val opaqueDistance = backgroundSeparationPercent
            .toDouble()
            .coerceIn(MIN_BACKGROUND_SEPARATION_PERCENT.toDouble(), MAX_BACKGROUND_SEPARATION_PERCENT.toDouble())

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
                AndroidColor.argb(
                    outAlpha,
                    AndroidColor.red(pixel),
                    AndroidColor.green(pixel),
                    AndroidColor.blue(pixel),
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

    private fun subtractBackground(composed: Bitmap, background: Bitmap): Bitmap {
        val width = composed.width
        val height = composed.height
        val composedPixels = IntArray(width * height)
        val backgroundPixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        composed.getPixels(composedPixels, 0, width, 0, 0, width, height)
        background.getPixels(backgroundPixels, 0, width, 0, 0, width, height)
        val transparentDistance = ADAPTIVE_SUBTRACT_TRANSPARENT_DISTANCE
        val opaqueDistance = backgroundSeparationPercent
            .toDouble()
            .coerceIn(MIN_BACKGROUND_SEPARATION_PERCENT.toDouble(), MAX_BACKGROUND_SEPARATION_PERCENT.toDouble())

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
            outPixels[i] = AndroidColor.argb(
                (composedAlpha * alpha).toInt().coerceIn(0, 255),
                AndroidColor.red(composedPixels[i]),
                AndroidColor.green(composedPixels[i]),
                AndroidColor.blue(composedPixels[i]),
            )
        }

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return repairTransparentEdgeColors(out)
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
            mode == LocalSeparationMode.ComponentSubject ||
            mode == LocalSeparationMode.ComponentBackground
        ) {
            return LocalSeparationResult(source, "${mode.label}: 不清理")
        }

        var current = source
        val actions = mutableListOf<String>()

        if (mode == LocalSeparationMode.Auto || mode == LocalSeparationMode.Plate || mode == LocalSeparationMode.Full) {
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

        if (mode == LocalSeparationMode.Auto || mode == LocalSeparationMode.Full) {
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
            LocalSeparationMode.Plate -> "去底板"
            LocalSeparationMode.ComponentSubject -> "组件主体"
            LocalSeparationMode.ComponentBackground -> "组件背景"
            LocalSeparationMode.Full -> "全清理"
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
        val colorDistanceThreshold = plateRemovalPercent
            .toDouble()
            .coerceIn(MIN_PLATE_REMOVAL_PERCENT.toDouble(), MAX_PLATE_REMOVAL_PERCENT.toDouble())
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
        return ForegroundCleanupResult(
            bitmap = removeBackgroundColoredResidue(out, edge.color),
            changed = true,
            removedRatio = removedRatio,
            repairedRatio = repaired.toDouble() / visible.toDouble(),
        )
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
            val pixel = pixels[i]
            val alpha = AndroidColor.alpha(pixel)
            if (alpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
                continue
            }
            candidate[i] =
                alpha < CORNER_MASK_OPAQUE_ALPHA ||
                colorDistance(pixel, backgroundPixels[i]) <= CORNER_MASK_BACKGROUND_DISTANCE ||
                (removeNearWhite && isNearWhite(pixel))
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
        (plateRemovalPercent * RESIDUE_DISTANCE_SCALE)
            .coerceIn(RESIDUE_MIN_DISTANCE, RESIDUE_MAX_DISTANCE)

    private fun edgeConnectedResidueDistanceThreshold(): Double =
        (plateRemovalPercent * RESIDUE_CONNECTED_DISTANCE_SCALE)
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
        val alphaMax = shadowRemovalPercent.coerceIn(MIN_SHADOW_REMOVAL_PERCENT, MAX_SHADOW_REMOVAL_PERCENT)
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
                    saturation(pixel) <= SHADOW_MAX_SATURATION &&
                    luma(pixel) <= SHADOW_MAX_LUMINANCE
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
            if (componentRatio < SHADOW_MIN_VISIBLE_RATIO) {
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
                offset >= SHADOW_MIN_OFFSET &&
                dy >= SHADOW_MIN_DOWN_OFFSET &&
                medianDrop >= SHADOW_MIN_LUMA_DROP
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
            selectedCount++
            val x = i % width
            val y = i / width
            val distance = distanceToNearbyMaskPixel(
                mask = highAlpha,
                width = width,
                height = height,
                x = x,
                y = y,
                maxRadius = SHADOW_PRESERVE_EDGE_RADIUS + SHADOW_FADE_RADIUS,
            ) ?: (SHADOW_PRESERVE_EDGE_RADIUS + SHADOW_FADE_RADIUS + 1).toDouble()
            val fade = ((distance - SHADOW_PRESERVE_EDGE_RADIUS) / SHADOW_FADE_RADIUS)
                .coerceIn(0.0, 1.0)
            val alphaScale = (1.0 - fade).coerceIn(0.0, 1.0)
            val pixel = cleaned[i]
            val alpha = (AndroidColor.alpha(pixel) * alphaScale).toInt().coerceIn(0, 255)
            cleaned[i] = if (alpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
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

    private fun nightForeground(source: Bitmap, background: Bitmap): Bitmap {
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
            if (pixelSaturation >= NIGHT_COLOR_SATURATION_THRESHOLD) {
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
        return sharpenMonochromeAlpha(out)
    }

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
                        return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                    }
                }
                null
            }
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
        if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) == 0) {
            return
        }
        if (debugHttpServer != null) {
            return
        }
        debugHttpServer = DebugHttpServer(DEBUG_HTTP_PORT).also { it.start() }
    }

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
            .put("monochrome_theme_scale", monochromeThemeScale.toDouble())
            .put("background_separation_percent", backgroundSeparationPercent)
            .put("plate_removal_percent", plateRemovalPercent)
            .put("shadow_removal_percent", shadowRemovalPercent)
            .put("rmbg_model_installed", findRmbgComponent() != null)
            .put("rmbg_component_installed", findRmbgComponent() != null)
            .put("rmbg_component_abi", findRmbgComponent()?.abi ?: "")
            .put("rmbg_model_name", RMBG_MODEL_NAME)
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
                    .put(
                        "monochrome_theme_scale",
                        JSONObject()
                            .put("min", MIN_MONOCHROME_THEME_SCALE.toDouble())
                            .put("max", MAX_MONOCHROME_THEME_SCALE.toDouble()),
                    )
                    .put("background_separation_percent", intRangeJson(MIN_BACKGROUND_SEPARATION_PERCENT, MAX_BACKGROUND_SEPARATION_PERCENT))
                    .put("plate_removal_percent", intRangeJson(MIN_PLATE_REMOVAL_PERCENT, MAX_PLATE_REMOVAL_PERCENT))
                    .put("shadow_removal_percent", intRangeJson(MIN_SHADOW_REMOVAL_PERCENT, MAX_SHADOW_REMOVAL_PERCENT))
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
            params["foreground_subject_percent"]?.toIntOrNull()?.let {
                foregroundSubjectPercent = it.coerceIn(MIN_FOREGROUND_SUBJECT_PERCENT, MAX_FOREGROUND_SUBJECT_PERCENT)
            }
            params["monochrome_theme_scale"]?.toFloatOrNull()?.let {
                monochromeThemeScale = it.coerceIn(MIN_MONOCHROME_THEME_SCALE, MAX_MONOCHROME_THEME_SCALE)
                draftMonochromeThemeScaleText = formatScale(monochromeThemeScale)
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
          <button id="generate" type="button">Generate</button>
          <h2>Status</h2>
          <pre id="out"></pre>
        </main>
        <script>
        const numericKeys = [
          'foreground_subject_percent','background_separation_percent','plate_removal_percent','shadow_removal_percent',
          'adaptive_direct_max_coverage_percent','adaptive_direct_max_coverage_increase_percent',
          'adaptive_mask_edge_coverage_percent','adaptive_mask_min_coverage_percent','adaptive_center_epsilon_percent'
        ];
        async function load(){
          const data = await fetch('/debug/params').then(r=>r.json());
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
          numericKeys.forEach(k => {
            const input = document.createElement('input');
            input.type = 'number'; input.name = k; input.value = data[k];
            if (data.ranges[k]) { input.min = data.ranges[k].min; input.max = data.ranges[k].max; }
            form.appendChild(row(k, input));
          });
          document.getElementById('out').textContent = JSON.stringify(data, null, 2);
        }
        function row(label, input){ const l=document.createElement('label'); const s=document.createElement('span'); s.textContent=label; l.appendChild(s); l.appendChild(input); return l; }
        document.getElementById('save').onclick = async () => {
          const body = {};
          new FormData(document.getElementById('params')).forEach((v,k)=>body[k]=v);
          document.getElementById('out').textContent = await fetch('/debug/params',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)}).then(r=>r.text());
          load();
        };
        document.getElementById('generate').onclick = async () => {
          const pkg = encodeURIComponent(document.getElementById('packageName').value);
          const mode = encodeURIComponent(document.getElementById('mode').value);
          document.getElementById('out').textContent = await fetch('/debug/generate?package='+pkg+'&mode='+mode,{method:'POST'}).then(r=>r.text());
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
                    ServerSocket(port).use { server ->
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
            writeResponse(output, route(request.method, request.target, request.body))
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

        private fun route(method: String, target: String, body: String): DebugHttpResponse {
            val path = target.substringBefore('?')
            val query = parseQuery(target.substringAfter('?', ""))
            return try {
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
                            )
                            val snapshot = currentDebugParamsOnMain()
                            jsonResponse(
                                JSONObject()
                                    .put("ok", accepted)
                                    .put("package", packageName)
                                    .put("mode", mode.value)
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
        val preserveGeometry: Boolean = false,
        val customFinalBitmap: Bitmap? = null,
    )

    private data class GenerationSession(
        val packageName: String,
        val outDir: File,
        val sourceIcon: Bitmap,
        val baseRecfg: Bitmap,
        val baseRecbg: Bitmap,
        val monochromeRaw: Bitmap?,
        val candidates: Map<PreviewChoice, IconCandidate>,
        val customCandidates: Map<PreviewMode, IconCandidate> = emptyMap(),
        val autoLocalChoice: PreviewChoice,
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

    private enum class PreviewMode(val label: String) {
        NormalLight("正常亮色"),
        NormalDark("正常暗色"),
        MonochromeLight("单色亮色"),
        MonochromeDark("单色暗色"),
    }

    private enum class PreviewChoice(val label: String, val summary: String) {
        Original("原始", "保留系统绘制的前景层，只做安全边角修复"),
        TextSafe("字标保全", "保护接近白色的文字和字标边角，适合红底白字、证件类图标"),
        Plate("去底板", "移除接触边界的大面积伪底板，主体仍按本地规则重排"),
        Full("全清理", "去底板后继续清理明显长阴影和残留边缘"),
        ComponentSubject("底座当主体", "把 adaptive background 里的复杂底座并入主体，背景重建为纯色或渐变"),
        ComponentBackground("底座当背景", "保留 adaptive background 为背景，只取 foreground 作为主体"),
        TwoLayer("二层", "把图标拆成底板和内层主体，适合圆底板里还有小主体的图标"),
        Rmbg("RMBG", "运行外置 RMBG-2.0 抠图组件，并通过覆盖率和边界校验"),
        Gpt("GPT", "通过 GPT Image 2 生成候选图层"),
        Custom("自定义图片", "从本机导入 PNG 或 SVG，作为当前槽位的输出图片"),
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

    private enum class LocalSeparationMode(val value: String, val label: String, val summary: String) {
        Auto("auto", "自动", "按图标特征自动选择底板清理、边缘修复或阴影清理"),
        Original("original", "原始", "完全保留系统绘制的前景层"),
        Plate("plate", "去底板", "只移除前景里接触边界的大面积伪底板"),
        Full("full", "全清理", "移除伪底板并柔化明显拖尾的长阴影"),
        ComponentSubject("component_subject", "底座当主体", "把 adaptive background 里的复杂底座合进主体，背景重建为纯色或渐变"),
        ComponentBackground("component_background", "底座当背景", "保留 adaptive background 为背景，只取 foreground 当主体");

        companion object {
            fun fromValue(value: String?): LocalSeparationMode =
                entries.firstOrNull { it.value == value } ?: Auto
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
        Ungenerated("未生成"),
    }

    private enum class RootWriteMode(val label: String) {
        All("全部"),
        DefaultOnly("默认"),
        MonochromeOnly("单色"),
    }

    companion object {
        private const val PREFS_NAME = "artplus_mobile"
        private const val PREF_GENERATED_PACKAGE_NAMES = "generated_package_names"
        private const val PREF_GENERATED_PACKAGE_NAMES_UPDATED_AT = "generated_package_names_updated_at"
        private const val PREF_GPT_MODE = "gpt_mode"
        private const val PREF_GPT_BASE_URL = "gpt_base_url"
        private const val PREF_GPT_API_KEY = "gpt_api_key"
        private const val PREF_LOCAL_SEPARATION_MODE = "local_separation_mode"
        private const val PREF_FOREGROUND_SUBJECT_PERCENT = "foreground_subject_percent"
        private const val PREF_MONOCHROME_THEME_SCALE = "monochrome_theme_scale"
        private const val PREF_BACKGROUND_SEPARATION_PERCENT = "background_separation_percent"
        private const val PREF_PLATE_REMOVAL_PERCENT = "plate_removal_percent"
        private const val PREF_SHADOW_REMOVAL_PERCENT = "shadow_removal_percent"
        private const val PREF_ADAPTIVE_FOREGROUND_MODE = "adaptive_foreground_mode"
        private const val PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT = "adaptive_direct_max_coverage_percent"
        private const val PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT = "adaptive_direct_max_coverage_increase_percent"
        private const val PREF_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT = "adaptive_mask_edge_coverage_percent"
        private const val PREF_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT = "adaptive_mask_min_coverage_percent"
        private const val PREF_ADAPTIVE_CENTER_EPSILON_PERCENT = "adaptive_center_epsilon_percent"
        private const val PREF_ORIGINAL_FOREGROUND_CLEANUP_MODE = "original_foreground_cleanup_mode"
        private const val PREF_IMAGE_TUNING_VERSION = "image_tuning_version"
        private const val PREF_FOREGROUND_SUBJECT_PERCENT_MIGRATED = "foreground_subject_percent_migrated"
        private const val PREF_USAGE_PERMISSION_PROMPTED = "usage_permission_prompted"
        private const val EXTRA_DEBUG_GENERATE_PACKAGE = "dev.artplus.mobile.DEBUG_GENERATE_PACKAGE"
        private const val EXTRA_DEBUG_GENERATE_USE_GPT = "dev.artplus.mobile.DEBUG_GENERATE_USE_GPT"
        private const val EXTRA_DEBUG_GENERATE_INSTALL_ROOT = "dev.artplus.mobile.DEBUG_GENERATE_INSTALL_ROOT"
        private const val EXTRA_DEBUG_GENERATE_MODE = "dev.artplus.mobile.DEBUG_GENERATE_MODE"
        private const val CURRENT_IMAGE_TUNING_VERSION = 3
        private const val SIZE_1X1 = 240
        private const val SIZE_2X2 = 704
        private const val LOCAL_ICON_RENDER_SCALE = 3
        private const val GPT_SOURCE_SIZE = 1024
        private const val RMBG_COMPONENT_DIR = "rmbg_component"
        private const val RMBG_MODEL_NAME = "bria-rmbg.onnx"
        private const val RMBG_ONNXRUNTIME_CLASSES_JAR = "onnxruntime-dex.jar"
        private const val RMBG_ONNXRUNTIME_LIB = "libonnxruntime.so"
        private const val RMBG_ONNXRUNTIME_JNI_LIB = "libonnxruntime4j_jni.so"
        private const val RMBG_INPUT_SIZE = 1024
        private const val RMBG_MIN_MODEL_BYTES = 100_000_000L
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
        private const val ICON_CACHE_SIZE = 96
        private const val PRELOAD_ICON_COUNT = 64
        private const val ROOT_UXICONS_DIR = "/data/oplus/uxicons"
        private const val ROOT_SCAN_TIMEOUT_MS = 8_000L
        private const val ARTPLUS_ICON_REFRESH_TIMEOUT_MS = 12_000L
        private const val COLOROS_UX_ICON_CONFIG_KEY = "key_ux_icon_config"
        private const val COLOROS_DEFAULT_ICON_THEME = 2
        private const val COLOROS_INSPIRATION_ICON_THEME = 3
        private const val COLOROS_ARTPLUS_ON = 1
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
        private const val BACK_GESTURE_PAGE_ALPHA_DROP = 0.08f
        private const val BACK_GESTURE_PAGE_SCALE_DROP = 0.035f
        private const val BACK_GESTURE_PAGE_CORNER_DP = 28
        private const val BACK_GESTURE_HOME_REST_SCALE = 0.985f
        private const val BACK_GESTURE_HOME_REST_ALPHA = 0.90f
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
        private const val MONO_ALPHA_MIN = 40
        private const val MONO_ALPHA_MAX = 230
        private const val MONO_ALPHA_GAMMA = 0.85
        private const val MONO_LIGHT_PREVIEW_SCALE = 0.90
        private const val DEFAULT_MONOCHROME_THEME_SCALE = 0.80f
        private const val MIN_MONOCHROME_THEME_SCALE = 0.20f
        private const val MAX_MONOCHROME_THEME_SCALE = 1.50f
        private const val MONO_EDGE_ALPHA_DROP_THRESHOLD = 12
        private const val MONO_EDGE_ALPHA_IGNORE_THRESHOLD = 32
        private const val MONO_EDGE_ALPHA_REPAIR_THRESHOLD = 96
        private const val MONO_EDGE_REPAIR_RADIUS = 3
        private const val MONO_TONAL_MIN_VISIBLE_PIXELS = 64
        private const val MONO_TONAL_RANGE_THRESHOLD = 12
        private const val MONO_EDGE_TRIM_FEATHER_SCALE = 0.26
        private const val MONO_NATIVE_EDGE_LOW_CUT = 6
        private const val MONO_NATIVE_EDGE_HIGH_CUT = 44
        private const val MONO_EDGE_SHARPEN_LOW_CUT = 18
        private const val MONO_EDGE_SHARPEN_HIGH_CUT = 96
        private const val MONO_EDGE_FEATHER_BLEND = 0.58
        private const val MONO_EDGE_SMOOTH_STRENGTH = 0.68
        private const val MONO_EDGE_SMOOTH_RADIUS = 2
        private const val MONO_EDGE_GROW_STRENGTH = 0.42
        private const val MIN_FOREGROUND_SUBJECT_PERCENT = 5
        private const val MAX_FOREGROUND_SUBJECT_PERCENT = 250
        private const val DEFAULT_FOREGROUND_SUBJECT_PERCENT = 100
        private const val LEGACY_FOREGROUND_SUBJECT_PERCENT = 70
        private const val DEFAULT_BACKGROUND_SEPARATION_PERCENT = 96
        private const val MIN_BACKGROUND_SEPARATION_PERCENT = 12
        private const val MAX_BACKGROUND_SEPARATION_PERCENT = 420
        private const val DEFAULT_PLATE_REMOVAL_PERCENT = 58
        private const val MIN_PLATE_REMOVAL_PERCENT = 0
        private const val MAX_PLATE_REMOVAL_PERCENT = 420
        private const val DEFAULT_SHADOW_REMOVAL_PERCENT = 96
        private const val MIN_SHADOW_REMOVAL_PERCENT = 0
        private const val MAX_SHADOW_REMOVAL_PERCENT = 255
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
        private const val CORNER_MASK_WHITE_THRESHOLD = 220
        private const val CORNER_MASK_MAX_REMOVED_RATIO = 0.45
        private const val SHADOW_HIGH_ALPHA_THRESHOLD = 160
        private const val SHADOW_MAX_SATURATION = 0.20
        private const val SHADOW_MAX_LUMINANCE = 220
        private const val SHADOW_MIN_VISIBLE_RATIO = 0.045
        private const val SHADOW_MIN_OFFSET = 8.0
        private const val SHADOW_MIN_DOWN_OFFSET = 2.0
        private const val SHADOW_MIN_LUMA_DROP = 9
        private const val SHADOW_PRESERVE_EDGE_RADIUS = 3
        private const val SHADOW_FADE_RADIUS = 13
        private const val FOREGROUND_EDGE_FEATHER_ALPHA_SCALE = 0.18
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
