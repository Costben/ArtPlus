package dev.artplus.mobile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * 批量预览应用元数据。
 */
data class BatchPreviewAppMeta(
    val packageName: String,
    val label: String,
) {
    fun toJson(): JSONObject =
        JSONObject()
            .put("packageName", packageName)
            .put("label", label)

    companion object {
        fun fromJson(json: JSONObject): BatchPreviewAppMeta? {
            val pkg = json.optString("packageName").trim().takeIf { it.isNotEmpty() } ?: return null
            val label = json.optString("label").trim()
            return BatchPreviewAppMeta(packageName = pkg, label = label)
        }
    }
}

/**
 * 预设批量预览清单。
 */
data class BatchPreviewManifest(
    val presetId: String,
    val presetName: String,
    val updatedAt: Long,
    val apps: List<BatchPreviewAppMeta>,
) {
    fun toJson(): JSONObject {
        val array = JSONArray()
        apps.forEach { array.put(it.toJson()) }
        return JSONObject()
            .put("presetId", presetId)
            .put("presetName", presetName)
            .put("updatedAt", updatedAt)
            .put("apps", array)
    }

    companion object {
        fun fromJson(json: JSONObject): BatchPreviewManifest? = runCatching {
            val presetId = json.optString("presetId").trim().takeIf { it.isNotEmpty() } ?: return null
            val presetName = json.optString("presetName").trim().takeIf { it.isNotEmpty() } ?: return null
            val updatedAt = json.optLong("updatedAt", System.currentTimeMillis())
            val appsArray = json.optJSONArray("apps") ?: JSONArray()
            val apps = buildList {
                for (i in 0 until appsArray.length()) {
                    val item = appsArray.optJSONObject(i) ?: continue
                    BatchPreviewAppMeta.fromJson(item)?.let { add(it) }
                }
            }
            if (apps.isEmpty()) return null
            BatchPreviewManifest(presetId = presetId, presetName = presetName, updatedAt = updatedAt, apps = apps)
        }.getOrNull()
    }
}

/**
 * 抽象位图读写，便于纯 JVM 单元测试与 Android 运行时解耦。
 */
interface BatchPreviewBitmapIo {
    fun saveBitmap(file: File, bitmap: Bitmap?): Boolean
    fun decodeBitmap(file: File): Bitmap?
}

object DefaultBatchPreviewBitmapIo : BatchPreviewBitmapIo {
    override fun saveBitmap(file: File, bitmap: Bitmap?): Boolean {
        if (bitmap == null) return false
        val parent = file.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }
        return runCatching {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            true
        }.getOrDefault(false)
    }

    override fun decodeBitmap(file: File): Bitmap? {
        if (!file.exists() || file.length() == 0L) return null
        return runCatching {
            BitmapFactory.decodeFile(file.absolutePath)?.also { it.prepareToDraw() }
        }.getOrNull()
    }
}

/**
 * 预设批量预览磁盘持久化管理。
 *
 * 目录结构：
 * filesDir/batch_previews/{presetId}/
 *   manifest.json
 *   apps/
 *     {packageName}/
 *       recbg.png
 *       recfg.png
 *       rec_night.png
 *       monochrome_light.png
 *       monochrome_dark.png
 */
object BatchPreviewStore {
    const val DIR_NAME = "batch_previews"
    const val MANIFEST_FILE_NAME = "manifest.json"
    const val APPS_DIR_NAME = "apps"

    fun getPresetDir(filesDir: File, presetId: String): File =
        File(File(filesDir, DIR_NAME), presetId)

    fun hasSnapshot(filesDir: File, presetId: String): Boolean {
        val manifest = readManifest(filesDir, presetId) ?: return false
        return manifest.apps.isNotEmpty()
    }

    fun deleteSnapshot(filesDir: File, presetId: String): Boolean {
        val dir = getPresetDir(filesDir, presetId)
        return if (dir.exists()) dir.deleteRecursively() else true
    }

    fun readManifest(filesDir: File, presetId: String): BatchPreviewManifest? {
        val dir = getPresetDir(filesDir, presetId)
        val manifestFile = File(dir, MANIFEST_FILE_NAME)
        if (!manifestFile.exists() || manifestFile.length() == 0L) return null
        return runCatching {
            val json = JSONObject(manifestFile.readText())
            BatchPreviewManifest.fromJson(json)
        }.getOrNull()
    }

    fun writeManifest(filesDir: File, manifest: BatchPreviewManifest): Boolean {
        val dir = getPresetDir(filesDir, manifest.presetId)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val manifestFile = File(dir, MANIFEST_FILE_NAME)
        return runCatching {
            manifestFile.writeText(manifest.toJson().toString())
            true
        }.getOrDefault(false)
    }

    fun saveSnapshot(
        filesDir: File,
        preset: TuningPreset,
        items: List<BatchPreviewItemData>,
        bitmapIo: BatchPreviewBitmapIo = DefaultBatchPreviewBitmapIo,
    ): Boolean {
        if (items.isEmpty()) return false
        val dir = getPresetDir(filesDir, preset.id)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val manifest = BatchPreviewManifest(
            presetId = preset.id,
            presetName = preset.name,
            updatedAt = System.currentTimeMillis(),
            apps = items.map { BatchPreviewAppMeta(it.packageName, it.label) },
        )
        if (!writeManifest(filesDir, manifest)) {
            return false
        }

        val appsDir = File(dir, APPS_DIR_NAME)
        for (item in items) {
            val appDir = File(appsDir, item.packageName)
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            bitmapIo.saveBitmap(File(appDir, "recbg.png"), item.recbg)
            bitmapIo.saveBitmap(File(appDir, "recfg.png"), item.recfg)
            bitmapIo.saveBitmap(File(appDir, "rec_night.png"), item.recNight)
            bitmapIo.saveBitmap(File(appDir, "monochrome_light.png"), item.monochromeLight)
            bitmapIo.saveBitmap(File(appDir, "monochrome_dark.png"), item.monochromeDark)
        }
        return true
    }

    fun loadSnapshot(
        filesDir: File,
        preset: TuningPreset,
        bitmapIo: BatchPreviewBitmapIo = DefaultBatchPreviewBitmapIo,
    ): List<BatchPreviewItemData>? {
        val manifest = readManifest(filesDir, preset.id) ?: return null
        val dir = getPresetDir(filesDir, preset.id)
        val appsDir = File(dir, APPS_DIR_NAME)
        val items = mutableListOf<BatchPreviewItemData>()

        for (app in manifest.apps) {
            val appDir = File(appsDir, app.packageName)
            if (!appDir.exists()) continue
            val recbg = bitmapIo.decodeBitmap(File(appDir, "recbg.png"))
            val recfg = bitmapIo.decodeBitmap(File(appDir, "recfg.png"))
            val recNight = bitmapIo.decodeBitmap(File(appDir, "rec_night.png"))
            val monochromeLight = bitmapIo.decodeBitmap(File(appDir, "monochrome_light.png"))
            val monochromeDark = bitmapIo.decodeBitmap(File(appDir, "monochrome_dark.png"))

            items.add(
                BatchPreviewItemData(
                    packageName = app.packageName,
                    label = app.label,
                    recbg = recbg,
                    recfg = recfg,
                    recNight = recNight,
                    monochromeLight = monochromeLight,
                    monochromeDark = monochromeDark,
                ),
            )
        }

        return items.takeIf { it.isNotEmpty() }
    }
}

/**
 * 独立的批量预览项数据类，解耦位图与展示。
 */
data class BatchPreviewItemData(
    val packageName: String,
    val label: String,
    val recbg: Bitmap?,
    val recfg: Bitmap?,
    val recNight: Bitmap?,
    val monochromeLight: Bitmap?,
    val monochromeDark: Bitmap?,
)
