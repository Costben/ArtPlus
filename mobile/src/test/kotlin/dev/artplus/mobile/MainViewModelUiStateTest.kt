package dev.artplus.mobile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Slice 3.1 状态收敛守卫（新增，不改既有 MainViewModelTest 14 项语义）：
 * 10 组具名 StateFlow 默认值必须与 MainActivity 基线逐字一致，
 * update* 写漏斗只改目标字段、不碰其他字段、不记历史。
 * 全部 JVM 直跑，无 Android 依赖调用（默认值均为 null/空/标量）。
 */
class MainViewModelUiStateTest {

    @Test
    fun picker_defaultsMatchActivityBaseline() {
        val vm = MainViewModel()
        val s = vm.picker.value
        assertEquals("", s.queryText)
        assertNull(s.selectedPackageName)
        assertFalse(s.showSystemApps)
        assertEquals(GeneratedFilter.All, s.generatedFilter)
        assertTrue(s.generatedPackageNames.isEmpty())
        assertTrue(s.multiSelectedPackageNames.isEmpty())
        assertTrue(s.packageListPermissionGranted)
        assertFalse(s.usageAccessGranted)
        assertFalse(s.isScanningGeneratedPackages)
        assertFalse(s.generatedScanFailed)
    }

    @Test
    fun shell_defaultsMatchActivityBaseline() {
        val vm = MainViewModel()
        val s = vm.shell.value
        assertEquals(AppPage.Home, s.currentPage)
        assertEquals("加载应用列表中...", s.statusText)
        assertFalse(s.isBusy)
        assertNull(s.outputTreeUri)
        assertEquals(AdvancedSettingsCategory.LiquidGlass, s.advancedSettingsCategory)
        assertEquals(AdvancedSettingsTab.Sliders, s.advancedSettingsTab)
        assertFalse(s.onboardingVisible)
    }

    @Test
    fun presetAndPreview_defaultsMatchActivityBaseline() {
        val vm = MainViewModel()
        val p = vm.presetUi.value
        assertNull(p.activePresetId)
        assertEquals(0, p.presetListVersion)
        assertEquals(BatchOutputMode.Root, p.batchOutputMode)
        assertEquals(0, p.gptRunCount)
        assertEquals(0, p.rmbgRunCount)
        assertFalse(p.presetSaveDialogVisible)
        val v = vm.previewSession.value
        assertNull(v.previewPackageName)
        assertEquals(0, v.previewVersion)
        assertFalse(v.previewStripEnabled)
        assertEquals(DEFAULT_PREVIEW_CORNER_RADIUS_DP, v.previewCornerRadiusDp)
        assertEquals(DEFAULT_PREVIEW_ICON_SIZE_DP, v.previewIconSizeDp)
        assertNull(v.lastParamsSnapshot)
        val b = vm.batchPreviewConfig.value
        assertEquals(BatchPreviewSampler.DEFAULT_BATCH_PREVIEW_COUNT, b.batchPreviewCount)
        assertEquals(4, b.batchPreviewColumns)
        assertEquals(PreviewDesktopBackground.DarkGray, b.batchPreviewDesktopBackground)
    }

    @Test
    fun updatePicker_onlyTouchesTargetField() {
        val vm = MainViewModel()
        vm.updatePicker { it.copy(queryText = "wechat") }
        assertEquals("wechat", vm.picker.value.queryText)
        assertNull(vm.picker.value.selectedPackageName)
        vm.updatePicker { it.copy(selectedPackageName = "com.tencent.mm") }
        assertEquals("wechat", vm.picker.value.queryText)
        assertEquals("com.tencent.mm", vm.picker.value.selectedPackageName)
    }

    @Test
    fun updateShellAndPreviewSession_isolated() {
        val vm = MainViewModel()
        vm.updateShell { it.copy(statusText = "ok", isBusy = true) }
        assertEquals("ok", vm.shell.value.statusText)
        assertTrue(vm.shell.value.isBusy)
        // 其他分组不受影响
        assertEquals("", vm.picker.value.queryText)
        vm.updatePreviewSession { it.copy(previewVersion = 3) }
        assertEquals(3, vm.previewSession.value.previewVersion)
        assertEquals("ok", vm.shell.value.statusText)
    }

    @Test
    fun updateConfirmTransferUpdateUi_noHistorySideEffect() {
        val vm = MainViewModel()
        vm.updateConfirm { it.copy(autoConfirmRefresh = true) }
        vm.updateTransfer { it.copy(backupBackgroundDots = 3) }
        vm.updateUpdateUi { it.copy(mitLicenseDialogVisible = true) }
        assertTrue(vm.confirm.value.autoConfirmRefresh)
        assertEquals(3, vm.transfer.value.backupBackgroundDots)
        assertTrue(vm.updateUi.value.mitLicenseDialogVisible)
        // 历史栈语义不受分组更新影响
        assertTrue(vm.history.value.isEmpty())
        assertFalse(vm.canUndo())
    }
}
