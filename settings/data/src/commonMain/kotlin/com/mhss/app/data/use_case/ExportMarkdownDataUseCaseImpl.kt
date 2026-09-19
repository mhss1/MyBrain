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

                if (exportNotes) exportNotesMarkdown(rootDir = exportRoot)
                yield()
                if (exportTasks) exportTasksMarkdown(rootDir = exportRoot)
                yield()
                if (exportDiary) exportDiaryMarkdown(rootDir = exportRoot)
                yield()
                if (exportBookmarks) exportBookmarksMarkdown(rootDir = exportRoot)
            } catch (e: BackupDataException) {
                throw e
            } catch (_: Exception) {
                throw BackupDataException.GenericError()
            }
        }
    }
 
    private suspend fun exportNotesMarkdown(
        rootDir: String
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
        val notesDirFileNames = storageManager.listFileNames(notesDir).toMutableSet()
        val folderTargets = mutableMapOf<String, NoteFolderTarget>()

        forEachPage(
            getId = BackupNoteFolder::id,
            loadPage = { afterId, limit ->
                noteRepository.getNoteFoldersPage(afterId, limit).map { it.toBackupNoteFolder() }
            }
        ) { folder ->
            val folderName = folder.name.ifBlank { "Untitled Folder" }
            val folderDir = storageManager.createUniqueDirectory(
                parentDirectoryUri = notesDir,
                baseName = folderName
            )
                ?: throw BackupDataException.CouldNotCreateDirectory(
                    directoryName = folderName,
                    parent = storageManager.getDisplayName(notesDir)
                )
            folderTargets[folder.id] = NoteFolderTarget(
                directoryUri = folderDir,
                folderName = folder.name,
                existingFileNames = storageManager.listFileNames(folderDir).toMutableSet()
            )
        }

        forEachPage(
            getId = BackupNote::id,
            loadPage = { afterId, limit ->
                noteRepository.getFullNotesPage(afterId, limit).map { it.toBackupNote() }
            }
        ) { note ->
            if (note.folderId == null) {
                writeMarkdownFile(
                    directoryUri = notesDir,
                    preferredName = note.title.ifBlank { "Untitled Note" },
                    content = note.toMarkdown(),
                    existingFileNames = notesDirFileNames
                )
            } else {
                folderTargets[note.folderId]?.let { target ->
                    writeMarkdownFile(
                        directoryUri = target.directoryUri,
                        preferredName = note.title.ifBlank { "Untitled Note" },
                        content = note.toMarkdown(folderName = target.folderName),
                        existingFileNames = target.existingFileNames
                    )
                }
            }
            yield()
        }
    }
 
    private suspend fun exportTasksMarkdown(
        rootDir: String
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
        forEachPage(
            getId = BackupTask::id,
            loadPage = { afterId, limit ->
                taskRepository.getFullTasksPage(afterId, limit).map { it.toBackupTask() }
            }
        ) { task ->
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
        rootDir: String
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
        forEachPage(
            getId = BackupDiaryEntry::id,
            loadPage = { afterId, limit ->
                diaryRepository.getFullEntriesPage(afterId, limit).map { it.toBackupDiaryEntry() }
            }
        ) { entry ->
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
        rootDir: String
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
        forEachPage(
            getId = BackupBookmark::id,
            loadPage = { afterId, limit ->
                bookmarkRepository.getFullBookmarksPage(afterId, limit).map { it.toBackupBookmark() }
            }
        ) { bookmark ->
            writeMarkdownFile(
                directoryUri = bookmarksDir,
                preferredName = bookmark.title.ifBlank { bookmark.url },
                content = bookmark.toMarkdown(),
                existingFileNames = bookmarksDirFileNames
            )
            yield()
        }
    }

    private suspend fun <T> forEachPage(
        getId: (T) -> String,
        loadPage: suspend (afterId: String, limit: Int) -> List<T>,
        processItem: suspend (T) -> Unit
    ) {
        var afterId = ""
        var page = loadPage(afterId, PAGE_SIZE)
        while (page.isNotEmpty()) {
            page.forEach { processItem(it) }
            afterId = getId(page.last())
            page = loadPage(afterId, PAGE_SIZE)
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

    private data class NoteFolderTarget(
        val directoryUri: String,
        val folderName: String,
        val existingFileNames: MutableSet<String>
    )

    private companion object {
        const val PAGE_SIZE = 100
    }
}
