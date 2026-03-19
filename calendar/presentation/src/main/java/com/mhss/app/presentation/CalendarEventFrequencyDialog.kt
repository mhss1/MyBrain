package com.mhss.app.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mhss.app.domain.model.CalendarEventFrequency
import com.mhss.app.ui.R
import com.mhss.app.util.date.getDisplayName
import kotlinx.datetime.DayOfWeek

private val recurringWeekDays = listOf(
    DayOfWeek.SUNDAY,
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencyDialog(
    selectedFrequency: CalendarEventFrequency,
    selectedInterval: Int,
    selectedWeekDays: Set<DayOfWeek>,
    fallbackDay: DayOfWeek,
    onApply: (CalendarEventFrequency, Int, Set<DayOfWeek>) -> Unit,
    open: Boolean,
    onClose: () -> Unit,
) {
    val frequencies = remember { CalendarEventFrequency.entries }
    var frequency by remember(open, selectedFrequency) { mutableStateOf(selectedFrequency) }
    var intervalText by remember(open, selectedInterval) {
        mutableStateOf(selectedInterval.coerceAtLeast(1).toString())
    }
    var weekDays by remember(open, selectedWeekDays) { mutableStateOf(selectedWeekDays) }
    val intervalValue = intervalText.toIntOrNull()?.coerceAtLeast(1) ?: 1
    if (open) {
        AlertDialog(
            onDismissRequest = onClose,
            title = { Text(text = stringResource(R.string.repeat)) },
            text = {
                FrequencyDialogContent(
                    frequencies = frequencies,
                    selectedFrequency = frequency,
                    onFrequencySelected = { frequency = it },
                    intervalText = intervalText,
                    onIntervalTextChanged = { newValue ->
                        if (newValue.all(Char::isDigit)) {
                            intervalText = newValue
                        }
                    },
                    weekDays = weekDays,
                    onWeekDayToggled = { day ->
                        weekDays = if (day in weekDays) weekDays - day else weekDays + day
                    },
                    intervalUnit = frequency.getIntervalUnitTitle(intervalValue)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onApply(
                            frequency,
                            intervalValue,
                            if (frequency == CalendarEventFrequency.WEEKLY) {
                                weekDays.ifEmpty { setOf(fallbackDay) }
                            } else {
                                emptySet()
                            }
                        )
                    }
                ) {
                    Text(text = stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onClose) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FrequencyDialogContent(
    frequencies: List<CalendarEventFrequency>,
    selectedFrequency: CalendarEventFrequency,
    onFrequencySelected: (CalendarEventFrequency) -> Unit,
    intervalText: String,
    onIntervalTextChanged: (String) -> Unit,
    weekDays: Set<DayOfWeek>,
    onWeekDayToggled: (DayOfWeek) -> Unit,
    intervalUnit: String,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        frequencies.forEach { frequency ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = frequency.getCalendarFrequencyTitle(),
                    style = MaterialTheme.typography.bodyLarge
                )
                RadioButton(
                    selected = frequency == selectedFrequency,
                    onClick = { onFrequencySelected(frequency) }
                )
            }
        }
        AnimatedVisibility(selectedFrequency != CalendarEventFrequency.NEVER) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = intervalText,
                    onValueChange = onIntervalTextChanged,
                    label = { Text(text = stringResource(R.string.repeats_every)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(100.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                AnimatedContent(
                    targetState = intervalUnit,
                    transitionSpec = {
                        slideInVertically { -it } togetherWith slideOutVertically { it }
                    },
                    label = "Interval unit"
                ) { unit ->
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        AnimatedVisibility(selectedFrequency == CalendarEventFrequency.WEEKLY) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                recurringWeekDays.forEach { dayOfWeek ->
                    FilterChip(
                        selected = dayOfWeek in weekDays,
                        onClick = { onWeekDayToggled(dayOfWeek) },
                        label = { Text(text = dayOfWeek.getDisplayName()) }
                    )
                }
            }
        }
    }
}
