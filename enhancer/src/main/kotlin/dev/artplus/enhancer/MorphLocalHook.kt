package dev.artplus.enhancer

import android.content.ComponentName
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.util.Log
import io.github.libxposed.api.XposedModule
import java.util.Collections
import java.util.WeakHashMap

/**
 * 可变图标（morph / fancy icon）接管。
 *
 * 现象：有些应用被 ColorOS 当成「可变图标」应用，桌面拉成 2x2 时图标不是从
 * `/data/oplus/uxicons/<pkg>/recfg_2x2.png` 读，而是找系统图标引擎
 * （`OplusUIEngine.createAppsIconsCtrl(...).getMorphIcon(...)`）现生成一张，
 * ArtPlus 写进去的素材就被整组绕过了；同一张图在 1x1 上也一样会被引擎版本替换，
 * 表现为「ArtPlus 里明明生成了，桌面上还是系统图」。
 *
 * 这条链在桌面内部是：
 *   IconCache.applyCacheEntry → i6.j0 → i6.v → IconCacheExtImplV2.loadFancyIcon
 *                                              / loadFancyIconMorph → 引擎
 *   ItemInfoWithIcon.isFancyIcon() 为 true 时，2x2 的 morph 也优先走 fancy 分支。
 *
 * 所以对 ArtPlus 写过素材的包做三件事：
 *   1. `loadFancyIcon*` / `loadFancyIconMorph` 直接返回 null —— 引擎不参与，
 *      调用方自动回落到 `tr.m.n0` 那条本地 morph 路径（读 ArtPlus 的 recfg/recbg）；
 *   2. `updateFancyMorphIconCache` 不再落缓存 —— 避免下一帧又从 fancy 缓存里取回引擎图；
 *   3. `isFancyIcon()` 返回 false —— 桌面不再把这张图当 fancy 处理，
 *      2x2 也不会进入「加载 fancy 失败 → 标网络错误」那条分支。
 *
 * 三条都只对 ArtPlus 素材包生效，其余应用完全不受影响。
 */
object MorphLocalHook {

    private const val TAG = "[ArtPlusEnh:Morph]"

    private const val ICON_CACHE_EXT = "com.android.launcher3.icons.IconCacheExtImplV2"
    private const val ITEM_INFO_WITH_ICON = "com.android.launcher3.model.data.ItemInfoWithIcon"

    /** 全部返回 Drawable、且最后一个有效参数是包名或组件名的引擎入口。 */
    private val FANCY_ENTRIES = arrayOf(
        "loadFancyIcon",
        "loadFancyIconForRecent",
        "loadFancyIconForTaskBar",
        "loadFancyIconForTaskBarFolderIcon",
        "loadFancyIconMorph",
    )

    private val logged = Collections.newSetFromMap(WeakHashMap<String, Boolean>())

    @Volatile
    private var installed = false

    /**
     * 开关的本地缓存。
     *
     * `isFancyIcon()` 在桌面上是每张图标、每次绑定都会走一遍的热路径，
     * 每次去问 LSPosed 的远程配置表太奢侈：这里缓存一份，靠配置变更回调刷新。
     */
    @Volatile
    private var morphEnabled = true

    @Volatile
    private var listenerBound = false

    /** SharedPreferences 对监听器是弱引用，必须存字段保活。 */
    @Volatile
    private var prefListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    private fun morphEnabled(): Boolean {
        if (!listenerBound) bindPrefListener()
        return morphEnabled
    }

    private fun bindPrefListener() {
        synchronized(this) {
            if (listenerBound) return
            val prefs = ModulePrefs.hostPrefs() ?: return
            morphEnabled = ModulePrefs.isMorphLocalEnabled()
            // SharedPreferences 对监听器是弱引用，必须存字段保活
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == ModulePrefs.KEY_MORPH_LOCAL) {
                    morphEnabled = ModulePrefs.isMorphLocalEnabled()
                    ArtPlusAssets.invalidate()
                }
            }
            runCatching { prefs.registerOnSharedPreferenceChangeListener(listener) }
                .onSuccess {
                    prefListener = listener
                    listenerBound = true
                }
        }
    }

    fun init(module: XposedModule, classLoader: ClassLoader) {
        if (installed) return
        installed = true

        var hooked = 0

        XReflect.findClass(ICON_CACHE_EXT, classLoader)?.let { cls ->
            for (name in FANCY_ENTRIES) {
                hooked += module.hookTargetsNamed(cls, name) { chain ->
                    val pkg = packageOfArgs(chain.args)
                    if (pkg != null && isManaged(pkg)) {
                        noteOnce(module, name, pkg)
                        null
                    } else {
                        chain.proceed()
                    }
                }
            }

            // 1x1 图标锁定：即使应用动态切换别名、或向 PackageManager 取新图，
            // 只要被 ArtPlus 托管，直接在 applyCacheEntry 核心入口处以 ArtPlus 本地素材覆盖。
            val specificHook = module.hookTarget(cls, "getSpecificIcon", ComponentName::class.java) { chain ->
                val cn = chain.args.getOrNull(0) as? ComponentName
                val pkg = cn?.packageName
                if (pkg != null && isManaged(pkg)) {
                    val custom = ArtPlusAssets.loadBitmap(pkg)
                    if (custom != null) {
                        val p = runCatching { custom.getPixel(custom.width / 2, custom.height / 2) }.getOrDefault(0)
                        module.log(
                            Log.INFO, MainModule.TAG,
                            "$TAG $pkg locked to ArtPlus icon size=${custom.width}x${custom.height} centerPixel=#${Integer.toHexString(p)} (bypassed alias icon)",
                        )
                        return@hookTarget custom
                    }
                }
                chain.proceed()
            }
            if (specificHook) hooked++
        }.also {
            if (it == null) {
                module.log(Log.WARN, MainModule.TAG, "$TAG $ICON_CACHE_EXT not found in launcher")
            }
        }

        XReflect.findClass(ITEM_INFO_WITH_ICON, classLoader)?.let { cls ->
            val blockedCache = module.hookTarget(
                cls,
                "updateFancyMorphIconCache",
                Int::class.javaPrimitiveType!!,
                Int::class.javaPrimitiveType!!,
                Drawable::class.java,
            ) { chain ->
                val pkg = packageOfItem(chain.thisObject)
                if (isManaged(pkg)) null else chain.proceed()
            }
            if (blockedCache) hooked++

            val blockedFlag = module.hookTarget(cls, "isFancyIcon") { chain ->
                val pkg = packageOfItem(chain.thisObject)
                if (isManaged(pkg)) false else chain.proceed()
            }
            if (blockedFlag) hooked++
        }.also {
            if (it == null) {
                module.log(Log.WARN, MainModule.TAG, "$TAG $ITEM_INFO_WITH_ICON not found in launcher")
            }
        }

        module.log(Log.INFO, MainModule.TAG, "$TAG $hooked hook(s) installed")
        bindPrefListener()
    }

    private fun isManaged(pkg: String?): Boolean =
        pkg != null && morphEnabled() && ArtPlusAssets.has(pkg)

    /**
     * 从 hook 参数里取包名。
     *
     * 引擎入口的签名是 `(int, int, String)` 或 `(Context, ComponentName, int, int, yl.a)`，
     * 两种都能按参数值取到。参数容器不写死类型：`Chain.getArgs()` 在不同实现里可能是数组也可能是 List，
     * 写死 `Array` 会被重载决议悄悄导向另一个同名函数，取到 null 而没有任何报错。
     */
    private fun packageOfArgs(args: Any?): String? {
        val values: Iterable<Any?> = when (args) {
            is Array<*> -> args.asIterable()
            is Iterable<*> -> args
            else -> return null
        }
        for (arg in values) {
            when (arg) {
                is ComponentName -> return arg.packageName
                is String -> if (arg.isNotEmpty() && arg.contains('.')) return arg
            }
        }
        return null
    }

    /** 从 `ItemInfoWithIcon` 实例上取包名。 */
    private fun packageOfItem(host: Any?): String? {
        if (host == null) return null
        return XReflect.callMethod(host, "getTargetPackage") as? String
            ?: (XReflect.callMethod(host, "getTargetComponent") as? ComponentName)?.packageName
    }

    private fun noteOnce(module: XposedModule, method: String, pkg: String) {
        if (!logged.add(pkg)) return
        module.log(Log.INFO, MainModule.TAG, "$TAG $pkg -> local morph assets ($method bypassed)")
    }
}
