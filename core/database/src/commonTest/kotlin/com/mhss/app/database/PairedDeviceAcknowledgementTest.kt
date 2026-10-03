package com.mhss.app.database

import com.mhss.app.database.entity.PairedDeviceEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PairedDeviceAcknowledgementTest : PlatformTest() {
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
    fun `paired devices start without confirmations`() = runTest {
        database.pairedDeviceDao().upsertDevice(device("phone"))

        assertEquals(0L, database.pairedDeviceDao().getDevice("phone")?.lastAcknowledgedLocalSeq)
    }

    @Test
    fun `each device confirms independently of received changes`() = runTest {
        val dao = database.pairedDeviceDao()
        dao.upsertDevice(device("phone"))
        dao.upsertDevice(device("tablet"))
        dao.upsertDevice(device("laptop"))

        dao.updateLastAcknowledgedLocalSeq("phone", 150L)
        dao.updateLastAcknowledgedLocalSeq("tablet", 130L)
        dao.updateLastAcknowledgedLocalSeq("laptop", 100L)

        assertEquals(150L, dao.getDevice("phone")?.lastAcknowledgedLocalSeq)
        assertEquals(130L, dao.getDevice("tablet")?.lastAcknowledgedLocalSeq)
        assertEquals(100L, dao.getDevice("laptop")?.lastAcknowledgedLocalSeq)
        assertEquals(100L, dao.getAllDevices().minOf { it.lastAcknowledgedLocalSeq })
        assertEquals(42L, dao.getDevice("phone")?.lastSyncedSeq)

        dao.updateLastAcknowledgedLocalSeq("laptop", 140L)

        assertEquals(130L, dao.getAllDevices().minOf { it.lastAcknowledgedLocalSeq })
    }

    @Test
    fun `repeated and older requests cannot reduce confirmation`() = runTest {
        val dao = database.pairedDeviceDao()
        dao.upsertDevice(device("phone"))
        dao.updateLastAcknowledgedLocalSeq("phone", 120L)

        for (sequence in listOf(120L, 100L, 0L, -1L)) {
            dao.updateLastAcknowledgedLocalSeq("phone", sequence)
            assertEquals(120L, dao.getDevice("phone")?.lastAcknowledgedLocalSeq)
        }
    }

    @Test
    fun `zero and negative requests leave initial confirmation unchanged`() = runTest {
        val dao = database.pairedDeviceDao()
        dao.upsertDevice(device("phone"))

        for (sequence in listOf(0L, -1L, Long.MIN_VALUE)) {
            dao.updateLastAcknowledgedLocalSeq("phone", sequence)
            assertEquals(0L, dao.getDevice("phone")?.lastAcknowledgedLocalSeq)
        }

        dao.updateLastAcknowledgedLocalSeq("phone", 150L)
        assertEquals(150L, dao.getDevice("phone")?.lastAcknowledgedLocalSeq)
    }

    @Test
    fun `requests from removed devices do not recreate their records`() = runTest {
        val dao = database.pairedDeviceDao()
        dao.upsertDevice(device("phone"))
        dao.deleteDevice("phone")

        dao.updateLastAcknowledgedLocalSeq("phone", 120L)

        assertNull(dao.getDevice("phone"))
    }

    private fun device(id: String) = PairedDeviceEntity(
        id = id,
        name = id,
        ipAddress = "127.0.0.1",
        port = 8080,
        lastSyncedSeq = 42L,
        encryptionKey = "test-key",
        deviceVersion = 1
    )
}
