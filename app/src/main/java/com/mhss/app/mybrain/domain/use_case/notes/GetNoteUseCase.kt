package com.mhss.app.mybrain.domain.use_case.notes

import com.mhss.app.mybrain.domain.repository.notes.NoteRepository
import org.koin.core.annotation.Single

@Single
class GetNoteUseCase(
    private val notesRepository: NoteRepository
) {
    suspend operator fun invoke(id: Int) = notesRepository.getNote(id)
}