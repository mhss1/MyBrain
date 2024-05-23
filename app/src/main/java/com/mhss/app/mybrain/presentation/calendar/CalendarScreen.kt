@file:OptIn(ExperimentalPermissionsApi::class)

package com.mhss.app.mybrain.presentation.calendar

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.calendar.Calendar
import com.mhss.app.mybrain.presentation.navigation.Screen
import com.mhss.app.mybrain.util.date.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CalendarScreen(
    navController: NavHostController,
    viewModel: CalendarViewModel = koinViewModel()
) {
    val state = viewModel.uiState
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()
    var settingsVisible by remember { mutableStateOf(false) }
    val readCalendarPermissionState = rememberPermissionState(
        android.Manifest.permission.READ_CALENDAR
    )
    val month by remember(state.events) {
        derivedStateOf {
            state.events.values.elementAt(lazyListState.firstVisibleItemIndex).first().start.monthName()
        }
    }
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.calendar),
                        style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    if (state.events.isNotEmpty()) MonthDropDownMenu(
                        selectedMonth = month,
                        months = state.months,
                        onMonthSelected = { selected ->
                            scope.launch {
                                lazyListState.scrollToItem(
                                    state.events.values.indexOfFirst {
                                        it.first().start.monthName() == selected
                                    }
                                )
                            }
                        }
                    )
                },
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp,
            )
        },
        floatingActionButton = {
            if (readCalendarPermissionState.hasPermission) FloatingActionButton(
                onClick = {
                    navController.navigate(
                        Screen.CalendarEventDetailsScreen(
                            null
                        )
                    )
                },
                backgroundColor = MaterialTheme.colors.primary,
            ) {
                Icon(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.add_event),
                    tint = Color.White
                )
            }
        },
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            if (readCalendarPermissionState.hasPermission) {
                LaunchedEffect(true) {
                    viewModel.onEvent(
                        CalendarViewModelEvent
                            .ReadPermissionChanged(readCalendarPermissionState.hasPermission)
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { settingsVisible = !settingsVisible }) {
                        Icon(
                            modifier = Modifier.size(25.dp),
                            painter = painterResource(R.drawable.ic_settings_sliders),
                            contentDescription = stringResource(R.string.include_calendars)
                        )
                    }
                }
                AnimatedVisibility(visible = settingsVisible) {
                    CalendarSettingsSection(
                        calendars = state.calendars, onCalendarClicked = {
                            viewModel.onEvent(CalendarViewModelEvent.IncludeCalendar(it))
                        }
                    )
                }
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    state.events.forEach { (day, events) ->
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = day.substring(0, day.indexOf(",")),
                                    style = MaterialTheme.typography.h5
                                )
                                events.forEach { event ->
                                    CalendarEventItem(event = event, onClick = {
                                        navController.navigate(
                                            Screen.CalendarEventDetailsScreen(
                                                Json.encodeToString(event)
                                            )
                                        )
                                    })
                                }
                            }
                        }
                    }
                }
            } else {
                NoReadCalendarPermissionMessage(
                    shouldShowRationale = readCalendarPermissionState.shouldShowRationale,
                    context
                ) {
                    readCalendarPermissionState.launchPermissionRequest()
                }
            }
        }
    }

}

@Composable
fun NoReadCalendarPermissionMessage(
    shouldShowRationale: Boolean,
    context: Context,
    onRequest: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.no_read_calendar_permission_message),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        if (shouldShowRationale) {
            TextButton(onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", context.packageName, null)
                context.startActivity(intent)
            }) {
                Text(text = stringResource(R.string.go_to_settings))
            }

        } else {
            TextButton(onClick = { onRequest() }) {
                Text(text = stringResource(R.string.grant_permission))
            }
        }
    }
}

@Composable
fun MonthDropDownMenu(
    selectedMonth: String,
    months: List<String>,
    onMonthSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        Modifier
            .clickable { expanded = true }
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(targetState = selectedMonth, label = "") { month ->
                Text(
                    text = month,
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
                )
            }
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            months.forEach {
                DropdownMenuItem(
                    onClick = {
                        onMonthSelected(it)
                        expanded = false
                    }
                ) {
                    Text(text = it)
                }
            }
        }
    }
}

@Composable
fun CalendarSettingsSection(
    calendars: Map<String, List<Calendar>>,
    onCalendarClicked: (Calendar) -> Unit
) {
    Column {
        Divider()
        Text(
            text = stringResource(R.string.include_calendars),
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(8.dp)
        )
        Divider()
        calendars.keys.forEach { calendar ->
            var expanded by remember { mutableStateOf(false) }
            Box(
                Modifier
                    .clickable { expanded = true }
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = calendar,
                        style = MaterialTheme.typography.body1
                    )
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    calendars[calendar]?.forEach { subCalendar ->
                        DropdownMenuItem(
                            onClick = {
                                onCalendarClicked(subCalendar)
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = subCalendar.included,
                                    onCheckedChange = { onCalendarClicked(subCalendar) },
                                    colors = CheckboxDefaults.colors(
                                        uncheckedColor = Color(subCalendar.color),
                                        checkedColor = Color(subCalendar.color)
                                    )
                                )
                                Text(
                                    text = subCalendar.name,
                                    style = MaterialTheme.typography.body2
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}