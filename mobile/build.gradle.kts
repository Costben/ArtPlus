import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.artplus.mobile"

    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "dev.artplus.mobile"
        minSdk = 26
        targetSdk = 37
        versionCode = 17
        versionName = "1.5.0-alpha4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    signingConfigs {
        create("release") {
            val keystoreFile = file("release.jks")
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
        debug {
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }

        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isDebuggable = false
            manifestPlaceholders["usesCleartextTraffic"] = "false"
        }
    }

    buildFeatures {
        compose = true
    }

    // Epic v3 S0.4：lint 只拦「新增」。基线在 lint-baseline.xml，随本 Slice 入库。
    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = true
        checkDependencies = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.4.10")
    testImplementation("org.json:json:20240303")
    // S3.8：仪器化冒烟只依赖已缓存的 androidx.test.*（ui-test-junit4 未在本地缓存，离线拉不到）
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:core:1.7.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.4.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    // P2 ViewModel 地基：lifecycle-bom 未发布（dl.google.com 全版本 404），改直钉版本；
    // 2.9.4 与 activity-compose 1.13.0 同期，本地缓存可 resolve。
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")

    implementation(platform("androidx.compose:compose-bom:2026.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")

    implementation("com.caverock:androidsvg-aar:1.4")
    // 热修复2-16KB：onnxruntime-android 1.20.0 的 libonnxruntime4j_jni.so ELF LOAD 段为 4KB 对齐(0x1000)，
    // Android 16 可调试包弹窗“LOAD 区段未对齐”；1.21.1 起该 .so 已是 16KB 对齐(0x4000，readelf 实锤最小修复版)。
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.21.1")
    // 热修复2-16KB：androidx.graphics:graphics-path 1.0.1 -> 1.1.0（16KB 对齐版本，直钉显式提升传递依赖）。
    implementation("androidx.graphics:graphics-path:1.1.0")
    // COUI17：仓库来源与对应坐标见 settings.gradle.kts。
    val couiVersionKey = if (gradle.extra["couiVendored"] == true) "couiVendoredVersion" else "couiVersion"
    val couiVersion = providers.gradleProperty(couiVersionKey).get()
    implementation("io.github.suqi8.coui.kmp:coui-ui:$couiVersion")
    implementation("io.github.suqi8.coui.kmp:coui-icons:$couiVersion")
    implementation("io.github.suqi8.coui.kmp:coui-blur:$couiVersion")
    implementation("io.github.suqi8.coui.kmp:coui-preference:$couiVersion")
    implementation("io.github.kyant0:shapes:1.2.0")
}
