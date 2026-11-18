package com.igor.tempero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igor.tempero.data.local.Receita
import com.igor.tempero.data.local.receitasExemplo
import com.igor.tempero.data.repository.ReceitaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Tudo o que as telas de receita precisam mostrar, num objeto só. */
data class ReceitasUiState(
    val receitas: List<Receita> = emptyList(),   // o que veio do banco
    val busca: String = "",
    val categoria: String? = null,               // null = todas
    val carregando: Boolean = true,
    val receitaSelecionada: Receita? = null,     // usada no detalhe e na edição
    val selecionadaCarregou: Boolean = false,
    val erroFormulario: String? = null,
    val mensagem: String? = null                 // aparece como Toast
) {
    /** Lista já filtrada pela busca e pela categoria. */
    val filtradas: List<Receita>
        get() = receitas.filter { receita ->
            val bateBusca = busca.isBlank() || receita.nome.contains(busca.trim(), ignoreCase = true)
            val bateCategoria = categoria == null || receita.categoria == categoria
            bateBusca && bateCategoria
        }
}

/**
 * ViewModel das receitas (lista, detalhe e formulário).
 * A tela só chama as funções daqui e observa o uiState.
 */
class ReceitasViewModel(
    private val repository: ReceitaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceitasUiState())
    val uiState: StateFlow<ReceitasUiState> = _uiState.asStateFlow()

    private var jobSelecionada: Job? = null

    init {
        // Inscreve uma vez no Flow do Room: a lista se atualiza sozinha
        viewModelScope.launch {
            repository.buscarTodos().collect { lista ->
                _uiState.update { it.copy(receitas = lista, carregando = false) }
            }
        }
    }

    // ---------- Lista ----------

    fun alterarBusca(texto: String) {
        _uiState.update { it.copy(busca = texto) }
    }

    fun alterarCategoria(categoria: String?) {
        _uiState.update { it.copy(categoria = categoria) }
    }

    fun alternarFavorita(receita: Receita) {
        viewModelScope.launch {
            repository.atualizar(receita.copy(favorita = !receita.favorita))
        }
    }

    fun carregarExemplos() {
        viewModelScope.launch {
            receitasExemplo.forEach { repository.inserir(it) }
            _uiState.update { it.copy(mensagem = "Receitas de exemplo adicionadas!") }
        }
    }

    // ---------- Detalhe (busca pelo id no banco) ----------

    fun selecionarReceita(id: Int) {
        jobSelecionada?.cancel()
        _uiState.update { it.copy(receitaSelecionada = null, selecionadaCarregou = false) }
        jobSelecionada = viewModelScope.launch {
            repository.buscarPorId(id).collect { receita ->
                _uiState.update { it.copy(receitaSelecionada = receita, selecionadaCarregou = true) }
            }
        }
    }

    fun excluir(receita: Receita, aoTerminar: () -> Unit) {
        viewModelScope.launch {
            if (repository.deletar(receita)) {
                _uiState.update { it.copy(mensagem = "\"${receita.nome}\" excluída.") }
                aoTerminar()
            } else {
                _uiState.update { it.copy(mensagem = "Não foi possível excluir.") }
            }
        }
    }

    // ---------- Formulário (novo = INSERT, edição = UPDATE) ----------

    fun salvarReceita(
        original: Receita?,
        nome: String,
        categoria: String,
        tempo: String,
        porcoes: String,
        ingredientes: String,
        modoPreparo: String,
        aoTerminar: () -> Unit
    ) {
        val listaIngredientes = ingredientes.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val tempoNumero = tempo.toIntOrNull()
        val porcoesNumero = porcoes.toIntOrNull()

        // Validação: regra de negócio fica no ViewModel, não na tela
        val erro = when {
            nome.isBlank() -> "Digite o nome da receita."
            tempoNumero == null || tempoNumero <= 0 -> "Digite um tempo válido."
            porcoesNumero == null || porcoesNumero <= 0 -> "Digite quantas porções rende."
            listaIngredientes.isEmpty() -> "Coloque pelo menos um ingrediente."
            modoPreparo.isBlank() -> "Descreva o modo de preparo."
            else -> null
        }
        if (erro != null) {
            _uiState.update { it.copy(erroFormulario = erro) }
            return
        }

        val receita = Receita(
            id = original?.id ?: 0,
            nome = nome.trim(),
            categoria = categoria,
            tempoMinutos = tempoNumero ?: 0,
            porcoes = porcoesNumero ?: 0,
            ingredientes = listaIngredientes.joinToString("\n"),
            modoPreparo = modoPreparo.trim(),
            favorita = original?.favorita ?: false
        )

        viewModelScope.launch {
            val deuCerto = if (original == null) repository.inserir(receita) else repository.atualizar(receita)
            if (deuCerto) {
                val texto = if (original == null) "Receita salva!" else "Receita atualizada!"
                _uiState.update { it.copy(erroFormulario = null, mensagem = texto) }
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
