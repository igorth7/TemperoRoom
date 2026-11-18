package com.igor.tempero.data.local

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

/** DAO 1 – todas as operações da tabela "receitas". */
@Dao
interface ReceitaDao {

    // C – Create
    @Insert
    suspend fun inserir(receita: Receita)

    // R – Read: agora devolve um Flow (não é mais suspend).
    // Sempre que a tabela muda, o Room manda a lista nova sozinho.
    @Query("SELECT * FROM receitas ORDER BY id DESC")
    fun buscarTodos(): Flow<List<Receita>>

    // R – Read filtrado pelo id (também em Flow: o detalhe se atualiza sozinho)
    @Query("SELECT * FROM receitas WHERE id = :id")
    fun buscarPorId(id: Int): Flow<Receita?>

    // U – Update
    @Update
    suspend fun atualizar(receita: Receita)

    // D – Delete
    @Delete
    suspend fun deletar(receita: Receita)
}
