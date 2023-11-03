
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("com.google.dagger.hilt.android")
    id ("com.google.devtools.ksp")
}

android {
    namespace = "com.mhss.app.mybrain"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mhss.app.mybrain"
        minSdk = 26
        targetSdk = 34
        versionCode = 7
        versionName = "1.0.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "MyBrain Debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"

    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    androidResources {
        generateLocaleConfig = true
    }
}

dependencies {
    val roomVersion = "2.6.0"
    val coroutinesVersion = "1.7.3"
    val lifecycleVersion = "2.6.2"
    val workVersion = "2.8.1"
    implementation(platform("androidx.compose:compose-bom:2023.09.01"))

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.activity:activity-compose:1.8.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Compose navigation
    implementation("androidx.navigation:navigation-compose:2.7.4")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    // Room
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    //Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-android-compiler:2.48")
    ksp("androidx.hilt:hilt-compiler:1.0.0")
    implementation("androidx.hilt:hilt-work:1.0.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // Gson
    implementation("com.google.code.gson:gson:2.10")

    // Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Accompanist libraries
    implementation("com.google.accompanist:accompanist-flowlayout:0.23.1")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.23.1")
    implementation("com.google.accompanist:accompanist-permissions:0.23.1")

    // Compose MarkDown
    implementation("com.github.jeziellago:compose-markdown:0.3.4")

    // Compose Glance (Widgets)
    implementation("androidx.glance:glance-appwidget:1.0.0")
    implementation("androidx.glance:glance-material:1.0.0")

    //Moshi
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:$workVersion")

    // Compose live data
    implementation("androidx.compose.runtime:runtime-livedata")

    // DocumentFile
    implementation("androidx.documentfile:documentfile:1.0.1")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
