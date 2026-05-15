package com.bemvinda.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bemvinda.app.data.UserRepository
import com.bemvinda.app.ui.components.AvisoDialog
import kotlinx.coroutines.launch

object EsqueceuSenhaScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var email by remember { mutableStateOf("") }
        var msgDialog by remember { mutableStateOf<String?>(null) }
        var voltarAposOk by remember { mutableStateOf(false) }

        msgDialog?.let { msg ->
            AvisoDialog(titulo = msg) {
                msgDialog = null
                if (voltarAposOk) {
                    navigator.replaceAll(PrimeirosPassosScreen)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.RosaForte)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(120.dp))
            Text(
                "Você esqueceu a senha?",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(60.dp))

            Text(
                "Confirme seu email:",
                color = Color.Black,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            CampoCaixa(value = email, onValueChange = { email = it }, kbType = KeyboardType.Email)
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        val existe = UserRepository.existeEmail(email.trim())
                        if (existe) {
                            // Envio real de email é feature futura — apenas exibe aviso
                            msgDialog = "As informações de redefinição foram enviadas ao seu email"
                            voltarAposOk = true
                        } else {
                            msgDialog = "Esse email não possui uma conta"
                            voltarAposOk = true
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.RosaCardBg,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirmar", fontSize = 22.sp, fontWeight = FontWeight.Bold)
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
