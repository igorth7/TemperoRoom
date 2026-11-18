package com.igor.tempero.data.repository

import android.util.Log
import com.igor.tempero.data.local.ItemCompra
import com.igor.tempero.data.local.ItemCompraDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/** Repository da lista de compras: o ÚNICO que conversa com o ItemCompraDao. */
class ItemCompraRepository(private val dao: ItemCompraDao) {

    fun buscarTodos(): Flow<List<ItemCompra>> =
        dao.buscarTodos().catch { erro ->
            Log.e(TAG, "Erro ao ler a lista de compras", erro)
            emit(emptyList())
        }

    fun buscarPorId(id: Int): Flow<ItemCompra?> =
        dao.buscarPorId(id).catch { erro ->
            Log.e(TAG, "Erro ao ler o item $id", erro)
            emit(null)
        }

    suspend fun inserir(item: ItemCompra): Boolean =
        executar("inserir") { dao.inserir(item) }

    suspend fun atualizar(item: ItemCompra): Boolean =
        executar("atualizar") { dao.atualizar(item) }

    suspend fun deletar(item: ItemCompra): Boolean =
        executar("deletar") { dao.deletar(item) }

    suspend fun limparComprados(): Boolean =
        executar("limpar os comprados") { dao.limparComprados() }

    private suspend fun executar(acao: String, operacao: suspend () -> Unit): Boolean {
        return try {
            operacao()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao $acao", e)
            false
        }
    }

    companion object {
        private const val TAG = "ItemCompraRepository"
    }
}
