package dev.artplus.mobile.ui.pages.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.EmptyState
import io.github.suqi8.coui.kmp.utils.PressFeedbackType
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TextButton
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.icon.basic.ArrowRight
import io.github.suqi8.coui.kmp.icon.extended.Layers
import io.github.suqi8.coui.kmp.theme.COUITheme
import dev.artplus.mobile.data.AppEntry
import dev.artplus.mobile.ui.components.BrandMark
/**
 * Slice 2.2：主页状态卡片（原 MainActivity 残留本体原样搬迁）。
 * Composable 只收 state + onEvent；Activity 状态经参数/回调注入，行为与 UI 100% 等价。
 */

@Composable
internal fun StatusCard(
    selectedApp: AppEntry?,
    isBusy: Boolean,
    hasApps: Boolean,
    statusText: String,
    onOpenPicker: () -> Unit,
    appIcon: @Composable (AppEntry) -> Unit,
) {
    val enabled = !isBusy && hasApps

    // COUI 可点击卡片：Tint 按压反馈由 Card 提供；不可用时不挂 onClick
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(16.dp),
        pressFeedbackType = PressFeedbackType.Tint,
        onClick = if (enabled) onOpenPicker else null,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (selectedApp == null) {
                BrandMark(size = 48.dp, text = "UX")
            } else {
                appIcon(selectedApp)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = selectedApp?.label ?: "选择一个应用开始生成",
                    style = COUITheme.textStyles.title4,
                    color = COUITheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = selectedApp?.packageName ?: statusText,
                    style = COUITheme.textStyles.footnote1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            // COUI 右箭头（与 ArrowPreference 末端箭头一致）
            Image(
                modifier = Modifier.size(width = 12.dp, height = 24.dp),
                imageVector = COUIIcons.Basic.ArrowRight,
                // decorative：纯装饰（箭头/应用图标），语义由相邻文本承载
                contentDescription = null,
                colorFilter = ColorFilter.tint(COUITheme.colorScheme.onSurfaceVariantActions),
            )
        }
    }
}

@Composable
internal fun EmptyAppListCard(
    queryText: String,
    showSystemApps: Boolean,
    hasHiddenSystemApps: Boolean,
    isBusy: Boolean,
    onShowSystemApps: () -> Unit,
    onRefresh: () -> Unit,
) {
    val hintText = when {
        queryText.isNotBlank() && !showSystemApps && hasHiddenSystemApps ->
            "没有匹配“${queryText.trim()}”的应用。尝试清空搜索词或打开“显示系统应用”开关查看系统应用。"
        queryText.isNotBlank() ->
            "没有匹配“${queryText.trim()}”的应用，尝试清空搜索词。"
        hasHiddenSystemApps ->
            "当前已隐藏系统应用。打开“显示系统应用”开关可查看系统应用，或在系统设置中允许 ArtPlus 读取应用列表后刷新。"
        else ->
            "清空搜索词，或在系统设置中允许 ArtPlus 读取应用列表后刷新。"
    }
    Card(
        modifier = Modifier.fillMaxWidth().semantics {
            contentDescription = "空应用列表提示"
        },
        insideMargin = PaddingValues(16.dp),
    ) {
        // COUI EmptyState 承载插画 + 标题 + 说明；两个操作保持 TextButton 以保留 enabled 语义
        EmptyState(
            title = "没有可显示的应用",
            subtitle = hintText,
            modifier = Modifier.fillMaxWidth(),
            illustration = {
                Icon(
                    imageVector = COUIIcons.Layers,
                    contentDescription = "暂无内容",
                    modifier = Modifier.size(36.dp),
                    tint = COUITheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.4f),
                )
            },
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (hasHiddenSystemApps && !showSystemApps) {
            TextButton(
                text = "显示系统应用",
                onClick = onShowSystemApps,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth().semantics {
                    contentDescription = "显示系统应用按钮"
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        TextButton(
            text = "刷新应用列表",
            onClick = onRefresh,
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth().semantics {
                contentDescription = "刷新应用列表"
            },
        )
    }
}
