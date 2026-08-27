package com.bemvinda.app.ciclo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

/**
 * Repositório de ciclos, atua sobre o banco local SQLite via SQLDelight.
 *
 * Todas as operações rodam em Dispatchers.Default para não bloquear a UI.
 * SQLDelight é síncrono; envolver com withContext é boa prática mesmo assim.
 */
object CicloRepository {

    private val queries get() = Banco.instancia.ciclosQueries

    /**
     * Lista todos os ciclos do usuário, ordenados por data mais recente primeiro.
     */
    suspend fun listar(emailUsuario: String): List<Ciclo> = withContext(Dispatchers.Default) {
        queries.listarPorUsuario(emailUsuario).executeAsList().map { it.toCiclo() }
    }

    suspend fun buscar(id: Long): Ciclo? = withContext(Dispatchers.Default) {
        queries.buscarPorId(id).executeAsOneOrNull()?.toCiclo()
    }

    suspend fun contar(emailUsuario: String): Long = withContext(Dispatchers.Default) {
        queries.contarDoUsuario(emailUsuario).executeAsOne()
    }

    /**
     * Insere novo ciclo. NÃO valida sobreposição — quem valida é a UI, com
     * suporte de CalculosCiclo.encontrarSobreposicao (para decidir sobrescrever).
     */
    suspend fun inserir(ciclo: Ciclo) = withContext(Dispatchers.Default) {
        queries.inserir(
            email_usuario = ciclo.emailUsuario,
            data_inicio = ciclo.dataInicio.toString(),
            data_fim = ciclo.dataFim.toString(),
            fluxo = ciclo.fluxo?.name,
            colicas = ciclo.colicas?.toLong(),
            observacoes = ciclo.observacoes
        )
    }

    suspend fun atualizar(ciclo: Ciclo) = withContext(Dispatchers.Default) {
        val id = ciclo.id ?: error("Ciclo sem id não pode ser atualizado")
        queries.atualizar(
            data_inicio = ciclo.dataInicio.toString(),
            data_fim = ciclo.dataFim.toString(),
            fluxo = ciclo.fluxo?.name,
            colicas = ciclo.colicas?.toLong(),
            observacoes = ciclo.observacoes,
            id = id
        )
    }

    suspend fun deletar(id: Long) = withContext(Dispatchers.Default) {
        queries.deletar(id)
    }

    /**
     * Deleta e re-insere. Usado para o fluxo "já existe ciclo aqui, deseja
     * substituir?" quando o usuário confirma a substituição.
     */
    suspend fun substituir(idAntigo: Long, novoCiclo: Ciclo) = withContext(Dispatchers.Default) {
        queries.transaction {
            queries.deletar(idAntigo)
            queries.inserir(
                email_usuario = novoCiclo.emailUsuario,
                data_inicio = novoCiclo.dataInicio.toString(),
                data_fim = novoCiclo.dataFim.toString(),
                fluxo = novoCiclo.fluxo?.name,
                colicas = novoCiclo.colicas?.toLong(),
                observacoes = novoCiclo.observacoes
            )
        }
    }
}

/**
 * Mapper: linha da tabela (gerada pelo SQLDelight) → domain model.
 * SQLDelight gera uma data class `Ciclos` com os campos da tabela.
 */
private fun com.bemvinda.app.db.Ciclos.toCiclo(): Ciclo = Ciclo(
    id = id,
    emailUsuario = email_usuario,
    dataInicio = LocalDate.parse(data_inicio),
    dataFim = LocalDate.parse(data_fim),
    fluxo = Fluxo.fromNullable(fluxo),
    colicas = colicas?.toInt(),
    observacoes = observacoes
)
