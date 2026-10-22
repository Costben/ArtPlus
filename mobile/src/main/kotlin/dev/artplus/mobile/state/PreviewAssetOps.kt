package dev.artplus.mobile.state

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dev.artplus.mobile.MainActivity
import dev.artplus.mobile.PREVIEW_LIVE_ASSET_DEBOUNCE_MS
import dev.artplus.mobile.imaging.IconCandidate
import dev.artplus.mobile.imaging.previewAssetsForSelections
import dev.artplus.mobile.pipeline.PreviewAssets
import dev.artplus.mobile.pipeline.preparedForDraw
import dev.artplus.mobile.pipeline.rmbgTunedForegroundRaw
import dev.artplus.mobile.ui.cards.PreviewSelections
import dev.artplus.mobile.pipeline.GenerationSession

/*
 * S4.5：从 Shell.kt 原样搬出的预览资产 effect（纯移动，逻辑零改动）。
 * 由 ArtPlusScreen 以 `PreviewAssetsEffect(...)` 调用。
 */
@Composable
internal fun MainActivity.PreviewAssetsEffect(
    packageName: String?,
    dirPath: String?,
    session: GenerationSession?,
    previewVersion: Int,
    previewEffectKey: PreviewEffectKey,
) {
    LaunchedEffect(
        packageName,
        dirPath,
        session,
        previewVersion,
        previewEffectKey,
    ) {
        val dirPath = dirPath
        val packageName = packageName
        if (dirPath.isNullOrBlank() || packageName.isNullOrBlank()) {
            mainViewModel.updatePreviewSession { v -> v.copy(sharedPreviewAssets = (null), isPreviewAssetsRefreshing = (false)) }
            return@LaunchedEffect
        }
        mainViewModel.updatePreviewSession { it -> it.copy(isPreviewAssetsRefreshing = (true)) }
        try {
            delay(PREVIEW_LIVE_ASSET_DEBOUNCE_MS)
            val diskAssets = withContext(Dispatchers.IO) {
    PreviewAssets(
                recbg = BitmapFactory.decodeFile(File(((File(dirPath))), ("recbg.png")).absolutePath)?.also { it.prepareToDraw() },
                recfg = BitmapFactory.decodeFile(File(((File(dirPath))), ("recfg.png")).absolutePath)?.also { it.prepareToDraw() },
                recNight = BitmapFactory.decodeFile(File(((File(dirPath))), ("rec_night.png")).absolutePath)?.also { it.prepareToDraw() },
                monochromeLight = BitmapFactory.decodeFile(File(((File(dirPath))), ("monochrome_light.png")).absolutePath)?.also { it.prepareToDraw() }
                    ?: BitmapFactory.decodeFile(File(((File(dirPath))), ("monochrome.png")).absolutePath)?.also { it.prepareToDraw() },
                monochromeDark = BitmapFactory.decodeFile(File(((File(dirPath))), ("monochrome_dark.png")).absolutePath)?.also { it.prepareToDraw() }
                    ?: BitmapFactory.decodeFile(File(((File(dirPath))), ("monochrome.png")).absolutePath)?.also { it.prepareToDraw() },
            ).preparedForDraw()
            }
            val liveAssets = session?.let { session ->
                try {
                    withContext(previewWorkerDispatcher) {
                        run {

            val params = mainViewModel.params.value
            return@run previewAssetsForSelections(
                session = (session),
                selections = (PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark)),
                edgePolishPercent = params.edgePolishPercent,
                foregroundSubjectPercent = params.foregroundSubjectPercent,
                rmbgTunedForeground = { __a0: IconCandidate -> run {

                val params = mainViewModel.params.value
                return@run rmbgTunedForegroundRaw(
                    candidate = __a0,
                    rmbgAlphaStrengthPercent = params.rmbgAlphaStrengthPercent,
                    rmbgEdgeAdjustPercent = params.rmbgEdgeAdjustPercent,
                    rmbgEdgeFeatherPercent = params.rmbgEdgeFeatherPercent,
                    rmbgWeakAlphaKeepPercent = params.rmbgWeakAlphaKeepPercent,
                )
    } },
                liquidGlassEnabled = params.liquidGlassEnabled,
                liquidGlassBackgroundMistAlpha = params.liquidGlassBackgroundMistAlpha,
                liquidGlassTopAlpha = params.liquidGlassTopAlpha,
                liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
                liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
                liquidGlassOuterWidth = params.liquidGlassOuterWidth,
                liquidGlassRadius = params.liquidGlassRadius,
                liquidGlassSubjectScalePercent = params.liquidGlassSubjectScalePercent,
                liquidGlassSubjectShadowAlpha = params.liquidGlassSubjectShadowAlpha,
                liquidGlassSubjectOutlineWidth = params.liquidGlassSubjectOutlineWidth,
                liquidGlassSubjectInnerOutlineWidth = params.liquidGlassSubjectInnerOutlineWidth,
                liquidGlassSubjectOpacityPercent = params.liquidGlassSubjectOpacityPercent,
                foregroundShadowLevel = params.foregroundShadowLevel,
                nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
            )
}.preparedForDraw()
                    }
                } catch (_: CancellationException) {
                    throw CancellationException()
                } catch (_: Throwable) {
                    null
                }
            }
            mainViewModel.updatePreviewSession { it -> it.copy(sharedPreviewAssets = (liveAssets ?: diskAssets)) }
        } catch (_: CancellationException) {
            throw CancellationException()
        } catch (_: Throwable) {
            mainViewModel.updatePreviewSession { it -> it.copy(sharedPreviewAssets = (null)) }
        } finally {
            mainViewModel.updatePreviewSession { it -> it.copy(isPreviewAssetsRefreshing = (false)) }
        }
    }
}

internal data class PreviewEffectKey(
    val selections: PreviewSelections,
    val liquidGlassEnabled: Boolean,
    val liquidGlassBackgroundMistAlpha: Int,
    val liquidGlassTopAlpha: Int,
    val liquidGlassBottomAlpha: Int,
    val liquidGlassBottomDarkAlpha: Int,
    val liquidGlassOuterWidth: Int,
    val liquidGlassRadius: Int,
    val liquidGlassSubjectScalePercent: Int,
    val liquidGlassSubjectShadowAlpha: Int,
    val liquidGlassSubjectOutlineWidth: Int,
    val liquidGlassSubjectInnerOutlineWidth: Int,
    val liquidGlassSubjectOpacityPercent: Int,
    val foregroundShadowLevel: Int,
    val monochromeThemeScale: Float,
    val nightSubjectLightBackgroundEnabled: Boolean,
)
