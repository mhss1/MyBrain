package com.mhss.app.mybrain.sync.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mhss.app.mybrain.sync.domain.CleanupDeletedEntitiesUseCase
import com.mhss.app.mybrain.sync.util.TOMBSTONE_CLEANUP_MAX_ATTEMPTS
import kotlinx.coroutines.CancellationException
import org.koin.android.annotation.KoinWorker

@KoinWorker
class TombstoneCleanupWorker(
    private val cleanupDeletedEntities: CleanupDeletedEntitiesUseCase,
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        cleanupDeletedEntities()
        Result.success()
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Log.e("TombstoneCleanupWorker", "Tombstone cleanup failed", error)
        if (runAttemptCount < TOMBSTONE_CLEANUP_MAX_ATTEMPTS - 1) Result.retry() else Result.failure()
    }
}
