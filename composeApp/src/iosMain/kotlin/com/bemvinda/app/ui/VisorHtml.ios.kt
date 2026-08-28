package com.bemvinda.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

/**
 * iOS usa WKWebView (moderno, sandboxeado).
 * Compose Multiplatform expõe UIKitView pra embutir UIKit em Compose.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VisorHtml(html: String, modifier: Modifier) {
    UIKitView(
        modifier = modifier,
        factory = {
            WKWebView(
                frame = platform.CoreGraphics.CGRectZero.readValue(),
                configuration = WKWebViewConfiguration()
            )
        },
        update = { webView ->
            webView.loadHTMLString(envolverComEstilo(html), baseURL = null)
        }
    )
}

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
            img { max-width: 100%; height: auto; border-radius: 8px; margin: 8px 0; }
            iframe { max-width: 100%; border: 0; border-radius: 8px; }
            blockquote {
                border-left: 4px solid #F25C8A;
                margin: 12px 0;
                padding: 4px 12px;
                color: #444;
                background: rgba(242, 92, 138, 0.08);
            }
            code, pre { background: rgba(0,0,0,0.06); padding: 2px 6px; border-radius: 4px; }
            pre { padding: 12px; overflow-x: auto; }
            a { color: #F25C8A; }
            ul, ol { padding-left: 24px; }
        </style>
    </head>
    <body>$corpo</body>
    </html>
""".trimIndent()
