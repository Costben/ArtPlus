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
        versionCode = 12
        versionName = "1.4.2"

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
    implementation("com.composables:icons-lucide-android:1.1.0")
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.20.0")
    implementation("top.yukonga.miuix.kmp:miuix-ui-android:0.9.3")
    implementation("top.yukonga.miuix.kmp:miuix-icons-android:0.9.3")
    implementation("top.yukonga.miuix.kmp:miuix-blur-android:0.9.3")
    implementation("top.yukonga.miuix.kmp:miuix-preference-android:0.9.3")
    implementation("io.github.kyant0:shapes:1.2.0")
}
