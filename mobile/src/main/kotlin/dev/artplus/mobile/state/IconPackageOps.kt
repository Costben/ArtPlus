package dev.artplus.mobile.state

import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.artplus.mobile.data.markPackageGenerated
import dev.artplus.mobile.data.scanRootPackages
import dev.artplus.mobile.data.updateGeneratedPackageCache
import dev.artplus.mobile.system.listArtPlusModifiedPackages
import dev.artplus.mobile.system.listArtPlusModifiedPackagesOrNull
import dev.artplus.mobile.system.restorePackageFromBackup

/**
 * `.artplusmodified` / `Backup/` 的界面侧编排：写入成功后同步本地集合、
 * 进入页面时静默对齐磁盘事实、以及按 `Backup/` 批量还原。
 */

/** 批量还原结果；failed 里的包保持原样（su 失败或目录已不存在）。 */
internal data class RestoreResult(
    val restored: List<String>,
    val failed: List<String>,
)

/**
 * Root 写入成功后同步本地状态：已生成缓存 + 接管标记集合。
 * `installWithRoot` 成功必定写下 `.artplusmodified`，所以这里可以直接乐观标记，
 * 不必等下一次 root 扫描。
 */
internal fun MainViewModel.markPackageWritten(prefs: SharedPreferences, packageName: String) {
    updatePicker {
        it.copy(
            generatedPackageNames = markPackageGenerated(prefs, it.generatedPackageNames, packageName),
            artPlusModifiedPackageNames = it.artPlusModifiedPackageNames + packageName,
        )
    }
}

/**
 * 静默对齐接管标记集合（一次 su）：只更新 `artPlusModifiedPackageNames`，
 * 不动「已生成」缓存与扫描状态。标记是磁盘事实，进入应用选择器或设置页时对一次，
 * 冷启动后徽标与还原入口才不会因为只有本地缓存而缺席。
 */
internal fun MainViewModel.refreshArtPlusModifiedPackagesAsync() {
    viewModelScope.launch {
        // 读不到（root 未授权等）时保持现状，避免把已有徽标清空
        val modified = withContext(Dispatchers.IO) { listArtPlusModifiedPackagesOrNull() } ?: return@launch
        updatePicker { it.copy(artPlusModifiedPackageNames = modified) }
    }
}

/**
 * 批量还原被 AM 接管过的图标包（viewModelScope 调度）。
 *
 * 目标集合现场经 root 重新列一次（不依赖可能过期的本地集合）；逐个执行
 * [restorePackageFromBackup]。还原后不靠猜改「已生成」，而是按磁盘重扫一次：
 * 有 `Backup/` 的包还原后目录里仍是 `recfg`/`recbg`（依旧「已生成」，只是不再带 AM 标记），
 * 只有标记的包还原后目录空了（确实没东西可生成）。这样列表不必等手动刷新才落到正确状态。
 */
internal fun MainViewModel.restoreArtPlusModifiedPackagesAsync(
    prefs: SharedPreferences,
    onProgress: (completed: Int, total: Int, current: String) -> Unit,
    onFinished: (RestoreResult) -> Unit,
) {
    if (shell.value.isBusy) {
        onFinished(RestoreResult(emptyList(), emptyList()))
        return
    }
    updateShell { it.copy(isBusy = true) }
    viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            val targets = listArtPlusModifiedPackages().sorted()
            if (targets.isEmpty()) {
                return@withContext RestoreResult(emptyList(), emptyList())
            }
            val restored = mutableListOf<String>()
            val failed = mutableListOf<String>()
            // 进度回调在 Main 上发出：调用方会写 Compose 状态并读当前值做 copy，跨线程读改写有丢失风险
            withContext(Dispatchers.Main) { onProgress(0, targets.size, "正在准备...") }
            targets.forEachIndexed { index, packageName ->
                withContext(Dispatchers.Main) {
                    onProgress(index, targets.size, "正在还原 ${index + 1}/${targets.size}: $packageName")
                }
                if (restorePackageFromBackup(packageName)) {
                    restored += packageName
                } else {
                    failed += packageName
                }
            }
            withContext(Dispatchers.Main) { onProgress(targets.size, targets.size, "还原完成") }
            if (restored.isNotEmpty()) {
                val restoredSet = restored.toSet()
                // 还原结果以磁盘为准：目录里还有 recfg/recbg 才算「已生成」
                val known = picker.value.generatedPackageNames + restoredSet
                val scanned = runCatching { scanRootPackages(known) }.getOrNull()
                if (scanned != null) {
                    val normalized = updateGeneratedPackageCache(prefs, scanned.generated)
                    updatePicker {
                        it.copy(
                            generatedPackageNames = normalized,
                            artPlusModifiedPackageNames = it.artPlusModifiedPackageNames - restoredSet,
                        )
                    }
                } else {
                    updatePicker {
                        it.copy(artPlusModifiedPackageNames = it.artPlusModifiedPackageNames - restoredSet)
                    }
                }
            }
            RestoreResult(restored = restored, failed = failed)
        }
        updateShell { it.copy(isBusy = false) }
        onFinished(result)
    }
}
