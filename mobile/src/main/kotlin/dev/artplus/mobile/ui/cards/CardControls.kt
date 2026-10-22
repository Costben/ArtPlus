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
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.blur.rememberLayerBackdrop
import io.github.suqi8.coui.kmp.blur.drawBackdrop
import io.github.suqi8.coui.kmp.blur.blur
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
import androidx.compose.ui.draw.alpha
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
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.IconButton
import io.github.suqi8.coui.kmp.basic.CardDefaults
import io.github.suqi8.coui.kmp.basic.TabRowDefaults
import io.github.suqi8.coui.kmp.basic.TabRowWithContour
import io.github.suqi8.coui.kmp.basic.TagView
import io.github.suqi8.coui.kmp.basic.Button
import io.github.suqi8.coui.kmp.basic.ButtonDefaults
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.Checkbox
import io.github.suqi8.coui.kmp.basic.CheckboxDefaults
import io.github.suqi8.coui.kmp.basic.DropdownColors
import io.github.suqi8.coui.kmp.basic.DropdownDefaults
import io.github.suqi8.coui.kmp.basic.DropdownEntry
import io.github.suqi8.coui.kmp.basic.DropdownItem
import io.github.suqi8.coui.kmp.basic.LinearProgressIndicator
import io.github.suqi8.coui.kmp.basic.ScrollBehavior
import io.github.suqi8.coui.kmp.basic.COUIScrollBehavior
import io.github.suqi8.coui.kmp.basic.Scaffold
import io.github.suqi8.coui.kmp.basic.Slider
import io.github.suqi8.coui.kmp.basic.SliderDefaults
import io.github.suqi8.coui.kmp.basic.Switch
import io.github.suqi8.coui.kmp.basic.TabRow
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TextButton
import io.github.suqi8.coui.kmp.basic.TextField
import io.github.suqi8.coui.kmp.basic.TopAppBar
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.basic.ArrowRight
import io.github.suqi8.coui.kmp.icon.basic.ArrowUpDown
import io.github.suqi8.coui.kmp.icon.extended.More
import io.github.suqi8.coui.kmp.popup.WindowDropdownPopup
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.theme.darkColorScheme
import io.github.suqi8.coui.kmp.theme.lightColorScheme
import android.graphics.Color as AndroidColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.caverock.androidsvg.SVG

internal fun formatScale(value: Float): String =
    String.format(Locale.US, "%.2f", value)

/**
 * 分段控件的禁用态包装：COUI TabRow 系列没有 enabled 参数，
 * 禁用时降低透明度并用透明层吞掉触摸，行为与原先 enabled=false 等价。
 */
@Composable
private fun SegmentEnabledScope(
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f),
    ) {
        content()
        if (!enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            )
        }
    }
}

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
    val safeSelectedIndex = selectedIndex.coerceIn(0, labels.lastIndex)
    SegmentEnabledScope(enabled = enabled) {
        // COUI 分段按钮：自带滑动轮廓动画；scrollable 时每段至少 86dp，超出可横向滚动。
        TabRowWithContour(
            tabs = labels,
            selectedTabIndex = safeSelectedIndex,
            onTabSelected = { index ->
                if (enabled && index != safeSelectedIndex) {
                    onSelected(index)
                }
            },
            minWidth = if (scrollable) 86.dp else TabRowDefaults.TabRowWithContourMinWidth,
        )
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
    inputBackgroundColor: Color? = null,
    standaloneCard: Boolean = false,
    cardHeight: Dp = 78.dp,
) {
    val controlEnabled = enabled && !busy
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val body: @Composable ColumnScope.() -> Unit = {
        // 头部行：COUI BasicComponent，按压反馈由 COUI 指示器提供
        BasicComponent(
            modifier = (if (standaloneCard) Modifier.heightIn(min = cardHeight) else Modifier)
                .semantics {
                    stateDescription = if (expanded) "已展开拖动条" else "已收起拖动条"
                },
            title = title,
            summary = summary,
            startAction = if (showIcon) {
                { SettingsLineIcon(kind = icon ?: settingsIconForTitle(title)) }
            } else {
                null
            },
            endActions = {
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
                        backgroundColor = inputBackgroundColor,
                    )
                }
                KernelStyleArrow(expanded = expanded)
            },
            onClick = { expanded = !expanded },
            role = Role.Button,
            enabled = controlEnabled,
        )
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
                    // 全出血行展开后用最小高度（52dp）提供呼吸感；standaloneCard 分支自带底部 12dp。
                    .heightIn(min = if (standaloneCard) 0.dp else 52.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = if (standaloneCard) 12.dp else 0.dp),
                contentAlignment = Alignment.Center,
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
    if (standaloneCard) {
        // 底部弹层内的独立卡片：中性 secondaryVariant 填充，避免与面板同色
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(0.dp),
            colors = CardDefaults.defaultColors(color = COUITheme.colorScheme.secondaryVariant),
            content = body,
        )
    } else {
        Column(modifier = Modifier.fillMaxWidth(), content = body)
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
    // 选中态用 COUI 蓝色淡底卡片（tertiaryContainer），未选中用中性 secondaryVariant
    val containerColor = if (isActive) {
        COUITheme.colorScheme.tertiaryContainer
    } else {
        COUITheme.colorScheme.secondaryVariant
    }
    val activeTextColor = COUITheme.colorScheme.onTertiaryContainer
    val tags = remember(preset.params) { preset.featureTags() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = CompactCardCornerRadius,
        // 3dp vertical margin keeps the 54dp card height while the icon buttons take 48dp.
        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 3.dp),
        colors = CardDefaults.defaultColors(color = containerColor),
        onClick = if (busy) null else onApply,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) {
                            if (isModified) COUITheme.colorScheme.primaryVariant.copy(alpha = 0.6f) else COUITheme.colorScheme.primaryVariant
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
                        style = COUITheme.textStyles.body1.copy(
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (isActive) activeTextColor else COUITheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    tags.forEach { tag ->
                        TagView(text = tag, cornerRadius = AppTagCornerRadius)
                    }
                }

                Text(
                    text = formatPresetDate(preset.updatedAt) + if (isActive) (if (isModified) " · 已套用 (已修改)" else " · 已套用") else "",
                    style = COUITheme.textStyles.footnote2,
                    color = if (isActive) activeTextColor.copy(alpha = 0.8f) else COUITheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(
                onClick = onPreview,
                enabled = !busy,
                modifier = Modifier.size(48.dp),
                backgroundColor = Color.Transparent,
                cornerRadius = 8.dp,
            ) {
                Icon(
                    imageVector = Lucide.Eye,
                    contentDescription = "批量预览",
                    modifier = Modifier.size(16.dp),
                    tint = COUITheme.colorScheme.onSurfaceVariantSummary,
                )
            }

            IconButton(
                onClick = onMore,
                enabled = !busy,
                modifier = Modifier.size(48.dp),
                backgroundColor = Color.Transparent,
                cornerRadius = 8.dp,
            ) {
                Icon(
                    imageVector = COUIIcons.More,
                    contentDescription = "更多操作",
                    modifier = Modifier.size(16.dp),
                    tint = COUITheme.colorScheme.onSurfaceVariantSummary,
                )
            }
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
    // COUI 行：按压反馈、标题/摘要配色与禁用态均由 BasicComponent 提供
    BasicComponent(
        title = title,
        summary = summary,
        onClick = onClick,
        enabled = enabled,
    )
}

@Composable
internal fun AdvancedCategoryTabs(
    selected: AdvancedSettingsCategory,
    onSelected: (AdvancedSettingsCategory) -> Unit,
    enabled: Boolean = true,
) {
    val categories = AdvancedSettingsCategory.entries
    val safeSelectedIndex = categories.indexOf(selected).coerceAtLeast(0)
    SegmentEnabledScope(enabled = enabled) {
        // 分类导航用无轮廓的 COUI TabRow，与上方轮廓分段按钮形成层级
        TabRow(
            tabs = categories.map { it.label },
            selectedTabIndex = safeSelectedIndex,
            onTabSelected = { index ->
                if (enabled && index != safeSelectedIndex) {
                    onSelected(categories[index])
                }
            },
        )
    }
}
