package dev.artplus.mobile

import android.net.LocalServerSocket
import android.net.LocalSocket
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
