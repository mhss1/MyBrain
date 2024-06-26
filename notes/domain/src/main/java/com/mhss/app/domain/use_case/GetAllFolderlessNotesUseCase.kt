package com.mhss.app.domain.use_case

import com.mhss.app.di.namedDefaultDispatcher
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.repository.NoteRepository
import com.mhss.app.preferences.domain.model.Order
import com.mhss.app.preferences.domain.model.OrderType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class GetAllFolderlessNotesUseCase(
    private val notesRepository: NoteRepository,
    @Named(namedDefaultDispatcher) private val defaultDispatcher: CoroutineDispatcher
) {
    operator fun invoke(order: Order) : Flow<List<Note>> {
        return notesRepository.getAllFolderlessNotes().map { list ->
            when (order.orderType) {
                is OrderType.ASC -> {
                    when (order) {
                        is Order.Alphabetical -> list.sortedWith(compareBy({!it.pinned}, { it.title }))
                        is Order.DateCreated -> list.sortedWith(compareBy({!it.pinned}, { it.createdDate }))
                        is Order.DateModified -> list.sortedWith(compareBy({!it.pinned}, { it.updatedDate }))
                        else -> list.sortedWith(compareBy({!it.pinned}, { it.updatedDate }))
                    }
                }
                is OrderType.DESC -> {
                    when (order) {
                        is Order.Alphabetical -> list.sortedWith(compareBy({it.pinned}, { it.title })).reversed()
                        is Order.DateCreated -> list.sortedWith(compareBy({it.pinned}, { it.createdDate })).reversed()
                        is Order.DateModified -> list.sortedWith(compareBy({it.pinned}, { it.updatedDate })).reversed()
                        else -> list.sortedWith(compareBy({it.pinned}, { it.updatedDate })).reversed()
                    }
                }
            }
        }.flowOn(defaultDispatcher)
    }

}