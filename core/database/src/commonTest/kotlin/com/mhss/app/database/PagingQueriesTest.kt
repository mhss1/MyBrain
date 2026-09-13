package com.mhss.app.database

import androidx.paging.PagingSource
import com.mhss.app.database.dao.DiaryOrder
import com.mhss.app.database.entity.AssistantThreadEntity
import com.mhss.app.database.entity.DiaryEntryEntity
import com.mhss.app.database.entity.NoteFolderEntity
import com.mhss.app.domain.model.Mood
import kotlinx.coroutines.flow.first
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

    @Test
    fun `thread pages sort by activity and refresh after activity and deletion`() = runTest {
        val dao = database.assistantDao()
        dao.upsertThreads((4 downTo 0).map {
            AssistantThreadEntity(id = "thread-$it", title = "Chat", createdAt = 0, updatedAt = 100L - it)
        })
        val source = dao.getPagedThreads()
        val invalidated = CompletableDeferred<Unit>()
        source.registerInvalidatedCallback { invalidated.complete(Unit) }
        assertEquals((0..4).map { "thread-$it" }, source.readAll().map { it.id })
        dao.updateThreadLastActive("thread-4", 200)
        withContext(Dispatchers.Default) { withTimeout(5_000) { invalidated.await() } }
        val updatedSource = dao.getPagedThreads()
        assertEquals(listOf("thread-4", "thread-0", "thread-1", "thread-2", "thread-3"),
            updatedSource.readAll().map { it.id })
        updatedSource.awaitInvalidation { dao.deleteThread("thread-2") }
        val remainingSource = dao.getPagedThreads()
        assertEquals(4, remainingSource.readAll().size)
        remainingSource.awaitInvalidation { dao.deleteAllThreads() }
        assertTrue(dao.getPagedThreads().readAll().isEmpty())
    }

    @Test
    fun `folder pages preserve insertion order across updates and deletion`() = runTest {
        val dao = database.noteDao()
        val ids = listOf("z", "a", "c", "b", "d")
        dao.upsertNoteFolders(ids.map { NoteFolderEntity(id = it, name = "Folder") })
        val source = dao.getPagedNoteFolders()
        assertEquals(ids, source.readAll().map { it.id })
        source.awaitInvalidation {
            dao.upsertNoteFolders(listOf(NoteFolderEntity(id = "a", name = "Renamed")))
        }
        val updatedSource = dao.getPagedNoteFolders()
        val updated = updatedSource.readAll()
        assertEquals(ids, updated.map { it.id })
        assertEquals("Renamed", updated[1].name)
        updatedSource.awaitInvalidation { dao.deleteNoteFolderById("c") }
        assertEquals(listOf("z", "a", "b", "d"), dao.getPagedNoteFolders().readAll().map { it.id })
    }

    @Test
    fun `diary pages sort consistently and search content beyond previews`() = runTest {
        val dao = database.diaryDao()
        dao.upsertEntries(listOf(
            DiaryEntryEntity(id = "b", title = "Some", createdDate = 101, updatedDate = 300, mood = Mood.GOOD),
            DiaryEntryEntity(id = "a", title = "same", createdDate = 100, updatedDate = 400, mood = Mood.BAD),
            DiaryEntryEntity(id = "c", title = "Alpha", createdDate = 200, updatedDate = 200,
                content = "x".repeat(200) + "needle", mood = Mood.OKAY),
            DiaryEntryEntity(id = "d", title = "Needle", createdDate = 300, updatedDate = 100, mood = Mood.AWESOME)
        ))
        assertEquals(listOf("a", "b", "c", "d"),
            dao.getPagedEntries(DiaryOrder.CREATED_DATE, QueryOrder.ASC).readAll().map { it.id })
        val descending = dao.getPagedEntries(DiaryOrder.CREATED_DATE, QueryOrder.DESC).readAll()
        assertEquals(listOf("d", "c", "b", "a"), descending.map { it.id })
        assertEquals(150, descending[1].content.length)
        assertEquals(listOf("c", "d", "a", "b"),
            dao.getPagedEntries(DiaryOrder.TITLE, QueryOrder.ASC).readAll().map { it.id })
        assertEquals(listOf("b", "a", "d", "c"),
            dao.getPagedEntries(DiaryOrder.TITLE, QueryOrder.DESC).readAll().map { it.id })
        assertEquals(listOf("a", "b", "c", "d"),
            dao.getPagedEntries(DiaryOrder.UPDATED_DATE, QueryOrder.DESC).readAll().map { it.id })
        val search = dao.searchPagedEntries("needle").readAll()
        assertEquals(listOf("d", "c"), search.map { it.id })
        assertEquals(100, search[1].content.length)
        assertTrue(dao.searchPagedEntries("missing").readAll().isEmpty())
        assertEquals(4, dao.searchPagedEntries("").readAll().size)
    }

    @Test
    fun `chart query includes range boundaries and excludes old and future entries`() = runTest {
        val dao = database.diaryDao()
        dao.upsertEntries(listOf(99L, 100L, 150L, 200L, 201L).map {
            DiaryEntryEntity(id = "$it", title = "Title", content = "x".repeat(500),
                createdDate = it, mood = if (it == 150L) Mood.BAD else Mood.GOOD)
        })
        val points = dao.getChartPoints(100, 200).first()
        assertEquals(listOf(100L, 150L, 200L), points.map { it.createdDate })
        assertEquals(listOf(Mood.GOOD, Mood.BAD, Mood.GOOD), points.map { it.mood })
        assertEquals(listOf(100L), dao.getChartPoints(100, 100).first().map { it.createdDate })
        assertTrue(dao.getChartPoints(300, 400).first().isEmpty())
        assertTrue(dao.getChartPoints(200, 100).first().isEmpty())
    }

    private suspend fun <T : Any> PagingSource<Int, T>.awaitInvalidation(write: suspend () -> Unit) {
        val invalidated = CompletableDeferred<Unit>()
        registerInvalidatedCallback { invalidated.complete(Unit) }
        write()
        withContext(Dispatchers.Default) { withTimeout(5_000) { invalidated.await() } }
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
