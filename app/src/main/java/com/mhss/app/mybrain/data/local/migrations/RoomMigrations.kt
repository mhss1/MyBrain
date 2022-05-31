package com.mhss.app.mybrain.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Adding folder foreign key to notes table
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE note_folders (name TEXT NOT NULL, PRIMARY KEY (name))")

        database.execSQL("CREATE TABLE IF NOT EXISTS `notes_new` (`title` TEXT NOT NULL, `content` TEXT NOT NULL, `created_date` INTEGER NOT NULL, `updated_date` INTEGER NOT NULL, `pinned` INTEGER NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `folder` TEXT, FOREIGN KEY (folder) REFERENCES note_folders (name) ON UPDATE CASCADE ON DELETE CASCADE)")
        database.execSQL("INSERT INTO notes_new (title, content, created_date, updated_date, pinned, id) SELECT title, content, created_date, updated_date, pinned, id FROM notes")
        database.execSQL("DROP TABLE notes")
        database.execSQL("ALTER TABLE notes_new RENAME TO notes")
    }
}
