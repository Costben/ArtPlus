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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
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
import io.github.suqi8.coui.kmp.blur.LocalBackdrop
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
import androidx.compose.runtime.collectAsState
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
import io.github.suqi8.coui.kmp.basic.TopAppBarDefaults
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.ChevronBackward
import io.github.suqi8.coui.kmp.layout.DialogButtonBarAction
import io.github.suqi8.coui.kmp.icon.basic.ArrowRight
import io.github.suqi8.coui.kmp.icon.basic.ArrowUpDown
import io.github.suqi8.coui.kmp.popup.WindowDropdownPopup
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.theme.darkColorScheme
import io.github.suqi8.coui.kmp.theme.lightColorScheme
import android.graphics.Color as AndroidColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.caverock.androidsvg.SVG

@Composable
internal fun MainActivity.AboutPage(pageBackground: Color) {
    val scrollBehavior = rememberLinkedTopAppBarScrollBehavior()
    val shell by mainViewModel.shell.collectAsState()
    val updateUi by mainViewModel.updateUi.collectAsState()
    val aboutCtx = LocalContext.current
    val versionName = pickerCurrentVersionName(
        getVersionName = {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0).versionName
        },
    )
    val versionCode = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(packageName, 0).longVersionCode.toString()
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0).versionCode.toString()
        }
    } catch (_: Exception) {
        ""
    }

    // Backdrop for the ColorOS app bar material: the list only, never the bar itself.
    val appBarBackdrop = rememberLayerBackdrop {
        drawRect(pageBackground)
        drawContent()
    }
    val contentTopInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
        TopAppBarDefaults.ExpandedHeight
    Scaffold(
        containerColor = pageBackground,
        topBar = {
            CompositionLocalProvider(LocalBackdrop provides appBarBackdrop) {
                TopAppBar(
                    title = "关于",
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        TitleBarIconButton(
                            icon = COUIIcons.ChevronBackward,
                            contentDescription = "返回",
                            enabled = !shell.isBusy,
                            onClick = { mainViewModel.updateShell { it -> it.copy(currentPage = (AppPage.Home)) } },
                        )
                    },
                    gradientGlass = true,
                    showDivider = false,
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(appBarBackdrop)
                .background(pageBackground)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = contentTopInset + 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher),
                            contentDescription = "ArtPlus",
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)),
                        )
                        Text(
                            text = "ArtPlus Mobile",
                            style = COUITheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                            color = COUITheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = if (versionCode.isNotBlank()) "$versionName ($versionCode)" else versionName,
                            style = COUITheme.textStyles.body2,
                            color = COUITheme.colorScheme.onSurfaceVariantSummary,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "ColorOS ART+ 图标生成与预览工具",
                            style = COUITheme.textStyles.footnote1,
                            color = COUITheme.colorScheme.onSurfaceVariantSummary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            item {
                SectionCard(rowsFullBleed = true) {
                    LibrarySettingRow(
                        title = "GitHub 仓库",
                        summary = GITHUB_REPO_URL,
                        icon = SettingsIconKind.Link,
                        showArrowRight = true,
                        enabled = !shell.isBusy,
                        onClick = {
                            pickerOpenExternalLink(
                                start = ::startActivity,
                                url = GITHUB_REPO_URL,
                                onError = { msg ->
                                    pickerToastStatus(
                                        message = msg,
                                        postOnUi = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                                        showToast = { Toast.makeText(aboutCtx, it, Toast.LENGTH_SHORT).show() },
                                    )
                                },
                            )
                        },
                    )
                    SectionDivider()
                    LibrarySettingRow(
                        title = "开源协议",
                        summary = "GPL-3.0-or-later",
                        icon = SettingsIconKind.Shield,
                        showArrowRight = true,
                        enabled = !shell.isBusy,
                        onClick = { mainViewModel.updateUpdateUi { it -> it.copy(gplLicenseDialogVisible = (true)) } },
                    )
                    SectionDivider()
                    LibrarySettingRow(
                        title = "检查更新",
                        summary = if (updateUi.isCheckingUpdate) "检查中..." else "当前 $versionName",
                        icon = SettingsIconKind.Grid,
                        showArrowRight = !updateUi.isCheckingUpdate,
                        enabled = !shell.isBusy && !updateUi.isCheckingUpdate,
                        onClick = {
                            pickerCheckForUpdate(
                                isChecking = updateUi.isCheckingUpdate,
                                onCheckingChange = { mainViewModel.updateUpdateUi { v -> v.copy(isCheckingUpdate = (it)) } },
                                onStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                                scope = mainScope,
                                resolveUrl = { pickerResolveUpdateUrl(it, "检查更新", (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) },
                                fetchLatest = ::pickerFetchUpdateBody,
                                currentVersion = versionName,
                                onUpdateAvailable = { info, text ->
                                    mainViewModel.updateUpdateUi { it -> it.copy(updateAvailableInfo = (info)) }
                                    mainViewModel.updateShell { it -> it.copy(statusText = (text)) }
                                },
                                onUpToDate = {
                                    mainViewModel.updateUpdateUi { it -> it.copy(updateUpToDateDialogVisible = (true)) }
                                    mainViewModel.updateShell { v -> v.copy(statusText = (it)) }
                                },
                                onFailed = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                            )
                        },
                    )
                }
            }
            item {
                Text(
                    text = "© 2026 Costben · GPL-3.0-or-later",
                    style = COUITheme.textStyles.footnote1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
            }
        }
    }

    if (updateUi.gplLicenseDialogVisible) {
        val dismissLicense = { mainViewModel.updateUpdateUi { it -> it.copy(gplLicenseDialogVisible = (false)) } }
        ArtDialog(
            title = "开源协议",
            summary = "GPL-3.0-or-later",
            onDismissRequest = dismissLicense,
            negative = DialogButtonBarAction(text = "关闭", onClick = dismissLicense),
            positive = DialogButtonBarAction(
                text = "在 GitHub 查看",
                recommended = true,
                onClick = {
                    dismissLicense()
                    pickerOpenExternalLink(
                        start = ::startActivity,
                        url = GITHUB_LICENSE_URL,
                        onError = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                    )
                },
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp, max = 360.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(COUITheme.colorScheme.secondaryVariant)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
            ) {
                Text(
                    text = remember(aboutCtx) {
                        runCatching {
                            aboutCtx.assets.open(GPL_LICENSE_ASSET_PATH).bufferedReader().readText()
                        }.getOrDefault(GPL_LICENSE_FALLBACK_TEXT)
                    },
                    style = COUITheme.textStyles.footnote2,
                    color = COUITheme.colorScheme.onSurface,
                )
            }
        }
    }

    updateUi.updateAvailableInfo?.let { info ->
        val dismissUpdate = { mainViewModel.updateUpdateUi { it -> it.copy(updateAvailableInfo = (null)) } }
        ArtDialog(
            title = "发现新版本",
            summary = "最新 ${info.tagName} · 当前 $versionName\n点击前往下载页查看更新内容。",
            onDismissRequest = dismissUpdate,
            centered = true,
            negative = DialogButtonBarAction(text = "稍后", onClick = dismissUpdate),
            positive = DialogButtonBarAction(
                text = "前往下载",
                recommended = true,
                onClick = {
                    val url = info.htmlUrl
                    dismissUpdate()
                    pickerOpenExternalLink(
                        start = ::startActivity,
                        url = url,
                        onError = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                    )
                },
            ),
        )
    }

    if (updateUi.updateUpToDateDialogVisible) {
        val dismissUpToDate = { mainViewModel.updateUpdateUi { it -> it.copy(updateUpToDateDialogVisible = (false)) } }
        ArtDialog(
            title = "已是最新版本",
            summary = "当前 $versionName 已是最新，无需更新。",
            onDismissRequest = dismissUpToDate,
            centered = true,
            positive = DialogButtonBarAction(text = "知道了", recommended = true, onClick = dismissUpToDate),
        )
    }
}
