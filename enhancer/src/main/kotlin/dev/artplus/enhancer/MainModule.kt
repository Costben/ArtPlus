package dev.artplus.enhancer

import android.util.Log
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import java.util.concurrent.atomic.AtomicBoolean

/**
 * LSPosed API 102 入口。
 *
 * 作用域只有桌面（com.android.launcher / com.oppo.launcher），不碰 system_server：
 *  - 可变图标接管必须在 onPackageLoaded 就挂上，IconCache / ItemInfoWithIcon 在桌面
 *    启动早期就开始建缓存，等到 onPackageReady 会漏掉第一批图标；
 *  - 玻璃托盘与图标边缘光挂的是 View 与 Drawable，onPackageReady 足够。
 */
class MainModule : XposedModule() {

    companion object {
        const val TAG = "ArtPlusEnh"

        private val LAUNCHER_PACKAGES = setOf("com.android.launcher", "com.oppo.launcher")

        lateinit var instance: MainModule
            private set

        private val morphHooked = AtomicBoolean(false)
        private val glassHooked = AtomicBoolean(false)
    }

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        super.onModuleLoaded(param)
        instance = this
        ModulePrefs.initHost(this)
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        instance = this
        if (param.packageName !in LAUNCHER_PACKAGES) return

        ModulePrefs.initHost(this)
        // 这里不挂 hook：桌面图标的第一个 fancy 请求出现在 onPackageReady 之后约 2s，
        // 统一放在 onPackageReady 用 param.classLoader 装，时机够早且只有一份代码路径。
    }

    override fun onPackageReady(param: PackageReadyParam) {
        instance = this
        ModulePrefs.initHost(this)

        if (param.packageName !in LAUNCHER_PACKAGES) return
        log(Log.INFO, TAG, "Launcher ready: ${param.packageName}")

        if (morphHooked.compareAndSet(false, true)) {
            MorphLocalHook.init(this, param.classLoader)
        }
        if (glassHooked.compareAndSet(false, true)) {
            DockGlassHook.init(this, param.classLoader)
            IconRimLightHook.init(this, param.classLoader)
        }

        // 监听设置页的刷新触发，自我退出让系统在 100ms 内重新拉起，无需 Root 即可刷新桌面
        bindRestartListener()
    }

    private fun bindRestartListener() {
        runCatching {
            val prefs = ModulePrefs.hostPrefs() ?: return
            prefs.registerOnSharedPreferenceChangeListener { _, key ->
                if (key == ModulePrefs.KEY_LAUNCHER_RESTART_TRIGGER) {
                    log(Log.INFO, TAG, "Received launcher restart trigger, self-terminating...")
                    android.os.Process.killProcess(android.os.Process.myPid())
                }
            }
        }
    }
}
