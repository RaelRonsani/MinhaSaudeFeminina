package com.bemvinda.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bemvinda.app.data.Session
import com.bemvinda.app.data.UserRepository
import com.bemvinda.app.ui.components.AvisoDialog
import kotlinx.coroutines.launch

object LoginScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var email by remember { mutableStateOf("") }
        var senha by remember { mutableStateOf("") }
        var carregando by remember { mutableStateOf(false) }
        var mostrarErro by remember { mutableStateOf(false) }

        if (mostrarErro) {
            AvisoDialog(titulo = "Login ou senha inválidos") { mostrarErro = false }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.RosaForte)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.5f))
            // Email
            Text(
                "Email:",
                color = Color.Black,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            CampoCaixa(value = email, onValueChange = { email = it }, kbType = KeyboardType.Email)
            Spacer(Modifier.height(12.dp))

            // Senha
            Text(
                "Senha:",
                color = Color.Black,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            CampoCaixa(value = senha, onValueChange = { senha = it }, senha = true)
            Spacer(Modifier.height(4.dp))
            Text(
                "Esqueceu a senha?",
                color = Color.Black,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .clickable { navigator.push(EsqueceuSenhaScreen) }
            )

            Spacer(Modifier.height(48.dp))

            // Botão Entrar
            Button(
                onClick = {
                    if (carregando) return@Button
                    scope.launch {
                        carregando = true
                        val u = UserRepository.login(email.trim(), senha)
                        carregando = false
                        if (u != null) {
                            Session.login(u)
                            navigator.replaceAll(InicialScreen)
                        } else {
                            mostrarErro = true
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.RosaCardBg,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (carregando) "..." else "Entrar", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.weight(1f))
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Female,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

/**
 * Campo de texto branco com borda preta arredondada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoCaixa(
    value: String,
    onValueChange: (String) -> Unit,
    senha: Boolean = false,
    kbType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        visualTransformation = if (senha) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = kbType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.Black,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}
