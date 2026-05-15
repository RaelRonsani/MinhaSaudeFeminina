package com.bemvinda.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay

/**
 * Tela de splash. Mostra ícone + título por 4s e vai para PrimeirosPassosScreen.
 */
object HomeScreen : Screen {
    @androidx.compose.runtime.Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) {
            delay(4000)
            navigator.replaceAll(PrimeirosPassosScreen)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.RosaForte)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Female,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(140.dp)
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "CUIDAR DE VOCÊ NUNCA FOI TÃO SIMPLES",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(48.dp))
        }
    }
}
