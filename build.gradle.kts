plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.novawolf.videovoid"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.novawolf.videovoid"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"
    }
    buildTypes { release { isMinifyEnabled = false } }
    packaging { jniLibs { useLegacyPackaging = true } }
    buildFeatures { compose = true }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.09.00"))
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.3")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("dev.ffmpegkit-maintained:yt-dlp-android:2.0.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
