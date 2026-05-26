plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.jp.widgetenglish"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.jp.widgetenglish"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Glance / Widget
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")

    // AndroidX base
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Credential Manager / Google ID
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Room
    implementation("androidx.room:room-runtime:2.8.3")
    implementation("androidx.room:room-ktx:2.8.3")
    ksp("androidx.room:room-compiler:2.8.3")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Google Play Services
    // Necesario para evitar errores tipo:
    // Cannot access 'AbstractSafeParcelable' which is a supertype of 'FirebaseUser'
    implementation("com.google.android.gms:play-services-auth:21.5.1")
    implementation("com.google.android.gms:play-services-base:18.7.1")
    implementation("com.google.android.gms:play-services-basement:18.7.1")
    implementation("com.google.android.gms:play-services-tasks:18.3.1")

    // Lifecycle / ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    // Coroutines para Firebase Tasks
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}