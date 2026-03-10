// app/build.gradle.kts
// Scripture Widgets - App module build file

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.scripturewidgets"
    compileSdk = 35
    signingConfigs {
        create("release") {
            storeFile = file("C:\\Users\\ivanf\\AndroidStudioProjects\\ScriptureWidgets\\scripture-widgets.jks")
            storePassword = "lepotan"
            keyAlias = "widget"
            keyPassword = "lepotan"
        }
    defaultConfig {
        applicationId = "com.scripturewidgets"
        minSdk = 26          // Android 8.0 â€” covers 95%+ of active devices
        targetSdk = 35       // Android 15
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // API key placeholder â€” replace with your key from scripture.api.bible
        buildConfigField("String", "BIBLE_API_KEY", "\"YOUR_API_KEY_HERE\"")
        buildConfigField("String", "BIBLE_API_BASE_URL", "\"https://api.scripture.api.bible/v1/\"")

        // Room schema export directory
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }


    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Retrofit / Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Kotlin
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // DataStore
    implementation(libs.datastore.preferences)

    // Glance Widgets
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // WorkManager
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // Coil
    implementation(libs.coil.compose)

    // Material Components (for XML themes)
    implementation(libs.material)

    // Billing
    implementation(libs.billing)

    // Accompanist
    implementation(libs.accompanist.permissions)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}}
