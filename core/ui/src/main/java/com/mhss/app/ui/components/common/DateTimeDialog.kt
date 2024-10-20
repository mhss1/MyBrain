package com.mhss.app.ui.components.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.R
import com.mhss.app.util.date.at
import com.mhss.app.util.date.hour
import com.mhss.app.util.date.minute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeDialog(
    initialDate: Long,
    onDismissRequest: () -> Unit,
    onDatePicked: (Long) -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
    )
    val timePickerState = rememberTimePickerState(
        initialHour = initialDate.hour,
        initialMinute = initialDate.minute
    )
    var showTime by remember {
        mutableStateOf(false)
    }
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxWidth(),
        confirmButton = {
            TextButton(
                onClick = {
                    if (showTime) {
                        onDatePicked(
                            datePickerState.selectedDateMillis?.at(
                                timePickerState.hour,
                                timePickerState.minute
                            ) ?: initialDate
                        )
                    } else showTime = true
                },
            ) {
                Text(stringResource(R.string.okay))
            }
        }
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(targetState = showTime, label = "") { isTime ->
                    if (isTime) {
                        TimeInput(
                            timePickerState,
                            modifier = Modifier.padding(12.dp)
                        )
                    } else {
                        DatePicker(
                            datePickerState,
                            showModeToggle = false
                        )
                    }
                }
            }
        }

    }
}