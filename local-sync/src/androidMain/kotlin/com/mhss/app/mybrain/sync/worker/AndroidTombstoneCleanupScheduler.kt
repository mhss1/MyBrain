package com.mhss.app.mybrain.sync.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mhss.app.mybrain.sync.domain.TombstoneCleanupScheduler
import com.mhss.app.mybrain.sync.util.TOMBSTONE_CLEANUP_INTERVAL
import com.mhss.app.mybrain.sync.util.TOMBSTONE_CLEANUP_WORK_NAME
import org.koin.core.annotation.Factory
import java.util.concurrent.TimeUnit

@Factory(binds = [TombstoneCleanupScheduler::class])
class AndroidTombstoneCleanupScheduler(private val context: Context) : TombstoneCleanupScheduler {
    override fun schedule() {
        val request = PeriodicWorkRequestBuilder<TombstoneCleanupWorker>(
            TOMBSTONE_CLEANUP_INTERVAL.inWholeMilliseconds,
            TimeUnit.MILLISECONDS
        )
            .setConstraints(Constraints.Builder().setRequiresDeviceIdle(true).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TOMBSTONE_CLEANUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
