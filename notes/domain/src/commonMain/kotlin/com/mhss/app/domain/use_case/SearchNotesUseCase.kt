package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.NoteRepository
import org.koin.core.annotation.Factory

@Factory
class SearchNotesUseCase(
    private val notesRepository: NoteRepository
) {
    fun paged(query: String) = notesRepository.searchPagedNotes(query)

    suspend operator fun invoke(query: String) = notesRepository.searchNotes(query)
}
