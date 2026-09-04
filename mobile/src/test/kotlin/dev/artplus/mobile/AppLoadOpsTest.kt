package dev.artplus.mobile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Slice 3.3 应用加载与生命周期调度单测（新增，不改既有 79 项语义）：
 * status 文案分支 / 重载条件 / 生命周期顺序与条件 / 空表分支状态落点。
 * 全部 JVM 直跑；viewModelScope 协程方法（requestAppLoad /
 * refreshGeneratedPackagesAsync 非空分支 / refreshArtPlusIconsAsync /
 * launchUiFriendly）需 Android Main dispatcher，不在此测，只测同步部分。
 */
class AppLoadOpsTest {

    @Test
    fun buildAppLoadStatus_branches() {
        assertEquals(
            "没有读取到应用。请确认已允许读取应用列表。",
            buildAppLoadStatus(0, 0, true),
        )
        assertEquals(
            "读取到 10 个应用，但应用列表权限状态异常。",
            buildAppLoadStatus(10, 7, false),
        )
        assertEquals(
            "共 10 个应用，其中 7 个有启动器入口。",
            buildAppLoadStatus(10, 7, true),
        )
    }

    @Test
    fun shouldReloadApps_branches() {
        assertFalse(shouldReloadApps(false, true, true, true, true, true))
        assertTrue(shouldReloadApps(true, true, true, true, true, true))
        assertTrue(shouldReloadApps(true, false, true, false, true, true))
        assertTrue(shouldReloadApps(true, false, true, true, false, true))
        assertFalse(shouldReloadApps(true, false, true, true, true, true))
    }

    @Test
    fun generatedStatus_branches() {
        assertEquals("应用列表为空，保留已生成缓存", generatedEmptyStatus())
        assertEquals("已刷新生成状态: 3 个", generatedSuccessStatus(3))
        assertTrue(
            generatedFailureStatus(RuntimeException("boom")).contains("boom"),
        )
        assertTrue(
            generatedFailureStatus(RuntimeException()).contains("RuntimeException"),
        )
    }

    @Test
    fun iconsStatus_branches() {
        assertEquals("已刷新 ART+ 图标", iconsSuccessStatus(""))
        assertEquals("已刷新 ART+ 图标", iconsSuccessStatus("   "))
        assertEquals("已刷新 ART+ 图标: ok", iconsSuccessStatus("ok"))
        assertTrue(iconsFailureStatus(RuntimeException("x")).contains("x"))
    }

    @Test
    fun onCreatePreContent_preservesOrder() {
        val vm = MainViewModel()
        val order = mutableListOf<String>()
        vm.onCreatePreContent(
            onLoadGptSettings = { order += "gpt" },
            onLoadTuningParams = { order += "tuning" },
            onInitTuningHistory = { order += "history" },
            onLoadRmbgSettings = { order += "rmbg" },
            onLoadGeneratedCache = { order += "cache" },
            onLoadUiState = { order += "ui" },
            onLoadPresetState = { order += "preset" },
            onStartDebugServer = { order += "server" },
            onRefreshPermissions = { order += "perm" },
        )
        assertEquals(
            listOf("gpt", "tuning", "history", "rmbg", "cache", "ui", "preset", "server", "perm"),
            order,
        )
    }

    @Test
    fun onCreatePostContent_debugSkipsSpecialPermission() {
        val vm = MainViewModel()
        val order = mutableListOf<String>()
        vm.onCreatePostContent(
            isDebugIntent = true,
            onRequestDeclaredPermissions = { order += "declared" },
            onRequestSpecialPermissionsOnce = { order += "special" },
            onLoadApps = { order += "apps" },
            onHandleDebugIntent = { order += "debug" },
        )
        assertEquals(listOf("declared", "apps", "debug"), order)

        order.clear()
        vm.onCreatePostContent(
            isDebugIntent = false,
            onRequestDeclaredPermissions = { order += "declared" },
            onRequestSpecialPermissionsOnce = { order += "special" },
            onLoadApps = { order += "apps" },
            onHandleDebugIntent = { order += "debug" },
        )
        assertEquals(listOf("declared", "special", "apps", "debug"), order)
    }

    @Test
    fun onPausePersist_delegatesSave() {
        val vm = MainViewModel()
        var saved = false
        vm.onPausePersist { saved = true }
        assertTrue(saved)
    }

    @Test
    fun onResumeRefresh_refreshFirstThenConditionalLoad() {
        val vm = MainViewModel()
        val order = mutableListOf<String>()
        vm.onResumeRefresh(
            didRequestAppLoad = true,
            appsEmpty = true,
            previousQueryGranted = true,
            previousUsageGranted = true,
            onRefreshPermissions = { order += "perm" },
            currentQueryGranted = { true },
            currentUsageGranted = { true },
            onLoadApps = { order += "apps" },
        )
        assertEquals(listOf("perm", "apps"), order)

        order.clear()
        vm.onResumeRefresh(
            didRequestAppLoad = true,
            appsEmpty = false,
            previousQueryGranted = true,
            previousUsageGranted = true,
            onRefreshPermissions = { order += "perm" },
            currentQueryGranted = { true },
            currentUsageGranted = { true },
            onLoadApps = { order += "apps" },
        )
        assertEquals(listOf("perm"), order)
    }

    @Test
    fun onNewIntentDebug_preservesOrder() {
        val vm = MainViewModel()
        val order = mutableListOf<String>()
        vm.onNewIntentDebug(
            onSetIntent = { order += "intent" },
            onHandleDebugIntent = { order += "debug" },
        )
        assertEquals(listOf("intent", "debug"), order)
    }

    @Test
    fun onDestroyCleanup_preservesOrder() {
        val vm = MainViewModel()
        val order = mutableListOf<String>()
        vm.onDestroyCleanup(
            onCancelPreviewJob = { order += "job" },
            onCancelWorkerScope = { order += "scope" },
            onCloseWorkerDispatcher = { order += "dispatcher" },
            onStopDebugServer = { order += "server" },
            onCloseRmbgRuntime = { order += "runtime" },
        )
        assertEquals(listOf("job", "scope", "dispatcher", "server", "runtime"), order)
    }

    @Test
    fun refreshGeneratedPackagesAsync_emptyWritesPickerAndShell() {
        val vm = MainViewModel()
        vm.refreshGeneratedPackagesAsync(emptyList(), FakeSharedPreferences())
        assertFalse(vm.picker.value.isScanningGeneratedPackages)
        assertFalse(vm.picker.value.generatedScanFailed)
        assertEquals("应用列表为空，保留已生成缓存", vm.shell.value.statusText)
    }
}
