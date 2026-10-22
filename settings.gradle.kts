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
        // COUI17 不在 Maven Central：默认用仓库内 vendored 的 lean 产物（CI 可直接解析）；
        // 本地改了 COUI 源码时用 -PcouiRepo=../coui/build/repository/local 指回本地发布目录。
        val couiRepo = providers.gradleProperty("couiRepo").getOrElse("third_party/coui-repo")
        maven(uri(settingsDir.resolve(couiRepo))) {
            content { includeGroup("io.github.suqi8.coui.kmp") }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "ArtPlus"
include(":mobile")
