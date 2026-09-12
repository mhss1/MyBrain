@file:OptIn(ExperimentalUuidApi::class)
package com.mhss.app.database.migrations

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

val MIGRATION_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE note_folders (name TEXT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")

        connection.execSQL("CREATE TABLE IF NOT EXISTS `notes_new` (`title` TEXT NOT NULL, `content` TEXT NOT NULL, `created_date` INTEGER NOT NULL, `updated_date` INTEGER NOT NULL, `pinned` INTEGER NOT NULL, `folder_id` INTEGER, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, FOREIGN KEY (folder_id) REFERENCES note_folders (id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        connection.execSQL("INSERT INTO notes_new (title, content, created_date, updated_date, pinned, id) SELECT title, content, created_date, updated_date, pinned, id FROM notes")
        connection.execSQL("DROP TABLE notes")
        connection.execSQL("ALTER TABLE notes_new RENAME TO notes")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE tasks ADD COLUMN recurring INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE tasks ADD COLUMN frequency INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE tasks ADD COLUMN frequency_amount INTEGER NOT NULL DEFAULT 1")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {

    override suspend fun migrate(connection: SQLiteConnection) {
        val folderIdMapping = HashMap<Int, String>()

        connection.execSQL("CREATE TABLE note_folders_new (name TEXT NOT NULL, id TEXT PRIMARY KEY NOT NULL)")
        connection.execSQL("CREATE TABLE notes_new (title TEXT NOT NULL, content TEXT NOT NULL, created_date INTEGER NOT NULL, updated_date INTEGER NOT NULL, pinned INTEGER NOT NULL, folder_id TEXT, id TEXT PRIMARY KEY NOT NULL)")

        val folders = mutableListOf<Pair<Int, String>>()
        connection.prepare("SELECT id, name FROM note_folders").use { stmt ->
            while (stmt.step()) {
                folders.add(stmt.getInt(0) to stmt.getText(1))
            }
        }
        for ((oldId, name) in folders) {
            val newId = Uuid.generateV7().toString()
            folderIdMapping[oldId] = newId
            connection.prepare("INSERT INTO note_folders_new (id, name) VALUES (?, ?)").use { stmt ->
                stmt.bindText(1, newId)
                stmt.bindText(2, name)
                stmt.step()
            }
        }

        data class OldNote(
            val title: String, val content: String, val createdDate: Long,
            val updatedDate: Long, val pinned: Int, val folderId: Int?, val id: Int
        )
        val notes = mutableListOf<OldNote>()
        connection.prepare("SELECT id, title, content, created_date, updated_date, pinned, folder_id FROM notes").use { stmt ->
            while (stmt.step()) {
                notes.add(
                    OldNote(
                        title = stmt.getText(1),
                        content = stmt.getText(2),
                        createdDate = stmt.getLong(3),
                        updatedDate = stmt.getLong(4),
                        pinned = stmt.getInt(5),
                        folderId = if (stmt.isNull(6)) null else stmt.getInt(6),
                        id = stmt.getInt(0)
                    )
                )
            }
        }
        for (note in notes) {
            val newFolderId = note.folderId?.let { folderIdMapping[it] }
            val newId = Uuid.generateV7().toString()
            connection.prepare("INSERT INTO notes_new (id, title, content, created_date, updated_date, pinned, folder_id) VALUES (?, ?, ?, ?, ?, ?, ?)").use { stmt ->
                stmt.bindText(1, newId)
                stmt.bindText(2, note.title)
                stmt.bindText(3, note.content)
                stmt.bindLong(4, note.createdDate)
                stmt.bindLong(5, note.updatedDate)
                stmt.bindLong(6, note.pinned.toLong())
                if (newFolderId != null) stmt.bindText(7, newFolderId) else stmt.bindNull(7)
                stmt.step()
            }
        }

        connection.execSQL("DROP TABLE notes")
        connection.execSQL("DROP TABLE note_folders")
        connection.execSQL("ALTER TABLE note_folders_new RENAME TO note_folders")
        connection.execSQL("ALTER TABLE notes_new RENAME TO notes")

        connection.execSQL("CREATE TABLE bookmarks_new (url TEXT NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, created_date INTEGER NOT NULL, updated_date INTEGER NOT NULL, id TEXT PRIMARY KEY NOT NULL)")

        data class OldBookmark(
            val url: String, val title: String, val description: String,
            val createdDate: Long, val updatedDate: Long
        )
        val bookmarks = mutableListOf<OldBookmark>()
        connection.prepare("SELECT url, title, description, created_date, updated_date FROM bookmarks").use { stmt ->
            while (stmt.step()) {
                bookmarks.add(
                    OldBookmark(
                        url = stmt.getText(0),
                        title = stmt.getText(1),
                        description = stmt.getText(2),
                        createdDate = stmt.getLong(3),
                        updatedDate = stmt.getLong(4)
                    )
                )
            }
        }
        for (bookmark in bookmarks) {
            val newId = Uuid.generateV7().toString()
            connection.prepare("INSERT INTO bookmarks_new (id, url, title, description, created_date, updated_date) VALUES (?, ?, ?, ?, ?, ?)").use { stmt ->
                stmt.bindText(1, newId)
                stmt.bindText(2, bookmark.url)
                stmt.bindText(3, bookmark.title)
                stmt.bindText(4, bookmark.description)
                stmt.bindLong(5, bookmark.createdDate)
                stmt.bindLong(6, bookmark.updatedDate)
                stmt.step()
            }
        }

        connection.execSQL("DROP TABLE bookmarks")
        connection.execSQL("ALTER TABLE bookmarks_new RENAME TO bookmarks")

        val alarmIdSet = HashSet<Int>()
        connection.execSQL("CREATE TABLE alarms_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, time INTEGER NOT NULL)")

        data class OldAlarm(val id: Int, val time: Long)
        val alarms = mutableListOf<OldAlarm>()
        connection.prepare("SELECT id, time FROM alarms").use { stmt ->
            while (stmt.step()) {
                alarms.add(OldAlarm(stmt.getInt(0), stmt.getLong(1)))
            }
        }
        for (alarm in alarms) {
            connection.prepare("INSERT INTO alarms_new (id, time) VALUES (?, ?)").use { stmt ->
                stmt.bindLong(1, alarm.id.toLong())
                stmt.bindLong(2, alarm.time)
                stmt.step()
            }
            alarmIdSet.add(alarm.id)
        }

        connection.execSQL("DROP TABLE alarms")
        connection.execSQL("ALTER TABLE alarms_new RENAME TO alarms")

        connection.execSQL("CREATE TABLE tasks_new (title TEXT NOT NULL, description TEXT NOT NULL, is_completed INTEGER NOT NULL, priority INTEGER NOT NULL, created_date INTEGER NOT NULL, updated_date INTEGER NOT NULL, sub_tasks TEXT NOT NULL, dueDate INTEGER NOT NULL, recurring INTEGER NOT NULL, frequency INTEGER NOT NULL, frequency_amount INTEGER NOT NULL, alarmId INTEGER, id TEXT PRIMARY KEY NOT NULL)")

        data class OldTask(
            val id: Int, val title: String, val description: String,
            val isCompleted: Int, val priority: Int, val createdDate: Long,
            val updatedDate: Long, val subTasks: String, val dueDate: Long,
            val recurring: Int, val frequency: Int, val frequencyAmount: Int
        )
        val tasks = mutableListOf<OldTask>()
        connection.prepare("SELECT id, title, description, is_completed, priority, created_date, updated_date, sub_tasks, dueDate, recurring, frequency, frequency_amount FROM tasks").use { stmt ->
            while (stmt.step()) {
                tasks.add(
                    OldTask(
                        id = stmt.getInt(0),
                        title = stmt.getText(1),
                        description = stmt.getText(2),
                        isCompleted = stmt.getInt(3),
                        priority = stmt.getInt(4),
                        createdDate = stmt.getLong(5),
                        updatedDate = stmt.getLong(6),
                        subTasks = stmt.getText(7),
                        dueDate = stmt.getLong(8),
                        recurring = stmt.getInt(9),
                        frequency = stmt.getInt(10),
                        frequencyAmount = stmt.getInt(11)
                    )
                )
            }
        }
        for (task in tasks) {
            val alarmId = if (task.id in alarmIdSet) task.id else null
            val newId = Uuid.generateV7().toString()
            connection.prepare("INSERT INTO tasks_new (id, title, description, is_completed, priority, created_date, updated_date, sub_tasks, dueDate, recurring, frequency, frequency_amount, alarmId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)").use { stmt ->
                stmt.bindText(1, newId)
                stmt.bindText(2, task.title)
                stmt.bindText(3, task.description)
                stmt.bindLong(4, task.isCompleted.toLong())
                stmt.bindLong(5, task.priority.toLong())
                stmt.bindLong(6, task.createdDate)
                stmt.bindLong(7, task.updatedDate)
                stmt.bindText(8, task.subTasks)
                stmt.bindLong(9, task.dueDate)
                stmt.bindLong(10, task.recurring.toLong())
                stmt.bindLong(11, task.frequency.toLong())
                stmt.bindLong(12, task.frequencyAmount.toLong())
                if (alarmId != null) stmt.bindLong(13, alarmId.toLong()) else stmt.bindNull(13)
                stmt.step()
            }
        }

        connection.execSQL("DROP TABLE tasks")
        connection.execSQL("ALTER TABLE tasks_new RENAME TO tasks")

        connection.execSQL("CREATE TABLE diary_new (title TEXT NOT NULL, content TEXT NOT NULL, created_date INTEGER NOT NULL, updated_date INTEGER NOT NULL, mood INTEGER NOT NULL, id TEXT PRIMARY KEY NOT NULL)")

        data class OldDiary(
            val title: String, val content: String, val createdDate: Long,
            val updatedDate: Long, val mood: Int
        )
        val diaryEntries = mutableListOf<OldDiary>()
        connection.prepare("SELECT title, content, created_date, updated_date, mood FROM diary").use { stmt ->
            while (stmt.step()) {
                diaryEntries.add(
                    OldDiary(
                        title = stmt.getText(0),
                        content = stmt.getText(1),
                        createdDate = stmt.getLong(2),
                        updatedDate = stmt.getLong(3),
                        mood = stmt.getInt(4)
                    )
                )
            }
        }
        for (entry in diaryEntries) {
            val newId = Uuid.generateV7().toString()
            connection.prepare("INSERT INTO diary_new (id, title, content, created_date, updated_date, mood) VALUES (?, ?, ?, ?, ?, ?)").use { stmt ->
                stmt.bindText(1, newId)
                stmt.bindText(2, entry.title)
                stmt.bindText(3, entry.content)
                stmt.bindLong(4, entry.createdDate)
                stmt.bindLong(5, entry.updatedDate)
                stmt.bindLong(6, entry.mood.toLong())
                stmt.step()
            }
        }

        connection.execSQL("DROP TABLE diary")
        connection.execSQL("ALTER TABLE diary_new RENAME TO diary")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `assistant_threads` (`id` TEXT PRIMARY KEY NOT NULL, `title` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `sync_seq` INTEGER NOT NULL DEFAULT 1)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `assistant_messages` (`id` TEXT PRIMARY KEY NOT NULL, `thread_id` TEXT NOT NULL, `type` INTEGER NOT NULL, `content` TEXT NOT NULL, `metadata` TEXT, `created_at` INTEGER NOT NULL, `sync_seq` INTEGER NOT NULL DEFAULT 1)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_assistant_messages_thread_id` ON `assistant_messages` (`thread_id`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `paired_devices` (`id` TEXT PRIMARY KEY NOT NULL, `name` TEXT NOT NULL, `ip_address` TEXT NOT NULL, `port` INTEGER NOT NULL, `last_synced_at` INTEGER NOT NULL, `encryption_key` TEXT NOT NULL, `device_version` INTEGER NOT NULL DEFAULT 1, `is_connected` INTEGER NOT NULL DEFAULT 0, `candidate_ip_addresses` TEXT NOT NULL DEFAULT '[]', `custom_ip_address` TEXT)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `deleted_entities` (`id` TEXT PRIMARY KEY NOT NULL, `entity_id` TEXT NOT NULL, `entity_type` TEXT NOT NULL, `deleted_at` INTEGER NOT NULL, `sync_seq` INTEGER NOT NULL DEFAULT 1)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sync_state` (`id` INTEGER PRIMARY KEY NOT NULL, `last_seq` INTEGER NOT NULL)")

        connection.execSQL("ALTER TABLE notes ADD COLUMN sync_seq INTEGER NOT NULL DEFAULT 1")
        connection.execSQL("ALTER TABLE note_folders ADD COLUMN sync_seq INTEGER NOT NULL DEFAULT 1")
        connection.execSQL("ALTER TABLE note_folders ADD COLUMN updated_date INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE tasks ADD COLUMN sync_seq INTEGER NOT NULL DEFAULT 1")
        connection.execSQL("ALTER TABLE diary ADD COLUMN sync_seq INTEGER NOT NULL DEFAULT 1")
        connection.execSQL("ALTER TABLE bookmarks ADD COLUMN sync_seq INTEGER NOT NULL DEFAULT 1")

        var sequence = 0L
        val syncableTables = listOf(
            "notes",
            "note_folders",
            "tasks",
            "diary",
            "bookmarks",
            "assistant_threads",
            "assistant_messages",
            "deleted_entities"
        )
        for (table in syncableTables) {
            val rowIds = mutableListOf<Long>()
            connection.prepare("SELECT rowid FROM $table ORDER BY rowid").use { query ->
                while (query.step()) {
                    rowIds += query.getLong(0)
                }
            }
            connection.prepare("UPDATE $table SET sync_seq = ? WHERE rowid = ?").use { update ->
                for (rowId in rowIds) {
                    update.reset()
                    update.bindLong(1, ++sequence)
                    update.bindLong(2, rowId)
                    update.step()
                }
            }
        }
        connection.prepare("INSERT OR REPLACE INTO sync_state (id, last_seq) VALUES (1, ?)").use { statement ->
            statement.bindLong(1, sequence)
            statement.step()
        }

        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_sync_seq` ON `notes` (`sync_seq`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_note_folders_sync_seq` ON `note_folders` (`sync_seq`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_sync_seq` ON `tasks` (`sync_seq`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_diary_sync_seq` ON `diary` (`sync_seq`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmarks_sync_seq` ON `bookmarks` (`sync_seq`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_assistant_threads_sync_seq` ON `assistant_threads` (`sync_seq`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_assistant_messages_sync_seq` ON `assistant_messages` (`sync_seq`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_deleted_entities_sync_seq` ON `deleted_entities` (`sync_seq`)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_deleted_entities_entity_type_entity_id` ON `deleted_entities` (`entity_type`, `entity_id`)")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_pinned_updated_date` ON `notes` (`pinned` ASC, `updated_date` ASC)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_folder_id_pinned_updated_date` ON `notes` (`folder_id` ASC, `pinned` ASC, `updated_date` ASC)")

        connection.execSQL("CREATE TABLE `tasks_new` (`title` TEXT NOT NULL, `description` TEXT NOT NULL, `is_completed` INTEGER NOT NULL, `priority` INTEGER NOT NULL, `created_date` INTEGER NOT NULL, `updated_date` INTEGER NOT NULL, `sub_tasks` TEXT NOT NULL, `dueDate` INTEGER, `recurring` INTEGER NOT NULL, `frequency` INTEGER NOT NULL, `frequency_amount` INTEGER NOT NULL, `alarmId` INTEGER, `id` TEXT NOT NULL, `sync_seq` INTEGER NOT NULL DEFAULT 1, PRIMARY KEY(`id`))")
        connection.execSQL("INSERT INTO `tasks_new` SELECT `title`, `description`, `is_completed`, `priority`, `created_date`, `updated_date`, `sub_tasks`, NULLIF(`dueDate`, 0), `recurring`, `frequency`, `frequency_amount`, `alarmId`, `id`, `sync_seq` FROM `tasks`")
        connection.execSQL("DROP TABLE `tasks`")
        connection.execSQL("ALTER TABLE `tasks_new` RENAME TO `tasks`")
        connection.execSQL("CREATE INDEX `index_tasks_sync_seq` ON `tasks` (`sync_seq`)")
        connection.execSQL("CREATE INDEX `index_tasks_updated_date` ON `tasks` (`updated_date`)")
        connection.execSQL("CREATE INDEX `index_tasks_is_completed_updated_date` ON `tasks` (`is_completed`, `updated_date`)")
        connection.execSQL("CREATE INDEX `index_tasks_dueDate` ON `tasks` (`dueDate`)")
        connection.execSQL("CREATE INDEX `index_tasks_is_completed_dueDate` ON `tasks` (`is_completed`, `dueDate`)")

        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_diary_updated_date` ON `diary` (`updated_date` ASC)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_diary_created_date` ON `diary` (`created_date` ASC)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmarks_updated_date` ON `bookmarks` (`updated_date` ASC)")
    }
}
