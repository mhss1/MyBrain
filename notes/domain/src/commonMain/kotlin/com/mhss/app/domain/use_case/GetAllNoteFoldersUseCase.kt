package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.NoteRepository
import org.koin.core.annotation.Factory

@Factory
class GetAllNoteFoldersUseCase(
    private val repository: NoteRepository
) {
    fun paged() = repository.getPagedNoteFolders()

    operator fun invoke() = repository.getAllNoteFolders()
}