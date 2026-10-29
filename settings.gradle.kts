pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // COUI17 不在 Maven Central，解析顺序：
        //   1. -PcouiRepo=<dir> 显式指定的本地发布目录
        //   2. 兄弟目录 ../coui 的本地发布：滚动坐标 couiVersion，那边每次提交都会重新发布
        //   3. 仓库内 vendored 的 lean 产物（CI 走这条）：钉住的坐标 couiVendoredVersion
        val couiRepo = providers.gradleProperty("couiRepo").orNull?.let(settingsDir::resolve)
            ?: settingsDir.resolve("../coui/build/repository/local").takeIf { it.isDirectory }
        gradle.extra["couiVendored"] = couiRepo == null
        maven(uri(couiRepo ?: settingsDir.resolve("third_party/coui-repo"))) {
            content { includeGroup("io.github.suqi8.coui.kmp") }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "ArtPlus"
include(":mobile")
