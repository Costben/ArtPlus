package dev.artplus.mobile

import java.util.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BatchPreviewSamplingTest {

    private data class TestApp(
        override val packageName: String,
        override val launchable: Boolean = true,
    ) : BatchSampleTarget

    @Test
    fun sample_prioritizesUngeneratedAppsWhenSufficient() {
        val apps = (1..50).map { TestApp("com.app.ungenerated.$it") } +
            (1..50).map { TestApp("com.app.generated.$it") }
        val generatedNames = (1..50).map { "com.app.generated.$it" }.toSet()

        val sampled = BatchPreviewSampler.sample(
            candidates = apps,
            generatedPackageNames = generatedNames,
            count = 20,
            random = Random(42),
        )

        assertEquals(20, sampled.size)
        // All sampled should be from ungenerated pool
        assertTrue(sampled.all { it.packageName !in generatedNames })
        // All sampled should have unique package names
        assertEquals(20, sampled.map { it.packageName }.toSet().size)
    }

    @Test
    fun sample_fallsBackToGeneratedWhenUngeneratedInsufficient() {
        val ungenerated = (1..5).map { TestApp("com.app.ungenerated.$it") }
        val generated = (1..30).map { TestApp("com.app.generated.$it") }
        val generatedNames = generated.map { it.packageName }.toSet()

        val sampled = BatchPreviewSampler.sample(
            candidates = ungenerated + generated,
            generatedPackageNames = generatedNames,
            count = 20,
            random = Random(42),
        )

        assertEquals(20, sampled.size)
        val ungeneratedSampled = sampled.filter { it.packageName !in generatedNames }
        val generatedSampled = sampled.filter { it.packageName in generatedNames }

        assertEquals(5, ungeneratedSampled.size)
        assertEquals(15, generatedSampled.size)
        assertEquals(20, sampled.map { it.packageName }.toSet().size)
    }

    @Test
    fun sample_excludesNonLaunchableAppsAndSelfPackage() {
        val selfPkg = "dev.artplus.mobile"
        val nonLaunchable = (1..10).map { TestApp("com.app.sys.$it", launchable = false) }
        val selfApp = TestApp(selfPkg, launchable = true)
        val normalApps = (1..10).map { TestApp("com.app.user.$it", launchable = true) }

        val sampled = BatchPreviewSampler.sample(
            candidates = nonLaunchable + listOf(selfApp) + normalApps,
            generatedPackageNames = emptySet(),
            count = 20,
            selfPackageName = selfPkg,
            random = Random(42),
        )

        assertEquals(10, sampled.size)
        assertTrue(sampled.none { it.packageName == selfPkg })
        assertTrue(sampled.none { !it.launchable })
    }

    @Test
    fun sample_clampsCountToAllowedRange() {
        val apps = (1..100).map { TestApp("com.app.$it") }

        val sampledMin = BatchPreviewSampler.sample(
            candidates = apps,
            generatedPackageNames = emptySet(),
            count = 1,
        )
        assertEquals(BatchPreviewSampler.MIN_BATCH_PREVIEW_COUNT, sampledMin.size)

        val sampledMax = BatchPreviewSampler.sample(
            candidates = apps,
            generatedPackageNames = emptySet(),
            count = 100,
        )
        assertEquals(BatchPreviewSampler.MAX_BATCH_PREVIEW_COUNT, sampledMax.size)
    }

    @Test
    fun sample_handlesEmptyOrFewerAppsThanTarget() {
        val apps = (1..3).map { TestApp("com.app.$it") }

        val sampled = BatchPreviewSampler.sample(
            candidates = apps,
            generatedPackageNames = emptySet(),
            count = 20,
        )

        assertEquals(3, sampled.size)
        assertEquals(apps.toSet(), sampled.toSet())
    }
}
