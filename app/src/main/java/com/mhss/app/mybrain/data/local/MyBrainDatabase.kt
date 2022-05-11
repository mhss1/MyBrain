package com.mhss.app.mybrain.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mhss.app.mybrain.data.local.dao.BookmarkDao
import com.mhss.app.mybrain.data.local.dao.DiaryDao
import com.mhss.app.mybrain.data.local.dao.NoteDao
import com.mhss.app.mybrain.data.local.dao.TaskDao
import com.mhss.app.mybrain.domain.model.Bookmark
import com.mhss.app.mybrain.domain.model.Diary
import com.mhss.app.mybrain.domain.model.Note
import com.mhss.app.mybrain.domain.model.Task

@Database(
    entities = [Note::class, Task::class, Diary::class, Bookmark::class],
    version = 1
)
abstract class MyBrainDatabase: RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
    abstract fun diaryDao(): DiaryDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        const val DATABASE_NAME = "by_brain_db"
    }
}