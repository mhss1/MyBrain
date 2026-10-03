package com.mhss.app.storage

import kotlinx.serialization.json.JsonElement

interface StorageManager {

    suspend fun writeBufferedFile(
        directoryUri: String,
        fileName: String,
        mimeType: String,
        block: suspend BufferedFileWriter.() -> Unit
    )

    suspend fun directoryExists(directoryUri: String): Boolean

    suspend fun getDisplayName(directoryUri: String): String

    suspend fun createUniqueDirectory(
        parentDirectoryUri: String,
        baseName: String
    ): String?

    suspend fun listFileNames(directoryUri: String): Set<String>

    suspend fun writeTextFile(
        directoryUri: String,
        preferredName: String,
        extension: String,
        mimeType: String,
        content: String,
        existingFileNames: MutableSet<String>
    ): WriteTextFileResult

    suspend fun readJsonArraysFromFile(
        fileUri: String,
        arrayNames: Set<String>,
        onItem: suspend (arrayName: String, item: JsonElement) -> Unit
    ): ReadJsonFileResult
}

interface BufferedFileWriter {
    suspend fun write(
        value: String,
        startIndex: Int = 0,
        endIndex: Int = value.length
    )
}

sealed interface WriteTextFileResult {
    data class Success(val fileName: String) : WriteTextFileResult
    data class CouldNotCreateFile(val fileName: String) : WriteTextFileResult
    data class CouldNotWriteFile(val fileName: String) : WriteTextFileResult
}

sealed interface ReadJsonFileResult {
    data object Success : ReadJsonFileResult
    data object CouldNotReadFile : ReadJsonFileResult
}
