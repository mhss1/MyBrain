@file:OptIn(ExperimentalLayoutApi::class)

package com.mhss.app.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mhss.app.ui.R
import com.mhss.app.util.permissions.Permission
import com.mhss.app.preferences.domain.model.Order
import com.mhss.app.preferences.domain.model.OrderType
import com.mhss.app.ui.components.tasks.TaskCard
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.titleRes
import com.mhss.app.util.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    navController: NavHostController,
    addTask: Boolean = false,
    viewModel: TasksViewModel = koinViewModel()
) {
    val context = LocalContext.current
    var orderSettingsVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val uiState = viewModel.tasksUiState
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var openSheet by remember {
        mutableStateOf(false)
    }
    val alarmPermissionState = rememberPermissionState(Permission.SCHEDULE_ALARMS)
    val scope = rememberCoroutineScope()
    BackHandler {
        if (openSheet)
            openSheet = false
        else
            navController.navigateUp()
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tasks),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(!sheetState.isVisible) {
                FloatingActionButton(
                    onClick = {
                        openSheet = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(
                        modifier = Modifier.size(25.dp),
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(R.string.add_task),
                        tint = Color.White
                    )
                }
            }
        },
    ) { paddingValues ->
        if (openSheet) ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { openSheet = false }
        ) {
            AddTaskBottomSheetContent(
                onAddTask = {
                    viewModel.onEvent(TaskEvent.AddTask(it))
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            openSheet = false
                        }
                    }
                    focusRequester.freeFocus()
                },
                focusRequester
            )
        }
        LaunchedEffect(uiState.error) {
            uiState.error?.let {
                val snackbarResult = snackbarHostState.showSnackbar(
                    context.getString(it),
                    if (uiState.errorAlarm) context.getString(R.string.grant_permission) else null
                )
                if (snackbarResult == SnackbarResult.ActionPerformed) {
                    alarmPermissionState.launchRequest()
                }
                viewModel.onEvent(TaskEvent.ErrorDisplayed)
            }
        }
        LaunchedEffect(true) {
            if (addTask) {
                openSheet = true
            }
        }
        if (uiState.tasks.isEmpty()) NoTasksMessage()
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                            painter = painterResource(R.drawable.ic_settings_sliders),
                            contentDescription = stringResource(R.string.order_by)
                        )
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.TaskSearchScreen)
                    }) {
                        Icon(
                            modifier = Modifier.size(25.dp),
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = stringResource(R.string.search)
                        )
                    }
                }
                AnimatedVisibility(visible = orderSettingsVisible) {
                    TasksSettingsSection(
                        uiState.taskOrder,
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
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp)
            ) {
                items(uiState.tasks, key = { it.id }) { task ->
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
                            navController.navigate(
                                Screen.TaskDetailScreen(
                                    taskId = task.id
                                )
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun NoTasksMessage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.no_tasks_message),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            modifier = Modifier.size(125.dp),
            painter = painterResource(id = R.drawable.tasks_img),
            contentDescription = stringResource(R.string.no_tasks_message),
            alpha = 0.7f
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TasksSettingsSection(
    order: Order,
    showCompleted: Boolean,
    onOrderChange: (Order) -> Unit,
    onShowCompletedChange: (Boolean) -> Unit
) {
    val orders = remember {
        listOf(
            Order.DateModified(),
            Order.DueDate(),
            Order.DateCreated(),
            Order.Alphabetical(),
            Order.Priority()
        )
    }
    val orderTypes = remember {
        listOf(
            OrderType.ASC,
            OrderType.DESC
        )
    }
    Column(
        Modifier.background(color = MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = stringResource(R.string.order_by),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
        FlowRow(
            modifier = Modifier.padding(end = 8.dp)
        ) {
            orders.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = order == it,
                        onClick = {
                            if (order != it)
                                onOrderChange(
                                    it.copyOrder(orderType = order.orderType)
                                )
                        }
                    )
                    Text(
                        text = stringResource(it.titleRes),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        HorizontalDivider()
        FlowRow {
            orderTypes.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = order.orderType == it,
                        onClick = {
                            if (order != it)
                                onOrderChange(
                                    order.copyOrder(it)
                                )
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(it.titleRes),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = showCompleted, onCheckedChange = { onShowCompletedChange(it) })
            Text(
                text = stringResource(R.string.show_completed_tasks),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}