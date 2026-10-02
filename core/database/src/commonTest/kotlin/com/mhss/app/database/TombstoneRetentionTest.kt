package com.mhss.app.database

import com.mhss.app.database.entity.DeletedEntityEntity
import com.mhss.app.database.entity.PairedDeviceEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TombstoneRetentionTest : PlatformTest() {
    private lateinit var database: MyBrainDatabase

    @BeforeTest
    fun createDatabase() {
        database = createTestDatabase()
    }

    @AfterTest
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun `no peers prune at the age boundary and preserve younger tombstones`() = runTest {
        tombstone("older", 999L, 1L)
        tombstone("boundary", 1000L, 2L)
        tombstone("younger", 1001L, 3L)

        database.syncDao().deleteExpiredTombstones(1000L, 500)

        assertEquals(listOf("younger"), remainingIds())
    }

    @Test
    fun `minimum acknowledgement includes offline peers and preserves received cursors`() = runTest {
        database.pairedDeviceDao().upsertDevice(device("online", 3L, true))
        database.pairedDeviceDao().upsertDevice(device("offline", 2L, false))
        tombstone("acknowledged", 1000L, 2L)
        tombstone("pending", 1000L, 3L)
        tombstone("young", 1001L, 1L)
        val sequence = database.syncDao().getLastSyncSequence()

        database.syncDao().deleteExpiredTombstones(1000L, 500)

        assertEquals(setOf("pending", "young"), remainingIds().toSet())
        assertEquals(sequence, database.syncDao().getLastSyncSequence())
        assertEquals(999L, database.pairedDeviceDao().getDevice("offline")?.lastSyncedSeq)
        assertEquals(2L, database.pairedDeviceDao().getDevice("offline")?.lastAcknowledgedLocalSeq)
    }

    @Test
    fun `new peer blocks pruning until unpaired`() = runTest {
        database.pairedDeviceDao().upsertDevice(device("old", 5L, true))
        database.pairedDeviceDao().upsertDevice(device("new", 0L, false))
        tombstone("pending", 1000L, 1L)

        database.syncDao().deleteExpiredTombstones(1000L, 500)
        assertEquals(listOf("pending"), remainingIds())

        database.pairedDeviceDao().deleteDevice("new")
        database.syncDao().deleteExpiredTombstones(1000L, 500)
        assertEquals(emptyList(), remainingIds())
    }

    @Test
    fun `batches are bounded and recheck newly paired devices`() = runTest {
        tombstone("first", 1000L, 1L)
        tombstone("second", 1000L, 2L)
        tombstone("young", 1001L, 3L)

        assertEquals(1, database.syncDao().deleteExpiredTombstones(1000L, 1))
        assertEquals(listOf("second", "young"), remainingIds())

        database.pairedDeviceDao().upsertDevice(device("new", 0L, false))
        assertEquals(0, database.syncDao().deleteExpiredTombstones(1000L, 1))
        assertEquals(listOf("second", "young"), remainingIds())

        database.pairedDeviceDao().deleteDevice("new")
        assertEquals(1, database.syncDao().deleteExpiredTombstones(1000L, 1))
        assertEquals(0, database.syncDao().deleteExpiredTombstones(1000L, 1))
        assertEquals(listOf("young"), remainingIds())
    }

    private suspend fun tombstone(id: String, deletedAt: Long, seq: Long) {
        database.syncDao().insertDeletedEntity(DeletedEntityEntity(id, id, "note", deletedAt, seq))
    }

    private suspend fun remainingIds() =
        database.syncDao().getDeletedEntitiesUpdatedAfter(0L, Long.MAX_VALUE).map { it.entityId }

    private fun device(id: String, acknowledgement: Long, connected: Boolean) = PairedDeviceEntity(
        id = id,
        name = id,
        ipAddress = "127.0.0.1",
        port = 8080,
        lastSyncedSeq = 999L,
        encryptionKey = "test-key",
        deviceVersion = 1,
        isConnected = connected,
        lastAcknowledgedLocalSeq = acknowledgement
    )
}
