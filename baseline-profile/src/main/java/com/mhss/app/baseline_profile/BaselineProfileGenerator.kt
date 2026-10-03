package com.mhss.app.baseline_profile

import android.Manifest
import android.os.Build
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    private val targetPackageName = requireNotNull(
        InstrumentationRegistry.getArguments()
            .getString(BaselineProfileConstants.TARGET_APP_ID_ARGUMENT)
    ) { "targetAppId was not passed as an instrumentation argument" }

    private val contentDescriptionSelector = By.desc(Pattern.compile(".+"))

    @Before
    fun grantPermissions() {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        automation.grantRuntimePermission(targetPackageName, Manifest.permission.READ_CALENDAR)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            automation.grantRuntimePermission(targetPackageName, Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    @Test
    fun startup() {
        rule.collect(
            packageName = targetPackageName,
            includeInStartupProfile = true,
        ) {
            launchAndWaitForContent()
        }
    }

    @Test
    fun dashboardAndMainScreens() {
        rule.collect(
            packageName = targetPackageName,
            includeInStartupProfile = false,
        ) {
            launchAndWaitForContent()
            navigateToTab(BaselineProfileConstants.NAVIGATE_DASHBOARD)
            navigateToTab(BaselineProfileConstants.NAVIGATE_SPACES)
            val spacesTitle = awaitObject(By.res(BaselineProfileConstants.SCREEN_TITLE)).text

            BaselineProfileConstants.MAIN_SCREEN_TAGS.forEach { tag ->
                val card = findSpaceCard(tag)
                val title = card.readContentDescription(tag)
                card.click()
                check(device.wait(Until.gone(By.res(tag)), BaselineProfileConstants.UI_TIMEOUT_MILLIS)) {
                    "Space card $tag remained visible after navigation"
                }
                awaitScreenTitle(title)
                device.pressBack()
                awaitScreenTitle(spacesTitle)
                awaitObject(By.res(BaselineProfileConstants.NAVIGATE_SPACES))
            }
        }
    }

    private fun MacrobenchmarkScope.launchAndWaitForContent() {
        pressHome()
        startActivityAndWait()
        awaitObject(By.res(BaselineProfileConstants.SCREEN_TITLE))
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.navigateToTab(tag: String) {
        val tab = awaitObject(By.res(tag))
        val title = tab.readContentDescription(tag)
        tab.click()
        awaitScreenTitle(title)
    }

    private fun UiObject2.readContentDescription(tag: String): String {
        return requireNotNull(
            contentDescription ?: findObject(contentDescriptionSelector)?.contentDescription
        ) { "UI element $tag and its children have no content description" }
    }

    private fun MacrobenchmarkScope.awaitScreenTitle(title: String) {
        awaitObject(By.res(BaselineProfileConstants.SCREEN_TITLE).text(title))
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.findSpaceCard(tag: String): UiObject2 {
        val selector = By.res(tag)
        repeat(BaselineProfileConstants.MAX_SPACES_SCROLLS) {
            device.findObject(selector)?.let { return it }
            val grid = awaitObject(By.res(BaselineProfileConstants.SPACES_GRID))
            grid.setGestureMargin(device.displayWidth / 5)
            if (!grid.scroll(Direction.DOWN, BaselineProfileConstants.SCROLL_PERCENT)) {
                return awaitObject(selector)
            }
        }
        return awaitObject(selector)
    }

    private fun MacrobenchmarkScope.awaitObject(selector: BySelector): UiObject2 {
        return requireNotNull(
            device.wait(Until.findObject(selector), BaselineProfileConstants.UI_TIMEOUT_MILLIS)
        ) { "Timed out waiting for $selector" }
    }
}
