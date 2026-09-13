package com.mhss.app.database

import androidx.room3.testing.MigrationTestHelper

expect fun createTestDatabase(): MyBrainDatabase

expect fun createMigrationTestHelper(databaseName: String): MigrationTestHelper

expect fun deleteTestDatabase(databaseName: String)

expect fun readQueryPlan(databaseName: String, query: String): List<String>

expect abstract class PlatformTest()
