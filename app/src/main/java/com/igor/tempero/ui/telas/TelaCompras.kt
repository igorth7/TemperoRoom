package com.igor.tempero.ui.telas

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igor.tempero.data.local.AppDatabase
import com.igor.tempero.data.local.ItemCompra
import com.igor.tempero.data.local.Receita
import com.igor.tempero.ui.componentes.BarraTopo
import com.igor.tempero.ui.componentes.EstadoVazio
import com.igor.tempero.ui.theme.CinzaTexto
import com.igor.tempero.ui.theme.Creme
import com.igor.tempero.ui.theme.Laranja
import com.igor.tempero.ui.theme.Linha
import com.igor.tempero.ui.theme.Marrom
import com.igor.tempero.ui.theme.VermelhoErro
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "TelaCompras"

/** Tela 4 – Lista de compras, lida do banco (Room). */
@Composable
fun TelaCompras(
    aoAbrirItem: (Int) -> Unit,
    aoNovoItem: () -> Unit
) {
    val context = LocalContext.current
    val banco = remember { AppDatabase.getDatabase(context) }
    val itemDao = banco.itemCompraDao()
    val receitaDao = banco.receitaDao()

    val itens = remember { mutableStateListOf<ItemCompra>() }
    val receitas = remember { mutableStateListOf<Receita>() }   // só para mostrar de qual receita veio
    var carregou by remember { mutableStateOf(false) }

    val recarregar: () -> Unit = {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listaItens = itemDao.buscarTodos()
                val listaReceitas = receitaDao.buscarTodos()
                withContext(Dispatchers.Main) {
                    itens.clear()
                    itens.addAll(listaItens)
                    receitas.clear()
                    receitas.addAll(listaReceitas)
                    carregou = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar itens", e)
            }
        }
    }

    LaunchedEffect(Unit) {
        recarregar()
    }

    val alternarComprado: (ItemCompra) -> Unit = { item ->
        CoroutineScope(Dispatchers.IO).launch {
            try {
                itemDao.atualizar(item.copy(comprado = !item.comprado))   // UPDATE
                recarregar()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao marcar item", e)
            }
        }
    }

    val remover: (ItemCompra) -> Unit = { item ->
        CoroutineScope(Dispatchers.IO).launch {
            try {
                itemDao.deletar(item)   // DELETE
                recarregar()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover item", e)
            }
        }
    }

    val limparComprados: () -> Unit = {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                itemDao.limparComprados()
                recarregar()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao limpar comprados", e)
            }
        }
    }

    val comprados = itens.count { it.comprado }

    Scaffold(
        topBar = {
            BarraTopo(
                titulo = "Lista de compras",
                acoes = {
                    if (comprados > 0) {
                        TextButton(onClick = limparComprados) {
                            Text("Limpar comprados", color = Laranja)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = aoNovoItem,
                containerColor = Laranja,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Novo item")
            }
        },
        containerColor = Creme,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("$comprados de ${itens.size} itens comprados", color = CinzaTexto, fontSize = 14.sp)
                    LinearProgressIndicator(
                        progress = { if (itens.isEmpty()) 0f else comprados.toFloat() / itens.size },
                        modifier = Modifier.fillMaxWidth(),
                        color = Laranja,
                        trackColor = Linha
                    )
                }
            }

            if (carregou && itens.isEmpty()) {
                item {
                    EstadoVazio("🛒", "Lista vazia", "Toque no + ou adicione os ingredientes de uma receita.")
                }
            }

            items(itens, key = { it.id }) { item ->
                val origem = receitas.find { it.id == item.receitaId }?.nome
                CardItemCompra(
                    item = item,
                    origem = origem,
                    aoClicar = { aoAbrirItem(item.id) },
                    aoAlternarComprado = { alternarComprado(item) },
                    aoRemover = { remover(item) }
                )
            }
        }
    }
}

@Composable
private fun CardItemCompra(
    item: ItemCompra,
    origem: String?,
    aoClicar: () -> Unit,
    aoAlternarComprado: () -> Unit,
    aoRemover: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { aoClicar() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.comprado,
                onCheckedChange = { aoAlternarComprado() },
                colors = CheckboxDefaults.colors(checkedColor = Laranja)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.nome,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.comprado) CinzaTexto else Marrom,
                    textDecoration = if (item.comprado) TextDecoration.LineThrough else TextDecoration.None
                )
                Text(
                    "Qtd: ${item.quantidade}" + (if (origem != null) "  •  da receita $origem" else ""),
                    fontSize = 12.sp,
                    color = CinzaTexto
                )
            }
            IconButton(onClick = aoRemover) {
                Icon(Icons.Filled.Delete, contentDescription = "Remover", tint = VermelhoErro)
            }
        }
    }
}
