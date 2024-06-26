package com.mhss.app.database.di

import androidx.room.Room
import com.mhss.app.database.MyBrainDatabase
import com.mhss.app.mybrain.data.local.migrations.MIGRATION_1_2
import com.mhss.app.mybrain.data.local.migrations.MIGRATION_2_3
import com.mhss.app.mybrain.data.local.migrations.MIGRATION_3_4
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
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

}