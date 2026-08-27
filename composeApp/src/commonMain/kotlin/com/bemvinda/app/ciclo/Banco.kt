package com.bemvinda.app.ciclo

import app.cash.sqldelight.db.SqlDriver
import com.bemvinda.app.db.BemVindaDb

/**
 * Fábrica de driver do banco local SQLite.
 * Cada plataforma implementa do seu jeito:
 *  - Android: AndroidSqliteDriver + contexto do app
 *  - iOS: NativeSqliteDriver (baseado em SQLite nativo do iOS)
 *
 * Não instancie diretamente. Use [Banco].
 */
expect class DriverFactory {
    fun criarDriver(): SqlDriver
}

/**
 * Ponto único de acesso ao banco local. Singleton lazy.
 *
 * Uso:
 *   val ciclo = Banco.instancia.ciclosQueries.buscarPorId(1).executeAsOneOrNull()
 */
object Banco {
    private var _instancia: BemVindaDb? = null
    private var _driverFactory: DriverFactory? = null

    /**
     * Deve ser chamado UMA vez, no início do app.
     * Android: em MainActivity.onCreate, ANTES do setContent.
     * iOS: em MainViewController, ANTES de retornar o ComposeUIViewController.
     */
    fun inicializar(driverFactory: DriverFactory) {
        _driverFactory = driverFactory
    }

    val instancia: BemVindaDb
        get() {
            if (_instancia == null) {
                val factory = _driverFactory
                    ?: error("Banco não inicializado. Chame Banco.inicializar(DriverFactory()) antes de usar.")
                _instancia = BemVindaDb(factory.criarDriver())
            }
            return _instancia!!
        }
}
