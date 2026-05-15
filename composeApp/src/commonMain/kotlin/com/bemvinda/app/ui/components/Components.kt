package com.bemvinda.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bemvinda.app.ui.AppColors
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding

/**
 * Topbar usada em todas as telas internas (após login).
 * Menu a esquerda: volta para "Inicial"
 * Ícone de perfil à direita vai para tela Perfil
 */
@Composable
fun AppTopBar(
    onMenuClick: () -> Unit,
    onPerfilClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.RosaForte)
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(70.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onMenuClick() }
            )
            if (onPerfilClick != null) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AppColors.RosaCardBg)
                        .clickable { onPerfilClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = AppColors.RosaForte
                    )
                }
            }
        }
    }
}

/**
 * Dialog de confirmação (OK / Cancelar).
 */
@Composable
fun ConfirmDialog(
    titulo: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    textoConfirmar: String = "OK",
    textoCancelar: String = "Cancelar"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(textoConfirmar) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(textoCancelar) }
        }
    )
}

/**
 * Dialog de aviso (apenas OK).
 */
@Composable
fun AvisoDialog(
    titulo: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}
