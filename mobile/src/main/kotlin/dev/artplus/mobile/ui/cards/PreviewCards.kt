package dev.artplus.mobile.ui.cards

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import dev.artplus.mobile.ui.components.CompactActionButton

@Composable
internal fun AiIconLoadingPreview(
    modifier: Modifier = Modifier.size(72.dp),
    overlay: Boolean = false,
) {
    val transition = rememberInfiniteTransition(label = "AiIconLoading")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "AiIconLoadingPhase",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "AiIconLoadingPulse",
    )
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (overlay) Color.Transparent else Color(0xFF14131A)),
        contentAlignment = Alignment.Center,
    ) {
        ComposeCanvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF15131D),
                        Color(0xFF1D2136),
                        Color(0xFF171522),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h),
                ),
                alpha = if (overlay) 0.22f else 1f,
            )
            val rows = 9
            val cols = 11
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    val fx = (col + 0.5f) / cols
                    val fy = (row + 0.5f) / rows
                    val wave = sin((fx * 5.6f + phase * 6.28318f).toDouble()).toFloat()
                    val ribbon = 1f - (abs(fy - (0.52f + wave * 0.18f)) / 0.34f).coerceIn(0f, 1f)
                    val shimmer = 0.55f + 0.45f * sin((phase * 6.28318f + row * 0.74f + col * 0.39f).toDouble()).toFloat()
                    val alphaBase = (0.12f + ribbon * 0.46f + pulse * 0.12f + shimmer * 0.08f).coerceIn(0.12f, 0.72f)
                    val alpha = if (overlay) alphaBase * 0.58f else alphaBase
                    val radius = w * (0.012f + ribbon * 0.012f)
                    val color = when ((row + col) % 5) {
                        0 -> Color(0xFF28F7D2)
                        1 -> Color(0xFF4C83FF)
                        2 -> Color(0xFFE044FF)
                        3 -> Color(0xFFFFC857)
                        else -> Color(0xFFFF4D9D)
                    }
                    val offsetX = sin((phase * 6.28318f + row).toDouble()).toFloat() * w * 0.018f
                    val offsetY = cos((phase * 6.28318f + col).toDouble()).toFloat() * h * 0.014f
                    drawCircle(
                        color = color,
                        radius = radius,
                        center = Offset(w * fx + offsetX, h * fy + offsetY),
                        alpha = alpha,
                    )
                }
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.18f + pulse * 0.12f), Color.Transparent),
                    center = Offset(w * (0.48f + 0.12f * sin((phase * 6.28318f).toDouble()).toFloat()), h * 0.5f),
                    radius = w * 0.54f,
                ),
                radius = w * 0.54f,
                center = Offset(w * 0.5f, h * 0.5f),
                alpha = if (overlay) 0.16f else 0.35f,
            )
        }
    }
}

@Composable
internal fun PreviewChoiceActions(
    showApplyAll: Boolean,
    applyEnabled: Boolean,
    onApplyAll: () -> Unit,
) {
    Box(
        modifier = Modifier.width(88.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (showApplyAll) {
            CompactActionButton(
                text = "应用",
                onClick = onApplyAll,
                enabled = applyEnabled,
                modifier = Modifier.fillMaxWidth(),
                height = 56.dp,
            )
        }
    }
}
