package dev.artplus.mobile.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * 软盘形状的「保存」图标。
 *
 * COUI 的图标集里没有保存/软盘（只有勾、下载、导入这类），而「保存成预设」用勾会和确认类动作混淆，
 * 所以按 24dp 视口手画一个：右上角切一刀的方形 + 顶部滑块 + 底部标签。
 *
 * 笔画用纯黑绘制，`Icon` 的 tint 会整体重新着色，颜色仍由调用方决定；描边而不是填充轮廓，
 * 是因为 COUI 的线性图标本质上就是「外轮廓 + 反向内轮廓」的镂空，这里用描边得到同样的观感且路径短得多。
 */
internal val SaveFloppyIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "SaveFloppy",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            // 外轮廓：右上角 45° 切角，其余三个角小圆角
            moveTo(5f, 3.6f)
            lineTo(15.9f, 3.6f)
            lineTo(20.4f, 8.1f)
            lineTo(20.4f, 19f)
            quadTo(20.4f, 20.4f, 19f, 20.4f)
            lineTo(5f, 20.4f)
            quadTo(3.6f, 20.4f, 3.6f, 19f)
            lineTo(3.6f, 5f)
            quadTo(3.6f, 3.6f, 5f, 3.6f)
            close()
            // 顶部金属滑块
            moveTo(8.2f, 3.6f)
            lineTo(8.2f, 8.6f)
            lineTo(14.4f, 8.6f)
            lineTo(14.4f, 3.6f)
            // 底部标签纸
            moveTo(7.2f, 20.4f)
            lineTo(7.2f, 14.6f)
            lineTo(16.8f, 14.6f)
            lineTo(16.8f, 20.4f)
        }
    }.build()
}
