package dev.artplus.mobile

import android.Manifest
import android.content.ContentResolver
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import java.io.File

/**
 * Slice 2.5：应用与权限编排 + UI 存取（原 MainActivity 残留本体原样搬迁）。
 * 只做物理搬迁+显式参数化：线程调度不变（Thread/runOnUiThread 原样保留，
 * 经 postOnUi/onLaunch 注入）；数据核收敛进 data/AppRepository + data/IconCache，
 * 只做调用，不重复定义；system/RootShell 只做调用。
 * MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动。
 * 名单内联 helper 扫描结论：本簇无内联 helper（startUiFriendlyThread/isDebugBuild/
 * addLiquidGlass/applyCurrentPresetBatch 属名单外，转回调注入，不移动本体）。
 * loadApps 等应用加载是 Phase 3 Slice 3.3 解耦对象，本 Slice 只做物理搬迁，协程调度不动。
 */

internal fun pickerLoadApps(
    refreshGenerated: Boolean,
    markLoad: () -> Unit,
    queryApps: () -> AppLoadResult,
    preloadIcons: (List<AppEntry>) -> Unit,
    postOnUi: (AppLoadResult) -> Unit,
) {
    // P3 交界：数据核收敛进 data/AppRepository.loadApps(pm)，线程/UI/状态写入留本编排。
    markLoad()
    Thread {
        val result = queryApps()
        // P3 交界：图标预热收敛进 data/IconCache（显式传 cache + pm + 尺寸 + 条数）。
        preloadIcons(result.entries)
        postOnUi(result)
    }.start()
}

internal fun pickerRefreshGeneratedPackages(
    entries: List<AppEntry>,
    onEmpty: () -> Unit,
    onScanningChange: (Boolean) -> Unit,
    onScanFailed: (Boolean) -> Unit,
    scan: (Set<String>) -> Set<String>,
    postOnUi: ((() -> Unit) -> Unit),
    onSuccess: (Set<String>) -> Unit,
    onFailure: (Throwable) -> Unit,
) {
    if (entries.isEmpty()) {
        onEmpty()
        return
    }
    onScanningChange(true)
    onScanFailed(false)
    Thread {
        val packageNames = entries.map { it.packageName }.toSet()
        val result = runCatching { scan(packageNames) }
        postOnUi {
            result
                .onSuccess { onSuccess(it) }
                .onFailure { onFailure(it) }
            onScanningChange(false)
        }
    }.start()
}

internal fun pickerLoadUiState(
    prefs: SharedPreferences,
    persistedReadWriteUri: Uri?,
    setSelectedPackage: (String?) -> Unit,
    setGeneratedFilter: (GeneratedFilter) -> Unit,
    setShowSystemApps: (Boolean) -> Unit,
    setQueryText: (String) -> Unit,
    setAdvancedCategory: (AdvancedSettingsCategory) -> Unit,
    setAdvancedTab: (AdvancedSettingsTab) -> Unit,
    setPreviewPackage: (String?) -> Unit,
    setPreviewDir: (String?) -> Unit,
    setPreviewStrip: (Boolean) -> Unit,
    updateLiveSelections: (PreviewSelections) -> Unit,
    setDesktopBackground: (PreviewDesktopBackground) -> Unit,
    setIconSize: (Int) -> Unit,
    setDraftIconSizeText: (String) -> Unit,
    setCornerRadius: (Int) -> Unit,
    setDraftCornerRadiusText: (String) -> Unit,
    setBatchCount: (Int) -> Unit,
    setDraftBatchCountText: (String) -> Unit,
    setBatchColumns: (Int) -> Unit,
    setDraftBatchColumnsText: (String) -> Unit,
    setBatchIconSize: (Int) -> Unit,
    setDraftBatchIconSizeText: (String) -> Unit,
    setBatchCorner: (Int) -> Unit,
    setDraftBatchCornerText: (String) -> Unit,
    setBatchDesktopBg: (PreviewDesktopBackground) -> Unit,
    setCustomPath: (String?) -> Unit,
    setCustomInfo: (String) -> Unit,
    setAutoRoot: (Boolean) -> Unit,
    setAutoRefresh: (Boolean) -> Unit,
    setOutputUri: (Uri?) -> Unit,
    setOnboardingVisible: (Boolean) -> Unit,
    parseUri: (String) -> Uri?,
    isFile: (String) -> Boolean,
    decodeBounds: (String) -> Pair<Int, Int>?,
) {
    setSelectedPackage(
        prefs.getString(PREF_SELECTED_PACKAGE_NAME, null)
            ?.takeIf { it.isNotBlank() },
    )
    setGeneratedFilter(GeneratedFilter.fromName(prefs.getString(PREF_GENERATED_FILTER, null)))
    setShowSystemApps(prefs.getBoolean(PREF_SHOW_SYSTEM_APPS, false))
    setQueryText(prefs.getString(PREF_QUERY_TEXT, "") ?: "")
    setAdvancedCategory(
        AdvancedSettingsCategory.fromName(
            prefs.getString(PREF_ADVANCED_SETTINGS_CATEGORY, null),
        ),
    )
    setAdvancedTab(
        runCatching {
            AdvancedSettingsTab.valueOf(
                prefs.getString(PREF_ADVANCED_SETTINGS_TAB, AdvancedSettingsTab.Sliders.name)
                    ?: AdvancedSettingsTab.Sliders.name,
            )
        }.getOrDefault(AdvancedSettingsTab.Sliders),
    )
    setPreviewPackage(
        prefs.getString(PREF_PREVIEW_PACKAGE_NAME, null)
            ?.takeIf { it.isNotBlank() },
    )
    setPreviewDir(
        prefs.getString(PREF_PREVIEW_DIR_PATH, null)
            ?.takeIf { it.isNotBlank() },
    )
    setPreviewStrip(prefs.getBoolean(PREF_PREVIEW_STRIP_ENABLED, false))
    updateLiveSelections(PreviewSelections.fromPrefs(prefs))
    setDesktopBackground(
        PreviewDesktopBackground.fromName(
            prefs.getString(PREF_PREVIEW_DESKTOP_BACKGROUND, null),
        ),
    )
    val iconSize = prefs.getInt(PREF_PREVIEW_ICON_SIZE_DP, DEFAULT_PREVIEW_ICON_SIZE_DP)
        .coerceIn(MIN_PREVIEW_ICON_SIZE_DP, MAX_PREVIEW_ICON_SIZE_DP)
    setIconSize(iconSize)
    setDraftIconSizeText(iconSize.toString())
    val corner = prefs.getInt(PREF_PREVIEW_CORNER_RADIUS_DP, DEFAULT_PREVIEW_CORNER_RADIUS_DP)
        .coerceIn(MIN_PREVIEW_CORNER_RADIUS_DP, MAX_PREVIEW_CORNER_RADIUS_DP)
    setCornerRadius(corner)
    setDraftCornerRadiusText(corner.toString())
    val batchCount = prefs.getInt(PREF_BATCH_PREVIEW_COUNT, DEFAULT_BATCH_PREVIEW_COUNT)
        .coerceIn(MIN_BATCH_PREVIEW_COUNT, MAX_BATCH_PREVIEW_COUNT)
    setBatchCount(batchCount)
    setDraftBatchCountText(batchCount.toString())
    val batchColumns = prefs.getInt(PREF_BATCH_PREVIEW_COLUMNS, 4).coerceIn(2, 5)
    setBatchColumns(batchColumns)
    setDraftBatchColumnsText(batchColumns.toString())
    val batchIcon = prefs.getInt(PREF_BATCH_PREVIEW_ICON_SIZE_DP, 54).coerceIn(40, 84)
    setBatchIconSize(batchIcon)
    setDraftBatchIconSizeText(batchIcon.toString())
    val batchCorner = prefs.getInt(PREF_BATCH_PREVIEW_CORNER_RADIUS_DP, corner).coerceIn(0, 36)
    setBatchCorner(batchCorner)
    setDraftBatchCornerText(batchCorner.toString())
    setBatchDesktopBg(
        PreviewDesktopBackground.fromName(
            prefs.getString(PREF_BATCH_PREVIEW_DESKTOP_BG, PreviewDesktopBackground.DarkGray.name),
        ),
    )
    val customPath = prefs.getString(PREF_CUSTOM_WALLPAPER_PATH, null)
        ?.takeIf { isFile(it) }
    setCustomPath(customPath)
    setCustomInfo(
        customPath?.let { path ->
            decodeBounds(path)?.let { (w, h) ->
                if (w > 0 && h > 0) "$w × $h" else ""
            }.orEmpty()
        }.orEmpty(),
    )
    setAutoRoot(
        prefs.getBoolean(
            PREF_AUTO_CONFIRM_ROOT_WRITE,
            prefs.getBoolean(PREF_SKIP_ROOT_WRITE_CONFIRM, false),
        ),
    )
    setAutoRefresh(prefs.getBoolean(PREF_AUTO_CONFIRM_REFRESH, false))
    val treeUri = prefs.getString(PREF_OUTPUT_TREE_URI, null)
        ?.takeIf { it.isNotBlank() }?.let { parseUri(it) }
        ?: persistedReadWriteUri
    setOutputUri(treeUri)
    // onboarding: if not completed and no dir, show guide
    val hasCompleted = prefs.getBoolean(PREF_HAS_COMPLETED_ONBOARDING, false)
    if (!hasCompleted && treeUri == null) {
        setOnboardingVisible(true)
    }
}

internal fun pickerSaveUiState(
    prefs: SharedPreferences,
    selectedPackage: String?,
    generatedFilter: GeneratedFilter,
    showSystemApps: Boolean,
    queryText: String,
    advancedCategory: AdvancedSettingsCategory,
    advancedTab: AdvancedSettingsTab,
    previewPackage: String?,
    previewDir: String?,
    previewStrip: Boolean,
    previewNormalLight: String,
    previewNormalDark: String,
    previewMonochromeLight: String,
    previewMonochromeDark: String,
    desktopBackground: PreviewDesktopBackground,
    iconSize: Int,
    cornerRadius: Int,
    batchCount: Int,
    batchColumns: Int,
    batchIconSize: Int,
    batchCorner: Int,
    batchDesktopBg: PreviewDesktopBackground,
    customPath: String?,
    autoRoot: Boolean,
    autoRefresh: Boolean,
    outputUri: Uri?,
) {
    prefs
        .edit()
        .putString(PREF_SELECTED_PACKAGE_NAME, selectedPackage)
        .putString(PREF_GENERATED_FILTER, generatedFilter.name)
        .putBoolean(PREF_SHOW_SYSTEM_APPS, showSystemApps)
        .putString(PREF_QUERY_TEXT, queryText)
        .putString(PREF_ADVANCED_SETTINGS_CATEGORY, advancedCategory.name)
        .putString(PREF_ADVANCED_SETTINGS_TAB, advancedTab.name)
        .putString(PREF_PREVIEW_PACKAGE_NAME, previewPackage)
        .putString(PREF_PREVIEW_DIR_PATH, previewDir)
        .putBoolean(PREF_PREVIEW_STRIP_ENABLED, previewStrip)
        .putString(PREF_PREVIEW_SELECTION_NORMAL_LIGHT, PreviewSelections.fromNames(previewNormalLight, previewNormalDark, previewMonochromeLight, previewMonochromeDark).normalLight.name)
        .putString(PREF_PREVIEW_SELECTION_NORMAL_DARK, PreviewSelections.fromNames(previewNormalLight, previewNormalDark, previewMonochromeLight, previewMonochromeDark).normalDark.name)
        .putString(PREF_PREVIEW_SELECTION_MONOCHROME_LIGHT, PreviewSelections.fromNames(previewNormalLight, previewNormalDark, previewMonochromeLight, previewMonochromeDark).monochromeLight.name)
        .putString(PREF_PREVIEW_SELECTION_MONOCHROME_DARK, PreviewSelections.fromNames(previewNormalLight, previewNormalDark, previewMonochromeLight, previewMonochromeDark).monochromeDark.name)
        .putString(PREF_PREVIEW_DESKTOP_BACKGROUND, desktopBackground.name)
        .putInt(PREF_PREVIEW_ICON_SIZE_DP, iconSize)
        .putInt(PREF_PREVIEW_CORNER_RADIUS_DP, cornerRadius)
        .putInt(PREF_BATCH_PREVIEW_COUNT, batchCount)
        .putInt(PREF_BATCH_PREVIEW_COLUMNS, batchColumns)
        .putInt(PREF_BATCH_PREVIEW_ICON_SIZE_DP, batchIconSize)
        .putInt(PREF_BATCH_PREVIEW_CORNER_RADIUS_DP, batchCorner)
        .putString(PREF_BATCH_PREVIEW_DESKTOP_BG, batchDesktopBg.name)
        .apply { customPath?.let { putString(PREF_CUSTOM_WALLPAPER_PATH, it) } ?: remove(PREF_CUSTOM_WALLPAPER_PATH) }
        .putBoolean(PREF_AUTO_CONFIRM_ROOT_WRITE, autoRoot)
        .putBoolean(PREF_SKIP_ROOT_WRITE_CONFIRM, autoRoot)
        .putBoolean(PREF_AUTO_CONFIRM_REFRESH, autoRefresh)
        .putString(PREF_OUTPUT_TREE_URI, outputUri?.toString())
        .apply()
}

internal fun pickerRefreshArtPlusIcons(
    isBusy: Boolean,
    isRefreshing: Boolean,
    onRefreshingChange: (Boolean) -> Unit,
    onStatusText: (String) -> Unit,
    blockingRefresh: (ContentResolver, String) -> String,
    contentResolver: ContentResolver,
    apkPath: String,
    postOnUi: ((() -> Unit) -> Unit),
    onSuccess: (String) -> Unit,
    onFailure: (Throwable) -> Unit,
) {
    if (isBusy || isRefreshing) {
        return
    }
    onRefreshingChange(true)
    onStatusText("正在刷新 ART+ 图标...")
    Thread {
        // P3 交界：阻塞核收敛进 system/RootShell（显式传 ContentResolver + apkPath）。
        val result = runCatching { blockingRefresh(contentResolver, apkPath) }
        postOnUi {
            result
                .onSuccess { onSuccess(it) }
                .onFailure { onFailure(it) }
            onRefreshingChange(false)
        }
    }.start()
}

internal fun pickerRefreshPermissionState(
    checkQueryPermission: () -> Boolean,
    hasUsage: () -> Boolean,
    onResult: (queryGranted: Boolean, usageGranted: Boolean) -> Unit,
) {
    onResult(checkQueryPermission(), hasUsage())
}

internal fun pickerRequestDeclaredPermissions(
    needsQuery: Boolean,
    launcher: (Array<String>) -> Unit,
) {
    val permissions = mutableListOf<String>()
    if (needsQuery) {
        permissions += Manifest.permission.QUERY_ALL_PACKAGES
    }
    if (permissions.isNotEmpty()) {
        launcher(permissions.toTypedArray())
    }
}

internal fun pickerRequestSpecialPermissionsOnce(
    usageGranted: Boolean,
    prompted: Boolean,
    markPrompted: () -> Unit,
    postOnDecor: ((() -> Unit) -> Unit),
    hasUsage: () -> Boolean,
    openSettings: () -> Unit,
) {
    if (usageGranted) {
        return
    }
    if (prompted) {
        return
    }
    markPrompted()
    postOnDecor {
        if (!hasUsage()) {
            openSettings()
        }
    }
}

/** loadUiState 默认文件探针/解码实现（供 wrapper 注入用，保持原判断顺序）。 */
internal fun pickerIsCustomWallpaperFile(path: String): Boolean =
    File(path).isFile

internal fun pickerDecodeWallpaperBounds(path: String): Pair<Int, Int>? =
    runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        bounds.outWidth to bounds.outHeight
    }.getOrNull()

internal fun pickerCheckQueryPermission(pm: PackageManager, packageName: String): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        pm.checkPermission(Manifest.permission.QUERY_ALL_PACKAGES, packageName) ==
            PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

internal fun pickerNeedsQueryPermission(pm: PackageManager, packageName: String): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
        pm.checkPermission(Manifest.permission.QUERY_ALL_PACKAGES, packageName) !=
        PackageManager.PERMISSION_GRANTED
