package com.bemvinda.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bemvinda.app.ciclo.Banco
import com.bemvinda.app.ciclo.DriverFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Inicializa o banco local SQLite ANTES de renderizar a UI.
        // Sem isso, qualquer chamada a Banco.instancia dispara erro.
        Banco.inicializar(DriverFactory(applicationContext))

        setContent {
            App()
        }
    }
}