
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("com.google.dagger.hilt.android")
    id ("com.google.devtools.ksp")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.mhss.app.mybrain"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mhss.app.mybrain"
        minSdk = 26
        targetSdk = 34
        versionCode = 8
        versionName = "1.0.7"

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
            isDebuggable = false
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
        kotlinCompilerExtensionVersion = "1.5.12"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    androidResources {
        generateLocaleConfig = true
    }
    lint {
        disable.add("MissingTranslation")
    }
}

dependencies {
    val roomVersion = "2.6.1"
    val coroutinesVersion = "1.8.0"
    val lifecycleVersion = "2.7.0"
    val workVersion = "2.9.0"
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    implementation("androidx.activity:activity-compose:1.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Compose navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    //Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.49")
    ksp("com.google.dagger:hilt-android-compiler:2.49")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Accompanist libraries
    implementation("com.google.accompanist:accompanist-flowlayout:0.23.1")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.23.1")
    implementation("com.google.accompanist:accompanist-permissions:0.23.1")

    // Compose MarkDown
    implementation("com.github.jeziellago:compose-markdown:0.5.0")

    // Compose Glance (Widgets)
    implementation("androidx.glance:glance-appwidget:1.1.0-beta02")
    implementation("androidx.glance:glance-material:1.1.0-beta02")

    //Moshi
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:$workVersion")

    // Compose live data
    implementation("androidx.compose.runtime:runtime-livedata")

    // DocumentFile
    implementation("androidx.documentfile:documentfile:1.0.1")

    // Biometric
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // Kotlinx serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
