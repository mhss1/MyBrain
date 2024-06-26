plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(project(":core:preferences"))
    implementation(project(":core:di"))
    implementation(project(":core:alarm"))
    implementation(project(":core:widget"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.uuid)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)
    ksp(libs.koin.ksp.compiler)
}