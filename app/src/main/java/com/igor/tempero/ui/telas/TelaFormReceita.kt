package com.igor.tempero.ui.telas

import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igor.tempero.data.local.AppDatabase
import com.igor.tempero.data.local.Receita
import com.igor.tempero.data.local.categorias
import com.igor.tempero.ui.componentes.BarraTopo
import com.igor.tempero.ui.theme.CinzaTexto
import com.igor.tempero.ui.theme.Creme
import com.igor.tempero.ui.theme.Laranja
import com.igor.tempero.ui.theme.VermelhoErro
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "TelaFormReceita"

/**
 * Tela 3 – Formulário de receita.
 * receitaId = -1  -> cadastra uma nova (INSERT)
 * receitaId = id  -> carrega do banco e edita (UPDATE)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaFormReceita(
    receitaId: Int,
    aoVoltar: () -> Unit
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).receitaDao() }
    val editando = receitaId != -1

    var original by remember { mutableStateOf<Receita?>(null) }
    var nome by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(categorias.first()) }
    var tempo by remember { mutableStateOf("") }
    var porcoes by remember { mutableStateOf("") }
    var ingredientes by remember { mutableStateOf("") }
    var modoPreparo by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }

    // Se for edição, busca a receita no banco e preenche os campos
    LaunchedEffect(Unit) {
        if (editando) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val receita = dao.buscarPorId(receitaId)
                    withContext(Dispatchers.Main) {
                        if (receita != null) {
                            original = receita
                            nome = receita.nome
                            categoria = receita.categoria
                            tempo = receita.tempoMinutos.toString()
                            porcoes = receita.porcoes.toString()
                            ingredientes = receita.ingredientes
                            modoPreparo = receita.modoPreparo
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao carregar receita para editar", e)
                }
            }
        }
    }

    val salvar: () -> Unit = {
        val listaIngredientes = ingredientes.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val tempoNumero = tempo.toIntOrNull()
        val porcoesNumero = porcoes.toIntOrNull()

        erro = when {
            nome.isBlank() -> "Digite o nome da receita."
            tempoNumero == null || tempoNumero <= 0 -> "Digite um tempo válido."
            porcoesNumero == null || porcoesNumero <= 0 -> "Digite quantas porções rende."
            listaIngredientes.isEmpty() -> "Coloque pelo menos um ingrediente."
            modoPreparo.isBlank() -> "Descreva o modo de preparo."
            else -> null
        }

        if (erro == null) {
            val receita = Receita(
                id = original?.id ?: 0,               // 0 = o Room gera o id sozinho
                nome = nome.trim(),
                categoria = categoria,
                tempoMinutos = tempoNumero ?: 0,
                porcoes = porcoesNumero ?: 0,
                ingredientes = listaIngredientes.joinToString("\n"),
                modoPreparo = modoPreparo.trim(),
                favorita = original?.favorita ?: false
            )
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (editando) dao.atualizar(receita) else dao.inserir(receita)
                    withContext(Dispatchers.Main) { aoVoltar() }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao salvar receita", e)
                    withContext(Dispatchers.Main) { erro = "Não foi possível salvar. Tente de novo." }
                }
            }
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

            if (erro != null) {
                Text("⚠️ $erro", color = VermelhoErro, fontSize = 14.sp)
            }

            Button(
                onClick = salvar,
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
