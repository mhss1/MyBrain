package com.mhss.app.mybrain.domain.use_case.notes

import com.mhss.app.mybrain.domain.model.Note
import com.mhss.app.mybrain.domain.repository.NoteRepository
import com.mhss.app.mybrain.util.settings.Order
import com.mhss.app.mybrain.util.settings.OrderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAllNotesUseCase @Inject constructor(
    private val notesRepository: NoteRepository
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
        }
    }

}