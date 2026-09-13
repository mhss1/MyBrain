package com.mhss.app.mybrain.presentation.main

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mhss.app.presentation.CalendarDashboardWidget
import com.mhss.app.presentation.MoodCircularBar
import com.mhss.app.presentation.TasksDashboardWidget
import com.mhss.app.ui.Res
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.dashboard
import com.mhss.app.ui.navigation.Screen
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: MainViewModel = koinViewModel()
) {
    Scaffold(
        topBar = {
            MyBrainAppBar(stringResource(Res.string.dashboard))
        }
    ) {paddingValues ->
        LaunchedEffect(true) { viewModel.onDashboardEvent(DashboardEvent.InitAll) }
        val dashboardTasks = viewModel.dashboardTasks.collectAsLazyPagingItems()
        LazyColumn(contentPadding = paddingValues) {
            item {
                CalendarDashboardWidget(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f),
                    events = viewModel.uiState.dashBoardEvents,
                    onClick = {
                        navController.navigate(
                            Screen.CalendarScreen
                        )
                    },
                    onPermission = {
                        viewModel.onDashboardEvent(DashboardEvent.ReadPermissionChanged(it))
                    },
                    onAddEventClicked = {
                        navController.navigate(
                            Screen.CalendarEventDetailsScreen()
                        )
                    },
                    onEventClicked = {
                        navController.navigate(
                            Screen.CalendarEventDetailsScreen(
                                it.id
                            )
                        )
                    }
                )
            }
            item {
                TasksDashboardWidget(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f),
                    tasks = dashboardTasks,
                    onCheck = { task, completed ->
                        viewModel.onDashboardEvent(DashboardEvent.CompleteTask(task, completed))
                    },
                    onTaskClick = {
                        navController.navigate(
                            Screen.TaskDetailScreen(it.id)
                        )
                    },
                    onAddClick = {
                        navController.navigate(
                            Screen.TasksScreen(addTask = true)
                        )
                    },
                    onClick = {
                        navController.navigate(
                            Screen.TasksScreen()
                        )
                    }
                )
            }
            item {
                Row {
                    MoodCircularBar(
                        entries = viewModel.uiState.dashBoardEntries,
                        showPercentage = false,
                        modifier = Modifier.weight(1f, fill = true),
                        onClick = {
                            navController.navigate(
                                Screen.DiaryChartScreen
                            )
                        }
                    )
                    TasksSummaryCard(
                        modifier = Modifier.weight(1f, fill = true),
                        tasks = viewModel.uiState.summaryTasks
                    )
                }
            }
            item { Spacer(Modifier.height(65.dp)) }
        }
    }
}
