package com.mhss.app.mybrain.sync.repository

import com.mhss.app.database.dao.PairedDeviceDao
import com.mhss.app.database.entity.PairedDeviceEntity
import com.mhss.app.database.helpers.DatabaseTransactionProvider
import com.mhss.app.mybrain.sync.model.PairedDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class PairedDevicesRepositoryImpl(
    private val pairedDeviceDao: PairedDeviceDao,
    private val transactionProvider: DatabaseTransactionProvider
) : PairedDevicesRepository {

    override fun getPairedDevicesFlow(): Flow<List<PairedDevice>> {
        return pairedDeviceDao.getAllDevicesFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPairedDevices(): List<PairedDevice> {
        return pairedDeviceDao.getAllDevices().map { it.toDomain() }
    }

    override suspend fun getPairedDevice(id: String): PairedDevice? {
        return pairedDeviceDao.getDevice(id)?.toDomain()
    }

    override suspend fun addOrUpdatePairedDevice(device: PairedDevice) {
        transactionProvider.runInTransaction {
            val existing = pairedDeviceDao.getDevice(device.deviceId)
            val finalDevice = if (existing != null) {
                val lastSyncedSeq = if (device.lastSyncedSeq == 0L) existing.lastSyncedSeq else device.lastSyncedSeq
                val candidates = device.candidateIpAddresses.ifEmpty { existing.candidateIpAddresses }
                val customIp = existing.customIpAddress
                device.copy(
                    lastSyncedSeq = lastSyncedSeq,
                    candidateIpAddresses = candidates,
                    customIpAddress = customIp,
                    lastAcknowledgedLocalSeq = existing.lastAcknowledgedLocalSeq
                )
            } else {
                device.copy(lastAcknowledgedLocalSeq = 0L)
            }
            pairedDeviceDao.upsertDevice(finalDevice.toEntity())
        }
    }

    override suspend fun updateConnectionStatus(id: String, isConnected: Boolean) {
        pairedDeviceDao.updateConnectionStatus(id, isConnected)
    }

    override suspend fun updateLastSyncedSeq(id: String, lastSyncedSeq: Long) {
        pairedDeviceDao.updateLastSyncedSeq(id, lastSyncedSeq)
    }

    override suspend fun updateLastAcknowledgedLocalSeq(id: String, seq: Long) {
        pairedDeviceDao.updateLastAcknowledgedLocalSeq(id, seq)
    }

    override suspend fun updateIpAddresses(id: String, ipAddress: String, candidateIps: List<String>) {
        pairedDeviceDao.updateIpAddresses(id, ipAddress, candidateIps)
    }

    override suspend fun updateCustomIpAddress(id: String, customIpAddress: String?) {
        pairedDeviceDao.updateCustomIpAddress(id, customIpAddress)
    }

    override suspend fun deletePairedDevice(id: String) {
        pairedDeviceDao.deleteDevice(id)
    }

    private fun PairedDeviceEntity.toDomain() = PairedDevice(
        deviceId = id,
        deviceName = name,
        ipAddress = ipAddress,
        port = port,
        lastSyncedSeq = lastSyncedSeq,
        encryptionKey = encryptionKey,
        deviceVersion = deviceVersion,
        isConnected = isConnected,
        candidateIpAddresses = candidateIpAddresses,
        customIpAddress = customIpAddress,
        lastAcknowledgedLocalSeq = lastAcknowledgedLocalSeq
    )

    private fun PairedDevice.toEntity() = PairedDeviceEntity(
        id = deviceId,
        name = deviceName,
        ipAddress = ipAddress,
        port = port,
        lastSyncedSeq = lastSyncedSeq,
        encryptionKey = encryptionKey,
        deviceVersion = deviceVersion,
        isConnected = isConnected,
        candidateIpAddresses = candidateIpAddresses,
        customIpAddress = customIpAddress,
        lastAcknowledgedLocalSeq = lastAcknowledgedLocalSeq
    )
}

interface PairedDevicesRepository {
    fun getPairedDevicesFlow(): Flow<List<PairedDevice>>
    suspend fun getPairedDevices(): List<PairedDevice>
    suspend fun getPairedDevice(id: String): PairedDevice?
    suspend fun addOrUpdatePairedDevice(device: PairedDevice)
    suspend fun updateConnectionStatus(id: String, isConnected: Boolean)
    suspend fun updateLastSyncedSeq(id: String, lastSyncedSeq: Long)
    suspend fun updateLastAcknowledgedLocalSeq(id: String, seq: Long)
    suspend fun updateIpAddresses(id: String, ipAddress: String, candidateIps: List<String>)
    suspend fun updateCustomIpAddress(id: String, customIpAddress: String?)
    suspend fun deletePairedDevice(id: String)
}
