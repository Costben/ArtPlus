package dev.artplus.mobile

import java.util.Random

interface BatchSampleTarget {
    val packageName: String
    val launchable: Boolean
}

object BatchPreviewSampler {
    const val DEFAULT_BATCH_PREVIEW_COUNT = 20
    const val MIN_BATCH_PREVIEW_COUNT = 4
    const val MAX_BATCH_PREVIEW_COUNT = 60

    /**
     * 随机抓取指定数量的应用：
     * (a) 优先抓取当前启动器中未生成的应用 (launchable && packageName !in generatedPackageNames)
     * (b) 其次随机抓取已生成的应用 (launchable && packageName in generatedPackageNames)
     *
     * @param candidates 全部候选应用列表
     * @param generatedPackageNames 已生成图标的包名集合
     * @param count 抓取数量 (限制在 MIN_BATCH_PREVIEW_COUNT..MAX_BATCH_PREVIEW_COUNT 之间)
     * @param selfPackageName 自身包名（排除应用自身）
     * @param random 随机生成器（可注入以便测试）
     */
    fun <T : BatchSampleTarget> sample(
        candidates: List<T>,
        generatedPackageNames: Set<String>,
        count: Int = DEFAULT_BATCH_PREVIEW_COUNT,
        selfPackageName: String? = null,
        random: Random = Random(),
    ): List<T> {
        val targetCount = count.coerceIn(MIN_BATCH_PREVIEW_COUNT, MAX_BATCH_PREVIEW_COUNT)
        val validApps = candidates.filter { app ->
            app.launchable && (selfPackageName == null || app.packageName != selfPackageName)
        }

        val ungenerated = validApps.filter { it.packageName !in generatedPackageNames }.shuffled(random)
        val generated = validApps.filter { it.packageName in generatedPackageNames }.shuffled(random)

        val result = ArrayList<T>(minOf(validApps.size, targetCount))
        result.addAll(ungenerated.take(targetCount))

        val remainingNeeded = targetCount - result.size
        if (remainingNeeded > 0) {
            result.addAll(generated.take(remainingNeeded))
        }

        return result
    }
}
