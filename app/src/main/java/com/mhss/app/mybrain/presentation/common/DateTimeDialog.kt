package com.mhss.app.mybrain.presentation.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.util.date.localDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeDialog(
    initialDate: Long,
    onDismissRequest: () -> Unit,
    onDatePicked: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
    )
    val timePickerState = rememberTimePickerState(
        initialHour = initialDate.localDateTime.hour,
        initialMinute = initialDate.localDateTime.minute
    )
    var showTime by remember {
        mutableStateOf(false)
    }
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                AnimatedContent(
                    showTime, label = "",
                    modifier = Modifier.fillMaxWidth()
                ) { isTime ->
                    if (isTime) {
                        TimePicker(
                            timePickerState
                        )
                    } else {
                        DatePicker(
                            datePickerState,
                            showModeToggle = false
                        )
                    }
                }
                Button(
                    onClick = {
                        if (showTime) {
                            datePickerState.selectedDateMillis?.localDateTime?.let {
                                onDatePicked(
                                    LocalDateTime(
                                        year = it.year,
                                        month = it.month,
                                        dayOfMonth = it.dayOfMonth,
                                        hour = timePickerState.hour,
                                        minute = timePickerState.minute,
                                        second = 0,
                                    ).toInstant(TimeZone.currentSystemDefault())
                                        .toEpochMilliseconds()
                                )
                            }
                        } else showTime = true
                    },
                ) {
                    Text(stringResource(R.string.okay))
                }
            }
        }
    }
}