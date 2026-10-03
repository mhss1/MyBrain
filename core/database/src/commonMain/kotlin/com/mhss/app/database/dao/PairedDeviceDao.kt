package com.mhss.app.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.mhss.app.database.entity.PairedDeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PairedDeviceDao {
    @Query("SELECT * FROM paired_devices")
    fun getAllDevicesFlow(): Flow<List<PairedDeviceEntity>>

    @Query("SELECT * FROM paired_devices")
    suspend fun getAllDevices(): List<PairedDeviceEntity>

    @Query("SELECT * FROM paired_devices WHERE id = :id")
    suspend fun getDevice(id: String): PairedDeviceEntity?

    @Upsert
    suspend fun upsertDevice(device: PairedDeviceEntity)

    @Query("UPDATE paired_devices SET is_connected = :isConnected WHERE id = :id")
    suspend fun updateConnectionStatus(id: String, isConnected: Boolean)

    @Query("UPDATE paired_devices SET last_synced_at = :lastSyncedSeq WHERE id = :id")
    suspend fun updateLastSyncedSeq(id: String, lastSyncedSeq: Long)

    @Query("UPDATE paired_devices SET last_acknowledged_local_seq = :seq WHERE id = :id AND last_acknowledged_local_seq < :seq")
    suspend fun updateLastAcknowledgedLocalSeq(id: String, seq: Long)

    @Query("UPDATE paired_devices SET ip_address = :ipAddress, candidate_ip_addresses = :candidateIpAddresses WHERE id = :id")
    suspend fun updateIpAddresses(id: String, ipAddress: String, candidateIpAddresses: List<String>)

    @Query("UPDATE paired_devices SET custom_ip_address = :customIpAddress WHERE id = :id")
    suspend fun updateCustomIpAddress(id: String, customIpAddress: String?)

    @Query("DELETE FROM paired_devices WHERE id = :id")
    suspend fun deleteDevice(id: String)
}
