package com.igor.tempero.data.local

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

/** DAO 2 – todas as operações da tabela "itens_compra". */
@Dao
interface ItemCompraDao {

    @Insert
    suspend fun inserir(item: ItemCompra)

    // Flow: a lista de compras se atualiza sozinha quando a tabela muda
    @Query("SELECT * FROM itens_compra ORDER BY comprado ASC, id ASC")
    fun buscarTodos(): Flow<List<ItemCompra>>

    @Query("SELECT * FROM itens_compra WHERE id = :id")
    fun buscarPorId(id: Int): Flow<ItemCompra?>

    @Update
    suspend fun atualizar(item: ItemCompra)

    @Delete
    suspend fun deletar(item: ItemCompra)

    @Query("DELETE FROM itens_compra WHERE comprado = 1")
    suspend fun limparComprados()
}
