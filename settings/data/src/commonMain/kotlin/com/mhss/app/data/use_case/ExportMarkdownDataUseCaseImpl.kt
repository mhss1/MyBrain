package com.mhss.app.data.use_case
 
import com.mhss.app.domain.exception.BackupDataException
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.TaskFrequency
import com.mhss.app.domain.model.backup.BackupBookmark
import com.mhss.app.domain.model.backup.BackupDiaryEntry
import com.mhss.app.domain.model.backup.BackupNote
import com.mhss.app.domain.model.backup.BackupNoteFolder
import com.mhss.app.domain.model.backup.BackupSubTask
import com.mhss.app.domain.model.backup.BackupTask
import com.mhss.app.domain.model.backup.toBackupBookmark
import com.mhss.app.domain.model.backup.toBackupDiaryEntry
import com.mhss.app.domain.model.backup.toBackupNote
import com.mhss.app.domain.model.backup.toBackupNoteFolder
import com.mhss.app.domain.model.backup.toBackupTask
import com.mhss.app.domain.repository.BookmarkRepository
import com.mhss.app.domain.repository.DiaryRepository
import com.mhss.app.domain.repository.NoteRepository
import com.mhss.app.domain.repository.TaskRepository
import com.mhss.app.domain.use_case.`interface`.ExportMarkdownDataUseCase
import com.mhss.app.storage.StorageManager
import com.mhss.app.storage.WriteTextFileResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import kotlin.time.Instant

@Factory
class ExportMarkdownDataUseCaseImpl(
    private val storageManager: StorageManager,
    private val noteRepository: NoteRepository,
    private val taskRepository: TaskRepository,
    private val diaryRepository: DiaryRepository,
    private val bookmarkRepository: BookmarkRepository,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : ExportMarkdownDataUseCase {
 
    override suspend fun invoke(
        directoryUri: String,
        exportNotes: Boolean,
        exportTasks: Boolean,
        exportDiary: Boolean,
        exportBookmarks: Boolean,
        encrypted: Boolean,
        password: String?
    ) {
        withContext(ioDispatcher) {
            try {
                if (!storageManager.directoryExists(directoryUri)) {
                    throw BackupDataException.InvalidBackupLocation(directoryUri)
                }
                val exportRootName = "MyBrain_Backup_${System.currentTimeMillis()}"
                val exportRoot = storageManager.createUniqueDirectory(
                    parentDirectoryUri = directoryUri,
                    baseName = exportRootName
                )
                    ?: throw BackupDataException.CouldNotCreateDirectory(
                        directoryName = exportRootName,
                        parent = storageManager.getDisplayName(directoryUri)
                    )

                val notes = if (exportNotes) noteRepository.getAllFullNotes() else emptyList()
                val noteFolders = if (exportNotes) noteRepository.getAllNoteFolders().first() else emptyList()
                val tasks = if (exportTasks) taskRepository.getAllTasks().first() else emptyList()
                val diaryEntries = if (exportDiary) diaryRepository.getAllFullEntries() else emptyList()
                val bookmarks = if (exportBookmarks) bookmarkRepository.getAllBookmarks().first() else emptyList()

                if (exportNotes) exportNotesMarkdown(
                    rootDir = exportRoot,
                    notes = notes.map { it.toBackupNote() },
                    folders = noteFolders.map { it.toBackupNoteFolder() }
                )
                yield()
                if (exportTasks) exportTasksMarkdown(
                    rootDir = exportRoot,
                    tasks = tasks.map { it.toBackupTask() }
                )
                yield()
                if (exportDiary) exportDiaryMarkdown(
                    rootDir = exportRoot,
                    diaryEntries = diaryEntries.map { it.toBackupDiaryEntry() }
                )
                yield()
                if (exportBookmarks) exportBookmarksMarkdown(
                    rootDir = exportRoot,
                    bookmarks = bookmarks.map { it.toBackupBookmark() }
                )
            } catch (e: BackupDataException) {
                throw e
            } catch (_: Exception) {
                throw BackupDataException.GenericError()
            }
        }
    }
 
    private suspend fun exportNotesMarkdown(
        rootDir: String,
        notes: List<BackupNote>,
        folders: List<BackupNoteFolder>
    ) {
        val notesDirName = "Notes"
        val notesDir = storageManager.createUniqueDirectory(
            parentDirectoryUri = rootDir,
            baseName = notesDirName
        )
            ?: throw BackupDataException.CouldNotCreateDirectory(
                directoryName = notesDirName,
                parent = storageManager.getDisplayName(rootDir)
            )
        val folderById = folders.associateBy { it.id }
        val notesDirFileNames = storageManager.listFileNames(notesDir).toMutableSet()
 
        notes.filter { it.folderId == null }.forEach { note ->
            writeMarkdownFile(
                directoryUri = notesDir,
                preferredName = note.title.ifBlank { "Untitled Note" },
                content = note.toMarkdown(),
                existingFileNames = notesDirFileNames
            )
            yield()
        }

        folders.forEach { folder ->
            val folderName = folder.name.ifBlank { "Untitled Folder" }
            val folderDir = storageManager.createUniqueDirectory(
                parentDirectoryUri = notesDir,
                baseName = folderName
            )
                ?: throw BackupDataException.CouldNotCreateDirectory(
                    directoryName = folderName,
                    parent = storageManager.getDisplayName(notesDir)
                )
            val folderFileNames = storageManager.listFileNames(folderDir).toMutableSet()
 
            notes.filter { it.folderId == folder.id }.forEach { note ->
                writeMarkdownFile(
                    directoryUri = folderDir,
                    preferredName = note.title.ifBlank { "Untitled Note" },
                    content = note.toMarkdown(folderName = folderById[note.folderId]?.name),
                    existingFileNames = folderFileNames
                )
                yield()
            }
        }
    }
 
    private suspend fun exportTasksMarkdown(
        rootDir: String,
        tasks: List<BackupTask>
    ) {
        val tasksDirName = "Tasks"
        val tasksDir = storageManager.createUniqueDirectory(
            parentDirectoryUri = rootDir,
            baseName = tasksDirName
        )
            ?: throw BackupDataException.CouldNotCreateDirectory(
                directoryName = tasksDirName,
                parent = storageManager.getDisplayName(rootDir)
            )
        val tasksDirFileNames = storageManager.listFileNames(tasksDir).toMutableSet()
        tasks.forEach { task ->
            writeMarkdownFile(
                directoryUri = tasksDir,
                preferredName = task.title.ifBlank { "Untitled Task" },
                content = task.toMarkdown(),
                existingFileNames = tasksDirFileNames
            )
            yield()
        }
    }
 
    private suspend fun exportDiaryMarkdown(
        rootDir: String,
        diaryEntries: List<BackupDiaryEntry>
    ) {
        val diaryDirName = "Diary"
        val diaryDir = storageManager.createUniqueDirectory(
            parentDirectoryUri = rootDir,
            baseName = diaryDirName
        )
            ?: throw BackupDataException.CouldNotCreateDirectory(
                directoryName = diaryDirName,
                parent = storageManager.getDisplayName(rootDir)
            )
        val diaryDirFileNames = storageManager.listFileNames(diaryDir).toMutableSet()
        diaryEntries.forEach { entry ->
            writeMarkdownFile(
                directoryUri = diaryDir,
                preferredName = entry.title.ifBlank { "Diary Entry ${entry.createdDate.safeTimestampForName()}" },
                content = entry.toMarkdown(),
                existingFileNames = diaryDirFileNames
            )
            yield()
        }
    }
 
    private suspend fun exportBookmarksMarkdown(
        rootDir: String,
        bookmarks: List<BackupBookmark>
    ) {
        val bookmarksDirName = "Bookmarks"
        val bookmarksDir = storageManager.createUniqueDirectory(
            parentDirectoryUri = rootDir,
            baseName = bookmarksDirName
        )
            ?: throw BackupDataException.CouldNotCreateDirectory(
                directoryName = bookmarksDirName,
                parent = storageManager.getDisplayName(rootDir)
            )
        val bookmarksDirFileNames = storageManager.listFileNames(bookmarksDir).toMutableSet()
        bookmarks.forEach { bookmark ->
            writeMarkdownFile(
                directoryUri = bookmarksDir,
                preferredName = bookmark.title.ifBlank { bookmark.url },
                content = bookmark.toMarkdown(),
                existingFileNames = bookmarksDirFileNames
            )
            yield()
        }
    }
 
    private fun BackupNote.toMarkdown(folderName: String? = null): String = buildString {
        appendLine("# ${title.ifBlank { "Untitled Note" }}")
        appendLine()
        appendLine("- **Pinned**: ${if (pinned) "Yes" else "No"}")
        folderName?.takeIf { it.isNotBlank() }?.let {
            appendLine("- **Folder**: $it")
        }
        appendLine("- **Created**: ${createdDate.toReadableDateTime()}")
        appendLine("- **Updated**: ${updatedDate.toReadableDateTime()}")
        if (content.isNotBlank()) {
            appendLine()
            appendLine(content.trim())
        }
    }.trimEnd()
 
    private fun BackupDiaryEntry.toMarkdown(): String = buildString {
        appendLine("# ${title.ifBlank { "Untitled Diary Entry" }}")
        appendLine()
        appendLine("- **Mood**: ${mood.displayName()}")
        appendLine("- **Created**: ${createdDate.toReadableDateTime()}")
        appendLine("- **Updated**: ${updatedDate.toReadableDateTime()}")
        if (content.isNotBlank()) {
            appendLine()
            appendLine(content.trim())
        }
    }.trimEnd()
 
    private fun BackupTask.toMarkdown(): String = buildString {
        appendLine("${if (isCompleted) "- [x]" else "- [ ]"} **${title.ifBlank { "Untitled Task" }}**")
        appendLine()
        appendLine("- **Priority**: ${priority.displayName()}")
        dueDate?.let {
            appendLine("- **Due date**: ${it.toReadableDateTime()}")
        }
        appendLine("- **Recurring**: ${if (recurring) "Yes" else "No"}")
        if (recurring) {
            appendLine("- **Repeat**: ${frequency.toFrequencyText(frequencyAmount)}")
        }
        appendLine("- **Created**: ${createdDate.toReadableDateTime()}")
        appendLine("- **Updated**: ${updatedDate.toReadableDateTime()}")
        if (description.isNotBlank()) {
            appendLine()
            appendLine("## Description")
            appendLine()
            appendLine(description.trim())
        }
        if (subTasks.isNotEmpty()) {
            appendLine()
            appendLine("## Subtasks")
            appendLine()
            subTasks.forEach { subTask ->
                appendLine("- ${subTask.toCheckboxText()}")
            }
        }
    }.trimEnd()
 
    private fun BackupBookmark.toMarkdown(): String = buildString {
        appendLine("# ${title.ifBlank { "Untitled Bookmark" }}")
        appendLine()
        appendLine("- **URL**: <$url>")
        appendLine("- **Created**: ${createdDate.toReadableDateTime()}")
        appendLine("- **Updated**: ${updatedDate.toReadableDateTime()}")
        if (description.isNotBlank()) {
            appendLine()
            appendLine("## Description")
            appendLine()
            appendLine(description.trim())
        }
    }.trimEnd()
 
    private fun String.displayName(): String = lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
 
    private fun Int.displayName(): String = when (this) {
        Priority.HIGH.value -> "High"
        Priority.MEDIUM.value -> "Medium"
        else -> "Low"
    }
 
    private fun Int.toFrequencyText(amount: Int): String = when (this) {
        TaskFrequency.EVERY_MINUTES.value -> "Every $amount minute${amount.pluralSuffix()}"
        TaskFrequency.HOURLY.value -> "Every $amount hour${amount.pluralSuffix()}"
        TaskFrequency.WEEKLY.value -> "Every $amount week${amount.pluralSuffix()}"
        TaskFrequency.MONTHLY.value -> "Every $amount month${amount.pluralSuffix()}"
        TaskFrequency.ANNUAL.value -> "Every $amount year${amount.pluralSuffix()}"
        else -> "Every $amount day${amount.pluralSuffix()}"
    }
 
    private fun BackupSubTask.toCheckboxText(): String =
        "${if (isCompleted) "[x]" else "[ ]"} ${title.ifBlank { "Untitled subtask" }}"
 
    private fun Int.pluralSuffix(): String = if (this == 1) "" else "s"
 
    private fun Long.toReadableDateTime(): String {
        if (this <= 0L) return "Unknown"
        val dateTime = Instant.fromEpochMilliseconds(this)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        return buildString {
            append(dateTime.year)
            append("-")
            append(dateTime.month.number.pad2())
            append("-")
            append(dateTime.day.pad2())
            append(" ")
            append(dateTime.hour.pad2())
            append(":")
            append(dateTime.minute.pad2())
        }
    }
 
    private fun Long.safeTimestampForName(): String {
        if (this <= 0L) return "Unknown"
        val dateTime = Instant.fromEpochMilliseconds(this)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        return buildString {
            append(dateTime.year)
            append("-")
            append(dateTime.month.number.pad2())
            append("-")
            append(dateTime.day.pad2())
            append("_")
            append(dateTime.hour.pad2())
            append("-")
            append(dateTime.minute.pad2())
        }
    }

    private fun Int.pad2(): String = toString().padStart(2, '0')
 
    private suspend fun writeMarkdownFile(
        directoryUri: String,
        preferredName: String,
        content: String,
        existingFileNames: MutableSet<String>
    ) {
        val parent = storageManager.getDisplayName(directoryUri)
        when (
            val result = storageManager.writeTextFile(
                directoryUri = directoryUri,
                preferredName = preferredName,
                extension = "md",
                mimeType = "text/markdown",
                content = content,
                existingFileNames = existingFileNames
            )
        ) {
            is WriteTextFileResult.Success -> Unit
            is WriteTextFileResult.CouldNotCreateFile -> {
                throw BackupDataException.CouldNotCreateFile(
                    fileName = result.fileName,
                    parent = parent
                )
            }

            is WriteTextFileResult.CouldNotWriteFile -> {
                throw BackupDataException.CouldNotWriteFile(
                    fileName = result.fileName,
                    parent = parent
                )
            }
        }
    }
}
