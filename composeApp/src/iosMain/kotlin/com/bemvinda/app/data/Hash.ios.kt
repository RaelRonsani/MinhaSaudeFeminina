package com.bemvinda.app.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.posix.uint8_tVar

@OptIn(ExperimentalForeignApi::class)
actual fun hashSenha(senha: String): String {
    val data = senha.encodeToByteArray()
    val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
    memScoped {
        val out = allocArray<uint8_tVar>(CC_SHA256_DIGEST_LENGTH)
        data.usePinned { pinned ->
            CC_SHA256(pinned.addressOf(0), data.size.toUInt(), out)
        }
        for (i in 0 until CC_SHA256_DIGEST_LENGTH) {
            digest[i] = out[i]
        }
    }
    return digest.joinToString("") { it.toString(16).padStart(2, '0') }
}
