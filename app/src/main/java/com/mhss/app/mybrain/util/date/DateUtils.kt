package com.mhss.app.mybrain.util.date

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mhss.app.mybrain.R
import java.text.SimpleDateFormat
import java.util.*

fun Long.formatDate(): String {
    val sdf = if (DateUtils.isToday(this))
        SimpleDateFormat("h:mm a", Locale.getDefault())
    else
        SimpleDateFormat("MMM dd,yyyy h:mm a", Locale.getDefault())


    return sdf.format(this)
}

fun Long.fullDate(): String {
    val sdf = SimpleDateFormat("MMM dd,yyyy h:mm a", Locale.getDefault())
    return sdf.format(this)
}

fun Long.formatDay(): String {
    val sdf = SimpleDateFormat("EEEE d", Locale.getDefault())
    return sdf.format(this)
}

fun Long.formatTime(): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    val sdfNoMinutes = SimpleDateFormat("h a", Locale.getDefault())
    val minutes = SimpleDateFormat("mm", Locale.getDefault()).format(this)
    return if (minutes == "00") sdfNoMinutes.format(this) else sdf.format(this)
}

fun Long.monthName(): String {
    val sdf = if (this.isCurrentYear())
        SimpleDateFormat("MMMM", Locale.getDefault())
    else
        SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return sdf.format(this)
}

fun Long.inTheLast30Days(): Boolean {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, -1)
    return this > calendar.timeInMillis
}
fun Long.inTheLastYear(): Boolean {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.YEAR, -1)
    return this > calendar.timeInMillis
}

fun Long.inTheLastWeek(): Boolean {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.WEEK_OF_MONTH, -1)
    return this > calendar.timeInMillis
}

fun Long.isCurrentYear(): Boolean {
    val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
    return sdf.format(this) == sdf.format(Date())
}

@Composable
fun formatEventStartEnd(start: Long, end: Long, location: String?, allDay: Boolean): String {
    return if (allDay)
        stringResource(R.string.all_day)
    else
        stringResource(
            id = if (!location.isNullOrBlank())
                R.string.event_time_at
            else R.string.event_time,
            start.formatTime(),
            end.formatTime(),
            location ?: ""
        )
}