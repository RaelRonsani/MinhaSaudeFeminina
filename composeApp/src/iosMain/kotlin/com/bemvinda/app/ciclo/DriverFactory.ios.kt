package com.bemvinda.app.ciclo

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.bemvinda.app.db.BemVindaDb

actual class DriverFactory {
    actual fun criarDriver(): SqlDriver {
        return NativeSqliteDriver(BemVindaDb.Schema, "bemvinda.db")
    }
}
