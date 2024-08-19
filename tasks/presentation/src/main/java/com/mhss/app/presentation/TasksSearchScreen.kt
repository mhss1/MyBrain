package com.mhss.app.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.mhss.app.ui.components.tasks.TaskSearchContent
import com.mhss.app.ui.navigation.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun TasksSearchScreen(
    navController: NavHostController,
    viewModel: TasksViewModel = koinViewModel()
) {
    val state = viewModel.tasksUiState
    TaskSearchContent(
        modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues()),
        tasks = state.searchTasks,
        onQueryChange = { viewModel.onEvent(TaskEvent.SearchTasks(it)) },
        onTaskClick = {
            navController.navigate(
                Screen.TaskDetailScreen(
                    taskId = it.id
                )
            )
        },
        onCompleteTask = { task ->
            viewModel.onEvent(
                TaskEvent.CompleteTask(
                    task,
                    !task.isCompleted
                )
            )
        }
    )
}