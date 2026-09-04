package dev.artplus.mobile

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.reflect.InvocationTargetException
import java.net.HttpURLConnection
import java.nio.FloatBuffer
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import android.graphics.Color as AndroidColor

/**
 * RMBG 模型管理/下载/解压/校验/推理管线与 Mask 处理（Epic v2 Phase 1 Slice 1.3）。
 *
 * 从 MainActivity 迁移而来：原 `internal fun` 成员，现顶层 `internal`。
 * - 纯函数签名未变，Activity 内删本体零 wrapper：`rebuildRmbgBackground /
 *   rmbgValidationWarning / unzipRmbgComponent / normalizeRmbgModelFile /
 *   validateRmbgComponentDir / copyDirectory / applyAlphaArrayToSource /
 *   applyMaskToSource`。
 * - 读 Activity 状态者改为显式参数，Activity 内保留原签名 wrapper
 *  （标注“重构期间保留”）委托到这里，原有调用点零改动：
 *   `tuneRmbgAlpha` 收 4 个 rmbg 百分比；`rmbgTunedForegroundRaw` 收同 4 参数；
 *   `rmbgComponentDir / findRmbgComponent` 收 filesDir；
 *   `buildRmbgCandidate / buildRmbgDebugCandidate / runRmbgAlphaMask` 收 filesDir +
 *   4 调参 + 推理锁与 runtime 存取；`runRmbgModel` 收推理锁与 runtime 存取，
 *   `synchronized(lock)` 与原 `synchronized(this)` 同监视器（wrapper 传 this）；
 *   `downloadRmbgFile` 收 isDebugBuild + 进度回调（原 runOnUiThread 改回调）；
 *   `installRmbgComponentFromModelUrl / installRmbgComponentFromInput` 收 filesDir +
 *   isDebugBuild + runtime 存取 + 进度回调；`clearInstalledRmbgComponent /
 *   installRmbgComponent / installRmbgComponentFromUrl` 收 busy 标志 + filesDir/cacheDir +
 *   ContentResolver 回调 + UI setter 回调 + runOnUi，线程模型不变（仍 Thread + runOnUi）。
 * - 与既有 `pipeline/RmbgRunner.kt`（90 行）去重：`morphRmbgAlpha /
 *   featherRmbgAlphaEdges` 只做调用，不重复定义。
 * - `DynamicRmbgRuntime`（任务名单称 OnnxSessionWrapper，实为当前本体）整体顶层化：
 *   原 `internal inner class`，现顶层 `internal class`，`createSessionPair /
 *   configureBaseOptions / run / close` 逻辑逐行等价，初始化/关闭顺序与线程模型不变。
 */

internal fun rebuildRmbgBackground(sourceIcon: Bitmap, foreground: Bitmap): Bitmap {
    val fallback = solidBitmap(
        sourceIcon.width,
        sourceIcon.height,
        estimatePlainIconBackground(sourceIcon),
    )
    return rebuildComposedIconBackground(sourceIcon, foreground, fallback)
}

internal fun rmbgValidationWarning(coverage: Double, bounds: Bounds?, cropRisk: Boolean): String {
    val coverageText = (coverage * 100.0).roundToInt()
    val boundsText = bounds?.let { "${it.width()}x${it.height()}@${it.left},${it.top}" } ?: "无"
    return "RMBG候选未通过校验，已保留: 覆盖率 ${coverageText}%，边界 $boundsText，贴边风险 ${if (cropRisk) "是" else "否"}"
}

internal fun rmbgComponentDir(filesDir: File): File = File(filesDir, RMBG_COMPONENT_DIR)

internal fun findRmbgComponent(filesDir: File): RmbgComponent? {
    val dir = rmbgComponentDir(filesDir)
    val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: return null
    val model = File(dir, RMBG_MODEL_NAME)
    if (!model.isFile || model.length() < RMBG_MIN_MODEL_BYTES) {
        return null
    }
    return RmbgComponent(dir, abi, model)
}

internal fun clearInstalledRmbgComponent(
    filesDir: File,
    isBusy: Boolean,
    isGeneratingRmbgCandidate: Boolean,
    isInstallingRmbgComponent: Boolean,
    closeRuntime: () -> Unit,
    onClearUiState: () -> Unit,
    onResult: (deleted: Boolean) -> Unit,
): Boolean {
    if (isBusy || isGeneratingRmbgCandidate || isInstallingRmbgComponent) {
        return false
    }
    runCatching { closeRuntime() }
    val targetDir = rmbgComponentDir(filesDir)
    val tmpDir = File(filesDir, "$RMBG_COMPONENT_DIR.tmp")
    val deleted = targetDir.exists() && targetDir.deleteRecursively()
    if (tmpDir.exists()) {
        tmpDir.deleteRecursively()
    }
    onClearUiState()
    onResult(deleted)
    return true
}

internal fun installRmbgComponent(
    uri: Uri,
    filesDir: File,
    isBusy: Boolean,
    isGeneratingRmbgCandidate: Boolean,
    isInstallingRmbgComponent: Boolean,
    openInput: (Uri) -> InputStream?,
    getRuntime: () -> DynamicRmbgRuntime?,
    setRuntime: (DynamicRmbgRuntime?) -> Unit,
    setInstalling: (Boolean) -> Unit,
    setStage: (String) -> Unit,
    setProgress: (Float?) -> Unit,
    setStatus: (String) -> Unit,
    setComponentStatus: (String) -> Unit,
    setLastError: (String?) -> Unit,
    runOnUi: (() -> Unit) -> Unit,
) {
    if (isBusy || isGeneratingRmbgCandidate || isInstallingRmbgComponent) {
        return
    }
    setInstalling(true)
    setStage("读取组件")
    setProgress(null)
    setStatus("RMBG组件安装中")
    Thread {
        try {
            val component = openInput(uri)?.use { input ->
                installRmbgComponentFromInput(input, filesDir, getRuntime, setRuntime)
            } ?: error("无法打开组件 ZIP")
            runOnUi {
                setComponentStatus("${System.currentTimeMillis()}")
                setLastError(null)
                setStage("安装完成")
                setProgress(1f)
                setStatus("RMBG已安装: ${component.abi}")
            }
        } catch (error: Exception) {
            runOnUi {
                setComponentStatus("${System.currentTimeMillis()}")
                setLastError("RMBG安装失败: ${error.message ?: error.javaClass.simpleName}")
                setStage("安装失败")
                setProgress(null)
                setStatus("RMBG安装失败: ${error.message ?: error.javaClass.simpleName}")
            }
        } finally {
            runOnUi {
                setInstalling(false)
                setProgress(null)
            }
        }
    }.start()
}

internal fun installRmbgComponentFromUrl(
    urlText: String,
    filesDir: File,
    cacheDir: File,
    isBusy: Boolean,
    isGeneratingRmbgCandidate: Boolean,
    isInstallingRmbgComponent: Boolean,
    isDebugBuild: Boolean,
    onSaveSettings: () -> Unit,
    getRuntime: () -> DynamicRmbgRuntime?,
    setRuntime: (DynamicRmbgRuntime?) -> Unit,
    setInstalling: (Boolean) -> Unit,
    setStage: (String) -> Unit,
    setProgress: (Float?) -> Unit,
    setStatus: (String) -> Unit,
    setComponentStatus: (String) -> Unit,
    setComponentSaveStatus: (String) -> Unit,
    setLastError: (String?) -> Unit,
    runOnUi: (() -> Unit) -> Unit,
) {
    if (isBusy || isGeneratingRmbgCandidate || isInstallingRmbgComponent) {
        return
    }
    val trimmed = urlText.trim()
    if (trimmed.isEmpty()) {
        setStatus("先填 RMBG 组件 URL")
        return
    }
    onSaveSettings()
    setInstalling(true)
    setStage("准备下载")
    setProgress(null)
    setStatus("RMBG组件下载中")
    Thread {
        val tmpDownload = File(cacheDir, "rmbg-download-${System.currentTimeMillis()}")
        try {
            val component = if (trimmed.endsWith(".zip", ignoreCase = true)) {
                downloadRmbgFile(
                    trimmed,
                    tmpDownload,
                    RMBG_MIN_COMPONENT_ZIP_BYTES,
                    "RMBG组件",
                    isDebugBuild,
                ) { stage, progress, status ->
                    runOnUi {
                        setStatus(status)
                        setStage(stage)
                        setProgress(progress)
                    }
                }
                FileInputStream(tmpDownload).use { input ->
                    installRmbgComponentFromInput(input, filesDir, getRuntime, setRuntime)
                }
            } else {
                installRmbgComponentFromModelUrl(
                    trimmed,
                    tmpDownload,
                    filesDir,
                    isDebugBuild,
                    getRuntime,
                    setRuntime,
                    onDownloadProgress = { stage, progress, status ->
                        runOnUi {
                            setStatus(status)
                            setStage(stage)
                            setProgress(progress)
                        }
                    },
                    onInstallStage = {
                        runOnUi {
                            setStage("安装模型")
                            setProgress(null)
                        }
                    },
                )
            }
            runOnUi {
                setComponentStatus("${System.currentTimeMillis()}")
                setComponentSaveStatus("已保存")
                setLastError(null)
                setStage("安装完成")
                setProgress(1f)
                setStatus("RMBG已安装: ${component.abi}")
            }
        } catch (error: Exception) {
            runOnUi {
                setComponentStatus("${System.currentTimeMillis()}")
                setLastError("RMBG安装失败: ${error.message ?: error.javaClass.simpleName}")
                setStage("安装失败")
                setProgress(null)
                setStatus("RMBG安装失败: ${error.message ?: error.javaClass.simpleName}")
            }
        } finally {
            tmpDownload.delete()
            runOnUi { setInstalling(false) }
        }
    }.start()
}

internal fun installRmbgComponentFromModelUrl(
    modelUrl: String,
    modelFile: File,
    filesDir: File,
    isDebugBuild: Boolean,
    getRuntime: () -> DynamicRmbgRuntime?,
    setRuntime: (DynamicRmbgRuntime?) -> Unit,
    onDownloadProgress: (stage: String, progress: Float?, status: String) -> Unit,
    onInstallStage: () -> Unit = {},
): RmbgComponent {
    val targetDir = rmbgComponentDir(filesDir)
    val tmpDir = File(filesDir, "$RMBG_COMPONENT_DIR.tmp")
    runCatching {
        getRuntime()?.close()
        setRuntime(null)
    }
    if (tmpDir.exists()) {
        tmpDir.deleteRecursively()
    }
    tmpDir.mkdirs()
    try {
        downloadRmbgFile(modelUrl, modelFile, RMBG_MIN_MODEL_BYTES, "RMBG模型", isDebugBuild, onDownloadProgress)
        onInstallStage()
        modelFile.copyTo(File(tmpDir, RMBG_MODEL_NAME), overwrite = true)
        validateRmbgComponentDir(tmpDir)
        if (targetDir.exists()) {
            targetDir.deleteRecursively()
        }
        if (!tmpDir.renameTo(targetDir)) {
            copyDirectory(tmpDir, targetDir)
            tmpDir.deleteRecursively()
        }
        return findRmbgComponent(filesDir)
            ?: error("缺少当前 ABI: ${Build.SUPPORTED_ABIS.joinToString()}")
    } catch (error: Exception) {
        tmpDir.deleteRecursively()
        throw error
    }
}

internal fun installRmbgComponentFromInput(
    input: InputStream,
    filesDir: File,
    getRuntime: () -> DynamicRmbgRuntime?,
    setRuntime: (DynamicRmbgRuntime?) -> Unit,
): RmbgComponent {
    val targetDir = rmbgComponentDir(filesDir)
    val tmpDir = File(filesDir, "$RMBG_COMPONENT_DIR.tmp")
    runCatching {
        getRuntime()?.close()
        setRuntime(null)
    }
    if (tmpDir.exists()) {
        tmpDir.deleteRecursively()
    }
    tmpDir.mkdirs()
    try {
        unzipRmbgComponent(input, tmpDir)
        normalizeRmbgModelFile(tmpDir)
        validateRmbgComponentDir(tmpDir)
        if (targetDir.exists()) {
            targetDir.deleteRecursively()
        }
        if (!tmpDir.renameTo(targetDir)) {
            copyDirectory(tmpDir, targetDir)
            tmpDir.deleteRecursively()
        }
        return findRmbgComponent(filesDir)
            ?: error("缺少当前 ABI: ${Build.SUPPORTED_ABIS.joinToString()}")
    } catch (error: Exception) {
        tmpDir.deleteRecursively()
        throw error
    }
}

internal fun downloadRmbgFile(
    urlText: String,
    target: File,
    minBytes: Long,
    label: String,
    isDebugBuild: Boolean,
    onProgress: (stage: String, progress: Float?, status: String) -> Unit,
) {
    val url = validatedRemoteUrl(urlText, label, isDebugBuild)
    val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = RMBG_DOWNLOAD_CONNECT_TIMEOUT_MS
        readTimeout = RMBG_DOWNLOAD_READ_TIMEOUT_MS
        url.userInfo?.takeIf { it.isNotBlank() }?.let { userInfo ->
            setRequestProperty(
                "Authorization",
                "Basic ${Base64.encodeToString(userInfo.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)}",
            )
        }
    }
    try {
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }
        if (connection.responseCode !in 200..299) {
            val message = stream.bufferedReader().use { it.readText() }.take(160)
            error("HTTP ${connection.responseCode}: $message")
        }
        val totalBytes = connection.contentLengthLong.takeIf { it > 0L }
        if (totalBytes != null && totalBytes > RMBG_MAX_DOWNLOAD_BYTES) {
            error("$label 超过最大下载大小")
        }
        var downloaded = 0L
        var nextReportAt = 0L
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        target.parentFile?.mkdirs()
        onProgress("$label 下载中", totalBytes?.let { 0f }, "$label 下载中")
        stream.use { input ->
            FileOutputStream(target).use { output ->
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) {
                        break
                    }
                    output.write(buffer, 0, read)
                    downloaded += read.toLong()
                    if (downloaded > RMBG_MAX_DOWNLOAD_BYTES) {
                        error("$label 超过最大下载大小")
                    }
                    if (downloaded >= nextReportAt) {
                        val progress = totalBytes?.let { downloaded.toFloat() / it.toFloat() }
                        val text = totalBytes?.let { total ->
                            val percent = ((progress ?: 0f) * 100f).roundToInt().coerceIn(0, 100)
                            "$label $percent% · ${downloaded / 1024 / 1024}/${total / 1024 / 1024}MB"
                        } ?: "$label ${downloaded / 1024 / 1024}MB"
                        onProgress(text, progress?.coerceIn(0f, 1f), text)
                        nextReportAt = downloaded + 2L * 1024L * 1024L
                    }
                }
            }
        }
        if (target.length() < minBytes) {
            error("$label 过小")
        }
    } finally {
        connection.disconnect()
    }
}

internal fun unzipRmbgComponent(input: InputStream, targetDir: File) {
    val canonicalTarget = targetDir.canonicalFile
    var totalWritten = 0L
    var fileCount = 0
    ZipInputStream(input).use { zip ->
        while (true) {
            val entry = zip.nextEntry ?: break
            val entryName = entry.name.replace('\\', '/').trimStart('/')
            if (entryName.isBlank() || entryName.contains("..")) {
                zip.closeEntry()
                continue
            }
            fileCount += 1
            if (fileCount > RMBG_MAX_COMPONENT_ZIP_ENTRIES) {
                error("RMBG组件压缩包文件过多")
            }
            val outFile = File(targetDir, entryName)
            val canonicalOut = outFile.canonicalFile
            if (!canonicalOut.path.startsWith(canonicalTarget.path + File.separator)) {
                error("RMBG组件压缩包路径非法")
            }
            if (entry.isDirectory) {
                canonicalOut.mkdirs()
            } else {
                canonicalOut.parentFile?.mkdirs()
                FileOutputStream(canonicalOut).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = zip.read(buffer)
                        if (read < 0) {
                            break
                        }
                        totalWritten += read.toLong()
                        if (totalWritten > RMBG_MAX_COMPONENT_ZIP_UNPACK_BYTES) {
                            error("RMBG组件压缩包过大")
                        }
                        output.write(buffer, 0, read)
                    }
                }
            }
            zip.closeEntry()
        }
    }
}

internal fun normalizeRmbgModelFile(dir: File) {
    val target = File(dir, RMBG_MODEL_NAME)
    if (target.isFile) {
        return
    }
    val candidate = listOf(
        File(dir, "onnx/model.onnx"),
        File(dir, "model.onnx"),
    ).firstOrNull { it.isFile && it.length() >= RMBG_MIN_MODEL_BYTES }
    candidate?.copyTo(target, overwrite = true)
}

internal fun validateRmbgComponentDir(dir: File) {
    val model = File(dir, RMBG_MODEL_NAME)
    if (!model.isFile || model.length() < RMBG_MIN_MODEL_BYTES) {
        error("缺少 $RMBG_MODEL_NAME")
    }
}

internal fun copyDirectory(source: File, target: File) {
    if (source.isDirectory) {
        target.mkdirs()
        source.listFiles().orEmpty().forEach { child ->
            copyDirectory(child, File(target, child.name))
        }
    } else {
        target.parentFile?.mkdirs()
        source.inputStream().use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
    }
}

internal class DynamicRmbgRuntime(
    private val component: RmbgComponent,
) : AutoCloseable {
    var activeBackend: RmbgInferenceBackend = RmbgInferenceBackend.Cpu
        private set

    private val environmentClass: Class<*>
    private val environment: Any
    private var sessionOptions: Any? = null
    private var session: Any? = null
    private val tensorClass: Class<*>
    private val onnxTensorClass: Class<*>
    private val closeMethod = AutoCloseable::class.java.getMethod("close")

    init {
        val classLoader = MainActivity::class.java.classLoader ?: ClassLoader.getSystemClassLoader()
        environmentClass = classLoader.loadClass("ai.onnxruntime.OrtEnvironment")
        val sessionOptionsClass = classLoader.loadClass("ai.onnxruntime.OrtSession\$SessionOptions")
        onnxTensorClass = classLoader.loadClass("ai.onnxruntime.OnnxTensor")
        tensorClass = onnxTensorClass
        environment = environmentClass.getMethod("getEnvironment").invoke(null)
            ?: error("无法初始化 ONNX Runtime 环境")
        val created = createSessionPair(sessionOptionsClass)
        sessionOptions = created.first
        session = created.second
        activeBackend = RmbgInferenceBackend.Cpu
    }

    private fun createSessionPair(sessionOptionsClass: Class<*>): Pair<Any, Any> {
        val options = sessionOptionsClass.getConstructor().newInstance()
        try {
            configureBaseOptions(sessionOptionsClass, options)
            val createdSession = environmentClass
                .getMethod("createSession", String::class.java, sessionOptionsClass)
                .invoke(environment, component.model.absolutePath, options)
                ?: error("无法创建 RMBG ONNX 会话")
            return options to createdSession
        } catch (error: InvocationTargetException) {
            runCatching { closeMethod.invoke(options) }
            throw error.targetException ?: error
        } catch (error: Throwable) {
            runCatching { closeMethod.invoke(options) }
            throw error
        }
    }

    private fun configureBaseOptions(sessionOptionsClass: Class<*>, options: Any) {
        runCatching { sessionOptionsClass.getMethod("setMemoryPatternOptimization", Boolean::class.javaPrimitiveType).invoke(options, false) }
        runCatching { sessionOptionsClass.getMethod("setCPUArenaAllocator", Boolean::class.javaPrimitiveType).invoke(options, false) }
        runCatching { sessionOptionsClass.getMethod("setIntraOpNumThreads", Int::class.javaPrimitiveType).invoke(options, 1) }
        runCatching { sessionOptionsClass.getMethod("setInterOpNumThreads", Int::class.javaPrimitiveType).invoke(options, 1) }
    }

    @Suppress("UNCHECKED_CAST")
    fun run(input: FloatBuffer, shape: LongArray): FloatArray {
        val activeSession = session ?: error("RMBG ONNX 会话未初始化")
        val tensor = tensorClass
            .getMethod("createTensor", environmentClass, FloatBuffer::class.java, LongArray::class.java)
            .invoke(null, environment, input, shape)
        try {
            val inputNames = activeSession.javaClass.getMethod("getInputNames").invoke(activeSession) as Set<String>
            val feeds = mapOf(inputNames.first() to tensor)
            val runMethod = activeSession.javaClass.getMethod("run", Map::class.java)
            val result = try {
                runMethod.invoke(activeSession, feeds)
            } catch (error: InvocationTargetException) {
                throw error.targetException ?: error
            }
            try {
                val outputTensor = result.javaClass.getMethod("get", Int::class.javaPrimitiveType).invoke(result, 0)
                val buffer = onnxTensorClass.getMethod("getFloatBuffer").invoke(outputTensor) as FloatBuffer
                buffer.rewind()
                return FloatArray(buffer.remaining()).also { buffer.get(it) }
            } finally {
                closeMethod.invoke(result)
            }
        } finally {
            closeMethod.invoke(tensor)
        }
    }

    override fun close() {
        runCatching { session?.let { closeMethod.invoke(it) } }
        runCatching { sessionOptions?.let { closeMethod.invoke(it) } }
        session = null
        sessionOptions = null
    }
}

internal fun runRmbgModel(
    component: RmbgComponent,
    input: FloatBuffer,
    shape: LongArray,
    lock: Any,
    getRuntime: () -> DynamicRmbgRuntime?,
    setRuntime: (DynamicRmbgRuntime?) -> Unit,
): RmbgModelOutput =
    synchronized(lock) {
        runCatching { getRuntime()?.close() }
        setRuntime(null)
        val startedAt = System.nanoTime()
        var runtime: DynamicRmbgRuntime? = null
        try {
            input.rewind()
            runtime = DynamicRmbgRuntime(component)
            setRuntime(runtime)
            val output = runtime.run(input, shape)
            RmbgModelOutput(
                output = output,
                report = RmbgInferenceReport(
                    actualBackend = runtime.activeBackend,
                    elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt),
                ),
            )
        } catch (error: Throwable) {
            runCatching { runtime?.close() }
            setRuntime(null)
            throw error
        } finally {
            runCatching { runtime?.close() }
            setRuntime(null)
        }
    }

internal fun runRmbgAlphaMask(
    sourceIcon: Bitmap,
    component: RmbgComponent,
    lock: Any,
    getRuntime: () -> DynamicRmbgRuntime?,
    setRuntime: (DynamicRmbgRuntime?) -> Unit,
): RmbgMaskResult {
    val inputSize = DEFAULT_RMBG_INPUT_SIZE
    val modelInput = resizeBitmap(sourceIcon, inputSize, inputSize)
    val inputPixels = IntArray(inputSize * inputSize)
    modelInput.getPixels(inputPixels, 0, inputSize, 0, 0, inputSize, inputSize)
    val input = FloatBuffer.allocate(inputSize * inputSize * 3)
    for (channel in 0..2) {
        val mean = RMBG_NORMALIZE_MEAN[channel]
        val std = RMBG_NORMALIZE_STD[channel]
        for (pixel in inputPixels) {
            val value = when (channel) {
                0 -> AndroidColor.red(pixel)
                1 -> AndroidColor.green(pixel)
                else -> AndroidColor.blue(pixel)
            }
            input.put(((value / 255.0f) - mean) / std)
        }
    }
    input.rewind()

    val modelOutput = runRmbgModel(component, input, longArrayOf(1L, 3L, inputSize.toLong(), inputSize.toLong()), lock, getRuntime, setRuntime)
    val output = modelOutput.output
    if (output.isEmpty()) {
        error("RMBG输出为空")
    }
    val outputSide = kotlin.math.sqrt(output.size.toDouble()).roundToInt()
    if (outputSide <= 0 || outputSide * outputSide != output.size) {
        error("RMBG输出尺寸异常: ${output.size}")
    }
    var min = Float.POSITIVE_INFINITY
    var max = Float.NEGATIVE_INFINITY
    output.forEach { value ->
        if (value < min) min = value
        if (value > max) max = value
    }
    val range = max - min
    if (range <= 0.000001f) {
        error("RMBG输出无有效 Alpha 范围")
    }
    val scaledPixels = IntArray(sourceIcon.width * sourceIcon.height)
    val scaleX = outputSide.toFloat() / sourceIcon.width.toFloat()
    val scaleY = outputSide.toFloat() / sourceIcon.height.toFloat()
    for (y in 0 until sourceIcon.height) {
        val sourceY = ((y + 0.5f) * scaleY - 0.5f).coerceIn(0f, (outputSide - 1).toFloat())
        val y0 = sourceY.toInt().coerceIn(0, outputSide - 1)
        val y1 = (y0 + 1).coerceIn(0, outputSide - 1)
        val yRatio = sourceY - y0.toFloat()
        val row0 = y0 * outputSide
        val row1 = y1 * outputSide
        val outOffset = y * sourceIcon.width
        for (x in 0 until sourceIcon.width) {
            val sourceX = ((x + 0.5f) * scaleX - 0.5f).coerceIn(0f, (outputSide - 1).toFloat())
            val x0 = sourceX.toInt().coerceIn(0, outputSide - 1)
            val x1 = (x0 + 1).coerceIn(0, outputSide - 1)
            val xRatio = sourceX - x0.toFloat()
            val top = output[row0 + x0] * (1f - xRatio) + output[row0 + x1] * xRatio
            val bottom = output[row1 + x0] * (1f - xRatio) + output[row1 + x1] * xRatio
            val value = top * (1f - yRatio) + bottom * yRatio
            scaledPixels[outOffset + x] = (((value - min) / range) * 255.0f)
                .roundToInt()
                .coerceIn(0, 255)
        }
    }
    return RmbgMaskResult(alpha = scaledPixels, report = modelOutput.report)
}

internal fun tuneRmbgAlpha(
    alpha: IntArray,
    width: Int,
    height: Int,
    rmbgAlphaStrengthPercent: Int,
    rmbgEdgeAdjustPercent: Int,
    rmbgEdgeFeatherPercent: Int,
    rmbgWeakAlphaKeepPercent: Int,
): IntArray {
    if (alpha.size != width * height || width <= 0 || height <= 0) {
        return alpha.copyOf()
    }
    var current = alpha.copyOf()
    val strength = rmbgAlphaStrengthPercent.coerceIn(
        MIN_RMBG_ALPHA_STRENGTH_PERCENT,
        MAX_RMBG_ALPHA_STRENGTH_PERCENT,
    )
    if (strength != DEFAULT_RMBG_ALPHA_STRENGTH_PERCENT) {
        val gamma = DEFAULT_RMBG_ALPHA_STRENGTH_PERCENT.toDouble() / strength.toDouble()
        for (i in current.indices) {
            val normalized = current[i].coerceIn(0, 255).toDouble() / 255.0
            current[i] = (normalized.pow(gamma) * 255.0)
                .roundToInt()
                .coerceIn(0, 255)
        }
    }

    val adjust = rmbgEdgeAdjustPercent.coerceIn(
        MIN_RMBG_EDGE_ADJUST_PERCENT,
        MAX_RMBG_EDGE_ADJUST_PERCENT,
    ) - DEFAULT_RMBG_EDGE_ADJUST_PERCENT
    if (adjust != 0) {
        val radius = ((abs(adjust) / 50.0) * RMBG_EDGE_ADJUST_MAX_RADIUS)
            .roundToInt()
            .coerceIn(1, RMBG_EDGE_ADJUST_MAX_RADIUS)
        val morphed = morphRmbgAlpha(current, width, height, expand = adjust > 0, radius = radius)
        val blend = (abs(adjust).toDouble() / DEFAULT_RMBG_EDGE_ADJUST_PERCENT.toDouble())
            .coerceIn(0.0, 1.0)
        for (i in current.indices) {
            current[i] = (current[i] * (1.0 - blend) + morphed[i] * blend)
                .roundToInt()
                .coerceIn(0, 255)
        }
    }

    val feather = ratioPercent(rmbgEdgeFeatherPercent.coerceIn(
        MIN_RMBG_EDGE_FEATHER_PERCENT,
        MAX_RMBG_EDGE_FEATHER_PERCENT,
    ))
    if (feather > 0.0) {
        val radius = if (rmbgEdgeFeatherPercent >= 70) 2 else 1
        current = featherRmbgAlphaEdges(current, width, height, strength = feather, radius = radius)
    }

    val weakKeep = ratioPercent(rmbgWeakAlphaKeepPercent.coerceIn(
        MIN_RMBG_WEAK_ALPHA_KEEP_PERCENT,
        MAX_RMBG_WEAK_ALPHA_KEEP_PERCENT,
    ))
    if (weakKeep < 1.0) {
        val lowCut = lerpDouble(RMBG_WEAK_ALPHA_MAX_CUT.toDouble(), 0.0, weakKeep)
            .roundToInt()
            .coerceIn(0, 254)
        if (lowCut > 0) {
            val range = (255 - lowCut).coerceAtLeast(1)
            for (i in current.indices) {
                val value = current[i].coerceIn(0, 255)
                current[i] = if (value <= lowCut) {
                    0
                } else {
                    (((value - lowCut).toDouble() / range.toDouble()) * 255.0)
                        .roundToInt()
                        .coerceIn(0, 255)
                }
            }
        }
    }
    return current
}

internal fun applyAlphaArrayToSource(source: Bitmap, alpha: IntArray): Bitmap {
    val width = source.width
    val height = source.height
    val pixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    val outPixels = IntArray(pixels.size)
    for (i in pixels.indices) {
        val outAlpha = alpha.getOrElse(i) { 0 }.coerceIn(0, 255)
        outPixels[i] = if (outAlpha <= LOCAL_ALPHA_VISIBLE_THRESHOLD) {
            AndroidColor.TRANSPARENT
        } else {
            AndroidColor.argb(
                outAlpha,
                AndroidColor.red(pixels[i]),
                AndroidColor.green(pixels[i]),
                AndroidColor.blue(pixels[i]),
            )
        }
    }
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return repairTransparentEdgeColors(out)
}

internal fun applyMaskToSource(pixels: IntArray, width: Int, height: Int, mask: BooleanArray): Bitmap {
    val outPixels = IntArray(pixels.size)
    for (i in pixels.indices) {
        val pixel = pixels[i]
        outPixels[i] = if (mask[i]) {
            AndroidColor.argb(
                255,
                AndroidColor.red(pixel),
                AndroidColor.green(pixel),
                AndroidColor.blue(pixel),
            )
        } else {
            AndroidColor.TRANSPARENT
        }
    }
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, width, 0, 0, width, height)
    return repairTransparentEdgeColors(out)
}

internal fun buildRmbgCandidate(
    sourceIcon: Bitmap,
    filesDir: File,
    rmbgAlphaStrengthPercent: Int,
    rmbgEdgeAdjustPercent: Int,
    rmbgEdgeFeatherPercent: Int,
    rmbgWeakAlphaKeepPercent: Int,
    lock: Any,
    getRuntime: () -> DynamicRmbgRuntime?,
    setRuntime: (DynamicRmbgRuntime?) -> Unit,
): CandidateBuildResult? {
    val component = findRmbgComponent(filesDir) ?: return null
    return runCatching {
        val mask = runRmbgAlphaMask(sourceIcon, component, lock, getRuntime, setRuntime)
        val tunedAlpha = tuneRmbgAlpha(
            mask.alpha,
            sourceIcon.width,
            sourceIcon.height,
            rmbgAlphaStrengthPercent,
            rmbgEdgeAdjustPercent,
            rmbgEdgeFeatherPercent,
            rmbgWeakAlphaKeepPercent,
        )
        val foreground = applyAlphaArrayToSource(sourceIcon, tunedAlpha)
        val cleanBackground = rebuildRmbgBackground(sourceIcon, foreground)
        val coverage = meaningfulAlphaCoverage(foreground)
        val bounds = meaningfulAlphaBounds(foreground)
        val cropRisk = bounds?.let { hasAutoCropRisk(it, foreground.width, foreground.height) } ?: true
        val manualUsable = coverage in RMBG_MIN_MANUAL_COVERAGE..RMBG_MAX_MANUAL_COVERAGE &&
            bounds != null &&
            !cropRisk
        val validationWarning = if (manualUsable) null else rmbgValidationWarning(coverage, bounds, cropRisk)
        CandidateBuildResult(
            candidate = IconCandidate(
                recfgRaw = foreground,
                recbg = cleanBackground,
                monochromeRaw = foreground,
                rmbgSourceRaw = sourceIcon,
                rmbgAlphaRaw = mask.alpha,
                isLocal = false,
            ),
            autoUsable = manualUsable && coverage in RMBG_MIN_AUTO_COVERAGE..RMBG_MAX_AUTO_COVERAGE,
            coverage = coverage,
            rmbgInference = mask.report,
            manualUsable = manualUsable,
            validationWarning = validationWarning,
        )
    }.getOrElse { throw it }
}

internal fun buildRmbgDebugCandidate(
    sourceIcon: Bitmap,
    filesDir: File,
    rmbgAlphaStrengthPercent: Int,
    rmbgEdgeAdjustPercent: Int,
    rmbgEdgeFeatherPercent: Int,
    rmbgWeakAlphaKeepPercent: Int,
    lock: Any,
    getRuntime: () -> DynamicRmbgRuntime?,
    setRuntime: (DynamicRmbgRuntime?) -> Unit,
): RmbgDebugCandidate {
    val component = findRmbgComponent(filesDir) ?: error("未安装 RMBG 组件 ZIP")
    val mask = runRmbgAlphaMask(sourceIcon, component, lock, getRuntime, setRuntime)
    val tunedAlpha = tuneRmbgAlpha(
        mask.alpha,
        sourceIcon.width,
        sourceIcon.height,
        rmbgAlphaStrengthPercent,
        rmbgEdgeAdjustPercent,
        rmbgEdgeFeatherPercent,
        rmbgWeakAlphaKeepPercent,
    )
    val foreground = applyAlphaArrayToSource(sourceIcon, tunedAlpha)
    val cleanBackground = rebuildRmbgBackground(sourceIcon, foreground)
    val coverage = meaningfulAlphaCoverage(foreground)
    val bounds = meaningfulAlphaBounds(foreground)
    val cropRisk = bounds?.let { hasAutoCropRisk(it, foreground.width, foreground.height) } ?: true
    val manualUsable = coverage in RMBG_MIN_MANUAL_COVERAGE..RMBG_MAX_MANUAL_COVERAGE &&
        bounds != null &&
        !cropRisk
    val candidate = IconCandidate(
        recfgRaw = foreground,
        recbg = cleanBackground,
        monochromeRaw = foreground,
        rmbgSourceRaw = sourceIcon,
        rmbgAlphaRaw = mask.alpha,
        isLocal = false,
    )
    val result = CandidateBuildResult(
        candidate = candidate,
        autoUsable = manualUsable && coverage in RMBG_MIN_AUTO_COVERAGE..RMBG_MAX_AUTO_COVERAGE,
        coverage = coverage,
        rmbgInference = mask.report,
        manualUsable = manualUsable,
        validationWarning = if (manualUsable) null else rmbgValidationWarning(coverage, bounds, cropRisk),
    )
    return RmbgDebugCandidate(
        foreground = foreground,
        result = result,
        coverage = coverage,
        boundsText = bounds?.let { "${it.width()}x${it.height()}@${it.left},${it.top}" } ?: "无",
        cropRisk = cropRisk,
        manualUsable = manualUsable,
        inference = mask.report,
    )
}

internal fun rmbgTunedForegroundRaw(
    candidate: IconCandidate,
    rmbgAlphaStrengthPercent: Int,
    rmbgEdgeAdjustPercent: Int,
    rmbgEdgeFeatherPercent: Int,
    rmbgWeakAlphaKeepPercent: Int,
): Bitmap? {
    val source = candidate.rmbgSourceRaw ?: return null
    val alpha = candidate.rmbgAlphaRaw ?: return null
    if (alpha.size != source.width * source.height) {
        return null
    }
    return applyAlphaArrayToSource(
        source = source,
        alpha = tuneRmbgAlpha(
            alpha,
            source.width,
            source.height,
            rmbgAlphaStrengthPercent,
            rmbgEdgeAdjustPercent,
            rmbgEdgeFeatherPercent,
            rmbgWeakAlphaKeepPercent,
        ),
    )
}
