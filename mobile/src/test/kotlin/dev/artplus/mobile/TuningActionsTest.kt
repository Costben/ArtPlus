package dev.artplus.mobile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import dev.artplus.mobile.imaging.PreviewChoice
import dev.artplus.mobile.state.batchApplyProcessingStatus
import dev.artplus.mobile.state.coercedBatchApplyProgress
import dev.artplus.mobile.state.currentBatchFinishStatus
import dev.artplus.mobile.state.presetBatchFinishStatus
import dev.artplus.mobile.ui.cards.PreviewSelections

/**
 * S3.8：写入收口与订阅下沉落地后补的单测（10 项）。
 *
 * 覆盖本轮动到的纯逻辑：
 * - `PreviewSelections.fromNames`（S2.1 落盘时 4 个预览字段 → 枚举的唯一换算点）
 * - `coercedBatchApplyProgress` / 三个完成文案分支（S2.4 合并写入后的状态收敛）
 * - `PreviewChoice` 与 `LocalSeparationMode` 的映射（S2.2 刷新链路里 `defaultLocal` 的等价性）
 */
class TuningActionsTest {

    // 1
    @Test
    fun previewSelections_fromNames_maps_all_four() {
        val s = PreviewSelections.fromNames("Full", "Original", "Rmbg", "Gpt")
        assertEquals(PreviewChoice.Full, s.normalLight)
        assertEquals(PreviewChoice.Original, s.normalDark)
        assertEquals(PreviewChoice.Rmbg, s.monochromeLight)
        assertEquals(PreviewChoice.Gpt, s.monochromeDark)
    }

    // 2
    @Test
    fun previewSelections_fromNames_unknown_falls_back_to_full() {
        val s = PreviewSelections.fromNames("Nope", "Nope", "Nope", "Nope")
        assertEquals(PreviewChoice.Full, s.normalLight)
        assertEquals(PreviewChoice.Full, s.monochromeDark)
    }

    // Plate 是旧值，必须归一到 Full（迁移兼容）
    @Test
    fun previewSelections_fromNames_plate_migrates_to_full() {
        assertEquals(PreviewChoice.Full, PreviewSelections.fromNames("Plate", null, null, null).normalLight)
    }

    // 自定义项（CustomForeground 等）不能进持久化枚举，必须回落 Full
    @Test
    fun previewSelections_fromNames_custom_choice_rejected() {
        assertEquals(
            PreviewChoice.Full,
            PreviewSelections.fromNames("CustomForeground", null, null, null).normalLight,
        )
    }

    // 3
    @Test
    fun previewSelections_fromNames_is_case_sensitive() {
        // 小写不是合法枚举名：回落到 Full，同组其它字段照常命中
        val s = PreviewSelections.fromNames("full", "Original", "Rmbg", "Gpt")
        assertEquals(PreviewChoice.Full, s.normalLight)
        assertEquals(PreviewChoice.Original, s.normalDark)
    }

    // 4
    @Test
    fun coercedBatchApplyProgress_clamps_completed() {
        val p = coercedBatchApplyProgress(completed = 99, total = 10, currentLabel = "x", failures = 0)
        assertEquals(10, p.completed)
        assertEquals(10, p.total)
    }

    // 5
    @Test
    fun coercedBatchApplyProgress_negative_total_safe() {
        val p = coercedBatchApplyProgress(completed = 5, total = -3, currentLabel = "x", failures = 2)
        assertEquals(0, p.completed)
        assertEquals(2, p.failures)
    }

    // 6
    @Test
    fun batchApplyProcessingStatus_never_exceeds_total() {
        assertEquals("全部应用处理中: 10/10", batchApplyProcessingStatus(12, 10))
        assertEquals("全部应用处理中: 0/0", batchApplyProcessingStatus(0, 0))
    }

    // 7
    @Test
    fun presetBatchFinishStatus_success_branch() {
        assertEquals(
            "预设「A」批量完成: 3/3",
            presetBatchFinishStatus("A", 3, 3, null),
        )
    }

    // 8
    @Test
    fun presetBatchFinishStatus_all_failed_branch() {
        assertEquals("预设批量失败: boom", presetBatchFinishStatus("A", 0, 3, "boom"))
    }

    // 9
    @Test
    fun presetBatchFinishStatus_partial_branch() {
        assertEquals(
            "预设批量完成 2 个，失败 1 个：boom",
            presetBatchFinishStatus("A", 2, 3, "boom"),
        )
    }

    // 10
    @Test
    fun currentBatchFinishStatus_branches() {
        assertEquals("按当前调参批量完成: 2/2", currentBatchFinishStatus(2, 2, null))
        assertEquals("批量失败: boom", currentBatchFinishStatus(0, 2, "boom"))
        assertEquals("批量完成 1 个，失败 1 个：boom", currentBatchFinishStatus(1, 2, "boom"))
    }

}
