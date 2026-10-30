package dev.artplus.mobile

import dev.artplus.mobile.ui.pages.picker.isNewerVersion
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SemVerUpdateTest {

    @Test
    fun testPrereleaseCanUpgradeToOfficialRelease() {
        // 核心场景：处于 1.5.0-alpha10 的用户在 1.5.0 正式版发布后，必须能够检测到更新
        assertTrue(isNewerVersion("1.5.0", "1.5.0-alpha10"))
        assertTrue(isNewerVersion("v1.5.0", "1.5.0-alpha10"))
        assertTrue(isNewerVersion("1.5.1", "1.5.0-alpha10"))
    }

    @Test
    fun testOfficialReleaseNeverReceivesPrereleaseUpdates() {
        // 核心约束：正式版用户绝不接收任何 alpha/beta/rc 预发布版本的更新提示
        assertFalse(isNewerVersion("1.5.1-alpha1", "1.5.0"))
        assertFalse(isNewerVersion("v1.5.1-alpha1", "1.5.0"))
        assertFalse(isNewerVersion("1.5.0-alpha11", "1.5.0"))
        assertFalse(isNewerVersion("2.0.0-beta1", "1.5.0"))
    }

    @Test
    fun testOfficialReleaseUpgradesToHigherOfficialRelease() {
        // 正式版正常升级到更高正式版
        assertTrue(isNewerVersion("1.5.1", "1.5.0"))
        assertTrue(isNewerVersion("1.6.0", "1.5.0"))
        assertTrue(isNewerVersion("2.0.0", "1.5.0"))
        assertFalse(isNewerVersion("1.5.0", "1.5.0"))
        assertFalse(isNewerVersion("1.4.3", "1.5.0"))
    }

    @Test
    fun testPrereleaseUpgradesToHigherPrerelease() {
        // 预发布通道内部迭代
        assertTrue(isNewerVersion("1.5.0-alpha10", "1.5.0-alpha9"))
        assertFalse(isNewerVersion("1.5.0-alpha9", "1.5.0-alpha10"))
        assertTrue(isNewerVersion("1.5.1-alpha1", "1.5.0-alpha10"))
    }
}
