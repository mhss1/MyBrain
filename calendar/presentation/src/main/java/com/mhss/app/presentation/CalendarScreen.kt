package com.mhss.app.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mhss.app.domain.model.Calendar
import com.mhss.app.domain.model.CalendarDay
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.ui.R
import com.mhss.app.ui.components.common.LiquidFloatingActionButton
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.util.date.HOUR_MILLIS
import com.mhss.app.util.date.currentLocalDate
import com.mhss.app.util.date.monthName
import com.mhss.app.util.date.now
import com.mhss.app.util.date.withTimeFrom
import com.mhss.app.util.permissions.Permission
import com.mhss.app.util.permissions.rememberPermissionState
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState
import kotlinx.coroutines.launch
import kotlinx.datetime.number
import org.koin.androidx.compose.koinViewModel


@Composable
fun CalendarScreen(
    navController: NavHostController,
    viewModel: CalendarViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listViewState = rememberLazyListState()
    val dayEventsState = rememberLazyListState()
    var settingsVisible by remember { mutableStateOf(false) }
    val readCalendarPermissionState = rememberPermissionState(
        permission = Permission.READ_CALENDAR
    )
    val today = remember { currentLocalDate() }
    val viewMode = if (state.isMonthView) CalendarViewMode.Month else CalendarViewMode.List

    val months = state.months
    val loadedMonths = state.loadedMonths
    val currentMonth = state.currentMonth
    val selectedDate = state.selectedDate
    
    val selectedDay = remember(selectedDate, currentMonth, loadedMonths[selectedDate.month.number]) {
        loadedMonths[selectedDate.month.number]?.days?.firstOrNull { it.date == selectedDate }
            ?: CalendarDay(
                date = selectedDate,
                isCurrentMonth = selectedDate.year == currentMonth.year && selectedDate.month == currentMonth.month,
                events = emptyList()
            )
    }

    val scope = rememberCoroutineScope()
    val liquidState = rememberLiquidState()

    val listMonthLabel by remember(state.events) {
        derivedStateOf {
            if (state.events.isEmpty()) ""
            else {
                val values = state.events.values.toList()
                val index = listViewState.firstVisibleItemIndex.coerceIn(0, values.lastIndex)
                values.getOrNull(index)?.firstOrNull()?.start?.monthName().orEmpty()
            }
        }
    }

    val selectedMonthLabel = when (viewMode) {
        CalendarViewMode.Month -> currentMonth.monthName()
        CalendarViewMode.List -> listMonthLabel
    }

    LaunchedEffect(viewMode) {
        if (viewMode == CalendarViewMode.Month && settingsVisible) {
            settingsVisible = false
        }
    }

    Scaffold(
        topBar = {
            MyBrainAppBar(
                title = stringResource(R.string.calendar),
                actions = {
                    if (viewMode == CalendarViewMode.List && months.isNotEmpty()) {
                        MonthDropDownMenu(
                            selectedMonth = selectedMonthLabel.ifEmpty { stringResource(R.string.calendar) },
                            months = months,
                            onMonthSelected = { selected ->
                                scope.launch {
                                    val targetIndex = state.events.values.indexOfFirst {
                                        it.firstOrNull()?.start?.monthName() == selected
                                    }
                                    if (targetIndex >= 0) {
                                        listViewState.scrollToItem(targetIndex)
                                    }
                                }
                            }
                        )
                    } else if (viewMode == CalendarViewMode.Month) {
                        AnimatedContent(
                            targetState = selectedMonthLabel,
                            label = "Month Title"
                        ) { month ->
                            Text(
                                text = month,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    ViewModeToggleButton(
                        mode = viewMode,
                        onToggle = {
                            val nextIsMonth = viewMode == CalendarViewMode.List
                            viewModel.onEvent(
                                CalendarViewModelEvent.ViewModeChanged(nextIsMonth)
                            )
                        }
                    )
                }
            )
        },
        floatingActionButton = {
            if (readCalendarPermissionState.isGranted) {
                LiquidFloatingActionButton(
                    onClick = {
                        val createEventStartMillis =
                            if (viewMode == CalendarViewMode.Month) selectedDate.withTimeFrom(now() + HOUR_MILLIS)
                            else null
                        navController.navigate(
                            Screen.CalendarEventDetailsScreen(
                                eventId = null,
                                initialStartMillis = createEventStartMillis
                            )
                        )
                    },
                    iconPainter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.add_event),
                    liquidState = liquidState
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .liquefiable(liquidState)
        ) {
            if (readCalendarPermissionState.isGranted) {
                LaunchedEffect(true) {
                    viewModel.onEvent(
                        CalendarViewModelEvent
                            .ReadPermissionChanged(readCalendarPermissionState.isGranted)
                    )
                }
                if (viewMode == CalendarViewMode.List) {
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
                    CalendarListView(
                        modifier = Modifier.fillMaxSize(),
                        state = listViewState,
                        events = state.events,
                        onEventClick = { event ->
                            navController.navigate(
                                Screen.CalendarEventDetailsScreen(
                                    event.id
                                )
                            )
                        }
                    )
                } else {
                    MonthlyCalendar(
                        modifier = Modifier
                            .padding(horizontal = 12.dp),
                        loadedMonths = loadedMonths,
                        onLoadMonth = viewModel::loadMonth,
                        initialMonth = currentMonth,
                        selectedDate = selectedDate,
                        today = today,
                        firstDayOfWeek = state.firstDayOfWeek,
                        onDaySelected = { date ->
                            viewModel.onEvent(CalendarViewModelEvent.SelectedDateChanged(date))
                        },
                        onMonthChanged = {
                            viewModel.onEvent(CalendarViewModelEvent.MonthChanged(it))
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                    DayEventsList(
                        modifier = Modifier
                            .fillMaxSize(),
                        state = dayEventsState,
                        selectedDate = selectedDay,
                        onEventClick = { event ->
                            navController.navigate(
                                Screen.CalendarEventDetailsScreen(
                                    event.id
                                )
                            )
                        }
                    )
                }
            } else {
                NoReadCalendarPermissionMessage(
                    shouldShowRationale = readCalendarPermissionState.shouldShowRationale,
                    onOpenSettings = {
                        readCalendarPermissionState.openAppSettings()
                    },
                    onRequest = {
                        readCalendarPermissionState.launchRequest()
                    }
                )
            }
        }
    }

}

@Composable
fun NoReadCalendarPermissionMessage(
    shouldShowRationale: Boolean,
    onOpenSettings: () -> Unit,
    onRequest: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.no_read_calendar_permission_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        if (shouldShowRationale) {
            TextButton(onClick = onOpenSettings) {
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
private fun CalendarListView(
    modifier: Modifier = Modifier,
    state: LazyListState,
    events: Map<String, List<CalendarEvent>>,
    onEventClick: (CalendarEvent) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        events.forEach { (day, dayEvents) ->
            item(key = day) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = day.substring(0, day.indexOf(",")),
                        style = MaterialTheme.typography.titleMedium
                    )
                    dayEvents.forEach { event ->
                        CalendarEventItem(event = event, onClick = onEventClick)
                    }
                }
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
    Box(Modifier.clickable { expanded = true }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(targetState = selectedMonth, label = "") { month ->
                Text(
                    text = month,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
                    },
                    text = {
                        Text(text = it)
                    })
            }
        }
    }
}

@Composable
private fun ViewModeToggleButton(
    mode: CalendarViewMode,
    onToggle: () -> Unit
) {
    IconButton(onClick = onToggle) {
        if (mode == CalendarViewMode.List) {
            Icon(
                painter = painterResource(R.drawable.ic_list_view),
                contentDescription = null
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_monthly_view),
                contentDescription = null
            )
        }
    }
}

@Composable
fun CalendarSettingsSection(
    calendars: Map<String, List<Calendar>>,
    onCalendarClicked: (Calendar) -> Unit
) {
    Column {
        HorizontalDivider()
        Text(
            text = stringResource(R.string.include_calendars),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(8.dp)
        )
        HorizontalDivider()
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
                        style = MaterialTheme.typography.bodyLarge
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
                            },
                            text = {
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
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            })
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
