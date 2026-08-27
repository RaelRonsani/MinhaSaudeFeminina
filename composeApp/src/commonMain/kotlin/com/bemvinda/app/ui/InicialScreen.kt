package com.bemvinda.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bemvinda.app.data.Session
import com.bemvinda.app.ui.components.AppTopBar
import com.bemvinda.app.ui.components.ConfirmDialog

object InicialScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var confirmaSair by remember { mutableStateOf(false) }

        if (confirmaSair) {
            ConfirmDialog(
                titulo = "Você será desconectada da sua conta, Confirmar?",
                onConfirm = {
                    Session.logout()
                    navigator.replaceAll(LoginScreen)
                },
                onDismiss = { confirmaSair = false }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.RosaClaro)
                .verticalScroll(rememberScrollState())
        ) {
            AppTopBar(
                onMenuClick = { },
                onPerfilClick = { navigator.push(PerfilScreen) }
            )
            Spacer(Modifier.height(16.dp))

            BotaoMenu("NOTÍCIAS", Icons.Default.Article) { navigator.push(NoticiasScreen) }
            BotaoMenu("CALENDÁRIO", Icons.Default.CalendarMonth) { navigator.push(CalendarioScreen) }
            BotaoMenu("MEUS CICLOS", Icons.Default.Favorite) { navigator.push(HistoricoCiclosScreen) }
            BotaoMenu("ESTATÍSTICA", Icons.Default.QueryStats) { navigator.push(EstatisticasScreen) }
            BotaoMenu("CONFIGURAR", Icons.Default.Settings) { navigator.push(ConfigurarScreen) }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(AppColors.RosaForte, RoundedCornerShape(12.dp))
                    .clickable { confirmaSair = true }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text("Sair", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BotaoMenu(texto: String, icone: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .background(AppColors.RosaCardBg, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icone, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Black)
        Spacer(Modifier.width(24.dp))
        Text(texto, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}
