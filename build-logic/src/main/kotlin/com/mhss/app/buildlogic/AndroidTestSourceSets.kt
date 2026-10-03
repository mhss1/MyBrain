package com.mhss.app.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidHostTestCompilation
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectCollection

val <T : Named> NamedDomainObjectCollection<T>.androidHostTest: T
    get() = getByName("androidHostTest")

fun KotlinMultiplatformAndroidLibraryTarget.configureHostTest(
    action: KotlinMultiplatformAndroidHostTestCompilation.() -> Unit,
) {
    compilations.withType(KotlinMultiplatformAndroidHostTestCompilation::class.java).configureEach {
        action()
    }
}
