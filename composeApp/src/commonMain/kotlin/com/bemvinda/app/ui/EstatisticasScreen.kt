package com.bemvinda.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bemvinda.app.data.EventRepository
import com.bemvinda.app.data.Session
import com.bemvinda.app.model.Evento
import com.bemvinda.app.ui.components.AppTopBar

object EstatisticasScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var eventos by remember { mutableStateOf<List<Evento>>(emptyList()) }
        val usuario = Session.usuario.value

        LaunchedEffect(usuario?.id) {
            val id = usuario?.id ?: return@LaunchedEffect
            eventos = EventRepository.listarDoUsuario(id)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.RosaClaro)
        ) {
            AppTopBar(
                onMenuClick = { navigator.replaceAll(InicialScreen) },
                onPerfilClick = { navigator.push(PerfilScreen) }
            )

            Spacer(Modifier.height(16.dp))

            if (eventos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum evento cadastrado", color = Color.Black)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(eventos) { ev ->
                        CardEvento(ev)
                    }
                }
            }
        }
    }
}

@Composable
fun CardEvento(ev: Evento) {
    val dia = ev.data_evento.split("-").lastOrNull() ?: ""
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.RosaCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.RosaForte)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Dia", fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color.Black)
            Text(dia.trimStart('0'), fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color.Black)
        }
        Text(
            ev.descricao,
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.padding(16.dp)
        )
    }
}
