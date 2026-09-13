package com.mhss.app.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.mhss.app.ui.components.tasks.TaskSearchContent
import com.mhss.app.ui.navigation.Screen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TasksSearchScreen(
    navController: NavHostController,
    viewModel: TasksViewModel = koinViewModel()
) {
    TaskSearchContent(
        modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues()),
        tasks = viewModel.searchResults.collectAsLazyPagingItems(),
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
