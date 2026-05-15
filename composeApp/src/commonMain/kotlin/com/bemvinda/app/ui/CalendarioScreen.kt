package com.bemvinda.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import com.bemvinda.app.data.EventRepository
import com.bemvinda.app.data.Session
import com.bemvinda.app.ui.components.AppTopBar
import com.bemvinda.app.ui.components.AvisoDialog
import kotlinx.coroutines.launch
import kotlinx.datetime.*

object CalendarioScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val hoje = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

        var mesAno by remember { mutableStateOf(YearMonth(hoje.year, hoje.monthNumber)) }
        var diaSelecionado by remember { mutableStateOf<LocalDate?>(null) }
        var descricao by remember { mutableStateOf("") }
        var msgDialog by remember { mutableStateOf<String?>(null) }

        msgDialog?.let { m -> AvisoDialog(titulo = m) { msgDialog = null } }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.RosaClaro)
                .verticalScroll(rememberScrollState())
        ) {
            AppTopBar(
                onMenuClick = { navigator.replaceAll(InicialScreen) },
                onPerfilClick = { navigator.push(PerfilScreen) }
            )

            Spacer(Modifier.height(16.dp))
            Text(
                "Calendário",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            // Calendário
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                // Navegação mês/ano
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Mês anterior",
                        modifier = Modifier.clickable { mesAno = mesAno.previous() }
                    )
                    Text(
                        "${nomeMes(mesAno.month)} ${mesAno.year}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Próximo mês",
                        modifier = Modifier.clickable { mesAno = mesAno.next() }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Cabeçalho dos dias da semana
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Do", "Se", "Te", "Qa", "Qi", "Sx", "Sa").forEach {
                        Text(
                            it,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))

                // Grid de dias
                val celulas = construirCelulas(mesAno)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    userScrollEnabled = false
                ) {
                    items(celulas.size) { idx ->
                        val data = celulas[idx]
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        data == null -> Color.Transparent
                                        data == diaSelecionado -> Color.Black
                                        data == hoje -> Color(0xFFEDEDED)
                                        else -> Color.Transparent
                                    }
                                )
                                .let { mod ->
                                    if (data != null) mod.clickable { diaSelecionado = data }
                                    else mod
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (data != null) {
                                Text(
                                    text = data.dayOfMonth.toString(),
                                    color = if (data == diaSelecionado) Color.White else Color.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Legenda (feature futura)
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                LegendaItem(Color.Red, "Menstruação")
                LegendaItem(Color.Green, "Fértil")
                LegendaItem(Color.Blue, "Eventos")
                LegendaItem(Color.Yellow, "Ovulação")
            }

            Spacer(Modifier.height(16.dp))

            // Formulário adicionar evento
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(AppColors.RosaCardBg, RoundedCornerShape(16.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.RosaForte, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Adicionar Evento", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }

                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = diaSelecionado?.let { formatarBR(it) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Selecione o Dia") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descricao,
                        onValueChange = { descricao = it },
                        placeholder = { Text("Insira a Descrição do Evento") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val u = Session.usuario.value
                            val dia = diaSelecionado
                            if (u == null || dia == null || descricao.isBlank()) {
                                msgDialog = "Selecione o dia e preencha a descrição"
                                return@Button
                            }
                            scope.launch {
                                val ok = EventRepository.adicionar(
                                    usuarioId = u.id!!,
                                    dataEvento = dia.toString(), // ISO yyyy-MM-dd
                                    descricao = descricao
                                )
                                msgDialog = if (ok) "Evento adicionado!" else "Erro ao adicionar"
                                if (ok) {
                                    descricao = ""
                                    diaSelecionado = null
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.VerdeAdicionar,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Adicionar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LegendaItem(cor: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(cor))
        Spacer(Modifier.width(12.dp))
        Text(texto, fontSize = 18.sp, color = Color.Black)
    }
}

/** Auxiliar: par ano/mês. */
data class YearMonth(val year: Int, val month: Int) {
    fun previous(): YearMonth = if (month == 1) YearMonth(year - 1, 12) else YearMonth(year, month - 1)
    fun next(): YearMonth = if (month == 12) YearMonth(year + 1, 1) else YearMonth(year, month + 1)
}

/** Constrói a grade de 42 células (6 semanas x 7 dias), null para células vazias. */
private fun construirCelulas(ym: YearMonth): List<LocalDate?> {
    val primeiroDia = LocalDate(ym.year, ym.month, 1)
    val diasNoMes = primeiroDia.monthLength()
    // Domingo = 0, Segunda = 1, ..., Sábado = 6
    val deslocamento = primeiroDia.dayOfWeek.isoDayNumber % 7 // ISO: seg=1...dom=7; queremos dom=0
    val total = 42
    val celulas = ArrayList<LocalDate?>(total)
    repeat(deslocamento) { celulas.add(null) }
    for (d in 1..diasNoMes) celulas.add(LocalDate(ym.year, ym.month, d))
    while (celulas.size < total) celulas.add(null)
    return celulas
}

private fun LocalDate.monthLength(): Int {
    val proximo = if (monthNumber == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, monthNumber + 1, 1)
    return proximo.minus(1, DateTimeUnit.DAY).dayOfMonth
}

private fun nomeMes(m: Int): String = listOf(
    "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
    "Jul", "Ago", "Set", "Out", "Nov", "Dez"
)[m - 1]

private fun formatarBR(d: LocalDate): String =
    "${d.dayOfMonth.toString().padStart(2, '0')}/${d.monthNumber.toString().padStart(2, '0')}/${d.year}"
