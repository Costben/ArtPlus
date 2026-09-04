package dev.artplus.mobile

import android.content.SharedPreferences

/**
 * Slice 2.4：GPT/RMBG/预设存取族（原 MainActivity 本体原样搬迁）。
 * 只做物理搬迁+显式参数化：prefs/快照经参数注入，Activity 可变态经回调注入；
 * 存储顺序/key 名/算法一律不变。
 * MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动。
 */

internal fun paramsLoadGptSettings(
    prefs: SharedPreferences,
    updateLive: ((TuningParams) -> TuningParams) -> Unit,
    setGptModelId: (String) -> Unit,
    setGptBaseUrl: (String) -> Unit,
    setGptApiKey: (String) -> Unit,
) {
    updateLive { p -> p.copy(gptImageMode = (GptImageMode.fromValue(prefs.getString(PREF_GPT_MODE, GptImageMode.Images.value))).value) }
    updateLive { p -> p.copy(gptPromptPreset = (GptPromptPreset.fromValue(
        prefs.getString(PREF_GPT_PROMPT_PRESET, GptPromptPreset.StableCutout.value),
    )).value) }
    updateLive { p -> p.copy(gptCustomPrompt = prefs.getString(PREF_GPT_CUSTOM_PROMPT, "") ?: "") }
    setGptModelId(prefs.getString(PREF_GPT_MODEL_ID, "") ?: "")
    val storedBaseUrl = prefs.getString(PREF_GPT_BASE_URL, "") ?: ""
    setGptBaseUrl(if (storedBaseUrl == LEGACY_DEFAULT_GPT_BASE_URL) "" else storedBaseUrl)
    setGptApiKey(paramsLoadGptApiKey(prefs))
    if (storedBaseUrl == LEGACY_DEFAULT_GPT_BASE_URL) {
        prefs.edit().putString(PREF_GPT_BASE_URL, "").apply()
    }
}

internal fun paramsSaveGptSettings(
    prefs: SharedPreferences,
    getParams: () -> TuningParams,
    getGptApiKey: () -> String,
    getGptModelId: () -> String,
    getGptBaseUrl: () -> String,
): Boolean {
    val encryptedKey = paramsEncryptSecret(getGptApiKey().trim())
    return prefs
        .edit()
        .putString(PREF_GPT_MODE, GptImageMode.fromValue(getParams().gptImageMode).value)
        .putString(PREF_GPT_PROMPT_PRESET, GptPromptPreset.fromValue(getParams().gptPromptPreset).value)
        .putString(PREF_GPT_CUSTOM_PROMPT, getParams().gptCustomPrompt.trim())
        .putString(PREF_GPT_MODEL_ID, getGptModelId().trim())
        .putString(PREF_GPT_BASE_URL, getGptBaseUrl().trim())
        .remove(PREF_GPT_API_KEY)
        .apply {
            if (encryptedKey.isBlank()) {
                remove(PREF_GPT_API_KEY_ENCRYPTED)
            } else {
                putString(PREF_GPT_API_KEY_ENCRYPTED, encryptedKey)
            }
        }
        .commit()
}

internal fun paramsSaveSettingsPage(
    saveGpt: () -> Boolean,
    saveRmbg: () -> Boolean,
    saveLocalSeparation: () -> Unit,
    saveImageTuning: () -> Unit,
    saveLiquidGlass: () -> Unit,
    saveUi: () -> Unit,
    setGptSaveStatus: (String) -> Unit,
    setRmbgSaveStatus: (String) -> Unit,
    setStatusText: (String) -> Unit,
) {
    val gptSaved = runCatching { saveGpt() }.getOrDefault(false)
    val rmbgSaved = runCatching { saveRmbg() }.getOrDefault(false)
    saveLocalSeparation()
    saveImageTuning()
    saveLiquidGlass()
    saveUi()
    setGptSaveStatus("")
    setRmbgSaveStatus("")
    setStatusText(if (gptSaved && rmbgSaved) "设置已保存" else "设置保存失败")
}

internal fun paramsLoadRmbgSettings(
    prefs: SharedPreferences,
    setComponentUrl: (String) -> Unit,
) {
    setComponentUrl(
        prefs.getString(PREF_RMBG_COMPONENT_URL, DEFAULT_RMBG_COMPONENT_URL)
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_RMBG_COMPONENT_URL,
    )
    val storedInputSize = prefs.getInt(PREF_RMBG_INPUT_SIZE, DEFAULT_RMBG_INPUT_SIZE)
    if (
        !prefs.getBoolean(PREF_RMBG_INPUT_SIZE_MIGRATED_TO_1024, false) ||
        storedInputSize != DEFAULT_RMBG_INPUT_SIZE
    ) {
        prefs.edit()
            .putInt(PREF_RMBG_INPUT_SIZE, DEFAULT_RMBG_INPUT_SIZE)
            .putBoolean(PREF_RMBG_INPUT_SIZE_MIGRATED_TO_1024, true)
            .apply()
    }
}

internal fun paramsSaveRmbgSettings(
    prefs: SharedPreferences,
    getComponentUrl: () -> String,
): Boolean =
    prefs
        .edit()
        .putString(PREF_RMBG_COMPONENT_URL, getComponentUrl().trim())
        .putInt(PREF_RMBG_INPUT_SIZE, DEFAULT_RMBG_INPUT_SIZE)
        .commit()

internal fun paramsCurrentRmbgModelPreset(componentUrl: String): RmbgModelPreset {
    val url = componentUrl.trim()
    return RMBG_MODEL_PRESETS.firstOrNull { preset ->
        preset.url.isNotBlank() && preset.url == url
    } ?: RMBG_MODEL_PRESET_CUSTOM
}

internal fun paramsUpdateRmbgModelPreset(
    preset: RmbgModelPreset,
    setComponentUrl: (String) -> Unit,
    setSaveStatus: (String) -> Unit,
    setStatusText: (String) -> Unit,
) {
    if (preset == RMBG_MODEL_PRESET_CUSTOM) {
        setSaveStatus("")
        setStatusText("RMBG 使用自定义 URL")
        return
    }
    if (preset.url.isBlank()) {
        setSaveStatus("该预设缺少 URL")
        setStatusText("RMBG ${preset.label} 还没有下载地址")
        return
    }
    setComponentUrl(preset.url)
    setSaveStatus("")
    setStatusText("RMBG 预设已选择: ${preset.label}")
}

internal fun paramsRmbgInferenceStatusSummary(
    isGenerating: Boolean,
    candidateStatusText: String,
    report: RmbgInferenceReport?,
): String {
    if (isGenerating) {
        return candidateStatusText.ifBlank { "RMBG运行中" }
    }
    if (report != null) {
        return "${report.actualBackend.label}，耗时 ${report.elapsedMs}ms"
    }
    return "尚未运行"
}
