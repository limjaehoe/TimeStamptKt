plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    kotlin("kapt") // kapt 플러그인
}

android {
    namespace = "com.androidkotlin.timestampkt.data"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.androidkotlin.timestampkt.data"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 필요한 경우 멀티덱스 활성화
        multiDexEnabled = true
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    // Lint 경고 처리
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    // Compose 컴파일러 옵션 추가
    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.0" // Kotlin 버전과 일치하도록 설정
    }
}

dependencies {
    implementation(libs.xlog)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.activity)
    kapt(libs.hilt.compiler)

    implementation(libs.androidx.room.paging) // 추가

    // Timber for logging
    implementation(libs.timber)

    //dataStore
    implementation("androidx.datastore:datastore-preferences:1.1.6")
    implementation("androidx.datastore:datastore-preferences-core:1.1.6")
    //protoStore
    implementation("androidx.datastore:datastore:1.1.6")
    implementation("androidx.datastore:datastore-core:1.1.6")

    // Room 데이터베이스 - 주석 처리 제거하고 kapt 추가
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1") // 이 부분이 활성화되어야 합니다!
    implementation("androidx.room:room-paging:2.6.1") // 이 줄을 추가

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // Paging 라이브러리
    implementation("androidx.paging:paging-runtime:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")
}