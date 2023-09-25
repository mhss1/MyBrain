package com.mhss.app.mybrain.util

object Constants {

    // Alarm & Notification
    const val REMINDERS_CHANNEL_ID = "reminders_notification_channel"
    const val TASK_ID_EXTRA = "task_Id"
    const val ACTION_COMPLETE = "com.mhss.app.mybrain.COMPLETE_ACTION"

    // Settings
    const val SETTINGS_PREFERENCES = "settings_preferences"
    const val SETTINGS_THEME_KEY = "settings_theme"
    const val DEFAULT_START_UP_SCREEN_KEY = "default_start_up_screen"
    const val SHOW_COMPLETED_TASKS_KEY = "show_completed_tasks"
    const val TASKS_ORDER_KEY = "tasks_order"
    const val NOTE_VIEW_KEY = "note_view"
    const val BOOKMARK_VIEW_KEY = "bookmark_view"
    const val NOTES_ORDER_KEY = "notes_order"
    const val BOOKMARK_ORDER_KEY = "bookmark_order"
    const val DIARY_ORDER_KEY = "diary_order"
    const val EXCLUDED_CALENDARS_KEY = "excluded_calendars"
    const val APP_FONT_KEY = "app_font"
    const val BLOCK_SCREENSHOTS_KEY = "block_screen_shots"

    // Navigation
    const val TASK_ID_ARG = "task_id"
    const val TASK_DETAILS_URI = "app://com.mhss.app.mybrain/task_details"
    const val ADD_TASK_ARG = "add_task"
    const val TASKS_SCREEN_URI = "app://com.mhss.app.mybrain/tasks"
    const val CALENDAR_SCREEN_URI = "app://com.mhss.app.mybrain/calendar"
    const val CALENDAR_DETAILS_SCREEN_URI = "app://com.mhss.app.mybrain/calendar_event_details"
    const val NOTE_ID_ARG = "note_id"
    const val BOOKMARK_ID_ARG = "bookmark_id"
    const val DIARY_ID_ARG = "diary_id"
    const val CALENDAR_EVENT_ARG = "calendar_event"
    const val FOLDER_ID = "folder_id"

    // lINKS
    const val PROJECT_GITHUB_LINK = "https://github.com/mhss1/ByBrain"
    const val PROJECT_ROADMAP_LINK = "https://github.com/users/mhss1/projects/2/"
    const val PRIVACY_POLICY_LINK = "https://github.com/mhss1/ByBrain/blob/master/privacy-policy.md"
    const val GITHUB_ISSUES_LINK = "https://github.com/mhss1/ByBrain/issues"
    const val GITHUB_RELEASES_LINK = "https://github.com/mhss1/ByBrain/releases"

    // Backup
    const val EXPORT_DIR = "MyBrain Export"
    const val BACKUP_NOTES_FILE_NAME = "mybrain_notes.txt"
    const val BACKUP_TASKS_FILE_NAME = "mybrain_tasks.txt"
    const val BACKUP_DIARY_FILE_NAME = "mybrain_diary.txt"
    const val BACKUP_BOOKMARKS_FILE_NAME = "mybrain_bookmarks.txt"

}