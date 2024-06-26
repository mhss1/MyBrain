package com.mhss.app.widget.tasks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mhss.app.domain.use_case.UpdateTaskCompletedUseCase
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CompleteTaskWidgetReceiver : BroadcastReceiver(), KoinComponent {

    private val completeTask: UpdateTaskCompletedUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getIntExtra("taskId", -1)
            val completed = intent.getBooleanExtra("completed", true)
            if (id != -1) {
                runBlocking {
                    completeTask(id, completed)
                }
            }
    }
}