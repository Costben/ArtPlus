package dev.artplus.mobile.ui.pages.tabs

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Save
import dev.artplus.mobile.MainActivity
import dev.artplus.mobile.PREFS_NAME
import dev.artplus.mobile.data.AppEntry
import dev.artplus.mobile.state.persistUiState
import dev.artplus.mobile.state.setStatus
import dev.artplus.mobile.ui.components.TitleBarIconButton
import dev.artplus.mobile.ui.pages.PagerShellPage
import dev.artplus.mobile.ui.pages.SettingsPage
import dev.artplus.mobile.ui.pages.params.paramsSaveGptSettings
import dev.artplus.mobile.ui.pages.params.paramsSaveImageTuningSettings
import dev.artplus.mobile.ui.pages.params.paramsSaveLocalSeparationSettings
import dev.artplus.mobile.ui.pages.params.paramsSaveRmbgSettings
import dev.artplus.mobile.ui.pages.params.paramsSaveSettingsPage

/**
 * S4.x：从 HomePage 的 `when (page)` 分支原样搬出的 tab 页（纯移动，key/remember 原样保留）。
 * 状态仍由 mainViewModel 单源提供，本页只订阅自己用到的 flow。
 */
@Composable
internal fun MainActivity.SettingsTabPage(
    pageBackground: Color,
    selectedApp: AppEntry?,
    launcherCount: Int,
    totalCount: Int,
    generatedCount: Int,
) {
    val shellState by mainViewModel.shell.collectAsState()

PagerShellPage(
                    title = "设置",
                    actions = {
                        TitleBarIconButton(
                            icon = Lucide.Save,
                            contentDescription = "保存",
                            // 热修复2：渲染位经已订阅 shellState 读取。
                            enabled = !shellState.isBusy,
                            onClick = { run {
    paramsSaveSettingsPage(
                saveGpt = { run {
        paramsSaveGptSettings(
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    getParams = { mainViewModel.params.value },
                    getGptApiKey = { mainViewModel.gptRmbgSettings.value.gptApiKey },
                    getGptModelId = { mainViewModel.gptRmbgSettings.value.gptModelId },
                    getGptBaseUrl = { mainViewModel.gptRmbgSettings.value.gptBaseUrl },
                )
    } },
                saveRmbg = { run {
        paramsSaveRmbgSettings(
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    getComponentUrl = { mainViewModel.gptRmbgSettings.value.rmbgComponentUrl },
                )
    } },
                saveLocalSeparation = { run {
        paramsSaveLocalSeparationSettings(
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    getParams = { mainViewModel.params.value },
                )
    } },
                saveImageTuning = { run {
        paramsSaveImageTuningSettings(
                    prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    getParams = { mainViewModel.params.value },
                )
    } },
                saveLiquidGlass = { tuningActions.save() },
                saveUi = { persistUiState() },
                setGptSaveStatus = { mainViewModel.updateGptRmbgSettings { v -> v.copy(gptSettingsSaveStatus = (it)) } },
                setRmbgSaveStatus = { mainViewModel.updateGptRmbgSettings { v -> v.copy(rmbgComponentSaveStatus = (it)) } },
                setStatusText = { setStatus((it)) },
            )
} },
                        )
                    },
                ) { innerPadding, scrollBehavior ->
                    SettingsPage(
                        innerPadding = innerPadding,
                        scrollBehavior = scrollBehavior,
                        launcherCount = launcherCount,
                        totalCount = totalCount,
                        generatedCount = generatedCount,
                    )
                }
}
