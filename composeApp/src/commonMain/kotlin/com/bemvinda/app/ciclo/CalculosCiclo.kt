package com.bemvinda.app.ciclo

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

/**

 * Referências:
 *
 *  FEBRASGO — Ciclo menstrual normal apresenta intervalo entre 24 e 38 dias;
 *     duração do sangramento de até 8 dias.
 *  Manual MSD — Sangramento menstrual normalmente dura de 4 a 8 dias.
 *  Fase lútea constante ~14 dias (base do cálculo de ovulação).
 *  Janela fértil = 5 dias antes da ovulação + dia da ovulação.
 */
object CalculosCiclo {

    /** Faixa considerada normal para INTERVALO entre ciclos (FEBRASGO). */
    const val INTERVALO_MIN_NORMAL = 24
    const val INTERVALO_MAX_NORMAL = 38

    /** Faixa considerada normal para DURAÇÃO do sangramento em dias (FEBRASGO/MSD). */
    const val SANGRAMENTO_MIN_NORMAL = 3
    const val SANGRAMENTO_MAX_NORMAL = 8

    /** Duração padrão do ciclo quando ainda não há dados suficientes. */
    const val CICLO_PADRAO_DIAS = 28

    /** Fase lútea (constante). Ovulação = próximo ciclo - 14 dias. */
    const val FASE_LUTEA_DIAS = 14

    /** Janela fértil: 5 dias antes da ovulação + dia da ovulação = 6 dias total. */
    const val DIAS_FERTEIS_ANTES_OVULACAO = 5

    /** Ciclos mínimos para calcular média personalizada. */
    const val MINIMO_CICLOS_PARA_PREVISAO = 3

    fun intervaloEntreCiclos(anterior: Ciclo, posterior: Ciclo): Int {
        return anterior.dataInicio.daysUntil(posterior.dataInicio)
    }

    fun duracaoMedia(ciclos: List<Ciclo>): Int? {
        if (ciclos.size < 2) return null
        val intervalos = ciclos.zipWithNext { a, b -> intervaloEntreCiclos(a, b) }
        return intervalos.average().toInt()
    }

    fun preverProximoCiclo(ciclos: List<Ciclo>): LocalDate? {
        if (ciclos.size < MINIMO_CICLOS_PARA_PREVISAO) return null
        val ordenados = ciclos.sortedBy { it.dataInicio }
        val ultimo = ordenados.last()
        val media = duracaoMedia(ordenados) ?: CICLO_PADRAO_DIAS
        return ultimo.dataInicio.plus(media, DateTimeUnit.DAY)
    }

    /**
     * Valida um ciclo ANTES de ser salvo. Retorna lista de avisos.
     * Cada aviso é INFORMATIVO (não bloqueia o salvamento) para permitir
     * que a usuária registre situações reais ainda que anormais — o médico
     * é quem interpreta.
     *
     * Regras de validação:
     *  - Duração do sangramento fora da faixa 3-8 dias (FEBRASGO/MSD)
     *  - Intervalo com o ciclo mais recente fora da faixa 24-38 dias
     *
     * @param novoCiclo o ciclo prestes a ser inserido
     * @param ciclosExistentes ciclos já registrados do usuário
     */
    fun validarNovoCiclo(
        novoCiclo: Ciclo,
        ciclosExistentes: List<Ciclo>
    ): List<AlertaCiclo> {
        val alertas = mutableListOf<AlertaCiclo>()

        // 1. Valida duração do sangramento
        val duracao = novoCiclo.duracaoSangramento
        when {
            duracao < SANGRAMENTO_MIN_NORMAL -> {
                alertas += AlertaCiclo(
                    tipo = TipoAlerta.SANGRAMENTO_CURTO,
                    mensagem = "Sangramento de $duracao ${diaOuDias(duracao)} " +
                            "é abaixo do esperado (faixa normal: " +
                            "$SANGRAMENTO_MIN_NORMAL a $SANGRAMENTO_MAX_NORMAL dias). " +
                            "Se persistir, procure orientação médica."
                )
            }
            duracao > SANGRAMENTO_MAX_NORMAL -> {
                val diff = duracao - SANGRAMENTO_MAX_NORMAL
                alertas += AlertaCiclo(
                    tipo = TipoAlerta.SANGRAMENTO_LONGO,
                    mensagem = "Sangramento de $duracao ${diaOuDias(duracao)} " +
                            "é $diff ${diaOuDias(diff)} mais longo que o esperado " +
                            "(faixa normal: até $SANGRAMENTO_MAX_NORMAL dias). " +
                            "Consulte um ginecologista para avaliação."
                )
            }
        }

        // 2. Valida intervalo com o ciclo anterior (se houver)
        val cicloAnterior = ciclosExistentes
            .filter { it.dataInicio < novoCiclo.dataInicio }
            .maxByOrNull { it.dataInicio }

        if (cicloAnterior != null) {
            val intervalo = intervaloEntreCiclos(cicloAnterior, novoCiclo)
            when {
                intervalo < INTERVALO_MIN_NORMAL -> {
                    val diff = INTERVALO_MIN_NORMAL - intervalo
                    alertas += AlertaCiclo(
                        tipo = TipoAlerta.INTERVALO_CURTO,
                        mensagem = "Você menstruou $diff ${diaOuDias(diff)} mais cedo " +
                                "que o padrão (intervalo mínimo esperado: " +
                                "$INTERVALO_MIN_NORMAL dias). " +
                                "Se persistir, procure orientação médica."
                    )
                }
                intervalo > INTERVALO_MAX_NORMAL -> {
                    val diff = intervalo - INTERVALO_MAX_NORMAL
                    alertas += AlertaCiclo(
                        tipo = TipoAlerta.INTERVALO_LONGO,
                        mensagem = "Você menstruou $diff ${diaOuDias(diff)} mais tarde " +
                                "que o padrão (intervalo máximo esperado: " +
                                "$INTERVALO_MAX_NORMAL dias). " +
                                "Se persistir, procure orientação médica."
                    )
                }
            }
        }

        return alertas
    }

    /**
     * Analisa os DOIS últimos ciclos registrados para alertar sobre desvio
     * de intervalo. Usado como resumo no Perfil/Histórico.
     *
     * @return alerta se intervalo dos 2 últimos ciclos estiver fora da faixa,
     *         null caso contrário.
     */
    fun avaliarUltimoIntervalo(ciclos: List<Ciclo>): AlertaCiclo? {
        if (ciclos.size < 2) return null
        val ordenados = ciclos.sortedBy { it.dataInicio }
        val ultimo = ordenados.last()
        val alertas = validarNovoCiclo(
            novoCiclo = ultimo,
            ciclosExistentes = ordenados.dropLast(1)
        )
        // Filtra só os alertas de intervalo (não de duração — esse é do próprio ciclo)
        return alertas.firstOrNull {
            it.tipo == TipoAlerta.INTERVALO_CURTO || it.tipo == TipoAlerta.INTERVALO_LONGO
        }
    }

    fun determinarFase(dia: LocalDate, ciclos: List<Ciclo>): FaseCiclo {
        if (ciclos.isEmpty()) return FaseCiclo.NENHUMA

        val emMenstruacaoRegistrada = ciclos.any { dia in it.dataInicio..it.dataFim }
        if (emMenstruacaoRegistrada) return FaseCiclo.MENSTRUACAO

        val ordenados = ciclos.sortedBy { it.dataInicio }
        val ultimo = ordenados.last()
        val media = duracaoMedia(ordenados) ?: CICLO_PADRAO_DIAS

        if (dia <= ultimo.dataFim) return FaseCiclo.NENHUMA

        val proximoInicio = ultimo.dataInicio.plus(media, DateTimeUnit.DAY)
        val duracaoSangramento = ultimo.duracaoSangramento.coerceIn(3, 8)
        val proximoFim = proximoInicio.plus(duracaoSangramento - 1, DateTimeUnit.DAY)
        if (dia in proximoInicio..proximoFim) return FaseCiclo.MENSTRUACAO

        val ovulacao = proximoInicio.plus(-FASE_LUTEA_DIAS, DateTimeUnit.DAY)
        if (dia == ovulacao) return FaseCiclo.OVULACAO

        val inicioFertil = ovulacao.plus(-DIAS_FERTEIS_ANTES_OVULACAO, DateTimeUnit.DAY)
        val fimFertil = ovulacao.plus(-1, DateTimeUnit.DAY)
        if (dia in inicioFertil..fimFertil) return FaseCiclo.FERTIL

        return FaseCiclo.NENHUMA
    }

    fun encontrarSobreposicao(
        dataInicio: LocalDate,
        dataFim: LocalDate,
        ciclosExistentes: List<Ciclo>,
        ignorarId: Long? = null
    ): Ciclo? {
        return ciclosExistentes.firstOrNull { c ->
            c.id != ignorarId &&
                    c.dataInicio <= dataFim &&
                    c.dataFim >= dataInicio
        }
    }

    private fun diaOuDias(n: Int): String = if (n == 1) "dia" else "dias"
}

/**
 * Alerta gerado por validação de ciclo.
 */
data class AlertaCiclo(
    val tipo: TipoAlerta,
    val mensagem: String
)

enum class TipoAlerta {
    SANGRAMENTO_CURTO,   // < 3 dias
    SANGRAMENTO_LONGO,   // > 8 dias
    INTERVALO_CURTO,     // < 24 dias
    INTERVALO_LONGO      // > 38 dias
}