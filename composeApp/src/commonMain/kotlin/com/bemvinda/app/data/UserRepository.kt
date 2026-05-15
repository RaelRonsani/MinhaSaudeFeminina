package com.bemvinda.app.data

import com.bemvinda.app.model.Usuario
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

object UserRepository {

    suspend fun login(email: String, senha: String): Usuario? {
        val hash = hashSenha(senha)
        return try {
            supabase.from("usuarios")
                .select(Columns.ALL) {
                    filter {
                        eq("email", email)
                        eq("senha_hash", hash)
                    }
                    limit(1)
                }
                .decodeSingleOrNull<Usuario>()
        } catch (e: Exception) {
            println("DEBUG_LOGIN: ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun existeEmail(email: String): Boolean {
        return try {
            supabase.from("usuarios")
                .select(Columns.list("id")) {
                    filter { eq("email", email) }
                    limit(1)
                }
                .decodeList<Map<String, Long>>()
                .isNotEmpty()
        } catch (e: Exception) {
            println("DEBUG_EXISTE: ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun cadastrar(
        nome: String,
        email: String,
        senha: String,
        dataNascimento: String,
        receberNotificacao: Boolean
    ): Usuario? {
        return try {
            val novo = Usuario(
                nome = nome,
                email = email,
                senha_hash = hashSenha(senha),
                data_nascimento = dataNascimento,
                receber_notificacao = receberNotificacao
            )
            println("DEBUG_CADASTRO: tentando inserir $novo")
            supabase.from("usuarios")
                .insert(novo) { select() }
                .decodeSingleOrNull<Usuario>()
        } catch (e: Exception) {
            println("DEBUG_CADASTRO: ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun atualizarPerfil(
        usuarioId: Long,
        novoNome: String,
        novaSenha: String?
    ): Usuario? {
        return try {
            supabase.from("usuarios").update(
                {
                    set("nome", novoNome)
                    if (!novaSenha.isNullOrBlank()) {
                        set("senha_hash", hashSenha(novaSenha))
                    }
                }
            ) {
                filter { eq("id", usuarioId) }
                select()
            }.decodeSingleOrNull<Usuario>()
        } catch (e: Exception) {
            println("DEBUG_UPDATE: ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}