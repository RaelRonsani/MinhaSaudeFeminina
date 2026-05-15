package com.bemvinda.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.bemvinda.app.data.UserRepository
import com.bemvinda.app.ui.components.AvisoDialog
import kotlinx.coroutines.launch

object CadastroScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var nome by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var senha by remember { mutableStateOf("") }
        var confirmaSenha by remember { mutableStateOf("") }
        var dataNasc by remember { mutableStateOf("") } // ddmmaaaa simples
        var notificacao by remember { mutableStateOf(true) }
        var msgDialog by remember { mutableStateOf<String?>(null) }
        var voltarAposOk by remember { mutableStateOf(false) }
        var carregando by remember { mutableStateOf(false) }

        msgDialog?.let { m ->
            AvisoDialog(titulo = m) {
                msgDialog = null
                if (voltarAposOk) navigator.replaceAll(LoginScreen)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.RosaForte)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            LabelCampo("Nome:")
            CampoCaixa(value = nome, onValueChange = { nome = it })
            Spacer(Modifier.height(12.dp))

            LabelCampo("Email:")
            CampoCaixa(value = email, onValueChange = { email = it }, kbType = KeyboardType.Email)
            Spacer(Modifier.height(12.dp))

            LabelCampo("Senha:")
            CampoCaixa(value = senha, onValueChange = { senha = it }, senha = true)
            Spacer(Modifier.height(12.dp))

            LabelCampo("Confirmar a Senha:")
            CampoCaixa(value = confirmaSenha, onValueChange = { confirmaSenha = it }, senha = true)
            Spacer(Modifier.height(12.dp))

            LabelCampo("Data de Nascimento:")
            CampoCaixa(
                value = dataNasc,
                onValueChange = { novo ->
                    // Aceita só dígitos e formata DD/MM/AAAA enquanto digita
                    val digitos = novo.filter { it.isDigit() }.take(8)
                    dataNasc = buildString {
                        digitos.forEachIndexed { i, c ->
                            append(c)
                            if (i == 1 || i == 3) append('/')
                        }
                    }
                },
                kbType = KeyboardType.Number
            )
            Spacer(Modifier.height(24.dp))

            // Switch de notificação (feature futura - apenas visual)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.RosaCardBg, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Deseja receber notificação?",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = notificacao, onCheckedChange = { notificacao = it })
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    when {
                        nome.isBlank() || email.isBlank() || senha.isBlank() ||
                                confirmaSenha.isBlank() || dataNasc.length != 10 -> {
                            msgDialog = "Preencha todos os campos"
                        }
                        senha != confirmaSenha -> {
                            msgDialog = "Senha não são iguais!"
                        }
                        else -> {
                            scope.launch {
                                carregando = true
                                if (UserRepository.existeEmail(email.trim())) {
                                    msgDialog = "Já existe um usuário com esse email"
                                } else {
                                    val iso = ddmmaaaaParaIso(dataNasc)
                                    val novo = UserRepository.cadastrar(
                                        nome = nome.trim(),
                                        email = email.trim(),
                                        senha = senha,
                                        dataNascimento = iso,
                                        receberNotificacao = notificacao
                                    )
                                    if (novo != null) {
                                        msgDialog = "Cadastrado com sucesso!"
                                        voltarAposOk = true
                                    } else {
                                        msgDialog = "Erro ao cadastrar. Tente novamente."
                                    }
                                }
                                carregando = false
                            }
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.RosaCardBg,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (carregando) "..." else "Cadastrar", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LabelCampo(texto: String) {
    Text(
        texto,
        color = Color.Black,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
}

/** Converte "31/12/2000" → "2000-12-31". Assume já validado em 10 chars. */
private fun ddmmaaaaParaIso(s: String): String {
    val (d, m, a) = s.split("/")
    return "$a-$m-$d"
}
