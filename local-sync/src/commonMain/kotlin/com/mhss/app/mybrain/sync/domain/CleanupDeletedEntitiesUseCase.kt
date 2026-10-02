package com.mhss.app.mybrain.sync.domain

import com.mhss.app.datetime.now
import com.mhss.app.mybrain.sync.repository.SyncRepository
import com.mhss.app.mybrain.sync.util.TOMBSTONE_CLEANUP_BATCH_SIZE
import com.mhss.app.mybrain.sync.util.TOMBSTONE_CLEANUP_INTERVAL
import com.mhss.app.mybrain.sync.util.TOMBSTONE_RETENTION
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.longPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.yield
import org.koin.core.annotation.Factory

@Factory
class CleanupDeletedEntitiesUseCase(
    private val repository: SyncRepository,
    private val getPreference: GetPreferenceUseCase,
    private val savePreference: SavePreferenceUseCase
) {
    suspend operator fun invoke(currentTime: Long = now()) {
        val cleanupKey = longPreferencesKey(PrefsConstants.LAST_TOMBSTONE_CLEANUP_AT)
        val lastCleanup = getPreference(cleanupKey, 0L).first()
        if (lastCleanup != 0L && currentTime - lastCleanup < TOMBSTONE_CLEANUP_INTERVAL.inWholeMilliseconds) return
        val cutoff = currentTime - TOMBSTONE_RETENTION.inWholeMilliseconds
        do {
            currentCoroutineContext().ensureActive()
            val deleted = repository.deleteExpiredTombstones(cutoff, TOMBSTONE_CLEANUP_BATCH_SIZE)
            yield()
        } while (deleted == TOMBSTONE_CLEANUP_BATCH_SIZE)
        savePreference(cleanupKey, now())
    }
}
