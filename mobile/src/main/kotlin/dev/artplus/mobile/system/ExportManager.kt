package dev.artplus.mobile

import android.content.ContentResolver
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Job

/**
 * SAF 导出管理（P4 拆分）：输出树写入/查找/.nomedia/路径解析/快速直拷。
 *
 * 从 MainActivity 迁移而来：原 `private fun`，现 `internal`。
 * - 读 Activity 状态者改为显式参数：`exportToTree`/`findChild`/`ensureNomediaAtTreeRoot`
 *   收 ContentResolver（原直读 contentResolver）；`exportToTree`/`ensureNomediaAtTreeRoot`/
 *   `exportToTreeFast` 的 treeUri 保持可空并保留原 `?: return/return false` 语义，
 *   调用方直接传 outputTreeUri。
 * - `copyStream`/`useRequired` 纯 helper 随带（唯一调用方即 exportToTree）。
 * - `resolveTreeUriToFilePath`/`backupPackageFast`（见 RootInstaller）在新家纯驻，
 *   调用点零改动。
 * - `exportSelectedToExternal`/`backupAllToExternal`/
 *   起止动画的协程/对话框/进度/UI 状态编排搬不动，留 Activity 做瘦壳。
 * 调用点改显式传参（同名同参 wrapper 禁止，§5.8）。
 * - Slice 1.5：`formatTreeUriDisplay` 纯移动（零 Activity 状态，同包直接用，
 *   Activity 内删本体零 wrapper，调用点零改动）。
 */

internal fun exportToTree(resolver: ContentResolver, treeUri: Uri?, packageDir: File) {
    val uri = treeUri ?: return
    val rootDoc = DocumentsContract.buildDocumentUriUsingTree(
        uri,
        DocumentsContract.getTreeDocumentId(uri),
    )
    var packageDoc = findChild(resolver, uri, rootDoc, packageDir.name)
    if (packageDoc == null) {
        packageDoc = DocumentsContract.createDocument(
            resolver,
            rootDoc,
            DocumentsContract.Document.MIME_TYPE_DIR,
            packageDir.name,
        )
    }
    if (packageDoc == null) {
        error("无法创建输出目录")
    }

    val files = packageDir.listFiles { _, name -> name.endsWith(".png") } ?: return
    for (file in files) {
        findChild(resolver, uri, packageDoc, file.name)?.let {
            DocumentsContract.deleteDocument(resolver, it)
        }
        val doc = DocumentsContract.createDocument(resolver, packageDoc, "image/png", file.name)
            ?: error("无法创建文件: ${file.name}")
        FileInputStream(file).use { input ->
            resolver.openOutputStream(doc, "w").useRequired { output ->
                copyStream(input, output)
            }
        }
    }
}

internal fun findChild(resolver: ContentResolver, treeUri: Uri, parentDoc: Uri, displayName: String): Uri? {
    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
        treeUri,
        DocumentsContract.getDocumentId(parentDoc),
    )
    return try {
        var found: Uri? = null
        resolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            ),
            null,
            null,
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val childName = cursor.getString(1)
                if (displayName == childName) {
                    val documentId = cursor.getString(0)
                    found = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                    break
                }
            }
        }
        found
    } catch (_: Exception) {
        null
    }
}

internal fun copyStream(input: InputStream, output: OutputStream) {
    val buffer = ByteArray(64 * 1024)
    while (true) {
        val read = input.read(buffer)
        if (read == -1) {
            return
        }
        output.write(buffer, 0, read)
    }
}

internal fun OutputStream?.useRequired(block: (OutputStream) -> Unit) {
    val output = this ?: error("无法打开输出流")
    output.use { block(it) }
}

internal fun ensureNomediaAtTreeRoot(resolver: ContentResolver, treeUri: Uri?) {
    val uri = treeUri ?: return
    // 优先尝试文件系统快速路径（su touch），失败再走 SAF
    resolveTreeUriToFilePath(uri)?.let { path ->
        runCatching {
            runRootCommand("mkdir -p ${shQuote(path)} && touch ${shQuote("$path/.nomedia")} && chmod 0644 ${shQuote("$path/.nomedia")} 2>/dev/null; echo ok", 3000)
        }.onSuccess { if (it.contains("ok")) return }
    }
    val rootDoc = DocumentsContract.buildDocumentUriUsingTree(
        uri,
        DocumentsContract.getTreeDocumentId(uri),
    )
    if (findChild(resolver, uri, rootDoc, ".nomedia") != null) return
    runCatching {
        val doc = DocumentsContract.createDocument(
            resolver,
            rootDoc,
            "application/octet-stream",
            ".nomedia",
        ) ?: return@runCatching
        resolver.openOutputStream(doc, "w")?.use { it.write(ByteArray(0)) }
    }
}

internal fun resolveTreeUriToFilePath(treeUri: Uri): String? {
    return try {
        val docId = DocumentsContract.getTreeDocumentId(treeUri) // e.g. primary:Download/ArtPlus
        val colon = docId.indexOf(':')
        if (colon <= 0) return null
        val volume = docId.substring(0, colon)
        val rel = java.net.URLDecoder.decode(docId.substring(colon + 1), "UTF-8")
        val base = when (volume) {
            "primary" -> Environment.getExternalStorageDirectory().absolutePath // /storage/emulated/0
            "home" -> Environment.getExternalStorageDirectory().absolutePath + "/Documents"
            else -> "/storage/$volume" // 外置 SD 卡: 1234-5678
        }
        if (rel.isBlank()) base else "$base/$rel"
    } catch (_: Exception) { null }
}

internal fun exportToTreeFast(treeUri: Uri?, packageDir: File): Boolean {
    val uri = treeUri ?: return false
    val destRoot = resolveTreeUriToFilePath(uri) ?: return false
    val destPkg = "$destRoot/${packageDir.name}"
    val src = packageDir.absolutePath
    // 单次 su 完成：建目录 + 拷贝 + 权限
    val cmd = "mkdir -p ${shQuote(destPkg)} && cp -f ${shQuote(src)}/*.png ${shQuote(destPkg)}/ 2>/dev/null && chmod 0644 ${shQuote(destPkg)}/*.png 2>/dev/null && echo ok"
    return try {
        val out = runRootCommand(cmd, 8000)
        out.contains("ok")
    } catch (_: Exception) { false }
}

/**
 * Slice 1.5 纯移动（零 Activity 状态，同包直接用，Activity 内删本体零 wrapper）：
 * 导出目录 TreeUri 可读展示（优先 treeDocumentId，形如 primary:Download/xxx 取冒号后路径）。
 */
internal fun formatTreeUriDisplay(uri: Uri?): String? {
    if (uri == null) return null
    // 优先用 treeDocumentId（如 primary:Download/ArtPlusOutput），比 raw Uri 更可读
    val fromId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
    if (!fromId.isNullOrBlank()) {
        // 形如 primary:Download/xxx -> 取冒号后路径，未含冒号则直接解码
        val raw = if (":" in fromId) fromId.substringAfter(":") else fromId
        val decoded = runCatching { java.net.URLDecoder.decode(raw, "UTF-8") }.getOrNull() ?: raw
        if (decoded.isNotBlank()) return decoded
    }
    return uri.lastPathSegment?.let { runCatching { java.net.URLDecoder.decode(it, "UTF-8") }.getOrNull() ?: it }
}

/**
 * Slice 1.5 移动：设置页旧导出入口本体（原直调 Activity 全量备份，改为显式回调参数；
 * MainActivity 留无参 wrapper，调用点零改动）。
 */
internal fun exportCurrentToExternal(onBackupAll: () -> Unit) {
    // 保留兼容：设置页旧入口，委托到全量备份
    onBackupAll()
}

/**
 * Slice 1.5 移动：备份/单包导出的取消状态显式参数（原直读直写 Activity 状态，
 * MainActivity 无参 wrapper 负责装配/写回，ui/pages/Shell.kt 调用点零改动；
 * Job 取消顺序与状态清零顺序与原本体一致；toast 经显式回调，不碰 Context）。
 */
internal class BackupCancelState(
    var backupJob: Job? = null,
    var backupDotJob: Job? = null,
    var sheetVisible: Boolean = false,
    var inBackground: Boolean = false,
    var progress: ExportProgress? = null,
    var isBusy: Boolean = false,
)

internal class SingleExportCancelState(
    var singleExportJob: Job? = null,
    var sheetVisible: Boolean = false,
    var progress: ExportProgress? = null,
)

/** 原 MainActivity.cancelBackup 本体（纯移动，行为等价）。 */
internal fun cancelBackup(state: BackupCancelState, onToast: (String) -> Unit) {
    state.backupJob?.cancel()
    state.backupJob = null
    state.backupDotJob?.cancel()
    state.backupDotJob = null
    state.sheetVisible = false
    state.inBackground = false
    state.progress = null
    state.isBusy = false
    onToast("已停止备份")
}

/** 原 MainActivity.cancelSingleExport 本体（纯移动，行为等价）。 */
internal fun cancelSingleExport(state: SingleExportCancelState, onToast: (String) -> Unit) {
    state.singleExportJob?.cancel()
    state.singleExportJob = null
    state.sheetVisible = false
    state.progress = null
    onToast("已停止导出")
}
