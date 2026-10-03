package com.mhss.app.database

import com.mhss.app.domain.model.Task
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class TaskDueDateSerializationTest : PlatformTest() {
    @Test
    fun `missing null and legacy zero dates decode as undated`() {
        for (date in listOf("", ",\"dueDate\":null", ",\"dueDate\":0")) {
            val task = Json.decodeFromString<Task>("{\"title\":\"Task\",\"id\":\"task\"$date}")
            assertEquals(null, task.dueDate)
        }
    }

    @Test
    fun `due dates round trip`() {
        for (json in listOf(Json, Json { encodeDefaults = true })) {
            for (date in listOf(null, -100L, 500L)) {
                val task = Task(title = "Task", id = "task", dueDate = date)
                val encoded = json.encodeToString(task)
                assertEquals(task, json.decodeFromString<Task>(encoded))
            }
        }
    }

    @Test
    fun `new undated task syncs to legacy client`() {
        val json = syncJson()
        val encoded = json.encodeToString(Task(title = "Task", id = "task"))

        assertFalse(json.parseToJsonElement(encoded).jsonObject.containsKey("dueDate"))
        assertEquals(0L, json.decodeFromString<LegacyTask>(encoded).dueDate)
    }

    @Test
    fun `legacy tasks sync to new client`() {
        val json = syncJson()

        assertEquals(
            null,
            json.decodeFromString<Task>(json.encodeToString(LegacyTask(title = "Task", id = "task"))).dueDate
        )
        assertEquals(
            500L,
            json.decodeFromString<Task>(
                json.encodeToString(LegacyTask(title = "Task", dueDate = 500L, id = "task"))
            ).dueDate
        )
    }

    private fun syncJson() = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
    }
}

@Serializable
private data class LegacyTask(
    val title: String,
    val dueDate: Long = 0L,
    val id: String
)
