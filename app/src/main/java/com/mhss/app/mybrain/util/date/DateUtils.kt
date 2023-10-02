package com.mhss.app.mybrain.util.date

import android.text.format.DateFormat
import android.text.format.DateUtils
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.app.MyBrainApplication.Companion.appContext
import com.mhss.app.mybrain.app.getString
import java.text.SimpleDateFormat
import java.util.*

fun Long.formatDateDependingOnDay(): String {
    val hourPatternString = if (is24Hour()) "H:mm" else "h:mm a"

    val sdf = if (DateUtils.isToday(this))
        SimpleDateFormat(hourPatternString, Locale.getDefault())
    else
        SimpleDateFormat("MMM dd,yyyy $hourPatternString", Locale.getDefault())


    return sdf.format(this)
}

fun Long.fullDate(): String {
    val hourPatternString = if (is24Hour()) "H:mm" else "h:mm a"
    val sdf = SimpleDateFormat("MMM dd,yyyy $hourPatternString", Locale.getDefault())
    return sdf.format(this)
}

fun Long.formatDateForMapping(): String {
    val sdf = SimpleDateFormat("EEEE d, MMM yyy", Locale.getDefault())
    return sdf.format(this)
}

fun Long.formatTime(): String {
    val is24 = is24Hour()
    val sdf = SimpleDateFormat(if (is24) "H:mm" else "h:mm a", Locale.getDefault())
    val sdfNoMinutes = SimpleDateFormat(if (is24) "H:mm" else "h a", Locale.getDefault())
    val minutes = SimpleDateFormat("mm", Locale.getDefault()).format(this)
    return if (minutes == "00") sdfNoMinutes.format(this) else sdf.format(this)
}

fun Long.formatDate(): String {
    val sdf = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
    return sdf.format(this)
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

fun Long.isDueDateOverdue(): Boolean {
    return this < System.currentTimeMillis()
}

fun formatEventStartEnd(start: Long, end: Long, location: String?, allDay: Boolean): String {
    return if (allDay)
        getString(R.string.all_day)
    else
        getString(
            if (!location.isNullOrBlank())
                R.string.event_time_at
            else R.string.event_time,
            start.formatTime(),
            end.formatTime(),
            location ?: ""
        )
}

fun is24Hour() = DateFormat.is24HourFormat(appContext)

const val HOUR_IN_MILLIS = 60 * 60 * 1000L