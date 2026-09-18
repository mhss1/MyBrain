plugins {
    alias(libs.plugins.mybrain.android.kmp.library)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.mokkery)
}

kotlin {
    android {
        namespace = "com.mhss.app.settings.data"
        compileSdk {
            version = release(libs.versions.compileSdk.get().toInt())
        }
        minSdk = libs.versions.minSdk.get().toInt()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.storage)
                implementation(projects.core.preferences)
                implementation(projects.settings.domain)
                implementation(projects.notes.domain)
                implementation(projects.tasks.domain)
                implementation(projects.diary.domain)
                implementation(projects.bookmarks.domain)

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.bundles.koin)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        androidMain {
            dependencies {
                implementation(projects.core.database)

                implementation(libs.koin.android.workmanager)

                implementation(libs.androidx.work.runtime.ktx)
            }
        }
    }
}

koinCompiler {
    compileSafety = false
}
