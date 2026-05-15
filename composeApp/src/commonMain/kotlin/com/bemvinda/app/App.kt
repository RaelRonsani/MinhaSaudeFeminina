package com.bemvinda.app

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.bemvinda.app.ui.BemVindaTheme
import com.bemvinda.app.ui.HomeScreen

@Composable
fun App() {
    BemVindaTheme {
        Navigator(HomeScreen) { navigator ->
            SlideTransition(navigator)
        }
    }
}
