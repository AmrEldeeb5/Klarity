package com.example.sentio.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.sentio.db.SentioDatabase
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = getDatabasePath()
        val driver = JdbcSqliteDriver("jdbc:sqlite:$databasePath")
        SentioDatabase.Schema.create(driver)
        return driver
    }

    private fun getDatabasePath(): String {
        val userHome = System.getProperty("user.home")
        val sentioDir = File(userHome, ".sentio")
        if (!sentioDir.exists()) {
            sentioDir.mkdirs()
        }
        return File(sentioDir, "sentio.db").absolutePath
    }
}
