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
        // COUI17 不在 Maven Central：先在 ../coui 执行 publishAllToLocalRepository。
        val couiRepo = providers.gradleProperty("couiRepo").getOrElse("../coui/build/repository/local")
        maven(uri(settingsDir.resolve(couiRepo))) {
            content { includeGroup("io.github.suqi8.coui.kmp") }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "ArtPlus"
include(":mobile")
