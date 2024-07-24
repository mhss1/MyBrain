package com.mhss.app.domain.use_case

import com.mhss.app.di.namedDefaultDispatcher
import com.mhss.app.domain.repository.DiaryRepository
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.preferences.domain.model.Order
import com.mhss.app.preferences.domain.model.OrderType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class GetAllEntriesUseCase(
    private val diaryRepository: DiaryRepository,
    @Named(namedDefaultDispatcher) private val defaultDispatcher: CoroutineDispatcher
) {
    operator fun invoke(order: Order) : Flow<List<DiaryEntry>> {
        return diaryRepository.getAllEntries().map { entries ->
            when (order.orderType) {
                is OrderType.ASC -> {
                    when (order) {
                        is Order.Alphabetical -> entries.sortedBy { it.title }
                        is Order.DateCreated -> entries.sortedBy { it.createdDate }
                        is Order.DateModified -> entries.sortedBy { it.updatedDate }
                        else -> entries.sortedBy { it.updatedDate }
                    }
                }
                is OrderType.DESC -> {
                    when (order) {
                        is Order.Alphabetical -> entries.sortedByDescending { it.title }
                        is Order.DateCreated -> entries.sortedByDescending { it.createdDate }
                        is Order.DateModified -> entries.sortedByDescending { it.updatedDate }
                        else -> entries.sortedByDescending { it.updatedDate }
                    }
                }
            }
        }.flowOn(defaultDispatcher)
    }
}