package dev.artplus.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Slice 2.1：预设页弹窗 UI（原 MainActivity 预设弹窗 6 件套原样搬迁）。
 * Composable 只收 state + onEvent；Activity 状态经参数/回调注入，行为与 UI 100% 等价。
 */
@Composable
internal fun PresetActionMenuDialog(
    target: TuningPreset,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onApply: (TuningPreset) -> Unit,
    onPreview: (TuningPreset) -> Unit,
    onOverwrite: (TuningPreset) -> Unit,
    onRename: (TuningPreset) -> Unit,
    onExportSingle: (TuningPreset) -> Unit,
    onDelete: (TuningPreset) -> Unit,
) {
    MiuixBottomDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MiuixTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "预设选项：${target.name}",
                style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                color = MiuixTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                    .padding(vertical = 8.dp),
            ) {
                PresetMenuItem(
                    enabled = !isBusy,
                    title = "应用此预设",
                    summary = "载入此快照的全部调参设置",
                    onClick = {
                        onDismiss()
                        onApply(target)
                    },
                )
                PresetMenuItem(
                    enabled = !isBusy,
                    title = "批量四风格预览",
                    summary = "随机抓取应用，四种风格宫格预览",
                    onClick = {
                        onDismiss()
                        onPreview(target)
                    },
                )
                PresetMenuItem(
                    enabled = !isBusy,
                    title = "覆盖为此预设",
                    summary = "将当前所有调参保存覆盖到「${target.name}」",
                    onClick = {
                        onDismiss()
                        onOverwrite(target)
                    },
                )
                PresetMenuItem(
                    enabled = !isBusy,
                    title = "重命名",
                    summary = "修改该预设名称",
                    onClick = {
                        onDismiss()
                        onRename(target)
                    },
                )
                PresetMenuItem(
                    enabled = !isBusy,
                    title = "复制单条 JSON",
                    summary = "导出该预设快照到剪贴板，方便分享",
                    onClick = {
                        onDismiss()
                        onExportSingle(target)
                    },
                )
                PresetMenuItem(
                    enabled = !isBusy,
                    title = "删除预设",
                    summary = "从预设库中彻底移除",
                    onClick = {
                        onDismiss()
                        onDelete(target)
                    },
                )
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(),
            ) {
                Text(
                    text = "取消",
                    style = MiuixTheme.textStyles.button,
                    color = MiuixTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
internal fun PresetDeleteConfirmDialog(
    target: TuningPreset,
    onDismiss: () -> Unit,
    onConfirmDelete: (String) -> Unit,
) {
    MiuixBottomDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MiuixTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "删除预设",
                style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                color = MiuixTheme.colorScheme.onSurface,
            )
            Text(
                text = "确定要删除预设「${target.name}」吗？此操作不可撤销。",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(),
                ) {
                    Text(
                        text = "取消",
                        style = MiuixTheme.textStyles.button,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                }
                Button(
                    onClick = {
                        onDismiss()
                        onConfirmDelete(target.id)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(
                        text = "确认删除",
                        style = MiuixTheme.textStyles.button,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
internal fun PresetPageDialogs(
    saveDialogVisible: Boolean,
    saveInitialName: String,
    onSaveConfirm: (String) -> Unit,
    onSaveDismiss: () -> Unit,
    renameTarget: TuningPreset?,
    onRenameConfirm: (String, String) -> Unit,
    onRenameDismiss: () -> Unit,
    actionMenuTarget: TuningPreset?,
    actionMenuBusy: Boolean,
    onActionMenuDismiss: () -> Unit,
    onActionApply: (TuningPreset) -> Unit,
    onActionPreview: (TuningPreset) -> Unit,
    onActionOverwrite: (TuningPreset) -> Unit,
    onActionRename: (TuningPreset) -> Unit,
    onActionExportSingle: (TuningPreset) -> Unit,
    onActionDelete: (TuningPreset) -> Unit,
    deleteConfirmTarget: TuningPreset?,
    onDeleteDismiss: () -> Unit,
    onDeleteConfirm: (String) -> Unit,
    importDialogVisible: Boolean,
    onImportConfirm: (String) -> Unit,
    onImportDismiss: () -> Unit,
    batchPreviewConfirmTarget: TuningPreset?,
    onBatchPreviewConfirm: (TuningPreset) -> Unit,
    onBatchPreviewConfirmDismiss: () -> Unit,
    batchPreviewProgress: BatchPreviewProgress?,
    onCancelBatchPreview: () -> Unit,
    showRefreshConfirm: Boolean,
    refreshConfirmPreset: TuningPreset?,
    batchPreviewCount: Int,
    onRefreshConfirm: (TuningPreset) -> Unit,
    onRefreshDismiss: () -> Unit,
) {
    if (saveDialogVisible) {
        PresetNameDialog(
            title = "保存当前为预设",
            initialName = saveInitialName,
            confirmLabel = "保存",
            onConfirm = { name ->
                onSaveConfirm(name)
            },
            onDismiss = { onSaveDismiss() },
        )
    }
    renameTarget?.let { target ->
        PresetNameDialog(
            title = "重命名预设",
            initialName = target.name,
            confirmLabel = "重命名",
            onConfirm = { name ->
                onRenameConfirm(target.id, name)
            },
            onDismiss = { onRenameDismiss() },
        )
    }
    actionMenuTarget?.let { target ->
        PresetActionMenuDialog(
            target = target,
            isBusy = actionMenuBusy,
            onDismiss = { onActionMenuDismiss() },
            onApply = onActionApply,
            onPreview = onActionPreview,
            onOverwrite = onActionOverwrite,
            onRename = onActionRename,
            onExportSingle = onActionExportSingle,
            onDelete = onActionDelete,
        )
    }
    deleteConfirmTarget?.let { target ->
        PresetDeleteConfirmDialog(
            target = target,
            onDismiss = { onDeleteDismiss() },
            onConfirmDelete = onDeleteConfirm,
        )
    }
    if (importDialogVisible) {
        PresetImportDialog(
            onConfirm = { text -> onImportConfirm(text) },
            onDismiss = { onImportDismiss() },
        )
    }
    batchPreviewConfirmTarget?.let { target ->
        PresetBatchPreviewConfirmDialog(
            preset = target,
            batchPreviewCount = batchPreviewCount,
            onConfirm = {
                onBatchPreviewConfirm(target)
            },
            onDismiss = { onBatchPreviewConfirmDismiss() },
        )
    }
    PresetBatchPreviewProgressDialog(
        progress = batchPreviewProgress,
        onCancel = onCancelBatchPreview,
    )
    if (showRefreshConfirm) {
        val targetPreset = refreshConfirmPreset
        if (targetPreset != null) {
            BatchPreviewRefreshConfirmDialog(
                preset = targetPreset,
                batchPreviewCount = batchPreviewCount,
                onConfirm = {
                    onRefreshConfirm(targetPreset)
                },
                onDismiss = { onRefreshDismiss() },
            )
        }
    }
}

@Composable
internal fun PresetBatchPreviewConfirmDialog(
    preset: TuningPreset,
    batchPreviewCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    MiuixBottomDialog(onDismissRequest = onDismiss) {
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
                text = "生成批量预览",
                style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                color = MiuixTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "将根据预设「${preset.name}」随机抽取 ${batchPreviewCount} 个应用（优先未生成图标），批量生成正常亮色、正常暗色、单色亮色与单色暗色共 ${batchPreviewCount * 4} 个图标供宫格预览。\n\n此过程仅在内存中生成预览，绝不写入分区或更改任何文件。确认开始？",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
                maxLines = 8,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(),
                ) {
                    Text(
                        text = "取消",
                        style = MiuixTheme.textStyles.button,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(
                        text = "开始生成",
                        style = MiuixTheme.textStyles.button,
                        color = Color.White,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
internal fun PresetBatchPreviewProgressDialog(
    progress: BatchPreviewProgress?,
    onCancel: () -> Unit,
) {
    val current = progress ?: return
    val fraction = if (current.total <= 0) 0f else (current.completed.toFloat() / current.total.toFloat()).coerceIn(0f, 1f)
    MiuixBottomDialog(onDismissRequest = { onCancel() }) {
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
                text = "正在生成批量预览",
                style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                color = MiuixTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "预设「${current.presetName}」· 进度 ${current.completed}/${current.total}",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = current.currentLabel,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            LinearProgressIndicator(
                progress = fraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
            )
            Button(
                onClick = { onCancel() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(),
            ) {
                Text(
                    text = "取消",
                    style = MiuixTheme.textStyles.button,
                    color = MiuixTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
internal fun BatchPreviewRefreshConfirmDialog(
    preset: TuningPreset,
    batchPreviewCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    MiuixBottomDialog(onDismissRequest = onDismiss) {
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
                text = "重新生成批量预览",
                style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                color = MiuixTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "是否重新随机抽取 ${batchPreviewCount} 个应用，重新生成预设「${preset.name}」的四风格预览？\n\n此操作将重新渲染并覆盖现有的快照数据。",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
                maxLines = 8,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(),
                ) {
                    Text(
                        text = "取消",
                        style = MiuixTheme.textStyles.button,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(
                        text = "确认重新生成",
                        style = MiuixTheme.textStyles.button,
                        color = Color.White,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
