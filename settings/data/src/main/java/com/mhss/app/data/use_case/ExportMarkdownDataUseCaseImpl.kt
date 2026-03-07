package com.mhss.app.data.use_case
 
import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.mhss.app.database.MyBrainDatabase
import com.mhss.app.database.entity.BookmarkEntity
import com.mhss.app.database.entity.DiaryEntryEntity
import com.mhss.app.database.entity.NoteEntity
import com.mhss.app.database.entity.NoteFolderEntity
import com.mhss.app.database.entity.TaskEntity
import com.mhss.app.domain.model.Mood
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.SubTask
import com.mhss.app.domain.model.TaskFrequency
import com.mhss.app.domain.use_case.`interface`.ExportMarkdownDataUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
 
@Factory
class ExportMarkdownDataUseCaseImpl(
    private val context: Context,
    private val database: MyBrainDatabase,
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
    ): Boolean {
        return withContext(ioDispatcher) {
            try {
                val pickedDir = DocumentFile.fromTreeUri(context, directoryUri.toUri())
                    ?: return@withContext false
                val exportRoot = pickedDir.createUniqueDirectory("MyBrain_Backup_${System.currentTimeMillis()}")
                    ?: return@withContext false
 
                val notes = if (exportNotes) database.noteDao().getAllFullNotes() else emptyList()
                val noteFolders = if (exportNotes) database.noteDao().getAllNoteFolders().first() else emptyList()
                val tasks = if (exportTasks) database.taskDao().getAllFullTasks() else emptyList()
                val diaryEntries = if (exportDiary) database.diaryDao().getAllFullEntries() else emptyList()
                val bookmarks = if (exportBookmarks) database.bookmarkDao().getAllFullBookmarks() else emptyList()
 
                if (exportNotes) exportNotesMarkdown(rootDir = exportRoot, notes = notes, folders = noteFolders)
                yield()
                if (exportTasks) exportTasksMarkdown(rootDir = exportRoot, tasks = tasks)
                yield()
                if (exportDiary) exportDiaryMarkdown(rootDir = exportRoot, diaryEntries = diaryEntries)
                yield()
                if (exportBookmarks) exportBookmarksMarkdown(rootDir = exportRoot, bookmarks = bookmarks)
 
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
 
    private suspend fun exportNotesMarkdown(
        rootDir: DocumentFile,
        notes: List<NoteEntity>,
        folders: List<NoteFolderEntity>
    ) {
        val notesDir = rootDir.createUniqueDirectory("Notes")
            ?: error("Failed to create notes directory")
        val folderById = folders.associateBy { it.id }
        val notesDirFileNames = notesDir.listFiles()
            .mapNotNull { it.name.takeIf { _ -> it.isFile } }
            .toHashSet()
 
        notes.filter { it.folderId == null }.forEach { note ->
            notesDir.writeMarkdownFile(
                preferredName = note.title.ifBlank { "Untitled Note" },
                content = note.toMarkdown(),
                existingFileNames = notesDirFileNames
            )
            yield()
        }
 
        folders.forEach { folder ->
            val folderDir = notesDir.createUniqueDirectory(folder.name.ifBlank { "Untitled Folder" })
                ?: error("Failed to create folder directory: ${folder.name}")
            val folderFileNames = folderDir.listFiles()
                .mapNotNull { it.name.takeIf { _ -> it.isFile } }
                .toHashSet()
 
            notes.filter { it.folderId == folder.id }.forEach { note ->
                folderDir.writeMarkdownFile(
                    preferredName = note.title.ifBlank { "Untitled Note" },
                    content = note.toMarkdown(folderName = folderById[note.folderId]?.name),
                    existingFileNames = folderFileNames
                )
                yield()
            }
        }
    }
 
    private suspend fun exportTasksMarkdown(
        rootDir: DocumentFile,
        tasks: List<TaskEntity>
    ) {
        val tasksDir = rootDir.createUniqueDirectory("Tasks")
            ?: error("Failed to create tasks directory")
        val tasksDirFileNames = tasksDir.listFiles()
            .mapNotNull { it.name.takeIf { _ -> it.isFile } }
            .toHashSet()
        tasks.forEach { task ->
            tasksDir.writeMarkdownFile(
                preferredName = task.title.ifBlank { "Untitled Task" },
                content = task.toMarkdown(),
                existingFileNames = tasksDirFileNames
            )
            yield()
        }
    }
 
    private suspend fun exportDiaryMarkdown(
        rootDir: DocumentFile,
        diaryEntries: List<DiaryEntryEntity>
    ) {
        val diaryDir = rootDir.createUniqueDirectory("Diary")
            ?: error("Failed to create diary directory")
        val diaryDirFileNames = diaryDir.listFiles()
            .mapNotNull { it.name.takeIf { _ -> it.isFile } }
            .toHashSet()
        diaryEntries.forEach { entry ->
            diaryDir.writeMarkdownFile(
                preferredName = entry.title.ifBlank { "Diary Entry ${entry.createdDate.safeTimestampForName()}" },
                content = entry.toMarkdown(),
                existingFileNames = diaryDirFileNames
            )
            yield()
        }
    }
 
    private suspend fun exportBookmarksMarkdown(
        rootDir: DocumentFile,
        bookmarks: List<BookmarkEntity>
    ) {
        val bookmarksDir = rootDir.createUniqueDirectory("Bookmarks")
            ?: error("Failed to create bookmarks directory")
        val bookmarksDirFileNames = bookmarksDir.listFiles()
            .mapNotNull { it.name.takeIf { _ -> it.isFile } }
            .toHashSet()
        bookmarks.forEach { bookmark ->
            bookmarksDir.writeMarkdownFile(
                preferredName = bookmark.title.ifBlank { bookmark.url },
                content = bookmark.toMarkdown(),
                existingFileNames = bookmarksDirFileNames
            )
            yield()
        }
    }
 
    private fun NoteEntity.toMarkdown(folderName: String? = null): String = buildString {
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
 
    private fun DiaryEntryEntity.toMarkdown(): String = buildString {
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
 
    private fun TaskEntity.toMarkdown(): String = buildString {
        appendLine("${if (isCompleted) "- [x]" else "- [ ]"} **${title.ifBlank { "Untitled Task" }}**")
        appendLine()
        appendLine("- **Priority**: ${priority.displayName()}")
        dueDate.takeIf { it > 0L }?.let {
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
 
    private fun BookmarkEntity.toMarkdown(): String = buildString {
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
 
    private fun Mood.displayName(): String = name.lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
 
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
 
    private fun SubTask.toCheckboxText(): String =
        "${if (isCompleted) "[x]" else "[ ]"} ${title.ifBlank { "Untitled subtask" }}"
 
    private fun Int.pluralSuffix(): String = if (this == 1) "" else "s"
 
    private fun Long.toReadableDateTime(): String {
        if (this <= 0L) return "Unknown"
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault()).format(
            Instant.ofEpochMilli(this)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        )
    }
 
    private fun Long.safeTimestampForName(): String {
        if (this <= 0L) return "Unknown"
        return DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(
            Instant.ofEpochMilli(this)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        )
    }
 
    private fun DocumentFile.writeMarkdownFile(
        preferredName: String,
        content: String,
        existingFileNames: MutableSet<String>
    ) {
        val safeName = preferredName.sanitizeForFileName().ifBlank { "Untitled" }
        val file = createUniqueMarkdownFile(
            baseName = safeName,
            existingFileNames = existingFileNames
        )
            ?: error("Failed to create markdown file: $safeName")
        context.contentResolver.openOutputStream(file.uri)?.bufferedWriter()?.use { writer ->
            writer.write(content)
        } ?: error("Failed to open output stream for: $safeName")
    }
 
    private fun DocumentFile.createUniqueMarkdownFile(
        baseName: String,
        existingFileNames: MutableSet<String>
    ): DocumentFile? {
        var candidate = "$baseName.md"
        var index = 2
        while (candidate in existingFileNames) {
            candidate = "$baseName ($index).md"
            index++
        }
        val file = createFile("text/markdown", candidate)
        if (file != null) {
            existingFileNames.add(candidate)
        }
        return file
    }
 
    private fun DocumentFile.createUniqueDirectory(baseName: String): DocumentFile? {
        val safeName = baseName.sanitizeForFileName().ifBlank { "Export" }
        val existingNames = listFiles()
            .filter { it.isDirectory }
            .mapNotNull { it.name }
            .toHashSet()
 
        var candidate = safeName
        var index = 2
        while (candidate in existingNames) {
            candidate = "$safeName ($index)"
            index++
        }
        return createDirectory(candidate)
    }
 
    private fun String.sanitizeForFileName(): String = trim()
        .replace(FILE_NAME_INVALID_CHARS_REGEX, "_")
        .replace(WHITESPACE_REGEX, " ")
        .trim('.', ' ')
 
    companion object {
        private val FILE_NAME_INVALID_CHARS_REGEX = Regex("""[\\/:*?"<>|]""")
        private val WHITESPACE_REGEX = Regex("\\s+")
    }
}