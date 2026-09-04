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
internal fun MainActivity.HomePage(pageBackground: Color, selectedApp: AppEntry?, launcherCount: Int, totalCount: Int, generatedCount: Int) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
    val scope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()
    val containerColor = if (isDark) Color(0xFF121212).copy(alpha = 0.4f) else Color(0xFFFAFAFA).copy(alpha = 0.4f)
    val isBlurEnabled = liquidGlassBottomBarEnabled && liquidGlassBottomBarBlurEnabled
    var beyondViewportCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        delay(300)
        beyondViewportCount = 1
    }
    val backdrop = rememberLayerBackdrop {
        drawRect(pageBackground)
        drawContent()
    }
    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = beyondViewportCount,
            modifier = Modifier
                .fillMaxSize()
                .background(pageBackground)
                .then(if (isBlurEnabled) Modifier.layerBackdrop(backdrop) else Modifier),
        ) { page ->
            when (page) {
                0 -> PagerShellPage(
                    title = "ArtPlus",
                    navigationIcon = {
                        TitleBarIconButton(
                            icon = Lucide.RefreshCw,
                            contentDescription = "刷新",
                            enabled = !isBusy && !isRefreshingArtPlusIcons,
                            dimWhenDisabled = false,
                            onClick = {
                                if (autoConfirmRefresh) {
                                    refreshArtPlusIcons()
                                } else {
                                    refreshConfirmRememberAuto = false
                                    refreshConfirmVisible = true
                                }
                            },
                        )
                    },
                    showPreviewStrip = previewStripEnabled,
                ) { innerPadding, scrollBehavior ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .imePadding()
                            .padding(innerPadding)
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (!packageListPermissionGranted || !usageAccessGranted) {
                            item(key = "permission") {
                                PermissionCard()
                            }
                        }
                        item(key = "status") {
                            StatusCard(
                                selectedApp = selectedApp,
                                launcherCount = launcherCount,
                                totalCount = apps.size,
                                generatedCount = generatedCount,
                            )
                        }
                        item(key = "generation_action") {
                            GenerationActionCard(selectedApp)
                        }
                        if (previewDirPath != null && previewPackageName != null) {
                            item(key = "generated_preview") {
                                GeneratedPreviewCard()
                            }
                        }
                        item(key = "preview_control") {
                            PreviewControlCard()
                        }
                        item(key = "layer_debug") {
                            LayerDebugCard()
                        }
                    }
                }

                1 -> PagerShellPage(
                    title = "生成参数",
                    showPreviewStrip = previewStripEnabled,
                ) { innerPadding, scrollBehavior ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .imePadding()
                            .padding(innerPadding)
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item(key = "gen_nav") {
                            GenerationNavCard()
                        }
                        when (advancedSettingsTab) {
                            AdvancedSettingsTab.Sliders -> when (advancedSettingsCategory) {
                                AdvancedSettingsCategory.LiquidGlass -> {
                                    item(key = "glass_toggle") { LiquidGlassToggleCard() }
                                    item(key = "glass_surface") { LiquidGlassSurfaceCard() }
                                    item(key = "glass_subject") { LiquidGlassSubjectCard() }
                                }
                                AdvancedSettingsCategory.Local -> {
                                    item(key = "local_rule") { LocalRuleTuningCard() }
                                    item(key = "local_pipeline") { LocalWorkflowPipelineCard() }
                                }
                                AdvancedSettingsCategory.Rmbg -> {
                                    item(key = "rmbg_tuning") { RmbgTuningCard() }
                                }
                            }
                            AdvancedSettingsTab.Json -> {
                                item(key = "json_editor") {
                                    JsonSettingsEditorCard()
                                }
                            }
                        }
                    }
                }

                2 -> PagerShellPage(
                    title = "预设",
                    actions = {
                        val presetCount = remember(presetListVersion) { presetStore.all().size }
                        TitleBarIconButton(
                            icon = Lucide.Download,
                            contentDescription = "导入预设",
                            enabled = !isBusy,
                            dimWhenDisabled = false,
                            onClick = {
                                presetImportText = ""
                                presetImportDialogVisible = true
                            },
                            paddingStart = 0.dp,
                            paddingEnd = 8.dp,
                        )
                        TitleBarIconButton(
                            icon = Lucide.Upload,
                            contentDescription = "导出全部预设",
                            enabled = !isBusy && presetCount > 0,
                            dimWhenDisabled = true,
                            onClick = { exportPresetsToClipboard() },
                            paddingStart = 0.dp,
                            paddingEnd = 16.dp,
                        )
                    },
                    showPreviewStrip = previewStripEnabled,
                ) { innerPadding, scrollBehavior ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .imePadding()
                            .padding(innerPadding)
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item(key = "preset_status") {
                            PresetStatusCard()
                        }
                        item(key = "preset_library") {
                            PresetLibraryCard()
                        }
                        item(key = "batch_preview") {
                            BatchPreviewSettingsCard()
                        }
                    }
                }

                3 -> PagerShellPage(
                    title = "设置",
                    actions = {
                        TitleBarIconButton(
                            icon = Lucide.Save,
                            contentDescription = null,
                            enabled = !isBusy,
                            dimWhenDisabled = false,
                            onClick = { saveSettingsPage() },
                            paddingStart = 0.dp,
                            paddingEnd = 16.dp,
                        )
                    },
                ) { innerPadding, scrollBehavior ->
                    SettingsPage(
                        innerPadding = innerPadding,
                        scrollBehavior = scrollBehavior,
                        launcherCount = launcherCount,
                        totalCount = totalCount,
                        generatedCount = generatedCount,
                    )
                }
            }
        }

        // 液态玻璃底栏（KernelSU FloatingBottomBar 1:1：vibrancy+blur4dp+lens24dp 三层玻璃+拖拽阻尼+高光镜面）
        if (liquidGlassBottomBarEnabled) {
            FloatingBottomBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                selectedIndex = { pagerState.targetPage },
                onSelected = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
                backdrop = backdrop,
                tabsCount = 4,
                isBlurEnabled = isBlurEnabled,
            ) {
                listOf(
                    Triple(Lucide.Grid2x2, "主页", 0),
                    Triple(Lucide.SlidersHorizontal, "生成参数", 1),
                    Triple(Lucide.Layers, "预设", 2),
                    Triple(Lucide.Settings, "设置", 3),
                ).forEach { (icon, label, index) ->
                    FloatingBottomBarItem(
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        modifier = Modifier.defaultMinSize(minWidth = 76.dp),
                    ) {
                        val selected = pagerState.targetPage == index
                        val baseTint = if (selected) MiuixTheme.colorScheme.primaryVariant else MiuixTheme.colorScheme.onSurfaceVariantSummary
                        val tint = if (isBusy) baseTint.copy(alpha = 0.45f) else baseTint
                        Image(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(tint),
                        )
                        Text(
                            text = label,
                            style = MiuixTheme.textStyles.footnote1.copy(fontSize = 11.sp),
                            color = tint,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .background(containerColor.copy(alpha = 0.92f))
                    .height(64.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf(
                        Triple(Lucide.Grid2x2, "主页", 0),
                        Triple(Lucide.SlidersHorizontal, "生成参数", 1),
                        Triple(Lucide.Layers, "预设", 2),
                        Triple(Lucide.Settings, "设置", 3),
                    ).forEach { (icon, label, index) ->
                        val selected = pagerState.targetPage == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (selected) MiuixTheme.colorScheme.primaryVariant.copy(alpha = 0.22f)
                                    else Color.Transparent,
                                )
                                .clickable { scope.launch { pagerState.animateScrollToPage(index) } },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                val baseTint = if (selected) MiuixTheme.colorScheme.primaryVariant else MiuixTheme.colorScheme.onSurfaceVariantSummary
                                val tint = if (isBusy) baseTint.copy(alpha = 0.45f) else baseTint
                                Image(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(20.dp),
                                    colorFilter = ColorFilter.tint(tint),
                                )
                                Text(
                                    text = label,
                                    style = MiuixTheme.textStyles.footnote1.copy(fontSize = 11.sp),
                                    color = tint,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 主页面顶栏的紧凑 1×4 预览；设置页和应用选择页不会组合此组件。 */
@Composable
internal fun MainActivity.HomePreviewStrip(
    onHeightMeasured: (androidx.compose.ui.unit.Dp) -> Unit = {},
) {
    val density = LocalDensity.current
    val assets = sharedPreviewAssets
    val loading = isPreviewAssetsRefreshing || isPreviewOutputRefreshing || isGptPreviewLoading
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 0.dp)
            .onGloballyPositioned { coords ->
                val h = with(density) { coords.size.height.toDp() }
                if (h > 0.dp) onHeightMeasured(h)
            },
        insideMargin = PaddingValues(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PreviewMode.entries.forEach { mode ->
                TopPreviewStripTile(
                    assets = assets,
                    mode = mode,
                    loading = loading,
                    desktopBackground = previewDesktopBackground,
                    iconSizeDp = previewIconSizeDp,
                    cornerRadiusDp = previewCornerRadiusDp,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
