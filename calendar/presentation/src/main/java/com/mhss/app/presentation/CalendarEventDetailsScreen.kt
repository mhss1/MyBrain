package com.mhss.app.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mhss.app.domain.model.Calendar
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.model.CalendarEventFrequency
import com.mhss.app.ui.R
import com.mhss.app.ui.components.common.DateDialog
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.components.common.TimeDialog
import com.mhss.app.ui.snackbar.LocalisedSnackbarHost
import com.mhss.app.util.date.HOUR_MILLIS
import com.mhss.app.util.date.formatDate
import com.mhss.app.util.date.formatTime
import com.mhss.app.util.date.now
import com.mhss.app.util.date.toDayOfWeek
import com.mhss.app.util.permissions.Permission
import com.mhss.app.util.permissions.rememberPermissionState
import kotlinx.datetime.DayOfWeek
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CalendarEventDetailsScreen(
    navController: NavHostController,
    eventId: Long?,
    initialStartMillis: Long? = null,
    viewModel: CalendarEventDetailsViewModel = koinViewModel(
        parameters = { parametersOf(eventId) }
    )
) {
    val state = viewModel.uiState
    val writeCalendarPermissionState = rememberPermissionState(
        Permission.WRITE_CALENDAR
    )
    var openDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val event = state.event
    var title by rememberSaveable(event) { mutableStateOf(event?.title ?: "") }
    var description by rememberSaveable(event) { mutableStateOf(event?.description ?: "") }
    var startDate by rememberSaveable(event, initialStartMillis) {
        mutableLongStateOf(
            event?.start ?: initialStartMillis ?: (now() + HOUR_MILLIS)
        )
    }
    var endDate by rememberSaveable(event, initialStartMillis) {
        mutableLongStateOf(
            event?.end ?: (initialStartMillis?.plus(HOUR_MILLIS) ?: (now() + 2 * HOUR_MILLIS))
        )
    }
    var frequency by rememberSaveable(event) {
        mutableStateOf(
            event?.frequency ?: CalendarEventFrequency.NEVER
        )
    }
    var interval by rememberSaveable(event) {
        mutableIntStateOf(event?.interval?.coerceAtLeast(1) ?: 1)
    }
    var weekDays by rememberSaveable(event, stateSaver = dayOfWeekSetSaver) {
        mutableStateOf(event?.weekDays ?: emptySet())
    }
    var calendar by remember {
        mutableStateOf(
            Calendar(
                id = 1,
                name = "",
                color = Color.Black.toArgb(),
                account = ""
            )
        )
    }
    LaunchedEffect(state.calendarsList, event) {
        if (event != null) {
            if (state.calendarsList.isNotEmpty()) {
                state.calendarsList.firstOrNull { it.id == event.calendarId }?.let {
                    calendar = it
                }
            }
        } else {
            if (state.calendarsList.isNotEmpty()) {
                calendar = state.calendarsList.first()
            }
        }
    }

    var allDay by rememberSaveable(event) { mutableStateOf(event?.allDay ?: false) }
    var location by rememberSaveable(event) { mutableStateOf(event?.location ?: "") }
    if (writeCalendarPermissionState.isGranted) {
        LaunchedEffect(state) {
            if (state.navigateUp) {
                navController.navigateUp()
            }
        }
        Scaffold(
            snackbarHost = { LocalisedSnackbarHost(state.snackbarHostState) },
            topBar = {
                if (event != null) MyBrainAppBar(
                    title = "",
                    actions = {
                        IconButton(onClick = { openDeleteDialog = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete),
                                contentDescription = stringResource(R.string.delete_event)
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                if (!state.isLoading) FloatingActionButton(onClick = {
                    val newEvent = CalendarEvent(
                        id = event?.id ?: 0,
                        title = title,
                        description = description,
                        start = startDate,
                        end = endDate,
                        allDay = allDay,
                        location = location,
                        calendarId = calendar.id,
                        recurring = frequency != CalendarEventFrequency.NEVER,
                        frequency = frequency,
                        interval = interval.coerceAtLeast(1),
                        weekDays = if (frequency == CalendarEventFrequency.WEEKLY) {
                            weekDays.ifEmpty { setOf(startDate.toDayOfWeek()) }
                        } else {
                            emptySet()
                        }
                    )
                    if (event != null) {
                        viewModel.onEvent(CalendarEventDetailsEvent.EditEvent(newEvent))
                    } else {
                        viewModel.onEvent(CalendarEventDetailsEvent.AddEvent(newEvent))
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_save),
                        contentDescription = stringResource(R.string.add_event)
                    )
                }
            }
        ) { paddingValues ->
            DeleteEventDialog(
                openDeleteDialog,
                onDelete = { viewModel.onEvent(CalendarEventDetailsEvent.DeleteEvent(event!!)) },
                onDismiss = { openDeleteDialog = false }
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(text = stringResource(R.string.title)) },
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                CalendarChoiceSection(
                    selectedCalendar = calendar,
                    calendars = state.calendarsList,
                    onCalendarSelected = { calendar = it }
                )
                Spacer(Modifier.height(8.dp))
                EventTimeSection(
                    startMillis = startDate,
                    endMillis = endDate,
                    onStartDateSelected = { startDate = it },
                    onEndDateSelected = { endDate = it },
                    allDay = allDay,
                    onAllDayChange = { allDay = it },
                    frequency = frequency,
                    onFrequencySelected = { frequency = it },
                    interval = interval,
                    onIntervalSelected = { interval = it },
                    weekDays = weekDays,
                    onWeekDaysSelected = { weekDays = it }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(text = stringResource(R.string.location)) },
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(painter = painterResource(id = R.drawable.ic_location), null)
                    }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text = stringResource(R.string.description)) },
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(painter = painterResource(id = R.drawable.ic_description), null)
                    }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    } else {
        NoWriteCalendarPermissionMessage(
            shouldShowRationale = writeCalendarPermissionState.shouldShowRationale,
            onOpenSettings = {
                writeCalendarPermissionState.openAppSettings()
            },
            onRequest = {
                writeCalendarPermissionState.launchRequest()
            }
        )
    }
}

@Composable
fun NoWriteCalendarPermissionMessage(
    shouldShowRationale: Boolean,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.no_write_calendar_permission_message),
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
fun CalendarChoiceSection(
    selectedCalendar: Calendar,
    calendars: List<Calendar>,
    onCalendarSelected: (Calendar) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(selectedCalendar.color))
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = selectedCalendar.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = selectedCalendar.account,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            calendars.forEach { calendar ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onCalendarSelected(calendar)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    text = {
                        Box(
                            Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(calendar.color))
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = calendar.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = calendar.account,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    })
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun EventTimeSection(
    startMillis: Long,
    endMillis: Long,
    onStartDateSelected: (Long) -> Unit,
    onEndDateSelected: (Long) -> Unit,
    allDay: Boolean,
    onAllDayChange: (Boolean) -> Unit,
    frequency: CalendarEventFrequency,
    onFrequencySelected: (CalendarEventFrequency) -> Unit,
    interval: Int,
    onIntervalSelected: (Int) -> Unit,
    weekDays: Set<DayOfWeek>,
    onWeekDaysSelected: (Set<DayOfWeek>) -> Unit
) {
    val context = LocalContext.current
    val startDayOfWeek = remember(startMillis) { startMillis.toDayOfWeek() }
    val formattedStartDate by remember(startMillis) {
        derivedStateOf { startMillis.formatDate(forceShowYear = true) }
    }
    val formattedStartTime by remember(startMillis) {
        derivedStateOf { startMillis.formatTime(context) }
    }
    val formattedEndDate by remember(endMillis) {
        derivedStateOf { endMillis.formatDate(forceShowYear = true) }
    }
    val formattedEndTime by remember(endMillis) {
        derivedStateOf { endMillis.formatTime(context) }
    }
    var showStartDateDialog by remember {
        mutableStateOf(false)
    }
    var showEndDateDialog by remember {
        mutableStateOf(false)
    }
    var showStartTimeDialog by remember {
        mutableStateOf(false)
    }
    var showEndTimeDialog by remember {
        mutableStateOf(false)
    }
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painter = painterResource(R.drawable.ic_time), null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.all_day),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Switch(
                checked = allDay,
                onCheckedChange = { onAllDayChange(it) }
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = formattedStartDate,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable {
                        showStartDateDialog = true
                    }
                    .padding(horizontal = 28.dp, vertical = 16.dp)
            )
            Text(
                text = formattedStartTime,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable {
                        showStartTimeDialog = true
                    }
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = formattedEndDate,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable {
                        showEndDateDialog = true
                    }
                    .padding(horizontal = 28.dp, vertical = 16.dp)
            )
            Text(
                text = formattedEndTime,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable {
                        showEndTimeDialog = true
                    }
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            )
        }
        if (showStartDateDialog) DateDialog(
            onDismissRequest = { showStartDateDialog = false },
            initialDate = startMillis
        ) {
            onStartDateSelected(it)
            if (it > endMillis) {
                onEndDateSelected(it + HOUR_MILLIS)
            }
            showStartDateDialog = false
        }
        if (showStartTimeDialog) TimeDialog(
            onDismissRequest = { showStartTimeDialog = false },
            initialDate = startMillis
        ) {
            onStartDateSelected(it)
            if (it > endMillis) {
                onEndDateSelected(it + HOUR_MILLIS)
            }
            showStartTimeDialog = false
        }
        if (showEndDateDialog) DateDialog(
            onDismissRequest = { showEndDateDialog = false },
            initialDate = endMillis
        ) {
            onEndDateSelected(it)
            if (it < startMillis) {
                onStartDateSelected(it - HOUR_MILLIS)
            }
            showEndDateDialog = false
        }
        if (showEndTimeDialog) TimeDialog(
            onDismissRequest = { showEndTimeDialog = false },
            initialDate = endMillis
        ) {
            onEndDateSelected(it)
            if (it < startMillis) {
                onStartDateSelected(it - HOUR_MILLIS)
            }
            showEndTimeDialog = false
        }
        var openDialog by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .clickable { openDialog = true }
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_refresh), null)
            Spacer(Modifier.width(8.dp))
            Text(
                frequency.getCalendarFrequencyTitle(
                    interval = interval,
                    weekDays = weekDays
                ),
                style = MaterialTheme.typography.bodyLarge
            )
            FrequencyDialog(
                selectedFrequency = frequency,
                selectedInterval = interval,
                selectedWeekDays = weekDays,
                fallbackDay = startDayOfWeek,
                onApply = { selectedFrequency, selectedInterval, selectedWeekDays ->
                    onFrequencySelected(selectedFrequency)
                    onIntervalSelected(selectedInterval)
                    onWeekDaysSelected(selectedWeekDays)
                    openDialog = false
                },
                open = openDialog,
                onClose = { openDialog = false }
            )
        }
    }
}

@Composable
fun DeleteEventDialog(
    openDialog: Boolean,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    if (openDialog) AlertDialog(
        shape = RoundedCornerShape(25.dp),
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.delete_event_confirmation_title)) },
        text = {
            Text(
                stringResource(
                    R.string.delete_event_confirmation_message
                )
            )
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(25.dp),
                onClick = {
                    onDelete()
                },
            ) {
                Text(stringResource(R.string.delete_event), color = Color.White)
            }
        },
        dismissButton = {
            Button(
                shape = RoundedCornerShape(25.dp),
                onClick = {
                    onDismiss()
                }) {
                Text(stringResource(R.string.cancel), color = Color.White)
            }
        }
    )
}
