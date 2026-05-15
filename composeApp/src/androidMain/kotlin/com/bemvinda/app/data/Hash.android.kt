package com.bemvinda.app.data

import java.security.MessageDigest

actual fun hashSenha(senha: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(senha.encodeToByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
