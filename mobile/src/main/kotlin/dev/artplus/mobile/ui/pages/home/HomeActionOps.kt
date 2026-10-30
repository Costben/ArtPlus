package dev.artplus.mobile.ui.pages.home

import android.content.Context
import android.widget.Toast
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.artplus.mobile.MainActivity
import dev.artplus.mobile.PREFS_NAME
import dev.artplus.mobile.ROOT_UXICONS_DIR
import dev.artplus.mobile.state.launchUiFriendly
import dev.artplus.mobile.state.markPackageWritten
import dev.artplus.mobile.state.persistUiState
import dev.artplus.mobile.state.setStatus
import dev.artplus.mobile.system.RootWriteMode
import dev.artplus.mobile.system.artPlusPackageDir
import dev.artplus.mobile.system.ensureNomediaAtTreeRoot
import dev.artplus.mobile.system.exportToTree
import dev.artplus.mobile.system.exportToTreeFast
import dev.artplus.mobile.system.hasGeneratedPackageBaseAssets
import dev.artplus.mobile.system.installWithRoot
import dev.artplus.mobile.ui.cards.ExportProgress
import dev.artplus.mobile.ui.cards.PreviewSelections
import dev.artplus.mobile.ui.cards.RootWriteConfirmRequest
import dev.artplus.mobile.ui.pages.picker.pickerToastStatus

/**
 * 底栏两个融合球背后的动作本体。
 *
 * 两个入口原先长在主页卡片里（导出与写入各占一个按钮），现在挂在全局底栏的配件球上。
 * 底栏由 Shell 绘制、拿不到 tab 页的局部变量，所以动作提到 Activity 级：
 * 面板只负责选「生成什么 / 写哪几套」，真正干活的是这两个函数。
 */

/** 「本地导出」：把当前选中的应用图标包导出到设置里的外部目录（SAF 直拷优先）。 */
internal fun MainActivity.exportCurrentPackage() {
    run {

                if (mainViewModel.shell.value.outputTreeUri == null) {
            pickerToastStatus(
                        message = ("还没有设置目录"),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(this@exportCurrentPackage, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        
                    mainViewModel.updatePreviewSession { it -> it.copy(exportDialogVisible = (true)) }
                    return@run
                }
                val dir = mainViewModel.previewSession.value.activeGenerationSession?.outDir
                    ?: mainViewModel.previewSession.value.previewDirPath?.let { File(it) }?.takeIf { it.isDirectory && hasGeneratedPackageBaseAssets(it) }
                    ?: mainViewModel.picker.value.selectedPackageName?.let { run {
            artPlusPackageDir(
                        packageName = (it),
                        externalArtPlusDir = getExternalFilesDir("ArtPlus"),
                        filesDir = filesDir,
                    )
        } }?.takeIf { hasGeneratedPackageBaseAssets(it) }
                if (dir == null || !hasGeneratedPackageBaseAssets(dir)) {
            pickerToastStatus(
                        message = ("没有可导出的图标包"),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(this@exportCurrentPackage, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        
                    return@run
                }
                if (mainViewModel.shell.value.isBusy) return@run
                mainViewModel.updateShell { it -> it.copy(isBusy = (true)) }
                mainViewModel.updateTransfer { it -> it.copy(exportProgress = (ExportProgress(
                    title = "导出中",
                    completed = 0,
                    total = 1,
                    currentLabel = "正在导出: ${dir.name}",
                    isIndeterminate = true,
                ))) }
                mainViewModel.updateTransfer { it -> it.copy(singleExportSheetVisible = (true)) }
                singleExportJob?.cancel()
                singleExportJob = mainScope.launch(Dispatchers.IO) {
                    try {
                        runCatching { ensureNomediaAtTreeRoot(contentResolver, mainViewModel.shell.value.outputTreeUri) }
                        // 优先尝试文件系统直拷（su cp），速度为 SAF 的 10-20 倍，失败再回退 SAF
                        val fastOk = runCatching { exportToTreeFast(mainViewModel.shell.value.outputTreeUri, dir) }.getOrDefault(false)
                        if (fastOk) {
                            withContext(Dispatchers.Main) { run {
            pickerToastStatus(
                        message = ("已导出到外部目录: ${dir.name}"),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(this@exportCurrentPackage, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        } }
                        } else {
                            withContext(Dispatchers.Main) {
                                runCatching { exportToTree(contentResolver, mainViewModel.shell.value.outputTreeUri, dir) }
                                    .onSuccess { run {
            pickerToastStatus(
                        message = ("已导出到外部目录: ${dir.name}"),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(this@exportCurrentPackage, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        } }
                                    .onFailure { error ->
            pickerToastStatus(
                        message = ("导出失败: ${error.message ?: error.javaClass.simpleName}"),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(this@exportCurrentPackage, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
         }
                            }
                        }
                    } catch (e: CancellationException) {
                        withContext(Dispatchers.Main) { /* 已在 cancelSingleExport 中处理 */ }
                        throw e
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) { run {
            pickerToastStatus(
                        message = ("导出失败: ${e.message ?: e.javaClass.simpleName}"),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(this@exportCurrentPackage, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        } }
                    } finally {
                        withContext(Dispatchers.Main) {
                            mainViewModel.updateTransfer { it -> it.copy(exportProgress = (null)) }
                            mainViewModel.updateTransfer { it -> it.copy(singleExportSheetVisible = (false)) }
                            singleExportJob = null
                            mainViewModel.updateShell { it -> it.copy(isBusy = (false)) }
                        }
                    }
                }
    }
}

/** 「写入」：把选中的应用按 [mode] 写进 `/data/oplus/uxicons/<包名>/`，首次接管先备份原图。 */
internal fun MainActivity.writeSelectedWithRoot(mode: RootWriteMode) {
    run {
        homeWriteSelectedWithRoot(
                    entry = apps.firstOrNull { it.packageName == mainViewModel.picker.value.selectedPackageName },
                    rootWriteMode = (mode),
                    isBusy = mainViewModel.shell.value.isBusy,
                    activeSession = mainViewModel.previewSession.value.activeGenerationSession,
                    selections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
                    autoConfirmRootWrite = mainViewModel.confirm.value.autoConfirmRootWrite,
                    targetPath = apps.firstOrNull { it.packageName == mainViewModel.picker.value.selectedPackageName }?.let { "$ROOT_UXICONS_DIR/${it.packageName}" },
                    onStatusText = { setStatus((it)) },
                    onGenerateFallback = { generateSelectedApp(installWithRoot = true, useGpt = false, rootWriteMode = (mode)) },
                    onBeginBusy = { msg ->
                        mainViewModel.updateShell { it -> it.copy(isBusy = true, statusText = msg) }
                    },
                    onLaunch = { name, block -> mainViewModel.launchUiFriendly((name), (block)) },
                    onWrite = tuningActions.write,
                    onInstall = { outDir, pkg, mode -> installWithRoot(outDir, pkg, mode) },
                    onPostWrite = { session, selections, e ->
                        runOnUiThread {
                            mainViewModel.markPackageWritten(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), e.packageName)
                            mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                            mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                            mainViewModel.updatePreviewSession { v -> v.copy(previewPackageName = (e.packageName), previewDirPath = (session.outDir.absolutePath)) }
                            mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                            persistUiState()
                        }
                    },
                    onToast = { run {
            pickerToastStatus(
                        message = (it),
                        postOnUi = { text -> runOnUiThread { setStatus((text)) } },
                        showToast = { text ->
                            runOnUiThread {
                                Toast.makeText(this@writeSelectedWithRoot, text, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
        } },
                    onFinish = { runOnUiThread { mainViewModel.updateShell { it -> it.copy(isBusy = (false)) } } },
                    onRequestConfirm = { pkg, targetPath, mode, onConfirm ->
                        mainViewModel.updateConfirm { it -> it.copy(rootWriteConfirmRememberSkip = (false)) }
                        mainViewModel.updateConfirm { it -> it.copy(pendingRootWriteConfirm = (RootWriteConfirmRequest(
                            packageName = pkg,
                            targetPath = targetPath,
                            rootWriteMode = mode,
                            onConfirm = { onConfirm() },
                        ))) }
                    },
                )
    }
}
