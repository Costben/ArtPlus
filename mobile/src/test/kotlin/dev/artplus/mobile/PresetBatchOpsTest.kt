package dev.artplus.mobile

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Slice 3.2 业务单测（新增，不改既有 MainViewModelTest 14 项 / MainViewModelUiStateTest 6 项语义）：
 * 预设批量包名解析 / 多选翻转 / 进度 coerce / 收尾文案分支 / VM 状态落点。
 * 全部 JVM 直跑（File 仅用空临时目录走无快照分支，不碰 Bitmap IO）。
 */
class PresetBatchOpsTest {

    @Test
    fun toggleMultiSelection_addAndRemove() {
        assertEquals(setOf("a"), toggleMultiSelection(emptySet(), "a"))
        assertEquals(emptySet(), toggleMultiSelection(setOf("a"), "a"))
        assertEquals(setOf("a", "b"), toggleMultiSelection(setOf("a"), "b"))
    }

    @Test
    fun toggleMultiSelectedPackage_writesPickerOnly() {
        val vm = MainViewModel()
        vm.toggleMultiSelectedPackage("com.a", vm.picker.value.multiSelectedPackageNames)
        assertEquals(setOf("com.a"), vm.picker.value.multiSelectedPackageNames)
        vm.toggleMultiSelectedPackage("com.a", vm.picker.value.multiSelectedPackageNames)
        assertTrue(vm.picker.value.multiSelectedPackageNames.isEmpty())
        // 其他分组不受影响
        assertEquals("", vm.picker.value.queryText)
        assertTrue(vm.history.value.isEmpty())
    }

    @Test
    fun resolvePresetBatchPackages_branches() {
        assertEquals(
            listOf("a", "b"),
            resolvePresetBatchPackages(setOf("b", "a"), "z"),
        )
        assertEquals(listOf("z"), resolvePresetBatchPackages(emptySet(), "z"))
        assertTrue(resolvePresetBatchPackages(emptySet(), null).isEmpty())
    }

    @Test
    fun updateBatchApplyProgress_coercesAndWritesTransferPlusStatus() {
        val vm = MainViewModel()
        vm.updateBatchApplyProgress(
            completed = 99,
            total = 3,
            currentLabel = "x",
            failures = 1,
            title = "全部应用",
        )
        val p = vm.transfer.value.batchApplyProgress
        assertEquals("全部应用", p?.title)
        assertEquals(3, p?.completed)
        assertEquals(3, p?.total)
        assertEquals("全部应用处理中: 3/3", vm.shell.value.statusText)
    }

    @Test
    fun buildBatchApplyProgress_keepsRawBeginValues() {
        val p = buildBatchApplyProgress("预设批量应用", 0, 5, "准备处理 5 个 APK", 0)
        assertEquals("预设批量应用", p.title)
        assertEquals(0, p.completed)
        assertEquals(5, p.total)
    }

    @Test
    fun presetBatchFinishStatus_branches() {
        assertEquals("预设「P」批量完成: 2/2", presetBatchFinishStatus("P", 2, 2, null))
        assertEquals("预设批量失败: e1", presetBatchFinishStatus("P", 0, 2, "e1"))
        assertEquals(
            "预设批量完成 1 个，失败 1 个：e1",
            presetBatchFinishStatus("P", 1, 2, "e1"),
        )
    }

    @Test
    fun currentBatchFinishStatus_branches() {
        assertEquals("按当前调参批量完成: 2/2", currentBatchFinishStatus(2, 2, null))
        assertEquals("批量失败: e1", currentBatchFinishStatus(0, 2, "e1"))
        assertEquals("批量完成 1 个，失败 1 个：e1", currentBatchFinishStatus(1, 2, "e1"))
    }

    @Test
    fun liquidGlassBatchFinishStatus_branches() {
        assertEquals(
            "已批量添加光影 2 个，未刷新，请手动点首页左上角刷新图标",
            liquidGlassBatchFinishStatus(2, 0, null),
        )
        assertEquals("批量添加光影失败: e1", liquidGlassBatchFinishStatus(0, 2, "e1"))
        assertEquals("已添加光影 1 个，失败 1 个: e1", liquidGlassBatchFinishStatus(1, 1, "e1"))
    }

    @Test
    fun openBatchPreviewForPreset_noSnapshot_setsConfirmTarget() {
        val vm = MainViewModel()
        val filesDir = Files.createTempDirectory("artplus-batch-test").toFile()
        try {
            val preset = TuningPreset("pid", "P", TuningParams(), 1L, 2L)
            vm.openBatchPreviewForPreset(preset, filesDir)
            assertEquals(preset, vm.presetUi.value.activeBatchPreviewPreset)
            assertEquals(preset, vm.presetUi.value.presetBatchPreviewConfirmTarget)
            assertNull(vm.presetUi.value.batchPreviewResult)
        } finally {
            filesDir.deleteRecursively()
        }
    }
}
