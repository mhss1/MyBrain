package com.mhss.app.database

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.mhss.app.database.migrations.MIGRATION_6_7
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class Migration6To7Test : PlatformTest() {
    private val databaseName = "migration-6-to-7-test.db"
    private lateinit var helper: MigrationTestHelper

    @BeforeTest
    fun prepareDatabase() {
        deleteTestDatabase(databaseName)
        helper = createMigrationTestHelper(databaseName)
    }

    @AfterTest
    fun cleanUpDatabase() {
        deleteTestDatabase(databaseName)
    }

    @Test
    fun `migration 6 to 7 validates schema and preserves data`() = runTest {
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
                assertEquals(rows, connection.readRows(table), "Data changed in $table")
            }
            connection.prepare("SELECT dueDate FROM tasks WHERE id = 'task-2'").use { statement ->
                assertTrue(statement.step())
                assertTrue(statement.isNull(0))
            }
            for (direction in listOf("ASC", "DESC")) {
                for (where in listOf("", " WHERE is_completed = 0")) {
                    val plan = readQueryPlan(
                        databaseName,
                        "EXPLAIN QUERY PLAN SELECT * FROM tasks$where ORDER BY dueDate $direction NULLS LAST"
                    )
                    assertFalse(plan.any { it.contains("TEMP B-TREE") }, plan.joinToString())
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
