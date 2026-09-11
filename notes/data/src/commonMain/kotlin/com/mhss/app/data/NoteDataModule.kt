package com.mhss.app.data

import com.mhss.app.data.impl.MarkdownNoteRepositoryImpl
import com.mhss.app.data.impl.RoomNoteRepositoryImpl
import com.mhss.app.database.dao.NoteDao
import com.mhss.app.database.dao.SyncDao
import com.mhss.app.database.sync.LocalChangeObserver
import com.mhss.app.database.helpers.DatabaseTransactionProvider
import com.mhss.app.domain.repository.NoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.qualifier.named
import org.koin.dsl.module

@Module
@ComponentScan("com.mhss.app.data")
class NoteDataModule {

    @Factory
    fun defaultNoteRepository(
        noteDao: NoteDao,
        syncDao: SyncDao,
        changeObserver: LocalChangeObserver,
        transactionProvider: DatabaseTransactionProvider,
        @Named("ioDispatcher") ioDispatcher: CoroutineDispatcher
    ): NoteRepository {
        return RoomNoteRepositoryImpl(noteDao, syncDao, changeObserver, transactionProvider, ioDispatcher)
    }
}

val noteRoomModule = module {
    factory<NoteRepository> {
        RoomNoteRepositoryImpl(get(), get(), get(), get(), get(named("ioDispatcher")))
    }
}

fun noteMarkdownModule(rootId: String) = module {
    factory<NoteRepository> {
        MarkdownNoteRepositoryImpl(get(), rootId, get(named("defaultDispatcher")))
    }
}
