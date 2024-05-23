package com.mhss.app.mybrain.domain.use_case.notes

import com.mhss.app.mybrain.domain.repository.notes.NoteRepository
import org.koin.core.annotation.Single

@Single
class GetNoteFolderUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(folderId: Int) = repository.getNoteFolder(folderId)
}