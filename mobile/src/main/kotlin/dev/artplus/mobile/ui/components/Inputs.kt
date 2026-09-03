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
internal fun SteppedPercentSlider(
    value: Int,
    min: Int,
    max: Int,
    step: Int,
    enabled: Boolean,
    showDots: Boolean = true,
    onValueChange: (Int) -> Unit,
) {
    val safeMin = min.toFloat()
    val safeMax = max.coerceAtLeast(min + 1).toFloat()
    val safeStep = step.coerceAtLeast(1)
    val rangeSpan = (max - min).coerceAtLeast(1)
    val stepCount = rangeSpan / safeStep
    val steps = (stepCount - 1).coerceAtLeast(0)
    val showKeyPoints = showDots && stepCount in 1..30
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    Slider(
        value = value.coerceIn(min, max).toFloat(),
        onValueChange = { floatVal ->
            val rawIndex = ((floatVal - safeMin) / safeStep.toFloat()).roundToInt()
            val nextValue = (min + rawIndex * safeStep).coerceIn(min, max)
            if (nextValue != value) {
                currentOnValueChange(nextValue)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        valueRange = safeMin..safeMax,
        steps = steps,
        hapticEffect = SliderDefaults.SliderHapticEffect.Step,
        showKeyPoints = showKeyPoints,
    )
}

@Composable
internal fun DecimalInputBox(
    value: String,
    fallbackValue: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onDone: (String) -> Unit,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(focused) {
        if (focused) {
            delay(260)
            bringIntoViewRequester.bringIntoView()
        }
    }
    var fieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    LaunchedEffect(value) {
        if (value != fieldValue.text) {
            fieldValue = TextFieldValue(value, TextRange(value.length))
        }
    }

    Box(
        modifier = Modifier
            .width(64.dp)
            .height(36.dp)
            .bringIntoViewRequester(bringIntoViewRequester)
            .clip(RoundedCornerShape(11.dp))
            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicTextField(
            value = fieldValue,
            onValueChange = { newValue ->
                val raw = newValue.text
                val filtered = filterDecimalInput(raw)
                if (filtered != raw) {
                    val sel = filtered.length.coerceIn(0, 8)
                    fieldValue = TextFieldValue(filtered, TextRange(sel))
                    onValueChange(filtered)
                } else {
                    // clamp selection to filtered length (filterDecimalInput already limits to 8)
                    val selStart = newValue.selection.start.coerceIn(0, filtered.length)
                    val selEnd = newValue.selection.end.coerceIn(0, filtered.length)
                    fieldValue = newValue.copy(text = filtered, selection = TextRange(selStart, selEnd))
                    onValueChange(filtered)
                }
            },
            enabled = enabled,
            singleLine = true,
            textStyle = MiuixTheme.textStyles.body1.copy(
                color = MiuixTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.None,
                ),
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onDone(fieldValue.text)
                    focusManager.clearFocus()
                },
            ),
            cursorBrush = SolidColor(MiuixTheme.colorScheme.primaryVariant),
            modifier = Modifier
                .fillMaxSize()
                .onFocusChanged { state ->
                    focused = state.isFocused
                    if (state.isFocused) {
                        fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
                    } else {
                        if (fieldValue.text.isEmpty()) {
                            fieldValue = TextFieldValue(fallbackValue, TextRange(fallbackValue.length))
                            onValueChange(fallbackValue)
                        }
                    }
                },
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    innerTextField()
                }
            },
        )
    }
}

@Composable
internal fun NumberInputBox(
    value: String,
    fallbackValue: Int,
    min: Int,
    max: Int,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onDone: (String) -> Unit,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(focused) {
        if (focused) {
            delay(260)
            bringIntoViewRequester.bringIntoView()
        }
    }
    var fieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    LaunchedEffect(value) {
        if (value != fieldValue.text) {
            fieldValue = TextFieldValue(value, TextRange(value.length))
        }
    }

    Box(
        modifier = Modifier
            .width(64.dp)
            .height(36.dp)
            .bringIntoViewRequester(bringIntoViewRequester)
            .clip(RoundedCornerShape(11.dp))
            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicTextField(
            value = fieldValue,
            onValueChange = { newValue ->
                val maxLen = max.toString().length.coerceAtLeast(1).coerceAtMost(3)
                var digits = newValue.text.filter { it.isDigit() }.take(maxLen)
                // 只能填写规定范围内：实时钳到 max，min 仅在 Done 时钳制，避免输入过程中误拦（如 min 45 时输 4）
                if (digits.isNotEmpty()) {
                    digits.toIntOrNull()?.let { v ->
                        if (v > max) digits = max.toString()
                    }
                }
                if (digits != newValue.text) {
                    val sel = digits.length
                    fieldValue = TextFieldValue(digits, TextRange(sel))
                    onValueChange(digits)
                } else {
                    val selStart = newValue.selection.start.coerceIn(0, digits.length)
                    val selEnd = newValue.selection.end.coerceIn(0, digits.length)
                    fieldValue = newValue.copy(text = digits, selection = TextRange(selStart, selEnd))
                    onValueChange(digits)
                }
            },
            enabled = enabled,
            singleLine = true,
            textStyle = MiuixTheme.textStyles.body1.copy(
                color = MiuixTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.None,
                ),
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onDone(fieldValue.text)
                    focusManager.clearFocus()
                },
            ),
            cursorBrush = SolidColor(MiuixTheme.colorScheme.primaryVariant),
            modifier = Modifier
                .fillMaxSize()
                .onFocusChanged { state ->
                    focused = state.isFocused
                    if (state.isFocused) {
                        fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
                    } else {
                        if (fieldValue.text.isEmpty()) {
                            val fb = fallbackValue.toString()
                            fieldValue = TextFieldValue(fb, TextRange(fb.length))
                            onValueChange(fb)
                        }
                    }
                },
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    innerTextField()
                }
            },
        )
    }
}

internal fun filterDecimalInput(value: String): String {
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

@Composable
internal fun CompactActionButton(
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
internal fun InlineInputField(
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
            val inputDark = isSystemInDarkTheme()
            val inputStateColor = Color.White.copy(alpha = if (focused) 0.18f else 0.10f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                        if (inputDark) {
                            Modifier
                                .background(Color(0xFF0A0A0C).copy(alpha = if (focused) 0.85f else 0.7f))
                        } else {
                            Modifier.background(MiuixTheme.colorScheme.surfaceContainerHigh)
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (inputDark) inputStateColor else Color.Transparent,
                        shape = RoundedCornerShape(14.dp),
                    )
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
internal fun TitleBarIconButton(
    icon: ImageVector,
    contentDescription: String?,
    enabled: Boolean,
    onClick: () -> Unit,
    dimWhenDisabled: Boolean = true,
    paddingStart: Dp = 16.dp,
    paddingEnd: Dp = 0.dp,
) {
    Box(
        modifier = Modifier
            .padding(start = paddingStart, end = paddingEnd)
            .size(36.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            colorFilter = ColorFilter.tint(
                if (dimWhenDisabled && !enabled) {
                    MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.45f)
                } else {
                    MiuixTheme.colorScheme.onSurface
                },
            ),
        )
    }
}

@Composable
internal fun PreviewCornerSwitch(checked: Boolean, enabled: Boolean) {
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
internal fun LiquidGlassSwitch(checked: Boolean, enabled: Boolean) {
    val offTrackColor = if (isSystemInDarkTheme()) {
        MiuixTheme.colorScheme.surfaceContainerHighest
    } else {
        MiuixTheme.colorScheme.surfaceContainerHigh
    }
    val targetTrackColor = when {
        checked && enabled -> MiuixTheme.colorScheme.primaryVariant
        checked -> MiuixTheme.colorScheme.primaryVariant.copy(alpha = 0.46f)
        enabled -> offTrackColor
        else -> offTrackColor.copy(alpha = 0.52f)
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
