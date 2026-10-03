package com.mhss.app.mybrain.appfunctions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.mhss.app.domain.model.Bookmark
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.model.Mood
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.use_case.AddBookmarkUseCase
import com.mhss.app.domain.use_case.AddDiaryEntryUseCase
import com.mhss.app.domain.use_case.GetNoteUseCase
import com.mhss.app.domain.use_case.GetTaskByIdUseCase
import com.mhss.app.domain.use_case.SearchBookmarksUseCase
import com.mhss.app.domain.use_case.SearchEntriesUseCase
import com.mhss.app.domain.use_case.SearchNotesUseCase
import com.mhss.app.domain.use_case.SearchTasksUseCase
import com.mhss.app.domain.use_case.UpdateTaskCompletedUseCase
import com.mhss.app.domain.use_case.UpsertNoteUseCase
import com.mhss.app.domain.use_case.UpsertTaskUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Exposes core application capabilities for notes, tasks, diary, and bookmarks to agents.
 */
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
    serviceName = "MyBrainAppFunctionService",
    appFunctionXmlFileName = "my_brain_app_function_service"
)
abstract class MyBrainAppFunctions : AppFunctionService() {
    private val upsertNoteUseCase: UpsertNoteUseCase by lazy { GlobalContext.get().get() }
    private val searchNotesUseCase: SearchNotesUseCase by lazy { GlobalContext.get().get() }
    private val getNoteUseCase: GetNoteUseCase by lazy { GlobalContext.get().get() }
    private val upsertTaskUseCase: UpsertTaskUseCase by lazy { GlobalContext.get().get() }
    private val searchTasksUseCase: SearchTasksUseCase by lazy { GlobalContext.get().get() }
    private val getTaskUseCase: GetTaskByIdUseCase by lazy { GlobalContext.get().get() }
    private val updateTaskCompletedUseCase: UpdateTaskCompletedUseCase by lazy { GlobalContext.get().get() }
    private val addDiaryEntryUseCase: AddDiaryEntryUseCase by lazy { GlobalContext.get().get() }
    private val searchEntriesUseCase: SearchEntriesUseCase by lazy { GlobalContext.get().get() }
    private val addBookmarkUseCase: AddBookmarkUseCase by lazy { GlobalContext.get().get() }
    private val searchBookmarksUseCase: SearchBookmarksUseCase by lazy { GlobalContext.get().get() }

    /**
     * Create a new note with a title and body content.
     *
     * @param title The title of the note.
     * @param content The body content of the note.
     * @return The created [AppNote] object including its generated ID.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun createNote(
        title: String,
        content: String
    ): AppNote = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val note = Note(
            title = title,
            content = content,
            createdDate = now,
            updatedDate = now,
        )
        val id = upsertNoteUseCase(note)
        AppNote(
            id = id,
            title = title,
            content = content,
            pinned = false,
            createdAt = now.toReadableDate()
        )
    }

    /**
     * Search notes by title or content matching a query string.
     * The content in the returned [AppNote]s may be truncated for maximum length.
     * If the full note content is required, use [getNoteById].
     *
     * @param query The search query string for matching note titles or content. If empty, all notes are returned.
     * @return A list of matching [AppNote]s.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun searchNotes(
        query: String
    ): List<AppNote> = withContext(Dispatchers.IO) {
        searchNotesUseCase(query).map { note ->
            AppNote(
                id = note.id,
                title = note.title,
                content = note.content,
                pinned = note.pinned,
                createdAt = note.createdDate.toReadableDate()
            )
        }
    }

    /**
     * Retrieve a full note by its unique identifier.
     * Required workflow: Call [searchNotes] first to obtain valid note IDs.
     *
     * @param noteId The unique identifier of the note.
     * @return The [AppNote] matching the ID, or null if not found.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getNoteById(
        noteId: String
    ): AppNote? = withContext(Dispatchers.IO) {
        getNoteUseCase(noteId)?.let { note ->
            AppNote(
                id = note.id,
                title = note.title,
                content = note.content,
                pinned = note.pinned,
                createdAt = note.createdDate.toReadableDate()
            )
        }
    }

    /**
     * Create a new task.
     *
     * @param title The title of the task.
     * @param description The optional detailed description of the task.
     * @param priority The priority of the task. Allowed values: "LOW", "MEDIUM", "HIGH". Defaults to "LOW".
     * @param dueDate The optional local due date and time in "HH:mm dd-MM-yyyy" format.
     * @return The created [AppTask] object including its generated ID.
     */
    @OptIn(ExperimentalUuidApi::class)
    @AppFunction(isDescribedByKDoc = true)
    suspend fun createTask(
        title: String,
        description: String? = null,
        priority: String? = null,
        dueDate: String? = null
    ): AppTask = withContext(Dispatchers.IO) {
        val id = Uuid.generateV7().toString()
        val actualDescription = description ?: ""
        val actualDueDate = dueDate?.let {
            it.toDateMillis()
                ?: throw IllegalArgumentException("Invalid due date. Expected format: HH:mm dd-MM-yyyy")
        }
        val actualPriority = when (priority?.uppercase()) {
            Priority.HIGH.name -> Priority.HIGH
            Priority.MEDIUM.name -> Priority.MEDIUM
            else -> Priority.LOW
        }
        val now = System.currentTimeMillis()
        val task = Task(
            title = title,
            description = actualDescription,
            priority = actualPriority,
            dueDate = actualDueDate,
            createdDate = now,
            updatedDate = now,
            id = id
        )
        upsertTaskUseCase(task)
        AppTask(
            id = id,
            title = title,
            description = actualDescription,
            isCompleted = false,
            priority = actualPriority.name,
            dueAt = actualDueDate?.toReadableDate()
        )
    }

    /**
     * Search tasks by title matching a query string.
     *
     * @param query The search query string for matching task titles. If empty, all tasks are returned.
     * @return A list of matching [AppTask]s.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun searchTasks(
        query: String
    ): List<AppTask> = withContext(Dispatchers.IO) {
        searchTasksUseCase(query).first().map { task ->
            AppTask(
                id = task.id,
                title = task.title,
                description = task.description,
                isCompleted = task.isCompleted,
                priority = task.priority.name,
                dueAt = task.dueDate?.toReadableDate()
            )
        }
    }

    /**
     * Mark a task as completed or incomplete.
     * Required workflow: Call [searchTasks] first to obtain valid task IDs.
     *
     * @param taskId The unique identifier of the task.
     * @param completed True to mark the task as completed, false to mark it incomplete.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun updateTaskCompleted(
        taskId: String,
        completed: Boolean
    ): Unit = withContext(Dispatchers.IO) {
        val task = getTaskUseCase(taskId)
            ?: throw IllegalArgumentException("No task found with ID: '$taskId'")
        updateTaskCompletedUseCase(task, completed)
    }

    /**
     * Create a new diary entry documenting the user's thoughts and mood.
     *
     * @param title The title of the diary entry.
     * @param content The text content of the diary entry.
     * @param mood The user's mood for the entry. Allowed values: "AWESOME", "GOOD", "OKAY", "BAD", "TERRIBLE". Defaults to "OKAY".
     * @return The created [AppDiaryEntry] object including its generated ID.
     */
    @OptIn(ExperimentalUuidApi::class)
    @AppFunction(isDescribedByKDoc = true)
    suspend fun createDiaryEntry(
        title: String,
        content: String,
        mood: String? = null
    ): AppDiaryEntry = withContext(Dispatchers.IO) {
        val id = Uuid.generateV7().toString()
        val actualMood = when (mood?.uppercase()) {
            Mood.AWESOME.name -> Mood.AWESOME
            Mood.GOOD.name -> Mood.GOOD
            Mood.BAD.name -> Mood.BAD
            Mood.TERRIBLE.name -> Mood.TERRIBLE
            else -> Mood.OKAY
        }
        val now = System.currentTimeMillis()
        val entry = DiaryEntry(
            title = title,
            content = content,
            mood = actualMood,
            createdDate = now,
            updatedDate = now,
            id = id
        )
        addDiaryEntryUseCase(entry)
        AppDiaryEntry(
            id = id,
            title = title,
            content = content,
            mood = actualMood.name,
            createdAt = now.toReadableDate()
        )
    }

    /**
     * Search diary entries by title matching a query string.
     *
     * @param query The search query string for matching diary entry titles. If empty, all entries are returned.
     * @return A list of matching [AppDiaryEntry]s.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun searchDiaryEntries(
        query: String
    ): List<AppDiaryEntry> = withContext(Dispatchers.IO) {
        searchEntriesUseCase(query).map { entry ->
            AppDiaryEntry(
                id = entry.id,
                title = entry.title,
                content = entry.content,
                mood = entry.mood.name,
                createdAt = entry.createdDate.toReadableDate()
            )
        }
    }

    /**
     * Create a new bookmark for a URL link.
     *
     * @param url The URL link to save.
     * @param title The title of the bookmark.
     * @param description The description of the bookmarked link.
     * @return The created [AppBookmark] object including its generated ID.
     */
    @OptIn(ExperimentalUuidApi::class)
    @AppFunction(isDescribedByKDoc = true)
    suspend fun createBookmark(
        url: String,
        title: String? = null,
        description: String? = null
    ): AppBookmark = withContext(Dispatchers.IO) {
        val id = Uuid.generateV7().toString()
        val actualTitle = title ?: ""
        val actualDescription = description ?: ""
        val now = System.currentTimeMillis()
        val bookmark = Bookmark(
            url = url,
            title = actualTitle,
            description = actualDescription,
            createdDate = now,
            updatedDate = now,
            id = id
        )
        addBookmarkUseCase(bookmark)
        AppBookmark(
            id = id,
            url = url,
            title = actualTitle,
            description = actualDescription,
            createdAt = now.toReadableDate()
        )
    }

    /**
     * Search bookmarks by title, description or URL matching a query string.
     *
     * @param query The search query string for matching bookmarks. If empty, all bookmarks are returned.
     * @return A list of matching [AppBookmark]s.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun searchBookmarks(
        query: String
    ): List<AppBookmark> = withContext(Dispatchers.IO) {
        searchBookmarksUseCase(query).map { bookmark ->
            AppBookmark(
                id = bookmark.id,
                url = bookmark.url,
                title = bookmark.title,
                description = bookmark.description,
                createdAt = bookmark.createdDate.toReadableDate()
            )
        }
    }
}

private const val APP_FUNCTION_DATE_INPUT_PATTERN = "HH:mm dd-MM-yyyy"
private const val APP_FUNCTION_DATE_OUTPUT_PATTERN = "HH:mm EEEE dd-MM-yyyy"

private fun Long.toReadableDate(): String =
    SimpleDateFormat(APP_FUNCTION_DATE_OUTPUT_PATTERN, Locale.US).format(Date(this))

private fun String.toDateMillis(): Long? = runCatching {
    SimpleDateFormat(APP_FUNCTION_DATE_INPUT_PATTERN, Locale.US)
        .apply { isLenient = false }
        .parse(this)
        ?.time
}.getOrNull()
