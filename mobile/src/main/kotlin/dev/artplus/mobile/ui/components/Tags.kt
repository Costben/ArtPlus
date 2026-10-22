package dev.artplus.mobile

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
import io.github.suqi8.coui.kmp.basic.LinearProgressIndicator
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.basic.ArrowRight
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
    val layoutDirection = LocalLayoutDirection.current
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "kernel-style-arrow-rotation",
    )

    Image(
        modifier = modifier
            .graphicsLayer {
                rotationZ = rotation
                if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
            }
            .padding(start = 8.dp)
            .size(width = 10.dp, height = 16.dp),
        imageVector = COUIIcons.Basic.ArrowRight,
        contentDescription = null,
        colorFilter = ColorFilter.tint(COUITheme.colorScheme.onSurfaceVariantActions),
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
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(COUITheme.colorScheme.secondaryVariant)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = COUITheme.textStyles.footnote1,
            color = COUITheme.colorScheme.onSurfaceSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun StatusDot(active: Boolean) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (active) {
                    COUITheme.colorScheme.primaryVariant
                } else {
                    COUITheme.colorScheme.secondaryContainer
                },
            ),
    )
}

@Composable
internal fun LayerDebugTile(label: String, bitmap: ImageBitmap?, modifier: Modifier = Modifier) {
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
