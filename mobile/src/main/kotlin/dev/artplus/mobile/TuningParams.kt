package dev.artplus.mobile

import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * 可调参数的范围与默认值（顶层 const，MainActivity 同包直接引用）。
 *
 * 从 MainActivity companion object 迁移而来（原 15214–15330 行）。
 */
const val MONO_ALPHA_MIN = 40
const val MONO_ALPHA_MAX = 230
const val MONO_ALPHA_GAMMA = 0.85
const val MONO_LIGHT_PREVIEW_SCALE = 0.90
const val DEFAULT_MONOCHROME_THEME_SCALE = 0.80f
const val MIN_MONOCHROME_THEME_SCALE = 0.20f
const val MAX_MONOCHROME_THEME_SCALE = 1.50f
const val MIN_MONOCHROME_THEME_SCALE_PERCENT = 20
const val MAX_MONOCHROME_THEME_SCALE_PERCENT = 150
const val MONO_EDGE_ALPHA_DROP_THRESHOLD = 12
const val MONO_EDGE_ALPHA_IGNORE_THRESHOLD = 32
const val MONO_EDGE_ALPHA_REPAIR_THRESHOLD = 96
const val MONO_EDGE_REPAIR_RADIUS = 3
const val MONO_TONAL_MIN_VISIBLE_PIXELS = 64
const val MONO_TONAL_RANGE_THRESHOLD = 12
const val MONO_EDGE_TRIM_FEATHER_SCALE = 0.26
const val MONO_NATIVE_MAX_TILE_COVERAGE = 0.70
const val MONO_NATIVE_MAX_COVERAGE_EXTRA = 0.18
const val MONO_NATIVE_EDGE_LOW_CUT = 6
const val MONO_NATIVE_EDGE_HIGH_CUT = 44
const val MONO_EDGE_SHARPEN_LOW_CUT = 18
const val MONO_EDGE_SHARPEN_HIGH_CUT = 96
const val MONO_EDGE_FEATHER_BLEND = 0.58
const val MONO_EDGE_SMOOTH_STRENGTH = 0.68
const val MONO_EDGE_SMOOTH_RADIUS = 2
const val MONO_EDGE_GROW_STRENGTH = 0.42
const val MONO_EDGE_POLISH_RADIUS = 1
const val MIN_FOREGROUND_SUBJECT_PERCENT = 20
const val MAX_FOREGROUND_SUBJECT_PERCENT = 150
const val DEFAULT_FOREGROUND_SUBJECT_PERCENT = 100
const val LEGACY_FOREGROUND_SUBJECT_PERCENT = 70
const val DEFAULT_FOREGROUND_SHADOW_LEVEL = 0
const val MIN_FOREGROUND_SHADOW_LEVEL = 0
const val MAX_FOREGROUND_SHADOW_LEVEL = 10
const val FOREGROUND_SHADOW_MAX_ALPHA = 190
const val FOREGROUND_SHADOW_MAX_BLUR = 7.5
const val FOREGROUND_SHADOW_MAX_OFFSET_X = 5.0
const val FOREGROUND_SHADOW_MAX_OFFSET_Y = 7.0
const val FOREGROUND_SHADOW_MAX_SPREAD = 2.0
const val DEFAULT_BACKGROUND_SEPARATION_PERCENT = 60
const val MIN_BACKGROUND_SEPARATION_PERCENT = 1
const val MAX_BACKGROUND_SEPARATION_PERCENT = 100
const val DEFAULT_PLATE_REMOVAL_PERCENT = 58
const val MIN_PLATE_REMOVAL_PERCENT = 1
const val MAX_PLATE_REMOVAL_PERCENT = 100
const val DEFAULT_SHADOW_REMOVAL_PERCENT = 60
const val MIN_SHADOW_REMOVAL_PERCENT = 1
const val MAX_SHADOW_REMOVAL_PERCENT = 100
const val DEFAULT_EDGE_POLISH_PERCENT = 60
const val MIN_EDGE_POLISH_PERCENT = 1
const val MAX_EDGE_POLISH_PERCENT = 100
const val DEFAULT_RMBG_ALPHA_STRENGTH_PERCENT = 100
const val MIN_RMBG_ALPHA_STRENGTH_PERCENT = 20
const val MAX_RMBG_ALPHA_STRENGTH_PERCENT = 220
const val DEFAULT_RMBG_EDGE_FEATHER_PERCENT = 0
const val MIN_RMBG_EDGE_FEATHER_PERCENT = 0
const val MAX_RMBG_EDGE_FEATHER_PERCENT = 100
const val DEFAULT_RMBG_EDGE_ADJUST_PERCENT = 50
const val MIN_RMBG_EDGE_ADJUST_PERCENT = 0
const val MAX_RMBG_EDGE_ADJUST_PERCENT = 100
const val DEFAULT_RMBG_WEAK_ALPHA_KEEP_PERCENT = 100
const val MIN_RMBG_WEAK_ALPHA_KEEP_PERCENT = 0
const val MAX_RMBG_WEAK_ALPHA_KEEP_PERCENT = 100
const val DEFAULT_LIQUID_GLASS_RADIUS = 95
const val MIN_LIQUID_GLASS_RADIUS = 0
const val MAX_LIQUID_GLASS_RADIUS = 240
const val DEFAULT_LIQUID_GLASS_OUTER_WIDTH = 2
const val MIN_LIQUID_GLASS_OUTER_WIDTH = 0
const val MAX_LIQUID_GLASS_OUTER_WIDTH = 70
const val DEFAULT_LIQUID_GLASS_TOP_ALPHA = 175
const val DEFAULT_LIQUID_GLASS_BOTTOM_ALPHA = 122
const val MIN_LIQUID_GLASS_ALPHA = 0
const val MAX_LIQUID_GLASS_ALPHA = 255
const val DEFAULT_LIQUID_GLASS_BACKGROUND_MIST_ALPHA = 0
const val MIN_LIQUID_GLASS_MIST_ALPHA = 0
const val MAX_LIQUID_GLASS_MIST_ALPHA = 160
const val DEFAULT_LIQUID_GLASS_BOTTOM_DARK_ALPHA = 24
const val MIN_LIQUID_GLASS_BOTTOM_DARK_ALPHA = 0
const val MAX_LIQUID_GLASS_BOTTOM_DARK_ALPHA = 50
const val DEFAULT_LIQUID_GLASS_SUBJECT_SCALE_PERCENT = 100
const val MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT = 45
const val MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT = 180
const val DEFAULT_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH = 0
const val DEFAULT_LIQUID_GLASS_SUBJECT_INNER_OUTLINE_WIDTH = 0
const val MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH = 0
const val MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH = 36
const val DEFAULT_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA = 0
const val MIN_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA = 0
const val MAX_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA = 180
const val DEFAULT_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT = 100
const val MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT = 0
const val MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT = 100
const val LIQUID_GLASS_SUBJECT_ALPHA_NORMALIZE_PERCENTILE = 0.96
const val LIQUID_GLASS_SUBJECT_ALPHA_BODY_PERCENTILE = 0.90
const val LIQUID_GLASS_SUBJECT_ALPHA_OUTLIER_CAP = 1.25
const val LIQUID_GLASS_SUBJECT_SOLID_ALPHA_RATIO = 0.48f
const val LEGACY_BACKGROUND_SEPARATION_MIN = 12.0
const val LEGACY_BACKGROUND_SEPARATION_MAX = 420.0
const val LEGACY_PLATE_REMOVAL_MIN = 0.0
const val LEGACY_PLATE_REMOVAL_MAX = 420.0
const val LEGACY_SHADOW_REMOVAL_MIN = 0.0
const val LEGACY_SHADOW_REMOVAL_MAX = 255.0
const val DEFAULT_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT = 68
const val MIN_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT = 0
const val MAX_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT = 100
const val DEFAULT_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT = 8
const val MIN_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT = 0
const val MAX_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT = 100
const val DEFAULT_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT = 34
const val MIN_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT = 0
const val MAX_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT = 100
const val DEFAULT_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT = 45
const val MIN_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT = 0
const val MAX_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT = 100
const val DEFAULT_ADAPTIVE_CENTER_EPSILON_PERCENT = 3
const val MIN_ADAPTIVE_CENTER_EPSILON_PERCENT = 0
const val MAX_ADAPTIVE_CENTER_EPSILON_PERCENT = 50

/**
 * Alpha 几何与抠像阈值（imaging/ 纯函数与 MainActivity 共用）。
 *
 * 从 MainActivity companion object 迁移而来（P1 拆分）：原为 private，
 * 现为顶层公开常量，数值未变。
 */
const val LOCAL_ALPHA_VISIBLE_THRESHOLD = 8
const val NORMALIZE_ALPHA_BOUNDS_THRESHOLD = 48
const val CORNER_MASK_ZONE_SIZE = 68
const val CORNER_MASK_OPAQUE_ALPHA = 250
const val CORNER_MASK_BACKGROUND_DISTANCE = 90.0
const val CORNER_MASK_SUBJECT_ALPHA = 32
const val CORNER_MASK_SUBJECT_BACKGROUND_DISTANCE = 130.0
const val CORNER_MASK_WHITE_THRESHOLD = 220
const val CORNER_MASK_WHITE_EDGE_ALPHA = 180
const val CHROMA_TRANSPARENT_THRESHOLD = 36.0
const val CHROMA_OPAQUE_THRESHOLD = 170.0
val CHROMA_KEY_CANDIDATES = intArrayOf(
    android.graphics.Color.rgb(0, 255, 0),
    android.graphics.Color.rgb(255, 0, 255),
    android.graphics.Color.rgb(0, 255, 255),
    android.graphics.Color.rgb(0, 0, 255),
    android.graphics.Color.rgb(255, 255, 0),
)

/**
 * 管线阈值（imaging/ 管线函数与 MainActivity 共用）。
 *
 * 从 MainActivity companion object 迁移而来（P1.2 拆分）：原为 private，
 * 现为顶层公开常量，数值与类型未变。同包直接引用，Activity 内原有调用点零改动。
 */
const val RESIDUE_MAX_ALPHA = 190
const val RESIDUE_BACKGROUND_MIN_SATURATION = 0.18
const val RESIDUE_DISTANCE_SCALE = 1.45
const val RESIDUE_MIN_DISTANCE = 64.0
const val RESIDUE_MAX_DISTANCE = 190.0
const val RESIDUE_CONNECTED_MAX_ALPHA = 248
const val RESIDUE_CONNECTED_DISTANCE_SCALE = 2.15
const val RESIDUE_CONNECTED_MIN_DISTANCE = 96.0
const val RESIDUE_CONNECTED_MAX_DISTANCE = 260.0
const val RESIDUE_CONNECTED_TRANSPARENT_RADIUS = 2
const val PLAIN_ICON_EDGE_BAND_RATIO = 0.06f
const val PLAIN_ICON_BACKGROUND_ALPHA_THRESHOLD = 32
const val PLAIN_ICON_MIN_BACKGROUND_SAMPLES = 20
const val CORNER_MASK_SEED_SIZE = 56
const val CORNER_MASK_MAX_REMOVED_RATIO = 0.45
const val SHADOW_HIGH_ALPHA_THRESHOLD = 160
const val SHADOW_MAX_SATURATION_MIN = 0.08
const val SHADOW_MAX_SATURATION_MAX = 0.42
const val SHADOW_MAX_LUMINANCE_MIN = 120.0
const val SHADOW_MAX_LUMINANCE_MAX = 245.0
const val SHADOW_MIN_VISIBLE_RATIO_MIN = 0.012
const val SHADOW_MIN_VISIBLE_RATIO_MAX = 0.085
const val SHADOW_MIN_OFFSET_MIN = 2.0
const val SHADOW_MIN_OFFSET_MAX = 16.0
const val SHADOW_MIN_DOWN_OFFSET_MIN = -2.0
const val SHADOW_MIN_DOWN_OFFSET_MAX = 6.0
const val SHADOW_MIN_LUMA_DROP_MIN = 2.0
const val SHADOW_MIN_LUMA_DROP_MAX = 18.0
const val SHADOW_EDGE_ANTIALIAS_RADIUS = 2
const val SHADOW_EDGE_REPAIR_MAX_ALPHA = 96
const val SHADOW_PRESERVE_EDGE_RADIUS = 3
const val SHADOW_FADE_RADIUS = 13
const val FOREGROUND_EDGE_FEATHER_ALPHA_SCALE = 0.18
const val FOREGROUND_EDGE_POLISH_RADIUS = 1
const val EDGE_POLISH_FOREGROUND_MIN_STRENGTH = 0.12
const val EDGE_POLISH_FOREGROUND_MAX_STRENGTH = 0.82
const val EDGE_POLISH_MONO_MIN_STRENGTH = 0.16
const val EDGE_POLISH_MONO_MAX_STRENGTH = 0.92
const val ADAPTIVE_SUBTRACT_TRANSPARENT_DISTANCE = 10.0
const val EDGE_BAND_RATIO = 0.035f
const val PLATE_BORDER_COVERAGE_THRESHOLD = 0.42
const val PLATE_MIN_REMOVED_RATIO = 0.05
const val PLATE_MIN_SAFE_REMAINING_COVERAGE = 0.01
const val PLATE_MIN_SAFE_KEEP_RATIO = 0.18
const val PLATE_MIN_SAFE_BOUNDS_RATIO = 0.28
const val SIZE_1X1 = 240
const val LOCAL_ICON_RENDER_SCALE = 3
const val MONOCHROME_MIN_COVERAGE = 0.004
const val ORIGINAL_CLEANUP_MIN_COVERAGE_DROP = 0.025
const val ORIGINAL_CLEANUP_MIN_REMAINING_COVERAGE = 0.012
const val ORIGINAL_CLEANUP_ALPHA_BOUNDS_THRESHOLD = 64
const val ORIGINAL_CLEANUP_MIN_BOUNDS_RATIO = 0.25
const val ADAPTIVE_DIRECT_MIN_COVERAGE = 0.02
const val ADAPTIVE_DIRECT_FULL_PLATE_COVERAGE = 0.72
const val ADAPTIVE_DIRECT_MIN_LOST_COVERAGE = 0.18
const val ADAPTIVE_DIRECT_PLATE_MIN_LUMA = 220
const val ADAPTIVE_DIRECT_PLATE_MAX_SATURATION = 0.16
const val ADAPTIVE_DIRECT_PLATE_MIN_RATIO = 0.24
const val ADAPTIVE_DIRECT_DETAIL_MAX_LUMA = 112
const val ADAPTIVE_DIRECT_DETAIL_MIN_RATIO = 0.01
const val ADAPTIVE_DIRECT_PLATE_BACKGROUND_DISTANCE = 36.0
const val ADAPTIVE_DIRECT_DETAIL_BACKGROUND_DISTANCE = 42.0
const val ADAPTIVE_BACKGROUND_DETAIL_DISTANCE = 52.0
const val ADAPTIVE_BACKGROUND_DETAIL_MIN_RATIO = 0.015
const val ADAPTIVE_BACKGROUND_DETAIL_MAX_RATIO = 0.55
const val ADAPTIVE_CLEAN_CORNER_RATIO = 0.18f
const val ADAPTIVE_CLEAN_SOLID_DISTANCE = 24.0

/**
 * 全部可调参数的不可变快照（预设/撤销/批量传输用）。
 *
 * 数字与布尔为强类型；模式（LocalSeparationMode / AdaptiveForegroundMode /
 * OriginalForegroundCleanupMode / GptImageMode / GptPromptPreset）存其 `.value`
 * 字符串，PreviewChoice 存 `.name`，这样本文件不依赖 MainActivity 的私有枚举，
 * 由 MainActivity 在应用时做枚举转换。
 */
data class TuningParams(
    val foregroundSubjectPercent: Int = DEFAULT_FOREGROUND_SUBJECT_PERCENT,
    val foregroundShadowLevel: Int = DEFAULT_FOREGROUND_SHADOW_LEVEL,
    val monochromeThemeScale: Float = DEFAULT_MONOCHROME_THEME_SCALE,
    val backgroundSeparationPercent: Int = DEFAULT_BACKGROUND_SEPARATION_PERCENT,
    val plateRemovalPercent: Int = DEFAULT_PLATE_REMOVAL_PERCENT,
    val shadowRemovalPercent: Int = DEFAULT_SHADOW_REMOVAL_PERCENT,
    val edgePolishPercent: Int = DEFAULT_EDGE_POLISH_PERCENT,
    val rmbgAlphaStrengthPercent: Int = DEFAULT_RMBG_ALPHA_STRENGTH_PERCENT,
    val rmbgEdgeFeatherPercent: Int = DEFAULT_RMBG_EDGE_FEATHER_PERCENT,
    val rmbgEdgeAdjustPercent: Int = DEFAULT_RMBG_EDGE_ADJUST_PERCENT,
    val rmbgWeakAlphaKeepPercent: Int = DEFAULT_RMBG_WEAK_ALPHA_KEEP_PERCENT,
    val liquidGlassEnabled: Boolean = true,
    val liquidGlassRadius: Int = DEFAULT_LIQUID_GLASS_RADIUS,
    val liquidGlassOuterWidth: Int = DEFAULT_LIQUID_GLASS_OUTER_WIDTH,
    val liquidGlassTopAlpha: Int = DEFAULT_LIQUID_GLASS_TOP_ALPHA,
    val liquidGlassBottomAlpha: Int = DEFAULT_LIQUID_GLASS_BOTTOM_ALPHA,
    val liquidGlassBackgroundMistAlpha: Int = DEFAULT_LIQUID_GLASS_BACKGROUND_MIST_ALPHA,
    val liquidGlassBottomDarkAlpha: Int = DEFAULT_LIQUID_GLASS_BOTTOM_DARK_ALPHA,
    val liquidGlassSubjectScalePercent: Int = DEFAULT_LIQUID_GLASS_SUBJECT_SCALE_PERCENT,
    val liquidGlassSubjectOutlineWidth: Int = DEFAULT_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
    val liquidGlassSubjectInnerOutlineWidth: Int = DEFAULT_LIQUID_GLASS_SUBJECT_INNER_OUTLINE_WIDTH,
    val liquidGlassSubjectShadowAlpha: Int = DEFAULT_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA,
    val liquidGlassSubjectOpacityPercent: Int = DEFAULT_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
    val adaptiveForegroundMode: String = "auto",
    val adaptiveDirectMaxCoveragePercent: Int = DEFAULT_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT,
    val adaptiveDirectMaxCoverageIncreasePercent: Int = DEFAULT_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
    val adaptiveMaskEdgeCoveragePercent: Int = DEFAULT_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT,
    val adaptiveMaskMinCoveragePercent: Int = DEFAULT_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT,
    val adaptiveCenterEpsilonPercent: Int = DEFAULT_ADAPTIVE_CENTER_EPSILON_PERCENT,
    val originalForegroundCleanupMode: String = "auto",
    // Local extraction workflow stages. Missing keys in older presets default to true.
    val localBackgroundSeparationEnabled: Boolean = true,
    val localAdaptiveSelectionEnabled: Boolean = true,
    val localCornerMaskCleanupEnabled: Boolean = true,
    val localAlphaEdgeColorRepairEnabled: Boolean = true,
    val localPlainBackgroundEstimationEnabled: Boolean = true,
    val localOriginalCleanupEnabled: Boolean = true,
    val localPlateCleanupEnabled: Boolean = true,
    val localPlateEdgeRepairEnabled: Boolean = true,
    val localPlateResidueCleanupEnabled: Boolean = true,
    val localShadowCleanupEnabled: Boolean = true,
    val localShadowEdgeRepairEnabled: Boolean = true,
    val localEdgeTrimEnabled: Boolean = true,
    val localComposedBackgroundEnabled: Boolean = true,
    val localTwoLayerCandidateEnabled: Boolean = true,
    val localComponentCandidatesEnabled: Boolean = true,
    val localTextSafeCandidateEnabled: Boolean = true,
    val localAutoSelectionEnabled: Boolean = true,
    val localEdgePolishEnabled: Boolean = true,
    val nightSubjectLightBackgroundEnabled: Boolean = false,
    val localSeparationMode: String = "auto",
    val gptImageMode: String = "responses",
    val gptPromptPreset: String = "stable_cutout",
    val gptCustomPrompt: String = "",
    val previewNormalLight: String = "Full",
    val previewNormalDark: String = "Full",
    val previewMonochromeLight: String = "Full",
    val previewMonochromeDark: String = "Full",
) {

    /** 与 debug HTTP 协议一致的扁平参数映射（键名即 currentDebugParamsJson 的键）。 */
    fun toParamMap(): Map<String, String> {
        val map = linkedMapOf<String, String>()
        map["foreground_subject_percent"] = foregroundSubjectPercent.toString()
        map["foreground_shadow_level"] = foregroundShadowLevel.toString()
        map["monochrome_theme_scale"] = (monochromeThemeScale * 100).roundToInt().toString()
        map["background_separation_percent"] = backgroundSeparationPercent.toString()
        map["plate_removal_percent"] = plateRemovalPercent.toString()
        map["shadow_removal_percent"] = shadowRemovalPercent.toString()
        map["edge_polish_percent"] = edgePolishPercent.toString()
        map["rmbg_alpha_strength_percent"] = rmbgAlphaStrengthPercent.toString()
        map["rmbg_edge_feather_percent"] = rmbgEdgeFeatherPercent.toString()
        map["rmbg_edge_adjust_percent"] = rmbgEdgeAdjustPercent.toString()
        map["rmbg_weak_alpha_keep_percent"] = rmbgWeakAlphaKeepPercent.toString()
        map["liquid_glass_enabled"] = liquidGlassEnabled.toString()
        map["liquid_glass_radius"] = liquidGlassRadius.toString()
        map["liquid_glass_outer_width"] = liquidGlassOuterWidth.toString()
        map["liquid_glass_top_alpha"] = liquidGlassTopAlpha.toString()
        map["liquid_glass_bottom_alpha"] = liquidGlassBottomAlpha.toString()
        map["liquid_glass_background_mist_alpha"] = liquidGlassBackgroundMistAlpha.toString()
        map["liquid_glass_bottom_dark_alpha"] = liquidGlassBottomDarkAlpha.toString()
        map["liquid_glass_subject_scale_percent"] = liquidGlassSubjectScalePercent.toString()
        map["liquid_glass_subject_outline_width"] = liquidGlassSubjectOutlineWidth.toString()
        map["liquid_glass_subject_inner_outline_width"] = liquidGlassSubjectInnerOutlineWidth.toString()
        map["liquid_glass_subject_shadow_alpha"] = liquidGlassSubjectShadowAlpha.toString()
        map["liquid_glass_subject_opacity_percent"] = liquidGlassSubjectOpacityPercent.toString()
        map["adaptive_foreground_mode"] = adaptiveForegroundMode
        map["adaptive_direct_max_coverage_percent"] = adaptiveDirectMaxCoveragePercent.toString()
        map["adaptive_direct_max_coverage_increase_percent"] = adaptiveDirectMaxCoverageIncreasePercent.toString()
        map["adaptive_mask_edge_coverage_percent"] = adaptiveMaskEdgeCoveragePercent.toString()
        map["adaptive_mask_min_coverage_percent"] = adaptiveMaskMinCoveragePercent.toString()
        map["adaptive_center_epsilon_percent"] = adaptiveCenterEpsilonPercent.toString()
        map["original_foreground_cleanup_mode"] = originalForegroundCleanupMode
        map["local_background_separation_enabled"] = localBackgroundSeparationEnabled.toString()
        map["local_adaptive_selection_enabled"] = localAdaptiveSelectionEnabled.toString()
        map["local_corner_mask_cleanup_enabled"] = localCornerMaskCleanupEnabled.toString()
        map["local_alpha_edge_color_repair_enabled"] = localAlphaEdgeColorRepairEnabled.toString()
        map["local_plain_background_estimation_enabled"] = localPlainBackgroundEstimationEnabled.toString()
        map["local_original_cleanup_enabled"] = localOriginalCleanupEnabled.toString()
        map["local_plate_cleanup_enabled"] = localPlateCleanupEnabled.toString()
        map["local_plate_edge_repair_enabled"] = localPlateEdgeRepairEnabled.toString()
        map["local_plate_residue_cleanup_enabled"] = localPlateResidueCleanupEnabled.toString()
        map["local_shadow_cleanup_enabled"] = localShadowCleanupEnabled.toString()
        map["local_shadow_edge_repair_enabled"] = localShadowEdgeRepairEnabled.toString()
        map["local_edge_trim_enabled"] = localEdgeTrimEnabled.toString()
        map["local_composed_background_enabled"] = localComposedBackgroundEnabled.toString()
        map["local_two_layer_candidate_enabled"] = localTwoLayerCandidateEnabled.toString()
        map["local_component_candidates_enabled"] = localComponentCandidatesEnabled.toString()
        map["local_text_safe_candidate_enabled"] = localTextSafeCandidateEnabled.toString()
        map["local_auto_selection_enabled"] = localAutoSelectionEnabled.toString()
        map["local_edge_polish_enabled"] = localEdgePolishEnabled.toString()
        map["night_subject_light_background_enabled"] = nightSubjectLightBackgroundEnabled.toString()
        map["local_separation_mode"] = localSeparationMode
        map["gpt_mode"] = gptImageMode
        map["gpt_prompt_preset"] = gptPromptPreset
        map["gpt_custom_prompt"] = gptCustomPrompt
        map["preview_selection_normal_light"] = previewNormalLight
        map["preview_selection_normal_dark"] = previewNormalDark
        map["preview_selection_monochrome_light"] = previewMonochromeLight
        map["preview_selection_monochrome_dark"] = previewMonochromeDark
        return map
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        toParamMap().forEach { (key, value) ->
            when (VALUE_TYPES[key]) {
                "int" -> json.put(key, value.toIntOrNull() ?: JSONObject.NULL)
                "float" -> json.put(key, value.toFloatOrNull() ?: JSONObject.NULL)
                "bool" -> json.put(key, value.toBooleanStrictOrNull() ?: JSONObject.NULL)
                else -> json.put(key, value)
            }
        }
        return json
    }

    fun sameAs(other: TuningParams): Boolean = toParamMap() == other.toParamMap()

    /** 生成“N 项参数不同”的简短中文摘要，用于状态提示。 */
    fun diffSummary(other: TuningParams): String {
        val mine = toParamMap()
        val theirs = other.toParamMap()
        val keys = mine.keys.filter { mine[it] != theirs[it] }
        if (keys.isEmpty()) {
            return "参数与当前一致"
        }
        val preview = keys.take(3).joinToString("、")
        return "${keys.size} 项参数不同（$preview${if (keys.size > 3) "…" else ""}）"
    }

    companion object {
        fun fromParamMap(map: Map<String, String>, defaults: TuningParams): TuningParams {
            fun intParam(key: String, min: Int, max: Int, fallback: Int): Int =
                map[key]?.toIntOrNull()?.coerceIn(min, max) ?: fallback

            fun boolParam(key: String, fallback: Boolean): Boolean =
                map[key]?.toBooleanStrictOrNull() ?: fallback

            fun strParam(key: String, fallback: String): String =
                map[key]?.takeIf { it.isNotBlank() } ?: fallback

            val monochromePercent = map["monochrome_theme_scale"]?.toFloatOrNull()?.let {
                if (it <= 2f) (it * 100f).roundToInt() else it.roundToInt()
            } ?: (defaults.monochromeThemeScale * 100).roundToInt()
            val monochromeThemeScale = (monochromePercent.coerceIn(
                MIN_MONOCHROME_THEME_SCALE_PERCENT,
                MAX_MONOCHROME_THEME_SCALE_PERCENT,
            ).toFloat() / 100f).coerceIn(
                MIN_MONOCHROME_THEME_SCALE,
                MAX_MONOCHROME_THEME_SCALE,
            )

            return TuningParams(
                foregroundSubjectPercent = intParam(
                    "foreground_subject_percent",
                    MIN_FOREGROUND_SUBJECT_PERCENT,
                    MAX_FOREGROUND_SUBJECT_PERCENT,
                    defaults.foregroundSubjectPercent,
                ),
                foregroundShadowLevel = intParam(
                    "foreground_shadow_level",
                    MIN_FOREGROUND_SHADOW_LEVEL,
                    MAX_FOREGROUND_SHADOW_LEVEL,
                    defaults.foregroundShadowLevel,
                ),
                monochromeThemeScale = monochromeThemeScale,
                backgroundSeparationPercent = intParam(
                    "background_separation_percent",
                    MIN_BACKGROUND_SEPARATION_PERCENT,
                    MAX_BACKGROUND_SEPARATION_PERCENT,
                    defaults.backgroundSeparationPercent,
                ),
                plateRemovalPercent = intParam(
                    "plate_removal_percent",
                    MIN_PLATE_REMOVAL_PERCENT,
                    MAX_PLATE_REMOVAL_PERCENT,
                    defaults.plateRemovalPercent,
                ),
                shadowRemovalPercent = intParam(
                    "shadow_removal_percent",
                    MIN_SHADOW_REMOVAL_PERCENT,
                    MAX_SHADOW_REMOVAL_PERCENT,
                    defaults.shadowRemovalPercent,
                ),
                edgePolishPercent = intParam(
                    "edge_polish_percent",
                    MIN_EDGE_POLISH_PERCENT,
                    MAX_EDGE_POLISH_PERCENT,
                    defaults.edgePolishPercent,
                ),
                rmbgAlphaStrengthPercent = intParam(
                    "rmbg_alpha_strength_percent",
                    MIN_RMBG_ALPHA_STRENGTH_PERCENT,
                    MAX_RMBG_ALPHA_STRENGTH_PERCENT,
                    defaults.rmbgAlphaStrengthPercent,
                ),
                rmbgEdgeFeatherPercent = intParam(
                    "rmbg_edge_feather_percent",
                    MIN_RMBG_EDGE_FEATHER_PERCENT,
                    MAX_RMBG_EDGE_FEATHER_PERCENT,
                    defaults.rmbgEdgeFeatherPercent,
                ),
                rmbgEdgeAdjustPercent = intParam(
                    "rmbg_edge_adjust_percent",
                    MIN_RMBG_EDGE_ADJUST_PERCENT,
                    MAX_RMBG_EDGE_ADJUST_PERCENT,
                    defaults.rmbgEdgeAdjustPercent,
                ),
                rmbgWeakAlphaKeepPercent = intParam(
                    "rmbg_weak_alpha_keep_percent",
                    MIN_RMBG_WEAK_ALPHA_KEEP_PERCENT,
                    MAX_RMBG_WEAK_ALPHA_KEEP_PERCENT,
                    defaults.rmbgWeakAlphaKeepPercent,
                ),
                liquidGlassEnabled = boolParam("liquid_glass_enabled", defaults.liquidGlassEnabled),
                liquidGlassRadius = intParam(
                    "liquid_glass_radius",
                    MIN_LIQUID_GLASS_RADIUS,
                    MAX_LIQUID_GLASS_RADIUS,
                    defaults.liquidGlassRadius,
                ),
                liquidGlassOuterWidth = intParam(
                    "liquid_glass_outer_width",
                    MIN_LIQUID_GLASS_OUTER_WIDTH,
                    MAX_LIQUID_GLASS_OUTER_WIDTH,
                    defaults.liquidGlassOuterWidth,
                ),
                liquidGlassTopAlpha = intParam(
                    "liquid_glass_top_alpha",
                    MIN_LIQUID_GLASS_ALPHA,
                    MAX_LIQUID_GLASS_ALPHA,
                    defaults.liquidGlassTopAlpha,
                ),
                liquidGlassBottomAlpha = intParam(
                    "liquid_glass_bottom_alpha",
                    MIN_LIQUID_GLASS_ALPHA,
                    MAX_LIQUID_GLASS_ALPHA,
                    defaults.liquidGlassBottomAlpha,
                ),
                liquidGlassBackgroundMistAlpha = intParam(
                    "liquid_glass_background_mist_alpha",
                    MIN_LIQUID_GLASS_MIST_ALPHA,
                    MAX_LIQUID_GLASS_MIST_ALPHA,
                    defaults.liquidGlassBackgroundMistAlpha,
                ),
                liquidGlassBottomDarkAlpha = intParam(
                    "liquid_glass_bottom_dark_alpha",
                    MIN_LIQUID_GLASS_BOTTOM_DARK_ALPHA,
                    MAX_LIQUID_GLASS_BOTTOM_DARK_ALPHA,
                    defaults.liquidGlassBottomDarkAlpha,
                ),
                liquidGlassSubjectScalePercent = intParam(
                    "liquid_glass_subject_scale_percent",
                    MIN_LIQUID_GLASS_SUBJECT_SCALE_PERCENT,
                    MAX_LIQUID_GLASS_SUBJECT_SCALE_PERCENT,
                    defaults.liquidGlassSubjectScalePercent,
                ),
                liquidGlassSubjectOutlineWidth = intParam(
                    "liquid_glass_subject_outline_width",
                    MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                    MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                    defaults.liquidGlassSubjectOutlineWidth,
                ),
                liquidGlassSubjectInnerOutlineWidth = intParam(
                    "liquid_glass_subject_inner_outline_width",
                    MIN_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                    MAX_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH,
                    defaults.liquidGlassSubjectInnerOutlineWidth,
                ),
                liquidGlassSubjectShadowAlpha = intParam(
                    "liquid_glass_subject_shadow_alpha",
                    MIN_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA,
                    MAX_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA,
                    defaults.liquidGlassSubjectShadowAlpha,
                ),
                liquidGlassSubjectOpacityPercent = intParam(
                    "liquid_glass_subject_opacity_percent",
                    MIN_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
                    MAX_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT,
                    defaults.liquidGlassSubjectOpacityPercent,
                ),
                adaptiveForegroundMode = strParam("adaptive_foreground_mode", defaults.adaptiveForegroundMode),
                adaptiveDirectMaxCoveragePercent = intParam(
                    "adaptive_direct_max_coverage_percent",
                    MIN_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT,
                    MAX_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT,
                    defaults.adaptiveDirectMaxCoveragePercent,
                ),
                adaptiveDirectMaxCoverageIncreasePercent = intParam(
                    "adaptive_direct_max_coverage_increase_percent",
                    MIN_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
                    MAX_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT,
                    defaults.adaptiveDirectMaxCoverageIncreasePercent,
                ),
                adaptiveMaskEdgeCoveragePercent = intParam(
                    "adaptive_mask_edge_coverage_percent",
                    MIN_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT,
                    MAX_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT,
                    defaults.adaptiveMaskEdgeCoveragePercent,
                ),
                adaptiveMaskMinCoveragePercent = intParam(
                    "adaptive_mask_min_coverage_percent",
                    MIN_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT,
                    MAX_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT,
                    defaults.adaptiveMaskMinCoveragePercent,
                ),
                adaptiveCenterEpsilonPercent = intParam(
                    "adaptive_center_epsilon_percent",
                    MIN_ADAPTIVE_CENTER_EPSILON_PERCENT,
                    MAX_ADAPTIVE_CENTER_EPSILON_PERCENT,
                    defaults.adaptiveCenterEpsilonPercent,
                ),
                originalForegroundCleanupMode = strParam(
                    "original_foreground_cleanup_mode",
                    defaults.originalForegroundCleanupMode,
                ),
                localBackgroundSeparationEnabled = boolParam(
                    "local_background_separation_enabled",
                    defaults.localBackgroundSeparationEnabled,
                ),
                localAdaptiveSelectionEnabled = boolParam(
                    "local_adaptive_selection_enabled",
                    defaults.localAdaptiveSelectionEnabled,
                ),
                localCornerMaskCleanupEnabled = boolParam(
                    "local_corner_mask_cleanup_enabled",
                    defaults.localCornerMaskCleanupEnabled,
                ),
                localAlphaEdgeColorRepairEnabled = boolParam(
                    "local_alpha_edge_color_repair_enabled",
                    defaults.localAlphaEdgeColorRepairEnabled,
                ),
                localPlainBackgroundEstimationEnabled = boolParam(
                    "local_plain_background_estimation_enabled",
                    defaults.localPlainBackgroundEstimationEnabled,
                ),
                localOriginalCleanupEnabled = boolParam(
                    "local_original_cleanup_enabled",
                    defaults.localOriginalCleanupEnabled,
                ),
                localPlateCleanupEnabled = boolParam(
                    "local_plate_cleanup_enabled",
                    defaults.localPlateCleanupEnabled,
                ),
                localPlateEdgeRepairEnabled = boolParam(
                    "local_plate_edge_repair_enabled",
                    defaults.localPlateEdgeRepairEnabled,
                ),
                localPlateResidueCleanupEnabled = boolParam(
                    "local_plate_residue_cleanup_enabled",
                    defaults.localPlateResidueCleanupEnabled,
                ),
                localShadowCleanupEnabled = boolParam(
                    "local_shadow_cleanup_enabled",
                    defaults.localShadowCleanupEnabled,
                ),
                localShadowEdgeRepairEnabled = boolParam(
                    "local_shadow_edge_repair_enabled",
                    defaults.localShadowEdgeRepairEnabled,
                ),
                localEdgeTrimEnabled = boolParam(
                    "local_edge_trim_enabled",
                    defaults.localEdgeTrimEnabled,
                ),
                localComposedBackgroundEnabled = boolParam(
                    "local_composed_background_enabled",
                    defaults.localComposedBackgroundEnabled,
                ),
                localTwoLayerCandidateEnabled = boolParam(
                    "local_two_layer_candidate_enabled",
                    defaults.localTwoLayerCandidateEnabled,
                ),
                localComponentCandidatesEnabled = boolParam(
                    "local_component_candidates_enabled",
                    defaults.localComponentCandidatesEnabled,
                ),
                localTextSafeCandidateEnabled = boolParam(
                    "local_text_safe_candidate_enabled",
                    defaults.localTextSafeCandidateEnabled,
                ),
                localAutoSelectionEnabled = boolParam(
                    "local_auto_selection_enabled",
                    defaults.localAutoSelectionEnabled,
                ),
                localEdgePolishEnabled = boolParam(
                    "local_edge_polish_enabled",
                    defaults.localEdgePolishEnabled,
                ),
                nightSubjectLightBackgroundEnabled = boolParam(
                    "night_subject_light_background_enabled",
                    defaults.nightSubjectLightBackgroundEnabled,
                ),
                localSeparationMode = strParam("local_separation_mode", defaults.localSeparationMode),
                gptImageMode = strParam("gpt_mode", defaults.gptImageMode),
                gptPromptPreset = strParam("gpt_prompt_preset", defaults.gptPromptPreset),
                gptCustomPrompt = map["gpt_custom_prompt"] ?: defaults.gptCustomPrompt,
                previewNormalLight = strParam("preview_selection_normal_light", defaults.previewNormalLight),
                previewNormalDark = strParam("preview_selection_normal_dark", defaults.previewNormalDark),
                previewMonochromeLight = strParam(
                    "preview_selection_monochrome_light",
                    defaults.previewMonochromeLight,
                ),
                previewMonochromeDark = strParam(
                    "preview_selection_monochrome_dark",
                    defaults.previewMonochromeDark,
                ),
            )
        }

        fun fromJson(json: JSONObject, defaults: TuningParams): TuningParams? {
            return runCatching {
                val map = HashMap<String, String>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    if (!json.isNull(key)) {
                        map[key] = json.optString(key)
                    }
                }
                fromParamMap(map, defaults)
            }.getOrNull()
        }

        /** 参数键 → JSON 值类型（决定 toJson 输出与文本编辑器语法高亮）。 */
        val VALUE_TYPES: Map<String, String> = mapOf(
            "foreground_subject_percent" to "int",
            "foreground_shadow_level" to "int",
            "monochrome_theme_scale" to "float",
            "background_separation_percent" to "int",
            "plate_removal_percent" to "int",
            "shadow_removal_percent" to "int",
            "edge_polish_percent" to "int",
            "rmbg_alpha_strength_percent" to "int",
            "rmbg_edge_feather_percent" to "int",
            "rmbg_edge_adjust_percent" to "int",
            "rmbg_weak_alpha_keep_percent" to "int",
            "liquid_glass_enabled" to "bool",
            "liquid_glass_radius" to "int",
            "liquid_glass_outer_width" to "int",
            "liquid_glass_top_alpha" to "int",
            "liquid_glass_bottom_alpha" to "int",
            "liquid_glass_background_mist_alpha" to "int",
            "liquid_glass_bottom_dark_alpha" to "int",
            "liquid_glass_subject_scale_percent" to "int",
            "liquid_glass_subject_outline_width" to "int",
            "liquid_glass_subject_inner_outline_width" to "int",
            "liquid_glass_subject_shadow_alpha" to "int",
            "liquid_glass_subject_opacity_percent" to "int",
            "adaptive_foreground_mode" to "string",
            "adaptive_direct_max_coverage_percent" to "int",
            "adaptive_direct_max_coverage_increase_percent" to "int",
            "adaptive_mask_edge_coverage_percent" to "int",
            "adaptive_mask_min_coverage_percent" to "int",
            "adaptive_center_epsilon_percent" to "int",
            "original_foreground_cleanup_mode" to "string",
            "local_background_separation_enabled" to "bool",
            "local_adaptive_selection_enabled" to "bool",
            "local_corner_mask_cleanup_enabled" to "bool",
            "local_alpha_edge_color_repair_enabled" to "bool",
            "local_plain_background_estimation_enabled" to "bool",
            "local_original_cleanup_enabled" to "bool",
            "local_plate_cleanup_enabled" to "bool",
            "local_plate_edge_repair_enabled" to "bool",
            "local_plate_residue_cleanup_enabled" to "bool",
            "local_shadow_cleanup_enabled" to "bool",
            "local_shadow_edge_repair_enabled" to "bool",
            "local_edge_trim_enabled" to "bool",
            "local_composed_background_enabled" to "bool",
            "local_two_layer_candidate_enabled" to "bool",
            "local_component_candidates_enabled" to "bool",
            "local_text_safe_candidate_enabled" to "bool",
            "local_auto_selection_enabled" to "bool",
            "local_edge_polish_enabled" to "bool",
            "night_subject_light_background_enabled" to "bool",
            "local_separation_mode" to "string",
            "gpt_mode" to "string",
            "gpt_prompt_preset" to "string",
            "gpt_custom_prompt" to "string",
            "preview_selection_normal_light" to "string",
            "preview_selection_normal_dark" to "string",
            "preview_selection_monochrome_light" to "string",
            "preview_selection_monochrome_dark" to "string",
        )
    }
}

/**
 * Immutable local extraction workflow snapshot. It is created once per generation/rebuild so
 * background workers cannot observe a half-updated Compose state while a preview is rebuilding.
 */
data class LocalPipelineConfig(
    val backgroundSeparationEnabled: Boolean = true,
    val adaptiveSelectionEnabled: Boolean = true,
    val cornerMaskCleanupEnabled: Boolean = true,
    val alphaEdgeColorRepairEnabled: Boolean = true,
    val plainBackgroundEstimationEnabled: Boolean = true,
    val originalCleanupEnabled: Boolean = true,
    val plateCleanupEnabled: Boolean = true,
    val plateEdgeRepairEnabled: Boolean = true,
    val plateResidueCleanupEnabled: Boolean = true,
    val shadowCleanupEnabled: Boolean = true,
    val shadowEdgeRepairEnabled: Boolean = true,
    val edgeTrimEnabled: Boolean = true,
    val composedBackgroundEnabled: Boolean = true,
    val twoLayerCandidateEnabled: Boolean = true,
    val componentCandidatesEnabled: Boolean = true,
    val textSafeCandidateEnabled: Boolean = true,
    val autoSelectionEnabled: Boolean = true,
    val edgePolishEnabled: Boolean = true,
) {
    companion object {
        fun from(params: TuningParams): LocalPipelineConfig = LocalPipelineConfig(
            backgroundSeparationEnabled = params.localBackgroundSeparationEnabled,
            adaptiveSelectionEnabled = params.localAdaptiveSelectionEnabled,
            cornerMaskCleanupEnabled = params.localCornerMaskCleanupEnabled,
            alphaEdgeColorRepairEnabled = params.localAlphaEdgeColorRepairEnabled,
            plainBackgroundEstimationEnabled = params.localPlainBackgroundEstimationEnabled,
            originalCleanupEnabled = params.localOriginalCleanupEnabled,
            plateCleanupEnabled = params.localPlateCleanupEnabled,
            plateEdgeRepairEnabled = params.localPlateEdgeRepairEnabled,
            plateResidueCleanupEnabled = params.localPlateResidueCleanupEnabled,
            shadowCleanupEnabled = params.localShadowCleanupEnabled,
            shadowEdgeRepairEnabled = params.localShadowEdgeRepairEnabled,
            edgeTrimEnabled = params.localEdgeTrimEnabled,
            composedBackgroundEnabled = params.localComposedBackgroundEnabled,
            twoLayerCandidateEnabled = params.localTwoLayerCandidateEnabled,
            componentCandidatesEnabled = params.localComponentCandidatesEnabled,
            textSafeCandidateEnabled = params.localTextSafeCandidateEnabled,
            autoSelectionEnabled = params.localAutoSelectionEnabled,
            edgePolishEnabled = params.localEdgePolishEnabled,
        )
    }
}
