package dev.artplus.mobile.ui.pages.home

import androidx.compose.runtime.Composable
import dev.artplus.mobile.system.RootWriteMode
import dev.artplus.mobile.ui.cards.AdvancedSettingsTab
import io.github.suqi8.coui.kmp.basic.DropdownEntry
import io.github.suqi8.coui.kmp.basic.DropdownItem
import io.github.suqi8.coui.kmp.basic.PopupPositionProvider
import io.github.suqi8.coui.kmp.overlay.OverlayNavigationPopupMenu

/**
 * 底栏两个融合球点开的菜单。
 *
 * 用 COUI 的 `OverlayNavigationPopupMenu`：底栏溢出菜单的定位（贴在锚点上方、按锚点边缘对齐、
 * 8dp 间隙、夹在安全区内）和材质（玻璃、投影、边光）都在库里，应用侧不再自带定位器和补光。
 *
 * 两个菜单都必须声明在球内部（`PagerShellActionBall` 的插槽），COUI 用一个 0 尺寸 Spacer
 * 量父节点的窗口矩形当锚点，父节点就是那个 56dp 的球，因此卡片从球的上沿长出来。
 *
 * 菜单由 `ArtPlusScreen` 根部 Scaffold 的 `COUIPopupHost` 渲染，那里的 `LocalBackdrop` 是整屏快照，
 * 玻璃和高光就从它采样。
 */

/** 生成菜单：本地管线 / 本地导出 / RMBG 抠图 / 云端大模型。左下角的球，菜单贴球的左边缘。 */
@Composable
internal fun GenerateActionMenu(
    show: Boolean,
    canGenerate: Boolean,
    canExport: Boolean,
    canRmbg: Boolean,
    canGpt: Boolean,
    onLocalGenerate: () -> Unit,
    onLocalExport: () -> Unit,
    onRmbgGenerate: () -> Unit,
    onGptGenerate: () -> Unit,
    onDismissRequest: () -> Unit,
    onDismissFinished: () -> Unit,
) {
    OverlayNavigationPopupMenu(
        expanded = show,
        entries = listOf(
            DropdownEntry(
                items = listOf(
                    DropdownItem(
                        text = "本地生成",
                        enabled = canGenerate,
                        onClick = onLocalGenerate,
                    ),
                    DropdownItem(
                        text = "本地导出",
                        enabled = canExport,
                        onClick = onLocalExport,
                    ),
                    DropdownItem(
                        text = "RMBG 生成",
                        enabled = canRmbg,
                        onClick = onRmbgGenerate,
                    ),
                    DropdownItem(
                        text = "大模型生成",
                        enabled = canGpt,
                        onClick = onGptGenerate,
                    ),
                ),
            ),
        ),
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        alignment = PopupPositionProvider.Align.BottomStart,
    )
}

/** 写入菜单：按套别拆分，只覆盖选中的那几套，不动目标目录里的其他文件。右下角的球，菜单贴球的右边缘。 */
@Composable
internal fun WriteActionMenu(
    show: Boolean,
    enabled: Boolean,
    onWrite: (RootWriteMode) -> Unit,
    onDismissRequest: () -> Unit,
    onDismissFinished: () -> Unit,
) {
    OverlayNavigationPopupMenu(
        expanded = show,
        entries = listOf(
            DropdownEntry(
                items = listOf(
                    DropdownItem(
                        text = "应用全部",
                        enabled = enabled,
                        onClick = { onWrite(RootWriteMode.All) },
                    ),
                    DropdownItem(
                        text = "应用标准亮色",
                        enabled = enabled,
                        onClick = { onWrite(RootWriteMode.LightOnly) },
                    ),
                    DropdownItem(
                        text = "应用标准暗色",
                        enabled = enabled,
                        onClick = { onWrite(RootWriteMode.DarkOnly) },
                    ),
                    DropdownItem(
                        text = "应用单色亮色",
                        enabled = enabled,
                        onClick = { onWrite(RootWriteMode.MonochromeLightOnly) },
                    ),
                    DropdownItem(
                        text = "应用单色暗色",
                        enabled = enabled,
                        onClick = { onWrite(RootWriteMode.MonochromeDarkOnly) },
                    ),
                ),
            ),
        ),
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        alignment = PopupPositionProvider.Align.BottomEnd,
    )
}

/** 生成参数页左下球的菜单：在「可视化」和「JSON」之间切。当前模式带勾。 */
@Composable
internal fun ParamsModeMenu(
    show: Boolean,
    current: AdvancedSettingsTab,
    onSelect: (AdvancedSettingsTab) -> Unit,
    onDismissRequest: () -> Unit,
) {
    OverlayNavigationPopupMenu(
        expanded = show,
        entries = listOf(
            DropdownEntry(
                items = AdvancedSettingsTab.entries.map { tab ->
                    DropdownItem(
                        text = tab.label,
                        selected = tab == current,
                        onClick = { onSelect(tab) },
                    )
                },
            ),
        ),
        onDismissRequest = onDismissRequest,
        alignment = PopupPositionProvider.Align.BottomStart,
    )
}
