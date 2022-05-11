package com.mhss.app.mybrain.util.alarms

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.util.Constants

fun NotificationManager.sendNotification(message: String, context: Context, id: Int) {
    val notification = NotificationCompat.Builder(context, Constants.REMINDERS_CHANNEL_ID)
//       TODO() .setSmallIcon()
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    notify(id, notification)
}