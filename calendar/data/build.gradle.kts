plugins {
    alias(libs.plugins.mybrain.android.kmp.library)
    alias(libs.plugins.koin.compiler)
}

kotlin {
    android {
        namespace = "com.mhss.app.calendar.data"
        compileSdk {
            version = release(libs.versions.compileSdk.get().toInt())
        }
        minSdk = libs.versions.minSdk.get().toInt()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.ui)
                implementation(projects.core.datetime)
                implementation(projects.calendar.domain)

                implementation(libs.kotlinx.coroutines.core)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.bundles.koin)
            }
        }
    }
}

koinCompiler {
    compileSafety = false
}
