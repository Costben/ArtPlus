package dev.artplus.mobile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * P3 数据层守卫：已生成包缓存的归一化/写回语义必须与原 Activity 内联逻辑一致。
 * 全部 JVM 直跑（FakeSharedPreferences）。
 */
class AppRepositoryTest {

    @Test
    fun loadCache_emptyWhenMissing() {
        assertTrue(loadGeneratedPackageCache(FakeSharedPreferences()).isEmpty())
    }

    @Test
    fun loadCache_trimsAndDropsBlanks() {
        val prefs = FakeSharedPreferences()
        prefs.edit().putStringSet(
            PREF_GENERATED_PACKAGE_NAMES,
            setOf(" com.a ", "", "  ", "com.b"),
        ).apply()
        assertEquals(setOf("com.a", "com.b"), loadGeneratedPackageCache(prefs))
    }

    @Test
    fun updateCache_normalizesWritesBackAndStampsTime() {
        val prefs = FakeSharedPreferences()
        val before = System.currentTimeMillis()
        val result = updateGeneratedPackageCache(prefs, setOf(" com.a ", "", "com.b"))
        assertEquals(setOf("com.a", "com.b"), result)
        assertEquals(setOf("com.a", "com.b"), loadGeneratedPackageCache(prefs))
        val stamped = prefs.getLong(PREF_GENERATED_PACKAGE_NAMES_UPDATED_AT, 0L)
        assertTrue(stamped >= before)
    }

    @Test
    fun markPackageGenerated_unionsAndPersists() {
        val prefs = FakeSharedPreferences()
        val afterFirst = markPackageGenerated(prefs, emptySet(), "com.a")
        assertEquals(setOf("com.a"), afterFirst)
        val afterSecond = markPackageGenerated(prefs, afterFirst, " com.b ")
        assertEquals(setOf("com.a", "com.b"), afterSecond)
        assertEquals(setOf("com.a", "com.b"), loadGeneratedPackageCache(prefs))
    }
}
