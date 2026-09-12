package com.mhss.app.database

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mhss.app.database.migrations.MIGRATION_6_7
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration6To7Test {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val databaseName = "migration-6-to-7-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = instrumentation,
        file = instrumentation.targetContext.getDatabasePath(databaseName),
        driver = BundledSQLiteDriver(),
        databaseClass = MyBrainDatabase::class,
        databaseFactory = { MyBrainDatabaseConstructor.initialize() }
    )

    @Before
    fun deleteTestDatabase() {
        instrumentation.targetContext.getDatabasePath(databaseName).parentFile?.mkdirs()
        instrumentation.targetContext.deleteDatabase(databaseName)
    }

    @Test
    fun migration6To7ValidatesSchemaAndPreservesData() = runBlocking {
        val before = helper.createDatabase(6).use { connection ->
            connection.execSQL(
                "INSERT INTO note_folders (name, id, sync_seq, updated_date) VALUES ('Folder', 'folder-1', 11, 200)"
            )
            connection.execSQL(
                "INSERT INTO notes (title, content, created_date, updated_date, pinned, folder_id, id, sync_seq) " +
                    "VALUES ('Pinned note', 'Full note content', 100, 200, 1, 'folder-1', 'note-1', 12), " +
                    "('Folderless note', '', 300, 400, 0, NULL, 'note-2', 13)"
            )
            connection.execSQL(
                "INSERT INTO tasks (title, description, is_completed, priority, created_date, updated_date, " +
                    "sub_tasks, dueDate, recurring, frequency, frequency_amount, alarmId, id, sync_seq) " +
                    "VALUES ('Due task', 'Description', 0, 2, 100, 200, '[]', 500, 1, 2, 3, 42, 'task-1', 14), " +
                    "('Completed task', '', 1, 0, 300, 400, '[]', 0, 0, 2, 1, NULL, 'task-2', 15)"
            )
            connection.execSQL(
                "INSERT INTO diary (title, content, created_date, updated_date, mood, id, sync_seq) " +
                    "VALUES ('Diary entry', 'Full diary content', 100, 200, 3, 'diary-1', 16)"
            )
            connection.execSQL(
                "INSERT INTO bookmarks (url, title, description, created_date, updated_date, id, sync_seq) " +
                    "VALUES ('https://example.com', 'Bookmark', 'Description', 100, 200, 'bookmark-1', 17)"
            )
            listOf("note_folders", "notes", "tasks", "diary", "bookmarks")
                .associateWith { connection.readRows(it) }
        }

        helper.runMigrationsAndValidate(7, listOf(MIGRATION_6_7)).use { connection ->
            before.forEach { (table, rows) ->
                assertEquals("Data changed in $table", rows, connection.readRows(table))
            }
            connection.prepare("SELECT dueDate FROM tasks WHERE id = 'task-2'").use { statement ->
                assertTrue(statement.step())
                assertTrue(statement.isNull(0))
            }
            for (direction in listOf("ASC", "DESC")) {
                for (where in listOf("", " WHERE is_completed = 0")) {
                    connection.prepare(
                        "EXPLAIN QUERY PLAN SELECT * FROM tasks$where ORDER BY dueDate $direction NULLS LAST"
                    ).use { statement ->
                        while (statement.step()) {
                            assertFalse(statement.getText(3), statement.getText(3).contains("TEMP B-TREE"))
                        }
                    }
                }
            }
        }
    }

    private suspend fun SQLiteConnection.readRows(table: String): List<List<String?>> =
        prepare("SELECT * FROM `$table` ORDER BY id").use { statement ->
            buildList {
                while (statement.step()) {
                    add(
                        List(statement.getColumnCount()) { column ->
                            val value = if (statement.isNull(column)) null else statement.getText(column)
                            if (table == "tasks" && statement.getColumnName(column) == "dueDate" && value == "0") {
                                null
                            } else {
                                value
                            }
                        }
                    )
                }
            }
        }
}
