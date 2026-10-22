package dev.artplus.mobile.ui.components

import androidx.compose.ui.unit.dp

/** Compact card corner radius; COUI's default is 17.dp (CardDefaults.CornerRadius). */
internal val CompactCardCornerRadius = 14.dp

/** Tag corner radius; COUI's default is 0.dp (TagViewDefaults.CornerRadius). */
internal val AppTagCornerRadius = 4.dp

// S5.3：尺寸 token。
// 底栏让位量由「底栏实测高度 + 手势条 inset」推导（COUI BottomFloatingNavigationBar：
// 64dp 胶囊 + 8dp 上下留白 + 16dp 安全边距 = 88dp），原 6 处硬编码 88.dp 统一引用 BarClearance。
internal object Spacing {
    val page = 16.dp
    val card = 12.dp
}

internal object Radius {
    val card = 14.dp
}

internal val BarClearance = 88.dp
