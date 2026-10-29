package dev.artplus.enhancer

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.github.libxposed.api.XposedModule
import java.lang.ref.WeakReference
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 桌面图标边缘光（液态玻璃的「边缘高光」那一半）。
 *
 * 移植自 ColorOS 17 桌面文件夹的纹理描边：`GradientDrawableWrapper` 沿圆角矩形路径画一圈
 * 2×6px 描边，着色器是 `GradientStrokeShader`（AGSL），白色、近光端 alpha 0.35、
 * 远光端 0.2，两端各自朝侧边淡出。
 *
 * 文件夹形状已知（圆角矩形）可以直接用路径；应用图标形状随主题变化，所以这里改从图标位图的
 * alpha 求边缘带与外法线，按法线与光照方向的夹角分配近/远光，效果等价且对任意形状成立。
 *
 * 主体高光（仿苹果模式下底板之外再描一圈）的蒙版来源：
 *  - 主题图标（ThemedIconDrawable）：底板与单色图形本来就是两张位图，直接取 mMonoIcon；
 *  - 普通图标：只剩合成好的一张位图，沿底板内侧取样拟合底板颜色（c = a + b·x + c·y），
 *    偏离拟合值足够远的像素算主体。底板是照片或复杂纹理时残差过大，主动放弃主体高光。
 *
 * 高光只算一次：每张图标位图对应一张 ALPHA_8 叠加层，后台线程生成，绘制时用白色画笔
 * 叠在 drawInternal 之后。不改图标缓存，关掉开关即恢复原样。
 */
object IconRimLightHook {

    private const val TAG = "[ArtPlusEnh:IconRim]"

    private const val FAST_BITMAP_DRAWABLE = "com.android.launcher3.icons.FastBitmapDrawable"
    private const val THEMED_ICON_DRAWABLE = "com.android.launcher3.icons.ThemedIconDrawable"

    /**
     * 只处理真正画应用图标位图的类。
     * ColorOS 17 上普通图标使用 [OplusFastBitmapDrawable]，主题图标使用 [OplusThemedIconDrawable]；
     * 父类 [FastBitmapDrawable] / [ThemedIconDrawable] 作为回退保留。
     */
    private val PLAIN_ICON_CLASSES = listOf(
        "com.oplus.icons.OplusFastBitmapDrawable",
        FAST_BITMAP_DRAWABLE,
    )

    private val THEMED_ICON_CLASSES = listOf(
        "com.android.launcher3.icons.OplusThemedIconDrawable",
        THEMED_ICON_DRAWABLE,
    )

    private const val METHOD_DRAW_INTERNAL = "drawInternal"
    private const val FIELD_THEMED_BG = "mBgBitmap"
    private const val FIELD_THEMED_MONO = "mMonoIcon"

    private lateinit var module: XposedModule

    @Volatile
    private var style: Style = Style.OFF

    /** 叠加层缓存。值是 ALPHA_8 位图，或 [NOTHING]（算过，没有可画的）。 */
    private val overlays = WeakHashMap<Bitmap, Any>()
    private val pending = WeakHashMap<Bitmap, MutableList<WeakReference<Drawable>>>()

    /** 画过高光的图标；切换开关时逐个 invalidate 让桌面重画。 */
    private val painted = Collections.newSetFromMap(WeakHashMap<Drawable, Boolean>())

    private val NOTHING = Any()

    /** 防止子类调用 `super.drawInternal` 导致同一帧重复叠加高光。 */
    private val drawingDepth = ThreadLocal.withInitial { 0 }

    private val worker = Executors.newSingleThreadExecutor { r ->
        Thread(r, "ArtPlusEnh-IconRim").apply { isDaemon = true }
    }
    private val mainHandler = Handler(Looper.getMainLooper())

    private val paint = ThreadLocal.withInitial {
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply { color = Color.WHITE }
    }

    private var prefListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    fun init(module: XposedModule, classLoader: ClassLoader) {
        this.module = module
        style = Style.current()

        var plainHooked = 0
        for (className in PLAIN_ICON_CLASSES) {
            val cls = XReflect.findClass(className, classLoader) ?: continue
            val ok = hookDrawInternal(cls) { drawable, canvas, rect ->
                if (XReflect.callMethod(drawable, "isDisabled") == true) return@hookDrawInternal
                val bitmap = XReflect.callMethod(drawable, "getBitmap") as? Bitmap ?: return@hookDrawInternal
                drawOverlay(drawable, canvas, rect, plate = bitmap, body = null)
            }
            if (ok) plainHooked++
        }

        var themedHooked = 0
        for (className in THEMED_ICON_CLASSES) {
            val cls = XReflect.findClass(className, classLoader) ?: continue
            val ok = hookDrawInternal(cls) { drawable, canvas, rect ->
                val bg = XReflect.getObjectField(drawable, FIELD_THEMED_BG) as? Bitmap ?: return@hookDrawInternal
                val mono = XReflect.getObjectField(drawable, FIELD_THEMED_MONO) as? Bitmap
                drawOverlay(drawable, canvas, rect, plate = bg, body = mono)
            }
            if (ok) themedHooked++
        }

        bindPrefListener()
        module.log(
            Log.INFO, MainModule.TAG,
            "$TAG hooked plain=$plainHooked themed=$themedHooked style=$style",
        )
    }

    private fun hookDrawInternal(clazz: Class<*>, after: (Drawable, Canvas, Rect) -> Unit): Boolean {
        val method = XReflect.findMethod(clazz, METHOD_DRAW_INTERNAL, Canvas::class.java, Rect::class.java)
            ?: return false
        if (method.declaringClass != clazz) return false
        return runCatching {
            module.hook(method).intercept { chain ->
                val depth = drawingDepth.get() ?: 0
                drawingDepth.set(depth + 1)
                try {
                    val result = chain.proceed()
                    // 只在最外层的 drawInternal 完成后绘制一次，避免子类调 super 时重复绘制
                    if (depth == 0) {
                        if (prefListener == null) bindPrefListener()
                        if (style.enabled) {
                            val drawable = chain.thisObject as? Drawable
                            val canvas = chain.args.getOrNull(0) as? Canvas
                            val rect = chain.args.getOrNull(1) as? Rect
                            if (drawable != null && canvas != null && rect != null) {
                                runCatching { after(drawable, canvas, rect) }
                            }
                        }
                    }
                    result
                } finally {
                    drawingDepth.set(depth)
                }
            }
            true
        }.getOrDefault(false)
    }

    // ==========================================================
    // 绘制
    // ==========================================================

    private fun drawOverlay(drawable: Drawable, canvas: Canvas, rect: Rect, plate: Bitmap, body: Bitmap?) {
        if (plate.isRecycled || rect.isEmpty) return
        // 主题图标的底板是所有图标共用的一张，必须按主体区分
        val key = body ?: plate

        val overlay = synchronized(overlays) { overlays[key] }
        when {
            overlay is Bitmap -> {
                val p = paint.get()!!
                p.alpha = drawable.alpha
                canvas.drawBitmap(overlay, null, rect, p)
                synchronized(painted) { painted.add(drawable) }
            }
            overlay == null -> schedule(key, plate, body, drawable)
        }
    }

    private fun schedule(key: Bitmap, plate: Bitmap, body: Bitmap?, drawable: Drawable) {
        val snapshot = style
        synchronized(overlays) {
            val waiters = pending[key]
            if (waiters != null) {
                waiters.add(WeakReference(drawable))
                return
            }
            pending[key] = mutableListOf(WeakReference(drawable))
        }

        worker.execute {
            val result = runCatching { RimLight.build(plate, body, snapshot) }
                .onFailure { module.log(Log.WARN, MainModule.TAG, "$TAG build failed: ${it.message}") }
                .getOrNull()

            val waiters = synchronized(overlays) {
                // 生成期间设置变了：结果作废，留给下一次绘制重新排队
                if (snapshot == style) overlays[key] = result ?: NOTHING
                pending.remove(key)
            }
            if (result != null && waiters != null) {
                mainHandler.post { waiters.forEach { it.get()?.invalidateSelf() } }
            }
        }
    }

    // ==========================================================
    // 开关
    // ==========================================================

    private fun bindPrefListener() {
        if (prefListener != null) return
        runCatching {
            val prefs = ModulePrefs.hostPrefs() ?: return@runCatching
            // SharedPreferences 对监听器是弱引用，必须存字段保活
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    ModulePrefs.KEY_ICON_RIM,
                    ModulePrefs.KEY_ICON_BODY,
                    ModulePrefs.KEY_ICON_RIM_LEVEL,
                    ModulePrefs.KEY_ICON_LIGHT_ANGLE,
                    ModulePrefs.KEY_ICON_EXACT_BLEND,
                    -> restyle()
                }
            }
            prefListener = listener
            prefs.registerOnSharedPreferenceChangeListener(listener)
            restyle()
        }.onFailure {
            module.log(Log.WARN, MainModule.TAG, "$TAG 开关监听注册失败：${it.message}")
        }
    }

    private fun restyle() {
        val next = Style.current()
        if (next == style) return
        style = next
        synchronized(overlays) { overlays.clear() }
        val targets = synchronized(painted) { painted.toList().also { painted.clear() } }
        mainHandler.post { targets.forEach { it.invalidateSelf() } }
        module.log(Log.INFO, MainModule.TAG, "$TAG style -> $next, redraw ${targets.size} icons")
    }

    // ==========================================================
    // 样式
    // ==========================================================

    data class Style(
        val enabled: Boolean,
        /** 是否给主体加高光（仿苹果）；关闭时只有底板边缘光（ColorOS 17）。 */
        val body: Boolean,
        /** 强度倍率，1 = ColorOS 17 文件夹原生强度。 */
        val strength: Float,
        /** 光源方向（度），从正右方逆时针量，90° 为正上方。 */
        val angle: Int,
        /**
         * true：与文件夹完全一致的混合（把底下的颜色提亮一倍），深色图标上会很淡。
         * false：同样的线形，但直接叠白，深色图标上也和文件夹一样亮。
         */
        val exactBlend: Boolean,
    ) {
        /** 指向光源的单位向量，屏幕坐标系（y 朝下）。 */
        val lightX: Float get() = cos(Math.toRadians(angle.toDouble())).toFloat()
        val lightY: Float get() = -sin(Math.toRadians(angle.toDouble())).toFloat()

        companion object {
            val OFF = Style(enabled = false, body = false, strength = 0f, angle = 90, exactBlend = false)

            fun current(): Style = Style(
                enabled = ModulePrefs.isIconRimEnabled(),
                body = ModulePrefs.isIconBodyLightEnabled(),
                strength = ModulePrefs.rimLevel() / 100f,
                angle = ModulePrefs.lightAngle(),
                exactBlend = ModulePrefs.isIconExactBlendEnabled(),
            )
        }
    }

    // ==========================================================
    // 高光生成（纯 CPU，在后台线程跑）
    // ==========================================================

    private object RimLight {

        /**
         * 文件夹边缘光的真正来源：com.oplus.posteffect 的 gradientStroke 模块，
         * 着色器函数 blendEdge；小文件夹参数是 LauncherMaterialEffectUtils.SMALL_FOLDER。
         * 参数进着色器前会打包成 u_edgeArray：每组「实色宽度 + 渐隐宽度」合成 (实色, 实色 + 渐隐)。
         *
         *   近光线：实色 2.5px，渐隐到 7px，alpha 0.3   —— 屏幕上是下边（粗）
         *   远光线：实色 2px，  渐隐到 4px，alpha 0.4   —— 屏幕上是上边（细、亮）
         *   沿轮廓衰减：离线中心 5% 周长内满强度，35% 处衰减到 0（周长按宽 + 高计）
         *   深度曲线：s' = s^0.8 · 0.5 + s · 0.5
         *
         * 宽度单位是屏幕像素；桌面图标位图与屏幕上的图标基本等大，这里直接按位图像素用。
         */
        private class StrokeParams(
            val nearSolid: Float, val nearEnd: Float,
            val farSolid: Float, val farEnd: Float,
            val alphaNear: Float, val alphaFar: Float,
            val nearFadeSolid: Float, val nearFadeEnd: Float,
            val farFadeSolid: Float, val farFadeEnd: Float,
            val curvePower: Float, val curveMix: Float,
        ) {
            fun scaled(k: Float) = StrokeParams(
                nearSolid * k, nearEnd * k, farSolid * k, farEnd * k,
                alphaNear, alphaFar, nearFadeSolid, nearFadeEnd, farFadeSolid, farFadeEnd,
                curvePower, curveMix,
            )
        }

        private val SMALL_FOLDER = StrokeParams(
            nearSolid = 2.5f, nearEnd = 2.5f + 4.5f,
            farSolid = 2.0f, farEnd = 2.0f + 2.0f,
            alphaNear = 0.3f, alphaFar = 0.4f,
            nearFadeSolid = 0.05f, nearFadeEnd = 0.05f + 0.3f,
            farFadeSolid = 0.05f, farFadeEnd = 0.05f + 0.3f,
            curvePower = 0.8f, curveMix = 0.5f,
        )

        /** 主体（Logo）的线条比底板细。 */
        private val BODY = SMALL_FOLDER.scaled(0.6f)

        /**
         * 叠白色模式的增益。文件夹本身是浅灰，「提亮一倍」在它上面几乎就是白色，
         * 再叠上背景模糊层的内发光，实测上边 0.60 / 下边 0.50，约为着色器 alpha 的 1.5 倍。
         */
        private const val WHITE_GAIN = 1.5f

        /** 底板颜色拟合的残差上限（0..1）。超出说明底板是照片或复杂纹理，放弃主体高光。 */
        private const val MAX_BG_RESIDUAL = 0.10f
        private const val MIN_BODY_THRESHOLD = 0.12f

        fun build(plateBitmap: Bitmap, bodyBitmap: Bitmap?, style: Style): Bitmap? {
            val plate = readable(plateBitmap) ?: return null
            val w = plate.width
            val h = plate.height
            if (w < 16 || h < 16) return null
            val size = min(w, h)

            val argb = IntArray(w * h)
            plate.getPixels(argb, 0, w, 0, 0, w, h)
            if (plate !== plateBitmap) plate.recycle()

            val alpha = FloatArray(w * h) { (argb[it] ushr 24) / 255f }

            // 底板和主体共用同一光源，否则光影会打架
            val light = FloatArray(w * h)
            addEdge(light, alpha, w, h, style, SMALL_FOLDER)

            if (style.body) {
                val body = if (bodyBitmap != null) maskOf(bodyBitmap, w, h)
                else segmentBody(argb, alpha, w, h, size)
                if (body != null) addEdge(light, body, w, h, style, BODY)
            }

            val gain = (if (style.exactBlend) 1f else WHITE_GAIN) * style.strength
            var peak = 0
            val out = IntArray(w * h)
            for (i in out.indices) {
                val lineAlpha = min(1f, light[i] * gain)
                val a = (lineAlpha * alpha[i] * 255f).roundToInt()
                if (a > peak) peak = a
                if (a == 0) continue
                out[i] = (a shl 24) or (if (style.exactBlend) brightened(argb[i]) else 0xFFFFFF)
            }
            if (peak < 4) return null

            val tmp = Bitmap.createBitmap(out, w, h, Bitmap.Config.ARGB_8888)
            if (style.exactBlend) return tmp
            return tmp.extractAlpha().also { tmp.recycle() }
        }

        /**
         * blendEdge 的混合：底色先按亮度补一点（越暗补得越多），再与白色线色做 overlay，
         * 线色是白色时 overlay 退化为「乘 2」。结果以 lineAlpha 与原色 mix，
         * 在这里等价于把这个颜色以 lineAlpha 的不透明度叠上去。
         */
        private fun brightened(c: Int): Int {
            val r = ((c shr 16) and 0xFF) / 255f
            val g = ((c shr 8) and 0xFF) / 255f
            val b = (c and 0xFF) / 255f
            val gray = ((0.2126f * r + 0.7152f * g + 0.0722f * b) * 1.2f).coerceIn(0f, 1f)
            var improve = 1f - gray
            improve *= improve
            improve *= improve
            improve *= 0.1f
            fun ch(v: Float) = (min(1f, v + improve) * 2f).coerceIn(0f, 1f).times(255f).roundToInt()
            return (ch(r) shl 16) or (ch(g) shl 8) or ch(b)
        }

        /**
         * 逐像素执行 blendEdge 的线形部分，结果累加进 [out]（lineAlpha，未乘增益）。
         * d 是到轮廓的有符号距离（内负外正），pos / halfSize 取自形状的外接矩形；
         * 光照角度通过旋转坐标系实现：把光源方向转到「正上方」再套文件夹的公式。
         */
        private fun addEdge(out: FloatArray, mask: FloatArray, w: Int, h: Int, style: Style, p: StrokeParams) {
            var minX = w
            var minY = h
            var maxX = -1
            var maxY = -1
            for (y in 0 until h) for (x in 0 until w) {
                if (mask[y * w + x] <= 0.5f) continue
                minX = min(minX, x)
                maxX = max(maxX, x)
                minY = min(minY, y)
                maxY = max(maxY, y)
            }
            if (maxX < minX) return

            val sdf = signedDistance(mask, w, h)
            val cx = (minX + maxX + 1) * 0.5f
            val cy = (minY + maxY + 1) * 0.5f
            val bx = (maxX - minX + 1) * 0.5f
            val by = (maxY - minY + 1) * 0.5f

            // 旋转后的坐标轴：v 指向光源，u 垂直于它；θ = 90° 时 u = x、v = -y
            val lx = style.lightX
            val ly = style.lightY
            val ux = -ly
            val uy = lx
            val hx = abs(ux) * bx + abs(uy) * by
            val hy = abs(lx) * bx + abs(ly) * by
            val reach = -max(p.nearEnd, p.farEnd)

            for (y in 0 until h) {
                val py = y + 0.5f - cy
                for (x in 0 until w) {
                    val i = y * w + x
                    if (mask[i] <= 0.01f) continue
                    val d = sdf[i]
                    if (d < reach) continue
                    val px = x + 0.5f - cx
                    // 文件夹着色器的坐标系 y 朝上：远光线（细、亮）落在光源一侧
                    val sx = px * ux + py * uy
                    val sy = px * lx + py * ly
                    out[i] += edgeAlpha(sx, sy, hx, hy, d, p)
                }
            }
        }

        /** blendEdge 的线形部分，逐行对照 posteffect 的 AGSL。 */
        private fun edgeAlpha(px: Float, py: Float, hx: Float, hy: Float, d: Float, p: StrokeParams): Float {
            val distanceAll = (hx + hy) * 2f
            val ratio = (hx / distanceAll * 0.5f + 0.5f) % 1f
            val len = sqrt(px * px + py * py)
            if (len < 1e-3f) return 0f
            val dx = px / len
            val dy = py / len
            val ax = abs(dx)
            val ay = abs(dy)
            val pnx = min(ax * hy / max(0.001f, ay), hx) * sign(dx)
            val pny = min(ay * hx / max(0.001f, ax), hy) * sign(dy)
            val flag = (if (ratio <= 0.5f) 1f else -1f) * (-hy * pnx + hx * pny)
            val disPoint = distanceAll * min(ratio, 1f - ratio) * 2f
            val disPos = abs(pnx - hx) + abs(pny - hy)
            var near = if (flag >= 0f) abs(disPoint - disPos) else disPoint + disPos
            near = min(2f * distanceAll - near, near)

            val nearAlpha = p.alphaNear *
                smoothstep(p.nearFadeEnd * distanceAll, p.nearFadeSolid * distanceAll, near)
            val farAlpha = p.alphaFar *
                smoothstep(p.farFadeEnd * distanceAll, p.farFadeSolid * distanceAll, distanceAll - near)

            var nearSdf = smoothstep(-p.nearEnd, -p.nearSolid, d)
            var farSdf = smoothstep(-p.farEnd, -p.farSolid, d)
            if (p.curveMix > 1e-5f) {
                nearSdf = nearSdf.pow(p.curvePower) * p.curveMix + (1f - p.curveMix) * nearSdf
                farSdf = farSdf.pow(p.curvePower) * p.curveMix + (1f - p.curveMix) * farSdf
            }
            return (nearAlpha * nearSdf + farAlpha * farSdf).coerceIn(0f, 1f)
        }

        /**
         * 到轮廓的有符号距离（像素）：形状内为负，外为正。
         * 以 mask > 0.5 为形状，画布外一律算形状外（图标底板贴着位图边缘画满），
         * 精确欧氏距离变换（Felzenszwalb），两遍一维。
         */
        private fun signedDistance(mask: FloatArray, w: Int, h: Int): FloatArray {
            val pw = w + 2
            val ph = h + 2
            val far = 1e10
            val grid = DoubleArray(pw * ph) { idx ->
                val x = idx % pw - 1
                val y = idx / pw - 1
                if (x in 0 until w && y in 0 until h && mask[y * w + x] > 0.5f) far else 0.0
            }
            val n = max(pw, ph)
            val f = DoubleArray(n)
            val d = DoubleArray(n)
            val v = IntArray(n)
            val z = DoubleArray(n + 1)
            for (x in 0 until pw) {
                for (y in 0 until ph) f[y] = grid[y * pw + x]
                distance1d(f, ph, d, v, z)
                for (y in 0 until ph) grid[y * pw + x] = d[y]
            }
            for (y in 0 until ph) {
                val row = y * pw
                for (x in 0 until pw) f[x] = grid[row + x]
                distance1d(f, pw, d, v, z)
                for (x in 0 until pw) grid[row + x] = d[x]
            }
            return FloatArray(w * h) { i ->
                val inside = grid[(i / w + 1) * pw + (i % w + 1)]
                // 像素中心到最近的形状外像素中心的距离，减半个像素才是到边界的距离
                if (inside > 0.0) -(sqrt(inside).toFloat() - 0.5f) else 0.5f
            }
        }

        private fun intersect(f: DoubleArray, q: Int, r: Int): Double =
            ((f[q] + q.toDouble() * q) - (f[r] + r.toDouble() * r)) / (2.0 * q - 2.0 * r)

        private fun distance1d(f: DoubleArray, n: Int, d: DoubleArray, v: IntArray, z: DoubleArray) {
            var k = 0
            v[0] = 0
            z[0] = Double.NEGATIVE_INFINITY
            z[1] = Double.POSITIVE_INFINITY
            for (q in 1 until n) {
                // z[0] 是负无穷，所以 k 不会退到 0 以下
                var s = intersect(f, q, v[k])
                while (s <= z[k]) {
                    k--
                    s = intersect(f, q, v[k])
                }
                k++
                v[k] = q
                z[k] = s
                z[k + 1] = Double.POSITIVE_INFINITY
            }
            k = 0
            for (q in 0 until n) {
                while (z[k + 1] < q) k++
                val r = v[k]
                d[q] = (q - r).toDouble() * (q - r) + f[r]
            }
        }

        /**
         * 从合成好的图标里分出主体：沿底板内侧取样拟合底板颜色，偏离拟合值的像素算主体。
         * 返回 null 表示这张图标不适合做主体高光（没有底板、底板太花、或主体占比异常）。
         */
        private fun segmentBody(argb: IntArray, alpha: FloatArray, w: Int, h: Int, size: Int): FloatArray? {
            // 没有底板的图标（异形透明图标）：alpha 本身就是主体轮廓，底板边缘光已经描过了
            var solid = 0
            var minX = w
            var minY = h
            var maxX = -1
            var maxY = -1
            for (y in 0 until h) for (x in 0 until w) {
                if (alpha[y * w + x] <= 0.5f) continue
                solid++
                minX = min(minX, x)
                maxX = max(maxX, x)
                minY = min(minY, y)
                maxY = max(maxY, y)
            }
            if (solid == 0) return null
            val bboxArea = (maxX - minX + 1) * (maxY - minY + 1)
            if (solid < bboxArea * 0.75f) return null

            // 取样环：避开边缘高光带（plateBlur 接近 1），又不深入图标中心
            val plateBlur = blur(alpha, w, h, max(1, (size * 0.02f).roundToInt()))
            val ringRadius = max(2, (size * 0.07f).roundToInt())
            val ringBlur = blur(alpha, w, h, ringRadius)
            val fit = BackgroundFit()
            for (i in alpha.indices) {
                if (alpha[i] < 0.99f || plateBlur[i] < 0.99f || ringBlur[i] > 0.97f) continue
                val c = argb[i]
                fit.add(
                    (i % w).toFloat() / w, (i / w).toFloat() / h,
                    ((c shr 16) and 0xFF) / 255f, ((c shr 8) and 0xFF) / 255f, (c and 0xFF) / 255f,
                )
            }
            if (!fit.solve()) return null
            val residual = fit.residual()
            if (residual > MAX_BG_RESIDUAL) return null

            val threshold = max(MIN_BODY_THRESHOLD, residual * 3f)
            val body = FloatArray(w * h)
            var covered = 0f
            var plateArea = 0f
            for (i in alpha.indices) {
                val a = alpha[i]
                if (a <= 0.5f) continue
                plateArea += a
                val c = argb[i]
                val d = fit.distance(
                    (i % w).toFloat() / w, (i / w).toFloat() / h,
                    ((c shr 16) and 0xFF) / 255f, ((c shr 8) and 0xFF) / 255f, (c and 0xFF) / 255f,
                )
                val v = smoothstep(threshold * 0.7f, threshold * 1.3f, d) * a
                body[i] = v
                covered += v
            }
            val coverage = covered / max(1f, plateArea)
            if (coverage < 0.01f || coverage > 0.8f) return null
            return body
        }

        /** 底板颜色模型：每个通道 c = a + b·x + c·y，最小二乘拟合。 */
        private class BackgroundFit {
            private val sums = DoubleArray(6) // n, Σx, Σy, Σxx, Σxy, Σyy
            private val rhs = Array(3) { DoubleArray(3) } // 每通道 Σc, Σcx, Σcy
            private val coef = Array(3) { DoubleArray(3) }
            private val samples = ArrayList<FloatArray>()

            fun add(x: Float, y: Float, r: Float, g: Float, b: Float) {
                sums[0] += 1.0
                sums[1] += x
                sums[2] += y
                sums[3] += x * x
                sums[4] += x * y
                sums[5] += y * y
                accumulate(0, r, x, y)
                accumulate(1, g, x, y)
                accumulate(2, b, x, y)
                // 残差只需要粗估，抽样即可
                if (sums[0].toInt() % 4 == 0) samples.add(floatArrayOf(x, y, r, g, b))
            }

            private fun accumulate(ch: Int, c: Float, x: Float, y: Float) {
                rhs[ch][0] += c
                rhs[ch][1] += c * x
                rhs[ch][2] += c * y
            }

            fun solve(): Boolean {
                if (sums[0] < 64) return false
                val m = arrayOf(
                    doubleArrayOf(sums[0], sums[1], sums[2]),
                    doubleArrayOf(sums[1], sums[3], sums[4]),
                    doubleArrayOf(sums[2], sums[4], sums[5]),
                )
                for (ch in 0 until 3) {
                    coef[ch] = solve3(m, rhs[ch]) ?: return false
                }
                return true
            }

            fun residual(): Float {
                if (samples.isEmpty()) return 1f
                var sum = 0.0
                for (s in samples) {
                    val d = distance(s[0], s[1], s[2], s[3], s[4])
                    sum += d * d
                }
                return sqrt(sum / samples.size).toFloat()
            }

            /** 与拟合底板色的距离，归一化到 0..1。 */
            fun distance(x: Float, y: Float, r: Float, g: Float, b: Float): Float {
                val dr = r - predict(0, x, y)
                val dg = g - predict(1, x, y)
                val db = b - predict(2, x, y)
                return sqrt((dr * dr + dg * dg + db * db) / 3f)
            }

            private fun predict(ch: Int, x: Float, y: Float): Float =
                (coef[ch][0] + coef[ch][1] * x + coef[ch][2] * y).toFloat()

            private fun solve3(m: Array<DoubleArray>, v: DoubleArray): DoubleArray? {
                val det = det3(m)
                if (abs(det) < 1e-9) {
                    // 取样点几乎共线（极少见），退化成常数色
                    return if (sums[0] > 0) doubleArrayOf(v[0] / sums[0], 0.0, 0.0) else null
                }
                return DoubleArray(3) { col ->
                    val c = Array(3) { r -> DoubleArray(3) { k -> if (k == col) v[r] else m[r][k] } }
                    det3(c) / det
                }
            }

            private fun det3(m: Array<DoubleArray>): Double =
                m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1]) -
                    m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0]) +
                    m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0])
        }

        private fun maskOf(bitmap: Bitmap, w: Int, h: Int): FloatArray? {
            val src = readable(bitmap) ?: return null
            val scaled = if (src.width == w && src.height == h) src
            else Bitmap.createScaledBitmap(src, w, h, true)
            val px = IntArray(w * h)
            scaled.getPixels(px, 0, w, 0, 0, w, h)
            if (scaled !== bitmap) scaled.recycle()
            if (src !== bitmap && src !== scaled) src.recycle()
            return FloatArray(w * h) { (px[it] ushr 24) / 255f }
        }

        /** 硬件位图读不了像素，先拷一份软件位图。 */
        private fun readable(bitmap: Bitmap): Bitmap? {
            if (bitmap.isRecycled) return null
            if (bitmap.config != Bitmap.Config.HARDWARE) return bitmap
            val copy = runCatching { bitmap.copy(Bitmap.Config.ARGB_8888, false) }.getOrNull()
            if (copy != null) return copy
            return runCatching {
                val soft = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                Canvas(soft).drawBitmap(bitmap, 0f, 0f, null)
                soft
            }.getOrNull()
        }

        /** 两遍可分离方框模糊，近似高斯。画布外按透明（0）处理。 */
        private fun blur(src: FloatArray, w: Int, h: Int, r: Int): FloatArray {
            val once = boxV(boxH(src, w, h, r), w, h, r)
            return boxV(boxH(once, w, h, r), w, h, r)
        }

        private fun boxH(src: FloatArray, w: Int, h: Int, r: Int): FloatArray {
            val out = FloatArray(src.size)
            val span = 2 * r + 1
            for (y in 0 until h) {
                val row = y * w
                var sum = 0f
                for (k in 0..min(r, w - 1)) sum += src[row + k]
                for (x in 0 until w) {
                    out[row + x] = sum / span
                    val add = x + r + 1
                    val drop = x - r
                    if (add < w) sum += src[row + add]
                    if (drop >= 0) sum -= src[row + drop]
                }
            }
            return out
        }

        private fun boxV(src: FloatArray, w: Int, h: Int, r: Int): FloatArray {
            val out = FloatArray(src.size)
            val span = 2 * r + 1
            for (x in 0 until w) {
                var sum = 0f
                for (k in 0..min(r, h - 1)) sum += src[k * w + x]
                for (y in 0 until h) {
                    out[y * w + x] = sum / span
                    val add = y + r + 1
                    val drop = y - r
                    if (add < h) sum += src[add * w + x]
                    if (drop >= 0) sum -= src[drop * w + x]
                }
            }
            return out
        }

        private fun smoothstep(e0: Float, e1: Float, x: Float): Float {
            val t = ((x - e0) / (e1 - e0)).coerceIn(0f, 1f)
            return t * t * (3f - 2f * t)
        }
    }
}
