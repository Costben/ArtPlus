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

internal fun MainActivity.startDebugHttpServerIfNeeded() {
    val __actHooks = this
        if (!run {
    (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}) {
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
                override fun onStatus(message: String) = run {

            pickerPostStatus((message)) { runOnUiThread { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } } }
}
                override fun homeHtml(): String = debugHomeHtml()
                override fun currentParams(): JSONObject = currentDebugParamsOnMain()
                override fun applyParams(params: Map<String, String>): JSONObject = applyDebugParams(params)
                override fun inspectPackage(packageName: String, includeRmbg: Boolean): JSONObject =
                    run {
    debugInspectPackage(
                packageName = (packageName),
                includeRmbg = (includeRmbg),
                getAppInfo = { __a0: String -> run {
        pickerGetApplicationInfoCompat(
                    pm = packageManager,
                    packageName = __a0,
                )
    } },
                packageManager = packageManager,
                externalLabDir = getExternalFilesDir("ArtPlusLab"),
                filesDir = filesDir,
                tuning = run {
        paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
    },
                runOnMainSync = { __a0: () -> Unit -> run {

                if (Looper.myLooper() == Looper.getMainLooper()) {
                    __a0()
                    return@run
                }
                val latch = CountDownLatch(1)
                var failure: Throwable? = null
                runOnUiThread {
                    try {
                        __a0()
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
    } },
                setLastReport = { mainViewModel.updatePreviewSession { v -> v.copy(lastRmbgInferenceReport = (it)) } },
                setLastError = { mainViewModel.updatePreviewSession { v -> v.copy(lastRmbgCandidateError = (it)) } },
                buildRmbgDebug = { __a0: Bitmap -> run {

                val params = mainViewModel.params.value
                return@run buildRmbgDebugCandidate(
                    sourceIcon = __a0,
                    filesDir = filesDir,
                    rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
                    rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
                    rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
                    rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
                    lock = __actHooks,
                    getRuntime = { rmbgRuntime },
                    setRuntime = { rmbgRuntime = it },
                )
    } },
                describeFailure = { __a0: Throwable -> run {

                val root = run {

                    var current = (__a0)
                    while (current is InvocationTargetException && current.targetException != null) {
                        current = current.targetException
                    }
                    return@run current
        }
                val raw = root.message ?: root.javaClass.simpleName
                val lower = raw.lowercase()
                return@run when {
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
    } },
                renderForeground = { __a0: IconCandidate -> run {

                val params = mainViewModel.params.value
                return@run renderCandidateForeground(
                    candidate = __a0,
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
    } },
                monochromeFor = { __a0: IconCandidate, __a1: Boolean -> run {

                val params = mainViewModel.params.value
                return@run monochromeForCandidate(
                    candidate = __a0,
                    invertLuma = __a1,
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
                )
    } },
            )
}
                override fun startGeneration(
                    packageName: String,
                    useGpt: Boolean,
                    installWithRoot: Boolean,
                    debugMode: LocalSeparationMode,
                    rootWriteMode: RootWriteMode,
                ): Boolean = run {
    startDebugGeneration(
                packageName = (packageName),
                useGpt = (useGpt),
                installWithRoot = (installWithRoot),
                debugMode = (debugMode),
                rootWriteMode = (rootWriteMode),
                runOnMainSync = { __a0: () -> Unit -> run {

                if (Looper.myLooper() == Looper.getMainLooper()) {
                    __a0()
                    return@run
                }
                val latch = CountDownLatch(1)
                var failure: Throwable? = null
                runOnUiThread {
                    try {
                        __a0()
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
    } },
                isBusyGet = { mainViewModel.shell.value.isBusy },
                setBusy = { mainViewModel.updateShell { v -> v.copy(isBusy = (it)) } },
                setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                onStatus = { __a0: String -> run {

                pickerPostStatus(__a0) { runOnUiThread { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } } }
    } },
                getAppInfo = { __a0: String -> run {
        pickerGetApplicationInfoCompat(
                    pm = packageManager,
                    packageName = __a0,
                )
    } },
                packageManager = packageManager,
                prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                getGeneratedNames = { mainViewModel.picker.value.generatedPackageNames },
                setGeneratedNames = { mainViewModel.updatePicker { v -> v.copy(generatedPackageNames = (it)) } },
                generatePackage = { __a0: AppEntry, __a1: Boolean, __a2: LocalSeparationMode? -> run {

                val icon = __a0.applicationInfo.loadIcon(packageManager)
                return@run generateArtPlusPackage(
                    app = __a0,
                    useGpt = __a1,
                    localModeOverride = __a2,
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
}
                override fun isTokenValid(token: String?): Boolean = isDebugTokenValid(token)
            },
        ).also { it.start() }
    }

internal fun MainActivity.debugToken(): String {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(MainActivity.PREF_DEBUG_TOKEN, null)
            ?.takeIf { it.length >= 32 }
        if (existing != null) {
            return existing
        }
        val created = UUID.randomUUID().toString() + UUID.randomUUID().toString()
        prefs.edit().putString(MainActivity.PREF_DEBUG_TOKEN, created).apply()
        return created
    }

internal fun MainActivity.isDebugTokenValid(token: String?): Boolean =
        token != null && token == debugToken()

internal fun MainActivity.currentDebugParamsJson(): JSONObject =
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
            .put("rmbg_model_installed", run {
    findRmbgComponent(filesDir)
} != null)
            .put("rmbg_component_installed", run {
    findRmbgComponent(filesDir)
} != null)
            .put("rmbg_component_abi", run {
    findRmbgComponent(filesDir)
}?.abi ?: "")
            .put("rmbg_model_name", RMBG_MODEL_NAME)
            .put("rmbg_status", run {
    paramsRmbgInferenceStatusSummary(
                isGenerating = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
                candidateStatusText = mainViewModel.previewSession.value.rmbgCandidateStatusText,
                report = mainViewModel.previewSession.value.lastRmbgInferenceReport,
            )
})
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

internal fun MainActivity.intRangeJson(min: Int, max: Int): JSONObject =
        JSONObject().put("min", min).put("max", max)

internal fun MainActivity.liquidGlassParamLabelsJson(): JSONObject =
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

internal fun MainActivity.currentDebugParamsOnMain(): JSONObject {
        var snapshot: JSONObject? = null
        run {

            if (Looper.myLooper() == Looper.getMainLooper()) {
                ({
                snapshot = currentDebugParamsJson()
            })()
                return@run
            }
            val latch = CountDownLatch(1)
            var failure: Throwable? = null
            runOnUiThread {
                try {
                    ({
                snapshot = currentDebugParamsJson()
            })()
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
        return snapshot ?: error("debug params unavailable")
    }

internal fun MainActivity.applyDebugParams(params: Map<String, String>): JSONObject {
        var snapshot: JSONObject? = null
        run {

            if (Looper.myLooper() == Looper.getMainLooper()) {
                ({
                check(!mainViewModel.shell.value.isBusy) { "当前任务正在运行，不能修改参数" }
                // AI 凭据不走 TuningParams（预设不导出密钥），单独处理。
                params["gpt_base_url"]?.let { mainViewModel.updateGptRmbgSettings { v -> v.copy(gptBaseUrl = (it)) } }
                params["gpt_api_key"]?.let { mainViewModel.updateGptRmbgSettings { v -> v.copy(gptApiKey = (it)) } }
                val gptCredentialChanged =
                    params.containsKey("gpt_base_url") || params.containsKey("gpt_api_key")
                run {
        paramsApplyTuningParams(
                    params = (TuningParams.fromParamMap(params, run {
            paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
        })),
                    rebuildCandidates = (true),
                    persist = (true),
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
                if (gptCredentialChanged) {
                    run {
        paramsSaveGptSettings(
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    getParams = { mainViewModel.params.value },
                    getGptApiKey = { mainViewModel.gptRmbgSettings.value.gptApiKey },
                    getGptModelId = { mainViewModel.gptRmbgSettings.value.gptModelId },
                    getGptBaseUrl = { mainViewModel.gptRmbgSettings.value.gptBaseUrl },
                )
    }
                }
                snapshot = currentDebugParamsJson()
            })()
                return@run
            }
            val latch = CountDownLatch(1)
            var failure: Throwable? = null
            runOnUiThread {
                try {
                    ({
                check(!mainViewModel.shell.value.isBusy) { "当前任务正在运行，不能修改参数" }
                // AI 凭据不走 TuningParams（预设不导出密钥），单独处理。
                params["gpt_base_url"]?.let { mainViewModel.updateGptRmbgSettings { v -> v.copy(gptBaseUrl = (it)) } }
                params["gpt_api_key"]?.let { mainViewModel.updateGptRmbgSettings { v -> v.copy(gptApiKey = (it)) } }
                val gptCredentialChanged =
                    params.containsKey("gpt_base_url") || params.containsKey("gpt_api_key")
                run {
        paramsApplyTuningParams(
                    params = (TuningParams.fromParamMap(params, run {
            paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
        })),
                    rebuildCandidates = (true),
                    persist = (true),
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
                if (gptCredentialChanged) {
                    run {
        paramsSaveGptSettings(
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    getParams = { mainViewModel.params.value },
                    getGptApiKey = { mainViewModel.gptRmbgSettings.value.gptApiKey },
                    getGptModelId = { mainViewModel.gptRmbgSettings.value.gptModelId },
                    getGptBaseUrl = { mainViewModel.gptRmbgSettings.value.gptBaseUrl },
                )
    }
                }
                snapshot = currentDebugParamsJson()
            })()
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
        return snapshot ?: error("debug params unavailable")
    }

internal fun MainActivity.debugHomeHtml(): String = """
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

internal fun MainActivity.backupAllToExternal(isFromOnboarding: Boolean = false) {
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
            run {

            backupDotJob?.cancel()
            backupDotJob = null
}
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
                    run {

            backupDotJob?.cancel()
            backupDotJob = null
}
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

internal fun MainActivity.loadUiState() =
        pickerLoadUiState(
            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
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

internal fun MainActivity.saveUiState() =
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

internal fun MainActivity.loadApps(refreshGenerated: Boolean = false) =
        mainViewModel.requestAppLoad(
            refreshGenerated = refreshGenerated,
            pm = packageManager,
            iconCache = MainActivity.appIconCache,
            cacheSize = ICON_CACHE_SIZE,
            preloadCount = PRELOAD_ICON_COUNT,
            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
            markLoad = { didRequestAppLoad = true },
            onRefreshPermissions = { refreshPermissionState() },
            applyEntries = { loaded ->
                androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
                    apps.clear()
                    apps.addAll(loaded)
                }
            },
        )

internal fun MainActivity.refreshPermissionState() =
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

internal fun MainActivity.loadPresetState() =
        loadPresetState(
            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
            batchOutputModeKey = MainActivity.PREF_BATCH_OUTPUT_MODE,
            batchOutputModeFallbackName = BatchOutputMode.Root.name,
            gptRunCountKey = MainActivity.PREF_GPT_RUN_COUNT,
            rmbgRunCountKey = MainActivity.PREF_RMBG_RUN_COUNT,
            onRefreshPresets = { run {
    refreshPresets(
                store = presetStore,
                onBumpVersion = { mainViewModel.updatePresetUi { it -> it.copy(presetListVersion = it.presetListVersion + (1)) } },
                onRefreshed = { id, base ->
                    mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (id)) }
                    mainViewModel.updatePresetUi { it -> it.copy(activePresetBaseParams = (base)) }
                },
            )
} },
            onLoaded = { mode, gpt, rmbg ->
                mainViewModel.updatePresetUi { it -> it.copy(batchOutputMode = (mode)) }
                mainViewModel.updatePresetUi { it -> it.copy(gptRunCount = (gpt)) }
                mainViewModel.updatePresetUi { it -> it.copy(rmbgRunCount = (rmbg)) }
            },
        )

internal fun MainActivity.loadTuningParams(): Unit =
        paramsLoadTuningParams(
            onLoadLocal = { run {
    paramsLoadLocalSeparationSettings(
                prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                updateLive = mainViewModel::updateLive,
            )
} },
            onLoadImage = { run {
    paramsLoadImageSettings(
                prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
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
} },
            onLoadLiquid = { run {
    paramsLoadLiquidGlassSettings(
                prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
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
                onSave = { run {
        paramsSaveLiquidGlassSettings(
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    getParams = { mainViewModel.params.value },
                    getBottomBarEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarEnabled },
                    getBottomBarBlurEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarBlurEnabled },
                )
    } },
            )
} },
            getParams = { run {
    paramsCurrentTuningParams(getParams = { mainViewModel.params.value })
} },
            setDraftJsonParamsText = { draftJsonParamsText = it },
        )

internal fun MainActivity.initTuningHistory(): Unit =
        paramsInitTuningHistory(
            getParams = { mainViewModel.params.value },
            resetHistory = mainViewModel::resetHistory,
        )

internal fun MainActivity.loadGptSettings(): Unit =
        paramsLoadGptSettings(
            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
            updateLive = mainViewModel::updateLive,
            setGptModelId = { mainViewModel.updateGptRmbgSettings { v -> v.copy(gptModelId = (it)) } },
            setGptBaseUrl = { mainViewModel.updateGptRmbgSettings { v -> v.copy(gptBaseUrl = (it)) } },
            setGptApiKey = { mainViewModel.updateGptRmbgSettings { v -> v.copy(gptApiKey = (it)) } },
        )

internal fun MainActivity.loadRmbgSettings(): Unit =
        paramsLoadRmbgSettings(
            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
            setComponentUrl = { mainViewModel.updateGptRmbgSettings { v -> v.copy(rmbgComponentUrl = (it)) } },
        )

internal fun MainActivity.requestDeclaredPermissions() =
        pickerRequestDeclaredPermissions(
            needsQuery = pickerNeedsQueryPermission(packageManager, packageName),
            launcher = { permissionLauncher.launch(it) },
        )

internal fun MainActivity.requestSpecialPermissionsOnce() =
        pickerRequestSpecialPermissionsOnce(
            usageGranted = mainViewModel.picker.value.usageAccessGranted,
            prompted = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(PREF_USAGE_PERMISSION_PROMPTED, false),
            markPrompted = {
                getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().putBoolean(PREF_USAGE_PERMISSION_PROMPTED, true).apply()
            },
            postOnDecor = { window.decorView.post(it) },
            hasUsage = {  -> run {
    pickerHasUsageAccess(
                appOps = getSystemService(AppOpsManager::class.java),
                uid = Process.myUid(),
                packageName = packageName,
            )
} },
            openSettings = {  -> run {
    pickerOpenUsageAccessSettings(
                start = ::startActivity,
                onError = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            )
} },
        )

internal fun MainActivity.handleDebugGenerateIntent(intent: Intent?) =
        handleDebugGenerateIntent(intent, {  -> run {
    (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
} }, ::isDebugTokenValid, { __a0: String, __a1: Boolean, __a2: Boolean, __a3: LocalSeparationMode, __a4: RootWriteMode -> run {
    startDebugGeneration(
                packageName = __a0,
                useGpt = __a1,
                installWithRoot = __a2,
                debugMode = __a3,
                rootWriteMode = __a4,
                runOnMainSync = { __a0: () -> Unit -> run {

                if (Looper.myLooper() == Looper.getMainLooper()) {
                    __a0()
                    return@run
                }
                val latch = CountDownLatch(1)
                var failure: Throwable? = null
                runOnUiThread {
                    try {
                        __a0()
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
    } },
                isBusyGet = { mainViewModel.shell.value.isBusy },
                setBusy = { mainViewModel.updateShell { v -> v.copy(isBusy = (it)) } },
                setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                onStatus = { __a0: String -> run {

                pickerPostStatus(__a0) { runOnUiThread { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } } }
    } },
                getAppInfo = { __a0: String -> run {
        pickerGetApplicationInfoCompat(
                    pm = packageManager,
                    packageName = __a0,
                )
    } },
                packageManager = packageManager,
                prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                getGeneratedNames = { mainViewModel.picker.value.generatedPackageNames },
                setGeneratedNames = { mainViewModel.updatePicker { v -> v.copy(generatedPackageNames = (it)) } },
                generatePackage = { __a0: AppEntry, __a1: Boolean, __a2: LocalSeparationMode? -> run {

                val icon = __a0.applicationInfo.loadIcon(packageManager)
                return@run generateArtPlusPackage(
                    app = __a0,
                    useGpt = __a1,
                    localModeOverride = __a2,
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
} })

internal fun MainActivity.importCustomPreviewImage(mode: PreviewMode, kind: CustomImageKind, uri: Uri) {
        val session = mainViewModel.previewSession.value.activeGenerationSession
        if (session == null) {
            mainViewModel.updateShell { it -> it.copy(statusText = ("先生成一次预览后再导入自定义图片")) }
            return
        }
        mainViewModel.updateShell { it -> it.copy(statusText = ("导入${kind.label}: ${mode.label}")) }
        run {

            mainViewModel.launchUiFriendly(("ArtPlusCustomImageImport"), ({
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
                    run {

                val params = mainViewModel.params.value
                writePackageOutputs(
                    session = (updatedSession),
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
    }
                    if (false && mainViewModel.shell.value.outputTreeUri != null) {
                        exportToTree(contentResolver, mainViewModel.shell.value.outputTreeUri, updatedSession.outDir)
                    }
                    runOnUiThread {
                        mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (updatedSession)) }
                        mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                        mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                        mainViewModel.updateShell { it -> it.copy(statusText = ("已导入${kind.label}: ${mode.label}")) }
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
                } catch (error: Exception) {
                    run {

                pickerPostStatus(("${kind.label}导入失败: ${error.message ?: error.javaClass.simpleName}")) { runOnUiThread { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } } }
    }
                }
            }))
}
    }

internal fun MainActivity.importCustomWallpaper(uri: Uri) =
        pickerImportCustomWallpaper(
            uri = uri,
            isBusy = mainViewModel.shell.value.isBusy,
            onStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
            onLaunch = { name, block -> run {

            mainViewModel.launchUiFriendly((name), (block))
} },
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
            onError = { __a0: String -> run {

            pickerPostStatus(__a0) { runOnUiThread { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } } }
} },
        )

internal fun MainActivity.toastStatus(message: String) =
        pickerToastStatus(
            message = message,
            postOnUi = { text -> runOnUiThread { mainViewModel.updateShell { it -> it.copy(statusText = (text)) } } },
            showToast = { text ->
                runOnUiThread {
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                }
            },
        )
