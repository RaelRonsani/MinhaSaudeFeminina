package com.bemvinda.app.ui

import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Renderiza HTML rico dentro de um WebView do Android.
 * Envelopa o HTML com CSS básico pra ficar legível (mesma paleta rosa).
 */
@Composable
actual fun VisorHtml(html: String, modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = false // não precisamos, é conteúdo do nosso banco
                settings.defaultTextEncodingName = "UTF-8"
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = false
                setBackgroundColor(0x00000000)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                null,
                envolverComEstilo(html),
                "text/html",
                "UTF-8",
                null
            )
        }
    )
}

/**
 * Wrapper HTML com CSS que combina com a paleta do app.
 * Fontes, tamanhos e cores alinhados com a UI Compose.
 */
private fun envolverComEstilo(corpo: String): String = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
            body {
                font-family: -apple-system, sans-serif;
                font-size: 16px;
                line-height: 1.6;
                color: #000;
                background: #FCE7F1;
                padding: 16px;
                margin: 0;
            }
            h1, h2, h3 { color: #6A1B4F; }
            h1 { font-size: 22px; }
            h2 { font-size: 20px; }
            h3 { font-size: 18px; }
            img { max-width: 100%; height: auto; border-radius: 8px; margin: 8px 0; }
            iframe { max-width: 100%; border: 0; border-radius: 8px; }
            blockquote {
                border-left: 4px solid #F25C8A;
                margin: 12px 0;
                padding: 4px 12px;
                color: #444;
                background: rgba(242, 92, 138, 0.08);
            }
            code, pre {
                background: rgba(0,0,0,0.06);
                padding: 2px 6px;
                border-radius: 4px;
                font-family: monospace;
            }
            pre { padding: 12px; overflow-x: auto; }
            a { color: #F25C8A; }
            ul, ol { padding-left: 24px; }
        </style>
    </head>
    <body>
        $corpo
    </body>
    </html>
""".trimIndent()
