package com.mhss.app.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room3.Room
import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

actual fun createTestDatabase(): MyBrainDatabase =
    Room.inMemoryDatabaseBuilder<MyBrainDatabase>(
        ApplicationProvider.getApplicationContext<Context>()
    ).setDriver(AndroidSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

actual fun createMigrationTestHelper(databaseName: String): MigrationTestHelper {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    return MigrationTestHelper(
        instrumentation = instrumentation,
        file = instrumentation.targetContext.getDatabasePath(databaseName),
        driver = AndroidSQLiteDriver(),
        databaseClass = MyBrainDatabase::class,
        databaseFactory = { MyBrainDatabaseConstructor.initialize() }
    )
}

actual fun deleteTestDatabase(databaseName: String) {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    context.getDatabasePath(databaseName).parentFile?.mkdirs()
    context.deleteDatabase(databaseName)
}

actual fun readQueryPlan(databaseName: String, query: String): List<String> {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    return SQLiteDatabase.openDatabase(
        context.getDatabasePath(databaseName).absolutePath,
        null,
        SQLiteDatabase.OPEN_READONLY
    ).use { database ->
        database.rawQuery(query, null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(3))
            }
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
actual abstract class PlatformTest actual constructor()
