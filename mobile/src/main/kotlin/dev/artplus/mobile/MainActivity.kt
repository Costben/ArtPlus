package dev.artplus.mobile

import android.Manifest
import android.app.AppOpsManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.DocumentsContract
import android.provider.Settings
import android.util.LruCache
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.Locale
import kotlin.math.pow
import org.json.JSONArray
import org.json.JSONObject
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import android.graphics.Color as AndroidColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val apps = mutableStateListOf<AppEntry>()
    private var queryText by mutableStateOf("")
    private var showAllApps by mutableStateOf(false)
    private var selectedPackageName by mutableStateOf<String?>(null)
    private var statusText by mutableStateOf("加载应用列表中...")
    private var packageListPermissionGranted by mutableStateOf(true)
    private var usageAccessGranted by mutableStateOf(false)
    private var outputTreeUri by mutableStateOf<Uri?>(null)
    private var isBusy by mutableStateOf(false)
    private var didRequestAppLoad = false
    private var gptImageMode by mutableStateOf(GptImageMode.Responses)
    private var gptBaseUrl by mutableStateOf(DEFAULT_GPT_BASE_URL)
    private var gptApiKey by mutableStateOf("")

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            refreshPermissionState()
            loadApps()
        }

    private val chooseTreeLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri == null) {
                statusText = "未选择输出目录"
                return@registerForActivityResult
            }
            outputTreeUri = uri
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }
            statusText = "已选择输出目录"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "ArtPlus Mobile"
        loadGptSettings()
        refreshPermissionState()

        setContent {
            val darkMode = isSystemInDarkTheme()

            DisposableEffect(darkMode) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        AndroidColor.TRANSPARENT,
                        AndroidColor.TRANSPARENT,
                    ) { darkMode },
                    navigationBarStyle = SystemBarStyle.auto(
                        AndroidColor.TRANSPARENT,
                        AndroidColor.TRANSPARENT,
                    ) { darkMode },
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
                onDispose {}
            }

            MiuixTheme(
                colors = if (darkMode) darkColorScheme() else lightColorScheme(),
            ) {
                ArtPlusScreen()
            }
        }

        requestDeclaredPermissions()
        requestSpecialPermissionsOnce()
        loadApps()
    }

    override fun onResume() {
        super.onResume()
        val previousPackageListPermission = packageListPermissionGranted
        val previousUsageAccess = usageAccessGranted
        refreshPermissionState()
        if (
            didRequestAppLoad &&
            (apps.isEmpty() ||
                previousPackageListPermission != packageListPermissionGranted ||
                previousUsageAccess != usageAccessGranted)
        ) {
            loadApps()
        }
    }

    @Composable
    private fun ArtPlusScreen() {
        val scrollBehavior = MiuixScrollBehavior()
        val pageBackground = if (isSystemInDarkTheme()) {
            MiuixTheme.colorScheme.background
        } else {
            Color(0xFFF7F7F7)
        }
        val selectedApp by remember {
            derivedStateOf { apps.firstOrNull { it.packageName == selectedPackageName } }
        }
        val filteredApps by remember {
            derivedStateOf {
                val query = queryText.trim().lowercase(Locale.ROOT)
                val scopedApps = if (showAllApps) {
                    apps.toList()
                } else {
                    apps.filter { it.launchable }
                }
                if (query.isEmpty()) {
                    scopedApps
                } else {
                    scopedApps.filter { entry ->
                        entry.label.lowercase(Locale.ROOT).contains(query) ||
                            entry.packageName.lowercase(Locale.ROOT).contains(query)
                    }
                }
            }
        }
        val scopeCount by remember {
            derivedStateOf {
                if (showAllApps) apps.size else apps.count { it.launchable }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = "ArtPlus",
                    scrollBehavior = scrollBehavior,
                )
            },
            popupHost = {},
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(pageBackground)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (!packageListPermissionGranted || !usageAccessGranted) {
                            PermissionCard()
                        }
                        StatusCard(
                            selectedApp = selectedApp,
                        )
                        GenerationCard(selectedApp)
                        AppPickerCard(filteredApps.size, scopeCount)
                    }
                }
                if (filteredApps.isEmpty()) {
                    item {
                        EmptyAppListCard()
                    }
                } else {
                    items(
                        items = filteredApps,
                        key = { it.packageName },
                        contentType = { "app" },
                    ) { entry ->
                        AppRow(
                            entry = entry,
                            selected = entry.packageName == selectedPackageName,
                            onClick = {
                                selectedPackageName = entry.packageName
                                statusText = "已选择: ${entry.label} (${entry.packageName})"
                            },
                        )
                    }
                }
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutputCard()
                        GptSettingsCard()
                    }
                }
            }
        }
    }

    @Composable
    private fun PermissionCard() {
        SectionCard(title = "权限", summary = "启动时会自动请求普通权限；特殊权限需要进入系统设置确认") {
            SettingLine(
                title = "应用列表",
                summary = if (packageListPermissionGranted) "已声明并可读取已安装应用" else "需要允许读取应用列表",
                value = if (packageListPermissionGranted) "已允许" else "待授权",
            )
            Spacer(modifier = Modifier.height(10.dp))
            SettingLine(
                title = "使用情况访问",
                summary = if (usageAccessGranted) "已允许任务/使用情况访问" else "Android 只能在系统设置中授权",
                value = if (usageAccessGranted) "已允许" else "待授权",
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    text = "应用权限",
                    onClick = { openAppPermissionSettings() },
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = "使用情况访问",
                    onClick = { openUsageAccessSettings() },
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    @Composable
    private fun StatusCard(selectedApp: AppEntry?) {
        val statusLabel = if (isBusy) "运行中" else "就绪"

        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (selectedApp == null) {
                    BrandMark(size = 48.dp, text = "UX")
                } else {
                    AppIcon(selectedApp, 48.dp)
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
                            style = MiuixTheme.textStyles.title4,
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = selectedApp?.label ?: "选择一个应用开始生成",
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = selectedApp?.packageName ?: statusText,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

    @Composable
    private fun EmptyAppListCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(16.dp),
        ) {
            Text(
                text = "没有可显示的应用",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "清空搜索词，或在系统设置中允许 ArtPlus 读取应用列表后刷新。",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                text = "刷新应用列表",
                onClick = { loadApps() },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    @Composable
    private fun GenerationCard(selectedApp: AppEntry?) {
        val canRun = selectedApp != null && !isBusy
        SectionCard(title = "生成任务", summary = "生成 ART+ 图标包，Root 写入固定 data 分区") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { generateSelected(installWithRoot = false, useGpt = false) },
                    enabled = canRun,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(
                        text = "本地生成",
                        style = MiuixTheme.textStyles.button,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Button(
                    onClick = { generateSelected(installWithRoot = false, useGpt = true) },
                    enabled = canRun,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "GPT生成",
                        style = MiuixTheme.textStyles.button,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    text = "本地写入",
                    onClick = { generateSelected(installWithRoot = true, useGpt = false) },
                    enabled = canRun,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = "GPT写入",
                    onClick = { generateSelected(installWithRoot = true, useGpt = true) },
                    enabled = canRun,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    @Composable
    private fun GptSettingsCard() {
        SectionCard(title = "GPT Image 2", summary = "响应模式走 Codex 能力；接口模式直接调用 Base URL + API key") {
            SegmentedModeControl()
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "当前: ${gptImageMode.label}",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = gptBaseUrl,
                onValueChange = {
                    gptBaseUrl = it
                    saveGptSettings()
                },
                label = "Base URL",
                useLabelAsPlaceholder = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = gptApiKey,
                onValueChange = {
                    gptApiKey = it
                    saveGptSettings()
                },
                label = "API key",
                useLabelAsPlaceholder = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    @Composable
    private fun OutputCard() {
        SectionCard(title = "输出与写入", summary = "普通导出使用系统目录选择器，Root 写入只使用 data 分区路径") {
            SettingLine(
                title = "Root 目标",
                summary = "/data/oplus/uxicons/{package}",
                value = "data",
            )
            Spacer(modifier = Modifier.height(10.dp))
            SettingLine(
                title = "外部导出",
                summary = if (outputTreeUri == null) "未选择时仅保存在应用私有目录" else "生成后同步复制到你选择的目录",
                value = if (outputTreeUri == null) "未选择" else "已启用",
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                text = "选择输出目录",
                onClick = { chooseTreeLauncher.launch(null) },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    @Composable
    private fun AppPickerCard(filteredCount: Int, totalCount: Int) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "应用",
                        style = MiuixTheme.textStyles.title4,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "$filteredCount/$totalCount",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    SegmentOption(
                        label = "启动器",
                        selected = !showAllApps,
                        modifier = Modifier.weight(1f),
                    ) {
                        showAllApps = false
                        queryText = ""
                    }
                    SegmentOption(
                        label = "全部",
                        selected = showAllApps,
                        modifier = Modifier.weight(1f),
                    ) {
                        showAllApps = true
                        queryText = ""
                    }
                }
                TextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    label = "搜索应用或包名",
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    @Composable
    private fun AppRow(entry: AppEntry, selected: Boolean, onClick: () -> Unit) {
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

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .padding(start = 2.dp)
                    .width(6.dp)
                    .height(24.dp)
                    .align(Alignment.CenterVertically)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (selected) {
                            MiuixTheme.colorScheme.primaryVariant
                        } else {
                            MiuixTheme.colorScheme.secondaryContainer
                        },
                    ),
            )
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp, bottom = 6.dp),
                insideMargin = PaddingValues(start = 10.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                colors = CardDefaults.defaultColors(
                    color = if (selected) {
                        MiuixTheme.colorScheme.primaryVariant
                    } else {
                        MiuixTheme.colorScheme.surfaceContainer
                    },
                ),
                showIndication = true,
                onClick = onClick,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppIcon(entry, 40.dp)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = entry.label,
                            style = MiuixTheme.textStyles.body1,
                            color = titleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = entry.packageName,
                            style = MiuixTheme.textStyles.footnote1,
                            color = summaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (selected || !entry.launchable) {
                        Text(
                            text = if (selected) "已选" else "应用",
                            style = MiuixTheme.textStyles.footnote1,
                            color = summaryColor,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SectionCard(title: String, summary: String, content: @Composable () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(16.dp),
        ) {
            Text(
                text = title,
                style = MiuixTheme.textStyles.title4,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = summary,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }

    @Composable
    private fun SegmentedModeControl() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SegmentOption(
                label = "响应模式",
                selected = gptImageMode == GptImageMode.Responses,
                modifier = Modifier.weight(1f),
            ) {
                gptImageMode = GptImageMode.Responses
                saveGptSettings()
            }
            SegmentOption(
                label = "接口模式",
                selected = gptImageMode == GptImageMode.Images,
                modifier = Modifier.weight(1f),
            ) {
                gptImageMode = GptImageMode.Images
                saveGptSettings()
            }
        }
    }

    @Composable
    private fun SegmentOption(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
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
                .clickable(enabled = !isBusy, onClick = onClick)
                .padding(vertical = 10.dp, horizontal = 12.dp),
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
    private fun SettingLine(title: String, summary: String, value: String) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
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
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            MetricPill(label = value)
        }
    }

    @Composable
    private fun AppIcon(entry: AppEntry, size: Dp) {
        var bitmap by remember(entry.iconKey) {
            mutableStateOf(getCachedAppIcon(entry.iconKey))
        }
        val imageBitmap = remember(bitmap) { bitmap?.asImageBitmap() }

        LaunchedEffect(entry.iconKey) {
            if (bitmap == null) {
                bitmap = loadCachedAppIcon(entry)
            }
        }

        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
                .background(MiuixTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            if (imageBitmap == null) {
                Text(
                    text = entry.label.firstOrNull()?.uppercaseChar()?.toString() ?: "#",
                    style = MiuixTheme.textStyles.title4,
                    color = MiuixTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                )
            } else {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    modifier = Modifier.size(size),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }

    private fun getCachedAppIcon(key: String): Bitmap? =
        synchronized(appIconCache) { appIconCache.get(key) }

    private suspend fun loadCachedAppIcon(entry: AppEntry): Bitmap? =
        withContext(Dispatchers.IO) {
            val cached = synchronized(appIconCache) {
                appIconCache.get(entry.iconKey)
            }
            if (cached != null) {
                return@withContext cached
            }

            val bitmap = runCatching { loadAppIconBitmap(entry) }.getOrNull() ?: return@withContext null

            synchronized(appIconCache) {
                appIconCache.put(entry.iconKey, bitmap)
            }
            bitmap
        }

    private fun loadAppIconBitmap(entry: AppEntry): Bitmap =
        drawDrawable(entry.applicationInfo.loadIcon(packageManager), ICON_CACHE_SIZE, ICON_CACHE_SIZE, transparent = true)
            .also { it.prepareToDraw() }

    @Composable
    private fun BrandMark(size: Dp, text: String) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(16.dp))
                .background(MiuixTheme.colorScheme.primaryVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MiuixTheme.textStyles.title4,
                color = MiuixTheme.colorScheme.onPrimaryVariant,
                maxLines = 1,
            )
        }
    }

    @Composable
    private fun MetricPill(label: String, modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(999.dp))
                .background(MiuixTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    @Composable
    private fun StatusDot(active: Boolean) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    if (active) {
                        MiuixTheme.colorScheme.primaryVariant
                    } else {
                        MiuixTheme.colorScheme.secondaryContainer
                    },
                ),
        )
    }

    private fun loadApps() {
        didRequestAppLoad = true
        Thread {
            val pm = packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
            val launchablePackages = queryLaunchablePackages(pm, intent)
            val installedApps = getInstalledApplications(pm)
            val entries = installedApps.mapNotNull { info ->
                val packageName = info.packageName ?: return@mapNotNull null
                val label = runCatching { info.loadLabel(pm)?.toString() }
                    .getOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?: packageName
                AppEntry(
                    label = label,
                    packageName = packageName,
                    applicationInfo = info,
                    launchable = packageName in launchablePackages,
                    iconKey = "${packageName}:${info.uid}:${info.sourceDir}",
                )
            }
                .sortedWith(
                    compareByDescending<AppEntry> { it.launchable }
                        .thenBy { it.label.lowercase(Locale.ROOT) }
                        .thenBy { it.packageName },
                )
            preloadAppIcons(entries)
            runOnUiThread {
                refreshPermissionState()
                apps.clear()
                apps.addAll(entries)
                statusText = when {
                    entries.isEmpty() -> "没有读取到应用。请确认已允许读取应用列表。"
                    !packageListPermissionGranted -> "读取到 ${apps.size} 个应用，但应用列表权限状态异常。"
                    else -> "共 ${apps.size} 个应用，其中 ${launchablePackages.size} 个有启动器入口。"
                }
            }
        }.start()
    }

    private fun preloadAppIcons(entries: List<AppEntry>) {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
        entries.asSequence()
            .filter { it.launchable }
            .take(PRELOAD_ICON_COUNT)
            .forEach { entry ->
                if (getCachedAppIcon(entry.iconKey) != null) {
                    return@forEach
                }
                val bitmap = runCatching { loadAppIconBitmap(entry) }.getOrNull() ?: return@forEach
                synchronized(appIconCache) {
                    if (appIconCache.get(entry.iconKey) == null) {
                        appIconCache.put(entry.iconKey, bitmap)
                    }
                }
            }
    }

    private fun refreshPermissionState() {
        packageListPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            packageManager.checkPermission(Manifest.permission.QUERY_ALL_PACKAGES, packageName) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        usageAccessGranted = hasUsageAccess()
    }

    private fun requestDeclaredPermissions() {
        val permissions = mutableListOf<String>()
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            packageManager.checkPermission(Manifest.permission.QUERY_ALL_PACKAGES, packageName) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissions += Manifest.permission.QUERY_ALL_PACKAGES
        }
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun requestSpecialPermissionsOnce() {
        if (usageAccessGranted) {
            return
        }
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if (prefs.getBoolean(PREF_USAGE_PERMISSION_PROMPTED, false)) {
            return
        }
        prefs.edit().putBoolean(PREF_USAGE_PERMISSION_PROMPTED, true).apply()
        window.decorView.post {
            if (!hasUsageAccess()) {
                openUsageAccessSettings()
            }
        }
    }

    private fun openAppPermissionSettings() {
        runCatching {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", packageName, null)),
            )
        }.onFailure {
            statusText = "无法打开应用权限设置: ${it.message ?: it.javaClass.simpleName}"
        }
    }

    private fun openUsageAccessSettings() {
        runCatching {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }.onFailure {
            statusText = "无法打开使用情况访问设置: ${it.message ?: it.javaClass.simpleName}"
        }
    }

    private fun hasUsageAccess(): Boolean {
        val appOps = getSystemService(AppOpsManager::class.java) ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName,
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName,
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    @Suppress("DEPRECATION")
    private fun queryLaunchablePackages(pm: PackageManager, intent: Intent): Set<String> {
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
            )
        } else {
            pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        }
        return resolveInfos
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    @Suppress("DEPRECATION")
    private fun getInstalledApplications(pm: PackageManager): List<ApplicationInfo> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
            )
        } else {
            pm.getInstalledApplications(PackageManager.MATCH_ALL)
        }

    private fun loadGptSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        gptImageMode = GptImageMode.fromValue(prefs.getString(PREF_GPT_MODE, GptImageMode.Responses.value))
        gptBaseUrl = prefs.getString(PREF_GPT_BASE_URL, DEFAULT_GPT_BASE_URL) ?: DEFAULT_GPT_BASE_URL
        gptApiKey = prefs.getString(PREF_GPT_API_KEY, "") ?: ""
    }

    private fun saveGptSettings() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(PREF_GPT_MODE, gptImageMode.value)
            .putString(PREF_GPT_BASE_URL, gptBaseUrl)
            .putString(PREF_GPT_API_KEY, gptApiKey)
            .apply()
    }

    private fun generateSelected(installWithRoot: Boolean, useGpt: Boolean) {
        val entry = apps.firstOrNull { it.packageName == selectedPackageName }
        if (entry == null) {
            statusText = "先选择一个应用"
            return
        }
        if (useGpt && gptApiKey.trim().isEmpty()) {
            statusText = "先填写 GPT API key"
            return
        }
        if (useGpt && gptBaseUrl.trim().isEmpty()) {
            statusText = "先填写 GPT Base URL"
            return
        }
        if (isBusy) {
            return
        }

        isBusy = true
        statusText = if (useGpt) {
            "GPT处理中: ${entry.packageName}"
        } else {
            "处理中: ${entry.packageName}"
        }
        Thread {
            try {
                val outDir = generateArtPlusPackage(entry, useGpt)
                if (outputTreeUri != null) {
                    exportToTree(outDir)
                }
                if (installWithRoot) {
                    installWithRoot(outDir, entry.packageName)
                    status("已生成${if (useGpt) "GPT版" else "本地版"}并尝试 Root 写入: ${entry.packageName}")
                } else {
                    status("已生成${if (useGpt) "GPT版" else "本地版"}: ${outDir.absolutePath}")
                }
            } catch (error: Exception) {
                status("失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                runOnUiThread { isBusy = false }
            }
        }.start()
    }

    private fun generateArtPlusPackage(app: AppEntry, useGpt: Boolean): File {
        val base = getExternalFilesDir("ArtPlus") ?: File(filesDir, "ArtPlus")
        val outDir = File(base, app.packageName)
        ensureCleanDir(outDir)

        val icon = app.applicationInfo.loadIcon(packageManager)
        val localRecfg: Bitmap
        val localRecbg: Bitmap
        if (icon is AdaptiveIconDrawable) {
            localRecfg = drawDrawable(icon.foreground, SIZE_1X1, SIZE_1X1, transparent = true)
            localRecbg = drawDrawable(
                icon.background ?: ColorDrawable(AndroidColor.WHITE),
                SIZE_1X1,
                SIZE_1X1,
                transparent = false,
            )
        } else {
            localRecfg = drawDrawable(icon, SIZE_1X1, SIZE_1X1, transparent = true)
            localRecbg = solidBitmap(SIZE_1X1, SIZE_1X1, AndroidColor.WHITE)
        }

        val sourceIcon = drawDrawable(icon, GPT_SOURCE_SIZE, GPT_SOURCE_SIZE, transparent = false)
        val layers = if (useGpt) {
            generateGptLayers(sourceIcon, localRecfg, localRecbg)
        } else {
            IconLayers(localRecfg, localRecbg)
        }
        val recfg = normalizeForegroundSubjectSize(layers.recfg)
        val recbg = layers.recbg

        savePng(recbg, File(outDir, "recbg.png"))
        savePng(recfg, File(outDir, "recfg.png"))
        savePng(resizeBitmap(recbg, SIZE_1X2[0], SIZE_1X2[1]), File(outDir, "recbg_1x2.png"))
        savePng(resizeBitmap(recbg, SIZE_2X1[0], SIZE_2X1[1]), File(outDir, "recbg_2x1.png"))
        savePng(resizeBitmap(recbg, SIZE_2X2, SIZE_2X2), File(outDir, "recbg_2x2.png"))

        val recfg1x2 = centerOnCanvas(recfg, SIZE_1X2[0], SIZE_1X2[1])
        val recfg2x1 = centerOnCanvas(recfg, SIZE_2X1[0], SIZE_2X1[1])
        val recfg2x2 = centerOnCanvas(recfg, SIZE_2X2, SIZE_2X2)
        savePng(recfg1x2, File(outDir, "recfg_1x2.png"))
        savePng(recfg2x1, File(outDir, "recfg_2x1.png"))
        savePng(recfg2x2, File(outDir, "recfg_2x2.png"))

        val bgColor = sampleColor(recbg)
        savePng(recolorAlpha(recfg, bgColor), File(outDir, "rec_night.png"))
        savePng(recolorAlpha(recfg1x2, bgColor), File(outDir, "rec_night_1x2.png"))
        savePng(recolorAlpha(recfg2x1, bgColor), File(outDir, "rec_night_2x1.png"))
        savePng(recolorAlpha(recfg2x2, bgColor), File(outDir, "rec_night_2x2.png"))

        savePng(monochromeAlpha(recfg), File(outDir, "monochrome.png"))
        savePng(monochromeAlpha(recfg1x2), File(outDir, "monochrome_1x2.png"))
        savePng(monochromeAlpha(recfg2x1), File(outDir, "monochrome_2x1.png"))
        savePng(monochromeAlpha(recfg2x2), File(outDir, "monochrome_2x2.png"))

        savePng(adjustColor(recfg, 1.3f, 1.0f), File(outDir, "day.png"))
        savePng(adjustColor(recfg, 0.9f, 0.9f), File(outDir, "nsd.png"))
        savePng(adjustColor(recfg, 0.9f, 1.05f), File(outDir, "mat.png"))
        savePng(adjustColor(recfg, 0.7f, 0.95f), File(outDir, "peb.png"))
        return outDir
    }

    private fun generateGptLayers(sourceIcon: Bitmap, localRecfg: Bitmap, localRecbg: Bitmap): IconLayers {
        val chromaKey = chooseChromaKey(sourceIcon)
        val chromaHex = "#%02x%02x%02x".format(
            AndroidColor.red(chromaKey),
            AndroidColor.green(chromaKey),
            AndroidColor.blue(chromaKey),
        )
        val foregroundPrompt = buildForegroundPrompt(chromaHex)
        val backgroundPrompt = buildBackgroundPrompt()

        status("GPT生成前景...")
        val rawForeground = gptEditImage(sourceIcon, foregroundPrompt, "opaque")
        status("GPT生成背景...")
        val rawBackground = gptEditImage(sourceIcon, backgroundPrompt, "opaque")

        val recbg = Bitmap.createScaledBitmap(rawBackground, SIZE_1X1, SIZE_1X1, true)
        val recfg = when {
            hasRealAlpha(rawForeground) -> {
                Bitmap.createScaledBitmap(rawForeground, SIZE_1X1, SIZE_1X1, true)
            }
            else -> {
                val keyed = removeChromaKeyBackground(rawForeground, chromaKey)
                if (alphaCoverage(keyed) in 0.002..0.95) {
                    Bitmap.createScaledBitmap(keyed, SIZE_1X1, SIZE_1X1, true)
                } else {
                    localRecfg
                }
            }
        }
        return IconLayers(recfg, recbg)
    }

    private fun gptEditImage(source: Bitmap, prompt: String, background: String): Bitmap =
        when (gptImageMode) {
            GptImageMode.Responses -> responsesEditImage(source, prompt, background)
            GptImageMode.Images -> imagesEditImage(source, prompt, background)
        }

    private fun responsesEditImage(source: Bitmap, prompt: String, background: String): Bitmap {
        val body = JSONObject()
            .put("model", GPT_RESPONSE_MODEL)
            .put(
                "input",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put(
                            "content",
                            JSONArray()
                                .put(JSONObject().put("type", "input_text").put("text", prompt))
                                .put(
                                    JSONObject()
                                        .put("type", "input_image")
                                        .put("image_url", bitmapToDataUrl(source)),
                                ),
                        ),
                ),
            )
            .put(
                "tools",
                JSONArray().put(
                    JSONObject()
                        .put("type", "image_generation")
                        .put("size", "auto")
                        .put("quality", GPT_IMAGE_QUALITY)
                        .put("background", background)
                        .put("output_format", "png"),
                ),
            )
            .put("tool_choice", JSONObject().put("type", "image_generation"))
            .put("stream", true)

        val response = postJson(normalizeResponsesUrl(gptBaseUrl), body)
        val parsed = if (response.trimStart().startsWith("data:") || response.trimStart().startsWith("event:")) {
            parseResponsesStream(response)
        } else {
            JSONObject(response)
        }
        return decodeBitmap(extractImageBytes(parsed))
    }

    private fun imagesEditImage(source: Bitmap, prompt: String, background: String): Bitmap {
        val boundary = "----ArtPlusMobile${UUID.randomUUID().toString().replace("-", "")}"
        val pngBytes = bitmapToPngBytes(source)
        val body = ByteArrayOutputStream()

        fun field(name: String, value: String) {
            body.writeString("--$boundary\r\n")
            body.writeString("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
            body.writeString(value)
            body.writeString("\r\n")
        }

        field("model", GPT_IMAGE_MODEL)
        field("prompt", prompt)
        field("size", GPT_IMAGE_SIZE)
        field("quality", GPT_IMAGE_QUALITY)
        field("background", background)
        field("output_format", "png")
        body.writeString("--$boundary\r\n")
        body.writeString("Content-Disposition: form-data; name=\"image\"; filename=\"artplus_source_icon.png\"\r\n")
        body.writeString("Content-Type: image/png\r\n\r\n")
        body.write(pngBytes)
        body.writeString("\r\n--$boundary--\r\n")

        val response = postBytes(
            urlText = normalizeImagesEditUrl(gptBaseUrl),
            body = body.toByteArray(),
            contentType = "multipart/form-data; boundary=$boundary",
        )
        return decodeBitmap(extractImageBytes(JSONObject(response)))
    }

    private fun postJson(urlText: String, body: JSONObject): String =
        postBytes(urlText, body.toString().toByteArray(Charsets.UTF_8), "application/json", accept = "text/event-stream, application/json")

    private fun postBytes(
        urlText: String,
        body: ByteArray,
        contentType: String,
        accept: String = "application/json",
    ): String {
        val connection = (URL(urlText).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = GPT_CONNECT_TIMEOUT_MS
            readTimeout = GPT_READ_TIMEOUT_MS
            doOutput = true
            setRequestProperty("Accept", accept)
            setRequestProperty("Authorization", "Bearer ${gptApiKey.trim()}")
            setRequestProperty("Content-Type", contentType)
            setRequestProperty("Content-Length", body.size.toString())
        }
        try {
            connection.outputStream.use { it.write(body) }
            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }
            val text = stream.bufferedReader().use { it.readText() }
            if (connection.responseCode !in 200..299) {
                error("GPT HTTP ${connection.responseCode}: ${text.take(300)}")
            }
            return text
        } finally {
            connection.disconnect()
        }
    }

    private fun downloadBytes(urlText: String): ByteArray {
        val connection = (URL(urlText).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = GPT_CONNECT_TIMEOUT_MS
            readTimeout = GPT_READ_TIMEOUT_MS
            if (gptApiKey.trim().isNotEmpty()) {
                setRequestProperty("Authorization", "Bearer ${gptApiKey.trim()}")
            }
        }
        try {
            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }
            val bytes = stream.use { it.readBytes() }
            if (connection.responseCode !in 200..299) {
                error("下载GPT图片失败 HTTP ${connection.responseCode}: ${String(bytes).take(300)}")
            }
            return bytes
        } finally {
            connection.disconnect()
        }
    }

    private fun parseResponsesStream(text: String): JSONObject {
        val output = JSONArray()
        var response: JSONObject? = null
        for (block in text.split("\n\n")) {
            val data = block.lineSequence()
                .map { it.trimEnd() }
                .filter { it.startsWith("data:") }
                .joinToString("\n") { it.removePrefix("data:").trimStart() }
                .trim()
            if (data.isEmpty() || data == "[DONE]") {
                continue
            }
            val event = runCatching { JSONObject(data) }.getOrNull() ?: continue
            event.optJSONObject("response")?.let {
                response = it
                val existing = it.optJSONArray("output")
                if (existing != null) {
                    for (i in 0 until existing.length()) {
                        output.put(existing.get(i))
                    }
                }
            }
            val item = event.optJSONObject("item")
            if (
                item != null &&
                (event.optString("type") == "response.output_item.done" ||
                    event.optString("type") == "response.output_item.added")
            ) {
                output.put(item)
            }
            if (event.optString("type") == "response.image_generation_call.partial_image") {
                val partial = event.optString("partial_image_b64")
                if (partial.isNotBlank()) {
                    output.put(
                        JSONObject()
                            .put("type", "image_generation_call")
                            .put("image_base64", partial),
                    )
                }
            }
        }
        return (response ?: JSONObject()).put("output", output)
    }

    private fun extractImageBytes(json: JSONObject): ByteArray {
        json.optJSONArray("output")?.let { output ->
            findImageBytes(output)?.let { return it }
        }
        json.optJSONArray("data")?.let { data ->
            findImageBytes(data)?.let { return it }
        }
        findImageBytes(JSONArray().put(json))?.let { return it }
        error("GPT响应没有图片数据")
    }

    private fun findImageBytes(items: JSONArray): ByteArray? {
        for (i in 0 until items.length()) {
            val item = items.optJSONObject(i) ?: continue
            decodeImageReference(item.opt("b64_json"))?.let { return it }
            decodeImageReference(item.opt("b64"))?.let { return it }
            decodeImageReference(item.opt("image_base64"))?.let { return it }
            decodeImageReference(item.opt("base64"))?.let { return it }
            decodeImageReference(item.opt("result"))?.let { return it }
            decodeImageReference(item.opt("url"))?.let { return it }
            decodeImageReference(item.opt("imageUrl"))?.let { return it }
            decodeImageReference(item.opt("remoteImageUrl"))?.let { return it }
            val imageUrl = item.optJSONObject("image_url")
            if (imageUrl != null) {
                decodeImageReference(imageUrl.opt("url"))?.let { return it }
            }
        }
        return null
    }

    private fun decodeImageReference(value: Any?): ByteArray? {
        val text = (value as? String)?.trim().orEmpty()
        if (text.isEmpty()) {
            return null
        }
        if (text.startsWith("http://") || text.startsWith("https://")) {
            return downloadBytes(text)
        }
        val b64 = if (text.startsWith("data:image/")) {
            text.substringAfter("base64,", "")
        } else {
            text
        }.replace("\\s".toRegex(), "")
        if (b64.length < 128) {
            return null
        }
        return runCatching { Base64.decode(b64, Base64.DEFAULT) }.getOrNull()
    }

    private fun decodeBitmap(bytes: ByteArray): Bitmap {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        return bitmap ?: error("GPT返回的图片无法解码")
    }

    private fun bitmapToDataUrl(bitmap: Bitmap): String =
        "data:image/png;base64,${Base64.encodeToString(bitmapToPngBytes(bitmap), Base64.NO_WRAP)}"

    private fun bitmapToPngBytes(bitmap: Bitmap): ByteArray {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        return output.toByteArray()
    }

    private fun ByteArrayOutputStream.writeString(value: String) {
        write(value.toByteArray(Charsets.UTF_8))
    }

    private fun normalizeResponsesUrl(baseUrl: String): String {
        val normalized = baseUrl.trim().trimEnd('/')
        return when {
            normalized.endsWith("/responses") -> normalized
            normalized.endsWith("/v1") -> "$normalized/responses"
            "/v1/" in "$normalized/" -> "$normalized/responses"
            else -> "$normalized/v1/responses"
        }
    }

    private fun normalizeImagesEditUrl(baseUrl: String): String {
        val normalized = baseUrl.trim().trimEnd('/')
        return when {
            normalized.endsWith("/images/edits") -> normalized
            normalized.endsWith("/v1") -> "$normalized/images/edits"
            "/v1/" in "$normalized/" -> "$normalized/images/edits"
            else -> "$normalized/v1/images/edits"
        }
    }

    private fun buildForegroundPrompt(chromaHex: String): String =
        "Keep only the app icon main subject/logo. Remove the original background. " +
            "Scale the subject/logo so its visible bounding box is about 70% of the final square canvas. " +
            "Place the remaining subject on a perfectly flat solid $chromaHex chroma-key background. " +
            "The chroma-key background must be one uniform color, with no checkerboard, no transparency preview pattern, " +
            "no shadows, no gradients, no texture, and no lighting variation. " +
            "Do not use $chromaHex anywhere in the subject/logo. Preserve the subject shape and colors."

    private fun buildBackgroundPrompt(): String =
        "Remove the app icon main subject/logo. Rebuild only the clean original background plate. No logo, no text, no symbol."

    private fun drawDrawable(
        drawable: Drawable?,
        width: Int,
        height: Int,
        transparent: Boolean,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(if (transparent) AndroidColor.TRANSPARENT else AndroidColor.WHITE)
        if (drawable != null) {
            val copy = drawable.constantState?.newDrawable()?.mutate() ?: drawable.mutate()
            copy.setBounds(0, 0, width, height)
            copy.draw(canvas)
        }
        return bitmap
    }

    private fun solidBitmap(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawColor(color)
        return bitmap
    }

    private fun resizeBitmap(source: Bitmap, width: Int, height: Int): Bitmap =
        Bitmap.createScaledBitmap(source, width, height, true)

    private fun centerOnCanvas(source: Bitmap, width: Int, height: Int): Bitmap {
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(AndroidColor.TRANSPARENT)
        val x = (width - source.width) / 2f
        val y = (height - source.height) / 2f
        canvas.drawBitmap(source, x, y, null)
        return out
    }

    private fun normalizeForegroundSubjectSize(source: Bitmap): Bitmap {
        val bounds = alphaBounds(source, 8) ?: return source
        val currentMax = maxOf(bounds.width(), bounds.height()).toFloat()
        if (currentMax <= 0f) {
            return source
        }
        val targetMax = source.width * FOREGROUND_SUBJECT_MAX_SIDE_RATIO
        val scale = targetMax / currentMax
        if (scale in 0.97f..1.03f) {
            return source
        }
        val scaledWidth = (source.width * scale).toInt().coerceAtLeast(1)
        val scaledHeight = (source.height * scale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
        val scaledBounds = alphaBounds(scaled, 8) ?: return source
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(AndroidColor.TRANSPARENT)
        val dx = source.width / 2f - (scaledBounds.left + scaledBounds.width() / 2f)
        val dy = source.height / 2f - (scaledBounds.top + scaledBounds.height() / 2f)
        canvas.drawBitmap(scaled, dx, dy, null)
        return out
    }

    private fun alphaBounds(source: Bitmap, threshold: Int): Bounds? {
        var left = source.width
        var top = source.height
        var right = -1
        var bottom = -1
        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                if (AndroidColor.alpha(source.getPixel(x, y)) > threshold) {
                    if (x < left) left = x
                    if (x > right) right = x
                    if (y < top) top = y
                    if (y > bottom) bottom = y
                }
            }
        }
        return if (right >= left && bottom >= top) {
            Bounds(left, top, right + 1, bottom + 1)
        } else {
            null
        }
    }

    private fun hasRealAlpha(source: Bitmap): Boolean {
        var transparent = 0
        var samples = 0
        for (y in 0 until source.height step maxOf(1, source.height / 128)) {
            for (x in 0 until source.width step maxOf(1, source.width / 128)) {
                samples++
                if (AndroidColor.alpha(source.getPixel(x, y)) < 8) {
                    transparent++
                }
            }
        }
        return samples > 0 && transparent.toDouble() / samples.toDouble() >= 0.05
    }

    private fun alphaCoverage(source: Bitmap): Double {
        var visible = 0
        val total = source.width * source.height
        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                if (AndroidColor.alpha(source.getPixel(x, y)) > 8) {
                    visible++
                }
            }
        }
        return if (total == 0) 0.0 else visible.toDouble() / total.toDouble()
    }

    private fun removeChromaKeyBackground(source: Bitmap, keyColor: Int): Bitmap {
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                val pixel = source.getPixel(x, y)
                val distance = colorDistance(pixel, keyColor)
                val originalAlpha = AndroidColor.alpha(pixel)
                val alpha = when {
                    distance <= CHROMA_TRANSPARENT_THRESHOLD -> 0
                    distance >= CHROMA_OPAQUE_THRESHOLD -> originalAlpha
                    else -> {
                        val factor = (distance - CHROMA_TRANSPARENT_THRESHOLD) /
                            (CHROMA_OPAQUE_THRESHOLD - CHROMA_TRANSPARENT_THRESHOLD)
                        (factor.coerceIn(0.0, 1.0) * originalAlpha).toInt()
                    }
                }
                out.setPixel(x, y, (alpha shl 24) or (pixel and 0x00ffffff))
            }
        }
        return out
    }

    private fun chooseChromaKey(source: Bitmap): Int {
        var best = CHROMA_KEY_CANDIDATES.first()
        var bestScore = -1.0
        for (candidate in CHROMA_KEY_CANDIDATES) {
            var minDistance = Double.MAX_VALUE
            for (y in 0 until source.height step maxOf(1, source.height / 64)) {
                for (x in 0 until source.width step maxOf(1, source.width / 64)) {
                    val pixel = source.getPixel(x, y)
                    if (AndroidColor.alpha(pixel) >= 64) {
                        minDistance = minOf(minDistance, colorDistance(candidate, pixel))
                    }
                }
            }
            if (minDistance > bestScore) {
                best = candidate
                bestScore = minDistance
            }
        }
        return best
    }

    private fun colorDistance(a: Int, b: Int): Double {
        val dr = AndroidColor.red(a) - AndroidColor.red(b)
        val dg = AndroidColor.green(a) - AndroidColor.green(b)
        val db = AndroidColor.blue(a) - AndroidColor.blue(b)
        return kotlin.math.sqrt((dr * dr + dg * dg + db * db).toDouble())
    }

    private fun recolorAlpha(source: Bitmap, color: Int): Bitmap {
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val rgb = color and 0x00ffffff
        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                val alpha = AndroidColor.alpha(source.getPixel(x, y))
                out.setPixel(x, y, (alpha shl 24) or rgb)
            }
        }
        return out
    }

    private fun monochromeAlpha(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val sourcePixels = IntArray(width * height)
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
        val lumas = sourcePixels
            .filter { AndroidColor.alpha(it) > 8 }
            .map { luma(it) }
            .toMutableList()
        val low = percentile(lumas, 0.02)
        val high = percentile(lumas, 0.98)
        val hasRange = high - low >= 12
        val outPixels = IntArray(sourcePixels.size)

        for (i in sourcePixels.indices) {
            val pixel = sourcePixels[i]
            val alpha = AndroidColor.alpha(pixel)
            if (alpha <= 0) {
                outPixels[i] = AndroidColor.TRANSPARENT
                continue
            }
            val maskAlpha = if (hasRange) {
                val normalized = ((luma(pixel) - low).toDouble() / (high - low).toDouble())
                    .coerceIn(0.0, 1.0)
                    .pow(MONO_ALPHA_GAMMA)
                MONO_ALPHA_MIN + normalized * (MONO_ALPHA_MAX - MONO_ALPHA_MIN)
            } else {
                MONO_ALPHA_MAX.toDouble()
            }
            val outAlpha = ((alpha / 255.0) * maskAlpha).toInt().coerceIn(0, 255)
            outPixels[i] = (outAlpha shl 24) or 0x00ffffff
        }

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun percentile(values: MutableList<Int>, ratio: Double): Int {
        if (values.isEmpty()) {
            return 0
        }
        values.sort()
        val index = ((values.size - 1) * ratio)
            .toInt()
            .coerceIn(0, values.size - 1)
        return values[index]
    }

    private fun luma(pixel: Int): Int =
        (AndroidColor.red(pixel) * 0.299 +
            AndroidColor.green(pixel) * 0.587 +
            AndroidColor.blue(pixel) * 0.114).toInt()

    private fun sampleColor(bitmap: Bitmap): Int {
        val center = bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
        if (
            AndroidColor.alpha(center) > 32 &&
            AndroidColor.red(center) + AndroidColor.green(center) + AndroidColor.blue(center) >= 120
        ) {
            return AndroidColor.rgb(
                AndroidColor.red(center),
                AndroidColor.green(center),
                AndroidColor.blue(center),
            )
        }

        var red = 0L
        var green = 0L
        var blue = 0L
        var count = 0L
        for (y in 0 until bitmap.height step 8) {
            for (x in 0 until bitmap.width step 8) {
                val pixel = bitmap.getPixel(x, y)
                if (AndroidColor.alpha(pixel) >= 128) {
                    red += AndroidColor.red(pixel)
                    green += AndroidColor.green(pixel)
                    blue += AndroidColor.blue(pixel)
                    count++
                }
            }
        }
        if (count == 0L) {
            return AndroidColor.rgb(216, 224, 253)
        }
        return AndroidColor.rgb((red / count).toInt(), (green / count).toInt(), (blue / count).toInt())
    }

    private fun adjustColor(source: Bitmap, saturation: Float, brightness: Float): Bitmap {
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val saturationMatrix = ColorMatrix()
        saturationMatrix.setSaturation(saturation)
        val brightnessMatrix = ColorMatrix(
            floatArrayOf(
                brightness, 0f, 0f, 0f, 0f,
                0f, brightness, 0f, 0f, 0f,
                0f, 0f, brightness, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            ),
        )
        saturationMatrix.postConcat(brightnessMatrix)
        paint.colorFilter = ColorMatrixColorFilter(saturationMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return out
    }

    private fun savePng(bitmap: Bitmap, file: File) {
        val parent = file.parentFile
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            error("无法创建目录: $parent")
        }
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    private fun ensureCleanDir(dir: File) {
        if (!dir.exists()) {
            dir.mkdirs()
            return
        }
        dir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".png") }
            ?.forEach { it.delete() }
    }

    private fun exportToTree(packageDir: File) {
        val treeUri = outputTreeUri ?: return
        val rootDoc = DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri),
        )
        var packageDoc = findChild(treeUri, rootDoc, packageDir.name)
        if (packageDoc == null) {
            packageDoc = DocumentsContract.createDocument(
                contentResolver,
                rootDoc,
                DocumentsContract.Document.MIME_TYPE_DIR,
                packageDir.name,
            )
        }
        if (packageDoc == null) {
            error("无法创建输出目录")
        }

        val files = packageDir.listFiles { _, name -> name.endsWith(".png") } ?: return
        for (file in files) {
            findChild(treeUri, packageDoc, file.name)?.let {
                DocumentsContract.deleteDocument(contentResolver, it)
            }
            val doc = DocumentsContract.createDocument(contentResolver, packageDoc, "image/png", file.name)
                ?: error("无法创建文件: ${file.name}")
            FileInputStream(file).use { input ->
                contentResolver.openOutputStream(doc, "w").useRequired { output ->
                    copyStream(input, output)
                }
            }
        }
    }

    private fun findChild(treeUri: Uri, parentDoc: Uri, displayName: String): Uri? {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            DocumentsContract.getDocumentId(parentDoc),
        )
        return try {
            contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                ),
                null,
                null,
                null,
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val childName = cursor.getString(1)
                    if (displayName == childName) {
                        val documentId = cursor.getString(0)
                        return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                    }
                }
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun installWithRoot(packageDir: File, packageName: String) {
        val target = "/data/oplus/uxicons/$packageName"
        val source = packageDir.absolutePath
        val command = """
            set -e
            mkdir -p ${shQuote(target)}
            cp -f ${shQuote(source)}/*.png ${shQuote(target)}/
            chmod 0644 ${shQuote(target)}/*.png
            restorecon -RF ${shQuote(target)} 2>/dev/null || true
        """.trimIndent()
        val process = ProcessBuilder("su", "-c", command)
            .redirectErrorStream(true)
            .start()
        val code = process.waitFor()
        if (code != 0) {
            error("su 退出码: $code")
        }
    }

    private fun shQuote(value: String): String = "'" + value.replace("'", "'\\''") + "'"

    private fun status(message: String) {
        runOnUiThread { statusText = message }
    }

    private fun copyStream(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(64 * 1024)
        while (true) {
            val read = input.read(buffer)
            if (read == -1) {
                return
            }
            output.write(buffer, 0, read)
        }
    }

    private fun OutputStream?.useRequired(block: (OutputStream) -> Unit) {
        val output = this ?: error("无法打开输出流")
        output.use { block(it) }
    }

    private data class AppEntry(
        val label: String,
        val packageName: String,
        val applicationInfo: ApplicationInfo,
        val launchable: Boolean,
        val iconKey: String,
    )

    private data class IconLayers(
        val recfg: Bitmap,
        val recbg: Bitmap,
    )

    private data class Bounds(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int,
    ) {
        fun width(): Int = right - left
        fun height(): Int = bottom - top
    }

    private enum class GptImageMode(val value: String, val label: String) {
        Responses("responses", "响应模式"),
        Images("images", "接口模式");

        companion object {
            fun fromValue(value: String?): GptImageMode =
                entries.firstOrNull { it.value == value } ?: Responses
        }
    }

    companion object {
        private const val PREFS_NAME = "artplus_mobile"
        private const val PREF_GPT_MODE = "gpt_mode"
        private const val PREF_GPT_BASE_URL = "gpt_base_url"
        private const val PREF_GPT_API_KEY = "gpt_api_key"
        private const val PREF_USAGE_PERMISSION_PROMPTED = "usage_permission_prompted"
        private const val SIZE_1X1 = 240
        private const val SIZE_2X2 = 704
        private const val GPT_SOURCE_SIZE = 1024
        private const val DEFAULT_GPT_BASE_URL = "http://192.168.31.179:3002/v1"
        private const val GPT_RESPONSE_MODEL = "gpt-5.4-mini"
        private const val GPT_IMAGE_MODEL = "gpt-image-2"
        private const val GPT_IMAGE_SIZE = "1024x1024"
        private const val GPT_IMAGE_QUALITY = "low"
        private const val GPT_CONNECT_TIMEOUT_MS = 30_000
        private const val GPT_READ_TIMEOUT_MS = 360_000
        private const val ICON_CACHE_SIZE = 96
        private const val PRELOAD_ICON_COUNT = 64
        private val appIconCache = object : LruCache<String, Bitmap>(
            ((Runtime.getRuntime().maxMemory() / 1024) / 16).toInt().coerceAtLeast(4 * 1024),
        ) {
            override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount / 1024
        }
        private val SIZE_1X2 = intArrayOf(240, 820)
        private val SIZE_2X1 = intArrayOf(820, 240)
        private const val MONO_ALPHA_MIN = 40
        private const val MONO_ALPHA_MAX = 230
        private const val MONO_ALPHA_GAMMA = 0.85
        private const val FOREGROUND_SUBJECT_MAX_SIDE_RATIO = 0.70f
        private const val CHROMA_TRANSPARENT_THRESHOLD = 36.0
        private const val CHROMA_OPAQUE_THRESHOLD = 170.0
        private val CHROMA_KEY_CANDIDATES = intArrayOf(
            AndroidColor.rgb(0, 255, 0),
            AndroidColor.rgb(255, 0, 255),
            AndroidColor.rgb(0, 255, 255),
            AndroidColor.rgb(0, 0, 255),
            AndroidColor.rgb(255, 255, 0),
        )
    }
}
