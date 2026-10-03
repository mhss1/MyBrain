package com.mhss.app.storage

import android.content.Context
import android.util.JsonReader
import android.util.JsonToken
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonUnquotedLiteral
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory(binds = [StorageManager::class])
class StorageManagerImpl(
    private val context: Context,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
): StorageManager {

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

    override suspend fun readJsonArraysFromFile(
        fileUri: String,
        arrayNames: Set<String>,
        onItem: suspend (arrayName: String, item: JsonElement) -> Unit
    ): ReadJsonFileResult = withContext(ioDispatcher) {
        val inputStream = context.contentResolver.openInputStream(fileUri.toUri())
            ?: return@withContext ReadJsonFileResult.CouldNotReadFile

        try {
            val remainingArrayNames = arrayNames.toMutableSet()
            inputStream.bufferedReader().use { reader ->
                JsonReader(reader).use { jsonReader ->
                    jsonReader.beginObject()

                    while (jsonReader.hasNext() && remainingArrayNames.isNotEmpty()) {
                        val arrayName = jsonReader.nextName()
                        if (arrayName !in remainingArrayNames) {
                            jsonReader.skipValue()
                            continue
                        }

                        jsonReader.beginArray()
                        while (jsonReader.hasNext()) {
                            val item = jsonReader.readJsonElement()
                            try {
                                onItem(arrayName, item)
                            } catch (error: Throwable) {
                                throw JsonArrayItemException(error)
                            }
                        }
                        jsonReader.endArray()
                        remainingArrayNames.remove(arrayName)
                    }

                    if (remainingArrayNames.isNotEmpty()) {
                        jsonReader.endObject()
                        check(jsonReader.peek() == JsonToken.END_DOCUMENT)
                    }
                }
            }
            ReadJsonFileResult.Success
        } catch (error: JsonArrayItemException) {
            throw error.itemError
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            ReadJsonFileResult.CouldNotReadFile
        }
    }

    private fun JsonReader.readJsonElement(): JsonElement = when (peek()) {
        JsonToken.BEGIN_ARRAY -> {
            beginArray()
            val values = buildList {
                while (hasNext()) add(readJsonElement())
            }
            endArray()
            JsonArray(values)
        }

        JsonToken.BEGIN_OBJECT -> {
            beginObject()
            val values = buildMap {
                while (hasNext()) put(nextName(), readJsonElement())
            }
            endObject()
            JsonObject(values)
        }

        JsonToken.STRING -> JsonPrimitive(nextString())
        JsonToken.NUMBER -> JsonUnquotedLiteral(nextString())
        JsonToken.BOOLEAN -> JsonPrimitive(nextBoolean())
        JsonToken.NULL -> {
            nextNull()
            JsonNull
        }

        else -> error("Unexpected JSON token: ${peek()}")
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

    private class JsonArrayItemException(val itemError: Throwable) : Exception(itemError)
}
