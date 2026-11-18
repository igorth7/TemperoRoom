package com.igor.tempero.data.local

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update

/** DAO 2 – todas as operações da tabela "itens_compra". */
@Dao
interface ItemCompraDao {

    @Insert
    suspend fun inserir(item: ItemCompra)

    // Os que faltam comprar aparecem primeiro
    @Query("SELECT * FROM itens_compra ORDER BY comprado ASC, id ASC")
    suspend fun buscarTodos(): List<ItemCompra>

    @Query("SELECT * FROM itens_compra WHERE id = :id")
    suspend fun buscarPorId(id: Int): ItemCompra?

    // Consulta filtrada: itens que vieram de uma receita
    @Query("SELECT * FROM itens_compra WHERE receitaId = :receitaId")
    suspend fun buscarPorReceita(receitaId: Int): List<ItemCompra>

    @Update
    suspend fun atualizar(item: ItemCompra)

    @Delete
    suspend fun deletar(item: ItemCompra)

    @Query("DELETE FROM itens_compra WHERE comprado = 1")
    suspend fun limparComprados()
}
