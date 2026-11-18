package com.igor.tempero.ui.telas

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igor.tempero.data.local.AppDatabase
import com.igor.tempero.data.local.ItemCompra
import com.igor.tempero.data.local.Receita
import com.igor.tempero.ui.componentes.BarraTopo
import com.igor.tempero.ui.componentes.CardNumero
import com.igor.tempero.ui.componentes.EstadoVazio
import com.igor.tempero.ui.componentes.TituloSecao
import com.igor.tempero.ui.componentes.emojiCategoria
import com.igor.tempero.ui.theme.CinzaTexto
import com.igor.tempero.ui.theme.Creme
import com.igor.tempero.ui.theme.Laranja
import com.igor.tempero.ui.theme.LaranjaClaro
import com.igor.tempero.ui.theme.Linha
import com.igor.tempero.ui.theme.Marrom
import com.igor.tempero.ui.theme.VermelhoErro
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "TelaDetalheReceita"

/**
 * Tela 2 – Detalhe da receita.
 * Recebe só o id pela rota e BUSCA a receita no banco com buscarPorId(id).
 * Extra: cruza com a lista de compras (quantos ingredientes já estão lá
 * e botão para adicionar os que faltam).
 */
@Composable
fun TelaDetalheReceita(
    receitaId: Int,
    aoVoltar: () -> Unit,
    aoEditar: () -> Unit,
    aoAbrirItem: (Int) -> Unit
) {
    val context = LocalContext.current
    val banco = remember { AppDatabase.getDatabase(context) }
    val receitaDao = banco.receitaDao()
    val itemDao = banco.itemCompraDao()

    var receita by remember { mutableStateOf<Receita?>(null) }
    val itens = remember { mutableStateListOf<ItemCompra>() }
    var carregou by remember { mutableStateOf(false) }
    var confirmarExclusao by remember { mutableStateOf(false) }

    val recarregar: () -> Unit = {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val encontrada = receitaDao.buscarPorId(receitaId)   // consulta filtrada pelo id
                val todosItens = itemDao.buscarTodos()
                withContext(Dispatchers.Main) {
                    receita = encontrada
                    itens.clear()
                    itens.addAll(todosItens)
                    carregou = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar a receita $receitaId", e)
            }
        }
    }

    LaunchedEffect(Unit) {
        recarregar()
    }

    val alternarFavorita: () -> Unit = {
        val atual = receita
        if (atual != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    receitaDao.atualizar(atual.copy(favorita = !atual.favorita))
                    recarregar()
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao favoritar", e)
                }
            }
        }
    }

    val adicionarIngredientes: (List<String>) -> Unit = { faltando ->
        CoroutineScope(Dispatchers.IO).launch {
            try {
                faltando.forEach { ingrediente ->
                    itemDao.inserir(ItemCompra(nome = ingrediente, receitaId = receitaId))
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "${faltando.size} ingrediente(s) adicionado(s) à lista", Toast.LENGTH_SHORT).show()
                }
                recarregar()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao adicionar ingredientes", e)
            }
        }
    }

    val excluir: () -> Unit = {
        val atual = receita
        if (atual != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    receitaDao.deletar(atual)   // DELETE
                    withContext(Dispatchers.Main) { aoVoltar() }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao excluir", e)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            BarraTopo(
                titulo = receita?.nome ?: "Receita",
                aoVoltar = aoVoltar,
                acoes = {
                    val atual = receita
                    if (atual != null) {
                        IconButton(onClick = alternarFavorita) {
                            Icon(
                                if (atual.favorita) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favoritar"
                            )
                        }
                        IconButton(onClick = aoEditar) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { confirmarExclusao = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                        }
                    }
                }
            )
        },
        containerColor = Creme,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        val atual = receita
        if (atual == null) {
            if (carregou) {
                Column(Modifier.padding(padding)) {
                    EstadoVazio("😕", "Receita não encontrada", "Ela pode ter sido excluída.")
                }
            }
        } else {
            val ingredientes = atual.listaIngredientes()
            // Cruzamento das duas tabelas: ingredientes que já estão na lista de compras
            val naLista = ingredientes.filter { ingrediente ->
                itens.any { it.nome.equals(ingrediente, ignoreCase = true) }
            }
            val faltando = ingredientes - naLista.toSet()
            val itensDaReceita = itens.filter { it.receitaId == atual.id }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(LaranjaClaro),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emojiCategoria(atual.categoria), fontSize = 72.sp)
                }

                Text(atual.nome, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Marrom)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CardNumero("${atual.tempoMinutos}", "minutos", Modifier.weight(1f))
                    CardNumero("${atual.porcoes}", "porções", Modifier.weight(1f))
                    CardNumero(atual.categoria, "categoria", Modifier.weight(1f))
                }

                // ---------- Extra: receita x lista de compras ----------
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = Laranja)
                            Text(
                                "  ${naLista.size} de ${ingredientes.size} ingredientes já estão na lista",
                                fontWeight = FontWeight.SemiBold,
                                color = Marrom
                            )
                        }
                        LinearProgressIndicator(
                            progress = { naLista.size.toFloat() / ingredientes.size.coerceAtLeast(1) },
                            modifier = Modifier.fillMaxWidth(),
                            color = Laranja,
                            trackColor = Linha
                        )
                        Button(
                            onClick = { adicionarIngredientes(faltando) },
                            enabled = faltando.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Laranja)
                        ) {
                            Text(if (faltando.isNotEmpty()) "Adicionar ${faltando.size} ingrediente(s) que faltam" else "Tudo na lista ✓")
                        }
                    }
                }

                TituloSecao("Ingredientes")
                ingredientes.forEach { ingrediente ->
                    val estaNaLista = naLista.contains(ingrediente)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (estaNaLista) "🛒" else "•", fontSize = 16.sp, color = Laranja)
                        Text(
                            "  $ingrediente",
                            fontSize = 15.sp,
                            color = if (estaNaLista) CinzaTexto else Marrom,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                TituloSecao("Modo de preparo")
                Text(atual.modoPreparo, fontSize = 15.sp, color = Marrom, lineHeight = 22.sp)

                if (itensDaReceita.isNotEmpty()) {
                    TituloSecao("Na sua lista de compras")
                    itensDaReceita.forEach { item ->
                        Text(
                            (if (item.comprado) "✅ " else "⬜ ") + "${item.quantidade}x ${item.nome}",
                            fontSize = 15.sp,
                            color = Marrom,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { aoAbrirItem(item.id) }
                                .padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }

    if (confirmarExclusao) {
        AlertDialog(
            onDismissRequest = { confirmarExclusao = false },
            title = { Text("Excluir receita?") },
            text = { Text("\"${receita?.nome}\" será apagada do banco.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarExclusao = false
                    excluir()
                }) { Text("Excluir", color = VermelhoErro) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarExclusao = false }) { Text("Cancelar") }
            }
        )
    }
}
