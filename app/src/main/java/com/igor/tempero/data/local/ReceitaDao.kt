package com.igor.tempero.data.local

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update

/** DAO 1 – todas as operações da tabela "receitas". */
@Dao
interface ReceitaDao {

    // C – Create
    @Insert
    suspend fun inserir(receita: Receita)

    // R – Read: todas (as mais novas primeiro)
    @Query("SELECT * FROM receitas ORDER BY id DESC")
    suspend fun buscarTodos(): List<Receita>

    // R – Read filtrado: só a receita com esse id (usado na tela de detalhe)
    @Query("SELECT * FROM receitas WHERE id = :id")
    suspend fun buscarPorId(id: Int): Receita?

    // U – Update
    @Update
    suspend fun atualizar(receita: Receita)

    // D – Delete
    @Delete
    suspend fun deletar(receita: Receita)
}
