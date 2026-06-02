plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.kel6.booking"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kel6.booking"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true   // pakai ViewBinding, lebih aman dari findViewById
//        buildConfig = true   // supaya buildConfigField bisa dibaca
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ── Retrofit + Gson (HTTP client & JSON parser) ──────────────────
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ── Coroutines (async/await) ─────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

    // ── Navigation Component ─────────────────────────────────────────
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.5")

    // ── DataStore (simpan token JWT, lebih modern dari SharedPreferences) ──
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ── Glide (load gambar dari URL) ─────────────────────────────────
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ── Shimmer (efek loading skeleton) ─────────────────────────────
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // ── CircleImageView (foto profil bulat) ──────────────────────────
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}