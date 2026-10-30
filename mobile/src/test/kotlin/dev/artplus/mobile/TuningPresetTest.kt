package dev.artplus.mobile

import org.json.JSONObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TuningPresetTest {

    @Test
    fun featureTags_detectsClassicAndLocalSeparation() {
        val params = TuningParams(
            localBackgroundSeparationEnabled = true,
            rmbgAlphaStrengthPercent = 80,
        )
        val preset = TuningPreset(
            id = "test-1",
            name = "本地分离预设",
            params = params,
            createdAt = 1000L,
            updatedAt = 2000L,
        )
        val tags = preset.featureTags()
        assertTrue(tags.contains("经典"))
        assertTrue(tags.contains("本地分离"))
    }

    @Test
    fun featureTags_detectsClassicAndMonochrome() {
        val params = TuningParams(
            localBackgroundSeparationEnabled = false,
            monochromeThemeScale = 0.85f,
            rmbgAlphaStrengthPercent = 0,
        )
        val preset = TuningPreset(
            id = "test-2",
            name = "黑白经典",
            params = params,
            createdAt = 1000L,
            updatedAt = 2000L,
        )
        val tags = preset.featureTags()
        assertTrue(tags.contains("经典"))
        assertTrue(tags.contains("单色 85%"))
    }

    @Test
    fun diffSummary_reportsDifferencesAccurately() {
        val base = TuningParams(edgePolishPercent = 24, foregroundSubjectPercent = 70)
        val modified = base.copy(edgePolishPercent = 32, foregroundSubjectPercent = 80)
        val summary = base.diffSummary(modified)
        assertTrue(summary.contains("2 项参数不同"))
        assertFalse(base.sameAs(modified))
    }

    @Test
    fun jsonSerialization_roundtripPreservesAllFields() {
        val preset = TuningPreset(
            id = "uuid-123",
            name = "我的专属预设",
            params = TuningParams(
                rmbgAlphaStrengthPercent = 36,
                edgePolishPercent = 75,
            ),
            createdAt = 123456789L,
            updatedAt = 987654321L,
        )
        val json = preset.toJson()
        val parsed = TuningPreset.fromJson(json, TuningParams())
        assertNotNull(parsed)
        assertEquals(preset.id, parsed.id)
        assertEquals(preset.name, parsed.name)
        assertEquals(preset.createdAt, parsed.createdAt)
        assertEquals(preset.updatedAt, parsed.updatedAt)
        assertEquals(36, parsed.params.rmbgAlphaStrengthPercent)
        assertEquals(75, parsed.params.edgePolishPercent)
    }
}
