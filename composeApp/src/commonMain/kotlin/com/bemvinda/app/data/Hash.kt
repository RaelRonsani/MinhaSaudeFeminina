package com.bemvinda.app.data

/**
 * Hash de senha. SHA-256 é melhor que texto puro mas ainda longe do ideal —
 * em produção use bcrypt ou Supabase Auth. Para faculdade serve.
 *
 * Implementação concreta em androidMain/ (java.security.MessageDigest) e
 * iosMain/ (CommonCrypto via interop).
 */
expect fun hashSenha(senha: String): String
