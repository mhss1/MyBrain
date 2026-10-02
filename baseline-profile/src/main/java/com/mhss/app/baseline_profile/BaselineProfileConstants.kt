package com.mhss.app.baseline_profile

internal object BaselineProfileConstants {
    const val TARGET_APP_ID_ARGUMENT = "targetAppId"
    const val UI_TIMEOUT_MILLIS = 10_000L
    const val STARTUP_BENCHMARK_ITERATIONS = 10
    const val MAX_SPACES_SCROLLS = 6
    const val SCROLL_PERCENT = 0.8f
    const val SCREEN_TITLE = "screen_title"
    const val SPACES_GRID = "spaces_grid"
    const val NAVIGATE_DASHBOARD = "navigate_dashboard"
    const val NAVIGATE_SPACES = "navigate_spaces"
    val MAIN_SCREEN_TAGS = listOf(
        "open_notes",
        "open_tasks",
        "open_diary",
        "open_bookmarks",
        "open_calendar",
        "open_assistant",
    )
}
