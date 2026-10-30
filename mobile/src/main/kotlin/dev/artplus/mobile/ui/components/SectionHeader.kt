package dev.artplus.mobile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.SmallTitle

/**
 * 卡片外的小标题：COUI 的分组标题（`couiTextAppearanceSmallButton`，12sp Medium），
 * 放在卡片左上角外侧，说明这一组是干什么的。
 *
 * 页面列表本身已有 16dp 横向边距，标题再内缩 16dp，文字就落在卡片左边缘内 16dp 处，
 * 与 COUI 原生设置页的分组标题位置一致。
 */
@Composable
internal fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    SmallTitle(
        text = text,
        modifier = modifier.fillMaxWidth(),
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    )
}

/** 列表里的「小标题 + 卡片」一组：标题在卡片外左上角，和本组内容同属一个列表项。 */
internal fun LazyListScope.sectionItem(
    key: String,
    title: String,
    content: @Composable () -> Unit,
) = item(key = key) {
    Column(Modifier.fillMaxWidth()) {
        SectionHeader(title)
        content()
    }
}
