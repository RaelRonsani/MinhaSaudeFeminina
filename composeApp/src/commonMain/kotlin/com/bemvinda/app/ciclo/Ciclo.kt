package com.bemvinda.app.ciclo

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/**
 * Um registro de ciclo menstrual.
 *
 * @property dataInicio primeiro dia do sangramento
 * @property dataFim último dia do sangramento
 * @property fluxo LEVE / MODERADO / INTENSO (nullable)
 * @property colicas 0..5 (nullable)
 * @property observacoes texto livre (nullable)
 */
data class Ciclo(
    val id: Long? = null,
    val emailUsuario: String,
    val dataInicio: LocalDate,
    val dataFim: LocalDate,
    val fluxo: Fluxo? = null,
    val colicas: Int? = null,
    val observacoes: String? = null
) {
    /** Duração do sangramento em dias (inclusive). Ex: início 01, fim 05 => 5 dias. */
    val duracaoSangramento: Int
        get() = dataInicio.daysUntilInclusive(dataFim)
}

enum class Fluxo(val label: String) {
    LEVE("Leve"),
    MODERADO("Moderado"),
    INTENSO("Intenso");

    companion object {
        fun fromNullable(s: String?): Fluxo? = values().firstOrNull { it.name == s }
    }
}

/**
 * Fase do ciclo em um dia específico. Usada para pintar o calendário.
 */
enum class FaseCiclo {
    MENSTRUACAO,   // vermelho
    FERTIL,        // verde
    OVULACAO,      // amarelo
    NENHUMA
}

// Extension helper — kotlinx.datetime não tem "daysUntil inclusive" nativo
internal fun LocalDate.daysUntilInclusive(outra: LocalDate): Int {
    return this.daysUntil(outra) + 1
}
