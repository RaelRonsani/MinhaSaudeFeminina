package com.bemvinda.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.bemvinda.app.data.NewsRepository
import com.bemvinda.app.model.CategoriaNoticia
import com.bemvinda.app.model.Noticia
import com.bemvinda.app.ui.components.AppTopBar
import kotlinx.coroutines.launch

object NoticiasScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val categoriasSelecionadas = remember { mutableStateListOf<String>() }
        var noticias by remember { mutableStateOf<List<Noticia>>(emptyList()) }
        var mostrarFiltros by remember { mutableStateOf(false) }
        var carregando by remember { mutableStateOf(false) }

        // Função de recarregar reusada pelo primeiro load e pelo pull-to-refresh
        suspend fun recarregar() {
            carregando = true
            noticias = NewsRepository.listar(categoriasSelecionadas.toList())
            carregando = false
        }

        // Recarrega quando o filtro muda
        LaunchedEffect(categoriasSelecionadas.toList()) {
            recarregar()
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

            // Título
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(AppColors.RosaCardBg, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("NOTÍCIAS", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            // Filtro
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .background(AppColors.RosaCardBg, RoundedCornerShape(20.dp))
                    .clickable { mostrarFiltros = !mostrarFiltros }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (categoriasSelecionadas.isEmpty()) "Filtro"
                    else "Filtro (${categoriasSelecionadas.size})",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.FilterAlt, contentDescription = null, tint = Color.Black)
            }

            if (mostrarFiltros) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    CategoriaNoticia.values().forEach { cat ->
                        val marcado = cat.label in categoriasSelecionadas
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (marcado) categoriasSelecionadas.remove(cat.label)
                                    else categoriasSelecionadas.add(cat.label)
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(checked = marcado, onCheckedChange = null)
                            Text(cat.label, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Lista com pull-to-refresh
            PullToRefreshBox(
                isRefreshing = carregando,
                onRefresh = { scope.launch { recarregar() } },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.CinzaListBg, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    if (noticias.isEmpty() && !carregando) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Nenhuma notícia. Arraste pra baixo para atualizar.",
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    items(noticias) { n ->
                        CardNoticia(n) {
                            navigator.push(NoticiaAbertaScreen(n.id ?: -1))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardNoticia(n: Noticia, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.RosaCardBg)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.RosaForte)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(n.categoria, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            n.titulo,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        if (n.resumo.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                n.resumo,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}
