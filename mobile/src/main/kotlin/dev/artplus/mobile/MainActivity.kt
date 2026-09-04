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


    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun OnboardingDialog() =
        OnboardingDialog(
            visible = onboardingVisible,
            isBusy = isBusy,
            onSkip = {
                // 允许通过外部点击关闭视为跳过
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putBoolean(PREF_HAS_COMPLETED_ONBOARDING, true).apply()
                onboardingVisible = false
                toastStatus("已跳过，可在设置-导出引导中重新进入")
            },
            onChooseDir = {
                // 不在此关闭，等待 chooseTreeLauncher 回调中关闭
                chooseTreeLauncher.launch(null)
            },
        )



    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun RefreshConfirmDialog() =
        RefreshConfirmDialog(
            visible = refreshConfirmVisible,
            rememberAuto = refreshConfirmRememberAuto,
            onDismiss = { refreshConfirmVisible = false },
            onToggleRemember = { refreshConfirmRememberAuto = !refreshConfirmRememberAuto },
            onConfirm = { shouldAuto ->
                refreshConfirmVisible = false
                if (shouldAuto) {
                    autoConfirmRefresh = true
                    saveUiState()
                }
                refreshArtPlusIcons()
            },
        )








    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PermissionCard() =
        PermissionCard(
            packageListGranted = packageListPermissionGranted,
            usageGranted = usageAccessGranted,
            isBusy = isBusy,
            onOpenAppSettings = { openAppPermissionSettings() },
            onOpenUsageSettings = { openUsageAccessSettings() },
        )




    // 重构期间保留：委托到 ui/pages/home/HomePreviewCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun GeneratedPreviewCard() =
        GeneratedPreviewCard(
            dirPath = previewDirPath,
            packageName = previewPackageName,
            session = activeGenerationSession?.takeIf {
                it.packageName == previewPackageName && it.outDir.absolutePath == previewDirPath
            },
            displayAssets = sharedPreviewAssets,
            previewLoading = isGptPreviewLoading || isPreviewAssetsRefreshing || isPreviewOutputRefreshing,
            desktopBackground = previewDesktopBackground,
            iconSizeDp = previewIconSizeDp,
            cornerRadiusDp = previewCornerRadiusDp,
            wallpaperInitial = cachedCustomWallpaper ?: cachedSystemWallpaper ?: cachedBundledWallpaper,
            wallpaperKey = customWallpaperPath,
            loadWallpaper = {
                withContext(Dispatchers.IO) {
                    loadCustomWallpaperBitmap() ?: loadPreviewWallpaperBitmap() ?: loadBundledPreviewWallpaperBitmap()
                }
            },
            materialColorProvider = ::systemMaterialColor,
            previewChoiceMode = previewChoiceMode,
            tuningState = mainViewModel.params.collectAsState().value,
            isBusy = isBusy,
            isGeneratingGptCandidate = isGeneratingGptCandidate,
            isGeneratingRmbgCandidate = isGeneratingRmbgCandidate,
            draftForegroundSubjectPercentText = draftForegroundSubjectPercentText,
            isDark = isSystemInDarkTheme(),
            nightSubjectLightBackgroundEnabled = mainViewModel.params.collectAsState().value.nightSubjectLightBackgroundEnabled,
            rmbgCandidatePackageName = rmbgCandidatePackageName,
            rmbgCandidateMode = rmbgCandidateMode,
            rmbgCandidateFailurePackageName = rmbgCandidateFailurePackageName,
            rmbgCandidateFailureMode = rmbgCandidateFailureMode,
            lastRmbgCandidateError = lastRmbgCandidateError,
            rmbgCandidateStatusText = rmbgCandidateStatusText,
            gptBaseUrl = gptBaseUrl,
            gptApiKey = gptApiKey,
            hasRmbgComponent = findRmbgComponent() != null,
            loadCandidateAssets = { candidate, mode ->
                withContext(previewWorkerDispatcher) {
                    previewAssetsForCandidate(candidate, mode).preparedForDraw()
                }
            },
            onChoiceClick = { previewChoiceMode = it },
            onNightFill = { updateNightSubjectLightBackgroundEnabled(it) },
            onDraftForegroundSubjectPercent = { draftForegroundSubjectPercentText = it },
            onSaveForegroundSubjectPercent = { updateForegroundSubjectPercent(it) },
            onGenerateGpt = { generateGptCandidateForMode(it) },
            onGenerateRmbg = { generateRmbgCandidateForMode(it) },
            onChooseCustom = { mode, kind -> chooseCustomImageForMode(mode, kind) },
            onApplyPreviewChoice = { mode, choice -> applyPreviewChoice(mode, choice) },
            onApplyPreviewChoiceToAll = { applyPreviewChoiceToAll(it) },
            onDismissChoice = { previewChoiceMode = null },
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
            wallpaperKey = customWallpaperPath,
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
            dirPath = previewDirPath,
            packageName = previewPackageName,
            session = activeGenerationSession?.takeIf {
                it.packageName == previewPackageName && it.outDir.absolutePath == previewDirPath
            },
            assets = sharedPreviewAssets,
            tuningState = mainViewModel.params.collectAsState().value,
        )


    // 重构期间保留：委托到 ui/pages/home/HomePreviewCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PreviewControlCard() =
        PreviewControlCard(
            tuningState = mainViewModel.params.collectAsState().value,
            isBusy = isBusy,
            previewCornerRadiusDp = previewCornerRadiusDp,
            draftPreviewCornerRadiusDpText = draftPreviewCornerRadiusDpText,
            previewIconSizeDp = previewIconSizeDp,
            draftPreviewIconSizeDpText = draftPreviewIconSizeDpText,
            draftForegroundSubjectPercentText = draftForegroundSubjectPercentText,
            previewStripEnabled = previewStripEnabled,
            previewDesktopBackground = previewDesktopBackground,
            wallpaperInitial = cachedCustomWallpaper ?: cachedSystemWallpaper ?: cachedBundledWallpaper,
            wallpaperKey = customWallpaperPath,
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
            isBusy = isBusy,
            wallpaperInitial = cachedCustomWallpaper ?: cachedSystemWallpaper ?: cachedBundledWallpaper,
            wallpaperKey = customWallpaperPath,
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
            wallpaperKey = customWallpaperPath,
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
        cornerRadiusDp: Int = previewCornerRadiusDp,
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
        cornerRadiusDp: Int = previewCornerRadiusDp,
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
            isBusy = isBusy,
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
            isBusy = isBusy,
            isGeneratingGptCandidate = isGeneratingGptCandidate,
            isGeneratingRmbgCandidate = isGeneratingRmbgCandidate,
            draftForegroundSubjectPercentText = draftForegroundSubjectPercentText,
            isDark = isSystemInDarkTheme(),
            nightSubjectLightBackgroundEnabled = mainViewModel.params.collectAsState().value.nightSubjectLightBackgroundEnabled,
            rmbgCandidatePackageName = rmbgCandidatePackageName,
            rmbgCandidateMode = rmbgCandidateMode,
            rmbgCandidateFailurePackageName = rmbgCandidateFailurePackageName,
            rmbgCandidateFailureMode = rmbgCandidateFailureMode,
            lastRmbgCandidateError = lastRmbgCandidateError,
            rmbgCandidateStatusText = rmbgCandidateStatusText,
            gptBaseUrl = gptBaseUrl,
            gptApiKey = gptApiKey,
            hasRmbgComponent = findRmbgComponent() != null,
            cornerRadiusDp = previewCornerRadiusDp,
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
            isBusy = isBusy,
            isGeneratingGptCandidate = isGeneratingGptCandidate,
            isGeneratingRmbgCandidate = isGeneratingRmbgCandidate,
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
            isBusy = isBusy,
            isGeneratingGptCandidate = isGeneratingGptCandidate,
            isGeneratingRmbgCandidate = isGeneratingRmbgCandidate,
            rmbgCandidatePackageName = rmbgCandidatePackageName,
            rmbgCandidateMode = rmbgCandidateMode,
            rmbgCandidateFailurePackageName = rmbgCandidateFailurePackageName,
            rmbgCandidateFailureMode = rmbgCandidateFailureMode,
            lastRmbgCandidateError = lastRmbgCandidateError,
            rmbgCandidateStatusText = rmbgCandidateStatusText,
            gptBaseUrl = gptBaseUrl,
            gptApiKey = gptApiKey,
            hasRmbgComponent = findRmbgComponent() != null,
            cornerRadiusDp = previewCornerRadiusDp,
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
            cornerRadiusDp = previewCornerRadiusDp,
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
    ) =
        StatusCard(
            selectedApp = selectedApp,
            launcherCount = launcherCount,
            totalCount = totalCount,
            generatedCount = generatedCount,
            isBusy = isBusy,
            hasApps = apps.isNotEmpty(),
            statusText = statusText,
            onOpenPicker = { currentPage = AppPage.AppPicker },
            appIcon = { entry -> AppIcon(entry, 48.dp) },
        )

    // 重构期间保留：委托到 ui/pages/home/HomeStatusCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun EmptyAppListCard() =
        EmptyAppListCard(
            queryText = queryText,
            showSystemApps = showSystemApps,
            hasHiddenSystemApps = apps.any { AppVisibility.isSystemAppFlags(it.applicationInfo.flags) && it.packageName != packageName },
            isBusy = isBusy,
            onShowSystemApps = {
                showSystemApps = true
                saveUiState()
            },
            onRefresh = { loadApps() },
        )

    // 重构期间保留：委托到 ui/pages/settings/SettingsTuningCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun LocalSeparationModeControl() =
        LocalSeparationModeControl(
            tuningState = mainViewModel.params.collectAsState().value,
            isBusy = isBusy,
            onSelect = { updateLocalSeparationMode(it) },
        )

    /** 第二层级「生成设置」：顶部「滑块 / JSON」切换 + 保存成预设 + 滑块分类导航。 */
    // 重构期间保留：委托到 ui/pages/home/HomeStatusCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun GenerationNavCard() =
        GenerationNavCard(
            isBusy = isBusy,
            advancedSettingsTab = advancedSettingsTab,
            advancedSettingsCategory = advancedSettingsCategory,
            onTabSelected = {
                advancedSettingsTab = it
                saveUiState()
            },
            onRequestSavePreset = {
                presetSaveName = ""
                presetSaveDialogVisible = true
            },
            onCategorySelected = {
                advancedSettingsCategory = it
                saveUiState()
            },
        )


    // 重构期间保留：委托到 ui/pages/settings/SettingsGlassCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun LiquidGlassToggleCard() =
        LiquidGlassToggleCard(
            enabled = mainViewModel.params.collectAsState().value.liquidGlassEnabled,
            isBusy = isBusy,
            onCheckedChange = { updateLiquidGlassEnabled(it) },
        )

    // 重构期间保留：委托到 ui/pages/settings/SettingsGlassCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun LiquidGlassSurfaceCard() =
        LiquidGlassSurfaceCard(
            tuningState = mainViewModel.params.collectAsState().value,
            isBusy = isBusy,
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
            isBusy = isBusy,
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
            isBusy = isBusy,
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
            isBusy = isBusy,
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
        isBusy = isBusy,
        onCheckedChange = { k, v -> updateLocalWorkflowToggle(k, v) },
    )

    // 重构期间保留：委托到 ui/pages/settings/SettingsTuningCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun RmbgTuningCard() =
        RmbgTuningCard(
            tuningState = mainViewModel.params.collectAsState().value,
            isBusy = isBusy,
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
                statusText = "已恢复为当前参数 JSON"
            },
        )

    // ---------- 预设：保存 / 应用 / 批量 / 导入导出 ----------

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun refreshPresets() =
        refreshPresets(
            store = presetStore,
            onBumpVersion = { presetListVersion += 1 },
            onRefreshed = { id, base ->
                activePresetId = id
                activePresetBaseParams = base
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
                batchOutputMode = mode
                gptRunCount = gpt
                rmbgRunCount = rmbg
            },
        )

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

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun saveCurrentAsPreset(rawName: String) =
        saveCurrentAsPreset(
            rawName = rawName,
            store = presetStore,
            current = currentTuningParams(),
            viewModel = mainViewModel,
            onSaved = { preset, msg ->
                activePresetId = preset.id
                activePresetBaseParams = preset.params
                presetListVersion += 1
                presetSaveDialogVisible = false
                statusText = msg
            },
            onStatus = { statusText = it },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun overwritePreset(preset: TuningPreset) =
        overwritePreset(
            preset = preset,
            store = presetStore,
            current = currentTuningParams(),
            viewModel = mainViewModel,
            onOverwritten = { p, cur, msg ->
                activePresetId = p.id
                activePresetBaseParams = cur
                presetListVersion += 1
                statusText = msg
            },
            onStatus = { statusText = it },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun resetToPreset(preset: TuningPreset) =
        resetToPreset(
            preset = preset,
            isBusy = isBusy,
            isGeneratingGptCandidate = isGeneratingGptCandidate,
            isGeneratingRmbgCandidate = isGeneratingRmbgCandidate,
            before = currentTuningParams(),
            viewModel = mainViewModel,
            onReset = { p, merged, msg ->
                applyTuningParams(merged, rebuildCandidates = true)
                presetStore.activePresetId = p.id
                activePresetId = p.id
                activePresetBaseParams = p.params
                statusText = msg
            },
            onStatus = { statusText = it },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun applyPreset(preset: TuningPreset) =
        applyPreset(
            preset = preset,
            isBusy = isBusy,
            isGeneratingGptCandidate = isGeneratingGptCandidate,
            isGeneratingRmbgCandidate = isGeneratingRmbgCandidate,
            before = currentTuningParams(),
            viewModel = mainViewModel,
            onApplied = { p, merged, msg ->
                applyTuningParams(merged, rebuildCandidates = true)
                presetStore.activePresetId = p.id
                activePresetId = p.id
                activePresetBaseParams = p.params
                statusText = msg
            },
            onStatus = { statusText = it },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun deletePreset(id: String) =
        deletePreset(
            id = id,
            filesDir = filesDir,
            store = presetStore,
            viewModel = mainViewModel,
            activeBatchPreviewPresetId = activeBatchPreviewPreset?.id,
            batchPreviewResultPresetId = batchPreviewResult?.preset?.id,
            currentPage = currentPage,
            activePresetId = activePresetId,
            onBatchPreviewReset = {
                activeBatchPreviewPreset = null
                batchPreviewResult = null
            },
            onNavigateHome = { currentPage = AppPage.Home },
            onActiveCleared = {
                activePresetId = null
                activePresetBaseParams = null
            },
            onBumpVersion = { presetListVersion += 1 },
            onStatus = { statusText = it },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun renamePreset(id: String, rawName: String) =
        renamePreset(
            id = id,
            rawName = rawName,
            store = presetStore,
            viewModel = mainViewModel,
            onRenamed = { _, msg ->
                presetListVersion += 1
                statusText = msg
            },
            onStatus = { statusText = it },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun exportPresetsToClipboard() =
        exportPresetsToClipboard(
            store = presetStore,
            clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager,
            onStatus = { statusText = it },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun exportSinglePresetToClipboard(preset: TuningPreset) =
        exportSinglePresetToClipboard(
            preset = preset,
            store = presetStore,
            clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager,
            onStatus = { statusText = it },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun importPresetsFromText(text: String) =
        importPresetsFromText(
            text = text,
            store = presetStore,
            onApplied = { msg ->
                presetImportDialogVisible = false
                presetImportText = ""
                presetListVersion += 1
                statusText = msg
            },
        )

    /** JSON 编辑器：解析文本为 TuningParams 并应用（缺失键保持当前值）。 */
    // 重构期间保留：委托到 ui/pages/presets/PresetOperations.kt 显式参数版本，调用点零改动。
    internal fun saveJsonParamsFromText(text: String) =
        saveJsonParamsFromText(
            text = text,
            current = currentTuningParams(),
            onApplyParams = { applyTuningParams(it, rebuildCandidates = true) },
            onStatus = { statusText = it },
        )

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


    // 重构期间保留：委托到 ui/pages/presets/PresetCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PresetStatusCard() =
        PresetStatusCard(
            presets = remember(presetListVersion) { presetStore.all() },
            activePresetId = activePresetId,
            activePresetBaseParams = activePresetBaseParams,
            currentParams = currentTuningParams(),
            isBusy = isBusy,
            onOverwrite = { overwritePreset(it) },
            onRequestSavePreset = {
                presetSaveName = it
                presetSaveDialogVisible = true
            },
            onResetToPreset = { resetToPreset(it) },
            onResetToDefaults = { resetToDefaults() },
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PresetLibraryCard() =
        PresetLibraryCard(
            presets = remember(presetListVersion) { presetStore.all() },
            activePresetId = activePresetId,
            activePresetBaseParams = activePresetBaseParams,
            currentParams = currentTuningParams(),
            searchQuery = presetSearchQuery,
            onSearchChange = { presetSearchQuery = it },
            listExpanded = presetListExpanded,
            onToggleExpanded = { presetListExpanded = !presetListExpanded },
            isBusy = isBusy,
            onApply = { applyPreset(it) },
            onPreview = { openBatchPreviewForPreset(it) },
            onMore = { presetActionMenuTarget = it },
        )

    // 重构期间保留：委托到 ui/pages/settings/SettingsAppCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun WallpaperSettingsCard() =
        WallpaperSettingsCard(
            hasCustom = customWallpaperPath != null,
            customInfo = customWallpaperInfo,
            isBusy = isBusy,
            onPickWallpaper = {
                chooseWallpaperLauncher.launch(arrayOf("image/jpeg", "image/png", "image/webp"))
            },
            onClearWallpaper = { clearCustomWallpaper() },
        )

    // 重构期间保留：委托到 ui/pages/settings/SettingsAppCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun BatchPreviewSettingsCard() =
        BatchPreviewSettingsCard(
            value = batchPreviewCount,
            draftText = draftBatchPreviewCountText,
            isBusy = isBusy,
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
            isBusy = isBusy,
            onDismiss = onDismiss,
            onApply = { applyPreset(it) },
            onPreview = { openBatchPreviewForPreset(it) },
            onOverwrite = { overwritePreset(it) },
            onRename = { presetRenameTarget = it },
            onExportSingle = { exportSinglePresetToClipboard(it) },
            onDelete = { presetDeleteConfirmTarget = it },
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
            saveDialogVisible = presetSaveDialogVisible,
            saveInitialName = presetSaveName,
            onSaveConfirm = { name ->
                presetSaveDialogVisible = false
                saveCurrentAsPreset(name)
            },
            onSaveDismiss = { presetSaveDialogVisible = false },
            renameTarget = presetRenameTarget,
            onRenameConfirm = { id, name ->
                presetRenameTarget = null
                renamePreset(id, name)
            },
            onRenameDismiss = { presetRenameTarget = null },
            actionMenuTarget = presetActionMenuTarget,
            actionMenuBusy = isBusy,
            onActionMenuDismiss = { presetActionMenuTarget = null },
            onActionApply = { applyPreset(it) },
            onActionPreview = { openBatchPreviewForPreset(it) },
            onActionOverwrite = { overwritePreset(it) },
            onActionRename = { presetRenameTarget = it },
            onActionExportSingle = { exportSinglePresetToClipboard(it) },
            onActionDelete = { presetDeleteConfirmTarget = it },
            deleteConfirmTarget = presetDeleteConfirmTarget,
            onDeleteDismiss = { presetDeleteConfirmTarget = null },
            onDeleteConfirm = { deletePreset(it) },
            importDialogVisible = presetImportDialogVisible,
            onImportConfirm = { text -> importPresetsFromText(text) },
            onImportDismiss = { presetImportDialogVisible = false },
            batchPreviewConfirmTarget = presetBatchPreviewConfirmTarget,
            onBatchPreviewConfirm = {
                presetBatchPreviewConfirmTarget = null
                startBatchPreview(it)
            },
            onBatchPreviewConfirmDismiss = { presetBatchPreviewConfirmTarget = null },
            batchPreviewProgress = batchPreviewProgress,
            onCancelBatchPreview = { batchPreviewCancelled = true },
            showRefreshConfirm = showBatchPreviewRefreshConfirm,
            refreshConfirmPreset = activeBatchPreviewPreset ?: batchPreviewResult?.preset,
            batchPreviewCount = batchPreviewCount,
            onRefreshConfirm = {
                showBatchPreviewRefreshConfirm = false
                startBatchPreview(it)
            },
            onRefreshDismiss = { showBatchPreviewRefreshConfirm = false },
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
            batchPreviewCount = batchPreviewCount,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )

    // 重构期间保留：委托到 ui/pages/presets/PresetDialogs.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun PresetBatchPreviewProgressDialog() =
        PresetBatchPreviewProgressDialog(
            progress = batchPreviewProgress,
            onCancel = { batchPreviewCancelled = true },
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
            batchPreviewCount = batchPreviewCount,
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
                statusText = "已恢复为当前参数 JSON"
            },
        )


    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun RootWriteConfirmDialog() =
        RootWriteConfirmDialog(
            request = pendingRootWriteConfirm,
            rememberSkip = rootWriteConfirmRememberSkip,
            onDismiss = { pendingRootWriteConfirm = null },
            onToggleSkip = { rootWriteConfirmRememberSkip = !rootWriteConfirmRememberSkip },
            onConfirm = { request, shouldSkip ->
                val onConfirm = request.onConfirm
                pendingRootWriteConfirm = null
                if (shouldSkip) {
                    autoConfirmRootWrite = true
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
            isBusy = isBusy,
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
            isBusy = isBusy,
            gptModelId = gptModelId,
            gptBaseUrl = gptBaseUrl,
            gptApiKey = gptApiKey,
            gptRunCount = gptRunCount,
            onGptImageModeChange = { mode ->
                mainViewModel.updateLive { p -> p.copy(gptImageMode = (mode).value) }
                gptSettingsSaveStatus = ""
            },
            onGptPromptPresetChange = { preset ->
                mainViewModel.updateLive { p -> p.copy(gptPromptPreset = (preset).value) }
                gptSettingsSaveStatus = ""
            },
            onGptCustomPromptChange = {
                mainViewModel.updateLive { p -> p.copy(gptCustomPrompt = it) }
                gptSettingsSaveStatus = ""
            },
            onGptModelIdChange = {
                gptModelId = it
                gptSettingsSaveStatus = ""
            },
            onGptBaseUrlChange = {
                gptBaseUrl = it
                gptSettingsSaveStatus = ""
            },
            onGptApiKeyChange = {
                gptApiKey = it
                gptSettingsSaveStatus = ""
            },
        )

    // 重构期间保留：委托到 ui/pages/settings/SettingsAppCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun RmbgComponentCard() =
        RmbgComponentCard(
            component = remember(rmbgComponentStatus) { findRmbgComponent() },
            rmbgRunCount = rmbgRunCount,
            currentPreset = currentRmbgModelPreset(),
            allPresets = RMBG_MODEL_PRESETS,
            lastError = lastRmbgCandidateError,
            componentUrl = rmbgComponentUrl,
            isBusy = isBusy,
            isGenerating = isGeneratingRmbgCandidate,
            isInstalling = isInstallingRmbgComponent,
            installStage = rmbgInstallStage,
            installProgress = rmbgInstallProgress,
            dialogVisible = rmbgDialogVisible,
            onPresetSelected = { updateRmbgModelPreset(it) },
            onComponentUrlChange = {
                rmbgComponentUrl = it
                rmbgComponentSaveStatus = ""
            },
            onDialogVisibleChange = { rmbgDialogVisible = it },
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
            autoConfirmRootWrite = autoConfirmRootWrite,
            autoConfirmRefresh = autoConfirmRefresh,
            isBusy = isBusy,
            outputTreeUri = outputTreeUri,
            treeDisplay = remember(outputTreeUri) { formatTreeUriDisplay(outputTreeUri) },
            backupActive = backupJob?.isActive == true && backupProgress != null,
            backupInBackground = backupInBackground,
            backupDots = backupBackgroundDots,
            exportDialogVisible = exportDialogVisible,
            onAutoConfirmRootWriteChange = {
                autoConfirmRootWrite = it
                saveUiState()
                statusText = if (autoConfirmRootWrite) "已开启自动确认写入" else "已关闭自动确认写入"
            },
            onAutoConfirmRefreshChange = {
                autoConfirmRefresh = it
                saveUiState()
                statusText = if (autoConfirmRefresh) "已开启自动确认刷新" else "已关闭自动确认刷新"
            },
            onBackupRowClick = {
                val active = backupJob?.isActive == true && backupProgress != null
                val inBg = backupInBackground && active
                if (inBg || (active && backupSheetVisible.not())) {
                    backupInBackground = false
                    backupSheetVisible = true
                    stopBackupDotAnimation()
                } else if (active) {
                    backupSheetVisible = true
                } else {
                    exportDialogVisible = true
                }
            },
            onBackupBackgroundActiveChanged = { inBg ->
                if (inBg) startBackupDotAnimation() else stopBackupDotAnimation()
            },
            onExportDialogDismiss = { exportDialogVisible = false },
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
            enabled = previewStripEnabled,
            isBusy = isBusy,
            onCheckedChange = { updatePreviewStripEnabled(it) },
        )

    // 重构期间保留：委托到 ui/pages/settings/SettingsGlassCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun LiquidGlassToggleRow() =
        LiquidGlassToggleRow(
            enabled = mainViewModel.params.collectAsState().value.liquidGlassEnabled,
            isBusy = isBusy,
            onToggle = { updateLiquidGlassEnabled(!mainViewModel.params.value.liquidGlassEnabled) },
        )



    // Slice 2.3 已搬入 ui/pages/settings/SettingsAppCards.kt：InputSettingsCard（纯 UI，直接搬迁，不留 wrapper）。
    // 重构期间保留：委托到 ui/pages/settings/SettingsAppCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun ShowSystemAppsToggleRow() =
        ShowSystemAppsToggleRow(
            checked = showSystemApps,
            isBusy = isBusy,
            onToggle = {
                showSystemApps = !showSystemApps
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
            multiCount = multiSelectedPackageNames.size,
            isScanning = isScanningGeneratedPackages,
            scanFailed = generatedScanFailed,
            isBusy = isBusy,
            hasApps = apps.isNotEmpty(),
            onRefreshGenerated = { refreshGeneratedPackages() },
            onReloadApps = { loadApps() },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun AppPickerFilterCard() =
        AppPickerFilterCard(
            showSystemApps = showSystemApps,
            generatedFilter = generatedFilter,
            isBusy = isBusy,
            onToggleSystemApps = {
                showSystemApps = !showSystemApps
                saveUiState()
            },
            onFilterSelected = {
                generatedFilter = it
                queryText = ""
                saveUiState()
            },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun AppPickerSearchCard(filteredApps: List<AppEntry>) =
        AppPickerSearchCard(
            queryText = queryText,
            isBusy = isBusy,
            onQueryChange = {
                queryText = it
                saveUiState()
            },
            onClearQuery = {
                queryText = ""
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
            multiCount = multiSelectedPackageNames.size,
            isScanning = isScanningGeneratedPackages,
            scanFailed = generatedScanFailed,
            isBusy = isBusy,
            hasApps = apps.isNotEmpty(),
            showSystemApps = showSystemApps,
            generatedFilter = generatedFilter,
            queryText = queryText,
            onRefreshGenerated = { refreshGeneratedPackages() },
            onReloadApps = { loadApps() },
            onToggleSystemApps = {
                showSystemApps = !showSystemApps
                saveUiState()
            },
            onFilterSelected = {
                generatedFilter = it
                queryText = ""
                saveUiState()
            },
            onQueryChange = {
                queryText = it
                saveUiState()
            },
            onClearQuery = {
                queryText = ""
                saveUiState()
            },
            multiSelectContent = { AppMultiSelectActions(filteredApps) },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerCards.kt 显式参数版本，调用点零改动。
    @Composable
    internal fun AppMultiSelectActions(filteredApps: List<AppEntry>) {
        val filteredPackageNames = remember(filteredApps) { filteredApps.map { it.packageName }.toSet() }
        AppMultiSelectActions(
            selectedCount = multiSelectedPackageNames.size,
            hasFiltered = filteredPackageNames.isNotEmpty(),
            allFilteredSelected = pickerAllFilteredSelected(filteredPackageNames, multiSelectedPackageNames),
            isBusy = isBusy,
            onToggleFiltered = {
                val allSelected = pickerAllFilteredSelected(filteredPackageNames, multiSelectedPackageNames)
                multiSelectedPackageNames = if (allSelected) {
                    multiSelectedPackageNames - filteredPackageNames
                } else {
                    multiSelectedPackageNames + filteredPackageNames
                }
            },
            onClear = { multiSelectedPackageNames = emptySet() },
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
            isBusy = isBusy,
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


    // 重构期间保留：委托到 ui/pages/picker/PickerIo.kt 显式参数版本，调用点零改动。
    internal suspend fun loadCachedAppIcon(entry: AppEntry): Bitmap? =
        pickerLoadCachedAppIcon(
            entry = entry,
            getCached = { key -> synchronized(appIconCache) { appIconCache.get(key) } },
            putCached = { key, value -> synchronized(appIconCache) { appIconCache.put(key, value) } },
            loadBitmap = { e ->
                runCatching { loadAppIconBitmap(e, packageManager, ICON_CACHE_SIZE) }.getOrNull()
            },
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
            isBusy = isBusy,
            onStatusText = { statusText = it },
            onLaunch = { name, block -> startUiFriendlyThread(name, block) },
            openInputBytes = { u -> contentResolver.openInputStream(u)?.use { it.readBytes() } },
            filesDir = filesDir,
            fileName = CUSTOM_WALLPAPER_FILE,
            onSuccess = { path, info ->
                runOnUiThread {
                    cachedCustomWallpaper = null
                    cachedCustomWallpaperPath = null
                    customWallpaperPath = path
                    customWallpaperInfo = info
                    statusText = "已导入自定义壁纸（$info），「桌面」背景优先使用此图"
                    saveUiState()
                }
            },
            onError = ::status,
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerIo.kt 显式参数版本，调用点零改动。
    internal fun clearCustomWallpaper() =
        pickerClearCustomWallpaper(
            filesDir = filesDir,
            customPath = customWallpaperPath,
            fileName = CUSTOM_WALLPAPER_FILE,
            onCleared = { statusText = it },
            onSave = ::saveUiState,
            clearCache = {
                cachedCustomWallpaper = null
                cachedCustomWallpaperPath = null
            },
            setPath = { customWallpaperPath = it },
            setInfo = { customWallpaperInfo = it },
        )

    // 纯函数 centerCropToSixteenNine 已直接搬迁至 ui/pages/picker/PickerIo.kt，同包直接引用，不留 wrapper。

    // 重构期间保留：委托到 ui/pages/picker/PickerIo.kt 显式参数版本，调用点零改动。
    internal fun loadCustomWallpaperBitmap(): Bitmap? =
        pickerLoadCustomWallpaperBitmap(
            path = customWallpaperPath,
            cachedPath = cachedCustomWallpaperPath,
            getCached = { cachedCustomWallpaper },
            setCached = { path, bitmap ->
                cachedCustomWallpaper = bitmap
                cachedCustomWallpaperPath = path
            },
            shortEdge = PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE,
        )




    // 重构期间保留：委托到 ui/pages/picker/PickerAppOps.kt 显式参数版本，调用点零改动。
    internal fun loadApps(refreshGenerated: Boolean = false) =
        pickerLoadApps(
            refreshGenerated = refreshGenerated,
            markLoad = { didRequestAppLoad = true },
            queryApps = { loadApps(packageManager) },
            preloadIcons = { entries ->
                preloadAppIcons(appIconCache, packageManager, entries, ICON_CACHE_SIZE, PRELOAD_ICON_COUNT)
            },
            postOnUi = { result ->
                runOnUiThread {
                    refreshPermissionState()
                    androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
                        apps.clear()
                        apps.addAll(result.entries)
                    }
                    statusText = when {
                        result.entries.isEmpty() -> "没有读取到应用。请确认已允许读取应用列表。"
                        !packageListPermissionGranted -> "读取到 ${apps.size} 个应用，但应用列表权限状态异常。"
                        else -> "共 ${apps.size} 个应用，其中 ${result.launchablePackages.size} 个有启动器入口。"
                    }
                    if (refreshGenerated) {
                        refreshGeneratedPackages(result.entries)
                    }
                }
            },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerAppOps.kt 显式参数版本，调用点零改动。
    internal fun refreshGeneratedPackages(entries: List<AppEntry> = apps.toList()) =
        pickerRefreshGeneratedPackages(
            entries = entries,
            onEmpty = {
                generatedScanFailed = false
                isScanningGeneratedPackages = false
                statusText = "应用列表为空，保留已生成缓存"
            },
            onScanningChange = { isScanningGeneratedPackages = it },
            onScanFailed = { generatedScanFailed = it },
            scan = ::scanRootGeneratedPackages,
            postOnUi = { block -> runOnUiThread(Runnable { block() }) },
            onSuccess = { generated ->
                generatedPackageNames = updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), generated)
                generatedScanFailed = false
                statusText = "已刷新生成状态: ${generated.size} 个"
            },
            onFailure = {
                generatedScanFailed = true
                statusText = "生成状态刷新失败，保留上次缓存: ${it.message ?: it.javaClass.simpleName}"
            },
        )




    // 重构期间保留：委托到 ui/pages/picker/PickerAppOps.kt 显式参数版本，调用点零改动。
    internal fun loadUiState() =
        pickerLoadUiState(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            persistedReadWriteUri = contentResolver.persistedUriPermissions.firstOrNull { it.isReadPermission && it.isWritePermission }?.uri,
            setSelectedPackage = { selectedPackageName = it },
            setGeneratedFilter = { generatedFilter = it },
            setShowSystemApps = { showSystemApps = it },
            setQueryText = { queryText = it },
            setAdvancedCategory = { advancedSettingsCategory = it },
            setAdvancedTab = { advancedSettingsTab = it },
            setPreviewPackage = { previewPackageName = it },
            setPreviewDir = { previewDirPath = it },
            setPreviewStrip = { previewStripEnabled = it },
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
            setDesktopBackground = { previewDesktopBackground = it },
            setIconSize = { previewIconSizeDp = it },
            setDraftIconSizeText = { draftPreviewIconSizeDpText = it },
            setCornerRadius = { previewCornerRadiusDp = it },
            setDraftCornerRadiusText = { draftPreviewCornerRadiusDpText = it },
            setBatchCount = { batchPreviewCount = it },
            setDraftBatchCountText = { draftBatchPreviewCountText = it },
            setBatchColumns = { batchPreviewColumns = it },
            setDraftBatchColumnsText = { draftBatchPreviewColumnsText = it },
            setBatchIconSize = { batchPreviewIconSizeDp = it },
            setDraftBatchIconSizeText = { draftBatchPreviewIconSizeDpText = it },
            setBatchCorner = { batchPreviewCornerRadiusDp = it },
            setDraftBatchCornerText = { draftBatchPreviewCornerRadiusDpText = it },
            setBatchDesktopBg = { batchPreviewDesktopBackground = it },
            setCustomPath = { customWallpaperPath = it },
            setCustomInfo = { customWallpaperInfo = it },
            setAutoRoot = { autoConfirmRootWrite = it },
            setAutoRefresh = { autoConfirmRefresh = it },
            setOutputUri = { outputTreeUri = it },
            setOnboardingVisible = { onboardingVisible = it },
            parseUri = { runCatching { Uri.parse(it) }.getOrNull() },
            isFile = ::pickerIsCustomWallpaperFile,
            decodeBounds = ::pickerDecodeWallpaperBounds,
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerAppOps.kt 显式参数版本，调用点零改动。
    internal fun saveUiState() =
        pickerSaveUiState(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            selectedPackage = selectedPackageName,
            generatedFilter = generatedFilter,
            showSystemApps = showSystemApps,
            queryText = queryText,
            advancedCategory = advancedSettingsCategory,
            advancedTab = advancedSettingsTab,
            previewPackage = previewPackageName,
            previewDir = previewDirPath,
            previewStrip = previewStripEnabled,
            previewNormalLight = mainViewModel.params.value.previewNormalLight,
            previewNormalDark = mainViewModel.params.value.previewNormalDark,
            previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
            previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
            desktopBackground = previewDesktopBackground,
            iconSize = previewIconSizeDp,
            cornerRadius = previewCornerRadiusDp,
            batchCount = batchPreviewCount,
            batchColumns = batchPreviewColumns,
            batchIconSize = batchPreviewIconSizeDp,
            batchCorner = batchPreviewCornerRadiusDp,
            batchDesktopBg = batchPreviewDesktopBackground,
            customPath = customWallpaperPath,
            autoRoot = autoConfirmRootWrite,
            autoRefresh = autoConfirmRefresh,
            outputUri = outputTreeUri,
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerAppOps.kt 显式参数版本，调用点零改动。
    internal fun refreshArtPlusIcons() =
        pickerRefreshArtPlusIcons(
            isBusy = isBusy,
            isRefreshing = isRefreshingArtPlusIcons,
            onRefreshingChange = { isRefreshingArtPlusIcons = it },
            onStatusText = { statusText = it },
            blockingRefresh = ::refreshArtPlusIconsBlocking,
            contentResolver = contentResolver,
            apkPath = applicationInfo.sourceDir,
            postOnUi = { block -> runOnUiThread(Runnable { block() }) },
            onSuccess = { summary ->
                statusText = if (summary.isBlank()) {
                    "已刷新 ART+ 图标"
                } else {
                    "已刷新 ART+ 图标: $summary"
                }
            },
            onFailure = { error ->
                statusText = "刷新 ART+ 图标失败: ${error.message ?: error.javaClass.simpleName}"
            },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerAppOps.kt 显式参数版本，调用点零改动。
    internal fun refreshPermissionState() =
        pickerRefreshPermissionState(
            checkQueryPermission = { pickerCheckQueryPermission(packageManager, packageName) },
            hasUsage = ::hasUsageAccess,
            onResult = { queryGranted, usageGranted ->
                packageListPermissionGranted = queryGranted
                usageAccessGranted = usageGranted
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
            usageGranted = usageAccessGranted,
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
            onError = { statusText = it },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    internal fun openUsageAccessSettings() =
        pickerOpenUsageAccessSettings(
            start = ::startActivity,
            onError = { statusText = it },
        )

    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    internal fun openExternalLink(url: String) =
        pickerOpenExternalLink(
            start = ::startActivity,
            url = url,
            onError = { statusText = it },
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
            isChecking = isCheckingUpdate,
            onCheckingChange = { isCheckingUpdate = it },
            onStatusText = { statusText = it },
            scope = mainScope,
            resolveUrl = { pickerResolveUpdateUrl(it, "检查更新", isDebugBuild()) },
            fetchLatest = ::pickerFetchUpdateBody,
            currentVersion = currentVersionName(),
            onUpdateAvailable = { info, text ->
                updateAvailableInfo = info
                statusText = text
            },
            onUpToDate = {
                updateUpToDateDialogVisible = true
                statusText = it
            },
            onFailed = { statusText = it },
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
            isBusyGet = { isBusy },
            setBusy = { isBusy = it },
            setStatusText = { statusText = it },
            onStatus = ::status,
            getAppInfo = ::getApplicationInfoCompat,
            packageManager = packageManager,
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            getGeneratedNames = { generatedPackageNames },
            setGeneratedNames = { generatedPackageNames = it },
            generatePackage = ::generateArtPlusPackage,
            setActiveSession = { activeGenerationSession = it },
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
            setPreviewChoiceMode = { previewChoiceMode = it },
            setPreviewPackage = { previewPackageName = it },
            setPreviewDir = { previewDirPath = it },
            bumpPreviewVersion = { previewVersion += 1 },
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
            setLastReport = { lastRmbgInferenceReport = it },
            setLastError = { lastRmbgCandidateError = it },
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
            setGptModelId = { gptModelId = it },
            setGptBaseUrl = { gptBaseUrl = it },
            setGptApiKey = { gptApiKey = it },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsGptRmbg.kt 显式参数版本，调用点零改动。
    internal fun saveGptSettings(): Boolean =
        paramsSaveGptSettings(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            getParams = { mainViewModel.params.value },
            getGptApiKey = { gptApiKey },
            getGptModelId = { gptModelId },
            getGptBaseUrl = { gptBaseUrl },
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
            setGptSaveStatus = { gptSettingsSaveStatus = it },
            setRmbgSaveStatus = { rmbgComponentSaveStatus = it },
            setStatusText = { statusText = it },
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
            setComponentUrl = { rmbgComponentUrl = it },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsGptRmbg.kt 显式参数版本，调用点零改动。
    internal fun saveRmbgSettings(): Boolean =
        paramsSaveRmbgSettings(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            getComponentUrl = { rmbgComponentUrl },
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
            getSession = { activeGenerationSession },
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
            setStatusText = { statusText = it },
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
        paramsCurrentRmbgModelPreset(componentUrl = rmbgComponentUrl)

    // 重构期间保留：委托到 ui/pages/params/ParamsGptRmbg.kt 显式参数版本，调用点零改动。
    internal fun updateRmbgModelPreset(preset: RmbgModelPreset): Unit =
        paramsUpdateRmbgModelPreset(
            preset = preset,
            setComponentUrl = { rmbgComponentUrl = it },
            setSaveStatus = { rmbgComponentSaveStatus = it },
            setStatusText = { statusText = it },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsGptRmbg.kt 显式参数版本，调用点零改动。
    internal fun rmbgInferenceStatusSummary(): String =
        paramsRmbgInferenceStatusSummary(
            isGenerating = isGeneratingRmbgCandidate,
            candidateStatusText = rmbgCandidateStatusText,
            report = lastRmbgInferenceReport,
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
            onCaptureUndo = { lastParamsSnapshot = it },
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
            isBusy = { isBusy },
            getSession = { activeGenerationSession },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    /** 撤销上一次参数应用（预设/批量前自动捕获快照）。 */
    // 重构期间保留：委托到 ui/pages/params/ParamsHistory.kt 显式参数版本，调用点零改动。
    internal fun restoreLastParams(): Unit =
        paramsRestoreLastParams(
            getSnapshot = { lastParamsSnapshot },
            clearSnapshot = { lastParamsSnapshot = null },
            setStatusText = { statusText = it },
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
            isBusy = { isBusy },
            isGeneratingGpt = { isGeneratingGptCandidate },
            isGeneratingRmbg = { isGeneratingRmbgCandidate },
            setStatusText = { statusText = it },
            onRequestConfirm = { title, message, confirmLabel, onConfirm ->
                requestServiceConfirm(title = title, message = message, confirmLabel = confirmLabel, onConfirm = onConfirm)
            },
            onApplyDefaults = { applyTuningParams(it, rebuildCandidates = true) },
            onClearPreset = {
                presetStore.activePresetId = null
                activePresetId = null
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
            isBusy = { isBusy },
            isGeneratingGpt = { isGeneratingGptCandidate },
            isGeneratingRmbg = { isGeneratingRmbgCandidate },
            setStatusText = { statusText = it },
            onUndo = { mainViewModel.undo() },
            onApply = { applyTuningParams(it, captureUndo = false) },
            onClearPreset = {
                presetStore.activePresetId = null
                activePresetId = null
            },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsHistory.kt 显式参数版本，调用点零改动。
    internal fun redoTuning(): Unit =
        paramsRedoTuning(
            isBusy = { isBusy },
            isGeneratingGpt = { isGeneratingGptCandidate },
            isGeneratingRmbg = { isGeneratingRmbgCandidate },
            setStatusText = { statusText = it },
            onRedo = { mainViewModel.redo() },
            onApply = { applyTuningParams(it, captureUndo = false) },
            onClearPreset = {
                presetStore.activePresetId = null
                activePresetId = null
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
            setStatusText = { statusText = it },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updatePreviewCornerRadiusDp(value: Int): Unit =
        paramsUpdatePreviewCornerRadiusDp(
            value = value,
            setValue = { previewCornerRadiusDp = it },
            setDraftText = { draftPreviewCornerRadiusDpText = it },
            onSave = { saveUiState() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updatePreviewIconSizeDp(value: Int): Unit =
        paramsUpdatePreviewIconSizeDp(
            value = value,
            setValue = { previewIconSizeDp = it },
            setDraftText = { draftPreviewIconSizeDpText = it },
            onSave = { saveUiState() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updateBatchPreviewCount(value: Int): Unit =
        paramsUpdateBatchPreviewCount(
            value = value,
            setValue = { batchPreviewCount = it },
            setDraftText = { draftBatchPreviewCountText = it },
            onSave = { saveUiState() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updateBatchPreviewColumns(value: Int): Unit =
        paramsUpdateBatchPreviewColumns(
            value = value,
            setColumns = { batchPreviewColumns = it },
            setDraftColumnsText = { draftBatchPreviewColumnsText = it },
            setIconSize = { batchPreviewIconSizeDp = it },
            setDraftIconSizeText = { draftBatchPreviewIconSizeDpText = it },
            onSave = { saveUiState() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updateBatchPreviewIconSizeDp(value: Int): Unit =
        paramsUpdateBatchPreviewIconSizeDp(
            value = value,
            setValue = { batchPreviewIconSizeDp = it },
            setDraftText = { draftBatchPreviewIconSizeDpText = it },
            onSave = { saveUiState() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updateBatchPreviewCornerRadiusDp(value: Int): Unit =
        paramsUpdateBatchPreviewCornerRadiusDp(
            value = value,
            setValue = { batchPreviewCornerRadiusDp = it },
            setDraftText = { draftBatchPreviewCornerRadiusDpText = it },
            onSave = { saveUiState() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updateBatchPreviewDesktopBackground(option: PreviewDesktopBackground): Unit =
        paramsUpdateBatchPreviewDesktopBackground(
            option = option,
            getValue = { batchPreviewDesktopBackground },
            setValue = { batchPreviewDesktopBackground = it },
            onSave = { saveUiState() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updatePreviewDesktopBackground(option: PreviewDesktopBackground): Unit =
        paramsUpdatePreviewDesktopBackground(
            option = option,
            getValue = { previewDesktopBackground },
            setValue = { previewDesktopBackground = it },
            onSave = { saveUiState() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsPreviewDisplay.kt 显式参数版本，调用点零改动。
    internal fun updatePreviewStripEnabled(enabled: Boolean): Unit =
        paramsUpdatePreviewStripEnabled(
            enabled = enabled,
            getValue = { previewStripEnabled },
            setValue = { previewStripEnabled = it },
            onSave = { saveUiState() },
            setStatusText = { statusText = it },
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
            setBottomBarEnabled = { liquidGlassBottomBarEnabled = it },
            setBottomBarBlurEnabled = { liquidGlassBottomBarBlurEnabled = it },
            getBottomBarEnabled = { liquidGlassBottomBarEnabled },
            getBottomBarBlurEnabled = { liquidGlassBottomBarBlurEnabled },
            onSave = { saveLiquidGlassSettings() },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun saveLiquidGlassSettings(): Unit =
        paramsSaveLiquidGlassSettings(
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE),
            getParams = { mainViewModel.params.value },
            getBottomBarEnabled = { liquidGlassBottomBarEnabled },
            getBottomBarBlurEnabled = { liquidGlassBottomBarBlurEnabled },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun SharedPreferences.Editor.putLiquidGlassSettings(): SharedPreferences.Editor =
        paramsPutLiquidGlassSettings(
            params = mainViewModel.params.value,
            bottomBarEnabled = liquidGlassBottomBarEnabled,
            bottomBarBlurEnabled = liquidGlassBottomBarBlurEnabled,
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassEnabled(enabled: Boolean): Unit =
        paramsUpdateLiquidGlassEnabled(
            enabled = enabled,
            getParams = { mainViewModel.params.value },
            updateLive = mainViewModel::updateLive,
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { statusText = it },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassRadius(value: Int): Unit =
        paramsUpdateLiquidGlassRadius(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassRadiusText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { statusText = it },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassOuterWidth(value: Int): Unit =
        paramsUpdateLiquidGlassOuterWidth(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassOuterWidthText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { statusText = it },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassTopAlpha(value: Int): Unit =
        paramsUpdateLiquidGlassTopAlpha(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassTopAlphaText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { statusText = it },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassBottomAlpha(value: Int): Unit =
        paramsUpdateLiquidGlassBottomAlpha(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassBottomAlphaText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { statusText = it },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassBackgroundMistAlpha(value: Int): Unit =
        paramsUpdateLiquidGlassBackgroundMistAlpha(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassBackgroundMistAlphaText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { statusText = it },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassBottomDarkAlpha(value: Int): Unit =
        paramsUpdateLiquidGlassBottomDarkAlpha(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassBottomDarkAlphaText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { statusText = it },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassSubjectScalePercent(value: Int): Unit =
        paramsUpdateLiquidGlassSubjectScalePercent(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassSubjectScaleText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { statusText = it },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassSubjectOutlineWidth(value: Int): Unit =
        paramsUpdateLiquidGlassSubjectOutlineWidth(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassSubjectOutlineWidthText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { statusText = it },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassSubjectInnerOutlineWidth(value: Int): Unit =
        paramsUpdateLiquidGlassSubjectInnerOutlineWidth(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassSubjectInnerOutlineWidthText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { statusText = it },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassSubjectShadowAlpha(value: Int): Unit =
        paramsUpdateLiquidGlassSubjectShadowAlpha(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassSubjectShadowAlphaText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { statusText = it },
            onRefresh = { rebuild -> refreshActivePreviewOutputs(rebuildLocalCandidates = rebuild) },
        )

    // 重构期间保留：委托到 ui/pages/params/ParamsLiquidGlass.kt 显式参数版本，调用点零改动。
    internal fun updateLiquidGlassSubjectOpacityPercent(value: Int): Unit =
        paramsUpdateLiquidGlassSubjectOpacityPercent(
            value = value,
            updateLive = mainViewModel::updateLive,
            setDraftText = { draftLiquidGlassSubjectOpacityText = it },
            onSave = { saveLiquidGlassSettings() },
            setStatusText = { statusText = it },
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
            entry = apps.firstOrNull { it.packageName == selectedPackageName },
            installWithRoot = installWithRoot,
            useGpt = useGpt,
            rootWriteMode = rootWriteMode,
            confirmed = confirmed,
            gptApiKey = gptApiKey,
            gptBaseUrl = gptBaseUrl,
            isBusy = isBusy,
            gptRunCount = gptRunCount,
            onStatusText = { statusText = it },
            onRequestConfirm = { title, message, confirmLabel, onConfirm ->
                requestServiceConfirm(title = title, message = message, confirmLabel = confirmLabel, onConfirm = onConfirm)
            },
            onBeginBusy = { gpt ->
                isBusy = true
                if (gpt) {
                    isGptPreviewLoading = true
                    incrementGptRunCount()
                }
            },
            onLaunch = { name, block -> startUiFriendlyThread(name, block) },
            onGenerate = { e, g -> generateArtPlusPackage(e, g) },
            onPostGenerate = { result, e ->
                runOnUiThread {
                    activeGenerationSession = result.session
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (result.selections).normalLight.name, previewNormalDark = (result.selections).normalDark.name, previewMonochromeLight = (result.selections).monochromeLight.name, previewMonochromeDark = (result.selections).monochromeDark.name) }
                    previewChoiceMode = null
                    previewPackageName = e.packageName
                    previewDirPath = result.outDir.absolutePath
                    previewVersion += 1
                    saveUiState()
                }
            },
            onInstall = { outDir, pkg, mode -> installWithRoot(outDir, pkg, mode) },
            onMarkGenerated = { pkg -> generatedPackageNames = markPackageGenerated(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), generatedPackageNames, pkg) },
            onToast = { toastStatus(it) },
            onStatus = { status(it) },
            onFinish = { gpt ->
                runOnUiThread {
                    isBusy = false
                    if (gpt) {
                        isGptPreviewLoading = false
                    }
                }
            },
            onConfirmedRetry = { root: Boolean, gpt: Boolean, mode: RootWriteMode -> generateSelected(root, gpt, mode, confirmed = true) },
        )

    // 重构期间保留：委托到 ui/pages/home/HomeGenerationOps.kt 显式参数版本（含内嵌 executeWrite），调用点零改动。
    internal fun writeSelectedWithRoot(rootWriteMode: RootWriteMode): Unit =
        homeWriteSelectedWithRoot(
            entry = apps.firstOrNull { it.packageName == selectedPackageName },
            rootWriteMode = rootWriteMode,
            isBusy = isBusy,
            activeSession = activeGenerationSession,
            selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
            autoConfirmRootWrite = autoConfirmRootWrite,
            targetPath = apps.firstOrNull { it.packageName == selectedPackageName }?.let { "$ROOT_UXICONS_DIR/${it.packageName}" },
            onStatusText = { statusText = it },
            onGenerateFallback = { generateSelected(installWithRoot = true, useGpt = false, rootWriteMode = rootWriteMode) },
            onBeginBusy = { msg ->
                isBusy = true
                statusText = msg
            },
            onLaunch = { name, block -> startUiFriendlyThread(name, block) },
            onWrite = { session, selections -> writePackageOutputs(session, selections) },
            onInstall = { outDir, pkg, mode -> installWithRoot(outDir, pkg, mode) },
            onPostWrite = { session, selections, e ->
                runOnUiThread {
                    generatedPackageNames = markPackageGenerated(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), generatedPackageNames, e.packageName)
                    activeGenerationSession = session
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                    previewPackageName = e.packageName
                    previewDirPath = session.outDir.absolutePath
                    previewVersion += 1
                    saveUiState()
                }
            },
            onToast = { toastStatus(it) },
            onFinish = { runOnUiThread { isBusy = false } },
            onRequestConfirm = { pkg, targetPath, mode, onConfirm ->
                rootWriteConfirmRememberSkip = false
                pendingRootWriteConfirm = RootWriteConfirmRequest(
                    packageName = pkg,
                    targetPath = targetPath,
                    rootWriteMode = mode,
                    onConfirm = { onConfirm() },
                )
            },
        )

    // 重构期间保留：委托到 ui/pages/home/HomeGenerationOps.kt 显式参数版本，调用点零改动。
    internal fun selectAppAndRestoreGeneratedPreview(entry: AppEntry) {
        val revision = ++generatedPreviewRestoreRevision
        val localDir = artPlusPackageDir(entry.packageName)
        val known = entry.packageName in generatedPackageNames || hasGeneratedPackageBaseAssets(localDir)
        homeSelectAppAndRestore(
            entry = entry,
            revision = revision,
            isBusy = isBusy,
            knownGenerated = known,
            getSelected = { selectedPackageName },
            getRevision = { generatedPreviewRestoreRevision },
            onResetSelection = { pkg ->
                selectedPackageName = pkg
                activeGenerationSession = null
                previewChoiceMode = null
                previewPackageName = null
                previewDirPath = null
                mainViewModel.updateLive { p -> p.copy(previewNormalLight = (PreviewSelections.default(PreviewChoice.Original)).normalLight.name, previewNormalDark = (PreviewSelections.default(PreviewChoice.Original)).normalDark.name, previewMonochromeLight = (PreviewSelections.default(PreviewChoice.Original)).monochromeLight.name, previewMonochromeDark = (PreviewSelections.default(PreviewChoice.Original)).monochromeDark.name) }
                previewVersion += 1
            },
            onStatusText = { statusText = it },
            onSaveUi = { saveUiState() },
            onClearRmbg = { clearRmbgCandidateUiState() },
            onLaunch = { name, block -> startUiFriendlyThread(name, block) },
            onLoadDir = { existingGeneratedPackageDir(entry.packageName) },
            onUi = { block -> runOnUiThread(block) },
            onMarkGenerated = { pkg -> generatedPackageNames = markPackageGenerated(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), generatedPackageNames, pkg) },
            onBuildSession = { pkg, dir -> buildGeneratedPackageSession(pkg, dir) },
            onCommitSession = { session, dir, e ->
                activeGenerationSession = session
                mainViewModel.updateLive { p -> p.copy(previewNormalLight = (PreviewSelections.default(PreviewChoice.Original)).normalLight.name, previewNormalDark = (PreviewSelections.default(PreviewChoice.Original)).normalDark.name, previewMonochromeLight = (PreviewSelections.default(PreviewChoice.Original)).monochromeLight.name, previewMonochromeDark = (PreviewSelections.default(PreviewChoice.Original)).monochromeDark.name) }
                previewChoiceMode = null
                previewPackageName = e.packageName
                previewDirPath = dir.absolutePath
                previewVersion += 1
            },
        )
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

    // 重构期间保留：委托到 system/RootInstaller.kt 显式参数版本，调用点零改动。
    internal fun existingGeneratedPackageDir(packageName: String): File =
        existingGeneratedPackageDir(
            packageName = packageName,
            previewDirPath = previewDirPath,
            previewPackageName = previewPackageName,
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
            gptModelId = gptModelId,
            gptBaseUrl = gptBaseUrl,
            gptApiKey = gptApiKey,
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
            isBusy = isBusy,
            isGeneratingRmbgCandidate = isGeneratingRmbgCandidate,
            isInstallingRmbgComponent = isInstallingRmbgComponent,
            closeRuntime = {
                runCatching { rmbgRuntime?.close() }
                rmbgRuntime = null
            },
            onClearUiState = { clearRmbgCandidateUiState() },
            onResult = { deleted ->
                lastRmbgInferenceReport = null
                rmbgComponentStatus = "${System.currentTimeMillis()}"
                rmbgInstallStage = ""
                rmbgInstallProgress = null
                rmbgComponentSaveStatus = ""
                statusText = if (deleted) "已清除 RMBG" else "没有已安装 RMBG"
            },
        )
    }

    // 重构期间保留：委托到 pipeline/RmbgManager.kt 显式参数版本，调用点零改动。
    internal fun installRmbgComponent(uri: Uri) {
        installRmbgComponent(
            uri = uri,
            filesDir = filesDir,
            isBusy = isBusy,
            isGeneratingRmbgCandidate = isGeneratingRmbgCandidate,
            isInstallingRmbgComponent = isInstallingRmbgComponent,
            openInput = { contentResolver.openInputStream(it) },
            getRuntime = { rmbgRuntime },
            setRuntime = { rmbgRuntime = it },
            setInstalling = { isInstallingRmbgComponent = it },
            setStage = { rmbgInstallStage = it },
            setProgress = { rmbgInstallProgress = it },
            setStatus = { statusText = it },
            setComponentStatus = { rmbgComponentStatus = it },
            setLastError = { lastRmbgCandidateError = it },
            runOnUi = { runOnUiThread(it) },
        )
    }

    // 重构期间保留：委托到 pipeline/RmbgManager.kt 显式参数版本，调用点零改动。
    internal fun installRmbgComponentFromUrl() {
        installRmbgComponentFromUrl(
            urlText = rmbgComponentUrl,
            filesDir = filesDir,
            cacheDir = cacheDir,
            isBusy = isBusy,
            isGeneratingRmbgCandidate = isGeneratingRmbgCandidate,
            isInstallingRmbgComponent = isInstallingRmbgComponent,
            isDebugBuild = isDebugBuild(),
            onSaveSettings = { saveRmbgSettings() },
            getRuntime = { rmbgRuntime },
            setRuntime = { rmbgRuntime = it },
            setInstalling = { isInstallingRmbgComponent = it },
            setStage = { rmbgInstallStage = it },
            setProgress = { rmbgInstallProgress = it },
            setStatus = { statusText = it },
            setComponentStatus = { rmbgComponentStatus = it },
            setComponentSaveStatus = { rmbgComponentSaveStatus = it },
            setLastError = { lastRmbgCandidateError = it },
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
                    statusText = status
                    rmbgInstallStage = stage
                    rmbgInstallProgress = progress
                }
            },
            onInstallStage = {
                runOnUiThread {
                    rmbgInstallStage = "安装模型"
                    rmbgInstallProgress = null
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
                statusText = status
                rmbgInstallStage = stage
                rmbgInstallProgress = progress
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
            session = activeGenerationSession,
            selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
            onChooseCustom = { chooseCustomImageForMode(mode, choice.customKind!!) },
            onGenerateGpt = { generateGptCandidateForMode(mode) },
            onStatusText = { statusText = it },
            onCommitSelections = { selections -> mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) } },
            onSaveUi = { saveUiState() },
            onWrite = { session, selections -> writeActivePreviewOutputs(session, selections, closeDialog = false) },
        )

    // 重构期间保留：委托到 ui/pages/home/HomeGenerationOps.kt 显式参数版本，调用点零改动。
    internal fun applyPreviewChoiceToAll(choice: PreviewChoice): Unit =
        homeApplyPreviewChoiceToAll(
            choice = choice,
            session = activeGenerationSession,
            batchPackageNames = multiSelectedPackageNames.toList().sorted(),
            onApplyToSelected = { c, pkgs -> applyPreviewChoiceToSelectedPackages(c, pkgs) },
            onGenerateGptAll = { generateGptCandidateForAll() },
            onGenerateRmbgAll = { generateRmbgCandidateForAll() },
            onStatusText = { statusText = it },
            candidateAvailable = { s, c -> candidateForChoice(s, c) != null },
            onCommitDefault = { selections -> mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) } },
            onClearChoiceMode = { previewChoiceMode = null },
            onSaveUi = { saveUiState() },
            onWriteClose = { session, selections -> writeActivePreviewOutputs(session, selections, closeDialog = true) },
        )

    // 重构期间保留：委托到 ui/pages/home/HomeGenerationOps.kt 显式参数版本，调用点零改动。
    internal fun applyPreviewChoiceToSelectedPackages(choice: PreviewChoice, packageNames: List<String>): Unit =
        homeApplyPreviewChoiceToSelectedPackages(
            choice = choice,
            packageNames = packageNames,
            gptBaseUrl = gptBaseUrl,
            gptApiKey = gptApiKey,
            hasRmbgComponent = findRmbgComponent() != null,
            isBusy = isBusy,
            isGeneratingGpt = isGeneratingGptCandidate,
            isGeneratingRmbg = isGeneratingRmbgCandidate,
            tryAcquireRmbg = { rmbgGenerationGate.compareAndSet(false, true) },
            onStatusText = { statusText = it },
            onBegin = { total ->
                isBusy = true
                previewChoiceMode = null
                batchApplyProgress = BatchApplyProgress(
                    title = "全部应用",
                    completed = 0,
                    total = total,
                    currentLabel = "准备处理 $total 个 APK",
                    failures = 0,
                )
            },
            selectedAtStart = selectedPackageName,
            apps = apps,
            onProgress = { completed, total, label, failures -> updateBatchApplyProgress(completed, total, label, failures) },
            onGeneratePackage = { app, c -> generatePackageForPreviewChoice(app, c) },
            onInstall = { outDir, pkg -> installWithRoot(outDir, pkg, RootWriteMode.All) },
            onFinishBatch = { successes, failures, selectedResult, atStart ->
                runOnUiThread {
                    if (successes.isNotEmpty()) {
                        generatedPackageNames = updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), generatedPackageNames + successes)
                        multiSelectedPackageNames = multiSelectedPackageNames - successes.toSet()
                    }
                    if (selectedResult != null && selectedPackageName == atStart) {
                        activeGenerationSession = selectedResult.session
                        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selectedResult.selections).normalLight.name, previewNormalDark = (selectedResult.selections).normalDark.name, previewMonochromeLight = (selectedResult.selections).monochromeLight.name, previewMonochromeDark = (selectedResult.selections).monochromeDark.name) }
                        previewChoiceMode = null
                        previewPackageName = selectedResult.session.packageName
                        previewDirPath = selectedResult.outDir.absolutePath
                        previewVersion += 1
                        saveUiState()
                    }
                    statusText = when {
                        failures.isEmpty() -> "全部应用完成: ${successes.size}/${packageNames.size}"
                        successes.isEmpty() -> "全部应用失败: ${failures.firstOrNull().orEmpty()}"
                        else -> "全部应用完成 ${successes.size} 个，失败 ${failures.size} 个: ${failures.firstOrNull().orEmpty()}"
                    }
                }
            },
            onReleaseRmbg = { rmbgGenerationGate.set(false) },
            onResetBusy = {
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
            },
            onLaunch = { name, block -> startUiFriendlyThread(name, block) },
        )

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

    // 重构期间保留：委托到 ui/pages/home/HomeGenerationOps.kt 显式参数版本，调用点零改动。
    internal fun refreshActivePreviewOutputs(
        rebuildLocalCandidates: Boolean,
        retargetFrom: PreviewChoice? = null,
    ): Unit =
        homeRefreshActivePreviewOutputs(
            currentSession = activeGenerationSession,
            rebuildLocalCandidates = rebuildLocalCandidates,
            retargetFrom = retargetFrom,
            app = activeGenerationSession?.let { s -> apps.firstOrNull { it.packageName == s.packageName } },
            currentSelections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
            scope = previewWorkerScope,
            getJob = { previewOutputJob },
            setJob = { previewOutputJob = it },
            incRevision = { ++previewOutputRevision },
            getRevision = { previewOutputRevision },
            setRefreshing = { isPreviewOutputRefreshing = it },
            rebuildDebounceMs = PREVIEW_REBUILD_DEBOUNCE_MS,
            outputDebounceMs = PREVIEW_OUTPUT_DEBOUNCE_MS,
            tuning = currentTuningParams(),
            onRebuild = { session, app, tuning -> rebuildLocalSession(session, app, packageManager, tuning) },
            defaultLocal = { auto -> defaultLocalPreviewChoice(auto) },
            normalize = { session, selections -> normalizePreviewSelections(session, selections) },
            onWrite = { session, selections -> writePackageOutputs(session, selections) },
            onCommit = { session, selections ->
                activeGenerationSession = session
                mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                previewVersion += 1
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
            setRefreshing = { isPreviewOutputRefreshing = it },
            outputDebounceMs = PREVIEW_OUTPUT_DEBOUNCE_MS,
            onWrite = { s, sel -> writePackageOutputs(s, sel) },
            onCommit = { s, sel, close ->
                activeGenerationSession = s
                mainViewModel.updateLive { p -> p.copy(previewNormalLight = (sel).normalLight.name, previewNormalDark = (sel).normalDark.name, previewMonochromeLight = (sel).monochromeLight.name, previewMonochromeDark = (sel).monochromeDark.name) }
                previewVersion += 1
                if (close) {
                    previewChoiceMode = null
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
        pickerPostStatus(message) { runOnUiThread { statusText = it } }
    }

    // 重构期间保留：委托到 ui/pages/picker/PickerCommon.kt 显式参数版本，调用点零改动。
    internal fun toastStatus(message: String) =
        pickerToastStatus(
            message = message,
            postOnUi = { text -> runOnUiThread { statusText = text } },
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
            sheetVisible = backupSheetVisible,
            inBackground = backupInBackground,
            progress = backupProgress,
            isBusy = isBusy,
        )
        cancelBackup(state, ::toastStatus)
        backupJob = state.backupJob
        backupDotJob = state.backupDotJob
        backupSheetVisible = state.sheetVisible
        backupInBackground = state.inBackground
        backupProgress = state.progress
        isBusy = state.isBusy
    }

    // 重构期间保留：委托到 system/ExportManager.kt 显式参数版本，调用点零改动。
    internal fun cancelSingleExport() {
        val state = SingleExportCancelState(
            singleExportJob = singleExportJob,
            sheetVisible = singleExportSheetVisible,
            progress = exportProgress,
        )
        cancelSingleExport(state, ::toastStatus)
        singleExportJob = state.singleExportJob
        singleExportSheetVisible = state.sheetVisible
        exportProgress = state.progress
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

