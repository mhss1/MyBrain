@file:OptIn(ExperimentalLayoutApi::class)
@file:Suppress("AssignedValueIsNeverRead")

package com.mhss.app.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import com.mhss.app.ui.Res
import com.mhss.app.ui.add_task
import com.mhss.app.ui.components.PagingLoadState
import com.mhss.app.ui.components.PagingPlaceholder
import com.mhss.app.ui.components.common.LiquidFloatingActionButton
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.components.isEmpty
import com.mhss.app.ui.components.tasks.TaskCard
import com.mhss.app.ui.grant_permission
import com.mhss.app.ui.ic_add
import com.mhss.app.ui.ic_search
import com.mhss.app.ui.ic_settings_sliders
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.no_alarm_permission
import com.mhss.app.ui.no_tasks_message
import com.mhss.app.ui.order_by
import com.mhss.app.ui.search
import com.mhss.app.ui.show_completed_tasks
import com.mhss.app.ui.snackbar.LocalisedSnackbarHost
import com.mhss.app.ui.snackbar.showSnackbar
import com.mhss.app.ui.tasks
import com.mhss.app.ui.tasks_img
import com.mhss.app.ui.titleRes
import com.mhss.app.util.permissions.Permission
import com.mhss.app.util.permissions.rememberPermissionState
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource as cmpStringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    navController: NavHostController,
    addTask: Boolean = false,
    viewModel: TasksViewModel = koinViewModel()
) {
    val tasks = viewModel.tasks.collectAsLazyPagingItems()
    var orderSettingsVisible by remember { mutableStateOf(false) }
    val uiState = viewModel.tasksUiState
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddTaskCard by rememberSaveable {
        mutableStateOf(false)
    }
    val alarmPermissionState = rememberPermissionState(Permission.SCHEDULE_ALARMS)
    val liquidState = rememberLiquidState()
    Scaffold(
        snackbarHost = { LocalisedSnackbarHost(snackbarHostState) },
        topBar = {
            MyBrainAppBar(stringResource(Res.string.tasks))
        },
        floatingActionButton = {
            AnimatedVisibility(!showAddTaskCard) {
                LiquidFloatingActionButton(
                    onClick = {
                        showAddTaskCard = true
                    },
                    iconPainter = painterResource(Res.drawable.ic_add),
                    contentDescription = stringResource(Res.string.add_task),
                    liquidState = liquidState
                )
            }
        },
    ) { paddingValues ->
        LaunchedEffect(uiState.alarmError) {
            if (uiState.alarmError) {
                val snackbarResult = snackbarHostState.showSnackbar(
                    Res.string.no_alarm_permission,
                    Res.string.grant_permission
                )
                if (snackbarResult == SnackbarResult.ActionPerformed) {
                    alarmPermissionState.launchRequest()
                }
                viewModel.onEvent(TaskEvent.ErrorDisplayed)
            }
        }
        LaunchedEffect(true) {
            if (addTask) {
                showAddTaskCard = true
            }
        }
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (tasks.isEmpty) NoTasksMessage(liquidState)
            Column(
                Modifier
                    .fillMaxSize()
                    .liquefiable(liquidState)
                    .background(
                        if (tasks.isEmpty) Color.Transparent else MaterialTheme.colorScheme.background
                    )
            ) {
                Column(
                    Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { orderSettingsVisible = !orderSettingsVisible }) {
                            Icon(
                                modifier = Modifier.size(25.dp),
                                painter = painterResource(Res.drawable.ic_settings_sliders),
                                contentDescription = stringResource(Res.string.order_by)
                            )
                        }
                        IconButton(onClick = {
                            navController.navigate(Screen.TaskSearchScreen)
                        }) {
                            Icon(
                                modifier = Modifier.size(25.dp),
                                painter = painterResource(Res.drawable.ic_search),
                                contentDescription = stringResource(Res.string.search)
                            )
                        }
                    }
                    AnimatedVisibility(visible = orderSettingsVisible) {
                        TasksSettingsSection(
                            uiState.taskSortOrder,
                            uiState.showCompletedTasks,
                            onShowCompletedChange = {
                                viewModel.onEvent(
                                    TaskEvent.ShowCompletedTasks(
                                        it
                                    )
                                )
                            },
                            onOrderChange = {
                                viewModel.onEvent(TaskEvent.UpdateOrder(it))
                            }
                        )
                    }
                }
                PagingLoadState(tasks)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp)
                ) {
                    items(tasks.itemCount, key = tasks.itemKey { it.id }) { index ->
                        tasks[index]?.let { task ->
                            TaskCard(
                                task = task,
                                onComplete = {
                                    viewModel.onEvent(
                                        TaskEvent.CompleteTask(
                                            task,
                                            !task.isCompleted
                                        )
                                    )
                                },
                                onClick = {
                                    showAddTaskCard = false
                                    navController.navigate(
                                        Screen.TaskDetailScreen(
                                            taskId = task.id
                                        )
                                    )
                                },
                            )
                        } ?: PagingPlaceholder(Modifier.padding(horizontal = 8.dp))
                    }
                }
            }
            AnimatedVisibility(
                visible = showAddTaskCard,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .imePadding()
            ) {
                AddTaskFloatingCard(
                    onAddTask = {
                        viewModel.onEvent(TaskEvent.AddTask(it))
                    },
                    onDismiss = {
                        showAddTaskCard = false
                    },
                    liquidState = liquidState
                )
            }
        }
    }
}

@Composable
fun NoTasksMessage(liquidState: LiquidState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .liquefiable(liquidState)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.no_tasks_message),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            modifier = Modifier.size(125.dp),
            painter = painterResource(Res.drawable.tasks_img),
            contentDescription = stringResource(Res.string.no_tasks_message),
            alpha = 0.7f
        )
    }
}

@Composable
fun TasksSettingsSection(
    sortOrder: SortOrder,
    showCompleted: Boolean,
    onOrderChange: (SortOrder) -> Unit,
    onShowCompletedChange: (Boolean) -> Unit
) {
    val sortOrders = remember {
        listOf(
            SortOrder.DateModified(),
            SortOrder.DueDate(),
            SortOrder.DateCreated(),
            SortOrder.Alphabetical(),
            SortOrder.Priority(),
            SortOrder.Done()
        )
    }
    val sortTypes = remember {
        listOf(
            SortType.ASC,
            SortType.DESC
        )
    }
    Column(
        Modifier.background(color = MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = stringResource(Res.string.order_by),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
        FlowRow(
            modifier = Modifier.padding(end = 8.dp)
        ) {
            sortOrders.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = sortOrder::class == it::class,
                        onClick = {
                            if (sortOrder != it)
                                onOrderChange(
                                    it.copyOrder(sortType = sortOrder.sortType)
                                )
                        }
                    )
                    Text(
                        text = cmpStringResource(it.titleRes),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        HorizontalDivider()
        FlowRow {
            sortTypes.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = sortOrder.sortType == it,
                        onClick = {
                            if (sortOrder != it)
                                onOrderChange(
                                    sortOrder.copyOrder(it)
                                )
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = cmpStringResource(it.titleRes),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = showCompleted, onCheckedChange = { onShowCompletedChange(it) })
            Text(
                text = stringResource(Res.string.show_completed_tasks),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
