package com.mhss.app.database

import androidx.paging.PagingSource
import com.mhss.app.database.dao.BookmarkOrder
import com.mhss.app.database.dao.NoteOrder
import com.mhss.app.database.dao.QueryOrder
import com.mhss.app.database.dao.TaskOrder
import com.mhss.app.database.entity.BookmarkEntity
import com.mhss.app.database.entity.NoteEntity
import com.mhss.app.database.entity.TaskEntity
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class PagingQueriesTest : PlatformTest() {
    private lateinit var database: MyBrainDatabase

    @BeforeTest
    fun createDatabase() {
        database = createTestDatabase()
    }

    @AfterTest
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun `bookmarks page in order and search all fields`() = runTest {
        val dao = database.bookmarkDao()
        dao.upsertBookmarks((6 downTo 0).map {
            BookmarkEntity(
                id = "bookmark-$it",
                title = if (it == 0) "Needle" else "Same",
                description = if (it == 3) "needle" else "",
                url = if (it == 6) "https://needle.example" else "https://example.com",
                updatedDate = it.toLong()
            )
        })
        val ids = dao.getPagedBookmarks(BookmarkOrder.UPDATED_DATE, QueryOrder.DESC).readAll().map { it.id }
        assertEquals((6 downTo 0).map { "bookmark-$it" }, ids)
        assertEquals(
            listOf("bookmark-6", "bookmark-3", "bookmark-0"),
            dao.searchPagedBookmarks("needle").readAll().map { it.id }
        )
        assertTrue(dao.searchPagedBookmarks("missing").readAll().isEmpty())
    }

    @Test
    fun `task pages keep undated last and invalidate after completion`() = runTest {
        val dao = database.taskDao()
        dao.upsertTasks(listOf(
            TaskEntity(id = "undated", title = "Task"),
            TaskEntity(id = "later", title = "Task", dueDate = 200),
            TaskEntity(id = "earlier-b", title = "Task", dueDate = 101),
            TaskEntity(id = "earlier-a", title = "Task", dueDate = 100),
            TaskEntity(id = "done", title = "Task", dueDate = 50, isCompleted = true)
        ))
        val source = dao.getPagedTasks(TaskOrder.DUE_DATE, QueryOrder.ASC, false)
        val invalidated = CompletableDeferred<Unit>()
        source.registerInvalidatedCallback { invalidated.complete(Unit) }
        assertEquals(listOf("earlier-a", "earlier-b", "later", "undated"), source.readAll().map { it.id })
        assertEquals(
            listOf("later", "earlier-b", "earlier-a", "done", "undated"),
            dao.getPagedTasks(TaskOrder.DUE_DATE, QueryOrder.DESC, true).readAll().map { it.id }
        )
        dao.updateCompleted("earlier-a", true, 2, 200)
        withContext(Dispatchers.Default) { withTimeout(5_000) { invalidated.await() } }
        assertEquals(
            listOf("earlier-b", "later", "undated"),
            dao.getPagedTasks(TaskOrder.DUE_DATE, QueryOrder.ASC, false).readAll().map { it.id }
        )
        assertEquals(5, dao.searchPagedTasks("task").readAll().size)
    }

    @Test
    fun `note pages respect pins folders and search beyond preview`() = runTest {
        val dao = database.noteDao()
        dao.upsertNotes(listOf(
            NoteEntity(id = "root", title = "Root", updatedDate = 300),
            NoteEntity(id = "pinned-root", title = "Pinned", pinned = true, updatedDate = 100),
            NoteEntity(id = "folder-a", title = "Folder", folderId = "folder", updatedDate = 201),
            NoteEntity(id = "folder-b", title = "Folder", folderId = "folder", updatedDate = 200),
            NoteEntity(id = "pinned-folder", title = "Pinned", folderId = "folder", pinned = true,
                content = "x".repeat(200) + "needle", updatedDate = 200)
        ))
        assertEquals(
            listOf("pinned-root", "root"),
            dao.getPagedNotes(NoteOrder.UPDATED_DATE, QueryOrder.DESC, false).readAll().map { it.id }
        )
        val all = dao.getPagedNotes(NoteOrder.UPDATED_DATE, QueryOrder.DESC, true).readAll()
        assertEquals(listOf("pinned-folder", "pinned-root", "root", "folder-a", "folder-b"), all.map { it.id })
        assertEquals(150, all.first().content.length)
        assertEquals(
            listOf("pinned-folder", "folder-a", "folder-b"),
            dao.getPagedNotesByFolder("folder", NoteOrder.UPDATED_DATE, QueryOrder.DESC).readAll().map { it.id }
        )
        val search = dao.searchPagedNotes("needle").readAll()
        assertEquals(listOf("pinned-folder"), search.map { it.id })
        assertEquals(100, search.single().content.length)
        assertTrue(dao.getPagedNotesByFolder("missing", NoteOrder.TITLE, QueryOrder.ASC).readAll().isEmpty())
    }

    private suspend fun <T : Any> PagingSource<Int, T>.readAll(): List<T> {
        val items = mutableListOf<T>()
        var params: PagingSource.LoadParams<Int> = PagingSource.LoadParams.Refresh(
            key = null, loadSize = 2, placeholdersEnabled = true
        )
        while (true) {
            val result = load(params)
            check(result is PagingSource.LoadResult.Page) { "Unexpected paging result: $result" }
            assertTrue(result.data.size <= 2)
            assertEquals(items.size, result.itemsBefore)
            assertTrue(result.itemsAfter >= 0)
            items.addAll(result.data)
            val next = result.nextKey ?: return items
            params = PagingSource.LoadParams.Append(next, loadSize = 2, placeholdersEnabled = true)
        }
    }
}
