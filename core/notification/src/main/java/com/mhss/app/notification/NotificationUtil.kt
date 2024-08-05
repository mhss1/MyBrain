package com.mhss.app.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.mhss.app.util.Constants
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.Task
import com.mhss.app.ui.R

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
        "${Constants.TASK_DETAILS_URI}/${task.id}".toUri()
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
                Priority.LOW -> NotificationCompat.PRIORITY_DEFAULT
                Priority.MEDIUM -> NotificationCompat.PRIORITY_HIGH
                Priority.HIGH -> NotificationCompat.PRIORITY_MAX
            }
        )
        .addAction(R.drawable.ic_check, context.getString(R.string.complete), completePendingIntent)
        .setAutoCancel(true)
        .build()

    notify(id, notification)
}