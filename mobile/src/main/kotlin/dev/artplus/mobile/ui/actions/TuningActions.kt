package dev.artplus.mobile.ui.actions

import android.content.Context
import androidx.compose.runtime.Stable
import dev.artplus.mobile.MainActivity
import dev.artplus.mobile.PREFS_NAME
import dev.artplus.mobile.PREVIEW_OUTPUT_DEBOUNCE_MS
import dev.artplus.mobile.PREVIEW_REBUILD_DEBOUNCE_MS
import dev.artplus.mobile.imaging.IconCandidate
import dev.artplus.mobile.imaging.LocalSeparationMode
import dev.artplus.mobile.imaging.PreviewChoice
import dev.artplus.mobile.imaging.normalizePreviewSelections
import dev.artplus.mobile.pipeline.GenerationSession
import dev.artplus.mobile.pipeline.rebuildLocalSession
import dev.artplus.mobile.pipeline.rmbgTunedForegroundRaw
import dev.artplus.mobile.state.persistUiState
import dev.artplus.mobile.state.setStatus
import dev.artplus.mobile.system.writePackageOutputs
import dev.artplus.mobile.ui.cards.PreviewSelections
import dev.artplus.mobile.ui.pages.home.homeRefreshActivePreviewOutputs
import dev.artplus.mobile.ui.pages.params.paramsSaveLiquidGlassSettings
import dev.artplus.mobile.ui.pages.picker.pickerPostStatus

/**
 * S2.2：主页「写入 / 保存 / 刷新」三件事的唯一入口。
 *
 * 收口前：43 份 onWrite 写入块复制粘贴（每加一个调参字段要改 43 处）、
 * 29 处液态玻璃保存调用内联、37 份预览刷新块复制粘贴。
 * 收口后：Activity 内 `tuningActions` 构造一次，调用点一律 `tuningActions.write` /
 * `tuningActions.save()` / `tuningActions.refresh`。
 *
 * 参数一律在调用时从 `mainViewModel` 现取，与收口前各调用点的读取表达式逐字一致。
 */
@Stable
internal class TuningActions(
    val write: (GenerationSession, PreviewSelections) -> Unit,
    val save: () -> Unit,
    val refresh: (Boolean) -> Unit,
)

internal fun MainActivity.buildTuningActions(): TuningActions {
    val write: (GenerationSession, PreviewSelections) -> Unit = { session, selections -> run {

                    val params = mainViewModel.params.value
                    writePackageOutputs(
                        session = (session),
                        selections = (selections),
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
                        monochromeThemeScale = params.monochromeThemeScale,
                        nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
                    )
        } }
    val refresh: (Boolean) -> Unit = { rebuild ->
            homeRefreshActivePreviewOutputs(
                        currentSession = mainViewModel.previewSession.value.activeGenerationSession,
                        rebuildLocalCandidates = (rebuild),
                        retargetFrom = (null),
                        app = mainViewModel.previewSession.value.activeGenerationSession?.let { s -> apps.firstOrNull { it.packageName == s.packageName } },
                        currentSelections = PreviewSelections.fromNames(mainViewModel.params.value.previewNormalLight, mainViewModel.params.value.previewNormalDark, mainViewModel.params.value.previewMonochromeLight, mainViewModel.params.value.previewMonochromeDark),
                        scope = previewWorkerScope,
                        getJob = { previewOutputJob },
                        setJob = { previewOutputJob = it },
                        incRevision = { ++previewOutputRevision },
                        getRevision = { previewOutputRevision },
                        setRefreshing = { mainViewModel.updatePreviewSession { v -> v.copy(isPreviewOutputRefreshing = (it)) } },
                        rebuildDebounceMs = PREVIEW_REBUILD_DEBOUNCE_MS,
                        outputDebounceMs = PREVIEW_OUTPUT_DEBOUNCE_MS,
                        tuning = mainViewModel.params.value,
                        onRebuild = { session, app, tuning -> rebuildLocalSession(session, app, packageManager, tuning) },
                        defaultLocal = { auto -> run {
                    when ((LocalSeparationMode.Auto)) {
                                LocalSeparationMode.Original -> PreviewChoice.Original
                                LocalSeparationMode.Plate -> PreviewChoice.Full
                                LocalSeparationMode.ComposedBackground -> PreviewChoice.ComposedBackground
                                LocalSeparationMode.ComponentSubject -> PreviewChoice.ComponentSubject
                                LocalSeparationMode.ComponentBackground -> PreviewChoice.ComponentBackground
                                LocalSeparationMode.Auto -> ((auto))
                                LocalSeparationMode.Full -> PreviewChoice.Full
                            }
                
            } },
                        normalize = { session, selections -> normalizePreviewSelections(session, selections) },
                        onWrite = write,
                        onCommit = { session, selections ->
                            mainViewModel.updatePreviewSession { it -> it.copy(activeGenerationSession = (session)) }
                            mainViewModel.updateLive { p -> p.copy(previewNormalLight = (selections).normalLight.name, previewNormalDark = (selections).normalDark.name, previewMonochromeLight = (selections).monochromeLight.name, previewMonochromeDark = (selections).monochromeDark.name) }
                            mainViewModel.updatePreviewSession { it -> it.copy(previewVersion = it.previewVersion + (1)) }
                            persistUiState()
                        },
                        onStatus = { pickerPostStatus((it)) { runOnUiThread { setStatus((it)) } } },
                    )
         }
    return TuningActions(
        write = write,
        save = {
            paramsSaveLiquidGlassSettings(
                        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                        getParams = { mainViewModel.params.value },
                    )
        },
        refresh = refresh,
    )
}
