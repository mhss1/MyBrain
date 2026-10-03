plugins {
    alias(libs.plugins.mybrain.android.kmp.library)
    alias(libs.plugins.koin.compiler)
}

kotlin {
    android {
        namespace = "com.mhss.app.datetime"
        compileSdk {
            version = release(libs.versions.compileSdk.get().toInt())
        }
        minSdk = libs.versions.minSdk.get().toInt()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.datetime)
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.bundles.koin)
            }
        }
    }
}

koinCompiler {
    compileSafety = false
}
