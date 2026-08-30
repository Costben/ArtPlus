package dev.artplus.mobile

import android.content.pm.ApplicationInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppVisibilityTest {

    @Test
    fun isSystemAppFlags_detectsSystemFlag() {
        assertTrue(AppVisibility.isSystemAppFlags(ApplicationInfo.FLAG_SYSTEM))
        assertTrue(AppVisibility.isSystemAppFlags(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))
        assertTrue(AppVisibility.isSystemAppFlags(ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))
        assertFalse(AppVisibility.isSystemAppFlags(0))
        assertFalse(AppVisibility.isSystemAppFlags(ApplicationInfo.FLAG_DEBUGGABLE))
    }

    @Test
    fun shouldShow_respectsShowSystemApps() {
        val selfPkg = "dev.artplus.mobile"
        assertFalse(AppVisibility.shouldShow(ApplicationInfo.FLAG_SYSTEM, "com.example.sys", selfPkg, false))
        assertFalse(AppVisibility.shouldShow(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP, "com.example.updated", selfPkg, false))
        assertTrue(AppVisibility.shouldShow(ApplicationInfo.FLAG_SYSTEM, "com.example.sys", selfPkg, true))
        assertTrue(AppVisibility.shouldShow(0, "com.example.user", selfPkg, false))
        assertTrue(AppVisibility.shouldShow(0, "com.example.user", selfPkg, true))
    }

    @Test
    fun shouldShow_neverHidesSelf() {
        val selfPkg = "dev.artplus.mobile"
        assertTrue(AppVisibility.shouldShow(ApplicationInfo.FLAG_SYSTEM, selfPkg, selfPkg, false))
        assertTrue(AppVisibility.shouldShow(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP, selfPkg, selfPkg, false))
        assertTrue(AppVisibility.shouldShow(ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP, selfPkg, selfPkg, false))
    }

    @Test
    fun shouldShow_mixedFlags() {
        val selfPkg = "dev.artplus.mobile"
        val mixed = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_DEBUGGABLE
        assertFalse(AppVisibility.shouldShow(mixed, "com.example.mixed", selfPkg, false))
        assertTrue(AppVisibility.shouldShow(mixed, "com.example.mixed", selfPkg, true))
    }

    @Test
    fun shouldShowInPicker_defaultOff_keepsLaunchableUserOnly() {
        val selfPkg = "dev.artplus.mobile"
        // 默认关闭：仅可启动的非系统应用可见，系统应用隐藏（自身除外）
        assertTrue(AppVisibility.shouldShowInPicker(0, "com.user.one", selfPkg, false, true))
        assertFalse(AppVisibility.shouldShowInPicker(0, "com.user.one", selfPkg, false, false))
        assertFalse(AppVisibility.shouldShowInPicker(ApplicationInfo.FLAG_SYSTEM, "com.sys.one", selfPkg, false, true))
        assertFalse(AppVisibility.shouldShowInPicker(ApplicationInfo.FLAG_SYSTEM, "com.sys.one", selfPkg, false, false))
        assertFalse(AppVisibility.shouldShowInPicker(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP, "com.sys.updated", selfPkg, false, true))
        // ArtPlus 自身即使被标记为系统也必须显示
        assertTrue(AppVisibility.shouldShowInPicker(ApplicationInfo.FLAG_SYSTEM, selfPkg, selfPkg, false, true))
        assertTrue(AppVisibility.shouldShowInPicker(ApplicationInfo.FLAG_SYSTEM, selfPkg, selfPkg, false, false))
        assertTrue(AppVisibility.shouldShowInPicker(0, selfPkg, selfPkg, false, false))
    }

    @Test
    fun shouldShowInPicker_open_includesAllSystemEvenWithoutLauncher_butNotUserWithoutLauncher() {
        val selfPkg = "dev.artplus.mobile"
        // 打开：所有系统应用（含无 launcher）都可见
        assertTrue(AppVisibility.shouldShowInPicker(ApplicationInfo.FLAG_SYSTEM, "com.sys.one", selfPkg, true, true))
        assertTrue(AppVisibility.shouldShowInPicker(ApplicationInfo.FLAG_SYSTEM, "com.sys.one", selfPkg, true, false))
        assertTrue(AppVisibility.shouldShowInPicker(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP, "com.sys.updated", selfPkg, true, false))
        // 普通用户应用仍仅当可启动时可见
        assertTrue(AppVisibility.shouldShowInPicker(0, "com.user.one", selfPkg, true, true))
        assertFalse(AppVisibility.shouldShowInPicker(0, "com.user.one", selfPkg, true, false))
        // 自身始终可见
        assertTrue(AppVisibility.shouldShowInPicker(ApplicationInfo.FLAG_SYSTEM, selfPkg, selfPkg, true, false))
    }

    @Test
    fun shouldShowInPicker_preservesSearchAndFilterConcept() {
        val selfPkg = "dev.artplus.mobile"
        data class Entry(val flags: Int, val pkg: String, val launchable: Boolean)
        val apps = listOf(
            Entry(ApplicationInfo.FLAG_SYSTEM, "com.sys.one", true),
            Entry(ApplicationInfo.FLAG_SYSTEM, "com.sys.noLauncher", false),
            Entry(0, "com.user.one", true),
            Entry(0, "com.user.noLauncher", false),
            Entry(0, selfPkg, true),
            Entry(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP, "com.sys.updated", false),
        )
        val filteredOff = apps.filter { e -> AppVisibility.shouldShowInPicker(e.flags, e.pkg, selfPkg, false, e.launchable) }
        val filteredOn = apps.filter { e -> AppVisibility.shouldShowInPicker(e.flags, e.pkg, selfPkg, true, e.launchable) }
        // 关闭：仅 user launchable + self
        assertEquals(2, filteredOff.size)
        assertTrue(filteredOff.any { it.pkg == "com.user.one" })
        assertTrue(filteredOff.any { it.pkg == selfPkg })
        assertFalse(filteredOff.any { it.pkg == "com.sys.one" })
        // 打开：user launchable + self + 所有系统（含无 launcher）
        assertEquals(5, filteredOn.size)
        assertTrue(filteredOn.any { it.pkg == "com.sys.one" })
        assertTrue(filteredOn.any { it.pkg == "com.sys.noLauncher" })
        assertTrue(filteredOn.any { it.pkg == "com.sys.updated" })
        assertTrue(filteredOn.any { it.pkg == "com.user.one" })
        assertTrue(filteredOn.any { it.pkg == selfPkg })
        assertFalse(filteredOn.any { it.pkg == "com.user.noLauncher" })
    }

    @Test
    fun shouldShowInPicker_applicationInfoOverload() {
        val selfPkg = "dev.artplus.mobile"
        val sysInfo = ApplicationInfo().apply {
            flags = ApplicationInfo.FLAG_SYSTEM
            packageName = "com.sys.one"
        }
        val userInfoLaunchable = ApplicationInfo().apply {
            flags = 0
            packageName = "com.user.one"
        }
        val userInfoNoLauncher = ApplicationInfo().apply {
            flags = 0
            packageName = "com.user.two"
        }
        assertTrue(AppVisibility.shouldShowInPicker(sysInfo, true, true, selfPkg))
        assertTrue(AppVisibility.shouldShowInPicker(sysInfo, false, true, selfPkg))
        assertFalse(AppVisibility.shouldShowInPicker(sysInfo, true, false, selfPkg))
        assertTrue(AppVisibility.shouldShowInPicker(userInfoLaunchable, true, false, selfPkg))
        assertFalse(AppVisibility.shouldShowInPicker(userInfoNoLauncher, false, true, selfPkg))
    }

    @Test
    fun shouldShow_preservesSearchAndFilterLogicConceptually() {
        val selfPkg = "dev.artplus.mobile"
        val apps = listOf(
            Triple(ApplicationInfo.FLAG_SYSTEM, "com.sys.one", false),
            Triple(0, "com.user.one", false),
            Triple(0, "dev.artplus.mobile", false),
            Triple(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP, "com.sys.updated", false),
        )
        val filteredOff = apps.filter { (flags, pkg, _) -> AppVisibility.shouldShow(flags, pkg, selfPkg, false) }
        val filteredOn = apps.filter { (flags, pkg, _) -> AppVisibility.shouldShow(flags, pkg, selfPkg, true) }
        assertEquals(2, filteredOff.size)
        assertEquals(4, filteredOn.size)
        assertTrue(filteredOff.any { it.second == "com.user.one" })
        assertTrue(filteredOff.any { it.second == selfPkg })
        assertFalse(filteredOff.any { it.second == "com.sys.one" })
    }
}
