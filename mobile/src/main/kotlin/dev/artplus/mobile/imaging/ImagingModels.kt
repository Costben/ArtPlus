package dev.artplus.mobile

import android.graphics.Bitmap

/**
 * 图像几何：可见像素包围盒。
 *
 * 从 MainActivity 迁移而来（原 private data class Bounds）。
 * 纯数据结构，不依赖 Activity 状态。
 */
internal data class Bounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    fun width(): Int = right - left
    fun height(): Int = bottom - top
}

/**
 * 管线结果模型（P1.2-b 从 MainActivity 提升而来，原 private 嵌套类）。
 */
internal data class ForegroundCleanupResult(
    val bitmap: Bitmap,
    val changed: Boolean,
    val removedRatio: Double,
    val repairedRatio: Double,
)

internal data class ShadowCleanupResult(
    val bitmap: Bitmap,
    val changed: Boolean,
    val removedRatio: Double,
)

internal data class EdgeAnalysis(
    val coverage: Double,
    val color: Int,
)

internal data class MaskComponent(
    val indices: IntArray,
    val touchesEdge: Boolean,
    val bounds: Bounds,
) {
    val size: Int
        get() = indices.size
}

/**
 * 管线/UI 共用类型簇（P1.2-b b3-slice0 从 MainActivity 纯移动而来，原 private 嵌套）。
 * PreviewChoice 簇 + pipeline 类型 + Rmbg 推理报告（RmbgInferenceBackend 为自包含小 enum，随带提升）。
 * 反向引用方（PreviewSelections / GenerationSession / PreviewMode 等暂留类型）同包零改动。
 */
internal enum class PreviewChoice(
    val label: String,
    val summary: String,
    val customKind: CustomImageKind? = null,
) {
    Original("原始", "保留原层"),
    TextSafe("字标保全", "保护白字"),
    Plate("清理", "兼容旧去底板规则"),
    Full("清理", "合并底板与阴影清理"),
    ComposedBackground("拼合背景", "从完整图标提取背景"),
    ComponentSubject("底座当主体", "保留复杂底座"),
    ComponentBackground("底座当背景", "底座作为背景"),
    TwoLayer("二层", "底板和主体分层"),
    Rmbg("RMBG", "模型抠图"),
    Gpt("AI", "AI生成"),
    RmbgComposedBackground("拼合背景", "RMBG 主体 + 原图背景"),
    GptComposedBackground("拼合背景", "AI 主体 + 原图背景"),
    CustomForeground("自定义主体", "导入主体", CustomImageKind.Foreground),
    CustomBackground("自定义背景", "导入背景", CustomImageKind.Background);

    val isCustom: Boolean
        get() = customKind != null
}

internal val PreviewChoice.isComposedBackgroundCombination: Boolean
    get() = this == PreviewChoice.RmbgComposedBackground ||
        this == PreviewChoice.GptComposedBackground

internal enum class CustomImageKind(val label: String) {
    Foreground("自定义主体"),
    Background("自定义背景"),
}

internal enum class LocalSeparationMode(val value: String, val label: String, val summary: String) {
    Auto("auto", "自动", "按图标特征自动选择底板清理、边缘修复或阴影清理"),
    Original("original", "原始", "完全保留系统绘制的前景层"),
    Plate("plate", "清理", "兼容旧去底板规则"),
    Full("full", "清理", "合并底板和长阴影清理"),
    ComposedBackground("composed_background", "拼合背景", "先拼合完整图标，再从拼合图里估算背景并分离主体"),
    ComponentSubject("component_subject", "底座当主体", "把 adaptive background 里的复杂底座合进主体，背景重建为纯色或渐变"),
    ComponentBackground("component_background", "底座当背景", "保留 adaptive background 为背景，只取 foreground 当主体");

    companion object {
        fun fromValue(value: String?): LocalSeparationMode =
            entries.firstOrNull { it.value == value }
                ?.let { if (it == Plate) Full else it }
                ?: Auto
    }
}

internal data class LocalSeparationResult(
    val bitmap: Bitmap,
    val summary: String,
)

internal data class IconCandidate(
    val recfgRaw: Bitmap,
    val recbg: Bitmap,
    val monochromeRaw: Bitmap?,
    val monochromeIsNative: Boolean = false,
    val monochromeFromDefaultSubject: Boolean = false,
    val preserveGeometry: Boolean = false,
    val customFinalBitmap: Bitmap? = null,
    val rmbgSourceRaw: Bitmap? = null,
    val rmbgAlphaRaw: IntArray? = null,
    val isLocal: Boolean = true,
    val applyLocalEdgePolish: Boolean = true,
)

internal data class LocalIconLayers(
    val recfg: Bitmap,
    val recbg: Bitmap,
    val monochrome: Bitmap?,
    val monochromeIsNative: Boolean,
    val preserveGeometry: Boolean,
    val textSafe: IconCandidate?,
    val componentSubject: IconCandidate?,
    val componentBackground: IconCandidate?,
)

internal data class LocalCandidateSet(
    val candidates: Map<PreviewChoice, IconCandidate>,
    val autoChoice: PreviewChoice,
)

internal data class CandidateBuildResult(
    val candidate: IconCandidate?,
    val autoUsable: Boolean,
    val coverage: Double,
    val rmbgInference: RmbgInferenceReport? = null,
    val manualUsable: Boolean = true,
    val validationWarning: String? = null,
)

internal data class RmbgInferenceReport(
    val actualBackend: RmbgInferenceBackend,
    val elapsedMs: Long,
)

internal enum class RmbgInferenceBackend(
    val value: String,
    val label: String,
) {
    Cpu("cpu", "CPU"),
}

internal enum class OriginalForegroundCleanupMode(val value: String, val label: String) {
    Auto("auto", "自动安全清理"),
    Off("off", "关闭"),
    Plate("plate", "强制去底板");

    companion object {
        fun fromValue(value: String?): OriginalForegroundCleanupMode =
            entries.firstOrNull { it.value == value } ?: Auto
    }
}

internal data class AdaptiveForegroundSelection(
    val bitmap: Bitmap,
    val preserveGeometry: Boolean,
)

internal enum class AdaptiveForegroundMode(val value: String, val label: String) {
    Auto("auto", "自动"),
    Composed("composed", "合成减背景"),
    Direct("direct", "直接前景");

    companion object {
        fun fromValue(value: String?): AdaptiveForegroundMode =
            entries.firstOrNull { it.value == value } ?: Auto
    }
}
