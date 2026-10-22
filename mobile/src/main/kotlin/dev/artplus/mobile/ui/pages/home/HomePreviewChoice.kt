package dev.artplus.mobile.ui.pages.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.window.WindowBottomSheet
import io.github.suqi8.coui.kmp.theme.COUITheme
import com.caverock.androidsvg.SVG
import dev.artplus.mobile.MAX_FOREGROUND_SUBJECT_PERCENT
import dev.artplus.mobile.MIN_FOREGROUND_SUBJECT_PERCENT
import dev.artplus.mobile.TuningParams
import dev.artplus.mobile.imaging.CustomImageKind
import dev.artplus.mobile.imaging.IconCandidate
import dev.artplus.mobile.imaging.PreviewChoice
import dev.artplus.mobile.imaging.candidateForChoice
import dev.artplus.mobile.imaging.effectiveChoiceForPreviewRow
import dev.artplus.mobile.imaging.isComposedBackgroundCombination
import dev.artplus.mobile.pipeline.GenerationSession
import dev.artplus.mobile.pipeline.PreviewAssets
import dev.artplus.mobile.pipeline.PreviewMode
import dev.artplus.mobile.pipeline.customCandidateForPreview
import dev.artplus.mobile.ui.cards.AiIconLoadingPreview
import dev.artplus.mobile.ui.cards.NumberParameterControl
import dev.artplus.mobile.ui.cards.PreviewChoiceActions
import dev.artplus.mobile.ui.cards.PreviewSelections
import dev.artplus.mobile.ui.components.MetricPill
/**
 * Slice 2.2：预览来源抽屉（原 MainActivity 残留本体原样搬迁）。
 * Composable 只收 state + onEvent；Activity 状态经参数/回调注入，行为与 UI 100% 等价。
 * 预览资产函数（candidateForChoice/customCandidateForPreview/effectiveChoiceForPreviewRow）同包直接用。
 */

internal fun shouldShowPreviewChoiceRow(choice: PreviewChoice, session: GenerationSession): Boolean =
    when {
        choice.isCustom -> true
        choice == PreviewChoice.Full -> session.candidates[PreviewChoice.Full] != null ||
            session.candidates[PreviewChoice.Plate] != null
        else -> candidateForChoice(session, choice) != null
    }

@Composable
internal fun MoreRulesGroupRow(
    selectedRule: PreviewChoice?,
    expanded: Boolean,
    isBusy: Boolean,
    isGeneratingGptCandidate: Boolean,
    isGeneratingRmbgCandidate: Boolean,
    onToggle: () -> Unit,
) {
    val selected = selectedRule != null
    // 选中态用 ColorOS 浅蓝底（tertiaryContainer）+ 蓝色文字；未选中为中性灰底（面板是白底，不能再用 surfaceContainerHigh）
    val background = if (selected) {
        COUITheme.colorScheme.tertiaryContainer
    } else {
        COUITheme.colorScheme.secondaryVariant
    }
    val titleColor = if (selected) {
        COUITheme.colorScheme.onTertiaryContainer
    } else {
        COUITheme.colorScheme.onSurface
    }
    val summaryColor = if (selected) {
        COUITheme.colorScheme.onTertiaryContainer
    } else {
        COUITheme.colorScheme.onSurfaceVariantSummary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 78.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable(
                enabled = !isBusy && !isGeneratingGptCandidate && !isGeneratingRmbgCandidate,
                onClick = onToggle,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "更多规则",
                style = COUITheme.textStyles.body1,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = selectedRule?.let { "当前使用: ${it.label}" }
                    ?: "字标保全 / 底座 / 二层",
                style = COUITheme.textStyles.footnote1,
                color = summaryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        MetricPill(label = if (expanded) "收起" else "展开")
    }
}

@Composable
internal fun CandidateIconPreview(
    candidate: IconCandidate,
    mode: PreviewMode,
    tuningState: TuningParams,
    cornerRadiusDp: Int,
    materialColorProvider: (String, Color) -> Color,
    loadAssets: suspend (IconCandidate, PreviewMode?) -> PreviewAssets?,
) {
    var assets by remember(
        candidate,
        mode,
        tuningState.foregroundSubjectPercent,
        tuningState.foregroundShadowLevel,
        tuningState.edgePolishPercent,
        tuningState.rmbgAlphaStrengthPercent,
        tuningState.rmbgEdgeFeatherPercent,
        tuningState.rmbgEdgeAdjustPercent,
        tuningState.rmbgWeakAlphaKeepPercent,
        tuningState.liquidGlassEnabled,
        tuningState.liquidGlassRadius,
        tuningState.liquidGlassOuterWidth,
        tuningState.liquidGlassTopAlpha,
        tuningState.liquidGlassBottomAlpha,
        tuningState.liquidGlassBackgroundMistAlpha,
        tuningState.liquidGlassBottomDarkAlpha,
        tuningState.liquidGlassSubjectScalePercent,
        tuningState.liquidGlassSubjectOutlineWidth,
        tuningState.liquidGlassSubjectInnerOutlineWidth,
        tuningState.liquidGlassSubjectShadowAlpha,
        tuningState.liquidGlassSubjectOpacityPercent,
        tuningState.nightSubjectLightBackgroundEnabled,
    ) {
        mutableStateOf<PreviewAssets?>(null)
    }
    LaunchedEffect(
        candidate,
        mode,
        tuningState.foregroundSubjectPercent,
        tuningState.foregroundShadowLevel,
        tuningState.edgePolishPercent,
        tuningState.rmbgAlphaStrengthPercent,
        tuningState.rmbgEdgeFeatherPercent,
        tuningState.rmbgEdgeAdjustPercent,
        tuningState.rmbgWeakAlphaKeepPercent,
        tuningState.liquidGlassEnabled,
        tuningState.liquidGlassRadius,
        tuningState.liquidGlassOuterWidth,
        tuningState.liquidGlassTopAlpha,
        tuningState.liquidGlassBottomAlpha,
        tuningState.liquidGlassBackgroundMistAlpha,
        tuningState.liquidGlassBottomDarkAlpha,
        tuningState.liquidGlassSubjectScalePercent,
        tuningState.liquidGlassSubjectOutlineWidth,
        tuningState.liquidGlassSubjectInnerOutlineWidth,
        tuningState.liquidGlassSubjectShadowAlpha,
        tuningState.liquidGlassSubjectOpacityPercent,
        tuningState.nightSubjectLightBackgroundEnabled,
    ) {
        assets = null
        try {
            assets = loadAssets(candidate, mode)
        } catch (_: CancellationException) {
            throw CancellationException()
        } catch (_: Throwable) {
            assets = null
        }
    }
    val readyAssets = assets
    if (readyAssets == null) {
        AiIconLoadingPreview(modifier = Modifier.fillMaxSize(), overlay = true)
    } else {
        GeneratedIconPreview(
            assets = readyAssets,
            mode = mode,
            cornerRadiusDp = cornerRadiusDp,
            materialColorProvider = materialColorProvider,
        )
    }
}

@Composable
internal fun PreviewChoiceRow(
    mode: PreviewMode,
    choice: PreviewChoice,
    session: GenerationSession,
    tuningState: TuningParams,
    isBusy: Boolean,
    isGeneratingGptCandidate: Boolean,
    isGeneratingRmbgCandidate: Boolean,
    rmbgCandidatePackageName: String?,
    rmbgCandidateMode: PreviewMode?,
    rmbgCandidateFailurePackageName: String?,
    rmbgCandidateFailureMode: PreviewMode?,
    lastRmbgCandidateError: String?,
    rmbgCandidateStatusText: String,
    gptBaseUrl: String,
    gptApiKey: String,
    hasRmbgComponent: Boolean,
    cornerRadiusDp: Int,
    materialColorProvider: (String, Color) -> Color,
    loadCandidateAssets: suspend (IconCandidate, PreviewMode?) -> PreviewAssets?,
    onGenerateGpt: (PreviewMode) -> Unit,
    onGenerateRmbg: (PreviewMode) -> Unit,
    onChooseCustom: (PreviewMode, CustomImageKind) -> Unit,
    onApply: (PreviewMode, PreviewChoice) -> Unit,
    onApplyAll: (PreviewChoice) -> Unit,
) {
    val currentChoice = PreviewSelections.fromNames(tuningState.previewNormalLight, tuningState.previewNormalDark, tuningState.previewMonochromeLight, tuningState.previewMonochromeDark).choiceFor(mode)
    val effectiveChoice = effectiveChoiceForPreviewRow(
        mode = mode,
        rowChoice = choice,
        session = session,
        previewNormalLight = tuningState.previewNormalLight,
        previewNormalDark = tuningState.previewNormalDark,
        previewMonochromeLight = tuningState.previewMonochromeLight,
        previewMonochromeDark = tuningState.previewMonochromeDark,
    )
    val selected = currentChoice == effectiveChoice ||
        (choice == PreviewChoice.ComposedBackground && currentChoice.isComposedBackgroundCombination)
    val customKind = choice.customKind
    val candidate = if (customKind == null) {
        candidateForChoice(session, effectiveChoice)
    } else {
        customCandidateForPreview(mode, customKind, session)
    }
    val gptMissing = effectiveChoice == PreviewChoice.Gpt && candidate == null
    val rmbgMissing = effectiveChoice == PreviewChoice.Rmbg && candidate == null
    val customMissing = customKind != null && candidate == null
    val rmbgRunning = choice == PreviewChoice.Rmbg &&
        isGeneratingRmbgCandidate &&
        rmbgCandidatePackageName == session.packageName &&
        (rmbgCandidateMode == null || rmbgCandidateMode == mode)
    val rmbgFailure = if (
        choice == PreviewChoice.Rmbg &&
        rmbgCandidateFailurePackageName == session.packageName &&
        (rmbgCandidateFailureMode == null || rmbgCandidateFailureMode == mode)
    ) {
        lastRmbgCandidateError
    } else {
        null
    }
    val canGenerateGpt = gptBaseUrl.trim().isNotEmpty() && gptApiKey.trim().isNotEmpty()
    val canGenerateRmbg = rmbgMissing && hasRmbgComponent
    val missingLocalCandidate = choice != PreviewChoice.Gpt &&
        customKind == null &&
        candidate == null &&
        !canGenerateRmbg
    val canImportCustom = customMissing
    val missingCandidate = missingLocalCandidate && !canImportCustom
    val enabled = !isBusy && !isGeneratingGptCandidate && !isGeneratingRmbgCandidate && !missingCandidate
    // 选中态用 ColorOS 浅蓝底（tertiaryContainer）+ 蓝色文字；未选中为中性灰底（面板是白底，不能再用 surfaceContainerHigh）
    val background = if (selected) {
        COUITheme.colorScheme.tertiaryContainer
    } else {
        COUITheme.colorScheme.secondaryVariant
    }
    val titleColor = if (selected) {
        COUITheme.colorScheme.onTertiaryContainer
    } else {
        COUITheme.colorScheme.onSurface
    }
    val summaryColor = if (selected) {
        COUITheme.colorScheme.onTertiaryContainer
    } else {
        COUITheme.colorScheme.onSurfaceVariantSummary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable(enabled = enabled) {
                if (gptMissing) {
                    onGenerateGpt(mode)
                } else if (rmbgMissing) {
                    onGenerateRmbg(mode)
                } else if (customKind != null) {
                    onChooseCustom(mode, customKind)
                } else {
                    onApply(mode, effectiveChoice)
                }
            }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(COUITheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            if (choice == PreviewChoice.Gpt && isGeneratingGptCandidate) {
                AiIconLoadingPreview(modifier = Modifier.fillMaxSize())
            } else if (rmbgRunning) {
                AiIconLoadingPreview(modifier = Modifier.fillMaxSize(), overlay = true)
            } else if (candidate != null) {
                CandidateIconPreview(
                    candidate = candidate,
                    mode = mode,
                    tuningState = tuningState,
                    cornerRadiusDp = cornerRadiusDp,
                    materialColorProvider = materialColorProvider,
                    loadAssets = loadCandidateAssets,
                )
            } else {
                MissingIconPreview(
                    modifier = Modifier.fillMaxSize(),
                    mode = mode,
                    compact = true,
                    cornerRadiusDp = cornerRadiusDp,
                    materialColorProvider = materialColorProvider,
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = choice.label,
                style = COUITheme.textStyles.body1,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = when {
                    selected -> "当前使用"
                    missingCandidate && choice == PreviewChoice.TwoLayer -> "当前图标不符合二层结构"
                    missingCandidate && choice == PreviewChoice.Rmbg -> "未安装组件"
                    rmbgRunning -> rmbgCandidateStatusText.ifBlank { "RMBG运行中" }
                    rmbgMissing && rmbgFailure != null -> rmbgFailure
                    rmbgMissing -> "点击运行"
                    customMissing -> "选择 PNG/SVG"
                    customKind != null -> "已导入"
                    missingCandidate -> "不可用"
                    choice == PreviewChoice.Gpt && isGeneratingGptCandidate -> "正在生成"
                    gptMissing && !canGenerateGpt -> "请填写AI提供商信息"
                    gptMissing -> "点击生成"
                    effectiveChoice.isComposedBackgroundCombination -> effectiveChoice.summary
                    else -> choice.summary
                },
                style = COUITheme.textStyles.footnote1,
                color = summaryColor,
                maxLines = if (choice == PreviewChoice.Rmbg && rmbgFailure != null) 4 else 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        PreviewChoiceActions(
            showApplyAll = customKind == null,
            applyEnabled = enabled && customKind == null,
            onApplyAll = { onApplyAll(effectiveChoice) },
        )
    }
}

@Composable
internal fun PreviewChoiceBottomSheet(
    show: Boolean,
    mode: PreviewMode,
    session: GenerationSession,
    tuningState: TuningParams,
    isBusy: Boolean,
    isGeneratingGptCandidate: Boolean,
    isGeneratingRmbgCandidate: Boolean,
    draftForegroundSubjectPercentText: String,
    isDark: Boolean,
    nightSubjectLightBackgroundEnabled: Boolean,
    rmbgCandidatePackageName: String?,
    rmbgCandidateMode: PreviewMode?,
    rmbgCandidateFailurePackageName: String?,
    rmbgCandidateFailureMode: PreviewMode?,
    lastRmbgCandidateError: String?,
    rmbgCandidateStatusText: String,
    gptBaseUrl: String,
    gptApiKey: String,
    hasRmbgComponent: Boolean,
    cornerRadiusDp: Int,
    materialColorProvider: (String, Color) -> Color,
    loadCandidateAssets: suspend (IconCandidate, PreviewMode?) -> PreviewAssets?,
    onNightFill: (Boolean) -> Unit,
    onDraftForegroundSubjectPercent: (String) -> Unit,
    onSaveForegroundSubjectPercent: (Int) -> Unit,
    onGenerateGpt: (PreviewMode) -> Unit,
    onGenerateRmbg: (PreviewMode) -> Unit,
    onChooseCustom: (PreviewMode, CustomImageKind) -> Unit,
    onApply: (PreviewMode, PreviewChoice) -> Unit,
    onApplyAll: (PreviewChoice) -> Unit,
    onDismissRequest: () -> Unit,
    onDismissFinished: () -> Unit,
) {
    val defaultChoices = listOf(
        PreviewChoice.Original,
        PreviewChoice.ComposedBackground,
        PreviewChoice.Rmbg,
        PreviewChoice.Gpt,
    )
    val customChoices = listOf(
        PreviewChoice.CustomForeground,
        PreviewChoice.CustomBackground,
    )
    val moreChoices = listOf(
        PreviewChoice.TextSafe,
        PreviewChoice.ComponentSubject,
        PreviewChoice.ComponentBackground,
        PreviewChoice.TwoLayer,
    )
    val selectedMoreRule = PreviewSelections.fromNames(
        tuningState.previewNormalLight,
        tuningState.previewNormalDark,
        tuningState.previewMonochromeLight,
        tuningState.previewMonochromeDark,
    ).choiceFor(mode).let { choice ->
        when {
            choice == PreviewChoice.Plate -> PreviewChoice.Full
            choice in moreChoices -> choice
            else -> null
        }
    }
    var showMoreRules by remember(mode) { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(showMoreRules) {
        if (showMoreRules) {
            delay(60)
            scrollState.animateScrollTo(
                scrollState.maxValue,
                animationSpec = tween(durationMillis = 120, easing = LinearEasing),
            )
            delay(140)
            scrollState.animateScrollTo(
                scrollState.maxValue,
                animationSpec = tween(durationMillis = 180, easing = LinearEasing),
            )
            delay(80)
            if (scrollState.value < scrollState.maxValue) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
    }

    WindowBottomSheet(
        show = show,
        title = "${mode.label} 来源",
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "每个槽位单独选择",
                style = COUITheme.textStyles.footnote1,
                color = COUITheme.colorScheme.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (mode == PreviewMode.NormalDark) {
                PreviewNightFillBackgroundRow(
                    checked = nightSubjectLightBackgroundEnabled,
                    isBusy = isBusy,
                    onCheckedChange = onNightFill,
                )
            }

            NumberParameterControl(
                busy = isBusy,
                title = "主体占比",
                summary = "复杂游戏图标建议 100%",
                value = tuningState.foregroundSubjectPercent,
                draftText = draftForegroundSubjectPercentText,
                min = MIN_FOREGROUND_SUBJECT_PERCENT,
                max = MAX_FOREGROUND_SUBJECT_PERCENT,
                step = 1,
                onDraftChange = onDraftForegroundSubjectPercent,
                onSave = onSaveForegroundSubjectPercent,
                showIcon = false,
                icon = null,
                standaloneCard = true,
                cardHeight = 78.dp,
                inputBackgroundColor = COUITheme.colorScheme.secondaryContainer,
            )

            defaultChoices.forEach { choice ->
                PreviewChoiceRow(
                    mode = mode,
                    choice = choice,
                    session = session,
                    tuningState = tuningState,
                    isBusy = isBusy,
                    isGeneratingGptCandidate = isGeneratingGptCandidate,
                    isGeneratingRmbgCandidate = isGeneratingRmbgCandidate,
                    rmbgCandidatePackageName = rmbgCandidatePackageName,
                    rmbgCandidateMode = rmbgCandidateMode,
                    rmbgCandidateFailurePackageName = rmbgCandidateFailurePackageName,
                    rmbgCandidateFailureMode = rmbgCandidateFailureMode,
                    lastRmbgCandidateError = lastRmbgCandidateError,
                    rmbgCandidateStatusText = rmbgCandidateStatusText,
                    gptBaseUrl = gptBaseUrl,
                    gptApiKey = gptApiKey,
                    hasRmbgComponent = hasRmbgComponent,
                    cornerRadiusDp = cornerRadiusDp,
                    materialColorProvider = materialColorProvider,
                    loadCandidateAssets = loadCandidateAssets,
                    onGenerateGpt = onGenerateGpt,
                    onGenerateRmbg = onGenerateRmbg,
                    onChooseCustom = onChooseCustom,
                    onApply = onApply,
                    onApplyAll = onApplyAll,
                )
            }
            if (shouldShowPreviewChoiceRow(PreviewChoice.Full, session)) {
                PreviewChoiceRow(
                    mode = mode,
                    choice = PreviewChoice.Full,
                    session = session,
                    tuningState = tuningState,
                    isBusy = isBusy,
                    isGeneratingGptCandidate = isGeneratingGptCandidate,
                    isGeneratingRmbgCandidate = isGeneratingRmbgCandidate,
                    rmbgCandidatePackageName = rmbgCandidatePackageName,
                    rmbgCandidateMode = rmbgCandidateMode,
                    rmbgCandidateFailurePackageName = rmbgCandidateFailurePackageName,
                    rmbgCandidateFailureMode = rmbgCandidateFailureMode,
                    lastRmbgCandidateError = lastRmbgCandidateError,
                    rmbgCandidateStatusText = rmbgCandidateStatusText,
                    gptBaseUrl = gptBaseUrl,
                    gptApiKey = gptApiKey,
                    hasRmbgComponent = hasRmbgComponent,
                    cornerRadiusDp = cornerRadiusDp,
                    materialColorProvider = materialColorProvider,
                    loadCandidateAssets = loadCandidateAssets,
                    onGenerateGpt = onGenerateGpt,
                    onGenerateRmbg = onGenerateRmbg,
                    onChooseCustom = onChooseCustom,
                    onApply = onApply,
                    onApplyAll = onApplyAll,
                )
            }

            customChoices.forEach { choice ->
                PreviewChoiceRow(
                    mode = mode,
                    choice = choice,
                    session = session,
                    tuningState = tuningState,
                    isBusy = isBusy,
                    isGeneratingGptCandidate = isGeneratingGptCandidate,
                    isGeneratingRmbgCandidate = isGeneratingRmbgCandidate,
                    rmbgCandidatePackageName = rmbgCandidatePackageName,
                    rmbgCandidateMode = rmbgCandidateMode,
                    rmbgCandidateFailurePackageName = rmbgCandidateFailurePackageName,
                    rmbgCandidateFailureMode = rmbgCandidateFailureMode,
                    lastRmbgCandidateError = lastRmbgCandidateError,
                    rmbgCandidateStatusText = rmbgCandidateStatusText,
                    gptBaseUrl = gptBaseUrl,
                    gptApiKey = gptApiKey,
                    hasRmbgComponent = hasRmbgComponent,
                    cornerRadiusDp = cornerRadiusDp,
                    materialColorProvider = materialColorProvider,
                    loadCandidateAssets = loadCandidateAssets,
                    onGenerateGpt = onGenerateGpt,
                    onGenerateRmbg = onGenerateRmbg,
                    onChooseCustom = onChooseCustom,
                    onApply = onApply,
                    onApplyAll = onApplyAll,
                )
            }

            MoreRulesGroupRow(
                selectedRule = selectedMoreRule,
                expanded = showMoreRules,
                isBusy = isBusy,
                isGeneratingGptCandidate = isGeneratingGptCandidate,
                isGeneratingRmbgCandidate = isGeneratingRmbgCandidate,
                onToggle = { showMoreRules = !showMoreRules },
            )
            AnimatedVisibility(
                visible = showMoreRules,
                enter = fadeIn(animationSpec = tween(durationMillis = 150)) +
                    expandVertically(animationSpec = tween(durationMillis = 180)),
                exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                    shrinkVertically(animationSpec = tween(durationMillis = 160)),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    moreChoices.forEach { choice ->
                        if (shouldShowPreviewChoiceRow(choice, session)) {
                            PreviewChoiceRow(
                                mode = mode,
                                choice = choice,
                                session = session,
                                tuningState = tuningState,
                                isBusy = isBusy,
                                isGeneratingGptCandidate = isGeneratingGptCandidate,
                                isGeneratingRmbgCandidate = isGeneratingRmbgCandidate,
                                rmbgCandidatePackageName = rmbgCandidatePackageName,
                                rmbgCandidateMode = rmbgCandidateMode,
                                rmbgCandidateFailurePackageName = rmbgCandidateFailurePackageName,
                                rmbgCandidateFailureMode = rmbgCandidateFailureMode,
                                lastRmbgCandidateError = lastRmbgCandidateError,
                                rmbgCandidateStatusText = rmbgCandidateStatusText,
                                gptBaseUrl = gptBaseUrl,
                                gptApiKey = gptApiKey,
                                hasRmbgComponent = hasRmbgComponent,
                                cornerRadiusDp = cornerRadiusDp,
                                materialColorProvider = materialColorProvider,
                                loadCandidateAssets = loadCandidateAssets,
                                onGenerateGpt = onGenerateGpt,
                                onGenerateRmbg = onGenerateRmbg,
                                onChooseCustom = onChooseCustom,
                                onApply = onApply,
                                onApplyAll = onApplyAll,
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = !showMoreRules && selectedMoreRule != null,
                enter = fadeIn(animationSpec = tween(durationMillis = 150)) +
                    expandVertically(animationSpec = tween(durationMillis = 180)),
                exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                    shrinkVertically(animationSpec = tween(durationMillis = 160)),
            ) {
                moreChoices
                    .firstOrNull { it == selectedMoreRule && shouldShowPreviewChoiceRow(it, session) }
                    ?.let { choice ->
                        PreviewChoiceRow(
                            mode = mode,
                            choice = choice,
                            session = session,
                            tuningState = tuningState,
                            isBusy = isBusy,
                            isGeneratingGptCandidate = isGeneratingGptCandidate,
                            isGeneratingRmbgCandidate = isGeneratingRmbgCandidate,
                            rmbgCandidatePackageName = rmbgCandidatePackageName,
                            rmbgCandidateMode = rmbgCandidateMode,
                            rmbgCandidateFailurePackageName = rmbgCandidateFailurePackageName,
                            rmbgCandidateFailureMode = rmbgCandidateFailureMode,
                            lastRmbgCandidateError = lastRmbgCandidateError,
                            rmbgCandidateStatusText = rmbgCandidateStatusText,
                            gptBaseUrl = gptBaseUrl,
                            gptApiKey = gptApiKey,
                            hasRmbgComponent = hasRmbgComponent,
                            cornerRadiusDp = cornerRadiusDp,
                            materialColorProvider = materialColorProvider,
                            loadCandidateAssets = loadCandidateAssets,
                            onGenerateGpt = onGenerateGpt,
                            onGenerateRmbg = onGenerateRmbg,
                            onChooseCustom = onChooseCustom,
                            onApply = onApply,
                            onApplyAll = onApplyAll,
                        )
                    }
            }
        }
    }
}

