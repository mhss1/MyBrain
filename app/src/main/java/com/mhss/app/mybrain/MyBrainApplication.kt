package com.mhss.app.mybrain

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.mhss.app.alarm.di.AlarmModule
import com.mhss.app.app.R
import com.mhss.app.data.bookmarksDataModule
import com.mhss.app.data.calendarDataModule
import com.mhss.app.data.di.aiDataModule
import com.mhss.app.data.diaryDataModule
import com.mhss.app.data.noteDataModule
import com.mhss.app.data.settingsDataModule
import com.mhss.app.data.tasksDataModule
import com.mhss.app.database.di.databaseModule
import com.mhss.app.di.coroutinesModule
import com.mhss.app.di.networkModule
import com.mhss.app.mybrain.di.MainPresentationModule
import com.mhss.app.mybrain.di.platformModule
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.di.PreferencesModule
import com.mhss.app.presentation.di.BookmarksPresentationModule
import com.mhss.app.presentation.di.CalendarPresentationModule
import com.mhss.app.presentation.di.DiaryPresentationModule
import com.mhss.app.presentation.di.NotePresentationModule
import com.mhss.app.presentation.di.SettingsPresentationModule
import com.mhss.app.presentation.di.TasksPresentationModule
import com.mhss.app.util.Constants
import com.mhss.app.widget.di.WidgetModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.ksp.generated.*
import kotlin.system.exitProcess

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PrefsConstants.SETTINGS_PREFERENCES)

class MyBrainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyBrainApplication)
            androidLogger()
            modules(
                platformModule,
                MainPresentationModule().module,
                AlarmModule().module,
                databaseModule,
                networkModule,
                coroutinesModule,
                PreferencesModule().module,
                NotePresentationModule().module,
                noteDataModule,
                DiaryPresentationModule().module,
                diaryDataModule,
                TasksPresentationModule().module,
                tasksDataModule,
                SettingsPresentationModule().module,
                settingsDataModule,
                CalendarPresentationModule().module,
                calendarDataModule,
                BookmarksPresentationModule().module,
                bookmarksDataModule,
                WidgetModule().module,
                aiDataModule
            )
            workManagerFactory()
        }
        createRemindersNotificationChannel()
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            e.printStackTrace()
            "```\n${e.stackTraceToString()}\n```".copyToClipboard()
            Toast.makeText(this, getString(R.string.exception_stack_trace_copied), Toast.LENGTH_LONG).show()
            exitProcess(1)
        }
    }

    private fun createRemindersNotificationChannel() {
        val channel = NotificationChannel(
            Constants.REMINDERS_CHANNEL_ID,
            getString(R.string.reminders_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = getString(R.string.reminders_channel_description)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }

    private fun String.copyToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", this)
        clipboard.setPrimaryClip(clip)
    }
}
