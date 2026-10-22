package dev.artplus.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.LinearProgressIndicator
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TextButtonColors
import io.github.suqi8.coui.kmp.layout.DialogButtonBar
import io.github.suqi8.coui.kmp.layout.DialogButtonBarAction
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.window.WindowDialog

/** COUI WindowDialog 不给 content 加任何内边距，标题/摘要的水平内边距为 24dp，这里保持一致。 */
private val ArtDialogContentHorizontalPadding = 24.dp

/**
 * 应用统一的 ColorOS 17 对话框（封装 COUI [WindowDialog] + [DialogButtonBar]）。
 *
 * 应用没有根 Scaffold / COUIPopupHost，所以只能用 Window* 变体。
 * - [content] 由本函数补 24dp 水平内边距（COUI 的 content 槽本身不带内边距），
 *   无按钮时再补 24dp 底部内边距；按钮栏贯穿整个面板宽度，不吃内边距。
 * - 按钮用 [negative] / [positive] / [neutral] 传入，不要把 [DialogButtonBar] 塞进 [content]
 *   （会被内边距挤窄）。[DialogButtonBarAction.enabled] 对应原来按钮的可用状态，
 *   [DialogButtonBarAction.recommended] 为实心主色按钮（每个栏最多一个）。
 *   3 个按钮时 COUI 自动竖排。
 * - [onDismissRequest]：点击外部 / 返回键触发；传 null 表示不可关闭（返回键与外部点击无效）。
 * - [show] 默认 true，调用方可继续用 `if (visible) ArtDialog(...)`（此时无退出动画）；
 *   需要退出动画时把 show 绑到状态并在 [onDismissFinished] 里清理。
 * - [centered] 为 true 时居中弹出（缩放 + 淡入，即 ColorOS 居中警示框样式），否则自底部滑入。
 * - [buttonColors] 覆盖非 recommended 按钮的配色（例如停止类操作使用 error 色文字）。
 * - 内容里要读取输入法时无需额外处理，COUI 的对话框布局已处理 ime 避让。
 */
@Composable
internal fun ArtDialog(
    show: Boolean = true,
    title: String? = null,
    summary: String? = null,
    onDismissRequest: (() -> Unit)?,
    onDismissFinished: (() -> Unit)? = null,
    negative: DialogButtonBarAction? = null,
    positive: DialogButtonBarAction? = null,
    neutral: DialogButtonBarAction? = null,
    centered: Boolean = false,
    buttonColors: TextButtonColors? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val hasButtons = negative != null || positive != null || neutral != null
    WindowDialog(
        show = show,
        title = title,
        summary = summary,
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        largeScreen = if (centered) true else null,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (content != null) {
                // 标题下方 COUI 只留 6dp；有摘要时摘要自带 18dp 下边距；都没有时补顶部 24dp
                val top = when {
                    summary != null -> 0.dp
                    title != null -> 10.dp
                    else -> 24.dp
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = ArtDialogContentHorizontalPadding,
                            end = ArtDialogContentHorizontalPadding,
                            top = top,
                            bottom = if (hasButtons) 8.dp else 24.dp,
                        ),
                    content = content,
                )
            }
            if (hasButtons) {
                if (buttonColors != null) {
                    DialogButtonBar(
                        negative = negative,
                        positive = positive,
                        neutral = neutral,
                        colors = buttonColors,
                    )
                } else {
                    DialogButtonBar(
                        negative = negative,
                        positive = positive,
                        neutral = neutral,
                    )
                }
            }
        }
    }
}

/**
 * 居中的确认类对话框：标题 + 摘要 + 取消 / 确认两个按钮，[content] 为按钮栏上方的可选额外内容
 * （例如「以后都自动确认」勾选行）。
 *
 * 审计建议直接包 COUI `WindowAlertDialog`，但它的确认按钮没有 enabled 参数，且 content 槽无内边距，
 * 所以这里基于 [ArtDialog]（`WindowDialog` 居中样式 + [DialogButtonBar]，与 WindowAlertDialog
 * 同一套布局）实现，额外支持 [confirmEnabled]。
 * 与 WindowAlertDialog 一致：确认不会自动关闭，点击外部 / 返回键走 [onCancel]。
 */
@Composable
internal fun ArtAlertDialog(
    show: Boolean = true,
    title: String?,
    summary: String?,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    confirmEnabled: Boolean = true,
    onDismissFinished: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    ArtDialog(
        show = show,
        title = title,
        summary = summary,
        onDismissRequest = onCancel,
        onDismissFinished = onDismissFinished,
        negative = DialogButtonBarAction(text = cancelText, onClick = onCancel),
        positive = DialogButtonBarAction(text = confirmText, enabled = confirmEnabled, onClick = onConfirm),
        centered = true,
        content = content?.let { extra -> { extra() } },
    )
}

/**
 * 进度对话框：标题 + [message] / [detail]（进度条上方的说明行）+ 进度条 + [footer]（进度条下方的计数行）。
 *
 * [progress] 为 0..1 时是确定进度，null 时是不确定进度。COUI 的 `WindowProgressDialog` 只有一行文字、
 * 没有标题和按钮槽，满足不了这里的多行信息和「取消 / 后台」按钮，所以走 [ArtDialog] + `LinearProgressIndicator`。
 * [onDismissRequest] 默认 null：不可被外部点击 / 返回键关闭。
 */
@Composable
internal fun ArtProgressDialog(
    title: String?,
    progress: Float?,
    message: String? = null,
    detail: String? = null,
    footer: String? = null,
    show: Boolean = true,
    onDismissRequest: (() -> Unit)? = null,
    negative: DialogButtonBarAction? = null,
    positive: DialogButtonBarAction? = null,
    buttonColors: TextButtonColors? = null,
) {
    ArtDialog(
        show = show,
        title = title,
        onDismissRequest = onDismissRequest,
        negative = negative,
        positive = positive,
        buttonColors = buttonColors,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (!message.isNullOrBlank()) {
                Text(
                    text = message,
                    style = COUITheme.textStyles.footnote1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (!detail.isNullOrBlank()) {
                Text(
                    text = detail,
                    style = COUITheme.textStyles.footnote1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            )
            if (!footer.isNullOrBlank()) {
                Text(
                    text = footer,
                    style = COUITheme.textStyles.footnote1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
