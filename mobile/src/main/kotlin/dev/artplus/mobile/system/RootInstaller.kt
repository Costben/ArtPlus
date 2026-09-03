package dev.artplus.mobile

import java.io.File

/**
 * Root 安装（P4 拆分）：图标包 root 写入 + 快速备份。
 *
 * 从 MainActivity 迁移而来：原 `private fun`，现 `internal`，全部纯移动
 * （`installWithRoot`/`backupPackageFast` 零 Activity 状态，直调 RootShell；
 * `RootWriteMode` 整枚举提升，原 private enum，自包含，UI 引用同包零改动）。
 * Activity 内无残留（纯移动零 wrapper），调用点零改动。
 */

internal enum class RootWriteMode(val value: String, val label: String) {
    All("all", "全部"),
    StandardOnly("standard", "标准"),
    MonochromeOnly("monochrome", "单色");

    companion object {
        fun fromValue(value: String?): RootWriteMode =
            entries.firstOrNull { it.value == value || (it == StandardOnly && value == "default") } ?: All
    }
}

internal fun installWithRoot(packageDir: File, packageName: String, mode: RootWriteMode) {
    val target = "$ROOT_UXICONS_DIR/$packageName"
    val source = packageDir.absolutePath
    val copyCommand = when (mode) {
        RootWriteMode.All -> """
            find ${shQuote(source)} -maxdepth 1 -type f -name '*.png' -exec cp -f {} ${shQuote(target)}/ \;
        """.trimIndent()
        RootWriteMode.StandardOnly -> """
            find ${shQuote(target)} -maxdepth 1 -type f -name 'monochrome*.png' -delete
            find ${shQuote(source)} -maxdepth 1 -type f -name '*.png' ! -name 'monochrome*.png' -exec cp -f {} ${shQuote(target)}/ \;
        """.trimIndent()
        RootWriteMode.MonochromeOnly -> """
            find ${shQuote(source)} -maxdepth 1 -type f -name 'monochrome*.png' -exec cp -f {} ${shQuote(target)}/ \;
        """.trimIndent()
    }
    val command = """
        set -e
        mkdir -p ${shQuote(target)}
        $copyCommand
        find ${shQuote(target)} -maxdepth 1 -type f -name '*.png' -exec chmod 0644 {} +
        restorecon -RF ${shQuote(target)} 2>/dev/null || true
    """.trimIndent()
    val process = ProcessBuilder("su", "-c", command)
        .redirectErrorStream(true)
        .start()
    val code = process.waitFor()
    if (code != 0) {
        error("su 退出码: $code")
    }
}

internal fun backupPackageFast(pkgName: String, destRoot: String): Boolean {
    val src = "$ROOT_UXICONS_DIR/$pkgName"
    val destPkg = "$destRoot/$pkgName"
    val cmd = "mkdir -p ${shQuote(destPkg)} && cp -f ${shQuote(src)}/*.png ${shQuote(destPkg)}/ 2>/dev/null && chmod 0644 ${shQuote(destPkg)}/*.png 2>/dev/null && echo ok"
    return try {
        val out = runRootCommand(cmd, 6000)
        out.contains("ok")
    } catch (_: Exception) { false }
}
