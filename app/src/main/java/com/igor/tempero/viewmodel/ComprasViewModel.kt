package com.igor.tempero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igor.tempero.data.local.ItemCompra
import com.igor.tempero.data.local.Receita
import com.igor.tempero.data.repository.ItemCompraRepository
import com.igor.tempero.data.repository.ReceitaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Tudo o que as telas da lista de compras precisam mostrar. */
data class ComprasUiState(
    val itens: List<ItemCompra> = emptyList(),
    val receitas: List<Receita> = emptyList(),   // para mostrar de qual receita o item veio
    val carregando: Boolean = true,
    val itemSelecionado: ItemCompra? = null,     // usado no detalhe e na edição
    val selecionadoCarregou: Boolean = false,
    val erroFormulario: String? = null,
    val mensagem: String? = null
) {
    val comprados: Int get() = itens.count { it.comprado }

    /** Receita de onde o item veio (ou null se foi adicionado à mão). */
    fun receitaDe(item: ItemCompra): Receita? = receitas.find { it.id == item.receitaId }

    /** Ingredientes da receita que ainda NÃO estão na lista de compras. */
    fun ingredientesFaltando(receita: Receita): List<String> =
        receita.listaIngredientes().filter { ingrediente ->
            itens.none { it.nome.equals(ingrediente, ignoreCase = true) }
        }
}

/** ViewModel da lista de compras (lista, detalhe, formulário e o "adicionar ingredientes"). */
class ComprasViewModel(
    private val itemRepository: ItemCompraRepository,
    private val receitaRepository: ReceitaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComprasUiState())
    val uiState: StateFlow<ComprasUiState> = _uiState.asStateFlow()

    private var jobSelecionado: Job? = null

    init {
        viewModelScope.launch {
            itemRepository.buscarTodos().collect { lista ->
                _uiState.update { it.copy(itens = lista, carregando = false) }
            }
        }
        viewModelScope.launch {
            receitaRepository.buscarTodos().collect { lista ->
                _uiState.update { it.copy(receitas = lista) }
            }
        }
    }

    // ---------- Lista ----------

    fun alternarComprado(item: ItemCompra) {
        viewModelScope.launch {
            itemRepository.atualizar(item.copy(comprado = !item.comprado))
        }
    }

    fun mudarQuantidade(item: ItemCompra, diferenca: Int) {
        val nova = (item.quantidade + diferenca).coerceAtLeast(1)
        viewModelScope.launch {
            itemRepository.atualizar(item.copy(quantidade = nova))
        }
    }

    fun remover(item: ItemCompra, aoTerminar: () -> Unit = {}) {
        viewModelScope.launch {
            if (itemRepository.deletar(item)) {
                _uiState.update { it.copy(mensagem = "\"${item.nome}\" removido.") }
                aoTerminar()
            }
        }
    }

    fun limparComprados() {
        viewModelScope.launch {
            if (itemRepository.limparComprados()) {
                _uiState.update { it.copy(mensagem = "Itens comprados removidos.") }
            }
        }
    }

    // ---------- Junta as duas tabelas ----------

    /** Coloca na lista de compras os ingredientes da receita que ainda não estão lá. */
    fun adicionarIngredientes(receita: Receita) {
        val faltando = _uiState.value.ingredientesFaltando(receita)
        viewModelScope.launch {
            faltando.forEach { ingrediente ->
                itemRepository.inserir(ItemCompra(nome = ingrediente, receitaId = receita.id))
            }
            _uiState.update { it.copy(mensagem = "${faltando.size} ingrediente(s) adicionado(s) à lista") }
        }
    }

    // ---------- Detalhe (busca pelo id no banco) ----------

    fun selecionarItem(id: Int) {
        jobSelecionado?.cancel()
        _uiState.update { it.copy(itemSelecionado = null, selecionadoCarregou = false) }
        jobSelecionado = viewModelScope.launch {
            itemRepository.buscarPorId(id).collect { item ->
                _uiState.update { it.copy(itemSelecionado = item, selecionadoCarregou = true) }
            }
        }
    }

    // ---------- Formulário ----------

    fun salvarItem(original: ItemCompra?, nome: String, quantidade: String, aoTerminar: () -> Unit) {
        val qtd = quantidade.toIntOrNull()
        val erro = when {
            nome.isBlank() -> "Digite o nome do item."
            qtd == null || qtd <= 0 -> "A quantidade precisa ser maior que zero."
            else -> null
        }
        if (erro != null) {
            _uiState.update { it.copy(erroFormulario = erro) }
            return
        }

        val item = original?.copy(nome = nome.trim(), quantidade = qtd ?: 1)
            ?: ItemCompra(nome = nome.trim(), quantidade = qtd ?: 1)

        viewModelScope.launch {
            val deuCerto = if (original == null) itemRepository.inserir(item) else itemRepository.atualizar(item)
            if (deuCerto) {
                _uiState.update { it.copy(erroFormulario = null, mensagem = "Item salvo!") }
                aoTerminar()
            } else {
                _uiState.update { it.copy(erroFormulario = "Não foi possível salvar. Tente de novo.") }
            }
        }
    }

    fun limparErroFormulario() {
        _uiState.update { it.copy(erroFormulario = null) }
    }

    fun mensagemExibida() {
        _uiState.update { it.copy(mensagem = null) }
    }
}
