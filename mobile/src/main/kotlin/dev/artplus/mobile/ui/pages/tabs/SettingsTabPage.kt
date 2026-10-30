package dev.artplus.mobile.ui.pages.tabs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import dev.artplus.mobile.ui.cards.AppPage
import dev.artplus.mobile.MainActivity
import dev.artplus.mobile.data.AppEntry
import dev.artplus.mobile.ui.components.TitleBarIconButton
import dev.artplus.mobile.ui.pages.PagerShellPage
import dev.artplus.mobile.ui.pages.SettingsPage
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.ChevronBackward

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
        navigationIcon = {
            TitleBarIconButton(
                icon = COUIIcons.ChevronBackward,
                contentDescription = "返回",
                enabled = !shellState.isBusy,
                onClick = { mainViewModel.setPage(AppPage.Home) },
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
