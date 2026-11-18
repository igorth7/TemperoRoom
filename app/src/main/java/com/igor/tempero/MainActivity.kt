package com.igor.tempero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.igor.tempero.data.local.AppDatabase
import com.igor.tempero.data.repository.ItemCompraRepository
import com.igor.tempero.data.repository.ReceitaRepository
import com.igor.tempero.navigation.AppNavigation
import com.igor.tempero.ui.theme.TemperoTheme
import com.igor.tempero.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Monta as camadas uma vez: Banco -> DAOs -> Repositories -> Factory
        val banco = AppDatabase.getDatabase(applicationContext)
        val receitaRepository = ReceitaRepository(banco.receitaDao())
        val itemCompraRepository = ItemCompraRepository(banco.itemCompraDao())
        val factory = AppViewModelFactory(receitaRepository, itemCompraRepository)

        setContent {
            TemperoTheme {
                AppNavigation(factory = factory)
            }
        }
    }
}
