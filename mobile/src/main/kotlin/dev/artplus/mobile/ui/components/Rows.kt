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
internal fun settingsDropdownColors(): DropdownColors {
    val primary = MiuixTheme.colorScheme.primaryVariant
    return DropdownDefaults.dropdownColors(
        selectedContainerColor = primary.copy(alpha = 0.12f),
        selectedContentColor = primary,
        selectedSummaryColor = primary,
        selectedIndicatorColor = primary,
    )
}

@Composable
internal fun libraryRowPressedColor(pressed: Boolean): Color =
    when {
        !pressed -> Color.Transparent
        isSystemInDarkTheme() -> Color.White.copy(alpha = 0.10f)
        else -> Color.Black.copy(alpha = 0.10f)
    }

/** 图1 标准设置行：整行按压块被卡片圆角裁切；内容元素为库组件。 */
@Composable
internal fun LibrarySettingRow(
    title: String,
    summary: String?,
    icon: SettingsIconKind? = null,
    value: String? = null,
    showValue: Boolean = true,
    showArrowUpDown: Boolean = false,
    showArrowRight: Boolean = false,
    showSwitch: Boolean = false,
    checked: Boolean = false,
    enabled: Boolean = true,
    showIcon: Boolean = icon != null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val bridge = LocalSectionCardPressBridge.current
    val clickableModifier = if (onClick != null || onCheckedChange != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled && (onClick != null || onCheckedChange != null),
            onClick = {
                if (onCheckedChange != null) {
                    onCheckedChange(!checked)
                } else {
                    onClick?.invoke()
                }
            },
        )
    } else {
        Modifier
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .trackSectionPress(bridge, pressed)
            .background(cardRowPressedColor(pressed))
            .then(clickableModifier)
            .padding(horizontal = CHOICE_ROW_HORIZONTAL_BLEED_DP.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        if (showIcon && icon != null) {
            SettingsLineIcon(kind = icon)
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
            if (!summary.isNullOrBlank()) {
                Text(
                    text = summary,
                    modifier = Modifier.basicMarquee(),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
        if (value != null && showValue) {
            Text(
                text = value,
                fontSize = MiuixTheme.textStyles.body2.fontSize,
                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                maxLines = 1,
            )
        }
        if (showSwitch) {
            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
            )
        }
        if (showArrowUpDown) {
            Image(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(width = 10.dp, height = 16.dp),
                imageVector = MiuixIcons.Basic.ArrowUpDown,
                contentDescription = null,
                colorFilter = ColorFilter.tint(if (enabled) {
                    MiuixTheme.colorScheme.onSurfaceVariantActions
                } else {
                    MiuixTheme.colorScheme.disabledOnSecondaryVariant
                }),
            )
        }
        if (showArrowRight) {
            Image(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(width = 10.dp, height = 16.dp),
                imageVector = MiuixIcons.Basic.ArrowRight,
                contentDescription = null,
                colorFilter = ColorFilter.tint(if (enabled) {
                    MiuixTheme.colorScheme.onSurfaceVariantActions
                } else {
                    MiuixTheme.colorScheme.disabledOnSecondaryVariant
                }),
            )
        }
    }
}

/** 库标准选择行：整行按压 + 窗口级列表弹窗（WindowDropdownPopup），保证层级在 FloatingBottomBar 之上。 */
@Composable
internal fun LibraryChoiceRow(
    title: String,
    summary: String?,
    value: String?,
    icon: SettingsIconKind,
    entry: DropdownEntry,
    enabled: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        LibrarySettingRow(
            title = title,
            summary = summary,
            value = value,
            icon = icon,
            showArrowUpDown = true,
            enabled = enabled,
            onClick = { expanded = true },
        )
        WindowDropdownPopup(
            entry = entry,
            show = expanded,
            onDismiss = { expanded = false },
            onDismissFinished = { },
            maxHeight = null,
            dropdownColors = settingsDropdownColors(),
            collapseOnSelection = true,
        )
    }
}

@Composable
internal fun SettingsTextInputRow(
    title: String,
    value: String,
    label: String,
    inputHint: String,
    icon: SettingsIconKind,
    obscure: Boolean = false,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
) {
    var dialogVisible by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf(value) }
    LaunchedEffect(value) {
        draft = value
    }
    LibrarySettingRow(
        title = title,
        summary = when {
            value.isBlank() -> "$label · 未设置"
            obscure -> "••••••••"
            else -> value
        },
        icon = icon,
        showValue = false,
        showArrowRight = true,
        enabled = enabled,
        onClick = {
            draft = value
            dialogVisible = true
        },
    )
    if (dialogVisible) {
        MiuixBottomDialog(onDismissRequest = { dialogVisible = false }) {
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
                    text = title,
                    style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                    color = MiuixTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                TextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = inputHint,
                    singleLine = true,
                    visualTransformation = if (obscure) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { dialogVisible = false },
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
                            onValueChange(draft)
                            dialogVisible = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary(),
                    ) {
                        Text(
                            text = "确定",
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

/** 库标准信息展示行：无交互、右侧可选值标记。 */
@Composable
internal fun SettingsInfoRow(
    title: String,
    summary: String?,
    value: String?,
    icon: SettingsIconKind,
) {
    LibrarySettingRow(
        title = title,
        summary = summary,
        value = value,
        icon = icon,
        enabled = true,
    )
}

@Composable
internal fun SettingLine(
    title: String,
    summary: String,
    value: String = "",
    showIcon: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val bleedPx = with(LocalDensity.current) { CHOICE_ROW_HORIZONTAL_BLEED_DP.dp.roundToPx() }
    val bridge = LocalSectionCardPressBridge.current
    val rowModifier = if (onClick == null) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .trackSectionPress(bridge, pressed)
            .cardRowBleed(bleedPx)
            .background(cardRowPressedColor(pressed))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = CHOICE_ROW_HORIZONTAL_BLEED_DP.dp)
            .padding(vertical = 4.dp)
    }
    Row(
        modifier = rowModifier.heightIn(min = 64.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showIcon) {
            SettingsLineIcon(kind = settingsIconForTitle(title))
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
                modifier = Modifier.basicMarquee(),
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                softWrap = false,
            )
        }
        if (value.isNotBlank()) {
            MetricPill(label = value)
        }
    }
}

@Composable
internal fun settingsIconForTitle(title: String): SettingsIconKind =
    when (title) {
        "目标应用" -> SettingsIconKind.Grid
        "应用预设" -> SettingsIconKind.Layers
        "应用范围" -> SettingsIconKind.Grid
        "应用列表" -> SettingsIconKind.Grid
        "显示系统应用" -> SettingsIconKind.Shield
        "已生成" -> SettingsIconKind.CheckBadge
        "使用情况访问" -> SettingsIconKind.Shield
        "Root 目标" -> SettingsIconKind.Shield
        "外部导出" -> SettingsIconKind.FileUpload
        "导出到外部目录" -> SettingsIconKind.FileUpload
        "备份到外部目录" -> SettingsIconKind.FileUpload
        "导出引导" -> SettingsIconKind.FileUpload
        "RMBG 状态" -> SettingsIconKind.Chip
        "模型版本" -> SettingsIconKind.Layers
        "AI image two 生成模式" -> SettingsIconKind.Spark
        "AI 提示词" -> SettingsIconKind.Prompt
        "液态玻璃风格" -> SettingsIconKind.Glass
        "渲染方式" -> SettingsIconKind.Layers
        "圆角半径" -> SettingsIconKind.Radius
        "边缘高光宽度" -> SettingsIconKind.Glass
        "主体占比" -> SettingsIconKind.Scale
        "前景主体大小" -> SettingsIconKind.Scale
        "预览圆角" -> SettingsIconKind.Radius
        "预览缩放" -> SettingsIconKind.Grid
        "主体阴影等级" -> SettingsIconKind.Shadow
        "单色主体缩放" -> SettingsIconKind.Scale
        "背景剔除阈值" -> SettingsIconKind.Cutout
        "背景相似度" -> SettingsIconKind.Cutout
        "底板颜色阈值" -> SettingsIconKind.Plate
        "底板清理" -> SettingsIconKind.Plate
        "长阴影清理强度" -> SettingsIconKind.Shadow
        "旧阴影清理" -> SettingsIconKind.Eraser
        "毛刺优化" -> SettingsIconKind.Spark
        "边缘修补" -> SettingsIconKind.Spark
        "RMBG Alpha 强度" -> SettingsIconKind.Cutout
        "Alpha 力度" -> SettingsIconKind.Cutout
        "RMBG 边缘柔化" -> SettingsIconKind.Cutout
        "边缘柔化" -> SettingsIconKind.Cutout
        "RMBG 边缘扩张" -> SettingsIconKind.Scale
        "边缘扩缩" -> SettingsIconKind.Scale
        "RMBG 弱透明保留" -> SettingsIconKind.Cutout
        "弱透明保留" -> SettingsIconKind.Cutout
        "自动确认写入" -> SettingsIconKind.Shield
        "自动确认刷新" -> SettingsIconKind.Refresh
        "单色缩放" -> SettingsIconKind.Scale
        else -> SettingsIconKind.Dot
    }

internal enum class SettingsIconKind {
    Grid,
    CheckBadge,
    Shield,
    FileUpload,
    Glass,
    Radius,
    Chip,
    Layers,
    Spark,
    Scale,
    Cutout,
    Palette,
    Plate,
    Shadow,
    Eraser,
    Link,
    Key,
    Prompt,
    Refresh,
    Dot,
}

internal fun settingsIconVector(kind: SettingsIconKind): ImageVector = when (kind) {
    SettingsIconKind.Grid -> Lucide.Grid2x2
    SettingsIconKind.CheckBadge -> Lucide.BadgeCheck
    SettingsIconKind.Shield -> Lucide.Shield
    SettingsIconKind.FileUpload -> Lucide.FileUp
    SettingsIconKind.Glass -> Lucide.GlassWater
    SettingsIconKind.Radius -> Lucide.Radius
    SettingsIconKind.Chip -> Lucide.Cpu
    SettingsIconKind.Layers -> Lucide.Layers
    SettingsIconKind.Spark -> Lucide.Sparkles
    SettingsIconKind.Scale -> Lucide.Scale
    SettingsIconKind.Cutout -> Lucide.SlidersHorizontal
    SettingsIconKind.Palette -> Lucide.Palette
    SettingsIconKind.Plate -> Lucide.Palette
    SettingsIconKind.Shadow -> Lucide.Sparkles
    SettingsIconKind.Eraser -> Lucide.Eraser
    SettingsIconKind.Link -> Lucide.Link
    SettingsIconKind.Key -> Lucide.KeyRound
    SettingsIconKind.Prompt -> Lucide.MessageSquareText
    SettingsIconKind.Refresh -> Lucide.RefreshCw
    SettingsIconKind.Dot -> Lucide.Settings
}

@Composable
internal fun SettingsLineIcon(kind: SettingsIconKind, modifier: Modifier = Modifier) {
    Image(
        imageVector = settingsIconVector(kind),
        contentDescription = null,
        modifier = modifier.size(18.dp),
        colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurface),
    )
}

@Composable
internal fun MiuixBottomDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        ApplyDialogDimEffect()
        var animateIn by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            animateIn = true
        }
        val offsetY by animateFloatAsState(
            targetValue = if (animateIn) 0f else 320f,
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = 420f,
            ),
            label = "MiuixBottomDialogSlideUp",
        )
        val alpha by animateFloatAsState(
            targetValue = if (animateIn) 1f else 0f,
            animationSpec = tween(durationMillis = 180),
            label = "MiuixBottomDialogFadeIn",
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest,
                )
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = offsetY
                        this.alpha = alpha
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            ) {
                content()
            }
        }
    }
}

@Composable
internal fun ApplyDialogDimEffect() {
    val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
    val targetDim = MiuixTheme.colorScheme.windowDimming.alpha
    SideEffect {
        dialogWindow?.setDimAmount(targetDim)
    }
}
