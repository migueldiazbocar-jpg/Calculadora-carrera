package com.calculadoracarrera.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.calculadoracarrera.app.ui.RaceCalculatorScreen
import com.calculadoracarrera.app.ui.theme.CalculadoraCarreraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculadoraCarreraTheme {
                RaceCalculatorScreen()
            }
        }
    }
}
