package com.bemvinda.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
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
import com.bemvinda.app.ciclo.CalculosCiclo
import com.bemvinda.app.ciclo.Ciclo
import com.bemvinda.app.ciclo.CicloRepository
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
        var ciclos by remember { mutableStateOf<List<Ciclo>>(emptyList()) }
        var indiceVisivel by remember { mutableStateOf(0) }

        LaunchedEffect(usuario?.id, usuario?.email) {
            usuario?.let { u ->
                val hoje = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val todos = EventRepository.listarDoUsuario(u.id!!)
                eventoMaisProximo = todos
                    .filter { it.data_evento >= hoje.toString() }
                    .minByOrNull { it.data_evento }
                    ?: todos.maxByOrNull { it.data_evento }

                ciclos = CicloRepository.listar(u.email).sortedByDescending { it.dataInicio }
                indiceVisivel = 0
            }
        }

        val u = usuario
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.RosaClaro)
                .verticalScroll(rememberScrollState())
        ) {
            AppTopBar(onMenuClick = { navigator.replaceAll(InicialScreen) })

            if (u == null) {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Text("Sessão expirou")
                }
                return@Column
            }

            Spacer(Modifier.height(24.dp))
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(120.dp).clip(CircleShape).align(Alignment.CenterHorizontally)
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

            Spacer(Modifier.height(16.dp))

            // Card "Informações do Ciclo" — agora COM dados
            InformacoesCicloCard(ciclos = ciclos)

            Spacer(Modifier.height(16.dp))

            // Card de navegação entre ciclos registrados
            HistoricoCicloCard(
                ciclos = ciclos,
                indice = indiceVisivel,
                onAnterior = {
                    if (indiceVisivel < ciclos.size - 1) indiceVisivel++
                },
                onProximo = {
                    if (indiceVisivel > 0) indiceVisivel--
                },
                onVerTodos = { navigator.push(HistoricoCiclosScreen) }
            )

            Spacer(Modifier.height(16.dp))

            // Card evento mais próximo (original)
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
private fun InformacoesCicloCard(ciclos: List<Ciclo>) {
    val duracaoMedia = remember(ciclos) { CalculosCiclo.duracaoMedia(ciclos) }
    val duracaoPeriodoMedia = remember(ciclos) {
        if (ciclos.isEmpty()) null
        else ciclos.map { it.duracaoSangramento }.average().toInt()
    }
    val ultimaMenstruacao = remember(ciclos) { ciclos.maxByOrNull { it.dataInicio } }
    val previsaoProxima = remember(ciclos) { CalculosCiclo.preverProximoCiclo(ciclos) }

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
            LinhaInfo(
                "Duração do ciclo:",
                duracaoMedia?.let { "$it dias (média)" } ?: "Registre 2+ ciclos"
            )
            LinhaInfo(
                "Duração do Período:",
                duracaoPeriodoMedia?.let { "$it dias" } ?: "—"
            )
            LinhaInfo(
                "Última Menstruação:",
                ultimaMenstruacao?.let { formatarBR(it.dataInicio) } ?: "—"
            )
            LinhaInfo(
                "Próximo ciclo previsto:",
                previsaoProxima?.let { formatarBR(it) }
                    ?: "Registre ${CalculosCiclo.MINIMO_CICLOS_PARA_PREVISAO}+ ciclos"
            )
        }
    }
}

@Composable
private fun HistoricoCicloCard(
    ciclos: List<Ciclo>,
    indice: Int,
    onAnterior: () -> Unit,
    onProximo: () -> Unit,
    onVerTodos: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.RosaCardBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.RosaForte, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Meus Ciclos Registrados",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
        }

        if (ciclos.isEmpty()) {
            Text(
                "Nenhum ciclo registrado ainda. Vá em Calendário para registrar seu primeiro.",
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        } else {
            val ciclo = ciclos.getOrNull(indice) ?: ciclos.first()

            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onAnterior,
                    enabled = indice < ciclos.size - 1
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Ciclo mais antigo")
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Ciclo ${ciclos.size - indice} de ${ciclos.size}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        "${formatarBR(ciclo.dataInicio)} a ${formatarBR(ciclo.dataFim)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        "Duração: ${ciclo.duracaoSangramento} dias",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }

                IconButton(
                    onClick = onProximo,
                    enabled = indice > 0
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Ciclo mais recente")
                }
            }

            TextButton(
                onClick = onVerTodos,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Ver todos em detalhes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LinhaInfo(label: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp, color = Color.Black)
        Text(valor, fontSize = 16.sp, color = AppColors.TextoRoxo, fontWeight = FontWeight.Bold)
    }
}

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

private fun formatarBR(d: LocalDate): String =
    "${d.dayOfMonth.toString().padStart(2, '0')}/${d.monthNumber.toString().padStart(2, '0')}/${d.year}"
