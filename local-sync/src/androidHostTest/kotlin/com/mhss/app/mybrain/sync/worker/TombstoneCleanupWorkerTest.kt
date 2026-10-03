package com.mhss.app.mybrain.sync.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.mhss.app.mybrain.sync.domain.CleanupDeletedEntitiesUseCase
import com.mhss.app.mybrain.sync.repository.SyncRepository
import com.mhss.app.preferences.domain.model.PrefsKey
import com.mhss.app.preferences.domain.repository.PreferenceRepository
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TombstoneCleanupWorkerTest {
    private lateinit var context: Context
    private lateinit var repository: SyncRepository
    private lateinit var useCase: CleanupDeletedEntitiesUseCase
    private var completionWrites = 0

    @BeforeTest
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = mock()
        val preferences = mock<PreferenceRepository>()
        every { preferences.getPreference(any<PrefsKey<Long>>(), any<Long>()) } returns flowOf(0L)
        everySuspend { preferences.savePreference(any<PrefsKey<Long>>(), any<Long>()) } calls {
            completionWrites++
            Unit
        }
        everySuspend { repository.deleteExpiredTombstones(any(), any()) } returns 0
        useCase = CleanupDeletedEntitiesUseCase(
            repository,
            GetPreferenceUseCase(preferences),
            SavePreferenceUseCase(preferences)
        )
    }

    @Test
    fun `successful cleanup records completion and finishes work`() = runTest {
        assertEquals(Result.success(), createWorker().doWork())
        assertEquals(1, completionWrites)
    }

    @Test
    fun `first and second failures request retry without recording completion`() = runTest {
        everySuspend { repository.deleteExpiredTombstones(any(), any()) } throws IllegalStateException("database unavailable")

        for (attempt in 0..1) {
            assertEquals(Result.retry(), createWorker(attempt).doWork())
        }
        assertEquals(0, completionWrites)
    }

    @Test
    fun `third failure or later ends retries without recording completion`() = runTest {
        everySuspend { repository.deleteExpiredTombstones(any(), any()) } throws IllegalStateException("database unavailable")

        for (attempt in 2..3) {
            assertEquals(Result.failure(), createWorker(attempt).doWork())
        }
        assertEquals(0, completionWrites)
    }

    @Test
    fun `cleanup can still succeed on its third attempt`() = runTest {
        assertEquals(Result.success(), createWorker(2).doWork())
        assertEquals(1, completionWrites)
    }

    @Test
    fun `stopped cleanup propagates cancellation without recording completion`() = runTest {
        everySuspend { repository.deleteExpiredTombstones(any(), any()) } throws CancellationException("device active")

        assertFailsWith<CancellationException> { createWorker(2).doWork() }
        assertEquals(0, completionWrites)
    }

    private fun createWorker(runAttemptCount: Int = 0) =
        TestListenableWorkerBuilder<TombstoneCleanupWorker>(context)
            .setRunAttemptCount(runAttemptCount)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ) = TombstoneCleanupWorker(useCase, appContext, workerParameters)
            })
            .build()
}
