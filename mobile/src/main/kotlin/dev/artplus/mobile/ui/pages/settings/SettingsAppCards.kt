package dev.artplus.mobile

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
    SectionCard(rowsFullBleed = true) {
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
    SectionCard(rowsFullBleed = true) {
        NumberParameterControl(
            busy = isBusy,
            title = "批量预览数量",
            summary = "预设四风格宫格预览时随机抓取的应用数量（默认 20，优先未生成图标应用）",
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
    SectionCard(rowsFullBleed = true) {
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
        Spacer(modifier = Modifier.height(4.dp))
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
                Spacer(modifier = Modifier.height(6.dp))
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
        Spacer(modifier = Modifier.height(6.dp))
        SettingsTextInputRow(
            title = "模型 ID",
            value = gptModelId,
            label = "模型 ID",
            inputHint = "请填写模型 ID",
            icon = SettingsIconKind.Layers,
            enabled = !isBusy,
            onValueChange = onGptModelIdChange,
        )
        Spacer(modifier = Modifier.height(6.dp))
        SettingsTextInputRow(
            title = "Base URL",
            value = gptBaseUrl,
            label = "Base URL",
            inputHint = "请填写 Base URL",
            icon = SettingsIconKind.Link,
            enabled = !isBusy,
            onValueChange = onGptBaseUrlChange,
        )
        Spacer(modifier = Modifier.height(6.dp))
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
        Spacer(modifier = Modifier.height(6.dp))
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
    SectionCard(rowsFullBleed = true) {
        SettingsInfoRow(
            title = "RMBG 状态",
            summary = component?.let { "ABI ${it.abi}" } ?: "未安装",
            value = if (component == null) "未安装" else "已安装",
            icon = settingsIconForTitle("RMBG 状态"),
        )
        Spacer(modifier = Modifier.height(6.dp))
        SettingsInfoRow(
            title = "累计调用",
            summary = "已累计运行 RMBG 模型抠图的次数",
            value = "$rmbgRunCount 次",
            icon = settingsIconForTitle("累计调用"),
        )
        Spacer(modifier = Modifier.height(4.dp))
        LibraryChoiceRow(
            title = "模型版本",
            summary = currentPreset.summary,
            value = currentPreset.label,
            icon = SettingsIconKind.Layers,
            enabled = !isBusy && !isGenerating && !isInstalling,
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lastError.orEmpty(),
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.error,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
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
            Spacer(modifier = Modifier.height(10.dp))
            RmbgInstallProgressBar(
                text = installStage.ifBlank { if (isInstalling) "安装中" else "" },
                progress = installProgress,
                active = isInstalling,
            )
        }
    }
    if (dialogVisible) {
        MiuixBottomDialog(onDismissRequest = { onDialogVisibleChange(false) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MiuixTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "模型或组件 ZIP 地址",
                    style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                    color = MiuixTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "粘贴 ZIP 地址，或从本地选择模型文件",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                TextField(
                    value = componentUrl,
                    onValueChange = onComponentUrlChange,
                    label = "请填写 ZIP 地址",
                    singleLine = true,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = {
                            onDialogVisibleChange(false)
                            onPickZip()
                        },
                        enabled = !isBusy && !isGenerating && !isInstalling,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(),
                    ) {
                        Text(
                            text = "选择 ZIP",
                            style = MiuixTheme.textStyles.button,
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                    }
                    Button(
                        onClick = {
                            onDialogVisibleChange(false)
                            onInstallFromUrl()
                        },
                        enabled = !isBusy && !isGenerating && !isInstalling,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary(),
                    ) {
                        Text(
                            text = if (isInstalling) "安装中" else "一键安装",
                            style = MiuixTheme.textStyles.button,
                            color = Color.White,
                            maxLines = 1,
                        )
                    }
                }
                if (component != null) {
                    Button(
                        onClick = {
                            onDialogVisibleChange(false)
                            onClearInstalled()
                        },
                        enabled = !isBusy && !isGenerating && !isInstalling,
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MiuixTheme.colorScheme.onSurfaceVariantActions,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "清除已安装 RMBG",
                            style = MiuixTheme.textStyles.button,
                            maxLines = 1,
                        )
                    }
                }
            }
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
    SectionCard(rowsFullBleed = true) {
        SettingsInfoRow(
            title = "Root 目标",
            summary = "/data/oplus/uxicons/{package}",
            value = "data",
            icon = settingsIconForTitle("Root 目标"),
        )
        Spacer(modifier = Modifier.height(4.dp))
        LibrarySettingRow(
            title = "自动确认写入",
            summary = if (autoConfirmRootWrite) "点击写入时直接写入 Root 目标" else "点击写入时会弹出二次确认提示",
            icon = SettingsIconKind.Shield,
            showSwitch = true,
            checked = autoConfirmRootWrite,
            enabled = !isBusy,
            onCheckedChange = onAutoConfirmRootWriteChange,
        )
        Spacer(modifier = Modifier.height(4.dp))
        LibrarySettingRow(
            title = "自动确认刷新",
            summary = if (autoConfirmRefresh) "点击刷新按钮时直接执行刷新" else "点击刷新按钮时会弹出二次确认提示",
            icon = SettingsIconKind.Refresh,
            showSwitch = true,
            checked = autoConfirmRefresh,
            enabled = !isBusy,
            onCheckedChange = onAutoConfirmRefreshChange,
        )
        Spacer(modifier = Modifier.height(4.dp))
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
        MiuixBottomDialog(onDismissRequest = onExportDialogDismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MiuixTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "备份到外部目录",
                    style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                    color = MiuixTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (outputTreeUri == null) "将把 /data/oplus/uxicons 内的全部图标（含官方与已写入）备份到你选择的目录" else "将把已写入系统的全部图标备份到你选择的目录",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                TextField(
                    value = draftExportPath,
                    onValueChange = { draftExportPath = it },
                    label = "备份路径",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = {
                            onExportDialogDismiss()
                            onChooseTree()
                        },
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(),
                    ) {
                        Text(
                            text = "选择目录",
                            style = MiuixTheme.textStyles.button,
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                    }
                    Button(
                        onClick = {
                            onExportDialogDismiss()
                            onBackupAll()
                        },
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary(),
                    ) {
                        Text(
                            text = "备份当前所有图标",
                            style = MiuixTheme.textStyles.button,
                            color = Color.White,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun PreviewStripSettingsCard(
    enabled: Boolean,
    isBusy: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SectionCard(rowsFullBleed = true) {
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
internal fun InputSettingsCard(launcherCount: Int, totalCount: Int, generatedCount: Int) {
    SectionCard(rowsFullBleed = true) {
        SettingsInfoRow(
            title = "应用范围",
            summary = "启动器 $launcherCount 个 / 全部 $totalCount 个",
            value = "启动器",
            icon = settingsIconForTitle("应用范围"),
        )
        Spacer(modifier = Modifier.height(4.dp))
        SettingsInfoRow(
            title = "已生成",
            summary = "来自本地缓存；手动刷新后才重新读取 data 路径",
            value = "$generatedCount",
            icon = settingsIconForTitle("已生成"),
        )
    }
}

@Composable
internal fun ShowSystemAppsToggleRow(
    checked: Boolean,
    isBusy: Boolean,
    onToggle: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val bleedPx = with(LocalDensity.current) { CHOICE_ROW_HORIZONTAL_BLEED_DP.dp.roundToPx() }
    val bridge = LocalSectionCardPressBridge.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .trackSectionPress(bridge, pressed)
            .cardRowBleed(bleedPx)
            .background(cardRowPressedColor(pressed))
            .semantics {
                contentDescription = "显示系统应用开关"
                stateDescription = if (checked) "已开启" else "已关闭"
                role = Role.Switch
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isBusy,
                onClick = onToggle,
            )
            .padding(horizontal = CHOICE_ROW_HORIZONTAL_BLEED_DP.dp)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsLineIcon(kind = SettingsIconKind.Shield)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = "显示系统应用",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (checked) "已包含系统应用，可搜索和批量选择" else "仅显示用户应用；系统应用已隐藏",
                modifier = Modifier.basicMarquee(),
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                softWrap = false,
            )
        }
        Box(
            modifier = Modifier.semantics {
                contentDescription = "显示系统应用"
            },
        ) {
            LiquidGlassSwitch(checked = checked, enabled = !isBusy)
        }
    }
}
