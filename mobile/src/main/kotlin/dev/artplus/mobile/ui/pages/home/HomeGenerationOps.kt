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
/**
 * Slice 2.2：主页生成调度（原 MainActivity 残留本体原样搬迁）。
 * 只做纯移动：线程名、状态文案、读写顺序与分支一律不变。
 * Activity 状态经参数/回调注入；MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动。
 * writeSelectedWithRoot 内嵌 executeWrite 一并搬迁为 executeHomeWrite；名单内联 helper 扫描结论：
 * executeWrite 为唯一内联 helper，已随迁；其余同簇纯 UI/调度无遗漏。
 */


// ---------- generateSelected ----------

internal fun homeGenerateSelected(
    entry: AppEntry?,
    installWithRoot: Boolean,
    useGpt: Boolean,
    rootWriteMode: RootWriteMode,
    confirmed: Boolean,
    gptApiKey: String,
    gptBaseUrl: String,
    isBusy: Boolean,
    gptRunCount: Int,
    onStatusText: (String) -> Unit,
    onRequestConfirm: (title: String, message: String, confirmLabel: String, onConfirm: () -> Unit) -> Unit,
    onBeginBusy: (useGpt: Boolean) -> Unit,
    onLaunch: (name: String, block: () -> Unit) -> Unit,
    onGenerate: (AppEntry, Boolean) -> GenerationResult,
    onPostGenerate: (GenerationResult, AppEntry) -> Unit,
    onInstall: (outDir: java.io.File, packageName: String, mode: RootWriteMode) -> Unit,
    onMarkGenerated: (packageName: String) -> Unit,
    onToast: (String) -> Unit,
    onStatus: (String) -> Unit,
    onFinish: (useGpt: Boolean) -> Unit,
    onConfirmedRetry: (Boolean, Boolean, RootWriteMode) -> Unit,
) {
    if (entry == null) {
        onStatusText("先选择一个应用")
        return
    }
    if (useGpt && gptApiKey.trim().isEmpty()) {
        onStatusText("请填写AI提供商信息")
        return
    }
    if (useGpt && gptBaseUrl.trim().isEmpty()) {
        onStatusText("请填写AI提供商信息")
        return
    }
    if (isBusy) {
        onStatusText("当前有任务在运行")
        return
    }
    if (useGpt && !confirmed) {
        onRequestConfirm(
            "使用 AI 生成",
            "将调用云端图像接口（已累计 $gptRunCount 次）生成图标包。确认继续？",
            "继续",
        ) {
            onConfirmedRetry(installWithRoot, true, rootWriteMode)
        }
        return
    }

    onBeginBusy(useGpt)
    onStatusText(
        if (useGpt) "AI处理中: ${entry.packageName}"
        else "本地处理中(自动): ${entry.packageName}",
    )
    onLaunch(if (useGpt) "ArtPlusGptGenerate" else "ArtPlusLocalGenerate") {
        try {
            val result = onGenerate(entry, useGpt)
            onPostGenerate(result, entry)
            if (installWithRoot) {
                onInstall(result.outDir, entry.packageName, rootWriteMode)
                onMarkGenerated(entry.packageName)
                if (useGpt) {
                    onToast("已生成AI版并${rootWriteMode.label}写入，未刷新，请手动点首页左上角刷新图标: ${entry.packageName}")
                } else {
                    onStatus("已生成本地版并${rootWriteMode.label}写入，未刷新，请手动点首页左上角刷新图标: ${entry.packageName}")
                }
            } else {
                if (useGpt) {
                    onToast("已生成AI版: ${result.outDir.absolutePath}")
                } else {
                    onStatus("已生成本地版: ${result.outDir.absolutePath}")
                }
            }
        } catch (error: Exception) {
            val msg = "失败: ${error.message ?: error.javaClass.simpleName}"
            if (useGpt) {
                onToast(msg)
            } else {
                onStatus(msg)
            }
        } finally {
            onFinish(useGpt)
        }
    }
}

// ---------- writeSelectedWithRoot (+ executeWrite) ----------

private fun executeHomeWrite(
    entry: AppEntry,
    rootWriteMode: RootWriteMode,
    session: GenerationSession?,
    selections: PreviewSelections,
    onGenerateFallback: () -> Unit,
    onBeginBusy: (String) -> Unit,
    onLaunch: (String, () -> Unit) -> Unit,
    onWrite: (GenerationSession, PreviewSelections) -> Unit,
    onInstall: (java.io.File, String, RootWriteMode) -> Unit,
    onPostWrite: (GenerationSession, PreviewSelections, AppEntry) -> Unit,
    onToast: (String) -> Unit,
    onFinish: () -> Unit,
) {
    if (session == null) {
        onGenerateFallback()
        return
    }
    onBeginBusy("按当前预览写入${rootWriteMode.label}: ${entry.packageName}")
    onLaunch("ArtPlusPreviewRootWrite") {
        try {
            onWrite(session, selections)
            onInstall(session.outDir, entry.packageName, rootWriteMode)
            onPostWrite(session, selections, entry)
            onToast("已按当前预览${rootWriteMode.label}写入，未刷新，请手动点首页左上角刷新图标: ${entry.packageName}")
        } catch (error: Exception) {
            onToast("写入失败: ${error.message ?: error.javaClass.simpleName}")
        } finally {
            onFinish()
        }
    }
}

internal fun homeWriteSelectedWithRoot(
    entry: AppEntry?,
    rootWriteMode: RootWriteMode,
    isBusy: Boolean,
    activeSession: GenerationSession?,
    selections: PreviewSelections,
    autoConfirmRootWrite: Boolean,
    targetPath: String?,
    onStatusText: (String) -> Unit,
    onGenerateFallback: () -> Unit,
    onBeginBusy: (String) -> Unit,
    onLaunch: (String, () -> Unit) -> Unit,
    onWrite: (GenerationSession, PreviewSelections) -> Unit,
    onInstall: (java.io.File, String, RootWriteMode) -> Unit,
    onPostWrite: (GenerationSession, PreviewSelections, AppEntry) -> Unit,
    onToast: (String) -> Unit,
    onFinish: () -> Unit,
    onRequestConfirm: (packageName: String, targetPath: String, mode: RootWriteMode, onConfirm: () -> Unit) -> Unit,
) {
    if (entry == null) {
        onStatusText("先选择一个应用")
        return
    }
    if (isBusy) {
        return
    }
    val run: () -> Unit = {
        executeHomeWrite(
            entry = entry,
            rootWriteMode = rootWriteMode,
            session = activeSession?.takeIf { it.packageName == entry.packageName },
            selections = selections,
            onGenerateFallback = onGenerateFallback,
            onBeginBusy = onBeginBusy,
            onLaunch = onLaunch,
            onWrite = onWrite,
            onInstall = onInstall,
            onPostWrite = onPostWrite,
            onToast = onToast,
            onFinish = onFinish,
        )
    }
    if (autoConfirmRootWrite) {
        run()
    } else {
        onRequestConfirm(entry.packageName, targetPath ?: "", rootWriteMode, run)
    }
}

// ---------- selectAppAndRestoreGeneratedPreview ----------

internal fun homeSelectAppAndRestore(
    entry: AppEntry,
    revision: Int,
    isBusy: Boolean,
    knownGenerated: Boolean,
    getSelected: () -> String?,
    getRevision: () -> Int,
    onResetSelection: (String) -> Unit,
    onStatusText: (String) -> Unit,
    onSaveUi: () -> Unit,
    onClearRmbg: () -> Unit,
    onLaunch: (String, () -> Unit) -> Unit,
    onLoadDir: () -> java.io.File,
    onUi: ((() -> Unit) -> Unit),
    onMarkGenerated: (String) -> Unit,
    onBuildSession: (String, java.io.File) -> GenerationSession,
    onCommitSession: (GenerationSession, java.io.File, AppEntry) -> Unit,
) {
    onResetSelection(entry.packageName)
    onClearRmbg()
    onStatusText(
        if (knownGenerated) "正在读取现有图标包: ${entry.label} (${entry.packageName})"
        else "已选择: ${entry.label} (${entry.packageName})",
    )
    onSaveUi()
    if (isBusy) {
        return
    }
    val packageName = entry.packageName
    onLaunch("ArtPlusRestoreGeneratedPreview") {
        val result = runCatching { onLoadDir() }
        onUi {
            if (revision != getRevision() || getSelected() != packageName) {
                return@onUi
            }
            result
                .onSuccess { packageDir ->
                    onMarkGenerated(packageName)
                    val session = onBuildSession(packageName, packageDir)
                    onCommitSession(session, packageDir, entry)
                    onStatusText("已读取现有图标包: ${entry.label} ($packageName)")
                    onSaveUi()
                }
                .onFailure { error ->
                    onStatusText("未读取到现有图标包: ${error.message ?: error.javaClass.simpleName}")
                }
        }
    }
}

// ---------- applyPreviewChoice ----------

internal fun homeApplyPreviewChoice(
    mode: PreviewMode,
    choice: PreviewChoice,
    session: GenerationSession?,
    selections: PreviewSelections,
    onChooseCustom: () -> Unit,
    onGenerateGpt: () -> Unit,
    onStatusText: (String) -> Unit,
    onCommitSelections: (PreviewSelections) -> Unit,
    onSaveUi: () -> Unit,
    onWrite: (GenerationSession, PreviewSelections) -> Unit,
) {
    if (session == null) return
    val customKind = choice.customKind
    if (customKind != null) {
        onChooseCustom()
        return
    }
    if (choice == PreviewChoice.Gpt && session.candidates[PreviewChoice.Gpt] == null) {
        onGenerateGpt()
        return
    }
    if (choice == PreviewChoice.GptComposedBackground && session.candidates[PreviewChoice.Gpt] == null) {
        onStatusText("先生成 AI 候选，再使用拼合背景")
        return
    }
    if (choice == PreviewChoice.RmbgComposedBackground && session.candidates[PreviewChoice.Rmbg] == null) {
        onStatusText("先生成 RMBG 候选，再使用拼合背景")
        return
    }
    val next = selections.withChoice(mode, choice)
    onCommitSelections(next)
    onSaveUi()
    onWrite(session, next)
}

// ---------- applyPreviewChoiceToAll ----------

internal fun homeApplyPreviewChoiceToAll(
    choice: PreviewChoice,
    session: GenerationSession?,
    batchPackageNames: List<String>,
    onApplyToSelected: (PreviewChoice, List<String>) -> Unit,
    onGenerateGptAll: () -> Unit,
    onGenerateRmbgAll: () -> Unit,
    onStatusText: (String) -> Unit,
    candidateAvailable: (GenerationSession, PreviewChoice) -> Boolean,
    onCommitDefault: (PreviewSelections) -> Unit,
    onClearChoiceMode: () -> Unit,
    onSaveUi: () -> Unit,
    onWriteClose: (GenerationSession, PreviewSelections) -> Unit,
) {
    if (session == null) return
    if (batchPackageNames.isNotEmpty()) {
        onApplyToSelected(choice, batchPackageNames)
        return
    }
    if (choice == PreviewChoice.Gpt && session.candidates[PreviewChoice.Gpt] == null) {
        onGenerateGptAll()
        return
    }
    if (choice == PreviewChoice.Rmbg && session.candidates[PreviewChoice.Rmbg] == null) {
        onGenerateRmbgAll()
        return
    }
    if (choice == PreviewChoice.GptComposedBackground && session.candidates[PreviewChoice.Gpt] == null) {
        onStatusText("先生成 AI 候选，再使用拼合背景")
        return
    }
    if (choice == PreviewChoice.RmbgComposedBackground && session.candidates[PreviewChoice.Rmbg] == null) {
        onStatusText("先生成 RMBG 候选，再使用拼合背景")
        return
    }
    if (choice.isCustom) {
        onStatusText("自定义图片需要逐个槽位上传")
        return
    }
    if (!candidateAvailable(session, choice)) {
        onStatusText("${choice.label} 当前不可用")
        return
    }
    val selections = PreviewSelections.default(choice)
    onCommitDefault(selections)
    onClearChoiceMode()
    onSaveUi()
    onWriteClose(session, selections)
}

// ---------- applyPreviewChoiceToSelectedPackages ----------

internal fun homeApplyPreviewChoiceToSelectedPackages(
    choice: PreviewChoice,
    packageNames: List<String>,
    gptBaseUrl: String,
    gptApiKey: String,
    hasRmbgComponent: Boolean,
    isBusy: Boolean,
    isGeneratingGpt: Boolean,
    isGeneratingRmbg: Boolean,
    tryAcquireRmbg: () -> Boolean,
    onStatusText: (String) -> Unit,
    onBegin: (Int) -> Unit,
    selectedAtStart: String?,
    apps: List<AppEntry>,
    onProgress: (completed: Int, total: Int, label: String, failures: Int) -> Unit,
    onGeneratePackage: (AppEntry, PreviewChoice) -> GenerationResult,
    onInstall: (java.io.File, String) -> Unit,
    onFinishBatch: (successes: List<String>, failures: List<String>, selectedResult: GenerationResult?, selectedAtStart: String?) -> Unit,
    onReleaseRmbg: () -> Unit,
    onResetBusy: () -> Unit,
    onLaunch: (String, () -> Unit) -> Unit,
) {
    if (choice.isCustom) {
        onStatusText("自定义图片需要逐个槽位上传")
        return
    }
    if (choice == PreviewChoice.Gpt && (gptBaseUrl.trim().isEmpty() || gptApiKey.trim().isEmpty())) {
        onStatusText("请填写AI提供商信息")
        return
    }
    if ((choice == PreviewChoice.Rmbg || choice == PreviewChoice.RmbgComposedBackground) && !hasRmbgComponent) {
        onStatusText("未安装 RMBG 组件 ZIP")
        return
    }
    if (isBusy || isGeneratingGpt || isGeneratingRmbg) {
        onStatusText("当前有任务在运行，请等待")
        return
    }
    if ((choice == PreviewChoice.Rmbg || choice == PreviewChoice.RmbgComposedBackground) && !tryAcquireRmbg()) {
        onStatusText("RMBG正在运行，请等待")
        return
    }

    onBegin(packageNames.size)
    onStatusText("全部应用处理中: 0/${packageNames.size}")
    onLaunch("ArtPlusBatchApplyRule") {
        val successes = mutableListOf<String>()
        val failures = mutableListOf<String>()
        var selectedResult: GenerationResult? = null
        try {
            packageNames.forEachIndexed { index, packageName ->
                val app = apps.firstOrNull { it.packageName == packageName }
                if (app == null) {
                    failures += "$packageName: 应用不存在"
                    onProgress(index + 1, packageNames.size, "跳过: $packageName", failures.size)
                    return@forEachIndexed
                }
                onProgress(index, packageNames.size, "处理中: ${app.label} (${packageName})", failures.size)
                try {
                    val result = onGeneratePackage(app, choice)
                    onInstall(result.outDir, packageName)
                    successes += packageName
                    if (packageName == selectedAtStart) {
                        selectedResult = result
                    }
                } catch (error: Throwable) {
                    failures += "$packageName: ${error.message ?: error.javaClass.simpleName}"
                }
                onProgress(index + 1, packageNames.size, "已完成: ${app.label} (${packageName})", failures.size)
            }
            onFinishBatch(successes, failures, selectedResult, selectedAtStart)
        } finally {
            if (choice == PreviewChoice.Rmbg || choice == PreviewChoice.RmbgComposedBackground) {
                onReleaseRmbg()
            }
            onResetBusy()
        }
    }
}

// ---------- generatePackageForPreviewChoice ----------

internal fun homeGeneratePackageForPreviewChoice(
    app: AppEntry,
    choice: PreviewChoice,
    onGenerate: (AppEntry, Boolean) -> GenerationResult,
    onBuildRmbg: (android.graphics.Bitmap) -> IconCandidate,
    onResize: (android.graphics.Bitmap, Int, Int) -> android.graphics.Bitmap,
    onWrite: (GenerationSession, PreviewSelections) -> Unit,
    defaultLocal: (PreviewChoice) -> PreviewChoice,
    candidateAvailable: (GenerationSession, PreviewChoice) -> IconCandidate?,
) : GenerationResult {
    val useGpt = choice == PreviewChoice.Gpt || choice == PreviewChoice.GptComposedBackground
    val result = onGenerate(app, useGpt)
    var session = result.session
    if (choice == PreviewChoice.Rmbg || choice == PreviewChoice.RmbgComposedBackground) {
        val source = onResize(session.sourceIcon, SIZE_1X1, SIZE_1X1)
        val candidate = onBuildRmbg(source)
        session = session.copy(
            candidates = session.candidates + (PreviewChoice.Rmbg to candidate),
        )
    }
    val effectiveChoice = when {
        choice == PreviewChoice.GptComposedBackground && candidateAvailable(session, PreviewChoice.GptComposedBackground) == null ->
            PreviewChoice.Gpt
        choice == PreviewChoice.RmbgComposedBackground && candidateAvailable(session, PreviewChoice.RmbgComposedBackground) == null ->
            PreviewChoice.Rmbg
        candidateAvailable(session, choice) != null -> choice
        else -> defaultLocal(session.autoLocalChoice)
    }
    val selections = PreviewSelections.default(effectiveChoice)
    val finalSession = session.copy(outDir = result.outDir)
    onWrite(finalSession, selections)
    return GenerationResult(
        outDir = result.outDir,
        session = finalSession,
        selections = selections,
    )
}

// ---------- refreshActivePreviewOutputs ----------

internal fun homeRefreshActivePreviewOutputs(
    currentSession: GenerationSession?,
    rebuildLocalCandidates: Boolean,
    retargetFrom: PreviewChoice?,
    app: AppEntry?,
    currentSelections: PreviewSelections,
    scope: CoroutineScope,
    getJob: () -> Job?,
    setJob: (Job?) -> Unit,
    incRevision: () -> Int,
    getRevision: () -> Int,
    setRefreshing: (Boolean) -> Unit,
    rebuildDebounceMs: Long,
    outputDebounceMs: Long,
    tuning: TuningParams,
    onRebuild: (GenerationSession, AppEntry, TuningParams) -> GenerationSession,
    defaultLocal: (PreviewChoice) -> PreviewChoice,
    normalize: (GenerationSession, PreviewSelections) -> PreviewSelections,
    onWrite: (GenerationSession, PreviewSelections) -> Unit,
    onCommit: (GenerationSession, PreviewSelections) -> Unit,
    onStatus: (String) -> Unit,
) {
    if (currentSession == null) {
        getJob()?.cancel()
        setRefreshing(false)
        return
    }
    val packageName = currentSession.packageName
    val requestRevision = incRevision()
    getJob()?.cancel()
    setRefreshing(true)
    setJob(
        scope.launch {
            try {
                delay(if (rebuildLocalCandidates) rebuildDebounceMs else outputDebounceMs)
                val updatedSession = when {
                    rebuildLocalCandidates && app != null && currentSession.canRebuildLocalCandidates ->
                        onRebuild(currentSession, app, tuning)
                    else -> currentSession
                }
                val previousDefault = retargetFrom
                    ?: if (rebuildLocalCandidates && currentSession.canRebuildLocalCandidates) {
                        defaultLocal(currentSession.autoLocalChoice)
                    } else {
                        null
                    }
                val nextDefault = defaultLocal(updatedSession.autoLocalChoice)
                val retargetedSelections = when {
                    previousDefault == null -> currentSelections
                    else -> currentSelections.retarget(previousDefault, nextDefault)
                }
                val selections = normalize(updatedSession, retargetedSelections)
                onWrite(updatedSession, selections)
                withContext(Dispatchers.Main) {
                    if (requestRevision == getRevision()) {
                        onCommit(updatedSession, selections)
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                onStatus("预览刷新失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                withContext(Dispatchers.Main) {
                    if (requestRevision == getRevision()) {
                        setRefreshing(false)
                    }
                }
            }
        },
    )
}

// ---------- writeActivePreviewOutputs ----------

internal fun homeWriteActivePreviewOutputs(
    session: GenerationSession,
    selections: PreviewSelections,
    closeDialog: Boolean,
    scope: CoroutineScope,
    getJob: () -> Job?,
    setJob: (Job?) -> Unit,
    incRevision: () -> Int,
    getRevision: () -> Int,
    setRefreshing: (Boolean) -> Unit,
    outputDebounceMs: Long,
    onWrite: (GenerationSession, PreviewSelections) -> Unit,
    onCommit: (GenerationSession, PreviewSelections, Boolean) -> Unit,
    onStatus: (String) -> Unit,
) {
    val requestRevision = incRevision()
    getJob()?.cancel()
    setRefreshing(true)
    setJob(
        scope.launch {
            try {
                delay(outputDebounceMs)
                onWrite(session, selections)
                withContext(Dispatchers.Main) {
                    if (requestRevision == getRevision()) {
                        onCommit(session, selections, closeDialog)
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                onStatus("预览刷新失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                withContext(Dispatchers.Main) {
                    if (requestRevision == getRevision()) {
                        setRefreshing(false)
                    }
                }
            }
        },
    )
}
