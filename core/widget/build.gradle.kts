plugins {
    alias(libs.plugins.mybrain.android.kmp.library)
}

kotlin {
    android {
        namespace = "com.mhss.app.core.widget"
        compileSdk {
            version = release(libs.versions.compileSdk.get().toInt())
        }
        minSdk = libs.versions.minSdk.get().toInt()
    }
}
