package dev.artplus.mobile

import org.json.JSONObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TuningPresetTest {

    @Test
    fun featureTags_detectsLiquidGlassAndLocalSeparation() {
        val params = TuningParams(
            liquidGlassEnabled = true,
            liquidGlassRadius = 28,
            localBackgroundSeparationEnabled = true,
            rmbgAlphaStrengthPercent = 80,
        )
        val preset = TuningPreset(
            id = "test-1",
            name = "液态预设",
            params = params,
            createdAt = 1000L,
            updatedAt = 2000L,
        )
        val tags = preset.featureTags()
        assertTrue(tags.contains("玻璃 28dp"))
        assertTrue(tags.contains("本地分离"))
    }

    @Test
    fun featureTags_detectsClassicAndMonochrome() {
        val params = TuningParams(
            liquidGlassEnabled = false,
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
        val base = TuningParams(liquidGlassRadius = 24, foregroundSubjectPercent = 70)
        val modified = base.copy(liquidGlassRadius = 32, foregroundSubjectPercent = 80)
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
                liquidGlassEnabled = true,
                liquidGlassRadius = 36,
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
        assertTrue(parsed.params.liquidGlassEnabled)
        assertEquals(36, parsed.params.liquidGlassRadius)
        assertEquals(75, parsed.params.edgePolishPercent)
    }
}
