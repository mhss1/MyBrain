package com.mhss.app.storage

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StorageManagerImplTest {

    private lateinit var context: Context
    private lateinit var storageManager: StorageManagerImpl
    private val files = mutableListOf<File>()

    @BeforeTest
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        storageManager = StorageManagerImpl(context, Dispatchers.Unconfined)
    }

    @AfterTest
    fun tearDown() {
        files.forEach(File::delete)
    }

    @Test
    fun `reader streams selected arrays with exact json values`() = runTest {
        val notes = (0..100).joinToString(",") { index ->
            """{"id":$index,"title":"Note $index","content":"Line one\nLine two with \"quotes\"","nested":{"values":[1,true,null]}}"""
        }
        val fileUri = writeBackup(
            """
                {
                  "schemaVersion": 2,
                  "notes": [$notes],
                  "ignored": [{"id":"ignored"}],
                  "tasks": [{"id":"task-1","subTasks":[{"id":"subtask-1"}]}]
                }
            """.trimIndent()
        )
        val items = mutableListOf<Pair<String, JsonElement>>()

        val result = storageManager.readJsonArraysFromFile(
            fileUri = fileUri,
            arrayNames = setOf("notes", "tasks")
        ) { arrayName, item -> items.add(arrayName to item) }

        assertEquals(ReadJsonFileResult.Success, result)
        assertEquals(102, items.size)
        assertEquals(101, items.count { it.first == "notes" })
        assertEquals(
            listOf("0", "1", "2"),
            items.filter { it.first == "notes" }
                .take(3)
                .map { it.second.jsonObject.getValue("id").jsonPrimitive.content }
        )
        assertEquals("100", items[100].second.jsonObject.getValue("id").toString())
        assertEquals(
            Json.parseToJsonElement("""{"id":"task-1","subTasks":[{"id":"subtask-1"}]}"""),
            items.last().second
        )
    }

    @Test
    fun `reader returns failure for malformed json`() = runTest {
        val fileUri = writeBackup("""{"notes":[{"id":"note-1"}""")

        val result = storageManager.readJsonArraysFromFile(
            fileUri = fileUri,
            arrayNames = setOf("notes")
        ) { _, _ -> }

        assertEquals(ReadJsonFileResult.CouldNotReadFile, result)
    }

    @Test
    fun `reader stops after all selected arrays are consumed`() = runTest {
        val fileUri = writeBackup("""{"notes":[{"id":"note-1"}],"broken":[""")
        val items = mutableListOf<JsonElement>()

        val result = storageManager.readJsonArraysFromFile(
            fileUri = fileUri,
            arrayNames = setOf("notes")
        ) { _, item -> items.add(item) }

        assertEquals(ReadJsonFileResult.Success, result)
        assertEquals("note-1", items.single().jsonObject.getValue("id").jsonPrimitive.content)
    }

    @Test
    fun `reader propagates item callback failures`() = runTest {
        val fileUri = writeBackup("""{"notes":[{"id":"note-1"}]}""")

        val error = assertFailsWith<IllegalStateException> {
            storageManager.readJsonArraysFromFile(
                fileUri = fileUri,
                arrayNames = setOf("notes")
            ) { _, _ -> throw IllegalStateException("failure") }
        }

        assertEquals("failure", error.message)
    }

    private fun writeBackup(content: String): String {
        val file = File.createTempFile("backup-", ".json", context.cacheDir)
        files.add(file)
        file.writeText(content)
        return Uri.fromFile(file).toString()
    }
}
