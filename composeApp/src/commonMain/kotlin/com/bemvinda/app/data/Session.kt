package com.bemvinda.app.data

import com.bemvinda.app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Sessão em memória do usuário logado reseta quando o app é encerrado
 */
object Session {
    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    fun login(u: Usuario) { _usuario.value = u }
    fun logout() { _usuario.value = null }
    fun atualizar(u: Usuario) { _usuario.value = u }
}
