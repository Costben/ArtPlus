package dev.artplus.mobile

import android.content.ContentResolver
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.LruCache
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Slice 3.3：应用加载与生命周期调度下沉至 MainViewModel。
 *
 * 从 MainActivity 原样搬迁（只搬名单 10 项）：
 * loadApps / refreshGeneratedPackages / refreshArtPlusIcons / loadCachedAppIcon /
 * onCreate / onDestroy / onPause / onResume / onNewIntent / startUiFriendlyThread。
 * 内联 helper 扫描结论：名单函数引用的 helper（loadGeneratedPackageCache /
 * updateGeneratedPackageCache / scanRootGeneratedPackages / loadApps /
 * preloadAppIcons / getCachedAppIcon / loadAppIconBitmap / pickerLoadCachedAppIcon /
 * refreshArtPlusIconsBlocking / isDebugGenerateIntent / handleDebugGenerateIntent /
 * loadGptSettings / loadTuningParams / initTuningHistory / loadRmbgSettings /
 * loadUiState / loadPresetState / startDebugHttpServerIfNeeded /
 * refreshPermissionState / requestDeclaredPermissions / requestSpecialPermissionsOnce /
 * saveUiState）均为跨簇共享（名单外另有引用）或 data/system/ui 既有符号
 * （禁动，原位调用）——无同簇独占内联 helper 需另搬，随本体迁移的只有
 * status 文案分支与扫描条件判断（提为纯 helper，JVM 可测）。
 *
 * 约定（沿用 Slice 3.1 分组槽位 + Slice 3.2 viewModelScope 模式，同包直接用）：
 * - 状态只写 PickerState（generated 族/scanning）/ ShellState（statusText）/
 *   PreviewSessionState（isRefreshingArtPlusIcons），经 updatePicker /
 *   updateShell / updatePreviewSession 漏斗；apps 列表仍由 Activity 持有
 *   （mutableStateListOf，大量 UI 引用，P5 再收敛），经 applyEntries 回调写回。
 * - 调度一律 viewModelScope 协程（Dispatchers.IO 做后台段），替代 Activity 的
 *   Thread / runOnUiThread 直调；StateFlow 写线程安全，回调一律回 Main。
 * - 数据核只做调用：loadApps / scanRootGeneratedPackages /
 *   loadGeneratedPackageCache / updateGeneratedPackageCache（data/AppRepository）、
 *   getCachedAppIcon / loadAppIconBitmap / preloadAppIcons（data/IconCache）、
 *   refreshArtPlusIconsBlocking（system/RootShell）、pickerLoadCachedAppIcon
 *   （ui/pages/picker，已是 suspend IO，无 Thread，保留委托链）。
 *   Slice 2.5 pickerLoadApps / pickerRefreshGeneratedPackages /
 *   pickerRefreshArtPlusIcons 的 Thread 版本不再被 in-scope wrapper 调用
 *   （调度已由本文件接管），其本体留 ui/ 原位不动（禁动），其他调用点不受影响。
 * - 同名同参 wrapper 禁止：VM 侧方法一律扩参或异名（requestAppLoad /
 *   refreshGeneratedPackagesAsync / refreshArtPlusIconsAsync /
 *   loadCachedAppIconOp / launchUiFriendly / onCreatePreContent /
 *   onCreatePostContent / onPausePersist / onResumeRefresh / onNewIntentDebug /
 *   onDestroyCleanup），Activity 留原名薄 wrapper。
 * - 生命周期只做位置迁移：onCreate 初始化链、onPause/onResume 持久化与重载条件、
 *   onNewIntent debug 入口的调用顺序与条件逐字保留，Context 绑定部分经参数/回调注入。
 */

// ---------- 纯 helper（JVM 可测，原 Activity 内联分支逐字提取） ----------

/** 原 loadApps postOnUi 的 statusText 文案分支。 */
internal fun buildAppLoadStatus(
    entryCount: Int,
    launchableCount: Int,
    permissionGranted: Boolean,
): String = when {
    entryCount == 0 -> "没有读取到应用。请确认已允许读取应用列表。"
    !permissionGranted -> "读取到 $entryCount 个应用，但应用列表权限状态异常。"
    else -> "共 $entryCount 个应用，其中 $launchableCount 个有启动器入口。"
}

/** 原 onResume 的重载条件分支。 */
internal fun shouldReloadApps(
    didRequestAppLoad: Boolean,
    appsEmpty: Boolean,
    previousQueryGranted: Boolean,
    currentQueryGranted: Boolean,
    previousUsageGranted: Boolean,
    currentUsageGranted: Boolean,
): Boolean =
    didRequestAppLoad &&
        (
            appsEmpty ||
                previousQueryGranted != currentQueryGranted ||
                previousUsageGranted != currentUsageGranted
        )

/** 原 refreshGeneratedPackages onEmpty 分支文案。 */
internal fun generatedEmptyStatus(): String = "应用列表为空，保留已生成缓存"

/** 原 refreshGeneratedPackages onSuccess 分支文案。 */
internal fun generatedSuccessStatus(count: Int): String = "已刷新生成状态: $count 个"

/** 原 refreshGeneratedPackages onFailure 分支文案。 */
internal fun generatedFailureStatus(error: Throwable): String =
    "生成状态刷新失败，保留上次缓存: ${error.message ?: error.javaClass.simpleName}"

/** 原 refreshArtPlusIcons onSuccess 分支文案。 */
internal fun iconsSuccessStatus(summary: String): String =
    if (summary.isBlank()) {
        "已刷新 ART+ 图标"
    } else {
        "已刷新 ART+ 图标: $summary"
    }

/** 原 refreshArtPlusIcons onFailure 分支文案。 */
internal fun iconsFailureStatus(error: Throwable): String =
    "刷新 ART+ 图标失败: ${error.message ?: error.javaClass.simpleName}"

// ---------- ViewModel 编排（viewModelScope 调度） ----------

/**
 * 应用列表加载（原 MainActivity.loadApps 本体下沉；viewModelScope 调度）。
 * markLoad 置 didRequestAppLoad（Activity 持有）；onRefreshPermissions 调
 * Activity.refreshPermissionState（名单外，原位调用）；applyEntries 写回
 * Activity apps 列表；status 经 updateShell 直写（单源）；refreshGenerated
 * 为 true 时链式调用本文件 refreshGeneratedPackagesAsync（需 prefs）。
 */
internal fun MainViewModel.requestAppLoad(
    refreshGenerated: Boolean,
    pm: PackageManager,
    iconCache: LruCache<String, Bitmap>,
    cacheSize: Int,
    preloadCount: Int,
    prefs: SharedPreferences,
    markLoad: () -> Unit,
    onRefreshPermissions: () -> Unit,
    applyEntries: (List<AppEntry>) -> Unit,
) {
    viewModelScope.launch {
        markLoad()
        // P3 交界：数据核收敛进 data/AppRepository.loadApps(pm) +
        // data/IconCache.preloadAppIcons（显式传 cache + pm + 尺寸 + 条数），IO 段后台。
        val result = withContext(Dispatchers.IO) {
            loadApps(pm).also { loaded ->
                preloadAppIcons(iconCache, pm, loaded.entries, cacheSize, preloadCount)
            }
        }
        onRefreshPermissions()
        applyEntries(result.entries)
        updateShell {
            it.copy(
                statusText = buildAppLoadStatus(
                    entryCount = result.entries.size,
                    launchableCount = result.launchablePackages.size,
                    permissionGranted = picker.value.packageListPermissionGranted,
                ),
            )
        }
        if (refreshGenerated) {
            refreshGeneratedPackagesAsync(result.entries, prefs)
        }
    }
}

/**
 * 已生成包扫描（原 MainActivity.refreshGeneratedPackages 本体下沉；viewModelScope 调度）。
 * 空表分支先行返回（顺序与原 onEmpty 一致）；扫描段 IO 后台；成功经
 * data/AppRepository.updateGeneratedPackageCache 写回 prefs，状态经 updatePicker /
 * updateShell 直写。
 */
internal fun MainViewModel.refreshGeneratedPackagesAsync(
    entries: List<AppEntry>,
    prefs: SharedPreferences,
) {
    if (entries.isEmpty()) {
        updatePicker { it.copy(isScanningGeneratedPackages = false, generatedScanFailed = false) }
        updateShell { it.copy(statusText = generatedEmptyStatus()) }
        return
    }
    updatePicker { it.copy(isScanningGeneratedPackages = true, generatedScanFailed = false) }
    viewModelScope.launch {
        val packageNames = entries.map { it.packageName }.toSet()
        val result = withContext(Dispatchers.IO) {
            runCatching { scanRootGeneratedPackages(packageNames) }
        }
        result
            .onSuccess { generated ->
                val normalized = updateGeneratedPackageCache(prefs, generated)
                updatePicker { it.copy(generatedPackageNames = normalized, generatedScanFailed = false) }
                updateShell { it.copy(statusText = generatedSuccessStatus(normalized.size)) }
            }
            .onFailure { error ->
                updatePicker { it.copy(generatedScanFailed = true) }
                updateShell { it.copy(statusText = generatedFailureStatus(error)) }
            }
        updatePicker { it.copy(isScanningGeneratedPackages = false) }
    }
}

/**
 * ART+ 图标刷新（原 MainActivity.refreshArtPlusIcons 本体下沉；viewModelScope 调度）。
 * 忙/刷新中直接返回（条件与原一致）；阻塞核收敛进
 * system/RootShell.refreshArtPlusIconsBlocking（显式传 ContentResolver + apkPath）。
 */
internal fun MainViewModel.refreshArtPlusIconsAsync(
    contentResolver: ContentResolver,
    apkPath: String,
) {
    if (shell.value.isBusy || previewSession.value.isRefreshingArtPlusIcons) {
        return
    }
    updatePreviewSession { it.copy(isRefreshingArtPlusIcons = true) }
    updateShell { it.copy(statusText = "正在刷新 ART+ 图标...") }
    viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            runCatching { refreshArtPlusIconsBlocking(contentResolver, apkPath) }
        }
        result
            .onSuccess { summary ->
                updateShell { it.copy(statusText = iconsSuccessStatus(summary)) }
            }
            .onFailure { error ->
                updateShell { it.copy(statusText = iconsFailureStatus(error)) }
            }
        updatePreviewSession { it.copy(isRefreshingArtPlusIcons = false) }
    }
}

/**
 * 图标单取（原 MainActivity.loadCachedAppIcon 本体下沉；suspend IO）。
 * 委托链保留：经 ui/pages/picker pickerLoadCachedAppIcon（已是 withContext IO，
 * 无 Thread），data 符号（getCachedAppIcon / loadAppIconBitmap）由其内部调用，
 * 本方法只做参数显式化（cache + pm + size 由 Activity 注入）。
 */
internal suspend fun MainViewModel.loadCachedAppIconOp(
    entry: AppEntry,
    iconCache: LruCache<String, Bitmap>,
    pm: PackageManager,
    cacheSize: Int,
): Bitmap? =
    pickerLoadCachedAppIcon(
        entry = entry,
        getCached = { key -> getCachedAppIcon(iconCache, key) },
        putCached = { key, value -> synchronized(iconCache) { iconCache.put(key, value) } },
        loadBitmap = { e ->
            runCatching { loadAppIconBitmap(e, pm, cacheSize) }.getOrNull()
        },
    )

/**
 * 后台发射（原 MainActivity.startUiFriendlyThread 本体下沉；viewModelScope 调度）。
 * 原语义为后台优先级的具名 Thread；协程等价为 Dispatchers.IO +
 * THREAD_PRIORITY_BACKGROUND + CoroutineName（线程名保留可观测，MIN_PRIORITY
 * 无协程等价，不保留）。Activity 同名 wrapper 委托至此，调用点零改动。
 */
internal fun MainViewModel.launchUiFriendly(name: String, block: () -> Unit) {
    viewModelScope.launch(Dispatchers.IO + CoroutineName(name)) {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
        block()
    }
}

// ---------- 生命周期编排（只做位置迁移，顺序与条件不变） ----------

/**
 * onCreate 前半链（setContent 之前；原 onCreate 565-578 行顺序逐字保留）。
 * onLoadGeneratedCache 内含 loadGeneratedPackageCache + 双 flag 复位（调用方闭包）。
 * EdgeToEdge / setContent 顶层容器挂载留 Activity（装配）。
 */
internal fun MainViewModel.onCreatePreContent(
    onLoadGptSettings: () -> Unit,
    onLoadTuningParams: () -> Unit,
    onInitTuningHistory: () -> Unit,
    onLoadRmbgSettings: () -> Unit,
    onLoadGeneratedCache: () -> Unit,
    onLoadUiState: () -> Unit,
    onLoadPresetState: () -> Unit,
    onStartDebugServer: () -> Unit,
    onRefreshPermissions: () -> Unit,
) {
    onLoadGptSettings()
    onLoadTuningParams()
    onInitTuningHistory()
    onLoadRmbgSettings()
    onLoadGeneratedCache()
    onLoadUiState()
    onLoadPresetState()
    onStartDebugServer()
    onRefreshPermissions()
}

/**
 * onCreate 后半链（setContent 之后；原 onCreate 607-612 行顺序与条件逐字保留）。
 * isDebugIntent 为 Activity.isDebugGenerateIntent(intent) 快照（条件位置不变）。
 */
internal fun MainViewModel.onCreatePostContent(
    isDebugIntent: Boolean,
    onRequestDeclaredPermissions: () -> Unit,
    onRequestSpecialPermissionsOnce: () -> Unit,
    onLoadApps: () -> Unit,
    onHandleDebugIntent: () -> Unit,
) {
    onRequestDeclaredPermissions()
    if (!isDebugIntent) {
        onRequestSpecialPermissionsOnce()
    }
    onLoadApps()
    onHandleDebugIntent()
}

/** onPause 持久化（原 saveUiState() 先行，super.onPause 留 Activity）。 */
internal fun MainViewModel.onPausePersist(onSaveUiState: () -> Unit) {
    onSaveUiState()
}

/**
 * onResume 权限刷新与条件重载（原 633-643 行顺序与条件逐字保留：
 * 先快照旧值（调用方），再 refresh，最后按 shouldReloadApps 条件 load）。
 */
internal fun MainViewModel.onResumeRefresh(
    didRequestAppLoad: Boolean,
    appsEmpty: Boolean,
    previousQueryGranted: Boolean,
    previousUsageGranted: Boolean,
    onRefreshPermissions: () -> Unit,
    currentQueryGranted: () -> Boolean,
    currentUsageGranted: () -> Boolean,
    onLoadApps: () -> Unit,
) {
    onRefreshPermissions()
    if (
        shouldReloadApps(
            didRequestAppLoad = didRequestAppLoad,
            appsEmpty = appsEmpty,
            previousQueryGranted = previousQueryGranted,
            currentQueryGranted = currentQueryGranted(),
            previousUsageGranted = previousUsageGranted,
            currentUsageGranted = currentUsageGranted(),
        )
    ) {
        onLoadApps()
    }
}

/** onNewIntent debug 入口（原 setIntent + handleDebugGenerateIntent 顺序保留）。 */
internal fun MainViewModel.onNewIntentDebug(
    onSetIntent: () -> Unit,
    onHandleDebugIntent: () -> Unit,
) {
    onSetIntent()
    onHandleDebugIntent()
}

/**
 * onDestroy 清理（原 616-622 行顺序逐字保留，super.onDestroy 留 Activity）。
 * 各回调关闭 Activity 持有资源（job/scope/dispatcher/server/runtime）。
 */
internal fun MainViewModel.onDestroyCleanup(
    onCancelPreviewJob: () -> Unit,
    onCancelWorkerScope: () -> Unit,
    onCloseWorkerDispatcher: () -> Unit,
    onStopDebugServer: () -> Unit,
    onCloseRmbgRuntime: () -> Unit,
) {
    onCancelPreviewJob()
    onCancelWorkerScope()
    onCloseWorkerDispatcher()
    onStopDebugServer()
    onCloseRmbgRuntime()
}
