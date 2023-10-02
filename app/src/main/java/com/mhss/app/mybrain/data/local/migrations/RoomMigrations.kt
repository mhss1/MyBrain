package com.mhss.app.mybrain.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Added note folders
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE note_folders (name TEXT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")

        database.execSQL("CREATE TABLE IF NOT EXISTS `notes_new` (`title` TEXT NOT NULL, `content` TEXT NOT NULL, `created_date` INTEGER NOT NULL, `updated_date` INTEGER NOT NULL, `pinned` INTEGER NOT NULL, `folder_id` INTEGER, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, FOREIGN KEY (folder_id) REFERENCES note_folders (id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        database.execSQL("INSERT INTO notes_new (title, content, created_date, updated_date, pinned, id) SELECT title, content, created_date, updated_date, pinned, id FROM notes")
        database.execSQL("DROP TABLE notes")
        database.execSQL("ALTER TABLE notes_new RENAME TO notes")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE tasks ADD COLUMN recurring INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE tasks ADD COLUMN frequency INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE tasks ADD COLUMN frequency_amount INTEGER NOT NULL DEFAULT 1")
    }
}
