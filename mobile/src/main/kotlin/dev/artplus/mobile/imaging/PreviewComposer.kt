package dev.artplus.mobile

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * 候选图提取 / 单色计算 / 预览合成管线：候选选择 + 前景渲染 + 阴影 + 单色 + 预览资源装配。
 *
 * 从 MainActivity 迁移而来（Epic v2 Phase 1 Slice 1.2 拆分）：原 `internal fun` 成员，
 * 现顶层 `internal`。同包直接复用 ImagingModels.kt 类型（IconCandidate / PreviewChoice /
 * PreviewSelections / Bounds / GenerationSession / PreviewAssets / PreviewMode /
 * ForegroundShadowParams）与既有 imaging 纯函数（monochromeAlpha /
 * hasForegroundTonalRange / isUsableNativeMonochrome / cleanNativeMonochrome /
 * trimMonochromeEdge / polishForegroundEdges / normalDarkForeground /
 * scaleBitmapAroundAlphaCenter / normalizeForegroundSubjectSize / liquidGlassBackgroundForSize /
 * foregroundForSize / alphaBounds / meaningfulAlphaBounds / meaningfulAlphaCentroid /
 * alphaCoverage / meaningfulAlphaCoverage / hasAutoCropRisk / luma）及 TuningParams.kt 常量。
 *
 * 显式参数规则：直接读 Activity 调参状态的函数改为显式收参，Activity 内保留原签名
 * wrapper（标注“重构期间保留”）委托到这里，原有调用点零改动；纯函数签名未变，
 * Activity 内删本体零 wrapper。`rmbgTunedForegroundRaw` 强依赖尚未搬迁的 Slice 1.3
 * RMBG 函数（tuneRmbgAlpha / applyAlphaArrayToSource），defer 回 MainActivity；
 * 需要它的函数（renderCandidateForegroundBase / monochromeForCandidate /
 * renderCandidateForeground / previewAssetsForSelections / previewAssetsForCandidate）
 * 以 `rmbgTunedForeground: (IconCandidate) -> Bitmap?` lambda 注入，wrapper 侧传
 * `::rmbgTunedForegroundRaw`，行为等价。
 */

internal fun candidateOrFallback(
    session: GenerationSession,
    choice: PreviewChoice,
): IconCandidate =
    candidateForChoice(session, choice)
        ?: session.candidates[PreviewChoice.Full]
        ?: session.candidates[PreviewChoice.Plate]
        ?: session.candidates.getValue(PreviewChoice.Original)

internal fun normalizePreviewSelections(
    session: GenerationSession,
    selections: PreviewSelections,
): PreviewSelections {
    val defaultChoice = listOf(
        session.autoLocalChoice,
        PreviewChoice.Full,
        PreviewChoice.Original,
        PreviewChoice.Gpt,
        PreviewChoice.Rmbg,
    ).firstOrNull { candidateForChoice(session, it) != null } ?: PreviewChoice.Original

    fun normalize(choice: PreviewChoice): PreviewChoice {
        if (candidateForChoice(session, choice) != null) {
            return choice
        }
        val directFallback = when (choice) {
            PreviewChoice.GptComposedBackground -> PreviewChoice.Gpt
            PreviewChoice.RmbgComposedBackground -> PreviewChoice.Rmbg
            else -> null
        }
        return directFallback?.takeIf { candidateForChoice(session, it) != null } ?: defaultChoice
    }

    return PreviewSelections(
        normalLight = normalize(selections.normalLight),
        normalDark = normalize(selections.normalDark),
        monochromeLight = normalize(selections.monochromeLight),
        monochromeDark = normalize(selections.monochromeDark),
    )
}

internal fun candidateForChoice(session: GenerationSession, choice: PreviewChoice): IconCandidate? =
    when (choice) {
        PreviewChoice.RmbgComposedBackground -> candidateWithComposedBackground(
            session = session,
            foregroundChoice = PreviewChoice.Rmbg,
        )
        PreviewChoice.GptComposedBackground -> candidateWithComposedBackground(
            session = session,
            foregroundChoice = PreviewChoice.Gpt,
        )
        else -> session.candidates[choice]
    }

internal fun candidateWithComposedBackground(
    session: GenerationSession,
    foregroundChoice: PreviewChoice,
): IconCandidate? {
    val foreground = session.candidates[foregroundChoice] ?: return null
    val background = session.candidates[PreviewChoice.ComposedBackground]?.recbg ?: return null
    return foreground.copy(
        recbg = background,
        customFinalBitmap = null,
    )
}

internal fun candidateWithCustomOverrides(
    session: GenerationSession,
    mode: PreviewMode,
    choice: PreviewChoice,
): IconCandidate {
    val base = candidateOrFallback(session, choice)
    val customForeground = session.customForegrounds[mode]
    val customBackground = session.customBackgrounds[mode]
    if (customForeground == null && customBackground == null) {
        return base
    }
    return base.copy(
        recfgRaw = customForeground ?: base.recfgRaw,
        recbg = customBackground ?: base.recbg,
        monochromeRaw = when {
            customForeground != null -> customForeground
            else -> base.monochromeRaw
        },
        preserveGeometry = if (customForeground != null) true else base.preserveGeometry,
        customFinalBitmap = null,
        rmbgSourceRaw = if (customForeground != null) null else base.rmbgSourceRaw,
        rmbgAlphaRaw = if (customForeground != null) null else base.rmbgAlphaRaw,
        isLocal = customForeground == null && base.isLocal,
        applyLocalEdgePolish = customForeground == null && base.applyLocalEdgePolish,
    )
}

internal fun foregroundShadowParams(level: Int, baseSize: Int): ForegroundShadowParams {
    val ratio = (level.toDouble() / MAX_FOREGROUND_SHADOW_LEVEL.toDouble()).coerceIn(0.0, 1.0)
    val scale = baseSize.toDouble() / SIZE_1X1.toDouble()
    return ForegroundShadowParams(
        alpha = (ratio * FOREGROUND_SHADOW_MAX_ALPHA).roundToInt().coerceIn(0, 255),
        blurRadius = (ratio * FOREGROUND_SHADOW_MAX_BLUR * scale).toFloat(),
        offsetX = (ratio * FOREGROUND_SHADOW_MAX_OFFSET_X * scale).roundToInt(),
        offsetY = (ratio * FOREGROUND_SHADOW_MAX_OFFSET_Y * scale).roundToInt(),
        spread = (ratio * FOREGROUND_SHADOW_MAX_SPREAD * scale).roundToInt().coerceAtLeast(0),
    )
}

internal fun subjectShadowBitmap(source: Bitmap, params: ForegroundShadowParams): Bitmap {
    val width = source.width
    val height = source.height
    val sourcePixels = IntArray(width * height)
    val shadowPixels = IntArray(sourcePixels.size)
    source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
    for (i in sourcePixels.indices) {
        val alpha = (AndroidColor.alpha(sourcePixels[i]) * params.alpha / 255.0)
            .roundToInt()
            .coerceIn(0, 255)
        shadowPixels[i] = if (alpha <= 0) AndroidColor.TRANSPARENT else AndroidColor.argb(alpha, 0, 0, 0)
    }
    val alphaMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    alphaMask.setPixels(shadowPixels, 0, width, 0, 0, width, height)
    val shadow = if (params.spread > 0) growAlphaMask(alphaMask, params.spread) else alphaMask
    return if (params.blurRadius > 0f) {
        blurAlphaMask(shadow, params.blurRadius)
    } else {
        shadow
    }
}

internal fun growAlphaMask(source: Bitmap, radius: Int): Bitmap {
    val width = source.width
    val height = source.height
    val sourcePixels = IntArray(width * height)
    val outPixels = IntArray(sourcePixels.size)
    source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
    val safeRadius = radius.coerceAtLeast(1)
    for (y in 0 until height) {
        for (x in 0 until width) {
            var maxAlpha = 0
            for (dy in -safeRadius..safeRadius) {
                val ny = y + dy
                if (ny !in 0 until height) continue
                for (dx in -safeRadius..safeRadius) {
                    val nx = x + dx
                    if (nx !in 0 until width) continue
                    maxAlpha = maxOf(maxAlpha, AndroidColor.alpha(sourcePixels[ny * width + nx]))
                }
            }
            outPixels[y * width + x] = if (maxAlpha <= 0) {
                AndroidColor.TRANSPARENT
            } else {
                AndroidColor.argb(maxAlpha, 0, 0, 0)
            }
        }
    }
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return out
}

internal fun blurAlphaMask(source: Bitmap, radius: Float): Bitmap {
    val safeRadius = radius.roundToInt().coerceIn(0, 25)
    if (safeRadius <= 0) {
        return source
    }
    val width = source.width
    val height = source.height
    val sourcePixels = IntArray(width * height)
    source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
    val horizontal = IntArray(sourcePixels.size)
    val outPixels = IntArray(sourcePixels.size)
    val window = safeRadius * 2 + 1

    for (y in 0 until height) {
        var sum = 0
        for (x in -safeRadius..safeRadius) {
            val cx = x.coerceIn(0, width - 1)
            sum += AndroidColor.alpha(sourcePixels[y * width + cx])
        }
        for (x in 0 until width) {
            horizontal[y * width + x] = sum / window
            val removeX = (x - safeRadius).coerceIn(0, width - 1)
            val addX = (x + safeRadius + 1).coerceIn(0, width - 1)
            sum += AndroidColor.alpha(sourcePixels[y * width + addX])
            sum -= AndroidColor.alpha(sourcePixels[y * width + removeX])
        }
    }

    for (x in 0 until width) {
        var sum = 0
        for (y in -safeRadius..safeRadius) {
            val cy = y.coerceIn(0, height - 1)
            sum += horizontal[cy * width + x]
        }
        for (y in 0 until height) {
            val alpha = (sum / window).coerceIn(0, 255)
            outPixels[y * width + x] = if (alpha <= 0) {
                AndroidColor.TRANSPARENT
            } else {
                AndroidColor.argb(alpha, 0, 0, 0)
            }
            val removeY = (y - safeRadius).coerceIn(0, height - 1)
            val addY = (y + safeRadius + 1).coerceIn(0, height - 1)
            sum += horizontal[addY * width + x]
            sum -= horizontal[removeY * width + x]
        }
    }
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return out
}

internal fun simpleMonochromeAlphaFromDefaultSubject(source: Bitmap, invertLuma: Boolean): Bitmap {
    val width = source.width
    val height = source.height
    val sourcePixels = IntArray(width * height)
    val outPixels = IntArray(sourcePixels.size)
    source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
    for (i in sourcePixels.indices) {
        val pixel = sourcePixels[i]
        val sourceAlpha = AndroidColor.alpha(pixel)
        if (sourceAlpha <= 0) {
            outPixels[i] = AndroidColor.TRANSPARENT
            continue
        }
        val gray = luma(pixel)
        val tonal = if (invertLuma) 255 - gray else gray
        val outAlpha = (sourceAlpha * tonal / 255.0)
            .roundToInt()
            .coerceIn(0, 255)
        outPixels[i] = if (outAlpha <= 0) {
            AndroidColor.TRANSPARENT
        } else {
            (outAlpha shl 24) or 0x00ffffff
        }
    }
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return out
}

internal fun bitmapStatsJson(bitmap: Bitmap): JSONObject {
    val visibleBounds = alphaBounds(bitmap, LOCAL_ALPHA_VISIBLE_THRESHOLD)
    val meaningfulBounds = meaningfulAlphaBounds(bitmap)
    val centroid = meaningfulAlphaCentroid(bitmap)
    return JSONObject()
        .put("width", bitmap.width)
        .put("height", bitmap.height)
        .put("alpha_coverage", alphaCoverage(bitmap))
        .put("meaningful_alpha_coverage", meaningfulAlphaCoverage(bitmap))
        .put("visible_bounds", boundsJson(visibleBounds))
        .put("meaningful_bounds", boundsJson(meaningfulBounds))
        .put(
            "centroid",
            if (centroid == null) {
                JSONObject.NULL
            } else {
                JSONObject()
                    .put("x", centroid.first)
                    .put("y", centroid.second)
            },
        )
        .put("touches_edge", meaningfulBounds?.let { hasAutoCropRisk(it, bitmap.width, bitmap.height) } ?: false)
}

internal fun boundsJson(bounds: Bounds?): Any =
    if (bounds == null) {
        JSONObject.NULL
    } else {
        JSONObject()
            .put("left", bounds.left)
            .put("top", bounds.top)
            .put("right", bounds.right)
            .put("bottom", bounds.bottom)
            .put("width", bounds.width())
            .put("height", bounds.height())
    }

internal fun effectiveChoiceForPreviewRow(
    mode: PreviewMode,
    rowChoice: PreviewChoice,
    session: GenerationSession,
    previewNormalLight: String,
    previewNormalDark: String,
    previewMonochromeLight: String,
    previewMonochromeDark: String,
): PreviewChoice {
    if (rowChoice != PreviewChoice.ComposedBackground) {
        return rowChoice
    }
    val currentChoice = PreviewSelections.fromNames(previewNormalLight, previewNormalDark, previewMonochromeLight, previewMonochromeDark).choiceFor(mode)
    val target = when (currentChoice) {
        PreviewChoice.Rmbg,
        PreviewChoice.RmbgComposedBackground -> PreviewChoice.RmbgComposedBackground
        PreviewChoice.Gpt,
        PreviewChoice.GptComposedBackground -> PreviewChoice.GptComposedBackground
        else -> PreviewChoice.ComposedBackground
    }
    return if (target == PreviewChoice.ComposedBackground || candidateForChoice(session, target) != null) {
        target
    } else {
        PreviewChoice.ComposedBackground
    }
}

internal fun scaleMonochromeForTheme(source: Bitmap, monochromeThemeScale: Float): Bitmap =
    scaleBitmapAroundAlphaCenter(source, monochromeThemeScale)

internal fun renderCandidateBitmap(bitmap: Bitmap, foregroundSubjectPercent: Int): Bitmap =
    normalizeForegroundSubjectSize(bitmap, foregroundSubjectPercent)

internal fun applyForegroundShadow(source: Bitmap, foregroundShadowLevel: Int): Bitmap {
    val level = foregroundShadowLevel.coerceIn(MIN_FOREGROUND_SHADOW_LEVEL, MAX_FOREGROUND_SHADOW_LEVEL)
    if (level <= 0) {
        return source
    }
    val params = foregroundShadowParams(level, minOf(source.width, source.height))
    val shadow = subjectShadowBitmap(source, params)
    val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    Canvas(out).apply {
        drawColor(AndroidColor.TRANSPARENT)
        drawBitmap(shadow, params.offsetX.toFloat(), params.offsetY.toFloat(), Paint(Paint.FILTER_BITMAP_FLAG))
        drawBitmap(source, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
    }
    return out
}

internal fun renderCandidateForegroundBase(
    candidate: IconCandidate,
    edgePolishPercent: Int,
    foregroundSubjectPercent: Int,
    rmbgTunedForeground: (IconCandidate) -> Bitmap?,
): Bitmap =
    renderCandidateBitmap(rmbgTunedForeground(candidate) ?: candidate.recfgRaw, foregroundSubjectPercent).let { bitmap ->
        if (candidate.isLocal && !candidate.applyLocalEdgePolish) bitmap else polishForegroundEdges(bitmap, edgePolishPercent)
    }

internal fun monochromeForCandidate(
    candidate: IconCandidate,
    invertLuma: Boolean = false,
    edgePolishPercent: Int,
    foregroundSubjectPercent: Int,
    rmbgTunedForeground: (IconCandidate) -> Bitmap?,
): Bitmap {
    if (candidate.monochromeFromDefaultSubject) {
        return simpleMonochromeAlphaFromDefaultSubject(
            renderCandidateBitmap(candidate.recfgRaw, foregroundSubjectPercent),
            invertLuma = invertLuma,
        )
    }
    val foreground = renderCandidateForegroundBase(candidate, edgePolishPercent, foregroundSubjectPercent, rmbgTunedForeground)
    val rmbgSource = rmbgTunedForeground(candidate)?.let { renderCandidateBitmap(it, foregroundSubjectPercent) }
    val nativeSource = candidate.monochromeRaw?.let { renderCandidateBitmap(it, foregroundSubjectPercent) }
    val monochrome = when {
        rmbgSource != null -> {
            monochromeAlpha(rmbgSource, invertLuma = invertLuma)
        }
        hasForegroundTonalRange(foreground) -> {
            monochromeAlpha(foreground, invertLuma = invertLuma)
        }
        nativeSource != null &&
            candidate.monochromeIsNative &&
            isUsableNativeMonochrome(nativeSource, foreground) -> {
            cleanNativeMonochrome(nativeSource)
        }
        nativeSource != null && hasForegroundTonalRange(nativeSource) -> {
            monochromeAlpha(nativeSource, invertLuma = invertLuma)
        }
        nativeSource != null && !candidate.monochromeIsNative -> {
            monochromeAlpha(nativeSource, invertLuma = invertLuma)
        }
        else -> {
            monochromeAlpha(foreground, invertLuma = invertLuma)
        }
    }
    return trimMonochromeEdge(monochrome)
}

internal fun renderCandidateForeground(
    candidate: IconCandidate,
    edgePolishPercent: Int,
    foregroundSubjectPercent: Int,
    rmbgTunedForeground: (IconCandidate) -> Bitmap?,
    liquidGlassEnabled: Boolean,
    liquidGlassSubjectScalePercent: Int,
    liquidGlassSubjectShadowAlpha: Int,
    liquidGlassSubjectOutlineWidth: Int,
    liquidGlassSubjectInnerOutlineWidth: Int,
    liquidGlassSubjectOpacityPercent: Int,
    liquidGlassTopAlpha: Int,
    liquidGlassBottomAlpha: Int,
    liquidGlassBottomDarkAlpha: Int,
    liquidGlassOuterWidth: Int,
    liquidGlassRadius: Int,
    foregroundShadowLevel: Int,
): Bitmap =
    foregroundForSize(
        source = renderCandidateForegroundBase(candidate, edgePolishPercent, foregroundSubjectPercent, rmbgTunedForeground),
        width = SIZE_1X1,
        height = SIZE_1X1,
        liquidGlassEnabled = liquidGlassEnabled,
        liquidGlassSubjectScalePercent = liquidGlassSubjectScalePercent,
        liquidGlassSubjectShadowAlpha = liquidGlassSubjectShadowAlpha,
        liquidGlassSubjectOutlineWidth = liquidGlassSubjectOutlineWidth,
        liquidGlassSubjectInnerOutlineWidth = liquidGlassSubjectInnerOutlineWidth,
        liquidGlassSubjectOpacityPercent = liquidGlassSubjectOpacityPercent,
        liquidGlassTopAlpha = liquidGlassTopAlpha,
        liquidGlassBottomAlpha = liquidGlassBottomAlpha,
        liquidGlassBottomDarkAlpha = liquidGlassBottomDarkAlpha,
        liquidGlassOuterWidth = liquidGlassOuterWidth,
        liquidGlassRadius = liquidGlassRadius,
        applyShadow = { bitmap -> applyForegroundShadow(bitmap, foregroundShadowLevel) },
        renderSubjectShadow = ::subjectShadowBitmap,
    )

internal fun previewAssetsForSelections(
    session: GenerationSession,
    selections: PreviewSelections,
    edgePolishPercent: Int,
    foregroundSubjectPercent: Int,
    rmbgTunedForeground: (IconCandidate) -> Bitmap?,
    liquidGlassEnabled: Boolean,
    liquidGlassBackgroundMistAlpha: Int,
    liquidGlassTopAlpha: Int,
    liquidGlassBottomAlpha: Int,
    liquidGlassBottomDarkAlpha: Int,
    liquidGlassOuterWidth: Int,
    liquidGlassRadius: Int,
    liquidGlassSubjectScalePercent: Int,
    liquidGlassSubjectShadowAlpha: Int,
    liquidGlassSubjectOutlineWidth: Int,
    liquidGlassSubjectInnerOutlineWidth: Int,
    liquidGlassSubjectOpacityPercent: Int,
    foregroundShadowLevel: Int,
    nightSubjectLightBackgroundEnabled: Boolean,
): PreviewAssets {
    val light = candidateWithCustomOverrides(session, PreviewMode.NormalLight, selections.normalLight)
    val lightRecfg = renderCandidateForeground(
        candidate = light,
        edgePolishPercent = edgePolishPercent,
        foregroundSubjectPercent = foregroundSubjectPercent,
        rmbgTunedForeground = rmbgTunedForeground,
        liquidGlassEnabled = liquidGlassEnabled,
        liquidGlassSubjectScalePercent = liquidGlassSubjectScalePercent,
        liquidGlassSubjectShadowAlpha = liquidGlassSubjectShadowAlpha,
        liquidGlassSubjectOutlineWidth = liquidGlassSubjectOutlineWidth,
        liquidGlassSubjectInnerOutlineWidth = liquidGlassSubjectInnerOutlineWidth,
        liquidGlassSubjectOpacityPercent = liquidGlassSubjectOpacityPercent,
        liquidGlassTopAlpha = liquidGlassTopAlpha,
        liquidGlassBottomAlpha = liquidGlassBottomAlpha,
        liquidGlassBottomDarkAlpha = liquidGlassBottomDarkAlpha,
        liquidGlassOuterWidth = liquidGlassOuterWidth,
        liquidGlassRadius = liquidGlassRadius,
        foregroundShadowLevel = foregroundShadowLevel,
    )
    val lightRecbg = liquidGlassBackgroundForSize(
        source = light.recbg,
        width = SIZE_1X1,
        height = SIZE_1X1,
        liquidGlassEnabled = liquidGlassEnabled,
        liquidGlassBackgroundMistAlpha = liquidGlassBackgroundMistAlpha,
        liquidGlassTopAlpha = liquidGlassTopAlpha,
        liquidGlassBottomAlpha = liquidGlassBottomAlpha,
        liquidGlassBottomDarkAlpha = liquidGlassBottomDarkAlpha,
        liquidGlassOuterWidth = liquidGlassOuterWidth,
        liquidGlassRadius = liquidGlassRadius,
    )

    val night = candidateWithCustomOverrides(session, PreviewMode.NormalDark, selections.normalDark)
    val nightPreview = run {
        val nightRecfg = renderCandidateForeground(
            candidate = night,
            edgePolishPercent = edgePolishPercent,
            foregroundSubjectPercent = foregroundSubjectPercent,
            rmbgTunedForeground = rmbgTunedForeground,
            liquidGlassEnabled = liquidGlassEnabled,
            liquidGlassSubjectScalePercent = liquidGlassSubjectScalePercent,
            liquidGlassSubjectShadowAlpha = liquidGlassSubjectShadowAlpha,
            liquidGlassSubjectOutlineWidth = liquidGlassSubjectOutlineWidth,
            liquidGlassSubjectInnerOutlineWidth = liquidGlassSubjectInnerOutlineWidth,
            liquidGlassSubjectOpacityPercent = liquidGlassSubjectOpacityPercent,
            liquidGlassTopAlpha = liquidGlassTopAlpha,
            liquidGlassBottomAlpha = liquidGlassBottomAlpha,
            liquidGlassBottomDarkAlpha = liquidGlassBottomDarkAlpha,
            liquidGlassOuterWidth = liquidGlassOuterWidth,
            liquidGlassRadius = liquidGlassRadius,
            foregroundShadowLevel = foregroundShadowLevel,
        )
        val nightRecbg = liquidGlassBackgroundForSize(
            source = night.recbg,
            width = SIZE_1X1,
            height = SIZE_1X1,
            liquidGlassEnabled = liquidGlassEnabled,
            liquidGlassBackgroundMistAlpha = liquidGlassBackgroundMistAlpha,
            liquidGlassTopAlpha = liquidGlassTopAlpha,
            liquidGlassBottomAlpha = liquidGlassBottomAlpha,
            liquidGlassBottomDarkAlpha = liquidGlassBottomDarkAlpha,
            liquidGlassOuterWidth = liquidGlassOuterWidth,
            liquidGlassRadius = liquidGlassRadius,
        )
        normalDarkForeground(nightRecfg, nightRecbg, nightSubjectLightBackgroundEnabled)
    }

    val monochromeLight = monochromeForCandidate(
        candidate = candidateWithCustomOverrides(session, PreviewMode.MonochromeLight, selections.monochromeLight),
        invertLuma = true,
        edgePolishPercent = edgePolishPercent,
        foregroundSubjectPercent = foregroundSubjectPercent,
        rmbgTunedForeground = rmbgTunedForeground,
    )
    val monochromeDark = monochromeForCandidate(
        candidate = candidateWithCustomOverrides(session, PreviewMode.MonochromeDark, selections.monochromeDark),
        invertLuma = false,
        edgePolishPercent = edgePolishPercent,
        foregroundSubjectPercent = foregroundSubjectPercent,
        rmbgTunedForeground = rmbgTunedForeground,
    )

    return PreviewAssets(
        recbg = lightRecbg,
        recfg = lightRecfg,
        recNight = nightPreview,
        monochromeLight = monochromeLight,
        monochromeDark = monochromeDark,
    )
}

internal fun previewAssetsForCandidate(
    candidate: IconCandidate,
    mode: PreviewMode? = null,
    edgePolishPercent: Int,
    foregroundSubjectPercent: Int,
    rmbgTunedForeground: (IconCandidate) -> Bitmap?,
    liquidGlassEnabled: Boolean,
    liquidGlassBackgroundMistAlpha: Int,
    liquidGlassTopAlpha: Int,
    liquidGlassBottomAlpha: Int,
    liquidGlassBottomDarkAlpha: Int,
    liquidGlassOuterWidth: Int,
    liquidGlassRadius: Int,
    liquidGlassSubjectScalePercent: Int,
    liquidGlassSubjectShadowAlpha: Int,
    liquidGlassSubjectOutlineWidth: Int,
    liquidGlassSubjectInnerOutlineWidth: Int,
    liquidGlassSubjectOpacityPercent: Int,
    foregroundShadowLevel: Int,
    nightSubjectLightBackgroundEnabled: Boolean,
): PreviewAssets {
    val customFinal = candidate.customFinalBitmap
    if (customFinal != null) {
        val transparent = solidBitmap(customFinal.width, customFinal.height, AndroidColor.TRANSPARENT)
        return when (mode) {
            PreviewMode.NormalLight -> PreviewAssets(
                recbg = transparent,
                recfg = customFinal,
                recNight = null,
                monochromeLight = null,
                monochromeDark = null,
            )
            PreviewMode.NormalDark -> PreviewAssets(
                recbg = null,
                recfg = null,
                recNight = customFinal,
                monochromeLight = null,
                monochromeDark = null,
            )
            PreviewMode.MonochromeLight,
            PreviewMode.MonochromeDark,
            null -> PreviewAssets(
                recbg = null,
                recfg = null,
                recNight = null,
                monochromeLight = monochromeForCandidate(
                    candidate = candidate,
                    invertLuma = true,
                    edgePolishPercent = edgePolishPercent,
                    foregroundSubjectPercent = foregroundSubjectPercent,
                    rmbgTunedForeground = rmbgTunedForeground,
                ),
                monochromeDark = monochromeForCandidate(
                    candidate = candidate,
                    invertLuma = false,
                    edgePolishPercent = edgePolishPercent,
                    foregroundSubjectPercent = foregroundSubjectPercent,
                    rmbgTunedForeground = rmbgTunedForeground,
                ),
            )
        }
    }
    val recfg = renderCandidateForeground(
        candidate = candidate,
        edgePolishPercent = edgePolishPercent,
        foregroundSubjectPercent = foregroundSubjectPercent,
        rmbgTunedForeground = rmbgTunedForeground,
        liquidGlassEnabled = liquidGlassEnabled,
        liquidGlassSubjectScalePercent = liquidGlassSubjectScalePercent,
        liquidGlassSubjectShadowAlpha = liquidGlassSubjectShadowAlpha,
        liquidGlassSubjectOutlineWidth = liquidGlassSubjectOutlineWidth,
        liquidGlassSubjectInnerOutlineWidth = liquidGlassSubjectInnerOutlineWidth,
        liquidGlassSubjectOpacityPercent = liquidGlassSubjectOpacityPercent,
        liquidGlassTopAlpha = liquidGlassTopAlpha,
        liquidGlassBottomAlpha = liquidGlassBottomAlpha,
        liquidGlassBottomDarkAlpha = liquidGlassBottomDarkAlpha,
        liquidGlassOuterWidth = liquidGlassOuterWidth,
        liquidGlassRadius = liquidGlassRadius,
        foregroundShadowLevel = foregroundShadowLevel,
    )
    val recbg = liquidGlassBackgroundForSize(
        source = candidate.recbg,
        width = SIZE_1X1,
        height = SIZE_1X1,
        liquidGlassEnabled = liquidGlassEnabled,
        liquidGlassBackgroundMistAlpha = liquidGlassBackgroundMistAlpha,
        liquidGlassTopAlpha = liquidGlassTopAlpha,
        liquidGlassBottomAlpha = liquidGlassBottomAlpha,
        liquidGlassBottomDarkAlpha = liquidGlassBottomDarkAlpha,
        liquidGlassOuterWidth = liquidGlassOuterWidth,
        liquidGlassRadius = liquidGlassRadius,
    )
    return PreviewAssets(
        recbg = recbg,
        recfg = recfg,
        recNight = normalDarkForeground(recfg, recbg, nightSubjectLightBackgroundEnabled),
        monochromeLight = monochromeForCandidate(
            candidate = candidate,
            invertLuma = true,
            edgePolishPercent = edgePolishPercent,
            foregroundSubjectPercent = foregroundSubjectPercent,
            rmbgTunedForeground = rmbgTunedForeground,
        ),
        monochromeDark = monochromeForCandidate(
            candidate = candidate,
            invertLuma = false,
            edgePolishPercent = edgePolishPercent,
            foregroundSubjectPercent = foregroundSubjectPercent,
            rmbgTunedForeground = rmbgTunedForeground,
        ),
    )
}
