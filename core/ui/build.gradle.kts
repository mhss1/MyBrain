import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.compose.compiler)
}

kotlin {
    android {
        namespace = "com.mhss.app.ui"
        compileSdk {
            version = release(libs.versions.compileSdk.get().toInt())
        }
        minSdk = libs.versions.minSdk.get().toInt()
        androidResources {
            enable = true
        }
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.tasks.domain)
                implementation(projects.notes.domain)
                implementation(projects.settings.domain)
                implementation(projects.core.datetime)
                implementation(projects.core.preferences)

                implementation(project.dependencies.platform(libs.koin.bom))

                implementation(libs.bundles.compose)
                api(libs.androidx.paging.compose)

                implementation(libs.kotlinx.serialization.json)

                implementation(libs.markdown.renderer)

                api(libs.liquid)

                api(libs.squircle.shape)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.compose.ui.tooling.preview)
            }
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.mhss.app.ui"
    generateResClass = always
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}
