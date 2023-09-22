package com.mhss.app.mybrain.presentation.glance_widgets

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.mhss.app.mybrain.presentation.main.MainActivity
import com.mhss.app.mybrain.util.Constants

class AddTaskAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "${Constants.TASKS_SCREEN_URI}/true".toUri(),
            context,
            MainActivity::class.java
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

class NavigateToTasksAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "${Constants.TASKS_SCREEN_URI}/false".toUri(),
            context,
            MainActivity::class.java
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

class TaskWidgetItemClickAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        parameters[taskId]?.let {
            val intent = Intent(
                Intent.ACTION_VIEW,
                "${Constants.TASK_DETAILS_URI}/${it}".toUri(),
                context,
                MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}

class CompleteTaskAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        parameters[taskId]?.let { id ->
            parameters[completed].let { completed ->
                val intent = Intent(context, CompleteTaskWidgetReceiver::class.java)
                intent.putExtra("taskId", id)
                intent.putExtra("completed", completed)
                context.sendBroadcast(intent)
            }
        }
    }
}

val taskId = ActionParameters.Key<Int>("taskId")
val completed = ActionParameters.Key<Boolean>("completed")