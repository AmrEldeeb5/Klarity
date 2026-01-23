package com.example.klarity.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.klarity.db.KlarityDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

/**
 * JVM/Desktop-specific dependency module.
 * Provides platform-specific implementations like the database driver.
 */
actual fun platformModule(): Module = module {
    single<SqlDriver> {
        val databasePath = getDatabasePath()
        val databaseFile = File(databasePath)
        val databaseExists = databaseFile.exists()
        
        val driver = JdbcSqliteDriver("jdbc:sqlite:$databasePath")
        
        if (!databaseExists) {
            KlarityDatabase.Schema.create(driver)
            driver.execute(null, "PRAGMA user_version = ${KlarityDatabase.Schema.version}", 0)
        } else {
            // Check current version
            // Check current version
            val currentVersion = driver.executeQuery(
                identifier = null, 
                sql = "PRAGMA user_version;", 
                mapper = { cursor ->
                    if (cursor.next().value) {
                        app.cash.sqldelight.db.QueryResult.Value(cursor.getLong(0) ?: 0L)
                    } else {
                        app.cash.sqldelight.db.QueryResult.Value(0L)
                    }
                },
                parameters = 0
            ).value
            
            val schemaVersion = KlarityDatabase.Schema.version
            
            // If version is 0 but file exists, it's likely version 1 (pre-migration tracking)
            val effectiveVersion = if (currentVersion == 0L) 1L else currentVersion
            
            if (effectiveVersion < schemaVersion) {
                // Apply migrations
                KlarityDatabase.Schema.migrate(driver, effectiveVersion, schemaVersion)
                driver.execute(null, "PRAGMA user_version = $schemaVersion", 0)
            }
        }
        
        driver
    }
}

private fun getDatabasePath(): String {
    val userHome = System.getProperty("user.home")
    val klarityDir = File(userHome, ".klarity")
    if (!klarityDir.exists()) {
        klarityDir.mkdirs()
    }
    return File(klarityDir, "klarity.db").absolutePath
}
