package com.bemvinda.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    val id: Long? = null,
    val nome: String,
    val email: String,
    val senha_hash: String,
    val data_nascimento: String, // ISO yyyy-MM-dd
    val receber_notificacao: Boolean = true
)

@Serializable
data class Noticia(
    val id: Long? = null,
    val categoria: String, // AUTOCUIDADO, SAÚDE, GRAVIDEZ, BEM-ESTAR, PUBERDADE, SEGURANÇA FEMININA
    val titulo: String,
    val resumo: String,
    val conteudo: String
)

@Serializable
data class Evento(
    val id: Long? = null,
    val usuario_id: Long,
    val data_evento: String, // ISO yyyy-MM-dd
    val descricao: String
)

enum class CategoriaNoticia(val label: String) {
    AUTOCUIDADO("AUTOCUIDADO"),
    SAUDE("SAÚDE"),
    GRAVIDEZ("GRAVIDEZ"),
    BEM_ESTAR("BEM-ESTAR"),
    PUBERDADE("PUBERDADE"),
    SEGURANCA_FEMININA("SEGURANÇA FEMININA")
}
