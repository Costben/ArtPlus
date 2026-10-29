package dev.artplus.mobile.ui.cards

import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import java.io.File
import dev.artplus.mobile.PREF_PREVIEW_SELECTION_MONOCHROME_DARK
import dev.artplus.mobile.PREF_PREVIEW_SELECTION_MONOCHROME_LIGHT
import dev.artplus.mobile.PREF_PREVIEW_SELECTION_NORMAL_DARK
import dev.artplus.mobile.PREF_PREVIEW_SELECTION_NORMAL_LIGHT
import dev.artplus.mobile.imaging.CandidateBuildResult
import dev.artplus.mobile.imaging.PreviewChoice
import dev.artplus.mobile.imaging.RmbgInferenceReport
import dev.artplus.mobile.pipeline.GenerationSession
import dev.artplus.mobile.pipeline.PreviewMode
import dev.artplus.mobile.system.RootWriteMode

internal data class UpdateInfo(
    val latestVersion: String,
    val tagName: String,
    val htmlUrl: String,
)

/** 调用 AI/RMBG 前的二次确认请求。 */
internal data class ServiceConfirmRequest(
    val title: String,
    val message: String,
    val confirmLabel: String,
    val onConfirm: () -> Unit,
)

/** 写入 Root 目标目录前的二次确认请求。 */
internal data class RootWriteConfirmRequest(
    val packageName: String,
    val targetPath: String,
    val rootWriteMode: RootWriteMode,
    val onConfirm: () -> Unit,
)

internal data class BatchApplyProgress(
    val title: String,
    val completed: Int,
    val total: Int,
    val currentLabel: String,
    val failures: Int,
)

internal data class ExportProgress(
    val title: String,
    val completed: Int,
    val total: Int,
    val currentLabel: String,
    val isIndeterminate: Boolean = false,
)


internal enum class AdvancedSettingsCategory(val label: String) {
    Local("稳定规则"),
    Rmbg("RMBG");

    companion object {
        fun fromName(name: String?): AdvancedSettingsCategory =
            entries.firstOrNull { it.name == name } ?: Local
    }
}

internal enum class PreviewDesktopBackground(val label: String, val fallbackColor: Color) {
    LightGray("浅灰", Color(0xFFE8E8E8)),
    DarkGray("深灰", Color(0xFF4A4A4A)),
    Black("纯黑", Color(0xFF050505)),
    Wallpaper("桌面", Color(0xFF30333A));

    companion object {
        fun fromName(name: String?): PreviewDesktopBackground =
            entries.firstOrNull { it.name == name } ?: DarkGray
    }
}

internal enum class AppPage(val order: Int) {
    Home(0),
    AppPicker(1),
    About(2),
    BatchPreview(3),
    Settings(4),
}

internal enum class GeneratedFilter(val label: String) {
    All("全部"),
    Generated("已生成"),
    Ungenerated("未生成");

    companion object {
        fun fromName(name: String?): GeneratedFilter =
            entries.firstOrNull { it.name == name } ?: All
    }
}

internal enum class AdvancedSettingsTab(val label: String) {
    Sliders("可视化"),
    Json("JSON"),
}

internal data class RmbgDebugCandidate(
    val foreground: Bitmap,
    val result: CandidateBuildResult?,
    val coverage: Double,
    val boundsText: String,
    val cropRisk: Boolean,
    val manualUsable: Boolean,
    val inference: RmbgInferenceReport,
)

internal data class RmbgComponent(
    val dir: File,
    val abi: String,
    val model: File,
) {
    val key: String = listOf(
        dir.absolutePath,
        abi,
        model.length(),
        model.lastModified(),
    ).joinToString("|")
}

internal data class RmbgModelOutput(
    val output: FloatArray,
    val report: RmbgInferenceReport,
)

internal data class RmbgMaskResult(
    val alpha: IntArray,
    val report: RmbgInferenceReport,
)

internal data class BatchPreviewProgress(
    val presetName: String,
    val completed: Int,
    val total: Int,
    val currentLabel: String,
)


internal data class ForegroundShadowParams(
    val alpha: Int,
    val blurRadius: Float,
    val offsetX: Int,
    val offsetY: Int,
    val spread: Int,
)

internal data class GenerationResult(
    val outDir: File,
    val session: GenerationSession,
    val selections: PreviewSelections,
)

internal data class PreviewSelections(
    val normalLight: PreviewChoice,
    val normalDark: PreviewChoice,
    val monochromeLight: PreviewChoice,
    val monochromeDark: PreviewChoice,
) {
    fun choiceFor(mode: PreviewMode): PreviewChoice =
        when (mode) {
            PreviewMode.NormalLight -> normalLight
            PreviewMode.NormalDark -> normalDark
            PreviewMode.MonochromeLight -> monochromeLight
            PreviewMode.MonochromeDark -> monochromeDark
        }

    fun withChoice(mode: PreviewMode, choice: PreviewChoice): PreviewSelections =
        when (mode) {
            PreviewMode.NormalLight -> copy(normalLight = choice)
            PreviewMode.NormalDark -> copy(normalDark = choice)
            PreviewMode.MonochromeLight -> copy(monochromeLight = choice)
            PreviewMode.MonochromeDark -> copy(monochromeDark = choice)
        }

    fun retarget(from: PreviewChoice, to: PreviewChoice): PreviewSelections {
        if (from == to) {
            return this
        }
        return PreviewSelections(
            normalLight = replaceDefault(normalLight, from, to),
            normalDark = replaceDefault(normalDark, from, to),
            monochromeLight = replaceDefault(monochromeLight, from, to),
            monochromeDark = replaceDefault(monochromeDark, from, to),
        )
    }

    private fun replaceDefault(
        choice: PreviewChoice,
        from: PreviewChoice,
        to: PreviewChoice,
    ): PreviewChoice =
        if (choice == from) to else choice

    companion object {
        fun default(choice: PreviewChoice): PreviewSelections =
            PreviewSelections(
                normalLight = choice,
                normalDark = choice,
                monochromeLight = choice,
                monochromeDark = choice,
            )

        fun fromPrefs(prefs: android.content.SharedPreferences): PreviewSelections =
            PreviewSelections(
                normalLight = previewChoiceFromName(
                    prefs.getString(PREF_PREVIEW_SELECTION_NORMAL_LIGHT, null),
                ),
                normalDark = previewChoiceFromName(
                    prefs.getString(PREF_PREVIEW_SELECTION_NORMAL_DARK, null),
                ),
                monochromeLight = previewChoiceFromName(
                    prefs.getString(PREF_PREVIEW_SELECTION_MONOCHROME_LIGHT, null),
                ),
                monochromeDark = previewChoiceFromName(
                    prefs.getString(PREF_PREVIEW_SELECTION_MONOCHROME_DARK, null),
                ),
            )

        fun fromNames(
            normalLight: String?,
            normalDark: String?,
            monochromeLight: String?,
            monochromeDark: String?,
        ): PreviewSelections =
            PreviewSelections(
                normalLight = previewChoiceFromName(normalLight),
                normalDark = previewChoiceFromName(normalDark),
                monochromeLight = previewChoiceFromName(monochromeLight),
                monochromeDark = previewChoiceFromName(monochromeDark),
            )

        private fun previewChoiceFromName(name: String?): PreviewChoice =
            PreviewChoice.entries.firstOrNull { it.name == name }
                ?.let { if (it == PreviewChoice.Plate) PreviewChoice.Full else it }
                ?.takeUnless { it.isCustom }
                ?: PreviewChoice.Full
    }
}

internal data class RmbgModelPreset(
    val id: String,
    val label: String,
    val summary: String,
    val url: String,
)

/** 预设批量应用的落地方式。 */
internal enum class BatchOutputMode(val label: String) {
    Root("Root 写入"),
    Saf("SAF 导出"),
}
