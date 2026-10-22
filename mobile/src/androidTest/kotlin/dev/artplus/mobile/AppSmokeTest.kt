package dev.artplus.mobile

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Test
import org.junit.runner.RunWith

/**
 * S3.8：Compose 冒烟（启动 → 4 页切换 → 打开选择入口）。
 *
 * 用 UI Automator 而不是 compose-ui-test：后者对应的 `ui-test-junit4` 不在本地依赖缓存里，
 * 离线构建拉不到，会让 CI 与本地门禁一起红。
 */
@RunWith(AndroidJUnit4::class)
class AppSmokeTest {

    private val device: UiDevice
        get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun launch_switch_four_tabs_and_open_picker() {
        ActivityScenario.launch(MainActivity::class.java).use {
            val timeout = 5_000L
            // 首页
            device.wait(Until.hasObject(By.text("ArtPlus")), timeout)
            // 生成参数
            device.findObject(By.text("生成参数"))?.click()
            device.wait(Until.hasObject(By.text("保存成预设")), timeout)
            // 预设
            device.findObject(By.text("预设"))?.click()
            device.wait(Until.hasObject(By.text("预设快照库")), timeout)
            // 设置
            device.findObject(By.text("设置"))?.click()
            device.wait(Until.hasObject(By.text("应用范围")), timeout)
            // 回首页并打开应用选择入口
            device.findObject(By.text("主页"))?.click()
            device.wait(Until.hasObject(By.text("ArtPlus")), timeout)
            val entry = device.wait(Until.findObject(By.textContains("选择一个应用")), timeout)
            entry?.click()
            device.wait(Until.hasObject(By.textContains("选择 APK")), timeout)
        }
    }
}
