package dev.artplus.mobile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * P2 地基守卫：MainViewModel 的历史栈语义必须与原 Activity 内联逻辑一致
 *（基线起点 / 相同快照不入栈 / redo 截断 / 上限 50 / undo-redo 游走），
 * 预设 CRUD 结果分支必须与原 Activity 文案分支一一对应。
 * 全部 JVM 直跑（FakeSharedPreferences，无 Robolectric）。
 */
class MainViewModelTest {

    private val base = TuningParams()
    private val modified = base.copy(edgePolishPercent = 80, foregroundSubjectPercent = 70)

    private fun store(): PresetStore = PresetStore(FakeSharedPreferences())

    @Test
    fun historyStartsEmpty_noUndoRedo() {
        val vm = MainViewModel()
        assertFalse(vm.canUndo())
        assertFalse(vm.canRedo())
        assertNull(vm.undo())
        assertNull(vm.redo())
    }

    @Test
    fun onParamsApplied_capturesBaselineAndApplied() {
        val vm = MainViewModel()
        vm.onParamsApplied(before = base, applied = modified, captureUndo = true)
        assertEquals(listOf(base, modified), vm.history.value)
        assertEquals(1, vm.historyIndex.value)
        assertEquals(modified, vm.params.value)
        assertTrue(vm.canUndo())
        assertFalse(vm.canRedo())
    }

    @Test
    fun onParamsApplied_sameSnapshot_noDuplicate() {
        val vm = MainViewModel()
        vm.onParamsApplied(before = base, applied = base, captureUndo = true)
        assertEquals(listOf(base), vm.history.value)
        assertEquals(0, vm.historyIndex.value)
        assertFalse(vm.canUndo())
    }

    @Test
    fun onParamsApplied_withoutCapture_onlyMirrorsParams() {
        val vm = MainViewModel()
        vm.onParamsApplied(before = base, applied = modified, captureUndo = false)
        assertTrue(vm.history.value.isEmpty())
        assertEquals(modified, vm.params.value)
    }

    @Test
    fun onParamsApplied_truncatesRedoTail() {
        val vm = MainViewModel()
        val third = base.copy(edgePolishPercent = 10)
        vm.onParamsApplied(before = base, applied = modified, captureUndo = true)
        assertNotNull(vm.undo())
        assertTrue(vm.canRedo())
        vm.onParamsApplied(before = modified, applied = third, captureUndo = true)
        assertEquals(listOf(base, third), vm.history.value)
        assertFalse(vm.canRedo())
    }

    @Test
    fun onParamsApplied_capsAt50_dropsHead() {
        val vm = MainViewModel()
        var prev = base
        repeat(MainViewModel.MAX_HISTORY_SIZE + 5) { i ->
            val next = prev.copy(edgePolishPercent = (i % 100) + 1)
            vm.onParamsApplied(before = prev, applied = next, captureUndo = true)
            prev = next
        }
        assertEquals(MainViewModel.MAX_HISTORY_SIZE, vm.history.value.size)
        assertEquals(MainViewModel.MAX_HISTORY_SIZE - 1, vm.historyIndex.value)
        assertTrue(vm.canUndo())
    }

    @Test
    fun undoRedo_walk() {
        val vm = MainViewModel()
        val third = base.copy(edgePolishPercent = 10)
        vm.onParamsApplied(before = base, applied = modified, captureUndo = true)
        vm.onParamsApplied(before = modified, applied = third, captureUndo = true)
        assertEquals(modified, vm.undo())
        assertEquals(base, vm.undo())
        assertNull(vm.undo())
        assertEquals(modified, vm.redo())
        assertEquals(third, vm.redo())
        assertNull(vm.redo())
    }

    @Test
    fun resetHistory_replacesStack() {
        val vm = MainViewModel()
        vm.onParamsApplied(before = base, applied = modified, captureUndo = true)
        vm.resetHistory(modified)
        assertEquals(listOf(modified), vm.history.value)
        assertEquals(0, vm.historyIndex.value)
        assertEquals(modified, vm.params.value)
        assertFalse(vm.canUndo())
    }

    @Test
    fun mergedPresetParams_matchesFromParamMap() {
        val vm = MainViewModel()
        val preset = TuningPreset("id", "n", modified, 1L, 2L)
        assertEquals(
            TuningParams.fromParamMap(modified.toParamMap(), base),
            vm.mergedPresetParams(preset, base),
        )
    }

    @Test
    fun savePreset_blankName() {
        val vm = MainViewModel()
        assertIs<SavePresetOutcome.BlankName>(vm.savePreset(store(), base, "   "))
    }

    @Test
    fun savePreset_savedAndDuplicate() {
        val vm = MainViewModel()
        val prefs = store()
        val outcome = vm.savePreset(prefs, base, "我的预设")
        assertIs<SavePresetOutcome.Saved>(outcome)
        assertEquals("我的预设", outcome.preset.name)
        assertEquals(outcome.preset.id, prefs.activePresetId)
        assertEquals(1, prefs.all().size)
        val dup = vm.savePreset(prefs, modified, "我的预设")
        assertIs<SavePresetOutcome.DuplicateName>(dup)
        assertEquals("我的预设", dup.name)
        assertEquals(1, prefs.all().size)
    }

    @Test
    fun overwritePreset_trueAndDuplicateFalse() {
        val vm = MainViewModel()
        val prefs = store()
        val saved = (vm.savePreset(prefs, base, "A") as SavePresetOutcome.Saved).preset
        assertTrue(vm.overwritePreset(prefs, saved, modified))
        assertEquals(modified, prefs.get(saved.id)?.params)
        assertEquals(saved.id, prefs.activePresetId)
        vm.savePreset(prefs, base, "B")
        assertFalse(vm.overwritePreset(prefs, saved.copy(name = "B"), modified))
    }

    @Test
    fun renamePreset_branches() {
        val vm = MainViewModel()
        val prefs = store()
        val saved = (vm.savePreset(prefs, base, "A") as SavePresetOutcome.Saved).preset
        assertIs<RenamePresetOutcome.BlankName>(vm.renamePreset(prefs, saved.id, "  "))
        vm.savePreset(prefs, base, "B")
        assertIs<RenamePresetOutcome.Failed>(vm.renamePreset(prefs, saved.id, "B"))
        val renamed = vm.renamePreset(prefs, saved.id, " C ")
        assertIs<RenamePresetOutcome.Renamed>(renamed)
        assertEquals("C", renamed.name)
        assertEquals("C", prefs.get(saved.id)?.name)
    }

    @Test
    fun deletePreset_removes() {
        val vm = MainViewModel()
        val prefs = store()
        val saved = (vm.savePreset(prefs, base, "A") as SavePresetOutcome.Saved).preset
        vm.deletePreset(prefs, saved.id)
        assertTrue(prefs.all().isEmpty())
    }
}
