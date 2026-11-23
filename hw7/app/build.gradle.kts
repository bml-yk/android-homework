plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "cn.edu.sicnucs.bml.servicemusicplayer"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cn.edu.sicnucs.bml.servicemusicplayer"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.material)
    testImplementation(libs.junit)
}
