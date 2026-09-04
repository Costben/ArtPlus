package dev.artplus.mobile

import android.content.pm.ApplicationInfo

/**
 * 纯逻辑辅助，供“显示系统应用”开关使用。
 * 保持可测试性：核心判定基于 flags 整数，避免直接依赖 Android Context。
 */
object AppVisibility {
    fun isSystemAppFlags(flags: Int): Boolean {
        return (flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
            (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
    }

    /**
     * 决定某应用是否应在列表中显示（不考虑 launchable，纯系统判定）。
     * @param flags 应用的 ApplicationInfo.flags
     * @param packageName 应用包名
     * @param selfPackageName ArtPlus 自身包名，永远显示
     * @param showSystemApps 开关状态
     */
    fun shouldShow(
        flags: Int,
        packageName: String,
        selfPackageName: String,
        showSystemApps: Boolean,
    ): Boolean {
        if (showSystemApps) return true
        if (packageName == selfPackageName) return true
        return !isSystemAppFlags(flags)
    }

    /**
     * 供选择器使用的最终可见性判定，包含 launchable 规则：
     * - 默认关闭时：仅显示可启动的非系统应用（及 ArtPlus 自身）
     * - 打开时：额外纳入所有系统应用（即使无 launcher），但不纳入无 launcher 的普通用户应用
     */
    fun shouldShowInPicker(
        flags: Int,
        packageName: String,
        selfPackageName: String,
        showSystemApps: Boolean,
        launchable: Boolean,
    ): Boolean {
        if (packageName == selfPackageName) return true
        val isSystem = isSystemAppFlags(flags)
        if (isSystem) {
            return showSystemApps
        } else {
            // 普通用户应用仅当可启动时显示，开关不影响
            return launchable
        }
    }

    fun shouldShowInPicker(
        info: ApplicationInfo,
        launchable: Boolean,
        showSystemApps: Boolean,
        selfPackageName: String,
    ): Boolean {
        return shouldShowInPicker(info.flags, info.packageName ?: "", selfPackageName, showSystemApps, launchable)
    }
}
