package dev.artplus.mobile

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.net.LocalServerSocket
import android.net.LocalSocket
import java.io.File
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import org.json.JSONObject

/**
 * Debug 服务器（P4 拆分）：TCP + 本地抽象 socket 双通道 HTTP 服务。
 *
 * 从 MainActivity 迁移而来：原 `private inner class DebugHttpServer` 顶层化为
 * `internal class DebugHttpServer`，原 `private fun` 现 `internal`。
 * - 内部类不再隐式持有 Activity：经 [DebugServerHooks] 回调解耦，server 不传/不存 Activity。
 * - 读 Activity 调参的 `currentDebugParamsOnMain`/`applyDebugParams` 系留置 Activity
 *   （读 186 live vars，P5 再议），server 经 hooks.currentParams()/applyParams() 调用。
 * - 纯 helper 顶层化：`DebugHttpResponse`/`DebugHttpRequest`、
 *   `parseQuery`/`urlDecode`/`headerLines`/`jsonToParamMap`。
 * DEBUG_* 常量已提升进 TuningParams.kt。
 */

/** server 回 Activity 的边界回调（Activity 用匿名对象实现，server 侧只见接口）。 */
internal interface DebugServerHooks {
    fun onStatus(message: String)
    fun homeHtml(): String
    fun currentParams(): JSONObject
    fun applyParams(params: Map<String, String>): JSONObject
    fun inspectPackage(packageName: String, includeRmbg: Boolean): JSONObject
    fun startGeneration(
        packageName: String,
        useGpt: Boolean,
        installWithRoot: Boolean,
        debugMode: LocalSeparationMode,
        rootWriteMode: RootWriteMode,
    ): Boolean
    fun isTokenValid(token: String?): Boolean
}

internal data class DebugHttpResponse(
    val status: Int,
    val contentType: String,
    val body: String,
)

internal data class DebugHttpRequest(
    val method: String,
    val target: String,
    val headers: Map<String, String>,
    val body: String,
)

internal fun jsonToParamMap(json: JSONObject): Map<String, String> {
    val params = mutableMapOf<String, String>()
    val keys = json.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        if (!json.isNull(key)) {
            params[key] = json.optString(key)
        }
    }
    return params
}

internal fun parseQuery(query: String): Map<String, String> {
    if (query.isBlank()) {
        return emptyMap()
    }
    return query.split('&')
        .filter { it.isNotBlank() }
        .associate { pair ->
            val key = pair.substringBefore('=')
            val value = pair.substringAfter('=', "")
            urlDecode(key) to urlDecode(value)
        }
}

internal fun urlDecode(value: String): String =
    URLDecoder.decode(value, StandardCharsets.UTF_8.name())

internal fun headerLines(headerText: String): List<String> =
    headerText
        .replace("\r\n", "\n")
        .split('\n')
        .map { it.trimEnd('\r') }
        .filter { it.isNotBlank() }

/**
 * Slice 1.6 移动：Debug 生成 Intent 判定本体（原 MainActivity.isDebugGenerateIntent）。
 *
 * EXTRA_* 已提升至 TuningParams.kt，同包直接引用；token 校验经显式回调，
 * 不传/不存 Activity。MainActivity 留 1 参 wrapper，调用点零改动。
 */
internal fun isDebugGenerateIntent(intent: Intent?, isTokenValid: (String?) -> Boolean): Boolean =
    intent?.getStringExtra(EXTRA_DEBUG_GENERATE_PACKAGE)?.isNotBlank() == true &&
        isTokenValid(intent.getStringExtra(EXTRA_DEBUG_GENERATE_TOKEN))

/**
 * Slice 1.6 移动：Debug 生成 Intent 编排本体（原 MainActivity.handleDebugGenerateIntent）。
 *
 * 纯移动：isDebugBuild/token 校验/实际生成均经显式回调，不持 Activity；
 * 端口/线程模型不变（生成线程由 startDebugGeneration 内创建）。
 * MainActivity 留 1 参 wrapper，onCreate/onNewIntent 调用点零改动。
 */
internal fun handleDebugGenerateIntent(
    intent: Intent?,
    isDebugBuild: () -> Boolean,
    isTokenValid: (String?) -> Boolean,
    startGeneration: (
        packageName: String,
        useGpt: Boolean,
        installWithRoot: Boolean,
        debugMode: LocalSeparationMode,
        rootWriteMode: RootWriteMode,
    ) -> Boolean,
) {
    if (!isDebugBuild()) {
        return
    }
    if (!isTokenValid(intent?.getStringExtra(EXTRA_DEBUG_GENERATE_TOKEN))) {
        return
    }
    val debugPackageName = intent
        ?.getStringExtra(EXTRA_DEBUG_GENERATE_PACKAGE)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: return
    val useGpt = intent.getBooleanExtra(EXTRA_DEBUG_GENERATE_USE_GPT, false)
    val installWithRoot = intent.getBooleanExtra(EXTRA_DEBUG_GENERATE_INSTALL_ROOT, false)
    val debugMode = LocalSeparationMode.fromValue(
        intent.getStringExtra(EXTRA_DEBUG_GENERATE_MODE),
    )
    val rootWriteMode = RootWriteMode.fromValue(
        intent.getStringExtra(EXTRA_DEBUG_GENERATE_ROOT_WRITE_MODE),
    )
    startGeneration(
        debugPackageName,
        useGpt,
        installWithRoot,
        debugMode,
        rootWriteMode,
    )
}

/**
 * Slice 1.6 移动：Debug 后台生成本体（原 MainActivity.startDebugGeneration）。
 *
 * 纯移动，行为 100% 等价：
 * - Activity 状态一律经显式回调（isBusy/statusText/生成名单/预览/session/UiState），
 *   不传/不存 Activity；Context 侧显式传参（packageManager/prefs/getAppInfo）。
 * - generateArtPlusPackage 经显式回调（Activity wrapper 负责快照 params/凭证）；
 *   installWithRoot/markPackageGenerated 系同包顶层符号，直接调用（不重复定义）。
 * - 线程模型不变：仍 `Thread { ... }.start()`（非 daemon，与原一致），
 *   主线程栅栏仍经 runOnMainSync，启动/停止顺序不变。
 * MainActivity 留 5 参 wrapper，/debug/generate 与 Intent 编排调用点零改动。
 */
internal fun startDebugGeneration(
    packageName: String,
    useGpt: Boolean,
    installWithRoot: Boolean,
    debugMode: LocalSeparationMode,
    rootWriteMode: RootWriteMode,
    runOnMainSync: ((() -> Unit) -> Unit),
    isBusyGet: () -> Boolean,
    setBusy: (Boolean) -> Unit,
    setStatusText: (String) -> Unit,
    onStatus: (String) -> Unit,
    getAppInfo: (String) -> ApplicationInfo,
    packageManager: PackageManager,
    prefs: SharedPreferences,
    getGeneratedNames: () -> Set<String>,
    setGeneratedNames: (Set<String>) -> Unit,
    generatePackage: (
        app: AppEntry,
        useGpt: Boolean,
        localModeOverride: LocalSeparationMode?,
    ) -> GenerationResult,
    setActiveSession: (GenerationSession?) -> Unit,
    updateSelections: (PreviewSelections) -> Unit,
    setPreviewChoiceMode: (PreviewMode?) -> Unit,
    setPreviewPackage: (String?) -> Unit,
    setPreviewDir: (String?) -> Unit,
    bumpPreviewVersion: () -> Unit,
    onSaveUiState: () -> Unit,
): Boolean {
    var accepted = false
    runOnMainSync {
        if (isBusyGet()) {
            setStatusText("调试生成排队失败，当前正在处理: $packageName")
        } else {
            setBusy(true)
            setStatusText("调试生成中: $packageName")
            accepted = true
        }
    }
    if (!accepted) {
        return false
    }
    Thread {
        try {
            val info = getAppInfo(packageName)
            val label = runCatching { info.loadLabel(packageManager)?.toString() }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: packageName
            val launchable = packageManager.getLaunchIntentForPackage(packageName) != null
            val entry = AppEntry(
                label = label,
                packageName = packageName,
                applicationInfo = info,
                launchable = launchable,
                iconKey = "${packageName}:${info.uid}:${info.sourceDir}",
            )
            val result = generatePackage(
                entry,
                useGpt,
                debugMode,
            )
            if (installWithRoot) {
                installWithRoot(result.outDir, packageName, rootWriteMode)
                runOnMainSync {
                    setGeneratedNames(markPackageGenerated(prefs, getGeneratedNames(), packageName))
                    setStatusText("调试生成完成并${rootWriteMode.label}写入 Root，未刷新，请手动点刷新 ART+ 图标: ${result.outDir.absolutePath}")
                }
            } else {
                runOnMainSync {
                    setStatusText("调试生成完成: ${result.outDir.absolutePath}")
                }
            }
            runOnMainSync {
                setActiveSession(result.session)
                updateSelections(result.selections)
                setPreviewChoiceMode(null)
                setPreviewPackage(packageName)
                setPreviewDir(result.outDir.absolutePath)
                bumpPreviewVersion()
                onSaveUiState()
            }
        } catch (error: Exception) {
            onStatus("调试生成失败: ${error.message ?: error.javaClass.simpleName}")
        } finally {
            runOnMainSync { setBusy(false) }
        }
    }.start()
    return true
}

/**
 * Slice 1.6 顶层化：原 debugInspectPackage 内嵌套 saveLayer（捕获 outDir/metadata）。
 *
 * 纯移动：显式传 outDir/metrics，同包直接调用 savePng/bitmapStatsJson（不重复定义）。
 */
internal fun saveLayer(outDir: File, name: String, bitmap: Bitmap, metrics: JSONObject): Bitmap {
    savePng(bitmap, File(outDir, "$name.png"))
    metrics.put(name, bitmapStatsJson(bitmap))
    return bitmap
}

/**
 * Slice 1.6 移动：Debug inspect 本体（原 MainActivity.debugInspectPackage，含 saveLayer 同簇）。
 *
 * 纯移动，行为 100% 等价：
 * - Activity 状态一律显式传参/回调（getAppInfo/packageManager/目录/tuning 快照/
 *   runOnMainSync/RMBG 上报/buildRmbg/describeFailure），不传/不存 Activity。
 * - tuning 系调用方快照（原 currentTuningParams() 即 mainViewModel.params.value，
 *   同线程快照语义不变）；localPipeline 由 tuning 派生，与原一致。
 * - 纯 helper（drawDrawable/drawLocalCandidateSourceIcon/subtractBackground/
 *   chooseBetterAdaptiveForeground/hasAdaptiveMaskArtifact/isUsableDirectAdaptiveForeground/
 *   alphaCoverage/buildLocalIconLayers/buildLocalCandidates/renderCandidateForeground/
 *   nightForeground/monochromeForCandidate/savePng/bitmapStatsJson/resizeBitmap/
 *   ensureFreshDir）系同包既有符号，直接调用（不重复定义）。
 * - buildRmbg/describeFailure 经显式回调（Activity wrapper 负责 lock/runtime/文案），
 *   RMBG 上报仍经 runOnMainSync，线程模型不变。
 * - 前景渲染/单色经显式回调（Activity wrapper 负责快照 params/rmbgTunedForeground，
 *   与原直接调 wrapper 语义一致；顶层同名符号需 5/16 个显式调参，不在此重复组装）。
 * MainActivity 留 2 参 wrapper，/debug/inspect 调用点零改动。
 */
internal fun debugInspectPackage(
    packageName: String,
    includeRmbg: Boolean,
    getAppInfo: (String) -> ApplicationInfo,
    packageManager: PackageManager,
    externalLabDir: File?,
    filesDir: File,
    tuning: TuningParams,
    runOnMainSync: ((() -> Unit) -> Unit),
    setLastReport: (RmbgInferenceReport?) -> Unit,
    setLastError: (String?) -> Unit,
    buildRmbgDebug: (Bitmap) -> RmbgDebugCandidate,
    describeFailure: (Throwable) -> String,
    renderForeground: (IconCandidate) -> Bitmap,
    monochromeFor: (IconCandidate, Boolean) -> Bitmap,
): JSONObject {
    val info = getAppInfo(packageName)
    val label = runCatching { info.loadLabel(packageManager)?.toString() }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: packageName
    val icon = info.loadIcon(packageManager)
    val base = externalLabDir ?: File(filesDir, "ArtPlusLab")
    val outDir = File(base, packageName)
    ensureFreshDir(outDir)
    val debugTuning = tuning
    val localPipeline = LocalPipelineConfig.from(debugTuning)

    val metadata = JSONObject()
        .put("ok", true)
        .put("package", packageName)
        .put("label", label)
        .put("output_dir", outDir.absolutePath)
        .put("source_dir", info.sourceDir ?: "")
        .put("public_source_dir", info.publicSourceDir ?: "")
        .put("is_adaptive", icon is AdaptiveIconDrawable)
        .put("settings", debugTuning.toJson())

    val source240 = saveLayer(outDir, "source_icon_240_opaque", drawDrawable(icon, SIZE_1X1, SIZE_1X1, transparent = false), metadata)
    val candidateSource240 = saveLayer(
        outDir,
        "source_icon_240_candidate",
        drawLocalCandidateSourceIcon(icon, SIZE_1X1, SIZE_1X1),
        metadata,
    )
    saveLayer(outDir, "source_icon_240_transparent", drawDrawable(icon, SIZE_1X1, SIZE_1X1, transparent = true), metadata)

    if (icon is AdaptiveIconDrawable) {
        val renderSize = SIZE_1X1 * LOCAL_ICON_RENDER_SCALE
        val background = drawDrawable(
            icon.background ?: ColorDrawable(AndroidColor.WHITE),
            renderSize,
            renderSize,
            transparent = false,
        )
        val direct = drawDrawable(icon.foreground, renderSize, renderSize, transparent = true)
        val composed = drawDrawable(icon, renderSize, renderSize, transparent = true)
        val subtracted = if (localPipeline.backgroundSeparationEnabled) {
            subtractBackground(composed, background, pipeline = localPipeline, backgroundSeparationPercent = tuning.backgroundSeparationPercent)
        } else {
            composed
        }
        val selection = chooseBetterAdaptiveForeground(subtracted, direct, background, localPipeline, AdaptiveForegroundMode.fromValue(tuning.adaptiveForegroundMode), tuning.adaptiveDirectMaxCoveragePercent, tuning.adaptiveDirectMaxCoverageIncreasePercent, tuning.adaptiveMaskEdgeCoveragePercent, tuning.adaptiveMaskMinCoveragePercent, tuning.adaptiveCenterEpsilonPercent)
        val chosen = selection.bitmap
        val adaptiveJson = JSONObject()
        saveLayer(outDir, "adaptive_background_240", resizeBitmap(background, SIZE_1X1, SIZE_1X1), adaptiveJson)
        saveLayer(outDir, "adaptive_composed_240", resizeBitmap(composed, SIZE_1X1, SIZE_1X1), adaptiveJson)
        saveLayer(outDir, "adaptive_subtracted_foreground_240", resizeBitmap(subtracted, SIZE_1X1, SIZE_1X1), adaptiveJson)
        saveLayer(outDir, "adaptive_direct_foreground_240", resizeBitmap(direct, SIZE_1X1, SIZE_1X1), adaptiveJson)
        saveLayer(outDir, "adaptive_chosen_foreground_240", resizeBitmap(chosen, SIZE_1X1, SIZE_1X1), adaptiveJson)
        adaptiveJson
            .put("subtracted_has_mask_artifact", hasAdaptiveMaskArtifact(subtracted, tuning.adaptiveMaskEdgeCoveragePercent, tuning.adaptiveMaskMinCoveragePercent))
            .put("direct_usable", isUsableDirectAdaptiveForeground(direct, alphaCoverage(subtracted), tuning.adaptiveDirectMaxCoveragePercent, tuning.adaptiveDirectMaxCoverageIncreasePercent))
            .put("subtracted_coverage", alphaCoverage(subtracted))
            .put("direct_coverage", alphaCoverage(direct))
            .put("chosen_preserve_geometry", selection.preserveGeometry)
        metadata.put("adaptive", adaptiveJson)
    }

    val localSource = buildLocalIconLayers(icon, localPipeline, tuning.backgroundSeparationPercent, AdaptiveForegroundMode.fromValue(tuning.adaptiveForegroundMode), tuning.adaptiveDirectMaxCoveragePercent, tuning.adaptiveDirectMaxCoverageIncreasePercent, tuning.adaptiveMaskEdgeCoveragePercent, tuning.adaptiveMaskMinCoveragePercent, tuning.adaptiveCenterEpsilonPercent)
    val localJson = JSONObject()
    saveLayer(outDir, "local_base_recbg", localSource.recbg, localJson)
    saveLayer(outDir, "local_base_recfg", localSource.recfg, localJson)
    localSource.monochrome?.let { saveLayer(outDir, "local_base_monochrome", it, localJson) }
    val candidateSet = buildLocalCandidates(localSource, candidateSource240, localPipeline, OriginalForegroundCleanupMode.fromValue(tuning.originalForegroundCleanupMode), tuning.plateRemovalPercent, tuning.shadowRemovalPercent, tuning.backgroundSeparationPercent)
    localJson.put("auto_choice", candidateSet.autoChoice.name.lowercase(Locale.US))
    metadata.put("local", localJson)

    val candidatesJson = JSONObject()
    candidateSet.candidates.forEach { (choice, candidate) ->
        val key = choice.name.lowercase(Locale.US)
        val candidateJson = JSONObject()
            .put("label", choice.label)
            .put("preserve_geometry", candidate.preserveGeometry)
        saveLayer(outDir, "candidate_${key}_raw", candidate.recfgRaw, candidateJson)
        val rendered = renderForeground(candidate)
        saveLayer(outDir, "candidate_${key}_rendered", rendered, candidateJson)
        saveLayer(outDir, "candidate_${key}_night", nightForeground(rendered, candidate.recbg), candidateJson)
        saveLayer(outDir, "candidate_${key}_monochrome_light", monochromeFor(candidate, true), candidateJson)
        saveLayer(outDir, "candidate_${key}_monochrome_dark", monochromeFor(candidate, false), candidateJson)
        candidatesJson.put(key, candidateJson)
    }

    if (includeRmbg) {
        val rmbgJson = JSONObject()
        try {
            val rmbgDebug = buildRmbgDebug(source240)
            val rmbgCandidate = rmbgDebug.result?.candidate
            val validationWarning = rmbgDebug.result?.validationWarning
            rmbgJson
                .put("coverage", rmbgDebug.coverage)
                .put("manual_usable", rmbgDebug.manualUsable)
                .put("auto_usable", rmbgDebug.result?.autoUsable ?: false)
                .put("bounds", rmbgDebug.boundsText)
                .put("crop_risk", rmbgDebug.cropRisk)
                .put("backend", rmbgDebug.inference.actualBackend.value)
                .put("elapsed_ms", rmbgDebug.inference.elapsedMs)
            saveLayer(outDir, "candidate_rmbg_raw", rmbgDebug.foreground, rmbgJson)
            val rendered = renderForeground(
                rmbgCandidate ?: IconCandidate(
                        recfgRaw = rmbgDebug.foreground,
                        recbg = localSource.recbg,
                        monochromeRaw = rmbgDebug.foreground,
                        isLocal = false,
                ),
            )
            saveLayer(outDir, "candidate_rmbg_rendered", rendered, rmbgJson)
            saveLayer(outDir, "candidate_rmbg_night", nightForeground(rendered, localSource.recbg), rmbgJson)
            saveLayer(outDir, "candidate_rmbg_monochrome_light", monochromeFor(
                rmbgCandidate ?: IconCandidate(rmbgDebug.foreground, localSource.recbg, monochromeRaw = rmbgDebug.foreground, isLocal = false),
                true,
            ), rmbgJson)
            saveLayer(outDir, "candidate_rmbg_monochrome_dark", monochromeFor(
                rmbgCandidate ?: IconCandidate(rmbgDebug.foreground, localSource.recbg, monochromeRaw = rmbgDebug.foreground, isLocal = false),
                false,
            ), rmbgJson)
            if (validationWarning != null) {
                rmbgJson.put("validation_warning", validationWarning)
            }
            rmbgJson.put("ok", true)
            runOnMainSync {
                setLastReport(rmbgDebug.inference)
                setLastError(null)
            }
        } catch (error: Throwable) {
            val message = describeFailure(error)
            rmbgJson
                .put("ok", false)
                .put("error", message)
            runOnMainSync {
                setLastError(message)
                setLastReport(null)
            }
        }
        candidatesJson.put("rmbg", rmbgJson)
    }

    metadata.put("candidates", candidatesJson)
    FileOutputStream(File(outDir, "metadata.json")).use { output ->
        output.write(metadata.toString(2).toByteArray(Charsets.UTF_8))
    }
    return metadata
}

internal class DebugHttpServer(private val port: Int, private val hooks: DebugServerHooks) {
    @Volatile
    private var running = false
    private var serverSocket: ServerSocket? = null
    private var localServerSocket: LocalServerSocket? = null
    private var thread: Thread? = null
    private var localThread: Thread? = null

    fun start() {
        if (running) {
            return
        }
        running = true
        startTcpServer()
        startLocalServer()
    }

    private fun startTcpServer() {
        thread = Thread({
            runCatching {
                ServerSocket().use { server ->
                    server.reuseAddress = true
                    server.bind(InetSocketAddress(InetAddress.getLoopbackAddress(), port))
                    serverSocket = server
                    while (running) {
                        val socket = runCatching { server.accept() }.getOrNull() ?: break
                        Thread({ handle(socket) }, "ArtPlusDebugHttpClient").also {
                            it.isDaemon = true
                            it.start()
                        }
                    }
                }
            }.onFailure {
                if (running) {
                    hooks.onStatus("Debug HTTP 启动失败: ${it.message ?: it.javaClass.simpleName}")
                }
            }
        }, "ArtPlusDebugHttp").also {
            it.isDaemon = true
            it.start()
        }
    }

    private fun startLocalServer() {
        localThread = Thread({
            var server: LocalServerSocket? = null
            try {
                server = LocalServerSocket(DEBUG_HTTP_ABSTRACT_NAME)
                localServerSocket = server
                while (running) {
                    val socket = runCatching { server.accept() }.getOrNull() ?: break
                    Thread({ handle(socket) }, "ArtPlusDebugLocalClient").also {
                        it.isDaemon = true
                        it.start()
                    }
                }
            } catch (error: Exception) {
                if (running) {
                    hooks.onStatus("Debug local HTTP 启动失败: ${error.message ?: error.javaClass.simpleName}")
                }
            } finally {
                runCatching { server?.close() }
            }
        }, "ArtPlusDebugLocalHttp").also {
            it.isDaemon = true
            it.start()
        }
    }

    fun stop() {
        running = false
        runCatching { serverSocket?.close() }
        runCatching { localServerSocket?.close() }
        serverSocket = null
        localServerSocket = null
        thread = null
        localThread = null
    }

    private fun handle(socket: Socket) {
        socket.use { client ->
            try {
                client.soTimeout = DEBUG_HTTP_READ_TIMEOUT_MS
                handleStreams(client.getInputStream(), client.getOutputStream())
            } catch (error: Exception) {
                writeResponseQuietly(client.getOutputStream(), errorResponse(error))
            }
        }
    }

    private fun handle(socket: LocalSocket) {
        try {
            handleStreams(socket.inputStream, socket.outputStream)
        } catch (error: Exception) {
            writeResponseQuietly(socket.outputStream, errorResponse(error))
        } finally {
            runCatching { socket.close() }
        }
    }

    private fun handleStreams(input: InputStream, output: OutputStream) {
        val request = readRequest(input)
        if (request == null) {
            writeResponse(output, DebugHttpResponse(400, "application/json; charset=utf-8", "{\"ok\":false,\"error\":\"bad request\"}"))
            return
        }
        writeResponse(output, route(request))
    }

    private fun readRequest(input: InputStream): DebugHttpRequest? {
        val headerBytes = readHttpHeader(input) ?: return null
        val headerText = String(headerBytes, StandardCharsets.UTF_8)
        val lines = headerLines(headerText)
        val requestLine = lines.firstOrNull() ?: return null
        val parts = requestLine.trim().split(Regex("\\s+"))
        if (parts.size < 2) {
            return null
        }
        val headers = mutableMapOf<String, String>()
        lines.drop(1).forEach { line ->
            val separator = line.indexOf(':')
            if (separator > 0) {
                headers[line.substring(0, separator).lowercase(Locale.US)] = line.substring(separator + 1).trim()
            }
        }
        val contentLength = headers["content-length"]?.toIntOrNull() ?: 0
        val body = if (contentLength > 0) {
            readExactlyAvailable(input, contentLength.coerceAtMost(DEBUG_HTTP_MAX_BODY_BYTES))
        } else {
            ""
        }
        return DebugHttpRequest(
            method = parts[0].uppercase(Locale.US),
            target = parts[1],
            headers = headers,
            body = body,
        )
    }

    private fun readHttpHeader(input: InputStream): ByteArray? {
        val header = ByteArrayOutputStream()
        while (header.size() < DEBUG_HTTP_MAX_HEADER_BYTES) {
            val next = input.read()
            if (next < 0) {
                return if (header.size() == 0) null else header.toByteArray()
            }
            header.write(next)
            val bytes = header.toByteArray()
            val size = bytes.size
            if (
                size >= 4 &&
                bytes[size - 4] == '\r'.code.toByte() &&
                bytes[size - 3] == '\n'.code.toByte() &&
                bytes[size - 2] == '\r'.code.toByte() &&
                bytes[size - 1] == '\n'.code.toByte()
            ) {
                return bytes
            }
            if (
                size >= 2 &&
                bytes[size - 2] == '\n'.code.toByte() &&
                bytes[size - 1] == '\n'.code.toByte()
            ) {
                return bytes
            }
        }
        return null
    }

    private fun readExactlyAvailable(input: InputStream, length: Int): String {
        val bodyBytes = ByteArray(length)
        var offset = 0
        while (offset < length) {
            val read = input.read(bodyBytes, offset, length - offset)
            if (read < 0) {
                break
            }
            offset += read
        }
        return String(bodyBytes, 0, offset, StandardCharsets.UTF_8)
    }

    private fun route(request: DebugHttpRequest): DebugHttpResponse {
        val method = request.method
        val target = request.target
        val body = request.body
        val path = target.substringBefore('?')
        val query = parseQuery(target.substringAfter('?', ""))
        return try {
            if (!isAuthorizedDebugRequest(request, query, body)) {
                return jsonResponse(JSONObject().put("ok", false).put("error", "forbidden"), 403)
            }
            when {
                method == "GET" && (path == "/" || path == "/debug") ->
                    DebugHttpResponse(200, "text/html; charset=utf-8", hooks.homeHtml())
                method == "GET" && path == "/debug/params" ->
                    jsonResponse(hooks.currentParams())
                method == "POST" && path == "/debug/params" -> {
                    val params = query.toMutableMap()
                    params.putAll(parseBodyParams(body))
                    jsonResponse(hooks.applyParams(params))
                }
                path == "/debug/status" ->
                    jsonResponse(hooks.currentParams())
                method == "POST" && path == "/debug/inspect" -> {
                    val params = query.toMutableMap()
                    params.putAll(parseBodyParams(body))
                    val packageName = params["package"]?.trim().orEmpty()
                    if (packageName.isEmpty()) {
                        jsonResponse(JSONObject().put("ok", false).put("error", "missing package"), 400)
                    } else {
                        jsonResponse(
                            hooks.inspectPackage(
                                packageName = packageName,
                                includeRmbg = params["include_rmbg"]?.toBooleanStrictOrNull() ?: false,
                            ),
                        )
                    }
                }
                method == "POST" && path == "/debug/generate" -> {
                    val params = query.toMutableMap()
                    params.putAll(parseBodyParams(body))
                    val packageName = params["package"]?.trim().orEmpty()
                    if (packageName.isEmpty()) {
                        jsonResponse(JSONObject().put("ok", false).put("error", "missing package"), 400)
                    } else {
                        val mode = LocalSeparationMode.fromValue(params["mode"])
                        val accepted = hooks.startGeneration(
                            packageName = packageName,
                            useGpt = params["use_gpt"]?.toBooleanStrictOrNull() ?: false,
                            installWithRoot = params["install_root"]?.toBooleanStrictOrNull() ?: false,
                            debugMode = mode,
                            rootWriteMode = RootWriteMode.fromValue(params["root_write_mode"]),
                        )
                        val snapshot = hooks.currentParams()
                        jsonResponse(
                            JSONObject()
                                .put("ok", accepted)
                                .put("package", packageName)
                                .put("mode", mode.value)
                                .put("rmbg_status", snapshot.optString("rmbg_status"))
                                .put("status", snapshot.optString("status")),
                            if (accepted) 202 else 409,
                        )
                    }
                }
                else -> jsonResponse(JSONObject().put("ok", false).put("error", "not found"), 404)
            }
        } catch (error: Exception) {
            jsonResponse(
                JSONObject()
                    .put("ok", false)
                    .put("error", error.message ?: error.javaClass.simpleName),
                500,
            )
        }
    }

    private fun isAuthorizedDebugRequest(
        request: DebugHttpRequest,
        query: Map<String, String>,
        body: String,
    ): Boolean {
        val bodyParams = runCatching { parseBodyParams(body) }.getOrDefault(emptyMap())
        val token = request.headers[DEBUG_HTTP_TOKEN_HEADER.lowercase(Locale.US)]
            ?: query[DEBUG_HTTP_TOKEN_PARAM]
            ?: bodyParams[DEBUG_HTTP_TOKEN_PARAM]
        return hooks.isTokenValid(token)
    }

    private fun parseBodyParams(body: String): Map<String, String> {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) {
            return emptyMap()
        }
        return if (trimmed.startsWith("{")) {
            jsonToParamMap(JSONObject(trimmed))
        } else {
            parseQuery(trimmed)
        }
    }

    private fun jsonResponse(json: JSONObject, status: Int = 200): DebugHttpResponse =
        DebugHttpResponse(status, "application/json; charset=utf-8", json.toString(2))

    private fun errorResponse(error: Exception): DebugHttpResponse =
        jsonResponse(
            JSONObject()
                .put("ok", false)
                .put("error", error.message ?: error.javaClass.simpleName),
            500,
        )

    private fun writeResponseQuietly(output: OutputStream, response: DebugHttpResponse) {
        runCatching { writeResponse(output, response) }
    }

    private fun writeResponse(output: OutputStream, response: DebugHttpResponse) {
        val bytes = response.body.toByteArray(StandardCharsets.UTF_8)
        val reason = when (response.status) {
            200 -> "OK"
            202 -> "Accepted"
            400 -> "Bad Request"
            403 -> "Forbidden"
            404 -> "Not Found"
            409 -> "Conflict"
            else -> "Error"
        }
        val header = buildString {
            append("HTTP/1.1 ${response.status} $reason\r\n")
            append("Content-Type: ${response.contentType}\r\n")
            append("Content-Length: ${bytes.size}\r\n")
            append("Cache-Control: no-store\r\n")
            append("Connection: close\r\n")
            append("\r\n")
        }
        output.write(header.toByteArray(StandardCharsets.UTF_8))
        output.write(bytes)
        output.flush()
    }
}
