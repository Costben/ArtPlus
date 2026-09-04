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
 * Slice 2.2：预览来源抽屉（原 MainActivity 残留本体原样搬迁）。
 * Composable 只收 state + onEvent；Activity 状态经参数/回调注入，行为与 UI 100% 等价。
 * 预览资产函数（candidateForChoice/customCandidateForPreview/effectiveChoiceForPreviewRow）同包直接用。
 */

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
    isBusy: Boolean,
    isGeneratingGptCandidate: Boolean,
    isGeneratingRmbgCandidate: Boolean,
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
            .heightIn(min = 78.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable(
                enabled = !isBusy && !isGeneratingGptCandidate && !isGeneratingRmbgCandidate,
                onClick = onToggle,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
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
                    ?: "字标保全 / 底座 / 二层",
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
internal fun CandidateIconPreview(
    candidate: IconCandidate,
    mode: PreviewMode,
    tuningState: TuningParams,
    cornerRadiusDp: Int,
    materialColorProvider: (String, Color) -> Color,
    loadAssets: suspend (IconCandidate, PreviewMode?) -> PreviewAssets?,
) {
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
            assets = loadAssets(candidate, mode)
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
        GeneratedIconPreview(
            assets = readyAssets,
            mode = mode,
            cornerRadiusDp = cornerRadiusDp,
            materialColorProvider = materialColorProvider,
        )
    }
}

@Composable
internal fun PreviewChoiceRow(
    mode: PreviewMode,
    choice: PreviewChoice,
    session: GenerationSession,
    tuningState: TuningParams,
    isBusy: Boolean,
    isGeneratingGptCandidate: Boolean,
    isGeneratingRmbgCandidate: Boolean,
    rmbgCandidatePackageName: String?,
    rmbgCandidateMode: PreviewMode?,
    rmbgCandidateFailurePackageName: String?,
    rmbgCandidateFailureMode: PreviewMode?,
    lastRmbgCandidateError: String?,
    rmbgCandidateStatusText: String,
    gptBaseUrl: String,
    gptApiKey: String,
    hasRmbgComponent: Boolean,
    cornerRadiusDp: Int,
    materialColorProvider: (String, Color) -> Color,
    loadCandidateAssets: suspend (IconCandidate, PreviewMode?) -> PreviewAssets?,
    onGenerateGpt: (PreviewMode) -> Unit,
    onGenerateRmbg: (PreviewMode) -> Unit,
    onChooseCustom: (PreviewMode, CustomImageKind) -> Unit,
    onApply: (PreviewMode, PreviewChoice) -> Unit,
    onApplyAll: (PreviewChoice) -> Unit,
) {
    val currentChoice = PreviewSelections.fromNames(tuningState.previewNormalLight, tuningState.previewNormalDark, tuningState.previewMonochromeLight, tuningState.previewMonochromeDark).choiceFor(mode)
    val effectiveChoice = effectiveChoiceForPreviewRow(
        mode = mode,
        rowChoice = choice,
        session = session,
        previewNormalLight = tuningState.previewNormalLight,
        previewNormalDark = tuningState.previewNormalDark,
        previewMonochromeLight = tuningState.previewMonochromeLight,
        previewMonochromeDark = tuningState.previewMonochromeDark,
    )
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
    val canGenerateRmbg = rmbgMissing && hasRmbgComponent
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
                    onGenerateGpt(mode)
                } else if (rmbgMissing) {
                    onGenerateRmbg(mode)
                } else if (customKind != null) {
                    onChooseCustom(mode, customKind)
                } else {
                    onApply(mode, effectiveChoice)
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
                CandidateIconPreview(
                    candidate = candidate,
                    mode = mode,
                    tuningState = tuningState,
                    cornerRadiusDp = cornerRadiusDp,
                    materialColorProvider = materialColorProvider,
                    loadAssets = loadCandidateAssets,
                )
            } else {
                MissingIconPreview(
                    modifier = Modifier.fillMaxSize(),
                    mode = mode,
                    compact = true,
                    cornerRadiusDp = cornerRadiusDp,
                    materialColorProvider = materialColorProvider,
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
            onApplyAll = { onApplyAll(effectiveChoice) },
        )
    }
}

@Composable
internal fun PreviewChoiceBottomSheet(
    show: Boolean,
    mode: PreviewMode,
    session: GenerationSession,
    tuningState: TuningParams,
    isBusy: Boolean,
    isGeneratingGptCandidate: Boolean,
    isGeneratingRmbgCandidate: Boolean,
    draftForegroundSubjectPercentText: String,
    isDark: Boolean,
    nightSubjectLightBackgroundEnabled: Boolean,
    rmbgCandidatePackageName: String?,
    rmbgCandidateMode: PreviewMode?,
    rmbgCandidateFailurePackageName: String?,
    rmbgCandidateFailureMode: PreviewMode?,
    lastRmbgCandidateError: String?,
    rmbgCandidateStatusText: String,
    gptBaseUrl: String,
    gptApiKey: String,
    hasRmbgComponent: Boolean,
    cornerRadiusDp: Int,
    materialColorProvider: (String, Color) -> Color,
    loadCandidateAssets: suspend (IconCandidate, PreviewMode?) -> PreviewAssets?,
    onNightFill: (Boolean) -> Unit,
    onDraftForegroundSubjectPercent: (String) -> Unit,
    onSaveForegroundSubjectPercent: (Int) -> Unit,
    onGenerateGpt: (PreviewMode) -> Unit,
    onGenerateRmbg: (PreviewMode) -> Unit,
    onChooseCustom: (PreviewMode, CustomImageKind) -> Unit,
    onApply: (PreviewMode, PreviewChoice) -> Unit,
    onApplyAll: (PreviewChoice) -> Unit,
    onDismissRequest: () -> Unit,
    onDismissFinished: () -> Unit,
) {
    val defaultChoices = listOf(
        PreviewChoice.Original,
        PreviewChoice.ComposedBackground,
        PreviewChoice.Rmbg,
        PreviewChoice.Gpt,
    )
    val customChoices = listOf(
        PreviewChoice.CustomForeground,
        PreviewChoice.CustomBackground,
    )
    val moreChoices = listOf(
        PreviewChoice.TextSafe,
        PreviewChoice.ComponentSubject,
        PreviewChoice.ComponentBackground,
        PreviewChoice.TwoLayer,
    )
    val selectedMoreRule = PreviewSelections.fromNames(
        tuningState.previewNormalLight,
        tuningState.previewNormalDark,
        tuningState.previewMonochromeLight,
        tuningState.previewMonochromeDark,
    ).choiceFor(mode).let { choice ->
        when {
            choice == PreviewChoice.Plate -> PreviewChoice.Full
            choice in moreChoices -> choice
            else -> null
        }
    }
    var showMoreRules by remember(mode) { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(showMoreRules) {
        if (showMoreRules) {
            delay(60)
            scrollState.animateScrollTo(
                scrollState.maxValue,
                animationSpec = tween(durationMillis = 120, easing = LinearEasing),
            )
            delay(140)
            scrollState.animateScrollTo(
                scrollState.maxValue,
                animationSpec = tween(durationMillis = 180, easing = LinearEasing),
            )
            delay(80)
            if (scrollState.value < scrollState.maxValue) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
    }

    WindowBottomSheet(
        show = show,
        title = "${mode.label} 来源",
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        insideMargin = DpSize(16.dp, 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "每个槽位单独选择",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (mode == PreviewMode.NormalDark) {
                PreviewNightFillBackgroundRow(
                    checked = nightSubjectLightBackgroundEnabled,
                    isBusy = isBusy,
                    onCheckedChange = onNightFill,
                )
            }

            NumberParameterControl(
                busy = isBusy,
                title = "主体占比",
                summary = "复杂游戏图标建议 100%",
                value = tuningState.foregroundSubjectPercent,
                draftText = draftForegroundSubjectPercentText,
                min = MIN_FOREGROUND_SUBJECT_PERCENT,
                max = MAX_FOREGROUND_SUBJECT_PERCENT,
                step = 1,
                onDraftChange = onDraftForegroundSubjectPercent,
                onSave = onSaveForegroundSubjectPercent,
                showIcon = false,
                icon = null,
                standaloneCard = true,
                cardHeight = 78.dp,
                inputBackgroundColor = if (isDark) {
                    Color.Black.copy(alpha = 0.36f)
                } else {
                    Color.Black.copy(alpha = 0.09f)
                },
            )

            defaultChoices.forEach { choice ->
                PreviewChoiceRow(
                    mode = mode,
                    choice = choice,
                    session = session,
                    tuningState = tuningState,
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
                    hasRmbgComponent = hasRmbgComponent,
                    cornerRadiusDp = cornerRadiusDp,
                    materialColorProvider = materialColorProvider,
                    loadCandidateAssets = loadCandidateAssets,
                    onGenerateGpt = onGenerateGpt,
                    onGenerateRmbg = onGenerateRmbg,
                    onChooseCustom = onChooseCustom,
                    onApply = onApply,
                    onApplyAll = onApplyAll,
                )
            }
            if (shouldShowPreviewChoiceRow(PreviewChoice.Full, session)) {
                PreviewChoiceRow(
                    mode = mode,
                    choice = PreviewChoice.Full,
                    session = session,
                    tuningState = tuningState,
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
                    hasRmbgComponent = hasRmbgComponent,
                    cornerRadiusDp = cornerRadiusDp,
                    materialColorProvider = materialColorProvider,
                    loadCandidateAssets = loadCandidateAssets,
                    onGenerateGpt = onGenerateGpt,
                    onGenerateRmbg = onGenerateRmbg,
                    onChooseCustom = onChooseCustom,
                    onApply = onApply,
                    onApplyAll = onApplyAll,
                )
            }

            customChoices.forEach { choice ->
                PreviewChoiceRow(
                    mode = mode,
                    choice = choice,
                    session = session,
                    tuningState = tuningState,
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
                    hasRmbgComponent = hasRmbgComponent,
                    cornerRadiusDp = cornerRadiusDp,
                    materialColorProvider = materialColorProvider,
                    loadCandidateAssets = loadCandidateAssets,
                    onGenerateGpt = onGenerateGpt,
                    onGenerateRmbg = onGenerateRmbg,
                    onChooseCustom = onChooseCustom,
                    onApply = onApply,
                    onApplyAll = onApplyAll,
                )
            }

            MoreRulesGroupRow(
                selectedRule = selectedMoreRule,
                expanded = showMoreRules,
                isBusy = isBusy,
                isGeneratingGptCandidate = isGeneratingGptCandidate,
                isGeneratingRmbgCandidate = isGeneratingRmbgCandidate,
                onToggle = { showMoreRules = !showMoreRules },
            )
            AnimatedVisibility(
                visible = showMoreRules,
                enter = fadeIn(animationSpec = tween(durationMillis = 150)) +
                    expandVertically(animationSpec = tween(durationMillis = 180)),
                exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                    shrinkVertically(animationSpec = tween(durationMillis = 160)),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    moreChoices.forEach { choice ->
                        if (shouldShowPreviewChoiceRow(choice, session)) {
                            PreviewChoiceRow(
                                mode = mode,
                                choice = choice,
                                session = session,
                                tuningState = tuningState,
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
                                hasRmbgComponent = hasRmbgComponent,
                                cornerRadiusDp = cornerRadiusDp,
                                materialColorProvider = materialColorProvider,
                                loadCandidateAssets = loadCandidateAssets,
                                onGenerateGpt = onGenerateGpt,
                                onGenerateRmbg = onGenerateRmbg,
                                onChooseCustom = onChooseCustom,
                                onApply = onApply,
                                onApplyAll = onApplyAll,
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = !showMoreRules && selectedMoreRule != null,
                enter = fadeIn(animationSpec = tween(durationMillis = 150)) +
                    expandVertically(animationSpec = tween(durationMillis = 180)),
                exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                    shrinkVertically(animationSpec = tween(durationMillis = 160)),
            ) {
                moreChoices
                    .firstOrNull { it == selectedMoreRule && shouldShowPreviewChoiceRow(it, session) }
                    ?.let { choice ->
                        PreviewChoiceRow(
                            mode = mode,
                            choice = choice,
                            session = session,
                            tuningState = tuningState,
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
                            hasRmbgComponent = hasRmbgComponent,
                            cornerRadiusDp = cornerRadiusDp,
                            materialColorProvider = materialColorProvider,
                            loadCandidateAssets = loadCandidateAssets,
                            onGenerateGpt = onGenerateGpt,
                            onGenerateRmbg = onGenerateRmbg,
                            onChooseCustom = onChooseCustom,
                            onApply = onApply,
                            onApplyAll = onApplyAll,
                        )
                    }
            }
        }
    }
}

