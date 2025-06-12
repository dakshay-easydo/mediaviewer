plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.liberty.sample"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.stfalcon.stfalconimageviewersample"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(project(":mediaviewer"))
    implementation(libs.appcompat)
    implementation(libs.cardview)
    implementation(libs.transition)
    implementation(libs.constraintLayout)
    implementation(libs.coordinatorLayout)
    implementation(libs.glide)
}
