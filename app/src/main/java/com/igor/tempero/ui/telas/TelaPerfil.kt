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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.igor.tempero.ui.componentes.TituloSecao
import com.igor.tempero.ui.componentes.emojiCategoria
import com.igor.tempero.ui.theme.CinzaTexto
import com.igor.tempero.ui.theme.Creme
import com.igor.tempero.ui.theme.Laranja
import com.igor.tempero.ui.theme.Linha
import com.igor.tempero.ui.theme.Marrom
import com.igor.tempero.viewmodel.ComprasViewModel
import com.igor.tempero.viewmodel.ReceitasViewModel

/** Tela 7 – Perfil: resumo calculado com o estado dos 2 ViewModels. */
@Composable
fun TelaPerfil(
    receitasViewModel: ReceitasViewModel,
    comprasViewModel: ComprasViewModel,
    aoAbrirReceita: (Int) -> Unit
) {
    val estadoReceitas by receitasViewModel.uiState.collectAsStateWithLifecycle()
    val estadoCompras by comprasViewModel.uiState.collectAsStateWithLifecycle()

    val receitas = estadoReceitas.receitas
    val itens = estadoCompras.itens
    val favoritas = receitas.filter { it.favorita }
    val comprados = estadoCompras.comprados
    val categoriaPreferida = receitas.groupBy { it.categoria }.maxByOrNull { it.value.size }?.key ?: "-"

    Scaffold(
        topBar = { BarraTopo(titulo = "Perfil") },
        containerColor = Creme,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Laranja),
                    contentAlignment = Alignment.Center
                ) {
                    Text("I", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.padding(start = 14.dp)) {
                    Text("Igor", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Marrom)
                    Text("Categoria preferida: $categoriaPreferida", color = CinzaTexto, fontSize = 14.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CardNumero("${receitas.size}", "receitas", Modifier.weight(1f))
                CardNumero("${favoritas.size}", "favoritas", Modifier.weight(1f))
                CardNumero("${itens.size}", "na lista", Modifier.weight(1f))
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Compras: $comprados de ${itens.size} itens", fontWeight = FontWeight.SemiBold, color = Marrom)
                    LinearProgressIndicator(
                        progress = { if (itens.isEmpty()) 0f else comprados.toFloat() / itens.size },
                        modifier = Modifier.fillMaxWidth(),
                        color = Laranja,
                        trackColor = Linha
                    )
                }
            }

            Text("💾 Tudo fica salvo no banco do celular (Room), organizado em MVVM.", color = CinzaTexto, fontSize = 13.sp)

            TituloSecao("Minhas favoritas")
            if (favoritas.isEmpty()) {
                Text("Toque no coração de uma receita para ela aparecer aqui.", color = CinzaTexto)
            }
            favoritas.forEach { receita ->
                Text(
                    "${emojiCategoria(receita.categoria)}  ${receita.nome}",
                    fontSize = 16.sp,
                    color = Marrom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable { aoAbrirReceita(receita.id) }
                        .padding(14.dp)
                )
            }
        }
    }
}
