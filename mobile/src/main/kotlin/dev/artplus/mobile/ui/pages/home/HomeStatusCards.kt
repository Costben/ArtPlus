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
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.blur.rememberLayerBackdrop
import io.github.suqi8.coui.kmp.blur.drawBackdrop
import io.github.suqi8.coui.kmp.blur.blur
import dev.artplus.mobile.glass.lens
import dev.artplus.mobile.glass.vibrancy
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
import io.github.suqi8.coui.kmp.basic.Button
import io.github.suqi8.coui.kmp.basic.EmptyState
import io.github.suqi8.coui.kmp.utils.PressFeedbackType
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
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.icon.basic.ArrowRight
import io.github.suqi8.coui.kmp.icon.basic.ArrowUpDown
import io.github.suqi8.coui.kmp.icon.extended.Layers
import io.github.suqi8.coui.kmp.popup.WindowDropdownPopup
import io.github.suqi8.coui.kmp.window.WindowBottomSheet
import io.github.suqi8.coui.kmp.layout.BottomSheetDefaults
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.theme.darkColorScheme
import io.github.suqi8.coui.kmp.theme.lightColorScheme
import android.graphics.Color as AndroidColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.caverock.androidsvg.SVG
/**
 * Slice 2.2：主页状态卡片（原 MainActivity 残留本体原样搬迁）。
 * Composable 只收 state + onEvent；Activity 状态经参数/回调注入，行为与 UI 100% 等价。
 */

@Composable
internal fun StatusCard(
    selectedApp: AppEntry?,
    launcherCount: Int,
    totalCount: Int,
    generatedCount: Int,
    isBusy: Boolean,
    hasApps: Boolean,
    statusText: String,
    onOpenPicker: () -> Unit,
    appIcon: @Composable (AppEntry) -> Unit,
) {
    val statusLabel = if (isBusy) "运行中" else "就绪"
    val enabled = !isBusy && hasApps

    // COUI 可点击卡片：Tint 按压反馈由 Card 提供；不可用时不挂 onClick
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(16.dp),
        pressFeedbackType = PressFeedbackType.Tint,
        onClick = if (enabled) onOpenPicker else null,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (selectedApp == null) {
                BrandMark(size = 48.dp, text = "UX")
            } else {
                appIcon(selectedApp)
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
                        style = COUITheme.textStyles.title4,
                        color = COUITheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = selectedApp?.label ?: "选择一个应用开始生成",
                    style = COUITheme.textStyles.body1,
                    color = COUITheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = selectedApp?.packageName ?: statusText,
                    style = COUITheme.textStyles.footnote1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "启动器 $launcherCount 个 / 全部 $totalCount 个 / 已生成 $generatedCount 个",
                    style = COUITheme.textStyles.footnote1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            // COUI 右箭头（与 ArrowPreference 末端箭头一致）
            Image(
                modifier = Modifier.size(width = 12.dp, height = 24.dp),
                imageVector = COUIIcons.Basic.ArrowRight,
                contentDescription = null,
                colorFilter = ColorFilter.tint(COUITheme.colorScheme.onSurfaceVariantActions),
            )
        }
    }
}

@Composable
internal fun EmptyAppListCard(
    queryText: String,
    showSystemApps: Boolean,
    hasHiddenSystemApps: Boolean,
    isBusy: Boolean,
    onShowSystemApps: () -> Unit,
    onRefresh: () -> Unit,
) {
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
        // COUI EmptyState 承载插画 + 标题 + 说明；两个操作保持 TextButton 以保留 enabled 语义
        EmptyState(
            title = "没有可显示的应用",
            subtitle = hintText,
            modifier = Modifier.fillMaxWidth(),
            illustration = {
                Icon(
                    imageVector = COUIIcons.Layers,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = COUITheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.4f),
                )
            },
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (hasHiddenSystemApps && !showSystemApps) {
            TextButton(
                text = "显示系统应用",
                onClick = onShowSystemApps,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth().semantics {
                    contentDescription = "显示系统应用按钮"
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        TextButton(
            text = "刷新应用列表",
            onClick = onRefresh,
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth().semantics {
                contentDescription = "刷新应用列表"
            },
        )
    }
}

/** 第二层级「生成设置」：顶部「滑块 / JSON」切换 + 保存成预设 + 滑块分类导航。 */
@Composable
internal fun GenerationNavCard(
    isBusy: Boolean,
    advancedSettingsTab: AdvancedSettingsTab,
    advancedSettingsCategory: AdvancedSettingsCategory,
    onTabSelected: (AdvancedSettingsTab) -> Unit,
    onRequestSavePreset: () -> Unit,
    onCategorySelected: (AdvancedSettingsCategory) -> Unit,
) {
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
                    onTabSelected(AdvancedSettingsTab.entries[index])
                },
            )
            TextButton(
                text = "保存成预设",
                onClick = onRequestSavePreset,
                modifier = Modifier.fillMaxWidth(),
            )
            if (advancedSettingsTab == AdvancedSettingsTab.Sliders) {
                AdvancedCategoryTabs(
                    enabled = !isBusy,
                    selected = advancedSettingsCategory,
                    onSelected = onCategorySelected,
                )
            }
        }
    }
}

@Composable
internal fun GenerationActionCard(
    selectedApp: AppEntry?,
    isBusy: Boolean,
    onLocalGenerate: () -> Unit,
    onLocalExport: () -> Unit,
    onWriteAll: () -> Unit,
    onWriteStandard: () -> Unit,
    onWriteMono: () -> Unit,
) {
    val canRun = selectedApp != null && !isBusy
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TextButton(
                text = "本地生成",
                onClick = onLocalGenerate,
                enabled = canRun,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
            TextButton(
                text = "本地导出",
                onClick = onLocalExport,
                enabled = !isBusy,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CompactActionButton(
                text = "写入全部",
                onClick = onWriteAll,
                enabled = canRun,
                modifier = Modifier.weight(1f),
                // 三等分行：默认 12dp 内边距会让 4 个汉字换行，收窄到 4dp 保持单行。
                insideMargin = PaddingValues(horizontal = 4.dp),
            )
            CompactActionButton(
                text = "写入标准",
                onClick = onWriteStandard,
                enabled = canRun,
                modifier = Modifier.weight(1f),
                // 三等分行：默认 12dp 内边距会让 4 个汉字换行，收窄到 4dp 保持单行。
                insideMargin = PaddingValues(horizontal = 4.dp),
            )
            CompactActionButton(
                text = "写入单色",
                onClick = onWriteMono,
                enabled = canRun,
                modifier = Modifier.weight(1f),
                // 三等分行：默认 12dp 内边距会让 4 个汉字换行，收窄到 4dp 保持单行。
                insideMargin = PaddingValues(horizontal = 4.dp),
            )
        }
    }
}
