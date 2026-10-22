package dev.artplus.mobile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.suqi8.coui.kmp.basic.ButtonDefaults
import io.github.suqi8.coui.kmp.basic.TextField
import io.github.suqi8.coui.kmp.basic.TextFieldMode
import io.github.suqi8.coui.kmp.layout.DialogButtonBarAction
import io.github.suqi8.coui.kmp.theme.COUITheme

@Composable
internal fun BatchApplyProgressDialog(progress: BatchApplyProgress) {
    val fraction = if (progress.total <= 0) {
        0f
    } else {
        (progress.completed.toFloat() / progress.total.toFloat()).coerceIn(0f, 1f)
    }
    // 批量应用期间不可被外部点击 / 返回键关闭（onDismissRequest 缺省为 null）
    ArtProgressDialog(
        title = progress.title,
        message = progress.currentLabel,
        progress = fraction,
        footer = buildString {
            append("${progress.completed}/${progress.total}")
            if (progress.failures > 0) {
                append(" · 失败 ${progress.failures}")
            }
        },
    )
}

@Composable
internal fun BackupProgressBottomSheet(
    progress: ExportProgress,
    onStop: () -> Unit,
    onBackground: () -> Unit,
) {
    val fraction = if (progress.total <= 0) 0f else (progress.completed.toFloat() / progress.total.toFloat()).coerceIn(0f, 1f)
    // 第一行：正在备份 + 进度分数/百分比
    val line1 = when {
        progress.isIndeterminate -> "正在备份"
        progress.title.contains("导出") -> "正在导出 ${progress.completed}/${progress.total}"
        else -> "正在备份 ${progress.completed}/${progress.total}"
    }
    // 第二行：包名，单行显示不换行
    val pkgName = remember(progress.currentLabel) {
        val raw = progress.currentLabel
        when {
            ":" in raw -> raw.substringAfterLast(":").trim()
            "：" in raw -> raw.substringAfterLast("：").trim()
            raw.contains("/") && raw.contains(" ") -> raw.substringAfterLast(" ").trim().takeIf { it.contains(".") } ?: ""
            else -> ""
        }.takeIf { it.isNotBlank() && it != raw.trim() } ?: ""
    }
    // 点击外部 / 返回键 = 后台备份（与原行为一致）；「停止备份」用 error 色文字，「后台备份」为推荐按钮
    ArtProgressDialog(
        title = progress.title,
        message = line1,
        detail = pkgName,
        progress = if (progress.isIndeterminate) null else fraction,
        onDismissRequest = onBackground,
        negative = DialogButtonBarAction(text = "停止备份", onClick = onStop),
        positive = DialogButtonBarAction(text = "后台备份", onClick = onBackground, recommended = true),
        buttonColors = ButtonDefaults.textButtonColorsBorderless(
            textColor = COUITheme.colorScheme.error,
            disabledTextColor = COUITheme.colorScheme.error.copy(alpha = 0.4f),
        ),
    )
}

@Composable
internal fun PresetNameDialog(
    title: String,
    initialName: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    ArtDialog(
        title = title,
        onDismissRequest = onDismiss,
        negative = DialogButtonBarAction(text = "取消", onClick = onDismiss),
        positive = DialogButtonBarAction(
            text = confirmLabel,
            enabled = name.trim().isNotEmpty(),
            onClick = { onConfirm(name.trim()) },
        ),
    ) {
        InlineInputField(
            value = name,
            onValueChange = { name = it },
            label = "预设名称",
        )
    }
}

@Composable
internal fun PresetImportDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    ArtDialog(
        title = "导入预设 JSON",
        onDismissRequest = onDismiss,
        negative = DialogButtonBarAction(text = "取消", onClick = onDismiss),
        positive = DialogButtonBarAction(
            text = "导入",
            enabled = text.trim().isNotEmpty(),
            onClick = { onConfirm(text) },
        ),
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            backgroundMode = TextFieldMode.Rectangle,
            label = "粘贴导出的预设 JSON...",
            minLines = 5,
            maxLines = 8,
        )
    }
}

@Composable
internal fun ServiceConfirmDialog(
    request: ServiceConfirmRequest,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ArtAlertDialog(
        title = request.title,
        summary = request.message,
        confirmText = request.confirmLabel,
        cancelText = "取消",
        onConfirm = onConfirm,
        onCancel = onDismiss,
    )
}
