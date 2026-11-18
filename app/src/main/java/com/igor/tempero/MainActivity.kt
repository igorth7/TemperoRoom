package com.igor.tempero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.igor.tempero.navigation.AppNavigation
import com.igor.tempero.ui.theme.TemperoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // A Activity só aplica o tema e chama a navegação central
        setContent {
            TemperoTheme {
                AppNavigation()
            }
        }
    }
}
