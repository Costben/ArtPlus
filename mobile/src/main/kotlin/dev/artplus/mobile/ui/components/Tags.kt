package dev.artplus.mobile.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.ExpandArrow
import io.github.suqi8.coui.kmp.basic.LinearProgressIndicator
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.basic.TagView
import io.github.suqi8.coui.kmp.basic.TagViewDefaults

internal data class AppListTag(
    val label: String,
    val backgroundColor: Color,
    val contentColor: Color,
)

@Composable
internal fun AppStatusTag(tag: AppListTag) {
    TagView(
        text = tag.label,
        cornerRadius = AppTagCornerRadius,
        colors = TagViewDefaults.tagViewColors(
            containerColor = tag.backgroundColor,
            contentColor = tag.contentColor,
        ),
    )
}

@Composable
internal fun KernelStyleArrow(modifier: Modifier = Modifier, expanded: Boolean = false) {
    ExpandArrow(
        expanded = expanded,
        modifier = modifier.padding(start = 4.dp),
    )
}

@Composable
internal fun BrandMark(size: Dp, text: String) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(COUITheme.colorScheme.primaryVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = COUITheme.textStyles.title4,
            color = COUITheme.colorScheme.onPrimary,
            maxLines = 1,
        )
    }
}

@Composable
internal fun MetricPill(label: String, modifier: Modifier = Modifier) {
    TagView(
        text = label,
        modifier = modifier,
        cornerRadius = 999.dp,
        colors = TagViewDefaults.tagViewColors(
            containerColor = COUITheme.colorScheme.secondaryVariant,
            contentColor = COUITheme.colorScheme.onSurfaceSecondary,
        ),
    )
}

@Composable
internal fun LayerDebugTile(
    label: String,
    bitmap: ImageBitmap?,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .clip(RoundedCornerShape(CompactCardCornerRadius))
                .background(COUITheme.colorScheme.secondaryVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) {
                Image(
                    // decorative：纯装饰（箭头/应用图标），语义由相邻文本承载
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text(
                    text = "无",
                    style = COUITheme.textStyles.footnote1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = COUITheme.textStyles.footnote1,
            color = COUITheme.colorScheme.onSurfaceVariantSummary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = COUITheme.textStyles.footnote1,
                color = COUITheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun RmbgInstallProgressBar(text: String, progress: Float?, active: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (text.isNotBlank()) {
            Text(
                text = text,
                style = COUITheme.textStyles.footnote1,
                color = COUITheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        LinearProgressIndicator(
            progress = progress?.coerceIn(0f, 1f) ?: if (active) null else 0f,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
