package com.mhss.app.mybrain.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mhss.app.mybrain.data.local.converters.DBConverters
import com.mhss.app.mybrain.data.local.dao.*
import com.mhss.app.mybrain.data.local.entity.AlarmEntity
import com.mhss.app.mybrain.data.local.entity.BookmarkEntity
import com.mhss.app.mybrain.data.local.entity.DiaryEntryEntity
import com.mhss.app.mybrain.data.local.entity.NoteEntity
import com.mhss.app.mybrain.data.local.entity.NoteFolderEntity
import com.mhss.app.mybrain.data.local.entity.TaskEntity

@Database(
    entities = [NoteEntity::class, TaskEntity::class, DiaryEntryEntity::class, BookmarkEntity::class, AlarmEntity::class, NoteFolderEntity::class],
    version = 4
)
@TypeConverters(DBConverters::class)
abstract class MyBrainDatabase: RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
    abstract fun diaryDao(): DiaryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun alarmDao(): AlarmDao

    companion object {
        const val DATABASE_NAME = "by_brain_db"
    }
}