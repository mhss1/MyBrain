package com.mhss.app.baseline_profile

import android.Manifest
import android.os.Build
import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmarks {

    @get:Rule
    val rule = MacrobenchmarkRule()

    private val targetPackageName = requireNotNull(
        InstrumentationRegistry.getArguments()
            .getString(BaselineProfileConstants.TARGET_APP_ID_ARGUMENT)
    ) { "targetAppId was not passed as an instrumentation argument" }

    @Test
    fun startupWithoutBaselineProfile() = benchmark(CompilationMode.None())

    @Test
    fun startupWithBaselineProfile() = benchmark(
        CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require)
    )

    private fun benchmark(compilationMode: CompilationMode) {
        rule.measureRepeated(
            packageName = targetPackageName,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = BaselineProfileConstants.STARTUP_BENCHMARK_ITERATIONS,
            setupBlock = {
                grantPermissions()
                pressHome()
            },
            measureBlock = {
                startActivityAndWait()
                check(
                    device.wait(
                        Until.hasObject(By.res(BaselineProfileConstants.SCREEN_TITLE)),
                        BaselineProfileConstants.UI_TIMEOUT_MILLIS,
                    )
                ) { "Timed out waiting for startup content" }
                device.waitForIdle()
            },
        )
    }

    private fun grantPermissions() {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        automation.grantRuntimePermission(targetPackageName, Manifest.permission.READ_CALENDAR)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            automation.grantRuntimePermission(targetPackageName, Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
