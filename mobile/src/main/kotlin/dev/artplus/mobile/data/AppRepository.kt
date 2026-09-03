package dev.artplus.mobile

import android.content.SharedPreferences
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import java.util.Locale

/**
 * 应用数据层（P3 拆分）：应用列表查询/排序 + 已生成包扫描/缓存。
 *
 * 从 MainActivity 迁移而来：原 `private fun`，现 `internal`。
 * - `AppEntry` 整类提升（原 private data class，实现 BatchSampleTarget；P3 前置）。
 * - 读 Activity 状态者改为显式参数/返回值：`loadApps(pm)` 收 PackageManager、
 *   返回 entries + launchablePackages（线程/UI/状态写入留 Activity 编排）；
 *   三个 cache 函数收 SharedPreferences、返回归一化集合（字段写回留 Activity wrapper）。
 * - `refreshGeneratedPackages` 的线程 + runOnUiThread 编排无法外迁，留 Activity
 *   做瘦编排，直接调用本文件的 scan/cache 函数。
 * Activity 内保留 arity 变化的同名 wrapper 委托（重构期间保留，P5 后删除），调用点零改动。
 */

internal data class AppEntry(
    val label: String,
    override val packageName: String,
    val applicationInfo: ApplicationInfo,
    override val launchable: Boolean,
    val iconKey: String,
) : BatchSampleTarget

internal data class AppLoadResult(
    val entries: List<AppEntry>,
    val launchablePackages: Set<String>,
)

/**
 * 后台查询全部已安装应用并排序（从 MainActivity.loadApps 拆出纯数据核；
 * Thread/runOnUiThread/apps 写入/statusText 留 Activity 编排）。
 */
internal fun loadApps(pm: PackageManager): AppLoadResult {
    val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
    val launchablePackages = queryLaunchablePackages(pm, intent)
    val installedApps = getInstalledApplications(pm)
    val entries = installedApps.mapNotNull { info ->
        val packageName = info.packageName ?: return@mapNotNull null
        val label = runCatching { info.loadLabel(pm)?.toString() }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: packageName
        AppEntry(
            label = label,
            packageName = packageName,
            applicationInfo = info,
            launchable = packageName in launchablePackages,
            iconKey = "${packageName}:${info.uid}:${info.sourceDir}",
        )
    }
        .sortedWith(
            compareByDescending<AppEntry> { it.launchable }
                .thenBy { it.label.lowercase(Locale.ROOT) }
                .thenBy { it.packageName },
        )
    return AppLoadResult(entries, launchablePackages)
}

@Suppress("DEPRECATION")
internal fun queryLaunchablePackages(pm: PackageManager, intent: Intent): Set<String> {
    val resolveInfos = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        pm.queryIntentActivities(
            intent,
            PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
        )
    } else {
        pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
    }
    return resolveInfos
        .mapNotNull { it.activityInfo?.packageName }
        .toSet()
}

@Suppress("DEPRECATION")
internal fun getInstalledApplications(pm: PackageManager): List<ApplicationInfo> =
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        pm.getInstalledApplications(
            PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
        )
    } else {
        pm.getInstalledApplications(PackageManager.GET_META_DATA)
    }

/**
 * 经 root 扫描 uxicons 目录，返回生成过图标的包名（调用 runRootCommand，抛异常由调用方处理）。
 */
internal fun scanRootGeneratedPackages(packageNames: Set<String>): Set<String> {
    if (packageNames.isEmpty()) {
        return emptySet()
    }
    val command = """
        if [ -d ${shQuote(ROOT_UXICONS_DIR)} ]; then
            while IFS= read -r name; do
                [ -n "${'$'}name" ] || continue
                dir=${shQuote(ROOT_UXICONS_DIR)}/"${'$'}name"
                [ -d "${'$'}dir" ] || continue
                if [ -f "${'$'}dir/recbg.png" ] ||
                    [ -f "${'$'}dir/recfg.png" ] ||
                    [ -f "${'$'}dir/rec_night.png" ] ||
                    [ -f "${'$'}dir/monochrome.png" ] ||
                    ls "${'$'}dir"/*.png >/dev/null 2>&1; then
                    printf '%s\n' "${'$'}name"
                fi
            done
        fi
    """.trimIndent()
    val input = packageNames
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .sorted()
        .joinToString(separator = "\n", postfix = "\n")
    val output = runRootCommand(command, ROOT_SCAN_TIMEOUT_MS, input)
    return output
        .lineSequence()
        .map { it.trim() }
        .filter { it in packageNames }
        .toSet()
}

/** 读已生成包缓存并归一化（去空/trim）。 */
internal fun loadGeneratedPackageCache(prefs: SharedPreferences): Set<String> =
    prefs.getStringSet(PREF_GENERATED_PACKAGE_NAMES, emptySet())
        .orEmpty()
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()

/** 归一化后写回缓存（含更新时间戳），返回归一化集合（调用方写回字段）。 */
internal fun updateGeneratedPackageCache(prefs: SharedPreferences, packages: Set<String>): Set<String> {
    val normalized = packages
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()
    prefs.edit()
        .putStringSet(PREF_GENERATED_PACKAGE_NAMES, normalized)
        .putLong(PREF_GENERATED_PACKAGE_NAMES_UPDATED_AT, System.currentTimeMillis())
        .apply()
    return normalized
}

/** 标记单个包已生成（读-改-写经 updateGeneratedPackageCache，返回新集合）。 */
internal fun markPackageGenerated(
    prefs: SharedPreferences,
    current: Set<String>,
    packageName: String,
): Set<String> = updateGeneratedPackageCache(prefs, current + packageName)
