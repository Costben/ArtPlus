package dev.artplus.mobile

import android.content.ContentResolver
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream

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
 * - `exportSelectedToExternal`/`backupAllToExternal`/`cancelBackup`/`cancelSingleExport`/
 *   起止动画的协程/对话框/进度/UI 状态编排搬不动，留 Activity 做瘦壳。
 * 调用点改显式传参（同名同参 wrapper 禁止，§5.8）。
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
