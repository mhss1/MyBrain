package com.mhss.app.mybrain.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.calendar.Calendar
import com.mhss.app.mybrain.domain.model.calendar.CalendarEvent
import com.mhss.app.mybrain.presentation.common.DateTimeDialog
import com.mhss.app.mybrain.util.calendar.*
import com.mhss.app.mybrain.util.date.HOUR_MILLIS
import com.mhss.app.mybrain.util.date.formatDate
import com.mhss.app.mybrain.util.date.formatTime
import com.mhss.app.mybrain.util.date.now
import com.mhss.app.mybrain.util.permissions.Permission
import com.mhss.app.mybrain.util.permissions.rememberPermissionState
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarEventDetailsScreen(
    navController: NavHostController,
    eventJson: String?,
    viewModel: CalendarViewModel = koinViewModel()
) {
    val state = viewModel.uiState
    val writeCalendarPermissionState = rememberPermissionState(
        Permission.WRITE_CALENDAR
    )
    var openDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val event by remember {
        mutableStateOf(
            eventJson?.let {
                Json.decodeFromString<CalendarEvent>(it)
            }
        )
    }
    var title by rememberSaveable { mutableStateOf(event?.title ?: "") }
    var description by rememberSaveable { mutableStateOf(event?.description ?: "") }
    var startDate by rememberSaveable {
        mutableLongStateOf(
            event?.start ?: (now() + HOUR_MILLIS)
        )
    }
    var endDate by rememberSaveable {
        mutableLongStateOf(
            event?.end ?: (now() + 2 * HOUR_MILLIS)
        )
    }
    var frequency by rememberSaveable { mutableStateOf(event?.frequency ?: CALENDAR_FREQ_NEVER) }
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
    LaunchedEffect(state.calendarsList) {
        if (event != null) {
            if (state.calendarsList.isNotEmpty()) {
                calendar = state.calendarsList.first { it.id == event!!.calendarId }
            }
        } else {
            if (state.calendarsList.isNotEmpty()) {
                calendar = state.calendarsList.first()
            }
        }
    }

    var allDay by rememberSaveable { mutableStateOf(event?.allDay ?: false) }
    var location by rememberSaveable { mutableStateOf(event?.location ?: "") }
    val snackbarHostState = remember { SnackbarHostState() }
    if (writeCalendarPermissionState.isGranted) {
        LaunchedEffect(true) { viewModel.onEvent(CalendarViewModelEvent.ReadPermissionChanged(true)) }
        LaunchedEffect(state) {
            if (state.navigateUp) {
                openDeleteDialog = false
                navController.navigateUp()
            }
            if (state.error != null) {
                snackbarHostState.showSnackbar(
                    state.error
                )
                viewModel.onEvent(CalendarViewModelEvent.ErrorDisplayed)
            }
        }
        Scaffold(
            topBar = {
                if (event != null) TopAppBar(
                    title = {},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
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
                FloatingActionButton(onClick = {
                    val newEvent = CalendarEvent(
                        id = event?.id ?: 0,
                        title = title,
                        description = description,
                        start = startDate,
                        end = endDate,
                        allDay = allDay,
                        location = location,
                        calendarId = calendar.id,
                        recurring = frequency != CALENDAR_FREQ_NEVER,
                        frequency = frequency
                    )
                    if (event != null) {
                        viewModel.onEvent(CalendarViewModelEvent.EditEvent(newEvent))
                    } else {
                        viewModel.onEvent(CalendarViewModelEvent.AddEvent(newEvent))
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
                onDelete = { viewModel.onEvent(CalendarViewModelEvent.DeleteEvent(event!!)) },
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
                    onFrequencySelected = { frequency = it }
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
        LaunchedEffect(true) { viewModel.onEvent(CalendarViewModelEvent.ReadPermissionChanged(false)) }
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
    frequency: String,
    onFrequencySelected: (String) -> Unit
) {
    var showStartDateDialog by remember {
        mutableStateOf(false)
    }
    var showEndDateDialog by remember {
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
                text = startMillis.formatDate(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable {
                        showStartDateDialog = true
                    }
                    .padding(horizontal = 28.dp, vertical = 16.dp)
            )
            Text(
                text = startMillis.formatTime(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable {
                        showStartDateDialog = true
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
                text = endMillis.formatDate(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable {
                        showEndDateDialog = true
                    }
                    .padding(horizontal = 28.dp, vertical = 16.dp)
            )
            Text(
                text = endMillis.formatTime(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable {
                        showEndDateDialog = true
                    }
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            )
        }
        if (showStartDateDialog) DateTimeDialog(
            onDismissRequest = { showStartDateDialog = false },
            initialDate = startMillis
        ) {
            onStartDateSelected(
                it
            )
            if (it > endMillis) {
                onEndDateSelected(endMillis + HOUR_MILLIS)
            }
            showStartDateDialog = false
        }
        if (showEndDateDialog) DateTimeDialog(
            onDismissRequest = { showEndDateDialog = false },
            initialDate = endMillis
        ) {
            onEndDateSelected(
                it
            )
            if (it < startMillis) {
                onStartDateSelected(it - HOUR_MILLIS)
            }
            showEndDateDialog = false
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
            Text(frequency.toUIFrequency(), style = MaterialTheme.typography.bodyLarge)
            FrequencyDialog(
                selectedFrequency = frequency,
                onFrequencySelected = {
                    onFrequencySelected(it)
                    openDialog = false
                },
                open = openDialog,
                onClose = { openDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencyDialog(
    selectedFrequency: String,
    onFrequencySelected: (String) -> Unit,
    open: Boolean,
    onClose: () -> Unit,
) {
    val frequencies = listOf(
        CALENDAR_FREQ_NEVER,
        CALENDAR_FREQ_DAILY,
        CALENDAR_FREQ_WEEKLY,
        CALENDAR_FREQ_MONTHLY,
        CALENDAR_FREQ_YEARLY
    )
    if (open) BasicAlertDialog(
        onDismissRequest = { onClose() },
        content = {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                frequencies.forEach { frequency ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = frequency.toUIFrequency(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        RadioButton(
                            selected = frequency == selectedFrequency,
                            onClick = { onFrequencySelected(frequency) }
                        )
                    }
                }
            }
        }
    )
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