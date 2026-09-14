plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val iqtansApiBaseUrl = providers.gradleProperty("IQTANS_API_BASE_URL").orElse("").get()
val escapedApiBaseUrl = iqtansApiBaseUrl.replace("\\", "\\\\").replace("\"", "\\\"")

android {
    namespace = "com.iqtans.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.iqtans.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.2.0"

        vectorDrawables { useSupportLibrary = true }
        buildConfigField("String", "IQTANS_API_BASE_URL", "\"$escapedApiBaseUrl\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
