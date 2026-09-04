package dev.artplus.mobile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
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

/**
 * Slice 1.4 纯移动（零 Activity 状态，同包直接用，Activity 内删本体零 wrapper）：
 * hasGeneratedPackageBaseAssets / decodeGeneratedBitmap /
 * installLiquidGlassFilesWithRoot / buildGeneratedPackageSession。
 * su 调用顺序、mount/写路径与权限模式未变，不新增 root 行为。
 */

internal fun hasGeneratedPackageBaseAssets(dir: File): Boolean =
    dir.isDirectory &&
        File(dir, "recbg.png").isFile &&
        File(dir, "recfg.png").isFile

internal fun decodeGeneratedBitmap(dir: File, name: String): Bitmap? =
    BitmapFactory.decodeFile(File(dir, name).absolutePath)

internal fun installLiquidGlassFilesWithRoot(packageDir: File, packageName: String) {
    val target = "$ROOT_UXICONS_DIR/$packageName"
    val source = packageDir.absolutePath
    val names = listOf(
        "recbg.png",
        "recbg_1x2.png",
        "recbg_2x1.png",
        "recbg_2x2.png",
        "recfg.png",
        "recfg_1x2.png",
        "recfg_2x1.png",
        "recfg_2x2.png",
        "rec_night.png",
        "rec_night_1x2.png",
        "rec_night_2x1.png",
        "rec_night_2x2.png",
        "monochrome_light.png",
        "monochrome_dark.png",
        "monochrome.png",
        "monochrome_1x2.png",
        "monochrome_2x1.png",
        "monochrome_2x2.png",
    )
    val copyCommands = names.joinToString(separator = "\n") { name ->
        """
        if [ -f ${shQuote("$source/$name")} ]; then
            cp -f ${shQuote("$source/$name")} ${shQuote("$target/$name")}
            chmod 0644 ${shQuote("$target/$name")}
        fi
        """.trimIndent()
    }
    val command = """
        set -e
        mkdir -p ${shQuote(target)}
        $copyCommands
        restorecon -RF ${shQuote(target)} 2>/dev/null || true
    """.trimIndent()
    runRootCommand(command, ROOT_SCAN_TIMEOUT_MS)
}

internal fun buildGeneratedPackageSession(packageName: String, packageDir: File): GenerationSession {
    val recfg = decodeGeneratedBitmap(packageDir, FOREGROUND_ORIGINAL_BACKUP_NAME)
        ?: decodeGeneratedBitmap(packageDir, "recfg.png")
        ?: error("现有图标包缺少 recfg.png")
    val recbg = decodeGeneratedBitmap(packageDir, "recbg.png")
        ?: error("现有图标包缺少 recbg.png")
    val normalizedRecfg = resizeBitmap(recfg, SIZE_1X1, SIZE_1X1)
    val normalizedRecbg = resizeBitmap(recbg, SIZE_1X1, SIZE_1X1)
    val monochrome = simpleMonochromeAlphaFromDefaultSubject(normalizedRecfg, invertLuma = false)
    val original = IconCandidate(
        recfgRaw = normalizedRecfg,
        recbg = normalizedRecbg,
        monochromeRaw = null,
        monochromeFromDefaultSubject = true,
        preserveGeometry = true,
    )
    return GenerationSession(
        packageName = packageName,
        outDir = packageDir,
        sourceIcon = centerOnCanvas(normalizedRecfg, GPT_SOURCE_SIZE, GPT_SOURCE_SIZE),
        baseRecfg = normalizedRecfg,
        baseRecbg = normalizedRecbg,
        monochromeRaw = monochrome,
        candidates = mapOf(PreviewChoice.Original to original),
        autoLocalChoice = PreviewChoice.Original,
        canRebuildLocalCandidates = false,
    )
}

/**
 * Slice 1.4 显式参数移动（读 Activity 状态/Context，同包直接用；
 * Activity 内留原签名 wrapper 标注“重构期间保留”，调用点零改动）：
 * artPlusPackageDir / rootGeneratedPreviewDir /
 * copyRootGeneratedPackageToLocal / existingGeneratedPackageDir。
 * su 调用顺序、写路径与权限模式未变，不新增 root 行为。
 */

internal fun artPlusPackageDir(packageName: String, externalArtPlusDir: File?, filesDir: File): File {
    val base = externalArtPlusDir ?: File(filesDir, "ArtPlus")
    return File(base, packageName)
}

internal fun rootGeneratedPreviewDir(packageName: String, filesDir: File): File =
    File(File(filesDir, "RootGeneratedPreview"), packageName)

internal fun copyRootGeneratedPackageToLocal(packageName: String, filesDir: File, appUid: Int): File {
    val targetDir = rootGeneratedPreviewDir(packageName, filesDir)
    ensureFreshDir(targetDir)
    val sourceDir = "$ROOT_UXICONS_DIR/$packageName"
    val command = """
        set -e
        src=${shQuote(sourceDir)}
        dst=${shQuote(targetDir.absolutePath)}
        [ -d "${'$'}src" ] || { echo "data 中没有图标包"; exit 2; }
        copied=0
        find "${'$'}src" -maxdepth 1 -type f -name '*.png' | while IFS= read -r file; do
            cp -f "${'$'}file" "${'$'}dst"/
            copied=1
        done
        if ! ls "${'$'}dst"/*.png >/dev/null 2>&1; then
            echo "data 图标包没有 PNG"
            exit 3
        fi
        chown -R $appUid:$appUid "${'$'}dst" 2>/dev/null || true
        chmod 0644 "${'$'}dst"/*.png 2>/dev/null || true
    """.trimIndent()
    runRootCommand(command, ROOT_SCAN_TIMEOUT_MS)
    if (!hasGeneratedPackageBaseAssets(targetDir)) {
        error("现有图标包缺少 recbg.png 或 recfg.png")
    }
    return targetDir
}

internal fun existingGeneratedPackageDir(
    packageName: String,
    previewDirPath: String?,
    previewPackageName: String?,
    externalArtPlusDir: File?,
    filesDir: File,
    appUid: Int,
): File {
    val currentPreviewDir = previewDirPath
        ?.takeIf { previewPackageName == packageName }
        ?.let(::File)
        ?.takeIf { hasGeneratedPackageBaseAssets(it) && it != artPlusPackageDir(packageName, externalArtPlusDir, filesDir) }
    if (currentPreviewDir != null) {
        return currentPreviewDir
    }
    runCatching { copyRootGeneratedPackageToLocal(packageName, filesDir, appUid) }
        .onSuccess { return it }
    val localDir = artPlusPackageDir(packageName, externalArtPlusDir, filesDir)
    if (hasGeneratedPackageBaseAssets(localDir)) {
        return localDir
    }
    return copyRootGeneratedPackageToLocal(packageName, filesDir, appUid)
}

/**
 * Slice 1.4 imaging 依赖移动（原调 Activity wrapper 的纯算法改调顶层显式版本，
 * 同包直接用；Activity 内留原签名 wrapper 标注“重构期间保留”，调用点零改动）：
 * writeDefaultSubjectMonochromeFiles / glassBackgroundForGeneratedPackage /
 * applyLiquidGlassToGeneratedPackage。
 */

internal fun writeDefaultSubjectMonochromeFiles(
    dir: File,
    baseRecfg: Bitmap,
    overwriteExisting: Boolean,
    monochromeThemeScale: Float,
) {
    val subject = if (baseRecfg.width == SIZE_1X1 && baseRecfg.height == SIZE_1X1) {
        baseRecfg
    } else {
        resizeBitmap(baseRecfg, SIZE_1X1, SIZE_1X1)
    }
    val rawLight = simpleMonochromeAlphaFromDefaultSubject(subject, invertLuma = true)
    val rawDark = simpleMonochromeAlphaFromDefaultSubject(subject, invertLuma = false)
    val outputs = listOf(
        "monochrome_light.png" to scaleMonochromeForTheme(rawLight, monochromeThemeScale),
        "monochrome_dark.png" to scaleMonochromeForTheme(rawDark, monochromeThemeScale),
        "monochrome.png" to scaleMonochromeForTheme(rawDark, monochromeThemeScale),
        "monochrome_1x2.png" to centerOnCanvas(rawDark, SIZE_1X2[0], SIZE_1X2[1]),
        "monochrome_2x1.png" to centerOnCanvas(rawDark, SIZE_2X1[0], SIZE_2X1[1]),
        "monochrome_2x2.png" to centerOnCanvas(rawDark, SIZE_2X2, SIZE_2X2),
    )
    outputs.forEach { (name, bitmap) ->
        val target = File(dir, name)
        if (overwriteExisting || !target.isFile) {
            savePng(bitmap, target)
        }
    }
}

internal fun glassBackgroundForGeneratedPackage(
    dir: File,
    name: String,
    fallback: Bitmap,
    width: Int,
    height: Int,
    liquidGlassEnabled: Boolean,
    liquidGlassBackgroundMistAlpha: Int,
    liquidGlassTopAlpha: Int,
    liquidGlassBottomAlpha: Int,
    liquidGlassBottomDarkAlpha: Int,
    liquidGlassOuterWidth: Int,
    liquidGlassRadius: Int,
): Bitmap {
    val source = decodeGeneratedBitmap(dir, name) ?: fallback
    val resized = if (source.width == width && source.height == height) {
        source
    } else {
        resizeBitmap(source, width, height)
    }
    return liquidGlassBackgroundForSize(
        source = resized,
        width = width,
        height = height,
        forceLiquidGlass = true,
        liquidGlassEnabled = liquidGlassEnabled,
        liquidGlassBackgroundMistAlpha = liquidGlassBackgroundMistAlpha,
        liquidGlassTopAlpha = liquidGlassTopAlpha,
        liquidGlassBottomAlpha = liquidGlassBottomAlpha,
        liquidGlassBottomDarkAlpha = liquidGlassBottomDarkAlpha,
        liquidGlassOuterWidth = liquidGlassOuterWidth,
        liquidGlassRadius = liquidGlassRadius,
    )
}

internal fun applyLiquidGlassToGeneratedPackage(
    dir: File,
    nightSubjectLightBackgroundEnabled: Boolean,
    liquidGlassEnabled: Boolean,
    liquidGlassBackgroundMistAlpha: Int,
    liquidGlassTopAlpha: Int,
    liquidGlassBottomAlpha: Int,
    liquidGlassBottomDarkAlpha: Int,
    liquidGlassOuterWidth: Int,
    liquidGlassRadius: Int,
    liquidGlassSubjectScalePercent: Int,
    liquidGlassSubjectShadowAlpha: Int,
    liquidGlassSubjectOutlineWidth: Int,
    liquidGlassSubjectInnerOutlineWidth: Int,
    liquidGlassSubjectOpacityPercent: Int,
    foregroundShadowLevel: Int,
    monochromeThemeScale: Float,
) {
    val baseRecbg = decodeGeneratedBitmap(dir, "recbg.png")
        ?: error("现有图标包缺少 recbg.png")
    val originalRecfgFile = File(dir, FOREGROUND_ORIGINAL_BACKUP_NAME)
    val baseRecfg = decodeGeneratedBitmap(dir, FOREGROUND_ORIGINAL_BACKUP_NAME)
        ?: decodeGeneratedBitmap(dir, "recfg.png")
        ?: error("现有图标包缺少 recfg.png")
    if (!originalRecfgFile.isFile) {
        savePng(baseRecfg, originalRecfgFile)
    }

    val recbg = glassBackgroundForGeneratedPackage(dir, "recbg.png", baseRecbg, SIZE_1X1, SIZE_1X1, liquidGlassEnabled, liquidGlassBackgroundMistAlpha, liquidGlassTopAlpha, liquidGlassBottomAlpha, liquidGlassBottomDarkAlpha, liquidGlassOuterWidth, liquidGlassRadius)
    val recbg1x2 = glassBackgroundForGeneratedPackage(dir, "recbg_1x2.png", baseRecbg, SIZE_1X2[0], SIZE_1X2[1], liquidGlassEnabled, liquidGlassBackgroundMistAlpha, liquidGlassTopAlpha, liquidGlassBottomAlpha, liquidGlassBottomDarkAlpha, liquidGlassOuterWidth, liquidGlassRadius)
    val recbg2x1 = glassBackgroundForGeneratedPackage(dir, "recbg_2x1.png", baseRecbg, SIZE_2X1[0], SIZE_2X1[1], liquidGlassEnabled, liquidGlassBackgroundMistAlpha, liquidGlassTopAlpha, liquidGlassBottomAlpha, liquidGlassBottomDarkAlpha, liquidGlassOuterWidth, liquidGlassRadius)
    val recbg2x2 = glassBackgroundForGeneratedPackage(dir, "recbg_2x2.png", baseRecbg, SIZE_2X2, SIZE_2X2, liquidGlassEnabled, liquidGlassBackgroundMistAlpha, liquidGlassTopAlpha, liquidGlassBottomAlpha, liquidGlassBottomDarkAlpha, liquidGlassOuterWidth, liquidGlassRadius)

    savePng(recbg, File(dir, "recbg.png"))
    savePng(recbg1x2, File(dir, "recbg_1x2.png"))
    savePng(recbg2x1, File(dir, "recbg_2x1.png"))
    savePng(recbg2x2, File(dir, "recbg_2x2.png"))

    fun foregroundForGenerated(source: Bitmap, width: Int, height: Int): Bitmap =
        foregroundForSize(
            source = source,
            width = width,
            height = height,
            forceLiquidGlass = true,
            liquidGlassEnabled = liquidGlassEnabled,
            liquidGlassSubjectScalePercent = liquidGlassSubjectScalePercent,
            liquidGlassSubjectShadowAlpha = liquidGlassSubjectShadowAlpha,
            liquidGlassSubjectOutlineWidth = liquidGlassSubjectOutlineWidth,
            liquidGlassSubjectInnerOutlineWidth = liquidGlassSubjectInnerOutlineWidth,
            liquidGlassSubjectOpacityPercent = liquidGlassSubjectOpacityPercent,
            liquidGlassTopAlpha = liquidGlassTopAlpha,
            liquidGlassBottomAlpha = liquidGlassBottomAlpha,
            liquidGlassBottomDarkAlpha = liquidGlassBottomDarkAlpha,
            liquidGlassOuterWidth = liquidGlassOuterWidth,
            liquidGlassRadius = liquidGlassRadius,
            applyShadow = { bitmap -> applyForegroundShadow(bitmap, foregroundShadowLevel) },
            renderSubjectShadow = ::subjectShadowBitmap,
        )

    val outputRecfg = foregroundForGenerated(baseRecfg, SIZE_1X1, SIZE_1X1)
    val recfg1x2Source = decodeGeneratedBitmap(dir, "recfg_1x2.png")
        ?: centerOnCanvas(baseRecfg, SIZE_1X2[0], SIZE_1X2[1])
    val recfg2x1Source = decodeGeneratedBitmap(dir, "recfg_2x1.png")
        ?: centerOnCanvas(baseRecfg, SIZE_2X1[0], SIZE_2X1[1])
    val recfg2x2Source = decodeGeneratedBitmap(dir, "recfg_2x2.png")
        ?: centerOnCanvas(baseRecfg, SIZE_2X2, SIZE_2X2)
    val recfg1x2 = foregroundForGenerated(recfg1x2Source, SIZE_1X2[0], SIZE_1X2[1])
    val recfg2x1 = foregroundForGenerated(recfg2x1Source, SIZE_2X1[0], SIZE_2X1[1])
    val recfg2x2 = foregroundForGenerated(recfg2x2Source, SIZE_2X2, SIZE_2X2)

    writeDefaultSubjectMonochromeFiles(dir, baseRecfg, overwriteExisting = false, monochromeThemeScale = monochromeThemeScale)

    savePng(outputRecfg, File(dir, "recfg.png"))
    savePng(recfg1x2, File(dir, "recfg_1x2.png"))
    savePng(recfg2x1, File(dir, "recfg_2x1.png"))
    savePng(recfg2x2, File(dir, "recfg_2x2.png"))

    savePng(normalDarkForeground(outputRecfg, recbg, nightSubjectLightBackgroundEnabled), File(dir, "rec_night.png"))
    savePng(normalDarkForeground(recfg1x2, recbg1x2, nightSubjectLightBackgroundEnabled), File(dir, "rec_night_1x2.png"))
    savePng(normalDarkForeground(recfg2x1, recbg2x1, nightSubjectLightBackgroundEnabled), File(dir, "rec_night_2x1.png"))
    savePng(normalDarkForeground(recfg2x2, recbg2x2, nightSubjectLightBackgroundEnabled), File(dir, "rec_night_2x2.png"))
}

/**
 * Slice 1.4 重型移动（读 Activity 调参/Context/凭证/状态回调，改为显式收参；
 * 纯算法改调顶层显式版本，同包直接用；Activity 内留原签名 wrapper
 * 标注“重构期间保留”，调用点零改动）：
 * generateArtPlusPackage / writePackageOutputs。
 * su 调用顺序、mount/写路径与权限模式未变，不新增 root 行为
 * （本组无 su，仅本地生成与落盘）。
 */

internal fun writePackageOutputs(
    session: GenerationSession,
    selections: PreviewSelections,
    edgePolishPercent: Int,
    foregroundSubjectPercent: Int,
    rmbgTunedForeground: (IconCandidate) -> Bitmap?,
    liquidGlassEnabled: Boolean,
    liquidGlassBackgroundMistAlpha: Int,
    liquidGlassTopAlpha: Int,
    liquidGlassBottomAlpha: Int,
    liquidGlassBottomDarkAlpha: Int,
    liquidGlassOuterWidth: Int,
    liquidGlassRadius: Int,
    liquidGlassSubjectScalePercent: Int,
    liquidGlassSubjectShadowAlpha: Int,
    liquidGlassSubjectOutlineWidth: Int,
    liquidGlassSubjectInnerOutlineWidth: Int,
    liquidGlassSubjectOpacityPercent: Int,
    foregroundShadowLevel: Int,
    monochromeThemeScale: Float,
    nightSubjectLightBackgroundEnabled: Boolean,
) {
    fun foregroundForOutputs(source: Bitmap, width: Int, height: Int): Bitmap =
        foregroundForSize(
            source = source,
            width = width,
            height = height,
            liquidGlassEnabled = liquidGlassEnabled,
            liquidGlassSubjectScalePercent = liquidGlassSubjectScalePercent,
            liquidGlassSubjectShadowAlpha = liquidGlassSubjectShadowAlpha,
            liquidGlassSubjectOutlineWidth = liquidGlassSubjectOutlineWidth,
            liquidGlassSubjectInnerOutlineWidth = liquidGlassSubjectInnerOutlineWidth,
            liquidGlassSubjectOpacityPercent = liquidGlassSubjectOpacityPercent,
            liquidGlassTopAlpha = liquidGlassTopAlpha,
            liquidGlassBottomAlpha = liquidGlassBottomAlpha,
            liquidGlassBottomDarkAlpha = liquidGlassBottomDarkAlpha,
            liquidGlassOuterWidth = liquidGlassOuterWidth,
            liquidGlassRadius = liquidGlassRadius,
            applyShadow = { bitmap -> applyForegroundShadow(bitmap, foregroundShadowLevel) },
            renderSubjectShadow = ::subjectShadowBitmap,
        )

    fun backgroundForOutputs(source: Bitmap, width: Int, height: Int): Bitmap =
        liquidGlassBackgroundForSize(
            source = source,
            width = width,
            height = height,
            liquidGlassEnabled = liquidGlassEnabled,
            liquidGlassBackgroundMistAlpha = liquidGlassBackgroundMistAlpha,
            liquidGlassTopAlpha = liquidGlassTopAlpha,
            liquidGlassBottomAlpha = liquidGlassBottomAlpha,
            liquidGlassBottomDarkAlpha = liquidGlassBottomDarkAlpha,
            liquidGlassOuterWidth = liquidGlassOuterWidth,
            liquidGlassRadius = liquidGlassRadius,
        )

    val light = candidateWithCustomOverrides(session, PreviewMode.NormalLight, selections.normalLight)
    val lightBaseRecfg = renderCandidateForegroundBase(light, edgePolishPercent, foregroundSubjectPercent, rmbgTunedForeground)
    val lightRecfg = foregroundForOutputs(lightBaseRecfg, SIZE_1X1, SIZE_1X1)
    val lightBaseRecbg = light.recbg
    val lightRecbg = backgroundForOutputs(lightBaseRecbg, SIZE_1X1, SIZE_1X1)
    savePng(lightRecbg, File(session.outDir, "recbg.png"))
    savePng(lightRecfg, File(session.outDir, "recfg.png"))
    val recbg1x2 = backgroundForOutputs(lightBaseRecbg, SIZE_1X2[0], SIZE_1X2[1])
    val recbg2x1 = backgroundForOutputs(lightBaseRecbg, SIZE_2X1[0], SIZE_2X1[1])
    val recbg2x2 = backgroundForOutputs(lightBaseRecbg, SIZE_2X2, SIZE_2X2)
    savePng(recbg1x2, File(session.outDir, "recbg_1x2.png"))
    savePng(recbg2x1, File(session.outDir, "recbg_2x1.png"))
    savePng(recbg2x2, File(session.outDir, "recbg_2x2.png"))

    val recfg1x2 = foregroundForOutputs(lightBaseRecfg, SIZE_1X2[0], SIZE_1X2[1])
    val recfg2x1 = foregroundForOutputs(lightBaseRecfg, SIZE_2X1[0], SIZE_2X1[1])
    val recfg2x2 = foregroundForOutputs(lightBaseRecfg, SIZE_2X2, SIZE_2X2)
    savePng(recfg1x2, File(session.outDir, "recfg_1x2.png"))
    savePng(recfg2x1, File(session.outDir, "recfg_2x1.png"))
    savePng(recfg2x2, File(session.outDir, "recfg_2x2.png"))

    val night = candidateWithCustomOverrides(session, PreviewMode.NormalDark, selections.normalDark)
    val nightBaseRecfg = renderCandidateForegroundBase(night, edgePolishPercent, foregroundSubjectPercent, rmbgTunedForeground)
    val nightRecfg = foregroundForOutputs(nightBaseRecfg, SIZE_1X1, SIZE_1X1)
    val nightBaseRecbg = night.recbg
    val nightRecbg = backgroundForOutputs(nightBaseRecbg, SIZE_1X1, SIZE_1X1)
    val nightRecfg1x2 = foregroundForOutputs(nightBaseRecfg, SIZE_1X2[0], SIZE_1X2[1])
    val nightRecfg2x1 = foregroundForOutputs(nightBaseRecfg, SIZE_2X1[0], SIZE_2X1[1])
    val nightRecfg2x2 = foregroundForOutputs(nightBaseRecfg, SIZE_2X2, SIZE_2X2)
    val nightRecbg1x2 = backgroundForOutputs(nightBaseRecbg, SIZE_1X2[0], SIZE_1X2[1])
    val nightRecbg2x1 = backgroundForOutputs(nightBaseRecbg, SIZE_2X1[0], SIZE_2X1[1])
    val nightRecbg2x2 = backgroundForOutputs(nightBaseRecbg, SIZE_2X2, SIZE_2X2)
    savePng(normalDarkForeground(nightRecfg, nightRecbg, nightSubjectLightBackgroundEnabled), File(session.outDir, "rec_night.png"))
    savePng(
        normalDarkForeground(nightRecfg1x2, nightRecbg1x2, nightSubjectLightBackgroundEnabled),
        File(session.outDir, "rec_night_1x2.png"),
    )
    savePng(
        normalDarkForeground(nightRecfg2x1, nightRecbg2x1, nightSubjectLightBackgroundEnabled),
        File(session.outDir, "rec_night_2x1.png"),
    )
    savePng(
        normalDarkForeground(nightRecfg2x2, nightRecbg2x2, nightSubjectLightBackgroundEnabled),
        File(session.outDir, "rec_night_2x2.png"),
    )

    val rawMonochromeLight = monochromeForCandidate(
        candidate = candidateWithCustomOverrides(session, PreviewMode.MonochromeLight, selections.monochromeLight),
        invertLuma = true,
        edgePolishPercent = edgePolishPercent,
        foregroundSubjectPercent = foregroundSubjectPercent,
        rmbgTunedForeground = rmbgTunedForeground,
    )
    val rawMonochromeDark = monochromeForCandidate(
        candidate = candidateWithCustomOverrides(session, PreviewMode.MonochromeDark, selections.monochromeDark),
        invertLuma = false,
        edgePolishPercent = edgePolishPercent,
        foregroundSubjectPercent = foregroundSubjectPercent,
        rmbgTunedForeground = rmbgTunedForeground,
    )
    val monochromeLight = scaleMonochromeForTheme(rawMonochromeLight, monochromeThemeScale)
    val monochromeDark = scaleMonochromeForTheme(rawMonochromeDark, monochromeThemeScale)
    savePng(monochromeLight, File(session.outDir, "monochrome_light.png"))
    savePng(monochromeDark, File(session.outDir, "monochrome_dark.png"))
    savePng(monochromeDark, File(session.outDir, "monochrome.png"))
    savePng(centerOnCanvas(rawMonochromeDark, SIZE_1X2[0], SIZE_1X2[1]), File(session.outDir, "monochrome_1x2.png"))
    savePng(centerOnCanvas(rawMonochromeDark, SIZE_2X1[0], SIZE_2X1[1]), File(session.outDir, "monochrome_2x1.png"))
    savePng(centerOnCanvas(rawMonochromeDark, SIZE_2X2, SIZE_2X2), File(session.outDir, "monochrome_2x2.png"))

    savePng(adjustColor(lightRecfg, 1.3f, 1.0f), File(session.outDir, "day.png"))
    savePng(adjustColor(lightRecfg, 0.9f, 0.9f), File(session.outDir, "nsd.png"))
    savePng(adjustColor(lightRecfg, 0.9f, 1.05f), File(session.outDir, "mat.png"))
    savePng(adjustColor(lightRecfg, 0.7f, 0.95f), File(session.outDir, "peb.png"))
}

internal fun generateArtPlusPackage(
    app: AppEntry,
    useGpt: Boolean,
    localModeOverride: LocalSeparationMode?,
    params: TuningParams,
    externalArtPlusDir: File?,
    filesDir: File,
    icon: Drawable,
    gptModelId: String,
    gptBaseUrl: String,
    gptApiKey: String,
    isDebug: Boolean,
    onStatus: (String) -> Unit,
    defaultChoiceForMode: (LocalSeparationMode, PreviewChoice) -> PreviewChoice,
    rmbgTunedForeground: (IconCandidate) -> Bitmap?,
): GenerationResult {
    val base = externalArtPlusDir ?: File(filesDir, "ArtPlus")
    val outDir = File(base, app.packageName)
    ensureCleanDir(outDir)

    val localSourceIcon = drawLocalCandidateSourceIcon(icon, SIZE_1X1, SIZE_1X1)
    val gptSourceIcon = drawDrawable(icon, GPT_SOURCE_SIZE, GPT_SOURCE_SIZE, transparent = false)
    val localPipeline = LocalPipelineConfig.from(params)
    val localSource = buildLocalIconLayers(icon, localPipeline, params.backgroundSeparationPercent, AdaptiveForegroundMode.fromValue(params.adaptiveForegroundMode), params.adaptiveDirectMaxCoveragePercent, params.adaptiveDirectMaxCoverageIncreasePercent, params.adaptiveMaskEdgeCoveragePercent, params.adaptiveMaskMinCoveragePercent, params.adaptiveCenterEpsilonPercent)
    val localCandidateSet = buildLocalCandidates(localSource, localSourceIcon, localPipeline, OriginalForegroundCleanupMode.fromValue(params.originalForegroundCleanupMode), params.plateRemovalPercent, params.shadowRemovalPercent, params.backgroundSeparationPercent)
    val localCandidates = localCandidateSet.candidates
    val candidates = if (useGpt) {
        // P4 交界：GPT 图层收敛进 pipeline/，显式传调参 + 凭证 + 状态回调。
        val gptLayers = generateGptLayers(gptSourceIcon, localSource.recfg, localSource.recbg, params.gptCustomPrompt, GptPromptPreset.fromValue(params.gptPromptPreset), params.foregroundSubjectPercent, GptImageMode.fromValue(params.gptImageMode), gptModelId, gptBaseUrl, gptApiKey, isDebug, onStatus)
        localCandidates + (PreviewChoice.Gpt to IconCandidate(gptLayers.recfg, gptLayers.recbg, monochromeRaw = null, isLocal = false))
    } else {
        localCandidates
    }
    val selectedLocalMode = localModeOverride ?: LocalSeparationMode.fromValue(params.localSeparationMode)
    val requestedChoice = if (useGpt) {
        PreviewChoice.Gpt
    } else {
        defaultChoiceForMode(selectedLocalMode, localCandidateSet.autoChoice)
    }
    val defaultChoice = requestedChoice.takeIf { candidates.containsKey(it) }
        ?: localCandidateSet.autoChoice.takeIf { candidates.containsKey(it) }
        ?: PreviewChoice.Original
    val selections = PreviewSelections.default(defaultChoice)
    val session = GenerationSession(
        packageName = app.packageName,
        outDir = outDir,
        sourceIcon = gptSourceIcon,
        baseRecfg = localSource.recfg,
        baseRecbg = localSource.recbg,
        monochromeRaw = localSource.monochrome,
        candidates = candidates,
        autoLocalChoice = localCandidateSet.autoChoice,
    )
    writePackageOutputs(
        session = session,
        selections = selections,
        edgePolishPercent = params.edgePolishPercent,
        foregroundSubjectPercent = params.foregroundSubjectPercent,
        rmbgTunedForeground = rmbgTunedForeground,
        liquidGlassEnabled = params.liquidGlassEnabled,
        liquidGlassBackgroundMistAlpha = params.liquidGlassBackgroundMistAlpha,
        liquidGlassTopAlpha = params.liquidGlassTopAlpha,
        liquidGlassBottomAlpha = params.liquidGlassBottomAlpha,
        liquidGlassBottomDarkAlpha = params.liquidGlassBottomDarkAlpha,
        liquidGlassOuterWidth = params.liquidGlassOuterWidth,
        liquidGlassRadius = params.liquidGlassRadius,
        liquidGlassSubjectScalePercent = params.liquidGlassSubjectScalePercent,
        liquidGlassSubjectShadowAlpha = params.liquidGlassSubjectShadowAlpha,
        liquidGlassSubjectOutlineWidth = params.liquidGlassSubjectOutlineWidth,
        liquidGlassSubjectInnerOutlineWidth = params.liquidGlassSubjectInnerOutlineWidth,
        liquidGlassSubjectOpacityPercent = params.liquidGlassSubjectOpacityPercent,
        foregroundShadowLevel = params.foregroundShadowLevel,
        monochromeThemeScale = params.monochromeThemeScale,
        nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
    )
    onStatus("本地分离: ${selectedLocalMode.label}/${defaultChoice.label} · 背景 ${params.backgroundSeparationPercent} · 底板 ${params.plateRemovalPercent} · 阴影 ${params.shadowRemovalPercent} · 毛刺 ${params.edgePolishPercent}")
    return GenerationResult(outDir = outDir, session = session, selections = selections)
}
