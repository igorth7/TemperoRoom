package com.igor.tempero.ui.telas

import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igor.tempero.data.local.AppDatabase
import com.igor.tempero.data.local.ItemCompra
import com.igor.tempero.ui.componentes.BarraTopo
import com.igor.tempero.ui.theme.CinzaTexto
import com.igor.tempero.ui.theme.Creme
import com.igor.tempero.ui.theme.Laranja
import com.igor.tempero.ui.theme.VermelhoErro
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "TelaFormItem"

/**
 * Tela 6 – Formulário de item da lista de compras.
 * itemId = -1 -> novo (INSERT) | itemId = id -> editar (UPDATE)
 */
@Composable
fun TelaFormItem(
    itemId: Int,
    aoVoltar: () -> Unit
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).itemCompraDao() }
    val editando = itemId != -1

    var original by remember { mutableStateOf<ItemCompra?>(null) }
    var nome by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("1") }
    var erro by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (editando) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val item = dao.buscarPorId(itemId)
                    withContext(Dispatchers.Main) {
                        if (item != null) {
                            original = item
                            nome = item.nome
                            quantidade = item.quantidade.toString()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao carregar item para editar", e)
                }
            }
        }
    }

    val salvar: () -> Unit = {
        val qtd = quantidade.toIntOrNull()
        erro = when {
            nome.isBlank() -> "Digite o nome do item."
            qtd == null || qtd <= 0 -> "A quantidade precisa ser maior que zero."
            else -> null
        }
        if (erro == null) {
            // Na edição mantém os outros campos (comprado, receitaId) com copy
            val item = original?.copy(nome = nome.trim(), quantidade = qtd ?: 1)
                ?: ItemCompra(nome = nome.trim(), quantidade = qtd ?: 1)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (editando) dao.atualizar(item) else dao.inserir(item)
                    withContext(Dispatchers.Main) { aoVoltar() }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao salvar item", e)
                    withContext(Dispatchers.Main) { erro = "Não foi possível salvar. Tente de novo." }
                }
            }
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
                Text(if (editando) "Salvar alterações" else "Adicionar à lista", fontSize = 16.sp)
            }
        }
    }
}
