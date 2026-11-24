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
        //   2. 兄弟目录 ../coui/build/repository/local（未指定 -Pcoui.useRemote=true 时优先）
        //   3. 远端 Costben/coui17 的 maven-repo 孤儿分支（CI 与外部克隆自动走这里）
        val forceRemoteCoui = providers.gradleProperty("coui.useRemote").orNull.equals("true", ignoreCase = true)
        val couiRepo = providers.gradleProperty("couiRepo").orNull?.let(settingsDir::resolve)
            ?: settingsDir.resolve("../coui/build/repository/local").takeIf { it.isDirectory && !forceRemoteCoui }
        exclusiveContent {
            forRepository {
                maven {
                    if (couiRepo != null) {
                        name = "localCouiRepo"
                        url = uri(couiRepo)
                    } else {
                        name = "coui17MavenRepo"
                        url = uri("https://raw.githubusercontent.com/Costben/coui17/maven-repo/coui-repo")
                    }
                }
            }
            filter {
                includeGroup("io.github.suqi8.coui.kmp")
            }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "ArtPlus"
include(":mobile")
// LSPosed 辅助模块：桌面液态玻璃 + 可变图标（morph）本地素材接管。见 enhancer/。
include(":enhancer")
// 动态改变图标的测试 App
include(":morphdemo")
