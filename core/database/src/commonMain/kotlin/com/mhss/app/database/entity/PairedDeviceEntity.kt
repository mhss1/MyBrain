package com.mhss.app.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "paired_devices")
data class PairedDeviceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "ip_address")
    val ipAddress: String,
    val port: Int,
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedSeq: Long,
    @ColumnInfo(name = "encryption_key")
    val encryptionKey: String,
    @ColumnInfo(name = "device_version", defaultValue = "1")
    val deviceVersion: Int,
    @ColumnInfo(name = "is_connected", defaultValue = "0")
    val isConnected: Boolean = false,
    @ColumnInfo(name = "candidate_ip_addresses", defaultValue = "[]")
    val candidateIpAddresses: List<String> = emptyList(),
    @ColumnInfo(name = "custom_ip_address")
    val customIpAddress: String? = null,
    @ColumnInfo(name = "last_acknowledged_local_seq", defaultValue = "0")
    val lastAcknowledgedLocalSeq: Long = 0L
)
