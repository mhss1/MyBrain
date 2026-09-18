package com.mhss.app.storage

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory(binds = [StorageManager::class])
class StorageManagerImpl(
    private val context: Context,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
): StorageManager {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    override suspend fun writeBufferedFile(
        directoryUri: String,
        fileName: String,
        mimeType: String,
        block: suspend BufferedFileWriter.() -> Unit
    ) = withContext(ioDispatcher) {
        val dir = DocumentFile.fromTreeUri(context, directoryUri.toUri())
        val destinationFile = dir?.createFile(mimeType, fileName)
            ?: throw IllegalStateException("Failed to create file")

        try {
            val outputStream = context.contentResolver.openOutputStream(destinationFile.uri)
                ?: throw IllegalStateException("Failed to open output stream")
            outputStream.bufferedWriter().use { writer ->
                val bufferedFileWriter = object : BufferedFileWriter {
                    override suspend fun write(value: String, startIndex: Int, endIndex: Int) {
                        writer.write(value, startIndex, endIndex - startIndex)
                    }
                }
                bufferedFileWriter.block()
            }
        } catch (error: Throwable) {
            runCatching { destinationFile.delete() }
            throw error
        }
    }

    override suspend fun directoryExists(directoryUri: String): Boolean = withContext(ioDispatcher) {
        val dir = DocumentFile.fromTreeUri(context, directoryUri.toUri())
        dir?.exists() == true && dir.isDirectory
    }

    override suspend fun getDisplayName(directoryUri: String): String = withContext(ioDispatcher) {
        val dir = DocumentFile.fromTreeUri(context, directoryUri.toUri())
        dir?.name?.takeIf { it.isNotBlank() } ?: directoryUri
    }

    override suspend fun createUniqueDirectory(
        parentDirectoryUri: String,
        baseName: String
    ): String? = withContext(ioDispatcher) {
        val parent = DocumentFile.fromTreeUri(context, parentDirectoryUri.toUri()) ?: return@withContext null
        val safeName = baseName.sanitizeForFileName().ifBlank { "Untitled" }
        val existingNames = parent.listFiles()
            .filter { it.isDirectory }
            .mapNotNull { it.name }
            .toHashSet()

        var candidate = safeName
        var index = 2
        while (candidate in existingNames) {
            candidate = "$safeName ($index)"
            index++
        }
        parent.createDirectory(candidate)?.uri?.toString()
    }

    override suspend fun listFileNames(directoryUri: String): Set<String> = withContext(ioDispatcher) {
        val dir = DocumentFile.fromTreeUri(context, directoryUri.toUri()) ?: return@withContext emptySet()
        dir.listFiles()
            .filter { it.isFile }
            .mapNotNull { it.name }
            .toSet()
    }

    override suspend fun writeTextFile(
        directoryUri: String,
        preferredName: String,
        extension: String,
        mimeType: String,
        content: String,
        existingFileNames: MutableSet<String>
    ): WriteTextFileResult = withContext(ioDispatcher) {
        val dir = DocumentFile.fromTreeUri(context, directoryUri.toUri())
            ?: return@withContext WriteTextFileResult.CouldNotCreateFile(
                fileName = "${preferredName.sanitizeForFileName().ifBlank { "Untitled" }}.${extension.trim().trimStart('.')}"
            )

        val safeName = preferredName.sanitizeForFileName().ifBlank { "Untitled" }
        val safeExtension = extension.trim().trimStart('.').ifBlank { "txt" }
        val file = dir.createUniqueFile(
            baseName = safeName,
            extension = safeExtension,
            mimeType = mimeType,
            existingFileNames = existingFileNames
        ) ?: return@withContext WriteTextFileResult.CouldNotCreateFile("$safeName.$safeExtension")

        val fileName = file.name ?: "$safeName.$safeExtension"
        runCatching {
            context.contentResolver.openOutputStream(file.uri)?.bufferedWriter()?.use { writer ->
                writer.write(content)
            } ?: throw IllegalStateException("Failed to open output stream")
        }.fold(
            onSuccess = {
                WriteTextFileResult.Success(fileName)
            },
            onFailure = {
                WriteTextFileResult.CouldNotWriteFile(fileName)
            }
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun <T> decodeJsonDataFromFile(
        fileUri: String,
        deserializer: DeserializationStrategy<T>
    ): DecodeDataFromFileResult<T> = withContext(ioDispatcher) {
        val inputStream = context.contentResolver.openInputStream(fileUri.toUri())
            ?: return@withContext DecodeDataFromFileResult.CouldNotReadFile

        runCatching {
            inputStream.use { stream ->
                json.decodeFromStream(
                    deserializer = deserializer,
                    stream = stream
                )
            }
        }.fold(
            onSuccess = { value ->
                DecodeDataFromFileResult.Success(value)
            },
            onFailure = {
                DecodeDataFromFileResult.CouldNotReadFile
            }
        )
    }

    private fun DocumentFile.createUniqueFile(
        baseName: String,
        extension: String,
        mimeType: String,
        existingFileNames: MutableSet<String>
    ): DocumentFile? {
        var candidate = "$baseName.$extension"
        var index = 2
        while (candidate in existingFileNames) {
            candidate = "$baseName ($index).$extension"
            index++
        }
        val file = createFile(mimeType, candidate)
        if (file != null) {
            existingFileNames.add(candidate)
        }
        return file
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
