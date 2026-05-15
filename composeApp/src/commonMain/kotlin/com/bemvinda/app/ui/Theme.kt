package com.bemvinda.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Paleta extraída do protótipo Figma.
 */
object AppColors {
    val RosaForte = Color(0xFFF25C8A)       // Header/topbar
    val RosaClaro = Color(0xFFF8C8DC)       // Fundo principal das telas internas
    val RosaCardBg = Color(0xFFFCE7F1)      // Fundo dos cards/botões grandes
    val RosaBotao = Color(0xFFF8C8DC)       // Botões cor de rosa claro (Entrar, Confirmar)
    val VerdeAdicionar = Color(0xFF1A8B1A)  // Botão "Adicionar" no calendário
    val CinzaListBg = Color(0xFFCFCFCF)     // Fundo da lista de notícias
    val TextoEscuro = Color(0xFF000000)
    val TextoRoxo = Color(0xFF6A1B4F)       // Subtítulos no perfil (idade, ciclo)
}

@Composable
fun BemVindaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = AppColors.RosaForte,
            onPrimary = Color.White,
            secondary = AppColors.RosaBotao,
            onSecondary = AppColors.TextoEscuro,
            background = AppColors.RosaClaro,
            onBackground = AppColors.TextoEscuro,
            surface = AppColors.RosaCardBg,
            onSurface = AppColors.TextoEscuro
        ),
        content = content
    )
}
