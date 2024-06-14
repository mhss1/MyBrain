package com.mhss.app.mybrain.domain.use_case.notes

import com.mhss.app.mybrain.domain.repository.notes.NoteRepository
import com.mhss.app.mybrain.domain.model.preferences.Order
import com.mhss.app.mybrain.domain.model.preferences.OrderType
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class GetNotesByFolderUseCase(
    private val notesRepository: NoteRepository
) {
    operator fun invoke(id: Int, order: Order) = notesRepository.getNotesByFolder(id).map { list ->
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
