package com.mhss.app.mybrain.sync.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.mhss.app.mybrain.sync.domain.TombstoneCleanupScheduler
import com.mhss.app.mybrain.sync.util.TOMBSTONE_CLEANUP_WORK_NAME
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TombstoneCleanupSchedulerTest {
    private lateinit var context: Context

    @BeforeTest
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setExecutor(SynchronousExecutor()).build()
        )
    }

    @AfterTest
    fun tearDown() {
        WorkManager.getInstance(context).cancelAllWork().result.get()
        WorkManagerTestInitHelper.closeWorkDatabase()
    }

    @Test
    fun `startup keeps one daily job waiting for device idle`() {
        val scheduler: TombstoneCleanupScheduler = AndroidTombstoneCleanupScheduler(context)
        val workManager = WorkManager.getInstance(context)

        scheduler.schedule()
        val first = workManager.getWorkInfosForUniqueWork(TOMBSTONE_CLEANUP_WORK_NAME).get().single()
        assertEquals(WorkInfo.State.ENQUEUED, first.state)
        assertTrue(first.constraints.requiresDeviceIdle())
        assertEquals(1.days.inWholeMilliseconds, first.periodicityInfo?.repeatIntervalMillis)

        scheduler.schedule()
        val second = workManager.getWorkInfosForUniqueWork(TOMBSTONE_CLEANUP_WORK_NAME).get().single()
        assertEquals(first.id, second.id)
        assertEquals(first.nextScheduleTimeMillis, second.nextScheduleTimeMillis)
    }
}
