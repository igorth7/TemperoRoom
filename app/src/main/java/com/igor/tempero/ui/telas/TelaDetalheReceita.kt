package com.igor.tempero.ui.telas

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.igor.tempero.viewmodel.ComprasViewModel
import com.igor.tempero.viewmodel.ReceitasViewModel

/**
 * Tela 2 – Detalhe da receita.
 * Pede ao ViewModel a receita do id da rota (que busca no banco pelo Repository).
 * Usa os 2 ViewModels: receita (ReceitasViewModel) + lista de compras (ComprasViewModel).
 */
@Composable
fun TelaDetalheReceita(
    receitaId: Int,
    receitasViewModel: ReceitasViewModel,
    comprasViewModel: ComprasViewModel,
    aoVoltar: () -> Unit,
    aoEditar: () -> Unit,
    aoAbrirItem: (Int) -> Unit
) {
    val estadoReceitas by receitasViewModel.uiState.collectAsStateWithLifecycle()
    val estadoCompras by comprasViewModel.uiState.collectAsStateWithLifecycle()
    var confirmarExclusao by remember { mutableStateOf(false) }

    LaunchedEffect(receitaId) {
        receitasViewModel.selecionarReceita(receitaId)
    }

    // Só usa a selecionada se for mesmo a deste id
    val receita = estadoReceitas.receitaSelecionada?.takeIf { it.id == receitaId }

    Scaffold(
        topBar = {
            BarraTopo(
                titulo = receita?.nome ?: "Receita",
                aoVoltar = aoVoltar,
                acoes = {
                    if (receita != null) {
                        IconButton(onClick = { receitasViewModel.alternarFavorita(receita) }) {
                            Icon(
                                if (receita.favorita) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
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
        if (receita == null) {
            if (estadoReceitas.selecionadaCarregou) {
                Column(Modifier.padding(padding)) {
                    EstadoVazio("😕", "Receita não encontrada", "Ela pode ter sido excluída.")
                }
            }
        } else {
            val ingredientes = receita.listaIngredientes()
            val faltando = estadoCompras.ingredientesFaltando(receita)
            val naLista = ingredientes - faltando.toSet()
            val itensDaReceita = estadoCompras.itens.filter { it.receitaId == receita.id }

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
                    Text(emojiCategoria(receita.categoria), fontSize = 72.sp)
                }

                Text(receita.nome, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Marrom)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CardNumero("${receita.tempoMinutos}", "minutos", Modifier.weight(1f))
                    CardNumero("${receita.porcoes}", "porções", Modifier.weight(1f))
                    CardNumero(receita.categoria, "categoria", Modifier.weight(1f))
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
                            onClick = { comprasViewModel.adicionarIngredientes(receita) },
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
                Text(receita.modoPreparo, fontSize = 15.sp, color = Marrom, lineHeight = 22.sp)

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

    if (confirmarExclusao && receita != null) {
        AlertDialog(
            onDismissRequest = { confirmarExclusao = false },
            title = { Text("Excluir receita?") },
            text = { Text("\"${receita.nome}\" será apagada do banco.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarExclusao = false
                    receitasViewModel.excluir(receita, aoTerminar = aoVoltar)
                }) { Text("Excluir", color = VermelhoErro) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarExclusao = false }) { Text("Cancelar") }
            }
        )
    }
}
