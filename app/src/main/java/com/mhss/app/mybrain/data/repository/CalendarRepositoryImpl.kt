package com.mhss.app.mybrain.data.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import com.mhss.app.mybrain.domain.model.Calendar
import com.mhss.app.mybrain.domain.model.CalendarEvent
import com.mhss.app.mybrain.domain.repository.CalendarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CalendarRepositoryImpl(private val context: Context) : CalendarRepository {

    override suspend fun getEvents(): List<CalendarEvent> {
        return withContext(Dispatchers.IO) {
            val projection: Array<String> = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.ALL_DAY,
                CalendarContract.Events.CALENDAR_COLOR,
                CalendarContract.Events.CALENDAR_ID,
            )

            val uri: Uri = CalendarContract.Events.CONTENT_URI
            val contentResolver = context.contentResolver
            val cur: Cursor? = contentResolver.query(
                uri,
                projection,
                "${CalendarContract.Events.DTSTART} > ?",
                // events today or in the future only
                arrayOf(System.currentTimeMillis().toString()),
                "${CalendarContract.Events.DTSTART} ASC"
            )

            val events: MutableList<CalendarEvent> = mutableListOf()
            if (cur != null) {
                while (cur.moveToNext()) {
                    val eventId: Long = cur.getLong(ID_INDEX)
                    val title: String = cur.getString(TITLE_INDEX) ?: continue
                    val description: String? = cur.getString(DESC_INDEX)
                    val start: Long = cur.getLong(START_INDEX)
                    val end: Long = cur.getLong(END_INDEX)
                    val location: String? = cur.getString(LOCATION_INDEX)
                    val allDay: Boolean = cur.getInt(ALL_DAY_INDEX) == 1
                    val color: Int = cur.getInt(COlOR_INDEX)
                    val calendarId: Long = cur.getLong(EVENT_CALENDAR_ID_INDEX)

                    events.add(CalendarEvent(eventId, title, description, start, end, location, allDay, color, calendarId))
                }
                cur.close()
                events
            }else
                emptyList()
        }
    }

    override suspend fun getCalendars() : List<Calendar>{
        return withContext(Dispatchers.IO){
            val projection = arrayOf(
                CalendarContract.Calendars._ID,                     // 0
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 1
                CalendarContract.Calendars.ACCOUNT_NAME, // 2
                CalendarContract.Calendars.ACCOUNT_TYPE, // 3
                CalendarContract.Calendars.CALENDAR_COLOR// 4

            )
            val uri = CalendarContract.Calendars.CONTENT_URI
            val contentResolver = context.contentResolver
            val cur = contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )
            val calendars: MutableList<Calendar> = mutableListOf()
            if (cur != null) {
                while (cur.moveToNext()) {

                    val calID: Long = cur.getLong(CALENDAR_ID_INDEX)
                    val calendarName: String = cur.getString(CALENDAR_NAME_INDEX)
                    val accountName: String = cur.getString(ACCOUNT_NAME_INDEX)
                    val color: Int = cur.getInt(CALENDAR_CALENDAR_COLOR_INDEX)

                    calendars.add(
                        Calendar(
                            calID,
                            calendarName,
                            accountName,
                            color
                        )
                    )
                }
                cur.close()
                calendars
            }else
                emptyList()
        }
    }

    companion object {
        private const val ID_INDEX = 0
        private const val TITLE_INDEX = 1
        private const val DESC_INDEX = 2
        private const val START_INDEX = 3
        private const val END_INDEX = 4
        private const val LOCATION_INDEX = 5
        private const val ALL_DAY_INDEX = 6
        private const val COlOR_INDEX = 7
        private const val EVENT_CALENDAR_ID_INDEX = 8

        private const val CALENDAR_ID_INDEX: Int = 0
        private const val CALENDAR_NAME_INDEX: Int = 1
        private const val ACCOUNT_NAME_INDEX: Int = 2
        private const val CALENDAR_CALENDAR_COLOR_INDEX: Int = 4
    }
}