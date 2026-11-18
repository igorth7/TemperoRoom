package com.igor.tempero.ui.telas

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.igor.tempero.ui.componentes.EstadoVazio
import com.igor.tempero.ui.componentes.ImagemCategoria
import com.igor.tempero.ui.componentes.TituloSecao
import com.igor.tempero.ui.theme.CinzaTexto
import com.igor.tempero.ui.theme.Creme
import com.igor.tempero.ui.theme.Laranja
import com.igor.tempero.ui.theme.LaranjaClaro
import com.igor.tempero.ui.theme.Marrom
import com.igor.tempero.ui.theme.VermelhoErro
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "TelaDetalheItem"

/**
 * Tela 5 – Detalhe do item.
 * Recebe o id pela rota e busca o item no banco com buscarPorId(id).
 * Também busca a receita de origem (outra tabela) pelo receitaId.
 */
@Composable
fun TelaDetalheItem(
    itemId: Int,
    aoVoltar: () -> Unit,
    aoEditar: () -> Unit,
    aoAbrirReceita: (Int) -> Unit
) {
    val context = LocalContext.current
    val banco = remember { AppDatabase.getDatabase(context) }
    val itemDao = banco.itemCompraDao()
    val receitaDao = banco.receitaDao()

    var item by remember { mutableStateOf<ItemCompra?>(null) }
    var receitaDeOrigem by remember { mutableStateOf<Receita?>(null) }
    var carregou by remember { mutableStateOf(false) }

    val recarregar: () -> Unit = {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val encontrado = itemDao.buscarPorId(itemId)
                val origem = encontrado?.receitaId?.let { receitaDao.buscarPorId(it) }
                withContext(Dispatchers.Main) {
                    item = encontrado
                    receitaDeOrigem = origem
                    carregou = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar o item $itemId", e)
            }
        }
    }

    LaunchedEffect(Unit) {
        recarregar()
    }

    // Grava a alteração no banco e busca de novo
    val salvar: (ItemCompra) -> Unit = { alterado ->
        CoroutineScope(Dispatchers.IO).launch {
            try {
                itemDao.atualizar(alterado)   // UPDATE
                recarregar()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar item", e)
            }
        }
    }

    val remover: () -> Unit = {
        val atual = item
        if (atual != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    itemDao.deletar(atual)   // DELETE
                    withContext(Dispatchers.Main) { aoVoltar() }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao remover item", e)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            BarraTopo(
                titulo = "Item da lista",
                aoVoltar = aoVoltar,
                acoes = {
                    if (item != null) {
                        IconButton(onClick = aoEditar) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )
        },
        containerColor = Creme,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        val atual = item
        if (atual == null) {
            if (carregou) {
                Column(Modifier.padding(padding)) {
                    EstadoVazio("😕", "Item não encontrado", "Ele pode ter sido removido.")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(LaranjaClaro),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (atual.comprado) "✅" else "🛒", fontSize = 56.sp)
                }

                Text(atual.nome, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Marrom)
                Text(
                    if (atual.comprado) "Já comprado" else "Falta comprar",
                    color = if (atual.comprado) Laranja else CinzaTexto,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quantidade", fontSize = 16.sp, color = Marrom, modifier = Modifier.weight(1f))
                        BotaoRedondo("−") {
                            salvar(atual.copy(quantidade = (atual.quantidade - 1).coerceAtLeast(1)))
                        }
                        Text(
                            "${atual.quantidade}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Marrom,
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )
                        BotaoRedondo("+") {
                            salvar(atual.copy(quantidade = atual.quantidade + 1))
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = atual.comprado,
                            onCheckedChange = { salvar(atual.copy(comprado = !atual.comprado)) },
                            colors = CheckboxDefaults.colors(checkedColor = Laranja)
                        )
                        Text("Marcar como comprado", fontSize = 16.sp, color = Marrom)
                    }
                }

                // Extra: de qual receita este item veio (dado da outra tabela)
                TituloSecao("Origem")
                val origem = receitaDeOrigem
                if (origem != null) {
                    Card(
                        modifier = Modifier.clickable { aoAbrirReceita(origem.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ImagemCategoria(origem.categoria, tamanho = 48)
                            Column(Modifier.weight(1f)) {
                                Text(origem.nome, fontWeight = FontWeight.SemiBold, color = Marrom)
                                Text("Toque para abrir a receita", fontSize = 12.sp, color = CinzaTexto)
                            }
                        }
                    }
                } else if (atual.receitaId != null) {
                    Text("A receita de origem foi excluída.", color = CinzaTexto)
                } else {
                    Text("Adicionado à mão na lista de compras.", color = CinzaTexto)
                }

                OutlinedButton(
                    onClick = remover,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VermelhoErro)
                ) {
                    Text("Remover da lista")
                }
            }
        }
    }
}

@Composable
private fun BotaoRedondo(texto: String, aoClicar: () -> Unit) {
    Button(
        onClick = aoClicar,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Laranja)
    ) {
        Text(texto, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}
