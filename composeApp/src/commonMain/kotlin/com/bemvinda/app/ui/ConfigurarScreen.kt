package com.bemvinda.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bemvinda.app.data.Session
import com.bemvinda.app.data.UserRepository
import com.bemvinda.app.ui.components.AppTopBar
import com.bemvinda.app.ui.components.AvisoDialog
import kotlinx.coroutines.launch

object ConfigurarScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val usuario by Session.usuario.collectAsState()

        val u = usuario
        var nome by remember(u?.id) { mutableStateOf(u?.nome ?: "") }
        var senha by remember { mutableStateOf("") }
        var msgDialog by remember { mutableStateOf<String?>(null) }
        var carregando by remember { mutableStateOf(false) }

        msgDialog?.let { m -> AvisoDialog(titulo = m) { msgDialog = null } }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.RosaClaro)
        ) {
            AppTopBar(
                onMenuClick = { navigator.replaceAll(InicialScreen) },
                onPerfilClick = { navigator.push(PerfilScreen) }
            )

            if (u == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Sessão expirou") }
                return@Column
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Cabeçalho "Editar perfil"
                Box(
                    modifier = Modifier
                        .background(AppColors.RosaForte, RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text("Editar perfil", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                }

                Spacer(Modifier.height(24.dp))

                Label("Nome:")
                CampoComLapis(
                    value = nome,
                    onValueChange = { nome = it }
                )
                Spacer(Modifier.height(16.dp))

                Label("Email:")
                CampoCaixa(value = u.email, onValueChange = {}) // readOnly visual
                Spacer(Modifier.height(16.dp))

                Label("Senha:")
                CampoComLapis(value = senha, onValueChange = { senha = it }, senha = true)
                Spacer(Modifier.height(16.dp))

                Label("Data de Nascimento:")
                CampoCaixa(
                    value = isoParaBR(u.data_nascimento),
                    onValueChange = {}
                ) // readOnly visual

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        if (nome.isBlank()) {
                            msgDialog = "Nome não pode ficar em branco"
                            return@Button
                        }
                        scope.launch {
                            carregando = true
                            val atualizado = UserRepository.atualizarPerfil(
                                usuarioId = u.id!!,
                                novoNome = nome.trim(),
                                novaSenha = senha.ifBlank { null }
                            )
                            carregando = false
                            if (atualizado != null) {
                                Session.atualizar(atualizado)
                                senha = ""
                                msgDialog = "Informações salvas!"
                            } else {
                                msgDialog = "Ocorreu um erro ao salvar suas informações, por favor tente novamente"
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.RosaForte,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (carregando) "..." else "Salvar", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun Label(texto: String) {
    Text(
        texto,
        color = Color.Black,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
}

@Composable
private fun CampoComLapis(
    value: String,
    onValueChange: (String) -> Unit,
    senha: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) {
            CampoCaixa(value = value, onValueChange = onValueChange, senha = senha)
        }
        Icon(
            Icons.Default.Edit,
            contentDescription = "Editar",
            modifier = Modifier
                .padding(start = 8.dp)
                .size(28.dp)
                .clickable { /* foco no campo - opcional */ }
        )
    }
}

/** "2000-12-31" → "31/12/2000". Retorna a string original se não casar. */
private fun isoParaBR(iso: String): String {
    val partes = iso.split("-")
    if (partes.size != 3) return iso
    return "${partes[2]}/${partes[1]}/${partes[0]}"
}
