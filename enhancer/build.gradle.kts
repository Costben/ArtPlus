import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.artplus.enhancer"

    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "dev.artplus.enhancer"
        // LibXposed service 自身 minSdk 26；桌面侧 hook 只跑在 ColorOS 17（API 37）上。
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            val keystoreFile = rootProject.file("mobile/release.jks")
            val localProperties = Properties().apply {
                val propFile = rootProject.file("local.properties")
                if (propFile.exists()) {
                    propFile.inputStream().use { load(it) }
                }
            }
            val storePass = System.getenv("KEYSTORE_PASSWORD")
                ?: localProperties.getProperty("KEYSTORE_PASSWORD")
            val keyPass = System.getenv("KEY_PASSWORD")
                ?: localProperties.getProperty("KEY_PASSWORD")
                ?: storePass
            val resolvedKeyAlias = System.getenv("KEY_ALIAS")
                ?: localProperties.getProperty("KEY_ALIAS")
                ?: "artplus"

            if (keystoreFile.exists() && !storePass.isNullOrBlank()) {
                storeFile = keystoreFile
                storePassword = storePass
                keyAlias = resolvedKeyAlias
                keyPassword = keyPass
            } else {
                val debug = getByName("debug")
                storeFile = debug.storeFile
                storePassword = debug.storePassword
                keyAlias = debug.keyAlias
                keyPassword = debug.keyPassword
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // 模块全靠反射挂 hook，混淆只会让类名/方法名与桌面版本对不上，这里保持不混淆。
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "**/*.kotlin_builtins"
            excludes += "kotlin-tooling-metadata.json"
        }
    }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // LibXposed API 102：模块入口与 hook 接口。
    compileOnly("io.github.libxposed:api:102.0.0")
    // 设置页经 XposedService 写 LSPosed 的远程配置表，桌面侧读同一份数据。
    implementation("io.github.libxposed:service:102.0.0")

    // COUI 浮层依赖 1.11+ 的 ComponentActivity（NavigationEventDispatcherOwner）。
    implementation("androidx.activity:activity-compose:1.13.0")

    // COUI17：仓库来源与坐标见根 settings.gradle.kts。
    val couiVersionKey = if (gradle.extra["couiVendored"] == true) "couiVendoredVersion" else "couiVersion"
    val couiVersion = providers.gradleProperty(couiVersionKey).get()
    implementation("io.github.suqi8.coui.kmp:coui-ui:$couiVersion")
    implementation("io.github.suqi8.coui.kmp:coui-preference:$couiVersion")
    implementation("io.github.suqi8.coui.kmp:coui-icons:$couiVersion")
}
