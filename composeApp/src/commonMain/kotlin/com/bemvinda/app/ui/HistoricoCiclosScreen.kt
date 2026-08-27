package com.bemvinda.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.bemvinda.app.data.Session
import com.bemvinda.app.ui.components.AppTopBar
import com.bemvinda.app.ui.components.ConfirmDialog
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * Tela dedicada de histórico completo de ciclos.
 * Cumpre o requisito "consulta dos registros já realizados" do professor.
 *
 * Mostra:
 *  - Média pessoal e alerta (se houver desvio da faixa 24-38 dias)
 *  - Lista completa de ciclos ordenada do mais recente ao mais antigo
 *  - Detalhes por ciclo (datas, duração, observações se houver)
 *  - Botão de exclusão por ciclo
 */
object HistoricoCiclosScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val usuario by Session.usuario.collectAsState()

        var ciclos by remember { mutableStateOf<List<Ciclo>>(emptyList()) }
        var idParaDeletar by remember { mutableStateOf<Long?>(null) }

        LaunchedEffect(usuario?.email) {
            usuario?.email?.let { email ->
                ciclos = CicloRepository.listar(email).sortedByDescending { it.dataInicio }
            }
        }

        idParaDeletar?.let { id ->
            ConfirmDialog(
                titulo = "Excluir este ciclo? Esta ação não pode ser desfeita.",
                onConfirm = {
                    val email = usuario?.email
                    if (email != null) {
                        scope.launch {
                            CicloRepository.deletar(id)
                            ciclos = CicloRepository.listar(email).sortedByDescending { it.dataInicio }
                        }
                    }
                    idParaDeletar = null
                },
                onDismiss = { idParaDeletar = null }
            )
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
            Text(
                "Histórico de Ciclos",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))

            // Resumo estatístico + eventual alerta
            ResumoEstatistico(ciclos = ciclos)

            Spacer(Modifier.height(8.dp))

            if (ciclos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Nenhum ciclo registrado. Vá em Calendário para começar.",
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // itemsIndexed para poder calcular intervalo com o anterior
                    items(ciclos) { ciclo ->
                        val indice = ciclos.indexOf(ciclo)
                        val cicloPrevio = ciclos.getOrNull(indice + 1) // mais antigo
                        val intervalo = cicloPrevio?.let {
                            CalculosCiclo.intervaloEntreCiclos(it, ciclo)
                        }
                        CicloCard(
                            ciclo = ciclo,
                            intervaloAntesDeste = intervalo,
                            onDeletar = { idParaDeletar = ciclo.id }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumoEstatistico(ciclos: List<Ciclo>) {
    val duracaoMedia = CalculosCiclo.duracaoMedia(ciclos)
    val proximoPrevisto = CalculosCiclo.preverProximoCiclo(ciclos)
    val alerta = CalculosCiclo.avaliarUltimoIntervalo(ciclos)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.RosaCardBg)
            .padding(16.dp)
    ) {
        Text(
            "Total de ciclos registrados: ${ciclos.size}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        if (duracaoMedia != null) {
            Text(
                "Duração média do ciclo: $duracaoMedia dias",
                fontSize = 14.sp
            )
        }
        if (proximoPrevisto != null) {
            Text(
                "Próximo ciclo previsto: ${formatarBR(proximoPrevisto)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextoRoxo
            )
        } else if (ciclos.size < CalculosCiclo.MINIMO_CICLOS_PARA_PREVISAO) {
            Text(
                "Previsão disponível a partir de ${CalculosCiclo.MINIMO_CICLOS_PARA_PREVISAO} ciclos " +
                        "(${CalculosCiclo.MINIMO_CICLOS_PARA_PREVISAO - ciclos.size} restantes).",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }

        if (alerta != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                "⚠ " + alerta.mensagem,
                fontSize = 13.sp,
                color = Color(0xFFB71C1C),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "Referência: FEBRASGO define intervalo normal entre 24 e 38 dias.",
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun CicloCard(
    ciclo: Ciclo,
    intervaloAntesDeste: Int?,
    onDeletar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.RosaForte)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${formatarBR(ciclo.dataInicio)} — ${formatarBR(ciclo.dataFim)}",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 15.sp
            )
            IconButton(onClick = onDeletar) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Black)
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text("Duração do sangramento: ${ciclo.duracaoSangramento} dias", fontSize = 14.sp)

            if (intervaloAntesDeste != null) {
                val statusFaixa = when {
                    intervaloAntesDeste < CalculosCiclo.INTERVALO_MIN_NORMAL -> " (abaixo do normal)"
                    intervaloAntesDeste > CalculosCiclo.INTERVALO_MAX_NORMAL -> " (acima do normal)"
                    else -> ""
                }
                Text(
                    "Intervalo desde o ciclo anterior: $intervaloAntesDeste dias$statusFaixa",
                    fontSize = 14.sp,
                    color = if (statusFaixa.isEmpty()) Color.Black else Color(0xFFB71C1C)
                )
            }

            if (!ciclo.observacoes.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("Obs: ${ciclo.observacoes}", fontSize = 13.sp, color = Color.DarkGray)
            }
        }
    }
}

private fun formatarBR(d: LocalDate): String =
    "${d.dayOfMonth.toString().padStart(2, '0')}/${d.monthNumber.toString().padStart(2, '0')}/${d.year}"
