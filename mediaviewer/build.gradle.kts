plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
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

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.dakshay-easydo"
                artifactId = "mediaviewer"
                version = "1.0.0"
            }
        }
    }
}

dependencies {
    api(libs.appcompat)
    api(libs.transition)
    implementation(libs.material)
    implementation(libs.glide)
    implementation(libs.photoview)
    implementation(libs.media3)
    implementation(libs.media3ui)
    implementation(libs.media3dash)
    implementation(libs.media3hls)
}
