plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.liberty.mediaviewer"
    compileSdk = 35

    defaultConfig {
        minSdk = 25
    }

    lint {
        targetSdk = 35
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    api(libs.appcompat)
    api(libs.transition)
    implementation(libs.material)
    implementation(libs.photoview)
    implementation(libs.media3)
    implementation(libs.media3ui)
    implementation(libs.media3dash)
    implementation(libs.media3hls)
}
