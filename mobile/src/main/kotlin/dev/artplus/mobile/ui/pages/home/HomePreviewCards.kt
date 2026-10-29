package dev.artplus.mobile.ui.pages.home

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.CardDefaults
import io.github.suqi8.coui.kmp.preference.SwitchPreference
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.theme.COUITheme
import android.graphics.Color as AndroidColor
import dev.artplus.mobile.MAX_FOREGROUND_SUBJECT_PERCENT
import dev.artplus.mobile.MAX_PREVIEW_CORNER_RADIUS_DP
import dev.artplus.mobile.MAX_PREVIEW_ICON_SIZE_DP
import dev.artplus.mobile.MIN_FOREGROUND_SUBJECT_PERCENT
import dev.artplus.mobile.MIN_PREVIEW_CORNER_RADIUS_DP
import dev.artplus.mobile.MIN_PREVIEW_ICON_SIZE_DP
import dev.artplus.mobile.TuningParams
import dev.artplus.mobile.imaging.CustomImageKind
import dev.artplus.mobile.imaging.IconCandidate
import dev.artplus.mobile.imaging.PreviewChoice
import dev.artplus.mobile.imaging.candidateForChoice
import dev.artplus.mobile.pipeline.GenerationSession
import dev.artplus.mobile.pipeline.PreviewAssets
import dev.artplus.mobile.pipeline.PreviewMode
import dev.artplus.mobile.ui.cards.AiIconLoadingPreview
import dev.artplus.mobile.ui.cards.NumberParameterControl
import dev.artplus.mobile.ui.cards.PreviewDesktopBackground
import dev.artplus.mobile.ui.cards.PreviewSelections
import dev.artplus.mobile.ui.components.LayerDebugTile
import dev.artplus.mobile.ui.components.LibrarySettingRow
import dev.artplus.mobile.ui.components.SectionCard
import dev.artplus.mobile.ui.components.SectionDivider
import dev.artplus.mobile.ui.components.SettingsIconKind
/**
 * Slice 2.2：主页预览卡片（原 MainActivity 残留本体原样搬迁）。
 * Composable 只收 state + onEvent；Activity 状态经参数/回调注入，行为与 UI 100% 等价。
 */

@Composable
internal fun GeneratedPreviewCard(
    dirPath: String?,
    packageName: String?,
    session: GenerationSession?,
    displayAssets: PreviewAssets?,
    previewLoading: Boolean,
    desktopBackground: PreviewDesktopBackground,
    iconSizeDp: Int,
    cornerRadiusDp: Int,
    wallpaperInitial: Bitmap?,
    wallpaperKey: String?,
    loadWallpaper: suspend () -> Bitmap?,
    materialColorProvider: (String, Color) -> Color,
    previewChoiceMode: PreviewMode?,
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
    loadCandidateAssets: suspend (IconCandidate, PreviewMode?) -> PreviewAssets?,
    onChoiceClick: (PreviewMode) -> Unit,
    onNightFill: (Boolean) -> Unit,
    onDraftForegroundSubjectPercent: (String) -> Unit,
    onSaveForegroundSubjectPercent: (Int) -> Unit,
    onGenerateGpt: (PreviewMode) -> Unit,
    onGenerateRmbg: (PreviewMode) -> Unit,
    onChooseCustom: (PreviewMode, CustomImageKind) -> Unit,
    onApplyPreviewChoice: (PreviewMode, PreviewChoice) -> Unit,
    onApplyPreviewChoiceToAll: (PreviewChoice) -> Unit,
    onDismissChoice: () -> Unit,
    onDismissChoiceFinished: () -> Unit,
) {
    if (dirPath == null) return
    if (packageName == null) return

    SectionCard {
        if (displayAssets == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AiIconLoadingPreview(modifier = Modifier.size(42.dp), overlay = true)
                Text(
                    text = "加载预览中",
                    style = COUITheme.textStyles.footnote1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            return@SectionCard
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PreviewTile(
                label = "标准亮色",
                assets = displayAssets,
                mode = PreviewMode.NormalLight,
                desktopBackground = desktopBackground,
                iconSizeDp = iconSizeDp,
                loading = previewLoading,
                choiceEnabled = session != null,
                cornerRadiusDp = cornerRadiusDp,
                wallpaperInitial = wallpaperInitial,
                wallpaperKey = wallpaperKey,
                loadWallpaper = loadWallpaper,
                materialColorProvider = materialColorProvider,
                onClick = { onChoiceClick(PreviewMode.NormalLight) },
                modifier = Modifier.weight(1f),
            )
            PreviewTile(
                label = "标准暗色",
                assets = displayAssets,
                mode = PreviewMode.NormalDark,
                desktopBackground = desktopBackground,
                iconSizeDp = iconSizeDp,
                loading = previewLoading,
                choiceEnabled = session != null,
                cornerRadiusDp = cornerRadiusDp,
                wallpaperInitial = wallpaperInitial,
                wallpaperKey = wallpaperKey,
                loadWallpaper = loadWallpaper,
                materialColorProvider = materialColorProvider,
                onClick = { onChoiceClick(PreviewMode.NormalDark) },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PreviewTile(
                label = "单色亮色",
                assets = displayAssets,
                mode = PreviewMode.MonochromeLight,
                desktopBackground = desktopBackground,
                iconSizeDp = iconSizeDp,
                loading = previewLoading,
                choiceEnabled = session != null,
                cornerRadiusDp = cornerRadiusDp,
                wallpaperInitial = wallpaperInitial,
                wallpaperKey = wallpaperKey,
                loadWallpaper = loadWallpaper,
                materialColorProvider = materialColorProvider,
                onClick = { onChoiceClick(PreviewMode.MonochromeLight) },
                modifier = Modifier.weight(1f),
            )
            PreviewTile(
                label = "单色暗色",
                assets = displayAssets,
                mode = PreviewMode.MonochromeDark,
                desktopBackground = desktopBackground,
                iconSizeDp = iconSizeDp,
                loading = previewLoading,
                choiceEnabled = session != null,
                cornerRadiusDp = cornerRadiusDp,
                wallpaperInitial = wallpaperInitial,
                wallpaperKey = wallpaperKey,
                loadWallpaper = loadWallpaper,
                materialColorProvider = materialColorProvider,
                onClick = { onChoiceClick(PreviewMode.MonochromeDark) },
                modifier = Modifier.weight(1f),
            )
        }
        var activeChooserMode by remember { mutableStateOf<PreviewMode?>(null) }
        var activeChooserSession by remember { mutableStateOf<GenerationSession?>(null) }
        if (previewChoiceMode != null && session != null) {
            activeChooserMode = previewChoiceMode
            activeChooserSession = session
        }
        val currentChooserMode = activeChooserMode
        val currentChooserSession = activeChooserSession
        if (currentChooserMode != null && currentChooserSession != null) {
            // 原 GeneratedPreviewCard 的 activeChooser 保持逻辑：关闭完成后若外部已置空则清理，避免残留。
            PreviewChoiceBottomSheet(
                show = previewChoiceMode != null && session != null,
                mode = currentChooserMode,
                session = currentChooserSession,
                tuningState = tuningState,
                isBusy = isBusy,
                isGeneratingGptCandidate = isGeneratingGptCandidate,
                isGeneratingRmbgCandidate = isGeneratingRmbgCandidate,
                draftForegroundSubjectPercentText = draftForegroundSubjectPercentText,
                isDark = isDark,
                nightSubjectLightBackgroundEnabled = nightSubjectLightBackgroundEnabled,
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
                onNightFill = onNightFill,
                onDraftForegroundSubjectPercent = onDraftForegroundSubjectPercent,
                onSaveForegroundSubjectPercent = onSaveForegroundSubjectPercent,
                onGenerateGpt = onGenerateGpt,
                onGenerateRmbg = onGenerateRmbg,
                onChooseCustom = onChooseCustom,
                onApply = onApplyPreviewChoice,
                onApplyAll = onApplyPreviewChoiceToAll,
                onDismissRequest = onDismissChoice,
                onDismissFinished = {
                    if (previewChoiceMode == null) {
                        activeChooserMode = null
                        activeChooserSession = null
                    }
                    onDismissChoiceFinished()
                },
            )
        }
    }
}

/** 调试图层卡片：前景 / 背景 / alpha 蒙版小图。 */
@Composable
internal fun LayerDebugCard(
    dirPath: String?,
    packageName: String?,
    session: GenerationSession?,
    assets: PreviewAssets?,
    tuningState: TuningParams,
) {
    if (dirPath == null) return
    if (packageName == null) return
    if (session == null) return
    if (assets == null) return

    val choice = PreviewSelections.fromNames(tuningState.previewNormalLight, tuningState.previewNormalDark, tuningState.previewMonochromeLight, tuningState.previewMonochromeDark).normalLight
    val candidate = candidateForChoice(session, choice)
    val alphaMask = remember(candidate) {
        candidate?.recfgRaw?.let { bitmap ->
            val width = bitmap.width
            val height = bitmap.height
            if (width <= 0 || height <= 0) {
                null
            } else {
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                val outPixels = IntArray(pixels.size)
                for (i in pixels.indices) {
                    val alpha = AndroidColor.alpha(pixels[i])
                    outPixels[i] = AndroidColor.rgb(alpha, alpha, alpha)
                }
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { out ->
                    out.setPixels(outPixels, 0, width, 0, 0, width, height)
                }
            }
        }
    }

    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LayerDebugTile(label = "前景", bitmap = assets.recfg?.asImageBitmap(), modifier = Modifier.weight(1f))
            LayerDebugTile(label = "背景", bitmap = assets.recbg?.asImageBitmap(), modifier = Modifier.weight(1f))
            LayerDebugTile(label = "蒙版", bitmap = alphaMask?.asImageBitmap(), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
internal fun PreviewControlCard(
    tuningState: TuningParams,
    isBusy: Boolean,
    previewCornerRadiusDp: Int,
    draftPreviewCornerRadiusDpText: String,
    previewIconSizeDp: Int,
    draftPreviewIconSizeDpText: String,
    draftForegroundSubjectPercentText: String,
    previewStripEnabled: Boolean,
    previewDesktopBackground: PreviewDesktopBackground,
    wallpaperInitial: Bitmap?,
    wallpaperKey: String?,
    loadWallpaper: suspend () -> Bitmap?,
    onDraftForegroundSubjectPercent: (String) -> Unit,
    onSaveForegroundSubjectPercent: (Int) -> Unit,
    onDraftPreviewCornerRadiusDp: (String) -> Unit,
    onSavePreviewCornerRadiusDp: (Int) -> Unit,
    onDraftPreviewIconSizeDp: (String) -> Unit,
    onSavePreviewIconSizeDp: (Int) -> Unit,
    onPreviewStripEnabled: (Boolean) -> Unit,
    onPreviewDesktopBackground: (PreviewDesktopBackground) -> Unit,
) {
    SectionCard(rowsFullBleed = true) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            NumberParameterControl(
                busy = isBusy,
                title = "前景主体大小",
                summary = "控制前景主体在图标画布中的占比",
                value = tuningState.foregroundSubjectPercent,
                draftText = draftForegroundSubjectPercentText,
                min = MIN_FOREGROUND_SUBJECT_PERCENT,
                max = MAX_FOREGROUND_SUBJECT_PERCENT,
                step = 1,
                onDraftChange = onDraftForegroundSubjectPercent,
                onSave = onSaveForegroundSubjectPercent,
                icon = SettingsIconKind.Scale,
            )
            SectionDivider()
            NumberParameterControl(
                busy = isBusy,
                title = "预览圆角",
                summary = "控制预览图标的圆角大小",
                value = previewCornerRadiusDp,
                draftText = draftPreviewCornerRadiusDpText,
                min = MIN_PREVIEW_CORNER_RADIUS_DP,
                max = MAX_PREVIEW_CORNER_RADIUS_DP,
                step = 1,
                onDraftChange = onDraftPreviewCornerRadiusDp,
                onSave = onSavePreviewCornerRadiusDp,
                icon = SettingsIconKind.Radius,
            )
            SectionDivider()
            NumberParameterControl(
                busy = isBusy,
                title = "预览缩放",
                summary = "预览图显示大小",
                value = previewIconSizeDp,
                draftText = draftPreviewIconSizeDpText,
                min = MIN_PREVIEW_ICON_SIZE_DP,
                max = MAX_PREVIEW_ICON_SIZE_DP,
                step = 1,
                onDraftChange = onDraftPreviewIconSizeDp,
                onSave = onSavePreviewIconSizeDp,
                icon = SettingsIconKind.Grid,
            )
            SectionDivider()
            LibrarySettingRow(
                title = "顶部 1×4 预览条",
                summary = "在主页、生成参数与预设页置顶显示",
                icon = SettingsIconKind.Palette,
                showSwitch = true,
                checked = previewStripEnabled,
                enabled = !isBusy,
                onCheckedChange = onPreviewStripEnabled,
            )
            SectionDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PreviewDesktopBackground.entries.forEach { option ->
                    PreviewBackgroundOption(
                        option = option,
                        selected = option == previewDesktopBackground,
                        isBusy = isBusy,
                        wallpaperInitial = wallpaperInitial,
                        wallpaperKey = wallpaperKey,
                        loadWallpaper = loadWallpaper,
                        modifier = Modifier.weight(1f),
                        onClick = { onPreviewDesktopBackground(option) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun PreviewNightFillBackgroundRow(
    checked: Boolean,
    isBusy: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    // COUI 开关行：整行点击切换，放在中性 secondaryVariant 卡片里（所在底部弹层面板为白色）
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(0.dp),
        colors = CardDefaults.defaultColors(color = COUITheme.colorScheme.secondaryVariant),
    ) {
        SwitchPreference(
            checked = checked,
            onCheckedChange = onCheckedChange,
            title = "填充背景色",
            summary = "将暗色背景颜色填补到主体暗部",
            enabled = !isBusy,
        )
    }
}
