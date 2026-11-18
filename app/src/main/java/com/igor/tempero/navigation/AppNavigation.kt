package com.igor.tempero.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.igor.tempero.ui.telas.TelaCompras
import com.igor.tempero.ui.telas.TelaDetalheItem
import com.igor.tempero.ui.telas.TelaDetalheReceita
import com.igor.tempero.ui.telas.TelaFormItem
import com.igor.tempero.ui.telas.TelaFormReceita
import com.igor.tempero.ui.telas.TelaPerfil
import com.igor.tempero.ui.telas.TelaReceitas

/** Um botão da barra inferior (BottomNavigation). */
data class ItemAba(val rota: String, val rotulo: String, val icone: ImageVector)

private val abas = listOf(
    ItemAba(Rotas.RECEITAS, "Receitas", Icons.Filled.Home),
    ItemAba(Rotas.COMPRAS, "Compras", Icons.Filled.ShoppingCart),
    ItemAba(Rotas.PERFIL, "Perfil", Icons.Filled.Person)
)

/**
 * NavHost central do app.
 * No T3 as listas NÃO ficam mais aqui: cada tela lê e grava direto no banco (Room).
 * A navegação só passa o id de uma tela para a outra.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val entradaAtual by navController.currentBackStackEntryAsState()
    val rotaAtual = entradaAtual?.destination?.route
    val mostrarBarra = abas.any { it.rota == rotaAtual }

    Scaffold(
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar {
                    abas.forEach { aba ->
                        NavigationBarItem(
                            selected = rotaAtual == aba.rota,
                            onClick = { navegarPelaBarra(navController, aba.rota) },
                            icon = { Icon(aba.icone, contentDescription = aba.rotulo) },
                            label = { Text(aba.rotulo) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Rotas.RECEITAS,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {
            // ---------- 1. Lista de receitas ----------
            composable(Rotas.RECEITAS) {
                TelaReceitas(
                    aoAbrirReceita = { id -> navController.navigate(Rotas.detalheReceita(id)) },
                    aoNovaReceita = { navController.navigate(Rotas.formReceita()) }
                )
            }

            // ---------- 2. Detalhe da receita (o id vem pela rota) ----------
            composable(
                route = Rotas.DETALHE_RECEITA,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: -1
                TelaDetalheReceita(
                    receitaId = id,
                    aoVoltar = { navController.popBackStack() },
                    aoEditar = { navController.navigate(Rotas.formReceita(id)) },
                    aoAbrirItem = { idItem -> navController.navigate(Rotas.detalheItem(idItem)) }
                )
            }

            // ---------- 3. Formulário de receita (novo ou editar) ----------
            composable(
                route = Rotas.FORM_RECEITA,
                arguments = listOf(navArgument("id") {
                    type = NavType.IntType
                    defaultValue = -1
                })
            ) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: -1
                TelaFormReceita(
                    receitaId = id,
                    aoVoltar = { navController.popBackStack() }
                )
            }

            // ---------- 4. Lista de compras ----------
            composable(Rotas.COMPRAS) {
                TelaCompras(
                    aoAbrirItem = { id -> navController.navigate(Rotas.detalheItem(id)) },
                    aoNovoItem = { navController.navigate(Rotas.formItem()) }
                )
            }

            // ---------- 5. Detalhe do item (o id vem pela rota) ----------
            composable(
                route = Rotas.DETALHE_ITEM,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: -1
                TelaDetalheItem(
                    itemId = id,
                    aoVoltar = { navController.popBackStack() },
                    aoEditar = { navController.navigate(Rotas.formItem(id)) },
                    aoAbrirReceita = { idReceita -> navController.navigate(Rotas.detalheReceita(idReceita)) }
                )
            }

            // ---------- 6. Formulário de item (novo ou editar) ----------
            composable(
                route = Rotas.FORM_ITEM,
                arguments = listOf(navArgument("id") {
                    type = NavType.IntType
                    defaultValue = -1
                })
            ) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: -1
                TelaFormItem(
                    itemId = id,
                    aoVoltar = { navController.popBackStack() }
                )
            }

            // ---------- 7. Perfil ----------
            composable(Rotas.PERFIL) {
                TelaPerfil(
                    aoAbrirReceita = { id -> navController.navigate(Rotas.detalheReceita(id)) }
                )
            }
        }
    }
}

/** Navegação da barra: não empilha várias cópias da mesma aba. */
private fun navegarPelaBarra(navController: NavHostController, rota: String) {
    navController.navigate(rota) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
