package dev.artplus.mobile.ui.pages.presets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.ButtonDefaults
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.CardDefaults
import io.github.suqi8.coui.kmp.basic.HorizontalDivider
import io.github.suqi8.coui.kmp.basic.TextButton
import io.github.suqi8.coui.kmp.layout.DialogButtonBarAction
import io.github.suqi8.coui.kmp.overlay.OverlayBottomSheet
import io.github.suqi8.coui.kmp.theme.COUITheme
import dev.artplus.mobile.TuningPreset
import dev.artplus.mobile.ui.cards.BatchPreviewProgress
import dev.artplus.mobile.ui.cards.PresetImportDialog
import dev.artplus.mobile.ui.cards.PresetMenuItem
import dev.artplus.mobile.ui.cards.PresetNameDialog
import dev.artplus.mobile.ui.components.ArtAlertDialog
import dev.artplus.mobile.ui.components.ArtProgressDialog

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
    // 选项列表用 ColorOS 底部面板承载；点击外部 / 返回键 / 下拉都等价于「取消」
    OverlayBottomSheet(
        show = true,
        title = "预设选项：${target.name}",
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(vertical = 8.dp),
                colors = CardDefaults.defaultColors(color = COUITheme.colorScheme.secondaryVariant),
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
                PresetMenuDivider()
                PresetMenuItem(
                    enabled = !isBusy,
                    title = "批量四风格预览",
                    summary = "随机抓取应用，四种风格宫格预览",
                    onClick = {
                        onDismiss()
                        onPreview(target)
                    },
                )
                PresetMenuDivider()
                PresetMenuItem(
                    enabled = !isBusy,
                    title = "覆盖为此预设",
                    summary = "将当前所有调参保存覆盖到「${target.name}」",
                    onClick = {
                        onDismiss()
                        onOverwrite(target)
                    },
                )
                PresetMenuDivider()
                PresetMenuItem(
                    enabled = !isBusy,
                    title = "重命名",
                    summary = "修改该预设名称",
                    onClick = {
                        onDismiss()
                        onRename(target)
                    },
                )
                PresetMenuDivider()
                PresetMenuItem(
                    enabled = !isBusy,
                    title = "复制单条 JSON",
                    summary = "导出该预设快照到剪贴板，方便分享",
                    onClick = {
                        onDismiss()
                        onExportSingle(target)
                    },
                )
                PresetMenuDivider()
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
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                text = "取消",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(),
            )
        }
    }
}

@Composable
private fun PresetMenuDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
internal fun PresetDeleteConfirmDialog(
    target: TuningPreset,
    onDismiss: () -> Unit,
    onConfirmDelete: (String) -> Unit,
) {
    ArtAlertDialog(
        title = "删除预设",
        summary = "确定要删除预设「${target.name}」吗？此操作不可撤销。",
        confirmText = "确认删除",
        cancelText = "取消",
        onConfirm = {
            onDismiss()
            onConfirmDelete(target.id)
        },
        onCancel = onDismiss,
    )
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
    ArtAlertDialog(
        title = "生成批量预览",
        summary = "将根据预设「${preset.name}」随机抽取 ${batchPreviewCount} 个应用（优先未生成图标），批量生成正常亮色、正常暗色、单色亮色与单色暗色共 ${batchPreviewCount * 4} 个图标供宫格预览。\n\n此过程仅在内存中生成预览，绝不写入分区或更改任何文件。确认开始？",
        confirmText = "开始生成",
        cancelText = "取消",
        onConfirm = onConfirm,
        onCancel = onDismiss,
    )
}

@Composable
internal fun PresetBatchPreviewProgressDialog(
    progress: BatchPreviewProgress?,
    onCancel: () -> Unit,
) {
    val current = progress ?: return
    val fraction = if (current.total <= 0) 0f else (current.completed.toFloat() / current.total.toFloat()).coerceIn(0f, 1f)
    // 点击外部 / 返回键 = 取消（与原行为一致）
    ArtProgressDialog(
        title = "正在生成批量预览",
        message = "预设「${current.presetName}」· 进度 ${current.completed}/${current.total}",
        detail = current.currentLabel,
        progress = fraction,
        onDismissRequest = { onCancel() },
        negative = DialogButtonBarAction(text = "取消", onClick = { onCancel() }),
    )
}

@Composable
internal fun BatchPreviewRefreshConfirmDialog(
    preset: TuningPreset,
    batchPreviewCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ArtAlertDialog(
        title = "重新生成批量预览",
        summary = "是否重新随机抽取 ${batchPreviewCount} 个应用，重新生成预设「${preset.name}」的四风格预览？\n\n此操作将重新渲染并覆盖现有的快照数据。",
        confirmText = "确认重新生成",
        cancelText = "取消",
        onConfirm = onConfirm,
        onCancel = onDismiss,
    )
}
