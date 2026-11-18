package com.igor.tempero.data.repository

import android.util.Log
import com.igor.tempero.data.local.Receita
import com.igor.tempero.data.local.ReceitaDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * Repository das receitas: é o ÚNICO que conversa com o ReceitaDao.
 * O ViewModel pede os dados aqui e não sabe que existe Room por trás.
 * O try/catch com Log.e (que no T3 ficava nas telas) agora fica aqui.
 */
class ReceitaRepository(private val dao: ReceitaDao) {

    fun buscarTodos(): Flow<List<Receita>> =
        dao.buscarTodos().catch { erro ->
            Log.e(TAG, "Erro ao ler as receitas", erro)
            emit(emptyList())
        }

    fun buscarPorId(id: Int): Flow<Receita?> =
        dao.buscarPorId(id).catch { erro ->
            Log.e(TAG, "Erro ao ler a receita $id", erro)
            emit(null)
        }

    suspend fun inserir(receita: Receita): Boolean =
        executar("inserir") { dao.inserir(receita) }

    suspend fun atualizar(receita: Receita): Boolean =
        executar("atualizar") { dao.atualizar(receita) }

    suspend fun deletar(receita: Receita): Boolean =
        executar("deletar") { dao.deletar(receita) }

    /** Roda a operação no banco; devolve true se deu certo e false se deu erro. */
    private suspend fun executar(acao: String, operacao: suspend () -> Unit): Boolean {
        return try {
            operacao()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao $acao receita", e)
            false
        }
    }

    companion object {
        private const val TAG = "ReceitaRepository"
    }
}
