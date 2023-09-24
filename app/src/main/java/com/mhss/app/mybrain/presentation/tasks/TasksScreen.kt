package com.mhss.app.mybrain.presentation.tasks

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.flowlayout.FlowRow
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.presentation.util.Screen
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.Order
import com.mhss.app.mybrain.util.settings.OrderType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TasksScreen(
    navController: NavHostController,
    addTask: Boolean = false,
    viewModel: TasksViewModel = hiltViewModel()
) {
    var orderSettingsVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val uiState = viewModel.tasksUiState
    val scaffoldState = rememberScaffoldState()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    BackHandler {
        if (sheetState.isVisible)
            scope.launch {
                sheetState.hide()

            }
        else
            navController.popBackStack()
    }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tasks),
                        style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold)
                    )
                },
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        sheetState.show()
                        focusRequester.requestFocus()
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
            ) {
                Icon(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.add_task),
                    tint = Color.White
                )
            }
        },
    ) {paddingValues ->
        ModalBottomSheetLayout(
            modifier = Modifier.padding(paddingValues),
            sheetState = sheetState,
            sheetShape = RoundedCornerShape(topEnd = 25.dp, topStart = 25.dp),
            sheetContent = {
                AddTaskBottomSheetContent(
                    onAddTask = {
                        viewModel.onEvent(TaskEvent.AddTask(it))
                        scope.launch { sheetState.hide() }
                        focusRequester.freeFocus()
                    },
                    focusRequester
                )
            }) {
            LaunchedEffect(uiState.error) {
                uiState.error?.let {
                    scaffoldState.snackbarHostState.showSnackbar(
                        uiState.error
                    )
                    viewModel.onEvent(TaskEvent.ErrorDisplayed)
                }
            }
            LaunchedEffect(true){
                if (addTask) scope.launch {
                    sheetState.show()
                    focusRequester.requestFocus()
                }
            }
            if (uiState.tasks.isEmpty())
                NoTasksMessage()
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp)
            ) {
                item {
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
                                navController.navigate(Screen.TaskSearchScreen.route)
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
                }
                items(uiState.tasks, key = { it.id }) { task ->
                    TaskItem(
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
                                Screen.TaskDetailScreen.route.replace(
                                    "{${Constants.TASK_ID_ARG}}",
                                    "${task.id}"
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
fun NoTasksMessage(){
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.no_tasks_message),
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
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
@Composable
fun TasksSettingsSection(
    order: Order,
    showCompleted: Boolean,
    onOrderChange: (Order) -> Unit,
    onShowCompletedChange: (Boolean) -> Unit
) {
    val orders = listOf(
        Order.DateModified(),
        Order.DueDate(),
        Order.DateCreated(),
        Order.Alphabetical(),
        Order.Priority()
    )
    val orderTypes = listOf(
        OrderType.ASC(),
        OrderType.DESC()
    )
    Column(
        Modifier.background(color = MaterialTheme.colors.background)
    ) {
        Text(
            text = stringResource(R.string.order_by),
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(start = 8.dp)
        )
        FlowRow(
            modifier = Modifier.padding(end = 8.dp)
        ) {
            orders.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = order.orderTitle == it.orderTitle,
                        onClick = {
                            if (order.orderTitle != it.orderTitle)
                                onOrderChange(
                                    it.copy(orderType = order.orderType)
                                )
                        }
                    )
                    Text(text = it.orderTitle, style = MaterialTheme.typography.body1)
                }
            }
        }
        Divider()
        FlowRow {
            orderTypes.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = order.orderType.orderTitle == it.orderTitle,
                        onClick = {
                            if (order.orderTitle != it.orderTitle)
                                onOrderChange(
                                    order.copy(it)
                                )
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = it.orderTitle, style = MaterialTheme.typography.body1)
                }
            }
        }
        Divider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = showCompleted, onCheckedChange = { onShowCompletedChange(it) })
            Text(
                text = stringResource(R.string.show_completed_tasks),
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}