package dev.artplus.mobile

/**
 * Slice 2.4：预览显示存取族（原 MainActivity 本体原样搬迁）。
 * 只做物理搬迁+显式参数化：预览/批量预览 var 经显式 getter/setter 注入，持久化经回调注入。
 * MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动。
 */

internal fun paramsUpdatePreviewCornerRadiusDp(
    value: Int,
    setValue: (Int) -> Unit,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
) {
    val next = value.coerceIn(MIN_PREVIEW_CORNER_RADIUS_DP, MAX_PREVIEW_CORNER_RADIUS_DP)
    setValue(next)
    setDraftText(next.toString())
    onSave()
}

internal fun paramsUpdatePreviewIconSizeDp(
    value: Int,
    setValue: (Int) -> Unit,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
) {
    val next = value.coerceIn(MIN_PREVIEW_ICON_SIZE_DP, MAX_PREVIEW_ICON_SIZE_DP)
    setValue(next)
    setDraftText(next.toString())
    onSave()
}

internal fun paramsUpdateBatchPreviewCount(
    value: Int,
    setValue: (Int) -> Unit,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
) {
    val next = value.coerceIn(MIN_BATCH_PREVIEW_COUNT, MAX_BATCH_PREVIEW_COUNT)
    setValue(next)
    setDraftText(next.toString())
    onSave()
}

internal fun paramsUpdateBatchPreviewColumns(
    value: Int,
    setColumns: (Int) -> Unit,
    setDraftColumnsText: (String) -> Unit,
    setIconSize: (Int) -> Unit,
    setDraftIconSizeText: (String) -> Unit,
    onSave: () -> Unit,
) {
    val next = value.coerceIn(2, 5)
    setColumns(next)
    setDraftColumnsText(next.toString())
    val autoSize = when (next) {
        2 -> 72
        3 -> 64
        4 -> 54
        5 -> 46
        else -> 54
    }
    setIconSize(autoSize)
    setDraftIconSizeText(autoSize.toString())
    onSave()
}

internal fun paramsUpdateBatchPreviewIconSizeDp(
    value: Int,
    setValue: (Int) -> Unit,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
) {
    val next = value.coerceIn(40, 84)
    setValue(next)
    setDraftText(next.toString())
    onSave()
}

internal fun paramsUpdateBatchPreviewCornerRadiusDp(
    value: Int,
    setValue: (Int) -> Unit,
    setDraftText: (String) -> Unit,
    onSave: () -> Unit,
) {
    val next = value.coerceIn(0, 36)
    setValue(next)
    setDraftText(next.toString())
    onSave()
}

internal fun paramsUpdateBatchPreviewDesktopBackground(
    option: PreviewDesktopBackground,
    getValue: () -> PreviewDesktopBackground,
    setValue: (PreviewDesktopBackground) -> Unit,
    onSave: () -> Unit,
) {
    if (getValue() == option) {
        return
    }
    setValue(option)
    onSave()
}

internal fun paramsUpdatePreviewDesktopBackground(
    option: PreviewDesktopBackground,
    getValue: () -> PreviewDesktopBackground,
    setValue: (PreviewDesktopBackground) -> Unit,
    onSave: () -> Unit,
) {
    if (getValue() == option) {
        return
    }
    setValue(option)
    onSave()
}

internal fun paramsUpdatePreviewStripEnabled(
    enabled: Boolean,
    getValue: () -> Boolean,
    setValue: (Boolean) -> Unit,
    onSave: () -> Unit,
    setStatusText: (String) -> Unit,
) {
    if (getValue() == enabled) {
        return
    }
    setValue(enabled)
    onSave()
    setStatusText(if (enabled) {
        "已开启主页面顶部预览条"
    } else {
        "已关闭主页面顶部预览条"
    })
}
