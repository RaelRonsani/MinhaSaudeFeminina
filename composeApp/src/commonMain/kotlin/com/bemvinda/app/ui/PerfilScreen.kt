package com.bemvinda.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bemvinda.app.data.EventRepository
import com.bemvinda.app.data.Session
import com.bemvinda.app.model.Evento
import com.bemvinda.app.ui.components.AppTopBar
import kotlinx.datetime.*

object PerfilScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val usuario by Session.usuario.collectAsState()
        var eventoMaisProximo by remember { mutableStateOf<Evento?>(null) }

        LaunchedEffect(usuario?.id) {
            val id = usuario?.id ?: return@LaunchedEffect
            val hoje = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val todos = EventRepository.listarDoUsuario(id)
            // Pega o próximo evento (data >= hoje), ou o último passado se não houver futuros
            eventoMaisProximo = todos
                .filter { it.data_evento >= hoje.toString() }
                .minByOrNull { it.data_evento }
                ?: todos.maxByOrNull { it.data_evento }
        }

        val u = usuario
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.RosaClaro)
        ) {
            AppTopBar(onMenuClick = { navigator.replaceAll(InicialScreen) })

            if (u == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sessão expirou")
                }
                return@Column
            }

            Spacer(Modifier.height(24.dp))

            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                u.nome,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            val (idade, diasParaAniversario) = remember(u.data_nascimento) {
                calcularIdadeEDiasParaAniversario(u.data_nascimento)
            }
            Text(
                "$idade Anos - Faltam $diasParaAniversario dias para seu aniversário!!",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextoRoxo,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Card "Informações do Ciclo" (vazio - feature futura)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.RosaCardBg)
            ) {
                Text(
                    "Informações do Ciclo:",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextoRoxo,
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    textAlign = TextAlign.Center
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    LinhaInfo("Duração do ciclo:", "")
                    LinhaInfo("Duração do Período:", "")
                    LinhaInfo("Última Menstruação:", "")
                }
            }

            Spacer(Modifier.height(24.dp))

            // Card evento mais próximo
            eventoMaisProximo?.let { ev ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CardEvento(ev)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LinhaInfo(label: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 18.sp, color = Color.Black)
        Text(valor, fontSize = 18.sp, color = AppColors.TextoRoxo, fontWeight = FontWeight.Bold)
    }
}

/**
 * Calcula idade em anos e dias restantes até o próximo aniversário.
 * Recebe data ISO yyyy-MM-dd.
 */
private fun calcularIdadeEDiasParaAniversario(dataIso: String): Pair<Int, Int> {
    return try {
        val nasc = LocalDate.parse(dataIso)
        val hoje = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val aniversarioEsteAno = LocalDate(hoje.year, nasc.monthNumber, nasc.dayOfMonth)
        val jaFezAniversarioEsteAno = aniversarioEsteAno <= hoje
        val idade = hoje.year - nasc.year - if (jaFezAniversarioEsteAno) 0 else 1
        val proximoAniversario = if (jaFezAniversarioEsteAno) {
            LocalDate(hoje.year + 1, nasc.monthNumber, nasc.dayOfMonth)
        } else {
            aniversarioEsteAno
        }
        val dias = hoje.daysUntil(proximoAniversario)
        idade to dias
    } catch (e: Exception) {
        0 to 0
    }
}
