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
    internal var queryText by mutableStateOf("")
    internal var selectedPackageName by mutableStateOf<String?>(null)
    internal var statusText by mutableStateOf("加载应用列表中...")
    internal var packageListPermissionGranted by mutableStateOf(true)
    internal var usageAccessGranted by mutableStateOf(false)
    internal var outputTreeUri by mutableStateOf<Uri?>(null)
    internal var isBusy by mutableStateOf(false)
    internal var didRequestAppLoad = false
    internal var gptModelId by mutableStateOf("")
    internal var gptBaseUrl by mutableStateOf("")
    internal var gptApiKey by mutableStateOf("")
    internal var gptSettingsSaveStatus by mutableStateOf("")
    internal var draftForegroundSubjectPercentText by mutableStateOf(DEFAULT_FOREGROUND_SUBJECT_PERCENT.toString())
    internal var draftForegroundShadowLevelText by mutableStateOf(DEFAULT_FOREGROUND_SHADOW_LEVEL.toString())
    internal var draftMonochromeThemeScaleText by mutableStateOf((DEFAULT_MONOCHROME_THEME_SCALE * 100).roundToInt().toString())
    internal var advancedSettingsCategory by mutableStateOf(AdvancedSettingsCategory.LiquidGlass)
    internal var advancedSettingsTab by mutableStateOf(AdvancedSettingsTab.Sliders)
    internal var draftBackgroundSeparationText by mutableStateOf(DEFAULT_BACKGROUND_SEPARATION_PERCENT.toString())
    internal var draftPlateRemovalText by mutableStateOf(DEFAULT_PLATE_REMOVAL_PERCENT.toString())
    internal var draftShadowRemovalText by mutableStateOf(DEFAULT_SHADOW_REMOVAL_PERCENT.toString())
    internal var draftEdgePolishText by mutableStateOf(DEFAULT_EDGE_POLISH_PERCENT.toString())
    internal var draftRmbgAlphaStrengthText by mutableStateOf(DEFAULT_RMBG_ALPHA_STRENGTH_PERCENT.toString())
    internal var draftRmbgEdgeFeatherText by mutableStateOf(DEFAULT_RMBG_EDGE_FEATHER_PERCENT.toString())
    internal var draftRmbgEdgeAdjustText by mutableStateOf(DEFAULT_RMBG_EDGE_ADJUST_PERCENT.toString())
    internal var draftRmbgWeakAlphaKeepText by mutableStateOf(DEFAULT_RMBG_WEAK_ALPHA_KEEP_PERCENT.toString())
    internal var liquidGlassBottomBarEnabled by mutableStateOf(true)
    internal var liquidGlassBottomBarBlurEnabled by mutableStateOf(true)
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
    internal var lastParamsSnapshot by mutableStateOf<TuningParams?>(null)
    // P2 交界：历史单源已收敛进 MainViewModel（state/），Activity 不再持有 tuningHistory 栈；
    // 186 live vars 与 currentTuningParams() 不动（P5 重写），同步一律走快照显式调用。
    internal val mainViewModel: MainViewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }
    internal var activePresetId by mutableStateOf<String?>(null)
    internal var activePresetBaseParams by mutableStateOf<TuningParams?>(null)
    internal var presetListVersion by mutableStateOf(0)
    internal var batchOutputMode by mutableStateOf(BatchOutputMode.Root)
    internal var gptRunCount by mutableStateOf(0)
    internal var rmbgRunCount by mutableStateOf(0)
    internal var presetSaveDialogVisible by mutableStateOf(false)
    internal var presetSaveName by mutableStateOf("")
    internal var presetImportDialogVisible by mutableStateOf(false)
    internal var presetImportText by mutableStateOf("")
    internal var presetRenameTarget by mutableStateOf<TuningPreset?>(null)
    internal var presetActionMenuTarget by mutableStateOf<TuningPreset?>(null)
    internal var presetDeleteConfirmTarget by mutableStateOf<TuningPreset?>(null)
    internal var presetSearchQuery by mutableStateOf("")
    internal var presetListExpanded by mutableStateOf(false)
    internal var presetBatchPreviewConfirmTarget by mutableStateOf<TuningPreset?>(null)
    internal var activeBatchPreviewPreset by mutableStateOf<TuningPreset?>(null)
    internal var showBatchPreviewRefreshConfirm by mutableStateOf(false)
    internal var batchPreviewProgress by mutableStateOf<BatchPreviewProgress?>(null)
    internal var batchPreviewResult by mutableStateOf<BatchPreviewResult?>(null)
    internal var batchPreviewCancelled by mutableStateOf(false)
    internal var isGeneratingBatchPreview by mutableStateOf(false)
    internal var batchPreviewCount by mutableIntStateOf(BatchPreviewSampler.DEFAULT_BATCH_PREVIEW_COUNT)
    internal var draftBatchPreviewCountText by mutableStateOf(BatchPreviewSampler.DEFAULT_BATCH_PREVIEW_COUNT.toString())
    internal var batchPreviewColumns by mutableIntStateOf(4)
    internal var draftBatchPreviewColumnsText by mutableStateOf("4")
    internal var batchPreviewIconSizeDp by mutableIntStateOf(54)
    internal var draftBatchPreviewIconSizeDpText by mutableStateOf("54")
    internal var batchPreviewCornerRadiusDp by mutableIntStateOf(20)
    internal var draftBatchPreviewCornerRadiusDpText by mutableStateOf("20")
    internal var batchPreviewDesktopBackground by mutableStateOf(PreviewDesktopBackground.DarkGray)
    internal var customWallpaperPath by mutableStateOf<String?>(null)
    internal var customWallpaperInfo by mutableStateOf("")
    internal var pendingServiceConfirm by mutableStateOf<ServiceConfirmRequest?>(null)
    internal var autoConfirmRootWrite by mutableStateOf(false)
    internal var pendingRootWriteConfirm by mutableStateOf<RootWriteConfirmRequest?>(null)
    internal var rootWriteConfirmRememberSkip by mutableStateOf(false)
    internal var draftJsonParamsText by mutableStateOf("")
    internal var refreshConfirmVisible by mutableStateOf(false)
    internal var autoConfirmRefresh by mutableStateOf(false)
    internal var refreshConfirmRememberAuto by mutableStateOf(false)
    internal val presetStore by lazy { PresetStore(getSharedPreferences(PREFS_NAME, MODE_PRIVATE)) }
    internal var currentPage by mutableStateOf(AppPage.Home)
    internal var showSystemApps by mutableStateOf(false)
    internal var generatedFilter by mutableStateOf(GeneratedFilter.All)
    internal var generatedPackageNames by mutableStateOf<Set<String>>(emptySet())
    internal var multiSelectedPackageNames by mutableStateOf<Set<String>>(emptySet())
    internal var batchApplyProgress by mutableStateOf<BatchApplyProgress?>(null)
    internal var exportProgress by mutableStateOf<ExportProgress?>(null)
    // 底部备份/导出弹窗与后台态
    internal var backupProgress by mutableStateOf<ExportProgress?>(null)
    internal var backupSheetVisible by mutableStateOf(false)
    internal var singleExportSheetVisible by mutableStateOf(false)
    internal var backupInBackground by mutableStateOf(false)
    internal var backupBackgroundDots by mutableStateOf(1)
    internal var backupJob: Job? = null
    internal var singleExportJob: Job? = null
    internal var backupDotJob: Job? = null
    internal var isScanningGeneratedPackages by mutableStateOf(false)
    internal var generatedScanFailed by mutableStateOf(false)
    internal var previewPackageName by mutableStateOf<String?>(null)
    internal var previewDirPath by mutableStateOf<String?>(null)
    internal var previewVersion by mutableStateOf(0)
    internal var previewStripEnabled by mutableStateOf(false)
    internal var sharedPreviewAssets by mutableStateOf<PreviewAssets?>(null)
    internal var activeGenerationSession by mutableStateOf<GenerationSession?>(null)
    internal var previewDesktopBackground by mutableStateOf(PreviewDesktopBackground.DarkGray)
    internal var previewCornerRadiusDp by mutableStateOf(DEFAULT_PREVIEW_CORNER_RADIUS_DP)
    internal var draftPreviewCornerRadiusDpText by mutableStateOf(DEFAULT_PREVIEW_CORNER_RADIUS_DP.toString())
    internal var previewIconSizeDp by mutableStateOf(DEFAULT_PREVIEW_ICON_SIZE_DP)
    internal var draftPreviewIconSizeDpText by mutableStateOf(DEFAULT_PREVIEW_ICON_SIZE_DP.toString())
    internal var previewChoiceMode by mutableStateOf<PreviewMode?>(null)
    internal var isGptPreviewLoading by mutableStateOf(false)
    internal var isGeneratingGptCandidate by mutableStateOf(false)
    internal var isGeneratingRmbgCandidate by mutableStateOf(false)
    internal var isRefreshingArtPlusIcons by mutableStateOf(false)
    internal var isPreviewAssetsRefreshing by mutableStateOf(false)
    internal var isPreviewOutputRefreshing by mutableStateOf(false)
    internal var lastRmbgCandidateError by mutableStateOf<String?>(null)
    internal var rmbgCandidatePackageName by mutableStateOf<String?>(null)
    internal var rmbgCandidateMode by mutableStateOf<PreviewMode?>(null)
    internal var rmbgCandidateStatusText by mutableStateOf("")
    internal var rmbgCandidateFailurePackageName by mutableStateOf<String?>(null)
    internal var rmbgCandidateFailureMode by mutableStateOf<PreviewMode?>(null)
    internal var skipNextHomeReturnAnimation by mutableStateOf(false)
    internal var pendingCustomImageMode by mutableStateOf<PreviewMode?>(null)
    internal var pendingCustomImageKind by mutableStateOf<CustomImageKind?>(null)
    internal var isInstallingRmbgComponent by mutableStateOf(false)
    internal var rmbgInstallStage by mutableStateOf("")
    internal var rmbgInstallProgress by mutableStateOf<Float?>(null)
    internal var rmbgDialogVisible by mutableStateOf(false)
    internal var exportDialogVisible by mutableStateOf(false)
    internal var resetDefaultsDialogVisible by mutableStateOf(false)
    internal var onboardingVisible by mutableStateOf(false)
    internal var rmbgComponentUrl by mutableStateOf("")
    internal var rmbgComponentSaveStatus by mutableStateOf("")
    internal var lastRmbgInferenceReport by mutableStateOf<RmbgInferenceReport?>(null)
    internal var previewOutputJob: Job? = null
    internal var previewOutputRevision = 0
    internal var generatedPreviewRestoreRevision = 0
    internal var debugHttpServer: DebugHttpServer? = null
    internal var rmbgRuntime: DynamicRmbgRuntime? = null
    internal var rmbgComponentStatus by mutableStateOf("")
    internal var isCheckingUpdate by mutableStateOf(false)
    internal var updateAvailableInfo by mutableStateOf<UpdateInfo?>(null)
    internal var updateUpToDateDialogVisible by mutableStateOf(false)
    internal var mitLicenseDialogVisible by mutableStateOf(false)
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
            outputTreeUri = uri
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }
            // 自动在根目录创建 .nomedia，避免出现在相册
            runCatching { ensureNomediaAtTreeRoot(contentResolver, outputTreeUri) }
            toastStatus("已选择输出目录")
            saveUiState()
            // 若来自首次引导，自动执行全量备份
            if (onboardingVisible) {
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putBoolean(PREF_HAS_COMPLETED_ONBOARDING, true).apply()
                onboardingVisible = false
                backupAllToExternal(isFromOnboarding = true)
            }
        }

    internal val chooseRmbgComponentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                statusText = "未选择 RMBG 组件"
                return@registerForActivityResult
            }
            installRmbgComponent(uri)
        }

    internal val chooseCustomImageLauncher =
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

    internal val chooseWallpaperLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                statusText = "未选择壁纸"
                return@registerForActivityResult
            }
            importCustomWallpaper(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "ArtPlus Mobile"
        loadGptSettings()
        loadTuningParams()
        initTuningHistory()
        loadRmbgSettings()
        generatedPackageNames = loadGeneratedPackageCache(getSharedPreferences(PREFS_NAME, MODE_PRIVATE))
        generatedScanFailed = false
        isScanningGeneratedPackages = false
        loadUiState()
        loadPresetState()
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

    internal fun startUiFriendlyThread(name: String, block: () -> Unit) {
        Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            block()
        }, name).apply {
            priority = Thread.MIN_PRIORITY
            start()
        }
    }


    @Composable
    internal fun OnboardingDialog() {
        if (!onboardingVisible) return
        MiuixBottomDialog(onDismissRequest = {
            // 允许通过外部点击关闭视为跳过
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putBoolean(PREF_HAS_COMPLETED_ONBOARDING, true).apply()
            onboardingVisible = false
            toastStatus("已跳过，可在设置-导出引导中重新进入")
        }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MiuixTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "设置备份目录",
                    style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                    color = MiuixTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "首次使用建议先选择一个外部目录，用于备份已写入系统的全部图标（含官方图标）。选择后将自动执行一次全量备份，并在该目录创建 .nomedia 避免出现在相册。",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = {
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                                .putBoolean(PREF_HAS_COMPLETED_ONBOARDING, true).apply()
                            onboardingVisible = false
                            toastStatus("已跳过，可在设置-导出引导中重新进入")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(),
                    ) {
                        Text(
                            text = "跳过",
                            style = MiuixTheme.textStyles.button,
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                    }
                    Button(
                        onClick = {
                            // 不在此关闭，等待 chooseTreeLauncher 回调中关闭
                            chooseTreeLauncher.launch(null)
                        },
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary(),
                    ) {
                        Text(
                            text = "选择目录",
                            style = MiuixTheme.textStyles.button,
                            color = Color.White,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }



    @Composable
    internal fun RefreshConfirmDialog() {
        if (!refreshConfirmVisible) return
        MiuixBottomDialog(onDismissRequest = { refreshConfirmVisible = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MiuixTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "确认刷新",
                    style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                    color = MiuixTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "将重新扫描已生成图标并刷新显示，确认继续？",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { refreshConfirmRememberAuto = !refreshConfirmRememberAuto }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Checkbox(
                        state = ToggleableState(refreshConfirmRememberAuto),
                        onClick = { refreshConfirmRememberAuto = !refreshConfirmRememberAuto },
                        colors = CheckboxDefaults.checkboxColors(
                            checkedBackgroundColor = MiuixTheme.colorScheme.primaryVariant,
                            checkedForegroundColor = MiuixTheme.colorScheme.onPrimaryVariant,
                            uncheckedBackgroundColor = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.35f),
                            uncheckedForegroundColor = Color.Transparent,
                        ),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "以后都自动确认",
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { refreshConfirmVisible = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(),
                    ) {
                        Text(
                            text = "取消",
                            style = MiuixTheme.textStyles.button,
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                    }
                    Button(
                        onClick = {
                            val shouldAuto = refreshConfirmRememberAuto
                            refreshConfirmVisible = false
                            if (shouldAuto) {
                                autoConfirmRefresh = true
                                saveUiState()
                            }
                            refreshArtPlusIcons()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary(),
                    ) {
                        Text(
                            text = "刷新",
                            style = MiuixTheme.textStyles.button,
                            color = Color.White,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }








    @Composable
    internal fun PermissionCard() {
        SectionCard {
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
    internal fun GeneratedPreviewCard() {
        val dirPath = previewDirPath ?: return
        val packageName = previewPackageName ?: return
        val session = activeGenerationSession?.takeIf {
            it.packageName == packageName && it.outDir.absolutePath == dirPath
        }
        val displayAssets = sharedPreviewAssets
        val previewLoading = isGptPreviewLoading || isPreviewAssetsRefreshing || isPreviewOutputRefreshing

        SectionCard {
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
                return@SectionCard
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PreviewTile(
                    label = "标准亮色",
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
                    label = "标准暗色",
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
    }


    @Composable
    internal fun TopPreviewStripTile(
        assets: PreviewAssets?,
        mode: PreviewMode,
        loading: Boolean,
        desktopBackground: PreviewDesktopBackground,
        iconSizeDp: Int,
        cornerRadiusDp: Int,
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
            )
            if (ready) {
                GeneratedIconPreview(
                    assets = assets,
                    mode = mode,
                    modifier = Modifier.fillMaxSize(iconFraction),
                    cornerRadiusDp = scaledCornerDp,
                )
            } else {
                MissingIconPreview(
                    modifier = Modifier.fillMaxSize(iconFraction),
                    mode = mode,
                    compact = true,
                    cornerRadiusDp = scaledCornerDp,
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

    /** 调试图层卡片：前景 / 背景 / alpha 蒙版小图。 */
    @Composable
    internal fun LayerDebugCard() {
        val tuningState = mainViewModel.params.collectAsState().value
        val dirPath = previewDirPath ?: return
        val packageName = previewPackageName ?: return
        val session = activeGenerationSession?.takeIf {
            it.packageName == packageName && it.outDir.absolutePath == dirPath
        } ?: return
        val assets = sharedPreviewAssets ?: return

        val choice = PreviewSelections.fromNames(tuningState.previewNormalLight, tuningState.previewNormalDark, tuningState.previewMonochromeLight, tuningState.previewMonochromeDark).normalLight
        val candidate = candidateForChoice(session, choice)
        val alphaMask = remember(candidate) {
            candidate?.recfgRaw?.let { bitmap ->
                val width = bitmap.width
                val height = bitmap.height
                if (width <= 0 || height <= 0) {
                    null
                } else {
                    val pixels = IntArray(width * height)
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                    val outPixels = IntArray(pixels.size)
                    for (i in pixels.indices) {
                        val alpha = AndroidColor.alpha(pixels[i])
                        outPixels[i] = AndroidColor.rgb(alpha, alpha, alpha)
                    }
                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { out ->
                        out.setPixels(outPixels, 0, width, 0, 0, width, height)
                    }
                }
            }
        }

        SectionCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LayerDebugTile(label = "前景", bitmap = assets.recfg?.asImageBitmap(), modifier = Modifier.weight(1f))
                LayerDebugTile(label = "背景", bitmap = assets.recbg?.asImageBitmap(), modifier = Modifier.weight(1f))
                LayerDebugTile(label = "蒙版", bitmap = alphaMask?.asImageBitmap(), modifier = Modifier.weight(1f))
            }
        }
    }


    @Composable
    internal fun PreviewControlCard() {
        val tuningState = mainViewModel.params.collectAsState().value
        SectionCard(rowsFullBleed = true) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NumberParameterControl(
                    busy = isBusy,
                    title = "前景主体大小",
                    summary = "控制前景主体在图标画布中的占比",
                    value = tuningState.foregroundSubjectPercent,
                    draftText = draftForegroundSubjectPercentText,
                    min = MIN_FOREGROUND_SUBJECT_PERCENT,
                    max = MAX_FOREGROUND_SUBJECT_PERCENT,
                    step = 1,
                    onDraftChange = { draftForegroundSubjectPercentText = it },
                    onSave = { updateForegroundSubjectPercent(it) },
                    icon = SettingsIconKind.Scale,
                )
                NumberParameterControl(
                    busy = isBusy,
                    title = "预览圆角",
                    summary = "控制预览图标的圆角大小",
                    value = previewCornerRadiusDp,
                    draftText = draftPreviewCornerRadiusDpText,
                    min = MIN_PREVIEW_CORNER_RADIUS_DP,
                    max = MAX_PREVIEW_CORNER_RADIUS_DP,
                    step = 1,
                    onDraftChange = { draftPreviewCornerRadiusDpText = it },
                    onSave = { updatePreviewCornerRadiusDp(it) },
                    icon = SettingsIconKind.Radius,
                )
                NumberParameterControl(
                    busy = isBusy,
                    title = "预览缩放",
                    summary = "预览图显示大小",
                    value = previewIconSizeDp,
                    draftText = draftPreviewIconSizeDpText,
                    min = MIN_PREVIEW_ICON_SIZE_DP,
                    max = MAX_PREVIEW_ICON_SIZE_DP,
                    step = 1,
                    onDraftChange = { draftPreviewIconSizeDpText = it },
                    onSave = { updatePreviewIconSizeDp(it) },
                    icon = SettingsIconKind.Grid,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isBusy) { updatePreviewStripEnabled(!previewStripEnabled) }
                        .padding(horizontal = CHOICE_ROW_HORIZONTAL_BLEED_DP.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SettingsLineIcon(kind = SettingsIconKind.Palette)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = "顶部 1×4 预览条",
                            style = MiuixTheme.textStyles.body1,
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "在主页、生成参数与预设页置顶显示",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Switch(
                        checked = previewStripEnabled,
                        onCheckedChange = { updatePreviewStripEnabled(it) },
                        enabled = !isBusy,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CHOICE_ROW_HORIZONTAL_BLEED_DP.dp),
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
    }

    @Composable
    internal fun PreviewBackgroundOption(
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
    internal fun PreviewTile(
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
                            cornerRadiusDp = previewCornerRadiusDp,
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
                            cornerRadiusDp = previewCornerRadiusDp,
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
    internal fun DesktopIconPreview(
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
    internal fun PreviewDesktopBackgroundSurface(
        option: PreviewDesktopBackground,
        modifier: Modifier = Modifier,
    ) {
        val wallpaper = remember(option, customWallpaperPath) {
            if (option == PreviewDesktopBackground.Wallpaper) {
                loadCustomWallpaperBitmap() ?: loadPreviewWallpaperBitmap() ?: loadBundledPreviewWallpaperBitmap()
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
            }
        }
    }

    @Composable
    internal fun GeneratedIconPreview(
        assets: PreviewAssets?,
        mode: PreviewMode,
        modifier: Modifier = Modifier.size(72.dp),
        cornerRadiusDp: Int = previewCornerRadiusDp,
    ) {
        val iconShape = RoundedCornerShape(cornerRadiusDp.dp)
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
    internal fun MissingIconPreview(
        modifier: Modifier = Modifier.size(72.dp),
        mode: PreviewMode? = null,
        compact: Boolean = false,
        cornerRadiusDp: Int = previewCornerRadiusDp,
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
    internal fun PreviewNightFillBackgroundRow() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MiuixTheme.colorScheme.secondaryContainer.copy(alpha = 0.58f))
                .clickable(enabled = !isBusy) {
                    updateNightSubjectLightBackgroundEnabled(!mainViewModel.params.value.nightSubjectLightBackgroundEnabled)
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
                checked = mainViewModel.params.value.nightSubjectLightBackgroundEnabled,
                enabled = !isBusy,
            )
        }
    }

    @Composable
    internal fun PreviewChoiceDialog(mode: PreviewMode, session: GenerationSession) {
        val tuningState = mainViewModel.params.collectAsState().value
        val defaultChoices = listOf(
            PreviewChoice.Original,
            PreviewChoice.ComposedBackground,
            PreviewChoice.Rmbg,
            PreviewChoice.Gpt,
        )
        val moreChoices = listOf(
            PreviewChoice.TextSafe,
            PreviewChoice.ComponentSubject,
            PreviewChoice.ComponentBackground,
            PreviewChoice.TwoLayer,
            PreviewChoice.CustomForeground,
            PreviewChoice.CustomBackground,
        )
        val selectedMoreRule = PreviewSelections.fromNames(tuningState.previewNormalLight, tuningState.previewNormalDark, tuningState.previewMonochromeLight, tuningState.previewMonochromeDark).choiceFor(mode).let { choice ->
            when {
                choice == PreviewChoice.Plate -> PreviewChoice.Full
                choice in moreChoices -> choice
                else -> null
            }
        }
        var showMoreRules by remember(mode) { mutableStateOf(false) }
        Dialog(onDismissRequest = { previewChoiceMode = null }) {
            ApplyDialogDimEffect()
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
                    NumberParameterControl(
                        busy = isBusy,
                        title = "主体占比",
                        summary = "复杂游戏图标建议 100%",
                        value = mainViewModel.params.value.foregroundSubjectPercent,
                        draftText = draftForegroundSubjectPercentText,
                        min = MIN_FOREGROUND_SUBJECT_PERCENT,
                        max = MAX_FOREGROUND_SUBJECT_PERCENT,
                        step = 1,
                        onDraftChange = { draftForegroundSubjectPercentText = it },
                        onSave = { updateForegroundSubjectPercent(it) },
                        icon = SettingsIconKind.Scale,
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
                        if (shouldShowPreviewChoiceRow(PreviewChoice.Full, session)) {
                            PreviewChoiceRow(
                                mode = mode,
                                choice = PreviewChoice.Full,
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

    internal fun shouldShowPreviewChoiceRow(choice: PreviewChoice, session: GenerationSession): Boolean =
        when {
            choice.isCustom -> true
            choice == PreviewChoice.Full -> session.candidates[PreviewChoice.Full] != null ||
                session.candidates[PreviewChoice.Plate] != null
            else -> candidateForChoice(session, choice) != null
        }

    @Composable
    internal fun MoreRulesGroupRow(
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
                        ?: "字标保全 / 底座 / 二层 / 自定义",
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
    internal fun PreviewChoiceRow(mode: PreviewMode, choice: PreviewChoice, session: GenerationSession) {
        val tuningState = mainViewModel.params.collectAsState().value
        val currentChoice = PreviewSelections.fromNames(tuningState.previewNormalLight, tuningState.previewNormalDark, tuningState.previewMonochromeLight, tuningState.previewMonochromeDark).choiceFor(mode)
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
                        gptMissing && !canGenerateGpt -> "请填写AI提供商信息"
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
    internal fun CandidateIconPreview(candidate: IconCandidate, mode: PreviewMode) {
        val tuningState = mainViewModel.params.collectAsState().value
        var assets by remember(
            candidate,
            mode,
            tuningState.foregroundSubjectPercent,
            tuningState.foregroundShadowLevel,
            tuningState.edgePolishPercent,
            tuningState.rmbgAlphaStrengthPercent,
            tuningState.rmbgEdgeFeatherPercent,
            tuningState.rmbgEdgeAdjustPercent,
            tuningState.rmbgWeakAlphaKeepPercent,
            tuningState.liquidGlassEnabled,
            tuningState.liquidGlassRadius,
            tuningState.liquidGlassOuterWidth,
            tuningState.liquidGlassTopAlpha,
            tuningState.liquidGlassBottomAlpha,
            tuningState.liquidGlassBackgroundMistAlpha,
            tuningState.liquidGlassBottomDarkAlpha,
            tuningState.liquidGlassSubjectScalePercent,
            tuningState.liquidGlassSubjectOutlineWidth,
            tuningState.liquidGlassSubjectInnerOutlineWidth,
            tuningState.liquidGlassSubjectShadowAlpha,
            tuningState.liquidGlassSubjectOpacityPercent,
            tuningState.nightSubjectLightBackgroundEnabled,
        ) {
            mutableStateOf<PreviewAssets?>(null)
        }
        LaunchedEffect(
            candidate,
            mode,
            tuningState.foregroundSubjectPercent,
            tuningState.foregroundShadowLevel,
            tuningState.edgePolishPercent,
            tuningState.rmbgAlphaStrengthPercent,
            tuningState.rmbgEdgeFeatherPercent,
            tuningState.rmbgEdgeAdjustPercent,
            tuningState.rmbgWeakAlphaKeepPercent,
            tuningState.liquidGlassEnabled,
            tuningState.liquidGlassRadius,
            tuningState.liquidGlassOuterWidth,
            tuningState.liquidGlassTopAlpha,
            tuningState.liquidGlassBottomAlpha,
            tuningState.liquidGlassBackgroundMistAlpha,
            tuningState.liquidGlassBottomDarkAlpha,
            tuningState.liquidGlassSubjectScalePercent,
            tuningState.liquidGlassSubjectOutlineWidth,
            tuningState.liquidGlassSubjectInnerOutlineWidth,
            tuningState.liquidGlassSubjectShadowAlpha,
            tuningState.liquidGlassSubjectOpacityPercent,
            tuningState.nightSubjectLightBackgroundEnabled,
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
    internal fun systemMaterialColor(resourceName: String, fallback: Color): Color {
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
    internal fun StatusCard(
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
            val interactionSource = remember { MutableInteractionSource() }
            val pressed by interactionSource.collectIsPressedAsState()
            val bleedPx = with(LocalDensity.current) { CHOICE_ROW_HORIZONTAL_BLEED_DP.dp.roundToPx() }
            val bridge = remember { SectionCardPressBridge() }
            CompositionLocalProvider(LocalSectionCardPressBridge provides bridge) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sectionPressOverlay(bridge, extendTopEdge = true),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .trackSectionPress(bridge, pressed)
                            .cardRowBleed(bleedPx)
                            .background(cardRowPressedColor(pressed))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                enabled = enabled,
                                onClick = { currentPage = AppPage.AppPicker },
                            )
                            .padding(horizontal = CHOICE_ROW_HORIZONTAL_BLEED_DP.dp)
                            .padding(vertical = 4.dp),
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
        }
    }

    @Composable
    internal fun EmptyAppListCard() {
        val hasHiddenSystemApps = !showSystemApps && apps.any { AppVisibility.isSystemAppFlags(it.applicationInfo.flags) && it.packageName != packageName }
        val hintText = when {
            queryText.isNotBlank() && !showSystemApps && hasHiddenSystemApps ->
                "没有匹配“${queryText.trim()}”的应用。尝试清空搜索词或打开“显示系统应用”开关查看系统应用。"
            queryText.isNotBlank() ->
                "没有匹配“${queryText.trim()}”的应用，尝试清空搜索词。"
            hasHiddenSystemApps ->
                "当前已隐藏系统应用。打开“显示系统应用”开关可查看系统应用，或在系统设置中允许 ArtPlus 读取应用列表后刷新。"
            else ->
                "清空搜索词，或在系统设置中允许 ArtPlus 读取应用列表后刷新。"
        }
        Card(
            modifier = Modifier.fillMaxWidth().semantics {
                contentDescription = "空应用列表提示"
            },
            insideMargin = PaddingValues(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    imageVector = Lucide.Layers,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.4f)),
                )
                Text(
                    text = "没有可显示的应用",
                    style = MiuixTheme.textStyles.body1.copy(fontWeight = FontWeight.Medium),
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { contentDescription = "没有可显示的应用" },
                )
                Text(
                    text = hintText,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { contentDescription = hintText },
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (hasHiddenSystemApps && !showSystemApps) {
                    TextButton(
                        text = "显示系统应用",
                        onClick = {
                            showSystemApps = true
                            saveUiState()
                        },
                        enabled = !isBusy,
                        modifier = Modifier.fillMaxWidth().semantics {
                            contentDescription = "显示系统应用按钮"
                        },
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                TextButton(
                    text = "刷新应用列表",
                    onClick = { loadApps() },
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth().semantics {
                        contentDescription = "刷新应用列表"
                    },
                )
            }
        }
    }

    @Composable
    internal fun LocalSeparationModeControl() {
        val tuningState = mainViewModel.params.collectAsState().value
        val modes = LocalSeparationMode.entries.filterNot { it == LocalSeparationMode.Plate }
        val selectedMode = if (LocalSeparationMode.fromValue(tuningState.localSeparationMode) == LocalSeparationMode.Plate) {
            LocalSeparationMode.Full
        } else {
            LocalSeparationMode.fromValue(tuningState.localSeparationMode)
        }
        SegmentedControl(
            enabled = !isBusy,
            labels = modes.map { it.label },
            selectedIndex = modes.indexOf(selectedMode).coerceAtLeast(0),
            scrollable = true,
            onSelected = { index ->
                updateLocalSeparationMode(modes[index])
            }
        )
    }

    /** 第二层级「生成设置」：顶部「滑块 / JSON」切换 + 保存成预设 + 滑块分类导航。 */
    @Composable
    internal fun GenerationNavCard() {
        SectionCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                SegmentedControl(
                    enabled = !isBusy,
                    labels = AdvancedSettingsTab.entries.map { it.label },
                    selectedIndex = AdvancedSettingsTab.entries.indexOf(advancedSettingsTab).coerceAtLeast(0),
                    onSelected = { index ->
                        advancedSettingsTab = AdvancedSettingsTab.entries[index]
                        saveUiState()
                    },
                )
                TextButton(
                    text = "保存成预设",
                    onClick = {
                        presetSaveName = ""
                        presetSaveDialogVisible = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (advancedSettingsTab == AdvancedSettingsTab.Sliders) {
                    AdvancedCategoryTabs(
                        enabled = !isBusy,
                        selected = advancedSettingsCategory,
                        onSelected = { category ->
                            advancedSettingsCategory = category
                            saveUiState()
                        },
                    )
                }
            }
        }
    }


    @Composable
    internal fun LiquidGlassToggleCard() {
        val tuningState = mainViewModel.params.collectAsState().value
        SectionCard(rowsFullBleed = true) {
            LibrarySettingRow(
                title = "液态玻璃风格",
                summary = "开启后按当前液态玻璃参数重绘背景和前景光影",
                icon = SettingsIconKind.Glass,
                showSwitch = true,
                checked = tuningState.liquidGlassEnabled,
                enabled = !isBusy,
                onCheckedChange = { updateLiquidGlassEnabled(it) },
            )
        }
    }

    @Composable
    internal fun LiquidGlassSurfaceCard() {
        val tuningState = mainViewModel.params.collectAsState().value
        SectionCard(rowsFullBleed = true) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NumberParameterControl(
                    busy = isBusy,
                    title = "玻璃圆角",
                    summary = "控制玻璃遮罩圆角，背景与主体按同一轮廓裁剪",
                    value = tuningState.liquidGlassRadius,
                    draftText = draftLiquidGlassRadiusText,
                    min = MIN_LIQUID_GLASS_RADIUS,
                    max = MAX_LIQUID_GLASS_RADIUS,
                    onDraftChange = { draftLiquidGlassRadiusText = it },
                    onSave = { updateLiquidGlassRadius(it) },
                    icon = SettingsIconKind.Radius,
                )
                NumberParameterControl(
                    busy = isBusy,
                    title = "外框高度",
                    summary = "控制玻璃外缘高光的厚度",
                    value = tuningState.liquidGlassOuterWidth,
                    draftText = draftLiquidGlassOuterWidthText,
                    min = MIN_LIQUID_GLASS_OUTER_WIDTH,
                    max = MAX_LIQUID_GLASS_OUTER_WIDTH,
                    step = 1,
                    onDraftChange = { draftLiquidGlassOuterWidthText = it },
                    onSave = { updateLiquidGlassOuterWidth(it) },
                    icon = SettingsIconKind.Glass,
                )
                NumberParameterControl(
                    busy = isBusy,
                    title = "顶部强度",
                    summary = "控制顶边贴边高光的亮度",
                    value = tuningState.liquidGlassTopAlpha,
                    draftText = draftLiquidGlassTopAlphaText,
                    min = MIN_LIQUID_GLASS_ALPHA,
                    max = MAX_LIQUID_GLASS_ALPHA,
                    step = 1,
                    onDraftChange = { draftLiquidGlassTopAlphaText = it },
                    onSave = { updateLiquidGlassTopAlpha(it) },
                    icon = SettingsIconKind.Spark,
                )
                NumberParameterControl(
                    busy = isBusy,
                    title = "底边强度",
                    summary = "控制底边贴边高光的亮度",
                    value = tuningState.liquidGlassBottomAlpha,
                    draftText = draftLiquidGlassBottomAlphaText,
                    min = MIN_LIQUID_GLASS_ALPHA,
                    max = MAX_LIQUID_GLASS_ALPHA,
                    step = 1,
                    onDraftChange = { draftLiquidGlassBottomAlphaText = it },
                    onSave = { updateLiquidGlassBottomAlpha(it) },
                    icon = SettingsIconKind.Spark,
                )
                NumberParameterControl(
                    busy = isBusy,
                    title = "背景灰雾",
                    summary = "给图标背景叠加均匀暗雾，降低整体亮度",
                    value = tuningState.liquidGlassBackgroundMistAlpha,
                    draftText = draftLiquidGlassBackgroundMistAlphaText,
                    min = MIN_LIQUID_GLASS_MIST_ALPHA,
                    max = MAX_LIQUID_GLASS_MIST_ALPHA,
                    step = 1,
                    onDraftChange = { draftLiquidGlassBackgroundMistAlphaText = it },
                    onSave = { updateLiquidGlassBackgroundMistAlpha(it) },
                    icon = SettingsIconKind.Shadow,
                )
                NumberParameterControl(
                    busy = isBusy,
                    title = "底部灰雾",
                    summary = "给底部叠加暗雾渐变，压住底边亮度",
                    value = tuningState.liquidGlassBottomDarkAlpha,
                    draftText = draftLiquidGlassBottomDarkAlphaText,
                    min = MIN_LIQUID_GLASS_BOTTOM_DARK_ALPHA,
                    max = MAX_LIQUID_GLASS_BOTTOM_DARK_ALPHA,
                    step = 1,
                    onDraftChange = { draftLiquidGlassBottomDarkAlphaText = it },
                    onSave = { updateLiquidGlassBottomDarkAlpha(it) },
                    icon = SettingsIconKind.Shadow,
                )
            }
        }
    }

    @Composable
    internal fun LiquidGlassSubjectCard() {
        val tuningState = mainViewModel.params.collectAsState().value
        SectionCard(rowsFullBleed = true) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NumberParameterControl(
                    busy = isBusy,
                    title = "主体比例",
                    summary = "调整主体在玻璃层中的缩放比例",
                    value = tuningState.liquidGlassSubjectScalePercent,
                    draftText = draftLiquidGlassSubjectScaleText,
                    min = MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT,
                    max = MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT,
                    step = 1,
                    onDraftChange = { draftLiquidGlassSubjectScaleText = it },
                    onSave = { updateLiquidGlassSubjectScalePercent(it) },
                    icon = SettingsIconKind.Scale,
                )
                NumberParameterControl(
                    busy = isBusy,
                    title = "主体外框宽度",
                    summary = "沿主体外侧透明边界添加高光描边",
                    value = tuningState.liquidGlassSubjectOutlineWidth,
                    draftText = draftLiquidGlassSubjectOutlineWidthText,
                    min = MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                    max = MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                    step = 1,
                    onDraftChange = { draftLiquidGlassSubjectOutlineWidthText = it },
                    onSave = { updateLiquidGlassSubjectOutlineWidth(it) },
                    icon = SettingsIconKind.Spark,
                )
                NumberParameterControl(
                    busy = isBusy,
                    title = "主体内框宽度",
                    summary = "沿主体内侧透明边界添加高光描边",
                    value = tuningState.liquidGlassSubjectInnerOutlineWidth,
                    draftText = draftLiquidGlassSubjectInnerOutlineWidthText,
                    min = MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                    max = MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                    step = 1,
                    onDraftChange = { draftLiquidGlassSubjectInnerOutlineWidthText = it },
                    onSave = { updateLiquidGlassSubjectInnerOutlineWidth(it) },
                    icon = SettingsIconKind.Spark,
                )
                NumberParameterControl(
                    busy = isBusy,
                    title = "主体阴影",
                    summary = "控制主体投影透明度，增强层次",
                    value = tuningState.liquidGlassSubjectShadowAlpha,
                    draftText = draftLiquidGlassSubjectShadowAlphaText,
                    min = MIN_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA,
                    max = MAX_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA,
                    step = 1,
                    onDraftChange = { draftLiquidGlassSubjectShadowAlphaText = it },
                    onSave = { updateLiquidGlassSubjectShadowAlpha(it) },
                    icon = SettingsIconKind.Shadow,
                )
                NumberParameterControl(
                    busy = isBusy,
                    title = "主体透明度",
                    summary = "归一化主体后再控制整体不透明度",
                    value = tuningState.liquidGlassSubjectOpacityPercent,
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
    }

    @Composable
    internal fun LocalRuleTuningCard() {
        val tuningState = mainViewModel.params.collectAsState().value
        SectionCard(rowsFullBleed = true) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NumberParameterControl(
                    busy = isBusy,
                    title = "背景相似度",
                    summary = "越高越容易把相近颜色当背景",
                    value = tuningState.backgroundSeparationPercent,
                    draftText = draftBackgroundSeparationText,
                    min = MIN_BACKGROUND_SEPARATION_PERCENT,
                    max = MAX_BACKGROUND_SEPARATION_PERCENT,
                    onDraftChange = { draftBackgroundSeparationText = it },
                    onSave = { updateBackgroundSeparationPercent(it) },
                    icon = SettingsIconKind.Cutout,
                )
                NumberParameterControl(
                    busy = isBusy,
                    title = "底板清理",
                    summary = "越高越容易移除纯色底板",
                    value = tuningState.plateRemovalPercent,
                    draftText = draftPlateRemovalText,
                    min = MIN_PLATE_REMOVAL_PERCENT,
                    max = MAX_PLATE_REMOVAL_PERCENT,
                    onDraftChange = { draftPlateRemovalText = it },
                    onSave = { updatePlateRemovalPercent(it) },
                    icon = SettingsIconKind.Plate,
                )
                NumberParameterControl(
                    busy = isBusy,
                    title = "旧阴影清理",
                    summary = "清掉原图里的长阴影，不是新增阴影",
                    value = tuningState.shadowRemovalPercent,
                    draftText = draftShadowRemovalText,
                    min = MIN_SHADOW_REMOVAL_PERCENT,
                    max = MAX_SHADOW_REMOVAL_PERCENT,
                    onDraftChange = { draftShadowRemovalText = it },
                    onSave = { updateShadowRemovalPercent(it) },
                    icon = SettingsIconKind.Eraser,
                )
                NumberParameterControl(
                    busy = isBusy,
                    title = "边缘修补",
                    summary = "修补抠图毛刺和半透明边",
                    value = tuningState.edgePolishPercent,
                    draftText = draftEdgePolishText,
                    min = MIN_EDGE_POLISH_PERCENT,
                    max = MAX_EDGE_POLISH_PERCENT,
                    onDraftChange = { draftEdgePolishText = it },
                    onSave = { updateEdgePolishPercent(it) },
                    icon = SettingsIconKind.Spark,
                )
            }
        }
    }

    @Composable
    internal fun LocalWorkflowPipelineCard() {
        val tuningState = mainViewModel.params.collectAsState().value
        SectionCard(rowsFullBleed = true) {
            LocalWorkflowToggleRow("背景估计与相减", "普通图标和 Adaptive 图标的背景分离", tuningState.localBackgroundSeparationEnabled, "background")
            LocalWorkflowToggleRow("Adaptive 自动选层", "在合成前景与直接前景之间自动判断", tuningState.localAdaptiveSelectionEnabled, "adaptive")
            LocalWorkflowToggleRow("角落蒙版清理", "清理 Adaptive 四角残留", tuningState.localCornerMaskCleanupEnabled, "corner")
            LocalWorkflowToggleRow("透明边缘补色", "修复本地抠图透明边的颜色残留", tuningState.localAlphaEdgeColorRepairEnabled, "alpha_edge_repair")
            LocalWorkflowToggleRow("普通背景估计", "关闭后跳过普通背景相减与拼合", tuningState.localPlainBackgroundEstimationEnabled, "plain_background")
            LocalWorkflowToggleRow("原始前景清理", "应用原始前景的底板清理规则", tuningState.localOriginalCleanupEnabled, "original")
            LocalWorkflowToggleRow("底板清理", "检测并移除连接到边缘的底板", tuningState.localPlateCleanupEnabled, "plate")
            LocalWorkflowToggleRow("底板修边", "修复底板移除后的边缘颜色", tuningState.localPlateEdgeRepairEnabled, "plate_edge")
            LocalWorkflowToggleRow("彩色残留清理", "清除底板颜色在主体边缘的残留", tuningState.localPlateResidueCleanupEnabled, "plate_residue")
            LocalWorkflowToggleRow("长阴影清理", "移除原图中偏移的长阴影", tuningState.localShadowCleanupEnabled, "shadow")
            LocalWorkflowToggleRow("阴影边缘修复", "保留阴影交界处的抗锯齿边缘", tuningState.localShadowEdgeRepairEnabled, "shadow_edge")
            LocalWorkflowToggleRow("前景收边", "执行局部侵蚀和边缘羽化", tuningState.localEdgeTrimEnabled, "edge_trim")
            LocalWorkflowToggleRow("拼合背景候选", "生成主体与重建背景的组合候选", tuningState.localComposedBackgroundEnabled, "composed")
            LocalWorkflowToggleRow("二层候选", "运行底板/主体分层候选算法", tuningState.localTwoLayerCandidateEnabled, "two_layer")
            LocalWorkflowToggleRow("组件候选", "生成底座作为主体或背景的候选", tuningState.localComponentCandidatesEnabled, "component")
            LocalWorkflowToggleRow("字标保全候选", "保留更完整文字的安全候选", tuningState.localTextSafeCandidateEnabled, "text_safe")
            LocalWorkflowToggleRow("自动候选选择", "关闭后固定使用完整清理结果", tuningState.localAutoSelectionEnabled, "auto")
            LocalWorkflowToggleRow("本地最终边缘润色", "渲染本地候选时执行最后的边缘处理", tuningState.localEdgePolishEnabled, "edge_polish")
        }
    }

    @Composable
    internal fun LocalWorkflowToggleRow(
        title: String,
        summary: String,
        checked: Boolean,
        key: String,
    ) {
        LibrarySettingRow(
            title = title,
            summary = summary,
            icon = SettingsIconKind.Cutout,
            showSwitch = true,
            checked = checked,
            enabled = !isBusy,
            onCheckedChange = { updateLocalWorkflowToggle(key, it) },
        )
    }

    @Composable
    internal fun RmbgTuningCard() {
        val tuningState = mainViewModel.params.collectAsState().value
        SectionCard(rowsFullBleed = true) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NumberParameterControl(
                    busy = isBusy,
                    title = "Alpha 力度",
                    summary = "100 不变，越高越实",
                    value = tuningState.rmbgAlphaStrengthPercent,
                    draftText = draftRmbgAlphaStrengthText,
                    min = MIN_RMBG_ALPHA_STRENGTH_PERCENT,
                    max = MAX_RMBG_ALPHA_STRENGTH_PERCENT,
                    onDraftChange = { draftRmbgAlphaStrengthText = it },
                    onSave = { updateRmbgAlphaStrengthPercent(it) },
                    icon = SettingsIconKind.Cutout,
                )
                NumberParameterControl(
                    busy = isBusy,
                    title = "边缘柔化",
                    summary = "越高边缘越软",
                    value = tuningState.rmbgEdgeFeatherPercent,
                    draftText = draftRmbgEdgeFeatherText,
                    min = MIN_RMBG_EDGE_FEATHER_PERCENT,
                    max = MAX_RMBG_EDGE_FEATHER_PERCENT,
                    onDraftChange = { draftRmbgEdgeFeatherText = it },
                    onSave = { updateRmbgEdgeFeatherPercent(it) },
                    icon = SettingsIconKind.Cutout,
                )
                NumberParameterControl(
                    busy = isBusy,
                    title = "边缘扩缩",
                    summary = "低收缩，高扩张",
                    value = tuningState.rmbgEdgeAdjustPercent,
                    draftText = draftRmbgEdgeAdjustText,
                    min = MIN_RMBG_EDGE_ADJUST_PERCENT,
                    max = MAX_RMBG_EDGE_ADJUST_PERCENT,
                    onDraftChange = { draftRmbgEdgeAdjustText = it },
                    onSave = { updateRmbgEdgeAdjustPercent(it) },
                    icon = SettingsIconKind.Scale,
                )
                NumberParameterControl(
                    busy = isBusy,
                    title = "弱透明保留",
                    summary = "越高越保留半透明细节",
                    value = tuningState.rmbgWeakAlphaKeepPercent,
                    draftText = draftRmbgWeakAlphaKeepText,
                    min = MIN_RMBG_WEAK_ALPHA_KEEP_PERCENT,
                    max = MAX_RMBG_WEAK_ALPHA_KEEP_PERCENT,
                    onDraftChange = { draftRmbgWeakAlphaKeepText = it },
                    onSave = { updateRmbgWeakAlphaKeepPercent(it) },
                    icon = SettingsIconKind.Cutout,
                )
            }
        }
    }

    @Composable
    internal fun JsonSettingsEditorCard() {
        SectionCard {
            JsonSettingsEditor()
        }
    }

    // ---------- 预设：保存 / 应用 / 批量 / 导入导出 ----------

    internal fun refreshPresets() {
        presetListVersion += 1
        val stored = presetStore.activePresetId
        val preset = if (stored != null) presetStore.get(stored) else null
        activePresetId = preset?.id
        activePresetBaseParams = preset?.params
    }

    internal fun loadPresetState() {
        refreshPresets()
        batchOutputMode = runCatching {
            BatchOutputMode.valueOf(getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(PREF_BATCH_OUTPUT_MODE, BatchOutputMode.Root.name) ?: BatchOutputMode.Root.name)
        }.getOrDefault(BatchOutputMode.Root)
        gptRunCount = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(PREF_GPT_RUN_COUNT, 0)
        rmbgRunCount = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(PREF_RMBG_RUN_COUNT, 0)
    }

    internal fun incrementGptRunCount() {
        gptRunCount += 1
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putInt(PREF_GPT_RUN_COUNT, gptRunCount)
            .apply()
    }

    internal fun incrementRmbgRunCount() {
        rmbgRunCount += 1
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putInt(PREF_RMBG_RUN_COUNT, rmbgRunCount)
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
        pendingServiceConfirm = ServiceConfirmRequest(
            title = title,
            message = message,
            confirmLabel = confirmLabel,
            onConfirm = onConfirm,
        )
    }

    internal fun dismissServiceConfirm(confirmed: Boolean) {
        val request = pendingServiceConfirm ?: return
        pendingServiceConfirm = null
        if (confirmed) {
            request.onConfirm()
        }
    }

    internal fun saveCurrentAsPreset(rawName: String) {
        // P2 交界：预设域收敛进 MainViewModel，这里只做 UI 状态（文案/镜像/版本/弹窗）。
        when (val outcome = mainViewModel.savePreset(presetStore, currentTuningParams(), rawName)) {
            SavePresetOutcome.BlankName -> {
                statusText = "预设名称不能为空"
                return
            }
            is SavePresetOutcome.DuplicateName -> {
                statusText = "预设「${outcome.name}」已存在，请换一个名称"
                return
            }
            is SavePresetOutcome.Saved -> {
                val preset = outcome.preset
                activePresetId = preset.id
                activePresetBaseParams = preset.params
                presetListVersion += 1
                presetSaveDialogVisible = false
                statusText = "已保存预设「${preset.name}」（${preset.params.toParamMap().size} 项参数）"
            }
        }
    }

    internal fun overwritePreset(preset: TuningPreset) {
        // P2 交界：预设域收敛进 MainViewModel，这里只做 UI 状态。
        val current = currentTuningParams()
        if (!mainViewModel.overwritePreset(presetStore, preset, current)) {
            statusText = "更新预设失败"
            return
        }
        activePresetId = preset.id
        activePresetBaseParams = current
        presetListVersion += 1
        statusText = "已覆盖更新预设「${preset.name}」"
    }

    internal fun resetToPreset(preset: TuningPreset) {
        if (isBusy || isGeneratingGptCandidate || isGeneratingRmbgCandidate) {
            statusText = "当前有任务在运行，请等待"
            return
        }
        val before = currentTuningParams()
        // P2 交界：预设合并收敛进 MainViewModel。
        val merged = mainViewModel.mergedPresetParams(preset, before)
        applyTuningParams(merged, rebuildCandidates = true)
        presetStore.activePresetId = preset.id
        activePresetId = preset.id
        activePresetBaseParams = preset.params
        statusText = "已重置回预设「${preset.name}」初始参数"
    }

    internal fun applyPreset(preset: TuningPreset) {
        if (isBusy || isGeneratingGptCandidate || isGeneratingRmbgCandidate) {
            statusText = "当前有任务在运行，请等待"
            return
        }
        val before = currentTuningParams()
        // P2 交界：预设合并收敛进 MainViewModel。
        val merged = mainViewModel.mergedPresetParams(preset, before)
        applyTuningParams(merged, rebuildCandidates = true)
        presetStore.activePresetId = preset.id
        activePresetId = preset.id
        activePresetBaseParams = preset.params
        statusText = "已应用预设「${preset.name}」，${before.diffSummary(merged)}"
    }

    internal fun deletePreset(id: String) {
        BatchPreviewStore.deleteSnapshot(filesDir, id)
        if (activeBatchPreviewPreset?.id == id || batchPreviewResult?.preset?.id == id) {
            activeBatchPreviewPreset = null
            batchPreviewResult = null
            if (currentPage == AppPage.BatchPreview) {
                currentPage = AppPage.Home
            }
        }
        // P2 交界：store 删除收敛进 MainViewModel；BatchPreview/页面/UI 状态留 Activity。
        mainViewModel.deletePreset(presetStore, id)
        if (activePresetId == id) {
            activePresetId = null
            activePresetBaseParams = null
        }
        presetListVersion += 1
        statusText = "已删除预设"
    }

    internal fun renamePreset(id: String, rawName: String) {
        // P2 交界：预设域收敛进 MainViewModel，这里只做 UI 状态。
        when (val outcome = mainViewModel.renamePreset(presetStore, id, rawName)) {
            RenamePresetOutcome.BlankName -> {
                statusText = "预设名称不能为空"
                return
            }
            RenamePresetOutcome.Failed -> {
                statusText = "重命名失败：名称与现有预设重复"
                return
            }
            is RenamePresetOutcome.Renamed -> {
                presetListVersion += 1
                statusText = "已重命名为「${outcome.name}」"
            }
        }
    }

    internal fun exportPresetsToClipboard() {
        val json = presetStore.exportJson()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        if (clipboard == null) {
            statusText = "剪贴板不可用"
            return
        }
        clipboard.setPrimaryClip(ClipData.newPlainText("ArtPlus预设", json))
        statusText = "已复制 ${presetStore.all().size} 条预设 JSON 到剪贴板"
    }

    internal fun exportSinglePresetToClipboard(preset: TuningPreset) {
        val json = presetStore.exportSingleJson(preset)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        if (clipboard == null) {
            statusText = "剪贴板不可用"
            return
        }
        clipboard.setPrimaryClip(ClipData.newPlainText("ArtPlus预设-${preset.name}", json))
        statusText = "已复制预设「${preset.name}」JSON 到剪贴板"
    }

    internal fun importPresetsFromText(text: String) {
        val result = presetStore.importJson(text)
        presetImportDialogVisible = false
        presetImportText = ""
        presetListVersion += 1
        statusText = if (result.errors.isEmpty()) {
            "已导入 ${result.imported} 条预设"
        } else {
            "已导入 ${result.imported} 条，失败 ${result.errors.size} 条：${result.errors.firstOrNull().orEmpty()}"
        }
    }

    /** JSON 编辑器：解析文本为 TuningParams 并应用（缺失键保持当前值）。 */
    internal fun saveJsonParamsFromText(text: String) {
        val json = runCatching { JSONObject(text) }.getOrElse { error ->
            statusText = "JSON 解析失败：${error.message ?: error.javaClass.simpleName}"
            return
        }
        val params = TuningParams.fromJson(json, currentTuningParams())
        if (params == null) {
            statusText = "JSON 参数解析失败"
            return
        }
        applyTuningParams(params, rebuildCandidates = true)
        statusText = "已应用 JSON 参数"
    }

    internal fun startBatchPreview(preset: TuningPreset) {
        if (isBusy || isGeneratingGptCandidate || isGeneratingRmbgCandidate || isGeneratingBatchPreview) {
            statusText = "当前有任务在运行，请等待"
            return
        }
        val sampledApps = BatchPreviewSampler.sample(
            candidates = apps,
            generatedPackageNames = generatedPackageNames,
            count = batchPreviewCount,
            selfPackageName = packageName,
        )
        if (sampledApps.isEmpty()) {
            statusText = "未找到可供预览的启动器应用"
            return
        }

        isBusy = true
        isGeneratingBatchPreview = true
        batchPreviewCancelled = false

        val originalParams = currentTuningParams()
        val merged = TuningParams.fromParamMap(preset.params.toParamMap(), originalParams)
        applyTuningParams(merged, rebuildCandidates = false, persist = false, captureUndo = false, refreshPreview = false)
        val pipeline = currentLocalPipelineConfig()

        batchPreviewProgress = BatchPreviewProgress(
            presetName = preset.name,
            completed = 0,
            total = sampledApps.size,
            currentLabel = "准备渲染 ${sampledApps.size} 个应用",
        )
        statusText = "预设「${preset.name}」批量预览渲染中..."

        startUiFriendlyThread("ArtPlusBatchPreview") {
            val items = mutableListOf<BatchPreviewItem>()
            var wasCancelled = false
            try {
                for ((index, app) in sampledApps.withIndex()) {
                    if (batchPreviewCancelled) {
                        wasCancelled = true
                        break
                    }
                    runOnUiThread {
                        batchPreviewProgress = BatchPreviewProgress(
                            presetName = preset.name,
                            completed = index,
                            total = sampledApps.size,
                            currentLabel = "渲染中: ${app.label}",
                        )
                    }
                    try {
                        val assets = generateMemoryPreviewAssetsForApp(app, pipeline)
                        items.add(BatchPreviewItem(packageName = app.packageName, label = app.label, assets = assets))
                    } catch (e: Throwable) {
                        // 跳过单应用渲染异常
                    }
                    runOnUiThread {
                        batchPreviewProgress = BatchPreviewProgress(
                            presetName = preset.name,
                            completed = index + 1,
                            total = sampledApps.size,
                            currentLabel = "已完成: ${app.label}",
                        )
                    }
                }
                runOnUiThread {
                    if (!wasCancelled && items.isNotEmpty()) {
                        val result = BatchPreviewResult(preset = preset, items = items)
                        batchPreviewResult = result
                        activeBatchPreviewPreset = preset

                        // 持久化保存快照到磁盘
                        val dataList = items.map { item ->
                            BatchPreviewItemData(
                                packageName = item.packageName,
                                label = item.label,
                                recbg = item.assets.recbg,
                                recfg = item.assets.recfg,
                                recNight = item.assets.recNight,
                                monochromeLight = item.assets.monochromeLight,
                                monochromeDark = item.assets.monochromeDark,
                            )
                        }
                        BatchPreviewStore.saveSnapshot(filesDir, preset, dataList)

                        currentPage = AppPage.BatchPreview
                        statusText = "已生成预设「${preset.name}」批量预览并保存快照 (${items.size} 个应用)"
                    } else if (wasCancelled) {
                        statusText = "已取消批量预览"
                    } else {
                        statusText = "批量预览生成失败"
                    }
                }
            } finally {
                runOnUiThread {
                    applyTuningParams(originalParams, rebuildCandidates = true, persist = false, captureUndo = false, refreshPreview = true)
                    isBusy = false
                    isGeneratingBatchPreview = false
                    batchPreviewProgress = null
                    batchPreviewCancelled = false
                }
            }
        }
    }

    internal fun openBatchPreviewForPreset(preset: TuningPreset) {
        activeBatchPreviewPreset = preset
        if (BatchPreviewStore.hasSnapshot(filesDir, preset.id)) {
            // P4 交界：快照读取收敛进 pipeline/，显式传 filesDir。
            val cached = loadBatchPreviewSnapshot(filesDir, preset)
            if (cached != null) {
                batchPreviewResult = cached
                currentPage = AppPage.BatchPreview
                statusText = "已加载预设「${preset.name}」批量预览快照"
                return
            }
        }
        presetBatchPreviewConfirmTarget = preset
    }

    internal fun generateMemoryPreviewAssetsForApp(
        app: AppEntry,
        pipeline: LocalPipelineConfig,
    ): PreviewAssets {
        val icon = app.applicationInfo.loadIcon(packageManager)
        val localSourceIcon = drawLocalCandidateSourceIcon(icon, SIZE_1X1, SIZE_1X1)
        val localSource = buildLocalIconLayers(icon, pipeline, mainViewModel.params.value.backgroundSeparationPercent, AdaptiveForegroundMode.fromValue(mainViewModel.params.value.adaptiveForegroundMode), mainViewModel.params.value.adaptiveDirectMaxCoveragePercent, mainViewModel.params.value.adaptiveDirectMaxCoverageIncreasePercent, mainViewModel.params.value.adaptiveMaskEdgeCoveragePercent, mainViewModel.params.value.adaptiveMaskMinCoveragePercent, mainViewModel.params.value.adaptiveCenterEpsilonPercent)
        val localCandidateSet = buildLocalCandidates(localSource, localSourceIcon, pipeline, OriginalForegroundCleanupMode.fromValue(mainViewModel.params.value.originalForegroundCleanupMode), mainViewModel.params.value.plateRemovalPercent, mainViewModel.params.value.shadowRemovalPercent, mainViewModel.params.value.backgroundSeparationPercent)
        val localCandidates = localCandidateSet.candidates
        val defaultChoice = defaultPreviewChoiceForMode(LocalSeparationMode.fromValue(mainViewModel.params.value.localSeparationMode), localCandidateSet.autoChoice)
            .takeIf { localCandidates.containsKey(it) }
            ?: localCandidateSet.autoChoice.takeIf { localCandidates.containsKey(it) }
            ?: PreviewChoice.Original
        val selections = PreviewSelections.default(defaultChoice)
        val dummyOutDir = File(cacheDir, "preview_tmp")
        val session = GenerationSession(
            packageName = app.packageName,
            outDir = dummyOutDir,
            sourceIcon = localSourceIcon,
            baseRecfg = localSource.recfg,
            baseRecbg = localSource.recbg,
            monochromeRaw = localSource.monochrome,
            candidates = localCandidates,
            autoLocalChoice = localCandidateSet.autoChoice,
        )
        return previewAssetsForSelections(session, selections).preparedForDraw()
    }

    internal fun applyPresetToSelectedApps(preset: TuningPreset) {
        val session = activeGenerationSession
        val batchPackageNames = if (multiSelectedPackageNames.isNotEmpty()) {
            multiSelectedPackageNames.toList().sorted()
        } else {
            listOfNotNull(session?.packageName)
        }
        if (batchPackageNames.isEmpty()) {
            statusText = "先在应用页多选或选中一个应用"
            return
        }
        if (isBusy || isGeneratingGptCandidate || isGeneratingRmbgCandidate) {
            statusText = "当前有任务在运行，请等待"
            return
        }
        requestServiceConfirm(
            title = "确认套用预设",
            message = "将按预设「${preset.name}」批量处理 ${batchPackageNames.size} 个应用，会覆盖现有图标并写入对应分区，确认继续？",
            confirmLabel = "确认套用",
        ) {
            executeApplyPresetToSelectedApps(preset, batchPackageNames)
        }
        return
    }

    internal fun executeApplyPresetToSelectedApps(preset: TuningPreset, batchPackageNames: List<String>) {
        if (isBusy || isGeneratingGptCandidate || isGeneratingRmbgCandidate) {
            statusText = "当前有任务在运行，请等待"
            return
        }
        isBusy = true
        previewChoiceMode = null
        batchApplyProgress = BatchApplyProgress(
            title = "预设批量应用",
            completed = 0,
            total = batchPackageNames.size,
            currentLabel = "准备处理 ${batchPackageNames.size} 个 APK",
            failures = 0,
        )
        statusText = "预设「${preset.name}」批量处理中: 0/${batchPackageNames.size}"
        val beforeParams = currentTuningParams()
        applyTuningParams(
            TuningParams.fromParamMap(preset.params.toParamMap(), beforeParams),
            rebuildCandidates = false,
        )
        presetStore.activePresetId = preset.id
        activePresetId = preset.id
        val outputUri = outputTreeUri
        val selectedAtStart = selectedPackageName
        startUiFriendlyThread("ArtPlusPresetBatch") {
            val successes = mutableListOf<String>()
            val failures = mutableListOf<String>()
            var selectedResult: GenerationResult? = null
            try {
                batchPackageNames.forEachIndexed { index, packageName ->
                    val app = apps.firstOrNull { it.packageName == packageName }
                    if (app == null) {
                        failures += "$packageName: 应用不存在"
                        updateBatchApplyProgress(
                            completed = index + 1,
                            total = batchPackageNames.size,
                            currentLabel = "跳过: $packageName",
                            failures = failures.size,
                        )
                        return@forEachIndexed
                    }
                    updateBatchApplyProgress(
                        completed = index,
                        total = batchPackageNames.size,
                        currentLabel = "处理中: ${app.label} ($packageName)",
                        failures = failures.size,
                    )
                    try {
                        val result = generateArtPlusPackage(app, useGpt = false)
                        if (false && outputUri != null) {
                            exportToTree(contentResolver, outputUri, result.outDir)
                        }
                        successes += packageName
                        if (packageName == selectedAtStart) {
                            selectedResult = result
                        }
                    } catch (error: Throwable) {
                        failures += "$packageName: ${error.message ?: error.javaClass.simpleName}"
                    }
                    updateBatchApplyProgress(
                        completed = index + 1,
                        total = batchPackageNames.size,
                        currentLabel = "已完成: ${app.label} ($packageName)",
                        failures = failures.size,
                    )
                }
                runOnUiThread {
                    if (successes.isNotEmpty()) {
                        generatedPackageNames = updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), generatedPackageNames + successes)
                        multiSelectedPackageNames = multiSelectedPackageNames - successes.toSet()
                    }
                    val result = selectedResult
                    if (result != null && selectedPackageName == selectedAtStart) {
                        activeGenerationSession = result.session
                        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (result.selections).normalLight.name, previewNormalDark = (result.selections).normalDark.name, previewMonochromeLight = (result.selections).monochromeLight.name, previewMonochromeDark = (result.selections).monochromeDark.name) }
                        previewChoiceMode = null
                        previewPackageName = result.session.packageName
                        previewDirPath = result.outDir.absolutePath
                        previewVersion += 1
                        saveUiState()
                    }
                    statusText = when {
                        failures.isEmpty() -> "预设「${preset.name}」批量完成: ${successes.size}/${batchPackageNames.size}"
                        successes.isEmpty() -> "预设批量失败: ${failures.firstOrNull().orEmpty()}"
                        else -> "预设批量完成 ${successes.size} 个，失败 ${failures.size} 个：${failures.firstOrNull().orEmpty()}"
                    }
                }
            } finally {
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

    /** 在 APK 选择页多选态下套用当前预设/当前调参批量生成（本地）。 */
    internal fun applyCurrentPresetBatch() {
        val preset = activePresetId?.let { presetStore.get(it) }
        if (preset != null) {
            applyPresetToSelectedApps(preset)
            return
        }
        // 未选择任何预设：直接按当前调参批量生成
        val batchPackageNames = multiSelectedPackageNames.toList().sorted()
        if (batchPackageNames.isEmpty()) {
            statusText = "先在应用页多选要批量处理的应用"
            return
        }
        if (isBusy || isGeneratingGptCandidate || isGeneratingRmbgCandidate) {
            statusText = "当前有任务在运行，请等待"
            return
        }
        requestServiceConfirm(
            title = "确认套用预设",
            message = "将按当前调参批量处理 ${batchPackageNames.size} 个应用，会覆盖现有图标并写入对应分区，确认继续？",
            confirmLabel = "确认套用",
        ) {
            executeApplyCurrentBatch(batchPackageNames)
        }
        return
    }

    internal fun executeApplyCurrentBatch(batchPackageNames: List<String>) {
        if (isBusy || isGeneratingGptCandidate || isGeneratingRmbgCandidate) {
            statusText = "当前有任务在运行，请等待"
            return
        }
        isBusy = true
        previewChoiceMode = null
        batchApplyProgress = BatchApplyProgress(
            title = "批量生成",
            completed = 0,
            total = batchPackageNames.size,
            currentLabel = "准备处理 ${batchPackageNames.size} 个 APK",
            failures = 0,
        )
        statusText = "按当前调参批量处理中: 0/${batchPackageNames.size}"
        val outputUri = outputTreeUri
        val selectedAtStart = selectedPackageName
        startUiFriendlyThread("ArtPlusCurrentBatch") {
            val successes = mutableListOf<String>()
            val failures = mutableListOf<String>()
            var selectedResult: GenerationResult? = null
            try {
                batchPackageNames.forEachIndexed { index, packageName ->
                    val app = apps.firstOrNull { it.packageName == packageName }
                    if (app == null) {
                        failures += "$packageName: 应用不存在"
                        updateBatchApplyProgress(
                            completed = index + 1,
                            total = batchPackageNames.size,
                            currentLabel = "跳过: $packageName",
                            failures = failures.size,
                        )
                        return@forEachIndexed
                    }
                    updateBatchApplyProgress(
                        completed = index,
                        total = batchPackageNames.size,
                        currentLabel = "处理中: ${app.label} ($packageName)",
                        failures = failures.size,
                    )
                    try {
                        val result = generateArtPlusPackage(app, useGpt = false)
                        if (false && outputUri != null) {
                            exportToTree(contentResolver, outputUri, result.outDir)
                        }
                        successes += packageName
                        if (packageName == selectedAtStart) {
                            selectedResult = result
                        }
                    } catch (error: Throwable) {
                        failures += "$packageName: ${error.message ?: error.javaClass.simpleName}"
                    }
                    updateBatchApplyProgress(
                        completed = index + 1,
                        total = batchPackageNames.size,
                        currentLabel = "已完成: ${app.label} ($packageName)",
                        failures = failures.size,
                    )
                }
                runOnUiThread {
                    if (successes.isNotEmpty()) {
                        generatedPackageNames = updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), generatedPackageNames + successes)
                        multiSelectedPackageNames = multiSelectedPackageNames - successes.toSet()
                    }
                    val result = selectedResult
                    if (result != null && selectedPackageName == selectedAtStart) {
                        activeGenerationSession = result.session
                        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (result.selections).normalLight.name, previewNormalDark = (result.selections).normalDark.name, previewMonochromeLight = (result.selections).monochromeLight.name, previewMonochromeDark = (result.selections).monochromeDark.name) }
                        previewChoiceMode = null
                        previewPackageName = result.session.packageName
                        previewDirPath = result.outDir.absolutePath
                        previewVersion += 1
                        saveUiState()
                    }
                    statusText = when {
                        failures.isEmpty() -> "按当前调参批量完成: ${successes.size}/${batchPackageNames.size}"
                        successes.isEmpty() -> "批量失败: ${failures.firstOrNull().orEmpty()}"
                        else -> "批量完成 ${successes.size} 个，失败 ${failures.size} 个：${failures.firstOrNull().orEmpty()}"
                    }
                }
            } finally {
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


    @Composable
    internal fun PresetStatusCard() {
        val presets = remember(presetListVersion) { presetStore.all() }
        val activePreset = presets.firstOrNull { it.id == activePresetId }
        val currentParams = currentTuningParams()
        val isPresetModified = activePreset != null && (activePresetBaseParams == null || !currentParams.sameAs(activePresetBaseParams ?: activePreset.params))

        SectionCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (activePreset != null) {
                                    if (isPresetModified) MiuixTheme.colorScheme.primaryVariant.copy(alpha = 0.6f) else MiuixTheme.colorScheme.primaryVariant
                                } else {
                                    MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.4f)
                                }
                            ),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = when {
                                    activePreset != null -> "当前生效：${activePreset.name}"
                                    else -> "当前生效：自定义调参"
                                },
                                style = MiuixTheme.textStyles.title4.copy(fontWeight = FontWeight.Bold),
                                color = MiuixTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (activePreset != null && isPresetModified) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = "已修改",
                                        style = MiuixTheme.textStyles.footnote2.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                        ),
                                        color = MiuixTheme.colorScheme.primaryVariant,
                                    )
                                }
                            }
                        }
                        Text(
                            text = when {
                                activePreset != null && isPresetModified -> "与快照有参数差异 · ${currentParams.diffSummary(activePreset.params)}"
                                activePreset != null -> "与快照保持一致 · 更新于 ${formatPresetDate(activePreset.updatedAt)}"
                                else -> "未绑定预设快照 · 可保存为独立快照"
                            },
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                if (activePreset != null && isPresetModified) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TextButton(
                                text = "覆盖更新",
                                onClick = { overwritePreset(activePreset) },
                                enabled = !isBusy,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(
                                text = "另存为",
                                onClick = {
                                    presetSaveName = "${activePreset.name} (副本)"
                                    presetSaveDialogVisible = true
                                },
                                enabled = !isBusy,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        TextButton(
                            text = "重置到快照",
                            onClick = { resetToPreset(activePreset) },
                            enabled = !isBusy,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else if (activePreset != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TextButton(
                            text = "另存为",
                            onClick = {
                                presetSaveName = "${activePreset.name} (副本)"
                                presetSaveDialogVisible = true
                            },
                            enabled = !isBusy,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = "恢复出厂",
                            onClick = { resetToDefaults() },
                            enabled = !isBusy,
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TextButton(
                            text = "保存为预设",
                            onClick = {
                                presetSaveName = ""
                                presetSaveDialogVisible = true
                            },
                            enabled = !isBusy,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = "恢复出厂",
                            onClick = { resetToDefaults() },
                            enabled = !isBusy,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }

    @Composable
    internal fun PresetLibraryCard() {
        val presets = remember(presetListVersion) { presetStore.all() }
        val activePreset = presets.firstOrNull { it.id == activePresetId }
        val currentParams = currentTuningParams()

        val filtered = remember(presets, presetSearchQuery) {
            if (presetSearchQuery.isBlank()) presets
            else presets.filter { it.name.contains(presetSearchQuery.trim(), ignoreCase = true) }
        }

        val displayList = remember(filtered, activePresetId, presetListExpanded, presetSearchQuery) {
            if (presetSearchQuery.isNotBlank() || presetListExpanded || filtered.size <= 5) {
                filtered
            } else {
                val result = mutableListOf<TuningPreset>()
                val active = filtered.firstOrNull { it.id == activePresetId }
                if (active != null) {
                    result.add(active)
                }
                filtered.forEach { p ->
                    if (p.id != activePresetId && result.size < 5) {
                        result.add(p)
                    }
                }
                result
            }
        }

        SectionCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "预设快照库 (${presets.size})",
                        style = MiuixTheme.textStyles.title4.copy(fontWeight = FontWeight.Bold),
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                    if (presets.isNotEmpty()) {
                        Text(
                            text = "轻按条目套用",
                            style = MiuixTheme.textStyles.footnote2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }

                if (presets.size >= 8) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Image(
                            imageVector = Lucide.Search,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantSummary),
                        )
                        BasicTextField(
                            value = presetSearchQuery,
                            onValueChange = { presetSearchQuery = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MiuixTheme.textStyles.body2.copy(
                                color = MiuixTheme.colorScheme.onSurface,
                            ),
                            cursorBrush = SolidColor(MiuixTheme.colorScheme.primaryVariant),
                            decorationBox = { innerTextField ->
                                if (presetSearchQuery.isEmpty()) {
                                    Text(
                                        text = "搜索预设名称...",
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.6f),
                                    )
                                }
                                innerTextField()
                            },
                        )
                        if (presetSearchQuery.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .clickable { presetSearchQuery = "" },
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    imageVector = Lucide.X,
                                    contentDescription = "清除",
                                    modifier = Modifier.size(14.dp),
                                    colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantSummary),
                                )
                            }
                        }
                    }
                }

                if (presets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                imageVector = Lucide.Layers,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.4f)),
                            )
                            Text(
                                text = "暂无预设快照",
                                style = MiuixTheme.textStyles.body1.copy(fontWeight = FontWeight.Medium),
                                color = MiuixTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "在「生成参数」微调好效果后，点击上方「保存为预设」即可创建",
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp),
                            )
                        }
                    }
                } else if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "未找到包含「$presetSearchQuery」的预设",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        displayList.forEach { preset ->
                            val isActive = preset.id == activePresetId
                            val isModified = isActive && (activePresetBaseParams == null || !currentParams.sameAs(activePresetBaseParams ?: preset.params))
                            CompactPresetRow(
                                busy = isBusy,
                                preset = preset,
                                isActive = isActive,
                                isModified = isModified,
                                onApply = { applyPreset(preset) },
                                onPreview = { openBatchPreviewForPreset(preset) },
                                onMore = { presetActionMenuTarget = preset },
                            )
                        }

                        if (presets.size > 5 && presetSearchQuery.isBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { presetListExpanded = !presetListExpanded }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = if (presetListExpanded) "收起 ▴" else "展开查看全部 (共 ${presets.size} 个) ▾",
                                    style = MiuixTheme.textStyles.footnote1.copy(fontWeight = FontWeight.Medium),
                                    color = MiuixTheme.colorScheme.primaryVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    internal fun WallpaperSettingsCard() {
        val hasCustom = customWallpaperPath != null
        SectionCard(rowsFullBleed = true) {
            LibrarySettingRow(
                title = "上传自定义壁纸",
                summary = if (hasCustom) {
                    "已上传${customWallpaperInfo.takeIf { it.isNotBlank() }?.let { "（$it）" }.orEmpty()}，「桌面」背景优先使用 · 自动居中裁剪 16:9（不缩放不变形）"
                } else {
                    "「桌面」背景当前用系统壁纸/内置壁纸 · 上传后自动居中裁剪 16:9（不缩放不变形）"
                },
                icon = SettingsIconKind.FileUpload,
                showValue = false,
                showArrowRight = true,
                enabled = !isBusy,
                onClick = {
                    chooseWallpaperLauncher.launch(arrayOf("image/jpeg", "image/png", "image/webp"))
                },
            )
            if (hasCustom) {
                LibrarySettingRow(
                    title = "清除自定义壁纸",
                    summary = "恢复为系统壁纸/内置壁纸",
                    icon = SettingsIconKind.Eraser,
                    showValue = false,
                    showArrowRight = true,
                    enabled = !isBusy,
                    onClick = { clearCustomWallpaper() },
                )
            }
        }
    }

    @Composable
    internal fun BatchPreviewSettingsCard() {
        SectionCard(rowsFullBleed = true) {
            NumberParameterControl(
                busy = isBusy,
                title = "批量预览数量",
                summary = "预设四风格宫格预览时随机抓取的应用数量（默认 20，优先未生成图标应用）",
                value = batchPreviewCount,
                draftText = draftBatchPreviewCountText,
                min = MIN_BATCH_PREVIEW_COUNT,
                max = MAX_BATCH_PREVIEW_COUNT,
                step = 1,
                onDraftChange = { draftBatchPreviewCountText = it },
                onSave = { updateBatchPreviewCount(it) },
                icon = SettingsIconKind.Grid,
            )
        }
    }


    @Composable
    internal fun PresetActionMenuDialog(
        target: TuningPreset,
        onDismiss: () -> Unit,
    ) {
        MiuixBottomDialog(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MiuixTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "预设选项：${target.name}",
                    style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                    color = MiuixTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                        .padding(vertical = 8.dp),
                ) {
                    PresetMenuItem(
                        enabled = !isBusy,
                        title = "应用此预设",
                        summary = "载入此快照的全部调参设置",
                        onClick = {
                            onDismiss()
                            applyPreset(target)
                        },
                    )
                    PresetMenuItem(
                        enabled = !isBusy,
                        title = "批量四风格预览",
                        summary = "随机抓取应用，四种风格宫格预览",
                        onClick = {
                            onDismiss()
                            openBatchPreviewForPreset(target)
                        },
                    )
                    PresetMenuItem(
                        enabled = !isBusy,
                        title = "覆盖为此预设",
                        summary = "将当前所有调参保存覆盖到「${target.name}」",
                        onClick = {
                            onDismiss()
                            overwritePreset(target)
                        },
                    )
                    PresetMenuItem(
                        enabled = !isBusy,
                        title = "重命名",
                        summary = "修改该预设名称",
                        onClick = {
                            onDismiss()
                            presetRenameTarget = target
                        },
                    )
                    PresetMenuItem(
                        enabled = !isBusy,
                        title = "复制单条 JSON",
                        summary = "导出该预设快照到剪贴板，方便分享",
                        onClick = {
                            onDismiss()
                            exportSinglePresetToClipboard(target)
                        },
                    )
                    PresetMenuItem(
                        enabled = !isBusy,
                        title = "删除预设",
                        summary = "从预设库中彻底移除",
                        onClick = {
                            onDismiss()
                            presetDeleteConfirmTarget = target
                        },
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(),
                ) {
                    Text(
                        text = "取消",
                        style = MiuixTheme.textStyles.button,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }


    @Composable
    internal fun PresetDeleteConfirmDialog(
        target: TuningPreset,
        onDismiss: () -> Unit,
    ) {
        MiuixBottomDialog(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MiuixTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "删除预设",
                    style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Text(
                    text = "确定要删除预设「${target.name}」吗？此操作不可撤销。",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(),
                    ) {
                        Text(
                            text = "取消",
                            style = MiuixTheme.textStyles.button,
                            color = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                    Button(
                        onClick = {
                            onDismiss()
                            deletePreset(target.id)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary(),
                    ) {
                        Text(
                            text = "确认删除",
                            style = MiuixTheme.textStyles.button,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }

    @Composable
    internal fun PresetPageDialogs() {
        if (presetSaveDialogVisible) {
            PresetNameDialog(
                title = "保存当前为预设",
                initialName = presetSaveName,
                confirmLabel = "保存",
                onConfirm = { name ->
                    presetSaveDialogVisible = false
                    saveCurrentAsPreset(name)
                },
                onDismiss = { presetSaveDialogVisible = false },
            )
        }
        presetRenameTarget?.let { target ->
            PresetNameDialog(
                title = "重命名预设",
                initialName = target.name,
                confirmLabel = "重命名",
                onConfirm = { name ->
                    presetRenameTarget = null
                    renamePreset(target.id, name)
                },
                onDismiss = { presetRenameTarget = null },
            )
        }
        presetActionMenuTarget?.let { target ->
            PresetActionMenuDialog(
                target = target,
                onDismiss = { presetActionMenuTarget = null },
            )
        }
        presetDeleteConfirmTarget?.let { target ->
            PresetDeleteConfirmDialog(
                target = target,
                onDismiss = { presetDeleteConfirmTarget = null },
            )
        }
        if (presetImportDialogVisible) {
            PresetImportDialog(
                onConfirm = { text -> importPresetsFromText(text) },
                onDismiss = { presetImportDialogVisible = false },
            )
        }
        presetBatchPreviewConfirmTarget?.let { target ->
            PresetBatchPreviewConfirmDialog(
                preset = target,
                onConfirm = {
                    presetBatchPreviewConfirmTarget = null
                    startBatchPreview(target)
                },
                onDismiss = { presetBatchPreviewConfirmTarget = null },
            )
        }
        PresetBatchPreviewProgressDialog()
        if (showBatchPreviewRefreshConfirm) {
            val targetPreset = activeBatchPreviewPreset ?: batchPreviewResult?.preset
            if (targetPreset != null) {
                BatchPreviewRefreshConfirmDialog(
                    preset = targetPreset,
                    onConfirm = {
                        showBatchPreviewRefreshConfirm = false
                        startBatchPreview(targetPreset)
                    },
                    onDismiss = { showBatchPreviewRefreshConfirm = false },
                )
            }
        }
    }

    @Composable
    internal fun PresetBatchPreviewConfirmDialog(
        preset: TuningPreset,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        MiuixBottomDialog(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MiuixTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "生成批量预览",
                    style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                    color = MiuixTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "将根据预设「${preset.name}」随机抽取 ${batchPreviewCount} 个应用（优先未生成图标），批量生成正常亮色、正常暗色、单色亮色与单色暗色共 ${batchPreviewCount * 4} 个图标供宫格预览。\n\n此过程仅在内存中生成预览，绝不写入分区或更改任何文件。确认开始？",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(),
                    ) {
                        Text(
                            text = "取消",
                            style = MiuixTheme.textStyles.button,
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary(),
                    ) {
                        Text(
                            text = "开始生成",
                            style = MiuixTheme.textStyles.button,
                            color = Color.White,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }

    @Composable
    internal fun PresetBatchPreviewProgressDialog() {
        val progress = batchPreviewProgress ?: return
        val fraction = if (progress.total <= 0) 0f else (progress.completed.toFloat() / progress.total.toFloat()).coerceIn(0f, 1f)
        MiuixBottomDialog(onDismissRequest = { batchPreviewCancelled = true }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MiuixTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "正在生成批量预览",
                    style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                    color = MiuixTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "预设「${progress.presetName}」· 进度 ${progress.completed}/${progress.total}",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = progress.currentLabel,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                LinearProgressIndicator(
                    progress = fraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp)),
                )
                Button(
                    onClick = { batchPreviewCancelled = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(),
                ) {
                    Text(
                        text = "取消",
                        style = MiuixTheme.textStyles.button,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }

    @Composable
    internal fun BatchPreviewRefreshConfirmDialog(
        preset: TuningPreset,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        MiuixBottomDialog(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MiuixTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "重新生成批量预览",
                    style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                    color = MiuixTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "是否重新随机抽取 ${batchPreviewCount} 个应用，重新生成预设「${preset.name}」的四风格预览？\n\n此操作将重新渲染并覆盖现有的快照数据。",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(),
                    ) {
                        Text(
                            text = "取消",
                            style = MiuixTheme.textStyles.button,
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary(),
                    ) {
                        Text(
                            text = "确认重新生成",
                            style = MiuixTheme.textStyles.button,
                            color = Color.White,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }



    /**
     * JSON 参数编辑器：全部调参参数以类型化 JSON 呈现。
     * 左侧滑块修改后此处自动刷新；也可以直接编辑文本，点「保存并应用」生效。
     */
    @Composable
    internal fun JsonSettingsEditor() {
        val currentParams = currentTuningParams()
        LaunchedEffect(currentParams) {
            draftJsonParamsText = currentParams.toJson().toString(4)
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "全部调参参数（含本地工作流开关、分离/清理/RMBG/液态玻璃/自适应/各模式选型）。可视化改动会同步到这里，也可直接编辑 JSON 后保存。",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            BasicTextField(
                value = draftJsonParamsText,
                onValueChange = { draftJsonParamsText = it },
                singleLine = false,
                textStyle = MiuixTheme.textStyles.body1.copy(
                    color = MiuixTheme.colorScheme.onSurface,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                ),
                cursorBrush = SolidColor(MiuixTheme.colorScheme.primaryVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                    .padding(12.dp),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        innerTextField()
                    }
                },
            )
            Text(
                text = "缺失的键保持当前值；非法 JSON 会提示错误且不生效。",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    text = "恢复当前",
                    onClick = {
                        draftJsonParamsText = currentTuningParams().toJson().toString(4)
                        statusText = "已恢复为当前参数 JSON"
                    },
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = "保存并应用",
                    onClick = { saveJsonParamsFromText(draftJsonParamsText) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }


    @Composable
    internal fun RootWriteConfirmDialog() {
        val request = pendingRootWriteConfirm ?: return
        MiuixBottomDialog(onDismissRequest = { pendingRootWriteConfirm = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MiuixTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "确认写入",
                    style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                    color = MiuixTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "将直接把当前生成的内容写入到指定路径：",
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = request.targetPath,
                        style = MiuixTheme.textStyles.footnote1.copy(fontWeight = FontWeight.SemiBold),
                        color = MiuixTheme.colorScheme.primaryVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "是否确认写入${request.rootWriteMode.label}？",
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { rootWriteConfirmRememberSkip = !rootWriteConfirmRememberSkip }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Checkbox(
                        state = ToggleableState(rootWriteConfirmRememberSkip),
                        onClick = { rootWriteConfirmRememberSkip = !rootWriteConfirmRememberSkip },
                        colors = CheckboxDefaults.checkboxColors(
                            checkedBackgroundColor = MiuixTheme.colorScheme.primaryVariant,
                            checkedForegroundColor = MiuixTheme.colorScheme.onPrimaryVariant,
                            uncheckedBackgroundColor = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.35f),
                            uncheckedForegroundColor = Color.Transparent,
                        ),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "以后都自动确认",
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { pendingRootWriteConfirm = null },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(),
                    ) {
                        Text(
                            text = "取消",
                            style = MiuixTheme.textStyles.button,
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                    }
                    Button(
                        onClick = {
                            val onConfirm = request.onConfirm
                            val shouldSkip = rootWriteConfirmRememberSkip
                            pendingRootWriteConfirm = null
                            if (shouldSkip) {
                                autoConfirmRootWrite = true
                                saveUiState()
                            }
                            onConfirm()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary(),
                    ) {
                        Text(
                            text = "确认写入",
                            style = MiuixTheme.textStyles.button,
                            color = Color.White,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }






    @Composable
    internal fun GenerationActionCard(selectedApp: AppEntry?) {
        val canRun = selectedApp != null && !isBusy
        SectionCard {
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
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Button(
                    onClick = { exportSelectedToExternal() },
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(
                        text = "本地导出",
                        style = MiuixTheme.textStyles.button,
                        color = Color.White,
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
                    text = "写入标准",
                    onClick = {
                        writeSelectedWithRoot(rootWriteMode = RootWriteMode.StandardOnly)
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
        }
    }








    @Composable
    internal fun GptSettingsCard() {
        val tuningState = mainViewModel.params.collectAsState().value
        SectionCard(rowsFullBleed = true) {
            LibraryChoiceRow(
                title = "调用方式",
                summary = "选择 AI 生图的调用方式",
                value = GptImageMode.fromValue(tuningState.gptImageMode).label,
                icon = SettingsIconKind.Spark,
                enabled = !isBusy,
                entry = remember(GptImageMode.fromValue(tuningState.gptImageMode)) {
                    DropdownEntry(
                        items = GptImageMode.entries.map { mode ->
                            DropdownItem(
                                text = mode.label,
                                selected = mode == GptImageMode.fromValue(tuningState.gptImageMode),
                                onClick = {
                                    mainViewModel.updateLive { p -> p.copy(gptImageMode = (mode).value) }
                                    gptSettingsSaveStatus = ""
                                },
                            )
                        },
                    )
                },
            )
            Spacer(modifier = Modifier.height(4.dp))
            LibraryChoiceRow(
                title = "AI 提示词",
                summary = GptPromptPreset.fromValue(tuningState.gptPromptPreset).summary,
                value = GptPromptPreset.fromValue(tuningState.gptPromptPreset).label,
                icon = SettingsIconKind.Prompt,
                enabled = !isBusy,
                entry = remember(GptPromptPreset.fromValue(tuningState.gptPromptPreset)) {
                    DropdownEntry(
                        items = GptPromptPreset.entries.map { preset ->
                            DropdownItem(
                                text = preset.label,
                                summary = preset.summary,
                                selected = preset == GptPromptPreset.fromValue(tuningState.gptPromptPreset),
                                onClick = {
                                    mainViewModel.updateLive { p -> p.copy(gptPromptPreset = (preset).value) }
                                    gptSettingsSaveStatus = ""
                                },
                            )
                        },
                    )
                },
            )
            AnimatedVisibility(
                visible = GptPromptPreset.fromValue(tuningState.gptPromptPreset) == GptPromptPreset.Custom,
                enter = fadeIn(animationSpec = tween(durationMillis = 150)) +
                    expandVertically(animationSpec = tween(durationMillis = 180)),
                exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                    shrinkVertically(animationSpec = tween(durationMillis = 160)),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    SettingsTextInputRow(
                        title = "自定义前景提示词",
                        value = tuningState.gptCustomPrompt,
                        label = "自定义前景提示词",
                        inputHint = "请填写自定义前景提示词",
                        icon = SettingsIconKind.Prompt,
                        enabled = !isBusy,
                        onValueChange = {
                            mainViewModel.updateLive { p -> p.copy(gptCustomPrompt = it) }
                            gptSettingsSaveStatus = ""
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            SettingsTextInputRow(
                title = "模型 ID",
                value = gptModelId,
                label = "模型 ID",
                inputHint = "请填写模型 ID",
                icon = SettingsIconKind.Layers,
                enabled = !isBusy,
                onValueChange = {
                    gptModelId = it
                    gptSettingsSaveStatus = ""
                },
            )
            Spacer(modifier = Modifier.height(6.dp))
            SettingsTextInputRow(
                title = "Base URL",
                value = gptBaseUrl,
                label = "Base URL",
                inputHint = "请填写 Base URL",
                icon = SettingsIconKind.Link,
                enabled = !isBusy,
                onValueChange = {
                    gptBaseUrl = it
                    gptSettingsSaveStatus = ""
                },
            )
            Spacer(modifier = Modifier.height(6.dp))
            SettingsTextInputRow(
                title = "API key",
                value = gptApiKey,
                label = "API key",
                inputHint = "请填写 API key",
                icon = SettingsIconKind.Key,
                obscure = true,
                enabled = !isBusy,
                onValueChange = {
                    gptApiKey = it
                    gptSettingsSaveStatus = ""
                },
            )
            Spacer(modifier = Modifier.height(6.dp))
            SettingsInfoRow(
                title = "累计调用",
                summary = "已累计调用 AI 云端接口的次数",
                value = "$gptRunCount 次",
                icon = settingsIconForTitle("累计调用"),
            )
        }
    }

    @Composable
    internal fun RmbgComponentCard() {
        val component = remember(rmbgComponentStatus) { findRmbgComponent() }

        SectionCard(rowsFullBleed = true) {
            SettingsInfoRow(
                title = "RMBG 状态",
                summary = component?.let { "ABI ${it.abi}" } ?: "未安装",
                value = if (component == null) "未安装" else "已安装",
                icon = settingsIconForTitle("RMBG 状态"),
            )
            Spacer(modifier = Modifier.height(6.dp))
            SettingsInfoRow(
                title = "累计调用",
                summary = "已累计运行 RMBG 模型抠图的次数",
                value = "$rmbgRunCount 次",
                icon = settingsIconForTitle("累计调用"),
            )
            Spacer(modifier = Modifier.height(4.dp))
            LibraryChoiceRow(
                title = "模型版本",
                summary = currentRmbgModelPreset().summary,
                value = currentRmbgModelPreset().label,
                icon = SettingsIconKind.Layers,
                enabled = !isBusy && !isGeneratingRmbgCandidate && !isInstallingRmbgComponent,
                entry = remember(currentRmbgModelPreset(), RMBG_MODEL_PRESETS) {
                    val preset = currentRmbgModelPreset()
                    DropdownEntry(
                        items = RMBG_MODEL_PRESETS.map { candidate ->
                            DropdownItem(
                                text = candidate.label,
                                summary = candidate.summary,
                                selected = candidate == preset,
                                onClick = { updateRmbgModelPreset(candidate) },
                            )
                        },
                    )
                },
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
            Spacer(modifier = Modifier.height(6.dp))
            LibrarySettingRow(
                title = "模型或组件 ZIP 地址",
                summary = if (rmbgComponentUrl.isBlank()) "粘贴 ZIP 地址或从本地选择 · 未设置" else rmbgComponentUrl,
                icon = SettingsIconKind.Link,
                showValue = false,
                showArrowRight = true,
                enabled = !isBusy && !isGeneratingRmbgCandidate && !isInstallingRmbgComponent,
                onClick = { rmbgDialogVisible = true },
            )
            if (isInstallingRmbgComponent || rmbgInstallStage.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                RmbgInstallProgressBar(
                    text = rmbgInstallStage.ifBlank { if (isInstallingRmbgComponent) "安装中" else "" },
                    progress = rmbgInstallProgress,
                    active = isInstallingRmbgComponent,
                )
            }
        }
        if (rmbgDialogVisible) {
            MiuixBottomDialog(onDismissRequest = { rmbgDialogVisible = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(MiuixTheme.colorScheme.background)
                        .padding(horizontal = 24.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "模型或组件 ZIP 地址",
                        style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                        color = MiuixTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "粘贴 ZIP 地址，或从本地选择模型文件",
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TextField(
                        value = rmbgComponentUrl,
                        onValueChange = {
                            rmbgComponentUrl = it
                            rmbgComponentSaveStatus = ""
                        },
                        label = "请填写 ZIP 地址",
                        singleLine = true,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(
                            onClick = {
                                rmbgDialogVisible = false
                                chooseRmbgComponentLauncher.launch(
                                    arrayOf("application/zip", "application/octet-stream", "*/*"),
                                )
                            },
                            enabled = !isBusy && !isGeneratingRmbgCandidate && !isInstallingRmbgComponent,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(),
                        ) {
                            Text(
                                text = "选择 ZIP",
                                style = MiuixTheme.textStyles.button,
                                color = MiuixTheme.colorScheme.onSurface,
                                maxLines = 1,
                            )
                        }
                        Button(
                            onClick = {
                                rmbgDialogVisible = false
                                installRmbgComponentFromUrl()
                            },
                            enabled = !isBusy && !isGeneratingRmbgCandidate && !isInstallingRmbgComponent,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColorsPrimary(),
                        ) {
                            Text(
                                text = if (isInstallingRmbgComponent) "安装中" else "一键安装",
                                style = MiuixTheme.textStyles.button,
                                color = Color.White,
                                maxLines = 1,
                            )
                        }
                    }
                    if (component != null) {
                        Button(
                            onClick = {
                                rmbgDialogVisible = false
                                clearInstalledRmbgComponent()
                            },
                            enabled = !isBusy && !isGeneratingRmbgCandidate && !isInstallingRmbgComponent,
                            colors = ButtonDefaults.buttonColors(
                                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MiuixTheme.colorScheme.onSurfaceVariantActions,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "清除已安装 RMBG",
                                style = MiuixTheme.textStyles.button,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }


    internal fun formatTreeUriDisplay(uri: Uri?): String? {
        if (uri == null) return null
        // 优先用 treeDocumentId（如 primary:Download/ArtPlusOutput），比 raw Uri 更可读
        val fromId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
        if (!fromId.isNullOrBlank()) {
            // 形如 primary:Download/xxx -> 取冒号后路径，未含冒号则直接解码
            val raw = if (":" in fromId) fromId.substringAfter(":") else fromId
            val decoded = runCatching { URLDecoder.decode(raw, "UTF-8") }.getOrNull() ?: raw
            if (decoded.isNotBlank()) return decoded
        }
        return uri.lastPathSegment?.let { runCatching { URLDecoder.decode(it, "UTF-8") }.getOrNull() ?: it }
    }

    @Composable
    internal fun OutputCard() {
        SectionCard(rowsFullBleed = true) {
            SettingsInfoRow(
                title = "Root 目标",
                summary = "/data/oplus/uxicons/{package}",
                value = "data",
                icon = settingsIconForTitle("Root 目标"),
            )
            Spacer(modifier = Modifier.height(4.dp))
            LibrarySettingRow(
                title = "自动确认写入",
                summary = if (autoConfirmRootWrite) "点击写入时直接写入 Root 目标" else "点击写入时会弹出二次确认提示",
                icon = SettingsIconKind.Shield,
                showSwitch = true,
                checked = autoConfirmRootWrite,
                enabled = !isBusy,
                onCheckedChange = {
                    autoConfirmRootWrite = it
                    saveUiState()
                    statusText = if (autoConfirmRootWrite) "已开启自动确认写入" else "已关闭自动确认写入"
                },
            )
            Spacer(modifier = Modifier.height(4.dp))
            LibrarySettingRow(
                title = "自动确认刷新",
                summary = if (autoConfirmRefresh) "点击刷新按钮时直接执行刷新" else "点击刷新按钮时会弹出二次确认提示",
                icon = SettingsIconKind.Refresh,
                showSwitch = true,
                checked = autoConfirmRefresh,
                enabled = !isBusy,
                onCheckedChange = {
                    autoConfirmRefresh = it
                    saveUiState()
                    statusText = if (autoConfirmRefresh) "已开启自动确认刷新" else "已关闭自动确认刷新"
                },
            )
            Spacer(modifier = Modifier.height(4.dp))
            val outputTreeDisplay = remember(outputTreeUri) { formatTreeUriDisplay(outputTreeUri) }
            val isBackupActive = backupJob?.isActive == true && backupProgress != null
            val isBackupInBg = backupInBackground && isBackupActive
            // 后台时“备份中”省略号动效：. -> .. -> ...
            val backupDots = remember(backupBackgroundDots) { ".".repeat(backupBackgroundDots.coerceIn(1, 3)) }
            LaunchedEffect(isBackupInBg) {
                if (isBackupInBg) startBackupDotAnimation() else stopBackupDotAnimation()
            }
            LibrarySettingRow(
                title = "备份到外部目录",
                summary = when {
                    isBackupInBg -> "备份中$backupDots"
                    isBackupActive -> "正在备份..."
                    outputTreeUri == null -> "未选择 · 备份已写入系统的全部图标"
                    outputTreeDisplay != null -> "已选择：$outputTreeDisplay"
                    else -> "已选择：${outputTreeUri.toString().take(40)}"
                },
                icon = settingsIconForTitle("备份到外部目录"),
                showValue = false,
                showArrowRight = true,
                enabled = !isBusy || isBackupInBg,
                onClick = {
                    if (isBackupInBg || (isBackupActive && backupSheetVisible.not())) {
                        backupInBackground = false
                        backupSheetVisible = true
                        stopBackupDotAnimation()
                    } else if (isBackupActive) {
                        backupSheetVisible = true
                    } else {
                        exportDialogVisible = true
                    }
                },
            )
        }
        if (exportDialogVisible) {
            val dialogTreeDisplay = remember(outputTreeUri) { formatTreeUriDisplay(outputTreeUri) }
            var draftExportPath by remember(outputTreeUri) {
                mutableStateOf(dialogTreeDisplay ?: outputTreeUri?.toString() ?: "")
            }
            // 保持与 treeUri 同步：当外部选择目录后，刷新输入框
            LaunchedEffect(dialogTreeDisplay, outputTreeUri) {
                val current = dialogTreeDisplay ?: outputTreeUri?.toString() ?: ""
                if (current != draftExportPath && (draftExportPath.isBlank() || outputTreeUri != null)) {
                    // 仅在空输入或已选状态下自动同步，避免覆盖用户正在输入的内容
                    if (draftExportPath.isBlank() || dialogTreeDisplay != null) {
                        draftExportPath = current
                    }
                }
            }
            MiuixBottomDialog(onDismissRequest = { exportDialogVisible = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(MiuixTheme.colorScheme.background)
                        .padding(horizontal = 24.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "备份到外部目录",
                        style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                        color = MiuixTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (outputTreeUri == null) "将把 /data/oplus/uxicons 内的全部图标（含官方与已写入）备份到你选择的目录" else "将把已写入系统的全部图标备份到你选择的目录",
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TextField(
                        value = draftExportPath,
                        onValueChange = { draftExportPath = it },
                        label = "备份路径",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(
                            onClick = {
                                exportDialogVisible = false
                                chooseTreeLauncher.launch(null)
                            },
                            enabled = !isBusy,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(),
                        ) {
                            Text(
                                text = "选择目录",
                                style = MiuixTheme.textStyles.button,
                                color = MiuixTheme.colorScheme.onSurface,
                                maxLines = 1,
                            )
                        }
                        Button(
                            onClick = {
                                exportDialogVisible = false
                                backupAllToExternal()
                            },
                            enabled = !isBusy,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColorsPrimary(),
                        ) {
                            Text(
                                text = "备份当前所有图标",
                                style = MiuixTheme.textStyles.button,
                                color = Color.White,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }

    internal fun exportCurrentToExternal() {
        // 保留兼容：设置页旧入口，委托到全量备份
        backupAllToExternal()
    }

    @Composable
    internal fun PreviewStripSettingsCard() {
        SectionCard(rowsFullBleed = true) {
            LibrarySettingRow(
                title = "顶部 1×4 预览条",
                summary = "在主页、生成参数与预设页置顶显示，参数或 JSON 保存后自动更新",
                icon = SettingsIconKind.Palette,
                showSwitch = true,
                checked = previewStripEnabled,
                enabled = !isBusy,
                onCheckedChange = { updatePreviewStripEnabled(it) },
            )
            NumberParameterControl(
                busy = isBusy,
                title = "批量预览数量",
                summary = "预设四风格宫格预览时随机抓取的应用数量（默认 20，优先未生成图标应用）",
                value = batchPreviewCount,
                draftText = draftBatchPreviewCountText,
                min = MIN_BATCH_PREVIEW_COUNT,
                max = MAX_BATCH_PREVIEW_COUNT,
                step = 1,
                onDraftChange = { draftBatchPreviewCountText = it },
                onSave = { updateBatchPreviewCount(it) },
                icon = SettingsIconKind.Grid,
            )
        }
    }

    @Composable
    internal fun LiquidGlassToggleRow() {
        val tuningState = mainViewModel.params.collectAsState().value
        val interactionSource = remember { MutableInteractionSource() }
        val pressed by interactionSource.collectIsPressedAsState()
        val bleedPx = with(LocalDensity.current) { CHOICE_ROW_HORIZONTAL_BLEED_DP.dp.roundToPx() }
        val bridge = LocalSectionCardPressBridge.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .trackSectionPress(bridge, pressed)
                .cardRowBleed(bleedPx)
                .background(cardRowPressedColor(pressed))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = !isBusy,
                    onClick = { updateLiquidGlassEnabled(!mainViewModel.params.value.liquidGlassEnabled) },
                )
                .padding(horizontal = CHOICE_ROW_HORIZONTAL_BLEED_DP.dp)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsLineIcon(kind = SettingsIconKind.Glass)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
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
                    modifier = Modifier.basicMarquee(),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    softWrap = false,
                )
            }
            LiquidGlassSwitch(checked = tuningState.liquidGlassEnabled, enabled = !isBusy)
        }
    }



    @Composable
    internal fun InputSettingsCard(launcherCount: Int, totalCount: Int, generatedCount: Int) {
        SectionCard(rowsFullBleed = true) {
            SettingsInfoRow(
                title = "应用范围",
                summary = "启动器 $launcherCount 个 / 全部 $totalCount 个",
                value = "启动器",
                icon = settingsIconForTitle("应用范围"),
            )
            Spacer(modifier = Modifier.height(4.dp))
            LibrarySettingRow(
                title = "已生成",
                summary = "来自本地缓存；手动刷新后才重新读取 data 路径",
                value = "$generatedCount",
                icon = settingsIconForTitle("已生成"),
                showArrowRight = true,
                enabled = !isBusy,
                onClick = { loadApps(refreshGenerated = true) },
            )
        }
    }

    @Composable
    internal fun ShowSystemAppsToggleRow() {
        val interactionSource = remember { MutableInteractionSource() }
        val pressed by interactionSource.collectIsPressedAsState()
        val bleedPx = with(LocalDensity.current) { CHOICE_ROW_HORIZONTAL_BLEED_DP.dp.roundToPx() }
        val bridge = LocalSectionCardPressBridge.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .trackSectionPress(bridge, pressed)
                .cardRowBleed(bleedPx)
                .background(cardRowPressedColor(pressed))
                .semantics {
                    contentDescription = "显示系统应用开关"
                    stateDescription = if (showSystemApps) "已开启" else "已关闭"
                    role = Role.Switch
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = !isBusy,
                    onClick = {
                        showSystemApps = !showSystemApps
                        saveUiState()
                    },
                )
                .padding(horizontal = CHOICE_ROW_HORIZONTAL_BLEED_DP.dp)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsLineIcon(kind = SettingsIconKind.Shield)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = "显示系统应用",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (showSystemApps) "已包含系统应用，可搜索和批量选择" else "仅显示用户应用；系统应用已隐藏",
                    modifier = Modifier.basicMarquee(),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    softWrap = false,
                )
            }
            Box(
                modifier = Modifier.semantics {
                    contentDescription = "显示系统应用"
                }
            ) {
                LiquidGlassSwitch(checked = showSystemApps, enabled = !isBusy)
            }
        }
    }

    @Composable
    internal fun AppPickerStatusCard(
        filteredCount: Int,
        totalCount: Int,
        generatedCount: Int,
        ungeneratedCount: Int,
    ) {
        val multiCount = multiSelectedPackageNames.size
        val statusText = buildString {
            append("$filteredCount/$totalCount")
            append(" · 已生成 $generatedCount")
            append(" · 未生成 $ungeneratedCount")
            if (isScanningGeneratedPackages) {
                append(" · 扫描中")
            } else if (generatedScanFailed) {
                append(" · 无法读取 data 路径")
            }
            if (multiCount > 0) {
                append(" · 多选 $multiCount")
            }
        }
        SectionCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (multiCount > 0) MiuixTheme.colorScheme.primaryVariant
                                else MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.4f),
                            ),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "应用列表 $filteredCount/$totalCount",
                                style = MiuixTheme.textStyles.title4.copy(fontWeight = FontWeight.Bold),
                                color = MiuixTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (multiCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = "多选 $multiCount",
                                        style = MiuixTheme.textStyles.footnote2.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                        ),
                                        color = MiuixTheme.colorScheme.primaryVariant,
                                    )
                                }
                            }
                        }
                        Text(
                            text = statusText,
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        text = "刷新生成",
                        onClick = { refreshGeneratedPackages() },
                        enabled = !isBusy && apps.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        text = "更新列表",
                        onClick = { loadApps() },
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    @Composable
    internal fun AppPickerFilterCard() {
        SectionCard(rowsFullBleed = true) {
            LibrarySettingRow(
                title = "显示系统应用",
                summary = if (showSystemApps) "已包含系统应用，可搜索和批量选择" else "仅显示用户应用；系统应用已隐藏",
                icon = SettingsIconKind.Shield,
                showSwitch = true,
                checked = showSystemApps,
                enabled = !isBusy,
                onCheckedChange = {
                    showSystemApps = !showSystemApps
                    saveUiState()
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp),
            ) {
                val filters = GeneratedFilter.entries
                SegmentedControl(
                    enabled = !isBusy,
                    labels = filters.map { it.label },
                    selectedIndex = filters.indexOf(generatedFilter),
                    onSelected = { index ->
                        generatedFilter = filters[index]
                        queryText = ""
                        saveUiState()
                    },
                )
            }
        }
    }

    @Composable
    internal fun AppPickerSearchCard(filteredApps: List<AppEntry>) {
        SectionCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // 标准搜索条：复刻 PresetLibraryCard 的搜索实现，高度与按钮对齐 48dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Image(
                        imageVector = Lucide.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantSummary),
                    )
                    BasicTextField(
                        value = queryText,
                        onValueChange = {
                            queryText = it
                            saveUiState()
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MiuixTheme.textStyles.body2.copy(
                            color = MiuixTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MiuixTheme.colorScheme.primaryVariant),
                        decorationBox = { innerTextField ->
                            if (queryText.isEmpty()) {
                                Text(
                                    text = "搜索应用或包名...",
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.6f),
                                )
                            }
                            innerTextField()
                        },
                    )
                    if (queryText.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .clickable {
                                    queryText = ""
                                    saveUiState()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                imageVector = Lucide.X,
                                contentDescription = "清除",
                                modifier = Modifier.size(14.dp),
                                colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantSummary),
                            )
                        }
                    }
                }
                AppMultiSelectActions(filteredApps)
            }
        }
    }

    @Composable
    internal fun AppPickerControlsCard(
        filteredCount: Int,
        totalCount: Int,
        generatedCount: Int,
        ungeneratedCount: Int,
        filteredApps: List<AppEntry>,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppPickerStatusCard(
                filteredCount = filteredCount,
                totalCount = totalCount,
                generatedCount = generatedCount,
                ungeneratedCount = ungeneratedCount,
            )
            AppPickerFilterCard()
            AppPickerSearchCard(filteredApps = filteredApps)
        }
    }

    @Composable
    internal fun AppMultiSelectActions(filteredApps: List<AppEntry>) {
        val filteredPackageNames = remember(filteredApps) { filteredApps.map { it.packageName }.toSet() }
        val selectedCount = multiSelectedPackageNames.size
        val hasFiltered = filteredPackageNames.isNotEmpty()
        val allFilteredSelected = hasFiltered && filteredPackageNames.all { it in multiSelectedPackageNames }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CompactActionButton(
                text = "添加光影 $selectedCount",
                onClick = { addLiquidGlassToMultiSelectedGenerated() },
                enabled = !isBusy && selectedCount > 0,
                modifier = Modifier.weight(1f),
                height = 48.dp,
            )
            CompactActionButton(
                text = "套用当前预设",
                onClick = { applyCurrentPresetBatch() },
                enabled = !isBusy && selectedCount > 0,
                modifier = Modifier.weight(1f),
                height = 48.dp,
            )
        }
    }

    @Composable
    internal fun AppRow(
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

        val containerBg = when {
            selected -> MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            multiSelected -> MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            else -> MiuixTheme.colorScheme.surfaceContainerHigh
        }
        // Card 本身无 colors 参数（miuix 0.9.1），用外层背景 + clip 模拟 CompactPresetRow 的选中态
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(containerBg)
                .clickable(onClick = onClick),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
                showIndication = false,
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // 复选框：多选清单语义，去掉旧的"选择/已选"文字按钮与箭头
                Checkbox(
                    state = ToggleableState(multiSelected),
                    onClick = onToggleMultiSelect,
                    enabled = !isBusy,
                    colors = CheckboxDefaults.checkboxColors(
                        checkedBackgroundColor = MiuixTheme.colorScheme.primaryVariant,
                        checkedForegroundColor = MiuixTheme.colorScheme.onPrimaryVariant,
                        uncheckedBackgroundColor = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.35f),
                        uncheckedForegroundColor = Color.Transparent,
                    ),
                )
                AppIcon(
                    entry = entry,
                    size = 48.dp,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = entry.label,
                        modifier = Modifier.basicMarquee(),
                        style = MiuixTheme.textStyles.body1.copy(fontWeight = FontWeight(550)),
                        color = if (selected) MiuixTheme.colorScheme.primaryVariant else MiuixTheme.colorScheme.onSurface,
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
                        modifier = Modifier.padding(start = 8.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        tags.forEach { tag ->
                            AppStatusTag(tag = tag)
                        }
                    }
                }
            }
            }
        }
    }
















    @Composable
    internal fun AppIcon(entry: AppEntry, size: Dp) {
        var bitmap by remember(entry.iconKey) {
            mutableStateOf(getCachedAppIcon(appIconCache, entry.iconKey))
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


    internal suspend fun loadCachedAppIcon(entry: AppEntry): Bitmap? =
        withContext(Dispatchers.IO) {
            val cached = synchronized(appIconCache) {
                appIconCache.get(entry.iconKey)
            }
            if (cached != null) {
                return@withContext cached
            }

            val bitmap = runCatching { loadAppIconBitmap(entry, packageManager, ICON_CACHE_SIZE) }.getOrNull() ?: return@withContext null

            synchronized(appIconCache) {
                appIconCache.put(entry.iconKey, bitmap)
            }
            bitmap
        }


    /**
     * 读取当前设备桌面壁纸并保留原始宽高比（短边缩放到 480 左右）。
     * 静态壁纸经 ImageWallpaper 暴露为 BitmapDrawable，直接取位图，无需任何权限；
     * 失败返回 null，调用方走内置图兜底。
     */
    internal fun loadPreviewWallpaperBitmap(): Bitmap? =
        runCatching {
            val drawable = WallpaperManager.getInstance(this).drawable ?: return null
            val sampled = if (drawable is BitmapDrawable) {
                drawable.bitmap?.let { sampleBitmapShortEdge(it, PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE) }
            } else {
                val intrinsicW = drawable.intrinsicWidth.takeIf { it > 0 }
                val intrinsicH = drawable.intrinsicHeight.takeIf { it > 0 }
                if (intrinsicW != null && intrinsicH != null) {
                    val scale = PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE.toFloat() /
                        minOf(intrinsicW, intrinsicH).toFloat()
                    drawDrawableCover(
                        drawable = drawable,
                        width = (intrinsicW * scale).roundToInt().coerceAtLeast(1),
                        height = (intrinsicH * scale).roundToInt().coerceAtLeast(1),
                    )
                } else {
                    // 无内在尺寸（如纯色壁纸）：按常见竖屏比例渲染
                    drawDrawableCover(drawable, PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE, 854)
                }
            }
            sampled?.also { it.prepareToDraw() }
        }.getOrNull()

    internal fun loadBundledPreviewWallpaperBitmap(): Bitmap? =
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(resources, R.drawable.preview_wallpaper, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null
            }
            val shortEdge = minOf(bounds.outWidth, bounds.outHeight)
            var sampleSize = 1
            while (shortEdge / (sampleSize * 2) >= PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE) {
                sampleSize *= 2
            }
            val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            BitmapFactory.decodeResource(resources, R.drawable.preview_wallpaper, opts)
                ?.also { it.prepareToDraw() }
        }.getOrNull()

    /**
     * 导入用户上传的壁纸：只居中裁剪为 16:9，不做任何缩放压缩，避免变形；
     * 以 PNG 无损存档到私有目录，「桌面」背景优先使用。
     */
    internal fun importCustomWallpaper(uri: Uri) {
        if (isBusy) {
            return
        }
        statusText = "正在导入壁纸…"
        startUiFriendlyThread("ArtPlusWallpaperImport") {
            try {
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("无法打开图片")
                val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ?: error("图片无法解码；请选择 JPG/PNG/WEBP")
                val cropped = centerCropToSixteenNine(decoded)
                if (cropped !== decoded && !decoded.isRecycled) {
                    decoded.recycle()
                }
                val outFile = File(filesDir, CUSTOM_WALLPAPER_FILE)
                FileOutputStream(outFile).use { fos ->
                    if (!cropped.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
                        error("壁纸保存失败")
                    }
                }
                val info = "${cropped.width} × ${cropped.height}"
                runOnUiThread {
                    customWallpaperPath = outFile.absolutePath
                    customWallpaperInfo = info
                    statusText = "已导入自定义壁纸（$info），「桌面」背景优先使用此图"
                    saveUiState()
                }
            } catch (error: Exception) {
                status("壁纸导入失败: ${error.message ?: error.javaClass.simpleName}")
            }
        }
    }

    internal fun clearCustomWallpaper() {
        runCatching {
            customWallpaperPath?.let { File(it).delete() }
            File(filesDir, CUSTOM_WALLPAPER_FILE).delete()
        }
        customWallpaperPath = null
        customWallpaperInfo = ""
        statusText = "已清除自定义壁纸，「桌面」背景恢复系统壁纸/内置壁纸"
        saveUiState()
    }

    /** 居中裁剪为 16:9（竖屏），只裁剪不缩放；已符合比例则原图返回。 */
    internal fun centerCropToSixteenNine(source: Bitmap): Bitmap {
        val w = source.width
        val h = source.height
        if (w <= 0 || h <= 0) {
            return source
        }
        val targetRatio = 9f / 16f
        val currentRatio = w.toFloat() / h.toFloat()
        if (abs(currentRatio - targetRatio) / targetRatio < 0.005f) {
            return source
        }
        return if (currentRatio > targetRatio) {
            val cropW = (h * targetRatio).roundToInt().coerceIn(1, w)
            Bitmap.createBitmap(source, (w - cropW) / 2, 0, cropW, h)
        } else {
            val cropH = (w / targetRatio).roundToInt().coerceIn(1, h)
            Bitmap.createBitmap(source, 0, (h - cropH) / 2, w, cropH)
        }
    }

    internal fun loadCustomWallpaperBitmap(): Bitmap? =
        runCatching {
            val path = customWallpaperPath ?: return null
            val file = File(path)
            if (!file.isFile) {
                return null
            }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null
            sampleBitmapShortEdge(bitmap, PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE)
                .also { it.prepareToDraw() }
        }.getOrNull()




    internal fun loadApps(refreshGenerated: Boolean = false) {
        // P3 交界：数据核收敛进 data/AppRepository.loadApps(pm)，线程/UI/状态写入留本编排。
        didRequestAppLoad = true
        Thread {
            val result = loadApps(packageManager)
            val entries = result.entries
            val launchablePackages = result.launchablePackages
            // P3 交界：图标预热收敛进 data/IconCache（显式传 cache + pm + 尺寸 + 条数）。
            preloadAppIcons(appIconCache, packageManager, entries, ICON_CACHE_SIZE, PRELOAD_ICON_COUNT)
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

    internal fun refreshGeneratedPackages(entries: List<AppEntry> = apps.toList()) {
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
                        generatedPackageNames = updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), generated)
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




    internal fun loadUiState() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        selectedPackageName = prefs.getString(PREF_SELECTED_PACKAGE_NAME, null)
            ?.takeIf { it.isNotBlank() }
        generatedFilter = GeneratedFilter.fromName(prefs.getString(PREF_GENERATED_FILTER, null))
        showSystemApps = prefs.getBoolean(PREF_SHOW_SYSTEM_APPS, false)
        queryText = prefs.getString(PREF_QUERY_TEXT, "") ?: ""
        advancedSettingsCategory = AdvancedSettingsCategory.fromName(
            prefs.getString(PREF_ADVANCED_SETTINGS_CATEGORY, null),
        )
        advancedSettingsTab = runCatching {
            AdvancedSettingsTab.valueOf(
                prefs.getString(PREF_ADVANCED_SETTINGS_TAB, AdvancedSettingsTab.Sliders.name)
                    ?: AdvancedSettingsTab.Sliders.name,
            )
        }.getOrDefault(AdvancedSettingsTab.Sliders)
        previewPackageName = prefs.getString(PREF_PREVIEW_PACKAGE_NAME, null)
            ?.takeIf { it.isNotBlank() }
        previewDirPath = prefs.getString(PREF_PREVIEW_DIR_PATH, null)
            ?.takeIf { it.isNotBlank() }
        previewStripEnabled = prefs.getBoolean(PREF_PREVIEW_STRIP_ENABLED, false)
        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (PreviewSelections.fromPrefs(prefs)).normalLight.name, previewNormalDark = (PreviewSelections.fromPrefs(prefs)).normalDark.name, previewMonochromeLight = (PreviewSelections.fromPrefs(prefs)).monochromeLight.name, previewMonochromeDark = (PreviewSelections.fromPrefs(prefs)).monochromeDark.name) }
        previewDesktopBackground = PreviewDesktopBackground.fromName(
            prefs.getString(PREF_PREVIEW_DESKTOP_BACKGROUND, null),
        )
        previewIconSizeDp = prefs.getInt(PREF_PREVIEW_ICON_SIZE_DP, DEFAULT_PREVIEW_ICON_SIZE_DP)
            .coerceIn(MIN_PREVIEW_ICON_SIZE_DP, MAX_PREVIEW_ICON_SIZE_DP)
        draftPreviewIconSizeDpText = previewIconSizeDp.toString()
        previewCornerRadiusDp = prefs.getInt(PREF_PREVIEW_CORNER_RADIUS_DP, DEFAULT_PREVIEW_CORNER_RADIUS_DP)
            .coerceIn(MIN_PREVIEW_CORNER_RADIUS_DP, MAX_PREVIEW_CORNER_RADIUS_DP)
        draftPreviewCornerRadiusDpText = previewCornerRadiusDp.toString()
        batchPreviewCount = prefs.getInt(PREF_BATCH_PREVIEW_COUNT, DEFAULT_BATCH_PREVIEW_COUNT)
            .coerceIn(MIN_BATCH_PREVIEW_COUNT, MAX_BATCH_PREVIEW_COUNT)
        draftBatchPreviewCountText = batchPreviewCount.toString()
        batchPreviewColumns = prefs.getInt(PREF_BATCH_PREVIEW_COLUMNS, 4).coerceIn(2, 5)
        draftBatchPreviewColumnsText = batchPreviewColumns.toString()
        batchPreviewIconSizeDp = prefs.getInt(PREF_BATCH_PREVIEW_ICON_SIZE_DP, 54).coerceIn(40, 84)
        draftBatchPreviewIconSizeDpText = batchPreviewIconSizeDp.toString()
        batchPreviewCornerRadiusDp = prefs.getInt(PREF_BATCH_PREVIEW_CORNER_RADIUS_DP, previewCornerRadiusDp).coerceIn(0, 36)
        draftBatchPreviewCornerRadiusDpText = batchPreviewCornerRadiusDp.toString()
        batchPreviewDesktopBackground = PreviewDesktopBackground.fromName(
            prefs.getString(PREF_BATCH_PREVIEW_DESKTOP_BG, PreviewDesktopBackground.DarkGray.name),
        )
        customWallpaperPath = prefs.getString(PREF_CUSTOM_WALLPAPER_PATH, null)
            ?.takeIf { File(it).isFile }
        customWallpaperInfo = customWallpaperPath?.let { path ->
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(path, bounds)
                if (bounds.outWidth > 0 && bounds.outHeight > 0) {
                    "${bounds.outWidth} × ${bounds.outHeight}"
                } else {
                    ""
                }
            }.getOrNull().orEmpty()
        }.orEmpty()
        autoConfirmRootWrite = prefs.getBoolean(PREF_AUTO_CONFIRM_ROOT_WRITE, prefs.getBoolean(PREF_SKIP_ROOT_WRITE_CONFIRM, false))
        autoConfirmRefresh = prefs.getBoolean(PREF_AUTO_CONFIRM_REFRESH, false)
        outputTreeUri = prefs.getString(PREF_OUTPUT_TREE_URI, null)?.takeIf { it.isNotBlank() }?.let { runCatching { Uri.parse(it) }.getOrNull() }
            ?: contentResolver.persistedUriPermissions.firstOrNull { it.isReadPermission && it.isWritePermission }?.uri
        // onboarding: if not completed and no dir, show guide
        val hasCompleted = prefs.getBoolean(PREF_HAS_COMPLETED_ONBOARDING, false)
        if (!hasCompleted && outputTreeUri == null) {
            onboardingVisible = true
        }
    }

    internal fun saveUiState() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(PREF_SELECTED_PACKAGE_NAME, selectedPackageName)
            .putString(PREF_GENERATED_FILTER, generatedFilter.name)
            .putBoolean(PREF_SHOW_SYSTEM_APPS, showSystemApps)
            .putString(PREF_QUERY_TEXT, queryText)
            .putString(PREF_ADVANCED_SETTINGS_CATEGORY, advancedSettingsCategory.name)
            .putString(PREF_ADVANCED_SETTINGS_TAB, advancedSettingsTab.name)
            .putString(PREF_PREVIEW_PACKAGE_NAME, previewPackageName)
            .putString(PREF_PREVIEW_DIR_PATH, previewDirPath)
            .putBoolean(PREF_PREVIEW_STRIP_ENABLED, previewStripEnabled)
            .putString(PREF_PREVIEW_SELECTION_NORMAL_LIGHT, PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark).normalLight.name)
            .putString(PREF_PREVIEW_SELECTION_NORMAL_DARK, PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark).normalDark.name)
            .putString(PREF_PREVIEW_SELECTION_MONOCHROME_LIGHT, PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark).monochromeLight.name)
            .putString(PREF_PREVIEW_SELECTION_MONOCHROME_DARK, PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark).monochromeDark.name)
            .putString(PREF_PREVIEW_DESKTOP_BACKGROUND, previewDesktopBackground.name)
            .putInt(PREF_PREVIEW_ICON_SIZE_DP, previewIconSizeDp)
            .putInt(PREF_PREVIEW_CORNER_RADIUS_DP, previewCornerRadiusDp)
            .putInt(PREF_BATCH_PREVIEW_COUNT, batchPreviewCount)
            .putInt(PREF_BATCH_PREVIEW_COLUMNS, batchPreviewColumns)
            .putInt(PREF_BATCH_PREVIEW_ICON_SIZE_DP, batchPreviewIconSizeDp)
            .putInt(PREF_BATCH_PREVIEW_CORNER_RADIUS_DP, batchPreviewCornerRadiusDp)
            .putString(PREF_BATCH_PREVIEW_DESKTOP_BG, batchPreviewDesktopBackground.name)
            .apply { customWallpaperPath?.let { putString(PREF_CUSTOM_WALLPAPER_PATH, it) } ?: remove(PREF_CUSTOM_WALLPAPER_PATH) }
            .putBoolean(PREF_AUTO_CONFIRM_ROOT_WRITE, autoConfirmRootWrite)
            .putBoolean(PREF_SKIP_ROOT_WRITE_CONFIRM, autoConfirmRootWrite)
            .putBoolean(PREF_AUTO_CONFIRM_REFRESH, autoConfirmRefresh)
            .putString(PREF_OUTPUT_TREE_URI, outputTreeUri?.toString())
            .apply()
    }

    internal fun refreshArtPlusIcons() {
        if (isBusy || isRefreshingArtPlusIcons) {
            return
        }
        isRefreshingArtPlusIcons = true
        statusText = "正在刷新 ART+ 图标..."
        Thread {
            // P3 交界：阻塞核收敛进 system/RootShell（显式传 ContentResolver + apkPath）。
            val result = runCatching { refreshArtPlusIconsBlocking(contentResolver, applicationInfo.sourceDir) }
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

    internal fun refreshPermissionState() {
        packageListPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            packageManager.checkPermission(Manifest.permission.QUERY_ALL_PACKAGES, packageName) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        usageAccessGranted = hasUsageAccess()
    }

    internal fun requestDeclaredPermissions() {
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

    internal fun requestSpecialPermissionsOnce() {
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

    internal fun openAppPermissionSettings() {
        runCatching {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", packageName, null)),
            )
        }.onFailure {
            statusText = "无法打开应用权限设置: ${it.message ?: it.javaClass.simpleName}"
        }
    }

    internal fun openUsageAccessSettings() {
        runCatching {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }.onFailure {
            statusText = "无法打开使用情况访问设置: ${it.message ?: it.javaClass.simpleName}"
        }
    }

    internal fun openExternalLink(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure {
            statusText = "无法打开链接: ${it.message ?: it.javaClass.simpleName}"
        }
    }

    internal fun currentVersionName(): String = try {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "1.4.0"
    } catch (_: Exception) {
        "1.4.0"
    }

    internal fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.trim().removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.trim().removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
        val len = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until len) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l != c) return l > c
        }
        return false
    }

    internal fun checkForUpdate() {
        if (isCheckingUpdate) return
        isCheckingUpdate = true
        statusText = "正在检查更新..."
        mainScope.launch(Dispatchers.IO) {
            try {
                val url = validatedRemoteUrl(
                    "https://api.github.com/repos/Costben/ArtPlus/releases/latest",
                    "检查更新",
                    isDebugBuild(),
                )
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 10_000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "ArtPlus-Android")
                }
                val code = connection.responseCode
                val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader()?.use { it.readText() } ?: ""
                connection.disconnect()
                if (code !in 200..299) error("HTTP $code ${body.take(200)}")
                val json = JSONObject(body)
                val tagName = json.optString("tag_name", "")
                val htmlUrl = json.optString("html_url", GITHUB_REPO_URL + "/releases")
                val latest = tagName.removePrefix("v").trim()
                val current = currentVersionName().trim()
                withContext(Dispatchers.Main) {
                    if (latest.isBlank()) {
                        statusText = "检查失败：未获取到版本信息"
                    } else if (isNewerVersion(latest, current)) {
                        updateAvailableInfo = UpdateInfo(latest, tagName.ifBlank { "v$latest" }, htmlUrl)
                        statusText = "发现新版本 $tagName"
                    } else {
                        updateUpToDateDialogVisible = true
                        statusText = "已是最新版本 $current"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusText = "检查更新失败: ${e.message ?: e.javaClass.simpleName}"
                }
            } finally {
                withContext(Dispatchers.Main) { isCheckingUpdate = false }
            }
        }
    }

    internal fun hasUsageAccess(): Boolean {
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

    internal fun getApplicationInfoCompat(packageName: String): ApplicationInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        }

    internal fun isDebugGenerateIntent(intent: Intent?): Boolean =
        intent?.getStringExtra(EXTRA_DEBUG_GENERATE_PACKAGE)?.isNotBlank() == true &&
            isDebugTokenValid(intent.getStringExtra(EXTRA_DEBUG_GENERATE_TOKEN))

    internal fun handleDebugGenerateIntent(intent: Intent?) {
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

    internal fun startDebugGeneration(
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
                        generatedPackageNames = markPackageGenerated(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), generatedPackageNames, packageName)
                        statusText = "调试生成完成并${rootWriteMode.label}写入 Root，未刷新，请手动点刷新 ART+ 图标: ${result.outDir.absolutePath}"
                    }
                } else {
                    runOnMainSync {
                        statusText = "调试生成完成: ${result.outDir.absolutePath}"
                    }
                }
                runOnMainSync {
                    activeGenerationSession = result.session
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (result.selections).normalLight.name, previewNormalDark = (result.selections).normalDark.name, previewMonochromeLight = (result.selections).monochromeLight.name, previewMonochromeDark = (result.selections).monochromeDark.name) }
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

    internal fun debugInspectPackage(packageName: String, includeRmbg: Boolean): JSONObject {
        val info = getApplicationInfoCompat(packageName)
        val label = runCatching { info.loadLabel(packageManager)?.toString() }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: packageName
        val icon = info.loadIcon(packageManager)
        val base = getExternalFilesDir("ArtPlusLab") ?: File(filesDir, "ArtPlusLab")
        val outDir = File(base, packageName)
        ensureFreshDir(outDir)
        val debugTuning = currentTuningParams()
        val localPipeline = LocalPipelineConfig.from(debugTuning)

        val metadata = JSONObject()
            .put("ok", true)
            .put("package", packageName)
            .put("label", label)
            .put("output_dir", outDir.absolutePath)
            .put("source_dir", info.sourceDir ?: "")
            .put("public_source_dir", info.publicSourceDir ?: "")
            .put("is_adaptive", icon is AdaptiveIconDrawable)
            .put("settings", debugTuning.toJson())

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
            val subtracted = if (localPipeline.backgroundSeparationEnabled) {
                subtractBackground(composed, background, pipeline = localPipeline, backgroundSeparationPercent = mainViewModel.params.value.backgroundSeparationPercent)
            } else {
                composed
            }
            val selection = chooseBetterAdaptiveForeground(subtracted, direct, background, localPipeline, AdaptiveForegroundMode.fromValue(mainViewModel.params.value.adaptiveForegroundMode), mainViewModel.params.value.adaptiveDirectMaxCoveragePercent, mainViewModel.params.value.adaptiveDirectMaxCoverageIncreasePercent, mainViewModel.params.value.adaptiveMaskEdgeCoveragePercent, mainViewModel.params.value.adaptiveMaskMinCoveragePercent, mainViewModel.params.value.adaptiveCenterEpsilonPercent)
            val chosen = selection.bitmap
            val adaptiveJson = JSONObject()
            saveLayer("adaptive_background_240", resizeBitmap(background, SIZE_1X1, SIZE_1X1), adaptiveJson)
            saveLayer("adaptive_composed_240", resizeBitmap(composed, SIZE_1X1, SIZE_1X1), adaptiveJson)
            saveLayer("adaptive_subtracted_foreground_240", resizeBitmap(subtracted, SIZE_1X1, SIZE_1X1), adaptiveJson)
            saveLayer("adaptive_direct_foreground_240", resizeBitmap(direct, SIZE_1X1, SIZE_1X1), adaptiveJson)
            saveLayer("adaptive_chosen_foreground_240", resizeBitmap(chosen, SIZE_1X1, SIZE_1X1), adaptiveJson)
            adaptiveJson
                .put("subtracted_has_mask_artifact", hasAdaptiveMaskArtifact(subtracted, mainViewModel.params.value.adaptiveMaskEdgeCoveragePercent, mainViewModel.params.value.adaptiveMaskMinCoveragePercent))
                .put("direct_usable", isUsableDirectAdaptiveForeground(direct, alphaCoverage(subtracted), mainViewModel.params.value.adaptiveDirectMaxCoveragePercent, mainViewModel.params.value.adaptiveDirectMaxCoverageIncreasePercent))
                .put("subtracted_coverage", alphaCoverage(subtracted))
                .put("direct_coverage", alphaCoverage(direct))
                .put("chosen_preserve_geometry", selection.preserveGeometry)
            metadata.put("adaptive", adaptiveJson)
        }

        val localSource = buildLocalIconLayers(icon, localPipeline, mainViewModel.params.value.backgroundSeparationPercent, AdaptiveForegroundMode.fromValue(mainViewModel.params.value.adaptiveForegroundMode), mainViewModel.params.value.adaptiveDirectMaxCoveragePercent, mainViewModel.params.value.adaptiveDirectMaxCoverageIncreasePercent, mainViewModel.params.value.adaptiveMaskEdgeCoveragePercent, mainViewModel.params.value.adaptiveMaskMinCoveragePercent, mainViewModel.params.value.adaptiveCenterEpsilonPercent)
        val localJson = JSONObject()
        saveLayer("local_base_recbg", localSource.recbg, localJson)
        saveLayer("local_base_recfg", localSource.recfg, localJson)
        localSource.monochrome?.let { saveLayer("local_base_monochrome", it, localJson) }
        val candidateSet = buildLocalCandidates(localSource, candidateSource240, localPipeline, OriginalForegroundCleanupMode.fromValue(mainViewModel.params.value.originalForegroundCleanupMode), mainViewModel.params.value.plateRemovalPercent, mainViewModel.params.value.shadowRemovalPercent, mainViewModel.params.value.backgroundSeparationPercent)
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
                            isLocal = false,
                    ),
                )
                saveLayer("candidate_rmbg_rendered", rendered, rmbgJson)
                saveLayer("candidate_rmbg_night", nightForeground(rendered, localSource.recbg), rmbgJson)
                saveLayer("candidate_rmbg_monochrome_light", monochromeForCandidate(
                    rmbgCandidate ?: IconCandidate(rmbgDebug.foreground, localSource.recbg, monochromeRaw = rmbgDebug.foreground, isLocal = false),
                    invertLuma = true,
                ), rmbgJson)
                saveLayer("candidate_rmbg_monochrome_dark", monochromeForCandidate(
                    rmbgCandidate ?: IconCandidate(rmbgDebug.foreground, localSource.recbg, monochromeRaw = rmbgDebug.foreground, isLocal = false),
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


    internal fun loadGptSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        mainViewModel.updateLive { p -> p.copy(gptImageMode = (GptImageMode.fromValue(prefs.getString(PREF_GPT_MODE, GptImageMode.Images.value))).value) }
        mainViewModel.updateLive { p -> p.copy(gptPromptPreset = (GptPromptPreset.fromValue(
            prefs.getString(PREF_GPT_PROMPT_PRESET, GptPromptPreset.StableCutout.value),
        )).value) }
        mainViewModel.updateLive { p -> p.copy(gptCustomPrompt = prefs.getString(PREF_GPT_CUSTOM_PROMPT, "") ?: "") }
        gptModelId = prefs.getString(PREF_GPT_MODEL_ID, "") ?: ""
        val storedBaseUrl = prefs.getString(PREF_GPT_BASE_URL, "") ?: ""
        gptBaseUrl = if (storedBaseUrl == LEGACY_DEFAULT_GPT_BASE_URL) "" else storedBaseUrl
        gptApiKey = loadGptApiKey(prefs)
        if (storedBaseUrl == LEGACY_DEFAULT_GPT_BASE_URL) {
            prefs.edit().putString(PREF_GPT_BASE_URL, "").apply()
        }
    }

    internal fun saveGptSettings(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val encryptedKey = encryptSecret(gptApiKey.trim())
        return prefs
            .edit()
            .putString(PREF_GPT_MODE, GptImageMode.fromValue(mainViewModel.params.value.gptImageMode).value)
            .putString(PREF_GPT_PROMPT_PRESET, GptPromptPreset.fromValue(mainViewModel.params.value.gptPromptPreset).value)
            .putString(PREF_GPT_CUSTOM_PROMPT, mainViewModel.params.value.gptCustomPrompt.trim())
            .putString(PREF_GPT_MODEL_ID, gptModelId.trim())
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

    internal fun saveSettingsPage() {
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

    internal fun loadGptApiKey(prefs: android.content.SharedPreferences): String {
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

    internal fun encryptSecret(value: String): String {
        if (value.isBlank()) {
            return ""
        }
        val cipher = Cipher.getInstance(KEYSTORE_CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, gptSecretKey())
        val encrypted = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        return listOf(cipher.iv, encrypted)
            .joinToString(":") { Base64.encodeToString(it, Base64.NO_WRAP) }
    }

    internal fun decryptSecret(value: String): String {
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

    internal fun gptSecretKey(): SecretKey {
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

    internal fun loadRmbgSettings() {
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

    internal fun saveRmbgSettings(): Boolean =
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(PREF_RMBG_COMPONENT_URL, rmbgComponentUrl.trim())
            .putInt(PREF_RMBG_INPUT_SIZE, DEFAULT_RMBG_INPUT_SIZE)
            .commit()

    internal fun loadLocalSeparationSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        mainViewModel.updateLive { p -> p.copy(localSeparationMode = (LocalSeparationMode.fromValue(
            prefs.getString(PREF_LOCAL_SEPARATION_MODE, LocalSeparationMode.Auto.value),
        )).value) }
    }

    internal fun saveLocalSeparationSettings() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(PREF_LOCAL_SEPARATION_MODE, LocalSeparationMode.fromValue(mainViewModel.params.value.localSeparationMode).value)
            .apply()
    }

    internal fun updateLocalSeparationMode(mode: LocalSeparationMode) {
        if (LocalSeparationMode.fromValue(mainViewModel.params.value.localSeparationMode) == mode) {
            return
        }
        val session = activeGenerationSession
        val previousDefault = session?.let {
            defaultPreviewChoiceForMode(LocalSeparationMode.fromValue(mainViewModel.params.value.localSeparationMode), it.autoLocalChoice)
        }
        mainViewModel.updateLive { p -> p.copy(localSeparationMode = (mode).value) }
        saveLocalSeparationSettings()
        refreshActivePreviewOutputs(
            rebuildLocalCandidates = true,
            retargetFrom = previousDefault,
        )
    }

    internal fun updateLocalWorkflowToggle(name: String, enabled: Boolean) {
        val previous = when (name) {
            "background" -> mainViewModel.params.value.localBackgroundSeparationEnabled
            "adaptive" -> mainViewModel.params.value.localAdaptiveSelectionEnabled
            "corner" -> mainViewModel.params.value.localCornerMaskCleanupEnabled
            "alpha_edge_repair" -> mainViewModel.params.value.localAlphaEdgeColorRepairEnabled
            "plain_background" -> mainViewModel.params.value.localPlainBackgroundEstimationEnabled
            "original" -> mainViewModel.params.value.localOriginalCleanupEnabled
            "plate" -> mainViewModel.params.value.localPlateCleanupEnabled
            "plate_edge" -> mainViewModel.params.value.localPlateEdgeRepairEnabled
            "plate_residue" -> mainViewModel.params.value.localPlateResidueCleanupEnabled
            "shadow" -> mainViewModel.params.value.localShadowCleanupEnabled
            "shadow_edge" -> mainViewModel.params.value.localShadowEdgeRepairEnabled
            "edge_trim" -> mainViewModel.params.value.localEdgeTrimEnabled
            "composed" -> mainViewModel.params.value.localComposedBackgroundEnabled
            "two_layer" -> mainViewModel.params.value.localTwoLayerCandidateEnabled
            "component" -> mainViewModel.params.value.localComponentCandidatesEnabled
            "text_safe" -> mainViewModel.params.value.localTextSafeCandidateEnabled
            "auto" -> mainViewModel.params.value.localAutoSelectionEnabled
            "edge_polish" -> mainViewModel.params.value.localEdgePolishEnabled
            else -> return
        }
        if (previous == enabled) {
            return
        }
        when (name) {
            "background" -> mainViewModel.updateLive { p -> p.copy(localBackgroundSeparationEnabled = enabled) }
            "adaptive" -> mainViewModel.updateLive { p -> p.copy(localAdaptiveSelectionEnabled = enabled) }
            "corner" -> mainViewModel.updateLive { p -> p.copy(localCornerMaskCleanupEnabled = enabled) }
            "alpha_edge_repair" -> mainViewModel.updateLive { p -> p.copy(localAlphaEdgeColorRepairEnabled = enabled) }
            "plain_background" -> mainViewModel.updateLive { p -> p.copy(localPlainBackgroundEstimationEnabled = enabled) }
            "original" -> mainViewModel.updateLive { p -> p.copy(localOriginalCleanupEnabled = enabled) }
            "plate" -> mainViewModel.updateLive { p -> p.copy(localPlateCleanupEnabled = enabled) }
            "plate_edge" -> mainViewModel.updateLive { p -> p.copy(localPlateEdgeRepairEnabled = enabled) }
            "plate_residue" -> mainViewModel.updateLive { p -> p.copy(localPlateResidueCleanupEnabled = enabled) }
            "shadow" -> mainViewModel.updateLive { p -> p.copy(localShadowCleanupEnabled = enabled) }
            "shadow_edge" -> mainViewModel.updateLive { p -> p.copy(localShadowEdgeRepairEnabled = enabled) }
            "edge_trim" -> mainViewModel.updateLive { p -> p.copy(localEdgeTrimEnabled = enabled) }
            "composed" -> mainViewModel.updateLive { p -> p.copy(localComposedBackgroundEnabled = enabled) }
            "two_layer" -> mainViewModel.updateLive { p -> p.copy(localTwoLayerCandidateEnabled = enabled) }
            "component" -> mainViewModel.updateLive { p -> p.copy(localComponentCandidatesEnabled = enabled) }
            "text_safe" -> mainViewModel.updateLive { p -> p.copy(localTextSafeCandidateEnabled = enabled) }
            "auto" -> mainViewModel.updateLive { p -> p.copy(localAutoSelectionEnabled = enabled) }
            "edge_polish" -> mainViewModel.updateLive { p -> p.copy(localEdgePolishEnabled = enabled) }
        }
        saveImageTuningSettings()
        statusText = if (enabled) "已启用本地步骤" else "已关闭本地步骤"
        refreshActivePreviewOutputs(rebuildLocalCandidates = true)
    }

    internal fun loadImageSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val storedValue = prefs.getInt(
            PREF_FOREGROUND_SUBJECT_PERCENT,
            DEFAULT_FOREGROUND_SUBJECT_PERCENT,
        )
        mainViewModel.updateLive { p -> p.copy(foregroundSubjectPercent = if (storedValue == LEGACY_FOREGROUND_SUBJECT_PERCENT) {
            DEFAULT_FOREGROUND_SUBJECT_PERCENT
        } else {
            storedValue.coerceIn(MIN_FOREGROUND_SUBJECT_PERCENT, MAX_FOREGROUND_SUBJECT_PERCENT)
        }) }
        draftForegroundSubjectPercentText = mainViewModel.params.value.foregroundSubjectPercent.toString()
        mainViewModel.updateLive { p -> p.copy(foregroundShadowLevel = prefs.getInt(
            PREF_FOREGROUND_SHADOW_LEVEL,
            DEFAULT_FOREGROUND_SHADOW_LEVEL,
        ).coerceIn(MIN_FOREGROUND_SHADOW_LEVEL, MAX_FOREGROUND_SHADOW_LEVEL)) }
        draftForegroundShadowLevelText = mainViewModel.params.value.foregroundShadowLevel.toString()
        mainViewModel.updateLive { p -> p.copy(monochromeThemeScale = prefs.getFloat(
            PREF_MONOCHROME_THEME_SCALE,
            DEFAULT_MONOCHROME_THEME_SCALE,
        ).coerceIn(MIN_MONOCHROME_THEME_SCALE, MAX_MONOCHROME_THEME_SCALE)) }
        draftMonochromeThemeScaleText = (mainViewModel.params.value.monochromeThemeScale * 100).roundToInt().toString()
        val tuningVersion = prefs.getInt(PREF_IMAGE_TUNING_VERSION, 1)
        mainViewModel.updateLive { p -> p.copy(backgroundSeparationPercent = if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
            DEFAULT_BACKGROUND_SEPARATION_PERCENT
        } else {
            prefs.getInt(PREF_BACKGROUND_SEPARATION_PERCENT, DEFAULT_BACKGROUND_SEPARATION_PERCENT)
                .let { migrateLegacyPercent(it, DEFAULT_BACKGROUND_SEPARATION_PERCENT) }
        }.coerceIn(MIN_BACKGROUND_SEPARATION_PERCENT, MAX_BACKGROUND_SEPARATION_PERCENT)) }
        draftBackgroundSeparationText = mainViewModel.params.value.backgroundSeparationPercent.toString()
        mainViewModel.updateLive { p -> p.copy(plateRemovalPercent = if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
            DEFAULT_PLATE_REMOVAL_PERCENT
        } else {
            prefs.getInt(PREF_PLATE_REMOVAL_PERCENT, DEFAULT_PLATE_REMOVAL_PERCENT)
                .let { migrateLegacyPercent(it, DEFAULT_PLATE_REMOVAL_PERCENT) }
        }.coerceIn(MIN_PLATE_REMOVAL_PERCENT, MAX_PLATE_REMOVAL_PERCENT)) }
        draftPlateRemovalText = mainViewModel.params.value.plateRemovalPercent.toString()
        mainViewModel.updateLive { p -> p.copy(shadowRemovalPercent = if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
            DEFAULT_SHADOW_REMOVAL_PERCENT
        } else {
            prefs.getInt(PREF_SHADOW_REMOVAL_PERCENT, DEFAULT_SHADOW_REMOVAL_PERCENT)
                .let { migrateLegacyPercent(it, DEFAULT_SHADOW_REMOVAL_PERCENT) }
        }.coerceIn(MIN_SHADOW_REMOVAL_PERCENT, MAX_SHADOW_REMOVAL_PERCENT)) }
        draftShadowRemovalText = mainViewModel.params.value.shadowRemovalPercent.toString()
        mainViewModel.updateLive { p -> p.copy(edgePolishPercent = if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
            DEFAULT_EDGE_POLISH_PERCENT
        } else {
            prefs.getInt(PREF_EDGE_POLISH_PERCENT, DEFAULT_EDGE_POLISH_PERCENT)
        }.coerceIn(MIN_EDGE_POLISH_PERCENT, MAX_EDGE_POLISH_PERCENT)) }
        draftEdgePolishText = mainViewModel.params.value.edgePolishPercent.toString()
        mainViewModel.updateLive { p -> p.copy(rmbgAlphaStrengthPercent = prefs.getInt(
            PREF_RMBG_ALPHA_STRENGTH_PERCENT,
            DEFAULT_RMBG_ALPHA_STRENGTH_PERCENT,
        ).coerceIn(MIN_RMBG_ALPHA_STRENGTH_PERCENT, MAX_RMBG_ALPHA_STRENGTH_PERCENT)) }
        draftRmbgAlphaStrengthText = mainViewModel.params.value.rmbgAlphaStrengthPercent.toString()
        mainViewModel.updateLive { p -> p.copy(rmbgEdgeFeatherPercent = prefs.getInt(
            PREF_RMBG_EDGE_FEATHER_PERCENT,
            DEFAULT_RMBG_EDGE_FEATHER_PERCENT,
        ).coerceIn(MIN_RMBG_EDGE_FEATHER_PERCENT, MAX_RMBG_EDGE_FEATHER_PERCENT)) }
        draftRmbgEdgeFeatherText = mainViewModel.params.value.rmbgEdgeFeatherPercent.toString()
        mainViewModel.updateLive { p -> p.copy(rmbgEdgeAdjustPercent = prefs.getInt(
            PREF_RMBG_EDGE_ADJUST_PERCENT,
            DEFAULT_RMBG_EDGE_ADJUST_PERCENT,
        ).coerceIn(MIN_RMBG_EDGE_ADJUST_PERCENT, MAX_RMBG_EDGE_ADJUST_PERCENT)) }
        draftRmbgEdgeAdjustText = mainViewModel.params.value.rmbgEdgeAdjustPercent.toString()
        mainViewModel.updateLive { p -> p.copy(rmbgWeakAlphaKeepPercent = prefs.getInt(
            PREF_RMBG_WEAK_ALPHA_KEEP_PERCENT,
            DEFAULT_RMBG_WEAK_ALPHA_KEEP_PERCENT,
        ).coerceIn(MIN_RMBG_WEAK_ALPHA_KEEP_PERCENT, MAX_RMBG_WEAK_ALPHA_KEEP_PERCENT)) }
        draftRmbgWeakAlphaKeepText = mainViewModel.params.value.rmbgWeakAlphaKeepPercent.toString()
        mainViewModel.updateLive { p -> p.copy(adaptiveForegroundMode = (if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
            AdaptiveForegroundMode.Auto
        } else {
            AdaptiveForegroundMode.fromValue(
                prefs.getString(PREF_ADAPTIVE_FOREGROUND_MODE, AdaptiveForegroundMode.Auto.value),
            )
        }).value) }
        mainViewModel.updateLive { p -> p.copy(adaptiveDirectMaxCoveragePercent = prefs.getInt(
            PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT,
            DEFAULT_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT,
        ).coerceIn(MIN_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT, MAX_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT)) }
        mainViewModel.updateLive { p -> p.copy(adaptiveDirectMaxCoverageIncreasePercent = prefs.getInt(
            PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
            DEFAULT_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
        ).coerceIn(
            MIN_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
            MAX_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
        )) }
        mainViewModel.updateLive { p -> p.copy(adaptiveMaskEdgeCoveragePercent = prefs.getInt(
            PREF_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT,
            DEFAULT_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT,
        ).coerceIn(MIN_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT, MAX_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT)) }
        mainViewModel.updateLive { p -> p.copy(adaptiveMaskMinCoveragePercent = prefs.getInt(
            PREF_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT,
            DEFAULT_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT,
        ).coerceIn(MIN_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT, MAX_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT)) }
        mainViewModel.updateLive { p -> p.copy(adaptiveCenterEpsilonPercent = prefs.getInt(
            PREF_ADAPTIVE_CENTER_EPSILON_PERCENT,
            DEFAULT_ADAPTIVE_CENTER_EPSILON_PERCENT,
        ).coerceIn(MIN_ADAPTIVE_CENTER_EPSILON_PERCENT, MAX_ADAPTIVE_CENTER_EPSILON_PERCENT)) }
        mainViewModel.updateLive { p -> p.copy(originalForegroundCleanupMode = (if (tuningVersion < CURRENT_IMAGE_TUNING_VERSION) {
            OriginalForegroundCleanupMode.Auto
        } else {
            OriginalForegroundCleanupMode.fromValue(
                prefs.getString(PREF_ORIGINAL_FOREGROUND_CLEANUP_MODE, OriginalForegroundCleanupMode.Auto.value),
            )
        }).value) }
        mainViewModel.updateLive { p -> p.copy(localBackgroundSeparationEnabled = prefs.getBoolean(PREF_LOCAL_BACKGROUND_SEPARATION_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(localAdaptiveSelectionEnabled = prefs.getBoolean(PREF_LOCAL_ADAPTIVE_SELECTION_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(localCornerMaskCleanupEnabled = prefs.getBoolean(PREF_LOCAL_CORNER_MASK_CLEANUP_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(localAlphaEdgeColorRepairEnabled = prefs.getBoolean(PREF_LOCAL_ALPHA_EDGE_COLOR_REPAIR_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(localPlainBackgroundEstimationEnabled = prefs.getBoolean(PREF_LOCAL_PLAIN_BACKGROUND_ESTIMATION_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(localOriginalCleanupEnabled = prefs.getBoolean(PREF_LOCAL_ORIGINAL_CLEANUP_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(localPlateCleanupEnabled = prefs.getBoolean(PREF_LOCAL_PLATE_CLEANUP_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(localPlateEdgeRepairEnabled = prefs.getBoolean(PREF_LOCAL_PLATE_EDGE_REPAIR_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(localPlateResidueCleanupEnabled = prefs.getBoolean(PREF_LOCAL_PLATE_RESIDUE_CLEANUP_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(localShadowCleanupEnabled = prefs.getBoolean(PREF_LOCAL_SHADOW_CLEANUP_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(localShadowEdgeRepairEnabled = prefs.getBoolean(PREF_LOCAL_SHADOW_EDGE_REPAIR_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(localEdgeTrimEnabled = prefs.getBoolean(PREF_LOCAL_EDGE_TRIM_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(localComposedBackgroundEnabled = prefs.getBoolean(PREF_LOCAL_COMPOSED_BACKGROUND_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(localTwoLayerCandidateEnabled = prefs.getBoolean(PREF_LOCAL_TWO_LAYER_CANDIDATE_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(localComponentCandidatesEnabled = prefs.getBoolean(PREF_LOCAL_COMPONENT_CANDIDATES_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(localTextSafeCandidateEnabled = prefs.getBoolean(PREF_LOCAL_TEXT_SAFE_CANDIDATE_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(localAutoSelectionEnabled = prefs.getBoolean(PREF_LOCAL_AUTO_SELECTION_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(localEdgePolishEnabled = prefs.getBoolean(PREF_LOCAL_EDGE_POLISH_ENABLED, true)) }
        mainViewModel.updateLive { p -> p.copy(nightSubjectLightBackgroundEnabled = prefs.getBoolean(
            PREF_NIGHT_SUBJECT_LIGHT_BACKGROUND_ENABLED,
            false,
        )) }
        prefs.edit()
            .putInt(PREF_FOREGROUND_SUBJECT_PERCENT, mainViewModel.params.value.foregroundSubjectPercent)
            .putInt(PREF_FOREGROUND_SHADOW_LEVEL, mainViewModel.params.value.foregroundShadowLevel)
            .putFloat(PREF_MONOCHROME_THEME_SCALE, mainViewModel.params.value.monochromeThemeScale)
            .putInt(PREF_BACKGROUND_SEPARATION_PERCENT, mainViewModel.params.value.backgroundSeparationPercent)
            .putInt(PREF_PLATE_REMOVAL_PERCENT, mainViewModel.params.value.plateRemovalPercent)
            .putInt(PREF_SHADOW_REMOVAL_PERCENT, mainViewModel.params.value.shadowRemovalPercent)
            .putInt(PREF_EDGE_POLISH_PERCENT, mainViewModel.params.value.edgePolishPercent)
            .putInt(PREF_RMBG_ALPHA_STRENGTH_PERCENT, mainViewModel.params.value.rmbgAlphaStrengthPercent)
            .putInt(PREF_RMBG_EDGE_FEATHER_PERCENT, mainViewModel.params.value.rmbgEdgeFeatherPercent)
            .putInt(PREF_RMBG_EDGE_ADJUST_PERCENT, mainViewModel.params.value.rmbgEdgeAdjustPercent)
            .putInt(PREF_RMBG_WEAK_ALPHA_KEEP_PERCENT, mainViewModel.params.value.rmbgWeakAlphaKeepPercent)
            .putString(PREF_ADAPTIVE_FOREGROUND_MODE, AdaptiveForegroundMode.fromValue(mainViewModel.params.value.adaptiveForegroundMode).value)
            .putInt(PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT, mainViewModel.params.value.adaptiveDirectMaxCoveragePercent)
            .putInt(PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT, mainViewModel.params.value.adaptiveDirectMaxCoverageIncreasePercent)
            .putInt(PREF_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT, mainViewModel.params.value.adaptiveMaskEdgeCoveragePercent)
            .putInt(PREF_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT, mainViewModel.params.value.adaptiveMaskMinCoveragePercent)
            .putInt(PREF_ADAPTIVE_CENTER_EPSILON_PERCENT, mainViewModel.params.value.adaptiveCenterEpsilonPercent)
            .putString(PREF_ORIGINAL_FOREGROUND_CLEANUP_MODE, OriginalForegroundCleanupMode.fromValue(mainViewModel.params.value.originalForegroundCleanupMode).value)
            .putBoolean(PREF_LOCAL_BACKGROUND_SEPARATION_ENABLED, mainViewModel.params.value.localBackgroundSeparationEnabled)
            .putBoolean(PREF_LOCAL_ADAPTIVE_SELECTION_ENABLED, mainViewModel.params.value.localAdaptiveSelectionEnabled)
            .putBoolean(PREF_LOCAL_CORNER_MASK_CLEANUP_ENABLED, mainViewModel.params.value.localCornerMaskCleanupEnabled)
            .putBoolean(PREF_LOCAL_ALPHA_EDGE_COLOR_REPAIR_ENABLED, mainViewModel.params.value.localAlphaEdgeColorRepairEnabled)
            .putBoolean(PREF_LOCAL_PLAIN_BACKGROUND_ESTIMATION_ENABLED, mainViewModel.params.value.localPlainBackgroundEstimationEnabled)
            .putBoolean(PREF_LOCAL_ORIGINAL_CLEANUP_ENABLED, mainViewModel.params.value.localOriginalCleanupEnabled)
            .putBoolean(PREF_LOCAL_PLATE_CLEANUP_ENABLED, mainViewModel.params.value.localPlateCleanupEnabled)
            .putBoolean(PREF_LOCAL_PLATE_EDGE_REPAIR_ENABLED, mainViewModel.params.value.localPlateEdgeRepairEnabled)
            .putBoolean(PREF_LOCAL_PLATE_RESIDUE_CLEANUP_ENABLED, mainViewModel.params.value.localPlateResidueCleanupEnabled)
            .putBoolean(PREF_LOCAL_SHADOW_CLEANUP_ENABLED, mainViewModel.params.value.localShadowCleanupEnabled)
            .putBoolean(PREF_LOCAL_SHADOW_EDGE_REPAIR_ENABLED, mainViewModel.params.value.localShadowEdgeRepairEnabled)
            .putBoolean(PREF_LOCAL_EDGE_TRIM_ENABLED, mainViewModel.params.value.localEdgeTrimEnabled)
            .putBoolean(PREF_LOCAL_COMPOSED_BACKGROUND_ENABLED, mainViewModel.params.value.localComposedBackgroundEnabled)
            .putBoolean(PREF_LOCAL_TWO_LAYER_CANDIDATE_ENABLED, mainViewModel.params.value.localTwoLayerCandidateEnabled)
            .putBoolean(PREF_LOCAL_COMPONENT_CANDIDATES_ENABLED, mainViewModel.params.value.localComponentCandidatesEnabled)
            .putBoolean(PREF_LOCAL_TEXT_SAFE_CANDIDATE_ENABLED, mainViewModel.params.value.localTextSafeCandidateEnabled)
            .putBoolean(PREF_LOCAL_AUTO_SELECTION_ENABLED, mainViewModel.params.value.localAutoSelectionEnabled)
            .putBoolean(PREF_LOCAL_EDGE_POLISH_ENABLED, mainViewModel.params.value.localEdgePolishEnabled)
            .putBoolean(PREF_NIGHT_SUBJECT_LIGHT_BACKGROUND_ENABLED, mainViewModel.params.value.nightSubjectLightBackgroundEnabled)
            .putInt(PREF_IMAGE_TUNING_VERSION, CURRENT_IMAGE_TUNING_VERSION)
            .putBoolean(PREF_FOREGROUND_SUBJECT_PERCENT_MIGRATED, true)
            .apply()
    }

    internal fun updateForegroundSubjectPercent(value: Int) {
        mainViewModel.updateLive { p -> p.copy(foregroundSubjectPercent = value.coerceIn(
            MIN_FOREGROUND_SUBJECT_PERCENT,
            MAX_FOREGROUND_SUBJECT_PERCENT,
        )) }
        draftForegroundSubjectPercentText = mainViewModel.params.value.foregroundSubjectPercent.toString()
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putInt(PREF_FOREGROUND_SUBJECT_PERCENT, mainViewModel.params.value.foregroundSubjectPercent)
            .apply()
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updateForegroundShadowLevel(value: Int) {
        mainViewModel.updateLive { p -> p.copy(foregroundShadowLevel = value.coerceIn(
            MIN_FOREGROUND_SHADOW_LEVEL,
            MAX_FOREGROUND_SHADOW_LEVEL,
        )) }
        draftForegroundShadowLevelText = mainViewModel.params.value.foregroundShadowLevel.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun migrateLegacyPercent(value: Int, fallback: Int): Int =
        when {
            value in 1..100 -> value
            value <= 0 -> 1
            else -> fallback
        }

    internal fun updateMonochromeThemeScalePercent(value: Int) {
        val percent = value.coerceIn(MIN_MONOCHROME_THEME_SCALE_PERCENT, MAX_MONOCHROME_THEME_SCALE_PERCENT)
        mainViewModel.updateLive { p -> p.copy(monochromeThemeScale = (percent.toFloat() / 100f).coerceIn(
            MIN_MONOCHROME_THEME_SCALE,
            MAX_MONOCHROME_THEME_SCALE,
        )) }
        draftMonochromeThemeScaleText = percent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updateBackgroundSeparationPercent(value: Int) {
        mainViewModel.updateLive { p -> p.copy(backgroundSeparationPercent = value.coerceIn(
            MIN_BACKGROUND_SEPARATION_PERCENT,
            MAX_BACKGROUND_SEPARATION_PERCENT,
        )) }
        draftBackgroundSeparationText = mainViewModel.params.value.backgroundSeparationPercent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = true)
    }

    internal fun updatePlateRemovalPercent(value: Int) {
        mainViewModel.updateLive { p -> p.copy(plateRemovalPercent = value.coerceIn(
            MIN_PLATE_REMOVAL_PERCENT,
            MAX_PLATE_REMOVAL_PERCENT,
        )) }
        draftPlateRemovalText = mainViewModel.params.value.plateRemovalPercent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = true)
    }

    internal fun updateShadowRemovalPercent(value: Int) {
        mainViewModel.updateLive { p -> p.copy(shadowRemovalPercent = value.coerceIn(
            MIN_SHADOW_REMOVAL_PERCENT,
            MAX_SHADOW_REMOVAL_PERCENT,
        )) }
        draftShadowRemovalText = mainViewModel.params.value.shadowRemovalPercent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = true)
    }

    internal fun updateEdgePolishPercent(value: Int) {
        mainViewModel.updateLive { p -> p.copy(edgePolishPercent = value.coerceIn(
            MIN_EDGE_POLISH_PERCENT,
            MAX_EDGE_POLISH_PERCENT,
        )) }
        draftEdgePolishText = mainViewModel.params.value.edgePolishPercent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updateRmbgAlphaStrengthPercent(value: Int) {
        mainViewModel.updateLive { p -> p.copy(rmbgAlphaStrengthPercent = value.coerceIn(
            MIN_RMBG_ALPHA_STRENGTH_PERCENT,
            MAX_RMBG_ALPHA_STRENGTH_PERCENT,
        )) }
        draftRmbgAlphaStrengthText = mainViewModel.params.value.rmbgAlphaStrengthPercent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updateRmbgEdgeFeatherPercent(value: Int) {
        mainViewModel.updateLive { p -> p.copy(rmbgEdgeFeatherPercent = value.coerceIn(
            MIN_RMBG_EDGE_FEATHER_PERCENT,
            MAX_RMBG_EDGE_FEATHER_PERCENT,
        )) }
        draftRmbgEdgeFeatherText = mainViewModel.params.value.rmbgEdgeFeatherPercent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updateRmbgEdgeAdjustPercent(value: Int) {
        mainViewModel.updateLive { p -> p.copy(rmbgEdgeAdjustPercent = value.coerceIn(
            MIN_RMBG_EDGE_ADJUST_PERCENT,
            MAX_RMBG_EDGE_ADJUST_PERCENT,
        )) }
        draftRmbgEdgeAdjustText = mainViewModel.params.value.rmbgEdgeAdjustPercent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updateRmbgWeakAlphaKeepPercent(value: Int) {
        mainViewModel.updateLive { p -> p.copy(rmbgWeakAlphaKeepPercent = value.coerceIn(
            MIN_RMBG_WEAK_ALPHA_KEEP_PERCENT,
            MAX_RMBG_WEAK_ALPHA_KEEP_PERCENT,
        )) }
        draftRmbgWeakAlphaKeepText = mainViewModel.params.value.rmbgWeakAlphaKeepPercent.toString()
        saveImageTuningSettings()
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun currentRmbgModelPreset(): RmbgModelPreset {
        val url = rmbgComponentUrl.trim()
        return RMBG_MODEL_PRESETS.firstOrNull { preset ->
            preset.url.isNotBlank() && preset.url == url
        } ?: RMBG_MODEL_PRESET_CUSTOM
    }

    internal fun updateRmbgModelPreset(preset: RmbgModelPreset) {
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

    internal fun rmbgInferenceStatusSummary(): String {
        if (isGeneratingRmbgCandidate) {
            return rmbgCandidateStatusText.ifBlank { "RMBG运行中" }
        }
        val report = lastRmbgInferenceReport
        if (report != null) {
            return "${report.actualBackend.label}，耗时 ${report.elapsedMs}ms"
        }
        return "尚未运行"
    }

    internal fun saveImageTuningSettings() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putInt(PREF_FOREGROUND_SUBJECT_PERCENT, mainViewModel.params.value.foregroundSubjectPercent)
            .putInt(PREF_FOREGROUND_SHADOW_LEVEL, mainViewModel.params.value.foregroundShadowLevel)
            .putFloat(PREF_MONOCHROME_THEME_SCALE, mainViewModel.params.value.monochromeThemeScale)
            .putInt(PREF_BACKGROUND_SEPARATION_PERCENT, mainViewModel.params.value.backgroundSeparationPercent)
            .putInt(PREF_PLATE_REMOVAL_PERCENT, mainViewModel.params.value.plateRemovalPercent)
            .putInt(PREF_SHADOW_REMOVAL_PERCENT, mainViewModel.params.value.shadowRemovalPercent)
            .putInt(PREF_EDGE_POLISH_PERCENT, mainViewModel.params.value.edgePolishPercent)
            .putInt(PREF_RMBG_ALPHA_STRENGTH_PERCENT, mainViewModel.params.value.rmbgAlphaStrengthPercent)
            .putInt(PREF_RMBG_EDGE_FEATHER_PERCENT, mainViewModel.params.value.rmbgEdgeFeatherPercent)
            .putInt(PREF_RMBG_EDGE_ADJUST_PERCENT, mainViewModel.params.value.rmbgEdgeAdjustPercent)
            .putInt(PREF_RMBG_WEAK_ALPHA_KEEP_PERCENT, mainViewModel.params.value.rmbgWeakAlphaKeepPercent)
            .putString(PREF_ADAPTIVE_FOREGROUND_MODE, AdaptiveForegroundMode.fromValue(mainViewModel.params.value.adaptiveForegroundMode).value)
            .putInt(PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT, mainViewModel.params.value.adaptiveDirectMaxCoveragePercent)
            .putInt(PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT, mainViewModel.params.value.adaptiveDirectMaxCoverageIncreasePercent)
            .putInt(PREF_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT, mainViewModel.params.value.adaptiveMaskEdgeCoveragePercent)
            .putInt(PREF_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT, mainViewModel.params.value.adaptiveMaskMinCoveragePercent)
            .putInt(PREF_ADAPTIVE_CENTER_EPSILON_PERCENT, mainViewModel.params.value.adaptiveCenterEpsilonPercent)
            .putString(PREF_ORIGINAL_FOREGROUND_CLEANUP_MODE, OriginalForegroundCleanupMode.fromValue(mainViewModel.params.value.originalForegroundCleanupMode).value)
            .putBoolean(PREF_LOCAL_BACKGROUND_SEPARATION_ENABLED, mainViewModel.params.value.localBackgroundSeparationEnabled)
            .putBoolean(PREF_LOCAL_ADAPTIVE_SELECTION_ENABLED, mainViewModel.params.value.localAdaptiveSelectionEnabled)
            .putBoolean(PREF_LOCAL_CORNER_MASK_CLEANUP_ENABLED, mainViewModel.params.value.localCornerMaskCleanupEnabled)
            .putBoolean(PREF_LOCAL_ALPHA_EDGE_COLOR_REPAIR_ENABLED, mainViewModel.params.value.localAlphaEdgeColorRepairEnabled)
            .putBoolean(PREF_LOCAL_PLAIN_BACKGROUND_ESTIMATION_ENABLED, mainViewModel.params.value.localPlainBackgroundEstimationEnabled)
            .putBoolean(PREF_LOCAL_ORIGINAL_CLEANUP_ENABLED, mainViewModel.params.value.localOriginalCleanupEnabled)
            .putBoolean(PREF_LOCAL_PLATE_CLEANUP_ENABLED, mainViewModel.params.value.localPlateCleanupEnabled)
            .putBoolean(PREF_LOCAL_PLATE_EDGE_REPAIR_ENABLED, mainViewModel.params.value.localPlateEdgeRepairEnabled)
            .putBoolean(PREF_LOCAL_PLATE_RESIDUE_CLEANUP_ENABLED, mainViewModel.params.value.localPlateResidueCleanupEnabled)
            .putBoolean(PREF_LOCAL_SHADOW_CLEANUP_ENABLED, mainViewModel.params.value.localShadowCleanupEnabled)
            .putBoolean(PREF_LOCAL_SHADOW_EDGE_REPAIR_ENABLED, mainViewModel.params.value.localShadowEdgeRepairEnabled)
            .putBoolean(PREF_LOCAL_EDGE_TRIM_ENABLED, mainViewModel.params.value.localEdgeTrimEnabled)
            .putBoolean(PREF_LOCAL_COMPOSED_BACKGROUND_ENABLED, mainViewModel.params.value.localComposedBackgroundEnabled)
            .putBoolean(PREF_LOCAL_TWO_LAYER_CANDIDATE_ENABLED, mainViewModel.params.value.localTwoLayerCandidateEnabled)
            .putBoolean(PREF_LOCAL_COMPONENT_CANDIDATES_ENABLED, mainViewModel.params.value.localComponentCandidatesEnabled)
            .putBoolean(PREF_LOCAL_TEXT_SAFE_CANDIDATE_ENABLED, mainViewModel.params.value.localTextSafeCandidateEnabled)
            .putBoolean(PREF_LOCAL_AUTO_SELECTION_ENABLED, mainViewModel.params.value.localAutoSelectionEnabled)
            .putBoolean(PREF_LOCAL_EDGE_POLISH_ENABLED, mainViewModel.params.value.localEdgePolishEnabled)
            .putBoolean(PREF_NIGHT_SUBJECT_LIGHT_BACKGROUND_ENABLED, mainViewModel.params.value.nightSubjectLightBackgroundEnabled)
            .putInt(PREF_IMAGE_TUNING_VERSION, CURRENT_IMAGE_TUNING_VERSION)
            .apply()
    }

    /** 汇总当前全部调参字段为不可变快照（预设保存、撤销、debug 往返共用）。 */
    internal fun currentTuningParams(): TuningParams =
        mainViewModel.params.value

    internal fun currentLocalPipelineConfig(): LocalPipelineConfig =
        LocalPipelineConfig.from(currentTuningParams())

    /**
     * 应用一份参数快照：写全部字段 + draft 文本，可选持久化并刷新预览。
     * 预设、debug HTTP、撤销/批量都走这里。
     */
    internal fun applyTuningParams(
        params: TuningParams,
        rebuildCandidates: Boolean = true,
        persist: Boolean = true,
        captureUndo: Boolean = true,
        refreshPreview: Boolean = true,
    ) {
        val before = currentTuningParams()
        if (captureUndo) {
            lastParamsSnapshot = before
        }
        // P2 交界：历史/快照单源在 MainViewModel，用快照显式同步（VM 不读 Activity 字段）；
        // 186 live vars 仍是 UI 真源（P5 重写），applied 传本函数收到的快照参数。
        mainViewModel.onParamsApplied(before = before, applied = params, captureUndo = captureUndo)
        draftForegroundSubjectPercentText = params.foregroundSubjectPercent.toString()
        draftForegroundShadowLevelText = params.foregroundShadowLevel.toString()
        draftMonochromeThemeScaleText = (params.monochromeThemeScale * 100).roundToInt().toString()
        draftBackgroundSeparationText = params.backgroundSeparationPercent.toString()
        draftPlateRemovalText = params.plateRemovalPercent.toString()
        draftShadowRemovalText = params.shadowRemovalPercent.toString()
        draftEdgePolishText = params.edgePolishPercent.toString()
        draftRmbgAlphaStrengthText = params.rmbgAlphaStrengthPercent.toString()
        draftRmbgEdgeFeatherText = params.rmbgEdgeFeatherPercent.toString()
        draftRmbgEdgeAdjustText = params.rmbgEdgeAdjustPercent.toString()
        draftRmbgWeakAlphaKeepText = params.rmbgWeakAlphaKeepPercent.toString()
        draftLiquidGlassRadiusText = params.liquidGlassRadius.toString()
        draftLiquidGlassOuterWidthText = params.liquidGlassOuterWidth.toString()
        draftLiquidGlassTopAlphaText = params.liquidGlassTopAlpha.toString()
        draftLiquidGlassBottomAlphaText = params.liquidGlassBottomAlpha.toString()
        draftLiquidGlassBackgroundMistAlphaText = params.liquidGlassBackgroundMistAlpha.toString()
        draftLiquidGlassBottomDarkAlphaText = params.liquidGlassBottomDarkAlpha.toString()
        draftLiquidGlassSubjectScaleText = params.liquidGlassSubjectScalePercent.toString()
        draftLiquidGlassSubjectOutlineWidthText = params.liquidGlassSubjectOutlineWidth.toString()
        draftLiquidGlassSubjectInnerOutlineWidthText = params.liquidGlassSubjectInnerOutlineWidth.toString()
        draftLiquidGlassSubjectShadowAlphaText = params.liquidGlassSubjectShadowAlpha.toString()
        draftLiquidGlassSubjectOpacityText = params.liquidGlassSubjectOpacityPercent.toString()
        draftJsonParamsText = params.toJson().toString(4)
        if (persist) {
            saveLocalSeparationSettings()
            saveImageTuningSettings()
            saveLiquidGlassSettings()
            if (
                before.gptImageMode != params.gptImageMode ||
                before.gptPromptPreset != params.gptPromptPreset ||
                before.gptCustomPrompt != params.gptCustomPrompt
            ) {
                saveGptSettings()
            }
            saveUiState()
        }
        if (refreshPreview && !isBusy && activeGenerationSession != null) {
            refreshActivePreviewOutputs(rebuildLocalCandidates = rebuildCandidates)
        }
    }

    /** 撤销上一次参数应用（预设/批量前自动捕获快照）。 */
    internal fun restoreLastParams() {
        val snapshot = lastParamsSnapshot ?: run {
            statusText = "没有可还原的参数"
            return
        }
        lastParamsSnapshot = null
        applyTuningParams(snapshot, captureUndo = false)
        statusText = "已还原上一个参数"
    }

    /**
     * 恢复默认配置（Issue #4）。
     * 仅重置全部调参到出厂默认值（TuningParams 默认构造），不清除已下载的 RMBG 模型与已生成的图标包。
     * 通过 TuningParams 默认值 + applyTuningParams 统一持久化，保证与各迁移逻辑一致。
     */
    internal fun resetToDefaults(confirmed: Boolean = false) {
        if (isBusy || isGeneratingGptCandidate || isGeneratingRmbgCandidate) {
            statusText = "当前有任务在运行，请等待"
            return
        }
        if (!confirmed) {
            requestServiceConfirm(
                title = "恢复默认配置",
                message = "将把全部调参恢复为出厂默认值（不影响已下载的 RMBG 模型与已生成的图标包），可通过「还原上一步」撤销。确认继续？",
                confirmLabel = "恢复默认",
            ) { resetToDefaults(confirmed = true) }
            return
        }
        val defaults = TuningParams()
        applyTuningParams(defaults, rebuildCandidates = true)
        presetStore.activePresetId = null
        activePresetId = null
        statusText = "已恢复默认配置"
    }

    internal fun initTuningHistory() {
        // P2 交界：历史基线进 MainViewModel（冷启动时快照显式同步一次）。
        mainViewModel.resetHistory(currentTuningParams())
    }

    internal fun undoTuning() {
        if (isBusy || isGeneratingGptCandidate || isGeneratingRmbgCandidate) {
            statusText = "当前有任务在运行，请等待"
            return
        }
        // P2 交界：取栈顶目标由 MainViewModel 判定（null 即已到最早），UI 状态留 Activity。
        val target = mainViewModel.undo()
        if (target == null) {
            statusText = "已到最早的配置"
            return
        }
        applyTuningParams(target, captureUndo = false)
        presetStore.activePresetId = null
        activePresetId = null
        statusText = "已后退到上一个配置"
    }

    internal fun redoTuning() {
        if (isBusy || isGeneratingGptCandidate || isGeneratingRmbgCandidate) {
            statusText = "当前有任务在运行，请等待"
            return
        }
        // P2 交界：取栈顶目标由 MainViewModel 判定（null 即已到最新），UI 状态留 Activity。
        val target = mainViewModel.redo()
        if (target == null) {
            statusText = "已到最新的配置"
            return
        }
        applyTuningParams(target, captureUndo = false)
        presetStore.activePresetId = null
        activePresetId = null
        statusText = "已前进到下一个配置"
    }

    /** 启动时统一加载调参相关设置（保留各迁移分支）。 */
    internal fun loadTuningParams() {
        loadLocalSeparationSettings()
        loadImageSettings()
        loadLiquidGlassSettings()
        draftJsonParamsText = currentTuningParams().toJson().toString(4)
    }

    internal fun updateNightSubjectLightBackgroundEnabled(enabled: Boolean) {
        if (mainViewModel.params.value.nightSubjectLightBackgroundEnabled == enabled) {
            return
        }
        mainViewModel.updateLive { p -> p.copy(nightSubjectLightBackgroundEnabled = enabled) }
        saveImageTuningSettings()
        statusText = if (enabled) {
            "标准暗色已开启填充背景色"
        } else {
            "标准暗色已关闭填充背景色"
        }
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updatePreviewCornerRadiusDp(value: Int) {
        val next = value.coerceIn(MIN_PREVIEW_CORNER_RADIUS_DP, MAX_PREVIEW_CORNER_RADIUS_DP)
        previewCornerRadiusDp = next
        draftPreviewCornerRadiusDpText = next.toString()
        saveUiState()
    }

    internal fun updatePreviewIconSizeDp(value: Int) {
        val next = value.coerceIn(MIN_PREVIEW_ICON_SIZE_DP, MAX_PREVIEW_ICON_SIZE_DP)
        previewIconSizeDp = next
        draftPreviewIconSizeDpText = next.toString()
        saveUiState()
    }

    internal fun updateBatchPreviewCount(value: Int) {
        val next = value.coerceIn(MIN_BATCH_PREVIEW_COUNT, MAX_BATCH_PREVIEW_COUNT)
        batchPreviewCount = next
        draftBatchPreviewCountText = next.toString()
        saveUiState()
    }

    internal fun updateBatchPreviewColumns(value: Int) {
        val next = value.coerceIn(2, 5)
        batchPreviewColumns = next
        draftBatchPreviewColumnsText = next.toString()
        val autoSize = when (next) {
            2 -> 72
            3 -> 64
            4 -> 54
            5 -> 46
            else -> 54
        }
        batchPreviewIconSizeDp = autoSize
        draftBatchPreviewIconSizeDpText = autoSize.toString()
        saveUiState()
    }

    internal fun updateBatchPreviewIconSizeDp(value: Int) {
        val next = value.coerceIn(40, 84)
        batchPreviewIconSizeDp = next
        draftBatchPreviewIconSizeDpText = next.toString()
        saveUiState()
    }

    internal fun updateBatchPreviewCornerRadiusDp(value: Int) {
        val next = value.coerceIn(0, 36)
        batchPreviewCornerRadiusDp = next
        draftBatchPreviewCornerRadiusDpText = next.toString()
        saveUiState()
    }

    internal fun updateBatchPreviewDesktopBackground(option: PreviewDesktopBackground) {
        if (batchPreviewDesktopBackground == option) {
            return
        }
        batchPreviewDesktopBackground = option
        saveUiState()
    }

    internal fun updatePreviewDesktopBackground(option: PreviewDesktopBackground) {
        if (previewDesktopBackground == option) {
            return
        }
        previewDesktopBackground = option
        saveUiState()
    }

    internal fun updatePreviewStripEnabled(enabled: Boolean) {
        if (previewStripEnabled == enabled) {
            return
        }
        previewStripEnabled = enabled
        saveUiState()
        statusText = if (enabled) {
            "已开启主页面顶部预览条"
        } else {
            "已关闭主页面顶部预览条"
        }
    }

    internal fun loadLiquidGlassSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val migratedToLayered = prefs.getBoolean(PREF_LIQUID_GLASS_LAYERED_MIGRATED, false)
        mainViewModel.updateLive { p -> p.copy(liquidGlassEnabled = if (migratedToLayered) {
            prefs.getBoolean(PREF_LIQUID_GLASS_ENABLED, true)
        } else {
            true
        }) }
        mainViewModel.updateLive { p -> p.copy(liquidGlassRadius = prefs.getInt(
            PREF_LIQUID_GLASS_RADIUS,
            DEFAULT_LIQUID_GLASS_RADIUS,
        ).coerceIn(MIN_LIQUID_GLASS_RADIUS, MAX_LIQUID_GLASS_RADIUS)) }
        draftLiquidGlassRadiusText = mainViewModel.params.value.liquidGlassRadius.toString()
        mainViewModel.updateLive { p -> p.copy(liquidGlassOuterWidth = prefs.getInt(
            PREF_LIQUID_GLASS_OUTER_WIDTH,
            prefs.getInt(PREF_LIQUID_GLASS_BACKGROUND_LEVEL_LEGACY, DEFAULT_LIQUID_GLASS_OUTER_WIDTH),
        ).coerceIn(MIN_LIQUID_GLASS_OUTER_WIDTH, MAX_LIQUID_GLASS_OUTER_WIDTH)) }
        draftLiquidGlassOuterWidthText = mainViewModel.params.value.liquidGlassOuterWidth.toString()
        mainViewModel.updateLive { p -> p.copy(liquidGlassTopAlpha = prefs.getInt(
            PREF_LIQUID_GLASS_TOP_ALPHA,
            DEFAULT_LIQUID_GLASS_TOP_ALPHA,
        ).coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)) }
        draftLiquidGlassTopAlphaText = mainViewModel.params.value.liquidGlassTopAlpha.toString()
        mainViewModel.updateLive { p -> p.copy(liquidGlassBottomAlpha = prefs.getInt(
            PREF_LIQUID_GLASS_BOTTOM_ALPHA,
            DEFAULT_LIQUID_GLASS_BOTTOM_ALPHA,
        ).coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)) }
        draftLiquidGlassBottomAlphaText = mainViewModel.params.value.liquidGlassBottomAlpha.toString()
        mainViewModel.updateLive { p -> p.copy(liquidGlassBackgroundMistAlpha = prefs.getInt(
            PREF_LIQUID_GLASS_BACKGROUND_MIST_ALPHA,
            DEFAULT_LIQUID_GLASS_BACKGROUND_MIST_ALPHA,
        ).coerceIn(MIN_LIQUID_GLASS_MIST_ALPHA, MAX_LIQUID_GLASS_MIST_ALPHA)) }
        draftLiquidGlassBackgroundMistAlphaText = mainViewModel.params.value.liquidGlassBackgroundMistAlpha.toString()
        mainViewModel.updateLive { p -> p.copy(liquidGlassBottomDarkAlpha = prefs.getInt(
            PREF_LIQUID_GLASS_BOTTOM_DARK_ALPHA,
            DEFAULT_LIQUID_GLASS_BOTTOM_DARK_ALPHA,
        ).coerceIn(MIN_LIQUID_GLASS_BOTTOM_DARK_ALPHA, MAX_LIQUID_GLASS_BOTTOM_DARK_ALPHA)) }
        draftLiquidGlassBottomDarkAlphaText = mainViewModel.params.value.liquidGlassBottomDarkAlpha.toString()
        mainViewModel.updateLive { p -> p.copy(liquidGlassSubjectScalePercent = prefs.getInt(
            PREF_LIQUID_GLASS_SUBJECT_SCALE_PERCENT,
            DEFAULT_LIQUID_GLASS_SUBJECT_SCALE_PERCENT,
        ).coerceIn(MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT, MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT)) }
        draftLiquidGlassSubjectScaleText = mainViewModel.params.value.liquidGlassSubjectScalePercent.toString()
        mainViewModel.updateLive { p -> p.copy(liquidGlassSubjectOutlineWidth = prefs.getInt(
            PREF_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
            DEFAULT_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
        ).coerceIn(MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH, MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH)) }
        draftLiquidGlassSubjectOutlineWidthText = mainViewModel.params.value.liquidGlassSubjectOutlineWidth.toString()
        mainViewModel.updateLive { p -> p.copy(liquidGlassSubjectInnerOutlineWidth = prefs.getInt(
            PREF_LIQUID_GLASS_SUBJECT_INNER_OUTLINE_WIDTH,
            DEFAULT_LIQUID_GLASS_SUBJECT_INNER_OUTLINE_WIDTH,
        ).coerceIn(MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH, MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH)) }
        draftLiquidGlassSubjectInnerOutlineWidthText = mainViewModel.params.value.liquidGlassSubjectInnerOutlineWidth.toString()
        mainViewModel.updateLive { p -> p.copy(liquidGlassSubjectShadowAlpha = prefs.getInt(
            PREF_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA,
            DEFAULT_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA,
        ).coerceIn(MIN_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA, MAX_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA)) }
        draftLiquidGlassSubjectShadowAlphaText = mainViewModel.params.value.liquidGlassSubjectShadowAlpha.toString()
        mainViewModel.updateLive { p -> p.copy(liquidGlassSubjectOpacityPercent = prefs.getInt(
            PREF_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
            DEFAULT_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
        ).coerceIn(MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT, MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT)) }
        draftLiquidGlassSubjectOpacityText = mainViewModel.params.value.liquidGlassSubjectOpacityPercent.toString()
        liquidGlassBottomBarEnabled = prefs.getBoolean(PREF_LIQUID_GLASS_BOTTOM_BAR_ENABLED, true)
        liquidGlassBottomBarBlurEnabled = prefs.getBoolean(PREF_LIQUID_GLASS_BOTTOM_BAR_BLUR_ENABLED, true)
        if (!migratedToLayered) {
            prefs.edit()
                .putLiquidGlassSettings()
                .apply()
        }
    }

    internal fun saveLiquidGlassSettings() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putLiquidGlassSettings()
            .apply()
    }

    internal fun SharedPreferences.Editor.putLiquidGlassSettings(): SharedPreferences.Editor =
        putBoolean(PREF_LIQUID_GLASS_LAYERED_MIGRATED, true)
            .putBoolean(PREF_LIQUID_GLASS_ENABLED, mainViewModel.params.value.liquidGlassEnabled)
            .putBoolean(PREF_LIQUID_GLASS_BOTTOM_BAR_ENABLED, liquidGlassBottomBarEnabled)
            .putBoolean(PREF_LIQUID_GLASS_BOTTOM_BAR_BLUR_ENABLED, liquidGlassBottomBarBlurEnabled)
            .putInt(PREF_LIQUID_GLASS_RADIUS, mainViewModel.params.value.liquidGlassRadius)
            .putInt(PREF_LIQUID_GLASS_OUTER_WIDTH, mainViewModel.params.value.liquidGlassOuterWidth)
            .putInt(PREF_LIQUID_GLASS_TOP_ALPHA, mainViewModel.params.value.liquidGlassTopAlpha)
            .putInt(PREF_LIQUID_GLASS_BOTTOM_ALPHA, mainViewModel.params.value.liquidGlassBottomAlpha)
            .putInt(PREF_LIQUID_GLASS_BACKGROUND_MIST_ALPHA, mainViewModel.params.value.liquidGlassBackgroundMistAlpha)
            .putInt(PREF_LIQUID_GLASS_BOTTOM_DARK_ALPHA, mainViewModel.params.value.liquidGlassBottomDarkAlpha)
            .putInt(PREF_LIQUID_GLASS_SUBJECT_SCALE_PERCENT, mainViewModel.params.value.liquidGlassSubjectScalePercent)
            .putInt(PREF_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH, mainViewModel.params.value.liquidGlassSubjectOutlineWidth)
            .putInt(PREF_LIQUID_GLASS_SUBJECT_INNER_OUTLINE_WIDTH, mainViewModel.params.value.liquidGlassSubjectInnerOutlineWidth)
            .putInt(PREF_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA, mainViewModel.params.value.liquidGlassSubjectShadowAlpha)
            .putInt(PREF_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT, mainViewModel.params.value.liquidGlassSubjectOpacityPercent)

    internal fun updateLiquidGlassEnabled(enabled: Boolean) {
        if (mainViewModel.params.value.liquidGlassEnabled == enabled) {
            return
        }
        mainViewModel.updateLive { p -> p.copy(liquidGlassEnabled = enabled) }
        saveLiquidGlassSettings()
        statusText = if (enabled) "液态玻璃风格已开启" else "液态玻璃风格已关闭"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updateLiquidGlassRadius(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_RADIUS, MAX_LIQUID_GLASS_RADIUS)
        mainViewModel.updateLive { p -> p.copy(liquidGlassRadius = next) }
        draftLiquidGlassRadiusText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃圆角 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updateLiquidGlassOuterWidth(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_OUTER_WIDTH, MAX_LIQUID_GLASS_OUTER_WIDTH)
        mainViewModel.updateLive { p -> p.copy(liquidGlassOuterWidth = next) }
        draftLiquidGlassOuterWidthText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃外框高度 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updateLiquidGlassTopAlpha(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)
        mainViewModel.updateLive { p -> p.copy(liquidGlassTopAlpha = next) }
        draftLiquidGlassTopAlphaText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃顶部强度 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updateLiquidGlassBottomAlpha(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)
        mainViewModel.updateLive { p -> p.copy(liquidGlassBottomAlpha = next) }
        draftLiquidGlassBottomAlphaText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃底边强度 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updateLiquidGlassBackgroundMistAlpha(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_MIST_ALPHA, MAX_LIQUID_GLASS_MIST_ALPHA)
        mainViewModel.updateLive { p -> p.copy(liquidGlassBackgroundMistAlpha = next) }
        draftLiquidGlassBackgroundMistAlphaText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃背景灰雾 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updateLiquidGlassBottomDarkAlpha(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_BOTTOM_DARK_ALPHA, MAX_LIQUID_GLASS_BOTTOM_DARK_ALPHA)
        mainViewModel.updateLive { p -> p.copy(liquidGlassBottomDarkAlpha = next) }
        draftLiquidGlassBottomDarkAlphaText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃底部灰雾 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updateLiquidGlassSubjectScalePercent(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT, MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT)
        mainViewModel.updateLive { p -> p.copy(liquidGlassSubjectScalePercent = next) }
        draftLiquidGlassSubjectScaleText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃主体比例 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updateLiquidGlassSubjectOutlineWidth(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH, MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH)
        mainViewModel.updateLive { p -> p.copy(liquidGlassSubjectOutlineWidth = next) }
        draftLiquidGlassSubjectOutlineWidthText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃主体外框 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updateLiquidGlassSubjectInnerOutlineWidth(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH, MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH)
        mainViewModel.updateLive { p -> p.copy(liquidGlassSubjectInnerOutlineWidth = next) }
        draftLiquidGlassSubjectInnerOutlineWidthText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃主体内框 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updateLiquidGlassSubjectShadowAlpha(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA, MAX_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA)
        mainViewModel.updateLive { p -> p.copy(liquidGlassSubjectShadowAlpha = next) }
        draftLiquidGlassSubjectShadowAlphaText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃主体阴影 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun updateLiquidGlassSubjectOpacityPercent(value: Int) {
        val next = value.coerceIn(MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT, MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT)
        mainViewModel.updateLive { p -> p.copy(liquidGlassSubjectOpacityPercent = next) }
        draftLiquidGlassSubjectOpacityText = next.toString()
        saveLiquidGlassSettings()
        statusText = "液态玻璃主体透明度 $next"
        refreshActivePreviewOutputs(rebuildLocalCandidates = false)
    }

    internal fun generateSelected(
        installWithRoot: Boolean,
        useGpt: Boolean,
        rootWriteMode: RootWriteMode = RootWriteMode.All,
        confirmed: Boolean = false,
    ) {
        val entry = apps.firstOrNull { it.packageName == selectedPackageName }
        if (entry == null) {
            statusText = "先选择一个应用"
            return
        }
        if (useGpt && gptApiKey.trim().isEmpty()) {
            statusText = "请填写AI提供商信息"
            return
        }
        if (useGpt && gptBaseUrl.trim().isEmpty()) {
            statusText = "请填写AI提供商信息"
            return
        }
        if (isBusy) {
            statusText = "当前有任务在运行"
            return
        }
        if (useGpt && !confirmed) {
            requestServiceConfirm(
                title = "使用 AI 生成",
                message = "将调用云端图像接口（已累计 $gptRunCount 次）生成图标包。确认继续？",
                confirmLabel = "继续",
            ) {
                generateSelected(installWithRoot, true, rootWriteMode, confirmed = true)
            }
            return
        }

        isBusy = true
        if (useGpt) {
            isGptPreviewLoading = true
            incrementGptRunCount()
        }
        statusText = if (useGpt) {
            "AI处理中: ${entry.packageName}"
        } else {
            "本地处理中(自动): ${entry.packageName}"
        }
        startUiFriendlyThread(if (useGpt) "ArtPlusGptGenerate" else "ArtPlusLocalGenerate") {
            try {
                val result = generateArtPlusPackage(entry, useGpt)
                runOnUiThread {
                    activeGenerationSession = result.session
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (result.selections).normalLight.name, previewNormalDark = (result.selections).normalDark.name, previewMonochromeLight = (result.selections).monochromeLight.name, previewMonochromeDark = (result.selections).monochromeDark.name) }
                    previewChoiceMode = null
                    previewPackageName = entry.packageName
                    previewDirPath = result.outDir.absolutePath
                    previewVersion += 1
                    saveUiState()
                }
                if (false && outputTreeUri != null) {
                    exportToTree(contentResolver, outputTreeUri, result.outDir)
                }
                if (installWithRoot) {
                    installWithRoot(result.outDir, entry.packageName, rootWriteMode)
                    runOnUiThread {
                        generatedPackageNames = markPackageGenerated(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), generatedPackageNames, entry.packageName)
                    }
                    if (useGpt) {
                        toastStatus("已生成AI版并${rootWriteMode.label}写入，未刷新，请手动点首页左上角刷新图标: ${entry.packageName}")
                    } else {
                        statusText = "已生成本地版并${rootWriteMode.label}写入，未刷新，请手动点首页左上角刷新图标: ${entry.packageName}"
                    }
                } else {
                    if (useGpt) {
                        toastStatus("已生成AI版: ${result.outDir.absolutePath}")
                    } else {
                        statusText = "已生成本地版: ${result.outDir.absolutePath}"
                    }
                }
            } catch (error: Exception) {
                val msg = "失败: ${error.message ?: error.javaClass.simpleName}"
                if (useGpt) {
                    toastStatus(msg)
                } else {
                    statusText = msg
                }
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

    internal fun writeSelectedWithRoot(rootWriteMode: RootWriteMode) {
        val entry = apps.firstOrNull { it.packageName == selectedPackageName }
        if (entry == null) {
            statusText = "先选择一个应用"
            return
        }
        if (isBusy) {
            return
        }

        fun executeWrite() {
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
            val selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark)
            startUiFriendlyThread("ArtPlusPreviewRootWrite") {
                try {
                    writePackageOutputs(session, selections)
                    if (false && outputTreeUri != null) {
                        exportToTree(contentResolver, outputTreeUri, session.outDir)
                    }
                    installWithRoot(session.outDir, entry.packageName, rootWriteMode)
                    runOnUiThread {
                        generatedPackageNames = markPackageGenerated(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), generatedPackageNames, entry.packageName)
                        activeGenerationSession = session
                        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                        previewPackageName = entry.packageName
                        previewDirPath = session.outDir.absolutePath
                        previewVersion += 1
                        saveUiState()
                    }
                    toastStatus("已按当前预览${rootWriteMode.label}写入，未刷新，请手动点首页左上角刷新图标: ${entry.packageName}")
                } catch (error: Exception) {
                    toastStatus("写入失败: ${error.message ?: error.javaClass.simpleName}")
                } finally {
                    runOnUiThread {
                        isBusy = false
                    }
                }
            }
        }

        if (autoConfirmRootWrite) {
            executeWrite()
        } else {
            rootWriteConfirmRememberSkip = false
            val targetPath = "$ROOT_UXICONS_DIR/${entry.packageName}"
            pendingRootWriteConfirm = RootWriteConfirmRequest(
                packageName = entry.packageName,
                targetPath = targetPath,
                rootWriteMode = rootWriteMode,
                onConfirm = { executeWrite() },
            )
        }
    }

    internal fun selectAppAndRestoreGeneratedPreview(entry: AppEntry) {
        val packageName = entry.packageName
        val revision = ++generatedPreviewRestoreRevision
        selectedPackageName = packageName
        activeGenerationSession = null
        previewChoiceMode = null
        previewPackageName = null
        previewDirPath = null
        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (PreviewSelections.default(PreviewChoice.Original)).normalLight.name, previewNormalDark = (PreviewSelections.default(PreviewChoice.Original)).normalDark.name, previewMonochromeLight = (PreviewSelections.default(PreviewChoice.Original)).monochromeLight.name, previewMonochromeDark = (PreviewSelections.default(PreviewChoice.Original)).monochromeDark.name) }
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
                        generatedPackageNames = markPackageGenerated(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), generatedPackageNames, packageName)
                        activeGenerationSession = buildGeneratedPackageSession(packageName, packageDir)
                        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (PreviewSelections.default(PreviewChoice.Original)).normalLight.name, previewNormalDark = (PreviewSelections.default(PreviewChoice.Original)).normalDark.name, previewMonochromeLight = (PreviewSelections.default(PreviewChoice.Original)).monochromeLight.name, previewMonochromeDark = (PreviewSelections.default(PreviewChoice.Original)).monochromeDark.name) }
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

    internal fun addLiquidGlassToSelectedGenerated() {
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
                    generatedPackageNames = markPackageGenerated(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), generatedPackageNames, entry.packageName)
                    activeGenerationSession = buildGeneratedPackageSession(entry.packageName, packageDir)
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (PreviewSelections.default(PreviewChoice.Original)).normalLight.name, previewNormalDark = (PreviewSelections.default(PreviewChoice.Original)).normalDark.name, previewMonochromeLight = (PreviewSelections.default(PreviewChoice.Original)).monochromeLight.name, previewMonochromeDark = (PreviewSelections.default(PreviewChoice.Original)).monochromeDark.name) }
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

    internal fun toggleMultiSelectedPackage(packageName: String) {
        multiSelectedPackageNames = if (packageName in multiSelectedPackageNames) {
            multiSelectedPackageNames - packageName
        } else {
            multiSelectedPackageNames + packageName
        }
    }

    internal fun addLiquidGlassToMultiSelectedGenerated() {
        val packageNames = multiSelectedPackageNames.toList().sorted()
        if (packageNames.isEmpty()) {
            statusText = "先选择要添加光影的应用"
            return
        }
        if (isBusy) {
            return
        }

        requestServiceConfirm(
            title = "确认添加光影",
            message = "将为 ${packageNames.size} 个已选项添加光影并写入 data 分区，耗时较长且会覆盖现有图标，确认继续？",
            confirmLabel = "确认添加",
        ) {
            executeAddLiquidGlassToMultiSelectedGenerated(packageNames)
        }
        return
    }

    internal fun executeAddLiquidGlassToMultiSelectedGenerated(packageNames: List<String>) {
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
                    generatedPackageNames = updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), generatedPackageNames + successes)
                    multiSelectedPackageNames = multiSelectedPackageNames - successes.toSet()
                }
                if (
                    selectedAtStart != null &&
                    selectedPackageName == selectedAtStart &&
                    selectedSession != null &&
                    selectedDirPath != null
                ) {
                    activeGenerationSession = selectedSession
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (PreviewSelections.default(PreviewChoice.Original)).normalLight.name, previewNormalDark = (PreviewSelections.default(PreviewChoice.Original)).normalDark.name, previewMonochromeLight = (PreviewSelections.default(PreviewChoice.Original)).monochromeLight.name, previewMonochromeDark = (PreviewSelections.default(PreviewChoice.Original)).monochromeDark.name) }
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

    internal fun existingGeneratedPackageDir(packageName: String): File {
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

    internal fun buildGeneratedPackageSession(packageName: String, packageDir: File): GenerationSession {
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

    internal fun artPlusPackageDir(packageName: String): File {
        val base = getExternalFilesDir("ArtPlus") ?: File(filesDir, "ArtPlus")
        return File(base, packageName)
    }

    internal fun rootGeneratedPreviewDir(packageName: String): File =
        File(File(filesDir, "RootGeneratedPreview"), packageName)

    internal fun hasGeneratedPackageBaseAssets(dir: File): Boolean =
        dir.isDirectory &&
            File(dir, "recbg.png").isFile &&
            File(dir, "recfg.png").isFile

    internal fun copyRootGeneratedPackageToLocal(packageName: String): File {
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

    internal fun applyLiquidGlassToGeneratedPackage(dir: File) {
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

        savePng(normalDarkForeground(outputRecfg, recbg, mainViewModel.params.value.nightSubjectLightBackgroundEnabled), File(dir, "rec_night.png"))
        savePng(normalDarkForeground(recfg1x2, recbg1x2, mainViewModel.params.value.nightSubjectLightBackgroundEnabled), File(dir, "rec_night_1x2.png"))
        savePng(normalDarkForeground(recfg2x1, recbg2x1, mainViewModel.params.value.nightSubjectLightBackgroundEnabled), File(dir, "rec_night_2x1.png"))
        savePng(normalDarkForeground(recfg2x2, recbg2x2, mainViewModel.params.value.nightSubjectLightBackgroundEnabled), File(dir, "rec_night_2x2.png"))
    }

    internal fun writeDefaultSubjectMonochromeFiles(
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

    internal fun glassBackgroundForGeneratedPackage(
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

    internal fun decodeGeneratedBitmap(dir: File, name: String): Bitmap? =
        BitmapFactory.decodeFile(File(dir, name).absolutePath)

    internal fun installLiquidGlassFilesWithRoot(packageDir: File, packageName: String) {
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

    internal fun generateArtPlusPackage(
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
        val localPipeline = currentLocalPipelineConfig()
        val localSource = buildLocalIconLayers(icon, localPipeline, mainViewModel.params.value.backgroundSeparationPercent, AdaptiveForegroundMode.fromValue(mainViewModel.params.value.adaptiveForegroundMode), mainViewModel.params.value.adaptiveDirectMaxCoveragePercent, mainViewModel.params.value.adaptiveDirectMaxCoverageIncreasePercent, mainViewModel.params.value.adaptiveMaskEdgeCoveragePercent, mainViewModel.params.value.adaptiveMaskMinCoveragePercent, mainViewModel.params.value.adaptiveCenterEpsilonPercent)
        val localCandidateSet = buildLocalCandidates(localSource, localSourceIcon, localPipeline, OriginalForegroundCleanupMode.fromValue(mainViewModel.params.value.originalForegroundCleanupMode), mainViewModel.params.value.plateRemovalPercent, mainViewModel.params.value.shadowRemovalPercent, mainViewModel.params.value.backgroundSeparationPercent)
        val localCandidates = localCandidateSet.candidates
        val candidates = if (useGpt) {
            // P4 交界：GPT 图层收敛进 pipeline/，显式传调参 + 凭证 + 状态回调。
            val gptLayers = generateGptLayers(gptSourceIcon, localSource.recfg, localSource.recbg, mainViewModel.params.value.gptCustomPrompt, GptPromptPreset.fromValue(mainViewModel.params.value.gptPromptPreset), mainViewModel.params.value.foregroundSubjectPercent, GptImageMode.fromValue(mainViewModel.params.value.gptImageMode), gptModelId, gptBaseUrl, gptApiKey, isDebugBuild(), ::status)
            localCandidates + (PreviewChoice.Gpt to IconCandidate(gptLayers.recfg, gptLayers.recbg, monochromeRaw = null, isLocal = false))
        } else {
            localCandidates
        }
        val selectedLocalMode = localModeOverride ?: LocalSeparationMode.fromValue(mainViewModel.params.value.localSeparationMode)
        val requestedChoice = if (useGpt) {
            PreviewChoice.Gpt
        } else {
            defaultPreviewChoiceForMode(selectedLocalMode, localCandidateSet.autoChoice)
        }
        val defaultChoice = requestedChoice.takeIf { candidates.containsKey(it) }
            ?: localCandidateSet.autoChoice.takeIf { candidates.containsKey(it) }
            ?: PreviewChoice.Original
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
        status("本地分离: ${selectedLocalMode.label}/${defaultChoice.label} · 背景 $mainViewModel.params.value.backgroundSeparationPercent · 底板 $mainViewModel.params.value.plateRemovalPercent · 阴影 $mainViewModel.params.value.shadowRemovalPercent · 毛刺 $mainViewModel.params.value.edgePolishPercent")
        return GenerationResult(outDir = outDir, session = session, selections = selections)
    }


    internal fun buildRmbgCandidate(sourceIcon: Bitmap): CandidateBuildResult? {
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
                    isLocal = false,
                ),
                autoUsable = manualUsable && coverage in RMBG_MIN_AUTO_COVERAGE..RMBG_MAX_AUTO_COVERAGE,
                coverage = coverage,
                rmbgInference = mask.report,
                manualUsable = manualUsable,
                validationWarning = validationWarning,
            )
        }.getOrElse { throw it }
    }

    internal fun rebuildRmbgBackground(sourceIcon: Bitmap, foreground: Bitmap): Bitmap {
        val fallback = solidBitmap(
            sourceIcon.width,
            sourceIcon.height,
            estimatePlainIconBackground(sourceIcon),
        )
        return rebuildComposedIconBackground(sourceIcon, foreground, fallback)
    }

    internal fun rmbgValidationWarning(coverage: Double, bounds: Bounds?, cropRisk: Boolean): String {
        val coverageText = (coverage * 100.0).roundToInt()
        val boundsText = bounds?.let { "${it.width()}x${it.height()}@${it.left},${it.top}" } ?: "无"
        return "RMBG候选未通过校验，已保留: 覆盖率 ${coverageText}%，边界 $boundsText，贴边风险 ${if (cropRisk) "是" else "否"}"
    }

    internal fun buildRmbgDebugCandidate(sourceIcon: Bitmap): RmbgDebugCandidate {
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
            isLocal = false,
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

    internal fun rmbgComponentDir(): File = File(filesDir, RMBG_COMPONENT_DIR)

    internal fun findRmbgComponent(): RmbgComponent? {
        val dir = rmbgComponentDir()
        val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: return null
        val model = File(dir, RMBG_MODEL_NAME)
        if (!model.isFile || model.length() < RMBG_MIN_MODEL_BYTES) {
            return null
        }
        return RmbgComponent(dir, abi, model)
    }

    internal fun clearInstalledRmbgComponent() {
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

    internal fun installRmbgComponent(uri: Uri) {
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

    internal fun installRmbgComponentFromUrl() {
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

    internal fun installRmbgComponentFromModelUrl(modelUrl: String, modelFile: File): RmbgComponent {
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

    internal fun installRmbgComponentFromInput(input: InputStream): RmbgComponent {
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

    internal fun downloadRmbgFile(urlText: String, target: File, minBytes: Long, label: String) {
        val url = validatedRemoteUrl(urlText, label, isDebugBuild())
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

    internal fun unzipRmbgComponent(input: InputStream, targetDir: File) {
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

    internal fun normalizeRmbgModelFile(dir: File) {
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

    internal fun validateRmbgComponentDir(dir: File) {
        val model = File(dir, RMBG_MODEL_NAME)
        if (!model.isFile || model.length() < RMBG_MIN_MODEL_BYTES) {
            error("缺少 $RMBG_MODEL_NAME")
        }
    }

    internal fun copyDirectory(source: File, target: File) {
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




    internal inner class DynamicRmbgRuntime(
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

    internal fun runRmbgModel(component: RmbgComponent, input: FloatBuffer, shape: LongArray): RmbgModelOutput =
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

    internal fun runRmbgAlphaMask(sourceIcon: Bitmap, component: RmbgComponent): RmbgMaskResult {
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

    internal fun tuneRmbgAlpha(alpha: IntArray, width: Int, height: Int): IntArray {
        if (alpha.size != width * height || width <= 0 || height <= 0) {
            return alpha.copyOf()
        }
        var current = alpha.copyOf()
        val strength = mainViewModel.params.value.rmbgAlphaStrengthPercent.coerceIn(
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

        val adjust = mainViewModel.params.value.rmbgEdgeAdjustPercent.coerceIn(
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

        val feather = ratioPercent(mainViewModel.params.value.rmbgEdgeFeatherPercent.coerceIn(
            MIN_RMBG_EDGE_FEATHER_PERCENT,
            MAX_RMBG_EDGE_FEATHER_PERCENT,
        ))
        if (feather > 0.0) {
            val radius = if (mainViewModel.params.value.rmbgEdgeFeatherPercent >= 70) 2 else 1
            current = featherRmbgAlphaEdges(current, width, height, strength = feather, radius = radius)
        }

        val weakKeep = ratioPercent(mainViewModel.params.value.rmbgWeakAlphaKeepPercent.coerceIn(
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

    internal fun applyAlphaArrayToSource(source: Bitmap, alpha: IntArray): Bitmap {
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

    internal fun applyMaskToSource(pixels: IntArray, width: Int, height: Int, mask: BooleanArray): Bitmap {
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

    internal fun writePackageOutputs(session: GenerationSession, selections: PreviewSelections) {
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
        savePng(normalDarkForeground(nightRecfg, nightRecbg, mainViewModel.params.value.nightSubjectLightBackgroundEnabled), File(session.outDir, "rec_night.png"))
        savePng(
            normalDarkForeground(nightRecfg1x2, nightRecbg1x2, mainViewModel.params.value.nightSubjectLightBackgroundEnabled),
            File(session.outDir, "rec_night_1x2.png"),
        )
        savePng(
            normalDarkForeground(nightRecfg2x1, nightRecbg2x1, mainViewModel.params.value.nightSubjectLightBackgroundEnabled),
            File(session.outDir, "rec_night_2x1.png"),
        )
        savePng(
            normalDarkForeground(nightRecfg2x2, nightRecbg2x2, mainViewModel.params.value.nightSubjectLightBackgroundEnabled),
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

    internal fun liquidGlassBackgroundForSize(
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
        return if (forceLiquidGlass || mainViewModel.params.value.liquidGlassEnabled) {
            renderLayeredLiquidGlassBackground(resized)
        } else {
            resized
        }
    }

    internal fun foregroundForSize(
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
        return if (forceLiquidGlass || mainViewModel.params.value.liquidGlassEnabled) {
            renderLayeredLiquidGlassForeground(sized)
        } else {
            applyForegroundShadow(sized)
        }
    }

    internal fun renderLayeredLiquidGlassBackground(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val radius = liquidGlassRadiusForSize(width, height)
        val shapeMask = roundedRectMaskAlpha(width, height, radius, feather = liquidGlassMaskFeather(width, height))
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(AndroidColor.TRANSPARENT)
        canvas.drawBitmap(source, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))

        val mistAlpha = mainViewModel.params.value.liquidGlassBackgroundMistAlpha.coerceIn(MIN_LIQUID_GLASS_MIST_ALPHA, MAX_LIQUID_GLASS_MIST_ALPHA)
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

    internal fun renderLayeredLiquidGlassForeground(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val radius = liquidGlassRadiusForSize(width, height)
        val shapeMask = roundedRectMaskAlpha(width, height, radius, feather = liquidGlassMaskFeather(width, height))
        val subject = scaleBitmapAroundCanvasCenter(
            source,
            mainViewModel.params.value.liquidGlassSubjectScalePercent
                .coerceIn(MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT, MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT)
                .toFloat() / 100f,
        )
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(AndroidColor.TRANSPARENT)

        val subjectShadowAlpha = mainViewModel.params.value.liquidGlassSubjectShadowAlpha
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
            mainViewModel.params.value.liquidGlassSubjectOutlineWidth.coerceIn(
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
            mainViewModel.params.value.liquidGlassSubjectInnerOutlineWidth.coerceIn(
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

        val subjectOpacity = mainViewModel.params.value.liquidGlassSubjectOpacityPercent
            .coerceIn(MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT, MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT)
        if (subjectOpacity > 0) {
            canvas.drawBitmap(
                applyLiquidGlassSubjectOpacity(subject, subjectOpacity),
                0f,
                0f,
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
            )
        }
        return applyAlphaMask(out, shapeMask)
    }

    internal fun applyLiquidGlassSubjectOpacity(source: Bitmap, opacityPercent: Int): Bitmap {
        val targetAlpha = (opacityPercent.coerceIn(
            MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
            MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
        ) * 255f / 100f).roundToInt().coerceIn(0, 255)
        if (targetAlpha <= 0) {
            return solidBitmap(source.width, source.height, AndroidColor.TRANSPARENT)
        }

        val pixels = IntArray(source.width * source.height)
        source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
        if (targetAlpha >= 255) {
            val outPixels = IntArray(pixels.size)
            for (i in pixels.indices) {
                val pixel = pixels[i]
                val alpha = AndroidColor.alpha(pixel)
                outPixels[i] = if (alpha <= 0) {
                    AndroidColor.TRANSPARENT
                } else {
                    val red = unpremultiplyChannel(AndroidColor.red(pixel), alpha)
                    val green = unpremultiplyChannel(AndroidColor.green(pixel), alpha)
                    val blue = unpremultiplyChannel(AndroidColor.blue(pixel), alpha)
                    AndroidColor.argb(255, red, green, blue)
                }
            }
            return Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888).apply {
                setPixels(outPixels, 0, source.width, 0, 0, source.width, source.height)
            }
        }
        val alphaScaleBase = liquidGlassSubjectAlphaScaleBase(pixels)
        if (alphaScaleBase <= 0) {
            return source
        }
        val solidAlphaCutoff = (alphaScaleBase * LIQUID_GLASS_SUBJECT_SOLID_ALPHA_RATIO)
            .roundToInt()
            .coerceIn(LOCAL_ALPHA_VISIBLE_THRESHOLD + 1, 255)

        val outPixels = IntArray(pixels.size)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = AndroidColor.alpha(pixel)
            outPixels[i] = if (alpha <= 0) {
                AndroidColor.TRANSPARENT
            } else {
                val red = unpremultiplyChannel(AndroidColor.red(pixel), alpha)
                val green = unpremultiplyChannel(AndroidColor.green(pixel), alpha)
                val blue = unpremultiplyChannel(AndroidColor.blue(pixel), alpha)
                val normalizedAlpha = liquidGlassSubjectNormalizedAlpha(alpha, solidAlphaCutoff)
                val outAlpha = (normalizedAlpha * targetAlpha).roundToInt().coerceIn(0, 255)
                if (outAlpha <= 0) {
                    AndroidColor.TRANSPARENT
                } else {
                    AndroidColor.argb(outAlpha, red, green, blue)
                }
            }
        }
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, source.width, 0, 0, source.width, source.height)
        return out
    }

    internal fun unpremultiplyChannel(channel: Int, alpha: Int): Int {
        if (alpha <= 0) {
            return 0
        }
        if (alpha >= 255) {
            return channel.coerceIn(0, 255)
        }
        return ((channel * 255f) / alpha.toFloat()).roundToInt().coerceIn(0, 255)
    }

    internal fun liquidGlassSubjectNormalizedAlpha(alpha: Int, solidAlphaCutoff: Int): Float {
        if (alpha >= solidAlphaCutoff) {
            return 1f
        }
        val range = (solidAlphaCutoff - LOCAL_ALPHA_VISIBLE_THRESHOLD)
            .coerceAtLeast(1)
            .toFloat()
        val t = ((alpha - LOCAL_ALPHA_VISIBLE_THRESHOLD) / range).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    internal fun liquidGlassSubjectAlphaScaleBase(pixels: IntArray): Int {
        val visibleAlpha = pixels
            .asSequence()
            .map { AndroidColor.alpha(it) }
            .filter { it > LOCAL_ALPHA_VISIBLE_THRESHOLD }
            .toMutableList()
        if (visibleAlpha.isEmpty()) {
            return 0
        }
        val highAlpha = percentile(visibleAlpha, LIQUID_GLASS_SUBJECT_ALPHA_NORMALIZE_PERCENTILE)
        val bodyAlpha = percentile(visibleAlpha, LIQUID_GLASS_SUBJECT_ALPHA_BODY_PERCENTILE)
        return minOf(
            highAlpha,
            (bodyAlpha * LIQUID_GLASS_SUBJECT_ALPHA_OUTLIER_CAP).roundToInt(),
        ).coerceIn(1, 255)
    }

    internal fun drawLayeredLiquidGlassLight(canvas: Canvas, width: Int, height: Int, radius: Float) {
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val bottom = height.toFloat()
        val topAlpha = mainViewModel.params.value.liquidGlassTopAlpha.coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)
        val bottomAlpha = mainViewModel.params.value.liquidGlassBottomAlpha.coerceIn(MIN_LIQUID_GLASS_ALPHA, MAX_LIQUID_GLASS_ALPHA)

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

        val bottomDarkAlpha = mainViewModel.params.value.liquidGlassBottomDarkAlpha
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

        val outerWidth = mainViewModel.params.value.liquidGlassOuterWidth
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

    internal fun liquidGlassRadiusForSize(width: Int, height: Int): Float {
        val minSide = minOf(width, height).toFloat().coerceAtLeast(1f)
        return (mainViewModel.params.value.liquidGlassRadius.coerceIn(MIN_LIQUID_GLASS_RADIUS, MAX_LIQUID_GLASS_RADIUS) * liquidGlassScaleForSize(width, height))
            .coerceIn(0f, minSide / 2f)
    }

    internal fun liquidGlassScaleForSize(width: Int, height: Int): Float =
        minOf(width, height).toFloat().coerceAtLeast(1f) / SIZE_1X1.toFloat()

    internal fun liquidGlassMaskFeather(width: Int, height: Int): Float =
        maxOf(1f, liquidGlassScaleForSize(width, height))

    internal fun liquidGlassScaledWidth(width: Int, height: Int, value: Int): Int =
        (value * liquidGlassScaleForSize(width, height)).roundToInt().coerceAtLeast(0)

    internal fun scaleBitmapAroundCanvasCenter(source: Bitmap, scale: Float): Bitmap {
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

    internal fun subjectOutlineLayer(source: Bitmap, width: Int, inner: Boolean, alphaScale: Float): Bitmap {
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

    internal fun maxFilterAlpha(alpha: IntArray, width: Int, height: Int, radius: Int): IntArray {
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

    internal fun minFilterAlpha(alpha: IntArray, width: Int, height: Int, radius: Int): IntArray {
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

    internal fun whiteWithAlpha(alpha: Float): Int =
        AndroidColor.argb(alpha.roundToInt().coerceIn(0, 255), 255, 255, 255)

    internal fun blackWithAlpha(alpha: Float): Int =
        AndroidColor.argb(alpha.roundToInt().coerceIn(0, 255), 0, 0, 0)

    internal fun applyAlphaMask(source: Bitmap, mask: IntArray): Bitmap {
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

    internal fun roundedRectMaskAlpha(width: Int, height: Int, radius: Float, feather: Float): IntArray {
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

    internal fun sdRoundedRectCentered(
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

    internal fun bitmapAlphaArray(source: Bitmap): IntArray {
        val pixels = IntArray(source.width * source.height)
        source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
        for (i in pixels.indices) {
            pixels[i] = AndroidColor.alpha(pixels[i])
        }
        return pixels
    }

    internal fun alphaArrayToColorLayer(
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

    internal fun vectorLength(x: Float, y: Float): Float =
        sqrt((x * x + y * y).toDouble()).toFloat()

    internal fun candidateOrFallback(
        session: GenerationSession,
        choice: PreviewChoice,
    ): IconCandidate =
        candidateForChoice(session, choice)
            ?: session.candidates[PreviewChoice.Full]
            ?: session.candidates[PreviewChoice.Plate]
            ?: session.candidates.getValue(PreviewChoice.Original)

    internal fun normalizePreviewSelections(
        session: GenerationSession,
        selections: PreviewSelections,
    ): PreviewSelections {
        val defaultChoice = listOf(
            session.autoLocalChoice,
            PreviewChoice.Full,
            PreviewChoice.Original,
            PreviewChoice.Gpt,
            PreviewChoice.Rmbg,
        ).firstOrNull { candidateForChoice(session, it) != null } ?: PreviewChoice.Original

        fun normalize(choice: PreviewChoice): PreviewChoice {
            if (candidateForChoice(session, choice) != null) {
                return choice
            }
            val directFallback = when (choice) {
                PreviewChoice.GptComposedBackground -> PreviewChoice.Gpt
                PreviewChoice.RmbgComposedBackground -> PreviewChoice.Rmbg
                else -> null
            }
            return directFallback?.takeIf { candidateForChoice(session, it) != null } ?: defaultChoice
        }

        return PreviewSelections(
            normalLight = normalize(selections.normalLight),
            normalDark = normalize(selections.normalDark),
            monochromeLight = normalize(selections.monochromeLight),
            monochromeDark = normalize(selections.monochromeDark),
        )
    }

    internal fun candidateForChoice(session: GenerationSession, choice: PreviewChoice): IconCandidate? =
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

    internal fun candidateWithComposedBackground(
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

    internal fun effectiveChoiceForPreviewRow(
        mode: PreviewMode,
        rowChoice: PreviewChoice,
        session: GenerationSession,
    ): PreviewChoice {
        if (rowChoice != PreviewChoice.ComposedBackground) {
            return rowChoice
        }
        val currentChoice = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark).choiceFor(mode)
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

    internal fun candidateWithCustomOverrides(
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
            isLocal = customForeground == null && base.isLocal,
            applyLocalEdgePolish = customForeground == null && base.applyLocalEdgePolish,
        )
    }

    internal fun monochromeForCandidate(candidate: IconCandidate, invertLuma: Boolean = false): Bitmap {
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

    internal fun scaleMonochromeForTheme(source: Bitmap): Bitmap =
        scaleBitmapAroundAlphaCenter(source, mainViewModel.params.value.monochromeThemeScale)



    internal fun previewAssetsForSelections(
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
            normalDarkForeground(nightRecfg, nightRecbg, mainViewModel.params.value.nightSubjectLightBackgroundEnabled)
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

    internal fun previewAssetsForCandidate(candidate: IconCandidate, mode: PreviewMode? = null): PreviewAssets {
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
            recNight = normalDarkForeground(recfg, recbg, mainViewModel.params.value.nightSubjectLightBackgroundEnabled),
            monochromeLight = monochromeForCandidate(candidate, invertLuma = true),
            monochromeDark = monochromeForCandidate(candidate, invertLuma = false),
        )
    }

    internal fun renderCandidateForegroundBase(candidate: IconCandidate): Bitmap =
        renderCandidateBitmap(rmbgTunedForegroundRaw(candidate) ?: candidate.recfgRaw).let { bitmap ->
            if (candidate.isLocal && !candidate.applyLocalEdgePolish) bitmap else polishForegroundEdges(bitmap, mainViewModel.params.value.edgePolishPercent)
        }

    internal fun renderCandidateForeground(candidate: IconCandidate): Bitmap =
        foregroundForSize(renderCandidateForegroundBase(candidate), SIZE_1X1, SIZE_1X1)

    internal fun applyForegroundShadow(source: Bitmap): Bitmap {
        val level = mainViewModel.params.value.foregroundShadowLevel.coerceIn(MIN_FOREGROUND_SHADOW_LEVEL, MAX_FOREGROUND_SHADOW_LEVEL)
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

    internal fun foregroundShadowParams(level: Int, baseSize: Int): ForegroundShadowParams {
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

    internal fun subjectShadowBitmap(source: Bitmap, params: ForegroundShadowParams): Bitmap {
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

    internal fun growAlphaMask(source: Bitmap, radius: Int): Bitmap {
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

    internal fun blurAlphaMask(source: Bitmap, radius: Float): Bitmap {
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

    internal fun rmbgTunedForegroundRaw(candidate: IconCandidate): Bitmap? {
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

    internal fun renderCandidateBitmap(bitmap: Bitmap): Bitmap =
        normalizeForegroundSubjectSize(bitmap, mainViewModel.params.value.foregroundSubjectPercent)

    internal fun applyPreviewChoice(mode: PreviewMode, choice: PreviewChoice) {
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
            statusText = "先生成 AI 候选，再使用拼合背景"
            return
        }
        if (choice == PreviewChoice.RmbgComposedBackground && session.candidates[PreviewChoice.Rmbg] == null) {
            statusText = "先生成 RMBG 候选，再使用拼合背景"
            return
        }
        val selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark).withChoice(mode, choice)
        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
        saveUiState()
        writeActivePreviewOutputs(session, selections, closeDialog = false)
    }

    internal fun applyPreviewChoiceToAll(choice: PreviewChoice) {
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
            statusText = "先生成 AI 候选，再使用拼合背景"
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
        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
        previewChoiceMode = null
        saveUiState()
        writeActivePreviewOutputs(session, selections, closeDialog = true)
    }

    internal fun applyPreviewChoiceToSelectedPackages(choice: PreviewChoice, packageNames: List<String>) {
        if (choice.isCustom) {
            statusText = "自定义图片需要逐个槽位上传"
            return
        }
        if (choice == PreviewChoice.Gpt && (gptBaseUrl.trim().isEmpty() || gptApiKey.trim().isEmpty())) {
            statusText = "请填写AI提供商信息"
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
                        if (false && outputUri != null) {
                            exportToTree(contentResolver, outputUri, result.outDir)
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
                        generatedPackageNames = updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), generatedPackageNames + successes)
                        multiSelectedPackageNames = multiSelectedPackageNames - successes.toSet()
                    }
                    val result = selectedResult
                    if (result != null && selectedPackageName == selectedAtStart) {
                        activeGenerationSession = result.session
                        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (result.selections).normalLight.name, previewNormalDark = (result.selections).normalDark.name, previewMonochromeLight = (result.selections).monochromeLight.name, previewMonochromeDark = (result.selections).monochromeDark.name) }
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

    internal fun updateBatchApplyProgress(
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

    internal fun generatePackageForPreviewChoice(app: AppEntry, choice: PreviewChoice): GenerationResult {
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

    internal fun clearRmbgCandidateUiState() {
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

    internal fun chooseCustomImageForMode(mode: PreviewMode, kind: CustomImageKind) {
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

    internal fun importCustomPreviewImage(mode: PreviewMode, kind: CustomImageKind, uri: Uri) {
        val session = activeGenerationSession
        if (session == null) {
            statusText = "先生成一次预览后再导入自定义图片"
            return
        }
        statusText = "导入${kind.label}: ${mode.label}"
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
                if (false && outputTreeUri != null) {
                    exportToTree(contentResolver, outputTreeUri, updatedSession.outDir)
                }
                runOnUiThread {
                    activeGenerationSession = updatedSession
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                    previewVersion += 1
                    statusText = "已导入${kind.label}: ${mode.label}"
                    saveUiState()
                }
            } catch (error: Exception) {
                status("${kind.label}导入失败: ${error.message ?: error.javaClass.simpleName}")
            }
        }
    }

    internal fun generateGptCandidateForMode(mode: PreviewMode, confirmed: Boolean = false) {
        val session = activeGenerationSession ?: return
        if (gptBaseUrl.trim().isEmpty() || gptApiKey.trim().isEmpty()) {
            statusText = "请填写AI提供商信息"
            return
        }
        if (isGeneratingGptCandidate || isBusy) {
            return
        }
        if (!confirmed) {
            requestServiceConfirm(
                title = "使用 AI 生成",
                message = "将调用云端图像接口（已累计 $gptRunCount 次）。确认继续？",
                confirmLabel = "继续",
            ) {
                generateGptCandidateForMode(mode, confirmed = true)
            }
            return
        }
        isGeneratingGptCandidate = true
        isGptPreviewLoading = true
        incrementGptRunCount()
        statusText = "AI候选生成中: ${session.packageName}"
        val selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark).withChoice(mode, PreviewChoice.Gpt)
        startUiFriendlyThread("ArtPlusGptCandidate") {
            try {
                // P4 交界：GPT 图层收敛进 pipeline/，显式传调参 + 凭证 + 状态回调。
                val gptLayers = generateGptLayers(session.sourceIcon, session.baseRecfg, session.baseRecbg, mainViewModel.params.value.gptCustomPrompt, GptPromptPreset.fromValue(mainViewModel.params.value.gptPromptPreset), mainViewModel.params.value.foregroundSubjectPercent, GptImageMode.fromValue(mainViewModel.params.value.gptImageMode), gptModelId, gptBaseUrl, gptApiKey, isDebugBuild(), ::status)
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
                if (false && outputTreeUri != null) {
                    exportToTree(contentResolver, outputTreeUri, updatedSession.outDir)
                }
                runOnUiThread {
                    activeGenerationSession = updatedSession
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                    previewVersion += 1
                    val msg = "AI候选已生成并应用到 ${mode.label}"
                    statusText = msg
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    saveUiState()
                }
            } catch (error: Exception) {
                toastStatus("AI候选失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                runOnUiThread {
                    isGeneratingGptCandidate = false
                    isGptPreviewLoading = false
                }
            }
        }
    }

    internal fun generateGptCandidateForAll(confirmed: Boolean = false) {
        val session = activeGenerationSession ?: return
        if (gptBaseUrl.trim().isEmpty() || gptApiKey.trim().isEmpty()) {
            statusText = "请填写AI提供商信息"
            return
        }
        if (isGeneratingGptCandidate || isBusy) {
            return
        }
        if (!confirmed) {
            requestServiceConfirm(
                title = "使用 AI 生成全部",
                message = "将调用云端图像接口（已累计 $gptRunCount 次）。确认继续？",
                confirmLabel = "继续",
            ) {
                generateGptCandidateForAll(confirmed = true)
            }
            return
        }
        isGeneratingGptCandidate = true
        isGptPreviewLoading = true
        incrementGptRunCount()
        statusText = "AI候选生成中: ${session.packageName}"
        val selections = PreviewSelections.default(PreviewChoice.Gpt)
        startUiFriendlyThread("ArtPlusGptCandidateAll") {
            try {
                // P4 交界：GPT 图层收敛进 pipeline/，显式传调参 + 凭证 + 状态回调。
                val gptLayers = generateGptLayers(session.sourceIcon, session.baseRecfg, session.baseRecbg, mainViewModel.params.value.gptCustomPrompt, GptPromptPreset.fromValue(mainViewModel.params.value.gptPromptPreset), mainViewModel.params.value.foregroundSubjectPercent, GptImageMode.fromValue(mainViewModel.params.value.gptImageMode), gptModelId, gptBaseUrl, gptApiKey, isDebugBuild(), ::status)
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
                if (false && outputTreeUri != null) {
                    exportToTree(contentResolver, outputTreeUri, updatedSession.outDir)
                }
                runOnUiThread {
                    activeGenerationSession = updatedSession
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                    previewChoiceMode = null
                    previewVersion += 1
                    val msg = "AI候选已生成并应用到全部"
                    statusText = msg
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    saveUiState()
                }
            } catch (error: Exception) {
                toastStatus("AI候选失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                runOnUiThread {
                    isGeneratingGptCandidate = false
                    isGptPreviewLoading = false
                }
            }
        }
    }

    internal fun generateRmbgCandidateForMode(mode: PreviewMode, confirmed: Boolean = false) {
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
        if (!confirmed) {
            requestServiceConfirm(
                title = "使用 RMBG 抠图",
                message = "将运行本地 ONNX 模型抠图（已累计 $rmbgRunCount 次）。确认继续？",
                confirmLabel = "继续",
            ) {
                generateRmbgCandidateForMode(mode, confirmed = true)
            }
            return
        }
        if (!rmbgGenerationGate.compareAndSet(false, true)) {
            statusText = "RMBG正在运行，请等待"
            return
        }
        isGeneratingRmbgCandidate = true
        incrementRmbgRunCount()
        lastRmbgCandidateError = null
        rmbgCandidatePackageName = session.packageName
        rmbgCandidateMode = mode
        rmbgCandidateStatusText = "RMBG运行中(${RmbgInferenceBackend.Cpu.label})，请等待: ${mode.label}"
        rmbgCandidateFailurePackageName = null
        rmbgCandidateFailureMode = null
        statusText = "RMBG候选生成中(${RmbgInferenceBackend.Cpu.label}): ${session.packageName}"
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
                if (false && outputTreeUri != null) {
                    exportToTree(contentResolver, outputTreeUri, updatedSession.outDir)
                }
                runOnUiThread {
                    activeGenerationSession = updatedSession
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                    previewVersion += 1
                    lastRmbgCandidateError = null
                    lastRmbgInferenceReport = inferenceReport
                    rmbgCandidateFailurePackageName = null
                    rmbgCandidateFailureMode = null
                    val msg = if (result.validationWarning != null) {
                        "${result.validationWarning}，已应用到 ${mode.label}: ${formatRmbgInferenceReport(inferenceReport)}"
                    } else {
                        "RMBG候选已生成并应用到 ${mode.label}: ${formatRmbgInferenceReport(inferenceReport)}"
                    }
                    statusText = msg
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    saveUiState()
                }
            } catch (error: Throwable) {
                val message = describeRmbgFailure(error)
                runOnUiThread {
                    lastRmbgCandidateError = message
                    rmbgCandidateFailurePackageName = session.packageName
                    rmbgCandidateFailureMode = mode
                    val msg = "RMBG候选失败(${RmbgInferenceBackend.Cpu.label}): $message"
                    statusText = msg
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
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

    internal fun generateRmbgCandidateForAll(confirmed: Boolean = false) {
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
        if (!confirmed) {
            requestServiceConfirm(
                title = "使用 RMBG 抠图全部",
                message = "将运行本地 ONNX 模型抠图（已累计 $rmbgRunCount 次）。确认继续？",
                confirmLabel = "继续",
            ) {
                generateRmbgCandidateForAll(confirmed = true)
            }
            return
        }
        if (!rmbgGenerationGate.compareAndSet(false, true)) {
            statusText = "RMBG正在运行，请等待"
            return
        }
        isGeneratingRmbgCandidate = true
        incrementRmbgRunCount()
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
                if (false && outputTreeUri != null) {
                    exportToTree(contentResolver, outputTreeUri, updatedSession.outDir)
                }
                runOnUiThread {
                    activeGenerationSession = updatedSession
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                    previewChoiceMode = null
                    previewVersion += 1
                    lastRmbgCandidateError = null
                    lastRmbgInferenceReport = inferenceReport
                    rmbgCandidateFailurePackageName = null
                    rmbgCandidateFailureMode = null
                    val msg = if (result.validationWarning != null) {
                        "${result.validationWarning}，已应用到全部: ${formatRmbgInferenceReport(inferenceReport)}"
                    } else {
                        "RMBG候选已生成并应用到全部: ${formatRmbgInferenceReport(inferenceReport)}"
                    }
                    statusText = msg
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    saveUiState()
                }
            } catch (error: Throwable) {
                val message = describeRmbgFailure(error)
                runOnUiThread {
                    lastRmbgCandidateError = message
                    rmbgCandidateFailurePackageName = session.packageName
                    rmbgCandidateFailureMode = null
                    val msg = "RMBG候选失败(${RmbgInferenceBackend.Cpu.label}): $message"
                    statusText = msg
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
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

    internal fun refreshActivePreviewOutputs(
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
        val currentSelections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark)
        val outputUri = outputTreeUri
        val requestRevision = ++previewOutputRevision
        previewOutputJob?.cancel()
        isPreviewOutputRefreshing = true
        previewOutputJob = previewWorkerScope.launch {
            try {
                delay(if (rebuildLocalCandidates) PREVIEW_REBUILD_DEBOUNCE_MS else PREVIEW_OUTPUT_DEBOUNCE_MS)
                val updatedSession = when {
                    rebuildLocalCandidates && app != null && currentSession.canRebuildLocalCandidates ->
                        // P4 交界：会话重建收敛进 pipeline/，显式传 pm + 调参快照。
                        rebuildLocalSession(currentSession, app, packageManager, currentTuningParams())
                    else -> currentSession
                }
                val previousDefault = retargetFrom
                    ?: if (rebuildLocalCandidates && currentSession.canRebuildLocalCandidates) {
                        defaultLocalPreviewChoice(currentSession.autoLocalChoice)
                    } else {
                        null
                }
                val nextDefault = defaultLocalPreviewChoice(updatedSession.autoLocalChoice)
                val retargetedSelections = when {
                    previousDefault == null -> currentSelections
                    else -> currentSelections.retarget(previousDefault, nextDefault)
                }
                val selections = normalizePreviewSelections(updatedSession, retargetedSelections)
                writePackageOutputs(updatedSession, selections)
                if (false && outputUri != null) {
                    exportToTree(contentResolver, outputUri, updatedSession.outDir)
                }
                withContext(Dispatchers.Main) {
                    if (requestRevision == previewOutputRevision) {
                        activeGenerationSession = updatedSession
                        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
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

    internal fun writeActivePreviewOutputs(
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
                if (false && outputUri != null) {
                    exportToTree(contentResolver, outputUri, session.outDir)
                }
                withContext(Dispatchers.Main) {
                    if (requestRevision == previewOutputRevision) {
                        activeGenerationSession = session
                        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
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



















    internal fun simpleMonochromeAlphaFromDefaultSubject(source: Bitmap, invertLuma: Boolean): Bitmap {
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





    internal fun bitmapStatsJson(bitmap: Bitmap): JSONObject {
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

    internal fun boundsJson(bounds: Bounds?): Any =
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

    internal fun showKeyboardFor(editText: EditText) {
        editText.post {
            editText.requestFocus()
            editText.context
                .getSystemService(InputMethodManager::class.java)
                ?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    internal fun status(message: String) {
        runOnUiThread { statusText = message }
    }

    internal fun toastStatus(message: String) {
        runOnUiThread {
            statusText = message
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    internal fun cancelBackup() {
        backupJob?.cancel()
        backupJob = null
        backupDotJob?.cancel()
        backupDotJob = null
        backupSheetVisible = false
        backupInBackground = false
        backupProgress = null
        isBusy = false
        toastStatus("已停止备份")
    }

    internal fun cancelSingleExport() {
        singleExportJob?.cancel()
        singleExportJob = null
        singleExportSheetVisible = false
        exportProgress = null
        toastStatus("已停止导出")
    }

    internal fun startBackupDotAnimation() {
        backupDotJob?.cancel()
        backupDotJob = mainScope.launch {
            while (isActive) {
                delay(500)
                backupBackgroundDots = if (backupBackgroundDots >= 3) 1 else backupBackgroundDots + 1
            }
        }
    }

    internal fun stopBackupDotAnimation() {
        backupDotJob?.cancel()
        backupDotJob = null
    }

    internal fun exportSelectedToExternal() {
        if (outputTreeUri == null) {
            toastStatus("还没有设置目录")
            exportDialogVisible = true
            return
        }
        val dir = activeGenerationSession?.outDir
            ?: previewDirPath?.let { File(it) }?.takeIf { it.isDirectory && hasGeneratedPackageBaseAssets(it) }
            ?: selectedPackageName?.let { artPlusPackageDir(it) }?.takeIf { hasGeneratedPackageBaseAssets(it) }
        if (dir == null || !hasGeneratedPackageBaseAssets(dir)) {
            toastStatus("没有可导出的图标包")
            return
        }
        if (isBusy) return
        isBusy = true
        exportProgress = ExportProgress(
            title = "导出中",
            completed = 0,
            total = 1,
            currentLabel = "正在导出: ${dir.name}",
            isIndeterminate = true,
        )
        singleExportSheetVisible = true
        singleExportJob?.cancel()
        singleExportJob = mainScope.launch(Dispatchers.IO) {
            try {
                runCatching { ensureNomediaAtTreeRoot(contentResolver, outputTreeUri) }
                // 优先尝试文件系统直拷（su cp），速度为 SAF 的 10-20 倍，失败再回退 SAF
                val fastOk = runCatching { exportToTreeFast(outputTreeUri, dir) }.getOrDefault(false)
                if (fastOk) {
                    withContext(Dispatchers.Main) { toastStatus("已导出到外部目录: ${dir.name}") }
                } else {
                    withContext(Dispatchers.Main) {
                        runCatching { exportToTree(contentResolver, outputTreeUri, dir) }
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
                    exportProgress = null
                    singleExportSheetVisible = false
                    singleExportJob = null
                    isBusy = false
                }
            }
        }
    }

    internal fun backupAllToExternal(isFromOnboarding: Boolean = false) {
        if (outputTreeUri == null) {
            toastStatus("还没有设置目录")
            exportDialogVisible = true
            return
        }
        if (isBusy) return
        // 若已有备份任务，仅重显弹窗
        if (backupJob?.isActive == true) {
            backupSheetVisible = true
            backupInBackground = false
            stopBackupDotAnimation()
            return
        }
        isBusy = true
        backupInBackground = false
        backupSheetVisible = true
        backupBackgroundDots = 1
        backupProgress = ExportProgress(
            title = "备份中",
            completed = 0,
            total = 1,
            currentLabel = "正在准备...",
            isIndeterminate = true,
        )
        toastStatus("正在备份...")
        backupJob?.cancel()
        backupDotJob?.cancel()
        backupJob = mainScope.launch(Dispatchers.IO) {
            try {
                runCatching { ensureNomediaAtTreeRoot(contentResolver, outputTreeUri) }
                val pkgs = listRootIconPackages()
                if (pkgs.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        backupProgress = null
                        backupSheetVisible = false
                        toastStatus("没有可导出的图标包")
                    }
                    return@launch
                }
                val treeUri = outputTreeUri
                if (treeUri == null) {
                    withContext(Dispatchers.Main) {
                        backupProgress = null
                        backupSheetVisible = false
                        toastStatus("还没有设置目录")
                    }
                    return@launch
                }
                withContext(Dispatchers.Main) {
                    backupProgress = ExportProgress(
                        title = "备份中",
                        completed = 0,
                        total = pkgs.size,
                        currentLabel = "准备备份 ${pkgs.size} 个图标包",
                        isIndeterminate = false,
                    )
                }
                var successCount = 0
                var failCount = 0
                val destRootFast = resolveTreeUriToFilePath(treeUri)
                // 情况1：可解析为文件系统路径 -> 使用 su 直拷（一次 su per pkg，约 10ms/包），最快
                if (destRootFast != null) {
                    for ((index, pkgName) in pkgs.withIndex()) {
                        ensureActive()
                        withContext(Dispatchers.Main) {
                            backupProgress = ExportProgress(
                                title = "备份中",
                                completed = index,
                                total = pkgs.size,
                                currentLabel = "正在备份 ${index + 1}/${pkgs.size}: $pkgName",
                                isIndeterminate = false,
                            )
                            statusText = "正在备份 ${index + 1}/${pkgs.size}: $pkgName"
                        }
                        val ok = runCatching { backupPackageFast(pkgName, destRootFast) }.getOrDefault(false)
                        if (ok) successCount++ else failCount++
                        withContext(Dispatchers.Main) {
                            backupProgress = ExportProgress(
                                title = "备份中",
                                completed = index + 1,
                                total = pkgs.size,
                                currentLabel = if (ok) "已完成 ${index + 1}/${pkgs.size}: $pkgName" else "失败 $pkgName",
                                isIndeterminate = false,
                            )
                        }
                    }
                    withContext(Dispatchers.Main) {
                        if (!backupInBackground) {
                            backupProgress = null
                            backupSheetVisible = false
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
                            backupProgress = ExportProgress(
                                title = "备份中",
                                completed = index,
                                total = pkgs.size,
                                currentLabel = "正在备份 ${index + 1}/${pkgs.size}: $pkgName",
                                isIndeterminate = false,
                            )
                            statusText = "正在备份 ${index + 1}/${pkgs.size}: $pkgName"
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
                                    backupProgress = ExportProgress(
                                        title = "备份中",
                                        completed = index + 1,
                                        total = pkgs.size,
                                        currentLabel = "已跳过 ${pkgName}（无图标）",
                                        isIndeterminate = false,
                                    )
                                }
                                continue
                            }
                            withContext(Dispatchers.Main) {
                                runCatching { exportToTree(contentResolver, treeUri, stagingDir) }.onSuccess { successCount++ }.onFailure { failCount++ }
                                backupProgress = ExportProgress(
                                    title = "备份中",
                                    completed = index + 1,
                                    total = pkgs.size,
                                    currentLabel = "已完成 ${index + 1}/${pkgs.size}: $pkgName",
                                    isIndeterminate = false,
                                )
                            }
                        } catch (_: Exception) {
                            failCount++
                            withContext(Dispatchers.Main) {
                                backupProgress = ExportProgress(
                                    title = "备份中",
                                    completed = index + 1,
                                    total = pkgs.size,
                                    currentLabel = "失败 ${pkgName}",
                                    isIndeterminate = false,
                                )
                            }
                        }
                    }
                    runCatching { stagingRoot.deleteRecursively() }
                    withContext(Dispatchers.Main) {
                        if (!backupInBackground) {
                            backupProgress = null
                            backupSheetVisible = false
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
                    backupProgress = null
                    backupSheetVisible = false
                    backupInBackground = false
                    toastStatus("备份失败: ${e.message ?: e.javaClass.simpleName}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isBusy = false
                    backupJob = null
                    stopBackupDotAnimation()
                    if (!backupInBackground) {
                        backupProgress = null
                        backupSheetVisible = false
                        backupInBackground = false
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
            .put("busy", isBusy)
            .put("status", statusText)
            .put("foreground_subject_percent", mainViewModel.params.value.foregroundSubjectPercent)
            .put("foreground_shadow_level", mainViewModel.params.value.foregroundShadowLevel)
            .put("monochrome_theme_scale", (mainViewModel.params.value.monochromeThemeScale * 100).roundToInt())
            .put("gpt_mode", GptImageMode.fromValue(mainViewModel.params.value.gptImageMode).value)
            .put("gpt_prompt_preset", GptPromptPreset.fromValue(mainViewModel.params.value.gptPromptPreset).value)
            .put("gpt_custom_prompt", mainViewModel.params.value.gptCustomPrompt)
            .put("gpt_base_url", gptBaseUrl)
            .put("gpt_api_key_set", gptApiKey.isNotBlank())
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
            .put("rmbg_actual_backend", lastRmbgInferenceReport?.actualBackend?.value ?: "")
            .put("rmbg_inference_elapsed_ms", lastRmbgInferenceReport?.elapsedMs ?: JSONObject.NULL)
            .put("rmbg_last_error", lastRmbgCandidateError ?: "")
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
            check(!isBusy) { "当前任务正在运行，不能修改参数" }
            // AI 凭据不走 TuningParams（预设不导出密钥），单独处理。
            params["gpt_base_url"]?.let { gptBaseUrl = it }
            params["gpt_api_key"]?.let { gptApiKey = it }
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
        private const val PREFS_NAME = "artplus_mobile"
        private const val PREF_GPT_MODE = "gpt_mode"
        private const val PREF_GPT_PROMPT_PRESET = "gpt_prompt_preset"
        private const val PREF_GPT_CUSTOM_PROMPT = "gpt_custom_prompt"
        private const val PREF_GPT_MODEL_ID = "gpt_model_id"
        private const val PREF_GPT_BASE_URL = "gpt_base_url"
        private const val PREF_GPT_API_KEY = "gpt_api_key"
        private const val PREF_GPT_API_KEY_ENCRYPTED = "gpt_api_key_encrypted"
        private const val PREF_AUTO_CONFIRM_ROOT_WRITE = "auto_confirm_root_write"
        private const val PREF_AUTO_CONFIRM_REFRESH = "auto_confirm_refresh"
        private const val PREF_SKIP_ROOT_WRITE_CONFIRM = "skip_root_write_confirm"
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
        private const val PREF_LIQUID_GLASS_BOTTOM_BAR_ENABLED = "liquid_glass_bottom_bar_enabled"
        private const val PREF_LIQUID_GLASS_BOTTOM_BAR_BLUR_ENABLED = "liquid_glass_bottom_bar_blur_enabled"
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
        private const val PREF_LOCAL_BACKGROUND_SEPARATION_ENABLED = "local_background_separation_enabled"
        private const val PREF_LOCAL_ADAPTIVE_SELECTION_ENABLED = "local_adaptive_selection_enabled"
        private const val PREF_LOCAL_CORNER_MASK_CLEANUP_ENABLED = "local_corner_mask_cleanup_enabled"
        private const val PREF_LOCAL_ALPHA_EDGE_COLOR_REPAIR_ENABLED = "local_alpha_edge_color_repair_enabled"
        private const val PREF_LOCAL_PLAIN_BACKGROUND_ESTIMATION_ENABLED = "local_plain_background_estimation_enabled"
        private const val PREF_LOCAL_ORIGINAL_CLEANUP_ENABLED = "local_original_cleanup_enabled"
        private const val PREF_LOCAL_PLATE_CLEANUP_ENABLED = "local_plate_cleanup_enabled"
        private const val PREF_LOCAL_PLATE_EDGE_REPAIR_ENABLED = "local_plate_edge_repair_enabled"
        private const val PREF_LOCAL_PLATE_RESIDUE_CLEANUP_ENABLED = "local_plate_residue_cleanup_enabled"
        private const val PREF_LOCAL_SHADOW_CLEANUP_ENABLED = "local_shadow_cleanup_enabled"
        private const val PREF_LOCAL_SHADOW_EDGE_REPAIR_ENABLED = "local_shadow_edge_repair_enabled"
        private const val PREF_LOCAL_EDGE_TRIM_ENABLED = "local_edge_trim_enabled"
        private const val PREF_LOCAL_COMPOSED_BACKGROUND_ENABLED = "local_composed_background_enabled"
        private const val PREF_LOCAL_TWO_LAYER_CANDIDATE_ENABLED = "local_two_layer_candidate_enabled"
        private const val PREF_LOCAL_COMPONENT_CANDIDATES_ENABLED = "local_component_candidates_enabled"
        private const val PREF_LOCAL_TEXT_SAFE_CANDIDATE_ENABLED = "local_text_safe_candidate_enabled"
        private const val PREF_LOCAL_AUTO_SELECTION_ENABLED = "local_auto_selection_enabled"
        private const val PREF_LOCAL_EDGE_POLISH_ENABLED = "local_edge_polish_enabled"
        private const val PREF_NIGHT_SUBJECT_LIGHT_BACKGROUND_ENABLED = "night_subject_light_background_enabled"
        private const val PREF_IMAGE_TUNING_VERSION = "image_tuning_version"
        private const val PREF_BATCH_OUTPUT_MODE = "batch_output_mode"
        private const val PREF_GPT_RUN_COUNT = "gpt_run_count"
        private const val PREF_RMBG_RUN_COUNT = "rmbg_run_count"
        private const val PREF_FOREGROUND_SUBJECT_PERCENT_MIGRATED = "foreground_subject_percent_migrated"
        private const val PREF_USAGE_PERMISSION_PROMPTED = "usage_permission_prompted"
        private const val PREF_DEBUG_TOKEN = "debug_token"
        private const val PREF_SELECTED_PACKAGE_NAME = "selected_package_name"
        private const val PREF_GENERATED_FILTER = "generated_filter"
        private const val PREF_QUERY_TEXT = "query_text"
        private const val PREF_ADVANCED_SETTINGS_CATEGORY = "advanced_settings_category"
        private const val PREF_ADVANCED_SETTINGS_TAB = "advanced_settings_tab"
        private const val PREF_PREVIEW_PACKAGE_NAME = "preview_package_name"
        private const val PREF_PREVIEW_DIR_PATH = "preview_dir_path"
        private const val PREF_PREVIEW_STRIP_ENABLED = "preview_strip_enabled"
        private const val PREF_BATCH_PREVIEW_COUNT = "batch_preview_count"
        private const val PREF_BATCH_PREVIEW_COLUMNS = "batch_preview_columns"
        private const val PREF_BATCH_PREVIEW_ICON_SIZE_DP = "batch_preview_icon_size_dp"
        private const val PREF_BATCH_PREVIEW_CORNER_RADIUS_DP = "batch_preview_corner_radius_dp"
        private const val PREF_BATCH_PREVIEW_DESKTOP_BG = "batch_preview_desktop_bg"
        private const val PREF_CUSTOM_WALLPAPER_PATH = "custom_wallpaper_path"
        private const val CUSTOM_WALLPAPER_FILE = "custom_wallpaper.png"
        private const val PREF_PREVIEW_DESKTOP_BACKGROUND = "preview_desktop_background"
        private const val PREF_PREVIEW_ICON_SIZE_DP = "preview_icon_size_dp"
        private const val PREF_PREVIEW_CORNER_RADIUS_DP = "preview_corner_radius_dp"
        private const val PREF_SHOW_SYSTEM_APPS = "show_system_apps"
        private const val PREF_OUTPUT_TREE_URI = "output_tree_uri"
        private const val PREF_HAS_COMPLETED_ONBOARDING = "has_completed_onboarding"
        private const val EXTRA_DEBUG_GENERATE_PACKAGE = "dev.artplus.mobile.DEBUG_GENERATE_PACKAGE"
        private const val EXTRA_DEBUG_GENERATE_USE_GPT = "dev.artplus.mobile.DEBUG_GENERATE_USE_GPT"
        private const val EXTRA_DEBUG_GENERATE_INSTALL_ROOT = "dev.artplus.mobile.DEBUG_GENERATE_INSTALL_ROOT"
        private const val EXTRA_DEBUG_GENERATE_MODE = "dev.artplus.mobile.DEBUG_GENERATE_MODE"
        private const val EXTRA_DEBUG_GENERATE_ROOT_WRITE_MODE = "dev.artplus.mobile.DEBUG_GENERATE_ROOT_WRITE_MODE"
        private const val EXTRA_DEBUG_GENERATE_TOKEN = "dev.artplus.mobile.DEBUG_GENERATE_TOKEN"
        private const val CURRENT_IMAGE_TUNING_VERSION = 4
        private const val SIZE_2X2 = 704
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
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEYSTORE_GPT_KEY_ALIAS = "artplus_gpt_api_key"
        private const val KEYSTORE_CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEYSTORE_GCM_TAG_BITS = 128
        private const val PREVIEW_OUTPUT_DEBOUNCE_MS = 140L
        private const val PREVIEW_REBUILD_DEBOUNCE_MS = 180L
        private const val DEFAULT_PREVIEW_ICON_SIZE_DP = 70
        private const val MIN_PREVIEW_ICON_SIZE_DP = 42
        private const val MAX_PREVIEW_ICON_SIZE_DP = 96
        private const val DEFAULT_PREVIEW_CORNER_RADIUS_DP = 20
        private const val MIN_PREVIEW_CORNER_RADIUS_DP = 0
        private const val MAX_PREVIEW_CORNER_RADIUS_DP = 36
        private const val DEFAULT_BATCH_PREVIEW_COUNT = BatchPreviewSampler.DEFAULT_BATCH_PREVIEW_COUNT
        private const val MIN_BATCH_PREVIEW_COUNT = BatchPreviewSampler.MIN_BATCH_PREVIEW_COUNT
        private const val MAX_BATCH_PREVIEW_COUNT = BatchPreviewSampler.MAX_BATCH_PREVIEW_COUNT
        private const val PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE = 480
        private val appIconCache = object : LruCache<String, Bitmap>(
            ((Runtime.getRuntime().maxMemory() / 1024) / 16).toInt().coerceAtLeast(4 * 1024),
        ) {
            override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount / 1024
        }
        private val SIZE_1X2 = intArrayOf(240, 820)
        private val SIZE_2X1 = intArrayOf(820, 240)
        // P4 pipeline 用（误删恢复）：recfg 原图备份文件名。
        private const val FOREGROUND_ORIGINAL_BACKUP_NAME = "recfg_original_artplus.png"
        private const val RMBG_MIN_MANUAL_COVERAGE = 0.02
        private const val RMBG_MAX_MANUAL_COVERAGE = 0.62
        private const val RMBG_MIN_AUTO_COVERAGE = 0.02
        private const val RMBG_MAX_AUTO_COVERAGE = 0.34
        private const val RMBG_EDGE_ADJUST_MAX_RADIUS = 3
        private const val RMBG_WEAK_ALPHA_MAX_CUT = 72
    }
}

