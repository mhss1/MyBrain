import com.mhss.app.buildlogic.androidHostTest
import com.mhss.app.buildlogic.configureHostTest

plugins {
    alias(libs.plugins.mybrain.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.mokkery)
}

kotlin {
    android {
        namespace = "com.mhss.app.localsync"
        compileSdk {
            version = release(libs.versions.compileSdk.get().toInt())
        }
        minSdk = libs.versions.minSdk.get().toInt()
        androidResources.enable = true
        configureHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.database)
            implementation(projects.core.preferences)
            implementation(projects.notes.domain)
            implementation(projects.tasks.domain)
            implementation(projects.diary.domain)
            implementation(projects.bookmarks.domain)
            implementation(projects.core.datetime)
            implementation(libs.androidx.room3.runtime)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.bundles.koin)

            implementation(libs.ktor.core)
            implementation(libs.ktor.serialization)
            implementation(libs.ktor.content.negotiation)
            implementation(libs.ktor.logging)
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.cio)
            implementation(libs.ktor.server.websockets)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.ktor.client.websockets)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.koin.android.workmanager)
            implementation(libs.androidx.work.runtime.ktx)

            implementation("${libs.zstd.jni.get()}@aar")
            implementation(libs.zxing.core)

            implementation(libs.ktor.okhttp)
        }

        androidHostTest.dependencies {
            implementation(libs.androidx.work.testing)
            implementation(libs.androidx.test.core)
            implementation(libs.junit)
            implementation(libs.robolectric)
        }
    }
}

koinCompiler {
    compileSafety = false
}
