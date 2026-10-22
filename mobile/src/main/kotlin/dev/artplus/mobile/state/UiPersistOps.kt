package dev.artplus.mobile.state

import android.content.Context
import dev.artplus.mobile.MainActivity
import dev.artplus.mobile.PREFS_NAME
import dev.artplus.mobile.PREF_ADVANCED_SETTINGS_CATEGORY
import dev.artplus.mobile.PREF_ADVANCED_SETTINGS_TAB
import dev.artplus.mobile.PREF_AUTO_CONFIRM_REFRESH
import dev.artplus.mobile.PREF_AUTO_CONFIRM_ROOT_WRITE
import dev.artplus.mobile.PREF_BATCH_PREVIEW_COLUMNS
import dev.artplus.mobile.PREF_BATCH_PREVIEW_CORNER_RADIUS_DP
import dev.artplus.mobile.PREF_BATCH_PREVIEW_COUNT
import dev.artplus.mobile.PREF_BATCH_PREVIEW_DESKTOP_BG
import dev.artplus.mobile.PREF_BATCH_PREVIEW_ICON_SIZE_DP
import dev.artplus.mobile.PREF_CUSTOM_WALLPAPER_PATH
import dev.artplus.mobile.PREF_GENERATED_FILTER
import dev.artplus.mobile.PREF_OUTPUT_TREE_URI
import dev.artplus.mobile.PREF_PREVIEW_CORNER_RADIUS_DP
import dev.artplus.mobile.PREF_PREVIEW_DESKTOP_BACKGROUND
import dev.artplus.mobile.PREF_PREVIEW_DIR_PATH
import dev.artplus.mobile.PREF_PREVIEW_ICON_SIZE_DP
import dev.artplus.mobile.PREF_PREVIEW_PACKAGE_NAME
import dev.artplus.mobile.PREF_PREVIEW_SELECTION_MONOCHROME_DARK
import dev.artplus.mobile.PREF_PREVIEW_SELECTION_MONOCHROME_LIGHT
import dev.artplus.mobile.PREF_PREVIEW_SELECTION_NORMAL_DARK
import dev.artplus.mobile.PREF_PREVIEW_SELECTION_NORMAL_LIGHT
import dev.artplus.mobile.PREF_PREVIEW_STRIP_ENABLED
import dev.artplus.mobile.PREF_QUERY_TEXT
import dev.artplus.mobile.PREF_SELECTED_PACKAGE_NAME
import dev.artplus.mobile.PREF_SHOW_SYSTEM_APPS
import dev.artplus.mobile.PREF_SKIP_ROOT_WRITE_CONFIRM
import dev.artplus.mobile.ui.cards.PreviewSelections

/**
 * S2.1：UI 状态落盘唯一实现。
 *
 * 收口前：96 处 `persistUiState()` 26 参复制粘贴，
 * 每加一个 UI 字段要改 96 个调用点。收口后：调用点一律 `persistUiState()`，
 * 参数由本函数从 `mainViewModel` 的 StateFlow 现取（与收口前各调用点读取的表达式逐字一致）。
 *
 * `prefs` 由 Activity 持有，VM 不感知；本函数只在主线程被调用（调用点全在 UI 回调里）。
 */
internal fun MainActivity.persistUiState() {
    val picker = mainViewModel.picker.value
    val shell = mainViewModel.shell.value
    val preview = mainViewModel.previewSession.value
    val params = mainViewModel.params.value
    val batch = mainViewModel.batchPreviewConfig.value
    val confirm = mainViewModel.confirm.value
    val selections = PreviewSelections.fromNames(
        params.previewNormalLight,
        params.previewNormalDark,
        params.previewMonochromeLight,
        params.previewMonochromeDark,
    )

    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(PREF_SELECTED_PACKAGE_NAME, picker.selectedPackageName)
        .putString(PREF_GENERATED_FILTER, picker.generatedFilter.name)
        .putBoolean(PREF_SHOW_SYSTEM_APPS, picker.showSystemApps)
        .putString(PREF_QUERY_TEXT, picker.queryText)
        .putString(PREF_ADVANCED_SETTINGS_CATEGORY, shell.advancedSettingsCategory.name)
        .putString(PREF_ADVANCED_SETTINGS_TAB, shell.advancedSettingsTab.name)
        .putString(PREF_PREVIEW_PACKAGE_NAME, preview.previewPackageName)
        .putString(PREF_PREVIEW_DIR_PATH, preview.previewDirPath)
        .putBoolean(PREF_PREVIEW_STRIP_ENABLED, preview.previewStripEnabled)
        .putString(PREF_PREVIEW_SELECTION_NORMAL_LIGHT, selections.normalLight.name)
        .putString(PREF_PREVIEW_SELECTION_NORMAL_DARK, selections.normalDark.name)
        .putString(PREF_PREVIEW_SELECTION_MONOCHROME_LIGHT, selections.monochromeLight.name)
        .putString(PREF_PREVIEW_SELECTION_MONOCHROME_DARK, selections.monochromeDark.name)
        .putString(PREF_PREVIEW_DESKTOP_BACKGROUND, preview.previewDesktopBackground.name)
        .putInt(PREF_PREVIEW_ICON_SIZE_DP, preview.previewIconSizeDp)
        .putInt(PREF_PREVIEW_CORNER_RADIUS_DP, preview.previewCornerRadiusDp)
        .putInt(PREF_BATCH_PREVIEW_COUNT, batch.batchPreviewCount)
        .putInt(PREF_BATCH_PREVIEW_COLUMNS, batch.batchPreviewColumns)
        .putInt(PREF_BATCH_PREVIEW_ICON_SIZE_DP, batch.batchPreviewIconSizeDp)
        .putInt(PREF_BATCH_PREVIEW_CORNER_RADIUS_DP, batch.batchPreviewCornerRadiusDp)
        .putString(PREF_BATCH_PREVIEW_DESKTOP_BG, batch.batchPreviewDesktopBackground.name)
        .apply {
            batch.customWallpaperPath?.let { putString(PREF_CUSTOM_WALLPAPER_PATH, it) }
                ?: remove(PREF_CUSTOM_WALLPAPER_PATH)
        }
        .putBoolean(PREF_AUTO_CONFIRM_ROOT_WRITE, confirm.autoConfirmRootWrite)
        .putBoolean(PREF_SKIP_ROOT_WRITE_CONFIRM, confirm.autoConfirmRootWrite)
        .putBoolean(PREF_AUTO_CONFIRM_REFRESH, confirm.autoConfirmRefresh)
        .putString(PREF_OUTPUT_TREE_URI, shell.outputTreeUri?.toString())
        .apply()
}

/**
 * S2.4：状态栏文案写入唯一入口。
 *
 * 收口前：143 处 `setStatus(...)` 单字段写。
 * 收口后：`setStatus(...)`，`updateShell` 的调用点只剩真正需要多字段合并的位置。
 */
internal fun MainActivity.setStatus(text: String) {
    setStatus(text)
}
