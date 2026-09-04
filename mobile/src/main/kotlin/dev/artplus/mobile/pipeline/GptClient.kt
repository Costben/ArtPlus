package dev.artplus.mobile

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.util.Base64
import com.caverock.androidsvg.SVG
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt
import org.json.JSONArray
import org.json.JSONObject
import android.graphics.Color as AndroidColor

/**
 * GPT 图像管线客户端（P4 拆分）：Responses/Images 双模式 + HTTP + 自定义图编解码 + 提示词。
 *
 * 从 MainActivity 迁移而来：原 `private fun`，现 `internal`。
 * - `GptImageMode`/`GptPromptPreset` 整枚举提升（原 private enum，自包含；UI 引用同包零改动）。
 * - 读 Activity 状态者改为显式参数：`gptEditImage` 收 mode + modelId + baseUrl + apiKey
 *   + isDebugBuild；`responsesEditImage`/`imagesEditImage` 收 modelId + baseUrl + apiKey
 *   + isDebugBuild；`postJson`/`postBytes`/`downloadBytes` 收 apiKey + isDebugBuild；
 *   `validatedRemoteUrl` 收 isDebugBuild（原直读 applicationInfo）；
 *   `extractImageBytes`/`findImageBytes`/`decodeImageReference` 透传 apiKey + isDebugBuild；
 *   `loadCustomImageBitmap` 收 ContentResolver；
 *   `activeGptForegroundPrompt` 收 customPrompt + preset + subjectPercent。
 */

internal enum class GptImageMode(val value: String, val label: String) {
    Responses("responses", "Codex Image Gen"),
    Images("images", "API 调用");

    companion object {
        fun fromValue(value: String?): GptImageMode =
            entries.firstOrNull { it.value == value } ?: Responses
    }
}

internal enum class GptPromptPreset(
    val value: String,
    val label: String,
    val summary: String,
    val foregroundPrompt: String,
) {
    Default(
        "default",
        "默认",
        "保留主体颜色和细节",
        "Keep only the app icon main subject/logo. Remove the original background. " +
            "Return the remaining subject/logo on a transparent background. " +
            "Do not add any new circle, glow, outline, shadow, halo, plate, or filled backdrop behind the subject. " +
            "Preserve the subject shape, position, colors, face details, highlights, and internal shading.",
    ),
    StableCutout(
        "stable_cutout",
        "镂空稳定",
        "优先保留孔洞和负形",
        "Extract only the visible foreground subject/logo from the app icon with a precise alpha mask. " +
            "Preserve all cutouts, counters, holes, transparent gaps, negative-space shapes, inner openings, and thin strokes exactly as in the source. " +
            "Do not fill enclosed holes or bridge gaps. Do not invent a backing plate, outline, halo, shadow, glow, circle, rounded square, or extra background. " +
            "Keep antialiasing on the true subject edge and preserve the original colors, gradients, highlights, shadows, and internal details of the subject.",
    ),
    CleanLayers(
        "clean_layers",
        "干净分层",
        "主体与背景分离更强",
        "Separate the app icon into a clean foreground subject/logo only. " +
            "Remove every background plate, wallpaper, rounded square, circle, glow, halo, cast shadow, and decorative backdrop. " +
            "Keep the subject/logo centered and preserve its original colors, gradients, highlights, and internal shading without redrawing it.",
    ),
    Custom(
        "custom",
        "自定义",
        "使用下面输入的前景提示词",
        "",
    );

    companion object {
        fun fromValue(value: String?): GptPromptPreset =
            entries.firstOrNull { it.value == value } ?: StableCutout
    }
}

/**
 * 预览会话（整类提升，原 MainActivity private data class，自包含；
 * customCandidateForPreview 签名依赖，P4 前置）。
 */
internal data class GenerationSession(
    val packageName: String,
    val outDir: java.io.File,
    val sourceIcon: Bitmap,
    val baseRecfg: Bitmap,
    val baseRecbg: Bitmap,
    val monochromeRaw: Bitmap?,
    val candidates: Map<PreviewChoice, IconCandidate>,
    val customForegrounds: Map<PreviewMode, Bitmap> = emptyMap(),
    val customBackgrounds: Map<PreviewMode, Bitmap> = emptyMap(),
    val autoLocalChoice: PreviewChoice,
    val canRebuildLocalCandidates: Boolean = true,
)

internal enum class PreviewMode(val label: String) {
    NormalLight("标准亮色"),
    NormalDark("标准暗色"),
    MonochromeLight("单色亮色"),
    MonochromeDark("单色暗色"),
}

internal fun gptEditImage(
    source: Bitmap,
    prompt: String,
    background: String,
    mode: GptImageMode,
    modelId: String,
    baseUrl: String,
    apiKey: String,
    isDebugBuild: Boolean,
): Bitmap =
    when (mode) {
        GptImageMode.Responses -> responsesEditImage(source, prompt, background, modelId, baseUrl, apiKey, isDebugBuild)
        GptImageMode.Images -> imagesEditImage(source, prompt, background, modelId, baseUrl, apiKey, isDebugBuild)
    }

internal fun responsesEditImage(
    source: Bitmap,
    prompt: String,
    background: String,
    modelId: String,
    baseUrl: String,
    apiKey: String,
    isDebugBuild: Boolean,
): Bitmap {
    val model = modelId.trim().ifBlank { GPT_RESPONSE_MODEL }
    val body = JSONObject()
        .put("model", model)
        .put(
            "input",
            JSONArray().put(
                JSONObject()
                    .put("role", "user")
                    .put(
                        "content",
                        JSONArray()
                            .put(JSONObject().put("type", "input_text").put("text", prompt))
                            .put(
                                JSONObject()
                                    .put("type", "input_image")
                                    .put("image_url", bitmapToDataUrl(source)),
                            ),
                    ),
            ),
        )
        .put(
            "tools",
            JSONArray().put(
                JSONObject()
                    .put("type", "image_generation")
                    .put("size", "auto")
                    .put("quality", GPT_IMAGE_QUALITY)
                    .put("background", background)
                    .put("output_format", "png"),
            ),
        )
        .put("tool_choice", JSONObject().put("type", "image_generation"))
        .put("stream", true)

    val response = postJson(normalizeResponsesUrl(baseUrl), body, apiKey, isDebugBuild)
    val parsed = if (response.trimStart().startsWith("data:") || response.trimStart().startsWith("event:")) {
        parseResponsesStream(response)
    } else {
        JSONObject(response)
    }
    return decodeBitmap(extractImageBytes(parsed, apiKey, isDebugBuild))
}

internal fun imagesEditImage(
    source: Bitmap,
    prompt: String,
    background: String,
    modelId: String,
    baseUrl: String,
    apiKey: String,
    isDebugBuild: Boolean,
): Bitmap {
    val boundary = "----ArtPlusMobile${UUID.randomUUID().toString().replace("-", "")}"
    val pngBytes = bitmapToPngBytes(source)
    val body = ByteArrayOutputStream()

    fun field(name: String, value: String) {
        body.writeString("--$boundary\r\n")
        body.writeString("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
        body.writeString(value)
        body.writeString("\r\n")
    }

    field("model", modelId.trim().ifBlank { GPT_IMAGE_MODEL })
    field("prompt", prompt)
    field("size", GPT_IMAGE_SIZE)
    field("quality", GPT_IMAGE_QUALITY)
    field("background", background)
    field("output_format", "png")
    body.writeString("--$boundary\r\n")
    body.writeString("Content-Disposition: form-data; name=\"image\"; filename=\"artplus_source_icon.png\"\r\n")
    body.writeString("Content-Type: image/png\r\n\r\n")
    body.write(pngBytes)
    body.writeString("\r\n--$boundary--\r\n")

    val response = postBytes(
        urlText = normalizeImagesEditUrl(baseUrl),
        body = body.toByteArray(),
        contentType = "multipart/form-data; boundary=$boundary",
        apiKey = apiKey,
        isDebugBuild = isDebugBuild,
    )
    return decodeBitmap(extractImageBytes(JSONObject(response), apiKey, isDebugBuild))
}

internal fun postJson(urlText: String, body: JSONObject, apiKey: String, isDebugBuild: Boolean): String =
    postBytes(urlText, body.toString().toByteArray(Charsets.UTF_8), "application/json", accept = "text/event-stream, application/json", apiKey = apiKey, isDebugBuild = isDebugBuild)

internal fun postBytes(
    urlText: String,
    body: ByteArray,
    contentType: String,
    accept: String = "application/json",
    apiKey: String,
    isDebugBuild: Boolean,
): String {
    val url = validatedRemoteUrl(urlText, "AI", isDebugBuild)
    val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = GPT_CONNECT_TIMEOUT_MS
        readTimeout = GPT_READ_TIMEOUT_MS
        doOutput = true
        setRequestProperty("Accept", accept)
        setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
        setRequestProperty("Content-Type", contentType)
        setRequestProperty("Content-Length", body.size.toString())
    }
    try {
        connection.outputStream.use { it.write(body) }
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }
        val text = stream.bufferedReader().use { it.readText() }
        if (connection.responseCode !in 200..299) {
            error("AI HTTP ${connection.responseCode}: ${text.take(300)}")
        }
        return text
    } finally {
        connection.disconnect()
    }
}

internal fun downloadBytes(urlText: String, apiKey: String, isDebugBuild: Boolean): ByteArray {
    val url = validatedRemoteUrl(urlText, "AI图片", isDebugBuild)
    val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = GPT_CONNECT_TIMEOUT_MS
        readTimeout = GPT_READ_TIMEOUT_MS
        if (apiKey.trim().isNotEmpty()) {
            setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
        }
    }
    try {
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }
        val bytes = stream.use { it.readBytes() }
        if (connection.responseCode !in 200..299) {
            error("下载AI图片失败 HTTP ${connection.responseCode}: ${String(bytes).take(300)}")
        }
        return bytes
    } finally {
        connection.disconnect()
    }
}

internal fun validatedRemoteUrl(urlText: String, label: String, isDebugBuild: Boolean): URL {
    val url = URL(urlText)
    val protocol = url.protocol.lowercase(Locale.US)
    if (protocol != "http" && protocol != "https") {
        error("$label URL 只支持 HTTP/HTTPS")
    }
    if (protocol == "http" && !isDebugBuild) {
        error("$label URL 在正式版中必须使用 HTTPS")
    }
    return url
}

internal fun parseResponsesStream(text: String): JSONObject {
    val output = JSONArray()
    var response: JSONObject? = null
    for (block in text.split("\n\n")) {
        val data = block.lineSequence()
            .map { it.trimEnd() }
            .filter { it.startsWith("data:") }
            .joinToString("\n") { it.removePrefix("data:").trimStart() }
            .trim()
        if (data.isEmpty() || data == "[DONE]") {
            continue
        }
        val event = runCatching { JSONObject(data) }.getOrNull() ?: continue
        event.optJSONObject("response")?.let {
            response = it
            val existing = it.optJSONArray("output")
            if (existing != null) {
                for (i in 0 until existing.length()) {
                    output.put(existing.get(i))
                }
            }
        }
        val item = event.optJSONObject("item")
        if (
            item != null &&
            (event.optString("type") == "response.output_item.done" ||
                event.optString("type") == "response.output_item.added")
        ) {
            output.put(item)
        }
        if (event.optString("type") == "response.image_generation_call.partial_image") {
            val partial = event.optString("partial_image_b64")
            if (partial.isNotBlank()) {
                output.put(
                    JSONObject()
                        .put("type", "image_generation_call")
                        .put("image_base64", partial),
                )
            }
        }
    }
    return (response ?: JSONObject()).put("output", output)
}

internal fun extractImageBytes(json: JSONObject, apiKey: String, isDebugBuild: Boolean): ByteArray {
    json.optJSONArray("output")?.let { output ->
        findImageBytes(output, apiKey, isDebugBuild)?.let { return it }
    }
    json.optJSONArray("data")?.let { data ->
        findImageBytes(data, apiKey, isDebugBuild)?.let { return it }
    }
    findImageBytes(JSONArray().put(json), apiKey, isDebugBuild)?.let { return it }
    error("AI响应没有图片数据")
}

internal fun findImageBytes(items: JSONArray, apiKey: String, isDebugBuild: Boolean): ByteArray? {
    for (i in 0 until items.length()) {
        val item = items.optJSONObject(i) ?: continue
        decodeImageReference(item.opt("b64_json"), apiKey, isDebugBuild)?.let { return it }
        decodeImageReference(item.opt("b64"), apiKey, isDebugBuild)?.let { return it }
        decodeImageReference(item.opt("image_base64"), apiKey, isDebugBuild)?.let { return it }
        decodeImageReference(item.opt("base64"), apiKey, isDebugBuild)?.let { return it }
        decodeImageReference(item.opt("result"), apiKey, isDebugBuild)?.let { return it }
        decodeImageReference(item.opt("url"), apiKey, isDebugBuild)?.let { return it }
        decodeImageReference(item.opt("imageUrl"), apiKey, isDebugBuild)?.let { return it }
        decodeImageReference(item.opt("remoteImageUrl"), apiKey, isDebugBuild)?.let { return it }
        val imageUrl = item.optJSONObject("image_url")
        if (imageUrl != null) {
            decodeImageReference(imageUrl.opt("url"), apiKey, isDebugBuild)?.let { return it }
        }
    }
    return null
}

internal fun decodeImageReference(value: Any?, apiKey: String, isDebugBuild: Boolean): ByteArray? {
    val text = (value as? String)?.trim().orEmpty()
    if (text.isEmpty()) {
        return null
    }
    if (text.startsWith("http://") || text.startsWith("https://")) {
        return downloadBytes(text, apiKey, isDebugBuild)
    }
    val b64 = if (text.startsWith("data:image/")) {
        text.substringAfter("base64,", "")
    } else {
        text
    }.replace("\\s".toRegex(), "")
    if (b64.length < 128) {
        return null
    }
    return runCatching { Base64.decode(b64, Base64.DEFAULT) }.getOrNull()
}

internal fun decodeBitmap(bytes: ByteArray): Bitmap {
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    return bitmap ?: error("AI返回的图片无法解码")
}

internal fun loadCustomImageBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap {
    val mime = contentResolver.getType(uri).orEmpty().lowercase(Locale.US)
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: error("无法打开图片")
    val bitmap = if (mime.contains("svg") || looksLikeSvg(bytes)) {
        decodeSvgBitmap(bytes)
    } else {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: error("图片无法解码；请选择 PNG 或 SVG")
    }
    return fitBitmapOnTransparentCanvas(bitmap, SIZE_1X1, SIZE_1X1)
}

internal fun looksLikeSvg(bytes: ByteArray): Boolean {
    val prefix = String(bytes, 0, minOf(bytes.size, 256), Charsets.UTF_8).trimStart()
    return prefix.startsWith("<svg", ignoreCase = true) ||
        prefix.startsWith("<?xml", ignoreCase = true) && "<svg" in prefix.lowercase(Locale.US)
}

internal fun decodeSvgBitmap(bytes: ByteArray): Bitmap {
    val svg = SVG.getFromInputStream(bytes.inputStream())
    val width = svg.documentWidth.takeIf { it > 0f } ?: SIZE_1X1.toFloat()
    val height = svg.documentHeight.takeIf { it > 0f } ?: SIZE_1X1.toFloat()
    svg.setDocumentWidth(width)
    svg.setDocumentHeight(height)
    val bitmap = Bitmap.createBitmap(width.roundToInt().coerceAtLeast(1), height.roundToInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(AndroidColor.TRANSPARENT)
    svg.renderToCanvas(canvas)
    return bitmap
}

internal fun fitBitmapOnTransparentCanvas(source: Bitmap, width: Int, height: Int): Bitmap {
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(out)
    canvas.drawColor(AndroidColor.TRANSPARENT)
    val scale = minOf(
        width.toFloat() / source.width.toFloat(),
        height.toFloat() / source.height.toFloat(),
    )
    val targetWidth = (source.width * scale).roundToInt().coerceAtLeast(1)
    val targetHeight = (source.height * scale).roundToInt().coerceAtLeast(1)
    val resized = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    canvas.drawBitmap(resized, (width - targetWidth) / 2f, (height - targetHeight) / 2f, null)
    return out
}

internal fun customCandidateForPreview(
    mode: PreviewMode,
    kind: CustomImageKind,
    session: GenerationSession,
): IconCandidate? {
    val foreground = session.customForegrounds[mode]
    val background = session.customBackgrounds[mode]
    val transparent = solidBitmap(SIZE_1X1, SIZE_1X1, AndroidColor.TRANSPARENT)
    return when (kind) {
        CustomImageKind.Foreground -> foreground?.let {
            IconCandidate(
                recfgRaw = it,
                recbg = background ?: session.baseRecbg,
                monochromeRaw = it,
                preserveGeometry = true,
                isLocal = false,
            )
        }
        CustomImageKind.Background -> background?.let {
            IconCandidate(
                recfgRaw = foreground ?: transparent,
                recbg = it,
                monochromeRaw = foreground,
                preserveGeometry = true,
                isLocal = false,
            )
        }
    }
}

internal fun bitmapToDataUrl(bitmap: Bitmap): String =
    "data:image/png;base64,${Base64.encodeToString(bitmapToPngBytes(bitmap), Base64.NO_WRAP)}"

internal fun bitmapToPngBytes(bitmap: Bitmap): ByteArray {
    val output = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    return output.toByteArray()
}

internal fun ByteArrayOutputStream.writeString(value: String) {
    write(value.toByteArray(Charsets.UTF_8))
}

internal fun normalizeResponsesUrl(baseUrl: String): String {
    val normalized = baseUrl.trim().trimEnd('/')
    return when {
        normalized.endsWith("/responses") -> normalized
        normalized.endsWith("/v1") -> "$normalized/responses"
        "/v1/" in "$normalized/" -> "$normalized/responses"
        else -> "$normalized/v1/responses"
    }
}

internal fun normalizeImagesEditUrl(baseUrl: String): String {
    val normalized = baseUrl.trim().trimEnd('/')
    return when {
        normalized.endsWith("/images/edits") -> normalized
        normalized.endsWith("/v1") -> "$normalized/images/edits"
        "/v1/" in "$normalized/" -> "$normalized/images/edits"
        else -> "$normalized/v1/images/edits"
    }
}

internal fun activeGptForegroundPrompt(customPrompt: String, preset: GptPromptPreset, subjectPercent: Int): String {
    val custom = customPrompt.trim()
    val base = if (preset == GptPromptPreset.Custom && custom.isNotBlank()) {
        custom
    } else {
        preset.foregroundPrompt.ifBlank { GptPromptPreset.StableCutout.foregroundPrompt }
    }
    return base.trim().trimEnd('.') +
        ". Scale the subject/logo so its visible bounding box is about $subjectPercent% of the final square canvas."
}

internal fun buildTransparentForegroundPrompt(customPrompt: String, preset: GptPromptPreset, subjectPercent: Int): String =
    activeGptForegroundPrompt(customPrompt, preset, subjectPercent) + " Return the extracted subject on a real transparent background with alpha channel."

internal fun buildChromaForegroundPrompt(chromaHex: String, customPrompt: String, preset: GptPromptPreset, subjectPercent: Int): String =
    activeGptForegroundPrompt(customPrompt, preset, subjectPercent) +
        " Place the extracted subject on a perfectly flat solid $chromaHex chroma-key background. " +
        "The chroma-key background must be one uniform color, with no checkerboard, no transparency preview pattern, " +
        "no shadows, no gradients, no texture, and no lighting variation. " +
        "Do not use $chromaHex anywhere in the subject/logo."

internal fun buildBackgroundPrompt(): String =
    "Remove the app icon main subject/logo. Rebuild only the clean original background plate. No logo, no text, no symbol."
