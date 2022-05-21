package com.mhss.app.mybrain.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.presentation.calendar.CalendarWidget
import com.mhss.app.mybrain.presentation.diary.MoodCircularBar
import com.mhss.app.mybrain.presentation.tasks.TasksWidget
import com.mhss.app.mybrain.presentation.util.Screen
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.date.inTheLastWeek

@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: MainViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.dashboard),
                        style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold)
                    )
                },
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp
            )
        }
    ) {
        LaunchedEffect(true) { viewModel.onDashboardEvent(DashboardEvent.InitAll) }
        LazyColumn {
            item {
                CalendarWidget(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f),
                    events = viewModel.uiState.dashBoardEvents,
                    onClick = {
                        navController.navigate(
                            Screen.CalendarScreen.route
                        )
                    },
                    onPermission = {
                        viewModel.onDashboardEvent(DashboardEvent.ReadPermissionChanged(it))
                    }
                )
            }
            item {
                TasksWidget(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f),
                    tasks = viewModel.uiState.dashBoardTasks,
                    onCheck = {
                        viewModel.onDashboardEvent(DashboardEvent.UpdateTask(it))
                    },
                    onTaskClick = {
                        navController.navigate(
                            Screen.TaskDetailScreen.route
                                .replace(
                                    "{${Constants.TASK_ID_ARG}}",
                                    it.id.toString()
                                )
                        )
                    },
                    onAddClick = {
                        navController.navigate(
                            Screen.TasksScreen
                                .route
                                .replace(
                                    "{${Constants.ADD_TASK_TILE_ARG}}",
                                    "true"
                                )
                        )
                    },
                    onClick = {
                        navController.navigate(
                            Screen.TasksScreen.route
                        )
                    }
                )
            }
            item {
                Row {
                    val tasks = remember(viewModel.uiState.dashBoardTasks) {
                        derivedStateOf {
                            viewModel.uiState.dashBoardTasks.filter { it.createdDate.inTheLastWeek() }
                        }
                    }
                    MoodCircularBar(
                        entries = viewModel.uiState.dashBoardEntries,
                        showPercentage = false,
                        modifier = Modifier.weight(1f, fill = true),
                        onClick = {
                            navController.navigate(
                                Screen.DiaryChartScreen.route
                            )
                        }
                    )
                    TasksSummaryCard(
                        modifier = Modifier.weight(1f, fill = true),
                        tasks = tasks.value
                    )
                }
            }
            item { Spacer(Modifier.height(65.dp)) }
        }
    }
}