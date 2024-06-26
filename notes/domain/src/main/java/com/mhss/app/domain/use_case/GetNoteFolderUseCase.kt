package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.NoteRepository
import org.koin.core.annotation.Single

@Single
class GetNoteFolderUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(folderId: Int) = repository.getNoteFolder(folderId)
}