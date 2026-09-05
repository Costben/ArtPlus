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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
internal fun MainActivity.AppPickerPage(
    pageBackground: Color,
    filteredApps: List<AppEntry>,
    scopeCount: Int,
    generatedCount: Int,
    ungeneratedCount: Int,
) {
    // 热修复：选择器渲染位订阅 picker/shell（StateFlow .value 裸读不触发重组，
    // 行高亮 AppRow selected/multi/generated/isBusy 全部 stale，点行写成功但不变色）。
    // 回调内事件时读 .value 仍合法，此处只修组合期渲染读。
    val pickerState by mainViewModel.picker.collectAsState()
    val shellState by mainViewModel.shell.collectAsState()
    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = "选择 APK",
                scrollBehavior = scrollBehavior,
                navigationIconPadding = 0.dp,
                navigationIcon = {
                    TitleBarIconButton(
                        icon = Lucide.ChevronLeft,
                        contentDescription = "返回",
                        enabled = !shellState.isBusy,
                        dimWhenDisabled = false,
                        onClick = { mainViewModel.updateShell { it -> it.copy(currentPage = (AppPage.Home)) } },
                    )
                },
            )
        },

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
                run {
    AppPickerControlsCard(
                filteredCount = ((filteredApps).size),
                totalCount = (scopeCount),
                generatedCount = (generatedCount),
                ungeneratedCount = (ungeneratedCount),
                multiCount = pickerState.multiSelectedPackageNames.size,
                isScanning = pickerState.isScanningGeneratedPackages,
                scanFailed = pickerState.generatedScanFailed,
                isBusy = shellState.isBusy,
                hasApps = apps.isNotEmpty(),
                showSystemApps = pickerState.showSystemApps,
                generatedFilter = pickerState.generatedFilter,
                queryText = pickerState.queryText,
                onRefreshGenerated = { run {
        mainViewModel.refreshGeneratedPackagesAsync(
                    entries = (apps.toList()),
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                )
    } },
                onReloadApps = { run {
        mainViewModel.requestAppLoad(
                    refreshGenerated = (false),
                    pm = packageManager,
                    iconCache = MainActivity.appIconCache,
                    cacheSize = ICON_CACHE_SIZE,
                    preloadCount = PRELOAD_ICON_COUNT,
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    markLoad = { didRequestAppLoad = true },
                    onRefreshPermissions = { run {
            pickerRefreshPermissionState(
                        checkQueryPermission = { pickerCheckQueryPermission(packageManager, packageName) },
                        hasUsage = {  -> run {
                pickerHasUsageAccess(
                            appOps = getSystemService(AppOpsManager::class.java),
                            uid = Process.myUid(),
                            packageName = packageName,
                        )
            } },
                        onResult = { queryGranted, usageGranted ->
                            mainViewModel.updatePicker { it -> it.copy(packageListPermissionGranted = (queryGranted)) }
                            mainViewModel.updatePicker { it -> it.copy(usageAccessGranted = (usageGranted)) }
                        },
                    )
        } },
                    applyEntries = { loaded ->
                        androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
                            apps.clear()
                            apps.addAll(loaded)
                        }
                    },
                )
    } },
                onToggleSystemApps = {
                    mainViewModel.updatePicker { it -> it.copy(showSystemApps = (!mainViewModel.picker.value.showSystemApps)) }
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
                onFilterSelected = {
                    mainViewModel.updatePicker { v -> v.copy(generatedFilter = (it)) }
                    mainViewModel.updatePicker { it -> it.copy(queryText = ("")) }
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
                onQueryChange = {
                    mainViewModel.updatePicker { v -> v.copy(queryText = (it)) }
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
                onClearQuery = {
                    mainViewModel.updatePicker { it -> it.copy(queryText = ("")) }
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
                multiSelectContent = { run {

                val filteredPackageNames = remember(((filteredApps))) { ((filteredApps)).map { it.packageName }.toSet() }
                AppMultiSelectActions(
                    selectedCount = pickerState.multiSelectedPackageNames.size,
                    hasFiltered = filteredPackageNames.isNotEmpty(),
                    allFilteredSelected = pickerAllFilteredSelected(filteredPackageNames, pickerState.multiSelectedPackageNames),
                    isBusy = shellState.isBusy,
                    onToggleFiltered = {
                        val allSelected = pickerAllFilteredSelected(filteredPackageNames, mainViewModel.picker.value.multiSelectedPackageNames)
                        mainViewModel.updatePicker { it -> it.copy(multiSelectedPackageNames = (if (allSelected) {
                            mainViewModel.picker.value.multiSelectedPackageNames - filteredPackageNames
                        } else {
                            mainViewModel.picker.value.multiSelectedPackageNames + filteredPackageNames
                        })) }
                    },
                    onClear = { mainViewModel.updatePicker { it -> it.copy(multiSelectedPackageNames = (emptySet())) } },
                    onAddGlass = { run {
            mainViewModel.addLiquidGlassToMultiSelectedGenerated(
                        onRequestConfirm = { title, message, confirmLabel, onConfirm ->
                            run {

                        mainViewModel.updateConfirm { it -> it.copy(pendingServiceConfirm = (ServiceConfirmRequest(
                            title = (title),
                            message = (message),
                            confirmLabel = (confirmLabel),
                            onConfirm = (onConfirm),
                        ))) }
            }
                        },
                        onExecute = { pkgs -> run {
                mainViewModel.executeAddLiquidGlassToMultiSelectedGenerated(
                            packageNames = (pkgs),
                            selectedAtStart = mainViewModel.picker.value.selectedPackageName,
                            resolvePackageDir = { __a0: String -> run {
                    existingGeneratedPackageDir(
                                packageName = __a0,
                                previewDirPath = mainViewModel.previewSession.value.previewDirPath,
                                previewPackageName = mainViewModel.previewSession.value.previewPackageName,
                                externalArtPlusDir = getExternalFilesDir("ArtPlus"),
                                filesDir = filesDir,
                                appUid = applicationInfo.uid,
                            )
                } },
                            applyGlass = { __a0: File -> run {

                            val params = mainViewModel.params.value
                            applyLiquidGlassToGeneratedPackage(
                                dir = __a0,
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
                } },
                            installGlass = ::installLiquidGlassFilesWithRoot,
                            buildSession = ::buildGeneratedPackageSession,
                            persistMany = { combined ->
                                updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), combined)
                            },
                            onSaveUiState = {  -> run {
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
                        )
            } },
                    )
        } },
                    onApplyPreset = { run {
            mainViewModel.applyCurrentPresetBatch(
                        store = presetStore,
                        onRequestConfirm = { title, message, confirmLabel, onConfirm ->
                            run {

                        mainViewModel.updateConfirm { it -> it.copy(pendingServiceConfirm = (ServiceConfirmRequest(
                            title = (title),
                            message = (message),
                            confirmLabel = (confirmLabel),
                            onConfirm = (onConfirm),
                        ))) }
            }
                        },
                        onExecutePreset = { p, pkgs -> run {
                mainViewModel.executeApplyPresetToSelectedApps(
                            preset = (p),
                            batchPackageNames = (pkgs),
                            beforeParams = run {
                    paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
                },
                            store = presetStore,
                            selectedAtStart = mainViewModel.picker.value.selectedPackageName,
                            apps = apps.toList(),
                            onApplyPresetParams = { merged -> run {
                    paramsApplyTuningParams(
                                params = (merged),
                                rebuildCandidates = (false),
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
                } },
                            generatePackage = { app -> run {

                            val icon = (app).applicationInfo.loadIcon(packageManager)
                            return@run generateArtPlusPackage(
                                app = (app),
                                useGpt = (false),
                                localModeOverride = (null),
                                params = mainViewModel.params.value,
                                externalArtPlusDir = getExternalFilesDir("ArtPlus"),
                                filesDir = filesDir,
                                icon = icon,
                                gptModelId = mainViewModel.gptRmbgSettings.value.gptModelId,
                                gptBaseUrl = mainViewModel.gptRmbgSettings.value.gptBaseUrl,
                                gptApiKey = mainViewModel.gptRmbgSettings.value.gptApiKey,
                                isDebug = run {
                        (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
                    },
                                onStatus = { __a0: String -> run {

                                pickerPostStatus(__a0) { runOnUiThread { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } } }
                    } },
                                defaultChoiceForMode = { __a0: LocalSeparationMode, __a1: PreviewChoice -> run {
                        when (__a0) {
                                    LocalSeparationMode.Original -> PreviewChoice.Original
                                    LocalSeparationMode.Plate -> PreviewChoice.Full
                                    LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                                    LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                                    LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                                    LocalSeparationMode.Auto -> __a1
                                    LocalSeparationMode.Full -> PreviewChoice.Full
                                }
                    } },
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
                            )
                } },
                            persistGenerated = { combined ->
                                updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), combined)
                            },
                            onSaveUiState = {  -> run {
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
                        )
            } },
                        onExecuteCurrent = { pkgs -> run {
                mainViewModel.executeApplyCurrentBatch(
                            batchPackageNames = (pkgs),
                            selectedAtStart = mainViewModel.picker.value.selectedPackageName,
                            apps = apps.toList(),
                            generatePackage = { app -> run {

                            val icon = (app).applicationInfo.loadIcon(packageManager)
                            return@run generateArtPlusPackage(
                                app = (app),
                                useGpt = (false),
                                localModeOverride = (null),
                                params = mainViewModel.params.value,
                                externalArtPlusDir = getExternalFilesDir("ArtPlus"),
                                filesDir = filesDir,
                                icon = icon,
                                gptModelId = mainViewModel.gptRmbgSettings.value.gptModelId,
                                gptBaseUrl = mainViewModel.gptRmbgSettings.value.gptBaseUrl,
                                gptApiKey = mainViewModel.gptRmbgSettings.value.gptApiKey,
                                isDebug = run {
                        (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
                    },
                                onStatus = { __a0: String -> run {

                                pickerPostStatus(__a0) { runOnUiThread { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } } }
                    } },
                                defaultChoiceForMode = { __a0: LocalSeparationMode, __a1: PreviewChoice -> run {
                        when (__a0) {
                                    LocalSeparationMode.Original -> PreviewChoice.Original
                                    LocalSeparationMode.Plate -> PreviewChoice.Full
                                    LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                                    LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                                    LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                                    LocalSeparationMode.Auto -> __a1
                                    LocalSeparationMode.Full -> PreviewChoice.Full
                                }
                    } },
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
                            )
                } },
                            persistGenerated = { combined ->
                                updateGeneratedPackageCache(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), combined)
                            },
                            onSaveUiState = {  -> run {
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
                        )
            } },
                    )
        } },
                )
    } },
            )
}
            }
            if (filteredApps.isEmpty()) {
                item {
                    run {

            // 热修复：pickerState/shellState 已提升至 AppPickerPage 顶层，此处复用（删重复订阅）。
            // Slice 3.1: Activity侧collect读VM单源；写经薄wrapper（重构期间保留）。
            EmptyAppListCard(
                queryText = pickerState.queryText,
                showSystemApps = pickerState.showSystemApps,
                hasHiddenSystemApps = apps.any { AppVisibility.isSystemAppFlags(it.applicationInfo.flags) && it.packageName != packageName },
                isBusy = shellState.isBusy,
                onShowSystemApps = {
                    mainViewModel.updatePicker { it -> it.copy(showSystemApps = (true)) }
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
                onRefresh = { run {
        mainViewModel.requestAppLoad(
                    refreshGenerated = (false),
                    pm = packageManager,
                    iconCache = MainActivity.appIconCache,
                    cacheSize = ICON_CACHE_SIZE,
                    preloadCount = PRELOAD_ICON_COUNT,
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    markLoad = { didRequestAppLoad = true },
                    onRefreshPermissions = { run {
            pickerRefreshPermissionState(
                        checkQueryPermission = { pickerCheckQueryPermission(packageManager, packageName) },
                        hasUsage = {  -> run {
                pickerHasUsageAccess(
                            appOps = getSystemService(AppOpsManager::class.java),
                            uid = Process.myUid(),
                            packageName = packageName,
                        )
            } },
                        onResult = { queryGranted, usageGranted ->
                            mainViewModel.updatePicker { it -> it.copy(packageListPermissionGranted = (queryGranted)) }
                            mainViewModel.updatePicker { it -> it.copy(usageAccessGranted = (usageGranted)) }
                        },
                    )
        } },
                    applyEntries = { loaded ->
                        androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
                            apps.clear()
                            apps.addAll(loaded)
                        }
                    },
                )
    } },
            )
}
                }
            } else {
                items(
                    items = filteredApps,
                    key = { it.packageName },
                    contentType = { "app" },
                ) { entry ->
                    run {
    AppRow(
                entry = (entry),
                selected = (entry.packageName == pickerState.selectedPackageName),
                multiSelected = (entry.packageName in pickerState.multiSelectedPackageNames),
                generated = (entry.packageName in pickerState.generatedPackageNames),
                isBusy = shellState.isBusy,
                onClick = ({
                                run {

                val revision = ++generatedPreviewRestoreRevision
                val localDir = run {
            artPlusPackageDir(
                        packageName = ((entry).packageName),
                        externalArtPlusDir = getExternalFilesDir("ArtPlus"),
                        filesDir = filesDir,
                    )
        }
                val known = (entry).packageName in mainViewModel.picker.value.generatedPackageNames || hasGeneratedPackageBaseAssets(localDir)
                homeSelectAppAndRestore(
                    entry = (entry),
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
                    onClearRmbg = { run {

                    if (mainViewModel.previewSession.value.isGeneratingRmbgCandidate) {
                        return@run
                    }
                    mainViewModel.updatePreviewSession { it -> it.copy(lastRmbgCandidateError = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidatePackageName = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateMode = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateStatusText = ("")) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailurePackageName = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailureMode = (null)) }
        } },
                    onLaunch = { name, block -> run {

                    mainViewModel.launchUiFriendly((name), (block))
        } },
                    onLoadDir = { run {
            existingGeneratedPackageDir(
                        packageName = ((entry).packageName),
                        previewDirPath = mainViewModel.previewSession.value.previewDirPath,
                        previewPackageName = mainViewModel.previewSession.value.previewPackageName,
                        externalArtPlusDir = getExternalFilesDir("ArtPlus"),
                        filesDir = filesDir,
                        appUid = applicationInfo.uid,
                    )
        } },
                    onUi = { block -> runOnUiThread(block) },
                    onMarkGenerated = { pkg -> mainViewModel.updatePicker { it -> it.copy(generatedPackageNames = (markPackageGenerated(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), mainViewModel.picker.value.generatedPackageNames, pkg))) } },
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
                                mainViewModel.updateShell { it -> it.copy(currentPage = (AppPage.Home)) }
                            }),
                onToggleMultiSelect = ({ run {
        mainViewModel.toggleMultiSelectedPackage(packageName = (entry.packageName), current = mainViewModel.picker.value.multiSelectedPackageNames)
    } }),
                icon = {
                    run {
        AppIcon(
                    entry = ((entry)),
                    size = (48.dp),
                    getCached = { key -> getCachedAppIcon(MainActivity.appIconCache, key) },
                    loadIcon = { run {
            mainViewModel.loadCachedAppIconOp(
                        entry = (((entry))),
                        iconCache = MainActivity.appIconCache,
                        pm = packageManager,
                        cacheSize = ICON_CACHE_SIZE,
                    )
        } },
                )
    }
                },
            )
}
                }
            }
        }
    }
}
