package dev.artplus.mobile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.CardDefaults
import io.github.suqi8.coui.kmp.basic.HorizontalDivider
import dev.artplus.mobile.SECTION_CARD_VERTICAL_PADDING_DP

/**
 * 设置/功能卡片：COUI Card 自带 17dp 圆角裁切，行内按压反馈由 COUI 指示器提供。
 * 卡片内多行之间请用 [SectionDivider] 分隔。
 *
 * [title] 是卡片外左上角的分组小标题，用来在页面上说明这一组是干什么的。
 */
@Composable
internal fun SectionCard(
    rowsFullBleed: Boolean = false,
    color: Color? = null,
    title: String? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (title != null) SectionHeader(title)
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = if (rowsFullBleed) {
                PaddingValues(
                    horizontal = 0.dp,
                    vertical = SECTION_CARD_VERTICAL_PADDING_DP.dp,
                )
            } else {
                PaddingValues(16.dp)
            },
            colors = if (color != null) CardDefaults.defaultColors(color = color) else CardDefaults.defaultColors(),
        ) {
            content()
        }
    }
}

/** 卡片内行间分隔线：ColorOS 17 的 0.33dp 细线，左右各缩进 16dp。 */
@Composable
internal fun SectionDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}
