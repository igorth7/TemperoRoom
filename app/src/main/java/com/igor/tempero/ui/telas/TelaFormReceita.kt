package com.igor.tempero.ui.telas

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igor.tempero.data.local.categorias
import com.igor.tempero.ui.componentes.BarraTopo
import com.igor.tempero.ui.theme.CinzaTexto
import com.igor.tempero.ui.theme.Creme
import com.igor.tempero.ui.theme.Laranja
import com.igor.tempero.ui.theme.VermelhoErro
import com.igor.tempero.viewmodel.ReceitasViewModel

/**
 * Tela 3 – Formulário de receita (novo ou editar).
 * Os campos são estado da própria tela; a validação e o salvar ficam no ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaFormReceita(
    receitaId: Int,
    viewModel: ReceitasViewModel,
    aoVoltar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editando = receitaId != -1

    var nome by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(categorias.first()) }
    var tempo by remember { mutableStateOf("") }
    var porcoes by remember { mutableStateOf("") }
    var ingredientes by remember { mutableStateOf("") }
    var modoPreparo by remember { mutableStateOf("") }
    var preenchido by remember { mutableStateOf(false) }

    // Ao abrir: limpa erro antigo e, se for edição, pede a receita ao ViewModel
    LaunchedEffect(receitaId) {
        viewModel.limparErroFormulario()
        if (editando) viewModel.selecionarReceita(receitaId)
    }

    val original = uiState.receitaSelecionada?.takeIf { editando && it.id == receitaId }

    // Quando a receita chega do banco, preenche os campos uma vez
    LaunchedEffect(original) {
        if (original != null && !preenchido) {
            nome = original.nome
            categoria = original.categoria
            tempo = original.tempoMinutos.toString()
            porcoes = original.porcoes.toString()
            ingredientes = original.ingredientes
            modoPreparo = original.modoPreparo
            preenchido = true
        }
    }

    Scaffold(
        topBar = { BarraTopo(titulo = if (editando) "Editar receita" else "Nova receita", aoVoltar = aoVoltar) },
        containerColor = Creme,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome da receita") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Categoria", color = CinzaTexto, fontSize = 13.sp)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categorias.forEach { opcao ->
                    FilterChip(
                        selected = categoria == opcao,
                        onClick = { categoria = opcao },
                        label = { Text(opcao) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = tempo,
                    onValueChange = { novo -> tempo = novo.filter { it.isDigit() } },
                    label = { Text("Tempo (min)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = porcoes,
                    onValueChange = { novo -> porcoes = novo.filter { it.isDigit() } },
                    label = { Text("Porções") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = ingredientes,
                onValueChange = { ingredientes = it },
                label = { Text("Ingredientes (um por linha)") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = modoPreparo,
                onValueChange = { modoPreparo = it },
                label = { Text("Modo de preparo") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            val erro = uiState.erroFormulario
            if (erro != null) {
                Text("⚠️ $erro", color = VermelhoErro, fontSize = 14.sp)
            }

            Button(
                onClick = {
                    viewModel.salvarReceita(
                        original = original,
                        nome = nome,
                        categoria = categoria,
                        tempo = tempo,
                        porcoes = porcoes,
                        ingredientes = ingredientes,
                        modoPreparo = modoPreparo,
                        aoTerminar = aoVoltar
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Laranja)
            ) {
                Text(if (editando) "Salvar alterações" else "Salvar receita", fontSize = 16.sp)
            }
        }
    }
}
