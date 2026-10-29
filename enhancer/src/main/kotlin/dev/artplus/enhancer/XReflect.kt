package dev.artplus.enhancer

import io.github.libxposed.api.XposedInterface.Chain
import io.github.libxposed.api.XposedModule
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * 反射工具。
 *
 * 插件不链接桌面的任何类（只 compileOnly LibXposed），所有目标类、字段、方法都在运行时按名字找；
 * 桌面小版本升级改了混淆名时，找不到的 hook 安静跳过，不会连带把其它 hook 拖垮。
 */
object XReflect {

    fun findClass(className: String, classLoader: ClassLoader?): Class<*>? = runCatching {
        Class.forName(className, false, classLoader ?: ClassLoader.getSystemClassLoader())
    }.getOrNull()

    fun findField(clazz: Class<*>, fieldName: String): Field? {
        var current: Class<*>? = clazz
        while (current != null && current != Any::class.java) {
            runCatching {
                val f = current.getDeclaredField(fieldName)
                f.isAccessible = true
                return f
            }
            current = current.superclass
        }
        return null
    }

    fun getObjectField(obj: Any?, fieldName: String): Any? {
        if (obj == null) return null
        return runCatching { findField(obj.javaClass, fieldName)?.get(obj) }.getOrNull()
    }

    fun getStaticObjectField(clazz: Class<*>, fieldName: String): Any? =
        runCatching { findField(clazz, fieldName)?.get(null) }.getOrNull()

    fun findMethod(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): Method? {
        var current: Class<*>? = clazz
        while (current != null && current != Any::class.java) {
            runCatching {
                val m = current.getDeclaredMethod(methodName, *parameterTypes)
                m.isAccessible = true
                return m
            }
            current = current.superclass
        }
        return null
    }

    fun callMethod(obj: Any?, methodName: String, vararg args: Any?): Any? {
        if (obj == null) return null
        var current: Class<*>? = obj.javaClass
        while (current != null && current != Any::class.java) {
            for (m in current.declaredMethods) {
                if (m.name == methodName && isMatchParams(m.parameterTypes, args)) {
                    m.isAccessible = true
                    return runCatching { m.invoke(obj, *args) }.getOrNull()
                }
            }
            current = current.superclass
        }
        return null
    }

    fun callStaticMethod(clazz: Class<*>, methodName: String, vararg args: Any?): Any? {
        var current: Class<*>? = clazz
        while (current != null && current != Any::class.java) {
            for (m in current.declaredMethods) {
                if (Modifier.isStatic(m.modifiers) && m.name == methodName && isMatchParams(m.parameterTypes, args)) {
                    m.isAccessible = true
                    return runCatching { m.invoke(null, *args) }.getOrNull()
                }
            }
            current = current.superclass
        }
        return null
    }

    private fun isMatchParams(paramTypes: Array<Class<*>>, args: Array<out Any?>): Boolean {
        if (paramTypes.size != args.size) return false
        for (i in paramTypes.indices) {
            val arg = args[i] ?: continue
            val expected = paramTypes[i]
            if (expected.isPrimitive) {
                if (!isBoxedPrimitiveMatch(expected, arg.javaClass)) return false
            } else if (!expected.isAssignableFrom(arg.javaClass)) {
                return false
            }
        }
        return true
    }

    private fun isBoxedPrimitiveMatch(primitive: Class<*>, boxed: Class<*>): Boolean =
        (primitive == Int::class.javaPrimitiveType && boxed == Int::class.javaObjectType) ||
            (primitive == Boolean::class.javaPrimitiveType && boxed == Boolean::class.javaObjectType) ||
            (primitive == Long::class.javaPrimitiveType && boxed == Long::class.javaObjectType) ||
            (primitive == Float::class.javaPrimitiveType && boxed == Float::class.javaObjectType) ||
            (primitive == Double::class.javaPrimitiveType && boxed == Double::class.javaObjectType) ||
            (primitive == Byte::class.javaPrimitiveType && boxed == Byte::class.javaObjectType) ||
            (primitive == Short::class.javaPrimitiveType && boxed == Short::class.javaObjectType) ||
            (primitive == Char::class.javaPrimitiveType && boxed == Char::class.javaObjectType)

    // ==========================================================
    // 反射调用在 XReflect 里，hook 便捷封装见文件末尾的顶层扩展函数
    // ==========================================================
}

/**
 * 按方法名 + 参数类型挂 hook：成功 true，类/方法不存在 false。
 *
 * 放在顶层而不是 [XReflect] 里：成员扩展函数需要 dispatch 接收者，
 * 别的文件 `import XReflect.hookTarget` 是拿不到 XReflect 这个接收者的。
 */
fun XposedModule.hookTarget(
    clazz: Class<*>,
    methodName: String,
    vararg paramTypes: Class<*>,
    consumer: (Chain) -> Any?,
): Boolean {
    val method = XReflect.findMethod(clazz, methodName, *paramTypes) ?: return false
    return runCatching {
        hook(method).intercept { chain -> consumer(chain) }
        true
    }.getOrDefault(false)
}

/** 按方法名挂上类里所有同名重载，用于签名里有混淆类（如 `yl.a`）没法写进参数列表的情况。 */
fun XposedModule.hookTargetsNamed(
    clazz: Class<*>,
    methodName: String,
    consumer: (Chain) -> Any?,
): Int {
    var count = 0
    for (method in clazz.declaredMethods) {
        if (method.name != methodName) continue
        val ok = runCatching {
            hook(method).intercept { chain -> consumer(chain) }
            true
        }.getOrDefault(false)
        if (ok) count++
    }
    return count
}
