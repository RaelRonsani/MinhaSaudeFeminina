package com.bemvinda.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

object PrimeirosPassosScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.RosaForte)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.6f))
            Text(
                text = "Bem-vinda!",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 56.sp
            )
            Spacer(Modifier.weight(0.4f))
            Icon(
                imageVector = Icons.Default.Female,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(120.dp)
            )
            Spacer(Modifier.weight(0.6f))
            Text(
                text = "Primeiro Acesso",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { navigator.push(CadastroScreen) }
                    .padding(8.dp)
            )
            Spacer(Modifier.height(48.dp))
            Text(
                text = "Login",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { navigator.push(LoginScreen) }
                    .padding(8.dp)
            )
            Spacer(Modifier.weight(1f))
        }
    }
}
