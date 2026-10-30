package dev.artplus.mobile.system

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import java.io.File
import dev.artplus.mobile.FOREGROUND_ORIGINAL_BACKUP_NAME
import dev.artplus.mobile.GPT_SOURCE_SIZE
import dev.artplus.mobile.LocalPipelineConfig
import dev.artplus.mobile.ROOT_SCAN_TIMEOUT_MS
import dev.artplus.mobile.ROOT_UXICONS_DIR
import dev.artplus.mobile.SIZE_1X1
import dev.artplus.mobile.SIZE_1X2
import dev.artplus.mobile.SIZE_2X1
import dev.artplus.mobile.SIZE_2X2
import dev.artplus.mobile.TuningParams
import dev.artplus.mobile.data.AppEntry
import dev.artplus.mobile.imaging.AdaptiveForegroundMode
import dev.artplus.mobile.imaging.IconCandidate
import dev.artplus.mobile.imaging.LocalSeparationMode
import dev.artplus.mobile.imaging.OriginalForegroundCleanupMode
import dev.artplus.mobile.imaging.PreviewChoice
import dev.artplus.mobile.imaging.adjustColor
import dev.artplus.mobile.imaging.buildLocalCandidates
import dev.artplus.mobile.imaging.buildLocalIconLayers
import dev.artplus.mobile.imaging.candidateWithCustomOverrides
import dev.artplus.mobile.imaging.centerOnCanvas
import dev.artplus.mobile.imaging.drawDrawable
import dev.artplus.mobile.imaging.drawLocalCandidateSourceIcon
import dev.artplus.mobile.imaging.ensureCleanDir
import dev.artplus.mobile.imaging.ensureFreshDir
import dev.artplus.mobile.imaging.iconBackgroundForSize
import dev.artplus.mobile.imaging.iconForegroundForSize
import dev.artplus.mobile.imaging.monochromeForCandidate
import dev.artplus.mobile.imaging.normalDarkForeground
import dev.artplus.mobile.imaging.renderCandidateForegroundBase
import dev.artplus.mobile.imaging.resizeBitmap
import dev.artplus.mobile.imaging.savePng
import dev.artplus.mobile.imaging.scaleMonochromeForTheme
import dev.artplus.mobile.imaging.simpleMonochromeAlphaFromDefaultSubject
import dev.artplus.mobile.pipeline.GenerationSession
import dev.artplus.mobile.pipeline.GptImageMode
import dev.artplus.mobile.pipeline.GptPromptPreset
import dev.artplus.mobile.pipeline.PreviewMode
import dev.artplus.mobile.pipeline.generateGptLayers
import dev.artplus.mobile.ui.cards.GenerationResult
import dev.artplus.mobile.ui.cards.PreviewSelections

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
    MonochromeOnly("monochrome", "单色"),
    LightOnly("light", "亮色"),
    DarkOnly("dark", "暗色"),
    MonochromeLightOnly("monochrome_light", "单色亮色"),
    MonochromeDarkOnly("monochrome_dark", "单色暗色");

    companion object {
        fun fromValue(value: String?): RootWriteMode =
            entries.firstOrNull { it.value == value || (it == StandardOnly && value == "default") } ?: All
    }
}

/** 首次接管某个包时写入的标记；存在即表示该包目录已被 ArtPlus 写过，原图备份不再重建。 */
internal const val ARTPLUS_MODIFIED_MARKER = ".artplusmodified"

/** 原图备份目录名，位于包目录内；一旦建立，里面的文件永不被覆盖。 */
internal const val PRISTINE_BACKUP_DIR = "Backup"

/**
 * 首次写入前的原图备份命令。
 *
 * 包目录里还没有 [ARTPLUS_MODIFIED_MARKER] 且存在 PNG 时，把这些 PNG 原样复制进
 * `<包名>/Backup/` 再打标记：原图通常是 ColorOS 生成的手绘图标，覆盖后无处可寻。
 * 逐个文件先判存在再复制（不依赖 `cp -n`），保证备份目录里已有的文件永不覆盖。
 */
private fun pristineBackupCommand(target: String): String {
    val marker = shQuote("$target/$ARTPLUS_MODIFIED_MARKER")
    val backupDir = shQuote("$target/$PRISTINE_BACKUP_DIR")
    return """
        if [ ! -f $marker ]; then
            if find ${shQuote(target)} -maxdepth 1 -type f -name '*.png' | grep -q .; then
                mkdir -p $backupDir
                chmod 0755 $backupDir
                find ${shQuote(target)} -maxdepth 1 -type f -name '*.png' -exec sh -c '
                    dst="${'$'}1"; shift
                    for f do
                        name="${'$'}{f##*/}"
                        [ -e "${'$'}dst/${'$'}name" ] || cp -f "${'$'}f" "${'$'}dst/${'$'}name"
                    done
                ' sh $backupDir {} +
                find $backupDir -maxdepth 1 -type f -name '*.png' -exec chmod 0644 {} +
            fi
            touch $marker
            chmod 0644 $marker
        fi
    """.trimIndent()
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
        // 细分档只覆盖对应那几套文件，不删除目标目录里其他档位的图标：
        // 「应用亮色」不该把用户已经写好的暗色/单色一起抹掉。
        // day/nsd/mat/peb 是亮色前景的调色衍生（ColorOS 主题取色用），跟亮色一起走。
        RootWriteMode.LightOnly -> """
            find ${shQuote(source)} -maxdepth 1 -type f \( -name 'recbg*.png' -o -name 'recfg*.png' -o -name 'day.png' -o -name 'nsd.png' -o -name 'mat.png' -o -name 'peb.png' \) -exec cp -f {} ${shQuote(target)}/ \;
        """.trimIndent()
        RootWriteMode.DarkOnly -> """
            find ${shQuote(source)} -maxdepth 1 -type f -name 'rec_night*.png' -exec cp -f {} ${shQuote(target)}/ \;
        """.trimIndent()
        RootWriteMode.MonochromeLightOnly -> """
            find ${shQuote(source)} -maxdepth 1 -type f -name 'monochrome_light*.png' -exec cp -f {} ${shQuote(target)}/ \;
        """.trimIndent()
        // 单色暗色 = monochrome.png（旧别名）+ monochrome_dark.png + monochrome_<尺寸>.png 的
        // morph 变体（后三者都由单色暗色派生），排除单色亮色那份。
        RootWriteMode.MonochromeDarkOnly -> """
            find ${shQuote(source)} -maxdepth 1 -type f -name 'monochrome*.png' ! -name 'monochrome_light*.png' -exec cp -f {} ${shQuote(target)}/ \;
        """.trimIndent()
    }
    val command = """
        set -e
        mkdir -p ${shQuote(target)}
        ${pristineBackupCommand(target)}
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
 * 列出所有带 [ARTPLUS_MODIFIED_MARKER] 的包名；root 读不到时返回 null，
 * 便于调用方区分「没有接管包」与「这次没读到」。
 */
internal fun listArtPlusModifiedPackagesOrNull(): Set<String>? {
    val command = """
        if [ -d ${shQuote(ROOT_UXICONS_DIR)} ]; then
            find ${shQuote(ROOT_UXICONS_DIR)} -maxdepth 2 -type f -name ${shQuote(ARTPLUS_MODIFIED_MARKER)} 2>/dev/null | while IFS= read -r marker; do
                dir=${'$'}{marker%/$ARTPLUS_MODIFIED_MARKER}
                name=${'$'}{dir##*/}
                [ -n "${'$'}name" ] || continue
                printf '%s\n' "${'$'}name"
            done
        fi
    """.trimIndent()
    val output = try {
        runRootCommand(command, ROOT_SCAN_TIMEOUT_MS)
    } catch (_: Exception) {
        return null
    }
    return output.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toSet()
}

/** 列出所有带 [ARTPLUS_MODIFIED_MARKER] 的包名（一次 su；读不到返回空集）。 */
internal fun listArtPlusModifiedPackages(): Set<String> = listArtPlusModifiedPackagesOrNull().orEmpty()

/**
 * 还原一个被接管的包目录，返回是否成功。
 *
 * 两种接管都要能退出：
 * - 有 `Backup/`（接管前目录里就有原图）：清空除 `Backup` 外的全部内容 →
 *   把 `Backup` 内条目移回原位 → 删除 `Backup` 与标记。
 * - 只有标记、没有 `Backup/`（接管前目录里本来就没有 PNG，备份步骤什么都没存）：
 *   删掉 ArtPlus 写进去的 `*.png`；非 PNG 的附属文件原地保留，因为那不是 ArtPlus 写的。
 *
 * 两种都删标记；目录空了顺手移除，让系统回落到默认图标。
 */
internal fun restorePackageFromBackup(packageName: String): Boolean {
    val target = "$ROOT_UXICONS_DIR/$packageName"
    val backupDir = shQuote("$target/$PRISTINE_BACKUP_DIR")
    val command = """
        set -e
        [ -d ${shQuote(target)} ] || exit 3
        cd ${shQuote(target)}
        if [ -d $backupDir ]; then
            find . -mindepth 1 -maxdepth 1 ! -name $PRISTINE_BACKUP_DIR -exec rm -rf {} +
            find $PRISTINE_BACKUP_DIR -mindepth 1 -maxdepth 1 -exec mv -f {} . \;
            rm -rf $PRISTINE_BACKUP_DIR
        else
            find . -mindepth 1 -maxdepth 1 -type f -name '*.png' -delete
        fi
        rm -f ${shQuote("$target/$ARTPLUS_MODIFIED_MARKER")}
        find . -mindepth 1 -maxdepth 1 -type f -name '*.png' -exec chmod 0644 {} +
        restorecon -RF ${shQuote(target)} 2>/dev/null || true
        cd /
        rmdir ${shQuote(target)} 2>/dev/null || true
    """.trimIndent()
    return try {
        runRootCommand(command, ROOT_SCAN_TIMEOUT_MS)
        true
    } catch (_: Exception) {
        false
    }
}

/**
 * Slice 1.4 纯移动（零 Activity 状态，同包直接用，Activity 内删本体零 wrapper）：
 * hasGeneratedPackageBaseAssets / decodeGeneratedBitmap / buildGeneratedPackageSession。
 * su 调用顺序、mount/写路径与权限模式未变，不新增 root 行为。
 */

internal fun hasGeneratedPackageBaseAssets(dir: File): Boolean =
    dir.isDirectory &&
        File(dir, "recbg.png").isFile &&
        File(dir, "recfg.png").isFile

internal fun decodeGeneratedBitmap(dir: File, name: String): Bitmap? =
    BitmapFactory.decodeFile(File(dir, name).absolutePath)

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
    foregroundShadowLevel: Int,
    monochromeThemeScale: Float,
    nightSubjectLightBackgroundEnabled: Boolean,
) {
    fun foregroundForOutputs(source: Bitmap, width: Int, height: Int): Bitmap =
        iconForegroundForSize(source, width, height, foregroundShadowLevel)

    fun backgroundForOutputs(source: Bitmap, width: Int, height: Int): Bitmap =
        iconBackgroundForSize(source, width, height)

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
        foregroundShadowLevel = params.foregroundShadowLevel,
        monochromeThemeScale = params.monochromeThemeScale,
        nightSubjectLightBackgroundEnabled = params.nightSubjectLightBackgroundEnabled,
    )
    onStatus("本地分离: ${selectedLocalMode.label}/${defaultChoice.label} · 背景 ${params.backgroundSeparationPercent} · 底板 ${params.plateRemovalPercent} · 阴影 ${params.shadowRemovalPercent} · 毛刺 ${params.edgePolishPercent}")
    return GenerationResult(outDir = outDir, session = session, selections = selections)
}
