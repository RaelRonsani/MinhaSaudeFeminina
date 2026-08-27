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
import com.bemvinda.app.ciclo.CalculosCiclo
import com.bemvinda.app.ciclo.Ciclo
import com.bemvinda.app.ciclo.CicloRepository
import com.bemvinda.app.ciclo.FaseCiclo
import com.bemvinda.app.data.EventRepository
import com.bemvinda.app.data.Session
import com.bemvinda.app.ui.components.AppTopBar
import com.bemvinda.app.ui.components.AvisoDialog
import com.bemvinda.app.ui.components.ConfirmDialog
import kotlinx.coroutines.launch
import kotlinx.datetime.*

object CalendarioScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val hoje = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
        val usuario by Session.usuario.collectAsState()

        var mesAno by remember { mutableStateOf(YearMonth(hoje.year, hoje.monthNumber)) }
        var diaSelecionado by remember { mutableStateOf<LocalDate?>(null) }

        // Estado do evento (original)
        var descricaoEvento by remember { mutableStateOf("") }

        // Estado da menstruação (novo)
        var inicioMenstruacao by remember { mutableStateOf<LocalDate?>(null) }
        var fimMenstruacao by remember { mutableStateOf<LocalDate?>(null) }
        var selecionandoInicio by remember { mutableStateOf(true) }

        // Ciclos do usuário (para pintar o calendário)
        var ciclos by remember { mutableStateOf<List<Ciclo>>(emptyList()) }
        var ciclosSobrepostos by remember { mutableStateOf<Ciclo?>(null) }

        var msgDialog by remember { mutableStateOf<String?>(null) }

        // Carrega ciclos do usuário sempre que sessão muda
        LaunchedEffect(usuario?.email) {
            usuario?.email?.let { email ->
                ciclos = CicloRepository.listar(email)
            }
        }

        msgDialog?.let { m -> AvisoDialog(titulo = m) { msgDialog = null } }

        ciclosSobrepostos?.let { conflito ->
            ConfirmDialog(
                titulo = "Já existe ciclo registrado nessas datas " +
                        "(${formatarBR(conflito.dataInicio)} a ${formatarBR(conflito.dataFim)}). Deseja substituir?",
                onConfirm = {
                    val email = usuario?.email ?: return@ConfirmDialog
                    val ini = inicioMenstruacao ?: return@ConfirmDialog
                    val fim = fimMenstruacao ?: return@ConfirmDialog
                    scope.launch {
                        CicloRepository.substituir(
                            idAntigo = conflito.id!!,
                            novoCiclo = Ciclo(
                                emailUsuario = email,
                                dataInicio = ini,
                                dataFim = fim
                            )
                        )
                        ciclos = CicloRepository.listar(email)
                        inicioMenstruacao = null
                        fimMenstruacao = null
                        ciclosSobrepostos = null
                        msgDialog = "Ciclo substituído com sucesso"
                    }
                },
                onDismiss = { ciclosSobrepostos = null }
            )
        }

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

            // Grade do calendário com pintura de fases
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
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
                        val fase = data?.let { CalculosCiclo.determinarFase(it, ciclos) }
                            ?: FaseCiclo.NENHUMA
                        DiaCalendario(
                            data = data,
                            isHoje = data == hoje,
                            isSelecionado = data == diaSelecionado,
                            fase = fase,
                            onClick = { data?.let { diaSelecionado = it } }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Legenda funcional (agora realmente marca)
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                LegendaItem(Color(0xFFE53935), "Menstruação")
                LegendaItem(Color(0xFF43A047), "Fértil")
                LegendaItem(Color(0xFFFDD835), "Ovulação")
            }

            Spacer(Modifier.height(16.dp))

            // Card: Adicionar Evento (ORIGINAL, mantido)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(AppColors.RosaCardBg, RoundedCornerShape(16.dp))
            ) {
                CardHeader("Adicionar Evento")
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
                        value = descricaoEvento,
                        onValueChange = { descricaoEvento = it },
                        placeholder = { Text("Insira a Descrição do Evento") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val u = usuario
                            val dia = diaSelecionado
                            if (u == null || dia == null || descricaoEvento.isBlank()) {
                                msgDialog = "Selecione o dia e preencha a descrição"
                                return@Button
                            }
                            scope.launch {
                                val ok = EventRepository.adicionar(
                                    usuarioId = u.id!!,
                                    dataEvento = dia.toString(),
                                    descricao = descricaoEvento
                                )
                                msgDialog = if (ok) "Evento adicionado!" else "Erro ao adicionar"
                                if (ok) descricaoEvento = ""
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

            // Card: Adicionar Menstruação (NOVO)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(AppColors.RosaCardBg, RoundedCornerShape(16.dp))
            ) {
                CardHeader("Registrar Menstruação")
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Toque em um dia do calendário e defina se é INÍCIO ou FIM:",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BotaoSeletor(
                            texto = "Marcar INÍCIO",
                            ativo = selecionandoInicio,
                            modifier = Modifier.weight(1f),
                            onClick = { selecionandoInicio = true }
                        )
                        BotaoSeletor(
                            texto = "Marcar FIM",
                            ativo = !selecionandoInicio,
                            modifier = Modifier.weight(1f),
                            onClick = { selecionandoInicio = false }
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val dia = diaSelecionado
                            if (dia == null) {
                                msgDialog = "Selecione um dia no calendário primeiro"
                                return@Button
                            }
                            if (selecionandoInicio) inicioMenstruacao = dia
                            else fimMenstruacao = dia
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.RosaForte,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Aplicar dia selecionado", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Início: " + (inicioMenstruacao?.let { formatarBR(it) } ?: "—"),
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        "Fim: " + (fimMenstruacao?.let { formatarBR(it) } ?: "—"),
                        fontSize = 14.sp,
                        color = Color.Black
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val email = usuario?.email
                            val ini = inicioMenstruacao
                            val fim = fimMenstruacao
                            if (email == null || ini == null || fim == null) {
                                msgDialog = "Marque início E fim antes de salvar"
                                return@Button
                            }
                            if (fim < ini) {
                                msgDialog = "A data de FIM não pode ser antes do INÍCIO"
                                return@Button
                            }
                            scope.launch {
                                val ciclosAtuais = CicloRepository.listar(email)
                                val conflito = CalculosCiclo.encontrarSobreposicao(ini, fim, ciclosAtuais)
                                if (conflito != null) {
                                    ciclosSobrepostos = conflito
                                    return@launch
                                }

                                val cicloNovo = Ciclo(emailUsuario = email, dataInicio = ini, dataFim = fim)
                                val alertas = CalculosCiclo.validarNovoCiclo(cicloNovo, ciclosAtuais)

                                CicloRepository.inserir(cicloNovo)
                                ciclos = CicloRepository.listar(email)
                                inicioMenstruacao = null
                                fimMenstruacao = null

                                msgDialog = if (alertas.isEmpty()) {
                                    "Ciclo registrado com sucesso"
                                } else {
                                    "⚠ " + alertas.joinToString("\n\n") { it.mensagem }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.VerdeAdicionar,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Salvar Ciclo", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DiaCalendario(
    data: LocalDate?,
    isHoje: Boolean,
    isSelecionado: Boolean,
    fase: FaseCiclo,
    onClick: () -> Unit
) {
    val corFundo = when {
        data == null -> Color.Transparent
        isSelecionado -> Color.Black
        fase == FaseCiclo.MENSTRUACAO -> Color(0xFFE53935)
        fase == FaseCiclo.FERTIL -> Color(0xFF43A047)
        fase == FaseCiclo.OVULACAO -> Color(0xFFFDD835)
        isHoje -> Color(0xFFEDEDED)
        else -> Color.Transparent
    }
    val corTexto = when {
        isSelecionado -> Color.White
        fase == FaseCiclo.MENSTRUACAO -> Color.White
        fase == FaseCiclo.FERTIL -> Color.White
        fase == FaseCiclo.OVULACAO -> Color.Black
        else -> Color.Black
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(corFundo)
            .let { m -> if (data != null) m.clickable { onClick() } else m },
        contentAlignment = Alignment.Center
    ) {
        if (data != null) {
            Text(text = data.dayOfMonth.toString(), color = corTexto, fontSize = 14.sp)
        }
    }
}

@Composable
private fun BotaoSeletor(
    texto: String,
    ativo: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (ativo) AppColors.RosaForte else AppColors.RosaCardBg,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(texto, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CardHeader(titulo: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.RosaForte, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(titulo, fontWeight = FontWeight.Bold, fontSize = 22.sp)
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

private fun construirCelulas(ym: YearMonth): List<LocalDate?> {
    val primeiroDia = LocalDate(ym.year, ym.month, 1)
    val diasNoMes = primeiroDia.monthLength()
    val deslocamento = primeiroDia.dayOfWeek.isoDayNumber % 7
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
