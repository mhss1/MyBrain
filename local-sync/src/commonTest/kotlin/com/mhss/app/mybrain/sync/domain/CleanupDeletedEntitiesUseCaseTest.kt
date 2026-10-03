package com.mhss.app.mybrain.sync.domain

import com.mhss.app.datetime.now
import com.mhss.app.mybrain.sync.repository.SyncRepository
import com.mhss.app.preferences.domain.model.PrefsKey
import com.mhss.app.preferences.domain.repository.PreferenceRepository
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import dev.mokkery.answering.calls
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days

class CleanupDeletedEntitiesUseCaseTest {
    @Test
    fun `first cleanup applies ninety day cutoff and records successful completion`() = runTest {
        val fixture = Fixture()
        val start = now()

        fixture.useCase(start)

        assertEquals(listOf(start - 90.days.inWholeMilliseconds), fixture.cutoffs)
        assertTrue(fixture.preferences.timestamp in start..now())
        assertEquals(1, fixture.preferences.saves)
    }

    @Test
    fun `cleanup is skipped within one day and for future completion timestamps`() = runTest {
        val currentTime = now()
        for (lastCleanup in listOf(currentTime - 1.days.inWholeMilliseconds + 1, currentTime + 1)) {
            val fixture = Fixture(lastCleanup)

            fixture.useCase(currentTime)

            assertTrue(fixture.cutoffs.isEmpty())
            assertEquals(0, fixture.preferences.saves)
        }
    }

    @Test
    fun `cleanup runs exactly one day after previous completion`() = runTest {
        val currentTime = now()
        val fixture = Fixture(currentTime - 1.days.inWholeMilliseconds)

        fixture.useCase(currentTime)
        fixture.useCase(now())

        assertEquals(listOf(currentTime - 90.days.inWholeMilliseconds), fixture.cutoffs)
        assertEquals(1, fixture.preferences.saves)
    }

    @Test
    fun `failed cleanup preserves completion timestamp and can retry`() = runTest {
        val fixture = Fixture(1L)
        fixture.failure = IllegalStateException("database unavailable")

        assertFailsWith<IllegalStateException> { fixture.useCase() }
        assertEquals(1L, fixture.preferences.timestamp)
        assertEquals(0, fixture.preferences.saves)

        fixture.failure = null
        fixture.useCase()
        assertEquals(2, fixture.cutoffs.size)
        assertEquals(1, fixture.preferences.saves)
    }

    @Test
    fun `cancellation propagates without recording completion`() = runTest {
        val fixture = Fixture()
        fixture.failure = CancellationException("cancelled")

        assertFailsWith<CancellationException> { fixture.useCase() }

        assertEquals(0, fixture.preferences.saves)
    }

    @Test
    fun `full batches continue until a partial batch completes`() = runTest {
        val fixture = Fixture()
        fixture.results.addAll(listOf(500, 500, 2))
        val currentTime = now()

        fixture.useCase(currentTime)

        assertEquals(List(3) { currentTime - 90.days.inWholeMilliseconds }, fixture.cutoffs)
        assertEquals(listOf(500, 500, 500), fixture.limits)
        assertEquals(1, fixture.preferences.saves)
    }

    @Test
    fun `exactly full batch requires an empty batch before completion`() = runTest {
        val fixture = Fixture()
        fixture.results.addAll(listOf(500, 0))

        fixture.useCase()

        assertEquals(2, fixture.cutoffs.size)
        assertEquals(1, fixture.preferences.saves)
    }

    @Test
    fun `failure and cancellation after a full batch leave completion unchanged`() = runTest {
        for (failure in listOf(IllegalStateException("failed"), CancellationException("cancelled"))) {
            val fixture = Fixture(1L)
            fixture.results.add(500)
            fixture.failure = failure
            fixture.failureAtCall = 2

            assertFailsWith<Exception> { fixture.useCase() }

            assertEquals(2, fixture.cutoffs.size)
            assertEquals(1L, fixture.preferences.timestamp)
            assertEquals(0, fixture.preferences.saves)
        }
    }

    private class Fixture(lastCleanup: Long = 0L) {
        val preferences = RecordingPreferences(lastCleanup)
        val cutoffs = mutableListOf<Long>()
        val limits = mutableListOf<Int>()
        val results = mutableListOf<Int>()
        var failure: Exception? = null
        var failureAtCall = 1
        private val repository = mock<SyncRepository>().also { repository ->
            everySuspend { repository.deleteExpiredTombstones(any(), any()) } calls { (cutoff: Long, limit: Int) ->
                cutoffs.add(cutoff)
                limits.add(limit)
                assertEquals(0, preferences.saves)
                if (cutoffs.size >= failureAtCall) failure?.let { throw it }
                if (results.isEmpty()) 0 else results.removeAt(0)
            }
        }
        val useCase = CleanupDeletedEntitiesUseCase(
            repository,
            GetPreferenceUseCase(preferences),
            SavePreferenceUseCase(preferences)
        )
    }

    private class RecordingPreferences(var timestamp: Long) : PreferenceRepository {
        var saves = 0

        override suspend fun <T> savePreference(key: PrefsKey<T>, value: T) {
            assertEquals("last_tombstone_cleanup_at", key.name)
            timestamp = value as Long
            saves++
        }

        override fun <T> getPreference(key: PrefsKey<T>, defaultValue: T) = flowOf(
            if (timestamp == 0L) defaultValue else {
                @Suppress("UNCHECKED_CAST")
                (timestamp as T)
            }
        )
    }
}
