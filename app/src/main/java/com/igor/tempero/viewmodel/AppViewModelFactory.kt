package com.igor.tempero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.igor.tempero.data.repository.ItemCompraRepository
import com.igor.tempero.data.repository.ReceitaRepository

/**
 * Factory: ensina o Android a criar os ViewModels que recebem
 * um Repository no construtor (injeção de dependência).
 */
class AppViewModelFactory(
    private val receitaRepository: ReceitaRepository,
    private val itemCompraRepository: ItemCompraRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ReceitasViewModel::class.java) ->
                ReceitasViewModel(receitaRepository)
            modelClass.isAssignableFrom(ComprasViewModel::class.java) ->
                ComprasViewModel(itemCompraRepository, receitaRepository)
            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        } as T
    }
}
