package dev.artplus.mobile

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/** 预设 blob 的 schema 版本，独立于 live 设置的 CURRENT_IMAGE_TUNING_VERSION。 */
const val PRESET_SCHEMA_VERSION = 1

/**
 * 一份命名参数预设：完整的 TuningParams 快照 + 元信息。
 */
data class TuningPreset(
    val id: String,
    val name: String,
    val params: TuningParams,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun toJson(): JSONObject =
        JSONObject()
            .put("id", id)
            .put("name", name)
            .put("params", params.toJson())
            .put("createdAt", createdAt)
            .put("updatedAt", updatedAt)

    companion object {
        fun fromJson(json: JSONObject, defaults: TuningParams): TuningPreset? =
            runCatching {
                val paramsJson = json.optJSONObject("params") ?: return null
                val params = TuningParams.fromJson(paramsJson, defaults) ?: return null
                val id = json.optString("id").trim().takeIf { it.isNotBlank() } ?: return null
                val name = json.optString("name").trim().takeIf { it.isNotBlank() } ?: return null
                val now = System.currentTimeMillis()
                TuningPreset(
                    id = id,
                    name = name,
                    params = params,
                    createdAt = json.optLong("createdAt", now),
                    updatedAt = json.optLong("updatedAt", now),
                )
            }.getOrNull()
    }

    /** 提取该预设的 1~2 个关键视觉风格特征标签。 */
    fun featureTags(): List<String> {
        val tags = mutableListOf<String>()
        if (params.liquidGlassEnabled) {
            tags += "玻璃 ${params.liquidGlassRadius}dp"
        } else {
            tags += "经典"
        }
        if (params.localBackgroundSeparationEnabled) {
            tags += "本地分离"
        }
        if (params.rmbgAlphaStrengthPercent > 0) {
            tags += "RMBG"
        }
        if (params.monochromeThemeScale > 0.05f) {
            tags += "单色 ${(params.monochromeThemeScale * 100).toInt()}%"
        }
        return tags.take(2)
    }
}

/** 导入结果摘要。 */
data class PresetImportResult(
    val imported: Int,
    val errors: List<String>,
)

/**
 * 预设存储：整个列表存为 SharedPreferences 下的单个 JSON 字符串（键 tuning_presets）。
 * 单键写入原子、天然可导入导出；schema 版本随 blob 走。
 */
class PresetStore(private val prefs: SharedPreferences) {

    fun all(): List<TuningPreset> {
        val raw = prefs.getString(PREF_PRESETS_JSON, null) ?: return emptyList()
        return runCatching {
            val root = JSONObject(raw)
            val array = root.optJSONArray("presets") ?: return emptyList()
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    TuningPreset.fromJson(item, TuningParams())?.let { add(it) }
                }
            }
        }.getOrDefault(emptyList())
    }

    fun get(id: String): TuningPreset? = all().firstOrNull { it.id == id }

    /** 重名（trim + 忽略大小写）返回 false，否则 upsert 并写入。 */
    fun save(preset: TuningPreset): Boolean {
        val list = all().toMutableList()
        val existingIndex = list.indexOfFirst { it.id == preset.id }
        val duplicateName = list.any { it.id != preset.id && it.name.trim().equals(preset.name.trim(), ignoreCase = true) }
        if (duplicateName) {
            return false
        }
        if (existingIndex >= 0) {
            list[existingIndex] = preset
        } else {
            list += preset
        }
        write(list)
        return true
    }

    fun delete(id: String) {
        val list = all().filterNot { it.id == id }
        write(list)
        if (activePresetId == id) {
            activePresetId = null
        }
    }

    fun rename(id: String, name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            return false
        }
        val list = all()
        val existingIndex = list.indexOfFirst { it.id == id }
        if (existingIndex < 0) {
            return false
        }
        if (list.any { it.id != id && it.name.trim().equals(trimmed, ignoreCase = true) }) {
            return false
        }
        val updated = list[existingIndex].copy(name = trimmed, updatedAt = System.currentTimeMillis())
        val next = list.toMutableList().apply { this[existingIndex] = updated }
        write(next)
        return true
    }

    var activePresetId: String?
        get() = prefs.getString(PREF_ACTIVE_PRESET_ID, null)
        set(value) {
            prefs.edit().putString(PREF_ACTIVE_PRESET_ID, value).apply()
        }

    /** 导出单条预设为 `{"schemaVersion":1,"presets":[...]}`。 */
    fun exportSingleJson(preset: TuningPreset): String =
        JSONObject()
            .put("schemaVersion", PRESET_SCHEMA_VERSION)
            .put("presets", JSONArray().also { it.put(preset.toJson()) })
            .toString()

    /** 导出全部预设为 `{"schemaVersion":1,"presets":[...]}`。 */
    fun exportJson(): String =
        JSONObject()
            .put("schemaVersion", PRESET_SCHEMA_VERSION)
            .put("presets", JSONArray().also { array ->
                all().forEach { array.put(it.toJson()) }
            })
            .toString()

    /**
     * 导入预设 blob：逐条校验，未知键由 TuningParams 忽略，缺失键取默认。
     * schemaVersion > 当前版本则整体拒绝。
     */
    fun importJson(json: String): PresetImportResult {
        return runCatching {
            val root = JSONObject(json)
            val version = root.optInt("schemaVersion", 1)
            if (version > PRESET_SCHEMA_VERSION) {
                return PresetImportResult(
                    imported = 0,
                    errors = listOf("预设版本 $version 高于当前支持的 $PRESET_SCHEMA_VERSION"),
                )
            }
            val array = root.optJSONArray("presets") ?: JSONArray()
            val list = all().toMutableList()
            val existingIds = list.mapTo(mutableSetOf()) { it.id }
            val errors = mutableListOf<String>()
            var imported = 0
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val preset = TuningPreset.fromJson(item, TuningParams())
                if (preset == null) {
                    errors += "第 ${i + 1} 条预设解析失败"
                    continue
                }
                if (list.any { it.id != preset.id && it.name.trim().equals(preset.name.trim(), ignoreCase = true) }) {
                    errors += "预设「${preset.name}」与现有重名，已跳过"
                    continue
                }
                if (existingIds.contains(preset.id)) {
                    list.removeAll { it.id == preset.id }
                }
                list += preset
                existingIds += preset.id
                imported += 1
            }
            if (imported > 0) {
                write(list)
            }
            PresetImportResult(imported = imported, errors = errors)
        }.getOrElse { error ->
            PresetImportResult(imported = 0, errors = listOf("导入内容不是有效 JSON: ${error.message}"))
        }
    }

    private fun write(list: List<TuningPreset>) {
        val blob = JSONObject()
            .put("schemaVersion", PRESET_SCHEMA_VERSION)
            .put(
                "presets",
                JSONArray().also { array ->
                    list.forEach { array.put(it.toJson()) }
                },
            )
            .toString()
        prefs.edit().putString(PREF_PRESETS_JSON, blob).apply()
    }

    private companion object {
        const val PREF_PRESETS_JSON = "tuning_presets"
        const val PREF_ACTIVE_PRESET_ID = "tuning_active_preset_id"
    }
}
