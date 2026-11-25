package dev.artplus.mobile

import dev.artplus.mobile.ui.pages.params.LocalTuningGroup
import dev.artplus.mobile.ui.pages.params.LocalTuningStylePreset
import dev.artplus.mobile.ui.pages.params.applyLocalTuningGroupToggle
import dev.artplus.mobile.ui.pages.params.countEnabledLocalWorkflowSteps
import dev.artplus.mobile.ui.pages.params.matchingLocalStylePreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalTuningStylePresetTest {

    @Test
    fun defaultParamsMatchStandardStyle() {
        val defaults = TuningParams()
        assertEquals(LocalTuningStylePreset.Standard, matchingLocalStylePreset(defaults))
        assertEquals(18, countEnabledLocalWorkflowSteps(defaults))
    }

    @Test
    fun everyStylePresetMatchesItselfAndPreservesUnrelatedFields() {
        val customBase = TuningParams(
            foregroundSubjectPercent = 92,
            rmbgAlphaStrengthPercent = 140,
            gptCustomPrompt = "keep me",
            nightSubjectLightBackgroundEnabled = true,
        )
        LocalTuningStylePreset.entries.forEach { preset ->
            val applied = preset.applyTo(customBase)
            assertEquals(preset, matchingLocalStylePreset(applied))
            assertEquals(92, applied.foregroundSubjectPercent)
            assertEquals(140, applied.rmbgAlphaStrengthPercent)
            assertEquals("keep me", applied.gptCustomPrompt)
            assertTrue(applied.nightSubjectLightBackgroundEnabled)
        }
    }

    @Test
    fun lightTouchDisablesPlateAndShadowSubStages() {
        val light = LocalTuningStylePreset.LightTouch.applyTo(TuningParams())
        assertFalse(light.localPlateCleanupEnabled)
        assertFalse(light.localPlateEdgeRepairEnabled)
        assertFalse(light.localPlateResidueCleanupEnabled)
        assertFalse(light.localOriginalCleanupEnabled)
        assertFalse(light.localShadowCleanupEnabled)
        assertFalse(light.localShadowEdgeRepairEnabled)
        assertTrue(light.localBackgroundSeparationEnabled)
        assertTrue(light.localEdgePolishEnabled)
        assertEquals(12, countEnabledLocalWorkflowSteps(light))
    }

    @Test
    fun groupTogglesFlipAllLinkedFlagsAtomically() {
        val offPlate = applyLocalTuningGroupToggle(TuningParams(), LocalTuningGroup.Plate, false)
        assertFalse(offPlate.localPlateCleanupEnabled)
        assertFalse(offPlate.localPlateEdgeRepairEnabled)
        assertFalse(offPlate.localPlateResidueCleanupEnabled)
        assertFalse(offPlate.localOriginalCleanupEnabled)
        assertNull(matchingLocalStylePreset(offPlate))

        val offShadow = applyLocalTuningGroupToggle(offPlate, LocalTuningGroup.Shadow, false)
        assertFalse(offShadow.localShadowCleanupEnabled)
        assertFalse(offShadow.localShadowEdgeRepairEnabled)

        val offMulti = applyLocalTuningGroupToggle(TuningParams(), LocalTuningGroup.MultiCandidates, false)
        assertFalse(offMulti.localComposedBackgroundEnabled)
        assertFalse(offMulti.localTwoLayerCandidateEnabled)
        assertFalse(offMulti.localComponentCandidatesEnabled)
        assertFalse(offMulti.localTextSafeCandidateEnabled)
        assertFalse(offMulti.localAdaptiveSelectionEnabled)
        assertTrue(offMulti.localAutoSelectionEnabled)
    }
}
