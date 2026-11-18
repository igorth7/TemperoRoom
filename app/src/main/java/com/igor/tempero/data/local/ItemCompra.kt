package com.igor.tempero.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * ENTIDADE 2 – tabela "itens_compra" no Room.
 * receitaId guarda de qual receita o item veio (null = adicionado à mão).
 */
@Entity(tableName = "itens_compra")
data class ItemCompra(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nome: String,
    val quantidade: Int = 1,
    val comprado: Boolean = false,
    val receitaId: Int? = null
)
