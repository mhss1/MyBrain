package com.mhss.app.mybrain

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.mhss.app.alarm.di.AlarmModule
import com.mhss.app.data.NoteDataModule
import com.mhss.app.data.di.AiDataModule
import com.mhss.app.data.di.BookmarksDataModule
import com.mhss.app.data.di.CalendarDataModule
import com.mhss.app.data.di.DiaryDataModule
import com.mhss.app.data.di.SettingsDataModule
import com.mhss.app.data.di.TasksDataModule
import com.mhss.app.data.noteMarkdownModule
import com.mhss.app.database.di.DatabaseModule
import com.mhss.app.datetime.DateTimeModule
import com.mhss.app.di.CoroutinesModule
import com.mhss.app.domain.di.AiDomainModule
import com.mhss.app.domain.di.BookmarksDomainModule
import com.mhss.app.domain.di.CalendarDomainModule
import com.mhss.app.domain.di.DiaryDomainModule
import com.mhss.app.domain.di.NoteDomainModule
import com.mhss.app.domain.di.SettingsDomainModule
import com.mhss.app.domain.di.TasksDomainModule
import com.mhss.app.mybrain.appfunctions.AppFunctionsModule
import com.mhss.app.mybrain.di.MainPresentationModule
import com.mhss.app.mybrain.notification.NotificationConstants
import com.mhss.app.mybrain.sync.SyncOrchestrator
import com.mhss.app.mybrain.sync.domain.TombstoneCleanupScheduler
import com.mhss.app.mybrain.sync.di.LocalSyncModule
import com.mhss.app.mybrain.sync.repository.DeviceKeyStore
import com.mhss.app.mybrain.sync.repository.PairedDevicesRepository
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.di.PreferencesModule
import com.mhss.app.preferences.di.PreferencesPlatformModule
import com.mhss.app.preferences.domain.model.booleanPreferencesKey
import com.mhss.app.preferences.domain.model.stringPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.presentation.di.AiPresentationModule
import com.mhss.app.presentation.di.BookmarksPresentationModule
import com.mhss.app.presentation.di.CalendarPresentationModule
import com.mhss.app.presentation.di.DiaryPresentationModule
import com.mhss.app.presentation.di.NotePresentationModule
import com.mhss.app.presentation.di.SettingsPresentationModule
import com.mhss.app.presentation.di.TasksPresentationModule
import com.mhss.app.storage.di.StorageModule
import com.mhss.app.ui.R
import com.mhss.app.widget.di.WidgetModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.annotation.KoinApplication
import org.koin.core.context.loadKoinModules
import org.koin.plugin.module.dsl.startKoin
import kotlin.system.exitProcess

class MyBrainApplication : Application() {

    private val getPreference: GetPreferenceUseCase by inject()
    private val tombstoneCleanupScheduler: TombstoneCleanupScheduler by inject()
    private val syncOrchestrator: SyncOrchestrator by inject()
    private val deviceKeyStore: DeviceKeyStore by inject()
    private val pairedDevicesRepository: PairedDevicesRepository by inject()
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        startKoin<MyBrainKoinApp> {
            allowOverride(true)
            androidContext(this@MyBrainApplication)
            androidLogger()
            workManagerFactory()
        }
        loadNotesModule()
        scheduleTombstoneCleanup()

        appScope.launch {
            deviceKeyStore.getCurrentDeviceId()
            if (pairedDevicesRepository.getPairedDevices().isNotEmpty()) {
                syncOrchestrator.startServer()
            }
        }

        createRemindersNotificationChannel()
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            e.printStackTrace()
            "```\n${e.stackTraceToString()}\n```".copyToClipboard()
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, getString(R.string.exception_stack_trace_copied), Toast.LENGTH_LONG).show()
            }
            exitProcess(1)
        }
    }

    private fun scheduleTombstoneCleanup() {
        appScope.launch {
            try {
                tombstoneCleanupScheduler.schedule()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e("MyBrainApplication", "Tombstone cleanup scheduling failed", error)
            }
        }
    }

    private fun loadNotesModule() = runBlocking {
        val isExternalNotesEnabled = getPreference(
            booleanPreferencesKey(PrefsConstants.EXTERNAL_NOTES_ENABLED),
            false
        ).first()

        if (!isExternalNotesEnabled) return@runBlocking

        val rootUri = getPreference(
            stringPreferencesKey(PrefsConstants.EXTERNAL_NOTES_FOLDER_URI),
            ""
        ).first()

        if (rootUri.isNotBlank()) {
            loadKoinModules(noteMarkdownModule(rootUri))
        }
    }

    private fun createRemindersNotificationChannel() {
        val channel = NotificationChannel(
            NotificationConstants.REMINDERS_CHANNEL_ID,
            getString(R.string.reminders_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = getString(R.string.reminders_channel_description)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }

    private fun String.copyToClipboard() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", this)
        clipboard.setPrimaryClip(clip)
    }
}

@KoinApplication(
    modules = [
        DateTimeModule::class,
        MainPresentationModule::class,
        AlarmModule::class,
        PreferencesModule::class,
        PreferencesPlatformModule::class,
        StorageModule::class,
        NotePresentationModule::class,
        NoteDataModule::class,
        NoteDomainModule::class,
        DiaryPresentationModule::class,
        DiaryDataModule::class,
        DiaryDomainModule::class,
        TasksPresentationModule::class,
        TasksDataModule::class,
        TasksDomainModule::class,
        SettingsPresentationModule::class,
        SettingsDataModule::class,
        SettingsDomainModule::class,
        CalendarPresentationModule::class,
        CalendarDataModule::class,
        CalendarDomainModule::class,
        BookmarksPresentationModule::class,
        BookmarksDataModule::class,
        BookmarksDomainModule::class,
        WidgetModule::class,
        AiDataModule::class,
        AiDomainModule::class,
        AiPresentationModule::class,
        CoroutinesModule::class,
        DatabaseModule::class,
        AppFunctionsModule::class,
        LocalSyncModule::class
    ]
)
class MyBrainKoinApp
