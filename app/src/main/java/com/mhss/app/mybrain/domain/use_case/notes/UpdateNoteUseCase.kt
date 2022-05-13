package com.mhss.app.mybrain.domain.use_case.notes

import com.mhss.app.mybrain.domain.model.Note
import com.mhss.app.mybrain.domain.repository.NoteRepository
import javax.inject.Inject

class UpdateNoteUseCase @Inject constructor(
    private val notesRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note) = notesRepository.updateNote(note)
}