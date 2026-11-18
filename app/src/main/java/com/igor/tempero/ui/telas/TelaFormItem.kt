package com.igor.tempero.ui.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.igor.tempero.ui.componentes.BarraTopo
import com.igor.tempero.ui.theme.CinzaTexto
import com.igor.tempero.ui.theme.Creme
import com.igor.tempero.ui.theme.Laranja
import com.igor.tempero.ui.theme.VermelhoErro
import com.igor.tempero.viewmodel.ComprasViewModel

/** Tela 6 – Formulário de item (novo ou editar). Validação e salvar ficam no ViewModel. */
@Composable
fun TelaFormItem(
    itemId: Int,
    viewModel: ComprasViewModel,
    aoVoltar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editando = itemId != -1

    var nome by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("1") }
    var preenchido by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        viewModel.limparErroFormulario()
        if (editando) viewModel.selecionarItem(itemId)
    }

    val original = uiState.itemSelecionado?.takeIf { editando && it.id == itemId }

    LaunchedEffect(original) {
        if (original != null && !preenchido) {
            nome = original.nome
            quantidade = original.quantidade.toString()
            preenchido = true
        }
    }

    Scaffold(
        topBar = { BarraTopo(titulo = if (editando) "Editar item" else "Novo item", aoVoltar = aoVoltar) },
        containerColor = Creme,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("O que você precisa comprar?", color = CinzaTexto, fontSize = 14.sp)

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome do item") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = quantidade,
                onValueChange = { novo -> quantidade = novo.filter { it.isDigit() } },
                label = { Text("Quantidade") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            val erro = uiState.erroFormulario
            if (erro != null) {
                Text("⚠️ $erro", color = VermelhoErro, fontSize = 14.sp)
            }

            Button(
                onClick = { viewModel.salvarItem(original, nome, quantidade, aoTerminar = aoVoltar) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Laranja)
            ) {
                Text(if (editando) "Salvar alterações" else "Adicionar à lista", fontSize = 16.sp)
            }
        }
    }
}
