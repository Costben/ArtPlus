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

@Composable
internal fun SegmentOption(label: String, selected: Boolean, modifier: Modifier, enabled: Boolean = true, onClick: () -> Unit) {
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
            .clickable(enabled = enabled, onClick = onClick)
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
internal fun DecimalParameterControl(
    title: String,
    summary: String,
    value: Float,
    draftText: String,
    min: Float,
    max: Float,
    onDraftChange: (String) -> Unit,
    onSave: (Float) -> Unit,
    enabled: Boolean = true,
    busy: Boolean = false,
    icon: SettingsIconKind? = null,
) {
    val controlEnabled = enabled && !busy
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsLineIcon(kind = icon ?: settingsIconForTitle(title))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
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

internal fun formatScale(value: Float): String =
    String.format(Locale.US, "%.2f", value)

@Composable
internal fun SegmentedControl(
    labels: List<String>,
    selectedIndex: Int,
    scrollable: Boolean = false,
    enabled: Boolean = true,
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
                                enabled = enabled && !selected,
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
internal fun NumberParameterControl(
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
    busy: Boolean = false,
    icon: SettingsIconKind? = null,
    showIcon: Boolean = true,
    initiallyExpanded: Boolean = false,
) {
    val controlEnabled = enabled && !busy
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val headerInteractionSource = remember { MutableInteractionSource() }
    val headerPressed by headerInteractionSource.collectIsPressedAsState()
    val bridge = LocalSectionCardPressBridge.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // 图1 全出血直角按压块：无 clip，直角背景由 Card 圆角裁切，左右铺满、首末补到容器边
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .trackSectionPress(bridge, headerPressed)
                .background(cardRowPressedColor(headerPressed))
                .clickable(
                    interactionSource = headerInteractionSource,
                    indication = null,
                    enabled = controlEnabled,
                    onClick = { expanded = !expanded },
                )
                .padding(horizontal = CHOICE_ROW_HORIZONTAL_BLEED_DP.dp, vertical = 13.dp)
                .semantics {
                    role = Role.Button
                    stateDescription = if (expanded) "已展开拖动条" else "已收起拖动条"
                },
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showIcon) {
                SettingsLineIcon(kind = icon ?: settingsIconForTitle(title))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
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
                    modifier = if (expanded) Modifier.basicMarquee() else Modifier,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                )
            }
            // 输入框区域消费点击，避免误触切换折叠
            Box(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = controlEnabled,
                    onClick = {},
                ),
            ) {
                NumberInputBox(
                    value = draftText,
                    fallbackValue = value,
                    min = min,
                    max = max,
                    enabled = controlEnabled,
                    onValueChange = onDraftChange,
                    onDone = { submitted ->
                        submitted.toIntOrNull()
                            ?.coerceIn(min, max)
                            ?.let(onSave)
                    },
                )
            }
            KernelStyleArrow(expanded = expanded)
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(durationMillis = 150)) +
                expandVertically(animationSpec = tween(durationMillis = 180)),
            exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                shrinkVertically(animationSpec = tween(durationMillis = 160)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CHOICE_ROW_HORIZONTAL_BLEED_DP.dp),
            ) {
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
    }
}

@Composable
internal fun CompactPresetRow(
    preset: TuningPreset,
    isActive: Boolean,
    isModified: Boolean,
    onApply: () -> Unit,
    onPreview: () -> Unit,
    onMore: () -> Unit,
    busy: Boolean = false,
) {
    val containerBg = if (isActive) {
        MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    } else {
        MiuixTheme.colorScheme.surfaceContainerHigh
    }
    val tags = remember(preset.params) { preset.featureTags() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(containerBg)
            .clickable(enabled = !busy) { onApply() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) {
                        if (isModified) MiuixTheme.colorScheme.primaryVariant.copy(alpha = 0.6f) else MiuixTheme.colorScheme.primaryVariant
                    } else {
                        Color.Transparent
                    }
                ),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = preset.name,
                    style = MiuixTheme.textStyles.body1.copy(
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    ),
                    color = if (isActive) MiuixTheme.colorScheme.primaryVariant else MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.12f))
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                    ) {
                        Text(
                            text = tag,
                            style = MiuixTheme.textStyles.footnote2.copy(fontSize = 10.sp),
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }

            Text(
                text = formatPresetDate(preset.updatedAt) + if (isActive) (if (isModified) " · 已套用 (已修改)" else " · 已套用") else "",
                style = MiuixTheme.textStyles.footnote2,
                color = if (isActive) MiuixTheme.colorScheme.primaryVariant.copy(alpha = 0.8f) else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(enabled = !busy) { onPreview() },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                imageVector = Lucide.Eye,
                contentDescription = "批量预览",
                modifier = Modifier.size(16.dp),
                colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantSummary),
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(enabled = !busy) { onMore() },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                imageVector = Lucide.EllipsisVertical,
                contentDescription = "更多操作",
                modifier = Modifier.size(16.dp),
                colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantSummary),
            )
        }
    }
}

internal fun formatPresetDate(timestamp: Long): String {
    val calendar = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
    return String.format(
        Locale.getDefault(),
        "%04d-%02d-%02d %02d:%02d",
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH) + 1,
        calendar.get(java.util.Calendar.DAY_OF_MONTH),
        calendar.get(java.util.Calendar.HOUR_OF_DAY),
        calendar.get(java.util.Calendar.MINUTE),
    )
}

@Composable
internal fun PresetMenuItem(
    title: String,
    summary: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (pressed) MiuixTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f) else Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MiuixTheme.textStyles.body1.copy(fontWeight = FontWeight.SemiBold),
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
}

@Composable
internal fun AdvancedCategoryTabs(
    selected: AdvancedSettingsCategory,
    onSelected: (AdvancedSettingsCategory) -> Unit,
    enabled: Boolean = true,
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
                            enabled = enabled && !isSelected,
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
