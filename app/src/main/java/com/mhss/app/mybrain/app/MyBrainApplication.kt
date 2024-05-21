package com.mhss.app.mybrain.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.di.DataModule
import com.mhss.app.mybrain.di.UseCasesModule
import com.mhss.app.mybrain.di.ViewModelsModule
import com.mhss.app.mybrain.util.Constants
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import kotlin.system.exitProcess

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.SETTINGS_PREFERENCES)

class MyBrainApplication : Application() {

    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        startKoin {
            androidContext(this@MyBrainApplication)
            androidLogger()
            modules(DataModule().module, UseCasesModule().module, ViewModelsModule().module)
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

// for string resources where context is not available
fun getString(
    @StringRes
    resId: Int,
    vararg args: String
) = MyBrainApplication.appContext.getString(resId, *args)
