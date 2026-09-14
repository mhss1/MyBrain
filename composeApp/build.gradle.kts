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
        namespace = "com.mhss.app.mybrain.library"
        compileSdk {
            version = release(libs.versions.compileSdk.get().toInt())
        }
        minSdk = libs.versions.minSdk.get().toInt()
        androidResources {
            enable = false
        }
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.notes.presentation)
                implementation(projects.tasks.presentation)
                implementation(projects.bookmarks.presentation)
                implementation(projects.calendar.presentation)
                implementation(projects.diary.presentation)
                implementation(projects.settings.presentation)
                implementation(projects.ai.presentation)

                implementation(projects.notes.data)
                implementation(projects.tasks.data)
                implementation(projects.bookmarks.data)
                implementation(projects.diary.data)
                implementation(projects.calendar.data)
                implementation(projects.ai.data)
                implementation(projects.settings.data)

                implementation(projects.notes.domain)
                implementation(projects.tasks.domain)
                implementation(projects.calendar.domain)
                implementation(projects.diary.domain)
                implementation(projects.bookmarks.domain)
                implementation(projects.settings.domain)
                implementation(projects.ai.domain)

                implementation(projects.core.ui)
                implementation(projects.core.di)
                implementation(projects.core.alarm)
                implementation(projects.core.database)
                implementation(projects.localSync)
                implementation(projects.core.preferences)
                implementation(projects.core.storage)
                implementation(projects.core.datetime)

                implementation(project.dependencies.platform(libs.compose.bom))
                implementation(libs.bundles.compose)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.viewmodel.ktx)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.bundles.koin)

                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        androidMain {
            dependencies {
                implementation(projects.widget)
                implementation(projects.appfunctions)

                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.glance.appwidget)
                implementation(libs.androidx.glance)

                implementation(libs.androidx.work.runtime.ktx)
                implementation(libs.androidx.biometric)

                implementation(libs.koin.android)
                implementation(libs.koin.android.workmanager)

                implementation(libs.androidx.datastore.preferences)
                implementation(libs.zxing.core)
            }
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}

koinCompiler {
    compileSafety = true
}
