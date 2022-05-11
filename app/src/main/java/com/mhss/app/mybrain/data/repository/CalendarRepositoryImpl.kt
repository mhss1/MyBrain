package com.mhss.app.mybrain.data.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
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

                    events.add(CalendarEvent(eventId, title, description, start, end, location, allDay))
                }
                cur.close()
                events
            }else
                emptyList()
        }
    }

    companion object {
        const val ID_INDEX = 0
        const val TITLE_INDEX = 1
        const val DESC_INDEX = 2
        const val START_INDEX = 3
        const val END_INDEX = 4
        const val LOCATION_INDEX = 5
        const val ALL_DAY_INDEX = 6
    }
}