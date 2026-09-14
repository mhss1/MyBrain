import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.mybrain.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.koin.compiler)
}

kotlin {
    android {
        namespace = "com.mhss.app.ai.presentation"
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
                implementation(projects.ai.domain)
                implementation(projects.notes.domain)
                implementation(projects.tasks.domain)
                implementation(projects.calendar.domain)
                implementation(projects.core.datetime)
                implementation(projects.core.ui)
                implementation(projects.core.preferences)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.bundles.koin)
                implementation(libs.bundles.compose)

                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.viewmodel.ktx)

                implementation(libs.kotlinx.serialization.json)
            }
        }
    }


    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}

koinCompiler {
    compileSafety = false
}
