package dev.artplus.mobile.ui.pages.settings

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.DropdownEntry
import io.github.suqi8.coui.kmp.basic.DropdownItem
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TextField
import io.github.suqi8.coui.kmp.layout.DialogButtonBarAction
import io.github.suqi8.coui.kmp.theme.COUITheme
import dev.artplus.mobile.MAX_BATCH_PREVIEW_COUNT
import dev.artplus.mobile.MIN_BATCH_PREVIEW_COUNT
import dev.artplus.mobile.TuningParams
import dev.artplus.mobile.pipeline.GptImageMode
import dev.artplus.mobile.pipeline.GptPromptPreset
import dev.artplus.mobile.ui.cards.NumberParameterControl
import dev.artplus.mobile.ui.cards.RmbgComponent
import dev.artplus.mobile.ui.cards.RmbgModelPreset
import dev.artplus.mobile.ui.components.ArtDialog
import dev.artplus.mobile.ui.components.LibraryChoiceRow
import dev.artplus.mobile.ui.components.LibrarySettingRow
import dev.artplus.mobile.ui.components.RmbgInstallProgressBar
import dev.artplus.mobile.ui.components.SectionCard
import dev.artplus.mobile.ui.components.SectionDivider
import dev.artplus.mobile.ui.components.SettingsIconKind
import dev.artplus.mobile.ui.components.SettingsInfoRow
import dev.artplus.mobile.ui.components.SettingsTextInputRow
import dev.artplus.mobile.ui.components.settingsIconForTitle

/**
 * Slice 2.3：设置页应用/输出/AI/RMBG 卡片（原 MainActivity 本体原样搬迁）。
 * 只做纯移动：文案/布局/参数范围一律不变。
 * Activity 状态经参数注入，调参存取（update*，Slice 2.4）与动作（安装/备份/清理/选择器）
 * 经回调注入；MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动。
 * 唯 InputSettingsCard 为纯 UI（仅收参），直接搬迁、不留 wrapper（避免同名同参）。
 */

@Composable
internal fun WallpaperSettingsCard(
    hasCustom: Boolean,
    customInfo: String,
    isBusy: Boolean,
    onPickWallpaper: () -> Unit,
    onClearWallpaper: () -> Unit,
) {
    SectionCard(rowsFullBleed = true, title = "预览壁纸") {
        LibrarySettingRow(
            title = "上传自定义壁纸",
            summary = if (hasCustom) {
                "已上传${customInfo.takeIf { it.isNotBlank() }?.let { "（$it）" }.orEmpty()}，「桌面」背景优先使用 · 自动居中裁剪 16:9（不缩放不变形）"
            } else {
                "「桌面」背景当前用系统壁纸/内置壁纸 · 上传后自动居中裁剪 16:9（不缩放不变形）"
            },
            icon = SettingsIconKind.FileUpload,
            showValue = false,
            showArrowRight = true,
            enabled = !isBusy,
            onClick = onPickWallpaper,
        )
        if (hasCustom) {
            LibrarySettingRow(
                title = "清除自定义壁纸",
                summary = "恢复为系统壁纸/内置壁纸",
                icon = SettingsIconKind.Eraser,
                showValue = false,
                showArrowRight = true,
                enabled = !isBusy,
                onClick = onClearWallpaper,
            )
        }
    }
}

@Composable
internal fun BatchPreviewSettingsCard(
    value: Int,
    draftText: String,
    isBusy: Boolean,
    onDraftChange: (String) -> Unit,
    onSave: (Int) -> Unit,
) {
    SectionCard(rowsFullBleed = true, title = "批量预览") {
        NumberParameterControl(
            busy = isBusy,
            title = "批量预览数量",
            summary = "预设批量生成预览时随机抓取的应用数量（默认 20，优先未生成图标应用）",
            value = value,
            draftText = draftText,
            min = MIN_BATCH_PREVIEW_COUNT,
            max = MAX_BATCH_PREVIEW_COUNT,
            step = 1,
            onDraftChange = onDraftChange,
            onSave = onSave,
            icon = SettingsIconKind.Grid,
        )
    }
}

@Composable
internal fun GptSettingsCard(
    tuningState: TuningParams,
    isBusy: Boolean,
    gptModelId: String,
    gptBaseUrl: String,
    gptApiKey: String,
    gptRunCount: Int,
    onGptImageModeChange: (GptImageMode) -> Unit,
    onGptPromptPresetChange: (GptPromptPreset) -> Unit,
    onGptCustomPromptChange: (String) -> Unit,
    onGptModelIdChange: (String) -> Unit,
    onGptBaseUrlChange: (String) -> Unit,
    onGptApiKeyChange: (String) -> Unit,
) {
    SectionCard(rowsFullBleed = true, title = "大模型") {
        LibraryChoiceRow(
            title = "调用方式",
            summary = "选择 AI 生图的调用方式",
            value = GptImageMode.fromValue(tuningState.gptImageMode).label,
            icon = SettingsIconKind.Spark,
            enabled = !isBusy,
            entry = remember(GptImageMode.fromValue(tuningState.gptImageMode)) {
                DropdownEntry(
                    items = GptImageMode.entries.map { mode ->
                        DropdownItem(
                            text = mode.label,
                            selected = mode == GptImageMode.fromValue(tuningState.gptImageMode),
                            onClick = { onGptImageModeChange(mode) },
                        )
                    },
                )
            },
        )
        SectionDivider()
        LibraryChoiceRow(
            title = "AI 提示词",
            summary = GptPromptPreset.fromValue(tuningState.gptPromptPreset).summary,
            value = GptPromptPreset.fromValue(tuningState.gptPromptPreset).label,
            icon = SettingsIconKind.Prompt,
            enabled = !isBusy,
            entry = remember(GptPromptPreset.fromValue(tuningState.gptPromptPreset)) {
                DropdownEntry(
                    items = GptPromptPreset.entries.map { preset ->
                        DropdownItem(
                            text = preset.label,
                            summary = preset.summary,
                            selected = preset == GptPromptPreset.fromValue(tuningState.gptPromptPreset),
                            onClick = { onGptPromptPresetChange(preset) },
                        )
                    },
                )
            },
        )
        AnimatedVisibility(
            visible = GptPromptPreset.fromValue(tuningState.gptPromptPreset) == GptPromptPreset.Custom,
            enter = fadeIn(animationSpec = tween(durationMillis = 150)) +
                expandVertically(animationSpec = tween(durationMillis = 180)),
            exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                shrinkVertically(animationSpec = tween(durationMillis = 160)),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SectionDivider()
                SettingsTextInputRow(
                    title = "自定义前景提示词",
                    value = tuningState.gptCustomPrompt,
                    label = "自定义前景提示词",
                    inputHint = "请填写自定义前景提示词",
                    icon = SettingsIconKind.Prompt,
                    enabled = !isBusy,
                    onValueChange = onGptCustomPromptChange,
                )
            }
        }
        SectionDivider()
        SettingsTextInputRow(
            title = "模型 ID",
            value = gptModelId,
            label = "模型 ID",
            inputHint = "请填写模型 ID",
            icon = SettingsIconKind.Layers,
            enabled = !isBusy,
            onValueChange = onGptModelIdChange,
        )
        SectionDivider()
        SettingsTextInputRow(
            title = "Base URL",
            value = gptBaseUrl,
            label = "Base URL",
            inputHint = "请填写 Base URL",
            icon = SettingsIconKind.Link,
            enabled = !isBusy,
            onValueChange = onGptBaseUrlChange,
        )
        SectionDivider()
        SettingsTextInputRow(
            title = "API key",
            value = gptApiKey,
            label = "API key",
            inputHint = "请填写 API key",
            icon = SettingsIconKind.Key,
            obscure = true,
            enabled = !isBusy,
            onValueChange = onGptApiKeyChange,
        )
        SectionDivider()
        SettingsInfoRow(
            title = "累计调用",
            summary = "已累计调用 AI 云端接口的次数",
            value = "$gptRunCount 次",
            icon = settingsIconForTitle("累计调用"),
        )
    }
}

@Composable
internal fun RmbgComponentCard(
    component: RmbgComponent?,
    rmbgRunCount: Int,
    currentPreset: RmbgModelPreset,
    allPresets: List<RmbgModelPreset>,
    lastError: String?,
    componentUrl: String,
    isBusy: Boolean,
    isGenerating: Boolean,
    isInstalling: Boolean,
    installStage: String,
    installProgress: Float?,
    dialogVisible: Boolean,
    onPresetSelected: (RmbgModelPreset) -> Unit,
    onComponentUrlChange: (String) -> Unit,
    onDialogVisibleChange: (Boolean) -> Unit,
    onPickZip: () -> Unit,
    onInstallFromUrl: () -> Unit,
    onClearInstalled: () -> Unit,
) {
    SectionCard(rowsFullBleed = true, title = "RMBG 组件") {
        SettingsInfoRow(
            title = "RMBG 状态",
            summary = component?.let { "ABI ${it.abi}" } ?: "未安装",
            value = if (component == null) "未安装" else "已安装",
            icon = settingsIconForTitle("RMBG 状态"),
        )
        SectionDivider()
        SettingsInfoRow(
            title = "累计调用",
            summary = "已累计运行 RMBG 模型抠图的次数",
            value = "$rmbgRunCount 次",
            icon = settingsIconForTitle("累计调用"),
        )
        SectionDivider()
        LibraryChoiceRow(
            title = "模型版本",
            summary = currentPreset.summary,
            value = currentPreset.label,
            icon = SettingsIconKind.Layers,
            enabled = !isBusy && !isGenerating && !isInstalling,
            summaryBelow = true,
            entry = remember(currentPreset, allPresets) {
                val preset = currentPreset
                DropdownEntry(
                    items = allPresets.map { candidate ->
                        DropdownItem(
                            text = candidate.label,
                            summary = candidate.summary,
                            selected = candidate == preset,
                            onClick = { onPresetSelected(candidate) },
                        )
                    },
                )
            },
        )
        if (lastError != null) {
            Text(
                text = lastError.orEmpty(),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = COUITheme.textStyles.footnote1,
                color = COUITheme.colorScheme.error,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
        SectionDivider()
        LibrarySettingRow(
            title = "模型或组件 ZIP 地址",
            summary = if (componentUrl.isBlank()) "粘贴 ZIP 地址或从本地选择 · 未设置" else componentUrl,
            icon = SettingsIconKind.Link,
            showValue = false,
            showArrowRight = true,
            enabled = !isBusy && !isGenerating && !isInstalling,
            onClick = { onDialogVisibleChange(true) },
        )
        if (isInstalling || installStage.isNotBlank()) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                RmbgInstallProgressBar(
                    text = installStage.ifBlank { if (isInstalling) "安装中" else "" },
                    progress = installProgress,
                    active = isInstalling,
                )
            }
        }
    }
    if (dialogVisible) {
        val actionEnabled = !isBusy && !isGenerating && !isInstalling
        ArtDialog(
            title = "模型或组件 ZIP 地址",
            summary = "粘贴 ZIP 地址，或从本地选择模型文件",
            onDismissRequest = { onDialogVisibleChange(false) },
            negative = DialogButtonBarAction(
                text = "选择 ZIP",
                enabled = actionEnabled,
                onClick = {
                    onDialogVisibleChange(false)
                    onPickZip()
                },
            ),
            positive = DialogButtonBarAction(
                text = if (isInstalling) "安装中" else "一键安装",
                enabled = actionEnabled,
                recommended = true,
                onClick = {
                    onDialogVisibleChange(false)
                    onInstallFromUrl()
                },
            ),
            neutral = if (component != null) {
                DialogButtonBarAction(
                    text = "清除已安装 RMBG",
                    enabled = actionEnabled,
                    onClick = {
                        onDialogVisibleChange(false)
                        onClearInstalled()
                    },
                )
            } else {
                null
            },
        ) {
            TextField(
                value = componentUrl,
                onValueChange = onComponentUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = "请填写 ZIP 地址",
                showClearButton = true,
                singleLine = true,
            )
        }
    }
}

@Composable
internal fun OutputCard(
    autoConfirmRootWrite: Boolean,
    autoConfirmRefresh: Boolean,
    isBusy: Boolean,
    outputTreeUri: Uri?,
    treeDisplay: String?,
    backupActive: Boolean,
    backupInBackground: Boolean,
    backupDots: Int,
    exportDialogVisible: Boolean,
    onAutoConfirmRootWriteChange: (Boolean) -> Unit,
    onAutoConfirmRefreshChange: (Boolean) -> Unit,
    onBackupRowClick: () -> Unit,
    onBackupBackgroundActiveChanged: (Boolean) -> Unit,
    onExportDialogDismiss: () -> Unit,
    onChooseTree: () -> Unit,
    onBackupAll: () -> Unit,
) {
    SectionCard(rowsFullBleed = true, title = "输出") {
        SettingsInfoRow(
            title = "Root 目标",
            summary = "/data/oplus/uxicons/{package}",
            value = null,
            icon = settingsIconForTitle("Root 目标"),
            summarySingleLine = true,
        )
        SectionDivider()
        LibrarySettingRow(
            title = "自动确认写入",
            summary = if (autoConfirmRootWrite) "点击写入时直接写入 Root 目标" else "点击写入时会弹出二次确认提示",
            icon = SettingsIconKind.Shield,
            showSwitch = true,
            checked = autoConfirmRootWrite,
            enabled = !isBusy,
            onCheckedChange = onAutoConfirmRootWriteChange,
        )
        SectionDivider()
        LibrarySettingRow(
            title = "自动确认刷新",
            summary = if (autoConfirmRefresh) "点击刷新按钮时直接执行刷新" else "点击刷新按钮时会弹出二次确认提示",
            icon = SettingsIconKind.Refresh,
            showSwitch = true,
            checked = autoConfirmRefresh,
            enabled = !isBusy,
            onCheckedChange = onAutoConfirmRefreshChange,
        )
        SectionDivider()
        val outputTreeDisplay = remember(outputTreeUri) { treeDisplay }
        val isBackupActive = backupActive
        val isBackupInBg = backupInBackground && isBackupActive
        // 后台时“备份中”省略号动效：. -> .. -> ...
        val backupDotsText = remember(backupDots) { ".".repeat(backupDots.coerceIn(1, 3)) }
        LaunchedEffect(isBackupInBg) {
            onBackupBackgroundActiveChanged(isBackupInBg)
        }
        LibrarySettingRow(
            title = "备份到外部目录",
            summary = when {
                isBackupInBg -> "备份中$backupDotsText"
                isBackupActive -> "正在备份..."
                outputTreeUri == null -> "未选择 · 备份已写入系统的全部图标"
                outputTreeDisplay != null -> "已选择：$outputTreeDisplay"
                else -> "已选择：${outputTreeUri.toString().take(40)}"
            },
            icon = settingsIconForTitle("备份到外部目录"),
            showValue = false,
            showArrowRight = true,
            enabled = !isBusy || isBackupInBg,
            onClick = onBackupRowClick,
        )
    }
    if (exportDialogVisible) {
        val dialogTreeDisplay = remember(outputTreeUri) { treeDisplay }
        var draftExportPath by remember(outputTreeUri) {
            mutableStateOf(dialogTreeDisplay ?: outputTreeUri?.toString() ?: "")
        }
        // 保持与 treeUri 同步：当外部选择目录后，刷新输入框
        LaunchedEffect(dialogTreeDisplay, outputTreeUri) {
            val current = dialogTreeDisplay ?: outputTreeUri?.toString() ?: ""
            if (current != draftExportPath && (draftExportPath.isBlank() || outputTreeUri != null)) {
                // 仅在空输入或已选状态下自动同步，避免覆盖用户正在输入的内容
                if (draftExportPath.isBlank() || dialogTreeDisplay != null) {
                    draftExportPath = current
                }
            }
        }
        ArtDialog(
            title = "备份到外部目录",
            summary = if (outputTreeUri == null) "将把 /data/oplus/uxicons 内的全部图标（含官方与已写入）备份到你选择的目录" else "将把已写入系统的全部图标备份到你选择的目录",
            onDismissRequest = onExportDialogDismiss,
            negative = DialogButtonBarAction(
                text = "选择目录",
                enabled = !isBusy,
                onClick = {
                    onExportDialogDismiss()
                    onChooseTree()
                },
            ),
            positive = DialogButtonBarAction(
                text = "备份当前所有图标",
                enabled = !isBusy,
                recommended = true,
                onClick = {
                    onExportDialogDismiss()
                    onBackupAll()
                },
            ),
        ) {
            TextField(
                value = draftExportPath,
                onValueChange = { draftExportPath = it },
                modifier = Modifier.fillMaxWidth(),
                label = "备份路径",
                showClearButton = true,
                singleLine = false,
                maxLines = 4,
            )
        }
    }
}

@Composable
internal fun PreviewStripSettingsCard(
    enabled: Boolean,
    isBusy: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SectionCard(rowsFullBleed = true, title = "预览条") {
        LibrarySettingRow(
            title = "顶部 1×4 预览条",
            summary = "在主页、生成参数与预设页置顶显示，参数或 JSON 保存后自动更新",
            icon = SettingsIconKind.Palette,
            showSwitch = true,
            checked = enabled,
            enabled = !isBusy,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
internal fun InputSettingsCard(
    launcherCount: Int,
    totalCount: Int,
    generatedCount: Int,
    amCount: Int,
    restoreEnabled: Boolean,
    onRestoreClick: () -> Unit,
) {
    SectionCard(rowsFullBleed = true, title = "应用范围") {
        SettingsInfoRow(
            title = "应用范围",
            summary = "启动器 $launcherCount 个 / 全部 $totalCount 个",
            value = "启动器",
            icon = settingsIconForTitle("应用范围"),
        )
        SectionDivider()
        SettingsInfoRow(
            title = "已生成",
            summary = null,
            value = "$generatedCount",
            icon = settingsIconForTitle("已生成"),
        )
        SectionDivider()
        SettingsInfoRow(
            title = "AM 标记",
            summary = null,
            value = "$amCount",
            icon = SettingsIconKind.CheckBadge,
        )
        SectionDivider()
        LibrarySettingRow(
            title = "一键还原 AM 图标",
            summary = if (amCount > 0) {
                "把 $amCount 个带 AM 标记的目录恢复成原图（没有备份的清空 ArtPlus 写入的图标），并删除标记"
            } else {
                "没有可还原的图标包"
            },
            icon = SettingsIconKind.Refresh,
            showValue = false,
            showArrowRight = true,
            enabled = restoreEnabled,
            onClick = onRestoreClick,
        )
    }
}
