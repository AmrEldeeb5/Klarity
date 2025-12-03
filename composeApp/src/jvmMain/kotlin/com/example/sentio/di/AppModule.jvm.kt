package com.example.sentio.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.sentio.db.SentioDatabase
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
        val driver = JdbcSqliteDriver("jdbc:sqlite:$databasePath")
        SentioDatabase.Schema.create(driver)
        driver
    }
}

private fun getDatabasePath(): String {
    val userHome = System.getProperty("user.home")
    val sentioDir = File(userHome, ".sentio")
    if (!sentioDir.exists()) {
        sentioDir.mkdirs()
    }
    return File(sentioDir, "sentio.db").absolutePath
}