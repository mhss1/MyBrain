import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.mybrain.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.koin.compiler)
}

kotlin {
    android {
        namespace = "com.mhss.app.tasks.presentation"
        compileSdk {
            version = release(libs.versions.compileSdk.get().toInt())
        }
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        lint {
            disable += "NullSafeMutableLiveData"
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.tasks.domain)
                implementation(projects.core.datetime)
                implementation(projects.core.ui)
                implementation(projects.core.preferences)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.bundles.koin)
                implementation(libs.bundles.compose)
                implementation(libs.androidx.paging.compose)

                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.viewmodel.ktx)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.core.ktx)
                implementation(libs.koin.android)
            }
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}

koinCompiler {
    compileSafety = false
}
