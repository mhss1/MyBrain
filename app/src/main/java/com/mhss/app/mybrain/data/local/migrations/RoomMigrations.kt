package com.mhss.app.mybrain.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Added note folders
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE note_folders (name TEXT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")

        db.execSQL("CREATE TABLE IF NOT EXISTS `notes_new` (`title` TEXT NOT NULL, `content` TEXT NOT NULL, `created_date` INTEGER NOT NULL, `updated_date` INTEGER NOT NULL, `pinned` INTEGER NOT NULL, `folder_id` INTEGER, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, FOREIGN KEY (folder_id) REFERENCES note_folders (id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        db.execSQL("INSERT INTO notes_new (title, content, created_date, updated_date, pinned, id) SELECT title, content, created_date, updated_date, pinned, id FROM notes")
        db.execSQL("DROP TABLE notes")
        db.execSQL("ALTER TABLE notes_new RENAME TO notes")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN recurring INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE tasks ADD COLUMN frequency INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN frequency_amount INTEGER NOT NULL DEFAULT 1")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("CREATE TABLE note_folders_temp (name TEXT PRIMARY KEY NOT NULL)")
        db.execSQL("INSERT INTO note_folders_temp (name) SELECT DISTINCT name FROM note_folders")

        db.execSQL(
            """
    CREATE TABLE notes_temp (
        title TEXT NOT NULL,
        content TEXT NOT NULL,
        created_date INTEGER NOT NULL,
        updated_date INTEGER NOT NULL,
        pinned INTEGER NOT NULL,
        folder_id TEXT, 
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        FOREIGN KEY (folder_id) REFERENCES note_folders_temp (name) ON UPDATE NO ACTION ON DELETE CASCADE
    )
    """
        )

        db.execSQL(
            """
    INSERT INTO notes_temp (title, content, created_date, updated_date, pinned, folder_id, id)
    SELECT  n.title, n.content, n.created_date, n.updated_date, n.pinned, nf.name, n.id
    FROM notes n
    LEFT JOIN note_folders nf ON n.folder_id = nf.id 
    """
        )

        db.execSQL("DROP TABLE notes")
        db.execSQL("DROP TABLE note_folders")
        db.execSQL("ALTER TABLE note_folders_temp RENAME TO note_folders")

        db.execSQL(
            """
    CREATE TABLE notes (
        title TEXT NOT NULL,
        content TEXT NOT NULL,
        created_date INTEGER NOT NULL,
        updated_date INTEGER NOT NULL,
        pinned INTEGER NOT NULL,
        folder_id TEXT, 
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        FOREIGN KEY (folder_id) REFERENCES note_folders (name) ON UPDATE NO ACTION ON DELETE CASCADE
    )
    """
        )
        db.execSQL("INSERT INTO notes (title, content, created_date, updated_date, pinned, folder_id, id) SELECT * FROM notes_temp")

        db.execSQL("DROP TABLE notes_temp")
    }
}
