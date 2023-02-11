package com.mhss.app.mybrain.util.alarms

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.app.getString
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.presentation.main.MainActivity
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.Priority
import com.mhss.app.mybrain.util.settings.toInt

fun NotificationManager.sendNotification(task: Task, context: Context, id: Int) {
    val completeIntent = Intent(context, TaskActionButtonBroadcastReceiver::class.java).apply {
        action = Constants.ACTION_COMPLETE
        putExtra(Constants.TASK_ID_EXTRA, task.id)
    }
    val completePendingIntent: PendingIntent =
        PendingIntent.getBroadcast(
            context,
            task.id,
            completeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    val taskDetailIntent = Intent(
        Intent.ACTION_VIEW,
        "${Constants.TASK_DETAILS_URI}/${task.id}".toUri(),
        context,
        MainActivity::class.java
    )
    val taskDetailsPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
        addNextIntentWithParentStack(taskDetailIntent)
        getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    val notification = NotificationCompat.Builder(context, Constants.REMINDERS_CHANNEL_ID)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(task.title)
        .setContentText(task.description)
        .setContentIntent(taskDetailsPendingIntent)
        .setPriority(
            when (task.priority) {
                Priority.LOW.toInt() -> NotificationCompat.PRIORITY_DEFAULT
                Priority.MEDIUM.toInt() -> NotificationCompat.PRIORITY_HIGH
                Priority.HIGH.toInt() -> NotificationCompat.PRIORITY_MAX
                else -> NotificationCompat.PRIORITY_DEFAULT
            }
        )
        .addAction(R.drawable.ic_check, getString(R.string.complete), completePendingIntent)
        .setAutoCancel(true)
        .build()

    notify(id, notification)
}