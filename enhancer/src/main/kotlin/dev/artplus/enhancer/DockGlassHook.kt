package dev.artplus.enhancer

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import java.util.Collections
import java.util.WeakHashMap

/**
 * 桌面 Dock（Hotseat）液态玻璃托盘。
 *
 * 不走官方的 `setDockerBackground()`：手机形态下它被
 * `ScreenUtils.isSupportDockerExpandScreen()` 整个锁死，而且依赖 `OplusHotseat.onDraw`，
 * 时机稍早就画出空 bounds；底色还固定取 day/night 资源，暗壁纸上等于看不见。
 *
 * 这里直接给 Dock 图标容器 `mShortcutsAndWidgets` 设背景：
 * 它的 bounds 就是图标那一行的矩形（`OplusHotseat.onLayout` 里水平居中 layout），
 * 而且整份桌面代码里只有 `OplusHotseat.updateDockerBgIfNecessary()` 会把它写成 null，
 * 不会跟别人抢。
 *
 * 模糊层用桌面自己的 `BlurBaseFactory.prepareBlurBase` + `BlurUtils.getBlurColorParams`，
 * 拿不到 SurfaceControl 时退回自绘圆角底色（按壁纸明暗选深/浅蒙版），保证任何情况下都看得见托盘。
 */
object DockGlassHook {

    private const val TAG = "[ArtPlusEnh:DockGlass]"

    private val HOTSEAT_CLASS_CANDIDATES = arrayOf(
        "com.android.launcher3.OplusHotseat",
        "com.oplus.launcher3.OplusHotseat",
        "com.android.launcher.OplusHotseat",
        "com.android.launcher3.Hotseat",
    )

    /** 只挂桌面自己的声明，避免把 View/ViewGroup 的通用方法一起挂上。 */
    private val HOOKABLE_DECLARERS = arrayOf(
        "com.android.launcher3.",
        "com.oplus.launcher3.",
    )

    private const val METHOD_ON_ATTACHED = "onAttachedToWindow"
    private const val METHOD_ON_DETACHED = "onDetachedFromWindow"
    private const val METHOD_ON_MEASURE = "onMeasure"
    private const val METHOD_ON_LAYOUT = "onLayout"
    private const val METHOD_UPDATE_DOCKER_BG = "updateDockerBgIfNecessary"
    private const val METHOD_WALLPAPER_BRIGHTNESS = "onWallpaperBrightnessChanged"

    private const val FIELD_CONTAINER = "mShortcutsAndWidgets"

    private const val BLUR_FACTORY = "com.android.launcher3.uioverrides.states.blurdrawable.BlurBaseFactory"
    private const val BLUR_UTILS = "com.android.launcher3.uioverrides.states.blurdrawable.BlurUtils"
    private const val WALLPAPER_RESOLVER = "com.android.launcher.wallpaper.z0"

    /** 与 ColorOS 原生托盘一致：30dp 圆角、blurRadius 200、blendMode 2、type 4。 */
    private const val CORNER_DP = 30f
    private const val BLUR_RADIUS_PX = 200
    private const val BLEND_MODE = 2
    private const val BLUR_TYPE = 4

    private val applied = Collections.newSetFromMap(WeakHashMap<ViewGroup, Boolean>())
    private val controllers = WeakHashMap<ViewGroup, Any>()

    /** 每个容器最近一次记录过的状态签名，状态没变就不重复写日志。 */
    private val lastState = WeakHashMap<ViewGroup, String>()

    /** 活着的 Hotseat，用于开关切换后重刷。 */
    private val hosts = Collections.newSetFromMap(WeakHashMap<View, Boolean>())

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var prefListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    fun init(module: XposedModule, classLoader: ClassLoader) {
        var hookedAny = false

        for (className in HOTSEAT_CLASS_CANDIDATES) {
            val hotseatClass = XReflect.findClass(className, classLoader) ?: continue

            val attached = hookDeclared(module, hotseatClass, METHOD_ON_ATTACHED) { chain ->
                val result = chain.proceed()
                (chain.thisObject as? View)?.let { view ->
                    hosts.add(view)
                    view.viewTreeObserver.addOnPreDrawListener(
                        object : ViewTreeObserver.OnPreDrawListener {
                            override fun onPreDraw(): Boolean {
                                view.viewTreeObserver.removeOnPreDrawListener(this)
                                applyDockBackground(chain.thisObject, module)
                                return true
                            }
                        }
                    )
                }
                result
            }

            val laidOut = hookDeclared(
                module, hotseatClass, METHOD_ON_LAYOUT,
                Boolean::class.javaPrimitiveType!!,
                Int::class.javaPrimitiveType!!,
                Int::class.javaPrimitiveType!!,
                Int::class.javaPrimitiveType!!,
                Int::class.javaPrimitiveType!!,
            ) { chain ->
                val result = chain.proceed()
                applyDockBackground(chain.thisObject, module)
                result
            }

            val measured = hookDeclared(
                module, hotseatClass, METHOD_ON_MEASURE,
                Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!,
            ) { chain ->
                val result = chain.proceed()
                applyDockBackground(chain.thisObject, module)
                result
            }

            // 手机形态下这个方法的意思就是「把容器背景清空」，跑完把托盘装回去
            val refreshed = hookDeclared(module, hotseatClass, METHOD_UPDATE_DOCKER_BG) { chain ->
                val result = chain.proceed()
                applyDockBackground(chain.thisObject, module, force = true)
                result
            }

            // 换壁纸 / 明暗变化：重挑底色
            val brightnessChanged = hookDeclared(module, hotseatClass, METHOD_WALLPAPER_BRIGHTNESS) { chain ->
                val result = chain.proceed()
                applyDockBackground(chain.thisObject, module, force = true)
                result
            }

            val detached = hookDeclared(module, hotseatClass, METHOD_ON_DETACHED) { chain ->
                (chain.thisObject as? View)?.let { view ->
                    hosts.remove(view)
                    releaseDockBackground(view)
                }
                chain.proceed()
            }

            if (attached || laidOut || measured || refreshed || brightnessChanged || detached) {
                hookedAny = true
                module.log(
                    Log.INFO, MainModule.TAG,
                    "$TAG hooked $className (attach=$attached layout=$laidOut measure=$measured " +
                        "refresh=$refreshed brightness=$brightnessChanged detach=$detached)",
                )
            }
        }

        if (!hookedAny) {
            module.log(Log.WARN, MainModule.TAG, "$TAG no Hotseat class found")
        }
        bindPrefListener(module)
    }

    private fun hookDeclared(
        module: XposedModule,
        clazz: Class<*>,
        methodName: String,
        vararg paramTypes: Class<*>,
        consumer: (XposedInterface.Chain) -> Any?,
    ): Boolean {
        val method = XReflect.findMethod(clazz, methodName, *paramTypes) ?: return false
        if (HOOKABLE_DECLARERS.none { method.declaringClass.name.startsWith(it) }) return false
        return runCatching {
            module.hook(method).intercept { chain -> consumer(chain) }
            true
        }.getOrDefault(false)
    }

    // ==========================================================
    // 开关
    // ==========================================================

    private fun bindPrefListener(module: XposedModule) {
        if (prefListener != null) return
        val prefs = ModulePrefs.hostPrefs() ?: return
        // SharedPreferences 对监听器是弱引用，必须存字段保活
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == ModulePrefs.KEY_DOCK_GLASS || key == ModulePrefs.KEY_DOCK_BLUR) {
                mainHandler.post { refreshAll(module) }
            }
        }
        runCatching { prefs.registerOnSharedPreferenceChangeListener(listener) }
            .onSuccess { prefListener = listener }
            .onFailure {
                module.log(Log.WARN, MainModule.TAG, "$TAG 开关监听注册失败：${it.message}")
            }
    }

    /** 开关切换后把已有托盘全部拆掉重装（关掉时这一步就是「全部移除」）。 */
    private fun refreshAll(module: XposedModule) {
        val snapshot = hosts.toList()
        for (host in snapshot) {
            val container = XReflect.getObjectField(host, FIELD_CONTAINER) as? ViewGroup ?: continue
            releaseController(container)
            applied.remove(container)
            container.background = null
            container.invalidate()
            applyDockBackground(host, module, force = true)
        }
        module.log(
            Log.INFO, MainModule.TAG,
            "$TAG refreshed ${snapshot.size} hotseat(s), glass=${ModulePrefs.isDockGlassEnabled()}",
        )
    }

    // ==========================================================
    // 装载 / 卸载
    // ==========================================================

    private fun applyDockBackground(host: Any?, module: XposedModule, force: Boolean = false) {
        if (host == null) return
        // 首次调用时远程配置可能还没就绪；没绑上就在每次布局里重试，绑上后不再进这里
        if (prefListener == null) bindPrefListener(module)
        try {
            val container = XReflect.getObjectField(host, FIELD_CONTAINER) as? ViewGroup ?: return
            if (!ModulePrefs.isDockGlassEnabled()) return

            val state = "children=${container.childCount} size=${container.width}x${container.height} " +
                "measured=${container.measuredWidth}x${container.measuredHeight} " +
                "at=${container.left},${container.top} attached=${container.isAttachedToWindow} " +
                "bg=${container.background?.javaClass?.simpleName}"
            if (lastState.put(container, state) != state) {
                module.log(Log.INFO, MainModule.TAG, "$TAG hotseat state: $state")
            }

            if (container.childCount <= 0) return

            val height = if (container.height > 0) container.height else container.measuredHeight
            if (height <= 0) return

            if (!force && applied.contains(container) && container.background != null) return

            releaseController(container)

            val solid = createFallbackDrawable(container)
            val blur = if (ModulePrefs.isDockBlurEnabled()) createBlurDrawable(host, container) else null
            // 模糊层叠在自绘托盘之上：模糊能用就是玻璃托盘；拿不到 SurfaceControl 时
            // getBlurDrawable 给出空 drawable，下面那层仍然可见，不会出现「hook 成功但桌面上什么都没有」。
            val drawable = if (blur != null) LayerDrawable(arrayOf(solid, blur)) else solid
            container.background = drawable
            applied.add(container)

            // 官方托盘走 OplusHotseat.onDraw + setBackgroundAlpha 控制显隐，
            // 这里的容器背景是独立的，但仍然保证 hotseat 自身 alpha 不被压到 0。
            XReflect.callMethod(host, "setBackgroundAlpha", 1.0f)
            container.invalidate()

            module.log(
                Log.INFO, MainModule.TAG,
                "$TAG dock glass applied on ${host.javaClass.simpleName} " +
                    "(h=$height, ${if (blur != null) "solid+blur" else "solid"})",
            )
        } catch (t: Throwable) {
            module.log(Log.ERROR, MainModule.TAG, "$TAG applyDockBackground failed: ${t.message}", t)
        }
    }

    private fun releaseDockBackground(view: View) {
        val container = XReflect.getObjectField(view, FIELD_CONTAINER) as? ViewGroup
        if (container != null) {
            applied.remove(container)
            releaseController(container)
            container.background = null
        } else {
            (view as? ViewGroup)?.let {
                applied.remove(it)
                releaseController(it)
                it.background = null
            }
        }
    }

    private fun releaseController(container: ViewGroup) {
        controllers.remove(container)?.let { controller ->
            runCatching { XReflect.callMethod(controller, "recycle") }
        }
    }

    // ==========================================================
    // 模糊玻璃
    // ==========================================================

    private fun createBlurDrawable(host: Any, container: ViewGroup): Drawable? {
        val classLoader = host.javaClass.classLoader ?: return null
        val context = container.context

        val controller = prepareBlurController(container, context, classLoader) ?: return null
        controllers[container] = controller

        val cornerPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, CORNER_DP, context.resources.displayMetrics,
        )
        val (blendColor, mixColor) = resolveBlurColors(context, classLoader)

        XReflect.callMethod(controller, "setBlurCornerRadius", cornerPx, false, null, true)
        XReflect.callMethod(controller, "setBlurParams", BLUR_RADIUS_PX, BLEND_MODE, blendColor, mixColor)
        XReflect.callMethod(controller, "setIgnorePageScrollBlurAnim", true)

        return XReflect.callMethod(controller, "getBlurDrawable") as? Drawable
    }

    private fun prepareBlurController(container: ViewGroup, context: Context, classLoader: ClassLoader): Any? {
        val factory = XReflect.findClass(BLUR_FACTORY, classLoader) ?: return null

        // Kotlin 伴生对象：@JvmStatic 会在外层类留静态转发方法；没有的话从 Companion 实例上调。
        val companion = XReflect.getStaticObjectField(factory, "Companion")

        val fiveArgs = arrayOf<Any?>(container, context, false, false, BLUR_TYPE)
        val sixArgs = arrayOf<Any?>(container, context, false, false, BLUR_TYPE, true)

        for (args in listOf(fiveArgs, sixArgs)) {
            runCatching { XReflect.callStaticMethod(factory, "prepareBlurBase", *args) }
                .getOrNull()?.let { return it }
            if (companion != null) {
                runCatching { XReflect.callMethod(companion, "prepareBlurBase", *args) }
                    .getOrNull()?.let { return it }
            }
        }
        return null
    }

    /** `BlurUtils.getBlurColorParams(Context)` 已按深色模式与壁纸明暗选好混合色。 */
    private fun resolveBlurColors(context: Context, classLoader: ClassLoader): Pair<Int, Int> {
        val params = runCatching {
            val cls = XReflect.findClass(BLUR_UTILS, classLoader)
            if (cls == null) null else XReflect.callStaticMethod(cls, "getBlurColorParams", context)
        }.getOrNull()

        val blend = (XReflect.callMethod(params, "getBlendColor") as? Int) ?: DEFAULT_BLEND_COLOR
        val mix = (XReflect.callMethod(params, "getMixColor") as? Int) ?: DEFAULT_MIX_COLOR
        return blend to mix
    }

    private const val DEFAULT_BLEND_COLOR = 0xFF333333.toInt()
    private const val DEFAULT_MIX_COLOR = 0x66526366

    // ==========================================================
    // 自绘兜底
    // ==========================================================

    /**
     * 模糊不可用时的退路。底色按壁纸明暗选：亮壁纸用深色蒙版，暗壁纸用浅色蒙版，
     * 两边都保证看得出托盘边界。
     */
    private fun createFallbackDrawable(container: ViewGroup): Drawable {
        val context = container.context
        val bright = isWorkspaceBright(context)

        val fill = if (bright) FILL_ON_BRIGHT_WALLPAPER else FILL_ON_DARK_WALLPAPER
        val stroke = if (bright) STROKE_ON_BRIGHT_WALLPAPER else STROKE_ON_DARK_WALLPAPER
        val cornerPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, CORNER_DP, context.resources.displayMetrics,
        )

        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = cornerPx
            setColor(fill)
            setStroke(2, stroke)
        }
    }

    private const val FILL_ON_BRIGHT_WALLPAPER = 0x33000000
    private const val FILL_ON_DARK_WALLPAPER = 0x33FFFFFF
    private const val STROKE_ON_BRIGHT_WALLPAPER = 0x1A000000
    private const val STROKE_ON_DARK_WALLPAPER = 0x2EFFFFFF

    /**
     * `com.android.launcher.wallpaper.z0.f()` = isWorkspaceBright。
     * 注意 day/night 资源判的是深色模式而不是壁纸明暗：深色模式下
     * `color/hotseat_bg_color` 取到 12% 黑，叠在暗壁纸上等于看不见。
     */
    private fun isWorkspaceBright(context: Context): Boolean {
        val resolver = XReflect.findClass(WALLPAPER_RESOLVER, context.classLoader) ?: return false
        return (XReflect.callStaticMethod(resolver, "f") as? Boolean) ?: false
    }
}
