package com.mhss.app.mybrain.domain.use_case.notes

import com.mhss.app.mybrain.domain.model.NoteFolder
import com.mhss.app.mybrain.domain.repository.NoteRepository
import org.koin.core.annotation.Single

@Single
class AddNoteFolderUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(folder: NoteFolder) = noteRepository.insertNoteFolder(folder)
}