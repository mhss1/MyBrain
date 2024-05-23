package com.mhss.app.mybrain.domain.use_case.notes

import com.mhss.app.mybrain.domain.model.notes.Note
import com.mhss.app.mybrain.domain.repository.notes.NoteRepository
import org.koin.core.annotation.Single

@Single
class AddNoteUseCase(
    private val notesRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note) = notesRepository.addNote(note)
}