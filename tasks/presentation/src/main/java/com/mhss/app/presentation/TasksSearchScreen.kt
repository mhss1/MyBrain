package com.mhss.app.presentation

import androidx.compose.runtime.Composable
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