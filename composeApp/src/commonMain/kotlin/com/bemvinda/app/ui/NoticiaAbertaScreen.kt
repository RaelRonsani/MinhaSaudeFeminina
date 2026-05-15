package com.bemvinda.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.bemvinda.app.data.NewsRepository
import com.bemvinda.app.model.Noticia
import com.bemvinda.app.ui.components.AppTopBar

data class NoticiaAbertaScreen(val noticiaId: Long) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var noticia by remember { mutableStateOf<Noticia?>(null) }

        LaunchedEffect(noticiaId) {
            noticia = NewsRepository.buscar(noticiaId)
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

            val n = noticia
            if (n == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Carregando...")
                }
                return@Column
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(AppColors.RosaCardBg, RoundedCornerShape(12.dp))
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    n.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    n.conteudo,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
