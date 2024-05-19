package com.mhss.app.mybrain.di

import androidx.room.Room
import com.mhss.app.mybrain.app.dataStore
import com.mhss.app.mybrain.data.backup.BackupRepositoryImpl
import com.mhss.app.mybrain.data.local.MyBrainDatabase
import com.mhss.app.mybrain.data.local.migrations.MIGRATION_1_2
import com.mhss.app.mybrain.data.local.migrations.MIGRATION_2_3
import com.mhss.app.mybrain.data.local.migrations.MIGRATION_3_4
import com.mhss.app.mybrain.data.repository.*
import com.mhss.app.mybrain.domain.repository.*
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.qualifier.named


val appModule = module {

    single(named("ioDispatcher")) {
        Dispatchers.IO
    }

    single {
        Room.databaseBuilder(
            androidApplication(),
            MyBrainDatabase::class.java,
            MyBrainDatabase.DATABASE_NAME
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
    }
    single { get<MyBrainDatabase>().noteDao() }
    single { get<MyBrainDatabase>().taskDao() }
    single { get<MyBrainDatabase>().diaryDao() }
    single { get<MyBrainDatabase>().bookmarkDao() }
    single { get<MyBrainDatabase>().alarmDao() }

    single { androidApplication().dataStore }

    single<NoteRepository> { NoteRepositoryImpl(get(), get(named("ioDispatcher"))) }
    single<TaskRepository> { TaskRepositoryImpl(get(), get(named("ioDispatcher"))) }
    single<BookmarkRepository> { BookmarkRepositoryImpl(get(), get(named("ioDispatcher"))) }
    single<DiaryRepository> { DiaryRepositoryImpl(get(), get(named("ioDispatcher"))) }
    single<CalendarRepository> { CalendarRepositoryImpl(get(), get(named("ioDispatcher"))) }
    single<AlarmRepository> { AlarmRepositoryImpl(get(), get(named("ioDispatcher"))) }
    single<SettingsRepository> { SettingsRepositoryImpl(get(), get(named("ioDispatcher"))) }
    single<BackupRepository> { BackupRepositoryImpl(get(), get(), get(named("ioDispatcher"))) }
}
@Module
@ComponentScan("com.mhss.app.mybrain.domain.use_case")
class UseCasesModule

@Module
@ComponentScan("com.mhss.app.mybrain.presentation")
class ViewModelsModule



