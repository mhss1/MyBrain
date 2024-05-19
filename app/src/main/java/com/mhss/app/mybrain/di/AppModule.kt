package com.mhss.app.mybrain.di

import android.content.Context
import androidx.room.Room
import com.mhss.app.mybrain.app.dataStore
import com.mhss.app.mybrain.data.local.MyBrainDatabase
import com.mhss.app.mybrain.data.local.migrations.MIGRATION_1_2
import com.mhss.app.mybrain.data.local.migrations.MIGRATION_2_3
import com.mhss.app.mybrain.data.local.migrations.MIGRATION_3_4
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single


const val namedIoDispatcher = "ioDispatcher"

@Module
@ComponentScan("com.mhss.app.mybrain.data")
class DataModule {
    @Single
    fun room(context: Context) = Room.databaseBuilder(
        context,
        MyBrainDatabase::class.java,
        MyBrainDatabase.DATABASE_NAME
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        .build()

    @Single
    fun noteDao(db: MyBrainDatabase) = db.noteDao()

    @Single
    fun taskDao(db: MyBrainDatabase) = db.taskDao()

    @Single
    fun diaryDao(db: MyBrainDatabase) = db.diaryDao()

    @Single
    fun bookmarkDao(db: MyBrainDatabase) = db.bookmarkDao()

    @Single
    fun alarmDao(db: MyBrainDatabase) = db.alarmDao()

    @Single
    @Named(namedIoDispatcher)
    fun ioDispatcher() = Dispatchers.IO

    @Single
    fun datastore(context: Context) = context.dataStore
}

@Module
@ComponentScan("com.mhss.app.mybrain.domain.use_case")
class UseCasesModule

@Module
@ComponentScan("com.mhss.app.mybrain.presentation")
class ViewModelsModule



