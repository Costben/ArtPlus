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
const val AUTO_EDGE_TOUCH_MARGIN_PX = 1
const val AUTO_EDGE_TOUCH_COUNT_LIMIT = 2
const val AUTO_COVERAGE_CHANGE_THRESHOLD = 0.012
const val TWO_LAYER_PLATE_BACKGROUND_DISTANCE = 35.0
const val TWO_LAYER_SUBJECT_BACKGROUND_DISTANCE = 72.0
const val TWO_LAYER_MIN_PLATE_COVERAGE = 0.12
const val TWO_LAYER_MAX_PLATE_COVERAGE = 0.62
const val TWO_LAYER_SUBJECT_PLATE_DILATE_RADIUS = 7
const val TWO_LAYER_MIN_SUBJECT_PIXELS = 20
const val TWO_LAYER_MIN_SUBJECT_COVERAGE = 0.008
const val TWO_LAYER_MIN_MANUAL_SUBJECT_COVERAGE = 0.010
const val TWO_LAYER_MAX_SUBJECT_TO_PLATE_RATIO = 0.45
const val TWO_LAYER_MIN_SUBJECT_FILL_RATIO = 0.08
const val TWO_LAYER_MAX_SUBJECT_BOUNDS_TO_PLATE_RATIO = 0.70
const val TWO_LAYER_SUBJECT_CLOSE_RADIUS = 2
const val TWO_LAYER_BACKGROUND_FILL_RADIUS = 1
const val COMPOSED_BACKGROUND_SUBJECT_ALPHA_THRESHOLD = 24
const val COMPOSED_BACKGROUND_FILL_RADIUS = 2
const val TWO_LAYER_EDGE_SMOOTH_STRENGTH = 0.32
const val TWO_LAYER_EDGE_SMOOTH_RADIUS = 1
const val TWO_LAYER_EDGE_GROW_STRENGTH = 0.22
const val TWO_LAYER_DOMINANT_MIN_SATURATION = 0.18
const val TWO_LAYER_DOMINANT_MAX_LUMA = 240
const val TWO_LAYER_MANUAL_MAX_PLATE_STD = 70.0
const val TWO_LAYER_AUTO_MAX_PLATE_STD = 45.0
const val TWO_LAYER_AUTO_MIN_PLATE_LUMA = 0.35
const val TWO_LAYER_AUTO_MAX_PLATE_COVERAGE = 0.62
const val TWO_LAYER_AUTO_MAX_SUBJECT_TO_PLATE_RATIO = 0.35

/**
 * 夜间前景（night）阈值（imaging/ 管线函数与 MainActivity 共用）。
 *
 * 从 MainActivity companion object 迁移而来（P1.2-c 拆分）：原为 private，
 * 现为顶层公开常量，数值与类型未变。同包直接引用，Activity 内原有调用点零改动。
 * NIGHT_APP_WHITE 是 `private val`（Color.rgb() 不能 const），提升用顶层 `val`。
 */
val NIGHT_APP_WHITE = android.graphics.Color.rgb(247, 248, 250)
const val NIGHT_VISIBLE_ALPHA_THRESHOLD = 8
const val NIGHT_WHITE_LUMA_THRESHOLD = 235
const val NIGHT_WHITE_MAX_SATURATION = 0.10
const val NIGHT_DARK_LUMA_THRESHOLD = 96
const val NIGHT_WHITE_RATIO_THRESHOLD = 0.55
const val NIGHT_DARK_RATIO_THRESHOLD = 0.55
const val NIGHT_DARK_MAX_WHITE_RATIO = 0.20
const val NIGHT_DARK_COLOR_IGNORE_LUMA_THRESHOLD = 112
const val NIGHT_COLOR_SATURATION_THRESHOLD = 0.28
const val NIGHT_COLOR_RATIO_MAX = 0.06
const val NIGHT_DARK_COLOR_RATIO_MAX = 0.16
const val NIGHT_COLORED_BACKGROUND_WHITE_RATIO_THRESHOLD = 0.34
const val NIGHT_COLORED_BACKGROUND_MIN_SATURATION = 0.18
const val NIGHT_COLORED_BACKGROUND_DARK_RATIO_MAX = 0.22
const val NIGHT_COLORED_BACKGROUND_COLOR_RATIO_MAX = 0.24
const val NIGHT_FLAT_LIGHT_ALPHA_THRESHOLD = 96
const val NIGHT_FLAT_LIGHT_MIN_PIXELS = 48
const val NIGHT_FLAT_LIGHT_LUMA_THRESHOLD = 220
const val NIGHT_FLAT_LIGHT_MAX_SATURATION = 0.16
const val NIGHT_FLAT_LIGHT_DARK_RATIO_MAX = 0.10
const val NIGHT_FLAT_LIGHT_COLOR_RATIO_MAX = 0.10
const val NIGHT_FLAT_LIGHT_RATIO_MIN = 0.72
const val NIGHT_FLAT_VERY_LIGHT_RATIO_MIN = 0.36
const val NIGHT_FLAT_PALE_RATIO_MIN = 0.88
const val NIGHT_FLAT_PALE_VERY_LIGHT_RATIO_MIN = 0.22
const val NIGHT_FLAT_LIGHT_SATURATED_THRESHOLD = 0.20
const val NIGHT_FLAT_LIGHT_SATURATED_RATIO_MAX = 0.08
const val NIGHT_FLAT_LIGHT_LUMA_RANGE_MAX = 36
const val NIGHT_BACKGROUND_DARK_LUMA_THRESHOLD = 76
const val NIGHT_BACKGROUND_COLORED_LUMA_THRESHOLD = 72
const val NIGHT_BACKGROUND_LIGHT_LUMA_THRESHOLD = 210
const val NIGHT_BACKGROUND_LIGHT_MAX_SATURATION = 0.16
const val NIGHT_DIRECT_WHITE_LUMA_THRESHOLD = 232
const val NIGHT_DIRECT_WHITE_MAX_SATURATION = 0.08
const val NIGHT_BACKGROUND_WHITE_BLEND = 0.18
const val NIGHT_EDGE_WHITE_LUMA_THRESHOLD = 218
const val NIGHT_EDGE_WHITE_MAX_SATURATION = 0.18
const val NIGHT_SOFT_EDGE_WHITE_LUMA_THRESHOLD = 142
const val NIGHT_SOFT_EDGE_WHITE_MAX_SATURATION = 0.22
const val NIGHT_EDGE_FEATHER_BLEND = 0.30
const val NIGHT_EDGE_SMOOTH_STRENGTH = 0.42
const val NIGHT_EDGE_CONTRAST_RADIUS = 2
const val NIGHT_EDGE_COLORED_NEIGHBOR_SATURATION = 0.18
const val NIGHT_EDGE_DARK_NEIGHBOR_LUMA = 116
const val NIGHT_SUPPORT_MIN_LUMA = 24
const val NIGHT_SUPPORT_MAX_LUMA = 150
const val NIGHT_SUPPORT_MAX_SATURATION = 0.22
const val NIGHT_SUPPORT_PRESERVE_SATURATION = 0.18
const val NIGHT_SUPPORT_PRESERVE_LUMA = 172
const val NIGHT_DEFAULT_BOOST_MAX_BLEND = 0.16
const val NIGHT_FILL_BACKGROUND_MAX_BLEND = 0.30

/**
 * 应用数据层（data/ 与 MainActivity 共用）。
 *
 * 从 MainActivity companion object 迁移而来（P3 拆分）：原为 private，
 * 现为顶层公开常量，数值与类型未变。同包直接引用，Activity 内原有调用点零改动。
 */
const val ICON_CACHE_SIZE = 96
const val PRELOAD_ICON_COUNT = 64
const val ROOT_UXICONS_DIR = "/data/oplus/uxicons"
const val ROOT_SCAN_TIMEOUT_MS = 8_000L
const val PREF_GENERATED_PACKAGE_NAMES = "generated_package_names"
const val PREF_GENERATED_PACKAGE_NAMES_UPDATED_AT = "generated_package_names_updated_at"
const val ARTPLUS_ICON_REFRESH_TIMEOUT_MS = 12_000L
const val COLOROS_UX_ICON_CONFIG_KEY = "key_ux_icon_config"
const val COLOROS_DEFAULT_ICON_THEME = 2
const val COLOROS_INSPIRATION_ICON_THEME = 3
const val COLOROS_ARTPLUS_ON = 1
const val COLOROS_UXICON_THEME_SHIFT = 4
const val COLOROS_UXICON_ARTPLUS_SHIFT = 8
const val COLOROS_UXICON_THEME_MASK = 0x0fL shl COLOROS_UXICON_THEME_SHIFT
const val COLOROS_UXICON_ARTPLUS_MASK = 0x07L shl COLOROS_UXICON_ARTPLUS_SHIFT
const val FALLBACK_ARTPLUS_INSPIRATION_UXICON_CONFIG = 2314313028685793584L

/**
 * GPT 管线（pipeline/ 与 MainActivity 共用）。
 *
 * 从 MainActivity companion object 迁移而来（P4 拆分）：原为 private，
 * 现为顶层公开常量，数值与类型未变。同包直接引用，Activity 内原有调用点零改动。
 */
const val GPT_RESPONSE_MODEL = "gpt-5.4-mini"
const val GPT_IMAGE_MODEL = "gpt-image-2"
const val GPT_IMAGE_SIZE = "1024x1024"
const val GPT_IMAGE_QUALITY = "low"
const val GPT_CONNECT_TIMEOUT_MS = 30_000
const val GPT_READ_TIMEOUT_MS = 360_000
const val GPT_SOURCE_SIZE = 1024

/**
 * Debug 服务器 + RMBG 形态学（system/pipeline 与 MainActivity 共用）。
 *
 * 从 MainActivity companion object 迁移而来（P4 拆分）：原为 private，
 * 现为顶层公开常量，数值与类型未变。同包直接引用，Activity 内原有调用点零改动。
 */
const val DEBUG_HTTP_PORT = 3964
const val DEBUG_HTTP_ABSTRACT_NAME = "artplus-debug-http"
const val DEBUG_HTTP_READ_TIMEOUT_MS = 4_000
const val DEBUG_HTTP_MAX_HEADER_BYTES = 16 * 1024
const val DEBUG_HTTP_MAX_BODY_BYTES = 64 * 1024
const val DEBUG_HTTP_TOKEN_HEADER = "X-ArtPlus-Debug-Token"
const val DEBUG_HTTP_TOKEN_PARAM = "token"

/**
 * Debug 生成 Intent extras（Epic v2 Phase 1 Slice 1.6 从 MainActivity 迁移而来）。
 *
 * 原为 MainActivity companion object 内 private const，现为顶层公开常量，
 * 数值与类型未变。同包直接引用，Activity 内原有调用点零改动。
 */
const val EXTRA_DEBUG_GENERATE_PACKAGE = "dev.artplus.mobile.DEBUG_GENERATE_PACKAGE"
const val EXTRA_DEBUG_GENERATE_USE_GPT = "dev.artplus.mobile.DEBUG_GENERATE_USE_GPT"
const val EXTRA_DEBUG_GENERATE_INSTALL_ROOT = "dev.artplus.mobile.DEBUG_GENERATE_INSTALL_ROOT"
const val EXTRA_DEBUG_GENERATE_MODE = "dev.artplus.mobile.DEBUG_GENERATE_MODE"
const val EXTRA_DEBUG_GENERATE_ROOT_WRITE_MODE = "dev.artplus.mobile.DEBUG_GENERATE_ROOT_WRITE_MODE"
const val EXTRA_DEBUG_GENERATE_TOKEN = "dev.artplus.mobile.DEBUG_GENERATE_TOKEN"
const val RMBG_EDGE_FEATHER_MIN_ALPHA_DELTA = 12

/**
 * RMBG 组件/推理常量（Epic v2 Phase 1 Slice 1.3 从 MainActivity 迁移而来）。
 *
 * 原为 MainActivity companion object 内 private const/val，现为顶层公开常量，
 * 数值与类型未变。同包直接引用，Activity 内原有调用点零改动。
 */
const val RMBG_COMPONENT_DIR = "rmbg_component"
const val RMBG_MODEL_NAME = "bria-rmbg.onnx"
const val DEFAULT_RMBG_INPUT_SIZE = 1024
const val RMBG_MIN_MODEL_BYTES = 100_000_000L
const val RMBG_MIN_COMPONENT_ZIP_BYTES = 100_000_000L
const val RMBG_MAX_DOWNLOAD_BYTES = 2L * 1024L * 1024L * 1024L
const val RMBG_MAX_COMPONENT_ZIP_ENTRIES = 128
const val RMBG_MAX_COMPONENT_ZIP_UNPACK_BYTES = 800L * 1024L * 1024L
const val RMBG_DOWNLOAD_CONNECT_TIMEOUT_MS = 30_000
const val RMBG_DOWNLOAD_READ_TIMEOUT_MS = 1_800_000
const val RMBG_MODEL_URL_ORIGINAL =
    "https://modelscope.cn/models/AI-ModelScope/RMBG-2.0/resolve/master/onnx/model.onnx"
const val RMBG_MODEL_URL_QUANTIZED =
    "https://modelscope.cn/models/AI-ModelScope/RMBG-2.0/resolve/master/onnx/model_quantized.onnx"
const val RMBG_MODEL_URL_UINT8 =
    "https://modelscope.cn/models/AI-ModelScope/RMBG-2.0/resolve/master/onnx/model_uint8.onnx"
const val RMBG_MODEL_URL_INT8 =
    "https://modelscope.cn/models/AI-ModelScope/RMBG-2.0/resolve/master/onnx/model_int8.onnx"
const val RMBG_MODEL_URL_FP16 =
    "https://modelscope.cn/models/AI-ModelScope/RMBG-2.0/resolve/master/onnx/model_fp16.onnx"
const val RMBG_MODEL_URL_Q4 =
    "https://modelscope.cn/models/AI-ModelScope/RMBG-2.0/resolve/master/onnx/model_q4.onnx"
const val RMBG_MODEL_URL_BNB4 =
    "https://modelscope.cn/models/AI-ModelScope/RMBG-2.0/resolve/master/onnx/model_bnb4.onnx"
const val RMBG_MODEL_URL_Q4F16 =
    "https://modelscope.cn/models/AI-ModelScope/RMBG-2.0/resolve/master/onnx/model_q4f16.onnx"
val DEFAULT_RMBG_COMPONENT_URL =
    RMBG_MODEL_URL_QUANTIZED.ifBlank { RMBG_MODEL_URL_ORIGINAL }
internal val RMBG_MODEL_PRESET_CUSTOM = RmbgModelPreset(
    id = "custom",
    label = "自定义 URL",
    summary = "手动填写模型或组件 ZIP 地址",
    url = "",
)
internal val RMBG_MODEL_PRESETS = listOf(
    RmbgModelPreset(
        id = "rmbg20_quantized",
        label = "量化推荐",
        summary = "model_quantized.onnx · 349MB · 默认候选",
        url = RMBG_MODEL_URL_QUANTIZED,
    ),
    RmbgModelPreset(
        id = "rmbg20_uint8",
        label = "UINT8",
        summary = "model_uint8.onnx · 349MB · 备选",
        url = RMBG_MODEL_URL_UINT8,
    ),
    RmbgModelPreset(
        id = "rmbg20_int8",
        label = "INT8",
        summary = "model_int8.onnx · 349MB · 备选",
        url = RMBG_MODEL_URL_INT8,
    ),
    RmbgModelPreset(
        id = "rmbg20_original",
        label = "原版",
        summary = "model.onnx · 官方 ONNX",
        url = RMBG_MODEL_URL_ORIGINAL,
    ),
    RmbgModelPreset(
        id = "rmbg20_fp16",
        label = "FP16",
        summary = "model_fp16.onnx · 490MB · 基线",
        url = RMBG_MODEL_URL_FP16,
    ),
    RmbgModelPreset(
        id = "rmbg20_q4",
        label = "Q4",
        summary = "model_q4.onnx · 350MB",
        url = RMBG_MODEL_URL_Q4,
    ),
    RmbgModelPreset(
        id = "rmbg20_bnb4",
        label = "BNB4",
        summary = "model_bnb4.onnx · 339MB",
        url = RMBG_MODEL_URL_BNB4,
    ),
    RmbgModelPreset(
        id = "rmbg20_q4f16",
        label = "Q4F16",
        summary = "model_q4f16.onnx · 223MB",
        url = RMBG_MODEL_URL_Q4F16,
    ),
    RMBG_MODEL_PRESET_CUSTOM,
)
val RMBG_NORMALIZE_MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)
val RMBG_NORMALIZE_STD = floatArrayOf(0.229f, 0.224f, 0.225f)
const val RMBG_MIN_MANUAL_COVERAGE = 0.02
const val RMBG_MAX_MANUAL_COVERAGE = 0.62
const val RMBG_MIN_AUTO_COVERAGE = 0.02
const val RMBG_MAX_AUTO_COVERAGE = 0.34
const val RMBG_EDGE_ADJUST_MAX_RADIUS = 3
const val RMBG_WEAK_ALPHA_MAX_CUT = 72

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

// ui/components 用行出血与卡片内边距（原 MainActivity companion，slice1 提升）。
const val CHOICE_ROW_HORIZONTAL_BLEED_DP = 16
const val SECTION_CARD_VERTICAL_PADDING_DP = 12

// ui/pages 用常量（原 MainActivity companion，slice3 提升）。
const val PREVIEW_LIVE_ASSET_DEBOUNCE_MS = 70L
const val PREVIEW_LABEL_HEIGHT_DP = 16
const val BACK_GESTURE_COMMIT_PROGRESS = 0.28f
const val BACK_GESTURE_PAGE_TRANSLATION_RATIO = 1.0f
const val GITHUB_REPO_URL = "https://github.com/Costben/ArtPlus"
const val GITHUB_LICENSE_URL = "https://github.com/Costben/ArtPlus/blob/main/LICENSE"
const val MIT_LICENSE_TEXT =
"MIT License\n\n" +
    "Copyright (c) 2026 Costben\n\n" +
    "Permission is hereby granted, free of charge, to any person obtaining a copy\n" +
    "of this software and associated documentation files (the \"Software\"), to deal\n" +
    "in the Software without restriction, including without limitation the rights\n" +
    "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" +
    "copies of the Software, and to permit persons to whom the Software is\n" +
    "furnished to do so, subject to the following conditions:\n\n" +
    "The above copyright notice and this permission notice shall be included in all\n" +
    "copies or substantial portions of the Software.\n\n" +
    "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
    "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
    "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
    "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
    "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
    "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n" +
    "SOFTWARE."

// 预览选择 prefs 键（原 MainActivity companion，slice5 提升）。
const val PREF_PREVIEW_SELECTION_NORMAL_LIGHT = "preview_selection_normal_light"
const val PREF_PREVIEW_SELECTION_NORMAL_DARK = "preview_selection_normal_dark"
const val PREF_PREVIEW_SELECTION_MONOCHROME_LIGHT = "preview_selection_monochrome_light"
const val PREF_PREVIEW_SELECTION_MONOCHROME_DARK = "preview_selection_monochrome_dark"

// ART+ 生成包尺寸/原图备份名（原 MainActivity companion，Slice 1.4 提升）。
// SIZE_1X1 / GPT_SOURCE_SIZE 已在顶层，-shape 同包直接引用；intArray 不能 const，用顶层 val。
const val SIZE_2X2 = 704
val SIZE_1X2 = intArrayOf(240, 820)
val SIZE_2X1 = intArrayOf(820, 240)
const val FOREGROUND_ORIGINAL_BACKUP_NAME = "recfg_original_artplus.png"

// 主页预览尺寸/圆角/防抖（原 MainActivity companion，Slice 2.2 提升）。
const val PREVIEW_OUTPUT_DEBOUNCE_MS = 140L
const val PREVIEW_REBUILD_DEBOUNCE_MS = 180L
const val DEFAULT_PREVIEW_ICON_SIZE_DP = 70
const val MIN_PREVIEW_ICON_SIZE_DP = 42
const val MAX_PREVIEW_ICON_SIZE_DP = 96
const val DEFAULT_PREVIEW_CORNER_RADIUS_DP = 20
const val MIN_PREVIEW_CORNER_RADIUS_DP = 0
const val MAX_PREVIEW_CORNER_RADIUS_DP = 36

// 设置页批量预览数量（原 MainActivity companion 私有别名，Slice 2.3 提升；BatchPreviewSampler 仍是取值源）。
const val DEFAULT_BATCH_PREVIEW_COUNT = BatchPreviewSampler.DEFAULT_BATCH_PREVIEW_COUNT
const val MIN_BATCH_PREVIEW_COUNT = BatchPreviewSampler.MIN_BATCH_PREVIEW_COUNT
const val MAX_BATCH_PREVIEW_COUNT = BatchPreviewSampler.MAX_BATCH_PREVIEW_COUNT

// 生成参数页调参存取 prefs 键 + 版本/密钥常量（原 MainActivity companion，Slice 2.4 提升）。
// 纯移动：key 名与数值未变；同包直接引用，Activity 内原有调用点零改动。
const val PREF_GPT_MODE = "gpt_mode"
const val PREF_GPT_PROMPT_PRESET = "gpt_prompt_preset"
const val PREF_GPT_CUSTOM_PROMPT = "gpt_custom_prompt"
const val PREF_GPT_MODEL_ID = "gpt_model_id"
const val PREF_GPT_BASE_URL = "gpt_base_url"
const val PREF_GPT_API_KEY = "gpt_api_key"
const val PREF_GPT_API_KEY_ENCRYPTED = "gpt_api_key_encrypted"
const val PREF_RMBG_COMPONENT_URL = "rmbg_component_url"
const val PREF_RMBG_INPUT_SIZE = "rmbg_input_size"
const val PREF_RMBG_INPUT_SIZE_MIGRATED_TO_1024 = "rmbg_input_size_migrated_to_1024"
const val PREF_LOCAL_SEPARATION_MODE = "local_separation_mode"
const val PREF_FOREGROUND_SUBJECT_PERCENT = "foreground_subject_percent"
const val PREF_FOREGROUND_SHADOW_LEVEL = "foreground_shadow_level"
const val PREF_MONOCHROME_THEME_SCALE = "monochrome_theme_scale"
const val PREF_BACKGROUND_SEPARATION_PERCENT = "background_separation_percent"
const val PREF_PLATE_REMOVAL_PERCENT = "plate_removal_percent"
const val PREF_SHADOW_REMOVAL_PERCENT = "shadow_removal_percent"
const val PREF_EDGE_POLISH_PERCENT = "edge_polish_percent"
const val PREF_RMBG_ALPHA_STRENGTH_PERCENT = "rmbg_alpha_strength_percent"
const val PREF_RMBG_EDGE_FEATHER_PERCENT = "rmbg_edge_feather_percent"
const val PREF_RMBG_EDGE_ADJUST_PERCENT = "rmbg_edge_adjust_percent"
const val PREF_RMBG_WEAK_ALPHA_KEEP_PERCENT = "rmbg_weak_alpha_keep_percent"
const val PREF_LIQUID_GLASS_ENABLED = "liquid_glass_enabled"
const val PREF_LIQUID_GLASS_BOTTOM_BAR_ENABLED = "liquid_glass_bottom_bar_enabled"
const val PREF_LIQUID_GLASS_BOTTOM_BAR_BLUR_ENABLED = "liquid_glass_bottom_bar_blur_enabled"
const val PREF_LIQUID_GLASS_LAYERED_MIGRATED = "liquid_glass_layered_migrated"
const val PREF_LIQUID_GLASS_RADIUS = "liquid_glass_radius"
const val PREF_LIQUID_GLASS_OUTER_WIDTH = "liquid_glass_outer_width"
const val PREF_LIQUID_GLASS_TOP_ALPHA = "liquid_glass_top_alpha"
const val PREF_LIQUID_GLASS_BOTTOM_ALPHA = "liquid_glass_bottom_alpha"
const val PREF_LIQUID_GLASS_BACKGROUND_MIST_ALPHA = "liquid_glass_background_mist_alpha"
const val PREF_LIQUID_GLASS_BOTTOM_DARK_ALPHA = "liquid_glass_bottom_dark_alpha"
const val PREF_LIQUID_GLASS_SUBJECT_SCALE_PERCENT = "liquid_glass_subject_scale_percent"
const val PREF_LIQUID_GLASS_SUBJECT_OUTLINE_WIDTH = "liquid_glass_subject_outline_width"
const val PREF_LIQUID_GLASS_SUBJECT_INNER_OUTLINE_WIDTH = "liquid_glass_subject_inner_outline_width"
const val PREF_LIQUID_GLASS_SUBJECT_SHADOW_ALPHA = "liquid_glass_subject_shadow_alpha"
const val PREF_LIQUID_GLASS_SUBJECT_OPACITY_PERCENT = "liquid_glass_subject_opacity_percent"
const val PREF_LIQUID_GLASS_BACKGROUND_LEVEL_LEGACY = "liquid_glass_background_level"
const val PREF_ADAPTIVE_FOREGROUND_MODE = "adaptive_foreground_mode"
const val PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_PERCENT = "adaptive_direct_max_coverage_percent"
const val PREF_ADAPTIVE_DIRECT_MAX_COVERAGE_INCREASE_PERCENT = "adaptive_direct_max_coverage_increase_percent"
const val PREF_ADAPTIVE_MASK_EDGE_COVERAGE_PERCENT = "adaptive_mask_edge_coverage_percent"
const val PREF_ADAPTIVE_MASK_MIN_COVERAGE_PERCENT = "adaptive_mask_min_coverage_percent"
const val PREF_ADAPTIVE_CENTER_EPSILON_PERCENT = "adaptive_center_epsilon_percent"
const val PREF_ORIGINAL_FOREGROUND_CLEANUP_MODE = "original_foreground_cleanup_mode"
const val PREF_LOCAL_BACKGROUND_SEPARATION_ENABLED = "local_background_separation_enabled"
const val PREF_LOCAL_ADAPTIVE_SELECTION_ENABLED = "local_adaptive_selection_enabled"
const val PREF_LOCAL_CORNER_MASK_CLEANUP_ENABLED = "local_corner_mask_cleanup_enabled"
const val PREF_LOCAL_ALPHA_EDGE_COLOR_REPAIR_ENABLED = "local_alpha_edge_color_repair_enabled"
const val PREF_LOCAL_PLAIN_BACKGROUND_ESTIMATION_ENABLED = "local_plain_background_estimation_enabled"
const val PREF_LOCAL_ORIGINAL_CLEANUP_ENABLED = "local_original_cleanup_enabled"
const val PREF_LOCAL_PLATE_CLEANUP_ENABLED = "local_plate_cleanup_enabled"
const val PREF_LOCAL_PLATE_EDGE_REPAIR_ENABLED = "local_plate_edge_repair_enabled"
const val PREF_LOCAL_PLATE_RESIDUE_CLEANUP_ENABLED = "local_plate_residue_cleanup_enabled"
const val PREF_LOCAL_SHADOW_CLEANUP_ENABLED = "local_shadow_cleanup_enabled"
const val PREF_LOCAL_SHADOW_EDGE_REPAIR_ENABLED = "local_shadow_edge_repair_enabled"
const val PREF_LOCAL_EDGE_TRIM_ENABLED = "local_edge_trim_enabled"
const val PREF_LOCAL_COMPOSED_BACKGROUND_ENABLED = "local_composed_background_enabled"
const val PREF_LOCAL_TWO_LAYER_CANDIDATE_ENABLED = "local_two_layer_candidate_enabled"
const val PREF_LOCAL_COMPONENT_CANDIDATES_ENABLED = "local_component_candidates_enabled"
const val PREF_LOCAL_TEXT_SAFE_CANDIDATE_ENABLED = "local_text_safe_candidate_enabled"
const val PREF_LOCAL_AUTO_SELECTION_ENABLED = "local_auto_selection_enabled"
const val PREF_LOCAL_EDGE_POLISH_ENABLED = "local_edge_polish_enabled"
const val PREF_NIGHT_SUBJECT_LIGHT_BACKGROUND_ENABLED = "night_subject_light_background_enabled"
const val PREF_IMAGE_TUNING_VERSION = "image_tuning_version"
const val PREF_FOREGROUND_SUBJECT_PERCENT_MIGRATED = "foreground_subject_percent_migrated"
const val CURRENT_IMAGE_TUNING_VERSION = 4
const val LEGACY_DEFAULT_GPT_BASE_URL = "http://192.168.31.179:3002/v1"
const val ANDROID_KEYSTORE = "AndroidKeyStore"
const val KEYSTORE_GPT_KEY_ALIAS = "artplus_gpt_api_key"
const val KEYSTORE_CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
const val KEYSTORE_GCM_TAG_BITS = 128

/**
 * Slice 2.5：应用选择器常量（从 MainActivity companion object 提升，原为 private）。
 * 只做物理提升：数值与类型未变。同包直接引用，Activity 内原有调用点零改动。
 * Slice 2.4 deliberately 未动的 saveUiState 域 PREF 及 PREFS_NAME 按需在此提升。
 */
const val PREFS_NAME = "artplus_mobile"
const val PREF_AUTO_CONFIRM_ROOT_WRITE = "auto_confirm_root_write"
const val PREF_AUTO_CONFIRM_REFRESH = "auto_confirm_refresh"
const val PREF_SKIP_ROOT_WRITE_CONFIRM = "skip_root_write_confirm"
const val PREF_USAGE_PERMISSION_PROMPTED = "usage_permission_prompted"
const val PREF_SELECTED_PACKAGE_NAME = "selected_package_name"
const val PREF_GENERATED_FILTER = "generated_filter"
const val PREF_QUERY_TEXT = "query_text"
const val PREF_ADVANCED_SETTINGS_CATEGORY = "advanced_settings_category"
const val PREF_ADVANCED_SETTINGS_TAB = "advanced_settings_tab"
const val PREF_PREVIEW_PACKAGE_NAME = "preview_package_name"
const val PREF_PREVIEW_DIR_PATH = "preview_dir_path"
const val PREF_PREVIEW_STRIP_ENABLED = "preview_strip_enabled"
const val PREF_BATCH_PREVIEW_COUNT = "batch_preview_count"
const val PREF_BATCH_PREVIEW_COLUMNS = "batch_preview_columns"
const val PREF_BATCH_PREVIEW_ICON_SIZE_DP = "batch_preview_icon_size_dp"
const val PREF_BATCH_PREVIEW_CORNER_RADIUS_DP = "batch_preview_corner_radius_dp"
const val PREF_BATCH_PREVIEW_DESKTOP_BG = "batch_preview_desktop_bg"
const val PREF_CUSTOM_WALLPAPER_PATH = "custom_wallpaper_path"
const val CUSTOM_WALLPAPER_FILE = "custom_wallpaper.png"
const val PREF_PREVIEW_DESKTOP_BACKGROUND = "preview_desktop_background"
const val PREF_PREVIEW_ICON_SIZE_DP = "preview_icon_size_dp"
const val PREF_PREVIEW_CORNER_RADIUS_DP = "preview_corner_radius_dp"
const val PREF_SHOW_SYSTEM_APPS = "show_system_apps"
const val PREF_OUTPUT_TREE_URI = "output_tree_uri"
const val PREF_HAS_COMPLETED_ONBOARDING = "has_completed_onboarding"
const val PREVIEW_BUNDLED_WALLPAPER_SHORT_EDGE = 480
