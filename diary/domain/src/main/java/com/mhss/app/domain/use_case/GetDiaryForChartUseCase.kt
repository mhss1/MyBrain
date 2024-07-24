package com.mhss.app.domain.use_case

import com.mhss.app.di.namedDefaultDispatcher
import com.mhss.app.domain.repository.DiaryRepository
import com.mhss.app.domain.model.DiaryEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class GetDiaryForChartUseCase(
    private val diaryRepository: DiaryRepository,
    @Named(namedDefaultDispatcher) private val defaultDispatcher: CoroutineDispatcher

) {
    suspend operator fun invoke(filterSelector: (DiaryEntry) -> Boolean) : List<DiaryEntry>{
        return withContext(defaultDispatcher) {
            diaryRepository
                .getAllEntries()
                .first()
                .filter(filterSelector)
                .sortedBy { it.createdDate }
        }
    }
}
