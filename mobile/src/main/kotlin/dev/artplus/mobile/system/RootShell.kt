package dev.artplus.mobile

import android.content.ContentResolver
import android.provider.Settings
import java.util.concurrent.TimeUnit

/**
 * Root 通道（P3 拆分）：su 命令执行 + shell 引用转义 + 图标刷新/包列表。
 *
 * 从 MainActivity 迁移而来：原 `private fun`，现 `internal`，全部纯移动（零 Activity 状态）。
 * - `refreshArtPlusIconsBlocking` 显式收 ContentResolver + apkPath
 *  （原直读 `contentResolver`/`applicationInfo`），随带 3 个 Long 位运算 helper；
 *   `refreshArtPlusIcons` 的线程 + runOnUiThread 编排无法外迁，留 Activity 做瘦编排。
 * Activity 内无同名同参残留（纯移动零 wrapper），调用点零改动。
 */

/** shell 单引号转义。 */
internal fun shQuote(value: String): String = "'" + value.replace("'", "'\\''") + "'"

/** 经 `su -c` 执行命令并返回合并后的 stdout（超时/非零退出抛异常）。 */
internal fun runRootCommand(command: String, timeoutMs: Long, stdin: String? = null): String {
    val process = ProcessBuilder("su", "-c", command)
        .redirectErrorStream(true)
        .start()
    val outputBuilder = StringBuilder()
    val outputReader = Thread {
        process.inputStream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                outputBuilder
                    .append(line)
                    .append('\n')
            }
        }
    }.apply {
        isDaemon = true
        start()
    }
    runCatching {
        process.outputStream.bufferedWriter().use { writer ->
            if (stdin != null) {
                writer.write(stdin)
            }
        }
    }
    val finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)
    if (!finished) {
        process.destroyForcibly()
        outputReader.join(250)
        error("su 超时")
    }
    outputReader.join(1_000)
    val output = outputBuilder.toString()
    val code = process.exitValue()
    if (code != 0) {
        val detail = output.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() }
            ?.take(120)
        error(
            buildString {
                append("su 退出码: ")
                append(code)
                if (detail != null) {
                    append(": ")
                    append(detail)
                }
            },
        )
    }
    return output
}

/**
 * 触发 ColorOS 图标主题重载（临时主题→目标主题各一次），返回带 mUxIconConfig= 的输出摘要。
 */
internal fun refreshArtPlusIconsBlocking(contentResolver: ContentResolver, apkPath: String): String {
    val currentConfig = Settings.System
        .getString(contentResolver, COLOROS_UX_ICON_CONFIG_KEY)
        ?.trim()
        ?.toLongOrNull()
        ?: FALLBACK_ARTPLUS_INSPIRATION_UXICON_CONFIG
    val finalTheme = currentConfig.uxIconTheme()
    val temporaryTheme = when (finalTheme) {
        COLOROS_DEFAULT_ICON_THEME -> COLOROS_INSPIRATION_ICON_THEME
        COLOROS_INSPIRATION_ICON_THEME -> COLOROS_DEFAULT_ICON_THEME
        else -> COLOROS_DEFAULT_ICON_THEME
    }
    val finalConfig = currentConfig
        .withUxIconArtPlusOn(COLOROS_ARTPLUS_ON)
    val temporaryConfig = finalConfig.withUxIconTheme(temporaryTheme)
    val command = """
        set -e
        APP_APK=${shQuote(apkPath)}
        apply_uxicon_config() {
            value="${'$'}1"
            app_process -Djava.class.path="${'$'}APP_APK" /system/bin ${UxIconConfigCli::class.java.name} "${'$'}value"
            settings put system ${shQuote(COLOROS_UX_ICON_CONFIG_KEY)} "${'$'}value"
            am broadcast -a oplus.intent.action.SKIN_CHANGED >/dev/null 2>&1 || true
        }
        apply_uxicon_config ${temporaryConfig}
        sleep 1
        apply_uxicon_config ${finalConfig}
        am start -a android.intent.action.MAIN -c android.intent.category.HOME >/dev/null 2>&1 ||
            input keyevent 3 >/dev/null 2>&1 || true
    """.trimIndent()
    return runRootCommand(command, ARTPLUS_ICON_REFRESH_TIMEOUT_MS)
        .lineSequence()
        .map { it.trim() }
        .filter { it.startsWith("mUxIconConfig=") }
        .joinToString(" -> ")
}

/** 经 root 列出 uxicons 目录下的包名（失败返回空列表）。 */
internal fun listRootIconPackages(): List<String> {
    val cmd = "ls -1 ${shQuote(ROOT_UXICONS_DIR)} 2>/dev/null || echo ''"
    val output = try {
        runRootCommand(cmd, timeoutMs = 3000)
    } catch (_: Exception) {
        return emptyList()
    }
    return output.lines().map { it.trim() }.filter { it.isNotEmpty() }
}

internal fun Long.withUxIconTheme(theme: Int): Long =
    (this and COLOROS_UXICON_THEME_MASK.inv()) or
        (((theme.toLong()) and 0x0fL) shl COLOROS_UXICON_THEME_SHIFT)

internal fun Long.uxIconTheme(): Int =
    ((this and COLOROS_UXICON_THEME_MASK) shr COLOROS_UXICON_THEME_SHIFT).toInt()

internal fun Long.withUxIconArtPlusOn(value: Int): Long =
    (this and COLOROS_UXICON_ARTPLUS_MASK.inv()) or
        (((value.toLong()) and 0x07L) shl COLOROS_UXICON_ARTPLUS_SHIFT)
