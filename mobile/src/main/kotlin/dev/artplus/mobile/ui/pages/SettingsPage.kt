package dev.artplus.mobile

import android.content.Context
import android.content.pm.ApplicationInfo
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Link
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import io.github.suqi8.coui.kmp.basic.ScrollBehavior
import io.github.suqi8.coui.kmp.layout.DialogButtonBarAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun MainActivity.SettingsPage(
    innerPadding: PaddingValues,
    scrollBehavior: ScrollBehavior,
    launcherCount: Int,
    totalCount: Int,
    generatedCount: Int,
) {
    val confirm by mainViewModel.confirm.collectAsState()
    val shell by mainViewModel.shell.collectAsState()
    val previewSession by mainViewModel.previewSession.collectAsState()
    val transfer by mainViewModel.transfer.collectAsState()
    val gptRmbgSettings by mainViewModel.gptRmbgSettings.collectAsState()
    val presetUi by mainViewModel.presetUi.collectAsState()
    val glassBar by mainViewModel.glassBar.collectAsState()
    val batchPreviewConfig by mainViewModel.batchPreviewConfig.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .imePadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + 16.dp,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "input_settings") {
            InputSettingsCard(
                launcherCount = launcherCount,
                totalCount = totalCount,
                generatedCount = generatedCount,
            )
        }
        item(key = "output") {
            run {
val __act1 = LocalContext.current
    OutputCard(
                autoConfirmRootWrite = confirm.autoConfirmRootWrite,
                autoConfirmRefresh = confirm.autoConfirmRefresh,
                isBusy = shell.isBusy,
                outputTreeUri = shell.outputTreeUri,
                treeDisplay = remember(shell.outputTreeUri?.toString()) { formatTreeUriDisplay(shell.outputTreeUri) },
                backupActive = backupJob?.isActive == true && transfer.backupProgress != null,
                backupInBackground = transfer.backupInBackground,
                backupDots = transfer.backupBackgroundDots,
                exportDialogVisible = previewSession.exportDialogVisible,
                onAutoConfirmRootWriteChange = {
                    mainViewModel.updateConfirm { v -> v.copy(autoConfirmRootWrite = (it)) }
                    run {
        pickerSaveUiState(
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    selectedPackage = mainViewModel.picker.value.selectedPackageName,
                    generatedFilter = mainViewModel.picker.value.generatedFilter,
                    showSystemApps = mainViewModel.picker.value.showSystemApps,
                    queryText = mainViewModel.picker.value.queryText,
                    advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                    advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                    previewPackage = mainViewModel.previewSession.value.previewPackageName,
                    previewDir = mainViewModel.previewSession.value.previewDirPath,
                    previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                    previewNormalLight = mainViewModel.params.value.previewNormalLight,
                    previewNormalDark = mainViewModel.params.value.previewNormalDark,
                    previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                    previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                    desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                    iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                    cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                    batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                    batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                    batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                    batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                    batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                    customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                    autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                    autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                    outputUri = mainViewModel.shell.value.outputTreeUri,
                )
    }
                    mainViewModel.updateShell { it -> it.copy(statusText = (if (mainViewModel.confirm.value.autoConfirmRootWrite) "已开启自动确认写入" else "已关闭自动确认写入")) }
                },
                onAutoConfirmRefreshChange = {
                    mainViewModel.updateConfirm { v -> v.copy(autoConfirmRefresh = (it)) }
                    run {
        pickerSaveUiState(
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    selectedPackage = mainViewModel.picker.value.selectedPackageName,
                    generatedFilter = mainViewModel.picker.value.generatedFilter,
                    showSystemApps = mainViewModel.picker.value.showSystemApps,
                    queryText = mainViewModel.picker.value.queryText,
                    advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                    advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                    previewPackage = mainViewModel.previewSession.value.previewPackageName,
                    previewDir = mainViewModel.previewSession.value.previewDirPath,
                    previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                    previewNormalLight = mainViewModel.params.value.previewNormalLight,
                    previewNormalDark = mainViewModel.params.value.previewNormalDark,
                    previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                    previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                    desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                    iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                    cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                    batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                    batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                    batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                    batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                    batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                    customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                    autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                    autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                    outputUri = mainViewModel.shell.value.outputTreeUri,
                )
    }
                    mainViewModel.updateShell { it -> it.copy(statusText = (if (mainViewModel.confirm.value.autoConfirmRefresh) "已开启自动确认刷新" else "已关闭自动确认刷新")) }
                },
                onBackupRowClick = {
                    val active = backupJob?.isActive == true && mainViewModel.transfer.value.backupProgress != null
                    val inBg = mainViewModel.transfer.value.backupInBackground && active
                    if (inBg || (active && mainViewModel.transfer.value.backupSheetVisible.not())) {
                        mainViewModel.updateTransfer { it -> it.copy(backupInBackground = (false)) }
                        mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (true)) }
                        run {

                backupDotJob?.cancel()
                backupDotJob = null
    }
                    } else if (active) {
                        mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (true)) }
                    } else {
                        mainViewModel.updatePreviewSession { it -> it.copy(exportDialogVisible = (true)) }
                    }
                },
                onBackupBackgroundActiveChanged = { inBg ->
                    if (inBg) run {

                backupDotJob?.cancel()
                backupDotJob = mainScope.launch {
                    while (isActive) {
                        delay(500)
                        mainViewModel.updateTransfer { it -> it.copy(backupBackgroundDots = (if (mainViewModel.transfer.value.backupBackgroundDots >= 3) 1 else mainViewModel.transfer.value.backupBackgroundDots + 1)) }
                    }
                }
    } else run {

                backupDotJob?.cancel()
                backupDotJob = null
    }
                },
                onExportDialogDismiss = { mainViewModel.updatePreviewSession { it -> it.copy(exportDialogVisible = (false)) } },
                onChooseTree = { chooseTreeLauncher.launch(null) },
                onBackupAll = { run {

                if (mainViewModel.shell.value.outputTreeUri == null) {
                    run {
            pickerToastStatus(
                        message = ("还没有设置目录"),
                        postOnUi = { text -> runOnUiThread { mainViewModel.updateShell { it -> it.copy(statusText = (text)) } } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act1, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        }
                    mainViewModel.updatePreviewSession { it -> it.copy(exportDialogVisible = (true)) }
                    return@run
                }
                if (mainViewModel.shell.value.isBusy) return@run
                // 若已有备份任务，仅重显弹窗
                if (backupJob?.isActive == true) {
                    mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (true)) }
                    mainViewModel.updateTransfer { it -> it.copy(backupInBackground = (false)) }
                    run {

                    backupDotJob?.cancel()
                    backupDotJob = null
        }
                    return@run
                }
                mainViewModel.updateShell { it -> it.copy(isBusy = (true)) }
                mainViewModel.updateTransfer { it -> it.copy(backupInBackground = (false)) }
                mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (true)) }
                mainViewModel.updateTransfer { it -> it.copy(backupBackgroundDots = (1)) }
                mainViewModel.updateTransfer { it -> it.copy(backupProgress = (ExportProgress(
                    title = "备份中",
                    completed = 0,
                    total = 1,
                    currentLabel = "正在准备...",
                    isIndeterminate = true,
                ))) }
                run {
            pickerToastStatus(
                        message = ("正在备份..."),
                        postOnUi = { text -> runOnUiThread { mainViewModel.updateShell { it -> it.copy(statusText = (text)) } } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act1, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        }
                backupJob?.cancel()
                backupDotJob?.cancel()
                backupJob = mainScope.launch(Dispatchers.IO) {
                    try {
                        runCatching { ensureNomediaAtTreeRoot(contentResolver, mainViewModel.shell.value.outputTreeUri) }
                        val pkgs = listRootIconPackages()
                        if (pkgs.isEmpty()) {
                            withContext(Dispatchers.Main) {
                                mainViewModel.updateTransfer { it -> it.copy(backupProgress = (null)) }
                                mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (false)) }
                                run {
            pickerToastStatus(
                        message = ("没有可导出的图标包"),
                        postOnUi = { text -> runOnUiThread { mainViewModel.updateShell { it -> it.copy(statusText = (text)) } } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act1, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        }
                            }
                            return@launch
                        }
                        val treeUri = mainViewModel.shell.value.outputTreeUri
                        if (treeUri == null) {
                            withContext(Dispatchers.Main) {
                                mainViewModel.updateTransfer { it -> it.copy(backupProgress = (null)) }
                                mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (false)) }
                                run {
            pickerToastStatus(
                        message = ("还没有设置目录"),
                        postOnUi = { text -> runOnUiThread { mainViewModel.updateShell { it -> it.copy(statusText = (text)) } } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act1, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        }
                            }
                            return@launch
                        }
                        withContext(Dispatchers.Main) {
                            mainViewModel.updateTransfer { it -> it.copy(backupProgress = (ExportProgress(
                                title = "备份中",
                                completed = 0,
                                total = pkgs.size,
                                currentLabel = "准备备份 ${pkgs.size} 个图标包",
                                isIndeterminate = false,
                            ))) }
                        }
                        var successCount = 0
                        var failCount = 0
                        val destRootFast = resolveTreeUriToFilePath(treeUri)
                        // 情况1：可解析为文件系统路径 -> 使用 su 直拷（一次 su per pkg，约 10ms/包），最快
                        if (destRootFast != null) {
                            for ((index, pkgName) in pkgs.withIndex()) {
                                ensureActive()
                                withContext(Dispatchers.Main) {
                                    mainViewModel.updateTransfer { it -> it.copy(backupProgress = (ExportProgress(
                                        title = "备份中",
                                        completed = index,
                                        total = pkgs.size,
                                        currentLabel = "正在备份 ${index + 1}/${pkgs.size}: $pkgName",
                                        isIndeterminate = false,
                                    ))) }
                                    mainViewModel.updateShell { it -> it.copy(statusText = ("正在备份 ${index + 1}/${pkgs.size}: $pkgName")) }
                                }
                                val ok = runCatching { backupPackageFast(pkgName, destRootFast) }.getOrDefault(false)
                                if (ok) successCount++ else failCount++
                                withContext(Dispatchers.Main) {
                                    mainViewModel.updateTransfer { it -> it.copy(backupProgress = (ExportProgress(
                                        title = "备份中",
                                        completed = index + 1,
                                        total = pkgs.size,
                                        currentLabel = if (ok) "已完成 ${index + 1}/${pkgs.size}: $pkgName" else "失败 $pkgName",
                                        isIndeterminate = false,
                                    ))) }
                                }
                            }
                            withContext(Dispatchers.Main) {
                                if (!mainViewModel.transfer.value.backupInBackground) {
                                    mainViewModel.updateTransfer { it -> it.copy(backupProgress = (null)) }
                                    mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (false)) }
                                }
                                if (failCount == 0) run {
            pickerToastStatus(
                        message = ("已备份 $successCount 个图标包"),
                        postOnUi = { text -> runOnUiThread { mainViewModel.updateShell { it -> it.copy(statusText = (text)) } } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act1, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        }
                                else run {
            pickerToastStatus(
                        message = ("已备份 $successCount 个，失败 $failCount 个"),
                        postOnUi = { text -> runOnUiThread { mainViewModel.updateShell { it -> it.copy(statusText = (text)) } } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act1, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        }
                            }
                        } else {
                            // 情况2：无法解析路径（SD卡/特殊 Provider）-> 回退 SAF 中转缓存方案
                            val stagingRoot = File(cacheDir, "backup_staging").also { it.mkdirs() }
                            for ((index, pkgName) in pkgs.withIndex()) {
                                ensureActive()
                                withContext(Dispatchers.Main) {
                                    mainViewModel.updateTransfer { it -> it.copy(backupProgress = (ExportProgress(
                                        title = "备份中",
                                        completed = index,
                                        total = pkgs.size,
                                        currentLabel = "正在备份 ${index + 1}/${pkgs.size}: $pkgName",
                                        isIndeterminate = false,
                                    ))) }
                                    mainViewModel.updateShell { it -> it.copy(statusText = ("正在备份 ${index + 1}/${pkgs.size}: $pkgName")) }
                                }
                                val stagingDir = File(stagingRoot, pkgName)
                                try {
                                    if (stagingDir.exists()) stagingDir.deleteRecursively()
                                    stagingDir.mkdirs()
                                    val src = "$ROOT_UXICONS_DIR/$pkgName"
                                    val cmd = "cp -f ${shQuote(src)}/*.png ${shQuote(stagingDir.absolutePath)}/ 2>/dev/null; echo done"
                                    runRootCommand(cmd, timeoutMs = 8000)
                                    val files = stagingDir.listFiles { _, name -> name.endsWith(".png") }
                                    if (files == null || files.isEmpty()) {
                                        withContext(Dispatchers.Main) {
                                            mainViewModel.updateTransfer { it -> it.copy(backupProgress = (ExportProgress(
                                                title = "备份中",
                                                completed = index + 1,
                                                total = pkgs.size,
                                                currentLabel = "已跳过 ${pkgName}（无图标）",
                                                isIndeterminate = false,
                                            ))) }
                                        }
                                        continue
                                    }
                                    withContext(Dispatchers.Main) {
                                        runCatching { exportToTree(contentResolver, treeUri, stagingDir) }.onSuccess { successCount++ }.onFailure { failCount++ }
                                        mainViewModel.updateTransfer { it -> it.copy(backupProgress = (ExportProgress(
                                            title = "备份中",
                                            completed = index + 1,
                                            total = pkgs.size,
                                            currentLabel = "已完成 ${index + 1}/${pkgs.size}: $pkgName",
                                            isIndeterminate = false,
                                        ))) }
                                    }
                                } catch (_: Exception) {
                                    failCount++
                                    withContext(Dispatchers.Main) {
                                        mainViewModel.updateTransfer { it -> it.copy(backupProgress = (ExportProgress(
                                            title = "备份中",
                                            completed = index + 1,
                                            total = pkgs.size,
                                            currentLabel = "失败 ${pkgName}",
                                            isIndeterminate = false,
                                        ))) }
                                    }
                                }
                            }
                            runCatching { stagingRoot.deleteRecursively() }
                            withContext(Dispatchers.Main) {
                                if (!mainViewModel.transfer.value.backupInBackground) {
                                    mainViewModel.updateTransfer { it -> it.copy(backupProgress = (null)) }
                                    mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (false)) }
                                }
                                if (failCount == 0) run {
            pickerToastStatus(
                        message = ("已备份 $successCount 个图标包"),
                        postOnUi = { text -> runOnUiThread { mainViewModel.updateShell { it -> it.copy(statusText = (text)) } } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act1, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        }
                                else run {
            pickerToastStatus(
                        message = ("已备份 $successCount 个，失败 $failCount 个"),
                        postOnUi = { text -> runOnUiThread { mainViewModel.updateShell { it -> it.copy(statusText = (text)) } } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act1, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        }
                            }
                        }
                    } catch (e: CancellationException) {
                        withContext(Dispatchers.Main) {
                            // 停止时已由 cancelBackup 清理
                        }
                        throw e
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            mainViewModel.updateTransfer { it -> it.copy(backupProgress = (null)) }
                            mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (false)) }
                            mainViewModel.updateTransfer { it -> it.copy(backupInBackground = (false)) }
                            run {
            pickerToastStatus(
                        message = ("备份失败: ${e.message ?: e.javaClass.simpleName}"),
                        postOnUi = { text -> runOnUiThread { mainViewModel.updateShell { it -> it.copy(statusText = (text)) } } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(__act1, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        }
                        }
                    } finally {
                        withContext(Dispatchers.Main) {
                            mainViewModel.updateShell { it -> it.copy(isBusy = (false)) }
                            backupJob = null
                            run {

                    backupDotJob?.cancel()
                    backupDotJob = null
        }
                            if (!mainViewModel.transfer.value.backupInBackground) {
                                mainViewModel.updateTransfer { it -> it.copy(backupProgress = (null)) }
                                mainViewModel.updateTransfer { it -> it.copy(backupSheetVisible = (false)) }
                                mainViewModel.updateTransfer { it -> it.copy(backupInBackground = (false)) }
                            }
                            // 若为后台，则保留 backupProgress 供设置页“备份中...”展示
                        }
                    }
                }
    } },
            )
}
        }
        item(key = "preview_strip") {
            run {
    PreviewStripSettingsCard(
                enabled = previewSession.previewStripEnabled,
                isBusy = shell.isBusy,
                onCheckedChange = { run {
        paramsUpdatePreviewStripEnabled(
                    enabled = (it),
                    getValue = { mainViewModel.previewSession.value.previewStripEnabled },
                    setValue = { mainViewModel.updatePreviewSession { v -> v.copy(previewStripEnabled = (it)) } },
                    onSave = { run {
            pickerSaveUiState(
                        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                        selectedPackage = mainViewModel.picker.value.selectedPackageName,
                        generatedFilter = mainViewModel.picker.value.generatedFilter,
                        showSystemApps = mainViewModel.picker.value.showSystemApps,
                        queryText = mainViewModel.picker.value.queryText,
                        advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                        advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                        previewPackage = mainViewModel.previewSession.value.previewPackageName,
                        previewDir = mainViewModel.previewSession.value.previewDirPath,
                        previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                        previewNormalLight = mainViewModel.params.value.previewNormalLight,
                        previewNormalDark = mainViewModel.params.value.previewNormalDark,
                        previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                        previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                        desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                        iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                        cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                        batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                        batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                        batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                        batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                        batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                        customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                        autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                        autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                        outputUri = mainViewModel.shell.value.outputTreeUri,
                    )
        } },
                    setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                )
    } },
            )
}
        }
        item(key = "wallpaper") {
            run {
    WallpaperSettingsCard(
                hasCustom = batchPreviewConfig.customWallpaperPath != null,
                customInfo = batchPreviewConfig.customWallpaperInfo,
                isBusy = shell.isBusy,
                onPickWallpaper = {
                    chooseWallpaperLauncher.launch(arrayOf("image/jpeg", "image/png", "image/webp"))
                },
                onClearWallpaper = { run {
        pickerClearCustomWallpaper(
                    filesDir = filesDir,
                    customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                    fileName = CUSTOM_WALLPAPER_FILE,
                    onCleared = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                    onSave = {  -> run {
            pickerSaveUiState(
                        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                        selectedPackage = mainViewModel.picker.value.selectedPackageName,
                        generatedFilter = mainViewModel.picker.value.generatedFilter,
                        showSystemApps = mainViewModel.picker.value.showSystemApps,
                        queryText = mainViewModel.picker.value.queryText,
                        advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                        advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                        previewPackage = mainViewModel.previewSession.value.previewPackageName,
                        previewDir = mainViewModel.previewSession.value.previewDirPath,
                        previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                        previewNormalLight = mainViewModel.params.value.previewNormalLight,
                        previewNormalDark = mainViewModel.params.value.previewNormalDark,
                        previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                        previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                        desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                        iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                        cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                        batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                        batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                        batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                        batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                        batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                        customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                        autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                        autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                        outputUri = mainViewModel.shell.value.outputTreeUri,
                    )
        } },
                    clearCache = {
                        cachedCustomWallpaper = null
                        cachedCustomWallpaperPath = null
                    },
                    setPath = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(customWallpaperPath = (it)) } },
                    setInfo = { mainViewModel.updateBatchPreviewConfig { v -> v.copy(customWallpaperInfo = (it)) } },
                )
    } },
            )
}
        }
        item(key = "gpt") {
            run {
    GptSettingsCard(
                tuningState = mainViewModel.params.collectAsState().value,
                isBusy = shell.isBusy,
                gptModelId = gptRmbgSettings.gptModelId,
                gptBaseUrl = gptRmbgSettings.gptBaseUrl,
                gptApiKey = gptRmbgSettings.gptApiKey,
                gptRunCount = presetUi.gptRunCount,
                onGptImageModeChange = { mode ->
                    mainViewModel.updateLive { p -> p.copy(gptImageMode = (mode).value) }
                    mainViewModel.updateGptRmbgSettings { it -> it.copy(gptSettingsSaveStatus = ("")) }
                },
                onGptPromptPresetChange = { preset ->
                    mainViewModel.updateLive { p -> p.copy(gptPromptPreset = (preset).value) }
                    mainViewModel.updateGptRmbgSettings { it -> it.copy(gptSettingsSaveStatus = ("")) }
                },
                onGptCustomPromptChange = {
                    mainViewModel.updateLive { p -> p.copy(gptCustomPrompt = it) }
                    mainViewModel.updateGptRmbgSettings { it -> it.copy(gptSettingsSaveStatus = ("")) }
                },
                onGptModelIdChange = {
                    mainViewModel.updateGptRmbgSettings { v -> v.copy(gptModelId = (it)) }
                    mainViewModel.updateGptRmbgSettings { it -> it.copy(gptSettingsSaveStatus = ("")) }
                },
                onGptBaseUrlChange = {
                    mainViewModel.updateGptRmbgSettings { v -> v.copy(gptBaseUrl = (it)) }
                    mainViewModel.updateGptRmbgSettings { it -> it.copy(gptSettingsSaveStatus = ("")) }
                },
                onGptApiKeyChange = {
                    mainViewModel.updateGptRmbgSettings { v -> v.copy(gptApiKey = (it)) }
                    mainViewModel.updateGptRmbgSettings { it -> it.copy(gptSettingsSaveStatus = ("")) }
                },
            )
}
        }
        item(key = "rmbg") {
            run {
    RmbgComponentCard(
                component = remember(gptRmbgSettings.rmbgComponentStatus) { run {
        findRmbgComponent(filesDir)
    } },
                rmbgRunCount = presetUi.rmbgRunCount,
                currentPreset = run {
        paramsCurrentRmbgModelPreset(componentUrl = gptRmbgSettings.rmbgComponentUrl)
    },
                allPresets = RMBG_MODEL_PRESETS,
                lastError = previewSession.lastRmbgCandidateError,
                componentUrl = gptRmbgSettings.rmbgComponentUrl,
                isBusy = shell.isBusy,
                isGenerating = previewSession.isGeneratingRmbgCandidate,
                isInstalling = previewSession.isInstallingRmbgComponent,
                installStage = previewSession.rmbgInstallStage,
                installProgress = previewSession.rmbgInstallProgress,
                dialogVisible = previewSession.rmbgDialogVisible,
                onPresetSelected = { run {
        paramsUpdateRmbgModelPreset(
                    preset = (it),
                    setComponentUrl = { mainViewModel.updateGptRmbgSettings { v -> v.copy(rmbgComponentUrl = (it)) } },
                    setSaveStatus = { mainViewModel.updateGptRmbgSettings { v -> v.copy(rmbgComponentSaveStatus = (it)) } },
                    setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                )
    } },
                onComponentUrlChange = {
                    mainViewModel.updateGptRmbgSettings { v -> v.copy(rmbgComponentUrl = (it)) }
                    mainViewModel.updateGptRmbgSettings { it -> it.copy(rmbgComponentSaveStatus = ("")) }
                },
                onDialogVisibleChange = { mainViewModel.updatePreviewSession { v -> v.copy(rmbgDialogVisible = (it)) } },
                onPickZip = {
                    chooseRmbgComponentLauncher.launch(
                        arrayOf("application/zip", "application/octet-stream", "*/*"),
                    )
                },
                onInstallFromUrl = { run {

                installRmbgComponentFromUrl(
                    urlText = mainViewModel.gptRmbgSettings.value.rmbgComponentUrl,
                    filesDir = filesDir,
                    cacheDir = cacheDir,
                    isBusy = mainViewModel.shell.value.isBusy,
                    isGeneratingRmbgCandidate = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
                    isInstallingRmbgComponent = mainViewModel.previewSession.value.isInstallingRmbgComponent,
                    isDebugBuild = run {
            (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        },
                    onSaveSettings = { run {
            paramsSaveRmbgSettings(
                        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                        getComponentUrl = { mainViewModel.gptRmbgSettings.value.rmbgComponentUrl },
                    )
        } },
                    getRuntime = { rmbgRuntime },
                    setRuntime = { rmbgRuntime = it },
                    setInstalling = { mainViewModel.updatePreviewSession { v -> v.copy(isInstallingRmbgComponent = (it)) } },
                    setStage = { mainViewModel.updatePreviewSession { v -> v.copy(rmbgInstallStage = (it)) } },
                    setProgress = { mainViewModel.updatePreviewSession { v -> v.copy(rmbgInstallProgress = (it)) } },
                    setStatus = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
                    setComponentStatus = { mainViewModel.updateGptRmbgSettings { v -> v.copy(rmbgComponentStatus = (it)) } },
                    setComponentSaveStatus = { mainViewModel.updateGptRmbgSettings { v -> v.copy(rmbgComponentSaveStatus = (it)) } },
                    setLastError = { mainViewModel.updatePreviewSession { v -> v.copy(lastRmbgCandidateError = (it)) } },
                    runOnUi = { runOnUiThread(it) },
                )
    } },
                onClearInstalled = { run {

                clearInstalledRmbgComponent(
                    filesDir = filesDir,
                    isBusy = mainViewModel.shell.value.isBusy,
                    isGeneratingRmbgCandidate = mainViewModel.previewSession.value.isGeneratingRmbgCandidate,
                    isInstallingRmbgComponent = mainViewModel.previewSession.value.isInstallingRmbgComponent,
                    closeRuntime = {
                        runCatching { rmbgRuntime?.close() }
                        rmbgRuntime = null
                    },
                    onClearUiState = { run {

                    if (mainViewModel.previewSession.value.isGeneratingRmbgCandidate) {
                        return@run
                    }
                    mainViewModel.updatePreviewSession { it -> it.copy(lastRmbgCandidateError = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidatePackageName = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateMode = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateStatusText = ("")) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailurePackageName = (null)) }
                    mainViewModel.updatePreviewSession { it -> it.copy(rmbgCandidateFailureMode = (null)) }
        } },
                    onResult = { deleted ->
                        mainViewModel.updatePreviewSession { it -> it.copy(lastRmbgInferenceReport = (null)) }
                        mainViewModel.updateGptRmbgSettings { it -> it.copy(rmbgComponentStatus = ("${System.currentTimeMillis()}")) }
                        mainViewModel.updatePreviewSession { it -> it.copy(rmbgInstallStage = ("")) }
                        mainViewModel.updatePreviewSession { it -> it.copy(rmbgInstallProgress = (null)) }
                        mainViewModel.updateGptRmbgSettings { it -> it.copy(rmbgComponentSaveStatus = ("")) }
                        mainViewModel.updateShell { it -> it.copy(statusText = (if (deleted) "已清除 RMBG" else "没有已安装 RMBG")) }
                    },
                )
    } },
            )
}
        }
        item(key = "bottom_bar") {
            SectionCard(rowsFullBleed = true) {
                LibrarySettingRow(
                    title = "悬浮底栏",
                    summary = if (glassBar.liquidGlassBottomBarEnabled) "已开启" else "已关闭",
                    icon = SettingsIconKind.Glass,
                    showSwitch = true,
                    checked = glassBar.liquidGlassBottomBarEnabled,
                    enabled = !shell.isBusy,
                    onCheckedChange = {
                        mainViewModel.updateGlassBar { v -> v.copy(liquidGlassBottomBarEnabled = (it)) }
                        run {
    paramsSaveLiquidGlassSettings(
                prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                getParams = { mainViewModel.params.value },
                getBottomBarEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarEnabled },
                getBottomBarBlurEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarBlurEnabled },
            )
}
                        mainViewModel.updateShell { it -> it.copy(statusText = (if (mainViewModel.glassBar.value.liquidGlassBottomBarEnabled) "悬浮底栏已开启" else "悬浮底栏已关闭")) }
                    },
                )
                SectionDivider()
                LibrarySettingRow(
                    title = "底栏模糊",
                    summary = when {
                        !glassBar.liquidGlassBottomBarEnabled -> "需先开启悬浮底栏"
                        glassBar.liquidGlassBottomBarBlurEnabled -> "已开启"
                        else -> "已关闭"
                    },
                    icon = SettingsIconKind.Glass,
                    showSwitch = true,
                    checked = glassBar.liquidGlassBottomBarBlurEnabled,
                    enabled = !shell.isBusy && glassBar.liquidGlassBottomBarEnabled,
                    onCheckedChange = {
                        mainViewModel.updateGlassBar { v -> v.copy(liquidGlassBottomBarBlurEnabled = (it)) }
                        run {
    paramsSaveLiquidGlassSettings(
                prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                getParams = { mainViewModel.params.value },
                getBottomBarEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarEnabled },
                getBottomBarBlurEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarBlurEnabled },
            )
}
                        mainViewModel.updateShell { it -> it.copy(statusText = (if (mainViewModel.glassBar.value.liquidGlassBottomBarBlurEnabled) "底栏模糊已开启" else "底栏模糊已关闭")) }
                    },
                )
            }
        }
        item(key = "reset_defaults") {
            SectionCard(rowsFullBleed = true) {
                LibrarySettingRow(
                    title = "恢复默认配置",
                    summary = "恢复后可通过首页预设卡片的「还原上一步」撤销",
                    icon = settingsIconForTitle("恢复默认配置"),
                    showValue = false,
                    showArrowRight = true,
                    enabled = !shell.isBusy,
                    onClick = { mainViewModel.updatePreviewSession { it -> it.copy(resetDefaultsDialogVisible = (true)) } },
                )
                if (previewSession.resetDefaultsDialogVisible) {
                    ArtDialog(
                        title = "恢复默认配置",
                        summary = "一键恢复全部调参到出厂默认值，不会删除本地 RMBG 模型与已生成的图标包。恢复后可通过预设卡片的「还原上一步」撤销。",
                        onDismissRequest = { mainViewModel.updatePreviewSession { it -> it.copy(resetDefaultsDialogVisible = (false)) } },
                        centered = true,
                        negative = DialogButtonBarAction(
                            text = "取消",
                            onClick = { mainViewModel.updatePreviewSession { it -> it.copy(resetDefaultsDialogVisible = (false)) } },
                        ),
                        positive = DialogButtonBarAction(
                            text = "恢复默认",
                            enabled = !shell.isBusy,
                            recommended = true,
                            onClick = {
                                mainViewModel.updatePreviewSession { it -> it.copy(resetDefaultsDialogVisible = (false)) }
                                run {
paramsResetToDefaults(
        confirmed = (false),
        isBusy = { mainViewModel.shell.value.isBusy },
        isGeneratingGpt = { mainViewModel.previewSession.value.isGeneratingGptCandidate },
        isGeneratingRmbg = { mainViewModel.previewSession.value.isGeneratingRmbgCandidate },
        setStatusText = { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } },
        onRequestConfirm = { title, message, confirmLabel, onConfirm ->
            run {

        mainViewModel.updateConfirm { it -> it.copy(pendingServiceConfirm = (ServiceConfirmRequest(
            title = (title),
            message = (message),
            confirmLabel = (confirmLabel),
            onConfirm = (onConfirm),
        ))) }
}
        },
        onApplyDefaults = { run {
paramsApplyTuningParams(
            params = (it),
            rebuildCandidates = (true),
            persist = (true),
            captureUndo = (true),
            refreshPreview = (true),
            getBefore = { run {
    mainViewModel.params.value
} },
            onCaptureUndo = { mainViewModel.updatePreviewSession { v -> v.copy(lastParamsSnapshot = (it)) } },
            onParamsApplied = { before, applied, capture -> mainViewModel.onParamsApplied(before = before, applied = applied, captureUndo = capture) },
            setDraftForegroundSubjectPercentText = { draftForegroundSubjectPercentText = it },
            setDraftForegroundShadowLevelText = { draftForegroundShadowLevelText = it },
            setDraftMonochromeThemeScaleText = { draftMonochromeThemeScaleText = it },
            setDraftBackgroundSeparationText = { draftBackgroundSeparationText = it },
            setDraftPlateRemovalText = { draftPlateRemovalText = it },
            setDraftShadowRemovalText = { draftShadowRemovalText = it },
            setDraftEdgePolishText = { draftEdgePolishText = it },
            setDraftRmbgAlphaStrengthText = { draftRmbgAlphaStrengthText = it },
            setDraftRmbgEdgeFeatherText = { draftRmbgEdgeFeatherText = it },
            setDraftRmbgEdgeAdjustText = { draftRmbgEdgeAdjustText = it },
            setDraftRmbgWeakAlphaKeepText = { draftRmbgWeakAlphaKeepText = it },
            setDraftLiquidGlassRadiusText = { draftLiquidGlassRadiusText = it },
            setDraftLiquidGlassOuterWidthText = { draftLiquidGlassOuterWidthText = it },
            setDraftLiquidGlassTopAlphaText = { draftLiquidGlassTopAlphaText = it },
            setDraftLiquidGlassBottomAlphaText = { draftLiquidGlassBottomAlphaText = it },
            setDraftLiquidGlassBackgroundMistAlphaText = { draftLiquidGlassBackgroundMistAlphaText = it },
            setDraftLiquidGlassBottomDarkAlphaText = { draftLiquidGlassBottomDarkAlphaText = it },
            setDraftLiquidGlassSubjectScaleText = { draftLiquidGlassSubjectScaleText = it },
            setDraftLiquidGlassSubjectOutlineWidthText = { draftLiquidGlassSubjectOutlineWidthText = it },
            setDraftLiquidGlassSubjectInnerOutlineWidthText = { draftLiquidGlassSubjectInnerOutlineWidthText = it },
            setDraftLiquidGlassSubjectShadowAlphaText = { draftLiquidGlassSubjectShadowAlphaText = it },
            setDraftLiquidGlassSubjectOpacityText = { draftLiquidGlassSubjectOpacityText = it },
            setDraftJsonParamsText = { draftJsonParamsText = it },
            onSaveLocalSeparation = { run {
    paramsSaveLocalSeparationSettings(
                prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                getParams = { mainViewModel.params.value },
            )
} },
            onSaveImageTuning = { run {
    paramsSaveImageTuningSettings(
                prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                getParams = { mainViewModel.params.value },
            )
} },
            onSaveLiquidGlass = { run {
    paramsSaveLiquidGlassSettings(
                prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                getParams = { mainViewModel.params.value },
                getBottomBarEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarEnabled },
                getBottomBarBlurEnabled = { mainViewModel.glassBar.value.liquidGlassBottomBarBlurEnabled },
            )
} },
            onSaveGpt = { run {
    paramsSaveGptSettings(
                prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                getParams = { mainViewModel.params.value },
                getGptApiKey = { mainViewModel.gptRmbgSettings.value.gptApiKey },
                getGptModelId = { mainViewModel.gptRmbgSettings.value.gptModelId },
                getGptBaseUrl = { mainViewModel.gptRmbgSettings.value.gptBaseUrl },
            )
} },
            onSaveUi = { run {
    pickerSaveUiState(
                prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                selectedPackage = mainViewModel.picker.value.selectedPackageName,
                generatedFilter = mainViewModel.picker.value.generatedFilter,
                showSystemApps = mainViewModel.picker.value.showSystemApps,
                queryText = mainViewModel.picker.value.queryText,
                advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                previewPackage = mainViewModel.previewSession.value.previewPackageName,
                previewDir = mainViewModel.previewSession.value.previewDirPath,
                previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                previewNormalLight = mainViewModel.params.value.previewNormalLight,
                previewNormalDark = mainViewModel.params.value.previewNormalDark,
                previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                outputUri = mainViewModel.shell.value.outputTreeUri,
            )
} },
            isBusy = { mainViewModel.shell.value.isBusy },
            getSession = { mainViewModel.previewSession.value.activeGenerationSession },
            onRefresh = { rebuild -> run {
    homeRefreshActivePreviewOutputs(
                currentSession = mainViewModel.previewSession.value.activeGenerationSession,
                rebuildLocalCandidates = (rebuild),
                retargetFrom = (null),
                app = mainViewModel.previewSession.value.activeGenerationSession?.let { s -> apps.firstOrNull { it.packageName == s.packageName } },
                currentSelections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
                scope = previewWorkerScope,
                getJob = { previewOutputJob },
                setJob = { previewOutputJob = it },
                incRevision = { ++previewOutputRevision },
                getRevision = { previewOutputRevision },
                setRefreshing = { mainViewModel.updatePreviewSession { v -> v.copy(isPreviewOutputRefreshing = (it)) } },
                rebuildDebounceMs = PREVIEW_REBUILD_DEBOUNCE_MS,
                outputDebounceMs = PREVIEW_OUTPUT_DEBOUNCE_MS,
                tuning = run {
        mainViewModel.params.value
    },
                onRebuild = { session, app, tuning -> rebuildLocalSession(session, app, packageManager, tuning) },
                defaultLocal = { auto -> run {
        run {
            when ((LocalSeparationMode.Auto)) {
                        LocalSeparationMode.Original -> PreviewChoice.Original
                        LocalSeparationMode.Plate -> PreviewChoice.Full
                        LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                        LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                        LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                        LocalSeparationMode.Auto -> ((auto))
                        LocalSeparationMode.Full -> PreviewChoice.Full
                    }
        }
    } },
                normalize = { session, selections -> normalizePreviewSelections(session, selections) },
                onWrite = { session, selections -> run {

                val params = mainViewModel.params.value
                writePackageOutputs(
                    session = (session),
                    selections = (selections),
                    edgePolishPercent = params.edgePolishPercent,
                    foregroundSubjectPercent = params.foregroundSubjectPercent,
                    rmbgTunedForeground = { __a0: IconCandidate -> run {

                    val params = mainViewModel.params.value
                    return@run rmbgTunedForegroundRaw(
                        candidate = __a0,
                        rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
                        rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
                        rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
                        rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
                    )
        } },
                    liquidGlassEnabled = params.liquidGlassEnabled,
                    liquidGlassBackgroundMistAlpha = params.liquidGlassBackgroundMistAlpha,
                    liquidGlassTopAlpha = params.liquidGlassTopAlpha,
                    liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
                    liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
                    liquidGlassOuterWidth = params.liquidGlassOuterWidth,
                    liquidGlassRadius = params.liquidGlassRadius,
                    liquidGlassSubjectScalePercent = params.liquidGlassSubjectScalePercent,
                    liquidGlassSubjectShadowAlpha = params.liquidGlassSubjectShadowAlpha,
                    liquidGlassSubjectOutlineWidth = params.liquidGlassSubjectOutlineWidth,
                    liquidGlassSubjectInnerOutlineWidth = params.liquidGlassSubjectInnerOutlineWidth,
                    liquidGlassSubjectOpacityPercent = params.liquidGlassSubjectOpacityPercent,
                    foregroundShadowLevel = params.foregroundShadowLevel,
                    monochromeThemeScale = params.monochromeThemeScale,
                    nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
                )
    } },
                onCommit = { session, selections ->
                    mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                    mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                    mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                    run {
        pickerSaveUiState(
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    selectedPackage = mainViewModel.picker.value.selectedPackageName,
                    generatedFilter = mainViewModel.picker.value.generatedFilter,
                    showSystemApps = mainViewModel.picker.value.showSystemApps,
                    queryText = mainViewModel.picker.value.queryText,
                    advancedCategory = mainViewModel.shell.value.advancedSettingsCategory,
                    advancedTab = mainViewModel.shell.value.advancedSettingsTab,
                    previewPackage = mainViewModel.previewSession.value.previewPackageName,
                    previewDir = mainViewModel.previewSession.value.previewDirPath,
                    previewStrip = mainViewModel.previewSession.value.previewStripEnabled,
                    previewNormalLight = mainViewModel.params.value.previewNormalLight,
                    previewNormalDark = mainViewModel.params.value.previewNormalDark,
                    previewMonochromeLight = mainViewModel.params.value.previewMonochromeLight,
                    previewMonochromeDark = mainViewModel.params.value.previewMonochromeDark,
                    desktopBackground = mainViewModel.previewSession.value.previewDesktopBackground,
                    iconSize = mainViewModel.previewSession.value.previewIconSizeDp,
                    cornerRadius = mainViewModel.previewSession.value.previewCornerRadiusDp,
                    batchCount = mainViewModel.batchPreviewConfig.value.batchPreviewCount,
                    batchColumns = mainViewModel.batchPreviewConfig.value.batchPreviewColumns,
                    batchIconSize = mainViewModel.batchPreviewConfig.value.batchPreviewIconSizeDp,
                    batchCorner = mainViewModel.batchPreviewConfig.value.batchPreviewCornerRadiusDp,
                    batchDesktopBg = mainViewModel.batchPreviewConfig.value.batchPreviewDesktopBackground,
                    customPath = mainViewModel.batchPreviewConfig.value.customWallpaperPath,
                    autoRoot = mainViewModel.confirm.value.autoConfirmRootWrite,
                    autoRefresh = mainViewModel.confirm.value.autoConfirmRefresh,
                    outputUri = mainViewModel.shell.value.outputTreeUri,
                )
    }
                            },
                            onStatus = { run {

                            pickerPostStatus((it)) { runOnUiThread { mainViewModel.updateShell { v -> v.copy(statusText = (it)) } } }
                } },
                        )
            } },
                    )
        } },
                    onClearPreset = {
                        presetStore.activePresetId = null
                        mainViewModel.updatePresetUi { it -> it.copy(activePresetId = (null)) }
                    },
                )
    }
                            },
                        ),
                    )
                }
            }
        }
        item(key = "about_section") {
            SectionCard(rowsFullBleed = true) {
                LibrarySettingRow(
                    title = "导出引导",
                    summary = "首次引导与全量备份入口",
                    icon = settingsIconForTitle("导出引导"),
                    showArrowRight = true,
                    enabled = !shell.isBusy,
                    onClick = { mainViewModel.updateShell { it -> it.copy(onboardingVisible = (true)) } },
                )
                SectionDivider()
                LibrarySettingRow(
                    title = "关于",
                    summary = "源码、开源协议与更新",
                    icon = SettingsIconKind.Link,
                    showArrowRight = true,
                    enabled = !shell.isBusy,
                    onClick = { mainViewModel.updateShell { it -> it.copy(currentPage = (AppPage.About)) } },
                )
            }
        }
    }
}
