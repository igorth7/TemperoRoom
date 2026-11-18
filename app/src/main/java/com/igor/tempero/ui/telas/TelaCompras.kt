package com.igor.tempero.ui.telas

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igor.tempero.data.local.ItemCompra
import com.igor.tempero.ui.componentes.BarraTopo
import com.igor.tempero.ui.componentes.EstadoVazio
import com.igor.tempero.ui.theme.CinzaTexto
import com.igor.tempero.ui.theme.Creme
import com.igor.tempero.ui.theme.Laranja
import com.igor.tempero.ui.theme.Linha
import com.igor.tempero.ui.theme.Marrom
import com.igor.tempero.ui.theme.VermelhoErro
import com.igor.tempero.viewmodel.ComprasViewModel

/** Tela 4 – Lista de compras. Observa o uiState e chama o ComprasViewModel. */
@Composable
fun TelaCompras(
    viewModel: ComprasViewModel,
    aoAbrirItem: (Int) -> Unit,
    aoNovoItem: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val itens = uiState.itens
    val comprados = uiState.comprados

    Scaffold(
        topBar = {
            BarraTopo(
                titulo = "Lista de compras",
                acoes = {
                    if (comprados > 0) {
                        TextButton(onClick = { viewModel.limparComprados() }) {
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

            if (!uiState.carregando && itens.isEmpty()) {
                item {
                    EstadoVazio("🛒", "Lista vazia", "Toque no + ou adicione os ingredientes de uma receita.")
                }
            }

            items(itens, key = { it.id }) { item ->
                CardItemCompra(
                    item = item,
                    origem = uiState.receitaDe(item)?.nome,
                    aoClicar = { aoAbrirItem(item.id) },
                    aoAlternarComprado = { viewModel.alternarComprado(item) },
                    aoRemover = { viewModel.remover(item) }
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
