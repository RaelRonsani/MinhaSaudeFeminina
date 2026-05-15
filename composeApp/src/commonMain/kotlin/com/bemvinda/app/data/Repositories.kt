package com.bemvinda.app.data

import com.bemvinda.app.model.Evento
import com.bemvinda.app.model.Noticia
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

object NewsRepository {

    /**
     * Lista notícias. Se [categorias] não for vazio, filtra por categoria.
     */
    suspend fun listar(categorias: List<String> = emptyList()): List<Noticia> {
        return try {
            supabase.from("noticias")
                .select(Columns.ALL) {
                    if (categorias.isNotEmpty()) {
                        filter { isIn("categoria", categorias) }
                    }
                    order("id", Order.DESCENDING)
                }
                .decodeList<Noticia>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun buscar(id: Long): Noticia? {
        return try {
            supabase.from("noticias")
                .select(Columns.ALL) {
                    filter { eq("id", id) }
                    limit(1)
                }
                .decodeSingleOrNull<Noticia>()
        } catch (e: Exception) {
            null
        }
    }
}

object EventRepository {

    suspend fun listarDoUsuario(usuarioId: Long): List<Evento> {
        return try {
            supabase.from("eventos")
                .select(Columns.ALL) {
                    filter { eq("usuario_id", usuarioId) }
                    order("data_evento", Order.ASCENDING)
                }
                .decodeList<Evento>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun adicionar(usuarioId: Long, dataEvento: String, descricao: String): Boolean {
        return try {
            val novo = Evento(
                usuario_id = usuarioId,
                data_evento = dataEvento,
                descricao = descricao
            )
            supabase.from("eventos").insert(novo)
            true
        } catch (e: Exception) {
            false
        }
    }
}
