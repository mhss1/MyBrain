@file:Suppress("UnsafeOptInUsageError")

package com.mhss.app.mybrain.sync.model

import com.mhss.app.database.entity.AssistantMessageEntity
import com.mhss.app.database.entity.AssistantThreadEntity
import com.mhss.app.database.entity.DeletedEntityEntity
import com.mhss.app.domain.model.Bookmark
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.domain.model.Task
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PairedDevice(
    @SerialName("deviceId") val deviceId: String,
    @SerialName("deviceName") val deviceName: String = "",
    @SerialName("ipAddress") val ipAddress: String,
    @SerialName("port") val port: Int,
    @SerialName("lastSyncedSeq") val lastSyncedSeq: Long = 0L,
    @SerialName("encryptionKey") val encryptionKey: String = "",
    @SerialName("deviceVersion") val deviceVersion: Int = 1,
    @SerialName("isConnected") val isConnected: Boolean = false,
    @SerialName("candidateIpAddresses") val candidateIpAddresses: List<String> = emptyList(),
    @SerialName("customIpAddress") val customIpAddress: String? = null,
    @SerialName("lastAcknowledgedLocalSeq") val lastAcknowledgedLocalSeq: Long = 0L
)

@Serializable
data class SyncPayload(
    @SerialName("notes") val notes: List<Note> = emptyList(),
    @SerialName("folders") val folders: List<NoteFolder> = emptyList(),
    @SerialName("tasks") val tasks: List<Task> = emptyList(),
    @SerialName("diaryEntries") val diaryEntries: List<DiaryEntry> = emptyList(),
    @SerialName("bookmarks") val bookmarks: List<Bookmark> = emptyList(),
    @SerialName("assistantThreads") val assistantThreads: List<AssistantThreadEntity> = emptyList(),
    @SerialName("assistantMessages") val assistantMessages: List<AssistantMessageEntity> = emptyList(),
    @SerialName("deletedEntities") val deletedEntities: List<DeletedEntityEntity> = emptyList(),
    @SerialName("maxSequence") val maxSequence: Long = 0L,
    @SerialName("hasMore") val hasMore: Boolean = false
)

@Serializable
data class PingPayload(
    @SerialName("sourceDeviceId") val sourceDeviceId: String,
    @SerialName("sourceDeviceName") val sourceDeviceName: String,
    @SerialName("sourceIps") val sourceIps: List<String>,
    @SerialName("sourcePort") val sourcePort: Int,
    @SerialName("targetDeviceId") val targetDeviceId: String,
    @SerialName("deviceVersion") val deviceVersion: Int = 1
)

@Serializable
data class PingResponse(
    @SerialName("currentDeviceId") val currentDeviceId: String,
    @SerialName("currentDeviceName") val currentDeviceName: String,
    @SerialName("deviceVersion") val deviceVersion: Int = 1,
    @SerialName("responseIps") val responseIps: List<String> = emptyList()
)


@Serializable
data class QrPayload(
    @SerialName("deviceId") val deviceId: String,
    @SerialName("ips") val ips: List<String>,
    @SerialName("port") val port: Int,
    @SerialName("inviteId") val inviteId: String,
    @SerialName("inviteSecret") val inviteSecret: String
)

@Serializable
data class PairRequest(
    @SerialName("sourceDeviceId") val sourceDeviceId: String,
    @SerialName("sourceDeviceName") val sourceDeviceName: String,
    @SerialName("sourceIps") val sourceIps: List<String>,
    @SerialName("sourcePort") val sourcePort: Int,
    @SerialName("targetDeviceId") val targetDeviceId: String,
    @SerialName("sharedKey") val sharedKey: String,
    @SerialName("deviceVersion") val deviceVersion: Int = 1
)

@Serializable
data class PairResponse(
    @SerialName("targetDeviceId") val targetDeviceId: String,
    @SerialName("targetDeviceName") val targetDeviceName: String,
    @SerialName("targetIps") val targetIps: List<String>,
    @SerialName("deviceVersion") val deviceVersion: Int = 1
)

@Serializable
sealed interface SyncSocketMessage

@Serializable
@SerialName("sync_trigger")
data object SyncTriggerMessage : SyncSocketMessage

@Serializable
@SerialName("request_changes")
data class RequestChangesMessage(
    @SerialName("lastSyncedSeq") val lastSyncedSeq: Long,
    @SerialName("sourceIps") val sourceIps: List<String> = emptyList()
) : SyncSocketMessage

@Serializable
@SerialName("changes")
data class ChangesMessage(
    @SerialName("lastSyncedSeq") val lastSyncedSeq: Long,
    @SerialName("payload") val payload: SyncPayload
) : SyncSocketMessage

@Serializable
@SerialName("error")
data class SyncErrorMessage(
    @SerialName("message") val message: String
) : SyncSocketMessage
