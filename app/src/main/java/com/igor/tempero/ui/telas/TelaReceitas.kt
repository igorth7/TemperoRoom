package com.igor.tempero.ui.telas

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igor.tempero.data.local.AppDatabase
import com.igor.tempero.data.local.Receita
import com.igor.tempero.data.local.categorias
import com.igor.tempero.data.local.receitasExemplo
import com.igor.tempero.ui.componentes.BarraTopo
import com.igor.tempero.ui.componentes.CardReceita
import com.igor.tempero.ui.componentes.EstadoVazio
import com.igor.tempero.ui.theme.CinzaTexto
import com.igor.tempero.ui.theme.Creme
import com.igor.tempero.ui.theme.Laranja
import com.igor.tempero.ui.theme.VermelhoErro
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "TelaReceitas"

/** Tela 1 – Lista de receitas, lida do banco (Room). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaReceitas(
    aoAbrirReceita: (Int) -> Unit,
    aoNovaReceita: () -> Unit
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).receitaDao() }

    // A lista da tela agora é só uma "cópia" do que está no banco
    val receitas = remember { mutableStateListOf<Receita>() }
    var carregou by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }

    var busca by remember { mutableStateOf("") }
    var categoriaEscolhida by remember { mutableStateOf<String?>(null) }

    // Busca tudo no banco (em segundo plano) e atualiza a tela
    val recarregar: () -> Unit = {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val lista = dao.buscarTodos()
                withContext(Dispatchers.Main) {
                    receitas.clear()
                    receitas.addAll(lista)
                    carregou = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar receitas", e)
                withContext(Dispatchers.Main) { erro = "Não foi possível carregar as receitas." }
            }
        }
    }

    // Roda uma vez quando a tela abre: carrega as receitas do banco
    LaunchedEffect(Unit) {
        recarregar()
    }

    val alternarFavorita: (Receita) -> Unit = { receita ->
        CoroutineScope(Dispatchers.IO).launch {
            try {
                dao.atualizar(receita.copy(favorita = !receita.favorita))   // UPDATE
                recarregar()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao favoritar receita", e)
            }
        }
    }

    val carregarExemplos: () -> Unit = {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                receitasExemplo.forEach { dao.inserir(it) }   // INSERT
                recarregar()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao inserir exemplos", e)
            }
        }
    }

    val filtradas = receitas.filter { receita ->
        val bateBusca = busca.isBlank() || receita.nome.contains(busca.trim(), ignoreCase = true)
        val bateCategoria = categoriaEscolhida == null || receita.categoria == categoriaEscolhida
        bateBusca && bateCategoria
    }

    Scaffold(
        topBar = { BarraTopo(titulo = "🍲 Tempero") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = aoNovaReceita,
                containerColor = Laranja,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nova receita")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    label = { Text("Buscar receita") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = categoriaEscolhida == null,
                            onClick = { categoriaEscolhida = null },
                            label = { Text("Todas") }
                        )
                    }
                    items(categorias) { categoria ->
                        FilterChip(
                            selected = categoriaEscolhida == categoria,
                            onClick = {
                                categoriaEscolhida = if (categoriaEscolhida == categoria) null else categoria
                            },
                            label = { Text(categoria) }
                        )
                    }
                }
            }

            item {
                Column {
                    Text("${filtradas.size} receita(s) salvas no banco", color = CinzaTexto, fontSize = 13.sp)
                    if (erro != null) Text("⚠️ $erro", color = VermelhoErro, fontSize = 14.sp)
                }
            }

            // Banco vazio: oferece carregar as receitas de exemplo
            if (carregou && receitas.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EstadoVazio("📖", "Nenhuma receita no banco", "Cadastre no + ou carregue algumas de exemplo.")
                        Button(
                            onClick = carregarExemplos,
                            colors = ButtonDefaults.buttonColors(containerColor = Laranja)
                        ) {
                            Text("Carregar receitas de exemplo")
                        }
                    }
                }
            } else if (carregou && filtradas.isEmpty()) {
                item {
                    EstadoVazio("🔎", "Nenhuma receita encontrada", "Tente outra busca ou outra categoria.")
                }
            }

            items(filtradas, key = { it.id }) { receita ->
                CardReceita(
                    receita = receita,
                    aoClicar = { aoAbrirReceita(receita.id) },
                    aoFavoritar = { alternarFavorita(receita) }
                )
            }
        }
    }
}
