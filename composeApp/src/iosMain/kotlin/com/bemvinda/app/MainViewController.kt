package com.bemvinda.app

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Função chamada pelo Swift (em iOSApp.swift) para obter o
 * UIViewController que renderiza a UI Compose Multiplatform.
 */
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
