package com.mhss.app.storage

import kotlinx.serialization.DeserializationStrategy

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

    suspend fun <T> decodeJsonDataFromFile(
        fileUri: String,
        deserializer: DeserializationStrategy<T>
    ): DecodeDataFromFileResult<T>
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

sealed interface DecodeDataFromFileResult<out T> {
    data class Success<T>(val value: T) : DecodeDataFromFileResult<T>
    data object CouldNotReadFile : DecodeDataFromFileResult<Nothing>
}
