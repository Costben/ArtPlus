package dev.artplus.enhancer

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import io.github.libxposed.api.XposedModule
import io.github.libxposed.service.XposedService

/**
 * 配置真源：LSPosed 远程配置表。
 *
 * 设置页（模块 App 进程）：`XposedService.getRemotePreferences(...)`
 * 桌面进程：`XposedModule.getRemotePreferences(...)`
 * 两端指向同一份数据，daemon 还会把变更推给桌面侧注册的监听回调。
 */
object ModulePrefs {

    const val PREFS_NAME = "artplus_enhancer_prefs"

    /** 桌面 Dock 的玻璃托盘。 */
    const val KEY_DOCK_GLASS = "launcher_dock_glass_enabled"

    /** 托盘是否叠桌面自己的模糊层；关掉只剩自绘的半透明底色。 */
    const val KEY_DOCK_BLUR = "launcher_dock_blur_enabled"

    /** 图标底板边缘光。 */
    const val KEY_ICON_RIM = "launcher_icon_rim_enabled"

    /** 图标主体（Logo）高光。 */
    const val KEY_ICON_BODY = "launcher_icon_body_enabled"

    /** 边缘光强度（百分比），100 = ColorOS 17 文件夹描边的原生强度。 */
    const val KEY_ICON_RIM_LEVEL = "launcher_icon_rim_level"

    /** 光源方向（度）：从正右方逆时针量，90° 为正上方。 */
    const val KEY_ICON_LIGHT_ANGLE = "launcher_icon_light_angle"

    /** true：与文件夹完全一致的混合（提亮底色）；false：直接叠白。 */
    const val KEY_ICON_EXACT_BLEND = "launcher_icon_exact_blend"

    /** 可变图标接管：让 ArtPlus 写过素材的包走本地 morph，而不是系统图标引擎。 */
    const val KEY_MORPH_LOCAL = "morph_local_enabled"

    /** 桌面刷新触发时间戳。每次点击右上角刷新按钮更新，桌面监听到此键变化直接自我重启。 */
    const val KEY_LAUNCHER_RESTART_TRIGGER = "launcher_restart_trigger"

    const val RIM_LEVEL_MIN = 0
    const val RIM_LEVEL_MAX = 100
    const val RIM_LEVEL_DEFAULT = 100

    const val LIGHT_ANGLE_MIN = 0
    const val LIGHT_ANGLE_MAX = 359
    const val LIGHT_ANGLE_DEFAULT = 90

    private const val HOST_TAG = "ArtPlusEnh:Prefs"

    // ==========================================
    // 桌面进程读取
    // ==========================================

    @Volatile
    private var remotePrefs: SharedPreferences? = null

    fun initHost(module: XposedModule) {
        if (remotePrefs != null) return
        runCatching {
            remotePrefs = module.getRemotePreferences(PREFS_NAME)
            module.log(Log.INFO, HOST_TAG, "Remote preferences bridged")
        }.onFailure { t ->
            module.log(Log.WARN, HOST_TAG, "getRemotePreferences failed: ${t.message}")
        }
    }

    fun hostPrefs(): SharedPreferences? = remotePrefs

    /**
     * 键不存在时用默认值。
     *
     * 必须显式判 contains：LSPosed 远程配置对缺失的布尔键直接返回 false，传进去的 default 不生效，
     * 会让默认打开的开关读成关闭。
     */
    private fun readBoolean(key: String, default: Boolean): Boolean {
        val prefs = remotePrefs ?: return default
        return runCatching { if (prefs.contains(key)) prefs.getBoolean(key, default) else default }
            .getOrDefault(default)
    }

    private fun readInt(key: String, default: Int, min: Int, max: Int): Int {
        val prefs = remotePrefs ?: return default
        return runCatching {
            if (prefs.contains(key)) prefs.getInt(key, default).coerceIn(min, max) else default
        }.getOrDefault(default)
    }

    fun isDockGlassEnabled(): Boolean = readBoolean(KEY_DOCK_GLASS, false)

    fun isDockBlurEnabled(): Boolean = readBoolean(KEY_DOCK_BLUR, true)

    fun isIconRimEnabled(): Boolean = readBoolean(KEY_ICON_RIM, false)

    fun isIconBodyLightEnabled(): Boolean = readBoolean(KEY_ICON_BODY, false)

    fun rimLevel(): Int = readInt(KEY_ICON_RIM_LEVEL, RIM_LEVEL_DEFAULT, RIM_LEVEL_MIN, RIM_LEVEL_MAX)

    fun lightAngle(): Int = readInt(KEY_ICON_LIGHT_ANGLE, LIGHT_ANGLE_DEFAULT, LIGHT_ANGLE_MIN, LIGHT_ANGLE_MAX)

    fun isIconExactBlendEnabled(): Boolean = readBoolean(KEY_ICON_EXACT_BLEND, false)

    /**
     * 可变图标接管默认打开：这是插件存在的理由，装上就该生效；
     * 只影响 ArtPlus 真正写过素材的包（判据见 [ArtPlusAssets]）。
     */
    fun isMorphLocalEnabled(): Boolean = readBoolean(KEY_MORPH_LOCAL, true)

    // ==========================================
    // 设置页（模块 App 进程）读写
    // ==========================================

    @Volatile
    private var appRemotePrefs: SharedPreferences? = null

    @Volatile
    private var appLocalPrefs: SharedPreferences? = null

    @Volatile
    private var appContext: Context? = null

    /** 本地表已并入远程表的标记；两边都写，避免远程短暂不可用时反复回灌旧值。 */
    private const val KEY_MIGRATED = "prefs_migrated"

    /** 必须在设置页 onCreate 里先调一次，否则迁移拿不到 Context。 */
    fun setAppContext(context: Context) {
        appContext = context.applicationContext
        migrateLocalToRemote()
    }

    /** 框架服务绑定/断开时调用；返回 true 表示配置能真正同步给桌面。 */
    fun attachService(service: XposedService?): Boolean {
        val prefs = runCatching { service?.getRemotePreferences(PREFS_NAME) }.getOrNull()
        appRemotePrefs = prefs
        if (prefs != null) migrateLocalToRemote()
        return prefs != null
    }

    /**
     * 服务还没绑上时用户拨的开关只会写进本地表，远程表永远收不到。
     * 远程表一可用就把本地值补推一次，之后本地表不再参与读写。
     */
    private fun migrateLocalToRemote() {
        val remote = appRemotePrefs ?: return
        val context = appContext ?: return
        val local = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (local === remote || local.getBoolean(KEY_MIGRATED, false)) return
        runCatching {
            val editor = remote.edit()
            for ((key, value) in local.all) {
                if (key == KEY_MIGRATED) continue
                when (value) {
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is String -> editor.putString(key, value)
                }
            }
            editor.putBoolean(KEY_MIGRATED, true).apply()
            local.edit().putBoolean(KEY_MIGRATED, true).apply()
        }
    }

    fun isChannelReady(): Boolean = appRemotePrefs != null

    fun appPrefs(context: Context): SharedPreferences {
        val remote = appRemotePrefs
        if (remote != null) return remote
        appLocalPrefs?.let { return it }
        synchronized(this) {
            appLocalPrefs?.let { return it }
            val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            appLocalPrefs = sp
            return sp
        }
    }

    fun setBoolean(context: Context, key: String, value: Boolean) {
        appPrefs(context).edit().putBoolean(key, value).apply()
    }

    fun setInt(context: Context, key: String, value: Int) {
        appPrefs(context).edit().putInt(key, value).apply()
    }

    fun getBoolean(context: Context, key: String, default: Boolean): Boolean {
        val prefs = appPrefs(context)
        return runCatching { if (prefs.contains(key)) prefs.getBoolean(key, default) else default }
            .getOrDefault(default)
    }

    fun getInt(context: Context, key: String, default: Int): Int {
        val prefs = appPrefs(context)
        return runCatching { if (prefs.contains(key)) prefs.getInt(key, default) else default }
            .getOrDefault(default)
    }

    /**
     * 触发系统桌面刷新。
     *
     * 核心路径：写入远程配置 [KEY_LAUNCHER_RESTART_TRIGGER]，受 hook 的桌面进程收到
     * 回调后自我退出（系统在 100ms 内重新拉起），无需 Root 也能 100% 刷新生效；
     * 辅路兜底：若具备 Root 权限，顺带执行 am force-stop 保证即使远程表断开也能强制杀掉。
     */
    fun triggerLauncherRestart(context: Context) {
        val ts = System.currentTimeMillis()
        appPrefs(context).edit().putLong(KEY_LAUNCHER_RESTART_TRIGGER, ts).apply()
        kotlin.concurrent.thread(isDaemon = true) {
            runCatching {
                ProcessBuilder("su", "-c", "am force-stop com.android.launcher; kill -9 $(pidof com.android.launcher 2>/dev/null) 2>/dev/null")
                    .redirectErrorStream(true)
                    .start()
                    .waitFor()
            }
        }
    }
}
