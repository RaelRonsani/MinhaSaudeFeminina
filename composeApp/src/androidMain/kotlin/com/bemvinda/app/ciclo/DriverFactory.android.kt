package com.bemvinda.app.ciclo

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.bemvinda.app.db.BemVindaDb

actual class DriverFactory(private val context: Context) {
    actual fun criarDriver(): SqlDriver {
        return AndroidSqliteDriver(BemVindaDb.Schema, context, "bemvinda.db")
    }
}
