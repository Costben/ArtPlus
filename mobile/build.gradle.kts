plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.artplus.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.artplus.mobile"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += listOf("arm64-v8a")
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
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.animation:animation:1.10.5")
    implementation("com.caverock:androidsvg-aar:1.4")
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.20.0")
    implementation("top.yukonga.miuix.kmp:miuix-android:0.8.8")
}
