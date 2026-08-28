package com.bemvinda.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renderiza HTML formatado (do Quill.js) usando o WebView nativo de cada
 * plataforma.
 *
 * Android: WebView (AndroidView em Compose)
 * iOS: WKWebView (via UIKitView em Compose)
 */
@Composable
expect fun VisorHtml(html: String, modifier: Modifier = Modifier)
