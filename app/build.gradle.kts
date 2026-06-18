plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.ql_homestay"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.ql_homestay"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    // Navigation Component
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // RecyclerView
    implementation(libs.recyclerview)

    // CardView
    implementation(libs.cardview)

    // MPAndroidChart (BarChart/LineChart/PieChart cho module Thống kê)
    implementation(libs.mpandroidchart)

    // CircleImageView (Avatar tròn)
    implementation(libs.circleimageview)
}