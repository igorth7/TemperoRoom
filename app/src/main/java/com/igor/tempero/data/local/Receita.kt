package com.igor.tempero.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * ENTIDADE 1 – tabela "receitas" no Room.
 * No T2 era uma data class comum; agora cada objeto vira uma linha da tabela.
 */
@Entity(tableName = "receitas")
data class Receita(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nome: String,
    val categoria: String,
    val tempoMinutos: Int,
    val porcoes: Int,
    val ingredientes: String,   // o Room não guarda List direto: salvo um ingrediente por linha
    val modoPreparo: String,
    val favorita: Boolean = false
) {
    /** Transforma o texto de ingredientes em lista (uma linha = um ingrediente). */
    fun listaIngredientes(): List<String> =
        ingredientes.lines().map { it.trim() }.filter { it.isNotEmpty() }
}
