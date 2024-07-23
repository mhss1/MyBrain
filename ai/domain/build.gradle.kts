plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(project(":core:di"))
    api(project(":core:network"))
    implementation(project(":core:preferences"))

    implementation(libs.kotlinx.coroutines.core)

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)
    ksp(libs.koin.ksp.compiler)
}