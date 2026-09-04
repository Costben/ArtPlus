package dev.artplus.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Slice 2.3：JSON 调参编辑器（原 MainActivity 本体原样搬迁）。
 * 只做纯移动：文案/布局一律不变。
 * Activity 状态（currentParams/draft 文本）经参数注入，存取（saveJsonParamsFromText，Slice 2.4）
 * 经 onSave 回调注入；MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动。
 */

@Composable
internal fun JsonSettingsEditorCard(
    currentParams: TuningParams,
    draftText: String,
    onDraftChange: (String) -> Unit,
    onSave: (String) -> Unit,
    onRestore: () -> Unit,
) {
    SectionCard {
        JsonSettingsEditor(
            currentParams = currentParams,
            draftText = draftText,
            onDraftChange = onDraftChange,
            onSave = onSave,
            onRestore = onRestore,
        )
    }
}

@Composable
internal fun JsonSettingsEditor(
    currentParams: TuningParams,
    draftText: String,
    onDraftChange: (String) -> Unit,
    onSave: (String) -> Unit,
    onRestore: () -> Unit,
) {
    LaunchedEffect(currentParams) {
        onDraftChange(currentParams.toJson().toString(4))
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "全部调参参数（含本地工作流开关、分离/清理/RMBG/液态玻璃/自适应/各模式选型）。可视化改动会同步到这里，也可直接编辑 JSON 后保存。",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        BasicTextField(
            value = draftText,
            onValueChange = onDraftChange,
            singleLine = false,
            textStyle = MiuixTheme.textStyles.body1.copy(
                color = MiuixTheme.colorScheme.onSurface,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            ),
            cursorBrush = SolidColor(MiuixTheme.colorScheme.primaryVariant),
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                .padding(12.dp),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxSize()) {
                    innerTextField()
                }
            },
        )
        Text(
            text = "缺失的键保持当前值；非法 JSON 会提示错误且不生效。",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TextButton(
                text = "恢复当前",
                onClick = onRestore,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                text = "保存并应用",
                onClick = { onSave(draftText) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
