package dev.artplus.mobile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.LruCache
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.theme.ColorSchemeMode
import io.github.suqi8.coui.kmp.theme.ThemeController
import android.graphics.Color as AndroidColor
import kotlinx.coroutines.Dispatchers
import dev.artplus.mobile.data.AppEntry
import dev.artplus.mobile.data.loadGeneratedPackageCache
import dev.artplus.mobile.pipeline.DynamicRmbgRuntime
import dev.artplus.mobile.pipeline.installRmbgComponent
import dev.artplus.mobile.state.MainViewModel
import dev.artplus.mobile.state.onCreatePostContent
import dev.artplus.mobile.state.onCreatePreContent
import dev.artplus.mobile.state.onDestroyCleanup
import dev.artplus.mobile.state.onNewIntentDebug
import dev.artplus.mobile.state.onPausePersist
import dev.artplus.mobile.state.onResumeRefresh
import dev.artplus.mobile.state.setStatus
import dev.artplus.mobile.system.ensureNomediaAtTreeRoot
import dev.artplus.mobile.system.isDebugGenerateIntent
import dev.artplus.mobile.ui.actions.buildTuningActions
import dev.artplus.mobile.ui.pages.ArtPlusScreen
import dev.artplus.mobile.system.DebugHttpServer

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
    /** S2.2：写入/保存/刷新三件事的唯一入口，Activity 生命周期内构造一次。 */
    internal val tuningActions by lazy { buildTuningActions() }

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
                setStatus(("未选择 RMBG 组件"))
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
                setStatus = { setStatus((it)) },
                setComponentStatus = { mainViewModel.updateGptRmbgSettings { v -> v.copy(rmbgComponentStatus = (it)) } },
                setLastError = { mainViewModel.updatePreviewSession { v -> v.copy(lastRmbgCandidateError = (it)) } },
                runOnUi = { runOnUiThread(it) },
            )

        }

    internal val chooseCustomImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            val mode = mainViewModel.previewSession.value.pendingCustomImageMode
            val kind = mainViewModel.previewSession.value.pendingCustomImageKind
            mainViewModel.updatePreviewSession { v -> v.copy(pendingCustomImageMode = (null), pendingCustomImageKind = (null)) }
            if (uri == null) {
                setStatus(("未选择自定义图片"))
                return@registerForActivityResult
            }
            if (mode == null || kind == null) {
                setStatus(("自定义槽位已失效"))
                return@registerForActivityResult
            }
            importCustomPreviewImage(mode, kind, uri)
        }

    internal val chooseWallpaperLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                setStatus(("未选择壁纸"))
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

            // 主题跟随系统深浅色，由 ThemeController 提供完整 ColorOS 调色板与 LocalContentColor。
            val themeController = remember { ThemeController(ColorSchemeMode.System) }
            COUITheme(controller = themeController) {
                ArtPlusScreen()
            }
        }

        mainViewModel.onCreatePostContent(
            isDebugIntent = isDebugGenerateIntent(intent) { token ->
                token != null && token == debugGenerateToken()
            },
            onRequestDeclaredPermissions = ::requestDeclaredPermissions,
            onRequestSpecialPermissionsOnce = ::requestSpecialPermissionsOnce,
            onLoadApps = { loadApps() },
            onHandleDebugIntent = { handleDebugGenerateIntent(intent) },
        )
    }

    /**
     * S1.5：debug 生成 token 的读/建（原嵌在 onCreatePostContent 参数里的 run{} 三层嵌套，等价重写）。
     * 仅在 isDebugGenerateIntent 需要校验时调用，行为与原来一致：≥32 字符即复用，否则新建并落盘。
     */
    private fun debugGenerateToken(): String {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.getString(PREF_DEBUG_TOKEN, null)?.takeIf { it.length >= 32 }?.let { return it }
        val created = UUID.randomUUID().toString() + UUID.randomUUID().toString()
        prefs.edit().putString(PREF_DEBUG_TOKEN, created).apply()
        return created
    }

    // S3.7：配置变更（字体缩放/分屏/旋转）不丢 29 个草稿态。
    // Activity 级 Bundle 保存，等价于把 rememberSaveable 提升到页面根。
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("draftForegroundSubjectPercentText", draftForegroundSubjectPercentText)
        outState.putString("draftForegroundShadowLevelText", draftForegroundShadowLevelText)
        outState.putString("draftMonochromeThemeScaleText", draftMonochromeThemeScaleText)
        outState.putString("draftBackgroundSeparationText", draftBackgroundSeparationText)
        outState.putString("draftPlateRemovalText", draftPlateRemovalText)
        outState.putString("draftShadowRemovalText", draftShadowRemovalText)
        outState.putString("draftEdgePolishText", draftEdgePolishText)
        outState.putString("draftRmbgAlphaStrengthText", draftRmbgAlphaStrengthText)
        outState.putString("draftRmbgEdgeFeatherText", draftRmbgEdgeFeatherText)
        outState.putString("draftRmbgEdgeAdjustText", draftRmbgEdgeAdjustText)
        outState.putString("draftRmbgWeakAlphaKeepText", draftRmbgWeakAlphaKeepText)
        outState.putString("draftLiquidGlassRadiusText", draftLiquidGlassRadiusText)
        outState.putString("draftLiquidGlassOuterWidthText", draftLiquidGlassOuterWidthText)
        outState.putString("draftLiquidGlassTopAlphaText", draftLiquidGlassTopAlphaText)
        outState.putString("draftLiquidGlassBottomAlphaText", draftLiquidGlassBottomAlphaText)
        outState.putString("draftLiquidGlassBackgroundMistAlphaText", draftLiquidGlassBackgroundMistAlphaText)
        outState.putString("draftLiquidGlassBottomDarkAlphaText", draftLiquidGlassBottomDarkAlphaText)
        outState.putString("draftLiquidGlassSubjectScaleText", draftLiquidGlassSubjectScaleText)
        outState.putString("draftLiquidGlassSubjectOutlineWidthText", draftLiquidGlassSubjectOutlineWidthText)
        outState.putString("draftLiquidGlassSubjectInnerOutlineWidthText", draftLiquidGlassSubjectInnerOutlineWidthText)
        outState.putString("draftLiquidGlassSubjectShadowAlphaText", draftLiquidGlassSubjectShadowAlphaText)
        outState.putString("draftLiquidGlassSubjectOpacityText", draftLiquidGlassSubjectOpacityText)
        outState.putString("draftBatchPreviewCountText", draftBatchPreviewCountText)
        outState.putString("draftBatchPreviewColumnsText", draftBatchPreviewColumnsText)
        outState.putString("draftBatchPreviewIconSizeDpText", draftBatchPreviewIconSizeDpText)
        outState.putString("draftBatchPreviewCornerRadiusDpText", draftBatchPreviewCornerRadiusDpText)
        outState.putString("draftJsonParamsText", draftJsonParamsText)
        outState.putString("draftPreviewCornerRadiusDpText", draftPreviewCornerRadiusDpText)
        outState.putString("draftPreviewIconSizeDpText", draftPreviewIconSizeDpText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getString("draftForegroundSubjectPercentText")?.let { draftForegroundSubjectPercentText = it }
        savedInstanceState.getString("draftForegroundShadowLevelText")?.let { draftForegroundShadowLevelText = it }
        savedInstanceState.getString("draftMonochromeThemeScaleText")?.let { draftMonochromeThemeScaleText = it }
        savedInstanceState.getString("draftBackgroundSeparationText")?.let { draftBackgroundSeparationText = it }
        savedInstanceState.getString("draftPlateRemovalText")?.let { draftPlateRemovalText = it }
        savedInstanceState.getString("draftShadowRemovalText")?.let { draftShadowRemovalText = it }
        savedInstanceState.getString("draftEdgePolishText")?.let { draftEdgePolishText = it }
        savedInstanceState.getString("draftRmbgAlphaStrengthText")?.let { draftRmbgAlphaStrengthText = it }
        savedInstanceState.getString("draftRmbgEdgeFeatherText")?.let { draftRmbgEdgeFeatherText = it }
        savedInstanceState.getString("draftRmbgEdgeAdjustText")?.let { draftRmbgEdgeAdjustText = it }
        savedInstanceState.getString("draftRmbgWeakAlphaKeepText")?.let { draftRmbgWeakAlphaKeepText = it }
        savedInstanceState.getString("draftLiquidGlassRadiusText")?.let { draftLiquidGlassRadiusText = it }
        savedInstanceState.getString("draftLiquidGlassOuterWidthText")?.let { draftLiquidGlassOuterWidthText = it }
        savedInstanceState.getString("draftLiquidGlassTopAlphaText")?.let { draftLiquidGlassTopAlphaText = it }
        savedInstanceState.getString("draftLiquidGlassBottomAlphaText")?.let { draftLiquidGlassBottomAlphaText = it }
        savedInstanceState.getString("draftLiquidGlassBackgroundMistAlphaText")?.let { draftLiquidGlassBackgroundMistAlphaText = it }
        savedInstanceState.getString("draftLiquidGlassBottomDarkAlphaText")?.let { draftLiquidGlassBottomDarkAlphaText = it }
        savedInstanceState.getString("draftLiquidGlassSubjectScaleText")?.let { draftLiquidGlassSubjectScaleText = it }
        savedInstanceState.getString("draftLiquidGlassSubjectOutlineWidthText")?.let { draftLiquidGlassSubjectOutlineWidthText = it }
        savedInstanceState.getString("draftLiquidGlassSubjectInnerOutlineWidthText")?.let { draftLiquidGlassSubjectInnerOutlineWidthText = it }
        savedInstanceState.getString("draftLiquidGlassSubjectShadowAlphaText")?.let { draftLiquidGlassSubjectShadowAlphaText = it }
        savedInstanceState.getString("draftLiquidGlassSubjectOpacityText")?.let { draftLiquidGlassSubjectOpacityText = it }
        savedInstanceState.getString("draftBatchPreviewCountText")?.let { draftBatchPreviewCountText = it }
        savedInstanceState.getString("draftBatchPreviewColumnsText")?.let { draftBatchPreviewColumnsText = it }
        savedInstanceState.getString("draftBatchPreviewIconSizeDpText")?.let { draftBatchPreviewIconSizeDpText = it }
        savedInstanceState.getString("draftBatchPreviewCornerRadiusDpText")?.let { draftBatchPreviewCornerRadiusDpText = it }
        savedInstanceState.getString("draftJsonParamsText")?.let { draftJsonParamsText = it }
        savedInstanceState.getString("draftPreviewCornerRadiusDpText")?.let { draftPreviewCornerRadiusDpText = it }
        savedInstanceState.getString("draftPreviewIconSizeDpText")?.let { draftPreviewIconSizeDpText = it }
    }

    /**
     * S6.4：内存压力下释放图标缓存（原来只在进程被杀时随 GC 回收）。
     * 只清缓存，不动任何持久化状态；缓存未命中时会按原路径重新解码。
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_RUNNING_LOW) {
            appIconCache.evictAll()
        }
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

    @Volatile
    internal var cachedCustomWallpaper: Bitmap? = null
    @Volatile
    internal var cachedCustomWallpaperPath: String? = null
    @Volatile
    internal var cachedSystemWallpaper: Bitmap? = null
    @Volatile
    internal var cachedBundledWallpaper: Bitmap? = null

    companion object {
        internal const val PREF_BATCH_OUTPUT_MODE = "batch_output_mode"
        internal const val PREF_GPT_RUN_COUNT = "gpt_run_count"
        internal const val PREF_RMBG_RUN_COUNT = "rmbg_run_count"
        internal const val PREF_DEBUG_TOKEN = "debug_token"
        internal val appIconCache = object : LruCache<String, Bitmap>(
            ((Runtime.getRuntime().maxMemory() / 1024) / 16).toInt().coerceAtLeast(4 * 1024),
        ) {
            override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount / 1024
        }
    }
}
