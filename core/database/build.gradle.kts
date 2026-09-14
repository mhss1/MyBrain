plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.room3)
}

kotlin {
    android {
        namespace = "com.mhss.app.database"
        compileSdk {
            version = release(libs.versions.compileSdk.get().toInt())
        }
        minSdk = libs.versions.minSdk.get().toInt()
        androidResources.enable = true
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.tasks.domain)
                implementation(projects.notes.domain)
                implementation(projects.bookmarks.domain)
                implementation(projects.diary.domain)
                implementation(projects.core.alarm)

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.androidx.room3.runtime)
                implementation(libs.androidx.room3.paging)
                implementation(libs.androidx.sqlite.bundled)
                implementation(libs.kotlinx.serialization.json)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.bundles.koin)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.androidx.room3.testing)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.koin.android)
            }
        }

        val androidHostTest by getting
        androidHostTest.dependencies {
            implementation(libs.androidx.test.core)
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.sqlite.framework)
            implementation(libs.junit)
            implementation(libs.robolectric)
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room3.compiler)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

androidComponents {
    onVariants { variant ->
        variant.hostTests.values.forEach { test ->
            test.sources.assets?.addStaticSourceDirectory("$projectDir/schemas")
        }
    }
}

koinCompiler {
    compileSafety = false
}
