package com.mhss.app.mybrain.presentation.calendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.calendar.Calendar
import com.mhss.app.mybrain.domain.model.calendar.CalendarEvent
import com.mhss.app.mybrain.util.calendar.*
import com.mhss.app.mybrain.util.date.HOUR_MILLIS
import com.mhss.app.mybrain.util.date.formatDate
import com.mhss.app.mybrain.util.date.formatTime
import com.mhss.app.mybrain.util.date.now
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalendarEventDetailsScreen(
    navController: NavHostController,
    eventJson: String?,
    viewModel: CalendarViewModel = koinViewModel()
) {
    val state = viewModel.uiState
    val writeCalendarPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.WRITE_CALENDAR
    )
    var openDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
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
    if (writeCalendarPermissionState.hasPermission) {
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
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
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
                    start = java.util.Calendar.getInstance().apply { timeInMillis = startDate },
                    end = java.util.Calendar.getInstance().apply { timeInMillis = endDate },
                    onStartDateSelected = { startDate = it.timeInMillis },
                    onEndDateSelected = { endDate = it.timeInMillis },
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
            context = context
        ) {
            writeCalendarPermissionState.launchPermissionRequest()
        }
    }
}

@Composable
fun NoWriteCalendarPermissionMessage(
    shouldShowRationale: Boolean,
    context: Context,
    onRequest: () -> Unit
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
    start: java.util.Calendar,
    end: java.util.Calendar,
    onStartDateSelected: (java.util.Calendar) -> Unit,
    onEndDateSelected: (java.util.Calendar) -> Unit,
    allDay: Boolean,
    onAllDayChange: (Boolean) -> Unit,
    frequency: String,
    onFrequencySelected: (String) -> Unit
) {
    val context = LocalContext.current
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
                text = start.timeInMillis.formatDate(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable {
                        showDatePicker(start, context) {
                            val newEvent = java.util.Calendar
                                .getInstance()
                                .apply {
                                    this[java.util.Calendar.YEAR] = it[java.util.Calendar.YEAR]
                                    this[java.util.Calendar.MONTH] =
                                        it[java.util.Calendar.MONTH]
                                    this[java.util.Calendar.DAY_OF_MONTH] =
                                        it[java.util.Calendar.DAY_OF_MONTH]

                                    this[java.util.Calendar.HOUR_OF_DAY] =
                                        start[java.util.Calendar.HOUR_OF_DAY]
                                    this[java.util.Calendar.MINUTE] =
                                        start[java.util.Calendar.MINUTE]
                                }
                            onStartDateSelected(
                                newEvent
                            )
                            if (newEvent.timeInMillis > end.timeInMillis) {
                                onEndDateSelected(newEvent.apply { timeInMillis += HOUR_MILLIS })
                            }
                        }
                    }
                    .padding(horizontal = 28.dp, vertical = 16.dp)
            )
            Text(
                text = start.timeInMillis.formatTime(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable {
                        showTimePicker(start, context) {
                            val newEvent = java.util.Calendar
                                .getInstance()
                                .apply {
                                    this[java.util.Calendar.HOUR_OF_DAY] =
                                        it[java.util.Calendar.HOUR_OF_DAY]
                                    this[java.util.Calendar.MINUTE] =
                                        it[java.util.Calendar.MINUTE]

                                    this[java.util.Calendar.YEAR] =
                                        start[java.util.Calendar.YEAR]
                                    this[java.util.Calendar.MONTH] =
                                        start[java.util.Calendar.MONTH]
                                    this[java.util.Calendar.DAY_OF_MONTH] =
                                        start[java.util.Calendar.DAY_OF_MONTH]
                                }
                            onStartDateSelected(
                                newEvent
                            )
                            if (newEvent.timeInMillis > end.timeInMillis) {
                                onEndDateSelected(newEvent.apply { timeInMillis += HOUR_MILLIS })
                            }
                        }
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
                text = end.timeInMillis.formatDate(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable {
                        showDatePicker(end, context) {
                            val newEvent = java.util.Calendar
                                .getInstance()
                                .apply {
                                    this[java.util.Calendar.YEAR] = it[java.util.Calendar.YEAR]
                                    this[java.util.Calendar.MONTH] =
                                        it[java.util.Calendar.MONTH]
                                    this[java.util.Calendar.DAY_OF_MONTH] =
                                        it[java.util.Calendar.DAY_OF_MONTH]

                                    this[java.util.Calendar.HOUR_OF_DAY] =
                                        end[java.util.Calendar.HOUR_OF_DAY]
                                    this[java.util.Calendar.MINUTE] =
                                        end[java.util.Calendar.MINUTE]
                                }
                            onEndDateSelected(
                                newEvent
                            )
                            if (newEvent.timeInMillis < start.timeInMillis) {
                                onStartDateSelected(newEvent.apply { timeInMillis -= HOUR_MILLIS })
                            }
                        }
                    }
                    .padding(horizontal = 28.dp, vertical = 16.dp)
            )
            Text(
                text = end.timeInMillis.formatTime(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable {
                        showTimePicker(end, context) {
                            val newEvent = java.util.Calendar
                                .getInstance()
                                .apply {
                                    this[java.util.Calendar.HOUR_OF_DAY] =
                                        it[java.util.Calendar.HOUR_OF_DAY]
                                    this[java.util.Calendar.MINUTE] =
                                        it[java.util.Calendar.MINUTE]

                                    this[java.util.Calendar.YEAR] =
                                        end[java.util.Calendar.YEAR]
                                    this[java.util.Calendar.MONTH] =
                                        end[java.util.Calendar.MONTH]
                                    this[java.util.Calendar.DAY_OF_MONTH] =
                                        end[java.util.Calendar.DAY_OF_MONTH]
                                }
                            onEndDateSelected(
                                newEvent
                            )
                            if (newEvent.timeInMillis < start.timeInMillis) {
                                onStartDateSelected(newEvent.apply { timeInMillis -= HOUR_MILLIS })
                            }
                        }
                    }
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            )
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

fun showDatePicker(
    initialDate: java.util.Calendar,
    context: Context,
    onDateSelected: (java.util.Calendar) -> Unit
) {
    val tempDate = java.util.Calendar.getInstance()
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            tempDate[java.util.Calendar.YEAR] = year
            tempDate[java.util.Calendar.MONTH] = month
            tempDate[java.util.Calendar.DAY_OF_MONTH] = day
            onDateSelected(tempDate)
        },
        initialDate[java.util.Calendar.YEAR],
        initialDate[java.util.Calendar.MONTH],
        initialDate[java.util.Calendar.DAY_OF_MONTH]
    )
    datePicker.show()
}

fun showTimePicker(
    initialDate: java.util.Calendar,
    context: Context,
    onTimeSelected: (java.util.Calendar) -> Unit
) {
    val tempDate = java.util.Calendar.getInstance()
    val timePicker = TimePickerDialog(
        context,
        { _, hour, minute ->
            tempDate[java.util.Calendar.HOUR_OF_DAY] = hour
            tempDate[java.util.Calendar.MINUTE] = minute
            onTimeSelected(tempDate)
        },
        initialDate[java.util.Calendar.HOUR_OF_DAY],
        initialDate[java.util.Calendar.MINUTE],
        false
    )
    timePicker.show()
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