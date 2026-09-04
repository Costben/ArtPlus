package dev.artplus.mobile

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.net.HttpURLConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.CheckboxDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Slice 2.5：应用选择器通用弹窗/杂项（原 MainActivity 残留本体原样搬迁）。
 * 只做物理搬迁+显式参数化：Activity 状态经参数/回调注入；
 * 权限请求/版本检查/外链跳转只做纯移动，不改请求码、判断顺序与跳转目标。
 * MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动；
 * 纯函数 isNewerVersion 直接搬迁、不留 wrapper（避免同名同参）。
 * 名单内联 helper 扫描结论：本簇无内联 helper（isDebugBuild/validatedRemoteUrl 属名单外，
 * 搬迁时转为参数注入，不移动本体）。
 */

@Composable
internal fun OnboardingDialog(
    visible: Boolean,
    isBusy: Boolean,
    onSkip: () -> Unit,
    onChooseDir: () -> Unit,
) {
    if (!visible) return
    MiuixBottomDialog(onDismissRequest = onSkip) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MiuixTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "设置备份目录",
                style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                color = MiuixTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "首次使用建议先选择一个外部目录，用于备份已写入系统的全部图标（含官方图标）。选择后将自动执行一次全量备份，并在该目录创建 .nomedia 避免出现在相册。",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(),
                ) {
                    Text(
                        text = "跳过",
                        style = MiuixTheme.textStyles.button,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                }
                Button(
                    onClick = onChooseDir,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(
                        text = "选择目录",
                        style = MiuixTheme.textStyles.button,
                        color = Color.White,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
internal fun RefreshConfirmDialog(
    visible: Boolean,
    rememberAuto: Boolean,
    onDismiss: () -> Unit,
    onToggleRemember: () -> Unit,
    onConfirm: (rememberAuto: Boolean) -> Unit,
) {
    if (!visible) return
    MiuixBottomDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MiuixTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "确认刷新",
                style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                color = MiuixTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "将重新扫描已生成图标并刷新显示，确认继续？",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onToggleRemember() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Checkbox(
                    state = ToggleableState(rememberAuto),
                    onClick = onToggleRemember,
                    colors = CheckboxDefaults.checkboxColors(
                        checkedBackgroundColor = MiuixTheme.colorScheme.primaryVariant,
                        checkedForegroundColor = MiuixTheme.colorScheme.onPrimaryVariant,
                        uncheckedBackgroundColor = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.35f),
                        uncheckedForegroundColor = Color.Transparent,
                    ),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "以后都自动确认",
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(),
                ) {
                    Text(
                        text = "取消",
                        style = MiuixTheme.textStyles.button,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                }
                Button(
                    onClick = { onConfirm(rememberAuto) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(
                        text = "刷新",
                        style = MiuixTheme.textStyles.button,
                        color = Color.White,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
internal fun PermissionCard(
    packageListGranted: Boolean,
    usageGranted: Boolean,
    isBusy: Boolean,
    onOpenAppSettings: () -> Unit,
    onOpenUsageSettings: () -> Unit,
) {
    SectionCard {
        SettingLine(
            title = "应用列表",
            summary = if (packageListGranted) "已声明并可读取已安装应用" else "需要允许读取应用列表",
            value = if (packageListGranted) "已允许" else "待授权",
        )
        Spacer(modifier = Modifier.height(10.dp))
        SettingLine(
            title = "使用情况访问",
            summary = if (usageGranted) "已允许任务/使用情况访问" else "Android 只能在系统设置中授权",
            value = if (usageGranted) "已允许" else "待授权",
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TextButton(
                text = "应用权限",
                onClick = onOpenAppSettings,
                enabled = !isBusy,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                text = "使用情况访问",
                onClick = onOpenUsageSettings,
                enabled = !isBusy,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun RootWriteConfirmDialog(
    request: RootWriteConfirmRequest?,
    rememberSkip: Boolean,
    onDismiss: () -> Unit,
    onToggleSkip: () -> Unit,
    onConfirm: (request: RootWriteConfirmRequest, skip: Boolean) -> Unit,
) {
    val current = request ?: return
    MiuixBottomDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MiuixTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "确认写入",
                style = MiuixTheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                color = MiuixTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "将直接把当前生成的内容写入到指定路径：",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = current.targetPath,
                    style = MiuixTheme.textStyles.footnote1.copy(fontWeight = FontWeight.SemiBold),
                    color = MiuixTheme.colorScheme.primaryVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "是否确认写入${current.rootWriteMode.label}？",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onToggleSkip() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Checkbox(
                    state = ToggleableState(rememberSkip),
                    onClick = onToggleSkip,
                    colors = CheckboxDefaults.checkboxColors(
                        checkedBackgroundColor = MiuixTheme.colorScheme.primaryVariant,
                        checkedForegroundColor = MiuixTheme.colorScheme.onPrimaryVariant,
                        uncheckedBackgroundColor = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.35f),
                        uncheckedForegroundColor = Color.Transparent,
                    ),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "以后都自动确认",
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(),
                ) {
                    Text(
                        text = "取消",
                        style = MiuixTheme.textStyles.button,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                }
                Button(
                    onClick = { onConfirm(current, rememberSkip) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(
                        text = "确认写入",
                        style = MiuixTheme.textStyles.button,
                        color = Color.White,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

internal fun pickerSystemMaterialColor(
    resources: Resources,
    getColor: (Int) -> Int,
    resourceName: String,
    fallback: Color,
): Color {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return fallback
    }
    val colorId = resources.getIdentifier(resourceName, "color", "android")
    if (colorId == 0) {
        return fallback
    }
    return runCatching { Color(getColor(colorId)) }.getOrDefault(fallback)
}

internal fun pickerCurrentVersionName(
    getVersionName: () -> String?,
    fallback: String = "1.4.0",
): String = try {
    getVersionName() ?: fallback
} catch (_: Exception) {
    fallback
}

internal fun isNewerVersion(latest: String, current: String): Boolean {
    val latestParts = latest.trim().removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
    val currentParts = current.trim().removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
    val len = maxOf(latestParts.size, currentParts.size)
    for (i in 0 until len) {
        val l = latestParts.getOrElse(i) { 0 }
        val c = currentParts.getOrElse(i) { 0 }
        if (l != c) return l > c
    }
    return false
}

internal fun pickerCheckForUpdate(
    isChecking: Boolean,
    onCheckingChange: (Boolean) -> Unit,
    onStatusText: (String) -> Unit,
    scope: CoroutineScope,
    resolveUrl: (String) -> java.net.URL,
    fetchLatest: (java.net.URL) -> Triple<Int, String, String>,
    currentVersion: String,
    onUpdateAvailable: (UpdateInfo, String) -> Unit,
    onUpToDate: (String) -> Unit,
    onFailed: (String) -> Unit,
) {
    if (isChecking) return
    onCheckingChange(true)
    onStatusText("正在检查更新...")
    scope.launch(Dispatchers.IO) {
        try {
            val url = resolveUrl("https://api.github.com/repos/Costben/ArtPlus/releases/latest")
            val (code, body, _) = fetchLatest(url).let { Triple(it.first, it.second, it.third) }
            if (code !in 200..299) error("HTTP $code ${body.take(200)}")
            val json = JSONObject(body)
            val tagName = json.optString("tag_name", "")
            val htmlUrl = json.optString("html_url", GITHUB_REPO_URL + "/releases")
            val latest = tagName.removePrefix("v").trim()
            val current = currentVersion.trim()
            withContext(Dispatchers.Main) {
                if (latest.isBlank()) {
                    onFailed("检查失败：未获取到版本信息")
                } else if (isNewerVersion(latest, current)) {
                    onUpdateAvailable(UpdateInfo(latest, tagName.ifBlank { "v$latest" }, htmlUrl), "发现新版本 $tagName")
                } else {
                    onUpToDate("已是最新版本 $current")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onFailed("检查更新失败: ${e.message ?: e.javaClass.simpleName}")
            }
        } finally {
            withContext(Dispatchers.Main) { onCheckingChange(false) }
        }
    }
}

/** 默认网络实现（供 wrapper 注入 resolveUrl/fetchLatest 用，保持原判断顺序）。 */
internal fun pickerResolveUpdateUrl(urlText: String, label: String, debugBuild: Boolean): java.net.URL =
    validatedRemoteUrl(urlText, label, debugBuild)

internal fun pickerFetchUpdateBody(url: java.net.URL): Triple<Int, String, String> {
    val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 10_000
        readTimeout = 10_000
        setRequestProperty("Accept", "application/vnd.github+json")
        setRequestProperty("User-Agent", "ArtPlus-Android")
    }
    val code = connection.responseCode
    val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
        ?.bufferedReader()?.use { it.readText() } ?: ""
    connection.disconnect()
    return Triple(code, body, "")
}

internal fun pickerHasUsageAccess(
    appOps: AppOpsManager?,
    uid: Int,
    packageName: String,
): Boolean {
    if (appOps == null) return false
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            uid,
            packageName,
        )
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            uid,
            packageName,
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

internal fun pickerGetApplicationInfoCompat(
    pm: PackageManager,
    packageName: String,
): ApplicationInfo =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.getApplicationInfo(
            packageName,
            PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
        )
    } else {
        @Suppress("DEPRECATION")
        pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    }

internal fun pickerOpenExternalLink(
    start: (Intent) -> Unit,
    url: String,
    onError: (String) -> Unit,
) {
    runCatching {
        start(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        onError("无法打开链接: ${it.message ?: it.javaClass.simpleName}")
    }
}

internal fun pickerOpenAppPermissionSettings(
    start: (Intent) -> Unit,
    packageName: String,
    onError: (String) -> Unit,
) {
    runCatching {
        start(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", packageName, null)),
        )
    }.onFailure {
        onError("无法打开应用权限设置: ${it.message ?: it.javaClass.simpleName}")
    }
}

internal fun pickerOpenUsageAccessSettings(
    start: (Intent) -> Unit,
    onError: (String) -> Unit,
) {
    runCatching {
        start(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }.onFailure {
        onError("无法打开使用情况访问设置: ${it.message ?: it.javaClass.simpleName}")
    }
}

internal fun pickerShowKeyboardFor(editText: EditText) {
    editText.post {
        editText.requestFocus()
        editText.context
            .getSystemService(InputMethodManager::class.java)
            ?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }
}

internal fun pickerPostStatus(message: String, postOnUi: (String) -> Unit) {
    postOnUi(message)
}

internal fun pickerToastStatus(
    message: String,
    postOnUi: (String) -> Unit,
    showToast: (String) -> Unit,
) {
    postOnUi(message)
    showToast(message)
}
